<template>
  <div>
    <!-- Page Header -->
    <div class="mb-8 flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-2">配送管理</h1>
        <p class="font-body-md text-body-md text-slate-500">协调您的物流网络并优化区域覆盖范围。</p>
      </div>
      <div class="flex bg-surface-container-low p-1 rounded-xl">
        <button class="px-6 py-2 bg-white shadow-sm rounded-lg font-label-bold text-label-bold text-primary flex items-center gap-2">
          <span class="material-symbols-outlined text-lg">store</span>
          自提点
        </button>
        <button class="px-6 py-2 font-label-bold text-label-bold text-slate-500 flex items-center gap-2 hover:text-primary transition-colors">
          <span class="material-symbols-outlined text-lg">settings_ethernet</span>
          配送设置
        </button>
      </div>
    </div>

    <!-- Dashboard Grid -->
    <div class="grid grid-cols-12 gap-6">
      <!-- Pickup Points Overview -->
      <div class="col-span-12 md:col-span-8 space-y-6">
        <div class="grid grid-cols-2 gap-6">
          <div v-for="point in pickupPoints" :key="point.name" class="bg-white border border-outline-variant p-6 rounded-xl hover:shadow-md transition-shadow group relative overflow-hidden">
            <div class="absolute top-0 right-0 p-4">
              <span class="text-xs font-bold px-2.5 py-1 rounded-full uppercase" :class="point.statusClass">{{ point.status }}</span>
            </div>
            <div class="mb-4">
              <div class="w-12 h-12 rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform" :class="point.iconBg">
                <span class="material-symbols-outlined" :class="point.iconColor" style="font-variation-settings: 'FILL' 1;">{{ point.icon }}</span>
              </div>
              <h3 class="font-h3 text-h3 mb-1">{{ point.name }}</h3>
              <p class="text-slate-500 font-body-sm text-body-sm">{{ point.address }}</p>
            </div>
            <div class="flex justify-between items-center pt-4 border-t border-slate-100">
              <div>
                <p class="text-[10px] text-slate-400 uppercase font-bold">库存数量</p>
                <p class="font-h3" :class="point.stockColor">{{ point.stock }} <span class="text-sm font-normal text-slate-400">单位</span></p>
              </div>
              <button class="p-2 hover:bg-slate-50 rounded-lg transition-colors">
                <span class="material-symbols-outlined text-slate-400">more_vert</span>
              </button>
            </div>
          </div>

          <!-- Add New Hub -->
          <button class="bg-slate-50 border-2 border-dashed border-slate-200 p-6 rounded-xl hover:bg-slate-100 hover:border-primary/50 transition-all flex flex-col items-center justify-center text-slate-400 group">
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
            <div class="flex gap-2">
              <button class="p-2 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors">
                <span class="material-symbols-outlined text-slate-600">draw</span>
              </button>
              <button class="p-2 border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors">
                <span class="material-symbols-outlined text-slate-600">zoom_in</span>
              </button>
            </div>
          </div>
          <div class="h-96 w-full relative bg-slate-200 flex items-center justify-center">
            <span class="material-symbols-outlined text-slate-400 text-6xl">map</span>
            <div class="absolute inset-0 bg-primary/10 border-4 border-primary/40 m-20 rounded-full flex items-center justify-center pointer-events-none">
              <div class="bg-white/90 backdrop-blur px-4 py-2 rounded-lg shadow-xl border border-primary/20 flex items-center gap-2">
                <span class="w-3 h-3 bg-primary rounded-full animate-pulse"></span>
                <span class="font-label-bold text-xs">标准区域 (15km)</span>
              </div>
            </div>
          </div>
          <div class="p-4 bg-surface-container-low flex items-center justify-between">
            <div class="flex gap-4">
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 bg-primary rounded-full"></span>
                <span class="text-xs font-medium">主中心范围</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 bg-secondary rounded-full"></span>
                <span class="text-xs font-medium">扩展区域</span>
              </div>
            </div>
            <span class="text-xs text-slate-400">最后更新：2 小时前</span>
          </div>
        </div>
      </div>

      <!-- Settings Sidebar -->
      <div class="col-span-12 md:col-span-4 space-y-6">
        <!-- Pricing Rules -->
        <div class="bg-white border border-outline-variant rounded-xl p-6 shadow-sm">
          <h3 class="font-h3 text-h3 mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">payments</span>
            定价规则
          </h3>
          <div class="space-y-4">
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">基础配送费</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
                <input class="w-full pl-8 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary outline-none" type="number" value="5.50" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">免运费阈值</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
                <input class="w-full pl-8 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary outline-none" type="number" value="50.00" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">加急费</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">¥</span>
                <input class="w-full pl-8 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary outline-none" type="number" value="3.00" />
              </div>
            </div>
            <button class="w-full py-3 bg-secondary-container text-on-secondary-container font-label-bold rounded-lg hover:bg-secondary-container/80 transition-colors">
              更新定价
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
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">时段间隔</label>
              <select class="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:ring-primary focus:border-primary text-sm outline-none">
                <option>30 分钟</option>
                <option selected>60 分钟 (标准)</option>
                <option>2 小时</option>
                <option>4 小时</option>
              </select>
            </div>
            <div v-for="slot in timeSlots" :key="slot.label" class="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-200" :class="{ 'opacity-50': !slot.active }">
              <span class="font-body-md text-body-md">{{ slot.label }}</span>
              <label class="relative inline-flex items-center cursor-pointer">
                <input class="sr-only peer" type="checkbox" :checked="slot.active" />
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
              </label>
            </div>
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

    <!-- Delivery Center Status Table -->
    <div class="mt-12 bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div class="p-6 border-b border-slate-100 flex items-center justify-between">
        <h3 class="font-h3 text-h3">配送中心实时状态</h3>
        <div class="flex gap-2">
          <button class="px-4 py-2 border border-slate-200 rounded-lg font-label-bold text-xs text-slate-600 hover:bg-slate-50 transition-colors">导出 CSV</button>
          <button class="px-4 py-2 bg-slate-900 text-white rounded-lg font-label-bold text-xs hover:bg-slate-800 transition-colors">生成标签</button>
        </div>
      </div>
      <table class="w-full text-left">
        <thead>
          <tr class="bg-slate-50 border-b border-slate-100 h-10">
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">站点名称</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">当前负荷</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">当日订单</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">处理效率</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase">健康状况</th>
            <th class="px-6 font-table-header text-table-header text-slate-500 uppercase"></th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="center in deliveryCenters" :key="center.name" class="hover:bg-slate-50 transition-colors h-[48px]">
            <td class="px-6 font-body-md text-body-md font-semibold">{{ center.name }}</td>
            <td class="px-6">
              <div class="w-full max-w-[120px] h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full rounded-full" :class="center.loadColor" :style="{ width: center.loadWidth }"></div>
              </div>
            </td>
            <td class="px-6 font-body-md text-body-md">{{ center.orders }}</td>
            <td class="px-6 font-body-md text-body-md">{{ center.efficiency }}</td>
            <td class="px-6">
              <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold" :class="center.healthClass">{{ center.health }}</span>
            </td>
            <td class="px-6 text-right">
              <button class="text-primary hover:underline text-xs font-bold">详情</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- FAB -->
    <button class="fixed bottom-8 right-8 w-14 h-14 bg-primary text-white rounded-full shadow-2xl flex items-center justify-center hover:scale-110 active:scale-95 transition-all z-50">
      <span class="material-symbols-outlined text-3xl">add_location</span>
    </button>
  </div>
