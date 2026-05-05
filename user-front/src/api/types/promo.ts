export interface CouponVO {
  id: number;
  name: string;
  type: number;
  amount: string;
  threshold: string;
  startTime: string;
  endTime: string;
  status: number;
  remaining: number;
  perUserLimit: number;
  userReceivedCount: number;
}
export interface UserCouponVO {
  id: number;
  couponId: number;
  name: string;
  type: number;
  amount: string;
  threshold: string;
  startTime: string;
  endTime: string;
  status: 0 | 1 | 2;
}
export interface GroupBuyActivityVO {
  id: number;
  productId: number;
  skuId: number;
  productName: string;
  mainImage: string;
  groupPrice: string;
  originalPrice: string;
  groupSize: number;
  validHours: number;
  endTime: string;
  totalJoinCount: number;
  successCount: number;
  status: number;
}
export interface GroupBuyInstanceVO {
  id: number;
  activityId: number;
  leaderId: number;
  currentCount: number;
  groupSize: number;
  endTime: string;
  status: number;
}
