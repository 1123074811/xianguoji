<template>
  <view class="page-container">
    <!-- Tabs -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-item"
        :class="{ active: activeTab === tab.id }"
        @tap="activeTab = tab.id"
      >
        {{ tab.name }}
        <text v-if="tab.count" class="count">({{ tab.count }})</text>
        <view class="line" v-if="activeTab === tab.id"></view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- 待评价 -->
      <view v-if="activeTab === 'pending'">
        <view v-if="pendingList.length === 0" class="empty">
          <wd-status-tip image="comment" tip="暂无待评价订单" />
        </view>
        <view v-else class="list">
          <view v-for="item in pendingList" :key="item.id" class="goods-card card">
            <view class="goods-row">
              <image :src="item.image" mode="aspectFill" class="img" />
              <view class="info">
                <text class="name">{{ item.name }}</text>
                <text class="meta">下单时间：{{ item.orderTime }}</text>
                <text class="meta">订单号：{{ item.orderNo }}</text>
              </view>
            </view>
            <view class="actions">
              <button class="btn ghost" @tap="skipReview(item)">暂不评价</button>
              <button class="btn primary" @tap="goReview(item)">立即评价</button>
            </view>
          </view>
        </view>
      </view>

      <!-- 已评价 -->
      <view v-else>
        <view v-if="doneList.length === 0" class="empty">
          <wd-status-tip image="comment" tip="还没有任何评价记录" />
        </view>
        <view v-else class="list">
          <view v-for="rev in doneList" :key="rev.id" class="review-card card">
            <view class="goods-row">
              <image :src="rev.image" mode="aspectFill" class="img-sm" />
              <view class="info">
                <text class="name">{{ rev.name }}</text>
                <view class="stars">
                  <svg-icon v-for="n in 5" :key="n" name="star" :size="28" :color="n <= rev.score ? '#FFB300' : '#E0E0E0'" />
                  <text class="time">{{ rev.time }}</text>
                </view>
              </view>
            </view>
            <text class="content">{{ rev.content }}</text>
            <view v-if="rev.images && rev.images.length" class="img-list">
              <image v-for="(img, i) in rev.images" :key="i" :src="img" mode="aspectFill" class="img-thumb" />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';

const pendingList = ref([
  {
    id: 'p1',
    orderNo: '202310249988',
    orderTime: '2024-05-01 14:20',
    name: '智利车厘子 J 级 2斤装',
    image: 'https://picsum.photos/200/200?random=51',
    spec: '2斤/箱'
  },
  {
    id: 'p2',
    orderNo: '202310249987',
    orderTime: '2024-04-28 10:05',
    name: '海南金钻凤梨 3只装',
    image: 'https://picsum.photos/200/200?random=52',
    spec: '约 3kg'
  }
]);

const doneList = ref([
  {
    id: 'd1',
    name: '新疆吐鲁番葡萄 玫瑰香 1kg',
    image: 'https://picsum.photos/200/200?random=53',
    score: 5,
    time: '2024-04-22',
    content: '葡萄非常新鲜，颗粒饱满，甜度也很高，全家都爱吃，下次还会回购！',
    images: ['https://picsum.photos/200/200?random=63', 'https://picsum.photos/200/200?random=64']
  },
  {
    id: 'd2',
    name: '云南高山雪莲果 2.5kg',
    image: 'https://picsum.photos/200/200?random=54',
    score: 4,
    time: '2024-04-15',
    content: '口感清甜，爽脆多汁，包装也比较严实，运输完好。',
    images: []
  }
]);

const tabs = computed(() => [
  { id: 'pending', name: '待评价', count: pendingList.value.length },
  { id: 'done', name: '已评价', count: doneList.value.length }
]);

const activeTab = ref('pending');

function goReview(item: any) {
  uni.navigateTo({ url: `/pagesB/evaluation/index?orderNo=${item.orderNo}&goodsId=${item.id}` });
}

function skipReview(item: any) {
  uni.showModal({
    title: '提示',
    content: '暂不评价后，订单将自动确认收货。',
    success: (res) => {
      if (res.confirm) {
        const idx = pendingList.value.findIndex(p => p.id === item.id);
        if (idx >= 0) pendingList.value.splice(idx, 1);
        uni.showToast({ title: '已跳过', icon: 'none' });
      }
    }
  });
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.tabs {
  display: flex;
  background-color: #ffffff;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);

  .tab-item {
    flex: 1;
    text-align: center;
    padding: $space-3 0;
    font-size: $font-base;
    color: $color-text-secondary;
    position: relative;

    &.active {
      color: $color-primary;
      font-weight: $weight-semibold;
    }

    .count {
      font-size: $font-xs;
      margin-left: 4rpx;
    }

    .line {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background-color: $color-primary;
      border-radius: 2rpx;
    }
  }
}

.main-scroll {
  flex: 1;
  padding: $space-3 $space-4 $space-6;
}

.empty { padding-top: 200rpx; }

.list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $space-4;
  box-shadow: $shadow-card;
}

.goods-row {
  display: flex;
  gap: $space-3;
  align-items: flex-start;

  .img {
    width: 160rpx;
    height: 160rpx;
    border-radius: $radius-sm;
    background-color: $color-bg-page;
    flex-shrink: 0;
  }

  .img-sm {
    width: 96rpx;
    height: 96rpx;
    border-radius: $radius-sm;
    background-color: $color-bg-page;
    flex-shrink: 0;
  }

  .info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    overflow: hidden;

    .name {
      font-size: $font-base;
      font-weight: $weight-medium;
      color: $color-text-primary;
      line-height: 1.4;
    }

    .meta {
      font-size: $font-xs;
      color: $color-text-secondary;
    }

    .stars {
      display: flex;
      align-items: center;
      gap: 4rpx;
      flex-wrap: wrap;

      .time {
        font-size: $font-xs;
        color: $color-text-placeholder;
        margin-left: $space-2;
      }
    }
  }
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: $space-2;
  margin-top: $space-3;

  .btn {
    height: 64rpx;
    padding: 0 $space-4;
    border-radius: $radius-pill;
    font-size: $font-sm;
    display: flex;
    align-items: center;
    justify-content: center;

    &::after { border: none; }

    &.ghost {
      background-color: transparent;
      color: $color-text-primary;
      border: 2rpx solid $color-divider;
    }

    &.primary {
      background-color: $color-primary;
      color: #ffffff;
      border: none;
    }
  }
}

.review-card {
  .content {
    font-size: $font-sm;
    color: $color-text-primary;
    line-height: 1.6;
    margin-top: $space-3;
  }

  .img-list {
    display: flex;
    gap: $space-2;
    margin-top: $space-2;
    flex-wrap: wrap;

    .img-thumb {
      width: 144rpx;
      height: 144rpx;
      border-radius: $radius-sm;
      background-color: $color-bg-page;
    }
  }
}
</style>
