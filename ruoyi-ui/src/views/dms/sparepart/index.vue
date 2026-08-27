<template>
  <div class="app-container dms-sparepart-page">
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
          <label>备件编号</label>
          <div class="control">
            <el-input v-model="queryParams.partCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>备件名称</label>
          <div class="control">
            <el-input v-model="queryParams.partName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>备件类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.partType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in dms_part_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>规格型号</label>
          <div class="control">
            <el-input v-model="queryParams.specModel" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplier" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
      <!-- Type Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTypeTab === 'all' }" @click="handleTypeTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ typeCounts.all || 0 }}</span>
          </button>
          <button v-for="t in dms_part_type" :key="t.value" class="status-tab" :class="[typeTabClass(t.value), { 'is-active': activeTypeTab === t.value }]" @click="handleTypeTabClick(t.value)">
            <span class="dot"></span><span>{{ t.label }}</span><span class="count">{{ typeCounts[t.value] || 0 }}</span>
          </button>
        </div>
      </div>
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:sparepart:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:sparepart:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:sparepart:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_sparepart_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="备件编号" prop="partCode" key="partCode" :width="colWidth('partCode', 140)" resizable v-if="columns.partCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.partCode }}</span></template>
          </el-table-column>
          <el-table-column label="备件名称" prop="partName" key="partName" :width="colWidth('partName', 200)" resizable show-overflow-tooltip v-if="columns.partName.visible" />
          <el-table-column label="备件类别" prop="partType" key="partType" :width="colWidth('partType', 100)" resizable align="center" v-if="columns.partType.visible">
            <template #default="scope">
              <span class="badge violet">{{ partTypeLabel(scope.row.partType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 150)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope">
              <span class="badge amber">{{ unitLabel(scope.row.unit) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplier" key="supplier" :width="colWidth('supplier', 140)" resizable show-overflow-tooltip v-if="columns.supplier.visible" />
          <el-table-column label="库存下限" prop="stockMin" key="stockMin" :width="colWidth('stockMin', 90)" resizable align="center" v-if="columns.stockMin.visible" />
          <el-table-column label="库存上限" prop="stockMax" key="stockMax" :width="colWidth('stockMax', 90)" resizable align="center" v-if="columns.stockMax.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" key="remark" :width="colWidth('remark', 180)" resizable show-overflow-tooltip v-if="columns.remark.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:sparepart:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:sparepart:edit']">修改</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="816px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="sparepartRef" :model="form" :rules="rules" label-width="100px" :disabled="formDisabled">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="12">
            <el-form-item label="备件编号" prop="partCode">
              <el-input v-model="form.partCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="备件名称" prop="partName"><el-input v-model="form.partName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="备件类别" prop="partType">
              <el-select v-model="form.partType" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in dms_part_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择" style="width: 100%">
                <el-option v-for="dict in wms_unit" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplier">
              <el-input v-model="form.supplier" readonly placeholder="请选择供应商" style="width: 100%" @click="openSupplierPicker">
                <template v-if="form.supplier" #append>
                  <el-button icon="CircleClose" @click.stop="clearSupplier" />
                </template>
                <template v-else #append>
                  <el-button icon="Search" @click="openSupplierPicker" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>库存控制</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <el-row>
              <el-col :span="12">
                <el-form-item label="库存下限" prop="stockMin">
                  <el-input-number v-model="form.stockMin" :min="0" :precision="2" :step="1" controls-position="right" placeholder="下限" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="库存上限" prop="stockMax">
                  <el-input-number v-model="form.stockMax" :min="0" :precision="2" :step="1" controls-position="right" placeholder="上限" style="width: 100%" />
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
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" v-if="!formDisabled">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 备件详情弹窗 -->
    <DmsSparePartViewDrawer ref="sparepartViewRef" />

    <!-- 供应商选择弹框 -->
    <supplier-picker ref="supplierPickerRef" title="选择供应商" @confirm="onSupplierPickerConfirm" />
  </div>
</template>

<script setup name="DmsSparePart">
import { listSparepart, getSparepart, addSparepart, updateSparepart, delSparepart } from '@/api/dms/sparepart'
import { fetchAllPages, downloadCsv } from '@/utils/csvExport'
import SupplierPicker from '@/components/SupplierPicker/index.vue'
import DmsSparePartViewDrawer from './view.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Edit, Delete, Download, ArrowDown } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_sparepart_index')
const { wms_unit, dms_part_type } = proxy.useDict('wms_unit', 'dms_part_type')

const list = ref([])
const supplierPickerRef = ref(null)
const sparepartViewRef = ref(null)
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const formDisabled = ref(false)
const activeTypeTab = ref('all')
const typeCounts = ref({ all: 0 })

const defaultColumns = {
  partCode: { label: '备件编号', visible: true },
  partName: { label: '备件名称', visible: true },
  partType: { label: '备件类别', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  supplier: { label: '供应商', visible: true },
  stockMin: { label: '库存下限', visible: true },
  stockMax: { label: '库存上限', visible: true },
  status: { label: '状态', visible: true },
  remark: { label: '备注', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_sparepart_columns')
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
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.partCode) count++
  if (queryParams.value.partName) count++
  if (queryParams.value.partType) count++
  if (queryParams.value.status) count++
  if (queryParams.value.specModel) count++
  if (queryParams.value.supplier) count++
  return count
})
function partTypeLabel(val) { const item = dms_part_type.value.find(d => d.value == val); return item ? item.label : '-' }
function unitLabel(val) { const item = wms_unit.value.find(d => d.value == val); return item ? item.label : '-' }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, partCode: undefined, partName: undefined, partType: undefined, status: undefined, specModel: undefined, supplier: undefined },
  rules: {
    partCode: [{ required: false }],
    partName: [{ required: true, message: '备件名称不能为空', trigger: 'blur' }],
    partType: [{ required: true, message: '备件类别不能为空', trigger: 'change' }],
    unit: [{ required: true, message: '单位不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listSparepart(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }).catch(() => { loading.value = false })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); loadTypeCounts() }
function resetQuery() { queryParams.value.partCode = undefined; queryParams.value.partName = undefined; queryParams.value.partType = undefined; queryParams.value.status = undefined; queryParams.value.specModel = undefined; queryParams.value.supplier = undefined; activeTypeTab.value = 'all'; handleQuery() }
function handleTypeTabClick(type) { activeTypeTab.value = type; queryParams.value.partType = type === 'all' ? undefined : type; handleQuery() }
function typeTabClass(value) { const map = { '0': 'tab-mechanical', '1': 'tab-electrical', '2': 'tab-hydraulic', '3': 'tab-instrument', '4': 'tab-consumable', '5': 'tab-other' }; return map[value] || '' }
function loadTypeCounts() {
  listSparepart({ pageNum: 1, pageSize: 9999 }).then(res => {
    const counts = { all: res.total };
    (dms_part_type.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.partType] !== undefined) counts[r.partType]++ });
    typeCounts.value = counts
  }).catch(() => {})
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.partId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = {
    partCode: undefined, partName: undefined, partType: undefined, specModel: undefined, unit: undefined,
    supplierId: undefined, supplier: undefined, stockMin: undefined, stockMax: undefined, status: '0', remark: undefined
  }
  proxy.resetForm('sparepartRef')
}
function handleAdd() {
  reset()
  formDisabled.value = false
  open.value = true
  title.value = '添加备件'
}
function handleUpdate(row) {
  reset()
  formDisabled.value = false
  getSparepart(row.partId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改备件' })
}
/** 查看备件详情 */
function handleView(row) {
  sparepartViewRef.value?.open(row.partId)
}
function submitForm() {
  proxy.$refs['sparepartRef'].validate(valid => {
    if (valid) {
      if (form.value.partId != undefined) { updateSparepart(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addSparepart(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const partIds = row.partId || ids.value; proxy.$modal.confirm('确认删除编号为"' + partIds + '"的数据？').then(() => delSparepart(partIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
/** 导出备件台账（与列表口径一致：含筛选条件，导出全部页数据） */
async function handleExport() {
  const rows = await fetchAllPages(listSparepart, queryParams.value)
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['备件编号', '备件名称', '备件类别', '规格型号', '单位', '供应商', '库存下限', '库存上限', '状态', '备注']
  downloadCsv(`sparepart_${new Date().getTime()}`, headers, rows.map(i => [
    i.partCode, i.partName, partTypeLabel(i.partType), i.specModel, unitLabel(i.unit), i.supplier || '',
    i.stockMin != null ? i.stockMin : '', i.stockMax != null ? i.stockMax : '',
    i.status === '0' ? '正常' : '停用', i.remark || ''
  ]))
}
function cancel() { open.value = false; reset() }

/** 打开供应商选择弹窗 */
function openSupplierPicker() {
  supplierPickerRef.value.open(form.value.supplierId)
}
/** 供应商选择确认回调 */
function onSupplierPickerConfirm(supplier) {
  form.value.supplierId = supplier.supplierId
  form.value.supplier = supplier.supplierName
}
/** 清除供应商 */
function clearSupplier() {
  form.value.supplierId = undefined
  form.value.supplier = undefined
}

getList()
loadTypeCounts()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-sparepart-page {
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
.dms-sparepart-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Type Tabs ===== */
.dms-sparepart-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.dms-sparepart-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; }
.dms-sparepart-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.dms-sparepart-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.dms-sparepart-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.dms-sparepart-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.dms-sparepart-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.dms-sparepart-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.dms-sparepart-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 备件类别颜色 */
.dms-sparepart-page .status-tab.tab-mechanical .dot { background:var(--amber-500); }
.dms-sparepart-page .status-tab.tab-mechanical .count { background:var(--amber-50); color:var(--amber-700); }
.dms-sparepart-page .status-tab.is-active.tab-mechanical .count { background:var(--amber-500); color:#fff; }
.dms-sparepart-page .status-tab.tab-electrical .dot { background:var(--blue-500); }
.dms-sparepart-page .status-tab.tab-electrical .count { background:var(--blue-50); color:var(--blue-700); }
.dms-sparepart-page .status-tab.is-active.tab-electrical .count { background:var(--blue-500); color:#fff; }
.dms-sparepart-page .status-tab.tab-hydraulic .dot { background:var(--green-500); }
.dms-sparepart-page .status-tab.tab-hydraulic .count { background:var(--green-50); color:var(--green-700); }
.dms-sparepart-page .status-tab.is-active.tab-hydraulic .count { background:var(--green-500); color:#fff; }
.dms-sparepart-page .status-tab.tab-instrument .dot { background:#8b5cf6; }
.dms-sparepart-page .status-tab.tab-instrument .count { background:var(--violet-50); color:#7c3aed; }
.dms-sparepart-page .status-tab.is-active.tab-instrument .count { background:#8b5cf6; color:#fff; }
.dms-sparepart-page .status-tab.tab-consumable .dot { background:#06b6d4; }
.dms-sparepart-page .status-tab.tab-consumable .count { background:#ecfeff; color:#0e7490; }
.dms-sparepart-page .status-tab.is-active.tab-consumable .count { background:#06b6d4; color:#fff; }
.dms-sparepart-page .status-tab.tab-other .dot { background:var(--ink-400); }
.dms-sparepart-page .status-tab.tab-other .count { background:var(--ink-100); color:var(--ink-500); }
.dms-sparepart-page .status-tab.is-active.tab-other .count { background:var(--ink-400); color:#fff; }
.dms-sparepart-page .filter-card { padding:14px 20px 16px; }
.dms-sparepart-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-sparepart-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-sparepart-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-sparepart-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-sparepart-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-sparepart-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-sparepart-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-sparepart-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-sparepart-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-sparepart-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-sparepart-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.dms-sparepart-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-sparepart-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-sparepart-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-sparepart-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-sparepart-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.dms-sparepart-page .field .control :deep(.el-select) { width:100%; }
.dms-sparepart-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-sparepart-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-sparepart-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.dms-sparepart-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-sparepart-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-sparepart-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-sparepart-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-sparepart-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-sparepart-page .btn-soft .el-icon { font-size:14px; }
.dms-sparepart-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-sparepart-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-sparepart-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-sparepart-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-sparepart-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-sparepart-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-sparepart-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }
.dms-sparepart-page .table-wrap { overflow-x:auto; }
.dms-sparepart-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-sparepart-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-sparepart-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-sparepart-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-sparepart-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-sparepart-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-sparepart-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-sparepart-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-sparepart-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-sparepart-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-sparepart-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.dms-sparepart-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-sparepart-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-sparepart-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-sparepart-page .badge.amber .dot { background:var(--amber-500); }
.dms-sparepart-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-sparepart-page .badge.violet .dot { background:var(--brand-500); }
.dms-sparepart-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-sparepart-page .badge.green .dot { background:var(--green-500); }
.dms-sparepart-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-sparepart-page .badge.gray .dot { background:var(--ink-400); }
.dms-sparepart-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-sparepart-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-sparepart-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-sparepart-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-sparepart-page .pagination-container :deep(.el-pagination .btn-prev), .dms-sparepart-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-sparepart-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-sparepart-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-sparepart-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-sparepart-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-sparepart-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-sparepart-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-sparepart-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-sparepart-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-sparepart-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-sparepart-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-sparepart-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
