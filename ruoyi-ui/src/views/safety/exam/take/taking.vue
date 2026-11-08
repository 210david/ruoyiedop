<template>
  <div class="taking-wrap" ref="takingRef">
    <!-- 全屏恢复遮罩：Esc/浏览器×退出全屏后点击恢复 -->
    <div class="fs-mask" v-if="fsMask" @click="resumeFullscreen">
      <div class="fs-mask-inner">考试须全屏进行，请点击任意位置恢复全屏</div>
    </div>
    <!-- 顶栏 -->
    <div class="surface taking-top">
      <div class="taking-info">
        <div class="taking-title">{{ startData.examName }}</div>
        <div class="taking-sub">
          <span>{{ startData.paperName }}</span>
          <span class="divider"></span>
          <span>共 {{ questionList.length }} 题</span>
          <span class="divider"></span>
          <span>及格 {{ startData.passScore }} 分</span>
          <span class="divider" v-if="switchLimit > 0"></span>
          <span class="switch-tip" v-if="switchLimit > 0">切屏超过 {{ switchLimit }} 次将强制交卷</span>
        </div>
      </div>
      <div class="taking-timer" :class="{ 'is-danger': remainSeconds <= 60 }">
        <el-icon><Timer /></el-icon>
        <span class="timer-text">{{ timerText }}</span>
        <template v-if="exitConfirming">
          <span class="exit-confirm-text">确认退出考试？</span>
          <el-button size="small" type="danger" @click="doExit">确认退出</el-button>
          <el-button size="small" @click="exitConfirming = false">继续考试</el-button>
        </template>
        <el-button v-else class="exit-btn" link type="danger" icon="SwitchButton" @click="exitConfirming = true">退出考试</el-button>
      </div>
    </div>

    <!-- 题目卡 -->
    <div class="surface taking-body">
      <div class="q-head">
        <span class="q-index">第 {{ currentIdx + 1 }} / {{ questionList.length }} 题</span>
        <span class="badge" :class="typeBadgeClass(currentQuestion.questionType)">{{ typeLabel(currentQuestion.questionType) }}</span>
        <span class="q-answered" v-if="isAnswered(currentQuestion)">已作答</span>
      </div>
      <div class="q-content">{{ currentQuestion.content }}</div>
      <div class="q-options">
        <!-- 单选 -->
        <div v-if="currentQuestion.questionType === '1'" class="option-item" v-for="opt in displayOptions(currentQuestion)" :key="opt.key"
          :class="{ 'is-selected': answers[currentQuestion.paperQuestionId] === opt.letter }" @click="setSingle(currentQuestion, opt.letter)">
          <span class="option-letter">{{ opt.letter }}</span>
          <span class="option-text">{{ opt.text }}</span>
        </div>
        <!-- 多选 -->
        <template v-if="currentQuestion.questionType === '2'">
          <div class="option-item" v-for="opt in displayOptions(currentQuestion)" :key="opt.key"
            :class="{ 'is-selected': isMultiChecked(currentQuestion, opt.letter) }" @click="toggleMulti(currentQuestion, opt.letter)">
            <span class="option-letter">{{ opt.letter }}</span>
            <span class="option-text">{{ opt.text }}</span>
          </div>
          <div class="multi-tip">多选题：可同时选择多个答案</div>
        </template>
        <!-- 判断 -->
        <div v-if="currentQuestion.questionType === '3'" class="option-item" v-for="judge in judgeOptions" :key="judge.value"
          :class="{ 'is-selected': answers[currentQuestion.paperQuestionId] === judge.value }" @click="setSingle(currentQuestion, judge.value)">
          <span class="option-letter">{{ judge.letter }}</span>
          <span class="option-text">{{ judge.value }}</span>
        </div>
      </div>

      <div class="q-nav">
        <el-button :disabled="currentIdx === 0" @click="prevQuestion">上一题</el-button>
        <el-button :disabled="currentIdx >= questionList.length - 1" type="primary" plain @click="nextQuestion">下一题</el-button>
        <div class="q-nav-right">
          <span class="draft-tip" v-if="draftSaving">草稿保存中…</span>
          <span class="draft-tip saved" v-else-if="draftSaved">草稿已自动保存</span>
          <el-button type="danger" @click="confirmSubmit">交卷</el-button>
        </div>
      </div>
    </div>

    <!-- 答题卡 -->
    <div class="surface taking-card">
      <div class="card-head">答题卡 <span class="card-stat">已答 {{ answeredCount }} / {{ questionList.length }}</span>
        <span class="card-legend"><i class="lg lg-done"></i>已答<i class="lg lg-todo"></i>未答<i class="lg lg-cur"></i>当前</span>
      </div>
      <div class="card-grid">
        <div class="card-item" v-for="(q, idx) in questionList" :key="q.paperQuestionId"
          :class="{ 'is-current': idx === currentIdx, 'is-answered': isAnswered(q) }" @click="jumpTo(idx)">{{ idx + 1 }}</div>
      </div>
    </div>
  </div>
