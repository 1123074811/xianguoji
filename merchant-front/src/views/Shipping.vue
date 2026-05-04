<template>
  <div>
    <!-- Page Header -->
    <div class="mb-8 flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-2">配送管理</h1>
        <p class="font-body-md text-body-md text-slate-500">协调您的物流网络并优化区域覆盖范围。</p>
      </div>
      <div class="flex bg-surface-container-low p-1 rounded-xl">
        <button @click="activeTab = 'pickup'" class="px-6 py-2 rounded-lg font-label-bold text-label-bold flex items-center gap-2 transition-colors"
          :class="activeTab === 'pickup' ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-primary'">
          <span class="material-symbols-outlined text-lg">store</span>
          自提点
        </button>
        <button @click="activeTab = 'delivery'" class="px-6 py-2 rounded-lg font-label-bold text-label-bold flex items-center gap-2 transition-colors"
          :class="activeTab === 'delivery' ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-primary'">
          <span class="material-symbols-outlined text-lg">settings_ethernet</span>
          配送设置
        </button>
      </div>
    </div>

    <!-- Dashboard Grid -->
    <div v-show="activeTab === 'pickup'" class="grid grid-cols-12 gap-6">
      <!-- Pickup Points Overview -->
      <div class="col-span-12 space-y-6">
        <div class="grid grid-cols-2 gap-6">
          <div v-for="(point, idx) in pickupPoints" :key="point.id" class="bg-white border border-outline-variant p-6 rounded-xl hover:shadow-md transition-shadow group relative overflow-hidden">
            <div class="absolute top-0 right-0 p-4">
              <span class="text-xs font-bold px-2.5 py-1 rounded-full uppercase bg-green-100 text-green-700">运营中</span>
            </div>
            <div class="mb-4">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform" :class="getPointIconBg(idx)">
                <span class="material-symbols-outlined" :class="getPointIconColor(idx)" style="font-variation-settings: 'FILL' 1;">{{ getPointIcon(idx) }}</span>
              </div>
              <h3 class="font-h3 text-h3 mb-1">{{ point.name }}</h3>
              <p class="text-slate-500 font-body-sm text-body-sm">{{ point.address }}</p>
              <p v-if="point.phone" class="text-slate-400 font-body-sm text-body-sm mt-1">{{ point.phone }}</p>
            </div>
            <div class="flex justify-between items-center pt-4 border-t border-slate-100">
              <div>
                <p class="text-[10px] text-slate-400 uppercase font-bold">营业时间</p>
                <p class="font-label-bold text-primary text-sm">{{ point.businessHours || '—' }}</p>
              </div>
              <div class="flex gap-1">
                <button class="p-2 hover:bg-primary/10 rounded-lg transition-colors" @click="openEditModal(point)" title="编辑">
                  <span class="material-symbols-outlined text-slate-400 hover:text-primary">edit</span>
                </button>
                <button class="p-2 hover:bg-error/10 rounded-lg transition-colors" @click="deletePickupPoint(point.id)" title="删除">
                  <span class="material-symbols-outlined text-slate-400 hover:text-error">delete</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Add New Hub -->
          <button @click="openCreateModal" class="bg-slate-50 border-2 border-dashed border-slate-200 p-6 rounded-xl hover:bg-slate-100 hover:border-primary/50 transition-all flex flex-col items-center justify-center text-slate-400 group">
            <div class="w-12 h-12 rounded-full border-2 border-dashed border-slate-300 flex items-center justify-center mb-3 group-hover:text-primary group-hover:border-primary transition-colors">
              <span class="material-symbols-outlined">add</span>
            </div>
            <span class="font-label-bold text-label-bold group-hover:text-primary">添加新自提点</span>
          </button>
        </div>

        <!-- Delivery Range Map Section -->
        <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
          <div class="p-6 border-b border-slate-100 flex justify-between items-center">
            <div>
              <h2 class="font-h2 text-h2 text-on-surface">配送服务范围</h2>
              <p class="font-body-sm text-body-sm text-slate-500">定义标准配送和高级配送的地理边界。</p>
            </div>
            <div class="flex items-center gap-3">
              <label class="flex items-center gap-2 text-xs text-slate-600">
                半径
                <input v-model.number="serviceRadiusKm" type="number" min="1" max="50" step="1" class="w-16 px-2 py-1 border border-slate-200 rounded text-center" />
                km
              </label>
              <button @click="saveServiceArea" :disabled="!mapReady || saving" class="px-4 py-1.5 bg-primary text-white text-xs font-label-bold rounded-lg hover:bg-primary/90 disabled:opacity-50 transition-colors">
                {{ saving ? '保存中…' : '保存范围' }}
              </button>
            </div>
          </div>
          <div class="h-96 w-full relative">
            <div ref="rangeMapRef" class="w-full h-full"></div>
            <div v-if="mapError" class="absolute inset-0 bg-slate-100 flex flex-col items-center justify-center text-center px-6">
              <span class="material-symbols-outlined text-slate-300 text-5xl">map_search</span>
              <p class="text-sm text-slate-500 mt-2">{{ mapError }}</p>
              <p class="text-xs text-slate-400 mt-1">请在 .env 中配置 VITE_AMAP_KEY 后重新启动</p>
            </div>
          </div>
          <div class="p-4 bg-surface-container-low flex items-center justify-between">
            <div class="flex gap-4">
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 bg-primary rounded-full"></span>
                <span class="text-xs font-medium">配送范围 ({{ serviceRadiusKm }}km)</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 bg-amber-500 rounded-full"></span>
                <span class="text-xs font-medium">自提点</span>
              </div>
            </div>
            <span class="text-xs text-slate-400">基于高德地图 · 共 {{ pickupPoints.length }} 个站点</span>
          </div>
        </div>
      </div>

    </div>

    <!-- Delivery Settings Tab -->
    <div v-show="activeTab === 'delivery'" class="grid grid-cols-12 gap-6">
      <!-- Settings Sidebar -->
      <div class="col-span-12 md:col-span-8 space-y-6">
        <!-- Pricing Rules -->
        <div class="bg-white border border-outline-variant rounded-xl p-6 shadow-sm">
          <h3 class="font-h3 text-h3 mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">payments</span>
            定价规则
          </h3>
          <div class="space-y-4">
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">起送价</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
                <input v-model="deliverySetting.minOrderAmount" class="w-full pl-8 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary outline-none" type="number" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">基础配送费</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
                <input v-model="deliverySetting.baseFee" class="w-full pl-8 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary outline-none" type="number" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">免运费阈值</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
                <input v-model="deliverySetting.freeAmount" class="w-full pl-8 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary outline-none" type="number" />
              </div>
            </div>
            <button @click="saveDeliverySetting" :disabled="saving" class="w-full py-3 bg-secondary-container text-on-secondary-container font-label-bold rounded-lg hover:bg-secondary-container/80 transition-colors disabled:opacity-50">
              {{ saving ? '保存中...' : '更新定价' }}
            </button>
          </div>
        </div>

        <!-- Time Slot Management -->
        <div class="bg-white border border-outline-variant rounded-xl p-6 shadow-sm">
          <h3 class="font-h3 text-h3 mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">schedule</span>
            配送时段
          </h3>
          <div class="space-y-4">
            <div v-if="deliverySetting.timeSlots && Array.isArray(deliverySetting.timeSlots)">
              <div v-for="(slot, si) in deliverySetting.timeSlots" :key="si" class="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-200">
                <span class="font-body-md text-body-md">{{ slot.label || slot }}</span>
              </div>
            </div>
            <div v-else class="text-sm text-slate-400">暂无时段数据，请在配送设置中配置</div>
          </div>
        </div>

        <!-- Logistics Health Card -->
        <div class="bg-primary text-white rounded-xl p-6 shadow-lg shadow-primary/20 relative overflow-hidden">
          <div class="absolute -right-4 -bottom-4 opacity-10">
            <span class="material-symbols-outlined text-[120px]">local_shipping</span>
          </div>
          <div class="relative z-10">
            <h4 class="font-label-bold text-white/80 uppercase text-[10px] tracking-widest mb-1">每周配送可靠性</h4>
            <div class="text-3xl font-bold mb-4">98.2%</div>
            <div class="flex items-center gap-2 text-xs bg-white/10 w-fit px-2 py-1 rounded">
              <span class="material-symbols-outlined text-sm">trending_up</span>
              <span>比上周增加 2.4%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pickup Point List Table -->
    <div v-show="activeTab === 'pickup'" class="mt-12 bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div class="p-6 border-b border-slate-100 flex items-center justify-between">
        <h3 class="font-h3 text-h3">自提点列表</h3>
        <span class="text-sm text-slate-400">共 {{ pickupPoints.length }} 个自提点</span>
      </div>
      <table class="w-full text-left">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100 h-10">
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">名称</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">地址</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">电话</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">营业时间</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">排序</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="point in pickupPoints" :key="point.id" class="hover:bg-slate-50 transition-colors h-[48px]">
            <td class="px-6 font-body-md text-body-md font-semibold">{{ point.name }}</td>
            <td class="px-6 font-body-md text-body-md text-slate-600">{{ point.address }}</td>
            <td class="px-6 font-body-md text-body-md">{{ point.phone || '—' }}</td>
            <td class="px-6 font-body-md text-body-md">{{ point.businessHours || '—' }}</td>
            <td class="px-6 font-body-md text-body-md">{{ point.sort }}</td>
            <td class="px-6 text-right">
              <button @click="deletePickupPoint(point.id)" class="text-error hover:underline text-xs font-bold">删除</button>
            </td>
          </tr>
          <tr v-if="pickupPoints.length === 0">
            <td colspan="6" class="px-6 py-8 text-center text-slate-400">暂无自提点数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- FAB -->
    <button @click="openCreateModal" class="fixed bottom-8 right-8 w-14 h-14 bg-primary text-white rounded-full shadow-2xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all z-50">
      <span class="material-symbols-outlined text-3xl">add_location</span>
    </button>

    <!-- Pickup Point Modal -->
    <div v-if="modalOpen" class="fixed inset-0 z-[60] bg-black/40 flex items-center justify-center p-4" @click.self="closeModal">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-h3 text-h3">{{ modalForm.id ? '编辑自提点' : '新增自提点' }}</h3>
          <button @click="closeModal" class="p-1 hover:bg-slate-100 rounded">
            <span class="material-symbols-outlined text-slate-500">close</span>
          </button>
        </div>
        <div class="flex-1 overflow-auto grid grid-cols-1 md:grid-cols-2 gap-0">
          <!-- Left: form -->
          <div class="p-6 space-y-4 border-r border-slate-100">
            <div>
              <label class="block text-xs font-label-bold text-slate-700 mb-1">名称 *</label>
              <input v-model="modalForm.name" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" placeholder="如：文三路自提点" />
            </div>
            <div>
              <label class="block text-xs font-label-bold text-slate-700 mb-1">地址 *</label>
              <input v-model="modalForm.address" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" placeholder="点击右侧地图选择，或手动输入" />
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-label-bold text-slate-700 mb-1">联系电话</label>
                <input v-model="modalForm.phone" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-xs font-label-bold text-slate-700 mb-1">营业时间</label>
                <input v-model="modalForm.businessHours" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" placeholder="08:00-22:00" />
              </div>
            </div>
            <div class="grid grid-cols-3 gap-3">
              <div>
                <label class="block text-xs font-label-bold text-slate-700 mb-1">经度</label>
                <input v-model.number="modalForm.longitude" type="number" step="0.000001" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-xs font-label-bold text-slate-700 mb-1">纬度</label>
                <input v-model.number="modalForm.latitude" type="number" step="0.000001" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" />
              </div>
              <div>
                <label class="block text-xs font-label-bold text-slate-700 mb-1">排序</label>
                <input v-model.number="modalForm.sort" type="number" class="w-full px-3 py-2 border border-slate-200 rounded-lg focus:ring-1 focus:ring-primary outline-none" />
              </div>
            </div>
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-3 text-xs text-blue-700 flex gap-2">
              <span class="material-symbols-outlined text-sm">info</span>
              <span>右侧地图可拖拽 / 点击选点；选点后将自动反向解析地址。</span>
            </div>
          </div>
          <!-- Right: map picker -->
          <div class="relative">
            <div ref="modalMapRef" class="w-full h-full min-h-[300px]"></div>
            <div v-if="mapError" class="absolute inset-0 bg-slate-100 flex flex-col items-center justify-center text-center px-6">
              <span class="material-symbols-outlined text-slate-300 text-5xl">map_search</span>
              <p class="text-sm text-slate-500 mt-2">{{ mapError }}</p>
            </div>
          </div>
        </div>
        <div class="px-6 py-4 border-t border-slate-100 flex justify-end gap-3 bg-slate-50">
          <button @click="closeModal" class="px-5 py-2 border border-slate-200 rounded-lg text-slate-600 hover:bg-white text-sm">取消</button>
          <button @click="submitModal" :disabled="saving" class="px-6 py-2 bg-primary text-white font-label-bold rounded-lg hover:bg-primary/90 disabled:opacity-50 text-sm">
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { adminShopApi } from '@/api/modules/shop'
import type { AdminPickupPointVO, AdminDeliverySettingVO } from '@/api/types/shop'
import { loadAmap, isAmapKeyConfigured } from '@/utils/amap'
import { toast } from '@/utils/toast'

