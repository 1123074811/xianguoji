package com.xianguoji.server.module.message.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackVO {

    private Long id;
    private Long userId;
    private String type;
    private String content;
    private List<String> images;
    private String contact;
    private Integer status;
    private LocalDateTime createdAt;
}
