export interface LoginVO {
  token: string;
  expireAt: string;
  userInfo: UserInfoVO;
}
export interface UserInfoVO {
  id: number;
  nickname: string;
  avatar: string;
  phone: string;
  role: 'user' | 'staff';
  staffRole?: 'owner' | 'admin' | 'packer' | 'courier';
}
export interface SmsSendDto { phone: string; }
export interface SmsLoginDto { phone: string; code: string; }
export interface WechatLoginDto { code: string; }
