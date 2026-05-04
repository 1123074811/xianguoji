import { request } from '@/api/request';
import type {
  DashboardVO,
  OrderTrendItem,
  OrderStatusMap,
  TopProductVO,
  TodoVO,
} from '@/api/types/stat';

export const statApi = {
  dashboard: () =>
    request<DashboardVO>({ url: '/api/admin/stat/dashboard' }),

  orderTrend: (days = 7) =>
    request<OrderTrendItem[]>({ url: '/api/admin/stat/order-trend', params: { days } }),

  orderStatus: () =>
    request<OrderStatusMap>({ url: '/api/admin/stat/order-status' }),

  topProducts: (limit = 10) =>
    request<TopProductVO[]>({ url: '/api/admin/stat/top-products', params: { limit } }),

  todo: () =>
    request<TodoVO>({ url: '/api/admin/stat/todo' }),
};
