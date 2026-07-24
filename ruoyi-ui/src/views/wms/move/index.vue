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
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="移库单号" prop="moveNo" :width="colWidth('moveNo', 160)" resizable />
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
      <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
      <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 100)" resizable />
      <el-table-column label="源库位" prop="fromLocationName" :width="colWidth('fromLocationName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="目标库位" prop="toLocationName" :width="colWidth('toLocationName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="移库数量" prop="moveQty" :width="colWidth('moveQty', 100)" resizable align="right" />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_move_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="操作" width="300" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['wms:move:query']">详情</el-button>
          <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['wms:move:edit']">审批</el-button>
          <el-button link type="primary" icon="Sort" @click="handleExecute(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['wms:move:edit']">执行</el-button>
          <el-button link type="warning" icon="CircleClose" @click="handleVoid(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:move:edit']">作废</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '3'" v-hasPermi="['wms:move:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 9 2 12 5 15"/><polyline points="9 5 12 2 15 5"/><polyline points="15 19 12 22 9 19"/><polyline points="19 9 22 12 19 15"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="12" y1="2" x2="12" y2="22"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="moveRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>仓库与物料</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
        <el-form-item label="仓库" prop="warehouseId">
          <el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange">
            <el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存物料" prop="inventoryId">
          <el-select v-model="form.inventoryId" filterable clearable placeholder="请先选择仓库，再选择库存物料" style="width:100%" :disabled="!form.warehouseId" @change="onInventoryChange">
            <el-option v-for="inv in inventoryOptions" :key="inv.inventoryId" :label="inv.materialCode + ' - ' + inv.materialName + '（批次: ' + (inv.batchNo || '无') + '，库位: ' + (inv.locationName || '') + '，可用: ' + inv.qty + '）'" :value="inv.inventoryId" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchNo" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位"><dict-tag :options="wms_unit" :value="form.unit" /></el-form-item></el-col>
        </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('move')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="5 9 2 12 5 15"/><polyline points="9 5 12 2 15 5"/><polyline points="15 19 12 22 9 19"/><polyline points="19 9 22 12 19 15"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="12" y1="2" x2="12" y2="22"/></svg></span>移库信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.move }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.move">
        <el-form-item label="源库位">
          <el-input :model-value="form.fromLocationName || ''" disabled />
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="可用数量"><el-input v-model="form.availableQty" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="移库数量" prop="moveQty"><el-input-number v-model="form.moveQty" :precision="2" :min="0" :max="form.availableQty || 0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="目标库位" prop="toLocationId">
          <el-select v-model="form.toLocationId" filterable clearable placeholder="请选择目标库位" style="width:100%" @change="onTargetLocationChange">
            <el-option v-for="l in targetLocationOptions" :key="l.warehouseId" :label="(l.warehouseName || '')" :value="l.warehouseId" />
          </el-select>
        </el-form-item>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg>
          </div>
          <span class="rd-detail-header-title">移库单详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.moveNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.moveNo }}</span>
            <dict-tag :options="wms_move_status" :value="detailData.status" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">移库单号</span><div class="rd-value">{{ detailData.moveNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_move_status" :value="detailData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dMaterial')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dMaterial }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dMaterial">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detailData.batchNo || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ detailData.materialCode }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ detailData.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="detailData.unit" /></div></div>
              <div class="rd-item"><span class="rd-label">移库数量</span><div class="rd-value">{{ detailData.moveQty }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dLocation')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></span>库位信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dLocation }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dLocation">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">源库位</span><div class="rd-value">{{ detailData.fromLocationName || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">目标库位</span><div class="rd-value">{{ detailData.toLocationName || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dFlow')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>流程信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dFlow }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dFlow">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ detailData.approveBy || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">审批时间</span><div class="rd-value">{{ detailData.approveTime || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">完成时间</span><div class="rd-value">{{ detailData.completeTime || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ detailData.createBy }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ detailData.createTime }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dOther')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dOther }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dOther">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detailData.remark }">{{ detailData.remark || '暂无备注' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsMove">
import { listMove, getMove, addMove, delMove, approveMove, executeMove, voidMove } from '@/api/wms/move'
import { listWarehouse, listLocation } from '@/api/wms/warehouse'
import { listInventory } from '@/api/wms/inventory'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'move', 'other', 'dBasic', 'dMaterial', 'dLocation', 'dFlow', 'dOther'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_move_index')
const { wms_move_status, wms_unit } = proxy.useDict('wms_move_status', 'wms_unit')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref('')
const warehouseOptions = ref([]); const inventoryOptions = ref([]); const allLocationOptions = ref([]); const detailOpen = ref(false); const detailData = ref({})
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
  form.value = { warehouseId: undefined, inventoryId: undefined, materialId: undefined, unit: undefined, batchNo: undefined, fromLocationId: undefined, fromLocationCode: undefined, fromLocationName: undefined, toLocationId: undefined, toLocationCode: undefined, toLocationName: undefined, moveQty: 0, availableQty: 0, remark: undefined }
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
function handleDetail(row) { getMove(row.moveId).then(res => { detailData.value = res.data; detailOpen.value = true }) }

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
  form.value.unit = undefined
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
    form.value.unit = inv.unit
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
