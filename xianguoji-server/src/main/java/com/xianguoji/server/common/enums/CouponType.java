package com.xianguoji.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponType {

    FULL_REDUCTION(1, "满减"),
    DISCOUNT(2, "折扣");

    private final int code;
    private final String desc;
}
