<template>
  <section class="rd-card fo-card" ref="cardRef">
    <div class="rd-card-header">
      <div class="rd-card-title"><span class="rd-card-icon">✎</span>{{ title }}</div>
      <div class="fo-tools">
        <span class="fo-tag is-gray">共 {{ rows.length }} 条</span>
        <span class="fo-tag is-amber" v-if="pendingCount">{{ pendingCount }} 人待办理</span>
        <span class="fo-tag is-gray" v-if="undoneCount">{{ undoneCount }} 人未办理</span>
        <span class="fo-tag is-green" v-if="ended">流程已结束</span>
        <el-checkbox v-model="onlyOpinion" size="small" class="fo-only">仅看有意见</el-checkbox>
      </div>
    </div>
    <div class="rd-card-body" style="display:block">
      <div class="fo-table" v-loading="loading">
        <el-table ref="tableRef" :data="visibleRows" size="small" border :row-class-name="rowClass" empty-text="暂无审批意见">
          <el-table-column label="序号" width="56" align="center">
            <template #default="{ $index }">{{ String($index + 1).padStart(2, '0') }}</template>
          </el-table-column>
          <el-table-column label="办理节点" min-width="118" show-overflow-tooltip>
            <template #default="{ row }"><span class="fo-node">{{ row.taskName || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="办理人" width="118" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fo-person">
                <span class="fo-avatar" :class="'tone-' + row.tone">{{ row.avatar }}</span>
                <span class="fo-person-name" :class="{ 'is-empty': !row.person }">{{ row.person || '待认领' }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="办理动作" width="106" align="center">
            <template #default="{ row }">
              <span class="fo-pill" :class="'is-' + row.tone"><span class="fo-dot"></span>{{ row.actionText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="办理时间" width="146" align="center">
            <template #default="{ row }">
              <div class="fo-time">{{ row.timeText || '—' }}</div>
              <div class="fo-cost" v-if="row.costText">耗时 {{ row.costText }}</div>
            </template>
          </el-table-column>
          <el-table-column label="接收人" min-width="132" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.receiver" class="fo-receiver">{{ row.receiver }}</span>
              <span v-else class="fo-none">—</span>
            </template>
          </el-table-column>
          <el-table-column label="签字意见" min-width="200">
            <template #default="{ row }">
              <div v-if="row.opinion" class="fo-op">
                <div class="fo-op-text" :class="{ 'is-clamped': row.long && !expanded[row.key] }">{{ row.opinion }}</div>
                <button type="button" class="fo-more" v-if="row.long" @click="toggle(row.key)">
                  {{ expanded[row.key] ? '收起' : '展开全部' }}
                </button>
              </div>
              <div v-else class="fo-none">{{ row.noneNote }}</div>
              <!-- 该环节上传的附件：可直接点开本环节材料 -->
              <div class="fo-attach" v-if="row.files.length">
                <a v-for="f in row.files" :key="f.attachId" class="fo-file" :href="fileHref(f.fileUrl)" target="_blank">
                  <el-icon><Paperclip /></el-icon>
                  <span class="fo-file-name">{{ f.fileName }}</span>
                  <span class="fo-file-size" v-if="fileSizeText(f.fileSize)">{{ fileSizeText(f.fileSize) }}</span>
                </a>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 抄送（签批意见区底部固定展示抄送人） -->
      <div class="fo-cc" v-if="ccReceivers.length">
        <span class="fo-cc-label">抄送</span>
        <span class="fo-cc-item" v-for="c in ccReceivers" :key="c.key" :title="c.title">{{ c.name }}</span>
        <span class="fo-cc-count">共 {{ ccReceivers.length }} 人</span>
      </div>
    </div>
  </section>
</template>

<script setup name="FlowOpinion">
import { htmlToText } from '@/utils'
import { getToken } from '@/utils/auth'
import { Paperclip } from '@element-plus/icons-vue'

const props = defineProps({
  /** 流转记录（/flowable/process/history/{instanceId} 返回的列表，按时间正序） */
  tasks: { type: Array, default: () => [] },
  /** 流程附件（/flowable/attach/list 返回），按 taskId 挂到对应节点的意见上 */
  attachments: { type: Array, default: () => [] },
  /** 抄送记录（/flowable/cc/byInstance/{instanceId} 返回） */
  ccList: { type: Array, default: () => [] },
  /** 加载中（仅遮罩，不隐藏已有内容） */
  loading: { type: Boolean, default: false },
  /** 卡片标题 */
  title: { type: String, default: '审批意见' },
  /** 是否展示「发起」节点（意见表首行即发起人，默认展示） */
  showStart: { type: Boolean, default: true }
})

/** 各办理动作 → 文案 + 色调（与「流程状态」页签同一口径） */
const ACTION_MAP = {
  start: { text: '发起提交', tone: 'violet' },
  approved: { text: '同意', tone: 'green' },
  rejected: { text: '驳回', tone: 'red' },
  delegate: { text: '转办', tone: 'blue' },
  canceled: { text: '撤销', tone: 'gray' },
  end: { text: '流程结束', tone: 'green' },
  undone: { text: '未办理', tone: 'gray' }
}

/** 长意见折叠阈值（字符数） */
const LONG_LIMIT = 110

const expanded = ref({})
/** 仅看有意见（「只看签名意见」开关） */
const onlyOpinion = ref(false)
/** 表格实例：弹窗打开动画中挂载时容器宽度尚未确定，需重算列宽，否则表格只占最小宽度 */
const tableRef = ref(null)
/** 卡片容器：宽度变化（弹窗打开、窗口缩放、侧边栏收起）时重算列宽 */
const cardRef = ref(null)
let cardObserver = null

function relayout() {
  nextTick(() => {
    try {
      if (tableRef.value && typeof tableRef.value.doLayout === 'function') tableRef.value.doLayout()
    } catch (e) { /* 表格未挂载时忽略 */ }
  })
}

/** 流程是否已结束（结束/撤销记录由后端以 __end__ 伪任务给出） */
const ended = computed(() => (props.tasks || []).some(t => t && (t.outcome === 'end' || t.outcome === 'canceled')))

/** 附件按 taskId 归集，便于逐条意见展示本环节材料 */
const filesByTask = computed(() => {
  const map = {}
  for (const a of props.attachments || []) {
    const key = a.taskId || '__none__'
    if (!map[key]) map[key] = []
    map[key].push(a)
  }
  return map
})

/**
 * 意见行：一条 = 一个节点上的一位办理人（候选池节点按候选人逐人展开）
 * - 完成后以完成时间作为办理时间；未完成显示到达时间并标记待办理
 * - state=undone 为「已收到但未处理」（他人已办/流程已撤销），与新后端的候选人行对齐
 * - 意见为富文本，统一转纯文本渲染（避免 v-html 注入风险），保留换行
 */
const rows = computed(() => {
  const list = (props.tasks || [])
    .filter(t => t && t.taskId !== '__end__')
    .filter(t => props.showStart || t.outcome !== 'start')
  const isEnded = ended.value
  return list.map((t, index) => {
    const meta = ACTION_MAP[t.outcome] || null
    const opinion = opinionOf(t.comment)
    const state = t.state || (t.endTime ? 'done' : 'pending')
    const pending = state === 'pending' && !isEnded
    // 未办理：候选池里没抢到的其他候选人；或撤销/终止时引擎补了 endTime 但无人真正办理
    const unhandled = state === 'undone' || (isEnded && !t.outcome && !opinion)
    // 下一节点：跳过同任务的其他候选人行；接收人列出该节点全部办理人（候选池多人时一并展示）
    const next = list.slice(index + 1).find(x => x.taskId !== t.taskId)
    const nextNames = next
      ? [...new Set(list.filter(x => x.taskId === next.taskId)
        .map(x => x.assigneeName || (x.assigneeId ? String(x.assigneeId) : ''))
        .filter(Boolean))]
      : []
    let receiver = ''
    if (!unhandled) {
      if (next) receiver = next.taskName + (nextNames.length ? '：' + nextNames.join('、') : '（待认领）')
      else if (isEnded) receiver = '流程结束'
    }
    return {
      key: (t.taskId || t.taskName || '') + '-' + (t.assigneeId || t.assigneeName || index),
      taskName: t.taskName,
      person: t.assigneeName || (t.assigneeId ? String(t.assigneeId) : ''),
      avatar: avatarChar(t.assigneeName || t.assigneeId),
      pending,
      unhandled,
      // 无意见时的空态文案：待办 / 候选未中签（流程还在走）/ 流程结束未办理 / 无
      noneNote: pending
        ? '等待办理，暂无意见'
        : (t.state === 'undone' && !isEnded ? '本人未办理（该环节已由他人处理）' : (unhandled ? '流程结束前未办理' : '—')),
      tone: pending ? 'amber' : (unhandled ? 'gray' : (meta ? meta.tone : 'gray')),
      actionText: pending ? '待办理' : (unhandled ? '未办理' : (meta ? meta.text : '已办理')),
      opinion,
      long: opinion.length > LONG_LIMIT || opinion.split('\n').length > 3,
      timeText: pending ? shortTime(t.createTime) : (unhandled ? '' : shortTime(t.endTime || t.createTime)),
      costText: pending || unhandled || t.outcome === 'start' ? '' : durationText(t.createTime, t.endTime),
      receiver,
      // 未办理行不挂环节附件：附件是该环节实际办理时上传的，不属于没办理的人
      files: unhandled ? [] : (filesByTask.value[t.taskId] || [])
    }
  })
})

const pendingCount = computed(() => rows.value.filter(r => r.pending).length)
const undoneCount = computed(() => rows.value.filter(r => r.unhandled).length)

/** 表格展示行：开启「仅看有意见」时过滤掉无意见的记录 */
const visibleRows = computed(() => (onlyOpinion.value ? rows.value.filter(r => r.opinion) : rows.value))

/** 抄送人：同一人多次抄送只显示一次，tooltip 给出抄送环节/时间/抄送人 */
const ccReceivers = computed(() => {
  const map = new Map()
  for (const c of props.ccList || []) {
    const key = c.receiverId != null ? String(c.receiverId) : c.receiverName
    map.set(key, {
      key,
      name: c.receiverName || (c.receiverId != null ? String(c.receiverId) : '-'),
      title: [
        c.taskName ? '抄送环节：' + c.taskName : '',
        c.senderName ? '抄送人：' + c.senderName : '',
        c.createTime ? '抄送时间：' + shortTime(c.createTime) : ''
      ].filter(Boolean).join(' ｜ ')
    })
  }
  return [...map.values()]
})

/** 待办行 / 未办理行高亮 */
function rowClass({ row }) {
  if (row.pending) return 'fo-row-pending'
  if (row.unhandled) return 'fo-row-unhandled'
  return ''
}

function toggle(key) {
  expanded.value = { ...expanded.value, [key]: !expanded.value[key] }
}

/** 富文本意见 → 纯文本（保留换行、去掉空行） */
function opinionOf(comment) {
  try {
    return htmlToText(comment || '')
      .split('\n')
      .map(s => s.trim())
      .filter(Boolean)
      .join('\n')
  } catch (e) {
    return ''
  }
}

/** 头像占位字符：中文取末字，英文取首字母 */
function avatarChar(name) {
  const s = String(name || '').trim()
  if (!s) return '·'
  return /[\u4e00-\u9fa5]/.test(s) ? s.slice(-1) : s.slice(0, 1).toUpperCase()
}

/** 时间显示：去掉毫秒与秒，只留到分钟 */
function shortTime(v) {
  if (!v) return ''
  const s = String(v).replace('T', ' ')
  return s.length >= 16 ? s.slice(0, 16) : s
}

/** 办理耗时：分钟 → 「1天2小时 / 3小时20分钟 / 45分钟」 */
function durationText(begin, end) {
  if (!begin || !end) return ''
  const start = new Date(String(begin).replace(/-/g, '/'))
  const stop = new Date(String(end).replace(/-/g, '/'))
  const ms = stop - start
  if (isNaN(ms) || ms < 0) return ''
  const mins = Math.floor(ms / 60000)
  if (mins < 1) return '不足1分钟'
  if (mins < 60) return mins + '分钟'
  const days = Math.floor(mins / 1440)
  const hours = Math.floor((mins % 1440) / 60)
  const rest = mins % 60
  if (days) return days + '天' + (hours ? hours + '小时' : '')
  return hours + '小时' + (rest ? rest + '分钟' : '')
}

/* 列宽自适应：挂载后、数据到位后、窗口尺寸变化时都重算一次
 * （弹窗/页签过渡期间挂载，容器宽度还没算出来，el-table 会按最小宽度定死） */
onMounted(() => {
  relayout()
  window.addEventListener('resize', relayout)
  if (typeof ResizeObserver !== 'undefined' && cardRef.value) {
    // 只在宽度真正变化时重算（行数增减引起的高度变化不必重算，也避免回调自循环）
    let lastWidth = Math.round(cardRef.value.getBoundingClientRect().width)
    cardObserver = new ResizeObserver(entries => {
      const w = Math.round(entries[0].contentRect.width)
      if (w !== lastWidth) {
        lastWidth = w
        relayout()
      }
    })
    cardObserver.observe(cardRef.value)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', relayout)
  if (cardObserver) {
    cardObserver.disconnect()
    cardObserver = null
  }
})

watch(() => props.loading, v => { if (!v) relayout() })
watch(() => visibleRows.value.length, () => relayout())

/** 附件访问地址（<a> 无法携带 Authorization，通过 token 参数访问） */
function fileHref(url) {
  if (!url) return ''
  return import.meta.env.VITE_APP_BASE_API + '/common/file/access?resource=' + encodeURIComponent(url) + '&token=' + getToken()
}

function fileSizeText(size) {
  const n = Number(size || 0)
  if (!n) return ''
  if (n < 1024) return n + ' B'
  if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB'
  return (n / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style lang="scss" scoped>
$c-blue: #2f8ee0;
$c-green: #1fae7b;
$c-amber: #f0a020;
$c-violet: #7c6cf0;
$c-red: #e4534f;
$c-text: #1f2937;
$c-text-2: #47506b;
$c-muted: #8a95a5;
$c-line: #e9eef5;

/* 卡片间距沿用详情页规范（.rd-card 自带 margin-bottom），此处不再额外加间距 */
.fo-card {
  margin-top: 0;
}

/* ===== 头部右侧工具区 ===== */
.fo-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-wrap: wrap;
}

.fo-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  white-space: nowrap;

  &.is-gray { color: $c-muted; background: #f5f7fa; border-color: $c-line; }
  &.is-amber { color: #b4720a; background: #fef6e6; border-color: #fbe3b6; }
  &.is-green { color: #12795a; background: #eaf8f2; border-color: #c7ecdd; }
}

.fo-only {
  margin-right: 0;

  :deep(.el-checkbox__label) { font-size: 12px; color: $c-text-2; }
}

/* ===== 签批意见表 ===== */
.fo-table {
  min-height: 56px;

  :deep(.el-table) {
    --el-table-border-color: #eef2f8;
    --el-table-header-bg-color: #f2f7fd;
    --el-table-header-text-color: #{$c-text-2};
    --el-table-row-hover-bg-color: #f7fbff;
    --el-table-text-color: #{$c-text-2};
    font-size: 13px;
  }

  :deep(.el-table th.el-table__cell) {
    font-weight: 600;
    padding: 8px 0;
    background: #f2f7fd;
  }

  :deep(.el-table td.el-table__cell) {
    padding: 8px 0;
    vertical-align: top;
  }

  :deep(.el-table__inner-wrapper::before) { display: none; }

  /* 待办行琥珀底、流程结束未办理行灰底 */
  :deep(.fo-row-pending) {
    --el-table-tr-bg-color: #fffaf1;
  }
  :deep(.fo-row-unhandled) {
    --el-table-tr-bg-color: #fafbfc;
  }
}

.fo-node {
  color: #4b73a6;
  font-weight: 500;
}

.fo-person {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.fo-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  flex: none;
  background: linear-gradient(135deg, #9db4cd, #7d94ad);

  &.tone-green { background: linear-gradient(135deg, #4bd0a0, $c-green); }
  &.tone-amber { background: linear-gradient(135deg, #f8c25c, $c-amber); }
  &.tone-blue { background: linear-gradient(135deg, #6cb6ea, $c-blue); }
  &.tone-violet { background: linear-gradient(135deg, #a99bf9, $c-violet); }
  &.tone-red { background: linear-gradient(135deg, #f08b88, $c-red); }
  &.tone-gray { background: linear-gradient(135deg, #c3ccd8, #a3aebd); }
}

.fo-person-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: $c-text;

  &.is-empty { color: #a6b0bf; }
}

.fo-time {
  color: $c-text-2;
  font-variant-numeric: tabular-nums;
}

.fo-receiver {
  color: $c-text-2;
  line-height: 1.6;
}

.fo-cost {
  margin-top: 2px;
  font-size: 12px;
  color: #98a2b3;
}

/* 意见正文 */
.fo-op-text {
  line-height: 1.7;
  color: $c-text-2;
  white-space: pre-wrap;
  word-break: break-word;

  &.is-clamped {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.fo-none {
  color: #a6b0bf;
}

.fo-more {
  margin-top: 2px;
  padding: 0;
  border: none;
  background: transparent;
  color: $c-blue;
  font-size: 12px;
  cursor: pointer;

  &:hover { text-decoration: underline; }
}

/* 环节附件 */
.fo-attach {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.fo-file {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 100%;
  font-size: 12px;
  color: #1a73c7;
  text-decoration: none;

  &:hover { text-decoration: underline; }

  .el-icon { flex: none; font-size: 13px; }
}

.fo-file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fo-file-size {
  color: #98a2b3;
  flex: none;
}

/* 状态胶囊 */
.fo-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 1px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  border: 1px solid transparent;
  white-space: nowrap;

  .fo-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    flex: none;
  }

  &.is-green { color: #12795a; background: #eaf8f2; border-color: #c7ecdd; .fo-dot { background: $c-green; } }
  &.is-amber { color: #b4720a; background: #fef6e6; border-color: #fbe3b6; .fo-dot { background: $c-amber; } }
  &.is-red { color: #b73b37; background: #fdeeee; border-color: #f8d3d2; .fo-dot { background: $c-red; } }
  &.is-blue { color: #1a73c7; background: #eef6fe; border-color: #d5e8fa; .fo-dot { background: $c-blue; } }
  &.is-violet { color: #5849c6; background: #f1effe; border-color: #ddd8fb; .fo-dot { background: $c-violet; } }
  &.is-gray { color: $c-muted; background: #f5f7fa; border-color: $c-line; .fo-dot { background: #b6c0cd; } }
}

/* ===== 抄送 ===== */
.fo-cc {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
  padding: 8px 10px;
  background: #f7f9fc;
  border: 1px solid #eef2f8;
  border-radius: 8px;
}

.fo-cc-label {
  font-size: 12px;
  font-weight: 600;
  color: $c-text-2;
  margin-right: 2px;
}

.fo-cc-item {
  font-size: 12px;
  color: #1a73c7;
  background: #fff;
  border: 1px solid #dbe8f5;
  border-radius: 999px;
  padding: 1px 10px;
  cursor: default;
}

.fo-cc-count {
  margin-left: auto;
  font-size: 12px;
  color: #98a2b3;
}
</style>
