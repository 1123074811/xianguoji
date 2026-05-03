package com.xianguoji.server.module.promo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UsableCouponQry {

    @NotNull(message = "商品总价不能为空")
    @DecimalMin(value = "0.00", message = "金额不能为负")
    private BigDecimal totalAmount;

    private List<Long> productIds;
}
