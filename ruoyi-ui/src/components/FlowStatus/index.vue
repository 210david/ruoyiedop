<template>
  <div class="fs-page" v-loading="loading">
    <!-- ===== 统计概览 ===== -->
    <div class="fs-stats">
      <div class="fs-stat" v-for="c in statCards" :key="c.key">
        <span class="fs-stat-icon" :class="'tone-' + c.tone">
          <el-icon><component :is="c.icon" /></el-icon>
        </span>
        <span class="fs-stat-body">
          <span class="fs-stat-num">{{ c.value }}</span>
          <span class="fs-stat-label">{{ c.label }}</span>
        </span>
      </div>
    </div>

    <!-- ===== 当前节点 ===== -->
    <section class="fs-section">
      <header class="fs-section-head">
        <span class="fs-section-icon"><el-icon><UserFilled /></el-icon></span>
        <h3 class="fs-section-title">当前节点</h3>
        <span class="fs-section-tag is-amber" v-if="currentNodes.length">{{ currentNodes.length }} 个节点待办理</span>
        <span class="fs-section-tag is-green" v-else-if="recordRows.length">流程已结束</span>
      </header>

      <div class="fs-current">
        <template v-if="currentNodes.length">
          <span class="fs-node-chip" v-for="n in currentNodes" :key="n.name">
            <span class="fs-node-name">{{ n.name }}</span>
            <span class="fs-node-owner" v-if="n.assignees.length">{{ n.assignees.join('、') }}</span>
          </span>
        </template>
        <div class="fs-done" v-else>
          <el-icon class="fs-done-icon"><CircleCheckFilled /></el-icon>
          <span>{{ recordRows.length ? '流程已流转结束，当前没有待办理节点' : '暂无流程流转数据' }}</span>
        </div>
      </div>

      <!-- 候选人提示：候选池节点按人展开后，说明这些人已同时收到流程、谁办都行 -->
      <div class="fs-hint" v-if="candidatePendingCount">
        <el-icon><InfoFilled /></el-icon>
        <span>当前节点支持候选办理：下列 {{ candidatePendingCount }} 人均已收到流程，任意一人办理即可，其余人记为「未办理」。</span>
      </div>

      <div class="fs-table" v-if="pendingRows.length">
        <el-table :data="pendingRows" border size="small">
          <el-table-column label="办理人" width="150">
            <template #default="{ row }">
              <span class="fs-person">
                <span class="fs-avatar" v-if="personName(row)">{{ avatarChar(personName(row)) }}</span>
                <span class="fs-person-name" :class="{ 'is-empty': !personName(row) }">{{ personName(row) || '待认领' }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="所属节点" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fs-activity is-current"><span class="fs-node-dot"></span>{{ row.taskName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="办理状态" width="112" align="center">
            <template #default="{ row }">
              <span class="fs-pill" :class="row.state === 'undone' ? 'is-gray' : 'is-amber'">
                <span class="fs-dot"></span>{{ row.state === 'undone' ? '未办理' : '待办理' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="到达时间" width="172" align="center">
            <template #default="{ row }"><span class="fs-time">{{ row.createTime || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="完成时间" width="172" align="center">
            <template #default="{ row }"><span class="fs-time">{{ row.endTime || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作明细" min-width="170" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fs-muted">{{ opinionOf(row.comment) || '暂未填写办理意见' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <!-- ===== 流程记录 ===== -->
    <section class="fs-section">
      <header class="fs-section-head">
        <span class="fs-section-icon"><el-icon><Connection /></el-icon></span>
        <h3 class="fs-section-title">流程记录</h3>
        <span class="fs-section-tag is-gray">共 {{ recordRows.length }} 条</span>
      </header>

      <div class="fs-table">
        <el-table :data="recordRows" border size="small" empty-text="暂无流转记录">
          <el-table-column type="index" label="序号" width="66" align="center" />
          <el-table-column label="活动名称" min-width="190" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fs-activity" :class="{ 'is-current': !row.endTime }">
                <span class="fs-node-dot"></span>{{ row.taskName || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提交人" width="130" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fs-person">
                <span class="fs-avatar" v-if="personName(row)">{{ avatarChar(personName(row)) }}</span>
                <span class="fs-person-name" :class="{ 'is-empty': !personName(row) }">{{ personName(row) || '待认领' }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作明细" width="132" align="center">
            <template #default="{ row }">
              <span class="fs-pill" :class="'is-' + row.tone"><span class="fs-dot"></span>{{ row.actionText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="批示意见" min-width="210">
            <template #default="{ row }">
              <el-tooltip v-if="row.opinion" :content="row.opinion" placement="top">
                <span class="fs-opinion">{{ row.opinion }}</span>
              </el-tooltip>
              <span v-else class="fs-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="参与时间" width="172" align="center">
            <template #default="{ row }"><span class="fs-time">{{ row.createTime || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="耗时" width="128" align="center">
            <template #default="{ row }">
              <span class="fs-time" :class="{ 'is-live': !row.endTime }">{{ row.costText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="接收者" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.receiver" class="fs-receiver">{{ row.receiver }}</span>
              <span v-else class="fs-muted">—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup name="FlowStatus">
import { UserFilled, CircleCheckFilled, Timer, Stamp, Share, Connection, InfoFilled, CircleClose } from '@element-plus/icons-vue'
import { htmlToText } from '@/utils'

const props = defineProps({
  /** 流转记录（/flowable/process/history/{instanceId} 返回的列表，按时间正序） */
  tasks: { type: Array, default: () => [] },
  /** 是否加载中（表格遮罩） */
  loading: { type: Boolean, default: false }
})

/** 操作结果 → 文案 / 色调 */
const OUTCOME_MAP = {
  start: { text: '发起提交', tone: 'violet' },
  approved: { text: '审批通过', tone: 'green' },
  rejected: { text: '审批驳回', tone: 'red' },
  delegate: { text: '转办', tone: 'blue' },
  canceled: { text: '流程撤销', tone: 'gray' },
  end: { text: '流程结束', tone: 'green' },
  undone: { text: '未办理', tone: 'gray' }
}

const rows = computed(() => props.tasks || [])

/** 参与状态：优先用后端返回的 state，老数据按 endTime 兜底 */
function stateOf(t) {
  if (t.state) return t.state
  return t.endTime ? 'done' : 'pending'
}

/** 统计概览：总人次 = 已办理 + 待办理 + 未办理（排除「流程结束」伪记录） */
const stats = computed(() => {
  const list = rows.value.filter(t => t.taskId !== '__end__')
  const done = list.filter(t => stateOf(t) === 'done').length
  const pending = list.filter(t => stateOf(t) === 'pending').length
  const undone = list.filter(t => stateOf(t) === 'undone').length
  return {
    total: list.length,
    finished: done,
    pending,
    undone,
    approved: list.filter(t => t.outcome === 'approved').length,
    nodes: new Set(list.map(t => t.taskName).filter(Boolean)).size
  }
})

const statCards = computed(() => [
  { key: 'total', label: '总人次', value: stats.value.total, tone: 'blue', icon: UserFilled },
  { key: 'finished', label: '已办理', value: stats.value.finished, tone: 'green', icon: CircleCheckFilled },
  { key: 'pending', label: '待办理', value: stats.value.pending, tone: 'amber', icon: Timer },
  { key: 'undone', label: '未办理', value: stats.value.undone, tone: 'gray', icon: CircleClose },
  { key: 'approved', label: '已通过', value: stats.value.approved, tone: 'cyan', icon: Stamp },
  { key: 'nodes', label: '节点数', value: stats.value.nodes, tone: 'violet', icon: Share }
])

/** 当前停留节点：未完成的任务，按节点归并办理人（候选池已按人展开，这里能看到全部候选人） */
const currentNodes = computed(() => {
  const map = new Map()
  rows.value.filter(t => stateOf(t) === 'pending').forEach(t => {
    const name = t.taskName || '未命名节点'
    if (!map.has(name)) map.set(name, { name, assignees: [] })
    const person = t.assigneeName || (t.assigneeId ? String(t.assigneeId) : '')
    if (person && !map.get(name).assignees.includes(person)) map.get(name).assignees.push(person)
  })
  return [...map.values()]
})

/** 当前节点办理人（含候选池展开出来的每一位候选人） */
const pendingRows = computed(() => rows.value.filter(t => stateOf(t) === 'pending'))

/** 当前节点里以候选人身份出现的办理人数（>0 时给出一句候选办理说明） */
const candidatePendingCount = computed(() => pendingRows.value.filter(r => r.candidate).length)

/**
 * 流程记录行：补充「操作明细 / 批示意见 / 耗时 / 接收者」
 * 接收者 = 下一条流转记录的节点 + 办理人（无下一条说明流程在此终止）
 */
const recordRows = computed(() => {
  const list = rows.value
  return list.map((t, i) => {
    const state = stateOf(t)
    const outcome = t.outcome || ''
    const meta = OUTCOME_MAP[outcome]
    // 下一个节点：跳过同一任务的其他候选人行，避免「接收者」显示成同节点的另一个人
    const next = list.slice(i + 1).find(x => x.taskId !== t.taskId)
    // 接收者 = 下一节点的全部办理人（候选池多人时一并列出，看清楚流程发给了谁）
    const nextNames = next
      ? [...new Set(list.filter(x => x.taskId === next.taskId).map(x => personName(x)).filter(Boolean))]
      : []
    let receiver = ''
    if (state !== 'undone' && next) {
      receiver = next.taskName + (nextNames.length ? '：' + nextNames.join('、') : '')
    }
    return {
      ...t,
      outcome,
      tone: state === 'undone' ? 'gray' : (state === 'pending' ? 'amber' : (meta ? meta.tone : 'gray')),
      actionText: state === 'undone' ? '未办理' : (state === 'pending' ? '待办理' : (meta ? meta.text : '已办理')),
      opinion: opinionOf(t.comment),
      costText: costText(t),
      receiver
    }
  })
})

/** 富文本意见转纯文本，压缩空白 */
function opinionOf(comment) {
  try {
    return htmlToText(comment || '').replace(/\s+/g, ' ').trim()
  } catch (e) {
    return ''
  }
}

/** 办理人姓名：未分配办理人时返回空串（模板里显示「待认领」） */
function personName(row) {
  return (row && (row.assigneeName || row.assigneeId)) || ''
}

/** 头像占位字符：中文取末字，英文取首字母 */
function avatarChar(name) {
  const s = (name || '').trim()
  if (!s) return '·'
  return /[\u4e00-\u9fa5]/.test(s) ? s.slice(-1) : s.slice(0, 1).toUpperCase()
}

/** 耗时：分钟 → 「1天2小时 / 3小时20分钟 / 45分钟」 */
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

/** 行耗时：待办理显示「进行中」；未办理（流程已被他人处理/撤销）与发起节点不计耗时 */
function costText(t) {
  if (t.outcome === 'start') return '—'
  if (stateOf(t) === 'undone') return '—'
  if (!t.endTime) return '进行中'
  return durationText(t.createTime, t.endTime) || '—'
}
</script>

<style lang="scss" scoped>
$c-blue: #2f8ee0;
$c-green: #1fae7b;
$c-amber: #f0a020;
$c-cyan: #12b5c9;
$c-violet: #7c6cf0;
$c-red: #e4534f;
$c-text: #1f2937;
$c-text-2: #47506b;
$c-muted: #8a95a5;
$c-line: #e9eef5;

.fs-page {
  max-width: 1440px;
  margin: 0 auto;
  padding: 4px 0 8px;
}

/* ===== 统计卡 ===== */
.fs-stats {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.fs-stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid $c-line;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #d7e6f6;
    box-shadow: 0 10px 22px -12px rgba(15, 23, 42, .28);
  }
}

.fs-stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  color: #fff;
  flex: none;

  .el-icon { font-size: 24px; }

  &.tone-blue { background: linear-gradient(135deg, #56b4f2, $c-blue); box-shadow: 0 8px 16px -10px rgba(47, 142, 224, .95); }
  &.tone-green { background: linear-gradient(135deg, #3fce9b, $c-green); box-shadow: 0 8px 16px -10px rgba(31, 174, 123, .95); }
  &.tone-amber { background: linear-gradient(135deg, #f8c25c, $c-amber); box-shadow: 0 8px 16px -10px rgba(240, 160, 32, .95); }
  &.tone-cyan { background: linear-gradient(135deg, #4fd0e0, $c-cyan); box-shadow: 0 8px 16px -10px rgba(18, 181, 201, .95); }
  &.tone-violet { background: linear-gradient(135deg, #a396f7, $c-violet); box-shadow: 0 8px 16px -10px rgba(124, 108, 240, .95); }
  &.tone-gray { background: linear-gradient(135deg, #c3ccd8, #8d9aab); box-shadow: 0 8px 16px -10px rgba(141, 154, 171, .95); }
}

.fs-stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.fs-stat-num {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
  color: $c-text;
  font-variant-numeric: tabular-nums;
}

.fs-stat-label {
  font-size: 13px;
  color: $c-muted;
  white-space: nowrap;
}

/* ===== 区块 ===== */
.fs-section {
  background: #fff;
  border: 1px solid $c-line;
  border-radius: 12px;
  padding: 16px 20px 18px;
  margin-bottom: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, .04);

  &:last-child { margin-bottom: 0; }
}

.fs-section-head {
  display: flex;
  align-items: center;
  gap: 9px;
  padding-bottom: 12px;
  margin-bottom: 14px;
  border-bottom: 1px solid #f2f5fa;
}

.fs-section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: #eaf3fd;
  color: $c-blue;
  font-size: 15px;
  flex: none;
}

.fs-section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: $c-text;
  letter-spacing: .2px;
}

.fs-section-tag {
  margin-left: auto;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid transparent;

  &.is-amber { color: #b4720a; background: #fef6e6; border-color: #fbe3b6; }
  &.is-green { color: #12795a; background: #eaf8f2; border-color: #c7ecdd; }
  &.is-gray { color: $c-muted; background: #f5f7fa; border-color: $c-line; }
}

/* ===== 候选办理提示 ===== */
.fs-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin-bottom: 12px;
  background: #f3f8fe;
  border: 1px solid #e0edfa;
  border-radius: 8px;
  font-size: 12.5px;
  line-height: 1.6;
  color: #1a73c7;

  .el-icon { flex: none; font-size: 14px; }
}

/* ===== 当前节点 ===== */
.fs-current {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.fs-node-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-radius: 8px;
  background: #f3f8fe;
  border: 1px solid #e0edfa;
  max-width: 100%;
}

.fs-node-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a73c7;
}

.fs-node-owner {
  font-size: 12px;
  color: $c-muted;
  padding-left: 8px;
  border-left: 1px solid #dbe8f5;
}

.fs-done {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #12795a;
  padding: 7px 14px;
  border-radius: 8px;
  background: #eefaf5;
  border: 1px solid #d3f0e4;

  .fs-done-icon { font-size: 15px; color: $c-green; }
}

/* ===== 表格 ===== */
.fs-table {
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
    padding: 9px 0;
  }

  :deep(.el-table td.el-table__cell) {
    padding: 9px 0;
  }

  :deep(.el-table__inner-wrapper::before) {
    display: none;
  }
}

.fs-activity {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: $c-text;
  font-weight: 500;

  .fs-node-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #cbd5e1;
    flex: none;
  }

  &.is-current {
    color: #1a73c7;

    .fs-node-dot {
      background: $c-blue;
      box-shadow: 0 0 0 3px rgba(47, 142, 224, .16);
    }
  }
}

.fs-person {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.fs-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #eef4fb;
  color: #4b73a6;
  font-size: 12px;
  font-weight: 600;
  flex: none;
}

.fs-person-name {
  color: $c-text;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &.is-empty { color: #a6b0bf; }
}

.fs-time {
  color: $c-text-2;
  font-variant-numeric: tabular-nums;

  &.is-live { color: #b4720a; }
}

.fs-opinion {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: $c-text-2;
  vertical-align: bottom;
}

.fs-receiver {
  color: $c-text-2;
}

.fs-muted {
  color: #a6b0bf;
}

/* 状态胶囊 */
.fs-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
  border: 1px solid transparent;
  white-space: nowrap;

  .fs-dot {
    width: 5px;
    height: 5px;
    border-radius: 50%;
    flex: none;
  }

  &.is-green { color: #12795a; background: #eaf8f2; border-color: #c7ecdd; .fs-dot { background: $c-green; } }
  &.is-amber { color: #b4720a; background: #fef6e6; border-color: #fbe3b6; .fs-dot { background: $c-amber; } }
  &.is-red { color: #b73b37; background: #fdeeee; border-color: #f8d3d2; .fs-dot { background: $c-red; } }
  &.is-blue { color: #1a73c7; background: #eef6fe; border-color: #d5e8fa; .fs-dot { background: $c-blue; } }
  &.is-violet { color: #5849c6; background: #f1effe; border-color: #ddd8fb; .fs-dot { background: $c-violet; } }
  &.is-cyan { color: #0c8494; background: #eaf9fb; border-color: #c8edf2; .fs-dot { background: $c-cyan; } }
  &.is-gray { color: $c-muted; background: #f5f7fa; border-color: $c-line; .fs-dot { background: #b6c0cd; } }
}

@media (max-width: 1400px) {
  .fs-stats { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 720px) {
  .fs-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  /* 卡片两列排布时，最后一张独占一行，避免出现半行空缺 */
  .fs-stat:last-child:nth-child(odd) { grid-column: 1 / -1; }
  .fs-stat { padding: 12px; gap: 10px; }
  .fs-stat-icon { width: 42px; height: 42px; }
  .fs-stat-num { font-size: 22px; }
}
</style>
