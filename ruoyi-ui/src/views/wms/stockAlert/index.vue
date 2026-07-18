<template>
  <div class="app-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6" v-for="item in summaryCards" :key="item.type">
        <el-card shadow="hover" :body-style="{ padding: '16px 20px' }" @click="handleTabClick(item.type)">
          <div class="summary-card" :class="{ active: activeTab === item.type }">
            <div class="summary-icon" :style="{ background: item.bg }">
              <el-icon :size="24" :color="item.color"><component :is="item.icon" /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-label">{{ item.label }}</div>
              <div class="summary-count">{{ item.count }} 条</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预警类型 Tab -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="过期预警" name="expired" />
      <el-tab-pane label="临期预警" name="near_expiry" />
      <el-tab-pane label="库存不足" name="low_stock" />
      <el-tab-pane label="库存积压" name="overstock" />
    </el-tabs>

    <!-- 查询条件 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="物料编码" prop="materialCode">
        <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="物料名称" prop="materialName">
        <el-input v-model="queryParams.materialName" placeholder="请输入物料名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="物料类型" prop="materialType">
        <el-select v-model="queryParams.materialType" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="dict in wms_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="请选择" clearable style="width: 180px">
          <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleBatchDelete" v-hasPermi="['wms:stockAlert:remove']">批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:stockAlert:export']">导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button icon="Refresh" @click="getList">刷新</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 预警列表 -->
    <el-table border v-loading="loading" :data="alertList" :row-class-name="rowClassName" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="预警类型" prop="alertType" :width="colWidth('alertType', 110)" resizable align="center">
        <template #default="scope">
          <el-tag :type="alertTagType(scope.row.alertType)" effect="dark" size="small">{{ alertLabel(scope.row.alertType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 130)" resizable />
      <el-table-column label="物料名称" prop="materialName" min-width="180" show-overflow-tooltip />
      <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 160)" resizable show-overflow-tooltip />
      <el-table-column label="物料类型" prop="materialType" :width="colWidth('materialType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_material_type" :value="scope.row.materialType" /></template>
      </el-table-column>
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
      </el-table-column>
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 130)" resizable show-overflow-tooltip>
        <template #default="scope">{{ scope.row.warehouseName || '—（汇总）' }}</template>
      </el-table-column>
      <el-table-column label="当前库存" prop="qty" :width="colWidth('qty', 100)" resizable align="right">
        <template #default="scope">
          <span :style="{ color: scope.row.alertType === 'low_stock' ? '#f56c6c' : scope.row.alertType === 'overstock' ? '#e6a23c' : '#409eff', fontWeight: 'bold' }">{{ scope.row.qty }}</span>
        </template>
      </el-table-column>
      <el-table-column label="安全下限" prop="safetyStockMin" :width="colWidth('safetyStockMin', 100)" resizable align="right" />
      <el-table-column label="安全上限" prop="safetyStockMax" :width="colWidth('safetyStockMax', 100)" resizable align="right" />
      <el-table-column label="生产日期" prop="productionDate" :width="colWidth('productionDate', 110)" resizable align="center">
        <template #default="scope">{{ scope.row.productionDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="有效期" prop="expiryDate" :width="colWidth('expiryDate', 110)" resizable align="center">
        <template #default="scope">
          <span v-if="scope.row.expiryDate" :style="{ color: scope.row.alertType === 'expired' ? '#f56c6c' : '#e6a23c' }">{{ scope.row.expiryDate }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="距过期(天)" prop="daysUntilExpiry" :width="colWidth('daysUntilExpiry', 100)" resizable align="center">
        <template #default="scope">
          <span v-if="scope.row.daysUntilExpiry !== null && scope.row.daysUntilExpiry !== undefined" :style="{ color: scope.row.daysUntilExpiry < 0 ? '#f56c6c' : '#e6a23c', fontWeight: 'bold' }">
            {{ scope.row.daysUntilExpiry < 0 ? '已过期' + Math.abs(scope.row.daysUntilExpiry) + '天' : '剩' + scope.row.daysUntilExpiry + '天' }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="预警说明" min-width="180" show-overflow-tooltip>
        <template #default="scope">{{ alertDesc(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:stockAlert:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsStockAlert">
import { listStockAlert, getStockAlertSummary, delStockAlert } from '@/api/wms/stockAlert'
import { listWarehouse } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('wms_stockAlert_index')
const { wms_material_type, wms_unit } = proxy.useDict('wms_material_type', 'wms_unit')

const alertList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeTab = ref('')
const warehouseOptions = ref([])
const summaryData = ref({})
const ids = ref([])
const multiple = ref(true)

const summaryCards = computed(() => [
  { type: 'expired', label: '过期预警', count: summaryData.value.expired || 0, icon: 'Warning', color: '#fff', bg: '#f56c6c' },
  { type: 'near_expiry', label: '临期预警', count: summaryData.value.near_expiry || 0, icon: 'Clock', color: '#fff', bg: '#e6a23c' },
  { type: 'low_stock', label: '库存不足', count: summaryData.value.low_stock || 0, icon: 'Goods', color: '#fff', bg: '#f56c6c' },
  { type: 'overstock', label: '库存积压', count: summaryData.value.overstock || 0, icon: 'Box', color: '#fff', bg: '#909399' }
])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    alertType: undefined,
    materialCode: undefined,
    materialName: undefined,
    materialType: undefined,
    warehouseId: undefined
  }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listStockAlert(queryParams.value).then(res => {
    alertList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function getSummary() {
  getStockAlertSummary().then(res => {
    const map = {}
    res.data.forEach(item => { map[item.alertType] = item.count })
    summaryData.value = map
  })
}

function getWarehouseOptions() {
  listWarehouse({ pageNum: 1, pageSize: 999 }).then(res => {
    warehouseOptions.value = res.rows || []
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  activeTab.value = ''
  queryParams.value.alertType = undefined
  handleQuery()
}

function handleTabClick(tab) {
  const type = typeof tab === 'string' ? tab : tab.paneName
  activeTab.value = type
  queryParams.value.alertType = type || undefined
  handleQuery()
}

function handleExport() {
  proxy.download('wms/stockAlert/export', { ...queryParams.value }, `stock_alert_${new Date().getTime()}.xlsx`)
}

function handleSelectionChange(sel) {
  ids.value = sel.map(i => ({ alertType: i.alertType, materialId: i.materialId, warehouseId: i.warehouseId, productionDate: i.productionDate, expiryDate: i.expiryDate }))
  multiple.value = !sel.length
}

function handleDelete(row) {
  const desc = alertLabel(row.alertType) + ' - ' + (row.materialCode || '')
  proxy.$modal.confirm('确认删除该预警记录？\n' + desc + '\n（临期/过期预警将删除对应库存记录，库存不足/积压预警将清除安全库存设置）').then(() => {
    return delStockAlert({ alertType: row.alertType, materialId: row.materialId, warehouseId: row.warehouseId, productionDate: row.productionDate, expiryDate: row.expiryDate })
  }).then(() => {
    getList()
    getSummary()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleBatchDelete() {
  if (!ids.value.length) {
    proxy.$modal.msgWarning('请先选择要删除的预警记录')
    return
  }
  proxy.$modal.confirm('确认批量删除选中的 ' + ids.value.length + ' 条预警记录？').then(() => {
    return Promise.all(ids.value.map(item => delStockAlert(item)))
  }).then(() => {
    getList()
    getSummary()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function alertLabel(type) {
  const map = { low_stock: '库存不足', overstock: '库存积压', near_expiry: '临期预警', expired: '过期预警' }
  return map[type] || type
}

function alertTagType(type) {
  const map = { low_stock: 'danger', overstock: 'warning', near_expiry: 'warning', expired: 'danger' }
  return map[type] || 'info'
}

function alertDesc(row) {
  if (row.alertType === 'low_stock') {
    return `当前库存 ${row.qty}，低于安全下限 ${row.safetyStockMin}，缺口 ${(Number(row.safetyStockMin) - Number(row.qty)).toFixed(2)}`
  }
  if (row.alertType === 'overstock') {
    return `当前库存 ${row.qty}，超过安全上限 ${row.safetyStockMax}，超出 ${(Number(row.qty) - Number(row.safetyStockMax)).toFixed(2)}`
  }
  if (row.alertType === 'near_expiry') {
    return `将于 ${row.expiryDate} 过期，剩余 ${row.daysUntilExpiry} 天（预警天数 ${row.expiryAlertDays || 30} 天）`
  }
  if (row.alertType === 'expired') {
    return `已于 ${row.expiryDate} 过期，已过期 ${Math.abs(row.daysUntilExpiry)} 天，库存 ${row.qty}`
  }
  return ''
}

function rowClassName({ row }) {
  if (row.alertType === 'expired') return 'alert-row-expired'
  if (row.alertType === 'near_expiry') return 'alert-row-near'
  if (row.alertType === 'low_stock') return 'alert-row-low'
  if (row.alertType === 'overstock') return 'alert-row-over'
  return ''
}

getWarehouseOptions()
getList()
getSummary()
</script>

<style scoped>
.summary-card {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}
.summary-card.active {
  transform: scale(1.02);
}
.summary-card:hover {
  transform: scale(1.03);
}
.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}
.summary-info {
  flex: 1;
}
.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.summary-count {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
}
:deep(.alert-row-expired) {
  background-color: #fef0f0;
}
:deep(.alert-row-near) {
  background-color: #fdf6ec;
}
:deep(.alert-row-low) {
  background-color: #fef0f0;
}
:deep(.alert-row-over) {
  background-color: #f4f4f5;
}
</style>
