<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="活动" prop="activityId">
        <el-select v-model="queryParams.activityId" filterable clearable placeholder="请选择活动" style="width: 240px">
          <el-option v-for="a in activityOptions" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
        </el-select>
      </el-form-item>
      <el-form-item label="企业名称" prop="companyName">
        <el-input v-model="queryParams.companyName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="参与状态" prop="participateStatus">
        <el-select v-model="queryParams.participateStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_participate_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:participant:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:participant:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:participant:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:participant:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="活动名称" prop="activityName" show-overflow-tooltip v-if="!queryParams.activityId" />
      <el-table-column label="企业名称" prop="companyName" :width="colWidth('companyName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="联系人" prop="contactName" :width="colWidth('contactName', 100)" resizable />
      <el-table-column label="手机号" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="邮箱" prop="contactEmail" :width="colWidth('contactEmail', 180)" resizable show-overflow-tooltip />
      <el-table-column label="职位" prop="position" :width="colWidth('position', 120)" resizable />
      <el-table-column label="参与状态" prop="participateStatus" :width="colWidth('participateStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_participate_status" :value="scope.row.participateStatus" /></template>
      </el-table-column>
      <el-table-column label="线索" :width="80" align="center">
        <template #default="scope"><el-tag v-if="scope.row.leadId" type="success" size="small">已转</el-tag><span v-else>-</span></template>
      </el-table-column>
      <el-table-column label="签到时间" prop="signTime" :width="colWidth('signTime', 160)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:participant:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="signIn" icon="CircleCheck" v-if="scope.row.participateStatus !== '1'" v-hasPermi="['marketing:participant:edit']">签到</el-dropdown-item>
                <el-dropdown-item command="convertLead" icon="Connection" v-if="!scope.row.leadId" v-hasPermi="['marketing:participant:convert']">转线索</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:participant:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="750px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="participantRef" :model="form" :rules="rules" label-width="100px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="活动关联" name="activity">
            <el-row>
              <el-col :span="24"><el-form-item label="所属活动" prop="activityId">
                <el-select v-model="form.activityId" filterable placeholder="请选择活动" style="width: 100%">
                  <el-option v-for="a in activityOptions" :key="a.activityId" :label="a.activityName" :value="a.activityId" />
                </el-select>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="参与人信息" name="info">
            <el-row>
              <el-col :span="24"><el-form-item label="从联系人库选">
                <el-select v-model="form.contactId" filterable clearable placeholder="选择后自动带出信息" style="width: 100%" @change="onContactSelect">
                  <el-option v-for="c in contactOptions" :key="c.contactId" :label="c.name + ' (' + (c.customerName||'') + ')'" :value="c.contactId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业名称" prop="companyName"><el-input v-model="form.companyName" placeholder="请输入企业名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" placeholder="请输入联系人姓名" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="手机号" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入手机号" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="邮箱" prop="contactEmail"><el-input v-model="form.contactEmail" placeholder="请输入邮箱" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="参与状态" prop="participateStatus">
                <el-select v-model="form.participateStatus" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_participate_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="签到时间" prop="signTime"><el-date-picker v-model="form.signTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择签到时间" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="其他信息" name="other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewOpen" width="750px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">参与人详情</span>
        </div>
      </template>
      <el-collapse v-model="viewActiveNames">
        <el-collapse-item title="活动关联" name="activity">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">所属活动</span><div class="rd-value">{{ viewForm.activityName }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="参与人信息" name="info">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewForm.companyName }}</div></div>
            <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
            <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewForm.contactPhone }}</div></div>
            <div class="rd-item"><span class="rd-label">邮箱</span><div class="rd-value">{{ viewForm.contactEmail }}</div></div>
            <div class="rd-item"><span class="rd-label">职位</span><div class="rd-value">{{ viewForm.position }}</div></div>
            <div class="rd-item"><span class="rd-label">参与状态</span><div class="rd-value"><dict-tag :options="marketing_participate_status" :value="viewForm.participateStatus" /></div></div>
            <div class="rd-item"><span class="rd-label">签到时间</span><div class="rd-value">{{ viewForm.signTime }}</div></div>
            <div class="rd-item"><span class="rd-label">关联线索ID</span><div class="rd-value">{{ viewForm.leadId || '未转化' }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="其他信息" name="other">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark }}</div></div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-dialog>
  </div>
</template>

<script setup name="MkParticipant">
import { listParticipant, getParticipant, addParticipant, updateParticipant, delParticipant, signInParticipant, convertToLead } from '@/api/mk/participant'
import { listActivity } from '@/api/mk/activity'
import { listContact } from '@/api/mk/contact'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_participant_index')
const { marketing_participate_status } = proxy.useDict('marketing_participate_status')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activityOptions = ref([])
const contactOptions = ref([])
const viewForm = ref({})
const activeNames = ref(['activity', 'info', 'other'])
const viewActiveNames = ref(['activity', 'info', 'other'])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, activityId: undefined, companyName: undefined, participateStatus: undefined },
  rules: {
    activityId: [{ required: true, message: '请选择所属活动', trigger: 'change' }],
    companyName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
    contactName: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
    contactPhone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listParticipant(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getActivityOptions() {
  listActivity({ pageNum: 1, pageSize: 9999 }).then(res => { activityOptions.value = res.rows })
}
function getContactOptions() {
  listContact({ pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.participantId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { activityId: undefined, leadId: undefined, contactId: undefined, companyName: undefined, contactName: undefined, contactPhone: undefined, contactEmail: undefined, position: undefined, participateStatus: '0', source: '0', signTime: undefined, remark: undefined }
  proxy.resetForm('participantRef')
}
function handleAdd() { reset(); if (contactOptions.value.length === 0) getContactOptions(); open.value = true; title.value = '新增活动参与人' }
function handleUpdate(row) { reset(); if (contactOptions.value.length === 0) getContactOptions(); getParticipant(row.participantId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改活动参与人' }) }
function handleView(row) { getParticipant(row.participantId).then(res => { viewForm.value = res.data; viewOpen.value = true }) }
function onContactSelect(contactId) {
  if (contactId) {
    const c = contactOptions.value.find(i => i.contactId === contactId)
    if (c) {
      form.value.companyName = c.customerName
      form.value.contactName = c.name
      form.value.contactPhone = c.phone
      form.value.contactEmail = c.email
      form.value.position = c.position
    }
  }
}
function submitForm() {
  proxy.$refs['participantRef'].validate(valid => {
    if (valid) {
      if (form.value.participantId != undefined) { updateParticipant(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addParticipant(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const participantIds = row.participantId || ids.value; proxy.$modal.confirm('确认删除编号为"' + participantIds + '"的数据？').then(() => delParticipant(participantIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/participant/export', { ...queryParams.value }, `participant_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// P1-5: 签到
function handleSignIn(row) {
  proxy.$modal.confirm('确认签到？').then(() => signInParticipant(row.participantId)).then(() => { proxy.$modal.msgSuccess('签到成功'); getList() }).catch(() => {})
}
// P1-5: 转线索
function handleConvertLead(row) {
  proxy.$modal.confirm('确认将"' + row.contactName + '"转化为线索？').then(() => convertToLead(row.participantId)).then(() => { proxy.$modal.msgSuccess('转线索成功'); getList() }).catch(() => {})
}

// 操作下拉菜单统一分发
function handleCommand(cmd, row) {
  switch (cmd) {
    case 'signIn': handleSignIn(row); break
    case 'convertLead': handleConvertLead(row); break
    case 'delete': handleDelete(row); break
  }
}

getActivityOptions()
getList()
</script>
