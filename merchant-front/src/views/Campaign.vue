<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">营销管理</h1>
        <p class="font-body-md text-body-md text-slate-500">优惠券与促销活动管理，助力销售增长。</p>
      </div>
      <router-link to="/campaign/coupon/create" class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all">
        <span class="material-symbols-outlined">add</span>
        创建优惠券
      </router-link>
    </div>

    <!-- Marketing Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-gutter mb-stack-lg">
      <div v-for="stat in stats" :key="stat.label" class="bg-white p-stack-md border border-outline-variant rounded-xl shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-center justify-between mb-stack-sm">
          <span class="font-label-bold text-label-bold text-slate-500 uppercase tracking-wider">{{ stat.label }}</span>
          <span class="material-symbols-outlined p-2 rounded-lg" :class="stat.iconClass">{{ stat.icon }}</span>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="font-h2 text-h2">{{ stat.value }}</span>
          <span v-if="stat.change" class="text-xs font-bold" :class="stat.changeColor">{{ stat.change }}</span>
        </div>
        <p class="text-xs text-slate-400 mt-2">{{ stat.desc }}</p>
      </div>
    </div>

    <!-- Coupon List Card -->
    <div class="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col">
      <!-- Tabs -->
      <div class="flex items-center px-6 border-b border-slate-100 overflow-x-auto">
        <button v-for="tab in couponTabs" :key="tab.label" @click="switchTab(tab.status)"
          class="px-6 py-4 font-label-bold border-b-2 whitespace-nowrap transition-colors"
          :class="currentStatus === tab.status ? 'border-primary text-primary' : 'border-transparent text-slate-400 hover:text-slate-600'">
          {{ tab.label }} ({{ tab.count }})
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <span class="material-symbols-outlined animate-spin text-4xl text-primary">refresh</span>
        <span class="ml-3 text-slate-500">加载中...</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="coupons.length === 0" class="flex flex-col items-center justify-center py-12">
        <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">coupon</span>
        <p class="text-slate-500 font-label-bold mb-2">暂无优惠券</p>
        <p class="text-slate-400 text-sm mb-6">点击上方按钮创建您的第一个优惠券</p>
        <router-link to="/campaign/coupon/create" class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all">
          <span class="material-symbols-outlined">add</span>
          创建优惠券
        </router-link>
      </div>

      <!-- Coupon Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">优惠券名称</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">类型</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">优惠额度</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">使用门槛</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">已领取/总量</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">有效期</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">状态</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="coupon in coupons" :key="coupon.id" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3 font-label-bold text-slate-800">{{ coupon.name }}</td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-bold" :class="getCouponTypeClass(coupon.type)">
                  {{ getCouponTypeText(coupon.type) }}
                </span>
              </td>
              <td class="px-6 py-3 text-sm font-medium text-error">{{ getDiscountDisplay(coupon) }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ getThresholdDisplay(coupon) }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ coupon.receivedCount || 0 }}/{{ coupon.total }}</td>
              <td class="px-6 py-3 text-sm text-slate-600">{{ getValidityDisplay(coupon) }}</td>
              <td class="px-6 py-3">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium" :class="getStatusClass(coupon.status)">
                  {{ getStatusText(coupon.status) }}
                </span>
              </td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 text-slate-400">
                  <Tooltip text="编辑优惠券">
                    <button @click="editCoupon(coupon)" class="p-1 hover:text-primary transition-colors">
                      <span class="material-symbols-outlined text-[20px]">edit</span>
                    </button>
                  </Tooltip>
                  <Popconfirm :title="coupon.status === 1 ? '停用优惠券' : '启用优惠券'" :message="`确定要${coupon.status === 1 ? '停用' : '启用'}优惠券「${coupon.name}」吗？${coupon.status === 1 ? '停用后用户将无法领取。' : '启用后用户可正常领取。'}`" :type="coupon.status === 1 ? 'warning' : 'info'" :confirm-text="coupon.status === 1 ? '停用' : '启用'" @confirm="doToggleCouponStatus(coupon)">
                    <Tooltip :text="coupon.status === 1 ? '停用后用户将无法领取' : '启用后用户可正常领取'">
                      <button class="p-1 hover:text-error transition-colors">
                        <span class="material-symbols-outlined text-[20px]">{{ coupon.status === 1 ? 'pause_circle' : 'play_circle' }}</span>
                      </button>
                    </Tooltip>
                  </Popconfirm>
                  <Popconfirm title="删除优惠券" :message="`确定要删除优惠券「${coupon.name}」吗？此操作不可撤销。`" type="danger" confirm-text="删除" @confirm="doDeleteCoupon(coupon)">
                    <Tooltip text="删除优惠券">
                      <button class="p-1 hover:text-error transition-colors">
                        <span class="material-symbols-outlined text-[20px]">delete</span>
                      </button>
                    </Tooltip>
                  </Popconfirm>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="!loading && coupons.length > 0" class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">
          显示第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, totalCount) }} 条，共 {{ totalCount }} 条优惠券
        </span>
        <div class="flex items-center gap-1">
          <button @click="prevPage" :disabled="currentPage <= 1" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
            <span class="material-symbols-outlined text-sm">chevron_left</span>
          </button>
          <button v-for="page in visiblePages" :key="page" @click="goToPage(page)" 
            class="w-8 h-8 flex items-center justify-center rounded font-bold text-sm transition-colors"
            :class="page === currentPage ? 'bg-primary text-on-primary shadow-sm' : 'border border-outline-variant text-slate-600 hover:bg-white'">
            {{ page }}
          </button>
          <button @click="nextPage" :disabled="currentPage >= totalPages" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
            <span class="material-symbols-outlined text-sm">chevron_right</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { adminPromoApi, type CouponStatsVO } from '@/api/modules/promo'
