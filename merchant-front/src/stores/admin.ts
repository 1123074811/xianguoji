import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { adminAuthApi } from '@/api/modules/auth';
import type { AdminUserInfoVO } from '@/api/types/auth';

const STAFF_ROLE_PERM_MAP: Record<string, string[]> = {
  owner: ['*'],
  admin: ['dashboard', 'orders', 'goods', 'campaign', 'reviews', 'customers', 'shipping', 'messages', 'analysis', 'reports', 'settings'],
  packer: ['orders', 'shipping'],
  courier: ['orders', 'shipping'],
};

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '');
  const staffInfo = ref<AdminUserInfoVO | null>(
    JSON.parse(localStorage.getItem('admin_info') || 'null'),
  );

  const isLogin = computed(() => !!token.value);
  const staffRole = computed(() => staffInfo.value?.staffRole || '');
  const permissions = computed(() => STAFF_ROLE_PERM_MAP[staffRole.value] || []);

  function setToken(t: string) {
    token.value = t;
    localStorage.setItem('admin_token', t);
  }

  function setStaffInfo(info: AdminUserInfoVO) {
    staffInfo.value = info;
    localStorage.setItem('admin_info', JSON.stringify(info));
  }

  function logout() {
    token.value = '';
    staffInfo.value = null;
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_info');
  }

  async function login(username: string, password: string, captchaKey: string, captchaCode: string) {
    const vo = await adminAuthApi.login({ username, password, captchaKey, captchaCode });
    setToken(vo.token);
    setStaffInfo(vo.userInfo);
  }

  async function fetchMe() {
    const info = await adminAuthApi.me();
    setStaffInfo(info);
  }

  function hasPermission(key: string): boolean {
    if (permissions.value.includes('*')) return true;
    return permissions.value.includes(key);
  }

  return {
    token, staffInfo, isLogin, staffRole, permissions,
    setToken, setStaffInfo, logout, login, fetchMe, hasPermission,
  };
});
