package com.xianguoji.server.module.message.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    private Long id;
    private Integer type;
    private String title;
    private String content;
    private String linkUrl;
    private Integer isRead;
    private LocalDateTime createdAt;
}
