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
      <el-table-column label="客户名称" prop="customerName" show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.customerName }}</el-button>
        </template>
      </el-table-column>
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

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="850px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="interactionRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('i_interact')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>互动信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i_interact }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.i_interact">
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
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('i_follow')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>跟进安排</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i_follow }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.i_follow">
            <el-row>
              <el-col :span="12"><el-form-item label="跟进人" prop="userId">
                <el-input v-model="form.userName" readonly placeholder="请选择跟进人" style="width: 100%" @click="openUserPicker">
                  <template #append>
                    <el-button icon="Search" @click="openUserPicker" />
                  </template>
                  <template #suffix>
                    <el-icon v-if="form.userName" class="clear-icon" @click.stop="clearUser"><CircleClose /></el-icon>
                  </template>
                </el-input>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下次跟进" prop="nextTime"><el-date-picker v-model="form.nextTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="跟进内容" prop="nextContent"><el-input v-model="form.nextContent" type="textarea" :rows="2" placeholder="请输入下次跟进内容" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('i_other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.i_other">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></div>
          <span class="rd-detail-header-title">互动记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.customerName">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ viewForm.customerName }}</span>
            <dict-tag :options="marketing_interaction_type" :value="viewForm.interactType" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_interact')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>互动信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_interact }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_interact">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">所属客户</span><div class="rd-value">{{ viewForm.customerName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.contactName }">{{ viewForm.contactName || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">互动类型</span><div class="rd-value"><dict-tag :options="marketing_interaction_type" :value="viewForm.interactType" /></div></div>
              <div class="rd-item"><span class="rd-label">互动时间</span><div class="rd-value">{{ viewForm.interactTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">互动内容</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.content }">{{ viewForm.content || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_follow')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>跟进安排</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_follow }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_follow">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">跟进人</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.userName }">{{ viewForm.userName || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">下次跟进</span><div class="rd-value">{{ viewForm.nextTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">跟进内容</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.nextContent }">{{ viewForm.nextContent || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v_other">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewForm.createTime || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- 跟进人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择跟进人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="MkInteraction">
import { CircleClose } from '@element-plus/icons-vue'
import { listInteraction, getInteraction, addInteraction, updateInteraction, delInteraction } from '@/api/mk/interaction'
import { listCustomer } from '@/api/mk/customer'
import { listContact } from '@/api/mk/contact'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['i_interact', 'i_follow', 'i_other', 'v_interact', 'v_follow', 'v_other'])

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
const viewForm = ref({})

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
function onCustomerChange(customerId) {
  form.value.contactId = undefined
  if (customerId) { listContact({ customerId: customerId, pageNum: 1, pageSize: 9999 }).then(res => { contactOptions.value = res.rows }) }
  else { contactOptions.value = [] }
}
/** 打开跟进人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.userId)
}
/** 跟进人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.userId = user.userId
  form.value.userName = user.nickName
}
/** 清除跟进人 */
function clearUser() {
  form.value.userId = undefined
  form.value.userName = undefined
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
function handleView(row) {
  getInteraction(row.recordId).then(res => {
    viewForm.value = res.data
    Object.keys(collapsedCards).forEach(k => { if (k.startsWith('v_')) collapsedCards[k] = false })
    viewOpen.value = true
  })
}
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
getList()
</script>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
:deep(.el-input.is-disabled .el-input__inner) {
  cursor: pointer;
}
</style>
