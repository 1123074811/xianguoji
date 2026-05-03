package com.xianguoji.server.module.user.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AddressVO {

    private Long id;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String tag;
    private Integer isDefault;
    private BigDecimal longitude;
    private BigDecimal latitude;
}
