package com.xianguoji.server.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notify_setting")
public class NotifySetting {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String eventKey;
    private String eventName;
    private Integer enableVoice;
    private Integer enableSms;
    private Integer enableApp;
    private LocalDateTime updatedAt;
}
