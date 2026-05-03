package com.xianguoji.server.module.promo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("group_buy_activity")
public class GroupBuyActivity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long skuId;
    private BigDecimal groupPrice;
    private Integer groupSize;
    private Integer validHours;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalJoinCount;
    private Integer successCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
