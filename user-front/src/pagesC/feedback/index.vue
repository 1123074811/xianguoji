<template>
  <view class="page-container">
    <scroll-view scroll-y class="main-scroll">
      <!-- 反馈类型 -->
      <view class="section-title">反馈类型</view>
      <view class="card type-grid">
        <view
          v-for="t in types"
          :key="t.id"
          class="type-item"
          :class="{ active: form.type === t.id }"
          hover-class="row-active"
          @tap="form.type = t.id"
        >
          <svg-icon :name="t.icon" :size="40" :color="form.type === t.id ? '#2E7D32' : '#757575'" />
          <text class="name">{{ t.name }}</text>
        </view>
      </view>

      <!-- 描述 -->
      <view class="section-title">问题描述</view>
      <view class="card">
        <textarea
          class="textarea"
          v-model="form.content"
          placeholder="请详细描述您遇到的问题或建议，便于我们更好地为您处理（5-500字）"
          :maxlength="500"
        />
        <view class="char-count">
          <text :class="{ warning: form.content.length > 480 }">{{ form.content.length }}/500</text>
        </view>
      </view>

      <!-- 上传图片 -->
      <view class="section-title">上传图片（选填）</view>
      <view class="card upload-card">
        <view class="img-list">
          <view v-for="(img, i) in form.images" :key="i" class="img-item">
            <image :src="img" mode="aspectFill" class="img" />
            <view class="del-btn" @tap="removeImage(i)">
              <svg-icon name="close" :size="24" color="#FFFFFF" />
            </view>
          </view>
          <view v-if="form.images.length < 6" class="add-btn" @tap="chooseImage">
            <svg-icon name="add" :size="48" color="#BDBDBD" />
            <text class="hint">添加图片</text>
          </view>
        </view>
      </view>

      <!-- 联系方式 -->
      <view class="section-title">联系方式（选填）</view>
      <view class="card">
        <input
          class="input"
          v-model="form.contact"
          placeholder="手机号或邮箱，便于我们回复您"
        />
      </view>

      <!-- 提交按钮 -->
      <view class="submit-box">
        <view class="submit-btn" :class="{ disabled: !canSubmit }" hover-class="row-active" @tap="handleSubmit">提交反馈</view>
        <text class="hint">我们会在 1-3 个工作日内回复</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { reactive, computed, ref } from 'vue';
import { messageApi } from '@/pagesC/api/message';
import { useUserStore } from '@/stores/user';
import { API_BASE_URL } from '@/config/env';
import SvgIcon from '@/components/svg-icon.vue';

const BASE_URL = API_BASE_URL;
const uploading = ref(false);

const types = [
  { id: 'bug', name: '功能异常', icon: 'edit' },
  { id: 'product', name: '商品质量', icon: 'eco' },
  { id: 'shipping', name: '物流配送', icon: 'shipping' },
  { id: 'service', name: '客服服务', icon: 'chat' },
  { id: 'suggestion', name: '建议', icon: 'star' },
  { id: 'other', name: '其他', icon: 'category' }
];

const form = reactive({
  type: 'bug',
  content: '',
  images: [] as string[],
  contact: ''
});

const canSubmit = computed(() => form.content.trim().length >= 5);

function chooseImage() {
  uni.chooseImage({
    count: 6 - form.images.length,
    success: async (res) => {
      const paths = (res.tempFilePaths || []) as string[];
      uploading.value = true;
      try {
        for (const path of paths) {
          const url = await uploadImage(path);
          form.images.push(url);
        }
      } catch (e) {
        uni.showToast({ title: '图片上传失败', icon: 'none' });
      } finally {
        uploading.value = false;
      }
    }
  });
}

function uploadImage(filePath: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const userStore = useUserStore();
    uni.uploadFile({
      url: BASE_URL + '/api/u/file/upload',
      filePath,
      name: 'file',
      header: {
        Authorization: `Bearer ${userStore.token}`
      },
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 0 && data.data) {
              resolve(data.data);
            } else {
              reject(new Error(data.msg || '上传失败'));
            }
          } catch {
            reject(new Error('解析失败'));
          }
        } else {
          reject(new Error('HTTP ' + res.statusCode));
        }
      },
      fail: reject
    });
  });
}

function removeImage(i: number) {
  form.images.splice(i, 1);
}

async function handleSubmit() {
  if (!canSubmit.value) {
    uni.showToast({ title: '请填写至少 5 个字的描述', icon: 'none' });
    return;
  }
  if (uploading.value) {
    uni.showToast({ title: '图片上传中，请稍候', icon: 'none' });
    return;
  }
  uni.showLoading({ title: '提交中...' });
  try {
    await messageApi.submitFeedback({
      type: form.type,
      content: form.content,
      images: form.images.length ? form.images : undefined,
      contact: form.contact || undefined,
    });
    uni.hideLoading();
    uni.showToast({ title: '反馈提交成功', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1000);
  } catch (e) {
    uni.hideLoading();
    uni.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
  }
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.main-scroll {
  flex: 1;
  padding: $space-3 $space-4 $space-6;
}

.section-title {
  font-size: $font-sm;
  color: $color-text-secondary;
  margin: $space-4 $space-2 $space-2;
}

.card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $space-4;
  box-shadow: $shadow-card;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $space-3 $space-2;

  .type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-1;
    padding: $space-2 0;
    border-radius: $radius-sm;
    border: 2rpx solid $color-divider;
    transition: all .2s;

    &.active {
      border-color: $color-primary;
      background-color: $color-primary-bg;

      .name { color: $color-primary; font-weight: $weight-medium; }
    }

    .name {
      font-size: $font-sm;
      color: $color-text-primary;
    }
  }
}

.row-active { background-color: rgba(46,125,50,0.08); }

.textarea {
  width: 100%;
  min-height: 240rpx;
  font-size: $font-base;
  color: $color-text-primary;
  line-height: 1.6;
}

.char-count {
  text-align: right;
  font-size: $font-xs;
  color: $color-text-placeholder;
  margin-top: $space-1;

  .warning { color: $color-price; }
}

.upload-card .img-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $space-2;

  .img-item {
    position: relative;
    width: 100%;
    aspect-ratio: 1;

    .img {
      width: 100%;
      height: 100%;
      border-radius: $radius-sm;
      background-color: $color-bg-page;
    }

    .del-btn {
      position: absolute;
      top: -8rpx;
      right: -8rpx;
      width: 36rpx;
      height: 36rpx;
      border-radius: 50%;
      background-color: rgba(0,0,0,0.6);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .add-btn {
    width: 100%;
    aspect-ratio: 1;
    border: 2rpx dashed $color-divider;
    border-radius: $radius-sm;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4rpx;

    .hint {
      font-size: $font-xs;
      color: $color-text-placeholder;
    }
  }
}

.input {
  width: 100%;
  height: 72rpx;
  font-size: $font-base;
  color: $color-text-primary;
}

.submit-box {
  margin-top: $space-5;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $space-2;

  .submit-btn {
    width: 100%;
    height: 96rpx;
    background-color: $color-primary;
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-md;
    font-weight: $weight-medium;
    display: flex;
    align-items: center;
    justify-content: center;

    &.disabled {
      background-color: $color-divider;
      color: $color-text-placeholder;
    }
  }

  .hint {
    font-size: $font-xs;
    color: $color-text-secondary;
  }
}
</style>
