import { request } from '@/api/request';

export interface HelpFaqVO {
  id: number;
  section: string;
  question: string;
  answer: string;
  sort: number;
  status: number;
  createdAt: string;
}

export interface HelpGuideVO {
  id: number;
  title: string;
  icon: string;
  duration: string;
  url: string;
  sort: number;
}

export const helpApi = {
  faqList: (params: { section?: string; keyword?: string } = {}) =>
    request<HelpFaqVO[]>({ url: '/api/admin/help/faq/list', params }),

  guideList: () =>
    request<HelpGuideVO[]>({ url: '/api/admin/help/guide/list' }),

  submitFeedback: (data: { content: string; contact?: string }) =>
    request<void>({ url: '/api/admin/help/feedback', method: 'POST', data }),
};
