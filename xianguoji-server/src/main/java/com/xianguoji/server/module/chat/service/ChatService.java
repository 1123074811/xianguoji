package com.xianguoji.server.module.chat.service;

import com.xianguoji.server.common.result.PageVO;
import com.xianguoji.server.module.chat.dto.ChatSendDto;
import com.xianguoji.server.module.chat.vo.ChatMessageVO;
import com.xianguoji.server.module.chat.vo.ChatUserVO;

public interface ChatService {

    /** 用户发送消息 */
    ChatMessageVO sendMessage(Long uid, ChatSendDto dto);

    /** 获取用户聊天记录（分页） */
    PageVO<ChatMessageVO> getMessagePage(Long uid, Integer page, Integer size);

    /** 获取用户未读消息数 */
    int getUnreadCount(Long uid);

    /** 标记用户消息全部已读 */
    void markAllRead(Long uid);

    /** 商家回复消息 */
    ChatMessageVO replyMessage(Long uid, ChatSendDto dto);

    /** 获取聊天用户列表（管理端） */
    PageVO<ChatUserVO> getChatUserPage(Integer page, Integer size);

    /** 管理端获取某用户聊天记录 */
    PageVO<ChatMessageVO> getAdminMessagePage(Long userId, Integer page, Integer size);
}
