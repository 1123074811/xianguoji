import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useCartStore = defineStore('cart', () => {
  const items = ref<any[]>([
    {
      id: '1',
      name: '智利进口车厘子 JJJ级 2.5kg 礼盒装',
      price: 288.00,
      image: 'https://images.unsplash.com/photo-1528821128474-27f963b062bf?w=500&q=80',
      count: 1,
      selected: true
    },
    {
      id: '2',
      name: '四川蒲江红心猕猴桃 15枚装',
      price: 39.90,
      image: 'https://images.unsplash.com/photo-1585059895524-72359e06138a?w=500&q=80',
      count: 2,
      selected: true
    }
  ]);

  const totalCount = computed(() => {
    return items.value.reduce((total, item) => total + item.count, 0);
  });

  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => total + item.price * item.count, 0);
  });

  function addToCart(product: any) {
    const existingItem = items.value.find(item => item.id === product.id);
    if (existingItem) {
      existingItem.count++;
    } else {
      items.value.push({ ...product, count: 1 });
    }
  }

  function removeFromCart(productId: string) {
    const index = items.value.findIndex(item => item.id === productId);
    if (index > -1) {
      if (items.value[index].count > 1) {
        items.value[index].count--;
      } else {
        items.value.splice(index, 1);
      }
    }
  }

  function clearCart() {
    items.value = [];
  }

  return {
    items,
    totalCount,
    totalPrice,
    addToCart,
    removeFromCart,
    clearCart,
  };
});
