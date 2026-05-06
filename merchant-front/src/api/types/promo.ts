export interface AdminCouponVO {
  id: number;
  name: string;
  type: number;
  amount: string;
  minAmount: string;
  startTime: string;
  endTime: string;
  total: number;
  receivedCount: number;
  usedCount: number;
  status: number;
  createdAt: string;
  updatedAt: string;
}
export interface CouponCreateDto {
  name: string;
  type: number;
  amount: string;
  minAmount: string;
  startTime: string;
  endTime: string;
  total: number;
}
export interface AdminGroupBuyVO {
  id: number;
  productId: number;
  skuId: number;
  productName?: string;
  mainImage?: string;
  groupPrice: string;
  originalPrice?: string;
  groupSize: number;
  validHours: number;
  startTime: string;
  endTime: string;
  totalJoinCount?: number;
  successCount?: number;
  status: number;
}