const activeTab = ref<'pickup' | 'delivery'>('pickup')
const pickupPoints = ref<AdminPickupPointVO[]>([])
const deliverySetting = ref<Partial<AdminDeliverySettingVO>>({})
const saving = ref(false)

// ===== 地图相关 =====
const rangeMapRef = ref<HTMLDivElement | null>(null)
const modalMapRef = ref<HTMLDivElement | null>(null)
const mapError = ref('')
const mapReady = ref(false)
const serviceRadiusKm = ref(15)

// 默认中心：杭州市文三路（与 shop 种子数据一致）
const defaultCenter: [number, number] = [120.130202, 30.281797]

let AMap: any = null
let rangeMap: any = null
let rangeCircle: any = null
let rangeMarkers: any[] = []
let modalMap: any = null
let modalMarker: any = null
let geocoder: any = null

async function ensureAmap() {
  if (AMap) return AMap
  if (!isAmapKeyConfigured()) {
    mapError.value = '未配置高德地图 Key'
    throw new Error('no key')
  }
  try {
    AMap = await loadAmap(['AMap.Geocoder'])
    geocoder = new AMap.Geocoder({})
    return AMap
  } catch (e: any) {
    mapError.value = e?.message || '地图加载失败'
    throw e
  }
}

async function initRangeMap() {
  await nextTick()
  if (!rangeMapRef.value) return
  try {
    await ensureAmap()
  } catch {
    return
  }
  rangeMap = new AMap.Map(rangeMapRef.value, {
    zoom: 12,
    center: getServiceCenter(),
  })
  rangeCircle = new AMap.Circle({
    center: getServiceCenter(),
    radius: serviceRadiusKm.value * 1000,
    strokeColor: '#2e7d32',
    strokeOpacity: 0.6,
    strokeWeight: 2,
    fillColor: '#2e7d32',
    fillOpacity: 0.12,
  })
  rangeCircle.setMap(rangeMap)
  refreshRangeMarkers()
  mapReady.value = true
}

