<template>
  <view class="share-page">
    <!-- 顶部邀请横幅 -->
    <view class="invite-banner">
      <view class="invite-bg"></view>
      <view class="invite-content">
        <image
          v-if="instance?.leaderAvatar"
          :src="resolveImageUrl(instance.leaderAvatar)"
          class="leader-avatar"
        />
        <view v-else class="leader-avatar placeholder">
          {{ instance?.leaderName?.charAt(0) || '?' }}
        </view>
        <view class="invite-text">
          <text class="leader-name">{{ instance?.leaderName || '好友' }}</text>
          <text class="invite-msg">邀请你一起拼</text>
        </view>
      </view>
    </view>

    <!-- 商品信息卡片 -->
    <view class="product-card" @tap="goProductDetail">
      <image
        v-if="instance?.mainImage"
        :src="resolveImageUrl(instance.mainImage)"
        mode="aspectFill"
        class="product-img"
      />
      <view class="product-info">
        <text class="product-name">{{ instance?.productName || '拼团商品' }}</text>
        <view class="price-row">
          <text class="group-price">¥{{ instance?.groupPrice || '--' }}</text>
          <text class="original-price">¥{{ activity?.originalPrice || '--' }}</text>
          <view class="size-tag">{{ instance?.targetSize || '?' }}人团</view>
        </view>
        <view class="save-row" v-if="saveAmount">
          <text class="save-tag">拼团立省 ¥{{ saveAmount }}</text>
        </view>
      </view>
      <view class="arrow-wrap">
        <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
      </view>
    </view>

    <!-- 拼团进度 -->
    <view class="progress-card" v-if="instance">
      <view class="progress-header">
        <text class="label">拼团进度</text>
        <text class="count">{{ instance.currentSize }}/{{ instance.targetSize }}</text>
      </view>
      <view class="progress-bar-wrap">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
      </view>
      <text class="remaining-text" v-if="instance.status === 1">
        还差 <text class="highlight">{{ remaining }}</text> 人成团
      </text>
      <text class="countdown-text" v-if="instance.status === 1 && countdown">
        剩余 {{ countdown }} 结束
      </text>
    </view>

    <!-- 参团成员 -->
    <view class="members-card" v-if="instance">
      <text class="section-title">参团成员</text>
      <view class="member-list">
        <view v-for="p in instance.participants" :key="p.userId" class="member">
          <image :src="p.avatar ? resolveImageUrl(p.avatar) : defaultAvatar" class="avatar" />
          <text class="name">{{ p.nickname || '匿名' }}</text>
          <text v-if="p.isLeader" class="leader-tag">团长</text>
        </view>
        <view v-for="i in emptySlots" :key="'e'+i" class="member empty">
          <view class="avatar empty-avatar">?</view>
          <text class="name">待加入</text>
        </view>
      </view>
    </view>

    <!-- 分享码卡片 -->
    <view class="code-card" v-if="instance?.shareCode">
      <view class="code-header">
        <svg-icon name="share" :size="32" color="#2E7D32" />
        <text class="code-title">分享码</text>
      </view>
      <view class="code-display">
        <text class="code-text">{{ instance.shareCode }}</text>
        <button class="copy-btn" @tap="copyShareCode">复制</button>
      </view>
      <text class="code-hint">将分享码发送给好友，好友在拼团专区输入即可参团</text>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <template v-if="instance?.status === 1 && !meIsParticipant">
        <button class="btn btn-share" open-type="share">邀请好友参团</button>
        <button class="btn btn-join" @tap="goJoin">我要参团</button>
      </template>
      <template v-else-if="instance?.status === 1">
        <button class="btn btn-share" open-type="share">邀请好友参团</button>
        <button class="btn btn-disabled" disabled>已在拼团中</button>
      </template>
      <template v-else-if="instance?.status === 2">
        <button class="btn btn-secondary" @tap="goProductDetail">查看商品</button>
      </template>
      <template v-else-if="instance?.status === 3">
        <button class="btn btn-secondary" @tap="goProductDetail">查看其他拼团</button>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { onShareAppMessage } from '@dcloudio/uni-app';
import { promoApi } from '@/api/modules/promo';
import { catalogApi } from '@/api/modules/catalog';
import { useUserStore } from '@/stores/user';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import type { GroupBuyInstanceVO, GroupBuyActivityVO } from '@/api/types/promo';
import type { ProductDetailVO } from '@/api/types/catalog';

