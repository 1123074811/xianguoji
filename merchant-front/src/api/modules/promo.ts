import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { AdminCouponVO, CouponCreateDto, AdminGroupBuyVO } from '@/api/types/promo';

export const adminPromoApi = {
  couponPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminCouponVO>>({ url: '/api/admin/promo/coupon/page', params }),

  createCoupon: (data: CouponCreateDto) =>
    request<{ id: number }>({ url: '/api/admin/promo/coupon', method: 'POST', data }),

  updateCoupon: (id: number, data: Partial<CouponCreateDto>) =>
    request<void>({ url: `/api/admin/promo/coupon/${id}`, method: 'PUT', data }),

  deleteCoupon: (id: number) =>
    request<void>({ url: `/api/admin/promo/coupon/${id}`, method: 'DELETE' }),

  groupBuyPage: (params: { page?: number; size?: number; status?: number }) =>
    request<PageVO<AdminGroupBuyVO>>({ url: '/api/admin/promo/group-buy/page', params }),

  createGroupBuy: (data: any) =>
    request<{ id: number }>({ url: '/api/admin/promo/group-buy', method: 'POST', data }),

  updateGroupBuy: (id: number, data: any) =>
    request<void>({ url: `/api/admin/promo/group-buy/${id}`, method: 'PUT', data }),

  deleteGroupBuy: (id: number) =>
    request<void>({ url: `/api/admin/promo/group-buy/${id}`, method: 'DELETE' }),
};
