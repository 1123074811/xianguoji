package com.xianguoji.server.module.chat.controller;

import com.xianguoji.server.support.ResponseAssertions;

import com.xianguoji.server.AbstractApiTest;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.chat.dto.ChatSendDto;
import com.xianguoji.server.module.chat.service.ChatService;
import com.xianguoji.server.module.chat.vo.ChatMessageVO;
import com.xianguoji.server.module.chat.vo.ChatUserVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TC-API-CHAT series: Chat controller API tests
 */
@DisplayName("Chat Controller API Tests")
class ChatControllerTest extends AbstractApiTest {

    @MockBean
    private ChatService chatService;

    private String userToken = "test_user_token";
    private String adminToken = "test_admin_token";

    @Nested
    @DisplayName("POST /api/u/chat/send")
    class SendMessageTests {

        @Test
        @DisplayName("TC-API-CHAT-001: Should send message successfully")
        void sendMessage_shouldSend_success() throws Exception {
            // Arrange
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("text");
            dto.setContent("Hello");
            
            ChatMessageVO message = ChatMessageVO.builder()
                    .id(1L)
                    .senderType(0)
                    .msgType("text")
                    .content("Hello")
                    .isRead(0)
                    .build();
            
            when(chatService.sendMessage(anyLong(), any(ChatSendDto.class))).thenReturn(message);

            // Act
            MvcResult result = performPost("/api/u/chat/send", dto, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).sendMessage(anyLong(), any(ChatSendDto.class));
        }

        @Test
        @DisplayName("TC-API-CHAT-002: Should return 401 when not logged in")
        void sendMessage_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Arrange
            ChatSendDto dto = new ChatSendDto();

            // Act
            MvcResult result = performPost("/api/u/chat/send", dto)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/chat/messages")
    class MessagePageTests {

        @Test
        @DisplayName("TC-API-CHAT-003: Should return message page")
        void messagePage_shouldReturnPage_success() throws Exception {
            // Arrange
            List<ChatMessageVO> messages = new ArrayList<>();
            ChatMessageVO message = ChatMessageVO.builder()
                    .id(1L)
                    .senderType(0)
                    .msgType("text")
                    .content("Hello")
                    .isRead(0)
                    .build();
            messages.add(message);
            
            PageVO<ChatMessageVO> pageVO = new PageVO<>(1L, messages, 1, 20);
            when(chatService.getMessagePage(anyLong(), eq(1), eq(20))).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/u/chat/messages?page=1&size=20", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).getMessagePage(anyLong(), eq(1), eq(20));
        }

        @Test
        @DisplayName("TC-API-CHAT-004: Should return 401 when not logged in")
        void messagePage_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/chat/messages")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/u/chat/unread-count")
    class UnreadCountTests {

        @Test
        @DisplayName("TC-API-CHAT-005: Should return unread count")
        void unreadCount_shouldReturnCount_success() throws Exception {
            // Arrange
            when(chatService.getUnreadCount(anyLong())).thenReturn(5);

            // Act
            MvcResult result = performGet("/api/u/chat/unread-count", userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).getUnreadCount(anyLong());
        }

        @Test
        @DisplayName("TC-API-CHAT-006: Should return 401 when not logged in")
        void unreadCount_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performGet("/api/u/chat/unread-count")
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("PUT /api/u/chat/read-all")
    class MarkAllReadTests {

        @Test
        @DisplayName("TC-API-CHAT-007: Should mark all messages as read")
        void markAllRead_shouldMarkAllRead_success() throws Exception {
            // Arrange
            doNothing().when(chatService).markAllRead(anyLong());

            // Act
            MvcResult result = performPut("/api/u/chat/read-all", null, userToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).markAllRead(anyLong());
        }

        @Test
        @DisplayName("TC-API-CHAT-008: Should return 401 when not logged in")
        void markAllRead_shouldReturn401_whenNotLoggedIn() throws Exception {
            // Act
            MvcResult result = performPut("/api/u/chat/read-all", null)
                    .andReturn();

            // Assert
            assertEquals(401, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/chat/users")
    class ChatUserPageTests {

        @Test
        @DisplayName("TC-API-CHAT-009: Should return chat user page")
        void chatUserPage_shouldReturnPage_success() throws Exception {
            // Arrange
            List<ChatUserVO> users = new ArrayList<>();
            ChatUserVO user = ChatUserVO.builder()
                    .userId(1L)
                    .nickname("Test User")
                    .avatar("http://test.com/avatar.jpg")
                    .lastMessage("Hello")
                    .unreadCount(0)
                    .build();
            users.add(user);
            
            PageVO<ChatUserVO> pageVO = new PageVO<>(1L, users, 1, 20);
            when(chatService.getChatUserPage(1, 20)).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/admin/chat/users?page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).getChatUserPage(1, 20);
        }

        @Test
        @DisplayName("TC-API-CHAT-010: Should return 403 when not admin")
        void chatUserPage_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/chat/users", userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/chat/messages")
    class AdminMessagePageTests {

        @Test
        @DisplayName("TC-API-CHAT-011: Should return admin message page")
        void adminMessagePage_shouldReturnPage_success() throws Exception {
            // Arrange
            Long userId = 1L;
            List<ChatMessageVO> messages = new ArrayList<>();
            ChatMessageVO message = ChatMessageVO.builder()
                    .id(1L)
                    .senderType(0)
                    .msgType("text")
                    .content("Hello")
                    .isRead(0)
                    .build();
            messages.add(message);
            
            PageVO<ChatMessageVO> pageVO = new PageVO<>(1L, messages, 1, 20);
            when(chatService.getAdminMessagePage(userId, 1, 20)).thenReturn(pageVO);

            // Act
            MvcResult result = performGet("/api/admin/chat/messages?userId=" + userId + "&page=1&size=20", adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).getAdminMessagePage(userId, 1, 20);
        }

        @Test
        @DisplayName("TC-API-CHAT-012: Should return 403 when not admin")
        void adminMessagePage_shouldReturn403_whenNotAdmin() throws Exception {
            // Act
            MvcResult result = performGet("/api/admin/chat/messages?userId=1", userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/chat/reply")
    class ReplyMessageTests {

        @Test
        @DisplayName("TC-API-CHAT-013: Should reply message successfully")
        void replyMessage_shouldReply_success() throws Exception {
            // Arrange
            Long userId = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("text");
            dto.setContent("Reply");
            
            ChatMessageVO message = ChatMessageVO.builder()
                    .id(1L)
                    .senderType(1)
                    .msgType("text")
                    .content("Reply")
                    .isRead(0)
                    .build();
            
            when(chatService.replyMessage(eq(userId), any(ChatSendDto.class))).thenReturn(message);

            // Act
            MvcResult result = performPost("/api/admin/chat/reply?userId=" + userId, dto, adminToken)
                    .andExpect(status().isOk())
                    .andReturn();

            // Assert
            String response = getResponseBody(result);
            ResponseAssertions.assertSuccess(response);
            verify(chatService).replyMessage(eq(userId), any(ChatSendDto.class));
        }

        @Test
        @DisplayName("TC-API-CHAT-014: Should return 403 when not admin")
        void replyMessage_shouldReturn403_whenNotAdmin() throws Exception {
            // Arrange
            Long userId = 1L;
            ChatSendDto dto = new ChatSendDto();

            // Act
            MvcResult result = performPost("/api/admin/chat/reply?userId=" + userId, dto, userToken)
                    .andReturn();

            // Assert
            assertEquals(403, result.getResponse().getStatus());
        }
    }
}

