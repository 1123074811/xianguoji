<template>
  <view class="order-container">
    <!-- Top Navigation -->
    <view class="header-nav-sticky">
      <view class="left">
        <svg-icon name="arrow-back" :size="40" color="#757575" @click="goBack" />
        <text class="title">我的订单</text>
      </view>
      <view class="right-icons" style="display: flex; gap: 24rpx;">
        <svg-icon name="search" :size="40" color="#757575" @click="goToSearch" />
        <svg-icon name="chat" :size="40" color="#757575" @click="goToMessage" />
      </view>
    </view>

    <!-- Tabs -->
    <view class="tabs-sticky">
      <scroll-view scroll-x class="tabs-scroll" show-scrollbar="false">
        <view 
          v-for="tab in tabs" 
          :key="tab.id" 
          class="tab-item"
          :class="{ active: activeTabId === tab.id }"
          @tap="activeTabId = tab.id"
        >
          {{ tab.name }}
          <view class="line" v-if="activeTabId === tab.id"></view>
        </view>
      </scroll-view>
    </view>

    <scroll-view scroll-y class="main-scroll" @scrolltolower="loadMore">
      <view class="order-list">
        <view v-for="order in filteredOrders" :key="order.id" class="order-card card">
          <view class="card-header">
            <text class="order-no">订单号: {{ order.order_no }}</text>
            <text class="status" :class="order.status">{{ order.statusText }}</text>
          </view>
          
          <view class="goods-scroll">
            <scroll-view scroll-x class="goods-imgs" show-scrollbar="false">
              <image 
                v-for="(img, index) in order.images" 
                :key="index" 
                :src="img" 
                mode="aspectFill" 
                class="goods-img" 
              />
            </scroll-view>
          </view>

          <view class="card-footer">
            <view class="total-info">
              <text class="count">共 {{ order.itemCount }} 件商品 实付</text>
              <text class="price">¥{{ order.total_price }}</text>
            </view>
            <view class="actions">
              <button 
                v-for="btn in order.buttons" 
                :key="btn.text" 
                class="action-btn"
                :class="{ primary: btn.primary }"
                @tap="handleAction(order, btn)"
              >
                {{ btn.text }}
              </button>
            </view>
          </view>
        </view>
      </view>

      <view class="load-more">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>
    </scroll-view>

    <!-- Custom Tab Bar -->
    <custom-tab-bar active-path="pages/order/order" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';

onShow(() => {
  uni.hideTabBar();
});

// 接受来自 profile 的 tab 切换事件
const setTabHandler = (id: string) => {
  if (tabs.find(t => t.id === id)) activeTabId.value = id;
};
uni.$on('order:setTab', setTabHandler);
onUnmounted(() => uni.$off('order:setTab', setTabHandler));

const tabs = [
  { id: 'all', name: '全部' },
  { id: 'unpaid', name: '待付款' },
  { id: 'toship', name: '待发货/提' },
  { id: 'toreceive', name: '待收货' },
  { id: 'completed', name: '已完成' },
  { id: 'aftersale', name: '退款/售后' }
];

const activeTabId = ref('all');
const orders = ref<any[]>([]);
const loading = ref(false);
const noMore = ref(false);

const filteredOrders = computed(() => {
  if (activeTabId.value === 'all') return orders.value;
  return orders.value.filter(o => o.status === activeTabId.value);
});

async function fetchOrders() {
  if (loading.value || noMore.value) return;
  loading.value = true;
  // 模拟数据
  setTimeout(() => {
    const mockOrders = [
      {
        id: '1',
        order_no: '202310248812',
        status: 'toreceive',
        statusText: '运输中',
        images: ['https://picsum.photos/160/160?random=20', 'https://picsum.photos/160/160?random=21', 'https://picsum.photos/160/160?random=22'],
        itemCount: 4,
        total_price: '128.50',
        buttons: [
          { text: '查看物流', primary: false },
          { text: '确认收货', primary: true }
        ]
      },
      {
        id: '2',
        order_no: '202310249905',
        status: 'unpaid',
        statusText: '待付款 23:59',
        images: ['https://picsum.photos/160/160?random=23'],
        itemCount: 1,
        total_price: '39.90',
        buttons: [
          { text: '取消订单', primary: false },
          { text: '立即支付', primary: true }
        ]
      }
    ];
    orders.value = [...orders.value, ...mockOrders];
    noMore.value = true;
    loading.value = false;
  }, 500);
}

