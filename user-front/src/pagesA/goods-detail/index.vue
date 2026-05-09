<template>
  <view class="detail-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="btn" @tap="goBack">
        <svg-icon name="arrow-back" :size="40" color="#2E7D32" />
      </view>
      <text class="title">鲜果记</text>
      <view class="right-btns">
        <button class="btn share-btn" open-type="share">
          <svg-icon name="share" :size="40" color="#2E7D32" />
        </button>
        <view class="btn" @tap="goToChat">
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
        <view v-if="canGroupBuy" class="buy-tabs">
          <view
            class="tab"
            :class="{ active: buyType === 'single' }"
            @tap="buyType = 'single'"
          >单独购买</view>
          <view
            v-if="canGroupBuy"
            class="tab"
            :class="{ active: buyType === 'group' }"
            @tap="buyType = 'group'"
          >发起拼团</view>
        </view>
        <view class="price-row">
          <view class="left">
            <text class="currency">¥</text>
            <text class="price">{{ buyType === 'group' && canGroupBuy ? groupActivity?.groupPrice : (activeSku ? activeSku.price : goods.minPrice) }}</text>
            <text class="original-price" v-if="buyType === 'group' && canGroupBuy">¥{{ activeSku?.price || goods.minPrice }}</text>
            <text class="original-price" v-else-if="activeSku && activeSku.originalPrice">¥{{ activeSku.originalPrice }}</text>
          </view>
          <text v-if="buyType === 'group' && canGroupBuy" class="sales">{{ groupActivity?.groupSize }}人成团 · 已拼{{ groupActivity?.totalJoinCount }}件</text>
          <text v-else class="sales">月销 {{ goods.sales }}+</text>
        </view>
        <view v-if="canGroupBuy && buyType === 'group'" class="activity-countdown">
          <svg-icon name="schedule" :size="28" color="#E53935" />
          <countdown-flip :end-time="groupActivity!.endTime" />
          <text class="activity-time">{{ formatActivityTime(groupActivity!.startTime) }} ~ {{ formatActivityTime(groupActivity!.endTime) }}</text>
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
          <rich-text v-if="goods.description" class="detail-text" :nodes="goods.description"></rich-text>
          <template v-if="goods.detailImages.length">
            <image v-for="(img, idx) in goods.detailImages" :key="idx" class="detail-img" :src="resolveImageUrl(img)" mode="widthFix" />
          </template>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <view class="bottom-action">
      <view class="nav-btns">
        <view class="nav-item" @tap="goToChat">
          <svg-icon name="chat" :size="40" color="#757575" />
          <text class="text">客服</text>
        </view>
        <view class="nav-item" @tap="goToCart">
          <svg-icon name="shopping_cart" :size="40" color="#757575" />
          <text class="text">购物车</text>
          <view v-if="cartStore.totalCount > 0" class="badge">{{ cartStore.totalCount }}</view>
        </view>
        <view class="nav-item" @tap="toggleFavorite">
          <svg-icon :name="isFavorite ? 'favorite' : 'favorite_border'" :size="40" :color="isFavorite ? '#E53935' : '#757575'" />
          <text class="text" :style="{ color: isFavorite ? '#E53935' : '' }">收藏</text>
        </view>
      </view>
      <view class="action-btns">
        <button v-if="buyType === 'group' && canGroupBuy" class="add-cart" @tap="goGroupBuyZone">拼团专区</button>
        <button v-else class="add-cart" @tap="handleAddToCart">加入购物车</button>
        <button v-if="buyType === 'group' && canGroupBuy" class="buy-now group-buy-now" @tap="handleLaunchGroup">发起拼团</button>
        <button v-else class="buy-now" @tap="handleBuyNow">立即购买</button>
      </view>
    </view>

    <view v-if="cartPanelVisible" class="cart-panel-mask" @tap="cartPanelVisible = false">
      <view class="cart-panel" @tap.stop>
        <view class="cart-panel-handle"></view>
        <view class="cart-panel-header">
          <view>
            <text class="cart-panel-title">已选商品</text>
            <text class="cart-panel-desc">共 {{ cartStore.totalCount }} 件，满 {{ deliverySetting?.freeAmount || '39' }} 元免配送费</text>
          </view>
          <view class="cart-panel-actions">
            <text v-if="cartStore.items.length" class="cart-panel-clear" @tap="clearCartPanel">清空</text>
            <svg-icon name="close" :size="34" color="#9E9E9E" @tap="cartPanelVisible = false" />
          </view>
        </view>
        <scroll-view scroll-y class="cart-panel-list">
          <view v-if="cartStore.items.length === 0" class="cart-panel-empty">
            <view class="cart-empty-icon">
              <svg-icon name="shopping_cart" :size="52" color="#BDBDBD" />
            </view>
            <text class="cart-empty-title">购物车还是空的</text>
            <text class="cart-empty-desc">先挑几件新鲜水果吧</text>
          </view>
          <view v-for="item in cartStore.items" :key="item.id" class="cart-panel-item">
            <image class="cart-panel-img" :src="resolveImageUrl(item.mainImage)" mode="aspectFill" />
            <view class="cart-panel-info">
              <text class="cart-panel-name">{{ item.productName }}</text>
              <text class="cart-panel-spec">{{ item.specName }}</text>
              <view class="cart-panel-price-row">
                <text class="cart-panel-price">¥{{ item.price }}</text>
                <text v-if="item.originalPrice" class="cart-panel-original">¥{{ item.originalPrice }}</text>
              </view>
            </view>
            <view class="cart-stepper">
              <view class="stepper-btn minus" @tap="decreaseCartItem(item)">-</view>
              <text class="stepper-num">{{ item.quantity }}</text>
              <view class="stepper-btn plus" @tap="increaseCartItem(item)">+</view>
            </view>
          </view>
        </scroll-view>
        <view class="cart-panel-footer">
          <view class="cart-bag">
            <svg-icon name="shopping_cart" :size="40" color="#ffffff" />
            <text v-if="cartStore.totalCount > 0" class="cart-bag-badge">{{ cartStore.totalCount }}</text>
          </view>
          <view class="cart-panel-total">
            <text class="cart-panel-total-price">¥{{ cartStore.totalPrice }}</text>
            <text class="cart-panel-total-label">已优惠 ¥{{ cartStore.discountAmount || '0.00' }}</text>
          </view>
          <button class="cart-panel-btn primary" :class="{ disabled: cartStore.selectedItems.length === 0 }" @tap="goCheckoutFromCart">去结算</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { onShareAppMessage } from '@dcloudio/uni-app';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import { catalogApi } from '@/api/modules/catalog';
