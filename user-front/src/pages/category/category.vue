<template>
  <view class="category-container">
    <!-- Top Search Bar -->
    <view class="header-sticky">
      <view class="brand-area">
        <svg-icon name="eco" :size="32" color="#2E7D32" />
        <text class="brand-name">鲜果记</text>
      </view>
      <view class="search-box" @tap="goToSearch">
        <svg-icon name="search" :size="28" color="#BDBDBD" />
        <text class="search-placeholder">搜索新鲜水果...</text>
      </view>
      <svg-icon name="chat" :size="40" color="#2E7D32" @click="goToMessage" />
    </view>

    <view class="main-content">
      <!-- Left Sidebar -->
      <scroll-view scroll-y class="sidebar">
        <view 
          v-for="cat in categories" 
          :key="cat.id" 
          class="sidebar-item"
          :class="{ active: activeCatId === cat.id }"
          @tap="activeCatId = cat.id"
        >
          <text class="item-text">{{ cat.name }}</text>
        </view>
      </scroll-view>

      <!-- Right Content -->
      <scroll-view scroll-y class="goods-area" @scrolltolower="loadMore">
        <!-- Sub Category Tabs -->
        <view class="sub-tabs-sticky">
          <scroll-view scroll-x class="sub-tabs-scroll" show-scrollbar="false">
            <view 
              v-for="sub in subCategories" 
              :key="sub.id" 
              class="sub-tab"
              :class="{ active: activeSubId === sub.id }"
              @tap="activeSubId = sub.id"
            >
              {{ sub.name }}
            </view>
          </scroll-view>
        </view>

        <!-- Product Grid -->
        <view class="goods-grid">
          <goods-card 
            v-for="item in filteredGoods" 
            :key="item.id" 
            :goods="item"
          />
        </view>

        <view class="load-more">
          <text v-if="loading">加载中...</text>
          <text v-else-if="noMore">没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tab-bar active-path="pages/category/category" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';
import { catalogApi } from '@/api/modules/catalog';
import type { CategoryTreeVO, ProductVO } from '@/api/types/catalog';

onShow(() => {
  uni.hideTabBar();
  refresh();
});

async function refresh() {
  await fetchCategories();
  await fetchGoods(true);
}

const categories = ref<CategoryTreeVO[]>([]);
const activeCatId = ref(0);
const activeSubId = ref(0);

const goods = ref<ProductVO[]>([]);
const loading = ref(false);
const noMore = ref(false);
const currentPage = ref(1);
const pageSize = 20;

const subCategories = computed(() => {
  const currentCat = categories.value.find(c => c.id === activeCatId.value);
  if (!currentCat) return [{ id: 0, name: '全部商品' }];
  return [{ id: 0, name: '全部商品' }, ...(currentCat.children || [])];
});

const filteredGoods = computed(() => goods.value);

async function fetchCategories() {
  try {
    const catData = await catalogApi.categoryTree();
    categories.value = catData;
    if (catData.length > 0) activeCatId.value = catData[0].id;
  } catch (e) {
    console.error('分类数据获取失败:', e);
  }
}

async function fetchGoods(reset = false) {
  if (loading.value) return;
  if (reset) {
    currentPage.value = 1;
    goods.value = [];
    noMore.value = false;
  }
  if (noMore.value) return;
  loading.value = true;
  try {
    const data = await catalogApi.productPage({
      page: currentPage.value,
      size: pageSize,
      categoryId: activeSubId.value || activeCatId.value || undefined,
    });
    if (reset) {
      goods.value = data.list;
    } else {
      goods.value = [...goods.value, ...data.list];
    }
    if (data.list.length < pageSize) noMore.value = true;
    currentPage.value++;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

watch(activeCatId, () => {
  if (activeSubId.value !== 0) {
    activeSubId.value = 0;
  } else {
    fetchGoods(true);
  }
});

watch(activeSubId, () => {
  fetchGoods(true);
});

function loadMore() {
  fetchGoods();
}

function goToSearch() {
  uni.navigateTo({ url: '/pagesA/search/index' });
}

function goToMessage() {
  uni.navigateTo({ url: '/pagesC/message/index' });
}
</script>

<style lang="scss" scoped>
.category-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
  box-sizing: border-box;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
}

.header-sticky {
  background-color: #ffffff;
  padding: $space-2 $space-3;
  display: flex;
  align-items: center;
  gap: $space-2;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
  z-index: 100;

  .brand-area {
    display: flex;
    align-items: center;
    gap: 8rpx;
    
    .brand-icon {
      font-size: 32rpx;
      color: $color-primary;
    }
    
    .brand-name {
      font-size: $font-base;
      font-weight: bold;
      color: $color-primary;
    }
  }

  .search-box {
    flex: 1;
    height: 64rpx;
    background-color: $color-bg-page;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    padding: 0 $space-2;
    gap: $space-1;

    .search-icon {
      font-size: 28rpx;
      color: $color-text-placeholder;
    }

    .search-placeholder {
      font-size: $font-xs;
      color: $color-text-placeholder;
    }
  }

  .msg-icon {
    font-size: 40rpx;
    color: $color-primary;
  }
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 180rpx;
  background-color: $color-bg-card;
  border-right: 2rpx solid rgba($color-divider, 0.3);

  .sidebar-item {
    padding: $space-4 $space-2;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    .item-text {
      font-size: $font-base;
      color: $color-text-secondary;
    }

    &.active {
      background-color: rgba($color-primary, 0.05);
      border-left: 8rpx solid $color-primary;
      
      .item-text {
        font-weight: $weight-semibold;
        color: $color-primary;
      }
    }
  }
}

.goods-area {
  flex: 1;
  background-color: $color-bg-page;

  .sub-tabs-sticky {
    position: sticky;
    top: 0;
    z-index: 10;
    background-color: rgba($color-bg-page, 0.95);
    backdrop-filter: blur(10rpx);
    padding: $space-2 0;
    border-bottom: 2rpx solid rgba($color-divider, 0.1);

    .sub-tabs-scroll {
      white-space: nowrap;
      padding: 0 $space-2;
    }

    .sub-tab {
      display: inline-block;
      padding: 8rpx $space-3;
      margin-right: $space-2;
      border-radius: $radius-pill;
      background-color: $color-bg-card;
      font-size: $font-xs;
      color: $color-text-secondary;
      transition: all 0.2s;

      &.active {
        background-color: $color-primary;
        color: #ffffff;
      }
    }
  }

  .goods-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: $space-2;
    padding: $space-2;
  }

  .load-more {
    padding: $space-4;
    text-align: center;
    font-size: $font-xs;
    color: $color-text-placeholder;
  }
}
</style>
