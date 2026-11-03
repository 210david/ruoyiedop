<template>
  <div class="app-container hr-perf-page">
    <el-tabs v-model="activeTab" class="perf-tabs">
      <el-tab-pane label="月度业绩归集" name="perf" />
      <el-tab-pane label="线下业绩补录" name="manual" />
    </el-tabs>

    <!-- ===== 归集 Pane ===== -->
    <div v-show="activeTab === 'perf'">
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
            <label>归集周期</label>
            <div class="control is-select">
              <el-date-picker v-model="queryPeriod" type="month" value-format="YYYY-MM" placeholder="选择月份" clearable style="width: 100%" />
            </div>
          </div>
          <div class="field">
            <label>员工姓名</label>
            <div class="control">
              <el-input v-model="perfQuery.employeeName" placeholder="请输入" clearable @keyup.enter="getPerfList" />
            </div>
          </div>
        </div>
        <div class="filter-actions">
          <div class="filter-info">
            <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
            <button class="tip-pill" style="margin-left:8px" @click="showStatusHelp = true">
              <el-icon><QuestionFilled /></el-icon>
              <span>业务操作说明</span>
            </button>
          </div>
          <div class="filter-buttons">
            <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
            <el-button type="primary" icon="Search" @click="getPerfList">搜索</el-button>
          </div>
        </div>
      </div>

      <div class="surface">
        <div class="toolbar">
          <div class="left">
            <el-button type="primary" plain icon="Refresh" @click="handleAggregate" v-hasPermi="['hr:perf:aggregate']">手动归集</el-button>
            <button type="button" class="btn-soft is-outline" @click="showRank">
              <el-icon><Trophy /></el-icon> 业绩排名
            </button>
          </div>
          <div class="right">
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getPerfList" :columns="perfColumns" storageKey="hr_perf_columns" />
          </div>
        </div>
        <div class="table-wrap">
          <el-table ref="perfTableRef" border v-loading="loading" :data="perfList" @header-dragend="onPerfHeaderDragEnd" class="app-table">
            <el-table-column type="index" label="序号" key="序号" :width="colWidthPerf('序号', 70)" align="center" resizable />
            <el-table-column label="归集周期" prop="period" key="period" :width="colWidthPerf('period', 100)" resizable align="center" v-if="perfColumns.period.visible" />
            <el-table-column label="员工姓名" prop="employeeName" key="employeeName" :width="colWidthPerf('employeeName', 120)" resizable v-if="perfColumns.employeeName.visible" />
            <el-table-column label="成交金额" prop="dealAmount" key="dealAmount" :width="colWidthPerf('dealAmount', 140)" resizable align="right" v-if="perfColumns.dealAmount.visible"><template #default="scope"><span>￥{{ formatAmount(scope.row.dealAmount) }}</span></template></el-table-column>
            <el-table-column label="回款金额" prop="paymentAmount" key="paymentAmount" :width="colWidthPerf('paymentAmount', 140)" resizable align="right" v-if="perfColumns.paymentAmount.visible"><template #default="scope"><span>￥{{ formatAmount(scope.row.paymentAmount) }}</span></template></el-table-column>
            <el-table-column label="新开客户数" prop="newCustomerCount" key="newCustomerCount" :width="colWidthPerf('newCustomerCount', 110)" resizable align="center" v-if="perfColumns.newCustomerCount.visible" />
            <el-table-column label="补录业绩" prop="manualAmount" key="manualAmount" :width="colWidthPerf('manualAmount', 130)" resizable align="right" v-if="perfColumns.manualAmount.visible"><template #default="scope"><span>￥{{ formatAmount(scope.row.manualAmount) }}</span></template></el-table-column>
            <el-table-column label="合计业绩" prop="totalAmount" key="totalAmount" :width="colWidthPerf('totalAmount', 140)" resizable align="right" v-if="perfColumns.totalAmount.visible"><template #default="scope"><span class="amount-strong">￥{{ formatAmount(scope.row.totalAmount) }}</span></template></el-table-column>
            <el-table-column label="归集时间" prop="aggTime" key="aggTime" :width="colWidthPerf('aggTime', 160)" resizable align="center" v-if="perfColumns.aggTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.aggTime) }}</span></template></el-table-column>
          </el-table>
        </div>
        <pagination v-show="perfTotal > 0" :total="perfTotal" v-model:page="perfQuery.pageNum" v-model:limit="perfQuery.pageSize" @pagination="getPerfList" />
      </div>
    </div>
    <!-- ===== 补录 Pane ===== -->
    <div v-show="activeTab === 'manual'">
      <div class="surface">
        <div class="toolbar">
          <div class="left">
            <el-button type="primary" plain icon="Plus" @click="manualOpen = true" v-hasPermi="['hr:perf:manualAdd']">发起补录</el-button>
          </div>
          <div class="right">
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getManualList" :columns="manualColumns" storageKey="hr_perf_manual_columns" />
          </div>
        </div>
        <div class="table-wrap">
          <el-table ref="manualTableRef" border v-loading="loading" :data="manualList" @header-dragend="onManualHeaderDragEnd" class="app-table">
            <el-table-column type="index" label="序号" key="序号" :width="colWidthManual('序号', 70)" align="center" resizable />
            <el-table-column label="归集周期" prop="period" key="period" :width="colWidthManual('period', 100)" resizable align="center" v-if="manualColumns.period.visible" />
            <el-table-column label="员工姓名" prop="employeeName" key="employeeName" :width="colWidthManual('employeeName', 120)" resizable v-if="manualColumns.employeeName.visible" />
            <el-table-column label="补录金额" prop="manualAmount" key="manualAmount" :width="colWidthManual('manualAmount', 140)" resizable align="right" v-if="manualColumns.manualAmount.visible"><template #default="scope"><span>￥{{ formatAmount(scope.row.manualAmount) }}</span></template></el-table-column>
            <el-table-column label="补录原因" prop="manualReason" key="manualReason" :min-width="colWidthManual('manualReason', 180)" resizable show-overflow-tooltip v-if="manualColumns.manualReason.visible" />
            <el-table-column label="状态" prop="perfStatus" key="perfStatus" :width="colWidthManual('perfStatus', 100)" resizable align="center" v-if="manualColumns.perfStatus.visible"><template #default="scope"><span v-if="dictLabel(manualStatusOptions, scope.row.perfStatus)" class="badge" :class="statusClass(scope.row.perfStatus)"><span class="dot"></span>{{ dictLabel(manualStatusOptions, scope.row.perfStatus) }}</span></template></el-table-column>
            <el-table-column label="审批人" prop="approver" key="approver" :width="colWidthManual('approver', 100)" resizable v-if="manualColumns.approver.visible" />
            <el-table-column label="审批意见" prop="approveOpinion" key="approveOpinion" :min-width="colWidthManual('approveOpinion', 150)" resizable show-overflow-tooltip v-if="manualColumns.approveOpinion.visible" />
            <el-table-column label="操作" :width="colWidthPerf('操作', 200)" align="center" fixed="right" class-name="col-action">
              <template #default="scope">
                <div class="action-btn-row">
                  <template v-if="scope.row.perfStatus === '0'">
                    <el-button link type="success" icon="CircleCheck" @click="handleApprove(scope.row, true)" v-hasPermi="['hr:perf:approve']">通过</el-button>
                    <el-button link type="danger" icon="CircleClose" @click="handleApprove(scope.row, false)" v-hasPermi="['hr:perf:approve']">驳回</el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <pagination v-show="manualTotal > 0" :total="manualTotal" v-model:page="manualQuery.pageNum" v-model:limit="manualQuery.pageSize" @pagination="getManualList" />
      </div>
    </div>
    <!-- 排名 Dialog -->
    <el-dialog v-model="rankOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 21h8"/><path d="M12 17v4"/><path d="M7 4h10v6a5 5 0 0 1-10 0z"/><path d="M17 5h3a1 1 0 0 1 1 1c0 2.5-2 4-4 4"/><path d="M7 5H4a1 1 0 0 0-1 1c0 2.5 2 4 4 4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">业绩排名（合计业绩降序）</div>
            <div class="rd-detail-header-sub" v-if="rankPeriod"><span class="rd-detail-header-no">周期：{{ rankPeriod }}</span></div>
          </div>
        </div>
      </template>
      <el-table :data="rankList" size="small" border>
        <el-table-column type="index" label="名次" width="70" align="center" />
        <el-table-column label="员工姓名" prop="employeeName" />
        <el-table-column label="合计业绩" prop="totalAmount" align="right"><template #default="scope"><span class="amount-strong">￥{{ formatAmount(scope.row.totalAmount) }}</span></template></el-table-column>
      </el-table>
      <template #footer><el-button @click="rankOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 发起补录 Dialog -->
    <el-dialog v-model="manualOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">发起线下业绩补录</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">MR-02：补录业绩必须经审批通过后计入归集</span></div>
          </div>
        </div>
      </template>
      <el-form ref="manualRef" :model="manualForm" :rules="manualRules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>补录信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="归集周期" prop="period"><el-date-picker v-model="manualForm.period" type="month" value-format="YYYY-MM" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="员工姓名" prop="employeeName"><el-input v-model="manualForm.employeeName" placeholder="请输入员工姓名" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="补录金额" prop="manualAmount"><el-input-number v-model="manualForm.manualAmount" :min="0.01" :precision="2" style="width: 100%" /></el-form-item>
              <el-form-item label="补录原因" prop="manualReason"><el-input v-model="manualForm.manualReason" type="textarea" :rows="2" placeholder="请说明补录原因（MR-02：须走审批）" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitManual">提 交</el-button><el-button @click="manualOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="营销业绩归集业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、业绩归集释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">归集口径说明</div>
          <div class="highlight-card-body">
            <p>• <strong>成交金额：</strong>营销模块销售订单（按订单创建时间归属周期），业务归属 = 客户档案的业务归属人</p>
            <p>• <strong>回款金额：</strong>回款记录（按记录创建时间归属周期）</p>
            <p>• <strong>新开客户数：</strong>周期内建档且完成首笔成交的客户</p>
            <p>• <strong>补录业绩：</strong>线下业绩手工补录，MR-02：必须经审批通过后计入</p>
            <p>• <strong>合计业绩：</strong>成交金额 + 补录业绩；归集支持定时任务（每月1日）与手动触发（幂等重算）</p>
          </div>
        </div>
        <h4>二、补录审批（MR-02）</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">线下业绩补录流程</div>
          <div class="highlight-card-body">
            <p>• 发起补录：填写归集周期、员工、金额与原因，提交后状态为「待审批」</p>
            <p>• 审批：审批通过后计入归集；驳回则不计入，可修改后重新提交</p>
            <p>• 审批通过的补录金额在下次归集（手动或定时）时计入该员工的月度业绩</p>
          </div>
        </div>
        <h4>三、与提成核算的联动（MR-01）</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">口径联动说明</div>
          <div class="highlight-card-body">
            <p>• 提成方案口径为「回款口径」时，核算基数取本页回款金额</p>
            <p>• 提成方案口径为「签单口径」时，核算基数取本页合计业绩（成交 + 补录）</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="HrPerf">
