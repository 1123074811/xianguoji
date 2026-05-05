<template>
  <view class="address-container">
    <!-- Top Nav -->
    <view class="header-nav">
      <view class="left" @tap="goBack">
        <svg-icon name="arrow-back" :size="40" color="#2E7D32" />
        <text class="title">收货地址</text>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll">
      <!-- Address List -->
      <view class="address-list">
        <view v-for="addr in addresses" :key="addr.id" class="address-card" @tap="handleSelect(addr)">
          <view class="left">
            <view class="user-row">
              <text class="name">{{ addr.consignee }}</text>
              <text class="phone">{{ addr.phone }}</text>
              <view v-if="addr.isDefault" class="tag default">默认</view>
              <view v-if="addr.tag" class="tag type">{{ addr.tag }}</view>
            </view>
            <text class="detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</text>
          </view>
          <view class="edit-btn" @tap.stop="handleEdit(addr)">
            <svg-icon name="edit" :size="36" color="#BDBDBD" />
          </view>
        </view>
      </view>

      <!-- New Address Form -->
      <view class="form-section">
        <view class="form-title">新增收货地址</view>

        <!-- Quick Import Buttons -->
        <view class="quick-import-row">
          <button class="import-btn wechat" @tap="importWxAddress">
            <svg-icon name="wechat" :size="28" color="#07C160" />
            <text>微信地址导入</text>
          </button>
          <button class="import-btn locate" @tap="getLocation">
            <svg-icon name="location" :size="28" color="#2E7D32" />
            <text>定位当前地址</text>
          </button>
        </view>

        <!-- Paste Import -->
        <view class="paste-row">
          <textarea
            class="paste-input"
            placeholder="粘贴收件信息一键导入\n如: 收件人: 张三\n手机号码: 13800138000\n所在地区: 广东省深圳市南山区\n详细地址: 科技园路1号"
            v-model="pasteText"
            :maxlength="-1"
          />
          <view class="paste-btn-wrap">
            <button class="paste-btn" @tap="parsePasteText">一键导入</button>
          </view>
        </view>

        <view class="divider" />

        <view class="form-grid">
          <view class="form-item">
            <text class="label">联系人</text>
            <input class="input" placeholder="收货人姓名" v-model="newAddr.consignee" />
          </view>
          <view class="form-item">
            <text class="label">手机号码</text>
            <input class="input" placeholder="11位手机号" type="tel" v-model="newAddr.phone" />
          </view>
        </view>
        <view class="form-item">
          <text class="label">所在地区</text>
          <region-picker v-model="newAddr.region" placeholder="请选择省/市/区" @change="onRegionChange" />
        </view>
        <view class="form-item">
          <text class="label">详细地址</text>
          <textarea class="textarea" placeholder="街道、楼牌号等" :maxlength="-1" v-model="newAddr.detail" />
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
import { ref, onMounted } from 'vue';
import { userApi } from '@/api/modules/user';
import SvgIcon from '@/components/svg-icon.vue';
import RegionPicker from '@/components/region-picker.vue';
import { provinceData } from '@/data/province';
import { cityData } from '@/data/city';
import { areaData } from '@/data/area';
import type { AddressVO } from '@/api/types/user';

const addresses = ref<AddressVO[]>([]);

async function loadAddresses() {
  try {
    addresses.value = await userApi.addressList();
  } catch (e) {
    console.warn('加载地址列表失败', e);
  }
}

onMounted(() => loadAddresses());

const pasteText = ref('');

const newAddr = ref({
  consignee: '',
  phone: '',
  region: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false as boolean | 0 | 1,
  tag: '',
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
});

function goBack() {
  uni.navigateBack();
}

function handleSelect(addr: AddressVO) {
  const pages = getCurrentPages();
  const prevPage = pages[pages.length - 2] as any;
  if (prevPage) {
    prevPage.$vm.selectedAddress = addr;
  }
  uni.navigateBack();
}

function handleEdit(addr: AddressVO) {
  newAddr.value = {
    consignee: addr.consignee,
    phone: addr.phone,
    region: `${addr.province}/${addr.city}/${addr.district}`,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
    isDefault: addr.isDefault,
    tag: addr.tag || '',
  };
  uni.pageScrollTo({ selector: '.form-section', duration: 300 });
}

