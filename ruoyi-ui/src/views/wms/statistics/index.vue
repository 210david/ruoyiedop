<template>
  <div class="app-container">
    <!-- 概览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" style="background: #409eff"><el-icon><Box /></el-icon></div>
          <div class="overview-info">
            <div class="overview-label">物料总数</div>
            <div class="overview-value">{{ overview.count || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" style="background: #67c23a"><el-icon><Goods /></el-icon></div>
          <div class="overview-info">
            <div class="overview-label">可用库存</div>
            <div class="overview-value">{{ overview.qty || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" style="background: #e6a23c"><el-icon><Lock /></el-icon></div>
          <div class="overview-info">
            <div class="overview-label">锁定库存</div>
            <div class="overview-value">{{ overview.lockQty || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" style="background: #909399"><el-icon><House /></el-icon></div>
          <div class="overview-info">
            <div class="overview-label">仓库数量</div>
            <div class="overview-value">{{ overview.materialId || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" style="background: #13c2c2"><el-icon><Download /></el-icon></div>
          <div class="overview-info">
            <div class="overview-label">已完成入库</div>
            <div class="overview-value">{{ overview.inboundQty || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4" :lg="4">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-icon" style="background: #f56c6c"><el-icon><Upload /></el-icon></div>
          <div class="overview-info">
            <div class="overview-label">已完成出库</div>
            <div class="overview-value">{{ overview.outboundQty || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 出入库趋势 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>出入库趋势</span>
          <el-date-picker
            v-model="trendDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
            @change="handleTrendQuery"
          />
        </div>
      </template>
      <div ref="trendChart" style="height: 350px" v-loading="trendLoading" />
    </el-card>

    <!-- 库存分布 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>仓库库存分布</span></template>
          <div ref="warehouseChart" style="height: 350px" v-loading="warehouseLoading" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header><span>物料类型库存分布</span></template>
          <div ref="materialTypeChart" style="height: 350px" v-loading="materialTypeLoading" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 出入库类型统计 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>入库类型统计</span>
              <el-date-picker
                v-model="inboundDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 260px"
                @change="handleInboundQuery"
              />
            </div>
          </template>
          <div ref="inboundTypeChart" style="height: 320px" v-loading="inboundLoading" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>出库类型统计</span>
              <el-date-picker
                v-model="outboundDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 260px"
                @change="handleOutboundQuery"
              />
            </div>
          </template>
          <div ref="outboundTypeChart" style="height: 320px" v-loading="outboundLoading" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 物料库存排行 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>物料库存排行 Top 10</span>
          <el-button link type="primary" @click="loadMaterialTop">刷新</el-button>
        </div>
      </template>
      <div ref="materialTopChart" style="height: 400px" v-loading="materialTopLoading" />
    </el-card>

    <!-- 库存预警 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>库存预警（低于安全库存下限）</span>
          <el-tag type="danger" size="small">共 {{ alertList.length }} 条预警</el-tag>
        </div>
      </template>
      <el-table ref="tableRef" border v-loading="alertLoading" :data="alertList" style="width: 100%" @header-dragend="onHeaderDragEnd">
        <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 140)" resizable />
        <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip min-width="180" />
        <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 160)" resizable show-overflow-tooltip />
        <el-table-column label="物料类型" prop="materialType" :width="colWidth('materialType', 100)" resizable align="center">
          <template #default="scope"><dict-tag :options="wms_material_type" :value="scope.row.materialType" /></template>
        </el-table-column>
        <el-table-column label="单位" prop="unit" :width="colWidth('unit', 80)" resizable align="center">
          <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
        </el-table-column>
        <el-table-column label="当前库存" prop="qty" :width="colWidth('qty', 110)" resizable align="right">
          <template #default="scope"><span style="color: #f56c6c; font-weight: bold">{{ scope.row.qty }}</span></template>
        </el-table-column>
        <el-table-column label="安全下限" prop="safetyStockMin" :width="colWidth('safetyStockMin', 110)" resizable align="right" />
        <el-table-column label="安全上限" prop="safetyStockMax" :width="colWidth('safetyStockMax', 110)" resizable align="right" />
        <el-table-column label="缺口" width="100" align="right">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold">{{ (Number(scope.row.safetyStockMin) - Number(scope.row.qty)).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 140)" resizable show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="WmsStatistics">
import * as echarts from 'echarts'
import { getOverview, getInventoryByWarehouse, getInventoryByMaterialType, getInboundOutboundTrend, getInboundByType, getOutboundByType, getMaterialTop, getStockAlert } from '@/api/wms/statistics'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_statistics_index')
const { wms_material_type, wms_unit } = proxy.useDict('wms_material_type', 'wms_unit')

// ==================== 概览 ====================
const overview = ref({})
function loadOverview() {
  getOverview().then(res => { overview.value = res.data || {} })
}

// ==================== 出入库趋势 ====================
const trendChart = ref(null)
const trendLoading = ref(false)
const trendDateRange = ref([])
let trendInstance = null
function loadTrend() {
  trendLoading.value = true
  const params = {}
  if (trendDateRange.value && trendDateRange.value.length === 2) {
    params['params[beginTime]'] = trendDateRange.value[0]
    params['params[endTime]'] = trendDateRange.value[1]
  }
  getInboundOutboundTrend(params).then(res => {
    const data = res.data || []
    trendInstance = echarts.init(trendChart.value)
    trendInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入库数量', '出库数量'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '8%', top: '5%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: data.map(d => d.date) },
      yAxis: { type: 'value' },
      series: [
        { name: '入库数量', type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409eff' }, data: data.map(d => d.inboundQty) },
        { name: '出库数量', type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#f56c6c' }, data: data.map(d => d.outboundQty) }
      ]
    })
    trendLoading.value = false
  }).catch(() => { trendLoading.value = false })
}
function handleTrendQuery() { loadTrend() }

// ==================== 仓库库存分布 ====================
const warehouseChart = ref(null)
const warehouseLoading = ref(false)
let warehouseInstance = null
function loadWarehouse() {
  warehouseLoading.value = true
  getInventoryByWarehouse().then(res => {
    const data = res.data || []
    warehouseInstance = echarts.init(warehouseChart.value)
    warehouseInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', left: 'left', top: 'middle' },
      series: [{
        type: 'pie', radius: ['40%', '70%'], center: ['60%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false }, emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: data.map(d => ({ name: d.name, value: Number(d.totalQty) }))
      }]
    })
    warehouseLoading.value = false
  }).catch(() => { warehouseLoading.value = false })
}

// ==================== 物料类型库存分布 ====================
const materialTypeChart = ref(null)
const materialTypeLoading = ref(false)
let materialTypeInstance = null
function loadMaterialType() {
  materialTypeLoading.value = true
  getInventoryByMaterialType().then(res => {
    const data = res.data || []
    materialTypeInstance = echarts.init(materialTypeChart.value)
    materialTypeInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: '65%', center: ['50%', '45%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%' },
        data: data.map(d => ({ name: d.name, value: Number(d.totalQty) }))
      }]
    })
    materialTypeLoading.value = false
  }).catch(() => { materialTypeLoading.value = false })
}

// ==================== 入库类型统计 ====================
const inboundTypeChart = ref(null)
const inboundLoading = ref(false)
const inboundDateRange = ref([])
let inboundTypeInstance = null
function loadInboundByType() {
  inboundLoading.value = true
  const params = {}
  if (inboundDateRange.value && inboundDateRange.value.length === 2) {
    params['params[beginTime]'] = inboundDateRange.value[0]
    params['params[endTime]'] = inboundDateRange.value[1]
  }
  getInboundByType(params).then(res => {
    const data = res.data || []
    inboundTypeInstance = echarts.init(inboundTypeChart.value)
    inboundTypeInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      xAxis: { type: 'category', data: data.map(d => d.name) },
      yAxis: [
        { type: 'value', name: '数量' },
        { type: 'value', name: '单数' }
      ],
      series: [
        { name: '入库数量', type: 'bar', barWidth: '40%', itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }, data: data.map(d => Number(d.qty)) },
        { name: '入库单数', type: 'line', yAxisIndex: 1, itemStyle: { color: '#67c23a' }, data: data.map(d => d.count) }
      ]
    })
    inboundLoading.value = false
  }).catch(() => { inboundLoading.value = false })
}
function handleInboundQuery() { loadInboundByType() }

// ==================== 出库类型统计 ====================
const outboundTypeChart = ref(null)
const outboundLoading = ref(false)
const outboundDateRange = ref([])
let outboundTypeInstance = null
function loadOutboundByType() {
  outboundLoading.value = true
  const params = {}
  if (outboundDateRange.value && outboundDateRange.value.length === 2) {
    params['params[beginTime]'] = outboundDateRange.value[0]
    params['params[endTime]'] = outboundDateRange.value[1]
  }
  getOutboundByType(params).then(res => {
    const data = res.data || []
    outboundTypeInstance = echarts.init(outboundTypeChart.value)
    outboundTypeInstance.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      xAxis: { type: 'category', data: data.map(d => d.name) },
      yAxis: [
        { type: 'value', name: '数量' },
        { type: 'value', name: '单数' }
      ],
      series: [
        { name: '出库数量', type: 'bar', barWidth: '40%', itemStyle: { color: '#f56c6c', borderRadius: [4, 4, 0, 0] }, data: data.map(d => Number(d.qty)) },
        { name: '出库单数', type: 'line', yAxisIndex: 1, itemStyle: { color: '#e6a23c' }, data: data.map(d => d.count) }
      ]
    })
    outboundLoading.value = false
  }).catch(() => { outboundLoading.value = false })
}
function handleOutboundQuery() { loadOutboundByType() }

