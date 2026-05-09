<template>
  <view class="countdown-flip" :class="{ compact }">
    <view v-if="days > 0" class="flip-group">
      <view class="flip-card">
        <view class="flip-num" :class="{ rolling: rollingKey !== 0 }">{{ pad(days) }}</view>
      </view>
      <text class="flip-label">天</text>
    </view>
    <view class="flip-group">
      <view class="flip-card">
        <view class="flip-num" :class="{ rolling: rollingKey !== 0 }">{{ pad(hours) }}</view>
      </view>
      <text class="flip-label">时</text>
    </view>
    <view class="flip-colon">:</view>
    <view class="flip-group">
      <view class="flip-card">
        <view class="flip-num" :class="{ rolling: rollingKey !== 0 }">{{ pad(minutes) }}</view>
      </view>
      <text class="flip-label">分</text>
    </view>
    <view class="flip-colon">:</view>
    <view class="flip-group">
      <view class="flip-card">
        <view class="flip-num" :class="{ rolling: rollingKey !== 0 }">{{ pad(seconds) }}</view>
      </view>
      <text class="flip-label">秒</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';

const props = withDefaults(defineProps<{
  endTime: string;
  compact?: boolean;
}>(), {
  compact: false,
});

const now = ref(Date.now());
const rollingKey = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

const diff = computed(() => {
  if (!props.endTime) return 0;
  const end = new Date(props.endTime.replace(' ', 'T')).getTime();
  if (Number.isNaN(end)) return 0;
  return Math.max(0, end - now.value);
});

const days = computed(() => Math.floor(diff.value / 86400000));
const hours = computed(() => Math.floor((diff.value % 86400000) / 3600000));
const minutes = computed(() => Math.floor((diff.value % 3600000) / 60000));
const seconds = computed(() => Math.floor((diff.value % 60000) / 1000));

function pad(n: number) {
  return n.toString().padStart(2, '0');
}

watch(seconds, () => {
  rollingKey.value = Date.now();
});

onMounted(() => {
  timer = setInterval(() => { now.value = Date.now(); }, 1000);
});

onUnmounted(() => {
  if (timer) { clearInterval(timer); timer = null; }
});
</script>

<style lang="scss" scoped>
.countdown-flip {
  display: inline-flex;
  align-items: flex-end;
  gap: 4rpx;
}

.flip-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rpx;
}

.flip-card {
  background: $color-price;
  border-radius: 6rpx;
  padding: 2rpx 8rpx;
  min-width: 44rpx;
  text-align: center;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    top: 50%;
    height: 2rpx;
    background: rgba(0, 0, 0, 0.1);
  }
}

.flip-num {
  font-size: $font-sm;
  font-weight: bold;
  color: #fff;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
  line-height: 1.4;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.rolling {
    animation: flipRoll 0.4s ease-out;
  }
}

@keyframes flipRoll {
  0% {
    transform: translateY(-40%);
    opacity: 0.3;
  }
  50% {
    transform: translateY(10%);
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
}

.flip-label {
  font-size: 16rpx;
  color: $color-text-placeholder;
  line-height: 1;
}

.flip-colon {
  font-size: $font-sm;
  font-weight: bold;
  color: $color-price;
  line-height: 1.4;
  align-self: flex-start;
  margin-bottom: 20rpx;
}

.compact {
  .flip-card {
    padding: 0 4rpx;
    min-width: 32rpx;
  }

  .flip-num {
    font-size: 20rpx;
  }

  .flip-label {
    font-size: 14rpx;
  }

  .flip-colon {
    font-size: 20rpx;
    margin-bottom: 14rpx;
  }
}
</style>
