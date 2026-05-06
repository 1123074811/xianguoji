import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { AdminCouponVO, CouponCreateDto, AdminGroupBuyVO } from '@/api/types/promo';

export interface CouponStatsVO {
  activeCount: number;
  totalReceived: number;
  verifyRate: string;
  couponRevenue: number;
}

export interface GroupBuyStatsVO {
  activeActivityCount: number;
  totalActivity: number;
  instanceTotal: number;
  instanceOngoing: number;
  instanceSuccess: number;
  instanceFailed: number;
  totalJoinCount: number;
  totalSuccessGroups: number;
  successRate: string;
  revenue: string;
}

export interface AdminGroupBuyDto {
  id?: number;
  productId: number;
  skuId: number;
  groupPrice: string;
  groupSize: number;
  validHours: number;
  startTime: string;
  endTime: string;
  status?: number;
}

export const adminPromoApi = {
  couponStats: () =>
    request<CouponStatsVO>({ url: '/api/admin/coupon/stats' }),

  couponStatusCounts: () =>
    request<Record<string, number>>({ url: '/api/admin/coupon/status-counts' }),

  couponPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminCouponVO>>({ url: '/api/admin/coupon/list', params }),

  createCoupon: (data: CouponCreateDto) =>
    request<{ id: number }>({ url: '/api/admin/coupon', method: 'POST', data }),

  updateCoupon: (id: number, data: Partial<AdminCouponVO>) =>
    request<void>({ url: `/api/admin/coupon/${id}`, method: 'PUT', data }),

  deleteCoupon: (id: number) =>
    request<void>({ url: `/api/admin/coupon/${id}`, method: 'DELETE' }),

  groupBuyStats: () =>
    request<GroupBuyStatsVO>({ url: '/api/admin/group-buy/stats' }),

  groupBuyByProduct: (productId: number) =>
    request<AdminGroupBuyVO | null>({ url: `/api/admin/group-buy/by-product/${productId}` }),

  groupBuyPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminGroupBuyVO>>({ url: '/api/admin/group-buy/list', params }),

  createGroupBuy: (data: AdminGroupBuyDto) =>
    request<{ id: number }>({ url: '/api/admin/group-buy', method: 'POST', data }),

  updateGroupBuy: (id: number, data: Partial<AdminGroupBuyDto>) =>
    request<void>({ url: `/api/admin/group-buy/${id}`, method: 'PUT', data }),

  deleteGroupBuy: (id: number) =>
    request<void>({ url: `/api/admin/group-buy/${id}`, method: 'DELETE' }),
};
