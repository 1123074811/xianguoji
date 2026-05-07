package com.xianguoji.server.module.review.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.order.entity.OrderItem;
import com.xianguoji.server.module.review.dto.ReviewAddDto;
import com.xianguoji.server.module.review.service.ReviewService;
import com.xianguoji.server.module.review.vo.ReviewSummaryVO;
import com.xianguoji.server.module.review.vo.ReviewVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-REVIEW series: Review controller API tests
 */
@DisplayName("Review Controller API Tests")
class ReviewControllerTest extends AbstractApiTest {

    @MockBean
    private ReviewService reviewService;

    private String userToken = "test_user_token";
    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/u/review/pending")
    class PendingReviewsTests {

        @Test
        @DisplayName("TC-API-REVIEW-001: Should return pending reviews")
        void pending_shouldReturnPendingReviews_success() throws Exception {
            // Arrange
            List<OrderItem> items = new ArrayList<>();
            OrderItem item = new OrderItem();
            item.setId(1L);
            item.setProductName("Apple");
            item.setIsReviewed(0);
            items.add(item);
            
            when(reviewService.getPendingReviews(anyLong())).thenReturn(items);

            // Act
            MvcResult result = performGet("/api/u/review/pending", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).getPendingReviews(anyLong());
        }

        @Test
        @DisplayName("TC-API-REVIEW-002: Should return 401 when not logged in")
        void pending_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/review/pending")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/review/my")
    class MyReviewsTests {

        @Test
        @DisplayName("TC-API-REVIEW-003: Should return my reviews")
        void myReviews_shouldReturnReviews_success() throws Exception {
            // Arrange
            List<ReviewVO> reviews = new ArrayList<>();
            ReviewVO review = ReviewVO.builder()
                    .id(1L)
                    .productId(1L)
                    .rating(5)
                    .content("Good")
                    .build();
            reviews.add(review);
            
            PageVO<ReviewVO> pageVO = new PageVO<>(1L, reviews, 1, 20);
            when(reviewService.getMyReviews(anyLong(), eq(1), eq(20))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/u/review/my?page=1&size=20", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).getMyReviews(anyLong(), eq(1), eq(20));
        }

        @Test
        @DisplayName("TC-API-REVIEW-004: Should return 401 when not logged in")
        void myReviews_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/review/my")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/u/review")
    class SubmitReviewTests {

        @Test
        @DisplayName("TC-API-REVIEW-005: Should submit review successfully")
        void submit_shouldSubmitReview_success() throws Exception {
            // Arrange
            ReviewAddDto dto = new ReviewAddDto();
            dto.setOrderItemId(1L);
            dto.setOrderId(1L);
            dto.setRating(5);
            dto.setContent("Good product");
            
            doNothing().when(reviewService).submitReview(anyLong(), any(ReviewAddDto.class));

            // Act
            MvcResult result = performPost("/api/u/review", dto, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).submitReview(anyLong(), any(ReviewAddDto.class));
        }

        @Test
        @DisplayName("TC-API-REVIEW-006: Should return 401 when not logged in")
        void submit_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            ReviewAddDto dto = new ReviewAddDto();

            // Act
            MvcResult result = performPost("/api/u/review", dto)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/pub/review/product/{productId}")
    class ProductReviewsTests {

        @Test
        @DisplayName("TC-API-REVIEW-007: Should return product reviews")
        void productReviews_shouldReturnReviews_success() throws Exception {
            // Arrange
            Long productId = 1L;
            List<ReviewVO> reviews = new ArrayList<>();
            ReviewVO review = ReviewVO.builder()
                    .id(1L)
                    .productId(productId)
                    .rating(5)
                    .content("Good")
                    .build();
            reviews.add(review);
            
            PageVO<ReviewVO> pageVO = new PageVO<>(1L, reviews, 1, 20);
            when(reviewService.getProductReviews(eq(productId), isNull(), eq(1), eq(20))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/review/product/" + productId + "?page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).getProductReviews(eq(productId), isNull(), eq(1), eq(20));
        }