</template>

<script setup name="SafetyExamTaking">
import { submitExam, saveExamDraft } from '@/api/safety/examTake'
import { Timer } from '@element-plus/icons-vue'

const props = defineProps({ startData: { type: Object, required: true } })
const emit = defineEmits(['submitted', 'error', 'exit'])

const { proxy } = getCurrentInstance()
const judgeOptions = [ { letter: 'A', value: '对' }, { letter: 'B', value: '错' } ]
const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']

// 题目乱序：仅打乱展示顺序（Fisher-Yates）
const questionList = ref([])
{
  const list = props.startData.questions || []
  questionList.value = String(props.startData.shuffleQuestion) === 'Y' ? shuffle(list) : list
}
// 选项乱序映射：每题固定生成一次（字母标签跟随原 index，保证判分一致）
const optionOrderMap = {}
if (String(props.startData.shuffleOption) === 'Y') {
  questionList.value.forEach(q => { optionOrderMap[q.paperQuestionId] = shuffle((q.options || []).map((_, i) => i)) })
}

function shuffle(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

const answers = reactive({})
// 回显草稿
const drafts = props.startData.drafts || {}
Object.keys(drafts).forEach(k => { if (drafts[k]) answers[k] = drafts[k] })

const currentIdx = ref(0)
const remainSeconds = ref(props.startData.remainSeconds || 0)
const switchCount = ref(0)
const switchLimit = computed(() => Number(props.startData.switchLimit) || 0)
const draftSaving = ref(false)
const draftSaved = ref(false)

const currentQuestion = computed(() => questionList.value[currentIdx.value] || {})
const answeredCount = computed(() => questionList.value.filter(q => isAnswered(q)).length)
const timerText = computed(() => {
  const s = remainSeconds.value
  const h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60), sec = s % 60
  const pad = n => String(n).padStart(2, '0')
  return h > 0 ? `${pad(h)}:${pad(m)}:${pad(sec)}` : `${pad(m)}:${pad(sec)}`
})

const currentQuestionId = computed(() => currentQuestion.value.paperQuestionId)

function displayOptions(q) {
  const opts = q.options || []
  const order = optionOrderMap[q.paperQuestionId] || opts.map((_, i) => i)
  return order.map(i => ({ key: i, letter: optionLetters[i], text: opts[i] }))
}
function isAnswered(q) {
  const v = answers[q.paperQuestionId]
  return v !== undefined && v !== null && v !== ''
}
function isMultiChecked(q, letter) {
  const v = answers[q.paperQuestionId]
  return Array.isArray(v) && v.includes(letter)
}
function setSingle(q, letter) {
  answers[q.paperQuestionId] = letter
  scheduleDraft()
  autoAdvance()
}
function toggleMulti(q, letter) {
  const cur = answers[q.paperQuestionId]
  const arr = Array.isArray(cur) ? [...cur] : []
  const idx = arr.indexOf(letter)
  if (idx >= 0) arr.splice(idx, 1); else arr.push(letter)
  answers[q.paperQuestionId] = arr
  scheduleDraft()
}
function autoAdvance() {
  if (currentIdx.value < questionList.value.length - 1) {
    // 单选/判断作答后 300ms 自动跳下一题（最后一题不跳）
    setTimeout(() => { if (currentIdx.value < questionList.value.length - 1) currentIdx.value++ }, 300)
  }
}
function prevQuestion() { if (currentIdx.value > 0) currentIdx.value-- }
function nextQuestion() { if (currentIdx.value < questionList.value.length - 1) currentIdx.value++ }
function jumpTo(idx) { currentIdx.value = idx }

