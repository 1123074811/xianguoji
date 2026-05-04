<template>
  <div>
    <!-- Welcome Header -->
    <header class="mb-stack-lg">
      <h2 class="font-h1 text-h1 text-on-surface">{{ greeting }}，{{ adminStore.staffInfo?.nickname || '管理员' }} ☀️</h2>
      <p class="font-body-md text-body-md text-slate-500 mt-1">这是您今天的水果生意经营概况。</p>
    </header>

    <!-- Metric Bento Grid -->
    <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <!-- Today's Orders -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">今日订单量</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">receipt_long</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">{{ dashboard.todayOrders ?? '--' }}</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">实时统计</p>
      </div>

      <!-- Today's Turnover -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">今日成交额</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">payments</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">¥{{ dashboard.todayRevenue ?? '0.00' }}</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">仅含已支付订单</p>
      </div>

      <!-- Pending Orders -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow relative overflow-hidden">
        <div class="absolute top-0 right-0 w-24 h-24 bg-error/5 -mr-10 -mt-10 rounded-full"></div>
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">待处理订单</span>
          <span class="material-symbols-outlined text-error bg-error/10 p-2 rounded-lg">pending_actions</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="font-h2 text-h2">{{ dashboard.pendingOrders ?? '--' }}</span>
          <span v-if="(dashboard.pendingOrders ?? 0) > 0" class="px-2 py-0.5 bg-error text-white text-[10px] font-black rounded-full uppercase">加急</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">待商家接单</p>
      </div>

      <!-- New Customers -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">新增客户</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">person_add</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">{{ dashboard.todayNewUsers ?? '--' }}</span>
          <span class="text-slate-400 text-xs font-bold">今日</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">今日注册用户数</p>
      </div>
    </section>

    <!-- Charts & To-Do Section -->
    <section class="grid grid-cols-1 lg:grid-cols-3 gap-gutter mb-stack-lg">
      <!-- 7-Day Trend Chart Area -->
      <div class="lg:col-span-2 bg-white border border-outline-variant rounded-xl p-stack-md">
        <div class="flex items-center justify-between mb-stack-lg">
          <h3 class="font-h3 text-h3">7日订单趋势</h3>
          <div class="flex gap-2">
            <button class="px-3 py-1 text-xs font-label-bold border border-slate-200 rounded-lg hover:bg-slate-50">过去7天</button>
            <button class="px-3 py-1 text-xs font-label-bold text-slate-400 hover:text-slate-600">过去30天</button>
          </div>
        </div>
        <!-- Simulated Bar Chart (按真实 7 天订单数渲染) -->
        <div class="h-64 flex items-end justify-between gap-2 px-2 pb-6 border-b border-slate-100">
          <div
            v-for="(day, i) in trendChart"
            :key="i"
            class="flex-1 rounded-t relative group bg-primary/60"
            :style="{ height: day.height }"
          >
            <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-slate-800 text-white text-[10px] px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity">{{ day.count }}</div>
          </div>
        </div>
        <div class="flex justify-between mt-2 px-2">
          <span v-for="(d, i) in trendChart" :key="i" class="text-[10px] text-slate-400 font-bold">{{ d.label }}</span>
        </div>
      </div>

      <!-- To-Do List Card -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md flex flex-col">
        <div class="flex items-center justify-between mb-stack-md">
          <h3 class="font-h3 text-h3">待办事项</h3>
          <span class="px-2 py-0.5 bg-slate-100 text-slate-600 text-[10px] font-bold rounded">{{ todoItems.length }} 个任务</span>
        </div>
        <div class="space-y-3 flex-1 overflow-y-auto">
          <router-link v-for="task in todoItems" :key="task.title" :to="task.link" class="group flex items-start gap-3 p-3 bg-slate-50 hover:bg-green-50 rounded-lg border border-transparent hover:border-green-100 transition-all cursor-pointer">
            <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0" :class="task.iconBg">
              <span class="material-symbols-outlined text-sm" :class="task.iconColor">{{ task.icon }}</span>
            </div>
            <div>
              <p class="font-label-bold text-slate-800">{{ task.title }}</p>
              <p class="text-[10px] text-slate-500">{{ task.desc }}</p>
            </div>
            <span class="material-symbols-outlined text-slate-300 group-hover:text-primary ml-auto">chevron_right</span>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Bottom Grid: Best Sellers & Distribution -->
    <section class="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
      <!-- Best Selling Products Table -->
      <div class="lg:col-span-2 bg-white border border-outline-variant rounded-xl overflow-hidden">
        <div class="p-stack-md border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-h3 text-h3">热销商品排行榜 Top {{ topProducts.length }}</h3>
          <button class="text-xs font-label-bold text-primary flex items-center gap-1">
            查看全部 <span class="material-symbols-outlined text-sm">arrow_forward</span>
          </button>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead class="bg-slate-50">
              <tr class="h-10">
                <th class="px-6 font-table-header text-table-header text-slate-500">排名</th>
                <th class="px-6 font-table-header text-table-header text-slate-500">商品名称</th>
                <th class="px-6 font-table-header text-table-header text-slate-500 text-right">销量</th>
                <th class="px-6 font-table-header text-table-header text-slate-500 text-right">成交额</th>
                <th class="px-6 font-table-header text-table-header text-slate-500 text-right">趋势</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="!topProducts.length">
                <td colspan="5" class="text-center text-sm text-slate-400 py-6">暂无销售数据</td>
              </tr>
              <tr v-for="(item, i) in topProducts" :key="item.id" class="h-[48px] hover:bg-slate-50 transition-colors">
                <td class="px-6 text-sm font-black text-slate-400">#{{ String(i + 1).padStart(2, '0') }}</td>
                <td class="px-6 flex items-center gap-3">
                  <img v-if="item.mainImage" :src="item.mainImage" class="w-8 h-8 rounded-lg object-cover shrink-0" alt="" />
                  <div v-else class="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center shrink-0">
                    <span class="material-symbols-outlined text-slate-400 text-sm">nutrition</span>
                  </div>
                  <span class="font-label-bold text-slate-800">{{ item.name }}</span>
                </td>
                <td class="px-6 text-sm text-right font-medium text-slate-700">{{ item.sales }}</td>
                <td class="px-6 text-sm text-right font-medium text-slate-700">¥{{ item.minPrice }}</td>
                <td class="px-6 text-right text-xs text-slate-400">库存 {{ item.totalStock }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Order Status Pie Chart -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md">
        <div class="mb-stack-lg">
          <h3 class="font-h3 text-h3">订单分布</h3>
          <p class="text-xs text-slate-500">实时订单状态追踪</p>
        </div>
        <!-- 真实订单状态分布 -->
        <div class="space-y-2">
          <div v-if="orderStatusList.length === 0" class="text-center text-sm text-slate-400 py-8">今日暂无订单</div>
          <div v-for="row in orderStatusList" :key="row.status" class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: row.color }"></span>
              <span class="text-xs font-label-bold">{{ row.label }}</span>
            </div>
            <span class="text-xs font-bold text-slate-600">{{ row.count }} 单</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useAdminStore } from '@/stores/admin';
