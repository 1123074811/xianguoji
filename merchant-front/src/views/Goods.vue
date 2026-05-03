<template>
  <div>
    <!-- Page Header -->
    <div class="mb-6 flex flex-col md:flex-row md:items-end justify-between gap-4">
      <div>
        <h1 class="font-h1 text-on-surface mb-1">商品管理</h1>
        <p class="text-slate-500 font-body-md">管理您的时令水果、库存水平和区域定价策略。</p>
      </div>
      <button class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all" @click="$router.push('/goods/edit')">
        <span class="material-symbols-outlined">add</span>
        新增商品
      </button>
    </div>

    <!-- Product Table Card -->
    <div class="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col">
      <!-- Tabs -->
      <div class="flex items-center px-6 border-b border-slate-100 overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.label" @click="activeTab = tab.label"
          class="px-6 py-4 font-label-bold border-b-2 whitespace-nowrap transition-colors"
          :class="activeTab === tab.label ? 'border-primary text-primary' : 'border-transparent text-slate-400 hover:text-slate-600'">
          {{ tab.label }}
          <span v-if="tab.badge" class="ml-1 bg-error text-on-error text-[10px] px-1.5 py-0.5 rounded-full font-bold">{{ tab.badge }}</span>
        </button>
      </div>

      <!-- Filter Bar -->
      <div class="p-4 bg-surface-container-lowest border-b border-slate-100 flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <div class="relative">
            <select class="appearance-none bg-white border border-outline-variant rounded-lg pl-3 pr-10 py-1.5 text-body-md font-label-bold focus:ring-1 focus:ring-primary outline-none cursor-pointer">
              <option>所有品类</option>
              <option>柑橘类</option>
              <option>热带水果</option>
              <option>浆果类</option>
            </select>
            <span class="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">expand_more</span>
          </div>
          <div class="relative">
            <select class="appearance-none bg-white border border-outline-variant rounded-lg pl-3 pr-10 py-1.5 text-body-md font-label-bold focus:ring-1 focus:ring-primary outline-none cursor-pointer">
              <option>批量操作</option>
              <option>批量上架</option>
              <option>批量下架</option>
              <option>批量删除</option>
            </select>
            <span class="material-symbols-outlined absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">layers</span>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <span class="text-body-sm text-slate-500">共 144 件商品</span>
          <div class="flex border border-outline-variant rounded-lg overflow-hidden">
            <button class="p-1.5 bg-surface-container-low text-primary"><span class="material-symbols-outlined">list</span></button>
            <button class="p-1.5 hover:bg-slate-50 text-slate-400 transition-colors"><span class="material-symbols-outlined">grid_view</span></button>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-surface-container-low border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 w-12"><input class="rounded border-slate-300 text-primary focus:ring-primary" type="checkbox" /></th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">商品预览</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">商品信息</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">分类</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">规格/单位</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">价格范围</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">库存</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">累计销量</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">状态</th>
              <th class="px-6 py-3 font-table-header text-on-surface-variant uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="product in products" :key="product.id" class="hover:bg-slate-50/80 transition-colors group" :class="product.warning ? 'bg-error-container/10 hover:bg-error-container/20' : ''">
              <td class="px-6 py-3"><input class="rounded border-slate-300 text-primary focus:ring-primary" type="checkbox" /></td>
              <td class="px-3 py-3">
                <div class="w-12 h-12 rounded bg-slate-100 overflow-hidden border flex items-center justify-center" :class="product.warning ? 'border-error-container' : 'border-slate-200'">
                  <span class="material-symbols-outlined text-slate-400">nutrition</span>
                </div>
              </td>
              <td class="px-3 py-3">
                <div class="font-label-bold text-on-surface flex items-center gap-2">
                  {{ product.name }}
                  <span v-if="product.warning" class="material-symbols-outlined text-error text-[16px]" style="font-variation-settings: 'FILL' 1;">warning</span>
                </div>
                <div class="text-xs text-slate-400 font-body-sm">{{ product.id }}</div>
                <div v-if="product.warning" class="text-xs text-error font-body-sm font-bold">库存预警: 低于 50</div>
              </td>
              <td class="px-3 py-3">
                <span class="bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-[11px] font-bold">{{ product.category }}</span>
              </td>
              <td class="px-3 py-3 text-body-md text-slate-600">{{ product.spec }}</td>
              <td class="px-3 py-3 text-body-md font-label-bold text-on-surface">{{ product.price }}</td>
              <td class="px-3 py-3 text-body-md" :class="product.warning ? 'text-error font-bold italic' : 'text-slate-600'">{{ product.stock }} 件</td>
              <td class="px-3 py-3 text-body-md text-slate-600">{{ product.sales }}</td>
              <td class="px-3 py-3">
                <div class="w-10 h-5 rounded-full relative cursor-pointer shadow-inner" :class="product.active ? 'bg-primary-container' : 'bg-outline-variant'">
                  <div class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-all" :class="product.active ? 'right-0.5' : 'left-0.5'"></div>
                </div>
              </td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 text-slate-400">
                  <button class="p-1 hover:text-primary transition-colors" title="编辑" @click="$router.push('/goods/edit/' + product.id)"><span class="material-symbols-outlined text-[20px]">edit</span></button>
                  <button class="p-1 hover:text-primary transition-colors" title="复制"><span class="material-symbols-outlined text-[20px]">content_copy</span></button>
                  <button class="p-1 hover:text-error transition-colors" title="删除"><span class="material-symbols-outlined text-[20px]">delete</span></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">显示第 1 到 4 条，共 144 条数据</span>
        <div class="flex items-center gap-1">
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_left</span></button>
          <button class="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary font-bold text-sm shadow-sm">1</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">2</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">3</button>
          <span class="px-2 text-slate-400">...</span>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">36</button>
          <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_right</span></button>
        </div>
      </div>
    </div>

    <!-- Bottom Stats -->
    <div class="mt-8 grid grid-cols-1 md:grid-cols-4 gap-6">
      <div v-for="stat in bottomStats" :key="stat.label" class="bg-white p-5 border border-slate-200 rounded-xl shadow-sm">
        <div class="flex justify-between items-start mb-3">
          <div class="p-2 rounded-lg" :class="stat.iconBg">
            <span class="material-symbols-outlined" :class="stat.iconColor">{{ stat.icon }}</span>
          </div>
          <span class="text-xs font-bold" :class="stat.badgeColor">{{ stat.badge }}</span>
        </div>
        <h4 class="text-slate-500 text-body-sm mb-1">{{ stat.label }}</h4>
        <p class="text-h2 font-black text-on-surface">{{ stat.value }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('在售中 (128)')

const tabs = [
  { label: '在售中 (128)' },
  { label: '已下架 (12)' },
  { label: '库存预警', badge: '4' },
  { label: '回收站' }
]

const products = [
  { id: 'FD-2024001', name: '新奇士脐橙 (L号)', category: '柑橘类', spec: '5kg/箱 (约12个)', price: '¥89.00 - ¥168.00', stock: '1,240', sales: '3,452', active: true, warning: false },
  { id: 'FD-2024035', name: '菲律宾金菠萝', category: '热带水果', spec: '2个装 (约3kg)', price: '¥45.00', stock: '12', sales: '892', active: true, warning: true },
  { id: 'FD-2024082', name: '红颜草莓 (特级)', category: '浆果类', spec: '500g/盒', price: '¥32.50 - ¥38.00', stock: '450', sales: '1,120', active: true, warning: false },
  { id: 'FD-2024105', name: '墨西哥牛油果', category: '热带水果', spec: '6个装 (1.2kg)', price: '¥68.00', stock: '312', sales: '745', active: false, warning: false }
]

const bottomStats = [
  { icon: 'inventory', iconBg: 'bg-green-50 text-green-700', badge: '较上月 +12%', badgeColor: 'text-green-600', label: '总商品数 (SKU)', value: '1,482' },
  { icon: 'warning', iconBg: 'bg-orange-50 text-orange-700', badge: '需要立即处理', badgeColor: 'text-orange-600', label: '库存预警', value: '4' },
  { icon: 'local_fire_department', iconBg: 'bg-blue-50 text-blue-700', badge: '表现优异', badgeColor: 'text-blue-600', label: '热销商品', value: '24' },
  { icon: 'category', iconBg: 'bg-purple-50 text-purple-700', badge: '涵盖 8 个区域', badgeColor: 'text-purple-600', label: '全部品类', value: '12' }
]
</script>
