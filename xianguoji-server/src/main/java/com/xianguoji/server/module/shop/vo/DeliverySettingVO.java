package com.xianguoji.server.module.shop.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeliverySettingVO {

    private BigDecimal minOrderAmount;
    private BigDecimal baseFee;
    private BigDecimal freeAmount;
    private Object timeSlots;
}
