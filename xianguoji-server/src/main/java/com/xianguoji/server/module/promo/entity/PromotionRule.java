package com.xianguoji.server.module.promo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("promotion_rule")
public class PromotionRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
}
