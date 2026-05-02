<template>
  <view class="detail-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="left">
        <svg-icon name="arrow-back" :size="40" color="#757575" @click="goBack" />
        <text class="title">订单详情</text>
      </view>
      <view class="right">
        <svg-icon name="chat" :size="40" color="#2E7D32" />
        <view style="width: 40rpx;"></view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Status Card -->
      <view class="status-card card">
        <view class="top">
          <view class="status-info">
            <text class="label">订单状态</text>
            <text class="status-text">派送中</text>
          </view>
          <text class="eta">预计 10:45 送达</text>
        </view>
        <!-- Steps -->
        <view class="steps">
          <view class="step-item active">
            <view class="dot"></view>
            <text class="step-label">已下单</text>
          </view>
          <view class="step-item active">
            <view class="dot"></view>
            <text class="step-label">待出库</text>
          </view>
          <view class="step-item current">
            <view class="dot"></view>
            <text class="step-label">派送中</text>
          </view>
          <view class="step-item">
            <view class="dot"></view>
            <text class="step-label">待签收</text>
          </view>
          <view class="line-bg"></view>
          <view class="line-active" style="width: 66%"></view>
        </view>
      </view>

      <!-- Tracking Map -->
      <view class="tracking-card card no-padding overflow-hidden">
        <view class="map-placeholder">
          <image src="https://picsum.photos/750/360?random=40" mode="aspectFill" class="map-img" />
          <view class="rider-tag">
            <view class="dot"></view>
            <text class="text">骑手距离您 1.2km</text>
          </view>
        </view>
        <view class="rider-info">
          <view class="left">
            <image src="https://picsum.photos/96/96?random=41" mode="aspectFill" class="avatar" />
            <view class="info">
              <text class="name">王师傅</text>
              <view class="rating">
                <svg-icon name="star" :size="24" color="#2E7D32" />
                <text class="text">4.9 · 顺丰同城专送</text>
              </view>
            </view>
          </view>
          <view class="btns">
            <view class="btn-circle">
              <svg-icon name="chat" :size="32" color="#2E7D32" />
            </view>
          </view>
        </view>
      </view>

      <!-- Goods List -->
      <view class="goods-card card">
        <view class="goods-list">
          <view v-for="i in 2" :key="i" class="goods-item">
            <image src="https://picsum.photos/160/160?random=42" mode="aspectFill" class="goods-img" />
            <view class="info">
              <view class="top">
                <text class="name">阳山水蜜桃</text>
                <text class="specs">约 250g-300g / 个</text>
              </view>
              <view class="bottom">
                <text class="price">¥18.9</text>
                <text class="count">x 2</text>
              </view>
            </view>
          </view>
        </view>
        <view class="summary">
          <view class="row">
            <text class="label">商品总额</text>
            <text class="value">¥37.8</text>
          </view>
          <view class="row">
            <text class="label">运费</text>
            <text class="value">¥0.00</text>
          </view>
          <view class="row total">
            <text class="label">实付款</text>
            <text class="value">¥37.8</text>
          </view>
        </view>
      </view>

      <!-- Order Info -->
      <view class="info-card card">
        <view class="row">
          <text class="label">订单编号</text>
          <text class="value">202310248812</text>
        </view>
        <view class="row">
          <text class="label">下单时间</text>
          <text class="value">2023-10-24 14:00:25</text>
        </view>
        <view class="row">
          <text class="label">支付方式</text>
          <text class="value">微信支付</text>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Actions -->
    <view class="bottom-bar">
      <button class="action-btn outline">申请售后</button>
      <button class="action-btn primary">确认收货</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/svg-icon.vue';
