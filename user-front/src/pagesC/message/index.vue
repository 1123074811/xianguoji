<template>
  <view class="page-container">
    <view class="message-nav">
      <view
        class="nav-item"
        v-for="item in navItems"
        :key="item.type"
        :class="{ active: activeType === item.type }"
        hover-class="btn-active"
        @tap="handleNavClick(item.type)"
      >
        <view class="icon-wrapper" :class="item.type">
          <svg-icon :name="item.icon" :size="48" color="#FFFFFF" />
          <view v-if="unreadCount(item.type) > 0" class="badge">{{ displayBadge(item.type) }}</view>
        </view>
        <text class="label">{{ item.label }}</text>
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
        <image v-if="msg.type === 'chat'" class="avatar" :src="CHAT_AVATAR" mode="aspectFill" />
        <view v-else class="avatar-icon" :class="msg.type">
          <svg-icon :name="messageIcon(msg.type)" :size="44" color="#FFFFFF" />
        </view>
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

    <view v-else class="empty-box">
      <view class="empty-icon">
        <svg-icon name="chat" :size="72" color="#BDBDBD" />
      </view>
      <text class="empty-text">暂无新消息</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import SvgIcon from '@/components/svg-icon.vue';
import { messageApi } from '@/pagesC/api/message';
import type { MessageVO, MessageUnreadCounts } from '@/api/types/message';

type MsgType = 'system' | 'promotion' | 'logistics' | 'chat';
type NavType = 'all' | MsgType;

interface Message {
  id: number;
  type: MsgType;
  title: string;
  desc: string;
  time: string;
  unread: boolean;
  link?: string;
}

const titleMap: Record<string, string> = {
  all: '全部消息',
  system: '系统通知',
  promotion: '优惠活动',
  logistics: '交易物流',
  chat: '客服消息'
};

const CHAT_AVATAR = '/static/images/logo.png';

const navItems: { type: NavType; label: string; icon: string }[] = [
  { type: 'all', label: '全部消息', icon: 'chat' },
  { type: 'system', label: '系统通知', icon: 'notification' },
  { type: 'promotion', label: '优惠活动', icon: 'gift' },
  { type: 'logistics', label: '交易物流', icon: 'shipping' },
  { type: 'chat', label: '客服消息', icon: 'chat' },
];

const messages = ref<Message[]>([]);
const activeType = ref<NavType>('all');
const unreadCounts = ref<MessageUnreadCounts>({
  all: 0,
  system: 0,
  promotion: 0,
  logistics: 0,
  chat: 0,
});

const filteredMessages = computed(() => {
  if (activeType.value === 'all') return messages.value;
  return messages.value.filter(m => m.type === activeType.value);
});

onShow(() => {
  loadMessages();
});

function unreadCount(type: NavType) {
  return unreadCounts.value[type] || 0;
}

function displayBadge(type: NavType) {
  const count = unreadCount(type);
  return count > 99 ? '99+' : String(count);
}

function handleNavClick(type: NavType) {
  activeType.value = type;
  loadMessages();
}

async function loadMessages() {
  try {
    const type = messageTypeParam(activeType.value);
    const data = await messageApi.page({ page: 1, size: 100, ...(type ? { type } : {}) });
    messages.value = data.list.map(mapMessage);
    await loadUnreadCounts();
  } catch (e) {
    console.warn('消息加载失败', e);
    messages.value = [];
  }
}

async function loadUnreadCounts() {
  try {
    const data = await messageApi.unreadCounts();
    unreadCounts.value = {
      all: data.all || 0,
      system: data.system || 0,
      promotion: data.promotion || 0,
      logistics: data.logistics || 0,
      chat: data.chat || 0,
    };
  } catch (e) {
    const system = messages.value.filter(m => m.type === 'system' && m.unread).length;
    const promotion = messages.value.filter(m => m.type === 'promotion' && m.unread).length;
    const logistics = messages.value.filter(m => m.type === 'logistics' && m.unread).length;
    const chat = messages.value.filter(m => m.type === 'chat' && m.unread).length;
    unreadCounts.value = {
      system,
      promotion,
      logistics,
      chat,
      all: system + promotion + logistics + chat,
    };
  }
}

async function handleMessageTap(msg: Message) {
  if (msg.unread) {
    msg.unread = false;
    decrementUnread(msg.type);
    messageApi.markRead(msg.id).then(loadUnreadCounts).catch(loadUnreadCounts);
  }
  if (msg.type === 'chat') {
    uni.navigateTo({ url: '/pagesC/chat/index' });
    return;
  }
  if (msg.type === 'system') {
    uni.navigateTo({ url: `/pagesC/message/detail?id=${msg.id}` });
    return;
  }
  if (msg.link) {
    if (msg.link.startsWith('/pages/')) {
      uni.switchTab({ url: msg.link });
    } else {
      uni.navigateTo({ url: msg.link });
    }
    return;
  }
  uni.navigateTo({ url: `/pagesC/message/detail?id=${msg.id}` });
}

async function markAllRead() {
  try {
    await messageApi.markAllRead(messageTypeParam(activeType.value));
    filteredMessages.value.forEach(m => (m.unread = false));
    await loadUnreadCounts();
    uni.showToast({ title: '已全部标为已读', icon: 'success' });
  } catch (e) {
    console.warn('全部已读失败', e);
  }
}

function mapMessage(msg: MessageVO): Message {
  const type = mapMsgType(msg.type);
  return {
    id: msg.id,
    type,
    title: msg.title,
    desc: msg.content,
    time: formatTime(msg.createdAt),
    unread: msg.isRead === 0,
    link: msg.linkUrl,
  };
}

function mapMsgType(type: number): MsgType {
  if (type === 3) return 'promotion';
  if (type === 2 || type === 4) return 'logistics';
  if (type === 6) return 'chat';
  return 'system';
}

function messageTypeParam(type: NavType) {
  if (type === 'promotion') return 3;
  if (type === 'logistics') return 2;
  if (type === 'chat') return 6;
  if (type === 'system') return 1;
  return undefined;
}

function messageIcon(type: MsgType) {
  if (type === 'promotion') return 'gift';
  if (type === 'logistics') return 'shipping';
  if (type === 'chat') return 'chat';
  return 'notification';
}

function decrementUnread(type: MsgType) {
  unreadCounts.value[type] = Math.max(0, unreadCounts.value[type] - 1);
  unreadCounts.value.all = Math.max(0, unreadCounts.value.all - 1);
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
      &.chat { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
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

    .avatar-icon {
      width: 96rpx;
      height: 96rpx;
      border-radius: 50%;
      margin-right: $space-3;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      &.system { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
      &.promotion { background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%); }
      &.logistics { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
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
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $space-3;
  padding-top: 120rpx;

  .empty-icon {
    width: 128rpx;
    height: 128rpx;
    border-radius: 50%;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-card;
  }

  .empty-text {
    font-size: $font-sm;
    color: $color-text-secondary;
  }
}
</style>

