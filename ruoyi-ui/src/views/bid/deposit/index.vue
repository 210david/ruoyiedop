<template>
  <div class="app-container bid-deposit-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div></div>
      <div class="filter-bar">
        <div class="field"><label>投标编号/项目</label><div class="control"><el-input v-model="queryParams.projectName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>退回状态</label><div class="control"><el-select v-model="queryParams.refundStatus" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_refund_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>保证金类型</label><div class="control"><el-select v-model="queryParams.depositType" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_deposit_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>缴纳形式</label><div class="control"><el-select v-model="queryParams.payType" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_pay_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>应退时间</label><div class="control"><el-date-picker v-model="refundRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="handleTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ tabCounts.all || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '0' }" @click="handleTabClick('0')"><span class="dot" style="background:var(--amber-500)"></span><span>在外</span><span class="count">{{ tabCounts['0'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '1' }" @click="handleTabClick('1')"><span class="dot" style="background:var(--green-500)"></span><span>已退回</span><span class="count">{{ tabCounts['1'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '3' }" @click="handleTabClick('3')"><span class="dot" style="background:var(--red-500)"></span><span>没收</span><span class="count">{{ tabCounts['3'] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bid:deposit:add']">登记保证金</el-button></div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange"
          :row-class-name="rowClassName">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="投标编号" prop="bidNo" key="bidNo" width="180" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="项目名称" prop="projectName" key="projectName" :min-width="colWidth('projectName', 240)" show-overflow-tooltip resizable />
          <el-table-column label="类型" prop="depositType" key="depositType" width="110" align="center">
            <template #default="scope"><span class="badge violet">{{ dictLabel(bid_deposit_type, scope.row.depositType) }}</span></template>
          </el-table-column>
          <el-table-column label="形式" prop="payType" key="payType" width="100" align="center">
            <template #default="scope"><span class="badge blue">{{ dictLabel(bid_pay_type, scope.row.payType) }}</span></template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" key="amount" width="130" align="center" sortable="custom" :sort-orders="['descending','ascending']">
            <template #default="scope"><span class="amount-red">￥{{ formatAmount(scope.row.amount) }}</span></template>
          </el-table-column>
          <el-table-column label="缴纳时间" prop="payTime" key="payTime" width="170" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="应退时间" prop="expectRefundTime" key="expectRefundTime" width="170" align="center" sortable="custom" :sort-orders="['descending','ascending']">
            <template #default="scope">{{ overdueText(scope.row) }}</template>
          </el-table-column>
          <el-table-column label="退回状态" prop="refundStatus" key="refundStatus" width="110" align="center">
            <template #default="scope"><span class="badge" :class="refundBadge(scope.row.refundStatus)">{{ dictLabel(bid_refund_status, scope.row.refundStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button link type="success" icon="SuccessFilled" @click="handleRefund(scope.row)" v-if="scope.row.refundStatus === '0' || scope.row.refundStatus === '2'" v-hasPermi="['bid:deposit:edit']">退回</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bid:deposit:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="depositRef" :model="form" :rules="rules" label-width="110px">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="投标项目" prop="projectId">
                <el-input v-model="form.projectName" readonly placeholder="请输入投标项目ID查询选择" @click="promptProjectId">
                  <template #append><el-button icon="Search" @click="promptProjectId" /></template>
                </el-input>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="保证金类型" prop="depositType"><el-select v-model="form.depositType" style="width:100%"><el-option v-for="d in bid_deposit_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="缴纳形式" prop="payType"><el-select v-model="form.payType" style="width:100%"><el-option v-for="d in bid_pay_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="缴纳金额" prop="amount"><el-input-number v-model="form.amount" :precision="2" :min="0" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="缴纳时间" prop="payTime"><el-date-picker v-model="form.payTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="应退时间" prop="expectRefundTime"><el-date-picker v-model="form.expectRefundTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="缴纳凭证" prop="voucherUrl"><file-upload v-model="form.voucherUrl" :fileSize="10" :fileType="['png','jpg','jpeg','pdf','doc','docx']" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="refundOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg></div><span class="rd-detail-header-title">登记退回</span></div></template>
      <el-form label-width="110px">
        <el-form-item label="实际退回时间" required><el-date-picker v-model="refundForm.actualRefundTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="refundForm.remark" type="textarea" :rows="3" show-word-limit maxlength="500" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitRefund">确 定</el-button><el-button @click="refundOpen = false">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="showStatusHelp" title="保证金台账业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary"><div class="highlight-card-title">保证金台账管什么？</div>
          <div class="highlight-card-body"><p>投标保证金的缴纳-占用-退回全程台账。开标结束后若长期未退回，系统每天扫描并向负责人推送超期提醒，避免沉淀资金流失。</p></div></div>
        <h4>二、操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>缴纳登记：</strong>缴纳后立即登记金额/时间/凭证</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>超期预警：</strong>超过应退时间未登记退回，行标红并推送提醒</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>退回登记：</strong>收到退回后点击「退回」登记实际时间</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="BidDeposit">
import { listDeposit, getDeposit, addDeposit, updateDeposit, delDeposit, refundDeposit } from '@/api/bid/deposit'
import { useColumnResize } from '@/composables/useColumnResize'
import { formatAmount } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { bid_refund_status, bid_deposit_type, bid_pay_type } = proxy.useDict('bid_refund_status', 'bid_deposit_type', 'bid_pay_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_deposit_index')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const showSearch = ref(true)
const refundRange = ref([])
const activeTab = ref('all')
const tabCounts = ref({})
const showStatusHelp = ref(false)
const refundOpen = ref(false)
const refundForm = ref({ depositId: undefined, actualRefundTime: undefined, remark: undefined })

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, projectName: undefined, refundStatus: undefined, depositType: undefined, payType: undefined, params: {} },
  rules: {
    projectId: [{ required: true, message: '投标项目不能为空', trigger: 'change' }],
    amount: [{ required: true, message: '缴纳金额不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['projectName','refundStatus','depositType','payType'].filter(k => q[k]).length + (refundRange.value.length ? 1 : 0)
})

function dictLabel(dict, v) { const d = dict.value.find(x => x.value == v); return d ? d.label : v }
function refundBadge(v) { return { '0': 'amber', '1': 'green', '2': 'blue', '3': 'red' }[v] || 'gray' }
function isOverdue(row) { return row.refundStatus === '0' && row.expectRefundTime && new Date(row.expectRefundTime) < new Date() }
function overdueText(row) {
  if (!row.expectRefundTime) return '—'
  const base = String(row.expectRefundTime).slice(0, 16)
  return isOverdue(row) ? base + '（超期）' : base
}
function rowClassName({ row }) { return isOverdue(row) ? 'overdue-row' : '' }

function getList() {
  loading.value = true
  listDeposit(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; tabCounts.value = { all: res.total }; loading.value = false; applySavedWidths() })
}
function handleTabClick(tab) { activeTab.value = tab; queryParams.value.refundStatus = tab === 'all' ? undefined : tab; handleQuery() }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, refundRange.value, 'ExpectRefundTime'); getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; refundRange.value = []; activeTab.value = 'all'; handleQuery() }
function handleSortChange(column) {
  if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' }
  else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }
  getList()
}

