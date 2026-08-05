<template>
  <div class="app-container dms-equipment-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>设备编号</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>设备名称</label>
          <div class="control">
            <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>设备状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.equipmentStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_equipment_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>设备等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.equipmentLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_equipment_level" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:equipment:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:equipment:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:equipment:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['dms:equipment:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['dms:equipment:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_equipment_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="设备编号" prop="equipmentCode" key="equipmentCode" :width="colWidth('equipmentCode', 130)" resizable v-if="columns.equipmentCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.equipmentCode }}</span></template>
          </el-table-column>
          <el-table-column label="资产编号" prop="assetCode" key="assetCode" :width="colWidth('assetCode', 130)" resizable v-if="columns.assetCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.assetCode }}</span></template>
          </el-table-column>
          <el-table-column label="设备名称" prop="equipmentName" key="equipmentName" :width="colWidth('equipmentName', 200)" resizable show-overflow-tooltip v-if="columns.equipmentName.visible" />
          <el-table-column label="分类" prop="categoryName" key="categoryName" :width="colWidth('categoryName', 120)" resizable v-if="columns.categoryName.visible" />
          <el-table-column label="型号" prop="model" key="model" :width="colWidth('model', 120)" resizable v-if="columns.model.visible" />
          <el-table-column label="设备等级" prop="equipmentLevel" key="equipmentLevel" :width="colWidth('equipmentLevel', 100)" resizable align="center" v-if="columns.equipmentLevel.visible">
            <template #default="scope">
              <span class="badge blue">{{ equipmentLevelLabel(scope.row.equipmentLevel) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="使用部门" prop="deptName" key="deptName" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="安装位置" prop="installLocation" key="installLocation" :width="colWidth('installLocation', 120)" resizable show-overflow-tooltip v-if="columns.installLocation.visible" />
          <el-table-column label="责任人" prop="responsibleName" key="responsibleName" :width="colWidth('responsibleName', 100)" resizable v-if="columns.responsibleName.visible" />
          <el-table-column label="设备状态" prop="equipmentStatus" key="equipmentStatus" :width="colWidth('equipmentStatus', 100)" resizable align="center" v-if="columns.equipmentStatus.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.equipmentStatus === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ equipmentStatusLabel(scope.row.equipmentStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['dms:equipment:query']">查看</el-button>
              <el-button link type="primary" @click="handleScreen(scope.row)" v-hasPermi="['dms:equipment:query']">大屏</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:equipment:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:equipment:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="equipmentRef" :model="form" :rules="rules" label-width="100px">
                <!-- 分组一：基本信息 -->
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c4">
        <el-row>
          <el-col :span="12"><el-form-item label="设备编号" prop="equipmentCode"><el-input v-model="form.equipmentCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="equipmentName"><el-input v-model="form.equipmentName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备分类" prop="categoryId"><el-tree-select v-model="form.categoryId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="请选择" check-strictly style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备状态" prop="equipmentStatus">
            <el-select v-model="form.equipmentStatus" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_equipment_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备等级" prop="equipmentLevel">
            <el-select v-model="form.equipmentLevel" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_equipment_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号" prop="model"><el-input v-model="form.model" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="序列号" prop="serialNumber"><el-input v-model="form.serialNumber" placeholder="请输入" /></el-form-item></el-col>
        </el-row>

        <!-- 分组二：资产信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>资产信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12"><el-form-item label="资产编号" prop="assetCode"><el-input v-model="form.assetCode" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="制造商" prop="manufacturer"><el-input v-model="form.manufacturer" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="供应商" prop="supplier"><el-input v-model="form.supplier" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="购置日期" prop="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="原值" prop="originalValue"><el-input-number v-model="form.originalValue" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="启用日期" prop="installDate"><el-date-picker v-model="form.installDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="质保期限" prop="warrantyDate"><el-date-picker v-model="form.warrantyDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="报废处置日期" prop="retireDate"><el-date-picker v-model="form.retireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
        </el-row>

        <!-- 分组三：使用信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>使用信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="责任人" prop="responsibleId">
              <el-input v-model="form.responsibleName" readonly placeholder="请选择责任人" style="width: 100%" @click="openUserPicker">
                <template #append>
                  <el-button icon="Search" @click="openUserPicker" />
                </template>
                <template #suffix>
                  <el-icon v-if="form.responsibleName" class="clear-icon" @click.stop="clearResponsible"><CircleClose /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用部门" prop="deptId">
              <el-input v-model="form.deptName" readonly placeholder="请选择部门" style="width: 100%" @click="openDeptPicker">
                <template #append>
                  <el-button icon="Search" @click="openDeptPicker" />
                </template>
                <template #suffix>
                  <el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="安装位置" prop="installLocation">
              <el-input v-model="form.installLocation" placeholder="请输入安装位置" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组四：图片与附件 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg></span>图片与附件</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="设备图片" prop="equipmentImage">
              <image-upload v-model="form.equipmentImage" :limit="5" :fileSize="10" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件资料" prop="attachmentUrl">
              <file-upload v-model="form.attachmentUrl" :fileSize="50" :fileType="['bmp', 'gif', 'jpg', 'jpeg', 'png', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf', 'rar', 'zip', 'gz', 'bz2', 'mp4', 'avi', 'rmvb']" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 分组五：其他信息 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-row :gutter="20">
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
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 设备详情弹窗 -->
    <dms-equipment-view-drawer ref="equipmentViewRef" />

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="设备台账导入"
      action="/dms/equipment/importData"
      template-action="/dms/equipment/importTemplate"
      template-file-name="equipment_template"
      update-support-label="是否更新已经存在的设备数据"
      :tips="importTips"
      :update-key-options="updateKeyOptions"
      default-update-key="equipmentName"
      @success="getList"
    />

    <!-- 责任人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择设备责任人" @confirm="onUserPickerConfirm" />

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择使用部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />
  </div>
</template>

<script setup name="DmsEquipment">
import { listEquipment, getEquipment, addEquipment, updateEquipment, delEquipment } from '@/api/dms/equipment'
import { listCategory } from '@/api/dms/category'
import DmsEquipmentViewDrawer from './view.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import ExcelImportDialog from '@/components/ExcelImportDialog/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Edit, Delete, Download, Upload } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(["c4","c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_equipment_index')
const { dms_equipment_status, dms_equipment_level } = proxy.useDict('dms_equipment_status', 'dms_equipment_level')

const list = ref([])
const categoryOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  equipmentCode: { label: '设备编号', visible: true },
  assetCode: { label: '资产编号', visible: true },
  equipmentName: { label: '设备名称', visible: true },
  categoryName: { label: '分类', visible: true },
  model: { label: '型号', visible: true },
  equipmentLevel: { label: '设备等级', visible: true },
  deptName: { label: '使用部门', visible: true },
  installLocation: { label: '安装位置', visible: true },
  responsibleName: { label: '责任人', visible: true },
  equipmentStatus: { label: '设备状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_equipment_columns')
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
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.equipmentCode) count++
  if (queryParams.value.equipmentName) count++
  if (queryParams.value.equipmentStatus) count++
  if (queryParams.value.equipmentLevel) count++
  return count
})
function equipmentLevelLabel(val) { const item = dms_equipment_level.value.find(d => d.value == val); return item ? item.label : '-' }
function equipmentStatusLabel(val) { const item = dms_equipment_status.value.find(d => d.value == val); return item ? item.label : '-' }

