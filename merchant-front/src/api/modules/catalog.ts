import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type {
  AdminProductVO, AdminProductDetailVO, AdminCategoryVO, AdminBannerVO,
} from '@/api/types/catalog';

export const adminCatalogApi = {
  productPage: (params: { page?: number; size?: number; keyword?: string; categoryId?: number; status?: number }) =>
    request<PageVO<AdminProductVO>>({ url: '/api/admin/product/page', params }),

  /** 后端商家端无独立 detail，复用公共接口（包含 SKU/图片/详情） */
  productDetail: (id: number) =>
    request<AdminProductDetailVO>({ url: `/api/pub/product/${id}` }),

  createProduct: (data: any) =>
    request<{ id: number }>({ url: '/api/admin/product', method: 'POST', data }),

  updateProduct: (id: number, data: any) =>
    request<void>({ url: `/api/admin/product/${id}`, method: 'PUT', data }),

  /** 后端禁止物理删除商品；下架 = status=2（回收站），与 backend-spec §7.4 一致 */
  deleteProduct: (id: number) =>
    request<void>({ url: `/api/admin/product/${id}/status`, method: 'PUT', data: { status: 2 } }),

  updateProductStatus: (id: number, status: 0 | 1 | 2) =>
    request<void>({ url: `/api/admin/product/${id}/status`, method: 'PUT', data: { status } }),

  copyProduct: (id: number) =>
    request<void>({ url: `/api/admin/product/${id}/copy`, method: 'POST' }),

  categoryList: () =>
    request<AdminCategoryVO[]>({ url: '/api/admin/category/list' }),

  createCategory: (data: { name: string; icon: string; sort: number; parentId?: number }) =>
    request<{ id: number }>({ url: '/api/admin/category', method: 'POST', data }),

  updateCategory: (id: number, data: Partial<AdminCategoryVO>) =>
    request<void>({ url: `/api/admin/category/${id}`, method: 'PUT', data }),

  deleteCategory: (id: number) =>
    request<void>({ url: `/api/admin/category/${id}`, method: 'DELETE' }),

  bannerList: () =>
    request<AdminBannerVO[]>({ url: '/api/admin/banner/list' }),

  createBanner: (data: Omit<AdminBannerVO, 'id'>) =>
    request<{ id: number }>({ url: '/api/admin/banner', method: 'POST', data }),

  updateBanner: (id: number, data: Partial<AdminBannerVO>) =>
    request<void>({ url: `/api/admin/banner/${id}`, method: 'PUT', data }),

  deleteBanner: (id: number) =>
    request<void>({ url: `/api/admin/banner/${id}`, method: 'DELETE' }),
};
