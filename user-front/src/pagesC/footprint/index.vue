<template>
  <view class="page-container">
    <view class="header-tools" v-if="footprintGroups.length > 0">
      <text class="count">共 {{ totalCount }} 条足迹</text>
      <text class="manage-btn" @tap="clearFootprints">清空</text>
    </view>

    <scroll-view scroll-y class="footprint-list" v-if="footprintGroups.length > 0">
      <view class="date-group" v-for="(group, index) in footprintGroups" :key="index">
        <view class="date-header">{{ group.date }}</view>
        <view class="goods-grid">
          <goods-card v-for="goods in group.items" :key="goods.id" :goods="goods" />
        </view>
      </view>
    </scroll-view>

    <wd-status-tip
      v-else
      image="search"
      tip="暂无浏览足迹"
      class="empty-box"
    >
      <wd-button size="small" @click="goShopping">去逛逛</wd-button>
    </wd-status-tip>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import GoodsCard from '@/components/goods-card.vue';

const footprintGroups = ref([
  {
    date: '今天',
    items: [
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
      }
    ]
  },
  {
    date: '昨天',
    items: [
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
    ]
  }
]);

const totalCount = computed(() => {
  return footprintGroups.value.reduce((acc, group) => acc + group.items.length, 0);
});

function clearFootprints() {
  uni.showModal({
    title: '提示',
    content: '确定要清空所有浏览足迹吗？',
    success: (res) => {
      if (res.confirm) {
        footprintGroups.value = [];
        uni.showToast({ title: '已清空', icon: 'success' });
      }
    }
  });
}

function goShopping() {
  uni.switchTab({ url: '/pages/index/index' });
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.header-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-3 $space-4;
  background-color: #ffffff;
  
  .count {
    font-size: $font-sm;
    color: $color-text-secondary;
  }
  
  .manage-btn {
    font-size: $font-sm;
    color: $color-text-secondary;
  }
}

.footprint-list {
  flex: 1;
  padding: 0 $space-3;
}

.date-group {
  margin-bottom: $space-4;

  .date-header {
    font-size: $font-base;
    font-weight: $weight-semibold;
    color: $color-text-primary;
    padding: $space-3 0;
  }
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
