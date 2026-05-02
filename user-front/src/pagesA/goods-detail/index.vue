<template>
  <view class="detail-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="btn" @tap="goBack">
        <svg-icon name="arrow-back" :size="40" color="#2E7D32" />
      </view>
      <text class="title">鲜果记</text>
      <view class="right-btns">
        <view class="btn">
          <svg-icon name="share" :size="40" color="#2E7D32" />
        </view>
        <view class="btn">
          <svg-icon name="chat" :size="40" color="#2E7D32" />
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Image Carousel -->
      <view class="carousel">
        <swiper class="swiper" circular autoplay interval="3000">
          <swiper-item v-for="(img, index) in goods.images" :key="index">
            <image :src="img" mode="aspectFill" class="slide-image" />
          </swiper-item>
        </swiper>
        <view class="indicator">1/5</view>
      </view>

      <!-- Price & Buy Tabs -->
      <view class="price-section card-flat">
        <view class="buy-tabs">
          <view 
            class="tab" 
            :class="{ active: buyType === 'single' }"
            @tap="buyType = 'single'"
          >单独购买</view>
          <view 
            class="tab" 
            :class="{ active: buyType === 'group' }"
            @tap="buyType = 'group'"
          >发起拼团</view>
        </view>
        <view class="price-row">
          <view class="left">
            <text class="currency">¥</text>
            <text class="price">{{ buyType === 'group' ? goods.groupBuyPrice : goods.price }}</text>
            <text class="original-price">¥{{ goods.originalPrice }}</text>
            <view class="save-tag">立省 ¥19</view>
          </view>
          <text class="sales">月销 2.4k+</text>
        </view>
      </view>

      <!-- Title & Slogan -->
      <view class="info-section card-flat">
        <text class="goods-title">{{ goods.name }}</text>
        <text class="slogan">出口级品质 · 颗颗爆汁 · 浓郁玫瑰芬芳 · 产地直达</text>
      </view>

      <!-- Specs -->
      <view class="specs-section card-flat">
        <text class="section-title">规格选择</text>
        <view class="specs-list">
          <view 
            v-for="spec in specs" 
            :key="spec" 
            class="spec-item"
            :class="{ active: activeSpec === spec }"
            @tap="activeSpec = spec"
          >
            {{ spec }}
          </view>
        </view>
      </view>

      <!-- Discount Bar -->
      <view class="discount-bar">
        <view class="bar-content">
          <view class="left">
            <svg-icon name="star" :size="36" color="#2E7D32" />
            <text class="text">满39减5，再买¥12即可享受</text>
          </view>
          <view class="right">
            去凑单 <svg-icon name="chevron-right" :size="24" color="#2E7D32" />
          </view>
        </view>
      </view>

      <!-- Delivery Info -->
      <view class="delivery-section card-flat">
        <view class="info-item">
          <svg-icon name="shipping" :size="40" color="#2E7D32" />
          <view class="content">
            <text class="label">次日送达</text>
            <text class="desc">16:00前下单预计明日上午送达</text>
          </view>
        </view>
        <view class="info-item">
          <svg-icon name="home" :size="40" color="#2E7D32" />
          <view class="content">
            <text class="label">支持自提</text>
            <text class="desc">可在鲜果记线下果园直营店取货</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <view class="bottom-action">
      <view class="nav-btns">
        <view class="nav-item" @tap="goToHome">
          <svg-icon name="home" :size="40" color="#757575" />
          <text class="text">首页</text>
        </view>
        <view class="nav-item" @tap="goToCart">
          <svg-icon name="cart" :size="40" color="#757575" />
          <text class="text">购物车</text>
          <view v-if="cartStore.totalCount > 0" class="badge">{{ cartStore.totalCount }}</view>
        </view>
      </view>
      <view class="action-btns">
        <button class="add-cart" @tap="handleAddToCart">加入购物车</button>
        <button class="buy-now" @tap="handleBuyNow">立即购买</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useCartStore } from '@/stores/cart';
import SvgIcon from '@/components/svg-icon.vue';

const cartStore = useCartStore();

const goods = ref<any>({
  name: '云南阳光玫瑰青提 (Shine Muscat)',
  price: 59.00,
  groupBuyPrice: 39.90,
  originalPrice: 78.00,
  images: ['https://picsum.photos/750/560?random=30', 'https://picsum.photos/750/560?random=31']
});

const buyType = ref('single');
const specs = ['500g 精装', '1kg 家庭装', '2kg 礼盒装'];
const activeSpec = ref('500g 精装');

function goBack() {
  uni.navigateBack();
}

function goToHome() {
  uni.switchTab({ url: '/pages/index/index' });
}

function goToCart() {
  uni.switchTab({ url: '/pages/cart/cart' });
}

function handleAddToCart() {
  cartStore.addToCart({ ...goods.value, id: 'muscat' });
  uni.showToast({ title: '已加入购物车', icon: 'success' });
}

