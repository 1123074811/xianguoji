<template>
  <span class="relative inline-flex" @mouseenter="show = true" @mouseleave="show = false" @focusin="show = true" @focusout="show = false">
    <slot />
    <transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-100 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <span
        v-if="show && text"
        role="tooltip"
        :class="[
          'absolute z-50 whitespace-nowrap px-2.5 py-1.5 rounded-md bg-slate-800 text-white text-xs font-medium shadow-lg pointer-events-none',
          positionClass,
        ]"
      >
        {{ text }}
        <span :class="['absolute w-2 h-2 bg-slate-800 rotate-45', arrowClass]"></span>
      </span>
    </transition>
  </span>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    text?: string
    placement?: 'top' | 'bottom' | 'left' | 'right'
  }>(),
  { placement: 'top' }
)

const show = ref(false)

const positionClass = computed(() => {
  switch (props.placement) {
    case 'bottom':
      return 'top-full left-1/2 -translate-x-1/2 mt-2'
    case 'left':
      return 'right-full top-1/2 -translate-y-1/2 mr-2'
    case 'right':
      return 'left-full top-1/2 -translate-y-1/2 ml-2'
    case 'top':
    default:
      return 'bottom-full left-1/2 -translate-x-1/2 mb-2'
  }
})

const arrowClass = computed(() => {
  switch (props.placement) {
    case 'bottom':
      return 'top-[-3px] left-1/2 -translate-x-1/2'
    case 'left':
      return 'right-[-3px] top-1/2 -translate-y-1/2'
    case 'right':
      return 'left-[-3px] top-1/2 -translate-y-1/2'
    case 'top':
    default:
      return 'bottom-[-3px] left-1/2 -translate-x-1/2'
  }
})
</script>
