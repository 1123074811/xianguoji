<template>
  <div>
    <!-- Breadcrumbs -->
    <nav class="flex items-center gap-2 text-xs text-slate-400 font-medium mb-4">
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/orders')">订单管理</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="text-slate-600">订单详情 ORD-2023-8842</span>
    </nav>

    <!-- Order Progress Tracker -->
    <div class="bg-white border border-slate-200 rounded-xl p-6 mb-gutter">
      <div class="flex items-center justify-between mb-6">
        <h2 class="font-h3 text-h3 text-slate-900">订单进度</h2>
        <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-primary-fixed text-on-primary-fixed-variant border border-primary/20">配送中</span>
      </div>
      <div class="flex items-center justify-between">
        <div v-for="(step, i) in progressSteps" :key="i" class="flex-1 flex items-center">
          <div class="flex flex-col items-center flex-1">
            <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold" :class="step.completed ? 'bg-primary text-white' : step.current ? 'bg-primary/20 text-primary border-2 border-primary' : 'bg-slate-100 text-slate-400'">
              <span v-if="step.completed" class="material-symbols-outlined text-lg">check</span>
              <span v-else>{{ i + 1 }}</span>
            </div>
            <span class="text-xs font-medium mt-2" :class="step.completed || step.current ? 'text-primary' : 'text-slate-400'">{{ step.label }}</span>
            <span class="text-[10px] text-slate-400">{{ step.time }}</span>
          </div>
          <div v-if="i < progressSteps.length - 1" class="h-0.5 flex-1 mx-2" :class="step.completed ? 'bg-primary' : 'bg-slate-200'"></div>
        </div>
      </div>
    </div>

    <!-- Info Cards Row -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-gutter mb-gutter">
      <!-- Customer Info -->
      <div class="bg-white border border-slate-200 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <span class="material-symbols-outlined text-primary">person</span>
          <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">客户信息</h3>
        </div>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-slate-500">姓名</span><span class="font-medium">李周</span></div>
          <div class="flex justify-between"><span class="text-slate-500">电话</span><span class="font-medium">+86 138****5521</span></div>
          <div class="flex justify-between"><span class="text-slate-500">会员等级</span><span class="font-medium text-primary">金牌会员</span></div>
          <div class="flex justify-between"><span class="text-slate-500">历史订单</span><span class="font-medium">42 单</span></div>
        </div>
      </div>

      <!-- Shipping Info -->
      <div class="bg-white border border-slate-200 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <span class="material-symbols-outlined text-primary">local_shipping</span>
          <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">配送信息</h3>
        </div>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-slate-500">方式</span><span class="font-medium">🚚 商家配送</span></div>
          <div class="flex justify-between"><span class="text-slate-500">地址</span><span class="font-medium text-right max-w-[180px]">加利福尼亚州 90210 有机区绿巷 45 号</span></div>
          <div class="flex justify-between"><span class="text-slate-500">预计送达</span><span class="font-medium text-primary">今日 16:00-18:00</span></div>
          <div class="flex justify-between"><span class="text-slate-500">骑手</span><span class="font-medium">王师傅 (138****9900)</span></div>
        </div>
      </div>

      <!-- Order Stats -->
      <div class="bg-white border border-slate-200 rounded-xl p-6">
        <div class="flex items-center gap-2 mb-4">
          <span class="material-symbols-outlined text-primary">receipt_long</span>
          <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">订单统计</h3>
        </div>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between"><span class="text-slate-500">订单号</span><span class="font-medium text-primary">ORD-2023-8842</span></div>
          <div class="flex justify-between"><span class="text-slate-500">下单时间</span><span class="font-medium">2023-10-24 14:30</span></div>
          <div class="flex justify-between"><span class="text-slate-500">支付方式</span><span class="font-medium">微信支付</span></div>
          <div class="flex justify-between"><span class="text-slate-500">备注</span><span class="font-medium text-slate-400">无</span></div>
        </div>
      </div>
    </div>

    <!-- Product List Table -->
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
            <tr v-for="item in orderItems" :key="item.name" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3 flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-slate-100 overflow-hidden shrink-0 flex items-center justify-center">
                  <span class="material-symbols-outlined text-slate-400 text-sm">nutrition</span>
                </div>
                <span class="font-label-bold text-slate-800">{{ item.name }}</span>
              </td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ item.spec }}</td>
              <td class="px-6 py-3 text-sm text-right text-slate-600">¥{{ item.price }}</td>
              <td class="px-6 py-3 text-sm text-right text-slate-600">x{{ item.qty }}</td>
              <td class="px-6 py-3 text-sm text-right font-medium text-slate-800">¥{{ (item.price * item.qty).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- Price Breakdown -->
      <div class="px-6 py-4 bg-slate-50 border-t border-slate-100">
        <div class="flex justify-end">
          <div class="w-72 space-y-2 text-sm">
            <div class="flex justify-between"><span class="text-slate-500">商品小计</span><span>¥438.00</span></div>
            <div class="flex justify-between"><span class="text-slate-500">配送费</span><span>¥5.50</span></div>
            <div class="flex justify-between"><span class="text-slate-500">优惠券</span><span class="text-error">-¥14.00</span></div>
            <div class="flex justify-between pt-2 border-t border-slate-200 font-bold text-base">
              <span>实付金额</span><span class="text-primary">¥429.50</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Operation Log -->
    <div class="bg-white border border-slate-200 rounded-xl p-6 mb-gutter">
      <h3 class="font-h3 text-h3 text-slate-900 mb-4">操作日志</h3>
      <div class="space-y-4">
        <div v-for="log in operationLogs" :key="log.time" class="flex gap-4">
          <div class="flex flex-col items-center">
            <div class="w-3 h-3 rounded-full bg-primary"></div>
            <div class="w-0.5 flex-1 bg-slate-200"></div>
          </div>
          <div class="pb-4">
            <p class="font-label-bold text-slate-800">{{ log.action }}</p>
            <p class="text-xs text-slate-500">{{ log.time }} · {{ log.operator }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Action Bar -->
    <div class="fixed bottom-0 right-0 left-60 h-20 bg-white border-t border-slate-200 px-gutter flex items-center justify-between z-50">
      <div class="flex items-center gap-2 text-slate-400">
        <span class="material-symbols-outlined text-lg">history</span>
        <span class="text-xs font-medium italic">最后更新于 5 分钟前</span>
      </div>
      <div class="flex items-center gap-gutter">
        <button class="px-6 py-2.5 rounded-lg border border-slate-200 text-slate-600 font-label-bold text-label-bold hover:bg-slate-50 active:scale-95 transition-all">返回列表</button>
        <button class="px-6 py-2.5 rounded-lg border border-primary text-primary font-label-bold text-label-bold hover:bg-primary/5 active:scale-95 transition-all">打印面单</button>
        <button class="px-10 py-2.5 rounded-lg bg-primary text-white font-label-bold text-label-bold shadow-md hover:bg-primary-container active:scale-95 transition-all">确认送达</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const progressSteps = [
  { label: '下单成功', time: '14:30', completed: true, current: false },
  { label: '商家接单', time: '14:35', completed: true, current: false },
  { label: '备货完成', time: '14:50', completed: true, current: false },
  { label: '骑手取件', time: '15:10', completed: true, current: false },
  { label: '配送中', time: '15:30', completed: false, current: true },
  { label: '已送达', time: '', completed: false, current: false }
]

const orderItems = [
  { name: '精品富士苹果', spec: '6枚装礼盒', price: 168.0, qty: 2 },
  { name: '泰国金枕榴莲', spec: '整果约3kg', price: 89.0, qty: 1 },
  { name: '有机蓝莓', spec: '500g/盒', price: 32.5, qty: 1 }
]

const operationLogs = [
  { action: '骑手已取件，正在配送中', time: '2023-10-24 15:10', operator: '系统' },
  { action: '备货完成，等待骑手取件', time: '2023-10-24 14:50', operator: '店员-小王' },
  { action: '商家已接单，开始备货', time: '2023-10-24 14:35', operator: '系统' },
  { action: '用户下单成功', time: '2023-10-24 14:30', operator: '系统' }
]
</script>
