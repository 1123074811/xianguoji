<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h2 class="font-h2 text-h2 text-on-surface mb-1">订单管理</h2>
        <p class="font-body-sm text-body-sm text-on-surface-variant">高效管理和处理您的生鲜水果订单。</p>
      </div>
      <button @click="loadOrders" class="bg-primary hover:bg-primary-container text-white px-stack-md py-2 rounded-lg font-label-bold flex items-center gap-2 transition-transform active:scale-95 shadow-sm">
        <span class="material-symbols-outlined text-[18px]">refresh</span>
        刷新
      </button>
    </div>

    <!-- Tabs Section -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden mb-stack-md">
      <div class="flex border-b border-outline-variant overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.value" @click="changeTab(tab.value)"
          class="px-6 py-4 font-label-bold whitespace-nowrap transition-colors"
          :class="activeStatus === tab.value ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant hover:text-primary border-b-2 border-transparent'">
          {{ tab.label }}
        </button>
      </div>

      <!-- Filter Bar -->
      <div class="p-stack-md grid grid-cols-1 md:grid-cols-4 lg:grid-cols-12 gap-stack-md items-end bg-surface-container-lowest">
        <div class="lg:col-span-3">
          <label class="block font-label-bold text-on-surface-variant mb-1">订单号 / 手机号</label>
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[20px]">search</span>
            <input v-model="filters.keyword" @keydown.enter="applyFilter" class="w-full pl-10 pr-4 py-2 bg-white border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary outline-none transition-all font-body-md" placeholder="搜索订单..." type="text" />
          </div>
        </div>
        <div class="lg:col-span-4">
          <label class="block font-label-bold text-on-surface-variant mb-1">日期范围</label>
          <div class="flex items-center gap-2">
            <input v-model="filters.startDate" class="flex-1 px-3 py-2 bg-white border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none font-body-sm" type="date" />
            <span class="text-on-surface-variant">至</span>
            <input v-model="filters.endDate" class="flex-1 px-3 py-2 bg-white border border-outline-variant rounded-lg focus:ring-1 focus:ring-primary outline-none font-body-sm" type="date" />
          </div>
        </div>
        <div class="lg:col-span-3 col-start-1 lg:col-start-auto flex gap-2">
          <button @click="resetFilter" class="flex-1 py-2 bg-surface-container-high hover:bg-surface-variant text-on-surface font-label-bold rounded-lg transition-colors">重置</button>
          <button @click="applyFilter" class="flex-1 py-2 bg-primary text-white font-label-bold rounded-lg transition-colors">应用筛选</button>
        </div>
      </div>
    </div>

    <!-- Data Table -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
      <div v-if="loading" class="text-center text-sm text-slate-400 py-12">加载中...</div>
      <div v-else-if="!orders.length" class="text-center text-sm text-slate-400 py-12">暂无订单</div>
      <div v-else class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-surface-container-low border-b border-outline-variant h-12">
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">订单号</th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">下单时间</th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">客户</th>
              <th class="px-stack-md text-left font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">商品摘要</th>
              <th class="px-stack-md text-right font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">金额</th>
              <th class="px-stack-md text-center font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">配送方式</th>
              <th class="px-stack-md text-center font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">状态</th>
              <th class="px-stack-md text-right font-table-header text-table-header text-on-surface-variant uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-outline-variant">
            <tr v-for="order in orders" :key="order.orderNo" class="hover:bg-slate-50 transition-colors group">
              <td class="px-stack-md py-3 font-label-bold text-primary cursor-pointer hover:underline" @click="$router.push(`/orders/${order.orderNo}`)">{{ order.orderNo }}</td>
              <td class="px-stack-md py-3 font-body-sm text-on-surface-variant">{{ order.createdAt }}</td>
              <td class="px-stack-md py-3">
                <div class="font-label-bold text-on-surface">{{ order.consignee }}</div>
                <div class="text-[10px] text-on-surface-variant">{{ order.consigneePhone }}</div>
              </td>
              <td class="px-stack-md py-3">
                <div class="flex flex-col">
                  <span class="font-body-md text-on-surface truncate max-w-[200px]">{{ summarizeItems(order.items) }}</span>
                  <span class="text-[11px] text-on-surface-variant">共 {{ order.items.length }} 项 / {{ totalQuantity(order.items) }} 件</span>
                </div>
              </td>
              <td class="px-stack-md py-3 text-right font-label-bold">¥{{ order.payAmount }}</td>
              <td class="px-stack-md py-3 text-center">
                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold bg-secondary-fixed text-on-secondary-fixed-variant">
                  {{ order.deliveryType === 1 ? '🚚 配送' : '🏠 自提' }}
                </span>
              </td>
              <td class="px-stack-md py-3 text-center">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border" :class="statusBadge(order.status).cls">{{ statusBadge(order.status).label }}</span>
              </td>
              <td class="px-stack-md py-3 text-right">
                <div class="flex items-center justify-end gap-2">
                  <Popconfirm v-if="order.status === 1" title="确认接单" :message="`确认接单 ${order.orderNo}？接单后需尽快备货发货。`" type="info" confirm-text="接单" @confirm="acceptOrder(order)">
                    <Tooltip text="接单（确认有库存可发货）">
                      <button class="p-1.5 text-primary hover:bg-primary-container/10 rounded-lg">
                        <span class="material-symbols-outlined text-[20px]">check_circle</span>
                      </button>
                    </Tooltip>
                  </Popconfirm>
                  <PopInput v-if="order.status === 1" title="拒单原因" :message="`拒单 ${order.orderNo} 后金额将原路退回，请填写拒单原因。`" placeholder="请输入拒单原因" type="danger" confirm-text="确认拒单" placement="bottom" @confirm="rejectOrder(order, $event)">
                    <Tooltip text="拒单（订单将关闭，金额原路退回）">
                      <button class="p-1.5 text-error hover:bg-error-container/20 rounded-lg">
                        <span class="material-symbols-outlined text-[20px]">cancel</span>
                      </button>
                    </Tooltip>
                  </PopInput>
                  <button v-if="order.status === 2" @click="shipOrder(order)" class="px-3 py-1 bg-primary text-white text-xs font-bold rounded hover:bg-primary-container">标记发货</button>
                  <Tooltip text="查看订单详情">
                    <button @click="$router.push(`/orders/${order.orderNo}`)" class="p-1.5 text-on-surface-variant hover:bg-surface-variant rounded-lg">
                      <span class="material-symbols-outlined text-[20px]">visibility</span>
                    </button>
                  </Tooltip>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Footer -->
      <div class="px-stack-md py-4 bg-surface-container-lowest border-t border-outline-variant flex items-center justify-between">
        <span class="text-body-sm text-on-surface-variant">显示第 {{ pageStart }}-{{ pageEnd }} 条，共 {{ total }} 条订单</span>
        <div class="flex items-center gap-2">
          <button :disabled="page <= 1" @click="page--; loadOrders()" class="p-2 border border-outline-variant rounded-lg hover:bg-surface-variant disabled:opacity-30">
            <span class="material-symbols-outlined text-[18px]">chevron_left</span>
          </button>
          <span class="px-3 text-sm">第 {{ page }} / {{ totalPages }} 页</span>
          <button :disabled="page >= totalPages" @click="page++; loadOrders()" class="p-2 border border-outline-variant rounded-lg hover:bg-surface-variant disabled:opacity-30">
            <span class="material-symbols-outlined text-[18px]">chevron_right</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { adminOrderApi } from '@/api/modules/order';
