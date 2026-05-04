<template>
  <div>
    <!-- Breadcrumbs -->
    <nav class="flex items-center gap-2 text-xs text-slate-400 font-medium mb-4">
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/goods')">商品管理</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/goods')">所有商品</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="text-slate-600">{{ isEdit ? '编辑：' + form.name : '新增商品' }}</span>
    </nav>

    <div class="grid grid-cols-12 gap-gutter">
      <!-- Left Column: Primary Details -->
      <div class="col-span-12 lg:col-span-8 space-y-gutter">
        <!-- Basic Info Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="font-h3 text-h3 text-slate-900">基本信息</h2>
            <span class="text-xs font-medium text-slate-400">必填项以 * 标记</span>
          </div>
          <div class="p-6 space-y-6">
            <div class="grid grid-cols-2 gap-stack-lg">
              <div class="col-span-2">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">商品名称 *</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" v-model="form.name" placeholder="请输入商品名称" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">类目 *</label>
                <select class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary outline-none" v-model="form.categoryId">
                  <option :value="0" disabled>请选择类目</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">品牌</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 outline-none focus:ring-primary" type="text" v-model="form.subtitle" placeholder="副标题（选填）" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-on-surface mb-4">轮播图 (最多 6 张) *</label>
              <div class="grid grid-cols-3 sm:grid-cols-6 gap-3">
                <div v-for="(img, idx) in carouselImages" :key="'c'+idx"
                  class="aspect-square rounded-lg border border-slate-200 overflow-hidden relative group">
                  <img :src="resolveImageUrl(img)" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span class="material-symbols-outlined text-white cursor-pointer" @click="removeCarousel(idx)">delete</span>
                  </div>
                  <span v-if="idx === 0" class="absolute top-1 left-1 text-[9px] bg-primary text-white px-1.5 py-0.5 rounded font-bold">主图</span>
                </div>
                <label v-if="carouselImages.length < 6"
                  class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 hover:border-primary hover:text-primary transition-colors cursor-pointer bg-slate-50">
                  <span class="material-symbols-outlined text-2xl">add_photo_alternate</span>
                  <span class="text-[10px] mt-1 font-bold uppercase">上传图片</span>
                  <input type="file" accept="image/*" class="hidden" @change="onCarouselUpload" />
                </label>
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-on-surface mb-4">详情图</label>
              <div class="grid grid-cols-3 sm:grid-cols-6 gap-3">
                <div v-for="(img, idx) in detailImages" :key="'d'+idx"
                  class="aspect-square rounded-lg border border-slate-200 overflow-hidden relative group">
                  <img :src="resolveImageUrl(img)" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span class="material-symbols-outlined text-white cursor-pointer" @click="removeDetail(idx)">delete</span>
                  </div>
                </div>
                <label
                  class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 hover:border-primary hover:text-primary transition-colors cursor-pointer bg-slate-50">
                  <span class="material-symbols-outlined text-2xl">add_photo_alternate</span>
                  <span class="text-[10px] mt-1 font-bold uppercase">详情图</span>
                  <input type="file" accept="image/*" class="hidden" @change="onDetailUpload" />
                </label>
              </div>
            </div>
          </div>
        </section>

        <!-- Specs & Price Table Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="font-h3 text-h3 text-slate-900">价格与库存</h2>
            <button class="text-primary font-label-bold text-label-bold flex items-center gap-1" @click="addSku">
              <span class="material-symbols-outlined text-lg">add</span>
              添加规格
            </button>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead class="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">规格名称</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">销售价</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">原价</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">库存</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">默认</th>
                  <th class="px-6 py-3"></th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="(spec, idx) in skuList" :key="idx" class="hover:bg-slate-50 transition-colors h-[48px]">
                  <td class="px-6 py-2">
                    <input class="w-24 bg-transparent border-none p-0 focus:ring-0 text-sm font-medium outline-none" type="text" v-model="spec.specName" placeholder="规格名" />
                  </td>
                  <td class="px-6 py-2">
                    <div class="flex items-center gap-1">
                      <span class="text-slate-400 text-sm">¥</span>
                      <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" v-model="spec.price" />
                    </div>
                  </td>
                  <td class="px-6 py-2">
                    <div class="flex items-center gap-1">
                      <span class="text-slate-400 text-sm">¥</span>
                      <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" v-model="spec.originalPrice" />
                    </div>
                  </td>
                  <td class="px-6 py-2">
                    <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="number" v-model.number="spec.stock" />
                  </td>
                  <td class="px-6 py-2 text-center">
                    <input type="radio" :name="'defaultSku'" :checked="spec.isDefault === 1" @change="setDefaultSku(idx)" class="text-primary" />
                  </td>
                  <td class="px-6 py-2 text-right">
                    <span class="material-symbols-outlined text-slate-300 hover:text-error cursor-pointer text-xl" @click="removeSku(idx)">do_not_disturb_on</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- Product Intro Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-h3 text-h3 text-slate-900">商品详情介绍</h2>
          </div>
          <div class="p-6">
            <textarea class="w-full border border-outline-variant rounded-lg px-4 py-3 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none text-sm min-h-[200px] resize-y" v-model="form.description" placeholder="请输入商品详情介绍"></textarea>
          </div>
        </section>
      </div>

      <!-- Right Column: Settings & Toggles -->
      <div class="col-span-12 lg:col-span-4 space-y-gutter">
        <!-- Group Buy Module -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">groups</span>
              <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">拼团设置</h3>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input checked class="sr-only peer" type="checkbox" />
              <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">最低成团人数</label>
              <div class="flex items-center border border-slate-200 rounded-lg overflow-hidden">
                <button class="px-3 py-2 bg-slate-50 text-slate-500">-</button>
                <input class="flex-1 text-center border-none text-sm font-medium outline-none" type="number" value="5" />
                <button class="px-3 py-2 bg-slate-50 text-slate-500">+</button>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">拼团折扣 (%)</label>
              <input class="w-full border border-slate-200 rounded-lg px-4 py-2 text-sm outline-none" type="text" value="15" />
            </div>
          </div>
        </section>

        <!-- Delivery Settings Module -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <div class="flex items-center gap-2 mb-6">
            <span class="material-symbols-outlined text-primary">local_shipping</span>
            <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">配送选项</h3>
          </div>
          <div class="space-y-3">
            <label class="flex items-center p-3 border rounded-lg cursor-pointer transition-colors"
              :class="form.supportDelivery ? 'border-primary bg-primary/5' : 'border-slate-200 hover:bg-slate-50'">
              <input class="rounded text-primary focus:ring-primary" type="checkbox" v-model="form.supportDelivery" :true-value="1" :false-value="0" />
              <div class="ml-3">
                <p class="text-sm font-bold text-slate-900">同城配送</p>
                <p class="text-[10px] text-slate-500">通过配送员送达</p>
              </div>
            </label>
            <label class="flex items-center p-3 border rounded-lg cursor-pointer transition-colors"
              :class="form.supportPickup ? 'border-primary bg-primary/5' : 'border-slate-200 hover:bg-slate-50'">
              <input class="rounded text-primary focus:ring-primary" type="checkbox" v-model="form.supportPickup" :true-value="1" :false-value="0" />
              <div class="ml-3">
                <p class="text-sm font-bold text-slate-900">到店自提</p>
                <p class="text-[10px] text-slate-500">从商家门店地址取货</p>
              </div>
            </label>
          </div>
          <div class="mt-6 pt-6 border-t border-slate-100">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-primary text-xl">stars</span>
                <span class="text-sm font-bold text-slate-700">店长推荐</span>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input class="sr-only peer" type="checkbox" v-model="form.isRecommend" :true-value="1" :false-value="0" />
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
              </label>
            </div>
          </div>
        </section>

        <!-- Quick Stats Widget -->
        <section class="bg-gradient-to-br from-green-700 to-green-900 rounded-xl p-6 text-white shadow-lg">
          <div class="flex items-center justify-between mb-4">
            <span class="text-xs font-bold uppercase tracking-widest opacity-80">商品健康度</span>
            <span class="material-symbols-outlined opacity-80">info</span>
          </div>
          <div class="space-y-4">
            <div class="flex items-end justify-between">
              <div class="text-3xl font-black">94%</div>
              <div class="text-[10px] font-bold uppercase py-1 px-2 bg-white/20 rounded">表现优异</div>
            </div>
            <div class="w-full bg-white/20 h-1 rounded-full overflow-hidden">
              <div class="bg-white w-[94%] h-full"></div>
            </div>
            <p class="text-xs opacity-70 leading-relaxed">完善详细的产品描述可以将您的搜索可见度提升 12%。</p>
          </div>
        </section>
      </div>
    </div>

    <!-- Fixed Bottom Action Bar -->
    <footer class="fixed bottom-0 right-0 left-60 h-20 bg-white border-t border-slate-200 px-gutter flex items-center justify-between z-50">
      <div class="flex items-center gap-2 text-slate-400">
        <span class="material-symbols-outlined text-lg">history</span>
        <span class="text-xs font-medium italic" v-if="saving">保存中...</span>
      </div>
      <div class="flex items-center gap-gutter">
        <button class="px-6 py-2.5 rounded-lg border border-slate-200 text-slate-600 font-label-bold text-label-bold hover:bg-slate-50 active:scale-95 transition-all" @click="goBack">取消</button>
        <button class="px-6 py-2.5 rounded-lg border border-primary text-primary font-label-bold text-label-bold hover:bg-primary/5 active:scale-95 transition-all" :disabled="saving" @click="saveDraft">保存草稿</button>
        <button class="px-10 py-2.5 rounded-lg bg-primary text-white font-label-bold text-label-bold shadow-md hover:bg-primary-container active:scale-95 transition-all" :disabled="saving" @click="publish">发布商品</button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminCatalogApi } from '@/api/modules/catalog'
