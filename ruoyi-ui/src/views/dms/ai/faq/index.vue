<template>
  <div class="app-container dms-ai-faq-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>问题</label>
          <div class="control">
            <el-input v-model="queryParams.question" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>关键词</label>
          <div class="control">
            <el-input v-model="queryParams.keywords" placeholder="请输入" clearable @keyup.enter="handleQuery">
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['dms:ai:faq:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['dms:ai:faq:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['dms:ai:faq:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="dms_ai_faq_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="问题" prop="question" key="question" :width="colWidth('question', 300)" resizable show-overflow-tooltip v-if="columns.question.visible" />
          <el-table-column label="分类" prop="categoryName" key="categoryName" :width="colWidth('categoryName', 120)" resizable v-if="columns.categoryName.visible">
            <template #default="scope">
              <span class="badge blue">{{ scope.row.categoryName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="关键词" prop="keywords" key="keywords" :width="colWidth('keywords', 180)" resizable show-overflow-tooltip v-if="columns.keywords.visible" />
          <el-table-column label="查看次数" prop="viewCount" key="viewCount" :width="colWidth('viewCount', 90)" resizable align="center" v-if="columns.viewCount.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'gray'"><span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['dms:ai:faq:edit']">修改</el-button>
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['dms:ai:faq:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/><circle cx="12" cy="12" r="10"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="faqRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/><circle cx="12" cy="12" r="10"/></svg></span>FAQ信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0">
        <el-form-item label="问题" prop="question"><el-input v-model="form.question" placeholder="请输入问题" /></el-form-item>
        <el-form-item label="标准答案" prop="answer"><el-input v-model="form.answer" type="textarea" :rows="5" placeholder="请输入标准答案" /></el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="分类" prop="categoryName"><el-input v-model="form.categoryName" placeholder="如：数控车床" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="关键词" prop="keywords"><el-input v-model="form.keywords" placeholder="多个关键词用逗号分隔，如：主轴,异响,温度" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
                </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="DmsRepairFaq">
import { listFaq, getFaq, addFaq, updateFaq, delFaq } from '@/api/dms/ai'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(["c0"])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('dms_ai_faq_index')

const list = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  question: { label: '问题', visible: true },
  categoryName: { label: '分类', visible: true },
  keywords: { label: '关键词', visible: true },
  viewCount: { label: '查看次数', visible: true },
  status: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('dms_ai_faq_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.question) count++
  if (queryParams.value.keywords) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, question: undefined, keywords: undefined },
  rules: {
    question: [{ required: true, message: '问题不能为空', trigger: 'blur' }],
    answer: [{ required: true, message: '标准答案不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listFaq(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.faqId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { question: undefined, answer: undefined, categoryName: undefined, keywords: undefined, status: '0', remark: undefined }; proxy.resetForm('faqRef') }
function handleAdd() { reset(); open.value = true; title.value = '新增FAQ' }
function handleUpdate(row) { reset(); getFaq(row.faqId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改FAQ' }) }
function submitForm() {
  proxy.$refs['faqRef'].validate(valid => {
    if (valid) {
      if (form.value.faqId != undefined) { updateFaq(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addFaq(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const faqIds = row.faqId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delFaq(faqIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
getList()
</script>

<style scoped>
.dms-ai-faq-page {
  padding-top: 10px;
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
.dms-ai-faq-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.dms-ai-faq-page .filter-card { padding:14px 20px 16px; }
.dms-ai-faq-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.dms-ai-faq-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.dms-ai-faq-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.dms-ai-faq-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.dms-ai-faq-page .filter-card .field { display:flex; flex-direction:column; gap:4px; }
.dms-ai-faq-page .filter-card .field > label { font-size:13px; font-weight:500; color:var(--ink-500); }
.dms-ai-faq-page .filter-card .field .control { width:100%; }
.dms-ai-faq-page .filter-card .field .control .el-input, .dms-ai-faq-page .filter-card .field .control .el-select { width:100%; }
.dms-ai-faq-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.dms-ai-faq-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.dms-ai-faq-page .filter-card .filter-buttons { display:flex; gap:8px; }
.dms-ai-faq-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 16px; border-bottom:1px solid var(--ink-200); }
.dms-ai-faq-page .toolbar .left { display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.dms-ai-faq-page .toolbar .right { display:flex; align-items:center; gap:8px; }
.dms-ai-faq-page .toolbar-divider { width:1px; height:20px; background:var(--ink-200); margin:0 4px; }
.dms-ai-faq-page .btn-soft { display:inline-flex; align-items:center; gap:4px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; cursor:pointer; transition:all .15s var(--ease-out); border:1px solid transparent; background:transparent; color:var(--ink-700); }
.dms-ai-faq-page .btn-soft.is-outline { border-color:var(--ink-300); background:#fff; }
.dms-ai-faq-page .btn-soft.is-outline:hover { border-color:var(--brand-400); color:var(--brand-600); background:var(--brand-50); }
.dms-ai-faq-page .btn-soft.is-danger-outline { border-color:#fecaca; background:var(--red-50); color:var(--red-700); }
.dms-ai-faq-page .btn-soft.is-danger-outline:hover { background:var(--red-500); color:#fff; border-color:var(--red-500); }
.dms-ai-faq-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.dms-ai-faq-page .table-wrap { overflow-x:auto; }
.dms-ai-faq-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.dms-ai-faq-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.dms-ai-faq-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.dms-ai-faq-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.dms-ai-faq-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.dms-ai-faq-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.dms-ai-faq-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.dms-ai-faq-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.dms-ai-faq-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.dms-ai-faq-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.dms-ai-faq-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.dms-ai-faq-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.dms-ai-faq-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.dms-ai-faq-page .badge.amber .dot { background:var(--amber-500); }
.dms-ai-faq-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.dms-ai-faq-page .badge.blue .dot { background:var(--blue-500); }
.dms-ai-faq-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.dms-ai-faq-page .badge.green .dot { background:var(--green-500); }
.dms-ai-faq-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.dms-ai-faq-page .badge.red .dot { background:var(--red-500); }
.dms-ai-faq-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.dms-ai-faq-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.dms-ai-faq-page .badge.gray .dot { background:var(--ink-400); }
@media (max-width:1100px) { .dms-ai-faq-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .dms-ai-faq-page .filter-card .filter-bar { grid-template-columns:1fr; } .dms-ai-faq-page .toolbar { flex-wrap:wrap; gap:10px; } }
.dms-ai-faq-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.dms-ai-faq-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.dms-ai-faq-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.dms-ai-faq-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.dms-ai-faq-page .pagination-container :deep(.el-pagination .btn-prev), .dms-ai-faq-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.dms-ai-faq-page .pagination-container :deep(.el-pagination .btn-prev:hover), .dms-ai-faq-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.dms-ai-faq-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
</style>
