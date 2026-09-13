<template>
   <!-- 授权用户 -->
   <el-dialog v-model="visible" width="960px" top="5vh" append-to-body draggable class="rd-dialog">
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
            <span class="rd-detail-header-title">选择用户</span>
         </div>
      </template>

      <div class="select-user-picker">
         <div class="picker-body">
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
                           <span class="node-icon-badge" :class="deptBadgeClass(data)">
                              <el-icon><OfficeBuilding v-if="isRootDept(data)" /><Folder v-else-if="data.children && data.children.length" /><Document v-else /></el-icon>
                           </span>
                           <span class="dept-label" :title="data.label">{{ data.label }}</span>
                        </span>
                     </template>
                  </el-tree>
               </div>
            </div>

            <!-- 右侧用户列表 -->
            <div class="picker-right">
               <!-- 搜索栏 -->
               <div class="select-user-picker-search">
                  <el-input v-model="queryParams.userName" placeholder="用户名称" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
                     <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
                  <el-input v-model="queryParams.nickName" placeholder="用户昵称" clearable size="small" style="width: 160px" @keyup.enter="handleQuery">
                     <template #prefix><el-icon><User /></el-icon></template>
                  </el-input>
                  <el-button type="primary" plain icon="Search" size="small" @click="handleQuery">查询</el-button>
                  <el-button icon="RefreshLeft" size="small" @click="resetQuery">重置</el-button>
               </div>

               <!-- 数据表格 -->
               <div class="select-user-picker-table">
                  <el-table ref="refTable" v-loading="loading" :data="userList" row-key="userId"
                     @row-click="clickRow" height="360" size="small">
                     <el-table-column width="55" align="center">
                        <template #header>
                           <el-checkbox
                              :model-value="isPageAllSelected"
                              :indeterminate="isPageIndeterminate"
                              @change="togglePageAll"
                           />
                        </template>
                        <template #default="scope">
                           <el-checkbox
                              :model-value="selectedIds.has(scope.row.userId)"
                              @change="val => toggleRow(scope.row, val)"
                              @click.stop
                           />
                        </template>
                     </el-table-column>
                     <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true" />
                     <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true" />
                     <el-table-column label="部门" prop="dept.deptName" :show-overflow-tooltip="true" />
                     <el-table-column label="岗位" prop="postName" :show-overflow-tooltip="true" />
                     <el-table-column label="创建时间" align="center" prop="createTime" width="160">
                        <template #default="scope">
                           <span>{{ parseTime(scope.row.createTime) }}</span>
                        </template>
                     </el-table-column>
                  </el-table>
               </div>

               <!-- 分页 -->
               <div class="select-user-picker-bottom">
                  <el-tag type="success" effect="light" size="small" closable v-if="selectedUsers.length" @close="clearAllSelection">
                     已选 {{ selectedUsers.length }} 人
                  </el-tag>
                  <div class="select-user-picker-pager">
                     <pagination
                        v-show="total > 0"
                        :total="total"
                        v-model:page="queryParams.pageNum"
                        v-model:limit="queryParams.pageSize"
                        layout="total, prev, pager, next"
                        @pagination="getList"
                     />
                  </div>
               </div>
            </div>
         </div>
      </div>

      <template #footer>
         <div class="dialog-footer">
            <el-button @click="visible = false">取 消</el-button>
            <el-button type="primary" :disabled="!selectedUsers.length" @click="handleSelectUser">确 定</el-button>
         </div>
      </template>
   </el-dialog>
</template>

<script setup name="SelectUser">
import { authUserSelectAll, unallocatedUserList } from "@/api/system/role"
import { deptTreeSelect } from "@/api/system/user"

const props = defineProps({
  roleId: {
    type: [Number, String]
  }
})

const { proxy } = getCurrentInstance()

const userList = ref([])
const loading = ref(false)
const visible = ref(false)
const total = ref(0)
const selectedUsers = ref([])
const deptOptions = ref([])
const deptKeyword = ref("")

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  deptId: undefined,
  userName: undefined,
  nickName: undefined
})

watch(deptKeyword, val => {
  proxy.$refs["deptTreeRef"]?.filter(val)
})

/** 显示弹框 */
function show() {
  queryParams.roleId = props.roleId
  queryParams.deptId = undefined
  queryParams.userName = undefined
  queryParams.nickName = undefined
  queryParams.pageNum = 1
  selectedUsers.value = []
  getDeptTree()
  getList()
  visible.value = true
}

/** 查询部门树 */
function getDeptTree() {
  deptTreeSelect().then(res => {
    deptOptions.value = res.data
  })
}

/** 顶层节点（公司级）ID集合 */
const rootDeptIds = computed(() => new Set(deptOptions.value.map(d => d.id)))

function isRootDept(data) {
  return rootDeptIds.value.has(data.id)
}

