<template>
  <view class="page-container">
    <view class="tabs">
      <view class="tab-item" :class="{ active: currentTab === 0 }" @tap="currentTab = 0">可领取</view>
      <view class="tab-item" :class="{ active: currentTab === 1 }" @tap="currentTab = 1">已领取</view>
      <view class="tab-item" :class="{ active: currentTab === 2 }" @tap="currentTab = 2">已失效</view>
    </view>

    <scroll-view scroll-y class="coupon-list" v-if="filteredCoupons.length > 0">
      <view class="coupon-card" v-for="coupon in filteredCoupons" :key="coupon.id" :class="{ 'is-disabled': currentTab === 2 }">
        <view class="coupon-left">
          <view class="amount-box">
            <text class="currency">¥</text>
            <text class="amount">{{ coupon.amount }}</text>
          </view>
          <text class="condition">{{ coupon.condition }}</text>
        </view>
        <view class="coupon-middle">
          <text class="title">{{ coupon.title }}</text>
          <text class="time">{{ coupon.time }}</text>
        </view>
        <view class="coupon-right">
          <button v-if="currentTab === 0" class="action-btn" @tap="receiveCoupon(coupon)">领取</button>
          <button v-else-if="currentTab === 1" class="action-btn use-btn" @tap="useCoupon">去使用</button>
          <text v-else class="status-text">已失效</text>
        </view>
      </view>
    </scroll-view>

    <wd-status-tip
      v-else
      image="search"
      tip="暂无相关优惠券"
      class="empty-box"
    >
      <wd-button size="small" @click="goShopping">去逛逛</wd-button>
    </wd-status-tip>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

const currentTab = ref(0);

const mockCoupons = ref([
  { id: 1, amount: '10', condition: '满99元可用', title: '新人专享优惠券', time: '有效期至 2024-12-31', status: 0 },
  { id: 2, amount: '30', condition: '满199元可用', title: '满减神券', time: '有效期至 2024-12-31', status: 0 },
  { id: 3, amount: '5', condition: '无门槛', title: '运费券', time: '有效期至 2024-12-31', status: 1 },
  { id: 4, amount: '50', condition: '满299元可用', title: '节日特惠券', time: '有效期至 2023-12-31', status: 2 },
]);

const filteredCoupons = computed(() => {
  return mockCoupons.value.filter(c => c.status === currentTab.value);
});

function receiveCoupon(coupon: any) {
  uni.showLoading({ title: '领取中' });
  setTimeout(() => {
    uni.hideLoading();
    coupon.status = 1;
    uni.showToast({ title: '领取成功', icon: 'success' });
  }, 500);
}

function useCoupon() {
  uni.switchTab({ url: '/pages/index/index' });
}

function goShopping() {
  uni.switchTab({ url: '/pages/index/index' });
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.tabs {
  display: flex;
  background-color: #ffffff;
  padding: 0 $space-4;
  height: 88rpx;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;

  .tab-item {
    flex: 1;
    text-align: center;
    font-size: $font-base;
    color: $color-text-secondary;
    position: relative;
    height: 100%;
    line-height: 88rpx;

    &.active {
      color: $color-primary;
      font-weight: $weight-semibold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 6rpx;
        background-color: $color-primary;
        border-radius: 3rpx;
      }
    }
  }
}

.coupon-list {
  flex: 1;
  padding: $space-3;
}

.coupon-card {
  display: flex;
  background-color: #ffffff;
  border-radius: $radius-md;
  margin-bottom: $space-3;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  &.is-disabled {
    filter: grayscale(100%);
    opacity: 0.6;
  }

  .coupon-left {
    width: 200rpx;
    background: linear-gradient(135deg, rgba($color-primary, 0.1) 0%, rgba($color-primary, 0.2) 100%);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: $space-3;
    color: $color-primary;

    .amount-box {
      display: flex;
      align-items: baseline;

      .currency {
        font-size: $font-sm;
        font-weight: bold;
      }

      .amount {
        font-size: 60rpx;
        font-weight: bold;
        line-height: 1;
      }
    }

    .condition {
      font-size: $font-xs;
      margin-top: $space-1;
    }
  }

  .coupon-middle {
    flex: 1;
    padding: $space-3;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .title {
      font-size: $font-base;
      font-weight: $weight-medium;
      color: $color-text-primary;
      margin-bottom: $space-2;
    }

    .time {
      font-size: $font-xs;
      color: $color-text-secondary;
    }
  }

  .coupon-right {
    width: 140rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-right: $space-3;

    .action-btn {
      width: 120rpx;
      height: 52rpx;
      line-height: 52rpx;
      background-color: $color-primary;
      color: #ffffff;
      font-size: $font-xs;
      border-radius: 26rpx;
      padding: 0;
      margin: 0;

      &::after {
        border: none;
      }

      &.use-btn {
        background-color: #ffffff;
        color: $color-primary;
        border: 2rpx solid $color-primary;
        line-height: 48rpx;
      }
    }

    .status-text {
      font-size: $font-sm;
      color: $color-text-secondary;
    }
  }
}

.empty-box {
  margin-top: 200rpx;
}
</style>
