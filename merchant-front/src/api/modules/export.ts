import { request } from '@/api/request';

export interface ReportExportRecordVO {
  id: number;
  staffId: number;
  reportType: string;
  startDate: string;
  endDate: string;
  format: string;
  fileName: string;
  fileSize: number;
  status: number;
  createdAt: string;
}

export interface ReportExportDto {
  reportType: string;
  startDate: string;
  endDate: string;
  format: string;
  includeCharts?: boolean;
  detailedMode?: boolean;
}

export const exportApi = {
  generate: (data: ReportExportDto) =>
    request<Blob>({ url: '/api/admin/report/export', method: 'POST', data, responseType: 'blob' }),

  history: (limit = 20) =>
    request<ReportExportRecordVO[]>({ url: '/api/admin/report/list', params: { limit } }),

  downloadHistory: (id: number) =>
    request<Blob>({ url: `/api/admin/report/download/${id}`, responseType: 'blob' }),

  remove: (id: number) =>
    request<void>({ url: `/api/admin/report/${id}`, method: 'DELETE' }),
};
