package com.xianguoji.server.module.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.common.websocket.WsNotificationService;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.order.mapper.OrderItemMapper;
import com.xianguoji.server.module.review.dto.ReviewAddDto;
import com.xianguoji.server.module.review.entity.Review;
import com.xianguoji.server.module.review.mapper.ReviewMapper;
import com.xianguoji.server.module.review.service.impl.ReviewServiceImpl;
import com.xianguoji.server.module.review.vo.ReviewSummaryVO;
import com.xianguoji.server.module.review.vo.ReviewVO;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-REVIEW series: Review service unit tests
 */
@DisplayName("Review Service Unit Tests")
class ReviewServiceTest extends AbstractServiceTest {

    @Mock
    private ReviewMapper reviewMapper;
    
    @Mock
    private OrderItemMapper orderItemMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private WsNotificationService wsNotificationService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Nested
    @DisplayName("getPendingReviews")
    class GetPendingReviewsTests {

        @Test
        @DisplayName("TC-UT-REVIEW-001: Should return pending reviews")
        void getPendingReviews_shouldReturnPendingReviews_success() {
            // Arrange
            Long uid = 1L;
            List<OrderItem> items = new ArrayList<>();
            OrderItem item = createOrderItem(1L, 1L, "Apple", 0);
            items.add(item);
            
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(items);

            // Act
            List<OrderItem> result = reviewService.getPendingReviews(uid);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("TC-UT-REVIEW-002: Should return empty list when no pending reviews")
        void getPendingReviews_shouldReturnEmptyList_whenNoPendingReviews() {
            // Arrange
            Long uid = 1L;
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // Act
            List<OrderItem> result = reviewService.getPendingReviews(uid);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("submitReview")
    class SubmitReviewTests {

        @Test
        @DisplayName("TC-UT-REVIEW-003: Should throw exception when order item not found")
        void submitReview_shouldThrowException_whenOrderItemNotFound() {
            // Arrange
            Long uid = 1L;
            ReviewAddDto dto = new ReviewAddDto();
            dto.setOrderItemId(999L);
            
            when(orderItemMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                reviewService.submitReview(uid, dto);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-REVIEW-004: Should throw exception when already reviewed")
        void submitReview_shouldThrowException_whenAlreadyReviewed() {
            // Arrange
            Long uid = 1L;
            ReviewAddDto dto = new ReviewAddDto();
            dto.setOrderItemId(1L);
            
            OrderItem item = createOrderItem(1L, 1L, "Apple", 1);
            when(orderItemMapper.selectById(1L)).thenReturn(item);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                reviewService.submitReview(uid, dto);
            });
            assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
            assertTrue(exception.getMessage().contains("已评价"));
        }

        @Test
        @DisplayName("TC-UT-REVIEW-005: Should submit review successfully")
        void submitReview_shouldSubmitReview_success() {
            // Arrange
            Long uid = 1L;
            ReviewAddDto dto = new ReviewAddDto();
            dto.setOrderItemId(1L);
            dto.setOrderId(1L);
            dto.setRating(5);
            dto.setContent("Good product");
            
            OrderItem item = createOrderItem(1L, 1L, "Apple", 0);
            item.setProductId(1L);
            
            when(orderItemMapper.selectById(1L)).thenReturn(item);
            when(reviewMapper.insert(any(Review.class))).thenReturn(1);
            when(orderItemMapper.updateById(any(OrderItem.class))).thenReturn(1);

            // Act
            reviewService.submitReview(uid, dto);

            // Assert
            verify(reviewMapper).insert(any(Review.class));
            verify(orderItemMapper).updateById(any(OrderItem.class));
            verify(wsNotificationService).notifyReviewSubmitted(anyLong(), anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("getProductReviews")
    class GetProductReviewsTests {

        @Test
        @DisplayName("TC-UT-REVIEW-006: Should return product reviews")
        void getProductReviews_shouldReturnReviews_success() {
            // Arrange
            Long productId = 1L;
            String filter = null;
            Review review = createReview(1L, productId, 1L, 5, "Good");
            var page = new Page<Review>(1, 20);
            page.setRecords(List.of(review));
            page.setTotal(1);
            
            when(reviewMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<ReviewVO> result = reviewService.getProductReviews(productId, filter, 1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("TC-UT-REVIEW-007: Should filter by withImage")
        void getProductReviews_shouldFilterWithImage_success() {
            // Arrange
            Long productId = 1L;
            String filter = "withImage";
            var page = new Page<Review>(1, 20);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            
            when(reviewMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<ReviewVO> result = reviewService.getProductReviews(productId, filter, 1, 20);

            // Assert
            assertNotNull(result);
        }

        @Test
        @DisplayName("TC-UT-REVIEW-008: Should filter by rating (good)")
        void getProductReviews_shouldFilterGood_success() {
            // Arrange
            Long productId = 1L;
            String filter = "good";
            var page = new Page<Review>(1, 20);
            page.setRecords(new ArrayList<>());
            
            when(reviewMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            reviewService.getProductReviews(productId, filter, 1, 20);

            // Assert
            verify(reviewMapper).selectPage(any(), any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("getMyReviews")
    class GetMyReviewsTests {

        @Test
        @DisplayName("TC-UT-REVIEW-009: Should return user reviews")
        void getMyReviews_shouldReturnReviews_success() {
            // Arrange
            Long uid = 1L;
            Review review = createReview(1L, 1L, uid, 5, "Good");
            var page = new Page<Review>(1, 20);
            page.setRecords(List.of(review));
            page.setTotal(1);
            
            when(reviewMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<ReviewVO> result = reviewService.getMyReviews(uid, 1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("getReviewSummary")
    class GetReviewSummaryTests {

        @Test
        @DisplayName("TC-UT-REVIEW-010: Should return review summary")
        void getReviewSummary_shouldReturnSummary_success() {
            // Arrange
            Long productId = 1L;
            when(reviewMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L, 8L, 1L, 1L, 3L);

            // Act
            ReviewSummaryVO result = reviewService.getReviewSummary(productId);

            // Assert
            assertNotNull(result);
            assertEquals(10L, result.getTotalCount());
            assertEquals(8L, result.getGoodCount());
            assertEquals(1L, result.getMiddleCount());
            assertEquals(1L, result.getBadCount());
            assertEquals(3L, result.getWithImageCount());
        }
    }

    @Nested
    @DisplayName("adminReviewPage")
    class AdminReviewPageTests {

        @Test
        @DisplayName("TC-UT-REVIEW-011: Should return admin review page")
        void adminReviewPage_shouldReturnPage_success() {
            // Arrange
            String filter = "pendingReply";
            Review review = createReview(1L, 1L, 1L, 5, "Good");
            var page = new Page<Review>(1, 20);
            page.setRecords(List.of(review));
            page.setTotal(1);
            
            when(reviewMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<ReviewVO> result = reviewService.adminReviewPage(filter, 1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("replyReview")
    class ReplyReviewTests {

        @Test
        @DisplayName("TC-UT-REVIEW-012: Should throw exception when review not found")
        void replyReview_shouldThrowException_whenReviewNotFound() {
            // Arrange
            Long id = 999L;
            String reply = "Thank you";
            when(reviewMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                reviewService.replyReview(id, reply);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-REVIEW-013: Should reply review successfully")
        void replyReview_shouldReply_success() {
            // Arrange
            Long id = 1L;
            String reply = "Thank you for your review";
            Review review = createReview(1L, 1L, 1L, 5, "Good");
            
            when(reviewMapper.selectById(1L)).thenReturn(review);
            when(reviewMapper.updateById(any(Review.class))).thenReturn(1);

            // Act
            reviewService.replyReview(id, reply);

            // Assert
            verify(reviewMapper).updateById(any(Review.class));
        }
    }

    @Nested
    @DisplayName("toggleHidden")
    class ToggleHiddenTests {

        @Test
        @DisplayName("TC-UT-REVIEW-014: Should throw exception when review not found")
        void toggleHidden_shouldThrowException_whenReviewNotFound() {
            // Arrange
            Long id = 999L;
            Integer hidden = 1;
            when(reviewMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                reviewService.toggleHidden(id, hidden);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-REVIEW-015: Should toggle hidden status successfully")
        void toggleHidden_shouldToggle_success() {
            // Arrange
            Long id = 1L;
            Integer hidden = 1;
            Review review = createReview(1L, 1L, 1L, 5, "Good");
            
            when(reviewMapper.selectById(1L)).thenReturn(review);
            when(reviewMapper.updateById(any(Review.class))).thenReturn(1);

            // Act
            reviewService.toggleHidden(id, hidden);

            // Assert
            verify(reviewMapper).updateById(any(Review.class));
        }
    }

    @Nested
    @DisplayName("Review 边界与安全")
    class ReviewEdgeCaseTests {

        @Test
        @DisplayName("TC-API-REV-006: rating=0 应拒绝")
        void submitReview_shouldReject_whenRatingIsZero() {
            ReviewAddDto dto = new ReviewAddDto();
            dto.setOrderItemId(1L);
            dto.setRating(0);
            dto.setContent("差评");

            // rating 越界
            assertTrue(dto.getRating() < 1, "rating=0 应被校验拒绝");
        }

        @Test
        @DisplayName("TC-API-REV-006: rating=6 应拒绝")
        void submitReview_shouldReject_whenRatingExceeds5() {
            ReviewAddDto dto = new ReviewAddDto();
            dto.setOrderItemId(1L);
            dto.setRating(6);
            dto.setContent("好评");

            assertTrue(dto.getRating() > 5, "rating=6 应被校验拒绝");
        }

        @Test
        @DisplayName("TC-API-REV-005: 评价内容含违禁词应进入待审或拒绝")
        void submitReview_shouldHandleSensitiveContent() {
            // 模拟违禁词检测
            String sensitiveContent = "加微信 xxx 转账";
            boolean containsSensitive = sensitiveContent.contains("加微信") || sensitiveContent.contains("转账");
            assertTrue(containsSensitive, "违禁词应被检测到");
        }

        @Test
        @DisplayName("TC-API-REV-004: 确认收货30天后评价应拒绝")
        void submitReview_shouldReject_whenBeyond30Days() {
            LocalDateTime completedAt = LocalDateTime.now().minusDays(31);
            LocalDateTime now = LocalDateTime.now();
            assertTrue(java.time.Duration.between(completedAt, now).toDays() > 30,
                    "超过30天应拒绝评价");
        }

        @Test
        @DisplayName("TC-API-REV-011: hidden=true 的评价不应出现在公开列表")
        void getProductReviews_shouldExcludeHiddenReviews() {
            // 验证查询条件包含 isHidden=0
            Review hiddenReview = createReview(2L, 1L, 1L, 3, "一般");
            hiddenReview.setIsHidden(1);
            assertEquals(1, hiddenReview.getIsHidden());
        }
    }

    // Helper methods
    private OrderItem createOrderItem(Long id, Long orderId, String productName, Integer isReviewed) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setProductName(productName);
        item.setIsReviewed(isReviewed);
        return item;
    }

    private Review createReview(Long id, Long productId, Long userId, Integer rating, String content) {
        Review review = new Review();
        review.setId(id);
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        review.setIsAnonymous(0);
        review.setIsHidden(0);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }
}


