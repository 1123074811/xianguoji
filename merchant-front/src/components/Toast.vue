<template>
  <Teleport to="body">
    <div class="toast-container fixed top-5 right-5 z-[9999] flex flex-col gap-2 pointer-events-none">
      <TransitionGroup name="toast">
        <div
          v-for="item in toastState.list"
          :key="item.id"
          :class="[
            'toast-item pointer-events-auto flex items-start gap-2.5 min-w-[260px] max-w-sm px-4 py-3 rounded-xl shadow-lg backdrop-blur border text-sm font-medium',
            typeClass(item.type),
          ]"
          role="status"
        >
          <span class="toast-icon flex-shrink-0 w-5 h-5 flex items-center justify-center rounded-full text-white text-xs font-bold" :class="iconBg(item.type)">
            {{ iconChar(item.type) }}
          </span>
          <span class="flex-1 leading-snug pt-0.5">{{ item.message }}</span>
          <button
            class="flex-shrink-0 opacity-50 hover:opacity-100 transition-opacity text-base leading-none -mt-0.5"
            @click="remove(item.id)"
            aria-label="关闭"
          >×</button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { toastState, remove, type ToastType } from '../utils/toast'

function typeClass(t: ToastType) {
  switch (t) {
    case 'success': return 'bg-green-50/95 border-green-200 text-green-800'
    case 'error':   return 'bg-red-50/95 border-red-200 text-red-800'
    case 'warning': return 'bg-amber-50/95 border-amber-200 text-amber-800'
    default:        return 'bg-sky-50/95 border-sky-200 text-sky-800'
  }
}

function iconBg(t: ToastType) {
  switch (t) {
    case 'success': return 'bg-green-500'
    case 'error':   return 'bg-red-500'
    case 'warning': return 'bg-amber-500'
    default:        return 'bg-sky-500'
  }
}

function iconChar(t: ToastType) {
  switch (t) {
    case 'success': return '✓'
    case 'error':   return '!'
    case 'warning': return '!'
    default:        return 'i'
  }
}
</script>

<style scoped>
.toast-enter-from {
  opacity: 0;
  transform: translateX(24px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(24px);
}
.toast-enter-active,
.toast-leave-active {
  transition: opacity 220ms ease, transform 220ms ease;
}
.toast-move {
  transition: transform 220ms ease;
}
</style>
