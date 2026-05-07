package com.xianguoji.server.module.message.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.message.dto.FeedbackAddDto;
import com.xianguoji.server.module.message.service.MessageService;
import com.xianguoji.server.module.message.vo.FeedbackVO;
import com.xianguoji.server.module.message.vo.MessageVO;
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
 * TC-API-MESSAGE series: Message controller API tests
 */
@DisplayName("Message Controller API Tests")
class MessageControllerTest extends AbstractApiTest {

    @MockBean
    private MessageService messageService;

    private String userToken = "test_user_token";
    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("GET /api/u/message/page")
    class MessagePageTests {

        @Test
        @DisplayName("TC-API-MESSAGE-001: Should return message page")
        void messagePage_shouldReturnPage_success() throws Exception {
            // Arrange
            List<MessageVO> messages = new ArrayList<>();
            MessageVO message = MessageVO.builder()
                    .id(1L)
                    .type(1)
                    .title("Test Title")
                    .content("Test Content")
                    .isRead(0)
                    .build();
            messages.add(message);
            
            PageVO<MessageVO> pageVO = new PageVO<>(1L, messages, 1, 20);
            when(messageService.getMessagePage(anyLong(), isNull(), eq(1), eq(20))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/u/message/page?page=1&size=20", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).getMessagePage(anyLong(), isNull(), eq(1), eq(20));
        }

        @Test
        @DisplayName("TC-API-MESSAGE-002: Should return 401 when not logged in")
        void messagePage_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/message/page")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/message/{id}")
    class MessageDetailTests {

        @Test
        @DisplayName("TC-API-MESSAGE-003: Should return message detail")
        void messageDetail_shouldReturnDetail_success() throws Exception {
            // Arrange
            Long id = 1L;
            MessageVO message = MessageVO.builder()
                    .id(id)
                    .type(1)
                    .title("Test Title")
                    .content("Test Content")
                    .isRead(0)
                    .build();
            
            when(messageService.getMessage(anyLong(), eq(id))).thenReturn(message);

            // Act
            MvcResult result = performGet("/api/u/message/" + id, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).getMessage(anyLong(), eq(id));
        }

        @Test
        @DisplayName("TC-API-MESSAGE-004: Should return 401 when not logged in")
        void messageDetail_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/message/1")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/u/message/{id}/read")
    class MarkReadTests {

        @Test
        @DisplayName("TC-API-MESSAGE-005: Should mark message as read")
        void markRead_shouldMarkRead_success() throws Exception {
            // Arrange
            Long id = 1L;
            doNothing().when(messageService).markRead(anyLong(), eq(id));

            // Act
            MvcResult result = performPut("/api/u/message/" + id + "/read", null, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).markRead(anyLong(), eq(id));
        }

        @Test
        @DisplayName("TC-API-MESSAGE-006: Should return 401 when not logged in")
        void markRead_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performPut("/api/u/message/1/read", null)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/u/message/read-all")
    class MarkAllReadTests {

        @Test
        @DisplayName("TC-API-MESSAGE-007: Should mark all messages as read")
        void markAllRead_shouldMarkAllRead_success() throws Exception {
            // Arrange
            doNothing().when(messageService).markAllRead(anyLong(), isNull());

            // Act
            MvcResult result = performPut("/api/u/message/read-all", null, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).markAllRead(anyLong(), isNull());
        }

        @Test
        @DisplayName("TC-API-MESSAGE-008: Should return 401 when not logged in")
        void markAllRead_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performPut("/api/u/message/read-all", null)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/message/unread-count")
    class UnreadCountTests {

        @Test
        @DisplayName("TC-API-MESSAGE-009: Should return unread count")
        void unreadCount_shouldReturnCount_success() throws Exception {
            // Arrange
            when(messageService.getUnreadCount(anyLong())).thenReturn(5);

            // Act
            MvcResult result = performGet("/api/u/message/unread-count", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).getUnreadCount(anyLong());
        }

        @Test
        @DisplayName("TC-API-MESSAGE-010: Should return 401 when not logged in")
        void unreadCount_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/message/unread-count")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/message/unread-counts")
    class UnreadCountsTests {

