import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi } from '@/api/modules/auth';
import { userApi } from '@/api/modules/user';
import type { UserInfoVO } from '@/api/types/auth';
import type { UserProfileVO } from '@/api/types/user';
import { encryptToken, decryptToken } from '@/api/request';

export const useUserStore = defineStore('user', () => {
  // F-3: token 加密存储
  const encryptedToken = uni.getStorageSync('token') || '';
  const token = ref<string>(encryptedToken ? decryptToken(encryptedToken) : '');
  const refreshToken = ref<string>(uni.getStorageSync('refreshToken') ? decryptToken(uni.getStorageSync('refreshToken')) : '');
  const userInfo = ref<UserInfoVO | UserProfileVO | null>(uni.getStorageSync('userInfo') || null);

  const isLogin = computed(() => !!token.value);

  function setUserInfo(info: any) {
    userInfo.value = info;
    uni.setStorageSync('userInfo', info);
  }

  // F-3: token 加密后存储
  function setToken(t: string) {
    token.value = typeof t === 'string' ? t.trim() : '';
    uni.setStorageSync('token', encryptToken(t));
  }

  function setRefreshToken(t: string) {
    refreshToken.value = typeof t === 'string' ? t.trim() : '';
    uni.setStorageSync('refreshToken', encryptToken(t));
  }

  function logout() {
    userInfo.value = null;
    token.value = '';
    refreshToken.value = '';
    uni.removeStorageSync('token');
    uni.removeStorageSync('refreshToken');
    uni.removeStorageSync('userInfo');
  }

  async function smsLogin(phone: string, code: string) {
    const vo = await authApi.smsLogin({ phone, code });
    setToken(vo.token);
    if (vo.refreshToken) setRefreshToken(vo.refreshToken);
    setUserInfo(vo.userInfo);
  }

  async function wechatLogin(jsCode: string, nickname: string, avatar: string) {
    const vo = await authApi.wechatLogin({ jsCode, nickname, avatar });
    setToken(vo.token);
    if (vo.refreshToken) setRefreshToken(vo.refreshToken);
    setUserInfo(vo.userInfo);
  }

  async function wechatQuickLogin(jsCode: string) {
    const vo = await authApi.wechatQuickLogin({ jsCode });
    setToken(vo.token);
    if (vo.refreshToken) setRefreshToken(vo.refreshToken);
    setUserInfo(vo.userInfo);
  }

  async function fetchProfile() {
    const profile = await userApi.profile();
    setUserInfo({ ...userInfo.value, ...profile });
  }

  return {
    userInfo,
    token,
    refreshToken,
    isLogin,
    setUserInfo,
    setToken,
    setRefreshToken,
    logout,
    smsLogin,
    wechatLogin,
    wechatQuickLogin,
    fetchProfile,
  };
});
