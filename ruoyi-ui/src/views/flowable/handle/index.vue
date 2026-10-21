<template>
  <div class="app-container flowable-handle-page">
    <!-- 顶部：第一行标题 + 操作按钮；第二行页签（对齐发起页与开发规范第12章） -->
    <div class="hd-header">
      <div class="hd-titlebar">
        <div class="hd-title">
          <span class="hd-glyph"></span>
          <span class="hd-name">{{ task.title || task.processDefinitionName || '任务办理' }}</span>
          <span class="hd-ver" v-if="task.taskName">{{ task.taskName }}</span>
          <span class="hd-form" v-if="task.processDefinitionName">
            <el-icon><Document /></el-icon>{{ task.processDefinitionName }}
          </span>
        </div>
        <div class="hd-actions">
          <el-button type="primary" icon="Check" :loading="submitting" @click="submitApprove('approved')"
            v-hasPermi="['flowable:task:complete']">同 意</el-button>
          <el-button type="danger" icon="CloseBold" :loading="submitting" @click="submitApprove('rejected')"
            v-hasPermi="['flowable:task:complete']">驳 回</el-button>
        </div>
      </div>
      <div class="hd-tabs">
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'form' }" @click="activeTab = 'form'">流程表单</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'diagram' }" @click="switchTab('diagram')">流程图</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'status' }" @click="switchTab('status')">流程状态</button>
        <button class="hd-tab" :class="{ 'is-active': activeTab === 'resource' }" @click="switchTab('resource')">相关资源</button>
      </div>
    </div>

    <div class="hd-body" v-loading="loading">
      <!-- ===== 页签：流程表单 ===== -->
      <div class="hd-pane" v-show="activeTab === 'form'">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">i</span>任务信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">流程标题</span><div class="rd-value">{{ task.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">办理环节</span><div class="rd-value">{{ task.taskName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ task.startUserName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">到达时间</span><div class="rd-value">{{ task.createTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">流程编号</span><div class="rd-value col-mono">{{ task.instanceId || '-' }}</div></div>
            </div>
          </div>
        </section>

        <section class="rd-card" v-if="formFields.length">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">▤</span>申请表单</div>
            <span class="hd-edit-tip" v-if="hasEditFields">标亮字段可修改，修改随本次办理一并保存</span>
          </div>
          <div class="rd-card-body" style="display:block">
            <!-- 与「流程发起」页一致：按表单设计器布局（网格布局）渲染后台 HTML 表单；无布局时回退字段列表 -->
            <div class="hd-form-wrap">
              <FormGridRender v-if="gridLayout" :grid="gridLayout" :fields="formFields" :values="formData" />
              <el-form v-else :model="formData" label-width="130px" size="default" class="hd-form">
                <el-row :gutter="0">
                  <el-col v-for="f in visibleFields" :key="f.field" :span="columnSpan(f)">
                    <el-form-item :label="f.label">
                      <dyn-field v-if="f._perm === 'edit'" :f="f" v-model="formData[f.field]" />
                      <span v-else class="hd-ro">{{ displayValue(formData[f.field]) }}</span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </div>
        </section>
        <section class="rd-card" v-else-if="!loading">
          <div class="rd-card-body" style="display:block">
            <div class="rd-empty"><p class="rd-empty-text">该节点未绑定业务表单，可直接在下方填写签字意见后办理</p></div>
          </div>
        </section>

        <!-- 签字意见（对齐泛微：签字意见紧邻表单下方） -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">✎</span>签字意见</div>
          </div>
          <div class="rd-card-body">
            <el-input v-model="comment" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入签字意见（同意/驳回时一并写入流转意见）" />
          </div>
        </section>

        <!-- 流转意见（与其他办理人可见的历史意见） -->
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon">≡</span>流转意见</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="(t, i) in historyTasks" :key="i">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': t.endTime, 'rd-timeline-dot--active': !t.endTime }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ t.taskName }}（{{ t.assigneeName || t.assigneeId || '-' }}）</span>
                    <span class="rd-timeline-time">{{ t.endTime || t.createTime }}</span>
                  </div>
                  <div class="rd-timeline-comment" v-if="t.comment"><strong>意见：</strong>{{ t.comment }}</div>
                </div>
              </div>
              <div class="rd-empty" v-if="!historyTasks.length"><p class="rd-empty-text">暂无流转意见</p></div>
            </div>
          </div>
        </section>
      </div>

      <!-- ===== 页签：流程图 ===== -->
      <div class="hd-pane" v-show="activeTab === 'diagram'">
        <section class="rd-card">
          <div class="rd-card-header hd-legend-only">
            <div class="hd-legend">
              <span class="legend-item"><span class="legend-swatch swatch-current-node"></span>当前停留节点</span>
              <span class="legend-item"><span class="legend-swatch swatch-highlight"></span>已流转节点/路径</span>
              <span class="legend-item"><span class="legend-swatch swatch-default"></span>未流转节点/路径</span>
              <span class="legend-item"><span class="legend-swatch swatch-end-event"></span>结束事件（红色圆心）</span>
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="hd-diagram" v-loading="diagramLoading">
              <img v-if="diagramUrl" :src="diagramUrl" class="hd-diagram-img" alt="流程图" />
              <div v-else class="hd-diagram-empty">{{ diagramLoading ? '流程图加载中…' : diagramEmptyText }}</div>
            </div>
          </div>
        </section>
      </div>

      <!-- ===== 页签：流程状态 ===== -->
      <div class="hd-pane" v-show="activeTab === 'status'">
        <div class="hd-status-cards">
          <div class="hd-status-card"><div class="sc-num">{{ statusStats.total }}</div><div class="sc-label">总人次</div></div>
          <div class="hd-status-card"><div class="sc-num sc-green">{{ statusStats.finished }}</div><div class="sc-label">已办理</div></div>
          <div class="hd-status-card"><div class="sc-num sc-amber">{{ statusStats.pending }}</div><div class="sc-label">待办理</div></div>
          <div class="hd-status-card"><div class="sc-num">{{ statusStats.nodes }}</div><div class="sc-label">节点数</div></div>
        </div>
        <section class="rd-card">
          <div class="rd-card-body" style="display:block">
            <div class="table-wrap">
              <el-table :data="historyTasks" border size="small" class="app-table">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="节点" prop="taskName" min-width="120" show-overflow-tooltip />
                <el-table-column label="操作人" width="120">
                  <template #default="s">{{ s.row.assigneeName || s.row.assigneeId || '-' }}</template>
                </el-table-column>
                <el-table-column label="状态" width="110" align="center">
                  <template #default="s">
                    <span v-if="s.row.endTime" class="badge" :class="outcomeClass(s.row.outcome)"><span class="dot"></span>{{ outcomeLabel(s.row.outcome) || '已办理' }}</span>
                    <span v-else class="badge amber"><span class="dot"></span>待办理</span>
                  </template>
                </el-table-column>
                <el-table-column label="接收时间" prop="createTime" width="165" align="center" />
                <el-table-column label="操作时间" width="165" align="center">
                  <template #default="s">{{ s.row.endTime || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作耗时" width="110" align="center">
                  <template #default="s">{{ s.row.endTime ? durationText(s.row.createTime, s.row.endTime) : '-' }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </section>
      </div>

      <!-- ===== 页签：相关资源 ===== -->
      <div class="hd-pane" v-show="activeTab === 'resource'">
        <section class="rd-card">
          <div class="rd-card-body" style="display:block">
            <div class="table-wrap">
              <el-table :data="[]" border size="small" class="app-table">
                <el-table-column label="资源名称" min-width="200" />
                <el-table-column label="创建人" width="120" />
                <el-table-column label="创建时间" width="170" align="center" />
              </el-table>
            </div>
            <div class="hd-resource-empty">流程过程中上传的附件将在后续版本中自动汇总展示</div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup name="FlowTaskHandle">
import request from '@/utils/request'
import { completeTask } from '@/api/flowable/flow'
import { DynField } from './dyn-field'
import { normalizeHandleField, handleDisplayValue } from './field-norm'
import FormGridRender from '@/components/FormGridRender.vue'
import { parseLayout } from '@/views/flowable/form/designer/converters'
import { useRoute, useRouter } from 'vue-router'
import { Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

/** URL 参数：/flow/task/handle?taskId=xxx&taskName=yyy&title=zzz&instanceId=iii&defName=ddd */
const task = ref({
  taskId: route.query.taskId || '',
  taskName: route.query.taskName || '',
  title: route.query.title || '',
  instanceId: route.query.instanceId || '',
  processDefinitionName: route.query.defName || '',
  startUserName: route.query.startUserName || '',
  createTime: route.query.createTime || ''
})

const loading = ref(true)
const submitting = ref(false)
const activeTab = ref('form')
const comment = ref('')

/** 表单字段（节点字段权限：edit 可编辑 / readonly 只读 / hidden 不渲染） */
const formFields = ref([])
const formData = ref({})

const visibleFields = computed(() => formFields.value.filter(f => f._perm !== 'hidden'))
const hasEditFields = computed(() => formFields.value.some(f => f._perm === 'edit'))
function displayValue(v) { return handleDisplayValue(v) }

/** 表单设计器布局（grid 模式 → 与「流程发起」页同一渲染口径，展示后台 HTML 表单） */
const gridLayout = ref(null)

/** 流转记录 / 流程图（页签数据按需加载） */
const historyTasks = ref([])
const diagramUrl = ref('')
const diagramLoading = ref(false)
const historyLoaded = ref(false)
const diagramLoaded = ref(false)

const statusStats = computed(() => {
  const tasks = historyTasks.value
  return {
    total: tasks.length,
    finished: tasks.filter(t => t.endTime).length,
    pending: tasks.filter(t => !t.endTime).length,
    nodes: new Set(tasks.map(t => t.taskName)).size
  }
})

const diagramEmptyText = computed(() => {
  if (!task.value.instanceId) return '暂无流程图（缺少流程实例标识）'
  return '暂无流程图（该流程定义未生成流程图资源）'
})

function outcomeClass(outcome) {
  return { approved: 'green', rejected: 'red', delegate: 'blue', start: 'violet', end: 'green', canceled: 'gray' }[outcome] || 'gray'
}
function outcomeLabel(outcome) {
  return { approved: '同意', rejected: '驳回', delegate: '转办', start: '发起', end: '结束', canceled: '已撤销' }[outcome] || ''
}
function durationText(begin, end) {
  try {
    const ms = new Date(String(end).replace(/-/g, '/')) - new Date(String(begin).replace(/-/g, '/'))
    if (isNaN(ms) || ms < 0) return '-'
    const min = Math.floor(ms / 60000)
    if (min < 1) return '不足1分钟'
    if (min < 60) return min + '分钟'
    return Math.floor(min / 60) + '小时' + (min % 60) + '分钟'
  } catch (e) { return '-' }
}

/** 加载节点字段权限 + 绑定表单 + 当前流程变量值 */
async function loadTaskForm() {
  if (!task.value.taskId) { loading.value = false; return }
  try {
    const res = await request({ url: '/flowable/task/fieldPerms/' + task.value.taskId, method: 'get' })
    const d = res.data || {}
    let perms = {}
    try { perms = JSON.parse(d.fieldPerms || '{}') || {} } catch (e) {}
    let fields = []
    try { fields = JSON.parse(d.fieldsJson || '[]') || [] } catch (e) {}
    const variables = d.variables || {}
    formFields.value = fields.map(f => normalizeHandleField(f, perms[f.field] || 'readonly'))
    const data = {}
    for (const f of formFields.value) data[f.field] = variables[f.field]
    formData.value = data
    // 与「流程发起」页统一：解析表单设计器布局，grid 模式用 FormGridRender 还原后台 HTML 表单
    const parsed = parseLayout(d.layoutJson)
    gridLayout.value = parsed.mode === 'grid' && parsed.grid ? parsed.grid : null
  } catch (e) {
    formFields.value = []
    formData.value = {}
    gridLayout.value = null
  } finally {
    loading.value = false
  }
}

/** 加载流转记录 */
function loadHistory() {
  if (historyLoaded.value || !task.value.instanceId) return
  historyLoaded.value = true
  request({ url: '/flowable/process/history/' + task.value.instanceId, method: 'get' }).then(res => {
    historyTasks.value = res.data || []
  }).catch(() => { historyLoaded.value = false })
}

/** 加载流程图 */
function loadDiagram() {
  if (diagramLoaded.value || !task.value.instanceId) return
  diagramLoaded.value = true
  diagramLoading.value = true
  request({ url: '/flowable/process/diagram/' + task.value.instanceId, method: 'get', responseType: 'blob' }).then(res => {
    diagramUrl.value = URL.createObjectURL(new Blob([res], { type: 'image/png' }))
  }).catch(() => { diagramUrl.value = '' }).finally(() => { diagramLoading.value = false })
}

/** 页签切换：流程图/流程状态按需加载，避免首屏多余请求 */
function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'diagram') loadDiagram()
  if (tab === 'status') loadHistory()
}

/** 提交办理（同意/驳回）：可编辑字段值随办理一并提交 */
function submitApprove(outcome) {
  if (!task.value.taskId) { proxy.$modal.msgWarning('缺少任务标识，无法办理'); return }
  proxy.$modal.confirm(outcome === 'approved' ? '确认同意该任务？' : '确认驳回该任务？').then(() => {
    submitting.value = true
    const variables = {}
    for (const f of formFields.value) {
      if (f._perm === 'edit') variables[f.field] = formData.value[f.field]
    }
    return completeTask({
      taskId: task.value.taskId,
      comment: comment.value,
      outcome,
      variables: Object.keys(variables).length ? variables : undefined
    })
  }).then(() => {
    proxy.$modal.msgSuccess(outcome === 'approved' ? '已同意' : '已驳回')
  }).catch(e => {
    if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('办理失败：' + (e?.message || e))
  }).finally(() => { submitting.value = false })
}

loadTaskForm()
loadHistory()
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

/* ===== 顶部：标题行 + 页签行（与发起页 launch-header 同一口径） ===== */
.hd-header { padding: 12px 20px 0; background: #fff; }
.hd-titlebar { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 2px 0 10px; }
.hd-title { display: flex; align-items: center; gap: 10px; min-width: 0; }
.hd-glyph { width: 4px; height: 18px; background: var(--brand-600, #4f46e5); border-radius: 2px; flex: none; }
.hd-name { font-size: 16px; font-weight: 600; color: var(--ink-900, #0f172a); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.hd-ver {
  font-size: 12px; font-weight: 600; padding: 1px 8px; border-radius: 999px;
  background: var(--brand-50, #eef2ff); color: var(--brand-700, #4338ca); flex: none;
}
.hd-form {
  display: inline-flex; align-items: center; gap: 5px; font-size: 13px; color: var(--ink-500, #64748b);
  padding-left: 10px; border-left: 1px solid var(--ink-200, #e2e8f0); white-space: nowrap;
}
.hd-actions { display: flex; align-items: center; gap: 8px; flex: none; }

/* 页签行：下划线风格（对齐开发规范第12章 详情页签） */
.hd-tabs { display: flex; align-items: center; gap: 4px; border-bottom: 1px solid var(--ink-200, #e2e8f0); }
.hd-tab {
  position: relative; border: none; background: transparent; cursor: pointer;
  padding: 9px 16px; font-size: 14px; font-weight: 600; color: var(--ink-500, #64748b);
  transition: color .15s cubic-bezier(.16, .84, .44, 1);
}
.hd-tab:hover { color: var(--brand-600, #4f46e5); }
.hd-tab.is-active { color: var(--brand-600, #4f46e5); }
.hd-tab.is-active::after {
  content: ''; position: absolute; left: 12px; right: 12px; bottom: -1px; height: 2px;
  background: var(--brand-600, #4f46e5); border-radius: 2px;
}

/* ===== 内容区 ===== */
.hd-body { padding: 14px 20px 28px; background: #f8fafc; min-height: calc(100vh - 105px); }
.hd-pane { width: 100%; }
.hd-pane :deep(.rd-page) { max-width: none; }

/* 申请表单：只读值（与审批页口径一致） */
.hd-form :deep(.el-form-item__label) { font-size: 14px; color: var(--ink-700, #334155); }
.hd-ro { display: block; color: var(--ink-900, #0f172a); font-size: 14px; line-height: 1.6; word-break: break-all; }
.hd-edit-tip { margin-left: auto; font-size: 12px; font-weight: 400; color: var(--ink-400, #94a3b8); }

/* 流程图 */
.hd-diagram {
  display: flex; align-items: center; justify-content: center; padding: 12px;
  background: var(--ink-50, #f8fafc); border: 1px solid var(--ink-200, #e2e8f0);
  border-radius: var(--r-md, 10px); min-height: 320px; overflow: auto;
}
.hd-diagram-img { max-width: 100%; height: auto; }
.hd-diagram-empty { color: var(--ink-400, #94a3b8); font-size: 13px; }
.hd-legend-only { background: none; border-bottom: none; justify-content: flex-end; cursor: default; padding: 6px 16px 0; }
.hd-legend { display: flex; align-items: center; gap: 16px; }
.hd-legend .legend-item { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 400; color: var(--ink-500, #64748b); }
.hd-legend .legend-swatch { width: 14px; height: 10px; border-radius: 2px; border: 1px solid rgba(15, 23, 42, .12); flex-shrink: 0; }
.hd-legend .swatch-highlight { background: #10b981; }
.hd-legend .swatch-default { background: #fff; }
.hd-legend .swatch-current-node { background: #fff; border: 2px solid #ef4444; }
.hd-legend .swatch-end-event {
  background: #fff; border: 2px solid #dc2626; border-radius: 50%; position: relative;
}
.hd-legend .swatch-end-event::after {
  content: ''; position: absolute; inset: 2px; margin: auto; width: 5px; height: 5px; border-radius: 50%; background: #dc2626;
}

/* 流程状态统计卡 */
.hd-status-cards { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 14px; }
.hd-status-card {
  background: #fff; border: 1px solid var(--ink-200, #e2e8f0); border-radius: var(--r-lg, 14px);
  padding: 14px; text-align: center; box-shadow: var(--shadow-card, 0 1px 2px rgba(15, 23, 42, .04));
}
.hd-status-card .sc-num { font-size: 22px; font-weight: 700; color: var(--ink-900, #0f172a); line-height: 1.2; }
.hd-status-card .sc-num.sc-green { color: #059669; }
.hd-status-card .sc-num.sc-amber { color: #d97706; }
.hd-status-card .sc-label { margin-top: 4px; font-size: 12px; color: var(--ink-500, #64748b); }

.hd-resource-empty { margin-top: 10px; text-align: center; color: var(--ink-400, #94a3b8); font-size: 13px; }

@media (max-width: 1100px) {
  .hd-status-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .hd-titlebar { flex-wrap: wrap; }
}
</style>
