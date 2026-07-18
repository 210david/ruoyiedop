<template>
  <div class="app-container">
    <!-- 视图切换 -->
    <el-radio-group v-model="viewMode" style="margin-bottom: 12px">
      <el-radio-button value="list">列表视图</el-radio-button>
      <el-radio-button value="calendar">日历视图</el-radio-button>
      <el-radio-button value="timeline">时间线视图</el-radio-button>
    </el-radio-group>

    <!-- ===== 列表视图 ===== -->
    <template v-if="viewMode === 'list'">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="计划名称" prop="planName">
        <el-input v-model="queryParams.planName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="触发类型" prop="triggerType">
        <el-select v-model="queryParams.triggerType" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="d in dms_pm_trigger_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:pmplan:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:pmplan:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:pmplan:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Refresh" @click="handleAutoGenerate" v-hasPermi="['dms:pmplan:edit']">执行自动生成</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="计划名称" prop="planName" min-width="180" show-overflow-tooltip />
      <el-table-column label="关联设备" prop="equipmentName" :width="colWidth('equipmentName', 160)" resizable show-overflow-tooltip />
      <el-table-column label="触发类型" prop="triggerType" :width="colWidth('triggerType', 130)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_pm_trigger_type" :value="scope.row.triggerType" /></template>
      </el-table-column>
      <el-table-column label="周期" width="100" align="center">
        <template #default="scope">{{ scope.row.cycleValue }} {{ scope.row.cycleUnit }}</template>
      </el-table-column>
      <el-table-column label="预估工时" prop="estimatedHours" :width="colWidth('estimatedHours', 90)" resizable align="center" />
      <el-table-column label="提前天数" prop="advanceDays" :width="colWidth('advanceDays', 80)" resizable align="center" />
      <el-table-column label="自动派工" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.autoAssign === '1' ? 'success' : 'info'" size="small">{{ scope.row.autoAssign === '1' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上次执行" prop="lastExecuteTime" :width="colWidth('lastExecuteTime', 160)" resizable align="center" />
      <el-table-column label="下次执行" prop="nextExecuteTime" :width="colWidth('nextExecuteTime', 160)" resizable align="center">
        <template #default="scope">
          <span :style="isExpired(scope.row) ? 'color: #f56c6c; font-weight: bold' : ''">{{ scope.row.nextExecuteTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:pmplan:edit']">修改</el-button>
          <el-button link type="success" @click="handleGenerate(scope.row)" v-if="scope.row.status === '0'">生成工单</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:pmplan:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </template>

    <!-- ===== 日历视图 ===== -->
    <template v-if="viewMode === 'calendar'">
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div style="height: 100%; position: relative; overflow: hidden">
            <div :class="data.isSelected ? 'is-selected' : ''">{{ data.day.split('-').slice(2).join('') }}</div>
            <div v-for="item in getCalendarItems(data.day)" :key="item.planId" style="font-size: 11px; margin-top: 2px; padding: 1px 4px; border-radius: 3px; background: #e8f4ff; color: #409eff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer" @click="handleCalendarClick(item)">
              {{ item.planName }}
            </div>
          </div>
        </template>
      </el-calendar>
    </template>

    <!-- ===== 时间线视图 ===== -->
    <template v-if="viewMode === 'timeline'">
      <el-timeline>
        <el-timeline-item v-for="item in calendarData" :key="item.planId" :timestamp="formatDate(item.nextExecuteTime)" placement="top" :type="isExpiredByDate(item.nextExecuteTime) ? 'danger' : 'primary'" :hollow="isExpiredByDate(item.nextExecuteTime)">
          <el-card shadow="hover" style="cursor: pointer" @click="handleUpdate(item)">
            <div style="display: flex; justify-content: space-between; align-items: center">
              <div>
                <strong>{{ item.planName }}</strong>
                <el-tag v-if="item.autoAssign === '1'" type="success" size="small" style="margin-left: 8px">自动派工</el-tag>
                <el-tag v-if="isExpiredByDate(item.nextExecuteTime)" type="danger" size="small" style="margin-left: 8px">已逾期</el-tag>
              </div>
              <div style="color: #909399; font-size: 13px">
                <span v-if="item.equipmentName">{{ item.equipmentName }} | </span>
                <span>{{ item.cycleValue }} {{ item.cycleUnit }}</span>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </template>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="pmplanRef" :model="form" :rules="rules" label-width="100px">
        <!-- 分组一：计划信息 -->
        <el-divider content-position="center">计划信息</el-divider>
        <el-row>
          <el-col :span="24"><el-form-item label="计划名称" prop="planName"><el-input v-model="form.planName" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="关联设备" prop="equipmentId">
            <el-select v-model="form.equipmentId" filterable clearable placeholder="请选择设备" style="width: 100%" @change="onEquipmentChange">
              <el-option v-for="e in equipmentOptions" :key="e.equipmentId" :label="e.equipmentCode + ' - ' + e.equipmentName" :value="e.equipmentId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备分类" prop="categoryName"><el-input v-model="form.categoryName" placeholder="自动带出" disabled /></el-form-item></el-col>
        </el-row>

        <!-- 分组二：触发规则 -->
        <el-divider content-position="center">触发规则</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="触发类型" prop="triggerType">
            <el-select v-model="form.triggerType" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_pm_trigger_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="6"><el-form-item label="周期值" prop="cycleValue"><el-input-number v-model="form.cycleValue" :min="1" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="单位" prop="cycleUnit"><el-input v-model="form.cycleUnit" placeholder="天/月/小时" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="提前天数" prop="advanceDays"><el-input-number v-model="form.advanceDays" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="自动派工" prop="autoAssign">
            <el-radio-group v-model="form.autoAssign"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <!-- 自动派工人员选择 -->
        <el-row v-if="form.autoAssign === '1'">
          <el-col :span="12"><el-form-item label="派工人员" prop="assigneeId">
            <el-select v-model="form.assigneeId" filterable clearable placeholder="请选择默认派工人员" style="width: 100%" @change="onAssigneeChange">
              <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="人员名称"><el-input v-model="form.assigneeName" placeholder="选择后自动带出" disabled /></el-form-item></el-col>
        </el-row>

        <!-- 分组三：任务配置 -->
        <el-divider content-position="center">任务配置</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="预估工时" prop="estimatedHours"><el-input-number v-model="form.estimatedHours" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <!-- 任务清单编辑器 -->
        <el-form-item label="任务清单" prop="taskList">
          <div style="width: 100%">
            <el-alert type="info" :closable="false" show-icon style="margin-bottom: 8px">
              <template #title>维护时需要逐项执行的标准作业步骤，生成工单后维修人员按清单逐项打勾确认</template>
            </el-alert>
            <div v-for="(task, index) in taskItems" :key="index" style="display: flex; align-items: center; margin-bottom: 8px">
              <span style="width: 28px; text-align: center; color: #909399; font-weight: bold; flex-shrink: 0">{{ index + 1 }}</span>
              <el-input v-model="task.text" placeholder="例如：检查电机温度、清洁滤网、紧固螺栓" style="flex: 1; margin-right: 8px" />
              <el-button type="danger" plain icon="Delete" circle size="small" @click="taskItems.splice(index, 1)" />
            </div>
            <el-button type="primary" plain icon="Plus" size="small" @click="taskItems.push({ text: '' })">添加任务项</el-button>
          </div>
        </el-form-item>
        <!-- 备件清单编辑器 -->
        <el-form-item label="备件清单" prop="sparePartList">
          <div style="width: 100%">
            <el-alert type="info" :closable="false" show-icon style="margin-bottom: 8px">
              <template #title>预先配置本次维护需要准备的备件及数量，生成工单时自动带入</template>
            </el-alert>
            <el-table v-if="sparePartItems.length > 0" :data="sparePartItems" border size="small" style="margin-bottom: 8px" @header-dragend="onHeaderDragEnd">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="备件名称" min-width="200">
                <template #default="{ row }">
                  <el-select v-model="row.partId" filterable clearable placeholder="请选择备件" style="width: 100%" @change="(val) => onSparePartSelect(row, val)">
                    <el-option v-for="p in getAvailableSpareParts(row.partId)" :key="p.partId" :label="p.partName + (p.specModel ? ' (' + p.specModel + ')' : '') + ' [库存:' + (p.currentStock || 0) + dictUnitLabel(p.unit) + ']'" :value="p.partId" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="数量" width="120" align="center">
                <template #default="{ row }">
                  <el-input-number v-model="row.qty" :min="1" :precision="0" size="small" controls-position="right" style="width: 100px" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="70" align="center">
                <template #default="{ row }">{{ dictUnitLabel(row.unit) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" icon="Delete" @click="sparePartItems.splice($index, 1)"></el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain icon="Plus" size="small" @click="sparePartItems.push({ partId: undefined, partName: '', qty: 1, unit: '' })">添加备件</el-button>
          </div>
        </el-form-item>

        <!-- 分组四：其他信息 -->
        <el-divider content-position="center">其他信息</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsPmPlan">
import { listPmplan, getPmplan, addPmplan, updatePmplan, delPmplan, generateWorkOrder, autoGeneratePm, getPmplanCalendar } from '@/api/dms/pmplan'
import { listEquipment } from '@/api/dms/equipment'
import { listUser } from '@/api/system/user'
import { listSparepart } from '@/api/dms/sparepart'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_pmplan_index')
const { dms_pm_trigger_type, wms_unit } = proxy.useDict('dms_pm_trigger_type', 'wms_unit')

const list = ref([])
const equipmentOptions = ref([])
const userOptions = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewMode = ref('list')
const calendarDate = ref(new Date())
const calendarData = ref([])
const taskItems = ref([])
const sparePartItems = ref([])
const sparePartOptions = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, planName: undefined, triggerType: undefined },
  rules: {
    planName: [{ required: true, message: '计划名称不能为空', trigger: 'blur' }],
    triggerType: [{ required: true, message: '触发类型不能为空', trigger: 'change' }],
    cycleValue: [{ required: true, message: '周期值不能为空', trigger: 'blur' }],
    cycleUnit: [{ required: true, message: '周期单位不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPmplan(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getEquipmentOptions() {
  listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows })
}
function getUserList() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
function getSparePartList() {
  listSparepart({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { sparePartOptions.value = res.rows })
}
/** 获取可选备件列表（排除已选的） */
function getAvailableSpareParts(currentPartId) {
  const selectedIds = sparePartItems.value.filter(i => i.partId && i.partId !== currentPartId).map(i => i.partId)
  return sparePartOptions.value.filter(p => !selectedIds.includes(p.partId))
}
/** 选择备件后自动带出名称和单位 */
function onSparePartSelect(row, partId) {
  if (partId) {
    const part = sparePartOptions.value.find(p => p.partId === partId)
    if (part) {
      row.partName = part.partName
      row.unit = part.unit || ''
    }
  } else {
    row.partName = ''
    row.unit = ''
  }
}
/** 字典值转单位标签 */
function dictUnitLabel(val) {
  if (!val && val !== '0') return ''
  const dictItem = wms_unit.value.find(d => d.value === val)
  return dictItem ? dictItem.label : val
}
function onEquipmentChange(equipmentId) {
  if (equipmentId) {
    const equipment = equipmentOptions.value.find(e => e.equipmentId === equipmentId)
    if (equipment) {
      form.value.categoryId = equipment.categoryId
      form.value.categoryName = equipment.categoryName
    }
  } else {
    form.value.categoryId = undefined
    form.value.categoryName = undefined
  }
}
function onAssigneeChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      form.value.assigneeName = user.nickName
    }
  } else {
    form.value.assigneeName = undefined
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.planId); single.value = selection.length !== 1; multiple.value = !selection.length }

