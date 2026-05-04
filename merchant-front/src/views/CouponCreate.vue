<template>
  <div>
    <!-- Page Header -->
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">创建优惠券</h1>
        <p class="font-body-md text-body-md text-slate-500">设置优惠券信息，吸引用户消费提升销量。</p>
      </div>
      <div class="flex gap-3">
        <button @click="handleCancel" class="px-6 py-2.5 rounded-lg border border-outline-variant text-slate-600 hover:bg-surface-variant transition-colors">
          取消
        </button>
        <button @click="handleSaveDraft" class="px-6 py-2.5 rounded-lg border border-outline-variant text-slate-600 hover:bg-surface-variant transition-colors">
          保存草稿
        </button>
        <button @click="handleSubmit" :disabled="isSubmitting" class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all disabled:opacity-50 disabled:cursor-not-allowed">
          <span v-if="isSubmitting" class="material-symbols-outlined animate-spin">refresh</span>
          <span class="material-symbols-outlined">check</span>
          {{ isSubmitting ? '创建中...' : '创建优惠券' }}
        </button>
      </div>
    </div>

    <!-- Form Content -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-gutter">
      <!-- Main Form -->
      <div class="lg:col-span-2 space-y-gutter">
        <!-- Basic Info Card -->
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-6">
          <h2 class="font-title-lg text-title-lg text-slate-800 mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">coupon</span>
            基本信息
          </h2>
          
          <div class="space-y-4">
            <!-- Coupon Name -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                优惠券名称 <span class="text-error">*</span>
              </label>
              <input 
                v-model="form.name" 
                type="text" 
                placeholder="请输入优惠券名称，如：新人首单立减"
                class="w-full px-4 py-2.5 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors"
                :class="{ 'border-error': errors.name }"
              />
              <p v-if="errors.name" class="text-error text-sm mt-1">{{ errors.name }}</p>
              <p class="text-slate-500 text-sm mt-1">建议名称简洁明了，方便用户识别</p>
            </div>

            <!-- Coupon Type -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                优惠券类型 <span class="text-error">*</span>
              </label>
              <div class="grid grid-cols-2 gap-3">
                <button 
                  v-for="type in couponTypes" 
                  :key="type.value"
                  @click="form.type = type.value"
                  class="p-4 border rounded-lg transition-all"
                  :class="form.type === type.value ? 'border-primary bg-primary/5 text-primary' : 'border-outline-variant hover:bg-surface-variant'"
                >
                  <span class="material-symbols-outlined text-2xl mb-1">{{ type.icon }}</span>
                  <div class="font-label-bold">{{ type.label }}</div>
                  <div class="text-sm text-slate-500">{{ type.desc }}</div>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Discount Settings Card -->
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-6">
          <h2 class="font-title-lg text-title-lg text-slate-800 mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">sell</span>
            优惠设置
          </h2>
          
          <div class="space-y-4">
            <!-- Discount Amount -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                {{ form.type === 1 ? '减免金额' : '折扣力度' }} <span class="text-error">*</span>
              </label>
              <div class="relative">
                <span v-if="form.type === 1" class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500">¥</span>
                <input 
                  v-model="form.amount" 
                  type="number" 
                  :placeholder="form.type === 1 ? '0.00' : '0.0'"
                  class="w-full px-4 py-2.5 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors"
                  :class="{ 'border-error': errors.amount, 'pl-8': form.type === 1 }"
                  step="0.01"
                />
                <span v-if="form.type === 2" class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500">折</span>
              </div>
              <p v-if="errors.amount" class="text-error text-sm mt-1">{{ errors.amount }}</p>
              <p class="text-slate-500 text-sm mt-1">
                {{ form.type === 1 ? '用户可享受的减免金额，需大于0' : '折扣力度，如8.5表示8.5折' }}
              </p>
            </div>

            <!-- Usage Threshold -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                使用门槛 <span class="text-error">*</span>
              </label>
              <div class="relative">
                <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500">¥</span>
                <input 
                  v-model="form.minAmount" 
                  type="number" 
                  placeholder="0.00"
                  class="w-full px-4 py-2.5 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors pl-8"
                  :class="{ 'border-error': errors.minAmount }"
                  step="0.01"
                />
              </div>
              <p v-if="errors.minAmount" class="text-error text-sm mt-1">{{ errors.minAmount }}</p>
              <p class="text-slate-500 text-sm mt-1">订单满此金额方可使用，0表示无门槛</p>
            </div>
          </div>
        </div>

        <!-- Validity Period Card -->
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-6">
          <h2 class="font-title-lg text-title-lg text-slate-800 mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">schedule</span>
            有效期设置
          </h2>
          
          <div class="space-y-4">
            <!-- Start Time -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                生效时间 <span class="text-error">*</span>
              </label>
              <input 
                v-model="form.startTime" 
                type="datetime-local" 
                class="w-full px-4 py-2.5 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors"
                :class="{ 'border-error': errors.startTime }"
              />
              <p v-if="errors.startTime" class="text-error text-sm mt-1">{{ errors.startTime }}</p>
            </div>

            <!-- End Time -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                失效时间 <span class="text-error">*</span>
              </label>
              <input 
                v-model="form.endTime" 
                type="datetime-local" 
                class="w-full px-4 py-2.5 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors"
                :class="{ 'border-error': errors.endTime }"
              />
              <p v-if="errors.endTime" class="text-error text-sm mt-1">{{ errors.endTime }}</p>
            </div>
          </div>
        </div>

        <!-- Quantity Settings Card -->
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-6">
          <h2 class="font-title-lg text-title-lg text-slate-800 mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">inventory_2</span>
            发放数量
          </h2>
          
          <div class="space-y-4">
            <!-- Total Count -->
            <div>
              <label class="block font-label-bold text-label-bold text-slate-700 mb-2">
                发放总量 <span class="text-error">*</span>
              </label>
              <input 
                v-model="form.total" 
                type="number" 
                placeholder="请输入发放总量"
                class="w-full px-4 py-2.5 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary/20 focus:border-primary transition-colors"
                :class="{ 'border-error': errors.total }"
                min="1"
              />
              <p v-if="errors.total" class="text-error text-sm mt-1">{{ errors.total }}</p>
              <p class="text-slate-500 text-sm mt-1">设置优惠券的发放总量，发完即止</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Preview Sidebar -->
      <div class="lg:col-span-1">
        <div class="bg-white border border-slate-200 rounded-xl shadow-sm p-6 sticky top-6">
          <h3 class="font-title-lg text-title-lg text-slate-800 mb-4 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">visibility</span>
            优惠券预览
          </h3>
          
          <div class="border-2 border-dashed border-slate-300 rounded-lg p-4 bg-gradient-to-br from-primary/5 to-tertiary/5">
            <div class="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
              <!-- Coupon Header -->
              <div class="bg-gradient-to-r from-primary to-primary/80 text-white p-4">
                <div class="flex justify-between items-start">
                  <div>
                    <div class="font-bold text-lg">{{ form.name || '优惠券名称' }}</div>
                    <div class="text-sm opacity-90 mt-1">{{ getCouponTypeText() }}</div>
                  </div>
                  <div class="text-right">
                    <div class="text-2xl font-bold">{{ getDiscountDisplay() }}</div>
                    <div class="text-sm opacity-90">{{ getThresholdDisplay() }}</div>
                  </div>
                </div>
              </div>
              
              <!-- Coupon Body -->
              <div class="p-4 space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-slate-500">有效期：</span>
                  <span class="text-slate-800">{{ getValidityDisplay() }}</span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-slate-500">发放量：</span>
                  <span class="text-slate-800">{{ form.total || '0' }}张</span>
                </div>
              </div>
              
              <!-- Coupon Footer -->
              <div class="bg-slate-50 px-4 py-2 border-t border-slate-200">
                <div class="text-xs text-slate-500 text-center">鲜果记 · 精品水果电商</div>
              </div>
            </div>
          </div>
          
          <div class="mt-4 p-3 bg-amber-50 border border-amber-200 rounded-lg">
            <div class="flex items-start gap-2">
              <span class="material-symbols-outlined text-amber-600 text-sm mt-0.5">tips_and_updates</span>
              <div class="text-sm text-amber-800">
                <p class="font-bold mb-1">温馨提示</p>
                <ul class="space-y-1 text-xs">
                  <li>• 优惠券创建后不可修改优惠金额</li>
                  <li>• 生效时间需早于失效时间</li>
                  <li>• 建议合理设置使用门槛</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminPromoApi } from '@/api/modules/promo'
