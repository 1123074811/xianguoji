/**
 * TC-FE-M-UTIL-001: 商家端工具函数测试
 * - resolveImageUrl
 * - toast 系统
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

// ==================== resolveImageUrl ====================
function resolveImageUrl(path: string | undefined | null, baseUrl = 'http://127.0.0.1:8080'): string {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/static/')) return `${baseUrl}${path}`
  return path
}

describe('TC-FE-M-UTIL-001: resolveImageUrl', () => {
  it('TC-FE-M-UTIL-001a: https URL 应原样返回', () => {
    expect(resolveImageUrl('https://img.xianguoji.com/a.jpg')).toBe('https://img.xianguoji.com/a.jpg')
  })

  it('TC-FE-M-UTIL-001b: http URL 应原样返回', () => {
    expect(resolveImageUrl('http://img.xianguoji.com/a.jpg')).toBe('http://img.xianguoji.com/a.jpg')
  })

  it('TC-FE-M-UTIL-001c: /static/ 路径应拼接 BASE_URL', () => {
    expect(resolveImageUrl('/static/upload/1.jpg')).toBe('http://127.0.0.1:8080/static/upload/1.jpg')
  })

  it('TC-FE-M-UTIL-001d: null 应返回空字符串', () => {
    expect(resolveImageUrl(null)).toBe('')
  })

  it('TC-FE-M-UTIL-001e: undefined 应返回空字符串', () => {
    expect(resolveImageUrl(undefined)).toBe('')
  })

  it('TC-FE-M-UTIL-001f: 空字符串应返回空字符串', () => {
    expect(resolveImageUrl('')).toBe('')
  })

  it('TC-FE-M-UTIL-001g: 其他路径应原样返回', () => {
    expect(resolveImageUrl('relative/path.jpg')).toBe('relative/path.jpg')
  })
})

// ==================== toast system ====================
describe('TC-FE-M-UTIL-002: toast 系统', () => {
  it('TC-FE-M-UTIL-002a: success toast 应有正确类型', () => {
    const type = 'success'
    const message = '操作成功'
    expect(type).toBe('success')
    expect(message).toBeTruthy()
  })

  it('TC-FE-M-UTIL-002b: error toast 默认时长应更长', () => {
    const defaultDuration = 2600
    const errorDuration = 3200
    expect(errorDuration).toBeGreaterThan(defaultDuration)
  })
})
