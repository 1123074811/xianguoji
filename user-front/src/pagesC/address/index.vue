<template>
  <view class="address-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="left">
        <svg-icon name="arrow-back" :size="40" color="#2E7D32" @click="goBack" />
        <text class="title">鲜果记</text>
      </view>
      <svg-icon name="chat" :size="40" color="#2E7D32" />
    </view>

    <scroll-view scroll-y class="main-scroll">
      <view class="section-title">收货地址</view>
      
      <view class="address-list">
        <view v-for="addr in addresses" :key="addr.id" class="address-card card" @tap="handleSelect(addr)">
          <view class="left">
            <view class="user-row">
              <text class="name">{{ addr.name }}</text>
              <text class="phone">{{ addr.phone }}</text>
              <view v-if="addr.isDefault" class="tag default">默认</view>
              <view class="tag type">{{ addr.tag }}</view>
            </view>
            <text class="detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</text>
          </view>
          <view class="edit-btn" @tap.stop="handleEdit(addr)">
            <svg-icon name="edit" :size="40" color="#BDBDBD" />
          </view>
        </view>
      </view>

      <!-- New Address Form -->
      <view class="new-address card-dashed">
        <view class="form-title">新增收货地址</view>
        <view class="form-grid">
          <view class="form-item">
            <text class="label">联系人</text>
            <input class="input" placeholder="收货人姓名" v-model="newAddr.name" />
          </view>
          <view class="form-item">
            <text class="label">手机号码</text>
            <input class="input" placeholder="11位手机号" type="tel" v-model="newAddr.phone" />
          </view>
        </view>
        <view class="form-item">
          <text class="label">所在地区</text>
          <view class="input-with-icon">
            <input class="input" placeholder="省市区县、乡镇等" v-model="newAddr.region" />
            <svg-icon name="location" :size="32" color="#BDBDBD" />
          </view>
        </view>
        <view class="form-item">
          <text class="label">详细地址</text>
          <textarea class="textarea" placeholder="街道、楼牌号等" rows="2" v-model="newAddr.detail"></textarea>
        </view>
        <view class="switch-row">
          <text class="label">设为默认地址</text>
          <switch :checked="newAddr.isDefault" @change="newAddr.isDefault = $event.detail.value" color="#2E7D32" />
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Bar -->
    <view class="bottom-bar">
      <button class="save-btn" @tap="handleSave">保存并使用</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import SvgIcon from '@/components/svg-icon.vue';

const addresses = ref([
  {
    id: '1',
    name: '张伟',
    phone: '138****5678',
    isDefault: true,
    tag: '家',
    province: '北京市',
    city: '朝阳区',
    district: '三里屯街道',
    detail: '幸福二村40号楼 3单元 502室'
  },
  {
    id: '2',
    name: '李娜',
    phone: '135****1234',
    isDefault: false,
    tag: '公司',
    province: '上海市',
    city: '浦东新区',
    district: '陆家嘴环路',
    detail: '1000号 恒生银行大厦 22层'
  }
]);

const newAddr = ref({
  name: '',
  phone: '',
  region: '',
  detail: '',
  isDefault: false
});

function goBack() {
  uni.navigateBack();
}

function handleSelect(addr: any) {
  // 选择地址逻辑
}

function handleEdit(addr: any) {
  // 编辑地址逻辑
}

function handleSave() {
  uni.showToast({ title: '地址已保存', icon: 'success' });
}
</script>

<style lang="scss" scoped>
.address-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $color-bg-page;
}

.header-nav {
  background-color: #ffffff;
  padding: $space-2 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 100;
  border-bottom: 2rpx solid rgba($color-divider, 0.5);

  .left {
    display: flex;
    align-items: center;
    gap: $space-2;
    .iconfont { font-size: 40rpx; color: $color-primary; }
    .title { font-size: $font-lg; font-weight: bold; color: $color-primary; }
  }

  .iconfont { font-size: 40rpx; color: $color-primary; }
}

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: 0 $space-4;
}

.section-title {
  font-size: $font-md;
  font-weight: bold;
  padding: $space-5 0 $space-3;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  margin-bottom: $space-5;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: $space-4;
  border: 2rpx solid transparent;

  .left {
    flex: 1;
    .user-row {
      display: flex;
      align-items: center;
      gap: $space-2;
      margin-bottom: $space-1;
      
      .name { font-size: $font-base; font-weight: bold; }
      .phone { font-size: $font-sm; color: $color-text-secondary; }
      
      .tag {
        font-size: 18rpx;
        padding: 2rpx $space-1;
        border-radius: 4rpx;
        font-weight: bold;
        &.default { background-color: $color-primary; color: #ffffff; }
        &.type { background-color: rgba($color-primary, 0.1); color: $color-primary; }
      }
    }
    
    .detail {
      font-size: $font-sm;
      color: $color-text-secondary;
      line-height: 1.5;
    }
  }

  .edit-btn {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-text-placeholder;
    .iconfont { font-size: 40rpx; }
  }
}

.card-dashed {
  background-color: rgba($color-bg-card, 0.5);
  border: 2rpx dashed $color-divider;
  padding: $space-4;
  border-radius: $radius-md;
  margin-bottom: 120rpx;

  .form-title { font-size: $font-base; font-weight: bold; color: $color-primary; margin-bottom: $space-4; }

  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: $space-3;
  }

  .form-item {
    margin-bottom: $space-4;
    .label { font-size: $font-xs; color: $color-text-secondary; margin-bottom: 8rpx; display: block; }
    .input, .textarea {
      background-color: #ffffff;
      border: 2rpx solid $color-divider;
      border-radius: $radius-sm;
      padding: $space-2;
      font-size: $font-sm;
      width: 100%;
    }
    
    .input-with-icon {
      position: relative;
      .iconfont {
        position: absolute;
        right: $space-2;
        top: 50%;
        transform: translateY(-50%);
        color: $color-text-placeholder;
      }
    }
  }

  .switch-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .label { font-size: $font-base; }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 112rpx;
  background-color: #ffffff;
  padding: 0 $space-4;
  display: flex;
  align-items: center;
  box-shadow: 0 -2rpx 16rpx rgba(0,0,0,0.04);
  @include safe-area-bottom;

  .save-btn {
    width: 100%;
    height: 80rpx;
    background-color: $color-primary;
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after { border: none; }
  }
}
</style>
