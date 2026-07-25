<template>
  <el-dialog v-model="visible" width="780px" append-to-body draggable class="rd-dialog" @open="onOpen">
    <template #header>
      <div class="rd-detail-header">
        <div class="rd-detail-header-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <span class="rd-detail-header-title">{{ title }}</span>
      </div>
    </template>
    <div class="user-picker">
      <!-- 左侧部门树 -->
      <div class="picker-left">
        <div class="picker-left-header">
          <el-icon><OfficeBuilding /></el-icon>
          <span>组织机构</span>
        </div>
        <div class="picker-left-search">
          <el-input v-model="deptKeyword" placeholder="搜索部门" clearable size="small">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="picker-left-tree">
          <el-tree
            ref="deptTreeRef"
            :data="deptOptions"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            :expand-on-click-node="false"
            :default-expand-all="true"
            :filter-node-method="filterDeptNode"
            highlight-current
            @node-click="onDeptClick"
          >
            <template #default="{ data }">
              <span class="dept-node">
                <span class="node-icon-badge" :class="data.children && data.children.length ? 'node-icon-badge--dept' : 'node-icon-badge--leaf'">
                  <el-icon><Folder v-if="data.children && data.children.length" /><Document v-else /></el-icon>
                </span>
                <span class="dept-label" :title="data.label">{{ data.label }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>

      <!-- 右侧用户列表 -->
      <div class="picker-right">
        <div class="picker-right-toolbar">
          <el-input v-model="userName" placeholder="搜索姓名" clearable size="small" style="width: 220px" @keyup.enter="loadUsers">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" @click="loadUsers">查询</el-button>
          <el-button icon="Refresh" size="small" @click="resetSearch">重置</el-button>
          <div class="picker-right-info" v-if="selectedUser">
            <el-tag type="success" effect="light" size="small" closable @close="clearSelection">
              {{ selectedUser.nickName }}
              <span class="tag-dept" v-if="selectedUser.dept && selectedUser.dept.deptName"> | {{ selectedUser.dept.deptName }}</span>
            </el-tag>
          </div>
        </div>
        <el-table
          v-loading="loading"
          :data="filteredUsers"
          border
          size="small"
          highlight-current-row
          height="360"
          @row-click="onRowClick"
          @row-dblclick="onRowDblClick"
          @current-change="onCurrentChange"
        >
          <el-table-column width="40" align="center">
            <template #default="scope">
              <el-radio v-model="selectedUserId" :value="scope.row.userId" @click.stop>&nbsp;</el-radio>
            </template>
          </el-table-column>
          <el-table-column label="姓名" prop="nickName" min-width="90" show-overflow-tooltip />
          <el-table-column label="岗位" prop="postName" min-width="110" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.postName || '-' }}</template>
          </el-table-column>
          <el-table-column label="部门" prop="dept.deptName" min-width="100" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.dept ? scope.row.dept.deptName : '-' }}</template>
          </el-table-column>
          <el-table-column label="手机号" prop="phonenumber" min-width="110" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.phonenumber || '-' }}</template>
          </el-table-column>
        </el-table>
        <div class="picker-right-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>点击行选中，双击行确认选择</span>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :disabled="!selectedUserId">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { deptTreeSelect, listUser } from '@/api/system/user'

const props = defineProps({
  title: {
    type: String,
    default: '选择人员'
  }
})

const emit = defineEmits(['confirm'])

const visible = ref(false)
const loading = ref(false)
const deptOptions = ref([])
const deptKeyword = ref('')
const userName = ref('')
const selectedDeptId = ref(undefined)
const allUsers = ref([])
const selectedUserId = ref(null)
const selectedUser = ref(null)

const deptTreeRef = ref()

/** 前端过滤后的用户列表 */
const filteredUsers = computed(() => {
  let result = allUsers.value
  // 过滤掉系统管理员（不属于正常组织用户）
  result = result.filter(u => u.userId !== 1 && u.userName !== 'admin')
  // 按部门过滤
  if (selectedDeptId.value) {
    result = result.filter(u => u.deptId === selectedDeptId.value)
  }
  // 按姓名搜索
  if (userName.value) {
    const kw = userName.value.toLowerCase()
    result = result.filter(u =>
      (u.nickName && u.nickName.toLowerCase().includes(kw))
    )
  }
  return result
})

