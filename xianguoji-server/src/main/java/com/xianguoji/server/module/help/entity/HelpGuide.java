package com.xianguoji.server.module.help.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("help_guide")
public class HelpGuide {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String icon;
    private String duration;
    private String url;
    private Integer sort;
    private LocalDateTime createdAt;
}
