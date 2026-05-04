package com.xianguoji.server.module.help.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_feedback")
public class HelpFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long staffId;
    private String content;
    private String contact;
    private Integer status;
    private String reply;
    private LocalDateTime createdAt;
}
