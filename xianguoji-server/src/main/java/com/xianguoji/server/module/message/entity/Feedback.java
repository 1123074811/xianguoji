package com.xianguoji.server.module.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "feedback", autoResultMap = true)
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private String contact;
    private Integer status;
    private LocalDateTime createdAt;
}