function getServiceCenter(): [number, number] {
  const sa: any = deliverySetting.value.serviceArea
  if (sa && Array.isArray(sa.center) && sa.center.length === 2) {
    return [Number(sa.center[0]), Number(sa.center[1])]
  }
  return defaultCenter
}

function refreshRangeMarkers() {
  if (!rangeMap || !AMap) return
  rangeMarkers.forEach(m => m.setMap(null))
  rangeMarkers = []
  pickupPoints.value.forEach(p => {
    if (p.longitude && p.latitude) {
      const marker = new AMap.Marker({
        position: [Number(p.longitude), Number(p.latitude)],
        title: p.name,
      })
      marker.setMap(rangeMap)
      rangeMarkers.push(marker)
    }
  })
}

watch(serviceRadiusKm, v => {
  if (rangeCircle) rangeCircle.setRadius((Number(v) || 0) * 1000)
})

watch(pickupPoints, () => refreshRangeMarkers(), { deep: true })

async function saveServiceArea() {
  if (!rangeCircle) return
  saving.value = true
  try {
    const center = rangeCircle.getCenter()
    const payload = {
      ...deliverySetting.value,
      serviceArea: { center: [center.lng, center.lat], radius: serviceRadiusKm.value * 1000 },
    }
    await adminShopApi.updateDeliverySetting(payload)
    deliverySetting.value = payload
    toast.success('配送范围已保存')
  } catch (e) {
    console.warn('保存配送范围失败', e)
    toast.error('保存配送范围失败')
  } finally {
    saving.value = false
  }
}

