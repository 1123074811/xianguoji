package com.xianguoji.server.module.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.AbstractServiceTest;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.chat.dto.ChatSendDto;
import com.xianguoji.server.module.chat.entity.ChatMessage;
import com.xianguoji.server.module.chat.mapper.ChatMessageMapper;
import com.xianguoji.server.module.chat.service.impl.ChatServiceImpl;
import com.xianguoji.server.module.chat.vo.ChatMessageVO;
import com.xianguoji.server.module.chat.vo.ChatUserVO;
import com.xianguoji.server.module.message.entity.Message;
import com.xianguoji.server.module.message.mapper.MessageMapper;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TC-UT-CHAT series: Chat service unit tests
 */
@DisplayName("Chat Service Unit Tests")
class ChatServiceTest extends AbstractServiceTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private ProductSkuMapper productSkuMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private StaffMapper staffMapper;
    
    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Nested
    @DisplayName("sendMessage")
    class SendMessageTests {

        @Test
        @DisplayName("TC-UT-CHAT-001: Should send text message successfully")
        void sendMessage_shouldSendText_success() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("text");
            dto.setContent("Hello");
            
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

            // Act
            ChatMessageVO result = chatService.sendMessage(uid, dto);

            // Assert
            assertNotNull(result);
            verify(chatMessageMapper).insert(any(ChatMessage.class));
        }

        @Test
        @DisplayName("TC-UT-CHAT-002: Should throw exception when text content is empty")
        void sendMessage_shouldThrowException_whenTextContentEmpty() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("text");
            dto.setContent("");

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                chatService.sendMessage(uid, dto);
            });
            assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
            assertTrue(exception.getMessage().contains("消息内容不能为空"));
        }

        @Test
        @DisplayName("TC-UT-CHAT-003: Should send product message successfully")
        void sendMessage_shouldSendProduct_success() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("product");
            dto.setProductId(1L);
            
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

            // Act
            ChatMessageVO result = chatService.sendMessage(uid, dto);

            // Assert
            assertNotNull(result);
            verify(chatMessageMapper).insert(any(ChatMessage.class));
        }

        @Test
        @DisplayName("TC-UT-CHAT-004: Should throw exception when product ID is null")
        void sendMessage_shouldThrowException_whenProductIdNull() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("product");
            dto.setProductId(null);

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                chatService.sendMessage(uid, dto);
            });
            assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-CHAT-005: Should send image message successfully")
        void sendMessage_shouldSendImage_success() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("image");
            dto.setImages(List.of("http://test.com/image.jpg"));
            
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);

            // Act
            ChatMessageVO result = chatService.sendMessage(uid, dto);

            // Assert
            assertNotNull(result);
            verify(chatMessageMapper).insert(any(ChatMessage.class));
        }

        @Test
        @DisplayName("TC-UT-CHAT-006: Should throw exception when images are empty")
        void sendMessage_shouldThrowException_whenImagesEmpty() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("image");
            dto.setImages(new ArrayList<>());

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                chatService.sendMessage(uid, dto);
            });
            assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
        }

        @Test
        @DisplayName("TC-UT-CHAT-007: Should throw exception for unsupported message type")
        void sendMessage_shouldThrowException_whenUnsupportedType() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("video");

            // Act & Assert
            BizException exception = assertThrows(BizException.class, () -> {
                chatService.sendMessage(uid, dto);
            });
            assertEquals(ResultCode.BIZ_ERROR, exception.getResultCode());
        }
    }

    @Nested
    @DisplayName("getMessagePage")
    class GetMessagePageTests {

        @Test
        @DisplayName("TC-UT-CHAT-008: Should return message page and mark as read")
        void getMessagePage_shouldReturnPage_success() {
            // Arrange
            Long uid = 1L;
            ChatMessage message = createChatMessage(1L, uid, 0, "text", "Hello");
            var page = new Page<ChatMessage>(1, 20);
            page.setRecords(List.of(message));
            page.setTotal(1);
            
            when(chatMessageMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(chatMessageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(uid)).thenReturn(createUser(uid, "Test User"));

            // Act
            PageVO<ChatMessageVO> result = chatService.getMessagePage(uid, 1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            verify(chatMessageMapper).update(any(), any(LambdaUpdateWrapper.class));
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    class GetUnreadCountTests {

        @Test
        @DisplayName("TC-UT-CHAT-009: Should return unread count")
        void getUnreadCount_shouldReturnCount_success() {
            // Arrange
            Long uid = 1L;
            when(chatMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // Act
            int result = chatService.getUnreadCount(uid);

            // Assert
            assertEquals(5, result);
        }
    }

    @Nested
    @DisplayName("markAllRead")
    class MarkAllReadTests {

        @Test
        @DisplayName("TC-UT-CHAT-010: Should mark all messages as read")
        void markAllRead_shouldMarkAllRead_success() {
            // Arrange
            Long uid = 1L;
            when(chatMessageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(messageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            // Act
            chatService.markAllRead(uid);

            // Assert
            verify(chatMessageMapper).update(any(), any(LambdaUpdateWrapper.class));
            verify(messageMapper).update(any(), any(LambdaUpdateWrapper.class));
        }
    }

    @Nested
    @DisplayName("replyMessage")
    class ReplyMessageTests {

        @Test
        @DisplayName("TC-UT-CHAT-011: Should reply with text message successfully")
        void replyMessage_shouldReplyText_success() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("text");
            dto.setContent("Reply");
            
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            // Act
            ChatMessageVO result = chatService.replyMessage(uid, dto);

            // Assert
            assertNotNull(result);
            verify(chatMessageMapper).insert(any(ChatMessage.class));
            verify(messageMapper).insert(any(Message.class));
        }

        @Test
        @DisplayName("TC-UT-CHAT-012: Should reply with image message successfully")
        void replyMessage_shouldReplyImage_success() {
            // Arrange
            Long uid = 1L;
            ChatSendDto dto = new ChatSendDto();
            dto.setMsgType("image");
            dto.setImages(List.of("http://test.com/image.jpg"));
            
            when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            // Act
            ChatMessageVO result = chatService.replyMessage(uid, dto);

            // Assert
            assertNotNull(result);
            verify(chatMessageMapper).insert(any(ChatMessage.class));
            verify(messageMapper).insert(any(Message.class));
        }
    }

    @Nested
    @DisplayName("getChatUserPage")
    class GetChatUserPageTests {

        @Test
        @DisplayName("TC-UT-CHAT-013: Should return chat user page")
        void getChatUserPage_shouldReturnPage_success() {
            // Arrange
            ChatMessage message = createChatMessage(1L, 1L, 0, "text", "Hello");
            User user = createUser(1L, "Test User");
            
            when(chatMessageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(message));
            when(userMapper.selectById(1L)).thenReturn(user);
            when(chatMessageMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(message);
            when(chatMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // Act
            PageVO<ChatUserVO> result = chatService.getChatUserPage(1, 20);

            // Assert
            assertNotNull(result);
            assertTrue(result.getTotal() > 0);
        }

        @Test
        @DisplayName("TC-UT-CHAT-014: Should return empty page when no users")
        void getChatUserPage_shouldReturnEmptyPage_whenNoUsers() {
            // Arrange
            when(chatMessageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // Act
            PageVO<ChatUserVO> result = chatService.getChatUserPage(1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getTotal());
        }
    }

    @Nested
    @DisplayName("getAdminMessagePage")
    class GetAdminMessagePageTests {

        @Test
        @DisplayName("TC-UT-CHAT-015: Should return admin message page and mark as read")
        void getAdminMessagePage_shouldReturnPage_success() {
            // Arrange
            Long userId = 1L;
            ChatMessage message = createChatMessage(1L, userId, 0, "text", "Hello");
            var page = new Page<ChatMessage>(1, 20);
            page.setRecords(List.of(message));
            page.setTotal(1);
            
            when(chatMessageMapper.selectPage(any(), any(LambdaQueryWrapper.class))).thenReturn(page);
            when(chatMessageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(userId)).thenReturn(createUser(userId, "Test User"));

            // Act
            PageVO<ChatMessageVO> result = chatService.getAdminMessagePage(userId, 1, 20);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            verify(chatMessageMapper).update(any(), any(LambdaUpdateWrapper.class));
        }
    }

    // Helper methods
    private ChatMessage createChatMessage(Long id, Long userId, Integer senderType, String msgType, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setUserId(userId);
        message.setSenderType(senderType);
        message.setMsgType(msgType);
        message.setContent(content);
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private User createUser(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar("http://test.com/avatar.jpg");
        return user;
    }
}

