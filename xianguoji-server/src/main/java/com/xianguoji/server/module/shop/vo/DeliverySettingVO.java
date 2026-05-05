package com.xianguoji.server.module.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySettingVO {

    private BigDecimal minOrderAmount;
    private BigDecimal baseFee;
    private BigDecimal freeAmount;
    private Object timeSlots;
}
