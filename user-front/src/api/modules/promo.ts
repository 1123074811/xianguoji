import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { CouponVO, UserCouponVO, GroupBuyActivityVO, GroupBuyInstanceVO } from '@/api/types/promo';

export interface GroupBuyOrderDto {
  addressId?: number;
  pickupPointId?: number;
  deliveryType?: number;
  deliveryTime?: string;
  userRemark?: string;
  payMethod?: string;
}

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

  groupBuyByProduct: (productId: number) =>
    request<GroupBuyActivityVO | null>({ url: `/api/pub/group-buy/by-product/${productId}`, anonymous: true }),

  groupBuyByShareCode: (shareCode: string) =>
    request<GroupBuyInstanceVO>({ url: `/api/pub/group-buy/share/${shareCode}`, anonymous: true }),

  groupBuyDetail: (instanceId: number) =>
    request<GroupBuyInstanceVO>({ url: `/api/u/group-buy/${instanceId}` }),

  groupBuyInstanceDetail: (instanceId: number) =>
    request<GroupBuyInstanceVO>({ url: `/api/pub/group-buy/instance/${instanceId}`, anonymous: true }),

  launchGroupBuy: (data: { activityId: number } & GroupBuyOrderDto) =>
    request<{ instanceId: number; shareCode: string }>({ url: '/api/u/group-buy/launch', method: 'POST', data }),

  joinGroupBuy: (instanceId: number, data: GroupBuyOrderDto) =>
    request<void>({ url: `/api/u/group-buy/${instanceId}/join`, method: 'POST', data }),
};