import { reviewApi } from '@/pagesA/api/review';
import { shopApi } from '@/api/modules/shop';
import { userApi } from '@/api/modules/user';
import { promoApi } from '@/api/modules/promo';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import CountdownFlip from '@/components/countdown-flip.vue';
import type { ProductDetailVO } from '@/api/types/catalog';
import type { ReviewVO, ReviewSummaryVO } from '@/api/types/review';
import type { DeliverySettingVO } from '@/api/types/shop';
import type { GroupBuyActivityVO } from '@/api/types/promo';

const cartStore = useCartStore();

const productId = ref(0);
const goods = ref<ProductDetailVO | null>(null);
const reviewSummary = ref<ReviewSummaryVO | null>(null);
const reviews = ref<ReviewVO[]>([]);
const deliverySetting = ref<DeliverySettingVO | null>(null);
const promotionTip = ref('');
const isFavorite = ref(false);
const groupActivity = ref<GroupBuyActivityVO | null>(null);
const buyType = ref<'single' | 'group'>('single');
const cartPanelVisible = ref(false);
const now = ref(Date.now());
let activityTimer: ReturnType<typeof setInterval> | null = null;

const activeSkuId = ref(0);

const activeSku = computed(() => {
  if (!goods.value) return null;
  return goods.value.skuList.find(s => s.id === activeSkuId.value) || goods.value.skuList.find(s => s.isDefault === 1) || goods.value.skuList[0];
});

