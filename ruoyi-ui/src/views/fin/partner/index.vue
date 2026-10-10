<template>
  <div class="app-container fin-partner-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>单位名称</label>
          <div class="control">
            <el-input v-model="queryParams.partnerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>单位类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.partnerType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="供应商" value="SUPPLIER" />
              <el-option label="客户" value="CUSTOMER" />
              <el-option label="既是供应商又是客户" value="BOTH" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['fin:partner:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="singleSelection === null" @click="handleUpdate" v-hasPermi="['fin:partner:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="selection.length === 0" @click="handleDelete" v-hasPermi="['fin:partner:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['fin:partner:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_partner_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="partnerList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="单位编码" prop="partnerCode" key="partnerCode" :width="colWidth('partnerCode', 140)" resizable align="center" show-overflow-tooltip v-if="columns.partnerCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partnerCode }}</span></template>
          </el-table-column>
          <el-table-column label="单位名称" prop="partnerName" key="partnerName" :min-width="colWidth('partnerName', 200)" resizable align="center" show-overflow-tooltip v-if="columns.partnerName.visible" />
          <el-table-column label="单位类型" prop="partnerType" key="partnerType" :width="colWidth('partnerType', 150)" resizable align="center" v-if="columns.partnerType.visible">
            <template #default="scope">
              <span class="badge violet">{{ typeLabel(scope.row.partnerType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="税号" prop="taxNumber" key="taxNumber" :width="colWidth('taxNumber', 180)" resizable align="center" show-overflow-tooltip v-if="columns.taxNumber.visible">
            <template #default="scope">{{ scope.row.taxNumber || '-' }}</template>
          </el-table-column>
          <el-table-column label="账期" prop="paymentDays" key="paymentDays" :width="colWidth('paymentDays', 90)" resizable align="center" v-if="columns.paymentDays.visible">
            <template #default="scope">{{ scope.row.paymentDays ? scope.row.paymentDays + ' 天' : '现结' }}</template>
          </el-table-column>
          <el-table-column label="信用额度" prop="creditLimit" key="creditLimit" :width="colWidth('creditLimit', 130)" resizable align="right" class-name="col-num" v-if="columns.creditLimit.visible">
            <template #default="scope"><span class="rd-amount">{{ formatAmount(scope.row.creditLimit) }}</span></template>
          </el-table-column>
          <el-table-column label="可用额度" prop="creditAvailable" key="creditAvailable" :width="colWidth('creditAvailable', 130)" resizable align="right" class-name="col-num" v-if="columns.creditAvailable.visible">
            <template #default="scope">
              <span class="rd-amount" :class="{ 'rd-amount--negative': Number(scope.row.creditLimit - scope.row.creditUsed) < 0 }">
                {{ formatAmount(Number(scope.row.creditLimit || 0) - Number(scope.row.creditUsed || 0)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['fin:partner:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDeleteRow(scope.row)" v-hasPermi="['fin:partner:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改弹窗 ===== -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l7-4 7 4v14"/><path d="M9 21v-6h6v6"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ dialogTitle }}</span>
        </div>
      </template>
      <el-form ref="partnerFormRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位名称" prop="partnerName">
              <el-input v-model="form.partnerName" placeholder="请输入单位名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位编码" prop="partnerCode">
              <el-input v-model="form.partnerCode" placeholder="请输入单位编码" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位类型" prop="partnerType">
              <el-select v-model="form.partnerType" placeholder="请选择" style="width: 100%">
                <el-option label="供应商" value="SUPPLIER" />
                <el-option label="客户" value="CUSTOMER" />
                <el-option label="既是供应商又是客户" value="BOTH" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税号" prop="taxNumber">
              <el-input v-model="form.taxNumber" placeholder="请输入税号" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票抬头" prop="invoiceTitle">
              <el-input v-model="form.invoiceTitle" placeholder="请输入发票抬头" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账期(天)" prop="paymentDays">
              <el-input-number v-model="form.paymentDays" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算方式" prop="settlementType">
              <el-select v-model="form.settlementType" placeholder="请选择" clearable style="width: 100%">
                <el-option label="现结" value="0" />
                <el-option label="月结" value="1" />
                <el-option label="预付" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="信用额度" prop="creditLimit">
              <el-input-number v-model="form.creditLimit" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗（独立只读） ===== -->
    <el-dialog v-model="viewOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">往来单位详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.partnerCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.partnerCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">单位编码</span>
                <div class="rd-value">{{ viewData.partnerCode || '暂无' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">单位名称</span>
                <div class="rd-value">{{ viewData.partnerName || '暂无' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">单位类型</span>
                <div class="rd-value"><span class="badge" :class="typeBadgeClass(viewData.partnerType)">{{ typeLabel(viewData.partnerType) }}</span></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">状态</span>
                <div class="rd-value">
                  <span class="badge" :class="statusBadgeClass(viewData.status)"><span class="dot"></span>{{ viewData.status === '0' ? '正常' : '停用' }}</span>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">税号</span>
                <div class="rd-value">{{ viewData.taxNumber || '暂无' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">发票抬头</span>
                <div class="rd-value">{{ viewData.invoiceTitle || '暂无' }}</div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>结算信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">账期</span>
                <div class="rd-value">{{ viewData.paymentDays ? viewData.paymentDays + ' 天' : '现结' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">结算方式</span>
                <div class="rd-value">{{ settlementLabel(viewData.settlementType) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">信用额度</span>
                <div class="rd-value"><span class="rd-amount">{{ formatAmount(viewData.creditLimit) }}</span></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">已用额度</span>
                <div class="rd-value">{{ formatAmount(viewData.creditUsed) }}</div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value">{{ viewData.remark || '暂无' }}</div>
              </div>
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

<script setup name="FinPartner">
import { listPartner, addPartner, updatePartner, delPartner } from '@/api/fin/partner'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_partner_index')

const loading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const partnerList = ref([])
const total = ref(0)
const selection = ref([])
const singleSelection = ref(null)
const editingId = ref(null)

const form = reactive({})
const rules = {
  partnerName: [{ required: true, message: '单位名称不能为空', trigger: 'blur' }],
  partnerType: [{ required: true, message: '单位类型不能为空', trigger: 'change' }]
}
const queryParams = ref({ pageNum: 1, pageSize: 20, partnerName: undefined, partnerType: undefined, status: undefined })

const dialogTitle = computed(() => (editingId.value ? '修改往来单位' : '新增往来单位'))

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  partnerCode: { label: '单位编码', visible: true },
  partnerName: { label: '单位名称', visible: true },
  partnerType: { label: '单位类型', visible: true },
  taxNumber: { label: '税号', visible: true },
  paymentDays: { label: '账期', visible: true },
  creditLimit: { label: '信用额度', visible: true },
  creditAvailable: { label: '可用额度', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('fin_partner_columns')
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

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.partnerName) count++
  if (queryParams.value.partnerType) count++
  if (queryParams.value.status) count++
  return count
})

function formatAmount(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function typeLabel(t) {
  return { SUPPLIER: '供应商', CUSTOMER: '客户', BOTH: '既是供应商又是客户' }[t] || t || '暂无'
}

function typeBadgeClass(t) {
  return { SUPPLIER: 'violet', CUSTOMER: 'blue', BOTH: 'amber' }[t] || 'gray'
}

function statusBadgeClass(s) {
  return s === '0' ? 'green' : 'gray'
}

function settlementLabel(t) {
  return { '0': '现结', '1': '月结', '2': '预付' }[t] || '暂无'
}

function getList() {
  loading.value = true
  listPartner(queryParams.value).then(res => {
    partnerList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, partnerName: undefined, partnerType: undefined, status: undefined }
  getList()
}

function handleSelectionChange(rows) { selection.value = rows }
function handleCurrentChange(row) { singleSelection.value = row }

function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

function handleAdd() {
  editingId.value = null
  form.partnerName = undefined
  form.partnerType = 'SUPPLIER'
  form.partnerCode = undefined
  form.taxNumber = undefined
  form.paymentDays = 0
  form.settlementType = undefined
  form.invoiceTitle = undefined
  form.creditLimit = 0
  form.status = '0'
  form.remark = undefined
  open.value = true
}

function handleUpdate(row) {
  const target = row || singleSelection.value
  if (!target) {
    proxy.$modal.msgWarning('请先选择要修改的往来单位')
    return
  }
  editingId.value = target.partnerId
  Object.assign(form, target)
  open.value = true
}

function submitForm() {
  proxy.$refs['partnerFormRef'].validate(valid => {
    if (!valid) return
    const req = editingId.value ? updatePartner : addPartner
    req(form).then(() => {
      proxy.$modal.msgSuccess(editingId.value ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete() {
  const ids = selection.value.map(r => r.partnerId)
  proxy.$modal.confirm(`是否确认删除选中的 ${ids.length} 个往来单位？`).then(() => {
    return delPartner(ids)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleDeleteRow(row) {
  proxy.$modal.confirm(`是否确认删除往来单位「${row.partnerName}」？`).then(() => {
    return delPartner([row.partnerId])
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/fin/partner/export', { ...queryParams.value }, `财务往来单位_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.fin-partner-page {
  --brand: #4b7bec;
  --brand-light: #e8f1ff;
  --ink: #22304a;
  --ink-light: #5b6b87;
  --muted: #8a97ad;
  --line: #e4e9f2;
  --bg: #f5f7fb;
  --amber: #d97706;
  --amber-bg: #fef3c7;
  --blue: #2563eb;
  --blue-bg: #dbeafe;
  --green: #059669;
  --green-bg: #d1fae5;
  --red: #dc2626;
  --red-bg: #fee2e2;
  --violet: #7c3aed;
  --violet-bg: #ede9fe;
  --gray: #6b7280;
  --gray-bg: #f3f4f6;
}

/* ===== Surface 卡片 ===== */
.fin-partner-page .surface {
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 14px;
  box-shadow: 0 1px 2px rgba(34, 48, 74, 0.04);
}

/* ===== 筛选卡片 ===== */
.filter-card { padding: 14px 16px 16px; }
.filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.filter-title { display: inline-flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: var(--ink); }
.filter-title .glyph { display: inline-flex; width: 6px; height: 16px; border-radius: 3px; background: linear-gradient(180deg, var(--brand), #74a4ff); }
.adv-link { color: var(--brand); font-size: 13px; cursor: pointer; }
.adv-link:hover { text-decoration: underline; }
.filter-bar { display: flex; flex-wrap: wrap; gap: 12px 18px; align-items: center; }
.filter-bar .field { display: inline-flex; align-items: center; gap: 8px; }
.filter-bar .field > label { font-size: 13px; color: var(--ink-light); white-space: nowrap; }
.filter-bar .control { width: 200px; }
.filter-actions { display: flex; align-items: center; gap: 10px; }
.filter-info { font-size: 12px; color: var(--muted); }
.filter-info .n { color: var(--brand); font-weight: 600; }
.filter-buttons { display: flex; gap: 10px; }

/* 控件透明化，融入卡片背景 */
.fin-partner-page :deep(.filter-card .el-input__wrapper),
.fin-partner-page :deep(.filter-card .el-select__wrapper) {
  background: #fff;
  box-shadow: 0 0 0 1px var(--line) inset;
}
.fin-partner-page :deep(.filter-card .el-input__wrapper.is-focus),
.fin-partner-page :deep(.filter-card .el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--brand) inset;
}

/* ===== 工具栏 ===== */
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.toolbar .left { display: flex; align-items: center; gap: 10px; }
.toolbar-divider { width: 1px; height: 18px; background: var(--line); margin: 0 2px; }

.btn-soft {
  display: inline-flex; align-items: center; gap: 5px;
  height: 32px; padding: 0 14px; border-radius: 8px;
  font-size: 13px; cursor: pointer; border: 1px solid var(--line);
  background: #fff; color: var(--ink-light); transition: all 0.15s ease;
}
.btn-soft:hover:not(:disabled) { border-color: var(--brand); color: var(--brand); background: var(--brand-light); }
.btn-soft:disabled { opacity: 0.45; cursor: not-allowed; }
.btn-soft.is-danger-outline:hover:not(:disabled) { border-color: var(--red); color: var(--red); background: var(--red-bg); }
.btn-soft .el-icon { font-size: 14px; }

.col-mono { font-family: 'JetBrains Mono', Consolas, monospace; font-size: 13px; color: var(--ink); }
.fin-partner-page :deep(.el-table .col-num) { font-variant-numeric: tabular-nums; }
.rd-amount--negative { color: var(--red) !important; }

/* ===== 表格 ===== */
.table-wrap { width: 100%; }
.fin-partner-page :deep(.el-table) { --el-table-border-color: var(--line); }
.fin-partner-page :deep(.el-table th.el-table__cell) {
  background: #f7f9fc;
  color: var(--ink);
  font-weight: 600;
  height: 44px;
}
.fin-partner-page :deep(.el-table td.el-table__cell) { color: var(--ink-light); }
.fin-partner-page :deep(.el-table .el-table__header th .cell) { display: inline-flex; align-items: center; justify-content: center; }

/* 列拖拽手柄 */
.fin-partner-page :deep(.el-table th .col-resize-handle) {
  position: absolute; top: 0; right: -4px; width: 8px; height: 100%;
  cursor: col-resize; z-index: 3;
}

/* ===== 徽章 ===== */
.badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 2px 10px; border-radius: 999px;
  font-size: 12px; line-height: 20px; font-weight: 500; white-space: nowrap;
}
.badge .dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.badge.green { color: var(--green); background: var(--green-bg); }
.badge.gray { color: var(--gray); background: var(--gray-bg); }
.badge.amber { color: var(--amber); background: var(--amber-bg); }
.badge.blue { color: var(--blue); background: var(--blue-bg); }
.badge.violet { color: var(--violet); background: var(--violet-bg); }
.badge.red { color: var(--red); background: var(--red-bg); }

.amount-cell { font-variant-numeric: tabular-nums; color: var(--ink); }
.text-danger { color: var(--red); }

/* ===== 操作列 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 分页 ===== */
.pagination-container {
  display: flex; justify-content: flex-end; align-items: center;
  padding-top: 14px; margin-top: 4px; border-top: 1px dashed var(--line);
}

/* ===== 弹窗（rd-dialog）===== */
.fin-partner-page :deep(.rd-dialog) { border-radius: 12px; overflow: hidden; }
.fin-partner-page :deep(.rd-dialog .el-dialog__header) {
  padding: 0; margin: 0 0 4px;
}
.fin-partner-page :deep(.rd-dialog .el-dialog__headerbtn) {
  top: 16px; right: 16px; width: 28px; height: 28px;
}
.fin-partner-page :deep(.rd-dialog .el-dialog__body) { padding: 0 20px 8px; }

.rd-detail-header {
  display: flex; align-items: center; gap: 10px;
  padding: 14px 20px;
  background: linear-gradient(90deg, var(--brand-light), #f6f9ff);
  border-bottom: 1px solid var(--line);
}
.rd-detail-header-icon {
  width: 34px; height: 34px; border-radius: 8px;
  display: inline-flex; align-items: center; justify-content: center;
  color: var(--brand); background: #fff;
  box-shadow: 0 1px 3px rgba(75, 123, 236, 0.25);
}
.rd-detail-header-title { font-size: 16px; font-weight: 600; color: var(--ink); }
.rd-detail-header-sub { display: inline-flex; align-items: center; gap: 10px; margin-left: 4px; }
.rd-detail-header-divider { width: 1px; height: 14px; background: var(--line); }
.rd-detail-header-no { font-size: 12px; color: var(--ink-light); }

/* ===== 详情页 rd- 体系 ===== */
.rd-page { display: flex; flex-direction: column; gap: 14px; padding: 4px 0 8px; }
.rd-card {
  background: #fff; border: 1px solid var(--line); border-radius: 10px;
  box-shadow: 0 1px 2px rgba(34, 48, 74, 0.04);
  animation: rdFadeIn 0.25s ease;
}
.rd-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; border-bottom: 1px solid var(--line);
  background: #fafbfd; border-radius: 10px 10px 0 0;
}
.rd-card-title { display: inline-flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink); }
.rd-card-icon {
  width: 28px; height: 28px; border-radius: 7px;
  display: inline-flex; align-items: center; justify-content: center;
  color: var(--brand); background: var(--brand-light);
}
.rd-card-body { padding: 16px; }
.rd-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px 20px; }
.rd-item { min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { font-size: 12px; color: var(--muted); }
.rd-value { font-size: 14px; color: var(--ink); line-height: 1.5; word-break: break-all; }
.rd-amount { font-variant-numeric: tabular-nums; font-weight: 600; color: var(--brand); }

.rd-empty { padding: 32px 0; text-align: center; color: var(--muted); font-size: 13px; }

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .rd-grid { grid-template-columns: 1fr; }
  .filter-bar .control { width: 100%; }
}
</style>
