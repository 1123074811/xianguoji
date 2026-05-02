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
        <view v-for="point in points" :key="point.id" class="pickup-card card">
          <view class="left-info">
            <text class="name">{{ point.name }}</text>
            <text class="address">{{ point.address }}</text>
            <view class="tags">
              <text class="tag distance">距离您 {{ point.distance }}km</text>
              <text class="tag status">营业中</text>
            </view>
          </view>
          <view class="right-actions">
            <view class="icon-btn">
              <svg-icon name="location" :size="40" color="#2E7D32" />
            </view>
            <view class="icon-btn">
              <svg-icon name="chat" :size="40" color="#2E7D32" />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';

const points = ref([
  { id: '1', name: '鲜果记（朝阳路店）', address: '北京市朝阳区朝阳路1号院', distance: '0.8' },
  { id: '2', name: '鲜果记（三里屯店）', address: '北京市朝阳区三里屯路11号', distance: '1.5' },
  { id: '3', name: '鲜果记（望京SOHO店）', address: '北京市朝阳区望京街10号', distance: '3.2' },
]);
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