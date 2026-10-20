<template>
  <div class="app-container pdm-category-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-row">
        <div class="filter-row-fields">
          <div class="field-inline">
            <label>分类名称</label>
            <el-input v-model="queryParams.categoryName" placeholder="请输入" clearable @keyup.enter="handleQuery" style="width: 220px">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="field-inline">
            <label>状态</label>
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </div>
        </div>
        <div class="filter-row-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="table-layout">
      <!-- 左侧分类结构树 -->
      <div class="surface tree-panel" v-show="showTreePanel">
        <div class="tree-head">
          <div class="tree-title"><span class="glyph"></span> 分类结构</div>
          <div class="tree-head-actions">
            <el-tooltip content="展开/折叠" placement="top"><el-icon class="tree-act" @click="toggleTreeExpand"><Sort /></el-icon></el-tooltip>
            <el-tooltip content="收起栏目" placement="top"><el-icon class="tree-act" @click="showTreePanel = false"><Fold /></el-icon></el-tooltip>
          </div>
        </div>
        <div class="tree-search">
          <el-input v-model="treeFilterName" placeholder="搜索名称/编码" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="tree-body">
          <el-tree
            ref="treeRef"
            :data="treeData"
            node-key="categoryId"
            :props="{ label: 'categoryName', children: 'children' }"
            default-expand-all
            :expand-on-click-node="false"
            :filter-node-method="filterTreeNode"
            highlight-current
            @node-click="handleTreeNodeClick">
            <template #default="{ data }">
              <span class="tree-node">
                <span class="tree-icon lv1" v-if="!data.parentId"><el-icon><Folder /></el-icon></span>
                <span class="tree-icon lv2" v-else><el-icon><Collection /></el-icon></span>
                <span class="tree-node-name">{{ data.categoryName }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>
      <button v-if="!showTreePanel" type="button" class="tree-toggle btn-soft is-outline" @click="showTreePanel = true">
        <el-icon><Expand /></el-icon> 分类结构
      </button>

      <div class="table-main">
        <div class="surface">
          <!-- Toolbar -->
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="['pdm:category:add']">新增</el-button>
              <button type="button" class="btn-soft is-outline" @click="toggleExpandAll">
                <el-icon><Sort /></el-icon> 展开/折叠
              </button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
            </div>
          </div>

          <!-- Table -->
          <div class="table-wrap">
            <el-table
              ref="tableRef"
              v-loading="loading"
              :data="categoryList"
              row-key="categoryId"
              :default-expand-all="isExpandAll"
              :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
              v-if="refreshTable"
              border
              class="app-table"
              @header-dragend="onHeaderDragEnd"
            >
              <el-table-column label="分类名称" prop="categoryName" key="categoryName" :min-width="colWidth('categoryName', 260)" resizable show-overflow-tooltip />
              <el-table-column label="分类编码" prop="categoryCode" key="categoryCode" :width="colWidth('categoryCode', 180)" resizable align="center">
                <template #default="scope"><span class="col-mono">{{ scope.row.categoryCode || '-' }}</span></template>
              </el-table-column>
              <el-table-column label="显示顺序" prop="orderNum" key="orderNum" :width="colWidth('orderNum', 100)" resizable align="center" />
              <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center">
                <template #default="scope">
                  <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                    <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" />
              <el-table-column label="操作" :width="colWidth('操作', 180)" align="center" fixed="right" class-name="col-action">
                <template #default="scope">
                  <div class="action-btn-row">
                    <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['pdm:category:add']">新增</el-button>
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pdm:category:edit']">修改</el-button>
                    <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pdm:category:remove']">删除</el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.categoryCode">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编码：{{ form.categoryCode }}</span>
          </div>
        </div>
      </template>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row>
              <el-col :span="24" v-if="form.categoryId == undefined || form.parentId !== 0">
                <el-form-item label="上级分类" prop="parentId">
                  <el-tree-select
                    v-model="form.parentId"
                    :data="categoryOptions"
                    :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }"
                    value-key="categoryId"
                    placeholder="不选则为顶级分类"
                    check-strictly
                    clearable
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="分类名称" prop="categoryName">
                  <el-input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="128" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="分类编码" prop="categoryCode">
                  <el-input v-model="form.categoryCode" placeholder="保存后自动生成" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="显示顺序" prop="orderNum">
                  <el-input-number v-model="form.orderNum" :min="0" :precision="0" controls-position="right" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="form.status">
                    <el-radio value="0">正常</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('other')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.other">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
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

