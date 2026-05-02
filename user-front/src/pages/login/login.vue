<template>
  <view class="login-container">
    <!-- Top Brand Area -->
    <view class="header">
      <view class="logo-box">
        <svg-icon name="eco" :size="72" color="#FFFFFF" />
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

      <!-- Social Login Divider -->
      <view class="divider">
        <view class="line"></view>
        <text class="divider-text">其他登录方式</text>
        <view class="line"></view>
      </view>

      <!-- WeChat Login -->
      <view class="social-login">
        <view class="wechat-btn" hover-class="btn-active" @tap="wechatLogin">
          <image class="wechat-icon" src="/static/images/wechat-logo.png" mode="aspectFit" />
        </view>
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

    <!-- Decorative Background Image -->
    <image class="bg-decoration" src="/static/images/login-bg-fruit.png" mode="aspectFill" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';
import SvgIcon from '@/components/svg-icon.vue';

const phone = ref('');
const code = ref('');
const agreed = ref(false);
const counting = ref(false);
const count = ref(60);

const userStore = useUserStore();

function getCode() {
  if (!phone.value) {
    return uni.showToast({ title: '请输入手机号', icon: 'none' });
  }
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
}

function handleLogin() {
  if (!agreed.value) {
    return uni.showToast({ title: '请先同意协议', icon: 'none' });
  }
  // 模拟登录
  userStore.setToken('mock-token');
  userStore.setUserInfo({ nickname: '鲜果记新用户', avatar: 'https://picsum.photos/160/160?random=100' });
  uni.switchTab({
    url: '/pages/index/index'
  });
}

function wechatLogin() {
  if (!agreed.value) {
    return uni.showToast({ title: '请先同意协议', icon: 'none' });
  }
  // 模拟微信登录
  handleLogin();
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

      .wechat-icon {
        width: 64rpx;
        height: 64rpx;
      }
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
</style>
