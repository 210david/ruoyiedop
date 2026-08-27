<template>
  <div class="app-container dms-partin-page">
    <!-- ===== Filter Card ===== -->
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
          <label>单据号</label>
          <div class="control">
            <el-input v-model="queryParams.documentCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>入库类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in dms_partin_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierOrDept" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>备件编号</label>
          <div class="control">
            <el-input v-model="queryParams.partCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>备件名称</label>
          <div class="control">
            <el-input v-model="queryParams.partName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>入库日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:partin:add']">新增入库</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:partstock:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:partin:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_partin_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="单据号" prop="documentCode" key="documentCode" :width="colWidth('documentCode', 140)" resizable v-if="columns.documentCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.documentCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件编号" prop="partCode" key="partCode" :width="colWidth('partCode', 120)" resizable v-if="columns.partCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件名称" prop="partName" key="partName" :width="colWidth('partName', 150)" resizable show-overflow-tooltip v-if="columns.partName.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge amber">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="入库类型" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 100)" resizable align="center" v-if="columns.sourceType.visible">
            <template #default="scope"><span class="badge violet">{{ sourceTypeLabel(scope.row.sourceType) }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplierOrDept" key="supplierOrDept" :width="colWidth('supplierOrDept', 150)" resizable show-overflow-tooltip v-if="columns.supplierOrDept.visible" />
          <el-table-column label="存放位置" prop="storageLocation" key="storageLocation" :width="colWidth('storageLocation', 120)" resizable show-overflow-tooltip v-if="columns.storageLocation.visible" />
          <el-table-column label="数量" prop="quantity" key="quantity" :width="colWidth('quantity', 90)" resizable align="center" v-if="columns.quantity.visible" />
          <el-table-column label="入库日期" prop="operateDate" key="operateDate" :width="colWidth('operateDate', 120)" resizable align="center" v-if="columns.operateDate.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
        <template #default="scope">
          <div class="action-btn-row">
            <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:partin:query']">查看</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:partin:edit']">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:partstock:remove']">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增入库弹窗 -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="partinRef" :model="form" :rules="rules" label-width="100px" :disabled="formDisabled">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12">
            <el-form-item label="单据号" prop="documentCode">
              <el-input v-model="form.documentCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库类型" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dms_partin_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>备件信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="24">
            <el-form-item label="备件" prop="partId">
              <el-input v-model="form.partName" readonly placeholder="请选择备件" style="width: 100%" @click="openSparepartPicker" :disabled="!!form.recordId">
                <template v-if="form.partName && !form.recordId" #append>
                  <el-button icon="CircleClose" @click.stop="clearSparepart" />
                </template>
                <template v-else #append>
                  <el-button icon="Search" @click="openSparepartPicker" :disabled="!!form.recordId" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierOrDept">
              <el-input v-model="form.supplierOrDept" placeholder="选择备件后自动带出" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <dict-tag :options="wms_unit" :value="form.unit" />
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>入库明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row>
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseName">
              <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="storageLocation">
              <el-input v-model="form.storageLocation" placeholder="请输入存放位置" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库日期" prop="operateDate">
              <el-date-picker v-model="form.operateDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number v-model="form.quantity" :min="0.01" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm" v-if="!formDisabled">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 备件选择弹窗 -->
    <sparepart-picker ref="sparepartPickerRef" title="选择备件" @confirm="onSparepartPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="备件入库业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">选择入库类型</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">选择备件</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">填写入库信息</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">库存自动更新</el-tag>
          </div>
        </div>

        <h4>二、入库类型说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="采购入库">通过采购订单收货后自动生成入库记录，备件正式入库存</el-descriptions-item>
          <el-descriptions-item label="工单退料">工单完工后未使用的备件退回库存</el-descriptions-item>
          <el-descriptions-item label="初始入库">备件台账创建时的初始库存录入</el-descriptions-item>
          <el-descriptions-item label="其他入库">盘盈入库、调拨入库等其他场景</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">库存自动同步</div>
              <div class="highlight-card-body">入库保存后，系统<strong>自动更新备件台账</strong>的当前库存数量，无需手动调整</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">信息自动带出</div>
              <div class="highlight-card-body">选择备件后自动带出备件编号、单位、供应商和存放位置等信息，减少手动输入</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>选择入库类型：</strong>根据实际场景选择对应的入库类型
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>选择备件：</strong>从备件台账中选择入库备件，系统自动带出基本信息
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>填写入库信息：</strong>录入入库数量、仓库、存放位置和入库日期</el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>保存入库：</strong>保存后库存自动增加，可在备件台账中查看最新库存</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPartIn">
import { listPartIn, addPartIn, delPartIn, getPartIn, updatePartIn } from '@/api/dms/partin'
import SparepartPicker from '@/components/SparepartPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Delete, Download, ArrowDown, CircleClose } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_partin_index')
const { dms_partin_type, wms_unit } = proxy.useDict('dms_partin_type', 'wms_unit')

const list = ref([])
const sparepartPickerRef = ref(null)
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const formDisabled = ref(false)
const showStatusHelp = ref(false)

