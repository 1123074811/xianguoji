import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { StaffVO, StaffCreateDto } from '@/api/types/staff';

export interface StaffSecurityVO {
  passwordChangedAt: string;
  lastLoginAt: string;
}

export const staffApi = {
  page: (params: { page?: number; size?: number }) =>
    request<PageVO<StaffVO>>({ url: '/api/admin/staff/page', params }),

  create: (data: StaffCreateDto) =>
    request<{ id: number }>({ url: '/api/admin/staff', method: 'POST', data }),

  update: (id: number, data: Partial<StaffCreateDto>) =>
    request<void>({ url: `/api/admin/staff/${id}`, method: 'PUT', data }),

  delete: (id: number) =>
    request<void>({ url: `/api/admin/staff/${id}`, method: 'DELETE' }),

  securityInfo: () =>
    request<StaffSecurityVO>({ url: '/api/admin/staff/me/security' }),

  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request<void>({ url: '/api/admin/staff/me/password', method: 'PUT', data }),

  logoutOthers: () =>
    request<{ count: number }>({ url: '/api/admin/staff/me/logout-others', method: 'POST' }),
};
