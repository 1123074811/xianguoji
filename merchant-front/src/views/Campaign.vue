<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">营销管理</h1>
        <p class="font-body-md text-body-md text-slate-500">优惠券与促销活动管理，助力销售增长。</p>
      </div>
      <button class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all">
        <span class="material-symbols-outlined">add</span>
        创建优惠券
      </button>
    </div>

    <!-- Marketing Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <div v-for="stat in stats" :key="stat.label" class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">{{ stat.label }}</span>
          <span class="material-symbols-outlined p-2 rounded-lg" :class="stat.iconClass">{{ stat.icon }}</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">{{ stat.value }}</span>
          <span v-if="stat.change" class="text-xs font-bold" :class="stat.changeColor">{{ stat.change }}</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">{{ stat.desc }}</p>
      </div>
    </div>

    <!-- Coupon List Card -->
    <div class="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col">
      <!-- Tabs -->
      <div class="flex items-center px-6 border-b border-slate-100 overflow-x-auto">
        <button v-for="tab in couponTabs" :key="tab.label" @click="activeTab = tab.label"
          class="px-6 py-4 font-label-bold border-b-2 whitespace-nowrap transition-colors"
          :class="activeTab === tab.label ? 'border-primary text-primary' : 'border-transparent text-slate-400 hover:text-slate-600'">
          {{ tab.label }} ({{ tab.count }})
        </button>
      </div>

      <!-- Coupon Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">优惠券名称</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">类型</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">优惠额度</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">使用门槛</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">已领取/总量</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">有效期</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">状态</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="coupon in coupons" :key="coupon.name" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3 font-label-bold text-slate-800">{{ coupon.name }}</td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold" :class="coupon.typeClass">{{ coupon.type }}</span>
              </td>
              <td class="px-6 py-3 text-sm font-medium text-error">{{ coupon.discount }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ coupon.threshold }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ coupon.claimed }}/{{ coupon.total }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ coupon.validity }}</td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="coupon.statusClass">{{ coupon.status }}</span>
              </td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 text-slate-400">
                  <button class="p-1 hover:text-primary transition-colors" title="编辑"><span class="material-symbols-outlined text-[20px]">edit</span></button>
                  <button class="p-1 hover:text-error transition-colors" title="停用"><span class="material-symbols-outlined text-[20px]">pause_circle</span></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">显示第 1-5 条，共 18 条优惠券</span>
        <div class="flex items-center gap-1">
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_left</span></button>
          <button class="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary font-bold text-sm shadow-sm">1</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">2</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_right</span></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('生效中')

const stats = [
  { label: '活跃优惠券', value: '8', change: '+2 本周', changeColor: 'text-green-600', desc: '正在发放中', icon: 'confirmation_number', iconClass: 'text-primary bg-primary/10' },
  { label: '累计领取量', value: '2,840', change: '+18.5%', changeColor: 'text-green-600', desc: '较上月增长', icon: 'redeem', iconClass: 'text-tertiary bg-tertiary-fixed' },
  { label: '核销率', value: '67.3%', change: '+3.2%', changeColor: 'text-green-600', desc: '高于行业均值', icon: 'verified', iconClass: 'text-secondary bg-secondary-fixed' },
  { label: '带来营收', value: '¥48.2K', change: '+22%', changeColor: 'text-green-600', desc: '优惠券贡献营收', icon: 'payments', iconClass: 'text-primary bg-primary/10' }
]

const couponTabs = [
  { label: '生效中', count: 8 },
  { label: '已过期', count: 12 },
  { label: '待生效', count: 3 }
]

const coupons = [
  { name: '新人首单立减', type: '满减券', typeClass: 'bg-primary-fixed text-on-primary-fixed-variant', discount: '¥20', threshold: '满 ¥99 可用', claimed: '1,240', total: '2,000', validity: '2024.01.01 - 2024.03.31', status: '生效中', statusClass: 'bg-primary-fixed text-on-primary-fixed-variant border border-primary/20' },
  { name: '水果满减优惠', type: '满减券', typeClass: 'bg-primary-fixed text-on-primary-fixed-variant', discount: '¥15', threshold: '满 ¥69 可用', claimed: '856', total: '1,500', validity: '2024.01.15 - 2024.06.30', status: '生效中', statusClass: 'bg-primary-fixed text-on-primary-fixed-variant border border-primary/20' },
  { name: '周末折扣', type: '折扣券', typeClass: 'bg-tertiary-fixed text-on-tertiary-fixed-variant', discount: '8.5折', threshold: '无门槛', claimed: '420', total: '800', validity: '2024.02.01 - 2024.04.30', status: '生效中', statusClass: 'bg-primary-fixed text-on-primary-fixed-variant border border-primary/20' },
  { name: '春节礼盒专享', type: '满减券', typeClass: 'bg-primary-fixed text-on-primary-fixed-variant', discount: '¥50', threshold: '满 ¥199 可用', claimed: '324', total: '500', validity: '2024.02.01 - 2024.02.15', status: '已过期', statusClass: 'bg-surface-variant text-on-surface-variant border border-outline-variant' },
  { name: '老客回馈券', type: '折扣券', typeClass: 'bg-tertiary-fixed text-on-tertiary-fixed-variant', discount: '9折', threshold: '满 ¥50 可用', claimed: '0', total: '1,000', validity: '2024.04.01 - 2024.06.30', status: '待生效', statusClass: 'bg-secondary-fixed text-on-secondary-fixed-variant border border-secondary/20' }
]
</script>
