<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="设备名称" prop="equipmentName">
        <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="运行状态" prop="runStatus">
        <el-select v-model="queryParams.runStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_run_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:data:record:add']">录入</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:data:record:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:data:record:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="设备编码" prop="equipmentCode" min-width="140" show-overflow-tooltip />
      <el-table-column label="设备名称" prop="equipmentName" min-width="140" show-overflow-tooltip />
      <el-table-column label="运行状态" prop="runStatus" :width="colWidth('runStatus', 90)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_run_status" :value="scope.row.runStatus" /></template>
      </el-table-column>
      <el-table-column label="运行小时" prop="runHours" :width="colWidth('runHours', 90)" resizable align="center" />
      <el-table-column label="加工件数" prop="productCount" :width="colWidth('productCount', 90)" resizable align="center" />
      <el-table-column label="参数1" prop="param1Value" :width="colWidth('param1Value', 80)" resizable align="center" />
      <el-table-column label="参数2" prop="param2Value" :width="colWidth('param2Value', 80)" resizable align="center" />
      <el-table-column label="参数3" prop="param3Value" :width="colWidth('param3Value', 80)" resizable align="center" />
      <el-table-column label="采集方式" prop="collectMode" :width="colWidth('collectMode', 90)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_collect_mode" :value="scope.row.collectMode" /></template>
      </el-table-column>
      <el-table-column label="采集时间" prop="collectTime" :width="colWidth('collectTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="130" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:data:record:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:data:record:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联设备" prop="equipmentId">
          <el-select v-model="form.equipmentId" filterable clearable placeholder="请选择设备" style="width: 100%" @change="onEquipmentChange">
            <el-option v-for="e in equipmentOptions" :key="e.equipmentId" :label="e.equipmentCode + ' - ' + e.equipmentName" :value="e.equipmentId" />
          </el-select>
        </el-form-item>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="设备编码"><el-input v-model="form.equipmentCode" placeholder="自动带出" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称"><el-input v-model="form.equipmentName" placeholder="自动带出" disabled /></el-form-item></el-col>
        </el-row>
        <el-form-item label="运行状态" prop="runStatus">
          <el-select v-model="form.runStatus" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in dms_run_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="运行小时" prop="runHours"><el-input-number v-model="form.runHours" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="加工件数" prop="productCount"><el-input-number v-model="form.productCount" :min="0" :precision="0" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="参数1值" prop="param1Value"><el-input-number v-model="form.param1Value" :precision="2" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="参数2值" prop="param2Value"><el-input-number v-model="form.param2Value" :precision="2" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="参数3值" prop="param3Value"><el-input-number v-model="form.param3Value" :precision="2" controls-position="right" style="width: 100%" /></el-form-item>
        <el-form-item label="采集时间" prop="collectTime"><el-date-picker v-model="form.collectTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsDataRecord">
import { listRecord, getRecord, addRecord, updateRecord, delRecord } from '@/api/dms/data'
import { listEquipment } from '@/api/dms/equipment'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_data_record_index')
const { dms_run_status, dms_collect_mode } = proxy.useDict('dms_run_status', 'dms_collect_mode')

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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, equipmentName: undefined, runStatus: undefined },
  rules: {
    equipmentName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
    runStatus: [{ required: true, message: '运行状态不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listRecord(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getEquipmentOptions() { listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows }) }
function onEquipmentChange(equipmentId) {
  if (equipmentId) {
    const equipment = equipmentOptions.value.find(e => e.equipmentId === equipmentId)
    if (equipment) {
      form.value.equipmentCode = equipment.equipmentCode
      form.value.equipmentName = equipment.equipmentName
    }
  } else {
    form.value.equipmentCode = undefined
    form.value.equipmentName = undefined
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { equipmentId: undefined, equipmentCode: undefined, equipmentName: undefined, runStatus: '0', runHours: 0, productCount: 0, param1Value: undefined, param2Value: undefined, param3Value: undefined, collectTime: new Date(), collectMode: '0' }; proxy.resetForm('recordRef') }
function handleAdd() { reset(); title.value = '录入数据'; open.value = true }
function handleUpdate(row) { reset(); getRecord(row.recordId || ids.value[0]).then(res => { form.value = res.data; if (form.value.equipmentId) { const equipment = equipmentOptions.value.find(e => e.equipmentId === form.value.equipmentId); if (equipment) { form.value.equipmentCode = equipment.equipmentCode; form.value.equipmentName = equipment.equipmentName } } title.value = '修改数据'; open.value = true }) }
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
getEquipmentOptions()
getList()
</script>
