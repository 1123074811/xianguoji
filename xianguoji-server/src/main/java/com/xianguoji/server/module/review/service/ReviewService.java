package com.xianguoji.server.module.review.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.review.dto.ReviewAddDto;
import com.xianguoji.server.module.review.vo.ReviewSummaryVO;
import com.xianguoji.server.module.review.vo.ReviewVO;

import java.util.List;

public interface ReviewService {

    List<com.xianguoji.server.module.order.entity.OrderItem> getPendingReviews(Long uid);

    void submitReview(Long uid, ReviewAddDto dto);

    PageVO<ReviewVO> getProductReviews(Long productId, String filter, Integer page, Integer size);

    ReviewSummaryVO getReviewSummary(Long productId);

    PageVO<ReviewVO> adminReviewPage(String filter, Integer page, Integer size);

    void replyReview(Long id, String reply);

    void toggleHidden(Long id, Integer hidden);
}
