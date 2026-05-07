<template>
  <view class="detail-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="left">
        <svg-icon name="arrow-back" :size="40" color="#757575" @click="goBack" />
        <text class="title">订单详情</text>
      </view>
      <view class="right">
        <svg-icon name="chat" :size="40" color="#2E7D32" />
        <view style="width: 40rpx;"></view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Status Card -->
      <view class="status-card card" v-if="order">
        <view class="top">
          <view class="status-info">
            <text class="label">订单状态</text>
            <text class="status-text">{{ statusText(order?.status) }}</text>
          </view>
        </view>
        <view v-if="isGrouping" class="group-status">
          <view class="avatar-stack">
            <image
              v-for="p in (order.groupBuyInstance?.participants || []).slice(0, 4)"
              :key="p.userId"
              :src="p.avatar ? resolveImageUrl(p.avatar) : '/static/images/default-avatar.png'"
              mode="aspectFill"
              class="group-avatar"
            />
          </view>
          <text class="group-text">{{ order.groupBuyInstance?.currentSize || 0 }}/{{ order.groupBuyInstance?.targetSize || 0 }} 人已参团</text>
          <text class="group-countdown">剩余 {{ groupCountdown }}</text>
        </view>
        <text v-else-if="order.groupBuyInstanceId && order.status === 8" class="refund-tip">拼团失败，支付金额已模拟原路退回</text>
      </view>

      <!-- Tracking (配送单时显示) -->
      <view class="tracking-card card no-padding overflow-hidden" v-if="order && order.deliveryType === 1 && order.status >= 30">
        <!-- TODO: 联调-接入骑手位置API -->
        <view class="rider-info">
          <view class="left">
            <view class="info">
              <text class="name">配送中</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Goods List -->
      <view class="goods-card card" v-if="order">
        <view class="goods-list">
          <view v-for="item in order.items" :key="item.skuId" class="goods-item">
            <image :src="resolveImageUrl(item.image)" mode="aspectFill" class="goods-img" />
            <view class="info">
              <view class="top">
                <text class="name">{{ item.productName }}</text>
                <text class="specs">{{ item.specName }}</text>
              </view>
              <view class="bottom">
                <text class="price">¥{{ item.price }}</text>
                <text class="count">x {{ item.quantity }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="summary">
          <view class="row">
            <text class="label">商品总额</text>
            <text class="value">¥{{ order.goodsAmount }}</text>
          </view>
          <view class="row">
            <text class="label">运费</text>
            <text class="value">¥{{ order.deliveryFee }}</text>
          </view>
          <view class="row" v-if="order.discountAmount !== '0.00'">
            <text class="label">优惠</text>
            <text class="value discount">-¥{{ order.discountAmount }}</text>
          </view>
          <view class="row total">
            <text class="label">实付款</text>
            <text class="value">¥{{ order.payAmount }}</text>
          </view>
        </view>
      </view>

      <!-- Order Info -->
      <view class="info-card card" v-if="order">
        <view class="row">
          <text class="label">订单编号</text>
          <text class="value">{{ order.orderNo }}</text>
        </view>
        <view class="row">
          <text class="label">下单时间</text>
          <text class="value">{{ order.createdAt }}</text>
        </view>
        <view class="row" v-if="order.payTime">
          <text class="label">支付时间</text>
          <text class="value">{{ order.payTime }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Actions -->
    <view class="bottom-bar" v-if="order">
      <button v-if="order.status >= 3" class="action-btn outline" @tap="handleRefund">申请售后</button>
      <button v-if="order.status === 3" class="action-btn primary" @tap="handleConfirm">确认收货</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { orderApi } from '@/api/modules/order';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import type { OrderVO } from '@/api/types/order';

const STATUS_MAP: Record<number, string> = {
  0: '待付款', 1: '待接单', 2: '备货中', 3: '配送中', 4: '待自提', 5: '已完成', 6: '已取消', 7: '退款中', 8: '已退款',
};
function statusText(s?: number) {
  if (order.value?.groupBuyInstanceId && s === 0) return '正在拼团';
  return STATUS_MAP[s ?? -1] || '未知';
}

const order = ref<OrderVO | null>(null);
const orderNo = ref('');
const now = ref(Date.now());
let timer: any = null;

const isGrouping = computed(() => !!order.value?.groupBuyInstanceId && order.value?.groupBuyInstance?.status === 1 && order.value?.status === 0);

const groupCountdown = computed(() => {
  const expireAt = order.value?.groupBuyInstance?.expireAt;
  if (!expireAt) return '--';
  const diff = new Date(expireAt.replace(' ', 'T')).getTime() - now.value;
  if (diff <= 0) return '等待退款';
  const h = Math.floor(diff / 3600_000);
  const m = Math.floor((diff % 3600_000) / 60_000);
  if (h > 0) return `${h}时${m}分`;
  return `${Math.max(1, m)}分`;
});

onMounted(async () => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  orderNo.value = page?.options?.orderNo || '';
  if (orderNo.value) {
    try {
      order.value = await orderApi.detail(orderNo.value);
    } catch (e) {
      console.warn('加载订单详情失败', e);
    }
  }
  timer = setInterval(() => { now.value = Date.now(); }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

function goBack() {
  uni.navigateBack();
}

async function handleConfirm() {
  if (!order.value) return;
  uni.showModal({
    title: '确认收货',
    content: '确认已收到该订单的所有商品？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await orderApi.confirm(order.value!.orderNo);
          uni.showToast({ title: '已确认收货', icon: 'success' });
          order.value = await orderApi.detail(order.value!.orderNo);
        } catch (e) { console.warn(e); }
      }
    }
  });
}

async function handleRefund() {
  if (!order.value) return;
  uni.navigateTo({ url: `/pagesC/refund/index?orderNo=${order.value.orderNo}` });
}
</script>

<style lang="scss" scoped>
.detail-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav {
  background-color: #ffffff;
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);

  .left {
    display: flex;
    align-items: center;
    gap: $space-2;
    .iconfont { font-size: 40rpx; color: $color-text-secondary; }
    .title { font-size: $font-base; font-weight: bold; color: $color-primary; }
  }

  .right {
    display: flex;
    gap: $space-4;
    .iconfont { font-size: 40rpx; color: $color-primary; }
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: $space-4;
}

.status-card {
  .top {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: $space-5;

    .status-info {
      display: flex;
      flex-direction: column;
      .label { font-size: $font-xs; color: $color-text-secondary; }
      .status-text { font-size: 40rpx; font-weight: bold; color: $color-primary; }
    }

    .eta { font-size: $font-sm; color: $color-primary; font-weight: $weight-medium; }
  }

  .steps {
    position: relative;
    display: flex;
    justify-content: space-between;
    padding: 0 $space-2;

    .step-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: $space-1;
      z-index: 2;

      .dot {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        background-color: $color-divider;
      }

      .step-label { font-size: 20rpx; color: $color-text-placeholder; }

      &.active {
        .dot { background-color: $color-primary; }
        .step-label { color: $color-primary; font-weight: bold; }
      }

      &.current {
        .dot { 
          width: 20rpx; 
          height: 20rpx; 
          background-color: $color-primary;
          box-shadow: 0 0 0 8rpx rgba($color-primary, 0.2);
        }
        .step-label { color: $color-primary; font-weight: bold; }
      }
    }

    .line-bg {
      position: absolute;
      top: 10rpx;
      left: $space-5;
      right: $space-5;
      height: 2rpx;
      background-color: $color-divider;
      z-index: 1;
    }

    .line-active {
      position: absolute;
      top: 10rpx;
      left: $space-5;
      height: 2rpx;
      background-color: $color-primary;
      z-index: 1;
    }
  }
}

