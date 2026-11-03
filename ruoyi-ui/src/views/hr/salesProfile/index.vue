<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="销售区域" prop="salesRegion">
        <el-input v-model="queryParams.salesRegion" placeholder="请输入销售区域" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="职级" prop="salesLevel">
        <el-select v-model="queryParams.salesLevel" placeholder="请选择职级" clearable style="width: 140px">
          <el-option v-for="d in salesLevelOptions" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:salesProfile:add']">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="profileList">
      <el-table-column label="员工编号" prop="employeeNo" width="120" />
      <el-table-column label="员工姓名" prop="employeeName" min-width="120" />
      <el-table-column label="销售区域" prop="salesRegion" width="140" />
      <el-table-column label="销售渠道" prop="channelName" width="140" />
      <el-table-column label="职级" prop="salesLevel" width="100" align="center">
        <template #default="scope">
          <dict-tag v-if="scope.row.salesLevel" :options="salesLevelOptions" :value="scope.row.salesLevel" />
        </template>
      </el-table-column>
      <el-table-column label="提成方案" min-width="170" show-overflow-tooltip>
        <template #default="scope"><span>{{ planName(scope.row.commissionPlanId) }}</span></template>
      </el-table-column>
      <el-table-column label="年度营销目标" prop="entryTarget" width="140" align="right" />
      <el-table-column label="操作" width="180" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:rule:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:rule:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改营销档案对话框 -->
    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="profileRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="员工编号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="请输入员工编号" />
        </el-form-item>
        <el-form-item label="员工姓名" prop="employeeName">
          <el-input v-model="form.employeeName" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="关联系统用户" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入关联系统用户ID（营销业绩归属）" />
        </el-form-item>
        <el-form-item label="销售区域" prop="salesRegion">
          <el-input v-model="form.salesRegion" placeholder="请输入销售区域" />
        </el-form-item>
        <el-form-item label="销售渠道" prop="channelName">
          <el-input v-model="form.channelName" placeholder="请输入销售渠道" />
        </el-form-item>
        <el-form-item label="职级" prop="salesLevel">
          <el-select v-model="form.salesLevel" placeholder="请选择职级" clearable style="width: 100%">
            <el-option v-for="d in salesLevelOptions" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="提成方案" prop="commissionPlanId">
          <el-select v-model="form.commissionPlanId" filterable clearable placeholder="请选择提成方案（仅启用中的方案可选）" style="width: 100%">
            <el-option v-for="p in planOptions" :key="p.planId" :label="p.planName + '（' + dictLabelOf(baseOptions, p.calcBase) + '）'" :value="p.planId" :disabled="p.planStatus !== '1'" />
          </el-select>
        </el-form-item>
        <el-form-item label="年度营销目标" prop="entryTarget">
          <el-input v-model="form.entryTarget" placeholder="请输入年度营销目标金额" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SalesProfile">
import { listProfile, getProfile, addProfile, updateProfile, delProfile } from '@/api/hr/salesPerf'
import { listPlan } from '@/api/hr/commission'

const { proxy } = getCurrentInstance()
const { hr_sales_level: salesLevelOptions } = proxy.useDict('hr_sales_level')
const { hr_commission_base: baseOptions } = proxy.useDict('hr_commission_base')

// 提成方案下拉（弹框选择替代手输 ID）：加载全部方案供名称显示，仅启用中的方案可选
const planOptions = ref([])
loadPlanOptions()
function loadPlanOptions() { listPlan({ pageNum: 1, pageSize: 200 }).then(res => { planOptions.value = res.rows || [] }).catch(() => {}) }
function planName(planId) { const p = planOptions.value.find(x => x.planId === planId); return p ? p.planName : (planId || '-') }
function dictLabelOf(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : (val || '-') }

const profileList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeName: undefined, salesRegion: undefined, salesLevel: undefined },
  rules: {
    employeeNo: [{ required: true, message: '请输入员工编号', trigger: 'blur' }],
    employeeName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
    userId: [{ required: true, message: '请输入关联系统用户ID', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listProfile(queryParams.value).then(res => { profileList.value = res.rows; total.value = res.total; loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function reset() {
  form.value = { profileId: undefined, employeeNo: undefined, employeeName: undefined, userId: undefined, salesRegion: undefined, channelName: undefined, salesLevel: undefined, commissionPlanId: undefined, entryTarget: undefined, remark: undefined }
  proxy.resetForm('profileRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增营销档案' }
function handleUpdate(row) {
  const id = row?.profileId
  if (!id) return
  getProfile(id).then(res => { form.value = res.data; open.value = true; title.value = '修改营销档案' })
}
function submitForm() {
  proxy.$refs['profileRef'].validate(valid => {
    if (valid) {
      if (form.value.profileId) updateProfile(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      else addProfile(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
    }
  })
}
function cancel() { open.value = false; reset() }
getList()
</script>
