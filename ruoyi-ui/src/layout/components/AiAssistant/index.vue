<template>
  <div class="ai-assistant-entry" id="header-ai-assistant">
    <!-- 入口：宽屏显示"输入框+发送按钮"形态（所见即所得，用户一看就懂），窄屏（<1200px）降级为图标按钮 -->
    <div v-if="pillMode" class="ai-search-box" :class="{ 'is-active': open }" role="button" tabindex="0" title="AI 智能问答" @click="togglePanel" @keydown.enter.prevent="togglePanel">
      <svg class="ai-box-search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <span class="ai-box-placeholder">请输入问题，AI 为您解答…</span>
      <span class="ai-box-send" title="提问">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2 11 13"/><path d="M22 2 15 22 11 13 2 9 22 2Z"/></svg>
      </span>
    </div>
    <button v-else class="ai-entry-btn" title="AI助手（知识库问答）" @click="togglePanel">
      <AiKnowledgeIcon :size="19" />
    </button>
    <transition name="el-zoom-in-top">
      <div v-if="open" class="ai-assistant-panel" :style="panelStyle">
        <!-- 头部：渐变底 + 图标 + 标题 + 操作区 -->
        <div class="ai-panel-head">
          <div class="ai-head-left">
            <span class="ai-head-icon"><AiKnowledgeIcon :size="15" /></span>
            <span class="ai-panel-title">AI 助手</span>
          </div>
          <div class="ai-head-right">
            <el-tooltip :content="mode === 'knowledge' ? '历史记录（知识问答）' : '历史记录（数据查询）'" placement="bottom">
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
        <!-- 双模式切换：知识问答 / 数据查询（选择持久化 localStorage） -->
        <div class="ai-mode-switch">
          <button v-for="m in modes" :key="m.key" class="ai-mode-btn" :class="{ 'is-on': mode === m.key }"
            type="button" @click="switchMode(m.key)">
            <span class="ai-mode-ico">{{ m.icon }}</span>{{ m.label }}
          </button>
          <span class="ai-mode-tip" :title="currentMode.tip">{{ currentMode.tip }}</span>
        </div>
        <!-- 模块筛选条：顶部轻量面板不提供（用户反馈无实际价值），完整筛选请到「AI知识库→知识问答」页 -->
        <!-- 消息区 -->
        <div class="ai-panel-body" ref="bodyRef">
          <div v-if="!visibleMessages.length" class="ai-empty">
            <div class="ai-empty-icon"><el-icon :size="30"><ChatDotRound /></el-icon></div>
            <div class="ai-empty-title">{{ currentMode.emptyTitle }}</div>
            <div class="ai-empty-desc">{{ currentMode.emptyDesc }}</div>
            <div class="ai-empty-chips">
              <button v-for="c in currentMode.chips" :key="c" class="ai-chip" type="button"
                :disabled="asking" @click="askDirect(c)">{{ c }}</button>
            </div>
          </div>
          <div v-for="(m, i) in visibleMessages" :key="i" :class="['ai-msg', m.role]">
            <div v-if="m.role === 'assistant'" class="ai-avatar ai-avatar-bot"><AiKnowledgeIcon :size="14" /></div>
            <div :class="['ai-bubble', m.role]">
              <div v-if="m.role === 'user'" class="ai-bubble-text">{{ m.text }}</div>
              <!-- AI回答：轻量Markdown渲染（加粗/分点/引用块）+ 知识库直接命中原文高亮 -->
              <template v-else>
                <div v-if="m.kind === 'error'" class="ai-bubble-text">{{ m.text }}</div>
                <!-- 数据查询结果：结论 + 结果表格 + 行数 + 查看SQL -->
                <div v-else-if="m.kind === 'data'" class="ai-data-result">
                  <div v-if="m.summary" class="ai-data-summary">
                    <el-icon :size="13"><DataAnalysis /></el-icon>{{ m.summary }}
                  </div>
                  <div v-if="m.error" class="ai-data-error">{{ m.error }}</div>
                  <el-table v-if="m.rows && m.rows.length" :data="m.rows" border size="small" max-height="220"
                    class="ai-data-table" :header-cell-style="{ background: '#F8FAFC', color: '#111827', fontWeight: 600 }">
                    <el-table-column type="index" label="#" width="44" align="center" />
                    <el-table-column v-for="col in m.columns" :key="col" :label="col" :prop="col" min-width="110" show-overflow-tooltip>
                      <template #default="{ row }">{{ formatCell(row[col]) }}</template>
                    </el-table-column>
                  </el-table>
                  <div v-else class="ai-data-norows">{{ m.emptyReason || '查询执行成功，但没有查到匹配数据——可能尚无关联业务记录，可到对应业务模块确认数据是否已录入。' }}</div>
                  <div v-if="m.rows && m.rows.length" class="ai-data-meta">共 {{ m.rows.length }} 行
                    <button class="ai-sql-toggle" type="button" @click="m.sqlOpen = !m.sqlOpen">{{ m.sqlOpen ? '隐藏 SQL' : '查看 SQL' }}</button>
                  </div>
                  <pre v-if="m.sqlOpen && m.sql" class="ai-data-sql">{{ m.sql }}</pre>
                </div>
                <template v-else>
                  <div v-if="isDirectHit(m.text)" class="ai-direct-hit">
                    <div class="ai-hit-label"><el-icon :size="12"><Collection /></el-icon> 知识库原文命中（未调用大模型，毫秒级响应）</div>
                    <div class="ai-bubble-text md" v-html="renderMd(stripHitPrefix(m.text))"></div>
                  </div>
                  <div v-else class="ai-bubble-text md" v-html="renderMd(m.text)"></div>
                </template>
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
            <div class="ai-avatar ai-avatar-bot"><AiKnowledgeIcon :size="14" /></div>
            <div class="ai-bubble assistant ai-typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
              <span class="ai-typing-text">{{ mode === 'data' ? '正在理解问题并生成查询…' : '正在检索知识库并生成答案…' }}</span>
            </div>
          </div>
        </div>
        <!-- 输入区 -->
        <div class="ai-panel-input">
          <el-input v-model="question" size="default" :placeholder="currentMode.placeholder" :disabled="asking"
            @keyup.enter="handleAsk" clearable />
          <button class="ai-send-btn" :disabled="asking || !(question && question.trim())" @click="handleAsk">
            <el-icon :size="15"><Promotion /></el-icon>
          </button>
        </div>
      </div>
    </transition>

    <!-- 历史记录抽屉 -->
    <!-- 历史记录抽屉：按模式加载对应历史（知识问答 / 数据查询） -->
    <el-drawer v-model="historyOpen" :title="mode === 'knowledge' ? '知识问答历史' : '数据查询历史'" size="520px" append-to-body>
      <div class="ai-history-tip">
        <el-icon :size="13"><InfoFilled /></el-icon>
        {{ mode === 'knowledge'
          ? '知识问答的提问与回答历史，仅您本人可见（按用户隔离保存）'
          : '数据查询的提问与结果历史，仅您本人可见（按用户隔离保存）' }}
      </div>
      <div class="ai-history-list">
        <div v-if="!historyList.length && !historyLoading" class="ai-history-empty">暂无提问历史</div>
        <div v-for="h in historyList" :key="h.id" class="ai-history-item" @click="replayHistory(h)">
          <div class="ai-history-q">
            <span class="ai-history-tag q">问</span>
            <span class="ai-history-qtext">{{ h.question }}</span>
            <span class="ai-history-time">{{ parseTime(h.createTime) }}</span>
          </div>
          <div v-if="mode === 'knowledge'" class="ai-history-a">
            <span class="ai-history-tag a">答</span>
            <span class="ai-history-atext">{{ shortAnswer(h.answer) }}</span>
          </div>
          <div v-else class="ai-history-a">
            <span class="ai-history-tag a">答</span>
            <span class="ai-history-atext">{{ h.answer ? shortAnswer(h.answer) : (h.status === '1' ? '（查询失败）' : '（已返回结果表格，点击查看）') }}</span>
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

    <!-- 来源文档预览弹窗（支持最大化/还原） -->
    <el-dialog v-model="previewOpen" :fullscreen="previewMaximized" width="780px" append-to-body
      class="doc-preview-dialog" :class="{ 'is-max': previewMaximized }"
      destroy-on-close @closed="cleanupPreview">
      <template #header>
        <div class="doc-dialog-header">
          <span class="doc-dialog-title" :title="previewDoc.docName || '文档预览'">{{ previewDoc.docName || '文档预览' }}</span>
          <el-button class="doc-collapse-btn" link type="primary" @click="metaCollapsed = !metaCollapsed">
            <el-icon :class="{ 'is-collapsed': metaCollapsed }"><ArrowUp /></el-icon>
            <span class="doc-collapse-text">{{ metaCollapsed ? '展开信息' : '收起信息' }}</span>
          </el-button>
          <el-tooltip :content="previewMaximized ? '还原' : '最大化'" placement="bottom">
            <el-button class="doc-max-btn" link @click="previewMaximized = !previewMaximized">
              <el-icon v-if="!previewMaximized"><FullScreen /></el-icon>
              <el-icon v-else><ScaleToOriginal /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </template>
      <div v-show="!metaCollapsed" class="doc-top-block">
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
      </div>
      <!-- 在线预览 -->
      <div class="doc-preview-area" :style="previewAreaStyle" v-loading="previewLoading">
        <template v-if="previewTab === 'preview'">
          <iframe v-if="previewType === 'pdf' && previewUrl" :src="previewUrl" class="doc-frame"></iframe>
          <pre v-else-if="previewType === 'text'" class="doc-text">{{ previewText }}</pre>
          <div v-else-if="previewType === 'docx'" ref="docxContainer" class="docx-preview-wrap"></div>
          <div v-else class="doc-preview-empty">
            <el-icon :size="30" color="#c0c4cc"><Document /></el-icon>
            <div>该格式暂不支持在线预览，请下载原文件后查看</div>
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
import { ref, computed, nextTick, getCurrentInstance, onMounted, onBeforeUnmount } from 'vue'
// 注：onMounted/onBeforeUnmount 仅用于胶囊/图标的响应式降级监听
import { renderAsync } from 'docx-preview'
import { Clock, Delete, Close, ChatDotRound, Document, Promotion, InfoFilled, Collection, Download, DataAnalysis, FullScreen, ScaleToOriginal, ArrowUp } from '@element-plus/icons-vue'
import AiKnowledgeIcon from '@/components/AiKnowledgeIcon/index.vue'
import useUserStore from '@/store/modules/user'
import { parseTime } from '@/utils/ruoyi'
import { saveAs } from 'file-saver'
import { knowledgeQa, listQaHistory, clearQaHistory, getDocInfo, getDocChunks, downloadDoc, findDocByName, getDocPreviewUrl } from '@/api/ai/knowledge'
import { nlQuery, listNlqHistory, clearNlqHistory } from '@/api/ai/manage'

