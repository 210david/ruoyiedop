<template>
  <el-dialog v-model="dialogVisible" width="780px" append-to-body draggable class="rd-dialog">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg v-if="type === 'log'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="16" y1="13" x2="8" y2="13" />
            <line x1="16" y1="17" x2="8" y2="17" />
          </svg>
          <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
        </div>
        <div class="rd-detail-header-main">
          <div class="rd-detail-header-title">{{ type === 'log' ? '调度日志详细' : '任务详细' }}</div>
          <div class="rd-detail-header-sub" v-if="type === 'log' && form.jobLogId">
            <span class="rd-detail-header-no">日志编号：{{ form.jobLogId }}</span>
          </div>
          <div class="rd-detail-header-sub" v-else-if="type === 'job' && form.jobId">
            <span class="rd-detail-header-no">任务编号：{{ form.jobId }}</span>
          </div>
        </div>
      </div>
    </template>
    <div class="rd-page">
      <template v-if="type === 'log'">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>
              基本信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">日志编号</span>
                <div class="rd-value">{{ form.jobLogId || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">执行状态</span>
                <div class="rd-value">
                  <el-tag v-if="form.status == 0" type="success" size="small" effect="light" round>正常</el-tag>
                  <el-tag v-else type="danger" size="small" effect="light" round>失败</el-tag>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">开始时间</span>
                <div class="rd-value">{{ form.startTime || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">结束时间</span>
                <div class="rd-value">{{ form.endTime || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">记录时间</span>
                <div class="rd-value">{{ form.createTime || '-' }}</div>
              </div>
              <div class="rd-item" v-if="form.status == 0 && form.startTime && form.endTime">
                <span class="rd-label">执行耗时</span>
                <div class="rd-value">{{ costTime }} 毫秒</div>
              </div>
            </div>
          </div>
        </section>
        <!-- 任务信息 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>
              任务信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">任务名称</span>
                <div class="rd-value">{{ form.jobName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">任务分组</span>
                <div class="rd-value"><dict-tag :options="sys_job_group" :value="form.jobGroup" /></div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">日志信息</span>
                <div class="rd-value">{{ form.jobMessage || '暂无日志信息' }}</div>
              </div>
            </div>
          </div>
        </section>
        <!-- 调用目标 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg></span>
              调用目标
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="code-block"><pre>{{ form.invokeTarget || '（无）' }}</pre></div>
          </div>
        </section>
        <!-- 异常信息 -->
        <section class="rd-card" v-if="form.status == 1">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon" style="color:#ef4444;"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>
              异常信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="error-block"><pre>{{ form.exceptionInfo || '暂无异常信息' }}</pre></div>
          </div>
        </section>
      </template>

      <template v-else>
        <!-- 任务配置 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>
              任务配置
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">任务编号</span>
                <div class="rd-value">{{ form.jobId || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">任务名称</span>
                <div class="rd-value">{{ form.jobName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">任务分组</span>
                <div class="rd-value"><dict-tag :options="sys_job_group" :value="form.jobGroup" /></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">执行状态</span>
                <div class="rd-value">
                  <el-tag v-if="form.status == 0" type="success" size="small" effect="light" round>正常</el-tag>
                  <el-tag v-else type="info" size="small" effect="light" round>暂停</el-tag>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 调度信息 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>
              调度信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">cron表达式</span>
                <div class="rd-value" style="font-family:ui-monospace,'JetBrains Mono','SF Mono',Menlo,monospace;">{{ form.cronExpression || '-' }}</div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">表达式释义</span>
                <div class="rd-value cron-desc">
                  <el-icon class="cron-desc-icon"><InfoFilled /></el-icon>
                  <span v-if="form.cronExpression">{{ cronDescription(form.cronExpression) }}</span>
                  <span v-else class="cron-desc-empty">-</span>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">下次执行时间</span>
                <div class="rd-value">{{ parseTime(form.nextValidTime) || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">执行策略</span>
                <div class="rd-value">
                  <el-tag v-if="form.misfirePolicy == 0" type="info" size="small" effect="light" round>默认策略</el-tag>
                  <el-tag v-else-if="form.misfirePolicy == 1" type="warning" size="small" effect="light" round>立即执行</el-tag>
                  <el-tag v-else-if="form.misfirePolicy == 2" type="primary" size="small" effect="light" round>执行一次</el-tag>
                  <el-tag v-else-if="form.misfirePolicy == 3" type="danger" size="small" effect="light" round>放弃执行</el-tag>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">并发执行</span>
                <div class="rd-value">
                  <el-tag v-if="form.concurrent == 0" type="success" size="small" effect="light" round>允许</el-tag>
                  <el-tag v-else type="danger" size="small" effect="light" round>禁止</el-tag>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 执行方法 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg></span>
              执行方法
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="code-block"><pre>{{ form.invokeTarget || '（无）' }}</pre></div>
          </div>
        </section>
        <!-- 元信息 -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>
              元信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">创建人</span>
                <div class="rd-value">{{ form.createBy || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">创建时间</span>
                <div class="rd-value">{{ form.createTime || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">更新人</span>
                <div class="rd-value">{{ form.updateBy || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">更新时间</span>
                <div class="rd-value">{{ form.updateTime || '-' }}</div>
              </div>
              <div class="rd-item rd-item--full" v-if="form.remark">
                <span class="rd-label">备注</span>
                <div class="rd-value">{{ form.remark }}</div>
              </div>
            </div>
          </div>
        </section>
      </template>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="JobDetail">
const props = defineProps({
  visible: { type: Boolean, default: false },
  row: { type: Object, default: () => ({}) },
  // 'job' 任务详细 | 'log' 调度日志详细
  type: { type: String, default: 'job' }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const { proxy } = getCurrentInstance()
const { sys_job_group } = useDict('sys_job_group')

const form = computed(() => props.row || {})

const costTime = computed(() => {
  if (!form.value.startTime || !form.value.endTime) return 0
  return new Date(form.value.endTime).getTime() - new Date(form.value.startTime).getTime()
})

/**
 * 解析 cron 表达式，返回人类可读的执行周期描述
 * 支持 Quartz 格式：秒 分 时 日 月 周 [年]
 */
function cronDescription(cron) {
  if (!cron) return '-'
  const parts = cron.trim().split(/\s+/)
  if (parts.length < 6) return '表达式格式不正确（至少需要6段：秒 分 时 日 月 周）'

  const [sec, min, hour, day, month, week] = parts
  const desc = []

  // ----- 秒 -----
  desc.push(parseField(sec, '秒', 0, 59))

  // ----- 分 -----
  desc.push(parseField(min, '分', 0, 59))

  // ----- 时 -----
  desc.push(parseField(hour, '时', 0, 23))

  // ----- 日 + 周（特殊关系：L、LW、? 等）-----
  const dayDesc = parseDayAndWeek(day, week)

  // ----- 月 -----
  const monthDesc = parseMonth(month)

  // 组装
  const timePart = desc.filter(Boolean).join('、')
  let result = ''
  if (timePart) {
    result = '在' + timePart
  }

  if (dayDesc) {
    result += (result ? '，' : '') + dayDesc
  }

  if (monthDesc && monthDesc !== '每月') {
    result += '，' + monthDesc
  }

  result += '执行'
  return result || '无法解析该表达式'
}

/**
 * 解析单个字段（秒/分/时）
 */
function parseField(val, unit, min, max) {
  if (!val || val === '*') return ''  // 通配符不描述
  if (val === '?') return ''  // 不指定

  // a/b → 从 a 开始，每 b 个单位
  const stepMatch = val.match(/^(\d+)\/(\d+)$/)
  if (stepMatch) {
    const start = parseInt(stepMatch[1])
    const step = parseInt(stepMatch[2])
    if (start === 0) {
      return `每${step}${unit}`
    }
    return `从第${start}${unit}开始每${step}${unit}`
  }

  // */b → 每 b 个单位
  const allStepMatch = val.match(/^\*\/(\d+)$/)
  if (allStepMatch) {
    return `每${parseInt(allStepMatch[1])}${unit}`
  }

  // a-b → 范围
  const rangeMatch = val.match(/^(\d+)-(\d+)$/)
  if (rangeMatch) {
    return `第${rangeMatch[1]}到第${rangeMatch[2]}${unit}`
  }

  // a,b,c → 列表
  if (val.includes(',')) {
    const items = val.split(',').map(v => v.trim())
    // 检查每个元素是否是范围
    const parts = items.map(item => {
      const r = item.match(/^(\d+)-(\d+)$/)
      if (r) return `第${r[1]}到第${r[2]}${unit}`
      return `第${item}${unit}`
    })
    return parts.join('、')
  }

  // L → 最后
  if (val === 'L') return `最后1${unit}`

  // 纯数字
  if (/^\d+$/.test(val)) {
    return `第${val}${unit}`
  }

  return ''
}

/**
 * 解析日和周的组合
 */
function parseDayAndWeek(day, week) {
  const weekNames = {
    '0': '周日', '1': '周一', '2': '周二', '3': '周三',
    '4': '周四', '5': '周五', '6': '周六', '7': '周日'
  }

  let dayPart = ''
  let weekPart = ''

  // 日
  if (day === '*') {
    dayPart = '每天'
  } else if (day === '?') {
    // 日不指定，由周决定
  } else if (day === 'L') {
    dayPart = '每月最后一天'
  } else if (day === 'LW') {
    dayPart = '每月最后一个工作日'
  } else if (/^(\d+)W$/.test(day)) {
    const d = day.match(/^(\d+)W$/)
    dayPart = `每月最接近第${d[1]}天的工作日`
  } else if (/^\*\/(\d+)$/.test(day)) {
    const d = day.match(/^\*\/(\d+)$/)
    dayPart = `每${parseInt(d[1])}天`
  } else if (/^(\d+)\/(\d+)$/.test(day)) {
    const d = day.match(/^(\d+)\/(\d+)$/)
    dayPart = `从每月第${d[1]}天开始每${d[2]}天`
  } else if (/^(\d+)-(\d+)$/.test(day)) {
    const d = day.match(/^(\d+)-(\d+)$/)
    dayPart = `每月第${d[1]}到第${d[2]}天`
  } else if (day.includes(',')) {
    dayPart = '每月第' + day.split(',').join('、') + '天'
  } else if (/^\d+$/.test(day)) {
    dayPart = `每月第${day}天`
  }

  // 周
  if (week === '*') {
    // 不额外描述
  } else if (week === '?') {
    // 周不指定
  } else if (/^(\d+)L$/.test(week)) {
    const w = week.match(/^(\d+)L$/)
    weekPart = `每月最后一个${weekNames[w[1]] || '周' + w[1]}`
  } else if (/^(\d+)#(\d+)$/.test(week)) {
    const w = week.match(/^(\d+)#(\d+)$/)
    weekPart = `每月第${w[2]}个${weekNames[w[1]] || '周' + w[1]}`
  } else if (/^\*\/(\d+)$/.test(week)) {
    const w = week.match(/^\*\/(\d+)$/)
    weekPart = `每${parseInt(w[1])}周`
  } else if (/^(\d+)-(\d+)$/.test(week)) {
    const w = week.match(/^(\d+)-(\d+)$/)
    weekPart = `从${weekNames[w[1]] || '周' + w[1]}到${weekNames[w[2]] || '周' + w[2]}`
  } else if (week.includes(',')) {
    const items = week.split(',').map(v => weekNames[v.trim()] || '周' + v.trim())
    weekPart = '每' + items.join('、')
  } else if (/^\d+$/.test(week)) {
    weekPart = `每${weekNames[week] || '周' + week}`
  }

  // 组合
  if (dayPart && weekPart) {
    return dayPart + '且' + weekPart
  }
  return dayPart || weekPart || '每天'
}

/**
 * 解析月字段
 */
function parseMonth(month) {
  if (!month || month === '*' || month === '?') return '每月'
  const monthNames = {
    '1': '一月', '2': '二月', '3': '三月', '4': '四月',
    '5': '五月', '6': '六月', '7': '七月', '8': '八月',
    '9': '九月', '10': '十月', '11': '十一月', '12': '十二月'
  }

  if (/^\*\/(\d+)$/.test(month)) {
    const m = month.match(/^\*\/(\d+)$/)
    return `每${parseInt(m[1])}个月`
  }

  if (/^(\d+)\/(\d+)$/.test(month)) {
    const m = month.match(/^(\d+)\/(\d+)$/)
    return `从${monthNames[m[1]] || m[1] + '月'}开始每${m[2]}个月`
  }

  if (/^(\d+)-(\d+)$/.test(month)) {
    const m = month.match(/^(\d+)-(\d+)$/)
    return `${monthNames[m[1]] || m[1] + '月'}到${monthNames[m[2]] || m[2] + '月'}`
  }

  if (month.includes(',')) {
    const items = month.split(',').map(v => monthNames[v.trim()] || v.trim() + '月')
    return items.join('、')
  }

  if (/^\d+$/.test(month)) {
    return monthNames[month] || month + '月'
  }

  return '每月'
}
</script>

<style scoped>
/* el-dialog 样式由全局 detail-page.scss 中的 .rd-dialog 控制 */

/* ---- 代码块 ---- */
.code-block {
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px 16px;
  overflow-x: auto;
}
.code-block pre {
  margin: 0; font-family: ui-monospace, 'JetBrains Mono', 'SF Mono', Menlo, monospace;
  font-size: 13px; color: #334155; white-space: pre-wrap; word-break: break-all; line-height: 1.6;
}

/* ---- 异常信息块 ---- */
.error-block {
  background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 12px 16px;
  overflow-x: auto;
}
.error-block pre {
  margin: 0; font-family: ui-monospace, 'JetBrains Mono', 'SF Mono', Menlo, monospace;
  font-size: 13px; color: #b91c1c; white-space: pre-wrap; word-break: break-all; line-height: 1.6;
}

/* ---- cron 表达式释义 ---- */
.cron-desc {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  color: #0c4a6e;
  line-height: 1.6;
}
.cron-desc-icon {
  color: #0284c7;
  font-size: 15px;
  flex-shrink: 0;
  margin-top: 1px;
}
.cron-desc-empty {
  color: #94a3b8;
}
</style>
