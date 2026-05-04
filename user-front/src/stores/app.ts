import { defineStore } from 'pinia';
import { ref } from 'vue';
import { shopApi } from '@/api/modules/shop';
import type { ShopVO, DeliverySettingVO } from '@/api/types/shop';

export const useAppStore = defineStore('app', () => {
  const shopInfo = ref<ShopVO | null>(null);
  const deliverySetting = ref<DeliverySettingVO | null>(null);
  const loaded = ref(false);

  async function loadShopInfo() {
    if (loaded.value) return;
    try {
      const [shop, delivery] = await Promise.all([
        shopApi.shopInfo(),
        shopApi.deliverySetting(),
      ]);
      shopInfo.value = shop;
      deliverySetting.value = delivery;
      loaded.value = true;
    } catch (e) {
      console.warn('加载店铺信息失败', e);
    }
  }

  function reset() {
    shopInfo.value = null;
    deliverySetting.value = null;
    loaded.value = false;
  }

  return { shopInfo, deliverySetting, loaded, loadShopInfo, reset };
});
