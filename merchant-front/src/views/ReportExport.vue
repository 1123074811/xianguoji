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
      <div class="mt-stack-lg flex gap-3 items-center">
        <button @click="generateReport" :disabled="generating" class="px-8 py-2.5 bg-primary text-white rounded-lg font-label-bold shadow-md hover:bg-primary/90 active:scale-95 transition-all flex items-center gap-2 disabled:opacity-50">
          <span class="material-symbols-outlined text-lg">download</span>
          {{ generating ? '生成中…' : '生成并下载' }}
        </button>
        <span v-if="generateMsg" class="text-xs" :class="generateMsgClass">{{ generateMsg }}</span>
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
            <tr v-if="recentReports.length === 0">
              <td colspan="7" class="px-6 py-10 text-center text-sm text-slate-400">暂无导出记录</td>
            </tr>
            <tr v-for="report in recentReports" :key="report.id" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3 font-label-bold text-slate-800 flex items-center gap-2">
                <span class="material-symbols-outlined text-sm" :class="iconClass(report.format)">{{ iconName(report.format) }}</span>
                {{ report.fileName }}
              </td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold" :class="typeBadgeClass(report.reportType)">{{ typeLabel(report.reportType) }}</span>
              </td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ report.startDate }} - {{ report.endDate }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ report.format.toUpperCase() }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ formatTime(report.createdAt) }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ formatSize(report.fileSize) }}</td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2">
                  <button @click="downloadHistory(report)" class="p-1.5 text-primary hover:bg-primary/10 rounded-lg transition-colors" title="下载"><span class="material-symbols-outlined text-[18px]">download</span></button>
                  <button @click="removeRecord(report.id)" class="p-1.5 text-slate-400 hover:text-error hover:bg-error/10 rounded-lg transition-colors" title="删除"><span class="material-symbols-outlined text-[18px]">delete</span></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">最近 {{ recentReports.length }} 条记录</span>
        <button @click="loadHistory" class="text-xs text-primary hover:underline">刷新</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { exportApi, type ReportExportRecordVO } from '@/api/modules/export'

function todayStr(offset = 0) {
  const d = new Date()
  d.setDate(d.getDate() + offset)
  return d.toISOString().slice(0, 10)
}

const reportType = ref('sales')
const startDate = ref(todayStr(-30))
const endDate = ref(todayStr())
const format = ref('xlsx')
const includeCharts = ref(true)
const detailedMode = ref(false)

const generating = ref(false)
const generateMsg = ref('')
const generateMsgClass = ref('text-slate-500')
const recentReports = ref<ReportExportRecordVO[]>([])

const templates = [
  { name: '日销售汇总', desc: '每日销售数据一览', icon: 'today', iconBg: 'bg-primary/10', iconColor: 'text-primary', type: 'sales', format: 'Excel' },
  { name: '周库存快照', desc: '每周库存变动追踪', icon: 'inventory', iconBg: 'bg-amber-50', iconColor: 'text-amber-600', type: 'inventory', format: 'Excel' },
  { name: '月度财务报表', desc: '收入/支出/利润汇总', icon: 'account_balance', iconBg: 'bg-green-50', iconColor: 'text-green-700', type: 'finance', format: 'Excel' },
  { name: '客户增长报告', desc: '新增/活跃/流失分析', icon: 'group_add', iconBg: 'bg-blue-50', iconColor: 'text-blue-600', type: 'customer', format: 'Excel' },
  { name: '配送效率报告', desc: '配送时效与覆盖率', icon: 'local_shipping', iconBg: 'bg-purple-50', iconColor: 'text-purple-600', type: 'delivery', format: 'Excel' },
  { name: '全维度经营报表', desc: '综合经营数据总览', icon: 'dashboard', iconBg: 'bg-primary/10', iconColor: 'text-primary', type: 'sales', format: 'Excel' }
]

const typeLabelMap: Record<string, string> = {
  sales: '销售',
  inventory: '库存',
  customer: '客户',
  delivery: '配送',
  finance: '财务',
}

const typeBadgeMap: Record<string, string> = {
  sales: 'bg-primary-fixed text-on-primary-fixed-variant',
  inventory: 'bg-amber-100 text-amber-700',
  customer: 'bg-blue-100 text-blue-700',
  delivery: 'bg-purple-100 text-purple-700',
  finance: 'bg-green-100 text-green-700',
}

function typeLabel(t: string) { return typeLabelMap[t] || t }
function typeBadgeClass(t: string) { return typeBadgeMap[t] || 'bg-slate-100 text-slate-600' }
function iconName(fmt: string) { return fmt === 'pdf' ? 'picture_as_pdf' : 'description' }
function iconClass(fmt: string) { return fmt === 'pdf' ? 'text-error' : 'text-green-600' }

function formatSize(n: number) {
  if (!n) return '0 B'
  if (n >= 1048576) return (n / 1048576).toFixed(1) + ' MB'
  if (n >= 1024) return (n / 1024).toFixed(1) + ' KB'
  return n + ' B'
}

function formatTime(s?: string) {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 16)
}

function triggerDownload(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

function suggestedFileName() {
  const map: Record<string, string> = { sales: '销售报表', inventory: '库存报表', customer: '客户报表', delivery: '配送报表', finance: '财务报表' }
  const ext = format.value === 'csv' ? 'csv' : 'xlsx'
  return `${map[reportType.value] || '报表'}_${startDate.value.replace(/-/g, '')}_${endDate.value.replace(/-/g, '')}.${ext}`
}

async function generateReport() {
  if (!startDate.value || !endDate.value) {
    generateMsg.value = '请选择起止日期'
    generateMsgClass.value = 'text-error'
    return
  }
  generating.value = true
  generateMsg.value = ''
  try {
    const blob = await exportApi.generate({
      reportType: reportType.value,
      startDate: startDate.value,
      endDate: endDate.value,
      format: format.value === 'pdf' ? 'xlsx' : format.value,
      includeCharts: includeCharts.value,
      detailedMode: detailedMode.value,
    })
    triggerDownload(blob, suggestedFileName())
    generateMsg.value = format.value === 'pdf' ? '已使用 XLSX 格式（PDF 暂未支持）' : '导出成功'
    generateMsgClass.value = 'text-primary'
    await loadHistory()
  } catch (e: any) {
    generateMsg.value = e?.message || '导出失败'
    generateMsgClass.value = 'text-error'
  } finally {
    generating.value = false
  }
}

async function downloadHistory(rec: ReportExportRecordVO) {
  try {
    const blob = await exportApi.downloadHistory(rec.id)
    triggerDownload(blob, rec.fileName)
  } catch (e) {
    console.warn('下载失败', e)
  }
}

async function removeRecord(id: number) {
  if (!confirm('确认删除此记录？')) return
  try {
    await exportApi.remove(id)
    await loadHistory()
  } catch (e) {
    console.warn('删除失败', e)
  }
}

async function loadHistory() {
  try {
    recentReports.value = await exportApi.history(20)
  } catch (e) {
    console.warn('加载历史记录失败', e)
  }
}

onMounted(loadHistory)
</script>
