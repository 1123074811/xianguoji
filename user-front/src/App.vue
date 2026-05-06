<script setup lang="ts">
import { onLaunch, onShow, onHide, onError } from "@dcloudio/uni-app";

// 需要静默的 SDK / 游客模式内部错误关键字
const SILENCED_ERROR_KEYWORDS = [
  "webapi_getwxaasyncsecinfo", // 游客模式下安全态接口失败
  "operateWXData",              // 游客模式 API 受限
  "getSystemInfoSync is deprecated" // SDK 内部废弃 API 警告
];

function isSilenced(msg: unknown): boolean {
  let text = "";
  try {
    text = typeof msg === "string" ? msg : JSON.stringify(msg ?? "");
  } catch (_) {
    text = String(msg);
  }
  return SILENCED_ERROR_KEYWORDS.some((k) => text.indexOf(k) !== -1);
}

// 包装 console.error / console.warn，过滤无害的 SDK 噪音
const originalError = console.error.bind(console);
const originalWarn = console.warn.bind(console);

console.error = (...args: any[]) => {
  if (args.some(isSilenced)) return;
  originalError(...args);
};
console.warn = (...args: any[]) => {
  if (args.some(isSilenced)) return;
  originalWarn(...args);
};

onLaunch(() => {
  console.log("App Launch");
  // F-10: 小程序版本更新检查
  import('@/utils/verify').then(({ checkUpdate }) => checkUpdate());
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});
onError((err) => {
  if (isSilenced(err)) return;
  console.error("[App onError]", err);
});
</script>
<style lang="scss">
@import "@/styles/global.scss";
</style>
