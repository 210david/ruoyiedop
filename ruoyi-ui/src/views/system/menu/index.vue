<template>
   <div class="app-container menu-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>菜单名称</label>
          <div class="control">
            <el-input v-model="queryParams.menuName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable>
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
          <el-icon><Filter /></el-icon> 支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['system:menu:add']"
          >新增</el-button>
          <el-button
            type="warning"
            plain
            icon="Check"
            @click="handleSaveSort"
            v-hasPermi="['system:menu:edit']"
          >保存排序</el-button>
          <el-button
            type="info"
            plain
            icon="Sort"
            @click="toggleExpandAll"
          >展开/折叠</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
         <el-table
            v-if="refreshTable"
            v-loading="loading"
            :data="menuList"
            row-key="menuId"
            :default-expand-all="isExpandAll"
            :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
            class="app-table"
         >
         <el-table-column prop="menuName" label="菜单名称" :show-overflow-tooltip="true" width="220">
            <template #default="scope">
               <svg-icon :icon-class="scope.row.icon" />
               <span class="ml5">{{ scope.row.menuName }}</span>
            </template>
         </el-table-column>
         <el-table-column prop="menuName" label="类型" :show-overflow-tooltip="true" width="100">
            <template #default="scope">
               <el-tag v-if="scope.row.menuType === 'M' && scope.row.isFrame === '0'" type="danger" size="small">外链</el-tag>
               <el-tag v-else-if="scope.row.menuType === 'M'" type="primary" size="small">目录</el-tag>
               <el-tag v-else-if="scope.row.menuType === 'C' && scope.row.isFrame === '0'" type="danger" size="small">外链</el-tag>
               <el-tag v-else-if="scope.row.menuType === 'C'" type="success" size="small">菜单</el-tag>
               <el-tag v-else-if="scope.row.menuType === 'F'" type="warning" size="small">按钮</el-tag>
            </template>
         </el-table-column>
         <el-table-column prop="orderNum" label="排序" width="200">
            <template #default="scope">
               <el-input-number v-model="scope.row.orderNum" controls-position="right" :min="0" style="width: 88px" />
            </template>
         </el-table-column>
         <el-table-column prop="perms" label="权限标识" :show-overflow-tooltip="true" />
         <el-table-column prop="component" label="组件路径" :show-overflow-tooltip="true" />
         <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
               <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="210" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:menu:edit']">修改</el-button>
               <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['system:menu:add']">新增</el-button>
               <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:menu:remove']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>
      </div>
    </div>

      <!-- 添加或修改菜单对话框 -->
      <el-dialog :title="title" v-model="open" width="680px" append-to-body>
         <el-form ref="menuRef" :model="form" :rules="rules" label-width="100px">
            <el-row>
               <el-col :span="24">
                  <el-form-item label="上级菜单">
                     <el-tree-select
                        v-model="form.parentId"
                        :data="menuOptions"
                        :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
                        value-key="menuId"
                        placeholder="选择上级菜单"
                        check-strictly
                     />
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="菜单类型" prop="menuType">
                     <el-radio-group v-model="form.menuType">
                        <el-radio value="M">目录</el-radio>
                        <el-radio value="C">菜单</el-radio>
                        <el-radio value="F">按钮</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item label="菜单图标" prop="icon">
                     <el-popover
                        placement="bottom-start"
                        :width="540"
                        trigger="click"
                     >
                        <template #reference>
                           <el-input v-model="form.icon" placeholder="点击选择图标" @blur="showSelectIcon" readonly>
                              <template #prefix>
                                 <svg-icon
                                    v-if="form.icon"
                                    :icon-class="form.icon"
                                    class="el-input__icon"
                                    style="height: 32px;width: 16px;"
                                 />
                                 <el-icon v-else style="height: 32px;width: 16px;"><search /></el-icon>
                              </template>
                           </el-input>
                        </template>
                        <icon-select ref="iconSelectRef" @selected="selected" :active-icon="form.icon" />
                     </el-popover>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="显示排序" prop="orderNum">
                     <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="菜单名称" prop="menuName">
                     <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item prop="routeName">
                     <template #label>
                        <span>
                           <el-tooltip content="默认不填则和路由地址相同：如地址为：`user`，则名称为`User`（注意：因为router会删除名称相同路由，为避免名字的冲突，特殊情况下请自定义，保证唯一性）" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           路由名称
                        </span>
                     </template>
                     <el-input v-model="form.routeName" placeholder="请输入路由名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择是外链则路由地址需要以`http(s)://`开头" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>是否外链
                        </span>
                     </template>
                     <el-radio-group v-model="form.isFrame">
                        <el-radio value="0">是</el-radio>
                        <el-radio value="1">否</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item prop="path">
                     <template #label>
                        <span>
                           <el-tooltip content="访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           路由地址
                        </span>
                     </template>
                     <el-input v-model="form.path" placeholder="请输入路由地址" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item prop="component">
                     <template #label>
                        <span>
                           <el-tooltip content="访问的组件路径，如：`system/user/index`，默认在`views`目录下" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           组件路径
                        </span>
                     </template>
                     <el-input v-model="form.component" placeholder="请输入组件路径" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'M'">
                  <el-form-item>
                     <el-input v-model="form.perms" placeholder="请输入权限标识" maxlength="100" />
                     <template #label>
                        <span>
                           <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasPermi('system:user:list')`)" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           权限字符
                        </span>
                     </template>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item>
                     <el-input v-model="form.query" placeholder="请输入路由参数" maxlength="255" />
                     <template #label>
                        <span>
                           <el-tooltip content='访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`' placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           路由参数
                        </span>
                     </template>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType == 'C'">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择是则会被`keep-alive`缓存，需要匹配组件的`name`和地址保持一致" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           是否缓存
                        </span>
                     </template>
                     <el-radio-group v-model="form.isCache">
                        <el-radio value="0">缓存</el-radio>
                        <el-radio value="1">不缓存</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.menuType != 'F'">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择隐藏则路由将不会出现在侧边栏，但仍然可以访问" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           显示状态
                        </span>
                     </template>
                     <el-radio-group v-model="form.visible">
                        <el-radio
                           v-for="dict in sys_show_hide"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择停用则路由将不会出现在侧边栏，也不能被访问" placement="top">
                              <el-icon><question-filled /></el-icon>
                           </el-tooltip>
                           菜单状态
                        </span>
                     </template>
                     <el-radio-group v-model="form.status">
                        <el-radio
                           v-for="dict in sys_normal_disable"
                           :key="dict.value"
                           :value="dict.value"
                        >{{ dict.label }}</el-radio>
                     </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
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

