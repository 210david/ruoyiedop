<template>
  <div class="app-container safety-remind-page">
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
          <label>提醒类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.remindType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_remind_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>提醒状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.remindStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_remind_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>关联名称</label>
          <div class="control">
            <el-input v-model="queryParams.relatedName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>接收人</label>
          <div class="control">
            <el-input v-model="queryParams.toPersonName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>到期日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>处理人</label>
          <div class="control">
            <el-input v-model="queryParams.handleBy" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="createTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:remind:handle']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:remind:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_remind_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="remindList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="提醒类型" prop="remindType" key="remindType" :width="colWidth('remindType', 120)" resizable align="center" v-if="columns.remindType.visible">
            <template #default="scope"><span class="badge violet">{{ remindTypeLabel(scope.row.remindType) }}</span></template>
          </el-table-column>
          <el-table-column label="关联名称" prop="relatedName" key="relatedName" :width="colWidth('relatedName', 180)" resizable show-overflow-tooltip v-if="columns.relatedName.visible" />
          <el-table-column label="提醒内容" prop="remindContent" key="remindContent" :width="colWidth('remindContent', 200)" resizable show-overflow-tooltip v-if="columns.remindContent.visible" />
          <el-table-column label="到期日期" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 120)" resizable align="center" sortable="custom" v-if="columns.expireDate.visible">
            <template #default="scope">
              <span class="badge" :class="isOverdue(scope.row) ? 'red' : 'gray'">
                <span class="dot"></span>{{ scope.row.expireDate || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提前天数" prop="remindDays" key="remindDays" :width="colWidth('remindDays', 90)" resizable align="center" v-if="columns.remindDays.visible" />
          <el-table-column label="提醒状态" prop="remindStatus" key="remindStatus" :width="colWidth('remindStatus', 100)" resizable align="center" v-if="columns.remindStatus.visible">
            <template #default="scope">
              <span class="badge" :class="remindStatusBadgeClass(scope.row.remindStatus)">
                <span class="dot"></span>{{ remindStatusLabel(scope.row.remindStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="接收人" prop="toPersonName" key="toPersonName" :width="colWidth('toPersonName', 100)" resizable v-if="columns.toPersonName.visible" />
          <el-table-column label="处理人" prop="handleBy" key="handleBy" :width="colWidth('handleBy', 100)" resizable v-if="columns.handleBy.visible" />
          <el-table-column label="处理时间" prop="handleTime" key="handleTime" :width="colWidth('handleTime', 160)" resizable align="center" v-if="columns.handleTime.visible" />
          <el-table-column label="处理结果" prop="handleResult" key="handleResult" :width="colWidth('handleResult', 150)" resizable show-overflow-tooltip v-if="columns.handleResult.visible" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:remind:query']">查看</el-button>
              <el-button v-if="scope.row.remindStatus === '0' || scope.row.remindStatus === '1'" link type="primary" icon="Check" @click="handleHandle(scope.row)" v-hasPermi="['safety:remind:handle']">处理</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:remind:handle']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 详情查看对话框 -->
    <el-dialog v-model="viewOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">提醒详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>提醒信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">提醒类型</span><div class="rd-value">{{ viewForm.remindTypeLabel || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">提醒状态</span><div class="rd-value">{{ viewForm.remindStatusLabel || '-' }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">关联名称</span><div class="rd-value">{{ viewForm.relatedName || '-' }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">提醒内容</span><div class="rd-value">{{ viewForm.remindContent || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ viewForm.expireDate || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">提前天数</span><div class="rd-value">{{ viewForm.remindDays != null ? viewForm.remindDays + ' 天' : '-' }}</div></div>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>处理信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
              <div class="rd-grid">
                <div class="rd-item"><span class="rd-label">接收人</span><div class="rd-value">{{ viewForm.toPersonName || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">处理人</span><div class="rd-value">{{ viewForm.handleBy || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">处理时间</span><div class="rd-value">{{ viewForm.handleTime || '-' }}</div></div>
                <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime || '-' }}</div></div>
                <div class="rd-item rd-item--full"><span class="rd-label">处理结果</span><div class="rd-value">{{ viewForm.handleResult || '-' }}</div></div>
              </div>
            </div>
          </section>
        </div>
      <template #footer><div class="dialog-footer"><el-button @click="viewOpen = false">关 闭</el-button></div></template>
    </el-dialog>

    <el-dialog v-model="handleOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">处理提醒</span>
        </div>
      </template>
      <el-form ref="handleRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>处理信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="处理人"><el-input v-model="handleForm.handleBy" disabled placeholder="处理时自动记录" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="处理日期"><el-input v-model="handleForm.handleTime" disabled placeholder="处理时自动记录" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="处理结果" prop="handleResult">
                <el-input v-model="handleForm.handleResult" type="textarea" :rows="3" placeholder="请输入处理结果" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitHandle">确认处理</el-button>
          <el-button @click="handleOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyRemind">
import { listRemind, getRemind, handleRemind, delRemind } from '@/api/safety/remind'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const { safety_remind_type, safety_remind_status } = proxy.useDict('safety_remind_type', 'safety_remind_status')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_remind_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2"])

const remindList = ref([])
const handleOpen = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const createTimeRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const default_columns = {
  remindType: { label: '提醒类型', visible: true },
  relatedName: { label: '关联名称', visible: true },
  remindContent: { label: '提醒内容', visible: true },
  expireDate: { label: '到期日期', visible: true },
  remindDays: { label: '提前天数', visible: true },
  remindStatus: { label: '提醒状态', visible: true },
  toPersonName: { label: '接收人', visible: true },
  handleBy: { label: '处理人', visible: true },
  handleTime: { label: '处理时间', visible: true },
  handleResult: { label: '处理结果', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_remind_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const viewForm = ref({})

const data = reactive({
  handleForm: {},
  queryParams: { pageNum: 1, pageSize: 10, remindType: undefined, remindStatus: undefined, relatedName: undefined, toPersonName: undefined, handleBy: undefined, params: {} },
  handleRules: { handleResult: [{ required: true, message: '处理结果不能为空', trigger: 'blur' }] }
})
const { queryParams, handleForm, handleRules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.remindType) count++
  if (queryParams.value.remindStatus) count++
  if (queryParams.value.relatedName) count++
  if (queryParams.value.toPersonName) count++
  if (queryParams.value.handleBy) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  if (createTimeRange.value && createTimeRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  proxy.addDateRange(queryParams.value, dateRange.value, 'ExpireDate')
  proxy.addDateRange(queryParams.value, createTimeRange.value, 'CreateTime')
  listRemind(queryParams.value).then(response => {
    remindList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.remindType = undefined; queryParams.value.remindStatus = undefined; queryParams.value.relatedName = undefined; queryParams.value.toPersonName = undefined; queryParams.value.handleBy = undefined; dateRange.value = []; createTimeRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.remindId); single.value = selection.length !== 1; multiple.value = !selection.length }

function isOverdue(row) {
  if (!row.expireDate) return false
  return new Date(row.expireDate) < new Date()
}

function remindTypeLabel(type) { const item = safety_remind_type.value.find(d => d.value == type); return item ? item.label : '-' }
function remindStatusLabel(status) { const item = safety_remind_status.value.find(d => d.value == status); return item ? item.label : '-' }
function remindStatusBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green' }; return map[status] || 'gray' }

function handleHandle(row) {
  handleForm.value = {
    remindId: row.remindId,
    handleBy: userStore.name,
    handleTime: proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'),
    handleResult: ''
  }
  handleOpen.value = true
}

function submitHandle() {
  proxy.$refs['handleRef'].validate(valid => {
    if (valid) {
      handleRemind(handleForm.value).then(() => {
        proxy.$modal.msgSuccess('处理成功')
        handleOpen.value = false
        getList()
      })
    }
  })
}

function handleView(row) {
  getRemind(row.remindId).then(response => {
    const d = response.data
    viewForm.value = {
      ...d,
      remindTypeLabel: remindTypeLabel(d.remindType),
      remindStatusLabel: remindStatusLabel(d.remindStatus)
    }
    collapsedCards.c1 = false
    collapsedCards.c2 = !d.handleResult && !d.handleTime
    viewOpen.value = true
  })
}

function handleExport() { proxy.download('safety/remind/export', { ...queryParams }, `remind_${new Date().getTime()}.xlsx`) }
function handleDelete(row) {
  const remindIds = row.remindId || ids.value
  proxy.$modal.confirm('是否确认删除提醒？').then(function() {
    return delRemind(remindIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.safety-remind-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-remind-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-remind-page .filter-card { padding:14px 20px 16px; }
.safety-remind-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-remind-page .filter-card .adv-link { display:inline-flex; align-items:center; gap:4px; font-size:13px; color:var(--ink-500); cursor:pointer; user-select:none; transition:color .15s var(--ease-out); }
.safety-remind-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-remind-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-remind-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-remind-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-remind-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-remind-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-remind-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-remind-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-remind-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-remind-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-remind-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-remind-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-remind-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-remind-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-remind-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-remind-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-remind-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-remind-page .field .control :deep(.el-select) { width:100%; }
.safety-remind-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-remind-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-remind-page .toolbar .left, .safety-remind-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-remind-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-remind-page .table-wrap { overflow-x:auto; }
.safety-remind-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-remind-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-remind-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-remind-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-remind-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-remind-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-remind-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-remind-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-remind-page .badge.amber .dot { background:var(--amber-500); }
.safety-remind-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-remind-page .badge.blue .dot { background:var(--blue-500); }
.safety-remind-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-remind-page .badge.green .dot { background:var(--green-500); }
.safety-remind-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-remind-page .badge.red .dot { background:var(--red-500); }
.safety-remind-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-remind-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-remind-page .badge.gray .dot { background:var(--ink-400); }
.safety-remind-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-remind-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-remind-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
