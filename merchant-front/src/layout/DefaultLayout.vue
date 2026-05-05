<template>
  <div class="min-h-screen bg-surface font-body-md text-on-surface antialiased">
    <!-- SideNavBar -->
    <aside class="fixed left-0 top-0 h-screen w-60 border-r border-slate-200 bg-white flex flex-col z-50 overflow-y-auto">
      <div class="p-6">
        <h1 class="text-lg font-black text-green-800 uppercase tracking-tight">鲜果日记</h1>
        <p class="text-xs text-slate-500 font-medium uppercase tracking-wider mt-1">商家管理后台</p>
      </div>
      <nav class="flex-1 space-y-1">
        <router-link v-for="menu in menus" :key="menu.path" :to="menu.path"
          class="flex items-center gap-3 px-4 py-3 border-l-4 hover:bg-slate-50 transition-all duration-150 cursor-pointer active:scale-95"
          :class="isActive(menu.path) ? 'border-green-700 bg-green-50 text-green-700 font-bold' : 'border-transparent text-slate-600'">
          <span class="material-symbols-outlined" :style="isActive(menu.path) ? 'font-variation-settings: \'FILL\' 1, \'wght\' 400, \'GRAD\' 0, \'opsz\' 24' : ''">{{ menu.icon }}</span>
          <span class="font-label-bold text-label-bold">{{ menu.name }}</span>
        </router-link>
      </nav>
      <div class="mt-auto border-t border-slate-200 p-4 space-y-1">
        <router-link to="/help" class="flex items-center gap-3 px-4 py-2 text-slate-500 hover:text-green-700 transition-colors cursor-pointer">
          <span class="material-symbols-outlined">help</span>
          <span class="text-xs font-medium">帮助中心</span>
        </router-link>
        <a class="flex items-center gap-3 px-4 py-2 text-slate-500 hover:text-red-600 transition-colors cursor-pointer" @click="handleLogout">
          <span class="material-symbols-outlined">logout</span>
          <span class="text-xs font-medium">退出登录</span>
        </a>
      </div>
    </aside>

    <!-- TopNavBar -->
    <header class="fixed top-0 right-0 left-60 h-16 border-b border-slate-200 bg-white/80 backdrop-blur-md z-40 flex items-center justify-between px-6">
      <div class="flex items-center gap-8">
        <div class="relative w-72">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
          <input class="w-full pl-10 pr-4 py-1.5 bg-slate-100 border-transparent rounded-full text-xs focus:ring-1 focus:ring-primary focus:bg-white transition-all outline-none" placeholder="搜索订单、商品..." type="text" />
        </div>
        <nav class="hidden lg:flex items-center gap-6">
          <router-link to="/dashboard" class="h-16 flex items-center font-medium text-sm transition-colors" :class="route.path === '/dashboard' ? 'text-green-700 border-b-2 border-green-700' : 'text-slate-500 hover:text-green-600'">首页</router-link>
          <router-link to="/analysis" class="h-16 flex items-center font-medium text-sm transition-colors" :class="route.path.startsWith('/analysis') ? 'text-green-700 border-b-2 border-green-700' : 'text-slate-500 hover:text-green-600'">经营分析</router-link>
          <router-link to="/reports" class="h-16 flex items-center font-medium text-sm transition-colors" :class="route.path.startsWith('/reports') ? 'text-green-700 border-b-2 border-green-700' : 'text-slate-500 hover:text-green-600'">报表导出</router-link>
        </nav>
      </div>
      <div class="flex items-center gap-5">
        <button @click="toggleOpenStatus" class="flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold border transition-all cursor-pointer select-none" :class="isOpen ? 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100' : 'bg-slate-100 text-slate-500 border-slate-200 hover:bg-slate-200'">
          <span class="w-2 h-2 rounded-full" :class="isOpen ? 'bg-green-600 animate-pulse' : 'bg-slate-400'"></span>
          {{ isOpen ? '营业中' : '已打烊' }}
        </button>
        <div class="flex items-center gap-3">
          <router-link to="/messages" class="text-slate-500 hover:text-primary transition-colors relative" @click="resetUnread">
            <span class="material-symbols-outlined">notifications</span>
            <span v-if="unreadCount > 0" class="absolute -top-1 -right-1 min-w-[18px] h-[18px] flex items-center justify-center bg-red-500 rounded-full border-2 border-white text-white text-[10px] font-bold leading-none px-1">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
          <router-link to="/messages" class="text-slate-500 hover:text-primary transition-colors">
            <span class="material-symbols-outlined">mail</span>
          </router-link>
          <div class="w-8 h-8 rounded-full overflow-hidden border border-slate-200 bg-slate-100">
            <img alt="商家管理员" class="w-full h-full object-cover" src="https://ui-avatars.com/api/?name=张&background=2e7d32&color=fff" />
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="ml-60 pt-16 min-h-screen bg-surface p-container-padding-pc">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminShopApi } from '@/api/modules/shop'
import { toast } from '@/utils/toast'
import { useWebSocket } from '@/composables/useWebSocket'

const route = useRoute()
const router = useRouter()

const { connected, lastMessage, unreadCount, resetUnread } = useWebSocket()

const isOpen = ref(true)

async function loadOpenStatus() {
  try {
    const shop = await adminShopApi.shopInfo()
    isOpen.value = shop.isOpen === 1
  } catch (e) {
    console.warn('加载店铺状态失败', e)
  }
}

async function toggleOpenStatus() {
  const next = isOpen.value ? 0 : 1
  isOpen.value = !isOpen.value
  try {
    await adminShopApi.updateOpenStatus(next)
    toast.success(next === 1 ? '已开启营业' : '已暂停营业')
  } catch (e) {
    console.warn('切换营业状态失败', e)
    isOpen.value = !isOpen.value
    toast.error('切换营业状态失败')
  }
}

const menus = [
  { name: '控制面板', path: '/dashboard', icon: 'dashboard' },
  { name: '订单管理', path: '/orders', icon: 'shopping_cart' },
  { name: '商品管理', path: '/goods', icon: 'inventory_2' },
  { name: '营销中心', path: '/campaign', icon: 'campaign' },
  { name: '评价管理', path: '/reviews', icon: 'rate_review' },
  { name: '客户管理', path: '/customers', icon: 'group' },
  { name: '配送设置', path: '/shipping', icon: 'local_shipping' },
  { name: '系统设置', path: '/settings', icon: 'settings' }
]

const isActive = (path: string) => {
  return route.path.startsWith(path)
}

const handleLogout = () => {
  router.push('/login')
}

watch(lastMessage, (msg) => {
  if (!msg) return
  const typeMap: Record<string, 'success' | 'warning' | 'info'> = {
    NEW_ORDER: 'success',
    REFUND_APPLY: 'warning',
    REMIND_SHIP: 'info',
  }
  const t = typeMap[msg.type] || 'info'
  toast[t](msg.content, 5000)
})

onMounted(loadOpenStatus)
</script>