<script setup name="Menu">
import { addMenu, delMenu, getMenu, listMenu, updateMenu, updateMenuSort } from "@/api/system/menu"
import SvgIcon from "@/components/SvgIcon"
import IconSelect from "@/components/IconSelect"

const { proxy } = getCurrentInstance()
const { sys_show_hide, sys_normal_disable } = useDict("sys_show_hide", "sys_normal_disable")

const menuList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref("")
const menuOptions = ref([])
const isExpandAll = ref(false)
const refreshTable = ref(true)
const iconSelectRef = ref(null)
const originalOrders = ref({})

const data = reactive({
  form: {},
  queryParams: {
    menuName: undefined,
    visible: undefined
  },
  rules: {
    menuName: [{ required: true, message: "菜单名称不能为空", trigger: "blur" }],
    orderNum: [{ required: true, message: "菜单顺序不能为空", trigger: "blur" }],
    path: [{ required: true, message: "路由地址不能为空", trigger: "blur" }]
  },
})

const { queryParams, form, rules } = toRefs(data)

/** 查询菜单列表 */
function getList() {
  loading.value = true
  listMenu(queryParams.value).then(response => {
    menuList.value = proxy.handleTree(response.data, "menuId")
    recordOriginalOrders(menuList.value)
    loading.value = false
  })
}

