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
          <view v-if="isManageMode" class="remove-mask" @tap="removeFavorite(Number(goods.id))">
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
import { ref, onMounted } from 'vue';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';
import { userApi } from '@/api/modules/user';
import type { ProductVO } from '@/api/types/catalog';

const isManageMode = ref(false);
const favoriteGoods = ref<ProductVO[]>([]);
const loading = ref(false);
const page = ref(1);
const noMore = ref(false);

async function loadFavorites(reset = false) {
  if (loading.value) return;
  if (!reset && noMore.value) return;
  loading.value = true;
  try {
    if (reset) { page.value = 1; noMore.value = false; }
    const data = await userApi.favoritePage({ page: page.value, size: 20 });
    favoriteGoods.value = reset ? data.list : [...favoriteGoods.value, ...data.list];
    if (favoriteGoods.value.length >= data.total) noMore.value = true;
    else page.value += 1;
  } catch (e) {
    console.warn('加载收藏失败', e);
  } finally {
    loading.value = false;
  }
}

function removeFavorite(id: number) {
  uni.showModal({
    title: '提示',
    content: '确定要取消收藏该商品吗？',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await userApi.removeFavorite(id);
        favoriteGoods.value = favoriteGoods.value.filter(g => g.id !== id);
        uni.showToast({ title: '已取消收藏', icon: 'none' });
        if (favoriteGoods.value.length === 0) {
          isManageMode.value = false;
        }
      } catch (e) {
        console.warn('取消收藏失败', e);
      }
    },
  });
}

function goShopping() {
  uni.switchTab({ url: '/pages/index/index' });
}

onMounted(() => loadFavorites(true));
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
