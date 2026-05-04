export interface AdminCouponVO {
  id: number;
  name: string;
  type: number;
  amount: string;
  threshold: string;
  startTime: string;
  endTime: string;
  totalCount: number;
  claimedCount: number;
  usedCount: number;
  status: number;
}
export interface CouponCreateDto {
  name: string;
  type: number;
  amount: string;
  threshold: string;
  startTime: string;
  endTime: string;
  totalCount: number;
}
export interface AdminGroupBuyVO {
  id: number;
  productId: number;
  productName: string;
  mainImage: string;
  groupPrice: string;
  originalPrice: string;
  groupSize: number;
  startTime: string;
  endTime: string;
  status: number;
}
