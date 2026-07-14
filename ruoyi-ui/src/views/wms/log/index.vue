<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="物料编码" prop="materialCode"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="变动类型" prop="changeType"><el-select v-model="queryParams.changeType" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_change_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="关联单据" prop="refOrderNo"><el-input v-model="queryParams.refOrderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="操作时间"><el-date-picker v-model="dateRange" style="width: 240px" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:log:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="物料编码" prop="materialCode" width="120" />
      <el-table-column label="物料名称" prop="materialName" width="280" show-overflow-tooltip />
      <el-table-column label="仓库/库区/库位" prop="warehouseName" width="220" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.warehouseName || '' }}<span v-if="scope.row.areaName">/{{ scope.row.areaName }}</span><span v-if="scope.row.locationName">/{{ scope.row.locationName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="批次号" prop="batchNo" width="100" />
      <el-table-column label="变动类型" prop="changeType" width="100" align="center"><template #default="scope"><dict-tag :options="wms_change_type" :value="scope.row.changeType" /></template></el-table-column>
      <el-table-column label="变动数量" prop="changeQty" width="100" align="right" />
      <el-table-column label="变动前" prop="beforeQty" width="100" align="right" />
      <el-table-column label="变动后" prop="afterQty" width="100" align="right" />
      <el-table-column label="关联单据" prop="refOrderNo" width="150" />
      <el-table-column label="操作人" prop="createBy" width="100" />
      <el-table-column label="操作时间" prop="createTime" width="160" align="center" />
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventoryLog">
import { listInventoryLog } from '@/api/wms/inventory'
const { proxy } = getCurrentInstance()
const { wms_change_type } = proxy.useDict('wms_change_type')
const list = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0); const dateRange = ref([])
const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, changeType: undefined, refOrderNo: undefined } })
const { queryParams } = toRefs(data)
function getList() { loading.value = true; listInventoryLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { dateRange.value = []; proxy.resetForm('queryRef'); handleQuery() }
function handleExport() { proxy.download('wms/log/export', { ...queryParams.value }, `inventory_log_${new Date().getTime()}.xlsx`) }
getList()
</script>
