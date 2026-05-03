<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">客户管理</h1>
        <p class="font-body-md text-body-md text-slate-500">管理您的客户关系，洞察消费行为。</p>
      </div>
      <div class="flex gap-2">
        <button class="px-4 py-2 border border-slate-200 rounded-lg font-label-bold text-xs text-slate-600 hover:bg-slate-50 transition-colors flex items-center gap-2">
          <span class="material-symbols-outlined text-sm">download</span>
          导出
        </button>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">总客户数</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">group</span>
        </div>
        <span class="font-h2 text-h2">3,842</span>
        <p class="text-xs text-green-600 font-bold mt-2">+156 本月新增</p>
      </div>
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">活跃客户</span>
          <span class="material-symbols-outlined text-secondary bg-secondary-fixed p-2 rounded-lg">person</span>
        </div>
        <span class="font-h2 text-h2">1,205</span>
        <p class="text-xs text-slate-400 mt-2">近30天有下单</p>
      </div>
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">平均客单价</span>
          <span class="material-symbols-outlined text-tertiary bg-tertiary-fixed p-2 rounded-lg">payments</span>
        </div>
        <span class="font-h2 text-h2">¥92.40</span>
        <p class="text-xs text-green-600 font-bold mt-2">+5.2% 较上月</p>
      </div>
      <div class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">复购率</span>
          <span class="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg">repeat</span>
        </div>
        <span class="font-h2 text-h2">38.6%</span>
        <p class="text-xs text-slate-400 mt-2">高于行业均值</p>
      </div>
    </div>

    <!-- Customer Table -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div class="p-stack-md border-b border-slate-100 flex items-center justify-between">
        <div class="relative w-72">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
          <input class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none text-sm" placeholder="搜索客户姓名、手机号..." type="text" />
        </div>
        <div class="flex gap-2">
          <select class="px-3 py-1.5 bg-white border border-outline-variant rounded-lg text-sm outline-none">
            <option>全部等级</option>
            <option>金牌会员</option>
            <option>银牌会员</option>
            <option>普通会员</option>
          </select>
        </div>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">客户</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">等级</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">累计消费</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">订单数</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">最近下单</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="customer in customers" :key="customer.name" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs" :class="customer.avatarBg">{{ customer.avatar }}</div>
                  <div>
                    <div class="font-label-bold text-slate-800">{{ customer.name }}</div>
                    <div class="text-[10px] text-slate-400">{{ customer.phone }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold" :class="customer.levelClass">{{ customer.level }}</span>
              </td>
              <td class="px-6 py-3 text-right font-medium text-sm">{{ customer.totalSpent }}</td>
              <td class="px-6 py-3 text-right text-sm text-slate-600">{{ customer.orders }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ customer.lastOrder }}</td>
              <td class="px-6 py-3 text-right">
                <button class="text-primary hover:underline text-xs font-bold">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">显示第 1-5 条，共 3,842 条数据</span>
        <div class="flex items-center gap-1">
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_left</span></button>
          <button class="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary font-bold text-sm shadow-sm">1</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">2</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">3</button>
          <span class="px-2 text-slate-400">...</span>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_right</span></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const customers = [
  { name: '王小红', avatar: 'WX', avatarBg: 'bg-pink-100 text-pink-700', phone: '+86 138****5521', level: '金牌会员', levelClass: 'bg-amber-100 text-amber-700', totalSpent: '¥12,480', orders: 42, lastOrder: '2024-01-15' },
  { name: '李明', avatar: 'LM', avatarBg: 'bg-blue-100 text-blue-700', phone: '+86 155****0023', level: '银牌会员', levelClass: 'bg-slate-200 text-slate-700', totalSpent: '¥4,260', orders: 18, lastOrder: '2024-01-14' },
  { name: '陈艳', avatar: 'CY', avatarBg: 'bg-purple-100 text-purple-700', phone: '+86 177****1234', level: '金牌会员', levelClass: 'bg-amber-100 text-amber-700', totalSpent: '¥8,920', orders: 35, lastOrder: '2024-01-13' },
  { name: '张三', avatar: 'ZS', avatarBg: 'bg-green-100 text-green-700', phone: '+86 139****8876', level: '普通会员', levelClass: 'bg-slate-100 text-slate-600', totalSpent: '¥680', orders: 3, lastOrder: '2024-01-10' },
  { name: '刘芳', avatar: 'LF', avatarBg: 'bg-orange-100 text-orange-700', phone: '+86 186****3345', level: '银牌会员', levelClass: 'bg-slate-200 text-slate-700', totalSpent: '¥3,150', orders: 12, lastOrder: '2024-01-08' }
]
</script>
