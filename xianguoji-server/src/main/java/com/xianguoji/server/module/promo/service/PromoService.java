package com.xianguoji.server.module.promo.service;

import com.xianguoji.server.module.promo.dto.UsableCouponQry;
import com.xianguoji.server.module.promo.vo.CouponVO;
import com.xianguoji.server.module.promo.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

public interface PromoService {

    List<CouponVO> getAvailableCoupons();

    void receiveCoupon(Long uid, Long couponId);

    List<UserCouponVO> getUserCoupons(Long uid, Integer status);

    List<UserCouponVO> getUsableCoupons(Long uid, UsableCouponQry qry);

    BigDecimal calculateDiscount(BigDecimal goodsAmount);
}
