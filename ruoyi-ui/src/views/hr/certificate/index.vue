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
      <el-form-item label="证书类别" prop="certType">
        <el-input v-model="queryParams.certType" placeholder="请输入证书类别" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:certificate:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:certificate:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:certificate:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:certificate:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrCertificateList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="员工ID" prop="employeeId" align="center"  />
      <el-table-column label="员工姓名" prop="employeeName" align="center"  />
      <el-table-column label="工号" prop="employeeNo" align="center"  />
      <el-table-column label="证书类别" prop="certType" align="center"  />
      <el-table-column label="证书名称" prop="certName" align="center"  />
      <el-table-column label="证书编号" prop="certNo" align="center"  />
      <el-table-column label="发证日期" prop="issueDate" align="center" width="120" />
      <el-table-column label="有效起始日" prop="validFrom" align="center" width="120" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:certificate:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:certificate:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="证书台账" append-to-body>
      <el-form ref="hrCertificateRef" :model="form" :rules="rules" label-width="100px">
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
          <el-form-item label="证书类别" prop="certType">
            <el-input v-model="form.certType" placeholder="请输入证书类别" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="证书名称" prop="certName">
            <el-input v-model="form.certName" placeholder="请输入证书名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="证书编号" prop="certNo">
            <el-input v-model="form.certNo" placeholder="请输入证书编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发证日期" prop="issueDate">
            <el-date-picker v-model="form.issueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有效起始日" prop="validFrom">
            <el-date-picker v-model="form.validFrom" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有效期至" prop="validTo">
            <el-date-picker v-model="form.validTo" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="复审日期" prop="reviewDate">
            <el-date-picker v-model="form.reviewDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发证机构" prop="issueOrg">
            <el-input v-model="form.issueOrg" placeholder="请输入发证机构" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="附件URL" prop="attachmentUrl">
            <el-input v-model="form.attachmentUrl" placeholder="请输入附件URL" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预警状态" prop="remindStatus">
            <el-input v-model="form.remindStatus" placeholder="请输入预警状态" />
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

<script setup name="HrCertificate">
import { listHrCertificate, getHrCertificate, addHrCertificate, updateHrCertificate, delHrCertificate } from '@/api/hr/hrCertificate'

const { proxy } = getCurrentInstance()

const hrCertificateList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, employeeNo: undefined, certType: undefined },
  rules: {
    employeeId: [{ required: true, message: '员工ID不能为空', trigger: 'blur' }],
    certType: [{ required: true, message: '证书类别不能为空', trigger: 'blur' }],
    certName: [{ required: true, message: '证书名称不能为空', trigger: 'blur' }],
    validTo: [{ required: true, message: '有效期至不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrCertificate(queryParams.value).then(response => {
    hrCertificateList.value = response.rows
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
  queryParams.value.certType = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.certId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    certType: undefined,
    certName: undefined,
    certNo: undefined,
    issueDate: undefined,
    validFrom: undefined,
    validTo: undefined,
    reviewDate: undefined,
    issueOrg: undefined,
    attachmentUrl: undefined,
    remindStatus: undefined
  }
  proxy.resetForm('hrCertificateRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const certId = row.certId || ids.value[0]
  getHrCertificate(certId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrCertificateRef'].validate(valid => {
    if (valid) {
      if (form.value.certId != undefined) {
        updateHrCertificate(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrCertificate(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const certIds = row.certId || ids.value
  proxy.$modal.confirm('是否确认删除选中的证书台账？').then(function() {
    return delHrCertificate(certIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/certificate/export', { ...queryParams.value }, `hrCertificate_${new Date().getTime()}.xlsx`)
}

getList()
</script>
