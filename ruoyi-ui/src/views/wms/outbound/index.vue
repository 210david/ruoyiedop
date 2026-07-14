<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="出库单号" prop="orderNo"><el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="出库类型" prop="orderType"><el-select v-model="queryParams.orderType" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_outbound_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_outbound_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:outbound:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:outbound:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:outbound:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="出库单号" prop="orderNo" width="200" />
      <el-table-column label="出库类型" prop="orderType" width="100" align="center"><template #default="scope"><dict-tag :options="wms_outbound_type" :value="scope.row.orderType" /></template></el-table-column>
      <el-table-column label="出库仓库" prop="warehouseName" width="150" />
      <el-table-column label="状态" prop="status" width="100" align="center"><template #default="scope"><dict-tag :options="wms_outbound_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="总数量" prop="totalQty" width="100" align="right" />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:outbound:edit']" v-if="scope.row.status === '0'">修改</el-button>
          <el-button link type="primary" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['wms:outbound:edit']" v-if="scope.row.status === '0'">提交</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:outbound:remove']" v-if="scope.row.status === '0'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="1100px" append-to-body>
      <el-form ref="outboundRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="8"><el-form-item label="出库单号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="自动生成" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出库类型" prop="orderType"><el-select v-model="form.orderType" style="width:100%"><el-option v-for="d in wms_outbound_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出库仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="预计出库" prop="outboundDate"><el-date-picker v-model="form.outboundDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="center">出库明细（从仓库库存中选择）</el-divider>
        <el-alert v-if="!form.warehouseId" title="请先选择出库仓库，系统将加载该仓库的可用库存" type="info" :closable="false" show-icon style="margin-bottom: 10px" />
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddDetail" :disabled="!form.warehouseId">添加明细</el-button></el-col></el-row>
        <el-table :data="form.detailList" border>
          <el-table-column label="库存物料" min-width="280">
            <template #default="scope">
              <el-select v-model="scope.row.inventoryId" filterable clearable placeholder="请选择库存物料" style="width:100%" @change="handleInventoryChange(scope.$index, $event)">
                <el-option v-for="inv in inventoryOptions" :key="inv.inventoryId" :label="formatInventoryLabel(inv)" :value="inv.inventoryId" :disabled="isInventorySelected(inv.inventoryId, scope.$index)" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="物料编码" prop="materialCode" width="120" />
          <el-table-column label="物料名称" prop="materialName" width="150" show-overflow-tooltip />
          <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
          <el-table-column label="单位" prop="unit" width="70" align="center">
            <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchNo" width="110" />
          <el-table-column label="库位" prop="locationCode" width="90" />
          <el-table-column label="可用库存" prop="availableQty" width="90" align="right" />
          <el-table-column label="出库数量" width="130">
            <template #default="scope">
              <el-input-number v-model="scope.row.planQty" :precision="2" :min="0" :max="scope.row.availableQty" controls-position="right" style="width:100%" @change="validatePlanQty(scope.$index)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
        </el-table>
        <el-form-item label="备注" prop="remark" style="margin-top: 15px"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 出库单详情对话框 -->
    <el-dialog title="出库单详情" v-model="detailOpen" width="1000px" append-to-body>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="出库单号">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="出库类型"><dict-tag :options="wms_outbound_type" :value="detailData.orderType" /></el-descriptions-item>
        <el-descriptions-item label="状态"><dict-tag :options="wms_outbound_status" :value="detailData.status" /></el-descriptions-item>
        <el-descriptions-item label="出库仓库">{{ detailData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ detailData.totalQty }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailData.detailList" border style="margin-top: 15px">
        <el-table-column label="物料编码" prop="materialCode" width="120" />
        <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip />
        <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
        <el-table-column label="单位" prop="unit" width="70" align="center">
          <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" width="110" />
        <el-table-column label="计划数量" prop="planQty" width="90" align="right" />
        <el-table-column label="已拣货" prop="pickQty" width="90" align="right" />
        <el-table-column label="实际数量" prop="actualQty" width="90" align="right" />
        <el-table-column label="拣货库位" prop="locationCode" width="90" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="WmsOutbound">
import { listOutbound, getOutbound, addOutbound, updateOutbound, delOutbound, submitOutbound } from '@/api/wms/outbound'
import { listWarehouse } from '@/api/wms/warehouse'
import { listInventory } from '@/api/wms/inventory'
const { proxy } = getCurrentInstance()
const { wms_outbound_type, wms_outbound_status, wms_unit } = proxy.useDict('wms_outbound_type', 'wms_outbound_status', 'wms_unit')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const detailOpen = ref(false); const detailData = ref({})
const warehouseOptions = ref([]); const inventoryOptions = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderType: undefined, status: undefined }, rules: { orderType: [{ required: true, message: '出库类型不能为空', trigger: 'change' }], warehouseId: [{ required: true, message: '出库仓库不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listOutbound(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.orderId); multiple.value = !sel.length }
function reset() { form.value = { orderNo: undefined, orderType: '0', warehouseId: undefined, outboundDate: undefined, status: '0', remark: undefined, detailList: [] }; proxy.resetForm('outboundRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加出库单' }
function handleUpdate(row) { reset(); getOutbound(row.orderId).then(res => { form.value = res.data; open.value = true; title.value = '修改出库单'; onWarehouseChange(form.value.warehouseId) }) }
function handleDetail(row) { getOutbound(row.orderId).then(res => { detailData.value = res.data; detailOpen.value = true }) }

/** 格式化库存物料下拉框标签 */
function formatInventoryLabel(inv) {
  let label = inv.materialCode + ' - ' + inv.materialName
  if (inv.batchNo) { label += ' [' + inv.batchNo + ']' }
  if (inv.locationCode) { label += ' (' + inv.locationCode + ')' }
  return label
}

/** 添加明细行 */
function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = []
  form.value.detailList.push({
    inventoryId: undefined, materialId: undefined, materialCode: '', materialName: '',
    specModel: '', unit: '', batchNo: '', locationId: undefined, locationCode: '',
    availableQty: 0, planQty: 0
  })
}
function handleDeleteDetail(index) { form.value.detailList.splice(index, 1) }

/** 选中库存物料时，自动带出所有字段 */
function handleInventoryChange(index, inventoryId) {
  const inv = inventoryOptions.value.find(i => i.inventoryId === inventoryId)
  if (inv) {
    const row = form.value.detailList[index]
    row.materialId = inv.materialId
    row.materialCode = inv.materialCode
    row.materialName = inv.materialName
    row.specModel = inv.specModel
    row.unit = inv.unit
    row.batchNo = inv.batchNo || ''
    row.locationId = inv.locationId
    row.locationCode = inv.locationCode
    row.availableQty = inv.qty
    row.planQty = inv.qty
  } else {
    const row = form.value.detailList[index]
    row.materialId = undefined; row.materialCode = ''; row.materialName = ''
    row.specModel = ''; row.unit = ''; row.batchNo = ''
    row.locationId = undefined; row.locationCode = ''
    row.availableQty = 0; row.planQty = 0
  }
}

/** 判断库存是否已被其他行选中（防止重复出库同一库存） */
function isInventorySelected(inventoryId, currentIndex) {
  if (!form.value.detailList) return false
  return form.value.detailList.some((r, i) => i !== currentIndex && r.inventoryId === inventoryId)
}

/** 校验出库数量不超过可用库存 */
function validatePlanQty(index) {
  const row = form.value.detailList[index]
  if (row.planQty && row.availableQty && row.planQty > row.availableQty) {
    row.planQty = row.availableQty
    proxy.$modal.msgWarning('出库数量不能超过可用库存数量')
  }
}

/** 提交表单 */
function submitForm() {
  if (!validateDetailList()) return
  proxy.$refs['outboundRef'].validate(v => {
    if (v) {
      if (form.value.orderId != null) {
        updateOutbound(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addOutbound(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

/** 校验明细列表 */
function validateDetailList() {
  if (!form.value.detailList || form.value.detailList.length === 0) {
    proxy.$modal.msgError('请至少添加一条出库明细')
    return false
  }
  for (let i = 0; i < form.value.detailList.length; i++) {
    const d = form.value.detailList[i]
    if (!d.inventoryId) { proxy.$modal.msgError('第' + (i + 1) + '行：请选择库存物料'); return false }
    if (!d.planQty || d.planQty <= 0) { proxy.$modal.msgError('第' + (i + 1) + '行：出库数量必须大于0'); return false }
    if (d.availableQty && d.planQty > d.availableQty) { proxy.$modal.msgError('第' + (i + 1) + '行：出库数量不能超过可用库存' + d.availableQty); return false }
  }
  return true
}

function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delOutbound(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交出库单「' + row.orderNo + '」？提交后将无法修改。').then(() => submitOutbound(row.orderId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handleExport() { proxy.download('wms/outbound/export', { ...queryParams.value }, 'outbound_' + new Date().getTime() + '.xlsx') }
function cancel() { open.value = false; reset() }

/** 仓库变更时加载该仓库的库存（仅可用数量>0的） */
function onWarehouseChange(warehouseId) {
  inventoryOptions.value = []
  if (form.value.detailList) {
    form.value.detailList.forEach(d => {
      d.inventoryId = undefined; d.materialId = undefined; d.materialCode = ''
      d.materialName = ''; d.specModel = ''; d.unit = ''; d.batchNo = ''
      d.locationId = undefined; d.locationCode = ''; d.availableQty = 0; d.planQty = 0
    })
  }
  if (warehouseId) {
    listInventory({ warehouseId: warehouseId, pageNum: 1, pageSize: 999 }).then(res => {
      inventoryOptions.value = res.rows.filter(r => r.qty > 0)
    })
  }
}

function loadOptions() {
  listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
}
loadOptions()
getList()
</script>
