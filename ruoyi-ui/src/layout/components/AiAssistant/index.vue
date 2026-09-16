<template>
  <div class="ai-assistant-entry" id="header-ai-assistant">
    <button class="ai-entry-btn" title="AI助手（知识库问答）" @click="togglePanel">
      <el-icon :size="17"><MagicStick /></el-icon>
    </button>
    <transition name="el-zoom-in-top">
      <div v-if="open" class="ai-assistant-panel" :style="panelStyle">
        <!-- 头部：渐变底 + 图标 + 标题 + 操作区 -->
        <div class="ai-panel-head">
          <div class="ai-head-left">
            <span class="ai-head-icon"><el-icon :size="15"><MagicStick /></el-icon></span>
            <span class="ai-panel-title">AI 助手</span>
            <span class="ai-panel-badge">知识库问答</span>
          </div>
          <div class="ai-head-right">
            <el-tooltip content="历史记录" placement="bottom">
              <button class="ai-head-btn" @click="openHistory"><el-icon :size="15"><Clock /></el-icon></button>
            </el-tooltip>
            <el-tooltip content="清空当前会话" placement="bottom">
              <button class="ai-head-btn" @click="clearMessages"><el-icon :size="15"><Delete /></el-icon></button>
            </el-tooltip>
            <el-tooltip content="收起" placement="bottom">
              <button class="ai-head-btn" @click="open = false"><el-icon :size="15"><Close /></el-icon></button>
            </el-tooltip>
          </div>
        </div>
        <!-- 模块筛选条 -->
        <div class="ai-panel-filter">
          <span class="ai-filter-label">检索范围</span>
          <el-select v-model="qaModule" size="small" style="flex: 1" title="限定检索范围可提升速度与精准度">
            <el-option label="全平台知识库" value="" />
            <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
          </el-select>
        </div>
        <!-- 消息区 -->
        <div class="ai-panel-body" ref="bodyRef">
          <div v-if="!messages.length" class="ai-empty">
            <div class="ai-empty-icon"><el-icon :size="30"><ChatDotRound /></el-icon></div>
            <div class="ai-empty-title">您好，我是 AI 助手</div>
            <div class="ai-empty-desc">基于企业知识库回答问题，并标注引用来源<br />试试问我：设备操作规范、质量标准、安全生产制度</div>
          </div>
          <div v-for="(m, i) in messages" :key="i" :class="['ai-msg', m.role]">
            <div v-if="m.role === 'assistant'" class="ai-avatar ai-avatar-bot"><el-icon :size="13"><MagicStick /></el-icon></div>
            <div :class="['ai-bubble', m.role]">
              <div v-if="m.role === 'user'" class="ai-bubble-text">{{ m.text }}</div>
              <!-- AI回答：轻量Markdown渲染（加粗/分点/引用块）+ 知识库直接命中原文高亮 -->
              <template v-else>
                <div v-if="isDirectHit(m.text)" class="ai-direct-hit">
                  <div class="ai-hit-label"><el-icon :size="12"><Collection /></el-icon> 知识库原文命中（未调用大模型，毫秒级响应）</div>
                  <div class="ai-bubble-text md" v-html="renderMd(stripHitPrefix(m.text))"></div>
                </div>
                <div v-else class="ai-bubble-text md" v-html="renderMd(m.text)"></div>
              </template>
              <div v-if="m.sources && m.sources.length" class="ai-msg-sources">
                <span class="ai-sources-label"><el-icon :size="11"><Document /></el-icon> 来源：</span>
                <el-tag v-for="(s, j) in uniqueSources(m.sources)" :key="j" size="small" effect="plain" round
                  class="qa-source-tag" @click="openSourceDoc(s)">
                  <el-icon :size="11" style="margin-right:2px"><Document /></el-icon>{{ s.docName }}
                </el-tag>
              </div>
            </div>
            <div v-if="m.role === 'user'" class="ai-avatar ai-avatar-user">{{ nickChar }}</div>
          </div>
          <div v-if="asking" class="ai-msg assistant">
            <div class="ai-avatar ai-avatar-bot"><el-icon :size="13"><MagicStick /></el-icon></div>
            <div class="ai-bubble assistant ai-typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
              <span class="ai-typing-text">正在检索知识库并生成答案…</span>
            </div>
          </div>
        </div>
        <!-- 输入区 -->
        <div class="ai-panel-input">
          <el-input v-model="question" size="default" placeholder="输入问题，回车发送…" :disabled="asking"
            @keyup.enter="handleAsk" clearable />
          <button class="ai-send-btn" :disabled="asking || !(question && question.trim())" @click="handleAsk">
            <el-icon :size="15"><Promotion /></el-icon>
          </button>
        </div>
      </div>
    </transition>

    <!-- 历史记录抽屉 -->
    <el-drawer v-model="historyOpen" title="我的提问历史" size="520px" append-to-body>
      <div class="ai-history-tip">
        <el-icon :size="13"><InfoFilled /></el-icon>
        历史记录仅您本人可见（按用户隔离保存），包含全部提问与回答
      </div>
      <div class="ai-history-list">
        <div v-if="!historyList.length && !historyLoading" class="ai-history-empty">暂无提问历史</div>
        <div v-for="h in historyList" :key="h.id" class="ai-history-item" @click="replayHistory(h)">
          <div class="ai-history-q">
            <span class="ai-history-tag q">问</span>
            <span class="ai-history-qtext">{{ h.question }}</span>
            <span class="ai-history-time">{{ parseTime(h.createTime) }}</span>
          </div>
          <div class="ai-history-a">
            <span class="ai-history-tag a">答</span>
            <span class="ai-history-atext">{{ shortAnswer(h.answer) }}</span>
          </div>
        </div>
        <div v-if="historyLoading" class="ai-history-loading">加载中…</div>
        <div v-if="!historyFinished && historyList.length && !historyLoading" class="ai-history-loading">
          <el-button size="small" text type="primary" @click="loadMoreHistory">加载更多</el-button>
        </div>
        <div v-if="historyFinished && historyList.length" class="ai-history-loading">已全部加载</div>
      </div>
      <template #footer>
        <el-button size="small" @click="loadHistory(true)" :loading="historyLoading">刷新</el-button>
        <el-button size="small" type="danger" plain @click="handleClearHistory">清空我的历史</el-button>
      </template>
    </el-drawer>

    <!-- 来源文档预览弹窗 -->
    <el-dialog v-model="previewOpen" :title="previewDoc.docName || '文档预览'" width="780px" append-to-body
      destroy-on-close @closed="cleanupPreview">
      <div class="doc-meta">
        <div class="doc-meta-item"><span class="doc-meta-label">所属模块</span><span class="badge violet">{{ moduleNames[previewDoc.module] || previewDoc.module || '—' }}</span></div>
        <div class="doc-meta-item"><span class="doc-meta-label">文件类型</span><span class="badge blue">{{ (previewDoc.fileType || '').toUpperCase() || '—' }}</span></div>
        <div class="doc-meta-item"><span class="doc-meta-label">切片数</span>{{ previewDoc.chunkCount ?? '—' }}</div>
        <div class="doc-meta-item"><span class="doc-meta-label">上传人</span>{{ previewDoc.createBy || '—' }}</div>
        <div class="doc-meta-item"><span class="doc-meta-label">上传时间</span>{{ previewDoc.createTime ? parseTime(previewDoc.createTime) : '—' }}</div>
      </div>
      <div class="doc-toolbar">
        <el-radio-group v-model="previewTab" size="small">
          <el-radio-button value="preview">在线预览</el-radio-button>
          <el-radio-button value="chunks">切片列表</el-radio-button>
        </el-radio-group>
        <el-button size="small" type="primary" plain icon="Download" :loading="downloading" @click="handleDownloadDoc">下载原文件</el-button>
      </div>
      <!-- 在线预览 -->
      <div class="doc-preview-area" v-loading="previewLoading">
        <template v-if="previewTab === 'preview'">
          <iframe v-if="previewType === 'pdf' && previewUrl" :src="previewUrl" class="doc-frame"></iframe>
          <pre v-else-if="previewType === 'text'" class="doc-text">{{ previewText }}</pre>
          <div v-else class="doc-preview-empty">
            <el-icon :size="30" color="#c0c4cc"><Document /></el-icon>
            <div>{{ previewDoc.fileType === 'docx' ? 'docx 格式暂不支持在线预览，请下载原文件后查看' : '该格式暂不支持在线预览，请下载原文件后查看' }}</div>
          </div>
        </template>
        <!-- 切片列表 -->
        <template v-else>
          <el-collapse v-loading="chunkLoading">
            <el-collapse-item v-for="c in chunkList" :key="c.chunkId"
              :title="'片段 ' + (c.chunkIndex + 1) + '（' + (c.content ? c.content.length : 0) + ' 字）'">
              <div class="doc-chunk-content">{{ c.content }}</div>
            </el-collapse-item>
          </el-collapse>
          <div v-if="!chunkLoading && !chunkList.length" class="doc-chunks-empty">暂无切片内容</div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="AiAssistant">
