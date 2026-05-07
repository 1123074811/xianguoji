import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { ChatMessageVO, ChatSendDto } from '@/api/types/chat';

export const chatApi = {
  /** 发送消息 */
  send: (data: ChatSendDto) =>
    request<ChatMessageVO>({ url: '/api/u/chat/send', method: 'POST', data }),

  /** 聊天记录 */
  messages: (params?: { page?: number; size?: number }) =>
    request<PageVO<ChatMessageVO>>({ url: '/api/u/chat/messages', params }),

  /** 未读消息数 */
  unreadCount: () =>
    request<number>({ url: '/api/u/chat/unread-count', silent: true }),

  /** 全部已读 */
  readAll: () =>
    request<void>({ url: '/api/u/chat/read-all', method: 'PUT' }),
};
