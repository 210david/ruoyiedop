<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="年月" prop="yearMonth">
        <el-input v-model="queryParams.yearMonth" placeholder="请输入年月" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:payslip:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:payslip:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:payslip:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:payslip:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrPayslipList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="年月" prop="yearMonth" align="center"  />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="组织ID" prop="orgId" align="center"  />
      <el-table-column label="基本工资" prop="basicSalary" align="center"  />
      <el-table-column label="岗位工资" prop="positionSalary" align="center"  />
      <el-table-column label="技能津贴" prop="skillAllowance" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:payslip:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:payslip:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="工资条" append-to-body>
      <el-form ref="hrPayslipRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年月" prop="yearMonth">
            <el-input v-model="form.yearMonth" placeholder="请输入年月" />
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
          <el-form-item label="组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
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
          <el-form-item label="夜班津贴" prop="nightAllowance">
            <el-input-number v-model="form.nightAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="高温津贴" prop="highTempAllowance">
            <el-input-number v-model="form.highTempAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有毒有害津贴" prop="hazardAllowance">
            <el-input-number v-model="form.hazardAllowance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="餐补" prop="mealAllowance">
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
          <el-form-item label="计件工资" prop="pieceworkPay">
            <el-input-number v-model="form.pieceworkPay" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="加班费" prop="overtimePay">
            <el-input-number v-model="form.overtimePay" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="绩效奖金" prop="performanceBonus">
            <el-input-number v-model="form.performanceBonus" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="缺勤扣款" prop="absenceDeduction">
            <el-input-number v-model="form.absenceDeduction" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="其他加项" prop="otherAdd">
            <el-input-number v-model="form.otherAdd" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="其他扣项" prop="otherDeduct">
            <el-input-number v-model="form.otherDeduct" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="应发合计" prop="grossSalary">
            <el-input-number v-model="form.grossSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="社保个人" prop="socialInsurance">
            <el-input-number v-model="form.socialInsurance" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公积金个人" prop="houseFund">
            <el-input-number v-model="form.houseFund" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="个人所得税" prop="incomeTax">
            <el-input-number v-model="form.incomeTax" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="其他扣款" prop="otherDeduction">
            <el-input-number v-model="form.otherDeduction" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实发工资" prop="netSalary">
            <el-input-number v-model="form.netSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="calcStatus">
            <el-input v-model="form.calcStatus" placeholder="请输入状态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="锁定人" prop="sealedBy">
            <el-input v-model="form.sealedBy" placeholder="请输入锁定人" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="锁定时间" prop="sealedTime">
            <el-date-picker v-model="form.sealedTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发放日期" prop="payDate">
            <el-date-picker v-model="form.payDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银行回单号" prop="bankReceiptNo">
            <el-input v-model="form.bankReceiptNo" placeholder="请输入银行回单号" />
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

<script setup name="HrPayslip">
import { listHrPayslip, getHrPayslip, addHrPayslip, updateHrPayslip, delHrPayslip } from '@/api/hr/hrPayslip'

const { proxy } = getCurrentInstance()

const hrPayslipList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, yearMonth: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    yearMonth: [{ required: true, message: '年月不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrPayslip(queryParams.value).then(response => {
    hrPayslipList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.yearMonth = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.payslipId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    yearMonth: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgId: undefined,
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
    pieceworkPay: undefined,
    overtimePay: undefined,
    performanceBonus: undefined,
    absenceDeduction: undefined,
    otherAdd: undefined,
    otherDeduct: undefined,
    grossSalary: undefined,
    socialInsurance: undefined,
    houseFund: undefined,
    incomeTax: undefined,
    otherDeduction: undefined,
    netSalary: undefined,
    calcStatus: undefined,
    sealedBy: undefined,
    sealedTime: undefined,
    payDate: undefined,
    bankReceiptNo: undefined
  }
  proxy.resetForm('hrPayslipRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const payslipId = row.payslipId || ids.value[0]
  getHrPayslip(payslipId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPayslipRef'].validate(valid => {
    if (valid) {
      if (form.value.payslipId != undefined) {
        updateHrPayslip(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPayslip(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const payslipIds = row.payslipId || ids.value
  proxy.$modal.confirm('是否确认删除选中的工资条？').then(function() {
    return delHrPayslip(payslipIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/payslip/export', { ...queryParams.value }, `hrPayslip_${new Date().getTime()}.xlsx`)
}

getList()
</script>
