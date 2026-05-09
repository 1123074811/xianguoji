<template>
  <div class="rich-editor border border-slate-200 rounded-lg overflow-hidden">
    <!-- Toolbar -->
    <div class="toolbar flex items-center flex-wrap gap-0.5 px-2 py-1.5 bg-slate-50 border-b border-slate-200">
      <!-- Undo / Redo -->
      <button type="button" class="toolbar-btn" @click="exec('undo')" title="撤销">
        <span class="material-symbols-outlined text-base">undo</span>
      </button>
      <button type="button" class="toolbar-btn" @click="exec('redo')" title="重做">
        <span class="material-symbols-outlined text-base">redo</span>
      </button>
      <div class="w-px h-4 bg-slate-300 mx-1"></div>
      <!-- Text style -->
      <button type="button" class="toolbar-btn" :class="{ active: isFormat('bold') }" @click="exec('bold')" title="加粗">
        <span class="material-symbols-outlined text-base">format_bold</span>
      </button>
      <button type="button" class="toolbar-btn" :class="{ active: isFormat('italic') }" @click="exec('italic')" title="斜体">
        <span class="material-symbols-outlined text-base">format_italic</span>
      </button>
      <button type="button" class="toolbar-btn" :class="{ active: isFormat('underline') }" @click="exec('underline')" title="下划线">
        <span class="material-symbols-outlined text-base">format_underlined</span>
      </button>
      <button type="button" class="toolbar-btn" :class="{ active: isFormat('strikeThrough') }" @click="exec('strikeThrough')" title="删除线">
        <span class="material-symbols-outlined text-base">strikethrough_s</span>
      </button>
      <div class="w-px h-4 bg-slate-300 mx-1"></div>
      <!-- Block format -->
      <button type="button" class="toolbar-btn" :class="{ active: isTag('H3') }" @click="formatBlock('H3')" title="标题">
        <span class="text-xs font-bold">H3</span>
      </button>
      <button type="button" class="toolbar-btn" :class="{ active: isTag('P') }" @click="formatBlock('P')" title="段落">
        <span class="material-symbols-outlined text-base">format_paragraph</span>
      </button>
      <button type="button" class="toolbar-btn" :class="{ active: isTag('BLOCKQUOTE') }" @click="formatBlock('BLOCKQUOTE')" title="引用">
        <span class="material-symbols-outlined text-base">format_quote</span>
      </button>
      <div class="w-px h-4 bg-slate-300 mx-1"></div>
      <!-- List -->
      <button type="button" class="toolbar-btn" :class="{ active: isList('UL') }" @click="exec('insertUnorderedList')" title="无序列表">
        <span class="material-symbols-outlined text-base">format_list_bulleted</span>
      </button>
      <button type="button" class="toolbar-btn" :class="{ active: isList('OL') }" @click="exec('insertOrderedList')" title="有序列表">
        <span class="material-symbols-outlined text-base">format_list_numbered</span>
      </button>
      <div class="w-px h-4 bg-slate-300 mx-1"></div>
      <!-- Alignment -->
      <button type="button" class="toolbar-btn" @click="exec('justifyLeft')" title="左对齐">
        <span class="material-symbols-outlined text-base">format_align_left</span>
      </button>
      <button type="button" class="toolbar-btn" @click="exec('justifyCenter')" title="居中">
        <span class="material-symbols-outlined text-base">format_align_center</span>
      </button>
      <button type="button" class="toolbar-btn" @click="exec('justifyRight')" title="右对齐">
        <span class="material-symbols-outlined text-base">format_align_right</span>
      </button>
      <div class="w-px h-4 bg-slate-300 mx-1"></div>
      <!-- Link -->
      <button type="button" class="toolbar-btn" @click="showLinkModal = true" title="插入链接">
        <span class="material-symbols-outlined text-base">link</span>
      </button>
      <!-- Image -->
      <button type="button" class="toolbar-btn" @click="triggerImageUpload" title="插入图片">
        <span class="material-symbols-outlined text-base">image</span>
      </button>
      <input ref="fileInputRef" type="file" accept="image/*" class="hidden" @change="onImageUpload" />
      <!-- HR -->
      <button type="button" class="toolbar-btn" @click="insertHr()" title="分割线">
        <span class="material-symbols-outlined text-base">horizontal_rule</span>
      </button>
      <!-- Remove format -->
      <button type="button" class="toolbar-btn" @click="exec('removeFormat')" title="清除格式">
        <span class="material-symbols-outlined text-base">format_clear</span>
      </button>
    </div>

    <!-- Link Modal -->
    <div v-if="showLinkModal" class="absolute inset-0 z-10 flex items-center justify-center bg-black/20">
      <div class="bg-white rounded-lg shadow-lg p-4 w-72 space-y-3">
        <p class="text-sm font-semibold text-slate-700">插入链接</p>
        <input v-model="linkUrl" type="url" placeholder="https://..." class="w-full px-3 py-1.5 border border-slate-200 rounded text-sm outline-none focus:ring-1 focus:ring-primary" />
        <div class="flex justify-end gap-2">
          <button @click="showLinkModal = false; linkUrl = ''" class="px-3 py-1 text-xs text-slate-500 hover:text-slate-700">取消</button>
          <button @click="insertLink()" class="px-3 py-1 text-xs bg-primary text-white rounded hover:opacity-90">确定</button>
        </div>
      </div>
    </div>

    <!-- Editable Area -->
    <div
      ref="editorRef"
      class="editor-content px-4 py-3 text-sm leading-relaxed min-h-[200px] max-h-[500px] overflow-y-auto outline-none relative"
      contenteditable="true"
      @input="onInput"
      @blur="onBlur"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { request } from '@/api/request'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = ref<HTMLDivElement>()
