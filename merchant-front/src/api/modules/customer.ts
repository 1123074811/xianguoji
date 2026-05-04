import { request } from '@/api/request';
import type { PageVO } from '@/api/types/common';
import type { CustomerVO } from '@/api/types/customer';

export const customerApi = {
  page: (params: { page?: number; size?: number; keyword?: string }) =>
    request<PageVO<CustomerVO>>({ url: '/api/admin/customer/page', params }),

  detail: (id: number) =>
    request<CustomerVO>({ url: `/api/admin/customer/${id}` }),
};
