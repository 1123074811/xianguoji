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
import { ref, computed, onMounted } from 'vue';
import GoodsCard from '@/components/goods-card.vue';
import { userApi } from '@/api/modules/user';
import type { ProductVO } from '@/api/types/catalog';

const footprintGroups = ref<{ date: string; items: ProductVO[] }[]>([]);
const total = ref(0);

const totalCount = computed(() => total.value);

async function loadFootprint() {
  try {
    const data = await userApi.footprintPage({ page: 1, size: 100 });
    total.value = data.total;
    // 后端 VO 不带浏览时间分组字段；当前一律放在「最近浏览」一组
    footprintGroups.value = data.list.length
      ? [{ date: '最近浏览', items: data.list }]
      : [];
  } catch (e) {
    console.warn('加载足迹失败', e);
  }
}

function clearFootprints() {
  uni.showModal({
    title: '提示',
    content: '确定要清空所有浏览足迹吗？',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await userApi.clearFootprint();
        footprintGroups.value = [];
        total.value = 0;
        uni.showToast({ title: '已清空', icon: 'success' });
      } catch (e) {
        console.warn('清空失败', e);
      }
    },
  });
}

function goShopping() {
  uni.switchTab({ url: '/pages/index/index' });
}

onMounted(loadFootprint);
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
