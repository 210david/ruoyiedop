<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-danger">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><WarningFilled /></el-icon>
            <div>
              <div class="alert-num">{{ alertCount }}</div>
              <div class="alert-label">预警备件数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-warning">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><CircleCloseFilled /></el-icon>
            <div>
              <div class="alert-num">{{ shortageCount }}</div>
              <div class="alert-label">库存不足（≤0）</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-info">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><InfoFilled /></el-icon>
            <div>
              <div class="alert-num">{{ lowStockCount }}</div>
              <div class="alert-label">低于下限（>0）</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="alert-card alert-success">
          <div class="alert-card-body">
            <el-icon class="alert-icon"><Goods /></el-icon>
            <div>
              <div class="alert-num">{{ overStockCount }}</div>
              <div class="alert-label">高于上限</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="备件编号" prop="partCode">
        <el-input v-model="queryParams.partCode" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备件名称" prop="partName">
        <el-input v-model="queryParams.partName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partalert:remove']">删除预警</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:partalert:export']">导出预警</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备件编号" prop="partCode" :width="colWidth('partCode', 130)" resizable />
      <el-table-column label="备件名称" prop="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip />
      <el-table-column label="备件类别" prop="partType" :width="colWidth('partType', 100)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="dms_part_type" :value="scope.row.partType" />
        </template>
      </el-table-column>
      <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="wms_unit" :value="scope.row.unit" />
        </template>
      </el-table-column>
      <el-table-column label="当前库存" prop="currentStock" :width="colWidth('currentStock', 90)" resizable align="center">
        <template #default="scope">
          <span :class="getStockClass(scope.row)">{{ scope.row.currentStock != null ? scope.row.currentStock : 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存下限" prop="stockMin" :width="colWidth('stockMin', 90)" resizable align="center" />
      <el-table-column label="库存上限" prop="stockMax" :width="colWidth('stockMax', 90)" resizable align="center" />
      <el-table-column label="预警状态" :width="colWidth('预警状态', 120)" resizable align="center">
        <template #default="scope">
          <el-tag :type="getAlertType(scope.row)" effect="dark">
            {{ getAlertText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="存放位置" prop="storageLocation" :width="colWidth('storageLocation', 120)" resizable show-overflow-tooltip />
      <el-table-column label="供应商" prop="supplier" :width="colWidth('supplier', 120)" resizable show-overflow-tooltip />
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partalert:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="DmsPartAlert">
import { listPartAlert, delPartAlert } from '@/api/dms/partalert'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_partalert_index')
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const multiple = ref(true)

const alertCount = ref(0)
const shortageCount = ref(0)
const lowStockCount = ref(0)
const overStockCount = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, partCode: undefined, partName: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listPartAlert(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 统计
    alertCount.value = res.total
    shortageCount.value = res.rows.filter(i => (i.currentStock != null ? Number(i.currentStock) : 0) <= 0).length
    lowStockCount.value = res.rows.filter(i => {
      const s = i.currentStock != null ? Number(i.currentStock) : 0
      const min = i.stockMin != null ? Number(i.stockMin) : 0
      return s > 0 && s <= min
    }).length
    overStockCount.value = res.rows.filter(i => {
      const s = i.currentStock != null ? Number(i.currentStock) : 0
      const max = i.stockMax != null ? Number(i.stockMax) : 0
      return max > 0 && s >= max
    }).length
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.partId); multiple.value = !selection.length }

function getAlertType(row) {
  const s = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (s <= 0) return 'danger'
  if (min > 0 && s <= min) return 'warning'
  if (max > 0 && s >= max) return 'success'
  return 'info'
}
function getAlertText(row) {
  const s = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (s <= 0) return '库存不足'
  if (min > 0 && s <= min) return '低于下限'
  if (max > 0 && s >= max) return '高于上限'
  return '正常'
}
function getStockClass(row) {
  const s = row.currentStock != null ? Number(row.currentStock) : 0
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  const max = row.stockMax != null ? Number(row.stockMax) : 0
  if (s <= 0) return 'stock-danger'
  if (min > 0 && s <= min) return 'stock-warning'
  if (max > 0 && s >= max) return 'stock-over'
  return ''
}

function handleExport() { proxy.download('dms/sparepart/export', { ...queryParams.value }, `partalert_${new Date().getTime()}.xlsx`) }
function handleDelete(row) {
  const partIds = row.partId || ids.value
  proxy.$modal.confirm('确认删除选中的备件库存预警？\n删除后将清除该备件的安全库存上下限设置。').then(() => delPartAlert(partIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
.alert-card {
  border-radius: 8px;
}
.alert-card-body {
  display: flex;
  align-items: center;
  gap: 12px;
}
.alert-icon {
  font-size: 36px;
}
.alert-num {
  font-size: 24px;
  font-weight: bold;
}
.alert-label {
  font-size: 13px;
  color: #909399;
}
.alert-danger .alert-icon { color: #f56c6c; }
.alert-warning .alert-icon { color: #e6a23c; }
.alert-info .alert-icon { color: #409eff; }
.alert-success .alert-icon { color: #67c23a; }
.stock-danger { color: #f56c6c; font-weight: bold; }
.stock-warning { color: #e6a23c; font-weight: bold; }
.stock-over { color: #67c23a; font-weight: bold; }
</style>
