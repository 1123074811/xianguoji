<template>
  <div>
    <!-- Breadcrumbs -->
    <nav class="flex items-center gap-2 text-xs text-slate-400 font-medium mb-4">
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/goods')">商品管理</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="cursor-pointer hover:text-primary" @click="$router.push('/goods')">所有商品</span>
      <span class="material-symbols-outlined text-sm">chevron_right</span>
      <span class="text-slate-600">{{ isEdit ? '编辑：' + form.name : '新增商品' }}</span>
    </nav>

    <div class="grid grid-cols-12 gap-gutter pb-24">
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
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none" type="text" v-model="form.name" placeholder="请输入商品名称" />
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">类目 *</label>
                <select class="w-full border border-outline-variant rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-primary focus:border-primary outline-none" v-model="form.categoryId">
                  <option :value="0" disabled>请选择类目</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>
              <div class="col-span-1">
                <label class="block font-label-bold text-label-bold text-on-surface mb-stack-xs">品牌</label>
                <input class="w-full border border-outline-variant rounded-lg px-4 py-2.5 outline-none focus:ring-primary" type="text" v-model="form.subtitle" placeholder="副标题（选填）" />
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-on-surface mb-4">轮播图 (最多 6 张) *</label>
              <div class="grid grid-cols-3 sm:grid-cols-6 gap-3">
                <div v-for="(img, idx) in carouselImages" :key="'c'+idx"
                  class="aspect-square rounded-lg border border-slate-200 overflow-hidden relative group">
                  <img :src="resolveImageUrl(img)" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span class="material-symbols-outlined text-white cursor-pointer" @click="removeCarousel(idx)">delete</span>
                  </div>
                  <span v-if="idx === 0" class="absolute top-1 left-1 text-[9px] bg-primary text-white px-1.5 py-0.5 rounded font-bold">主图</span>
                </div>
                <label v-if="carouselImages.length < 6"
                  class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 hover:border-primary hover:text-primary transition-colors cursor-pointer bg-slate-50">
                  <span class="material-symbols-outlined text-2xl">add_photo_alternate</span>
                  <span class="text-[10px] mt-1 font-bold uppercase">上传图片</span>
                  <input type="file" accept="image/*" multiple class="hidden" @change="onCarouselUpload" />
                </label>
              </div>
            </div>
            <div>
              <label class="block font-label-bold text-label-bold text-on-surface mb-4">详情图</label>
              <div class="grid grid-cols-3 sm:grid-cols-6 gap-3">
                <div v-for="(img, idx) in detailImages" :key="'d'+idx"
                  class="aspect-square rounded-lg border border-slate-200 overflow-hidden relative group">
                  <img :src="resolveImageUrl(img)" class="w-full h-full object-cover" />
                  <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span class="material-symbols-outlined text-white cursor-pointer" @click="removeDetail(idx)">delete</span>
                  </div>
                </div>
                <label
                  class="aspect-square rounded-lg border-2 border-dashed border-slate-200 flex flex-col items-center justify-center text-slate-400 hover:border-primary hover:text-primary transition-colors cursor-pointer bg-slate-50">
                  <span class="material-symbols-outlined text-2xl">add_photo_alternate</span>
                  <span class="text-[10px] mt-1 font-bold uppercase">详情图</span>
                  <input type="file" accept="image/*" multiple class="hidden" @change="onDetailUpload" />
                </label>
              </div>
            </div>
          </div>
        </section>

        <!-- Specs & Price Table Module -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="font-h3 text-h3 text-slate-900">价格与库存</h2>
            <button class="text-primary font-label-bold text-label-bold flex items-center gap-1" @click="addSku">
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
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">原价</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">库存</th>
                  <th class="px-6 py-3 font-table-header text-table-header text-slate-500 uppercase">默认</th>
                  <th class="px-6 py-3"></th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="(spec, idx) in skuList" :key="idx" class="hover:bg-slate-50 transition-colors h-[48px]">
                  <td class="px-6 py-2">
                    <input class="w-24 bg-transparent border-none p-0 focus:ring-0 text-sm font-medium outline-none" type="text" v-model="spec.specName" placeholder="规格名" />
                  </td>
                  <td class="px-6 py-2">
                    <div class="flex items-center gap-1">
                      <span class="text-slate-400 text-sm">¥</span>
                      <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" v-model="spec.price" />
                    </div>
                  </td>
                  <td class="px-6 py-2">
                    <div class="flex items-center gap-1">
                      <span class="text-slate-400 text-sm">¥</span>
                      <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="text" v-model="spec.originalPrice" />
                    </div>
                  </td>
                  <td class="px-6 py-2">
                    <input class="w-16 bg-transparent border-none p-0 focus:ring-0 text-sm outline-none" type="number" v-model.number="spec.stock" />
                  </td>
                  <td class="px-6 py-2 text-center">
                    <input type="radio" :name="'defaultSku'" :checked="spec.isDefault === 1" @change="setDefaultSku(idx)" class="text-primary" />
                  </td>
                  <td class="px-6 py-2 text-right">
                    <span class="material-symbols-outlined text-slate-300 hover:text-error cursor-pointer text-xl" @click="removeSku(idx)">do_not_disturb_on</span>
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
          <div class="p-6">
            <textarea class="w-full border border-outline-variant rounded-lg px-4 py-3 focus:ring-2 focus:ring-primary focus:border-primary transition-all outline-none text-sm min-h-[200px] resize-y" v-model="form.description" placeholder="请输入商品详情介绍"></textarea>
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
              <input class="sr-only peer" type="checkbox" v-model="groupBuy.enabled" />
              <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>
          <div class="space-y-4" v-if="groupBuy.enabled">
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">最低成团人数</label>
              <div class="flex items-center border border-slate-200 rounded-lg overflow-hidden">
                <button class="px-3 py-2 bg-slate-50 text-slate-500" @click="groupBuy.groupSize = Math.max(2, groupBuy.groupSize - 1)">-</button>
                <input class="flex-1 text-center border-none text-sm font-medium outline-none" type="number" v-model.number="groupBuy.groupSize" min="2" />
                <button class="px-3 py-2 bg-slate-50 text-slate-500" @click="groupBuy.groupSize++">+</button>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">拼团折扣 (%)</label>
              <input class="w-full border border-slate-200 rounded-lg px-4 py-2 text-sm outline-none" type="number" v-model.number="groupBuy.discountPercent" min="1" max="99" />
              <p class="text-[10px] text-slate-400 mt-1">基于默认SKU销售价计算，预计拼团价 ¥{{ computedGroupPrice }}</p>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">单团有效期（小时）</label>
              <input class="w-full border border-slate-200 rounded-lg px-4 py-2 text-sm outline-none" type="number" v-model.number="groupBuy.validHours" min="1" max="168" />
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-500 mb-1">活动结束时间</label>
              <input class="w-full border border-slate-200 rounded-lg px-4 py-2 text-sm outline-none" type="datetime-local" v-model="groupBuy.endTime" />
            </div>
          </div>
          <p v-else class="text-xs text-slate-400">启用拼团后可邀请好友共同下单获得专属优惠价格</p>
        </section>

        <!-- Delivery Settings Module -->
        <section class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
          <div class="flex items-center gap-2 mb-6">
            <span class="material-symbols-outlined text-primary">local_shipping</span>
            <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">配送选项</h3>
          </div>
          <div class="space-y-3">
            <label class="flex items-center p-3 border rounded-lg cursor-pointer transition-colors"
              :class="form.supportDelivery ? 'border-primary bg-primary/5' : 'border-slate-200 hover:bg-slate-50'">
              <input class="rounded text-primary focus:ring-primary" type="checkbox" v-model="form.supportDelivery" :true-value="1" :false-value="0" />
              <div class="ml-3">
                <p class="text-sm font-bold text-slate-900">同城配送</p>
                <p class="text-[10px] text-slate-500">通过配送员送达</p>
              </div>
            </label>
            <label class="flex items-center p-3 border rounded-lg cursor-pointer transition-colors"
              :class="form.supportPickup ? 'border-primary bg-primary/5' : 'border-slate-200 hover:bg-slate-50'">
              <input class="rounded text-primary focus:ring-primary" type="checkbox" v-model="form.supportPickup" :true-value="1" :false-value="0" />
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
                <input class="sr-only peer" type="checkbox" v-model="form.isRecommend" :true-value="1" :false-value="0" />
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
              <div class="text-3xl font-black">{{ healthScore }}%</div>
              <div class="text-[10px] font-bold uppercase py-1 px-2 bg-white/20 rounded">{{ healthLabel }}</div>
            </div>
            <div class="w-full bg-white/20 h-1 rounded-full overflow-hidden">
              <div class="bg-white h-full transition-all" :style="{ width: healthScore + '%' }"></div>
            </div>
            <p class="text-xs opacity-70 leading-relaxed">{{ healthTip }}</p>
          </div>
        </section>

        <!-- User-side Preview -->
        <section class="bg-white border border-slate-200 rounded-xl overflow-hidden shadow-sm">
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-primary">phone_iphone</span>
              <h3 class="font-label-bold text-label-bold text-slate-900 uppercase tracking-wide">用户端预览</h3>
            </div>
            <span class="text-[10px] text-slate-400">实时同步编辑内容</span>
          </div>
          <div class="p-4 flex justify-center bg-slate-50">
            <!-- Phone Frame -->
            <div class="phone-frame w-[280px] bg-white rounded-[28px] border-[3px] border-slate-800 overflow-hidden shadow-lg relative">
              <!-- Notch -->
              <div class="absolute top-0 left-1/2 -translate-x-1/2 w-24 h-5 bg-slate-800 rounded-b-xl z-10"></div>
              <!-- Screen Content -->
              <div class="h-[640px] overflow-y-auto bg-[#fcf9f8] text-left [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
                <div class="h-12 bg-white border-b border-slate-100 flex items-center justify-between px-3 pt-4">
                  <span class="material-symbols-outlined text-green-700" style="font-size:16px">arrow_back</span>
                  <span class="text-[11px] font-bold text-green-700">鲜果记</span>
                  <div class="flex items-center gap-2">
                    <span class="material-symbols-outlined text-green-700" style="font-size:16px">share</span>
                    <span class="material-symbols-outlined text-green-700" style="font-size:16px">chat</span>
                  </div>
                </div>
                <!-- Carousel -->
                <div class="relative w-full aspect-[4/3] bg-slate-100 overflow-hidden">
                  <div
                    v-if="previewCarouselImages.length"
                    class="flex w-full h-full transition-transform duration-500 ease-out"
                    :style="{ transform: `translateX(-${activePreviewCarouselIndex * 100}%)` }"
                  >
                    <img
                      v-for="(img, idx) in previewCarouselImages"
                      :key="'preview-carousel-' + idx"
                      :src="img"
                      class="w-full h-full object-cover flex-none"
                    />
                  </div>
                  <div v-else class="w-full h-full flex items-center justify-center">
                    <span class="material-symbols-outlined text-slate-300 text-4xl">image</span>
                  </div>
                  <div v-if="previewCarouselImages.length > 1" class="absolute bottom-2 right-2 bg-black/30 text-white text-[9px] px-1.5 py-0.5 rounded-full backdrop-blur-sm">{{ activePreviewCarouselIndex + 1 }}/{{ previewCarouselImages.length }}</div>
                </div>
                <!-- Price -->
                <div class="bg-white px-4 py-3 border-b border-slate-100">
                  <div v-if="previewHasGroupBuy" class="grid grid-cols-2 border-b border-slate-100 mb-3 text-[10px] font-semibold">
                    <div class="relative pb-2 text-center text-slate-400">单独购买</div>
                    <div class="relative pb-2 text-center text-green-700 after:absolute after:left-1/2 after:-translate-x-1/2 after:-bottom-px after:w-8 after:h-0.5 after:bg-green-700">发起拼团</div>
                  </div>
                  <div class="flex items-end justify-between gap-2">
                    <div class="flex items-baseline gap-1 min-w-0">
                    <span class="text-[10px] text-green-700 font-bold">¥</span>
                      <span class="text-xl text-green-700 font-black leading-none">{{ previewHasGroupBuy ? computedGroupPrice : previewPrice }}</span>
                    <span v-if="previewHasGroupBuy" class="text-[9px] text-slate-400 line-through ml-1">¥{{ previewPrice }}</span>
                    <span v-else-if="previewOriginalPrice" class="text-[9px] text-slate-400 line-through ml-1">¥{{ previewOriginalPrice }}</span>
                    </div>
                    <div class="text-[9px] text-slate-400 shrink-0">{{ previewHasGroupBuy ? `${groupBuy.groupSize}人成团` : '月销 0+' }}</div>
                  </div>
                </div>
                <!-- Title -->
                <div class="bg-white px-4 py-3 border-b border-slate-100">
                  <div class="text-xs font-bold text-slate-800 leading-snug">{{ form.name || '商品名称' }}</div>
                  <div v-if="form.subtitle" class="text-[10px] text-green-600 mt-1">{{ form.subtitle }}</div>
                </div>
                <!-- Specs -->
                <div v-if="skuList.length > 0 && skuList.some(s => s.specName)" class="bg-white px-4 py-3 border-b border-slate-100">
                  <div class="text-[10px] font-bold text-slate-700 mb-2">规格选择</div>
                  <div class="flex flex-wrap gap-2">
                    <span v-for="(spec, idx) in skuList.filter(s => s.specName)" :key="idx"
                      class="px-2.5 py-1 text-[9px] rounded-md border"
                      :class="idx === 0 ? 'border-green-700 bg-green-50 text-green-700' : 'border-slate-200 text-slate-500'">
                      {{ spec.specName }}
                    </span>
                  </div>
                </div>
                <!-- Delivery -->
                <div v-if="form.supportDelivery || form.supportPickup" class="bg-white px-4 py-3 border-b border-slate-100">
                  <div v-if="form.supportDelivery" class="flex items-center gap-1.5 mb-1.5">
                    <span class="material-symbols-outlined text-green-700" style="font-size:14px">local_shipping</span>
                    <span class="text-[9px] text-slate-600">同城配送</span>
                  </div>
                  <div v-if="form.supportPickup" class="flex items-center gap-1.5">
                    <span class="material-symbols-outlined text-green-700" style="font-size:14px">storefront</span>
                    <span class="text-[9px] text-slate-600">支持自提</span>
                  </div>
                </div>
                <!-- Reviews -->
                <div class="bg-white px-4 py-3 border-b border-slate-100">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-[10px] font-bold text-slate-800">用户评价 (1)</span>
                    <div class="flex items-center gap-0.5 text-[9px] text-slate-500">
                      <span class="text-amber-500">★</span>
                      <span>满意度 100%</span>
                    </div>
                  </div>
                  <div class="rounded-lg bg-[#fcf9f8] px-3 py-2">
                    <div class="flex items-center justify-between mb-1">
                      <div class="flex items-center gap-1.5">
                        <div class="w-5 h-5 rounded-full bg-green-100 text-green-700 text-[9px] font-bold flex items-center justify-center">鲜</div>
                        <span class="text-[9px] font-semibold text-slate-700">鲜果达人</span>
                      </div>
                      <span class="text-[8px] text-slate-400">刚刚</span>
                    </div>
                    <p class="text-[9px] text-slate-600 leading-relaxed line-clamp-2">果子很新鲜，包装也很完整，口感清甜多汁，会继续回购。</p>
                  </div>
                </div>
                <!-- Description -->
                <div v-if="form.description" class="px-4 py-3">
                  <div class="flex items-center gap-1 mb-1.5">
                    <div class="w-0.5 h-3 bg-green-700 rounded"></div>
                    <span class="text-[10px] font-semibold text-slate-700">产品详情</span>
                  </div>
                  <p class="text-[9px] text-slate-600 leading-relaxed line-clamp-4">{{ form.description }}</p>
                </div>
                <!-- Detail Images -->
                <div v-if="detailImages.length > 0" class="px-3 pb-3 space-y-1.5">
                  <img v-for="(img, idx) in detailImages.slice(0, 3)" :key="idx" :src="resolveImageUrl(img)" class="w-full rounded-lg" />
                  <div v-if="detailImages.length > 3" class="text-[9px] text-slate-400 text-center">还有 {{ detailImages.length - 3 }} 张详情图...</div>
                </div>
                <!-- Bottom Bar Preview -->
                <div class="sticky bottom-0 bg-white border-t border-slate-100 px-3 py-2 flex items-center gap-2">
                  <div class="flex items-center gap-2 shrink-0">
                    <div class="flex flex-col items-center gap-0.5">
                      <span class="material-symbols-outlined text-slate-400" style="font-size:15px">chat</span>
                      <span class="text-[7px] text-slate-400 leading-none">客服</span>
                    </div>
                    <div class="flex flex-col items-center gap-0.5">
                      <span class="material-symbols-outlined text-slate-400" style="font-size:15px">shopping_cart</span>
                      <span class="text-[7px] text-slate-400 leading-none">购物车</span>
                    </div>
                  </div>
                  <div class="flex-1 min-w-0 flex gap-1.5">
                    <div class="flex-1 min-w-0 bg-[#fcf9f8] text-slate-700 text-[8px] font-semibold rounded-full h-7 leading-7 text-center border border-slate-200 whitespace-nowrap overflow-hidden">{{ previewHasGroupBuy ? '拼团专区' : '加入购物车' }}</div>
                    <div class="flex-[1.35] min-w-0 bg-green-700 text-white text-[8px] font-semibold rounded-full h-7 leading-7 text-center whitespace-nowrap overflow-hidden">{{ previewHasGroupBuy ? `¥${computedGroupPrice} 发起拼团` : '立即购买' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- Fixed Bottom Action Bar -->
    <footer class="fixed bottom-0 right-0 left-60 h-20 bg-white border-t border-slate-200 px-gutter flex items-center justify-between z-50">
      <div class="flex items-center gap-2 text-slate-400">
        <span class="material-symbols-outlined text-lg">history</span>
        <span class="text-xs font-medium italic" v-if="saving">保存中...</span>
      </div>
      <div class="flex items-center gap-gutter">
        <button class="px-6 py-2.5 rounded-lg border border-slate-200 text-slate-600 font-label-bold text-label-bold hover:bg-slate-50 active:scale-95 transition-all" @click="goBack">取消</button>
        <button class="px-6 py-2.5 rounded-lg border border-primary text-primary font-label-bold text-label-bold hover:bg-primary/5 active:scale-95 transition-all" :disabled="saving" @click="saveDraft">保存草稿</button>
        <button class="px-10 py-2.5 rounded-lg bg-primary text-white font-label-bold text-label-bold shadow-md hover:bg-primary-container active:scale-95 transition-all" :disabled="saving" @click="publish">发布商品</button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminCatalogApi } from '@/api/modules/catalog'
import { adminPromoApi } from '@/api/modules/promo'
import { request } from '@/api/request'
import { resolveImageUrl } from '@/utils/image'
import type { AdminCategoryVO, AdminProductDetailVO } from '@/api/types/catalog'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const productId = computed(() => Number(route.params.id) || 0)
const loading = ref(false)
const saving = ref(false)

// ---- 表单数据 ----
const form = ref({
  name: '',
  subtitle: '',
  categoryId: 0,
  mainImage: '',
  description: '',
  videoUrl: '',
  isRecommend: 0 as 0 | 1,
  supportDelivery: 1 as 0 | 1,
  supportPickup: 1 as 0 | 1,
  status: 1 as 0 | 1,
})

const carouselImages = ref<string[]>([])
const detailImages = ref<string[]>([])
const previewCarouselIndex = ref(0)
let previewCarouselTimer: number | undefined
const groupBuy = reactive({
  enabled: false,
  activityId: 0 as number,
  groupSize: 3,
  discountPercent: 15,
  validHours: 24,
  endTime: '',
  skuId: 0 as number,
  loadedGroupPrice: '' as string,
})

const computedGroupPrice = computed(() => {
  const defaultSku = skuList.value.find(s => s.isDefault === 1) || skuList.value[0]
  const base = Number(defaultSku?.price || 0)
  if (!base) return '0.00'
  const ratio = (100 - Math.min(99, Math.max(1, groupBuy.discountPercent))) / 100
  return (base * ratio).toFixed(2)
})

interface SkuItem {
  id?: number
  specName: string
  price: string
  originalPrice: string
  stock: number
  isDefault: 0 | 1
  _deleted?: boolean
}

const skuList = ref<SkuItem[]>([{ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 1 }])

const categories = ref<AdminCategoryVO[]>([])

const healthScore = computed(() => {
  let score = 0
  if (form.value.name) score += 20
  if (form.value.categoryId && form.value.categoryId > 0) score += 15
  if (carouselImages.value.length > 0) score += 20
  if (skuList.value.some(s => s.specName && s.price)) score += 20
  if (form.value.description) score += 15
  if (detailImages.value.length > 0) score += 10
  return Math.min(score, 100)
})

const healthLabel = computed(() => {
  const s = healthScore.value
  if (s >= 90) return '表现优异'
  if (s >= 70) return '表现良好'
  if (s >= 50) return '需要完善'
  return '信息不足'
})

const healthTip = computed(() => {
  const missing: string[] = []
  if (!form.value.name) missing.push('商品名称')
  if (!form.value.categoryId || form.value.categoryId <= 0) missing.push('类目')
  if (carouselImages.value.length === 0) missing.push('轮播图')
  if (!skuList.value.some(s => s.specName && s.price)) missing.push('规格价格')
  if (!form.value.description) missing.push('商品描述')
  if (missing.length === 0) return '商品信息完善，搜索可见度已最大化！'
  return `补充${missing.slice(0, 2).join('、')}可提升搜索可见度`
})

// ---- 用户端预览 ----
const previewMainImage = computed(() => {
  const img = carouselImages.value[0] || form.value.mainImage
  return img ? resolveImageUrl(img) : ''
})

const previewCarouselImages = computed(() => {
  const images = carouselImages.value.length ? carouselImages.value : (form.value.mainImage ? [form.value.mainImage] : [])
  return images.map(resolveImageUrl)
})

const activePreviewCarouselIndex = computed(() => {
  const len = previewCarouselImages.value.length
  return len ? previewCarouselIndex.value % len : 0
})

const previewPrice = computed(() => {
  const defaultSku = skuList.value.find(s => s.isDefault === 1) || skuList.value[0]
  if (defaultSku?.price) return defaultSku.price
  if (skuList.value.length > 0) {
    const prices = skuList.value.map(s => Number(s.price)).filter(p => p > 0)
    if (prices.length > 0) return Math.min(...prices).toFixed(2)
  }
  return '0.00'
})

const previewOriginalPrice = computed(() => {
  const defaultSku = skuList.value.find(s => s.isDefault === 1) || skuList.value[0]
  if (defaultSku?.originalPrice && Number(defaultSku.originalPrice) > Number(defaultSku?.price || 0)) {
    return defaultSku.originalPrice
  }
  return ''
})

const previewHasGroupBuy = computed(() => {
  const defaultSku = skuList.value.find(s => s.isDefault === 1) || skuList.value[0]
  return groupBuy.enabled && !!defaultSku?.specName && Number(defaultSku?.price || 0) > 0
})

// ---- 加载数据 ----
async function loadCategories() {
  try {
    categories.value = await adminCatalogApi.categoryList()
  } catch (e) { console.warn('加载分类失败', e) }
}

async function loadProduct() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data: AdminProductDetailVO = await adminCatalogApi.productDetail(productId.value)
    form.value.name = data.name
    form.value.subtitle = data.subtitle
    form.value.categoryId = data.categoryId
    form.value.mainImage = data.mainImage
    form.value.description = data.description
    form.value.videoUrl = data.videoUrl || ''
    form.value.isRecommend = data.isRecommend
    form.value.supportDelivery = data.supportDelivery ?? 1
    form.value.supportPickup = data.supportPickup ?? 1
    form.value.status = data.status
    carouselImages.value = data.carouselImages || []
    detailImages.value = data.detailImages || []
    skuList.value = (data.skuList || []).map(s => ({
      id: s.id,
      specName: s.specName,
      price: String(s.price),
      originalPrice: String(s.originalPrice),
      stock: s.stock,
      isDefault: s.isDefault,
    }))
    if (skuList.value.length === 0) {
      skuList.value.push({ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 1 })
    }
    await loadGroupBuy()
  } catch (e) { console.warn('加载商品失败', e) }
  finally { loading.value = false }
}

