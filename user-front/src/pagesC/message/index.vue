<template>
  <view class="page-container">
    <view class="message-nav">
      <view class="nav-item" hover-class="btn-active" @tap="handleNavClick('system')">
        <view class="icon-wrapper system">
          <svg-icon name="notification" :size="48" color="#FFFFFF" />
        </view>
        <text class="label">系统通知</text>
      </view>
      <view class="nav-item" hover-class="btn-active" @tap="handleNavClick('promotion')">
        <view class="icon-wrapper promotion">
          <svg-icon name="gift" :size="48" color="#FFFFFF" />
          <view class="badge">1</view>
        </view>
        <text class="label">优惠活动</text>
      </view>
      <view class="nav-item" hover-class="btn-active" @tap="handleNavClick('logistics')">
        <view class="icon-wrapper logistics">
          <svg-icon name="shipping" :size="48" color="#FFFFFF" />
        </view>
        <text class="label">交易物流</text>
      </view>
    </view>

    <scroll-view scroll-y class="message-list" v-if="messages.length > 0">
      <view class="message-item" v-for="msg in messages" :key="msg.id" hover-class="btn-active">
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
import { ref } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';

const messages = ref([
  {
    id: 1,
    title: '鲜果记官方客服',
    desc: '您的订单 [1234567890] 已经发货啦，请注意查收哦~',
    time: '10:30',
    avatar: '/static/images/logo.png',
    unread: true
  },
  {
    id: 2,
    title: '系统通知',
    desc: '恭喜您升级为 [黄金会员]，快去查看您的专属特权吧！',
    time: '昨天',
    avatar: '/static/images/logo.png',
    unread: false
  },
  {
    id: 3,
    title: '优惠活动',
    desc: '周末狂欢！全场车厘子满199减50，速来抢购！',
    time: '星期三',
    avatar: '/static/images/logo.png',
    unread: false
  }
]);

function handleNavClick(type: string) {
  uni.showToast({ title: '开发中', icon: 'none' });
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
