<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="物料编码" prop="materialCode"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="物料名称" prop="materialName"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="仓库ID" prop="warehouseId"><el-input v-model="queryParams.warehouseId" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:inventory:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="物料编码" prop="materialCode" width="120" />
      <el-table-column label="物料名称" prop="materialName" width="224" show-overflow-tooltip />
      <el-table-column label="规格型号" prop="specModel" width="180" show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" width="80" align="center">
        <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
      </el-table-column>
      <el-table-column label="仓库/库区/库位" prop="warehouseName" width="220" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.warehouseName || '' }}<span v-if="scope.row.areaName">/{{ scope.row.areaName }}</span><span v-if="scope.row.locationName">/{{ scope.row.locationName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="批次号" prop="batchNo" width="120" />
      <el-table-column label="生产日期" prop="productionDate" width="120" align="center" />
      <el-table-column label="有效期" prop="expiryDate" width="120" align="center" />
      <el-table-column label="可用数量" prop="qty" width="100" align="right" />
      <el-table-column label="锁定数量" prop="lockQty" width="100" align="right" />
      <el-table-column label="更新时间" prop="updateTime" width="160" align="center" />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventory">
import { listInventory } from '@/api/wms/inventory'
const { proxy } = getCurrentInstance()
const { wms_unit } = proxy.useDict('wms_unit')
const list = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0)
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, warehouseId: undefined } })
const { queryParams } = toRefs(data)
function getList() { loading.value = true; listInventory(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleExport() { proxy.download('wms/inventory/export', { ...queryParams.value }, `inventory_${new Date().getTime()}.xlsx`) }
getList()
</script>
