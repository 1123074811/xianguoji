<template>
  <view class="profile-container">
    <!-- Top Navigation -->
    <view class="header-nav">
      <text class="brand-name">鲜果记</text>
      <view class="right-btns">
        <svg-icon name="chat" :size="40" color="#2E7D32" @click="handleServiceClick('chat')" />
        <svg-icon name="settings" :size="40" color="#2E7D32" @click="handleServiceClick('settings')" />
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Profile Header -->
      <view class="profile-header">
        <view class="user-info" @tap="goEditProfile">
          <image :src="userStore.isLogin ? (userStore.userInfo?.avatar || '/static/images/default-avatar.png') : '/static/images/default-avatar.png'" mode="aspectFill" class="avatar" />
          <view class="info">
            <template v-if="userStore.isLogin">
              <text class="nickname">{{ userStore.userInfo?.nickname || '用户' }}</text>
              <view class="level-tag">
                <svg-icon name="star" :size="24" color="#2E7D32" />
                <text class="level-text">{{ tagLabel[userStore.userInfo?.tag || 'regular'] || '普通会员' }}</text>
              </view>
            </template>
            <template v-else>
              <view class="login-prompt" @tap="goToLogin">
                <text class="nickname">未登录，点击登录</text>
                <svg-icon name="chevron-right" :size="32" color="#757575" />
              </view>
            </template>
          </view>
        </view>
      </view>

      <!-- Asset Card -->
      <view class="asset-card card">
        <view v-for="asset in assets" :key="asset.id" class="asset-item" hover-class="btn-active" @tap="handleAssetClick(asset.id)">
          <text class="value">{{ asset.value }}</text>
          <text class="label">{{ asset.label }}</text>
        </view>
      </view>

      <!-- My Orders -->
      <view class="order-section card">
        <view class="section-header">
          <text class="title">我的订单</text>
          <view class="more-btn" @tap="goToOrders('all')">
            查看全部订单 <svg-icon name="chevron-right" :size="24" color="#757575" />
          </view>
        </view>
        <view class="order-nav">
          <view v-for="nav in orderNavs" :key="nav.id" class="nav-item" @tap="goToOrders(nav.id)">
            <view class="icon-wrapper">
              <svg-icon :name="nav.icon" :size="48" color="#2E7D32" />
              <view v-if="nav.badge && userStore.isLogin" class="badge">{{ nav.badge }}</view>
            </view>
            <text class="label">{{ nav.label }}</text>
          </view>
        </view>
      </view>

      <!-- Other Services -->
      <view class="service-section card">
        <view class="section-header">
          <text class="title">更多服务</text>
        </view>
        <view class="service-list">
          <view v-for="service in services" :key="service.id" class="service-item" hover-class="btn-active" @tap="handleServiceClick(service.id)">
            <svg-icon :name="service.icon" :size="48" color="#2E7D32" />
            <text class="label">{{ service.label }}</text>
          </view>
        </view>
      </view>

      <view class="logout-btn-box" v-if="userStore.isLogin">
        <button class="logout-btn" @tap="handleLogout">退出登录</button>
      </view>
    </scroll-view>

    <!-- Custom Tab Bar -->
    <custom-tab-bar active-path="pages/profile/profile" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useUserStore } from '@/stores/user';
import { authApi } from '@/api/modules/auth';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';

onShow(async () => {
  uni.hideTabBar();
  if (userStore.isLogin) {
    await userStore.fetchProfile();
  }
});

const userStore = useUserStore();

const tagLabel: Record<string, string> = {
  new: '新客会员',
  regular: '普通会员',
  silent: '沉默会员',
};

const assets = computed(() => {
  const p = userStore.userInfo;
  if (userStore.isLogin && p) {
    return [
      { id: 'coupon', label: '优惠券', value: String(p.couponCount ?? '-') },
      { id: 'groupbuy', label: '我的拼团', value: String(p.groupBuyCount ?? '-') },
      { id: 'favorite', label: '收藏夹', value: String(p.favoriteCount ?? '-') },
      { id: 'footprint', label: '足迹', value: String(p.footprintCount ?? '-') }
    ];
  } else {
    return [
      { id: 'coupon', label: '优惠券', value: '-' },
      { id: 'groupbuy', label: '我的拼团', value: '-' },
      { id: 'favorite', label: '收藏夹', value: '-' },
      { id: 'footprint', label: '足迹', value: '-' }
    ];
  }
});

const orderNavs = [
  { id: 'unpaid', label: '待付款', icon: 'receipt_long' },
  { id: 'toship', label: '待发货', icon: 'shopping_cart' },
  { id: 'toreceive', label: '待收货', icon: 'shipping' },
  { id: 'evaluation', label: '待评价', icon: 'star' },
  { id: 'aftersale', label: '售后', icon: 'arrow-back' }
];

const services = [
  { id: 'address', label: '收货地址', icon: 'location' },
  { id: 'pickup', label: '自提点', icon: 'home' },
  { id: 'chat', label: '在线客服', icon: 'chat' },
  { id: 'feedback', label: '意见反馈', icon: 'edit' }
];

