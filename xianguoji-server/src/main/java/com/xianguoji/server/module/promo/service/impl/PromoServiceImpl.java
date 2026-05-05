package com.xianguoji.server.module.promo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.promo.dto.UsableCouponQry;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.promo.service.PromoService;
import com.xianguoji.server.module.promo.vo.CouponVO;
import com.xianguoji.server.module.promo.vo.UserCouponVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromoServiceImpl implements PromoService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final PromotionRuleMapper promotionRuleMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> couponReceiveScript;

    @PostConstruct
    public void init() {
        couponReceiveScript = new DefaultRedisScript<>();
        couponReceiveScript.setLocation(new org.springframework.core.io.ClassPathResource("lua/coupon_receive.lua"));
        couponReceiveScript.setResultType(Long.class);
    }

    @Override
    public List<CouponVO> getAvailableCoupons(Long uid) {
        List<Coupon> list = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 1));
        return list.stream().map(c -> {
            CouponVO vo = toCouponVO(c);
            if (uid != null) {
                Long userReceived = userCouponMapper.selectCount(
                        new LambdaQueryWrapper<UserCoupon>()
                                .eq(UserCoupon::getUserId, uid)
                                .eq(UserCoupon::getCouponId, c.getId()));
                vo.setUserReceivedCount(userReceived.intValue());
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Long uid, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BizException(ResultCode.COUPON_NOT_AVAILABLE, "优惠券不存在或已结束");
        }

        // Lua 原子校验：总量 + 每人限领
        String totalKey = "xgj:coupon:received:" + couponId;
        String userKey = "xgj:coupon:user:" + couponId + ":" + uid;
        Long result = stringRedisTemplate.execute(couponReceiveScript,
                Arrays.asList(totalKey, userKey),
                String.valueOf(coupon.getTotal()),
                String.valueOf(coupon.getPerUserLimit()));
        if (result == null || result == 0L) {
            throw new BizException(ResultCode.COUPON_NOT_AVAILABLE, "优惠券已被领完");
        }
        if (result == -1L) {
            throw new BizException(ResultCode.COUPON_NOT_AVAILABLE, "每人限领" + coupon.getPerUserLimit() + "张");
        }

        // Lua 已通过 → DB 写入。一旦异常，回滚 Redis 计数避免漏发
        try {
            UserCoupon uc = new UserCoupon();
            uc.setUserId(uid);
            uc.setCouponId(couponId);
            uc.setStatus(0);
            uc.setReceivedAt(LocalDateTime.now());
            if (coupon.getValidType() == 1) {
                uc.setExpireAt(coupon.getEndTime());
            } else if (coupon.getValidType() == 2 && coupon.getValidDays() != null) {
                uc.setExpireAt(LocalDateTime.now().plusDays(coupon.getValidDays()));
            }
            userCouponMapper.insert(uc);

            // DB 原子更新已领取数
            couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                    .eq(Coupon::getId, couponId)
                    .setSql("received_count = received_count + 1"));
        } catch (RuntimeException ex) {
            // 回滚 Redis 计数
            try {
                stringRedisTemplate.opsForValue().decrement(totalKey);
                stringRedisTemplate.opsForValue().decrement(userKey);
            } catch (Exception ignore) {
                log.error("回滚优惠券Redis计数失败 couponId={} uid={}", couponId, uid, ignore);
            }
            throw ex;
        }
    }

    @Override
    public List<UserCouponVO> getUserCoupons(Long uid, Integer status) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, uid);
        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }
        wrapper.orderByDesc(UserCoupon::getReceivedAt);
        List<UserCoupon> list = userCouponMapper.selectList(wrapper);
        return list.stream().map(uc -> {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            return UserCouponVO.builder()
                    .id(uc.getId())
                    .couponId(uc.getCouponId())
                    .status(uc.getStatus())
                    .expireAt(uc.getExpireAt())
                    .coupon(coupon != null ? toCouponVO(coupon) : null)
                    .build();
        }).toList();
    }

    @Override
    public List<UserCouponVO> getUsableCoupons(Long uid, UsableCouponQry qry) {
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, uid)
                        .eq(UserCoupon::getStatus, 0)
                        .ge(UserCoupon::getExpireAt, LocalDateTime.now()));

        List<UserCouponVO> result = new ArrayList<>();
        for (UserCoupon uc : userCoupons) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon == null) continue;

            String reason = null;
            // 检查门槛
            if (qry.getTotalAmount().compareTo(coupon.getMinAmount()) < 0) {
                reason = "未满" + coupon.getMinAmount() + "元";
            }
            // 检查适用范围
            if (coupon.getScope() == 2 && qry.getProductIds() != null) {
                if (coupon.getScopeProductIds() == null || coupon.getScopeProductIds().isEmpty()) {
                    reason = "该商品不可用此券";
                } else {
                    boolean match = qry.getProductIds().stream().anyMatch(coupon.getScopeProductIds()::contains);
                    if (!match) reason = "该商品不在适用范围";
                }
            }

            result.add(UserCouponVO.builder()
                    .id(uc.getId())
                    .couponId(uc.getCouponId())
                    .status(uc.getStatus())
                    .expireAt(uc.getExpireAt())
                    .coupon(toCouponVO(coupon))
                    .unavailableReason(reason)
                    .build());
        }
        return result;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal goodsAmount) {
        List<PromotionRule> rules = promotionRuleMapper.selectList(
                new LambdaQueryWrapper<PromotionRule>()
                        .eq(PromotionRule::getStatus, 1)
                        .orderByDesc(PromotionRule::getMinAmount));
        for (PromotionRule rule : rules) {
            if (goodsAmount.compareTo(rule.getMinAmount()) >= 0) {
                return rule.getDiscount();
            }
        }
        return BigDecimal.ZERO;
    }

    private CouponVO toCouponVO(Coupon c) {
        return CouponVO.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType())
                .amount(c.getAmount())
                .minAmount(c.getMinAmount())
                .total(c.getTotal())
                .receivedCount(c.getReceivedCount())
                .perUserLimit(c.getPerUserLimit())
                .validType(c.getValidType())
                .startTime(c.getStartTime())
                .endTime(c.getEndTime())
                .validDays(c.getValidDays())
                .scope(c.getScope())
                .status(c.getStatus())
                .build();
    }
}