</template>

<script setup lang="ts">
const pickupPoints = [
  { name: '中心果园配送站', address: '加利福尼亚州 90210 有机区绿巷 45 号', icon: 'location_on', iconBg: 'bg-green-50 text-green-700', status: '运营中', statusClass: 'bg-green-100 text-green-700', stock: '1,240', stockColor: 'text-primary' },
  { name: '北区浆果仓库', address: '加利福尼亚州 90552 高地高地磨砂路 88 号', icon: 'warehouse', iconBg: 'bg-amber-50 text-amber-600', status: '容量预警', statusClass: 'bg-amber-100 text-amber-700', stock: '3,890', stockColor: 'text-amber-600' },
  { name: '东区丰收商店', address: '加利福尼亚州 90001 市中心日出大道 12 号', icon: 'shopping_basket', iconBg: 'bg-green-50 text-green-700', status: '运营中', statusClass: 'bg-green-100 text-green-700', stock: '856', stockColor: 'text-primary' }
]

const timeSlots = [
  { label: '早班 (08-12)', active: true },
  { label: '中班 (13-17)', active: true },
  { label: '晚班 (18-22)', active: false }
]

const deliveryCenters = [
  { name: '中心果园配送站', loadWidth: '33%', loadColor: 'bg-green-500', orders: 142, efficiency: '94%', health: '良好', healthClass: 'bg-green-100 text-green-700' },
  { name: '北区浆果仓库', loadWidth: '85%', loadColor: 'bg-amber-500', orders: 288, efficiency: '81%', health: '接近上限', healthClass: 'bg-amber-100 text-amber-700' },
  { name: '东区丰收商店', loadWidth: '25%', loadColor: 'bg-green-500', orders: 65, efficiency: '99%', health: '良好', healthClass: 'bg-green-100 text-green-700' }
]
</script>
