import { defineStore } from 'pinia';
import { ref } from 'vue';
import { adminShopApi } from '@/api/modules/shop';

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false);
  const shopOpen = ref(true);

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  }

  async function loadShopOpen() {
    try {
      const shop = await adminShopApi.shopInfo();
      shopOpen.value = shop.isOpen === 1;
    } catch (e) {
      console.warn('加载店铺状态失败', e);
    }
  }

  async function toggleShopOpen() {
    const next = shopOpen.value ? 0 : 1;
    shopOpen.value = !shopOpen.value;
    try {
      await adminShopApi.updateOpenStatus(next);
    } catch (e) {
      console.warn('切换营业状态失败', e);
      shopOpen.value = !shopOpen.value;
      throw e;
    }
  }

  return { sidebarCollapsed, toggleSidebar, shopOpen, loadShopOpen, toggleShopOpen };
});
