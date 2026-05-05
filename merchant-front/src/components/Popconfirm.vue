<template>
  <span class="relative inline-block align-middle" ref="triggerRef">
    <span @click.stop="toggle" class="inline-flex cursor-pointer">
      <slot />
    </span>
    <transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="opacity-0 scale-95 translate-y-1"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <div
        v-if="visible"
        ref="popoverRef"
        :class="[
          'absolute z-[100] w-64 rounded-xl bg-white shadow-[0_8px_32px_rgba(0,0,0,0.12)] border border-slate-200 p-4',
          positionClass,
        ]"
        @click.stop
      >
        <!-- Arrow -->
        <span :class="['absolute w-3 h-3 bg-white border-slate-200 rotate-45', arrowClass]"></span>

        <!-- Content -->
        <div class="flex gap-3 mb-3">
          <span class="flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center" :class="iconWrapClass">
            <span class="material-symbols-outlined text-base" :class="iconClass">{{ icon }}</span>
          </span>
          <div class="flex-1 min-w-0 text-left">
            <p v-if="title" class="font-label-bold text-slate-800 text-sm mb-0.5">{{ title }}</p>
            <p class="text-xs text-slate-500 leading-relaxed">{{ message }}</p>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end gap-2">
          <button
            @click="cancel"
            class="px-3 py-1.5 text-xs font-label-bold rounded-lg text-slate-600 hover:bg-slate-100 transition-colors"
          >
            {{ cancelText }}
          </button>
          <button
            @click="confirm"
            class="px-3 py-1.5 text-xs font-label-bold rounded-lg text-white transition-colors shadow-sm"
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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

type PopconfirmType = 'danger' | 'warning' | 'info'

const props = withDefaults(
  defineProps<{
    title?: string
    message: string
    confirmText?: string
    cancelText?: string
    type?: PopconfirmType
    placement?: 'top' | 'bottom' | 'left' | 'right'
  }>(),
  {
    confirmText: '确定',
    cancelText: '取消',
    type: 'danger',
    placement: 'top',
  }
)

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const visible = ref(false)
const triggerRef = ref<HTMLSpanElement | null>(null)
const popoverRef = ref<HTMLDivElement | null>(null)

const icon = computed(() => {
  switch (props.type) {
    case 'danger': return 'error'
    case 'warning': return 'warning'
    case 'info': return 'info'
  }
})

const iconWrapClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'bg-red-50'
    case 'warning': return 'bg-amber-50'
    case 'info': return 'bg-primary-fixed/50'
  }
})

const iconClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'text-red-500'
    case 'warning': return 'text-amber-500'
    case 'info': return 'text-primary'
  }
})

const confirmBtnClass = computed(() => {
  switch (props.type) {
    case 'danger': return 'bg-red-500 hover:bg-red-600'
    case 'warning': return 'bg-amber-500 hover:bg-amber-600'
    case 'info': return 'bg-primary hover:bg-primary-container active:bg-on-primary-fixed-variant'
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
    case 'left': return 'right-[-5px] top-1/2 -translate-y-1/2 border-t border-r'
    case 'right': return 'left-[-5px] top-1/2 -translate-y-1/2 border-b border-l'
    case 'top':
    default: return 'bottom-[-5px] left-1/2 -translate-x-1/2 border-r border-b'
  }
})

function toggle() {
  visible.value = !visible.value
}

function confirm() {
  visible.value = false
  emit('confirm')
}

function cancel() {
  visible.value = false
  emit('cancel')
}

function onClickOutside(e: MouseEvent) {
  if (!visible.value) return
  const target = e.target as Node
  if (triggerRef.value?.contains(target)) return
  if (popoverRef.value?.contains(target)) return
  visible.value = false
}

onMounted(() => document.addEventListener('click', onClickOutside, true))
onBeforeUnmount(() => document.removeEventListener('click', onClickOutside, true))
</script>
