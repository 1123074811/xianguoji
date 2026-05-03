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
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" value="鲜果日记旗舰店" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">联系电话</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" value="400-888-9999" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">营业时间</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" value="08:00 - 22:00" />
              </div>
              <div class="col-span-2">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">店铺地址</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" value="加利福尼亚州 90210 有机区绿巷 45 号" />
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
                <input class="sr-only peer" type="checkbox" :checked="notif.active" />
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
                  <p class="text-xs text-slate-500">上次修改于 30 天前</p>
                </div>
              </div>
              <button class="px-4 py-1.5 border border-primary text-primary font-label-bold text-xs rounded-lg hover:bg-primary/5 transition-colors">修改</button>
            </div>
            <div class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-secondary">devices</span>
                <div>
                  <p class="font-label-bold text-slate-800">登录设备管理</p>
                  <p class="text-xs text-slate-500">当前有 2 台设备在线</p>
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
          <div class="flex items-center justify-between p-4 bg-green-50 rounded-lg border border-green-200">
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 bg-green-600 rounded-full animate-pulse"></span>
              <span class="font-label-bold text-green-700">营业中</span>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input checked class="sr-only peer" type="checkbox" />
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
            <span class="material-symbols-outlined text-primary text-3xl">restaurant</span>
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
const notifications = [
  { icon: 'shopping_cart', iconColor: 'text-primary', label: '新订单通知', desc: '有新订单时推送通知', active: true },
  { icon: 'rate_review', iconColor: 'text-tertiary', label: '评价提醒', desc: '收到新评价时通知', active: true },
  { icon: 'inventory', iconColor: 'text-amber-600', label: '库存预警', desc: '库存低于阈值时通知', active: true },
  { icon: 'payments', iconColor: 'text-error', label: '退款通知', desc: '有退款申请时通知', active: true },
  { icon: 'campaign', iconColor: 'text-secondary', label: '营销活动到期', desc: '优惠券即将过期时提醒', active: false }
]
</script>
