<template>
  <div class="app-container monitor-job-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>任务名称</label>
          <div class="control">
            <el-input v-model="queryParams.jobName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>任务组名</label>
          <div class="control is-select">
            <el-select v-model="queryParams.jobGroup" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sys_job_group" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>任务状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sys_job_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>所属模块</label>
          <div class="control is-select">
            <el-select v-model="queryParams.moduleKey" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="m in moduleOptions" :key="m.moduleKey" :label="m.moduleName" :value="m.moduleKey" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>调用目标</label>
          <div class="control">
            <el-input v-model="queryParams.invokeTarget" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
          <el-checkbox v-model="showDisabledModule" style="margin-left: 16px;">显示已停用模块任务</el-checkbox>
        </div>
        <div class="filter-buttons">
          <el-button link type="primary" class="advanced-toggle" @click="showAdvanced = !showAdvanced">
            {{ showAdvanced ? '收起筛选' : '高级筛选' }}
            <el-icon class="advanced-arrow" :class="{ 'is-open': showAdvanced }"><ArrowDown /></el-icon>
            <span v-if="advancedCount > 0 && !showAdvanced" class="advanced-badge">{{ advancedCount }}</span>
          </el-button>
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['monitor:job:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['monitor:job:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:job:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['monitor:job:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleJobLog" v-hasPermi="['monitor:job:query']">
            <el-icon><Operation /></el-icon> 日志
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="monitor_job_columns"></right-toolbar>
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="jobList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="任务编号" prop="jobId" key="jobId" :width="colWidth('jobId', 100)" resizable align="center" v-if="columns.jobId.visible" />
          <el-table-column label="任务名称" prop="jobName" key="jobName" :min-width="colWidth('jobName', 200)" resizable align="center" show-overflow-tooltip v-if="columns.jobName.visible">
            <template #default="scope">
              <a class="link-type" style="cursor:pointer" @click="handleView(scope.row)">{{ scope.row.jobName }}</a>
            </template>
          </el-table-column>
          <el-table-column label="任务组名" prop="jobGroup" key="jobGroup" :width="colWidth('jobGroup', 120)" resizable align="center" v-if="columns.jobGroup.visible">
            <template #default="scope">
              <span class="badge violet">{{ jobGroupLabel(scope.row.jobGroup) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="所属模块" prop="moduleKey" key="moduleKey" :width="colWidth('moduleKey', 120)" resizable align="center" v-if="columns.moduleKey.visible">
            <template #default="scope">
              <el-tag v-if="scope.row.moduleKey" size="small" :type="moduleMap[scope.row.moduleKey]?.status === '1' ? 'info' : 'success'">{{ moduleMap[scope.row.moduleKey]?.moduleName || scope.row.moduleKey }}</el-tag>
              <el-tag v-else size="small" type="primary" effect="plain">平台</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="调用目标字符串" prop="invokeTarget" key="invokeTarget" :width="colWidth('invokeTarget', 220)" resizable align="center" show-overflow-tooltip v-if="columns.invokeTarget.visible" />
          <el-table-column label="cron执行表达式" prop="cronExpression" key="cronExpression" :width="colWidth('cronExpression', 180)" resizable align="center" show-overflow-tooltip v-if="columns.cronExpression.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleStatusChange(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['monitor:job:edit']">修改</el-button>
                <el-button link type="primary" icon="CaretRight" @click="handleRun(scope.row)" v-hasPermi="['monitor:job:changeStatus']">执行</el-button>
                <el-button link type="primary" icon="Operation" @click="handleJobLog(scope.row)" v-hasPermi="['monitor:job:query']">日志</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['monitor:job:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加或修改定时任务对话框 -->
    <el-dialog v-model="open" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="jobRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <!-- 任务基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>任务基本信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="任务名称" prop="jobName">
                    <el-input v-model="form.jobName" placeholder="请输入任务名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="任务分组" prop="jobGroup">
                    <el-select v-model="form.jobGroup" placeholder="请选择">
                      <el-option
                        v-for="dict in sys_job_group"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item prop="invokeTarget">
                    <template #label>
                      <span>
                        调用方法
                        <el-tooltip placement="top">
                          <template #content>
                            <div>
                              Bean调用示例：ryTask.ryParams('ry')
                              <br />Class类调用示例：com.ruoyi.quartz.task.RyTask.ryParams('ry')
                              <br />参数说明：支持字符串，布尔类型，长整型，浮点型，整型
                            </div>
                          </template>
                          <el-icon><question-filled /></el-icon>
                        </el-tooltip>
                      </span>
                    </template>
                    <el-input v-model="form.invokeTarget" placeholder="请输入调用目标字符串" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="cron表达式" prop="cronExpression">
                    <div class="cron-field">
                      <el-input v-model="form.cronExpression" placeholder="请输入cron执行表达式">
                        <template #append>
                          <el-button type="primary" @click="handleShowCron">
                            生成表达式
                            <i class="el-icon-time el-icon--right"></i>
                          </el-button>
                        </template>
                      </el-input>
                      <div class="cron-presets">
                        <span class="cron-presets-label">常用：</span>
                        <el-tag v-for="preset in cronPresets" :key="preset.value" size="small" effect="plain" class="cron-preset-tag" @click="form.cronExpression = preset.value">
                          {{ preset.label }}
                        </el-tag>
                      </div>
                      <div class="cron-hint" v-if="form.cronExpression">
                        <el-icon><InfoFilled /></el-icon>
                        <span>格式：秒 分 时 日 月 周 [年]，共6-7段。示例：0/10 * * * * ?</span>
                      </div>
                      <div class="cron-desc" v-if="form.cronExpression && cronDescription(form.cronExpression)">
                        <el-icon class="cron-desc-icon"><InfoFilled /></el-icon>
                        <span>{{ cronDescription(form.cronExpression) }}</span>
                      </div>
                      <div class="cron-next-run" v-if="nextValidTime">
                        <el-icon><Clock /></el-icon>
                        <span>下次执行时间：{{ nextValidTime }}</span>
                      </div>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属模块" prop="moduleKey">
                    <el-select v-model="form.moduleKey" placeholder="平台级（默认）" clearable style="width: 100%">
                      <el-option v-for="m in moduleOptions" :key="m.moduleKey" :label="m.moduleName + (m.status === '1' ? '（已停用）' : '')" :value="m.moduleKey" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12" v-if="form.jobId !== undefined">
                  <el-form-item label="状态">
                    <el-radio-group v-model="form.status">
                      <el-radio
                        v-for="dict in sys_job_status"
                        :key="dict.value"
                        :value="dict.value"
                      >{{ dict.label }}</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 调度配置 -->
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>调度配置</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item prop="misfirePolicy">
                    <template #label>
                      <span>
                        执行策略
                        <el-tooltip placement="top" :width="320">
                          <template #content>
                            <div>
                              执行策略是指任务因服务器重启等原因错过触发时间后，如何处理这些错过的执行。与"暂停"不同：暂停是停止任务调度，而执行策略是处理错过触发的补偿方式。
                            </div>
                          </template>
                          <el-icon><question-filled /></el-icon>
                        </el-tooltip>
                      </span>
                    </template>
                    <el-radio-group v-model="form.misfirePolicy">
                      <el-radio-button value="1">立即执行</el-radio-button>
                      <el-radio-button value="2">执行一次</el-radio-button>
                      <el-radio-button value="3">放弃执行</el-radio-button>
                    </el-radio-group>
                    <div class="field-hint">
                      <span v-if="form.misfirePolicy === '1'">立即补执行所有错过的触发</span>
                      <span v-else-if="form.misfirePolicy === '2'">仅补执行一次，后续按正常计划继续</span>
                      <span v-else-if="form.misfirePolicy === '3'">忽略所有错过的触发，等待下次正常触发</span>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item prop="concurrent">
                    <template #label>
                      <span>
                        是否并发
                        <el-tooltip placement="top" :width="320">
                          <template #content>
                            <div>
                              是否允许任务并发执行：允许时，上一次任务尚未执行完，下次触发仍会启动新的执行实例；禁止时，必须等待上一次执行完成后才会触发下次执行，防止任务重叠。
                            </div>
                          </template>
                          <el-icon><question-filled /></el-icon>
                        </el-tooltip>
                      </span>
                    </template>
                    <el-radio-group v-model="form.concurrent">
                      <el-radio-button value="0">允许</el-radio-button>
                      <el-radio-button value="1">禁止</el-radio-button>
                    </el-radio-group>
                    <div class="field-hint">
                      <span v-if="form.concurrent === '0'">允许同时运行多个任务实例</span>
                      <span v-else-if="form.concurrent === '1'">禁止重叠执行，上一次未完成时不会触发下次</span>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="openCron" append-to-body destroy-on-close class="job-cron-dialog" title="Cron表达式生成器">
      <crontab ref="crontabRef" @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>

    <!-- 任务详细 -->
    <job-detail v-model:visible="openView" :row="form" type="job" />
  </div>
</template>

<script setup name="Job">
import Crontab from '@/components/Crontab'
import JobDetail from './detail'
import { listJob, getJob, delJob, addJob, updateJob, runJob, changeJobStatus } from "@/api/monitor/job"
import { listModule } from "@/api/system/module"
import { useColumnResize } from '@/composables/useColumnResize'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { sys_job_group, sys_job_status } = useDict("sys_job_group", "sys_job_status")

const jobList = ref([])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('monitor_job_index')
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const openView = ref(false)
const openCron = ref(false)
const expression = ref("")
// 模块开关选项与显示已停用模块任务
const moduleOptions = ref([])
const showDisabledModule = ref(false)
const moduleMap = computed(() => {
  const map = {}
  moduleOptions.value.forEach(m => { map[m.moduleKey] = m })
  return map
})

/** cron常用表达式预设 */
const cronPresets = [
  { label: '每10秒', value: '0/10 * * * * ?' },
  { label: '每30秒', value: '0/30 * * * * ?' },
  { label: '每1分钟', value: '0 * * * * ?' },
  { label: '每5分钟', value: '0 0/5 * * * ?' },
  { label: '每30分钟', value: '0 0/30 * * * ?' },
  { label: '每1小时', value: '0 0 * * * ?' },
  { label: '每天1点', value: '0 0 1 * * ?' },
  { label: '每天8点', value: '0 0 8 * * ?' },
  { label: '每月1号', value: '0 0 0 1 * ?' },
]

/** 下次执行时间预览 */
const nextValidTime = computed(() => {
  if (!form.value.cronExpression) return null
  try {
    // 简单校验：至少6段
    const parts = form.value.cronExpression.trim().split(/\s+/)
    if (parts.length < 6) return null
    // 如果是编辑已有任务，后端会返回 nextValidTime；新增时本地不做复杂计算
    return form.value.nextValidTime || null
  } catch {
    return null
  }
})

/**
 * 解析 cron 表达式，返回人类可读的执行周期描述
 * 支持 Quartz 格式：秒 分 时 日 月 周 [年]
 */
function cronDescription(cron) {
  if (!cron) return ''
  const parts = cron.trim().split(/\s+/)
  if (parts.length < 6) return ''

  const [sec, min, hour, day, month, week] = parts
  const desc = []

  desc.push(parseCronField(sec, '秒'))
  desc.push(parseCronField(min, '分'))
  desc.push(parseCronField(hour, '时'))

  const dayDesc = parseCronDayAndWeek(day, week)
  const monthDesc = parseCronMonth(month)

  const timePart = desc.filter(Boolean).join('、')
  let result = ''
  if (timePart) result = '在' + timePart
  if (dayDesc) result += (result ? '，' : '') + dayDesc
  if (monthDesc && monthDesc !== '每月') result += '，' + monthDesc
  result += '执行'
  return result || ''
}

function parseCronField(val, unit) {
  if (!val || val === '*' || val === '?') return ''
  const stepMatch = val.match(/^(\d+)\/(\d+)$/)
  if (stepMatch) {
    const start = parseInt(stepMatch[1])
    const step = parseInt(stepMatch[2])
    if (start === 0) return `每${step}${unit}`
    return `从第${start}${unit}开始每${step}${unit}`
  }
  const allStepMatch = val.match(/^\*\/(\d+)$/)
  if (allStepMatch) return `每${parseInt(allStepMatch[1])}${unit}`
  const rangeMatch = val.match(/^(\d+)-(\d+)$/)
  if (rangeMatch) return `第${rangeMatch[1]}到第${rangeMatch[2]}${unit}`
  if (val.includes(',')) {
    return val.split(',').map(item => {
      const r = item.trim().match(/^(\d+)-(\d+)$/)
      if (r) return `第${r[1]}到第${r[2]}${unit}`
      return `第${item.trim()}${unit}`
    }).join('、')
  }
  if (val === 'L') return `最后1${unit}`
  if (/^\d+$/.test(val)) return `第${val}${unit}`
  return ''
}

function parseCronDayAndWeek(day, week) {
  const weekNames = { '0': '周日', '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日' }
  let dayPart = '', weekPart = ''
  if (day === '*') dayPart = '每天'
  else if (day === '?') { /* 由周决定 */ }
  else if (day === 'L') dayPart = '每月最后一天'
  else if (day === 'LW') dayPart = '每月最后一个工作日'
  else if (/^(\d+)W$/.test(day)) { const d = day.match(/^(\d+)W$/); dayPart = `每月最接近第${d[1]}天的工作日` }
  else if (/^\*\/(\d+)$/.test(day)) { const d = day.match(/^\*\/(\d+)$/); dayPart = `每${parseInt(d[1])}天` }
  else if (/^(\d+)\/(\d+)$/.test(day)) { const d = day.match(/^(\d+)\/(\d+)$/); dayPart = `从每月第${d[1]}天开始每${d[2]}天` }
  else if (/^(\d+)-(\d+)$/.test(day)) { const d = day.match(/^(\d+)-(\d+)$/); dayPart = `每月第${d[1]}到第${d[2]}天` }
  else if (day.includes(',')) dayPart = '每月第' + day.split(',').join('、') + '天'
  else if (/^\d+$/.test(day)) dayPart = `每月第${day}天`

  if (week === '*' || week === '?') { /* 不额外描述 */ }
  else if (/^(\d+)L$/.test(week)) { const w = week.match(/^(\d+)L$/); weekPart = `每月最后一个${weekNames[w[1]] || '周' + w[1]}` }
  else if (/^(\d+)#(\d+)$/.test(week)) { const w = week.match(/^(\d+)#(\d+)$/); weekPart = `每月第${w[2]}个${weekNames[w[1]] || '周' + w[1]}` }
  else if (/^\*\/(\d+)$/.test(week)) { const w = week.match(/^\*\/(\d+)$/); weekPart = `每${parseInt(w[1])}周` }
  else if (/^(\d+)-(\d+)$/.test(week)) { const w = week.match(/^(\d+)-(\d+)$/); weekPart = `从${weekNames[w[1]] || '周' + w[1]}到${weekNames[w[2]] || '周' + w[2]}` }
  else if (week.includes(',')) weekPart = '每' + week.split(',').map(v => weekNames[v.trim()] || '周' + v.trim()).join('、')
  else if (/^\d+$/.test(week)) weekPart = `每${weekNames[week] || '周' + week}`

  if (dayPart && weekPart) return dayPart + '且' + weekPart
  return dayPart || weekPart || '每天'
}

function parseCronMonth(month) {
  if (!month || month === '*' || month === '?') return '每月'
  const monthNames = { '1': '一月', '2': '二月', '3': '三月', '4': '四月', '5': '五月', '6': '六月', '7': '七月', '8': '八月', '9': '九月', '10': '十月', '11': '十一月', '12': '十二月' }
  if (/^\*\/(\d+)$/.test(month)) { const m = month.match(/^\*\/(\d+)$/); return `每${parseInt(m[1])}个月` }
  if (/^(\d+)\/(\d+)$/.test(month)) { const m = month.match(/^(\d+)\/(\d+)$/); return `从${monthNames[m[1]] || m[1] + '月'}开始每${m[2]}个月` }
  if (/^(\d+)-(\d+)$/.test(month)) { const m = month.match(/^(\d+)-(\d+)$/); return `${monthNames[m[1]] || m[1] + '月'}到${monthNames[m[2]] || m[2] + '月'}` }
  if (month.includes(',')) return month.split(',').map(v => monthNames[v.trim()] || v.trim() + '月').join('、')
  if (/^\d+$/.test(month)) return monthNames[month] || month + '月'
  return '每月'
}

/** 列显隐配置 */
const defaultColumns = {
  jobId: { label: '任务编号', visible: true },
  jobName: { label: '任务名称', visible: true },
  jobGroup: { label: '任务组名', visible: true },
  moduleKey: { label: '所属模块', visible: true },
  invokeTarget: { label: '调用目标字符串', visible: true },
  cronExpression: { label: 'cron执行表达式', visible: true },
  status: { label: '状态', visible: true },
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('monitor_job_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

/** 已选筛选条件数量 */
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.jobName) count++
  if (queryParams.value.jobGroup) count++
  if (queryParams.value.status) count++
  if (queryParams.value.moduleKey) count++
  if (queryParams.value.invokeTarget) count++
  return count
})

/** 高级筛选区已填条件数量 */
const advancedCount = computed(() => {
  let count = 0
  if (queryParams.value.invokeTarget) count++
  return count
})

/** 获取模块开关选项 */
function getModuleOptions() {
  listModule({ pageNum: 1, pageSize: 100 }).then(response => {
    moduleOptions.value = response.rows
  })
}

/** 任务组名字典标签 */
function jobGroupLabel(value) {
  const item = sys_job_group.value.find(d => d.value == value)
  return item ? item.label : '-'
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    jobName: undefined,
    jobGroup: undefined,
    status: undefined,
    moduleKey: undefined,
    invokeTarget: undefined
  },
  rules: {
    jobName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
    invokeTarget: [{ required: true, message: "调用目标字符串不能为空", trigger: "blur" }],
    cronExpression: [{ required: true, message: "cron执行表达式不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询定时任务列表 */
function getList() {
  loading.value = true
  listJob({ ...queryParams.value, showDisabledModule: showDisabledModule.value ? 'true' : undefined }).then(response => {
    jobList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    jobId: undefined,
    jobName: undefined,
    jobGroup: undefined,
    moduleKey: undefined,
    invokeTarget: undefined,
    cronExpression: undefined,
    misfirePolicy: '1',
    concurrent: '1',
    status: "0"
  }
  proxy.resetForm("jobRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  // 搜索后自动收起高级筛选
  showAdvanced.value = false
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value.jobName = undefined
  queryParams.value.jobGroup = undefined
  queryParams.value.status = undefined
  queryParams.value.moduleKey = undefined
  queryParams.value.invokeTarget = undefined
  showDisabledModule.value = false
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.jobId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 任务状态修改 */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.jobName + '"任务吗?').then(function () {
    return changeJobStatus(row.jobId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 立即执行一次 */
function handleRun(row) {
  proxy.$modal.confirm('确认要立即执行一次"' + row.jobName + '"任务吗?').then(function () {
    return runJob(row.jobId, row.jobGroup)
  }).then(() => {
    proxy.$modal.msgSuccess("执行成功")
  }).catch(() => {})
}

/** 任务详细信息 */
function handleView(row) {
  getJob(row.jobId).then(response => {
    form.value = response.data
    openView.value = true
  })
}

/** cron表达式按钮操作 */
function handleShowCron() {
  expression.value = form.value.cronExpression
  openCron.value = true
}

/** 确定后回传值 */
function crontabFill(value) {
  form.value.cronExpression = value
}

/** 任务日志列表查询 */
function handleJobLog(row) {
  const jobId = row.jobId || 0
  router.push('/monitor/job-log/index/' + jobId)
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加任务"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const jobId = row.jobId || ids.value
  getJob(jobId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改任务"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["jobRef"].validate(valid => {
    if (valid) {
      // 所属模块空值统一为 null（平台级）
      if (!form.value.moduleKey) {
        form.value.moduleKey = null
      }
      if (form.value.jobId != undefined) {
        updateJob(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addJob(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const jobIds = row.jobId || ids.value
  proxy.$modal.confirm('是否确认删除选中的定时任务？').then(function () {
    return delJob(jobIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("monitor/job/export", proxy.withExportIds({
    ...queryParams.value,
  }, ids.value, 'jobId'), `job_${new Date().getTime()}.xlsx`)
}

getList()
getModuleOptions()

// 勾选显示已停用模块任务时重新加载
watch(showDisabledModule, () => {
  handleQuery()
})
</script>

<style scoped>
/* ===== Design Tokens ===== */
.monitor-job-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.monitor-job-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.monitor-job-page .filter-card { padding:14px 20px 16px; }
.monitor-job-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.monitor-job-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.monitor-job-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.monitor-job-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.monitor-job-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.monitor-job-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.monitor-job-page .filter-card .filter-buttons { display:flex; gap:8px; align-items:center; }
.monitor-job-page .filter-card .advanced-toggle { height:32px; padding:0 8px; font-size:14px; font-weight:500; }
.monitor-job-page .filter-card .advanced-arrow { margin-left:2px; font-size:14px; transition:transform .2s var(--ease-out); }
.monitor-job-page .filter-card .advanced-arrow.is-open { transform:rotate(180deg); }
.monitor-job-page .filter-card .advanced-badge { min-width:18px; height:18px; padding:0 5px; margin-left:2px; display:inline-flex; align-items:center; justify-content:center; border-radius:9px; background:var(--brand-600); color:#fff; font-size:12px; font-weight:600; line-height:1; }

/* ===== Form Field ===== */
.monitor-job-page .field { display:flex; flex-direction:column; gap:6px; }
.monitor-job-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.monitor-job-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.monitor-job-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.monitor-job-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.monitor-job-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.monitor-job-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.monitor-job-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.monitor-job-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.monitor-job-page .field .control :deep(.el-select) { width:100%; }
.monitor-job-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.monitor-job-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.monitor-job-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.monitor-job-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.monitor-job-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.monitor-job-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.monitor-job-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.monitor-job-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.monitor-job-page .btn-soft .el-icon { font-size:14px; }
.monitor-job-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.monitor-job-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.monitor-job-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.monitor-job-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.monitor-job-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.monitor-job-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.monitor-job-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.monitor-job-page .table-wrap { overflow-x:auto; }
.monitor-job-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.monitor-job-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.monitor-job-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.monitor-job-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.monitor-job-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.monitor-job-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.monitor-job-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.monitor-job-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.monitor-job-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Badges ===== */
.monitor-job-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.monitor-job-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.monitor-job-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }

/* ===== Pagination ===== */
.monitor-job-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.monitor-job-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.monitor-job-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.monitor-job-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.monitor-job-page .pagination-container :deep(.el-pagination .btn-prev), .monitor-job-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.monitor-job-page .pagination-container :deep(.el-pagination .btn-prev:hover), .monitor-job-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.monitor-job-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .monitor-job-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .monitor-job-page .filter-card .filter-bar { grid-template-columns:1fr; } .monitor-job-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 操作列按钮对齐 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* Dialog 样式由全局 detail-page.scss 中的 .rd-dialog 控制 */

/* ===== cron表达式字段增强 ===== */
.cron-field { display: flex; flex-direction: column; gap: 8px; width: 100%; }
.cron-presets { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
.cron-presets-label { font-size: 12px; color: #6b7280; white-space: nowrap; }
.cron-preset-tag { cursor: pointer; transition: all 0.15s ease; }
.cron-preset-tag:hover { background: #eef2ff; border-color: #c7d2fe; color: #4338ca; }
.cron-hint { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #6b7280; }
.cron-hint .el-icon { font-size: 13px; color: #94a3b8; flex-shrink: 0; }
.cron-next-run { display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 500; color: #059669; background: #ecfdf5; border: 1px solid #a7f3d0; border-radius: 6px; padding: 4px 10px; width: fit-content; }
.cron-next-run .el-icon { font-size: 13px; flex-shrink: 0; }

/* ===== cron 表达式释义 ===== */
.cron-desc { display: flex; align-items: flex-start; gap: 6px; background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 6px; padding: 6px 10px; font-size: 12px; color: #0c4a6e; line-height: 1.6; width: fit-content; }
.cron-desc-icon { color: #0284c7; font-size: 14px; flex-shrink: 0; margin-top: 1px; }

/* ===== 表单字段提示 ===== */
.field-hint { margin-top: 4px; font-size: 12px; color: #6b7280; }
</style>
