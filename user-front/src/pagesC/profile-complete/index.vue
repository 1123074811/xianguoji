<template>
  <view class="complete-container">
    <!-- Header -->
    <view class="header">
      <text class="title">完善个人资料</text>
      <text class="subtitle">设置头像和昵称，让大家认识你</text>
    </view>

    <!-- Avatar -->
    <view class="avatar-section">
      <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        <image :src="avatarUrl || '/static/images/default-avatar.png'" mode="aspectFill" class="avatar" />
        <view class="avatar-edit-hint">
          <svg-icon name="edit" :size="28" color="#ffffff" />
        </view>
      </button>
      <text class="hint">点击更换头像</text>
    </view>

    <!-- Nickname & Phone -->
    <view class="form-section">
      <view class="form-item">
        <text class="label">昵称</text>
        <input
          type="nickname"
          class="nickname-input"
          :value="nickname"
          placeholder="请输入昵称"
          @blur="onNicknameBlur"
        />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <template v-if="userStore.userInfo?.phone">
          <text class="phone-value">{{ userStore.userInfo.phone }}</text>
        </template>
        <template v-else>
          <!-- #ifdef MP-WEIXIN -->
          <button class="bind-phone-btn" open-type="getPhoneNumber" @getphonenumber="onGetPhoneNumber">绑定手机号</button>
          <!-- #endif -->
          <!-- #ifndef MP-WEIXIN -->
          <text class="phone-unbound">未绑定</text>
          <!-- #endif -->
        </template>
      </view>
    </view>

    <!-- Actions -->
    <view class="actions">
      <button class="skip-btn" @tap="handleSkip">跳过</button>
      <button class="save-btn" @tap="handleSave">保存</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { userApi } from '@/api/modules/user';
import SvgIcon from '@/components/svg-icon.vue';

const userStore = useUserStore();

const avatarUrl = ref(userStore.userInfo?.avatar || '');
const nickname = ref(userStore.userInfo?.nickname || '');
const avatarChanged = ref(false);

function onChooseAvatar(e: any) {
  const tempUrl = e.detail.avatarUrl;
  if (tempUrl) {
    avatarUrl.value = tempUrl;
    avatarChanged.value = true;
  }
}

function onNicknameBlur(e: any) {
  nickname.value = e.detail.value || nickname.value;
}

async function handleSave() {
  if (!nickname.value.trim()) {
    return uni.showToast({ title: '请输入昵称', icon: 'none' });
  }
  uni.showLoading({ title: '保存中...', mask: true });
  try {
    await userApi.updateProfile({
      nickname: nickname.value.trim(),
      avatar: avatarUrl.value || undefined,
    });
    // 更新本地 store
    userStore.setUserInfo({
      ...userStore.userInfo,
      nickname: nickname.value.trim(),
      avatar: avatarUrl.value || userStore.userInfo?.avatar,
      isNew: false,
    });
    uni.hideLoading();
    uni.showToast({ title: '保存成功', icon: 'success' });
    setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length > 1) uni.navigateBack();
      else uni.switchTab({ url: '/pages/index/index' });
    }, 600);
  } catch (e) {
    uni.hideLoading();
    console.warn('保存资料失败', e);
  }
}

async function onGetPhoneNumber(e: any) {
  const detail = e.detail;
  if (detail.errMsg !== 'getPhoneNumber:ok' || !detail.code) {
    return uni.showToast({ title: '获取手机号失败', icon: 'none' });
  }
  uni.showLoading({ title: '绑定中...', mask: true });
  try {
    // 使用微信新版 getPhoneNumber API 返回的 code，后端直接换取手机号
    await userApi.bindPhone({ code: detail.code });
    // 刷新用户资料
    await userStore.fetchProfile();
    uni.hideLoading();
    uni.showToast({ title: '绑定成功', icon: 'success' });
  } catch (err: any) {
    uni.hideLoading();
    uni.showToast({ title: err?.message || '绑定失败', icon: 'none' });
  }
}

function handleSkip() {
  const pages = getCurrentPages();
  if (pages.length > 1) uni.navigateBack();
  else uni.switchTab({ url: '/pages/index/index' });
}
</script>