// ===== 导入功能配置 =====
const importTips = [
  '设备名称为必填字段，不能为空',
  '设备编号由系统自动生成，无需填写',
  '设备状态填：运行/空闲/维修/故障/报废（或留空，默认为运行）',
  '设备等级、设备状态等字典字段请参考系统中已有数据填写',
  '如勾选「更新已存在数据」，需选择匹配字段：按设备名称、设备编号、资产编号或序列号匹配',
  '导入结果将显示每条数据的处理情况，失败数据可修正后重新导入'
]
const updateKeyOptions = [
  { value: 'equipmentName', label: '设备名称' },
  { value: 'equipmentCode', label: '设备编号' },
  { value: 'assetCode', label: '资产编号' },
  { value: 'serialNumber', label: '序列号' }
]
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, equipmentCode: undefined, equipmentName: undefined, equipmentStatus: undefined, equipmentLevel: undefined },
  rules: {
    equipmentName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listEquipment(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}
function getCategoryTree() {
  listCategory().then(res => { categoryOptions.value = proxy.handleTree(res.data, 'categoryId') })
}
/** 打开责任人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.responsibleId)
}
/** 责任人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.responsibleId = user.userId
  form.value.responsibleName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除责任人 */
function clearResponsible() {
  form.value.responsibleId = undefined
  form.value.responsibleName = undefined
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
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.equipmentCode = undefined; queryParams.value.equipmentName = undefined; queryParams.value.equipmentStatus = undefined; queryParams.value.equipmentLevel = undefined; proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.equipmentId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = {
    equipmentCode: undefined, assetCode: undefined, equipmentName: undefined, categoryId: undefined, model: undefined, serialNumber: undefined,
    manufacturer: undefined, supplier: undefined, purchaseDate: undefined, installDate: undefined, retireDate: undefined, originalValue: undefined,
    deptId: undefined, deptName: undefined, installLocation: undefined, equipmentStatus: '0', equipmentLevel: undefined, warrantyDate: undefined,
    responsibleId: undefined,
    equipmentImage: undefined, attachmentUrl: undefined, status: '0', remark: undefined,
    responsibleName: undefined
  }
  proxy.resetForm('equipmentRef')
}
function handleAdd() { reset(); open.value = true; title.value = '添加设备' }
function handleUpdate(row) {
  reset()
  getEquipment(row.equipmentId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改设备' })
}
function submitForm() {
  proxy.$refs['equipmentRef'].validate(valid => {
    if (valid) {
      if (form.value.equipmentId != undefined) { updateEquipment(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addEquipment(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const equipmentIds = row.equipmentId || ids.value; proxy.$modal.confirm('确认删除编号为"' + equipmentIds + '"的数据？').then(() => delEquipment(equipmentIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('dms/equipment/export', { ...queryParams.value }, `equipment_${new Date().getTime()}.xlsx`) }
function handleImport() {
  proxy.$refs['importRef'].open()
}
function cancel() { open.value = false; reset() }
/** 查看设备详情 */
function handleView(row) {
  proxy.$refs.equipmentViewRef.open(row.equipmentId)
}
/** 跳转设备大屏（新窗口全屏打开） */
function handleScreen(row) {
  const url = '/dms/dashboard/screen?equipmentId=' + row.equipmentId
  window.open(url, '_blank')
}
getCategoryTree()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.dms-equipment-page {
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
.dms-equipment-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.dms-equipment-page .filter-card { padding:14px 20px 16px; }
.dms-equipment-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-equipment-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-equipment-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-equipment-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-equipment-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-equipment-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-equipment-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.dms-equipment-page .field { display:flex; flex-direction:column; gap:6px; }
.dms-equipment-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.dms-equipment-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.dms-equipment-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.dms-equipment-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.dms-equipment-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.dms-equipment-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.dms-equipment-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.dms-equipment-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select transparent inside .control */
.dms-equipment-page .field .control :deep(.el-select) { width:100%; }
.dms-equipment-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.dms-equipment-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.dms-equipment-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.dms-equipment-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.dms-equipment-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.dms-equipment-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.dms-equipment-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.dms-equipment-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.dms-equipment-page .btn-soft .el-icon { font-size:14px; }
.dms-equipment-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.dms-equipment-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.dms-equipment-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.dms-equipment-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.dms-equipment-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-equipment-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.dms-equipment-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.dms-equipment-page .table-wrap { overflow-x:auto; }
.dms-equipment-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-equipment-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-equipment-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-equipment-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-equipment-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-equipment-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-equipment-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-equipment-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-equipment-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-equipment-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-equipment-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.dms-equipment-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-equipment-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-equipment-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-equipment-page .badge.blue .dot { background:var(--blue-500); }
.dms-equipment-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-equipment-page .badge.green .dot { background:var(--green-500); }
.dms-equipment-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-equipment-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.dms-equipment-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-equipment-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-equipment-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-equipment-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-equipment-page .pagination-container :deep(.el-pagination .btn-prev), .dms-equipment-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-equipment-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-equipment-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-equipment-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Dialog (existing) ===== */
.dms-equipment-page .clear-icon { cursor:pointer; color:#c0c4cc; font-size:14px; }
.dms-equipment-page .clear-icon:hover { color:#909399; }
.dms-equipment-page :deep(.el-input.is-disabled .el-input__inner) { cursor:pointer; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .dms-equipment-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-equipment-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-equipment-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
