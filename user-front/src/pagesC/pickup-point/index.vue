<template>
  <view class="pickup-container">
    <view class="search-bar">
      <view class="search-inner">
        <svg-icon name="search" :size="32" color="#757575" />
        <input class="search-input" placeholder="搜索附近自提点" />
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <view class="pickup-list">
        <view
          v-for="point in points"
          :key="point.id"
          class="pickup-card card"
          :class="{ selectable: selectMode }"
          @tap="selectPoint(point)"
        >
          <view class="left-info">
            <text class="name">{{ point.name }}</text>
            <text class="address">{{ point.address }}</text>
            <view class="tags">
              <text class="tag distance">{{ point.businessHours }}</text>
              <text class="tag status" :class="{ closed: !isOpen(point.businessHours) }">{{ isOpen(point.businessHours) ? '营业中' : '休息中' }}</text>
            </view>
          </view>
          <view class="right-actions">
            <view class="icon-btn" @tap.stop="openMap(point)">
              <svg-icon name="location" :size="40" color="#2E7D32" />
            </view>
            <view class="icon-btn" @tap.stop>
              <svg-icon name="chat" :size="40" color="#2E7D32" />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import SvgIcon from '@/components/svg-icon.vue';
import { shopApi } from '@/api/modules/shop';
import type { PickupPointVO } from '@/api/types/shop';

const points = ref<PickupPointVO[]>([]);
const selectMode = ref(false);

onLoad((options: any) => {
  selectMode.value = options?.select === '1';
});

onMounted(loadPickupPoints);

async function loadPickupPoints() {
  try {
    points.value = await shopApi.pickupPointList();
  } catch (e) {
    console.warn('自提点加载失败', e);
    points.value = [];
  }
}

function selectPoint(point: PickupPointVO) {
  if (!selectMode.value) return;
  const pages = getCurrentPages();
  const prevPage = pages[pages.length - 2] as any;
  if (prevPage?.$vm) {
    prevPage.$vm.selectedPickupPoint = point;
  }
  uni.navigateBack();
}

function openMap(point: PickupPointVO) {
  if (!point.latitude || !point.longitude) {
    return uni.showToast({ title: '暂无位置信息', icon: 'none' });
  }
  uni.openLocation({
    latitude: Number(point.latitude),
    longitude: Number(point.longitude),
    name: point.name,
    address: point.address,
  });
}

function isOpen(businessHours?: string): boolean {
  if (!businessHours) return true;
  const [start, end] = businessHours.split('-');
  if (!start || !end) return true;
  const now = new Date();
  const h = now.getHours();
  const m = now.getMinutes();
  const [sh, sm] = start.split(':').map(Number);
  const [eh, em] = end.split(':').map(Number);
  const nowMin = h * 60 + m;
  return nowMin >= sh * 60 + (sm || 0) && nowMin <= eh * 60 + (em || 0);
}
</script>

<style lang="scss" scoped>
.pickup-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.search-bar {
  padding: $space-3 $space-4;
  background-color: #fff;
  
  .search-inner {
    display: flex;
    align-items: center;
    background-color: #F5F5F5;
    padding: $space-2 $space-3;
    border-radius: $radius-pill;
    gap: $space-2;
    
    .search-input {
      flex: 1;
      font-size: $font-base;
    }
  }
}

.main-scroll {
  flex: 1;
  padding: $space-3 $space-4;
}

.pickup-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-4;
  margin-bottom: $space-3;

  .left-info {
    display: flex;
    flex-direction: column;
    gap: $space-2;

    .name {
      font-size: $font-md;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }

    .address {
      font-size: $font-sm;
      color: $color-text-secondary;
    }

    .tags {
      display: flex;
      gap: $space-2;

      .tag {
        font-size: $font-xs;
        padding: 4rpx 12rpx;
        border-radius: 4rpx;
        
        &.distance {
          background-color: #E8F5E9;
          color: #2E7D32;
        }
        &.status {
          background-color: #FFF3E0;
          color: #EF6C00;

          &.closed {
            background-color: #FFEBEE;
            color: #C62828;
          }
        }
      }
    }
  }

  .right-actions {
    display: flex;
    flex-direction: column;
    gap: $space-3;
    border-left: 1px solid $color-divider;
    padding-left: $space-4;

    .icon-btn {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      background-color: #F5F5F5;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}
</style>