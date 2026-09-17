<template>
  <div class="app-container nlq-page">
    <!-- 顶部横幅 -->
    <div class="nlq-hero">
      <div class="nlq-hero-left">
        <div class="nlq-hero-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </div>
        <div>
          <div class="nlq-hero-title">智能查询</div>
          <div class="nlq-hero-sub">用自然语言提问，AI 自动生成查询语句并返回数据</div>
        </div>
      </div>
      <div class="nlq-hero-badges">
        <span class="nlq-badge">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
          白名单表
        </span>
        <span class="nlq-badge">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          只读查询
        </span>
        <span class="nlq-badge">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20h16M4 20V8l8-5 8 5v12"/></svg>
          自动限 100 行
        </span>
      </div>
    </div>

    <!-- 查询卡片 -->
    <section class="rd-card nlq-card">
      <div class="nlq-search-row">
        <div class="nlq-search-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        </div>
        <el-input v-model="question" size="large" clearable
          placeholder="试试问：上月故障次数最多的三台设备是哪些？"
          @keyup.enter="handleQuery" :disabled="querying" class="nlq-input" />
        <el-button type="primary" size="large" class="nlq-btn" :loading="querying" @click="handleQuery">
          <svg v-if="!querying" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px">
            <polygon points="5 3 19 12 5 21 5 3" />
          </svg>{{ querying ? '查询中' : '开始查询' }}
        </el-button>
      </div>

      <div class="nlq-suggest">
        <span class="nlq-suggest-label">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M9 11.5a3 3 0 1 1 6 0c0 1.5-3 2-3 4M12 20h.01"/></svg>
          大家都在问
        </span>
        <button v-for="s in suggestions" :key="s.q" class="nlq-chip" :class="{ 'is-active': question === s.q }"
          :disabled="querying" type="button" @click="askExample(s.q)">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>{{ s.t }}
        </button>
      </div>
    </section>

    <!-- 错误提示 -->
    <el-alert v-if="error" :title="error" type="warning" :closable="false" show-icon class="nlq-error" />

    <!-- 加载态：分段思考进度 -->
    <section v-if="querying" class="rd-card nlq-card">
      <div class="nlq-thinking">
        <div class="nlq-thinking-head">
          <span class="nlq-thinking-avatar">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 9h6v6H9z"/><path d="M9 1v3M15 1v3M9 20v3M15 20v3M1 9h3M1 15h3M20 9h3M20 15h3"/>
            </svg>
          </span>
          <span class="nlq-thinking-title">AI 正在思考</span>
          <span class="nlq-thinking-time">{{ elapsed }}s</span>
        </div>
        <div class="nlq-steps">
          <div v-for="(s, i) in steps" :key="s.key" class="nlq-step" :class="{ 'is-done': s.state === 'done', 'is-active': s.state === 'active' }">
            <span class="nlq-step-dot">
              <svg v-if="s.state === 'done'" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
              <span v-else-if="s.state === 'active'" class="nlq-step-spinner"></span>
            </span>
            <span class="nlq-step-label">{{ s.label }}</span>
            <span v-if="i < steps.length - 1" class="nlq-step-line" :class="{ 'is-done': s.state === 'done' }"></span>
          </div>
        </div>
        <div class="nlq-skeleton">
          <div class="nlq-skeleton-row" v-for="n in 3" :key="n"></div>
        </div>
      </div>
    </section>

    <!-- 结果卡片 -->
    <section v-if="!querying && resultColumns.length" class="rd-card nlq-card">
      <div class="nlq-result-head">
        <div class="rd-card-title nlq-result-title">
          <span class="rd-card-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" /><path d="M3 9h18M3 15h18M9 3v18M15 3v18" />
            </svg>
          </span>
          查询结果
        </div>
        <div class="nlq-result-meta">
          <span class="nlq-row-count">共 <b>{{ resultRows.length }}</b> 行</span>
          <el-tooltip content="AI 生成的查询语句（只读）" placement="top">
            <button class="nlq-sql-toggle" :class="{ 'is-open': sqlVisible }" type="button" @click="sqlVisible = !sqlVisible">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
              {{ sqlVisible ? '隐藏 SQL' : '查看 SQL' }}
            </button>
          </el-tooltip>
        </div>
      </div>

      <pre v-if="sqlVisible" class="nlq-sql-view">{{ resultSql }}</pre>

      <el-table :data="resultRows" border size="small" max-height="480" class="nlq-table" :header-cell-style="{ background: '#F8FAFC', color: '#111827', fontWeight: 600 }">
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column v-for="col in resultColumns" :key="col" :label="col" :prop="col" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="nlq-cell">{{ formatCell(row[col]) }}</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="rd-empty"><p class="rd-empty-text">暂无数据</p></div>
        </template>
      </el-table>
    </section>

    <!-- 空状态 -->
    <section v-if="!querying && !resultColumns.length && !error" class="rd-card nlq-card nlq-empty-card">
      <div class="rd-empty">
        <svg class="nlq-empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          <path d="M8 9h8M8 12h5" stroke="#2563EB" />
        </svg>
        <p class="rd-empty-text">在上方输入问题，开始你的第一次智能查询</p>
      </div>
    </section>
  </div>
