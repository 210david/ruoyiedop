<template>
  <div class="app-container fin-provision-page">
    <!-- ===== 筛选条件卡片 ===== -->
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
          <label>会计期间</label>
          <div class="control is-select">
            <el-select v-model="period" placeholder="全部" @change="handleQuery">
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="模糊搜索" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="模糊搜索" clearable @keyup.enter="handleQuery">
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

    <!-- ===== 汇总卡片 ===== -->
    <el-row :gutter="12" style="margin-bottom: 8px">
      <el-col :span="8">
        <div class="mini-card">
          <div class="mini-label">{{ formatPeriod(period) }} 存货成本</div>
          <div class="mini-value asset">{{ fmt(provisionSum.totalValue) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="mini-card">
          <div class="mini-label">本期计提合计</div>
          <div class="mini-value text-danger">{{ fmt(provisionSum.provisionAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="mini-card">
          <div class="mini-label">计提笔数</div>
          <div class="mini-value">{{ provisionSum.cnt || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handlePending" v-hasPermi="['fin:cost:valuation']">待计提存货</el-button>
          <el-button icon="Download" @click="handleExport">导出</el-button>
          <span class="form-tip">计提金额 = 存货成本 × 跌价比例；冲回后本期可重新计提</span>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_provision_index" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="会计期间" prop="period" key="period" :width="colWidth('period', 110)" resizable align="center" v-if="columns.period.visible">
            <template #default="scope">{{ formatPeriod(scope.row.period) }}</template>
          </el-table-column>
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 150)" resizable align="center" v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 180)" resizable align="center" show-overflow-tooltip v-if="columns.materialName.visible">
            <template #default="scope">{{ scope.row.materialName || '—' }}</template>
          </el-table-column>
          <el-table-column label="结存数量" prop="qty" key="qty" :width="colWidth('qty', 110)" resizable align="center" class-name="col-num" v-if="columns.qty.visible">
            <template #default="scope">
              <span class="amount-cell">{{ Number(scope.row.qty || 0).toLocaleString('zh-CN') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位成本" prop="unitCost" key="unitCost" :width="colWidth('unitCost', 120)" resizable align="center" class-name="col-num" v-if="columns.unitCost.visible">
            <template #default="scope">
              <span class="amount-cell">{{ fmt(scope.row.unitCost, 4) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="存货成本" prop="totalValue" key="totalValue" :width="colWidth('totalValue', 140)" resizable align="center" class-name="col-num" v-if="columns.totalValue.visible">
            <template #default="scope">
              <span class="amount-cell">{{ fmt(scope.row.totalValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="计提金额" prop="provisionAmount" key="provisionAmount" :width="colWidth('provisionAmount', 140)" resizable align="center" class-name="col-num" v-if="columns.provisionAmount.visible">
            <template #default="scope">
              <span class="amount-cell strong text-danger">{{ fmt(scope.row.provisionAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="计提比例" prop="ratio" key="ratio" :width="colWidth('ratio', 110)" resizable align="center" class-name="col-num" v-if="columns.ratio.visible">
            <template #default="scope">
              <span class="amount-cell">{{ pct(ratioOf(scope.row)) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :min-width="colWidth('remark', 150)" resizable align="center" show-overflow-tooltip v-if="columns.remark.visible">
            <template #default="scope">{{ scope.row.remark || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="viewOpen(scope.row)">详情</el-button>
                <el-button link type="warning" icon="RefreshLeft" @click="handleReverse(scope.row)" v-hasPermi="['fin:cost:valuation']">冲回</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 待计提存货清单弹窗 ===== -->
    <el-dialog v-model="showPending" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg></div>
          <span class="rd-detail-header-title">待计提跌价准备的存货</span>
        </div>
      </template>
      <el-alert v-if="pendingList.length === 0" type="info" :closable="false" show-icon
                title="本期无可计提的存货（成本台账为空，或本期已全部计提过）" />
      <template v-else>
        <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 10px"
                  title="请按物料实际跌价程度逐个确定比例，不要批量套用同一比例" />
        <el-table :data="pendingList" size="small" border max-height="420">
          <el-table-column prop="materialCode" label="物料编码" width="140" align="center" />
          <el-table-column prop="materialName" label="物料名称" min-width="170" align="center" show-overflow-tooltip />
          <el-table-column label="结存数量" width="100" align="center">
            <template #default="{ row }">
              <span class="amount-cell">{{ Number(row.qty || 0).toLocaleString('zh-CN') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位成本" width="110" align="center">
            <template #default="{ row }">
              <span class="amount-cell">{{ fmt(row.unitCost, 4) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="存货成本" width="130" align="center">
            <template #default="{ row }">
              <span class="amount-cell strong">{{ fmt(row.totalValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已累计计提" width="120" align="center">
            <template #default="{ row }">
              <span class="amount-cell text-muted">{{ fmt(row.alreadyProvision) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="跌价比例" width="120" align="center">
            <template #default="{ row }">
              <el-input v-model="ratioMap[row.materialId]" placeholder="如 0.05" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="计提金额" width="120" align="center">
            <template #default="{ row }">
              <span class="amount-cell strong text-danger">
                {{ fmt(calcAmount(row)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button link type="primary" icon="Check" @click="handleProvision(row)">计提</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template #footer>
        <el-button @click="showPending = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情只读弹窗 ===== -->
    <el-dialog v-model="viewVisible" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">存货跌价准备详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.valId">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">准备编号：{{ viewData.valId }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">会计期间</span>
                <div class="rd-value">{{ formatPeriod(viewData.period) || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">物料编码</span>
                <div class="rd-value">{{ viewData.materialCode || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">物料名称</span>
                <div class="rd-value">{{ viewData.materialName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">结存数量</span>
                <div class="rd-value">{{ Number(viewData.qty || 0).toLocaleString('zh-CN') }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">单位成本</span>
                <div class="rd-value">{{ fmt(viewData.unitCost, 4) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">存货成本</span>
                <div class="rd-value rd-value--large">{{ fmt(viewData.totalValue) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">计提金额</span>
                <div class="rd-value"><span class="rd-amount">{{ fmt(viewData.provisionAmount) }}</span></div>
              </div>
              <div class="rd-item rd-item--full" v-if="viewData.remark">
                <span class="rd-label">备注</span>
                <div class="rd-value">{{ viewData.remark }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FinProvision">
import { listProvision, pendingProvision, doProvision, reverseProvision, provisionSummary }
  from '@/api/fin/configExt'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_provision_index')

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pendingList = ref([])
const provisionSum = ref({})
const showPending = ref(false)
const viewVisible = ref(false)
const viewData = ref({})
const ratioMap = reactive({})
const period = ref('')
const showSearch = ref(true)
const showAdvanced = ref(false)
const queryParams = ref({ materialCode: '', materialName: '', pageNum: 1, pageSize: 10 })

const defaultColumns = {
  period: { label: '会计期间', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  qty: { label: '结存数量', visible: true },
  unitCost: { label: '单位成本', visible: true },
  totalValue: { label: '存货成本', visible: true },
  provisionAmount: { label: '计提金额', visible: true },
  ratio: { label: '计提比例', visible: true },
  remark: { label: '备注', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('fin_provision_columns')
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
  if (period.value) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  return count
})

const periodOptions = computed(() => {
  const now = new Date()
  const arr = []
  for (let i = 0; i < 24; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    arr.push(`${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  return arr
})

function fmt(v, digits = 2) {
  return Number(v || 0).toLocaleString('zh-CN',
    { minimumFractionDigits: digits, maximumFractionDigits: digits })
}

function pct(v) {
  if (!v) return '—'
  return (Number(v) * 100).toFixed(2).replace(/\.00$/, '') + '%'
}

function formatPeriod(p) {
  if (!p || p.length !== 6) return p || ''
  return `${p.slice(0, 4)}年${p.slice(4)}月`
}

/** 从备注解析计提比例（比例不落库，只在 remark 里留痕） */
function ratioOf(row) {
  const m = /([\d.]+)%/.exec(row.remark || '')
  return m ? Number(m[1]) / 100 : null
}

/** 试算本次计提金额 */
function calcAmount(row) {
  const r = Number(ratioMap[row.materialId])
  if (!r || r <= 0) return 0
  return (Number(row.totalValue || 0) * r).toFixed(2)
}

function getList() {
  loading.value = true
  listProvision({ ...queryParams.value, period: period.value }).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
  provisionSummary(period.value).then(res => { provisionSum.value = res.data || {} })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  period.value = `${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}`
  queryParams.value = { materialCode: '', materialName: '', pageNum: 1, pageSize: 10 }
  handleQuery()
}

/** 详情 —— 独立只读弹窗 */
function viewOpen(row) {
  viewData.value = { ...row }
  viewVisible.value = true
}

function handlePending() {
  pendingProvision(period.value).then(res => {
    pendingList.value = res.data || []
    Object.keys(ratioMap).forEach(k => delete ratioMap[k])
    showPending.value = true
  })
}

function handleProvision(row) {
  const r = Number(ratioMap[row.materialId])
  if (!r || r <= 0) {
    proxy.$modal.msgError('请填写有效的跌价比例（大于 0，如 0.05）')
    return
  }
  if (r > 1) {
    proxy.$modal.msgError('跌价比例应在 0 ~ 1 之间')
    return
  }
  proxy.$modal.confirm(
    `确认对物料「${row.materialCode}」计提跌价准备？\n` +
    `存货成本 ${fmt(row.totalValue)} 元 × 跌价比例 ${(r * 100).toFixed(2)}% = ${calcAmount(row)} 元`
  ).then(() => {
    return doProvision(period.value, row.materialId, r)
  }).then(res => {
    proxy.$modal.msgSuccess(`已计提 ${fmt(res.data)} 元`)
    delete ratioMap[row.materialId]
    pendingList.value = pendingList.value.filter(m => m.materialId !== row.materialId)
    getList()
  }).catch(() => {})
}

function handleReverse(row) {
  proxy.$modal.confirm(
    `是否确认冲回物料「${row.materialCode}」在 ${formatPeriod(row.period)} 计提的跌价准备？` +
    `冲回后可重新计提。`
  ).then(() => {
    return reverseProvision(row.valId)
  }).then(() => {
    proxy.$modal.msgSuccess('已冲回')
    getList()
  }).catch(() => {})
}

function handleExport() {
  proxy.download('/fin/taxProvision/provision/export',
    { ...queryParams.value, period: period.value },
    `存货跌价准备_${new Date().getTime()}.xlsx`)
}

period.value = `${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}`
getList()
</script>

<style scoped>
/* ===== design tokens（list guide 7.1） ===== */
.fin-provision-page {
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
  --edop-primary: #2563eb;
  --edop-primary-weak: #eef4ff;
  --edop-text: #1f2937;
  --edop-text-2: #475569;
  --edop-text-3: #94a3b8;
  --edop-border: #e2e8f0;
  --edop-bg-soft: #f8fafc;
  --edop-radius: 8px;
  --edop-radius-lg: 12px;
  --edop-shadow: 0 1px 3px rgba(15, 23, 42, .06), 0 1px 2px rgba(15, 23, 42, .04);
}

/* ===== 卡片容器 ===== */
.fin-provision-page .surface {
  background: #fff;
  border: 1px solid var(--edop-border);
  border-radius: var(--edop-radius-lg);
  box-shadow: var(--edop-shadow);
  padding: 16px 20px;
}

/* ===== 筛选卡片 ===== */
.filter-head {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.filter-glyph,
.glyph {
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: var(--edop-primary);
  margin-right: 8px;
  display: inline-block;
}
.filter-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--edop-text);
}
.adv-link {
  margin-left: 12px;
  color: var(--edop-primary);
  cursor: pointer;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.adv-link .chev,
.adv-link .adv-arrow {
  transition: transform .2s;
}
.adv-link .chev,
.adv-link .adv-arrow.open {
  transform: rotate(180deg);
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  align-items: flex-start;
}
.field {
  display: flex;
  align-items: center;
  gap: 8px;
}
.field > label {
  font-size: 13px;
  color: var(--edop-text-2);
  white-space: nowrap;
}
.field .control {
  width: 200px;
}
.filter-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-info {
  font-size: 13px;
  color: var(--edop-text-3);
  margin-right: 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.filter-info b { color: var(--edop-primary); }
.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--edop-text-3);
  line-height: 1.6;
}

/* ===== 工具栏 / 表格 ===== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.toolbar .left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.toolbar .right {
  display: flex;
  align-items: center;
}
.table-wrap {
  border-radius: var(--edop-radius);
  overflow: hidden;
}
.app-table {
  width: 100%;
}
.app-table :deep(.el-table__header th) {
  background: var(--edop-bg-soft);
  color: var(--edop-text-2);
  font-weight: 600;
}
.col-num :deep(.cell) {
  font-variant-numeric: tabular-nums;
}
.amount-cell {
  font-variant-numeric: tabular-nums;
}
.amount-cell.strong { font-weight: 600; }
.text-danger { color: #dc2626; }
.text-muted { color: var(--edop-text-3); }

/* ===== 操作列（list guide 5.5.3） ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 状态徽章（badge） ===== */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 20px;
  white-space: nowrap;
}
.badge .dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.badge.green { background: var(--green-50); color: var(--green-700); }
.badge.blue { background: var(--blue-50); color: var(--blue-700); }
.badge.amber { background: var(--amber-50); color: var(--amber-700); }
.badge.red { background: var(--red-50); color: var(--red-700); }
.badge.violet { background: var(--violet-50); color: var(--brand-600); }
.badge.gray { background: var(--ink-100); color: var(--ink-500); }

/* ===== 汇总迷你卡片 ===== */
.mini-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}
.mini-card {
  flex: 1;
  background: #fff;
  border: 1px solid var(--edop-border);
  border-radius: var(--edop-radius-lg);
  box-shadow: var(--edop-shadow);
  padding: 14px 18px;
}
.mini-card .mini-label {
  font-size: 13px;
  color: var(--edop-text-3);
  margin-bottom: 6px;
}
.mini-card .mini-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--edop-text);
  font-variant-numeric: tabular-nums;
}
.mini-card .mini-value.asset { color: var(--edop-primary); }

/* ===== 弹窗（detail guide 第五章） ===== */
:deep(.rd-dialog) {
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, .18);
}
:deep(.rd-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
}
:deep(.rd-dialog .el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
  z-index: 2;
}
:deep(.rd-dialog .el-dialog__body) {
  padding: 20px 24px;
  background: #f6f8fb;
}
:deep(.rd-dialog .el-dialog__footer) {
  border-top: 1px solid var(--edop-border);
  padding: 12px 24px;
}
.rd-detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 56px 14px 20px;
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 60%, #60a5fa 100%);
  color: #fff;
  border-radius: 14px 14px 0 0;
}
.rd-detail-header-icon {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: rgba(255, 255, 255, .18);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: .5px;
}
.rd-detail-header-sub {
  display: flex;
  align-items: center;
  margin-left: 4px;
}
.rd-detail-header-divider {
  width: 1px;
  height: 16px;
  background: rgba(255, 255, 255, .35);
  margin: 0 10px;
}
.rd-detail-header-no {
  font-size: 12px;
  color: rgba(255, 255, 255, .85);
}
</style>

<style scoped>
/* ===== 详情卡片（detail guide 第五章续） ===== */
.rd-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.rd-card {
  background: #fff;
  border: 1px solid var(--edop-border);
  border-radius: 12px;
  overflow: hidden;
}
.rd-card-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--edop-border);
  background: #fbfcfe;
}
.rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--edop-text);
}
.rd-card-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--edop-primary-weak);
  color: var(--edop-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rd-card-body {
  padding: 16px;
}
.rd-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px 20px;
}
.rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.rd-item--full {
  grid-column: 1 / -1;
}
.rd-label {
  font-size: 12px;
  color: var(--edop-text-3);
}
.rd-value {
  font-size: 14px;
  color: var(--edop-text);
  word-break: break-all;
  line-height: 1.5;
}
.rd-value--large {
  font-weight: 600;
}
.rd-amount {
  color: #dc2626;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .rd-grid { grid-template-columns: repeat(2, 1fr); }
  .mini-cards { flex-wrap: wrap; }
  .mini-card { min-width: calc(50% - 8px); }
}
@media (max-width: 768px) {
  .rd-grid { grid-template-columns: 1fr; }
  .filter-actions { margin-left: 0; width: 100%; justify-content: flex-end; }
}

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.rd-page {
  animation: rdFadeIn .25s ease;
}
</style>
