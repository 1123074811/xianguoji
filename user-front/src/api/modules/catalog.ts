import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type {
  ProductVO, ProductDetailVO, CategoryTreeVO, BannerVO,
} from '@/api/types/catalog';

export const catalogApi = {
  categoryTree: () =>
    request<CategoryTreeVO[]>({ url: '/api/pub/category/tree', anonymous: true }),

  bannerList: () =>
    request<BannerVO[]>({ url: '/api/pub/banner/list', anonymous: true }),

  hotSearch: () =>
    request<string[]>({ url: '/api/pub/hot-search/list', anonymous: true }),

  productPage: (params: {
    page?: number; size?: number;
    categoryId?: number; keyword?: string;
    sort?: 'sales' | 'price_asc' | 'price_desc';
  }) =>
    request<PageVO<ProductVO>>({ url: '/api/pub/product/page', params, anonymous: true }),

  recommend: () =>
    request<ProductVO[]>({ url: '/api/pub/product/recommend', anonymous: true }),

  productDetail: (id: number) =>
    request<ProductDetailVO>({ url: `/api/pub/product/${id}`, anonymous: true }),

  recordSearch: (keyword: string) =>
    request<void>({ url: '/api/u/search/record', method: 'POST', data: { keyword }, silent: true }),

  searchHistory: () =>
    request<string[]>({ url: '/api/u/search/history' }),

  clearSearchHistory: () =>
    request<void>({ url: '/api/u/search/history', method: 'DELETE' }),
};
