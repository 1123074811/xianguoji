<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">评价管理</h1>
        <p class="font-body-md text-body-md text-slate-500">查看并回复客户评价，维护店铺口碑。</p>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <div v-for="stat in stats" :key="stat.label" class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">{{ stat.label }}</span>
          <span class="material-symbols-outlined p-2 rounded-lg" :class="stat.iconClass">{{ stat.icon }}</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">{{ stat.value }}</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">{{ stat.desc }}</p>
      </div>
    </div>

    <!-- Filter Tabs -->
    <div class="bg-white border border-slate-200 rounded-xl overflow-hidden mb-stack-md">
      <div class="flex border-b border-slate-100 overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.label" @click="activeTab = tab.label"
          class="px-6 py-4 font-label-bold whitespace-nowrap transition-colors"
          :class="activeTab === tab.label ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant hover:text-primary border-b-2 border-transparent'">
          {{ tab.label }}
          <span v-if="tab.badge" class="ml-1 px-1.5 py-0.5 bg-error-container text-on-error-container rounded-full text-[10px]">{{ tab.badge }}</span>
        </button>
      </div>
    </div>

    <!-- Review Cards List -->
    <div class="space-y-gutter">
      <div v-for="review in reviews" :key="review.id" class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div class="flex gap-4">
          <!-- Customer Avatar -->
          <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 font-bold text-xs" :class="review.avatarBg">
            {{ review.avatar }}
          </div>
          <!-- Review Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-3">
                <span class="font-label-bold text-slate-800">{{ review.customer }}</span>
                <div class="flex items-center gap-0.5">
                  <span v-for="i in 5" :key="i" class="material-symbols-outlined text-sm" :class="i <= review.rating ? 'text-amber-400' : 'text-slate-300'" style="font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24;">star</span>
                </div>
                <span class="text-xs text-slate-400">{{ review.date }}</span>
              </div>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="review.statusClass">{{ review.status }}</span>
            </div>

            <p class="text-sm text-slate-700 mb-3">{{ review.content }}</p>

            <!-- Product Info -->
            <div class="flex items-center gap-2 mb-3 text-xs text-slate-500">
              <span class="material-symbols-outlined text-sm">inventory_2</span>
              <span>{{ review.product }}</span>
            </div>

            <!-- AI Suggestion (for negative reviews) -->
            <div v-if="review.aiSuggestion" class="bg-primary/5 border border-primary/20 rounded-lg p-3 mb-3">
              <div class="flex items-center gap-2 mb-1">
                <span class="material-symbols-outlined text-primary text-sm">auto_awesome</span>
                <span class="text-xs font-bold text-primary">AI 建议回复</span>
              </div>
              <p class="text-xs text-slate-600">{{ review.aiSuggestion }}</p>
            </div>

            <!-- Reply Section -->
            <div v-if="review.reply" class="bg-slate-50 rounded-lg p-3 border border-slate-200">
              <div class="flex items-center gap-2 mb-1">
                <span class="material-symbols-outlined text-primary text-sm">store</span>
                <span class="text-xs font-bold text-primary">商家回复</span>
              </div>
              <p class="text-xs text-slate-600">{{ review.reply }}</p>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-3 mt-3">
              <button v-if="!review.reply" class="text-primary text-xs font-bold flex items-center gap-1 hover:underline">
                <span class="material-symbols-outlined text-sm">reply</span>
                回复
              </button>
              <button v-if="review.aiSuggestion && !review.reply" class="text-primary text-xs font-bold flex items-center gap-1 hover:underline">
                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                采用AI建议
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="mt-stack-lg flex items-center justify-between">
      <span class="text-body-sm text-slate-500">显示第 1-5 条，共 86 条评价</span>
      <div class="flex items-center gap-1">
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_left</span></button>
        <button class="w-8 h-8 flex items-center justify-center rounded bg-primary text-on-primary font-bold text-sm shadow-sm">1</button>
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">2</button>
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">3</button>
        <span class="px-2 text-slate-400">...</span>
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-600 hover:bg-white transition-colors text-sm">9</button>
        <button class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors"><span class="material-symbols-outlined text-sm">chevron_right</span></button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('全部')

const stats = [
  { label: '总评价数', value: '1,284', desc: '累计收到', icon: 'rate_review', iconClass: 'text-primary bg-primary/10' },
  { label: '平均评分', value: '4.6', desc: '近30天', icon: 'star', iconClass: 'text-amber-500 bg-amber-50' },
  { label: '待回复', value: '12', desc: '需要尽快处理', icon: 'pending_actions', iconClass: 'text-error bg-error/10' },
  { label: '差评率', value: '2.3%', desc: '低于行业平均', icon: 'trending_down', iconClass: 'text-secondary bg-secondary-fixed' }
]

const tabs = [
  { label: '全部' },
  { label: '待回复', badge: '12' },
  { label: '已回复' },
  { label: '差评' }
]

const reviews = [
  {
    id: 1, customer: '王小红', avatar: 'WX', avatarBg: 'bg-pink-100 text-pink-700',
    rating: 5, date: '2024-01-15 14:30', product: '精品富士苹果 - 6枚装礼盒',
    content: '苹果非常新鲜，口感脆甜！包装也很精美，送人特别有面子。物流速度也很快，当天就送到了。强烈推荐！',
    status: '已回复', statusClass: 'bg-primary-fixed text-on-primary-fixed-variant border border-primary/20',
    reply: '感谢您的好评！我们一直坚持为您提供最新鲜的水果，期待您的再次光临！',
    aiSuggestion: null
  },
  {
    id: 2, customer: '李明', avatar: 'LM', avatarBg: 'bg-blue-100 text-blue-700',
    rating: 4, date: '2024-01-14 09:20', product: '泰国金枕榴莲 - 整果约3kg',
    content: '榴莲味道不错，果肉饱满。但是外皮有点磕碰，希望包装能再加强一下。总体来说还是满意的。',
    status: '待回复', statusClass: 'bg-tertiary-fixed text-on-tertiary-fixed-variant border border-tertiary-container/20',
    reply: null, aiSuggestion: null
  },
  {
    id: 3, customer: '张三', avatar: 'ZS', avatarBg: 'bg-red-100 text-red-700',
    rating: 2, date: '2024-01-13 18:45', product: '有机蓝莓 - 500g/盒',
    content: '蓝莓收到的时候有几颗已经烂了，而且味道不如上次买的好。希望商家能改进品控。',
    status: '待回复', statusClass: 'bg-tertiary-fixed text-on-tertiary-fixed-variant border border-tertiary-container/20',
    reply: null,
    aiSuggestion: '非常抱歉给您带来不好的体验！我们已加强品控检查，并愿意为您补发一盒新鲜蓝莓。请联系客服处理。'
  }
]
</script>
