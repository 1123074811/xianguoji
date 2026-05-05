package com.xianguoji.server.module.review.controller;

import com.xianguoji.server.common.annotation.AdminRequired;
import com.xianguoji.server.common.annotation.LoginRequired;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.R;
import com.xianguoji.server.common.security.LoginContext;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.review.dto.ReviewAddDto;
import com.xianguoji.server.module.review.service.ReviewService;
import com.xianguoji.server.module.review.vo.ReviewSummaryVO;
import com.xianguoji.server.module.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "评价")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "待评价订单项")
    @GetMapping("/api/u/review/pending")
    @LoginRequired
    public R<List<OrderItem>> pending() {
        return R.ok(reviewService.getPendingReviews(LoginContext.uid()));
    }

    @Operation(summary = "我的评价列表")
    @GetMapping("/api/u/review/my")
    @LoginRequired
    public R<PageVO<ReviewVO>> myReviews(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(reviewService.getMyReviews(LoginContext.uid(), page, size));
    }

    @Operation(summary = "提交评价")
    @PostMapping("/api/u/review")
    @LoginRequired
    public R<Void> submit(@Valid @RequestBody ReviewAddDto dto) {
        reviewService.submitReview(LoginContext.uid(), dto);
        return R.ok();
    }

    @Operation(summary = "商品评价分页")
    @GetMapping("/api/pub/review/product/{productId}")
    public R<PageVO<ReviewVO>> productReviews(@PathVariable Long productId,
                                               @RequestParam(required = false) String filter,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(reviewService.getProductReviews(productId, filter, page, size));
    }

    @Operation(summary = "评价摘要")
    @GetMapping("/api/pub/review/product/{productId}/summary")
    public R<ReviewSummaryVO> summary(@PathVariable Long productId) {
        return R.ok(reviewService.getReviewSummary(productId));
    }

    @Operation(summary = "商家端评价列表")
    @GetMapping("/api/admin/review/page")
    @AdminRequired
    public R<PageVO<ReviewVO>> adminPage(@RequestParam(required = false) String filter,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(reviewService.adminReviewPage(filter, page, size));
    }

    @Operation(summary = "回复评价")
    @PostMapping("/api/admin/review/{id}/reply")
    @AdminRequired
    public R<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reviewService.replyReview(id, body.get("reply"));
        return R.ok();
    }

    @Operation(summary = "隐藏/显示评价")
    @PutMapping("/api/admin/review/{id}/hidden")
    @AdminRequired
    public R<Void> toggleHidden(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        reviewService.toggleHidden(id, body.get("hidden"));
        return R.ok();
    }
}
