<template>
   <div class="app-container tree-sidebar-manage-wrap sys-role-page">
      <tree-panel title="角色分类" :tree-data="categoryOptions" search-placeholder="请输入分类名称" storage-key="role-category-sidebar-width" :defaultExpandAll="true" @node-click="handleNodeClick" @refresh="getCategoryTree" ref="categoryTreeRef" />
      <div class="tree-sidebar-content">
         <div class="content-inner">
            <!-- Filter Card -->
            <div class="surface filter-card" v-show="showSearch">
               <div class="filter-head">
                  <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
               </div>
               <div class="filter-bar">
                  <div class="field">
                     <label>角色名称</label>
                     <div class="control">
                        <el-input v-model="queryParams.roleName" placeholder="请输入" clearable @keyup.enter="handleQuery">
                           <template #prefix><el-icon><Search /></el-icon></template>
                        </el-input>
                     </div>
                  </div>
                  <div class="field">
                     <label>权限字符</label>
                     <div class="control">
                        <el-input v-model="queryParams.roleKey" placeholder="请输入" clearable @keyup.enter="handleQuery">
                           <template #prefix><el-icon><Key /></el-icon></template>
                        </el-input>
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
                  <div class="field">
                     <label>创建时间</label>
                     <div class="control">
                        <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
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
               <!-- Toolbar -->
               <div class="toolbar">
                  <div class="left">
                     <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:role:add']">新增</el-button>
                     <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['system:role:edit']">
                        <el-icon><Edit /></el-icon> 修改
                     </button>
                     <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:role:remove']">
                        <el-icon><Delete /></el-icon> 删除
                     </button>
                     <div class="toolbar-divider"></div>
                     <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['system:role:export']">
                        <el-icon><Download /></el-icon> 导出
                     </button>
                  </div>
                  <div class="right">
                     <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="sys_role_columns"></right-toolbar>
                  </div>
               </div>

               <!-- Table -->
               <div class="table-wrap">
                  <el-table ref="tableRef" v-loading="loading" :data="roleList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="角色编号" prop="roleId" key="roleId" :width="colWidth('roleId', 120)" v-if="columns.roleId.visible" />
                     <el-table-column label="角色名称" prop="roleName" key="roleName" :show-overflow-tooltip="true" :width="colWidth('roleName', 150)" v-if="columns.roleName.visible" />
                     <el-table-column label="角色分类" align="center" key="roleCategory" prop="roleCategory" :width="colWidth('roleCategory', 180)" v-if="columns.roleCategory.visible">
                        <template #default="scope">
                           <div class="category-tags">
                              <el-tag v-for="cat in parseCategoryList(scope.row.roleCategory)" :key="cat" :type="getCategoryTagType(cat)" size="small" style="margin: 2px;">{{ getCategoryLabel(cat) }}</el-tag>
                           </div>
                        </template>
                     </el-table-column>
                     <el-table-column label="权限字符" prop="roleKey" key="roleKey" :show-overflow-tooltip="true" :width="colWidth('roleKey', 150)" v-if="columns.roleKey.visible" />
                     <el-table-column label="显示顺序" prop="roleSort" key="roleSort" :width="colWidth('roleSort', 100)" v-if="columns.roleSort.visible" />
                     <el-table-column label="状态" align="center" key="status" prop="status" :width="colWidth('status', 100)" v-if="columns.status.visible">
                        <template #default="scope">
                           <el-switch
                              v-model="scope.row.status"
                              active-value="0"
                              inactive-value="1"
                              @change="handleStatusChange(scope.row)"
                           ></el-switch>
                        </template>
                     </el-table-column>
                     <el-table-column label="创建时间" align="center" prop="createTime" key="createTime" v-if="columns.createTime.visible" :width="colWidth('createTime', 160)">
                        <template #default="scope">
                           <span>{{ parseTime(scope.row.createTime) }}</span>
                        </template>
                     </el-table-column>
                     <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
                        <template #default="scope">
                <div class="action-btn-row">
                           <el-tooltip content="修改" placement="top" v-if="scope.row.roleId !== 1">
                              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:role:edit']"></el-button>
                           </el-tooltip>
                           <el-tooltip content="删除" placement="top" v-if="scope.row.roleId !== 1">
                              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:role:remove']"></el-button>
                           </el-tooltip>
                           <el-tooltip content="数据权限" placement="top" v-if="scope.row.roleId !== 1">
                              <el-button link type="primary" icon="CircleCheck" @click="handleDataScope(scope.row)" v-hasPermi="['system:role:edit']"></el-button>
                           </el-tooltip>
                           <el-tooltip content="分配用户" placement="top" v-if="scope.row.roleId !== 1">
                              <el-button link type="primary" icon="User" @click="handleAuthUser(scope.row)" v-hasPermi="['system:role:edit']"></el-button>
                           </el-tooltip>
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
      </div>

      <!-- 添加或修改角色配置对话框 -->
      <el-dialog v-model="open" width="1080px" append-to-body draggable :close-on-click-modal="false" class="rd-dialog role-form-dialog">
         <template #header>
            <div class="rd-detail-header">
               <div class="rd-detail-header-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                     <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                     <circle cx="9" cy="7" r="4" />
                     <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
                     <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                  </svg>
               </div>
               <span class="rd-detail-header-title">{{ title }}</span>
               <div class="rd-detail-header-sub" v-if="form.roleId">
                  <div class="rd-detail-header-divider"></div>
                  <span class="rd-detail-header-no">编号：{{ form.roleId }}</span>
               </div>
            </div>
         </template>
         <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px" class="role-form-grid">
            <!-- 左栏：基本信息 -->
            <div class="form-left-col">
               <section class="rd-card">
                  <div class="rd-card-header">
                     <div class="rd-card-title">
                        <span class="rd-card-icon">
                           <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                              <polyline points="14 2 14 8 20 8" />
                              <line x1="9" y1="15" x2="15" y2="15" />
                           </svg>
                        </span>
                        基本信息
                     </div>
                  </div>
                  <div class="rd-card-body">
                     <el-form-item label="角色名称" prop="roleName">
                        <el-input v-model="form.roleName" placeholder="请输入角色名称" @input="handleRoleNameInput" />
                     </el-form-item>
                     <el-form-item label="角色分类" prop="roleCategoryList">
                        <el-select v-model="form.roleCategoryList" placeholder="请选择角色分类" multiple collapse-tags collapse-tags-tooltip style="width: 100%">
                           <el-option v-for="dict in sys_role_category" :key="dict.value" :label="dict.label" :value="dict.value" />
                        </el-select>
                     </el-form-item>
                     <el-form-item prop="roleKey">
                        <template #label>
                           <span>
                              <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)&#10;输入角色名称后自动生成，也可手动修改" placement="top">
                                 <el-icon><question-filled /></el-icon>
                              </el-tooltip>
                              权限字符
                           </span>
                        </template>
                        <el-input v-model="form.roleKey" placeholder="请输入权限字符">
                           <template #append>
                              <el-button @click="generateRoleKey" :disabled="!form.roleName">生成</el-button>
                           </template>
                        </el-input>
                     </el-form-item>
                     <el-form-item label="角色顺序" prop="roleSort">
                        <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
                     </el-form-item>
                     <el-form-item label="状态">
                        <el-radio-group v-model="form.status">
                           <el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
                        </el-radio-group>
                     </el-form-item>
                     <el-form-item label="备注">
                        <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" :rows="3"></el-input>
                     </el-form-item>
                  </div>
               </section>
            </div>
            <!-- 右栏：菜单权限 -->
            <div class="form-right-col">
               <section class="rd-card">
                  <div class="rd-card-header">
                     <div class="rd-card-title">
                        <span class="rd-card-icon">
                           <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                              stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                              <polyline points="9 11 12 14 22 4" />
                              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                           </svg>
                        </span>
                        菜单权限
                     </div>
                  </div>
                  <div class="rd-card-body">
                     <div class="menu-perm-toolbar">
                        <div class="menu-perm-ops">
                           <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠</el-checkbox>
                           <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选</el-checkbox>
                           <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">父子联动</el-checkbox>
                        </div>
                        <el-input v-model="menuFilterText" placeholder="搜索菜单名称" clearable size="small" style="width: 200px" :prefix-icon="Search" @input="handleMenuFilter" />
                     </div>
                     <el-tree
                        class="tree-border menu-tree"
                        :data="menuOptions"
                        show-checkbox
                        ref="menuRef"
                        node-key="id"
                        :check-strictly="!form.menuCheckStrictly"
                        empty-text="加载中，请稍候"
                        :props="{ label: 'label', children: 'children' }"
                        :filter-node-method="filterMenuNode"
                        default-expand-all
                     ></el-tree>
                  </div>
               </section>
            </div>
         </el-form>
         <template #footer>
            <el-button type="primary" @click="submitForm">确 定</el-button>
            <el-button @click="cancel">取 消</el-button>
         </template>
      </el-dialog>

      <!-- 分配角色数据权限对话框 -->
      <el-dialog v-model="openDataScope" width="860px" append-to-body draggable class="rd-dialog">
         <template #header>
            <div class="rd-detail-header">
               <div class="rd-detail-header-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                     <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                     <circle cx="12" cy="7" r="4" />
                  </svg>
               </div>
               <span class="rd-detail-header-title">分配数据权限</span>
               <div class="rd-detail-header-sub" v-if="form.roleName">
                  <div class="rd-detail-header-divider"></div>
                  <span class="rd-detail-header-no">{{ form.roleName }}</span>
               </div>
            </div>
         </template>
         <el-form :model="form" label-width="80px">
            <section class="rd-card">
               <div class="rd-card-header">
                  <div class="rd-card-title">
                     <span class="rd-card-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                           <circle cx="12" cy="12" r="3" />
                           <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0 1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83 1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0 1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83 1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
                        </svg>
                     </span>
                     权限配置
                  </div>
               </div>
               <div class="rd-card-body">
                  <el-form-item label="角色名称">
                     <el-input v-model="form.roleName" :disabled="true" />
                  </el-form-item>
                  <el-form-item label="权限字符">
                     <el-input v-model="form.roleKey" :disabled="true" />
                  </el-form-item>
                  <el-form-item label="权限范围">
                     <el-select v-model="form.dataScope" @change="dataScopeSelectChange">
                        <el-option v-for="item in dataScopeOptions" :key="item.value" :label="item.label" :value="item.value" />
                     </el-select>
                  </el-form-item>
                  <el-form-item label="数据权限" v-show="form.dataScope == 2">
                     <div class="menu-perm-toolbar" style="margin-bottom: 8px;">
                        <div class="menu-perm-ops">
                           <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠</el-checkbox>
                           <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选</el-checkbox>
                           <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">父子联动</el-checkbox>
                        </div>
                     </div>
                     <el-tree
                        class="tree-border"
                        :data="deptOptions"
                        show-checkbox
                        default-expand-all
                        ref="deptRef"
                        node-key="id"
                        :check-strictly="!form.deptCheckStrictly"
                        empty-text="加载中，请稍候"
                        :props="{ label: 'label', children: 'children' }"
                     ></el-tree>
                  </el-form-item>
               </div>
            </section>
         </el-form>
         <template #footer>
            <el-button type="primary" @click="submitDataScope">确 定</el-button>
            <el-button @click="cancelDataScope">取 消</el-button>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Role">
