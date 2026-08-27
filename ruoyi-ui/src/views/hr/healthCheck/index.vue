<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="员工ID" prop="employeeId">
        <el-input v-model="queryParams.employeeId" placeholder="请输入员工ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="员工姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="工号" prop="employeeNo">
        <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="体检类型" prop="checkType">
        <el-input v-model="queryParams.checkType" placeholder="请输入体检类型" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:healthCheck:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:healthCheck:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:healthCheck:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:healthCheck:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrHealthCheckList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="体检类型" prop="checkType" align="center"  />
      <el-table-column label="体检日期" prop="checkDate" align="center" width="120" />
      <el-table-column label="体检机构" prop="checkOrg" align="center"  />
      <el-table-column label="职业危害因素" prop="hazardType" align="center"  />
      <el-table-column label="结论" prop="checkResult" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:healthCheck:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:healthCheck:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="职业健康体检" append-to-body>
      <el-form ref="hrHealthCheckRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
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
          <el-form-item label="体检类型" prop="checkType">
            <el-input v-model="form.checkType" placeholder="请输入体检类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体检日期" prop="checkDate">
            <el-date-picker v-model="form.checkDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体检机构" prop="checkOrg">
            <el-input v-model="form.checkOrg" placeholder="请输入体检机构" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="职业危害因素" prop="hazardType">
            <el-input v-model="form.hazardType" placeholder="请输入职业危害因素" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结论" prop="checkResult">
            <el-input v-model="form.checkResult" placeholder="请输入结论" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体检结论" prop="conclusion">
            <el-input v-model="form.conclusion" placeholder="请输入体检结论" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="附件URL" prop="attachmentUrl">
            <el-input v-model="form.attachmentUrl" placeholder="请输入附件URL" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下次体检日期" prop="nextCheckDate">
            <el-date-picker v-model="form.nextCheckDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
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

<script setup name="HrHealthCheck">
import { listHrHealthCheck, getHrHealthCheck, addHrHealthCheck, updateHrHealthCheck, delHrHealthCheck } from '@/api/hr/hrHealthCheck'

const { proxy } = getCurrentInstance()

const hrHealthCheckList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, employeeNo: undefined, checkType: undefined },
  rules: {
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    checkType: [{ required: true, message: '体检类型不能为空', trigger: 'blur' }],
    checkDate: [{ required: true, message: '体检日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrHealthCheck(queryParams.value).then(response => {
    hrHealthCheckList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeId = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.checkType = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.healthId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    checkType: undefined,
    checkDate: undefined,
    checkOrg: undefined,
    hazardType: undefined,
    checkResult: undefined,
    conclusion: undefined,
    attachmentUrl: undefined,
    nextCheckDate: undefined
  }
  proxy.resetForm('hrHealthCheckRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const healthId = row.healthId || ids.value[0]
  getHrHealthCheck(healthId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrHealthCheckRef'].validate(valid => {
    if (valid) {
      if (form.value.healthId != undefined) {
        updateHrHealthCheck(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrHealthCheck(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const healthIds = row.healthId || ids.value
  proxy.$modal.confirm('是否确认删除选中的职业健康体检？').then(function() {
    return delHrHealthCheck(healthIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/healthCheck/export', { ...queryParams.value }, `hrHealthCheck_${new Date().getTime()}.xlsx`)
}

getList()
</script>
