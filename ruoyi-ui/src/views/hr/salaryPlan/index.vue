<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="定薪单号" prop="planNo">
        <el-input v-model="queryParams.planNo" placeholder="请输入定薪单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工ID" prop="employeeId">
        <el-input v-model="queryParams.employeeId" placeholder="请输入员工ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工号" prop="employeeNo">
        <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:salaryPlan:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:salaryPlan:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:salaryPlan:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:salaryPlan:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrSalaryPlanList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="定薪单号" prop="planNo" align="center"  />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="类型" prop="planType" align="center"  />
      <el-table-column label="生效日期" prop="effectiveDate" align="center" width="120" />
      <el-table-column label="基本工资" prop="basicSalary" align="center"  />
      <el-table-column label="岗位工资" prop="positionSalary" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:salaryPlan:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:salaryPlan:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="定薪调薪" append-to-body>
      <el-form ref="hrSalaryPlanRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="定薪单号" prop="planNo">
            <el-input v-model="form.planNo" placeholder="请输入定薪单号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="员工ID" prop="employeeId">
            <el-input-number v-model="form.employeeId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="员工姓名" prop="employeeName">
            <el-input v-model="form.employeeName" placeholder="请输入员工姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="请输入工号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型" prop="planType">
            <el-input v-model="form.planType" placeholder="请输入类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生效日期" prop="effectiveDate">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="基本工资" prop="basicSalary">
            <el-input-number v-model="form.basicSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗位工资" prop="positionSalary">
            <el-input-number v-model="form.positionSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="技能津贴" prop="skillAllowance">
            <el-input-number v-model="form.skillAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工龄工资" prop="seniorityPay">
            <el-input-number v-model="form.seniorityPay" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="夜班津贴标准" prop="nightAllowance">
            <el-input-number v-model="form.nightAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="高温津贴标准" prop="highTempAllowance">
            <el-input-number v-model="form.highTempAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有毒有害津贴" prop="hazardAllowance">
            <el-input-number v-model="form.hazardAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="餐补标准" prop="mealAllowance">
            <el-input-number v-model="form.mealAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="交通补贴" prop="transportAllowance">
            <el-input-number v-model="form.transportAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="全勤奖" prop="fullAttendanceBonus">
            <el-input-number v-model="form.fullAttendanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="绩效奖金" prop="performanceBonus">
            <el-input-number v-model="form.performanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="其他补贴" prop="otherAllowance">
            <el-input-number v-model="form.otherAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="薪资合计" prop="totalSalary">
            <el-input-number v-model="form.totalSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批状态" prop="auditStatus">
            <el-input v-model="form.auditStatus" placeholder="请输入审批状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批人" prop="auditBy">
            <el-input v-model="form.auditBy" placeholder="请输入审批人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批时间" prop="auditTime">
            <el-date-picker v-model="form.auditTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="审批意见" prop="auditRemark">
            <el-input v-model="form.auditRemark" placeholder="请输入审批意见" />
          </el-form-item>
        </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrSalaryPlan">
import { listHrSalaryPlan, getHrSalaryPlan, addHrSalaryPlan, updateHrSalaryPlan, delHrSalaryPlan } from '@/api/hr/hrSalaryPlan'

const { proxy } = getCurrentInstance()

const hrSalaryPlanList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, planNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    planNo: [{ required: true, message: '定薪单号不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrSalaryPlan(queryParams.value).then(response => {
    hrSalaryPlanList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.planNo = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.planId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    planNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    planType: undefined,
    effectiveDate: undefined,
    basicSalary: undefined,
    positionSalary: undefined,
    skillAllowance: undefined,
    seniorityPay: undefined,
    nightAllowance: undefined,
    highTempAllowance: undefined,
    hazardAllowance: undefined,
    mealAllowance: undefined,
    transportAllowance: undefined,
    fullAttendanceBonus: undefined,
    performanceBonus: undefined,
    otherAllowance: undefined,
    totalSalary: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrSalaryPlanRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const planId = row.planId || ids.value[0]
  getHrSalaryPlan(planId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSalaryPlanRef'].validate(valid => {
    if (valid) {
      if (form.value.planId != undefined) {
        updateHrSalaryPlan(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSalaryPlan(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const planIds = row.planId || ids.value
  proxy.$modal.confirm('是否确认删除选中的定薪调薪？').then(function() {
    return delHrSalaryPlan(planIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/salaryPlan/export', { ...queryParams.value }, `hrSalaryPlan_${new Date().getTime()}.xlsx`)
}

getList()
</script>
