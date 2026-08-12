<template>
  <div class="app-container qms-complaint-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>客诉编号</label>
          <div class="control">
            <el-input v-model="queryParams.complaintNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.complaintType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.complaintStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:complaint:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:complaint:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:complaint:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:complaint:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="客诉编号" prop="complaintNo" :width="colWidth('complaintNo', 160)" resizable show-overflow-tooltip />
          <el-table-column label="客户名称" prop="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip />
          <el-table-column label="物料" prop="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip />
          <el-table-column label="投诉日期" prop="complaintDate" :width="colWidth('complaintDate', 120)" resizable align="center"><template #default="scope"><span>{{ parseTime(scope.row.complaintDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="类型" prop="complaintType" :width="colWidth('complaintType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="typeOptions" :value="scope.row.complaintType" /></template></el-table-column>
          <el-table-column label="严重" prop="severity" :width="colWidth('severity', 80)" resizable align="center"><template #default="scope"><span>{{ {1:'严重',2:'一般',3:'轻微'}[scope.row.severity] }}</span></template></el-table-column>
          <el-table-column label="退货金额" prop="returnAmt" :width="colWidth('returnAmt', 110)" resizable align="right" />
          <el-table-column label="索赔金额" prop="claimAmt" :width="colWidth('claimAmt', 110)" resizable align="right" />
          <el-table-column label="状态" prop="complaintStatus" :width="colWidth('complaintStatus', 100)" resizable align="center"><template #default="scope"><dict-tag :options="statusOptions" :value="scope.row.complaintStatus" /></template></el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:complaint:edit']">修改</el-button>
              <el-button v-if="scope.row.complaintStatus !== '3'" link type="success" icon="CircleCheck" @click="handleClose(scope.row)" v-hasPermi="['qms:complaint:close']">关闭</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:complaint:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="complaintRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="客户编码"><el-input v-model="form.customerCode" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物料编码"><el-input v-model="form.materialCode" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="投诉日期"><el-date-picker v-model="form.complaintDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="投诉类型"><el-select v-model="form.complaintType" style="width: 100%"><el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="严重程度"><el-select v-model="form.severity" style="width: 100%"><el-option label="严重" value="1" /><el-option label="一般" value="2" /><el-option label="轻微" value="3" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="退货数量"><el-input-number v-model="form.returnQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="退货金额"><el-input-number v-model="form.returnAmt" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="索赔金额"><el-input-number v-model="form.claimAmt" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>处理信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="缺陷描述"><el-input v-model="form.defectDesc" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
              <el-form-item label="处理描述"><el-input v-model="form.handleDesc" type="textarea" :rows="3" placeholder="8D回复内容" /></el-form-item>
              <el-form-item label="处理结果"><el-select v-model="form.handleResult"><el-option label="退货" value="return" /><el-option label="换货" value="exchange" /><el-option label="赔偿" value="compensate" /><el-option label="关闭" value="close" /></el-select></el-form-item>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsComplaint">
import { listComplaint, getComplaint, addComplaint, updateComplaint, delComplaint, closeComplaint } from '@/api/qms/complaint'
import { useColumnResize } from '@/composables/useColumnResize'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_complaint_index')
const { proxy } = getCurrentInstance()
const { qms_complaint_type: typeOptions, qms_complaint_status: statusOptions } = proxy.useDict('qms_complaint_type', 'qms_complaint_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const open = ref(false)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } })
const { queryParams, form } = toRefs(data)
const rules = { customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }], materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.complaintNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.complaintType) count++
  if (queryParams.value.complaintStatus) count++
  return count
})

function getList() { loading.value = true; listComplaint(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增客诉' }
function handleUpdate(row) {
  const id = row?.complaintId || selectedId.value
  getComplaint(id).then(res => { data.form = res.data; open.value = true; title.value = '修改客诉' })
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.complaintId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['complaintRef'].validate(valid => {
    if (valid) {
      if (form.value.complaintId) { updateComplaint(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addComplaint(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.complaintId ? [row.complaintId] : selectedIds.value
  proxy.$modal.confirm('确认删除？').then(() => delComplaint(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleClose(row) {
  proxy.$modal.confirm('确认关闭客诉 "' + row.complaintNo + '"？').then(() => closeComplaint(row.complaintId)).then(() => { getList(); proxy.$modal.msgSuccess('关闭成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/complaint/export', { ...queryParams.value }, `complaint_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { complaintId: null, customerName: undefined, materialName: undefined, complaintType: 'quality', severity: '2', complaintDate: new Date().toISOString().slice(0,10), handleResult: undefined }; proxy.resetForm('complaintRef') }
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
.qms-complaint-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-complaint-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-complaint-page .filter-card { padding:14px 20px 16px; }
.qms-complaint-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-complaint-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-complaint-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-complaint-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-complaint-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-complaint-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-complaint-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-complaint-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-complaint-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-complaint-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-complaint-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-complaint-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-complaint-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-complaint-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-complaint-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-complaint-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-complaint-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-complaint-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-complaint-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-complaint-page .field .control :deep(.el-select) { width:100%; }
.qms-complaint-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-complaint-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-complaint-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-complaint-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-complaint-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-complaint-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-complaint-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-complaint-page .btn-soft .el-icon { font-size:14px; }
.qms-complaint-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-complaint-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-complaint-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-complaint-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-complaint-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-complaint-page .table-wrap { overflow-x:auto; }
.qms-complaint-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-complaint-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-complaint-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-complaint-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-complaint-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-complaint-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-complaint-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-complaint-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-complaint-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-complaint-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
