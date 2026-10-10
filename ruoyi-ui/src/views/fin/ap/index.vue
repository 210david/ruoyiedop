<template>
  <div class="app-container fin-list-page fin-ap-page">
    <!-- 应付总览 -->
    <div class="surface summary-bar">
      <div class="summary-item">
        <div class="summary-label">应付总额（元）</div>
        <div class="summary-value">{{ formatAmount(summary.totalAmount) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">已付（元）</div>
        <div class="summary-value text-paid">{{ formatAmount(summary.paidAmount) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">未付（元）</div>
        <div class="summary-value text-unpaid">{{ formatAmount(summary.unpaidAmount) }}</div>
      </div>
      <div class="summary-item">
        <div class="summary-label">逾期未付</div>
        <div class="summary-value" :class="overdueList.length > 0 ? 'text-danger' : ''">
          {{ overdueList.length }}<span class="summary-unit">笔</span>
        </div>
      </div>
    </div>

    <!-- ===== 筛选卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.partnerName" placeholder="请输入名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="未核销" value="OPEN" />
              <el-option label="部分核销" value="PARTIAL" />
              <el-option label="已核销" value="SETTLED" />
              <el-option label="已作废" value="VOID" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>应付日期</label>
          <div class="control">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 100%" />
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

    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button v-hasPermi="['fin:ap:add']" type="primary" icon="Plus" @click="handleGenerate">从发票生成应付单</el-button>
          <el-button v-hasPermi="['fin:ap:pay']" type="warning" icon="Refresh" @click="handleSyncPaid">同步已付金额</el-button>
          <el-button v-hasPermi="['fin:ap:writeoff']" type="success" icon="CircleCheck" :disabled="!canWriteoff" @click="handleWriteoff">核销</el-button>
          <el-button icon="DataAnalysis" @click="showAging = true">账龄分析</el-button>
          <el-button icon="Warning" @click="showOverdue = true">逾期预警</el-button>
          <el-button v-hasPermi="['fin:ap:export']" icon="Download" @click="handleExport">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_ap_columns" />
        </div>
      </div>

      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px">
        <template #title>
          <span class="hint-text">
            应付单由<b>已审核的采购发票</b>自动生成，金额不可手工修改；
            已付金额以采购系统付款记录为准，财务侧不重复登记，避免重复付款。
          </span>
        </template>
      </el-alert>

      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="apList"
          border
          class="app-table"
          highlight-current
          @current-change="handleCurrentChange"
          @selection-change="handleSelectionChange"
          @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column prop="apNo" label="应付单号" key="apNo" v-if="columns.apNo.visible" :width="colWidth('应付单号', 160)" resizable align="center" />
          <el-table-column prop="partnerName" label="供应商" key="partnerName" v-if="columns.partnerName.visible" :min-width="colWidth('供应商', 200)" resizable align="center" show-overflow-tooltip />
          <el-table-column prop="sourceKey" label="来源发票" key="sourceKey" v-if="columns.sourceKey.visible" :width="colWidth('来源发票', 150)" resizable align="center">
            <template #default="{ row }">
              <el-tooltip :content="`来源：${row.sourceModule} / ${row.sourceType}`">
                <span class="text-muted">{{ row.sourceKey || '手工' }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="apDate" label="应付日期" key="apDate" v-if="columns.apDate.visible" :width="colWidth('应付日期', 120)" resizable align="center" />
          <el-table-column prop="amountExclTax" label="不含税" key="amountExclTax" v-if="columns.amountExclTax.visible" :width="colWidth('不含税', 120)" resizable align="center">
            <template #default="{ row }">{{ formatAmount(row.amountExclTax) }}</template>
          </el-table-column>
          <el-table-column prop="taxAmount" label="税额" key="taxAmount" v-if="columns.taxAmount.visible" :width="colWidth('税额', 110)" resizable align="center">
            <template #default="{ row }">{{ formatAmount(row.taxAmount) }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="价税合计" key="totalAmount" v-if="columns.totalAmount.visible" :width="colWidth('价税合计', 130)" resizable align="center">
            <template #default="{ row }">
              <span class="rd-amount">{{ formatAmount(row.totalAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="已付" key="paidAmount" v-if="columns.paidAmount.visible" :width="colWidth('已付', 120)" resizable align="center">
            <template #default="{ row }">{{ formatAmount(row.paidAmount) }}</template>
          </el-table-column>
          <el-table-column prop="unpaidAmount" label="未付" key="unpaidAmount" v-if="columns.unpaidAmount.visible" :width="colWidth('未付', 130)" resizable align="center">
            <template #default="{ row }">
              <span class="rd-amount text-unpaid">
                {{ formatAmount(Number(row.totalAmount || 0) - Number(row.paidAmount || 0)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="到期日" key="dueDate" v-if="columns.dueDate.visible" :width="colWidth('到期日', 120)" resizable align="center">
            <template #default="{ row }">
              <span :class="isOverdue(row) ? 'text-danger' : 'text-muted'">
                {{ row.dueDate ? String(row.dueDate).slice(0, 10) : '—' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" key="status" v-if="columns.status.visible" :width="colWidth('状态', 100)" resizable align="center">
            <template #default="{ row }">
              <span class="badge" :class="badgeClass(row.status)">
                <span class="dot"></span>{{ statusLabel(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="{ row }">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(row)">详情</el-button>
                <el-button v-if="row.status !== 'SETTLED' && row.status !== 'VOID'"
                           v-hasPermi="['fin:ap:edit']" link type="warning" icon="Close" @click="handleVoid(row)">作废</el-button>
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

    <!-- ===== 应付单详情（只读弹窗） ===== -->
    <el-dialog v-model="viewOpen" title="" width="860px" append-to-body class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon><Document /></el-icon></div>
          <div>
            <div class="rd-detail-header-title">应付单详情</div>
            <div class="rd-detail-header-sub">
              单号 <span class="rd-detail-header-no">{{ current?.apNo || '—' }}</span>
              <span class="badge" :class="badgeClass(current?.status)" style="margin-left: 8px">
                <span class="dot"></span>{{ statusLabel(current?.status) }}
              </span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="current">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><InfoFilled /></el-icon></span> 基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><div class="rd-label">应付单号</div><div class="rd-value">{{ current.apNo || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">供应商</div><div class="rd-value">{{ current.partnerName || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">来源发票</div><div class="rd-value">{{ current.sourceKey || '手工' }}</div></div>
              <div class="rd-item"><div class="rd-label">发票号码</div><div class="rd-value">{{ current.invoiceNo || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">应付日期</div><div class="rd-value">{{ current.apDate || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">会计期间</div><div class="rd-value">{{ formatPeriod(current.period) || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">不含税金额</div><div class="rd-value">{{ formatAmount(current.amountExclTax) }}</div></div>
              <div class="rd-item"><div class="rd-label">税额</div><div class="rd-value">{{ formatAmount(current.taxAmount) }}</div></div>
              <div class="rd-item"><div class="rd-label">价税合计</div><div class="rd-value rd-value--large"><span class="rd-amount">{{ formatAmount(current.totalAmount) }}</span></div></div>
              <div class="rd-item"><div class="rd-label">已付金额</div><div class="rd-value">{{ formatAmount(current.paidAmount) }}</div></div>
              <div class="rd-item"><div class="rd-label">未付余额</div><div class="rd-value rd-value--large"><span class="rd-amount">{{ formatAmount(detailUnpaid) }}</span></div></div>
              <div class="rd-item"><div class="rd-label">账期</div><div class="rd-value">{{ current.paymentDays ? current.paymentDays + ' 天' : '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">到期日</div><div class="rd-value">{{ current.dueDate || '暂无' }}</div></div>
              <div class="rd-item"><div class="rd-label">状态</div><div class="rd-value"><span class="badge" :class="badgeClass(current.status)"><span class="dot"></span>{{ statusLabel(current.status) }}</span></div></div>
              <div class="rd-item rd-item--full"><div class="rd-label">备注</div><div class="rd-value">{{ current.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><el-icon><Tickets /></el-icon></span> 核销记录</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="writeoffList" border>
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="writeoffDate" label="核销日期" align="center" />
              <el-table-column prop="writeoffAmount" label="核销金额" align="center">
                <template #default="{ row }"><span class="rd-amount">{{ formatAmount(row.writeoffAmount) }}</span></template>
              </el-table-column>
              <el-table-column prop="createBy" label="核销人" align="center" />
              <el-table-column prop="createTime" label="核销时间" align="center" />
              <el-table-column prop="remark" label="备注" align="center" show-overflow-tooltip />
              <template #empty>
                <span class="text-muted">尚未核销</span>
              </template>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 核销弹框 -->
    <el-dialog v-model="showWriteoff" title="付款核销" width="480px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="应付单">
          <el-input :model-value="current?.apNo" disabled />
        </el-form-item>
        <el-form-item label="未付余额">
          <el-input :model-value="formatAmount(detailUnpaid)" disabled />
        </el-form-item>
        <el-form-item label="核销金额" required>
          <el-input v-model="writeoffAmount" type="number" :max="detailUnpaid" placeholder="请输入核销金额" />
        </el-form-item>
        <el-form-item label="快速填写">
          <el-button size="small" @click="writeoffAmount = detailUnpaid">全额 {{ formatAmount(detailUnpaid) }}</el-button>
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
    <el-dialog v-model="showResult" title="应付单生成结果" width="440px" append-to-body>
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

    <!-- 账龄分析 -->
    <el-dialog v-model="showAging" title="应付账龄分析" width="720px" append-to-body>
      <div style="margin-bottom: 12px">
        <el-button type="primary" icon="Search" @click="loadAging">查询</el-button>
      </div>
      <el-table :data="agingList" size="small" border>
        <el-table-column prop="bucket" label="账龄区间" width="180" align="center" />
        <el-table-column prop="cnt" label="笔数" width="100" align="right" />
        <el-table-column label="应付总额" width="150" align="right">
          <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="未付金额" width="150" align="right">
          <template #default="{ row }">
            <span class="text-unpaid">{{ formatAmount(row.unpaidAmount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 逾期预警 -->
    <el-dialog v-model="showOverdue" title="逾期未付预警" width="880px" append-to-body>
      <el-table :data="overdueList" size="small" border max-height="420">
        <el-table-column prop="apNo" label="应付单号" width="150" align="left" />
        <el-table-column prop="partnerName" label="供应商" min-width="180" align="left" />
        <el-table-column prop="dueDate" label="到期日" width="110" align="center" />
        <el-table-column label="未付金额" width="140" align="right">
          <template #default="{ row }">
            <span class="text-danger">{{ formatAmount(Number(row.totalAmount || 0) - Number(row.paidAmount || 0)) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="账龄" width="100" align="center">
          <template #default="{ row }">{{ overdueDays(row) }} 天</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="FinAp">
import {
  listAp, getAp, generateAp, syncPaid, writeoffAp, voidAp, getApAging, getApOverdue, getApSummary
} from '@/api/fin/ap'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_ap_index')

// 列显隐配置（localStorage 持久化，key: fin_ap_columns）
const defaultColumns = {
  apNo: { label: '应付单号', visible: true },
  partnerName: { label: '供应商', visible: true },
  sourceKey: { label: '来源发票', visible: true },
  apDate: { label: '应付日期', visible: true },
  amountExclTax: { label: '不含税', visible: true },
  taxAmount: { label: '税额', visible: true },
  totalAmount: { label: '价税合计', visible: true },
  paidAmount: { label: '已付', visible: true },
  unpaidAmount: { label: '未付', visible: true },
  dueDate: { label: '到期日', visible: true },
  status: { label: '状态', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('fin_ap_columns')
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

const loading = ref(false)
const showSearch = ref(true)
const apList = ref([])
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
const showResult = ref(false)
const showAging = ref(false)
const showOverdue = ref(false)
const writeoffAmount = ref('')
const writeoffRemark = ref('')
const checkedRows = ref([])

// 已选筛选条件数
const activeFilterCount = computed(() => {
  let n = 0
  if (queryParams.value.partnerName) n++
  if (queryParams.value.status) n++
  if (dateRange.value && dateRange.value.length === 2) n++
  return n
})

const queryParams = ref({ pageNum: 1, pageSize: 20, partnerName: undefined, status: undefined })

const detailUnpaid = computed(() => {
  if (!current.value) return 0
  return Number(current.value.totalAmount || 0) - Number(current.value.paidAmount || 0)
})

const canWriteoff = computed(() => {
  const c = current.value
  if (!c) return false
  return ['OPEN', 'PARTIAL'].includes(c.status) && detailUnpaid.value > 0
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
  return { OPEN: 'badge-amber', PARTIAL: 'badge-blue', SETTLED: 'badge-green', VOID: 'badge-gray' }[s] || 'badge-gray'
}

function isOverdue(row) {
  if (!row.dueDate || ['SETTLED', 'VOID'].includes(row.status)) return false
  if (Number(row.totalAmount || 0) - Number(row.paidAmount || 0) <= 0) return false
  return new Date(row.dueDate) < new Date()
}

function overdueDays(row) {
  if (!row.dueDate) return 0
  return Math.floor((Date.now() - new Date(row.dueDate).getTime()) / 86400000)
}

function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.params = { beginApDate: dateRange.value[0], endApDate: dateRange.value[1] }
  }
  listAp(params).then(res => {
    apList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
}

function loadSummary() {
  getApSummary().then(res => { summary.value = res.data || {} })
}

function loadOverdue() {
  getApOverdue().then(res => { overdueList.value = res.data || [] })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 20, partnerName: undefined, status: undefined }
  dateRange.value = []
  getList()
}

/** 勾选行：单选时同步为当前核销对象，避免必须点行才能核销 */
function handleSelectionChange(rows) {
  checkedRows.value = rows
  if (rows.length === 1) {
    current.value = rows[0]
  }
}

function handleCurrentChange(row) { current.value = row }

/** 从发票生成应付单 */
function handleGenerate() {
  proxy.$modal.confirm('将扫描已审核的采购发票生成应付单，是否继续？').then(() => {
    return generateAp()
  }).then(res => {
    lastResult.value = res.data
    showResult.value = true
    getList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

/** 同步已付金额 */
function handleSyncPaid() {
  proxy.$modal.confirm('将从采购系统同步最新的已付金额，是否继续？').then(() => {
    return syncPaid()
  }).then(res => {
    proxy.$modal.msgSuccess(`已同步 ${res.data} 张应付单的已付金额`)
    getList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

function handleView(row) {
  getAp(row.apId).then(res => {
    current.value = res.data.ap
    writeoffList.value = res.data.writeoffs || []
    viewOpen.value = true
  })
}

function handleWriteoff() {
  if (!canWriteoff.value) {
    proxy.$modal.msgWarning('请选择未核销或部分核销的应付单')
    return
  }
  writeoffAmount.value = String(detailUnpaid.value.toFixed(2))
  writeoffRemark.value = ''
  showWriteoff.value = true
}

function submitWriteoff() {
  const amt = Number(writeoffAmount.value)
  if (!amt || amt <= 0) {
    proxy.$modal.msgError('核销金额必须大于 0')
    return
  }
  if (amt > detailUnpaid.value) {
    proxy.$modal.msgError(`核销金额不能超过未付余额 ${formatAmount(detailUnpaid.value)}`)
    return
  }
  writeoffAp(current.value.apId, amt, writeoffRemark.value).then(() => {
    proxy.$modal.msgSuccess('核销成功')
    showWriteoff.value = false
    getList(); loadSummary(); loadOverdue()
    // 刷新当前单据，避免详情弹框显示过期数据
    if (current.value && current.value.apId) {
      getAp(current.value.apId).then(res => {
        current.value = res.data.ap
        writeoffList.value = res.data.writeoffs || []
      })
    }
  })
}

function handleVoid(row) {
  proxy.$modal.confirm(`是否确认作废应付单「${row.apNo}」？作废后不再参与账龄统计。`).then(() => {
    return voidAp(row.apId, '手工作废')
  }).then(() => {
    proxy.$modal.msgSuccess('作废成功')
    getList(); loadSummary(); loadOverdue()
  }).catch(() => {})
}

function loadAging() {
  getApAging().then(res => { agingList.value = res.data || [] })
}

function handleExport() {
  proxy.download('/fin/ap/export', { ...queryParams.value }, `应付单_${new Date().getTime()}.xlsx`)
}

getList()
loadSummary()
loadOverdue()
</script>

<style scoped>
.hint-text { font-size: 12px; }
.divider-text { font-size: 13px; font-weight: 500; color: var(--color-text-secondary); }

.summary-bar { display: flex; gap: 32px; padding: 16px 20px; margin-bottom: 12px; }
.summary-label { font-size: 12px; color: var(--color-text-secondary); }
.summary-value {
  font-size: 20px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  margin: 4px 0;
}
.summary-unit { font-size: 12px; margin-left: 2px; font-weight: 400; }

.amount-cell { font-variant-numeric: tabular-nums; }
.amount-cell.strong { font-weight: 600; }
.text-muted { color: var(--color-text-tertiary); }
.text-paid { color: #16a34a; }
.text-unpaid { color: #b85450; }
.text-success { color: #16a34a; font-weight: 600; }
.text-danger { color: #dc2626; font-weight: 600; }
</style>