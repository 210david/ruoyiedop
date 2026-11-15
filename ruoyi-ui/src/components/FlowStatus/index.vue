<template>
  <div class="fs-page" v-loading="loading">
    <!-- ===== 统计概览：点击卡片筛选下方列表（对标泛微流程状态） ===== -->
    <div class="fs-stats">
      <div class="fs-stat"
           v-for="c in statCards"
           :key="c.key"
           :class="{ 'is-active': activeFilter === c.key }"
           :title="c.desc"
           @click="toggleFilter(c.key)">
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

      <div class="fs-table" v-if="filteredPendingRows.length">
        <el-table :data="filteredPendingRows" border size="small">
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
          <el-table-column label="查看时间" width="172" align="center">
            <template #default="{ row }">
              <span class="fs-time" :class="{ 'is-live': row.viewTime }">{{ row.viewTime || '未查看' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="fs-done fs-done-filter" v-else-if="activeCard">
        <el-icon class="fs-done-icon"><InfoFilled /></el-icon>
        <span>当前节点没有符合「{{ activeCard.label }}」的办理人</span>
      </div>
    </section>

    <!-- ===== 流程记录 ===== -->
    <section class="fs-section">
      <header class="fs-section-head">
        <span class="fs-section-icon"><el-icon><Connection /></el-icon></span>
        <h3 class="fs-section-title">流程记录</h3>
        <span class="fs-filter-chip" v-if="activeCard" @click="clearFilter">
          筛选中：{{ activeCard.label }}
          <el-icon><Close /></el-icon>
        </span>
        <span class="fs-section-tag is-gray">共 {{ filteredRecordRows.length }} 条</span>
      </header>

      <div class="fs-table">
        <el-table :data="filteredRecordRows" border size="small" :empty-text="activeCard ? '没有符合「' + activeCard.label + '」的记录' : '暂无流转记录'">
          <el-table-column type="index" label="序号" width="66" align="center" />
          <el-table-column label="活动名称" min-width="190" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fs-activity" :class="{ 'is-current': !row.endTime }">
                <span class="fs-node-dot"></span>{{ row.taskName || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作人" width="130" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="fs-person">
                <span class="fs-avatar" v-if="personName(row)">{{ avatarChar(personName(row)) }}</span>
                <span class="fs-person-name" :class="{ 'is-empty': !personName(row) }">{{ personName(row) || '待认领' }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作状态" width="132" align="center">
            <template #default="{ row }">
              <span class="fs-pill" :class="'is-' + row.tone"><span class="fs-dot"></span>{{ row.actionText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="接收时间" width="172" align="center">
            <template #default="{ row }"><span class="fs-time">{{ row.createTime || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作时间" width="172" align="center">
            <template #default="{ row }"><span class="fs-time">{{ row.operateTime || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="操作耗时" width="170" align="center">
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
import { UserFilled, CircleCheckFilled, Timer, Stamp, View, Connection, InfoFilled, CircleClose, Close } from '@element-plus/icons-vue'

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
  cc: { text: '抄送', tone: 'blue' },
  ccSend: { text: '抄送', tone: 'blue' },
  canceled: { text: '流程撤销', tone: 'gray' },
  end: { text: '流程结束', tone: 'green' },
  undone: { text: '未办理', tone: 'gray' },
  intervene: { text: '管理员干预', tone: 'violet' }
}

/** 抄送动作行（ccSend）与被抄送人阅读行成对生成，统计总人次时只保留被抄送行，避免重复计数 */
function isCcRow(t) {
  return t.outcome === 'cc' || t.outcome === 'ccSend'
}

/** 管理员干预行：后台调整流程的操作，不参与「接收者」推导与办理人次统计 */
function isInterveneRow(t) {
  return t.outcome === 'intervene'
}

/**
 * 强制收回后回到发起人节点的待办：发起记录（发起提交）已经存在，
 * 这条只用于「当前节点」展示，不再重复出现在流程记录里
 */
function isInitiatorPending(t) {
  return !!t.initiatorPending
}

const rows = computed(() => props.tasks || [])

/** 参与状态：优先用后端返回的 state，老数据按 endTime 兜底 */
function stateOf(t) {
  if (t.state) return t.state
  return t.endTime ? 'done' : 'pending'
}

/**
 * 总人次：流程全部参与人员（发起人、各节点办理人/候选人、抄送接收人、转办人等），
 * 不再只统计流程节点办理人。
 * 去重口径「人员 + 所在节点」：同一人在同一节点的多条记录（已查看 + 转办等）只算一次，
 * 跨节点参与则各算一次，保持「人次」语义。
 */
const participantCount = computed(() => {
  const seen = new Set()
  rows.value.forEach(t => {
    if (t.taskId === '__end__' || isInterveneRow(t) || isInitiatorPending(t)) return
    // 抄送动作行与被抄送行重复，只保留被抄送人
    if (t.outcome === 'ccSend') return
    const name = t.assigneeName || (t.assigneeId ? String(t.assigneeId) : '')
    seen.add((name || '待认领') + '#' + (t.taskName || t.taskId || ''))
  })
  return seen.size
})

/**
 * 统计概览（对标泛微，按用户最关心的「办理进度」分类）：
 * 总人次 = 全部参与人员（含发起、抄送、转办）；
 * 已提交 / 未提交 / 已通过 只算办理人次；
 * 「已查看 / 未查看」看的是「谁点开过流程」，因此把抄送接收人的已读/未读一并算进来，
 * 一眼看清流程发出后到底谁看过、谁还没看。抄送人动作行（ccSend）不算人次。
 */
const stats = computed(() => {
  const list = rows.value.filter(t => t.taskId !== '__end__' && !isCcRow(t) && !isInterveneRow(t) && !isInitiatorPending(t))
  const unsubmit = list.filter(t => stateOf(t) !== 'done')
  // 被抄送人（cc 接收行）：已读计已查看，未读计未查看
  const ccRows = rows.value.filter(t => t.outcome === 'cc')
  const ccRead = ccRows.filter(t => !!t.viewTime).length
  return {
    total: participantCount.value,
    submitted: list.filter(t => stateOf(t) === 'done').length,
    unsubmitted: unsubmit.length,
    viewed: unsubmit.filter(t => !!t.viewTime).length + ccRead,
    unviewed: unsubmit.filter(t => !t.viewTime).length + (ccRows.length - ccRead),
    approved: list.filter(t => t.outcome === 'approved').length
  }
})

const statCards = computed(() => [
  { key: 'total', label: '总人次', value: stats.value.total, tone: 'blue', icon: UserFilled, desc: '流程全部参与人员（含发起、审批、抄送、转办等，同一节点同一人只计一次）' },
  { key: 'submitted', label: '已提交', value: stats.value.submitted, tone: 'green', icon: CircleCheckFilled, desc: '已完成办理的人次（审批通过/驳回/转办）' },
  { key: 'unsubmitted', label: '未提交', value: stats.value.unsubmitted, tone: 'amber', icon: Timer, desc: '尚未办理的人次（含待办理与未办理）' },
  { key: 'viewed', label: '已查看', value: stats.value.viewed, tone: 'cyan', icon: View, desc: '未办理但已点开查看，以及抄送已读的人次' },
  { key: 'unviewed', label: '未查看', value: stats.value.unviewed, tone: 'red', icon: CircleClose, desc: '未办理且从未点开查看，以及抄送未读的人次' },
  { key: 'approved', label: '已通过', value: stats.value.approved, tone: 'violet', icon: Stamp, desc: '审批通过的人次' }
])

/** 当前选中的统计卡（点击卡片筛选列表，再次点击取消） */
const activeFilter = ref('')
const activeCard = computed(() => statCards.value.find(c => c.key === activeFilter.value) || null)

function toggleFilter(key) {
  activeFilter.value = activeFilter.value === key ? '' : key
}

function clearFilter() {
  activeFilter.value = ''
}

/** 行是否命中当前统计口径（「总人次」= 全部记录，含抄送留痕） */
function matchFilter(t) {
  const key = activeFilter.value
  if (!key || key === 'total') return true
  if (t.taskId === '__end__') return false
  // 管理员干预是后台操作，不参与办理人次的各口径统计
  if (isInterveneRow(t)) return false
  // 被抄送人（cc 接收行）参与「已查看 / 未查看」口径：已读归已查看，未读归未查看
  if (t.outcome === 'cc') {
    if (key === 'viewed') return !!t.viewTime
    if (key === 'unviewed') return !t.viewTime
    return false
  }
  if (isCcRow(t)) return false
  const state = stateOf(t)
  if (key === 'submitted') return state === 'done'
  if (key === 'unsubmitted') return state !== 'done'
  if (key === 'viewed') return state !== 'done' && !!t.viewTime
  if (key === 'unviewed') return state !== 'done' && !t.viewTime
  if (key === 'approved') return t.outcome === 'approved'
  return true
}

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

/** 当前节点表：叠加顶部统计卡筛选 */
const filteredPendingRows = computed(() => pendingRows.value.filter(matchFilter))

/** 当前时间戳：驱动待办节点「已等待 X」刷新（有待办时每 30 秒走一次，无待办自动停表） */
const nowTs = ref(Date.now())
let ticker = null
watch(() => pendingRows.value.length, n => {
  if (n && !ticker) {
    ticker = setInterval(() => { nowTs.value = Date.now() }, 30000)
  } else if (!n && ticker) {
    clearInterval(ticker)
    ticker = null
  }
}, { immediate: true })
onBeforeUnmount(() => {
  if (ticker) clearInterval(ticker)
})

/** 当前节点里以候选人身份出现的办理人数（>0 时给出一句候选办理说明） */
const candidatePendingCount = computed(() => pendingRows.value.filter(r => r.candidate).length)

/**
 * 流程记录行（对标泛微流程状态）：操作人 / 操作状态 / 接收时间 / 操作时间 / 操作耗时
 * 接收者 = 下一条流转记录的节点 + 办理人（无下一条说明流程在此终止）
 */
const recordRows = computed(() => {
  // 发起人（开始）节点任务由 __start__ 发起记录代表：不进入流转记录，也不参与「接收者」推导
  const list = rows.value.filter(t => !isInitiatorPending(t))
  return list.map((t, i) => {
    const state = stateOf(t)
    const outcome = t.outcome || ''
    const meta = OUTCOME_MAP[outcome]
    const operateTime = operateTimeOf(t, state)
    // 下一个节点：跳过同一任务的其他候选人行、抄送记录与管理员干预行，避免「接收者」错位
    const next = list.slice(i + 1).find(x => x.taskId !== t.taskId && !isCcRow(x) && !isInterveneRow(x))
    // 接收者 = 下一节点的全部办理人（候选池多人时一并列出，看清楚流程发给了谁）
    const nextNames = next
      ? [...new Set(list.filter(x => x.taskId === next.taskId).map(x => personName(x)).filter(Boolean))]
      : []
    let receiver = ''
    if (outcome === 'cc') {
      // 被抄送人：接收者列标注抄送来源
      receiver = t.receiverName ? '由 ' + t.receiverName + ' 抄送' : ''
    } else if (outcome === 'ccSend') {
      // 抄送人：接收者列就是被抄送人
      receiver = t.receiverName || ''
    } else if (outcome === 'delegate') {
      // 转办人：接收者列是被转办人（接手继续审批的人）
      receiver = t.receiverName ? '转办给 ' + t.receiverName : ''
    } else if (outcome === 'intervene') {
      // 管理员干预：接收者列是干预后的目标节点 + 指定办理人
      receiver = t.receiverName || ''
    } else if ((state !== 'undone' || t.skipped) && next) {
      // 被跳过的节点也要显示实际流转到的节点，让流程链路保持连续
      receiver = next.taskName + (nextNames.length ? '：' + nextNames.join('、') : '')
    }
    let actionText = meta ? meta.text : '已办理'
    let tone = meta ? meta.tone : 'gray'
    if (state === 'undone') {
      // 节点未实际办理：按办理人自己的操作情况显示「未办理」，干预操作单独成行
      actionText = '未办理'
      tone = 'gray'
    } else if (state === 'pending') {
      // 待办不写「待办理」，按泛微口径直接体现是否已查看
      const viewed = !!t.viewTime
      actionText = viewed ? '已查看' : '未查看'
      tone = viewed ? 'cyan' : 'amber'
    }
    if (outcome === 'cc') {
      // 抄送接收人的操作状态：已读/未读 + 阅读时间
      const read = !!t.viewTime
      actionText = read ? '抄送·已读' : '抄送·未读'
      tone = read ? 'cyan' : 'red'
    }
    return {
      ...t,
      outcome,
      tone,
      actionText,
      operateTime,
      costText: costText(t, operateTime),
      receiver
    }
  })
})

/** 流程记录表：叠加顶部统计卡筛选（接收者仍按完整时间线推导，不受筛选影响） */
const filteredRecordRows = computed(() => recordRows.value.filter(matchFilter))

/** 办理人姓名：未分配办理人时返回空串（模板里显示「待认领」） */
function personName(row) {
  return (row && (row.assigneeName || row.assigneeId)) || ''
}

/** 头像占位字符：中文取姓（首字），英文取首字母 */
function avatarChar(name) {
  const s = (name || '').trim()
  if (!s) return '·'
  return /[\u4e00-\u9fa5]/.test(s) ? s.slice(0, 1) : s.slice(0, 1).toUpperCase()
}

/** 时间解析：兼容 Date 与「yyyy-MM-dd HH:mm:ss」字符串 */
function parseTime(v) {
  if (v instanceof Date) return v
  return new Date(String(v).replace(/-/g, '/'))
}

/**
 * 耗时：毫秒 → 「1天2小时20分钟 / 3小时20分钟 / 45分钟 / 36秒」
 * 不足 1 分钟按秒展示，1 分钟以上四舍五入到分钟；天级同样保留小时与分钟，避免信息被吞掉
 */
function durationText(begin, end) {
  if (!begin || !end) return ''
  const ms = parseTime(end) - parseTime(begin)
  if (isNaN(ms) || ms < 0) return ''
  if (ms < 60000) {
    const secs = Math.floor(ms / 1000)
    return secs > 0 ? secs + '秒' : '不足1秒'
  }
  const totalMins = Math.round(ms / 60000)
  const days = Math.floor(totalMins / 1440)
  const hours = Math.floor((totalMins % 1440) / 60)
  const rest = totalMins % 60
  return (days ? days + '天' : '') + (hours ? hours + '小时' : '') + (rest ? rest + '分钟' : '')
}

/** 操作时间：已办取完成时间；待办/候选取查看时间（未查看为空）；未办理为空 */
function operateTimeOf(t, state) {
  if (t.outcome === 'cc') return t.viewTime || ''
  if (state === 'pending') return t.viewTime || ''
  if (state === 'undone') return ''
  return t.endTime || ''
}

/** 行耗时：以待办理「已等待 X」兜底；发起节点与「流程结束」伪记录不计耗时 */
function costText(t, operateTime) {
  if (t.taskId === '__end__') return '—'
  if (t.outcome === 'start' || t.outcome === 'intervene') return '—'
  if (stateOf(t) === 'undone') return '—'
  if (!operateTime) {
    if (stateOf(t) === 'pending' && t.createTime) {
      return '已等待 ' + durationText(t.createTime, new Date(nowTs.value))
    }
    return '—'
  }
  return durationText(t.createTime, operateTime) || '—'
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
  cursor: pointer;
  user-select: none;
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease, background .2s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #d7e6f6;
    box-shadow: 0 10px 22px -12px rgba(15, 23, 42, .28);
  }

  /* 点击卡片筛选：选中态用蓝色描边高亮（对标泛微） */
  &.is-active {
    border-color: $c-blue;
    background: #f5faff;
    box-shadow: 0 0 0 2px rgba(47, 142, 224, .14), 0 10px 22px -14px rgba(47, 142, 224, .6);

    .fs-stat-label { color: $c-blue; font-weight: 600; }
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
  &.tone-red { background: linear-gradient(135deg, #f0837f, $c-red); box-shadow: 0 8px 16px -10px rgba(228, 83, 79, .95); }
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

  /* 统计卡筛选把当前节点过滤空了 */
  &.fs-done-filter {
    margin-top: 4px;
    color: $c-muted;
    background: #f6f8fb;
    border-color: $c-line;

    .fs-done-icon { color: $c-muted; }
  }
}

/* 统计卡筛选中的标识（点击可清除） */
.fs-filter-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  color: $c-blue;
  background: #eaf3fd;
  border: 1px solid #cfe5fa;
  border-radius: 999px;
  cursor: pointer;
  transition: background .2s ease;

  &:hover { background: #dcecfc; }

  .el-icon { font-size: 12px; }
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