/**
 * 全局AI助手入口（P2）：导航栏浮动对话面板
 * 复用 RAG 知识问答接口（/ai/knowledge/qa），问答历史按用户隔离落库，可随时回看
 */
import { ref, computed, nextTick, getCurrentInstance } from 'vue'
import { MagicStick, Clock, Delete, Close, ChatDotRound, Document, Promotion, InfoFilled, Collection, Download } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import { parseTime } from '@/utils/ruoyi'
import { saveAs } from 'file-saver'
import { knowledgeQa, listQaHistory, clearQaHistory, getDocInfo, getDocChunks, downloadDoc, findDocByName } from '@/api/ai/knowledge'

const { proxy } = getCurrentInstance()

const open = ref(false)
const qaModule = ref('')
const moduleNames = {
  platform: '平台公共', dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产'
}
const question = ref('')
const asking = ref(false)
const messages = ref([])
const bodyRef = ref()
const panelStyle = ref({})

const userStore = useUserStore()
const nickChar = computed(() => (userStore.name || '我').charAt(0))

/* ---------- 历史记录 ---------- */
const historyOpen = ref(false)
const historyList = ref([])
const historyPage = ref(1)
const historyFinished = ref(false)
const historyLoading = ref(false)

function openHistory() {
  historyOpen.value = true
  loadHistory(true)
}