function reset() {
  form.value = { depositId: undefined, projectId: undefined, projectName: undefined, depositType: '1', payType: '1', amount: undefined, payTime: undefined, expectRefundTime: undefined, voucherUrl: undefined, remark: undefined }
  proxy.resetForm('depositRef')
}
function handleAdd() { reset(); open.value = true; title.value = '登记保证金' }
function handleDetail(row) { getDeposit(row.depositId).then(res => { form.value = res.data; open.value = true; title.value = '保证金详情' }) }
function promptProjectId() {
  proxy.$modal.prompt?.('请输入投标项目ID')
  if (!(proxy.$modal && proxy.$modal.prompt)) {
    proxy.$modal.msgInfo('请在投标台账详情中「新增保证金」自动关联，或输入项目ID')
  }
}
function submitForm() {
  proxy.$refs['depositRef'].validate(v => {
    if (v) {
      if (form.value.depositId != null) {
        updateDeposit(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addDeposit(form.value).then(() => { proxy.$modal.msgSuccess('登记成功'); open.value = false; getList() })
      }
    }
  })
}
function cancel() { open.value = false; reset() }
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该保证金记录？').then(() => delDeposit(row.depositId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleRefund(row) {
  refundForm.value = { depositId: row.depositId, actualRefundTime: undefined, remark: undefined }
  refundOpen.value = true
}
function submitRefund() {
  if (!refundForm.value.actualRefundTime) { proxy.$modal.msgError('实际退回时间不能为空'); return }
  refundDeposit(refundForm.value.depositId, refundForm.value.actualRefundTime, refundForm.value.remark).then(() => {
    proxy.$modal.msgSuccess('退回登记成功'); refundOpen.value = false; getList()
  })
}
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-deposit-page { padding-top:10px; --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.bid-deposit-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-deposit-page .filter-head { display:flex; align-items:center; padding:12px 20px 0; }
.bid-deposit-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-deposit-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-deposit-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-deposit-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-deposit-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-deposit-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-deposit-page .filter-buttons { display:flex; gap:8px; }
.bid-deposit-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.bid-deposit-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.bid-deposit-page .tabs-track::-webkit-scrollbar { display:none; }
.bid-deposit-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; white-space:nowrap; border:1px solid transparent; background:transparent; }
.bid-deposit-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.bid-deposit-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.bid-deposit-page .status-tab:hover { background:var(--ink-50); }
.bid-deposit-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.bid-deposit-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; }
.tip-pill:hover { background:#fffbeb; border-color:#f59e0b; }
.bid-deposit-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; }
.bid-deposit-page .toolbar .left { display:flex; gap:8px; }
.bid-deposit-page .pagination-container { padding:10px 12px; }
.bid-deposit-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bid-deposit-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bid-deposit-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bid-deposit-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.bid-deposit-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.bid-deposit-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bid-deposit-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.amount-red { color: var(--red-700); font-weight: 600; }
:deep(.overdue-row) { background: var(--red-50) !important; }
:deep(.col-action) { padding:6px 4px !important; }
:deep(.col-action .cell) { display:flex; justify-content:center; padding:0; }
.action-btn-row { display:inline-flex; flex-wrap:wrap; justify-content:center; gap:0; }
:deep(.col-action .el-button) { padding:2px 4px; margin:0 2px; white-space:nowrap; justify-content:center; }
:deep(.col-action .el-button + .el-button) { margin-left:2px; }
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
</style>
