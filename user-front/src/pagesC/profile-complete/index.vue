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

    <!-- Nickname -->
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

function handleSkip() {
  const pages = getCurrentPages();
  if (pages.length > 1) uni.navigateBack();
  else uni.switchTab({ url: '/pages/index/index' });
}
</script>

<style lang="scss" scoped>
.complete-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: 0 $space-4;
}

.header {
  padding: $space-6 0 $space-4;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;

  .title {
    font-size: $font-xl;
    font-weight: $weight-semibold;
    color: $color-text-primary;
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
  padding: $space-4 0;

  .avatar-btn {
    position: relative;
    width: 192rpx;
    height: 192rpx;
    padding: 0;
    margin: 0;
    border: none;
    background: transparent;
    border-radius: 50%;
    overflow: visible;

    &::after { border: none; }

    .avatar {
      width: 192rpx;
      height: 192rpx;
      border-radius: 50%;
      border: 4rpx solid #ffffff;
      box-shadow: $shadow-card;
      background-color: $color-bg-card;
    }

    .avatar-edit-hint {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 56rpx;
      height: 56rpx;
      background-color: $color-primary;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4rpx solid #ffffff;
    }
  }

  .hint {
    margin-top: $space-2;
    font-size: $font-xs;
    color: $color-text-secondary;
  }
}

.form-section {
  margin-top: $space-4;
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: 0 $space-4;

  .form-item {
    display: flex;
    align-items: center;
    padding: $space-4 0;
    border-bottom: 2rpx solid $color-divider;

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
    }
  }
}

.actions {
  display: flex;
  gap: $space-3;
  padding: $space-6 0;

  .skip-btn {
    flex: 1;
    height: 96rpx;
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

  .save-btn {
    flex: 2;
    height: 96rpx;
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
</style>
