<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="单据号" prop="documentCode">
        <el-input v-model="queryParams.documentCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="moveType">
        <el-select v-model="queryParams.moveType" placeholder="请选择" clearable style="width: 120px">
          <el-option label="入库" value="0" />
          <el-option label="出库" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="备件编号" prop="partCode">
        <el-input v-model="queryParams.partCode" placeholder="请输入" clearable style="width: 130px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商/部门" prop="supplierOrDept">
        <el-input v-model="queryParams.supplierOrDept" placeholder="请输入" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partstock:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:partstock:export']">导出流水账</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange" border @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      <el-table-column label="单据号" prop="documentCode" :width="colWidth('documentCode', 140)" resizable />
      <el-table-column label="备件编号" prop="partCode" :width="colWidth('partCode', 120)" resizable />
      <el-table-column label="备件名称" prop="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip />
      <el-table-column label="类型" prop="moveType" :width="colWidth('moveType', 80)" resizable align="center">
        <template #default="scope">
          <el-tag :type="scope.row.moveType === '0' ? 'success' : 'warning'">
            {{ {0:'入库',1:'出库'}[scope.row.moveType] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="出入库类型" :width="colWidth('出入库类型', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag v-if="scope.row.moveType === '0'" :options="dms_partin_type" :value="scope.row.sourceType" />
          <dict-tag v-else :options="dms_partout_type" :value="scope.row.targetType" />
        </template>
      </el-table-column>
      <el-table-column label="供应商/部门" prop="supplierOrDept" :width="colWidth('supplierOrDept', 150)" resizable show-overflow-tooltip />
      <el-table-column label="数量" prop="quantity" :width="colWidth('quantity', 80)" resizable align="center" />
      <el-table-column label="变更前" prop="beforeStock" :width="colWidth('beforeStock', 80)" resizable align="center" />
      <el-table-column label="变更后" prop="afterStock" :width="colWidth('afterStock', 80)" resizable align="center" />
      <el-table-column label="操作人" prop="operatorName" :width="colWidth('operatorName', 80)" resizable />
      <el-table-column label="备注" prop="remark" :width="colWidth('remark', 150)" resizable show-overflow-tooltip />
      <el-table-column label="操作" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partstock:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="DmsPartStock">
import { listPartStock, delPartStock } from '@/api/dms/partstock'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_partstock_index')
const { dms_partin_type, dms_partout_type } = proxy.useDict('dms_partin_type', 'dms_partout_type')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    documentCode: undefined,
    moveType: undefined,
    partCode: undefined,
    supplierOrDept: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listPartStock(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); multiple.value = !selection.length }
function handleExport() { proxy.download('dms/sparepart/partstock/export', { ...queryParams.value }, `partstock_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的流水记录？').then(() => delPartStock(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

onActivated(() => {
  getList()
})

getList()
</script>