import { request } from '@/api/request'
import { resolveImageUrl } from '@/utils/image'
import type { AdminCategoryVO, AdminProductDetailVO } from '@/api/types/catalog'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const productId = computed(() => Number(route.params.id) || 0)
const loading = ref(false)
const saving = ref(false)

// ---- 表单数据 ----
const form = ref({
  name: '',
  subtitle: '',
  categoryId: 0,
  mainImage: '',
  description: '',
  videoUrl: '',
  isRecommend: 0 as 0 | 1,
  supportDelivery: 1 as 0 | 1,
  supportPickup: 1 as 0 | 1,
  status: 1 as 0 | 1,
})

const carouselImages = ref<string[]>([])
const detailImages = ref<string[]>([])

interface SkuItem {
  id?: number
  specName: string
  price: string
  originalPrice: string
  stock: number
  isDefault: 0 | 1
  _deleted?: boolean
}

const skuList = ref<SkuItem[]>([{ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 1 }])

const categories = ref<AdminCategoryVO[]>([])

// ---- 加载数据 ----
async function loadCategories() {
  try {
    categories.value = await adminCatalogApi.categoryList()
  } catch (e) { console.warn('加载分类失败', e) }
}

async function loadProduct() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data: AdminProductDetailVO = await adminCatalogApi.productDetail(productId.value)
    form.value.name = data.name
    form.value.subtitle = data.subtitle
    form.value.categoryId = data.categoryId
    form.value.mainImage = data.mainImage
    form.value.description = data.description
    form.value.videoUrl = data.videoUrl || ''
    form.value.isRecommend = data.isRecommend
    form.value.supportDelivery = data.supportDelivery ?? 1
    form.value.supportPickup = data.supportPickup ?? 1
    form.value.status = data.status
    carouselImages.value = data.carouselImages || []
    detailImages.value = data.detailImages || []
    skuList.value = (data.skuList || []).map(s => ({
      id: s.id,
      specName: s.specName,
      price: String(s.price),
      originalPrice: String(s.originalPrice),
      stock: s.stock,
      isDefault: s.isDefault,
    }))
    if (skuList.value.length === 0) {
      skuList.value.push({ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 1 })
    }
  } catch (e) { console.warn('加载商品失败', e) }
  finally { loading.value = false }
}

