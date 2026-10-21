<template>
  <div class="app-container flowable-leave-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>请假标题</label>
          <div class="control">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>请假类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.leaveType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="事假" value="1" /><el-option label="病假" value="2" />
              <el-option label="年假" value="3" /><el-option label="调休" value="4" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="草稿" value="0" /><el-option label="审批中" value="1" />
              <el-option label="已通过" value="2" /><el-option label="已驳回" value="3" />
              <el-option label="已撤销" value="4" />
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

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button class="status-tab tab-draft" :class="{ 'is-active': activeStatusTab === '0' }" @click="handleStatusTabClick('0')">
            <span class="dot"></span><span>草稿</span><span class="count">{{ statusCounts['0'] }}</span>
          </button>
          <button class="status-tab tab-audit" :class="{ 'is-active': activeStatusTab === '1' }" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>审批中</span><span class="count">{{ statusCounts['1'] }}</span>
          </button>
          <button class="status-tab tab-approved" :class="{ 'is-active': activeStatusTab === '2' }" @click="handleStatusTabClick('2')">
            <span class="dot"></span><span>已通过</span><span class="count">{{ statusCounts['2'] }}</span>
          </button>
          <button class="status-tab tab-reject" :class="{ 'is-active': activeStatusTab === '3' }" @click="handleStatusTabClick('3')">
            <span class="dot"></span><span>已驳回</span><span class="count">{{ statusCounts['3'] }}</span>
          </button>
          <button class="status-tab tab-void" :class="{ 'is-active': activeStatusTab === '4' }" @click="handleStatusTabClick('4')">
            <span class="dot"></span><span>已撤销</span><span class="count">{{ statusCounts['4'] }}</span>
          </button>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:leave:add']">新增请假</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_leave_columns" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table v-loading="loading" :data="list" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="请假标题" prop="title" min-width="150" show-overflow-tooltip>
            <template #default="scope"><span class="col-strong">{{ scope.row.title }}</span></template>
          </el-table-column>
          <el-table-column label="类型" prop="leaveType" width="90" align="center">
            <template #default="scope">
              <span class="badge violet">{{ leaveTypeLabel(scope.row.leaveType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" prop="startDate" width="110" align="center" />
          <el-table-column label="结束日期" prop="endDate" width="110" align="center" />
          <el-table-column label="天数" prop="days" width="80" align="right" class-name="col-num" />
          <el-table-column label="状态" prop="status" width="100" align="center">
            <template #default="scope">
              <span class="badge" :class="statusBadge(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="审批意见" prop="auditOpinion" min-width="140" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.auditOpinion || '-' }}</template>
          </el-table-column>
          <el-table-column label="申请时间" prop="createTime" width="170" align="center" />
          <el-table-column label="操作" width="240" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="success" icon="Promotion" @click="handleSubmit(scope.row)"
                  v-if="scope.row.status === '0' || scope.row.status === '3'" v-hasPermi="['flowable:leave:submit']">提交</el-button>
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)"
                  v-if="scope.row.status === '0' || scope.row.status === '3'" v-hasPermi="['flowable:leave:remove']">删除</el-button>
                <el-button link type="warning" icon="CircleClose" @click="handleCancel(scope.row)"
                  v-if="scope.row.status === '1'" v-hasPermi="['flowable:process:cancel']">撤销</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 新增/修改弹窗 ===== -->
    <el-dialog v-model="open" width="640px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="leaveRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="请假标题" prop="title">
          <el-input v-model="form.title" placeholder="如：年假-家庭旅行" maxlength="50" />
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" style="width: 100%">
            <el-option label="事假" value="1" /><el-option label="病假" value="2" />
            <el-option label="年假" value="3" /><el-option label="调休" value="4" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="天数" prop="days">
              <el-input-number v-model="form.days" :min="0.5" :step="0.5" :precision="1" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入请假事由" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 提交审批弹窗 ===== -->
    <el-dialog v-model="submitOpen" width="480px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </div>
          <span class="rd-detail-header-title">提交审批</span>
          <div class="rd-detail-header-sub" v-if="submitRow.title">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ submitRow.title }}</span>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <el-form-item label="审批人">
          <el-select v-model="approverUserId" placeholder="请选择审批人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="u.userId" />
          </el-select>
          <div style="font-size:12px;color:#94a3b8;margin-top:4px">流程将发起至所选审批人的待办</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="doSubmit">发起审批</el-button>
        <el-button @click="submitOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 详情弹窗（业务信息+流程图+流转记录） ===== -->
    <el-dialog v-model="detailOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </div>
          <span class="rd-detail-header-title">请假详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.title">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ detailData.title }}</span>
            <span class="badge" :class="statusBadge(detailData.status)"><span class="dot"></span>{{ statusLabel(detailData.status) }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon">i</span>请假信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">请假标题</span><div class="rd-value">{{ detailData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">请假类型</span><div class="rd-value">{{ leaveTypeLabel(detailData.leaveType) }}</div></div>
              <div class="rd-item"><span class="rd-label">请假天数</span><div class="rd-value">{{ detailData.days || '-' }} 天</div></div>
              <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ detailData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ detailData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ detailData.startUserName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">请假事由</span><div class="rd-value">{{ detailData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">审批意见</span><div class="rd-value">{{ detailData.auditOpinion || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="detailData.instanceId">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon">◈</span>流程图</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="diagram-wrap">
              <img v-if="diagramUrl" :src="diagramUrl" class="diagram-img" alt="流程图" />
              <div v-else class="diagram-empty">流程图加载中…</div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="detailData.instanceId">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon">≡</span>流转记录</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="t in historyTasks" :key="t.taskId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': t.endTime, 'rd-timeline-dot--active': !t.endTime }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ t.taskName }}</span>
                    <span class="rd-timeline-time">{{ t.createTime }}</span>
                  </div>
                  <div class="rd-timeline-comment" v-if="t.comment"><strong>审批意见：</strong>{{ t.comment }}</div>
                </div>
              </div>
              <div class="rd-empty" v-if="!historyTasks.length"><p class="rd-empty-text">暂无流转记录</p></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowLeave">
import { listLeave, getLeave, addLeave, updateLeave, delLeave, submitLeave } from '@/api/flowable/leave'
import request from '@/utils/request'
import { Search, Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 })
const open = ref(false)
const title = ref('')
const submitOpen = ref(false)
const submitRow = ref({})
const approverUserId = ref(undefined)
const userOptions = ref([])
const detailOpen = ref(false)
const detailData = ref({})
const historyTasks = ref([])
const diagramUrl = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, leaveType: undefined, status: undefined, params: {} },
  rules: {
    title: [{ required: true, message: '请输入请假标题', trigger: 'blur' }],
    startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
    endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
    days: [{ required: true, message: '请输入请假天数', trigger: 'blur' }],
    reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.title) count++
  if (queryParams.value.leaveType) count++
  if (queryParams.value.status) count++
  return count
})

const defaultColumns = {
  title: { label: '请假标题', visible: true },
  leaveType: { label: '类型', visible: true },
  startDate: { label: '开始日期', visible: true },
  endDate: { label: '结束日期', visible: true },
  days: { label: '天数', visible: true },
  status: { label: '状态', visible: true },
  auditOpinion: { label: '审批意见', visible: true },
  createTime: { label: '申请时间', visible: true }
}
const columns = ref(loadColumnVisibility())
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_leave_columns')
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

function leaveTypeLabel(v) { return { '1': '事假', '2': '病假', '3': '年假', '4': '调休' }[v] || '-' }
function statusBadge(s) { return { '0': 'amber', '1': 'blue', '2': 'green', '3': 'red', '4': 'gray' }[s] || 'gray' }
function statusLabel(s) { return { '0': '草稿', '1': '审批中', '2': '已通过', '3': '已驳回', '4': '已撤销' }[s] || s }

function getList() {
  loading.value = true
  listLeave(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function loadStatusCounts() {
  listLeave({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => { statusCounts.value = { all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 } })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.title = undefined; queryParams.value.leaveType = undefined
  activeStatusTab.value = 'all'; queryParams.value.status = undefined
  handleQuery()
}
function handleStatusTabClick(s) {
  activeStatusTab.value = s
  queryParams.value.status = s === 'all' ? undefined : s
  handleQuery()
}

function handleAdd() {
  form.value = { title: undefined, leaveType: '1', startDate: undefined, endDate: undefined, days: 1, reason: undefined }
  open.value = true; title.value = '新增请假'
}

function submitForm() {
  proxy.$refs['leaveRef'].validate(valid => {
    if (valid) {
      if (form.value.leaveId) {
        updateLeave(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList(); loadStatusCounts() })
      } else {
        addLeave(form.value).then(() => { proxy.$modal.msgSuccess('保存成功，可点击"提交"发起审批'); open.value = false; getList(); loadStatusCounts() })
      }
    }
  })
}

function handleSubmit(row) {
  submitRow.value = row
  approverUserId.value = undefined
  submitOpen.value = true
  // 加载用户列表（选择审批人）
  request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 100 } }).then(res => {
    userOptions.value = res.rows || []
  })
}