function onRegionChange(result: { province: string; city: string; district: string }) {
  newAddr.value.province = result.province;
  newAddr.value.city = result.city;
  newAddr.value.district = result.district;
}

/** Parse pasted address text and fill form */
function parsePasteText() {
  const text = pasteText.value.trim();
  if (!text) {
    return uni.showToast({ title: '请先粘贴地址信息', icon: 'none' });
  }

  let consignee = '';
  let phone = '';
  let regionStr = '';
  let detail = '';

  // Try key-value format: "收件人: xxx" / "联系人: xxx"
  const kvPatterns: Record<string, string[]> = {
    consignee: ['收件人', '联系人', '姓名', '名字', '收货人'],
    phone: ['手机号码', '手机号', '电话', '联系方式', '联系电话'],
    regionStr: ['所在地区', '地区', '省市区', '地址', '收货地址'],
    detail: ['详细地址', '详细', '街道地址', '门牌号'],
  };

  const lines = text.split(/[\n\r]+/).map(l => l.trim()).filter(Boolean);

  for (const line of lines) {
    for (const [field, keywords] of Object.entries(kvPatterns)) {
      for (const kw of keywords) {
        const regex = new RegExp(`^${kw}[：:，,\\s]+(.+)$`);
        const match = line.match(regex);
        if (match) {
          const val = match[1].trim();
          if (field === 'consignee') consignee = val;
          else if (field === 'phone') phone = val.replace(/[^0-9]/g, '');
          else if (field === 'regionStr') regionStr = val;
          else if (field === 'detail') detail = val;
        }
      }
    }
  }

  // Fallback: if no key-value found, try parsing as single-line address
  if (!consignee && !phone && !regionStr && !detail) {
    // Try to extract phone number
    const phoneMatch = text.match(/1[3-9]\d{9}/);
    if (phoneMatch) phone = phoneMatch[0];

    // Try to extract name (2-4 Chinese chars before phone)
    const nameMatch = text.match(/([\u4e00-\u9fa5]{2,4})\s*(?:1[3-9]\d{9}|电话|手机)/);
    if (nameMatch) consignee = nameMatch[1];

    // Try to parse region from text
    const regionMatch = text.match(/([\u4e00-\u9fa5]+(?:省|自治区|特别行政区))\s*([\u4e00-\u9fa5]+(?:市|州|盟|地区))\s*([\u4e00-\u9fa5]+(?:区|县|市|旗))/);
    if (regionMatch) {
      regionStr = regionMatch[1] + regionMatch[2] + regionMatch[3];
    }
  }

  // Parse region string into province/city/district
  if (regionStr) {
    const parsed = matchRegionFromData(regionStr);
    if (parsed) {
      newAddr.value.province = parsed.province;
      newAddr.value.city = parsed.city;
      newAddr.value.district = parsed.district;
      newAddr.value.region = `${parsed.province}/${parsed.city}/${parsed.district}`;

      // Remove matched region part from detail if detail contains it
      if (detail && detail.startsWith(regionStr)) {
        detail = detail.substring(regionStr.length).replace(/^[\s,，、]+/, '');
      }
    }
  }

  if (consignee) newAddr.value.consignee = consignee;
  if (phone) newAddr.value.phone = phone;
  if (detail) newAddr.value.detail = detail;

  if (consignee || phone || regionStr || detail) {
    uni.showToast({ title: '导入成功', icon: 'success' });
    pasteText.value = '';
  } else {
    uni.showToast({ title: '未能识别地址信息', icon: 'none' });
  }
}

/** Match region string against province/city/area data */
function matchRegionFromData(regionStr: string) {
  // Try matching province
  for (const prov of provinceData) {
    if (!regionStr.startsWith(prov.name)) continue;
    const rest = regionStr.substring(prov.name.length);

    // Try matching city
    const provCities = cityData.filter(c => c.province === prov.province);
    for (const city of provCities) {
      if (!rest.startsWith(city.name)) continue;
      const rest2 = rest.substring(city.name.length);

      // Try matching district
      const cityDistricts = areaData.filter(a => a.province === prov.province && a.city === city.city);
      for (const dist of cityDistricts) {
        if (rest2.startsWith(dist.name)) {
          return { province: prov.name, city: city.name, district: dist.name };
        }
      }
      // No district match, return province + city
      return { province: prov.name, city: city.name, district: '' };
    }
  }
  return null;
}

