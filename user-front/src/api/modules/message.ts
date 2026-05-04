import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { MessageVO, FeedbackSubmitDto } from '@/api/types/message';

export const messageApi = {
  page: (params?: { page?: number; size?: number; type?: number }) =>
    request<PageVO<MessageVO>>({ url: '/api/u/message/page', params }),

  markRead: (id: number) =>
    request<void>({ url: `/api/u/message/${id}/read`, method: 'PUT' }),

  markAllRead: () =>
    request<void>({ url: '/api/u/message/read-all', method: 'PUT' }),

  unreadCount: () =>
    request<number>({ url: '/api/u/message/unread-count', silent: true }),

  submitFeedback: (data: FeedbackSubmitDto) =>
    request<void>({ url: '/api/u/feedback', method: 'POST', data }),
};
