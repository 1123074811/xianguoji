import { request } from '@/api/request';
import type { AdminLoginVO, AdminUserInfoVO, AdminLoginDto, CaptchaVO } from '@/api/types/auth';

export const adminAuthApi = {
  login: (data: AdminLoginDto) =>
    request<AdminLoginVO>({ url: '/api/pub/admin/login', method: 'POST', data }),

  me: () =>
    request<AdminUserInfoVO>({ url: '/api/admin/auth/me' }),

  logout: () =>
    request<void>({ url: '/api/admin/auth/logout', method: 'POST' }),

  captcha: () =>
    request<CaptchaVO>({ url: '/api/pub/captcha' }),
};
