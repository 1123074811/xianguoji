<template>
  <view class="home-container">
    <!-- Top Search Bar -->
    <view class="header-sticky">
      <view class="search-bar" @tap="goToSearch">
        <svg-icon name="search" :size="32" color="#BDBDBD" />
        <text class="search-placeholder">搜索新鲜果蔬</text>
      </view>
      <view class="msg-btn" hover-class="btn-active" @tap="goToMessage">
        <svg-icon name="chat" :size="40" color="#757575" />
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll" @scrolltolower="loadMore">
      <!-- Shop Closed Banner -->
      <view v-if="!shopOpen" class="closed-banner">
        <svg-icon name="store" :size="36" color="#C62828" />
        <view class="closed-info">
          <text class="closed-title">店铺休息中</text>
          <text class="closed-desc">商家暂未营业，暂时无法下单，请稍后再来～</text>
        </view>
      </view>
      <!-- Category Shortcuts -->
      <view class="category-shortcuts">
        <view 
          v-for="(cat, idx) in shortcuts" 
          :key="cat.id" 
          class="shortcut-item"
          @tap="goToCategory(cat)"
        >
          <view class="icon-box" :style="{ backgroundColor: shortcutColors[idx % shortcutColors.length] }">
            <svg-icon :name="cat.icon || 'fruit_cherries'" :size="48" :color="shortcutIconColors[idx % shortcutIconColors.length]" />
          </view>
          <text class="shortcut-name">{{ cat.name }}</text>
        </view>
      </view>

      <!-- Banner -->
      <swiper v-if="banners.length" class="banner-section" autoplay circular :interval="4000">
        <swiper-item v-for="banner in banners" :key="banner.id">
          <image class="banner-img" :src="resolveImageUrl(banner.image)" mode="aspectFill" />
          <view class="banner-content">
            <text class="banner-title">{{ banner.title }}</text>
            <view class="banner-btn" @tap="handleBannerClick(banner)">立即抢购</view>
          </view>
        </swiper-item>
      </swiper>

      <!-- Coupon Area -->
      <scroll-view scroll-x class="coupon-scroll">
        <view class="coupon-list">
          <view
            v-for="(coupon, idx) in coupons"
            :key="coupon.id"
            class="coupon-card"
            :style="{ backgroundColor: couponBgColors[idx % couponBgColors.length] }"
          >
            <view class="coupon-info">
              <text class="coupon-amount" :style="{ color: couponTextColors[idx % couponTextColors.length] }">¥{{ formatAmount(coupon.amount) }}</text>
              <text class="coupon-name" :style="{ color: couponTextColors[idx % couponTextColors.length] }">{{ coupon.name }}</text>
              <text class="coupon-expire" :style="{ color: couponTextColors[idx % couponTextColors.length] }">{{ formatExpire(coupon.endTime) }}前</text>
            </view>
            <view class="coupon-action">
              <text v-if="isCouponClaimed(coupon)" class="coupon-btn claimed" :style="{ color: couponTextColors[idx % couponTextColors.length] }">已领取</text>
              <text v-else class="coupon-btn" :style="{ backgroundColor: couponTextColors[idx % couponTextColors.length], color: '#ffffff' }" @tap="claimCoupon(coupon)">领取</text>
            </view>
            <view class="coupon-notch" :style="{ borderLeftColor: couponBgColors[idx % couponBgColors.length] }"></view>
          </view>
        </view>
      </scroll-view>

      <!-- Group Buy Section Entry -->
      <view class="section-header">
        <text class="section-title">限时拼团</text>
        <view class="more-btn" @tap="goToGroupBuy">
          <text>更多</text>
          <svg-icon name="chevron-right" :size="24" color="#2E7D32" />
        </view>
      </view>
      
      <!-- Recommended Products -->
      <view class="section-header">
        <text class="section-title">店主推荐</text>
      </view>
      <view class="goods-grid">
        <goods-card 
          v-for="item in recommendedGoods" 
          :key="item.id" 
          :goods="item"
        />
      </view>

      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>
    </scroll-view>

    <!-- Custom Tab Bar -->
    <custom-tab-bar active-path="pages/index/index" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';