.tracking-card {
  .map-placeholder {
    height: 360rpx;
    position: relative;
    .map-img { width: 100%; height: 100%; }
    .rider-tag {
      position: absolute;
      bottom: $space-3;
      left: $space-3;
      background-color: rgba(255,255,255,0.9);
      padding: 12rpx $space-3;
      border-radius: $radius-pill;
      display: flex;
      align-items: center;
      gap: $space-2;
      box-shadow: $shadow-card;
      
      .dot { width: 16rpx; height: 16rpx; background-color: $color-primary; border-radius: 50%; }
      .text { font-size: 22rpx; font-weight: $weight-medium; }
    }
  }

  .rider-info {
    padding: $space-4;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: $space-3;
      
      .avatar { width: 96rpx; height: 96rpx; border-radius: 50%; }
      .info {
        .name { font-size: $font-base; font-weight: bold; }
        .rating {
          display: flex;
          align-items: center;
          gap: 4rpx;
          .iconfont { font-size: 24rpx; color: $color-primary; }
          .text { font-size: $font-xs; color: $color-text-secondary; }
        }
      }
    }

    .btns {
      display: flex;
      gap: $space-2;
      .btn-circle {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        border: 2rpx solid $color-divider;
        display: flex;
        align-items: center;
        justify-content: center;
        color: $color-primary;
      }
    }
  }
}

.goods-card {
  .goods-list {
    display: flex;
    flex-direction: column;
    gap: $space-4;
    margin-bottom: $space-5;
  }

  .goods-item {
    display: flex;
    gap: $space-3;
    .goods-img { width: 160rpx; height: 160rpx; border-radius: $radius-sm; background-color: $color-bg-page; }
    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      .top {
        .name { font-size: $font-base; font-weight: bold; }
        .specs { font-size: $font-xs; color: $color-text-placeholder; }
      }
      .bottom {
        display: flex;
        justify-content: space-between;
        .price { font-size: $font-base; font-weight: bold; color: $color-primary; }
        .count { font-size: $font-xs; color: $color-text-placeholder; }
      }
    }
  }

  .summary {
    border-top: 2rpx solid rgba($color-divider, 0.3);
    padding-top: $space-4;
    display: flex;
    flex-direction: column;
    gap: $space-2;

    .row {
      display: flex;
      justify-content: space-between;
      .label { font-size: $font-sm; color: $color-text-secondary; }
      .value { font-size: $font-sm; color: $color-text-primary; }
      &.total {
        margin-top: $space-2;
        .label { font-size: $font-base; font-weight: bold; color: $color-text-primary; }
        .value { font-size: $font-lg; font-weight: bold; color: $color-primary; }
      }
    }
  }
}

.info-card {
  .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: $space-3;
    &:last-child { margin-bottom: 0; }
    .label { font-size: $font-sm; color: $color-text-secondary; }
    .value { font-size: $font-sm; color: $color-text-primary; }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 112rpx;
  background-color: #ffffff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 $space-4;
  gap: $space-3;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  z-index: 100;
  @include safe-area-bottom;

  .action-btn {
    height: 80rpx;
    padding: 0 $space-5;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after { border: none; }

    &.outline {
      background-color: transparent;
      border: 2rpx solid $color-divider;
      color: $color-text-secondary;
    }

    &.primary {
      background-color: $color-primary;
      color: #ffffff;
    }
  }
}
</style>

