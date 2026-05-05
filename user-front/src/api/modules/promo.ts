import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { CouponVO, UserCouponVO, GroupBuyActivityVO, GroupBuyInstanceVO } from '@/api/types/promo';

export const promoApi = {
  couponList: () =>
    request<CouponVO[]>({ url: '/api/pub/coupon/list', anonymous: true }),

  claimCoupon: (couponId: number) =>
    request<void>({ url: `/api/u/coupon/${couponId}/receive`, method: 'POST' }),

  myCoupons: (params?: { status?: number }) =>
    request<UserCouponVO[]>({ url: '/api/u/coupon/list', params }),

  usableCoupons: (data: { totalAmount: string; productIds?: number[] }) =>
    request<UserCouponVO[]>({ url: '/api/u/coupon/usable', method: 'POST', data }),

  groupBuyPage: (params?: { page?: number; size?: number }) =>
    request<PageVO<GroupBuyActivityVO>>({ url: '/api/pub/group-buy/page', params, anonymous: true }),

  groupBuyDetail: (instanceId: number) =>
    request<GroupBuyInstanceVO>({ url: `/api/u/group-buy/${instanceId}` }),

  launchGroupBuy: (data: {
    activityId: number;
    addressId?: number;
    pickupPointId?: number;
    deliveryType?: number;
    deliveryTime?: string;
    userRemark?: string;
    payMethod?: string;
  }) =>
    request<{ instanceId: number }>({ url: '/api/u/group-buy/launch', method: 'POST', data }),

  joinGroupBuy: (instanceId: number, data: {
    addressId?: number;
    pickupPointId?: number;
    deliveryType?: number;
    deliveryTime?: string;
    userRemark?: string;
    payMethod?: string;
  }) =>
    request<void>({ url: `/api/u/group-buy/${instanceId}/join`, method: 'POST', data }),
};
