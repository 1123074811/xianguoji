<template>
  <view class="page-container">
    <!-- Search Header -->
    <view class="search-header">
      <wd-search
        v-model="keyword"
        placeholder="搜索新鲜水果、蔬菜"
        focus
        hide-cancel
        @search="handleSearch"
      />
      <text class="search-btn" @tap="handleSearch">搜索</text>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Search History -->
      <view class="section" v-if="historyList.length > 0">
        <view class="section-header">
          <text class="title">历史搜索</text>
          <view class="delete-btn" @tap="clearHistory">
            <svg-icon name="delete" :size="32" color="#757575" />
          </view>
        </view>
        <view class="tags-wrapper">
          <view 
            class="tag" 
            v-for="(item, index) in historyList" 
            :key="index"
            @tap="tapTag(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>

      <!-- Hot Search -->
      <view class="section">
        <view class="section-header">
          <text class="title">热门搜索</text>
        </view>
        <view class="tags-wrapper">
          <view 
            class="tag hot" 
            v-for="(item, index) in hotList" 
            :key="index"
            @tap="tapTag(item)"
          >
            <svg-icon v-if="index < 3" name="fire" :size="24" color="#FF5252" class="hot-icon" />
            {{ item }}
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { catalogApi } from '@/api/modules/catalog';
import SvgIcon from '@/components/svg-icon.vue';

const userStore = useUserStore();
const keyword = ref('');
const historyList = ref<string[]>([]);
const hotList = ref<string[]>([]);

onMounted(async () => {
  // 加载热门搜索
  try {
    hotList.value = await catalogApi.hotSearch();
  } catch (e) {
    console.warn('加载热门搜索失败', e);
  }
  // 加载历史搜索（需登录）
  if (userStore.isLogin) {
    try {
      historyList.value = await catalogApi.searchHistory();
    } catch (e) {
      // 未登录时从本地缓存读取
      const history = uni.getStorageSync('searchHistory');
      if (history) historyList.value = JSON.parse(history);
    }
  } else {
    const history = uni.getStorageSync('searchHistory');
    if (history) historyList.value = JSON.parse(history);
  }
});

async function handleSearch() {
  const query = keyword.value.trim();
  if (!query) {
    return uni.showToast({ title: '请输入搜索内容', icon: 'none' });
  }

  // 记录搜索
  if (userStore.isLogin) {
    catalogApi.recordSearch(query).catch(() => {});
  }

  // 本地缓存
  const index = historyList.value.indexOf(query);
  if (index > -1) historyList.value.splice(index, 1);
  historyList.value.unshift(query);
  if (historyList.value.length > 10) historyList.value.pop();
  uni.setStorageSync('searchHistory', JSON.stringify(historyList.value));

  uni.navigateTo({
    url: `/pagesA/search-result/index?keyword=${encodeURIComponent(query)}`
  });
}

function tapTag(tag: string) {
  keyword.value = tag;
  handleSearch();
}

async function clearHistory() {
  uni.showModal({
    title: '提示',
    content: '确认清空历史记录吗？',
    success: async (res) => {
      if (res.confirm) {
        if (userStore.isLogin) {
          try { await catalogApi.clearSearchHistory(); } catch (e) { /* ignore */ }
        }
        historyList.value = [];
        uni.removeStorageSync('searchHistory');
      }
    }
  });
}
</script>

<style lang="scss" scoped>
.page-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
}

.search-header {
  display: flex;
  align-items: center;
  padding: $space-2 $space-4;
  gap: $space-3;
  border-bottom: 2rpx solid $color-divider;

  :deep(.wd-search) {
    flex: 1;
    padding: 0;
    background: transparent;
  }

  .search-btn {
    font-size: $font-base;
    color: $color-primary;
    font-weight: $weight-medium;
  }
}

.main-scroll {
  flex: 1;
  padding: $space-4;
}

.section {
  margin-bottom: $space-6;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-3;

    .title {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }

    .delete-btn {
      padding: $space-1;
    }
  }

  .tags-wrapper {
    display: flex;
    flex-wrap: wrap;
    gap: $space-2;

    .tag {
      padding: 8rpx $space-3;
      background-color: $color-bg-page;
      color: $color-text-secondary;
      font-size: $font-sm;
      border-radius: $radius-pill;
      display: flex;
      align-items: center;
      gap: 4rpx;

      &.hot {
        background-color: rgba($color-price, 0.05);
        color: $color-text-primary;
      }

      .hot-icon {
        margin-right: 4rpx;
      }
    }
  }
}
</style>
