<template>
  <view class="gb-detail">
    <view class="header card">
      <image v-if="instance?.mainImage" :src="resolveImageUrl(instance.mainImage)" mode="aspectFill" class="cover" />
      <view class="info">
        <text class="name">{{ instance?.productName || '拼团详情' }}</text>
        <view class="price-row">
          <text class="price">¥{{ instance?.groupPrice || '--' }}</text>
          <text class="size-tag">{{ instance?.targetSize }}人团</text>
        </view>
      </view>
    </view>

    <view class="status-card card">
      <template v-if="instance?.status === 1">
        <text class="status status-doing">拼团进行中</text>
        <text class="progress">还差 {{ remaining }} 人成团</text>
        <text class="countdown" v-if="countdown">剩余 {{ countdown }} 自动结束</text>
      </template>
      <template v-else-if="instance?.status === 2">
        <text class="status status-success">已成团</text>
        <text class="desc">订单已进入备货流程</text>
      </template>
      <template v-else-if="instance?.status === 3">
        <text class="status status-fail">拼团失败</text>
        <text class="desc">未达成团人数，关联订单已自动取消/退款</text>
      </template>
    </view>

    <view class="participants card">
      <text class="section-title">参团成员（{{ instance?.currentSize || 0 }}/{{ instance?.targetSize || 0 }}）</text>
      <view class="member-list">
        <view v-for="p in instance?.participants || []" :key="p.userId" class="member">
          <image :src="p.avatar ? resolveImageUrl(p.avatar) : defaultAvatar" class="avatar" />
          <text class="name">{{ p.nickname || '匿名用户' }}</text>
          <text v-if="p.isLeader" class="leader-tag">团长</text>
        </view>
        <view v-for="i in emptySlots" :key="'e'+i" class="member empty">
          <view class="avatar empty-avatar">?</view>
          <text class="name">待加入</text>
        </view>
      </view>
    </view>

    <view class="share-card card" v-if="instance?.shareCode">
      <text class="section-title">邀请好友参团</text>
      <view class="share-row">
        <text class="code">分享码：{{ instance.shareCode }}</text>
        <button class="copy-btn" @tap="copyShareCode">复制</button>
      </view>
    </view>

    <view class="action-bar">
      <button v-if="instance?.status === 1 && !meIsParticipant" class="btn btn-primary" @tap="goJoin">立即参团</button>
      <button v-else-if="instance?.status === 1" class="btn btn-disabled" disabled>已在拼团中</button>
      <button v-else-if="instance?.status === 2" class="btn btn-secondary" @tap="goOrders">查看订单</button>
      <button v-else class="btn btn-secondary" @tap="goBack">返回</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { promoApi } from '@/api/modules/promo';
import { useUserStore } from '@/stores/user';
import { resolveImageUrl } from '@/utils/image';
import type { GroupBuyInstanceVO } from '@/api/types/promo';

const instance = ref<GroupBuyInstanceVO | null>(null);
const userStore = useUserStore();
const now = ref(Date.now());
const defaultAvatar = '/static/avatar-default.png';
let timer: any = null;

const remaining = computed(() => {
  if (!instance.value) return 0;
  return Math.max(0, (instance.value.targetSize || 0) - (instance.value.currentSize || 0));
});

const emptySlots = computed(() => Math.max(0, remaining.value));

const meIsParticipant = computed(() => {
  const myId = userStore.userInfo?.id;
  if (!myId) return false;
  return (instance.value?.participants || []).some(p => String(p.userId) === String(myId));
});

const countdown = computed(() => {
  if (!instance.value?.expireAt) return '';
  const t = new Date(instance.value.expireAt.replace(' ', 'T')).getTime() - now.value;
  if (t <= 0) return '';
  const h = Math.floor(t / 3600_000);
  const m = Math.floor((t % 3600_000) / 60_000);
  const s = Math.floor((t % 60_000) / 1000);
  if (h > 0) return `${h}时${m}分`;
  return `${m}分${s}秒`;
});