/** 过滤部门节点 */
function filterDeptNode(value, data) {
  if (!value) return true
  return data.label && data.label.indexOf(value) !== -1
}

/** 监听部门搜索关键词 */
watch(deptKeyword, (val) => {
  if (deptTreeRef.value) {
    deptTreeRef.value.filter(val)
  }
})

/** 弹窗打开时初始化 */
function onOpen() {
  userName.value = ''
  selectedDeptId.value = undefined
  selectedUserId.value = null
  selectedUser.value = null
  deptKeyword.value = ''
  loadDeptTree()
  loadUsers()
}

/** 加载部门树 */
function loadDeptTree() {
  deptTreeSelect().then(res => {
    deptOptions.value = res.data
  })
}

/** 加载用户列表（全量加载，前端过滤） */
function loadUsers() {
  loading.value = true
  listUser({ pageNum: 1, pageSize: 9999 }).then(res => {
    allUsers.value = res.rows
  }).finally(() => {
    loading.value = false
  })
}

/** 重置搜索 */
function resetSearch() {
  userName.value = ''
  selectedDeptId.value = undefined
  if (deptTreeRef.value) {
    deptTreeRef.value.setCurrentKey(null)
  }
}

/** 部门树点击 */
function onDeptClick(data) {
  selectedDeptId.value = data.id
}

/** 行点击 - 选中 */
function onRowClick(row) {
  selectedUserId.value = row.userId
}

/** 行双击 - 确认 */
function onRowDblClick(row) {
  selectedUserId.value = row.userId
  selectedUser.value = row
  handleConfirm()
}

/** 当前行变化 */
function onCurrentChange(row) {
  if (row) {
    selectedUser.value = row
  }
}

/** 清除选择 */
function clearSelection() {
  selectedUserId.value = null
  selectedUser.value = null
}

/** 确认选择 */
function handleConfirm() {
  if (!selectedUserId.value) return
  // 从 filteredUsers 中找到选中的用户
  const user = filteredUsers.value.find(u => u.userId === selectedUserId.value)
  if (user) {
    emit('confirm', {
      userId: user.userId,
      nickName: user.nickName,
      userName: user.userName,
      postName: user.postName,
      deptId: user.dept ? user.dept.deptId : undefined,
      deptName: user.dept ? user.dept.deptName : undefined,
      phonenumber: user.phonenumber
    })
  }
  visible.value = false
}

/** 打开弹窗 */
function open(currentUserId) {
  visible.value = true
  // 如果传入了当前选中的用户ID，在加载后回显
  if (currentUserId) {
    nextTick(() => {
      selectedUserId.value = currentUserId
    })
  }
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.user-picker {
  display: flex;
  gap: 12px;
  height: 440px;
}

/* 左侧部门树 */
.picker-left {
  width: 240px;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fafbfc;
}

.picker-left-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 12px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;

  .el-icon {
    color: #409eff;
    font-size: 15px;
  }
}

.picker-left-search {
  padding: 8px 10px;
  border-bottom: 1px solid #f0f0f0;
}

.picker-left-tree {
  flex: 1;
  overflow-y: auto;
  padding: 4px 6px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 4px;
  }

  :deep(.el-tree-node__content) {
    height: 30px;
    border-radius: 4px;
    margin-bottom: 1px;

    &:hover {
      background: #f0f7ff;
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background: #e6f0fd;
    color: #409eff;
    font-weight: 600;
  }
}

.dept-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  overflow: hidden;

  .dept-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.node-icon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 5px;
  flex-shrink: 0;

  .el-icon {
    font-size: 12px;
    color: #fff;
  }

  &--dept {
    background: linear-gradient(135deg, #f5b454, #e6a23c);
  }

  &--leaf {
    background: linear-gradient(135deg, #67c23a, #529b2e);
  }
}

/* 右侧用户列表 */
.picker-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.picker-right-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.picker-right-info {
  margin-left: auto;
  display: flex;
  align-items: center;

  .tag-dept {
    color: #909399;
    font-size: 12px;
  }
}

.picker-right-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;

  .el-icon {
    font-size: 14px;
  }
}

:deep(.el-table) {
  .el-table__row {
    cursor: pointer;
  }
}
</style>
