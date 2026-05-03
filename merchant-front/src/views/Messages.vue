<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">消息中心</h1>
        <p class="font-body-md text-body-md text-slate-500">查看系统通知、订单消息和互动提醒。</p>
      </div>
      <button class="px-4 py-2 text-xs font-label-bold text-primary hover:bg-primary/5 rounded-lg transition-colors" @click="markAllRead">
        全部标记已读
      </button>
    </div>

    <!-- Message Tabs -->
    <div class="bg-white border border-outline-variant rounded-xl overflow-hidden mb-stack-md">
      <div class="flex border-b border-outline-variant overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.label" @click="activeTab = tab.label"
          class="px-6 py-4 font-label-bold whitespace-nowrap transition-colors"
          :class="activeTab === tab.label ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant hover:text-primary border-b-2 border-transparent'">
          {{ tab.label }}
          <span v-if="tab.badge" class="ml-1 px-1.5 py-0.5 bg-error-container text-on-error-container rounded-full text-[10px]">{{ tab.badge }}</span>
        </button>
      </div>
    </div>

    <!-- Message List -->
    <div class="space-y-3">
      <div v-for="msg in filteredMessages" :key="msg.id"
        class="bg-white border rounded-xl p-5 hover:shadow-md transition-all cursor-pointer group"
        :class="msg.read ? 'border-outline-variant' : 'border-primary/30 bg-primary/5'">
        <div class="flex gap-4">
          <!-- Icon -->
          <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" :class="msg.iconBg">
            <span class="material-symbols-outlined text-lg" :class="msg.iconColor" style="font-variation-settings: 'FILL' 1;">{{ msg.icon }}</span>
          </div>
          <!-- Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-1">
              <div class="flex items-center gap-2">
                <span v-if="!msg.read" class="w-2 h-2 bg-primary rounded-full"></span>
                <span class="font-label-bold text-slate-800">{{ msg.title }}</span>
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold" :class="msg.tagClass">{{ msg.tag }}</span>
              </div>
              <span class="text-xs text-slate-400 shrink-0">{{ msg.time }}</span>
            </div>
            <p class="text-sm text-slate-600 mb-2">{{ msg.content }}</p>
            <!-- Action Buttons -->
            <div v-if="msg.actions" class="flex items-center gap-2">
              <button v-for="action in msg.actions" :key="action.label"
                class="px-3 py-1 text-xs font-label-bold rounded transition-colors"
                :class="action.primary ? 'bg-primary text-white hover:bg-primary/90' : 'border border-slate-200 text-slate-600 hover:bg-slate-50'">
                {{ action.label }}
              </button>
            </div>
            <!-- Related Link -->
            <div v-if="msg.link" class="mt-2">
              <router-link :to="msg.link" class="text-xs font-label-bold text-primary flex items-center gap-1 hover:underline">
                查看详情 <span class="material-symbols-outlined text-sm">arrow_forward</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="filteredMessages.length === 0" class="text-center py-20">
      <span class="material-symbols-outlined text-slate-200 text-6xl">notifications_off</span>
      <p class="text-slate-400 mt-4">暂无消息</p>
    </div>

    <!-- Pagination -->
    <div v-if="filteredMessages.length > 0" class="mt-stack-lg flex items-center justify-between">
      <span class="text-body-sm text-slate-500">显示第 1-10 条，共 {{ filteredMessages.length }} 条消息</span>
      <div class="flex items-center gap-1">
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_left</span></button>
        <button class="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary font-bold text-sm shadow-sm">1</button>
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_right</span></button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const activeTab = ref('全部')

const tabs = [
  { label: '全部', badge: '8' },
  { label: '订单消息', badge: '3' },
  { label: '系统通知', badge: '2' },
  { label: '评价提醒', badge: '2' },
  { label: '营销通知', badge: '1' }
]