        @Test
        @DisplayName("TC-API-MESSAGE-011: Should return unread counts by type")
        void unreadCounts_shouldReturnCounts_success() throws Exception {
            // Arrange
            Map<String, Integer> counts = Map.of(
                    "system", 2,
                    "promotion", 3,
                    "logistics", 1,
                    "chat", 0,
                    "all", 6
            );
            when(messageService.getUnreadCounts(anyLong())).thenReturn(counts);

            // Act
            MvcResult result = performGet("/api/u/message/unread-counts", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).getUnreadCounts(anyLong());
        }

        @Test
        @DisplayName("TC-API-MESSAGE-012: Should return 401 when not logged in")
        void unreadCounts_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/message/unread-counts")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/u/feedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("TC-API-MESSAGE-013: Should submit feedback successfully")
        void submitFeedback_shouldSubmit_success() throws Exception {
            // Arrange
            FeedbackAddDto dto = new FeedbackAddDto();
            dto.setType("general");
            dto.setContent("Test feedback");
            dto.setContact("test@example.com");
            
            doNothing().when(messageService).submitFeedback(anyLong(), any(FeedbackAddDto.class));

            // Act
            MvcResult result = performPost("/api/u/feedback", dto, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).submitFeedback(anyLong(), any(FeedbackAddDto.class));
        }

        @Test
        @DisplayName("TC-API-MESSAGE-014: Should return 401 when not logged in")
        void submitFeedback_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            FeedbackAddDto dto = new FeedbackAddDto();

            // Act
            MvcResult result = performPost("/api/u/feedback", dto)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/feedback/page")
    class FeedbackPageTests {

        @Test
        @DisplayName("TC-API-MESSAGE-015: Should return feedback page")
        void feedbackPage_shouldReturnPage_success() throws Exception {
            // Arrange
            List<FeedbackVO> feedbacks = new ArrayList<>();
            FeedbackVO feedback = FeedbackVO.builder()
                    .id(1L)
                    .userId(1L)
                    .type("general")
                    .content("Test feedback")
                    .status(0)
                    .build();
            feedbacks.add(feedback);
            
            PageVO<FeedbackVO> pageVO = new PageVO<>(1L, feedbacks, 1, 20);
            when(messageService.getFeedbackPage(1, 20)).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/admin/feedback/page?page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).getFeedbackPage(1, 20);
        }

        @Test
        @DisplayName("TC-API-MESSAGE-016: Should return 403 when not admin")
        void feedbackPage_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/feedback/page", userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/feedback/{id}/handle")
    class HandleFeedbackTests {

        @Test
        @DisplayName("TC-API-MESSAGE-017: Should handle feedback successfully")
        void handleFeedback_shouldHandle_success() throws Exception {
            // Arrange
            Long id = 1L;
            doNothing().when(messageService).handleFeedback(id);

            // Act
            MvcResult result = performPut("/api/admin/feedback/" + id + "/handle", null, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).handleFeedback(id);
        }

        @Test
        @DisplayName("TC-API-MESSAGE-018: Should return 403 when not admin")
        void handleFeedback_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performPut("/api/admin/feedback/1/handle", null, userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/message/broadcast")
    class BroadcastTests {

        @Test
        @DisplayName("TC-API-MESSAGE-019: Should broadcast message successfully")
        void broadcast_shouldBroadcast_success() throws Exception {
            // Arrange
            Map<String, String> body = Map.of(
                    "title", "Test Title",
                    "content", "Test Content",
                    "linkUrl", "http://test.com"
            );
            doNothing().when(messageService).broadcast(anyString(), anyString(), anyString());

            // Act
            MvcResult result = performPost("/api/admin/message/broadcast", body, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(messageService).broadcast(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("TC-API-MESSAGE-020: Should return 403 when not admin")
        void broadcast_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Map<String, String> body = Map.of("title", "Test");

            // Act
            MvcResult result = performPost("/api/admin/message/broadcast", body, userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }
}



