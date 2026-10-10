<template>
  <div class="app-container fin-subject-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>科目编码</label>
          <div class="control">
            <el-input v-model="queryParams.subjectCode" placeholder="请输入编码" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>科目名称</label>
          <div class="control">
            <el-input v-model="queryParams.subjectName" placeholder="请输入名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>科目类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.category" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in subjectCategoryOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
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

    <!-- ===== 数据卡片 ===== -->
    <div class="surface">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button v-hasPermi="['fin:subject:add']" type="primary" plain icon="Plus" @click="handleAdd()">新增</el-button>
          <el-button v-hasPermi="['fin:subject:edit']" type="success" plain icon="Edit" :disabled="singleNode === null" @click="handleUpdate()">修改</el-button>
          <el-button v-hasPermi="['fin:subject:remove']" type="danger" plain icon="Delete" :disabled="multipleSelection.length === 0" @click="handleDelete()">删除</el-button>
          <el-button v-hasPermi="['fin:subject:export']" plain icon="Download" @click="handleExport">导出</el-button>
          <el-button v-hasPermi="['fin:subject:import']" plain icon="Upload" @click="handleImport">导入</el-button>
        </div>
        <div class="right">
          <el-tooltip content="展开/折叠" placement="top">
            <el-icon class="tree-act" @click="toggleTreeExpand"><Sort /></el-icon>
          </el-tooltip>
          <el-tooltip content="刷新" placement="top">
            <el-icon class="tree-act" @click="getList"><Refresh /></el-icon>
          </el-tooltip>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="fin_subject_index" />
        </div>
      </div>

      <!-- 提示条：说明三条硬规则 -->
      <el-alert type="info" :closable="false" show-icon style="margin: 12px 16px 0;">
        <template #title>
          <span class="rule-hint">
            科目层级最多 4 级 ｜ 有子科目不可删除 ｜ 已有凭证分录不可删除（请改为停用）
          </span>
        </template>
      </el-alert>

      <!-- 表格：树形（树形数据不分页） -->
      <div class="table-wrap">
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="subjectTree"
          row-key="subjectId"
          border
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          :default-expand-all="expandAll"
          @selection-change="handleSelectionChange"
          @current-change="handleCurrentChange"
          @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" label="序号" key="序号" width="85" resizable align="center" />
          <el-table-column label="科目编码" prop="subjectCode" key="subjectCode" :width="colWidth('subjectCode', 130)" resizable align="center" v-if="columns.subjectCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.subjectCode }}</span></template>
          </el-table-column>
          <el-table-column label="科目名称" prop="subjectName" key="subjectName" :min-width="colWidth('subjectName', 220)" resizable show-overflow-tooltip align="center" v-if="columns.subjectName.visible" />
          <el-table-column label="科目类别" prop="category" key="category" :width="colWidth('category', 110)" resizable align="center" v-if="columns.category.visible">
            <template #default="scope">
              <span class="badge no-dot violet" v-if="scope.row.category">{{ categoryLabel(scope.row.category) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="余额方向" prop="direction" key="direction" :width="colWidth('direction', 90)" resizable align="center" v-if="columns.direction.visible">
            <template #default="scope">
              <span :class="scope.row.direction === 'DEBIT' ? 'dir-debit' : 'dir-credit'">
                {{ scope.row.direction === 'DEBIT' ? '借' : '贷' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="级次" prop="level" key="level" :width="colWidth('level', 70)" resizable align="center" v-if="columns.level.visible">
            <template #default="scope">
              <span class="badge no-dot blue">{{ scope.row.level }}</span>
            </template>
          </el-table-column>
          <el-table-column label="明细科目" prop="isLeaf" key="isLeaf" :width="colWidth('isLeaf', 90)" resizable align="center" v-if="columns.isLeaf.visible">
            <template #default="scope">
              <span class="badge no-dot" :class="scope.row.isLeaf === '1' ? 'green' : 'gray'">{{ scope.row.isLeaf === '1' ? '是' : '否' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="现金等价物" prop="isCashEquiv" key="isCashEquiv" :width="colWidth('isCashEquiv', 100)" resizable align="center" v-if="columns.isCashEquiv.visible">
            <template #default="scope">
              <span class="badge no-dot amber" v-if="scope.row.isCashEquiv === '1'">是</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['fin:subject:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['fin:subject:edit']">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDeleteRow(scope.row)" v-hasPermi="['fin:subject:remove']">删除</el-button>
                <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['fin:subject:add']">新增下级</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container"><!-- 树形表格整体加载，不分页 --></div>
    </div>

    <!-- ===== 新增/修改弹框 ===== -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ dialogTitle }}</span>
        </div>
      </template>
      <div class="rd-page">
      <el-form ref="subjectRefForm" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级科目" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="parentTreeOptions"
                :props="{ label: 'subjectName', value: 'subjectId', children: 'children' }"
                value-key="subjectId"
                placeholder="不选则为一级科目"
                check-strictly
                clearable
                style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="级次" prop="level">
              <el-input v-model="form.level" disabled placeholder="由上级科目自动推导" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科目编码" prop="subjectCode">
              <el-input v-model="form.subjectCode" placeholder="如 1001 / 2202 / 6001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目名称" prop="subjectName">
              <el-input v-model="form.subjectName" placeholder="如 库存现金" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科目类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%" @change="onCategoryChange">
                <el-option v-for="d in subjectCategoryOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="余额方向" prop="direction">
              <el-radio-group v-model="form.direction">
                <el-radio value="DEBIT">借</el-radio>
                <el-radio value="CREDIT">贷</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="是否明细科目">
              <el-radio-group v-model="form.isLeaf">
                <el-radio value="1">明细科目</el-radio>
                <el-radio value="0">非明细</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现金等价物">
              <el-switch v-model="form.isCashEquiv" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="现金流分类">
              <el-select v-model="form.cashFlowCategory" placeholder="非现金科目可不填" clearable style="width: 100%">
                <el-option label="经营活动" value="OPERATING" />
                <el-option label="投资活动" value="INVESTING" />
                <el-option label="筹资活动" value="FINANCING" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="辅助核算">
              <el-select v-model="auxTypesArr" multiple placeholder="按需勾选" style="width: 100%">
                <el-option label="客户" value="C" />
                <el-option label="供应商" value="S" />
                <el-option label="部门" value="D" />
                <el-option label="项目" value="P" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 只读详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">会计科目详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.subjectCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.subjectCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息
            </div>
          </div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">科目编码</span>
                <div class="rd-value"><span class="col-mono">{{ viewData.subjectCode || '-' }}</span></div>
              </div>
              <div class="rd-item">
                <span class="rd-label">科目名称</span>
                <div class="rd-value">{{ viewData.subjectName || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">科目类别</span>
                <div class="rd-value">{{ categoryLabel(viewData.category) || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">余额方向</span>
                <div class="rd-value">{{ viewData.direction === 'DEBIT' ? '借' : (viewData.direction === 'CREDIT' ? '贷' : '-') }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">级次</span>
                <div class="rd-value">{{ viewData.level || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">明细科目</span>
                <div class="rd-value">{{ viewData.isLeaf === '1' ? '是' : (viewData.isLeaf === '0' ? '否' : '-') }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">现金等价物</span>
                <div class="rd-value">{{ viewData.isCashEquiv === '1' ? '是' : '否' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">现金流分类</span>
                <div class="rd-value">{{ cashFlowLabel(viewData.cashFlowCategory) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">辅助核算</span>
                <div class="rd-value">{{ auxTypesText || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">排序号</span>
                <div class="rd-value">{{ viewData.sortOrder ?? '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">状态</span>
                <div class="rd-value">
                  <span class="badge" :class="viewData.status === '0' ? 'green' : 'gray'">
                    <span class="dot"></span>{{ viewData.status === '0' ? '正常' : '停用' }}
                  </span>
                </div>
              </div>
              <div class="rd-item rd-item--full" v-if="viewData.remark">
                <span class="rd-label">备注</span>
                <div class="rd-value">{{ viewData.remark }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
    <excel-import-dialog
      ref="importRef"
      title="会计科目导入"
      action="/fin/subject/importData"
      template-action="/fin/subject/importTemplate"
      template-file-name="subject_template"
      :tips="['科目编码全局唯一；上级科目需先于子科目存在或同批导入',
              '科目类别：ASSET/LIABILITY/EQUITY/COST/INCOME；余额方向：DEBIT/CREDIT']"
      @success="getList"
    />
  </div>
</template>

<script setup name="FinSubject">
import { listSubject, addSubject, updateSubject, delSubject, checkCodeUnique } from '@/api/fin/subject'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import { useColumnResize } from '@/composables/useColumnResize'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const expandAll = ref(true)
const subjectTree = ref([])
const flatList = ref([])
const multipleSelection = ref([])
const singleNode = ref(null)
const isEdit = ref(false)
const auxTypesArr = ref([])

// 列宽持久化
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('fin_subject_index')

const columns = ref({
  subjectCode: { visible: true },
  subjectName: { visible: true },
  category: { visible: true },
  direction: { visible: true },
  level: { visible: true },
  isLeaf: { visible: true },
  isCashEquiv: { visible: true },
  status: { visible: true }
})

/** 加载已保存的列显示配置 */
function loadColumnVisibility() {
  try {
    const saved = JSON.parse(localStorage.getItem('fin_subject_index_visible') || '{}')
    Object.keys(columns.value).forEach(k => {
      if (typeof saved[k] === 'boolean') columns.value[k].visible = saved[k]
    })
  } catch (e) {}
}

onMounted(() => {
  loadColumnVisibility()
})

const subjectCategoryOptions = [
  { value: 'ASSET', label: '资产' },
  { value: 'LIABILITY', label: '负债' },
  { value: 'EQUITY', label: '所有者权益' },
  { value: 'COST', label: '成本' },
  { value: 'INCOME', label: '损益' }
]

// 科目大类变更时自动带出默认余额方向，减少手工选择
const CATEGORY_DEFAULT_DIRECTION = {
  ASSET: 'DEBIT',
  LIABILITY: 'CREDIT',
  EQUITY: 'CREDIT',
  COST: 'DEBIT',
  INCOME: 'CREDIT'
}

const data = reactive({
  form: {},
  queryParams: {
    subjectCode: undefined,
    subjectName: undefined,
    category: undefined,
    status: undefined
  },
  rules: {
    subjectCode: [
      { required: true, message: '科目编码不能为空', trigger: 'blur' },
      { pattern: /^\d{1,20}$/, message: '科目编码必须为数字，如 1001', trigger: 'blur' },
      { validator: validateCodeUnique, trigger: 'blur' }
    ],
    subjectName: [{ required: true, message: '科目名称不能为空', trigger: 'blur' }],
    category: [{ required: true, message: '科目类别不能为空', trigger: 'change' }],
    direction: [{ required: true, message: '余额方向不能为空', trigger: 'change' }],
    level: [{ required: true, message: '级次不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 已选筛选条件数 */
const activeFilterCount = computed(() => {
  return Object.values(queryParams.value).filter(v => v !== undefined && v !== null && v !== '').length
})

/** 详情弹窗辅助核算文本 */
const auxTypesText = computed(() => {
  if (!viewData.value.auxTypes) return ''
  const map = { C: '客户', S: '供应商', D: '部门', P: '项目' }
  return viewData.value.auxTypes.split(',').map(v => map[v.trim()] || v.trim()).join('、')
})

const parentTreeOptions = computed(() => {
  return [{ subjectId: null, subjectName: '（无，一级科目）', children: subjectTree.value }]
})

const dialogTitle = computed(() => {
  if (isEdit.value) return '修改会计科目'
  return form.value.parentId ? '新增下级科目' : '新增一级科目'
})

function categoryLabel(v) {
  return subjectCategoryOptions.find(o => o.value === v)?.label || v
}

function cashFlowLabel(v) {
  const map = { OPERATING: '经营活动', INVESTING: '投资活动', FINANCING: '筹资活动' }
  return v ? (map[v] || v) : '-'
}

/** 编码唯一性校验（后端兜底，此处为前端提前拦截） */
function validateCodeUnique(rule, value, callback) {
  if (!value) return callback()
  checkCodeUnique(value, form.value.subjectId).then(res => {
    if (res.data) callback()
    else callback(new Error('科目编码已存在'))
  }).catch(() => callback())
}

/** 加载科目树 */
const importRef = ref(null)
function handleImport() {
  importRef.value.open()
}
function getList() {
  loading.value = true
  listSubject(queryParams.value).then(res => {
    flatList.value = res.data || []
    subjectTree.value = proxy.handleTree(flatList.value, 'subjectId', 'parentId')
    loading.value = false
    applySavedWidths() // 应用保存的列宽
  })
}

/** 展开/收起全部 */
function toggleTreeExpand() {
  expandAll.value = !expandAll.value
  flatList.value.forEach(row => {
    tableRef.value && tableRef.value.toggleRowExpansion(row, expandAll.value)
  })
}

/** 搜索 */
function handleQuery() {
  getList()
}

/** 重置：显式重建 queryParams */
function resetQuery() {
  queryParams.value = {
    subjectCode: undefined,
    subjectName: undefined,
    category: undefined,
    status: undefined
  }
  getList()
}

/** 多选 */
function handleSelectionChange(selection) {
  multipleSelection.value = selection
}

/** 当前选中树节点 */
function handleCurrentChange(row) {
  singleNode.value = row
}

/** 类别联动默认方向 */
function onCategoryChange(val) {
  if (CATEGORY_DEFAULT_DIRECTION[val]) {
    form.value.direction = CATEGORY_DEFAULT_DIRECTION[val]
  }
}

/** 新增（row 可选：新增下级） */
function handleAdd(row) {
  reset()
  isEdit.value = false
  if (row && row.subjectId) {
    form.value.parentId = row.subjectId
    form.value.level = (row.level || 1) + 1
  }
  open.value = true
}

/** 修改 */
function handleUpdate(row) {
  reset()
  isEdit.value = true
  form.value = { ...row }
  auxTypesArr.value = row.auxTypes ? row.auxTypes.split(',').map(v => v.trim()).filter(Boolean) : []
  open.value = true
}

/** 详情（只读弹窗） */
function handleView(row) {
  viewData.value = { ...row }
  viewOpen.value = true
}

/** 表单重置 */
function reset() {
  form.value = {
    subjectId: undefined,
    parentId: undefined,
    subjectCode: undefined,
    subjectName: undefined,
    category: undefined,
    direction: undefined,
    level: undefined,
    isLeaf: '1',
    isCashEquiv: '0',
    cashFlowCategory: undefined,
    auxTypes: undefined,
    sortOrder: 0,
    status: '0',
    remark: undefined
  }
  auxTypesArr.value = []
  proxy.resetForm('subjectRefForm')
}

/** 提交 */
function submitForm() {
  proxy.$refs['subjectRefForm'].validate(valid => {
    if (!valid) return
    form.value.auxTypes = auxTypesArr.value.join(',')
    const api = isEdit.value ? updateSubject : addSubject
    api(form.value).then(() => {
      proxy.$modal.msgSuccess(isEdit.value ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

/** 取消 */
function cancel() {
  open.value = false
  reset()
}

/** 批量删除 */
function handleDelete() {
  if (!multipleSelection.value.length) {
    proxy.$modal.msgWarning('请先勾选要删除的科目')
    return
  }
  const ids = multipleSelection.value.map(r => r.subjectId)
  proxy.$modal.confirm('是否确认删除选中的 ' + ids.length + ' 条科目？').then(() => {
    return delSubject(ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 单行删除 */
function handleDeleteRow(row) {
  proxy.$modal.confirm('是否确认删除科目「' + row.subjectName + '」？').then(() => {
    return delSubject([row.subjectId])
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出 */
function handleExport() {
  proxy.download('fin/subject/export', { ...queryParams.value }, `subject_${new Date().getTime()}.xlsx`)
}

getList()
</script>

<style scoped>
/* ===== design tokens（list guide 7.1）===== */
.fin-subject-page {
  --page-primary: #2f54eb;
  --page-primary-light: #e8effd;
  --page-success: #16a34a;
  --page-warning: #d97706;
  --page-danger: #dc2626;
  --page-text: #1f2937;
  --page-text-secondary: #6b7280;
  --page-border: #e5e7eb;
  --page-radius: 8px;
}

/* ===== surface 卡片 ===== */
.fin-subject-page .surface {
  background: #fff;
  border-radius: var(--page-radius);
  border: 1px solid var(--page-border);
  box-shadow: 0 1px 2px rgba(16, 24, 40, 0.04);
}

/* ===== 筛选卡片 ===== */
.fin-subject-page .filter-card {
  margin-bottom: 12px;
  padding: 14px 16px 4px;
}
.fin-subject-page .filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.fin-subject-page .filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--page-text);
}
.fin-subject-page .filter-title .glyph {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: var(--page-primary-light);
  color: var(--page-primary);
  font-size: 13px;
}
.fin-subject-page .filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  align-items: flex-start;
}
.fin-subject-page .field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 200px;
}
.fin-subject-page .field > label {
  font-size: 12px;
  color: var(--page-text-secondary);
  line-height: 18px;
}
.fin-subject-page .field .control {
  width: 100%;
}
.fin-subject-page .field .control :deep(.el-input__wrapper),
.fin-subject-page .field .control :deep(.el-select .el-input__wrapper) {
  background: #f9fafb;
  box-shadow: 0 0 0 1px var(--page-border) inset;
}
.fin-subject-page .filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 2px 0 12px;
  margin-left: auto;
}
.fin-subject-page .filter-info {
  font-size: 12px;
  color: var(--page-text-secondary);
  margin-right: 4px;
  white-space: nowrap;
}

/* ===== 工具栏 / 表格 ===== */
.fin-subject-page .toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--page-border);
}
.fin-subject-page .table-wrap {
  padding: 0 16px 12px;
}
.fin-subject-page .pagination-container {
  padding: 8px 16px 12px;
  display: flex;
  justify-content: flex-end;
}
.fin-subject-page :deep(.el-table) {
  --el-table-border-color: var(--page-border);
}
.fin-subject-page .col-mono {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
}

/* ===== badge（无圆点/带圆点）===== */
.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 18px;
  white-space: nowrap;
}
.badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.badge.no-dot .dot { display: none; }
.badge.violet { background: #f3f0ff; color: #7c3aed; }
.badge.blue { background: #e8effd; color: #2f54eb; }
.badge.green { background: #e7f7ee; color: #16a34a; }
.badge.gray { background: #f3f4f6; color: #6b7280; }
.badge.amber { background: #fdf3e3; color: #d97706; }
.badge.red { background: #fdecec; color: #dc2626; }

/* ===== 操作列（list guide 5.5.3）===== */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== 余额方向 / 备注提示 ===== */
.dir-debit { color: var(--page-danger); font-weight: 500; }
.dir-credit { color: var(--page-primary); font-weight: 500; }
.rule-hint { font-size: 12px; color: var(--page-text-secondary); line-height: 18px; }
.text-muted { color: var(--page-text-secondary); }

/* ===== rd-dialog 弹窗（detail guide 第五章）===== */
:deep(.rd-dialog) {
  border-radius: 12px;
  overflow: hidden;
}
:deep(.rd-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
}
:deep(.rd-dialog .el-dialog__body) {
  padding: 16px 20px;
  max-height: 62vh;
  overflow: auto;
}
.rd-detail-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: linear-gradient(90deg, var(--page-primary-light), #fff);
  border-bottom: 1px solid var(--page-border);
}
.rd-detail-header-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--page-primary);
  color: #fff;
}
.rd-detail-header-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--page-text);
}
.rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 4px;
}
.rd-detail-header-divider {
  width: 1px;
  height: 14px;
  background: var(--page-border);
}
.rd-detail-header-no {
  font-size: 13px;
  color: var(--page-text-secondary);
}

.rd-page { display: flex; flex-direction: column; gap: 12px; }
.rd-card {
  background: #fff;
  border: 1px solid var(--page-border);
  border-radius: var(--page-radius);
  overflow: hidden;
}
.rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--page-border);
  background: #fafbfc;
}
.rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--page-text);
}
.rd-card-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: var(--page-primary-light);
  color: var(--page-primary);
}
.rd-card-body { padding: 12px 14px; }
.rd-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 20px;
}
.rd-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.rd-item--full { grid-column: 1 / -1; }
.rd-label { font-size: 12px; color: var(--page-text-secondary); }
.rd-value { font-size: 13px; color: var(--page-text); line-height: 20px; word-break: break-all; }

:deep(.rd-dialog .dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