const canGroupBuy = computed(() => {
  if (!groupActivity.value || groupActivity.value.status !== 1) return false;
  if (!groupActivity.value.groupPrice || !groupActivity.value.skuId) return false;
  if (!groupActivity.value.endTime) return true;
  const end = new Date(groupActivity.value.endTime.replace(' ', 'T')).getTime();
  return Number.isNaN(end) || end > now.value;
});

const displayImages = computed(() => {
  if (!goods.value) return [];
  const raw = goods.value.carouselImages.length > 0 ? goods.value.carouselImages : [goods.value.mainImage];
  return raw.map(resolveImageUrl);
});

function decodeHtmlEntities(str: string): string {
  if (!str) return '';
  return str
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&nbsp;/g, ' ');
}

async function loadDetail() {
  try {
    const detail = await catalogApi.productDetail(productId.value);
    if (detail.description) {
      detail.description = decodeHtmlEntities(detail.description);
    }
    goods.value = detail;
    isFavorite.value = detail.isFavorite;
    // 设置默认SKU
    const defaultSku = detail.skuList.find(s => s.isDefault === 1) || detail.skuList[0];
    if (defaultSku) activeSkuId.value = defaultSku.id;

    // 并行加载评价、配送设置、拼团活动
    const [summary, reviewList, ds, gb] = await Promise.all([
      reviewApi.summary(productId.value).catch(() => null),
      reviewApi.productReviews(productId.value, { size: 3 }).catch(() => null),
      shopApi.deliverySetting().catch(() => null),
      promoApi.groupBuyByProduct(productId.value).catch(() => null),
    ]);
    if (summary) reviewSummary.value = summary;
    if (reviewList) reviews.value = reviewList.list;
    if (ds) deliverySetting.value = ds;
    groupActivity.value = gb && gb.status === 1 ? gb : null;
    if (!canGroupBuy.value) buyType.value = 'single';

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
  console.error('[DEBUG] onMounted fired');
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  console.error('[DEBUG] page options=', JSON.stringify(page?.options));
  productId.value = Number(page?.options?.id || page?.options?.productId || 0);
  console.error('[DEBUG] productId=', productId.value);
  if (productId.value) {
    loadDetail();
    recordFootprint();
  }
  activityTimer = setInterval(() => { now.value = Date.now(); }, 1000);
});

onUnmounted(() => {
  if (activityTimer) { clearInterval(activityTimer); activityTimer = null; }
});

async function recordFootprint() {
  const userStore = useUserStore();
  if (!userStore.token) return;
  try {
    await userApi.addFootprint(productId.value);
  } catch (e: any) {
    console.warn('[footprint] failed', e?.code, e?.msg || e?.errMsg || e);
  }
}

function goBack() {
  uni.navigateBack();
}

