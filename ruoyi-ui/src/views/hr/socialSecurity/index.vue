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
      <el-form-item label="年度" prop="year">
        <el-input v-model="queryParams.year" placeholder="请输入年度" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:socialSecurity:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:socialSecurity:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:socialSecurity:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:socialSecurity:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrSocialSecurityList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="年度" prop="year" align="center"  />
      <el-table-column label="缴费基数" prop="baseAmount" align="center"  />
      <el-table-column label="养老单位比例" prop="pensionEmployer" align="center"  />
      <el-table-column label="养老个人比例" prop="pensionEmployee" align="center"  />
      <el-table-column label="医疗单位比例" prop="medicalEmployer" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:socialSecurity:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:socialSecurity:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="社保公积金" append-to-body>
      <el-form ref="hrSocialSecurityRef" :model="form" :rules="rules" label-width="100px">
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
          <el-form-item label="年度" prop="year">
            <el-input-number v-model="form.year" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="缴费基数" prop="baseAmount">
            <el-input-number v-model="form.baseAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="养老单位比例" prop="pensionEmployer">
            <el-input-number v-model="form.pensionEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="养老个人比例" prop="pensionEmployee">
            <el-input-number v-model="form.pensionEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="医疗单位比例" prop="medicalEmployer">
            <el-input-number v-model="form.medicalEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="医疗个人比例" prop="medicalEmployee">
            <el-input-number v-model="form.medicalEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失业单位比例" prop="unemploymentEmployer">
            <el-input-number v-model="form.unemploymentEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="失业个人比例" prop="unemploymentEmployee">
            <el-input-number v-model="form.unemploymentEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工伤单位比例" prop="injuryEmployer">
            <el-input-number v-model="form.injuryEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生育单位比例" prop="maternityEmployer">
            <el-input-number v-model="form.maternityEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公积金单位比例" prop="fundEmployer">
            <el-input-number v-model="form.fundEmployer" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公积金个人比例" prop="fundEmployee">
            <el-input-number v-model="form.fundEmployee" :precision="2" :min="0" controls-position="right" style="width: 100%" />
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

<script setup name="HrSocialSecurity">
import { listHrSocialSecurity, getHrSocialSecurity, addHrSocialSecurity, updateHrSocialSecurity, delHrSocialSecurity } from '@/api/hr/hrSocialSecurity'

const { proxy } = getCurrentInstance()

const hrSocialSecurityList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, employeeNo: undefined, year: undefined },
  rules: {
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    year: [{ required: true, message: '年度不能为空', trigger: 'blur' }],
    baseAmount: [{ required: true, message: '缴费基数不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrSocialSecurity(queryParams.value).then(response => {
    hrSocialSecurityList.value = response.rows
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
  queryParams.value.year = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.ssId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    year: undefined,
    baseAmount: undefined,
    pensionEmployer: undefined,
    pensionEmployee: undefined,
    medicalEmployer: undefined,
    medicalEmployee: undefined,
    unemploymentEmployer: undefined,
    unemploymentEmployee: undefined,
    injuryEmployer: undefined,
    maternityEmployer: undefined,
    fundEmployer: undefined,
    fundEmployee: undefined
  }
  proxy.resetForm('hrSocialSecurityRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const ssId = row.ssId || ids.value[0]
  getHrSocialSecurity(ssId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSocialSecurityRef'].validate(valid => {
    if (valid) {
      if (form.value.ssId != undefined) {
        updateHrSocialSecurity(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSocialSecurity(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const ssIds = row.ssId || ids.value
  proxy.$modal.confirm('是否确认删除选中的社保公积金？').then(function() {
    return delHrSocialSecurity(ssIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/socialSecurity/export', { ...queryParams.value }, `hrSocialSecurity_${new Date().getTime()}.xlsx`)
}

getList()
</script>
