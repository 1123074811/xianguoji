<template>
  <view class="checkout-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <svg-icon name="arrow-back" :size="40" color="#757575" @click="goBack" />
      <text class="title">确认订单</text>
      <view style="width: 40rpx;"></view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Delivery Method -->
      <view class="delivery-method">
        <view 
          class="method-card" 
          :class="{ active: deliveryType === 'express' }"
          @tap="deliveryType = 'express'"
        >
          <svg-icon name="shipping" :size="48" :color="deliveryType === 'express' ? '#2E7D32' : '#BDBDBD'" />
          <text class="label">外卖配送</text>
          <text class="desc">最快 30 分钟送达</text>
          <view v-if="deliveryType === 'express'" class="active-tag">已选</view>
        </view>
        <view 
          class="method-card" 
          :class="{ active: deliveryType === 'pickup' }"
          @tap="deliveryType = 'pickup'"
        >
          <svg-icon name="home" :size="48" :color="deliveryType === 'pickup' ? '#2E7D32' : '#BDBDBD'" />
          <text class="label">到店自提</text>
          <text class="desc">离您 1.2km</text>
          <view v-if="deliveryType === 'pickup'" class="active-tag">已选</view>
        </view>
      </view>

      <!-- Address Info -->
      <view class="address-card card">
        <view class="address-info" @tap="goToAddress">
          <view class="left">
            <view class="top">
              <view class="tag">家</view>
              <text class="addr-text">静安区南京西路 1618 号</text>
            </view>
            <text class="user-text">久光百货 5 楼 502 (张先生) 138****8888</text>
          </view>
          <svg-icon name="chevron-right" :size="32" color="#BDBDBD" />
        </view>
        <view class="divider"></view>
        <view class="time-info">
          <view class="left">
            <svg-icon name="star" :size="32" color="#2E7D32" />
            <text class="label">立即送达</text>
          </view>
          <text class="time">预计 14:35 分送达</text>
        </view>
      </view>

      <!-- Goods List -->
      <view class="goods-section card">
        <view class="shop-header">
          <svg-icon name="eco" :size="32" color="#2E7D32" />
          <text class="shop-name">鲜果记 (精品果园店)</text>
        </view>
        <view class="goods-list">
          <view v-for="item in cartStore.items" :key="item.id" class="goods-item">
            <image :src="item.image" mode="aspectFill" class="goods-img" />
            <view class="info">
              <view class="top">
                <text class="name">{{ item.name }}</text>
                <text class="specs">约 250g-300g / 个</text>
              </view>
              <view class="bottom">
                <text class="price">¥{{ item.price }}</text>
                <text class="count">x {{ item.count }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Order Summary -->
      <view class="summary-card card">
        <view class="row">
          <text class="label">商品总额</text>
          <text class="value">¥{{ cartStore.totalPrice }}</text>
        </view>
        <view class="row">
          <text class="label">配送费</text>
          <text class="value">¥0.00</text>
        </view>
        <view class="row">
          <text class="label">优惠券</text>
          <view class="value discount">
            <text>-¥5.00</text>
            <svg-icon name="chevron-right" :size="24" color="#E53935" />
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Bar -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="label">实付款：</text>
        <text class="price">¥{{ finalPrice }}</text>
      </view>
      <button class="submit-btn" @tap="submitOrder">提交订单</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useCartStore } from '@/stores/cart';
import SvgIcon from '@/components/svg-icon.vue';

const cartStore = useCartStore();
const deliveryType = ref('express');

const finalPrice = computed(() => {
  // 简单计算：商品总价 - 优惠券(5元)
  const total = cartStore.totalPrice - 5;
  return total > 0 ? total.toFixed(2) : '0.00';
});

function goBack() {
  uni.navigateBack();
}

function goToAddress() {
  uni.navigateTo({ url: '/pagesC/address/index' });
}

function submitOrder() {
  uni.showLoading({ title: '提交中' });
  setTimeout(() => {
    uni.hideLoading();
    // 清空选中的购物车商品
    cartStore.clearCart();
    uni.redirectTo({ url: '/pagesB/payment-result/index' });
  }, 1000);
}
</script>

<style lang="scss" scoped>
.checkout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav {
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border-bottom: 2rpx solid $color-divider;
  z-index: 100;

  .title {
    font-size: $font-lg;
    font-weight: $weight-semibold;
    color: $color-text-primary;
  }
}

.main-scroll {
  flex: 1;
  padding: $space-3;
  padding-bottom: 140rpx; /* Leave space for bottom bar */
}

