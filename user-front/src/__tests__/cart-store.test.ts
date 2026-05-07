/**
 * TC-FE-U-CART-001: 购物车 store 测试
 * - 增/减/选/合计 / 持久化
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

// Mock uni-app API
vi.stubGlobal('uni', {
  getStorageSync: vi.fn(() => ''),
  setStorageSync: vi.fn(),
  removeStorageSync: vi.fn(),
})

// Mock cart API
vi.mock('@/api/modules/cart', () => ({
  cartApi: {
    list: vi.fn(),
    count: vi.fn(),
    add: vi.fn(),
    updateQuantity: vi.fn(),
    updateSelected: vi.fn(),
    delete: vi.fn(),
    clear: vi.fn(),
  },
}))

import { useCartStore } from '@/stores/cart'
import { cartApi } from '@/api/modules/cart'

describe('useCartStore - TC-FE-U-CART-001', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('TC-FE-U-CART-001a: refreshList 应更新 items 和 count', async () => {
    const mockData = {
      items: [
        { id: 1, skuId: 10, productId: 100, productName: '苹果', quantity: 2, selected: 1, price: '9.90', subtotal: '19.80' },
        { id: 2, skuId: 20, productId: 200, productName: '香蕉', quantity: 1, selected: 0, price: '5.50', subtotal: '5.50' },
      ],
      totalAmount: '25.30',
      discountAmount: '0.00',
      promotionTip: '',
    }
    ;(cartApi.list as any).mockResolvedValue(mockData)

    const store = useCartStore()
    await store.refreshList()

    expect(store.items.length).toBe(2)
    expect(store.count).toBe(3) // 2 + 1
    expect(store.totalAmount).toBe('25.30')
  })

  it('TC-FE-U-CART-001b: selectedItems 应只返回 selected=1 的项', async () => {
    const mockData = {
      items: [
        { id: 1, skuId: 10, quantity: 2, selected: 1, price: '9.90' },
        { id: 2, skuId: 20, quantity: 1, selected: 0, price: '5.50' },
      ],
      totalAmount: '19.80',
      discountAmount: '0.00',
      promotionTip: '',
    }
    ;(cartApi.list as any).mockResolvedValue(mockData)

    const store = useCartStore()
    await store.refreshList()

    expect(store.selectedItems.length).toBe(1)
    expect(store.selectedItems[0].id).toBe(1)
  })

  it('TC-FE-U-CART-001c: addToCart 应调用 API 并刷新计数', async () => {
    ;(cartApi.add as any).mockResolvedValue(undefined)
    ;(cartApi.count as any).mockResolvedValue(3)

    const store = useCartStore()
    await store.addToCart(10, 2)

    expect(cartApi.add).toHaveBeenCalledWith({ skuId: 10, quantity: 2 })
    expect(cartApi.count).toHaveBeenCalled()
    expect(store.count).toBe(3)
  })

  it('TC-FE-U-CART-001d: updateQty 应调用 API 并刷新列表', async () => {
    ;(cartApi.updateQuantity as any).mockResolvedValue(undefined)
    ;(cartApi.list as any).mockResolvedValue({
      items: [], totalAmount: '0.00', discountAmount: '0.00', promotionTip: '',
    })

    const store = useCartStore()
    await store.updateQty(1, 5)

    expect(cartApi.updateQuantity).toHaveBeenCalledWith(1, 5)
    expect(cartApi.list).toHaveBeenCalled()
  })

  it('TC-FE-U-CART-001e: remove 应调用 API 并刷新列表', async () => {
    ;(cartApi.delete as any).mockResolvedValue(undefined)
    ;(cartApi.list as any).mockResolvedValue({
      items: [], totalAmount: '0.00', discountAmount: '0.00', promotionTip: '',
    })

    const store = useCartStore()
    await store.remove(1)

    expect(cartApi.delete).toHaveBeenCalledWith(1)
  })

  it('TC-FE-U-CART-001f: clear 应调用 API 并刷新列表', async () => {
    ;(cartApi.clear as any).mockResolvedValue(undefined)
    ;(cartApi.list as any).mockResolvedValue({
      items: [], totalAmount: '0.00', discountAmount: '0.00', promotionTip: '',
    })

    const store = useCartStore()
    await store.clear()

    expect(cartApi.clear).toHaveBeenCalled()
    expect(store.items.length).toBe(0)
  })

  it('TC-FE-U-CART-001g: setSelected 应调用 API 并刷新列表', async () => {
    ;(cartApi.updateSelected as any).mockResolvedValue(undefined)
    ;(cartApi.list as any).mockResolvedValue({
      items: [], totalAmount: '0.00', discountAmount: '0.00', promotionTip: '',
    })

    const store = useCartStore()
    await store.setSelected([1, 2], 1)

    expect(cartApi.updateSelected).toHaveBeenCalledWith({ ids: [1, 2], selected: 1 })
  })
})
