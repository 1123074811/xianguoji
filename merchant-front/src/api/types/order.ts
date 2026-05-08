/**
 * 后端 OrderVO（com.xianguoji.server.module.order.vo.OrderVO）
 * 商家端订单复用同一个 VO（AdminOrderController 返回 OrderVO）
 */
export interface AdminOrderVO {
  id: number;
  orderNo: string;
  status: number;
  payStatus: number;
  deliveryType: 1 | 2;
  deliveryTime?: string;
  consignee: string;
  consigneePhone: string;
  consigneeAddress: string;
  pickupCode?: string;
  goodsAmount: string;
  couponAmount: string;
  discountAmount: string;
  deliveryFee: string;
  payAmount: string;
  userRemark?: string;
  cancelReason?: string;
  groupBuyInstanceId?: number;
  payTime?: string;
  deliveredAt?: string;
  finishedAt?: string;
  createdAt: string;
  items: AdminOrderItemVO[];
}

export interface AdminOrderItemVO {
  id: number;
  productId: number;
  skuId: number;
  productName: string;
  specName: string;
  image: string;
  price: string;
  originalPrice: string;
  quantity: number;
  subtotal: string;
  isReviewed: number;
}

export interface AdminOrderQry {
  status?: number;
  keyword?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export interface ShipDto {
  /** 1=配送 2=自提 */
  deliveryType?: number;
  courierName?: string;
  courierPhone?: string;
}
