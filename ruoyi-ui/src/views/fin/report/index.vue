<template>
  <div class="app-container fin-report-page">
    <!-- 会计恒等式校验条 -->
    <div class="surface equation-bar" :class="checkClass">
      <div class="eq-left">
        <span class="eq-label">会计恒等式校验</span>
        <span class="eq-formula">
          资产 <b class="asset">{{ formatAmount(check.totalAsset) }}</b>
          ＝ 负债 <b class="liab">{{ formatAmount(check.totalLiability) }}</b>
          ＋ 所有者权益 <b class="equity">{{ formatAmount(check.totalEquity) }}</b>
        </span>
      </div>
      <div class="eq-right">
        <template v-if="checked">
          <span class="eq-diff">差额 <b>{{ formatAmount(check.diff) }}</b></span>
          <el-tag :type="check.balanced ? 'success' : 'danger'" effect="dark" size="small">
            {{ check.balanced ? '报表平衡' : '报表不平衡，请检查凭证' }}
          </el-tag>
        </template>
        <el-button v-else type="primary" size="small" icon="Search" @click="loadBalanceCheck">执行校验</el-button>
      </div>
    </div>

    <div class="surface filter-card">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>会计期间</label>
          <div class="control is-select">
            <el-select v-model="period" placeholder="请选择" style="width: 150px">
              <el-option v-for="p in periodOptions" :key="p" :label="formatPeriod(p)" :value="p" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 选择会计期间后查询对应报表</div>
        <div class="filter-buttons">
          <el-button icon="Download" @click="handleExport">导出</el-button>
          <el-button type="primary" icon="Search" @click="loadAll">查询报表</el-button>
        </div>
      </div>
    </div>

    <div class="surface">
      <!-- 菜单重构：页签已独立为菜单，隐藏页签头，按路由 path 激活对应报表 -->
      <el-tabs v-model="tab" @tab-change="onTabChange" class="tabs-no-header">
        <!-- 资产负债表 -->
        <el-tab-pane v-if="canSee('fin:report:assets')" label="资产负债表" name="assets">
          <div class="report-head">
            <h3 class="report-title">资产负债表</h3>
            <span class="report-period">{{ formatPeriod(period) }}</span>
            <span class="report-unit">单位：元</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="bs-side">
                <div class="bs-side-title">资产</div>
                <el-table ref="balanceTableRef" :data="balance.assetRows || []" size="small" border
                          class="report-table" @header-dragend="balanceCol.onHeaderDragEnd">
                  <el-table-column key="accountCode" prop="accountCode" label="科目编码" :width="balanceCol.colWidth('accountCode', 90)" resizable align="center" />
                  <el-table-column key="accountName" prop="accountName" label="资产项目" :min-width="balanceCol.colWidth('accountName', 150)" resizable align="center" show-overflow-tooltip />
                  <el-table-column key="closingBalance" prop="closingBalance" label="期末余额" :width="balanceCol.colWidth('closingBalance', 130)" resizable align="center">
                    <template #default="{ row }">{{ formatAmount(row.closingBalance) }}</template>
                  </el-table-column>
                </el-table>
                <div class="bs-total">
                  <span>资产总计</span>
                  <b>{{ formatAmount(balance.totalAsset) }}</b>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="bs-side">
                <div class="bs-side-title">负债和所有者权益</div>
                <el-table :data="[...(balance.liabilityRows || []), ...(balance.equityRows || [])]"
                          size="small" border class="report-table" @header-dragend="balanceCol.onHeaderDragEnd">
                  <el-table-column key="accountCode" prop="accountCode" label="科目编码" :width="balanceCol.colWidth('accountCode', 90)" resizable align="center" />
                  <el-table-column key="accountName" prop="accountName" label="负债和所有者权益项目" :min-width="balanceCol.colWidth('accountName', 150)" resizable align="center" show-overflow-tooltip />
                  <el-table-column key="closingBalance" prop="closingBalance" label="期末余额" :width="balanceCol.colWidth('closingBalance', 130)" resizable align="center">
                    <template #default="{ row }">
                      {{ formatAmount(Number(row.closingBalance || 0) * -1) }}
                    </template>
                  </el-table-column>
                </el-table>
                <div class="bs-total">
                  <span>负债和所有者权益总计</span>
                  <b>{{ formatAmount(Number(balance.totalLiability || 0) + Number(balance.totalEquity || 0)) }}</b>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 利润表 -->
        <el-tab-pane v-if="canSee('fin:report:profit')" label="利润表" name="profit">
          <div class="report-head">
            <h3 class="report-title">利润表</h3>
            <span class="report-period">{{ formatPeriod(period) }}</span>
            <span class="report-unit">单位：元</span>
          </div>
          <el-table ref="incomeTableRef" :data="income.incomeRows || []" size="small" border
                    class="report-table income-table" @header-dragend="incomeCol.onHeaderDragEnd">
            <el-table-column key="accountName" prop="accountName" label="项目" :min-width="incomeCol.colWidth('accountName', 200)" resizable align="center" show-overflow-tooltip />
            <el-table-column key="ytdAmount" prop="ytdAmount" label="本期金额" :width="incomeCol.colWidth('ytdAmount', 150)" resizable align="center">
              <template #default="{ row }">{{ formatAmount(row.ytdAmount) }}</template>
            </el-table-column>
          </el-table>
          <div class="stat-row">
            <span>一、营业收入</span>
            <b class="income-value">{{ formatAmount(income.totalRevenue) }}</b>
          </div>
          <el-table :data="income.costRows || []" size="small" border
                    class="report-table income-table" @header-dragend="incomeCol.onHeaderDragEnd">
            <el-table-column key="accountName" prop="accountName" label="项目" :min-width="incomeCol.colWidth('accountName', 200)" resizable align="center" show-overflow-tooltip />
            <el-table-column key="ytdAmount" prop="ytdAmount" label="本年累计" :width="incomeCol.colWidth('ytdAmount', 150)" resizable align="center">
              <template #default="{ row }">{{ formatAmount(row.ytdAmount) }}</template>
            </el-table-column>
          </el-table>
          <div class="stat-row">
            <span>减：营业成本与费用</span>
            <b class="cost-value">{{ formatAmount(income.totalCost) }}</b>
          </div>
          <div class="stat-row total-row" :class="Number(income.totalProfit) >= 0 ? 'is-profit' : 'is-loss'">
            <span>三、利润总额</span>
            <b>{{ formatAmount(income.totalProfit) }}</b>
          </div>
        </el-tab-pane>

        <!-- 现金流量表 -->
        <el-tab-pane v-if="canSee('fin:report:cashflow')" label="现金流量表" name="cashflow">
          <div class="report-head">
            <h3 class="report-title">现金流量表</h3>
            <span class="report-period">{{ formatPeriod(period) }}（本年累计）</span>
            <span class="report-unit">单位：元</span>
          </div>
          <el-table ref="cashflowTableRef" :data="cashSummary" size="small" border
                    class="report-table" @header-dragend="cashflowCol.onHeaderDragEnd">
            <el-table-column key="label" prop="label" label="项目" :min-width="cashflowCol.colWidth('label', 240)" resizable align="center" show-overflow-tooltip />
            <el-table-column key="value" prop="value" label="金额" :width="cashflowCol.colWidth('value', 180)" resizable align="center">
              <template #default="{ row }">
                <span class="amount-cell" :class="Number(row.value) >= 0 ? 'text-in' : 'text-out'">
                  {{ formatAmount(row.value) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          <div class="stat-row total-row">
            <span>现金及现金等价物净增加额</span>
            <b>{{ formatAmount(cashFlow.netIncrease) }}</b>
          </div>
          <el-divider content-position="left"><span class="divider-text">明细（按科目）</span></el-divider>
          <el-table :data="cashFlow.detail || []" size="small" border max-height="360"
                    class="report-table" @header-dragend="cashflowCol.onHeaderDragEnd">
            <el-table-column key="cashFlowCategory" prop="cashFlowCategory" label="分类" :width="cashflowCol.colWidth('cashFlowCategory', 110)" resizable align="center">
              <template #default="{ row }">
                <el-tag :type="cfTag(row.cashFlowCategory)" effect="plain" size="small">
                  {{ cfLabel(row.cashFlowCategory) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column key="accountCode" prop="accountCode" label="科目编码" :width="cashflowCol.colWidth('accountCode', 100)" resizable align="center" />
            <el-table-column key="accountName" prop="accountName" label="科目名称" :min-width="cashflowCol.colWidth('accountName', 160)" resizable align="center" show-overflow-tooltip />
            <el-table-column key="inflowAmount" prop="inflowAmount" label="流入" :width="cashflowCol.colWidth('inflowAmount', 130)" resizable align="center">
              <template #default="{ row }">{{ formatAmount(row.inflowAmount) }}</template>
            </el-table-column>
            <el-table-column key="outflowAmount" prop="outflowAmount" label="流出" :width="cashflowCol.colWidth('outflowAmount', 130)" resizable align="center">
              <template #default="{ row }">{{ formatAmount(row.outflowAmount) }}</template>
            </el-table-column>
            <el-table-column key="netAmount" prop="netAmount" label="净额" :width="cashflowCol.colWidth('netAmount', 140)" resizable align="center">
              <template #default="{ row }">
                <span class="amount-cell" :class="Number(row.netAmount) >= 0 ? 'text-in' : 'text-out'">
                  {{ formatAmount(row.netAmount) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 往来对账 -->
        <el-tab-pane v-if="canSee('fin:report:ledger')" label="往来对账" name="reconcile">
          <el-row :gutter="16" class="summary-mini-row">
            <el-col :span="12">
              <div class="mini-card">
                <div class="mini-label">应收账款余额</div>
                <div class="mini-value text-unreceived">{{ formatAmount(recon.totalAr) }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="mini-card">
                <div class="mini-label">应付账款余额</div>
                <div class="mini-value asset">{{ formatAmount(recon.totalAp) }}</div>
              </div>
            </el-col>
          </el-row>
          <el-tabs>
            <el-tab-pane :label="`应收对账（${(recon.arList || []).length}）`">
              <el-table ref="reconTableRef" :data="recon.arList || []" size="small" border max-height="420"
                        class="report-table" @header-dragend="reconCol.onHeaderDragEnd">
                <el-table-column key="partnerName" prop="partnerName" label="客户" :min-width="reconCol.colWidth('partnerName', 200)" resizable align="center" show-overflow-tooltip />
                <el-table-column key="arCount" prop="arCount" label="单数" :width="reconCol.colWidth('arCount', 80)" resizable align="center" />
                <el-table-column key="totalAmount" prop="totalAmount" label="应收总额" :width="reconCol.colWidth('totalAmount', 140)" resizable align="center">
                  <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
                </el-table-column>
                <el-table-column key="receivedAmount" prop="receivedAmount" label="已收" :width="reconCol.colWidth('receivedAmount', 140)" resizable align="center">
                  <template #default="{ row }">{{ formatAmount(row.receivedAmount) }}</template>
                </el-table-column>
                <el-table-column key="unreceivedAmount" prop="unreceivedAmount" label="未收余额" :width="reconCol.colWidth('unreceivedAmount', 140)" resizable align="center">
                  <template #default="{ row }">
                    <span class="text-unreceived">{{ formatAmount(row.unreceivedAmount) }}</span>
                  </template>
                </el-table-column>
                <el-table-column key="badDebtAmount" prop="badDebtAmount" label="已提坏账" :width="reconCol.colWidth('badDebtAmount', 120)" resizable align="center">
                  <template #default="{ row }">{{ formatAmount(row.badDebtAmount) }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane :label="`应付对账（${(recon.apList || []).length}）`">
              <el-table :data="recon.apList || []" size="small" border max-height="420"
                        class="report-table" @header-dragend="reconCol.onHeaderDragEnd">
                <el-table-column key="partnerName" prop="partnerName" label="供应商" :min-width="reconCol.colWidth('partnerName', 200)" resizable align="center" show-overflow-tooltip />
                <el-table-column key="apCount" prop="apCount" label="单数" :width="reconCol.colWidth('apCount', 80)" resizable align="center" />
                <el-table-column key="totalAmount" prop="totalAmount" label="应付总额" :width="reconCol.colWidth('totalAmount', 140)" resizable align="center">
                  <template #default="{ row }">{{ formatAmount(row.totalAmount) }}</template>
                </el-table-column>
                <el-table-column key="paidAmount" prop="paidAmount" label="已付" :width="reconCol.colWidth('paidAmount', 140)" resizable align="center">
                  <template #default="{ row }">{{ formatAmount(row.paidAmount) }}</template>
                </el-table-column>
                <el-table-column key="unpaidAmount" prop="unpaidAmount" label="未付余额" :width="reconCol.colWidth('unpaidAmount', 140)" resizable align="center">
                  <template #default="{ row }">
                    <span class="asset">{{ formatAmount(row.unpaidAmount) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <!-- 账龄分析 -->
        <el-tab-pane v-if="canSee('fin:report:aging')" label="账龄分析" name="aging">
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="bs-side-title">应收账龄</div>
              <el-table ref="agingTableRef" :data="aging.arAging || []" size="small" border
                        class="report-table" @header-dragend="agingCol.onHeaderDragEnd">
                <el-table-column key="bucket" prop="bucket" label="账龄区间" :width="agingCol.colWidth('bucket', 120)" resizable align="center" />
                <el-table-column key="cnt" prop="cnt" label="笔数" :width="agingCol.colWidth('cnt', 80)" resizable align="center" />
                <el-table-column key="unreceivedAmount" prop="unreceivedAmount" label="未收金额" :width="agingCol.colWidth('unreceivedAmount', 140)" resizable align="center">
                  <template #default="{ row }">
                    <span class="text-unreceived">{{ formatAmount(row.unreceivedAmount) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
            <el-col :span="12">
              <div class="bs-side-title">应付账龄</div>
              <el-table :data="aging.apAging || []" size="small" border
                        class="report-table" @header-dragend="agingCol.onHeaderDragEnd">
                <el-table-column key="bucket" prop="bucket" label="账龄区间" :width="agingCol.colWidth('bucket', 120)" resizable align="center" />
                <el-table-column key="cnt" prop="cnt" label="笔数" :width="agingCol.colWidth('cnt', 80)" resizable align="center" />
                <el-table-column key="unpaidAmount" prop="unpaidAmount" label="未付金额" :width="agingCol.colWidth('unpaidAmount', 140)" resizable align="center">
                  <template #default="{ row }">
                    <span class="asset">{{ formatAmount(row.unpaidAmount) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup name="FinReport">
import {
  getBalanceSheet, getIncomeStatement, getCashFlow,
  getReconciliation, getReportAging, getBalanceCheck
} from '@/api/fin/report'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()

const tab = ref('assets')

/** 菜单重构（路径A）：财务报表页签独立菜单，按路由 path 激活对应页签 */
const route = useRoute()
const pathTabMap = {
  '/balanceSheet': 'assets',
  '/incomeStatement': 'profit',
  '/cashFlow': 'cashflow',
  '/reconciliation': 'reconcile',
  '/aging': 'aging'
}
const pathTab = pathTabMap[route.path.replace(/^\/fin\/report/, '')]
if (pathTab) {
  tab.value = pathTab
}

/** D-02 修复：Tab 按权限渲染，默认落在第一个有权限的 Tab 上 */
function canSee(perm) {
  return proxy.$auth.hasPermi(perm)
}

const visibleTabNames = ['assets', 'profit', 'cashflow', 'reconcile', 'aging']
  .filter(n => {
    const map = { assets: 'fin:report:assets', profit: 'fin:report:profit',
      cashflow: 'fin:report:cashflow', reconcile: 'fin:report:ledger', aging: 'fin:report:aging' }
    return canSee(map[n])
  })
if (visibleTabNames.length && !visibleTabNames.includes(tab.value)) {
  tab.value = visibleTabNames[0]
}

/** 列宽拖拽持久化：每个报表独立 storageKey（同一报表的并列表格共用一组配置） */
const balanceCol = useColumnResize('fin_report_balance_index')
const incomeCol = useColumnResize('fin_report_income_index')
const cashflowCol = useColumnResize('fin_report_cashflow_index')
const reconCol = useColumnResize('fin_report_recon_index')
const agingCol = useColumnResize('fin_report_aging_index')

const period = ref('')
const periodOptions = ref([])
const balance = ref({})
const income = ref({})
const cashFlow = ref({})
const recon = ref({})
const aging = ref({})
const check = ref({})
const checked = ref(false)

const checkClass = computed(() => {
  if (!checked.value) return ''
  return check.value.balanced ? 'is-balanced' : 'is-unbalanced'
})

const cashSummary = computed(() => [
  { label: '一、经营活动产生的现金流量净额', value: cashFlow.value.operating || 0 },
  { label: '二、投资活动产生的现金流量净额', value: cashFlow.value.investing || 0 },
  { label: '三、筹资活动产生的现金流量净额', value: cashFlow.value.financing || 0 }
])

function formatAmount(v) {
  return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatPeriod(p) {
  if (!p || p.length !== 6) return p || ''
  return `${p.slice(0, 4)}年${p.slice(4)}月`
}

function cfLabel(c) {
  return { OPERATING: '经营活动', INVESTING: '投资活动', FINANCING: '筹资活动' }[c] || c
}

function cfTag(c) {
  return { OPERATING: 'primary', INVESTING: 'warning', FINANCING: 'success' }[c] || 'info'
}

function loadPeriods() {
  const now = new Date()
  const arr = []
  for (let i = 0; i < 24; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    arr.push(`${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}`)
  }
  periodOptions.value = arr
  period.value = arr[0]
}

function loadAll() {
  loadBalanceCheck()
  if (tab.value === 'assets') loadBalance()
  else if (tab.value === 'profit') loadIncome()
  else if (tab.value === 'cashflow') loadCash()
  else if (tab.value === 'reconcile') loadRecon()
  else if (tab.value === 'aging') loadAging()
}

function onTabChange() { loadAll() }

function loadBalance() {
  getBalanceSheet(period.value).then(res => { balance.value = res.data || {}; balanceCol.applySavedWidths() })
}

function loadIncome() {
  getIncomeStatement(period.value).then(res => { income.value = res.data || {}; incomeCol.applySavedWidths() })
}

function loadCash() {
  getCashFlow(period.value).then(res => { cashFlow.value = res.data || {}; cashflowCol.applySavedWidths() })
}

function loadRecon() {
  getReconciliation().then(res => { recon.value = res.data || {}; reconCol.applySavedWidths() })
}

function loadAging() {
  getReportAging().then(res => { aging.value = res.data || {}; agingCol.applySavedWidths() })
}

function loadBalanceCheck() {
  getBalanceCheck(period.value).then(res => {
    check.value = res.data || {}
    checked.value = true
  })
}

/** 导出当前 Tab 的报表（D-01 修复：真实 Excel 导出） */
function handleExport() {
  const names = { assets: '资产负债表', profit: '利润表', cashflow: '现金流量表',
    reconcile: '往来对账表', aging: '账龄分析表' }
  proxy.download('/fin/report/export',
    { reportType: tab.value, period: period.value },
    `${names[tab.value] || tab.value}_${period.value}.xlsx`)
}

loadPeriods()
loadAll()
</script>

<style scoped>
/* ===== design tokens（list-page-style-guide 7.1） ===== */
.fin-report-page {
  --brand: #2563eb;
  --ink: #1e293b;
  --amber: #d97706;
  --blue: #2563eb;
  --green: #16a34a;
  --red: #dc2626;
  --violet: #7c3aed;
}

/* ===== surface 卡片 ===== */
.fin-report-page .surface {
  background: #fff;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

/* 菜单重构：页签已独立为菜单，隐藏页签头，页面仅展示当前路由对应报表 */
.tabs-no-header :deep(.el-tabs__header) {
  display: none;
}

/* ===== 会计恒等式校验条 ===== */
.equation-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  margin-bottom: 12px;
}
.equation-bar.is-balanced { border-left: 3px solid var(--green); }
.equation-bar.is-unbalanced { border-left: 3px solid var(--red); background: #fef2f2; }

.eq-label { font-size: 13px; color: var(--color-text-secondary); margin-right: 16px; font-weight: 500; }
.eq-formula { font-size: 14px; }
.eq-formula b { font-variant-numeric: tabular-nums; margin: 0 4px; }
.eq-formula .asset { color: var(--blue); }
.eq-formula .liab { color: #b85450; }
.eq-formula .equity { color: var(--violet); }

.eq-right { display: flex; align-items: center; gap: 12px; }
.eq-diff { font-size: 13px; }
.eq-diff b { font-variant-numeric: tabular-nums; }

/* ===== 筛选卡片（filter-head / filter-bar / filter-actions 规范结构） ===== */
.filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.filter-title .glyph {
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: var(--brand);
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
}
.filter-bar .field { display: flex; align-items: center; gap: 8px; }
.filter-bar .field label { font-size: 13px; color: var(--color-text-secondary); white-space: nowrap; }
.filter-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}
.filter-info { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--color-text-tertiary); }
.filter-buttons { display: flex; align-items: center; gap: 8px; }

/* ===== 报表区块 ===== */
.report-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 8px 0 12px;
}
.report-title { font-size: 17px; font-weight: 600; margin: 0; color: var(--color-text-primary); }
.report-period { font-size: 13px; color: var(--color-text-secondary); }
.report-unit { font-size: 12px; color: var(--color-text-tertiary); margin-left: auto; }

/* ===== 表格样式（list-page-style-guide 7.3，报表保留边框） ===== */
.report-table {
  --el-table-bg-color: #fff;
  --el-table-header-bg-color: #f8fafc;
  --el-table-row-hover-bg-color: #fafbff;
  --el-table-border-color: #e4e7ed;
  --el-table-text-color: #334155;
  --el-table-header-text-color: #64748b;
}
.report-table :deep(.el-table__header th) {
  font-weight: 600;
}

.bs-side-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; color: var(--color-text-primary); }
.bs-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  margin-top: 8px;
  background: var(--color-background-secondary);
  border-radius: 4px;
  font-size: 13px;
}
.bs-total b { font-size: 15px; font-variant-numeric: tabular-nums; color: var(--blue); }

.income-table { margin-bottom: 4px; }
.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 12px;
  font-size: 13px;
  border-bottom: 1px solid var(--color-border-tertiary);
}
.stat-row b { font-variant-numeric: tabular-nums; }
.income-value { color: var(--green); }
.cost-value { color: #b85450; }
.total-row { font-weight: 600; background: var(--color-background-secondary); }
.total-row.is-profit b { color: var(--green); }
.total-row.is-loss b { color: var(--red); }

.summary-mini-row { margin-bottom: 12px; }
.mini-card {
  padding: 12px 16px;
  border: 1px solid var(--color-border-tertiary);
  border-radius: 6px;
}
.mini-label { font-size: 12px; color: var(--color-text-secondary); }
.mini-value { font-size: 20px; font-weight: 600; font-variant-numeric: tabular-nums; margin-top: 4px; }

.divider-text { font-size: 13px; font-weight: 500; color: var(--color-text-secondary); }
.amount-cell { font-variant-numeric: tabular-nums; }
.text-in { color: var(--green); }
.text-out { color: #b85450; }
.asset { color: var(--blue); }
.text-unreceived { color: #b85450; }
</style>
