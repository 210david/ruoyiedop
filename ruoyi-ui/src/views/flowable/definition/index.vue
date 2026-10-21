<template>
  <div class="app-container flowable-definition-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>流程名称</label>
          <div class="control">
            <el-input v-model="queryParams.modelName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程Key</label>
          <div class="control">
            <el-input v-model="queryParams.modelKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>流程分类</label>
          <div class="control is-select">
            <el-select v-model="queryParams.category" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in flowable_category" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left"></div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_definition_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="流程名称" prop="modelName" min-width="160" show-overflow-tooltip>
            <template #default="scope"><span class="col-strong">{{ scope.row.modelName }}</span></template>
          </el-table-column>
          <el-table-column label="流程Key" prop="modelKey" min-width="140" show-overflow-tooltip>
            <template #default="scope"><span class="col-mono">{{ scope.row.modelKey }}</span></template>
          </el-table-column>
          <el-table-column label="流程分类" prop="category" width="110" align="center">
            <template #default="scope">
              <span class="badge violet">{{ categoryLabel(scope.row.category) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="版本" prop="version" width="150" align="center">
            <template #default="scope">
              <span>v{{ scope.row.version }}</span>
              <span class="badge blue" style="margin-left:6px" v-if="scope.row.activeFlag === '1'"><span class="dot"></span>当前版本</span>
            </template>
          </el-table-column>
          <el-table-column label="版本备注" prop="remark" min-width="120" align="center" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.remark || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="suspended" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="scope.row.suspended ? 'gray' : 'green'">
                <span class="dot"></span>{{ scope.row.suspended ? '已挂起' : '已激活' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="部署时间" prop="deploymentTime" width="170" align="center" />
          <el-table-column label="操作" width="280" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleViewXml(scope.row)" v-hasPermi="['flowable:definition:query']">查看XML</el-button>
                <el-button link type="primary" icon="EditPen" @click="handleUpdateRemark(scope.row)" v-hasPermi="['flowable:definition:state']">备注</el-button>
                <el-button v-if="scope.row.activeFlag !== '1' && !scope.row.suspended" link type="success" icon="Select" @click="handleSetActive(scope.row)" v-hasPermi="['flowable:definition:state']">设为当前</el-button>
                <el-button v-if="!scope.row.suspended" link type="warning" icon="VideoPause" @click="handleChangeState(scope.row, true)" v-hasPermi="['flowable:definition:state']">挂起</el-button>
                <el-button v-else link type="success" icon="VideoPlay" @click="handleChangeState(scope.row, false)" v-hasPermi="['flowable:definition:state']">激活</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== BPMN XML 查看弹窗 ===== -->
    <el-dialog v-model="xmlOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
          </div>
          <span class="rd-detail-header-title">BPMN XML</span>
          <div class="rd-detail-header-sub" v-if="xmlModel.modelName">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ xmlModel.modelName }}（v{{ xmlModel.version }}）</span>
          </div>
        </div>
      </template>
      <pre class="xml-view">{{ xmlContent }}</pre>
      <template #footer>
        <el-button @click="xmlOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowDefinition">
import { listDefinition, getDefinitionXml, changeDefinitionState, updateDefinitionRemark, setActiveDefinition } from '@/api/flowable/flow'
import { ElMessageBox } from 'element-plus'
import { Search, Filter, Document } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { flowable_category } = proxy.useDict('flowable_category')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const xmlOpen = ref(false)
const xmlContent = ref('')
const xmlModel = ref({})

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, modelName: undefined, modelKey: undefined, category: undefined }
})
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.modelName) count++
  if (queryParams.value.modelKey) count++
  if (queryParams.value.category) count++
  return count
})

const defaultColumns = {
  modelName: { label: '流程名称', visible: true },
  modelKey: { label: '流程Key', visible: true },
  category: { label: '流程分类', visible: true },
  version: { label: '版本', visible: true },
  suspended: { label: '状态', visible: true },
  deploymentTime: { label: '部署时间', visible: true }
}
const columns = ref(loadColumnVisibility())
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_definition_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

function categoryLabel(value) {
  const item = flowable_category.value.find(d => d.value == value)
  return item ? item.label : (value || '-')
}

function getList() {
  loading.value = true
  listDefinition(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.modelName = undefined
  queryParams.value.modelKey = undefined
  queryParams.value.category = undefined
  handleQuery()
}

function handleViewXml(row) {
  xmlModel.value = row
  getDefinitionXml(row.modelId).then(res => {
    xmlContent.value = res.data || ''
    xmlOpen.value = true
  })
}

function handleChangeState(row, suspend) {
  const action = suspend ? '挂起' : '激活'
  proxy.$modal.confirm(`确认${action}流程"${row.modelName}"？挂起后该流程不可发起新实例`).then(() => {
    return changeDefinitionState(row.modelId, suspend)
  }).then(() => {
    proxy.$modal.msgSuccess(`已${action}`)
    getList()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError(`${action}失败：` + (e?.message || e)) })
}

/** 编辑版本备注（泛微式：每个版本可填写备注说明） */
function handleUpdateRemark(row) {
  ElMessageBox.prompt('请输入版本备注说明', '版本备注：v' + row.version, {
    inputValue: row.remark || '',
    inputPlaceholder: '如：调整审批人为部门经理',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea'
  }).then(({ value }) => {
    return updateDefinitionRemark(row.modelId, value || '').then(() => {
      proxy.$modal.msgSuccess('版本备注已更新')
      getList()
    }).catch(e => { proxy.$modal.msgError('版本备注更新失败：' + (e?.message || e)) })
  }).catch(() => {})
}

/** 设为当前活动版本（发起流程时优先使用该版本） */
function handleSetActive(row) {
  proxy.$modal.confirm(`确认将版本 v${row.version} 设为当前版本？新发起的流程将优先使用该版本`).then(() => {
    return setActiveDefinition(row.modelId).then(() => {
      proxy.$modal.msgSuccess('已设为当前版本')
      getList()
    }).catch(e => { proxy.$modal.msgError('设为当前版本失败：' + (e?.message || e)) })
  }).catch(() => {})
}

getList()
onActivated(() => { getList() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.xml-view {
  background: var(--ink-900); color: #cbd5e1; padding: 16px; border-radius: 8px;
  font-family: ui-monospace, Menlo, monospace; font-size: 12px; line-height: 1.6;
  max-height: 520px; overflow: auto; margin: 0; white-space: pre-wrap; word-break: break-all;
}
</style>
