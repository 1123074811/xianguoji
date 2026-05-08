<template>
  <view class="order-container">
    <!-- Top Navigation -->
    <view class="header-nav-sticky">
      <view class="left">
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

    <scroll-view scroll-y class="main-scroll" show-scrollbar="false" @scrolltolower="loadMore">
      <view class="order-list">
        <view v-for="order in filteredOrders" :key="order.orderNo" class="order-card card" @tap="goToDetail(order)">
          <view class="card-header">
            <text class="order-no">订单号: {{ order.orderNo }}</text>
            <text class="status">{{ statusText(order) }}</text>
          </view>
          
          <view class="goods-scroll">
            <scroll-view scroll-x class="goods-imgs" show-scrollbar="false">
              <image 
                v-for="(item, index) in order.items" 
                :key="index" 
                :src="resolveImageUrl(item.image)" 
                mode="aspectFill" 
                class="goods-img" 
              />
            </scroll-view>
          </view>

          <view v-if="isGrouping(order)" class="grouping-card">
            <view class="grouping-left">
              <view class="avatar-stack">
                <image
                  v-for="p in (order.groupBuyInstance?.participants || []).slice(0, 4)"
                  :key="p.userId"
                  :src="p.avatar ? resolveImageUrl(p.avatar) : '/static/images/default-avatar.png'"
                  mode="aspectFill"
                  class="group-avatar"
                />
                <view v-for="i in groupEmptySlots(order)" :key="'empty-' + i" class="group-avatar empty-avatar">?</view>
              </view>
              <text class="grouping-text">{{ order.groupBuyInstance?.currentSize || 0 }}/{{ order.groupBuyInstance?.targetSize || 0 }} 人已参团</text>
            </view>
            <text class="grouping-countdown">剩余 {{ groupCountdown(order) }}</text>
          </view>

          <view class="card-footer">
            <view class="total-info">
              <text class="count">共 {{ order.items.length }} 件商品 实付</text>
              <text class="price">¥{{ order.payAmount }}</text>
            </view>
            <view class="actions" @tap.stop>
              <button
                v-for="btn in getOrderButtons(order)"
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { orderApi } from '@/api/modules/order';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';
import type { OrderVO } from '@/api/types/order';

const STATUS_MAP: Record<number, string> = {
  0: '待付款', 1: '待接单', 2: '备货中', 3: '配送中', 4: '待自提', 5: '已完成', 6: '已取消', 7: '退款中', 8: '已退款',
};
function statusText(order: OrderVO) {
  if (order.groupBuyInstanceId && order.status === 0) return '正在拼团';
  return STATUS_MAP[order.status] || '未知';
}

onShow(async () => {
  const storedTab = uni.getStorageSync('orderActiveTab');
  if (storedTab && tabs.find(t => t.id === storedTab)) {
    activeTabId.value = storedTab;
    uni.removeStorageSync('orderActiveTab');
  }
  await fetchOrders(true);
});

// 接受来自 profile 的 tab 切换事件
const setTabHandler = (id: string) => {
  if (tabs.find(t => t.id === id)) activeTabId.value = id;
};
uni.$on('order:setTab', setTabHandler);
onMounted(() => {
  timer = setInterval(() => { now.value = Date.now(); }, 1000);
});
onUnmounted(() => {
  uni.$off('order:setTab', setTabHandler);
  if (timer) clearInterval(timer);
});

const tabs = [
  { id: 'all', name: '全部' },
  { id: 'unpaid', name: '待付款' },
  { id: 'toship', name: '待发货/提' },
  { id: 'toreceive', name: '待收货' },
  { id: 'completed', name: '已完成' },
  { id: 'groupbuy', name: '拼团中' },
  { id: 'aftersale', name: '退款/售后' }
];

const tabMap: Record<string, string | undefined> = {
  all: undefined,
  unpaid: 'pending',
  toship: 'processing',
  toreceive: 'delivering',
  completed: 'done',
  aftersale: 'aftersale',
};