import { listPerf, perfRank, aggregatePerf, listManual, addManual, approveManual } from '@/api/hr/salesPerf'
import { useColumnResize } from '@/composables/useColumnResize'
import { QuestionFilled, ArrowDown } from '@element-plus/icons-vue'
const { colWidthPerf, onPerfHeaderDragEnd, perfTableRef, applyPerfSavedWidths } = useColumnResize('hr_perf_perf')
const { colWidthManual, onManualHeaderDragEnd, manualTableRef, applyManualSavedWidths } = useColumnResize('hr_perf_manual')
const { proxy } = getCurrentInstance()
const { hr_manual_status: manualStatusOptions } = proxy.useDict('hr_manual_status')

const activeTab = ref('perf')
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const loading = ref(false)
const queryPeriod = ref('')
const perfList = ref([])
const perfTotal = ref(0)
const manualList = ref([])
const manualTotal = ref(0)
const rankOpen = ref(false)
const rankList = ref([])
const rankPeriod = ref('')
const manualOpen = ref(false)
const manualForm = ref({})
const perfQuery = ref({ pageNum: 1, pageSize: 10, period: undefined, employeeName: undefined })
const manualQuery = ref({ pageNum: 1, pageSize: 10 })
const manualRules = {
  period: [{ required: true, message: '请选择归集周期', trigger: 'change' }],
  employeeName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  manualAmount: [{ required: true, message: '请输入补录金额', trigger: 'blur' }],
  manualReason: [{ required: true, message: '请输入补录原因（MR-02）', trigger: 'blur' }]
}

