<template>
  <div class="app-container qms-mr-page">
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
          <label>评审编号</label>
          <div class="control">
            <el-input v-model="queryParams.mrNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>评审标题</label>
          <div class="control">
            <el-input v-model="queryParams.mrTitle" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>年度</label>
          <div class="control">
            <el-input-number v-model="queryParams.mrYear" :min="2020" :max="2099" style="width: 100%" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.mrStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:mr:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:mr:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:mr:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:mr:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_mr_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="评审编号" prop="mrNo" key="mrNo" :width="colWidth('mrNo', 160)" resizable show-overflow-tooltip v-if="columns.mrNo.visible" />
          <el-table-column label="评审标题" prop="mrTitle" key="mrTitle" :width="colWidth('mrTitle', 220)" resizable show-overflow-tooltip v-if="columns.mrTitle.visible" />
          <el-table-column label="年度" prop="mrYear" key="mrYear" :width="colWidth('mrYear', 80)" resizable align="center" v-if="columns.mrYear.visible" />
          <el-table-column label="评审日期" prop="mrDate" key="mrDate" :width="colWidth('mrDate', 120)" resizable align="center" v-if="columns.mrDate.visible"><template #default="scope"><span>{{ parseTime(scope.row.mrDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="主持人" prop="chairperson" key="chairperson" :width="colWidth('chairperson', 100)" resizable show-overflow-tooltip v-if="columns.chairperson.visible" />
          <el-table-column label="状态" prop="mrStatus" key="mrStatus" :width="colWidth('mrStatus', 100)" resizable align="center" v-if="columns.mrStatus.visible"><template #default="scope"><span class="badge" :class="mrBadgeClass(scope.row.mrStatus)"><span class="dot"></span>{{ mrStatusLabel(scope.row.mrStatus) }}</span></template></el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:mr:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:mr:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">管理评审详情</span>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">评审编号</span><div class="rd-value">{{ viewData.mrNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评审标题</span><div class="rd-value">{{ viewData.mrTitle || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">年度</span><div class="rd-value">{{ viewData.mrYear || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评审日期</span><div class="rd-value">{{ parseTime(viewData.mrDate, '{y}-{m}-{d}') || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">主持人</span><div class="rd-value">{{ viewData.chairperson || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="mrBadgeClass(viewData.mrStatus)"><span class="dot"></span>{{ mrStatusLabel(viewData.mrStatus) }}</span></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_content')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>评审内容</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_content }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_content" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">参会人员</span><div class="rd-value">{{ viewData.participants || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">输入汇总</span><div class="rd-value">{{ viewData.inputSummary || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">决议事项</span><div class="rd-value">{{ viewData.resolution || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">改进项</span><div class="rd-value">{{ viewData.actionItems || '-' }}</div></div>
            <div class="rd-item rd-item--full" v-if="viewData.remark"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/><path d="M9 14l2 2 4-4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="mrRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="评审标题" prop="mrTitle"><el-input v-model="form.mrTitle" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年度"><el-input-number v-model="form.mrYear" :min="2020" :max="2099" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="评审日期"><el-date-picker v-model="form.mrDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="主持人"><el-input v-model="form.chairperson" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="状态"><el-select v-model="form.mrStatus" style="width: 100%"><el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_content')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>评审内容</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_content }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_content">
              <el-form-item label="参会人员"><el-input v-model="form.participants" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
              <el-form-item label="输入汇总"><el-input v-model="form.inputSummary" type="textarea" :rows="3" placeholder="质量目标达成、客诉情况、CAPA情况、内审结果、供应商质量等" /></el-form-item>
              <el-form-item label="决议事项"><el-input v-model="form.resolution" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
              <el-form-item label="改进项"><el-input v-model="form.actionItems" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsMr">
