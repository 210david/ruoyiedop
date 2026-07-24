<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户编号" prop="customerNo">
        <el-input v-model="queryParams.customerNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="企业名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户等级" prop="customerLevel">
        <el-select v-model="queryParams.customerLevel" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户状态" prop="customerStatus">
        <el-select v-model="queryParams.customerStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:customer:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['marketing:customer:import']">导入</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:customer:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:customer:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="primary" plain icon="User" :disabled="multiple" @click="handleBatchAssign" v-hasPermi="['marketing:customer:assign']">批量分配</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="RefreshRight" :disabled="multiple" @click="handleBatchStatus" v-hasPermi="['marketing:customer:edit']">批量改状态</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:customer:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户编号" prop="customerNo" :width="colWidth('customerNo', 150)" resizable />
      <el-table-column label="企业名称" prop="customerName" show-overflow-tooltip>
        <template #default="scope">
          <el-button link type="primary" @click="handleView(scope.row)">{{ scope.row.customerName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="客户标签" prop="tagNames" :width="colWidth('tagNames', 150)" resizable show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.tagNames">{{ scope.row.tagNames }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="客户等级" prop="customerLevel" :width="colWidth('customerLevel', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_level" :value="scope.row.customerLevel" /></template>
      </el-table-column>
      <el-table-column label="所属行业" prop="industry" :width="colWidth('industry', 120)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_industry" :value="scope.row.industry" /></template>
      </el-table-column>
      <el-table-column label="客户状态" prop="customerStatus" :width="colWidth('customerStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_customer_status" :value="scope.row.customerStatus" /></template>
      </el-table-column>
      <el-table-column label="累计交易额" prop="totalAmount" :width="colWidth('totalAmount', 120)" resizable align="right" />
      <el-table-column label="负责人" prop="userName" :width="colWidth('userName', 100)" resizable>
        <template #default="scope">
          <span v-if="scope.row.userName">{{ scope.row.userName }}</span>
          <el-tag v-else type="warning" size="small">公海</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="部门" prop="deptName" :width="colWidth('deptName', 120)" resizable />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:customer:edit']">修改</el-button>
          <el-dropdown @command="(cmd) => handleCommand(cmd, scope.row)" trigger="click" v-hasPermi="['marketing:customer:edit']">
            <el-button link type="primary" icon="ArrowDown">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="assign" icon="User" v-if="scope.row.userId" v-hasPermi="['marketing:customer:assign']">转移分配</el-dropdown-item>
                <el-dropdown-item command="release" icon="Coin" v-if="scope.row.userId && scope.row.customerStatus !== '0'">释放到公海</el-dropdown-item>
                <el-dropdown-item command="claim" icon="Pointer" v-if="!scope.row.userId" v-hasPermi="['marketing:customer:claim']">领取客户</el-dropdown-item>
                <el-dropdown-item command="status" icon="RefreshRight" divided>变更状态</el-dropdown-item>
                <el-dropdown-item command="delete" icon="Delete" v-hasPermi="['marketing:customer:remove']" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="customerRef" :model="form" :rules="rules" label-width="110px">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="基本信息" name="basic">
            <el-row>
              <el-col :span="12"><el-form-item label="客户编号" prop="customerNo"><el-input v-model="form.customerNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业名称" prop="customerName"><el-input v-model="form.customerName" placeholder="请输入企业名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="信用代码" prop="creditCode"><el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="客户等级" prop="customerLevel">
                <el-select v-model="form.customerLevel" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_customer_level" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="客户来源" prop="customerSource">
                <el-select v-model="form.customerSource" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_customer_source" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="客户状态" prop="customerStatus">
                <el-select v-model="form.customerStatus" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="客户标签" prop="tagIds">
                <el-select v-model="form.tagIds" multiple filterable clearable placeholder="请选择客户标签" style="width: 100%">
                  <el-option v-for="t in tagOptions" :key="t.tagId" :label="t.tagName" :value="t.tagId" />
                </el-select>
              </el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="企业信息" name="company">
            <el-row>
              <el-col :span="12"><el-form-item label="所属行业" prop="industry">
                <el-select v-model="form.industry" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_industry" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="企业规模" prop="companySize">
                <el-select v-model="form.companySize" placeholder="请选择" style="width: 100%">
                  <el-option v-for="d in marketing_company_size" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="官网" prop="website"><el-input v-model="form.website" placeholder="请输入官网地址" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="地址信息" name="address">
            <el-row>
              <el-col :span="24"><el-form-item label="详细地址" prop="address"><el-input v-model="form.address" placeholder="请输入详细地址" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="业务信息" name="business">
            <el-row>
              <el-col :span="12"><el-form-item label="首次接触" prop="firstContactDate"><el-date-picker v-model="form.firstContactDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="合作开始" prop="cooperationDate"><el-date-picker v-model="form.cooperationDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="累计交易额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item title="负责信息" name="owner">
            <el-row>
              <el-col :span="12"><el-form-item label="负责人" prop="userId">
                <el-select v-model="form.userId" filterable clearable placeholder="请选择负责人" style="width: 100%" @change="onUserChange">
                  <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="所属部门" prop="deptId">
                <el-tree-select v-model="form.deptId" :data="deptOptions" :props="{ value: 'id', label: 'label', children: 'children' }" value-key="id" placeholder="请选择部门" check-strictly clearable style="width: 100%" />
              </el-form-item></el-col>
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

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">分配客户</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="负责人">
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

    <!-- 批量分配弹窗 -->
    <el-dialog v-model="batchAssignOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">批量分配客户</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 个客户'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="batchAssignUserId" filterable clearable placeholder="请选择负责人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchAssign">确 定</el-button>
        <el-button @click="batchAssignOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量变更状态弹窗 -->
    <el-dialog v-model="batchStatusOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">批量变更客户状态</span>
        </div>
      </template>
      <el-alert :title="'已选中 ' + ids.length + ' 个客户'" type="info" :closable="false" class="mb8" />
      <el-form label-width="80px">
        <el-form-item label="客户状态">
          <el-select v-model="batchStatusValue" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmBatchStatus">确 定</el-button>
        <el-button @click="batchStatusOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 变更状态弹窗（单条） -->
    <el-dialog v-model="statusOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">变更客户状态</span>
        </div>
      </template>
      <el-form label-width="80px">
        <el-form-item label="客户状态">
          <el-select v-model="statusValue" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in marketing_customer_status" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmStatusChange">确 定</el-button>
        <el-button @click="statusOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importOpen" width="500px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">客户导入</span>
        </div>
      </template>
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :auto-upload="false" :action="importUrl" :headers="headers" :data="{ updateSupport: importUpdateSupport }" :on-success="handleImportSuccess" :on-error="handleImportError" drag>
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
        <el-checkbox v-model="importUpdateSupport">如果已存在相同企业名称，则更新数据</el-checkbox>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitImport">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MkCustomer">
import { useRouter } from 'vue-router'
import { UploadFilled } from '@element-plus/icons-vue'
import { listCustomer, getCustomer, addCustomer, updateCustomer, delCustomer, changeCustomerStatus, assignCustomer, releaseToPool, claimCustomer, batchAssignCustomer, batchUpdateStatus } from '@/api/mk/customer'
import { listTag } from '@/api/mk/tag'
import { listUser, deptTreeSelect } from '@/api/system/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard([])
import { getToken } from '@/utils/auth'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_customer_index')
const { marketing_customer_level, marketing_customer_source, marketing_industry, marketing_company_size, marketing_customer_status } = proxy.useDict('marketing_customer_level', 'marketing_customer_source', 'marketing_industry', 'marketing_company_size', 'marketing_customer_status')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const userOptions = ref([])
const deptOptions = ref([])
const tagOptions = ref([])
const activeNames = ref(['basic', 'company', 'address', 'business', 'owner', 'other'])

// 分配相关
const assignOpen = ref(false)
const assignUserId = ref(null)
const assignCustomerId = ref(null)

// 批量分配相关
const batchAssignOpen = ref(false)
const batchAssignUserId = ref(null)

// 批量变更状态相关
const batchStatusOpen = ref(false)
const batchStatusValue = ref(null)

// 单条变更状态
const statusOpen = ref(false)
const statusValue = ref(null)
const statusCustomerId = ref(null)

// 导入相关
const importOpen = ref(false)
const importUpdateSupport = ref(false)
const importUrl = ref(import.meta.env.VITE_APP_BASE_API + '/mk/customer/importData')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, customerNo: undefined, customerName: undefined, customerLevel: undefined, customerStatus: undefined },
  rules: {
    customerName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false })
}
function getUserOptions() {
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => { userOptions.value = res.rows.filter(u => u.userId !== 1) })
}
function getDeptTree() {
  deptTreeSelect().then(res => { deptOptions.value = res.data })
}
function getTagOptions() {
  listTag({ pageNum: 1, pageSize: 9999, status: '0' }).then(res => { tagOptions.value = res.rows })
}
function onUserChange(userId) {
  if (userId) {
    const user = userOptions.value.find(u => u.userId === userId)
    if (user) {
      form.value.userName = user.nickName
      if (user.deptId) {
        form.value.deptId = user.deptId
        form.value.deptName = user.dept ? user.dept.deptName : undefined
      }
    }
  } else {
    form.value.userName = undefined
  }
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.customerId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = { customerNo: undefined, customerName: undefined, creditCode: undefined, customerLevel: '3', customerSource: undefined, industry: undefined, companySize: undefined, address: undefined, website: undefined, customerStatus: '0', firstContactDate: undefined, cooperationDate: undefined, totalAmount: 0, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, tagIds: [], remark: undefined }
  proxy.resetForm('customerRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增企业客户' }
function handleUpdate(row) { reset(); getCustomer(row.customerId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改企业客户' }) }
function handleView(row) { router.push('/mk/customer-detail/' + row.customerId) }
function submitForm() {
  proxy.$refs['customerRef'].validate(valid => {
    if (valid) {
      if (form.value.customerId != undefined) { updateCustomer(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCustomer(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const customerIds = row.customerId || ids.value; proxy.$modal.confirm('确认删除编号为"' + customerIds + '"的数据？').then(() => delCustomer(customerIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('mk/customer/export', { ...queryParams.value }, `customer_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ====== 操作下拉菜单 ======
function handleCommand(cmd, row) {
  if (cmd === 'assign') handleAssignSingle(row)
  else if (cmd === 'release') handleRelease(row)
  else if (cmd === 'claim') handleClaim(row)
  else if (cmd === 'status') handleStatusSingle(row)
  else if (cmd === 'delete') handleDelete(row)
}

// 分配/转移
function handleAssignSingle(row) {
  assignCustomerId.value = row.customerId
  assignUserId.value = row.userId || null
  assignOpen.value = true
}
function confirmAssign() {
  const data = { userId: assignUserId.value }
  const user = userOptions.value.find(u => u.userId === assignUserId.value)
  if (user) {
    data.deptId = user.deptId
    data.userName = user.nickName
  }
  assignCustomer(assignCustomerId.value, data).then(() => {
    proxy.$modal.msgSuccess('分配成功')
    assignOpen.value = false
    getList()
  })
}

// 释放到公海
function handleRelease(row) {
  proxy.$modal.confirm('确认将"' + row.customerName + '"释放到公海？').then(() => {
    releaseToPool(row.customerId).then(() => {
      proxy.$modal.msgSuccess('已释放到公海')
      getList()
    })
  }).catch(() => {})
}

// 领取客户
function handleClaim(row) {
  proxy.$modal.confirm('确认领取"' + row.customerName + '"？').then(() => {
    claimCustomer(row.customerId).then(() => {
      proxy.$modal.msgSuccess('领取成功')
      getList()
    })
  }).catch(() => {})
}

// 变更状态（单条）
function handleStatusSingle(row) {
  statusCustomerId.value = row.customerId
  statusValue.value = row.customerStatus
  statusOpen.value = true
}
function confirmStatusChange() {
  changeCustomerStatus(statusCustomerId.value, statusValue.value).then(() => {
    proxy.$modal.msgSuccess('状态变更成功')
    statusOpen.value = false
    getList()
  })
}

// 批量分配
function handleBatchAssign() {
  batchAssignUserId.value = null
  batchAssignOpen.value = true
}
function confirmBatchAssign() {
  if (!batchAssignUserId.value) {
    proxy.$modal.msgWarning('请选择负责人')
    return
  }
  const user = userOptions.value.find(u => u.userId === batchAssignUserId.value)
  const data = {
    customerIds: ids.value,
    userId: batchAssignUserId.value,
    deptId: user ? user.deptId : undefined,
    userName: user ? user.nickName : undefined
  }
  batchAssignCustomer(data).then(() => {
    proxy.$modal.msgSuccess('批量分配成功')
    batchAssignOpen.value = false
    getList()
  })
}

// 批量变更状态
function handleBatchStatus() {
  batchStatusValue.value = null
  batchStatusOpen.value = true
}
function confirmBatchStatus() {
  if (!batchStatusValue.value) {
    proxy.$modal.msgWarning('请选择客户状态')
    return
  }
  batchUpdateStatus({ customerIds: ids.value, customerStatus: batchStatusValue.value }).then(() => {
    proxy.$modal.msgSuccess('批量变更状态成功')
    batchStatusOpen.value = false
    getList()
  })
}

// 导入
function handleImport() { importOpen.value = true }
function handleDownloadTemplate() { proxy.download('mk/customer/importTemplate', {}, `customer_template_${new Date().getTime()}.xlsx`) }
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

getUserOptions()
getDeptTree()
getTagOptions()
getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.ml5 { margin-left: 5px; }
.mt16 { margin-top: 16px; }
.text-center { text-align: center; }
</style>
