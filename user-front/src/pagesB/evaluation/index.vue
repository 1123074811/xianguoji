<template>
  <view class="evaluation-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="left">
        <svg-icon name="arrow-back" :size="40" color="#757575" @click="goBack" />
        <text class="title">评价中心</text>
      </view>
      <view style="width: 40rpx;"></view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Form Section -->
      <view class="form-section card">
        <view class="goods-info">
          <view class="info">
            <text class="name">评价订单 {{ orderNo }}</text>
          </view>
        </view>

        <view class="rating-box">
          <text class="label">为商品打分</text>
          <view class="stars">
            <svg-icon 
              v-for="i in 5" 
              :key="i" 
              name="star" 
              :size="64" 
              :color="i <= rating ? '#2E7D32' : '#EEEEEE'"
              @click="rating = i"
            />
          </view>
        </view>

        <textarea 
          class="content-input" 
          placeholder="分享您的购物心得，帮助更多果友挑选好货..." 
          v-model="content"
        ></textarea>

        <view class="upload-grid">
          <view class="upload-btn" @tap="handleUpload">
            <svg-icon name="chat" :size="48" color="#BDBDBD" />
            <text class="text">添加图片</text>
          </view>
          <view v-for="(img, index) in images" :key="index" class="img-wrapper">
            <image :src="img" mode="aspectFill" class="upload-img" />
            <view class="close-btn" @tap="removeImg(index)">
              <svg-icon name="close" :size="20" color="#FFFFFF" />
            </view>
          </view>
        </view>

        <view class="footer-row">
          <view class="anonymous" @tap="isAnonymous = !isAnonymous">
            <view class="checkbox" :class="{ checked: isAnonymous }">
              <svg-icon v-if="isAnonymous" name="check" :size="20" color="#FFFFFF" />
            </view>
            <text class="label">匿名评价</text>
          </view>
          <button class="submit-btn" @tap="handleSubmit">发布评价</button>
        </view>
      </view>

      <!-- Review List -->
      <view class="review-section">
        <view class="section-header">
          <view class="left">
            <text class="title">商品评价 (128)</text>
            <view class="rating-info">
              <view class="stars">
                <svg-icon name="star" :size="24" color="#2E7D32" v-for="i in 5" :key="i" />
              </view>
              <text class="score">4.8</text>
              <text class="label">满意度</text>
            </view>
          </view>
        </view>
        
        <view class="tags-row">
          <view v-for="tag in tags" :key="tag.name" class="tag" :class="{ active: tag.active }">
            {{ tag.name }} ({{ tag.count }})
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { reviewApi } from '@/pagesB/api/review';
import SvgIcon from '@/components/svg-icon.vue';

const orderId = ref(0);
const orderItemId = ref(0);
const rating = ref(5);
const content = ref('');
const images = ref<string[]>([]);
const isAnonymous = ref(true);

onMounted(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  orderId.value = Number(page?.options?.orderId || 0);
  orderItemId.value = Number(page?.options?.orderItemId || 0);
});

const tags = [
  { name: '全部', count: 0, active: true },
  { name: '有图', count: 0, active: false },
  { name: '好评', count: 0, active: false },
  { name: '最新', count: 0, active: false }
];

function goBack() {
  uni.navigateBack();
}

function handleUpload() {
  uni.chooseImage({
    count: 9 - images.value.length,
    success: (res) => {
      images.value = [...images.value, ...res.tempFilePaths];
    }
  });
}

function removeImg(index: number) {
  images.value.splice(index, 1);
}

