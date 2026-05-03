package com.xianguoji.server.module.shop.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PickupPointVO {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String businessHours;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
