<template>
  <div>
    <!-- Welcome Header -->
    <header class="mb-stack-lg">
      <h2 class="font-h1 text-h1 text-on-surface">早上好，张老板 ☀️</h2>
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
          <span class="font-h2 text-h2">142</span>
          <span class="text-green-600 text-xs font-bold">+12%</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">较昨日 (126)</p>
      </div>

      <!-- Today's Turnover -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">今日成交额</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">payments</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">¥12,840</span>
          <span class="text-green-600 text-xs font-bold">+8.4%</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">平均客单价: ¥90.42</p>
      </div>

      <!-- Pending Orders -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow relative overflow-hidden">
        <div class="absolute top-0 right-0 w-24 h-24 bg-error/5 -mr-10 -mt-10 rounded-full"></div>
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">待处理订单</span>
          <span class="material-symbols-outlined text-error bg-error/10 p-2 rounded-lg">pending_actions</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="font-h2 text-h2">28</span>
          <span class="px-2 py-0.5 bg-error text-white text-[10px] font-black rounded-full uppercase">加急</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">需在2小时内处理完毕</p>
      </div>

      <!-- New Customers -->
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">新增客户</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">person_add</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">15</span>
          <span class="text-slate-400 text-xs font-bold">今日</span>
        </div>
        <div class="flex -space-x-2 mt-2">
          <div class="w-6 h-6 rounded-full border-2 border-white bg-primary/20 flex items-center justify-center text-[8px] text-primary font-bold">A</div>
          <div class="w-6 h-6 rounded-full border-2 border-white bg-blue-200 flex items-center justify-center text-[8px] text-blue-700 font-bold">B</div>
          <div class="w-6 h-6 rounded-full border-2 border-white bg-slate-200 text-[10px] flex items-center justify-center text-slate-600 font-bold">+12</div>
        </div>
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
        <!-- Simulated Bar Chart -->
        <div class="h-64 flex items-end justify-between gap-2 px-2 pb-6 border-b border-slate-100">
          <div v-for="(day, i) in chartData" :key="i" class="flex-1 rounded-t relative group" :class="day.color" :style="{ height: day.height }">
            <div class="absolute -top-8 left-1/2 -translate-x-1/2 bg-slate-800 text-white text-[10px] px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity">{{ day.value }}</div>
          </div>
        </div>
        <div class="flex justify-between mt-2 px-2">
          <span v-for="d in ['周一','周二','周三','周四','周五','周六','周日']" :key="d" class="text-[10px] text-slate-400 font-bold">{{ d }}</span>
        </div>
      </div>

      <!-- To-Do List Card -->
      <div class="bg-white border border-outline-variant rounded-xl p-stack-md flex flex-col">
        <div class="flex items-center justify-between mb-stack-md">
          <h3 class="font-h3 text-h3">待办事项</h3>
          <span class="px-2 py-0.5 bg-slate-100 text-slate-600 text-[10px] font-bold rounded">4 个任务</span>
        </div>
        <div class="space-y-3 flex-1 overflow-y-auto">
          <div v-for="task in todoItems" :key="task.title" class="group flex items-start gap-3 p-3 bg-slate-50 hover:bg-green-50 rounded-lg border border-transparent hover:border-green-100 transition-all cursor-pointer">
            <div class="w-8 h-8 rounded-full flex items-center justify-center shrink-0" :class="task.iconBg">
              <span class="material-symbols-outlined text-sm" :class="task.iconColor">{{ task.icon }}</span>
            </div>
            <div>
              <p class="font-label-bold text-slate-800">{{ task.title }}</p>
              <p class="text-[10px] text-slate-500">{{ task.desc }}</p>
            </div>
            <span class="material-symbols-outlined text-slate-300 group-hover:text-primary ml-auto">chevron_right</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Bottom Grid: Best Sellers & Distribution -->
    <section class="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
      <!-- Best Selling Products Table -->
      <div class="lg:col-span-2 bg-white border border-outline-variant rounded-xl overflow-hidden">
        <div class="p-stack-md border-b border-slate-100 flex items-center justify-between">
          <h3 class="font-h3 text-h3">热销商品排行榜 Top 10</h3>
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
              <tr v-for="(item, i) in topProducts" :key="i" class="h-[48px] hover:bg-slate-50 transition-colors">
                <td class="px-6 text-sm font-black text-slate-400">#0{{ i + 1 }}</td>
                <td class="px-6 flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-slate-100 overflow-hidden shrink-0 flex items-center justify-center">
                    <span class="material-symbols-outlined text-slate-400 text-sm">nutrition</span>
                  </div>
                  <span class="font-label-bold text-slate-800">{{ item.name }}</span>
                </td>
                <td class="px-6 text-sm text-right font-medium text-slate-700">{{ item.sales }}</td>
                <td class="px-6 text-sm text-right font-medium text-slate-700">{{ item.revenue }}</td>
                <td class="px-6 text-right">
                  <span class="text-xs flex items-center justify-end gap-1 font-bold" :class="item.trend > 0 ? 'text-green-600' : 'text-error'">
                    <span class="material-symbols-outlined text-sm">{{ item.trend > 0 ? 'trending_up' : 'trending_down' }}</span> {{ Math.abs(item.trend) }}%
                  </span>
                </td>
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
        <!-- Simulated Pie Chart -->
        <div class="relative w-48 h-48 mx-auto mb-stack-lg">
          <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
            <circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#f1f5f9" stroke-width="3"></circle>
            <circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#2e7d32" stroke-dasharray="60 40" stroke-dashoffset="0" stroke-width="3"></circle>
            <circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#ffb68d" stroke-dasharray="20 80" stroke-dashoffset="-60" stroke-width="3"></circle>
            <circle cx="18" cy="18" fill="transparent" r="15.915" stroke="#ba1a1a" stroke-dasharray="20 80" stroke-dashoffset="-80" stroke-width="3"></circle>
          </svg>
          <div class="absolute inset-0 flex flex-col items-center justify-center">
            <span class="text-2xl font-bold">142</span>
            <span class="text-[10px] text-slate-400 font-bold uppercase">总量</span>
          </div>
        </div>
        <!-- Legend -->
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-primary-container"></span>
              <span class="text-xs font-label-bold">已完成</span>
            </div>
            <span class="text-xs font-bold text-slate-600">60%</span>
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-tertiary-fixed-dim"></span>
              <span class="text-xs font-label-bold">处理中</span>
            </div>
            <span class="text-xs font-bold text-slate-600">20%</span>
          </div>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-error"></span>
              <span class="text-xs font-label-bold">待处理</span>
            </div>
            <span class="text-xs font-bold text-slate-600">20%</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const chartData = [
  { value: 98, height: '40%', color: 'bg-primary/20' },
  { value: 112, height: '55%', color: 'bg-primary/30' },
  { value: 105, height: '45%', color: 'bg-primary/40' },
  { value: 138, height: '70%', color: 'bg-primary/50' },
  { value: 126, height: '65%', color: 'bg-primary/60' },
  { value: 152, height: '85%', color: 'bg-primary/80' },
  { value: 142, height: '75%', color: 'bg-primary' }
]

const todoItems = [
  { icon: 'schedule', iconBg: 'bg-error/10 text-error', title: '28 个待处理订单', desc: '等待时长已超过1小时' },
  { icon: 'inventory', iconBg: 'bg-orange-100 text-orange-600', title: '库存预警', desc: '泰国金枕榴莲库存不足 (仅剩3件)' },
  { icon: 'star', iconBg: 'bg-blue-100 text-blue-600', title: '新评价提醒', desc: '有5条未读客户评价' },
  { icon: 'undo', iconBg: 'bg-purple-100 text-purple-600', title: '退款申请', desc: '2个退款请求待审核' }
]

const topProducts = [
  { name: '猫山王榴莲', sales: '428 kg', revenue: '¥42,800', trend: 14 },
  { name: '阳光玫瑰葡萄', sales: '356 kg', revenue: '¥18,500', trend: 8 },
  { name: '台南金钻凤梨', sales: '210 kg', revenue: '¥7,400', trend: -2 }
]
</script>
