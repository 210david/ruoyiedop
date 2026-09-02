<template>
   <div class="app-container tree-sidebar-manage-wrap sys-dept-page">
      <!-- 左侧组织机构树 -->
      <tree-panel
         title="组织机构"
         :tree-data="deptTreeData"
         :tree-props="{ children: 'children', label: 'label' }"
         node-key="id"
         search-placeholder="请输入部门名称"
         storage-key="dept-manage-sidebar-width"
         :default-expand-all="true"
         @node-click="handleNodeClick"
         @refresh="getDeptTree"
         ref="deptTreeRef"
      />

      <!-- 右侧内容区 -->
      <div class="tree-sidebar-content">
         <div class="content-inner">
            <!-- Filter Card -->
            <div class="surface filter-card" v-show="showSearch">
               <div class="filter-head">
                  <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
               </div>
               <div class="filter-bar">
                  <div class="field">
                     <label>部门名称</label>
                     <div class="control">
                        <el-input
                           v-model="queryParams.deptName"
                           placeholder="请输入"
                           clearable
                           @keyup.enter="handleQuery"
                        >
                           <template #prefix><el-icon><Search /></el-icon></template>
                        </el-input>
                     </div>
                  </div>
                  <div class="field">
                     <label>状态</label>
                     <div class="control is-select">
                        <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
                           <el-option
                              v-for="dict in sys_normal_disable"
                              :key="dict.value"
                              :label="dict.label"
                              :value="dict.value"
                           />
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

            <!-- Table Section -->
            <div class="surface">
               <div class="toolbar">
                  <div class="left">
                     <el-button
                        type="primary"
                        plain
                        icon="Plus"
                        @click="handleAdd"
                        v-hasPermi="['system:dept:add']"
                     >新增</el-button>
                     <el-button
                        type="warning"
                        plain
                        icon="Check"
                        @click="handleSaveSort"
                        v-hasPermi="['system:dept:edit']"
                     >保存排序</el-button>
                     <el-button
                        type="info"
                        plain
                        icon="Sort"
                        @click="toggleExpandAll"
                     >展开/折叠</el-button>
                  </div>
                  <div class="right">
                     <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_dept_columns"></right-toolbar>
                  </div>
               </div>

               <div class="table-wrap">
                  <el-table
                     v-if="refreshTable"
                     ref="tableRef"
                     v-loading="loading"
                     :data="deptList"
                     row-key="deptId"
                     :default-expand-all="isExpandAll"
                     :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                     @header-dragend="onHeaderDragEnd"
                     border
                     class="app-table"
                  >
                     <el-table-column type="index" label="序号" width="85" align="center" />
                     <el-table-column prop="deptName" label="部门名称" :width="colWidth('deptName', 260)" v-if="columns.deptName.visible"></el-table-column>
                     <el-table-column prop="orderNum" label="排序" :width="colWidth('orderNum', 200)" v-if="columns.orderNum.visible">
                        <template #default="scope">
                           <el-input-number v-model="scope.row.orderNum" controls-position="right" :min="0" style="width: 88px" />
                        </template>
                     </el-table-column>
                     <el-table-column prop="status" label="状态" :width="colWidth('status', 100)" v-if="columns.status.visible">
                        <template #default="scope">
                           <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
                        </template>
                     </el-table-column>
                     <el-table-column label="创建时间" align="center" prop="createTime" :width="colWidth('createTime', 200)" v-if="columns.createTime.visible">
                        <template #default="scope">
                           <span>{{ parseTime(scope.row.createTime) }}</span>
                        </template>
                     </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:dept:edit']">修改</el-button>
                <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['system:dept:add']">新增</el-button>
                <el-button v-if="scope.row.parentId != 0" link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:dept:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
                  </el-table>
               </div>
            </div>
         </div>
      </div>

      <!-- 添加或修改部门对话框 -->
      <el-dialog v-model="open" width="680px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog">
         <template #header>
            <div class="rd-detail-header">
               <div class="rd-detail-header-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                     <path d="M3 21h18" /><path d="M5 21V7l8-4v18" /><path d="M19 21V11l-6-4" />
                  </svg>
               </div>
               <span class="rd-detail-header-title">{{ title }}</span>
               <div class="rd-detail-header-sub" v-if="form.deptId">
                  <div class="rd-detail-header-divider"></div>
                  <span class="rd-detail-header-no">编号：{{ form.deptId }}</span>
               </div>
            </div>
         </template>
         <el-form ref="deptRef" :model="form" :rules="rules" label-width="80px">
            <section class="rd-card">
               <div class="rd-card-header">
                  <div class="rd-card-title">
                     <span class="rd-card-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                           <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                           <polyline points="14 2 14 8 20 8" /><line x1="9" y1="15" x2="15" y2="15" />
                        </svg>
                     </span>
                     基本信息
                  </div>
               </div>
               <div class="rd-card-body">
                  <el-row :gutter="16">
                     <el-col :span="24" v-if="form.parentId !== 0">
                        <el-form-item label="上级部门" prop="parentId">
                           <el-tree-select v-model="form.parentId" :data="deptOptions"
                              :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
                              value-key="deptId" placeholder="选择上级部门" check-strictly style="width: 100%" />
                        </el-form-item>
                     </el-col>
                     <el-col :span="12">
                        <el-form-item label="部门名称" prop="deptName">
                           <el-input v-model="form.deptName" placeholder="请输入部门名称" />
                        </el-form-item>
                     </el-col>
                     <el-col :span="12">
                        <el-form-item label="显示排序" prop="orderNum">
                           <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                        </el-form-item>
                     </el-col>
                  </el-row>
               </div>
            </section>
            <section class="rd-card">
               <div class="rd-card-header">
                  <div class="rd-card-title">
                     <span class="rd-card-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                           <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
                        </svg>
                     </span>
                     联系信息
                  </div>
               </div>
               <div class="rd-card-body">
                  <el-row :gutter="16">
                     <el-col :span="12">
                        <el-form-item label="负责人" prop="leader">
                           <el-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" />
                        </el-form-item>
                     </el-col>
                     <el-col :span="12">
                        <el-form-item label="联系电话" prop="phone">
                           <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
                        </el-form-item>
                     </el-col>
                     <el-col :span="12">
                        <el-form-item label="邮箱" prop="email">
                           <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
                        </el-form-item>
                     </el-col>
                     <el-col :span="12">
                        <el-form-item label="部门状态">
                           <el-radio-group v-model="form.status">
                              <el-radio v-for="dict in sys_normal_disable" :key="dict.value"
                                 :value="dict.value">{{ dict.label }}</el-radio>
                           </el-radio-group>
                        </el-form-item>
                     </el-col>
                  </el-row>
               </div>
            </section>
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