// ---- 图片上传 ----
async function uploadImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const url = await request<string>({
    url: '/api/admin/file/upload',
    method: 'POST',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return url
}

async function onCarouselUpload(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  for (const file of Array.from(input.files)) {
    try {
      const url = await uploadImage(file)
      carouselImages.value.push(url)
      if (!form.value.mainImage) form.value.mainImage = url
    } catch (e) { console.warn('上传失败', e) }
  }
  input.value = ''
}

async function onDetailUpload(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  for (const file of Array.from(input.files)) {
    try {
      const url = await uploadImage(file)
      detailImages.value.push(url)
    } catch (e) { console.warn('上传失败', e) }
  }
  input.value = ''
}

function removeCarousel(idx: number) {
  carouselImages.value.splice(idx, 1)
  if (form.value.mainImage === carouselImages.value[idx]) {
    form.value.mainImage = carouselImages.value[0] || ''
  }
}

function removeDetail(idx: number) {
  detailImages.value.splice(idx, 1)
}

// ---- SKU 操作 ----
function addSku() {
  skuList.value.push({ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 0 })
}

function removeSku(idx: number) {
  skuList.value.splice(idx, 1)
}

function setDefaultSku(idx: number) {
  skuList.value.forEach((s, i) => { s.isDefault = (i === idx ? 1 : 0) as 0 | 1 })
}

// ---- 保存 ----
function buildPayload(statusOverride?: number) {
  const images = [
    ...carouselImages.value.map(url => ({ url, type: 1 })),
    ...detailImages.value.map(url => ({ url, type: 2 })),
  ]
  return {
    name: form.value.name,
    subtitle: form.value.subtitle,
    categoryId: form.value.categoryId,
    mainImage: form.value.mainImage || carouselImages.value[0] || '',
    description: form.value.description,
    videoUrl: form.value.videoUrl || undefined,
    isRecommend: form.value.isRecommend,
    supportDelivery: form.value.supportDelivery,
    supportPickup: form.value.supportPickup,
    skus: skuList.value.filter(s => s.specName).map(s => ({
      specName: s.specName,
      price: s.price,
      originalPrice: s.originalPrice || undefined,
      stock: s.stock,
      isDefault: s.isDefault,
    })),
    images,
    status: statusOverride ?? form.value.status,
  }
}

async function saveDraft() {
  saving.value = true
  try {
    const payload = buildPayload(0)
    if (isEdit.value) {
      await adminCatalogApi.updateProduct(productId.value, payload)
    } else {
      await adminCatalogApi.createProduct(payload as any)
    }
    toast.success('草稿已保存')
    router.push('/goods')
  } catch (e) {
    console.warn('保存失败', e)
    toast.error('保存失败，请稍后重试')
  } finally { saving.value = false }
}

async function publish() {
  saving.value = true
  try {
    const payload = buildPayload(1)
    if (isEdit.value) {
      await adminCatalogApi.updateProduct(productId.value, payload)
    } else {
      await adminCatalogApi.createProduct(payload as any)
    }
    toast.success(isEdit.value ? '商品已更新' : '商品已发布上架')
    router.push('/goods')
  } catch (e) {
    console.warn('发布失败', e)
    toast.error('发布失败，请稍后重试')
  } finally { saving.value = false }
}

function goBack() {
  router.push('/goods')
}

onMounted(() => {
  loadCategories()
  loadProduct()
})
</script>
