<template>
  <div class="app-container qms-defect-page">
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
          <label>缺陷代码</label>
          <div class="control"><el-input v-model="queryParams.defectCode" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div>
        </div>
        <div class="field">
          <label>缺陷名称</label>
          <div class="control"><el-input v-model="queryParams.defectName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>缺陷等级</label>
          <div class="control is-select"><el-select v-model="queryParams.defectLevel" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field">
          <label>缺陷分类</label>
          <div class="control is-select"><el-select v-model="queryParams.defectCategory" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in qms_defect_category" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option label="正常" value="0" /><el-option label="停用" value="1" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备注</label>
          <div class="control"><el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:defect:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:defect:edit']"><el-icon><Edit /></el-icon> 修改</button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:defect:remove']"><el-icon><Delete /></el-icon> 删除</button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:defect:export']"><el-icon><Download /></el-icon> 导出</button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_defect_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="缺陷代码" prop="defectCode" key="defectCode" :width="colWidth('defectCode', 130)" resizable show-overflow-tooltip v-if="columns.defectCode.visible" />
          <el-table-column label="缺陷名称" prop="defectName" key="defectName" :width="colWidth('defectName', 200)" resizable show-overflow-tooltip v-if="columns.defectName.visible" />
          <el-table-column label="缺陷等级" prop="defectLevel" key="defectLevel" :width="colWidth('defectLevel', 110)" resizable align="center" v-if="columns.defectLevel.visible"><template #default="scope"><span class="badge" :class="defectBadgeClass(scope.row.defectLevel)"><span class="dot"></span>{{ defectLevelLabel(scope.row.defectLevel) }}</span></template></el-table-column>
          <el-table-column label="缺陷分类" prop="defectCategory" key="defectCategory" :width="colWidth('defectCategory', 120)" resizable align="center" v-if="columns.defectCategory.visible"><template #default="scope"><span class="badge violet"><span class="dot"></span>{{ defectCategoryLabel(scope.row.defectCategory) }}</span></template></el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:defect:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:defect:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="816px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="defectRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="缺陷代码" prop="defectCode"><el-input v-model="form.defectCode" placeholder="请输入缺陷代码" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="缺陷名称" prop="defectName"><el-input v-model="form.defectName" placeholder="请输入缺陷名称" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>分类与等级</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="缺陷等级" prop="defectLevel"><el-select v-model="form.defectLevel" style="width: 100%"><el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="缺陷分类" prop="defectCategory"><el-select v-model="form.defectCategory" clearable placeholder="请选择缺陷分类" style="width: 100%"><el-option v-for="d in qms_defect_category" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="816px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">缺陷代码详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">缺陷代码</span><div class="rd-value">{{ viewData.defectCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">缺陷名称</span><div class="rd-value">{{ viewData.defectName || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>分类与等级</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">缺陷等级</span><div class="rd-value"><span class="badge" :class="defectBadgeClass(viewData.defectLevel)"><span class="dot"></span>{{ defectLevelLabel(viewData.defectLevel) }}</span></div></div>
            <div class="rd-item"><span class="rd-label">缺陷分类</span><div class="rd-value"><dict-tag :options="qms_defect_category" :value="viewData.defectCategory" /></div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="viewData.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ viewData.status === '0' ? '正常' : '停用' }}</span></div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="QmsDefect">
import { listDefect, getDefect, addDefect, updateDefect, delDefect } from '@/api/qms/defect'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_defect_index')
const { qms_defect_level, qms_defect_category } = proxy.useDict('qms_defect_level', 'qms_defect_category')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const defaultColumns = { defectCode: { label: '缺陷代码', visible: true }, defectName: { label: '缺陷名称', visible: true }, defectLevel: { label: '缺陷等级', visible: true }, defectCategory: { label: '缺陷分类', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_defect_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, defectCode: undefined, defectName: undefined, defectLevel: undefined, defectCategory: undefined, status: undefined, remark: undefined, params: {} },
  rules: {
    defectCode: [{ required: true, message: '缺陷代码不能为空', trigger: 'blur' }],
    defectName: [{ required: true, message: '缺陷名称不能为空', trigger: 'blur' }],
    defectLevel: [{ required: true, message: '缺陷等级不能为空', trigger: 'change' }],
    defectCategory: [{ required: true, message: '缺陷分类不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.defectCode) c++; if (queryParams.value.defectName) c++; if (queryParams.value.defectLevel) c++; if (queryParams.value.defectCategory) c++; if (queryParams.value.status) c++; if (queryParams.value.remark) c++; if (dateRange.value && dateRange.value.length > 0) c++; return c })

function defectLevelLabel(val) { const item = qms_defect_level.value.find(d => d.value == val); return item ? item.label : '-' }
function defectBadgeClass(val) { if (val == '1' || val == '2') return 'red'; if (val == '3') return 'orange'; return 'gray' }
function defectCategoryLabel(val) { const item = qms_defect_category.value.find(d => d.value == val); return item ? item.label : '-' }

function getList() { loading.value = true; listDefect(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.defectCode = undefined; queryParams.value.defectName = undefined; queryParams.value.defectLevel = undefined; queryParams.value.defectCategory = undefined; queryParams.value.status = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.defectId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { defectId: undefined, defectCode: undefined, defectName: undefined, defectLevel: '3', defectCategory: undefined, status: '0', remark: undefined }; proxy.resetForm('defectRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加缺陷代码' }
function handleUpdate(row) { reset(); getDefect(row.defectId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改缺陷代码' }) }
function handleView(row) { getDefect(row.defectId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['defectRef'].validate(valid => { if (valid) { if (form.value.defectId != undefined) { updateDefect(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addDefect(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const defectIds = row.defectId || ids.value; proxy.$modal.confirm('确认删除编号为"' + defectIds + '"的数据？').then(() => delDefect(defectIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/defect/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `defect_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-defect-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-500:#6366f1; --brand-600:#4f46e5; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-defect-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-defect-page .filter-card { padding:14px 20px 16px; }
.qms-defect-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-defect-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-defect-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-defect-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-defect-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-defect-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-defect-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-defect-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-defect-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-defect-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-defect-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-defect-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-defect-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-defect-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-defect-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-defect-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-defect-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-defect-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-defect-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-defect-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-defect-page .field .control :deep(.el-select) { width:100%; }
.qms-defect-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-defect-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-defect-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-defect-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-defect-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-defect-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-defect-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.qms-defect-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-defect-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-defect-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-defect-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-defect-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-defect-page .btn-soft .el-icon { font-size:14px; }
.qms-defect-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-defect-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-defect-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-defect-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-defect-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-defect-page .table-wrap { overflow-x:auto; }
.qms-defect-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-defect-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-defect-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-defect-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-defect-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-defect-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-defect-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-defect-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-defect-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-defect-page .badge.green .dot { background:var(--green-500); }
.qms-defect-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-defect-page .badge.gray .dot { background:var(--ink-400); }
.qms-defect-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-defect-page .badge.red .dot { background:var(--red-500); }
.qms-defect-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-defect-page .badge.orange .dot { background:#f59e0b; }
.qms-defect-page .badge.violet { background:var(--violet-50); color:#7c3aed; border-color:#ddd6fe; }
.qms-defect-page .badge.violet .dot { background:#8b5cf6; }
.qms-defect-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-defect-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-defect-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
