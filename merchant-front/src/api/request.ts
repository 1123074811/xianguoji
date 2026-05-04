import axios, { AxiosError } from 'axios';
import type { AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import { useAdminStore } from '@/stores/admin';

const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';

export interface R<T = any> { code: number; msg: string; data: T; ts: number; }
export interface PageVO<T> { total: number; list: T[]; page: number; size: number; }

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' },
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const store = useAdminStore();
  if (store.token) config.headers.Authorization = `Bearer ${store.token}`;
  return config;
});

instance.interceptors.response.use(
  (res) => {
    const body = res.data as R<any>;
    if (body.code === 0) return body.data;
    if (body.code === 4010 || body.code === 4011) {
      handleAuthFail();
      return Promise.reject(body);
    }
    showError(body.msg);
    return Promise.reject(body);
  },
  (err: AxiosError) => {
    if (err.response?.status === 401) handleAuthFail();
    showError(err.message);
    return Promise.reject(err);
  },
);

export function request<T = any>(config: AxiosRequestConfig & { silent?: boolean }): Promise<T> {
  return instance.request<any, T>(config);
}

function handleAuthFail() {
  useAdminStore().logout();
  window.location.href = '/login';
}

function showError(msg?: string) {
  console.error('[API ERROR]', msg);
  // TODO: 联调-接入 ElMessage / 自研 Toast
}
