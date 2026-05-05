import { ref, onUnmounted, watch } from 'vue'
import { useAdminStore } from '@/stores/admin'

export interface WsMessage {
  type: string
  title: string
  content: string
  orderNo?: string
  payAmount?: string
  deliveryType?: string
  refundAmount?: string
}

export function useWebSocket() {
  const BASE_URL = import.meta.env.VITE_WS_URL || 'ws://127.0.0.1:8080'
  const adminStore = useAdminStore()

  const connected = ref(false)
  const lastMessage = ref<WsMessage | null>(null)
  const unreadCount = ref(0)

  let ws: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let reconnectAttempts = 0
  const MAX_RECONNECT = 10
  const HEARTBEAT_INTERVAL = 30000

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
      startHeartbeat()
    }

    ws.onmessage = (event) => {
      try {
        const msg: WsMessage = JSON.parse(event.data)
        lastMessage.value = msg
        unreadCount.value++
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

  function resetUnread() {
    unreadCount.value = 0
  }

  // Auto-connect when logged in, disconnect when logged out
  watch(() => adminStore.isLogin, (isLogin) => {
    if (isLogin) {
      connect()
    } else {
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
    resetUnread,
    connect,
    disconnect,
  }
}