.delivery-method {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-3;

  .method-card {
    flex: 1;
    background-color: #ffffff;
    border-radius: $radius-md;
    padding: $space-4;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-1;
    border: 2rpx solid transparent;
    position: relative;
    overflow: hidden;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);

    &.active {
      border-color: $color-primary;
      background-color: rgba($color-primary, 0.02);

      .label {
        color: $color-primary;
        font-weight: $weight-semibold;
      }
    }

    .label {
      font-size: $font-base;
      color: $color-text-primary;
      margin-top: $space-1;
    }

    .desc {
      font-size: $font-xs;
      color: $color-text-secondary;
    }

    .active-tag {
      position: absolute;
      top: 0;
      right: 0;
      background-color: $color-primary;
      color: #ffffff;
      font-size: 18rpx;
      padding: 2rpx 12rpx;
      border-bottom-left-radius: $radius-sm;
    }
  }
}

.card {
  background-color: #ffffff;
  border-radius: $radius-md;
  margin-bottom: $space-3;
  padding: $space-4;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

.address-card {
  display: flex;
  flex-direction: column;
  gap: $space-3;

  .address-info {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      flex-direction: column;
      gap: $space-1;

      .top {
        display: flex;
        align-items: center;
        gap: $space-2;

        .tag {
          background-color: $color-primary-bg;
          color: $color-primary;
          font-size: 20rpx;
          padding: 2rpx 8rpx;
          border-radius: 4rpx;
        }

        .addr-text {
          font-size: $font-md;
          font-weight: $weight-semibold;
          color: $color-text-primary;
        }
      }

      .user-text {
        font-size: $font-sm;
        color: $color-text-secondary;
      }
    }
  }

  .divider {
    height: 2rpx;
    background-color: $color-divider;
  }

  .time-info {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: $space-1;

      .label {
        font-size: $font-base;
        font-weight: $weight-medium;
        color: $color-text-primary;
      }
    }

    .time {
      font-size: $font-sm;
      color: $color-primary;
    }
  }
}

.goods-section {
  padding: 0;

  .shop-header {
    padding: $space-3 $space-4;
    display: flex;
    align-items: center;
    gap: $space-1;
    border-bottom: 2rpx solid $color-divider;

    .shop-name {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }
  }

  .goods-list {
    padding: $space-3 $space-4;
    display: flex;
    flex-direction: column;
    gap: $space-3;

    .goods-item {
      display: flex;
      gap: $space-3;

      .goods-img {
        width: 120rpx;
        height: 120rpx;
        border-radius: $radius-sm;
        background-color: $color-bg-page;
      }

      .info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .top {
          display: flex;
          flex-direction: column;
          gap: 4rpx;

          .name {
            font-size: $font-base;
            color: $color-text-primary;
            @include text-ellipsis;
          }

          .specs {
            font-size: $font-xs;
            color: $color-text-secondary;
          }
        }

        .bottom {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .price {
            font-size: $font-base;
            font-weight: $weight-semibold;
            color: $color-text-primary;
          }

          .count {
            font-size: $font-sm;
            color: $color-text-secondary;
          }
        }
      }
    }
  }
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: $space-2;

  .row {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .label {
      font-size: $font-sm;
      color: $color-text-secondary;
    }

    .value {
      font-size: $font-sm;
      color: $color-text-primary;
      font-weight: $weight-medium;

      &.discount {
        color: $color-price;
        display: flex;
        align-items: center;
      }
    }
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
  justify-content: space-between;
  align-items: center;
  padding: 0 $space-4;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;

  .price-info {
    display: flex;
    align-items: baseline;

    .label {
      font-size: $font-sm;
      color: $color-text-primary;
    }

    .price {
      font-size: 40rpx;
      color: $color-price;
      font-weight: bold;
    }
  }

  .submit-btn {
    width: 240rpx;
    height: 80rpx;
    background-color: $color-primary;
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0;
    
    &::after { border: none; }
  }
}
</style>
    <view class="bottom-bar">
      <view class="left">
        <text class="label">合计：</text>
        <text class="price">¥{{ cartStore.totalPrice - 5 }}</text>
      </view>
      <button class="pay-btn" @tap="handlePay">立即支付</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useCartStore } from '@/stores/cart';
import SvgIcon from '@/components/svg-icon.vue';

const cartStore = useCartStore();
const deliveryType = ref('express');

function goBack() {
  uni.navigateBack();
}

function goToAddress() {
  uni.navigateTo({ url: '/pagesC/address/index' });
}