<script setup name="Dept">
import TreePanel from "@/components/TreePanel"
import { listDept, getDept, delDept, addDept, updateDept, updateDeptSort, listDeptExcludeChild } from "@/api/system/dept"
import { deptTreeSelect } from "@/api/system/user"
import { Search, Filter } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = useDict("sys_normal_disable")
// 列宽拖拽持久化
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_dept_index')

const deptList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref("")
const deptOptions = ref([])
const isExpandAll = ref(true)
const refreshTable = ref(true)
const originalOrders = ref({})
const deptTreeData = ref([])
const currentDeptId = ref(undefined)

// 列显隐信息
const columns = ref({
  deptName: { label: '部门名称', visible: true },
  orderNum: { label: '排序', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
})

const data = reactive({
  form: {},
  queryParams: {
    deptName: undefined,
    status: undefined
  },
  rules: {
    parentId: [{ required: true, message: "上级部门不能为空", trigger: "blur" }],
    deptName: [{ required: true, message: "部门名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }],
    email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
    phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.deptName) count++
  if (queryParams.value.status) count++
  return count
})

/** 查询部门列表 */
function getList() {
  loading.value = true
  listDept(queryParams.value).then(response => {
    let allTree = proxy.handleTree(response.data, "deptId")
    if (currentDeptId.value) {
      // 左侧树选中了某个节点，只显示该节点及其子节点
      deptList.value = filterTreeById(allTree, currentDeptId.value)
    } else {
      deptList.value = allTree
    }
    recordOriginalOrders(deptList.value)
    loading.value = false
    applySavedWidths()
  })
}

/** 查询左侧组织机构树 */
function getDeptTree() {
  deptTreeSelect().then(response => {
    deptTreeData.value = response.data
  })
}

/** 从树中查找指定ID的节点及其子树 */
function filterTreeById(tree, targetId) {
  for (let node of tree) {
    if (node.deptId === targetId) {
      return [node]
    }
    if (node.children && node.children.length) {
      let result = filterTreeById(node.children, targetId)
      if (result) return result
    }
  }
  return null
}

/** 左侧树节点点击事件 */
function handleNodeClick(data) {
  currentDeptId.value = data.id
  getList()
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    deptId: undefined,
    parentId: undefined,
    deptName: undefined,
    orderNum: 0,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: "0"
  }
  proxy.resetForm("deptRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  currentDeptId.value = undefined
  if (proxy.$refs.deptTreeRef && proxy.$refs.deptTreeRef.treeRef) {
    proxy.$refs.deptTreeRef.treeRef.setCurrentKey(null)
  }
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset()
  listDept().then(response => {
    deptOptions.value = proxy.handleTree(response.data, "deptId")
  })
  if (row != undefined) {
    form.value.parentId = row.deptId
  } else if (currentDeptId.value) {
    form.value.parentId = currentDeptId.value
  }
  open.value = true
  title.value = "添加部门"
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  listDeptExcludeChild(row.deptId).then(response => {
    deptOptions.value = proxy.handleTree(response.data, "deptId")
  })
  getDept(row.deptId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改部门"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["deptRef"].validate(valid => {
    if (valid) {
      if (form.value.deptId != undefined) {
        updateDept(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
          getDeptTree()
        })
      } else {
        addDept(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
          getDeptTree()
        })
      }
    }
  })
}

