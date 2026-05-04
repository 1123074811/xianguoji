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
            <div v-if="filteredFaqs.length === 0" class="px-6 py-8 text-center text-sm text-slate-400">暂无问题</div>
            <div v-for="faq in filteredFaqs" :key="faq.id" class="px-6 py-4 hover:bg-slate-50 transition-colors cursor-pointer" @click="toggleFaq(faq.id)">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <span class="material-symbols-outlined text-primary text-lg">help</span>
                  <span class="font-label-bold text-slate-800">{{ faq.question }}</span>
                </div>
                <span class="material-symbols-outlined text-slate-400 transition-transform" :class="{ 'rotate-180': expandedFaq === faq.id }">expand_more</span>
              </div>
              <div v-if="expandedFaq === faq.id" class="mt-3 pl-9 text-sm text-slate-600 leading-relaxed">
                {{ faq.answer }}
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
            <div v-if="guides.length === 0" class="text-xs text-slate-400 px-3 py-2">暂无内容</div>
            <a v-for="guide in guides" :key="guide.id" :href="guide.url || undefined" class="flex items-center gap-3 p-3 rounded-lg hover:bg-slate-50 transition-colors cursor-pointer group">
              <span class="material-symbols-outlined text-slate-400 group-hover:text-primary transition-colors">{{ guide.icon }}</span>
              <div class="flex-1">
                <p class="text-sm font-medium text-slate-700 group-hover:text-primary transition-colors">{{ guide.title }}</p>
                <p class="text-[10px] text-slate-400">{{ guide.duration }}</p>
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
          <textarea v-model="feedbackContent" class="w-full h-24 p-3 bg-slate-50 border border-outline-variant rounded-lg text-sm outline-none focus:ring-1 focus:ring-primary resize-none" placeholder="请输入您的建议或问题..."></textarea>
          <input v-model="feedbackContact" class="mt-2 w-full px-3 py-2 bg-slate-50 border border-outline-variant rounded-lg text-sm outline-none focus:ring-1 focus:ring-primary" placeholder="联系方式（可选）" />
          <button @click="submitFeedback" :disabled="submittingFeedback" class="mt-3 w-full py-2 bg-primary text-white rounded-lg font-label-bold text-sm hover:bg-primary/90 transition-colors disabled:opacity-50">
            {{ submittingFeedback ? '提交中…' : '提交反馈' }}
          </button>
          <p v-if="feedbackTip" :class="feedbackTipClass" class="mt-2 text-xs text-center">{{ feedbackTip }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { helpApi, type HelpFaqVO, type HelpGuideVO } from '@/api/modules/help'

const searchQuery = ref('')
const activeSection = ref('')
const expandedFaq = ref<number | null>(null)
const faqs = ref<HelpFaqVO[]>([])
const guides = ref<HelpGuideVO[]>([])
const feedbackContent = ref('')
const feedbackContact = ref('')
const submittingFeedback = ref(false)
const feedbackTip = ref('')
const feedbackTipClass = ref('text-slate-500')

const quickHelp = [
  { title: '订单管理', desc: '接单、备货、发货流程', icon: 'shopping_cart', iconBg: 'bg-primary/10', iconColor: 'text-primary', section: 'orders' },
  { title: '商品上架', desc: '添加商品、设置规格', icon: 'inventory_2', iconBg: 'bg-amber-50', iconColor: 'text-amber-600', section: 'goods' },
  { title: '配送设置', desc: '配送范围、运费模板', icon: 'local_shipping', iconBg: 'bg-blue-50', iconColor: 'text-blue-600', section: 'shipping' },
  { title: '账户安全', desc: '密码修改、设备管理', icon: 'lock', iconBg: 'bg-purple-50', iconColor: 'text-purple-600', section: 'security' }
]

const filteredFaqs = computed(() => {
  let result = faqs.value
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    result = result.filter(f => f.question.toLowerCase().includes(q) || f.answer.toLowerCase().includes(q))
  }
  return result
})

// 模板里使用 faq.q / faq.a；兼容
const toggleFaq = (id: number) => {
  expandedFaq.value = expandedFaq.value === id ? null : id
}

async function loadFaqs() {
  try {
    faqs.value = await helpApi.faqList({ section: activeSection.value || undefined })
  } catch (e) {
    console.warn('加载FAQ失败', e)
  }
}

async function loadGuides() {
  try {
    guides.value = await helpApi.guideList()
  } catch (e) {
    console.warn('加载操作指南失败', e)
  }
}

watch(activeSection, () => loadFaqs())

async function submitFeedback() {
  if (!feedbackContent.value.trim()) {
    feedbackTip.value = '请填写反馈内容'
    feedbackTipClass.value = 'text-error'
    return
  }
  submittingFeedback.value = true
  try {
    await helpApi.submitFeedback({ content: feedbackContent.value, contact: feedbackContact.value || undefined })
    feedbackContent.value = ''
    feedbackContact.value = ''
    feedbackTip.value = '感谢您的反馈，我们会尽快处理'
    feedbackTipClass.value = 'text-primary'
  } catch (e: any) {
    feedbackTip.value = e?.message || '提交失败'
    feedbackTipClass.value = 'text-error'
  } finally {
    submittingFeedback.value = false
  }
}

onMounted(() => {
  loadFaqs()
  loadGuides()
})
</script>
