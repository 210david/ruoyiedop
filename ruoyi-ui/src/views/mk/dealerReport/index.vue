<template>
  <div class="app-container mk-list-page mk-dealer-report-page">
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
          <label>经销商名称</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户编号</label>
          <div class="control">
            <el-input v-model="queryParams.customerNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>经销层级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.dealerLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_dealer_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>授权区域</label>
          <div class="control">
            <el-input v-model="queryParams.authRegions" placeholder="如：华东 / 江苏省" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>信用状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.creditStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_credit_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>档案状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>统计时间范围</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-"
              start-placeholder="开始日期" end-placeholder="结束日期"
              value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 时间范围仅影响销量与流向；返利、账龄为截至当前的时点数据。已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- View Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="handleTabClick('all')">
            <span class="dot"></span><span>全部经销商</span><span class="count">{{ counts.allCount || 0 }}</span>
          </button>
          <button class="status-tab" :class="{ 'is-active': activeTab === 'l1' }" @click="handleTabClick('l1')">
            <span class="dot"></span><span>一级经销商</span><span class="count">{{ counts.level1Count || 0 }}</span>
          </button>
          <button class="status-tab" :class="{ 'is-active': activeTab === 'l2' }" @click="handleTabClick('l2')">
            <span class="dot"></span><span>二级经销商</span><span class="count">{{ counts.level2Count || 0 }}</span>
          </button>
          <button class="status-tab tab-reject" :class="{ 'is-active': activeTab === 'frozen' }" @click="handleTabClick('frozen')">
            <span class="dot"></span><span>信用冻结</span><span class="count">{{ counts.frozenCount || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>口径说明</span>
        </button>
      </div>

      <!-- Summary Strip -->
      <div class="summary-strip">
        <div class="sum-item"><span class="sum-label">经销商数</span><span class="sum-value">{{ fmtNum(totalData.dealerCount) }}</span></div>
        <div class="sum-item"><span class="sum-label">有效订单数</span><span class="sum-value">{{ fmtNum(totalData.orderCount) }}</span></div>
        <div class="sum-item"><span class="sum-label">销售额合计</span><span class="sum-value">{{ fmtAmount(totalData.orderAmount) }}</span></div>
        <div class="sum-item"><span class="sum-label">销量合计</span><span class="sum-value">{{ fmtQty(totalData.shippedQty) }}</span></div>
        <div class="sum-item"><span class="sum-label">累计返利</span><span class="sum-value">{{ fmtAmount(totalData.rebateTotal) }}</span></div>
        <div class="sum-item"><span class="sum-label">未结返利</span><span class="sum-value">{{ fmtAmount(totalData.unsettledRebate) }}</span></div>
        <div class="sum-item"><span class="sum-label">应收余额</span><span class="sum-value">{{ fmtAmount(totalData.receivableAmount) }}</span></div>
        <div class="sum-item is-risk"><span class="sum-label">逾期金额</span><span class="sum-value">{{ fmtAmount(totalData.overdueAmount) }}</span></div>
        <div class="sum-item"><span class="sum-label">最长逾期</span><span class="sum-value">{{ fmtNum(totalData.worstAgingDays) }} 天</span></div>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:dealerReport:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mk_dealer_report_index" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="经销商名称" prop="customerName" key="customerName" :min-width="colWidth('customerName', 170)" resizable show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="客户编号" prop="customerNo" key="customerNo" :min-width="colWidth('customerNo', 130)" resizable show-overflow-tooltip v-if="columns.customerNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.customerNo }}</span></template>
          </el-table-column>
          <el-table-column label="经销层级" prop="dealerLevel" key="dealerLevel" :width="colWidth('dealerLevel', 100)" resizable align="center" v-if="columns.dealerLevel.visible">
            <template #default="scope">
              <span class="badge violet">{{ levelLabel(scope.row.dealerLevel) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="授权区域" prop="authRegions" key="authRegions" :min-width="colWidth('authRegions', 130)" resizable show-overflow-tooltip v-if="columns.authRegions.visible">
            <template #default="scope">{{ scope.row.authRegions || '—' }}</template>
          </el-table-column>
          <el-table-column label="信用状态" prop="creditStatus" key="creditStatus" :width="colWidth('creditStatus', 100)" resizable align="center" v-if="columns.creditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.creditStatus === '1' ? 'red' : 'green'"><span class="dot"></span>{{ creditLabel(scope.row.creditStatus) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="销量（统计期内）" align="center">
            <el-table-column label="有效订单数" prop="orderCount" key="orderCount" :width="colWidth('orderCount', 100)" resizable align="center" v-if="columns.orderCount.visible">
              <template #default="scope">{{ fmtNum(scope.row.orderCount) }}</template>
            </el-table-column>
            <el-table-column label="销售额" prop="orderAmount" key="orderAmount" :width="colWidth('orderAmount', 130)" resizable align="center" v-if="columns.orderAmount.visible">
              <template #default="scope">{{ fmtAmount(scope.row.orderAmount) }}</template>
            </el-table-column>
            <el-table-column label="销量" prop="shippedQty" key="shippedQty" :width="colWidth('shippedQty', 110)" resizable align="center" v-if="columns.shippedQty.visible">
              <template #default="scope">{{ fmtQty(scope.row.shippedQty) }}</template>
            </el-table-column>
          </el-table-column>

          <el-table-column label="返利（截至当前）" align="center">
            <el-table-column label="累计返利" prop="rebateTotal" key="rebateTotal" :width="colWidth('rebateTotal', 120)" resizable align="center" v-if="columns.rebateTotal.visible">
              <template #default="scope">{{ fmtAmount(scope.row.rebateTotal) }}</template>
            </el-table-column>
            <el-table-column label="已兑现" prop="redeemedAmount" key="redeemedAmount" :width="colWidth('redeemedAmount', 110)" resizable align="center" v-if="columns.redeemedAmount.visible">
              <template #default="scope">{{ fmtAmount(scope.row.redeemedAmount) }}</template>
            </el-table-column>
            <el-table-column label="已冲抵" prop="offsetAmount" key="offsetAmount" :width="colWidth('offsetAmount', 110)" resizable align="center" v-if="columns.offsetAmount.visible">
              <template #default="scope">{{ fmtAmount(scope.row.offsetAmount) }}</template>
            </el-table-column>
            <el-table-column label="未结返利" prop="unsettledRebate" key="unsettledRebate" :width="colWidth('unsettledRebate', 120)" resizable align="center" v-if="columns.unsettledRebate.visible">
              <template #default="scope">{{ fmtAmount(scope.row.unsettledRebate) }}</template>
            </el-table-column>
          </el-table-column>

          <el-table-column label="账龄（截至当前）" align="center">
            <el-table-column label="应收余额" prop="receivableAmount" key="receivableAmount" :width="colWidth('receivableAmount', 120)" resizable align="center" v-if="columns.receivableAmount.visible">
              <template #default="scope">{{ fmtAmount(scope.row.receivableAmount) }}</template>
            </el-table-column>
            <el-table-column label="逾期金额" prop="overdueAmount" key="overdueAmount" :width="colWidth('overdueAmount', 120)" resizable align="center" v-if="columns.overdueAmount.visible">
              <template #default="scope">
                <span :class="{ 'num-risk': Number(scope.row.overdueAmount) > 0 }">{{ fmtAmount(scope.row.overdueAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="最长逾期天数" prop="worstAgingDays" key="worstAgingDays" :width="colWidth('worstAgingDays', 110)" resizable align="center" v-if="columns.worstAgingDays.visible">
              <template #default="scope">
                <span :class="{ 'num-risk': Number(scope.row.worstAgingDays) > 0 }">{{ fmtNum(scope.row.worstAgingDays) }}</span>
              </template>
            </el-table-column>
          </el-table-column>

          <el-table-column label="流向（统计期内）" align="center">
            <el-table-column label="流向条数" prop="flowCount" key="flowCount" :width="colWidth('flowCount', 100)" resizable align="center" v-if="columns.flowCount.visible">
              <template #default="scope">{{ fmtNum(scope.row.flowCount) }}</template>
            </el-table-column>
            <el-table-column label="流向数量" prop="flowQty" key="flowQty" :width="colWidth('flowQty', 110)" resizable align="center" v-if="columns.flowQty.visible">
              <template #default="scope">{{ fmtQty(scope.row.flowQty) }}</template>
            </el-table-column>
            <el-table-column label="下游客户数" prop="downstreamCount" key="downstreamCount" :width="colWidth('downstreamCount', 100)" resizable align="center" v-if="columns.downstreamCount.visible">
              <template #default="scope">{{ fmtNum(scope.row.downstreamCount) }}</template>
            </el-table-column>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 口径说明 -->
    <el-dialog v-model="showHelp" title="经销商专项报表口径说明" width="680px" append-to-body>
      <div style="line-height: 1.9; font-size: 14px">
        <p><b>1. 销量：</b>统计期内有效订单（已发货/已签收/已完成）的销售额与发货数量合计；时间范围由筛选条件「统计时间范围」控制。</p>
        <p><b>2. 返利：</b>来自返利结算单——累计返利=全部结算单返利金额；已兑现=已兑现结算单返利金额；已冲抵=已冲抵金额合计；未结返利=未全部冲抵结算单的剩余余额，均为截至当前的时点数据。</p>
        <p><b>3. 账龄：</b>基于回款计划——应收余额=计划金额-已回款；逾期金额=计划回款日已过仍未收部分；最长逾期天数=最老一笔未收计划距今天数，均为截至当前的时点数据。</p>
        <p><b>4. 流向：</b>统计期内分销流向登记的条数、数量及去重下游客户数。</p>
        <p><b>5. 合计条：</b>为当前筛选条件下全量数据的汇总，不受分页影响；导出同样导出筛选条件下全部数据。</p>
      </div>
      <template #footer>
        <el-button @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkDealerReport">
import { listDealerReport, totalDealerReport, countsDealerReport } from '@/api/mk/dealerReport'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { mk_dealer_level, mk_credit_status } = proxy.useDict('mk_dealer_level', 'mk_credit_status')

const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showHelp = ref(false)
const list = ref([])
const total = ref(0)
const activeTab = ref('all')
const dateRange = ref([])
const counts = ref({})
const totalData = ref({})

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_dealer_report_index')

// 列显隐配置
function loadColumnVisibility() {
  const defaultColumns = {
    customerName: { label: '经销商名称', visible: true },
    customerNo: { label: '客户编号', visible: true },
    dealerLevel: { label: '经销层级', visible: true },
    authRegions: { label: '授权区域', visible: true },
    creditStatus: { label: '信用状态', visible: true },
    orderCount: { label: '有效订单数', visible: true },
    orderAmount: { label: '销售额', visible: true },
    shippedQty: { label: '销量', visible: true },
    rebateTotal: { label: '累计返利', visible: true },
    redeemedAmount: { label: '已兑现', visible: true },
    offsetAmount: { label: '已冲抵', visible: true },
    unsettledRebate: { label: '未结返利', visible: true },
    receivableAmount: { label: '应收余额', visible: true },
    overdueAmount: { label: '逾期金额', visible: true },
    worstAgingDays: { label: '最长逾期天数', visible: true },
    flowCount: { label: '流向条数', visible: true },
    flowQty: { label: '流向数量', visible: true },
    downstreamCount: { label: '下游客户数', visible: true }
  }
  try {
    const saved = JSON.parse(localStorage.getItem('mk_dealer_report_columns') || '{}')
    const result = {}
    Object.keys(defaultColumns).forEach(key => {
      result[key] = { label: defaultColumns[key].label, visible: saved[key] !== undefined ? saved[key] : defaultColumns[key].visible }
    })
    return result
  } catch (e) {
    return { ...defaultColumns }
  }
}
const columns = ref(loadColumnVisibility())

const data = reactive({
  queryParams: {
    pageNum: 1, pageSize: 10,
    customerName: undefined, customerNo: undefined, dealerLevel: undefined,
    authRegions: undefined, creditStatus: undefined, status: undefined, params: {}
  }
})
const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  const q = queryParams.value
  let n = 0
  ;['customerName', 'customerNo', 'dealerLevel', 'authRegions', 'creditStatus', 'status'].forEach(k => { if (q[k] !== undefined && q[k] !== '') n++ })
  if (dateRange.value && dateRange.value.length === 2) n++
  return n
})

function levelLabel(level) { const item = mk_dealer_level.value.find(d => d.value == level); return item ? item.label : '-' }
function creditLabel(status) { const item = mk_credit_status.value.find(d => d.value == status); return item ? item.label : '-' }
function fmtAmount(val) { if (val == null || val === '' || isNaN(Number(val))) return '0.00'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }
function fmtQty(val) { if (val == null || val === '' || isNaN(Number(val))) return '0'; return Number(val).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) }
function fmtNum(val) { if (val == null || val === '' || isNaN(Number(val))) return '0'; return Number(val).toLocaleString('zh-CN') }

function getList() {
  loading.value = true
  listDealerReport(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
}

function getTotal() {
  totalDealerReport(queryParams.value).then(res => { totalData.value = res.data || {} }).catch(() => { totalData.value = {} })
}

function loadCounts() {
  countsDealerReport().then(res => { counts.value = res.data || {} }).catch(() => { counts.value = {} })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value)
  queryParams.value.pageNum = 1
  getList()
  getTotal()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    customerName: undefined, customerNo: undefined, dealerLevel: undefined,
    authRegions: undefined, creditStatus: undefined, status: undefined, params: {}
  }
  dateRange.value = []
  activeTab.value = 'all'
  getList()
  getTotal()
}

function handleTabClick(tab) {
  activeTab.value = tab
  if (tab === 'l1') { queryParams.value.dealerLevel = '1'; queryParams.value.creditStatus = undefined }
  else if (tab === 'l2') { queryParams.value.dealerLevel = '2'; queryParams.value.creditStatus = undefined }
  else if (tab === 'frozen') { queryParams.value.dealerLevel = undefined; queryParams.value.creditStatus = '1' }
  else { queryParams.value.dealerLevel = undefined; queryParams.value.creditStatus = undefined }
  queryParams.value.pageNum = 1
  getList()
  getTotal()
}

function handleExport() {
  proxy.download('mk/dealerReport/export', { ...queryParams.value }, `dealer_report_${new Date().getTime()}.xlsx`)
}

loadCounts()
getList()
getTotal()
</script>

<style scoped>
.mk-dealer-report-page .summary-strip {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 0;
  padding: 10px 12px;
  border-bottom: 1px solid var(--ink-200);
  background: #fff;
}
.mk-dealer-report-page .sum-item {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 0 16px;
  border-right: 1px solid var(--ink-200);
  font-feature-settings: "tnum" 1;
}
.mk-dealer-report-page .sum-item:last-child { border-right: none; }
.mk-dealer-report-page .sum-label { font-size: 12px; color: var(--ink-500); white-space: nowrap; }
.mk-dealer-report-page .sum-value { font-size: 15px; font-weight: 700; color: var(--ink-900); white-space: nowrap; }
.mk-dealer-report-page .sum-item.is-risk .sum-value { color: var(--red-600, #dc2626); }
.mk-dealer-report-page .num-risk { color: var(--red-600, #dc2626); font-weight: 600; }
</style>