const { proxy } = getCurrentInstance()

const open = ref(false)

/* ===== 双模式：knowledge=知识问答（默认） / data=数据查询（NL2SQL） ===== */
const MODE_STORAGE_KEY = 'ai_assistant_mode'
const modes = [
  {
    key: 'knowledge', label: '知识问答', icon: '📖',
    placeholder: '输入问题，基于知识库回答…',
    tip: '基于企业知识库文档回答，标注引用来源',
    emptyTitle: '您好，我是 AI 助手',
    emptyDesc: '基于企业知识库回答问题，并标注引用来源',
    chips: ['设备操作规范', '质量标准', '安全生产制度']
  },
  {
    key: 'data', label: '数据查询', icon: '📊',
    placeholder: '用自然语言查数据，如：上月故障最多的三台设备',
    tip: '自然语言生成 SQL 查询业务数据，只读安全',
    emptyTitle: '问我数据，我帮你查',
    emptyDesc: '用日常语言提问，AI 自动生成查询并返回结果',
    chips: ['本月工单状态分布', '故障最多的前5台设备', '各供应商供货金额排名']
  }
]
const mode = ref(localStorage.getItem(MODE_STORAGE_KEY) === 'data' ? 'data' : 'knowledge')
const currentMode = computed(() => modes.find(m => m.key === mode.value))

