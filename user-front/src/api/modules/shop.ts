import { request } from '@/api/request';
import type { ShopVO, PickupPointVO, DeliverySettingVO } from '@/api/types/shop';

export const shopApi = {
  shopInfo: () =>
    request<ShopVO>({ url: '/api/pub/shop/info', anonymous: true }),

  pickupPointList: () =>
    request<PickupPointVO[]>({ url: '/api/pub/pickup-point/list', anonymous: true }),

  deliverySetting: () =>
    request<DeliverySettingVO>({ url: '/api/pub/delivery-setting', anonymous: true }),
};