async function loadGroupBuy() {
  if (!productId.value) return
  try {
    const activity = await adminPromoApi.groupBuyByProduct(productId.value)
    if (!activity) return
    groupBuy.activityId = activity.id
    groupBuy.groupSize = activity.groupSize
    groupBuy.validHours = activity.validHours
    groupBuy.skuId = activity.skuId
    groupBuy.loadedGroupPrice = String(activity.groupPrice || '')
    groupBuy.endTime = activity.endTime ? activity.endTime.slice(0, 16) : ''
    groupBuy.enabled = activity.status === 1
    // 反推折扣（仅展示用）
    const defaultSku = skuList.value.find(s => s.isDefault === 1) || skuList.value[0]
    const base = Number(defaultSku?.price || 0)
    const gp = Number(activity.groupPrice || 0)
    if (base > 0 && gp > 0) {
      groupBuy.discountPercent = Math.max(1, Math.min(99, Math.round((1 - gp / base) * 100)))
    }
  } catch (e) { console.warn('加载拼团活动失败', e) }
}

async function saveGroupBuy(savedProductId: number) {
  if (!savedProductId) return
  const defaultSku = skuList.value.find(s => s.isDefault === 1 && s.id) || skuList.value.find(s => s.id)
  const skuId = defaultSku?.id || groupBuy.skuId
  if (!groupBuy.enabled) {
    if (groupBuy.activityId) {
      await adminPromoApi.updateGroupBuy(groupBuy.activityId, { status: 0 })
    }
    return
  }
  if (!skuId) {
    throw new Error('请先保存SKU再启用拼团')
  }
  const groupPrice = computedGroupPrice.value
  const now = new Date()
  const startTime = formatLocalDateTime(now)
  const endTime = groupBuy.endTime
    ? `${groupBuy.endTime}:00`
    : formatLocalDateTime(new Date(now.getTime() + 7 * 24 * 3600 * 1000))
  const payload = {
    productId: savedProductId,
    skuId,
    groupPrice,
    groupSize: Math.max(2, groupBuy.groupSize),
    validHours: Math.max(1, groupBuy.validHours),
    startTime,
    endTime,
    status: 1,
  }
  if (groupBuy.activityId) {
    await adminPromoApi.updateGroupBuy(groupBuy.activityId, payload)
  } else {
    await adminPromoApi.createGroupBuy(payload)
    await loadGroupBuy()
  }
}

