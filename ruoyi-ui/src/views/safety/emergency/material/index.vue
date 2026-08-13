<template>
  <div class="app-container safety-emergency-material-page">
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
          <label>物资名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物资编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物资类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.materialType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_material_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:emergency:material:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:emergency:material:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:emergency:material:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_emergency_material_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="materialList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="物资编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 130)" resizable v-if="columns.materialCode.visible" />
          <el-table-column label="物资名称" prop="materialName" key="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="物资类别" prop="materialType" key="materialType" :width="colWidth('materialType', 110)" resizable align="center" v-if="columns.materialType.visible">
            <template #default="scope"><span class="badge violet">{{ materialTypeLabel(scope.row.materialType) }}</span></template>
          </el-table-column>
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 150)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="数量" prop="quantity" key="quantity" :width="colWidth('quantity', 80)" resizable align="center" v-if="columns.quantity.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 70)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge violet">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="存放位置" prop="storageLocation" key="storageLocation" :width="colWidth('storageLocation', 150)" resizable show-overflow-tooltip v-if="columns.storageLocation.visible" />
          <el-table-column label="管理人" prop="personName" key="personName" :width="colWidth('personName', 100)" resizable v-if="columns.personName.visible" />
          <el-table-column label="有效期至" prop="effectiveDate" key="effectiveDate" :width="colWidth('effectiveDate', 120)" resizable align="center" sortable="custom" v-if="columns.effectiveDate.visible">
            <template #default="scope">
              <span class="badge" :class="isExpired(scope.row) ? 'red' : 'gray'">
                <span class="dot"></span>{{ scope.row.effectiveDate || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:emergency:material:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:emergency:material:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:emergency:material:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="materialRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>物资信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物资编码" prop="materialCode"><el-input v-model="form.materialCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物资名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入物资名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物资类别" prop="materialType"><el-select v-model="form.materialType" clearable placeholder="请选择物资类别" style="width: 100%"><el-option v-for="d in safety_material_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" placeholder="请输入规格型号" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>储存信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" clearable placeholder="请选择单位" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="有效期至" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="存放位置" prop="storageLocation"><el-input v-model="form.storageLocation" placeholder="请输入存放位置" /></el-form-item>
              <el-form-item label="管理人" prop="personId">
                <el-input v-model="form.personName" readonly placeholder="请选择管理人" style="width: 100%" @click="openUserPicker">
                  <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                  <template #suffix><el-icon v-if="form.personName" class="clear-icon" @click.stop="clearPerson"><CircleClose /></el-icon></template>
                </el-input>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">应急物资详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.materialCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.materialCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg></span>物资信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物资编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物资名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物资类别</span><div class="rd-value"><dict-tag :options="safety_material_type" :value="viewData.materialType" /></div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.quantity != null || viewData.unit || viewData.effectiveDate || viewData.storageLocation || viewData.personName">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>储存信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">数量</span><div class="rd-value">{{ viewData.quantity || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
              <div class="rd-item"><span class="rd-label">有效期至</span><div class="rd-value">{{ viewData.effectiveDate || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">存放位置</span><div class="rd-value">{{ viewData.storageLocation || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">管理人</span><div class="rd-value">{{ viewData.personName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>


    <!-- 管理人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择管理人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="SafetyEmergencyMaterial">
import { listEmergencyMaterial, getEmergencyMaterial, addEmergencyMaterial, updateEmergencyMaterial, delEmergencyMaterial } from '@/api/safety/emergencyMaterial'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_emergency_material_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])
const { safety_material_type, wms_unit } = proxy.useDict('safety_material_type', 'wms_unit')

const materialList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})

const default_columns = {
  materialCode: { label: '物资编码', visible: true },
  materialName: { label: '物资名称', visible: true },
  materialType: { label: '物资类别', visible: true },
  specModel: { label: '规格型号', visible: true },
  quantity: { label: '数量', visible: true },
  unit: { label: '单位', visible: true },
  storageLocation: { label: '存放位置', visible: true },
  personName: { label: '管理人', visible: true },
  effectiveDate: { label: '有效期至', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_emergency_material_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, materialName: undefined, materialCode: undefined, materialType: undefined, status: undefined, params: {} },
  rules: {
    materialName: [{ required: true, message: '物资名称不能为空', trigger: 'blur' }],
    materialType: [{ required: true, message: '物资类别不能为空', trigger: 'change' }],
    quantity: [{ required: true, message: '数量不能为空', trigger: 'blur' }],
    unit: [{ required: true, message: '单位不能为空', trigger: 'change' }],
    storageLocation: [{ required: true, message: '存放位置不能为空', trigger: 'blur' }],
    personId: [{ required: true, message: '管理人不能为空', trigger: 'change' }],
    effectiveDate: [{ required: true, message: '有效期至不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialName) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialType) count++
  if (queryParams.value.status) count++
  return count
})

function getList() { loading.value = true; listEmergencyMaterial(queryParams.value).then(response => { materialList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.materialName = undefined; queryParams.value.materialCode = undefined; queryParams.value.materialType = undefined; queryParams.value.status = undefined; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.materialId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = '添加应急物资' }
function handleView(row) { const id = row.materialId || ids.value[0]; getEmergencyMaterial(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getEmergencyMaterial(row.materialId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.storageLocation && !response.data.personName; collapsedCards.c2 = !response.data.remark; open.value = true; title.value = '修改应急物资' }) }
function submitForm() {
  proxy.$refs['materialRef'].validate(valid => {
    if (valid) {
      if (form.value.materialId != undefined) { updateEmergencyMaterial(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addEmergencyMaterial(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const materialIds = row.materialId || ids.value; proxy.$modal.confirm('是否确认删除应急物资？').then(function() { return delEmergencyMaterial(materialIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/emergency/material/export', { ...queryParams.value }, `emergency_material_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { materialId: undefined, materialCode: undefined, materialName: undefined, materialType: undefined, specModel: undefined, quantity: undefined, unit: undefined, storageLocation: undefined, personId: undefined, personName: undefined, effectiveDate: undefined, remark: undefined }
  proxy.resetForm('materialRef')
}

/** 打开管理人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.personId) }
/** 管理人选择确认回调 */
function onUserPickerConfirm(user) { form.value.personId = user.userId; form.value.personName = user.nickName }
/** 清除管理人 */
function clearPerson() { form.value.personId = undefined; form.value.personName = undefined }

function isExpired(row) {
  if (!row.effectiveDate) return false
  return new Date(row.effectiveDate) < new Date()
}

function materialTypeLabel(type) { const item = safety_material_type.value.find(d => d.value == type); return item ? item.label : '-' }
function unitLabel(unit) { const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '-' }

getList()
</script>

<style scoped>
.safety-emergency-material-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-emergency-material-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-emergency-material-page .filter-card { padding:14px 20px 16px; }
.safety-emergency-material-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-emergency-material-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-emergency-material-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-emergency-material-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-emergency-material-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-emergency-material-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-emergency-material-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-emergency-material-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-emergency-material-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-emergency-material-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-emergency-material-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-emergency-material-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-emergency-material-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-emergency-material-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-emergency-material-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-emergency-material-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-emergency-material-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-emergency-material-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-emergency-material-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-emergency-material-page .field .control :deep(.el-select) { width:100%; }
.safety-emergency-material-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-emergency-material-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-emergency-material-page .toolbar .left, .safety-emergency-material-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-emergency-material-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-emergency-material-page .table-wrap { overflow-x:auto; }
.safety-emergency-material-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-emergency-material-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-emergency-material-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-emergency-material-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-emergency-material-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-emergency-material-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-emergency-material-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-emergency-material-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-emergency-material-page .badge.red .dot { background:var(--red-500); }
.safety-emergency-material-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-emergency-material-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-emergency-material-page .badge.gray .dot { background:var(--ink-400); }
.safety-emergency-material-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-emergency-material-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-emergency-material-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
</style>