const instance = ref<GroupBuyInstanceVO | null>(null);
const activity = ref<GroupBuyActivityVO | null>(null);
const product = ref<ProductDetailVO | null>(null);
const userStore = useUserStore();
const now = ref(Date.now());
const defaultAvatar = '/static/avatar-default.png';
let timer: any = null;

const remaining = computed(() => {
  if (!instance.value) return 0;
  return Math.max(0, (instance.value.targetSize || 0) - (instance.value.currentSize || 0));
});

const emptySlots = computed(() => Math.max(0, remaining.value));

const progressPercent = computed(() => {
  if (!instance.value || !instance.value.targetSize) return 0;
  return Math.min(100, Math.round((instance.value.currentSize / instance.value.targetSize) * 100));
});

const saveAmount = computed(() => {
  if (!activity.value) return '';
  const group = Number(activity.value.groupPrice || 0);
  const orig = Number(activity.value.originalPrice || 0);
  const diff = orig - group;
  return diff > 0 ? diff.toFixed(0) : '';
});

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

async function loadByShareCode(code: string) {
  try {
    instance.value = await promoApi.groupBuyByShareCode(code);
    await loadRelated();
  } catch (e) {
    uni.showToast({ title: '分享码无效或已过期', icon: 'none' });
  }
}

async function loadByInstanceId(id: number) {
  try {
    instance.value = await promoApi.groupBuyDetail(id);
    await loadRelated();
  } catch (e) {
    uni.showToast({ title: '加载拼团失败', icon: 'none' });
  }
}

async function loadRelated() {
  if (!instance.value) return;
  const productId = instance.value.productId;
  if (productId) {
    try {
      product.value = await catalogApi.productDetail(productId);
    } catch { /* ignore */ }
    try {
      activity.value = await promoApi.groupBuyByProduct(productId);
    } catch { /* ignore */ }
  }
}

function copyShareCode() {
  if (!instance.value?.shareCode) return;
  uni.setClipboardData({ data: instance.value.shareCode });
}

async function goJoin() {
  if (!instance.value) return;
  uni.navigateTo({ url: `/pagesB/checkout/index?groupBuyInstanceId=${instance.value.id}` });
}

function goProductDetail() {
  const pid = instance.value?.productId || product.value?.id;
  if (pid) {
    uni.navigateTo({ url: `/pagesA/goods-detail/index?id=${pid}` });
  }
}

onShareAppMessage(() => {
  const name = instance.value?.productName || '鲜果记拼团';
  const price = instance.value?.groupPrice || '';
  const image = instance.value?.mainImage ? resolveImageUrl(instance.value.mainImage) : '';
  const code = instance.value?.shareCode || '';
  const leader = instance.value?.leaderName || '好友';
  return {
    title: `${leader}邀请你一起拼${name}，仅¥${price}`,
    path: `/pagesC/group-buy/share?code=${code}`,
    imageUrl: image,
  };
});

onMounted(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  const opts = page?.options || {};
  const code = opts.code || opts.shareCode || '';
  const id = Number(opts.id || opts.instanceId || 0);
  if (code) {
    loadByShareCode(code);
  } else if (id) {
    loadByInstanceId(id);
  }
  timer = setInterval(() => { now.value = Date.now(); }, 1000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style lang="scss" scoped>
.share-page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 160rpx;
}

/* 邀请横幅 */
.invite-banner {
  position: relative;
  overflow: hidden;

  .invite-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, $color-primary 0%, #1B5E20 100%);
  }

  .invite-content {
    position: relative;
    display: flex;
    align-items: center;
    gap: $space-3;
    padding: $space-5 $space-4;
  }

  .leader-avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.6);
    background: rgba(255, 255, 255, 0.2);
    flex-shrink: 0;

    &.placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: $font-lg;
      font-weight: bold;
    }
  }

  .invite-text {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  .leader-name {
    color: #fff;
    font-size: $font-lg;
    font-weight: bold;
  }

  .invite-msg {
    color: rgba(255, 255, 255, 0.85);
    font-size: $font-sm;
  }
}