import type { AdminOrderVO, AdminOrderItemVO } from '@/api/types/order';
import Tooltip from '@/components/Tooltip.vue';
import Popconfirm from '@/components/Popconfirm.vue';
import PopInput from '@/components/PopInput.vue';

const tabs: { label: string; value?: number }[] = [
  { label: '全部' },
  { label: '待接单', value: 1 },
  { label: '备货中', value: 2 },
  { label: '配送中', value: 3 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 0 },
];

const STATUS_MAP: Record<number, { label: string; cls: string }> = {
  0: { label: '已取消', cls: 'bg-surface-variant text-on-surface-variant border-outline-variant' },
  1: { label: '待接单', cls: 'bg-tertiary-fixed text-on-tertiary-fixed-variant border-tertiary-container/20' },
  2: { label: '备货中', cls: 'bg-primary-fixed text-on-primary-fixed-variant border-primary/20' },
  3: { label: '配送中', cls: 'bg-secondary-fixed text-on-secondary-fixed-variant border-secondary/20' },
  4: { label: '已送达', cls: 'bg-blue-50 text-blue-700 border-blue-200' },
  5: { label: '已完成', cls: 'bg-surface-variant text-on-surface-variant border-outline-variant' },
  6: { label: '退款中', cls: 'bg-purple-50 text-purple-700 border-purple-200' },
  7: { label: '已退款', cls: 'bg-purple-100 text-purple-700 border-purple-300' },
  8: { label: '已拒单', cls: 'bg-slate-100 text-slate-600 border-slate-200' },
};

const activeStatus = ref<number | undefined>(undefined);
const filters = ref({ keyword: '', startDate: '', endDate: '' });
const orders = ref<AdminOrderVO[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(20);

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));
const pageStart = computed(() => (orders.value.length ? (page.value - 1) * size.value + 1 : 0));
const pageEnd = computed(() => (page.value - 1) * size.value + orders.value.length);

function statusBadge(s: number) {
  return STATUS_MAP[s] || { label: `状态${s}`, cls: 'bg-slate-100 text-slate-600 border-slate-200' };
}
function summarizeItems(items: AdminOrderItemVO[]) {
  if (!items?.length) return '-';
  const head = items.slice(0, 2).map((i) => i.productName).join(', ');
  return items.length > 2 ? `${head}...` : head;
}
function totalQuantity(items: AdminOrderItemVO[]) {
  return items?.reduce((s, i) => s + i.quantity, 0) || 0;
}

async function loadOrders() {
  loading.value = true;
  try {
    const data = await adminOrderApi.page({
      page: page.value,
      size: size.value,
      status: activeStatus.value,
      keyword: filters.value.keyword || undefined,
      startDate: filters.value.startDate || undefined,
      endDate: filters.value.endDate || undefined,
    });
    orders.value = data.list;
    total.value = data.total;
  } catch (e) {
    console.warn('加载订单失败', e);
  } finally {
    loading.value = false;
  }
}

function changeTab(value?: number) {
  activeStatus.value = value;
  page.value = 1;
  loadOrders();
}
function applyFilter() {
  page.value = 1;
  loadOrders();
}
function resetFilter() {
  filters.value = { keyword: '', startDate: '', endDate: '' };
  page.value = 1;
  loadOrders();
}

async function acceptOrder(order: AdminOrderVO) {
  await adminOrderApi.accept(order.orderNo);
  await loadOrders();
}
async function rejectOrder(order: AdminOrderVO, reason: string) {
  await adminOrderApi.reject(order.orderNo, reason);
  await loadOrders();
}
async function shipOrder(order: AdminOrderVO) {
  await adminOrderApi.ship(order.orderNo, { deliveryType: order.deliveryType });
  await loadOrders();
}

onMounted(loadOrders);
</script>
