<template>
  <div class="app-container flowable-category-page">
    <!-- ===== 筛选条件卡片 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>名称</label>
          <div class="control">
            <el-input v-model="queryParams.dictLabel" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['flowable:category:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-hasPermi="['flowable:category:remove']">批量删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="flowable_category_index" />
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table"
          @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="图标" prop="icon" key="icon" :width="colWidth('icon', 90)" align="center" v-if="columns.icon.visible">
            <template #default="scope">
              <span class="icon-cell"><el-icon :size="18"><component :is="iconComp(scope.row.icon)" /></el-icon></span>
            </template>
          </el-table-column>
          <el-table-column label="名称" prop="dictLabel" key="dictLabel" :min-width="colWidth('dictLabel', 240)" align="center" show-overflow-tooltip resizable v-if="columns.dictLabel.visible">
            <template #default="scope"><span class="col-strong">{{ scope.row.dictLabel }}</span></template>
          </el-table-column>
          <el-table-column label="描述" prop="remark" key="remark" :min-width="colWidth('remark', 260)" align="center" show-overflow-tooltip resizable v-if="columns.remark.visible">
            <template #default="scope">{{ scope.row.remark || '—' }}</template>
          </el-table-column>
          <el-table-column label="显示顺序" prop="dictSort" key="dictSort" :width="colWidth('dictSort', 110)" align="center" resizable v-if="columns.dictSort.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['flowable:category:edit']">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['flowable:category:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" @change="getList" />
      </div>
    </div>

    <!-- ===== 新增/编辑弹窗 ===== -->
    <el-dialog v-model="open" width="560px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ form.dictCode ? '编辑流程分类' : '新增流程分类' }}</span>
        </div>
      </template>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="90px" size="small">
        <el-form-item label="图标" prop="icon">
          <div class="icon-field">
            <span class="icon-cell"><el-icon :size="18"><component :is="iconComp(form.icon)" /></el-icon></span>
            <el-button size="small" icon="Grid" @click="iconPickerOpen = true">浏览</el-button>
          </div>
        </el-form-item>
        <el-form-item label="名称" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="如：财务类" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如：报销/付款申请等" maxlength="200" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="dictSort">
          <el-input-number v-model="form.dictSort" :min="1" :max="999" controls-position="right" style="width:160px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">保 存</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 图标浏览选择弹窗 ===== -->
    <el-dialog v-model="iconPickerOpen" width="480px" append-to-body draggable class="rd-dialog" title="选择图标" :close-on-click-modal="false">
      <div class="icon-grid">
        <button v-for="ic in iconOptions" :key="ic" type="button" class="icon-item"
          :class="{ 'is-active': pendingIcon === ic }" @click="pendingIcon = ic" @dblclick="confirmIcon">
          <el-icon :size="18"><component :is="iconComp(ic)" /></el-icon>
        </button>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmIcon">确 定</el-button>
        <el-button @click="iconPickerOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowCategory">
import { listCategory, getCategory, addCategory, updateCategory, delCategory } from '@/api/flowable/category'
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, Document, Coin, User, Files, Briefcase, Ticket, MessageBox, SetUp, DataAnalysis, Management, Folder, FolderOpened, Star, Flag, List, Notebook, Calendar, AlarmClock, Promotion, Histogram, Goods, OfficeBuilding, School } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('flowable_category_index')
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const categoryRef = ref(null)
const selectedIds = ref([])
const iconPickerOpen = ref(false)
const pendingIcon = ref('Document')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, dictLabel: undefined }
})
const { queryParams, form } = toRefs(data)

// ===== 列显隐（RightToolbar 持久化） =====
const defaultColumns = {
  icon: { label: '图标', visible: true },
  dictLabel: { label: '名称', visible: true },
  remark: { label: '描述', visible: true },
  dictSort: { label: '显示顺序', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('flowable_category_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
watch(columns, v => localStorage.setItem('flowable_category_columns', JSON.stringify(v)), { deep: true })

const rules = {
  dictLabel: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  dictSort: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }]
}

// 可选图标（存组件名，展示用动态渲染）
const iconOptions = ['Document', 'Coin', 'User', 'Files', 'Briefcase', 'Ticket', 'MessageBox', 'SetUp', 'DataAnalysis', 'Management', 'Folder', 'FolderOpened', 'Star', 'Flag', 'List', 'Notebook', 'Calendar', 'AlarmClock', 'Promotion', 'Histogram', 'Goods', 'OfficeBuilding', 'School']
const iconMap = { Document, Coin, User, Files, Briefcase, Ticket, MessageBox, SetUp, DataAnalysis, Management, Folder, FolderOpened, Star, Flag, List, Notebook, Calendar, AlarmClock, Promotion, Histogram, Goods, OfficeBuilding, School }
function iconComp(name) {
  return iconMap[name] || Document
}

const activeFilterCount = computed(() => {
  return [queryParams.value.dictLabel].filter(v => v !== undefined && v !== null && v !== '').length
})

function getList() {
  loading.value = true
  listCategory(queryParams.value).then(res => {
    list.value = res.rows || []
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.dictLabel = undefined; handleQuery() }

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.dictCode)
}

function handleAdd() {
  form.value = { dictCode: undefined, dictLabel: undefined, icon: 'Document', dictSort: nextSort(), remark: undefined }
  open.value = true
  nextTick(() => categoryRef.value && categoryRef.value.resetFields())
}

function nextSort() {
  const max = list.value.reduce((m, r) => Math.max(m, r.dictSort || 0), 0)
  return max + 1
}

async function handleEdit(row) {
  const res = await getCategory(row.dictCode)
  form.value = { ...res.data, icon: res.data.icon || 'Document' }
  open.value = true
  nextTick(() => categoryRef.value && categoryRef.value.clearValidate())
}

function confirmIcon() {
  form.value.icon = pendingIcon.value
  iconPickerOpen.value = false
}

function submitForm() {
  categoryRef.value.validate(valid => {
    if (!valid) return
    const payload = {
      dictCode: form.value.dictCode, dictLabel: form.value.dictLabel, icon: form.value.icon,
      dictSort: form.value.dictSort, remark: form.value.remark
    }
    if (form.value.dictCode) {
      updateCategory(payload).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
    } else {
      addCategory(payload).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除分类"' + row.dictLabel + '"？该分类被流程引用时删除将被拒绝。').then(() => delCategory(row.dictCode)).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}

function handleBatchDelete() {
  proxy.$modal.confirm('确认删除选中的 ' + selectedIds.value.length + ' 个分类？被流程引用的分类删除将被拒绝。').then(() => delCategory(selectedIds.value.join(','))).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('批量删除失败：' + (e?.message || e)) })
}

getList()
onActivated(() => { getList() })
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.icon-cell { display:inline-flex; align-items:center; justify-content:center; width:32px; height:32px; border-radius:8px; background:var(--brand-50, #eef2ff); color:var(--brand-600, #4f46e5); }
.icon-field { display:flex; align-items:center; gap:10px; }
.icon-grid { display:grid; grid-template-columns:repeat(6, 1fr); gap:8px; padding:4px; }
.icon-item { display:flex; align-items:center; justify-content:center; height:44px; border:1px solid var(--ink-200, #e2e8f0); border-radius:8px; background:#fff; color:var(--ink-500, #64748b); cursor:pointer; transition:all .15s; }
.icon-item:hover { border-color:var(--brand-500, #6366f1); color:var(--brand-600, #4f46e5); }
.icon-item.is-active { border-color:var(--brand-500, #6366f1); background:var(--brand-50, #eef2ff); color:var(--brand-600, #4f46e5); }
</style>
