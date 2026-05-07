/**
 * TC-FE-M-PERM-001: 权限指令/hasPermission 测试
 * - 角色权限矩阵
 * - owner 全权限
 * - admin 部分权限
 * - packer/courier 有限权限
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, val: string) => { store[key] = val }),
    removeItem: vi.fn((key: string) => { delete store[key] }),
    clear: vi.fn(() => { store = {} }),
  }
})()
vi.stubGlobal('localStorage', localStorageMock)

// Mock admin auth API
vi.mock('@/api/modules/auth', () => ({
  adminAuthApi: {
    login: vi.fn(),
    me: vi.fn(),
    logout: vi.fn(),
  },
}))

import { useAdminStore } from '@/stores/admin'

describe('TC-FE-M-PERM-001: 权限管理', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorageMock.clear()
    vi.clearAllMocks()
  })

  describe('hasPermission', () => {
    it('TC-FE-M-PERM-001a: owner 应有全部权限', () => {
      const store = useAdminStore()
      store.setToken('test-token')
      store.setStaffInfo({ staffRole: 'owner', name: 'Owner' } as any)

      expect(store.hasPermission('dashboard')).toBe(true)
      expect(store.hasPermission('orders')).toBe(true)
      expect(store.hasPermission('goods')).toBe(true)
      expect(store.hasPermission('settings')).toBe(true)
      expect(store.hasPermission('nonexistent')).toBe(true) // * = all
    })

    it('TC-FE-M-PERM-001b: admin 应有大部分权限', () => {
      const store = useAdminStore()
      store.setToken('test-token')
      store.setStaffInfo({ staffRole: 'admin', name: 'Admin' } as any)

      expect(store.hasPermission('dashboard')).toBe(true)
      expect(store.hasPermission('orders')).toBe(true)
      expect(store.hasPermission('goods')).toBe(true)
      expect(store.hasPermission('campaign')).toBe(true)
    })

    it('TC-FE-M-PERM-001c: packer 应只有 orders + shipping 权限', () => {
      const store = useAdminStore()
      store.setToken('test-token')
      store.setStaffInfo({ staffRole: 'packer', name: 'Packer' } as any)

      expect(store.hasPermission('orders')).toBe(true)
      expect(store.hasPermission('shipping')).toBe(true)
      expect(store.hasPermission('goods')).toBe(false)
      expect(store.hasPermission('dashboard')).toBe(false)
      expect(store.hasPermission('settings')).toBe(false)
    })

    it('TC-FE-M-PERM-001d: courier 应只有 orders + shipping 权限', () => {
      const store = useAdminStore()
      store.setToken('test-token')
      store.setStaffInfo({ staffRole: 'courier', name: 'Courier' } as any)

      expect(store.hasPermission('orders')).toBe(true)
      expect(store.hasPermission('shipping')).toBe(true)
      expect(store.hasPermission('campaign')).toBe(false)
    })

    it('TC-FE-M-PERM-001e: 未知角色应无权限', () => {
      const store = useAdminStore()
      store.setToken('test-token')
      store.setStaffInfo({ staffRole: 'unknown', name: 'Unknown' } as any)

      expect(store.hasPermission('dashboard')).toBe(false)
      expect(store.hasPermission('orders')).toBe(false)
    })
  })

  describe('isLogin', () => {
    it('TC-FE-M-PERM-001f: 有 token 时 isLogin=true', () => {
      const store = useAdminStore()
      store.setToken('valid-token')
      expect(store.isLogin).toBe(true)
    })

    it('TC-FE-M-PERM-001g: 无 token 时 isLogin=false', () => {
      const store = useAdminStore()
      expect(store.isLogin).toBe(false)
    })

    it('TC-FE-M-PERM-001h: logout 应清除 token 和 info', () => {
      const store = useAdminStore()
      store.setToken('valid-token')
      store.setStaffInfo({ staffRole: 'owner', name: 'Owner' } as any)
      store.logout()

      expect(store.isLogin).toBe(false)
      expect(store.staffInfo).toBe(null)
    })
  })
})
