<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="任务编号" prop="taskNo">
        <el-input v-model="queryParams.taskNo" placeholder="请输入" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="任务状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in dms_inspection_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:inspection:task:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:inspection:task:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" prop="taskNo" :width="colWidth('taskNo', 180)" resizable />
      <el-table-column label="路线名称" prop="routeName" show-overflow-tooltip />
      <el-table-column label="计划日期" prop="planDate" :width="colWidth('planDate', 120)" resizable align="center" />
      <el-table-column label="点检人" prop="inspectorName" :width="colWidth('inspectorName', 80)" resizable />
      <el-table-column label="异常项" prop="abnormalCount" :width="colWidth('abnormalCount', 70)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.abnormalCount > 0 ? 'danger' : 'success'">{{ scope.row.abnormalCount || 0 }}</el-tag></template>
      </el-table-column>
      <el-table-column label="状态" prop="taskStatus" :width="colWidth('taskStatus', 90)" resizable align="center">
        <template #default="scope"><dict-tag :options="dms_inspection_status" :value="scope.row.taskStatus" /></template>
      </el-table-column>
      <el-table-column label="完成时间" prop="completeTime" :width="colWidth('completeTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'" link type="primary" icon="VideoPlay" @click="handleExecute(scope.row)">执行点检</el-button>
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:inspection:task:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增弹窗 -->
    <el-dialog v-model="open" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12"><el-form-item label="任务编号" prop="taskNo"><el-input v-model="form.taskNo" placeholder="自动生成" disabled /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="巡检路线" prop="routeId">
            <el-select v-model="form.routeId" filterable placeholder="请选择路线" style="width: 100%" @change="onRouteChange">
              <el-option v-for="r in routeOptions" :key="r.routeId" :label="r.routeName" :value="r.routeId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="点检人" prop="inspectorId">
            <el-select v-model="form.inspectorId" filterable placeholder="请选择点检人" style="width: 100%" @change="onInspectorChange">
              <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
            </el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="850px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">点检详情</span>
        </div>
      </template>
      <div class="rd-grid">
        <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ viewForm.taskNo }}</div></div>
        <div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ viewForm.routeName }}</div></div>
        <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ viewForm.planDate }}</div></div>
        <div class="rd-item"><span class="rd-label">点检人</span><div class="rd-value">{{ viewForm.inspectorName }}</div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="dms_inspection_status" :value="viewForm.taskStatus" /></div></div>
        <div class="rd-item"><span class="rd-label">异常项数</span><div class="rd-value">{{ viewForm.abnormalCount || 0 }}</div></div>
        <div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewForm.startTime }}</div></div>
        <div class="rd-item"><span class="rd-label">完成时间</span><div class="rd-value">{{ viewForm.completeTime }}</div></div>
      </div>

      <template v-if="viewGroups.common.length > 0">
        <el-divider content-position="left">通用检查项</el-divider>
        <el-table :data="viewGroups.common" border size="small" @header-dragend="onHeaderDragEnd">
          <el-table-column label="序号" width="60" align="center" type="index" />
          <el-table-column label="检查项" prop="item" />
          <el-table-column label="结果" width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.abnormal" type="danger" size="small">异常</el-tag>
              <span v-else-if="scope.row.value !== undefined">{{ scope.row.value }}{{ scope.row.unit ? ' ' + scope.row.unit : '' }}</span>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" prop="abnormalDesc" show-overflow-tooltip />
        </el-table>
      </template>

      <template v-for="(dg, di) in viewGroups.devices" :key="di">
        <el-divider content-position="left">{{ dg.equipmentName }}</el-divider>
        <el-table :data="dg.items" border size="small" @header-dragend="onHeaderDragEnd">
          <el-table-column label="序号" width="60" align="center" type="index" />
          <el-table-column label="检查项" prop="item" />
          <el-table-column label="结果" width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.abnormal" type="danger" size="small">异常</el-tag>
              <span v-else-if="scope.row.value !== undefined">{{ scope.row.value }}{{ scope.row.unit ? ' ' + scope.row.unit : '' }}</span>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" prop="abnormalDesc" show-overflow-tooltip />
        </el-table>
      </template>

      <div v-if="viewForm.photoUrls" style="margin-top: 12px">
        <el-divider>点检照片</el-divider>
        <div v-for="(url, i) in parsePhotos(viewForm.photoUrls)" :key="i" style="display: inline-block; margin-right: 8px">
          <el-image :src="url" style="width: 120px; height: 120px" fit="cover" :preview-src-list="parsePhotos(viewForm.photoUrls)" />
        </div>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 执行点检弹窗 -->
    <el-dialog v-model="execOpen" width="960px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">执行点检</span>
        </div>
      </template>
      <el-descriptions :column="2" border style="margin-bottom: 16px">
        <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ execForm.taskNo }}</div></div>
        <div class="rd-item"><span class="rd-label">路线名称</span><div class="rd-value">{{ execForm.routeName }}</div></div>
        <div class="rd-item"><span class="rd-label">点检人</span><div class="rd-value">{{ execForm.inspectorName }}</div></div>
        <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ execForm.planDate }}</div></div>
      </div>

      <!-- 通用检查项 -->
      <template v-if="execGroups.common.length > 0">
        <el-divider content-position="left">通用检查项</el-divider>
        <el-table :data="execGroups.common" border style="width: 100%" @header-dragend="onHeaderDragEnd">
          <el-table-column label="序号" width="60" align="center" type="index" />
          <el-table-column label="检查项" prop="item" min-width="180">
            <template #default="scope"><span style="font-weight: 500">{{ scope.row.item }}</span></template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template #default="scope"><el-tag size="small" :type="scope.row.type === 'number' ? 'warning' : 'info'">{{ typeText(scope.row.type) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="检查结果" min-width="260" align="center">
            <template #default="scope">
              <div v-if="scope.row.type === 'check'" style="display: flex; align-items: center; justify-content: center; gap: 12px">
                <el-radio-group v-model="scope.row.result">
                  <el-radio-button value="ok">正常</el-radio-button>
                  <el-radio-button value="abnormal">异常</el-radio-button>
                </el-radio-group>
              </div>
              <div v-else-if="scope.row.type === 'number'" style="display: flex; align-items: center; justify-content: center; gap: 10px">
                <el-input-number v-model="scope.row.value" :controls="false" style="width: 140px" placeholder="输入数值" />
                <span style="color: #999; min-width: 30px">{{ scope.row.unit }}</span>
                <el-checkbox v-model="scope.row.abnormal">异常</el-checkbox>
              </div>
              <div v-else style="display: flex; flex-direction: column; gap: 6px">
                <el-input v-model="scope.row.value" placeholder="输入文本" />
                <el-checkbox v-model="scope.row.abnormal">标记为异常</el-checkbox>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" min-width="240">
            <template #default="scope">
              <el-input v-model="scope.row.abnormalDesc" type="textarea" :rows="2"
                :placeholder="(scope.row.abnormal || scope.row.result === 'abnormal') ? '必填：请描述异常情况' : '无异常时可不填'"
                :required="scope.row.abnormal || scope.row.result === 'abnormal'" />
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 设备明细分组 -->
      <template v-for="(dg, di) in execGroups.devices" :key="di">
        <el-divider content-position="left">{{ dg.equipmentName }}</el-divider>
        <el-table :data="dg.items" border style="width: 100%" @header-dragend="onHeaderDragEnd">
          <el-table-column label="序号" width="60" align="center" type="index" />
          <el-table-column label="检查项" prop="item" min-width="180">
            <template #default="scope"><span style="font-weight: 500">{{ scope.row.item }}</span></template>
          </el-table-column>
          <el-table-column label="类型" width="90" align="center">
            <template #default="scope"><el-tag size="small" :type="scope.row.type === 'number' ? 'warning' : 'info'">{{ typeText(scope.row.type) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="检查结果" min-width="260" align="center">
            <template #default="scope">
              <div v-if="scope.row.type === 'check'" style="display: flex; align-items: center; justify-content: center; gap: 12px">
                <el-radio-group v-model="scope.row.result">
                  <el-radio-button value="ok">正常</el-radio-button>
                  <el-radio-button value="abnormal">异常</el-radio-button>
                </el-radio-group>
              </div>
              <div v-else-if="scope.row.type === 'number'" style="display: flex; align-items: center; justify-content: center; gap: 10px">
                <el-input-number v-model="scope.row.value" :controls="false" style="width: 140px" placeholder="输入数值" />
                <span style="color: #999; min-width: 30px">{{ scope.row.unit }}</span>
                <el-checkbox v-model="scope.row.abnormal">异常</el-checkbox>
              </div>
              <div v-else style="display: flex; flex-direction: column; gap: 6px">
                <el-input v-model="scope.row.value" placeholder="输入文本" />
                <el-checkbox v-model="scope.row.abnormal">标记为异常</el-checkbox>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="异常说明" min-width="240">
            <template #default="scope">
              <el-input v-model="scope.row.abnormalDesc" type="textarea" :rows="2"
                :placeholder="(scope.row.abnormal || scope.row.result === 'abnormal') ? '必填：请描述异常情况' : '无异常时可不填'"
                :required="scope.row.abnormal || scope.row.result === 'abnormal'" />
            </template>
          </el-table-column>
        </el-table>
      </template>

      <div v-if="execGroups.common.length === 0 && execGroups.devices.length === 0" style="text-align: center; color: #999; padding: 24px 0">
        该路线未配置点检项，请先在巡检路线中配置
      </div>

      <!-- 照片上传 -->
      <el-divider>现场照片</el-divider>
      <el-upload v-model:file-list="execPhotoList" :action="uploadUrl" :headers="uploadHeaders" list-type="picture-card"
        :on-success="handlePhotoSuccess" :before-upload="beforePhotoUpload" accept="image/*" :limit="6">
        <el-icon><Plus /></el-icon>
      </el-upload>
      <p style="color: #999; font-size: 12px">最多上传6张照片，单张不超过10MB，支持 JPG/PNG 格式</p>

      <template #footer>
        <el-button type="primary" @click="submitExecute" :loading="execSaving">提交点检结果</el-button>
        <el-button @click="execOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsInspectionTask">
