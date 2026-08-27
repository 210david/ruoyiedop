<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="组织编码" prop="orgCode">
        <el-input v-model="queryParams.orgCode" placeholder="请输入组织编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="组织名称" prop="orgName">
        <el-input v-model="queryParams.orgName" placeholder="请输入组织名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="父组织ID" prop="parentId">
        <el-input v-model="queryParams.parentId" placeholder="请输入父组织ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="排序号" prop="orderNum">
        <el-input v-model="queryParams.orderNum" placeholder="请输入排序号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:organization:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:organization:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:organization:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:organization:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="hrOrganizationList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="组织编码" prop="orgCode" align="center"  />
      <el-table-column label="组织名称" prop="orgName" align="center"  />
      <el-table-column label="父组织ID" prop="parentId" align="center"  />
      <el-table-column label="排序号" prop="orderNum" align="center"  />
      <el-table-column label="组织层级" prop="orgLevel" align="center"  />
      <el-table-column label="负责人ID" prop="leaderId" align="center"  />
      <el-table-column label="负责人姓名" prop="leaderName" align="center"  />
      <el-table-column label="成本中心" prop="costCenter" align="center"  />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:organization:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:organization:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" title="组织架构" append-to-body>
      <el-form ref="hrOrganizationRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="组织编码" prop="orgCode">
            <el-input v-model="form.orgCode" placeholder="请输入组织编码" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织名称" prop="orgName">
            <el-input v-model="form.orgName" placeholder="请输入组织名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="父组织ID" prop="parentId">
            <el-input-number v-model="form.parentId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序号" prop="orderNum">
            <el-input-number v-model="form.orderNum" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="组织层级" prop="orgLevel">
            <el-input-number v-model="form.orgLevel" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人ID" prop="leaderId">
            <el-input-number v-model="form.leaderId" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="负责人姓名" prop="leaderName">
            <el-input v-model="form.leaderName" placeholder="请输入负责人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成本中心" prop="costCenter">
            <el-input-number v-model="form.costCenter" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="考勤归属" prop="attendanceOrg">
            <el-input-number v-model="form.attendanceOrg" :precision="0" :min="0" controls-position="right" style="width: 100%" />
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

<script setup name="HrOrganization">
import { listHrOrganization, getHrOrganization, addHrOrganization, updateHrOrganization, delHrOrganization } from '@/api/hr/hrOrganization'

const { proxy } = getCurrentInstance()

const hrOrganizationList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, orgCode: undefined, orgName: undefined, parentId: undefined, orderNum: undefined },
  rules: {
    orgCode: [{ required: true, message: '组织编码不能为空', trigger: 'blur' }],
    orgName: [{ required: true, message: '组织名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrOrganization(queryParams.value).then(response => {
    hrOrganizationList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.orgCode = undefined
  queryParams.value.orgName = undefined
  queryParams.value.parentId = undefined
  queryParams.value.orderNum = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.orgId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    orgCode: undefined,
    orgName: undefined,
    parentId: undefined,
    orderNum: undefined,
    orgLevel: undefined,
    leaderId: undefined,
    leaderName: undefined,
    costCenter: undefined,
    attendanceOrg: undefined
  }
  proxy.resetForm('hrOrganizationRef')
}

function handleAdd() {
  reset()
  open.value = true
}

function handleUpdate(row) {
  reset()
  const orgId = row.orgId || ids.value[0]
  getHrOrganization(orgId).then(response => {
    form.value = response.data
    open.value = true
  })
}

function submitForm() {
  proxy.$refs['hrOrganizationRef'].validate(valid => {
    if (valid) {
      if (form.value.orgId != undefined) {
        updateHrOrganization(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrOrganization(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const orgIds = row.orgId || ids.value
  proxy.$modal.confirm('是否确认删除选中的组织架构？').then(function() {
    return delHrOrganization(orgIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/organization/export', { ...queryParams.value }, `hrOrganization_${new Date().getTime()}.xlsx`)
}

getList()
</script>