/** 任务清单JSON与数组互转 */
function parseTaskList(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) {
      // 兼容字符串数组和对象数组
      return arr.map(item => typeof item === 'string' ? { text: item } : { text: item.text || item.task || item.name || '' })
    }
  } catch (e) {
    // 非JSON，按换行分割
    return str.split('\n').filter(s => s.trim()).map(s => ({ text: s.trim() }))
  }
  return []
}
function taskListToString(items) {
  const valid = items.filter(i => i.text && i.text.trim())
  return valid.length > 0 ? JSON.stringify(valid.map(i => i.text.trim())) : ''
}
/** 备件清单JSON解析为数组 */
function parseSparePartList(str) {
  if (!str) return []
  try {
    const arr = JSON.parse(str)
    if (Array.isArray(arr)) {
      return arr.map(item => ({ partId: item.partId, partName: item.partName || '', qty: item.qty || 1, unit: item.unit || '' }))
    }
  } catch (e) { /* ignore */ }
  return []
}
/** 备件清单数组序列化为JSON */
function sparePartListToString(items) {
  const valid = items.filter(i => i.partId)
  return valid.length > 0 ? JSON.stringify(valid.map(i => ({ partId: i.partId, partName: i.partName, qty: i.qty || 1, unit: i.unit }))) : ''
}

