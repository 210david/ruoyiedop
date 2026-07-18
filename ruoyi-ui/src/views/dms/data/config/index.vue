<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="设备名称" prop="equipmentName">
        <el-input v-model="queryParams.equipmentName" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:data:config:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:data:config:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:data:config:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="设备名称" prop="equipmentName" show-overflow-tooltip />
      <el-table-column label="采集模式" prop="collectMode" :width="colWidth('collectMode', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_collect_mode" :value="scope.row.collectMode" /></template>
      </el-table-column>
      <el-table-column label="参数1" width="120"><template #default="scope">{{ scope.row.param1Name }} ({{ scope.row.param1Unit }})</template></el-table-column>
      <el-table-column label="参数2" width="120"><template #default="scope">{{ scope.row.param2Name }} ({{ scope.row.param2Unit }})</template></el-table-column>
      <el-table-column label="参数3" width="120"><template #default="scope">{{ scope.row.param3Name }} ({{ scope.row.param3Unit }})</template></el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:data:config:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:data:config:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="1000px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="90px">
        <el-divider content-position="center">设备信息</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="关联设备" prop="equipmentId">
            <el-select v-model="form.equipmentId" filterable clearable placeholder="请选择设备" style="width: 100%" @change="onEquipmentChange">
              <el-option v-for="e in equipmentOptions" :key="e.equipmentId" :label="e.equipmentCode + ' - ' + e.equipmentName" :value="e.equipmentId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备名称" prop="equipmentName"><el-input v-model="form.equipmentName" placeholder="自动带出" disabled /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="采集模式" prop="collectMode">
            <el-select v-model="form.collectMode" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in dms_collect_mode" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="8"><el-form-item label="状态采集" prop="collectStatus"><el-switch v-model="form.collectStatus" active-value="1" inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="工时采集" prop="collectHours"><el-switch v-model="form.collectHours" active-value="1" inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="产量采集" prop="collectCount"><el-switch v-model="form.collectCount" active-value="1" inactive-value="0" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="center">自定义参数配置</el-divider>
        <el-row v-for="i in 3" :key="i">
          <el-col :span="6"><el-form-item :label="'参数'+i+'名称'" :prop="'param'+i+'Name'"><el-input v-model="form['param'+i+'Name']" placeholder="如：温度" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item :label="'单位'" :prop="'param'+i+'Unit'"><el-input v-model="form['param'+i+'Unit']" placeholder="如：℃" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item :label="'告警下限'" :prop="'param'+i+'AlarmMin'"><el-input-number v-model="form['param'+i+'AlarmMin']" controls-position="right" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item :label="'告警上限'" :prop="'param'+i+'AlarmMax'"><el-input-number v-model="form['param'+i+'AlarmMax']" controls-position="right" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="center">其他</el-divider>
        <el-row>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsDataConfig">
import { listConfig, getConfig, addConfig, updateConfig, delConfig } from '@/api/dms/data'
import { listEquipment } from '@/api/dms/equipment'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd } = useColumnResize('dms_data_config_index')
const { dms_collect_mode } = proxy.useDict('dms_collect_mode')

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
  queryParams: { pageNum: 1, pageSize: 10, equipmentName: undefined },
  rules: {
    equipmentName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
    collectMode: [{ required: true, message: '采集模式不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listConfig(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getEquipmentOptions() { listEquipment({ pageNum: 1, pageSize: 9999 }).then(res => { equipmentOptions.value = res.rows }) }
function onEquipmentChange(equipmentId) {
  if (equipmentId) {
    const equipment = equipmentOptions.value.find(e => e.equipmentId === equipmentId)
    if (equipment) { form.value.equipmentName = equipment.equipmentName }
  } else {
    form.value.equipmentName = undefined
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.configId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { equipmentId: undefined, equipmentName: undefined, collectMode: '0', collectStatus: '1', collectHours: '1', collectCount: '0', param1Name: undefined, param1Unit: undefined, param1AlarmMin: undefined, param1AlarmMax: undefined, param2Name: undefined, param2Unit: undefined, param2AlarmMin: undefined, param2AlarmMax: undefined, param3Name: undefined, param3Unit: undefined, param3AlarmMin: undefined, param3AlarmMax: undefined, status: '0', remark: undefined }; proxy.resetForm('configRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增采集配置' }
function handleUpdate(row) { reset(); getConfig(row.configId || ids.value[0]).then(res => { form.value = res.data; if (form.value.equipmentId) { const equipment = equipmentOptions.value.find(e => e.equipmentId === form.value.equipmentId); if (equipment) { form.value.equipmentName = equipment.equipmentName } } open.value = true; title.value = '修改采集配置' }) }
function submitForm() {
  proxy.$refs['configRef'].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) { updateConfig(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addConfig(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const configIds = row.configId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delConfig(configIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getEquipmentOptions()
getList()
</script>
