<template>
  <div class="app-container hr-payroll-page">
    <!-- 筛选条件卡片 -->
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
          <label>批次编号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入批次编号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>薪资年月</label>
          <div class="control">
            <el-input v-model="queryParams.yearMonth" placeholder="如 2025-01" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次名称</label>
          <div class="control">
            <el-input v-model="queryParams.batchName" placeholder="请输入批次名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>发薪日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发薪银行</label>
          <div class="control">
            <el-input v-model="queryParams.bankName" placeholder="请输入发薪银行" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>审批状态</label>
          <div class="control">
            <el-input v-model="queryParams.auditStatus" placeholder="请输入审批状态" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:payroll:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:payroll:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:payroll:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:payroll:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_payroll_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrPayrollBatchList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="批次编号" prop="batchNo" key="batchNo" align="center" :width="colWidth('batchNo', 150)" resizable v-if="columns.batchNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.batchNo }}</span></template>
          </el-table-column>
          <el-table-column label="薪资年月" prop="yearMonth" key="yearMonth" align="center" :width="colWidth('yearMonth', 100)" resizable v-if="columns.yearMonth.visible" />
          <el-table-column label="批次名称" prop="batchName" key="batchName" align="center" :width="colWidth('batchName', 180)" resizable show-overflow-tooltip v-if="columns.batchName.visible" />
          <el-table-column label="发薪日期" prop="payDate" key="payDate" align="center" :width="colWidth('payDate', 120)" resizable v-if="columns.payDate.visible" />
          <el-table-column label="发薪银行" prop="bankName" key="bankName" align="center" :width="colWidth('bankName', 160)" resizable v-if="columns.bankName.visible" />
          <el-table-column label="总人数" prop="totalCount" key="totalCount" align="center" :width="colWidth('totalCount', 100)" resizable v-if="columns.totalCount.visible" />
          <el-table-column label="总金额" prop="totalAmount" key="totalAmount" align="center" :width="colWidth('totalAmount', 120)" resizable v-if="columns.totalAmount.visible" />
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span v-if="scope.row.auditStatus" class="badge" :class="auditBadgeClass(scope.row.auditStatus)"><span class="dot"></span>{{ auditStatusLabel(scope.row.auditStatus) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:payroll:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:payroll:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:payroll:remove']">删除</el-button>
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
      <el-form ref="hrPayrollBatchRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 卡片：基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="批次编号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="薪资年月" prop="yearMonth"><el-input v-model="form.yearMonth" placeholder="如 2025-01" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次名称" prop="batchName"><el-input v-model="form.batchName" placeholder="请输入批次名称" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：汇总与发放 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>汇总与发放</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="总人数" prop="totalCount"><el-input-number v-model="form.totalCount" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="总金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发薪银行" prop="bankName"><el-input v-model="form.bankName" placeholder="请输入发薪银行" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发薪日期" prop="payDate"><el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="银行回单号" prop="bankReceiptNo"><el-input v-model="form.bankReceiptNo" placeholder="请输入银行回单号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="封账状态" prop="sealedStatus"><el-input v-model="form.sealedStatus" placeholder="请输入封账状态" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 卡片：审批信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审批信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="审批状态" prop="auditStatus"><el-input v-model="form.auditStatus" placeholder="请输入审批状态" /></el-form-item></el-col>
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
            <div class="rd-detail-header-title">工资批次详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.batchNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">批次：{{ viewData.batchNo }}</span>
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
              <div class="rd-item"><span class="rd-label">批次编号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">薪资年月</span><div class="rd-value">{{ viewData.yearMonth || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">批次名称</span><div class="rd-value">{{ viewData.batchName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>汇总与发放</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">总人数</span><div class="rd-value">{{ viewData.totalCount != null ? viewData.totalCount : '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">总金额</span><div class="rd-value rd-value--large rd-amount">{{ viewData.totalAmount != null ? '￥' + formatAmount(viewData.totalAmount) : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发薪银行</span><div class="rd-value">{{ viewData.bankName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发薪日期</span><div class="rd-value">{{ viewData.payDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">银行回单号</span><div class="rd-value">{{ viewData.bankReceiptNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">封账状态</span><div class="rd-value"><dict-tag :options="sealedStatusOptions" :value="viewData.sealedStatus"/></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审批信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">审批状态</span><div class="rd-value"><dict-tag :options="auditStatusOptions" :value="viewData.auditStatus"/></div></div>
              <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ viewData.auditBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">审批时间</span><div class="rd-value">{{ viewData.auditTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">审批意见</span><div class="rd-value">{{ viewData.auditRemark || '-' }}</div></div>
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

<script setup name="HrPayrollBatch">
import { listHrPayrollBatch, getHrPayrollBatch, addHrPayrollBatch, updateHrPayrollBatch, delHrPayrollBatch } from '@/api/hr/hrPayrollBatch'
import { Filter, ArrowDown, Search, Edit, Delete, Download } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()

/** 字典：封账状态、审批状态 */
const { hr_sealed_status: sealedStatusOptions, hr_audit_status: auditStatusOptions } = proxy.useDict('hr_sealed_status', 'hr_audit_status')

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_payroll_index')

/** 详情弹窗卡片折叠 */
const { collapsedCards, toggleCard } = useDetailCard(['c0', 'c1', 'c2', 'c3'])

/** 列显隐配置 */
const defaultColumns = {
  batchNo: { label: '批次编号', visible: true },
  yearMonth: { label: '薪资年月', visible: true },
  batchName: { label: '批次名称', visible: true },
  payDate: { label: '发薪日期', visible: true },
  bankName: { label: '发薪银行', visible: true },
  totalCount: { label: '总人数', visible: true },
  totalAmount: { label: '总金额', visible: true },
  auditStatus: { label: '审批状态', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_payroll_columns')
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

const hrPayrollBatchList = ref([])
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, batchNo: undefined, yearMonth: undefined, batchName: undefined, bankName: undefined, auditStatus: undefined },
  rules: {
    yearMonth: [{ required: true, message: '薪资年月不能为空', trigger: 'blur' }],
    batchName: [{ required: true, message: '批次名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.batchNo) count++
  if (queryParams.value.yearMonth) count++
  if (queryParams.value.batchName) count++
  if (queryParams.value.bankName) count++
  if (queryParams.value.auditStatus) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

/** 审批状态徽标样式（关键字匹配，无字典） */
function auditBadgeClass(v) {
  const s = String(v)
  if (s.includes('驳回') || s.includes('不通过')) return 'red'
  if (s.includes('通过') || s.includes('已审批') || s.includes('完成')) return 'green'
  if (s.includes('待')) return 'amber'
  if (s.includes('审')) return 'blue'
  return 'gray'
}
function auditStatusLabel(v) {
  if (v == null || v === '') return '-'
  return String(v)
}

function getList() {
  loading.value = true
  listHrPayrollBatch(queryParams.value).then(response => {
    hrPayrollBatchList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value, 'PayDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.batchNo = undefined
  queryParams.value.yearMonth = undefined
  queryParams.value.batchName = undefined
  queryParams.value.bankName = undefined
  queryParams.value.auditStatus = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.batchId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    batchNo: undefined,
    yearMonth: undefined,
    batchName: undefined,
    payDate: undefined,
    bankName: undefined,
    totalCount: undefined,
    totalAmount: undefined,
    auditStatus: undefined,
    bankReceiptNo: undefined,
    sealedStatus: undefined
  }
  proxy.resetForm('hrPayrollBatchRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增工资批次'
}

function handleUpdate(row) {
  reset()
  const batchId = row.batchId || ids.value[0]
  getHrPayrollBatch(batchId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改工资批次'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const batchId = row.batchId || ids.value[0]
  getHrPayrollBatch(batchId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPayrollBatchRef'].validate(valid => {
    if (valid) {
      if (form.value.batchId != undefined) {
        updateHrPayrollBatch(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPayrollBatch(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const batchIds = row.batchId || ids.value
  proxy.$modal.confirm('是否确认删除选中的工资批次？').then(function() {
    return delHrPayrollBatch(batchIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/payroll/export', { ...queryParams.value }, `hrPayrollBatch_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-payroll-page {
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
.hr-payroll-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-payroll-page .filter-card { padding:14px 20px 16px; }
.hr-payroll-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-payroll-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-payroll-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-payroll-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-payroll-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-payroll-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-payroll-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-payroll-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-payroll-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-payroll-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-payroll-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-payroll-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-payroll-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-payroll-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-payroll-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.hr-payroll-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-payroll-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-payroll-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-payroll-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-payroll-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select transparent inside .control */
.hr-payroll-page .field .control :deep(.el-select) { width:100%; }
.hr-payroll-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-payroll-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-payroll-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-payroll-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-payroll-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-payroll-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-payroll-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-payroll-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-payroll-page .btn-soft .el-icon { font-size:14px; }
.hr-payroll-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-payroll-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-payroll-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-payroll-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-payroll-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-payroll-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-payroll-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-payroll-page .table-wrap { overflow-x:auto; }
.hr-payroll-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-payroll-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-payroll-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-payroll-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-payroll-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-payroll-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-payroll-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-payroll-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-payroll-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-payroll-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-payroll-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-payroll-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-payroll-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-payroll-page .badge.amber .dot { background:var(--amber-500); }
.hr-payroll-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-payroll-page .badge.blue .dot { background:var(--blue-500); }
.hr-payroll-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-payroll-page .badge.green .dot { background:var(--green-500); }
.hr-payroll-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-payroll-page .badge.red .dot { background:var(--red-500); }
.hr-payroll-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-payroll-page .badge.violet .dot { background:var(--brand-500); }
.hr-payroll-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-payroll-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-payroll-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-payroll-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-payroll-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-payroll-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-payroll-page .pagination-container :deep(.el-pagination .btn-prev), .hr-payroll-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-payroll-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-payroll-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-payroll-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

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
.hr-payroll-page .rd-page { max-height: 60vh; overflow-y: auto; }
.hr-payroll-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-payroll-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-payroll-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-payroll-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-payroll-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-payroll-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-payroll-page .rd-card-body { padding: 16px; }
.hr-payroll-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-payroll-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-payroll-page .rd-item--full { grid-column: 1 / -1; }
.hr-payroll-page .rd-label { font-size: 12px; color: #909399; }
.hr-payroll-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-payroll-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-payroll-page .rd-empty-icon { margin-bottom: 8px; }
.hr-payroll-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-payroll-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-payroll-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-payroll-page .rd-detail-header-main { flex: 1; }
.hr-payroll-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-payroll-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-payroll-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-payroll-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-payroll-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-payroll-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-payroll-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
