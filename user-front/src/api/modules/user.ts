import { request } from '@/api/request';
import type { UserProfileVO, AddressVO, AddressDto } from '@/api/types/user';
import type { PageVO } from '@/api/types/common';
import type { ProductVO } from '@/api/types/catalog';

export const userApi = {
  profile: () =>
    request<UserProfileVO>({ url: '/api/u/user/profile' }),

  updateProfile: (data: Partial<UserProfileVO>) =>
    request<void>({ url: '/api/u/user/profile', method: 'PUT', data }),

  addressList: () =>
    request<AddressVO[]>({ url: '/api/u/address/list' }),

  addAddress: (data: AddressDto) =>
    request<AddressVO>({ url: '/api/u/address', method: 'POST', data }),

  updateAddress: (id: number, data: AddressDto) =>
    request<void>({ url: `/api/u/address/${id}`, method: 'PUT', data }),

  deleteAddress: (id: number) =>
    request<void>({ url: `/api/u/address/${id}`, method: 'DELETE' }),

  setDefaultAddress: (id: number) =>
    request<void>({ url: `/api/u/address/${id}/default`, method: 'PUT' }),

  footprintPage: (params: { page?: number; size?: number }) =>
    request<PageVO<ProductVO>>({ url: '/api/u/footprint/page', params }),

  addFootprint: (productId: number) =>
    request<void>({ url: `/api/u/footprint/${productId}`, method: 'POST' }),

  clearFootprint: () =>
    request<void>({ url: '/api/u/footprint', method: 'DELETE' }),

  favoritePage: (params: { page?: number; size?: number }) =>
    request<PageVO<ProductVO>>({ url: '/api/u/favorite/page', params }),

  addFavorite: (productId: number) =>
    request<void>({ url: `/api/u/favorite/${productId}`, method: 'POST' }),

  removeFavorite: (productId: number) =>
    request<void>({ url: `/api/u/favorite/${productId}`, method: 'DELETE' }),

  bindPhone: (data: { code: string }) =>
    request<void>({ url: '/api/u/user/bind-phone', method: 'POST', data }),
};
