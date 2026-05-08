<template>
  <view class="group-buy-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="left">
        <svg-icon name="category" :size="40" color="#757575" @click="goBack" />
        <text class="title">鲜果记</text>
      </view>
      <svg-icon name="chat" :size="40" color="#757575" />
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Hero Banner -->
      <view class="hero-section card no-padding overflow-hidden">
        <view class="hero-content">
          <text class="title">果园拼购更优惠</text>
          <text class="desc">邀好友参团，低至五折起</text>
        </view>
      </view>

      <!-- Guide -->
      <view class="guide-section card">
        <view class="guide-item">
          <view class="step-num">1</view>
          <text class="label">选择心仪水果</text>
        </view>
        <view class="guide-item">
          <view class="step-num">2</view>
          <text class="label">支付开团/参团</text>
        </view>
        <view class="guide-item">
          <view class="step-num">3</view>
          <text class="label">成团快速发货</text>
        </view>
      </view>

      <!-- Quick Join by Share Code -->
      <view class="card share-input-card">
        <text class="label">已有分享码？</text>
        <view class="row">
          <input v-model="shareCodeInput" class="input" placeholder="输入8位分享码" maxlength="12" />
          <button class="action-btn" @tap="goShareDetail">查看</button>
        </view>
      </view>

      <!-- Hot List -->
      <view class="list-section">
        <view class="section-header">
          <text class="title">热门拼团</text>
          <text class="more-btn">查看全部</text>
        </view>

        <view class="empty" v-if="!groupGoods.length">
          <text>暂无在售拼团活动</text>
        </view>

        <view class="group-list">
          <view v-for="item in groupGoods" :key="item.id" class="group-card card">
            <image :src="resolveImageUrl(item.mainImage)" mode="aspectFill" class="goods-img" />
            <view class="info">
              <view class="top">
                <text class="name">{{ item.productName }}</text>
                <view class="tags">
                  <text class="tag">{{ item.groupSize }}人团</text>
                  <text class="sales">已拼 {{ item.totalJoinCount }} 件 · 已成 {{ item.successCount }} 团</text>
                </view>
              </view>
              <view class="bottom">
                <view class="price-box">
                  <text class="price">¥{{ item.groupPrice }}</text>
                  <text class="original">¥{{ item.originalPrice }}</text>
                </view>
                <button class="join-btn" @tap="goGroupDetail(item)">去开团</button>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { promoApi } from '@/api/modules/promo';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import type { GroupBuyActivityVO } from '@/api/types/promo';

const groupGoods = ref<GroupBuyActivityVO[]>([]);
const shareCodeInput = ref('');

onMounted(async () => {
  try {
    const data = await promoApi.groupBuyPage({ page: 1, size: 20 });
    groupGoods.value = data.list;
  } catch (e) {
    console.warn('加载拼团活动失败', e);
  }
});

function goBack() {
  uni.navigateBack();
}

function goGroupDetail(item: GroupBuyActivityVO) {
  uni.navigateTo({ url: `/pagesA/goods-detail/index?id=${item.productId}` });
}

async function goShareDetail() {
  const code = shareCodeInput.value.trim().toUpperCase();
  if (!code) {
    uni.showToast({ title: '请输入分享码', icon: 'none' });
    return;
  }
  try {
    const inst = await promoApi.groupBuyByShareCode(code);
    uni.navigateTo({ url: `/pagesC/group-buy/share?code=${code}` });
  } catch (e) {
    uni.showToast({ title: '分享码无效或已过期', icon: 'none' });
  }
}
</script>

<style lang="scss" scoped>
.group-buy-container {
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
    .title { font-size: $font-lg; font-weight: bold; color: $color-primary; }
  }

  .iconfont { font-size: 40rpx; color: $color-text-secondary; }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: $space-4;
}

.hero-section {
  height: 352rpx;
  position: relative;
  margin-bottom: $space-4;

  .hero-img { width: 100%; height: 100%; }
  .hero-content {
    position: absolute;
    inset: 0;
    background: linear-gradient(to right, rgba(0,0,0,0.4), transparent);
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 0 $space-4;
    gap: 8rpx;

    .title { font-size: 40rpx; color: #ffffff; font-weight: bold; }
    .desc { font-size: $font-xs; color: rgba(255,255,255,0.9); }
  }
}

.guide-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  text-align: center;
  padding: $space-3 0;
  margin-bottom: $space-5;

  .guide-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-1;

    .step-num {
      width: 48rpx;
      height: 48rpx;
      background-color: $color-primary-bg;
      color: $color-primary;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      font-weight: bold;
    }

    .label { font-size: $font-xs; color: $color-text-primary; }
  }
}

.list-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $space-3;

    .title { font-size: $font-base; font-weight: bold; }
    .more-btn { font-size: $font-xs; color: $color-primary; }
  }
}

.share-input-card {
  margin-bottom: $space-4;
  padding: $space-3;
  display: flex;
  flex-direction: column;
  gap: $space-2;
  .label { font-size: $font-xs; color: $color-text-secondary; }
  .row { display: flex; gap: $space-2; align-items: center; }
  .input {
    flex: 1;
    height: 64rpx;
    padding: 0 $space-3;
    border-radius: $radius-pill;
    background: $color-bg-page;
    font-size: $font-sm;
    letter-spacing: 2rpx;
    text-transform: uppercase;
  }
  .action-btn {
    background: $color-primary;
    color: #fff;
    font-size: $font-xs;
    padding: 0 $space-4;
    height: 64rpx;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    &::after { border: none; }
  }
}

.empty {
  text-align: center;
  padding: $space-6 0;
  color: $color-text-placeholder;
  font-size: $font-xs;
}

.group-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  padding-bottom: 80rpx;
}

.group-card {
  display: flex;
  gap: $space-3;
  padding: $space-3;

  .goods-img { width: 224rpx; height: 224rpx; border-radius: $radius-sm; background-color: $color-bg-page; }
  
  .info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .top {
      .name { font-size: $font-base; font-weight: bold; color: $color-text-primary; @include text-ellipsis; }
      .tags {
        display: flex;
        align-items: center;
        gap: $space-1;
        margin-top: 8rpx;

        .tag { font-size: 18rpx; color: $color-primary; background-color: $color-primary-bg; padding: 2rpx $space-1; border-radius: $radius-pill; }
        .sales { font-size: 18rpx; color: $color-text-placeholder; }
      }
    }

    .bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price-box {
        display: flex;
        align-items: baseline;
        gap: 4rpx;
        .price { font-size: $font-md; font-weight: bold; color: $color-primary; }
        .original { font-size: $font-xs; color: $color-text-placeholder; text-decoration: line-through; }
      }

      .join-btn {
        background-color: $color-primary;
        color: #ffffff;
        font-size: 24rpx;
        padding: 0 $space-4;
        height: 56rpx;
        border-radius: $radius-pill;
        display: flex;
        align-items: center;
        &::after { border: none; }
      }
    }
  }
}
</style>