/** Import address from WeChat payment address book */
function importWxAddress() {
  uni.chooseAddress({
    success(res: any) {
      newAddr.value.consignee = res.userName || '';
      newAddr.value.phone = res.telNumber || '';
      newAddr.value.province = res.provinceName || '';
      newAddr.value.city = res.cityName || '';
      newAddr.value.district = res.countyName || '';
      newAddr.value.region = `${res.provinceName || ''}/${res.cityName || ''}/${res.countyName || ''}`;
      newAddr.value.detail = res.detailInfo || '';
      newAddr.value.isDefault = false;
      uni.showToast({ title: '导入成功', icon: 'success' });
    },
    fail(err: any) {
      if (err.errMsg?.includes('cancel')) return;
      uni.showToast({ title: '获取微信地址失败', icon: 'none' });
    },
  });
}

/** Get current location and fill address via reverse geocode */
function getLocation() {
  uni.showLoading({ title: '定位中...' });
  uni.getLocation({
    type: 'gcj02',
    success(loc) {
      uni.hideLoading();
      reverseGeocode(loc.latitude, loc.longitude);
    },
    fail() {
      uni.hideLoading();
      uni.showModal({
        title: '位置授权',
        content: '需要获取您的位置信息来填充所在地区，是否前往设置开启？',
        success(modalRes) {
          if (modalRes.confirm) {
            uni.openSetting({
              success(settingRes: any) {
                if (settingRes.authSetting?.['scope.userLocation']) {
                  setTimeout(() => getLocation(), 300);
                }
              },
            });
          }
        },
      });
    },
  });
}

/** Reverse geocode coordinates to address via Tencent Map */
function reverseGeocode(latitude: number, longitude: number) {
  uni.request({
    url: 'https://apis.map.qq.com/ws/geocoder/v1/',
    data: {
      location: `${latitude},${longitude}`,
      key: import.meta.env.VITE_TENCENT_MAP_KEY,
      get_poi: 0,
    },
    success(res: any) {
      const data = res.data?.result;
      if (data?.address_component) {
        const comp = data.address_component;
        const province = comp.province || '';
        const city = comp.city || '';
        const district = comp.district || '';
        if (province && city) {
          newAddr.value.province = province;
          newAddr.value.city = city;
          newAddr.value.district = district;
          newAddr.value.region = `${province}/${city}/${district}`;
          newAddr.value.longitude = longitude;
          newAddr.value.latitude = latitude;
          if (data.address && !newAddr.value.detail) {
            const street = data.address.replace(province + city + district, '').replace(/^[\s,，、]+/, '');
            newAddr.value.detail = street;
          }
          uni.showToast({ title: '定位成功', icon: 'success' });
        }
      } else {
        uni.showToast({ title: '解析地址失败', icon: 'none' });
      }
    },
    fail() {
      uni.showToast({ title: '逆地理编码失败', icon: 'none' });
    },
  });
}

