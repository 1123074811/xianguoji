import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { AdminReviewVO } from '@/api/types/review';

export const adminReviewApi = {
  /** filter: 'all'|'pending_reply'|'with_image'|'low_rating'，对齐后端 reviewService.adminReviewPage */
  page: (params: { page?: number; size?: number; filter?: string }) =>
    request<PageVO<AdminReviewVO>>({ url: '/api/admin/review/page', params }),

  /** 后端 ReviewController.reply 读取 body.get("reply") */
  reply: (id: number, replyText: string) =>
    request<void>({ url: `/api/admin/review/${id}/reply`, method: 'POST', data: { reply: replyText } }),

  toggleHidden: (id: number, hidden: 0 | 1) =>
    request<void>({ url: `/api/admin/review/${id}/hidden`, method: 'PUT', data: { hidden } }),
};
