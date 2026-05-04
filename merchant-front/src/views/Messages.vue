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
        <button v-for="tab in tabs" :key="tab.value" @click="activeTab = tab.value"
          class="px-6 py-4 font-label-bold whitespace-nowrap transition-colors"
          :class="activeTab === tab.value ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant hover:text-primary border-b-2 border-transparent'">
          {{ tab.label }}
          <span v-if="badgeCount(tab.value)" class="ml-1 px-1.5 py-0.5 bg-error-container text-on-error-container rounded-full text-[10px]">{{ badgeCount(tab.value) }}</span>
        </button>
      </div>
    </div>

    <!-- Message List -->
    <div class="space-y-3">
      <div v-for="msg in filteredMessages" :key="msg.id"
        class="bg-white border rounded-xl p-5 hover:shadow-md transition-all cursor-pointer group"
        :class="msg.isRead ? 'border-outline-variant' : 'border-primary/30 bg-primary/5'"
        @click="onClickMessage(msg)">
        <div class="flex gap-4">
          <div class="w-10 h-10 rounded-full flex items-center justify-center shrink-0" :class="typeMeta(msg.type).iconBg">
            <span class="material-symbols-outlined text-lg" :class="typeMeta(msg.type).iconColor" style="font-variation-settings: 'FILL' 1;">{{ typeMeta(msg.type).icon }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-1">
              <div class="flex items-center gap-2">
                <span v-if="!msg.isRead" class="w-2 h-2 bg-primary rounded-full"></span>
                <span class="font-label-bold text-slate-800">{{ msg.title }}</span>
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold" :class="typeMeta(msg.type).tagClass">{{ typeMeta(msg.type).tag }}</span>
              </div>
              <span class="text-xs text-slate-400 shrink-0">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <p class="text-sm text-slate-600 mb-2">{{ msg.content }}</p>
            <div v-if="msg.linkUrl" class="mt-2">
              <router-link :to="msg.linkUrl" class="text-xs font-label-bold text-primary flex items-center gap-1 hover:underline">
                查看详情 <span class="material-symbols-outlined text-sm">arrow_forward</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && filteredMessages.length === 0" class="text-center py-20">
      <span class="material-symbols-outlined text-slate-200 text-6xl">notifications_off</span>
      <p class="text-slate-400 mt-4">暂无消息</p>
    </div>
    <div v-if="loading" class="text-center py-12 text-slate-400 text-sm">加载中…</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminShopApi } from '@/api/modules/shop'
import type { AdminNotificationVO } from '@/api/types/shop'

const messages = ref<AdminNotificationVO[]>([])
const loading = ref(false)
const activeTab = ref<number | 0>(0)

const tabs = [
  { label: '全部', value: 0 },
  { label: '订单消息', value: 1 },
  { label: '库存预警', value: 2 },
  { label: '评价提醒', value: 3 },
  { label: '营销通知', value: 4 },
  { label: '系统通知', value: 5 },
]

const typeMap: Record<number, { icon: string; iconBg: string; iconColor: string; tag: string; tagClass: string }> = {
  1: { icon: 'shopping_cart', iconBg: 'bg-primary/10', iconColor: 'text-primary', tag: '订单', tagClass: 'bg-primary-fixed text-on-primary-fixed-variant' },
  2: { icon: 'inventory', iconBg: 'bg-orange-50', iconColor: 'text-orange-600', tag: '库存', tagClass: 'bg-orange-100 text-orange-700' },
  3: { icon: 'rate_review', iconBg: 'bg-blue-50', iconColor: 'text-blue-600', tag: '评价', tagClass: 'bg-blue-100 text-blue-700' },
  4: { icon: 'campaign', iconBg: 'bg-purple-50', iconColor: 'text-purple-600', tag: '营销', tagClass: 'bg-purple-100 text-purple-700' },
  5: { icon: 'info', iconBg: 'bg-slate-100', iconColor: 'text-slate-600', tag: '系统', tagClass: 'bg-slate-100 text-slate-600' },
}

function typeMeta(t: number) {
  return typeMap[t] || typeMap[5]
}

function badgeCount(v: number) {
  if (v === 0) return messages.value.filter(m => !m.isRead).length
  return messages.value.filter(m => !m.isRead && m.type === v).length
}

const filteredMessages = computed(() => {
  if (activeTab.value === 0) return messages.value
  return messages.value.filter(m => m.type === activeTab.value)
})

function formatTime(s?: string) {
  if (!s) return ''
  const d = new Date(s.replace(' ', 'T'))
  if (isNaN(d.getTime())) return s
  const diff = Date.now() - d.getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const h = Math.floor(min / 60)
  if (h < 24) return `${h} 小时前`
  const day = Math.floor(h / 24)
  if (day < 30) return `${day} 天前`
  return s.slice(0, 10)
}

async function loadList() {
  loading.value = true
  try {
    messages.value = await adminShopApi.notificationList()
  } catch (e) {
    console.warn('加载通知失败', e)
  } finally {
    loading.value = false
  }
}

async function markAllRead() {
  try {
    await adminShopApi.markAllNotificationRead()
    messages.value.forEach(m => (m.isRead = 1))
  } catch (e) {
    console.warn('标记已读失败', e)
  }
}

async function onClickMessage(msg: AdminNotificationVO) {
  if (msg.isRead) return
  try {
    await adminShopApi.markNotificationRead(msg.id)
    msg.isRead = 1
  } catch (e) {
    console.warn('标记已读失败', e)
  }
}

onMounted(loadList)
</script>