function loadHistory(reset) {
  if (historyLoading.value) return
  if (reset) { historyPage.value = 1; historyList.value = []; historyFinished.value = false }
  if (historyFinished.value) return
  historyLoading.value = true
  listQaHistory({ pageNum: historyPage.value, pageSize: 20 }).then(res => {
    const rows = res.data || []
    historyList.value = historyList.value.concat(rows)
    if (rows.length < 20) historyFinished.value = true
    historyPage.value += 1
  }).finally(() => { historyLoading.value = false })
}

function loadMoreHistory() { loadHistory(false) }

function replayHistory(h) {
  messages.value.push({ role: 'user', text: h.question })
  messages.value.push({ role: 'assistant', text: h.answer || '（无回答）', sources: parseSources(h.sourcesJson) })
  historyOpen.value = false
  open.value = true
  scrollToBottom()
}

function parseSources(json) {
  try { const arr = JSON.parse(json || '[]'); return Array.isArray(arr) ? arr : [] } catch (e) { return [] }
}

function shortAnswer(a) {
  if (!a) return '（无回答）'
  return a.length > 80 ? a.slice(0, 80) + '…' : a
}

function handleClearHistory() {
  proxy.$modal.confirm('确认清空您的全部提问历史？此操作不可恢复。').then(() => {
    clearQaHistory().then(() => {
      proxy.$modal.msgSuccess('历史已清空')
      loadHistory(true)
    })
  }).catch(() => {})
}

/* ---------- 面板 ---------- */
function togglePanel() {
  open.value = !open.value
  if (open.value) {
    const btn = document.getElementById('header-ai-assistant')
    if (btn) {
      const r = btn.getBoundingClientRect()
      panelStyle.value = { top: r.bottom + 8 + 'px', left: Math.max(12, r.right - 480) + 'px' }
    }
    scrollToBottom()
  }
}

function clearMessages() {
  messages.value = []
}

