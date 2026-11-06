<template>
  <div class="app-container sys-connector-grant-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>用户账号</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <span class="toolbar-title">连接器授权管理</span>
          <div class="toolbar-divider"></div>
          <span class="toolbar-hint">持有 grant_token 的用户可通过 WorkBuddy 连接器按本人数据权限查询平台数据</span>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="edop_grant_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="grantList" border class="app-table">
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="用户账号" prop="userName" min-width="110" align="center" show-overflow-tooltip v-if="columns.userName.visible" />
          <el-table-column label="部门" prop="deptName" min-width="130" align="center" show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="手机号快照" prop="phoneSnapshot" width="130" align="center" show-overflow-tooltip v-if="columns.phoneSnapshot.visible" />
          <el-table-column label="授权来源" prop="clientDesc" min-width="150" align="center" show-overflow-tooltip v-if="columns.clientDesc.visible" />
          <el-table-column label="状态" prop="status" width="90" align="center" v-if="columns.status.visible">
            <template #default="scope">
              <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column label="最近使用" prop="lastUsedTime" width="160" align="center" v-if="columns.lastUsedTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.lastUsedTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="过期时间" prop="expireTime" width="160" align="center" v-if="columns.expireTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.expireTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="160" align="center" v-if="columns.createTime.visible">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="danger" icon="CircleClose" :disabled="scope.row.status === '1'" @click="handleRevoke(scope.row)" v-hasPermi="['edop:grant:revoke']">吊销</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup name="ConnectorGrant">
import { listGrant, revokeGrant } from "@/api/system/connectorGrant"
import { Filter } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")

const grantList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const columns = ref({
  userName: { label: '用户账号', visible: true },
  deptName: { label: '部门', visible: true },
  phoneSnapshot: { label: '手机号快照', visible: true },
  clientDesc: { label: '授权来源', visible: true },
  status: { label: '状态', visible: true },
  lastUsedTime: { label: '最近使用', visible: true },
  expireTime: { label: '过期时间', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

const activeFilterCount = computed(() => {
  return [queryParams.value.userName, queryParams.value.status].filter(v => v !== undefined && v !== null && v !== '').length
})

/** 查询授权列表 */
function getList() {
  loading.value = true
  listGrant(queryParams.value).then(response => {
    grantList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.userName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

/** 吊销按钮操作 */
function handleRevoke(row) {
  proxy.$modal.confirm('确认吊销用户「' + row.userName + '」的连接器授权吗？吊销后该用户需重新授权才能继续使用。').then(() => {
    return revokeGrant(row.grantId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("吊销成功")
  }).catch(() => {})
}

getList()
</script>
