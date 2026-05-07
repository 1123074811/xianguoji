package com.xianguoji.server.module.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 0=用户发送 1=商家回复 */
    private Integer senderType;
    /** text=文字 product=商品卡片 image=图片 */
    private String msgType;
    private String content;
    /** 商品卡片时关联的商品ID */
    private Long productId;
    /** 图片URL列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}
