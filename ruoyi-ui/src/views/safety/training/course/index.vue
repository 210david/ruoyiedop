<template>
  <div class="app-container safety-training-course-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>课程名称</label>
          <div class="control">
            <el-input v-model="queryParams.courseName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>课程类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.courseType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_course_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>课程形式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.courseForm" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_course_form" :key="dict.value" :label="dict.label" :value="dict.value" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:training:course:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:training:course:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_training_course_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="courseList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="课程编号" prop="courseCode" key="courseCode" :width="colWidth('courseCode', 156)" resizable v-if="columns.courseCode.visible" />
          <el-table-column label="课程名称" prop="courseName" key="courseName" :width="colWidth('courseName', 240)" resizable show-overflow-tooltip v-if="columns.courseName.visible" />
          <el-table-column label="课程类别" prop="courseType" key="courseType" :width="colWidth('courseType', 132)" resizable align="center" v-if="columns.courseType.visible">
            <template #default="scope"><span class="badge violet">{{ courseTypeLabel(scope.row.courseType) }}</span></template>
          </el-table-column>
          <el-table-column label="课程形式" prop="courseForm" key="courseForm" :width="colWidth('courseForm', 120)" resizable align="center" v-if="columns.courseForm.visible">
            <template #default="scope">
              <span class="badge" :class="courseFormBadgeClass(scope.row.courseForm)">{{ courseFormLabel(scope.row.courseForm) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="标准课时" prop="hours" key="hours" :width="colWidth('hours', 120)" resizable align="center" v-if="columns.hours.visible">
            <template #default="scope">{{ scope.row.hours != null ? scope.row.hours + ' 学时' : '-' }}</template>
          </el-table-column>
          <el-table-column label="讲师" prop="instructor" key="instructor" :width="colWidth('instructor', 144)" resizable show-overflow-tooltip v-if="columns.instructor.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 96)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'red'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="216" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:training:course:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:training:course:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:training:course:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="courseRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></span>课程信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="课程编号" prop="courseCode"><el-input v-model="form.courseCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="课程名称" prop="courseName"><el-input v-model="form.courseName" placeholder="请输入课程名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="课程类别" prop="courseType"><el-select v-model="form.courseType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_course_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="课程形式" prop="courseForm"><el-select v-model="form.courseForm" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_course_form" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="标准课时" prop="hours"><div style="display: flex; align-items: center; width: 100%;"><el-input-number v-model="form.hours" :precision="2" :min="0" :step="0.5" style="flex: 1" /><span style="margin-left: 8px; font-size: 14px; color: #94a3b8; white-space: nowrap;">学时</span></div></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="讲师" prop="instructor"><el-input v-model="form.instructor" placeholder="请输入讲师" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>课件与描述</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="课件附件" prop="materialUrl"><file-upload v-model="form.materialUrl" :file-size="100" /></el-form-item>
              <el-form-item label="课程描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入课程描述" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">课程详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.courseCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.courseCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title">课程信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">课程编号</span><div class="rd-value">{{ viewData.courseCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">课程名称</span><div class="rd-value">{{ viewData.courseName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">课程类别</span><div class="rd-value"><dict-tag :options="safety_course_type" :value="viewData.courseType" /></div></div>
              <div class="rd-item"><span class="rd-label">课程形式</span><div class="rd-value"><dict-tag :options="safety_course_form" :value="viewData.courseForm" /></div></div>
              <div class="rd-item"><span class="rd-label">标准课时</span><div class="rd-value">{{ viewData.hours != null ? viewData.hours + ' 学时' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">讲师</span><div class="rd-value">{{ viewData.instructor || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.materialUrl || viewData.description || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title">课件与描述</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full" v-if="viewData.materialUrl"><span class="rd-label">课件附件</span><div class="rd-value"><div class="rd-file-links" v-if="viewData.materialUrl">
              <div class="rd-file-link" v-for="(url, idx) in String(viewData.materialUrl).split(',')" :key="idx">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                <span class="rd-file-name" @click="handleFilePreview(url)">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
                <a :href="baseUrl + url" target="_blank" class="rd-file-dl">下载</a>
              </div>
            </div></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">课程描述</span><div class="rd-value">{{ viewData.description || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <file-preview ref="filePreviewRef" />

  </div>
</template>

<script setup name="SafetyTrainingCourse">
import { listTrainingCourse, getTrainingCourse, addTrainingCourse, updateTrainingCourse, delTrainingCourse } from '@/api/safety/trainingCourse'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft } from '@element-plus/icons-vue'
import FilePreview from '@/components/FilePreview/index.vue'

const { proxy } = getCurrentInstance()
const { safety_course_type, safety_course_form } = proxy.useDict('safety_course_type', 'safety_course_form')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_training_course_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])

const courseList = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})

const default_columns = {
  courseCode: { label: '课程编号', visible: true },
  courseName: { label: '课程名称', visible: true },
  courseType: { label: '课程类别', visible: true },
  courseForm: { label: '课程形式', visible: true },
  hours: { label: '标准课时', visible: true },
  instructor: { label: '讲师', visible: true },
  status: { label: '状态', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_training_course_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, courseName: undefined, courseType: undefined, courseForm: undefined, params: {} },
  rules: {
    courseName: [{ required: true, message: '课程名称不能为空', trigger: 'blur' }],
    courseType: [{ required: true, message: '课程类别不能为空', trigger: 'change' }],
    courseForm: [{ required: true, message: '课程形式不能为空', trigger: 'change' }],
    hours: [{ required: true, message: '标准课时不能为空', trigger: 'blur' }],
    instructor: [{ required: true, message: '讲师不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.courseName) count++
  if (queryParams.value.courseType) count++
  if (queryParams.value.courseForm) count++
  return count
})

function handleFilePreview(url) {
  const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
  proxy.$refs.filePreviewRef.open(url, name)
}

function getList() { loading.value = true; listTrainingCourse(queryParams.value).then(response => { courseList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.courseName = undefined; queryParams.value.courseType = undefined; queryParams.value.courseForm = undefined; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.courseId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; open.value = true; title.value = '添加课程' }
function handleUpdate(row) { reset(); getTrainingCourse(row.courseId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.description && !response.data.remark; open.value = true; title.value = '修改课程' }) }
function handleView(row) { const id = row.courseId || ids.value[0]; getTrainingCourse(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['courseRef'].validate(valid => {
    if (valid) {
      if (form.value.courseId != undefined) { updateTrainingCourse(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addTrainingCourse(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const courseIds = row.courseId || ids.value; proxy.$modal.confirm('是否确认删除课程？').then(function() { return delTrainingCourse(courseIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { courseId: undefined, courseCode: undefined, courseName: undefined, courseType: undefined, courseForm: undefined, hours: undefined, instructor: undefined, description: undefined, materialUrl: undefined, status: '0', remark: undefined }
  proxy.resetForm('courseRef')
}

function courseTypeLabel(type) { const item = safety_course_type.value.find(d => d.value == type); return item ? item.label : '-' }
function courseFormLabel(form) { const item = safety_course_form.value.find(d => d.value == form); return item ? item.label : '-' }
function courseFormBadgeClass(form) { const map = { '1': 'violet', '2': 'green', '3': 'blue' }; return map[form] || 'gray' }

getList()
</script>

<style scoped>
.safety-training-course-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-training-course-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-training-course-page .filter-card { padding:14px 20px 16px; }
.safety-training-course-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-training-course-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-course-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-training-course-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-training-course-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-training-course-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-training-course-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-training-course-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-training-course-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-training-course-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-training-course-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-training-course-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-training-course-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-training-course-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-training-course-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-training-course-page .field .control :deep(.el-select) { width:100%; }
.safety-training-course-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-training-course-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-training-course-page .toolbar .left, .safety-training-course-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-training-course-page .table-wrap { overflow-x:auto; }
.safety-training-course-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-training-course-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-training-course-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-training-course-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-training-course-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-training-course-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-training-course-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-training-course-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-training-course-page .badge.green .dot { background:var(--green-500); }
.safety-training-course-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-training-course-page .badge.red .dot { background:var(--red-500); }
.safety-training-course-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-training-course-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-training-course-page .badge.blue .dot { background:var(--blue-500); }
.safety-training-course-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-training-course-page .badge.gray .dot { background:var(--ink-400); }
.safety-training-course-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-training-course-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-training-course-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>
