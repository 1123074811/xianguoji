<template>
  <view class="checkout-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <svg-icon name="arrow-back" :size="40" color="#757575" @click="goBack" />
      <text class="title">确认订单</text>
      <view style="width: 40rpx;"></view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Delivery Method -->
      <view class="delivery-method">
        <view 
          class="method-card" 
          :class="{ active: deliveryType === 1 }"
          @tap="deliveryType = 1"
        >
          <svg-icon name="shipping" :size="48" :color="deliveryType === 1 ? '#2E7D32' : '#BDBDBD'" />
          <text class="label">快递配送</text>
          <text class="desc">最快次日送达</text>
          <view v-if="deliveryType === 1" class="active-tag">已选</view>
        </view>
        <view 
          class="method-card" 
          :class="{ active: deliveryType === 2 }"
          @tap="deliveryType = 2"
        >
          <svg-icon name="home" :size="48" :color="deliveryType === 2 ? '#2E7D32' : '#BDBDBD'" />
          <text class="label">到店自提</text>
          <text class="desc">到店自取</text>
          <view v-if="deliveryType === 2" class="active-tag">已选</view>
        </view>
      </view>

      <!-- Address / Pickup Info -->
      <view v-if="deliveryType === 1" class="address-card card">
        <view class="address-info" @tap="goToAddress">
          <view class="left">
            <view v-if="preview?.address" class="top">
              <view class="tag">家</view>
              <text class="addr-text">{{ preview.address.fullAddress }}</text>
            </view>
            <text v-if="preview?.address" class="user-text">{{ preview.address.consignee }} {{ preview.address.phone }}</text>
            <text v-else class="user-text" style="color: #E53935;">请选择收货地址</text>
          </view>
          <svg-icon name="chevron-right" :size="32" color="#BDBDBD" />
        </view>
        <view class="divider"></view>
        <view class="time-info">
          <view class="left">
            <svg-icon name="schedule" :size="32" color="#2E7D32" />
            <text class="label">立即送达</text>
          </view>
          <text class="time">预计 {{ estimatedTime }} 送达</text>
        </view>
      </view>
      <view v-else class="address-card card">
        <view class="address-info" @tap="goToPickupPoint">
          <view class="left">
            <view v-if="preview?.pickupPoint" class="top">
              <view class="tag">自提</view>
              <text class="addr-text">{{ preview.pickupPoint.name }}</text>
            </view>
            <text v-if="preview?.pickupPoint" class="user-text">{{ preview.pickupPoint.address }}</text>
            <text v-else class="user-text" style="color: #E53935;">请选择自提点</text>
          </view>
          <svg-icon name="chevron-right" :size="32" color="#BDBDBD" />
        </view>
      </view>

      <!-- Goods List -->
      <view class="goods-section card">
        <view class="shop-header">
          <svg-icon name="eco" :size="32" color="#2E7D32" />
          <text class="shop-name">鲜果记 (精品果园店)</text>
        </view>
        <view class="goods-list">
          <view v-for="item in preview?.items || []" :key="item.skuId" class="goods-item">
            <image :src="resolveImageUrl(item.image)" mode="aspectFill" class="goods-img" />
            <view class="info">
              <view class="top">
                <text class="name">{{ item.productName }}</text>
                <text class="specs">{{ item.specName }}</text>
              </view>
              <view class="bottom">
                <text class="price">¥{{ item.price }}</text>
                <text class="count">x {{ item.quantity }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Coupon & Remarks -->
      <view class="coupon-remark-card card">
        <view class="coupon-row" @tap="showCouponPicker">
          <view class="left">
            <svg-icon name="local_offer" :size="36" color="#E53935" />
            <text class="label">优惠券</text>
          </view>
          <view class="right">
            <text class="coupon-avail">{{ usableCoupons.length }} 张可用</text>
            <text v-if="preview?.couponAmount && Number(preview.couponAmount) > 0" class="coupon-discount">-¥{{ preview.couponAmount }}</text>
            <svg-icon name="chevron-right" :size="28" color="#BDBDBD" />
          </view>
        </view>
        <view class="divider"></view>
        <view class="remark-row">
          <svg-icon name="edit" :size="36" color="#BDBDBD" />
          <view class="remark-content">
            <text class="label">备注说明</text>
            <input class="remark-input" placeholder="Anything for the shopkeeper?" v-model="remark" />
          </view>
        </view>
      </view>

      <!-- Price Details -->
      <view class="price-detail-card card">
        <view class="row">
          <text class="label">商品小计</text>
          <text class="value">¥{{ preview?.goodsAmount || '0.00' }}</text>
        </view>
        <view class="row">
          <text class="label">优惠金额</text>
          <text class="value discount">-¥{{ preview?.discountAmount || '0.00' }}</text>
        </view>
        <view class="row">
          <text class="label">优惠券抵扣</text>
          <text class="value discount">-¥{{ preview?.couponAmount || '0.00' }}</text>
        </view>
        <view class="row">
          <view class="label-with-tag">
            <text class="label">配送费</text>
            <text v-if="deliveryType === 1" class="free-tag">满额免运费</text>
          </view>
          <view class="value-group">
            <text class="value">¥{{ preview?.deliveryFee || '0.00' }}</text>
          </view>
        </view>
        <view v-if="preview?.promotionTip" class="row">
          <text class="label" style="color: #2E7D32;">{{ preview.promotionTip }}</text>
          <text></text>
        </view>
        <view class="total-divider"></view>
        <view class="total-row">
          <text class="total-label">共 {{ totalQuantity }} 件, 实付合计</text>
          <text class="total-price">¥{{ finalPrice }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Coupon Picker Popup -->
    <view v-if="couponPickerVisible" class="coupon-mask" @tap="closeCouponPicker">
      <view class="coupon-popup" @tap.stop>
        <view class="popup-header">
          <text class="popup-title">选择优惠券</text>
          <view class="popup-close" @tap="closeCouponPicker">
            <svg-icon name="close" :size="36" color="#757575" />
          </view>
        </view>
        <scroll-view scroll-y class="popup-list">
          <!-- No coupon option -->
          <view class="coupon-item" :class="{ selected: !selectedCouponId }" @tap="selectCoupon(0)">
            <view class="coupon-left no-discount">
              <text class="no-amount">不使用</text>
            </view>
            <view class="coupon-right">
              <text class="coupon-name">不使用优惠券</text>
              <view v-if="!selectedCouponId" class="check-mark">
                <svg-icon name="check" :size="28" color="#2E7D32" />
              </view>
            </view>
          </view>
          <view
            v-for="c in usableCoupons"
            :key="c.id"
            class="coupon-item"
            :class="{ selected: selectedCouponId === c.id, disabled: !!c.unavailableReason }"
            @tap="!c.unavailableReason && selectCoupon(c.id)"
          >
            <view class="coupon-left">
              <text class="coupon-symbol">¥</text>
              <text class="coupon-amount">{{ c.coupon?.amount || '0' }}</text>
            </view>
            <view class="coupon-right">
              <text class="coupon-name">{{ c.coupon?.name || '优惠券' }}</text>
              <text class="coupon-condition">{{ Number(c.coupon?.threshold || 0) > 0 ? `满${c.coupon.threshold}元可用` : '无门槛' }}</text>
              <text v-if="c.unavailableReason" class="coupon-reason">{{ c.unavailableReason }}</text>
              <view v-if="selectedCouponId === c.id" class="check-mark">
                <svg-icon name="check" :size="28" color="#2E7D32" />
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- Bottom Bar -->
    <view class="bottom-bar">
      <view v-if="!shopOpen" class="closed-bar-tip">
        <svg-icon name="store" :size="28" color="#C62828" />
        <text class="closed-bar-text">店铺休息中，暂无法下单</text>
      </view>
      <view v-else class="price-info">
        <view class="price-row">
          <text class="label">合计:</text>
          <text class="price">¥{{ finalPrice }}</text>
        </view>
        <text v-if="totalDiscount > 0" class="discount-text">已优惠 ¥{{ totalDiscount }}</text>
      </view>
      <button class="submit-btn" :class="{ disabled: !shopOpen }" :disabled="!shopOpen" @tap="submitOrder">{{ shopOpen ? '提交订单' : '店铺休息中' }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { useAppStore } from '@/stores/app';
import { orderApi } from '@/api/modules/order';
import { promoApi } from '@/api/modules/promo';
import { resolveImageUrl } from '@/utils/image';
import SvgIcon from '@/components/svg-icon.vue';
import type { OrderPreviewVO } from '@/api/types/order';
import type { UserCouponVO } from '@/api/types/promo';

const appStore = useAppStore();
const shopOpen = computed(() => appStore.shopInfo?.isOpen === 1);
const deliveryType = ref<1 | 2>(1);
const remark = ref('');
const preview = ref<OrderPreviewVO | null>(null);
const selectedCouponId = ref<number | undefined>();
const usableCoupons = ref<UserCouponVO[]>([]);
const couponPickerVisible = ref(false);
const submitLoading = ref(false);
const routeOptions = ref<Record<string, string>>({});

const finalPrice = computed(() => {
  if (preview.value) return preview.value.payAmount;
  return '0.00';
});

const totalQuantity = computed(() => {
  if (!preview.value?.items) return 0;
  return preview.value.items.reduce((sum, item) => sum + item.quantity, 0);
});

const totalDiscount = computed(() => {
  if (!preview.value) return 0;
  const d = Number(preview.value.discountAmount || 0);
  const c = Number(preview.value.couponAmount || 0);
  return (d + c).toFixed(2);
});

const estimatedTime = computed(() => {
  const now = new Date(Date.now() + 30 * 60 * 1000);
  return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
});

async function loadPreview() {
  try {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1] as any;
    routeOptions.value = page?.options || {};
    const cartItemIds = routeOptions.value.cartItemIds?.split(',').map(Number).filter(Boolean) || [];
    const data = await orderApi.preview({
      deliveryType: deliveryType.value,
      addressId: preview.value?.address?.id || (routeOptions.value.addressId ? Number(routeOptions.value.addressId) : undefined),
      pickupPointId: deliveryType.value === 2 ? (preview.value?.pickupPoint?.id || (routeOptions.value.pickupPointId ? Number(routeOptions.value.pickupPointId) : undefined)) : undefined,
      cartItemIds: cartItemIds.length ? cartItemIds : undefined,
      userCouponId: selectedCouponId.value,
      userRemark: remark.value || undefined,
    });
    preview.value = data;
    // Load usable coupons based on preview items
    if (data.items?.length) {
      try {
        usableCoupons.value = await promoApi.usableCoupons({
          totalAmount: data.goodsAmount,
          productIds: data.items.map(i => i.skuId),
        });
      } catch {
        usableCoupons.value = [];
      }
    }
  } catch (e) {
    console.warn('加载订单预览失败', e);
  }
}

onMounted(() => {
  loadPreview();
});

// When returning from address/pickup page, reload preview with selected address or pickup point
onShow(() => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1] as any;

  const selectedAddress = page?.$vm?.selectedAddress;
  if (selectedAddress) {
    const addr = {
      id: selectedAddress.id,
      consignee: selectedAddress.consignee,
      phone: selectedAddress.phone,
      fullAddress: `${selectedAddress.province}${selectedAddress.city}${selectedAddress.district}${selectedAddress.detail}`,
    };
    if (!preview.value) preview.value = {} as OrderPreviewVO;
    preview.value.address = addr;
    page.$vm.selectedAddress = null;
    loadPreview();
  }

  const selectedPickupPoint = page?.$vm?.selectedPickupPoint;
  if (selectedPickupPoint) {
    if (!preview.value) preview.value = {} as OrderPreviewVO;
    preview.value.pickupPoint = selectedPickupPoint;
    page.$vm.selectedPickupPoint = null;
    loadPreview();
  }
});