function formatLocalDateTime(date: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

async function refreshSavedSkuList(savedProductId: number) {
  const data: AdminProductDetailVO = await adminCatalogApi.productDetail(savedProductId)
  skuList.value = (data.skuList || []).map(s => ({
    id: s.id,
    specName: s.specName,
    price: String(s.price),
    originalPrice: String(s.originalPrice),
    stock: s.stock,
    isDefault: s.isDefault,
  }))
  if (skuList.value.length === 0) {
    skuList.value.push({ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 1 })
  }
}

// ---- 图片上传 ----
async function uploadImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const url = await request<string>({
    url: '/api/admin/file/upload',
    method: 'POST',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return url
}

async function onCarouselUpload(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  const files = Array.from(input.files).slice(0, Math.max(0, 6 - carouselImages.value.length))
  for (const file of files) {
    try {
      const url = await uploadImage(file)
      carouselImages.value.push(url)
      if (!form.value.mainImage) form.value.mainImage = url
    } catch (e) { console.warn('上传失败', e) }
  }
  input.value = ''
}

async function onDetailUpload(e: Event) {
  const input = e.target as HTMLInputElement
  if (!input.files?.length) return
  for (const file of Array.from(input.files)) {
    try {
      const url = await uploadImage(file)
      detailImages.value.push(url)
    } catch (e) { console.warn('上传失败', e) }
  }
  input.value = ''
}

function removeCarousel(idx: number) {
  carouselImages.value.splice(idx, 1)
  if (form.value.mainImage === carouselImages.value[idx]) {
    form.value.mainImage = carouselImages.value[0] || ''
  }
}

function removeDetail(idx: number) {
  detailImages.value.splice(idx, 1)
}

// ---- SKU 操作 ----
function addSku() {
  skuList.value.push({ specName: '', price: '', originalPrice: '', stock: 0, isDefault: 0 })
}

function removeSku(idx: number) {
  skuList.value.splice(idx, 1)
}

function setDefaultSku(idx: number) {
  skuList.value.forEach((s, i) => { s.isDefault = (i === idx ? 1 : 0) as 0 | 1 })
}

// ---- 保存 ----
function buildPayload(statusOverride?: number) {
  const images = [
    ...carouselImages.value.map(url => ({ url, type: 1 })),
    ...detailImages.value.map(url => ({ url, type: 2 })),
  ]
  return {
    name: form.value.name,
    subtitle: form.value.subtitle,
    categoryId: form.value.categoryId,
    mainImage: form.value.mainImage || carouselImages.value[0] || '',
    description: form.value.description,
    videoUrl: form.value.videoUrl || undefined,
    isRecommend: form.value.isRecommend,
    supportDelivery: form.value.supportDelivery,
    supportPickup: form.value.supportPickup,
    skus: skuList.value.filter(s => s.specName).map(s => ({
      specName: s.specName,
      price: s.price,
      originalPrice: s.originalPrice || undefined,
      stock: s.stock,
      isDefault: s.isDefault,
    })),
    images,
    status: statusOverride ?? form.value.status,
  }
}

async function saveDraft() {
  saving.value = true
  try {
    const payload = buildPayload(0)
    let savedId = productId.value
    if (isEdit.value) {
      await adminCatalogApi.updateProduct(productId.value, payload)
    } else {
      const res = await adminCatalogApi.createProduct(payload as any)
      savedId = res?.id || 0
    }
    if (savedId) {
      await refreshSavedSkuList(savedId)
      await saveGroupBuy(savedId)
    }
    toast.success('草稿已保存')
    router.push('/goods')
  } catch (e) {
    console.warn('保存失败', e)
    toast.error('保存失败，请稍后重试')
  } finally { saving.value = false }
}

async function publish() {
  saving.value = true
  try {
    const payload = buildPayload(1)
    let savedId = productId.value
    if (isEdit.value) {
      await adminCatalogApi.updateProduct(productId.value, payload)
    } else {
      const res = await adminCatalogApi.createProduct(payload as any)
      savedId = res?.id || 0
    }
    if (savedId) {
      await refreshSavedSkuList(savedId)
      await saveGroupBuy(savedId)
    }
    toast.success(isEdit.value ? '商品已更新' : '商品已发布上架')
    router.push('/goods')
  } catch (e) {
    console.warn('发布失败', e)
    toast.error('发布失败，请稍后重试')
  } finally { saving.value = false }
}

function goBack() {
  router.push('/goods')
}

onMounted(() => {
  loadCategories()
  loadProduct()
  previewCarouselTimer = window.setInterval(() => {
    if (previewCarouselImages.value.length > 1) {
      previewCarouselIndex.value = (previewCarouselIndex.value + 1) % previewCarouselImages.value.length
    }
  }, 2500)
})

onUnmounted(() => {
  if (previewCarouselTimer) window.clearInterval(previewCarouselTimer)
})
</script>
