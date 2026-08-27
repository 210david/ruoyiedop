<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="转正单号" prop="regularNo">
        <el-input v-model="queryParams.regularNo" placeholder="请输入转正单号" clearable @keyup.enter="handleQuery" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:regular:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:regular:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:regular:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:regular:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrRegularList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="转正单号" prop="regularNo" align="center"  />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="组织名称" prop="orgName" align="center"  />
      <el-table-column label="岗位名称" prop="positionName" align="center"  />
      <el-table-column label="入职日期" prop="entryDate" align="center" width="120" />
      <el-table-column label="拟转正日期" prop="regularDate" align="center" width="120" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:regular:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:regular:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="转正" append-to-body>
      <el-form ref="hrRegularRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="转正单号" prop="regularNo">
            <el-input v-model="form.regularNo" placeholder="请输入转正单号" />
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
          <el-form-item label="组织名称" prop="orgName">
            <el-input v-model="form.orgName" placeholder="请输入组织名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗位名称" prop="positionName">
            <el-input v-model="form.positionName" placeholder="请输入岗位名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="入职日期" prop="entryDate">
            <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="拟转正日期" prop="regularDate">
            <el-date-picker v-model="form.regularDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工作表现" prop="workPerformance">
            <el-input v-model="form.workPerformance" placeholder="请输入工作表现" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="技能达标" prop="skillAssessment">
            <el-input v-model="form.skillAssessment" placeholder="请输入技能达标" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="出勤情况" prop="attendanceRecord">
            <el-input v-model="form.attendanceRecord" placeholder="请输入出勤情况" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="安全违规" prop="safetyViolation">
            <el-input v-model="form.safetyViolation" placeholder="请输入安全违规" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="评估结果" prop="evaluationResult">
            <el-input v-model="form.evaluationResult" placeholder="请输入评估结果" />
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

<script setup name="HrRegular">
import { listHrRegular, getHrRegular, addHrRegular, updateHrRegular, delHrRegular } from '@/api/hr/hrRegular'

const { proxy } = getCurrentInstance()

const hrRegularList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, regularNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined },
  rules: {
    regularNo: [{ required: true, message: '转正单号不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrRegular(queryParams.value).then(response => {
    hrRegularList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.regularNo = undefined
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.regularId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    regularNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgName: undefined,
    positionName: undefined,
    entryDate: undefined,
    regularDate: undefined,
    workPerformance: undefined,
    skillAssessment: undefined,
    attendanceRecord: undefined,
    safetyViolation: undefined,
    evaluationResult: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrRegularRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const regularId = row.regularId || ids.value[0]
  getHrRegular(regularId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrRegularRef'].validate(valid => {
    if (valid) {
      if (form.value.regularId != undefined) {
        updateHrRegular(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrRegular(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const regularIds = row.regularId || ids.value
  proxy.$modal.confirm('是否确认删除选中的转正？').then(function() {
    return delHrRegular(regularIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/regular/export', { ...queryParams.value }, `hrRegular_${new Date().getTime()}.xlsx`)
}

getList()
</script>
