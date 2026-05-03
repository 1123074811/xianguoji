package com.xianguoji.server.module.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "review", autoResultMap = true)
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long userId;
    private Integer rating;
    private Integer freshnessRating;
    private Integer valueRating;
    private Integer packageRating;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private Integer isAnonymous;
    private Integer isHidden;
    private String merchantReply;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
