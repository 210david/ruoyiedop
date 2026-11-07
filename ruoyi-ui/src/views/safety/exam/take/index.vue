<template>
  <div class="app-container safety-exam-take-page">
    <!-- ===== 列表态 ===== -->
    <template v-if="mode === 'list'">
      <div class="surface list-head-card">
        <div class="lh-title"><span class="glyph"></span> 我的考试</div>
        <el-button icon="Refresh" circle @click="loadList" />
      </div>
      <!-- 筛选栏：状态页签 + 名称搜索 -->
      <div class="surface filter-bar">
        <el-radio-group v-model="query.status" @change="onFilterChange">
          <el-radio-button v-for="t in statusTabs" :key="t.value" :value="t.value">{{ t.label }}（{{ statusCount(t.value) }}）</el-radio-button>
        </el-radio-group>
        <el-input v-model="query.keyword" placeholder="搜索考试名称" clearable style="width: 220px" @input="onFilterChange">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div class="exam-grid" v-loading="loading">
        <div class="surface exam-card" v-for="item in pagedExams" :key="item.recordId">
          <div class="ec-head">
            <span class="ec-name">{{ item.examName }}</span>
            <span class="badge" :class="statusBadgeClass(item.examStatus)"><span class="dot"></span>{{ statusLabel(item.examStatus) }}</span>
          </div>
          <div class="ec-meta">
            <div class="meta-row"><span class="meta-k">试卷</span><span>{{ item.paperName }}</span></div>
            <div class="meta-row"><span class="meta-k">窗口</span><span>{{ fmtTime(item.startTime) }} ~ {{ fmtTime(item.endTime) }}</span></div>
            <div class="meta-row"><span class="meta-k">时长 / 题数</span><span>{{ item.duration }} 分钟 / {{ item.questionCount }} 题</span></div>
            <div class="meta-row" v-if="item.examResult === '2'"><span class="meta-k">成绩</span>
              <span class="score-text" :class="item.isPass === '1' ? 'pass' : 'fail'">{{ item.totalScore }} 分（{{ item.isPass === '1' ? '及格' : '不及格' }}）</span>
            </div>
          </div>
          <div class="ec-footer">
            <span class="badge" :class="resultBadgeClass(item.examResult)">{{ resultLabel(item.examResult) }}</span>
            <div class="ec-actions">
              <el-button v-if="canTake(item)" type="primary" size="small" @click="handleTake(item)">进入考试</el-button>
              <el-button v-else-if="item.examResult === '2'" type="primary" plain size="small" @click="handleViewResult(item)">查看成绩</el-button>
              <el-button v-else disabled size="small">{{ item.examResult === '3' ? '已缺考' : '未开始' }}</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && !pagedExams.length" description="暂无符合条件的考试" style="grid-column: 1 / -1" />
      </div>
      <!-- 分页 -->
      <div class="surface pager-bar" v-if="filteredExams.length > query.pageSize">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="filteredExams.length" :page-sizes="[9, 18, 36]" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" />
      </div>
    </template>

    <!-- ===== 答题态 ===== -->
    <taking v-else-if="mode === 'taking'" :start-data="startData" @submitted="onSubmitted" @error="onTakeError" @exit="backToList" />

    <!-- ===== 成绩态 ===== -->
    <result-view v-else-if="mode === 'result'" :result-data="resultData" @back="backToList" />
  </div>
</template>

<script setup name="SafetyExamTake">
import { getMyExams, startExam, getExamResult } from '@/api/safety/examTake'
import Taking from './taking.vue'
import ResultView from './result.vue'

const { proxy } = getCurrentInstance()

const mode = ref('list')
const loading = ref(false)
const myExams = ref([])
const startData = ref({})
const resultData = ref({})

/** 友好时间显示：ISO 原始格式（2026-11-05T00:00:00）→ 2026-11-05 00:00 */
function fmtTime(t) {
  if (!t) return '-'
  return String(t).replace('T', ' ').slice(0, 16)
}

function loadList() {
  loading.value = true
  getMyExams().then(res => { myExams.value = res.data || [] }).catch(e => { console.error(e) }).finally(() => { loading.value = false })
}

