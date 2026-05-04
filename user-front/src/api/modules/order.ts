import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type {
  OrderPreviewVO, OrderSubmitDto, OrderVO, RefundVO, RefundSubmitDto,
} from '@/api/types/order';

export const orderApi = {
  preview: (data: { cartItemIds?: number[]; groupBuyActivityId?: number }) =>
    request<OrderPreviewVO>({ url: '/api/u/order/preview', method: 'POST', data }),

  submit: (data: OrderSubmitDto) =>
    request<{ orderNo: string }>({ url: '/api/u/order/submit', method: 'POST', data }),

  pay: (orderNo: string, data: { payMethod: string }) =>
    request<any>({ url: `/api/u/order/${orderNo}/pay`, method: 'POST', data }),

  page: (params: { page?: number; size?: number; tab?: string }) =>
    request<PageVO<OrderVO>>({ url: '/api/u/order/page', params }),

  detail: (orderNo: string) =>
    request<OrderVO>({ url: `/api/u/order/${orderNo}` }),

  cancel: (orderNo: string) =>
    request<void>({ url: `/api/u/order/${orderNo}/cancel`, method: 'POST' }),

  confirm: (orderNo: string) =>
    request<void>({ url: `/api/u/order/${orderNo}/confirm`, method: 'POST' }),

  remind: (orderNo: string) =>
    request<void>({ url: `/api/u/order/${orderNo}/remind`, method: 'POST' }),

  repurchase: (orderNo: string) =>
    request<void>({ url: `/api/u/order/${orderNo}/repurchase`, method: 'POST' }),

  submitRefund: (data: RefundSubmitDto) =>
    request<RefundVO>({ url: '/api/u/refund', method: 'POST', data }),

  refundDetail: (refundNo: string) =>
    request<RefundVO>({ url: `/api/u/refund/${refundNo}` }),
};
