<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">经营分析</h1>
        <p class="font-body-md text-body-md text-slate-500">深度洞察店铺经营数据，辅助决策优化。</p>
      </div>
      <div class="flex items-center gap-3">
        <div class="flex bg-surface-container-low p-1 rounded-lg">
          <button v-for="period in periods" :key="period" @click="activePeriod = period"
            class="px-4 py-1.5 rounded font-label-bold text-xs transition-colors"
            :class="activePeriod === period ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-slate-700'">
            {{ period }}
          </button>
        </div>
        <button class="px-4 py-2 border border-slate-200 rounded-lg font-label-bold text-xs text-slate-600 hover:bg-slate-50 transition-colors flex items-center gap-2">
          <span class="material-symbols-outlined text-sm">download</span>
          导出报告
        </button>
      </div>
    </div>

    <!-- Core KPI Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <div v-for="kpi in kpiCards" :key="kpi.label" class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider text-xs">{{ kpi.label }}</span>
          <span class="material-symbols-outlined p-1.5 rounded-lg text-sm" :class="kpi.iconClass">{{ kpi.icon }}</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">{{ kpi.value }}</span>
          <span class="text-xs font-bold" :class="kpi.changeColor">{{ kpi.change }}</span>
        </div>
        <p class="text-xs text-slate-400 mt-1">{{ kpi.desc }}</p>
      </div>
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-gutter mb-stack-lg">
      <!-- Revenue Trend -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
        <div class="flex items-center justify-between mb-stack-lg">
          <h3 class="font-h3 text-h3">营收趋势</h3>
          <div class="flex items-center gap-4 text-xs">
            <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-full bg-primary"></span>营收</span>
            <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-full bg-secondary"></span>成本</span>
          </div>
        </div>
        <!-- Simulated Line Chart -->
        <div class="h-56 flex items-end justify-between gap-1 px-2 pb-8 border-b border-slate-100 relative">
          <div class="absolute left-0 top-0 bottom-8 flex flex-col justify-between text-[10px] text-slate-400">
            <span>¥50K</span><span>¥30K</span><span>¥10K</span><span>¥0</span>
          </div>
          <div v-for="(bar, i) in revenueData" :key="i" class="flex-1 flex flex-col items-center gap-1">
            <div class="w-full flex gap-0.5 items-end" style="height: 180px;">
              <div class="flex-1 bg-primary/80 rounded-t" :style="{ height: bar.revenue + '%' }"></div>
              <div class="flex-1 bg-secondary/60 rounded-t" :style="{ height: bar.cost + '%' }"></div>
            </div>
            <span class="text-[10px] text-slate-400 font-bold">{{ bar.month }}</span>
          </div>
        </div>
      </div>

      <!-- Category Distribution -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
        <div class="flex items-center justify-between mb-stack-lg">
          <h3 class="font-h3 text-h3">品类销售占比</h3>
          <button class="text-xs font-label-bold text-primary flex items-center gap-1">详情 <span class="material-symbols-outlined text-sm">arrow_forward</span></button>
        </div>
        <div class="flex items-center gap-8">
          <!-- Donut Chart -->
          <div class="relative w-40 h-40 shrink-0">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
              <circle cx="18" cy="18" fill="transparent" r="14" stroke="#f1f5f9" stroke-width="4"></circle>
              <circle cx="18" cy="18" fill="transparent" r="14" stroke="#2e7d32" stroke-dasharray="35 65" stroke-dashoffset="0" stroke-width="4"></circle>
              <circle cx="18" cy="18" fill="transparent" r="14" stroke="#ffb68d" stroke-dasharray="25 75" stroke-dashoffset="-35" stroke-width="4"></circle>
              <circle cx="18" cy="18" fill="transparent" r="14" stroke="#4a6175" stroke-dasharray="20 80" stroke-dashoffset="-60" stroke-width="4"></circle>
              <circle cx="18" cy="18" fill="transparent" r="14" stroke="#ba1a1a" stroke-dasharray="12 88" stroke-dashoffset="-80" stroke-width="4"></circle>
            </svg>
            <div class="absolute inset-0 flex flex-col items-center justify-center">
              <span class="text-lg font-bold">¥128K</span>
              <span class="text-[9px] text-slate-400">总营收</span>
            </div>
          </div>
          <!-- Legend -->
          <div class="flex-1 space-y-3">
            <div v-for="cat in categoryData" :key="cat.name" class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: cat.color }"></span>
                <span class="text-xs font-label-bold">{{ cat.name }}</span>
              </div>
              <div class="flex items-center gap-3">
                <span class="text-xs font-bold text-slate-700">{{ cat.percent }}%</span>
                <span class="text-xs text-slate-400">¥{{ cat.amount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer Analysis & Product Performance -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-gutter mb-stack-lg">
      <!-- Customer Value Distribution -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
        <h3 class="font-h3 text-h3 mb-stack-md">客户价值分布</h3>
        <div class="space-y-4">
          <div v-for="seg in customerSegments" :key="seg.label">
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-label-bold text-slate-700">{{ seg.label }}</span>
              <span class="text-xs font-bold text-slate-500">{{ seg.count }} 人 ({{ seg.percent }}%)</span>
            </div>
            <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
              <div class="h-full rounded-full transition-all" :class="seg.barColor" :style="{ width: seg.percent + '%' }"></div>
            </div>
          </div>
        </div>
        <div class="mt-6 pt-4 border-t border-slate-100">
          <div class="flex justify-between text-xs">
            <span class="text-slate-500">平均客户生命周期价值</span>
            <span class="font-bold text-primary">¥2,480</span>
          </div>
        </div>
      </div>

      <!-- Top Products Performance -->
      <div class="lg:col-span-2 bg-white border border-outline-variant rounded-xl overflow-hidden">
        <div class="p-stack-md border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-h3 text-h3">商品绩效排行</h3>
          <div class="flex gap-2">
            <button class="px-3 py-1 text-xs font-label-bold bg-primary text-white rounded">按营收</button>
            <button class="px-3 py-1 text-xs font-label-bold text-slate-500 hover:bg-slate-50 rounded">按销量</button>
            <button class="px-3 py-1 text-xs font-label-bold text-slate-500 hover:bg-slate-50 rounded">按利润率</button>
          </div>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead class="bg-slate-50 border-b border-slate-200">
              <tr>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">商品</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">营收</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">利润率</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">退货率</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">趋势</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="prod in productPerformance" :key="prod.name" class="hover:bg-slate-50 transition-colors h-[48px]">
                <td class="px-6 py-3 font-label-bold text-slate-800">{{ prod.name }}</td>
                <td class="px-6 py-3 text-right font-medium text-sm">¥{{ prod.revenue }}</td>
                <td class="px-6 py-3 text-right">
                  <span class="text-sm font-bold" :class="prod.margin > 30 ? 'text-green-600' : 'text-amber-600'">{{ prod.margin }}%</span>
                </td>
                <td class="px-6 py-3 text-right text-sm text-slate-600">{{ prod.returnRate }}%</td>
                <td class="px-6">
                  <span class="text-xs flex items-center gap-1 font-bold" :class="prod.trend > 0 ? 'text-green-600' : 'text-error'">
                    <span class="material-symbols-outlined text-sm">{{ prod.trend > 0 ? 'trending_up' : 'trending_down' }}</span>{{ Math.abs(prod.trend) }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Hourly Order Heatmap -->
    <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
      <div class="flex items-center justify-between mb-stack-md">
        <h3 class="font-h3 text-h3">订单时段热力图</h3>
        <span class="text-xs text-slate-400">基于近30天数据</span>
      </div>
      <div class="grid grid-cols-12 gap-1">
        <div v-for="(cell, i) in heatmapData" :key="i" class="aspect-square rounded-sm flex items-center justify-center text-[9px] font-bold"
          :class="cell.heat > 70 ? 'bg-primary text-white' : cell.heat > 40 ? 'bg-primary/40 text-primary' : cell.heat > 15 ? 'bg-primary/15 text-primary/70' : 'bg-slate-50 text-slate-400'">
          {{ cell.hour }}
        </div>
      </div>
      <div class="flex items-center justify-between mt-3 text-[10px] text-slate-400">
        <span>0:00</span><span>6:00</span><span>12:00</span><span>18:00</span><span>23:00</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activePeriod = ref('近7天')
const periods = ['今日', '近7天', '近30天', '本月']

const kpiCards = [
  { label: '总营收', value: '¥128,400', change: '+18.5%', changeColor: 'text-green-600', desc: '较上周期增长', icon: 'payments', iconClass: 'text-primary bg-primary/10' },
  { label: '净利润', value: '¥42,800', change: '+12.3%', changeColor: 'text-green-600', desc: '利润率 33.3%', icon: 'trending_up', iconClass: 'text-green-600 bg-green-50' },
  { label: '客单价', value: '¥92.40', change: '+5.2%', changeColor: 'text-green-600', desc: '较上月', icon: 'shopping_bag', iconClass: 'text-tertiary bg-tertiary-fixed' },
  { label: '转化率', value: '4.8%', change: '-0.3%', changeColor: 'text-error', desc: '需关注优化', icon: 'ads_click', iconClass: 'text-error bg-error/10' }
]

const revenueData = [
  { month: '7月', revenue: 55, cost: 35 },
  { month: '8月', revenue: 65, cost: 40 },
  { month: '9月', revenue: 50, cost: 32 },
  { month: '10月', revenue: 78, cost: 48 },
  { month: '11月', revenue: 72, cost: 45 },
  { month: '12月', revenue: 90, cost: 55 }
]

const categoryData = [
  { name: '柑橘类', percent: 35, amount: '44.9K', color: '#2e7d32' },
  { name: '热带水果', percent: 25, amount: '32.1K', color: '#ffb68d' },
  { name: '浆果类', percent: 20, amount: '25.7K', color: '#4a6175' },
  { name: '礼盒系列', percent: 12, amount: '15.4K', color: '#ba1a1a' },
  { name: '其他', percent: 8, amount: '10.3K', color: '#94a3b8' }
]

const customerSegments = [
  { label: '高价值客户', count: 420, percent: 11, barColor: 'bg-primary' },
  { label: '中价值客户', count: 1280, percent: 33, barColor: 'bg-secondary' },
  { label: '低价值客户', count: 1850, percent: 48, barColor: 'bg-amber-400' },
  { label: '流失客户', count: 292, percent: 8, barColor: 'bg-slate-300' }
]

const productPerformance = [
  { name: '猫山王榴莲', revenue: '42,800', margin: 42, returnRate: 1.2, trend: 14 },
  { name: '阳光玫瑰葡萄', revenue: '28,500', margin: 35, returnRate: 2.1, trend: 8 },
  { name: '台南金钻凤梨', revenue: '18,400', margin: 28, returnRate: 3.5, trend: -2 },
  { name: '红颜草莓', revenue: '15,200', margin: 22, returnRate: 5.8, trend: -5 },
  { name: '有机蓝莓', revenue: '12,600', margin: 31, returnRate: 2.8, trend: 3 }
]

const heatmapData = Array.from({ length: 24 }, (_, i) => ({
  hour: i,
  heat: [2, 1, 0, 0, 0, 5, 15, 35, 55, 72, 80, 65, 45, 50, 60, 75, 85, 90, 78, 55, 40, 25, 12, 5][i]
}))
</script>
