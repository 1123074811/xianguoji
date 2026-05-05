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
          <swiper-item v-for="(img, index) in displayImages" :key="index">
            <image :src="img" mode="aspectFill" class="slide-image" />
          </swiper-item>
        </swiper>
        <view class="indicator">{{ displayImages.length > 0 ? 1 : 0 }}/{{ displayImages.length }}</view>
      </view>

      <!-- Price & Buy Tabs -->
      <view class="price-section card-flat" v-if="goods">
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
            <text class="price">{{ activeSku ? activeSku.price : goods.minPrice }}</text>
            <text class="original-price" v-if="activeSku && activeSku.originalPrice">¥{{ activeSku.originalPrice }}</text>
          </view>
          <text class="sales">月销 {{ goods.sales }}+</text>
        </view>
      </view>

      <!-- Title & Slogan -->
      <view class="info-section card-flat" v-if="goods">
        <text class="goods-title">{{ goods.name }}</text>
        <text class="slogan">{{ goods.subtitle }}</text>
      </view>

      <!-- Specs -->
      <view class="specs-section card-flat" v-if="goods && goods.skuList.length">
        <text class="section-title">规格选择</text>
        <view class="specs-list">
          <view 
            v-for="sku in goods.skuList" 
            :key="sku.id" 
            class="spec-item"
            :class="{ active: activeSkuId === sku.id }"
            @tap="activeSkuId = sku.id"
          >
            {{ sku.specName }}
          </view>
        </view>
      </view>

      <!-- Discount Bar -->
      <view class="discount-bar" v-if="promotionTip">
        <view class="bar-content">
          <view class="left">
            <svg-icon name="star" :size="36" color="#2E7D32" />
            <text class="text">{{ promotionTip }}</text>
          </view>
          <view class="right" @tap="goToHome">
            去凑单 <svg-icon name="chevron-right" :size="24" color="#2E7D32" />
          </view>
        </view>
      </view>

      <!-- Delivery Info -->
      <view class="delivery-section card-flat">
        <view class="info-item" v-if="goods && goods.supportDelivery">
          <svg-icon name="shipping" :size="40" color="#2E7D32" />
          <view class="content">
            <text class="label">同城配送</text>
            <text class="desc">满{{ deliverySetting?.freeAmount || '39' }}元免配送费</text>
          </view>
        </view>
        <view class="info-item" v-if="goods && goods.supportPickup">
          <svg-icon name="home" :size="40" color="#2E7D32" />
          <view class="content">
            <text class="label">支持自提</text>
            <text class="desc">可在鲜果记线下果园直营店取货</text>
          </view>
        </view>
      </view>

      <!-- User Reviews -->
      <view class="reviews-section" v-if="reviewSummary">
        <view class="reviews-header">
          <text class="reviews-title">用户评价 ({{ reviewSummary.totalCount }})</text>
          <view class="reviews-score">
            <text class="score-num">{{ reviewSummary.avgRating }}</text>
            <svg-icon name="star" :size="24" color="#FFA000" />
            <text class="satisfaction">满意度 {{ reviewSummary.totalCount ? Math.round(reviewSummary.goodCount / reviewSummary.totalCount * 100) : 0 }}%</text>
          </view>
        </view>
        <view class="review-list">
          <view v-for="review in reviews" :key="review.id" class="review-card">
            <view class="review-top">
              <view class="user-info">
                <image v-if="review.userAvatar" :src="resolveImageUrl(review.userAvatar)" class="avatar-img" />
                <view v-else class="avatar">{{ review.userName?.charAt(0) || '?' }}</view>
                <text class="username">{{ review.userName }}</text>
              </view>
              <text class="review-time">{{ review.createdAt }}</text>
            </view>
            <text class="review-content">{{ review.content }}</text>
          </view>
        </view>
      </view>

      <!-- Product Details -->
      <view class="product-detail-section">
        <view class="detail-title-bar">
          <view class="accent-bar"></view>
          <text class="detail-title">产品详情</text>
        </view>
        <view class="detail-content" v-if="goods">
          <text class="detail-text">{{ goods.description }}</text>
          <template v-if="goods.detailImages.length">
            <image v-for="(img, idx) in goods.detailImages" :key="idx" class="detail-img" :src="resolveImageUrl(img)" mode="widthFix" />
          </template>
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
          <svg-icon name="shopping_cart" :size="40" color="#757575" />
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
import { ref, computed, onMounted } from 'vue';
import { useCartStore } from '@/stores/cart';
import { catalogApi } from '@/api/modules/catalog';
import { reviewApi } from '@/api/modules/review';
import { shopApi } from '@/api/modules/shop';
import { userApi } from '@/api/modules/user';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import type { ProductDetailVO } from '@/api/types/catalog';
import type { ReviewVO, ReviewSummaryVO } from '@/api/types/review';
import type { DeliverySettingVO } from '@/api/types/shop';

const cartStore = useCartStore();

const productId = ref(0);
const goods = ref<ProductDetailVO | null>(null);
const reviewSummary = ref<ReviewSummaryVO | null>(null);
const reviews = ref<ReviewVO[]>([]);
const deliverySetting = ref<DeliverySettingVO | null>(null);
const promotionTip = ref('');

const activeSkuId = ref(0);

const activeSku = computed(() => {
  if (!goods.value) return null;
  return goods.value.skuList.find(s => s.id === activeSkuId.value) || goods.value.skuList.find(s => s.isDefault === 1) || goods.value.skuList[0];
});

