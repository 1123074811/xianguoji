import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { AdminFeedbackVO, NotifySettingVO } from '@/api/types/message';

export const adminMessageApi = {
  feedbackPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminFeedbackVO>>({ url: '/api/admin/feedback/page', params }),

  feedbackReply: (id: number, content: string) =>
    request<void>({ url: `/api/admin/feedback/${id}/reply`, method: 'POST', data: { content } }),

  notifySettings: () =>
    request<NotifySettingVO[]>({ url: '/api/admin/notify-setting/list' }),

  updateNotifySetting: (id: number, enabled: boolean) =>
    request<void>({ url: `/api/admin/notify-setting/${id}`, method: 'PUT', data: { enabled } }),
};