/* ===== 草稿自动保存（防抖 2s） ===== */
let draftTimer = null
function scheduleDraft() {
  draftSaved.value = false
  if (draftTimer) clearTimeout(draftTimer)
  draftTimer = setTimeout(saveDraft, 2000)
}
function saveDraft() {
  if (draftSaving.value) return
  draftSaving.value = true
  saveExamDraft({ recordId: props.startData.recordId, answers: serializeAnswers() }).then(() => {
    draftSaved.value = true
  }).catch(() => {}).finally(() => { draftSaving.value = false })
}
function serializeAnswers() {
  const list = []
  questionList.value.forEach(q => {
    let v = answers[q.paperQuestionId]
    if (v === undefined || v === null) return
    if (Array.isArray(v)) { if (!v.length) return; v = [...v].sort().join('') }
    list.push({ paperQuestionId: q.paperQuestionId, userAnswer: String(v) })
  })
  return list
}

/* ===== 倒计时 + 切屏检测 + 无操作锁卷 ===== */
let timer = null
let submitted = false
let idleSeconds = 0
const IDLE_WARN_SECONDS = 300   // 5 分钟无操作弹窗警告
const IDLE_LOCK_SECONDS = 600   // 10 分钟无操作自动锁卷（FR-13）
onMounted(() => {
  timer = setInterval(() => {
    if (remainSeconds.value > 0) remainSeconds.value--
    // FR-13：无操作检测（mousemove/keydown 活动会重置计数）
    idleSeconds++
    if (idleSeconds === IDLE_WARN_SECONDS && !submitted) {
      proxy.$modal.msgWarning('已 5 分钟无操作，请继续作答；连续 10 分钟无操作将自动锁卷提交')
    }
    if (idleSeconds >= IDLE_LOCK_SECONDS && !submitted) {
      proxy.$modal.msgWarning('连续 10 分钟无操作，试卷已自动锁卷提交')
      doSubmit('4')
    }
    if (remainSeconds.value <= 0 && !submitted) {
      doSubmit('2')
    }
  }, 1000)
  document.addEventListener('visibilitychange', onVisibility)
  window.addEventListener('blur', onWindowBlur)
  // 防复制（FR-13）
  document.addEventListener('copy', preventEvent)
  document.addEventListener('cut', preventEvent)
  document.addEventListener('contextmenu', preventEvent)
  document.addEventListener('selectstart', preventEvent)
  // 无操作监测（FR-13）
  document.addEventListener('mousemove', onActivity)
  document.addEventListener('keydown', onActivity)
  enterFullscreen()
})
onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', onVisibility)
  window.removeEventListener('blur', onWindowBlur)
  document.removeEventListener('copy', preventEvent)
  document.removeEventListener('cut', preventEvent)
  document.removeEventListener('contextmenu', preventEvent)
  document.removeEventListener('selectstart', preventEvent)
  document.removeEventListener('mousemove', onActivity)
  document.removeEventListener('keydown', onActivity)
  exitFullscreen()
})
function preventEvent(e) { e.preventDefault() }
function onActivity() { idleSeconds = 0 }
/* ===== 全屏答题（FR-13 反作弊）：对答题页自身根元素全屏，屏幕上只剩试卷 ===== */
const takingRef = ref(null)
function enterFullscreen() {
  const el = takingRef.value
  if (el && el.requestFullscreen) { el.requestFullscreen().catch(() => {}) }
}
function exitFullscreen() {
  if (document.fullscreenElement && document.exitFullscreen) { document.exitFullscreen().catch(() => {}) }
}
document.addEventListener('fullscreenchange', onFullscreenChange)
onBeforeUnmount(() => { document.removeEventListener('fullscreenchange', onFullscreenChange) })
/** 统一切屏计数 + 超限强制交卷（visibility / blur / 退出全屏共用；1 秒内多事件合并计 1 次） */
let lastSwitchAt = 0
function countSwitch() {
  const now = Date.now()
  if (now - lastSwitchAt < 1000) return
  lastSwitchAt = now
  switchCount.value++
  if (switchLimit.value > 0 && switchCount.value >= switchLimit.value && !submitted) {
    proxy.$modal.msgWarning(`切屏已达 ${switchLimit.value} 次上限，试卷将强制提交`)
    doSubmit('3')
  }
}
function onWindowBlur() {
  // 切换窗口/点击系统其他应用（visibilitychange 捕获不到的场景）
  if (!submitted) { countSwitch() }
}
function onVisibility() {
  if (document.visibilityState === 'hidden') { countSwitch() }
}

