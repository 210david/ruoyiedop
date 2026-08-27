<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="姓名" prop="workerName">
        <el-input v-model="queryParams.workerName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用工形态" prop="workerType">
        <el-input v-model="queryParams.workerType" placeholder="请输入用工形态" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:laborWorker:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:laborWorker:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:laborWorker:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:laborWorker:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrLaborWorkerList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="姓名" prop="workerName" align="center"  />
      <el-table-column label="身份证号" prop="idCard" align="center"  />
      <el-table-column label="联系电话" prop="phone" align="center"  />
      <el-table-column label="用工形态" prop="workerType" align="center"  />
      <el-table-column label="派遣公司" prop="dispatchCompany" align="center"  />
      <el-table-column label="开户银行" prop="bankName" align="center"  />
      <el-table-column label="银行卡号" prop="bankAccount" align="center"  />
      <el-table-column label="分配组织ID" prop="orgId" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:laborWorker:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:laborWorker:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="劳务用工" append-to-body>
      <el-form ref="hrLaborWorkerRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="姓名" prop="workerName">
            <el-input v-model="form.workerName" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用工形态" prop="workerType">
            <el-input v-model="form.workerType" placeholder="请输入用工形态" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="派遣公司" prop="dispatchCompany">
            <el-input v-model="form.dispatchCompany" placeholder="请输入派遣公司" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开户银行" prop="bankName">
            <el-input v-model="form.bankName" placeholder="请输入开户银行" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="银行卡号" prop="bankAccount">
            <el-input v-model="form.bankAccount" placeholder="请输入银行卡号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分配组织ID" prop="orgId">
            <el-input-number v-model="form.orgId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="岗位" prop="positionName">
            <el-input v-model="form.positionName" placeholder="请输入岗位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用工开始日期" prop="startDate">
            <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="用工结束日期" prop="endDate">
            <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结算方式" prop="payType">
            <el-input v-model="form.payType" placeholder="请输入结算方式" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="日薪标准" prop="dailyWage">
            <el-input-number v-model="form.dailyWage" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="协议编号" prop="agreementNo">
            <el-input v-model="form.agreementNo" placeholder="请输入协议编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否转正" prop="toRegular">
            <el-input v-model="form.toRegular" placeholder="请输入是否转正" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="转正后员工ID" prop="employeeId">
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

<script setup name="HrLaborWorker">
import { listHrLaborWorker, getHrLaborWorker, addHrLaborWorker, updateHrLaborWorker, delHrLaborWorker } from '@/api/hr/hrLaborWorker'

const { proxy } = getCurrentInstance()

const hrLaborWorkerList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, workerName: undefined, idCard: undefined, phone: undefined, workerType: undefined },
  rules: {
    workerName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
    startDate: [{ required: true, message: '用工开始日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrLaborWorker(queryParams.value).then(response => {
    hrLaborWorkerList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.workerName = undefined
  queryParams.value.idCard = undefined
  queryParams.value.phone = undefined
  queryParams.value.workerType = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.laborId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    workerName: undefined,
    idCard: undefined,
    phone: undefined,
    workerType: undefined,
    dispatchCompany: undefined,
    bankName: undefined,
    bankAccount: undefined,
    orgId: undefined,
    positionName: undefined,
    startDate: undefined,
    endDate: undefined,
    payType: undefined,
    dailyWage: undefined,
    agreementNo: undefined,
    toRegular: undefined,
    employeeId: undefined
  }
  proxy.resetForm('hrLaborWorkerRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const laborId = row.laborId || ids.value[0]
  getHrLaborWorker(laborId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrLaborWorkerRef'].validate(valid => {
    if (valid) {
      if (form.value.laborId != undefined) {
        updateHrLaborWorker(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrLaborWorker(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const laborIds = row.laborId || ids.value
  proxy.$modal.confirm('是否确认删除选中的劳务用工？').then(function() {
    return delHrLaborWorker(laborIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/laborWorker/export', { ...queryParams.value }, `hrLaborWorker_${new Date().getTime()}.xlsx`)
}

getList()
</script>
