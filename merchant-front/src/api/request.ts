import axios, { AxiosError } from 'axios';
import type { AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import { useAdminStore } from '@/stores/admin';

// F-1: 环境切换
const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';

// F-2: 生产环境强制 HTTPS
if (import.meta.env.PROD && BASE_URL.startsWith('http://')) {
  console.warn('[F-2] 生产环境应使用 HTTPS，当前 BASE_URL=' + BASE_URL);
}

export interface R<T = any> { code: number; msg: string; data: T; ts: number; }
export interface PageVO<T> { total: number; list: T[]; page: number; size: number; }

const instance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
    // F-9: 防抓包标记
    'X-Client-Type': 'merchant-web',
  },
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const store = useAdminStore();
  if (store.token) config.headers.Authorization = `Bearer ${store.token}`;
  // F-2: 生产环境禁止 HTTP
  if (import.meta.env.PROD && config.url?.startsWith('http://')) {
    return Promise.reject(new Error('生产环境仅允许HTTPS请求'));
  }
  return config;
});

instance.interceptors.response.use(
  (res) => {
    // S-7: Token 续期
    const renewalToken = res.headers['x-token-renewal'];
    if (renewalToken && typeof renewalToken === 'string') {
      useAdminStore().setToken(renewalToken);
    }

    // 二进制下载（blob/arraybuffer）直接返回原始数据
    if (res.config.responseType === 'blob' || res.config.responseType === 'arraybuffer') {
      return res.data;
    }
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
    // F-7: 错误日志
    reportError('HTTP', `${err.config?.method} ${err.config?.url} status=${err.response?.status}`);
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
}

// F-7: 错误日志上报
function reportError(level: string, info: string) {
  const store = useAdminStore();
  console.error(`[${level}] sid=${store.staffInfo?.id || '-'} ts=${Date.now()} ${info}`);
}