</template>

<script setup name="AiNlQuery">
import { onUnmounted } from 'vue'
import { nlQuery, nlQueryStream } from '@/api/ai/manage'
import { getToken } from '@/utils/auth'

const question = ref('')
const querying = ref(false)
const error = ref('')
const resultColumns = ref([])
const resultRows = ref([])
const resultSql = ref('')
const sqlVisible = ref(false)

// ===== 分段思考进度 =====
const steps = ref([
  { key: 'understand', label: '理解问题', state: 'pending' },
  { key: 'tables', label: '检索相关数据表', state: 'pending' },
  { key: 'sql', label: '生成查询语句', state: 'pending' },
  { key: 'exec', label: '执行查询', state: 'pending' },
])
const elapsed = ref(0)
let stepTimer = null
let stepIndex = 0

function startThinking() {
  stepIndex = 0
  elapsed.value = 0
  steps.value.forEach(s => { s.state = 'pending' })
  steps.value[0].state = 'active'
  stepTimer && clearInterval(stepTimer)
  stepTimer = setInterval(() => {
    elapsed.value++
  }, 1000)
  // 阶段推进：0s激活第1步，1.2s第2步，2.4s第3步，3.6s第4步
  const advance = () => {
    stepIndex++
    if (stepIndex < steps.value.length) {
      steps.value[stepIndex - 1].state = 'done'
      steps.value[stepIndex].state = 'active'
    }
  }
  setTimeout(advance, 1200)
  setTimeout(advance, 2400)
  setTimeout(advance, 3600)
}

function stopThinking(ok) {
  stepTimer && clearInterval(stepTimer)
  stepTimer = null
  steps.value.forEach(s => { s.state = 'done' })
  if (ok === false) { /* 失败保持完成态即可 */ }
}

onUnmounted(() => { stepTimer && clearInterval(stepTimer) })

const suggestions = [
  { t: '本月工单状态分布', q: '本月工单数量按状态分布如何？' },
  { t: '故障最多的前 5 台设备', q: '故障次数最多的前5台设备' },
  { t: '供应商供货金额排名', q: '各供应商的供货金额排名' },
]

function askExample(q) {
  question.value = q
  handleQuery()
}

function formatCell(v) {
  if (v === null || v === undefined || v === '') return '—'
  return v
}

const STAGE_INDEX = { understand: 0, tables: 1, sql: 2, exec: 3 }
let currentAbort = null

