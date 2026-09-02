<template>
  <el-dialog v-model="visible" width="640px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="role-picker">
      <!-- 搜索栏 -->
      <div class="role-picker-search">
        <el-input v-model="queryParams.roleName" placeholder="角色名称" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="queryParams.roleKey" placeholder="权限字符" clearable size="small" style="width: 160px; margin-left: 8px" @keyup.enter="handleQuery" />
        <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
      </div>

      <!-- 角色表格 -->
      <div class="role-picker-table">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="list"
          height="360"
          size="small"
          @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="45" align="center" />
          <el-table-column label="角色编号" prop="roleId" width="100" align="center" />
          <el-table-column label="角色名称" prop="roleName" min-width="160" show-overflow-tooltip />
          <el-table-column label="权限字符" prop="roleKey" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.status === '0'">正常</span>
              <span v-else-if="row.status === '1'">停用</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="role-picker-pager">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          small
          @current-change="getList"
        />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selection.length">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { listRole } from '@/api/system/role'

const props = defineProps({
  title: {
    type: String,
    default: '选择角色'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const list = ref([])
const total = ref(0)
const selection = ref([])
const pendingIds = ref([])
const tableRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: undefined,
  roleKey: undefined,
  status: '0'
})

/** 弹窗打开时初始化 */
function onOpen() {
  queryParams.roleName = undefined
  queryParams.roleKey = undefined
  queryParams.pageNum = 1
  selection.value = []
  getList()
}

/** 加载角色列表，并回显已选行 */
function getList() {
  loading.value = true
  listRole(queryParams).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    nextTick(() => {
      if (!tableRef.value) return
      list.value.forEach(row => {
        if (pendingIds.value.includes(row.roleId)) {
          tableRef.value.toggleRowSelection(row, true)
        }
      })
    })
  })
}

/** 查询 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.roleName = undefined
  queryParams.roleKey = undefined
  handleQuery()
}

/** 跨页保留已选行 */
function onSelectionChange(rows) {
  const pageIds = list.value.map(r => r.roleId)
  const kept = selection.value.filter(r => !pageIds.includes(r.roleId))
  selection.value = [...kept, ...rows]
}

/** 确认选择 */
function handleConfirm() {
  if (!selection.value.length) return
  emit('confirm', selection.value.map(r => ({
    roleId: r.roleId,
    roleName: r.roleName,
    roleKey: r.roleKey
  })))
  visible.value = false
}

/** 打开弹窗，传入已选角色ID数组用于回显 */
function open(currentRoleIds) {
  pendingIds.value = Array.isArray(currentRoleIds) ? currentRoleIds : []
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.role-picker {
  display: flex;
  flex-direction: column;
}

.role-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 4px;
}

.role-picker-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;

  :deep(.el-table__row) {
    cursor: pointer;

    &:hover > td {
      background: #f0f7ff;
    }
  }
}

.role-picker-pager {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
