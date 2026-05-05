export interface UserProfileVO {
  id: number;
  nickname: string;
  avatar: string;
  phone: string;
  gender: number;
  birthday: string;
  tag: string;
  registerTime: string;
  couponCount: number;
  favoriteCount: number;
  footprintCount: number;
  groupBuyCount: number;
}
export interface AddressVO {
  id: number;
  consignee: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  tag?: string;
  isDefault: 0 | 1;
  longitude?: number;
  latitude?: number;
}
export interface AddressDto {
  consignee: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  tag?: string;
  isDefault?: 0 | 1;
}