async function handleAsk() {
  const q = (question.value || '').trim()
  if (!q || asking.value) return
  messages.value.push({ role: 'user', text: q })
  question.value = ''
  asking.value = true
  scrollToBottom()
  try {
    const res = await knowledgeQa({ question: q, module: qaModule.value || undefined })
    const d = res.data || {}
    messages.value.push({ role: 'assistant', text: d.answer || '未返回答案', sources: d.sources || [] })
  } catch (e) {
    messages.value.push({ role: 'assistant', text: '提问失败，请稍后重试' })
  } finally {
    asking.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

/** 来源按文档名去重（保留docId供点击查看） */
function uniqueSources(sources) {
  const seen = new Set()
  return (sources || []).filter(s => {
    const name = (s && s.docName) || ''
    if (!name || seen.has(name)) return false
    seen.add(name)
    return true
  })
}

/* ---------- 来源文档预览 ---------- */
const previewOpen = ref(false)
const previewDoc = ref({})
const previewTab = ref('preview')
const previewType = ref('none') // pdf / text / none
const previewUrl = ref('')
const previewText = ref('')
const previewLoading = ref(false)
const chunkList = ref([])
const chunkLoading = ref(false)
const downloading = ref(false)

function cleanupPreview() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  previewTab.value = 'preview'
  previewType.value = 'none'
  previewText.value = ''
}

/** 加载在线预览内容：pdf用iframe渲染，txt/md显示全文，docx仅下载 */
function loadPreviewContent(fileType) {
  previewType.value = 'none'
  previewUrl.value = ''
  previewText.value = ''
  if (!previewDoc.value.docId) return
  const ft = (fileType || '').toLowerCase()
  if (ft !== 'pdf' && ft !== 'txt' && ft !== 'md') return
  previewLoading.value = true
  downloadDoc(previewDoc.value.docId).then(data => {
    if (!data || data.type === 'application/json') return
    if (ft === 'pdf') {
      previewUrl.value = URL.createObjectURL(new Blob([data], { type: 'application/pdf' }))
      previewType.value = 'pdf'
    } else {
      data.text().then(t => {
        previewText.value = t
        previewType.value = 'text'
      })
    }
  }).finally(() => { previewLoading.value = false })
}

function openSourceDoc(s) {
  if (!s || !s.docId) {
    // 兜底：历史来源无docId时按文档名查找
    if (s && s.docName) {
      findDocByName(s.docName).then(res => {
        const doc = res.data || {}
        if (doc.docId) {
          s.docId = doc.docId
          openSourceDoc(s)
        } else {
          proxy.$modal.msgWarning('未找到该文档，可能已被删除或重命名')
        }
      }).catch(() => { proxy.$modal.msgWarning('未找到该文档，可能已被删除或重命名') })
      return
    }
    proxy.$modal.msgWarning('该来源缺少文档标识，无法打开')
    return
  }
  previewOpen.value = true
  previewDoc.value = { docId: s.docId, docName: s.docName, module: s.module }
  chunkList.value = []
  cleanupPreview()
  getDocInfo(s.docId).then(res => {
    previewDoc.value = { ...previewDoc.value, ...(res.data || {}) }
    loadPreviewContent(previewDoc.value.fileType)
  }).catch(() => {
    proxy.$modal.msgError('获取文档信息失败')
  })
  chunkLoading.value = true
  getDocChunks(s.docId).then(res => {
    chunkList.value = res.data || []
  }).finally(() => { chunkLoading.value = false })
}

function handleDownloadDoc() {
  const docId = previewDoc.value.docId
  if (!docId) return
  downloading.value = true
  downloadDoc(docId).then(data => {
    if (data && data.type === 'application/json') {
      const reader = new FileReader()
      reader.onload = () => {
        try { const j = JSON.parse(reader.result); proxy.$modal.msgError(j.msg || '下载失败') } catch (e) { proxy.$modal.msgError('下载失败') }
      }
      reader.readAsText(data)
      return
    }
    const name = previewDoc.value.docName + '.' + (previewDoc.value.fileType || 'txt')
    saveAs(new Blob([data]), name)
    proxy.$modal.msgSuccess('已开始下载')
  }).finally(() => { downloading.value = false })
}

/* ---------- 轻量Markdown渲染（AI回答格式优化，零依赖） ---------- */
const HIT_PREFIX = '【知识库直接命中 · 原文返回，未调用大模型】'

function isDirectHit(text) {
  return (text || '').startsWith(HIT_PREFIX)
}

function stripHitPrefix(text) {
  return isDirectHit(text) ? text.slice(HIT_PREFIX.length).replace(/^\s*\n+/, '') : text
}

function esc(s) {
  return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

/** 支持：**加粗**、*斜体*、`code`、无序/有序列表、段落换行；先转义防注入 */
function renderMd(text) {
  if (!text) return ''
  const lines = esc(text).split('\n')
  let html = ''
  let listType = null
  const closeList = () => { if (listType) { html += `</${listType}>`; listType = null } }
  for (let raw of lines) {
    const line = raw.trim()
    if (!line) { closeList(); continue }
    // 列表项：- / * / 数字.
    let m = line.match(/^(?:[-*])\s+(.+)$/)
    if (m) {
      if (listType !== 'ul') { closeList(); html += '<ul>'; listType = 'ul' }
      html += `<li>${inline(m[1])}</li>`
      continue
    }
    m = line.match(/^(\d+)[.、)）]\s*(.+)$/)
    if (m) {
      if (listType !== 'ol') { closeList(); html += '<ol>'; listType = 'ol' }
      html += `<li>${inline(m[1] ? m[2] : '')}</li>`
      continue
    }
    closeList()
    // 标题行（模型偶尔输出 #）降级为加粗段落
    m = line.match(/^#{1,4}\s*(.+)$/)
    if (m) { html += `<p class="md-h">${inline(m[1])}</p>`; continue }
    html += `<p>${inline(line)}</p>`
  }
  closeList()
  return html
}

/** 行内元素：加粗/斜体/代码 */
function inline(s) {
  return s
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}
</script>

<style scoped>
.ai-assistant-entry { display: inline-flex; align-items: center; margin-right: 4px; }
.ai-entry-btn {
  width: 34px; height: 34px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1); color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 3px 10px rgba(99, 102, 241, 0.35); transition: all 0.18s;
}
.ai-entry-btn:hover { transform: translateY(-1px); box-shadow: 0 5px 14px rgba(99, 102, 241, 0.45); }

/* ===== 面板 ===== */
.ai-assistant-panel {
  position: fixed; z-index: 3000; width: 480px; height: 640px;
  background: #fff; border-radius: 14px; border: 1px solid #ececf3;
  box-shadow: 0 12px 40px rgba(31, 35, 41, 0.16), 0 0 0 1px rgba(99, 102, 241, 0.04);
  display: flex; flex-direction: column; overflow: hidden;
}
.ai-panel-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 11px 14px;
  background: linear-gradient(120deg, #ede9fe 0%, #e0e7ff 55%, #eef2ff 100%);
  border-bottom: 1px solid #e4e4f5;
}
.ai-head-left { display: flex; align-items: center; gap: 8px; }
.ai-head-icon {
  width: 26px; height: 26px; border-radius: 8px; color: #fff;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.35);
}
.ai-panel-title { font-weight: 700; font-size: 14px; color: #4338ca; }
.ai-panel-badge {
  font-size: 10px; color: #7c3aed; background: rgba(139, 92, 246, 0.12);
  border: 1px solid rgba(139, 92, 246, 0.25); border-radius: 10px; padding: 1px 8px;
}
.ai-head-right { display: flex; gap: 4px; }
.ai-head-btn {
  width: 26px; height: 26px; border: none; border-radius: 7px; background: transparent;
  color: #6b7280; cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.ai-head-btn:hover { background: rgba(99, 102, 241, 0.1); color: #4f46e5; }

/* 模块筛选条 */
.ai-panel-filter {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 14px; background: #fafaff; border-bottom: 1px solid #f0f0f6;
}
.ai-filter-label { font-size: 12px; color: #6b7280; flex: none; }

/* 消息区 */
.ai-panel-body { flex: 1; overflow-y: auto; padding: 12px 14px; background: #fbfbfe; }
.ai-empty { text-align: center; margin-top: 56px; }
.ai-empty-icon {
  width: 56px; height: 56px; margin: 0 auto 12px; border-radius: 18px;
  background: linear-gradient(135deg, #ede9fe, #e0e7ff); color: #7c3aed;
  display: flex; align-items: center; justify-content: center;
}
.ai-empty-title { font-size: 15px; font-weight: 700; color: #3730a3; margin-bottom: 8px; }
.ai-empty-desc { font-size: 12px; color: #9ca3af; line-height: 1.9; }

.ai-msg { margin-bottom: 12px; display: flex; gap: 8px; align-items: flex-start; }
.ai-msg.user { justify-content: flex-end; }
.ai-avatar {
  flex: none; width: 24px; height: 24px; border-radius: 8px; margin-top: 2px;
  display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; font-weight: 600;
}
.ai-avatar-bot { background: linear-gradient(135deg, #8b5cf6, #6366f1); }
.ai-avatar-user { background: #94a3b8; }
.ai-bubble { max-width: 82%; border-radius: 12px; padding: 8px 12px; font-size: 13px; line-height: 1.75; }
.ai-bubble.user {
  background: linear-gradient(135deg, #6366f1, #818cf8); color: #fff;
  border-bottom-right-radius: 4px; white-space: pre-wrap; word-break: break-word;
}
.ai-bubble.assistant {
  background: #fff; border: 1px solid #e9e9f3; color: #303133;
  border-bottom-left-radius: 4px; word-break: break-word;
  box-shadow: 0 1px 3px rgba(31, 35, 41, 0.04);
}
.ai-msg-sources { margin-top: 6px; font-size: 11px; color: #9ca3af; display: flex; align-items: center; gap: 4px; flex-wrap: wrap; row-gap: 4px; }
.ai-sources-label { flex: none; display: inline-flex; align-items: center; gap: 2px; white-space: nowrap; }
.qa-source-tag { cursor: pointer; transition: all .15s; white-space: nowrap; }
.qa-source-tag :deep(.el-tag__content) { display: inline-flex; align-items: center; white-space: nowrap; }
.qa-source-tag:hover { color: #4f46e5; border-color: #a5b4fc; background: #eef2ff; transform: translateY(-1px); }

/* ===== 来源文档预览弹窗 ===== */
.doc-meta { display:flex; flex-wrap:wrap; gap:10px 24px; background:#f8faff; border:1px solid #e4e8ff; border-radius:10px; padding:12px 16px; margin-bottom:14px; }
.doc-meta-item { display:flex; align-items:center; gap:6px; font-size:13px; color:#374151; }
.doc-meta-label { color:#9ca3af; }
.doc-meta-item .badge { display:inline-flex; align-items:center; padding:2px 8px; border-radius:999px; font-size:12px; font-weight:600; }
.doc-meta-item .badge.violet { background:#f5f3ff; color:#4338ca; }
.doc-meta-item .badge.blue { background:#eff6ff; color:#1d4ed8; }
.doc-toolbar { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.doc-preview-area { min-height: 360px; max-height: 480px; overflow:auto; border:1px solid var(--ink-200, #e2e8f0); border-radius:10px; background:#fff; }
.doc-frame { width:100%; height:480px; border:0; display:block; }
.doc-text { margin:0; padding:16px 18px; white-space:pre-wrap; word-break:break-word; font-size:13.5px; color:#374151; line-height:1.9; font-family:inherit; }
.doc-preview-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; gap:10px; padding:80px 0; color:#9ca3af; font-size:13px; }
.doc-chunks { min-height:200px; }
.doc-chunk-content { white-space:pre-wrap; font-size:13px; color:#4b5563; line-height:1.8; background:#f8fafc; border-radius:8px; padding:10px 12px; }
.doc-chunks-empty { text-align:center; color:#9ca3af; font-size:13px; padding:20px 0; }
.ai-sources-label { display: inline-flex; align-items: center; gap: 2px; }

/* ===== Markdown渲染样式 ===== */
.ai-bubble.assistant .md :deep(p) { margin: 0 0 4px; }
.ai-bubble.assistant .md :deep(p:last-child) { margin-bottom: 0; }
.ai-bubble.assistant .md :deep(strong) { font-weight: 700; color: #1f2937; }
.ai-bubble.assistant .md :deep(em) { font-style: italic; color: #4b5563; }
.ai-bubble.assistant .md :deep(code) {
  background: #f3f4f6; border: 1px solid #e5e7eb; border-radius: 4px;
  padding: 0 4px; font-family: Consolas, Monaco, monospace; font-size: 12px; color: #dc2626;
}
.ai-bubble.assistant .md :deep(ul), .ai-bubble.assistant .md :deep(ol) { margin: 2px 0 6px; padding-left: 20px; }
.ai-bubble.assistant .md :deep(li) { margin: 2px 0; }
.ai-bubble.assistant .md :deep(.md-h) { font-weight: 700; margin: 4px 0; color: #374151; }

/* ===== 知识库直接命中引用块 ===== */
.ai-direct-hit {
  border-left: 3px solid #a5b4fc; background: #f8faff; border-radius: 0 8px 8px 0;
  padding: 6px 10px 6px 12px; margin: 2px 0;
}
.ai-hit-label {
  display: inline-flex; align-items: center; gap: 4px; font-size: 11px; font-weight: 600;
  color: #4f46e5; background: rgba(99, 102, 241, 0.1); border-radius: 10px;
  padding: 2px 8px; margin-bottom: 6px;
}
.ai-direct-hit .ai-bubble-text { padding: 0; }

/* 输入等待动画 */
.ai-typing { display: flex; align-items: center; gap: 3px; }
.ai-typing .dot {
  width: 5px; height: 5px; border-radius: 50%; background: #a5b4fc; display: inline-block;
  animation: aiBlink 1.2s infinite both;
}
.ai-typing .dot:nth-child(2) { animation-delay: 0.2s; }
.ai-typing .dot:nth-child(3) { animation-delay: 0.4s; }
.ai-typing-text { font-size: 12px; color: #9ca3af; margin-left: 5px; }
@keyframes aiBlink { 0%, 80%, 100% { opacity: 0.25; } 40% { opacity: 1; } }

/* 输入区 */
.ai-panel-input { display: flex; gap: 8px; padding: 10px 12px; border-top: 1px solid #f0f0f6; background: #fff; }
.ai-send-btn {
  flex: none; width: 34px; height: 32px; border: none; border-radius: 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6); color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.15s;
}
.ai-send-btn:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 3px 10px rgba(99, 102, 241, 0.4); }
.ai-send-btn:disabled { opacity: 0.45; cursor: not-allowed; }

/* ===== 历史抽屉 ===== */
.ai-history-tip {
  display: flex; align-items: center; gap: 6px; font-size: 12px; color: #6b7280;
  background: #f5f7ff; border: 1px solid #e4e8ff; border-radius: 8px; padding: 8px 12px; margin-bottom: 12px;
}
.ai-history-list { height: calc(100vh - 180px); overflow-y: auto; }
.ai-history-empty { text-align: center; color: #9ca3af; font-size: 13px; margin-top: 40px; }
.ai-history-item {
  border: 1px solid #eef0f5; border-radius: 10px; padding: 10px 12px; margin-bottom: 10px;
  cursor: pointer; transition: all 0.15s; background: #fff;
}
.ai-history-item:hover { border-color: #c7d2fe; box-shadow: 0 2px 8px rgba(99, 102, 241, 0.08); }
.ai-history-q { display: flex; align-items: flex-start; gap: 6px; margin-bottom: 6px; }
.ai-history-tag {
  flex: none; font-size: 10px; border-radius: 4px; padding: 1px 5px; color: #fff; margin-top: 2px;
}
.ai-history-tag.q { background: #6366f1; }
.ai-history-tag.a { background: #059669; }
.ai-history-qtext { font-size: 13px; font-weight: 600; color: #303133; flex: 1; }
.ai-history-time { flex: none; font-size: 11px; color: #c0c4cc; margin-top: 3px; }
.ai-history-a { display: flex; align-items: flex-start; gap: 6px; }
.ai-history-atext { font-size: 12px; color: #6b7280; line-height: 1.6; }
.ai-history-loading { text-align: center; font-size: 12px; color: #c0c4cc; padding: 8px 0; }
</style>
