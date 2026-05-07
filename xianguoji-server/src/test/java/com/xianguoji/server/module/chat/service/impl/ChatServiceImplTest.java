package com.xianguoji.server.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.module.chat.dto.ChatSendDto;
import com.xianguoji.server.module.chat.entity.ChatMessage;
import com.xianguoji.server.module.chat.mapper.ChatMessageMapper;
import com.xianguoji.server.module.catalog.entity.Product;
import com.xianguoji.server.module.catalog.entity.ProductSku;
import com.xianguoji.server.module.catalog.mapper.ProductMapper;
import com.xianguoji.server.module.catalog.mapper.ProductSkuMapper;
import com.xianguoji.server.module.message.entity.Message;
import com.xianguoji.server.module.message.mapper.MessageMapper;
import com.xianguoji.server.module.staff.entity.Staff;
import com.xianguoji.server.module.staff.mapper.StaffMapper;
import com.xianguoji.server.module.user.entity.User;
import com.xianguoji.server.module.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * §2.7 Chat 单元测试
 *
 * TC-UT-CHAT-001: 发送文本消息
 * TC-UT-CHAT-002: 发送商品咨询消息
 * TC-UT-CHAT-003: 发送图片消息
 * TC-UT-CHAT-004: 空文本消息拒绝
 * TC-UT-CHAT-005: 不支持的消息类型
 * TC-UT-CHAT-006: 获取未读消息数
 * TC-UT-CHAT-007: 标记全部已读
 * TC-UT-CHAT-008: 商家回复消息
 * TC-UT-CHAT-009: 商品咨询消息含productCard
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

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

    /**
     * TC-UT-CHAT-001: 发送文本消息
     */
    @Test
    void sendMessage_shouldInsertTextMessage_whenContentValid() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("你好，请问有货吗？");

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(userMapper.selectById(anyLong())).thenReturn(createUser(1L, "测试用户"));

        // Act
        var result = chatService.sendMessage(1L, dto);

        // Assert
        assertNotNull(result);
        verify(chatMessageMapper).insert(argThat(msg ->
                msg.getUserId().equals(1L) &&
                msg.getSenderType() == 0 &&
                "text".equals(msg.getMsgType()) &&
                "你好，请问有货吗？".equals(msg.getContent())
        ));
    }

    /**
     * TC-UT-CHAT-002: 发送商品咨询消息
     */
    @Test
    void sendMessage_shouldInsertProductMessage_whenProductIdProvided() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("product");
        dto.setProductId(100L);

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(userMapper.selectById(anyLong())).thenReturn(createUser(1L, "测试用户"));
        when(productMapper.selectById(100L)).thenReturn(createProduct(100L, "红富士苹果"));

        // Act
        var result = chatService.sendMessage(1L, dto);

        // Assert
        assertNotNull(result);
        verify(chatMessageMapper).insert(argThat(msg ->
                "product".equals(msg.getMsgType()) &&
                msg.getProductId().equals(100L) &&
                "商品咨询".equals(msg.getContent())
        ));
    }

    /**
     * TC-UT-CHAT-003: 发送图片消息
     */
    @Test
    void sendMessage_shouldInsertImageMessage_whenImagesProvided() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("image");
        dto.setImages(List.of("https://img.test.com/1.jpg"));

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(userMapper.selectById(anyLong())).thenReturn(createUser(1L, "测试用户"));

        // Act
        var result = chatService.sendMessage(1L, dto);

        // Assert
        assertNotNull(result);
        verify(chatMessageMapper).insert(argThat(msg ->
                "image".equals(msg.getMsgType()) &&
                "[图片]".equals(msg.getContent())
        ));
    }

    /**
     * TC-UT-CHAT-004: 空文本消息拒绝
     */
    @Test
    void sendMessage_shouldReject_whenTextContentIsBlank() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("");

        // Act & Assert
        assertThrows(BizException.class, () -> chatService.sendMessage(1L, dto));
    }

    /**
     * TC-UT-CHAT-005: 不支持的消息类型
     */
    @Test
    void sendMessage_shouldReject_whenUnsupportedMsgType() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("video");

        // Act & Assert
        assertThrows(BizException.class, () -> chatService.sendMessage(1L, dto));
    }

    /**
     * TC-UT-CHAT-006: 获取未读消息数
     */
    @Test
    void getUnreadCount_shouldReturnCorrectCount() {
        // Arrange
        when(chatMessageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // Act
        int count = chatService.getUnreadCount(1L);

        // Assert
        assertEquals(5, count);
    }

    /**
     * TC-UT-CHAT-007: 标记全部已读
     */
    @Test
    void markAllRead_shouldUpdateBothTables() {
        // Arrange
        when(chatMessageMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(3);
        when(messageMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(2);

        // Act
        chatService.markAllRead(1L);

        // Assert - chat_message 和 message 表都应被更新
        verify(chatMessageMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        verify(messageMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    /**
     * TC-UT-CHAT-008: 商家回复消息
     */
    @Test
    void replyMessage_shouldInsertAsStaffAndNotify() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("好的，有货的");

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(staffMapper.selectById(anyLong())).thenReturn(createStaff(1L, "客服小王"));

        // Act
        var result = chatService.replyMessage(100L, dto);

        // Assert
        assertNotNull(result);
        verify(chatMessageMapper).insert(argThat(msg ->
                msg.getSenderType() == 1 &&  // 商家发送
                msg.getUserId().equals(100L)
        ));
        verify(messageMapper).insert(argThat(m ->
                m.getType() == 6 &&  // 客服消息类型
                m.getUserId().equals(100L)
        ));
    }

    /**
     * TC-UT-CHAT-009: 商品咨询消息含productCard
     */
    @Test
    void sendMessage_shouldIncludeProductCard_whenProductMessage() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("product");
        dto.setProductId(100L);

        Product product = createProduct(100L, "红富士苹果");
        ProductSku defaultSku = new ProductSku();
        defaultSku.setId(1L);
        defaultSku.setProductId(100L);
        defaultSku.setIsDefault(1);
        defaultSku.setPrice(java.math.BigDecimal.valueOf(9.90));
        defaultSku.setSpecName("500g/份");

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(userMapper.selectById(anyLong())).thenReturn(createUser(1L, "测试用户"));
        when(productMapper.selectById(100L)).thenReturn(product);
        when(productSkuMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(defaultSku);

        // Act
        var result = chatService.sendMessage(1L, dto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductCard());
        assertEquals("红富士苹果", result.getProductCard().getName());
        assertEquals("9.9", result.getProductCard().getPrice());
        assertEquals("500g/份", result.getProductCard().getSpecName());
    }

    /**
     * TC-API-CHAT-007: senderType=0 时 senderAvatar = 用户 avatar URL
     */
    @Test
    void sendMessage_shouldSetSenderAvatar_toUserAvatar_whenSenderTypeIsUser() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("你好");

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(createUser(1L, "测试用户"));

        // Act
        var result = chatService.sendMessage(1L, dto);

        // Assert - senderType=0, senderAvatar 应为用户头像
        assertNotNull(result);
        assertEquals(0, result.getSenderType());
        assertEquals("https://img.test.com/avatar.jpg", result.getSenderAvatar());
    }

    /**
     * TC-API-CHAT-008: senderType=0 用户无头像时 senderAvatar = null
     */
    @Test
    void sendMessage_shouldSetSenderAvatar_toNull_whenUserHasNoAvatar() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("你好");

        User userNoAvatar = new User();
        userNoAvatar.setId(1L);
        userNoAvatar.setNickname("无头像用户");
        userNoAvatar.setAvatar(null);

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(userNoAvatar);

        // Act
        var result = chatService.sendMessage(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getSenderType());
        assertNull(result.getSenderAvatar());
    }

    /**
     * TC-API-CHAT-009: senderType=1 时 senderAvatar = Staff avatar URL（通过 createBy 查 staff）
     */
    @Test
    void replyMessage_shouldSetSenderAvatar_toStaffAvatar_whenSenderTypeIsStaff() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("好的，有货的");

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(staffMapper.selectById(1L)).thenReturn(createStaff(1L, "客服小王"));

        // Act
        var result = chatService.replyMessage(100L, dto);

        // Assert - senderType=1, senderAvatar 应为 Staff 头像
        assertNotNull(result);
        assertEquals(1, result.getSenderType());
        assertEquals("https://img.test.com/staff.jpg", result.getSenderAvatar());
    }

    /**
     * TC-API-CHAT-010: senderType=1 Staff 无头像时 senderAvatar = null
     */
    @Test
    void replyMessage_shouldSetSenderAvatar_toNull_whenStaffHasNoAvatar() {
        // Arrange
        ChatSendDto dto = new ChatSendDto();
        dto.setMsgType("text");
        dto.setContent("好的");

        Staff staffNoAvatar = new Staff();
        staffNoAvatar.setId(1L);
        staffNoAvatar.setName("无头像客服");
        staffNoAvatar.setAvatar(null);

        when(chatMessageMapper.insert(any(ChatMessage.class))).thenReturn(1);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(staffMapper.selectById(1L)).thenReturn(staffNoAvatar);

        // Act
        var result = chatService.replyMessage(100L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSenderType());
        assertNull(result.getSenderAvatar());
    }

    // Helper methods
    private User createUser(Long id, String nickname) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar("https://img.test.com/avatar.jpg");
        return user;
    }

    private Product createProduct(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setMainImage("https://img.test.com/apple.jpg");
        product.setMinPrice(java.math.BigDecimal.valueOf(9.90));
        return product;
    }

    private Staff createStaff(Long id, String name) {
        Staff staff = new Staff();
        staff.setId(id);
        staff.setName(name);
        staff.setAvatar("https://img.test.com/staff.jpg");
        return staff;
    }
}