/** 查询菜单下拉树结构 */
function getTreeselect() {
  menuOptions.value = []
  listMenu().then(response => {
    const menu = { menuId: 0, menuName: "主类目", children: [] }
    menu.children = proxy.handleTree(response.data, "menuId")
    menuOptions.value.push(menu)
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: "M",
    orderNum: undefined,
    isFrame: "1",
    isCache: "0",
    visible: "0",
    status: "0"
  }
  proxy.resetForm("menuRef")
}

/** 展示下拉图标 */
function showSelectIcon() {
  iconSelectRef.value.reset()
}

/** 选择图标 */
function selected(name) {
  form.value.icon = name
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value.menuName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset()
  getTreeselect()
  if (row != null && row.menuId) {
    form.value.parentId = row.menuId
  } else {
    form.value.parentId = 0
  }
  open.value = true
  title.value = "添加菜单"
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
async function handleUpdate(row) {
  reset()
  await getTreeselect()
  getMenu(row.menuId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改菜单"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["menuRef"].validate(valid => {
    if (valid) {
      if (form.value.menuId != undefined) {
        updateMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}


/** 递归记录原始排序 */
function recordOriginalOrders(list) {
  list.forEach(item => {
    originalOrders.value[item.menuId] = item.orderNum
    if (item.children && item.children.length) {
      recordOriginalOrders(item.children)
    }
  })
}

/** 保存排序 */
function handleSaveSort() {
  const changedMenuIds = []
  const changedOrderNums = []
  const collectChanged = (list) => {
    list.forEach(item => {
      if (String(originalOrders.value[item.menuId]) !== String(item.orderNum)) {
        changedMenuIds.push(item.menuId)
        changedOrderNums.push(item.orderNum)
      }
      if (item.children && item.children.length) {
        collectChanged(item.children)
      }
    })
  }
  collectChanged(menuList.value)
  if (changedMenuIds.length === 0) {
   proxy.$modal.msgWarning("未检测到排序修改")
    return
  }
  updateMenuSort({ menuIds: changedMenuIds.join(","), orderNums: changedOrderNums.join(",") }).then(() => {
   proxy.$modal.msgSuccess("排序保存成功")
    recordOriginalOrders(menuList.value)
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.menuName + '"的数据项?').then(function() {
    return delMenu(row.menuId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.menu-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.menu-page .surface {
  background:#fff;
  border:1px solid var(--ink-200);
  border-radius:var(--r-lg);
  box-shadow:var(--shadow-card);
  overflow:hidden;
  margin-bottom:8px;
}

/* ===== Filter Card ===== */
.menu-page .filter-card {
  padding:14px 20px 16px;
}
.menu-page .filter-card .filter-head {
  display:flex;
  align-items:center;
  justify-content:space-between;
  margin-bottom:12px;
}
.menu-page .filter-card .filter-title {
  display:flex;
  align-items:center;
  gap:8px;
  font-size:14px;
  font-weight:600;
  color:var(--ink-700);
}
.menu-page .filter-card .filter-title .glyph {
  width:4px;
  height:14px;
  background:var(--brand-600);
  border-radius:2px;
}
.menu-page .filter-card .filter-bar {
  display:grid;
  grid-template-columns:repeat(4, minmax(0,1fr));
  gap:12px 16px;
}
.menu-page .filter-card .filter-actions {
  display:flex;
  align-items:center;
  justify-content:space-between;
  margin-top:14px;
  padding-top:14px;
  border-top:1px dashed var(--ink-200);
}
.menu-page .filter-card .filter-info {
  font-size:13px;
  color:var(--ink-500);
  display:flex;
  align-items:center;
  gap:6px;
}
.menu-page .filter-card .filter-buttons {
  display:flex;
  gap:8px;
}

/* ===== Form Field ===== */
.menu-page .field {
  display:flex;
  flex-direction:column;
  gap:6px;
}
.menu-page .field label {
  font-size:14px;
  font-weight:500;
  color:var(--ink-700);
  display:flex;
  align-items:center;
  gap:6px;
}
.menu-page .field .control {
  display:flex;
  align-items:center;
  height:36px;
  padding:0 12px;
  background:#fff;
  border:1px solid var(--ink-200);
  border-radius:var(--r-sm);
  transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out);
}
.menu-page .field .control:focus-within {
  border-color:var(--brand-500);
  box-shadow:0 0 0 3px rgba(99,102,241,.15);
}

/* el-input transparent inside .control */
.menu-page .field .control :deep(.el-input__wrapper) {
  box-shadow:none !important;
  background:transparent !important;
  padding:0;
  height:34px;
}
.menu-page .field .control :deep(.el-input__inner) {
  border:0;
  background:transparent;
  font-size:14px;
  color:var(--ink-900);
  height:34px;
  line-height:34px;
}
.menu-page .field .control :deep(.el-input__inner::placeholder) {
  color:var(--ink-400);
}
.menu-page .field .control :deep(.el-input__prefix) {
  color:var(--ink-400);
  margin-right:4px;
}
.menu-page .field .control :deep(.el-input__prefix .el-icon) {
  font-size:14px;
}

/* el-select transparent inside .control */
.menu-page .field .control :deep(.el-select) {
  width:100%;
}
.menu-page .field .control :deep(.el-select .el-select__wrapper) {
  box-shadow:none !important;
  background:transparent !important;
  padding:0;
  min-height:34px;
  height:34px;
}
.menu-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) {
  font-size:14px;
  color:var(--ink-900);
}
.menu-page .field .control :deep(.el-select .el-select__wrapper.is-focused) {
  box-shadow:none !important;
}

/* ===== Toolbar ===== */
.menu-page .toolbar {
  display:flex;
  align-items:center;
  justify-content:space-between;
  padding:12px 20px;
  border-bottom:1px solid var(--ink-200);
  background:var(--ink-50);
}
.menu-page .toolbar .left {
  display:flex;
  gap:8px;
  align-items:center;
}
.menu-page .toolbar .right {
  display:flex;
  gap:8px;
  align-items:center;
}

/* ===== Table ===== */
.menu-page .table-wrap {
  overflow-x:auto;
}
.menu-page .app-table {
  --el-table-bg-color:#fff;
  --el-table-header-bg-color:var(--ink-50);
  --el-table-row-hover-bg-color:#fafbff;
  --el-table-border-color:transparent;
  --el-table-text-color:var(--ink-700);
  --el-table-header-text-color:var(--ink-500);
}
.menu-page .app-table :deep(.el-table__body td) {
  border-right-color:transparent !important;
}
.menu-page .app-table :deep(.el-table__header th) {
  border-right-color:transparent !important;
}
.menu-page .app-table :deep(.el-table__header th:hover) {
  border-right-color:var(--ink-200) !important;
}
.menu-page .app-table :deep(.el-table__header th) {
  background:var(--ink-50) !important;
  color:var(--ink-500);
  font-weight:600;
  font-size:14px;
  letter-spacing:.02em;
  padding:12px 16px;
  border-bottom:1px solid var(--ink-200);
}
.menu-page .app-table :deep(.el-table__header th .cell) {
  text-transform:uppercase;
}
.menu-page .app-table :deep(.el-table__body td) {
  padding:14px 16px;
  border-bottom:1px solid var(--ink-100);
  color:var(--ink-700);
}
.menu-page .app-table :deep(.el-table__row:hover > td) {
  background:#fafbff !important;
}
.menu-page .app-table :deep(.el-table__inner-wrapper::before) {
  display:none;
}
.menu-page .app-table :deep(.el-table__border-left-patch) {
  display:none;
}

/* ===== Responsive ===== */
@media (max-width:1100px) {
  .menu-page .filter-card .filter-bar {
    grid-template-columns:repeat(2,1fr);
  }
}
@media (max-width:720px) {
  .menu-page .filter-card .filter-bar {
    grid-template-columns:1fr;
  }
  .menu-page .toolbar {
    flex-wrap:wrap;
    gap:10px;
  }
}
</style>
