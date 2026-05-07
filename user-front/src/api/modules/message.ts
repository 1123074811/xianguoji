import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { MessageVO, MessageUnreadCounts, FeedbackSubmitDto } from '@/api/types/message';

export const messageApi = {
  page: (params?: { page?: number; size?: number; type?: number }) =>
    request<PageVO<MessageVO>>({ url: '/api/u/message/page', params }),

  detail: (id: number) =>
    request<MessageVO>({ url: `/api/u/message/${id}` }),

  markRead: (id: number) =>
    request<void>({ url: `/api/u/message/${id}/read`, method: 'PUT' }),

  markAllRead: (type?: number) =>
    request<void>({ url: '/api/u/message/read-all', method: 'PUT', params: type ? { type } : undefined }),

  unreadCount: () =>
    request<number>({ url: '/api/u/message/unread-count', silent: true }),

  unreadCounts: () =>
    request<MessageUnreadCounts>({ url: '/api/u/message/unread-counts', silent: true }),

  submitFeedback: (data: FeedbackSubmitDto) =>
    request<void>({ url: '/api/u/feedback', method: 'POST', data }),
};
