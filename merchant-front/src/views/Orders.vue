<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h2 class="font-h2 text-h2 text-on-surface mb-1">订单管理</h2>
        <p class="font-body-sm text-body-sm text-on-surface-variant">高效管理和处理您的生鲜水果订单。</p>
      </div>
      <button class="bg-primary hover:bg-primary-container text-white px-stack-md py-2 rounded-lg font-label-bold flex items-center gap-2 transition-transform active:scale-95 shadow-sm">
        <span class="material-symbols-outlined text-[18px]">add</span>
        创建手工订单
      </button>
    </div>

    <!-- Tabs Section -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden mb-stack-md">
      <div class="flex border-b border-outline-variant overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.label" @click="activeTab = tab.label"
          class="px-6 py-4 font-label-bold whitespace-nowrap transition-colors"
          :class="activeTab === tab.label ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant hover:text-primary border-b-2 border-transparent'">
          {{ tab.label }}
          <span v-if="tab.badge" class="ml-1 px-1.5 py-0.5 bg-error-container text-on-error-container rounded-full text-[10px]">{{ tab.badge }}</span>
        </button>
      </div>

      <!-- Filter Bar -->
      <div class="p-stack-md grid grid-cols-1 md:grid-cols-4 lg:grid-cols-12 gap-stack-md items-end bg-surface-container-lowest">
        <div class="lg:col-span-3">
          <label class="block font-label-bold text-on-surface-variant mb-1">订单号 / 手机号</label>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[20px]">search</span>
            <input class="w-full pl-10 pr-4 py-2 bg-white border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all font-body-md" placeholder="搜索订单..." type="text" />
          </div>
        </div>
        <div class="lg:col-span-4">
          <label class="block font-label-bold text-on-surface-variant mb-1">日期范围</label>
          <div class="flex items-center gap-2">
            <input class="flex-1 px-3 py-2 bg-white border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none font-body-sm" type="date" />
            <span class="text-on-surface-variant">至</span>
            <input class="flex-1 px-3 py-2 bg-white border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none font-body-sm" type="date" />
          </div>
        </div>
        <div class="lg:col-span-2">
          <label class="block font-label-bold text-on-surface-variant mb-1">取货方式</label>
          <select class="w-full px-3 py-2 bg-white border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none font-body-md">
            <option>全部方式</option>
            <option>商家配送 🚚</option>
            <option>到店自提 🏠</option>
          </select>
        </div>
        <div class="lg:col-span-3 flex gap-2">
          <button class="flex-1 py-2 bg-surface-container-high hover:bg-surface-variant text-on-surface font-label-bold rounded-lg transition-colors">重置</button>
          <button class="flex-1 py-2 bg-primary-fixed-dim hover:bg-primary-fixed text-on-primary-fixed font-label-bold rounded-lg transition-colors">应用筛选</button>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-surface-container-low border-b border-outline-variant h-12">
              <th class="px-stack-md text-left w-12"><input class="rounded border-outline-variant text-primary focus:ring-primary" type="checkbox" /></th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">订单号</th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">下单时间</th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">客户</th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">商品摘要</th>
              <th class="px-stack-md text-right font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">金额</th>
              <th class="px-stack-md text-center font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">配送方式</th>
              <th class="px-stack-md text-center font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">状态</th>
              <th class="px-stack-md text-right font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-outline-variant">
            <tr v-for="order in orders" :key="order.id" class="hover:bg-slate-50 transition-colors group">
              <td class="px-stack-md py-3"><input class="rounded border-outline-variant text-primary focus:ring-primary" type="checkbox" /></td>
              <td class="px-stack-md py-3 font-label-bold text-primary cursor-pointer hover:underline" @click="$router.push(`/orders/${order.id}`)">{{ order.id }}</td>
              <td class="px-stack-md py-3 font-body-sm text-on-surface-variant">{{ order.time }}</td>
              <td class="px-stack-md py-3">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs" :class="order.avatarBg">{{ order.avatar }}</div>
                  <div>
                    <div class="font-label-bold text-on-surface">{{ order.customer }}</div>
                    <div class="text-[10px] text-on-surface-variant">{{ order.phone }}</div>
                  </div>
                </div>
              </td>
              <td class="px-stack-md py-3">
                <div class="flex flex-col">
                  <span class="font-body-md text-on-surface truncate max-w-[200px]">{{ order.products }}</span>
                  <span class="text-[11px] text-on-surface-variant">共 {{ order.count }} 件商品</span>
                </div>
              </td>
              <td class="px-stack-md py-3 text-right font-label-bold">{{ order.amount }}</td>
              <td class="px-stack-md py-3 text-center">
                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold" :class="order.deliveryClass">{{ order.delivery }}</span>
              </td>
              <td class="px-stack-md py-3 text-center">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border" :class="order.statusClass">{{ order.status }}</span>
              </td>
              <td class="px-stack-md py-3 text-right">
                <div class="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button v-if="order.status === '待接单'" class="p-1.5 text-primary hover:bg-primary-container/10 rounded-lg transition-colors" title="接单"><span class="material-symbols-outlined text-[20px]">check_circle</span></button>
                  <button v-if="order.status === '待接单'" class="p-1.5 text-error hover:bg-error-container/20 rounded-lg transition-colors" title="拒单"><span class="material-symbols-outlined text-[20px]">cancel</span></button>
                  <button v-if="order.status === '备货中'" class="px-3 py-1 bg-primary text-white text-xs font-bold rounded hover:bg-primary-container transition-colors">标记发货</button>
                  <button class="p-1.5 text-on-surface-variant hover:bg-surface-variant rounded-lg transition-colors"><span class="material-symbols-outlined text-[20px]">more_vert</span></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Footer -->
      <div class="px-stack-md py-4 bg-surface-container-lowest border-t border-outline-variant flex items-center justify-between">
        <span class="text-body-sm text-on-surface-variant">显示第 1-10 条，共 124 条订单</span>
        <div class="flex items-center gap-2">
          <button class="p-2 border border-outline-variant rounded-lg hover:bg-surface-variant disabled:opacity-30" disabled>
            <span class="material-symbols-outlined text-[18px]">chevron_left</span>
          </button>
          <button class="w-8 h-8 bg-primary text-white font-label-bold rounded-lg flex items-center justify-center">1</button>
          <button class="w-8 h-8 border border-outline-variant font-label-bold rounded-lg flex items-center justify-center hover:bg-surface-variant">2</button>
          <button class="w-8 h-8 border border-outline-variant font-label-bold rounded-lg flex items-center justify-center hover:bg-surface-variant">3</button>
          <span class="px-2">...</span>
          <button class="w-8 h-8 border border-outline-variant font-label-bold rounded-lg flex items-center justify-center hover:bg-surface-variant">12</button>
          <button class="p-2 border border-outline-variant rounded-lg hover:bg-surface-variant">
            <span class="material-symbols-outlined text-[18px]">chevron_right</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Bento Status Overview Widgets -->
    <div class="mt-stack-lg grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter">
      <div v-for="stat in stats" :key="stat.label" class="bg-white p-5 rounded-xl border border-outline-variant shadow-sm flex flex-col gap-3">
        <div class="flex justify-between items-center">
          <span class="material-symbols-outlined p-2 rounded-lg" :class="stat.iconClass">{{ stat.icon }}</span>
          <span class="text-[10px] font-bold px-2 py-0.5 rounded-full" :class="stat.badgeClass">{{ stat.badge }}</span>
        </div>
        <div>
          <p class="text-body-sm text-on-surface-variant">{{ stat.label }}</p>
          <h3 class="text-h2 font-h2 text-on-surface">{{ stat.value }} <span class="text-xs font-normal text-on-surface-variant">{{ stat.unit }}</span></h3>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('全部')