import type { CouponCreateDto } from '@/api/types/promo'
import { toast } from '@/utils/toast'

const router = useRouter()

// Form data
const form = reactive<CouponCreateDto>({
  name: '',
  type: 1,
  amount: '',
  minAmount: '',
  startTime: '',
  endTime: '',
  total: 0
})

// Form state
const isSubmitting = ref(false)
const errors = reactive<Record<string, string>>({})

// Coupon types
const couponTypes = [
  { value: 1, label: '满减券', desc: '满足金额减免固定金额', icon: 'money_off' },
  { value: 2, label: '折扣券', desc: '按折扣比例计算价格', icon: 'percent' }
]

// Validation
const validateForm = (): boolean => {
  Object.keys(errors).forEach(key => delete errors[key])

  if (!form.name.trim()) {
    errors.name = '请输入优惠券名称'
  } else if (form.name.length > 50) {
    errors.name = '优惠券名称不能超过50个字符'
  }

  if (!form.amount) {
    errors.amount = '请输入优惠金额'
  } else if (form.type === 1 && parseFloat(form.amount) <= 0) {
    errors.amount = '减免金额必须大于0'
  } else if (form.type === 2 && (parseFloat(form.amount) <= 0 || parseFloat(form.amount) >= 10)) {
    errors.amount = '折扣力度必须在0-10之间'
  }

  if (!form.minAmount) {
    errors.minAmount = '请输入使用门槛'
  } else if (parseFloat(form.minAmount) < 0) {
    errors.minAmount = '使用门槛不能为负数'
  }

  if (!form.startTime) {
    errors.startTime = '请选择生效时间'
  }

  if (!form.endTime) {
    errors.endTime = '请选择失效时间'
  } else if (form.startTime && new Date(form.endTime) <= new Date(form.startTime)) {
    errors.endTime = '失效时间必须晚于生效时间'
  }

  if (!form.total || form.total <= 0) {
    errors.total = '发放总量必须大于0'
  } else if (form.total > 999999) {
    errors.total = '发放总量不能超过999999'
  }

  return Object.keys(errors).length === 0
}

