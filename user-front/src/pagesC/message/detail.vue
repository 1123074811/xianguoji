<template>
  <view class="page-container">
    <view v-if="message" class="detail-card">
      <view class="header-row">
        <view class="type-icon" :class="typeKey">{{ typeSymbol }}</view>
        <view class="title-wrap">
          <text class="title">{{ message.title }}</text>
          <text class="time">{{ formatTime(message.createdAt) }}</text>
        </view>
      </view>

      <view class="content-box">
        <text class="content">{{ message.content }}</text>
      </view>

      <button v-if="message.linkUrl" class="link-btn" @tap="goLink">查看相关内容</button>
    </view>

    <view v-else class="empty-box">
      <text class="empty-symbol">💬</text>
      <text>{{ loading ? '加载中...' : '消息不存在或已删除' }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { messageApi } from '@/pagesC/api/message';
import type { MessageVO } from '@/api/types/message';

type MsgType = 'system' | 'promotion' | 'logistics' | 'chat';

const loading = ref(false);
const message = ref<MessageVO | null>(null);

const typeKey = computed<MsgType>(() => mapMsgType(message.value?.type || 1));
const typeSymbol = computed(() => {
  if (typeKey.value === 'promotion') return '礼';
  if (typeKey.value === 'logistics') return '运';
  if (typeKey.value === 'chat') return '客';
  return '通';
});

onMounted(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  const opts = page?.options || {};
  const id = Number(opts.id || 0);
  if (id) {
    loadMessage(id);
  } else {
    loading.value = false;
    message.value = null;
  }
});

async function loadMessage(id: number) {
  loading.value = true;
  try {
    const data = await messageApi.detail(id);
    message.value = data;
    if (data.isRead === 0) {
      messageApi.markRead(id).catch(() => {});
    }
  } catch (e) {
    console.warn('消息详情加载失败', e);
    message.value = null;
  } finally {
    loading.value = false;
  }
}

function mapMsgType(type: number): MsgType {
  if (type === 3) return 'promotion';
  if (type === 2 || type === 4) return 'logistics';
  if (type === 6) return 'chat';
  return 'system';
}

function goLink() {
  const link = message.value?.linkUrl;
  if (!link) return;
  if (link === '/pagesC/chat/index') {
    uni.navigateTo({ url: link });
    return;
  }
  if (link.startsWith('/pages/')) {
    uni.switchTab({ url: link });
  } else {
    uni.navigateTo({ url: link });
  }
}

function formatTime(value?: string) {
  if (!value) return '';
  return value.replace('T', ' ').slice(0, 16);
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $space-4;
}

.loading-box,
.empty-box {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $space-3;
  font-size: $font-sm;
  color: $color-text-secondary;
}

.empty-symbol {
  font-size: 88rpx;
  line-height: 1;
}

.detail-card {
  background-color: #ffffff;
  border-radius: $radius-lg;
  padding: $space-4;
  box-shadow: $shadow-card;
}

.header-row {
  display: flex;
  align-items: center;
  gap: $space-3;
  padding-bottom: $space-4;
  border-bottom: 2rpx solid $color-divider;
}

.type-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #ffffff;
  font-size: 34rpx;
  font-weight: $weight-semibold;

  &.system { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  &.promotion { background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%); }
  &.logistics { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
  &.chat { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
}

.title-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $space-1;
}

.title {
  font-size: $font-lg;
  font-weight: $weight-semibold;
  color: $color-text-primary;
}

.time {
  font-size: $font-xs;
  color: $color-text-secondary;
}

.content-box {
  padding: $space-5 0;
}

.content {
  font-size: $font-base;
  color: $color-text-primary;
  line-height: 1.8;
  white-space: pre-wrap;
}

.link-btn {
  height: 80rpx;
  line-height: 80rpx;
  border-radius: $radius-pill;
  background-color: $color-primary;
  color: #ffffff;
  font-size: $font-base;
  font-weight: $weight-semibold;
  border: none;

  &::after { border: none; }
}
</style>
