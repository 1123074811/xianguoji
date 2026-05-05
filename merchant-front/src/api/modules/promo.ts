import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { AdminCouponVO, CouponCreateDto, AdminGroupBuyVO } from '@/api/types/promo';

export interface CouponStatsVO {
  activeCount: number;
  totalReceived: number;
  verifyRate: string;
  couponRevenue: number;
}

export const adminPromoApi = {
  couponStats: () =>
    request<CouponStatsVO>({ url: '/api/admin/coupon/stats' }),

  couponPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminCouponVO>>({ url: '/api/admin/coupon/list', params }),

  createCoupon: (data: CouponCreateDto) =>
    request<{ id: number }>({ url: '/api/admin/coupon', method: 'POST', data }),

  updateCoupon: (id: number, data: Partial<CouponCreateDto>) =>
    request<void>({ url: `/api/admin/coupon/${id}`, method: 'PUT', data }),

  deleteCoupon: (id: number) =>
    request<void>({ url: `/api/admin/coupon/${id}`, method: 'DELETE' }),

  groupBuyPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminGroupBuyVO>>({ url: '/api/admin/group-buy/list', params }),

  createGroupBuy: (data: any) =>
    request<{ id: number }>({ url: '/api/admin/group-buy', method: 'POST', data }),

  updateGroupBuy: (id: number, data: any) =>
    request<void>({ url: `/api/admin/group-buy/${id}`, method: 'PUT', data }),

  deleteGroupBuy: (id: number) =>
    request<void>({ url: `/api/admin/group-buy/${id}`, method: 'DELETE' }),
};