function goBack() {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
.detail-container {
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

  .left {
    display: flex;
    align-items: center;
    gap: $space-2;
    .iconfont { font-size: 40rpx; color: $color-text-secondary; }
    .title { font-size: $font-base; font-weight: bold; color: $color-primary; }
  }

  .right {
    display: flex;
    gap: $space-4;
    .iconfont { font-size: 40rpx; color: $color-primary; }
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: $space-4;
}

.status-card {
  .top {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: $space-5;

    .status-info {
      display: flex;
      flex-direction: column;
      .label { font-size: $font-xs; color: $color-text-secondary; }
      .status-text { font-size: 40rpx; font-weight: bold; color: $color-primary; }
    }

    .eta { font-size: $font-sm; color: $color-primary; font-weight: $weight-medium; }
  }

  .steps {
    position: relative;
    display: flex;
    justify-content: space-between;
    padding: 0 $space-2;

    .step-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: $space-1;
      z-index: 2;

      .dot {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        background-color: $color-divider;
      }

      .step-label { font-size: 20rpx; color: $color-text-placeholder; }

      &.active {
        .dot { background-color: $color-primary; }
        .step-label { color: $color-primary; font-weight: bold; }
      }

      &.current {
        .dot { 
          width: 20rpx; 
          height: 20rpx; 
          background-color: $color-primary;
          box-shadow: 0 0 0 8rpx rgba($color-primary, 0.2);
        }
        .step-label { color: $color-primary; font-weight: bold; }
      }
    }

    .line-bg {
      position: absolute;
      top: 10rpx;
      left: $space-5;
      right: $space-5;
      height: 2rpx;
      background-color: $color-divider;
      z-index: 1;
    }

    .line-active {
      position: absolute;
      top: 10rpx;
      left: $space-5;
      height: 2rpx;
      background-color: $color-primary;
      z-index: 1;
    }
  }
}

.tracking-card {
  .map-placeholder {
    height: 360rpx;
    position: relative;
    .map-img { width: 100%; height: 100%; }
    .rider-tag {
      position: absolute;
      bottom: $space-3;
      left: $space-3;
      background-color: rgba(255,255,255,0.9);
      padding: 12rpx $space-3;
      border-radius: $radius-pill;
      display: flex;
      align-items: center;
      gap: $space-2;
      box-shadow: $shadow-card;
      
      .dot { width: 16rpx; height: 16rpx; background-color: $color-primary; border-radius: 50%; }
      .text { font-size: 22rpx; font-weight: $weight-medium; }
    }
  }

  .rider-info {
    padding: $space-4;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: $space-3;
      
      .avatar { width: 96rpx; height: 96rpx; border-radius: 50%; }
      .info {
        .name { font-size: $font-base; font-weight: bold; }
        .rating {
          display: flex;
          align-items: center;
          gap: 4rpx;
          .iconfont { font-size: 24rpx; color: $color-primary; }
          .text { font-size: $font-xs; color: $color-text-secondary; }
        }
      }
    }

    .btns {
      display: flex;
      gap: $space-2;
      .btn-circle {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        border: 2rpx solid $color-divider;
        display: flex;
        align-items: center;
        justify-content: center;
        color: $color-primary;
      }
    }
  }
}

.goods-card {
  .goods-list {
    display: flex;
    flex-direction: column;
    gap: $space-4;
    margin-bottom: $space-5;
  }

  .goods-item {
    display: flex;
    gap: $space-3;
    .goods-img { width: 160rpx; height: 160rpx; border-radius: $radius-sm; background-color: $color-bg-page; }
    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      .top {
        .name { font-size: $font-base; font-weight: bold; }
        .specs { font-size: $font-xs; color: $color-text-placeholder; }
      }
      .bottom {
        display: flex;
        justify-content: space-between;
        .price { font-size: $font-base; font-weight: bold; color: $color-primary; }
        .count { font-size: $font-xs; color: $color-text-placeholder; }
      }
    }
  }

  .summary {
    border-top: 2rpx solid rgba($color-divider, 0.3);
    padding-top: $space-4;
    display: flex;
    flex-direction: column;
    gap: $space-2;

    .row {
      display: flex;
      justify-content: space-between;
      .label { font-size: $font-sm; color: $color-text-secondary; }
      .value { font-size: $font-sm; color: $color-text-primary; }
      &.total {
        margin-top: $space-2;
        .label { font-size: $font-base; font-weight: bold; color: $color-text-primary; }
        .value { font-size: $font-lg; font-weight: bold; color: $color-primary; }
      }
    }
  }
}

.info-card {
  .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: $space-3;
    &:last-child { margin-bottom: 0; }
    .label { font-size: $font-sm; color: $color-text-secondary; }
    .value { font-size: $font-sm; color: $color-text-primary; }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 112rpx;
  background-color: #ffffff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 $space-4;
  gap: $space-3;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  z-index: 100;
  @include safe-area-bottom;

  .action-btn {
    height: 80rpx;
    padding: 0 $space-5;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after { border: none; }

    &.outline {
      background-color: transparent;
      border: 2rpx solid $color-divider;
      color: $color-text-secondary;
    }

    &.primary {
      background-color: $color-primary;
      color: #ffffff;
    }
  }
}
</style>
