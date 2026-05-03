package com.xianguoji.server.module.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String subtitle;
    private Long categoryId;
    private String mainImage;
    private String videoUrl;
    private String description;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalStock;
    private Integer sales;
    private Integer isRecommend;
    private Integer supportDelivery;
    private Integer supportPickup;
    private Integer status;
    private Integer stockWarnThreshold;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