import { Plus } from '@element-plus/icons-vue'
import { listTask, getTask, addTask, updateTask, delTask, completeTask, startTask } from '@/api/dms/inspection'
import { listRoute } from '@/api/dms/inspection'
import { listUser } from '@/api/system/user'
import { getToken } from '@/utils/auth'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_inspection_task_index')
const { dms_inspection_status } = proxy.useDict('dms_inspection_status')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const execOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const routeOptions = ref([])
const userOptions = ref([])
const execSaving = ref(false)

const viewForm = ref({})
const viewGroups = ref({ common: [], devices: [] })
const execForm = ref({})
const execGroups = ref({ common: [], devices: [] })
const execPhotoList = ref([])
const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/common/upload')
const uploadHeaders = ref({ Authorization: 'Bearer ' + getToken() })

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, taskNo: undefined, taskStatus: undefined },
  rules: {
    routeId: [{ required: true, message: '请选择巡检路线', trigger: 'change' }],
    inspectorId: [{ required: true, message: '请选择点检人', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listTask(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getRouteOptions() { listRoute({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { routeOptions.value = res.rows }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.taskId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = { routeId: undefined, routeName: undefined, planDate: new Date().toISOString().slice(0, 10), inspectorId: undefined, inspectorName: undefined, taskStatus: '0', remark: undefined }
  proxy.resetForm('taskRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增点检任务' }
function onRouteChange(routeId) {
  const route = routeOptions.value.find(r => r.routeId === routeId)
  if (route) form.value.routeName = route.routeName
}
function onInspectorChange(userId) {
  const user = userOptions.value.find(u => u.userId === userId)
  if (user) form.value.inspectorName = user.nickName
}

function submitForm() {
  proxy.$refs['taskRef'].validate(valid => {
    if (valid) { addTask(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
  })
}
function handleDelete(row) { const taskIds = row.taskId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delTask(taskIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }

// ===== 查看详情 =====
function handleView(row) {
  getTask(row.taskId).then(res => {
    viewForm.value = res.data
    viewGroups.value = { common: [], devices: [] }
    if (res.data.resultData) {
      try {
        const parsed = JSON.parse(res.data.resultData)
        // 兼容旧格式（扁平数组）
        if (Array.isArray(parsed)) {
          viewGroups.value = { common: parsed, devices: [] }
        } else {
          viewGroups.value = {
            common: parsed.common || [],
            devices: parsed.devices || []
          }
        }
      } catch (e) { viewGroups.value = { common: [], devices: [] } }
    }
    viewOpen.value = true
  })
}

// ===== 执行点检 =====
function handleExecute(row) {
  // 调用后端开始接口，记录开始时间并将状态改为执行中
  startTask(row.taskId).then(() => {
    getTask(row.taskId).then(res => {
      execForm.value = res.data
      execPhotoList.value = []
      execGroups.value = { common: [], devices: [] }

      // 从路线获取点检项模板
      const route = routeOptions.value.find(r => r.routeId === res.data.routeId)
      if (route && route.inspectionItems) {
        try {
          const parsed = JSON.parse(route.inspectionItems)
          // 兼容旧格式（扁平数组）
          if (Array.isArray(parsed)) {
            execGroups.value = { common: initItems(parsed), devices: [] }
          } else {
            execGroups.value = {
              common: initItems(parsed.common || []),
              devices: (parsed.devices || []).map(d => ({
                equipmentId: d.equipmentId,
                equipmentName: d.equipmentName,
                items: initItems(d.items || [])
              }))
            }
          }
        } catch (e) { execGroups.value = { common: [], devices: [] } }
      }
      // 如果没有模板，给一个默认通用项
      if (execGroups.value.common.length === 0 && execGroups.value.devices.length === 0) {
        execGroups.value.common = [{ item: '通用检查', type: 'check', unit: '', result: 'ok', value: undefined, abnormal: false, abnormalDesc: '' }]
      }

      // 恢复之前的结果数据
      if (res.data.resultData) {
        try {
          const prev = JSON.parse(res.data.resultData)
          if (Array.isArray(prev)) {
            prev.forEach((r, i) => { if (execGroups.value.common[i]) restoreItem(execGroups.value.common[i], r) })
          } else {
            (prev.common || []).forEach((r, i) => { if (execGroups.value.common[i]) restoreItem(execGroups.value.common[i], r) })
            ;(prev.devices || []).forEach((d, di) => {
              if (execGroups.value.devices[di]) {
                d.items.forEach((r, ii) => { if (execGroups.value.devices[di].items[ii]) restoreItem(execGroups.value.devices[di].items[ii], r) })
              }
            })
          }
        } catch (e) {}
      }

      execOpen.value = true
      // 刷新列表以显示状态变化
      getList()
    })
  }).catch(() => {})
}

function initItems(items) {
  return items.map(item => ({
    item: item.item || '',
    type: item.type || 'check',
    unit: item.unit || '',
    abnormalRequired: item.abnormalRequired || false,
    result: 'ok',
    value: undefined,
    abnormal: false,
    abnormalDesc: ''
  }))
}
function restoreItem(target, source) {
  target.result = source.result || (source.abnormal ? 'abnormal' : 'ok')
  target.value = source.value
  target.abnormal = source.abnormal || false
  target.abnormalDesc = source.abnormalDesc || ''
}

function handlePhotoSuccess(res) {}
function beforePhotoUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) { proxy.$modal.msgError('只能上传图片文件'); return false }
  if (!isLt10M) { proxy.$modal.msgError('图片大小不能超过10MB'); return false }
  return true
}

function submitExecute() {
  let abnormalCount = 0
  // 校验所有组的检查结果必填
  const allGroups = [...execGroups.value.common, ...execGroups.value.devices.flatMap(d => d.items)]
  for (const item of allGroups) {
    // 检查结果必填校验
    if (item.type === 'check') {
      if (!item.result) {
        proxy.$modal.msgError(`检查项「${item.item}」请选择检查结果（正常/异常）`)
        return
      }
    } else if (item.type === 'number') {
      if (item.value === undefined || item.value === null || item.value === '') {
        proxy.$modal.msgError(`检查项「${item.item}」请填写检查数值`)
        return
      }
    } else {
      if (item.value === undefined || item.value === null || item.value === '') {
        proxy.$modal.msgError(`检查项「${item.item}」请填写检查结果`)
        return
      }
    }
    // 异常项必须有异常说明
    const isAbnormal = item.abnormal || item.result === 'abnormal'
    if (isAbnormal) {
      abnormalCount++
      if (!item.abnormalDesc || item.abnormalDesc.trim() === '') {
        proxy.$modal.msgError(`检查项「${item.item}」标记为异常，请填写异常说明`)
        return
      }
    }
  }

  execSaving.value = true
  // 构建嵌套结构的结果数据
  const resultData = {
    common: execGroups.value.common.map(item => ({
      item: item.item, type: item.type, unit: item.unit,
      result: item.result, value: item.value,
      abnormal: item.abnormal || item.result === 'abnormal',
      abnormalDesc: item.abnormalDesc
    })),
    devices: execGroups.value.devices.map(dg => ({
      equipmentId: dg.equipmentId,
      equipmentName: dg.equipmentName,
      items: dg.items.map(item => ({
        item: item.item, type: item.type, unit: item.unit,
        result: item.result, value: item.value,
        abnormal: item.abnormal || item.result === 'abnormal',
        abnormalDesc: item.abnormalDesc
      }))
    }))
  }

  const photoUrls = execPhotoList.value.map(f => f.response?.fileName || f.response?.url || f.url).filter(u => u)

  const submitData = {
    taskId: execForm.value.taskId,
    taskNo: execForm.value.taskNo,
    taskStatus: '2',
    abnormalCount: abnormalCount,
    resultData: JSON.stringify(resultData),
    photoUrls: photoUrls.length > 0 ? JSON.stringify(photoUrls) : null,
    inspectorId: execForm.value.inspectorId,
    inspectorName: execForm.value.inspectorName
  }

  completeTask(submitData).then(() => {
    proxy.$modal.msgSuccess(abnormalCount > 0 ? `点检完成，发现${abnormalCount}个异常项，已自动生成整改工单` : '点检完成，一切正常')
    execOpen.value = false
    execSaving.value = false
    getList()
  }).catch(() => { execSaving.value = false })
}

function typeText(type) { return { check: '打勾', number: '数值', text: '文本' }[type] || type }
function parsePhotos(photoUrls) {
  if (!photoUrls) return []
  try {
    const urls = JSON.parse(photoUrls)
    const baseApi = import.meta.env.VITE_APP_BASE_API
    return urls.map(u => {
      if (u && !u.startsWith('http') && !u.startsWith('data:')) return baseApi + u
      return u
    })
  } catch (e) { return [] }
}

getRouteOptions()
getUserOptions()
getList()
</script>
