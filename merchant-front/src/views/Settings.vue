<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg">
      <h1 class="font-h1 text-h1 text-on-surface mb-1">系统设置</h1>
      <p class="font-body-md text-body-md text-slate-500">管理店铺基本信息、通知偏好和安全设置。</p>
    </div>

    <div class="grid grid-cols-12 gap-gutter">
      <!-- Left: Settings Sections -->
      <div class="col-span-12 lg:col-span-8 space-y-gutter">
        <!-- Shop Info -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-h3 text-h3 text-slate-900">店铺信息</h2>
          </div>
          <div class="p-6 space-y-6">
            <div class="grid grid-cols-2 gap-stack-lg">
              <div class="col-span-2">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">店铺名称</label>
                <input v-model="shop.name" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">联系电话</label>
                <input v-model="shop.phone" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">营业时间</label>
                <input v-model="shop.businessHours" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" />
              </div>
              <div class="col-span-2">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">店铺地址</label>
                <input v-model="shop.address" class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" />
              </div>
              <div class="col-span-2">
                <button @click="saveShop" :disabled="saving" class="px-6 py-2.5 bg-primary text-white font-label-bold rounded-lg hover:bg-primary/90 transition-colors disabled:opacity-50">
                  {{ saving ? '保存中...' : '保存修改' }}
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- Notification Settings -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-h3 text-h3 text-slate-900">通知设置</h2>
          </div>
          <div class="p-6 space-y-4">
            <div v-for="notif in notifications" :key="notif.label" class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined" :class="notif.iconColor">{{ notif.icon }}</span>
                <div>
                  <p class="font-label-bold text-slate-800">{{ notif.label }}</p>
                  <p class="text-xs text-slate-500">{{ notif.desc }}</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input class="sr-only peer" type="checkbox" :checked="notif.active" @change="notif.setting && toggleNotifySetting(notif.setting)" />
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
              </label>
            </div>
          </div>
        </section>

        <!-- Security Settings -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-h3 text-h3 text-slate-900">安全设置</h2>
          </div>
          <div class="p-6 space-y-4">
            <div class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">lock</span>
                <div>
                  <p class="font-label-bold text-slate-800">修改密码</p>
                  <p class="text-xs text-slate-500">{{ passwordChangedDesc }}</p>
                </div>
              </div>
              <button class="px-4 py-1.5 border border-primary text-primary font-label-bold text-xs rounded-lg hover:bg-primary/5 transition-colors">修改</button>
            </div>
            <div class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-secondary">devices</span>
                <div>
                  <p class="font-label-bold text-slate-800">登录设备管理</p>
                  <p class="text-xs text-slate-500">上次登录 {{ lastLoginDesc || '暂无记录' }}</p>
                </div>
              </div>
              <button class="px-4 py-1.5 border border-primary text-primary font-label-bold text-xs rounded-lg hover:bg-primary/5 transition-colors">管理</button>
            </div>
          </div>
        </section>
      </div>

      <!-- Right: Quick Actions -->
      <div class="col-span-12 lg:col-span-4 space-y-gutter">
        <!-- Shop Status -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <h3 class="font-h3 text-h3 mb-6 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">store</span>
            店铺状态
          </h3>
          <div class="flex items-center justify-between p-4 rounded-lg border" :class="shop.isOpen === 1 ? 'bg-green-50 border-green-200' : 'bg-slate-50 border-slate-200'">
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full animate-pulse" :class="shop.isOpen === 1 ? 'bg-green-600' : 'bg-slate-400'"></span>
              <span class="font-label-bold" :class="shop.isOpen === 1 ? 'text-green-700' : 'text-slate-500'">{{ shop.isOpen === 1 ? '营业中' : '已打烊' }}</span>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input class="sr-only peer" type="checkbox" :checked="shop.isOpen === 1" @change="toggleOpenStatus" />
              <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
        </section>

        <!-- Quick Links -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <h3 class="font-h3 text-h3 mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">link</span>
            快捷入口
          </h3>
          <div class="space-y-2">
            <a class="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer">
              <span class="material-symbols-outlined text-slate-500">description</span>
              <span class="text-sm font-medium text-slate-700">经营数据报表</span>
              <span class="material-symbols-outlined text-slate-300 ml-auto">chevron_right</span>
            </a>
            <a class="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer">
              <span class="material-symbols-outlined text-slate-500">help</span>
              <span class="text-sm font-medium text-slate-700">帮助文档</span>
              <span class="material-symbols-outlined text-slate-300 ml-auto">chevron_right</span>
            </a>
            <a class="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer">
              <span class="material-symbols-outlined text-slate-500">feedback</span>
              <span class="text-sm font-medium text-slate-700">意见反馈</span>
              <span class="material-symbols-outlined text-slate-300 ml-auto">chevron_right</span>
            </a>
          </div>
        </section>

        <!-- Version Info -->
        <section class="bg-surface-container rounded-xl p-6">
          <div class="text-center">
            <img src="/images/logo.png" alt="鲜果记" class="w-12 h-12 rounded-lg object-cover mx-auto" />
            <h4 class="font-label-bold text-slate-700 mt-2">鲜果记 · 商家版</h4>
            <p class="text-xs text-slate-400 mt-1">v1.0.0</p>
            <p class="text-xs text-slate-400 mt-1">© 2024 鲜果日记</p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { adminShopApi } from '@/api/modules/shop'
