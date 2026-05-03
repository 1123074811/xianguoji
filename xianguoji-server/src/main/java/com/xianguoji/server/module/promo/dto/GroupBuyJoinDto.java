package com.xianguoji.server.module.promo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupBuyJoinDto {

    private Long addressId;
    private Long pickupPointId;
    private Integer deliveryType;
    private String deliveryTime;
    private String userRemark;
    private String payMethod;
}
