<template>
  <div class="app-container qms-workshop-page">
    <el-row :gutter="20">
      <!-- 左侧树 -->
      <el-col :span="4" :xs="24">
        <div class="surface tree-panel">
          <div class="tree-head">
            <el-input v-model="deptName" placeholder="请输入节点名称" clearable size="small" prefix-icon="Search" />
          </div>
          <div class="tree-body">
            <el-tree :data="workshopTreeOptions" :props="defaultProps" :expand-on-click-node="false" :filter-node-method="filterNode" ref="deptTreeRef" default-expand-all highlight-current @node-click="handleNodeClick" />
          </div>
        </div>
      </el-col>
      <!-- 右侧表格 -->
      <el-col :span="20" :xs="24">
        <!-- ===== Filter Card ===== -->
        <div class="surface filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>节点编码</label>
              <div class="control">
                <el-input v-model="queryParams.workshopCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>节点名称</label>
              <div class="control">
                <el-input v-model="queryParams.workshopName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
              </div>
            </div>
            <div class="field">
              <label>层级类型</label>
              <div class="control is-select">
                <el-select v-model="queryParams.nodeType" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in qms_workshop_node_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </div>
            </div>
          </div>
          <div class="filter-actions">
            <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
            <div class="filter-buttons">
              <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </div>
          </div>
        </div>

        <!-- ===== Table Section ===== -->
        <div class="surface">
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:workshop:add']">新增</el-button>
              <button type="button" class="btn-soft is-outline" @click="toggleExpandAll">
                <el-icon><Sort /></el-icon> 展开/折叠
              </button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" v-if="refreshTable" v-loading="loading" :data="workshopList" row-key="workshopId" :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border @header-dragend="onHeaderDragEnd" class="app-table">
              <el-table-column prop="workshopName" label="节点名称" :width="colWidth('workshopName', 220)" resizable />
              <el-table-column prop="workshopCode" label="节点编码" :width="colWidth('workshopCode', 160)" resizable align="center" />
              <el-table-column label="层级类型" align="center" :width="colWidth('nodeType', 120)" resizable>
                <template #default="scope">
                  <dict-tag :options="qms_workshop_node_type" :value="scope.row.nodeType" />
                </template>
              </el-table-column>
              <el-table-column prop="orderNum" label="排序" align="center" :width="colWidth('orderNum', 80)" resizable />
              <el-table-column label="状态" align="center" :width="colWidth('status', 100)" resizable>
                <template #default="scope">
                  <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
                </template>
              </el-table-column>
              <el-table-column label="创建时间" align="center" prop="createTime" :width="colWidth('createTime', 180)" resizable>
                <template #default="scope">
                  <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['qms:workshop:add']">新增</el-button>
                  <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:workshop:edit']">修改</el-button>
                  <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:workshop:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 添加或修改对话框 -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="workshopRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="上级节点" prop="parentId" v-if="form.parentId !== 0">
                <el-tree-select v-model="form.parentId" :data="workshopTreeOptions" :props="{ value: 'workshopId', label: 'workshopName', children: 'children' }" value-key="workshopId" placeholder="选择上级节点" check-strictly style="width: 100%" />
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="节点编码" prop="workshopCode">
                    <el-input v-model="form.workshopCode" placeholder="请输入节点编码" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="节点名称" prop="workshopName">
                    <el-input v-model="form.workshopName" placeholder="请输入节点名称" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></span>配置信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="层级类型" prop="nodeType">
                    <el-select v-model="form.nodeType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="dict in qms_workshop_node_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="排序号" prop="orderNum">
                    <el-input-number v-model="form.orderNum" controls-position="right" :min="0" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
              </el-form-item>
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
  </div>
</template>

<script setup name="QmsWorkshop">
import { listWorkshop, workshopTree, getWorkshop, addWorkshop, updateWorkshop, delWorkshop } from '@/api/qms/workshop'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_workshop_index')
const { sys_normal_disable, qms_workshop_node_type } = proxy.useDict('sys_normal_disable', 'qms_workshop_node_type')