const activeTabId = ref('all');
const orders = ref<OrderVO[]>([]);
const now = ref(Date.now());
let timer: any = null;
const loading = ref(false);
const noMore = ref(false);
const currentPage = ref(1);
const pageSize = 10;

const filteredOrders = computed(() => {
  if (activeTabId.value === 'groupbuy') {
    return orders.value.filter(order => !!order.groupBuyInstanceId && order.status === 0);
  }
  return orders.value;
});

function isGrouping(order: OrderVO) {
  return !!order.groupBuyInstanceId && order.groupBuyInstance?.status === 1 && order.status === 0;
}

function groupEmptySlots(order: OrderVO) {
  const current = order.groupBuyInstance?.currentSize || 0;
  const target = order.groupBuyInstance?.targetSize || 0;
  return Math.max(0, Math.min(4 - current, target - current));
}

function groupCountdown(order: OrderVO) {
  const expireAt = order.groupBuyInstance?.expireAt;
  if (!expireAt) return '--';
  const diff = new Date(expireAt.replace(' ', 'T')).getTime() - now.value;
  if (diff <= 0) return '等待退款';
  const h = Math.floor(diff / 3600_000);
  const m = Math.floor((diff % 3600_000) / 60_000);
  const s = Math.floor((diff % 60_000) / 1000);
  if (h > 0) return `${h}时${m}分${s}秒`;
  return `${m}分${s}秒`;
}

async function fetchOrders(reset = false) {
  if (loading.value) return;
  if (reset) {
    currentPage.value = 1;
    orders.value = [];
    noMore.value = false;
  }
  if (noMore.value) return;
  loading.value = true;
  try {
    const tab = tabMap[activeTabId.value];
    const data = await orderApi.page({
      page: currentPage.value,
      size: pageSize,
      tab,
    });
    if (reset) {
      orders.value = data.list;
    } else {
      orders.value = [...orders.value, ...data.list];
    }
    if (data.list.length < pageSize) noMore.value = true;
    currentPage.value++;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

watch(activeTabId, () => fetchOrders(true));

function loadMore() {
  fetchOrders();
}

function goToMessage() {
  uni.navigateTo({ url: '/pagesC/message/index' });
}

function goToSearch() {
  uni.navigateTo({ url: '/pagesA/search/index' });
}

function goToDetail(order: OrderVO) {
  uni.navigateTo({ url: `/pagesB/order-detail/index?orderNo=${order.orderNo}` });
}

function getOrderButtons(order: OrderVO) {
  const btns: { text: string; primary: boolean }[] = [];
  if (isGrouping(order)) {
    btns.push({ text: '查看拼团', primary: true });
    return btns;
  }
  switch (order.status) {
    case 0: btns.push({ text: '查看详情', primary: false }, { text: '取消订单', primary: false }, { text: '立即支付', primary: true }); break;
    case 1: case 2: btns.push({ text: '查看详情', primary: false }, { text: '提醒发货', primary: true }); break;
    case 3: btns.push({ text: '查看详情', primary: false }, { text: '查看物流', primary: false }, { text: '确认收货', primary: true }); break;
    case 5: btns.push({ text: '查看详情', primary: false }, { text: '去评价', primary: true }, { text: '再次购买', primary: false }); break;
    default: btns.push({ text: '查看详情', primary: false });
  }
  return btns;
}

async function handleAction(order: OrderVO, btn: any) {
  switch (btn.text) {
    case '查看物流':
    case '查看详情':
      uni.navigateTo({ url: `/pagesB/order-detail/index?orderNo=${order.orderNo}` });
      break;
    case '查看拼团':
      if (order.groupBuyInstanceId) {
        uni.navigateTo({ url: `/pagesC/group-buy/detail?id=${order.groupBuyInstanceId}` });
      }
      break;
    case '确认收货':
      uni.showModal({
        title: '确认收货',
        content: '确认已收到该订单的所有商品？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.confirm(order.orderNo);
              uni.showToast({ title: '已确认收货', icon: 'success' });
              fetchOrders(true);
            } catch (e) { console.warn(e); }
          }
        }
      });
      break;
    case '立即支付':
      uni.navigateTo({ url: `/pagesB/payment-result/index?orderNo=${order.orderNo}` });
      break;
    case '取消订单':
      uni.showModal({
        title: '取消订单',
        content: '确定要取消该订单？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await orderApi.cancel(order.orderNo);
              uni.showToast({ title: '订单已取消', icon: 'success' });
              fetchOrders(true);
            } catch (e) { console.warn(e); }
          }
        }
      });
      break;
    case '提醒发货':
      try {
        await orderApi.remind(order.orderNo);
        uni.showToast({ title: '已提醒商家发货', icon: 'success' });
      } catch (e) { console.warn(e); }
      break;
    case '去评价':
      uni.navigateTo({ url: `/pagesB/evaluation/index?orderNo=${order.orderNo}` });
      break;
    case '再次购买':
      try {
        await orderApi.repurchase(order.orderNo);
        uni.switchTab({ url: '/pages/cart/cart' });
      } catch (e) { console.warn(e); }
      break;
    default:
      uni.navigateTo({ url: `/pagesB/order-detail/index?orderNo=${order.orderNo}` });
  }
}
</script>