import { catalogApi } from '@/api/modules/catalog';
import { resolveImageUrl } from '@/utils/image';
import { promoApi } from '@/api/modules/promo';
import type { BannerVO, CategoryTreeVO, ProductVO } from '@/api/types/catalog';
import type { CouponVO } from '@/api/types/promo';
import { useAppStore } from '@/stores/app';

const appStore = useAppStore();
const shopOpen = computed(() => appStore.shopInfo?.isOpen === 1);

onShow(() => {
  uni.hideTabBar();
  appStore.loadShopInfo();
  loadHomeData();
});

const shortcuts = ref<CategoryTreeVO[]>([]);
const banners = ref<BannerVO[]>([]);
const coupons = ref<CouponVO[]>([]);
const recommendedGoods = ref<ProductVO[]>([]);
const loading = ref(false);
const noMore = ref(false);

const shortcutColors = [
  '#E8F5E9', '#FFF3E0', '#FFF8E1', '#F5F5F5',
  '#FFEBEE', '#E3F2FD', '#FFFDE7', '#FFF9C4',
];
const shortcutIconColors = [
  '#2E7D32', '#EF6C00', '#F9A825', '#616161',
  '#C62828', '#1565C0', '#FBC02D', '#F57F17',
];
const couponBgColors = ['#FFDAD6', '#B9F474', '#A3F69C', '#FFDAD6'];
const couponTextColors = ['#BA1A1A', '#3E6A00', '#0D631B', '#BA1A1A'];

async function loadHomeData() {
  try {
    const [tree, bannerList, couponList, recList] = await Promise.all([
      catalogApi.categoryTree(),
      catalogApi.bannerList(),
      promoApi.couponList(),
      catalogApi.recommend(),
    ]);
    shortcuts.value = tree.slice(0, 8);
    banners.value = bannerList;
    coupons.value = couponList;
    recommendedGoods.value = recList;
  } catch (e) {
    console.warn('首页数据加载失败', e);
  }
}

function formatAmount(value: string | number) {
  const num = Number(value);
  if (!isFinite(num)) return value;
  return Number.isInteger(num) ? String(num) : num.toFixed(2).replace(/\.?0+$/, '');
}