/* ===== 退出考试（全屏内内联确认，避免弹层被全屏层遮挡） ===== */
const exiting = ref(false)
const exitConfirming = ref(false)
const fsMask = ref(false)
function doExit() {
  exiting.value = true
  submitted = true // 防止退出过程的 blur/visibility 误触强制交卷
  exitConfirming.value = false
  exitFullscreen()
  emit('exit')
}
function onFullscreenChange() {
  if (document.fullscreenElement) return
  if (exiting.value || submitted) return
  // 用户要求：Esc/浏览器×退出全屏不作为退出考试路径，立即自动恢复全屏
  enterFullscreen()
  setTimeout(() => {
    if (!document.fullscreenElement && !exiting.value && !submitted) { fsMask.value = true }
  }, 400)
}
function resumeFullscreen() {
  fsMask.value = false
  enterFullscreen()
}

/* ===== 交卷 ===== */
function confirmSubmit() {
  const unanswered = questionList.value.length - answeredCount.value
  const tip = unanswered > 0 ? `还有 ${unanswered} 题未作答，交卷后不可修改，确认交卷？` : '确认交卷？'
  proxy.$modal.confirm(tip).then(() => { doSubmit('1') }).catch(() => {})
}
function doSubmit(submitType) {
  if (submitted) return
  submitted = true
  submitExam({ recordId: props.startData.recordId, answers: serializeAnswers(), switchCount: switchCount.value, submitType }).then(res => {
    emit('submitted', res.data)
  }).catch(e => {
    submitted = false
    emit('error', e)
  })
}

/* ===== 展示辅助 ===== */
function typeLabel(type) { return { '1': '单选题', '2': '多选题', '3': '判断题' }[type] || '-' }
function typeBadgeClass(type) { return { '1': 'blue', '2': 'violet', '3': 'green' }[type] || 'gray' }
</script>

