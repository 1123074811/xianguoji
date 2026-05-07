package com.xianguoji.server.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ChatSendDto {

    /** text / product / image */
    @NotBlank(message = "消息类型不能为空")
    private String msgType;

    /** 文字内容（msgType=text时必填） */
    @Size(max = 2000)
    private String content;

    /** 商品ID（msgType=product时必填） */
    private Long productId;

    /** 图片URL列表（msgType=image时必填） */
    private List<String> images;
}
