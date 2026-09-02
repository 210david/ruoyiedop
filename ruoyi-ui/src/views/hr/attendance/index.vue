<template>
  <div class="app-container hr-attendance-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>考勤日期</label>
          <div class="control">
            <el-date-picker v-model="recordDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field">
          <label>班次名称</label>
          <div class="control">
            <el-input v-model="queryParams.shiftName" placeholder="请输入班次名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>考勤状态</label>
          <div class="control">
            <el-select v-model="queryParams.attendanceStatus" placeholder="请选择" clearable @change="handleQuery">
              <el-option v-for="d in attendanceStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:attendance:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:attendance:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:attendance:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:attendance:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_attendance_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrAttendanceRecordList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="考勤日期" prop="recordDate" key="recordDate" align="center" :width="colWidth('recordDate', 120)" resizable v-if="columns.recordDate.visible" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="班次名称" prop="shiftName" key="shiftName" align="center" :width="colWidth('shiftName', 120)" resizable v-if="columns.shiftName.visible" />
          <el-table-column label="上班打卡" prop="clockIn" key="clockIn" align="center" :width="colWidth('clockIn', 130)" resizable v-if="columns.clockIn.visible" />
          <el-table-column label="考勤状态" prop="attendanceStatus" key="attendanceStatus" align="center" :width="colWidth('attendanceStatus', 110)" resizable v-if="columns.attendanceStatus.visible">
            <template #default="scope">
              <span class="badge" :class="attendanceStatusClass(scope.row.attendanceStatus)"><span class="dot"></span>{{ attendanceStatusLabel(scope.row.attendanceStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:attendance:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:attendance:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:attendance:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrAttendanceRecordRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="考勤日期" prop="recordDate"><el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="orgId">
                  <el-input :model-value="form.orgName" readonly placeholder="请选择部门">
                    <template #append>
                      <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
                      <el-button v-else icon="Search" @click="onOrgFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>打卡信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="班次" prop="shiftId"><el-select v-model="form.shiftId" placeholder="请选择班次" clearable style="width: 100%" @change="onShiftChange"><el-option v-for="s in shiftOptions" :key="s.shiftId" :label="s.shiftName" :value="s.shiftId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="上班打卡" prop="clockIn"><el-date-picker v-model="form.clockIn" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="下班打卡" prop="clockOut"><el-date-picker v-model="form.clockOut" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></span>考勤结果</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="考勤状态" prop="attendanceStatus"><el-select v-model="form.attendanceStatus" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in attendanceStatusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="迟到分钟" prop="lateMinutes"><el-input-number v-model="form.lateMinutes" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="早退分钟" prop="earlyMinutes"><el-input-number v-model="form.earlyMinutes" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3">
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" placeholder="请输入备注" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">考勤详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.recordDate">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">{{ viewData.recordDate }}<template v-if="viewData.employeeNo"> · {{ viewData.employeeNo }}</template></span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">考勤日期</span><div class="rd-value">{{ viewData.recordDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>打卡信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">班次名称</span><div class="rd-value">{{ viewData.shiftName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">上班打卡</span><div class="rd-value">{{ viewData.clockIn || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">下班打卡</span><div class="rd-value">{{ viewData.clockOut || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></span>考勤结果</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">考勤状态</span><div class="rd-value"><dict-tag :options="attendanceStatusOptions" :value="viewData.attendanceStatus"/></div></div>
              <div class="rd-item"><span class="rd-label">迟到分钟</span><div class="rd-value">{{ viewData.lateMinutes != null ? viewData.lateMinutes : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">早退分钟</span><div class="rd-value">{{ viewData.earlyMinutes != null ? viewData.earlyMinutes : '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择弹窗 -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
    <!-- 部门选择弹窗 -->
    <dept-picker ref="orgPickerRef" title="选择部门" @confirm="onOrgPickerConfirm" />
  </div>
</template>

<script setup name="HrAttendanceRecord">
import { listHrAttendanceRecord, getHrAttendanceRecord, addHrAttendanceRecord, updateHrAttendanceRecord, delHrAttendanceRecord } from '@/api/hr/hrAttendanceRecord'
import { listHrShift } from '@/api/hr/hrShift'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker';
import DeptPicker from '@/components/DeptPicker';

const { proxy } = getCurrentInstance()
const { hr_attendance_status: attendanceStatusOptions } = proxy.useDict('hr_attendance_status')

const hrAttendanceRecordList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showAdvanced = ref(false)
const recordDateRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e2: false, e3: false })

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_attendance_index')

/** 列显隐配置 */
const defaultColumns = {
  recordDate: { label: '考勤日期', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  shiftName: { label: '班次名称', visible: true },
  clockIn: { label: '上班打卡', visible: true },
  attendanceStatus: { label: '考勤状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_attendance_columns')
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

/** 考勤状态徽章映射（兼容字典编码与历史中文值） */
function attendanceStatusClass(v) {
  const map = { '0': 'green', '正常': 'green', '1': 'amber', '迟到': 'amber', '2': 'amber', '早退': 'amber', '3': 'red', '旷工': 'red', '缺勤': 'red', '4': 'amber', '漏卡': 'amber', '5': 'blue', '请假': 'blue', '6': 'violet', '出差': 'violet', '7': 'violet', '加班': 'violet' }
  return map[v] || 'gray'
}
function attendanceStatusLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const map = { '0': '正常', '1': '迟到', '2': '早退', '3': '旷工', '4': '漏卡', '5': '请假', '6': '出差', '7': '加班' }
  return map[v] || String(v)
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (recordDateRange.value && recordDateRange.value.length === 2) count++
  if (queryParams.value.shiftName) count++
  if (queryParams.value.attendanceStatus) count++
  return count
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeId: undefined, employeeName: undefined, employeeNo: undefined, shiftName: undefined, attendanceStatus: undefined },
  rules: {
    recordDate: [{ required: true, message: '考勤日期不能为空', trigger: 'blur' }],
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** ===== 人员/部门/岗位弹框选择 ===== */
const employeePickerRef = ref(null)
const orgPickerRef = ref(null)

function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}
function openOrgPicker() { orgPickerRef.value.open(form.value.orgId); }
function onOrgPickerConfirm(dept) { form.value.orgId = dept.deptId; form.value.orgName = dept.deptName; }
function clearOrg() { form.value.orgId = null; form.value.orgName = ''; }
function onOrgFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.orgId) { clearOrg(); } else { openOrgPicker(); }
    return;
  }
  openOrgPicker();
}

/** ===== 班次下拉 ===== */
const shiftOptions = ref([])
function loadShiftOptions() {
  listHrShift({ pageNum: 1, pageSize: 100 }).then(res => {
    shiftOptions.value = res.rows || []
  })
}
function onShiftChange(shiftId) {
  const s = (shiftOptions.value || []).find(i => i.shiftId === shiftId)
  form.value.shiftName = s ? s.shiftName : undefined
}

function getList() {
  loading.value = true
  listHrAttendanceRecord(queryParams.value).then(response => {
    hrAttendanceRecordList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  queryParams.value.params = {}
  proxy.addDateRange(queryParams.value, recordDateRange.value, 'RecordDate')
  getList()
}

function resetQuery() {
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.shiftName = undefined
  queryParams.value.attendanceStatus = undefined
  recordDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.recordId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    recordDate: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    orgId: undefined,
    shiftId: undefined,
    shiftName: undefined,
    clockIn: undefined,
    clockOut: undefined,
    lateMinutes: undefined,
    earlyMinutes: undefined,
    attendanceStatus: undefined,
    remark: undefined
  }
  proxy.resetForm('hrAttendanceRecordRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增考勤记录'
}

function handleUpdate(row) {
  reset()
  const recordId = row.recordId || ids.value[0]
  getHrAttendanceRecord(recordId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改考勤记录'
  })
}

function handleView(row) {
  const recordId = row.recordId
  getHrAttendanceRecord(recordId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrAttendanceRecordRef'].validate(valid => {
    if (valid) {
      if (form.value.recordId != undefined) {
        updateHrAttendanceRecord(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrAttendanceRecord(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const recordIds = row.recordId || ids.value
  proxy.$modal.confirm('是否确认删除选中的考勤记录？').then(function() {
    return delHrAttendanceRecord(recordIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/attendance/export', { ...queryParams.value }, `hrAttendanceRecord_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

loadShiftOptions()
getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-attendance-page {
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

/* ===== Surface Card ===== */
.hr-attendance-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-attendance-page .filter-card { padding:14px 20px 16px; }
.hr-attendance-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-attendance-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-attendance-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-attendance-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-attendance-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-attendance-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-attendance-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-attendance-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-attendance-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-attendance-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-attendance-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-attendance-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-attendance-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-attendance-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-attendance-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-attendance-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-attendance-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-attendance-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-attendance-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-attendance-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-attendance-page .field .control :deep(.el-select) { width:100%; }
.hr-attendance-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-attendance-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-attendance-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-attendance-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-attendance-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-attendance-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-attendance-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-attendance-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-attendance-page .btn-soft .el-icon { font-size:14px; }
.hr-attendance-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-attendance-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-attendance-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-attendance-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-attendance-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-attendance-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-attendance-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-attendance-page .table-wrap { overflow-x:auto; }
.hr-attendance-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-attendance-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-attendance-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-attendance-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-attendance-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-attendance-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-attendance-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-attendance-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-attendance-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-attendance-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-attendance-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-attendance-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-attendance-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-attendance-page .badge.amber .dot { background:var(--amber-500); }
.hr-attendance-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-attendance-page .badge.blue .dot { background:var(--blue-500); }
.hr-attendance-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-attendance-page .badge.green .dot { background:var(--green-500); }
.hr-attendance-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-attendance-page .badge.red .dot { background:var(--red-500); }
.hr-attendance-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-attendance-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-attendance-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-attendance-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-attendance-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-attendance-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-attendance-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-attendance-page .pagination-container :deep(.el-pagination .btn-prev), .hr-attendance-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-attendance-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-attendance-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-attendance-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-attendance-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-attendance-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-attendance-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-attendance-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-attendance-page .rd-card-icon { display: flex; align-items: center; color: #409eff; }
.hr-attendance-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-attendance-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-attendance-page .rd-card-body { padding: 16px; }
.hr-attendance-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-attendance-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-attendance-page .rd-item--full { grid-column: 1 / -1; }
.hr-attendance-page .rd-label { font-size: 12px; color: #909399; }
.hr-attendance-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-attendance-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-attendance-page .rd-empty-icon { margin-bottom: 8px; }
.hr-attendance-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-attendance-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-attendance-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-attendance-page .rd-detail-header-main { flex: 1; }
.hr-attendance-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-attendance-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-attendance-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-attendance-page .rd-detail-header-no { font-size: 13px; color: #909399; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-attendance-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-attendance-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-attendance-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-attendance-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-attendance-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-attendance-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-attendance-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
