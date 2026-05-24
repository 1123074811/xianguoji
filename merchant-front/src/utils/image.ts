import { API_BASE_URL } from '@/config/env';

const BASE_URL = API_BASE_URL;

/**
 * 将后端返回的图片路径转为完整 URL
 * - 以 http/https 开头 → 原样返回
 * - 以 /static/ 开头 → 拼接后端 BASE_URL（后端通过 ResourceHandler 提供）
 * - 其他 → 原样返回
 */
export function resolveImageUrl(path: string | undefined | null): string {
  if (!path) return '';
  if (path.startsWith('http://') || path.startsWith('https://')) return path;
  if (path.startsWith('/static/')) return `${BASE_URL}${path}`;
  return path;
}
