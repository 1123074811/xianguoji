import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { cartApi } from '@/api/modules/cart';
import type { CartItemVO, CartListVO } from '@/api/types/cart';

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItemVO[]>([]);
  const totalAmount = ref('0.00');
  const discountAmount = ref('0.00');
  const promotionTip = ref('');
  const count = ref(0);

  const totalCount = computed(() => count.value);
  const totalPrice = computed(() => totalAmount.value);
  const selectedItems = computed(() => items.value.filter(i => i.selected === 1));

  async function refreshList() {
    const data = await cartApi.list();
    items.value = data.items;
    totalAmount.value = data.totalAmount;
    discountAmount.value = data.discountAmount;
    promotionTip.value = data.promotionTip;
    count.value = data.items.reduce((s, i) => s + i.quantity, 0);
  }

  async function refreshCount() {
    count.value = await cartApi.count();
  }

  async function addToCart(skuId: number, quantity = 1) {
    await cartApi.add({ skuId, quantity });
    await refreshCount();
  }

  async function updateQty(id: number, quantity: number) {
    await cartApi.updateQuantity(id, quantity);
    await refreshList();
  }

  async function setSelected(ids: number[], selected: 0 | 1) {
    await cartApi.updateSelected({ ids, selected });
    await refreshList();
  }

  async function remove(id: number) {
    await cartApi.delete(id);
    await refreshList();
  }

  async function clear() {
    await cartApi.clear();
    await refreshList();
  }

  return {
    items, totalAmount, discountAmount, promotionTip, count,
    totalCount, totalPrice, selectedItems,
    refreshList, refreshCount, addToCart, updateQty, setSelected, remove, clear,
  };
});
