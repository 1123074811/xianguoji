package com.xianguoji.server.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressAddDto {

    @NotBlank(message = "收货人不能为空")
    @Size(max = 32)
    private String consignee;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private String tag;

    private Integer isDefault;

    private BigDecimal longitude;

    private BigDecimal latitude;
}
