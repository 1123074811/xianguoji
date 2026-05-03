package com.xianguoji.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RefundStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已同意"),
    REJECTED(2, "已拒绝"),
    REFUNDED(3, "已退款");

    private final int code;
    private final String desc;

    public static RefundStatus of(int code) {
        for (RefundStatus r : values()) {
            if (r.code == code) return r;
        }
        throw new IllegalArgumentException("Invalid RefundStatus code: " + code);
    }
}
