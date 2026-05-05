<template>
  <view class="goods-card" hover-class="btn-active" @tap="handleTap">
    <view class="image-wrapper">
      <image :src="imageSrc" mode="aspectFill" class="goods-image" @error="handleImageError" />
      <view v-if="goods.isGroupBuy" class="tag group-buy-tag">拼团</view>
      <view v-if="stock <= 0" class="out-of-stock-mask">已售罄</view>
    </view>
    <view class="info-wrapper">
      <text class="goods-name">{{ goods.name }}</text>
      <view class="price-row">
        <view class="price-box">
          <text class="currency">¥</text>
          <text class="price">{{ price }}</text>
          <text v-if="goods.originalPrice" class="original-price">¥{{ goods.originalPrice }}</text>
        </view>
        <view class="add-btn" @tap.stop="handleAddToCart">
          <svg-icon name="add" :size="24" color="#FFFFFF" />
        </view>
      </view>
    </view>
    <view
      v-if="ball.visible"
      class="fly-ball"
      :style="ballStyle"
    >+</view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref, nextTick } from 'vue';
import { useCartStore } from '@/stores/cart';
import SvgIcon from './svg-icon.vue';
import { resolveImageUrl } from '@/utils/image';

const props = defineProps<{
  goods: any
}>();

const cartStore = useCartStore();
const imageLoadFailed = ref(false);
const fallbackImage = '/static/images/goods/apple.png';
const imageSrc = computed(() => imageLoadFailed.value ? fallbackImage : resolveImageUrl(props.goods.mainImage || props.goods.image) || fallbackImage);
const price = computed(() => props.goods.isGroupBuy ? props.goods.groupBuyPrice : (props.goods.minPrice || props.goods.price || '0.00'));
const stock = computed(() => props.goods.totalStock ?? props.goods.stock ?? 0);

function handleTap() {
  uni.navigateTo({
    url: `/pagesA/goods-detail/index?id=${props.goods.id}`
  });
}

const ball = reactive({ visible: false, x: 0, y: 0, dx: 0, dy: 0, flying: false });
const ballStyle = computed(() => ({
  left: ball.x + 'px',
  top: ball.y + 'px',
  transform: ball.flying
    ? `translate(${ball.dx}px, ${ball.dy}px) scale(0.3)`
    : 'translate(0, 0) scale(1)',
  opacity: ball.flying ? 0.4 : 1,
  transition: ball.flying
    ? 'transform 600ms cubic-bezier(0.55, -0.2, 0.7, 0.4), opacity 600ms ease'
    : 'none',
}));

async function handleAddToCart(e: any) {
  if (!props.goods.defaultSkuId) {
    uni.showToast({ title: '请先选择规格', icon: 'none', duration: 1200 });
    setTimeout(() => {
      uni.navigateTo({ url: `/pagesA/goods-detail/index?id=${props.goods.id}` });
    }, 600);
    return;
  }

  // 取点击位置（视口坐标），优先 changedTouches，回退 detail
  const touch = e?.changedTouches?.[0] || e?.touches?.[0];
  const startX = touch?.clientX ?? e?.detail?.x ?? 0;
  const startY = touch?.clientY ?? e?.detail?.y ?? 0;

  // 终点：底部 tab 栏中央（购物车是5个tab中第3个，正好屏幕中线）
  let endX = startX;
  let endY = startY + 200;
  try {
    const sys = uni.getWindowInfo ? uni.getWindowInfo() : uni.getSystemInfoSync();
    endX = sys.windowWidth / 2;
    endY = sys.windowHeight - 40; // 约 tab 图标中心
  } catch (_) {}

  ball.x = startX - 16;
  ball.y = startY - 16;
  ball.dx = 0;
  ball.dy = 0;
  ball.flying = false;
  ball.visible = true;

  await nextTick();
  // 触发动画
  ball.dx = endX - startX;
  ball.dy = endY - startY;
  ball.flying = true;

  // 并行调用加车
  try {
    await cartStore.addToCart(props.goods.defaultSkuId, 1);
  } catch (err) {
    console.warn('加车失败', err);
    uni.showToast({ title: '加入购物车失败', icon: 'none', duration: 1200 });
  }

  setTimeout(() => {
    ball.visible = false;
    ball.flying = false;
  }, 650);
}

function handleImageError() {
  imageLoadFailed.value = true;
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
    // 微信小程序不支持 aspect-ratio，用 padding-bottom 撑正方形
    height: 0;
    padding-bottom: 100%;
    background-color: $color-bg-page;

    .goods-image {
      position: absolute;
      top: 0;
      left: 0;
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

.fly-ball {
  position: fixed;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: $color-primary;
  color: #ffffff;
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
  text-align: center;
  z-index: 9999;
  pointer-events: none;
  box-shadow: 0 4rpx 12rpx rgba(46, 125, 50, 0.4);
  will-change: transform, opacity;
}
</style>
