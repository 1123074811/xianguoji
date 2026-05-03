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
        <a class="flex items-center gap-3 px-4 py-2 text-slate-500 hover:text-green-700 transition-colors cursor-pointer">
          <span class="material-symbols-outlined" data-icon="help">help</span>
          <span class="text-xs font-medium">帮助中心</span>
        </a>
        <a class="flex items-center gap-3 px-4 py-2 text-slate-500 hover:text-red-600 transition-colors cursor-pointer">
          <span class="material-symbols-outlined" data-icon="logout">logout</span>
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
          <a class="text-green-700 border-b-2 border-green-700 h-16 flex items-center font-medium text-sm cursor-pointer">首页</a>
          <a class="text-slate-500 hover:text-green-600 transition-colors h-16 flex items-center font-medium text-sm cursor-pointer">经营分析</a>
          <a class="text-slate-500 hover:text-green-600 transition-colors h-16 flex items-center font-medium text-sm cursor-pointer">报表导出</a>
        </nav>
      </div>
      <div class="flex items-center gap-5">
        <div class="flex items-center gap-1.5 px-3 py-1 bg-green-50 text-green-700 border border-green-200 rounded-full text-xs font-bold">
          <span class="w-2 h-2 bg-green-600 rounded-full animate-pulse"></span>
          店铺营业中
        </div>
        <div class="flex items-center gap-3">
          <button class="text-slate-500 hover:text-primary transition-colors relative">
            <span class="material-symbols-outlined">notifications</span>
            <span class="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
          </button>
          <button class="text-slate-500 hover:text-primary transition-colors">
            <span class="material-symbols-outlined">mail</span>
          </button>
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
import { useRoute } from 'vue-router'

const route = useRoute()

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
</script>