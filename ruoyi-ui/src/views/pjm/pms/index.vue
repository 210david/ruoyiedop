<template>
  <div class="app-container pjm-list-page pjm-pms-page">
    <!-- ===== 筛选卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field" style="grid-column: span 2">
          <label>选择项目</label>
          <div class="control">
            <el-input :model-value="selectedProjectLabel" placeholder="点击选择项目" readonly style="cursor:pointer" @click="openProjectPicker">
              <template #append><el-button icon="Search" @click.stop="openProjectPicker" /></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>单据类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.docType" placeholder="全部" clearable @change="getList">
              <el-option v-for="d in docTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>单据编号</label>
          <div class="control">
            <el-input v-model="queryParams.docNo" placeholder="请输入" clearable @keyup.enter="getList">
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
          <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== 采购汇总指标卡 ===== -->
    <div class="stat-grid" v-if="summary">
      <div class="stat-card is-brand">
        <div class="stat-label"><el-icon><Wallet /></el-icon>项目预算</div>
        <div class="stat-value">¥{{ money(summary.budgetAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">计划采购</div>
        <div class="stat-value">¥{{ money(summary.plannedPurchase) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">实际采购</div>
        <div class="stat-value">¥{{ money(summary.actualPurchase) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已开票</div>
        <div class="stat-value">¥{{ money(summary.invoiceAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已付款</div>
        <div class="stat-value">¥{{ money(summary.paidAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">采购申请数</div>
        <div class="stat-value">{{ summary.purchaseCount || 0 }}</div>
      </div>
    </div>

    <!-- ===== 台账表格 ===== -->
    <div class="surface" v-if="queryParams.projectId">
      <div class="toolbar">
        <div class="left">
          <button type="button" class="btn-soft is-outline" @click="refreshAll">
            <el-icon><Refresh /></el-icon> 刷新
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pjm_pms_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="ledgerList" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="单据类型" prop="docType" key="docType" :width="colWidth('docType', 110)" resizable align="center" v-if="columns.docType.visible">
            <template #default="scope">
              <span class="badge" :class="docTypeBadge(scope.row.docType)">{{ docTypeLabel(scope.row.docType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单据编号" prop="docNo" key="docNo" :width="colWidth('docNo', 160)" resizable v-if="columns.docNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.docNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="标题" prop="title" key="title" :min-width="colWidth('title', 220)" resizable show-overflow-tooltip v-if="columns.title.visible" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :min-width="colWidth('supplierName', 150)" resizable show-overflow-tooltip v-if="columns.supplierName.visible">
            <template #default="scope">{{ scope.row.supplierName || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row)">
                <span class="dot"></span>{{ statusLabel(scope.row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" key="amount" :width="colWidth('amount', 130)" resizable align="right" v-if="columns.amount.visible">
            <template #default="scope"><span class="col-amt">¥{{ money(scope.row.amount) }}</span></template>
          </el-table-column>
          <el-table-column label="已付款" prop="paidAmount" key="paidAmount" :width="colWidth('paidAmount', 120)" resizable align="right" v-if="columns.paidAmount.visible">
            <template #default="scope">
              <span v-if="scope.row.paidAmount != null" class="col-amt">¥{{ money(scope.row.paidAmount) }}</span>
              <span v-else class="muted-dash">—</span>
            </template>
          </el-table-column>
          <el-table-column label="关联单号" prop="relNo" key="relNo" :width="colWidth('relNo', 150)" resizable v-if="columns.relNo.visible">
            <template #default="scope">
              <span v-if="scope.row.relNo" class="col-mono">{{ scope.row.relNo }}</span>
              <span v-else class="muted-dash">—</span>
            </template>
          </el-table-column>
          <el-table-column label="单据日期" prop="docDate" key="docDate" :width="colWidth('docDate', 110)" resizable align="center" v-if="columns.docDate.visible">
            <template #default="scope">{{ scope.row.docDate || '-' }}</template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope">{{ scope.row.createTime || '-' }}</template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <div class="surface" v-else>
      <div class="empty-body">
        <el-empty description="请先选择一个项目查看采购台账" />
      </div>
    </div>
    <ProjectPicker ref="projectPickerRef" @confirm="onProjectPickerConfirm" />
  </div>
</template>

<script setup name="PjmPmsLedger">
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { listProject, getPurchaseSummary } from '@/api/pjm/project'
import { listPmsLedger } from '@/api/pjm/pms'
import { useColumnResize } from '@/composables/useColumnResize'
import ProjectPicker from '@/components/ProjectPicker/index.vue'

const { proxy } = getCurrentInstance()
const { pms_request_status, pms_plan_status, pms_order_status, pms_contract_status } =
  proxy.useDict('pms_request_status', 'pms_plan_status', 'pms_order_status', 'pms_contract_status')

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pjm_pms_ledger')

const loading = ref(false)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const ledgerList = ref([])
const projectOptions = ref([])
const summary = ref(null)

const columns = ref({
  docType: { label: '单据类型', visible: true },
  docNo: { label: '单据编号', visible: true },
  title: { label: '标题', visible: true },
  supplierName: { label: '供应商', visible: true },
  status: { label: '状态', visible: true },
  amount: { label: '金额', visible: true },
  paidAmount: { label: '已付款', visible: true },
  relNo: { label: '关联单号', visible: true },
  docDate: { label: '单据日期', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  projectId: null,
  docType: null,
  docNo: null,
  status: null
})
const projectPickerRef = ref()
const selectedProjectLabel = computed(() => {
  const p = projectOptions.value.find(x => String(x.projectId) === String(queryParams.projectId))
  return p ? p.projectNo + ' ' + p.projectName : ''
})
function openProjectPicker() { projectPickerRef.value?.open(queryParams.projectId) }
function onProjectPickerConfirm(p) {
  if (!p || !p.projectId) return
  if (!projectOptions.value.some(x => String(x.projectId) === String(p.projectId))) projectOptions.value.push(p)
  queryParams.projectId = p.projectId
  onProjectChange()
}

const docTypeOptions = [
  { value: 'request', label: '采购申请' },
  { value: 'plan', label: '采购计划' },
  { value: 'order', label: '采购订单' },
  { value: 'contract', label: '采购合同' }
]

const activeFilterCount = computed(() =>
  ['projectId', 'docType', 'docNo', 'status'].filter(k => queryParams[k] != null && queryParams[k] !== '').length
)

const docTypeLabel = (t) => ({ request: '采购申请', plan: '采购计划', order: '采购订单', contract: '采购合同' }[t] || t)
const docTypeBadge = (t) => ({ request: 'amber', plan: 'blue', order: 'violet', contract: 'green' }[t] || 'gray')

const dictMap = computed(() => ({
  request: pms_request_status.value,
  plan: pms_plan_status.value,
  order: pms_order_status.value,
  contract: pms_contract_status.value
}))

function statusLabel(row) {
  const list = dictMap.value[row.docType] || []
  const d = list.find(i => i.value == row.status)
  return d ? d.label : (row.status ?? '-')
}

/** 通用状态色映射（草稿/审批中/已通过/执行中/完成/驳回/作废） */
const STATUS_BADGE = { '0': 'gray', '1': 'blue', '2': 'green', '3': 'violet', '4': 'green', '5': 'amber', '6': 'gray', '7': 'red', '9': 'gray' }
function statusBadge(row) { return STATUS_BADGE[row.status] || 'blue' }

function money(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function loadProjects() {
  listProject({ pageNum: 1, pageSize: 999 }).then(res => { projectOptions.value = res.rows || [] })
}

function loadSummary() {
  if (!queryParams.projectId) { summary.value = null; return }
  getPurchaseSummary(queryParams.projectId).then(res => { summary.value = res.data })
}

function getList() {
  if (!queryParams.projectId) { ledgerList.value = []; total.value = 0; return }
  loading.value = true
  listPmsLedger(queryParams).then(res => {
    ledgerList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function onProjectChange() {
  queryParams.pageNum = 1
  loadSummary()
  getList()
}

function refreshAll() { loadSummary(); getList() }

function resetQuery() {
  showAdvanced.value = false
  queryParams.projectId = null
  queryParams.docType = null
  queryParams.docNo = null
  queryParams.status = null
  queryParams.pageNum = 1
  summary.value = null
  getList()
}

onMounted(() => {
  loadProjects()
  getList()
})
</script>

<style scoped>
.empty-body { display: flex; align-items: center; justify-content: center; padding: 60px 20px; }
</style>
