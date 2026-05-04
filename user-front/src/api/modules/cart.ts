import { request } from '@/api/request';
import type { CartItemVO, CartListVO } from '@/api/types/cart';

export const cartApi = {
  list: () =>
    request<CartListVO>({ url: '/api/u/cart/list' }),

  add: (data: { skuId: number; quantity: number }) =>
    request<void>({ url: '/api/u/cart', method: 'POST', data }),

  updateQuantity: (id: number, quantity: number) =>
    request<void>({ url: `/api/u/cart/${id}/quantity`, method: 'PUT', data: { quantity } }),

  updateSelected: (data: { ids: number[]; selected: 0 | 1 }) =>
    request<void>({ url: '/api/u/cart/selected', method: 'PUT', data }),

  delete: (id: number) =>
    request<void>({ url: `/api/u/cart/${id}`, method: 'DELETE' }),

  clear: () =>
    request<void>({ url: '/api/u/cart/clear', method: 'DELETE' }),

  count: () =>
    request<number>({ url: '/api/u/cart/count' }),
};
