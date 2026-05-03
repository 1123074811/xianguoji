package com.xianguoji.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    PENDING_PAY(0, "待付款"),
    PENDING_ACCEPT(1, "待接单"),
    PREPARING(2, "备货中"),
    DELIVERING(3, "配送中"),
    PENDING_PICKUP(4, "待自提"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    REFUNDING(7, "退款中"),
    REFUNDED(8, "已退款");

    private final int code;
    private final String desc;

    public static OrderStatus of(int code) {
        for (OrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Invalid OrderStatus code: " + code);
    }
}
