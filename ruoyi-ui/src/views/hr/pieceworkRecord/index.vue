<template>
  <div class="app-container hr-pieceworkrecord-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>计件单号</label>
          <div class="control">
            <el-input v-model="queryParams.pwRecordNo" placeholder="请输入计件单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>计件项目</label>
          <div class="control">
            <el-input v-model="queryParams.pwName" placeholder="请输入计件项目" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>计件日期</label>
          <div class="control">
            <el-date-picker v-model="recordDateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:piecework:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:piecework:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:piecework:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:piecework:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_pieceworkRecord_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrPieceworkRecordList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="计件单号" prop="pwRecordNo" key="pwRecordNo" align="center" :width="colWidth('pwRecordNo', 160)" resizable v-if="columns.pwRecordNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.pwRecordNo }}</span></template>
          </el-table-column>
          <el-table-column label="计件日期" prop="recordDate" key="recordDate" align="center" :width="colWidth('recordDate', 120)" resizable v-if="columns.recordDate.visible" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="计件计划ID" prop="pwPlanId" key="pwPlanId" align="center" :width="colWidth('pwPlanId', 110)" resizable v-if="columns.pwPlanId.visible" />
          <el-table-column label="计件项目" prop="pwName" key="pwName" align="center" :width="colWidth('pwName', 160)" resizable show-overflow-tooltip v-if="columns.pwName.visible" />
          <el-table-column label="合格数量" prop="qualifiedQty" key="qualifiedQty" align="center" :width="colWidth('qualifiedQty', 110)" resizable v-if="columns.qualifiedQty.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:piecework:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:piecework:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:piecework:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrPieceworkRecordRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="计件单号" prop="pwRecordNo"><el-input v-model="form.pwRecordNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计件日期" prop="recordDate"><el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="orgId">
                  <el-input :model-value="form.orgName" readonly placeholder="请选择部门">
                    <template #append>
                      <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
                      <el-button v-else icon="Search" @click="onOrgFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计件计划ID" prop="pwPlanId"><el-input-number v-model="form.pwPlanId" :precision="0" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计件项目" prop="pwName"><el-input v-model="form.pwName" placeholder="请输入计件项目" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 计件信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg></span>计件信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="合格数量" prop="qualifiedQty"><el-input-number v-model="form.qualifiedQty" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="不合格数量" prop="unqualifiedQty"><el-input-number v-model="form.unqualifiedQty" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="总金额" prop="totalAmount"><el-input-number v-model="form.totalAmount" :precision="2" :min="0" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="来源" prop="source"><el-input v-model="form.source" placeholder="请输入来源" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 确认信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>确认信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="确认状态" prop="confirmStatus"><el-select v-model="form.confirmStatus" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in auditStatusOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="确认人" prop="confirmBy"><el-input v-model="form.confirmBy" placeholder="请输入确认人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="确认时间" prop="confirmTime"><el-date-picker v-model="form.confirmTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
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

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">计件记录详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.pwRecordNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">计件单号：{{ viewData.pwRecordNo }}</span>
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
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">计件单号</span><div class="rd-value">{{ viewData.pwRecordNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计件日期</span><div class="rd-value">{{ viewData.recordDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计件计划ID</span><div class="rd-value">{{ viewData.pwPlanId || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计件项目</span><div class="rd-value">{{ viewData.pwName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>计件信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.qualifiedQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ viewData.unqualifiedQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">单价</span><div class="rd-value rd-amount">{{ viewData.unitPrice != null ? '￥' + viewData.unitPrice : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value rd-amount">{{ viewData.totalAmount != null ? '￥' + viewData.totalAmount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">来源</span><div class="rd-value">{{ viewData.source || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>确认信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">确认状态</span><div class="rd-value"><dict-tag :options="auditStatusOptions" :value="viewData.confirmStatus"/></div></div>
              <div class="rd-item"><span class="rd-label">确认人</span><div class="rd-value">{{ viewData.confirmBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">确认时间</span><div class="rd-value">{{ viewData.confirmTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.c3">
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

    <!-- ===== 人员选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />

    <!-- ===== 部门选择弹窗 ===== -->
    <dept-picker ref="orgPickerRef" title="选择部门" @confirm="onOrgPickerConfirm" />
  </div>
</template>

<script setup name="HrPieceworkRecord">
import { listHrPieceworkRecord, getHrPieceworkRecord, addHrPieceworkRecord, updateHrPieceworkRecord, delHrPieceworkRecord } from '@/api/hr/hrPieceworkRecord'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'
import DeptPicker from '@/components/DeptPicker'

const { proxy } = getCurrentInstance()

/** 字典：确认状态（复用 hr_audit_status：0待审 1通过 2驳回） */
const { hr_audit_status: auditStatusOptions } = proxy.useDict('hr_audit_status')

const hrPieceworkRecordList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const recordDateRange = ref([])
const collapsedCards = reactive({ c0: false, c1: false, c2: false, c3: false, e0: false, e1: false, e2: false })

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_pieceworkRecord_index')

const defaultColumns = {
  pwRecordNo: { label: '计件单号', visible: true },
  recordDate: { label: '计件日期', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  pwPlanId: { label: '计件计划ID', visible: true },
  pwName: { label: '计件项目', visible: true },
  qualifiedQty: { label: '合格数量', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_pieceworkRecord_index')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, pwRecordNo: undefined, employeeName: undefined, pwName: undefined },
  rules: {
    recordDate: [{ required: true, message: '计件日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.pwRecordNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.pwName) count++
  if (recordDateRange.value && recordDateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrPieceworkRecord(queryParams.value).then(response => {
    hrPieceworkRecordList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  proxy.addDateRange(queryParams.value, recordDateRange.value, 'RecordDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.pwRecordNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.pwName = undefined
  recordDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.pwRecordId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    pwRecordNo: undefined,
    recordDate: undefined,
    employeeId: undefined,
    employeeName: undefined,
    orgId: undefined,
    orgName: undefined,
    pwPlanId: undefined,
    pwName: undefined,
    qualifiedQty: undefined,
    unqualifiedQty: undefined,
    unitPrice: undefined,
    totalAmount: undefined,
    confirmStatus: undefined,
    confirmBy: undefined,
    confirmTime: undefined,
    source: undefined
  }
  proxy.resetForm('hrPieceworkRecordRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增计件记录'
}

function handleUpdate(row) {
  reset()
  const pwRecordId = row.pwRecordId || ids.value[0]
  getHrPieceworkRecord(pwRecordId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改计件记录'
  })
}

function handleView(row) {
  const pwRecordId = row.pwRecordId
  getHrPieceworkRecord(pwRecordId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrPieceworkRecordRef'].validate(valid => {
    if (valid) {
      if (form.value.pwRecordId != undefined) {
        updateHrPieceworkRecord(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrPieceworkRecord(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const pwRecordIds = row.pwRecordId || ids.value
  proxy.$modal.confirm('是否确认删除选中的计件记录？').then(function() {
    return delHrPieceworkRecord(pwRecordIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/pieceworkRecord/export', { ...queryParams.value }, `hrPieceworkRecord_${new Date().getTime()}.xlsx`)
}

/** ===== 人员/部门弹框选择 ===== */
const employeePickerRef = ref(null)
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
const orgPickerRef = ref(null)
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

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-pieceworkrecord-page {
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
.hr-pieceworkrecord-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-pieceworkrecord-page .filter-card { padding:14px 20px 16px; }
.hr-pieceworkrecord-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-pieceworkrecord-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-pieceworkrecord-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-pieceworkrecord-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-pieceworkrecord-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-pieceworkrecord-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-pieceworkrecord-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-pieceworkrecord-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-pieceworkrecord-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-pieceworkrecord-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-pieceworkrecord-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-pieceworkrecord-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-pieceworkrecord-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-pieceworkrecord-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-pieceworkrecord-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-pieceworkrecord-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-pieceworkrecord-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-pieceworkrecord-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-pieceworkrecord-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-pieceworkrecord-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-pieceworkrecord-page .field .control :deep(.el-select) { width:100%; }
.hr-pieceworkrecord-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-pieceworkrecord-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-pieceworkrecord-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.hr-pieceworkrecord-page .field .control :deep(.el-date-editor) { width:100%; }
.hr-pieceworkrecord-page .field .control :deep(.el-date-editor .el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; }
.hr-pieceworkrecord-page .field .control :deep(.el-range-editor) { border:0; background:transparent; box-shadow:none !important; padding:0; width:100%; }

/* ===== Toolbar ===== */
.hr-pieceworkrecord-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-pieceworkrecord-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-pieceworkrecord-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-pieceworkrecord-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-pieceworkrecord-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-pieceworkrecord-page .btn-soft .el-icon { font-size:14px; }
.hr-pieceworkrecord-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-pieceworkrecord-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-pieceworkrecord-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-pieceworkrecord-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-pieceworkrecord-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-pieceworkrecord-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-pieceworkrecord-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-pieceworkrecord-page .table-wrap { overflow-x:auto; }
.hr-pieceworkrecord-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-pieceworkrecord-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-pieceworkrecord-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-pieceworkrecord-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-pieceworkrecord-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-pieceworkrecord-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-pieceworkrecord-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-pieceworkrecord-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-pieceworkrecord-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-pieceworkrecord-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-pieceworkrecord-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-pieceworkrecord-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-pieceworkrecord-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-pieceworkrecord-page .badge.amber .dot { background:var(--amber-500); }
.hr-pieceworkrecord-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-pieceworkrecord-page .badge.blue .dot { background:var(--blue-500); }
.hr-pieceworkrecord-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-pieceworkrecord-page .badge.green .dot { background:var(--green-500); }
.hr-pieceworkrecord-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-pieceworkrecord-page .badge.red .dot { background:var(--red-500); }
.hr-pieceworkrecord-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-pieceworkrecord-page .badge.violet .dot { background:var(--brand-500); }
.hr-pieceworkrecord-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-pieceworkrecord-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-pieceworkrecord-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-pieceworkrecord-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .btn-prev), .hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-pieceworkrecord-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 查看详情弹窗（rd-*) ===== */
.hr-pieceworkrecord-page .rd-page {
  max-height: 60vh;
  overflow-y: auto;
}
.hr-pieceworkrecord-page .rd-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  animation: rdFadeIn 0.4s ease-out forwards;
}
.hr-pieceworkrecord-page .rd-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  cursor: pointer;
  user-select: none;
}
.hr-pieceworkrecord-page .rd-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.hr-pieceworkrecord-page .rd-card-icon {
  display: flex;
  align-items: center;
  color: #409eff;
}
.hr-pieceworkrecord-page .rd-collapse-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  color: #909399;
  transition: transform 0.2s;
}
.hr-pieceworkrecord-page .rd-collapse-btn.is-collapsed {
  transform: rotate(-90deg);
}
.hr-pieceworkrecord-page .rd-card-body {
  padding: 16px;
}
.hr-pieceworkrecord-page .rd-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 24px;
}
.hr-pieceworkrecord-page .rd-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hr-pieceworkrecord-page .rd-item--full {
  grid-column: 1 / -1;
}
.hr-pieceworkrecord-page .rd-label {
  font-size: 12px;
  color: #909399;
}
.hr-pieceworkrecord-page .rd-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.hr-pieceworkrecord-page .rd-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  color: #c0c4cc;
}
.hr-pieceworkrecord-page .rd-empty-icon {
  margin-bottom: 8px;
}
.hr-pieceworkrecord-page .rd-empty-text {
  font-size: 13px;
  margin: 0;
}
.hr-pieceworkrecord-page .rd-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hr-pieceworkrecord-page .rd-detail-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}
.hr-pieceworkrecord-page .rd-detail-header-main {
  flex: 1;
}
.hr-pieceworkrecord-page .rd-detail-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.hr-pieceworkrecord-page .rd-detail-header-sub {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}
.hr-pieceworkrecord-page .rd-detail-header-divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.hr-pieceworkrecord-page .rd-detail-header-no {
  font-size: 13px;
  color: #909399;
}

/* ===== rd-dialog header 覆盖（全局 detail-page.scss 兜底） ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 16px; right: 16px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }

@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-pieceworkrecord-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-pieceworkrecord-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-pieceworkrecord-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