const tabs = [
  { label: '全部' },
  { label: '待接单', badge: '12' },
  { label: '备货中' },
  { label: '配送中/待自提' },
  { label: '已完成' },
  { label: '已取消' },
  { label: '退款/售后' }
]

const orders = [
  {
    id: 'ORD-2023-8842', time: '2023-10-24 14:30', customer: '李周', avatar: 'LZ', avatarBg: 'bg-secondary-container text-secondary',
    phone: '+86 138****5521', products: '精品富士苹果, 泰国榴莲...', count: 3, amount: '¥458.00',
    delivery: '🚚 商家配送', deliveryClass: 'bg-secondary-fixed text-on-secondary-fixed-variant',
    status: '待接单', statusClass: 'bg-tertiary-fixed text-on-tertiary-fixed-variant border-tertiary-container/20'
  },
  {
    id: 'ORD-2023-8841', time: '2023-10-24 14:15', customer: '小明', avatar: 'XM', avatarBg: 'bg-surface-container-high text-on-surface-variant',
    phone: '+86 155****0023', products: '红心火龙果 (箱装)', count: 1, amount: '¥89.90',
    delivery: '🏠 到店自提', deliveryClass: 'bg-surface-container-high text-on-surface-variant',
    status: '备货中', statusClass: 'bg-primary-fixed text-on-primary-fixed-variant border-primary/20'
  },
  {
    id: 'ORD-2023-8839', time: '2023-10-24 13:00', customer: '陈艳', avatar: 'CY', avatarBg: 'bg-secondary-container text-secondary',
    phone: '+86 177****1234', products: '有机蓝莓礼盒 (x4)', count: 1, amount: '¥124.00',
    delivery: '🚚 商家配送', deliveryClass: 'bg-secondary-fixed text-on-secondary-fixed-variant',
    status: '已完成', statusClass: 'bg-surface-variant text-on-surface-variant border-outline-variant'
  }
]

const stats = [
  { icon: 'assignment', iconClass: 'text-primary bg-primary-fixed', badge: '+12.5%', badgeClass: 'text-primary bg-primary/10', label: '待处理订单', value: '32', unit: '单' },
  { icon: 'hourglass_empty', iconClass: 'text-tertiary bg-tertiary-fixed', badge: '忙碌中', badgeClass: 'text-tertiary bg-tertiary/10', label: '平均备货时间', value: '18', unit: '分钟' },
  { icon: 'local_shipping', iconClass: 'text-secondary bg-secondary-fixed', badge: '4 单在途', badgeClass: 'text-secondary bg-secondary/10', label: '今日已配送', value: '114', unit: '份' },
  { icon: 'payments', iconClass: 'text-on-error-container bg-error-container', badge: '需跟进', badgeClass: 'text-error bg-error/10', label: '退款申请', value: '2', unit: '待处理' }
]
</script>
