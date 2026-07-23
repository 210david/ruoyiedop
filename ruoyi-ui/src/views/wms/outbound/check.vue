<template>
  <div class="app-container">
    <!-- 状态页签 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="check-tabs">
      <el-tab-pane name="2">
        <template #label>
          <span>复核中 <el-tag size="small" type="warning" round>{{ tabCounts['2'] }}</el-tag></span>
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
        <el-input v-model="queryParams.orderNo" placeholder="请输入或扫描" clearable style="width: 240px" @keyup.enter="handleQuery" />
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
      <el-table-column label="预计出库" prop="outboundDate" :width="colWidth('outboundDate', 120)" resizable align="center" />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Check" @click="handleDetail(scope.row)" v-if="scope.row.status === '2'">复核</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 扫码复核详情面板 -->
    <el-dialog :title="'扫码复核 - ' + (currentOrder.orderNo || '')" v-model="detailOpen" width="1000px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="出库单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="出库类型"><dict-tag :options="wms_outbound_type" :value="currentOrder.orderType" /></el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="wms_outbound_status" :value="currentOrder.status" /></el-descriptions-item>
        <el-descriptions-item label="出库仓库">{{ currentOrder.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentOrder.totalQty }}</el-descriptions-item>
      </el-descriptions>

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
    <el-dialog title="复核确认" v-model="checkOpen" width="450px" append-to-body>
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
  </div>
</template>

<script setup name="WmsOutboundCheck">
import { listOutbound, getOutbound, checkOutbound } from '@/api/wms/outbound'
import { useColumnResize } from '@/composables/useColumnResize'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_outbound_check')
const { wms_outbound_type, wms_outbound_status } = proxy.useDict('wms_outbound_type', 'wms_outbound_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeTab = ref('2')
const tabCounts = ref({ '2': 0, '3': 0 })
const detailOpen = ref(false)
const currentOrder = ref({})
const checkOpen = ref(false)
const checkForm = ref({})
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
  const statuses = ['2', '3']
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
.check-tabs {
  margin-bottom: 12px;
}
.el-tabs__item .el-tag {
  margin-left: 4px;
}
</style>
