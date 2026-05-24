<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg">
      <h1 class="font-h1 text-h1 text-on-surface mb-1">在线客服</h1>
      <p class="font-body-md text-body-md text-slate-500">与用户实时沟通，解答商品咨询。</p>
    </div>

    <div class="flex gap-6 h-[calc(100vh-12rem)]">
      <!-- User List -->
      <div class="w-80 shrink-0 bg-white border border-outline-variant rounded-xl flex flex-col overflow-hidden">
        <div class="p-4 border-b border-outline-variant">
          <div class="relative">
            <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">search</span>
            <input v-model="searchKey" class="w-full pl-9 pr-4 py-2 bg-slate-50 rounded-lg text-sm focus:ring-1 focus:ring-primary outline-none" placeholder="搜索用户..." />
          </div>
        </div>
        <div class="flex-1 overflow-y-auto">
          <div v-for="user in filteredUsers" :key="user.userId"
            class="flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-slate-50 transition-colors border-b border-slate-100"
            :class="activeUserId === user.userId ? 'bg-green-50 border-l-4 border-l-green-700' : ''"
            @click="selectUser(user)">
            <el-badge :value="user.unreadCount" :max="99" :hidden="user.unreadCount <= 0">
              <img :src="user.avatar || '/default-avatar.png'" class="w-10 h-10 rounded-full object-cover border border-slate-200" />
            </el-badge>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <span class="text-sm font-semibold text-slate-800 truncate">{{ user.nickname || `用户${user.userId}` }}</span>
                <span class="text-[10px] text-slate-400 shrink-0">{{ formatTime(user.lastTime) }}</span>
              </div>
              <p class="text-xs text-slate-500 truncate mt-0.5">{{ user.lastMessage || '暂无消息' }}</p>
            </div>
          </div>
          <div v-if="!userLoading && filteredUsers.length === 0" class="text-center py-16">
            <span class="material-symbols-outlined text-slate-200 text-5xl">forum</span>
            <p class="text-slate-400 mt-3 text-sm">暂无用户咨询</p>
          </div>
        </div>
      </div>

      <!-- Chat Area -->
      <div class="flex-1 bg-white border border-outline-variant rounded-xl flex flex-col overflow-hidden">
        <!-- No user selected -->
        <div v-if="!activeUserId" class="flex-1 flex items-center justify-center">
          <div class="text-center">
            <span class="material-symbols-outlined text-slate-200 text-7xl">chat</span>
            <p class="text-slate-400 mt-4">选择左侧用户开始聊天</p>
          </div>
        </div>

        <template v-else>
          <!-- Chat Header -->
          <div class="px-6 py-4 border-b border-outline-variant flex items-center justify-between">
            <div class="flex items-center gap-3">
              <img :src="activeUser?.avatar || '/default-avatar.png'" class="w-9 h-9 rounded-full object-cover border border-slate-200" />
              <div>
                <span class="font-semibold text-slate-800">{{ activeUser?.nickname || `用户${activeUserId}` }}</span>
              </div>
            </div>
          </div>

          <!-- Messages -->
          <div ref="msgListRef" class="flex-1 overflow-y-auto px-6 py-4 space-y-4">
            <div v-for="msg in messages" :key="msg.id" class="flex gap-3" :class="msg.senderType === 1 ? 'flex-row-reverse' : ''">
              <!-- Avatar -->
              <img v-if="msg.senderAvatar" :src="resolveImageUrl(msg.senderAvatar)"
                class="w-8 h-8 rounded-full shrink-0 object-cover border border-slate-200" />
              <div v-else class="w-8 h-8 rounded-full shrink-0 flex items-center justify-center"
                :class="msg.senderType === 1 ? 'bg-green-100' : 'bg-slate-100'">
                <span class="material-symbols-outlined text-sm" :class="msg.senderType === 1 ? 'text-green-700' : 'text-slate-500'"
                  style="font-variation-settings: 'FILL' 1;">{{ msg.senderType === 1 ? 'store' : 'person' }}</span>
              </div>
              <!-- Bubble -->
              <div class="max-w-[70%]" :class="msg.senderType === 1 ? 'items-end' : 'items-start'">
                <!-- Text -->
                <div v-if="msg.msgType === 'text'" class="px-4 py-2.5 rounded-2xl text-sm leading-relaxed"
                  :class="msg.senderType === 1 ? 'bg-green-700 text-white rounded-tr-sm' : 'bg-slate-100 text-slate-800 rounded-tl-sm'">
                  {{ msg.content }}
                </div>
                <!-- Product Card -->
                <div v-else-if="msg.msgType === 'product' && msg.productCard" class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm max-w-xs cursor-pointer hover:shadow-md transition-shadow" @click="goToProduct(msg.productCard.productId)">
                  <img :src="resolveImageUrl(msg.productCard.mainImage)" class="w-full h-32 object-cover" />
                  <div class="p-3">
                    <p class="text-sm font-medium text-slate-800 line-clamp-2">{{ msg.productCard.name }}</p>
                    <div class="flex items-center justify-between mt-1">
                      <span class="text-red-600 font-bold">¥{{ msg.productCard.price }}</span>
                      <span v-if="msg.productCard.specName" class="text-xs text-slate-400">{{ msg.productCard.specName }}</span>
                    </div>
                  </div>
                </div>
                <!-- Image -->
                <div v-else-if="msg.msgType === 'image'" class="space-y-1">
                  <img v-for="(img, idx) in msg.images" :key="idx" :src="resolveImageUrl(img)" class="max-w-[240px] rounded-xl cursor-pointer hover:opacity-90 transition-opacity" @click="previewImage(img)" />
                </div>
                <!-- Time -->
                <div class="mt-1 flex items-center gap-2" :class="msg.senderType === 1 ? 'justify-end text-right' : ''">
                  <span class="text-[10px] text-slate-400">{{ formatTime(msg.createdAt) }}</span>
                  <span v-if="msg.senderType === 1" class="text-[10px] text-slate-400">{{ msg.isRead === 1 ? '已读' : '未读' }}</span>
                </div>
              </div>
            </div>
            <div v-if="msgLoading" class="text-center py-4 text-slate-400 text-sm">加载中...</div>
          </div>

          <!-- Input -->
          <div class="px-6 py-4 border-t border-outline-variant">
            <div class="flex items-center gap-3 h-10">
              <div class="flex-1 relative">
                <input v-model="inputText" @keydown.enter.exact.prevent="handleSend"
                  class="w-full h-10 px-4 bg-slate-50 rounded-xl text-sm outline-none focus:ring-1 focus:ring-primary"
                  placeholder="输入回复内容，Enter 发送..." />
              </div>
              <div class="flex items-center gap-2 shrink-0">
                <label class="h-10 flex items-center px-3 rounded-xl text-sm font-bold transition-all cursor-pointer bg-slate-100 text-slate-500 hover:bg-slate-200">
                  <span class="material-symbols-outlined text-base align-middle">image</span>
                  <input type="file" accept="image/*" class="hidden" @change="handleImageUpload" />
                </label>
                <button @click="handleSend" :disabled="!inputText.trim() && !pendingImage"
                  class="h-10 px-6 rounded-xl text-sm font-bold transition-all"
                  :class="(inputText.trim() || pendingImage) ? 'bg-green-700 text-white hover:bg-green-800' : 'bg-slate-200 text-slate-400 cursor-not-allowed'">
                  发送
                </button>
              </div>
            </div>
            <!-- Image preview before send -->
            <div v-if="pendingImage" class="mt-2 flex items-center gap-2 bg-slate-50 rounded-lg p-2">
              <img :src="pendingImage" class="w-16 h-16 object-cover rounded-lg" />
              <span class="text-xs text-slate-400">待发送图片</span>
              <button class="ml-auto text-slate-400 hover:text-red-500" @click="pendingImage = ''">
                <span class="material-symbols-outlined text-base">close</span>
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Image Preview -->
    <el-image-viewer
      v-if="showPreview"
      :url-list="[previewSrc]"
      @close="showPreview = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElBadge, ElImageViewer } from 'element-plus'
