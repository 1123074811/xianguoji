package com.xianguoji.server.module.promo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupBuyLaunchDto {

    @NotNull(message = "拼团活动ID不能为空")
    private Long activityId;

    private Long addressId;
    private Long pickupPointId;
    private Integer deliveryType;
    private String deliveryTime;
    private String userRemark;
    private String payMethod;
}
