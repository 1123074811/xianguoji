package com.xianguoji.server.module.cart.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemVO {

    private Long id;
    private Long productId;
    private Long skuId;
    private String productName;
    private String mainImage;
    private String specName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer productStatus;
    private Integer quantity;
    private Integer selected;
    private BigDecimal subtotal;
}
