<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #409eff"><Box /></el-icon>
            <div>
              <div class="stat-num">{{ statTotal }}</div>
              <div class="stat-label">备件种类</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #67c23a"><CircleCheckFilled /></el-icon>
            <div>
              <div class="stat-num">{{ statNormal }}</div>
              <div class="stat-label">库存正常</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #e6a23c"><WarningFilled /></el-icon>
            <div>
              <div class="stat-num">{{ statLow }}</div>
              <div class="stat-label">低于下限</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon class="stat-icon" style="color: #f56c6c"><CircleCloseFilled /></el-icon>
            <div>
              <div class="stat-num">{{ statOut }}</div>
              <div class="stat-label">缺货（≤0）</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="备件编号" prop="partCode">
        <el-input v-model="queryParams.partCode" placeholder="请输入" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备件名称" prop="partName">
        <el-input v-model="queryParams.partName" placeholder="请输入" clearable style="width: 150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="备件类别" prop="partType">
        <el-select v-model="queryParams.partType" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="dict in dms_part_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="库存状态" prop="stockStatus">
        <el-select v-model="queryParams.stockStatus" placeholder="请选择" clearable style="width: 120px">
          <el-option label="正常" value="normal" />
          <el-option label="低于下限" value="low" />
          <el-option label="缺货" value="out" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:sparepart:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['dms:partledger:export']">导出台账</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="filteredList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="备件编号" prop="partCode" :width="colWidth('partCode', 130)" resizable />
      <el-table-column label="备件名称" prop="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip />
      <el-table-column label="备件类别" prop="partType" :width="colWidth('partType', 90)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="dms_part_type" :value="scope.row.partType" />
        </template>
      </el-table-column>
      <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 130)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 60)" resizable align="center">
        <template #default="scope">
          <dict-tag :options="wms_unit" :value="scope.row.unit" />
        </template>
      </el-table-column>
      <el-table-column label="当前库存" prop="currentStock" :width="colWidth('currentStock', 90)" resizable align="center">
        <template #default="scope">
          <span :class="getStockClass(scope.row)">{{ scope.row.currentStock != null ? scope.row.currentStock : 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存下限" prop="stockMin" :width="colWidth('stockMin', 80)" resizable align="center" />
      <el-table-column label="库存上限" prop="stockMax" :width="colWidth('stockMax', 80)" resizable align="center" />
      <el-table-column label="库存状态" :width="colWidth('库存状态', 90)" resizable align="center">
        <template #default="scope">
          <el-tag :type="getStockType(scope.row)" effect="dark" size="small">
            {{ getStockText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="存放位置" prop="storageLocation" :width="colWidth('storageLocation', 110)" resizable show-overflow-tooltip />
      <el-table-column label="供应商" prop="supplier" :width="colWidth('supplier', 110)" resizable show-overflow-tooltip />
      <el-table-column label="操作" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:sparepart:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="DmsPartLedger">
import { listPartLedger, delPartLedger } from '@/api/dms/partledger'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_partledger_index')
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)

const statTotal = ref(0)
const statNormal = ref(0)
const statLow = ref(0)
const statOut = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, partCode: undefined, partName: undefined, partType: undefined, stockStatus: undefined }
})
const { queryParams } = toRefs(data)

/** 前端过滤库存状态 */
const filteredList = computed(() => {
  const status = queryParams.value.stockStatus
  if (!status) return list.value
  return list.value.filter(row => {
    const s = getStockKey(row)
    return s === status
  })
})

function getStockKey(row) {
  const stock = row.currentStock != null ? Number(row.currentStock) : 0
  if (stock <= 0) return 'out'
  const min = row.stockMin != null ? Number(row.stockMin) : 0
  if (min > 0 && stock <= min) return 'low'
  return 'normal'
}
function getStockType(row) {
  const k = getStockKey(row)
  if (k === 'out') return 'danger'
  if (k === 'low') return 'warning'
  return 'success'
}
function getStockText(row) {
  const k = getStockKey(row)
  if (k === 'out') return '缺货'
  if (k === 'low') return '低于下限'
  return '正常'
}
function getStockClass(row) {
  const k = getStockKey(row)
  if (k === 'out') return 'stock-danger'
  if (k === 'low') return 'stock-warning'
  return ''
}

function getList() {
  loading.value = true
  listPartLedger(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    // 统计
    statTotal.value = res.total
    statOut.value = res.rows.filter(i => (i.currentStock != null ? Number(i.currentStock) : 0) <= 0).length
    statLow.value = res.rows.filter(i => {
      const s = i.currentStock != null ? Number(i.currentStock) : 0
      const min = i.stockMin != null ? Number(i.stockMin) : 0
      return s > 0 && min > 0 && s <= min
    }).length
    statNormal.value = res.rows.filter(i => {
      const s = i.currentStock != null ? Number(i.currentStock) : 0
      const min = i.stockMin != null ? Number(i.stockMin) : 0
      return s > 0 && (min === 0 || s > min)
    }).length
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.partId); multiple.value = !selection.length }
function handleDelete(row) { const partIds = row.partId || ids.value; proxy.$modal.confirm('确认删除选中的备件台账记录？').then(() => delPartLedger(partIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('dms/sparepart/export', { ...queryParams.value }, `partledger_${new Date().getTime()}.xlsx`) }

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
.stat-card { border-radius: 8px; }
.stat-body { display: flex; align-items: center; gap: 12px; }
.stat-icon { font-size: 36px; }
.stat-num { font-size: 24px; font-weight: bold; }
.stat-label { font-size: 13px; color: #909399; }
.stock-danger { color: #f56c6c; font-weight: bold; }
.stock-warning { color: #e6a23c; font-weight: bold; }
</style>
