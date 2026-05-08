<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div class="flex items-center gap-6">
        <div>
          <h1 class="font-h1 text-h1 text-on-surface mb-1">营销管理</h1>
          <p class="font-body-md text-body-md text-slate-500">优惠券与促销活动管理，助力销售增长。</p>
        </div>
        <el-segmented v-model="activeSection" :options="sectionOptions" size="default" />
      </div>
      <router-link v-if="activeSection === 'coupon'" to="/campaign/coupon/create" class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all">
        <span class="material-symbols-outlined">add</span>
        创建优惠券
      </router-link>
    </div>

    <!-- ===== 优惠券视图 ===== -->
    <template v-if="activeSection === 'coupon'">
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
    </template>

    <!-- ===== 拼团视图 ===== -->
    <template v-if="activeSection === 'groupBuy'">
    <!-- Group-Buy Stats -->
    <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-stack-md mb-stack-lg">
      <div class="flex items-center justify-between mb-stack-sm">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-primary">groups</span>
          <h2 class="font-h3 text-h3 text-on-surface">拼团数据</h2>
        </div>
        <span class="text-xs text-slate-400">含全部活动维度统计</span>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-5 gap-gutter">
        <div>
          <div class="text-xs text-slate-500 mb-1">活跃活动</div>
          <div class="text-lg font-bold">{{ groupBuyStats?.activeActivityCount ?? 0 }}<span class="text-xs text-slate-400 font-normal"> / {{ groupBuyStats?.totalActivity ?? 0 }}</span></div>
        </div>
        <div>
          <div class="text-xs text-slate-500 mb-1">进行中团</div>
          <div class="text-lg font-bold">{{ groupBuyStats?.instanceOngoing ?? 0 }}</div>
        </div>
        <div>
          <div class="text-xs text-slate-500 mb-1">成团数</div>
          <div class="text-lg font-bold text-green-600">{{ groupBuyStats?.instanceSuccess ?? 0 }}</div>
        </div>
        <div>
          <div class="text-xs text-slate-500 mb-1">成团率</div>
          <div class="text-lg font-bold">{{ groupBuyStats?.successRate || '0%' }}</div>
        </div>
        <div>
          <div class="text-xs text-slate-500 mb-1">拼团营收</div>
          <div class="text-lg font-bold text-primary">¥{{ groupBuyStats?.revenue ?? 0 }}</div>
        </div>
      </div>
    </div>

    <!-- Group-Buy Activity Table -->
    <div class="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col mb-stack-lg">
      <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-primary">list_alt</span>
          <h2 class="font-h3 text-h3 text-on-surface">拼团活动</h2>
        </div>
        <div class="flex items-center gap-2">
          <button v-for="gbTab in groupBuyTabs" :key="gbTab.label" @click="switchGroupBuyTab(gbTab.status)"
            class="px-3 py-1 text-xs font-label-bold rounded-full transition-colors"
            :class="gbCurrentStatus === gbTab.status ? 'bg-primary text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'">
            {{ gbTab.label }}
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="gbLoading" class="flex items-center justify-center py-12">
        <span class="material-symbols-outlined animate-spin text-4xl text-primary">refresh</span>
        <span class="ml-3 text-slate-500">加载中...</span>
      </div>

      <!-- Empty -->
      <div v-else-if="groupBuyList.length === 0" class="flex flex-col items-center justify-center py-12">
        <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">groups</span>
        <p class="text-slate-500 font-label-bold mb-2">暂无拼团活动</p>
        <p class="text-slate-400 text-sm">在商品编辑页中启用拼团设置即可创建</p>
      </div>

      <!-- Table -->
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-slate-50 border-b border-slate-200">
            <tr>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">商品</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">拼团价</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-center">成团人数</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-center">参团人数</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-center">进行中</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-center">已成团</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-center">已失败</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">活动时间</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-center">状态</th>
              <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="gb in groupBuyList" :key="gb.id" class="hover:bg-slate-50 transition-colors h-[48px]">
              <td class="px-6 py-3">
                <div class="flex items-center gap-3">
                  <img v-if="gb.mainImage" :src="gb.mainImage" class="w-9 h-9 rounded-lg object-cover border border-slate-100" />
                  <span class="font-label-bold text-slate-800 truncate max-w-[160px]">{{ gb.productName || `商品#${gb.productId}` }}</span>
                </div>
              </td>
              <td class="px-6 py-3">
                <span class="text-error font-bold">¥{{ gb.groupPrice }}</span>
                <span v-if="gb.originalPrice" class="text-xs text-slate-400 line-through ml-1">¥{{ gb.originalPrice }}</span>
              </td>
              <td class="px-6 py-3 text-center font-label-bold">{{ gb.groupSize }}人</td>
              <td class="px-6 py-3 text-center font-label-bold">{{ gb.totalJoinCount ?? 0 }}</td>
              <td class="px-6 py-3 text-center">
                <span v-if="(gb.ongoingCount ?? 0) > 0" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-orange-50 text-orange-700 border border-orange-200">{{ gb.ongoingCount }}</span>
                <span v-else class="text-slate-400">0</span>
              </td>
              <td class="px-6 py-3 text-center">
                <span v-if="(gb.instanceSuccessCount ?? 0) > 0" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-50 text-green-700 border border-green-200">{{ gb.instanceSuccessCount }}</span>
                <span v-else class="text-slate-400">0</span>
              </td>
              <td class="px-6 py-3 text-center">
                <span v-if="(gb.instanceFailedCount ?? 0) > 0" class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-600 border border-red-200">{{ gb.instanceFailedCount }}</span>
                <span v-else class="text-slate-400">0</span>
              </td>
              <td class="px-6 py-3 text-sm text-slate-600">
                <div>{{ formatDate(gb.startTime) }}</div>
                <div class="text-xs text-slate-400">至 {{ formatDate(gb.endTime) }}</div>
              </td>
              <td class="px-6 py-3 text-center">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border" :class="gbStatusClass(gb.status)">{{ gbStatusText(gb.status) }}</span>
              </td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 text-slate-400">
                  <Popconfirm v-if="gb.status === 1" title="结束拼团" :message="`确定要结束拼团活动「${gb.productName || '#' + gb.productId}」吗？进行中的团不受影响。`" type="warning" confirm-text="结束" @confirm="doEndGroupBuy(gb)">
                    <Tooltip text="结束活动">
                      <button class="p-1 hover:text-amber-500 transition-colors">
                        <span class="material-symbols-outlined text-[20px]">stop_circle</span>
                      </button>
                    </Tooltip>
                  </Popconfirm>
                  <Popconfirm title="删除拼团" :message="`确定要删除拼团活动「${gb.productName || '#' + gb.productId}」吗？此操作不可撤销。`" type="danger" confirm-text="删除" @confirm="doDeleteGroupBuy(gb)">
                    <Tooltip text="删除活动">
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
      <div v-if="!gbLoading && groupBuyList.length > 0" class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low">
        <span class="text-body-sm text-slate-500">
          共 {{ gbTotal }} 条活动
        </span>
        <div class="flex items-center gap-1">
          <button @click="gbPage > 1 && (gbPage--, loadGroupBuyList())" :disabled="gbPage <= 1" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
            <span class="material-symbols-outlined text-sm">chevron_left</span>
          </button>
          <span class="px-2 text-sm text-slate-500">{{ gbPage }} / {{ gbTotalPages }}</span>
          <button @click="gbPage < gbTotalPages && (gbPage++, loadGroupBuyList())" :disabled="gbPage >= gbTotalPages" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
            <span class="material-symbols-outlined text-sm">chevron_right</span>
          </button>
        </div>
      </div>
    </div>

    </template>

    <!-- Coupon List Card -->
    <div v-if="activeSection === 'coupon'" class="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col">
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
import { ElSegmented } from 'element-plus'
import { adminPromoApi, type CouponStatsVO, type GroupBuyStatsVO } from '@/api/modules/promo'
import type { AdminCouponVO, AdminGroupBuyVO } from '@/api/types/promo'
import Tooltip from '@/components/Tooltip.vue'
import Popconfirm from '@/components/Popconfirm.vue'
import { toast } from '@/utils/toast'

