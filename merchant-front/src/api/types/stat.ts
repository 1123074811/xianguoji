/** 后端 GET /api/admin/stat/dashboard 返回（4 指标卡） */
export interface DashboardVO {
  todayOrders: number;
  todayRevenue: string;
  todayNewUsers: number;
  pendingOrders: number;
}

/** 后端 GET /api/admin/stat/order-trend 返回 */
export type OrderTrendItem = { date: string; count: number };

/** 后端 GET /api/admin/stat/order-status 返回（status -> count） */
export type OrderStatusMap = Record<number, number>;

/** 后端 GET /api/admin/stat/top-products 返回（直接 Product 实体列表） */
export interface TopProductVO {
  id: number;
  name: string;
  mainImage: string;
  minPrice: string;
  maxPrice: string;
  totalStock: number;
  sales: number;
}

/** 后端 GET /api/admin/stat/todo 返回 */
export interface TodoVO {
  pendingAccept: number;
  pendingReview: number;
  stockWarn: number;
  pendingRefund: number;
}