function handlePay() {
  uni.showLoading({ title: '正在支付...' });
  setTimeout(() => {
    uni.hideLoading();
    uni.navigateTo({ url: '/pagesB/payment-result/index?status=success' });
  }, 1500);
}
</script>

<style lang="scss" scoped>
.checkout-container {
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
    color: $color-text-secondary;
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

.delivery-method {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-3;
  margin-bottom: $space-4;

  .method-card {
    background-color: #ffffff;
    border-radius: $radius-md;
    padding: $space-3;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    border: 2rpx solid transparent;
    box-shadow: $shadow-card;

    &.active {
      border-color: $color-primary;
      .iconfont, .label { color: $color-primary; }
    }

    .iconfont {
      font-size: 48rpx;
      color: $color-text-placeholder;
      margin-bottom: $space-1;
    }

    .label {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
      margin-bottom: 4rpx;
    }

    .desc {
      font-size: $font-xs;
      color: $color-text-placeholder;
    }

    .active-tag {
      position: absolute;
      top: -10rpx;
      right: -10rpx;
      background-color: $color-primary;
      color: #ffffff;
      font-size: 16rpx;
      padding: 2rpx $space-2;
      border-radius: $radius-pill;
    }
  }
}

.address-card {
  .address-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: $space-3;

    .left {
      flex: 1;
      .top {
        display: flex;
        align-items: center;
        gap: $space-2;
        margin-bottom: 4rpx;

        .tag {
          font-size: 18rpx;
          color: $color-primary;
          background-color: $color-primary-bg;
          padding: 2rpx $space-1;
          border-radius: 4rpx;
          font-weight: bold;
        }

        .addr-text {
          font-size: $font-base;
          font-weight: $weight-semibold;
          color: $color-text-primary;
        }
      }

      .user-text {
        font-size: $font-sm;
        color: $color-text-secondary;
      }
    }

    .iconfont {
      font-size: 32rpx;
      color: $color-text-placeholder;
    }
  }

  .divider {
    height: 2rpx;
    background-color: $color-divider;
    margin: 0 - $space-3;
  }

  .time-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $space-3;

    .left {
      display: flex;
      align-items: center;
      gap: $space-2;
      color: $color-primary;

      .iconfont { font-size: 32rpx; }
      .label { font-size: $font-sm; font-weight: $weight-medium; }
    }

    .time {
      font-size: $font-sm;
      color: $color-primary;
      font-weight: bold;
    }
  }
}

.goods-section {
  padding: 0;
  overflow: hidden;

  .shop-header {
    padding: $space-3 $space-4;
    display: flex;
    align-items: center;
    gap: $space-2;
    border-bottom: 2rpx solid rgba($color-divider, 0.3);

    .iconfont { color: $color-primary; font-size: 32rpx; }
    .shop-name { font-size: $font-base; font-weight: $weight-semibold; }
  }

  .goods-list {
    padding: $space-3 $space-4;
    display: flex;
    flex-direction: column;
    gap: $space-4;
  }

  .goods-item {
    display: flex;
    gap: $space-3;

    .goods-img {
      width: 120rpx;
      height: 120rpx;
      border-radius: $radius-sm;
      background-color: $color-bg-page;
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .top {
        .name { font-size: $font-base; color: $color-text-primary; @include text-ellipsis; }
        .specs { font-size: $font-xs; color: $color-text-placeholder; }
      }

      .bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        .price { font-size: $font-base; font-weight: bold; }
        .count { font-size: $font-xs; color: $color-text-placeholder; }
      }
    }
  }
}

.summary-card {
  .row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-3;
    &:last-child { margin-bottom: 0; }

    .label { font-size: $font-sm; color: $color-text-secondary; }
    .value { font-size: $font-sm; color: $color-text-primary; font-weight: $weight-medium; }
    
    .discount {
      display: flex;
      align-items: center;
      color: $color-price;
      .iconfont { font-size: 24rpx; margin-left: 4rpx; }
    }
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
  justify-content: space-between;
  align-items: center;
  padding: 0 $space-4;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  z-index: 100;
  @include safe-area-bottom;

  .left {
    display: flex;
    align-items: baseline;
    gap: 4rpx;
    .label { font-size: $font-sm; color: $color-text-primary; }
    .price { font-size: 40rpx; color: $color-primary; font-weight: bold; }
  }

  .pay-btn {
    width: 280rpx;
    height: 80rpx;
    background-color: $color-primary;
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after { border: none; }
  }
}
</style>
