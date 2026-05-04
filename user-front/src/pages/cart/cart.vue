<template>
  <view class="cart-container">
    <!-- Top Navigation -->
    <view class="header-nav-sticky">
      <view class="left" @tap="goToCategory">
        <svg-icon name="category" :size="40" color="#757575" />
        <text class="brand-name">鲜果记</text>
      </view>
      <svg-icon name="chat" :size="40" color="#757575" @click="goToMessage" />
    </view>

    <scroll-view scroll-y class="main-scroll">
      <view class="cart-header">
        <text class="title">购物车</text>
        <text class="manage-btn" @tap="toggleManage">{{ isManaging ? '完成' : '管理' }}</text>
      </view>

      <!-- Discount Progress Bar -->
      <view v-if="cartStore.promotionTip" class="discount-bar card">
        <view class="info-row">
          <view class="tag">满减</view>
          <text class="desc">{{ cartStore.promotionTip }}</text>
          <view class="more-btn" @tap="goToHome">
            去凑单 <svg-icon name="chevron-right" :size="24" color="#2E7D32" />
          </view>
        </view>
      </view>

      <!-- Cart Items -->
      <view v-if="cartStore.items.length > 0" class="cart-list">
        <view v-for="item in cartStore.items" :key="item.id" class="cart-item card">
          <view class="check-box" @tap="toggleSelect(item)">
            <view class="circle" :class="{ checked: item.selected === 1 }">
              <svg-icon v-if="item.selected === 1" name="check" :size="24" color="#FFFFFF" />
            </view>
          </view>
          <image :src="item.mainImage" mode="aspectFill" class="item-img" />
          <view class="item-info">
            <view class="top">
              <text class="name">{{ item.productName }}</text>
              <text class="specs">{{ item.specName }}</text>
            </view>
            <view class="bottom">
              <text class="price">¥{{ item.price }}</text>
              <view class="counter">
                <view class="btn" @tap="handleDecrease(item)">
                  <svg-icon name="remove" :size="32" color="#757575" />
                </view>
                <text class="num">{{ item.quantity }}</text>
                <view class="btn primary" @tap="handleIncrease(item)">
                  <svg-icon name="add" :size="32" color="#2E7D32" />
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty State -->
      <view v-else class="empty-state">
        <image src="/static/images/empty-cart.png" mode="aspectFit" class="empty-img" />
        <text class="empty-title">购物车空空如也</text>
        <text class="empty-desc">去挑选一些从果园直采的新鲜美味吧</text>
        <button class="go-btn" @tap="goToHome">去逛逛</button>
      </view>
    </scroll-view>

    <!-- Bottom Bar -->
    <view class="bottom-bar">
      <view class="left" @tap="toggleSelectAll">
        <view class="circle" :class="{ checked: isAllSelected }">
          <svg-icon v-if="isAllSelected" name="check" :size="24" color="#FFFFFF" />
        </view>
        <text class="text">全选</text>
      </view>
      <view class="right">
        <view v-if="!isManaging" class="total-info">
          <text class="label">合计：</text>
          <text class="price">¥{{ cartStore.totalPrice }}</text>
        </view>
        <button 
          class="submit-btn" 
          :class="{ delete: isManaging }"
          @tap="handleSubmit"
        >
          {{ isManaging ? '删除' : '去结算' }}
        </button>
      </view>
    </view>

    <!-- Custom Tab Bar -->
    <custom-tab-bar active-path="pages/cart/cart" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useCartStore } from '@/stores/cart';
import { useUserStore } from '@/stores/user';
import SvgIcon from '@/components/svg-icon.vue';
import CustomTabBar from '@/components/custom-tab-bar.vue';

const cartStore = useCartStore();
const userStore = useUserStore();
const isManaging = ref(false);

onShow(async () => {
  uni.hideTabBar();
  if (userStore.isLogin) {
    await cartStore.refreshList();
  }
});

const isAllSelected = computed(() => {
  return cartStore.items.length > 0 && cartStore.items.every(item => item.selected === 1);
});

function toggleManage() {
  isManaging.value = !isManaging.value;
}

async function toggleSelect(item: any) {
  const newSelected: 0 | 1 = item.selected === 1 ? 0 : 1;
  await cartStore.setSelected([item.id], newSelected);
}

async function toggleSelectAll() {
  const target: 0 | 1 = isAllSelected.value ? 0 : 1;
  const ids = cartStore.items.map(i => i.id);
  await cartStore.setSelected(ids, target);
}

function goToHome() {
  uni.switchTab({ url: '/pages/index/index' });
}

function goToCategory() {
  uni.switchTab({ url: '/pages/category/category' });
}

function goToMessage() {
  uni.navigateTo({ url: '/pagesC/message/index' });
}

async function handleDecrease(item: any) {
  if (item.quantity <= 1) return;
  await cartStore.updateQty(item.id, item.quantity - 1);
}

async function handleIncrease(item: any) {
  await cartStore.updateQty(item.id, item.quantity + 1);
}

async function handleSubmit() {
  if (isManaging.value) {
    const selected = cartStore.selectedItems;
    if (selected.length === 0) {
      uni.showToast({ title: '请选择要删除的商品', icon: 'none' });
      return;
    }
    uni.showModal({
      title: '提示',
      content: `确定要删除选中的 ${selected.length} 件商品？`,
      success: async (res) => {
        if (res.confirm) {
          for (const item of selected) {
            await cartStore.remove(item.id);
          }
          uni.showToast({ title: '已删除', icon: 'success' });
        }
      }
    });
  } else {
    const selected = cartStore.selectedItems;
    if (selected.length === 0) {
      uni.showToast({ title: '请选择要结算的商品', icon: 'none' });
      return;
    }
    const ids = selected.map(i => i.id).join(',');
    uni.navigateTo({ url: `/pagesB/checkout/index?cartItemIds=${ids}` });
  }
}
</script>

