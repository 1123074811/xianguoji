package com.xianguoji.server.module.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.message.dto.FeedbackAddDto;
import com.xianguoji.server.module.message.entity.Feedback;
import com.xianguoji.server.module.message.entity.Message;
import com.xianguoji.server.module.message.mapper.FeedbackMapper;
import com.xianguoji.server.module.message.mapper.MessageMapper;
import com.xianguoji.server.module.message.service.impl.MessageServiceImpl;
import com.xianguoji.server.module.message.vo.FeedbackVO;
import com.xianguoji.server.module.message.vo.MessageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-MESSAGE series: Message service unit tests
 */
@DisplayName("Message Service Unit Tests")
class MessageServiceTest extends AbstractServiceTest {

    @Mock
    private MessageMapper messageMapper;
    
    @Mock
    private FeedbackMapper feedbackMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Nested
    @DisplayName("getMessagePage")
    class GetMessagePageTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-001: Should return message page")
        void getMessagePage_shouldReturnPage_success() {
            // Arrange
            Long uid = 1L;
            Integer type = null;
            Message message = createMessage(1L, uid, 1, "Test Title", "Test Content");
            var page = new Page<Message>(1, 20);
            page.setRecords(List.of(message));
            page.setTotal(1);
            
            when(messageMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<MessageVO> result = messageService.getMessagePage(uid, type, 1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("TC-UT-MESSAGE-002: Should filter by type")
        void getMessagePage_shouldFilterByType_success() {
            // Arrange
            Long uid = 1L;
            Integer type = 1;
            var page = new Page<Message>(1, 20);
            page.setRecords(new ArrayList<>());
            
            when(messageMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<MessageVO> result = messageService.getMessagePage(uid, type, 1, 20);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getMessage")
    class GetMessageTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-003: Should return message")
        void getMessage_shouldReturnMessage_success() {
            // Arrange
            Long uid = 1L;
            Long id = 1L;
            Message message = createMessage(1L, uid, 1, "Test Title", "Test Content");
            
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(message);

            // Act
            MessageVO result = messageService.getMessage(uid, id);

            // Assert
            assertNotNull(result);
            assertEquals("Test Title", result.getTitle());
        }

        @Test
        @DisplayName("TC-UT-MESSAGE-004: Should throw exception when message not found")
        void getMessage_shouldThrowException_whenMessageNotFound() {
            // Arrange
            Long uid = 1L;
            Long id = 999L;
            when(messageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                messageService.getMessage(uid, id);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }
    }

    @Nested
    @DisplayName("markRead")
    class MarkReadTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-005: Should mark message as read")
        void markRead_shouldMarkRead_success() {
            // Arrange
            Long uid = 1L;
            Long id = 1L;
            when(messageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            // Act
            messageService.markRead(uid, id);

            // Assert
            verify(messageMapper).update(any(), any(LambdaUpdateWrapper.class));
        }
    }

    @Nested
    @DisplayName("markAllRead")
    class MarkAllReadTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-006: Should mark all messages as read")
        void markAllRead_shouldMarkAllRead_success() {
            // Arrange
            Long uid = 1L;
            Integer type = null;
            when(messageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            // Act
            messageService.markAllRead(uid, type);

            // Assert
            verify(messageMapper).update(any(), any(LambdaUpdateWrapper.class));
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCountTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-007: Should return unread count")
        void getUnreadCount_shouldReturnCount_success() {
            // Arrange
            Long uid = 1L;
            when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // Act
            int result = messageService.getUnreadCount(uid);

            // Assert
            assertEquals(5, result);
        }
    }

    @Nested
    @DisplayName("getUnreadCounts")
    class GetUnreadCountsTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-008: Should return unread counts by type")
        void getUnreadCounts_shouldReturnCounts_success() {
            // Arrange
            Long uid = 1L;
            when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L, 3L, 1L, 0L, 6L);

            // Act
            Map<String, Integer> result = messageService.getUnreadCounts(uid);

            // Assert
            assertNotNull(result);
            assertTrue(result.containsKey("system"));
            assertTrue(result.containsKey("promotion"));
            assertTrue(result.containsKey("logistics"));
            assertTrue(result.containsKey("chat"));
            assertTrue(result.containsKey("all"));
            assertEquals(6, result.get("all"));
        }
    }

    @Nested
    @DisplayName("submitFeedback")
    class SubmitFeedbackTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-009: Should submit feedback successfully")
        void submitFeedback_shouldSubmit_success() {
            // Arrange
            Long uid = 1L;
            FeedbackAddDto dto = new FeedbackAddDto();
            dto.setType("general");
            dto.setContent("Test feedback");
            dto.setContact("test@example.com");
            
            when(feedbackMapper.insert(any(Feedback.class))).thenReturn(1);

            // Act
            messageService.submitFeedback(uid, dto);

            // Assert
            verify(feedbackMapper).insert(any(Feedback.class));
        }
    }

    @Nested
    @DisplayName("getFeedbackPage")
    class GetFeedbackPageTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-010: Should return feedback page")
        void getFeedbackPage_shouldReturnPage_success() {
            // Arrange
            Feedback feedback = createFeedback(1L, 1L, 1, "Test feedback");
            var page = new Page<Feedback>(1, 20);
            page.setRecords(List.of(feedback));
            page.setTotal(1);
            
            when(feedbackMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);

            // Act
            PageVO<FeedbackVO> result = messageService.getFeedbackPage(1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }
    }

    @Nested
    @DisplayName("handleFeedback")
    class HandleFeedbackTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-011: Should throw exception when feedback not found")
        void handleFeedback_shouldThrowException_whenFeedbackNotFound() {
            // Arrange
            Long id = 999L;
            when(feedbackMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                messageService.handleFeedback(id);
            });
            assertEquals(ResultCode.NOT_FOUND, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-MESSAGE-012: Should handle feedback successfully")
        void handleFeedback_shouldHandle_success() {
            // Arrange
            Long id = 1L;
            Feedback feedback = createFeedback(1L, 1L, 0, "Test feedback");
            when(feedbackMapper.selectById(1L)).thenReturn(feedback);
            when(feedbackMapper.updateById(any(Feedback.class))).thenReturn(1);

            // Act
            messageService.handleFeedback(id);

            // Assert
            verify(feedbackMapper).updateById(any(Feedback.class));
        }
    }

    @Nested
    @DisplayName("broadcast")
    class BroadcastTests {

        @Test
        @DisplayName("TC-UT-MESSAGE-013: Should broadcast message successfully")
        void broadcast_shouldBroadcast_success() {
            // Arrange
            String title = "Test Title";
            String content = "Test Content";
            String linkUrl = "http://test.com";
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            // Act
            messageService.broadcast(title, content, linkUrl);

            // Assert
            verify(messageMapper).insert(any(Message.class));
        }
    }

    // Helper methods
    private Message createMessage(Long id, Long userId, Integer type, String title, String content) {
        Message message = new Message();
        message.setId(id);
        message.setUserId(userId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private Feedback createFeedback(Long id, Long userId, Integer status, String content) {
        Feedback feedback = new Feedback();
        feedback.setId(id);
        feedback.setUserId(userId);
        feedback.setType("general");
        feedback.setContent(content);
        feedback.setStatus(status);
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }
}


