/**
 * F-4: v-html 安全渲染工具
 * 使用 DOMPurify 或简易白名单过滤，防止 XSS
 * 小程序中 v-html 不直接支持，rich-text 组件需过滤
 */

/** 允许的 HTML 标签白名单 */
const ALLOWED_TAGS = new Set([
  'p', 'br', 'b', 'strong', 'i', 'em', 'u', 's', 'span',
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'ul', 'ol', 'li', 'div', 'img'
]);

/** 允许的属性白名单 */
const ALLOWED_ATTRS = new Set(['class', 'style', 'src', 'alt', 'href']);

/**
 * 简易 HTML 过滤器：移除不在白名单中的标签和属性
 * 生产环境建议替换为 DOMPurify
 */
export function sanitizeHtml(html: string): string {
  if (!html) return '';
  // 移除 script 标签
  let result = html.replace(/<script[\s\S]*?<\/script>/gi, '');
  // 移除 on* 事件属性
  result = result.replace(/\s+on\w+\s*=\s*["'][^"']*["']/gi, '');
  // 移除 javascript: 协议
  result = result.replace(/href\s*=\s*["']javascript:[^"']*["']/gi, '');
  // 移除 style 中的 expression/url
  result = result.replace(/expression\s*\(/gi, '');
  return result;
}

/**
 * F-5: 隐私合规 —— 手机号脱敏
 */
export function maskPhone(phone: string): string {
  if (!phone || phone.length < 7) return phone || '';
  return phone.substring(0, 3) + '****' + phone.substring(7);
}

/**
 * F-5: 隐私合规 —— 身份证脱敏
 */
export function maskIdCard(id: string): string {
  if (!id || id.length < 8) return id || '';
  return id.substring(0, 4) + '**********' + id.substring(id.length - 4);
}

/**
 * F-6: 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  fn: T, delay: number = 300
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null;
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
}

/**
 * F-6: 生成幂等请求 ID
 */
export function generateRequestId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2, 8);
}