import { staffApi, type StaffSecurityVO } from '@/api/modules/staff'
import type { AdminShopVO, NotifySettingVO } from '@/api/types/shop'
import { toast } from '@/utils/toast'

const shop = ref<Partial<AdminShopVO>>({})
const notifySettings = ref<NotifySettingVO[]>([])
const securityInfo = ref<StaffSecurityVO | null>(null)
const saving = ref(false)

const passwordChangedDesc = computed(() => {
  if (!securityInfo.value?.passwordChangedAt) return '从未修改'
  const d = new Date(securityInfo.value.passwordChangedAt)
  const diff = Math.floor((Date.now() - d.getTime()) / 86400000)
  if (diff === 0) return '今天修改'
  if (diff < 30) return `${diff} 天前修改`
  if (diff < 365) return `${Math.floor(diff / 30)} 个月前修改`
  return `${Math.floor(diff / 365)} 年前修改`
})

const lastLoginDesc = computed(() => {
  if (!securityInfo.value?.lastLoginAt) return ''
  const d = new Date(securityInfo.value.lastLoginAt)
  const diff = Math.floor((Date.now() - d.getTime()) / 86400000)
  if (diff === 0) return '今天'
  if (diff === 1) return '昨天'
  if (diff < 7) return `${diff} 天前`
  return d.toLocaleDateString()
})

async function loadShop() {
  try {
    shop.value = await adminShopApi.shopInfo()
  } catch (e) {
    console.warn('加载店铺信息失败', e)
  }
}

async function loadNotifySettings() {
  try {
    notifySettings.value = await adminShopApi.notifySettingList()
  } catch (e) {
    console.warn('加载通知设置失败', e)
  }
}

async function saveShop() {
  saving.value = true
  try {
    await adminShopApi.updateShopInfo(shop.value)
    toast.success('店铺信息已保存')
  } catch (e) {
    console.warn('保存店铺信息失败', e)
    toast.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function toggleOpenStatus() {
  const next = shop.value.isOpen === 1 ? 0 : 1
  shop.value.isOpen = next
  try {
    await adminShopApi.updateOpenStatus(next)
    toast.success(next === 1 ? '已开启营业' : '已暂停营业')
  } catch (e) {
    console.warn('切换营业状态失败', e)
    shop.value.isOpen = next === 1 ? 0 : 1
    toast.error('切换营业状态失败')
  }
}

async function toggleNotifySetting(item: NotifySettingVO) {
  item.enableApp = item.enableApp === 1 ? 0 : 1
  try {
    await adminShopApi.updateNotifySetting(item.id, { enableApp: item.enableApp })
  } catch (e) {
    console.warn('更新通知设置失败', e)
  }
}

const notifications = ref<{ icon: string; iconColor: string; label: string; desc: string; get active(): boolean; setting?: NotifySettingVO }[]>([])

function buildNotificationList() {
  const iconMap: Record<string, { icon: string; iconColor: string; label: string; desc: string }> = {
    new_order: { icon: 'shopping_cart', iconColor: 'text-primary', label: '新订单通知', desc: '有新订单时推送通知' },
    refund: { icon: 'payments', iconColor: 'text-error', label: '退款通知', desc: '有退款申请时通知' },
    stock_warn: { icon: 'inventory', iconColor: 'text-amber-600', label: '库存预警', desc: '库存低于阈值时通知' },
    review: { icon: 'rate_review', iconColor: 'text-tertiary', label: '评价提醒', desc: '收到新评价时通知' },
    promo: { icon: 'campaign', iconColor: 'text-secondary', label: '营销活动到期', desc: '优惠券即将过期时提醒' },
  }
  notifications.value = notifySettings.value.map(ns => {
    const cfg = iconMap[ns.eventKey] || { icon: 'info', iconColor: 'text-slate-600', label: ns.eventName, desc: '' }
    return { ...cfg, get active() { return ns.enableApp === 1 }, setting: ns }
  })
  // Add missing items that don't exist in DB yet
  const existingKeys = new Set(notifySettings.value.map(n => n.eventKey))
  for (const [key, cfg] of Object.entries(iconMap)) {
    if (!existingKeys.has(key)) {
      notifications.value.push({ ...cfg, active: false })
    }
  }
}

async function loadSecurityInfo() {
  try {
    securityInfo.value = await staffApi.securityInfo()
  } catch (e) {
    console.warn('加载安全信息失败', e)
  }
}

onMounted(async () => {
  await Promise.all([loadShop(), loadNotifySettings(), loadSecurityInfo()])
  buildNotificationList()
})
</script>
