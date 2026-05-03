package com.xianguoji.server.module.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartSelectedDto {

    @NotNull(message = "ids不能为空")
    private List<Long> ids;

    private Integer selected = 1;
}
