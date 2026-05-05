package com.xianguoji.server.module.review.vo;

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
public class ReviewVO {

    private Long id;
    private Long productId;
    private String userName;
    private String userAvatar;
    private Integer rating;
    private Integer freshnessRating;
    private Integer valueRating;
    private Integer packageRating;
    private String content;
    private List<String> images;
    private Integer isAnonymous;
    private String merchantReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