const displayImages = computed(() => {
  if (!goods.value) return [];
  const raw = goods.value.carouselImages.length > 0 ? goods.value.carouselImages : [goods.value.mainImage];
  return raw.map(resolveImageUrl);
});

async function loadDetail() {
  try {
    const detail = await catalogApi.productDetail(productId.value);
    goods.value = detail;
    // 设置默认SKU
    const defaultSku = detail.skuList.find(s => s.isDefault === 1) || detail.skuList[0];
    if (defaultSku) activeSkuId.value = defaultSku.id;

    // 并行加载评价、配送设置
    const [summary, reviewList, ds] = await Promise.all([
      reviewApi.summary(productId.value).catch(() => null),
      reviewApi.productReviews(productId.value, { size: 3 }).catch(() => null),
      shopApi.deliverySetting().catch(() => null),
    ]);
    if (summary) reviewSummary.value = summary;
    if (reviewList) reviews.value = reviewList.list;
    if (ds) deliverySetting.value = ds;

    // 生成满减提示：基于默认SKU价格和满减规则
    if (ds && ds.freeAmount && activeSku.value) {
      const price = Number(activeSku.value.price);
      const freeAmt = Number(ds.freeAmount);
      if (price < freeAmt) {
        promotionTip.value = `再购¥${(freeAmt - price).toFixed(0)}可享满减优惠`;
      }
    }
  } catch (e) {
    console.warn('加载商品详情失败', e);
  }
}

onMounted(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  productId.value = Number(page?.options?.id || page?.options?.productId || 0);
  if (productId.value) {
    loadDetail();
    recordFootprint();
  }
});

async function recordFootprint() {
  try {
    await userApi.addFootprint(productId.value);
  } catch {
    // 未登录时静默忽略
  }
}

function goBack() {
  uni.navigateBack();
}

function goToHome() {
  uni.switchTab({ url: '/pages/index/index' });
}

function goToCart() {
  uni.switchTab({ url: '/pages/cart/cart' });
}

async function handleAddToCart() {
  if (!activeSku.value) {
    return uni.showToast({ title: '请选择规格', icon: 'none' });
  }
  try {
    await cartStore.addToCart(activeSku.value.id, 1);
    uni.showToast({ title: '已加入购物车', icon: 'success' });
  } catch (e) {
    console.warn('加车失败', e);
  }
}

async function handleBuyNow() {
  if (!activeSku.value) {
    return uni.showToast({ title: '请选择规格', icon: 'none' });
  }
  try {
    await cartStore.addToCart(activeSku.value.id, 1);
    uni.navigateTo({ url: '/pagesB/checkout/index' });
  } catch (e) {
    console.warn('立即购买失败', e);
  }
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
  // 微信小程序不支持 aspect-ratio，用 padding-bottom 撑 4:3
  height: 0;
  padding-bottom: 75%;
  position: relative;
  background-color: #ffffff;

  .swiper {
    position: absolute;
    top: 0;
    left: 0;
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

.reviews-section {
  padding: $space-4;

  .reviews-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-3;

    .reviews-title {
      font-size: $font-lg;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }

    .reviews-score {
      display: flex;
      align-items: center;
      gap: 8rpx;
      color: $color-primary;

      .score-num {
        font-weight: bold;
        font-size: $font-md;
      }

      .satisfaction {
        font-size: $font-xs;
        color: $color-text-secondary;
        margin-left: $space-2;
      }
    }
  }

  .review-list {
    display: flex;
    flex-direction: column;
    gap: $space-3;
  }

  .review-card {
    background-color: #ffffff;
    padding: $space-3;
    border-radius: $radius-md;
    box-shadow: $shadow-card;

    .review-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $space-2;

      .user-info {
        display: flex;
        align-items: center;
        gap: $space-2;

        .avatar {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          background-color: $color-bg-page;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          color: $color-primary;
          font-size: $font-md;
        }

        .username {
          font-size: $font-md;
          font-weight: $weight-semibold;
          color: $color-text-primary;
        }
      }

      .review-time {
        font-size: $font-xs;
        color: $color-text-secondary;
      }
    }

    .review-content {
      font-size: $font-sm;
      color: $color-text-primary;
      @include text-ellipsis-2;
    }
  }
}

.product-detail-section {
  margin-top: $space-5;

  .detail-title-bar {
    display: flex;
    align-items: center;
    gap: $space-2;
    padding: 0 $space-4 $space-4;

    .accent-bar {
      width: 8rpx;
      height: 40rpx;
      background-color: $color-primary;
      border-radius: 4rpx;
    }

    .detail-title {
      font-size: $font-lg;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }
  }

  .detail-content {
    .detail-text {
      display: block;
      font-size: $font-base;
      line-height: 1.6;
      color: $color-text-primary;
      padding: $space-4;
      background-color: #ffffff;
    }

    .detail-img {
      width: 100%;
      display: block;
      border-radius: $radius-lg;
      margin-bottom: $space-3;
    }

    .detail-img-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: $space-2;
      padding: 0 $space-3;

      .detail-img-sm {
        width: 100%;
        // 微信小程序不支持 aspect-ratio
        height: 0;
        padding-bottom: 100%;
        border-radius: $radius-lg;
      }
    }

    .detail-quote {
      display: block;
      text-align: center;
      font-style: italic;
      font-size: $font-sm;
      color: $color-text-secondary;
      padding: $space-4;
      background-color: #ffffff;
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