import type { AdminCouponVO } from '@/api/types/promo'
import Tooltip from '@/components/Tooltip.vue'
import Popconfirm from '@/components/Popconfirm.vue'
import { toast } from '@/utils/toast'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const coupons = ref<AdminCouponVO[]>([])
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const currentStatus = ref<number | null>(null) // null表示全部，1=生效中，2=已过期，3=待生效

// 统计数据
const couponStats = ref<CouponStatsVO | null>(null)
const stats = computed(() => [
  { label: '活跃优惠券', value: couponStats.value ? String(couponStats.value.activeCount) : '0', change: '+0 本周', changeColor: 'text-green-600', desc: '正在发放中', icon: 'confirmation_number', iconClass: 'text-primary bg-primary/10' },
  { label: '累计领取量', value: couponStats.value ? String(couponStats.value.totalReceived) : '0', change: '+0%', changeColor: 'text-green-600', desc: '较上月增长', icon: 'redeem', iconClass: 'text-tertiary bg-tertiary-fixed' },
  { label: '核销率', value: couponStats.value?.verifyRate || '0%', change: '+0%', changeColor: 'text-green-600', desc: '高于行业均值', icon: 'verified', iconClass: 'text-secondary bg-secondary-fixed' },
  { label: '带来营收', value: couponStats.value ? `¥${couponStats.value.couponRevenue}` : '¥0', change: '+0%', changeColor: 'text-green-600', desc: '优惠券贡献营收', icon: 'payments', iconClass: 'text-primary bg-primary/10' }
])

// 标签页配置
const couponTabs = computed(() => [
  { label: '全部', status: null, count: totalCount.value },
  { label: '生效中', status: 1, count: getStatusCount(1) },
  { label: '已过期', status: 2, count: getStatusCount(2) },
  { label: '待生效', status: 3, count: getStatusCount(3) }
])

// 分页相关
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))
const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 4)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

// 获取各状态的优惠券数量
const getStatusCount = (status: number) => {
  return coupons.value.filter(c => c.status === status).length
}

// 加载优惠券列表
const loadCoupons = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (currentStatus.value !== null) {
      params.status = currentStatus.value
    }
    
    // 注意：后端返回的是简单的列表，不是分页结构
    const data = await adminPromoApi.couponPage(params)
    coupons.value = data.list || data || []
    totalCount.value = data.total || coupons.value.length
    
    // 加载统计数据
    loadCouponStats()
  } catch (error) {
    console.error('加载优惠券列表失败:', error)
    coupons.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

// 加载优惠券统计
const loadCouponStats = async () => {
  try {
    couponStats.value = await adminPromoApi.couponStats()
  } catch (e) {
    console.warn('加载优惠券统计失败', e)
  }
}

// 切换标签页
const switchTab = (status: number | null) => {
  currentStatus.value = status
  currentPage.value = 1
  loadCoupons()
}

// 分页操作
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadCoupons()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadCoupons()
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
  loadCoupons()
}

// 编辑优惠券
const editCoupon = (coupon: AdminCouponVO) => {
  router.push(`/campaign/coupon/edit/${coupon.id}`)
}

// 切换优惠券状态
const doToggleCouponStatus = async (coupon: AdminCouponVO) => {
  const newStatus = coupon.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  
  try {
    await adminPromoApi.updateCoupon(coupon.id, { status: newStatus })
    coupon.status = newStatus
    updateStats()
    toast.success(`优惠券已${action}`)
  } catch (error) {
    console.error(`${action}优惠券失败:`, error)
    toast.error(`${action}失败，请稍后重试`)
  }
}

// 删除优惠券
const doDeleteCoupon = async (coupon: AdminCouponVO) => {
  try {
    await adminPromoApi.deleteCoupon(coupon.id)
    loadCoupons()
    toast.success('优惠券已删除')
  } catch (error) {
    console.error('删除优惠券失败:', error)
    toast.error('删除失败，请稍后重试')
  }
}

// 辅助函数
const getCouponTypeText = (type: number) => {
  return type === 1 ? '满减券' : '折扣券'
}

const getCouponTypeClass = (type: number) => {
  return type === 1 
    ? 'bg-primary-fixed text-on-primary-fixed-variant' 
    : 'bg-tertiary-fixed text-on-tertiary-fixed-variant'
}

const getDiscountDisplay = (coupon: AdminCouponVO) => {
  if (coupon.type === 1) {
    return `¥${coupon.amount}`
  } else {
    return `${coupon.amount}折`
  }
}

const getThresholdDisplay = (coupon: AdminCouponVO) => {
  const minAmount = parseFloat(coupon.minAmount || '0')
  return minAmount > 0 ? `满¥${minAmount}可用` : '无门槛'
}

const getValidityDisplay = (coupon: AdminCouponVO) => {
  const start = new Date(coupon.startTime).toLocaleDateString()
  const end = new Date(coupon.endTime).toLocaleDateString()
  return `${start} - ${end}`
}

const getStatusText = (status: number) => {
  const statusMap = {
    1: '生效中',
    2: '已过期', 
    3: '待生效'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap = {
    1: 'bg-primary-fixed text-on-primary-fixed-variant border border-primary/20',
    2: 'bg-surface-variant text-on-surface-variant border border-outline-variant',
    3: 'bg-secondary-fixed text-on-secondary-fixed-variant border border-secondary/20'
  }
  return classMap[status] || 'bg-surface-variant text-on-surface-variant'
}

// 监听状态变化
watch(currentStatus, () => {
  currentPage.value = 1
})

// 组件挂载时加载数据
onMounted(() => {
  loadCoupons()
})
</script>