function loadCols(key, defaults) {
  try {
    const saved = localStorage.getItem(key)
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaults).forEach(k => { result[k] = { label: defaults[k].label, visible: parsed[k] !== undefined ? parsed[k] : defaults[k].visible } })
      return result
    }
  } catch (e) {}
  return JSON.parse(JSON.stringify(defaults))
}
const perfColumns = ref(loadCols('hr_perf_columns', {
  period: { label: '归集周期', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  dealAmount: { label: '成交金额', visible: true },
  paymentAmount: { label: '回款金额', visible: true },
  newCustomerCount: { label: '新开客户数', visible: true },
  manualAmount: { label: '补录业绩', visible: true },
  totalAmount: { label: '合计业绩', visible: true },
  aggTime: { label: '归集时间', visible: true }
}))
const manualColumns = ref(loadCols('hr_perf_manual_columns', {
  period: { label: '归集周期', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  manualAmount: { label: '补录金额', visible: true },
  manualReason: { label: '补录原因', visible: true },
  perfStatus: { label: '状态', visible: true },
  approver: { label: '审批人', visible: true },
  approveOpinion: { label: '审批意见', visible: true }
}))
const activeFilterCount = computed(() => {
  let count = 0
  if (queryPeriod.value) count++
  if (perfQuery.value.employeeName) count++
  return count
})

function formatAmount(v) { return v != null ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00' }
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function statusClass(val) { const map = { '0': 'amber', '1': 'green', '2': 'red' }; return map[val] || 'gray' }
function getPerfList() {
  loading.value = true
  perfQuery.value.period = queryPeriod.value || undefined
  listPerf(perfQuery.value).then(res => { perfList.value = res.rows; perfTotal.value = res.total; loading.value = false; applyPerfSavedWidths() })
}
function getManualList() { listManual(manualQuery.value).then(res => { manualList.value = res.rows; manualTotal.value = res.total }) }
function resetQuery() { queryPeriod.value = ''; perfQuery.value.employeeName = undefined; getPerfList() }
function handleAggregate() {
  const period = queryPeriod.value || new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 7)
  proxy.$modal.confirm('确认对 ' + period + ' 周期重新归集？将清理该周期快照后重算（幂等）。').then(() => aggregatePerf(period)).then(res => {
    proxy.$modal.msgSuccess('归集完成，共 ' + res.data + ' 条记录')
    getPerfList()
  }).catch(() => {})
}
function showRank() {
  const period = queryPeriod.value || (perfList.value[0] && perfList.value[0].period)
  if (!period) { proxy.$modal.msgError('请先选择归集周期'); return }
  rankPeriod.value = period
  perfRank(period).then(res => { rankList.value = res.data || []; rankOpen.value = true })
}
function handleApprove(row, approved) {
  proxy.$modal.prompt(approved ? '确认通过该补录？可填写审批意见' : '请填写驳回原因').then(({ value }) => {
    approveManual({ manualId: row.manualId, perfStatus: approved ? '1' : '2', approveOpinion: value }).then(() => {
      proxy.$modal.msgSuccess(approved ? '已通过' : '已驳回')
      getManualList()
    })
  }).catch(() => {})
}
function submitManual() {
  proxy.$refs['manualRef'].validate(valid => {
    if (valid) addManual(manualForm.value).then(() => { proxy.$modal.msgSuccess('补录已提交，等待审批'); manualOpen.value = false; getManualList() })
  })
}
getPerfList()
getManualList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.hr-perf-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.hr-perf-page .perf-tabs { margin-bottom: 8px; }
.hr-perf-page .amount-strong { font-weight: 600; color: var(--brand-700); }
.hr-perf-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.hr-perf-page .filter-card { padding:14px 20px 16px; }
.hr-perf-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-perf-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-perf-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-perf-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-perf-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-perf-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-perf-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-perf-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-perf-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-perf-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-perf-page .filter-card .filter-buttons { display:flex; gap:8px; }
.hr-perf-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-perf-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.hr-perf-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-perf-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-perf-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-perf-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-perf-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-perf-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-perf-page .field .control :deep(.el-select) { width:100%; }
.hr-perf-page .field .control :deep(.el-date-editor) { width:100%; }
.hr-perf-page .field .control :deep(.el-date-editor .el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-perf-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-perf-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-perf-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-perf-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-perf-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-perf-page .btn-soft .el-icon { font-size:14px; }
.hr-perf-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-perf-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-perf-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-perf-page .table-wrap { overflow-x:auto; }
.hr-perf-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-perf-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-perf-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-perf-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-perf-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-perf-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-perf-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .hr-perf-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-perf-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-perf-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.hr-perf-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.hr-perf-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-perf-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.hr-perf-page .badge.amber .dot { background:#f59e0b; }
.hr-perf-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-perf-page .badge.green .dot { background:var(--green-500); }
.hr-perf-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-perf-page .badge.red .dot { background:var(--red-500); }
.hr-perf-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-perf-page .badge.gray .dot { background:var(--ink-400); }
/* ===== Tip Pill ===== */
.hr-perf-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:28px; padding:0 10px; font-size:13px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.hr-perf-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
