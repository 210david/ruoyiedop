<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="takeNo">
        <el-input v-model="queryParams.takeNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="盘点类型" prop="takeType">
        <el-select v-model="queryParams.takeType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in wms_take_status" :key="d.value" :label="d.label" :value="d.value" />
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

    <el-table ref="tableRef" border v-loading="loading" :data="list" @row-click="handleRowClick" highlight-current-row @header-dragend="onHeaderDragEnd">
      <el-table-column label="盘点单号" prop="takeNo" :width="colWidth('takeNo', 180)" resizable />
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="库区" prop="areaName" :width="colWidth('areaName', 120)" resizable />
      <el-table-column label="盘点类型" prop="takeType" :width="colWidth('takeType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_take_type" :value="scope.row.takeType" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_take_status" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="计划日期" prop="planDate" :width="colWidth('planDate', 120)" resizable align="center" />
      <el-table-column label="开始时间" prop="startTime" :width="colWidth('startTime', 160)" resizable align="center" />
      <el-table-column label="结束时间" prop="endTime" :width="colWidth('endTime', 160)" resizable align="center" />
      <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click.stop="handleRowClick(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click.stop="handleRowClick(scope.row)" v-if="scope.row.status === '1'">录入</el-button>
          <el-button link type="primary" icon="Check" @click.stop="handleApprove(scope.row)" v-if="scope.row.status === '2'">审批</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 盘点作业详情面板 -->
    <el-dialog :title="'盘点作业 - ' + (currentTake.takeNo || '')" v-model="detailOpen" width="1100px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="盘点单号">{{ currentTake.takeNo }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ currentTake.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="库区">{{ currentTake.areaName || '全部' }}</el-descriptions-item>
        <el-descriptions-item label="盘点类型"><dict-tag :options="wms_take_type" :value="currentTake.takeType" /></el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="wms_take_status" :value="currentTake.status" /></el-descriptions-item>
        <el-descriptions-item label="计划日期">{{ currentTake.planDate }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="currentTake.detailList" border style="margin-top: 15px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
        <el-table-column label="物料名称" prop="materialName" min-width="200" show-overflow-tooltip />
        <el-table-column label="库区/库位" width="180">
          <template #default="scope">{{ [scope.row.areaName, scope.row.locationName].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 100)" resizable />
        <el-table-column label="账面数量" prop="bookQty" :width="colWidth('bookQty', 100)" resizable align="right" />
        <el-table-column label="实盘数量" prop="actualQty" :width="colWidth('actualQty', 100)" resizable align="right">
          <template #default="scope">
            <span v-if="scope.row.actualQty != null" :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.actualQty }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="差异" prop="diffQty" :width="colWidth('diffQty', 80)" resizable align="right">
          <template #default="scope">
            <span v-if="scope.row.diffQty != null" :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" prop="diffReason" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center" v-if="currentTake.status === '1'">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="openSubmit(scope.row)">录入</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 录入实盘数量 -->
    <el-dialog title="录入实盘数量" v-model="submitOpen" width="450px" append-to-body>
      <el-form ref="submitRef" :model="submitFormData" :rules="submitRules" label-width="100px">
        <el-form-item label="物料编码">{{ submitFormData.materialCode }}</el-form-item>
        <el-form-item label="物料名称">{{ submitFormData.materialName }}</el-form-item>
        <el-form-item label="库区/库位">{{ [submitFormData.areaName, submitFormData.locationName].filter(Boolean).join(' / ') || '-' }}</el-form-item>
        <el-form-item label="批次号">{{ submitFormData.batchNo || '-' }}</el-form-item>
        <el-form-item label="账面数量">{{ submitFormData.bookQty }}</el-form-item>
        <el-form-item label="实盘数量" prop="actualQty">
          <el-input-number v-model="submitFormData.actualQty" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="差异原因"><el-input v-model="submitFormData.diffReason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitDetailForm">确 定</el-button>
        <el-button @click="submitOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsStockTakeDetail">
import { listStockTake, getStockTake, submitStockTakeDetail, approveStockTake } from '@/api/wms/stocktake'
import { useColumnResize } from '@/composables/useColumnResize'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_stocktake_detail')
const { wms_take_type, wms_take_status } = proxy.useDict('wms_take_type', 'wms_take_status')
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const currentTake = ref({})
const submitOpen = ref(false)
const submitFormData = ref({})
const submitRules = {
  actualQty: [{ required: true, message: '请输入实盘数量', trigger: 'blur' }]
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, takeNo: undefined, takeType: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listStockTake(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleRowClick(row) {
  getStockTake(row.takeId).then(res => {
    currentTake.value = res.data
    detailOpen.value = true
  })
}
function openSubmit(row) {
  submitFormData.value = {
    takeId: currentTake.value.takeId,
    detailId: row.detailId,
    materialCode: row.materialCode,
    materialName: row.materialName,
    locationCode: row.locationCode,
    locationName: row.locationName,
    areaName: row.areaName,
    batchNo: row.batchNo,
    bookQty: row.bookQty,
    actualQty: row.actualQty != null ? row.actualQty : row.bookQty,
    diffReason: row.diffReason
  }
  submitOpen.value = true
}
function submitDetailForm() {
  proxy.$refs['submitRef'].validate(valid => {
    if (valid) {
      submitStockTakeDetail(submitFormData.value.takeId, submitFormData.value.detailId, submitFormData.value.actualQty, submitFormData.value.diffReason).then(() => {
        proxy.$modal.msgSuccess('录入成功')
        submitOpen.value = false
        getStockTake(currentTake.value.takeId).then(res => {
          currentTake.value = res.data
          // 所有明细录入完成后，自动提交审批
          if (res.data.status === '2') {
            proxy.$modal.confirm('所有明细已录入完成，是否自动提交审批并调整库存？').then(() => {
              return approveStockTake(currentTake.value.takeId)
            }).then(() => {
              proxy.$modal.msgSuccess('盘点审批完成，库存已自动调整')
              detailOpen.value = false
              getList()
            }).catch(() => {})
          }
        })
      })
    }
  })
}
// 初始化
getList()

function handleApprove(row) {
  proxy.$modal.confirm('确认审批并调整库存？').then(() => {
    return approveStockTake(row.takeId)
  }).then(() => {
    proxy.$modal.msgSuccess('盘点审批完成，库存已自动调整')
    getList()
  }).catch(() => {})
}
</script>
