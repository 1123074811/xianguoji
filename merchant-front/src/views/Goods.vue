<template>
  <div>
    <!-- Header -->
    <div class="mb-6 flex flex-col md:flex-row md:items-end justify-between gap-4">
      <div>
        <h1 class="font-h1 text-on-surface mb-1">商品管理</h1>
        <p class="text-slate-500 font-body-md">管理您的时令水果、库存水平和定价。</p>
      </div>
      <button class="bg-primary text-on-primary px-6 py-2.5 rounded-lg flex items-center gap-2 font-label-bold shadow-md hover:translate-y-[-1px] transition-all" @click="$router.push('/goods/edit')">
        <span class="material-symbols-outlined">add</span>
        新增商品
      </button>
    </div>

    <div class="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col">
      <!-- Tabs -->
      <div class="flex items-center px-6 border-b border-slate-100 overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.label" @click="changeTab(tab.value)"
          class="px-6 py-4 font-label-bold border-b-2 whitespace-nowrap transition-colors"
          :class="activeStatus === tab.value ? 'border-primary text-primary' : 'border-transparent text-slate-400 hover:text-slate-600'">
          {{ tab.label }}
        </button>
      </div>

      <!-- Filter Bar -->
      <div class="p-4 bg-surface-container-lowest border-b border-slate-100 flex flex-wrap items-center gap-4">
        <div class="relative flex-1 max-w-xs">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-[18px]">search</span>
          <input v-model="keyword" @keydown.enter="applyFilter" class="w-full pl-9 pr-3 py-2 border border-outline-variant rounded-lg outline-none focus:ring-1 focus:ring-primary text-sm" placeholder="搜索商品名称" />
        </div>
        <select v-model.number="categoryId" class="bg-white border border-outline-variant rounded-lg pl-3 pr-10 py-2 text-sm font-label-bold cursor-pointer">
          <option :value="0">所有分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        <button @click="applyFilter" class="px-4 py-2 bg-primary text-white text-sm font-label-bold rounded-lg">查询</button>
        <span class="ml-auto text-body-sm text-slate-500">共 {{ total }} 件商品</span>
      </div>

      <!-- Table -->
      <div v-if="loading" class="text-center text-sm text-slate-400 py-12">加载中...</div>
      <div v-else-if="!products.length" class="text-center text-sm text-slate-400 py-12">暂无商品</div>
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead class="bg-surface-container-low border-b border-slate-200">
            <tr>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">预览</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">商品信息</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">价格</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">库存</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">销量</th>
              <th class="px-3 py-3 font-table-header text-on-surface-variant uppercase">状态</th>
              <th class="px-6 py-3 font-table-header text-on-surface-variant uppercase text-right">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="product in products" :key="product.id" class="hover:bg-slate-50/80 transition-colors group">
              <td class="px-3 py-3">
                <img v-if="product.mainImage" :src="resolveImageUrl(product.mainImage)" class="w-12 h-12 rounded object-cover" alt="" />
                <div v-else class="w-12 h-12 rounded bg-slate-100 border border-slate-200 flex items-center justify-center">
                  <span class="material-symbols-outlined text-slate-400">nutrition</span>
                </div>
              </td>
              <td class="px-3 py-3">
                <div class="font-label-bold text-on-surface flex items-center gap-2">
                  {{ product.name }}
                  <span v-if="product.totalStock < 50" class="material-symbols-outlined text-error text-[16px]">warning</span>
                </div>
                <div class="text-xs text-slate-400 font-body-sm">#{{ product.id }}</div>
                <div v-if="product.subtitle" class="text-xs text-slate-500">{{ product.subtitle }}</div>
              </td>
              <td class="px-3 py-3 text-body-md font-label-bold text-on-surface">¥{{ product.minPrice }}<span v-if="product.maxPrice !== product.minPrice"> ~ ¥{{ product.maxPrice }}</span></td>
              <td class="px-3 py-3 text-body-md" :class="product.totalStock < 50 ? 'text-error font-bold' : 'text-slate-600'">{{ product.totalStock }} 件</td>
              <td class="px-3 py-3 text-body-md text-slate-600">{{ product.sales }}</td>
              <td class="px-3 py-3">
                <button @click="toggleStatus(product)"
                  class="w-10 h-5 rounded-full relative cursor-pointer shadow-inner transition-colors"
                  :class="product.status === 1 ? 'bg-primary-container' : 'bg-outline-variant'">
                  <div class="absolute top-0.5 w-4 h-4 bg-white rounded-full shadow-sm transition-all" :class="product.status === 1 ? 'right-0.5' : 'left-0.5'"></div>
                </button>
              </td>
              <td class="px-6 py-3 text-right">
                <div class="flex items-center justify-end gap-2 text-slate-400">
                  <button class="p-1 hover:text-primary" title="编辑" @click="$router.push('/goods/edit/' + product.id)">
                    <span class="material-symbols-outlined text-[20px]">edit</span>
                  </button>
                  <button class="p-1 hover:text-primary" title="复制" @click="onCopy(product)">
                    <span class="material-symbols-outlined text-[20px]">content_copy</span>
                  </button>
                  <button class="p-1 hover:text-error" title="移入回收站" @click="onDelete(product)">
                    <span class="material-symbols-outlined text-[20px]">delete</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-between bg-surface-container-low gap-4 flex-wrap">
        <div class="flex items-center gap-3 text-body-sm text-slate-500">
          <span>第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</span>
          <select v-model.number="size" @change="changeSize" class="border border-outline-variant rounded px-2 py-1 text-xs bg-white">
            <option :value="10">10条/页</option>
            <option :value="20">20条/页</option>
            <option :value="50">50条/页</option>
            <option :value="100">100条/页</option>
          </select>
        </div>
        <div class="flex items-center gap-1">
          <button :disabled="page <= 1" @click="goPage(1)" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-500 hover:bg-white disabled:opacity-30 text-xs">首页</button>
          <button :disabled="page <= 1" @click="goPage(page - 1)" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white disabled:opacity-30">
            <span class="material-symbols-outlined text-sm">chevron_left</span>
          </button>
          <button v-for="n in pageNumbers" :key="n" @click="goPage(n)" class="w-8 h-8 flex items-center justify-center rounded border text-sm transition-colors"
            :class="n === page ? 'bg-primary text-white border-primary font-bold' : 'border-outline-variant text-slate-600 hover:bg-white'">
            {{ n }}
          </button>
          <button :disabled="page >= totalPages" @click="goPage(page + 1)" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white disabled:opacity-30">
            <span class="material-symbols-outlined text-sm">chevron_right</span>
          </button>
          <button :disabled="page >= totalPages" @click="goPage(totalPages)" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-500 hover:bg-white disabled:opacity-30 text-xs">末页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { adminCatalogApi } from '@/api/modules/catalog';
