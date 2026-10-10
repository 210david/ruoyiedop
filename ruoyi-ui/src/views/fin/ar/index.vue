<template>
  <div class="app-container fin-ar-page">
    <!-- 应收总览 -->
    <div class="surface summary-bar">
      <div class="summary-item">
        <div class="summary-label">应收总额（元）</div>
        <div class="summary-value">{{ formatAmount(summary.totalAmount) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">已收（元）</div>
        <div class="summary-value text-received">{{ formatAmount(summary.receivedAmount) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">未收（元）</div>
        <div class="summary-value text-unreceived">{{ formatAmount(summary.unreceivedAmount) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">逾期未付</div>
        <div class="summary-value" :class="overdueList.length > 0 ? 'text-danger' : ''">
          {{ overdueList.length }}<span class="summary-unit">笔</span>
        </div>
      </div>
    </div>

    <!-- 筛选卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span>筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>应收单号</label>
          <div class="control">
            <el-input v-model="queryParams.apNo" placeholder="请输入应收单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户</label>
          <div class="control">
            <el-input v-model="queryParams.partnerName" placeholder="请输入名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable>
              <el-option label="未核销" value="OPEN" />
              <el-option label="部分核销" value="PARTIAL" />
              <el-option label="已核销" value="SETTLED" />
              <el-option label="已作废" value="VOID" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>应收日期</label>
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
      </div>
      <div class="filter-actions">
        <div class="filter-info">已选 {{ checkedRows.length }} 条</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 数据卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button v-hasPermi="['fin:ar:add']" type="primary" plain icon="Plus" @click="handleGenerate">从发货单生成应收单</el-button>
          <el-button v-hasPermi="['fin:ar:receipt']" type="warning" icon="Refresh" @click="handleSyncReceived">同步已收金额</el-button>
          <el-button v-hasPermi="['fin:ar:invoice']" type="success" icon="Tickets" :disabled="!canInvoice" @click="handleInvoice">登记销项发票</el-button>
          <el-button v-hasPermi="['fin:ar:badDebt']" type="danger" icon="Warning" @click="handleBadDebt">坏账计提</el-button>
          <el-button v-hasPermi="['fin:ar:writeoff']" type="success" icon="CircleCheck" :disabled="!canWriteoff" @click="handleWriteoff">核销</el-button>
          <el-button icon="DataAnalysis" @click="showAging = true">账龄分析</el-button>
          <el-button icon="Warning" @click="showOverdue = true">逾期预警</el-button>
          <el-button v-hasPermi="['fin:ar:export']" icon="Download" @click="handleExport">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:show-search="showSearch" :columns="columns" storageKey="fin_ar_columns" @query-table="getArList" />
        </div>
      </div>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          <span class="hint-text">
            应收单由<b>已审核的发货单</b>自动生成，金额不可手工修改；
            已收金额以营销系统收款记录为准，财务侧不重复登记，避免重复收款。
            税额按配置的<b>销售税率</b>（默认13%）从价税合计自动拆分。
          </span>
        </template>
      </el-alert>

      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="arList"
          border
          highlight-current
          @header-dragend="onHeaderDragEnd"
          @current-change="handleCurrentChange"
          @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column v-if="columns.apNo.visible" key="apNo" prop="apNo" label="应收单号" align="center" resizable :min-width="colWidth('apNo', 150)" show-overflow-tooltip>
            <template #default="{ row }"><span class="col-mono">{{ row.apNo }}</span></template>
          </el-table-column>
          <el-table-column v-if="columns.partnerName.visible" key="partnerName" prop="partnerName" label="客户" align="center" resizable :min-width="colWidth('partnerName', 180)" show-overflow-tooltip />
          <el-table-column v-if="columns.sourceKey.visible" key="sourceKey" prop="sourceKey" label="来源发货单" align="center" resizable :width="colWidth('sourceKey', 140)">
            <template #default="{ row }">
              <el-tooltip :content="`来源：${row.sourceModule} / ${row.sourceType}`">
                <span class="text-muted">{{ row.sourceKey || '手工' }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.apDate.visible" key="apDate" prop="apDate" label="应收日期" align="center" resizable :width="colWidth('apDate', 110)">
            <template #default="{ row }">{{ row.apDate ? String(row.apDate).slice(0, 10) : '—' }}</template>
          </el-table-column>
          <el-table-column v-if="columns.amountExclTax.visible" key="amountExclTax" prop="amountExclTax" label="不含税" align="center" resizable :width="colWidth('amountExclTax', 120)">
            <template #default="{ row }">{{ formatAmount(row.amountExclTax) }}</template>
          </el-table-column>
          <el-table-column v-if="columns.taxAmount.visible" key="taxAmount" prop="taxAmount" label="税额" align="center" resizable :width="colWidth('taxAmount', 110)">
            <template #default="{ row }">{{ formatAmount(row.taxAmount) }}</template>
          </el-table-column>
          <el-table-column v-if="columns.totalAmount.visible" key="totalAmount" prop="totalAmount" label="价税合计" align="center" resizable :width="colWidth('totalAmount', 130)">
            <template #default="{ row }"><span class="rd-amount">{{ formatAmount(row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column v-if="columns.receivedAmount.visible" key="receivedAmount" prop="receivedAmount" label="已收" align="center" resizable :width="colWidth('receivedAmount', 120)">
            <template #default="{ row }"><span class="text-received">{{ formatAmount(row.receivedAmount) }}</span></template>
          </el-table-column>
          <el-table-column v-if="columns.unreceivedAmount.visible" key="unreceivedAmount" prop="unreceivedAmount" label="未付" align="center" resizable :width="colWidth('unreceivedAmount', 130)">
            <template #default="{ row }">
              <span class="amount-cell strong text-unreceived">
                {{ formatAmount(Number(row.totalAmount || 0) - Number(row.receivedAmount || 0)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.dueDate.visible" key="dueDate" prop="dueDate" label="到期日" align="center" resizable :width="colWidth('dueDate', 110)">
            <template #default="{ row }">
              <span :class="isOverdue(row) ? 'text-danger' : 'text-muted'">
                {{ row.dueDate ? String(row.dueDate).slice(0, 10) : '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column v-if="columns.status.visible" key="status" prop="status" label="状态" align="center" resizable :width="colWidth('status', 100)">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass(row.status)"><span class="dot"></span>{{ statusLabel(row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="{ row }">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(row)">详情</el-button>
                <el-button v-if="row.status !== 'SETTLED' && row.status !== 'VOID'"
                           v-hasPermi="['fin:ar:edit']" link type="warning" icon="Close" @click="handleVoid(row)">作废</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper" />
      </div>
    </div>

    <!-- 详情弹框（独立只读） -->
    <el-dialog v-model="viewOpen" class="rd-dialog" width="820px" append-to-body>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><Tickets /></el-icon></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">应收单详情</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no">{{ current?.apNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="current">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><Document /></el-icon></span>基本信息
            </div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">客户</span><span class="rd-value">{{ current.partnerName }}</span></div>
              <div class="rd-item"><span class="rd-label">来源发货单</span><span class="rd-value">{{ current.sourceKey || '手工' }}</span></div>
              <div class="rd-item"><span class="rd-label">发票号码</span><span class="rd-value">{{ current.invoiceNo || '—' }}</span></div>
              <div class="rd-item"><span class="rd-label">应收日期</span><span class="rd-value">{{ current.apDate }}</span></div>
              <div class="rd-item"><span class="rd-label">会计期间</span><span class="rd-value">{{ formatPeriod(current.period) }}</span></div>
              <div class="rd-item"><span class="rd-label">账期</span><span class="rd-value">{{ current.paymentDays }} 天</span></div>
              <div class="rd-item"><span class="rd-label">不含税金额</span><span class="rd-value rd-amount">{{ formatAmount(current.amountExclTax) }}</span></div>
              <div class="rd-item"><span class="rd-label">税额</span><span class="rd-value rd-amount">{{ formatAmount(current.taxAmount) }}</span></div>
              <div class="rd-item"><span class="rd-label">价税合计</span><span class="rd-value rd-amount rd-value--large">{{ formatAmount(current.totalAmount) }}</span></div>
              <div class="rd-item"><span class="rd-label">已收金额</span><span class="rd-value rd-amount">{{ formatAmount(current.receivedAmount) }}</span></div>
              <div class="rd-item"><span class="rd-label">未付余额</span><span class="rd-value rd-amount rd-value--large">{{ formatAmount(detailUnreceived) }}</span></div>
              <div class="rd-item"><span class="rd-label">到期日</span><span class="rd-value">{{ current.dueDate || '—' }}</span></div>
              <div class="rd-item"><span class="rd-label">状态</span><span class="rd-value"><span class="badge" :class="badgeClass(current.status)"><span class="dot"></span>{{ statusLabel(current.status) }}</span></span></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><span class="rd-value">{{ current.remark || '—' }}</span></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><el-icon><CircleCheck /></el-icon></span>核销记录
            </div>
          </div>
          <div class="rd-card-body">
            <el-table :data="writeoffList" size="small" border>
              <el-table-column prop="writeoffDate" label="核销日期" width="120" align="center" />
              <el-table-column label="核销金额" width="140" align="center">
                <template #default="{ row }">
                  <span class="rd-amount">{{ formatAmount(row.writeoffAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createBy" label="核销人" width="120" align="center" />
              <el-table-column prop="createTime" label="核销时间" min-width="160" align="center" />
              <el-table-column prop="remark" label="备注" min-width="140" align="center" />
              <template #empty>
                <div class="rd-empty"><span class="rd-empty-text">尚未核销</span></div>
              </template>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 核销弹框 -->
    <el-dialog v-model="showWriteoff" title="收款核销" width="480px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="应收单">
          <el-input :model-value="current?.apNo" disabled />
        </el-form-item>
        <el-form-item label="未付余额">
          <el-input :model-value="formatAmount(detailUnreceived)" disabled />
        </el-form-item>
        <el-form-item label="核销金额" required>
          <el-input v-model="writeoffAmount" type="number" :max="detailUnreceived" placeholder="请输入核销金额" />
        </el-form-item>
        <el-form-item label="快速填写">
          <el-button size="small" @click="writeoffAmount = detailUnreceived">全额 {{ formatAmount(detailUnreceived) }}</el-button>
          <el-button size="small" @click="writeoffAmount = (Number(writeoffAmount) || 0).toFixed(2)">对齐两位小数</el-button>
        </el-form-item>
        <el-form-item label="核销备注">
          <el-input v-model="writeoffRemark" type="textarea" :rows="2" placeholder="选填，将留痕" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitWriteoff">确 定</el-button>
          <el-button @click="showWriteoff = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 生成结果 -->
    <el-dialog v-model="showResult" title="应收单生成结果" width="440px" append-to-body>
      <el-descriptions :column="1" border v-if="lastResult">
        <el-descriptions-item label="待处理发票">{{ lastResult.total }}</el-descriptions-item>
        <el-descriptions-item label="成功">
          <span class="text-success">{{ lastResult.success }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="跳过">{{ lastResult.skip }}</el-descriptions-item>
        <el-descriptions-item label="失败">
          <span class="text-danger">{{ lastResult.fail }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="showResult = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- 销项发票登记 -->
    <el-dialog v-model="showInvoice" title="登记销项发票" width="480px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="应收单号">
          <el-input :model-value="current?.apNo" disabled />
        </el-form-item>
        <el-form-item label="发票号码" required>
          <el-input v-model="invoiceForm.invoiceNo" placeholder="请输入销项发票号码" />
        </el-form-item>
        <el-form-item label="发票代码">
          <el-input v-model="invoiceForm.invoiceCode" placeholder="选填" />
        </el-form-item>
        <el-form-item label="开票日期">
          <el-date-picker v-model="invoiceForm.invoiceDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 100%" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitInvoice">确 定</el-button>
          <el-button @click="showInvoice = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 账龄分析 -->
    <el-dialog v-model="showAging" title="应收账龄分析" width="720px" append-to-body>
      <div style="margin-bottom: 12px">
        <el-button type="primary" icon="Search" @click="loadAging">查询</el-button>
      </div>
      <el-table :data="agingList" size="small" border>
        <el-table-column prop="bucket" label="账龄区间" width="180" align="center" />
        <el-table-column prop="cnt" label="笔数" width="100" align="center" />
        <el-table-column label="应收总额" width="150" align="center">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="未收金额" width="150" align="center">
          <template #default="{ row }">
            <span class="text-unreceived">{{ formatAmount(row.unreceivedAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 逾期预警 -->
    <el-dialog v-model="showOverdue" title="逾期未付预警" width="880px" append-to-body>
      <el-table :data="overdueList" size="small" border max-height="420">
        <el-table-column prop="apNo" label="应收单号" width="150" align="center" />
        <el-table-column prop="partnerName" label="客户" min-width="180" align="center" />
        <el-table-column prop="dueDate" label="到期日" width="110" align="center" />
        <el-table-column label="未收金额" width="140" align="center">
          <template #default="{ row }">
            <span class="text-danger">{{ formatAmount(Number(row.totalAmount || 0) - Number(row.receivedAmount || 0)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账龄" width="100" align="center">
          <template #default="{ row }">{{ overdueDays(row) }} 天</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="FinAr">
import {
  listAr, getAr, generateAr, syncReceived, writeoffAr, voidAr, getArAging, getArOverdue, getArSummary,
  invoiceAr, badDebt
} from '@/api/fin/ar'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_ar_index')

const loading = ref(false)
const showSearch = ref(true)
const arList = ref([])
const total = ref(0)
const dateRange = ref([])
const current = ref(null)
const writeoffList = ref([])
const summary = ref({})
const agingList = ref([])
const overdueList = ref([])
const lastResult = ref(null)

const viewOpen = ref(false)
const showWriteoff = ref(false)
const showInvoice = ref(false)
const invoiceForm = reactive({ invoiceNo: '', invoiceCode: '', invoiceDate: '' })
const badDebtPeriod = ref('')
const showResult = ref(false)
const showAging = ref(false)
const showOverdue = ref(false)
const writeoffAmount = ref('')
const writeoffRemark = ref('')
const checkedRows = ref([])

/** 列显隐配置 */
const defaultColumns = [
  { key: 'apNo', label: '应收单号', visible: true },
  { key: 'partnerName', label: '客户', visible: true },
  { key: 'sourceKey', label: '来源发货单', visible: true },
  { key: 'apDate', label: '应收日期', visible: true },
  { key: 'amountExclTax', label: '不含税', visible: true },
  { key: 'taxAmount', label: '税额', visible: true },
  { key: 'totalAmount', label: '价税合计', visible: true },
  { key: 'receivedAmount', label: '已收', visible: true },
  { key: 'unreceivedAmount', label: '未付', visible: true },
  { key: 'dueDate', label: '到期日', visible: true },
  { key: 'status', label: '状态', visible: true }
]
const columns = ref(defaultColumns.map(c => ({ ...c })))

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('fin_ar_columns')
    if (saved) {
      const arr = JSON.parse(saved)
      columns.value.forEach(col => {
        const s = arr.find(item => item.key === col.key)
        if (s) col.visible = !!s.visible
      })
    }
  } catch (e) { /* ignore */ }
}
loadColumnVisibility()

const queryParams = ref({ pageNum: 1, pageSize: 20, apNo: undefined, partnerName: undefined, status: undefined })

const detailUnreceived = computed(() => {
  if (!current.value) return 0
  return Number(current.value.totalAmount || 0) - Number(current.value.receivedAmount || 0)
})

const canWriteoff = computed(() => {
  const c = current.value
  if (!c) return false
  return ['OPEN', 'PARTIAL'].includes(c.status) && detailUnreceived.value > 0
})

/** 可登记销项发票的应收单（未作废即可） */
const canInvoice = computed(() => {
  const c = current.value
  return !!c && c.status !== 'VOID'
})

function formatAmount(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatPeriod(p) {
  if (!p || p.length !== 6) return p || ''
  return `${p.slice(0, 4)}年${p.slice(4)}月`
}

function statusLabel(s) {
  return { OPEN: '未核销', PARTIAL: '部分核销', SETTLED: '已核销', VOID: '已作废' }[s] || s
}

function badgeClass(s) {
  return { OPEN: 'badge-amber', PARTIAL: 'badge-violet', SETTLED: 'badge-green', VOID: 'badge-gray' }[s] || 'badge-gray'
}

function isOverdue(row) {
  if (!row.dueDate || ['SETTLED', 'VOID'].includes(row.status)) return false
  if (Number(row.totalAmount || 0) - Number(row.receivedAmount || 0) <= 0) return false
  return new Date(row.dueDate) < new Date()
}

function overdueDays(row) {
  if (!row.dueDate) return 0
  return Math.floor((Date.now() - new Date(row.dueDate).getTime()) / 86400000)
}

function getArList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.params = { beginApDate: dateRange.value[0], endApDate: dateRange.value[1] }
  }
  listAr(params).then(res => {
    arList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
}

function loadSummary() {
  getArSummary().then(res => { summary.value = res.data || {} })
}

function loadOverdue() {
  getArOverdue().then(res => { overdueList.value = res.data || [] })
}

function handleQuery() { queryParams.value.pageNum = 1; getArList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, apNo: undefined, partnerName: undefined, status: undefined }
  dateRange.value = []
  getArList()
}

/** 勾选行：单选时同步为当前核销对象，避免必须点行才能核销 */
function handleSelectionChange(rows) {
  checkedRows.value = rows
  if (rows.length === 1) {
    current.value = rows[0]
  }
}

function handleCurrentChange(row) { current.value = row }

/** 从发货单生成应收单 */
function handleGenerate() {
  proxy.$modal.confirm('将扫描已审核的发货单生成应收单，是否继续？').then(() => {
    return generateAr()
  }).then(res => {
    lastResult.value = res.data
    showResult.value = true
    getArList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

/** 同步已收金额 */
function handleSyncReceived() {
  proxy.$modal.confirm('将从营销系统同步最新的已收金额，是否继续？').then(() => {
    return syncReceived()
  }).then(res => {
    proxy.$modal.msgSuccess(`已同步 ${res.data} 张应收单的已收金额`)
    getArList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

/** 登记销项发票 */
function handleInvoice() {
  if (!canInvoice.value) {
    proxy.$modal.msgWarning('请先选择一张应收单')
    return
  }
  invoiceForm.invoiceNo = current.value.invoiceNo || ''
  invoiceForm.invoiceCode = current.value.invoiceCode || ''
  invoiceForm.invoiceDate = current.value.invoiceDate
    ? String(current.value.invoiceDate).slice(0, 10) : ''
  showInvoice.value = true
}

function submitInvoice() {
  if (!invoiceForm.invoiceNo || !invoiceForm.invoiceNo.trim()) {
    proxy.$modal.msgError('销项发票号码不能为空')
    return
  }
  invoiceAr(current.value.arId, invoiceForm.invoiceNo.trim(),
            invoiceForm.invoiceCode, invoiceForm.invoiceDate).then(() => {
    proxy.$modal.msgSuccess('销项发票登记成功')
    showInvoice.value = false
    getArList()
  })
}

/** 坏账计提 */
function handleBadDebt() {
  const d = new Date()
  badDebtPeriod.value = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`
  proxy.$modal.confirm(`将按账龄区间比例对${badDebtPeriod.value} 期间的逾期应收计提坏账（1-30天5%、31-60天10%、61-90天30%、90天以上50%），是否继续？`).then(() => {
    return badDebt(badDebtPeriod.value)
  }).then(res => {
    const d2 = res.data
    if (d2.count === 0) {
      proxy.$modal.msgInfo('本期无逾期应收，无需计提')
    } else {
      proxy.$modal.msgSuccess(`计提 ${d2.count} 笔，合计 ${formatAmount(d2.totalAmount)} 元`)
    }
    getArList(); loadSummary()
  }).catch(() => {})
}

/** 查看详情（独立只读弹窗） */
function handleView(row) {
  getAr(row.arId).then(res => {
    current.value = res.data.ap
    writeoffList.value = res.data.writeoffs || []
    viewOpen.value = true
  })
}

function handleWriteoff() {
  if (!canWriteoff.value) {
    proxy.$modal.msgWarning('请选择未核销或部分核销的应收单')
    return
  }
  writeoffAmount.value = String(detailUnreceived.value.toFixed(2))
  writeoffRemark.value = ''
  showWriteoff.value = true
}

function submitWriteoff() {
  const amt = Number(writeoffAmount.value)
  if (!amt || amt <= 0) {
    proxy.$modal.msgError('核销金额必须大于 0')
    return
  }
  if (amt > detailUnreceived.value) {
    proxy.$modal.msgError(`核销金额不能超过未付余额 ${formatAmount(detailUnreceived.value)}`)
    return
  }
  writeoffAr(current.value.arId, amt, writeoffRemark.value).then(() => {
    proxy.$modal.msgSuccess('核销成功')
    showWriteoff.value = false
    getArList(); loadSummary(); loadOverdue()
    // 刷新当前单据，避免详情弹框显示过期数据
    if (current.value && current.value.arId) {
      getAr(current.value.arId).then(res => {
        current.value = res.data.ap
        writeoffList.value = res.data.writeoffs || []
      })
    }
  })
}

function handleVoid(row) {
  proxy.$modal.confirm(`是否确认作废应收单「${row.apNo}」？作废后不再参与账龄统计。`).then(() => {
    return voidAr(row.arId, '手工作废')
  }).then(() => {
    proxy.$modal.msgSuccess('作废成功')
    getArList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

function loadAging() {
  getArAging().then(res => { agingList.value = res.data || [] })
}

function handleExport() {
  proxy.download('/fin/ap/export', { ...queryParams.value }, `应收单_${new Date().getTime()}.xlsx`)
}

getArList()
loadSummary()
loadOverdue()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.fin-ar-page {
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
.fin-ar-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Summary Bar ===== */
.fin-ar-page .summary-bar { display:flex; gap:32px; padding:16px 20px; flex-wrap:wrap; }
.fin-ar-page .summary-label { font-size:12px; color:var(--ink-500); }
.fin-ar-page .summary-value { font-size:20px; font-weight:600; font-variant-numeric:tabular-nums; margin:4px 0; color:var(--ink-900); }
.fin-ar-page .summary-unit { font-size:12px; margin-left:2px; font-weight:400; }

/* ===== Filter Card ===== */
.fin-ar-page .filter-card { padding:14px 20px 16px; }
.fin-ar-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.fin-ar-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.fin-ar-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.fin-ar-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.fin-ar-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.fin-ar-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.fin-ar-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.fin-ar-page .field { display:flex; flex-direction:column; gap:6px; }
.fin-ar-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.fin-ar-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.fin-ar-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.fin-ar-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.fin-ar-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.fin-ar-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.fin-ar-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.fin-ar-page .field .control :deep(.el-select) { width:100%; }
.fin-ar-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.fin-ar-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.fin-ar-page .field .control :deep(.el-date-editor) { width:100%; }
.fin-ar-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.fin-ar-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.fin-ar-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.fin-ar-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.fin-ar-page .toolbar .left { display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
.fin-ar-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== Table ===== */
.fin-ar-page .table-wrap { overflow-x:auto; }
.fin-ar-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:var(--ink-200); --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.fin-ar-page .el-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.fin-ar-page .el-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.fin-ar-page .el-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.fin-ar-page .el-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.fin-ar-page .el-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:#dc2626; font-weight:600; }
.fin-ar-page .amount-cell { font-variant-numeric:tabular-nums; }
.fin-ar-page .amount-cell.strong { font-weight:600; }
.fin-ar-page .text-muted { color:var(--ink-400); }
.fin-ar-page .text-received { color:var(--green-700); }
.fin-ar-page .text-unreceived { color:#b85450; }
.fin-ar-page .text-success { color:var(--green-700); font-weight:600; }
.fin-ar-page .text-danger { color:var(--red-700); font-weight:600; }

/* ===== Badges ===== */
.fin-ar-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.fin-ar-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.fin-ar-page .badge.badge-amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.fin-ar-page .badge.badge-amber .dot { background:var(--amber-500); }
.fin-ar-page .badge.badge-blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.fin-ar-page .badge.badge-blue .dot { background:var(--blue-500); }
.fin-ar-page .badge.badge-green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.fin-ar-page .badge.badge-green .dot { background:var(--green-500); }
.fin-ar-page .badge.badge-red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.fin-ar-page .badge.badge-red .dot { background:var(--red-500); }
.fin-ar-page .badge.badge-violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.fin-ar-page .badge.badge-violet .dot { background:var(--brand-500); }
.fin-ar-page .badge.badge-gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.fin-ar-page .badge.badge-gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.fin-ar-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.fin-ar-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.fin-ar-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.fin-ar-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.fin-ar-page .pagination-container :deep(.el-pagination .btn-prev), .fin-ar-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .fin-ar-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .fin-ar-page .filter-card .filter-bar { grid-template-columns:1fr; } .fin-ar-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== 操作列 ===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 详情弹窗（rd- 规范） ===== */
:deep(.rd-dialog .el-dialog__header) { padding:0; margin:0; border:none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top:14px; right:14px; z-index:10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color:#fff; font-size:20px; }
:deep(.rd-dialog .el-dialog__headerbtn:hover .el-dialog__close) { color:#fff; }
:deep(.rd-dialog .el-dialog__body) { padding:12px 16px 16px; background:linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width:none; }

.rd-detail-header { display:flex; align-items:center; gap:12px; padding:12px 20px; background:linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius:12px 12px 0 0; position:relative; overflow:hidden; }
.rd-detail-header::before { content:''; position:absolute; top:-30px; right:-15px; width:140px; height:140px; border-radius:50%; background:radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events:none; }
.rd-detail-header-icon { display:flex; align-items:center; justify-content:center; width:40px; height:40px; border-radius:10px; background:rgb(255 255 255 / 0.2); border:1px solid rgb(255 255 255 / 0.25); color:#fff; flex-shrink:0; backdrop-filter:blur(4px); box-shadow:0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-main { display:flex; flex-direction:column; gap:4px; min-width:0; padding-right:36px; }
.rd-detail-header-title { font-size:18px; font-weight:700; color:#fff; letter-spacing:-0.02em; line-height:1.3; }
.rd-detail-header-sub { display:flex; align-items:center; gap:12px; flex-wrap:wrap; }
.rd-detail-header-no { font-size:13px; font-weight:500; color:rgb(255 255 255 / 0.8); font-variant-numeric:tabular-nums; }

.rd-page { max-width:840px; margin:0 auto; }
.rd-card { background:#fff; border-radius:12px; border:1px solid #e5e7eb; box-shadow:0 1px 2px 0 rgb(0 0 0 / 0.05); overflow:hidden; margin-bottom:8px; transition:box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation:rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow:0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom:0; }
.rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:8px 16px; background:linear-gradient(to right, #f9fafb, #fff); border-bottom:1px solid #f3f4f6; }
.rd-card-title { display:flex; align-items:center; gap:8px; font-size:16px; font-weight:700; color:#111827; }
.rd-card-icon { display:flex; align-items:center; justify-content:center; width:32px; height:32px; border-radius:8px; background:#fff; border:1px solid #e5e7eb; color:#2563eb; box-shadow:0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-body { padding:14px 16px; }
.rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:12px 24px; }
.rd-item { display:flex; align-items:center; gap:12px; }
.rd-item--full { grid-column:1 / -1; }
.rd-label { flex:0 0 auto; min-width:72px; display:flex; align-items:center; font-size:14px; font-weight:500; color:#6b7280; white-space:nowrap; }
.rd-value { flex:1 1 auto; font-size:14px; font-weight:500; color:#111827; line-height:1.5; padding-left:12px; border-left:1px solid #e5e7eb; min-width:0; }
.rd-value--large { font-size:18px; font-weight:700; }
.rd-amount { font-variant-numeric:tabular-nums; font-weight:700; color:#dc2626; }
.rd-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:20px; text-align:center; color:#9ca3af; background:#f9fafb; border-radius:12px; border:1px dashed #e5e7eb; }
.rd-empty-text { font-size:14px; font-weight:500; margin:0; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }
.rd-card:nth-child(2) { animation-delay:0.06s; }
.rd-card:nth-child(3) { animation-delay:0.12s; }
@media (max-width:768px) {
  .rd-grid { grid-template-columns:1fr; }
  .rd-card-header { padding:8px 12px; }
  .rd-card-body { padding:12px; }
}

.hint-text { font-size:12px; }
.divider-text { font-size:13px; }
</style>
