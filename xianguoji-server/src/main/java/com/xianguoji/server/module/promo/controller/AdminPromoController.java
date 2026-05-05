package com.xianguoji.server.module.promo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.module.promo.entity.Coupon;
import com.xianguoji.server.module.promo.entity.GroupBuyActivity;
import com.xianguoji.server.module.promo.entity.PromotionRule;
import com.xianguoji.server.module.promo.mapper.CouponMapper;
import com.xianguoji.server.module.promo.mapper.GroupBuyActivityMapper;
import com.xianguoji.server.module.promo.mapper.PromotionRuleMapper;
import com.xianguoji.server.module.promo.mapper.UserCouponMapper;
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

    // ===== 优惠券 =====
    @Operation(summary = "优惠券列表")
    @GetMapping("/coupon/list")
    @AdminRequired
    public R<List<Coupon>> couponList() {
        return R.ok(couponMapper.selectList(new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getCreatedAt)));
    }

    @Operation(summary = "新增优惠券")
    @PostMapping("/coupon")
    @AdminRequired
    public R<Void> addCoupon(@RequestBody Coupon dto) {
        couponMapper.insert(dto);
        return R.ok();
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/coupon/{id}")
    @AdminRequired
    public R<Void> updateCoupon(@PathVariable Long id, @RequestBody Coupon dto) {
        dto.setId(id);
        couponMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/coupon/{id}")
    @AdminRequired
    public R<Void> deleteCoupon(@PathVariable Long id) {
        couponMapper.deleteById(id);
        return R.ok();
    }

    // ===== 满减 =====
    @Operation(summary = "满减规则列表")
    @GetMapping("/promotion-rule/list")
    @AdminRequired
    public R<List<PromotionRule>> ruleList() {
        return R.ok(promotionRuleMapper.selectList(new LambdaQueryWrapper<PromotionRule>().orderByDesc(PromotionRule::getMinAmount)));
    }

    @Operation(summary = "新增满减规则")
    @PostMapping("/promotion-rule")
    @AdminRequired
    public R<Void> addRule(@RequestBody PromotionRule dto) {
        promotionRuleMapper.insert(dto);
        return R.ok();
    }

    @Operation(summary = "更新满减规则")
    @PutMapping("/promotion-rule/{id}")
    @AdminRequired
    public R<Void> updateRule(@PathVariable Long id, @RequestBody PromotionRule dto) {
        dto.setId(id);
        promotionRuleMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除满减规则")
    @DeleteMapping("/promotion-rule/{id}")
    @AdminRequired
    public R<Void> deleteRule(@PathVariable Long id) {
        promotionRuleMapper.deleteById(id);
        return R.ok();
    }

    // ===== 拼团 =====
    @Operation(summary = "拼团活动列表")
    @GetMapping("/group-buy/list")
    @AdminRequired
    public R<List<GroupBuyActivity>> groupBuyList() {
        return R.ok(groupBuyActivityMapper.selectList(new LambdaQueryWrapper<GroupBuyActivity>().orderByDesc(GroupBuyActivity::getCreatedAt)));
    }

    @Operation(summary = "新增拼团活动")
    @PostMapping("/group-buy")
    @AdminRequired
    public R<Void> addGroupBuy(@RequestBody GroupBuyActivity dto) {
        groupBuyActivityMapper.insert(dto);
        return R.ok();
    }

    @Operation(summary = "更新拼团活动")
    @PutMapping("/group-buy/{id}")
    @AdminRequired
    public R<Void> updateGroupBuy(@PathVariable Long id, @RequestBody GroupBuyActivity dto) {
        dto.setId(id);
        groupBuyActivityMapper.updateById(dto);
        return R.ok();
    }

    @Operation(summary = "删除拼团活动")
    @DeleteMapping("/group-buy/{id}")
    @AdminRequired
    public R<Void> deleteGroupBuy(@PathVariable Long id) {
        groupBuyActivityMapper.deleteById(id);
        return R.ok();
    }
}
