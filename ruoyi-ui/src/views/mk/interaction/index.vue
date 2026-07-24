<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户名称" prop="customerName"><el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="互动类型" prop="interactType">
        <el-select v-model="queryParams.interactType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_interaction_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:interaction:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:interaction:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:interaction:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip />
      <el-table-column label="联系人" prop="contactName" :width="colWidth('contactName', 100)" resizable />
      <el-table-column label="互动类型" prop="interactType" :width="colWidth('interactType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_interaction_type" :value="scope.row.interactType" /></template>
      </el-table-column>
      <el-table-column label="互动时间" prop="interactTime" :width="colWidth('interactTime', 160)" resizable />
      <el-table-column label="互动内容" prop="content" show-overflow-tooltip />
      <el-table-column label="跟进人" prop="userName" :width="colWidth('userName', 100)" resizable />
      <el-table-column label="下次跟进" prop="nextTime" :width="colWidth('nextTime', 160)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:interaction:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:interaction:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="open" width="750px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="interactionRef" :model="form" :rules="rules" label-width="100px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="互动信息" name="interact">
            <el-row>
              <el-col :span="12"><el-form-item label="所属客户" prop="customerId">
                <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                  <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联系人" prop="contactId">
                <el-select v-model="form.contactId" filterable clearable placeholder="请选择联系人" style="width: 100%">
                  <el-option v-for="c in contactOptions" :key="c.contactId" :label="c.name" :value="c.contactId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="互动类型" prop="interactType">
                <el-select v-model="form.interactType" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_interaction_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="互动时间" prop="interactTime"><el-date-picker v-model="form.interactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="互动内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入互动内容" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="跟进安排" name="follow">
            <el-row>
              <el-col :span="12"><el-form-item label="跟进人" prop="userId">
                <el-select v-model="form.userId" filterable clearable placeholder="请选择跟进人" style="width: 100%" @change="onUserChange">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下次跟进" prop="nextTime"><el-date-picker v-model="form.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="跟进内容" prop="nextContent"><el-input v-model="form.nextContent" type="textarea" :rows="2" placeholder="请输入下次跟进内容" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="其他信息" name="other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="750px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></div>
          <span class="rd-detail-header-title">互动记录详情</span>
        </div>
      </template>
      <el-collapse v-model="viewActiveNames">
        <el-collapse-item title="互动信息" name="interact">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">所属客户</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
            <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewForm.contactName }}</div></div>
            <div class="rd-item"><span class="rd-label">互动类型</span><div class="rd-value"><dict-tag :options="marketing_interaction_type" :value="viewForm.interactType" /></div></div>
            <div class="rd-item"><span class="rd-label">互动时间</span><div class="rd-value">{{ viewForm.interactTime }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">互动内容</span><div class="rd-value">{{ viewForm.content }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="跟进安排" name="follow">
          <div class="rd-grid">
            <div class="rd-item"><span class="rd-label">跟进人</span><div class="rd-value">{{ viewForm.userName }}</div></div>
            <div class="rd-item"><span class="rd-label">下次跟进</span><div class="rd-value">{{ viewForm.nextTime }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">跟进内容</span><div class="rd-value">{{ viewForm.nextContent }}</div></div>
          </div>
        </el-collapse-item>
        <el-collapse-item title="其他信息" name="other">
          <div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewForm.remark }}</div></div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-dialog>
  </div>
</template>

<script setup name="MkInteraction">
import { listInteraction, getInteraction, addInteraction, updateInteraction, delInteraction } from '@/api/mk/interaction'
import { listCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import { listUser } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_interaction_index')
const { marketing_interaction_type } = proxy.useDict('marketing_interaction_type')

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
const customerOptions = ref([])
const contactOptions = ref([])
const userOptions = ref([])
const viewForm = ref({})
const activeNames = ref(['interact', 'follow', 'other'])
const viewActiveNames = ref(['interact', 'follow', 'other'])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, customerName: undefined, interactType: undefined },
  rules: {
    customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
    interactType: [{ required: true, message: '请选择互动类型', trigger: 'change' }],
    interactTime: [{ required: true, message: '请选择互动时间', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listInteraction(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getCustomerOptions() { listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function onCustomerChange(customerId) {
  form.value.contactId = undefined
  if (customerId) { listContact({ customerId: customerId, pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows }) }
  else { contactOptions.value = [] }
}
function onUserChange(userId) {
  if (userId) { const user = userOptions.value.find(u => u.userId === userId); if (user) { form.value.userName = user.nickName } }
  else { form.value.userName = undefined }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.recordId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { customerId: undefined, contactId: undefined, opportunityId: undefined, leadId: undefined, interactType: undefined, interactTime: undefined, content: undefined, userId: undefined, userName: undefined, nextTime: undefined, nextContent: undefined, remark: undefined }
  contactOptions.value = []
  proxy.resetForm('interactionRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增互动记录' }
function handleUpdate(row) {
  reset()
  getInteraction(row.recordId || ids.value[0]).then(res => {
    form.value = res.data
    if (form.value.customerId) { listContact({ customerId: form.value.customerId, pageNum: 1, pageSize: 9999 }).then(r => { contactOptions.value = r.rows }) }
    open.value = true; title.value = '修改互动记录'
  })
}
function handleView(row) { getInteraction(row.recordId).then(res => { viewForm.value = res.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['interactionRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) { updateInteraction(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addInteraction(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('确认删除编号为"' + recordIds + '"的数据？').then(() => delInteraction(recordIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getCustomerOptions()
getUserOptions()
getList()
</script>