/** 部门树节点徽标类型：公司级/部门级/叶子 */
function deptBadgeClass(data) {
  if (isRootDept(data)) return "node-icon-badge--company"
  return data.children && data.children.length ? "node-icon-badge--dept" : "node-icon-badge--leaf"
}

/** 部门树过滤 */
function filterDeptNode(value, data) {
  if (!value) return true
  return data.label.indexOf(value) !== -1
}

/** 点击部门节点 */
function onDeptClick(data) {
  queryParams.deptId = data.id === queryParams.deptId ? undefined : data.id
  queryParams.pageNum = 1
  getList()
}

/** 清空所有已选 */
function clearAllSelection() {
  selectedUsers.value = []
}

/**选择行 */
function clickRow(row) {
  toggleRow(row, !selectedIds.value.has(row.userId))
}

/** 勾选/取消单个用户 */
function toggleRow(row, val) {
  const idx = selectedUsers.value.findIndex(u => u.userId === row.userId)
  if (val && idx === -1) {
    selectedUsers.value.push(row)
  } else if (!val && idx !== -1) {
    selectedUsers.value.splice(idx, 1)
  }
}

// ===== 勾选状态完全由当前列表页数据驱动：表头只反映当前页，跨部门/跨页保留由 selectedUsers 集合维护 =====
const selectedIds = computed(() => new Set(selectedUsers.value.map(u => u.userId)))
const pageSelectedCount = computed(() => userList.value.filter(u => selectedIds.value.has(u.userId)).length)
const isPageAllSelected = computed(() => userList.value.length > 0 && pageSelectedCount.value === userList.value.length)
const isPageIndeterminate = computed(() => pageSelectedCount.value > 0 && pageSelectedCount.value < userList.value.length)

/** 表头勾选框：全选/取消全选当前列表页 */
function togglePageAll(val) {
  userList.value.forEach(row => toggleRow(row, val))
}

// 查询表数据
function getList() {
  loading.value = true
  unallocatedUserList(queryParams).then(res => {
    userList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.deptId = undefined
  queryParams.userName = undefined
  queryParams.nickName = undefined
  handleQuery()
}

const emit = defineEmits(["ok"])
/** 选择授权用户操作 */
function handleSelectUser() {
  const roleId = queryParams.roleId
  const uIds = selectedUsers.value.map(u => u.userId).join(",")
  if (uIds == "") {
    proxy.$modal.msgError("请选择要分配的用户")
    return
  }
  authUserSelectAll({ roleId: roleId, userIds: uIds }).then(res => {
    proxy.$modal.msgSuccess(res.msg)
    visible.value = false
    emit("ok")
  })
}

defineExpose({
  show,
})
</script>

<style scoped>
/* ===== 弹框选择器统一样式（依据 design/picker-component-style-guide.md）===== */
.select-user-picker {
  display: flex;
  flex-direction: column;
}

.picker-body {
  display: flex;
  gap: 12px;
  min-height: 0;
}

/* 左侧部门树 */
.picker-left {
  width: 220px;
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
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}
.picker-left-header .el-icon {
  color: #409eff;
  font-size: 15px;
}
.picker-left-search {
  padding: 8px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.picker-left-tree {
  flex: 1;
  overflow: auto;
  padding: 4px 6px 8px;
  max-height: 428px;
}
.picker-left-tree::-webkit-scrollbar {
  width: 4px;
}
.picker-left-tree::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
}
.picker-left-tree :deep(.el-tree-node__content) {
  height: 30px;
  border-radius: 4px;
  margin-bottom: 1px;
}
.picker-left-tree :deep(.el-tree-node__content:hover) {
  background: #f0f7ff;
}
.picker-left-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #e6f0fd;
  color: #409eff;
  font-weight: 600;
}

/* 部门节点 + 渐变图标徽标（依据 picker-component-style-guide.md 图标设计规范）*/
.dept-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  overflow: hidden;
}
.dept-node .dept-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.node-icon-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 5px;
  flex-shrink: 0;
}
.node-icon-badge .el-icon {
  font-size: 12px;
  color: #fff;
}
.node-icon-badge--company {
  background: linear-gradient(135deg, #4facfe, #2f8eed);
}
.node-icon-badge--dept {
  background: linear-gradient(135deg, #f5b454, #e6a23c);
}
.node-icon-badge--leaf {
  background: linear-gradient(135deg, #67c23a, #529b2e);
}

.picker-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

/* 搜索栏 */
.select-user-picker-search {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

/* 表格 */
.select-user-picker-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.select-user-picker-table :deep(.el-table__row) {
  cursor: pointer;
}
.select-user-picker-table :deep(.el-table__row:hover > td) {
  background: #f0f7ff !important;
}
.select-user-picker-table :deep(.el-table__row.is-current > td) {
  background: #e6f0fd;
}

/* 底部：已选标签 + 分页 */
.select-user-picker-bottom {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.select-user-picker-pager {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
.select-user-picker-pager :deep(.pagination-container) {
  padding: 0;
  background: transparent;
}
</style>
