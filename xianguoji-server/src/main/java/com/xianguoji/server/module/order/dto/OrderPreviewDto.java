package com.xianguoji.server.module.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderPreviewDto {

    private Long addressId;
    private Long pickupPointId;
    @NotNull(message = "配送方式不能为空")
    private Integer deliveryType; // 1配送 2自提
    private String deliveryTime;
    private List<Long> cartItemIds; // null=全选
    private Long userCouponId;
    private String userRemark;
}