function handleQuery() {
  const q = (question.value || '').trim()
  if (!q) return
  // 取消上一次未完成的请求，避免旧响应交错
  if (currentAbort) { try { currentAbort.abort() } catch (e) {} }
  currentAbort = new AbortController()
  const mySignal = currentAbort.signal
  querying.value = true
  error.value = ''
  resultColumns.value = []
  resultRows.value = []
  sqlVisible.value = false
  startThinking()
  const onDone = (d) => {
    querying.value = false
    stopThinking()
    if (d.error) {
      error.value = d.error
      if (d.sql) { resultSql.value = d.sql }
      return
    }
    resultColumns.value = d.columns || []
    resultRows.value = d.rows || []
    resultSql.value = d.sql || ''
    if (!resultRows.value.length) {
      error.value = '查询结果为空，请尝试调整问题表述'
    }
  }
  // 真流式：后端实时推送阶段事件；失败降级为普通接口
  nlQueryStream(q,
    (stage, status) => {
      const i = STAGE_INDEX[stage]
      if (i === undefined) return
      if (status === 'start') {
        steps.value[i].state = 'active'
      } else if (status === 'done') {
        steps.value[i].state = 'done'
        if (steps.value[i + 1] && steps.value[i + 1].state === 'pending') steps.value[i + 1].state = 'active'
      }
    },
    (payload) => {
      onDone(payload && payload.data ? payload.data : payload)
    },
    currentAbort.signal
  ).catch((err) => {
    // 被新查询取代：静默忽略；60秒超时：直接提示；其他：降级非流式接口
    if (currentAbort && currentAbort.signal !== mySignal) return
    if (err && err.isExternal) return
    if (err && err.isTimeout) {
      querying.value = false
      stopThinking()
      error.value = '查询超时（60秒），请稍后重试或调整问题表述'
      return
    }
    nlQuery(q).then(res => { onDone(res.data || {}) }).catch(() => { querying.value = false; stopThinking() })
  })
}
</script>

