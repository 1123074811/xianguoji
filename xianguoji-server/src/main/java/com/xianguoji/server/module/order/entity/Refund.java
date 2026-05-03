package com.xianguoji.server.module.order.entity;

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
@TableName(value = "refund", autoResultMap = true)
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long userId;
    private Integer type;
    private BigDecimal amount;
    private String reason;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private Integer status;
    private String rejectReason;
    private Long handledBy;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
