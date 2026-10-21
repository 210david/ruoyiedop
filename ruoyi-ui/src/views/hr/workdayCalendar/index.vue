<template>
  <div class="app-container hr-workday-calendar-page">
    <!-- 筛选条件 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>年份</label>
          <div class="control is-select">
            <el-select v-model="queryParams.year" placeholder="选择年份" clearable @change="handleQuery">
              <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>月份</label>
          <div class="control is-select">
            <el-select v-model="queryParams.month" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>日期类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.dateType" placeholder="全部" clearable @change="handleQuery">
              <el-option label="工作日" value="0" />
              <el-option label="休息日" value="1" />
              <el-option label="法定节假日" value="2" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>节假日名称</label>
          <div class="control">
            <el-input v-model="queryParams.holidayName" placeholder="请输入节假日名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 日历视图 -->
    <div class="surface">
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTypeTab === 'all' }" @click="handleTypeTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ typeCounts.all }}</span>
          </button>
          <button class="status-tab" :class="['tab-green', { 'is-active': activeTypeTab === '0' }]" @click="handleTypeTabClick('0')">
            <span class="dot"></span><span>工作日</span><span class="count">{{ typeCounts['0'] || 0 }}</span>
          </button>
          <button class="status-tab" :class="['tab-void', { 'is-active': activeTypeTab === '1' }]" @click="handleTypeTabClick('1')">
            <span class="dot"></span><span>休息日</span><span class="count">{{ typeCounts['1'] || 0 }}</span>
          </button>
          <button class="status-tab" :class="['tab-amber', { 'is-active': activeTypeTab === '2' }]" @click="handleTypeTabClick('2')">
            <span class="dot"></span><span>节假日</span><span class="count">{{ typeCounts['2'] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Calendar" @click="handleGenerate" v-hasPermi="['hr:workdayCalendar:generate']">生成年度日历</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['hr:workdayCalendar:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <!-- 年度日历网格 -->
      <div class="calendar-wrap" v-loading="loading">
        <div class="calendar-header">
          <span class="calendar-year">{{ currentYear }}年 工作日历</span>
          <span class="calendar-stats">共 {{ calendarData.length }} 天 | 工作日 {{ typeCounts['0'] || 0 }} 天 | 休息日 {{ typeCounts['1'] || 0 }} 天 | 节假日 {{ typeCounts['2'] || 0 }} 天</span>
        </div>
        <div class="calendar-grid" v-if="calendarData.length > 0">
          <div class="month-card" v-for="m in 12" :key="m">
            <div class="month-header">{{ m }}月</div>
            <div class="month-weekdays">
              <span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span><span>日</span>
            </div>
            <div class="month-days">
              <div
                v-for="(day, idx) in getMonthDays(m)"
                :key="idx"
                class="day-cell"
                :class="getDayCellClass(day)"
                @click="day && day.calendarId ? handleEditDay(day) : null"
              >
                <template v-if="day">
                  <span class="day-num">{{ day.dayNum }}</span>
                  <span class="day-label" v-if="day.dateType === '2'">{{ day.holidayName || '节假日' }}</span>
                  <span class="day-label" v-else-if="day.dateType === '1'">休息</span>
                  <span class="day-label" v-else-if="day.dateType === '0'">{{ day.shiftName || '工作' }}</span>
                </template>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无日历数据，请点击「生成年度日历」按钮生成" />
      </div>
    </div>

    <!-- ===== 生成年度日历弹框 ===== -->
    <el-dialog v-model="generateOpen" width="864px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></div>
          <div class="rd-detail-header-main">
            <span class="rd-detail-header-title">生成年度工作日历</span>
          </div>
        </div>
      </template>
      <el-form ref="generateRef" :model="generateForm" :rules="generateRules" label-width="120px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('g0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.g0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.g0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="年份" prop="year">
                    <el-input-number v-model="generateForm.year" :min="2020" :max="2050" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="周末是否休息" prop="weekendRest">
                    <el-switch v-model="generateForm.weekendRest" active-text="周六日休息" inactive-text="全工作日" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 班次配置 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('g1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>默认班次配置</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.g1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.g1">
              <div class="rd-section-desc">为生成的工作日关联默认班次，后续可在日历中逐日调整。</div>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="默认班次" prop="shiftId">
                    <el-select v-model="generateForm.shiftId" placeholder="选择班次（可不选）" clearable style="width: 100%" @change="onGenerateShiftChange">
                      <el-option v-for="s in shiftOptions" :key="s.shiftId" :label="s.shiftName" :value="s.shiftId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 提示信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('g2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>生成说明</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.g2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.g2">
              <div class="rd-section-desc">
                <p>1. 生成日历将创建所选年份全年365/366天的日历记录</p>
                <p>2. 若该年份已有日历数据，将被<strong style="color:#ef4444">覆盖删除后重新生成</strong></p>
                <p>3. 周末设为"休息日"，周一至周五设为"工作日"</p>
                <p>4. 生成后可在日历网格中点击日期逐日调整类型和班次</p>
                <p>5. 法定节假日需手动在日历中标记，系统不会自动识别</p>
              </div>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitGenerate" :loading="generateLoading">确 定 生 成</el-button>
        <el-button @click="generateOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 日期编辑弹框 ===== -->
    <el-dialog v-model="editOpen" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></div>
          <div class="rd-detail-header-main">
            <span class="rd-detail-header-title">编辑日期</span>
            <div class="rd-detail-header-sub" v-if="editForm.calendarDate">
              <span class="rd-detail-header-no">{{ formatDate(editForm.calendarDate) }} {{ formatWeekday(editForm.weekDay) }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="100px">
        <div class="rd-page">
          <!-- 日期信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d0">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="日期类型" prop="dateType">
                    <el-radio-group v-model="editForm.dateType">
                      <el-radio label="0">工作日</el-radio>
                      <el-radio label="1">休息日</el-radio>
                      <el-radio label="2">法定节假日</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 班次关联 -->
          <section class="rd-card" v-if="editForm.dateType === '0'">
            <div class="rd-card-header" @click="toggleCard('d1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>班次关联</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d1">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="关联班次" prop="shiftId">
                    <el-select v-model="editForm.shiftId" placeholder="选择班次（可不选）" clearable style="width: 100%" @change="onEditShiftChange">
                      <el-option v-for="s in shiftOptions" :key="s.shiftId" :label="s.shiftName" :value="s.shiftId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 节假日信息 -->
          <section class="rd-card" v-if="editForm.dateType === '2'">
            <div class="rd-card-header" @click="toggleCard('d2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>节假日信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d2">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="节假日名称" prop="holidayName">
                    <el-input v-model="editForm.holidayName" placeholder="请输入节假日名称（如：元旦、春节、国庆节等）" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>

          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('d3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.d3">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="editForm.remark" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="请输入备注" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
        <el-button @click="editOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 业务操作说明 -->
    <el-dialog v-model="showStatusHelp" title="工作日期维护业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、功能概述</h4>
        <div class="highlight-card highlight-info">
          <div class="highlight-card-title">什么是工作日期维护？</div>
          <div class="highlight-card-body">
            <strong>工作日期维护</strong>用于管理企业运营日历，标记每个日期是工作日、休息日还是法定节假日。排班管理和考勤统计依赖此日历数据来判断某天是否需要出勤。
          </div>
        </div>

        <h4>二、日期类型说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工作日"><el-tag type="success" size="small">工作日</el-tag> 需要出勤的日期，可关联班次</el-descriptions-item>
          <el-descriptions-item label="休息日"><el-tag type="info" size="small">休息日</el-tag> 不需要出勤的日期（如周末）</el-descriptions-item>
          <el-descriptions-item label="法定节假日"><el-tag type="warning" size="small">法定节假日</el-tag> 国家法定假日，需手动标记节假日名称</el-descriptions-item>
        </el-descriptions>

        <h4>三、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>生成年度日历：</strong>点击「生成年度日历」按钮，选择年份和生成选项，系统自动生成全年365/366天的日历记录
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>标记节假日：</strong>在日历网格中点击日期，将法定节假日（如春节、国庆节等）的日期类型改为"法定节假日"并填写节假日名称
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>关联班次：</strong>工作日可关联具体班次，休息日和节假日不需要关联班次
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>调整休息日：</strong>如需调休（如将周六设为工作日），点击对应日期修改类型即可
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>重新生成：</strong>如需重置某年日历，重新点击「生成年度日历」选择同一年份即可覆盖
          </el-timeline-item>
        </el-timeline>

        <h4>四、注意事项</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">使用提示</div>
          <div class="highlight-card-body">
            <p>• 生成年度日历会覆盖该年份已有的日历数据，请谨慎操作</p>
            <p>• 法定节假日需手动标记，系统不会自动识别国家假日安排</p>
            <p>• 建议在每年年初或年末生成次年的工作日历</p>
            <p>• 日历数据直接影响排班管理和考勤统计，修改前请确认</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HrWorkdayCalendar">
import { listHrWorkdayCalendar, updateHrWorkdayCalendar, generateYearlyCalendar } from '@/api/hr/hrWorkdayCalendar'
import { listHrShift } from '@/api/hr/hrShift'
import { useDetailCard } from '@/composables/useDetailCard'
import { Filter, Search, RefreshLeft, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

/** 详情弹窗卡片折叠 */
const { collapsedCards, toggleCard } = useDetailCard(['g0', 'g1', 'g2', 'd0', 'd1', 'd2', 'd3'])

const calendarData = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showStatusHelp = ref(false)
const generateOpen = ref(false)
const editOpen = ref(false)
const generateLoading = ref(false)
const shiftOptions = ref([])
const activeTypeTab = ref('all')
const currentYear = ref(new Date().getFullYear())

const yearOptions = computed(() => {
  const base = new Date().getFullYear()
  const arr = []
  for (let y = base - 2; y <= base + 5; y++) arr.push(y)
  return arr
})

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 9999, year: new Date().getFullYear(), month: undefined, dateType: undefined, holidayName: undefined },
  generateForm: { year: new Date().getFullYear() + 1, weekendRest: true, shiftId: undefined, shiftName: undefined },
  editForm: {},
  generateRules: {
    year: [{ required: true, message: '年份不能为空', trigger: 'blur' }]
  },
  editRules: {
    dateType: [{ required: true, message: '请选择日期类型', trigger: 'change' }]
  }
})
const { queryParams, generateForm, editForm, generateRules, editRules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.year) count++
  if (queryParams.value.month) count++
  if (queryParams.value.dateType) count++
  if (queryParams.value.holidayName) count++
  return count
})

const typeCounts = computed(() => {
  const counts = { all: calendarData.value.length }
  calendarData.value.forEach(item => {
    const key = item.dateType || '0'
    counts[key] = (counts[key] || 0) + 1
  })
  return counts
})

/** 获取列表数据（不分页，取全部） */
function getList() {
  loading.value = true
  listHrWorkdayCalendar(queryParams.value).then(res => {
    calendarData.value = (res.rows || []).map(item => {
      const date = new Date(item.calendarDate)
      return { ...item, dayNum: date.getDate(), _month: date.getMonth() + 1 }
    })
    currentYear.value = queryParams.value.year || new Date().getFullYear()
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  activeTypeTab.value = 'all'
  getList()
}

function resetQuery() {
  queryParams.value.year = new Date().getFullYear()
  queryParams.value.month = undefined
  queryParams.value.dateType = undefined
  queryParams.value.holidayName = undefined
  handleQuery()
}

function handleTypeTabClick(tab) {
  activeTypeTab.value = tab
  if (tab === 'all') {
    queryParams.value.dateType = undefined
  } else {
    queryParams.value.dateType = tab
  }
  getList()
}

function loadShiftOptions() {
  listHrShift({ pageNum: 1, pageSize: 100 }).then(res => { shiftOptions.value = res.rows || [] })
}

/** 获取某月的日历天数（含前置空位） */
function getMonthDays(month) {
  const monthItems = calendarData.value.filter(d => d._month === month)
  if (monthItems.length === 0) return []
  const firstDay = monthItems[0]
  // 前置空位：weekDay 1=周一，空位数 = weekDay - 1
  const blanks = new Array((firstDay.weekDay || 1) - 1).fill(null)
  // 按天号排序
  const sorted = monthItems.sort((a, b) => a.dayNum - b.dayNum)
  return [...blanks, ...sorted]
}

function getDayCellClass(day) {
  if (!day) return 'day-empty'
  if (day.dateType === '2') return 'day-holiday'
  if (day.dateType === '1') return 'day-rest'
  return 'day-work'
}

function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatWeekday(weekDay) {
  const names = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return names[weekDay] || ''
}

/** 生成年度日历 */
function handleGenerate() {
  generateForm.value = { year: currentYear.value + 1, weekendRest: true, shiftId: undefined, shiftName: undefined }
  generateOpen.value = true
}

function onGenerateShiftChange(val) {
  const s = shiftOptions.value.find(s => s.shiftId === val)
  generateForm.value.shiftName = s ? s.shiftName : undefined
}

function submitGenerate() {
  proxy.$refs['generateRef'].validate(valid => {
    if (valid) {
      proxy.$modal.confirm(`确定要生成 ${generateForm.value.year} 年的年度工作日历吗？如该年份已有数据将被覆盖删除后重新生成。`).then(() => {
        generateLoading.value = true
        generateYearlyCalendar({
          year: generateForm.value.year,
          weekendRest: generateForm.value.weekendRest,
          shiftId: generateForm.value.shiftId,
          shiftName: generateForm.value.shiftName
        }).then(res => {
          proxy.$modal.msgSuccess(`成功生成 ${res.data || res.msg} 条日历记录`)
          generateOpen.value = false
          generateLoading.value = false
          queryParams.value.year = generateForm.value.year
          getList()
        }).catch(() => { generateLoading.value = false })
      }).catch(() => {})
    }
  })
}

/** 编辑日期 */
function handleEditDay(day) {
  editForm.value = {
    calendarId: day.calendarId,
    calendarDate: day.calendarDate,
    year: day.year,
    month: day.month,
    weekDay: day.weekDay,
    dateType: day.dateType,
    shiftId: day.shiftId,
    shiftName: day.shiftName,
    holidayName: day.holidayName,
    remark: day.remark
  }
  editOpen.value = true
}

function onEditShiftChange(val) {
  const s = shiftOptions.value.find(s => s.shiftId === val)
  editForm.value.shiftName = s ? s.shiftName : undefined
}

function submitEdit() {
  proxy.$refs['editRef'].validate(valid => {
    if (valid) {
      // 休息日和节假日清空班次
      if (editForm.value.dateType !== '0') {
        editForm.value.shiftId = null
        editForm.value.shiftName = null
      }
      // 非节假日清空节假日名称
      if (editForm.value.dateType !== '2') {
        editForm.value.holidayName = null
      }
      updateHrWorkdayCalendar(editForm.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        editOpen.value = false
        getList()
      })
    }
  })
}

function handleExport() {
  proxy.download('hr/workdayCalendar/export', { ...queryParams.value }, `workdayCalendar_${new Date().getTime()}.xlsx`)
}

loadShiftOptions()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-workday-calendar-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.06);
  --shadow-pop:0 4px 6px -1px rgba(0,0,0,.06),0 2px 4px -2px rgba(0,0,0,.05);
}

