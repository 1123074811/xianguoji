<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg">
      <h1 class="font-h1 text-h1 text-on-surface mb-1">帮助中心</h1>
      <p class="font-body-md text-body-md text-slate-500">查找常见问题解答、操作指南和技术支持。</p>
    </div>

    <!-- Search Bar -->
    <div class="bg-white border border-outline-variant rounded-xl p-stack-lg mb-stack-lg shadow-sm">
      <div class="relative max-w-xl mx-auto">
        <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl">search</span>
        <input class="w-full pl-12 pr-4 py-3 bg-slate-50 border border-outline-variant rounded-xl focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none text-sm" placeholder="搜索帮助文档..." type="text" v-model="searchQuery" />
      </div>
    </div>

    <!-- Quick Help Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <div v-for="card in quickHelp" :key="card.title" class="bg-white border border-outline-variant rounded-xl p-stack-md hover:shadow-md transition-shadow cursor-pointer group" @click="activeSection = card.section">
        <div class="w-10 h-10 rounded-lg flex items-center justify-center mb-3" :class="card.iconBg">
          <span class="material-symbols-outlined" :class="card.iconColor">{{ card.icon }}</span>
        </div>
        <h4 class="font-label-bold text-slate-800 group-hover:text-primary transition-colors mb-1">{{ card.title }}</h4>
        <p class="text-xs text-slate-500">{{ card.desc }}</p>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-gutter">
      <!-- FAQ Section -->
      <div class="col-span-12 lg:col-span-8">
        <div class="bg-white border border-outline-variant rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h3 class="font-h3 text-h3">常见问题</h3>
            <span class="text-xs text-slate-400">{{ filteredFaqs.length }} 个问题</span>
          </div>
          <div class="divide-y divide-slate-100">
            <div v-for="faq in filteredFaqs" :key="faq.q" class="px-6 py-4 hover:bg-slate-50 transition-colors cursor-pointer" @click="toggleFaq(faq.q)">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-primary text-lg">help</span>
                  <span class="font-label-bold text-slate-800">{{ faq.q }}</span>
                </div>
                <span class="material-symbols-outlined text-slate-400 transition-transform" :class="{ 'rotate-180': expandedFaq === faq.q }">expand_more</span>
              </div>
              <div v-if="expandedFaq === faq.q" class="mt-3 pl-9 text-sm text-slate-600 leading-relaxed">
                {{ faq.a }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar: Guides & Contact -->
      <div class="col-span-12 lg:col-span-4 space-y-gutter">
        <!-- Operation Guides -->
        <div class="bg-white border border-outline-variant rounded-xl p-stack-md shadow-sm">
          <h3 class="font-h3 text-h3 mb-stack-md flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">menu_book</span>
            操作指南
          </h3>
          <div class="space-y-2">
            <a v-for="guide in guides" :key="guide.title" class="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer group">
              <span class="material-symbols-outlined text-slate-400 group-hover:text-primary transition-colors">{{ guide.icon }}</span>
              <div class="flex-1">
                <p class="text-sm font-medium text-slate-700 group-hover:text-primary transition-colors">{{ guide.title }}</p>
                <p class="text-[10px] text-slate-400">{{ guide.time }}</p>
              </div>
              <span class="material-symbols-outlined text-slate-300 group-hover:text-primary text-sm">chevron_right</span>
            </a>
          </div>
        </div>

        <!-- Contact Support -->
        <div class="bg-primary text-white rounded-xl p-stack-md shadow-lg shadow-primary/20 relative overflow-hidden">
          <div class="absolute -right-4 -bottom-4 opacity-10">
            <span class="material-symbols-outlined text-[80px]">support_agent</span>
          </div>
          <div class="relative z-10">
            <h3 class="font-h3 text-h3 mb-2">需要更多帮助？</h3>
            <p class="text-sm text-white/80 mb-4">我们的客服团队随时为您服务</p>
            <div class="space-y-3">
              <button class="w-full py-2.5 bg-white text-primary rounded-lg font-label-bold text-sm hover:bg-white/90 transition-colors flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-lg">chat</span>
                在线客服
              </button>
              <button class="w-full py-2.5 bg-white/20 text-white rounded-lg font-label-bold text-sm hover:bg-white/30 transition-colors flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-lg">call</span>
                400-888-9999
              </button>
            </div>
          </div>
        </div>

        <!-- Feedback -->
        <div class="bg-white border border-outline-variant rounded-xl p-stack-md shadow-sm">
          <h3 class="font-h3 text-h3 mb-stack-sm flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">feedback</span>
            意见反馈
          </h3>
          <p class="text-xs text-slate-500 mb-4">您的建议将帮助我们改进产品</p>
          <textarea class="w-full h-24 p-3 bg-slate-50 border border-outline-variant rounded-lg text-sm outline-none focus:ring-1 focus:ring-primary resize-none" placeholder="请输入您的建议或问题..."></textarea>
          <button class="mt-3 w-full py-2 bg-primary text-white rounded-lg font-label-bold text-sm hover:bg-primary/90 transition-colors">提交反馈</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const searchQuery = ref('')
const activeSection = ref('')
const expandedFaq = ref('')

const quickHelp = [
  { title: '订单管理', desc: '接单、备货、发货流程', icon: 'shopping_cart', iconBg: 'bg-primary/10', iconColor: 'text-primary', section: 'orders' },
  { title: '商品上架', desc: '添加商品、设置规格', icon: 'inventory_2', iconBg: 'bg-amber-50', iconColor: 'text-amber-600', section: 'goods' },
  { title: '配送设置', desc: '配送范围、运费模板', icon: 'local_shipping', iconBg: 'bg-blue-50', iconColor: 'text-blue-600', section: 'shipping' },
  { title: '账户安全', desc: '密码修改、设备管理', icon: 'lock', iconBg: 'bg-purple-50', iconColor: 'text-purple-600', section: 'security' }
]

const faqs = [
  { q: '如何处理客户退款申请？', a: '进入订单管理页面，筛选"退款/售后"标签页，点击对应订单的"处理退款"按钮。您可以选择同意退款、部分退款或拒绝退款。处理时限为48小时，超时系统将自动同意退款。', section: 'orders' },
  { q: '商品库存预警阈值如何设置？', a: '进入商品管理页面，点击商品编辑，在"库存与价格"模块中可以设置每个SKU的库存预警阈值。当库存低于该值时，系统会自动推送通知到消息中心。', section: 'goods' },
  { q: '如何创建拼团活动？', a: '进入营销中心，点击"创建优惠券"，选择"拼团活动"类型。设置成团人数、拼团折扣和活动时间即可。拼团商品会在用户端展示拼团标签。', section: 'campaign' },
  { q: '配送范围如何调整？', a: '进入配送设置页面，在"配送服务范围"模块中，使用地图绘制工具调整配送区域。您可以设置标准配送范围和扩展配送范围，不同范围可配置不同运费。', section: 'shipping' },
  { q: '如何导出经营数据？', a: '进入报表导出页面，选择报表类型、时间范围和导出格式，点击"生成并下载"即可。您也可以使用快捷模板快速生成常用报表，或设置定时自动生成。', section: 'report' },
  { q: '忘记密码怎么办？', a: '在登录页面点击"忘记密码"，输入注册手机号获取验证码，验证后即可重置密码。如果手机号已变更，请联系客服400-888-9999进行人工验证。', section: 'security' },
  { q: '如何查看客户评价并回复？', a: '进入评价管理页面，可以按"待回复"、"已回复"、"差评"筛选。点击评价卡片上的"回复"按钮即可撰写回复。对于差评，系统会提供AI建议回复供参考。', section: 'reviews' },
  { q: '店铺状态如何切换？', a: '进入系统设置页面，在"店铺状态"模块中切换营业/休息状态。休息状态下用户端将显示"店铺休息中"，无法下单。您也可以设置定时营业时间自动切换。', section: 'settings' }
]

const filteredFaqs = computed(() => {
  let result = faqs
  if (activeSection.value) {
    result = result.filter(f => f.section === activeSection.value)
  }
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(f => f.q.toLowerCase().includes(q) || f.a.toLowerCase().includes(q))
  }
  return result
})

const toggleFaq = (q: string) => {
  expandedFaq.value = expandedFaq.value === q ? '' : q
}

const guides = [
  { title: '新手入驻指南', icon: 'rocket_launch', time: '约10分钟' },
  { title: '商品发布教程', icon: 'inventory_2', time: '约8分钟' },
  { title: '订单处理流程', icon: 'receipt_long', time: '约5分钟' },
  { title: '营销活动设置', icon: 'campaign', time: '约6分钟' },
  { title: '数据报表解读', icon: 'analytics', time: '约7分钟' }
]
</script>
