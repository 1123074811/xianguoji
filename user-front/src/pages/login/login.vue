<template>
  <view class="login-container">
    <!-- Top Brand Area -->
    <view class="header">
      <view class="logo-box">
        <image class="logo-img" src="/static/images/logo.png" mode="aspectFit" />
      </view>
      <text class="brand-name">鲜果记</text>
      <text class="brand-slogan">精品果园 · 产地直采</text>
    </view>

    <!-- Main Form Content -->
    <view class="main-form">
      <view class="input-group">
        <view class="phone-prefix">+86</view>
        <input 
          class="login-input" 
          type="tel" 
          placeholder="请输入手机号" 
          placeholder-class="placeholder"
          v-model="phone"
        />
      </view>

      <view class="code-group">
        <input 
          class="login-input code-input" 
          type="number" 
          placeholder="验证码" 
          placeholder-class="placeholder"
          v-model="code"
        />
        <button 
          class="get-code-btn" 
          :disabled="counting"
          @tap="getCode"
        >
          {{ counting ? `${count}s` : '获取验证码' }}
        </button>
      </view>

      <button class="login-btn" @tap="handleLogin">立即登录</button>

      <button v-if="enableDemoLogin" class="demo-login-btn" @tap="handleDemoLogin">演示账号一键登录</button>

      <!-- Social Login Divider -->
      <view v-if="showWechatLogin" class="divider">
        <view class="line"></view>
        <text class="divider-text">其他登录方式</text>
        <view class="line"></view>
      </view>

      <!-- WeChat Login -->
      <view v-if="showWechatLogin" class="social-login">
        <!-- #ifdef MP-WEIXIN -->
        <button class="wechat-btn" hover-class="btn-active" :loading="quickLoading" @tap="handleWechatTap">
          <svg-icon name="wechat" :size="64" color="#07C160" />
        </button>
        <!-- #endif -->
        <text class="social-text">微信快捷登录</text>
      </view>
    </view>

    <!-- Footer Agreement -->
    <view class="footer">
      <view class="agreement-box">
        <checkbox 
          class="agreement-checkbox" 
          :checked="agreed" 
          @tap="agreed = !agreed"
          color="#2E7D32"
        />
        <view class="agreement-text">
          登录即代表您已阅读并同意 
          <text class="link">《用户服务协议》</text> 与 
          <text class="link">《隐私权政策》</text>，授权鲜果记使用您的账号信息。
        </view>
      </view>
    </view>

    <!-- #ifdef MP-WEIXIN -->
    <!-- WeChat Authorization Modal -->
    <view v-if="showWechatModal" class="modal-mask" @tap="showWechatModal = false">
      <view class="modal-content" @tap.stop>
        <text class="modal-title">微信授权登录</text>
        <text class="modal-subtitle">授权头像和昵称，完善你的个人信息</text>

        <view class="modal-avatar-section">
          <button class="modal-avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
            <image :src="wxAvatarUrl || '/static/images/default-avatar.png'" mode="aspectFill" class="modal-avatar" />
            <view class="modal-avatar-edit">
              <svg-icon name="edit" :size="24" color="#ffffff" />
            </view>
          </button>
          <text class="modal-avatar-hint">点击选择头像</text>
        </view>

        <view class="modal-nickname-section">
          <text class="modal-label">昵称</text>
          <input
            type="nickname"
            class="modal-nickname-input"
            :value="wxNickname"
            placeholder="请输入昵称"
            @input="onNicknameInput"
          />
        </view>

        <view class="modal-actions">
          <button class="modal-cancel-btn" @tap="showWechatModal = false">取消</button>
          <button class="modal-confirm-btn" :loading="wxLoginLoading" @tap="handleWechatConfirm">允许</button>
        </view>
      </view>
    </view>
    <!-- #endif -->

    <!-- Decorative Background Image -->
    <image class="bg-decoration" src="/static/images/login-bg-fruit.png" mode="aspectFill" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { useCartStore } from '@/stores/cart';
import { authApi } from '@/api/modules/auth';
import SvgIcon from '@/components/svg-icon.vue';

