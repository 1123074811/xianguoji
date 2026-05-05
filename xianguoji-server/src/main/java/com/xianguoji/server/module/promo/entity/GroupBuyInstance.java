package com.xianguoji.server.module.promo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_buy_instance")
public class GroupBuyInstance {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long leaderId;
    private Integer currentSize;
    private Integer targetSize;
    private Integer status;
    private LocalDateTime expireAt;
    private LocalDateTime successAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}
