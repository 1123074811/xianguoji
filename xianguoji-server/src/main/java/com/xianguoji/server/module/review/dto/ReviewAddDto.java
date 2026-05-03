package com.xianguoji.server.module.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReviewAddDto {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "订单项ID不能为空")
    private Long orderItemId;

    @NotNull(message = "评分不能为空")
    @Min(1) @Max(5)
    private Integer rating;

    @Min(1) @Max(5)
    private Integer freshnessRating;

    @Min(1) @Max(5)
    private Integer valueRating;

    @Min(1) @Max(5)
    private Integer packageRating;

    @Size(max = 1000)
    private String content;

    private List<String> images;

    private Integer isAnonymous;
}
