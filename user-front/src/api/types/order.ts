export interface OrderPreviewVO {
  items: OrderPreviewItemVO[];
  address: import('./user').AddressVO | null;
  pickupPoint: import('./shop').PickupPointVO | null;
  deliveryFee: string;
  originalDeliveryFee: string;
  discountAmount: string;
  totalAmount: string;
  availableCoupons: import('./promo').UserCouponVO[];
}
export interface OrderPreviewItemVO {
  cartItemId: number;
  productId: number;
  skuId: number;
  productName: string;
  mainImage: string;
  specName: string;
  price: string;
  quantity: number;
  subtotal: string;
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
  orderNo: string;
  status: number;
  statusText: string;
  deliveryType: 1 | 2;
  totalAmount: string;
  discountAmount: string;
  deliveryFee: string;
  payAmount: string;
  createTime: string;
  payTime?: string;
  deliveryTime?: string;
  finishTime?: string;
  items: OrderItemVO[];
  address?: import('./user').AddressVO;
  pickupPoint?: import('./shop').PickupPointVO;
}
export interface OrderItemVO {
  productId: number;
  skuId: number;
  productName: string;
  mainImage: string;
  specName: string;
  price: string;
  quantity: number;
  subtotal: string;
}
export interface RefundVO {
  refundNo: string;
  orderNo: string;
  reason: string;
  status: number;
  statusText: string;
  amount: string;
  createTime: string;
  images: string[];
}
export interface RefundSubmitDto {
  orderNo: string;
  reason: string;
  images?: string[];
}
