<template>
  <view class="chat-container">
    <!-- Header -->
    <view class="header-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="left" @tap="goBack">
        <svg-icon name="arrow-back" :size="40" color="#2E7D32" />
        <text class="title">在线客服</text>
      </view>
    </view>

    <!-- Message List -->
    <scroll-view
      scroll-y
      class="message-list"
      :scroll-top="scrollTop"
      :scroll-into-view="scrollIntoView"
      scroll-with-animation
    >
      <!-- Welcome -->
      <view class="welcome-tip" v-if="messages.length === 0 && !loading">
        <image class="welcome-avatar" src="/static/images/logo.png" mode="aspectFill" />
        <view class="welcome-info">
          <text class="welcome-name">鲜果记客服</text>
          <text class="welcome-desc">您好！欢迎来到鲜果记，有什么可以帮您？</text>
        </view>
      </view>

      <view
        v-for="msg in messages"
        :key="msg.id"
        class="msg-row"
        :class="{ 'is-merchant': msg.senderType === 1 }"
        :id="`msg-${msg.id}`"
      >
        <!-- Avatar -->
        <image
          class="avatar"
          :src="resolveImageUrl(msg.senderAvatar) || (msg.senderType === 1 ? '/static/images/logo.png' : userAvatar)"
          mode="aspectFill"
        />

        <!-- Bubble -->
        <view class="bubble-wrapper">
          <!-- Text -->
          <view v-if="msg.msgType === 'text'" class="bubble text-bubble">
            <text class="bubble-text">{{ msg.content }}</text>
          </view>

          <!-- Product Card -->
          <view
            v-else-if="msg.msgType === 'product' && msg.productCard"
            class="bubble product-bubble"
            @tap="goToProduct(msg.productCard.productId)"
          >
            <image class="product-img" :src="resolveImageUrl(msg.productCard.mainImage)" mode="aspectFill" />
            <view class="product-info">
              <text class="product-name">{{ msg.productCard.name }}</text>
              <view class="product-bottom">
                <text class="product-price">¥{{ msg.productCard.price }}</text>
                <text class="product-spec" v-if="msg.productCard.specName">{{ msg.productCard.specName }}</text>
              </view>
            </view>
          </view>

          <!-- Image -->
          <view v-else-if="msg.msgType === 'image'" class="bubble image-bubble">
            <image
              v-for="(img, idx) in msg.images"
              :key="idx"
              class="chat-image"
              :src="resolveImageUrl(img)"
              mode="widthFix"
              @tap="previewImage(img, msg.images || [])"
            />
          </view>

          <!-- Time -->
          <view class="meta-row">
            <text class="msg-time">{{ formatTime(msg.createdAt) }}</text>
            <text v-if="msg.senderType === 0" class="read-status">{{ msg.isRead === 1 ? '已读' : '未读' }}</text>
          </view>
        </view>
      </view>

      <!-- Loading -->
      <view v-if="loading" class="loading-tip">
        <text>加载中...</text>
      </view>
    </scroll-view>

    <!-- Product Card Preview (when sharing a product) -->
    <view v-if="shareProduct" class="share-preview">
      <image class="share-img" :src="resolveImageUrl(shareProduct.mainImage)" mode="aspectFill" />
      <view class="share-info">
        <text class="share-name">{{ shareProduct.name }}</text>
        <text class="share-price">¥{{ shareProduct.price }}</text>
      </view>
      <view class="share-cancel" @tap="shareProduct = null">
        <svg-icon name="close" :size="32" color="#757575" />
      </view>
    </view>

    <!-- Input Bar -->
    <view class="input-bar">
      <view class="input-wrapper">
        <input
          class="msg-input"
          v-model="inputText"
          placeholder="输入消息..."
          confirm-type="send"
          @confirm="handleSend"
          :adjust-position="true"
        />
        <view class="tool-btns">
          <view class="tool-btn" @tap="handleChooseImage">
            <svg-icon name="photo" :size="44" color="#757575" />
          </view>
        </view>
      </view>
      <button class="send-btn" :class="{ active: canSend }" @tap="handleSend">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue';
import { chatApi } from '@/api/modules/chat';
import type { ChatMessageVO, ProductCardVO, ChatSendDto } from '@/api/types/chat';
import { resolveImageUrl } from '@/utils/image';
import { useUserStore } from '@/stores/user';
import SvgIcon from '@/components/svg-icon.vue';

const userStore = useUserStore();
const userAvatar = computed(() =>
  userStore.userInfo?.avatar || '/static/images/default-avatar.png'
);

