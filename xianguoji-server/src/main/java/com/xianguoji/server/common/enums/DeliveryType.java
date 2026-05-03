package com.xianguoji.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryType {

    DELIVERY(1, "同城配送"),
    PICKUP(2, "到店自提");

    private final int code;
    private final String desc;

    public static DeliveryType of(int code) {
        for (DeliveryType d : values()) {
            if (d.code == code) return d;
        }
        throw new IllegalArgumentException("Invalid DeliveryType code: " + code);
    }
}
