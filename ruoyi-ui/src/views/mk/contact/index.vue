<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="所属客户" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:contact:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['marketing:contact:import']">导入</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:contact:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:contact:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Connection" :disabled="multiple" @click="handleBatchSetKey" v-hasPermi="['marketing:contact:edit']">批量设为关键</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="User" :disabled="single" @click="handleAssign" v-hasPermi="['marketing:contact:assign']">分配</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:contact:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="所属客户" prop="customerName" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.customerName }}</span>
          <el-badge v-if="isOverdue(scope.row)" is-dot type="danger" class="ml5" />
        </template>
      </el-table-column>
      <el-table-column label="姓名" prop="name" :width="colWidth('name', 100)" resizable>
        <template #default="scope">
          <el-button link type="primary" @click="handleDetail(scope.row)">{{ scope.row.name }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="性别" prop="gender" :width="colWidth('gender', 80)" resizable align="center">
        <template #default="scope">{{ scope.row.gender === '0' ? '男' : '女' }}</template>
      </el-table-column>
      <el-table-column label="职位" prop="position" :width="colWidth('position', 120)" resizable />
      <el-table-column label="手机号" prop="phone" :width="colWidth('phone', 130)" resizable />
      <el-table-column label="邮箱" prop="email" :width="colWidth('email', 180)" resizable show-overflow-tooltip />
      <el-table-column label="关键联系人" prop="isKey" :width="colWidth('isKey', 100)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.isKey === '1' ? 'danger' : 'info'">{{ scope.row.isKey === '1' ? '是' : '否' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="主要联系人" prop="isPrimary" :width="colWidth('isPrimary', 100)" resizable align="center">
        <template #default="scope"><el-tag :type="scope.row.isPrimary === '1' ? 'success' : 'info'" size="small">{{ scope.row.isPrimary === '1' ? '是' : '否' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="归属销售" prop="ownerUserName" :width="colWidth('ownerUserName', 100)" resizable />
      <el-table-column label="下次联系时间" prop="nextContactTime" :width="colWidth('nextContactTime', 160)" resizable>
        <template #default="scope">
          <span :class="{ 'text-danger': isOverdue(scope.row) }">{{ scope.row.nextContactTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:contact:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click" v-hasPermi="['marketing:contact:edit']">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="setPrimary" icon="Star" v-if="scope.row.isPrimary !== '1'">设为主要联系人</el-dropdown-item>
                <el-dropdown-item command="checkDup" icon="CopyDocument" v-hasPermi="['marketing:contact:list']">查重</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:contact:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" append-to-body>
      <el-form ref="contactRef" :model="form" :rules="rules" label-width="100px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="基本信息" name="basic">
            <el-row>
              <el-col :span="12"><el-form-item label="所属客户" prop="customerId">
                <el-select v-model="form.customerId" filterable clearable placeholder="请选择客户" style="width: 100%">
                  <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="请输入姓名" @blur="onNameBlur" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender"><el-radio value="0">男</el-radio><el-radio value="1">女</el-radio></el-radio-group>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="职位" prop="position"><el-input v-model="form.position" placeholder="请输入职位" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="联系方式" name="contact">
            <el-row>
              <el-col :span="12"><el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" @blur="onPhoneBlur" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" @blur="onEmailBlur" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="微信号" prop="wechat"><el-input v-model="form.wechat" placeholder="请输入微信号" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="QQ号" prop="qq"><el-input v-model="form.qq" placeholder="请输入QQ号" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="角色与特征" name="role">
            <el-row>
              <el-col :span="12"><el-form-item label="所属部门" prop="department"><el-input v-model="form.department" placeholder="请输入所属部门" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="角色标签" prop="roleTag">
                <el-select v-model="form.roleTag" multiple placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_contact_role" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="关键联系人" prop="isKey">
                <el-radio-group v-model="form.isKey"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="主要联系人" prop="isPrimary">
                <el-radio-group v-model="form.isPrimary"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="归属销售" prop="ownerUserId">
                <el-select v-model="form.ownerUserId" filterable clearable placeholder="请选择" style="width: 100%">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下次联系时间" prop="nextContactTime">
                <el-date-picker v-model="form.nextContactTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="个人特点" prop="personalTrait"><el-input v-model="form.personalTrait" type="textarea" :rows="2" placeholder="请输入个人特征" /></el-form-item></el-col>
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

    <!-- 重复联系人弹窗 -->
    <el-dialog title="疑似重复联系人" v-model="dupOpen" width="700px" append-to-body>
      <el-alert v-if="dupList.length > 0" title="系统检测到以下联系人可能与当前输入重复，请确认是否继续或选择合并" type="warning" :closable="false" class="mb8" />
      <el-table :data="dupList" border size="small">
        <el-table-column label="姓名" prop="name" width="100" />
        <el-table-column label="手机号" prop="phone" width="130" />
        <el-table-column label="邮箱" prop="email" />
        <el-table-column label="所属客户" prop="customerName" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleMerge(scope.row)">合并到此</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="dupList.length === 0" description="未检测到重复联系人" />
      <template #footer>
        <el-button @click="dupOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 分配弹窗 -->
    <el-dialog title="分配联系人" v-model="assignOpen" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="归属销售">
          <el-select v-model="assignUserId" filterable clearable placeholder="请选择（留空释放到公海）" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmAssign">确 定</el-button>
        <el-button @click="assignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog title="联系人导入" v-model="importOpen" width="500px" append-to-body>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :auto-upload="false" :action="importUrl" :headers="headers" :on-success="handleImportSuccess" :on-error="handleImportError" drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-button type="primary" link @click="handleDownloadTemplate">下载模板</el-button>
          </div>
        </template>
      </el-upload>
      <div class="mt16">
        <el-checkbox v-model="importUpdateSupport">如果已存在相同手机号/邮箱，则更新数据</el-checkbox>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitImport">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkContact">
import { useRouter } from 'vue-router'
import { UploadFilled } from '@element-plus/icons-vue'
import { listContact, getContact, addContact, updateContact, delContact, checkDuplicate, mergeContacts, setPrimary, batchSetKey, assignContact } from '@/api/mk/contact'
import { listCustomer } from '@/api/mk/customer'
import { listUser } from '@/api/system/user'
import { getToken } from '@/utils/auth'
import { useColumnResize } from '@/composables/useColumnResize'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_contact_index')
const { marketing_contact_role } = proxy.useDict('marketing_contact_role')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const customerOptions = ref([])
const userOptions = ref([])
const activeNames = ref(['basic', 'contact', 'role', 'other'])
const dupOpen = ref(false)
const dupList = ref([])
const currentForm = ref({})
const assignOpen = ref(false)
const assignUserId = ref(null)
const assignContactId = ref(null)
const importOpen = ref(false)
const importUpdateSupport = ref(false)
const importUrl = ref(import.meta.env.VITE_APP_BASE_API + '/mk/contact/importData')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: undefined, phone: undefined, customerName: undefined },
  rules: {
    customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
    name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    phone: [{ required: true, message: '手机号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listContact(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function getCustomerOptions() { listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows }) }
function getUserOptions() { listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.contactId); single.value = selection.length !== 1; multiple.value = !selection.length }
function isOverdue(row) { return row.nextContactTime && new Date(row.nextContactTime) <= new Date(Date.now() + 86400000) }
function reset() {
  form.value = { customerId: undefined, name: undefined, gender: '0', position: undefined, department: undefined, roleTag: undefined, phone: undefined, email: undefined, wechat: undefined, qq: undefined, isKey: '0', isPrimary: '0', ownerUserId: undefined, nextContactTime: undefined, personalTrait: undefined, remark: undefined }
  proxy.resetForm('contactRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增联系人' }
function handleUpdate(row) { reset(); getContact(row.contactId || ids.value[0]).then(res => { form.value = res.data; if (form.value.roleTag) { form.value.roleTag = form.value.roleTag.split(',') } open.value = true; title.value = '修改联系人' }) }
function handleDetail(row) { router.push('/mk/contact-detail/' + row.contactId) }
function submitForm() {
  proxy.$refs['contactRef'].validate(valid => {
    if (valid) {
      const submitData = { ...form.value }
      if (submitData.roleTag && Array.isArray(submitData.roleTag)) { submitData.roleTag = submitData.roleTag.join(',') }
      if (form.value.contactId != undefined) { updateContact(submitData).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addContact(submitData).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const contactIds = row.contactId || ids.value; proxy.$modal.confirm('确认删除编号为"' + contactIds + '"的数据？').then(() => delContact(contactIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/contact/export', { ...queryParams.value }, `contact_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// 操作下拉菜单
function handleCommand(cmd, row) {
  if (cmd === 'setPrimary') handleSetPrimary(row)
  else if (cmd === 'checkDup') handleCheckDup(row)
  else if (cmd === 'delete') handleDelete(row)
}

// P1-7: 设为主要联系人
function handleSetPrimary(row) {
  proxy.$modal.confirm('确认将"' + row.name + '"设为主要联系人？（同客户下仅一个主要联系人）').then(() => setPrimary(row.contactId)).then(() => { proxy.$modal.msgSuccess('设置成功'); getList() }).catch(() => {})
}
// P1-7: 批量设为关键联系人
function handleBatchSetKey() {
  proxy.$modal.confirm('确认将选中的' + ids.value.length + '个联系人设为关键联系人？').then(() => batchSetKey({ contactIds: ids.value, isKey: '1' })).then(() => { proxy.$modal.msgSuccess('设置成功'); getList() }).catch(() => {})
}

// P2-8: 查重
function onPhoneBlur() { if (form.value.phone && !form.value.contactId) doCheckDup() }
function onEmailBlur() { if (form.value.email && !form.value.contactId) doCheckDup() }
function onNameBlur() { /* name blur - no auto check */ }
function doCheckDup() {
  checkDuplicate(form.value.phone, form.value.email).then(res => {
    if (res.data && res.data.length > 0) {
      currentForm.value = { ...form.value }
      dupList.value = res.data
      dupOpen.value = true
    }
  })
}
function handleCheckDup(row) {
  checkDuplicate(row.phone, row.email).then(res => {
    dupList.value = (res.data || []).filter(c => c.contactId !== row.contactId)
    currentForm.value = row
    dupOpen.value = true
  })
}
function handleMerge(slaveRow) {
  const masterId = currentForm.value.contactId
  if (!masterId) { proxy.$modal.msgWarning('新增时无法合并，请先保存'); return }
  proxy.$modal.confirm('确认将"' + slaveRow.name + '"合并到当前联系人？合并后从记录将被删除，其互动/活动记录将转移到主记录。').then(() => {
    mergeContacts({ masterId: masterId, slaveId: slaveRow.contactId }).then(() => {
      proxy.$modal.msgSuccess('合并成功')
      dupOpen.value = false
      getList()
    })
  }).catch(() => {})
}

// P2-10: 分配
function handleAssign() {
  assignContactId.value = ids.value[0]
  assignUserId.value = null
  assignOpen.value = true
}
function confirmAssign() {
  assignContact(assignContactId.value, { ownerUserId: assignUserId.value }).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getList()
  })
}

// P3-13: 导入
function handleImport() { importOpen.value = true }
function handleDownloadTemplate() { proxy.download('mk/contact/importTemplate', {}, `contact_template_${new Date().getTime()}.xlsx`) }
function submitImport() {
  proxy.$refs['uploadRef'].submit()
}
function handleImportSuccess(res) {
  if (res.code === 200) {
    proxy.$modal.msgSuccess(res.msg)
    importOpen.value = false
    getList()
  } else {
    proxy.$modal.msgError(res.msg)
  }
}
function handleImportError() {
  proxy.$modal.msgError('导入失败')
}

getCustomerOptions()
getUserOptions()
getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.ml5 { margin-left: 5px; }
.mt16 { margin-top: 16px; }
.text-danger { color: #f56c6c; }
.text-center { text-align: center; }
</style>