function formatExpire(endTime: string) {
  if (!endTime) return '';
  const d = new Date(endTime.replace(' ', 'T'));
  if (isNaN(d.getTime())) return endTime.slice(0, 10);
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${m}.${day}`;
}

function isCouponClaimed(coupon: CouponVO) {
  return (coupon.userReceivedCount || 0) >= (coupon.perUserLimit || 1);
}

async function claimCoupon(coupon: CouponVO) {
  try {
    await promoApi.claimCoupon(coupon.id);
    uni.showToast({ title: `已领取 ¥${coupon.amount} 优惠券`, icon: 'success' });
    await loadHomeData();
  } catch (e) {
    console.warn('领券失败', e);
  }
}

async function fetchGoods() {
  if (loading.value || noMore.value) return;
  loading.value = true;
  try {
    const data = await catalogApi.recommend();
    const existingIds = new Set(recommendedGoods.value.map(g => g.id));
    const newItems = data.filter(g => !existingIds.has(g.id));
    if (newItems.length === 0) {
      noMore.value = true;
    } else {
      recommendedGoods.value = [...recommendedGoods.value, ...newItems];
      if (recommendedGoods.value.length > 20) noMore.value = true;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function loadMore() {
  fetchGoods();
}

function goToSearch() {
  uni.navigateTo({ url: '/pagesA/search/index' });
}

function goToMessage() {
  uni.navigateTo({ url: '/pagesC/message/index' });
}

function goToCategory(cat: any) {
  uni.switchTab({ url: `/pages/category/category?catId=${cat.id}` });
}

function handleBannerClick(banner: BannerVO) {
  if (banner.linkType === 1 && banner.linkValue) {
    uni.navigateTo({ url: `/pagesA/goods-detail/index?id=${banner.linkValue}` });
  } else if (banner.linkType === 2 && banner.linkValue) {
    uni.navigateTo({ url: `/pagesC/group-buy/index?id=${banner.linkValue}` });
  }
}

function goToGroupBuy() {
  uni.navigateTo({ url: '/pagesC/group-buy/index' });
}
</script>

<style lang="scss" scoped>
.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
  box-sizing: border-box;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
}

.header-sticky {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #ffffff;
  padding: $space-2 $space-4;
  display: flex;
  align-items: center;
  gap: $space-3;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.04);

  .search-bar {
    flex: 1;
    height: 64rpx;
    background-color: $color-bg-page;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    padding: 0 $space-3;
    gap: $space-2;

    .search-icon {
      font-size: 32rpx;
      color: $color-text-placeholder;
    }

    .search-placeholder {
      font-size: $font-sm;
      color: $color-text-placeholder;
    }
  }

  .msg-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-text-secondary;
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
}

.closed-banner {
  display: flex;
  align-items: center;
  gap: $space-3;
  margin: $space-3 $space-4 0;
  padding: $space-3 $space-4;
  background-color: #FFF3E0;
  border: 2rpx solid #FFB74D;
  border-radius: $radius-md;

  .closed-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4rpx;

    .closed-title {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: #C62828;
    }

    .closed-desc {
      font-size: $font-xs;
      color: #BF360C;
      opacity: 0.8;
    }
  }
}

.category-shortcuts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-4 $space-2;
  padding: $space-4;
  background-color: #ffffff;

  .shortcut-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-1;

    .icon-box {
      width: 96rpx;
      height: 96rpx;
      border-radius: 32rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      .iconfont {
        font-size: 48rpx;
      }
    }

    .shortcut-name {
      font-size: $font-sm;
      color: $color-text-primary;
    }
  }
}

.banner-section {
  margin: $space-4;
  height: 320rpx;
  border-radius: $radius-md;
  position: relative;
  overflow: hidden;
  box-shadow: $shadow-card;

  .banner-img {
    width: 100%;
    height: 100%;
  }

  .banner-content {
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, rgba(0,0,0,0.4), transparent);
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 0 $space-5;
    gap: $space-1;

    .banner-title {
      font-size: 40rpx;
      font-weight: $weight-semibold;
      color: #ffffff;
    }

    .banner-desc {
      font-size: $font-sm;
      color: rgba(255,255,255,0.9);
    }

    .banner-btn {
      margin-top: $space-3;
      width: fit-content;
      background-color: #ffffff;
      color: $color-primary;
      padding: 8rpx $space-3;
      border-radius: $radius-pill;
      font-size: $font-sm;
      font-weight: $weight-semibold;
    }
  }
}

.section-header {
  padding: $space-4 $space-4 $space-2;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .section-title {
    font-size: $font-md;
    font-weight: $weight-semibold;
    color: $color-text-primary;
  }

  .more-btn {
    display: flex;
    align-items: center;
    gap: 4rpx;
    font-size: $font-sm;
    color: $color-primary;
  }
}

.coupon-scroll {
  white-space: nowrap;
  padding: 0 $space-4 $space-2;

  .coupon-list {
    display: inline-flex;
    gap: $space-3;
  }

  .coupon-card {
    display: inline-flex;
    align-items: center;
    width: 340rpx;
    height: 180rpx;
    border-radius: $radius-md;
    padding: $space-3;
    position: relative;
    overflow: hidden;
    flex-shrink: 0;

    .coupon-info {
      display: flex;
      flex-direction: column;
      gap: 4rpx;
      z-index: 1;
      min-width: 0;
      flex: 1;

      .coupon-amount {
        font-size: 36rpx;
        font-weight: $weight-semibold;
      }

      .coupon-name {
        font-size: 22rpx;
        font-weight: $weight-medium;
        max-width: 200rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .coupon-expire {
        font-size: 18rpx;
        opacity: 0.75;
      }
    }

    .coupon-action {
      margin-left: auto;
      z-index: 1;

      .coupon-btn {
        font-size: 20rpx;
        padding: 8rpx 16rpx;
        border-radius: 8rpx;

        &.claimed {
          background-color: transparent;
          opacity: 0.7;
        }
      }
    }

    .coupon-notch {
      position: absolute;
      right: -16rpx;
      top: 0;
      bottom: 0;
      width: 32rpx;
      border-left: 4rpx dashed rgba(255,255,255,0.3);
      background-color: rgba(0,0,0,0.03);
    }
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
  padding: $space-3;
}

.load-more {
  padding: $space-5;
  text-align: center;
  font-size: $font-sm;
  color: $color-text-placeholder;
}
</style>