// ==================== 物料库存排行 ====================
const materialTopChart = ref(null)
const materialTopLoading = ref(false)
let materialTopInstance = null
function loadMaterialTop() {
  materialTopLoading.value = true
  getMaterialTop(10).then(res => {
    const data = (res.data || []).reverse()
    materialTopInstance = echarts.init(materialTopChart.value)
    materialTopInstance.setOption({
      tooltip: {
        trigger: 'axis', axisPointer: { type: 'shadow' },
        formatter: params => {
          const d = data[params[0].dataIndex]
          return `${d.materialCode} - ${d.materialName}<br/>可用: ${d.qty}<br/>锁定: ${d.lockQty}<br/>合计: ${d.totalQty}`
        }
      },
      grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: data.map(d => d.materialName), axisLabel: { width: 120, overflow: 'truncate' } },
      series: [
        { name: '可用数量', type: 'bar', stack: 'total', barWidth: '60%', itemStyle: { color: '#409eff' }, data: data.map(d => Number(d.qty)) },
        { name: '锁定数量', type: 'bar', stack: 'total', itemStyle: { color: '#e6a23c' }, data: data.map(d => Number(d.lockQty)) }
      ]
    })
    materialTopLoading.value = false
  }).catch(() => { materialTopLoading.value = false })
}