<style lang="scss" scoped>
.cart-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav-sticky {
  background-color: #ffffff;
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);
  z-index: 100;

  .left {
    display: flex;
    align-items: center;
    gap: $space-3;
    
    .brand-name {
      font-size: $font-lg;
      font-weight: bold;
      color: $color-primary;
    }
  }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: 0 $space-4;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: $space-5 0 $space-3;

  .title {
    font-size: 48rpx;
    font-weight: $weight-semibold;
    color: $color-text-primary;
  }

  .manage-btn {
    font-size: $font-base;
    color: $color-primary;
    font-weight: $weight-medium;
  }
}

.discount-bar {
  .info-row {
    display: flex;
    align-items: center;
    gap: $space-2;
    margin-bottom: $space-2;

    .tag {
      font-size: 20rpx;
      background-color: $color-primary-bg;
      color: $color-primary;
      padding: 2rpx $space-1;
      border-radius: $radius-sm;
      font-weight: bold;
    }

    .desc {
      flex: 1;
      font-size: $font-sm;
      color: $color-text-primary;

      .highlight {
        color: $color-primary;
        font-weight: bold;
      }
    }

    .more-btn {
      font-size: $font-sm;
      color: $color-primary;
      font-weight: $weight-medium;
      display: flex;
      align-items: center;
    }
  }

  .progress-bg {
    height: 6rpx;
    background-color: $color-divider;
    border-radius: 3rpx;
    overflow: hidden;

    .progress-inner {
      height: 100%;
      background-color: $color-primary;
      border-radius: 3rpx;
    }
  }
}

.cart-list {
  padding-bottom: 120rpx;
}

.cart-item {
  display: flex;
  gap: $space-3;
  align-items: center;

  .check-box {
    .circle {
      width: 40rpx;
      height: 40rpx;
      border: 2rpx solid $color-divider;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.checked {
        background-color: $color-primary;
        border-color: $color-primary;
      }

      .icon-check {
        color: #ffffff;
        font-size: 24rpx;
      }
    }
  }

  .item-img {
    width: 160rpx;
    height: 160rpx;
    border-radius: $radius-sm;
    background-color: $color-bg-page;
  }

  .item-info {
    flex: 1;
    height: 160rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .name {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
      @include text-ellipsis;
    }

    .specs {
      font-size: $font-xs;
      color: $color-text-secondary;
      margin-top: 4rpx;
    }

    .bottom {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;

      .price {
        font-size: $font-md;
        color: $color-primary;
        font-weight: bold;
      }

      .counter {
        display: flex;
        align-items: center;
        background-color: $color-bg-page;
        border-radius: $radius-pill;
        padding: 4rpx;
        gap: $space-2;

        .btn {
          width: 48rpx;
          height: 48rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          color: $color-text-secondary;
          
          &.primary {
            color: $color-primary;
          }

          .iconfont {
            font-size: 32rpx;
          }
        }

        .num {
          font-size: $font-base;
          color: $color-text-primary;
          min-width: 40rpx;
          text-align: center;
        }
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 100rpx;

  .empty-img {
    width: 384rpx;
    height: 384rpx;
    margin-bottom: $space-4;
  }

  .empty-title {
    font-size: $font-lg;
    font-weight: $weight-semibold;
    color: $color-text-primary;
    margin-bottom: $space-1;
  }

  .empty-desc {
    font-size: $font-base;
    color: $color-text-secondary;
    margin-bottom: $space-5;
    max-width: 480rpx;
    text-align: center;
  }

  .go-btn {
    width: 100%;
    max-width: 400rpx;
    height: 96rpx;
    background-color: $color-primary;
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-md;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-card;
  }
}

.bottom-bar {
  position: fixed;
  bottom: calc(128rpx + constant(safe-area-inset-bottom));
  bottom: calc(128rpx + env(safe-area-inset-bottom));
  left: 0;
  right: 0;
  height: 112rpx;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $space-4;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  z-index: 100;
  @include safe-area-bottom;

  .left {
    display: flex;
    align-items: center;
    gap: $space-2;

    .circle {
      width: 40rpx;
      height: 40rpx;
      border: 2rpx solid $color-divider;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.checked {
        background-color: $color-primary;
        border-color: $color-primary;
      }

      .icon-check {
        color: #ffffff;
        font-size: 24rpx;
      }
    }

    .text {
      font-size: $font-sm;
      color: $color-text-secondary;
    }
  }

  .right {
    display: flex;
    align-items: center;
    gap: $space-4;

    .total-info {
      display: flex;
      align-items: baseline;

      .label {
        font-size: $font-sm;
        color: $color-text-primary;
      }

      .price {
        font-size: $font-lg;
        color: $color-primary;
        font-weight: bold;
      }
    }

    .submit-btn {
      width: 240rpx;
      height: 80rpx;
      background-color: $color-primary;
      color: #ffffff;
      border-radius: $radius-pill;
      font-size: $font-base;
      font-weight: $weight-semibold;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.delete {
        background-color: $color-price;
      }

      &::after { border: none; }
    }
  }
}
</style>
