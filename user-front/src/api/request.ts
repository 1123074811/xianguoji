import { useUserStore } from '@/stores/user';

const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';

export interface R<T = any> {
  code: number;
  msg: string;
  data: T;
  ts: number;
}

export interface PageVO<T> {
  total: number;
  list: T[];
  page: number;
  size: number;
}

interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: any;
  params?: Record<string, any>;
  header?: Record<string, string>;
  /** 不需要 Toast 失败提示时设 true */
  silent?: boolean;
  /** 不需要登录态也不会 401 跳转 */
  anonymous?: boolean;
}

export function request<T = any>(opts: RequestOptions): Promise<T> {
  const userStore = useUserStore();
  const token = userStore.token;

  // GET 参数拼接
  let url = opts.url.startsWith('http') ? opts.url : `${BASE_URL}${opts.url}`;
  if (opts.params && Object.keys(opts.params).length) {
    const qs = Object.entries(opts.params)
      .filter(([, v]) => v !== undefined && v !== null && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join('&');
    url += (url.includes('?') ? '&' : '?') + qs;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: opts.method || 'GET',
      data: opts.data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...opts.header,
      },
      success: (res) => {
        // 1. HTTP 层异常
        if (res.statusCode < 200 || res.statusCode >= 300) {
          // 尝试从响应体识别认证错误
          const body = res.data as R<T>;
          if (body && (body.code === 4010 || body.code === 4011) && !opts.anonymous) {
            handleAuthFail();
            return reject(body);
          }
          if ((res.statusCode === 401) && !opts.anonymous) handleAuthFail();
          !opts.silent && uni.showToast({ title: `网络异常 ${res.statusCode}`, icon: 'none' });
          return reject(res);
        }
        // 2. 业务码
        const body = res.data as R<T>;
        if (body.code === 0) return resolve(body.data);
        if ((body.code === 4010 || body.code === 4011) && !opts.anonymous) {
          handleAuthFail();
          return reject(body);
        }
        !opts.silent && uni.showToast({ title: body.msg || '请求失败', icon: 'none' });
        reject(body);
      },
      fail: (err) => {
        !opts.silent && uni.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      },
    });
  });
}

function handleAuthFail() {
  const userStore = useUserStore();
  userStore.logout();
  uni.showToast({ title: '请重新登录', icon: 'none' });
  setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 800);
}
