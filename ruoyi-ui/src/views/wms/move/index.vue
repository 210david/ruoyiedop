<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="移库单号" prop="moveNo"><el-input v-model="queryParams.moveNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_move_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:move:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:move:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:move:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="移库单号" prop="moveNo" width="160" />
      <el-table-column label="仓库" prop="warehouseName" width="120" />
      <el-table-column label="物料编码" prop="materialCode" width="120" />
      <el-table-column label="物料名称" prop="materialName" min-width="200" show-overflow-tooltip />
      <el-table-column label="批次号" prop="batchNo" width="100" />
      <el-table-column label="源库位" width="140" show-overflow-tooltip><template #default="scope">{{ scope.row.fromLocationCode }}{{ scope.row.fromLocationName ? ' / ' + scope.row.fromLocationName : '' }}</template></el-table-column>
      <el-table-column label="目标库位" width="140" show-overflow-tooltip><template #default="scope">{{ scope.row.toLocationCode }}{{ scope.row.toLocationName ? ' / ' + scope.row.toLocationName : '' }}</template></el-table-column>
      <el-table-column label="移库数量" prop="moveQty" width="100" align="right" />
      <el-table-column label="状态" prop="status" width="100" align="center"><template #default="scope"><dict-tag :options="wms_move_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['wms:move:edit']">审批</el-button>
          <el-button link type="primary" icon="Sort" @click="handleExecute(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['wms:move:edit']">执行</el-button>
          <el-button link type="warning" icon="CircleClose" @click="handleVoid(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:move:edit']">作废</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '3'" v-hasPermi="['wms:move:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="moveRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存物料" prop="inventoryId">
          <el-select v-model="form.inventoryId" filterable clearable placeholder="请先选择仓库，再选择库存物料" style="width:100%" :disabled="!form.warehouseId" @change="onInventoryChange">
            <el-option v-for="inv in inventoryOptions" :key="inv.inventoryId" :label="inv.materialCode + ' - ' + inv.materialName + '（批次: ' + (inv.batchNo || '无') + '，库位: ' + (inv.locationCode || '') + '，可用: ' + inv.qty + '）'" :value="inv.inventoryId" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchNo" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="源库位"><el-input :model-value="form.fromLocationCode ? form.fromLocationCode + (form.fromLocationName ? ' / ' + form.fromLocationName : '') : ''" disabled /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="可用数量"><el-input v-model="form.availableQty" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="移库数量" prop="moveQty"><el-input-number v-model="form.moveQty" :precision="2" :min="0" :max="form.availableQty || 0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="目标库位" prop="toLocationId">
          <el-select v-model="form.toLocationId" filterable clearable placeholder="请选择目标库位" style="width:100%" @change="onTargetLocationChange">
            <el-option v-for="l in targetLocationOptions" :key="l.warehouseId" :label="l.warehouseCode + ' / ' + (l.warehouseName || '')" :value="l.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsMove">
import { listMove, getMove, addMove, delMove, approveMove, executeMove, voidMove } from '@/api/wms/move'
import { listWarehouse, listLocation } from '@/api/wms/warehouse'
import { listInventory } from '@/api/wms/inventory'
const { proxy } = getCurrentInstance()
const { wms_move_status } = proxy.useDict('wms_move_status')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref('')
const warehouseOptions = ref([]); const inventoryOptions = ref([]); const allLocationOptions = ref([])
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, moveNo: undefined, status: undefined },
  rules: {
    warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }],
    inventoryId: [{ required: true, message: '请选择库存物料', trigger: 'change' }],
    toLocationId: [{ required: true, message: '目标库位不能为空', trigger: 'change' }],
    moveQty: [{ required: true, message: '移库数量不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 目标库位选项：排除源库位 */
const targetLocationOptions = computed(() => {
  if (!form.value.fromLocationId) return allLocationOptions.value
  return allLocationOptions.value.filter(l => l.warehouseId !== form.value.fromLocationId)
})

function getList() { loading.value = true; listMove(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.moveId); multiple.value = !sel.length }
function reset() {
  form.value = { warehouseId: undefined, inventoryId: undefined, materialId: undefined, batchNo: undefined, fromLocationId: undefined, fromLocationCode: undefined, fromLocationName: undefined, toLocationId: undefined, toLocationCode: undefined, toLocationName: undefined, moveQty: 0, availableQty: 0, remark: undefined }
  inventoryOptions.value = []; allLocationOptions.value = []
  proxy.resetForm('moveRef')
}
function handleAdd() { reset(); open.value = true; title.value = '添加移库单' }
function submitForm() {
  proxy.$refs['moveRef'].validate(v => {
    if (v) {
      if (form.value.moveQty <= 0) { proxy.$modal.msgError('移库数量必须大于0'); return }
      if (form.value.toLocationId === form.value.fromLocationId) { proxy.$modal.msgError('目标库位不能与源库位相同'); return }
      addMove(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
    }
  })
}
function handleApprove(row) { proxy.$modal.confirm('确认审批通过？').then(() => approveMove(row.moveId)).then(() => { getList(); proxy.$modal.msgSuccess('已审批') }).catch(() => {}) }
function handleExecute(row) { proxy.$modal.confirm('确认执行移库？').then(() => executeMove(row.moveId)).then(() => { getList(); proxy.$modal.msgSuccess('执行完成') }).catch(() => {}) }
function handleVoid(row) { proxy.$modal.confirm('确认作废该移库单？作废后不可恢复').then(() => voidMove(row.moveId)).then(() => { getList(); proxy.$modal.msgSuccess('已作废') }).catch(() => {}) }
function handleExport() { proxy.download('wms/move/export', { ...queryParams.value }, `move_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const moveIds = row.moveId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delMove(moveIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }

/** 选择目标库位后：记录编码和名称 */
function onTargetLocationChange(locationId) {
  const loc = allLocationOptions.value.find(l => l.warehouseId === locationId)
  if (loc) {
    form.value.toLocationCode = loc.warehouseCode
    form.value.toLocationName = loc.warehouseName
  } else {
    form.value.toLocationCode = undefined
    form.value.toLocationName = undefined
  }
}

/** 选择仓库后：加载该仓库的库存列表和库位列表 */
function onWarehouseChange(warehouseId) {
  form.value.inventoryId = undefined
  form.value.batchNo = undefined
  form.value.fromLocationId = undefined
  form.value.fromLocationCode = undefined
  form.value.availableQty = 0
  form.value.moveQty = 0
  form.value.toLocationId = undefined
  inventoryOptions.value = []
  allLocationOptions.value = []
  if (warehouseId) {
    listInventory({ warehouseId: warehouseId, pageSize: 999 }).then(res => {
      inventoryOptions.value = res.rows.filter(r => r.qty > 0)
    })
    listLocation({ warehouseId: warehouseId, status: '0', pageSize: 999 }).then(res => {
      allLocationOptions.value = res.rows
    })
  }
}

/** 选择库存物料后：自动带出物料信息、批次号、源库位、可用数量 */
function onInventoryChange(inventoryId) {
  const inv = inventoryOptions.value.find(i => i.inventoryId === inventoryId)
  if (inv) {
    form.value.materialId = inv.materialId
    form.value.materialCode = inv.materialCode
    form.value.materialName = inv.materialName
    form.value.batchNo = inv.batchNo
    form.value.fromLocationId = inv.locationId
    form.value.fromLocationCode = inv.locationCode
    form.value.fromLocationName = inv.locationName
    form.value.availableQty = inv.qty
    form.value.moveQty = inv.qty
    form.value.toLocationId = undefined
  }
}

listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
getList()
</script>
