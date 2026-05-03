package com.xianguoji.server.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String specName;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer isReviewed;
}