function reset() {
  form.value = { planName: undefined, equipmentId: undefined, categoryId: undefined, categoryName: undefined, triggerType: '0', cycleValue: 30, cycleUnit: '天', taskList: undefined, sparePartList: undefined, estimatedHours: undefined, advanceDays: 3, autoAssign: '0', assigneeId: undefined, assigneeName: undefined, status: '0', remark: undefined }
  taskItems.value = []
  sparePartItems.value = []
  proxy.resetForm('pmplanRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增PM计划' }
function handleUpdate(row) {
  reset()
  getPmplan(row.planId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.equipmentId) {
      const equipment = equipmentOptions.value.find(e => e.equipmentId === form.value.equipmentId)
      if (equipment) { form.value.categoryName = equipment.categoryName }
    }
    taskItems.value = parseTaskList(form.value.taskList)
    sparePartItems.value = parseSparePartList(form.value.sparePartList)
    open.value = true
    title.value = '修改PM计划'
  })
}
function submitForm() {
  proxy.$refs['pmplanRef'].validate(valid => {
    if (valid) {
      form.value.taskList = taskListToString(taskItems.value) || undefined
      form.value.sparePartList = sparePartListToString(sparePartItems.value) || undefined
      if (form.value.autoAssign !== '1') {
        form.value.assigneeId = undefined
        form.value.assigneeName = undefined
      }
      if (form.value.planId != undefined) { updatePmplan(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addPmplan(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const planIds = row.planId || ids.value; proxy.$modal.confirm('确认删除编号为"' + planIds + '"的数据？').then(() => delPmplan(planIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleGenerate(row) {
  proxy.$modal.confirm('确认根据计划「' + row.planName + '」生成工单？').then(() => {
    return generateWorkOrder(row.planId)
  }).then(() => {
    proxy.$modal.msgSuccess('工单已生成')
    getList()
  }).catch((err) => {
    if (err !== 'cancel' && err !== undefined && err !== null) {
      proxy.$modal.msgError(typeof err === 'string' ? err : (err.msg || err.message || '生成工单失败'))
    }
  })
}
function handleAutoGenerate() {
  proxy.$modal.confirm('确认立即执行PM计划自动生成？将扫描所有到期计划并自动生成工单。').then(() => {
    return autoGeneratePm()
  }).then((res) => {
    proxy.$modal.msgSuccess(res.msg || '执行完成')
    getList()
  }).catch(() => {})
}
function cancel() { open.value = false; reset() }

// ===== 日历视图 =====
function loadCalendarData() {
  getPmplanCalendar().then(res => { calendarData.value = res.data || [] })
}
function getCalendarItems(dayStr) {
  return calendarData.value.filter(item => {
    if (!item.nextExecuteTime) return false
    return formatDate(item.nextExecuteTime).startsWith(dayStr)
  })
}
function handleCalendarClick(item) {
  handleUpdate(item)
}
function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}
function isExpired(row) {
  if (!row.nextExecuteTime) return false
  return new Date(row.nextExecuteTime) < new Date()
}
function isExpiredByDate(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

// 监听视图切换
watch(viewMode, (val) => {
  if (val === 'calendar' || val === 'timeline') {
    loadCalendarData()
  }
})

getEquipmentOptions()
getUserList()
getSparePartList()
getList()
</script>

<style scoped>
.is-selected {
  color: #1989fa;
  font-weight: bold;
}
</style>
