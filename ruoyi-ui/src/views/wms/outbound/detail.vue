<template>
  <div class="app-container">
    <!-- 状态页签 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="outbound-tabs">
      <el-tab-pane name="1">
        <template #label>
          <span>待拣货 <el-tag size="small" type="primary" round>{{ tabCounts['1'] }}</el-tag></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="3">
        <template #label>
          <span>已完成 <el-tag size="small" type="success" round>{{ tabCounts['3'] }}</el-tag></span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="出库单号" prop="orderNo"><el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="出库类型" prop="orderType"><el-select v-model="queryParams.orderType" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_outbound_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table ref="tableRef" border v-loading="loading" :data="list" highlight-current-row @header-dragend="onHeaderDragEnd">
      <el-table-column label="出库单号" prop="orderNo" :width="colWidth('orderNo', 200)" resizable />
      <el-table-column label="出库类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_outbound_type" :value="scope.row.orderType" /></template>
      </el-table-column>
      <el-table-column label="出库仓库" prop="warehouseName" :width="colWidth('warehouseName', 150)" resizable />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_outbound_status" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="总数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
      <el-table-column label="出库日期" prop="outboundDate" :width="colWidth('outboundDate', 120)" resizable align="center" />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button plain type="warning" icon="HandTaking" size="small" @click="handleDetail(scope.row)" v-if="scope.row.status === '1'">拣货</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 出库作业详情面板 -->
    <el-dialog v-model="detailOpen" width="1200px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg></div>
          <span class="rd-detail-header-title">出库作业</span>
          <div class="rd-detail-header-sub" v-if="currentOrder.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ currentOrder.orderNo }}</span>
          </div>
        </div>
      </template>
      <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c4">
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">出库单号</span><div class="rd-value">{{ currentOrder.orderNo }}</div></div>
        <div class="rd-item"><span class="rd-label">出库类型</span><div class="rd-value"><dict-tag :options="wms_outbound_type" :value="currentOrder.orderType" /></div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_outbound_status" :value="currentOrder.status" /></div></div>
        <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ currentOrder.totalQty }}</div></div>
      </div>
                </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>出库信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">出库仓库</span><div class="rd-value">{{ currentOrder.warehouseName }}</div></div>
      </div>
                </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>出库明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">

      <el-table :data="currentOrder.detailList" border style="margin-top: 15px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
        <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip />
        <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
        <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable />
        <el-table-column label="计划数量" prop="planQty" :width="colWidth('planQty', 100)" resizable align="right" />
        <el-table-column label="已拣货数量" prop="pickQty" :width="colWidth('pickQty', 100)" resizable align="right" />
        <el-table-column label="拣货库位" prop="locationName" :width="colWidth('locationName', 100)" resizable show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" v-if="currentOrder.status === '1'">
          <template #default="scope">
            <el-button plain type="warning" icon="HandTaking" size="small" @click="openPick(scope.row)" v-if="scope.row.planQty > scope.row.pickQty">拣货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 15px; text-align: right" v-if="currentOrder.status === '1'">
        <el-tag type="warning">所有明细拣货完成后，出库单将自动完成</el-tag>
      </div>
      </div>
    </section>
    </el-dialog>

    <!-- 拣货对话框 -->
    <el-dialog v-model="pickOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">拣货确认</span>
          <div class="rd-detail-header-sub" v-if="currentOrder.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ currentOrder.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="pickRef" :model="pickForm" :rules="pickRules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ pickForm.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="pickForm.unit" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">物料名称</span><div class="rd-value">{{ pickForm.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ pickForm.batchNo || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">拣货库位</span><div class="rd-value">{{ pickForm.locationName || pickForm.locationCode || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>拣货信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
            <div class="rd-grid" style="margin-bottom: 16px">
              <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ pickForm.planQty }}</div></div>
              <div class="rd-item"><span class="rd-label">已拣货数量</span><div class="rd-value">{{ pickForm.pickQty }}</div></div>
              <div class="rd-item"><span class="rd-label">待拣数量</span><div class="rd-value" style="color: #f56c6c; font-weight: 700">{{ pickForm.maxQty != null ? pickForm.maxQty.toFixed(2) : '0.00' }}</div></div>
            </div>
            <el-form-item label="本次拣货" prop="qty">
              <el-input-number v-model="pickForm.qty" :precision="2" :min="0" :max="pickForm.maxQty" style="width: 100%" />
              <div style="margin-top: 6px; color: #909399; font-size: 12px">最大可拣数量：{{ pickForm.maxQty != null ? pickForm.maxQty.toFixed(2) : '0.00' }}</div>
            </el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer>
        <el-button type="warning" @click="submitPick">确认拣货</el-button>
        <el-button @click="pickOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsOutboundDetail">
import { listOutbound, getOutbound, pickOutbound } from '@/api/wms/outbound'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c4","c3","c2","c1","c0"])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_outbound_detail')
const { wms_outbound_type, wms_outbound_status, wms_unit } = proxy.useDict('wms_outbound_type', 'wms_outbound_status', 'wms_unit')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeTab = ref('1')
const tabCounts = ref({ '1': 0, '3': 0 })
const detailOpen = ref(false)
const currentOrder = ref({})
const pickOpen = ref(false)
const pickForm = ref({})
const pickRules = {
  qty: [{ required: true, message: '请输入拣货数量', trigger: 'blur' }]
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderType: undefined }
})
const { queryParams } = toRefs(data)

/** 查询当前标签页的出库单列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value, status: activeTab.value }
  listOutbound(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 加载各标签页的记录数 */
function loadTabCounts() {
  const statuses = ['1', '3']
  statuses.forEach(s => {
    listOutbound({ status: s, pageNum: 1, pageSize: 1 }).then(res => {
      tabCounts.value[s] = res.total || 0
    })
  })
}

/** 切换标签页 */
function handleTabChange() {
  queryParams.value.pageNum = 1
  getList()
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 打开出库单详情（列表操作按钮 + 行点击都走这里） */
function handleDetail(row) {
  getOutbound(row.orderId).then(res => {
    currentOrder.value = res.data
    detailOpen.value = true
  })
}
function openPick(row) {
  pickForm.value = {
    orderId: currentOrder.value.orderId,
    detailId: row.detailId,
    materialCode: row.materialCode,
    materialName: row.materialName,
    unit: row.unit,
    batchNo: row.batchNo,
    locationCode: row.locationCode,
    locationName: row.locationName,
    planQty: row.planQty,
    pickQty: row.pickQty,
    qty: row.planQty - row.pickQty,
    maxQty: row.planQty - row.pickQty
  }
  pickOpen.value = true
}
function submitPick() {
  proxy.$refs['pickRef'].validate(valid => {
    if (valid) {
      pickOutbound(pickForm.value.orderId, pickForm.value.detailId, pickForm.value.qty).then(() => {
        proxy.$modal.msgSuccess('拣货成功')
        pickOpen.value = false
        getOutbound(currentOrder.value.orderId).then(res => { currentOrder.value = res.data })
        getList()
        loadTabCounts()
      })
    }
  })
}

// 初始化
getList()
loadTabCounts()

onActivated(() => {
  getList()
  loadTabCounts()
})
</script>

<style scoped>
.outbound-tabs {
  margin-bottom: 12px;
}
.el-tabs__item .el-tag {
  margin-left: 4px;
}
/* 表格内操作按钮优化 */
.el-table .el-button--small {
  border-radius: 6px;
  padding: 5px 12px;
  font-size: 12px;
}
</style>
