import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi } from '@/api/modules/auth';
import { userApi } from '@/api/modules/user';
import type { UserInfoVO } from '@/api/types/auth';
import type { UserProfileVO } from '@/api/types/user';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfoVO | UserProfileVO | null>(uni.getStorageSync('userInfo') || null);
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

  async function smsLogin(phone: string, code: string) {
    const vo = await authApi.smsLogin({ phone, code });
    setToken(vo.token);
    setUserInfo(vo.userInfo);
  }

  async function wechatLogin(code: string) {
    const vo = await authApi.wechatLogin({ code });
    setToken(vo.token);
    setUserInfo(vo.userInfo);
  }

  async function fetchProfile() {
    const profile = await userApi.profile();
    setUserInfo({ ...userInfo.value, ...profile });
  }

  return {
    userInfo,
    token,
    isLogin,
    setUserInfo,
    setToken,
    logout,
    smsLogin,
    wechatLogin,
    fetchProfile,
  };
});
