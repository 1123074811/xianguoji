package com.xianguoji.server.module.promo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "coupon", autoResultMap = true)
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal minAmount;
    private Integer total;
    private Integer receivedCount;
    private Integer usedCount;
    private Integer perUserLimit;
    private Integer validType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer scope;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> scopeProductIds;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
