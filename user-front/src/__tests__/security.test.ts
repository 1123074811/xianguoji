/**
 * TC-FE-U-SEC-001: security.ts 工具函数测试
 * - sanitizeHtml (XSS 防御)
 * - maskPhone / maskIdCard (PII 脱敏)
 * - debounce (防抖)
 * - generateRequestId (幂等 ID)
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 直接导入纯函数，无需 mock uni-app
import { sanitizeHtml, maskPhone, maskIdCard, debounce, generateRequestId } from '@/utils/security'

describe('security.ts', () => {
  // ==================== sanitizeHtml ====================
  describe('sanitizeHtml - XSS 防御', () => {
    it('TC-FE-U-SEC-001a: 移除 <script> 标签', () => {
      const input = '<script>alert("xss")</script><p>正常内容</p>'
      const result = sanitizeHtml(input)
      expect(result).not.toContain('<script>')
      expect(result).toContain('<p>正常内容</p>')
    })

    it('TC-FE-U-SEC-001b: 移除 on* 事件属性', () => {
      const input = '<img onerror="alert(1)" src=x>'
      const result = sanitizeHtml(input)
      expect(result).not.toContain('onerror')
    })

    it('TC-FE-U-SEC-001c: 移除 javascript: 协议', () => {
      const input = '<a href="javascript:alert(1)">点击</a>'
      const result = sanitizeHtml(input)
      expect(result).not.toContain('javascript:')
    })

    it('TC-FE-U-SEC-001d: 移除 CSS expression', () => {
      const input = '<div style="width:expression(alert(1))">'
      const result = sanitizeHtml(input)
      expect(result).not.toContain('expression')
    })

    it('TC-FE-U-SEC-001e: 空字符串返回空', () => {
      expect(sanitizeHtml('')).toBe('')
      expect(sanitizeHtml(null as any)).toBe('')
    })

    it('TC-FE-U-SEC-001f: 正常 HTML 应保留', () => {
      const input = '<p>你好</p><strong>鲜果记</strong>'
      const result = sanitizeHtml(input)
      expect(result).toContain('<p>你好</p>')
      expect(result).toContain('<strong>鲜果记</strong>')
    })
  })

  // ==================== maskPhone ====================
  describe('maskPhone - 手机号脱敏', () => {
    it('TC-FE-U-SEC-002a: 11位手机号脱敏为 138****8001', () => {
      expect(maskPhone('13800138001')).toBe('138****8001')
    })

    it('TC-FE-U-SEC-002b: 不同号段脱敏正确', () => {
      expect(maskPhone('15912346789')).toBe('159****6789')
      expect(maskPhone('18612340000')).toBe('186****0000')
    })

    it('TC-FE-U-SEC-002c: 短号码不崩溃', () => {
      expect(maskPhone('138')).toBe('138')
      expect(maskPhone('')).toBe('')
    })

    it('TC-FE-U-SEC-002d: null 返回空字符串', () => {
      expect(maskPhone(null as any)).toBe('')
    })
  })

  // ==================== maskIdCard ====================
  describe('maskIdCard - 身份证脱敏', () => {
    it('TC-FE-U-SEC-003a: 18位身份证脱敏', () => {
      expect(maskIdCard('110101199001011234')).toBe('1101**********1234')
    })

    it('TC-FE-U-SEC-003b: 短字符串不崩溃', () => {
      expect(maskIdCard('1234567')).toBe('1234567')
    })

    it('TC-FE-U-SEC-003c: null 返回空字符串', () => {
      expect(maskIdCard(null as any)).toBe('')
    })
  })

  // ==================== debounce ====================
  describe('debounce - 防抖函数', () => {
    it('TC-FE-U-SEC-004: 多次调用只执行最后一次', async () => {
      vi.useFakeTimers()
      const fn = vi.fn()
      const debounced = debounce(fn, 300)

      debounced()
      debounced()
      debounced()

      expect(fn).not.toHaveBeenCalled()

      vi.advanceTimersByTime(300)
      expect(fn).toHaveBeenCalledTimes(1)

      vi.useRealTimers()
    })
  })

  // ==================== generateRequestId ====================
  describe('generateRequestId - 幂等请求 ID', () => {
    it('TC-FE-U-SEC-005: 每次生成唯一 ID', () => {
      const id1 = generateRequestId()
      const id2 = generateRequestId()
      expect(id1).not.toBe(id2)
    })

    it('TC-FE-U-SEC-005: ID 非空', () => {
      const id = generateRequestId()
      expect(id.length).toBeGreaterThan(0)
    })
  })
})
