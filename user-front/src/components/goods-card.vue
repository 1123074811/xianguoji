<template>
  <view class="goods-card" hover-class="btn-active" @tap="handleTap">
    <view class="image-wrapper">
      <image :src="goods.image" mode="aspectFill" class="goods-image" />
      <view v-if="goods.isGroupBuy" class="tag group-buy-tag">拼团</view>
      <view v-if="goods.stock <= 0" class="out-of-stock-mask">已售罄</view>
    </view>
    <view class="info-wrapper">
      <text class="goods-name">{{ goods.name }}</text>
      <view class="price-row">
        <view class="price-box">
          <text class="currency">¥</text>
          <text class="price">{{ goods.isGroupBuy ? goods.groupBuyPrice : goods.price }}</text>
          <text v-if="goods.originalPrice" class="original-price">¥{{ goods.originalPrice }}</text>
        </view>
        <view class="add-btn" @tap.stop="handleAddToCart">
          <svg-icon name="add" :size="24" color="#FFFFFF" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useCartStore } from '@/stores/cart';
import SvgIcon from './svg-icon.vue';

const props = defineProps<{
  goods: any
}>();

const cartStore = useCartStore();

function handleTap() {
  uni.navigateTo({
    url: `/pagesA/goods-detail/index?id=${props.goods.id}`
  });
}

function handleAddToCart() {
  cartStore.addToCart(props.goods);
  uni.showToast({
    title: '已加入购物车',
    icon: 'success',
    duration: 1000
  });
}
</script>

<style lang="scss" scoped>
.goods-card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .image-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 1;
    background-color: $color-bg-page;

    .goods-image {
      width: 100%;
      height: 100%;
    }

    .tag {
      position: absolute;
      top: $space-1;
      left: $space-1;
      padding: 4rpx $space-2;
      border-radius: $radius-sm;
      font-size: $font-xs;
      font-weight: $weight-medium;
      
      &.group-buy-tag {
        background-color: rgba($color-primary, 0.9);
        color: #ffffff;
      }
    }

    .out-of-stock-mask {
      position: absolute;
      inset: 0;
      background-color: rgba(0,0,0,0.4);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: $font-base;
      font-weight: $weight-medium;
    }
  }

  .info-wrapper {
    padding: $space-2;
    display: flex;
    flex-direction: column;
    gap: $space-2;

    .goods-name {
      font-size: $font-base;
      color: $color-text-primary;
      font-weight: $weight-medium;
      @include text-ellipsis-2;
      height: 80rpx;
    }

    .price-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;

      .price-box {
        display: flex;
        align-items: baseline;
        gap: 4rpx;

        .currency {
          font-size: $font-xs;
          color: $color-primary;
          font-weight: $weight-semibold;
        }

        .price {
          font-size: $font-md;
          color: $color-primary;
          font-weight: $weight-semibold;
        }

        .original-price {
          font-size: $font-xs;
          color: $color-text-placeholder;
          text-decoration: line-through;
          margin-left: 4rpx;
        }
      }

      .add-btn {
        width: 48rpx;
        height: 48rpx;
        background-color: $color-primary;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        
        .icon-add {
          font-size: 24rpx;
        }
      }
    }
  }
}
</style>