const router = useRouter()

// 分段控制器
const activeSection = ref<'coupon' | 'groupBuy'>('coupon')
const sectionOptions = [
  { label: '优惠券', value: 'coupon' },
  { label: '拼团', value: 'groupBuy' },
]

// 响应式数据
const loading = ref(false)
const coupons = ref<AdminCouponVO[]>([])
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const currentStatus = ref<number | null>(null) // null表示全部，1=生效中，2=已过期，3=待生效
const statusCounts = ref<Record<string, number>>({})

// 统计数据
const couponStats = ref<CouponStatsVO | null>(null)
const groupBuyStats = ref<GroupBuyStatsVO | null>(null)
const loadGroupBuyStats = async () => {
  try { groupBuyStats.value = await adminPromoApi.groupBuyStats() }
  catch (e) { console.warn('加载拼团统计失败', e) }
}

// ===== 拼团活动列表 =====
const groupBuyList = ref<AdminGroupBuyVO[]>([])
const gbLoading = ref(false)
const gbTotal = ref(0)
const gbPage = ref(1)
const gbPageSize = 10
const gbCurrentStatus = ref<number | null>(null)
const gbTotalPages = computed(() => Math.max(1, Math.ceil(gbTotal.value / gbPageSize)))

const groupBuyTabs = [
  { label: '全部', status: null },
  { label: '进行中', status: 1 },
  { label: '已结束', status: 0 },
]

