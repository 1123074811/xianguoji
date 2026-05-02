import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<any>(uni.getStorageSync('userInfo') || null);
  const token = ref<string>(uni.getStorageSync('token') || '');

  const isLogin = computed(() => !!token.value);

  function setUserInfo(info: any) {
    userInfo.value = info;
    uni.setStorageSync('userInfo', info);
  }

  function setToken(t: string) {
    token.value = t;
    uni.setStorageSync('token', t);
  }

  function logout() {
    userInfo.value = null;
    token.value = '';
    uni.removeStorageSync('token');
    uni.removeStorageSync('userInfo');
  }

  return {
    userInfo,
    token,
    isLogin,
    setUserInfo,
    setToken,
    logout,
  };
});
