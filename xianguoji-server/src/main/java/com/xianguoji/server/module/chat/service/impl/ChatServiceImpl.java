package com.xianguoji.server.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianguoji.server.common.exception.BizException;
import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.common.result.ResultCode;
import com.xianguoji.server.module.chat.dto.ChatSendDto;
import com.xianguoji.server.module.chat.entity.ChatMessage;
import com.xianguoji.server.module.chat.mapper.ChatMessageMapper;
import com.xianguoji.server.module.chat.service.ChatService;
import com.xianguoji.server.module.chat.vo.ChatMessageVO;
import com.xianguoji.server.module.chat.vo.ChatUserVO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserMapper userMapper;
    private final StaffMapper staffMapper;
    private final MessageMapper messageMapper;

    @Override
    public ChatMessageVO sendMessage(Long uid, ChatSendDto dto) {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(uid);
        msg.setSenderType(0);
        msg.setMsgType(dto.getMsgType());
        msg.setIsRead(0);

        switch (dto.getMsgType()) {
            case "text" -> {
                if (dto.getContent() == null || dto.getContent().isBlank()) {
                    throw new BizException(ResultCode.BIZ_ERROR, "消息内容不能为空");
                }
                msg.setContent(dto.getContent());
            }
            case "product" -> {
                if (dto.getProductId() == null) {
                    throw new BizException(ResultCode.BIZ_ERROR, "商品ID不能为空");
                }
                msg.setProductId(dto.getProductId());
                msg.setContent("商品咨询");
            }
            case "image" -> {
                if (dto.getImages() == null || dto.getImages().isEmpty()) {
                    throw new BizException(ResultCode.BIZ_ERROR, "图片不能为空");
                }
                msg.setImages(dto.getImages());
                msg.setContent("[图片]");
            }
            default -> throw new BizException(ResultCode.BIZ_ERROR, "不支持的消息类型");
        }

        chatMessageMapper.insert(msg);
        return toVO(msg);
    }

    @Override
    public PageVO<ChatMessageVO> getMessagePage(Long uid, Integer page, Integer size) {
        // 先标记商家消息为已读
        markAllRead(uid);

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getUserId, uid)
                .orderByAsc(ChatMessage::getCreatedAt);

        Page<ChatMessage> p = chatMessageMapper.selectPage(new Page<>(page, size), wrapper);
        List<ChatMessageVO> voList = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toCollection(ArrayList::new));
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    @Override
    public int getUnreadCount(Long uid) {
        return Math.toIntExact(chatMessageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getUserId, uid)
                        .eq(ChatMessage::getSenderType, 1)
                        .eq(ChatMessage::getIsRead, 0)));
    }

    @Override
    public void markAllRead(Long uid) {
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getUserId, uid)
                .eq(ChatMessage::getSenderType, 1)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getUserId, uid)
                .eq(Message::getType, 6)
                .eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1));
    }

    @Override
    public ChatMessageVO replyMessage(Long uid, ChatSendDto dto) {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(uid);
        msg.setSenderType(1);
        msg.setMsgType(dto.getMsgType() != null ? dto.getMsgType() : "text");
        msg.setIsRead(0);

        if ("text".equals(msg.getMsgType())) {
            msg.setContent(dto.getContent());
        } else if ("image".equals(msg.getMsgType())) {
            msg.setImages(dto.getImages());
            msg.setContent("[图片]");
        }

        chatMessageMapper.insert(msg);

        // 同步写入 message 表，让消息中心也能收到客服回复
        Message notification = new Message();
        notification.setUserId(uid);
        notification.setType(6); // type=6 客服消息
        notification.setTitle("客服回复");
        notification.setContent(msg.getContent());
        notification.setLinkUrl("/pagesC/chat/index");
        notification.setIsRead(0);
        messageMapper.insert(notification);

        return toVO(msg);
    }

    @Override
    public PageVO<ChatUserVO> getChatUserPage(Integer page, Integer size) {
        // 查询有聊天记录的用户ID（按最后消息时间倒序）
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .select(ChatMessage::getUserId)
                .groupBy(ChatMessage::getUserId)
                .orderByDesc(ChatMessage::getCreatedAt);

        // 先拿到所有有聊天记录的userId
        List<ChatMessage> allMsg = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .select(ChatMessage::getUserId, ChatMessage::getCreatedAt)
                        .groupBy(ChatMessage::getUserId, ChatMessage::getCreatedAt)
                        .orderByDesc(ChatMessage::getCreatedAt));

        // 按userId分组取最新时间
        Map<Long, java.time.LocalDateTime> lastTimeMap = new LinkedHashMap<>();
        for (ChatMessage m : allMsg) {
            if (!lastTimeMap.containsKey(m.getUserId())) {
                lastTimeMap.put(m.getUserId(), m.getCreatedAt());
            }
        }

        List<Long> userIds = new ArrayList<>(lastTimeMap.keySet());
        int total = userIds.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        if (from >= total) {
            return new PageVO<>(total, new ArrayList<>(), page, size);
        }
        List<Long> pagedIds = userIds.subList(from, to);

        List<ChatUserVO> voList = new ArrayList<>();
        for (Long userId : pagedIds) {
            User user = userMapper.selectById(userId);
            if (user == null) continue;

            // 最后一条消息
            ChatMessage lastMsg = chatMessageMapper.selectOne(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getUserId, userId)
                            .orderByDesc(ChatMessage::getCreatedAt)
                            .last("LIMIT 1"));

            // 未读数（商家视角：用户发来的未读消息）
            int unread = Math.toIntExact(chatMessageMapper.selectCount(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getUserId, userId)
                            .eq(ChatMessage::getSenderType, 0)
                            .eq(ChatMessage::getIsRead, 0)));

            voList.add(ChatUserVO.builder()
                    .userId(userId)
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                    .lastTime(lastTimeMap.get(userId))
                    .unreadCount(unread)
                    .build());
        }

        return new PageVO<>(total, voList, page, size);
    }

    @Override
    public PageVO<ChatMessageVO> getAdminMessagePage(Long userId, Integer page, Integer size) {
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getSenderType, 0)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getCreatedAt);

        Page<ChatMessage> p = chatMessageMapper.selectPage(new Page<>(page, size), wrapper);
        List<ChatMessageVO> voList = p.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toCollection(ArrayList::new));
        return new PageVO<>(p.getTotal(), voList, page, size);
    }

    private ChatMessageVO toVO(ChatMessage msg) {
        // 根据senderType查头像
        String senderAvatar = null;
        if (msg.getSenderType() == 0) {
            User user = userMapper.selectById(msg.getUserId());
            senderAvatar = user != null ? user.getAvatar() : null;
        } else {
            Staff staff = staffMapper.selectById(msg.getCreateBy());
            senderAvatar = staff != null ? staff.getAvatar() : null;
        }

        ChatMessageVO.ChatMessageVOBuilder builder = ChatMessageVO.builder()
                .id(msg.getId())
                .senderType(msg.getSenderType())
                .msgType(msg.getMsgType())
                .content(msg.getContent())
                .images(msg.getImages())
                .isRead(msg.getIsRead())
                .senderAvatar(senderAvatar)
                .createdAt(msg.getCreatedAt());

        if ("product".equals(msg.getMsgType()) && msg.getProductId() != null) {
            Product product = productMapper.selectById(msg.getProductId());
            if (product != null) {
                // 取默认SKU
                ProductSku defaultSku = productSkuMapper.selectOne(
                        new LambdaQueryWrapper<ProductSku>()
                                .eq(ProductSku::getProductId, product.getId())
                                .eq(ProductSku::getIsDefault, 1)
                                .last("LIMIT 1"));
                String price = defaultSku != null ? defaultSku.getPrice().toPlainString() : product.getMinPrice().toPlainString();
                String specName = defaultSku != null ? defaultSku.getSpecName() : "";

                builder.productCard(ChatMessageVO.ProductCardVO.builder()
                        .productId(product.getId())
                        .name(product.getName())
                        .mainImage(product.getMainImage())
                        .price(price)
                        .specName(specName)
                        .build());
            }
        }

        return builder.build();
    }
}
