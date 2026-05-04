package com.xianguoji.server.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_notification")
public class AdminNotification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private String linkUrl;
    private Integer isRead;
    private LocalDateTime createdAt;
}