watch(deliveryType, () => {
  loadPreview();
});

async function showCouponPicker() {
  if (!usableCoupons.value.length) {
    return uni.showToast({ title: '暂无可用优惠券', icon: 'none' });
  }
  couponPickerVisible.value = true;
}

function closeCouponPicker() {
  couponPickerVisible.value = false;
}

async function selectCoupon(couponId: number) {
  if (couponId === 0) {
    selectedCouponId.value = undefined;
  } else {
    selectedCouponId.value = couponId;
  }
  closeCouponPicker();
  await loadPreview();
}

function goBack() {
  uni.navigateBack();
}

function goToAddress() {
  uni.navigateTo({ url: '/pagesC/address/index?select=1' });
}

function goToPickupPoint() {
  uni.navigateTo({ url: '/pagesC/pickup-point/index?select=1' });
}

async function submitOrder() {
  if (submitLoading.value) return;
  // Validate address/pickup point before submit
  if (deliveryType.value === 1 && !preview.value?.address?.id) {
    return uni.showToast({ title: '请选择收货地址', icon: 'none' });
  }
  if (deliveryType.value === 2 && !preview.value?.pickupPoint?.id) {
    return uni.showToast({ title: '请选择自提点', icon: 'none' });
  }
  submitLoading.value = true;
  uni.showLoading({ title: '提交中' });
  try {
    const pages = getCurrentPages();
    const page = pages[pages.length - 1] as any;
    const cartItemIds = page?.options?.cartItemIds?.split(',').map(Number).filter(Boolean) || [];
    const result = await orderApi.submit({
      addressId: deliveryType.value === 1 ? preview.value?.address?.id : undefined,
      pickupPointId: deliveryType.value === 2 ? preview.value?.pickupPoint?.id : undefined,
      deliveryType: deliveryType.value,
      cartItemIds: cartItemIds.length ? cartItemIds : undefined,
      userCouponId: selectedCouponId.value,
      userRemark: remark.value || undefined,
      payMethod: 'wechat',
    });
    uni.hideLoading();
    uni.redirectTo({ url: `/pagesB/payment-result/index?orderNo=${result.orderNo}` });
  } catch (e) {
    uni.hideLoading();
    console.warn('提交订单失败', e);
  } finally {
    submitLoading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.checkout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav {
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
  border-bottom: 2rpx solid $color-divider;
  z-index: 100;

  .title {
    font-size: $font-lg;
    font-weight: $weight-semibold;
    color: $color-text-primary;
  }
}

.main-scroll {
  flex: 1;
  padding: $space-3;
  padding-bottom: 140rpx; /* Leave space for bottom bar */
}

.delivery-method {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-3;

  .method-card {
    flex: 1;
    background-color: #ffffff;
    border-radius: $radius-md;
    padding: $space-4;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $space-1;
    border: 2rpx solid transparent;
    position: relative;
    overflow: hidden;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);

    &.active {
      border-color: $color-primary;
      background-color: rgba($color-primary, 0.02);

      .label {
        color: $color-primary;
        font-weight: $weight-semibold;
      }
    }

    .label {
      font-size: $font-base;
      color: $color-text-primary;
      margin-top: $space-1;
    }

    .desc {
      font-size: $font-xs;
      color: $color-text-secondary;
    }

    .active-tag {
      position: absolute;
      top: 0;
      right: 0;
      background-color: $color-primary;
      color: #ffffff;
      font-size: 18rpx;
      padding: 2rpx 12rpx;
      border-bottom-left-radius: $radius-sm;
    }
  }
}

.card {
  background-color: #ffffff;
  border-radius: $radius-md;
  margin-bottom: $space-3;
  padding: $space-4;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

.address-card {
  display: flex;
  flex-direction: column;
  gap: $space-3;

  .address-info {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      flex-direction: column;
      gap: $space-1;

      .top {
        display: flex;
        align-items: center;
        gap: $space-2;

        .tag {
          background-color: $color-primary-bg;
          color: $color-primary;
          font-size: 20rpx;
          padding: 2rpx 8rpx;
          border-radius: 4rpx;
        }

        .addr-text {
          font-size: $font-md;
          font-weight: $weight-semibold;
          color: $color-text-primary;
        }
      }

      .user-text {
        font-size: $font-sm;
        color: $color-text-secondary;
      }
    }
  }

  .divider {
    height: 2rpx;
    background-color: $color-divider;
  }

  .time-info {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: $space-1;

      .label {
        font-size: $font-base;
        font-weight: $weight-medium;
        color: $color-text-primary;
      }
    }

    .time {
      font-size: $font-sm;
      color: $color-primary;
    }
  }
}

