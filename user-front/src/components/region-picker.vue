<template>
  <view class="region-picker" @tap="openPicker">
    <view class="picker-trigger">
      <text class="display" :class="{ placeholder: !selectedText }" :style="{ fontSize: '28rpx', padding: '16rpx', background: '#fff', border: '2rpx solid #e0e0e0', borderRadius: '8rpx', minHeight: '72rpx', lineHeight: '40rpx', display: 'block', color: selectedText ? '#333' : '#999' }">{{ selectedText || placeholder }}</text>
    </view>
  </view>

  <!-- Popup Picker -->
  <view v-if="visible" class="picker-mask" @tap="cancelPicker">
    <view class="picker-popup" @tap.stop>
      <view class="picker-header">
        <text class="picker-cancel" @tap="cancelPicker">取消</text>
        <text class="picker-title">选择地区</text>
        <text class="picker-confirm" @tap="confirmPicker">确定</text>
      </view>
      <picker-view :value="pickerValue" class="picker-view" @change="onPickerChange">
        <picker-view-column>
          <view v-for="item in provinceData" :key="item.code" class="picker-item">{{ item.name }}</view>
        </picker-view-column>
        <picker-view-column>
          <view v-for="item in curCities" :key="item.code" class="picker-item">{{ item.name }}</view>
        </picker-view-column>
        <picker-view-column>
          <view v-for="item in curDistricts" :key="item.code" class="picker-item">{{ item.name }}</view>
        </picker-view-column>
      </picker-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { provinceData } from '@/data/province';
import { cityData } from '@/data/city';
import { areaData } from '@/data/area';

interface RegionResult {
  province: string;
  city: string;
  district: string;
  provinceCode: string;
  cityCode: string;
  districtCode: string;
}

const props = withDefaults(defineProps<{
  modelValue?: string;
  placeholder?: string;
}>(), {
  placeholder: '请选择省/市/区',
});

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'change', result: RegionResult): void;
}>();

const visible = ref(false);
const pickerValue = ref([0, 0, 0]);
const confirmedValue = ref([0, 0, 0]);

const curCities = computed(() => {
  const provCode = provinceData[pickerValue.value[0]]?.province;
  if (!provCode) return [];
  return cityData.filter(c => c.province === provCode);
});

const curDistricts = computed(() => {
  const provCode = provinceData[pickerValue.value[0]]?.province;
  const cityItem = curCities.value[pickerValue.value[1]];
  if (!cityItem) return [];
  return areaData.filter(a => a.province === provCode && a.city === cityItem.city);
});

const selectedText = computed(() => {
  const [pi, ci, di] = confirmedValue.value;
  const provItem = provinceData[pi];
  if (!provItem) return '';
  const prov = provItem.name;
  const fCities = cityData.filter(c => c.province === provItem.province);
  const city = fCities[ci]?.name;
  const distCityCode = fCities[ci]?.city;
  const fDistricts = areaData.filter(a => a.province === provItem.province && a.city === distCityCode);
  const dist = fDistricts[di]?.name;
  if (prov && city && dist) return `${prov} ${city} ${dist}`;
  if (prov && city) return `${prov} ${city}`;
  if (prov) return prov;
  return '';
});

// Init from modelValue
watch(() => props.modelValue, (val) => {
  if (!val) return;
  const parts = val.split('/');
  if (parts.length < 3) return;
  const [provName, cityName, distName] = parts;

  const pi = provinceData.findIndex(p => p.name === provName);
  if (pi < 0) return;

  const provCode = provinceData[pi]?.province;
  const filteredCities = cityData.filter(c => c.province === provCode);
  const ci = filteredCities.findIndex(c => c.name === cityName);
  if (ci < 0) { confirmedValue.value = [pi, 0, 0]; return; }

  const cityItem = filteredCities[ci];
  const filteredDistricts = areaData.filter(a => a.province === provCode && a.city === cityItem.city);
  const di = filteredDistricts.findIndex(d => d.name === distName);
  if (di < 0) { confirmedValue.value = [pi, ci, 0]; return; }

  confirmedValue.value = [pi, ci, di];
}, { immediate: true });

function openPicker() {
  pickerValue.value = [...confirmedValue.value];
  visible.value = true;
}

function cancelPicker() {
  visible.value = false;
}

function onPickerChange(e: any) {
  const [pi, ci, di] = e.detail.value as number[];
  const newVal = [pi ?? 0, ci ?? 0, di ?? 0];

  // Province changed → reset city & district
  if (newVal[0] !== pickerValue.value[0]) {
    newVal[1] = 0;
    newVal[2] = 0;
  }
  // City changed → reset district
  if (newVal[1] !== pickerValue.value[1]) {
    newVal[2] = 0;
  }

  pickerValue.value = newVal;
}

function confirmPicker() {
  confirmedValue.value = [...pickerValue.value];
  const [pi, ci, di] = confirmedValue.value;

  const prov = provinceData[pi];
  const city = curCities.value[ci];
  const dist = curDistricts.value[di];

  if (!prov || !city || !dist) {
    visible.value = false;
    return;
  }

  const value = `${prov.name}/${city.name}/${dist.name}`;
  emit('update:modelValue', value);
  emit('change', {
    province: prov.name,
    city: city.name,
    district: dist.name,
    provinceCode: prov.code,
    cityCode: city.code,
    districtCode: dist.code,
  });

  visible.value = false;
}
</script>

<style lang="scss" scoped>
.region-picker {
  width: 100%;
}

.picker-trigger {
  width: 100%;
}

.display {
  font-size: $font-sm;
  color: $color-text-primary;
  padding: $space-2;
  background-color: #ffffff;
  border: 2rpx solid $color-divider;
  border-radius: $radius-sm;
  min-height: 72rpx;
  line-height: 40rpx;
  display: block;

  &.placeholder {
    color: $color-text-placeholder;
  }
}

.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.picker-popup {
  width: 100%;
  background-color: #ffffff;
  border-radius: $radius-lg $radius-lg 0 0;
  overflow: hidden;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-3 $space-4;
  border-bottom: 2rpx solid $color-divider;

  .picker-cancel {
    font-size: $font-base;
    color: $color-text-secondary;
  }

  .picker-title {
    font-size: $font-base;
    font-weight: $weight-semibold;
    color: $color-text-primary;
  }

  .picker-confirm {
    font-size: $font-base;
    color: $color-primary;
    font-weight: $weight-semibold;
  }
}

.picker-view {
  width: 100%;
  height: 480rpx;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: $font-base;
  color: $color-text-primary;
  height: 80rpx;
}
</style>
