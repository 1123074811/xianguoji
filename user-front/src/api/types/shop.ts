export interface ShopVO {
  id: number;
  name: string;
  logo: string;
  description: string;
  phone: string;
  address: string;
  businessHours: string;
  isOpen: number;
  autoAccept: number;
  voiceNotify: number;
}
export interface PickupPointVO {
  id: number;
  name: string;
  address: string;
  phone: string;
  businessHours: string;
  latitude: number;
  longitude: number;
}
export interface DeliverySettingVO {
  minOrderAmount: string;
  baseFee: string;
  freeAmount: string;
  timeSlots: { label: string; start: string; end: string }[];
}
