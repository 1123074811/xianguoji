// 高德地图 JS API 2.0 加载器
// 文档：https://lbs.amap.com/api/jsapi-v2/guide/abc/load

declare global {
  interface Window {
    AMap?: any
    _AMapSecurityConfig?: { securityJsCode: string }
  }
}

let loadingPromise: Promise<any> | null = null

export function isAmapKeyConfigured(): boolean {
  return !!import.meta.env.VITE_AMAP_KEY
}

export function loadAmap(plugins: string[] = []): Promise<any> {
  if (window.AMap) return Promise.resolve(window.AMap)
  if (loadingPromise) return loadingPromise

  const key = import.meta.env.VITE_AMAP_KEY as string
  if (!key) {
    return Promise.reject(new Error('未配置 VITE_AMAP_KEY，请前往 https://lbs.amap.com 申请并填入 .env'))
  }

  const securityCode = import.meta.env.VITE_AMAP_SECURITY_CODE as string
  if (securityCode) {
    window._AMapSecurityConfig = { securityJsCode: securityCode }
  }

  loadingPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    const pluginParam = plugins.length ? `&plugin=${plugins.join(',')}` : ''
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}${pluginParam}`
    script.async = true
    script.onload = () => {
      if (window.AMap) resolve(window.AMap)
      else reject(new Error('高德地图加载失败'))
    }
    script.onerror = () => {
      loadingPromise = null
      reject(new Error('高德地图脚本加载失败，请检查 Key 与网络'))
    }
    document.head.appendChild(script)
  })

  return loadingPromise
}