import { listMr, getMr, addMr, updateMr, delMr } from '@/api/qms/mr'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_mr_index')
const { collapsedCards, toggleCard } = useDetailCard(['v_basic', 'v_content', 'e_basic', 'e_content'])
const { proxy } = getCurrentInstance()
const { qms_mr_status: statusOptions } = proxy.useDict('qms_mr_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const viewOpen = ref(false)
const viewData = ref(null)
const defaultColumns = { mrNo: { label: '评审编号', visible: true }, mrTitle: { label: '评审标题', visible: true }, mrYear: { label: '年度', visible: true }, mrDate: { label: '评审日期', visible: true }, chairperson: { label: '主持人', visible: true }, mrStatus: { label: '状态', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_mr_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, mrNo: undefined, mrTitle: undefined, mrYear: new Date().getFullYear(), mrStatus: undefined, remark: undefined, params: {} } })
const { queryParams, form } = toRefs(data)
const rules = { mrTitle: [{ required: true, message: '请输入评审标题', trigger: 'blur' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.mrNo) count++
  if (queryParams.value.mrTitle) count++
  if (queryParams.value.mrYear) count++
  if (queryParams.value.mrStatus) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listMr(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.mrNo = undefined; queryParams.value.mrTitle = undefined; queryParams.value.mrStatus = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.mrYear = new Date().getFullYear(); queryParams.value.params = {}; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增管理评审' }
function handleUpdate(row) {
  const id = row?.mrId || selectedId.value
  getMr(id).then(res => { data.form = res.data; open.value = true; title.value = '修改管理评审' })
}
function handleView(row) { getMr(row.mrId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.mrId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['mrRef'].validate(valid => {
    if (valid) {
      if (form.value.mrId) { updateMr(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addMr(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.mrId ? [row.mrId] : selectedIds.value
  proxy.$modal.confirm('确认删除？').then(() => delMr(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/mr/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `mr_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { mrId: null, mrTitle: undefined, mrYear: new Date().getFullYear(), mrDate: undefined, chairperson: undefined, mrStatus: '0' }; proxy.resetForm('mrRef') }
function cancel() { open.value = false; reset() }
function mrStatusLabel(val) { const item = statusOptions.value.find(d => d.value == val); return item ? item.label : '-' }
function mrBadgeClass(val) { if (val == '0') return 'gray'; if (val == '1') return 'green'; if (val == '2') return 'blue'; if (val == '3') return 'orange'; return 'gray' }
getList()
</script>

<style scoped>
.qms-mr-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-mr-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-mr-page .filter-card { padding:14px 20px 16px; }
.qms-mr-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-mr-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-mr-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-mr-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-mr-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-mr-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-mr-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-mr-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-mr-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-mr-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-mr-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-mr-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-mr-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-mr-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-mr-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-mr-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-mr-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-mr-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-mr-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-mr-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-mr-page .field .control :deep(.el-select) { width:100%; }
.qms-mr-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-mr-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-mr-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-mr-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-mr-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-mr-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-mr-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-mr-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-mr-page .btn-soft .el-icon { font-size:14px; }
.qms-mr-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-mr-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-mr-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-mr-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-mr-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-mr-page .table-wrap { overflow-x:auto; }
.qms-mr-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-mr-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-mr-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-mr-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-mr-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-mr-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-mr-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-mr-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-mr-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-mr-page .badge.green .dot { background:var(--green-500); }
.qms-mr-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-mr-page .badge.gray .dot { background:var(--ink-400); }
.qms-mr-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-mr-page .badge.orange .dot { background:#f59e0b; }
.qms-mr-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-mr-page .badge.blue .dot { background:var(--brand-500); }
.qms-mr-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-mr-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-mr-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-mr-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-mr-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
@media (max-width:1100px) { .qms-mr-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-mr-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-mr-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== Dialog Detail Page Styles ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: 100% !important; }

.rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -30px; right: -15px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 10px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }

.rd-page { max-width: 100%; margin: 0 auto; }
.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-header .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; border-radius: 6px; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-card-header .rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-card-header .rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-card-header .rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }
.rd-card-body { padding: 14px 16px; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.rd-form-tip:hover { color: #909399; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }
.rd-card:nth-child(5) { animation-delay: 0.24s; }
.rd-card:nth-child(6) { animation-delay: 0.30s; }

@media (max-width:768px) { .rd-grid { grid-template-columns: 1fr; } }
</style>