async function loadDetail(instanceId: number, shareCode: string) {
  try {
    if (instanceId) {
      instance.value = await promoApi.groupBuyDetail(instanceId);
    } else if (shareCode) {
      instance.value = await promoApi.groupBuyByShareCode(shareCode);
    }
  } catch (e) {
    uni.showToast({ title: '加载拼团失败', icon: 'none' });
  }
}

function copyShareCode() {
  if (!instance.value?.shareCode) return;
  uni.setClipboardData({ data: instance.value.shareCode });
}

async function goJoin() {
  if (!instance.value) return;
  const url = `/pagesB/checkout/index?groupBuyInstanceId=${instance.value.id}`;
  uni.navigateTo({ url });
}

function goOrders() {
  uni.switchTab({ url: '/pages/order/order' });
}

function goBack() {
  uni.navigateBack();
}

onMounted(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  const opts = page?.options || {};
  loadDetail(Number(opts.id || opts.instanceId || 0), opts.code || opts.shareCode || '');
  timer = setInterval(() => { now.value = Date.now(); }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style lang="scss" scoped>
.gb-detail {
  min-height: 100vh;
  padding: $space-3;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
  gap: $space-3;
}
.card {
  background: #fff;
  border-radius: $radius-md;
  padding: $space-4;
}
.header {
  display: flex;
  gap: $space-3;
  .cover {
    width: 180rpx;
    height: 180rpx;
    border-radius: $radius-sm;
    background: $color-bg-page;
  }
  .info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
  .name { font-size: $font-base; font-weight: bold; }
  .price-row { display: flex; align-items: baseline; gap: $space-2; }
  .price { color: $color-primary; font-size: $font-lg; font-weight: bold; }
  .size-tag {
    font-size: 22rpx;
    color: $color-primary;
    background: $color-primary-bg;
    padding: 2rpx $space-2;
    border-radius: $radius-pill;
  }
}
.status-card {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: $space-1;
  .status { font-size: $font-md; font-weight: bold; }
  .status-doing { color: $color-primary; }
  .status-success { color: #16a34a; }
  .status-fail { color: #dc2626; }
  .progress { color: $color-text-secondary; }
  .countdown { color: $color-text-placeholder; font-size: $font-xs; }
  .desc { color: $color-text-secondary; font-size: $font-sm; }
}
.section-title {
  display: block;
  font-weight: bold;
  margin-bottom: $space-2;
}
.member-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-3;
  .member {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4rpx;
    .avatar { width: 96rpx; height: 96rpx; border-radius: 50%; background: $color-bg-page; }
    .empty-avatar {
      display: flex; align-items: center; justify-content: center;
      color: $color-text-placeholder;
      border: 2rpx dashed $color-divider;
    }
    .name {
      font-size: 22rpx;
      color: $color-text-secondary;
      max-width: 130rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .leader-tag {
      font-size: 18rpx;
      color: #fff;
      background: $color-primary;
      padding: 0 $space-1;
      border-radius: $radius-pill;
    }
    &.empty .name { color: $color-text-placeholder; }
  }
}
.share-card {
  .share-row {
    display: flex;
    align-items: center;
    gap: $space-2;
    .code {
      flex: 1;
      font-family: monospace;
      letter-spacing: 2rpx;
      font-size: $font-md;
      color: $color-text-primary;
    }
    .copy-btn {
      background: $color-primary;
      color: #fff;
      font-size: $font-xs;
      padding: 0 $space-3;
      height: 56rpx;
      border-radius: $radius-pill;
      display: flex; align-items: center;
      &::after { border: none; }
    }
  }
}
.action-bar {
  margin-top: auto;
  padding-top: $space-2;
  .btn {
    width: 100%;
    height: 88rpx;
    border-radius: $radius-pill;
    font-size: $font-base;
    display: flex; align-items: center; justify-content: center;
    &::after { border: none; }
  }
  .btn-primary { background: $color-primary; color: #fff; }
  .btn-secondary { background: $color-bg-page; color: $color-text-primary; }
  .btn-disabled { background: $color-divider; color: #fff; }
}
</style>