        @Test
        @DisplayName("TC-API-REVIEW-008: Should return reviews with filter")
        void productReviews_shouldReturnReviewsWithFilter_success() throws Exception {
            // Arrange
            Long productId = 1L;
            String filter = "withImage";
            PageVO<ReviewVO> pageVO = new PageVO<>(0L, new ArrayList<>(), 1, 20);
            when(reviewService.getProductReviews(eq(productId), eq(filter), eq(1), eq(20))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/pub/review/product/" + productId + "?filter=" + filter + "&page=1&size=20")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).getProductReviews(eq(productId), eq(filter), eq(1), eq(20));
        }
    }

    @Nested
    @DisplayName("GET /api/pub/review/product/{productId}/summary")
    class SummaryTests {

        @Test
        @DisplayName("TC-API-REVIEW-009: Should return review summary")
        void summary_shouldReturnSummary_success() throws Exception {
            // Arrange
            Long productId = 1L;
            ReviewSummaryVO summary = ReviewSummaryVO.builder()
                    .totalCount(10L)
                    .goodCount(8L)
                    .middleCount(1L)
                    .badCount(1L)
                    .withImageCount(3L)
                    .build();
            
            when(reviewService.getReviewSummary(productId)).thenReturn(summary);

            // Act
            MvcResult result = performGet("/api/pub/review/product/" + productId + "/summary")
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).getReviewSummary(productId);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/review/page")
    class AdminPageTests {

        @Test
        @DisplayName("TC-API-REVIEW-010: Should return admin review page")
        void adminPage_shouldReturnPage_success() throws Exception {
            // Arrange
            List<ReviewVO> reviews = new ArrayList<>();
            ReviewVO review = ReviewVO.builder()
                    .id(1L)
                    .productId(1L)
                    .rating(5)
                    .content("Good")
                    .build();
            reviews.add(review);
            
            PageVO<ReviewVO> pageVO = new PageVO<>(1L, reviews, 1, 20);
            when(reviewService.adminReviewPage(isNull(), eq(1), eq(20))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/admin/review/page?page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).adminReviewPage(isNull(), eq(1), eq(20));
        }

        @Test
        @DisplayName("TC-API-REVIEW-011: Should return 403 when not admin")
        void adminPage_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/review/page", userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/review/{id}/reply")
    class ReplyTests {

        @Test
        @DisplayName("TC-API-REVIEW-012: Should reply review successfully")
        void reply_shouldReply_success() throws Exception {
            // Arrange
            Long id = 1L;
            Map<String, String> body = Map.of("reply", "Thank you for your review");
            doNothing().when(reviewService).replyReview(eq(id), eq("Thank you for your review"));

            // Act
            MvcResult result = performPost("/api/admin/review/" + id + "/reply", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).replyReview(eq(id), eq("Thank you for your review"));
        }

        @Test
        @DisplayName("TC-API-REVIEW-013: Should return 403 when not admin")
        void reply_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Long id = 1L;
            Map<String, String> body = Map.of("reply", "Thank you");

            // Act
            MvcResult result = performPost("/api/admin/review/" + id + "/reply", body, userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/review/{id}/hidden")
    class ToggleHiddenTests {

        @Test
        @DisplayName("TC-API-REVIEW-014: Should toggle hidden status successfully")
        void toggleHidden_shouldToggle_success() throws Exception {
            // Arrange
            Long id = 1L;
            Map<String, Integer> body = Map.of("hidden", 1);
            doNothing().when(reviewService).toggleHidden(eq(id), eq(1));

            // Act
            MvcResult result = performPut("/api/admin/review/" + id + "/hidden", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(reviewService).toggleHidden(eq(id), eq(1));
        }

        @Test
        @DisplayName("TC-API-REVIEW-015: Should return 403 when not admin")
        void toggleHidden_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Long id = 1L;
            Map<String, Integer> body = Map.of("hidden", 1);

            // Act
            MvcResult result = performPut("/api/admin/review/" + id + "/hidden", body, userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }
}