function handleBuyNow() {
  uni.navigateTo({ url: '/pagesB/checkout/index' });
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

  .btn {
    width: 80rpx;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-primary;
    
    .iconfont {
      font-size: 40rpx;
    }
  }

  .title {
    font-size: $font-lg;
    font-weight: bold;
    color: $color-primary;
  }

  .right-btns {
    display: flex;
    gap: $space-2;
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
}

.carousel {
  width: 100%;
  aspect-ratio: 4/3;
  position: relative;
  background-color: #ffffff;

  .swiper {
    width: 100%;
    height: 100%;
  }

  .slide-image {
    width: 100%;
    height: 100%;
  }

  .indicator {
    position: absolute;
    bottom: $space-4;
    right: $space-4;
    background-color: rgba(0,0,0,0.3);
    color: #ffffff;
    font-size: $font-xs;
    padding: 4rpx $space-3;
    border-radius: $radius-pill;
    backdrop-filter: blur(8rpx);
  }
}

.card-flat {
  background-color: #ffffff;
  padding: $space-4;
  margin-bottom: $space-1;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.price-section {
  .buy-tabs {
    display: flex;
    border-bottom: 2rpx solid $color-divider;
    margin-bottom: $space-4;

    .tab {
      flex: 1;
      padding-bottom: $space-4;
      text-align: center;
      font-size: $font-md;
      color: $color-text-secondary;
      position: relative;
      opacity: 0.5;

      &.active {
        color: $color-primary;
        opacity: 1;
        font-weight: $weight-semibold;
        &::after {
          content: '';
          position: absolute;
          bottom: -2rpx;
          left: 50%;
          transform: translateX(-50%);
          width: 80rpx;
          height: 4rpx;
          background-color: $color-primary;
        }
      }
    }
  }

  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    .left {
      display: flex;
      align-items: baseline;
      gap: 8rpx;

      .currency {
        font-size: $font-sm;
        color: $color-primary;
      }

      .price {
        font-size: 56rpx;
        color: $color-primary;
        font-weight: bold;
      }

      .original-price {
        font-size: $font-sm;
        color: $color-text-placeholder;
        text-decoration: line-through;
      }

      .save-tag {
        background-color: $color-primary-bg;
        color: $color-primary;
        font-size: 20rpx;
        font-weight: bold;
        padding: 2rpx $space-2;
        border-radius: $radius-sm;
      }
    }

    .sales {
      font-size: $font-xs;
      color: $color-text-secondary;
    }
  }
}

.info-section {
  .goods-title {
    font-size: $font-lg;
    font-weight: $weight-semibold;
    color: $color-text-primary;
    margin-bottom: $space-1;
    display: block;
  }

  .slogan {
    font-size: $font-sm;
    color: $color-primary-light;
  }
}

.specs-section {
  .section-title {
    font-size: $font-base;
    font-weight: $weight-semibold;
    margin-bottom: $space-4;
    display: block;
  }

  .specs-list {
    display: flex;
    flex-wrap: wrap;
    gap: $space-3;

    .spec-item {
      padding: $space-2 $space-4;
      border: 2rpx solid $color-divider;
      border-radius: $radius-md;
      font-size: $font-sm;
      color: $color-text-secondary;
      transition: all 0.2s;

      &.active {
        border-color: $color-primary;
        background-color: $color-primary-bg;
        color: $color-primary;
        font-weight: $weight-medium;
      }
    }
  }
}

.discount-bar {
  margin: $space-4;

  .bar-content {
    background-color: rgba($color-primary-bg, 0.5);
    border: 2rpx solid $color-primary-bg;
    padding: $space-3;
    border-radius: $radius-md;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: $space-2;
      color: $color-primary;

      .iconfont { font-size: 36rpx; }
      .text { font-size: $font-sm; font-weight: $weight-medium; }
    }

    .right {
      font-size: $font-sm;
      font-weight: bold;
      color: $color-primary;
      display: flex;
      align-items: center;
    }
  }
}

.delivery-section {
  .info-item {
    display: flex;
    gap: $space-3;
    margin-bottom: $space-4;
    &:last-child { margin-bottom: 0; }

    .iconfont {
      font-size: 40rpx;
      color: $color-primary;
    }

    .content {
      display: flex;
      flex-direction: column;
      gap: 4rpx;

      .label {
        font-size: $font-base;
        font-weight: $weight-semibold;
        color: $color-text-primary;
      }

      .desc {
        font-size: $font-xs;
        color: $color-text-secondary;
      }
    }
  }
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 112rpx;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 $space-4;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  z-index: 100;
  @include safe-area-bottom;

  .nav-btns {
    display: flex;
    gap: $space-4;
    margin-right: $space-4;

    .nav-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4rpx;
      position: relative;

      .iconfont {
        font-size: 40rpx;
        color: $color-text-secondary;
      }

      .text {
        font-size: 18rpx;
        color: $color-text-secondary;
      }

      .badge {
        position: absolute;
        top: -8rpx;
        right: -8rpx;
        background-color: $color-price;
        color: #ffffff;
        font-size: 16rpx;
        min-width: 24rpx;
        height: 24rpx;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 4rpx;
      }
    }
  }

  .action-btns {
    flex: 1;
    display: flex;
    gap: $space-2;

    button {
      flex: 1;
      height: 80rpx;
      border-radius: $radius-pill;
      font-size: $font-base;
      font-weight: $weight-semibold;
      display: flex;
      align-items: center;
      justify-content: center;
      &::after { border: none; }
    }

    .add-cart {
      background-color: $color-bg-page;
      color: $color-text-primary;
      border: 2rpx solid $color-divider;
    }

    .buy-now {
      background-color: $color-primary;
      color: #ffffff;
    }
  }
}
</style>
