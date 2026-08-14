<template>
  <div class="app-container qms-audit-page">
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
          <label>审核编号</label>
          <div class="control">
            <el-input v-model="queryParams.auditPlanNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>审核标题</label>
          <div class="control">
            <el-input v-model="queryParams.auditTitle" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>年度</label>
          <div class="control">
            <el-input-number v-model="queryParams.auditYear" :min="2020" :max="2099" style="width: 100%" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.planStatus" placeholder="全部" clearable @change="handleQuery">
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:audit:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:audit:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:audit:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:audit:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_audit_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="审核编号" prop="auditPlanNo" key="auditPlanNo" :width="colWidth('auditPlanNo', 160)" resizable show-overflow-tooltip v-if="columns.auditPlanNo.visible" />
          <el-table-column label="审核标题" prop="auditTitle" key="auditTitle" :width="colWidth('auditTitle', 220)" resizable show-overflow-tooltip v-if="columns.auditTitle.visible" />
          <el-table-column label="年度" prop="auditYear" key="auditYear" :width="colWidth('auditYear', 80)" resizable align="center" v-if="columns.auditYear.visible" />
          <el-table-column label="主审" prop="leadAuditor" key="leadAuditor" :width="colWidth('leadAuditor', 100)" resizable show-overflow-tooltip v-if="columns.leadAuditor.visible" />
          <el-table-column label="开始日期" prop="auditDateFrom" key="auditDateFrom" :width="colWidth('auditDateFrom', 120)" resizable align="center" v-if="columns.auditDateFrom.visible"><template #default="scope"><span>{{ parseTime(scope.row.auditDateFrom, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="结束日期" prop="auditDateTo" key="auditDateTo" :width="colWidth('auditDateTo', 120)" resizable align="center" v-if="columns.auditDateTo.visible"><template #default="scope"><span>{{ parseTime(scope.row.auditDateTo, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="状态" prop="planStatus" key="planStatus" :width="colWidth('planStatus', 100)" resizable align="center" v-if="columns.planStatus.visible"><template #default="scope"><span class="badge" :class="planStatusBadgeClass(scope.row.planStatus)"><span class="dot"></span>{{ planStatusLabel(scope.row.planStatus) }}</span></template></el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:audit:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:audit:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">内审计划详情</span>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">审核编号</span><div class="rd-value">{{ viewData.auditPlanNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">审核标题</span><div class="rd-value">{{ viewData.auditTitle || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">审核年度</span><div class="rd-value">{{ viewData.auditYear || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">类型</span><div class="rd-value">{{ viewData.auditType === 'annual' ? '年度计划' : viewData.auditType === 'adhoc' ? '临时审核' : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">主审</span><div class="rd-value">{{ viewData.leadAuditor || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="planStatusBadgeClass(viewData.planStatus)"><span class="dot"></span>{{ planStatusLabel(viewData.planStatus) }}</span></div></div>
            <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ parseTime(viewData.auditDateFrom, '{y}-{m}-{d}') || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ parseTime(viewData.auditDateTo, '{y}-{m}-{d}') || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_content')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>审核内容</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_content }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_content" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">审核范围</span><div class="rd-value">{{ viewData.auditScope || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">审核结论</span><div class="rd-value">{{ viewData.auditConclusion || '-' }}</div></div>
            <div class="rd-item rd-item--full" v-if="viewData.remark"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="auditRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="审核标题" prop="auditTitle"><el-input v-model="form.auditTitle" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="审核年度"><el-input-number v-model="form.auditYear" :min="2020" :max="2099" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="类型"><el-select v-model="form.auditType" style="width: 100%"><el-option label="年度计划" value="annual" /><el-option label="临时审核" value="adhoc" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="主审"><el-input v-model="form.leadAuditor" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="开始日期"><el-date-picker v-model="form.auditDateFrom" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="结束日期"><el-date-picker v-model="form.auditDateTo" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="状态"><el-select v-model="form.planStatus" style="width: 100%"><el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_content')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>审核内容</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_content }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_content">
              <el-form-item label="审核范围"><el-input v-model="form.auditScope" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item label="审核结论"><el-input v-model="form.auditConclusion" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsAudit">
