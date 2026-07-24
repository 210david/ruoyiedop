<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="物料编码" prop="materialCode"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="物料名称" prop="materialName"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="仓库/库区/库位" prop="locationKeyword"><el-input v-model="queryParams.locationKeyword" placeholder="请输入仓库/库区/库位" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="更新时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:inventory:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:inventory:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable sortable="custom" />
      <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 224)" resizable show-overflow-tooltip />
      <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 180)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 80)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
      </el-table-column>
      <el-table-column label="仓库/库区/库位" prop="warehouseName" :width="colWidth('warehouseName', 220)" resizable show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.warehouseName || '' }}<span v-if="scope.row.areaName">/{{ scope.row.areaName }}</span><span v-if="scope.row.locationName">/{{ scope.row.locationName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable />
      <el-table-column label="生产日期" prop="productionDate" :width="colWidth('productionDate', 120)" resizable align="center" />
      <el-table-column label="有效期" prop="expiryDate" :width="colWidth('expiryDate', 120)" resizable align="center" />
      <el-table-column label="可用数量" prop="qty" :width="colWidth('qty', 100)" resizable align="right" />
      <el-table-column label="锁定数量" prop="lockQty" :width="colWidth('lockQty', 100)" resizable align="right" />
      <el-table-column label="更新时间" prop="updateTime" :width="colWidth('updateTime', 160)" resizable align="center" sortable="custom" />
      <el-table-column label="操作" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:inventory:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventory">
import { listInventory, delInventory } from '@/api/wms/inventory'
import { useColumnResize } from '@/composables/useColumnResize'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_inventory_index')
const { wms_unit } = proxy.useDict('wms_unit')
const list = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0); const ids = ref([]); const multiple = ref(true)
const dateRange = ref([])
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, locationKeyword: undefined, params: {} } })
const { queryParams } = toRefs(data)
function getList() {
  loading.value = true
  listInventory(proxy.addDateRange(queryParams.value, dateRange.value, 'UpdateTime')).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm('queryRef'); queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}
function handleExport() { proxy.download('wms/inventory/export', { ...queryParams.value }, `inventory_${new Date().getTime()}.xlsx`) }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.inventoryId); multiple.value = !sel.length }
function handleDelete(row) { const inventoryIds = row.inventoryId || ids.value; proxy.$modal.confirm('确认删除选中的库存记录？').then(() => delInventory(inventoryIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
getList()
</script>
