<template>
  <div class="app-container dms-inspection-route-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>路线名称</label>
          <div class="control">
            <el-input v-model="queryParams.routeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>周期类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.cycleType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_inspection_cycle" :key="d.value" :label="d.label" :value="d.value" />
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
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track"></div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:inspection:route:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:inspection:route:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:inspection:route:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_inspection_route_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="路线名称" prop="routeName" key="routeName" :width="colWidth('routeName', 160)" resizable show-overflow-tooltip v-if="columns.routeName.visible" />
          <el-table-column label="路线编码" prop="routeCode" key="routeCode" :width="colWidth('routeCode', 180)" resizable show-overflow-tooltip v-if="columns.routeCode.visible" />
          <el-table-column label="部门" prop="deptName" key="deptName" :width="colWidth('deptName', 120)" resizable v-if="columns.deptName.visible" />
          <el-table-column label="周期" prop="cycleType" key="cycleType" :width="colWidth('cycleType', 120)" resizable align="center" v-if="columns.cycleType.visible">
            <template #default="scope"><span class="badge amber">{{ cycleTypeLabel(scope.row.cycleType) }}</span> {{ scope.row.cycleValue }}</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:inspection:route:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:inspection:route:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="routeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
        <el-row>
          <el-col :span="12"><el-form-item label="路线名称" prop="routeName"><el-input v-model="form.routeName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="路线编码" prop="routeCode"><el-input v-model="form.routeCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部门" prop="deptId">
            <el-input v-model="form.deptName" readonly placeholder="请选择归属部门" style="width: 100%" @click="openDeptPicker">
              <template #append>
                <el-button icon="Search" @click="openDeptPicker" />
              </template>
              <template #suffix>
                <el-icon v-if="form.deptName" class="clear-icon" @click.stop="clearDept"><CircleClose /></el-icon>
              </template>
            </el-input>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></span>巡检配置</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
        <el-row>
          <el-col :span="12"><el-form-item label="周期类型" prop="cycleType">
            <el-select v-model="form.cycleType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_inspection_cycle" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="周期值" prop="cycleValue"><el-input-number v-model="form.cycleValue" :min="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="巡检设备" prop="equipmentIdList">
          <el-select v-model="form.equipmentIdList" multiple filterable clearable placeholder="请选择设备（可多选，按选择顺序为巡检顺序）" style="width: 100%" @change="onEquipmentChange">
            <el-option v-for="e in equipmentOptions" :key="e.equipmentId" :label="e.equipmentCode + ' - ' + e.equipmentName" :value="e.equipmentId" />
          </el-select>
        </el-form-item>

        <!-- 通用点检项 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>通用点检项（所有设备共用）</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
        <el-table :data="form.commonItems" border style="width: 100%" size="small" @header-dragend="onHeaderDragEnd">
          <el-table-column label="序号" width="60" align="center">
            <template #default="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column label="检查项" min-width="200">
            <template #default="scope"><el-input v-model="scope.row.item" placeholder="如：设备周围卫生" size="small" /></template>
          </el-table-column>
          <el-table-column label="类型" width="120" align="center">
            <template #default="scope">
              <el-select v-model="scope.row.type" size="small" placeholder="类型">
                <el-option label="打勾" value="check" />
                <el-option label="数值" value="number" />
                <el-option label="文本" value="text" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80" align="center">
            <template #default="scope"><el-input v-model="scope.row.unit" placeholder="如℃/MPa" size="small" :disabled="scope.row.type !== 'number'" /></template>
          </el-table-column>
          <el-table-column label="异常必填说明" width="110" align="center">
            <template #default="scope"><el-switch v-model="scope.row.abnormalRequired" /></template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeCommonItem(scope.$index)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 8px" @click="addCommonItem">添加通用项</el-button>

        <!-- 设备明细分组 -->
                  </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>设备点检项明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <div v-if="form.deviceItems.length === 0" style="text-align: center; color: #999; padding: 16px 0">请先选择巡检设备，然后为每台设备配置专属点检项</div>
        <el-collapse v-model="activeDeviceTabs" style="margin-top: 4px">
          <el-collapse-item v-for="(dev, di) in form.deviceItems" :key="dev.equipmentId" :name="dev.equipmentId">
            <template #title>
              <span style="font-weight: 600; font-size: 14px">{{ dev.equipmentName }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ dev.items.length }}项</el-tag>
            </template>
            <el-table :data="dev.items" border style="width: 100%" size="small" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" width="60" align="center">
                <template #default="scope">{{ scope.$index + 1 }}</template>
              </el-table-column>
              <el-table-column label="检查项" min-width="200">
                <template #default="scope"><el-input v-model="scope.row.item" :placeholder="'如：检查' + dev.equipmentName + '温度'" size="small" /></template>
              </el-table-column>
              <el-table-column label="类型" width="120" align="center">
                <template #default="scope">
                  <el-select v-model="scope.row.type" size="small" placeholder="类型">
                    <el-option label="打勾" value="check" />
                    <el-option label="数值" value="number" />
                    <el-option label="文本" value="text" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="单位" width="80" align="center">
                <template #default="scope"><el-input v-model="scope.row.unit" placeholder="如℃" size="small" :disabled="scope.row.type !== 'number'" /></template>
              </el-table-column>
              <el-table-column label="异常必填说明" width="110" align="center">
                <template #default="scope"><el-switch v-model="scope.row.abnormalRequired" /></template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="removeDeviceItem(di, scope.$index)">删除</el-button></template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" style="margin-top: 8px" @click="addDeviceItem(di)">添加点检项</el-button>
          </el-collapse-item>
        </el-collapse>

        <el-form-item label="备注" prop="remark" style="margin-top: 12px"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 部门选择弹窗 -->
    <dept-picker ref="deptPickerRef" title="选择归属部门" :disabled-ids="[100]" @confirm="onDeptPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="巡检路线业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务流程图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="primary">创建巡检路线</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">选择巡检设备</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">配置点检项</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">生成点检任务</el-tag>
          </div>
        </div>

        <h4>二、点检项配置说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="通用点检项">所有巡检设备共用的检查项，如设备周围卫生、安全防护装置等</el-descriptions-item>
          <el-descriptions-item label="设备专属点检项">每台设备单独配置的检查项，如特定设备的温度、压力、振动等</el-descriptions-item>
          <el-descriptions-item label="打勾类型">只需判断正常/异常的检查项，如“安全装置完好”</el-descriptions-item>
          <el-descriptions-item label="数值类型">需填写具体数值的检查项，可设置单位（如℃/MPa），便于数据分析</el-descriptions-item>
          <el-descriptions-item label="文本类型">需填写文字描述的检查项，如“外观描述”</el-descriptions-item>
          <el-descriptions-item label="异常必填说明">开启后，该检查项标记为异常时必须填写异常说明</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">巡检顺序</div>
              <div class="highlight-card-body">设备选择顺序即为<strong>巡检执行顺序</strong>，点检人按此顺序依次巡检各设备</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">周期配置</div>
              <div class="highlight-card-body">设置巡检周期类型和值，系统据此<strong>自动生成点检任务</strong></div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建路线：</strong>填写路线名称、选择归属部门，设置巡检周期类型和值
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>选择设备：</strong>多选巡检设备，选择顺序为巡检执行顺序</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>配置点检项：</strong>配置通用点检项（所有设备共用）和每台设备的专属点检项</el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>保存路线：</strong>保存后在点检任务模块中创建任务，点检人按路线配置执行点检</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsInspectionRoute">
import { CircleClose } from '@element-plus/icons-vue'
import { listRoute, getRoute, addRoute, updateRoute, delRoute } from '@/api/dms/inspection'
import { listEquipment } from '@/api/dms/equipment'
import DeptPicker from '@/components/DeptPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c3","c2","c1","c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_inspection_route_index')
const { dms_inspection_cycle } = proxy.useDict('dms_inspection_cycle')

