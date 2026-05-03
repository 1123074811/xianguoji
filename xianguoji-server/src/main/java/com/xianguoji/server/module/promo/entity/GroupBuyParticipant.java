package com.xianguoji.server.module.promo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_buy_participant")
public class GroupBuyParticipant {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long userId;
    private Long orderId;
    private Integer isLeader;
    private LocalDateTime joinedAt;
}
