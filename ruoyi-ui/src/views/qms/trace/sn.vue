<template>
  <div class="app-container">
    <!-- Filter -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> SN追溯查询</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>序列号</label>
          <div class="control">
            <el-input v-model="queryParams.snCode" placeholder="请输入序列号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入工单号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="在制" value="0" />
              <el-option label="入库" value="1" />
              <el-option label="发货" value="2" />
              <el-option label="退货" value="3" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="surface">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:trace:sn:add']">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:trace:sn:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:trace:sn:remove']">删除</el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="snList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" prop="id" width="80" align="center" />
        <el-table-column label="序列号" prop="snCode" width="200" align="center">
          <template #default="scope">
            <el-link type="primary" @click="handleTrace(scope.row)">{{ scope.row.snCode }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" width="160" align="center" />
        <el-table-column label="工单号" prop="workOrderNo" width="140" align="center" />
        <el-table-column label="物料编码" prop="materialCode" width="160" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="snStatusType(scope.row.status)" size="small">{{ snStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleTrace(scope.row)">追溯</el-button>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:trace:sn:edit']">修改</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="snRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="序列号" prop="snCode">
          <el-input v-model="form.snCode" placeholder="请输入序列号" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderNo">
              <el-input v-model="form.workOrderNo" placeholder="请输入工单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物料编码" prop="materialCode">
              <el-input v-model="form.materialCode" placeholder="请输入物料编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="在制" value="0" />
                <el-option label="入库" value="1" />
                <el-option label="发货" value="2" />
                <el-option label="退货" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- Trace Result Dialog -->
    <el-dialog title="SN追溯结果" v-model="traceOpen" width="800px" append-to-body>
      <div v-if="traceData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="序列号">{{ traceData.snCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ snStatusText(traceData.status) }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ traceData.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="工单号">{{ traceData.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="物料编码">{{ traceData.materialCode }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ traceData.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <div v-if="batchTraceData">
          <h4>批次追溯信息</h4>
          <el-table :data="batchTraceList" border size="small">
            <el-table-column label="序列号" prop="snCode" width="200" />
            <el-table-column label="状态" width="100">
              <template #default="scope">{{ snStatusText(scope.row.status) }}</template>
            </el-table-column>
            <el-table-column label="物料编码" prop="materialCode" />
            <el-table-column label="创建时间" prop="createTime" width="160" />
          </el-table>
        </div>
      </div>
      <div v-else style="text-align: center; padding: 40px;">
        <el-empty description="未找到追溯信息" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { listSn, getSn, addSn, updateSn, delSn, getSnByCode, getSnByBatch, exportTracePdf } from '@/api/qms/traceSn'

const { proxy } = getCurrentInstance()
const snList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const traceOpen = ref(false)
const traceData = ref(null)
const batchTraceData = ref(null)
const batchTraceList = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, snCode: undefined, batchNo: undefined, workOrderNo: undefined, materialCode: undefined, status: undefined },
  rules: {
    snCode: [{ required: true, message: '序列号不能为空', trigger: 'blur' }],
    batchNo: [{ required: true, message: '批次号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function snStatusText(status) {
  const map = { '0': '在制', '1': '入库', '2': '发货', '3': '退货' }
  return map[status] || '未知'
}

function snStatusType(status) {
  const map = { '0': 'warning', '1': 'primary', '2': 'success', '3': 'danger' }
  return map[status] || 'info'
}

function getList() {
  loading.value = true
  listSn(queryParams.value).then(res => {
    snList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = { snCode: undefined, batchNo: undefined, workOrderNo: undefined, materialCode: undefined, status: '0' }
  proxy.resetForm('snRef')
}

function handleAdd() { reset(); open.value = true; title.value = '新增SN记录' }

function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getSn(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改SN记录'
  })
}

function submitForm() {
  proxy.$refs['snRef'].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateSn(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addSn(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

function handleDelete(row) {
  const idArr = row.id ? [row.id] : ids.value
  proxy.$modal.confirm('确认删除选中的SN记录?').then(() => {
    return delSn(idArr)
  }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

async function handleTrace(row) {
  traceData.value = null
  batchTraceData.value = null
  batchTraceList.value = []
  traceOpen.value = true

  // 查询SN详情
  const res = await getSnByCode(row.snCode)
  if (res.data) {
    traceData.value = res.data
    // 查询同批次SN列表
    if (res.data.batchNo) {
      const batchRes = await getSnByBatch(res.data.batchNo)
      batchTraceData.value = batchRes.data
      batchTraceList.value = batchRes.data || []
    }
  }
}

function cancel() { open.value = false; reset() }

getList()
</script>
