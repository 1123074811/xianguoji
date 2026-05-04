<template>
  <div>
    <div class="mb-stack-lg flex justify-between items-end">
      <div>
        <h1 class="font-h1 text-h1 text-on-surface mb-1">评价管理</h1>
        <p class="font-body-md text-body-md text-slate-500">查看并回复客户评价，维护店铺口碑。</p>
      </div>
    </div>

    <!-- Filter Tabs -->
    <div class="bg-white border border-slate-200 rounded-xl overflow-hidden mb-stack-md">
      <div class="flex border-b border-slate-100 overflow-x-auto">
        <button v-for="tab in tabs" :key="tab.label" @click="changeFilter(tab.value)"
          class="px-6 py-4 font-label-bold whitespace-nowrap transition-colors"
          :class="filter === tab.value ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant hover:text-primary border-b-2 border-transparent'">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center text-sm text-slate-400 py-12">加载中...</div>
    <div v-else-if="!reviews.length" class="text-center text-sm text-slate-400 py-12">暂无评价</div>
    <div v-else class="space-y-gutter">
      <div v-for="review in reviews" :key="review.id" class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
        <div class="flex gap-4">
          <img v-if="review.userAvatar" :src="review.userAvatar" class="w-10 h-10 rounded-full object-cover shrink-0" alt="" />
          <div v-else class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 font-bold text-xs bg-primary/10 text-primary">
            {{ initials(review.userName) }}
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-3">
                <span class="font-label-bold text-slate-800">{{ review.userName || '匿名用户' }}</span>
                <div class="flex items-center gap-0.5">
                  <span v-for="i in 5" :key="i" class="material-symbols-outlined text-sm" :class="i <= review.rating ? 'text-amber-400' : 'text-slate-300'" style="font-variation-settings: 'FILL' 1;">star</span>
                </div>
                <span class="text-xs text-slate-400">{{ review.createdAt }}</span>
              </div>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                :class="review.merchantReply ? 'bg-primary-fixed text-on-primary-fixed-variant border border-primary/20' : 'bg-tertiary-fixed text-on-tertiary-fixed-variant border border-tertiary-container/20'">
                {{ review.merchantReply ? '已回复' : '待回复' }}
              </span>
            </div>

            <p class="text-sm text-slate-700 mb-3">{{ review.content }}</p>

            <div class="flex flex-wrap gap-2 mb-3" v-if="review.images?.length">
              <img v-for="img in review.images" :key="img" :src="img" class="w-16 h-16 rounded object-cover" alt="" />
            </div>

            <div v-if="review.merchantReply" class="bg-slate-50 rounded-lg p-3 border border-slate-200">
              <div class="flex items-center gap-2 mb-1">
                <span class="material-symbols-outlined text-primary text-sm">store</span>
                <span class="text-xs font-bold text-primary">商家回复</span>
                <span v-if="review.repliedAt" class="text-[10px] text-slate-400">{{ review.repliedAt }}</span>
              </div>
              <p class="text-xs text-slate-600">{{ review.merchantReply }}</p>
            </div>

            <div class="flex items-center gap-3 mt-3">
              <button v-if="!review.merchantReply" class="text-primary text-xs font-bold flex items-center gap-1 hover:underline" @click="onReply(review)">
                <span class="material-symbols-outlined text-sm">reply</span>
                回复
              </button>
              <button class="text-slate-400 text-xs flex items-center gap-1 hover:text-error" @click="onToggleHidden(review)">
                <span class="material-symbols-outlined text-sm">visibility_off</span>
                隐藏
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="reviews.length" class="mt-stack-lg flex items-center justify-between">
      <span class="text-body-sm text-slate-500">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</span>
      <div class="flex items-center gap-1">
        <button :disabled="page <= 1" @click="page--; load()" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white disabled:opacity-30">
          <span class="material-symbols-outlined text-sm">chevron_left</span>
        </button>
        <button :disabled="page >= totalPages" @click="page++; load()" class="w-8 h-8 flex items-center justify-center rounded border border-outline-variant text-slate-400 hover:bg-white disabled:opacity-30">
          <span class="material-symbols-outlined text-sm">chevron_right</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { adminReviewApi } from '@/api/modules/review';
import type { AdminReviewVO } from '@/api/types/review';

const tabs: { label: string; value?: string }[] = [
  { label: '全部' },
  { label: '待回复', value: 'pending_reply' },
  { label: '差评', value: 'low_rating' },
  { label: '带图', value: 'with_image' },
];

const filter = ref<string | undefined>(undefined);
const reviews = ref<AdminReviewVO[]>([]);
const loading = ref(false);
const total = ref(0);
const page = ref(1);
const size = ref(20);

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)));

function initials(name: string) {
  if (!name) return '?';
  return name.slice(0, 2).toUpperCase();
}

async function load() {
  loading.value = true;
  try {
    const data = await adminReviewApi.page({ page: page.value, size: size.value, filter: filter.value });
    reviews.value = data.list;
    total.value = data.total;
  } catch (e) {
    console.warn('加载评价失败', e);
  } finally {
    loading.value = false;
  }
}

function changeFilter(v?: string) {
  filter.value = v;
  page.value = 1;
  load();
}

async function onReply(review: AdminReviewVO) {
  const text = prompt('请输入回复内容（不超过200字）');
  if (!text) return;
  await adminReviewApi.reply(review.id, text);
  await load();
}

async function onToggleHidden(review: AdminReviewVO) {
  if (!confirm('确定要隐藏该条评价吗？')) return;
  await adminReviewApi.toggleHidden(review.id, 1);
  await load();
}

onMounted(load);
</script>
