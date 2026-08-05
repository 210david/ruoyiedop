<template>
  <div class="app-container wms-list-page">
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
          <label>出库单号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入或扫描" clearable @keyup.enter="handleQuery">
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left"></div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_outbound_check_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" highlight-current-row @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column label="出库单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 200)" resizable v-if="columns.orderNo.visible" />
          <el-table-column label="出库类型" prop="orderType" key="orderType" :width="colWidth('orderType', 100)" resizable align="center" v-if="columns.orderType.visible">
            <template #default="scope"><span class="badge violet">{{ orderTypeLabel(scope.row.orderType) }}</span></template>
          </el-table-column>
          <el-table-column label="出库仓库" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 150)" resizable v-if="columns.warehouseName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="总数量" prop="totalQty" key="totalQty" :width="colWidth('totalQty', 110)" resizable align="right" class-name="col-num" v-if="columns.totalQty.visible" />
          <el-table-column label="预计出库" prop="outboundDate" key="outboundDate" :width="colWidth('outboundDate', 130)" resizable align="center" v-if="columns.outboundDate.visible" />
          <el-table-column label="备注" prop="remark" key="remark" :width="colWidth('remark', 200)" resizable :show-overflow-tooltip="true" v-if="columns.remark.visible" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              <el-button link type="primary" icon="Check" @click="handleDetail(scope.row)" v-if="scope.row.status === '2'">复核</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 扫码复核详情面板 -->
    <el-dialog v-model="detailOpen" width="1000px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ '扫码复核 - ' + (currentOrder.orderNo || '') }}</span>
        </div>
      </template>
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">出库单号</span><div class="rd-value">{{ currentOrder.orderNo }}</div></div>
        <div class="rd-item"><span class="rd-label">出库类型</span><div class="rd-value"><dict-tag :options="wms_outbound_type" :value="currentOrder.orderType" /></div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_outbound_status" :value="currentOrder.status" /></div></div>
        <div class="rd-item"><span class="rd-label">出库仓库</span><div class="rd-value">{{ currentOrder.warehouseName }}</div></div>
        <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ currentOrder.totalQty }}</div></div>
      </div>

      <el-table :data="currentOrder.detailList" border style="margin-top: 15px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
        <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip />
        <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable />
        <el-table-column label="计划数量" prop="planQty" :width="colWidth('planQty', 100)" resizable align="right" />
        <el-table-column label="已拣货" prop="pickQty" :width="colWidth('pickQty', 100)" resizable align="right" />
        <el-table-column label="复核数量" prop="actualQty" :width="colWidth('actualQty', 100)" resizable align="right">
          <template #default="scope">
            <span v-if="scope.row.actualQty != null">{{ scope.row.actualQty }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Check" @click="openCheck(scope.row)" v-if="currentOrder.status === '2'">复核</el-button>
            <el-tag v-else-if="scope.row.actualQty != null" type="success" size="small">已复核</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 15px; text-align: right" v-if="currentOrder.status === '2'">
        <el-tag type="warning">所有明细复核完成后，出库单将自动完成</el-tag>
      </div>
    </el-dialog>

    <!-- 复核对话框 -->
    <el-dialog v-model="checkOpen" width="450px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">复核确认</span>
        </div>
      </template>
      <el-form ref="checkRef" :model="checkForm" :rules="checkRules" label-width="100px">
        <el-form-item label="物料编码">{{ checkForm.materialCode }}</el-form-item>
        <el-form-item label="物料名称">{{ checkForm.materialName }}</el-form-item>
        <el-form-item label="批次号">{{ checkForm.batchNo || '-' }}</el-form-item>
        <el-form-item label="计划数量">{{ checkForm.planQty }}</el-form-item>
        <el-form-item label="已拣货">{{ checkForm.pickQty }}</el-form-item>
        <el-form-item label="复核数量" prop="qty">
          <el-input-number v-model="checkForm.qty" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitCheck">确认复核</el-button>
        <el-button @click="checkOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="出库复核业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、作业流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">复核中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>

        <h4>二、页签说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="复核中">出库单已拣货完成，等待仓库人员逐条明细复核确认实际出库数量</el-descriptions-item>
          <el-descriptions-item label="已完成">所有明细复核完成后出库单自动完成，系统库存自动扣减</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">复核数量确认</div>
              <div class="highlight-card-body">复核数量默认等于<strong>已拣货数量</strong>，仓库人员可根据实际情况调整复核数量</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">自动完成</div>
              <div class="highlight-card-body">所有明细复核完成后出库单自动进入「已完成」状态，无需手动操作</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">扫码复核</div>
              <div class="highlight-card-body">支持通过出库单号扫码快速定位单据，提高复核效率</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">库存扣减时机</div>
              <div class="highlight-card-body">复核完成后系统<strong>正式扣减库存</strong>，拣货阶段不扣减库存，复核是出库流程的最后一环</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>查看出库单：</strong>在「复核中」页签中选择出库单，查看明细列表中的物料信息和已拣货数量
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>逐条复核：</strong>点击「复核」按钮，确认复核数量后提交。复核数量默认为已拣货数量，可调整
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>自动完成：</strong>所有明细复核完成后，出库单自动进入「已完成」状态，系统库存自动扣减
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsOutboundCheck">
import { listOutbound, getOutbound, checkOutbound } from '@/api/wms/outbound'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, QuestionFilled, WarningFilled, Filter, Search } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard([])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_outbound_check')
const { wms_outbound_type, wms_outbound_status } = proxy.useDict('wms_outbound_type', 'wms_outbound_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const activeStatusTab = ref('2')
const statusCounts = ref({ all: 0, '2': 0, '3': 0 })
const statusTabList = ref([{ value: '2', label: '复核中' }, { value: '3', label: '已完成' }])
const detailOpen = ref(false)
const currentOrder = ref({})
const checkOpen = ref(false)
const checkForm = ref({})
const showStatusHelp = ref(false)
const defaultColumns = { orderNo: { label: '出库单号', visible: true }, orderType: { label: '出库类型', visible: true }, warehouseName: { label: '出库仓库', visible: true }, status: { label: '状态', visible: true }, totalQty: { label: '总数量', visible: true }, outboundDate: { label: '预计出库', visible: true }, remark: { label: '备注', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('wms_outbound_check_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let count = 0; if (queryParams.value.orderNo) count++; return count })
const checkRules = {
  qty: [{ required: true, message: '请输入复核数量', trigger: 'blur' }]
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined }
})
const { queryParams } = toRefs(data)

/** 查询当前标签页的出库单列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value, status: activeStatusTab.value === 'all' ? undefined : activeStatusTab.value }
  listOutbound(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => {
    loading.value = false
  })
}

function loadStatusCounts() {
  listOutbound({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '2': 0, '3': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.pageNum = 1; getList() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'violet', '3': 'green' }; return map[status] || 'gray' }
function statusLabel(status) { const item = wms_outbound_status.value.find(d => d.value == status); return item ? item.label : '-' }
function orderTypeLabel(type) { const item = wms_outbound_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '2': 'tab-partial', '3': 'tab-done' }; return map[value] || '' }

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.orderNo = undefined; handleQuery() }

/** 打开出库单详情（列表操作按钮 + 行点击都走这里） */
function handleDetail(row) {
  getOutbound(row.orderId).then(res => {
    currentOrder.value = res.data
    detailOpen.value = true
  })
}
function openCheck(row) {
  checkForm.value = {
    orderId: currentOrder.value.orderId,
    detailId: row.detailId,
    materialCode: row.materialCode,
    materialName: row.materialName,
    batchNo: row.batchNo,
    planQty: row.planQty,
    pickQty: row.pickQty,
    qty: row.pickQty
  }
  checkOpen.value = true
}
function submitCheck() {
  proxy.$refs['checkRef'].validate(valid => {
    if (valid) {
      checkOutbound(checkForm.value.orderId, checkForm.value.detailId, checkForm.value.qty).then(() => {
        proxy.$modal.msgSuccess('复核成功')
        checkOpen.value = false
        getOutbound(currentOrder.value.orderId).then(res => { currentOrder.value = res.data })
        getList()
        loadStatusCounts()
      })
    }
  })
}

// 初始化
getList()
loadStatusCounts()

onActivated(() => {
  getList()
  loadStatusCounts()
})
</script>

<style scoped>
/* 页面特定样式 - 列表页面共享样式见 wms-list-page.scss */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.flow-item { display: flex; align-items: center; gap: 8px; }
.flow-arrow { color: #909399; font-size: 16px; }
.highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.highlight-success { background-color: #f0f9ff; border-color: #b3e19d; }
.highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
</style>
