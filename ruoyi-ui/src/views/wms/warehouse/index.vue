<template>
  <div class="app-container wms-warehouse-page">
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
          <label>编码</label>
          <div class="control">
            <el-input v-model="queryParams.warehouseCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>名称</label>
          <div class="control">
            <el-input v-model="queryParams.warehouseName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>节点类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.nodeType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="仓库" value="1" />
              <el-option label="仓区" value="2" />
              <el-option label="仓位" value="3" />
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
          <label>库区类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.areaType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_area_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>库位类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.locationType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_location_type" :key="d.value" :label="d.label" :value="d.value" />
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
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['wms:warehouse:add']">新增仓库</el-button>
          <button type="button" class="btn-soft is-outline" @click="toggleExpandAll">
            <el-icon><Sort /></el-icon> 展开/折叠
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_warehouse_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-if="refreshTable" v-loading="loading" :data="warehouseList" row-key="warehouseId" :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column prop="warehouseCode" label="编码" key="warehouseCode" :width="colWidth('warehouseCode', 200)" resizable v-if="columns.warehouseCode.visible" />
          <el-table-column prop="warehouseName" label="名称" key="warehouseName" :show-overflow-tooltip="true" v-if="columns.warehouseName.visible" />
          <el-table-column prop="nodeType" label="类型" key="nodeType" :width="colWidth('nodeType', 90)" resizable align="center" v-if="columns.nodeType.visible">
            <template #default="scope">
              <span class="badge violet" v-if="scope.row.nodeType === '1'">仓库</span>
              <span class="badge blue" v-else-if="scope.row.nodeType === '2'">仓区</span>
              <span class="badge amber" v-else-if="scope.row.nodeType === '3'">仓位</span>
            </template>
          </el-table-column>
          <el-table-column prop="areaType" label="库区类型" key="areaType" :width="colWidth('areaType', 100)" resizable align="center" v-if="columns.areaType.visible">
            <template #default="scope">
              <span class="badge blue" v-if="scope.row.nodeType === '2'">{{ areaTypeLabel(scope.row.areaType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="locationType" label="库位类型" key="locationType" :width="colWidth('locationType', 100)" resizable align="center" v-if="columns.locationType.visible">
            <template #default="scope">
              <span class="badge blue" v-if="scope.row.nodeType === '3'">{{ locationTypeLabel(scope.row.locationType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="capacity" label="容量" key="capacity" :width="colWidth('capacity', 90)" resizable align="center" class-name="col-num" v-if="columns.capacity.visible">
            <template #default="scope">
              <span v-if="scope.row.nodeType === '3'">{{ scope.row.capacity }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="locationStatus" label="库位状态" key="locationStatus" :width="colWidth('locationStatus', 90)" resizable align="center" v-if="columns.locationStatus.visible">
            <template #default="scope">
              <span class="badge" :class="locationStatusBadgeClass(scope.row.locationStatus)" v-if="scope.row.nodeType === '3'">
                <span class="dot"></span>{{ locationStatusText(scope.row.locationStatus) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="managerName" label="负责人" key="managerName" :width="colWidth('managerName', 120)" resizable align="center" v-if="columns.managerName.visible">
            <template #default="scope">
              <span v-if="scope.row.nodeType === '1'">{{ scope.row.managerName || '-' }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="deptName" label="管理部门" key="deptName" :width="colWidth('deptName', 120)" resizable align="center" v-if="columns.deptName.visible">
            <template #default="scope">
              <span v-if="scope.row.nodeType === '1'">{{ scope.row.deptName || '-' }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button v-if="scope.row.nodeType !== '3'" link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['wms:warehouse:add']">新增</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:warehouse:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:warehouse:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="816px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="warehouseRef" :model="form" :rules="rules" label-width="100px">
                <!-- 基本信息 -->
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c4">
        <el-row>
          <el-col :span="12">
            <el-form-item label="上级节点">
              <el-input :value="form.parentName || '无（顶级仓库）'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="节点类型" prop="nodeType">
              <el-radio-group v-model="form.nodeType" disabled>
                <el-radio value="1">仓库</el-radio>
                <el-radio value="2">仓区</el-radio>
                <el-radio value="3">仓位</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="编码" prop="warehouseCode">
              <el-input v-model="form.warehouseCode" placeholder="保存后自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="warehouseName">
              <el-input v-model="form.warehouseName" :placeholder="form.nodeType === '1' ? '仓库名称' : form.nodeType === '2' ? '仓区名称' : '仓位名称'" />
            </el-form-item>
          </el-col>
                </el-row>
          </div>
        </section>

        <!-- 仓库级字段 -->
        <template v-if="form.nodeType === '1'">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg></span>仓库信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row>
                <el-col :span="12">
                  <el-form-item label="负责人" prop="managerId">
                    <el-input v-model="form.managerName" readonly placeholder="请选择负责人" style="width: 100%" @click="openUserPicker">
                      <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                      <template #suffix><el-icon v-if="form.managerName" class="clear-icon" @click.stop="clearManager"><CircleClose /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="管理部门" prop="deptId">
                    <el-input v-model="form.deptName" readonly placeholder="选择负责人后自动带出，或手动选择" style="width: 100%" @click="openDeptPicker">
                      <template #append><el-button icon="Search" @click="openDeptPicker" /></template>
                      <template #suffix><el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="仓库地址" prop="address">
                <el-input v-model="form.address" type="textarea" placeholder="请输入仓库地址" />
              </el-form-item>
            </div>
          </section>
        </template>

        <!-- 仓区级字段 -->
        <template v-if="form.nodeType === '2'">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg></span>仓区信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row>
                <el-col :span="12">
                  <el-form-item label="库区类型" prop="areaType">
                    <el-select v-model="form.areaType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in wms_area_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </template>

        <!-- 仓位级字段 -->
        <template v-if="form.nodeType === '3'">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></span>仓位信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row>
                <el-col :span="12">
                  <el-form-item label="库位类型" prop="locationType">
                    <el-select v-model="form.locationType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in wms_location_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="库位容量" prop="capacity">
                    <el-input-number v-model="form.capacity" :precision="2" :min="0" controls-position="right" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </template>

        <!-- 通用字段 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
            <el-row>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio value="0">正常</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <user-picker ref="userPickerRef" title="选择仓库负责人" @confirm="onUserPickerConfirm" />
    <dept-picker ref="deptPickerRef" title="选择管理部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />
  </div>
</template>

<script setup name="WmsWarehouse">
import { listWarehouseTree, getWarehouse, addWarehouse, updateWarehouse, delWarehouse } from '@/api/wms/warehouse'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c4","c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_warehouse_index')
const { wms_area_type, wms_location_type } = proxy.useDict('wms_area_type', 'wms_location_type')

const warehouseList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref('')
const isExpandAll = ref(true)
const refreshTable = ref(true)
const showAdvanced = ref(false)

const defaultColumns = {
  warehouseCode: { label: '编码', visible: true },
  warehouseName: { label: '名称', visible: true },
  nodeType: { label: '类型', visible: true },
  areaType: { label: '库区类型', visible: true },
  locationType: { label: '库位类型', visible: true },
  capacity: { label: '容量', visible: true },
  locationStatus: { label: '库位状态', visible: true },
  managerName: { label: '负责人', visible: true },
  deptName: { label: '管理部门', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('wms_warehouse_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.warehouseCode) count++
  if (queryParams.value.warehouseName) count++
  if (queryParams.value.nodeType) count++
  if (queryParams.value.areaType) count++
  if (queryParams.value.locationType) count++
  if (queryParams.value.status) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: {
    warehouseCode: undefined,
    warehouseName: undefined,
    nodeType: undefined,
    areaType: undefined,
    locationType: undefined,
    status: undefined
  },
  rules: {
    nodeType: [{ required: true, message: '请选择节点类型', trigger: 'change' }],
    warehouseName: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listWarehouseTree(queryParams.value).then(response => {
    warehouseList.value = proxy.handleTree(response.data, 'warehouseId', 'parentId')
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryParams.value.warehouseCode = undefined
  queryParams.value.warehouseName = undefined
  queryParams.value.nodeType = undefined
  queryParams.value.areaType = undefined
  queryParams.value.locationType = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

function locationStatusBadgeClass(status) {
  if (status === '0') return 'green'
  if (status === '1') return 'amber'
  if (status === '2') return 'red'
  return 'gray'
}

function locationStatusText(status) {
  if (status === '0') return '空闲'
  if (status === '1') return '占用'
  if (status === '2') return '锁定'
  return '-'
}
function areaTypeLabel(type) {
  const item = wms_area_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}
function locationTypeLabel(type) {
  const item = wms_location_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

function reset() {
  form.value = {
    warehouseId: undefined,
    parentId: 0,
    parentName: undefined,
    warehouseCode: undefined,
    warehouseName: undefined,
    nodeType: '1',
    deptId: undefined,
    deptName: undefined,
    managerId: undefined,
    managerName: undefined,
    address: undefined,
    areaType: '0',
    locationType: '0',
    capacity: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('warehouseRef')
}

function handleAdd(row) {
  reset()
  if (row && row.warehouseId) {
    form.value.parentId = row.warehouseId
    form.value.parentName = row.warehouseName
    // 根据父节点类型自动设置子节点类型
    if (row.nodeType === '1') {
      form.value.nodeType = '2'
    } else if (row.nodeType === '2') {
      form.value.nodeType = '3'
    }
  } else {
    form.value.parentId = 0
    form.value.parentName = '无（顶级仓库）'
    form.value.nodeType = '1'
  }
  open.value = true
  title.value = '新增' + (form.value.nodeType === '1' ? '仓库' : form.value.nodeType === '2' ? '仓区' : '仓位')
}

function handleUpdate(row) {
  reset()
  getWarehouse(row.warehouseId).then(response => {
    form.value = response.data
    // 查找父节点名称
    if (form.value.parentId && form.value.parentId !== 0) {
      findParentName(warehouseList.value, form.value.parentId)
    } else {
      form.value.parentName = '无（顶级仓库）'
    }
    open.value = true
    title.value = '修改' + (form.value.nodeType === '1' ? '仓库' : form.value.nodeType === '2' ? '仓区' : '仓位')
  })
}

function findParentName(list, parentId) {
  for (let item of list) {
    if (item.warehouseId === parentId) {
      form.value.parentName = item.warehouseName
      return true
    }
    if (item.children && item.children.length) {
      if (findParentName(item.children, parentId)) return true
    }
  }
  return false
}

function submitForm() {
  proxy.$refs['warehouseRef'].validate(valid => {
    if (valid) {
      if (form.value.warehouseId != undefined) {
        updateWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addWarehouse(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除"' + row.warehouseName + '"？').then(function () {
    return delWarehouse(row.warehouseId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function cancel() {
  open.value = false
  reset()
}

/** 打开人员选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.managerId)
}
/** 人员选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.managerId = user.userId
  form.value.managerName = user.nickName
  // 选择负责人后自动带出部门信息
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除负责人 */
function clearManager() {
  form.value.managerId = undefined
  form.value.managerName = undefined
}
/** 打开部门选择弹窗 */
function openDeptPicker() {
  proxy.$refs.deptPickerRef.open(form.value.deptId)
}
/** 部门选择确认回调 */
function onDeptPickerConfirm(dept) {
  form.value.deptId = dept.deptId
  form.value.deptName = dept.deptName
}
/** 清除部门 */
function clearDept() {
  form.value.deptId = undefined
  form.value.deptName = undefined
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.wms-warehouse-page {
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

/* ===== Surface Card ===== */
.wms-warehouse-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.wms-warehouse-page .filter-card { padding:14px 20px 16px; }
.wms-warehouse-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.wms-warehouse-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.wms-warehouse-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.wms-warehouse-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.wms-warehouse-page .filter-card .adv-link:hover { color:var(--brand-600); }
.wms-warehouse-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.wms-warehouse-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.wms-warehouse-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.wms-warehouse-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.wms-warehouse-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.wms-warehouse-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.wms-warehouse-page .field { display:flex; flex-direction:column; gap:6px; }
.wms-warehouse-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.wms-warehouse-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.wms-warehouse-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.wms-warehouse-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.wms-warehouse-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.wms-warehouse-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.wms-warehouse-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.wms-warehouse-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.wms-warehouse-page .field .control :deep(.el-select) { width:100%; }
.wms-warehouse-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.wms-warehouse-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.wms-warehouse-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.wms-warehouse-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.wms-warehouse-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.wms-warehouse-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== Buttons ===== */
.wms-warehouse-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.wms-warehouse-page .btn-soft .el-icon { font-size:14px; }
.wms-warehouse-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.wms-warehouse-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }

/* ===== Table ===== */
.wms-warehouse-page .table-wrap { overflow-x:auto; }
.wms-warehouse-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.wms-warehouse-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.wms-warehouse-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.wms-warehouse-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.wms-warehouse-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.wms-warehouse-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.wms-warehouse-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.wms-warehouse-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.wms-warehouse-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.wms-warehouse-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.wms-warehouse-page .app-table :deep(.col-num) { text-align:center; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }

/* ===== Badges ===== */
.wms-warehouse-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.wms-warehouse-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.wms-warehouse-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.wms-warehouse-page .badge.amber .dot { background:var(--amber-500); }
.wms-warehouse-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.wms-warehouse-page .badge.blue .dot { background:var(--blue-500); }
.wms-warehouse-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.wms-warehouse-page .badge.green .dot { background:var(--green-500); }
.wms-warehouse-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.wms-warehouse-page .badge.red .dot { background:var(--red-500); }
.wms-warehouse-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.wms-warehouse-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.wms-warehouse-page .badge.gray .dot { background:var(--ink-400); }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
.wms-warehouse-page :deep(.col-action) { padding: 6px 4px !important; }
.wms-warehouse-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.wms-warehouse-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.wms-warehouse-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.wms-warehouse-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .wms-warehouse-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .wms-warehouse-page .filter-card .filter-bar { grid-template-columns:1fr; } .wms-warehouse-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* ===== Dialog (scoped overrides) ===== */
.wms-warehouse-page .rd-page { max-width: 1008px; }
.wms-warehouse-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.wms-warehouse-page .clear-icon:hover { color: #909399; }
.wms-warehouse-page :deep(.el-input.is-disabled .el-input__inner) { cursor: pointer; }
</style>
