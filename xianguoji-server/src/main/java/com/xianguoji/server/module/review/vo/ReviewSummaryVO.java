package com.xianguoji.server.module.review.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryVO {

    private long totalCount;
    private Double avgRating;
    private long goodCount;
    private long middleCount;
    private long badCount;
    private long withImageCount;
}
