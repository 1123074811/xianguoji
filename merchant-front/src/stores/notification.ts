import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminShopApi } from '@/api/modules/shop'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function refreshUnreadCount() {
    unreadCount.value = await adminShopApi.notificationUnreadCount()
  }

  function incrementUnreadCount() {
    unreadCount.value += 1
  }

  function decrementUnreadCount() {
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  function clearUnreadCount() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    refreshUnreadCount,
    incrementUnreadCount,
    decrementUnreadCount,
    clearUnreadCount,
  }
})
