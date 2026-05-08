<template>
  <view class="splash-container">
    <!-- Background Artistic Elements -->
    <view class="bg-elements">
      <image class="leaf-top-right" src="/static/images/splash-leaf.png" mode="aspectFit" />
      <view class="organic-shape blur-bg"></view>
      <image class="fruit-bottom-left" src="/static/images/splash-fruit.png" mode="aspectFit" />
      <view class="accent-dot top-left"></view>
      <view class="accent-dot bottom-right"></view>
    </view>

    <!-- Central Identity Cluster -->
    <view class="identity-cluster">
      <view class="logo-wrapper">
        <view class="logo-bg-glow"></view>
        <view class="logo-circle">
          <image class="logo-img" src="/static/images/logo.png" mode="aspectFit" />
        </view>
      </view>
      
      <view class="brand-info">
        <text class="brand-name">鲜果记</text>
        <text class="brand-slogan">新鲜直达，每日精选</text>
      </view>

      <!-- Loading Indicator -->
      <view class="loading-area">
        <view class="progress-bar">
          <view class="progress-inner" :style="{ width: progress + '%' }"></view>
        </view>
        <text class="loading-text">匠心手选 产地直供</text>
      </view>
    </view>

    <!-- Footer Motto -->
    <view class="footer">
      <view class="footer-divider">
        <view class="line"></view>
        <svg-icon name="shipping" :size="28" color="rgba(46, 125, 50, 0.4)" />
        <view class="line"></view>
      </view>
      <text class="footer-text">精品果蔬 产地到家</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';

const progress = ref(0);

onMounted(() => {
  const timer = setInterval(() => {
    if (progress.value >= 100) {
      clearInterval(timer);
      uni.switchTab({
        url: '/pages/index/index'
      });
    } else {
      progress.value += 5;
    }
  }, 30);
});
</script>

<style lang="scss" scoped>
.splash-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.bg-elements {
  position: absolute;
  inset: 0;
  z-index: 0;

  .leaf-top-right {
    position: absolute;
    top: -40rpx;
    right: -40rpx;
    width: 320rpx;
    height: 320rpx;
    opacity: 0.1;
    transform: rotate(45deg);
  }

  .organic-shape {
    position: absolute;
    bottom: -80rpx;
    left: -80rpx;
    width: 400rpx;
    height: 400rpx;
    background-color: rgba($color-primary, 0.05);
    border-radius: 60% 40% 30% 70% / 60% 30% 70% 40%;
    filter: blur(60rpx);
  }

  .fruit-bottom-left {
    position: absolute;
    bottom: 48rpx;
    left: 16rpx;
    width: 200rpx;
    height: 200rpx;
    opacity: 0.2;
    transform: rotate(-12deg);
  }

  .accent-dot {
    position: absolute;
    border-radius: 50%;
    &.top-left {
      top: 25%;
      left: 40rpx;
      width: 12rpx;
      height: 12rpx;
      background-color: rgba($color-primary-light, 0.2);
    }
    &.bottom-right {
      bottom: 33%;
      right: 48rpx;
      width: 8rpx;
      height: 8rpx;
      background-color: rgba($color-primary, 0.2);
    }
  }
}

.identity-cluster {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: $space-3;

  .logo-wrapper {
    position: relative;
    margin-bottom: $space-2;

    .logo-bg-glow {
      position: absolute;
      inset: -16rpx;
      background-color: rgba($color-primary, 0.05);
      border-radius: 50%;
      transform: scale(1.25);
      filter: blur(20rpx);
    }

    .logo-circle {
      width: 192rpx;
      height: 192rpx;
      background-color: $color-bg-card;
      border-radius: 50%;
      box-shadow: $shadow-card;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2rpx solid #ffffff;
      position: relative;
      overflow: hidden;

      .logo-img {
        width: 140rpx;
        height: 140rpx;
      }

      .logo-icon {
        font-size: 96rpx;
        color: $color-primary;
      }
    }
  }

  .brand-info {
    display: flex;
    flex-direction: column;
    gap: $space-1;

    .brand-name {
      font-size: 48rpx;
      font-weight: $weight-semibold;
      color: $color-primary;
      letter-spacing: -1rpx;
    }

    .brand-slogan {
      font-size: $font-lg;
      color: $color-text-secondary;
      letter-spacing: 4rpx;
      font-weight: 300;
      opacity: 0.8;
    }
  }

  .loading-area {
    margin-top: $space-5;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-2;
    width: 384rpx;

    .progress-bar {
      height: 4rpx;
      width: 100%;
      background-color: $color-divider;
      border-radius: 2rpx;
      overflow: hidden;

      .progress-inner {
        height: 100%;
        background-color: $color-primary;
        border-radius: 2rpx;
        transition: width 0.03s linear;
      }
    }

    .loading-text {
      font-size: $font-sm;
      color: $color-text-secondary;
      opacity: 0.6;
      letter-spacing: -0.5rpx;
    }
  }
}

.footer {
  position: absolute;
  bottom: 80rpx;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-1;

  .footer-divider {
    display: flex;
    align-items: center;
    gap: $space-1;
    color: rgba($color-primary, 0.4);

    .line {
      height: 2rpx;
      width: 64rpx;
      background-color: currentColor;
    }

    .icon-shipping {
      font-size: 28rpx;
    }
  }

  .footer-text {
    font-size: $font-sm;
    color: rgba($color-text-secondary, 0.4);
  }
}
</style>
