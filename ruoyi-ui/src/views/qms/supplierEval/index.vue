<template>
  <div class="app-container qms-seval-page">
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
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>周期</label>
          <div class="control">
            <el-input v-model="queryParams.evalPeriod" placeholder="如2026-08" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.grade" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in gradeOptions" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:supplierEval:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:supplierEval:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:supplierEval:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:supplierEval:export']">
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
          <el-table-column label="供应商名称" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
          <el-table-column label="评价周期" prop="evalPeriod" :width="colWidth('evalPeriod', 110)" resizable align="center" />
          <el-table-column label="来料批次" prop="incomingTotal" :width="colWidth('incomingTotal', 100)" resizable align="center" />
          <el-table-column label="合格批次" prop="incomingPass" :width="colWidth('incomingPass', 100)" resizable align="center" />
          <el-table-column label="合格率%" prop="passRate" :width="colWidth('passRate', 100)" resizable align="center" />
          <el-table-column label="PPM" prop="ppm" :width="colWidth('ppm', 90)" resizable align="center" />
          <el-table-column label="质量分" prop="qualityScore" :width="colWidth('qualityScore', 90)" resizable align="center" />
          <el-table-column label="交期分" prop="deliveryScore" :width="colWidth('deliveryScore', 90)" resizable align="center" />
          <el-table-column label="服务分" prop="serviceScore" :width="colWidth('serviceScore', 90)" resizable align="center" />
          <el-table-column label="综合分" prop="totalScore" :width="colWidth('totalScore', 90)" resizable align="center" />
          <el-table-column label="等级" prop="grade" :width="colWidth('grade', 80)" resizable align="center"><template #default="scope"><dict-tag :options="gradeOptions" :value="scope.row.grade" /></template></el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:supplierEval:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:supplierEval:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="evalRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商名称" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="评价周期" prop="evalPeriod"><el-input v-model="form.evalPeriod" placeholder="如2026-08" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="评价类型"><el-select v-model="form.evalType" style="width: 100%"><el-option label="月度" value="monthly" /><el-option label="季度" value="quarterly" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="来料批次"><el-input-number v-model="form.incomingTotal" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="合格批次"><el-input-number v-model="form.incomingPass" :min="0" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="质量分"><el-input-number v-model="form.qualityScore" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="交期分"><el-input-number v-model="form.deliveryScore" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="服务分"><el-input-number v-model="form.serviceScore" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsSupplierEval">
import { listSupplierEval, getSupplierEval, addSupplierEval, updateSupplierEval, delSupplierEval } from '@/api/qms/supplierEval'
import { useColumnResize } from '@/composables/useColumnResize'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_supplierEval_index')
const { proxy } = getCurrentInstance()
const { qms_supplier_grade: gradeOptions } = proxy.useDict('qms_supplier_grade')

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
const rules = { supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }], evalPeriod: [{ required: true, message: '请输入评价周期', trigger: 'blur' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierName) count++
  if (queryParams.value.evalPeriod) count++
  if (queryParams.value.grade) count++
  return count
})

function getList() { loading.value = true; listSupplierEval(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增供应商评价' }
function handleUpdate(row) {
  const id = row?.evalId || selectedId.value
  getSupplierEval(id).then(res => { data.form = res.data; open.value = true; title.value = '修改供应商评价' })
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.evalId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['evalRef'].validate(valid => {
    if (valid) {
      if (form.value.evalId) { updateSupplierEval(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addSupplierEval(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.evalId ? [row.evalId] : selectedIds.value
  proxy.$modal.confirm('确认删除选中的数据？').then(() => delSupplierEval(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/supplierEval/export', { ...queryParams.value }, `supplierEval_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { evalId: null, supplierName: undefined, evalPeriod: undefined, evalType: 'monthly', incomingTotal: 0, incomingPass: 0, qualityScore: undefined, deliveryScore: undefined, serviceScore: undefined }; proxy.resetForm('evalRef') }
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
.qms-seval-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-seval-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-seval-page .filter-card { padding:14px 20px 16px; }
.qms-seval-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-seval-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-seval-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-seval-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-seval-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-seval-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-seval-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-seval-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-seval-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-seval-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-seval-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-seval-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-seval-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-seval-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-seval-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-seval-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-seval-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-seval-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-seval-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-seval-page .field .control :deep(.el-select) { width:100%; }
.qms-seval-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-seval-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-seval-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-seval-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-seval-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-seval-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-seval-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-seval-page .btn-soft .el-icon { font-size:14px; }
.qms-seval-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-seval-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-seval-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-seval-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-seval-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-seval-page .table-wrap { overflow-x:auto; }
.qms-seval-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-seval-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-seval-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-seval-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-seval-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-seval-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-seval-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-seval-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-seval-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-seval-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