import TreePanel from "@/components/TreePanel"
import { addRole, changeRoleStatus, dataScope, delRole, getRole, listRole, updateRole, deptTreeSelect } from "@/api/system/role"
import { roleMenuTreeselect, treeselect as menuTreeselect } from "@/api/system/menu"
import { Search, Key, Filter, Edit, Delete, Download, RefreshLeft } from "@element-plus/icons-vue"
import { pinyin } from 'pinyin-pro'
import { useColumnResize } from '@/composables/useColumnResize'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { sys_normal_disable, sys_role_category } = useDict("sys_normal_disable", "sys_role_category")
// 列宽拖拽持久化
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('sys_role_index')

const roleList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])
const menuOptions = ref([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const deptExpand = ref(true)
const deptNodeAll = ref(false)
const deptOptions = ref([])
const openDataScope = ref(false)
const menuRef = ref(null)
const deptRef = ref(null)
const categoryOptions = ref([])
const menuFilterText = ref("")
const roleKeyAutoGen = ref(true)

// 列显隐信息
const columns = ref({
  roleId: { label: '角色编号', visible: true },
  roleName: { label: '角色名称', visible: true },
  roleCategory: { label: '角色分类', visible: true },
  roleKey: { label: '权限字符', visible: true },
  roleSort: { label: '显示顺序', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
})

/** 数据范围选项*/
const dataScopeOptions = ref([
  { value: "1", label: "全部数据权限" },
  { value: "2", label: "自定数据权限" },
  { value: "3", label: "本部门数据权限" },
  { value: "4", label: "本部门及以下数据权限" },
  { value: "5", label: "仅本人数据权限" }
])

/** 分类标签类型映射 */
const categoryTagTypeMap = {
  system: 'primary',
  dms: 'success',
  pms: 'warning',
  mk: 'danger',
  wms: 'info',
  safety: 'warning',
  qms: 'success'
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined,
    roleKey: undefined,
    status: undefined,
    roleCategory: undefined
  },
  rules: {
    roleName: [{ required: true, message: "角色名称不能为空", trigger: "blur" }],
    roleKey: [{ required: true, message: "权限字符不能为空", trigger: "blur" }],
    roleSort: [{ required: true, message: "角色顺序不能为空", trigger: "blur" }],
    roleCategoryList: [{ required: true, message: "角色分类不能为空", trigger: "change", type: "array" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.roleName) count++
  if (queryParams.value.roleKey) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

/** 获取分类标签类型 */
function getCategoryTagType(category) {
  const dict = sys_role_category.value?.find(d => d.value === category)
  return dict?.elTagType || categoryTagTypeMap[category] || 'info'
}

/** 获取分类标签文本 */
function getCategoryLabel(category) {
const dict = sys_role_category.value?.find(d => d.value === category)
return dict ? dict.label : category
}

/** 解析角色分类字符串为数组（支持多选） */
function parseCategoryList(roleCategory) {
if (!roleCategory) return ['system']
return roleCategory.split(',').map(c => c.trim()).filter(c => c)
}

/** 构建角色分类树（从字典数据生成） */
function getCategoryTree() {
  categoryOptions.value = (sys_role_category.value || []).map(d => ({
    id: d.value,
    label: d.label
  }))
}

/** 查询角色列表 */
function getList() {
  loading.value = true
  listRole(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    roleList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

/** 节点单击事件 */
function handleNodeClick(data) {
  queryParams.value.roleCategory = data.id
  handleQuery()
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.roleName = undefined
  queryParams.value.roleKey = undefined
  queryParams.value.status = undefined
  queryParams.value.roleCategory = undefined
  proxy.$refs.categoryTreeRef.setCurrentKey(null)
  handleQuery()
}

/** 删除按钮操作 */
function handleDelete(row) {
  const roleIds = row.roleId || ids.value
  proxy.$modal.confirm('是否确认删除角色编号为"' + roleIds + '"的数据项?').then(function () {
    return delRole(roleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("system/role/export", {
    ...proxy.addDateRange(queryParams.value, dateRange.value),
  }, `role_${new Date().getTime()}.xlsx`)
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.roleId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 角色状态修改 */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '""' + row.roleName + '"角色吗?').then(function () {
    return changeRoleStatus(row.roleId, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    row.status = row.status === "0" ? "1" : "0"
  })
}

/** 更多操作 */
function handleCommand(command, row) {
  switch (command) {
    case "handleDataScope":
      handleDataScope(row)
      break
    case "handleAuthUser":
      handleAuthUser(row)
      break
    default:
      break
  }
}

/** 分配用户 */
function handleAuthUser(row) {
  router.push("/system/role-auth/user/" + row.roleId)
}

/** 查询菜单树结构 */
function getMenuTreeselect() {
  menuTreeselect().then(response => {
    menuOptions.value = response.data
  })
}

/** 根据角色名称自动生成权限字符 */
function generateRoleKey() {
  const name = form.value.roleName
  if (!name) return
  // 使用 pinyin-pro 将中文转为拼音，无空格小写，非中文字符保留
  const py = pinyin(name, { toneType: 'none', type: 'array', nonZh: 'consecutive' })
  let key = py.join('').replace(/\s+/g, '').toLowerCase()
  // 移除可能的多余分隔符，只保留字母数字和下划线
  key = key.replace(/[^a-z0-9_]/g, '')
  if (key) {
    form.value.roleKey = key
  }
}

/** 角色名称输入时自动生成权限字符（仅在新增模式且自动生成开关打开时） */
function handleRoleNameInput() {
  if (roleKeyAutoGen.value && form.value.roleId === undefined) {
    generateRoleKey()
  }
}

/** 菜单树过滤方法 */
function filterMenuNode(value, data) {
  if (!value) return true
  return data.label && data.label.indexOf(value) !== -1
}

/** 菜单搜索输入 */
function handleMenuFilter() {
  menuRef.value && menuRef.value.filter(menuFilterText.value)
  // 搜索时自动展开所有节点
  if (menuFilterText.value) {
    menuExpand.value = true
    nextTick(() => {
      handleCheckedTreeExpand(true, 'menu')
    })
  }
}

/** 所有部门节点数据 */
function getDeptAllCheckedKeys() {
  // 目前被选中的部门节点
  let checkedKeys = deptRef.value.getCheckedKeys()
  // 半选中的部门节点
  let halfCheckedKeys = deptRef.value.getHalfCheckedKeys()
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
  return checkedKeys
}

/** 重置新增的表单以及其他数据  */
function reset() {
  if (menuRef.value != undefined) {
    menuRef.value.setCheckedKeys([])
  }
  menuExpand.value = false
  menuNodeAll.value = false
  menuFilterText.value = ""
  deptExpand.value = true
  deptNodeAll.value = false
form.value = {
roleId: undefined,
roleName: undefined,
roleKey: undefined,
roleSort: 0,
roleCategory: "system",
roleCategoryList: ["system"],
status: "0",
menuIds: [],
deptIds: [],
menuCheckStrictly: true,
deptCheckStrictly: true,
remark: undefined
}
  proxy.resetForm("roleRef")
}

/** 添加角色 */
function handleAdd() {
reset()
// 如果当前选中了分类节点，自动填充分类
if (queryParams.value.roleCategory) {
form.value.roleCategory = queryParams.value.roleCategory
form.value.roleCategoryList = [queryParams.value.roleCategory]
}
getMenuTreeselect()
open.value = true
title.value = "添加角色"
}

/** 修改角色 */
function handleUpdate(row) {
reset()
const roleId = row.roleId || ids.value
const roleMenu = getRoleMenuTreeselect(roleId)
getRole(roleId).then(response => {
form.value = response.data
form.value.roleSort = Number(form.value.roleSort)
// 将逗号分隔的分类字符串转换为数组
form.value.roleCategoryList = parseCategoryList(form.value.roleCategory)
open.value = true
    nextTick(() => {
      roleMenu.then((res) => {
        let checkedKeys = res.checkedKeys
        checkedKeys.forEach((v) => {
          nextTick(() => {
            menuRef.value.setChecked(v, true, false)
          })
        })
      })
    })
  })
  title.value = "修改角色"
}

/** 根据角色ID查询菜单树结构 */
function getRoleMenuTreeselect(roleId) {
  return roleMenuTreeselect(roleId).then(response => {
    menuOptions.value = response.menus
    return response
  })
}

/** 根据角色ID查询部门树结构 */
function getDeptTree(roleId) {
  return deptTreeSelect(roleId).then(response => {
    deptOptions.value = response.depts
    return response
  })
}

/** 树权限（展开/折叠）*/
function handleCheckedTreeExpand(value, type) {
  if (type == "menu") {
    let treeList = menuOptions.value
    for (let i = 0; i < treeList.length; i++) {
      menuRef.value.store.nodesMap[treeList[i].id].expanded = value
    }
  } else if (type == "dept") {
    let treeList = deptOptions.value
    for (let i = 0; i < treeList.length; i++) {
      deptRef.value.store.nodesMap[treeList[i].id].expanded = value
    }
  }
}

/** 树权限（全选/全不选） */
function handleCheckedTreeNodeAll(value, type) {
  if (type == "menu") {
    menuRef.value.setCheckedNodes(value ? menuOptions.value : [])
  } else if (type == "dept") {
    deptRef.value.setCheckedNodes(value ? deptOptions.value : [])
  }
}

/** 树权限（父子联动） */
function handleCheckedTreeConnect(value, type) {
  if (type == "menu") {
    form.value.menuCheckStrictly = value ? true : false
  } else if (type == "dept") {
    form.value.deptCheckStrictly = value ? true : false
  }
}

/** 所有菜单节点数据 */
function getMenuAllCheckedKeys() {
  // 目前被选中的菜单节点
  let checkedKeys = menuRef.value.getCheckedKeys()
  // 半选中的菜单节点
  let halfCheckedKeys = menuRef.value.getHalfCheckedKeys()
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
  return checkedKeys
}

/** 提交按钮 */
function submitForm() {
proxy.$refs["roleRef"].validate(valid => {
if (valid) {
// 将角色分类数组转换为逗号分隔的字符串
if (form.value.roleCategoryList && form.value.roleCategoryList.length > 0) {
form.value.roleCategory = form.value.roleCategoryList.join(',')
} else {
form.value.roleCategory = 'system'
}
if (form.value.roleId != undefined) {
form.value.menuIds = getMenuAllCheckedKeys()
updateRole(form.value).then(() => {
proxy.$modal.msgSuccess("修改成功")
open.value = false
getList()
})
} else {
form.value.menuIds = getMenuAllCheckedKeys()
addRole(form.value).then(() => {
proxy.$modal.msgSuccess("新增成功")
open.value = false
getList()
})
}
}
})
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 选择角色权限范围触发 */
function dataScopeSelectChange(value) {
  if (value !== "2") {
    deptRef.value.setCheckedKeys([])
  }
}

/** 分配数据权限操作 */
function handleDataScope(row) {
  reset()
  const deptTreeSelect = getDeptTree(row.roleId)
  getRole(row.roleId).then(response => {
    form.value = response.data
    openDataScope.value = true
    nextTick(() => {
      deptTreeSelect.then(res => {
        nextTick(() => {
          if (deptRef.value) {
            deptRef.value.setCheckedKeys(res.checkedKeys)
          }
        })
      })
    })
  })
  title.value = "分配数据权限"
}

/** 提交按钮（数据权限） */
function submitDataScope() {
  if (form.value.roleId != undefined) {
    form.value.deptIds = getDeptAllCheckedKeys()
    dataScope(form.value).then(() => {
      proxy.$modal.msgSuccess("修改成功")
      openDataScope.value = false
      getList()
    })
  }
}

/** 取消按钮（数据权限）*/
function cancelDataScope() {
  openDataScope.value = false
  reset()
}

onMounted(() => {
  getList()
})

// 字典数据加载完成后构建分类树
watch(() => sys_role_category.value, (val) => {
  if (val && val.length) {
    getCategoryTree()
  }
}, { immediate: true })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.sys-role-page {
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
.sys-role-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.sys-role-page .filter-card { padding:14px 20px 16px; }
.sys-role-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.sys-role-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.sys-role-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.sys-role-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.sys-role-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.sys-role-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.sys-role-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.sys-role-page .field { display:flex; flex-direction:column; gap:6px; }
.sys-role-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.sys-role-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.sys-role-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.sys-role-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.sys-role-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.sys-role-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.sys-role-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.sys-role-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.sys-role-page .field .control :deep(.el-select) { width:100%; }
.sys-role-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.sys-role-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.sys-role-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.sys-role-page .field .control :deep(.el-date-editor) { width:100%; }
.sys-role-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.sys-role-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.sys-role-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Toolbar ===== */
.sys-role-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.sys-role-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.sys-role-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.sys-role-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.sys-role-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.sys-role-page .btn-soft .el-icon { font-size:14px; }
.sys-role-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.sys-role-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.sys-role-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.sys-role-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.sys-role-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.sys-role-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.sys-role-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.sys-role-page .table-wrap { overflow-x:auto; }
.sys-role-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.sys-role-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.sys-role-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.sys-role-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.sys-role-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.sys-role-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.sys-role-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.sys-role-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.sys-role-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.sys-role-page .app-table :deep(.el-table__border-left-patch) { display:none; }

/* ===== Pagination ===== */
.sys-role-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.sys-role-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.sys-role-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.sys-role-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.sys-role-page .pagination-container :deep(.el-pagination .btn-prev), .sys-role-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.sys-role-page .pagination-container :deep(.el-pagination .btn-prev:hover), .sys-role-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.sys-role-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Role Form Dialog - Dual Column Layout (extends rd-dialog) ===== */
.role-form-dialog .role-form-grid { display: grid; grid-template-columns: 384px 1fr; gap: 0 28px; align-items: start; }
.role-form-dialog .form-left-col { min-width: 0; }
.role-form-dialog .form-right-col { min-width: 0; }
.role-form-dialog .menu-perm-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.role-form-dialog .menu-perm-ops { display: flex; gap: 12px; align-items: center; }
.role-form-dialog .menu-tree { max-height: 504px; overflow-y: auto; border: 1px solid #e5e7eb; border-radius: 8px; padding: 6px; background: #fff; }

/* el-input with append button */
.role-form-dialog .el-input-group__append { padding: 0 12px; }
.role-form-dialog .el-input-group__append .el-button { font-size: 13px; }

/* rd-card header cursor default for role page (no collapse) */
.sys-role-page .rd-card .rd-card-header { cursor: default; }

/* data scope dialog tree */
.rd-dialog .menu-perm-toolbar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-dialog .menu-perm-ops { display: flex; gap: 12px; align-items: center; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .sys-role-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } .role-form-dialog .role-form-grid { grid-template-columns: 1fr; } .role-form-dialog .form-right-col { border-left: none; border-top: 1px solid var(--ink-200); padding-left: 0; padding-top: 16px; } }
@media (max-width:720px) { .sys-role-page .filter-card .filter-bar { grid-template-columns:1fr; } .sys-role-page .toolbar { flex-wrap:wrap; gap:10px; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
