<template>
  <view class="page-container">
    <scroll-view scroll-y class="main-scroll">
      <!-- 账号信息 -->
      <view class="section-title">账号</view>
      <view class="card group">
        <view class="row" hover-class="row-active" @tap="goAccount">
          <text class="label">账号信息</text>
          <view class="right">
            <text class="value">{{ userStore.userInfo?.phone || '未绑定' }}</text>
            <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
          </view>
        </view>
        <view class="divider"></view>
        <view class="row" hover-class="row-active" @tap="goAddress">
          <text class="label">收货地址管理</text>
          <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
        </view>
        <view class="divider"></view>
        <view class="row" hover-class="row-active" @tap="goPickup">
          <text class="label">自提点</text>
          <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
        </view>
      </view>

      <!-- 通知 & 隐私 -->
      <view class="section-title">通知与隐私</view>
      <view class="card group">
        <view class="row">
          <text class="label">订单消息推送</text>
          <switch :checked="settings.orderPush" @change="onSwitch('orderPush', $event)" color="#2E7D32" />
        </view>
        <view class="divider"></view>
        <view class="row">
          <text class="label">优惠活动推送</text>
          <switch :checked="settings.promoPush" @change="onSwitch('promoPush', $event)" color="#2E7D32" />
        </view>
        <view class="divider"></view>
        <view class="row">
          <text class="label">个性化推荐</text>
          <switch :checked="settings.personalRec" @change="onSwitch('personalRec', $event)" color="#2E7D32" />
        </view>
      </view>

      <!-- 通用 -->
      <view class="section-title">通用</view>
      <view class="card group">
        <view class="row" hover-class="row-active" @tap="clearCache">
          <text class="label">清除缓存</text>
          <view class="right">
            <text class="value">{{ cacheSize }}</text>
            <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
          </view>
        </view>
        <view class="divider"></view>
        <view class="row" hover-class="row-active" @tap="goAbout">
          <text class="label">关于鲜果记</text>
          <view class="right">
            <text class="value">v1.0.0</text>
            <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
          </view>
        </view>
        <view class="divider"></view>
        <view class="row" hover-class="row-active" @tap="goFeedback">
          <text class="label">意见反馈</text>
          <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-box" v-if="userStore.isLogin">
        <button class="logout-btn" @tap="handleLogout">退出登录</button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useUserStore } from '@/stores/user';
import SvgIcon from '@/components/svg-icon.vue';

const userStore = useUserStore();

const settings = reactive({
  orderPush: true,
  promoPush: true,
  personalRec: true
});

const cacheSize = ref('2.3MB');

function onSwitch(key: keyof typeof settings, e: any) {
  settings[key] = e.detail.value;
}

function goAccount() {
  uni.showToast({ title: '账号详情见个人中心', icon: 'none' });
}

function goAddress() {
  uni.navigateTo({ url: '/pagesC/address/index' });
}

function goPickup() {
  uni.navigateTo({ url: '/pagesC/pickup-point/index' });
}

function goFeedback() {
  uni.navigateTo({ url: '/pagesC/feedback/index' });
}

function goAbout() {
  uni.showModal({
    title: '关于鲜果记',
    content: '鲜果记 v1.0.0\n精品果园 · 产地直采\n\n© 2024 鲜果记团队',
    showCancel: false,
    confirmText: '知道了'
  });
}

function clearCache() {
  uni.showModal({
    title: '清除缓存',
    content: `将清除约 ${cacheSize.value} 缓存数据，是否继续？`,
    success: (res) => {
      if (res.confirm) {
        cacheSize.value = '0KB';
        uni.showToast({ title: '已清除', icon: 'success' });
      }
    }
  });
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout();
        uni.showToast({ title: '已退出登录', icon: 'success' });
        setTimeout(() => uni.navigateBack(), 800);
      }
    }
  });
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.main-scroll {
  flex: 1;
  padding: $space-3 $space-4 $space-5;
}

.section-title {
  font-size: $font-sm;
  color: $color-text-secondary;
  margin: $space-4 $space-2 $space-2;
}

.card.group {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: 0 $space-4;
  box-shadow: $shadow-card;
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-4 0;
  min-height: 96rpx;

  .label {
    font-size: $font-base;
    color: $color-text-primary;
  }

  .right {
    display: flex;
    align-items: center;
    gap: $space-1;

    .value {
      font-size: $font-sm;
      color: $color-text-secondary;
    }
  }
}

.row-active {
  background-color: $color-primary-bg;
}

.divider {
  height: 2rpx;
  background-color: $color-divider;
}

.logout-box {
  padding: $space-5 0 $space-6;

  .logout-btn {
    width: 100%;
    height: 96rpx;
    background-color: $color-bg-card;
    color: $color-price;
    border-radius: $radius-pill;
    font-size: $font-base;
    border: 2rpx solid rgba($color-price, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;

    &::after { border: none; }
  }
}
</style>
