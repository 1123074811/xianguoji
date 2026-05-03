package com.xianguoji.server.module.catalog.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVO {

    private Long id;
    private String name;
    private String subtitle;
    private Long categoryId;
    private String mainImage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalStock;
    private Integer sales;
    private Integer isRecommend;
    private Integer supportDelivery;
    private Integer supportPickup;
}
