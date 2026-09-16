<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- 搜索区 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
        <el-form-item label="模块" prop="module">
          <el-select v-model="queryParams.module" placeholder="全部模块" clearable style="width: 160px">
            <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="成功" value="0" />
            <el-option label="降级" value="1" />
            <el-option label="失败" value="2" />
            <el-option label="限额拦截" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="userName">
          <el-input v-model="queryParams.userName" placeholder="请输入操作人" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="功能" prop="feature">
          <el-input v-model="queryParams.feature" placeholder="如 repair_assistant" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="调用时间">
          <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange"
            range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表 -->
      <el-table v-loading="loading" :data="logList">
        <el-table-column label="日志ID" prop="logId" width="90" align="center" />
        <el-table-column label="模块" width="110" align="center">
          <template #default="scope">
            <el-tag size="small">{{ moduleNames[scope.row.module] || scope.row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="功能" prop="feature" min-width="150" />
        <el-table-column label="操作人" prop="userName" width="110" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusType[scope.row.status]" size="small">{{ statusText[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="center">
          <template #default="scope">{{ scope.row.durationMs || 0 }} ms</template>
        </el-table-column>
        <el-table-column label="调用时间" prop="callTime" width="170" align="center" />
        <el-table-column label="错误信息" prop="errorMsg" min-width="180" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.errorMsg || '—' }}</template>
        </el-table-column>
        <el-table-column label="提示词摘要" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.promptDigest || '—' }}</template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="AiLog">
import { listAiLog } from '@/api/ai/center'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const logList = ref([])
const total = ref(0)
const dateRange = ref([])

const moduleNames = {
  dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产', platform: '平台'
}
const statusText = { '0': '成功', '1': '降级', '2': '失败', '3': '限额拦截' }
const statusType = { '0': 'success', '1': 'warning', '2': 'danger', '3': 'info' }

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    module: undefined,
    feature: undefined,
    userName: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listAiLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    logList.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

getList()
</script>
