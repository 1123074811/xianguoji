<template>
  <view class="result-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <svg-icon name="category" :size="40" color="#2E7D32" />
      <text class="title">鲜果记</text>
      <svg-icon name="chat" :size="40" color="#2E7D32" />
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Status Card -->
      <view class="status-card card" :class="status">
        <view class="icon-box">
          <svg-icon :name="status === 'success' ? 'check' : 'close'" :size="80" :color="status === 'success' ? '#2E7D32' : '#E53935'" />
        </view>
        <text class="status-title">{{ status === 'success' ? '支付成功' : '支付失败' }}</text>
        
        <view v-if="status === 'success'" class="price-box">
          <text class="currency">¥</text>
          <text class="price">168.50</text>
        </view>
        <view v-else class="error-box">
          <text class="error-text">原因：账户余额不足或支付平台响应超时</text>
        </view>

        <text class="status-desc">
          {{ status === 'success' ? '感谢您的信任，果园正快马加鞭为您备货' : '检查一下网络，再次与新鲜连接' }}
        </text>

        <view class="btn-group">
          <button class="btn primary" @tap="goToOrderDetail">查看订单</button>
          <button class="btn outline" @tap="goToHome">继续购物</button>
        </view>
      </view>

      <!-- Recommendations -->
      <view class="recommend-section">
        <view class="section-header">
          <text class="title">精选推荐</text>
          <text class="desc">为您推荐更多时令好果</text>
        </view>
        <view class="goods-grid">
          <goods-card 
            v-for="item in recommendations" 
            :key="item.id" 
            :goods="item"
          />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';
import { getMockData } from '@/mock/index';

const status = ref('success');
const recommendations = ref<any[]>([]);

onLoad((options) => {
  if (options && options.status) {
    status.value = options.status;
  }
});

onMounted(async () => {
  const data = await getMockData<any[]>('goods.json');
  recommendations.value = data;
});

function goToHome() {
  uni.switchTab({ url: '/pages/index/index' });
}

function goToOrderDetail() {
  uni.switchTab({ url: '/pages/order/order' });
}
</script>

<style lang="scss" scoped>
.result-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav {
  background-color: #ffffff;
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);

  .iconfont {
    font-size: 40rpx;
    color: $color-primary;
  }

  .title {
    font-size: $font-lg;
    font-weight: bold;
    color: $color-primary;
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: $space-4;
}

.status-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-5;
  text-align: center;
  border: 2rpx solid transparent;

  &.success {
    box-shadow: 0 0 80rpx rgba($color-primary, 0.1);
    border-color: rgba($color-primary, 0.05);
    .icon-box { background-color: rgba($color-primary, 0.1); color: $color-primary; }
  }

  &.failure {
    box-shadow: 0 0 80rpx rgba($color-price, 0.1);
    border-color: rgba($color-price, 0.05);
    .icon-box { background-color: rgba($color-price, 0.1); color: $color-price; }
  }

  .icon-box {
    width: 128rpx;
    height: 128rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $space-3;
    .iconfont { font-size: 80rpx; }
  }

  .status-title {
    font-size: 48rpx;
    font-weight: $weight-semibold;
    color: $color-text-primary;
    margin-bottom: $space-2;
  }

  .price-box {
    display: flex;
    align-items: baseline;
    gap: 4rpx;
    margin-bottom: $space-2;
    .currency { font-size: $font-sm; color: $color-text-primary; font-weight: $weight-medium; }
    .price { font-size: 64rpx; color: $color-text-primary; font-weight: bold; }
  }

  .error-box {
    background-color: rgba($color-price, 0.05);
    padding: $space-2 $space-4;
    border-radius: $radius-md;
    margin-bottom: $space-3;
    .error-text { font-size: $font-sm; color: $color-price; }
  }

  .status-desc {
    font-size: $font-sm;
    color: $color-text-secondary;
    margin-bottom: $space-5;
    max-width: 400rpx;
  }

  .btn-group {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: $space-3;

    .btn {
      width: 100%;
      height: 96rpx;
      border-radius: $radius-pill;
      font-size: $font-base;
      font-weight: $weight-semibold;
      display: flex;
      align-items: center;
      justify-content: center;
      &::after { border: none; }

      &.primary {
        background-color: $color-primary;
        color: #ffffff;
      }

      &.outline {
        background-color: transparent;
        border: 2rpx solid $color-divider;
        color: $color-primary;
      }
    }
  }
}

.recommend-section {
  margin-top: $space-5;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: $space-4;

    .title { font-size: $font-md; font-weight: bold; }
    .desc { font-size: $font-xs; color: $color-text-secondary; }
  }

  .goods-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $space-3;
  }
}
</style>