// Submit form
const handleSubmit = async () => {
  if (!validateForm()) return

  isSubmitting.value = true
  try {
    await adminPromoApi.createCoupon(form)
    toast.success('优惠券创建成功')
    router.push('/campaign')
  } catch (error: any) {
    console.error('创建优惠券失败:', error)
    toast.error(error?.message || '创建优惠券失败')
  } finally {
    isSubmitting.value = false
  }
}

// Save draft
const handleSaveDraft = () => {
  toast.info('保存草稿功能开发中')
}

// Cancel
const handleCancel = () => {
  router.push('/campaign')
}

// Helper functions for preview
const getCouponTypeText = () => {
  const type = couponTypes.find(t => t.value === form.type)
  return type?.label || '优惠券类型'
}

const getDiscountDisplay = () => {
  if (!form.amount) return '¥0'
  return form.type === 1 ? `¥${form.amount}` : `${form.amount}折`
}

const getThresholdDisplay = () => {
  if (!form.minAmount || parseFloat(form.minAmount) === 0) return '无门槛'
  return `满¥${form.minAmount}可用`
}

const getValidityDisplay = () => {
  if (!form.startTime || !form.endTime) return '请设置有效期'
  const start = new Date(form.startTime).toLocaleDateString()
  const end = new Date(form.endTime).toLocaleDateString()
  return `${start} - ${end}`
}
</script>
