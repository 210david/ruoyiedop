<template>
  <div class="app-container safety-equipment-page">
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
          <label>设备名称</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>设备编号</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>设备类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.equipmentCategory" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_equipment_category" :key="dict.value" :label="dict.label" :value="dict.value" />
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
          <label>特种子类</label>
          <div class="control is-select">
            <el-select v-model="queryParams.specialSubType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_special_equipment" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>所属区域</label>
          <div class="control">
            <el-input v-model="queryParams.areaName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>责任人</label>
          <div class="control">
            <el-input v-model="queryParams.personName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:equipment:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:equipment:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:equipment:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_equipment_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="equipmentList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="设备编号" prop="equipmentCode" key="equipmentCode" :width="colWidth('equipmentCode', 130)" resizable v-if="columns.equipmentCode.visible" />
          <el-table-column label="设备名称" prop="equipmentName" key="equipmentName" :width="colWidth('equipmentName', 180)" resizable show-overflow-tooltip v-if="columns.equipmentName.visible" />
          <el-table-column label="型号规格" prop="specModel" key="specModel" :width="colWidth('specModel', 160)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="设备类别" prop="equipmentCategory" key="equipmentCategory" :width="colWidth('equipmentCategory', 110)" resizable align="center" v-if="columns.equipmentCategory.visible">
            <template #default="scope"><span class="badge violet">{{ equipmentCategoryLabel(scope.row.equipmentCategory) }}</span></template>
          </el-table-column>
          <el-table-column label="特种设备子类" prop="specialSubType" key="specialSubType" :width="colWidth('specialSubType', 130)" resizable align="center" v-if="columns.specialSubType.visible">
            <template #default="scope"><span class="badge blue">{{ specialSubTypeLabel(scope.row.specialSubType) }}</span></template>
          </el-table-column>
          <el-table-column label="所属区域" prop="areaName" key="areaName" :width="colWidth('areaName', 130)" resizable show-overflow-tooltip v-if="columns.areaName.visible" />
          <el-table-column label="使用登记证号" prop="regCertNo" key="regCertNo" :width="colWidth('regCertNo', 140)" resizable v-if="columns.regCertNo.visible" />
          <el-table-column label="检验有效期至" prop="inspectExpire" key="inspectExpire" :width="colWidth('inspectExpire', 140)" resizable align="center" sortable="custom" v-if="columns.inspectExpire.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.remindStatus === '1' ? 'red' : 'green'">
                <span class="dot"></span>{{ scope.row.inspectExpire }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="责任人" prop="personName" key="personName" :width="colWidth('personName', 110)" resizable v-if="columns.personName.visible" />
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:equipment:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:equipment:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:equipment:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="equipmentRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="8" rx="2"/><rect x="2" y="14" width="20" height="8" rx="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg></span>特种设备信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="设备编号" prop="equipmentCode"><el-input v-model="form.equipmentCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="设备名称" prop="equipmentName"><el-input v-model="form.equipmentName" placeholder="请输入设备名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="型号规格" prop="specModel"><el-input v-model="form.specModel" placeholder="请输入型号规格" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="所属区域" prop="areaId"><el-tree-select v-model="form.areaId" :data="areaOptions" :props="{ value: 'areaId', label: 'areaName', children: 'children' }" value-key="areaId" placeholder="请选择所属区域" check-strictly style="width: 100%" @change="onAreaChange" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg></span>分类与登记</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="设备类别" prop="equipmentCategory"><el-select v-model="form.equipmentCategory" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_equipment_category" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="特种子类" prop="specialSubType"><el-select v-model="form.specialSubType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="dict in safety_special_equipment" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="登记证号" prop="regCertNo"><el-input v-model="form.regCertNo" placeholder="请输入使用登记证号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检验有效期" prop="inspectExpire"><el-date-picker v-model="form.inspectExpire" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="责任人" prop="personId">
                <el-input v-model="form.personName" readonly placeholder="请选择责任人" style="width: 100%" @click="openUserPicker">
                  <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                  <template #suffix><el-icon v-if="form.personName" class="clear-icon" @click.stop="clearPerson"><CircleClose /></el-icon></template>
                </el-input>
              </el-form-item>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">特种设备详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.equipmentCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.equipmentCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="8" rx="2"/><rect x="2" y="14" width="20" height="8" rx="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg></span>特种设备信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">设备编号</span><div class="rd-value">{{ viewData.equipmentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ viewData.equipmentName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">型号规格</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属区域</span><div class="rd-value">{{ viewData.areaName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.equipmentCategory || viewData.specialSubType || viewData.regCertNo || viewData.inspectExpire">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg></span>分类与登记</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">设备类别</span><div class="rd-value"><dict-tag :options="safety_equipment_category" :value="viewData.equipmentCategory" /></div></div>
              <div class="rd-item"><span class="rd-label">特种子类</span><div class="rd-value"><dict-tag :options="safety_special_equipment" :value="viewData.specialSubType" /></div></div>
              <div class="rd-item"><span class="rd-label">登记证号</span><div class="rd-value">{{ viewData.regCertNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">检验有效期</span><div class="rd-value"><span class="badge" :class="viewData.remindStatus === '1' ? 'red' : 'green'"><span class="dot"></span>{{ viewData.inspectExpire || '-' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.personName || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ viewData.personName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 责任人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择责任人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="SafetyEquipment">
import { listEquipment, getEquipment, addEquipment, updateEquipment, delEquipment } from '@/api/safety/equipment'
import { listArea } from '@/api/safety/area'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_equipment_category, safety_special_equipment } = proxy.useDict('safety_equipment_category', 'safety_special_equipment')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_equipment_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const equipmentList = ref([])
const areaOptions = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  equipmentCode: { label: '设备编号', visible: true },
  equipmentName: { label: '设备名称', visible: true },
  specModel: { label: '型号规格', visible: true },
  equipmentCategory: { label: '设备类别', visible: true },
  specialSubType: { label: '特种设备子类', visible: true },
  areaName: { label: '所属区域', visible: true },
  regCertNo: { label: '使用登记证号', visible: true },
  inspectExpire: { label: '检验有效期至', visible: true },
  personName: { label: '责任人', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_equipment_columns')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, equipmentName: undefined, equipmentCode: undefined, equipmentCategory: undefined, specialSubType: undefined, status: undefined, areaName: undefined, personName: undefined, params: {} },
  rules: {
    equipmentName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
    equipmentCategory: [{ required: true, message: '设备类别不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.equipmentName) count++
  if (queryParams.value.equipmentCode) count++
  if (queryParams.value.equipmentCategory) count++
  if (queryParams.value.status) count++
  if (queryParams.value.specialSubType) count++
  if (queryParams.value.areaName) count++
  if (queryParams.value.personName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listEquipment(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { equipmentList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.equipmentName = undefined; queryParams.value.equipmentCode = undefined; queryParams.value.equipmentCategory = undefined; queryParams.value.specialSubType = undefined; queryParams.value.status = undefined; queryParams.value.areaName = undefined; queryParams.value.personName = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.equipmentId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = '添加特种设备' }
function handleView(row) { const equipmentId = row.equipmentId || ids.value[0]; getEquipment(equipmentId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getEquipment(row.equipmentId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.equipmentCategory && !response.data.regCertNo && !response.data.inspectExpire; collapsedCards.c2 = !response.data.personName && !response.data.remark; open.value = true; title.value = '修改特种设备' }) }
function submitForm() {
  proxy.$refs['equipmentRef'].validate(valid => {
    if (valid) {
      if (form.value.equipmentId != undefined) { updateEquipment(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addEquipment(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const equipmentIds = row.equipmentId || ids.value; proxy.$modal.confirm('是否确认删除特种设备？').then(function() { return delEquipment(equipmentIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/equipment/export', { ...queryParams.value }, `equipment_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined, specModel: undefined, areaId: undefined, areaName: undefined, equipmentCategory: undefined, specialSubType: undefined, regCertNo: undefined, inspectExpire: undefined, personId: undefined, personName: undefined, remark: undefined }
  proxy.resetForm('equipmentRef')
}
/** 打开责任人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.personId) }
/** 责任人选择确认回调 */
function onUserPickerConfirm(user) { form.value.personId = user.userId; form.value.personName = user.nickName }
/** 清除责任人 */
function clearPerson() { form.value.personId = undefined; form.value.personName = undefined }

function equipmentCategoryLabel(type) { const item = safety_equipment_category.value.find(d => d.value == type); return item ? item.label : '-' }
function specialSubTypeLabel(type) { const item = safety_special_equipment.value.find(d => d.value == type); return item ? item.label : '-' }

/** 加载区域树 */
function getAreaTree() {
  listArea({ pageNum: 1, pageSize: 9999 }).then(response => {
    areaOptions.value = proxy.handleTree(response.rows, 'areaId')
  })
}
/** 区域选择变化时同步区域名称 */
function onAreaChange(areaId) {
  const node = findAreaNode(areaOptions.value, areaId)
  form.value.areaName = node ? node.areaName : undefined
}
/** 递归查找区域节点 */
function findAreaNode(nodes, id) {
  for (const n of nodes) {
    if (n.areaId === id) return n
    if (n.children && n.children.length) {
      const found = findAreaNode(n.children, id)
      if (found) return found
    }
  }
  return null
}

getAreaTree()
getList()
</script>

<style scoped>
.safety-equipment-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-equipment-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-equipment-page .filter-card { padding:14px 20px 16px; }
.safety-equipment-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-equipment-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-equipment-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-equipment-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-equipment-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-equipment-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-equipment-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-equipment-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-equipment-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-equipment-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-equipment-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-equipment-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-equipment-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-equipment-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-equipment-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-equipment-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-equipment-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-equipment-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-equipment-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-equipment-page .field .control :deep(.el-select) { width:100%; }
.safety-equipment-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-equipment-page .field .control :deep(.el-date-editor) { width:100%; }
.safety-equipment-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.safety-equipment-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.safety-equipment-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.safety-equipment-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-equipment-page .toolbar .left, .safety-equipment-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-equipment-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-equipment-page .table-wrap { overflow-x:auto; }
.safety-equipment-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-equipment-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-equipment-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-equipment-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-equipment-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-equipment-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-equipment-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-equipment-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-equipment-page .badge.amber .dot { background:var(--amber-500); }
.safety-equipment-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-equipment-page .badge.blue .dot { background:var(--blue-500); }
.safety-equipment-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-equipment-page .badge.green .dot { background:var(--green-500); }
.safety-equipment-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-equipment-page .badge.red .dot { background:var(--red-500); }
.safety-equipment-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-equipment-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-equipment-page .badge.gray .dot { background:var(--ink-400); }
.safety-equipment-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-equipment-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-equipment-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
</style>
