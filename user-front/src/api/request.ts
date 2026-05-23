import { useUserStore } from '@/stores/user';

// F-1: 环境切换 + F-2: 生产环境强制 HTTPS
const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';
let CLIENT_TYPE = import.meta.env.VITE_CLIENT_TYPE || 'h5';
// #ifdef MP-WEIXIN
CLIENT_TYPE = 'miniapp';
// #endif
// #ifdef APP-PLUS
CLIENT_TYPE = 'app';
// #endif

// F-2: 生产环境校验 HTTPS
if (import.meta.env.PROD && BASE_URL.startsWith('http://')) {
  console.warn('[F-2] 生产环境应使用 HTTPS，当前 BASE_URL=' + BASE_URL);
}

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
  /** F-6: 幂等请求 ID */
  requestId?: string;
}

// F-3: 简易 token 混淆（XOR + Base64，防止明文存储）
// 注意：微信小程序运行时不提供全局 btoa/atob，必须使用便携式 Base64 实现
const B64_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

function b64encode(input: string): string {
  let output = '';
  let i = 0;
  while (i < input.length) {
    const c1 = input.charCodeAt(i++) & 0xff;
    const c2 = i < input.length ? input.charCodeAt(i++) & 0xff : NaN;
    const c3 = i < input.length ? input.charCodeAt(i++) & 0xff : NaN;
    const e1 = c1 >> 2;
    const e2 = ((c1 & 3) << 4) | (isNaN(c2) ? 0 : c2 >> 4);
    const e3 = isNaN(c2) ? 64 : (((c2 & 15) << 2) | (isNaN(c3) ? 0 : c3 >> 6));
    const e4 = isNaN(c3) ? 64 : (c3 & 63);
    output += B64_CHARS.charAt(e1) + B64_CHARS.charAt(e2)
            + (e3 === 64 ? '=' : B64_CHARS.charAt(e3))
            + (e4 === 64 ? '=' : B64_CHARS.charAt(e4));
  }
  return output;
}

function b64decode(input: string): string {
  const clean = input.replace(/[^A-Za-z0-9+/=]/g, '');
  let output = '';
  let i = 0;
  while (i < clean.length) {
    const ch1 = clean.charAt(i++);
    const ch2 = clean.charAt(i++);
    const ch3 = clean.charAt(i++);
    const ch4 = clean.charAt(i++);
    const e1 = B64_CHARS.indexOf(ch1);
    const e2 = B64_CHARS.indexOf(ch2);
    const e3 = ch3 === '=' ? 64 : B64_CHARS.indexOf(ch3);
    const e4 = ch4 === '=' ? 64 : B64_CHARS.indexOf(ch4);
    if (e1 < 0 || e2 < 0 || e3 < 0 || e4 < 0) {
      throw new Error('Invalid base64');
    }
    const c1 = (e1 << 2) | (e2 >> 4);
    const c2 = ((e2 & 15) << 4) | (e3 >> 2);
    const c3 = ((e3 & 3) << 6) | e4;
    output += String.fromCharCode(c1);
    if (e3 !== 64) output += String.fromCharCode(c2);
    if (e4 !== 64) output += String.fromCharCode(c3);
  }
  return output;
}

function encryptToken(token: string): string {
  if (!token) return '';
  const mask = 'xgj2025';
  let result = '';
  for (let i = 0; i < token.length; i++) {
    result += String.fromCharCode(token.charCodeAt(i) ^ mask.charCodeAt(i % mask.length));
  }
  return b64encode(result);
}

function isHeaderSafeToken(token: string): boolean {
  return !!token && !/[\u0000-\u001F\u007F]/.test(token);
}

function decryptToken(encrypted: string): string {
  if (!encrypted) return '';
  try {
    const mask = 'xgj2025';
    const decoded = b64decode(encrypted);
    let result = '';
    for (let i = 0; i < decoded.length; i++) {
      result += String.fromCharCode(decoded.charCodeAt(i) ^ mask.charCodeAt(i % mask.length));
    }
    if (!isHeaderSafeToken(result) && isHeaderSafeToken(encrypted)) {
      return encrypted;
    }
    return result;
  } catch {
    return isHeaderSafeToken(encrypted) ? encrypted : '';
  }
}

export { encryptToken, decryptToken };

// F-7: 错误日志上报
function reportError(level: string, info: string) {
  const userStore = useUserStore();
  const logLine = `[${level}] uid=${userStore.userInfo?.id || '-'} ts=${Date.now()} ${info}`;
  console.error(logLine);
  // 可扩展为上报到后端 /api/pub/log/error
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

  // F-2: 生产环境禁止非 HTTPS 请求
  if (import.meta.env.PROD && url.startsWith('http://')) {
    reportError('SEC', `F-2: HTTP请求被拦截 url=${url}`);
    return Promise.reject(new Error('生产环境仅允许HTTPS请求'));
  }

  console.log('[request]', opts.method || 'GET', url, 'token=', token ? 'yes' : 'no');
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: opts.method || 'GET',
      data: opts.data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        // F-6: 幂等请求 ID
        ...(opts.requestId ? { 'X-Request-Id': opts.requestId } : {}),
        // F-9: 防抓包标记
        'X-Client-Type': CLIENT_TYPE,
        ...opts.header,
      },
      success: (res) => {
        // 处理 token 续期（S-7）
        const renewalToken = res.header?.['X-Token-Renewal'] || res.header?.['x-token-renewal'];
        if (renewalToken && typeof renewalToken === 'string') {
          userStore.setToken(renewalToken);
        }

        // 1. HTTP 层异常
        if (res.statusCode < 200 || res.statusCode >= 300) {
          const body = res.data as R<T>;
          if (body && (body.code === 4010 || body.code === 4011) && !opts.anonymous) {
            handleAuthFail();
            return reject(body);
          }
          if ((res.statusCode === 401) && !opts.anonymous) handleAuthFail();
          !opts.silent && uni.showToast({ title: (body && body.msg) || `网络异常 ${res.statusCode}`, icon: 'none' });
          // F-7: 记录 HTTP 错误
          reportError('HTTP', `${opts.method} ${url} status=${res.statusCode}`);
          return reject(body || res);
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
        // F-7: 记录网络错误
        reportError('NET', `${opts.method} ${url} err=${err.errMsg}`);
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