const workshopList = ref([])
const workshopTreeOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref('')
const isExpandAll = ref(true)
const refreshTable = ref(true)
const deptName = ref(undefined)
const defaultProps = { children: 'children', label: 'workshopName' }

const data = reactive({
  form: {},
  queryParams: { workshopCode: undefined, workshopName: undefined, nodeType: undefined, status: undefined },
  rules: {
    workshopCode: [{ required: true, message: '节点编码不能为空', trigger: 'blur' }],
    workshopName: [{ required: true, message: '节点名称不能为空', trigger: 'blur' }],
    nodeType: [{ required: true, message: '层级类型不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.workshopCode) count++
  if (queryParams.value.workshopName) count++
  if (queryParams.value.nodeType) count++
  if (queryParams.value.status) count++
  return count
})

/** 查询列表 */
function getList() {
  loading.value = true
  workshopTree(queryParams.value).then(response => {
    workshopList.value = response.data
    loading.value = false
    applySavedWidths()
  })
}

/** 查询树 */
function getTreeselect() {
  workshopTree().then(response => {
    workshopTreeOptions.value = [{ workshopId: 0, workshopName: '顶级节点', children: response.data }]
  })
}

/** 筛选节点 */
function filterNode(value, data) {
  if (!value) return true
  return data.workshopName.indexOf(value) !== -1
}

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.workshopName = undefined
  queryParams.value.parentId = data.workshopId
  getList()
}

/** 搜索按钮 */
function handleQuery() {
  getList()
}

/** 重置按钮 */
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.parentId = undefined
  handleQuery()
}

/** 新增按钮 */
function handleAdd(row) {
  reset()
  getTreeselect()
  if (row != null && row.workshopId) {
    form.value.parentId = row.workshopId
  } else {
    form.value.parentId = 0
  }
  open.value = true
  title.value = '添加节点'
}

/** 修改按钮 */
function handleUpdate(row) {
  reset()
  getTreeselect()
  getWorkshop(row.workshopId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改节点'
  })
}

/** 提交 */
function submitForm() {
  proxy.$refs['workshopRef'].validate(valid => {
    if (valid) {
      if (form.value.workshopId != null) {
        updateWorkshop(form.value).then(response => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
          getTreeselect()
        })
      } else {
        addWorkshop(form.value).then(response => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
          getTreeselect()
        })
      }
    }
  })
}

/** 删除按钮 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除节点"' + row.workshopName + '"？').then(function () {
    return delWorkshop(row.workshopId)
  }).then(() => {
    getList()
    getTreeselect()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 展开/折叠 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => { refreshTable.value = true })
}

/** 表单重置 */
function reset() {
  form.value = {
    workshopId: null,
    parentId: 0,
    workshopCode: undefined,
    workshopName: undefined,
    nodeType: undefined,
    orderNum: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('workshopRef')
}

/** 取消 */
function cancel() {
  open.value = false
  reset()
}

watch(deptName, val => {
  proxy.$refs['deptTreeRef'].filter(val)
})

getList()
getTreeselect()
</script>

<style scoped>
.qms-workshop-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-workshop-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-workshop-page .tree-panel { position: sticky; top: 10px; }
.qms-workshop-page .tree-panel .tree-head { padding:12px 16px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-workshop-page .tree-panel .tree-body { padding:12px 16px; max-height: calc(100vh - 200px); overflow-y: auto; }
.qms-workshop-page .filter-card { padding:14px 20px 16px; }
.qms-workshop-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-workshop-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-workshop-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-workshop-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-workshop-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-workshop-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-workshop-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-workshop-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-workshop-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-workshop-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-workshop-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-workshop-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-workshop-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-workshop-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-workshop-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-workshop-page .field .control :deep(.el-select) { width:100%; }
.qms-workshop-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-workshop-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-workshop-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-workshop-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-workshop-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-workshop-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-workshop-page .btn-soft .el-icon { font-size:14px; }
.qms-workshop-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-workshop-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-workshop-page .table-wrap { overflow-x:auto; }
.qms-workshop-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-workshop-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-workshop-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-workshop-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-workshop-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-workshop-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
@media (max-width:1100px) { .qms-workshop-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-workshop-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-workshop-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