async function handleSave() {
  if (!newAddr.value.consignee || !newAddr.value.phone || !newAddr.value.province || !newAddr.value.detail) {
    return uni.showToast({ title: '请填写完整地址信息', icon: 'none' });
  }
  try {
    await userApi.addAddress({
      consignee: newAddr.value.consignee,
      phone: newAddr.value.phone,
      province: newAddr.value.province,
      city: newAddr.value.city,
      district: newAddr.value.district,
      detail: newAddr.value.detail,
      isDefault: newAddr.value.isDefault ? 1 : 0,
      tag: newAddr.value.tag || undefined,
      longitude: newAddr.value.longitude,
      latitude: newAddr.value.latitude,
    });
    uni.showToast({ title: '地址已保存', icon: 'success' });
    // Reset form
    newAddr.value = {
      consignee: '', phone: '', region: '', province: '', city: '', district: '',
      detail: '', isDefault: false, tag: '', longitude: undefined, latitude: undefined,
    };
    loadAddresses();
  } catch (e) {
    console.warn('保存地址失败', e);
  }
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
  padding: $space-3 $space-4;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.main-scroll {
  flex: 1;
  overflow: hidden;
  padding: 0 $space-4;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: $space-3;
  padding-top: $space-4;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: $space-4;
  background-color: #ffffff;
  border-radius: $radius-lg;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);

  .left {
    flex: 1;
    .user-row {
      display: flex;
      align-items: center;
      gap: $space-2;
      margin-bottom: $space-1;
      .name { font-size: $font-base; font-weight: $weight-semibold; }
      .phone { font-size: $font-sm; color: $color-text-secondary; }
      .tag {
        font-size: 18rpx;
        padding: 2rpx $space-1;
        border-radius: 4rpx;
        font-weight: $weight-medium;
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
  }
}

.form-section {
  margin-top: $space-5;
  background-color: rgba($color-bg-card, 0.5);
  border: 2rpx dashed $color-divider;
  padding: $space-4;
  border-radius: $radius-lg;
  margin-bottom: 160rpx;

  .form-title {
    font-size: $font-base;
    font-weight: $weight-semibold;
    color: $color-primary;
    margin-bottom: $space-4;
  }
}

.quick-import-row {
  display: flex;
  gap: $space-3;
  margin-bottom: $space-4;

  .import-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $space-1;
    height: 72rpx;
    border-radius: $radius-sm;
    font-size: $font-sm;
    font-weight: $weight-semibold;
    &::after { border: none; }

    .import-icon {
      font-size: $font-md;
    }

    &.wechat {
      background-color: rgba(7, 193, 96, 0.1);
      color: #07C160;
      border: 2rpx solid rgba(7, 193, 96, 0.3);
    }

    &.locate {
      background-color: rgba($color-primary, 0.08);
      color: $color-primary;
      border: 2rpx solid rgba($color-primary, 0.3);
    }
  }
}

.paste-row {
  display: flex;
  gap: $space-2;
  margin-bottom: $space-3;

  .paste-input {
    flex: 1;
    background-color: #ffffff;
    border: 2rpx solid $color-divider;
    border-radius: $radius-sm;
    padding: $space-3;
    font-size: $font-sm;
    height: 240rpx;
    width: auto;
    line-height: 1.8;
  }

  .paste-btn-wrap {
    display: flex;
    align-items: flex-end;
  }

  .paste-btn {
    background-color: $color-primary;
    color: #ffffff;
    font-size: $font-sm;
    font-weight: $weight-semibold;
    border-radius: $radius-sm;
    padding: 0 $space-3;
    height: 72rpx;
    line-height: 72rpx;
    white-space: nowrap;
    &::after { border: none; }
  }
}

.divider {
  height: 2rpx;
  background-color: $color-divider;
  margin: $space-3 0 $space-4;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-3;
}

.form-item {
  margin-bottom: $space-4;

  .label {
    font-size: $font-xs;
    color: $color-text-secondary;
    margin-bottom: 8rpx;
    display: block;
  }

  .input {
    background-color: #ffffff;
    border: 2rpx solid $color-divider;
    border-radius: $radius-sm;
    padding: $space-3 $space-3;
    font-size: $font-sm;
    width: 100%;
    height: 80rpx;
    box-sizing: border-box;
  }

  .textarea {
    background-color: #ffffff;
    border: 2rpx solid $color-divider;
    border-radius: $radius-sm;
    padding: $space-3;
    font-size: $font-sm;
    width: 100%;
    height: 160rpx;
    box-sizing: border-box;
  }
}


.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  .label { font-size: $font-base; }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: $space-3 $space-4;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.04);
  @include safe-area-bottom;

  .save-btn {
    width: 100%;
    height: 88rpx;
    background-color: $color-primary;
    color: #ffffff;
    border-radius: $radius-pill;
    font-size: $font-base;
    font-weight: $weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
    &::after { border: none; }
  }
}
</style>
