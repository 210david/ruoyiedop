<template>
  <div class="app-container ai-qa-page">
    <div class="surface qa-shell">
      <!-- 头部 -->
      <div class="qa-head">
        <div class="qa-head-left">
          <span class="qa-head-icon"><el-icon :size="15"><ChatDotRound /></el-icon></span>
          <span class="qa-title">知识问答</span>
          <span class="qa-badge">基于企业知识库 · 标注引用来源</span>
        </div>
        <div class="qa-head-right">
          <el-select v-model="qaModule" size="default" style="width: 160px" title="限定检索范围可提升速度与精准度">
            <el-option label="全平台知识库" value="" />
            <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
          </el-select>
          <el-tooltip content="历史记录" placement="bottom">
            <button class="qa-head-btn" @click="openHistory"><el-icon :size="15"><Clock /></el-icon></button>
          </el-tooltip>
          <el-tooltip content="清空当前会话" placement="bottom">
            <button class="qa-head-btn" @click="qaHistory = []"><el-icon :size="15"><Delete /></el-icon></button>
          </el-tooltip>
        </div>
      </div>

      <!-- 消息区 -->
      <div class="qa-body" ref="bodyRef">
        <div v-if="!qaHistory.length" class="qa-empty">
          <div class="qa-empty-icon"><el-icon :size="32"><ChatDotRound /></el-icon></div>
          <div class="qa-empty-title">您好，请基于知识库向我提问</div>
          <div class="qa-empty-desc">上传文档后即可提问，AI 将基于知识库内容回答并标注引用来源<br />试试问我：设备操作规范、质量标准、安全生产制度</div>
        </div>
        <div v-for="(item, i) in qaHistory" :key="i" class="qa-msg">
          <div class="qa-row user">
            <div class="qa-avatar u">{{ nickChar }}</div>
            <div class="qa-bubble user">{{ item.question }}</div>
          </div>
          <div class="qa-row assistant">
            <div class="qa-avatar a"><el-icon :size="13"><ChatDotRound /></el-icon></div>
            <div class="qa-bubble assistant">
              <template v-if="isDirectHit(item.answer)">
                <div class="ai-direct-hit">
                  <div class="ai-hit-label"><el-icon :size="12"><Collection /></el-icon> 知识库原文命中（未调用大模型，毫秒级响应）</div>
                  <div class="md" v-html="renderMd(stripHitPrefix(item.answer))"></div>
                </div>
              </template>
              <template v-else>
                <div class="md" v-html="renderMd(item.answer)"></div>
              </template>
              <div class="qa-sources" v-if="item.sources && item.sources.length">
                <span class="qa-sources-label"><el-icon :size="11"><Document /></el-icon> 来源：</span>
                <el-tag v-for="(s, j) in uniqueSources(item.sources)" :key="j" size="small" effect="plain" round
                  class="qa-source-tag" @click="openSourceDoc(s)">
                  <el-icon :size="11" style="margin-right:2px"><Document /></el-icon>{{ s.docName }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
        <div v-if="asking" class="qa-row assistant">
          <div class="qa-avatar a"><el-icon :size="13"><ChatDotRound /></el-icon></div>
          <div class="qa-bubble assistant ai-typing">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            <span class="ai-typing-text">正在检索知识库并生成答案…</span>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="qa-input">
        <el-input v-model="question" size="large" placeholder="输入问题，回车提问" @keyup.enter="handleAsk" :disabled="asking" clearable />
        <button class="qa-send-btn" :disabled="asking || !(question && question.trim())" @click="handleAsk">
          <el-icon :size="15"><Promotion /></el-icon>
        </button>
      </div>
    </div>

    <!-- 历史记录抽屉 -->
    <el-drawer v-model="historyOpen" title="我的提问历史" size="520px" append-to-body>
      <div class="ai-history-tip">
        <el-icon :size="13"><InfoFilled /></el-icon>
        历史记录仅您本人可见（按用户隔离保存），点击任意记录可回放问答
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

<script setup name="AiKnowledgeQa">
import { knowledgeQa, listQaHistory, clearQaHistory, getDocInfo, getDocChunks, downloadDoc, findDocByName } from '@/api/ai/knowledge'
import { parseTime } from '@/utils/ruoyi'
import { saveAs } from 'file-saver'
import { ChatDotRound, Clock, Delete, Document, Promotion, InfoFilled, Collection, Download } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const nickChar = computed(() => (userStore.name || '我').charAt(0))

const moduleNames = {
  platform: '平台公共', dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产'
}
const qaModule = ref('')
const question = ref('')
const asking = ref(false)
const qaHistory = ref([])
const bodyRef = ref()

/* 历史记录 */
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
  qaHistory.value.push({ question: h.question, answer: h.answer || '（无回答）', sources: parseSources(h.sourcesJson) })
  historyOpen.value = false
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

/* 问答 */
function handleAsk() {
  const q = (question.value || '').trim()
  if (!q || asking.value) return
  asking.value = true
  qaHistory.value.push({ question: q, answer: '', sources: [] })
  question.value = ''
  const item = qaHistory.value[qaHistory.value.length - 1]
  scrollToBottom()
  knowledgeQa({ question: q, module: qaModule.value }).then(res => {
    asking.value = false
    const d = res.data || {}
    item.answer = d.answer || '未返回答案'
    item.sources = d.sources || []
    scrollToBottom()
  }).catch(() => {
    asking.value = false
    item.answer = '提问失败，请稍后重试'
    scrollToBottom()
  })
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

/* ---------- 轻量Markdown渲染（零依赖） ---------- */
const HIT_PREFIX = '【知识库直接命中 · 原文返回，未调用大模型】'
function isDirectHit(text) { return (text || '').startsWith(HIT_PREFIX) }
function stripHitPrefix(text) { return isDirectHit(text) ? text.slice(HIT_PREFIX.length).replace(/^\s*\n+/, '') : text }
function esc(s) {
  return (s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}
function renderMd(text) {
  if (!text) return ''
  const lines = esc(text).split('\n')
  let html = ''
  let listType = null
  const closeList = () => { if (listType) { html += `</${listType}>`; listType = null } }
  for (let raw of lines) {
    const line = raw.trim()
    if (!line) { closeList(); continue }
    let m = line.match(/^(?:[-*])\s+(.+)$/)
    if (m) {
      if (listType !== 'ul') { closeList(); html += '<ul>'; listType = 'ul' }
      html += `<li>${inline(m[1])}</li>`
      continue
    }
    m = line.match(/^(\d+)[.、)）]\s*(.+)$/)
    if (m) {
      if (listType !== 'ol') { closeList(); html += '<ol>'; listType = 'ol' }
      html += `<li>${inline(m[2])}</li>`
      continue
    }
    closeList()
    m = line.match(/^#{1,4}\s*(.+)$/)
    if (m) { html += `<p class="md-h">${inline(m[1])}</p>`; continue }
    html += `<p>${inline(line)}</p>`
  }
  closeList()
  return html
}
function inline(s) {
  return s
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
}
</script>

<style scoped>
/* ===== Design Tokens ===== */
.ai-qa-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  color: var(--ink-900);
}
.ai-qa-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; }

/* ===== Shell：占满视口高度 ===== */
.qa-shell { display:flex; flex-direction:column; height: calc(100vh - 110px); min-height: 480px; }

/* ===== 头部 ===== */
.qa-head {
  display:flex; align-items:center; justify-content:space-between; padding: 12px 20px;
  background: linear-gradient(120deg, #ede9fe 0%, #e0e7ff 55%, #eef2ff 100%);
  border-bottom: 1px solid #e4e4f5;
}
.qa-head-left { display:flex; align-items:center; gap:10px; }
.qa-head-icon {
  width:30px; height:30px; border-radius:9px; color:#fff;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  display:flex; align-items:center; justify-content:center;
  box-shadow: 0 2px 6px rgba(99,102,241,.35);
}
.qa-title { font-size:15px; font-weight:700; color:#4338ca; }
.qa-badge { font-size:11px; color:#7c3aed; background:rgba(139,92,246,.12); border:1px solid rgba(139,92,246,.25); border-radius:10px; padding:2px 9px; }
.qa-head-right { display:flex; align-items:center; gap:6px; }
.qa-head-btn {
  width:30px; height:30px; border:none; border-radius:8px; background:transparent;
  color:#6b7280; cursor:pointer; display:flex; align-items:center; justify-content:center; transition:all .15s;
}
.qa-head-btn:hover { background:rgba(99,102,241,.12); color:#4f46e5; }

/* ===== 消息区 ===== */
.qa-body { flex:1; overflow-y:auto; padding: 18px 22px; background:#fbfbfe; }
.qa-empty { text-align:center; margin-top: 12vh; }
.qa-empty-icon {
  width:60px; height:60px; margin:0 auto 14px; border-radius:20px;
  background:linear-gradient(135deg,#ede9fe,#e0e7ff); color:#7c3aed;
  display:flex; align-items:center; justify-content:center;
}
.qa-empty-title { font-size:16px; font-weight:700; color:#3730a3; margin-bottom:10px; }
.qa-empty-desc { font-size:13px; color:#9ca3af; line-height:2; }

.qa-row { display:flex; gap:10px; margin-bottom:16px; }
.qa-row.user { justify-content:flex-end; }
.qa-avatar {
  flex:none; width:28px; height:28px; border-radius:9px; margin-top:2px;
  display:flex; align-items:center; justify-content:center; color:#fff; font-size:12px; font-weight:600;
}
.qa-avatar.a { background:linear-gradient(135deg,#8b5cf6,#6366f1); }
.qa-avatar.u { background:#94a3b8; }
.qa-bubble { max-width:76%; border-radius:14px; padding:10px 14px; font-size:14px; line-height:1.8; }
.qa-bubble.user {
  background:linear-gradient(135deg,#6366f1,#818cf8); color:#fff; font-weight:500;
  border-bottom-right-radius:4px; word-break:break-word;
}
.qa-bubble.assistant {
  background:#fff; border:1px solid #e9e9f3; color:#303133;
  border-bottom-left-radius:4px; word-break:break-word;
  box-shadow:0 1px 3px rgba(31,35,41,.04);
}

/* Markdown */
.qa-bubble.assistant .md :deep(p) { margin:0 0 5px; }
.qa-bubble.assistant .md :deep(p:last-child) { margin-bottom:0; }
.qa-bubble.assistant .md :deep(strong) { font-weight:700; color:#1f2937; }
.qa-bubble.assistant .md :deep(code) { background:#f3f4f6; border:1px solid #e5e7eb; border-radius:4px; padding:0 4px; font-family:Consolas,Monaco,monospace; font-size:13px; color:#dc2626; }
.qa-bubble.assistant .md :deep(ul), .qa-bubble.assistant .md :deep(ol) { margin:3px 0 8px; padding-left:22px; }
.qa-bubble.assistant .md :deep(li) { margin:3px 0; }
.qa-bubble.assistant .md :deep(.md-h) { font-weight:700; margin:5px 0; color:#374151; }

/* 直接命中引用块 */
.ai-direct-hit { border-left:3px solid #a5b4fc; background:#f8faff; border-radius:0 8px 8px 0; padding:6px 10px 6px 12px; }
.ai-hit-label {
  display:inline-flex; align-items:center; gap:4px; font-size:12px; font-weight:600;
  color:#4f46e5; background:rgba(99,102,241,.1); border-radius:10px; padding:2px 9px; margin-bottom:8px;
}

/* 来源 */
.qa-sources { margin-top:8px; font-size:12px; color:#9ca3af; display:flex; align-items:center; gap:4px; flex-wrap:wrap; row-gap:4px; }
.qa-sources-label { flex:none; display:inline-flex; align-items:center; gap:2px; white-space:nowrap; }
.qa-source-tag { cursor:pointer; transition:all .15s; white-space:nowrap; }
.qa-source-tag :deep(.el-tag__content) { display:inline-flex; align-items:center; white-space:nowrap; }
.qa-source-tag:hover { color:var(--brand-600, #4f46e5); border-color:#a5b4fc; background:#eef2ff; transform:translateY(-1px); }

/* ===== 来源文档预览弹窗 ===== */
.doc-meta { display:flex; flex-wrap:wrap; gap:10px 24px; background:#f8faff; border:1px solid #e4e8ff; border-radius:10px; padding:12px 16px; margin-bottom:14px; }
.doc-meta-item { display:flex; align-items:center; gap:6px; font-size:13px; color:#374151; }
.doc-meta-label { color:#9ca3af; }
.doc-meta-item .badge { display:inline-flex; align-items:center; padding:2px 8px; border-radius:999px; font-size:12px; font-weight:600; }
.doc-meta-item .badge.violet { background:#f5f3ff; color:#4338ca; }
.doc-meta-item .badge.blue { background:#eff6ff; color:#1d4ed8; }
.doc-toolbar { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.doc-preview-area { min-height:360px; max-height:480px; overflow:auto; border:1px solid #e2e8f0; border-radius:10px; background:#fff; }
.doc-frame { width:100%; height:480px; border:0; display:block; }
.doc-text { margin:0; padding:16px 18px; white-space:pre-wrap; word-break:break-word; font-size:13.5px; color:#374151; line-height:1.9; font-family:inherit; }
.doc-preview-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; gap:10px; padding:80px 0; color:#9ca3af; font-size:13px; }
.doc-chunks { min-height:200px; }
.doc-chunk-content { white-space:pre-wrap; font-size:13px; color:#4b5563; line-height:1.8; background:#f8fafc; border-radius:8px; padding:10px 12px; }
.doc-chunks-empty { text-align:center; color:#9ca3af; font-size:13px; padding:20px 0; }

/* 等待动画 */
.ai-typing { display:flex; align-items:center; gap:3px; }
.ai-typing .dot { width:5px; height:5px; border-radius:50%; background:#a5b4fc; display:inline-block; animation:qaBlink 1.2s infinite both; }
.ai-typing .dot:nth-child(2) { animation-delay:.2s; }
.ai-typing .dot:nth-child(3) { animation-delay:.4s; }
.ai-typing-text { font-size:13px; color:#9ca3af; margin-left:5px; }
@keyframes qaBlink { 0%,80%,100% { opacity:.25; } 40% { opacity:1; } }

/* ===== 输入区 ===== */
.qa-input { display:flex; gap:10px; padding:12px 20px; border-top:1px solid var(--ink-200); background:#fff; }
.qa-send-btn {
  flex:none; width:44px; height:40px; border:none; border-radius:10px;
  background:linear-gradient(135deg,#6366f1,#8b5cf6); color:#fff; cursor:pointer;
  display:flex; align-items:center; justify-content:center; transition:all .15s;
}
.qa-send-btn:hover:not(:disabled) { transform:translateY(-1px); box-shadow:0 3px 10px rgba(99,102,241,.4); }
.qa-send-btn:disabled { opacity:.45; cursor:not-allowed; }

/* ===== 历史抽屉 ===== */
.ai-history-tip { display:flex; align-items:center; gap:6px; font-size:12px; color:#6b7280; background:#f5f7ff; border:1px solid #e4e8ff; border-radius:8px; padding:8px 12px; margin-bottom:12px; }
.ai-history-list { height: calc(100vh - 180px); overflow-y:auto; }
.ai-history-empty { text-align:center; color:#9ca3af; font-size:13px; margin-top:40px; }
.ai-history-item { border:1px solid #eef0f5; border-radius:10px; padding:10px 12px; margin-bottom:10px; cursor:pointer; transition:all .15s; background:#fff; }
.ai-history-item:hover { border-color:#c7d2fe; box-shadow:0 2px 8px rgba(99,102,241,.08); }
.ai-history-q { display:flex; align-items:flex-start; gap:6px; margin-bottom:6px; }
.ai-history-tag { flex:none; font-size:10px; border-radius:4px; padding:1px 5px; color:#fff; margin-top:2px; }
.ai-history-tag.q { background:#6366f1; }
.ai-history-tag.a { background:#059669; }
.ai-history-qtext { font-size:13px; font-weight:600; color:#303133; flex:1; }
.ai-history-time { flex:none; font-size:11px; color:#c0c4cc; margin-top:3px; }
.ai-history-a { display:flex; align-items:flex-start; gap:6px; }
.ai-history-atext { font-size:12px; color:#6b7280; line-height:1.6; }
.ai-history-loading { text-align:center; font-size:12px; color:#c0c4cc; padding:8px 0; }
</style>