const switchGroupBuyTab = (status: number | null) => {
  gbCurrentStatus.value = status
  gbPage.value = 1
  loadGroupBuyList()
}

const loadGroupBuyList = async () => {
  gbLoading.value = true
  try {
    const params: any = { page: gbPage.value, size: gbPageSize }
    if (gbCurrentStatus.value !== null) params.status = gbCurrentStatus.value
    const data = await adminPromoApi.groupBuyPage(params)
    groupBuyList.value = data.list || []
    gbTotal.value = data.total || 0
  } catch (e) {
    console.warn('加载拼团活动失败', e)
  } finally {
    gbLoading.value = false
  }
}

const gbStatusText = (status: number) => status === 1 ? '进行中' : '已结束'
const gbStatusClass = (status: number) =>
  status === 1
    ? 'bg-green-50 text-green-700 border-green-200'
    : 'bg-slate-100 text-slate-500 border-slate-200'

const formatDate = (d?: string) => {
  if (!d) return '-'
  return new Date(d).toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const doEndGroupBuy = async (gb: AdminGroupBuyVO) => {
  try {
    await adminPromoApi.updateGroupBuy(gb.id, { status: 0 })
    loadGroupBuyList()
    loadGroupBuyStats()
    toast.success('拼团活动已结束')
  } catch (e) {
    console.error('结束拼团失败', e)
    toast.error('操作失败')
  }
}

const doDeleteGroupBuy = async (gb: AdminGroupBuyVO) => {
  try {
    await adminPromoApi.deleteGroupBuy(gb.id)
    loadGroupBuyList()
    loadGroupBuyStats()
    toast.success('拼团活动已删除')
  } catch (e) {
    console.error('删除拼团失败', e)
    toast.error('删除失败')
  }
}
const stats = computed(() => [
  { label: '活跃优惠券', value: couponStats.value ? String(couponStats.value.activeCount) : '0', change: '+0 本周', changeColor: 'text-green-600', desc: '正在发放中', icon: 'confirmation_number', iconClass: 'text-primary bg-primary/10' },
  { label: '累计领取量', value: couponStats.value ? String(couponStats.value.totalReceived) : '0', change: '+0%', changeColor: 'text-green-600', desc: '较上月增长', icon: 'redeem', iconClass: 'text-tertiary bg-tertiary-fixed' },
  { label: '核销率', value: couponStats.value?.verifyRate || '0%', change: '+0%', changeColor: 'text-green-600', desc: '高于行业均值', icon: 'verified', iconClass: 'text-secondary bg-secondary-fixed' },
  { label: '带来营收', value: couponStats.value ? `¥${couponStats.value.couponRevenue}` : '¥0', change: '+0%', changeColor: 'text-green-600', desc: '优惠券贡献营收', icon: 'payments', iconClass: 'text-primary bg-primary/10' }
])

// 标签页配置
const couponTabs = computed(() => [
  { label: '全部', status: null, count: statusCounts.value['total'] || 0 },
  { label: '生效中', status: 1, count: statusCounts.value['1'] || 0 },
  { label: '已过期', status: 2, count: statusCounts.value['2'] || 0 },
  { label: '待生效', status: 3, count: statusCounts.value['3'] || 0 }
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

// 加载各状态数量（独立于分页）
const loadStatusCounts = async () => {
  try {
    statusCounts.value = await adminPromoApi.couponStatusCounts()
  } catch (e) {
    console.warn('加载状态数量失败', e)
  }
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
    
    const data = await adminPromoApi.couponPage(params)
    coupons.value = data.list || data || []
    totalCount.value = data.total || coupons.value.length
    
    // 加载统计数据和状态数量
    loadCouponStats()
    loadStatusCounts()
    loadGroupBuyStats()
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
    loadCouponStats()
    loadStatusCounts()
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
  const statusMap: Record<number, string> = {
    1: '生效中',
    2: '已过期', 
    3: '待生效'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
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
  loadGroupBuyList()
})

// 切换视图时加载对应数据
watch(activeSection, (val) => {
  if (val === 'coupon') loadCoupons()
  else loadGroupBuyList()
})
</script>
