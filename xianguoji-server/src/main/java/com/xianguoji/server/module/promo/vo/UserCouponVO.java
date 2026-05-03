package com.xianguoji.server.module.promo.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserCouponVO {

    private Long id;
    private Long couponId;
    private Integer status;
    private LocalDateTime expireAt;
    private CouponVO coupon;
    private String unavailableReason;
}
