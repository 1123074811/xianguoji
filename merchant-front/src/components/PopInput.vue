<template>
  <span class="relative inline-block align-middle" ref="triggerRef">
    <span @click.stop="toggle" class="inline-flex cursor-pointer">
      <slot />
    </span>
    <transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <div
        v-if="visible"
        ref="popoverRef"
        :class="[
          'absolute z-50 w-72 rounded-xl bg-white shadow-xl border border-slate-200/80 p-4',
          positionClass,
        ]"
        @click.stop
      >
        <!-- Arrow -->
        <span :class="['absolute w-2.5 h-2.5 bg-white border-slate-200/80 rotate-45', arrowClass]"></span>

        <!-- Header -->
        <div class="flex gap-2.5 mb-3">
          <span class="flex-shrink-0 w-7 h-7 rounded-full flex items-center justify-center" :class="iconWrapClass">
            <span class="material-symbols-outlined text-sm" :class="iconColorClass">{{ icon }}</span>
          </span>
          <div class="flex-1 min-w-0">
            <p v-if="title" class="font-label-bold text-slate-800 text-sm mb-0.5">{{ title }}</p>
            <p class="text-[11px] text-slate-500 leading-relaxed">{{ message }}</p>
          </div>
        </div>

        <!-- Input -->
        <div class="relative mb-3">
          <input
            ref="inputRef"
            v-model="inputValue"
            :maxlength="maxLength"
            :placeholder="placeholder"
            @keydown.enter="onConfirm"
            class="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:ring-2 focus:ring-primary focus:border-primary outline-none text-sm transition-all"
          />
          <span v-if="maxLength" class="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-slate-400">{{ inputValue.length }}/{{ maxLength }}</span>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-2">
          <button
            @click="onCancel"
            class="px-3 py-1.5 text-xs font-label-bold rounded-lg text-slate-600 hover:bg-slate-100 transition-colors"
          >
            {{ cancelText }}
          </button>
          <button
            @click="onConfirm"
            :disabled="!inputValue.trim()"
            class="px-3 py-1.5 text-xs font-label-bold rounded-lg text-white transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            :class="confirmBtnClass"
          >
            {{ confirmText }}
          </button>
        </div>
      </div>
    </transition>
  </span>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'

type PopInputType = 'danger' | 'warning' | 'info'

const props = withDefaults(
  defineProps<{
    title?: string
    message: string
    placeholder?: string
    maxLength?: number
    confirmText?: string
    cancelText?: string
    type?: PopInputType
    placement?: 'top' | 'bottom' | 'left' | 'right'
  }>(),
  {
    confirmText: '确定',
    cancelText: '取消',
    type: 'info',
    placement: 'top',
    placeholder: '',
    maxLength: 200,
  }
)

const emit = defineEmits<{
  confirm: [value: string]
  cancel: []
}>()

const visible = ref(false)
const inputValue = ref('')
const triggerRef = ref<HTMLSpanElement | null>(null)
const popoverRef = ref<HTMLDivElement | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)

const icon = computed(() => {
  switch (props.type) {
    case 'danger': return 'error'
    case 'warning': return 'warning'
    case 'info': return 'edit'
  }
})

const iconWrapClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'bg-red-50'
    case 'warning': return 'bg-amber-50'
    case 'info': return 'bg-sky-50'
  }
})

const iconColorClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'text-red-500'
    case 'warning': return 'text-amber-500'
    case 'info': return 'text-sky-500'
  }
})

const confirmBtnClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'bg-red-500 hover:bg-red-600'
    case 'warning': return 'bg-amber-500 hover:bg-amber-600'
    case 'info': return 'bg-primary hover:bg-primary-container'
  }
})

const positionClass = computed(() => {
  switch (props.placement) {
    case 'bottom': return 'top-full left-1/2 -translate-x-1/2 mt-2.5'
    case 'left': return 'right-full top-1/2 -translate-y-1/2 mr-2.5'
    case 'right': return 'left-full top-1/2 -translate-y-1/2 ml-2.5'
    case 'top':
    default: return 'bottom-full left-1/2 -translate-x-1/2 mb-2.5'
  }
})

const arrowClass = computed(() => {
  switch (props.placement) {
    case 'bottom': return 'top-[-5px] left-1/2 -translate-x-1/2 border-t border-l'
    case 'left': return 'right-[-5px] top-1/2 -translate-y-1/2 border-t border-l'
    case 'right': return 'left-[-5px] top-1/2 -translate-y-1/2 border-r border-b'
    case 'top':
    default: return 'bottom-[-5px] left-1/2 -translate-x-1/2 border-r border-b'
  }
})

function toggle() {
  visible.value = !visible.value
  if (visible.value) {
    inputValue.value = ''
    nextTick(() => inputRef.value?.focus())
  }
}

function onConfirm() {
  if (!inputValue.value.trim()) return
  visible.value = false
  emit('confirm', inputValue.value.trim())
}

function onCancel() {
  visible.value = false
  emit('cancel')
}

function onClickOutside(e: MouseEvent) {
  if (!visible.value) return
  const target = e.target as Node
  if (triggerRef.value?.contains(target)) return
  if (popoverRef.value?.contains(target)) return
  visible.value = false
  emit('cancel')
}

onMounted(() => document.addEventListener('click', onClickOutside, true))
onBeforeUnmount(() => document.removeEventListener('click', onClickOutside, true))
</script>