function switchMode(m) {
  if (mode.value === m || asking.value) return
  mode.value = m
  localStorage.setItem(MODE_STORAGE_KEY, m)
  scrollToBottom()
}

/** 当前模式可见的消息（两种模式对话历史独立，避免知识回答与数据表格混排） */
const visibleMessages = computed(() => messages.value.filter(x => (x.mode || 'knowledge') === mode.value))

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
  const params = { pageNum: historyPage.value, pageSize: 20 }
  const req = mode.value === 'data' ? listNlqHistory(params) : listQaHistory(params)
  req.then(res => {
    const rows = res.data || []
    historyList.value = historyList.value.concat(rows)
    if (rows.length < 20) historyFinished.value = true
    historyPage.value += 1
  }).finally(() => { historyLoading.value = false })
}

function loadMoreHistory() { loadHistory(false) }

function replayHistory(h) {
  if (mode.value === 'data') {
    // 数据查询历史回放：从 resultJson 还原结果表格
    let snap = {}
    try { snap = JSON.parse(h.resultJson || '{}') } catch (e) { snap = {} }
    messages.value.push({ role: 'user', text: h.question, mode: 'data' })
    messages.value.push({
      role: 'assistant', mode: 'data', kind: 'data',
      summary: h.answer || '', error: h.status === '1' ? '（当时查询失败）' : '',
      columns: snap.columns || [], rows: snap.rows || [],
      sql: snap.sql || '', emptyReason: snap.emptyReason || '', sqlOpen: false
    })
    historyOpen.value = false
    open.value = true
    scrollToBottom()
    return
  }
  messages.value.push({ role: 'user', text: h.question, mode: 'knowledge' })
  messages.value.push({ role: 'assistant', text: h.answer || '（无回答）', sources: parseSources(h.sourcesJson), mode: 'knowledge' })
  historyOpen.value = false
  open.value = true
  if (mode.value !== 'knowledge') switchMode('knowledge')
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
    const req = mode.value === 'data' ? clearNlqHistory() : clearQaHistory()
    req.then(() => {
      proxy.$modal.msgSuccess('历史已清空')
      loadHistory(true)
    })
  }).catch(() => {})
}