async function handleSubmit() {
  if (!content.value.trim()) {
    return uni.showToast({ title: '请输入评价内容', icon: 'none' });
  }
  if (!orderId.value || !orderItemId.value) {
    return uni.showToast({ title: '缺少订单评价信息', icon: 'none' });
  }
  try {
    await reviewApi.submit({
      orderId: orderId.value,
      orderItemId: orderItemId.value,
      rating: rating.value,
      freshnessRating: rating.value,
      valueRating: rating.value,
      packageRating: rating.value,
      content: content.value,
      images: images.value,
      isAnonymous: isAnonymous.value ? 1 : 0,
    });
    uni.showToast({ title: '评价发布成功', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1500);
  } catch (e) {
    console.warn('评价提交失败', e);
  }
}
</script>

<style lang="scss" scoped>
.evaluation-container {
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

  .iconfont { font-size: 40rpx; color: $color-text-secondary; }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: $space-4;
}

.form-section {
  .goods-info {
    display: flex;
    align-items: center;
    gap: $space-3;
    margin-bottom: $space-5;

    .goods-img { width: 128rpx; height: 128rpx; border-radius: $radius-md; background-color: $color-bg-page; }
    .info {
      .name { font-size: $font-base; font-weight: bold; color: $color-text-primary; }
      .specs { font-size: $font-xs; color: $color-text-placeholder; }
    }
  }

  .rating-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $space-4 0;
    border-top: 2rpx solid rgba($color-divider, 0.3);
    border-bottom: 2rpx solid rgba($color-divider, 0.3);
    margin-bottom: $space-4;

    .label { font-size: $font-sm; color: $color-text-secondary; margin-bottom: $space-2; }
    .stars {
      display: flex;
      gap: $space-2;
      .iconfont {
        font-size: 64rpx;
        color: $color-divider;
        &.active { color: $color-primary; }
      }
    }
  }

  .content-input {
    width: 100%;
    height: 256rpx;
    background-color: $color-bg-page;
    padding: $space-3;
    border-radius: $radius-md;
    font-size: $font-base;
    margin-bottom: $space-4;
  }

  .upload-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: $space-3;
    margin-bottom: $space-5;

    .upload-btn {
      // 微信小程序不支持 aspect-ratio
      height: 0;
      padding-bottom: 100%;
      position: relative;
      background-color: $color-bg-page;
      border: 2rpx dashed $color-divider;
      border-radius: $radius-md;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: $color-text-placeholder;
      
      .iconfont { font-size: 48rpx; }
      .text { font-size: 20rpx; margin-top: 4rpx; }
    }

    .img-wrapper {
      // 微信小程序不支持 aspect-ratio
      height: 0;
      padding-bottom: 100%;
      position: relative;
      .upload-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border-radius: $radius-md; }
      .close-btn {
        position: absolute;
        top: 4rpx;
        right: 4rpx;
        width: 32rpx;
        height: 32rpx;
        background-color: rgba(0,0,0,0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        .iconfont { font-size: 20rpx; }
      }
    }
  }

  .footer-row {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .anonymous {
      display: flex;
      align-items: center;
      gap: $space-2;

      .checkbox {
        width: 32rpx;
        height: 32rpx;
        border: 2rpx solid $color-primary;
        border-radius: 4rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        &.checked { background-color: $color-primary; }
        .iconfont { color: #ffffff; font-size: 20rpx; }
      }

      .label { font-size: $font-sm; color: $color-text-secondary; }
    }

    .submit-btn {
      background-color: $color-primary;
      color: #ffffff;
      padding: 0 $space-5;
      height: 72rpx;
      border-radius: $radius-pill;
      font-size: $font-base;
      font-weight: bold;
      &::after { border: none; }
    }
  }
}

.review-section {
  .section-header {
    margin-bottom: $space-4;
    .title { font-size: $font-md; font-weight: bold; }
    .rating-info {
      display: flex;
      align-items: center;
      gap: $space-2;
      margin-top: $space-1;
      
      .stars {
        display: flex;
        .iconfont { font-size: 24rpx; color: $color-primary; }
      }
      .score { font-size: $font-md; font-weight: bold; color: $color-primary; }
      .label { font-size: $font-xs; color: $color-text-secondary; }
    }
  }

  .tags-row {
    display: flex;
    flex-wrap: wrap;
    gap: $space-2;
    
    .tag {
      padding: 8rpx $space-3;
      background-color: $color-bg-card;
      border-radius: $radius-pill;
      font-size: $font-xs;
      color: $color-text-secondary;
      
      &.active {
        background-color: $color-primary;
        color: #ffffff;
      }
    }
  }
}
</style>