function formatActivityTime(t?: string) {
  if (!t) return '';
  const d = new Date(t.replace(' ', 'T'));
  if (Number.isNaN(d.getTime())) return t;
  return `${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}

function goToHome() {
  uni.switchTab({ url: '/pages/index/index' });
}

function goToChat() {
  const p = goods.value;
  const sku = activeSku.value;
  const params: string[] = [];
  if (p) {
    params.push(`productId=${p.id}`);
    params.push(`productName=${encodeURIComponent(p.name)}`);
    if (p.mainImage) params.push(`productImage=${encodeURIComponent(p.mainImage)}`);
    if (sku) {
      params.push(`productPrice=${encodeURIComponent(sku.price)}`);
      if (sku.specName) params.push(`productSpec=${encodeURIComponent(sku.specName)}`);
    }
  }
  uni.navigateTo({ url: `/pagesC/chat/index${params.length ? '?' + params.join('&') : ''}` });
}

async function goToCart() {
  try {
    await cartStore.refreshList();
  } catch (e) {
    console.warn('加载购物车失败', e);
  }
  cartPanelVisible.value = true;
}

function goCartPage() {
  cartPanelVisible.value = false;
  uni.switchTab({ url: '/pages/cart/cart' });
}

function goCheckoutFromCart() {
  const selected = cartStore.selectedItems;
  if (selected.length === 0) {
    uni.showToast({ title: '请选择要结算的商品', icon: 'none' });
    return;
  }
  const ids = selected.map(i => i.id).join(',');
  cartPanelVisible.value = false;
  uni.navigateTo({ url: `/pagesB/checkout/index?cartItemIds=${ids}` });
}

async function decreaseCartItem(item: any) {
  if (item.quantity <= 1) {
    await cartStore.remove(item.id);
  } else {
    await cartStore.updateQty(item.id, item.quantity - 1);
  }
}

async function increaseCartItem(item: any) {
  await cartStore.updateQty(item.id, item.quantity + 1);
}

function clearCartPanel() {
  uni.showModal({
    title: '清空购物车',
    content: '确定清空已选商品吗？',
    confirmColor: '#2E7D32',
    success: async (res) => {
      if (res.confirm) await cartStore.clear();
    },
  });
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

async function toggleFavorite() {
  if (!productId.value) return;
  try {
    if (isFavorite.value) {
      await userApi.removeFavorite(productId.value);
      isFavorite.value = false;
      uni.showToast({ title: '已取消收藏', icon: 'none' });
    } else {
      await userApi.addFavorite(productId.value);
      isFavorite.value = true;
      uni.showToast({ title: '已收藏', icon: 'success' });
    }
  } catch (e: any) {
    if (e?.code === 401) {
      uni.showToast({ title: '请先登录', icon: 'none' });
    } else {
      console.warn('收藏操作失败', e);
    }
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

function goGroupBuyZone() {
  uni.navigateTo({ url: '/pagesC/group-buy/index' });
}

async function handleLaunchGroup() {
  if (!canGroupBuy.value || !groupActivity.value) {
    return uni.showToast({ title: '当前商品暂无拼团', icon: 'none' });
  }
  // 跳转到结算页携带 groupBuyActivityId，由结算页发起 launchGroupBuy
  uni.navigateTo({
    url: `/pagesB/checkout/index?groupBuyActivityId=${groupActivity.value.id}`,
  });
}

onShareAppMessage(() => {
  const name = goods.value?.name || '鲜果记好物推荐';
  const image = goods.value?.mainImage ? resolveImageUrl(goods.value.mainImage) : '';
  const price = activeSku.value?.price || goods.value?.minPrice || '';
  return {
    title: `${name} ¥${price}`,
    path: `/pagesA/goods-detail/index?id=${productId.value}`,
    imageUrl: image,
  };
});
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

  .share-btn {
    padding: 0;
    margin: 0;
    line-height: 1;
    background: transparent;
    border: none;

    &::after {
      border: none;
    }
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding-bottom: calc(112rpx + env(safe-area-inset-bottom));
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

  .activity-countdown {
    display: flex;
    align-items: center;
    gap: $space-2;
    margin-top: $space-2;
    padding: $space-1 $space-2;
    background: rgba($color-price, 0.06);
    border-radius: $radius-sm;

    .activity-time {
      font-size: 20rpx;
      color: $color-text-placeholder;
      margin-left: auto;
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
    padding: $space-4;
    background-color: #ffffff;

    .detail-text {
      display: block;
      font-size: $font-base;
      line-height: 1.6;
      color: $color-text-primary;
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

.cart-panel-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.48));
  display: flex;
  align-items: flex-end;
}

.cart-panel {
  width: 100%;
  max-height: 72vh;
  background-color: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -12rpx 40rpx rgba(0,0,0,0.14);
}

.cart-panel-handle {
  width: 72rpx;
  height: 8rpx;
  border-radius: 8rpx;
  background-color: #E0E0E0;
  margin: 16rpx auto 4rpx;
}

.cart-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-3 $space-4 $space-3;
  border-bottom: 2rpx solid rgba($color-divider, 0.45);
}

.cart-panel-title {
  display: block;
  font-size: $font-md;
  font-weight: $weight-semibold;
  color: $color-text-primary;
}

.cart-panel-desc {
  display: block;
  margin-top: 6rpx;
  font-size: $font-xs;
  color: $color-text-secondary;
}

.cart-panel-actions {
  display: flex;
  align-items: center;
  gap: $space-3;
}

.cart-panel-clear {
  font-size: $font-xs;
  color: $color-text-secondary;
}

.cart-panel-list {
  max-height: 560rpx;
  padding: 0 $space-4;
  box-sizing: border-box;
  background-color: #ffffff;
}

.cart-panel-empty {
  padding: $space-6 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;
}

.cart-empty-icon {
  width: 112rpx;
  height: 112rpx;
  border-radius: 56rpx;
  background-color: $color-bg-page;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-empty-title {
  font-size: $font-base;
  font-weight: $weight-semibold;
  color: $color-text-primary;
}

.cart-empty-desc {
  font-size: $font-xs;
  color: $color-text-secondary;
}

.cart-panel-item {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 0;
  border-bottom: 2rpx solid rgba($color-divider, 0.35);
}

.cart-panel-img {
  width: 112rpx;
  height: 112rpx;
  border-radius: $radius-md;
  background-color: $color-bg-page;
  flex-shrink: 0;
}

.cart-panel-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.cart-panel-name {
  font-size: $font-sm;
  font-weight: $weight-semibold;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-panel-spec {
  font-size: $font-xs;
  color: $color-text-secondary;
}

.cart-panel-price-row {
  display: flex;
  align-items: baseline;
  gap: $space-2;
}

.cart-panel-price {
  font-size: $font-base;
  font-weight: $weight-semibold;
  color: $color-price;
}

.cart-panel-original {
  font-size: $font-xs;
  color: $color-text-placeholder;
  text-decoration: line-through;
}

.cart-stepper {
  display: flex;
  align-items: center;
  gap: $space-2;
}

.stepper-btn {
  width: 44rpx;
  height: 44rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-base;
  font-weight: $weight-semibold;

  &.minus {
    color: $color-primary;
    border: 2rpx solid rgba($color-primary, 0.35);
    background-color: #ffffff;
  }

  &.plus {
    color: #ffffff;
    background-color: $color-primary;
  }
}

.stepper-num {
  min-width: 32rpx;
  text-align: center;
  font-size: $font-sm;
  color: $color-text-primary;
}

.cart-panel-footer {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding: $space-3 $space-4;
  background-color: #ffffff;
  border-top: 2rpx solid rgba($color-divider, 0.45);
}

.cart-bag {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, $color-primary, #1B5E20);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 8rpx 24rpx rgba($color-primary, 0.28);
}

.cart-bag-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 30rpx;
  height: 30rpx;
  padding: 0 6rpx;
  border-radius: 15rpx;
  background-color: $color-price;
  color: #ffffff;
  font-size: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-panel-total {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.cart-panel-total-price {
  font-size: $font-lg;
  font-weight: $weight-semibold;
  color: $color-text-primary;
}

.cart-panel-total-label {
  font-size: $font-xs;
  color: $color-text-secondary;
}

.cart-panel-btn {
  width: 180rpx;
  height: 76rpx;
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

  &.disabled {
    opacity: 0.45;
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
      min-width: 0;
      height: 80rpx;
      border-radius: $radius-pill;
      font-size: $font-base;
      font-weight: $weight-semibold;
      display: flex;
      align-items: center;
      justify-content: center;
      white-space: nowrap;
      overflow: hidden;
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

    .group-buy-now {
      font-size: $font-sm;
      letter-spacing: 0;
    }
  }
}
</style>