// ===== Modal =====
type PickupForm = Partial<AdminPickupPointVO> & { id?: number }
const modalOpen = ref(false)
const modalForm = ref<PickupForm>({})

function emptyForm(): PickupForm {
  return { name: '', address: '', phone: '', businessHours: '08:00-22:00', longitude: defaultCenter[0], latitude: defaultCenter[1], sort: 0 }
}

async function openCreateModal() {
  modalForm.value = emptyForm()
  modalOpen.value = true
  await initModalMap()
}

async function openEditModal(p: AdminPickupPointVO) {
  modalForm.value = { ...p }
  modalOpen.value = true
  await initModalMap()
}

function closeModal() {
  modalOpen.value = false
  if (modalMap) {
    modalMap.destroy()
    modalMap = null
    modalMarker = null
  }
}

async function initModalMap() {
  await nextTick()
  if (!modalMapRef.value) return
  try {
    await ensureAmap()
  } catch {
    return
  }
  const center: [number, number] = [
    Number(modalForm.value.longitude) || defaultCenter[0],
    Number(modalForm.value.latitude) || defaultCenter[1],
  ]
  modalMap = new AMap.Map(modalMapRef.value, { zoom: 14, center })
  modalMarker = new AMap.Marker({ position: center, draggable: true })
  modalMarker.setMap(modalMap)
  modalMap.on('click', (e: any) => {
    setModalPos([e.lnglat.getLng(), e.lnglat.getLat()])
  })
  modalMarker.on('dragend', (e: any) => {
    setModalPos([e.lnglat.getLng(), e.lnglat.getLat()])
  })
}

