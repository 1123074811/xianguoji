<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">客户管理</h1>
        <p class="font-body-md text-body-md text-slate-500">管理您的客户关系，洞察消费行为。</p>
      </div>
    </div>

    <!-- Customer Table -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div class="p-stack-md border-b border-slate-100 flex items-center justify-between">
        <div class="relative w-72">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
          <input
            v-model="keyword"
            @keydown.enter="applyFilter"
            class="w-full pl-10 pr-4 py-2 bg-slate-50 border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none text-sm"
            placeholder="搜索客户姓名、手机号..."
            type="text"
          />
        </div>
        <button @click="applyFilter" class="px-4 py-1.5 bg-primary text-white text-sm font-label-bold rounded-lg">查询</button>
      </div>

      <div v-if="loading" class="text-center text-sm text-slate-400 py-12">加载中...</div>
      <div v-else-if="!customers.length" class="text-center text-sm text-slate-400 py-12">暂无客户</div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">客户</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">标签</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">累计消费</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">订单数</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">最近下单</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">注册时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="customer in customers" :key="customer.id" class="hover:bg-slate-50 h-[48px]">
              <td class="px-6 py-3">
                <div class="flex items-center gap-3">
                  <img v-if="customer.avatar" :src="customer.avatar" class="w-8 h-8 rounded-full object-cover" alt="" />
                  <div v-else class="w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs bg-primary/10 text-primary">
                    {{ initials(customer.nickname) }}
                  </div>
                  <div>
                    <div class="font-label-bold text-slate-800">{{ customer.nickname || '匿名用户' }}</div>
                    <div class="text-[10px] text-slate-400">{{ customer.phone }}</div>
                  </div>
                </div>
              </td>
              <td class="px-6 py-3">
                <span v-if="customer.tag" class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold bg-amber-100 text-amber-700">
                  {{ tagLabel(customer.tag) }}
                </span>
                <span v-else class="text-xs text-slate-400">—</span>
              </td>
              <td class="px-6 py-3 text-right font-medium text-sm">¥{{ customer.totalSpend }}</td>
              <td class="px-6 py-3 text-right text-sm text-slate-600">{{ customer.orderCount }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ customer.lastOrderTime || '—' }}</td>
              <td class="px-6 py-3 text-sm text-slate-500">{{ customer.registerTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 名</span>
        <div class="flex items-center gap-1">
          <button :disabled="page <= 1" @click="page--; load()" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white disabled:opacity-30">
            <span class="material-symbols-outlined text-sm">chevron_left</span>
          </button>
          <button :disabled="page >= totalPages" @click="page++; load()" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white disabled:opacity-30">
            <span class="material-symbols-outlined text-sm">chevron_right</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { customerApi } from '@/api/modules/customer';
import type { CustomerVO } from '@/api/types/customer';

const customers = ref<CustomerVO[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const keyword = ref('');

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));

function initials(name: string) {
  if (!name) return '?';
  return name.slice(0, 2).toUpperCase();
}

function tagLabel(tag: string) {
  return { new: '新客', vip: 'VIP', loyal: '老客', sleep: '沉睡' }[tag] || tag;
}

async function load() {
  loading.value = true;
  try {
    const data = await customerApi.page({ page: page.value, size: size.value, keyword: keyword.value || undefined });
    customers.value = data.list;
    total.value = data.total;
  } catch (e) {
    console.warn('加载客户列表失败', e);
  } finally {
    loading.value = false;
  }
}

function applyFilter() {
  page.value = 1;
  load();
}

onMounted(load);
</script>
