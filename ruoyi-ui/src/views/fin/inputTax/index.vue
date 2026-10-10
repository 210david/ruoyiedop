<template>
  <div class="app-container fin-inputtax-page">
    <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 12px">
      <template #title>
        <span class="hint-text">
          进项税额须在<b>认证抵扣期内</b>（开票日起 12 个月）使用，逾期不得抵扣。
          系统会把超期未抵扣的记录标记为「已过期」，避免长期挂账虚增待抵扣进项税。
        </span>
      </template>
    </el-alert>

    <!-- ===== 筛选条件 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>发票号码</label>
          <div class="control">
            <el-input v-model="queryParams.invoiceNo" placeholder="模糊搜索" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.partnerName" placeholder="模糊搜索" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>抵扣状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.deductStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option label="待抵扣" value="PENDING" />
              <el-option label="已抵扣" value="DEDUCTED" />
              <el-option label="已过期" value="EXPIRED" />
              <el-option label="已作废" value="VOID" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>开票日期</label>
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

    <!-- 汇总卡片 -->
    <el-row :gutter="12" style="margin-bottom: 12px">
      <el-col :span="6">
        <div class="mini-card">
          <div class="mini-label">待抵扣税额</div>
          <div class="mini-value text-warning">{{ fmt(sumOf('PENDING')) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="mini-card">
          <div class="mini-label">已抵扣税额</div>
          <div class="mini-value text-success">{{ fmt(sumOf('DEDUCTED')) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="mini-card">
          <div class="mini-label">已过期（不得抵扣）</div>
          <div class="mini-value text-danger">{{ fmt(sumOf('EXPIRED')) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="mini-card">
          <div class="mini-label">超期未处理笔数</div>
          <div class="mini-value" :class="overdueCount > 0 ? 'text-danger' : ''">{{ overdueCount }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 即将过期预警 -->
    <div v-if="expiringList.length" class="surface" style="margin-bottom: 12px">
      <div class="toolbar">
        <div class="left">
          <el-icon color="var(--amber-500)"><Warning /></el-icon>
          <span class="text-warning">即将过期的待抵扣进项税（{{ expiringList.length }} 笔，30 天内）</span>
        </div>
      </div>
      <div class="table-wrap">
        <el-table :data="expiringList" size="small" border>
          <el-table-column prop="invoiceNo" label="发票号码" width="160" align="center" />
          <el-table-column prop="partnerName" label="供应商" min-width="160" align="center" show-overflow-tooltip />
          <el-table-column prop="invoiceDate" label="开票日期" width="120" align="center" />
          <el-table-column prop="deductDeadline" label="抵扣截止" width="120" align="center" />
          <el-table-column label="进项税额" width="130" align="center">
            <template #default="{ row }">
              <span class="rd-amount">{{ fmt(row.taxAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- ===== 数据列表 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleRefresh" v-hasPermi="['fin:ap:inputTax']">刷新</el-button>
          <el-button icon="Warning" type="warning" :disabled="overdueCount === 0" @click="handleMarkExpired" v-hasPermi="['fin:ap:inputTax']">
            处理超期（{{ overdueCount }}）
          </el-button>
          <el-button icon="Download" @click="handleExport">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_inputtax_index" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="pagedList" border @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="发票号码" prop="invoiceNo" key="invoiceNo" :min-width="colWidth('invoiceNo', 160)" resizable align="center" show-overflow-tooltip v-if="columns.invoiceNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.invoiceNo }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="partnerName" key="partnerName" :min-width="colWidth('partnerName', 180)" resizable align="center" show-overflow-tooltip v-if="columns.partnerName.visible" />
          <el-table-column label="开票日期" prop="invoiceDate" key="invoiceDate" :width="colWidth('invoiceDate', 120)" resizable align="center" v-if="columns.invoiceDate.visible" />
          <el-table-column label="税率" prop="taxRate" key="taxRate" :width="colWidth('taxRate', 90)" resizable align="center" v-if="columns.taxRate.visible">
            <template #default="{ row }">{{ pct(row.taxRate) }}</template>
          </el-table-column>
          <el-table-column label="不含税金额" prop="amountExclTax" key="amountExclTax" :width="colWidth('amountExclTax', 130)" resizable align="center" v-if="columns.amountExclTax.visible">
            <template #default="{ row }"><span class="rd-amount-plain">{{ fmt(row.amountExclTax) }}</span></template>
          </el-table-column>
          <el-table-column label="进项税额" prop="taxAmount" key="taxAmount" :width="colWidth('taxAmount', 130)" resizable align="center" v-if="columns.taxAmount.visible">
            <template #default="{ row }"><span class="rd-amount">{{ fmt(row.taxAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="抵扣截止" prop="deductDeadline" key="deductDeadline" :width="colWidth('deductDeadline', 120)" resizable align="center" v-if="columns.deductDeadline.visible">
            <template #default="{ row }">
              <span :class="deadlineClass(row)">{{ row.deductDeadline || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="deductStatus" key="deductStatus" :width="colWidth('deductStatus', 110)" resizable align="center" v-if="columns.deductStatus.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.deductStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.deductStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="抵扣期间" prop="deductPeriod" key="deductPeriod" :width="colWidth('deductPeriod', 110)" resizable align="center" v-if="columns.deductPeriod.visible">
            <template #default="{ row }">{{ row.deductPeriod ? formatPeriod(row.deductPeriod) : '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="success" icon="Check" v-if="scope.row.deductStatus === 'PENDING'" v-hasPermi="['fin:ap:inputTax']" @click="handleDeduct(scope.row)">抵扣</el-button>
                <el-button link type="danger" icon="Delete" v-if="scope.row.deductStatus === 'PENDING'" v-hasPermi="['fin:ap:inputTax']" @click="handleVoid(scope.row)">作废</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination v-show="total > 0" :total="total" v-model:current-page="pageNum" v-model:page-size="pageSize"
          :page-sizes="[20, 50, 100, 200]" layout="total, sizes, prev, pager, next, jumper" @size-change="pageNum = 1" />
      </div>
    </div>

    <!-- ===== 抵扣登记弹窗 ===== -->
    <el-dialog v-model="showDeduct" width="480px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg></div>
          <span class="rd-detail-header-title">登记进项税抵扣</span>
        </div>
      </template>
      <el-form label-width="110px">
        <el-form-item label="发票号码">
          <el-input :model-value="current?.invoiceNo" disabled />
        </el-form-item>
        <el-form-item label="进项税额">
          <el-input :model-value="fmt(current?.taxAmount)" disabled />
        </el-form-item>
        <el-form-item label="抵扣所属期间" required>
          <el-select v-model="deductPeriod" style="width: 100%">
            <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
          </el-select>
          <div class="form-tip">抵扣期间决定计入哪个会计期间的增值税</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitDeduct">确 定</el-button>
        <el-button @click="showDeduct = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg></div>
          <span class="rd-detail-header-title">进项税详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.invoiceNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">发票号码：{{ viewData.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">发票号码</span>
                <div class="rd-value">{{ viewData.invoiceNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">供应商</span>
                <div class="rd-value">{{ viewData.partnerName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">开票日期</span>
                <div class="rd-value">{{ viewData.invoiceDate || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">抵扣截止</span>
                <div class="rd-value">{{ viewData.deductDeadline || '-' }}</div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>税额信息</div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">税率</span>
                <div class="rd-value">{{ pct(viewData.taxRate) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">不含税金额</span>
                <div class="rd-value">{{ fmt(viewData.amountExclTax) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">进项税额</span>
                <div class="rd-value rd-amount">{{ fmt(viewData.taxAmount) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">抵扣期间</span>
                <div class="rd-value">{{ viewData.deductPeriod ? formatPeriod(viewData.deductPeriod) : '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">抵扣状态</span>
                <div class="rd-value">
                  <span class="badge" :class="badgeClass(viewData.deductStatus)">
                    <span class="dot"></span>{{ statusLabel(viewData.deductStatus) }}
                  </span>
                </div>
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

<script setup name="FinInputTax">
import {
  listInputTax, deductInputTax, voidInputTax, markInputTaxExpired,
  inputTaxSummary, expiringInputTax
} from '@/api/fin/configExt'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_inputtax_index')

const loading = ref(false)
const list = ref([])
const summary = ref([])
const expiringList = ref([])
const overdueCount = ref(0)
const showSearch = ref(true)
const dateRange = ref([])
const queryParams = ref({ partnerName: '', invoiceNo: '', deductStatus: '' })
const showDeduct = ref(false)
const deductPeriod = ref('')
const current = ref(null)
const viewOpen = ref(false)
const viewData = ref({})

/** 前端分页（后端一次拉取，本地分页展示） */
const pageNum = ref(1)
const pageSize = ref(20)
const total = computed(() => list.value.length)
const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return list.value.slice(start, start + pageSize.value)
})

/** 列显隐配置 */
const columns = ref({
  invoiceNo: { visible: true },
  partnerName: { visible: true },
  invoiceDate: { visible: true },
  taxRate: { visible: true },
  amountExclTax: { visible: true },
  taxAmount: { visible: true },
  deductDeadline: { visible: true },
  deductStatus: { visible: true },
  deductPeriod: { visible: true }
})

function loadColumnVisibility() {
  try {
    const saved = JSON.parse(localStorage.getItem('fin_inputtax_index:columns') || 'null')
    if (saved && typeof saved === 'object') {
      Object.keys(columns.value).forEach(k => {
        if (saved[k] && typeof saved[k].visible === 'boolean') columns.value[k].visible = saved[k].visible
      })
    }
  } catch (e) { /* 忽略本地缓存解析失败 */ }
}

const activeFilterCount = computed(() => {
  let n = 0
  if (queryParams.value.invoiceNo) n++
  if (queryParams.value.partnerName) n++
  if (queryParams.value.deductStatus) n++
  if (dateRange.value && dateRange.value.length === 2) n++
  return n
})

/** 可选抵扣期间：本期 + 上期（增值税通常按月申报） */
const periodOptions = computed(() => {
  const now = new Date()
  const arr = []
  for (let i = 0; i < 6; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    arr.push(`${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return arr
})

function fmt(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function pct(v) {
  if (v === null || v === undefined) return '—'
  return (Number(v) * 100).toFixed(2).replace(/\.00$/, '') + '%'
}

function formatPeriod(p) {
  if (!p || p.length !== 6) return p
  return `${p.slice(0, 4)}年${p.slice(4)}月`
}

function statusLabel(s) {
  return { PENDING: '待抵扣', DEDUCTED: '已抵扣', EXPIRED: '已过期', VOID: '已作废' }[s] || s
}

/** 状态徽章样式映射 */
function badgeClass(status) {
  const map = { PENDING: 'amber', DEDUCTED: 'green', EXPIRED: 'red', VOID: 'gray' }
  return map[status] || 'gray'
}

function sumOf(status) {
  const hit = summary.value.find(s => s.deductStatus === status)
  return hit ? hit.taxAmount : 0
}

/** 抵扣截止日临近时标红提示 */
function deadlineClass(row) {
  if (row.deductStatus !== 'PENDING' || !row.deductDeadline) return 'text-muted'
  const days = Math.ceil((new Date(row.deductDeadline) - new Date()) / 86400000)
  if (days < 0) return 'text-danger'
  if (days <= 30) return 'text-warning'
  return ''
}

function getList() {
  loading.value = true
  listInputTax(proxy.addDateRange({ ...queryParams.value, pageNum: 1, pageSize: 200 }, dateRange.value)).then(res => {
    list.value = res.rows || []
    loading.value = false
    applySavedWidths()
  })
  inputTaxSummary().then(res => { summary.value = res.data || [] })
  expiringInputTax(30).then(res => {
    expiringList.value = res.data || []
    overdueCount.value = expiringList.value.filter(
      t => t.deductDeadline && new Date(t.deductDeadline) < new Date()
    ).length
  })
}

function handleQuery() {
  pageNum.value = 1
  getList()
}

function resetQuery() {
  queryParams.value = { partnerName: '', invoiceNo: '', deductStatus: '' }
  dateRange.value = []
  pageNum.value = 1
  getList()
}

function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

function handleDeduct(row) {
  current.value = row
  deductPeriod.value = periodOptions.value[0]
  showDeduct.value = true
}

function submitDeduct() {
  if (!deductPeriod.value) {
    proxy.$modal.msgError('请选择抵扣所属期间')
    return
  }
  deductInputTax(current.value.taxId, deductPeriod.value).then(() => {
    proxy.$modal.msgSuccess('已标记为已抵扣')
    showDeduct.value = false
    getList()
  })
}

function handleVoid(row) {
  proxy.$modal.confirm(
    `是否确认作废发票「${row.invoiceNo}」的进项税记录？作废后不可抵扣。`
  ).then(() => {
    return voidInputTax(row.taxId)
  }).then(() => {
    proxy.$modal.msgSuccess('已作废')
    getList()
  }).catch(() => {})
}

function handleMarkExpired() {
  proxy.$modal.confirm(
    `将把 ${overdueCount.value} 笔超期未抵扣的进项税标记为「已过期」。` +
    `依据增值税暂行条例，逾期进项税不得抵扣。是否继续？`
  ).then(() => {
    return markInputTaxExpired()
  }).then(res => {
    proxy.$modal.msgSuccess(`已处理 ${res.data} 笔`)
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/fin/taxProvision/inputTax/export',
    { ...queryParams.value }, `进项税抵扣台账_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  loadColumnVisibility()
})

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.fin-inputtax-page {
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
.fin-inputtax-page .surface {
  background:#fff;
  border:1px solid var(--ink-200);
  border-radius:var(--r-lg);
  box-shadow:var(--shadow-card);
  overflow:hidden;
  margin-bottom:8px;
}

/* ===== Filter Card ===== */
.fin-inputtax-page .filter-card { padding:14px 20px 16px; }
.fin-inputtax-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.fin-inputtax-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.fin-inputtax-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.fin-inputtax-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.fin-inputtax-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.fin-inputtax-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.fin-inputtax-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.fin-inputtax-page .field { display:flex; flex-direction:column; gap:6px; }
.fin-inputtax-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.fin-inputtax-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.fin-inputtax-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.fin-inputtax-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.fin-inputtax-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.fin-inputtax-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.fin-inputtax-page .field .control :deep(.el-select) { width:100%; }
.fin-inputtax-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.fin-inputtax-page .field .control :deep(.el-date-editor) { width:100%; }
.fin-inputtax-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.fin-inputtax-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.fin-inputtax-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.fin-inputtax-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.fin-inputtax-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.fin-inputtax-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== Table ===== */
.fin-inputtax-page .table-wrap { overflow-x:auto; }
.fin-inputtax-page :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.fin-inputtax-page :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.fin-inputtax-page .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; letter-spacing:-.01em; }
.fin-inputtax-page .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:#dc2626; font-weight:700; }
.fin-inputtax-page .rd-amount-plain { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }

/* ===== Badges ===== */
.fin-inputtax-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.fin-inputtax-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.fin-inputtax-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.fin-inputtax-page .badge.amber .dot { background:var(--amber-500); }
.fin-inputtax-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.fin-inputtax-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.fin-inputtax-page .badge.green .dot { background:var(--green-500); }
.fin-inputtax-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.fin-inputtax-page .badge.red .dot { background:var(--red-500); }
.fin-inputtax-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.fin-inputtax-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.fin-inputtax-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.fin-inputtax-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.fin-inputtax-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.fin-inputtax-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.fin-inputtax-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.fin-inputtax-page .pagination-container :deep(.el-pagination .btn-prev), .fin-inputtax-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }

/* ===== 操作列（规范 5.5.3） ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 汇总卡片 / 文本色 ===== */
.mini-card { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-md); box-shadow:var(--shadow-card); padding:12px 16px; }
.mini-label { font-size:13px; color:var(--ink-500); margin-bottom:6px; }
.mini-value { font-size:20px; font-weight:700; font-variant-numeric:tabular-nums; }
.text-warning { color:var(--amber-700); }
.text-success { color:var(--green-700); }
.text-danger { color:var(--red-700); }
.text-muted { color:var(--ink-400); }
.hint-text { font-size:13px; color:var(--ink-700); }
.form-tip { font-size:12px; color:var(--ink-400); line-height:1.5; margin-top:4px; }

@media (max-width:1100px) { .fin-inputtax-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .fin-inputtax-page .filter-card .filter-bar { grid-template-columns:1fr; } .fin-inputtax-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>

<style scoped>
/* ===== rd- 详情弹窗 CSS（detail guide 第五章） ===== */
:deep(.rd-dialog .el-dialog__header) { background: #fff; border-bottom: 1px solid #e2e8f0; padding: 0; margin: 0; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: center; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #64748b; font-size: 16px; }
:deep(.rd-dialog .el-dialog__headerbtn:hover) { background: #f8fafc; }
:deep(.rd-dialog .el-dialog__body) { padding: 24px; background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%); }

.rd-detail-header { position: relative; display: flex; align-items: center; gap: 16px; padding: 20px 24px; background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%); color: #fff; border-radius: 12px 12px 0 0; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; right: -30px; top: -30px; width: 140px; height: 140px; border-radius: 50%; background: rgba(255,255,255,.08); }
.rd-detail-header .rd-detail-header-icon { width: 44px; height: 44px; border-radius: 10px; background: rgba(255,255,255,.18); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.rd-detail-header .rd-detail-header-main { flex: 1; min-width: 0; }
.rd-detail-header .rd-detail-header-title { font-size: 17px; font-weight: 700; line-height: 1.3; }
.rd-detail-header .rd-detail-header-sub { font-size: 13px; opacity: .85; margin-top: 3px; }
.rd-detail-header .rd-detail-header-no { font-family: ui-monospace, Menlo, monospace; font-size: 13px; background: rgba(255,255,255,.16); padding: 3px 10px; border-radius: 6px; }
.rd-detail-header .rd-detail-header-placeholder { width: 44px; flex-shrink: 0; }
.rd-detail-header + .rd-detail-header-divider { height: 1px; background: #e2e8f0; }

.rd-page { background: transparent; }
.rd-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; box-shadow: 0 1px 2px rgba(15,23,42,.05); margin-bottom: 14px; overflow: hidden; transition: box-shadow .2s cubic-bezier(.16,.84,.44,1); }
.rd-card:hover { box-shadow: 0 6px 16px -4px rgba(15,23,42,.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-bottom: 1px solid #f1f5f9; }
.rd-card-title { display: flex; align-items: center; gap: 9px; font-size: 15px; font-weight: 600; color: #0f172a; }
.rd-card-icon { width: 28px; height: 28px; border-radius: 7px; background: #eef2ff; color: #4f46e5; display: inline-flex; align-items: center; justify-content: center; flex-shrink: 0; }
.rd-card-body { padding: 18px 20px; }

.rd-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px 20px; }
.rd-item { min-width: 0; display: flex; flex-direction: column; gap: 5px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 6px; }
.rd-label-dot { width: 4px; height: 4px; border-radius: 50%; background: #94a3b8; flex-shrink: 0; }
.rd-value { font-size: 14px; color: #0f172a; font-weight: 500; line-height: 1.5; word-break: break-all; }
.rd-value--large { font-size: 16px; font-weight: 600; }
.rd-value--muted { color: #94a3b8; font-weight: 400; }
.rd-amount { font-variant-numeric: tabular-nums; color: #dc2626; font-weight: 700; }
.rd-empty { padding: 40px 0; text-align: center; color: #94a3b8; font-size: 14px; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }
.rd-page > .rd-card { animation: rdFadeIn .25s cubic-bezier(.16,.84,.44,1) both; }
.rd-page > .rd-card:nth-child(1) { animation-delay: 0s; }
.rd-page > .rd-card:nth-child(2) { animation-delay: .04s; }
.rd-page > .rd-card:nth-child(3) { animation-delay: .08s; }
.rd-page > .rd-card:nth-child(4) { animation-delay: .12s; }

@media (max-width: 1100px) { .rd-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 720px) { .rd-grid { grid-template-columns: 1fr; } }
</style>
