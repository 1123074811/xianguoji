package com.xianguoji.server.module.promo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
