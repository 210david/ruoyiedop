<template>
  <div class="app-container qms-esig-page">
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
          <label>业务类型</label>
          <div class="control is-select"><el-select v-model="queryParams.bizType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in bizTypeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field">
          <label>业务编号</label>
          <div class="control"><el-input v-model="queryParams.bizNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>签名用户</label>
          <div class="control"><el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>签名结果</label>
          <div class="control is-select"><el-select v-model="queryParams.sigResult" placeholder="全部" clearable @change="handleQuery"><el-option label="通过" value="pass" /><el-option label="不通过" value="fail" /><el-option label="驳回" value="reject" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备注</label>
          <div class="control"><el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>签名时间</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:esig:remove']"><el-icon><Delete /></el-icon> 删除</button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_esig_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="业务类型" prop="bizType" key="bizType" :width="colWidth('bizType', 130)" resizable align="center" v-if="columns.bizType.visible"><template #default="scope"><span class="badge" :class="bizTypeBadgeClass(scope.row.bizType)"><span class="dot"></span>{{ bizTypeLabel(scope.row.bizType) }}</span></template></el-table-column>
          <el-table-column label="业务编号" prop="bizNo" key="bizNo" :width="colWidth('bizNo', 160)" resizable show-overflow-tooltip v-if="columns.bizNo.visible" />
          <el-table-column label="签名用户" prop="nickName" key="nickName" :width="colWidth('nickName', 120)" resizable v-if="columns.nickName.visible" />
          <el-table-column label="签名含义" prop="sigMeaning" key="sigMeaning" :width="colWidth('sigMeaning', 160)" resizable show-overflow-tooltip v-if="columns.sigMeaning.visible" />
          <el-table-column label="签名结果" prop="sigResult" key="sigResult" :width="colWidth('sigResult', 100)" resizable align="center" v-if="columns.sigResult.visible"><template #default="scope"><span class="badge" :class="sigResultBadgeClass(scope.row.sigResult)"><span class="dot"></span>{{ sigResultLabel(scope.row.sigResult) }}</span></template></el-table-column>
          <el-table-column label="签名意见" prop="sigOpinion" key="sigOpinion" :width="colWidth('sigOpinion', 200)" resizable show-overflow-tooltip v-if="columns.sigOpinion.visible" />
          <el-table-column label="IP地址" prop="sigIp" key="sigIp" :width="colWidth('sigIp', 130)" resizable v-if="columns.sigIp.visible" />
          <el-table-column label="签名时间" prop="sigTime" key="sigTime" :width="colWidth('sigTime', 160)" resizable align="center" v-if="columns.sigTime.visible" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:esig:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- 详情弹窗 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><span class="rd-detail-header-title">签名记录详情</span></div></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务类型">{{ bizTypeLabel(form.bizType) }}</el-descriptions-item>
        <el-descriptions-item label="业务编号">{{ form.bizNo }}</el-descriptions-item>
        <el-descriptions-item label="签名用户">{{ form.nickName }}（{{ form.userName }}）</el-descriptions-item>
        <el-descriptions-item label="签名结果">{{ sigResultLabel(form.sigResult) }}</el-descriptions-item>
        <el-descriptions-item label="签名含义" :span="2">{{ form.sigMeaning }}</el-descriptions-item>
        <el-descriptions-item label="签名意见" :span="2">{{ form.sigOpinion }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ form.sigIp }}</el-descriptions-item>
        <el-descriptions-item label="签名时间">{{ form.sigTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="open = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsEsig">
import { listEsigRecord, getEsigRecord, delEsigRecord } from '@/api/qms/esig'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_esig_index')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const defaultColumns = { bizType: { label: '业务类型', visible: true }, bizNo: { label: '业务编号', visible: true }, nickName: { label: '签名用户', visible: true }, sigMeaning: { label: '签名含义', visible: true }, sigResult: { label: '签名结果', visible: true }, sigOpinion: { label: '签名意见', visible: true }, sigIp: { label: 'IP地址', visible: true }, sigTime: { label: '签名时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_esig_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const bizTypeOptions = [
  { value: 'insp_judge', label: '检验判定' },
  { value: 'ncr_approve', label: 'NCR审批' },
  { value: 'ncr_disposition', label: 'NCR处置' },
  { value: 'ncr_verify', label: 'NCR验证' }
]

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, bizType: undefined, bizNo: undefined, userName: undefined, sigResult: undefined, remark: undefined, params: {} }
})
const { queryParams, form } = toRefs(data)

const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.bizType) c++; if (queryParams.value.bizNo) c++; if (queryParams.value.userName) c++; if (queryParams.value.sigResult) c++; if (queryParams.value.remark) c++; if (dateRange.value && dateRange.value.length > 0) c++; return c })

function bizTypeLabel(val) { const item = bizTypeOptions.find(d => d.value == val); return item ? item.label : (val || '-') }
function bizTypeBadgeClass(val) { if (val == 'insp_judge') return 'blue'; if (val == 'ncr_approve') return 'orange'; if (val == 'ncr_disposition') return 'gray'; if (val == 'ncr_verify') return 'green'; return 'gray' }
function sigResultLabel(val) { if (val == 'pass') return '通过'; if (val == 'fail') return '不通过'; if (val == 'reject') return '驳回'; return val || '-' }
function sigResultBadgeClass(val) { if (val == 'pass') return 'green'; if (val == 'fail') return 'red'; if (val == 'reject') return 'orange'; return 'gray' }

function getList() { loading.value = true; listEsigRecord(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.bizType = undefined; queryParams.value.bizNo = undefined; queryParams.value.userName = undefined; queryParams.value.sigResult = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.esigId); multiple.value = !selection.length }
function handleDetail(row) { getEsigRecord(row.esigId).then(res => { form.value = res.data; open.value = true }) }
function handleDelete(row) { const esigIds = row.esigId || ids.value; proxy.$modal.confirm('确认删除编号为"' + esigIds + '"的签名记录？').then(() => delEsigRecord(esigIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-esig-page { padding-top: 10px; --brand-50:#eef2ff; --brand-500:#6366f1; --brand-600:#4f46e5; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-esig-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-esig-page .filter-card { padding:14px 20px 16px; }
.qms-esig-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-esig-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-esig-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-esig-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-esig-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-esig-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-esig-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-esig-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-esig-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-esig-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-esig-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-esig-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-esig-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-esig-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-esig-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-esig-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-esig-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-esig-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-esig-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-esig-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-esig-page .field .control :deep(.el-select) { width:100%; }
.qms-esig-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-esig-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-esig-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-esig-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-esig-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-esig-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-esig-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.qms-esig-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-esig-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-esig-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-esig-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-esig-page .btn-soft .el-icon { font-size:14px; }
.qms-esig-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-esig-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-esig-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-esig-page .table-wrap { overflow-x:auto; }
.qms-esig-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-esig-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-esig-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-esig-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-esig-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-esig-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-esig-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-esig-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-esig-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-esig-page .badge.green .dot { background:var(--green-500); }
.qms-esig-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-esig-page .badge.gray .dot { background:var(--ink-400); }
.qms-esig-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-esig-page .badge.red .dot { background:var(--red-500); }
.qms-esig-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-esig-page .badge.orange .dot { background:#f59e0b; }
.qms-esig-page .badge.blue { background:var(--brand-50); color:var(--brand-600); border-color:#c7d2fe; }
.qms-esig-page .badge.blue .dot { background:var(--brand-500); }
.qms-esig-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-esig-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-esig-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