/* ===== Surface / Card ===== */
.surface { background:#fff; border-radius:var(--r-lg); border:1px solid var(--ink-200); box-shadow:var(--shadow-card); margin-bottom:14px; }

/* ===== Filter Card ===== */
.filter-card { padding:14px 18px 10px; }
.filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; }
.filter-title { font-size:13px; font-weight:600; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.filter-title .glyph { width:4px; height:14px; background:var(--brand-500); border-radius:2px; display:inline-block; }
.filter-bar { display:flex; flex-wrap:wrap; gap:12px 20px; align-items:flex-end; }
.field { display:flex; flex-direction:column; gap:4px; }
.field label { font-size:12px; color:var(--ink-500); font-weight:500; }
.field .control { width:180px; }
.field .control.is-select { width:160px; }
.filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:12px; padding-top:10px; border-top:1px dashed var(--ink-200); }
.filter-info { font-size:12px; color:var(--ink-400); display:flex; align-items:center; gap:4px; }
.filter-buttons { display:flex; gap:8px; }

/* ===== Status Tabs ===== */
.status-tabs { display:flex; align-items:center; justify-content:space-between; padding:10px 18px; border-bottom:1px solid var(--ink-100); }
.tabs-track { display:flex; gap:6px; flex-wrap:wrap; }
.status-tab { display:inline-flex; align-items:center; gap:5px; padding:5px 12px 5px 10px; border:1px solid var(--ink-200); border-radius:20px; background:#fff; color:var(--ink-500); font-size:12px; font-weight:500; cursor:pointer; transition:all .2s; }
.status-tab:hover { border-color:var(--brand-200); color:var(--brand-600); background:var(--brand-50); }
.status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.status-tab .count { background:var(--ink-100); color:var(--ink-500); border-radius:10px; padding:1px 7px; font-size:11px; font-weight:600; }
.status-tab.is-active { background:var(--brand-50); border-color:var(--brand-200); color:var(--brand-700); }
.status-tab.is-active .dot { background:var(--brand-500); }
.status-tab.is-active .count { background:var(--brand-100); color:var(--brand-700); }
.status-tab.tab-green.is-active { background:var(--green-50); border-color:#a7f3d0; color:var(--green-700); }
.status-tab.tab-green.is-active .dot { background:var(--green-500); }
.status-tab.tab-green.is-active .count { background:#d1fae5; color:var(--green-700); }
.status-tab.tab-void.is-active { background:var(--blue-50); border-color:#93c5fd; color:var(--blue-700); }
.status-tab.tab-void.is-active .dot { background:var(--blue-500); }
.status-tab.tab-void.is-active .count { background:#dbeafe; color:var(--blue-700); }
.status-tab.tab-amber.is-active { background:var(--amber-50); border-color:#fcd34d; color:var(--amber-700); }
.status-tab.tab-amber.is-active .dot { background:var(--amber-500); }
.status-tab.tab-amber.is-active .count { background:#fef3c7; color:var(--amber-700); }
.tip-pill { display:inline-flex; align-items:center; gap:4px; border:1px solid var(--amber-500); background:var(--amber-50); color:var(--amber-700); border-radius:20px; padding:4px 12px; font-size:12px; font-weight:500; cursor:pointer; transition:all .2s; }
.tip-pill:hover { background:#fef3c7; }

/* ===== Toolbar ===== */
.toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 18px; border-bottom:1px solid var(--ink-100); }
.toolbar .left, .toolbar .right { display:flex; align-items:center; gap:8px; }

/* ===== Calendar Grid ===== */
.calendar-wrap { padding:16px 18px; }
.calendar-header { display:flex; align-items:baseline; justify-content:space-between; margin-bottom:14px; }
.calendar-year { font-size:18px; font-weight:700; color:var(--ink-900); }
.calendar-stats { font-size:13px; color:var(--ink-500); }
.calendar-grid { display:grid; grid-template-columns:repeat(4, 1fr); gap:14px; }
.month-card { border:1px solid var(--ink-200); border-radius:var(--r-md); overflow:hidden; background:var(--ink-50); }
.month-header { background:var(--brand-600); color:#fff; font-size:13px; font-weight:600; padding:5px 10px; text-align:center; }
.month-weekdays { display:grid; grid-template-columns:repeat(7,1fr); gap:1px; background:var(--ink-200); padding:1px; }
.month-weekdays span { text-align:center; font-size:11px; color:var(--ink-500); background:#fff; padding:2px 0; font-weight:500; }
.month-days { display:grid; grid-template-columns:repeat(7,1fr); gap:1px; background:var(--ink-200); padding:1px; }
.day-cell { min-height:48px; background:#fff; display:flex; flex-direction:column; align-items:center; justify-content:center; cursor:pointer; transition:all .15s; position:relative; padding:2px; }
.day-cell:hover { box-shadow:0 0 0 2px var(--brand-200) inset; }
.day-empty { background:var(--ink-50); cursor:default; }
.day-empty:hover { box-shadow:none; }
.day-work { background:var(--green-50); }
.day-rest { background:var(--blue-50); }
.day-holiday { background:var(--amber-50); }
.day-num { font-size:13px; font-weight:600; color:var(--ink-700); }
.day-label { font-size:10px; color:var(--ink-400); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:100%; }
.day-holiday .day-label { color:var(--amber-700); font-weight:500; }
.day-rest .day-label { color:var(--blue-700); }
.day-work .day-label { color:var(--green-700); }

/* ===== rd-section-desc（页面特有，全局无此样式） ===== */
.rd-section-desc { font-size:12px; color:var(--ink-500); margin-bottom:10px; line-height:1.6; }
.rd-section-desc p { margin:0 0 4px; }

/* ===== Status Help ===== */
.status-help-content { padding:10px 4px; }
.status-help-content h4 { margin:18px 0 10px; font-size:14px; font-weight:700; color:var(--ink-900); }
.status-help-content h4:first-child { margin-top:0; }
.highlight-card { border-radius:var(--r-md); padding:12px 16px; margin-bottom:10px; border:1px solid; }
.highlight-info { background:var(--blue-50); border-color:#bfdbfe; }
.highlight-warning { background:var(--amber-50); border-color:#fde68a; }
.highlight-card-title { font-size:13px; font-weight:600; margin-bottom:6px; }
.highlight-info .highlight-card-title { color:var(--blue-700); }
.highlight-warning .highlight-card-title { color:var(--amber-700); }
.highlight-card-body { font-size:12px; color:var(--ink-700); line-height:1.6; }
.highlight-card-body p { margin:0 0 4px; }
</style>