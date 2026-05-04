import { request } from '@/api/request';
import type { AdminShopVO, AdminDeliverySettingVO, AdminPickupPointVO } from '@/api/types/shop';

export const adminShopApi = {
  shopInfo: () =>
    request<AdminShopVO>({ url: '/api/admin/shop/info' }),

  updateShopInfo: (data: Partial<AdminShopVO>) =>
    request<void>({ url: '/api/admin/shop/info', method: 'PUT', data }),

  updateOpenStatus: (isOpen: boolean) =>
    request<void>({ url: '/api/admin/shop/open-status', method: 'PUT', data: { isOpen } }),

  deliverySetting: () =>
    request<AdminDeliverySettingVO>({ url: '/api/admin/delivery-setting' }),

  updateDeliverySetting: (data: Partial<AdminDeliverySettingVO>) =>
    request<void>({ url: '/api/admin/delivery-setting', method: 'PUT', data }),

  pickupPointList: () =>
    request<AdminPickupPointVO[]>({ url: '/api/admin/pickup-point/list' }),

  createPickupPoint: (data: Omit<AdminPickupPointVO, 'id'>) =>
    request<{ id: number }>({ url: '/api/admin/pickup-point', method: 'POST', data }),

  updatePickupPoint: (id: number, data: Partial<AdminPickupPointVO>) =>
    request<void>({ url: `/api/admin/pickup-point/${id}`, method: 'PUT', data }),

  deletePickupPoint: (id: number) =>
    request<void>({ url: `/api/admin/pickup-point/${id}`, method: 'DELETE' }),
};