import { adminChatApi } from '@/api/modules/chat'
import type { ChatUserVO, ChatMessageVO } from '@/api/types/chat'
import { useAdminStore } from '@/stores/admin'

import { API_BASE_URL } from '@/config/env'
import { resolveImageUrl } from '@/utils/image'

const BASE_URL = API_BASE_URL

// User list
const users = ref<ChatUserVO[]>([])
const userLoading = ref(false)
const searchKey = ref('')
const activeUserId = ref<number | null>(null)
const activeUser = computed(() => users.value.find(u => u.userId === activeUserId.value))

const filteredUsers = computed(() => {
  if (!searchKey.value) return users.value
  const key = searchKey.value.toLowerCase()
  return users.value.filter(u =>
    (u.nickname || '').toLowerCase().includes(key) ||
    String(u.userId).includes(key)
  )
})

async function loadUsers() {
  userLoading.value = true
  try {
    const data = await adminChatApi.users({ page: 1, size: 100 })
    users.value = data.list
  } catch (e) {
    console.warn('加载用户列表失败', e)
  } finally {
    userLoading.value = false
  }
}

function selectUser(user: ChatUserVO) {
  activeUserId.value = user.userId
  // 清除该用户的未读
  user.unreadCount = 0
}

// Messages
const messages = ref<ChatMessageVO[]>([])
const msgLoading = ref(false)
const inputText = ref('')
const router = useRouter()
const msgListRef = ref<HTMLElement | null>(null)

