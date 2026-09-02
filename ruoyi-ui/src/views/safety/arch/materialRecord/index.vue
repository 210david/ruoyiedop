<template>
  <div class="app-container safety-record-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>出入库单号</label>
          <div class="control">
            <el-input v-model="queryParams.recordCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>危化品名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.recordType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_record_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>供应商/客户</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>领用人</label>
          <div class="control">
            <el-input v-model="queryParams.recipient" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>出入库时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:100%" @change="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="createTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:materialRecord:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:materialRecord:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:materialRecord:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_record_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="recordList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="出入库单号" prop="recordCode" key="recordCode" :width="colWidth('recordCode', 160)" resizable show-overflow-tooltip v-if="columns.recordCode.visible" />
          <el-table-column label="危化品名称" prop="materialName" key="materialName" :width="colWidth('materialName', 160)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="类型" prop="recordType" key="recordType" :width="colWidth('recordType', 80)" align="center" resizable v-if="columns.recordType.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.recordType === '1' ? 'blue' : 'amber'"><span class="dot"></span>{{ scope.row.recordType === '1' ? '入库' : '出库' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" key="quantity" :width="colWidth('quantity', 100)" align="center" resizable v-if="columns.quantity.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" align="center" resizable v-if="columns.unit.visible" />
          <el-table-column label="操作前库存" prop="beforeStock" key="beforeStock" :width="colWidth('beforeStock', 110)" align="center" resizable v-if="columns.beforeStock.visible" />
          <el-table-column label="操作后库存" prop="afterStock" key="afterStock" :width="colWidth('afterStock', 110)" align="center" resizable v-if="columns.afterStock.visible" />
          <el-table-column label="供应商/客户" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 140)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="领用人" prop="recipient" key="recipient" :width="colWidth('recipient', 100)" resizable v-if="columns.recipient.visible" />
          <el-table-column label="出入库时间" prop="recordTime" key="recordTime" :width="colWidth('recordTime', 160)" align="center" resizable sortable="custom" v-if="columns.recordTime.visible">
            <template #default="scope">{{ parseTime(scope.row.recordTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:materialRecord:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:materialRecord:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:materialRecord:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>出入库信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="单号" prop="recordCode"><el-input v-model="form.recordCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="类型" prop="recordType"><el-radio-group v-model="form.recordType"><el-radio value="1">入库</el-radio><el-radio value="2">出库</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="危化品" prop="materialId"><el-select v-model="form.materialId" filterable placeholder="请选择危化品" style="width: 100%" @change="onMaterialChange"><el-option v-for="item in materialOptions" :key="item.materialId" :label="item.materialName" :value="item.materialId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="当前库存"><el-input v-model="currentStockDisplay" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="出入库时间" prop="recordTime"><el-date-picker v-model="form.recordTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>来源/去向信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商/客户" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入供应商/客户名称" /></el-form-item></el-col>
                <el-col :span="12" v-if="form.recordType === '2'"><el-form-item label="领用人" prop="recipient"><el-input v-model="form.recipient" readonly placeholder="请选择领用人" style="width: 100%" @click="openRecipientPicker"><template #append><el-button icon="Search" @click="openRecipientPicker" /></template><template #suffix><el-icon v-if="form.recipient" class="clear-icon" @click.stop="clearRecipient"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12" v-if="form.recordType === '2'"><el-form-item label="批次号"><el-input v-model="form.batchNo" placeholder="请输入批次号" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="用途" prop="purpose"><el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="请输入用途" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- 领用人选择弹窗 -->
    <user-picker ref="recipientPickerRef" title="选择领用人" @confirm="onRecipientConfirm" />

    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div><span class="rd-detail-header-title">出入库记录详情</span><div class="rd-detail-header-sub" v-if="viewData.recordCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.recordCode }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>出入库信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">出入库单号</span><div class="rd-value">{{ viewData.recordCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">危化品名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><span class="badge" :class="viewData.recordType === '1' ? 'blue' : 'amber'"><span class="dot"></span>{{ viewData.recordType === '1' ? '入库' : '出库' }}</span></div></div>
              <div class="rd-item"><span class="rd-label">数量</span><div class="rd-value">{{ viewData.quantity }} {{ viewData.unit }}</div></div>
              <div class="rd-item"><span class="rd-label">操作前库存</span><div class="rd-value">{{ viewData.beforeStock != null ? viewData.beforeStock : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">操作后库存</span><div class="rd-value">{{ viewData.afterStock != null ? viewData.afterStock : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">出入库时间</span><div class="rd-value">{{ parseTime(viewData.recordTime) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.supplierName || viewData.recipient || viewData.purpose || viewData.batchNo || viewData.operator">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>来源/去向信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">供应商/客户</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item" v-if="viewData.recordType === '2'"><span class="rd-label">领用人</span><div class="rd-value">{{ viewData.recipient || '-' }}</div></div>
              <div class="rd-item" v-if="viewData.recordType === '2'"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">用途</span><div class="rd-value">{{ viewData.purpose || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyMaterialRecord">
import { listMaterialRecord, getMaterialRecord, addMaterialRecord, updateMaterialRecord, delMaterialRecord } from '@/api/safety/materialRecord'
import { listMaterial } from '@/api/safety/material'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_record_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])
const { safety_record_type } = proxy.useDict('safety_record_type')

const recordList = ref([])
const materialOptions = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const createTimeRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  recordCode: { label: '出入库单号', visible: true },
  materialName: { label: '危化品名称', visible: true },
  recordType: { label: '类型', visible: true },
  quantity: { label: '数量', visible: true },
  unit: { label: '单位', visible: true },
  beforeStock: { label: '操作前库存', visible: true },
  afterStock: { label: '操作后库存', visible: true },
  supplierName: { label: '供应商/客户', visible: true },
  recipient: { label: '领用人', visible: true },
  recordTime: { label: '出入库时间', visible: true }
}

function loadColumnVisibility() {
  try { const saved = localStorage.getItem('safety_record_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, recordCode: undefined, materialName: undefined, recordType: undefined, supplierName: undefined, recipient: undefined, params: {} },
  rules: {
    materialId: [{ required: true, message: '危化品不能为空', trigger: 'change' }],
    recordType: [{ required: true, message: '类型不能为空', trigger: 'change' }],
    quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }],
    recordTime: [{ required: true, message: '出入库时间不能为空', trigger: 'change' }],
    purpose: [{ required: true, message: '用途不能为空', trigger: 'blur' }],
    recipient: [{ required: true, message: '领用人不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.recordCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.recordType) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.recipient) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  if (createTimeRange.value && createTimeRange.value.length > 0) count++
  return count
})
const currentStockDisplay = computed(() => { const m = materialOptions.value.find(i => i.materialId === form.value.materialId); return m ? (m.currentStock || 0) + ' ' + (m.storageUnit || '') : '-' })

function getList() {
  loading.value = true
  proxy.addDateRange(queryParams.value, dateRange.value, 'RecordTime')
  proxy.addDateRange(queryParams.value, createTimeRange.value, 'CreateTime')
  listMaterialRecord(queryParams.value).then(response => { recordList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }).catch(error => { console.error(error) }).finally(() => { loading.value = false })
}
function handleQuery() {
  queryParams.value.pageNum = 1
  showAdvanced.value = false
  getList()
}
function resetQuery() {
  queryParams.value.recordCode = undefined
  queryParams.value.materialName = undefined
  queryParams.value.recordType = undefined
  queryParams.value.supplierName = undefined
  queryParams.value.recipient = undefined
  queryParams.value.params = {}
  dateRange.value = []
  createTimeRange.value = []
  if (tableRef.value) tableRef.value.clearSort()
  handleQuery()
}
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.recordId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; open.value = true; title.value = '新增出入库记录'; form.value.recordType = '1'; form.value.recordTime = parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}') }
function handleView(row) { const recordId = row.recordId || ids.value[0]; getMaterialRecord(recordId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { const recordId = row.recordId || ids.value[0]; getMaterialRecord(recordId).then(response => { reset(); form.value = { ...response.data }; collapsedCards.c0 = false; collapsedCards.c1 = false; open.value = true; title.value = '修改出入库记录' }) }
function submitForm() {
  proxy.$refs['recordRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) { updateMaterialRecord(form.value).then(() => { proxy.$modal.msgSuccess('修改成功，库存已同步调整'); open.value = false; getList(); loadMaterials() }) }
      else { addMaterialRecord(form.value).then(() => { proxy.$modal.msgSuccess('新增成功，库存已自动更新'); open.value = false; getList(); loadMaterials() }) }
    }
  })
}
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('是否确认删除该出入库记录？删除后库存将自动冲正').then(function() { return delMaterialRecord(recordIds) }).then(() => { getList(); loadMaterials(); proxy.$modal.msgSuccess('删除成功，库存已冲正') }).catch(() => {}) }
function handleExport() { proxy.download('safety/materialRecord/export', { ...queryParams.value }, `materialRecord_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() { form.value = { recordId: undefined, recordCode: undefined, materialId: undefined, recordType: '1', quantity: undefined, supplierName: undefined, recipient: undefined, purpose: undefined, batchNo: undefined, recordTime: undefined, remark: undefined }; proxy.resetForm('recordRef') }
function onMaterialChange(val) { /* could auto-fill unit etc */ }
/** 打开领用人选择弹窗 */
function openRecipientPicker() { proxy.$refs.recipientPickerRef.open(form.value.recipientId) }
/** 领用人选择确认回调 */
function onRecipientConfirm(user) { form.value.recipient = user.nickName; form.value.recipientId = user.userId }
/** 清除领用人 */
function clearRecipient() { form.value.recipient = undefined; form.value.recipientId = undefined }
function loadMaterials() { listMaterial({ pageNum: 1, pageSize: 9999 }).then(response => { materialOptions.value = response.rows }) }

loadMaterials()
getList()
</script>

<style scoped>
.safety-record-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-record-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-record-page .filter-card { padding:14px 20px 16px; }
.safety-record-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-record-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-record-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-record-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-record-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-record-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-record-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-record-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-record-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-record-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-record-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-record-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-record-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-record-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-record-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-record-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-record-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-record-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-record-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-record-page .field .control :deep(.el-select) { width:100%; }
.safety-record-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-record-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-record-page .toolbar .left, .safety-record-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-record-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-record-page .table-wrap { overflow-x:auto; }
.safety-record-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-record-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-record-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-record-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-record-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-record-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-record-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-record-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-record-page .badge.amber .dot { background:var(--amber-500); }
.safety-record-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-record-page .badge.blue .dot { background:var(--blue-500); }
.safety-record-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-record-page .badge.gray .dot { background:var(--ink-400); }
..safety-record-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } ..safety-record-page .badge.green .dot { background:var(--green-500); }
..safety-record-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } ..safety-record-page .badge.red .dot { background:var(--red-500); }
..safety-record-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-record-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-record-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-record-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.safety-record-page .clear-icon { cursor:pointer; color:var(--ink-400); }
.safety-record-page .clear-icon:hover { color:var(--ink-700); }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