<style lang="scss" scoped>
.complete-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf6ec 0%, #f6fbf5 30%, $color-bg-page 100%);
  padding: 0 $space-4 $space-6;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -200rpx;
    right: -160rpx;
    width: 480rpx;
    height: 480rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba($color-primary, 0.18) 0%, rgba($color-primary, 0) 70%);
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    top: 280rpx;
    left: -180rpx;
    width: 420rpx;
    height: 420rpx;
    border-radius: 50%;
    background: radial-gradient(circle, rgba($color-primary, 0.10) 0%, rgba($color-primary, 0) 70%);
    pointer-events: none;
  }
}

.header {
  padding: $space-6 0 $space-4;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;
  position: relative;
  z-index: 1;

  .title {
    font-size: 44rpx;
    font-weight: $weight-semibold;
    color: $color-text-primary;
    letter-spacing: 2rpx;
  }

  .subtitle {
    font-size: $font-sm;
    color: $color-text-secondary;
  }
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-6 0 $space-4;
  position: relative;
  z-index: 1;

  .avatar-btn {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    padding: 0;
    margin: 0;
    border: none;
    background: transparent;
    border-radius: 50%;
    overflow: visible;

    &::after { border: none; }
    &:active .avatar { transform: scale(0.96); }

    .avatar {
      width: 200rpx;
      height: 200rpx;
      border-radius: 50%;
      border: 6rpx solid #ffffff;
      box-shadow: 0 12rpx 32rpx rgba($color-primary, 0.18), 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
      background-color: $color-bg-card;
      transition: transform 0.2s ease;
    }

    .avatar-edit-hint {
      position: absolute;
      bottom: 4rpx;
      right: 4rpx;
      width: 60rpx;
      height: 60rpx;
      background: linear-gradient(135deg, lighten($color-primary, 8%) 0%, $color-primary 100%);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4rpx solid #ffffff;
      box-shadow: 0 4rpx 12rpx rgba($color-primary, 0.3);
    }
  }

  .hint {
    margin-top: $space-3;
    font-size: $font-xs;
    color: $color-text-secondary;
  }
}

.form-section {
  margin-top: $space-5;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 0 $space-5;
  box-shadow: 0 8rpx 28rpx rgba(46, 125, 50, 0.06), 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
  position: relative;
  z-index: 1;

  .form-item {
    display: flex;
    align-items: center;
    min-height: 112rpx;
    padding: $space-3 0;
    border-bottom: 2rpx solid rgba($color-divider, 0.6);

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 120rpx;
      font-size: $font-base;
      color: $color-text-primary;
      font-weight: $weight-medium;
    }

    .nickname-input {
      flex: 1;
      font-size: $font-base;
      color: $color-text-primary;
      height: 64rpx;
    }

    .phone-value {
      flex: 1;
      font-size: $font-base;
      color: $color-text-primary;
      font-weight: $weight-medium;
      letter-spacing: 1rpx;
    }

    .phone-unbound {
      flex: 1;
      font-size: $font-base;
      color: $color-text-placeholder;
    }

    .bind-phone-btn {
      flex: 1;
      height: 72rpx;
      line-height: 72rpx;
      padding: 0 $space-4;
      background: linear-gradient(135deg, lighten($color-primary, 6%) 0%, $color-primary 100%);
      color: #ffffff;
      font-size: $font-sm;
      font-weight: $weight-medium;
      border-radius: $radius-pill;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 6rpx 16rpx rgba($color-primary, 0.25);

      &::after { border: none; }
      &:active { opacity: 0.9; transform: translateY(1rpx); }
    }
  }
}

.actions {
  display: flex;
  gap: $space-3;
  padding: $space-6 0 $space-4;
  position: relative;
  z-index: 1;

  .skip-btn {
    flex: 1;
    height: 96rpx;
    background-color: #ffffff;
    color: $color-text-secondary;
    border-radius: $radius-pill;
    font-size: $font-base;
    border: 2rpx solid rgba($color-divider, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

    &::after { border: none; }
    &:active { background-color: #f7f7f7; }
  }

  .save-btn {
    flex: 2;
    height: 96rpx;
    background: linear-gradient(135deg, lighten($color-primary, 8%) 0%, $color-primary 100%);
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: $weight-semibold;
    letter-spacing: 4rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10rpx 28rpx rgba($color-primary, 0.32);

    &::after { border: none; }
    &:active { opacity: 0.92; transform: translateY(1rpx); }
  }
}
</style>