function requireLogin(callback: () => void) {
  if (userStore.isLogin) {
    callback();
  } else {
    uni.navigateTo({ url: '/pages/login/login' });
  }
}

function goToLogin() {
  uni.navigateTo({ url: '/pages/login/login' });
}

function goEditProfile() {
  requireLogin(() => {
    uni.navigateTo({ url: '/pagesC/profile-complete/index' });
  });
}

function goToOrders(id: string) {
  requireLogin(() => {
    if (id === 'evaluation') {
      uni.navigateTo({ url: '/pagesC/evaluation/index' });
    } else {
      // 通过全局标记把 tab 传给订单页
      uni.$emit('order:setTab', id);
      uni.switchTab({ url: '/pages/order/order' });
    }
  });
}

function handleAssetClick(id: string) {
  requireLogin(() => {
    if (id === 'favorite') {
      uni.navigateTo({ url: '/pagesC/favorite/index' });
    } else if (id === 'footprint') {
      uni.navigateTo({ url: '/pagesC/footprint/index' });
    } else if (id === 'coupon') {
      uni.navigateTo({ url: '/pagesC/coupons/index' });
    } else if (id === 'groupbuy') {
      uni.navigateTo({ url: '/pagesC/group-buy/index' });
    }
  });
}

function handleServiceClick(id: string) {
  if (id === 'chat') {
    requireLogin(() => {
      uni.navigateTo({ url: '/pagesC/chat/index' });
    });
    return;
  }
  requireLogin(() => {
    if (id === 'address') {
      uni.navigateTo({ url: '/pagesC/address/index' });
    } else if (id === 'pickup') {
      uni.navigateTo({ url: '/pagesC/pickup-point/index' });
    } else if (id === 'settings') {
      uni.navigateTo({ url: '/pagesC/settings/index' });
    } else if (id === 'feedback') {
      uni.navigateTo({ url: '/pagesC/feedback/index' });
    }
  });
}

async function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        try { await authApi.logout(); } catch (e) { /* ignore */ }
        userStore.logout();
        uni.showToast({ title: '已退出登录', icon: 'success' });
      }
    }
  });
}
</script>

<style lang="scss" scoped>
.profile-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #E8F5E9 0%, $color-bg-page 30%, $color-bg-page 100%);
  box-sizing: border-box;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
}

.header-nav {
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;

  .brand-name {
    font-size: $font-lg;
    font-weight: bold;
    color: $color-primary;
  }

  .right-btns {
    display: flex;
    gap: $space-4;
    color: $color-primary;
    
    .iconfont {
      font-size: 40rpx;
    }
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: 0 $space-4;
}

.profile-header {
  padding: $space-5 0;

  .user-info {
    display: flex;
    align-items: center;
    gap: $space-4;

    .avatar {
      width: 128rpx;
      height: 128rpx;
      border-radius: 50%;
      border: 4rpx solid #ffffff;
      box-shadow: $shadow-card;
      background-color: $color-bg-card;
    }

    .info {
      display: flex;
      flex-direction: column;
      gap: $space-1;

      .nickname {
        font-size: 40rpx;
        font-weight: $weight-semibold;
        color: $color-text-primary;
      }

      .login-prompt {
        display: flex;
        align-items: center;
        gap: $space-1;
      }

      .level-tag {
        display: flex;
        align-items: center;
        gap: 4rpx;
        background-color: $color-primary-bg;
        padding: 4rpx $space-2;
        border-radius: $radius-pill;
        width: fit-content;

        .icon-star {
          font-size: 24rpx;
          color: $color-primary;
        }

        .level-text {
          font-size: $font-xs;
          color: $color-primary;
          font-weight: $weight-medium;
        }
      }
    }
  }
}

.asset-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  text-align: center;
  padding: $space-4;

  .asset-item {
    display: flex;
    flex-direction: column;
    gap: 4rpx;

    .value {
      font-size: $font-md;
      font-weight: $weight-semibold;
      color: $color-primary;
    }

    .label {
      font-size: $font-xs;
      color: $color-text-secondary;
    }
  }
}

.order-section, .service-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-4;

    .title {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }

    .more-btn {
      display: flex;
      align-items: center;
      gap: 4rpx;
      font-size: $font-xs;
      color: $color-text-secondary;
    }
  }
}

.order-nav {
  display: flex;
  justify-content: space-between;
  padding: 0 $space-1;

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-2;

    .icon-wrapper {
      position: relative;
      
      .iconfont {
        font-size: 48rpx;
        color: $color-primary;
      }

      .badge {
        position: absolute;
        top: -8rpx;
        right: -8rpx;
        background-color: $color-price;
        color: #ffffff;
        font-size: 18rpx;
        min-width: 28rpx;
        height: 28rpx;
        border-radius: 14rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 6rpx;
      }
    }

    .label {
      font-size: $font-xs;
      color: $color-text-primary;
    }
  }
}

.service-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-4 0;

  .service-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-2;
    position: relative;

    .iconfont {
      font-size: 48rpx;
      color: $color-primary;
    }

    .label {
      font-size: $font-xs;
      color: $color-text-primary;
    }
  }
}

.logout-btn-box {
  padding: $space-5 0 80rpx;
  
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