.goods-section {
  padding: 0;

  .shop-header {
    padding: $space-3 $space-4;
    display: flex;
    align-items: center;
    gap: $space-1;
    border-bottom: 2rpx solid $color-divider;

    .shop-name {
      font-size: $font-base;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }
  }

  .goods-list {
    padding: $space-3 $space-4;
    display: flex;
    flex-direction: column;
    gap: $space-3;

    .goods-item {
      display: flex;
      gap: $space-3;

      .goods-img {
        width: 120rpx;
        height: 120rpx;
        border-radius: $radius-sm;
        background-color: $color-bg-page;
      }

      .info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .top {
          display: flex;
          flex-direction: column;
          gap: 4rpx;

          .name {
            font-size: $font-base;
            color: $color-text-primary;
            @include text-ellipsis;
          }

          .specs {
            font-size: $font-xs;
            color: $color-text-secondary;
          }
        }

        .bottom {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .price {
            font-size: $font-base;
            font-weight: $weight-semibold;
            color: $color-text-primary;
          }

          .count {
            font-size: $font-sm;
            color: $color-text-secondary;
          }
        }
      }
    }
  }
}

.coupon-remark-card {
  display: flex;
  flex-direction: column;

  .coupon-row {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: $space-2;

      .label {
        font-size: $font-base;
        font-weight: $weight-medium;
        color: $color-text-primary;
      }
    }

    .right {
      display: flex;
      align-items: center;
      gap: $space-1;

      .coupon-avail {
        font-size: $font-xs;
        background-color: rgba($color-price, 0.1);
        color: $color-price;
        padding: 4rpx 12rpx;
        border-radius: $radius-pill;
      }

      .coupon-discount {
        font-size: $font-base;
        font-weight: $weight-semibold;
        color: $color-price;
      }
    }
  }

  .divider {
    height: 2rpx;
    background-color: $color-divider;
    margin: $space-3 0;
  }

  .remark-row {
    display: flex;
    align-items: flex-start;
    gap: $space-2;

    .remark-content {
      flex: 1;

      .label {
        font-size: $font-base;
        font-weight: $weight-medium;
        color: $color-text-primary;
        margin-bottom: $space-1;
        display: block;
      }

      .remark-input {
        width: 100%;
        font-size: $font-sm;
        color: $color-text-primary;
        background: transparent;
        border: none;
        padding: 0;
      }
    }
  }
}

