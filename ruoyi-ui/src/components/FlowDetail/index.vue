<template>
  <el-dialog :model-value="open" width="1100px" append-to-body draggable class="rd-dialog" @update:model-value="v => emit('update:open', v)" @open="load">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </div>
        <span class="rd-detail-header-title">流程详情</span>
        <div class="rd-detail-header-sub" v-if="detailData.processDefinitionName">
          <div class="rd-detail-header-divider"></div>
          <span class="rd-detail-header-no">{{ detailData.processDefinitionName }}（{{ detailData.instanceId }}）</span>
        </div>
        <slot name="header-extra"></slot>
      </div>
    </template>

    <div class="detail-tabs">
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'form' }" @click="detailTab = 'form'">流程表单</button>
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'diagram' }" @click="detailTab = 'diagram'">流程图</button>
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'status' }" @click="detailTab = 'status'">流程状态</button>
      <button class="detail-tab" :class="{ 'is-active': detailTab === 'resource' }" @click="detailTab = 'resource'">相关资源</button>
    </div>

    <!-- 页签：流程表单 -->
    <div class="rd-page" v-show="detailTab === 'form'">
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title"><span class="rd-card-icon">i</span>{{ detailData.processDefinitionName || '流程表单' }}</div>
        </div>
        <div class="rd-card-body" style="display:block">
          <div class="rd-grid" v-if="formFields.length">
            <div class="rd-item" v-for="f in formFields" :key="f.field">
              <span class="rd-label">{{ f.label }}</span>
              <div class="rd-value">{{ formatValue(f, detailData.variables?.[f.field]) }}</div>
            </div>
          </div>
          <div class="rd-grid" v-else>
            <div class="rd-item" v-for="(v, k) in detailData.variables || {}" :key="k">
              <span class="rd-label">{{ k }}</span><div class="rd-value">{{ v ?? '-' }}</div>
            </div>
            <div class="rd-empty" v-if="!(detailData.variables && Object.keys(detailData.variables).length)">
              <p class="rd-empty-text">该流程未绑定表单且暂无表单数据</p>
            </div>
          </div>
        </div>
      </section>
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title"><span class="rd-card-icon">≡</span>流转意见</div>
        </div>
        <div class="rd-card-body" style="display:block">
          <div class="rd-timeline">
            <div class="rd-timeline-item" v-for="t in historyTasks" :key="t.taskId">
              <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': t.endTime, 'rd-timeline-dot--active': !t.endTime }"></div>
              <div class="rd-timeline-content">
                <div class="rd-timeline-header">
                  <span class="rd-timeline-title">{{ t.taskName }}（{{ t.assigneeName || t.assigneeId || '-' }}）</span>
                  <span class="rd-timeline-time">{{ t.createTime }}</span>
                </div>
                <div class="rd-timeline-comment" v-if="t.comment"><strong>意见：</strong>{{ t.comment }}</div>
              </div>
            </div>
            <div class="rd-empty" v-if="!historyTasks.length"><p class="rd-empty-text">暂无流转意见</p></div>
          </div>
        </div>
      </section>
    </div>

    <!-- 页签：流程图 -->
    <div class="rd-page" v-show="detailTab === 'diagram'">
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title"><span class="rd-card-icon">◈</span>流程图</div>
        </div>
        <div class="rd-card-body" style="display:block">
          <div class="diagram-wrap">
            <img v-if="diagramUrl" :src="diagramUrl" class="diagram-img" alt="流程图" />
            <div v-else class="diagram-empty">流程已结束或暂无流程图</div>
          </div>
        </div>
      </section>
    </div>

    <!-- 页签：流程状态 -->
    <div class="rd-page" v-show="detailTab === 'status'">
      <div class="status-cards">
        <div class="status-card"><div class="sc-num">{{ statusStats.total }}</div><div class="sc-label">总人次</div></div>
        <div class="status-card"><div class="sc-num sc-green">{{ statusStats.finished }}</div><div class="sc-label">已提交</div></div>
        <div class="status-card"><div class="sc-num sc-amber">{{ statusStats.pending }}</div><div class="sc-label">未提交</div></div>
        <div class="status-card"><div class="sc-num">{{ statusStats.nodes }}</div><div class="sc-label">节点数</div></div>
      </div>
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title"><span class="rd-card-icon">≡</span>流转明细</div>
        </div>
        <div class="rd-card-body">
          <el-table :data="historyTasks" border size="small">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="节点" prop="taskName" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作人" prop="assigneeName" width="120">
              <template #default="s">{{ s.row.assigneeName || s.row.assigneeId || '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="s">
                <span v-if="s.row.endTime" class="badge" :class="outcomeClass(s.row.outcome)"><span class="dot"></span>{{ outcomeLabel(s.row.outcome) || '已办理' }}</span>
                <span v-else class="badge amber"><span class="dot"></span>待办理</span>
              </template>
            </el-table-column>
            <el-table-column label="接收时间" prop="createTime" width="165" align="center" />
            <el-table-column label="操作时间" prop="endTime" width="165" align="center">
              <template #default="s">{{ s.row.endTime || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作耗时" width="110" align="center">
              <template #default="s">{{ s.row.endTime ? durationText(s.row.createTime, s.row.endTime) : '-' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </div>

    <!-- 页签：相关资源 -->
    <div class="rd-page" v-show="detailTab === 'resource'">
      <section class="rd-card">
        <div class="rd-card-header">
          <div class="rd-card-title"><span class="rd-card-icon">⊞</span>相关资源</div>
        </div>
        <div class="rd-card-body">
          <el-table :data="[]" border size="small">
            <el-table-column label="资源名称" min-width="200" />
            <el-table-column label="创建人" width="120" />
            <el-table-column label="创建时间" width="170" align="center" />
          </el-table>
          <div class="resource-empty">流程过程中上传的附件将在后续版本中自动汇总展示</div>
        </div>
      </section>
    </div>

    <template #footer>
      <el-button @click="emit('update:open', false)">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="FlowDetail">