function setModalPos(lnglat: [number, number]) {
  modalForm.value.longitude = Number(lnglat[0].toFixed(6))
  modalForm.value.latitude = Number(lnglat[1].toFixed(6))
  if (modalMarker) modalMarker.setPosition(lnglat)
  // 反向地理编码自动填地址
  if (geocoder) {
    geocoder.getAddress(lnglat, (status: string, result: any) => {
      if (status === 'complete' && result.regeocode) {
        modalForm.value.address = result.regeocode.formattedAddress
      }
    })
  }
}

async function submitModal() {
  const f = modalForm.value
  if (!f.name || !f.address) {
    toast.warning('请填写名称与地址')
    return
  }
  saving.value = true
  try {
    const isEdit = !!f.id
    if (f.id) {
      await adminShopApi.updatePickupPoint(f.id, f)
    } else {
      await adminShopApi.createPickupPoint(f as Omit<AdminPickupPointVO, 'id'>)
    }
    closeModal()
    await loadPickupPoints()
    toast.success(isEdit ? '自提点已更新' : '自提点已创建')
  } catch (e) {
    console.warn('保存自提点失败', e)
    toast.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

// ===== 数据加载 =====
async function loadPickupPoints() {
  try {
    pickupPoints.value = await adminShopApi.pickupPointList()
  } catch (e) {
    console.warn('加载自提点失败', e)
  }
}

async function loadDeliverySetting() {
  try {
    deliverySetting.value = (await adminShopApi.deliverySetting()) || {}
    const sa: any = deliverySetting.value.serviceArea
    if (sa && sa.radius) serviceRadiusKm.value = Math.round(Number(sa.radius) / 1000)
  } catch (e) {
    console.warn('加载配送设置失败', e)
  }
}

async function saveDeliverySetting() {
  saving.value = true
  try {
    await adminShopApi.updateDeliverySetting(deliverySetting.value)
  } catch (e) {
    console.warn('保存配送设置失败', e)
  } finally {
    saving.value = false
  }
}

async function deletePickupPoint(id: number) {
  if (!confirm('确定删除该自提点？')) return
  try {
    await adminShopApi.deletePickupPoint(id)
    await loadPickupPoints()
    toast.success('自提点已删除')
  } catch (e) {
    console.warn('删除自提点失败', e)
    toast.error('删除失败，请稍后重试')
  }
}

const pointIcons = ['location_on', 'warehouse', 'shopping_basket', 'store', 'apartment']
const pointIconBgs = ['bg-green-50', 'bg-amber-50', 'bg-blue-50', 'bg-purple-50', 'bg-orange-50']
const pointIconColors = ['text-green-700', 'text-amber-600', 'text-blue-600', 'text-purple-600', 'text-orange-600']

function getPointIcon(index: number) { return pointIcons[index % pointIcons.length] }
function getPointIconBg(index: number) { return pointIconBgs[index % pointIconBgs.length] }
function getPointIconColor(index: number) { return pointIconColors[index % pointIconColors.length] }

onMounted(async () => {
  await Promise.all([loadPickupPoints(), loadDeliverySetting()])
  initRangeMap()
})

onBeforeUnmount(() => {
  if (rangeMap) rangeMap.destroy()
  if (modalMap) modalMap.destroy()
})
</script>
