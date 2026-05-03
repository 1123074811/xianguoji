package com.xianguoji.server.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName(value = "delivery_setting", autoResultMap = true)
public class DeliverySetting {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private BigDecimal minOrderAmount;
    private BigDecimal baseFee;
    private BigDecimal freeAmount;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object timeSlots;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object serviceArea;
    private LocalDateTime updatedAt;
}