.price-detail-card {
  display: flex;
  flex-direction: column;
  gap: $space-2;

  .row {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .label {
      font-size: $font-sm;
      color: $color-text-secondary;
    }

    .label-with-tag {
      display: flex;
      align-items: center;
      gap: $space-1;

      .free-tag {
        font-size: $font-xs;
        background-color: $color-primary-bg;
        color: $color-primary;
        padding: 2rpx 8rpx;
        border-radius: 4rpx;
      }
    }

    .value {
      font-size: $font-sm;
      color: $color-text-primary;
      font-weight: $weight-medium;

      &.discount {
        color: $color-price;
      }

      &.line-through {
        text-decoration: line-through;
        color: $color-text-placeholder;
        font-weight: normal;
        font-size: $font-xs;
        margin-right: $space-2;
      }
    }

    .value-group {
      display: flex;
      align-items: center;
      gap: $space-1;
    }
  }

  .total-divider {
    height: 2rpx;
    background-color: $color-divider;
    margin: $space-2 0;
  }

  .total-row {
    display: flex;
    justify-content: flex-end;
    align-items: baseline;
    gap: $space-1;
    padding-top: $space-1;

    .total-label {
      font-size: $font-xs;
      color: $color-text-secondary;
    }

    .total-price {
      font-size: 48rpx;
      color: $color-primary;
      font-weight: bold;
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 112rpx;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $space-4;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;

  .closed-bar-tip {
    display: flex;
    align-items: center;
    gap: $space-1;

    .closed-bar-text {
      font-size: $font-sm;
      color: #C62828;
      font-weight: $weight-medium;
    }
  }

  .price-info {
    display: flex;
    flex-direction: column;

    .price-row {
      display: flex;
      align-items: baseline;
      gap: 4rpx;

      .label {
        font-size: $font-xs;
        color: $color-text-secondary;
      }

      .price {
        font-size: 44rpx;
        color: $color-primary;
        font-weight: bold;
      }
    }

    .discount-text {
      font-size: $font-xs;
      color: $color-price;
      font-weight: $weight-medium;
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
    margin: 0;
    
    &::after { border: none; }

    &.disabled {
      background-color: #BDBDBD;
      color: #ffffff;
    }
  }
}

.coupon-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.coupon-popup {
  width: 100%;
  max-height: 70vh;
  background-color: #ffffff;
  border-radius: $radius-lg $radius-lg 0 0;
  display: flex;
  flex-direction: column;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $space-4;
    border-bottom: 2rpx solid $color-divider;

    .popup-title {
      font-size: $font-lg;
      font-weight: $weight-semibold;
      color: $color-text-primary;
    }

    .popup-close {
      padding: $space-1;
    }
  }

  .popup-list {
    max-height: 60vh;
    padding: $space-3 $space-4;
  }

  .coupon-item {
    display: flex;
    align-items: stretch;
    margin-bottom: $space-3;
    border-radius: $radius-md;
    overflow: hidden;
    border: 2rpx solid $color-divider;
    transition: all 0.2s;

    &.selected {
      border-color: $color-primary;
      background-color: rgba($color-primary, 0.02);
    }

    &.disabled {
      opacity: 0.5;
    }

    .coupon-left {
      width: 180rpx;
      min-height: 160rpx;
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: center;
      gap: 2rpx;
      background: linear-gradient(135deg, #E53935, #FF7043);
      color: #ffffff;
      position: relative;
      flex-shrink: 0;

      &.no-discount {
        background: linear-gradient(135deg, #9E9E9E, #BDBDBD);
      }

      .coupon-symbol {
        font-size: $font-base;
        font-weight: $weight-semibold;
        line-height: 1;
        margin-bottom: 6rpx;
      }

      .coupon-amount {
        font-size: 56rpx;
        font-weight: bold;
        line-height: 1;
      }

      .no-amount {
        font-size: $font-base;
        font-weight: $weight-semibold;
      }
    }

    .coupon-right {
      flex: 1;
      padding: $space-3 $space-4;
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 4rpx;
      position: relative;

      .coupon-name {
        font-size: $font-base;
        font-weight: $weight-semibold;
        color: $color-text-primary;
      }

      .coupon-condition {
        font-size: $font-xs;
        color: $color-text-secondary;
      }

      .coupon-reason {
        font-size: $font-xs;
        color: $color-price;
      }

      .check-mark {
        position: absolute;
        top: $space-3;
        right: $space-3;
      }
    }
  }
}
</style>