import { listAudit, getAudit, addAudit, updateAudit, delAudit } from '@/api/qms/audit'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_audit_index')
const { collapsedCards, toggleCard } = useDetailCard(['v_basic', 'v_content', 'e_basic', 'e_content'])
const { proxy } = getCurrentInstance()
const { qms_audit_plan_status: statusOptions } = proxy.useDict('qms_audit_plan_status')

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
const defaultColumns = { auditPlanNo: { label: '审核编号', visible: true }, auditTitle: { label: '审核标题', visible: true }, auditYear: { label: '年度', visible: true }, leadAuditor: { label: '主审', visible: true }, auditDateFrom: { label: '开始日期', visible: true }, auditDateTo: { label: '结束日期', visible: true }, planStatus: { label: '状态', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_audit_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, auditPlanNo: undefined, auditTitle: undefined, auditYear: new Date().getFullYear(), planStatus: undefined, remark: undefined, params: {} } })
const { queryParams, form } = toRefs(data)
const rules = { auditTitle: [{ required: true, message: '请输入审核标题', trigger: 'blur' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.auditPlanNo) count++
  if (queryParams.value.auditTitle) count++
  if (queryParams.value.auditYear) count++
  if (queryParams.value.planStatus) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listAudit(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.auditPlanNo = undefined; queryParams.value.auditTitle = undefined; queryParams.value.planStatus = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.auditYear = new Date().getFullYear(); queryParams.value.params = {}; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增内审计划' }
function handleUpdate(row) {
  const id = row?.auditPlanId || selectedId.value
  getAudit(id).then(res => { data.form = res.data; open.value = true; title.value = '修改内审计划' })
}
function handleView(row) { getAudit(row.auditPlanId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.auditPlanId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      if (form.value.auditPlanId) { updateAudit(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addAudit(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.auditPlanId ? [row.auditPlanId] : selectedIds.value
  proxy.$modal.confirm('确认删除？').then(() => delAudit(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/audit/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `audit_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { auditPlanId: null, auditTitle: undefined, auditYear: new Date().getFullYear(), auditType: 'annual', leadAuditor: undefined, planStatus: '0' }; proxy.resetForm('auditRef') }
function cancel() { open.value = false; reset() }
function planStatusLabel(val) { const item = statusOptions.value.find(d => d.value == val); return item ? item.label : '-' }
function planStatusBadgeClass(val) { if (val == '0') return 'gray'; if (val == '1') return 'blue'; if (val == '2') return 'green'; if (val == '3') return 'orange'; return 'gray' }
getList()
</script>

<style scoped>
.qms-audit-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-audit-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-audit-page .filter-card { padding:14px 20px 16px; }
.qms-audit-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-audit-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-audit-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-audit-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-audit-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-audit-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-audit-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-audit-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-audit-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-audit-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-audit-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-audit-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-audit-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-audit-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-audit-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-audit-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-audit-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-audit-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-audit-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-audit-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-audit-page .field .control :deep(.el-select) { width:100%; }
.qms-audit-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-audit-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-audit-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-audit-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-audit-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-audit-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-audit-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-audit-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-audit-page .btn-soft .el-icon { font-size:14px; }
.qms-audit-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-audit-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-audit-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-audit-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-audit-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-audit-page .table-wrap { overflow-x:auto; }
.qms-audit-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-audit-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-audit-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-audit-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-audit-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-audit-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-audit-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-audit-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-audit-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-audit-page .badge.green .dot { background:var(--green-500); }
.qms-audit-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-audit-page .badge.gray .dot { background:var(--ink-400); }
.qms-audit-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-audit-page .badge.orange .dot { background:#f59e0b; }
.qms-audit-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-audit-page .badge.blue .dot { background:var(--brand-500); }
.qms-audit-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-audit-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-audit-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-audit-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-audit-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
@media (max-width:1100px) { .qms-audit-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-audit-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-audit-page .toolbar { flex-wrap:wrap; gap:10px; } }

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