// ==================== 库存预警 ====================
const alertList = ref([])
const alertLoading = ref(false)
function loadStockAlert() {
  alertLoading.value = true
  getStockAlert().then(res => {
    alertList.value = res.data || []
    alertLoading.value = false
  }).catch(() => { alertLoading.value = false })
}

// ==================== 窗口大小自适应 ====================
function handleResize() {
  trendInstance && trendInstance.resize()
  warehouseInstance && warehouseInstance.resize()
  materialTypeInstance && materialTypeInstance.resize()
  inboundTypeInstance && inboundTypeInstance.resize()
  outboundTypeInstance && outboundTypeInstance.resize()
  materialTopInstance && materialTopInstance.resize()
}

onMounted(() => {
  loadOverview()
  loadTrend()
  loadWarehouse()
  loadMaterialType()
  loadInboundByType()
  loadOutboundByType()
  loadMaterialTop()
  loadStockAlert()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendInstance && trendInstance.dispose()
  warehouseInstance && warehouseInstance.dispose()
  materialTypeInstance && materialTypeInstance.dispose()
  inboundTypeInstance && inboundTypeInstance.dispose()
  outboundTypeInstance && outboundTypeInstance.dispose()
  materialTopInstance && materialTopInstance.dispose()
})
</script>

<style scoped lang="scss">
.overview-row {
  margin-bottom: 4px;
}
.overview-card {
  margin-bottom: 12px;
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    padding: 16px 20px;
  }
}
.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  .el-icon {
    font-size: 24px;
    color: #fff;
  }
}
.overview-info {
  flex: 1;
  min-width: 0;
}
.overview-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chart-card {
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
