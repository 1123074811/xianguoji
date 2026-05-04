import { request } from '@/api/request';
import type { LoginVO } from '@/api/types/auth';
import type { SmsSendDto, SmsLoginDto, WechatLoginDto } from '@/api/types/auth';

export const authApi = {
  sendSms: (data: SmsSendDto) =>
    request<void>({ url: '/api/pub/auth/sms/send', method: 'POST', data, anonymous: true }),

  smsLogin: (data: SmsLoginDto) =>
    request<LoginVO>({ url: '/api/pub/auth/login/sms', method: 'POST', data, anonymous: true }),

  wechatLogin: (data: WechatLoginDto) =>
    request<LoginVO>({ url: '/api/pub/auth/login/wechat', method: 'POST', data, anonymous: true }),

  logout: () =>
    request<void>({ url: '/api/u/auth/logout', method: 'POST' }),
};
