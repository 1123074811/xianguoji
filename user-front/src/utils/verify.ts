/**
 * F-8: 敏感操作二次验证
 * 在支付、修改地址等敏感操作前要求短信验证码确认
 */

import { authApi } from '@/api/modules/auth';

/**
 * 发送二次验证码
 */
export async function sendVerifyCode(phone: string): Promise<void> {
  await authApi.sendSmsCode({ phone });
}

/**
 * 校验二次验证码
 * @returns true 验证通过
 */
export async function verifyCode(phone: string, code: string): Promise<boolean> {
  // 通过登录接口验证码逻辑间接验证，或后端提供独立校验接口
  try {
    await authApi.smsLogin({ phone, code });
    return true;
  } catch {
    return false;
  }
}

/**
 * F-10: 小程序版本更新检查
 */
export function checkUpdate() {
  // #ifdef MP-WEIXIN
  const updateManager = uni.getUpdateManager();
  updateManager.onCheckForUpdate((res) => {
    if (res.hasUpdate) {
      updateManager.onUpdateReady(() => {
        uni.showModal({
          title: '更新提示',
          content: '新版本已经准备好，是否重启应用？',
          success: (modalRes) => {
            if (modalRes.confirm) {
              updateManager.applyUpdate();
            }
          },
        });
      });
      updateManager.onUpdateFailed(() => {
        uni.showToast({ title: '新版本下载失败，请检查网络', icon: 'none' });
      });
    }
  });
  // #endif
}
