package com.xianguoji.server.module.stat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.order.entity.Order;
import com.xianguoji.server.module.order.mapper.OrderMapper;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.entity.UserCoupon;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/admin/customer")
@RequiredArgsConstructor
@AdminRequired
public class CustomerController {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;

    @Operation(summary = "客户列表")
    @GetMapping("/page")
    public R<PageVO<CustomerVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size) {
        Page<User> p = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getRegisterTime));
        var voList = p.getRecords().stream().map(u -> {
            Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, u.getId()));
            BigDecimal totalSpend = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                            .eq(Order::getUserId, u.getId()).eq(Order::getPayStatus, 1))
                    .stream().map(Order::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            Order lastOrder = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, u.getId()).orderByDesc(Order::getCreatedAt).last("LIMIT 1"));
            return CustomerVO.builder()
                    .id(u.getId()).nickname(u.getNickname()).avatar(u.getAvatar())
                    .phone(maskPhone(u.getPhone())).tag(u.getTag())
                    .orderCount(orderCount).totalSpend(totalSpend)
                    .lastOrderTime(lastOrder != null ? lastOrder.getCreatedAt() : null)
                    .registerTime(u.getRegisterTime())
                    .build();
        }).toList();
        return R.ok(new PageVO<>(p.getTotal(), voList, page, size));
    }

    @Operation(summary = "客户详情")
    @GetMapping("/{id}")
    public R<CustomerVO> detail(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return R.fail(4040, "用户不存在");
        Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, id));
        BigDecimal totalSpend = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, id).eq(Order::getPayStatus, 1))
                .stream().map(Order::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return R.ok(CustomerVO.builder()
                .id(u.getId()).nickname(u.getNickname()).avatar(u.getAvatar())
                .phone(u.getPhone()).tag(u.getTag())
                .orderCount(orderCount).totalSpend(totalSpend)
                .registerTime(u.getRegisterTime()).build());
    }

    @Operation(summary = "给指定用户发券")
    @PostMapping("/{id}/coupon")
    public R<Void> sendCoupon(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long couponId = body.get("couponId");
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) return R.fail(4040, "优惠券不存在");
        UserCoupon uc = new UserCoupon();
        uc.setUserId(id);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        uc.setReceivedAt(java.time.LocalDateTime.now());
        if (coupon.getValidType() == 1) uc.setExpireAt(coupon.getEndTime());
        else if (coupon.getValidType() == 2 && coupon.getValidDays() != null)
            uc.setExpireAt(java.time.LocalDateTime.now().plusDays(coupon.getValidDays()));
        userCouponMapper.insert(uc);
        return R.ok();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    @Data
    @Builder
    public static class CustomerVO {
        private Long id;
        private String nickname;
        private String avatar;
        private String phone;
        private String tag;
        private Long orderCount;
        private BigDecimal totalSpend;
        private java.time.LocalDateTime lastOrderTime;
        private java.time.LocalDateTime registerTime;
    }
}
