<template>
  <view class="tab-bar-wrapper">
    <view class="tab-bar-placeholder"></view>
    <view class="tab-bar">
      <view 
        v-for="item in list" 
        :key="item.pagePath" 
        class="tab-item"
        :class="{ active: activePath === item.pagePath }"
        @tap="switchTab(item.pagePath)"
      >
        <view class="icon-box">
          <svg-icon 
            :name="item.icon" 
            :size="48" 
            :color="activePath === item.pagePath ? '#2E7D32' : '#757575'"
          />
          <view v-if="item.badge && item.badge > 0" class="badge">{{ item.badge }}</view>
        </view>
        <text class="label">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import SvgIcon from './svg-icon.vue';
import { useCartStore } from '@/stores/cart';

const props = defineProps<{
  activePath: string
}>();

const cartStore = useCartStore();

const list = computed(() => [
  { pagePath: 'pages/index/index', text: '首页', icon: 'home' },
  { pagePath: 'pages/category/category', text: '分类', icon: 'grid_view' },
  { pagePath: 'pages/cart/cart', text: '购物车', icon: 'shopping_cart', badge: cartStore.totalCount },
  { pagePath: 'pages/order/order', text: '订单', icon: 'receipt_long' },
  { pagePath: 'pages/profile/profile', text: '我的', icon: 'person' }
]);

function switchTab(path: string) {
  if (props.activePath === path) return;
  uni.switchTab({
    url: '/' + path
  });
}
</script>

<style lang="scss" scoped>
.tab-bar-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
}

.tab-bar-placeholder {
  height: calc(128rpx + constant(safe-area-inset-bottom));
  height: calc(128rpx + env(safe-area-inset-bottom));
}

.tab-bar {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  height: calc(128rpx + constant(safe-area-inset-bottom));
  height: calc(128rpx + env(safe-area-inset-bottom));
  display: flex;
  border-top: 2rpx solid rgba(0, 0, 0, 0.05);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.02);

  .tab-item {
    flex: 1;
    height: 128rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6rpx;
    padding: 12rpx 0 8rpx;
    transition: all 0.2s;

    .icon-box {
      position: relative;
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .badge {
        position: absolute;
        top: -8rpx;
        right: -12rpx;
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
        font-weight: bold;
        border: 2rpx solid #ffffff;
      }
    }

    .label {
      font-size: 20rpx;
      color: #757575;
      font-weight: 500;
    }

    &.active {
      transform: scale(1.05);
      .label {
        color: $color-primary;
        font-weight: 600;
      }
    }
  }
}
</style>