const phone = ref('');
const code = ref('');
const agreed = ref(false);
const counting = ref(false);
const count = ref(60);
const loginLoading = ref(false);
const demoLoginLoading = ref(false);
const showWechatLogin = ref(import.meta.env.VITE_ENABLE_WECHAT_LOGIN === 'true');
const enableDemoLogin = ref(import.meta.env.VITE_ENABLE_DEMO_LOGIN !== 'false');
const demoPhone = import.meta.env.VITE_DEMO_LOGIN_PHONE || '13800000001';
const demoCode = import.meta.env.VITE_DEMO_LOGIN_CODE || '1234';

// 微信授权弹窗状态
const showWechatModal = ref(false);
const wxAvatarUrl = ref('');
const wxNickname = ref('');
const wxLoginLoading = ref(false);
const quickLoading = ref(false);
let cachedJsCode = ''; // 缓存静默登录用的 jsCode，弹窗确认时复用

const userStore = useUserStore();

async function getCode() {
  if (!phone.value) {
    return uni.showToast({ title: '请输入手机号', icon: 'none' });
  }
  if (!/^1\d{10}$/.test(phone.value)) {
    return uni.showToast({ title: '手机号格式不正确', icon: 'none' });
  }
  try {
    await authApi.sendSms({ phone: phone.value });
    counting.value = true;
    const timer = setInterval(() => {
      if (count.value <= 1) {
        clearInterval(timer);
        counting.value = false;
        count.value = 60;
      } else {
        count.value--;
      }
    }, 1000);
  } catch (e) {
    console.warn('发送验证码失败', e);
  }
}

async function handleLogin() {
  if (!agreed.value) {
    return uni.showToast({ title: '请先同意协议', icon: 'none' });
  }
  if (!phone.value || !code.value) {
    return uni.showToast({ title: '请输入手机号和验证码', icon: 'none' });
  }
  if (loginLoading.value) return;
  loginLoading.value = true;
  try {
    await userStore.smsLogin(phone.value, code.value);
    // 登录成功后预加载购物车角标
    await useCartStore().refreshCount();
    uni.switchTab({ url: '/pages/index/index' });
  } catch (e) {
    console.warn('登录失败', e);
  } finally {
    loginLoading.value = false;
  }
}

async function handleDemoLogin() {
  if (!agreed.value) {
    return uni.showToast({ title: '请先同意协议', icon: 'none' });
  }
  if (demoLoginLoading.value) return;
  demoLoginLoading.value = true;
  uni.showLoading({ title: '演示登录中' });
  try {
    phone.value = demoPhone;
    code.value = demoCode;
    await authApi.sendSms({ phone: demoPhone });
    await userStore.smsLogin(demoPhone, demoCode);
    await useCartStore().refreshCount();
    uni.hideLoading();
    uni.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 400);
  } catch (e) {
    uni.hideLoading();
    console.warn('演示登录失败', e);
  } finally {
    demoLoginLoading.value = false;
  }
}

async function handleWechatTap() {
  if (!agreed.value) {
    return uni.showToast({ title: '请先同意协议', icon: 'none' });
  }
  if (quickLoading.value) return;
  quickLoading.value = true;
  try {
    const jsCode = await new Promise<string>((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: (r: any) => r.code ? resolve(r.code) : reject(new Error('无code')),
        fail: reject,
      });
    });
    cachedJsCode = jsCode;
    try {
      await userStore.wechatQuickLogin(jsCode);
      // 已注册用户：直接登录
      await useCartStore().refreshCount();
      uni.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 400);
    } catch (err: any) {
      // 4101 = WX_PROFILE_REQUIRED：新用户需要补全资料
      if (err?.code === 4101) {
        showWechatModal.value = true;
      } else {
        console.warn('微信静默登录失败', err);
        uni.showToast({ title: err?.message || '微信登录失败', icon: 'none' });
        cachedJsCode = '';
      }
    }
  } catch (e) {
    console.warn('wx.login 失败', e);
    uni.showToast({ title: '获取微信授权失败', icon: 'none' });
  } finally {
    quickLoading.value = false;
  }
}

function onChooseAvatar(e: any) {
  wxAvatarUrl.value = e.detail.avatarUrl || '';
}

function onNicknameInput(e: any) {
  wxNickname.value = e.detail.value || '';
}