import request from '@/utils/request'

const props = defineProps({
  open: { type: Boolean, default: false },
  instanceId: { type: String, default: '' },
  /* 附件列过滤：业务表单生成的数据表中，哪些列属于附件（由调用方按流程传） */
  attachmentFields: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:open'])

const detailData = ref({})
const historyTasks = ref([])
const diagramUrl = ref('')
const detailTab = ref('form')
const formFields = ref([])

const statusStats = computed(() => {
  const tasks = historyTasks.value
  return {
    total: tasks.length,
    finished: tasks.filter(t => t.endTime).length,
    pending: tasks.filter(t => !t.endTime).length,
    nodes: new Set(tasks.map(t => t.taskName)).size
  }
})

function outcomeClass(outcome) {
  const map = { approved: 'green', rejected: 'red', delegate: 'blue' }
  return map[outcome] || 'gray'
}
function outcomeLabel(outcome) {
  const map = { approved: '同意', rejected: '驳回', delegate: '转办' }
  return map[outcome] || ''
}
function durationText(begin, end) {
  try {
    const ms = new Date(end.replace(/-/g, '/')) - new Date(begin.replace(/-/g, '/'))
    if (isNaN(ms) || ms < 0) return '-'
    const min = Math.floor(ms / 60000)
    if (min < 1) return '不足1分钟'
    if (min < 60) return min + '分钟'
    const h = Math.floor(min / 60)
    return h + '小时' + (min % 60) + '分钟'
  } catch (e) { return '-' }
}
/* select 选项转中文 */
function formatValue(field, val) {
  if (val === undefined || val === null || val === '') return '-'
  if (field.type === 'select' && Array.isArray(field.options)) {
    return field.options.includes(val) ? val : val
  }
  return val
}

async function loadFormFields(processKey) {
  formFields.value = []
  if (!processKey) return
  try {
    const res = await request({ url: '/flowable/form/byProcess/' + processKey, method: 'get' })
    const form = res.data
    if (form && form.fieldsJson) {
      formFields.value = JSON.parse(form.fieldsJson) || []
    }
  } catch (e) {}
}

function load() {
  if (!props.instanceId) return
  detailData.value = {}
  historyTasks.value = []
  diagramUrl.value = ''
  formFields.value = []
  detailTab.value = 'form'
  request({ url: '/flowable/process/' + props.instanceId, method: 'get' }).then(res => {
    detailData.value = res.data || {}
    loadFormFields(detailData.value.processDefinitionKey)
  })
  request({ url: '/flowable/process/history/' + props.instanceId, method: 'get' }).then(res => {
    historyTasks.value = res.data || []
  })
  request({ url: '/flowable/process/diagram/' + props.instanceId, method: 'get', responseType: 'blob' }).then(res => {
    diagramUrl.value = URL.createObjectURL(new Blob([res], { type: 'image/png' }))
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.detail-tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--rd-border, #e5e7eb);
  margin-bottom: 12px;
  .detail-tab {
    background: none;
    border: none;
    padding: 10px 16px;
    font-size: 14px;
    color: var(--rd-muted, #6b7280);
    cursor: pointer;
    border-bottom: 2px solid transparent;
    transition: color .15s;
    &:hover { color: var(--el-color-primary); }
    &.is-active {
      color: var(--el-color-primary);
      font-weight: 600;
      border-bottom-color: var(--el-color-primary);
    }
  }
}
.status-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 12px;
  .status-card {
    background: #f8fafc;
    border: 1px solid var(--rd-border, #e5e7eb);
    border-radius: 10px;
    text-align: center;
    padding: 12px 0;
    .sc-num { font-size: 22px; font-weight: 700; color: var(--ink-900, #0f172a); }
    .sc-num.sc-green { color: #10b981; }
    .sc-num.sc-amber { color: #f59e0b; }
    .sc-label { font-size: 12px; color: var(--rd-muted, #6b7280); margin-top: 2px; }
  }
}
.resource-empty {
  text-align: center;
  color: var(--rd-muted, #9ca3af);
  font-size: 13px;
  padding: 14px 0 4px;
}
:deep(.badge.amber) {
  background: #fef3c7;
  color: #b45309;
}
.diagram-wrap { text-align: center; min-height: 120px; }
.diagram-img { max-width: 100%; }
.diagram-empty { color: var(--rd-muted, #9ca3af); font-size: 13px; padding: 24px 0; }
</style>
