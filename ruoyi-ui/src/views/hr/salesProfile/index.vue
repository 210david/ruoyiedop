<template>
  <div class="app-container hr-salesprofile-page">
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
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>销售区域</label>
          <div class="control">
            <el-input v-model="queryParams.salesRegion" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>职级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.salesLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in salesLevelOptions" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:salesProfile:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['hr:salesProfile:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['hr:salesProfile:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_salesProfile_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="profileList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="员工编号" prop="employeeNo" key="employeeNo" :width="colWidth('employeeNo', 130)" resizable show-overflow-tooltip v-if="columns.employeeNo.visible" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" :min-width="colWidth('employeeName', 120)" resizable show-overflow-tooltip v-if="columns.employeeName.visible" />
          <el-table-column label="销售区域" prop="salesRegion" key="salesRegion" :width="colWidth('salesRegion', 130)" resizable show-overflow-tooltip v-if="columns.salesRegion.visible" />
          <el-table-column label="销售渠道" prop="channelName" key="channelName" :width="colWidth('channelName', 130)" resizable show-overflow-tooltip v-if="columns.channelName.visible" />
          <el-table-column label="职级" prop="salesLevel" key="salesLevel" :width="colWidth('salesLevel', 100)" resizable align="center" v-if="columns.salesLevel.visible"><template #default="scope"><span v-if="dictLabel(salesLevelOptions, scope.row.salesLevel)" class="badge blue"><span class="dot"></span>{{ dictLabel(salesLevelOptions, scope.row.salesLevel) }}</span></template></el-table-column>
          <el-table-column label="提成方案" key="commissionPlanId" :min-width="colWidth('commissionPlanId', 180)" resizable show-overflow-tooltip v-if="columns.commissionPlanId.visible"><template #default="scope"><span>{{ planName(scope.row.commissionPlanId) }}</span></template></el-table-column>
          <el-table-column label="年度营销目标" prop="entryTarget" key="entryTarget" :width="colWidth('entryTarget', 140)" resizable align="right" v-if="columns.entryTarget.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 180)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:salesProfile:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:salesProfile:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">关联系统用户ID用于营销业绩归属（客户档案业务归属字段）</span></div>
          </div>
        </div>
      </template>
      <el-form ref="profileRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="员工编号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入员工编号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="员工姓名" prop="employeeName"><el-input v-model="form.employeeName" placeholder="请输入员工姓名" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="关联系统用户" prop="userId"><el-input v-model="form.userId" placeholder="请输入关联系统用户ID（营销业绩归属）" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>营销属性与提成方案</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="销售区域" prop="salesRegion"><el-input v-model="form.salesRegion" placeholder="请输入销售区域" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="销售渠道" prop="channelName"><el-input v-model="form.channelName" placeholder="请输入销售渠道" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="职级" prop="salesLevel">
                  <el-select v-model="form.salesLevel" placeholder="请选择职级" clearable style="width: 100%">
                    <el-option v-for="d in salesLevelOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="年度营销目标" prop="entryTarget"><el-input v-model="form.entryTarget" placeholder="请输入年度营销目标金额" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="提成方案" prop="commissionPlanId">
                <el-select v-model="form.commissionPlanId" filterable clearable placeholder="请选择提成方案（仅启用中的方案可选）" style="width: 100%">
                  <el-option v-for="p in planOptions" :key="p.planId" :label="p.planName + '（' + dictLabel(baseOptions, p.calcBase) + '）'" :value="p.planId" :disabled="p.planStatus !== '1'" />
                </el-select>
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
  </div>
</template>
<script setup name="SalesProfile">
import { listProfile, getProfile, addProfile, updateProfile, delProfile } from '@/api/hr/salesPerf'
import { listPlan } from '@/api/hr/commission'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_salesProfile_index')
const { collapsedCards, toggleCard } = useDetailCard(['ec0','ec1'])
const { proxy } = getCurrentInstance()
const { hr_sales_level: salesLevelOptions } = proxy.useDict('hr_sales_level')
const { hr_commission_base: baseOptions } = proxy.useDict('hr_commission_base')