const messages = [
  {
    id: 1, type: '订单', icon: 'shopping_cart', iconBg: 'bg-primary/10', iconColor: 'text-primary',
    title: '新订单待处理', tag: '订单', tagClass: 'bg-primary-fixed text-on-primary-fixed-variant',
    content: '客户李周下单了精品富士苹果和泰国榴莲，订单金额 ¥458.00，请在2小时内确认接单。',
    time: '5 分钟前', read: false,
    actions: [{ label: '立即接单', primary: true }, { label: '拒单', primary: false }],
    link: '/orders'
  },
  {
    id: 2, type: '订单', icon: 'schedule', iconBg: 'bg-amber-50', iconColor: 'text-amber-600',
    title: '订单超时提醒', tag: '加急', tagClass: 'bg-error-container text-on-error-container',
    content: '订单 ORD-2023-8838 已超过备货时限（30分钟），请尽快处理以避免客户投诉。',
    time: '15 分钟前', read: false,
    link: '/orders'
  },
  {
    id: 3, type: '库存', icon: 'inventory', iconBg: 'bg-orange-50', iconColor: 'text-orange-600',
    title: '库存预警', tag: '库存', tagClass: 'bg-orange-100 text-orange-700',
    content: '泰国金枕榴莲当前库存仅剩 3 件，低于安全库存阈值（50件），建议尽快补货。',
    time: '1 小时前', read: false,
    link: '/goods'
  },
  {
    id: 4, type: '评价', icon: 'rate_review', iconBg: 'bg-blue-50', iconColor: 'text-blue-600',
    title: '收到差评待回复', tag: '评价', tagClass: 'bg-blue-100 text-blue-700',
    content: '客户张三对有机蓝莓给出了2星评价，建议尽快回复以维护店铺口碑。',
    time: '2 小时前', read: false,
    link: '/reviews'
  },
  {
    id: 5, type: '系统', icon: 'info', iconBg: 'bg-slate-100', iconColor: 'text-slate-600',
    title: '系统维护通知', tag: '系统', tagClass: 'bg-slate-100 text-slate-600',
    content: '系统将于今晚 02:00-04:00 进行例行维护升级，届时部分功能可能暂时不可用。',
    time: '3 小时前', read: true
  },
  {
    id: 6, type: '营销', icon: 'campaign', iconBg: 'bg-purple-50', iconColor: 'text-purple-600',
    title: '优惠券即将到期', tag: '营销', tagClass: 'bg-purple-100 text-purple-700',
    content: '「春节礼盒专享」优惠券将于 2024-02-15 到期，当前核销率仅 64.8%，建议推送提醒。',
    time: '5 小时前', read: true,
    link: '/campaign'
  },
  {
    id: 7, type: '订单', icon: 'payments', iconBg: 'bg-error/10', iconColor: 'text-error',
    title: '退款申请待处理', tag: '退款', tagClass: 'bg-error-container text-on-error-container',
    content: '客户刘芳申请退款 ¥89.90，原因：商品与描述不符。请在48小时内处理。',
    time: '6 小时前', read: false,
    link: '/orders'
  },
  {
    id: 8, type: '评价', icon: 'star', iconBg: 'bg-amber-50', iconColor: 'text-amber-500',
    title: '新好评提醒', tag: '评价', tagClass: 'bg-amber-100 text-amber-700',
    content: '客户王小红对精品富士苹果给出了5星好评："苹果非常新鲜，包装精美！"',
    time: '1 天前', read: true,
    link: '/reviews'
  }
]

const filteredMessages = computed(() => {
  if (activeTab.value === '全部') return messages
  if (activeTab.value === '订单消息') return messages.filter(m => m.type === '订单' || m.type === '库存')
  if (activeTab.value === '系统通知') return messages.filter(m => m.type === '系统')
  if (activeTab.value === '评价提醒') return messages.filter(m => m.type === '评价')
  if (activeTab.value === '营销通知') return messages.filter(m => m.type === '营销')
  return messages
})

const markAllRead = () => {
  messages.forEach(m => m.read = true)
}
</script>
