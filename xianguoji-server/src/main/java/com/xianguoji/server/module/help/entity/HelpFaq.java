package com.xianguoji.server.module.help.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_faq")
public class HelpFaq {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String section;
    private String question;
    private String answer;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
}
