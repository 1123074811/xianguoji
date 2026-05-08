import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { ReviewVO, ReviewSummaryVO, ReviewSubmitDto } from '@/api/types/review';

export const reviewApi = {
  productReviews: (productId: number, params?: { filter?: string; page?: number; size?: number }) =>
    request<PageVO<ReviewVO>>({
      url: `/api/pub/review/product/${productId}`,
      params,
      anonymous: true,
    }),

  summary: (productId: number) =>
    request<ReviewSummaryVO>({
      url: `/api/pub/review/product/${productId}/summary`,
      anonymous: true,
    }),

  /** 后端实际为 pending：待评价订单项列表 */
  pendingReviews: () =>
    request<any[]>({ url: '/api/u/review/pending' }),

  /** 我的评价列表 */
  myReviews: (params?: { page?: number; size?: number }) =>
    request<PageVO<ReviewVO>>({ url: '/api/u/review/my', params }),

  submit: (data: ReviewSubmitDto) =>
    request<void>({ url: '/api/u/review', method: 'POST', data }),
};
