<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">报表导出</h1>
        <p class="font-body-md text-body-md text-slate-500">生成和下载经营数据报表，支持自定义时间范围。</p>
      </div>
    </div>

    <!-- Report Generator Card -->
    <div class="bg-white border border-outline-variant rounded-xl p-stack-lg shadow-sm mb-stack-lg">
      <h2 class="font-h3 text-h3 mb-stack-md flex items-center gap-2">
        <span class="material-symbols-outlined text-primary">description</span>
        生成新报表
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-stack-md">
        <div>
          <label class="block font-label-bold text-label-bold text-on-surface-variant mb-stack-xs">报表类型</label>
          <select class="w-full px-4 py-2.5 bg-slate-50 border border-outline-variant rounded-lg focus:ring-primary focus:border-primary outline-none text-sm" v-model="reportType">
            <option value="sales">销售报表</option>
            <option value="inventory">库存报表</option>
            <option value="customer">客户报表</option>
            <option value="delivery">配送报表</option>
            <option value="finance">财务报表</option>
          </select>
        </div>
        <div>
          <label class="block font-label-bold text-label-bold text-on-surface-variant mb-stack-xs">开始日期</label>
          <input class="w-full px-4 py-2.5 bg-slate-50 border border-outline-variant rounded-lg focus:ring-primary focus:border-primary outline-none text-sm" type="date" v-model="startDate" />
        </div>
        <div>
          <label class="block font-label-bold text-label-bold text-on-surface-variant mb-stack-xs">结束日期</label>
          <input class="w-full px-4 py-2.5 bg-slate-50 border border-outline-variant rounded-lg focus:ring-primary focus:border-primary outline-none text-sm" type="date" v-model="endDate" />
        </div>
        <div>
          <label class="block font-label-bold text-label-bold text-on-surface-variant mb-stack-xs">导出格式</label>
          <select class="w-full px-4 py-2.5 bg-slate-50 border border-outline-variant rounded-lg focus:ring-primary focus:border-primary outline-none text-sm" v-model="format">
            <option value="xlsx">Excel (.xlsx)</option>
            <option value="csv">CSV (.csv)</option>
            <option value="pdf">PDF (.pdf)</option>
          </select>
        </div>
      </div>
      <div class="mt-stack-md flex items-center gap-3">
        <label class="flex items-center gap-2 cursor-pointer">
          <input class="rounded border-outline-variant text-primary focus:ring-primary" type="checkbox" v-model="includeCharts" />
          <span class="text-sm text-slate-600">包含图表</span>
        </label>
        <label class="flex items-center gap-2 cursor-pointer">
          <input class="rounded border-outline-variant text-primary focus:ring-primary" type="checkbox" v-model="detailedMode" />
          <span class="text-sm text-slate-600">详细模式（含子分类）</span>
        </label>
      </div>
      <div class="mt-stack-lg flex gap-3">
        <button class="px-8 py-2.5 bg-primary text-white rounded-lg font-label-bold shadow-md hover:bg-primary/90 active:scale-95 transition-all flex items-center gap-2">
          <span class="material-symbols-outlined text-lg">download</span>
          生成并下载
        </button>
        <button class="px-6 py-2.5 border border-slate-200 rounded-lg font-label-bold text-slate-600 hover:bg-slate-50 transition-colors flex items-center gap-2">
          <span class="material-symbols-outlined text-lg">schedule</span>
          定时生成
        </button>
      </div>
    </div>

    <!-- Quick Report Templates -->
    <div class="mb-stack-lg">
      <h3 class="font-h3 text-h3 mb-stack-md">快捷报表模板</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-gutter">
        <div v-for="tpl in templates" :key="tpl.name" class="bg-white border border-outline-variant rounded-xl p-stack-md hover:shadow-md transition-shadow cursor-pointer group" @click="reportType = tpl.type">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="tpl.iconBg">
              <span class="material-symbols-outlined" :class="tpl.iconColor">{{ tpl.icon }}</span>
            </div>
            <div>
              <h4 class="font-label-bold text-slate-800 group-hover:text-primary transition-colors">{{ tpl.name }}</h4>
              <p class="text-[10px] text-slate-400">{{ tpl.desc }}</p>
            </div>
          </div>
          <div class="flex items-center justify-between text-xs text-slate-400">
            <span>{{ tpl.format }}</span>
            <span class="material-symbols-outlined text-sm group-hover:text-primary transition-colors">arrow_forward</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Reports Table -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <h3 class="font-h3 text-h3">历史报表记录</h3>
        <span class="text-xs text-slate-400">保留最近 90 天</span>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">报表名称</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">类型</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">时间范围</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">格式</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">生成时间</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">大小</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="report in recentReports" :key="report.name" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3 font-label-bold text-slate-800 flex items-center gap-2">
                <span class="material-symbols-outlined text-sm" :class="report.fileIcon">{{ report.fileIconName }}</span>
                {{ report.name }}
              </td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold" :class="report.typeClass">{{ report.type }}</span>
              </td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ report.range }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ report.format }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ report.generatedAt }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ report.size }}</td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button class="p-1.5 text-primary hover:bg-primary/10 rounded-lg transition-colors" title="下载"><span class="material-symbols-outlined text-[18px]">download</span></button>
                  <button class="p-1.5 text-slate-400 hover:text-error hover:bg-error/10 rounded-lg transition-colors" title="删除"><span class="material-symbols-outlined text-[18px]">delete</span></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">显示第 1-5 条，共 24 条记录</span>
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

