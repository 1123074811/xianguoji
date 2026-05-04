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

  analysisKpi: (days = 7) =>
    request<any>({ url: '/api/admin/stat/analysis/kpi', params: { days } }),

  revenueTrend: (months = 6) =>
    request<any[]>({ url: '/api/admin/stat/analysis/revenue-trend', params: { months } }),

  categoryDistribution: () =>
    request<any[]>({ url: '/api/admin/stat/analysis/category-distribution' }),

  hourlyHeatmap: () =>
    request<any[]>({ url: '/api/admin/stat/analysis/hourly-heatmap' }),

  customerSegments: () =>
    request<any>({ url: '/api/admin/stat/analysis/customer-segments' }),

  productPerformance: (limit = 10) =>
    request<any[]>({ url: '/api/admin/stat/analysis/product-performance', params: { limit } }),
};