const defaultColumns = {
  documentCode: { label: '单据号', visible: true },
  partCode: { label: '备件编号', visible: true },
  partName: { label: '备件名称', visible: true },
  unit: { label: '单位', visible: true },
  sourceType: { label: '入库类型', visible: true },
  supplierOrDept: { label: '供应商', visible: true },
  storageLocation: { label: '存放位置', visible: true },
  quantity: { label: '数量', visible: true },
  operateDate: { label: '入库日期', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_partin_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const showAdvanced = ref(false)
const dateRange = ref([])
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.documentCode) count++
  if (queryParams.value.sourceType) count++
  if (queryParams.value.supplierOrDept) count++
  if (queryParams.value.partCode) count++
  if (queryParams.value.partName) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})
function unitLabel(val) { const item = wms_unit.value.find(d => d.value == val); return item ? item.label : '-' }
function sourceTypeLabel(val) { const item = dms_partin_type.value.find(d => d.value == val); return item ? item.label : '-' }

/** 获取当天日期 YYYY-MM-DD */
function today() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, documentCode: undefined, sourceType: undefined, supplierOrDept: undefined, partCode: undefined, partName: undefined, params: {} },
  rules: {
    sourceType: [{ required: true, message: '入库类型不能为空', trigger: 'change' }],
    partId: [{ required: true, message: '备件不能为空', trigger: 'change' }],
    quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }],
    storageLocation: [{ required: true, message: '存放位置不能为空', trigger: 'blur' }],
    operateDate: [{ required: true, message: '入库日期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPartIn(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.params = proxy.addDateRange(queryParams.value.params, dateRange.value, 'OperateDate'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.documentCode = undefined; queryParams.value.sourceType = undefined; queryParams.value.supplierOrDept = undefined; queryParams.value.partCode = undefined; queryParams.value.partName = undefined; dateRange.value = []; queryParams.value.params = {}; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); multiple.value = !selection.length }
function handleAdd() {
  reset(); open.value = true
  formDisabled.value = false
  title.value = '新增入库'
}
function handleView(row) {
  reset(); formDisabled.value = true
  getPartIn(row.recordId).then(res => { form.value = res.data; open.value = true; title.value = '查看入库记录' })
}
function handleUpdate(row) {
  reset(); formDisabled.value = false
  getPartIn(row.recordId).then(res => { form.value = res.data; open.value = true; title.value = '修改入库记录' })
}
function reset() {
  form.value = {
    documentCode: undefined, sourceType: undefined, partId: undefined, partCode: undefined, partName: undefined,
    unit: undefined, supplierOrDept: undefined, warehouseName: '备件库', storageLocation: undefined, operateDate: today(),
    quantity: 1, operatorName: undefined, remark: undefined
  }
  proxy.resetForm('partinRef')
}
/** 打开备件选择弹窗 */
function openSparepartPicker() {
  if (form.value.recordId) return
  sparepartPickerRef.value.open(form.value.partId)
}
/** 备件选择确认回调 */
function onSparepartPickerConfirm(part) {
  form.value.partId = part.partId
  form.value.partCode = part.partCode
  form.value.partName = part.partName
  form.value.unit = part.unit || ''
  form.value.supplierOrDept = part.supplier || ''
  // 如果备件已有存放位置，自动带出
  if (part.storageLocation) {
    form.value.storageLocation = part.storageLocation
  }
}
/** 清除备件 */
function clearSparepart() {
  form.value.partId = undefined
  form.value.partCode = undefined
  form.value.partName = undefined
  form.value.unit = undefined
  form.value.supplierOrDept = undefined
}
function submitForm() {
  proxy.$refs['partinRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) { updatePartIn(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addPartIn(form.value).then(() => { proxy.$modal.msgSuccess('入库成功'); open.value = false; getList() }) }
    }
  })
}
function cancel() { open.value = false; reset() }
function handleExport() { proxy.download('dms/sparepart/partin/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'OperateDate') }, `partin_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除选中的入库记录？').then(() => delPartIn(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }

onActivated(() => {
  getList()
})

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-partin-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.dms-partin-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-partin-page .filter-card { padding:14px 20px 16px; }
.dms-partin-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-partin-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-partin-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-partin-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-partin-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-partin-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-partin-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-partin-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-partin-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-partin-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-partin-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-partin-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-partin-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-partin-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-partin-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-partin-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-partin-page .field .control :deep(.el-select) { width:100%; }
.dms-partin-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-partin-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-partin-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-partin-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-partin-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-partin-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-partin-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-partin-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-partin-page .btn-soft .el-icon { font-size:14px; }
.dms-partin-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-partin-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-partin-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-partin-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-partin-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-partin-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-partin-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-partin-page .table-wrap { overflow-x:auto; }
.dms-partin-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-partin-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-partin-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-partin-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-partin-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-partin-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-partin-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-partin-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-partin-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-partin-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-partin-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-partin-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-partin-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-partin-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-partin-page .badge.amber .dot { background:var(--amber-500); }
.dms-partin-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-partin-page .badge.violet .dot { background:var(--brand-500); }
.dms-partin-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-partin-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-partin-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-partin-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-partin-page .pagination-container :deep(.el-pagination .btn-prev), .dms-partin-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-partin-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-partin-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-partin-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-partin-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-partin-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-partin-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-partin-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-partin-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-partin-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-partin-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-partin-page .toolbar { flex-wrap:wrap; gap:10px; } }

.status-help-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}
.status-help-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}
.status-help-content h4:first-child {
  margin-top: 0;
}
.status-help-content .status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}
.status-help-content .flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-help-content .flow-arrow {
  color: #909399;
  font-size: 16px;
}
.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}
.highlight-success { background-color: #f0f9ff; border-color: #b3e19d; }
.highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
</style>