<style lang="scss" scoped>
.order-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
  box-sizing: border-box;
  padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
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

/* Hide horizontal scrollbar on H5 for inner scroll-views */
:deep(.goods-imgs ::-webkit-scrollbar),
:deep(.tabs-scroll ::-webkit-scrollbar),
:deep(.main-scroll ::-webkit-scrollbar) {
  display: none;
  width: 0;
  height: 0;
}

.order-card {
  overflow: hidden;

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

  .grouping-card {
    margin-bottom: $space-3;
    padding: $space-3;
    border-radius: $radius-md;
    background: linear-gradient(135deg, rgba($color-primary, 0.1), rgba($color-primary, 0.03));
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $space-2;

    .grouping-left {
      display: flex;
      align-items: center;
      min-width: 0;
      gap: $space-2;
    }

    .avatar-stack {
      display: flex;
      align-items: center;
      flex-shrink: 0;
    }

    .group-avatar {
      width: 44rpx;
      height: 44rpx;
      border-radius: 50%;
      border: 3rpx solid #ffffff;
      margin-left: -10rpx;
      background-color: $color-bg-page;

      &:first-child {
        margin-left: 0;
      }
    }

    .empty-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      color: $color-text-placeholder;
      font-size: $font-xs;
      border-style: dashed;
    }

    .grouping-text {
      font-size: $font-xs;
      color: $color-primary;
      font-weight: $weight-semibold;
      white-space: nowrap;
    }

    .grouping-countdown {
      font-size: $font-xs;
      color: $color-price;
      white-space: nowrap;
    }
  }

  .card-footer {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: $space-3;

    .total-info {
      display: flex;
      align-items: baseline;
      flex: 1;
      min-width: 0;
      gap: $space-1;
      overflow: hidden;

      .count {
        font-size: $font-xs;
        color: $color-text-secondary;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .price {
        font-size: $font-md;
        font-weight: bold;
        color: $color-text-primary;
        white-space: nowrap;
      }
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      flex-shrink: 0;
      gap: $space-1;
      overflow: hidden;

      .action-btn {
        height: 48rpx;
        min-height: 48rpx;
        padding: 0 $space-2;
        border-radius: $radius-pill;
        font-size: $font-xs;
        line-height: 44rpx;
        text-align: center;
        white-space: nowrap;
        flex-shrink: 0;
        border: 2rpx solid $color-divider;
        background-color: transparent;
        color: $color-text-primary;

        &.primary {
          background-color: $color-primary;
          color: #ffffff;
          border: none;
          line-height: 48rpx;
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


