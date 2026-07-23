<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="应用名称" prop="appName">
        <el-input v-model="queryParams.appName" placeholder="请输入应用名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:openapi:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:openapi:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:openapi:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="openapiList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="应用ID" align="center" prop="appId" width="80" />
      <el-table-column label="应用名称" align="center" prop="appName" />
      <el-table-column label="API Key" align="center" prop="appKey" show-overflow-tooltip>
        <template #default="scope">
          <span style="font-family: monospace;">{{ scope.row.appKey }}</span>
        </template>
      </el-table-column>
      <el-table-column label="允许模块" align="center" prop="allowedModules" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:openapi:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:openapi:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="openapiRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="请输入对接方应用名称" />
        </el-form-item>
        <el-form-item label="API Key" prop="appKey">
          <el-input v-model="form.appKey" placeholder="留空则自动生成">
            <template #append>
              <el-button @click="generateKey">生成</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="允许模块" prop="allowedModules">
          <el-input v-model="form.allowedModules" placeholder="* 表示全部，或逗号分隔如 wms,dms" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
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

<script setup>
import { listOpenApi, getOpenApi, addOpenApi, updateOpenApi, delOpenApi } from '@/api/system/openapi'

const { proxy } = getCurrentInstance()

const openapiList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    appName: undefined,
    status: undefined
  },
  rules: {
    appName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }],
    appKey: [{ required: true, message: 'API Key不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listOpenApi(queryParams.value).then(res => {
    openapiList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.appId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    appId: undefined,
    appName: undefined,
    appKey: undefined,
    allowedModules: '*',
    status: '0',
    remark: undefined
  }
  proxy.resetForm('openapiRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增开放接口应用'
}

function handleUpdate(row) {
  reset()
  const appId = row.appId || ids.value
  getOpenApi(appId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改开放接口应用'
  })
}

function submitForm() {
  proxy.$refs['openapiRef'].validate(valid => {
    if (valid) {
      if (form.value.appId != null) {
        updateOpenApi(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addOpenApi(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const appIds = row.appId || ids.value
  proxy.$modal.confirm('是否确认删除应用编号为"' + appIds + '"的数据项？').then(() => {
    return delOpenApi(appIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function generateKey() {
  form.value.appKey = 'ak-' + Date.now() + '-' + Math.floor(Math.random() * 10000)
}

getList()
</script>
