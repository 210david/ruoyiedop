<template>
  <div class="app-container dms-data-record-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
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
          <label>运行状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.runStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_run_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>采集方式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.collectMode" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in dms_collect_mode" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:data:record:add']">录入</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:data:record:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:data:record:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_data_record_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="设备编码" prop="equipmentCode" key="equipmentCode" :width="colWidth('equipmentCode', 140)" resizable show-overflow-tooltip v-if="columns.equipmentCode.visible" />
          <el-table-column label="设备名称" prop="equipmentName" key="equipmentName" :width="colWidth('equipmentName', 160)" resizable show-overflow-tooltip v-if="columns.equipmentName.visible" />
          <el-table-column label="运行状态" prop="runStatus" key="runStatus" :width="colWidth('runStatus', 100)" resizable align="center" v-if="columns.runStatus.visible">
            <template #default="scope">
              <span class="badge" :class="runStatusBadge(scope.row.runStatus)"><span class="dot"></span>{{ runStatusLabel(scope.row.runStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="运行小时" prop="runHours" key="runHours" :width="colWidth('runHours', 100)" resizable align="center" v-if="columns.runHours.visible" />
          <el-table-column label="加工件数" prop="productCount" key="productCount" :width="colWidth('productCount', 100)" resizable align="center" v-if="columns.productCount.visible" />
          <el-table-column label="参数1" prop="param1Value" key="param1Value" :width="colWidth('param1Value', 90)" resizable align="center" v-if="columns.param1Value.visible" />
          <el-table-column label="参数2" prop="param2Value" key="param2Value" :width="colWidth('param2Value', 90)" resizable align="center" v-if="columns.param2Value.visible" />
          <el-table-column label="参数3" prop="param3Value" key="param3Value" :width="colWidth('param3Value', 90)" resizable align="center" v-if="columns.param3Value.visible" />
          <el-table-column label="采集方式" prop="collectMode" key="collectMode" :width="colWidth('collectMode', 100)" resizable align="center" v-if="columns.collectMode.visible">
            <template #default="scope">
              <span class="badge violet">{{ collectModeLabel(scope.row.collectMode) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="采集时间" prop="collectTime" key="collectTime" :width="colWidth('collectTime', 180)" resizable align="center" v-if="columns.collectTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:data:record:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:data:record:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

        <!-- 录入/修改弹窗 -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog data-record-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <div class="rd-page">
        <el-form ref="recordRef" :model="form" :rules="rules" label-width="100px">
          <!-- 设备信息卡片 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('edit_equipment')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>
                设备信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.edit_equipment }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.edit_equipment">
              <el-row>
                <el-col :span="12"><el-form-item label="关联设备" prop="equipmentId">
                  <el-input :model-value="form.equipmentCode" readonly placeholder="请选择设备" style="width: 100%" @click="openEquipmentPicker">
                    <template #suffix><el-icon style="cursor: pointer" @click.stop="openEquipmentPicker"><Search /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="设备名称" prop="equipmentName"><el-input v-model="form.equipmentName" placeholder="自动带出" disabled style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 运行数据卡片 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('edit_runtime')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>
                运行数据
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.edit_runtime }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.edit_runtime">
              <el-row>
                <el-col :span="12"><el-form-item label="运行状态" prop="runStatus">
                  <el-select v-model="form.runStatus" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in dms_run_status" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="运行小时" prop="runHours"><el-input-number v-model="form.runHours" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="加工件数" prop="productCount"><el-input-number v-model="form.productCount" :min="0" :precision="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="采集时间" prop="collectTime"><el-date-picker v-model="form.collectTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 参数数据卡片 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('edit_params')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="9" x2="20" y2="9"/><line x1="4" y1="15" x2="20" y2="15"/><line x1="10" y1="3" x2="8" y2="21"/><line x1="16" y1="3" x2="14" y2="21"/></svg></span>
                参数数据
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.edit_params }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.edit_params">
              <el-row>
                <el-col :span="8"><el-form-item label="参数1值" prop="param1Value"><el-input-number v-model="form.param1Value" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="参数2值" prop="param2Value"><el-input-number v-model="form.param2Value" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="参数3值" prop="param3Value"><el-input-number v-model="form.param3Value" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog data-record-view-dialog" top="5vh">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg></div>
          <span class="rd-detail-header-title">数据详情</span>
          <div class="rd-detail-header-tags" v-if="viewForm.runStatus != null">
            <dict-tag :options="dms_run_status" :value="viewForm.runStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 设备信息卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('view_equipment')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg></span>
              设备信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.view_equipment }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.view_equipment">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">设备编码</span><div class="rd-value">{{ viewForm.equipmentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">设备名称</span><div class="rd-value">{{ viewForm.equipmentName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采集方式</span><div class="rd-value"><dict-tag :options="dms_collect_mode" :value="viewForm.collectMode" /></div></div>
            </div>
          </div>
        </section>
        <!-- 运行数据卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('view_runtime')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>
              运行数据
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.view_runtime }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.view_runtime">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">运行状态</span><div class="rd-value"><dict-tag :options="dms_run_status" :value="viewForm.runStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">运行小时</span><div class="rd-value">{{ viewForm.runHours ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">加工件数</span><div class="rd-value">{{ viewForm.productCount ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采集时间</span><div class="rd-value">{{ viewForm.collectTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 参数数据卡片 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('view_params')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="9" x2="20" y2="9"/><line x1="4" y1="15" x2="20" y2="15"/><line x1="10" y1="3" x2="8" y2="21"/><line x1="16" y1="3" x2="14" y2="21"/></svg></span>
              参数数据
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.view_params }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.view_params">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">参数1值</span><div class="rd-value">{{ viewForm.param1Value ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">参数2值</span><div class="rd-value">{{ viewForm.param2Value ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">参数3值</span><div class="rd-value">{{ viewForm.param3Value ?? '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 设备选择弹框 -->
    <equipment-picker ref="equipmentPickerRef" title="选择采集设备" @confirm="onEquipmentPickerConfirm" />
  </div>
</template>

<script setup name="DmsDataRecord">
import { listRecord, getRecord, addRecord, updateRecord, delRecord } from '@/api/dms/data'
import EquipmentPicker from '@/components/EquipmentPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['view_equipment', 'view_runtime', 'view_params', 'edit_equipment', 'edit_runtime', 'edit_params'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_data_record_index')
const { dms_run_status, dms_collect_mode } = proxy.useDict('dms_run_status', 'dms_collect_mode')

const list = ref([])
const equipmentPickerRef = ref()
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewForm = ref({})

const defaultColumns = {
  equipmentCode: { label: '设备编码', visible: true },
  equipmentName: { label: '设备名称', visible: true },
  runStatus: { label: '运行状态', visible: true },
  runHours: { label: '运行小时', visible: true },
  productCount: { label: '加工件数', visible: true },
  param1Value: { label: '参数1', visible: true },
  param2Value: { label: '参数2', visible: true },
  param3Value: { label: '参数3', visible: true },
  collectMode: { label: '采集方式', visible: true },
  collectTime: { label: '采集时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_data_record_columns')
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
  if (queryParams.value.equipmentName) count++
  if (queryParams.value.runStatus !== undefined && queryParams.value.runStatus !== null && queryParams.value.runStatus !== '') count++
  if (queryParams.value.collectMode !== undefined && queryParams.value.collectMode !== null && queryParams.value.collectMode !== '') count++
  return count
})

function runStatusLabel(val) {
  const item = dms_run_status.value.find(d => d.value == val)
  return item ? item.label : '-'
}
function runStatusBadge(val) {
  const map = { '0': 'green', '1': 'amber', '2': 'red', '3': 'gray' }
  return map[val] || 'gray'
}
function collectModeLabel(val) {
  const item = dms_collect_mode.value.find(d => d.value == val)
  return item ? item.label : '-'
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, equipmentName: undefined, runStatus: undefined, collectMode: undefined },
  rules: {
    equipmentId: [{ required: true, message: '请选择关联设备', trigger: 'change' }],
    runStatus: [{ required: true, message: '运行状态不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listRecord(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
/** 打开设备选择弹框 */
function openEquipmentPicker() { equipmentPickerRef.value.open(form.value.equipmentId) }
/** 设备选择确认：显示设备编号，自动带出名称 */
function onEquipmentPickerConfirm(equipment) {
  form.value.equipmentId = equipment.equipmentId
  form.value.equipmentCode = equipment.equipmentCode
  form.value.equipmentName = equipment.equipmentName
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined, runStatus: '0', runHours: 0, productCount: 0, param1Value: undefined, param2Value: undefined, param3Value: undefined, collectTime: new Date(), collectMode: '0' }; proxy.resetForm('recordRef') }
function handleAdd() { reset(); title.value = '录入数据'; open.value = true }
function handleUpdate(row) { reset(); getRecord(row.recordId || ids.value[0]).then(res => { form.value = res.data; title.value = '修改数据'; open.value = true }) }
function submitForm() {
  proxy.$refs['recordRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) { updateRecord(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addRecord(form.value).then(() => { proxy.$modal.msgSuccess('录入成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delRecord(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function handleView(row) { getRecord(row.recordId).then(res => { viewForm.value = res.data; viewOpen.value = true }) }
getList()
</script>

<style scoped>
.dms-data-record-page {
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
.dms-data-record-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-data-record-page .filter-card { padding:14px 20px 16px; }
.dms-data-record-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-data-record-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-data-record-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-data-record-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-data-record-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.dms-data-record-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.dms-data-record-page .filter-card .field .control { width:100%; }
.dms-data-record-page .filter-card .field .control .el-input, .dms-data-record-page .filter-card .field .control .el-select { width:100%; }
.dms-data-record-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-data-record-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-data-record-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-data-record-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.dms-data-record-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.dms-data-record-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.dms-data-record-page .toolbar-divider { width:1px; height:20px; background:var(--ink-200); margin:0 4px; }
.dms-data-record-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.dms-data-record-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.dms-data-record-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.dms-data-record-page .btn-soft.is-danger-outline { border-color:#fecaca; background:var(--red-50); color:var(--red-700); }
.dms-data-record-page .btn-soft.is-danger-outline:hover { background:var(--red-500); color:#fff; border-color:var(--red-500); }
.dms-data-record-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-data-record-page .table-wrap { overflow-x:auto; }
.dms-data-record-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-data-record-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-data-record-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-data-record-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-data-record-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-data-record-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-data-record-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-data-record-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-data-record-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-data-record-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-data-record-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-data-record-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-data-record-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-data-record-page .badge.amber .dot { background:var(--amber-500); }
.dms-data-record-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-data-record-page .badge.blue .dot { background:var(--blue-500); }
.dms-data-record-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-data-record-page .badge.green .dot { background:var(--green-500); }
.dms-data-record-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-data-record-page .badge.red .dot { background:var(--red-500); }
.dms-data-record-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-data-record-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-data-record-page .badge.gray .dot { background:var(--ink-400); }
/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行 */
.dms-data-record-page :deep(.col-action) { padding: 6px 4px !important; }
.dms-data-record-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.dms-data-record-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.dms-data-record-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.dms-data-record-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

@media (max-width:1100px) { .dms-data-record-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-data-record-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-data-record-page .toolbar { flex-wrap:wrap; gap:10px; } }
.dms-data-record-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-data-record-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-data-record-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-data-record-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-data-record-page .pagination-container :deep(.el-pagination .btn-prev), .dms-data-record-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-data-record-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-data-record-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-data-record-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
</style>