// 提成方案下拉（弹框选择替代手输 ID）：加载全部方案供名称显示，仅启用中的方案可选
const planOptions = ref([])
loadPlanOptions()
function loadPlanOptions() { listPlan({ pageNum: 1, pageSize: 200 }).then(res => { planOptions.value = res.rows || [] }).catch(() => {}) }
function planName(planId) { const p = planOptions.value.find(x => x.planId === planId); return p ? p.planName : (planId || '-') }

const defaultColumns = {
  employeeNo: { label: '员工编号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  salesRegion: { label: '销售区域', visible: true },
  channelName: { label: '销售渠道', visible: true },
  salesLevel: { label: '职级', visible: true },
  commissionPlanId: { label: '提成方案', visible: true },
  entryTarget: { label: '年度营销目标', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_salesProfile_columns')
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
const profileList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const open = ref(false)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, employeeName: undefined, salesRegion: undefined, salesLevel: undefined } })
const { queryParams, form } = toRefs(data)
const rules = {
  employeeNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
  employeeName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  userId: [{ required: true, message: '请输入关联系统用户ID', trigger: 'blur' }]
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.salesRegion) count++
  if (queryParams.value.salesLevel) count++
  return count
})

function getList() {
  loading.value = true
  listProfile(queryParams.value).then(res => {
    profileList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.employeeName = undefined; queryParams.value.salesRegion = undefined; queryParams.value.salesLevel = undefined; handleQuery() }
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function handleAdd() { reset(); open.value = true; title.value = '新增营销档案' }
function handleUpdate(row) {
  const id = row?.profileId || selectedId.value
  if (!id) { proxy.$modal.msgError('请先选择档案'); return }
  getProfile(id).then(res => { data.form = res.data; open.value = true; title.value = '修改营销档案' })
}
function handleDelete(row) {
  const ids = row?.profileId ? [row.profileId] : selectedIds.value
  proxy.$modal.confirm('确认删除该营销档案？').then(() => delProfile(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.profileId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['profileRef'].validate(valid => {
    if (valid) {
      if (form.value.profileId) { updateProfile(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addProfile(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function reset() { form.value = { profileId: null, employeeNo: undefined, employeeName: undefined, userId: undefined, salesRegion: undefined, channelName: undefined, salesLevel: undefined, commissionPlanId: undefined, entryTarget: undefined, remark: undefined }; proxy.resetForm('profileRef') }
function cancel() { open.value = false; reset() }
getList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.hr-salesprofile-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.hr-salesprofile-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.hr-salesprofile-page .filter-card { padding:14px 20px 16px; }
.hr-salesprofile-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-salesprofile-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-salesprofile-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-salesprofile-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-salesprofile-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-salesprofile-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-salesprofile-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-salesprofile-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(3, minmax(0,1fr)); gap:12px 16px; }
.hr-salesprofile-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-salesprofile-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-salesprofile-page .filter-card .filter-buttons { display:flex; gap:8px; }
.hr-salesprofile-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-salesprofile-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.hr-salesprofile-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-salesprofile-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-salesprofile-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-salesprofile-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-salesprofile-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-salesprofile-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-salesprofile-page .field .control :deep(.el-select) { width:100%; }
.hr-salesprofile-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-salesprofile-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.hr-salesprofile-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-salesprofile-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-salesprofile-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-salesprofile-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-salesprofile-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-salesprofile-page .btn-soft .el-icon { font-size:14px; }
.hr-salesprofile-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-salesprofile-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-salesprofile-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-salesprofile-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-salesprofile-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-salesprofile-page .table-wrap { overflow-x:auto; }
.hr-salesprofile-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-salesprofile-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-salesprofile-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-salesprofile-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-salesprofile-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-salesprofile-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-salesprofile-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .hr-salesprofile-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-salesprofile-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-salesprofile-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.hr-salesprofile-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.hr-salesprofile-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-salesprofile-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-salesprofile-page .badge.blue .dot { background:var(--brand-500); }
.hr-salesprofile-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-salesprofile-page .badge.green .dot { background:var(--green-500); }
.hr-salesprofile-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-salesprofile-page .badge.gray .dot { background:var(--ink-400); }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
