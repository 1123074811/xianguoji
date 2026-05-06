import { ref, onUnmounted, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminStore } from '@/stores/admin'
import { useNotificationStore } from '@/stores/notification'

export interface WsMessage {
  type: string
  title: string
  content: string
  orderNo?: string
  payAmount?: string
  deliveryType?: string
  refundAmount?: string
  notificationId?: number
  notificationType?: number
  linkUrl?: string
  isRead?: number
  createdAt?: string
}

const connected = ref(false)
const lastMessage = ref<WsMessage | null>(null)

let ws: WebSocket | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let reconnectAttempts = 0
const MAX_RECONNECT = 10
const HEARTBEAT_INTERVAL = 30000

export function useWebSocket() {
  const BASE_URL = import.meta.env.VITE_WS_URL || 'ws://127.0.0.1:8080'
  const adminStore = useAdminStore()
  const notificationStore = useNotificationStore()
  const { unreadCount } = storeToRefs(notificationStore)

  function connect() {
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
      return
    }
    if (!adminStore.token) return

    const url = `${BASE_URL}/ws/admin?token=${adminStore.token}`
    ws = new WebSocket(url)

    ws.onopen = () => {
      connected.value = true
      reconnectAttempts = 0
      notificationStore.refreshUnreadCount().catch(() => undefined)
      startHeartbeat()
    }

    ws.onmessage = (event) => {
      try {
        const msg: WsMessage = JSON.parse(event.data)
        lastMessage.value = msg
        notificationStore.incrementUnreadCount()
      } catch {
        // ignore non-JSON
      }
    }

    ws.onclose = () => {
      connected.value = false
      stopHeartbeat()
      scheduleReconnect()
    }

    ws.onerror = () => {
      connected.value = false
    }
  }

  function disconnect() {
    stopHeartbeat()
    clearReconnect()
    if (ws) {
      ws.onclose = null
      ws.close()
      ws = null
    }
    connected.value = false
  }

  function scheduleReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT) return
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000)
    reconnectAttempts++
    reconnectTimer = setTimeout(() => {
      if (adminStore.isLogin) connect()
    }, delay)
  }

  function clearReconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    reconnectAttempts = 0
  }

  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send('ping')
      }
    }, HEARTBEAT_INTERVAL)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  // Auto-connect when logged in, disconnect when logged out
  watch(() => adminStore.isLogin, (isLogin) => {
    if (isLogin) {
      notificationStore.refreshUnreadCount().catch(() => undefined)
      connect()
    } else {
      notificationStore.clearUnreadCount()
      disconnect()
    }
  }, { immediate: true })

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    lastMessage,
    unreadCount,
    refreshUnreadCount: notificationStore.refreshUnreadCount,
    connect,
    disconnect,
  }
}
