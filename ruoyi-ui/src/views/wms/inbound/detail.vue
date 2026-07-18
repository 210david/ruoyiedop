<template>
  <div class="app-container">
    <!-- 状态页签 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="inbound-tabs">
      <el-tab-pane name="1">
        <template #label>
          <span>待收货 <el-tag size="small" type="primary" round>{{ tabCounts['1'] }}</el-tag></span>
        </template>
      </el-tab-pane>
      <el-tab-pane name="2">
        <template #label>
          <span>待上架 <el-tag size="small" type="warning" round>{{ tabCounts['2'] }}</el-tag></span>
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
      <el-form-item label="入库单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="入库类型" prop="orderType">
        <el-select v-model="queryParams.orderType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in wms_inbound_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="供应商" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
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
    <el-table border v-loading="loading" :data="list" highlight-current-row @header-dragend="onHeaderDragEnd">
      <el-table-column label="入库单号" prop="orderNo" :width="colWidth('orderNo', 180)" resizable />
      <el-table-column label="入库类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_inbound_type" :value="scope.row.orderType" /></template>
      </el-table-column>
      <el-table-column label="供应商" prop="supplierName" show-overflow-tooltip />
      <el-table-column label="目标仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_inbound_status" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="总数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
      <el-table-column label="预计入库" prop="inboundDate" :width="colWidth('inboundDate', 120)" resizable align="center" />
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Download" @click="handleDetail(scope.row)" v-if="scope.row.status === '1'">收货</el-button>
          <el-button link type="primary" icon="Upload" @click="handleDetail(scope.row)" v-if="scope.row.status === '2'">上架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 入库作业详情面板 -->
    <el-dialog :title="'入库作业 - ' + (currentOrder.orderNo || '')" v-model="detailOpen" width="1000px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="入库单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="入库类型"><dict-tag :options="wms_inbound_type" :value="currentOrder.orderType" /></el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="wms_inbound_status" :value="currentOrder.status" /></el-descriptions-item>
        <el-descriptions-item label="供应商">{{ currentOrder.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="目标仓库">{{ currentOrder.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentOrder.totalQty }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="currentOrder.detailList" border style="margin-top: 15px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
        <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip />
        <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable />
        <el-table-column label="计划数量" prop="planQty" :width="colWidth('planQty', 100)" resizable align="right" />
        <el-table-column label="已收数量" prop="receivedQty" :width="colWidth('receivedQty', 100)" resizable align="right" />
        <el-table-column label="已上架" prop="putawayQty" :width="colWidth('putawayQty', 100)" resizable align="right" />
        <el-table-column label="上架库位" prop="locationCode" :width="colWidth('locationCode', 100)" resizable />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Download" @click="openReceive(scope.row)" v-if="currentOrder.status === '1'">收货</el-button>
            <el-button link type="primary" icon="Upload" @click="openPutaway(scope.row)" v-if="currentOrder.status === '2'">上架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 收货对话框 -->
    <el-dialog title="收货确认" v-model="receiveOpen" width="450px" append-to-body>
      <el-form ref="receiveRef" :model="receiveForm" label-width="100px">
        <el-form-item label="物料">{{ receiveForm.materialName }}</el-form-item>
        <el-form-item label="批次号">{{ receiveForm.batchNo || '-' }}</el-form-item>
        <el-form-item label="计划数量">{{ receiveForm.planQty }}</el-form-item>
        <el-form-item label="已收数量">{{ receiveForm.receivedQty }}</el-form-item>
        <el-form-item label="本次收货" prop="qty">
          <el-input-number v-model="receiveForm.qty" :precision="2" :min="0" :max="receiveForm.planQty - receiveForm.receivedQty" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitReceive">确认收货</el-button>
        <el-button @click="receiveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 上架对话框 -->
    <el-dialog title="上架确认" v-model="putawayOpen" width="500px" append-to-body>
      <el-form ref="putawayRef" :model="putawayForm" :rules="putawayRules" label-width="100px">
        <el-form-item label="物料">{{ putawayForm.materialName }}</el-form-item>
        <el-form-item label="批次号">{{ putawayForm.batchNo || '-' }}</el-form-item>
        <el-form-item label="已收数量">{{ putawayForm.receivedQty }}</el-form-item>
        <el-form-item label="已上架">{{ putawayForm.putawayQty }}</el-form-item>
        <el-form-item label="上架库位" prop="locationId">
          <el-select v-model="putawayForm.locationId" filterable placeholder="请选择库位" style="width: 100%">
            <el-option v-for="loc in locationOptions" :key="loc.warehouseId" :label="loc.warehouseCode + ' - ' + (loc.warehouseName || '')" :value="loc.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="上架数量" prop="qty">
          <el-input-number v-model="putawayForm.qty" :precision="2" :min="0" :max="putawayForm.receivedQty - putawayForm.putawayQty" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPutaway">确认上架</el-button>
        <el-button @click="putawayOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsInboundDetail">
import { listInbound, getInbound, receiveInbound, putawayInbound } from '@/api/wms/inbound'
import { listLocation } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('wms_inbound_detail')
const { wms_inbound_type, wms_inbound_status } = proxy.useDict('wms_inbound_type', 'wms_inbound_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeTab = ref('1')
const tabCounts = ref({ '1': 0, '2': 0, '3': 0 })
const detailOpen = ref(false)
const currentOrder = ref({})
const receiveOpen = ref(false)
const putawayOpen = ref(false)
const receiveForm = ref({})
const putawayForm = ref({})
const locationOptions = ref([])
const putawayRules = {
  locationId: [{ required: true, message: '请选择库位', trigger: 'change' }],
  qty: [{ required: true, message: '请输入上架数量', trigger: 'blur' }]
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderType: undefined, supplierName: undefined, status: '1' }
})
const { queryParams } = toRefs(data)

/** 查询当前标签页的入库单列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value, status: activeTab.value }
  listInbound(params).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 加载各标签页的记录数 */
function loadTabCounts() {
  const statuses = ['1', '2', '3']
  statuses.forEach(s => {
    listInbound({ status: s, pageNum: 1, pageSize: 1 }).then(res => {
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
  queryParams.value.status = activeTab.value
  handleQuery()
}

/** 打开入库单详情（列表操作按钮 + 行点击都走这里） */
function handleDetail(row) {
  getInbound(row.orderId).then(res => {
    currentOrder.value = res.data
    detailOpen.value = true
  })
}
function openReceive(row) {
  receiveForm.value = {
    orderId: currentOrder.value.orderId,
    detailId: row.detailId,
    materialName: row.materialName,
    batchNo: row.batchNo,
    planQty: row.planQty,
    receivedQty: row.receivedQty,
    qty: row.planQty - row.receivedQty
  }
  receiveOpen.value = true
}
function submitReceive() {
  receiveInbound(receiveForm.value.orderId, receiveForm.value.detailId, receiveForm.value.qty).then(() => {
    proxy.$modal.msgSuccess('收货成功')
    receiveOpen.value = false
    getInbound(currentOrder.value.orderId).then(res => { currentOrder.value = res.data })
    getList()
    loadTabCounts()
  })
}
function openPutaway(row) {
  putawayForm.value = {
    orderId: currentOrder.value.orderId,
    detailId: row.detailId,
    materialName: row.materialName,
    batchNo: row.batchNo,
    receivedQty: row.receivedQty,
    putawayQty: row.putawayQty,
    locationId: row.locationId,
    qty: row.receivedQty - row.putawayQty
  }
  // 加载该仓库的库位列表
  listLocation({ warehouseId: currentOrder.value.warehouseId, status: '0', pageSize: 999 }).then(res => {
    locationOptions.value = res.rows
  })
  putawayOpen.value = true
}
function submitPutaway() {
  proxy.$refs['putawayRef'].validate(valid => {
    if (valid) {
      putawayInbound(putawayForm.value.orderId, putawayForm.value.detailId, putawayForm.value.locationId, putawayForm.value.qty).then(() => {
        proxy.$modal.msgSuccess('上架成功')
        putawayOpen.value = false
        getInbound(currentOrder.value.orderId).then(res => { currentOrder.value = res.data })
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
.inbound-tabs {
  margin-bottom: 12px;
}
.el-tabs__item .el-tag {
  margin-left: 4px;
}
</style>
