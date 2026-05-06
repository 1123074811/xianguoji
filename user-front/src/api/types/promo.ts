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
  status: 0 | 1 | 2;
  expireAt: string;
  coupon: CouponVO;
  unavailableReason?: string;
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
export interface GroupBuyParticipantVO {
  userId: number;
  nickname: string;
  avatar: string;
  isLeader: 0 | 1;
  joinedAt: string;
}
export interface GroupBuyInstanceVO {
  id: number;
  activityId: number;
  leaderId: number;
  leaderName: string;
  leaderAvatar: string;
  currentSize: number;
  targetSize: number;
  status: number; // 1 拼团中 2 已成团 3 已失败
  expireAt: string;
  successAt?: string;
  shareCode?: string;
  productId?: number;
  skuId?: number;
  productName?: string;
  mainImage?: string;
  groupPrice?: string;
  groupSize?: number;
  participants: GroupBuyParticipantVO[];
}
