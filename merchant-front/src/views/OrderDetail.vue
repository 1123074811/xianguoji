<template>
  <div v-if="order">
    <!-- Breadcrumbs -->
    <nav class="flex items-center gap-2 text-xs text-slate-400 font-medium mb-4">
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/orders')">订单管理</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="text-slate-600">订单详情 {{ order.orderNo }}</span>
    </nav>

    <!-- Order Progress -->
    <div class="bg-white border border-slate-200 rounded-xl p-6 mb-gutter">
      <div class="flex items-center justify-between mb-6">
        <h2 class="font-h3 text-h3 text-slate-900">订单进度</h2>
        <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold border" :class="statusBadge(order.status).cls">
          {{ statusBadge(order.status).label }}
        </span>
      </div>
      <div class="flex items-center justify-between">
        <div v-for="(step, i) in progressSteps" :key="i" class="flex-1 flex items-center">
          <div class="flex flex-col items-center flex-1">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold"
              :class="step.completed ? 'bg-primary text-white' : step.current ? 'bg-primary/20 text-primary border-2 border-primary' : 'bg-slate-100 text-slate-400'">
              <span v-if="step.completed" class="material-symbols-outlined text-lg">check</span>
              <span v-else>{{ i + 1 }}</span>
            </div>
            <span class="text-xs font-medium mt-2" :class="step.completed || step.current ? 'text-primary' : 'text-slate-400'">{{ step.label }}</span>
            <span class="text-[10px] text-slate-400">{{ step.time || '' }}</span>
          </div>
          <div v-if="i < progressSteps.length - 1" class="h-0.5 flex-1 mx-2" :class="step.completed ? 'bg-primary' : 'bg-slate-200'"></div>
        </div>
      </div>
    </div>

    <!-- Info Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-gutter mb-gutter">
      <div class="bg-white border border-slate-200 rounded-xl p-6">
        <h3 class="font-label-bold text-slate-900 mb-4">客户信息</h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-slate-500">姓名</span><span class="font-medium">{{ order.consignee }}</span></div>
          <div class="flex justify-between"><span class="text-slate-500">电话</span><span class="font-medium">{{ order.consigneePhone }}</span></div>
          <div class="flex justify-between"><span class="text-slate-500">地址</span><span class="font-medium text-right max-w-[180px]">{{ order.consigneeAddress }}</span></div>
        </div>
      </div>

      <div class="bg-white border border-slate-200 rounded-xl p-6">
        <h3 class="font-label-bold text-slate-900 mb-4">配送信息</h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-slate-500">方式</span><span class="font-medium">{{ order.deliveryType === 1 ? '🚚 商家配送' : '🏠 到店自提' }}</span></div>
          <div v-if="order.deliveryTime" class="flex justify-between"><span class="text-slate-500">时段</span><span class="font-medium">{{ order.deliveryTime }}</span></div>
          <div v-if="order.pickupCode" class="flex justify-between"><span class="text-slate-500">自提码</span><span class="font-bold text-primary">{{ order.pickupCode }}</span></div>
        </div>
      </div>

      <div class="bg-white border border-slate-200 rounded-xl p-6">
        <h3 class="font-label-bold text-slate-900 mb-4">订单信息</h3>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-slate-500">订单号</span><span class="font-medium text-primary">{{ order.orderNo }}</span></div>
          <div class="flex justify-between"><span class="text-slate-500">下单时间</span><span class="font-medium">{{ order.createdAt }}</span></div>
          <div v-if="order.payTime" class="flex justify-between"><span class="text-slate-500">支付时间</span><span class="font-medium">{{ order.payTime }}</span></div>
          <div class="flex justify-between"><span class="text-slate-500">备注</span><span class="font-medium" :class="order.userRemark ? '' : 'text-slate-400'">{{ order.userRemark || '无' }}</span></div>
        </div>
      </div>
    </div>

    <!-- Items -->
    <div class="bg-white border border-slate-200 rounded-xl overflow-hidden mb-gutter">
      <div class="px-6 py-4 border-b border-slate-100">
        <h3 class="font-h3 text-h3 text-slate-900">商品清单</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">商品</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">规格</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">单价</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">数量</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">小计</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="item in order.items" :key="item.id" class="hover:bg-slate-50 h-[48px]">
              <td class="px-6 py-3 flex items-center gap-3">
                <img v-if="item.image" :src="resolveImageUrl(item.image)" class="w-10 h-10 rounded-lg object-cover shrink-0" alt="" />
                <div v-else class="w-10 h-10 rounded-lg bg-slate-100 flex items-center justify-center shrink-0">
                  <span class="material-symbols-outlined text-slate-400 text-sm">nutrition</span>
                </div>
                <span class="font-label-bold text-slate-800">{{ item.productName }}</span>
              </td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ item.specName }}</td>
              <td class="px-6 py-3 text-sm text-right text-slate-600">¥{{ item.price }}</td>
              <td class="px-6 py-3 text-sm text-right text-slate-600">x{{ item.quantity }}</td>
              <td class="px-6 py-3 text-sm text-right font-medium text-slate-800">¥{{ item.subtotal }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-6 py-4 bg-slate-50 border-t border-slate-100">
        <div class="flex justify-end">
          <div class="w-72 space-y-2 text-sm">
            <div class="flex justify-between"><span class="text-slate-500">商品小计</span><span>¥{{ order.goodsAmount }}</span></div>
            <div class="flex justify-between"><span class="text-slate-500">配送费</span><span>¥{{ order.deliveryFee }}</span></div>
            <div v-if="Number(order.couponAmount) > 0" class="flex justify-between"><span class="text-slate-500">优惠券</span><span class="text-error">-¥{{ order.couponAmount }}</span></div>
            <div v-if="Number(order.discountAmount) > 0" class="flex justify-between"><span class="text-slate-500">满减</span><span class="text-error">-¥{{ order.discountAmount }}</span></div>
            <div class="flex justify-between pt-2 border-t border-slate-200 font-bold text-base">
              <span>实付金额</span><span class="text-primary">¥{{ order.payAmount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Action Bar -->
    <div class="fixed bottom-0 right-0 left-60 h-20 bg-white border-t border-slate-200 px-gutter flex items-center justify-end gap-gutter z-50">
      <button class="px-6 py-2.5 rounded-lg border border-slate-200 text-slate-600 font-label-bold hover:bg-slate-50" @click="$router.push('/orders')">返回列表</button>
      <button v-if="order.status === 1" class="px-6 py-2.5 rounded-lg border border-primary text-primary font-label-bold hover:bg-primary/5" @click="onAccept">接单</button>
      <PopInput v-if="order.status === 1" title="拒单原因" :message="`拒单 ${order.orderNo} 后金额将原路退回，请填写拒单原因。`" placeholder="请输入拒单原因" type="danger" confirm-text="确认拒单" placement="top" @confirm="onReject($event)">
        <button class="px-6 py-2.5 rounded-lg border border-error text-error font-label-bold hover:bg-error/5">拒单</button>
      </PopInput>
      <button v-if="order.status === 2" class="px-10 py-2.5 rounded-lg bg-primary text-white font-label-bold shadow-md hover:bg-primary-container" @click="onShip">标记发货</button>
      <PopInput v-if="order.deliveryType === 2 && order.status === 2" title="核销自提码" message="请输入用户出示的自提码进行核销。" placeholder="输入自提码" type="info" confirm-text="核销" placement="top" @confirm="onPickupVerify($event)">
        <button class="px-10 py-2.5 rounded-lg bg-primary text-white font-label-bold shadow-md">核销自提码</button>
      </PopInput>
    </div>
  </div>
  <div v-else class="text-center text-slate-400 py-20">{{ loadError ? '加载失败' : '加载中...' }}</div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { adminOrderApi } from '@/api/modules/order';
import type { AdminOrderVO } from '@/api/types/order';
import { resolveImageUrl } from '@/utils/image';
import PopInput from '@/components/PopInput.vue';

const route = useRoute();
const router = useRouter();
const order = ref<AdminOrderVO | null>(null);
const loadError = ref(false);

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

function statusBadge(s: number) {
  return STATUS_MAP[s] || { label: `状态${s}`, cls: 'bg-slate-100 text-slate-600 border-slate-200' };
}

const progressSteps = computed(() => {
  if (!order.value) return [];
  const s = order.value.status;
  return [
    { label: '下单成功', time: order.value.createdAt, completed: s >= 1, current: s === 0 },
    { label: '商家接单', time: '', completed: s >= 2, current: s === 1 },
    { label: '备货中', time: '', completed: s >= 3, current: s === 2 },
    { label: order.value.deliveryType === 1 ? '配送中' : '待自提', time: '', completed: s >= 4, current: s === 3 },
    { label: '已送达', time: order.value.deliveredAt || '', completed: s >= 5, current: s === 4 },
    { label: '已完成', time: order.value.finishedAt || '', completed: s >= 5, current: false },
  ];
});

async function load() {
  const orderNo = String(route.params.id || '');
  if (!orderNo) return;
  try {
    order.value = await adminOrderApi.detail(orderNo);
  } catch (e) {
    loadError.value = true;
    console.warn('加载订单详情失败', e);
  }
}

async function onAccept() {
  if (!order.value) return;
  await adminOrderApi.accept(order.value.orderNo);
  await load();
}
async function onReject(reason: string) {
  if (!order.value) return;
  await adminOrderApi.reject(order.value.orderNo, reason);
  router.push('/orders');
}
async function onShip() {
  if (!order.value) return;
  await adminOrderApi.ship(order.value.orderNo, { deliveryType: order.value.deliveryType });
  await load();
}
async function onPickupVerify(code: string) {
  if (!order.value) return;
  await adminOrderApi.pickupVerify(order.value.orderNo, code);
  await load();
}

onMounted(load);
</script>
