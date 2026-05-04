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
          <button v-for="period in periods" :key="period.label" @click="switchPeriod(period)"
            class="px-4 py-1.5 rounded font-label-bold text-xs transition-colors"
            :class="activePeriod === period.label ? 'bg-white shadow-sm text-primary' : 'text-slate-500 hover:text-slate-700'">
            {{ period.label }}
          </button>
        </div>
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
          </div>
        </div>
        <div class="h-56 flex items-end justify-between gap-1 px-2 pb-8 border-b border-slate-100 relative">
          <div v-if="revenueData.length === 0" class="w-full text-center self-center text-sm text-slate-400">暂无数据</div>
          <div v-for="(bar, i) in revenueData" :key="i" class="flex-1 flex flex-col items-center gap-1">
            <div class="w-full" style="height: 180px; display: flex; align-items: flex-end;">
              <div class="w-full bg-primary/80 rounded-t" :style="{ height: barHeight(bar.revenue) + '%' }"></div>
            </div>
            <span class="text-[10px] text-slate-400 font-bold">{{ bar.month }}</span>
          </div>
        </div>
      </div>

      <!-- Category Distribution -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
        <div class="flex items-center justify-between mb-stack-lg">
          <h3 class="font-h3 text-h3">品类销售占比</h3>
        </div>
        <div class="flex items-center gap-8">
          <div class="relative w-40 h-40 shrink-0">
            <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
              <circle cx="18" cy="18" fill="transparent" r="14" stroke="#f1f5f9" stroke-width="4"></circle>
              <circle v-for="(seg, i) in donutSegments" :key="i" cx="18" cy="18" fill="transparent" r="14"
                :stroke="seg.color" :stroke-dasharray="`${seg.percent} ${100 - seg.percent}`" :stroke-dashoffset="seg.offset" stroke-width="4"></circle>
            </svg>
            <div class="absolute inset-0 flex flex-col items-center justify-center">
              <span class="text-lg font-bold">¥{{ shortMoney(totalCategoryRevenue) }}</span>
              <span class="text-[9px] text-slate-400">总营收</span>
            </div>
          </div>
          <div class="flex-1 space-y-3">
            <div v-if="categoryData.length === 0" class="text-sm text-slate-400">暂无数据</div>
            <div v-for="(cat, i) in categoryData" :key="cat.name" class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: paletteColor(i) }"></span>
                <span class="text-xs font-label-bold">{{ cat.name }}</span>
              </div>
              <div class="flex items-center gap-3">
                <span class="text-xs font-bold text-slate-700">{{ cat.percent }}%</span>
                <span class="text-xs text-slate-400">¥{{ shortMoney(cat.amount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer Analysis & Product Performance -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-gutter mb-stack-lg">
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
        <h3 class="font-h3 text-h3 mb-stack-md">客户价值分布</h3>
        <div class="space-y-4">
          <div v-if="customerSegments.length === 0" class="text-sm text-slate-400">暂无数据</div>
          <div v-for="(seg, i) in customerSegments" :key="seg.label">
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-label-bold text-slate-700">{{ seg.label }}</span>
              <span class="text-xs font-bold text-slate-500">{{ seg.count }} 人 ({{ seg.percent }}%)</span>
            </div>
            <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
              <div class="h-full rounded-full transition-all" :class="segBarColor(i)" :style="{ width: seg.percent + '%' }"></div>
            </div>
          </div>
        </div>
        <div class="mt-6 pt-4 border-t border-slate-100">
          <div class="flex justify-between text-xs">
            <span class="text-slate-500">平均客户生命周期价值</span>
            <span class="font-bold text-primary">¥{{ avgLtv }}</span>
          </div>
        </div>
      </div>

      <div class="lg:col-span-2 bg-white border border-outline-variant rounded-xl overflow-hidden">
        <div class="p-stack-md border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-h3 text-h3">商品绩效排行</h3>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead class="bg-slate-50 border-b border-slate-200">
              <tr>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">商品</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">营收</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">销量</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">利润率</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">退货率</th>
                <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">趋势</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="productPerformance.length === 0">
                <td colspan="6" class="px-6 py-8 text-center text-sm text-slate-400">暂无数据</td>
              </tr>
              <tr v-for="prod in productPerformance" :key="prod.id" class="hover:bg-slate-50 transition-colors h-[48px]">
                <td class="px-6 py-3 font-label-bold text-slate-800">{{ prod.name }}</td>
                <td class="px-6 py-3 text-right font-medium text-sm">¥{{ formatNumber(prod.revenue) }}</td>
                <td class="px-6 py-3 text-right text-sm text-slate-600">{{ prod.sales }}</td>
                <td class="px-6 py-3 text-right">
                  <span class="text-sm font-bold" :class="prod.margin > 30 ? 'text-green-600' : 'text-amber-600'">{{ prod.margin }}%</span>
                </td>
                <td class="px-6 py-3 text-right text-sm text-slate-600">{{ prod.returnRate }}%</td>
                <td class="px-6">
                  <span class="text-xs flex items-center gap-1 font-bold" :class="prod.trend >= 0 ? 'text-green-600' : 'text-error'">
                    <span class="material-symbols-outlined text-sm">{{ prod.trend >= 0 ? 'trending_up' : 'trending_down' }}</span>{{ Math.abs(prod.trend) }}%
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
        <div v-for="(cell, i) in heatmapCells" :key="i" class="aspect-square rounded-sm flex items-center justify-center text-[9px] font-bold"
          :class="heatClass(cell.heat)">
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
import { ref, computed, onMounted } from 'vue'
import { statApi } from '@/api/modules/stat'

const periods = [
  { label: '今日', days: 1 },
  { label: '近7天', days: 7 },
  { label: '近30天', days: 30 },
  { label: '本月', days: new Date().getDate() },
]
const activePeriod = ref('近7天')
const activeDays = ref(7)

const kpi = ref<any>({})
const revenueData = ref<{ month: string; revenue: number }[]>([])
const categoryData = ref<{ name: string; percent: number; amount: number }[]>([])
const customerSegments = ref<{ label: string; count: number; percent: number }[]>([])
const avgLtv = ref('0.00')
const productPerformance = ref<any[]>([])
const heatmapData = ref<{ hour: number; count: number }[]>([])

const palette = ['#2e7d32', '#ffb68d', '#4a6175', '#ba1a1a', '#94a3b8', '#0ea5e9', '#a855f7']
function paletteColor(i: number) { return palette[i % palette.length] }

const totalCategoryRevenue = computed(() => categoryData.value.reduce((s, c) => s + Number(c.amount || 0), 0))

const donutSegments = computed(() => {
  let offset = 0
  return categoryData.value.map((c, i) => {
    const seg = { color: paletteColor(i), percent: Number(c.percent) || 0, offset: -offset }
    offset += seg.percent
    return seg
  })
})

const kpiCards = computed(() => [
  { label: '总营收', value: '¥' + formatNumber(kpi.value.totalRevenue || 0), desc: `近${activeDays.value}天`, icon: 'payments', iconClass: 'text-primary bg-primary/10' },
  { label: '订单数', value: String(kpi.value.orderCount || 0), desc: '已支付订单', icon: 'shopping_bag', iconClass: 'text-tertiary bg-tertiary-fixed' },
  { label: '客单价', value: '¥' + formatNumber(kpi.value.avgPrice || 0), desc: '平均每单', icon: 'trending_up', iconClass: 'text-green-600 bg-green-50' },
  { label: '转化率', value: (kpi.value.conversionRate || '0') + '%', desc: '支付/总下单', icon: 'ads_click', iconClass: 'text-secondary bg-secondary/10' },
])

const heatmapCells = computed(() => {
  const max = Math.max(1, ...heatmapData.value.map(c => c.count || 0))
  return heatmapData.value.map(c => ({ hour: c.hour, heat: max > 0 ? (c.count / max) * 100 : 0 }))
})

function barHeight(rev: number) {
  const max = Math.max(1, ...revenueData.value.map(d => Number(d.revenue) || 0))
  return max > 0 ? (Number(rev) / max) * 100 : 0
}

function shortMoney(v: number | string) {
  const n = Number(v) || 0
  if (n >= 10000) return (n / 10000).toFixed(1) + 'W'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K'
  return n.toFixed(0)
}

function formatNumber(v: any) {
  const n = Number(v) || 0
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

function heatClass(h: number) {
  if (h > 70) return 'bg-primary text-white'
  if (h > 40) return 'bg-primary/40 text-primary'
  if (h > 15) return 'bg-primary/15 text-primary/70'
  return 'bg-slate-50 text-slate-400'
}

function segBarColor(i: number) {
  return ['bg-primary', 'bg-secondary', 'bg-amber-400', 'bg-slate-300'][i] || 'bg-slate-300'
}

async function loadAll() {
  try {
    const [k, rev, cat, seg, perf, heat] = await Promise.all([
      statApi.analysisKpi(activeDays.value),
      statApi.revenueTrend(6),
      statApi.categoryDistribution(),
      statApi.customerSegments(),
      statApi.productPerformance(10),
      statApi.hourlyHeatmap(),
    ])
    kpi.value = k || {}
    revenueData.value = (rev || []).map((m: any) => ({ month: (m.month || '').slice(5), revenue: Number(m.revenue) || 0 }))
    categoryData.value = cat || []
    customerSegments.value = (seg && seg.segments) || []
    avgLtv.value = formatNumber(seg && seg.avgLtv)
    productPerformance.value = perf || []
    heatmapData.value = heat || []
  } catch (e) {
    console.warn('加载经营分析失败', e)
  }
}

function switchPeriod(p: { label: string; days: number }) {
  activePeriod.value = p.label
  activeDays.value = p.days
  statApi.analysisKpi(p.days).then(k => { kpi.value = k || {} }).catch(() => {})
}

onMounted(loadAll)
</script>