<style scoped>
.taking-wrap { display: flex; flex-direction: column; gap: 8px; --brand-600:#4f46e5; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --blue-50:#eff6ff; --blue-700:#1d4ed8; --violet-50:#f5f3ff; }
/* 全屏态：根元素铺满整屏并给背景，屏幕上只剩试卷 */
.taking-wrap:fullscreen { background: var(--ink-50, #f8fafc); padding: 16px; overflow: auto; }
.exit-btn { margin-left: 8px; }
.exit-confirm-text { margin-left: 8px; font-size: 13px; color: var(--red-700, #b91c1c); font-weight: 600; }
/* 全屏恢复遮罩 */
.fs-mask { position: fixed; inset: 0; background: rgba(15, 23, 42, .88); z-index: 3000; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.fs-mask-inner { color: #fff; font-size: 16px; font-weight: 600; letter-spacing: 1px; }
.surface { background:#fff; border:1px solid var(--ink-200); border-radius:12px; box-shadow:0 1px 2px rgba(15,23,42,.05); }
.taking-top { display:flex; align-items:center; justify-content:space-between; padding:14px 20px; }
.taking-title { font-size:16px; font-weight:700; color:var(--ink-900); }
.taking-sub { display:flex; align-items:center; gap:8px; margin-top:6px; font-size:13px; color:var(--ink-500); flex-wrap:wrap; }
.taking-sub .divider { width:1px; height:12px; background:var(--ink-200); }
.switch-tip { color:var(--red-700); }
.taking-timer { display:flex; align-items:center; gap:8px; padding:8px 16px; border-radius:10px; background:var(--ink-50); font-size:20px; font-weight:700; color:var(--ink-900); font-variant-numeric:tabular-nums; }
.taking-timer.is-danger { background:var(--red-50); color:var(--red-700); animation:pulse 1s infinite; }
@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:.75; } }
.taking-body { padding:18px 24px; user-select:none; }
.q-head { display:flex; align-items:center; gap:10px; margin-bottom:12px; }
.q-index { font-size:13px; font-weight:600; color:var(--ink-500); }
.q-answered { font-size:12px; color:var(--green-700); background:var(--green-50); padding:2px 8px; border-radius:999px; }
.q-content { font-size:16px; font-weight:600; color:var(--ink-900); line-height:1.7; margin-bottom:16px; }
.q-options { display:flex; flex-direction:column; gap:10px; }
.option-item { display:flex; align-items:center; gap:12px; padding:12px 16px; border:1px solid var(--ink-200); border-radius:10px; cursor:pointer; transition:all .15s; }
.option-item:hover { border-color:var(--brand-600); background:#fafbff; }
.option-item.is-selected { border-color:var(--brand-600); background:#eef2ff; }
.option-letter { width:28px; height:28px; border-radius:50%; background:var(--ink-100); color:var(--ink-500); display:inline-flex; align-items:center; justify-content:center; font-size:14px; font-weight:700; flex-shrink:0; }
.option-item.is-selected .option-letter { background:var(--brand-600); color:#fff; }
.option-text { font-size:14px; color:var(--ink-700); line-height:1.6; }
.multi-tip { font-size:12px; color:var(--ink-400); }
.q-nav { display:flex; align-items:center; gap:8px; margin-top:20px; padding-top:16px; border-top:1px dashed var(--ink-200); }
.q-nav-right { margin-left:auto; display:flex; align-items:center; gap:10px; }
.draft-tip { font-size:12px; color:var(--ink-400); }
.draft-tip.saved { color:var(--green-700); }
.taking-card { padding:14px 20px; }
.card-head { font-size:14px; font-weight:600; color:var(--ink-700); margin-bottom:12px; display:flex; align-items:center; gap:10px; }
.card-stat { font-size:12px; font-weight:500; color:var(--ink-400); }
.card-grid { display:flex; flex-wrap:wrap; gap:8px; }
.card-item { width:36px; height:36px; border-radius:8px; background:#fff; color:var(--ink-500); display:inline-flex; align-items:center; justify-content:center; font-size:13px; font-weight:600; cursor:pointer; border:2px solid var(--ink-200); box-shadow:0 1px 2px rgba(15,23,42,.04); transition:all .15s; }
.card-item:hover { border-color:var(--brand-600); color:var(--brand-600); }
.card-item.is-answered { background:var(--green-500); border-color:var(--green-500); color:#fff; }
.card-item.is-current { border-color:var(--brand-600); color:var(--brand-600); background:#fff; box-shadow:0 0 0 3px rgba(79,70,229,.15); transform:scale(1.08); }
.card-item.is-current.is-answered { background:var(--green-500); border-color:var(--brand-600); color:#fff; }
.card-legend { margin-left:auto; display:inline-flex; align-items:center; font-size:12px; color:var(--ink-400); }
.card-legend .lg { width:12px; height:12px; border-radius:4px; display:inline-block; margin:0 4px 0 10px; vertical-align:-1px; }
.lg-done { background:var(--green-500); }
.lg-todo { background:#fff; border:2px solid var(--ink-200); }
.lg-cur { background:#fff; border:2px solid var(--brand-600); }
.badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.badge.blue { background:var(--blue-50); color:var(--blue-700); }
.badge.violet { background:var(--violet-50); color:var(--brand-600); }
.badge.green { background:var(--green-50); color:var(--green-700); }
</style>
