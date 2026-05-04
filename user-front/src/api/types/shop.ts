export interface ShopVO {
  id: number;
  name: string;
  logo: string;
  phone: string;
  address: string;
  openTime: string;
  closeTime: string;
  isOpen: boolean;
  notice: string;
}
export interface PickupPointVO {
  id: number;
  name: string;
  address: string;
  phone: string;
  openTime: string;
  closeTime: string;
  latitude: number;
  longitude: number;
}
export interface DeliverySettingVO {
  freeThreshold: string;
  baseFee: string;
  distanceFee: string;
  maxDistance: number;
  supportDelivery: boolean;
  supportPickup: boolean;
}
