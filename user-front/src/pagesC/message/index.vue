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
import { ref, computed } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';

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

const messages = ref<Message[]>([
  {
    id: 1,
    type: 'logistics',
    title: '订单已发货',
    desc: '您的订单 [202310249988] 已发货，预计明日送达，请保持电话畅通~',
    time: '10:30',
    avatar: '/static/images/logo.png',
    unread: true,
    link: '/pages/order/order'
  },
  {
    id: 2,
    type: 'system',
    title: '会员升级',
    desc: '恭喜您升级为 [黄金会员]，专属权益与优惠等您查看！',
    time: '昨天',
    avatar: '/static/images/logo.png',
    unread: true
  },
  {
    id: 3,
    type: 'promotion',
    title: '周末狂欢',
    desc: '全场车厘子满 199 减 50，速来抢购！',
    time: '星期三',
    avatar: '/static/images/logo.png',
    unread: false,
    link: '/pagesC/coupons/index'
  },
  {
    id: 4,
    type: 'logistics',
    title: '订单已签收',
    desc: '您的订单 [202310249987] 已签收，欢迎对果园好物作出评价。',
    time: '04-29',
    avatar: '/static/images/logo.png',
    unread: false,
    link: '/pagesC/evaluation/index'
  },
  {
    id: 5,
    type: 'system',
    title: '隐私政策更新',
    desc: '我们更新了隐私政策，详细内容请前往设置中心查看。',
    time: '04-25',
    avatar: '/static/images/logo.png',
    unread: false,
    link: '/pagesC/settings/index'
  }
]);

const activeType = ref<'all' | MsgType>('all');

const filteredMessages = computed(() => {
  if (activeType.value === 'all') return messages.value;
  return messages.value.filter(m => m.type === activeType.value);
});

function unreadCount(type: MsgType) {
  return messages.value.filter(m => m.type === type && m.unread).length;
}

function handleNavClick(type: 'all' | MsgType) {
  activeType.value = type;
}

function handleMessageTap(msg: Message) {
  msg.unread = false;
  if (msg.link) {
    if (msg.link.startsWith('/pages/')) {
      uni.switchTab({ url: msg.link });
    } else {
      uni.navigateTo({ url: msg.link });
    }
  }
}

function markAllRead() {
  filteredMessages.value.forEach(m => (m.unread = false));
  uni.showToast({ title: '已全部标为已读', icon: 'success' });
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