async function handleWechatConfirm() {
  if (!agreed.value) {
    return uni.showToast({ title: '请先同意协议', icon: 'none' });
  }
  if (!wxNickname.value.trim()) {
    return uni.showToast({ title: '请输入昵称', icon: 'none' });
  }
  wxLoginLoading.value = true;

  try {
    // 1. 上传头像到服务器（如果有临时头像）
    let avatarUrl = '';
    if (wxAvatarUrl.value) {
      try {
        const uploadRes = await new Promise<string>((resolve, reject) => {
          uni.uploadFile({
            url: (import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080') + '/api/pub/file/upload-avatar',
            filePath: wxAvatarUrl.value,
            name: 'file',
            success: (r: any) => {
              if (r.statusCode === 200) {
                const data = JSON.parse(r.data);
                if (data.code === 0) resolve(data.data);
                else reject(data);
              } else {
                reject(r);
              }
            },
            fail: reject,
          });
        });
        avatarUrl = uploadRes;
      } catch (e) {
        console.warn('头像上传失败，使用默认头像', e);
      }
    }

    // 2. 优先复用静默登录已拿到的 jsCode；过期/不存在则重新拿
    let loginRes = cachedJsCode;
    if (!loginRes) {
      loginRes = await new Promise<string>((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: (r: any) => r.code ? resolve(r.code) : reject(new Error('无code')),
          fail: reject,
        });
      });
    }

    // 3. 调用后端登录接口
    await userStore.wechatLogin(loginRes, wxNickname.value.trim(), avatarUrl);
    cachedJsCode = ''; // 用过就丢
    await useCartStore().refreshCount();
    showWechatModal.value = false;
    uni.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 600);
  } catch (e) {
    console.warn('微信登录失败', e);
    uni.showToast({ title: '登录失败，请重试', icon: 'none' });
  } finally {
    wxLoginLoading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
  padding: 0 $space-4;
  position: relative;
  overflow: hidden;
}

.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 180rpx;
  padding-bottom: 120rpx;

  .logo-box {
    width: 160rpx;
    height: 160rpx;
    background-color: $color-primary;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $space-4;
    box-shadow: 0 8rpx 24rpx rgba($color-primary, 0.12);
    overflow: hidden;

    .logo-img {
      width: 120rpx;
      height: 120rpx;
    }

    .logo-icon {
      color: #ffffff;
      font-size: 72rpx;
    }
  }

  .brand-name {
    font-size: 48rpx;
    font-weight: $weight-semibold;
    color: $color-primary;
    letter-spacing: -1rpx;
  }

  .brand-slogan {
    font-size: $font-sm;
    color: $color-text-secondary;
    margin-top: $space-1;
    opacity: 0.8;
  }
}

.main-form {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $space-4;
  width: 100%;
  max-width: 800rpx;
  margin: 0 auto;

  .input-group {
    position: relative;
    display: flex;
    align-items: center;
    background-color: $color-bg-card;
    border: 2rpx solid $color-divider;
    border-radius: $radius-md;
    height: 112rpx;
    padding: 0 $space-3;

    .phone-prefix {
      font-size: $font-base;
      color: $color-text-secondary;
      padding-right: $space-3;
      border-right: 2rpx solid $color-divider;
      margin-right: $space-3;
    }
  }

  .code-group {
    display: flex;
    gap: $space-2;

    .code-input {
      flex: 1;
      background-color: $color-bg-card;
      border: 2rpx solid $color-divider;
      border-radius: $radius-md;
      height: 112rpx;
      padding: 0 $space-3;
    }

    .get-code-btn {
      width: 240rpx;
      height: 112rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: $font-base;
      color: $color-primary;
      background-color: transparent;
      border: 2rpx solid rgba($color-primary, 0.2);
      border-radius: $radius-md;
      
      &::after { border: none; }
      &[disabled] { color: $color-text-placeholder; }
    }
  }

  .login-input {
    flex: 1;
    height: 100%;
    font-size: $font-md;
    color: $color-text-primary;
  }

  .placeholder {
    color: $color-text-placeholder;
  }

  .login-btn {
    width: 100%;
    height: 112rpx;
    background-color: $color-primary;
    color: #ffffff;
    font-size: $font-lg;
    font-weight: $weight-medium;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: $space-2;
    box-shadow: 0 8rpx 32rpx rgba($color-primary, 0.2);
    
    &::after { border: none; }
    &:active { opacity: 0.9; transform: scale(0.98); }
  }

  .demo-login-btn {
    width: 100%;
    height: 96rpx;
    background-color: rgba($color-primary, 0.08);
    color: $color-primary;
    font-size: $font-base;
    font-weight: $weight-medium;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx solid rgba($color-primary, 0.18);

    &::after { border: none; }
    &:active { opacity: 0.9; transform: scale(0.98); }
  }

  .divider {
    display: flex;
    align-items: center;
    padding: $space-5 0;

    .line {
      flex: 1;
      height: 2rpx;
      background-color: $color-divider;
    }

    .divider-text {
      font-size: $font-xs;
      color: $color-text-secondary;
      padding: 0 $space-4;
    }
  }

  .social-login {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-2;

    .wechat-btn {
      width: 112rpx;
      height: 112rpx;
      background-color: #ffffff;
      border: 2rpx solid $color-divider;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4rpx 8rpx rgba(0,0,0,0.04);

    }

    .social-text {
      font-size: $font-xs;
      color: $color-text-secondary;
    }
  }
}