/* 商品卡片 */
.product-card {
  margin: $space-3;
  background: #fff;
  border-radius: $radius-md;
  padding: $space-3;
  display: flex;
  gap: $space-3;
  align-items: center;
  box-shadow: $shadow-card;

  .product-img {
    width: 180rpx;
    height: 180rpx;
    border-radius: $radius-sm;
    background: $color-bg-page;
    flex-shrink: 0;
  }

  .product-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: $space-2;
  }

  .product-name {
    font-size: $font-base;
    font-weight: bold;
    color: $color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .price-row {
    display: flex;
    align-items: baseline;
    gap: $space-2;
  }

  .group-price {
    font-size: $font-price;
    color: $color-primary;
    font-weight: bold;
  }

  .original-price {
    font-size: $font-xs;
    color: $color-text-placeholder;
    text-decoration: line-through;
  }

  .size-tag {
    font-size: 20rpx;
    color: $color-primary;
    background: $color-primary-bg;
    padding: 2rpx $space-2;
    border-radius: $radius-pill;
  }

  .save-row {
    margin-top: -4rpx;
  }

  .save-tag {
    font-size: 20rpx;
    color: #fff;
    background: $color-price;
    padding: 2rpx $space-2;
    border-radius: $radius-sm;
  }

  .arrow-wrap {
    flex-shrink: 0;
  }
}

/* 拼团进度 */
.progress-card {
  margin: 0 $space-3 $space-3;
  background: #fff;
  border-radius: $radius-md;
  padding: $space-4;
  box-shadow: $shadow-card;

  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-3;

    .label {
      font-size: $font-base;
      font-weight: bold;
      color: $color-text-primary;
    }

    .count {
      font-size: $font-sm;
      color: $color-primary;
      font-weight: bold;
    }
  }

  .progress-bar-wrap {
    margin-bottom: $space-2;
  }

  .progress-bar {
    height: 16rpx;
    background: $color-bg-page;
    border-radius: 8rpx;
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, $color-primary, #66BB6A);
    border-radius: 8rpx;
    transition: width 0.3s ease;
  }

  .remaining-text {
    font-size: $font-sm;
    color: $color-text-secondary;

    .highlight {
      color: $color-primary;
      font-weight: bold;
      font-size: $font-md;
    }
  }

  .countdown-text {
    display: block;
    font-size: $font-xs;
    color: $color-text-placeholder;
    margin-top: 4rpx;
  }
}

/* 参团成员 */
.members-card {
  margin: 0 $space-3 $space-3;
  background: #fff;
  border-radius: $radius-md;
  padding: $space-4;
  box-shadow: $shadow-card;

  .section-title {
    display: block;
    font-size: $font-base;
    font-weight: bold;
    color: $color-text-primary;
    margin-bottom: $space-3;
  }

  .member-list {
    display: flex;
    flex-wrap: wrap;
    gap: $space-3;
  }

  .member {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4rpx;
    width: 120rpx;

    .avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 50%;
      background: $color-bg-page;
    }

    .empty-avatar {
      display: flex;
      align-items: center;
      justify-content: center;
      color: $color-text-placeholder;
      border: 2rpx dashed $color-divider;
    }

    .name {
      font-size: 20rpx;
      color: $color-text-secondary;
      max-width: 120rpx;
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

    &.empty .name {
      color: $color-text-placeholder;
    }
  }
}

/* 分享码卡片 */
.code-card {
  margin: 0 $space-3 $space-3;
  background: #fff;
  border-radius: $radius-md;
  padding: $space-4;
  box-shadow: $shadow-card;

  .code-header {
    display: flex;
    align-items: center;
    gap: $space-2;
    margin-bottom: $space-3;
  }

  .code-title {
    font-size: $font-base;
    font-weight: bold;
    color: $color-text-primary;
  }

  .code-display {
    display: flex;
    align-items: center;
    gap: $space-3;
    background: $color-bg-page;
    border-radius: $radius-md;
    padding: $space-3 $space-4;
    margin-bottom: $space-2;
  }

  .code-text {
    flex: 1;
    font-family: monospace;
    font-size: $font-lg;
    letter-spacing: 4rpx;
    color: $color-primary;
    font-weight: bold;
  }

  .copy-btn {
    background: $color-primary;
    color: #fff;
    font-size: $font-xs;
    padding: 0 $space-3;
    height: 56rpx;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    &::after { border: none; }
  }

  .code-hint {
    font-size: $font-xs;
    color: $color-text-placeholder;
  }
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: $space-2;
  padding: $space-3 $space-4;
  background: #fff;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.06);
  padding-bottom: calc(#{$space-3} + env(safe-area-inset-bottom));

  .btn {
    flex: 1;
    height: 88rpx;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after { border: none; }
  }

  .btn-share {
    background: $color-primary-bg;
    color: $color-primary;
    border: 2rpx solid rgba($color-primary, 0.3);
  }

  .btn-join {
    background: $color-primary;
    color: #fff;
  }

  .btn-disabled {
    background: $color-divider;
    color: #fff;
  }

  .btn-secondary {
    background: $color-bg-page;
    color: $color-text-primary;
    border: 2rpx solid $color-divider;
  }
}
</style>
