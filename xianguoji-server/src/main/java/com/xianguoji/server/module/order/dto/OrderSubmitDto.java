package com.xianguoji.server.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitDto {

    private Long addressId;
    private Long pickupPointId;
    @NotNull(message = "配送方式不能为空")
    private Integer deliveryType;
    private String deliveryTime;
    private List<Long> cartItemIds;
    private Long userCouponId;
    private String userRemark;
    private String payMethod; // wechat / alipay
    private Long groupBuyActivityId; // 拼团活动ID（拼团下单时传）
    private Long groupBuyInstanceId; // 参团实例ID（参团时传）
}
