<template>
  <view class="page-container">
    <!-- Search Header -->
    <view class="search-header-sticky">
      <wd-search
        v-model="keyword"
        placeholder="搜索新鲜水果、蔬菜"
        hide-cancel
        @search="handleSearch"
      />
    </view>

    <!-- Filter Bar -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: currentSort === 'default' }" @tap="handleSort('default')">综合</view>
      <view class="filter-item" :class="{ active: currentSort === 'sales' }" @tap="handleSort('sales')">销量</view>
      <view class="filter-item price-sort" :class="{ active: currentSort === 'price' }" @tap="handleSort('price')">
        <text>价格</text>
        <view class="sort-icons">
          <svg-icon name="arrow-up" :size="16" :color="priceOrder === 'asc' ? '#2E7D32' : '#BDBDBD'" />
          <svg-icon name="arrow-down" :size="16" :color="priceOrder === 'desc' ? '#2E7D32' : '#BDBDBD'" />
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="result-list" v-if="results.length > 0">
      <view class="goods-grid">
        <goods-card v-for="goods in sortedResults" :key="goods.id" :goods="goods" />
      </view>
    </scroll-view>

    <wd-status-tip
      v-else
      image="search"
      :tip="`暂无与“${keyword}”相关的商品`"
      class="empty-box"
    >
      <wd-button size="small" @click="goBack">重新搜索</wd-button>
    </wd-status-tip>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';
import { catalogApi } from '@/api/modules/catalog';
import type { ProductVO } from '@/api/types/catalog';

const keyword = ref('');
const currentSort = ref<'default' | 'sales' | 'price'>('default');
const priceOrder = ref<'' | 'asc' | 'desc'>('');
const results = ref<ProductVO[]>([]);
const loading = ref(false);

onLoad((options) => {
  if (options && options.keyword) {
    keyword.value = decodeURIComponent(options.keyword);
    handleSearch();
  }
});

function buildSortParam(): 'sales' | 'priceAsc' | 'priceDesc' | undefined {
  if (currentSort.value === 'sales') return 'sales';
  if (currentSort.value === 'price') {
    return priceOrder.value === 'desc' ? 'priceDesc' : 'priceAsc';
  }
  return undefined;
}

async function handleSearch() {
  if (!keyword.value.trim()) return;
  loading.value = true;
  try {
    const data = await catalogApi.productPage({
      keyword: keyword.value.trim(),
      sort: buildSortParam(),
      page: 1,
      size: 20,
    });
    results.value = data.list;
    // 上报搜索词（已登录用户）
    catalogApi.recordSearch(keyword.value.trim()).catch(() => {});
  } catch (e) {
    console.warn('搜索失败', e);
  } finally {
    loading.value = false;
  }
}

const sortedResults = computed(() => results.value);

function handleSort(type: 'default' | 'sales' | 'price') {
  if (type === 'price') {
    if (currentSort.value !== 'price') {
      currentSort.value = 'price';
      priceOrder.value = 'asc';
    } else {
      priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc';
    }
  } else {
    currentSort.value = type;
    priceOrder.value = '';
  }
  handleSearch();
}

function goBack() {
  uni.navigateBack();
}
</script>

<style lang="scss" scoped>
.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.search-header-sticky {
  background-color: #ffffff;
  padding: $space-2 $space-4;
  border-bottom: 2rpx solid $color-divider;
  z-index: 100;

  :deep(.wd-search) {
    padding: 0;
    background: transparent;
  }
}

.filter-bar {
  display: flex;
  background-color: #ffffff;
  padding: 0 $space-4;
  height: 88rpx;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 99;

  .filter-item {
    flex: 1;
    text-align: center;
    font-size: $font-base;
    color: $color-text-secondary;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4rpx;

    &.active {
      color: $color-primary;
      font-weight: $weight-semibold;
    }
  }

  .price-sort {
    .sort-icons {
      display: flex;
      flex-direction: column;
      gap: 2rpx;
    }
  }
}

.result-list {
  flex: 1;
  padding: $space-3;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.empty-box {
  margin-top: 200rpx;
}
</style>