const reportType = ref('sales')
const startDate = ref('2024-01-01')
const endDate = ref('2024-01-31')
const format = ref('xlsx')
const includeCharts = ref(true)
const detailedMode = ref(false)

const templates = [
  { name: '日销售汇总', desc: '每日销售数据一览', icon: 'today', iconBg: 'bg-primary/10', iconColor: 'text-primary', type: 'sales', format: 'Excel' },
  { name: '周库存快照', desc: '每周库存变动追踪', icon: 'inventory', iconBg: 'bg-amber-50', iconColor: 'text-amber-600', type: 'inventory', format: 'Excel' },
  { name: '月度财务报表', desc: '收入/支出/利润汇总', icon: 'account_balance', iconBg: 'bg-green-50', iconColor: 'text-green-700', type: 'finance', format: 'PDF' },
  { name: '客户增长报告', desc: '新增/活跃/流失分析', icon: 'group_add', iconBg: 'bg-blue-50', iconColor: 'text-blue-600', type: 'customer', format: 'Excel' },
  { name: '配送效率报告', desc: '配送时效与覆盖率', icon: 'local_shipping', iconBg: 'bg-purple-50', iconColor: 'text-purple-600', type: 'delivery', format: 'PDF' },
  { name: '全维度经营报表', desc: '综合经营数据总览', icon: 'dashboard', iconBg: 'bg-primary/10', iconColor: 'text-primary', type: 'sales', format: 'PDF' }
]

const recentReports = [
  { name: '2024年1月销售报表', type: '销售', typeClass: 'bg-primary-fixed text-on-primary-fixed-variant', range: '2024.01.01 - 2024.01.31', format: 'XLSX', generatedAt: '2024-02-01 09:00', size: '2.4 MB', fileIcon: 'text-green-600', fileIconName: 'description' },
  { name: '2024年1月库存快照', type: '库存', typeClass: 'bg-amber-100 text-amber-700', range: '2024.01.01 - 2024.01.31', format: 'XLSX', generatedAt: '2024-02-01 09:15', size: '1.8 MB', fileIcon: 'text-green-600', fileIconName: 'description' },
  { name: 'Q4财务报表', type: '财务', typeClass: 'bg-green-100 text-green-700', range: '2023.10.01 - 2023.12.31', format: 'PDF', generatedAt: '2024-01-05 10:30', size: '5.2 MB', fileIcon: 'text-error', fileIconName: 'picture_as_pdf' },
  { name: '2023年度客户报告', type: '客户', typeClass: 'bg-blue-100 text-blue-700', range: '2023.01.01 - 2023.12.31', format: 'XLSX', generatedAt: '2024-01-02 14:00', size: '3.1 MB', fileIcon: 'text-green-600', fileIconName: 'description' },
  { name: '12月配送效率报告', type: '配送', typeClass: 'bg-purple-100 text-purple-700', range: '2023.12.01 - 2023.12.31', format: 'PDF', generatedAt: '2024-01-03 11:00', size: '4.0 MB', fileIcon: 'text-error', fileIconName: 'picture_as_pdf' }
]
</script>
