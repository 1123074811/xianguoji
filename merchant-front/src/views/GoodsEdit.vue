<template>
  <div>
    <!-- Breadcrumbs -->
    <nav class="flex items-center gap-2 text-xs text-slate-400 font-medium mb-4">
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/goods')">商品管理</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/goods')">所有商品</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="text-slate-600">编辑：季节性优质富士苹果</span>
    </nav>

    <div class="grid grid-cols-12 gap-gutter">
      <!-- Left Column: Primary Details -->
      <div class="col-span-12 lg:col-span-8 space-y-gutter">
        <!-- Basic Info Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="font-h3 text-h3 text-slate-900">基本信息</h2>
            <span class="text-xs font-medium text-slate-400">必填项以 * 标记</span>
          </div>
          <div class="p-6 space-y-6">
            <div class="grid grid-cols-2 gap-stack-lg">
              <div class="col-span-2">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">商品名称 *</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" value="季节性优质富士苹果 - 礼盒装" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">类目 *</label>
                <select class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary outline-none">
                  <option>精品水果</option>
                  <option>柑橘类</option>
                  <option>莓果类</option>
                  <option>礼盒系列</option>
                </select>
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">品牌</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 outline-none focus:ring-primary" type="text" value="青森采摘" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-on-surface mb-4">商品图片 (最多 6 张) *</label>
              <div class="grid grid-cols-3 sm:grid-cols-6 gap-3">
                <div class="aspect-square rounded-lg border-2 border-dashed border-primary bg-primary-container/10 flex items-center justify-center relative group overflow-hidden">
                  <div class="w-full h-full bg-slate-100 flex items-center justify-center">
                    <span class="material-symbols-outlined text-slate-400 text-2xl">nutrition</span>
                  </div>
                  <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                    <span class="material-symbols-outlined text-white cursor-pointer">delete</span>
                  </div>
                </div>
                <div class="aspect-square rounded-lg border border-slate-200 overflow-hidden relative group">
                  <div class="w-full h-full bg-slate-100 flex items-center justify-center">
                    <span class="material-symbols-outlined text-slate-400 text-2xl">nutrition</span>
                  </div>
                  <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span class="material-symbols-outlined text-white cursor-pointer">delete</span>
                  </div>
                </div>
                <div class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 hover:border-primary hover:text-primary transition-colors cursor-pointer bg-slate-50">
                  <span class="material-symbols-outlined text-2xl">add_photo_alternate</span>
                  <span class="text-[10px] mt-1 font-bold uppercase">上传图片</span>
                </div>
                <div class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 bg-slate-50"></div>
                <div class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 bg-slate-50"></div>
                <div class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 bg-slate-50"></div>
              </div>
            </div>
          </div>
        </section>

        <!-- Specs & Price Table Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="font-h3 text-h3 text-slate-900">价格与库存</h2>
            <button class="text-primary font-label-bold text-label-bold flex items-center gap-1">
              <span class="material-symbols-outlined text-lg">add</span>
              添加规格
            </button>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead class="bg-slate-50 border-b border-slate-200">
                <tr>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">规格名称</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">销售价</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">成本价</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">库存</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">SKU 编码</th>
                  <th class="px-6 py-3"></th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="spec in specs" :key="spec.sku" class="hover:bg-slate-50 transition-colors h-[48px]">
                  <td class="px-6 py-2">
                    <input class="w-24 bg-transparent border-none p-0 focus:ring-0 text-sm font-medium outline-none" type="text" :value="spec.name" />
                  </td>
                  <td class="px-6 py-2">
                    <div class="flex items-center gap-1">
                      <span class="text-slate-400 text-sm">¥</span>
                      <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" :value="spec.price" />
                    </div>
                  </td>
                  <td class="px-6 py-2">
                    <div class="flex items-center gap-1">
                      <span class="text-slate-400 text-sm">¥</span>
                      <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" :value="spec.cost" />
                    </div>
                  </td>
                  <td class="px-6 py-2">
                    <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" :value="spec.stock" />
                  </td>
                  <td class="px-6 py-2 text-slate-500 text-sm">{{ spec.sku }}</td>
                  <td class="px-6 py-2 text-right">
                    <span class="material-symbols-outlined text-slate-300 hover:text-error cursor-pointer text-xl">do_not_disturb_on</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- Product Intro Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-h3 text-h3 text-slate-900">商品详情介绍</h2>
          </div>
          <div class="p-6 space-y-4">
            <div class="border border-slate-200 rounded-lg overflow-hidden">
              <div class="bg-slate-50 px-4 py-2 border-b border-slate-200 flex items-center gap-4 text-slate-500">
                <span class="material-symbols-outlined cursor-pointer hover:text-primary">format_bold</span>
                <span class="material-symbols-outlined cursor-pointer hover:text-primary">format_italic</span>
                <span class="material-symbols-outlined cursor-pointer hover:text-primary">format_list_bulleted</span>
                <span class="w-[1px] h-4 bg-slate-300"></span>
                <span class="material-symbols-outlined cursor-pointer hover:text-primary">image</span>
                <span class="material-symbols-outlined cursor-pointer hover:text-primary">link</span>
              </div>
              <div class="p-4 min-h-[200px] text-on-surface text-sm leading-relaxed">
                <p class="mb-4">我们的优质富士苹果直接源自青森县的纯净果园。这些苹果以其惊人的脆度和完美的酸甜比而闻名，是为我们的独家礼盒装手工精选的。</p>
                <div class="w-full h-48 bg-slate-100 rounded-lg flex items-center justify-center my-4">
                  <span class="material-symbols-outlined text-slate-300 text-4xl">image</span>
                </div>
                <p>无论是作为礼品还是个人享用，每个苹果都由可持续泡沫套保护，以确保其以完美的状态送达。</p>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- Right Column: Settings & Toggles -->
      <div class="col-span-12 lg:col-span-4 space-y-gutter">
        <!-- Group Buy Module -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">groups</span>
              <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">拼团设置</h3>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input checked class="sr-only peer" type="checkbox" />
              <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">最低成团人数</label>
              <div class="flex items-center border border-slate-200 rounded-lg overflow-hidden">
                <button class="px-3 py-2 bg-slate-50 text-slate-500">-</button>
                <input class="flex-1 text-center border-none text-sm font-medium outline-none" type="number" value="5" />
                <button class="px-3 py-2 bg-slate-50 text-slate-500">+</button>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">拼团折扣 (%)</label>
              <input class="w-full border border-slate-200 rounded-lg px-4 py-2 text-sm outline-none" type="text" value="15" />
            </div>
          </div>
        </section>

        <!-- Delivery Settings Module -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <div class="flex items-center gap-2 mb-6">
            <span class="material-symbols-outlined text-primary">local_shipping</span>
            <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">配送选项</h3>
          </div>
          <div class="space-y-3">
            <label class="flex items-center p-3 border border-primary bg-primary/5 rounded-lg cursor-pointer">
              <input checked class="rounded text-primary focus:ring-primary" type="checkbox" />
              <div class="ml-3">
                <p class="text-sm font-bold text-slate-900">标准快递</p>
                <p class="text-[10px] text-slate-500">通过物流合作伙伴寄送</p>
              </div>
            </label>
            <label class="flex items-center p-3 border border-slate-200 rounded-lg cursor-pointer hover:bg-slate-50 transition-colors">
              <input class="rounded text-primary focus:ring-primary" type="checkbox" />
              <div class="ml-3">
                <p class="text-sm font-bold text-slate-900">到店自提</p>
                <p class="text-[10px] text-slate-500">从商家门店地址取货</p>
              </div>
            </label>
          </div>
          <div class="mt-6 pt-6 border-t border-slate-100">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <span class="material-symbols-outlined text-primary text-xl">stars</span>
                <span class="text-sm font-bold text-slate-700">店长推荐</span>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input class="sr-only peer" type="checkbox" />
                <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
              </label>
            </div>
          </div>
        </section>

        <!-- Quick Stats Widget -->
        <section class="bg-gradient-to-br from-green-700 to-green-900 rounded-xl p-6 text-white shadow-lg">
          <div class="flex items-center justify-between mb-4">
            <span class="text-xs font-bold uppercase tracking-widest opacity-80">商品健康度</span>
            <span class="material-symbols-outlined opacity-80">info</span>
          </div>
          <div class="space-y-4">
            <div class="flex items-end justify-between">
              <div class="text-3xl font-black">94%</div>
              <div class="text-[10px] font-bold uppercase py-1 px-2 bg-white/20 rounded">表现优异</div>
            </div>
            <div class="w-full bg-white/20 h-1 rounded-full overflow-hidden">
              <div class="bg-white w-[94%] h-full"></div>
            </div>
            <p class="text-xs opacity-70 leading-relaxed">完善详细的产品描述可以将您的搜索可见度提升 12%。</p>
          </div>
        </section>
      </div>
    </div>

    <!-- Fixed Bottom Action Bar -->
    <footer class="fixed bottom-0 right-0 left-60 h-20 bg-white border-t border-slate-200 px-gutter flex items-center justify-between z-50">
      <div class="flex items-center gap-2 text-slate-400">
        <span class="material-symbols-outlined text-lg">history</span>
        <span class="text-xs font-medium italic">最后修改于 2 分钟前，由 Admin 操作</span>
      </div>
      <div class="flex items-center gap-gutter">
        <button class="px-6 py-2.5 rounded-lg border border-slate-200 text-slate-600 font-label-bold text-label-bold hover:bg-slate-50 active:scale-95 transition-all">取消</button>
        <button class="px-6 py-2.5 rounded-lg border border-primary text-primary font-label-bold text-label-bold hover:bg-primary/5 active:scale-95 transition-all">保存草稿</button>
        <button class="px-10 py-2.5 rounded-lg bg-primary text-white font-label-bold text-label-bold shadow-md hover:bg-primary-container active:scale-95 transition-all">发布商品</button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
const specs = [
  { name: '6枚装礼盒', price: '168.00', cost: '85.00', stock: '150', sku: 'FA-006-PREM' },
  { name: '12枚装礼盒', price: '298.00', cost: '150.00', stock: '85', sku: 'FA-012-PREM' }
]
</script>
