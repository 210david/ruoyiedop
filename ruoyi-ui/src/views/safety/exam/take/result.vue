<template>
  <div class="result-wrap">
    <!-- 成绩头部 -->
    <div class="surface result-head" :class="isPass ? 'pass' : 'fail'">
      <div class="score-block">
        <div class="score-value">{{ resultData.totalScore }}<span class="score-unit">分</span></div>
        <div class="score-label">{{ isPass ? '恭喜，考试通过' : '很遗憾，未达及格线' }}</div>
      </div>
      <div class="meta-block">
        <div class="meta-item"><span class="meta-label">考试</span><span>{{ resultData.examName }}</span></div>
        <div class="meta-item"><span class="meta-label">试卷</span><span>{{ resultData.paperName }}</span></div>
        <div class="meta-item"><span class="meta-label">及格分</span><span>{{ resultData.passScore }} 分</span></div>
        <div class="meta-item"><span class="meta-label">交卷时间</span><span>{{ resultData.submitTime || '-' }}</span></div>
        <div class="meta-item"><span class="meta-label">用时</span><span>{{ usedTimeText }}</span></div>
      </div>
    </div>

    <!-- 题目明细 -->
    <div class="surface result-body" v-if="resultData.showAnalysis">
      <div class="body-head">题目明细</div>
      <div class="rq-item" v-for="(q, idx) in resultData.questions || []" :key="q.paperQuestionId">
        <div class="rq-head">
          <span class="rq-no" :class="q.isCorrect ? 'ok' : 'bad'">{{ idx + 1 }}</span>
          <span class="badge" :class="typeBadgeClass(q.questionType)">{{ typeLabel(q.questionType) }}</span>
          <span class="rq-content">{{ q.content }}</span>
          <span class="rq-score">{{ q.score }} 分</span>
        </div>
        <div class="rq-answer">
          <span class="ans-chip" :class="q.isCorrect ? 'ok' : 'bad'">你的答案：{{ q.userAnswer || '未作答' }}</span>
          <span class="ans-chip ok" v-if="!q.isCorrect">正确答案：{{ q.answer }}</span>
        </div>
        <div class="rq-analysis" v-if="q.analysis">解析：{{ q.analysis }}</div>
      </div>
    </div>
    <div class="surface result-body" v-else>
      <el-empty description="本场考试未开启答案解析展示" :image-size="80" />
    </div>

    <div class="result-footer">
      <el-button type="primary" @click="$emit('back')">返回我的考试</el-button>
    </div>
  </div>
</template>

<script setup name="SafetyExamResult">
const props = defineProps({ resultData: { type: Object, required: true } })
defineEmits(['back'])

const isPass = computed(() => props.resultData.isPass === '1' || props.resultData.isPass === true)
const usedTimeText = computed(() => {
  const s = props.resultData.usedSeconds
  if (s == null) return '-'
  const m = Math.floor(s / 60), sec = s % 60
  return m > 0 ? `${m}分${sec}秒` : `${sec}秒`
})
function typeLabel(type) { return { '1': '单选题', '2': '多选题', '3': '判断题' }[type] || '-' }
function typeBadgeClass(type) { return { '1': 'blue', '2': 'violet', '3': 'green' }[type] || 'gray' }
</script>

<style scoped>
.result-wrap { display:flex; flex-direction:column; gap:8px; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --blue-50:#eff6ff; --blue-700:#1d4ed8; --violet-50:#f5f3ff; --brand-600:#4f46e5; }
.surface { background:#fff; border:1px solid var(--ink-200); border-radius:12px; box-shadow:0 1px 2px rgba(15,23,42,.05); }
.result-head { display:flex; gap:32px; padding:24px 28px; align-items:center; }
.result-head.pass { background:linear-gradient(135deg, #ecfdf5 0%, #ffffff 60%); }
.result-head.fail { background:linear-gradient(135deg, #fef2f2 0%, #ffffff 60%); }
.score-value { font-size:52px; font-weight:800; font-variant-numeric:tabular-nums; line-height:1; }
.pass .score-value { color:var(--green-700); }
.fail .score-value { color:var(--red-700); }
.score-unit { font-size:16px; font-weight:600; margin-left:4px; }
.score-label { margin-top:8px; font-size:14px; color:var(--ink-500); }
.meta-block { display:flex; flex-direction:column; gap:6px; }
.meta-item { font-size:14px; color:var(--ink-700); display:flex; gap:12px; }
.meta-label { color:var(--ink-400); width:64px; flex-shrink:0; }
.result-body { padding:18px 24px; }
.body-head { font-size:15px; font-weight:700; color:var(--ink-900); margin-bottom:14px; }
.rq-item { padding:14px 0; border-bottom:1px dashed var(--ink-200); }
.rq-item:last-child { border-bottom:none; }
.rq-head { display:flex; align-items:flex-start; gap:10px; }
.rq-no { width:24px; height:24px; border-radius:50%; display:inline-flex; align-items:center; justify-content:center; font-size:12px; font-weight:700; color:#fff; flex-shrink:0; margin-top:2px; }
.rq-no.ok { background:var(--green-500); }
.rq-no.bad { background:var(--red-500); }
.rq-content { flex:1; font-size:14px; font-weight:600; color:var(--ink-900); line-height:1.6; }
.rq-score { font-size:13px; font-weight:700; color:var(--brand-600); flex-shrink:0; }
.rq-answer { display:flex; gap:8px; margin:10px 0 0 34px; flex-wrap:wrap; }
.ans-chip { font-size:13px; padding:4px 12px; border-radius:999px; }
.ans-chip.ok { background:var(--green-50); color:var(--green-700); }
.ans-chip.bad { background:var(--red-50); color:var(--red-700); }
.rq-analysis { margin:8px 0 0 34px; font-size:13px; color:var(--ink-500); line-height:1.7; }
.result-footer { display:flex; justify-content:center; padding:4px 0 12px; }
.badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; flex-shrink:0; }
.badge.blue { background:var(--blue-50); color:var(--blue-700); }
.badge.violet { background:var(--violet-50); color:var(--brand-600); }
.badge.green { background:var(--green-50); color:var(--green-700); }
</style>
