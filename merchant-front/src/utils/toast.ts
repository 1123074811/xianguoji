import { reactive } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastItem {
  id: number
  type: ToastType
  message: string
  duration: number
}

interface ToastState {
  list: ToastItem[]
}

export const toastState = reactive<ToastState>({ list: [] })

let seed = 0

function push(type: ToastType, message: string, duration = 2600) {
  const id = ++seed
  toastState.list.push({ id, type, message, duration })
  if (duration > 0) {
    window.setTimeout(() => remove(id), duration)
  }
  return id
}

export function remove(id: number) {
  const idx = toastState.list.findIndex(t => t.id === id)
  if (idx >= 0) toastState.list.splice(idx, 1)
}

export const toast = {
  success: (msg: string, duration?: number) => push('success', msg, duration),
  error: (msg: string, duration?: number) => push('error', msg, duration ?? 3200),
  warning: (msg: string, duration?: number) => push('warning', msg, duration),
  info: (msg: string, duration?: number) => push('info', msg, duration),
}
