<template>
  <view class="page-container">
    <view class="message-nav">
      <view
        class="nav-item"
        :class="{ active: activeType === 'system' }"
        hover-class="btn-active"
        @tap="handleNavClick('system')"
      >
        <view class="icon-wrapper system">
          <svg-icon name="notification" :size="48" color="#FFFFFF" />
          <view v-if="unreadCount('system') > 0" class="badge">{{ unreadCount('system') }}</view>
        </view>
        <text class="label">系统通知</text>
      </view>
      <view
        class="nav-item"
        :class="{ active: activeType === 'promotion' }"
        hover-class="btn-active"
        @tap="handleNavClick('promotion')"
      >
        <view class="icon-wrapper promotion">
          <svg-icon name="gift" :size="48" color="#FFFFFF" />
          <view v-if="unreadCount('promotion') > 0" class="badge">{{ unreadCount('promotion') }}</view>
        </view>
        <text class="label">优惠活动</text>
      </view>
      <view
        class="nav-item"
        :class="{ active: activeType === 'logistics' }"
        hover-class="btn-active"
        @tap="handleNavClick('logistics')"
      >
        <view class="icon-wrapper logistics">
          <svg-icon name="shipping" :size="48" color="#FFFFFF" />
          <view v-if="unreadCount('logistics') > 0" class="badge">{{ unreadCount('logistics') }}</view>
        </view>
        <text class="label">交易物流</text>
      </view>
      <view
        class="nav-item"
        :class="{ active: activeType === 'all' }"
        hover-class="btn-active"
        @tap="handleNavClick('all')"
      >
        <view class="icon-wrapper all">
          <svg-icon name="chat" :size="48" color="#FFFFFF" />
        </view>
        <text class="label">全部</text>
      </view>
    </view>

    <view class="filter-bar">
      <text class="title">{{ titleMap[activeType] }}</text>
      <text v-if="filteredMessages.length > 0" class="action" @tap="markAllRead">全部已读</text>
    </view>

    <scroll-view scroll-y class="message-list" v-if="filteredMessages.length > 0">
      <view
        class="message-item"
        v-for="msg in filteredMessages"
        :key="msg.id"
        hover-class="btn-active"
        @tap="handleMessageTap(msg)"
      >
        <image class="avatar" :src="msg.avatar" mode="aspectFill" />
        <view class="content-wrapper">
          <view class="top-row">
            <text class="title">{{ msg.title }}</text>
            <text class="time">{{ msg.time }}</text>
          </view>
          <text class="desc">{{ msg.desc }}</text>
        </view>
        <view class="unread-dot" v-if="msg.unread"></view>
      </view>
    </scroll-view>

    <wd-status-tip
      v-else
      image="message"
      tip="暂无新消息"
      class="empty-box"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';
import { messageApi } from '@/api/modules/message';
import type { MessageVO } from '@/api/types/message';

type MsgType = 'system' | 'promotion' | 'logistics';

interface Message {
  id: number;
  type: MsgType;
  title: string;
  desc: string;
  time: string;
  avatar: string;
  unread: boolean;
  link?: string;
}

const titleMap: Record<string, string> = {
  all: '全部消息',
  system: '系统通知',
  promotion: '优惠活动',
  logistics: '交易物流'
};

const messages = ref<Message[]>([]);
const activeType = ref<'all' | MsgType>('all');

const filteredMessages = computed(() => {
  if (activeType.value === 'all') return messages.value;
  return messages.value.filter(m => m.type === activeType.value);
});

onMounted(loadMessages);

function unreadCount(type: MsgType) {
  return messages.value.filter(m => m.type === type && m.unread).length;
}

function handleNavClick(type: 'all' | MsgType) {
  activeType.value = type;
}

async function loadMessages() {
  try {
    const data = await messageApi.page({ page: 1, size: 100 });
    messages.value = data.list.map(mapMessage);
  } catch (e) {
    console.warn('消息加载失败', e);
    messages.value = [];
  }
}

async function handleMessageTap(msg: Message) {
  if (msg.unread) {
    msg.unread = false;
    messageApi.markRead(msg.id).catch(() => {});
  }
  if (msg.link) {
    if (msg.link.startsWith('/pages/')) {
      uni.switchTab({ url: msg.link });
    } else {
      uni.navigateTo({ url: msg.link });
    }
  }
}

async function markAllRead() {
  try {
    await messageApi.markAllRead();
    filteredMessages.value.forEach(m => (m.unread = false));
    uni.showToast({ title: '已全部标为已读', icon: 'success' });
  } catch (e) {
    console.warn('全部已读失败', e);
  }
}

function mapMessage(msg: MessageVO): Message {
  return {
    id: msg.id,
    type: mapMsgType(msg.type),
    title: msg.title,
    desc: msg.content,
    time: formatTime(msg.createdAt),
    avatar: '/static/images/wechat-logo.png',
    unread: msg.isRead === 0,
    link: msg.linkUrl,
  };
}

function mapMsgType(type: number): MsgType {
  if (type === 3) return 'promotion';
  if (type === 2 || type === 4) return 'logistics';
  return 'system';
}

function formatTime(value?: string) {
  if (!value) return '';
  return value.length > 10 ? value.slice(5, 16) : value;
}
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.message-nav {
  display: flex;
  justify-content: space-around;
  background-color: #ffffff;
  padding: $space-4 0;
  margin-bottom: $space-2;

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-2;
    transition: transform .2s;

    &.active {
      transform: translateY(-4rpx);

      .label { color: $color-primary; font-weight: $weight-semibold; }
    }

    .icon-wrapper {
      width: 96rpx;
      height: 96rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;

      &.system { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
      &.promotion { background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%); }
      &.logistics { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
      &.all { background: linear-gradient(135deg, #2E7D32 0%, #8BC34A 100%); }

      .badge {
        position: absolute;
        top: -4rpx;
        right: -4rpx;
        background-color: $color-price;
        color: #ffffff;
        font-size: 20rpx;
        min-width: 32rpx;
        height: 32rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 8rpx;
        border: 2rpx solid #ffffff;
      }
    }

    .label {
      font-size: $font-sm;
      color: $color-text-primary;
    }
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-3 $space-4 $space-2;

  .title {
    font-size: $font-sm;
    color: $color-text-secondary;
  }

  .action {
    font-size: $font-sm;
    color: $color-primary;
  }
}

.message-list {
  flex: 1;
  background-color: #ffffff;

  .message-item {
    display: flex;
    align-items: center;
    padding: $space-3 $space-4;
    position: relative;
    border-bottom: 2rpx solid $color-divider;

    .avatar {
      width: 96rpx;
      height: 96rpx;
      border-radius: 50%;
      background-color: $color-bg-page;
      margin-right: $space-3;
    }

    .content-wrapper {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;
      overflow: hidden;

      .top-row {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .title {
          font-size: $font-base;
          font-weight: $weight-medium;
          color: $color-text-primary;
        }

        .time {
          font-size: $font-xs;
          color: $color-text-secondary;
        }
      }

      .desc {
        font-size: $font-sm;
        color: $color-text-secondary;
        @include text-ellipsis;
      }
    }

    .unread-dot {
      position: absolute;
      top: 50%;
      right: $space-4;
      transform: translateY(-50%);
      width: 16rpx;
      height: 16rpx;
      background-color: $color-price;
      border-radius: 50%;
    }
  }
}

.empty-box {
  margin-top: 200rpx;
}
</style>