<style scoped lang="scss">
.nlq-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 顶部横幅：遵循规范 5.2 标题横幅配色 ===== */
.nlq-hero {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%);
  border-radius: 10px;
  padding: 20px 24px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.25);
}
.nlq-hero-left { display: flex; align-items: center; gap: 14px; }
.nlq-hero-icon {
  width: 40px; height: 40px; border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.nlq-hero-title { font-size: 18px; font-weight: 700; line-height: 1.3; }
.nlq-hero-sub { font-size: 12px; color: rgba(255, 255, 255, 0.85); margin-top: 2px; }
.nlq-hero-badges { display: flex; gap: 8px; flex-wrap: wrap; }
.nlq-badge {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 12px; border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-size: 12px; color: rgba(255, 255, 255, 0.95);
}

/* ===== 查询卡片 ===== */
.nlq-card { margin: 0; padding: 20px; }
.nlq-empty-card { padding: 40px 20px; text-align: center; }
.nlq-search-row { display: flex; align-items: center; gap: 12px; }
.nlq-search-icon {
  width: 38px; height: 38px; border-radius: 10px; flex-shrink: 0;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  color: #2563eb;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid #bfdbfe;
}
.nlq-search-row :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb inset;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 0 0 1px #93c5fd inset; }
  &.is-focus { box-shadow: 0 0 0 2px #2563eb inset; }
}
.nlq-search-row :deep(.el-input__inner) { font-size: 15px; }
.nlq-btn {
  border-radius: 8px; font-weight: 600; padding: 0 22px;
  background: #2563eb; border-color: #2563eb;
  display: inline-flex; align-items: center;
  &:hover, &:focus { background: #1d4ed8; border-color: #1d4ed8; }
}

/* ===== 示例 chips ===== */
.nlq-suggest { margin-top: 14px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.nlq-suggest-label { font-size: 12px; color: #9ca3af; display: inline-flex; align-items: center; gap: 4px; }
.nlq-chip {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 12px; border-radius: 999px;
  background: #f9fafb; border: 1px solid #e5e7eb;
  color: #6b7280; font-size: 12px; cursor: pointer;
  transition: all 0.2s;
  &:hover { color: #2563eb; border-color: #93c5fd; background: #eff6ff; }
  &.is-active { color: #2563eb; border-color: #2563eb; background: #eff6ff; font-weight: 600; }
  &:disabled { opacity: 0.5; cursor: not-allowed; }
}

/* ===== 加载态：分段思考 ===== */
.nlq-thinking { max-width: 560px; margin: 0 auto; }
.nlq-thinking-head { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.nlq-thinking-avatar {
  width: 30px; height: 30px; border-radius: 8px; flex-shrink: 0;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid #bfdbfe; color: #2563eb;
  display: flex; align-items: center; justify-content: center;
}
.nlq-thinking-title { font-size: 14px; font-weight: 600; color: #111827; }
.nlq-thinking-time { margin-left: auto; font-size: 12px; color: #9ca3af; font-variant-numeric: tabular-nums; }
.nlq-steps { display: flex; align-items: center; margin-bottom: 16px; }
.nlq-step { display: flex; align-items: center; }
.nlq-step-dot {
  width: 22px; height: 22px; border-radius: 50%; flex-shrink: 0;
  border: 2px solid #e5e7eb; background: #f9fafb;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.3s;
}
.nlq-step.is-done .nlq-step-dot { background: #2563eb; border-color: #2563eb; color: #fff; }
.nlq-step.is-active .nlq-step-dot { border-color: #2563eb; background: #eff6ff; }
.nlq-step-label { margin: 0 8px 0 6px; font-size: 12px; color: #9ca3af; white-space: nowrap; transition: color 0.3s; }
.nlq-step.is-done .nlq-step-label, .nlq-step.is-active .nlq-step-label { color: #111827; font-weight: 600; }
.nlq-step-line { width: 18px; height: 2px; background: #e5e7eb; margin-right: 8px; border-radius: 1px; transition: background 0.3s; }
.nlq-step-line.is-done { background: #2563eb; }
.nlq-step-spinner {
  width: 10px; height: 10px; border-radius: 50%;
  border: 2px solid #93c5fd; border-top-color: #2563eb;
  display: inline-block; animation: nlqSpin 0.8s linear infinite;
}
@keyframes nlqSpin { to { transform: rotate(360deg); } }
/* ===== 旧加载动画（保留兼容） ===== */
.nlq-loading { display: flex; align-items: center; gap: 6px; margin-bottom: 16px; }
.nlq-loading-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #2563eb;
  animation: nlqBounce 1.2s infinite ease-in-out;
  &:nth-child(2) { animation-delay: 0.15s; }
  &:nth-child(3) { animation-delay: 0.3s; }
}
@keyframes nlqBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}
.nlq-loading-text { margin-left: 8px; font-size: 13px; color: #6b7280; }
.nlq-skeleton { display: flex; flex-direction: column; gap: 10px; }
.nlq-skeleton-row {
  height: 34px; border-radius: 6px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e5e7eb 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: nlqShimmer 1.4s infinite;
}
@keyframes nlqShimmer { to { background-position: -200% 0; } }

/* ===== 错误提示 ===== */
.nlq-error { border-radius: 8px; }

/* ===== 结果卡片 ===== */
.nlq-result-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12px; flex-wrap: wrap; gap: 8px;
}
.nlq-result-title { display: flex; align-items: center; gap: 8px; font-size: 15px; }
.nlq-result-meta { display: flex; align-items: center; gap: 14px; }
.nlq-row-count { font-size: 13px; color: #6b7280; b { color: #111827; font-variant-numeric: tabular-nums; } }
.nlq-sql-toggle {
  display: inline-flex; align-items: center; gap: 4px;
  border: none; background: transparent; cursor: pointer;
  font-size: 13px; color: #2563eb; padding: 2px 0;
  &:hover { color: #1d4ed8; text-decoration: underline; }
  &.is-open { color: #6b7280; }
}

/* SQL 代码块：深色终端风 */
.nlq-sql-view {
  background: #0f172a;
  color: #7dd3fc;
  border-radius: 8px;
  padding: 14px 16px;
  font-size: 12.5px;
  line-height: 1.7;
  font-family: Consolas, Monaco, 'Courier New', monospace;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0 0 12px 0;
  border: 1px solid #1e293b;
}

.nlq-table { width: 100%; border-radius: 8px; }
.nlq-cell { font-variant-numeric: tabular-nums; color: #374151; }

/* ===== 空状态 ===== */
.nlq-empty-icon { color: #d1d5db; margin-bottom: 8px; }
</style>
