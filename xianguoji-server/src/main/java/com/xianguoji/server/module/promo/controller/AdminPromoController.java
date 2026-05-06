package com.xianguoji.server.module.promo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.promo.dto.AdminPromoQry;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.entity.GroupBuyActivity;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.GroupBuyActivityMapper;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
import com.xianguoji.server.module.promo.service.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "促销管理-商家端")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminPromoController {

    private final CouponMapper couponMapper;
    private final PromotionRuleMapper promotionRuleMapper;
    private final GroupBuyActivityMapper groupBuyActivityMapper;
    private final UserCouponMapper userCouponMapper;
    private final GroupBuyService groupBuyService;
    private final WsNotificationService wsNotificationService;

    // ===== 优惠券统计 =====
    @Operation(summary = "优惠券统计概览")
    @GetMapping("/coupon/stats")
    @AdminRequired
    public R<java.util.Map<String, Object>> couponStats() {
        List<Coupon> allCoupons = couponMapper.selectList(null);
        long activeCount = allCoupons.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();
        int totalReceived = allCoupons.stream().mapToInt(c -> c.getReceivedCount() != null ? c.getReceivedCount() : 0).sum();
        int totalUsed = allCoupons.stream().mapToInt(c -> c.getUsedCount() != null ? c.getUsedCount() : 0).sum();
        String verifyRate = totalReceived > 0 ? String.format("%.1f", totalUsed * 100.0 / totalReceived) : "0";
        // 优惠券贡献营收 = 已核销优惠券关联订单的 pay_amount 之和（简化：用 used_count * avg_amount 估算）
        long couponRevenue = allCoupons.stream()
                .filter(c -> c.getUsedCount() != null && c.getUsedCount() > 0 && c.getType() != null && c.getType() == 1)
                .mapToLong(c -> (long) c.getUsedCount() * (c.getAmount() != null ? c.getAmount().longValue() : 0))
                .sum();
        return R.ok(java.util.Map.of(
                "activeCount", activeCount,
                "totalReceived", totalReceived,
                "verifyRate", verifyRate + "%",
                "couponRevenue", couponRevenue
        ));
    }

    @Operation(summary = "优惠券各状态数量")
    @GetMapping("/coupon/status-counts")
    @AdminRequired
    public R<java.util.Map<String, Long>> couponStatusCounts() {
        long total = couponMapper.selectCount(null);
        long active = couponMapper.selectCount(new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 1));
        long expired = couponMapper.selectCount(new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 2));
        long pending = couponMapper.selectCount(new LambdaQueryWrapper<Coupon>().eq(Coupon::getStatus, 3));
        return R.ok(java.util.Map.of(
                "total", total,
                "1", active,
                "2", expired,
                "3", pending
        ));
    }

    // ===== 优惠券 =====
    @Operation(summary = "优惠券列表")
    @GetMapping("/coupon/list")
    @AdminRequired
    public R<PageVO<Coupon>> couponList(AdminPromoQry qry) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<Coupon>()
                .orderByDesc(Coupon::getCreatedAt);
        if (qry.getStatus() != null) {
            wrapper.eq(Coupon::getStatus, qry.getStatus());
        }
        Page<Coupon> page = couponMapper.selectPage(new Page<>(qry.getPage(), qry.getSize()), wrapper);
        return R.ok(new PageVO<>(page.getTotal(), page.getRecords(), qry.getPage(), qry.getSize()));
    }

    @Operation(summary = "新增优惠券")
    @PostMapping("/coupon")
    @AdminRequired
    public R<Void> addCoupon(@RequestBody Coupon dto) {
        couponMapper.insert(dto);
        if (dto.getStatus() != null && dto.getStatus() == 2) {
            notifyCouponEnded(dto);
        }
        return R.ok();
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/coupon/{id}")
    @AdminRequired
    public R<Void> updateCoupon(@PathVariable Long id, @RequestBody Coupon dto) {
        Coupon old = couponMapper.selectById(id);
        dto.setId(id);
        couponMapper.updateById(dto);
        if (dto.getStatus() != null && dto.getStatus() == 2 && (old == null || old.getStatus() == null || old.getStatus() != 2)) {
            notifyCouponEnded(resolveCoupon(id, old, dto));
        }
        return R.ok();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/coupon/{id}")
    @AdminRequired
    public R<Void> deleteCoupon(@PathVariable Long id) {
        Coupon old = couponMapper.selectById(id);
        couponMapper.deleteById(id);
        if (old != null) {
            wsNotificationService.notifyMarketing("优惠券已删除", "「" + old.getName() + "」优惠券已删除", "/campaign");
        }
        return R.ok();
    }

    // ===== 满减 =====
    @Operation(summary = "满减规则列表")
    @GetMapping("/promotion-rule/list")
    @AdminRequired
    public R<PageVO<PromotionRule>> ruleList(AdminPromoQry qry) {
        LambdaQueryWrapper<PromotionRule> wrapper = new LambdaQueryWrapper<PromotionRule>()
                .orderByDesc(PromotionRule::getMinAmount);
        if (qry.getStatus() != null) {
            wrapper.eq(PromotionRule::getStatus, qry.getStatus());
        }
        Page<PromotionRule> page = promotionRuleMapper.selectPage(new Page<>(qry.getPage(), qry.getSize()), wrapper);
        return R.ok(new PageVO<>(page.getTotal(), page.getRecords(), qry.getPage(), qry.getSize()));
    }

    @Operation(summary = "新增满减规则")
    @PostMapping("/promotion-rule")
    @AdminRequired
    public R<Void> addRule(@RequestBody PromotionRule dto) {
        promotionRuleMapper.insert(dto);
        if (dto.getStatus() != null && dto.getStatus() == 0) {
            notifyRuleEnded(dto);
        }
        return R.ok();
    }

    @Operation(summary = "更新满减规则")
    @PutMapping("/promotion-rule/{id}")
    @AdminRequired
    public R<Void> updateRule(@PathVariable Long id, @RequestBody PromotionRule dto) {
        PromotionRule old = promotionRuleMapper.selectById(id);
        dto.setId(id);
        promotionRuleMapper.updateById(dto);
        if (dto.getStatus() != null && dto.getStatus() == 0 && (old == null || old.getStatus() == null || old.getStatus() != 0)) {
            notifyRuleEnded(resolveRule(id, old, dto));
        }
        return R.ok();
    }

    @Operation(summary = "删除满减规则")
    @DeleteMapping("/promotion-rule/{id}")
    @AdminRequired
    public R<Void> deleteRule(@PathVariable Long id) {
        PromotionRule old = promotionRuleMapper.selectById(id);
        promotionRuleMapper.deleteById(id);
        if (old != null) {
            wsNotificationService.notifyMarketing("满减活动已删除", "「" + old.getName() + "」满减活动已删除", "/campaign");
        }
        return R.ok();
    }

    // ===== 拼团 =====
    @Operation(summary = "拼团数据看板")
    @GetMapping("/group-buy/stats")
    @AdminRequired
    public R<java.util.Map<String, Object>> groupBuyStats() {
        return R.ok(groupBuyService.getStats());
    }

    @Operation(summary = "查询商品对应的拼团活动")
    @GetMapping("/group-buy/by-product/{productId}")
    @AdminRequired
    public R<GroupBuyActivity> groupBuyByProduct(@PathVariable Long productId) {
        GroupBuyActivity activity = groupBuyActivityMapper.selectOne(
                new LambdaQueryWrapper<GroupBuyActivity>()
                        .eq(GroupBuyActivity::getProductId, productId)
                        .orderByDesc(GroupBuyActivity::getCreatedAt)
                        .last("LIMIT 1"));
        return R.ok(activity);
    }

    @Operation(summary = "拼团活动列表")
    @GetMapping("/group-buy/list")
    @AdminRequired
    public R<PageVO<GroupBuyActivity>> groupBuyList(AdminPromoQry qry) {
        LambdaQueryWrapper<GroupBuyActivity> wrapper = new LambdaQueryWrapper<GroupBuyActivity>()
                .orderByDesc(GroupBuyActivity::getCreatedAt);
        if (qry.getStatus() != null) {
            wrapper.eq(GroupBuyActivity::getStatus, qry.getStatus());
        }
        Page<GroupBuyActivity> page = groupBuyActivityMapper.selectPage(new Page<>(qry.getPage(), qry.getSize()), wrapper);
        return R.ok(new PageVO<>(page.getTotal(), page.getRecords(), qry.getPage(), qry.getSize()));
    }

    @Operation(summary = "新增拼团活动")
    @PostMapping("/group-buy")
    @AdminRequired
    public R<Void> addGroupBuy(@RequestBody GroupBuyActivity dto) {
        groupBuyActivityMapper.insert(dto);
        if (dto.getStatus() != null && dto.getStatus() == 0) {
            notifyGroupBuyEnded(dto);
        }
        return R.ok();
    }

    @Operation(summary = "更新拼团活动")
    @PutMapping("/group-buy/{id}")
    @AdminRequired
    public R<Void> updateGroupBuy(@PathVariable Long id, @RequestBody GroupBuyActivity dto) {
        GroupBuyActivity old = groupBuyActivityMapper.selectById(id);
        dto.setId(id);
        groupBuyActivityMapper.updateById(dto);
        if (dto.getStatus() != null && dto.getStatus() == 0 && (old == null || old.getStatus() == null || old.getStatus() != 0)) {
            notifyGroupBuyEnded(resolveGroupBuy(id, old, dto));
        }
        return R.ok();
    }

    @Operation(summary = "删除拼团活动")
    @DeleteMapping("/group-buy/{id}")
    @AdminRequired
    public R<Void> deleteGroupBuy(@PathVariable Long id) {
        GroupBuyActivity old = groupBuyActivityMapper.selectById(id);
        groupBuyActivityMapper.deleteById(id);
        if (old != null) {
            wsNotificationService.notifyMarketing("拼团活动已删除", "拼团活动 #" + old.getId() + " 已删除", "/campaign");
        }
        return R.ok();
    }

    private Coupon resolveCoupon(Long id, Coupon old, Coupon dto) {
        Coupon current = couponMapper.selectById(id);
        if (current != null) return current;
        if (old != null) return old;
        return dto;
    }

    private PromotionRule resolveRule(Long id, PromotionRule old, PromotionRule dto) {
        PromotionRule current = promotionRuleMapper.selectById(id);
        if (current != null) return current;
        if (old != null) return old;
        return dto;
    }

    private GroupBuyActivity resolveGroupBuy(Long id, GroupBuyActivity old, GroupBuyActivity dto) {
        GroupBuyActivity current = groupBuyActivityMapper.selectById(id);
        if (current != null) return current;
        if (old != null) return old;
        return dto;
    }

    private void notifyCouponEnded(Coupon coupon) {
        wsNotificationService.notifyMarketing("优惠券已结束", "「" + coupon.getName() + "」优惠券已结束", "/campaign");
    }

    private void notifyRuleEnded(PromotionRule rule) {
        wsNotificationService.notifyMarketing("满减活动已停用", "「" + rule.getName() + "」满减活动已停用", "/campaign");
    }

    private void notifyGroupBuyEnded(GroupBuyActivity activity) {
        wsNotificationService.notifyMarketing("拼团活动已结束", "拼团活动 #" + activity.getId() + " 已结束", "/campaign");
    }
}
