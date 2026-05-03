<template>
  <main class="min-h-screen flex flex-col md:flex-row overflow-hidden">
    <!-- Left Side: Brand Illustration (60%) -->
    <section class="relative hidden md:flex md:w-[60%] h-screen bg-gradient-to-br from-green-700/80 via-green-800/70 to-green-900/60 overflow-hidden items-center justify-center login-bg">
      <!-- Background Image with Overlay -->
      <div class="absolute inset-0 z-0">
        <img alt="新鲜水果" class="w-full h-full object-cover opacity-70" src="/images/login-bg.jpg" />
      </div>
      <!-- Color Tint Overlay -->
      <div class="absolute inset-0 z-0 bg-primary/30"></div>
      <!-- Decorative Pattern Overlay -->
      <div class="absolute inset-0 z-0 opacity-10 login-pattern"></div>
      <!-- Branding Content -->
      <div class="relative z-10 flex flex-col items-center text-center px-12">
        <div class="mb-stack-lg p-6 glass-panel rounded-full shadow-2xl">
          <span class="material-symbols-outlined text-white text-6xl">restaurant</span>
        </div>
        <h1 class="font-h1 text-h1 text-white tracking-tight mb-stack-sm">鲜果记</h1>
        <p class="font-body-lg text-body-lg text-primary-fixed max-w-md">
          安全重置您的密码，保障账户安全。
        </p>
        <!-- Decorative Bento Elements -->
        <div class="mt-12 grid grid-cols-2 gap-gutter w-full max-w-lg">
          <div class="glass-panel p-stack-md rounded-xl text-left border-white/10">
            <span class="material-symbols-outlined text-white mb-2">verified_user</span>
            <div class="font-label-bold text-label-bold text-white">安全验证</div>
            <div class="font-body-sm text-body-sm text-white/70">手机号实时验证</div>
          </div>
          <div class="glass-panel p-stack-md rounded-xl text-left border-white/10">
            <span class="material-symbols-outlined text-white mb-2">lock_reset</span>
            <div class="font-label-bold text-label-bold text-white">即时重置</div>
            <div class="font-body-sm text-body-sm text-white/70">验证通过后立即生效</div>
          </div>
        </div>
      </div>
      <!-- Accent Curve -->
      <div class="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-primary to-transparent opacity-50"></div>
    </section>

    <!-- Right Side: Reset Form (40%) -->
    <section class="flex flex-col w-full md:w-[40%] bg-white items-center justify-center px-container-padding-mobile md:px-container-padding-pc relative">
      <!-- Mobile Brand Logo -->
      <div class="md:hidden flex items-center gap-2 mb-stack-lg">
        <span class="material-symbols-outlined text-primary text-3xl">restaurant</span>
        <span class="font-h2 text-h2 text-primary">鲜果记</span>
      </div>

      <div class="w-full max-w-md">
        <!-- Back to Login -->
        <button class="flex items-center gap-2 text-slate-500 hover:text-primary transition-colors mb-6" @click="$router.push('/login')">
          <span class="material-symbols-outlined text-lg">arrow_back</span>
          <span class="text-sm font-medium">返回登录</span>
        </button>

        <div class="mb-stack-lg">
          <h2 class="font-h1 text-h1 text-on-surface mb-stack-xs">忘记密码</h2>
          <p class="font-body-md text-body-md text-on-surface-variant">请输入您的账号和绑定手机号来重置密码。</p>
        </div>

        <!-- Step Indicator -->
        <div class="flex items-center gap-3 mb-8">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold" :class="step >= 1 ? 'bg-primary text-white' : 'bg-slate-200 text-slate-400'">1</div>
            <span class="text-xs font-medium" :class="step >= 1 ? 'text-primary' : 'text-slate-400'">验证身份</span>
          </div>
          <div class="flex-1 h-0.5" :class="step >= 2 ? 'bg-primary' : 'bg-slate-200'"></div>
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold" :class="step >= 2 ? 'bg-primary text-white' : 'bg-slate-200 text-slate-400'">2</div>
            <span class="text-xs font-medium" :class="step >= 2 ? 'text-primary' : 'text-slate-400'">重置密码</span>
          </div>
          <div class="flex-1 h-0.5" :class="step >= 3 ? 'bg-primary' : 'bg-slate-200'"></div>
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold" :class="step >= 3 ? 'bg-primary text-white' : 'bg-slate-200 text-slate-400'">3</div>
            <span class="text-xs font-medium" :class="step >= 3 ? 'text-primary' : 'text-slate-400'">完成</span>
          </div>
        </div>

        <!-- Step 1: Verify Identity -->
        <form v-if="step === 1" class="space-y-stack-md" @submit.prevent="handleVerify">
          <!-- Account Input -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="reset-account">账号</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">person</span>
              <input class="w-full pl-10 pr-4 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="reset-account" placeholder="请输入商户账号" type="text" v-model="form.account" />
            </div>
          </div>

          <!-- Phone Input -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="reset-phone">绑定手机号</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">phone_iphone</span>
              <input class="w-full pl-10 pr-4 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="reset-phone" placeholder="请输入绑定手机号" type="tel" v-model="form.phone" />
            </div>
          </div>

          <!-- SMS Code -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="reset-sms">短信验证码</label>
            <div class="flex gap-stack-md">
              <div class="relative flex-1">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">sms</span>
                <input class="w-full pl-10 pr-4 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="reset-sms" placeholder="输入6位验证码" type="text" maxlength="6" v-model="form.smsCode" />
              </div>
              <button type="button" class="w-32 h-12 border border-primary text-primary rounded-lg font-label-bold text-xs hover:bg-primary/5 transition-colors shrink-0 disabled:opacity-50 disabled:cursor-not-allowed" :disabled="countdown > 0" @click="sendSmsCode">
                {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
              </button>
            </div>
          </div>

          <!-- Next Button -->
          <button class="w-full h-12 bg-primary text-white rounded-lg font-label-bold text-label-bold hover:bg-primary/90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-md" type="submit">
            <span>下一步</span>
            <span class="material-symbols-outlined text-sm">arrow_forward</span>
          </button>
        </form>

        <!-- Step 2: Reset Password -->
        <form v-if="step === 2" class="space-y-stack-md" @submit.prevent="handleReset">
          <!-- New Password -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="new-password">新密码</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">lock</span>
              <input class="w-full pl-10 pr-12 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="new-password" placeholder="请输入新密码 (至少8位)" :type="showNewPwd ? 'text' : 'password'" v-model="form.newPassword" />
              <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors" type="button" @click="showNewPwd = !showNewPwd">
                <span class="material-symbols-outlined text-sm">{{ showNewPwd ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
          </div>

          <!-- Confirm Password -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="confirm-password">确认密码</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">lock</span>
              <input class="w-full pl-10 pr-12 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="confirm-password" placeholder="请再次输入新密码" :type="showConfirmPwd ? 'text' : 'password'" v-model="form.confirmPassword" />
              <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors" type="button" @click="showConfirmPwd = !showConfirmPwd">
                <span class="material-symbols-outlined text-sm">{{ showConfirmPwd ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
          </div>

          <!-- Password Strength Indicator -->
          <div class="space-y-2">
            <div class="flex gap-1">
              <div class="h-1 flex-1 rounded-full" :class="passwordStrength >= 1 ? 'bg-green-500' : 'bg-slate-200'"></div>
              <div class="h-1 flex-1 rounded-full" :class="passwordStrength >= 2 ? 'bg-green-500' : 'bg-slate-200'"></div>
              <div class="h-1 flex-1 rounded-full" :class="passwordStrength >= 3 ? 'bg-green-500' : 'bg-slate-200'"></div>
              <div class="h-1 flex-1 rounded-full" :class="passwordStrength >= 4 ? 'bg-green-500' : 'bg-slate-200'"></div>
            </div>
            <p class="text-xs" :class="strengthColor">{{ strengthText }}</p>
          </div>

          <!-- Submit Button -->
          <button class="w-full h-12 bg-primary text-white rounded-lg font-label-bold text-label-bold hover:bg-primary/90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-md" type="submit">
            <span>确认重置</span>
            <span class="material-symbols-outlined text-sm">check_circle</span>
          </button>
        </form>

        <!-- Step 3: Success -->
        <div v-if="step === 3" class="text-center py-8">
          <div class="w-20 h-20 mx-auto mb-6 bg-primary/10 rounded-full flex items-center justify-center">
            <span class="material-symbols-outlined text-primary text-4xl" style="font-variation-settings: 'FILL' 1;">check_circle</span>
          </div>
          <h3 class="font-h2 text-h2 text-on-surface mb-2">密码重置成功</h3>
          <p class="font-body-md text-body-md text-on-surface-variant mb-8">您的密码已成功重置，请使用新密码登录。</p>
          <button class="w-full h-12 bg-primary text-white rounded-lg font-label-bold text-label-bold hover:bg-primary/90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-md" @click="$router.push('/login')">
            <span>立即登录</span>
            <span class="material-symbols-outlined text-sm">login</span>
          </button>
          <p class="text-xs text-slate-400 mt-4">{{ redirectCountdown }} 秒后自动跳转到登录页</p>
        </div>
      </div>

      <!-- Footer -->
      <footer class="absolute bottom-8 left-0 right-0 text-center">
        <p class="font-body-sm text-body-sm text-outline">鲜果记 · 单店版 v1.0</p>
        <div class="flex justify-center gap-stack-md mt-2">
          <a class="font-body-sm text-body-sm text-outline hover:text-on-surface-variant transition-colors cursor-pointer">隐私政策</a>
          <span class="text-outline-variant">|</span>
          <a class="font-body-sm text-body-sm text-outline hover:text-on-surface-variant transition-colors cursor-pointer">服务条款</a>
          <span class="text-outline-variant">|</span>
          <a class="font-body-sm text-body-sm text-outline hover:text-on-surface-variant transition-colors cursor-pointer">系统反馈</a>
        </div>
      </footer>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const step = ref(1)
const countdown = ref(0)
const redirectCountdown = ref(5)
const showNewPwd = ref(false)
const showConfirmPwd = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null
let redirectTimer: ReturnType<typeof setInterval> | null = null

const form = reactive({
  account: '',
  phone: '',
  smsCode: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordStrength = computed(() => {
  const pwd = form.newPassword
  if (!pwd) return 0
  let score = 0
  if (pwd.length >= 8) score++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
  if (/\d/.test(pwd)) score++
  if (/[^a-zA-Z0-9]/.test(pwd)) score++
  return score
})

const strengthText = computed(() => {
  const texts = ['', '弱', '一般', '较强', '强']
  return texts[passwordStrength.value] || ''
})

const strengthColor = computed(() => {
  const colors = ['text-slate-400', 'text-error', 'text-amber-600', 'text-green-600', 'text-green-700']
  return colors[passwordStrength.value] || 'text-slate-400'
})

const sendSmsCode = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleVerify = () => {
  step.value = 2
}

const handleReset = () => {
  step.value = 3
  redirectTimer = setInterval(() => {
    redirectCountdown.value--
    if (redirectCountdown.value <= 0) {
      if (redirectTimer) clearInterval(redirectTimer)
      router.push('/login')
    }
  }, 1000)
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (redirectTimer) clearInterval(redirectTimer)
})
</script>

<style scoped>
.glass-panel {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-pattern {
  background-image:
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.06) 0%, transparent 40%),
    radial-gradient(circle at 40% 80%, rgba(255,255,255,0.05) 0%, transparent 45%);
}
</style>