.footer {
  padding-bottom: 80rpx;
  width: 100%;
  max-width: 800rpx;
  margin: 0 auto;

  .agreement-box {
    display: flex;
    gap: $space-2;

    .agreement-checkbox {
      transform: scale(0.7);
      margin-top: 4rpx;
    }

    .agreement-text {
      font-size: $font-xs;
      color: $color-text-secondary;
      line-height: 1.6;

      .link {
        color: $color-primary;
        font-weight: $weight-medium;
      }
    }
  }
}

.bg-decoration {
  position: fixed;
  bottom: 0;
  right: 0;
  z-index: -1;
  width: 512rpx;
  height: 512rpx;
  opacity: 0.1;
  pointer-events: none;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 600rpx;
  background-color: #ffffff;
  border-radius: $radius-lg;
  padding: $space-5;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-3;

  .modal-title {
    font-size: $font-lg;
    font-weight: $weight-semibold;
    color: $color-text-primary;
  }

  .modal-subtitle {
    font-size: $font-sm;
    color: $color-text-secondary;
  }

  .modal-avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-1;
    padding: $space-3 0;

    .modal-avatar-btn {
      position: relative;
      width: 160rpx;
      height: 160rpx;
      padding: 0;
      margin: 0;
      border: none;
      background: transparent;
      border-radius: 50%;
      overflow: visible;

      &::after { border: none; }

      .modal-avatar {
        width: 160rpx;
        height: 160rpx;
        border-radius: 50%;
        border: 4rpx solid #ffffff;
        box-shadow: $shadow-card;
        background-color: $color-bg-card;
      }

      .modal-avatar-edit {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 48rpx;
        height: 48rpx;
        background-color: $color-primary;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 3rpx solid #ffffff;
      }
    }

    .modal-avatar-hint {
      font-size: $font-xs;
      color: $color-text-secondary;
    }
  }

  .modal-nickname-section {
    width: 100%;
    display: flex;
    align-items: center;
    background-color: $color-bg-page;
    border-radius: $radius-md;
    padding: 0 $space-3;
    height: 88rpx;

    .modal-label {
      width: 80rpx;
      font-size: $font-base;
      color: $color-text-primary;
      font-weight: $weight-medium;
    }

    .modal-nickname-input {
      flex: 1;
      font-size: $font-base;
      color: $color-text-primary;
    }
  }

  .modal-actions {
    width: 100%;
    display: flex;
    gap: $space-3;
    margin-top: $space-2;

    .modal-cancel-btn {
      flex: 1;
      height: 88rpx;
      background-color: $color-bg-card;
      color: $color-text-secondary;
      border-radius: $radius-pill;
      font-size: $font-base;
      border: 2rpx solid $color-divider;
      display: flex;
      align-items: center;
      justify-content: center;

      &::after { border: none; }
    }

    .modal-confirm-btn {
      flex: 2;
      height: 88rpx;
      background-color: $color-primary;
      color: #ffffff;
      border-radius: $radius-pill;
      font-size: $font-base;
      font-weight: $weight-medium;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8rpx 24rpx rgba($color-primary, 0.2);

      &::after { border: none; }
      &:active { opacity: 0.9; }
    }
  }
}
</style>
