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
      <el-form-item label="出库单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="出库类型" prop="orderType">
        <el-select v-model="queryParams.orderType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in wms_outbound_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
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
          <el-button link type="primary" icon="HandTaking" @click="handleDetail(scope.row)" v-if="scope.row.status === '1'">拣货</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 出库作业详情面板 -->
    <el-dialog :title="'出库作业 - ' + (currentOrder.orderNo || '')" v-model="detailOpen" width="1000px" append-to-body>
      <el-divider content-position="center">单据信息</el-divider>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="出库单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="出库类型"><dict-tag :options="wms_outbound_type" :value="currentOrder.orderType" /></el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="wms_outbound_status" :value="currentOrder.status" /></el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentOrder.totalQty }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="center">出库信息</el-divider>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="出库仓库">{{ currentOrder.warehouseName }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="center">出库明细</el-divider>

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
            <el-button link type="primary" icon="HandTaking" @click="openPick(scope.row)">拣货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 15px; text-align: right" v-if="currentOrder.status === '1'">
        <el-tag type="warning">所有明细拣货完成后，出库单将自动完成</el-tag>
      </div>
    </el-dialog>

    <!-- 拣货对话框 -->
    <el-dialog title="拣货确认" v-model="pickOpen" width="450px" append-to-body>
      <el-form ref="pickRef" :model="pickForm" :rules="pickRules" label-width="100px">
        <el-divider content-position="center">物料信息</el-divider>
        <el-form-item label="物料">{{ pickForm.materialName }}</el-form-item>
        <el-form-item label="批次号">{{ pickForm.batchNo || '-' }}</el-form-item>
        <el-form-item label="拣货库位">{{ pickForm.locationName || pickForm.locationCode || '-' }}</el-form-item>
        <el-divider content-position="center">拣货信息</el-divider>
        <el-form-item label="计划数量">{{ pickForm.planQty }}</el-form-item>
        <el-form-item label="已拣货数量">{{ pickForm.pickQty }}</el-form-item>
        <el-form-item label="本次拣货" prop="qty">
          <el-input-number v-model="pickForm.qty" :precision="2" :min="0" :max="pickForm.planQty - pickForm.pickQty" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPick">确认拣货</el-button>
        <el-button @click="pickOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsOutboundDetail">
import { listOutbound, getOutbound, pickOutbound } from '@/api/wms/outbound'
import { useColumnResize } from '@/composables/useColumnResize'
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
    materialName: row.materialName,
    batchNo: row.batchNo,
    locationCode: row.locationCode,
    locationName: row.locationName,
    planQty: row.planQty,
    pickQty: row.pickQty,
    qty: row.planQty - row.pickQty
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
</script>

<style scoped>
.outbound-tabs {
  margin-bottom: 12px;
}
.el-tabs__item .el-tag {
  margin-left: 4px;
}
</style>
