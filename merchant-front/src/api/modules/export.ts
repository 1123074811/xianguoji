import { request } from '@/api/request';

export const exportApi = {
  orderExport: (params: { start?: string; end?: string; status?: number }) =>
    request<Blob>({ url: '/api/admin/export/order', params, responseType: 'blob' }),
};
