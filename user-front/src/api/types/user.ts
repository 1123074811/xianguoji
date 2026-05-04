export interface UserProfileVO {
  id: number;
  nickname: string;
  avatar: string;
  phone: string;
  gender: number;
  birthday: string;
  registerTime: string;
}
export interface AddressVO {
  id: number;
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  isDefault: 0 | 1;
}
export interface AddressDto {
  name: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  isDefault?: 0 | 1;
}
