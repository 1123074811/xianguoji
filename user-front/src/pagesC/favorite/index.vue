<template>
  <view class="page-container">
    <view class="header-tools" v-if="favoriteGoods.length > 0">
      <text class="count">共 {{ favoriteGoods.length }} 件宝贝</text>
      <text class="manage-btn" @tap="isManageMode = !isManageMode">{{ isManageMode ? '完成' : '管理' }}</text>
    </view>

    <scroll-view scroll-y class="goods-list" v-if="favoriteGoods.length > 0">
      <view class="goods-grid">
        <view class="goods-item-wrapper" v-for="goods in favoriteGoods" :key="goods.id">
          <goods-card :goods="goods" />
          <view v-if="isManageMode" class="remove-mask" @tap="removeFavorite(goods.id)">
            <view class="remove-btn">
              <svg-icon name="close" :size="32" color="#FFFFFF" />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <wd-status-tip
      v-else
      image="search"
      tip="暂无收藏商品"
      class="empty-box"
    >
      <wd-button size="small" @click="goShopping">去逛逛</wd-button>
    </wd-status-tip>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';

const isManageMode = ref(false);

const favoriteGoods = ref([
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
  }
]);

function removeFavorite(id: string) {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏该商品吗？',
    success: (res) => {
      if (res.confirm) {
        favoriteGoods.value = favoriteGoods.value.filter(g => g.id !== id);
        uni.showToast({ title: '已取消收藏', icon: 'none' });
        if (favoriteGoods.value.length === 0) {
          isManageMode.value = false;
        }
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
    color: $color-primary;
  }
}

.goods-list {
  flex: 1;
  padding: $space-3;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $space-3;
}

.goods-item-wrapper {
  position: relative;

  .remove-mask {
    position: absolute;
    inset: 0;
    background-color: rgba(255, 255, 255, 0.5);
    z-index: 10;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;

    .remove-btn {
      width: 80rpx;
      height: 80rpx;
      background-color: rgba(0, 0, 0, 0.6);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.empty-box {
  margin-top: 200rpx;
}
</style>