import { statApi } from '@/api/modules/stat';
import type { DashboardVO, OrderTrendItem, TopProductVO, TodoVO } from '@/api/types/stat';

const adminStore = useAdminStore();

const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '凌晨好';
  if (h < 12) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});

const dashboard = ref<Partial<DashboardVO>>({});
const trend = ref<OrderTrendItem[]>([]);
const topProducts = ref<TopProductVO[]>([]);
const todo = ref<Partial<TodoVO>>({});
const statusMap = ref<Record<number, number>>({});

const ORDER_STATUS_LABELS: Record<number, { label: string; color: string }> = {
  0: { label: '已取消', color: '#94a3b8' },
  1: { label: '待接单', color: '#ba1a1a' },
  2: { label: '已接单', color: '#ffb68d' },
  3: { label: '配送中', color: '#fbbf24' },
  4: { label: '已送达', color: '#60a5fa' },
  5: { label: '已完成', color: '#2e7d32' },
  6: { label: '退款中', color: '#a78bfa' },
  7: { label: '已退款', color: '#7c3aed' },
  8: { label: '已拒单', color: '#64748b' },
};

const orderStatusList = computed(() =>
  Object.entries(statusMap.value).map(([s, c]) => ({
    status: Number(s),
    count: c,
    label: ORDER_STATUS_LABELS[Number(s)]?.label || `状态${s}`,
    color: ORDER_STATUS_LABELS[Number(s)]?.color || '#cbd5e1',
  })),
);

const trendChart = computed(() => {
  const max = Math.max(1, ...trend.value.map((t) => t.count));
  return trend.value.map((t) => ({
    label: t.date.slice(5),
    count: t.count,
    height: `${Math.max(4, Math.round((t.count / max) * 100))}%`,
  }));
});

const todoItems = computed(() => {
  const items: { icon: string; iconBg: string; iconColor: string; title: string; desc: string; link: string }[] = [];
  if ((todo.value.pendingAccept ?? 0) > 0) {
    items.push({
      icon: 'schedule', iconBg: 'bg-error/10', iconColor: 'text-error',
      title: `${todo.value.pendingAccept} 个订单待接单`, desc: '尽快处理，避免超时', link: '/orders',
    });
  }
  if ((todo.value.stockWarn ?? 0) > 0) {
    items.push({
      icon: 'inventory', iconBg: 'bg-orange-100', iconColor: 'text-orange-600',
      title: `${todo.value.stockWarn} 个商品库存预警`, desc: '低于阈值，请及时补货', link: '/goods',
    });
  }
  if ((todo.value.pendingRefund ?? 0) > 0) {
    items.push({
      icon: 'undo', iconBg: 'bg-purple-100', iconColor: 'text-purple-600',
      title: `${todo.value.pendingRefund} 个退款待审核`, desc: '请尽快处理客户退款', link: '/orders',
    });
  }
  if ((todo.value.pendingReview ?? 0) > 0) {
    items.push({
      icon: 'star', iconBg: 'bg-blue-100', iconColor: 'text-blue-600',
      title: `${todo.value.pendingReview} 条评价待回复`, desc: '及时回应可提升口碑', link: '/reviews',
    });
  }
  if (items.length === 0) {
    items.push({
      icon: 'check_circle', iconBg: 'bg-green-100', iconColor: 'text-green-600',
      title: '今日没有待办事项', desc: '一切正常 ✓', link: '/dashboard',
    });
  }
  return items;
});

async function loadDashboard() {
  try {
    const [d, t, sMap, tp, td] = await Promise.all([
      statApi.dashboard(),
      statApi.orderTrend(7),
      statApi.orderStatus(),
      statApi.topProducts(10),
      statApi.todo(),
    ]);
    dashboard.value = d;
    trend.value = t;
    statusMap.value = sMap;
    topProducts.value = tp;
    todo.value = td;
  } catch (e) {
    console.warn('加载工作台数据失败', e);
  }
}

onMounted(loadDashboard);
</script>
