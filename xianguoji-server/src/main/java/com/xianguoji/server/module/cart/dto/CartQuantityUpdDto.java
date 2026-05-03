package com.xianguoji.server.module.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartQuantityUpdDto {

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量最小1")
    private Integer quantity;
}