/* ===== 筛选 + 分页（考试过多时本地过滤，响应快且不动后端） ===== */
const query = reactive({ status: 'all', keyword: '', pageNum: 1, pageSize: 9 })
const statusTabs = [
  { value: 'all', label: '全部' },
  { value: 'ongoing', label: '进行中' },
  { value: 'upcoming', label: '未开始' },
  { value: 'finished', label: '已结束' }
]
/** 考生视角分类：进行中（场次考试中且未交卷）/ 未开始 / 已结束（交卷、判分、缺考或场次结束） */
function examBucket(item) {
  if (item.examStatus === '2' && (item.examResult === '0' || item.examResult === '1')) return 'ongoing'
  if (item.examStatus === '1' || item.examResult === '0') return 'upcoming'
  return 'finished'
}
const filteredExams = computed(() => {
  const kw = (query.keyword || '').trim().toLowerCase()
  const list = myExams.value.filter(it => {
    if (query.status !== 'all' && examBucket(it) !== query.status) return false
    if (kw && !(it.examName || '').toLowerCase().includes(kw)) return false
    return true
  })
  const order = { ongoing: 0, upcoming: 1, finished: 2 }
  return [...list].sort((a, b) => {
    const oa = order[examBucket(a)], ob = order[examBucket(b)]
    if (oa !== ob) return oa - ob
    // 同组内：进行中临近结束优先、未开始最近开始优先、已结束最近结束优先（时间字符串字典序即时间序）
    if (oa === 0) return String(a.endTime || '').localeCompare(String(b.endTime || ''))
    if (oa === 1) return String(a.startTime || '').localeCompare(String(b.startTime || ''))
    return String(b.endTime || '').localeCompare(String(a.endTime || ''))
  })
})
const pagedExams = computed(() => filteredExams.value.slice((query.pageNum - 1) * query.pageSize, query.pageNum * query.pageSize))
function statusCount(v) { return v === 'all' ? myExams.value.length : myExams.value.filter(it => examBucket(it) === v).length }
function onFilterChange() { query.pageNum = 1 }
function canTake(item) {
  return item.examStatus === '2' && (item.examResult === '0' || item.examResult === '1')
}
function handleTake(item) {
  startExam(item.recordId).then(res => {
    const limit = Number(res.data && res.data.switchLimit) || 0
    const tip = limit > 0
      ? `考试将在全屏模式下进行，全程禁用复制/右键；切屏（切窗口/切标签/退出全屏）${limit} 次将强制交卷，确认进入考试？`
      : '考试将在全屏模式下进行，全程禁用复制/右键；切屏行为将被记录，确认进入考试？'
    proxy.$modal.confirm(tip).then(() => {
      startData.value = res.data
      mode.value = 'taking'
    }).catch(() => {})
  }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}
function handleViewResult(item) {
  getExamResult(item.recordId).then(res => {
    resultData.value = res.data
    mode.value = 'result'
  }).catch(e => { if (e && e.message) proxy.$modal.msgError(e.message) })
}
function onSubmitted(result) {
  proxy.$modal.alert(`交卷成功，本次得分 ${result.totalScore} 分（${result.isPass ? '及格' : '不及格'}）`).then(() => {
    backToList()
  })
}
function onTakeError(e) {
  if (e && e.message) proxy.$modal.msgError(e.message)
  backToList()
}
function backToList() { mode.value = 'list'; loadList() }

/* ===== 展示辅助 ===== */
function statusLabel(s) { return { '0': '草稿', '1': '未开始', '2': '考试中', '3': '已结束', '4': '已撤回' }[s] || '-' }
function statusBadgeClass(s) { return { '0': 'gray', '1': 'blue', '2': 'green', '3': 'gray', '4': 'red' }[s] || 'gray' }
function resultLabel(r) { return { '0': '未开始', '1': '进行中', '2': '已交卷', '3': '缺考', '4': '已判分' }[r] || '-' }
function resultBadgeClass(r) { return { '0': 'gray', '1': 'blue', '2': 'green', '3': 'red', '4': 'green' }[r] || 'gray' }

onActivated(() => { if (mode.value === 'list') loadList() })
loadList()
</script>

<style scoped>
.safety-exam-take-page { padding-top:10px; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --brand-600:#4f46e5; --brand-200:#c7d2fe; }
.surface { background:#fff; border:1px solid var(--ink-200); border-radius:12px; box-shadow:0 1px 2px rgba(15,23,42,.05); }
.list-head-card { display:flex; align-items:center; justify-content:space-between; padding:14px 20px; margin-bottom:8px; }
.lh-title { display:flex; align-items:center; gap:8px; font-size:15px; font-weight:700; color:var(--ink-900); }
.lh-title .glyph { width:4px; height:16px; background:var(--brand-600); border-radius:2px; }
.exam-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(360px, 1fr)); gap:10px; }
.filter-bar { display:flex; align-items:center; justify-content:space-between; padding:10px 20px; margin-bottom:8px; flex-wrap:wrap; gap:8px; }
.pager-bar { display:flex; justify-content:flex-end; padding:10px 20px; margin-top:8px; }
.exam-card { padding:16px 20px; display:flex; flex-direction:column; gap:12px; }
.ec-head { display:flex; align-items:center; justify-content:space-between; gap:10px; }
.ec-name { font-size:15px; font-weight:700; color:var(--ink-900); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.ec-meta { display:flex; flex-direction:column; gap:5px; }
.meta-row { display:flex; gap:12px; font-size:13px; color:var(--ink-700); }
.meta-k { color:var(--ink-400); width:76px; flex-shrink:0; }
.score-text { font-weight:700; }
.score-text.pass { color:var(--green-700); }
.score-text.fail { color:var(--red-700); }
.ec-footer { display:flex; align-items:center; justify-content:space-between; padding-top:10px; border-top:1px dashed var(--ink-200); }
.ec-actions { display:flex; gap:6px; }
.badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.badge .dot { width:6px; height:6px; border-radius:50%; }
.badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .badge.green .dot { background:var(--green-500); }
.badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .badge.red .dot { background:var(--red-500); }
.badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .badge.blue .dot { background:var(--blue-500); }
.badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .badge.gray .dot { background:var(--ink-400); }
</style>
