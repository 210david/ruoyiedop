<template>
  <div class="app-container kd-inv-page">
    <!-- ===== 左树右表布局 ===== -->
    <div class="table-layout">
      <!-- 左侧库存分类树 -->
      <div class="surface tree-panel" v-show="showTreePanel">
        <div class="tree-head">
          <div class="tree-title"><span class="glyph"></span> 库存分类</div>
          <div class="tree-head-actions">
            <el-tooltip content="展开/折叠" placement="top"><el-icon class="tree-act" @click="toggleTreeExpand"><Sort /></el-icon></el-tooltip>
            <el-tooltip content="收起栏目" placement="top"><el-icon class="tree-act" @click="showTreePanel = false"><Fold /></el-icon></el-tooltip>
          </div>
        </div>
        <div class="tree-search">
          <el-input v-model="treeFilterName" placeholder="搜索分类" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <div class="tree-body">
          <el-tree
            ref="treeRef"
            :data="treeData"
            node-key="id"
            :props="{ label: 'label', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterTreeNode"
            highlight-current
            @node-click="handleTreeNodeClick">
            <template #default="{ data }">
              <span class="tree-node">
                <span class="tree-icon lv1" v-if="!data.parentId"><el-icon><Folder /></el-icon></span>
                <span class="tree-icon lv2" v-else><el-icon><Collection /></el-icon></span>
                <span class="tree-node-name">{{ data.label }}</span>
                <span v-if="data.count != null" class="count-tag">{{ data.count }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </div>
      <button v-if="!showTreePanel" type="button" class="tree-toggle btn-soft is-outline" @click="showTreePanel = true">
        <el-icon><Expand /></el-icon> 库存分类
      </button>

      <!-- 右侧库存表 -->
      <div class="table-main">
        <!-- 筛选条件 -->
        <div class="surface filter-card" v-show="showSearch">
          <div class="filter-head">
            <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
          </div>
          <div class="filter-bar">
            <div class="field">
              <label>商品编号</label>
              <div class="control">
                <el-input v-model="queryParams.materialNumber" placeholder="请输入" clearable @keyup.enter="handleQuery" />
              </div>
            </div>
            <div class="field">
              <label>商品名称</label>
              <div class="control">
                <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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

        <div class="surface">
          <div class="toolbar">
            <div class="left">
              <el-button type="primary" plain icon="Refresh" :loading="syncing" @click="handleSync" v-hasPermi="['kingdee:inventory:sync']">
                同步金蝶
              </el-button>
              <el-button icon="RefreshRight" @click="getList">刷新</el-button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table v-loading="loading" :data="invList" border class="app-table">
              <el-table-column type="index" label="序号" width="70" align="center" />
              <el-table-column label="商品编号" prop="materialNumber" width="130" align="center" show-overflow-tooltip />
              <el-table-column label="商品名称" prop="materialName" min-width="200" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="140" show-overflow-tooltip />
              <el-table-column label="仓库名称" prop="stockName" min-width="130" align="center" show-overflow-tooltip />
              <el-table-column label="基本单位" prop="unitName" width="90" align="center" />
              <el-table-column label="即时库存" prop="qty" width="110" align="right">
                <template #default="scope"><span class="num">{{ num(scope.row.qty) }}</span></template>
              </el-table-column>
              <el-table-column label="可用库存" prop="validQty" width="110" align="right">
                <template #default="scope"><span class="num">{{ num(scope.row.validQty) }}</span></template>
              </el-table-column>
              <el-table-column label="可出库数量" prop="availableOutQty" width="120" align="right">
                <template #default="scope"><span class="num">{{ num(scope.row.availableOutQty) }}</span></template>
              </el-table-column>
              <el-table-column label="分类" prop="categoryName" min-width="120" align="center" show-overflow-tooltip>
                <template #default="scope">
                  <span class="badge violet" v-if="scope.row.categoryName">{{ scope.row.categoryName }}</span>
                  <span v-else>-</span>
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
  </div>
</template>

<script setup name="KdInventory">
import { listInventory, listInventoryCategories, syncInventory } from '@/api/kingdee/inventory'
import { Search, RefreshLeft, Refresh, RefreshRight, Filter, Sort, Fold, Expand, Folder, Collection } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const invList = ref([])
const treeData = ref([])
const treeRef = ref(null)
const treeFilterName = ref('')
const showTreePanel = ref(true)
const loading = ref(false)
const syncing = ref(false)
const showSearch = ref(true)
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  materialNumber: undefined,
  materialName: undefined,
  categoryId: undefined
})

