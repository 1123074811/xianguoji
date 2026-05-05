package com.xianguoji.server.module.promo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponVO {

    private Long id;
    private Long couponId;
    private Integer status;
    private LocalDateTime expireAt;
    private CouponVO coupon;
    private String unavailableReason;
}
