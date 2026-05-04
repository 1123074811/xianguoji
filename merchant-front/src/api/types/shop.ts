export interface AdminShopVO {
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
  createdAt: string;
  updatedAt: string;
}

export interface AdminDeliverySettingVO {
  id: number;
  minOrderAmount: string;
  baseFee: string;
  freeAmount: string;
  timeSlots: any;
  serviceArea: any;
  updatedAt: string;
}

export interface AdminPickupPointVO {
  id: number;
  name: string;
  address: string;
  phone: string;
  businessHours: string;
  latitude: number;
  longitude: number;
  sort: number;
}

export interface AdminNotificationVO {
  id: number;
  type: number;
  title: string;
  content: string;
  linkUrl: string;
  isRead: number;
  createdAt: string;
}

export interface NotifySettingVO {
  id: number;
  eventKey: string;
  eventName: string;
  enableVoice: number;
  enableSms: number;
  enableApp: number;
}