async function loadMessages() {
  if (!activeUserId.value) return
  msgLoading.value = true
  try {
    const data = await adminChatApi.messages(activeUserId.value, { page: 1, size: 200 })
    messages.value = data.list
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.warn('加载消息失败', e)
  } finally {
    msgLoading.value = false
  }
}

watch(activeUserId, () => {
  messages.value = []
  loadMessages()
})

async function handleSend() {
  if (!activeUserId.value) return
  const text = inputText.value.trim()
  const img = pendingImage.value

  // 发送图片
  if (img) {
    try {
      const msg = await adminChatApi.reply(activeUserId.value, {
        msgType: 'image',
        images: [img],
      })
      messages.value.push(msg)
      pendingImage.value = ''
      await nextTick()
      scrollToBottom()
      const user = users.value.find(u => u.userId === activeUserId.value)
      if (user) {
        user.lastMessage = '[图片]'
        user.lastTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
      }
    } catch (e) {
      console.warn('发送失败', e)
    }
  }

  // 发送文字
  if (text) {
    try {
      const msg = await adminChatApi.reply(activeUserId.value, {
        msgType: 'text',
        content: text,
      })
      messages.value.push(msg)
      inputText.value = ''
      await nextTick()
      scrollToBottom()
      const user = users.value.find(u => u.userId === activeUserId.value)
      if (user) {
        user.lastMessage = text
        user.lastTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
      }
    } catch (e) {
      console.warn('发送失败', e)
    }
  }
}

function scrollToBottom() {
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

function previewImage(img: string) {
  previewSrc.value = resolveImageUrl(img)
  showPreview.value = true
}

function goToProduct(productId: number) {
  router.push(`/goods/edit/${productId}`)
}

// Image upload
const pendingImage = ref('')
const uploading = ref(false)

async function handleImageUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await fetch(`${BASE_URL}/api/u/file/upload`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useAdminStore().token}` },
      body: formData,
    })
    const data = await res.json()
    if (data.code === 0 && data.data) {
      pendingImage.value = data.data
    } else {
      console.warn('上传失败', data.msg)
    }
  } catch (e) {
    console.warn('上传失败', e)
  } finally {
    uploading.value = false
    ;(e.target as HTMLInputElement).value = ''
  }
}

// Image preview overlay
const showPreview = ref(false)
const previewSrc = ref('')

function formatTime(s?: string) {
  if (!s) return ''
  const d = new Date(s.replace(' ', 'T'))
  if (isNaN(d.getTime())) return s
  const diff = Date.now() - d.getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min}分钟前`
  const h = Math.floor(min / 60)
  if (h < 24) return `${h}小时前`
  const day = Math.floor(h / 24)
  if (day < 30) return `${day}天前`
  return s.slice(0, 10)
}

// Polling
let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadUsers()
  pollTimer = setInterval(() => {
    loadUsers()
    if (activeUserId.value) loadMessages()
  }, 5000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>
