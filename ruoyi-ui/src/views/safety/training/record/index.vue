<template>
  <div class="app-container safety-training-record-page">
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
          <label>培训日期</label>
          <div class="control is-select">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:training:record:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:training:record:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_training_record_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="recordList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="关联计划" key="planName" :width="colWidth('planName', 192)" resizable show-overflow-tooltip v-if="columns.planName.visible">
            <template #default="scope">
              <span v-if="scope.row.planId">{{ getPlanName(scope.row.planId) }}</span>
              <span v-else style="color:var(--ink-400)">—</span>
            </template>
          </el-table-column>
          <el-table-column label="课程名称" prop="courseName" key="courseName" :width="colWidth('courseName', 240)" resizable show-overflow-tooltip v-if="columns.courseName.visible" />
          <el-table-column label="课程类别" prop="courseType" key="courseType" :width="colWidth('courseType', 132)" resizable align="center" v-if="columns.courseType.visible">
            <template #default="scope"><span class="badge violet">{{ courseTypeLabel(scope.row.courseType) }}</span></template>
          </el-table-column>
          <el-table-column label="培训日期" prop="trainingDate" key="trainingDate" :width="colWidth('trainingDate', 144)" resizable align="center" sortable="custom" v-if="columns.trainingDate.visible" />
          <el-table-column label="培训地点" prop="trainingLocation" key="trainingLocation" :width="colWidth('trainingLocation', 180)" resizable show-overflow-tooltip v-if="columns.trainingLocation.visible" />
          <el-table-column label="课时" prop="hours" key="hours" :width="colWidth('hours', 96)" resizable align="center" v-if="columns.hours.visible" />
          <el-table-column label="培训讲师" prop="trainer" key="trainer" :width="colWidth('trainer', 144)" resizable show-overflow-tooltip v-if="columns.trainer.visible" />
          <el-table-column label="参训人数" key="attendeeCount" :width="colWidth('attendeeCount', 108)" resizable align="center" v-if="columns.attendeeCount.visible">
            <template #default="scope">
              <span>{{ scope.row.attendeeCount || 0 }} 人</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:training:record:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:training:record:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:training:record:remove']">删除</el-button>
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
      <el-form ref="recordRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <div v-if="linkedPlanName" class="plan-linked-banner">
            <el-icon><Document /></el-icon>
            <span>关联培训计划：<strong>{{ linkedPlanName }}</strong></span>
          </div>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg></span>课程信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-form-item label="关联计划" prop="planId">
                <el-select v-model="form.planId" placeholder="选择关联培训计划（可选）" filterable clearable style="width: 100%" @change="onPlanChange">
                  <el-option v-for="item in planOptions" :key="item.planId" :label="item.planName" :value="item.planId">
                    <span style="float: left">{{ item.planName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px">{{ item.planStatus === '1' ? '执行中' : '已完成' }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="选择课程" prop="courseId">
                <el-select v-model="form.courseId" placeholder="请选择课程（自动回填信息）" filterable clearable style="width: 100%" @change="onCourseChange">
                  <el-option v-for="item in courseOptions" :key="item.courseId" :label="item.courseName" :value="item.courseId">
                    <span style="float: left">{{ item.courseName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 12px">{{ courseTypeLabel(item.courseType) }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="课程名称" prop="courseName"><el-input v-model="form.courseName" placeholder="请输入课程名称" /></el-form-item>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="课程类别" prop="courseType"><el-select v-model="form.courseType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_course_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="培训日期" prop="trainingDate"><el-date-picker v-model="form.trainingDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="课时" prop="hours"><el-input-number v-model="form.hours" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="16"><el-form-item label="培训地点" prop="trainingLocation"><el-input v-model="form.trainingLocation" placeholder="请输入培训地点" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>培训讲师</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="培训讲师" prop="trainer">
                <el-input v-model="form.trainer" placeholder="请输入培训讲师姓名" style="width: 100%" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1b')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>参训人员</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1b }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1b">
              <el-form-item label="参训人员" prop="attendeeUsers">
                <div class="attendee-picker-wrap">
                  <div class="attendee-tags" v-if="form.attendeeUsers && form.attendeeUsers.length">
                    <el-tag v-for="user in form.attendeeUsers" :key="user.userId" closable :disable-transitions="false" @close="removeAttendee(user)" style="margin: 2px">{{ user.nickName }}</el-tag>
                  </div>
                  <div class="attendee-actions">
                    <el-button type="primary" plain icon="User" @click="openAttendeePicker">选择参训人员</el-button>
                    <el-button v-if="form.attendeeUsers && form.attendeeUsers.length" link type="danger" icon="Delete" @click="clearAttendees">清空</el-button>
                  </div>
                </div>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="附件" prop="attachment"><file-upload v-model="form.attachment" /></el-form-item>
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
          <span class="rd-detail-header-title">培训记录详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title">课程信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">关联计划</span><div class="rd-value">{{ viewData.planName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">课程名称</span><div class="rd-value">{{ viewData.courseName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">课程类别</span><div class="rd-value"><dict-tag :options="safety_course_type" :value="viewData.courseType" /></div></div>
              <div class="rd-item"><span class="rd-label">培训日期</span><div class="rd-value">{{ viewData.trainingDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">课时</span><div class="rd-value">{{ viewData.hours != null ? viewData.hours + ' 学时' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">培训地点</span><div class="rd-value">{{ viewData.trainingLocation || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.trainer">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title">培训讲师</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">培训讲师</span><div class="rd-value">{{ viewData.trainer || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.attendeeNames">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title">参训人员</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">参训人员</span><div class="rd-value">{{ viewData.attendeeNames || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.attachment || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title">附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full" v-if="viewData.attachment"><span class="rd-label">附件</span><div class="rd-value"><div class="rd-file-links" v-if="viewData.attachment">
              <div class="rd-file-link" v-for="(url, idx) in String(viewData.attachment).split(',')" :key="idx">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                <span class="rd-file-name" @click="handleFilePreview(url)">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
                <a :href="baseUrl + url" target="_blank" class="rd-file-dl">下载</a>
              </div>
            </div></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>


    <!-- 参训人员选择弹窗（多选） -->
    <user-picker ref="attendeePickerRef" title="选择参训人员" :multiple="true" @confirm="onAttendeePickerConfirm" />
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="SafetyTrainingRecord">
import { listTrainingRecord, getTrainingRecord, addTrainingRecord, updateTrainingRecord, delTrainingRecord } from '@/api/safety/trainingRecord'
import { listTrainingCourse } from '@/api/safety/trainingCourse'
import { listTrainingPlan } from '@/api/safety/trainingPlan'
import { batchAddTrainingAttendee, listTrainingAttendee, delTrainingAttendee } from '@/api/safety/trainingAttendee'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, Document, User } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import FilePreview from '@/components/FilePreview/index.vue'

const route = useRoute()
const { proxy } = getCurrentInstance()
const { safety_course_type } = proxy.useDict('safety_course_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_training_record_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c1b","c2","vc0","vc1","vc2","vc3"])

const recordList = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
const courseOptions = ref([])
const planOptions = ref([])
const linkedPlanName = ref('')
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
const dateRange = ref([])

// ===== 参训人员相关 =====
const removedAttendeeIds = ref([])

const default_columns = {
  planName: { label: '关联计划', visible: true },
  courseName: { label: '课程名称', visible: true },
  courseType: { label: '课程类别', visible: true },
  trainingDate: { label: '培训日期', visible: true },
  trainingLocation: { label: '培训地点', visible: true },
  hours: { label: '课时', visible: true },
  trainer: { label: '培训讲师', visible: true },
  attendeeCount: { label: '参训人数', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_training_record_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, courseName: undefined, courseType: undefined, planId: undefined, params: {} },
  rules: {
    courseName: [{ required: true, message: '课程名称不能为空', trigger: 'blur' }],
    courseType: [{ required: true, message: '课程类别不能为空', trigger: 'change' }],
    trainingDate: [{ required: true, message: '培训日期不能为空', trigger: 'change' }],
    trainingLocation: [{ required: true, message: '培训地点不能为空', trigger: 'blur' }],
    hours: [{ required: true, message: '课时不能为空', trigger: 'blur' }],
    trainer: [{ required: true, message: '培训讲师不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.courseName) count++
  if (queryParams.value.courseType) count++
  if (queryParams.value.planId) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

/** 加载课程选项 */
function loadCourseOptions() {
  listTrainingCourse({ pageSize: 1000, status: '0' }).then(response => {
    courseOptions.value = response.rows || []
  })
}

/** 加载培训计划选项（仅执行中/已完成的计划可关联） */
function loadPlanOptions() {
  listTrainingPlan({ pageSize: 1000 }).then(response => {
    planOptions.value = (response.rows || []).filter(p => p.planStatus === '1' || p.planStatus === '2')
  })
}

/** 选择关联计划 */
function onPlanChange(planId) {
  if (planId) {
    const plan = planOptions.value.find(p => p.planId === planId)
    if (plan) {
      linkedPlanName.value = plan.planName
    }
  } else {
    linkedPlanName.value = ''
  }
}

/** 选择课程后自动回填 */
function onCourseChange(courseId) {
  if (courseId) {
    const course = courseOptions.value.find(c => c.courseId === courseId)
    if (course) {
      form.value.courseName = course.courseName
      form.value.courseType = course.courseType
      if (course.hours) form.value.hours = course.hours
      if (!form.value.trainer && course.instructor) form.value.trainer = course.instructor
    }
  }
}

function handleFilePreview(url) {
  const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
  proxy.$refs.filePreviewRef.open(url, name)
}

function getList() {
  loading.value = true
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.value.params.beginTime = dateRange.value[0]
    queryParams.value.params.endTime = dateRange.value[1]
  } else {
    queryParams.value.params.beginTime = undefined
    queryParams.value.params.endTime = undefined
  }
  listTrainingRecord(queryParams.value).then(response => {
    recordList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.courseName = undefined; queryParams.value.courseType = undefined; queryParams.value.planId = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.recordId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() {
  reset()
  collapsedCards.c0 = false
  collapsedCards.c1 = false
  collapsedCards.c1b = false
  collapsedCards.c2 = false
  // 如果是从计划页面跳转来的，自动填充 planId
  if (route.query.planId) {
    form.value.planId = Number(route.query.planId)
    linkedPlanName.value = route.query.planName || ''
  }
  open.value = true
  title.value = '添加培训记录'
}
function handleUpdate(row) {
  reset()
  getTrainingRecord(row.recordId || ids.value[0]).then(response => {
    form.value = response.data
    form.value.attendeeUsers = []
    if (form.value.planId) {
      const plan = planOptions.value.find(p => p.planId === form.value.planId)
      linkedPlanName.value = plan ? plan.planName : ''
    }
    collapsedCards.c1 = !response.data.trainer
    collapsedCards.c1b = false
    collapsedCards.c2 = !response.data.attachment && !response.data.remark
    // 加载已关联的参训人员
    loadRecordAttendees(row.recordId || ids.value[0])
    open.value = true
    title.value = '修改培训记录'
  })
}
function handleView(row) { const id = row.recordId || ids.value[0]; getTrainingRecord(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['recordRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) {
        updateTrainingRecord(form.value).then(() => {
          saveAttendees(form.value.recordId, () => {
            proxy.$modal.msgSuccess('修改成功'); open.value = false; getList()
          })
        })
      } else {
        addTrainingRecord(form.value).then(response => {
          const newRecordId = response.recordId || response.data?.recordId
          saveAttendees(newRecordId, () => {
            proxy.$modal.msgSuccess('新增成功'); open.value = false; getList()
          })
        })
      }
    }
  })
}
function handleDelete(row) { const recordIds = row.recordId || ids.value; proxy.$modal.confirm('是否确认删除培训记录？').then(function() { return delTrainingRecord(recordIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { recordId: undefined, planId: undefined, courseId: undefined, trainerId: undefined, courseName: undefined, courseType: undefined, trainingDate: undefined, trainingLocation: undefined, hours: undefined, trainer: undefined, attachment: undefined, remark: undefined, attendeeUsers: [] }
  linkedPlanName.value = ''
  removedAttendeeIds.value = []
  proxy.resetForm('recordRef')
}

/** 加载培训记录已关联的参训人员 */
function loadRecordAttendees(recordId) {
  listTrainingAttendee({ recordId: recordId, pageSize: 9999 }).then(response => {
    form.value.attendeeUsers = (response.rows || []).map(a => ({
      userId: a.userId,
      nickName: a.userName,
      deptId: a.deptId,
      deptName: a.deptName,
      attendeeId: a.attendeeId
    }))
    collapsedCards.c1b = !form.value.attendeeUsers.length
  })
}

/** 保存参训人员（新增 + 删除） */
function saveAttendees(recordId, callback) {
  const attendeeUsers = form.value.attendeeUsers || []
  let pending = 0
  let done = false
  function finish() {
    if (done) return
    pending--
    if (pending <= 0) { done = true; callback && callback() }
  }
  // 1) 删除已移除的参训人员
  if (removedAttendeeIds.value.length > 0) {
    pending++
    delTrainingAttendee(removedAttendeeIds.value.join(',')).then(() => finish()).catch(() => finish())
  }
  // 2) 批量添加选中的参训人员（后端会自动跳过已存在的）
  if (attendeeUsers.length > 0) {
    pending++
    const attendeeList = attendeeUsers.map(user => ({
      recordId: recordId,
      userId: user.userId,
      userName: user.nickName,
      deptId: user.deptId,
      deptName: user.deptName || '',
      isPass: '1'
    }))
    batchAddTrainingAttendee(attendeeList).then(() => finish()).catch(() => finish())
  }
  // 如果没有参训人员操作，直接回调
  if (pending === 0) { done = true; callback && callback() }
}

/** 打开参训人员选择弹窗（多选） */
function openAttendeePicker() {
  const currentIds = form.value.attendeeUsers ? form.value.attendeeUsers.map(u => u.userId) : []
  proxy.$refs.attendeePickerRef.open(currentIds)
}
/** 参训人员选择确认回调（多选） */
function onAttendeePickerConfirm(users) {
  // 保留已有 attendeeId 的用户信息，合并新选择的用户
  const existingMap = new Map()
  ;(form.value.attendeeUsers || []).forEach(u => existingMap.set(u.userId, u))
  form.value.attendeeUsers = users.map(u => {
    const existing = existingMap.get(u.userId)
    return existing ? { ...existing, ...u } : u
  })
}
/** 移除单个参训人员 */
function removeAttendee(user) {
  if (user.attendeeId) {
    removedAttendeeIds.value.push(user.attendeeId)
  }
  form.value.attendeeUsers = (form.value.attendeeUsers || []).filter(u => u.userId !== user.userId)
}
/** 清空所有参训人员 */
function clearAttendees() {
  ;(form.value.attendeeUsers || []).forEach(u => {
    if (u.attendeeId) removedAttendeeIds.value.push(u.attendeeId)
  })
  form.value.attendeeUsers = []
}

function courseTypeLabel(type) { const item = safety_course_type.value.find(d => d.value == type); return item ? item.label : '-' }
function getPlanName(planId) { const plan = planOptions.value.find(p => p.planId === planId); return plan ? plan.planName : '#' + planId }

loadCourseOptions()
loadPlanOptions()

// 如果从计划页面跳转来，自动加载该计划的记录
if (route.query.planId) {
  queryParams.value.planId = Number(route.query.planId)
}
getList()
</script>

<style scoped>
.safety-training-record-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-training-record-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-training-record-page .filter-card { padding:14px 20px 16px; }
.safety-training-record-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-training-record-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-record-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-training-record-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-training-record-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-training-record-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-training-record-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-training-record-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-training-record-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-training-record-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-training-record-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-training-record-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-training-record-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-training-record-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-training-record-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-training-record-page .field .control :deep(.el-select) { width:100%; }
.safety-training-record-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-training-record-page .field .control.is-select { padding:0; }
.safety-training-record-page .field .control.is-select :deep(.el-select) { width:100%; }
.safety-training-record-page .field .control.is-select :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0 12px; min-height:34px; height:34px; }
.safety-training-record-page .field .control.is-select :deep(.el-date-editor) { width:100%; }
.safety-training-record-page .field .control.is-select :deep(.el-range-editor) { width:100%; }
.safety-training-record-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-training-record-page .toolbar .left, .safety-training-record-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-training-record-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-training-record-page .table-wrap { overflow-x:auto; }
.safety-training-record-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-training-record-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-training-record-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-training-record-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-training-record-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-training-record-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-training-record-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-training-record-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-training-record-page .badge.green .dot { background:var(--green-500); }
.safety-training-record-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-training-record-page .badge.red .dot { background:var(--red-500); }
.safety-training-record-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-training-record-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-training-record-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-training-record-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
.attendee-picker-wrap { display: flex; flex-direction: column; gap: 8px; width: 100%; }
.attendee-tags { display: flex; flex-wrap: wrap; gap: 4px; padding: 8px 12px; background: var(--ink-50); border: 1px dashed var(--ink-200); border-radius: var(--r-sm); min-height: 40px; align-items: center; }
.attendee-actions { display: flex; align-items: center; gap: 8px; }
.plan-linked-banner { display: flex; align-items: center; gap: 6px; padding: 8px 16px; background: var(--brand-50); border: 1px solid var(--brand-200); border-radius: 8px; margin-bottom: 12px; font-size: 13px; color: var(--brand-700); }
.safety-training-record-page .plan-linked-banner { display:flex; align-items:center; gap:8px; padding:10px 16px; background:var(--blue-50); border:1px solid #bfdbfe; border-radius:var(--r-sm); margin-bottom:12px; font-size:13px; color:var(--blue-700); }
.safety-training-record-page .plan-linked-banner strong { font-weight:600; }
</style>
