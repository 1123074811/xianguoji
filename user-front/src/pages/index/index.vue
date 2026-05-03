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
      <!-- Category Shortcuts -->
      <view class="category-shortcuts">
        <view 
          v-for="cat in shortcuts" 
          :key="cat.name" 
          class="shortcut-item"
          @tap="goToCategory(cat)"
        >
          <view class="icon-box" :style="{ backgroundColor: cat.bgColor }">
            <svg-icon :name="cat.icon" :size="48" :color="cat.iconColor" />
          </view>
          <text class="shortcut-name">{{ cat.name }}</text>
        </view>
      </view>

      <!-- Banner -->
      <view class="banner-section">
        <image class="banner-img" src="https://picsum.photos/750/300?random=10" mode="aspectFill" />
        <view class="banner-content">
          <text class="banner-title">夏日西瓜季</text>
          <text class="banner-desc">清凉一夏，甜彻心扉</text>
          <view class="banner-btn">立即抢购</view>
        </view>
      </view>

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
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import GoodsCard from '@/components/goods-card.vue';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';
import { getMockData } from '@/mock/index';

onShow(() => {
  uni.hideTabBar();
});

const shortcuts = [
  { name: '叶菜', icon: 'eco', bgColor: '#E8F5E9', iconColor: '#2E7D32' },
  { name: '水果', icon: 'nutrition', bgColor: '#FFF3E0', iconColor: '#EF6C00' },
  { name: '根茎', icon: 'garden_cart', bgColor: '#FFF8E1', iconColor: '#F9A825' },
  { name: '菌菇', icon: 'forest', bgColor: '#F5F5F5', iconColor: '#616161' },
  { name: '肉蛋', icon: 'restaurant', bgColor: '#FFEBEE', iconColor: '#C62828' },
  { name: '水产', icon: 'water_drop', bgColor: '#E3F2FD', iconColor: '#1565C0' },
  { name: '豆制', icon: 'liquor', bgColor: '#FFFDE7', iconColor: '#FBC02D' },
  { name: '烘焙', icon: 'bakery_dining', bgColor: '#FFF9C4', iconColor: '#F57F17' }
];

const recommendedGoods = ref<any[]>([]);
const loading = ref(false);
const noMore = ref(false);

async function fetchGoods() {
  if (loading.value || noMore.value) return;
  loading.value = true;
  try {
    const data = await getMockData<any[]>('goods.json');
    // 确保 ID 唯一，避免 wx:key 报错
    const newData = data.map(item => ({
      ...item,
      id: item.id + '_' + Date.now() + '_' + Math.random()
    }));
    recommendedGoods.value = [...recommendedGoods.value, ...newData];
    if (recommendedGoods.value.length > 20) noMore.value = true;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchGoods();
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

function goToCategory(cat: any) {
  uni.switchTab({ url: '/pages/category/category' });
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