const messages = ref<ChatMessageVO[]>([]);
const inputText = ref('');
const loading = ref(false);
const scrollTop = ref(0);
const scrollIntoView = ref('');
const shareProduct = ref<ProductCardVO | null>(null);
const statusBarHeight = ref(0);

const canSend = computed(() => inputText.value.trim().length > 0 || shareProduct.value !== null);

let pollTimer: ReturnType<typeof setInterval> | null = null;

onMounted(() => {
  // 获取状态栏高度
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 0;
  
  loadMessages();
  // 轮询新消息
  pollTimer = setInterval(() => {
    pollNewMessages();
  }, 5000);
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

function goBack() {
  if (pollTimer) clearInterval(pollTimer);
  uni.navigateBack();
}

async function loadMessages() {
  loading.value = true;
  try {
    const data = await chatApi.messages({ page: 1, size: 100 });
    messages.value = data.list;
    await nextTick();
    scrollToBottom();
  } catch (e) {
    console.warn('加载聊天记录失败', e);
  } finally {
    loading.value = false;
  }
}

async function pollNewMessages() {
  try {
    const data = await chatApi.messages({ page: 1, size: 100 });
    const lastId = messages.value[messages.value.length - 1]?.id || 0;
    const newMsgs = data.list.filter(m => m.id > lastId);
    messages.value = data.list;
    if (newMsgs.length > 0 || messages.value.length === 0) {
      await nextTick();
      scrollToBottom();
    }
  } catch (e) {
    // silent
  }
}

function scrollToBottom() {
  if (messages.value.length > 0) {
    const lastMsg = messages.value[messages.value.length - 1];
    scrollIntoView.value = `msg-${lastMsg.id}`;
  }
}

async function handleSend() {
  const text = inputText.value.trim();

  // 发送商品卡片 + 文字（如果有）
  if (shareProduct.value) {
    try {
      const msg = await chatApi.send({
        msgType: 'product',
        productId: shareProduct.value.productId,
      });
      messages.value.push(msg);
      shareProduct.value = null;
      await nextTick();
      scrollToBottom();
    } catch (e) {
      console.warn('发送失败', e);
    }
  }

  // 发送文字（如果有）
  if (text) {
    try {
      const msg = await chatApi.send({
        msgType: 'text',
        content: text,
      });
      messages.value.push(msg);
      inputText.value = '';
      await nextTick();
      scrollToBottom();
    } catch (e) {
      console.warn('发送失败', e);
    }
  }
}

function handleChooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const path = res.tempFilePaths[0];
      if (!path) return;
      // 上传图片
      uni.showLoading({ title: '发送中...' });
      try {
        const url = await uploadImage(path);
        const msg = await chatApi.send({
          msgType: 'image',
          images: [url],
        });
        messages.value.push(msg);
        await nextTick();
        scrollToBottom();
      } catch (e) {
        uni.showToast({ title: '图片发送失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
  });
}

function uploadImage(filePath: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const BASE_URL = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080';
    uni.uploadFile({
      url: BASE_URL + '/api/u/file/upload',
      filePath,
      name: 'file',
      header: {
        Authorization: `Bearer ${userStore.token}`,
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
      fail: reject,
    });
  });
}

function goToProduct(productId: number) {
  uni.navigateTo({ url: `/pagesA/goods-detail/index?id=${productId}` });
}

function previewImage(current: string, urls: string[]) {
  uni.previewImage({
    current: resolveImageUrl(current),
    urls: urls.map(resolveImageUrl),
  });
}

function formatTime(value?: string) {
  if (!value) return '';
  const d = new Date(value.replace(' ', 'T'));
  if (isNaN(d.getTime())) return '';
  const now = new Date();
  const isToday = d.toDateString() === now.toDateString();
  const h = String(d.getHours()).padStart(2, '0');
  const m = String(d.getMinutes()).padStart(2, '0');
  if (isToday) return `${h}:${m}`;
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${month}-${day} ${h}:${m}`;
}

/** 接收商品分享参数（从商品详情页跳转时传入） */
onMounted(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;
  const opts = page?.options || {};
  if (opts.productId && opts.productName) {
    shareProduct.value = {
      productId: Number(opts.productId),
      name: decodeURIComponent(String(opts.productName)),
      mainImage: opts.productImage ? decodeURIComponent(String(opts.productImage)) : '',
      price: opts.productPrice ? decodeURIComponent(String(opts.productPrice)) : '',
      specName: opts.productSpec ? decodeURIComponent(String(opts.productSpec)) : '',
    };
  }
});
</script>

<style lang="scss" scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav {
  background-color: #ffffff;
  padding: $space-3 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);

  .left {
    display: flex;
    align-items: center;
    gap: $space-2;
  }

  .title {
    font-size: $font-lg;
    font-weight: $weight-semibold;
    color: $color-primary;
  }
}

.message-list {
  flex: 1;
  overflow: hidden;
  padding: $space-3 $space-4;
}

.welcome-tip {
  display: flex;
  gap: $space-3;
  padding: $space-4;
  background-color: #ffffff;
  border-radius: $radius-md;
  margin-bottom: $space-4;

  .welcome-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .welcome-info {
    display: flex;
    flex-direction: column;
    gap: $space-1;

    .welcome-name {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }

    .welcome-desc {
      font-size: $font-sm;
      color: $color-text-secondary;
      line-height: 1.5;
    }
  }
}

.msg-row {
  display: flex;
  gap: $space-2;
  margin-bottom: $space-4;

  &.is-merchant {
    flex-direction: row;

    .bubble {
      background-color: #ffffff;
      border-radius: 0 $radius-md $radius-md $radius-md;
    }

    .msg-time {
      text-align: left;
    }
  }

  &:not(.is-merchant) {
    flex-direction: row-reverse;

    .bubble {
      background-color: $color-primary-bg;
      border-radius: $radius-md 0 $radius-md $radius-md;
    }

    .bubble-wrapper {
      align-items: flex-end;
    }

    .msg-time {
      text-align: right;
    }
  }

  .avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .bubble-wrapper {
    max-width: 70%;
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }
}

.bubble {
  padding: $space-3 $space-4;
  box-shadow: $shadow-card;

  .bubble-text {
    font-size: $font-base;
    color: $color-text-primary;
    line-height: 1.6;
    word-break: break-all;
  }
}

.product-bubble {
  width: 480rpx;
  padding: 0;
  overflow: hidden;

  .product-img {
    width: 100%;
    height: 320rpx;
  }

  .product-info {
    padding: $space-3;

    .product-name {
      font-size: $font-sm;
      color: $color-text-primary;
      @include text-ellipsis-2;
      display: block;
      margin-bottom: $space-1;
    }

    .product-bottom {
      display: flex;
      align-items: center;
      gap: $space-2;

      .product-price {
        font-size: $font-md;
        color: $color-price;
        font-weight: $weight-semibold;
      }

      .product-spec {
        font-size: $font-xs;
        color: $color-text-secondary;
      }
    }
  }
}

.image-bubble {
  padding: $space-2;
  background-color: transparent !important;
  box-shadow: none;

  .chat-image {
    width: 360rpx;
    border-radius: $radius-md;
  }
}

.msg-time {
  font-size: $font-xs;
  color: $color-text-placeholder;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: 0 $space-2;
}

.read-status {
  font-size: $font-xs;
  color: $color-text-placeholder;
}

.loading-tip {
  text-align: center;
  padding: $space-3;
  font-size: $font-sm;
  color: $color-text-placeholder;
}

.share-preview {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-2 $space-4;
  background-color: #ffffff;
  border-top: 2rpx solid $color-divider;

  .share-img {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-sm;
    flex-shrink: 0;
  }

  .share-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4rpx;

    .share-name {
      font-size: $font-sm;
      color: $color-text-primary;
      @include text-ellipsis;
    }

    .share-price {
      font-size: $font-xs;
      color: $color-price;
      font-weight: $weight-semibold;
    }
  }

  .share-cancel {
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    flex-shrink: 0;
  }
}

.input-bar {
  display: flex;
  align-items: center;
  gap: $space-2;
  padding: $space-2 $space-4;
  background-color: #ffffff;
  border-top: 2rpx solid $color-divider;
  @include safe-area-bottom;

  .input-wrapper {
    flex: 1;
    display: flex;
    align-items: center;
    background-color: $color-bg-page;
    border-radius: $radius-pill;
    padding: 0 $space-2 0 $space-4;
    height: 64rpx;

    .msg-input {
      flex: 1;
      font-size: $font-base;
      height: 64rpx;
      line-height: 64rpx;
    }

    .tool-btns {
      display: flex;
      gap: $space-2;

      .tool-btn {
        width: 56rpx;
        height: 56rpx;
        @include flex-center;
      }
    }
  }

  .send-btn {
    width: 120rpx;
    height: 64rpx;
    line-height: 64rpx;
    border-radius: $radius-pill;
    background-color: $color-divider;
    color: $color-text-placeholder;
    font-size: $font-base;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    &::after { border: none; }

    &.active {
      background-color: $color-primary;
      color: #ffffff;
    }
  }
}
</style>
