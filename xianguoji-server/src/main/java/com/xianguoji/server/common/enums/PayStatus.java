package com.xianguoji.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayStatus {

    UNPAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUNDED(2, "已退款");

    private final int code;
    private final String desc;

    public static PayStatus of(int code) {
        for (PayStatus p : values()) {
            if (p.code == code) return p;
        }
        throw new IllegalArgumentException("Invalid PayStatus code: " + code);
    }
}
