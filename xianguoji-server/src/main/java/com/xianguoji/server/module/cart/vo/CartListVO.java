package com.xianguoji.server.module.cart.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartListVO {

    private List<CartItemVO> items;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String promotionTip;
}