onMounted(() => {
  fetchOrders();
});

function loadMore() {
  fetchOrders();
}

function goBack() {
  uni.navigateBack();
}

function goToMessage() {
  uni.navigateTo({ url: '/pagesC/message/index' });
}

function goToSearch() {
  uni.navigateTo({ url: '/pagesA/search/index' });
}

function handleAction(order: any, btn: any) {
  switch (btn.text) {
    case '查看物流':
    case '查看详情':
      uni.navigateTo({ url: `/pagesB/order-detail/index?orderNo=${order.order_no}` });
      break;
    case '确认收货':
      uni.showModal({
        title: '确认收货',
        content: '确认已收到该订单的所有商品？',
        success: (res) => {
          if (res.confirm) {
            order.status = 'completed';
            order.statusText = '已完成';
            order.buttons = [
              { text: '查看详情', primary: false },
              { text: '去评价', primary: true }
            ];
            uni.showToast({ title: '已确认收货', icon: 'success' });
          }
        }
      });
      break;
    case '立即支付':
      uni.navigateTo({ url: `/pagesB/checkout/index?orderNo=${order.order_no}` });
      break;
    case '取消订单':
      uni.showModal({
        title: '取消订单',
        content: '确定要取消该订单？',
        success: (res) => {
          if (res.confirm) {
            const idx = orders.value.findIndex(o => o.id === order.id);
            if (idx >= 0) orders.value.splice(idx, 1);
            uni.showToast({ title: '订单已取消', icon: 'success' });
          }
        }
      });
      break;
    case '去评价':
      uni.navigateTo({ url: '/pagesC/evaluation/index' });
      break;
    case '再次购买':
      uni.switchTab({ url: '/pages/index/index' });
      break;
    default:
      uni.navigateTo({ url: `/pagesB/order-detail/index?orderNo=${order.order_no}` });
  }
}
</script>

<style lang="scss" scoped>
.order-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav-sticky {
  background-color: #ffffff;
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;

  .left {
    display: flex;
    align-items: center;
    gap: $space-3;

    .title {
      font-size: $font-lg;
      font-weight: bold;
      color: $color-primary;
    }
  }
}

.tabs-sticky {
  background-color: #ffffff;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);
  z-index: 99;

  .tabs-scroll {
    white-space: nowrap;
    padding: 0 $space-2;
  }

  .tab-item {
    display: inline-block;
    padding: $space-3 $space-4;
    font-size: $font-base;
    color: $color-text-secondary;
    position: relative;
    transition: all 0.2s;

    &.active {
      color: $color-primary;
      font-weight: $weight-semibold;

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
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: $space-4;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
}

.order-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-3;

    .order-no {
      font-size: $font-xs;
      color: $color-text-secondary;
    }

    .status {
      font-size: $font-sm;
      font-weight: $weight-semibold;
      
      &.toreceive { color: $color-primary; }
      &.unpaid { color: $color-price; }
    }
  }

  .goods-scroll {
    margin-bottom: $space-3;
    
    .goods-imgs {
      white-space: nowrap;
      
      .goods-img {
        width: 160rpx;
        height: 160rpx;
        border-radius: $radius-sm;
        background-color: $color-bg-page;
        margin-right: $space-2;
      }

      .more-icon {
        display: inline-flex;
        width: 160rpx;
        height: 160rpx;
        background-color: $color-primary-bg;
        border-radius: $radius-sm;
        align-items: center;
        justify-content: center;
        color: $color-text-secondary;
      }
    }
  }

  .card-footer {
    display: flex;
    flex-direction: column;
    gap: $space-4;

    .total-info {
      display: flex;
      justify-content: flex-end;
      align-items: baseline;
      gap: $space-1;

      .count {
        font-size: $font-xs;
        color: $color-text-secondary;
      }

      .price {
        font-size: $font-md;
        font-weight: bold;
        color: $color-text-primary;
      }
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      gap: $space-2;

      .action-btn {
        height: 64rpx;
        padding: 0 $space-4;
        border-radius: $radius-pill;
        font-size: $font-sm;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2rpx solid $color-divider;
        background-color: transparent;
        color: $color-text-primary;

        &.primary {
          background-color: $color-primary;
          color: #ffffff;
          border: none;
        }

        &::after { border: none; }
      }
    }
  }
}

.load-more {
  padding: $space-4;
  text-align: center;
  font-size: $font-xs;
  color: $color-text-placeholder;
}
</style>