const fileInputRef = ref<HTMLInputElement>()
const isFocused = ref(false)
const showLinkModal = ref(false)
const linkUrl = ref('')
const imageUploading = ref(false)

function onInput() {
  const html = editorRef.value?.innerHTML || ''
  emit('update:modelValue', html)
}

function onBlur() {
  isFocused.value = false
}

function exec(command: string, value: string | undefined = undefined) {
  document.execCommand(command, false, value)
  onInput()
  editorRef.value?.focus()
}

function formatBlock(tag: string) {
  document.execCommand('formatBlock', false, tag)
  onInput()
  editorRef.value?.focus()
}

function isFormat(command: string) {
  try {
    return document.queryCommandState(command)
  } catch {
    return false
  }
}

function isTag(tag: string) {
  const sel = window.getSelection()
  if (!sel || sel.rangeCount === 0) return false
  let node = sel.getRangeAt(0).commonAncestorContainer as Node | null
  if (node.nodeType === Node.TEXT_NODE) node = node.parentElement
  while (node && node !== editorRef.value) {
    if ((node as HTMLElement).tagName === tag) return true
    node = node.parentElement
  }
  return false
}

function isList(tag: string) {
  const sel = window.getSelection()
  if (!sel || sel.rangeCount === 0) return false
  let node = sel.getRangeAt(0).commonAncestorContainer as Node | null
  if (node.nodeType === Node.TEXT_NODE) node = node.parentElement
  while (node && node !== editorRef.value) {
    if ((node as HTMLElement).tagName === 'UL' && tag === 'UL') return true
    if ((node as HTMLElement).tagName === 'OL' && tag === 'OL') return true
    node = node.parentElement
  }
  return false
}

function insertLink() {
  const url = linkUrl.value.trim()
  if (!url) return
  showLinkModal.value = false
  editorRef.value?.focus()
  document.execCommand('createLink', false, url)
  linkUrl.value = ''
  onInput()
}

function triggerImageUpload() {
  fileInputRef.value?.click()
}

async function onImageUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  imageUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const url = await request<string>({
      url: '/api/admin/file/upload',
      method: 'POST',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    editorRef.value?.focus()
    document.execCommand('insertImage', false, url)
    onInput()
  } catch (err) {
    console.warn('图片上传失败', err)
  } finally {
    imageUploading.value = false
    input.value = ''
  }
}

function insertHr() {
  editorRef.value?.focus()
  document.execCommand('insertHTML', false, '<hr style="border:none;border-top:1px solid #cbd5e1;margin:0.75rem 0;" />')
  onInput()
}

function decodeHtmlEntities(str: string): string {
  if (!str) return ''
  const txt = document.createElement('textarea')
  txt.innerHTML = str
  return txt.value
}

// Initialize content
watch(() => props.modelValue, (val) => {
  const decoded = decodeHtmlEntities(val || '')
  if (editorRef.value && editorRef.value.innerHTML !== decoded) {
    editorRef.value.innerHTML = decoded
  }
}, { immediate: true })

onMounted(() => {
  nextTick(() => {
    if (editorRef.value && props.modelValue) {
      editorRef.value.innerHTML = decodeHtmlEntities(props.modelValue)
    }
  })
})
</script>

<style scoped>
.rich-editor {
  position: relative;
}
.toolbar-btn {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.375rem;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
  background: transparent;
  border: none;
}
.toolbar-btn:hover {
  background: #fff;
  color: var(--color-primary);
}
.toolbar-btn.active {
  background: color-mix(in srgb, var(--color-primary) 10%, transparent);
  color: var(--color-primary);
}
.editor-content:empty::before {
  content: '请输入商品详情介绍...';
  color: #94a3b8;
}
.editor-content :deep(h3) {
  font-size: 1rem;
  font-weight: 700;
  color: #1e293b;
  margin-top: 0.75rem;
  margin-bottom: 0.5rem;
}
.editor-content :deep(p) {
  font-size: 0.875rem;
  color: #475569;
  margin-bottom: 0.5rem;
}
.editor-content :deep(blockquote) {
  border-left: 3px solid #2E7D32;
  padding: 0.5rem 0.75rem;
  margin: 0.5rem 0;
  background: #f0fdf4;
  font-size: 0.875rem;
  color: #475569;
  border-radius: 0 0.375rem 0.375rem 0;
}
.editor-content :deep(ul) {
  list-style: disc;
  list-style-position: inside;
  font-size: 0.875rem;
  color: #475569;
  margin-bottom: 0.5rem;
  padding-left: 0.5rem;
}
.editor-content :deep(ol) {
  list-style: decimal;
  list-style-position: inside;
  font-size: 0.875rem;
  color: #475569;
  margin-bottom: 0.5rem;
  padding-left: 0.5rem;
}
.editor-content :deep(li) {
  margin-bottom: 0.25rem;
}
.editor-content :deep(b), .editor-content :deep(strong) {
  font-weight: 600;
}
.editor-content :deep(u) {
  text-decoration: underline;
}
.editor-content :deep(strike), .editor-content :deep(s) {
  text-decoration: line-through;
  color: #94a3b8;
}
.editor-content :deep(a) {
  color: #2E7D32;
  text-decoration: underline;
  text-underline-offset: 2px;
}
.editor-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.375rem;
  margin: 0.5rem 0;
}
.editor-content :deep(hr) {
  border: none;
  border-top: 1px solid #cbd5e1;
  margin: 0.75rem 0;
}
</style>