import type { AdminProductVO, AdminCategoryVO } from '@/api/types/catalog';
import { resolveImageUrl } from '@/utils/image';
import { toast } from '@/utils/toast';

/** status: 0=已下架 1=在售 2=回收站 */
const tabs: { label: string; value?: number }[] = [
  { label: '全部' },
  { label: '在售中', value: 1 },
  { label: '已下架', value: 0 },
  { label: '回收站', value: 2 },
];

const activeStatus = ref<number | undefined>(1);
const keyword = ref('');
const categoryId = ref(0);
const categories = ref<AdminCategoryVO[]>([]);
const products = ref<AdminProductVO[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(20);
const loading = ref(false);

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));

// 显示当前页前后各 2 页
const pageNumbers = computed(() => {
  const cur = page.value;
  const total = totalPages.value;
  const arr: number[] = [];
  const start = Math.max(1, cur - 2);
  const end = Math.min(total, start + 4);
  for (let i = start; i <= end; i++) arr.push(i);
  return arr;
});

function goPage(n: number) {
  if (n < 1 || n > totalPages.value || n === page.value) return;
  page.value = n;
  loadProducts();
}

function changeSize() {
  page.value = 1;
  loadProducts();
}

async function loadProducts() {
  loading.value = true;
  try {
    const data = await adminCatalogApi.productPage({
      page: page.value,
      size: size.value,
      status: activeStatus.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
    });
    products.value = data.list;
    total.value = data.total;
  } catch (e) {
    console.warn('加载商品失败', e);
  } finally {
    loading.value = false;
  }
}

async function loadCategories() {
  try {
    categories.value = await adminCatalogApi.categoryList();
  } catch (e) {
    console.warn('加载分类失败', e);
  }
}

function changeTab(value?: number) {
  activeStatus.value = value;
  page.value = 1;
  loadProducts();
}

function applyFilter() {
  page.value = 1;
  loadProducts();
}

async function toggleStatus(p: AdminProductVO) {
  const next = (p.status === 1 ? 0 : 1) as 0 | 1;
  try {
    await adminCatalogApi.updateProductStatus(p.id, next);
    p.status = next;
    toast.success(next === 1 ? `「${p.name}」已上架` : `「${p.name}」已下架`);
  } catch (e) {
    console.warn('切换商品状态失败', e);
    toast.error('操作失败，请稍后重试');
  }
}

async function onCopy(p: AdminProductVO) {
  if (!confirm(`复制商品「${p.name}」？`)) return;
  try {
    await adminCatalogApi.copyProduct(p.id);
    await loadProducts();
    toast.success('已复制商品');
  } catch (e) {
    console.warn('复制商品失败', e);
    toast.error('复制失败，请稍后重试');
  }
}

async function onDelete(p: AdminProductVO) {
  if (!confirm(`确定要将「${p.name}」移入回收站吗？`)) return;
  try {
    await adminCatalogApi.deleteProduct(p.id);
    await loadProducts();
    toast.success('已移入回收站');
  } catch (e) {
    console.warn('删除商品失败', e);
    toast.error('删除失败，请稍后重试');
  }
}

onMounted(() => {
  loadCategories();
  loadProducts();
});
</script>