/** 递归记录原始排序 */
function recordOriginalOrders(list) {
  list.forEach(item => {
    originalOrders.value[item.deptId] = item.orderNum
    if (item.children && item.children.length) {
      recordOriginalOrders(item.children)
    }
  })
}

/** 保存排序 */
function handleSaveSort() {
  const changedDeptIds = []
  const changedOrderNums = []
  const collectChanged = (list) => {
    list.forEach(item => {
      if (String(originalOrders.value[item.deptId]) !== String(item.orderNum)) {
        changedDeptIds.push(item.deptId)
        changedOrderNums.push(item.orderNum)
      }
      if (item.children && item.children.length) {
        collectChanged(item.children)
      }
    })
  }
  collectChanged(deptList.value)
  if (changedDeptIds.length === 0) {
   proxy.$modal.msgWarning("未检测到排序修改")
    return
  }
  updateDeptSort({ deptIds: changedDeptIds.join(","), orderNums: changedOrderNums.join(",") }).then(() => {
   proxy.$modal.msgSuccess("排序保存成功")
    recordOriginalOrders(deptList.value)
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.deptName + '"的数据项?').then(function() {
    return delDept(row.deptId)
  }).then(() => {
    getList()
    getDeptTree()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

onMounted(() => {
  getDeptTree()
  getList()
})
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-dept-page {
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.sys-dept-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-dept-page .filter-card { padding:14px 20px 16px; }
.sys-dept-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-dept-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-dept-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-dept-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-dept-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-dept-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-dept-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-dept-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-dept-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-dept-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-dept-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-dept-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-dept-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-dept-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-dept-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-dept-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-dept-page .field .control :deep(.el-select) { width:100%; }
.sys-dept-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-dept-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-dept-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.sys-dept-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-dept-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-dept-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-dept-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Table ===== */
.sys-dept-page .table-wrap { overflow-x:auto; }
.sys-dept-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-dept-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-dept-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-dept-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-dept-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-dept-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-dept-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-dept-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-dept-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-dept-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-dept-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .sys-dept-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-dept-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
