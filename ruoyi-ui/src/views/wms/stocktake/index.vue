<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="takeNo"><el-input v-model="queryParams.takeNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="盘点类型" prop="takeType"><el-select v-model="queryParams.takeType" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_take_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:stocktake:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:stocktake:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="盘点单号" prop="takeNo" :width="colWidth('takeNo', 160)" resizable />
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="库区" prop="areaName" :width="colWidth('areaName', 120)" resizable />
      <el-table-column label="盘点类型" prop="takeType" :width="colWidth('takeType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_take_type" :value="scope.row.takeType" /></template></el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_take_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="计划日期" prop="planDate" :width="colWidth('planDate', 120)" resizable align="center" />
      <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="280" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="VideoPlay" @click="handleStart(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['wms:stocktake:edit']">开始</el-button>
          <el-button link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:stocktake:edit']">作废</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:stocktake:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="takeRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item>
        <el-form-item label="库区" prop="areaId"><el-select v-model="form.areaId" filterable clearable placeholder="请选择库区（可选，不选则全仓库）" style="width:100%"><el-option v-for="a in areaOptions" :key="a.warehouseId" :label="a.warehouseName" :value="a.warehouseId" /></el-select></el-form-item>
        <el-form-item label="盘点类型" prop="takeType"><el-select v-model="form.takeType" style="width:100%" @change="onTakeTypeChange"><el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
        <el-form-item label="抽样比例" prop="sampleRatio" v-if="form.takeType === '1'"><el-slider v-model="form.sampleRatio" :min="10" :max="100" :step="10" show-input style="width:100%" /><div style="color:#909399;font-size:12px">按该比例随机抽取库存进行盘点</div></el-form-item>
        <el-form-item label="循环批次" prop="cycleNo" v-if="form.takeType === '2'"><el-select v-model="form.cycleNo" style="width:100%"><el-option :value="1" label="第1轮（共4轮）" /><el-option :value="2" label="第2轮（共4轮）" /><el-option :value="3" label="第3轮（共4轮）" /><el-option :value="4" label="第4轮（共4轮）" /></el-select><div style="color:#909399;font-size:12px">库存分为4个批次，每次盘一个批次</div></el-form-item>
        <el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="盘点详情" v-model="detailOpen" width="900px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="盘点单号">{{ detailData.takeNo }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ detailData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="wms_take_status" :value="detailData.status" /></el-descriptions-item>
        <el-descriptions-item label="盘点类型"><dict-tag :options="wms_take_type" :value="detailData.takeType" /></el-descriptions-item>
        <el-descriptions-item label="抽样比例" v-if="detailData.takeType === '1'">{{ detailData.sampleRatio }}%</el-descriptions-item>
        <el-descriptions-item label="循环批次" v-if="detailData.takeType === '2'">第{{ detailData.cycleNo }}轮（共4轮）</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detailData.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detailData.endTime }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailData.detailList" border style="margin-top: 15px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
        <el-table-column label="物料名称" prop="materialName" min-width="200" show-overflow-tooltip />
        <el-table-column label="库区/库位" width="180">
          <template #default="scope">{{ [scope.row.areaName, scope.row.locationName].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 100)" resizable />
        <el-table-column label="账面数量" prop="bookQty" :width="colWidth('bookQty', 100)" resizable align="right" />
        <el-table-column label="实盘数量" prop="actualQty" :width="colWidth('actualQty', 100)" resizable align="right" />
        <el-table-column label="差异" prop="diffQty" :width="colWidth('diffQty', 100)" resizable align="right">
          <template #default="scope"><span :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span></template>
        </el-table-column>
        <el-table-column label="差异原因" prop="diffReason" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center" v-if="detailData.status === '1'">
          <template #default="scope">
            <el-button link type="primary" @click="handleSubmitDetail(scope.row)">录入</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 录入实盘数量 -->
    <el-dialog title="录入实盘数量" v-model="submitOpen" width="400px" append-to-body>
      <el-form ref="submitRef" :model="inputForm" label-width="100px">
        <el-form-item label="账面数量">{{ inputForm.bookQty }}</el-form-item>
        <el-form-item label="实盘数量" prop="actualQty"><el-input-number v-model="inputForm.actualQty" :precision="2" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="差异原因"><el-input v-model="inputForm.diffReason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitDetailForm">确 定</el-button><el-button @click="submitOpen = false">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsStockTake">
import { listStockTake, getStockTake, addStockTake, delStockTake, startStockTake, submitStockTakeDetail, voidStockTake } from '@/api/wms/stocktake'
import { listWarehouse, listArea } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_stocktake_index')
const { wms_take_type, wms_take_status } = proxy.useDict('wms_take_type', 'wms_take_status')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const detailOpen = ref(false); const detailData = ref({}); const submitOpen = ref(false)
const inputForm = ref({})
const warehouseOptions = ref([]); const areaOptions = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, takeNo: undefined, takeType: undefined, status: undefined }, rules: { warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listStockTake(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.takeId); multiple.value = !sel.length }
function reset() { form.value = { warehouseId: undefined, areaId: undefined, takeType: '0', sampleRatio: 30, cycleNo: 1, planDate: undefined, remark: undefined }; areaOptions.value = []; proxy.resetForm('takeRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加盘点单' }
function submitForm() { proxy.$refs['takeRef'].validate(v => { if (v) { addStockTake(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } }) }
function handleDetail(row) { getStockTake(row.takeId).then(res => { detailData.value = res.data; detailOpen.value = true }) }
function handleStart(row) { proxy.$modal.confirm('确认开始盘点？').then(() => startStockTake(row.takeId)).then(() => { getList(); proxy.$modal.msgSuccess('已开始') }).catch(() => {}) }
function handleVoid(row) { proxy.$modal.confirm('确认作废该盘点单？作废后将无法继续盘点。').then(() => voidStockTake(row.takeId)).then(() => { getList(); proxy.$modal.msgSuccess('已作废') }).catch(() => {}) }
function handleSubmitDetail(row) { inputForm.value = { takeId: detailData.value.takeId, detailId: row.detailId, bookQty: row.bookQty, actualQty: row.actualQty || row.bookQty, diffReason: undefined }; submitOpen.value = true }
function submitDetailForm() { submitStockTakeDetail(inputForm.value.takeId, inputForm.value.detailId, inputForm.value.actualQty, inputForm.value.diffReason).then(() => { proxy.$modal.msgSuccess('录入成功'); submitOpen.value = false; getStockTake(detailData.value.takeId).then(res => { detailData.value = res.data }) }) }
function handleDelete(row) { const takeIds = row.takeId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delStockTake(takeIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function onTakeTypeChange() {
  if (form.value.takeType === '1' && !form.value.sampleRatio) { form.value.sampleRatio = 30 }
  if (form.value.takeType === '2' && !form.value.cycleNo) { form.value.cycleNo = 1 }
}
function onWarehouseChange(warehouseId) {
  form.value.areaId = undefined
  if (warehouseId) {
    listArea({ warehouseId: warehouseId, status: '0', pageSize: 999 }).then(res => { areaOptions.value = res.rows })
  } else {
    areaOptions.value = []
  }
}
listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
getList()
</script>