/** 查询库存列表 */
function getList() {
  loading.value = true
  listInventory(queryParams.value).then(response => {
    invList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 加载库存分类树（与金蝶商品类别树一致的多级分类） */
function loadTreeData() {
  listInventoryCategories().then(response => {
    const mapNode = (node, parentId) => {
      const subs = (node.children || []).map(c => mapNode(c, node.categoryId))
      return {
        id: node.categoryId,
        label: node.categoryName || node.categoryId,
        parentId,
        count: null,
        children: subs.length ? subs : undefined
      }
    }
    const children = (response.data || []).map(c => mapNode(c, 'root'))
    treeData.value = [{
      id: '',
      label: '全部商品类目',
      parentId: null,
      count: null,
      children
    }]
    // 数据是异步加载的，default-expand-all 不会对后到的数据生效；
    // 默认只展开一级：展开根节点显示全部一级分类，下级由用户按需点开
    nextTick(() => {
      const root = treeRef.value?.store?.nodesMap?.['']
      if (root) root.expanded = true
    })
  }).catch(() => {})
}

/** 树节点过滤 */
watch(treeFilterName, val => {
  treeRef.value?.filter(val)
})
function filterTreeNode(value, data) {
  if (!value) return true
  return (data.label || '').toLowerCase().includes(value.toLowerCase())
}

/** 展开/折叠全部 */
function toggleTreeExpand() {
  const nodes = treeRef.value?.store?.nodesMap || {}
  const first = Object.values(nodes)[0]
  const expand = first ? !first.expanded : false
  Object.values(nodes).forEach(n => { n.expanded = expand })
}

/** 点击分类节点 */
function handleTreeNodeClick(data) {
  queryParams.value.categoryId = data.id || undefined
  queryParams.value.pageNum = 1
  getList()
}

/** 搜索 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置 */
function resetQuery() {
  queryParams.value.materialNumber = undefined
  queryParams.value.materialName = undefined
  queryParams.value.categoryId = undefined
  if (treeRef.value) treeRef.value.setCurrentKey(null)
  handleQuery()
}

/** 同步金蝶库存 */
function handleSync() {
  proxy.$modal.confirm('确认从金蝶云星辰同步库存数据？将全量刷新本地库存表。').then(() => {
    syncing.value = true
    syncInventory().then(res => {
      syncing.value = false
      proxy.$modal.msgSuccess(res.msg || '同步完成')
      loadTreeData()
      getList()
    }).catch(() => {
      syncing.value = false
    })
  }).catch(() => {})
}

/** 数量格式化 */
function num(v) {
  if (v == null || v === '') return '-'
  const n = Number(v)
  if (isNaN(n)) return v
  return n % 1 === 0 ? n.toString() : n.toFixed(2)
}

getList()
loadTreeData()
</script>

<style scoped>
.kd-inv-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --blue-50:#eff6ff; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

.kd-inv-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== 左树右表布局 ===== */
.kd-inv-page .table-layout { display:flex; gap:8px; align-items:flex-start; }
.kd-inv-page .table-main { flex:1; min-width:0; }
.kd-inv-page .tree-panel { flex:0 0 260px; margin-bottom:0; position:sticky; top:10px; }
.kd-inv-page .tree-toggle { margin-bottom:8px; height:36px; }
.kd-inv-page .tree-head { display:flex; align-items:center; justify-content:space-between; padding:12px 14px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.kd-inv-page .tree-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-900); }
.kd-inv-page .tree-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-600); }
.kd-inv-page .tree-head-actions { display:flex; gap:10px; }
.kd-inv-page .tree-act { color:var(--ink-500); cursor:pointer; font-size:16px; transition:color .15s; }
.kd-inv-page .tree-act:hover { color:var(--brand-600); }
.kd-inv-page .tree-search { padding:10px 12px 6px; }
.kd-inv-page .tree-search :deep(.el-input__wrapper) { box-shadow:0 0 0 1px var(--ink-200) inset !important; border-radius:var(--r-sm); }
.kd-inv-page .tree-search :deep(.el-input__wrapper.is-focus) { box-shadow:0 0 0 1px var(--brand-500) inset !important; }
.kd-inv-page .tree-body { padding:4px 8px 10px; max-height:calc(100vh - 280px); overflow-y:auto; }
.kd-inv-page .tree-body :deep(.el-tree) { background:transparent; }
.kd-inv-page .tree-body :deep(.el-tree-node__content) { height:32px; border-radius:6px; }
.kd-inv-page .tree-node { display:inline-flex; align-items:center; gap:7px; overflow:hidden; }
.kd-inv-page .tree-icon { display:inline-flex; align-items:center; justify-content:center; width:22px; height:22px; border-radius:6px; flex-shrink:0; font-size:14px; }
.kd-inv-page .tree-icon.lv1 { background:var(--brand-50); color:var(--brand-600); border:1px solid var(--brand-200); }
.kd-inv-page .tree-icon.lv2 { background:var(--blue-50); color:var(--blue-700); border:1px solid #bfdbfe; }
.kd-inv-page .tree-node-name { font-size:14px; color:var(--ink-700); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.kd-inv-page .count-tag { margin-left:auto; font-size:12px; color:var(--ink-400); padding:1px 6px; }

/* ===== 筛选卡片 ===== */
.kd-inv-page .filter-card { padding:14px 20px 16px; }
.kd-inv-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.kd-inv-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.kd-inv-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.kd-inv-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.kd-inv-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.kd-inv-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.kd-inv-page .filter-card .filter-buttons { display:flex; gap:8px; }

.kd-inv-page .field { display:flex; flex-direction:column; gap:6px; }
.kd-inv-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.kd-inv-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s, box-shadow .15s; }
.kd-inv-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.kd-inv-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.kd-inv-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }

/* ===== 工具栏与表格 ===== */
.kd-inv-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.kd-inv-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.kd-inv-page .table-wrap { overflow-x:auto; }
.kd-inv-page .app-table { --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.kd-inv-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 12px; border-bottom:1px solid var(--ink-200); white-space:nowrap; }
.kd-inv-page .app-table :deep(.el-table__header th .cell) { white-space:nowrap; overflow:visible; text-overflow:clip; }
.kd-inv-page .app-table :deep(.el-table__body td) { padding:12px 12px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); white-space:nowrap; }
.kd-inv-page .app-table :deep(.el-table__body td .cell) { white-space:nowrap; }
.kd-inv-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.kd-inv-page .num { font-variant-numeric:tabular-nums; font-weight:600; }

/* ===== Badges ===== */
.kd-inv-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.kd-inv-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }

/* ===== Buttons ===== */
.kd-inv-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.kd-inv-page .btn-soft .el-icon { font-size:14px; }
.kd-inv-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.kd-inv-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }

@media (max-width:1100px) { .kd-inv-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .kd-inv-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
