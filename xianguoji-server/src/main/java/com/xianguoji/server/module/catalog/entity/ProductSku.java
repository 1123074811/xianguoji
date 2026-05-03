package com.xianguoji.server.module.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_sku")
public class ProductSku {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String specName;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer sales;
    private Integer isDefault;
    private Integer status;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
