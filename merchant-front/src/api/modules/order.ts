import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { AdminOrderVO, AdminOrderQry, ShipDto } from '@/api/types/order';

export const adminOrderApi = {
  page: (params: AdminOrderQry) =>
    request<PageVO<AdminOrderVO>>({ url: '/api/admin/order/page', params }),

  detail: (orderNo: string) =>
    request<AdminOrderVO>({ url: `/api/admin/order/${orderNo}` }),

  accept: (orderNo: string) =>
    request<void>({ url: `/api/admin/order/${orderNo}/accept`, method: 'POST' }),

  reject: (orderNo: string, reason: string) =>
    request<void>({ url: `/api/admin/order/${orderNo}/reject`, method: 'POST', data: { reason } }),

  ship: (orderNo: string, data: ShipDto) =>
    request<void>({ url: `/api/admin/order/${orderNo}/ship`, method: 'POST', data }),

  pickupVerify: (orderNo: string, pickupCode: string) =>
    request<void>({
      url: `/api/admin/order/${orderNo}/pickup-verify`,
      method: 'POST',
      data: { pickupCode },
    }),

  complete: (orderNo: string) =>
    request<void>({ url: `/api/admin/order/${orderNo}/complete`, method: 'POST' }),

  print: (orderNo: string) =>
    request<any>({ url: `/api/admin/order/${orderNo}/print`, method: 'POST' }),

  newCount: (since: number) =>
    request<number>({ url: '/api/admin/order/new-count', params: { since }, silent: true }),

  approveRefund: (refundNo: string) =>
    request<void>({ url: `/api/admin/refund/${refundNo}/approve`, method: 'POST' }),

  rejectRefund: (refundNo: string, reason: string) =>
    request<void>({ url: `/api/admin/refund/${refundNo}/reject`, method: 'POST', data: { reason } }),
};