<script setup name="PdmCategory">
import { listCategory, getCategory, addCategory, updateCategory, delCategory } from '@/api/pdm/category'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, RefreshLeft, Sort, Fold, Expand, Folder, Collection } from '@element-plus/icons-vue'

const { collapsedCards, toggleCard } = useDetailCard(["basic","other"])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_category_index')

const categoryList = ref([])
const categoryOptions = ref([])
const treeData = ref([])
const treeRef = ref(null)
const treeFilterName = ref('')
const showTreePanel = ref(true)
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const isExpandAll = ref(true)
const refreshTable = ref(true)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    categoryName: undefined,
    status: undefined
  },
  rules: {
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询分类列表（平铺后组树，支持筛选） */
function getList() {
  loading.value = true
  listCategory(queryParams.value).then(response => {
    categoryList.value = proxy.handleTree(response.data, 'categoryId')
    loading.value = false
    applySavedWidths()
  })
}

/** 加载完整分类结构树（不受表格筛选影响） */
function loadTreeData() {
  listCategory().then(response => {
    treeData.value = proxy.handleTree(response.data, 'categoryId', 'parentId')
  })
}

// ===== 左侧分类结构树 =====
watch(treeFilterName, val => {
  treeRef.value?.filter(val)
})
function filterTreeNode(value, data) {
  if (!value) return true
  const v = value.toLowerCase()
  return (data.categoryName || '').toLowerCase().includes(v) || (data.categoryCode || '').toLowerCase().includes(v)
}
function toggleTreeExpand() {
  const nodes = treeRef.value?.store?.nodesMap || {}
  const first = Object.values(nodes)[0]
  const expand = first ? !first.expanded : false
  Object.values(nodes).forEach(n => { n.expanded = expand })
}
function handleTreeNodeClick(data) {
  // 点击树节点：按该节点及其子孙过滤右侧表格
  listCategory().then(res => {
    const flat = res.data || []
    const childrenMap = {}
    flat.forEach(n => { (childrenMap[n.parentId] = childrenMap[n.parentId] || []).push(n) })
    const keep = new Set([data.categoryId])
    const queue = [data.categoryId]
    while (queue.length) {
      const cur = queue.shift()
      ;(childrenMap[cur] || []).forEach(c => { keep.add(c.categoryId); queue.push(c.categoryId) })
    }
    categoryList.value = proxy.handleTree(flat.filter(n => keep.has(n.categoryId)), 'categoryId', 'parentId')
  })
}

function handleQuery() { getList() }
function resetQuery() {
  queryParams.value.categoryName = undefined
  queryParams.value.status = undefined
  handleQuery()
}
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 构建树选择选项（排除自身及其子级，防止形成环） */
function buildTreeOptions(excludeId) {
  const source = treeData.value || []
  function filterTree(nodes) {
    return nodes
      .filter(n => n.categoryId !== excludeId)
      .map(n => ({ ...n, children: n.children && n.children.length ? filterTree(n.children) : [] }))
  }
  return filterTree(source)
}

function reset() {
  form.value = {
    categoryId: undefined,
    parentId: undefined,
    categoryName: undefined,
    categoryCode: undefined,
    orderNum: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('categoryRef')
}

function handleAdd(row) {
  reset()
  categoryOptions.value = buildTreeOptions(null)
  if (row != null && row.categoryId) {
    form.value.parentId = row.categoryId
  }
  open.value = true
  title.value = '添加产品分类'
}

function handleUpdate(row) {
  reset()
  getCategory(row.categoryId).then(response => {
    form.value = response.data
    // 上级分类选项排除自身及子级
    categoryOptions.value = buildTreeOptions(row.categoryId)
    open.value = true
    title.value = '修改产品分类'
  })
}

function submitForm() {
  proxy.$refs['categoryRef'].validate(valid => {
    if (valid) {
      if (form.value.parentId === undefined) form.value.parentId = 0
      if (form.value.categoryId != undefined) {
        updateCategory(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
          loadTreeData()
        })
      } else {
        addCategory(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功，分类编码已自动生成')
          open.value = false
          getList()
          loadTreeData()
        })
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除分类【' + row.categoryName + '】？删除前会校验是否存在子分类或产品引用。').then(function () {
    return delCategory(row.categoryId)
  }).then(() => {
    getList()
    loadTreeData()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function cancel() {
  open.value = false
  reset()
}

getList()
loadTreeData()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pdm-category-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --blue-50:#eff6ff; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.pdm-category-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.pdm-category-page .filter-card { padding:12px 20px; }
.pdm-category-page .filter-card .filter-row { display:flex; align-items:center; justify-content:flex-start; gap:16px; flex-wrap:wrap; }
.pdm-category-page .filter-card .filter-row-fields { display:flex; align-items:center; gap:16px; flex-wrap:wrap; flex:1; }
.pdm-category-page .filter-card .filter-row-buttons { display:flex; align-items:center; gap:8px; }
.pdm-category-page .field-inline { display:flex; align-items:center; gap:8px; }
.pdm-category-page .field-inline label { font-size:14px; font-weight:500; color:var(--ink-700); white-space:nowrap; }
.pdm-category-page .field-inline :deep(.el-input__wrapper) { box-shadow:0 0 0 1px var(--ink-200) inset !important; border-radius:var(--r-sm); }
.pdm-category-page .field-inline :deep(.el-input__wrapper.is-focus) { box-shadow:0 0 0 1px var(--brand-500) inset !important; }
.pdm-category-page .field-inline :deep(.el-input__inner) { font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pdm-category-page .field-inline :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pdm-category-page .field-inline :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }

/* ===== Toolbar ===== */
.pdm-category-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-category-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-category-page .toolbar .right { display:flex; gap:8px; align-items:center; }

/* ===== Buttons ===== */
.pdm-category-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pdm-category-page .btn-soft .el-icon { font-size:14px; }
.pdm-category-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pdm-category-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }

/* ===== Table ===== */
.pdm-category-page .table-wrap { overflow-x:auto; }
.pdm-category-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-category-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-category-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-category-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-category-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-category-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-category-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-category-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-category-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badges ===== */
.pdm-category-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-category-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-category-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pdm-category-page .badge.green .dot { background:var(--green-500); }
.pdm-category-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-category-page .badge.gray .dot { background:var(--ink-400); }

/* ===== 操作列 ===== */
.pdm-category-page :deep(.col-action) { padding: 6px 4px !important; }
.pdm-category-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.pdm-category-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.pdm-category-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.pdm-category-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 左树右表布局 ===== */
.pdm-category-page .table-layout { display:flex; gap:8px; align-items:flex-start; }
.pdm-category-page .table-main { flex:1; min-width:0; }
.pdm-category-page .tree-panel { flex:0 0 260px; margin-bottom:0; position:sticky; top:10px; }
.pdm-category-page .tree-toggle { margin-bottom:8px; height:36px; }
.pdm-category-page .tree-head { display:flex; align-items:center; justify-content:space-between; padding:12px 14px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-category-page .tree-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-900); }
.pdm-category-page .tree-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-600); }
.pdm-category-page .tree-head-actions { display:flex; gap:10px; }
.pdm-category-page .tree-act { color:var(--ink-500); cursor:pointer; font-size:16px; transition:color .15s; }
.pdm-category-page .tree-act:hover { color:var(--brand-600); }
.pdm-category-page .tree-search { padding:10px 12px 6px; }
.pdm-category-page .tree-body { padding:4px 8px 10px; max-height:calc(100vh - 280px); overflow-y:auto; }
.pdm-category-page .tree-body :deep(.el-tree) { background:transparent; }
.pdm-category-page .tree-body :deep(.el-tree-node__content) { height:32px; border-radius:6px; }
.pdm-category-page .tree-node { display:inline-flex; align-items:center; gap:7px; overflow:hidden; }
.pdm-category-page .tree-icon { display:inline-flex; align-items:center; justify-content:center; width:22px; height:22px; border-radius:6px; flex-shrink:0; font-size:14px; }
.pdm-category-page .tree-icon.lv1 { background:var(--brand-50); color:var(--brand-600); border:1px solid var(--brand-200); }
.pdm-category-page .tree-icon.lv2 { background:var(--blue-50); color:var(--blue-700); border:1px solid #bfdbfe; }
.pdm-category-page .tree-node-name { font-size:14px; color:var(--ink-700); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }

@media (max-width:720px) { .pdm-category-page .filter-card .filter-row { flex-direction:column; align-items:stretch; } .pdm-category-page .filter-card .filter-row-fields { flex-direction:column; align-items:stretch; } .pdm-category-page .field-inline { flex-direction:column; align-items:stretch; } .pdm-category-page .field-inline :deep(.el-input) { width:100% !important; } }
</style>
