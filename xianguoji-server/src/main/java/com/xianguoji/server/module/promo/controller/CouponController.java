package com.xianguoji.server.module.promo.controller;

import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.promo.dto.UsableCouponQry;
import com.xianguoji.server.module.promo.service.PromoService;
import com.xianguoji.server.module.promo.vo.CouponVO;
import com.xianguoji.server.module.promo.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "优惠券")
@RestController
@RequiredArgsConstructor
public class CouponController {

    private final PromoService promoService;

    @Operation(summary = "当前可领券列表")
    @GetMapping("/api/pub/coupon/list")
    public R<List<CouponVO>> couponList() {
        Long uid = LoginContext.uidOptional();
        return R.ok(promoService.getAvailableCoupons(uid));
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/api/u/coupon/{id}/receive")
    @LoginRequired
    public R<Void> receive(@PathVariable Long id) {
        promoService.receiveCoupon(LoginContext.uid(), id);
        return R.ok();
    }

    @Operation(summary = "我的券")
    @GetMapping("/api/u/coupon/list")
    @LoginRequired
    public R<List<UserCouponVO>> myCoupons(@RequestParam(required = false) Integer status) {
        return R.ok(promoService.getUserCoupons(LoginContext.uid(), status));
    }

    @Operation(summary = "计算可用券")
    @PostMapping("/api/u/coupon/usable")
    @LoginRequired
    public R<List<UserCouponVO>> usableCoupons(@Valid @RequestBody UsableCouponQry qry) {
        return R.ok(promoService.getUsableCoupons(LoginContext.uid(), qry));
    }
}