/* ---------- 面板 ---------- */
/* 入口形态：宽屏显示醒目胶囊，窄屏降级为图标按钮 */
const PILL_BREAKPOINT = 1200
const pillMode = ref(true)

function syncPillMode() {
  pillMode.value = window.innerWidth >= PILL_BREAKPOINT
}

function togglePanel() {
  open.value = !open.value
  if (open.value) {
    const btn = document.getElementById('header-ai-assistant')
    if (btn) {
      const r = btn.getBoundingClientRect()
      panelStyle.value = { top: r.bottom + 8 + 'px', left: Math.max(12, r.right - 560) + 'px' }
    }
    scrollToBottom()
  }
}

onMounted(() => {
  syncPillMode()
  window.addEventListener('resize', syncPillMode)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncPillMode)
})

function clearMessages() {
  messages.value = messages.value.filter(x => (x.mode || 'knowledge') !== mode.value)
}

/** 点击空状态示例 chip 直接提问 */
function askDirect(q) {
  question.value = q
  handleAsk()
}

function formatCell(v) {
  if (v === null || v === undefined || v === '') return '—'
  return v
}

async function handleAsk() {
  const q = (question.value || '').trim()
  if (!q || asking.value) return
  question.value = ''
  asking.value = true
  if (mode.value === 'data') {
    // 数据查询模式：NL2SQL，结果以 kind='data' 消息渲染（不进入知识模式可见历史）
    messages.value.push({ role: 'user', text: q, mode: 'data' })
    scrollToBottom()
    try {
      const res = await nlQuery(q)
      const d = res.data || {}
      messages.value.push({
        role: 'assistant', mode: 'data', kind: 'data',
        summary: d.summary || '', error: d.error || '',
        columns: d.columns || [], rows: d.rows || [],
        sql: d.sql || '', emptyReason: d.emptyReason || '',
        sqlOpen: false
      })
    } catch (e) {
      messages.value.push({ role: 'assistant', mode: 'data', kind: 'error', text: '查询失败，请稍后重试或调整问题表述' })
    } finally {
      asking.value = false
      scrollToBottom()
    }
    return
  }
  // 知识问答模式
  messages.value.push({ role: 'user', text: q })
  scrollToBottom()
  try {
    const res = await knowledgeQa({ question: q })
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
const previewMaximized = ref(false)
const metaCollapsed = ref(false)
/** 预览区高度：普通弹窗按收起状态估算；全屏时交由 flex 精确撑满，避免底部残留空白 */
const previewAreaStyle = computed(() => {
  if (previewMaximized.value) return { height: '100%' }
  return { height: metaCollapsed.value ? '70vh' : '55vh', minHeight: '320px' }
})
const previewDoc = ref({})
const previewTab = ref('preview')
const previewType = ref('none') // pdf / text / docx / none
const previewUrl = ref('')
const previewText = ref('')
const docxContainer = ref(null)
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

/** 加载在线预览内容：pdf用iframe渲染，txt/md显示全文，Office文档优先服务端转PDF（保真），失败回退docx-preview */
async function loadPreviewContent(fileType) {
  previewType.value = 'none'
  previewUrl.value = ''
  previewText.value = ''
  if (!previewDoc.value.docId) return
  const ft = (fileType || '').toLowerCase()
  const convertible = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'rtf', 'odt', 'ods', 'odp']
  if (ft !== 'pdf' && ft !== 'txt' && ft !== 'md' && !convertible.includes(ft)) return
  previewLoading.value = true
  try {
    // Office 文档：先尝试服务端转换后的 PDF（保真渲染，客户端无需控件）
    if (convertible.includes(ft)) {
      const ok = await tryServerPdfPreview()
      if (ok) return
      // 服务端转换不可用：docx 回退前端渲染，其他格式提示下载
      if (ft !== 'docx') {
        previewType.value = 'none'
        return
      }
    }
    const data = await downloadDoc(previewDoc.value.docId)
    if (!data || data.type === 'application/json') return
    if (ft === 'pdf') {
      previewUrl.value = URL.createObjectURL(new Blob([data], { type: 'application/pdf' }))
      previewType.value = 'pdf'
    } else if (ft === 'docx') {
      previewType.value = 'docx'
      await nextTick()
      if (!docxContainer.value) return
      try {
        await renderAsync(data, docxContainer.value, null, {
          className: 'docx', inWrapper: true, ignoreWidth: false, ignoreHeight: false,
          breakPages: true, experimental: true, useBase64URL: true
        })
      } catch (e) {
        previewType.value = 'none'
        proxy.$modal.msgError('文档渲染失败，请下载原文件查看')
      }
    } else {
      const t = await data.text()
      previewText.value = t
      previewType.value = 'text'
    }
  } catch (e) {
    previewType.value = 'none'
  } finally {
    previewLoading.value = false
  }
}

/** 尝试用服务端转换的 PDF 预览（保真渲染）；成功返回 true */
async function tryServerPdfPreview() {
  try {
    const data = await getDocPreviewUrl(previewDoc.value.docId)
    // 转换失败时后端返回 JSON 错误体，需识别并回退
    if (!data || data.type === 'application/json' || data.size < 1000) return false
    previewUrl.value = URL.createObjectURL(new Blob([data], { type: 'application/pdf' }))
    previewType.value = 'pdf'
    return true
  } catch (e) {
    return false
  }
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
  previewMaximized.value = false
  metaCollapsed.value = false
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

/* ===== "输入框+发送按钮"入口（所见即所得，参考用户提供的移动端问答框形态）===== */
.ai-search-box {
  display: flex; align-items: center; gap: 8px;
  width: 260px; height: 32px; padding: 0 4px 0 12px;
  border-radius: 16px;
  border: 1.5px solid #c7b8f5;
  background: #fff; cursor: pointer; user-select: none; flex-shrink: 0;
  transition: all 0.18s;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.12);
}
.ai-search-box:hover {
  border-color: #8b5cf6;
  box-shadow: 0 3px 12px rgba(139, 92, 246, 0.22);
}
.ai-search-box.is-active {
  border-color: #8b5cf6;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
}
.ai-box-search-icon { flex-shrink: 0; color: #8b5cf6; }
.ai-box-placeholder {
  flex: 1; min-width: 0; font-size: 12.5px; color: #a8adb8;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
/* 内嵌发送按钮：紫色渐变实底圆形 */
.ai-box-send {
  flex-shrink: 0; width: 24px; height: 24px; border-radius: 50%;
  background: linear-gradient(135deg, #8b5cf6, #6366f1); color: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.35);
}
/* 暗色模式适配 */
html.dark .ai-search-box { background: rgba(255, 255, 255, 0.06); }
html.dark .ai-box-placeholder { color: #6b7280; }


.ai-entry-btn {
  width: 34px; height: 34px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1); color: #fff; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 3px 10px rgba(99, 102, 241, 0.35); transition: all 0.18s;
}
.ai-entry-btn:hover { transform: translateY(-1px); box-shadow: 0 5px 14px rgba(99, 102, 241, 0.45); }

/* ===== 面板 ===== */
.ai-assistant-panel {
  position: fixed; z-index: 3000; width: 560px; height: 660px;
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

/* ===== 双模式切换条 ===== */
.ai-mode-switch {
  display: flex; align-items: center; gap: 4px;
  padding: 7px 10px; background: #fafaff; border-bottom: 1px solid #f0f0f6;
}
.ai-mode-btn {
  display: inline-flex; align-items: center; gap: 4px;
  border: 1px solid #e4e4f5; border-radius: 14px; background: #fff;
  color: #6b7280; font-size: 12px; font-weight: 600;
  padding: 3px 12px; cursor: pointer; transition: all 0.15s;
}
.ai-mode-btn:hover { border-color: #c7d2fe; color: #4f46e5; }
.ai-mode-btn.is-on {
  background: linear-gradient(135deg, #8b5cf6, #6366f1); border-color: transparent; color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}
.ai-mode-ico { font-size: 12px; }
.ai-mode-tip {
  margin-left: auto; font-size: 11px; color: #9ca3af;
  max-width: 180px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
/* ===== 空状态示例 chips ===== */
.ai-empty-chips { display: flex; flex-wrap: wrap; justify-content: center; gap: 6px; margin-top: 14px; }
.ai-chip {
  border: 1px solid #e4e4f5; border-radius: 12px; background: #fff;
  color: #6b7280; font-size: 11.5px; padding: 3px 10px; cursor: pointer; transition: all 0.15s;
}
.ai-chip:hover:not(:disabled) { border-color: #a5b4fc; color: #4f46e5; background: #eef2ff; }
.ai-chip:disabled { opacity: 0.5; cursor: not-allowed; }
/* ===== 数据查询结果渲染 ===== */
.ai-data-result { display: flex; flex-direction: column; gap: 8px; min-width: 0; width: 100%; }
.ai-data-summary {
  display: flex; align-items: flex-start; gap: 6px; font-size: 12.5px; color: #1f2937; line-height: 1.6;
  background: #f0f9ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 6px 10px;
}
.ai-data-error { font-size: 12px; color: #b45309; background: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 6px 10px; }
.ai-data-table { width: 100%; }
.ai-data-norows { font-size: 12px; color: #6b7280; line-height: 1.7; background: #f8fafc; border-radius: 8px; padding: 8px 12px; }
.ai-data-meta {
  display: flex; align-items: center; gap: 10px; font-size: 11px; color: #9ca3af;
  font-variant-numeric: tabular-nums;
}
.ai-sql-toggle {
  border: none; background: transparent; color: #6366f1; font-size: 11px;
  cursor: pointer; padding: 0; font-weight: 600;
}
.ai-sql-toggle:hover { text-decoration: underline; }
.ai-data-sql {
  margin: 0; background: #0F172A; color: #7DD3FC; font-family: Consolas, Monaco, monospace;
  font-size: 11.5px; line-height: 1.7; border-radius: 8px; padding: 10px 12px;
  white-space: pre-wrap; word-break: break-all; max-height: 160px; overflow-y: auto;
}
/* 数据模式气泡占满宽度以容纳表格 */
.ai-msg.assistant:has(.ai-data-result) { width: 100%; }
.ai-msg.assistant:has(.ai-data-result) .ai-bubble { max-width: 100%; }

/* 模块筛选条已移除（用户反馈无用，2026-09-19）*/

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
.doc-preview-area { overflow:auto; border:1px solid var(--ink-200, #e2e8f0); border-radius:10px; background:#fff; }
.doc-frame { width:100%; height:100%; border:0; display:block; }
.docx-preview-wrap {
  height: 100%; overflow: auto; padding: 10px; background: #f5f5f5;
  :deep(.docx-wrapper) { background: #f5f5f5; padding: 10px; }
  :deep(.docx-wrapper > section.docx) {
    box-shadow: 0 2px 12px rgba(0,0,0,0.15); margin-bottom: 16px; background: #fff;
  }
}
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

/* 来源文档预览弹窗：自定义头部（标题 + 最大化按钮） */
.doc-dialog-header { display: flex; align-items: center; min-width: 0; padding-right: 30px; }
.doc-dialog-title { font-size: 16px; font-weight: 600; color: #1f2937; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.doc-max-btn { margin-left: auto; margin-right: 24px; font-size: 16px; color: #6b7280; }
.doc-max-btn:hover { color: #1d4ed8; }
.doc-collapse-btn { margin-right: 6px; font-size: 14px; }
.doc-collapse-btn:hover { color: #1d4ed8; }
.doc-collapse-text { margin-left: 3px; font-size: 13px; font-weight: 600; }
.doc-collapse-btn .el-icon { transition: transform .2s; }
.doc-collapse-btn .el-icon.is-collapsed { transform: rotate(180deg); }
</style>

<!-- 弹窗根节点 append-to-body 挂到 body 下，需用非 scoped 样式覆盖 -->
<style lang="scss">
/* 弹窗内容高度由预览区内联样式精确控制（见 previewAreaStyle），此处只做容器配合 */
.doc-preview-dialog {
  .el-dialog__body { overflow: hidden; }
  .doc-top-block { flex: none; }
  .doc-preview-area { overflow: auto; }
  .doc-frame { height: 100%; }
  .docx-preview-wrap { height: 100%; }
}
.doc-preview-dialog.is-max {
  /* 注意：doc-preview-dialog 类就挂在 .el-dialog 根元素上，flex 必须设在根节点本身 */
  display: flex;
  flex-direction: column;
  .el-dialog__header { flex: none; }
  .el-dialog__body { flex: 1; min-height: 0; overflow: hidden; display: flex; flex-direction: column; padding-bottom: 12px; }
  .doc-top-block { flex: none; }
  .doc-preview-area { flex: 1; min-height: 0; overflow: auto; }
  .doc-frame { height: 100%; }
  .docx-preview-wrap { height: 100%; }
}
.ai-history-loading { text-align: center; font-size: 12px; color: #c0c4cc; padding: 8px 0; }
</style>
