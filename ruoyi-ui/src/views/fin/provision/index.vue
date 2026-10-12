<template>
  <div class="app-container fin-provision-page">
    <!-- ===== 跌价准备总览（stat-card，置于顶部，对齐应收管理页面） ===== -->
    <el-row :gutter="12" class="mb8">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-label">{{ period ? formatPeriod(period) : '全部期间' }} 存货成本（元）</div>
          <div class="stat-value">{{ fmt(provisionSum.totalValue) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-danger">
          <div class="stat-label">本期计提合计（元）</div>
          <div class="stat-value">{{ fmt(provisionSum.provisionAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-label">计提笔数</div>
          <div class="stat-value">{{ provisionSum.cnt || 0 }}<span class="stat-unit">笔</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-label">综合计提比例</div>
          <div class="stat-value">{{ overallRatio }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span>筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>会计期间</label>
          <div class="control">
            <el-select v-model="period" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>计提日期</label>
          <div class="control">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>计提状态</label>
          <div class="control">
            <el-select v-model="queryParams.provisionStatus" placeholder="全部" clearable @change="handleQuery">
              <template #prefix><el-icon><Filter /></el-icon></template>
              <el-option label="已计提" value="PROVISIONED" />
              <el-option label="已冲回" value="REVERSED" />
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
          <el-table-column label="计提状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="isReversed(scope.row) ? 'badge-gray' : 'badge-green'">
                <span class="dot"></span>{{ isReversed(scope.row) ? '已冲回' : '已计提' }}
              </span>
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

    <!-- ===== 详情只读弹窗（rd- 规范，独立只读，禁止禁用表单模拟） ===== -->
    <el-dialog v-model="viewVisible" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">存货跌价准备详情</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">编号：{{ viewData.valId || '—' }}</span>
              <span class="badge" :class="isReversed(viewData) ? 'badge-gray' : 'badge-green'">
                <span class="dot"></span>{{ isReversed(viewData) ? '已冲回' : '已计提' }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="viewData.valId">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </span>基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">会计期间</span>
                <div class="rd-value">{{ formatPeriod(viewData.period) || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">计提状态</span>
                <div class="rd-value">
                  <span class="badge" :class="isReversed(viewData) ? 'badge-gray' : 'badge-green'">
                    <span class="dot"></span>{{ isReversed(viewData) ? '已冲回' : '已计提' }}
                  </span>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">物料编码</span>
                <div class="rd-value col-mono">{{ viewData.materialCode || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">物料名称</span>
                <div class="rd-value">{{ viewData.materialName || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">结存数量</span>
                <div class="rd-value">{{ Number(viewData.qty || 0).toLocaleString('zh-CN') }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">单位成本</span>
                <div class="rd-value">{{ fmt(viewData.unitCost, 4) }}</div>
              </div>
            </div>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
              </span>计提信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">存货成本</span>
                <div class="rd-value rd-value--large">{{ fmt(viewData.totalValue) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">计提金额</span>
                <div class="rd-value"><span class="rd-amount">{{ fmt(viewData.provisionAmount) }}</span></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">计提比例</span>
                <div class="rd-value">{{ pct(ratioOf(viewData)) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">账面价值</span>
                <div class="rd-value">{{ fmt(bookValue(viewData)) }}</div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无备注' }}</div>
              </div>
            </div>
          </div>
        </section>

        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </span>审计信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">计提人</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewData.createBy }">{{ viewData.createBy || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">计提时间</span>
                <div class="rd-value">{{ viewData.createTime || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">更新人</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewData.updateBy }">{{ viewData.updateBy || '—' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">更新时间</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewData.updateTime }">{{ viewData.updateTime || '—' }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewVisible = false">关 闭</el-button>
        </div>
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
const dateRange = ref([])
const showSearch = ref(true)
const showAdvanced = ref(false)
const queryParams = ref({
  materialCode: '', materialName: '', provisionStatus: '', pageNum: 1, pageSize: 10
})

const defaultColumns = {
  period: { label: '会计期间', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  qty: { label: '结存数量', visible: true },
  unitCost: { label: '单位成本', visible: true },
  totalValue: { label: '存货成本', visible: true },
  provisionAmount: { label: '计提金额', visible: true },
  ratio: { label: '计提比例', visible: true },
  status: { label: '计提状态', visible: true },
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
  if (queryParams.value.provisionStatus) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

/** 综合计提比例 = 本期计提合计 ÷ 本期存货成本 */
const overallRatio = computed(() => {
  const total = Number(provisionSum.value.totalValue || 0)
  if (!total) return '—'
  return ((Number(provisionSum.value.provisionAmount || 0) / total) * 100).toFixed(2) + '%'
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

/** 是否已冲回（冲回时后端把计提金额置 0 并在备注追加「[已冲回]」） */
function isReversed(row) {
  if (!row) return false
  return Number(row.provisionAmount || 0) === 0 || /\[已冲回\]/.test(row.remark || '')
}

/** 账面价值 = 存货成本 - 已计提跌价准备 */
function bookValue(row) {
  if (!row) return 0
  return Number(row.totalValue || 0) - Number(row.provisionAmount || 0)
}

/** 试算本次计提金额 */
function calcAmount(row) {
  const r = Number(ratioMap[row.materialId])
  if (!r || r <= 0) return 0
  return (Number(row.totalValue || 0) * r).toFixed(2)
}

/** 组装查询参数：业务字段走顶层，扩展条件（计提状态、计提日期范围）走 params */
function buildQuery() {
  const params = { ...queryParams.value, period: period.value }
  const extra = {}
  if (queryParams.value.provisionStatus) extra.provisionStatus = queryParams.value.provisionStatus
  if (dateRange.value && dateRange.value.length === 2) {
    extra.beginCreateTime = dateRange.value[0]
    extra.endCreateTime = dateRange.value[1]
  }
  params.params = extra
  delete params.provisionStatus
  return params
}

function getList() {
  loading.value = true
  listProvision(buildQuery()).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
  provisionSummary(period.value).then(res => { provisionSum.value = res.data || {} })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  period.value = `${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}`
  queryParams.value = {
    materialCode: '', materialName: '', provisionStatus: '',
    pageNum: 1, pageSize: queryParams.value.pageSize
  }
  dateRange.value = []
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
}

/* ===== 卡片容器 ===== */
.fin-provision-page .surface {
  background: #fff;
  border: 1px solid var(--ink-200);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  margin-bottom: 8px;
}

/* ===== 统计卡片（stat-card，置于顶部，对齐应收管理页面） ===== */
.fin-provision-page .stat-card {
  background: #fff;
  border: 1px solid var(--ink-200);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  padding: 16px 18px;
  text-align: center;
  transition: all .15s var(--ease-out);
}
.fin-provision-page .stat-card:hover {
  border-color: var(--brand-200);
  box-shadow: 0 4px 12px -2px rgba(99, 102, 241, .12);
}
.fin-provision-page .stat-card .stat-label {
  font-size: 13px;
  color: var(--ink-500);
  margin-bottom: 8px;
}
.fin-provision-page .stat-card .stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink-900);
  font-feature-settings: "tnum" 1;
  font-variant-numeric: tabular-nums;
}
.fin-provision-page .stat-card.stat-success .stat-value { color: var(--green-500); }
.fin-provision-page .stat-card.stat-warning .stat-value { color: var(--amber-500); }
.fin-provision-page .stat-card.stat-danger .stat-value { color: var(--red-500); }
.fin-provision-page .stat-unit {
  font-size: 12px;
  margin-left: 2px;
  font-weight: 400;
}

/* ===== 筛选卡片（list guide 2.1 / 2.3） ===== */
.fin-provision-page .filter-card { padding: 14px 20px 16px; }
.fin-provision-page .filter-card .filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.fin-provision-page .filter-card .filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-700);
}
.fin-provision-page .filter-card .filter-title .glyph {
  width: 4px;
  height: 14px;
  background: var(--brand-600);
  border-radius: 2px;
}
.fin-provision-page .filter-card .filter-bar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 16px;
}
.fin-provision-page .filter-card .filter-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--ink-200);
}
.fin-provision-page .filter-card .filter-info {
  font-size: 13px;
  color: var(--ink-500);
  display: flex;
  align-items: center;
  gap: 6px;
}
.fin-provision-page .filter-card .filter-buttons { display: flex; gap: 8px; }
.fin-provision-page .adv-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--brand-600);
  cursor: pointer;
  user-select: none;
  transition: color .15s var(--ease-out);
}
.fin-provision-page .adv-link:hover { color: var(--brand-700); }
.fin-provision-page .adv-link .el-icon { transition: transform .2s var(--ease-out); }
.fin-provision-page .adv-link.is-open .el-icon { transform: rotate(180deg); }

/* ===== 筛选字段（label 在上，控件在下） ===== */
.fin-provision-page .field { display: flex; flex-direction: column; gap: 6px; }
.fin-provision-page .field label {
  font-size: 14px;
  font-weight: 500;
  color: var(--ink-700);
  display: flex;
  align-items: center;
  gap: 6px;
}
.fin-provision-page .field .control {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px;
  background: #fff;
  border: 1px solid var(--ink-200);
  border-radius: var(--r-sm);
  transition: border-color .15s var(--ease-out), box-shadow .15s var(--ease-out);
}
.fin-provision-page .field .control:focus-within {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, .15);
}
.fin-provision-page .field .control :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0;
  height: 34px;
}
.fin-provision-page .field .control :deep(.el-input__inner) {
  border: 0;
  background: transparent;
  font-size: 14px;
  color: var(--ink-900);
  height: 34px;
  line-height: 34px;
}
.fin-provision-page .field .control :deep(.el-input__inner::placeholder) { color: var(--ink-400); }
.fin-provision-page .field .control :deep(.el-input__prefix) { color: var(--ink-400); margin-right: 4px; }
.fin-provision-page .field .control :deep(.el-select) { width: 100%; }
.fin-provision-page .field .control :deep(.el-select .el-select__wrapper) {
  box-shadow: none !important;
  background: transparent !important;
  padding: 0;
  min-height: 34px;
  height: 34px;
}
.fin-provision-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) {
  font-size: 14px;
  color: var(--ink-900);
}
.fin-provision-page .field .control :deep(.el-date-editor) { width: 100%; }
.fin-provision-page .field .control :deep(.el-date-editor .el-range-input) {
  background: transparent;
  border: 0;
  font-size: 14px;
  color: var(--ink-900);
}
.fin-provision-page .field .control :deep(.el-date-editor .el-range-separator) { color: var(--ink-400); }
.fin-provision-page .field .control :deep(.el-date-editor .el-range__icon) { color: var(--ink-400); }

/* ===== 工具栏 / 表格 ===== */
.fin-provision-page .toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--ink-200);
  background: var(--ink-50);
}
.fin-provision-page .toolbar .left {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
.fin-provision-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.form-tip {
  font-size: 12px;
  color: var(--ink-400);
  line-height: 1.6;
}
.fin-provision-page .table-wrap { overflow-x: auto; }
.fin-provision-page .app-table {
  width: 100%;
  --el-table-bg-color: #fff;
  --el-table-header-bg-color: var(--ink-50);
  --el-table-row-hover-bg-color: #fafbff;
  --el-table-border-color: var(--ink-200);
  --el-table-text-color: var(--ink-700);
  --el-table-header-text-color: var(--ink-500);
}
.fin-provision-page .app-table :deep(.el-table__header th) {
  background: var(--ink-50) !important;
  color: var(--ink-500);
  font-weight: 600;
  font-size: 14px;
  letter-spacing: .02em;
  padding: 12px 16px;
  border-bottom: 1px solid var(--ink-200);
}
.fin-provision-page .app-table :deep(.el-table__body td) {
  padding: 14px 16px;
  border-bottom: 1px solid var(--ink-100);
  color: var(--ink-700);
}
.fin-provision-page .app-table :deep(.el-table__row:hover > td) { background: #fafbff !important; }
.col-num :deep(.cell) { font-variant-numeric: tabular-nums; }
.amount-cell { font-variant-numeric: tabular-nums; }
.amount-cell.strong { font-weight: 600; }
.col-mono {
  font-family: ui-monospace, "JetBrains Mono", "SF Mono", Menlo, monospace;
  letter-spacing: -.01em;
}
.text-danger { color: var(--red-700); font-weight: 600; }
.text-muted { color: var(--ink-400); }

/* ===== 操作列（list guide 5.5.3） ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 状态徽章（badge，list guide 5.5 / 5.6） ===== */
.fin-provision-page .badge,
.rd-detail-header .badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  border: 1px solid transparent;
}
.fin-provision-page .badge .dot,
.rd-detail-header .badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}
/* 徽章颜色使用字面量：弹框 append-to-body 后会脱离 .fin-provision-page，
   若依赖页面级 CSS 变量将取不到值 */
.badge.badge-green { background: #ecfdf5; color: #047857; border-color: #a7f3d0; }
.badge.badge-green .dot { background: #10b981; }
.badge.badge-gray { background: #f1f5f9; color: #64748b; border-color: #e2e8f0; }
.badge.badge-gray .dot { background: #94a3b8; }

/* ===== 分页 ===== */
.fin-provision-page .pagination-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 14px 20px;
  font-size: 14px;
  color: var(--ink-500);
  background: #fff;
}
.fin-provision-page .pagination-container :deep(.el-pagination) { justify-content: flex-end; }

/* ===== 响应式 ===== */
@media (max-width: 1100px) { .fin-provision-page .filter-card .filter-bar { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 720px) {
  .fin-provision-page .filter-card .filter-bar { grid-template-columns: 1fr; }
  .fin-provision-page .toolbar { flex-wrap: wrap; gap: 10px; }
}

/* ===== 弹窗 =====
   标题横幅 / 卡片 / 信息网格 / 金额 / 空状态统一使用全局 detail-page.scss 的 rd- 体系，
   页面不再重复定义（detail-page-style-guide 3.1 / 3.2 / 3.3 / 3.4、第 14 条） */
:deep(.rd-dialog) {
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, .18);
}
/* 详情弹框内容较长时 body 内滚动，避免弹框超出视口 */
:deep(.rd-dialog .el-dialog__body) { max-height: 68vh; overflow-y: auto; }
</style>
