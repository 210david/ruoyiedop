<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
        <div class="rd-page">
      <el-form-item label="批次号" prop="waveNo"><el-input v-model="queryParams.waveNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_wave_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:wave:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:wave:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="批次号" prop="waveNo" :width="colWidth('waveNo', 160)" resizable />
      <el-table-column label="批次名称" prop="waveName" show-overflow-tooltip />
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_wave_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="总数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
      <el-table-column label="订单数" prop="orderCount" :width="colWidth('orderCount', 80)" resizable align="center" />
      <el-table-column label="操作" width="300" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Connection" @click="openGenerate(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:wave:edit']">添加订单</el-button>
          <el-button link type="primary" icon="Unlock" @click="handleRelease(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['wms:wave:edit']">释放</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:wave:edit']" v-if="scope.row.status === '0'">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:wave:remove']" v-if="scope.row.status === '0' || scope.row.status === '3'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="waveRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="批次名称" prop="waveName"><el-input v-model="form.waveName" placeholder="请输入" /></el-form-item>
        <el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 批次详情对话框 -->
    <el-dialog v-model="detailOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ '批次详情 - ' + (detailData.waveNo || '') }}</span>
        </div>
      </template>
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detailData.waveNo }}</div></div>
        <div class="rd-item"><span class="rd-label">批次名称</span><div class="rd-value">{{ detailData.waveName }}</div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_wave_status" :value="detailData.status" /></div></div>
        <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
        <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ detailData.totalQty }}</div></div>
        <div class="rd-item"><span class="rd-label">订单数</span><div class="rd-value">{{ detailData.orderCount }}</div></div>
      </div>
      <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>关联出库单</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
      <el-table :data="waveOrders" border style="margin-top: 10px" @header-dragend="onHeaderDragEnd">
        <el-table-column label="出库单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
        <el-table-column label="出库类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_outbound_type" :value="scope.row.orderType" /></template></el-table-column>
        <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_outbound_status" :value="scope.row.status" /></template></el-table-column>
        <el-table-column label="总数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
        <el-table-column label="预计出库" prop="outboundDate" :width="colWidth('outboundDate', 120)" resizable align="center" />
      </el-table>
    </el-dialog>

    <!-- 添加出库单对话框 -->
    <el-dialog v-model="generateOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ '添加出库单到批次 - ' + (currentWave.waveNo || '') }}</span>
        </div>
      </template>
      <el-alert title="仅显示同仓库、草稿状态且未关联批次的出库单" type="info" :closable="false" style="margin-bottom: 15px" />
      <el-table :data="availableOrders" @selection-change="handleOrderSelectChange" border @header-dragend="onHeaderDragEnd">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="出库单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
        <el-table-column label="出库类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_outbound_type" :value="scope.row.orderType" /></template></el-table-column>
        <el-table-column label="总数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
        <el-table-column label="预计出库" prop="outboundDate" :width="colWidth('outboundDate', 120)" resizable align="center" />
        <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      </el-table>
      <template #footer>
        <el-button type="primary" :disabled="selectedOrderIds.length === 0" @click="submitGenerate">确认添加 ({{ selectedOrderIds.length }})</el-button>
        <el-button @click="generateOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsWave">
import { listWave, getWave, addWave, updateWave, delWave, generateWave, releaseWave, getWaveOrders } from '@/api/wms/wave'
import { listWarehouse } from '@/api/wms/warehouse'
import { listOutbound } from '@/api/wms/outbound'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c0"])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_outbound_wave')
const { wms_wave_status, wms_outbound_type, wms_outbound_status } = proxy.useDict('wms_wave_status', 'wms_outbound_type', 'wms_outbound_status')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref('')
const detailOpen = ref(false); const detailData = ref({}); const waveOrders = ref([])
const generateOpen = ref(false); const currentWave = ref({}); const availableOrders = ref([]); const selectedOrderIds = ref([])
const warehouseOptions = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, waveNo: undefined, status: undefined }, rules: { waveName: [{ required: true, message: '批次名称不能为空', trigger: 'blur' }], warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listWave(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.waveId); multiple.value = !sel.length }
function reset() { form.value = { waveName: undefined, warehouseId: undefined, remark: undefined }; proxy.resetForm('waveRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加批次' }
function handleUpdate(row) { reset(); getWave(row.waveId).then(res => { form.value = res.data; open.value = true; title.value = '修改批次' }) }
function submitForm() { proxy.$refs['waveRef'].validate(v => { if (v) { if (form.value.waveId != null) { updateWave(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addWave(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const waveIds = row.waveId || ids.value; proxy.$modal.confirm('确认删除？删除后将解除关联的出库单。').then(() => delWave(waveIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function handleDetail(row) {
  getWave(row.waveId).then(res => { detailData.value = res.data; detailOpen.value = true })
  getWaveOrders(row.waveId).then(res => { waveOrders.value = res.data || [] })
}
function openGenerate(row) {
  currentWave.value = row
  selectedOrderIds.value = []
  listOutbound({ warehouseId: row.warehouseId, status: '0', pageSize: 999 }).then(res => {
    availableOrders.value = res.rows.filter(o => !o.waveId)
    generateOpen.value = true
  })
}
function handleOrderSelectChange(sel) { selectedOrderIds.value = sel.map(i => i.orderId) }
function submitGenerate() {
  generateWave(currentWave.value.waveId, selectedOrderIds.value).then(() => {
    proxy.$modal.msgSuccess('添加成功，出库单已关联批次')
    generateOpen.value = false
    getList()
  })
}
function handleRelease(row) {
  proxy.$modal.confirm('确认释放批次？释放后关联的出库单将恢复为草稿状态。').then(() => {
    return releaseWave(row.waveId)
  }).then(() => {
    proxy.$modal.msgSuccess('释放成功')
    getList()
  }).catch(() => {})
}
listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
getList()
</script>
