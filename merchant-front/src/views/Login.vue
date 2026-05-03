<template>
  <main class="min-h-screen flex flex-col md:flex-row overflow-hidden">
    <!-- Left Side: Brand Illustration (60%) -->
    <section class="relative hidden md:flex md:w-[60%] h-screen bg-primary overflow-hidden items-center justify-center">
      <!-- Background Image with Overlay -->
      <div class="absolute inset-0 z-0">
        <img alt="Fresh Organic Fruits" class="w-full h-full object-cover opacity-40 mix-blend-overlay" src="https://images.unsplash.com/photo-1619566636858-003a8348b1af?w=800&q=80" />
      </div>
      <!-- Branding Content -->
      <div class="relative z-10 flex flex-col items-center text-center px-12">
        <div class="mb-stack-lg p-6 glass-panel rounded-full shadow-2xl">
          <span class="material-symbols-outlined text-white text-6xl">restaurant</span>
        </div>
        <h1 class="font-h1 text-h1 text-white tracking-tight mb-stack-sm">鲜果记</h1>
        <p class="font-body-lg text-body-lg text-primary-fixed max-w-md">
          为现代鲜果商户与物流管理量身打造，融合自然活力与精准运营。
        </p>
        <!-- Decorative Bento Elements -->
        <div class="mt-12 grid grid-cols-2 gap-gutter w-full max-w-lg">
          <div class="glass-panel p-stack-md rounded-xl text-left border-white/10">
            <span class="material-symbols-outlined text-white mb-2">inventory_2</span>
            <div class="font-label-bold text-label-bold text-white">智能库存</div>
            <div class="font-body-sm text-body-sm text-white/70">实时保鲜周期追踪</div>
          </div>
          <div class="glass-panel p-stack-md rounded-xl text-left border-white/10">
            <span class="material-symbols-outlined text-white mb-2">trending_up</span>
            <div class="font-label-bold text-label-bold text-white">盈利分析</div>
            <div class="font-body-sm text-body-sm text-white/70">详尽的销售表现洞察</div>
          </div>
        </div>
      </div>
      <!-- Accent Curve -->
      <div class="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-primary to-transparent opacity-50"></div>
    </section>

    <!-- Right Side: Login Form (40%) -->
    <section class="flex flex-col w-full md:w-[40%] bg-white items-center justify-center px-container-padding-mobile md:px-container-padding-pc relative">
      <!-- Mobile Brand Logo -->
      <div class="md:hidden flex items-center gap-2 mb-stack-lg">
        <span class="material-symbols-outlined text-primary text-3xl">restaurant</span>
        <span class="font-h2 text-h2 text-primary">鲜果记</span>
      </div>

      <div class="w-full max-w-md">
        <div class="mb-stack-lg">
          <h2 class="font-h1 text-h1 text-on-surface mb-stack-xs">商家登录</h2>
          <p class="font-body-md text-body-md text-on-surface-variant">欢迎回来，请输入您的商户凭据以继续管理。</p>
        </div>

        <form class="space-y-stack-md" @submit.prevent="handleLogin">
          <!-- Account Input -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="account">账号</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">person</span>
              <input class="w-full pl-10 pr-4 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="account" placeholder="请输入商户账号" type="text" v-model="form.account" />
            </div>
          </div>

          <!-- Password Input -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="password">密码</label>
            <div class="relative">
              <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">lock</span>
              <input class="w-full pl-10 pr-4 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="password" placeholder="请输入登录密码" :type="showPassword ? 'text' : 'password'" v-model="form.password" />
              <button class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface transition-colors" type="button" @click="showPassword = !showPassword">
                <span class="material-symbols-outlined text-sm">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
          </div>

          <!-- Verification Code -->
          <div class="space-y-stack-xs">
            <label class="font-label-bold text-label-bold text-on-surface-variant" for="captcha">验证码</label>
            <div class="flex gap-stack-md">
              <div class="relative flex-1">
                <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline">verified_user</span>
                <input class="w-full pl-10 pr-4 h-12 border border-outline-variant rounded-lg focus:ring-2 focus:ring-primary focus:border-primary transition-all font-body-md text-body-md outline-none" id="captcha" placeholder="输入验证码" type="text" v-model="form.captcha" />
              </div>
              <div class="w-32 h-12 bg-surface-container rounded-lg border border-outline-variant overflow-hidden cursor-pointer flex items-center justify-center text-xs text-slate-400 font-bold">
                验证码图
              </div>
            </div>
          </div>

          <!-- Remember Me & Forgot Password -->
          <div class="flex items-center justify-between pt-stack-xs">
            <label class="flex items-center gap-2 cursor-pointer group">
              <input class="w-4 h-4 rounded border-outline-variant text-primary focus:ring-primary" type="checkbox" v-model="form.remember" />
              <span class="font-body-sm text-body-sm text-on-surface-variant group-hover:text-on-surface transition-colors">记住我</span>
            </label>
            <a class="font-body-sm text-body-sm text-primary hover:underline font-medium cursor-pointer">忘记密码？</a>
          </div>

          <!-- Login Button -->
          <button class="w-full h-12 bg-primary text-white rounded-lg font-label-bold text-label-bold hover:bg-primary/90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 shadow-md" type="submit">
            <span>登录</span>
            <span class="material-symbols-outlined text-sm">login</span>
          </button>
        </form>

        <!-- Additional Options -->
        <div class="mt-stack-lg text-center">
          <p class="font-body-sm text-body-sm text-on-surface-variant">
            还没有账号？ <a class="text-primary font-medium hover:underline cursor-pointer">申请商户入驻</a>
          </p>
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const showPassword = ref(false)
const form = reactive({
  account: '',
  password: '',
  captcha: '',
  remember: false
})

const handleLogin = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.glass-panel {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}
</style>
