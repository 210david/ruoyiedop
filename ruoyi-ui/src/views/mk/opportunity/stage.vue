<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:stage:add']">新增</el-button></el-col>
      <right-toolbar @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd">
      <el-table-column label="阶段编码" prop="stageCode" :width="colWidth('stageCode', 120)" resizable />
      <el-table-column label="阶段名称" prop="stageName" :width="colWidth('stageName', 120)" resizable />
      <el-table-column label="排序" prop="sort" :width="colWidth('sort', 80)" resizable align="center" />
      <el-table-column label="赢率(%)" prop="winRate" :width="colWidth('winRate', 100)" resizable align="center" />
      <el-table-column label="最大停留天数" prop="maxDays" :width="colWidth('maxDays', 120)" resizable align="center" />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:stage:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:stage:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="stageRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="阶段编码" prop="stageCode"><el-input v-model="form.stageCode" placeholder="请输入阶段编号" /></el-form-item>
        <el-form-item label="阶段名称" prop="stageName"><el-input v-model="form.stageName" placeholder="请输入阶段名称" /></el-form-item>
        <el-form-item label="排序" prop="sort"><el-input-number v-model="form.sort" :min="1" controls-position="right" /></el-form-item>
        <el-form-item label="赢率(%)" prop="winRate"><el-input-number v-model="form.winRate" :min="0" :max="100" controls-position="right" /></el-form-item>
        <el-form-item label="最大停留天数" prop="maxDays"><el-input-number v-model="form.maxDays" :min="1" controls-position="right" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">阶段详情</span>
        </div>
      </template>
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">阶段编码</span><div class="rd-value">{{ viewForm.stageCode }}</div></div>
        <div class="rd-item"><span class="rd-label">阶段名称</span><div class="rd-value">{{ viewForm.stageName }}</div></div>
        <div class="rd-item"><span class="rd-label">排序</span><div class="rd-value">{{ viewForm.sort }}</div></div>
        <div class="rd-item"><span class="rd-label">赢率(%)</span><div class="rd-value">{{ viewForm.winRate }}</div></div>
        <div class="rd-item"><span class="rd-label">最大停留天数</span><div class="rd-value">{{ viewForm.maxDays }}</div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewForm.status === '0' ? '正常' : '停用' }}</div></div>
        <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark }}</div></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkOpportunityStage">
import { listStage, getStage, addStage, updateStage, delStage } from '@/api/mk/stage'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_opportunity_stage')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const total = ref(0)
const title = ref('')
const viewForm = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 9999 },
  rules: {
    stageCode: [{ required: true, message: '阶段编码不能为空', trigger: 'blur' }],
    stageName: [{ required: true, message: '阶段名称不能为空', trigger: 'blur' }],
    sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listStage(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { getList() }
function resetQuery() { handleQuery() }
function reset() { form.value = { stageCode: undefined, stageName: undefined, sort: 1, winRate: 0, maxDays: 30, status: '0', remark: undefined }; proxy.resetForm('stageRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增销售阶段' }
function handleUpdate(row) { reset(); getStage(row.stageId).then(res => { form.value = res.data; open.value = true; title.value = '修改销售阶段' }) }
function handleView(row) { getStage(row.stageId).then(res => { viewForm.value = res.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['stageRef'].validate(valid => {
    if (valid) {
      if (form.value.stageId != undefined) { updateStage(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addStage(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const stageIds = row.stageId; proxy.$modal.confirm('确认删除阶段"' + row.stageName + '"的数据？').then(() => delStage(stageIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getList()
</script>
