package com.xianguoji.server.module.message.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageVO {

    private Long id;
    private Integer type;
    private String title;
    private String content;
    private String linkUrl;
    private Integer isRead;
    private LocalDateTime createdAt;
}
