<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="入职单号" prop="entryNo">
        <el-input v-model="queryParams.entryNo" placeholder="请输入入职单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="employeeName">
        <el-input v-model="queryParams.employeeName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-input v-model="queryParams.gender" placeholder="请输入性别" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:entry:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:entry:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:entry:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:entry:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrEntryList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="入职单号" prop="entryNo" align="center"  />
      <el-table-column label="姓名" prop="employeeName" align="center"  />
      <el-table-column label="身份证号" prop="idCard" align="center"  />
      <el-table-column label="性别" prop="gender" align="center"  />
      <el-table-column label="联系电话" prop="phone" align="center"  />
      <el-table-column label="组织ID" prop="orgId" align="center"  />
      <el-table-column label="组织名称" prop="orgName" align="center"  />
      <el-table-column label="岗位ID" prop="positionId" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:entry:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:entry:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="入职" append-to-body>
      <el-form ref="hrEntryRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="入职单号" prop="entryNo">
            <el-input v-model="form.entryNo" placeholder="请输入入职单号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="employeeName">
            <el-input v-model="form.employeeName" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-input v-model="form.gender" placeholder="请输入性别" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织名称" prop="orgName">
            <el-input v-model="form.orgName" placeholder="请输入组织名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗位ID" prop="positionId">
            <el-input-number v-model="form.positionId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
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
          <el-form-item label="试用期月数" prop="probationMonths">
            <el-input-number v-model="form.probationMonths" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="试用工资" prop="probationSalary">
            <el-input-number v-model="form.probationSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="转正工资" prop="regularSalary">
            <el-input-number v-model="form.regularSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同类型" prop="contractType">
            <el-input v-model="form.contractType" placeholder="请输入合同类型" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同期限" prop="contractPeriod">
            <el-input v-model="form.contractPeriod" placeholder="请输入合同期限" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="材料清单" prop="materialsStatus">
            <el-input v-model="form.materialsStatus" placeholder="请输入材料清单" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗前体检" prop="healthCheck">
            <el-input v-model="form.healthCheck" placeholder="请输入岗前体检" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="三级安全教育" prop="safetyEducation">
            <el-input v-model="form.safetyEducation" placeholder="请输入三级安全教育" />
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
        <el-col :span="12">
          <el-form-item label="员工档案ID" prop="employeeId">
            <el-input-number v-model="form.employeeId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
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

<script setup name="HrEntry">
import { listHrEntry, getHrEntry, addHrEntry, updateHrEntry, delHrEntry } from '@/api/hr/hrEntry'

const { proxy } = getCurrentInstance()

const hrEntryList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, entryNo: undefined, employeeName: undefined, idCard: undefined, gender: undefined },
  rules: {
    entryNo: [{ required: true, message: '入职单号不能为空', trigger: 'blur' }],
    employeeName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    entryDate: [{ required: true, message: '入职日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrEntry(queryParams.value).then(response => {
    hrEntryList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.entryNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.idCard = undefined
  queryParams.value.gender = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.entryId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    entryNo: undefined,
    employeeName: undefined,
    idCard: undefined,
    gender: undefined,
    phone: undefined,
    orgId: undefined,
    orgName: undefined,
    positionId: undefined,
    positionName: undefined,
    entryDate: undefined,
    probationMonths: undefined,
    probationSalary: undefined,
    regularSalary: undefined,
    contractType: undefined,
    contractPeriod: undefined,
    materialsStatus: undefined,
    healthCheck: undefined,
    safetyEducation: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined,
    employeeId: undefined
  }
  proxy.resetForm('hrEntryRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const entryId = row.entryId || ids.value[0]
  getHrEntry(entryId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrEntryRef'].validate(valid => {
    if (valid) {
      if (form.value.entryId != undefined) {
        updateHrEntry(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrEntry(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const entryIds = row.entryId || ids.value
  proxy.$modal.confirm('是否确认删除选中的入职？').then(function() {
    return delHrEntry(entryIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/entry/export', { ...queryParams.value }, `hrEntry_${new Date().getTime()}.xlsx`)
}

getList()
</script>
