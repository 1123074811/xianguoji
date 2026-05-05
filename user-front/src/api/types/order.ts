export interface OrderPreviewVO {
  items: OrderPreviewItemVO[];
  goodsAmount: string;
  discountAmount: string;
  couponAmount: string;
  deliveryFee: string;
  payAmount: string;
  promotionTip: string;
  address: PreviewAddressVO | null;
  pickupPoint: PreviewPickupPointVO | null;
}
export interface OrderPreviewItemVO {
  skuId: number;
  productName: string;
  specName: string;
  image: string;
  price: string;
  originalPrice: string;
  quantity: number;
  subtotal: string;
}
export interface PreviewAddressVO {
  id: number;
  consignee: string;
  phone: string;
  fullAddress: string;
}
export interface PreviewPickupPointVO {
  id: number;
  name: string;
  address: string;
}
export interface OrderSubmitDto {
  addressId?: number;
  pickupPointId?: number;
  deliveryType: 1 | 2;
  deliveryTime?: string;
  cartItemIds?: number[];
  userCouponId?: number;
  userRemark?: string;
  payMethod: 'wechat' | 'alipay';
  groupBuyActivityId?: number;
  groupBuyInstanceId?: number;
}
export interface OrderVO {
  id: number;
  orderNo: string;
  status: number;
  payStatus: number;
  deliveryType: 1 | 2;
  deliveryTime?: string;
  consignee?: string;
  consigneePhone?: string;
  consigneeAddress?: string;
  pickupCode?: string;
  goodsAmount: string;
  couponAmount: string;
  discountAmount: string;
  deliveryFee: string;
  payAmount: string;
  userRemark?: string;
  cancelReason?: string;
  payTime?: string;
  deliveredAt?: string;
  finishedAt?: string;
  createdAt: string;
  items: OrderItemVO[];
}
export interface OrderItemVO {
  id: number;
  productId: number;
  skuId: number;
  productName: string;
  specName: string;
  image: string;
  price: string;
  originalPrice?: string;
  quantity: number;
  subtotal: string;
  isReviewed: number;
}
export interface RefundVO {
  id: number;
  refundNo: string;
  orderNo: string;
  type: number;
  amount: string;
  reason: string;
  images: string[];
  status: number;
  rejectReason?: string;
  handledAt?: string;
  createdAt: string;
}
export interface RefundSubmitDto {
  orderNo: string;
  reason: string;
  images?: string[];
}
