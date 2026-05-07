import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { ChatMessageVO, ChatUserVO, ChatSendDto } from '@/api/types/chat';

export const adminChatApi = {
  /** 聊天用户列表 */
  users: (params?: { page?: number; size?: number }) =>
    request<PageVO<ChatUserVO>>({ url: '/api/admin/chat/users', params }),

  /** 某用户聊天记录 */
  messages: (userId: number, params?: { page?: number; size?: number }) =>
    request<PageVO<ChatMessageVO>>({ url: '/api/admin/chat/messages', params: { userId, ...params } }),

  /** 回复消息 */
  reply: (userId: number, data: ChatSendDto) =>
    request<ChatMessageVO>({ url: '/api/admin/chat/reply', method: 'POST', params: { userId }, data }),
};
