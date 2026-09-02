<template>
  <div class="app-container hr-tempSettlement-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>结算单号</label>
          <div class="control">
            <el-input v-model="queryParams.settlementNo" placeholder="请输入结算单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>劳务工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.workerName" placeholder="请输入劳务工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>结算日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:tempSettlement:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:tempSettlement:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:tempSettlement:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:tempSettlement:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_tempSettlement_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrTempDailySettlementList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="结算单号" prop="settlementNo" key="settlementNo" align="center" :width="colWidth('settlementNo', 150)" resizable v-if="columns.settlementNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.settlementNo }}</span></template>
          </el-table-column>
          <el-table-column label="结算日期" prop="settlementDate" key="settlementDate" align="center" :width="colWidth('settlementDate', 120)" resizable v-if="columns.settlementDate.visible" />
          <el-table-column label="劳务工ID" prop="laborId" key="laborId" align="center" :width="colWidth('laborId', 110)" resizable v-if="columns.laborId.visible" />
          <el-table-column label="劳务工姓名" prop="workerName" key="workerName" align="center" :width="colWidth('workerName', 110)" resizable v-if="columns.workerName.visible" />
          <el-table-column label="出勤天数" prop="attendDays" key="attendDays" align="center" :width="colWidth('attendDays', 100)" resizable v-if="columns.attendDays.visible" />
          <el-table-column label="日薪" prop="dailyWage" key="dailyWage" align="center" :width="colWidth('dailyWage', 110)" resizable v-if="columns.dailyWage.visible" />
          <el-table-column label="计件金额" prop="pieceworkAmount" key="pieceworkAmount" align="center" :width="colWidth('pieceworkAmount', 120)" resizable v-if="columns.pieceworkAmount.visible" />
          <el-table-column label="结算总额" prop="totalAmount" key="totalAmount" align="center" :width="colWidth('totalAmount', 120)" resizable v-if="columns.totalAmount.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:tempSettlement:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:tempSettlement:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:tempSettlement:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrTempDailySettlementRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="结算单号" prop="settlementNo"><el-input v-model="form.settlementNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="结算日期" prop="settlementDate"><el-date-picker v-model="form.settlementDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="劳务工ID" prop="laborId"><el-input-number v-model="form.laborId" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="劳务工姓名" prop="workerName"><el-input v-model="form.workerName" placeholder="请输入劳务工姓名" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 结算信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>结算信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="出勤天数" prop="attendDays"><el-input-number v-model="form.attendDays" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="日薪" prop="dailyWage"><el-input-number v-model="form.dailyWage" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计件金额" prop="pieceworkAmount"><el-input-number v-model="form.pieceworkAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="结算总额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 发放信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>发放信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="支付状态" prop="payStatus"><el-select v-model="form.payStatus" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in payStatusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="支付日期" prop="payDate"><el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="支付方式" prop="payMethod"><el-input v-model="form.payMethod" placeholder="请输入支付方式" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">临时工日结算详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.settlementNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">单号：{{ viewData.settlementNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">结算单号</span><div class="rd-value">{{ viewData.settlementNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结算日期</span><div class="rd-value">{{ viewData.settlementDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">劳务工ID</span><div class="rd-value">{{ viewData.laborId != null ? viewData.laborId : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">劳务工姓名</span><div class="rd-value">{{ viewData.workerName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>结算信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">出勤天数</span><div class="rd-value">{{ viewData.attendDays != null ? viewData.attendDays : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">日薪</span><div class="rd-value rd-amount">{{ viewData.dailyWage != null ? '￥' + formatAmount(viewData.dailyWage) : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计件金额</span><div class="rd-value rd-amount">{{ viewData.pieceworkAmount != null ? '￥' + formatAmount(viewData.pieceworkAmount) : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结算总额</span><div class="rd-value rd-amount">{{ viewData.totalAmount != null ? '￥' + formatAmount(viewData.totalAmount) : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>发放信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">支付状态</span><div class="rd-value"><dict-tag :options="payStatusOptions" :value="viewData.payStatus"/></div></div>
              <div class="rd-item"><span class="rd-label">支付日期</span><div class="rd-value">{{ viewData.payDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">支付方式</span><div class="rd-value">{{ viewData.payMethod || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c3">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrTempDailySettlement">
import { listHrTempDailySettlement, getHrTempDailySettlement, addHrTempDailySettlement, updateHrTempDailySettlement, delHrTempDailySettlement } from '@/api/hr/hrTempDailySettlement'
import { Filter, Search, Edit, Delete, Download } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()

/** 字典：支付状态 */
const { hr_pay_status: payStatusOptions } = proxy.useDict('hr_pay_status')

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_tempSettlement_index')

/** 详情弹窗卡片折叠 */
const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1', 'c2', 'c3', 'e0', 'e1', 'e2'])

/** 列显隐配置 */
const defaultColumns = {
  settlementNo: { label: '结算单号', visible: true },
  settlementDate: { label: '结算日期', visible: true },
  laborId: { label: '劳务工ID', visible: true },
  workerName: { label: '劳务工姓名', visible: true },
  attendDays: { label: '出勤天数', visible: true },
  dailyWage: { label: '日薪', visible: true },
  pieceworkAmount: { label: '计件金额', visible: true },
  totalAmount: { label: '结算总额', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_tempSettlement_columns')
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
const columns = ref(loadColumnVisibility())

const hrTempDailySettlementList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, settlementNo: undefined, workerName: undefined },
  rules: {
    settlementDate: [{ required: true, message: '结算日期不能为空', trigger: 'blur' }],
    laborId: [{ required: true, message: '劳务工ID不能为空', trigger: 'blur' }],
    workerName: [{ required: true, message: '劳务工姓名不能为空', trigger: 'blur' }],
    attendDays: [{ required: true, message: '出勤天数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.settlementNo) count++
  if (queryParams.value.workerName) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrTempDailySettlement(queryParams.value).then(response => {
    hrTempDailySettlementList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  proxy.addDateRange(queryParams.value, dateRange.value, 'SettlementDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.settlementNo = undefined
  queryParams.value.workerName = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.settlementId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    settlementNo: undefined,
    settlementDate: undefined,
    laborId: undefined,
    workerName: undefined,
    attendDays: undefined,
    dailyWage: undefined,
    pieceworkAmount: undefined,
    totalAmount: undefined,
    payStatus: undefined,
    payDate: undefined,
    payMethod: undefined
  }
  proxy.resetForm('hrTempDailySettlementRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增临时工日结算'
}

function handleUpdate(row) {
  reset()
  const settlementId = row.settlementId || ids.value[0]
  getHrTempDailySettlement(settlementId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改临时工日结算'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const settlementId = row.settlementId || ids.value[0]
  getHrTempDailySettlement(settlementId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrTempDailySettlementRef'].validate(valid => {
    if (valid) {
      if (form.value.settlementId != undefined) {
        updateHrTempDailySettlement(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrTempDailySettlement(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const settlementIds = row.settlementId || ids.value
  proxy.$modal.confirm('是否确认删除选中的临时工日结算？').then(function() {
    return delHrTempDailySettlement(settlementIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/tempSettlement/export', { ...queryParams.value }, `hrTempDailySettlement_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-tempSettlement-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.hr-tempSettlement-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-tempSettlement-page .filter-card { padding:14px 20px 16px; }
.hr-tempSettlement-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-tempSettlement-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-tempSettlement-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-tempSettlement-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-tempSettlement-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-tempSettlement-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-tempSettlement-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-tempSettlement-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-tempSettlement-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-tempSettlement-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-tempSettlement-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.hr-tempSettlement-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-tempSettlement-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-tempSettlement-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-tempSettlement-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-tempSettlement-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select transparent inside .control */
.hr-tempSettlement-page .field .control :deep(.el-select) { width:100%; }
.hr-tempSettlement-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-tempSettlement-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-tempSettlement-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-tempSettlement-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-tempSettlement-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-tempSettlement-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-tempSettlement-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-tempSettlement-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-tempSettlement-page .btn-soft .el-icon { font-size:14px; }
.hr-tempSettlement-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-tempSettlement-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-tempSettlement-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-tempSettlement-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-tempSettlement-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-tempSettlement-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-tempSettlement-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-tempSettlement-page .table-wrap { overflow-x:auto; }
.hr-tempSettlement-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-tempSettlement-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-tempSettlement-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-tempSettlement-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-tempSettlement-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-tempSettlement-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-tempSettlement-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-tempSettlement-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-tempSettlement-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-tempSettlement-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-tempSettlement-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-tempSettlement-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-tempSettlement-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-tempSettlement-page .badge.amber .dot { background:var(--amber-500); }
.hr-tempSettlement-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-tempSettlement-page .badge.blue .dot { background:var(--blue-500); }
.hr-tempSettlement-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-tempSettlement-page .badge.green .dot { background:var(--green-500); }
.hr-tempSettlement-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-tempSettlement-page .badge.red .dot { background:var(--red-500); }
.hr-tempSettlement-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-tempSettlement-page .badge.violet .dot { background:var(--brand-500); }
.hr-tempSettlement-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-tempSettlement-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-tempSettlement-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-tempSettlement-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-tempSettlement-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-tempSettlement-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-tempSettlement-page .pagination-container :deep(.el-pagination .btn-prev), .hr-tempSettlement-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-tempSettlement-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-tempSettlement-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-tempSettlement-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 详情弹窗（rd-dialog）样式 ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
.hr-tempSettlement-page .rd-page { max-height: 60vh; overflow-y: auto; }
.hr-tempSettlement-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-tempSettlement-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-tempSettlement-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-tempSettlement-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-tempSettlement-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-tempSettlement-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-tempSettlement-page .rd-card-body { padding: 16px; }
.hr-tempSettlement-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-tempSettlement-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-tempSettlement-page .rd-item--full { grid-column: 1 / -1; }
.hr-tempSettlement-page .rd-label { font-size: 12px; color: #909399; }
.hr-tempSettlement-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-tempSettlement-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-tempSettlement-page .rd-empty-icon { margin-bottom: 8px; }
.hr-tempSettlement-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-tempSettlement-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-tempSettlement-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-tempSettlement-page .rd-detail-header-main { flex: 1; }
.hr-tempSettlement-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-tempSettlement-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-tempSettlement-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-tempSettlement-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-tempSettlement-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-tempSettlement-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-tempSettlement-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