function doSubmit() {
  if (!approverUserId.value) { proxy.$modal.msgWarning('请选择审批人'); return }
  submitLeave(submitRow.value.leaveId, approverUserId.value).then(() => {
    proxy.$modal.msgSuccess('审批流程已发起，可在"我的待办"（审批人）中查看')
    submitOpen.value = false
    getList(); loadStatusCounts()
  })
}

function handleView(row) {
  detailData.value = {}; historyTasks.value = []; diagramUrl.value = ''
  detailOpen.value = true
  getLeave(row.leaveId).then(res => { detailData.value = res.data || {} })
  if (row.instanceId) {
    request({ url: '/flowable/process/history/' + row.instanceId, method: 'get' }).then(res => { historyTasks.value = res.data || [] })
    request({ url: '/flowable/process/diagram/' + row.instanceId, method: 'get', responseType: 'blob' }).then(res => {
      diagramUrl.value = URL.createObjectURL(new Blob([res], { type: 'image/png' }))
    }).catch(() => { diagramUrl.value = '' })  // 降级：页面显示「暂无流程图」空态
  }
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除请假单"' + row.title + '"？').then(() => delLeave(row.leaveId)).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList(); loadStatusCounts()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}

function handleCancel(row) {
  proxy.$modal.confirm('确认撤销该请假流程？').then(() => {
    return request({ url: '/flowable/process/cancel/' + row.instanceId, method: 'put', params: { reason: '申请人撤销' } })
  }).then(() => {
    proxy.$modal.msgSuccess('流程已撤销'); getList(); loadStatusCounts()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('撤销失败：' + (e?.message || e)) })
}

getList()
loadStatusCounts()
onActivated(() => { getList(); loadStatusCounts() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';
</style>
