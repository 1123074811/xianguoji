package com.xianguoji.server.module.review.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSummaryVO {

    private long totalCount;
    private Double avgRating;
    private long goodCount;
    private long middleCount;
    private long badCount;
    private long withImageCount;
}