const list = ref([])
const equipmentOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activeDeviceTabs = ref([])
const showStatusHelp = ref(false)

const defaultColumns = {
  routeName: { label: '路线名称', visible: true },
  routeCode: { label: '路线编码', visible: true },
  deptName: { label: '部门', visible: true },
  cycleType: { label: '周期', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_inspection_route_columns')
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
  if (queryParams.value.routeName) count++
  if (queryParams.value.cycleType !== undefined && queryParams.value.cycleType !== null && queryParams.value.cycleType !== '') count++
  if (queryParams.value.status !== undefined && queryParams.value.status !== null && queryParams.value.status !== '') count++
  return count
})

function cycleTypeLabel(val) {
  const item = dms_inspection_cycle.value.find(d => d.value == val)
  return item ? item.label : '-'
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, routeName: undefined, cycleType: undefined, status: undefined },
  rules: {
    routeName: [{ required: true, message: '路线名称不能为空', trigger: 'blur' }],
    cycleType: [{ required: true, message: '周期类型不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listRoute(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function getEquipmentOptions() { listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows }) }
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
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.routeId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = {
    routeName: undefined, routeCode: undefined, deptId: undefined, deptName: undefined,
    cycleType: '0', cycleValue: 1,
    equipmentIds: undefined, equipmentIdList: [],
    inspectionItems: undefined,
    commonItems: [], deviceItems: [],
    status: '0', remark: undefined
  }
  activeDeviceTabs.value = []
  proxy.resetForm('routeRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增巡检路线' }

function handleUpdate(row) {
  reset()
  getRoute(row.routeId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.equipmentIds) { try { form.value.equipmentIdList = JSON.parse(form.value.equipmentIds) } catch (e) { form.value.equipmentIdList = [] } }
    if (form.value.inspectionItems) {
      try {
        const parsed = JSON.parse(form.value.inspectionItems)
        // 兼容旧格式：如果是扁平数组，当作通用项处理
        if (Array.isArray(parsed)) {
          form.value.commonItems = parsed
          form.value.deviceItems = []
        } else {
          form.value.commonItems = parsed.common || []
          form.value.deviceItems = parsed.devices || []
        }
      } catch (e) { form.value.commonItems = []; form.value.deviceItems = [] }
    }
    // 确保选中的设备都有对应的设备明细组
    syncDeviceItems()
    open.value = true; title.value = '修改巡检路线'
  })
}

// 设备选择变化时，同步设备明细分组
function onEquipmentChange(selectedIds) {
  syncDeviceItems()
}
function syncDeviceItems() {
  const selectedIds = form.value.equipmentIdList || []
  // 移除已取消选择的设备分组
  form.value.deviceItems = form.value.deviceItems.filter(d => selectedIds.includes(d.equipmentId))
  // 为新选择的设备创建空分组
  selectedIds.forEach(eid => {
    if (!form.value.deviceItems.find(d => d.equipmentId === eid)) {
      const eq = equipmentOptions.value.find(e => e.equipmentId === eid)
      form.value.deviceItems.push({
        equipmentId: eid,
        equipmentName: eq ? eq.equipmentCode + ' - ' + eq.equipmentName : '设备' + eid,
        items: []
      })
    }
  })
  // 默认展开所有设备
  activeDeviceTabs.value = form.value.deviceItems.map(d => d.equipmentId)
}

// 通用项操作
function addCommonItem() { form.value.commonItems.push({ item: '', type: 'check', unit: '', abnormalRequired: false }) }
function removeCommonItem(index) { form.value.commonItems.splice(index, 1) }

// 设备明细项操作
function addDeviceItem(di) { form.value.deviceItems[di].items.push({ item: '', type: 'check', unit: '', abnormalRequired: false }) }
function removeDeviceItem(di, ii) { form.value.deviceItems[di].items.splice(ii, 1) }

function submitForm() {
  proxy.$refs['routeRef'].validate(valid => {
    if (valid) {
      form.value.equipmentIds = form.value.equipmentIdList && form.value.equipmentIdList.length > 0 ? JSON.stringify(form.value.equipmentIdList) : undefined
      // 序列化为嵌套JSON结构
      const inspectionData = {
        common: form.value.commonItems || [],
        devices: form.value.deviceItems || []
      }
      const hasItems = inspectionData.common.length > 0 || inspectionData.devices.some(d => d.items.length > 0)
      form.value.inspectionItems = hasItems ? JSON.stringify(inspectionData) : undefined
      if (form.value.routeId != undefined) { updateRoute(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addRoute(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const routeIds = row.routeId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delRoute(routeIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getEquipmentOptions()
getList()
</script>

<style scoped>
.dms-inspection-route-page {
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
.dms-inspection-route-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-inspection-route-page .filter-card { padding:14px 20px 16px; }
.dms-inspection-route-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-inspection-route-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-inspection-route-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-inspection-route-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-inspection-route-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.dms-inspection-route-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.dms-inspection-route-page .filter-card .field .control { width:100%; }
.dms-inspection-route-page .filter-card .field .control .el-input, .dms-inspection-route-page .filter-card .field .control .el-select { width:100%; }
.dms-inspection-route-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-inspection-route-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-inspection-route-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-inspection-route-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.dms-inspection-route-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.dms-inspection-route-page .tabs-track::-webkit-scrollbar { display:none; }
.dms-inspection-route-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.dms-inspection-route-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.dms-inspection-route-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.dms-inspection-route-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.dms-inspection-route-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.dms-inspection-route-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.dms-inspection-route-page .toolbar-divider { width:1px; height:20px; background:var(--ink-200); margin:0 4px; }
.dms-inspection-route-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.dms-inspection-route-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.dms-inspection-route-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.dms-inspection-route-page .btn-soft.is-danger-outline { border-color:#fecaca; background:var(--red-50); color:var(--red-700); }
.dms-inspection-route-page .btn-soft.is-danger-outline:hover { background:var(--red-500); color:#fff; border-color:var(--red-500); }
.dms-inspection-route-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-inspection-route-page .table-wrap { overflow-x:auto; }
.dms-inspection-route-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-inspection-route-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-inspection-route-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-inspection-route-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-inspection-route-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-inspection-route-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-inspection-route-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-inspection-route-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-inspection-route-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-inspection-route-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-inspection-route-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-inspection-route-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-inspection-route-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-inspection-route-page .badge.amber .dot { background:var(--amber-500); }
.dms-inspection-route-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-inspection-route-page .badge.blue .dot { background:var(--blue-500); }
.dms-inspection-route-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-inspection-route-page .badge.green .dot { background:var(--green-500); }
.dms-inspection-route-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-inspection-route-page .badge.red .dot { background:var(--red-500); }
.dms-inspection-route-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-inspection-route-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-inspection-route-page .badge.gray .dot { background:var(--ink-400); }
@media (max-width:1100px) { .dms-inspection-route-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-inspection-route-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-inspection-route-page .toolbar { flex-wrap:wrap; gap:10px; } }
.dms-inspection-route-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .btn-prev), .dms-inspection-route-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-inspection-route-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-inspection-route-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
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
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
</style>
