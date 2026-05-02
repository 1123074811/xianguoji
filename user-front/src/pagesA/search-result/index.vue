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

const keyword = ref('');
const currentSort = ref('default'); // default, sales, price
const priceOrder = ref(''); // asc, desc

// Mock data
const mockResults = ref([
  {
    id: '1',
    name: '智利进口车厘子 JJJ级 2.5kg 礼盒装',
    price: 288.00,
    originalPrice: '358.00',
    image: 'https://images.unsplash.com/photo-1528821128474-27f963b062bf?w=500&q=80',
    sales: 1200,
    stock: 50,
    isGroupBuy: true,
    groupBuyPrice: 258.00
  },
  {
    id: '2',
    name: '四川蒲江红心猕猴桃 15枚装',
    price: 39.90,
    originalPrice: '59.90',
    image: 'https://images.unsplash.com/photo-1585059895524-72359e06138a?w=500&q=80',
    sales: 856,
    stock: 200,
    isGroupBuy: false
  },
  {
    id: '3',
    name: '泰国进口金枕榴莲 3-4斤/个',
    price: 168.00,
    originalPrice: '198.00',
    image: 'https://images.unsplash.com/photo-1552089123-2d26226fc2b7?w=500&q=80',
    sales: 432,
    stock: 0,
    isGroupBuy: false
  },
  {
    id: '4',
    name: '新疆阿克苏冰糖心苹果 5kg',
    price: 58.00,
    originalPrice: '78.00',
    image: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cb6c?w=500&q=80',
    sales: 2100,
    stock: 500,
    isGroupBuy: false
  }
]);

const results = ref<any[]>([]);

onLoad((options) => {
  if (options && options.keyword) {
    keyword.value = decodeURIComponent(options.keyword);
    handleSearch();
  }
});

function handleSearch() {
  if (!keyword.value.trim()) return;
  // 简单的模拟搜索：只展示名称中包含关键词的商品，如果为空展示全部（方便测试）
  if (keyword.value === '123') { // 测试空状态
    results.value = [];
  } else {
    results.value = [...mockResults.value];
  }
}

const sortedResults = computed(() => {
  const list = [...results.value];
  if (currentSort.value === 'sales') {
    return list.sort((a, b) => b.sales - a.sales);
  } else if (currentSort.value === 'price') {
    return list.sort((a, b) => {
      const priceA = a.isGroupBuy ? a.groupBuyPrice : a.price;
      const priceB = b.isGroupBuy ? b.groupBuyPrice : b.price;
      return priceOrder.value === 'asc' ? priceA - priceB : priceB - priceA;
    });
  }
  return list; // default
});

function handleSort(type: string) {
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
