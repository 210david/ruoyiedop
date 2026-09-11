<template>
  <div class="app-container hr-specialDeduction-page">
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
          <label>扣除项目</label>
          <div class="control">
            <el-select v-model="queryParams.deductionType" placeholder="请选择扣除项目" clearable filterable @change="handleQuery" style="width: 100%">
              <el-option v-for="d in deductionTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>生效日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>失效日期</label>
          <div class="control">
            <el-date-picker v-model="expiryDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:specialDeduction:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:specialDeduction:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:specialDeduction:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['hr:specialDeduction:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:specialDeduction:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_specialDeduction_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrSpecialDeductionList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :min-width="colWidth('employeeName', 120)" show-overflow-tooltip resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="部门" prop="deptName" key="deptName" align="center" :min-width="colWidth('deptName', 140)" show-overflow-tooltip resizable v-if="columns.deptName.visible" />
          <el-table-column label="扣除项目" prop="deductionType" key="deductionType" align="center" :width="colWidth('deductionType', 180)" resizable v-if="columns.deductionType.visible">
            <template #default="scope">
              <span class="badge" :class="deductionBadgeClass(scope.row.deductionType)">{{ scope.row.deductionType }}</span>
            </template>
          </el-table-column>
          <el-table-column label="月扣除金额" prop="deductionAmount" key="deductionAmount" align="center" :width="colWidth('deductionAmount', 140)" resizable v-if="columns.deductionAmount.visible">
            <template #default="scope"><span class="rd-amount">￥{{ formatAmount(scope.row.deductionAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="生效日期" prop="effectiveDate" key="effectiveDate" align="center" :width="colWidth('effectiveDate', 130)" resizable v-if="columns.effectiveDate.visible" />
          <el-table-column label="失效日期" prop="expiryDate" key="expiryDate" align="center" :width="colWidth('expiryDate', 130)" resizable v-if="columns.expiryDate.visible" />
          <el-table-column label="删除标志" prop="delFlag" key="delFlag" align="center" :width="colWidth('delFlag', 110)" resizable v-if="columns.delFlag.visible">
            <template #default="scope">
              <span class="badge" :class="delFlagBadgeClass(scope.row.delFlag)"><span class="dot"></span>{{ delFlagLabel(scope.row.delFlag) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:specialDeduction:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:specialDeduction:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:specialDeduction:remove']">删除</el-button>
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
    <el-dialog v-model="open" width="800px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrSpecialDeductionRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
                  <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
                    <template #append>
                      <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
                      <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
                    </template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" placeholder="请输入工号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="部门" prop="deptName"><el-input :model-value="form.deptName" readonly placeholder="选择人员后自动带出" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 扣除信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>扣除信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="扣除项目" prop="deductionType">
                  <el-select v-model="form.deductionType" placeholder="请选择扣除项目" style="width: 100%">
                    <el-option v-for="d in deductionTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="月扣除金额" prop="deductionAmount">
                  <el-select v-if="deductionRule.options" v-model="form.deductionAmount" placeholder="请选择扣除档位" style="width: 100%">
                    <el-option v-for="o in deductionRule.options" :key="o.value" :label="o.label" :value="o.value" />
                  </el-select>
                  <el-input-number v-else v-model="form.deductionAmount" :precision="2" :min="0" :max="deductionRule.max || Infinity" :disabled="deductionRule.fixed !== undefined" controls-position="right" style="width: 100%" />
                  <div v-if="deductionRule.hint" class="amount-hint">{{ deductionRule.hint }}</div>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失效日期" prop="expiryDate"><el-date-picker v-model="form.expiryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
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
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">专项附加扣除详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.sdId">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">工号：{{ viewData.employeeNo || '-' }}</span>
              <span class="rd-detail-header-no">员工：{{ viewData.employeeName || '-' }}</span>
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
          <div class="rd-card-body" v-show="!collapsedCards.c0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>部门</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.deptName }">{{ viewData.deptName || '暂无部门' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('c1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>扣除信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>扣除项目</span><div class="rd-value">{{ viewData.deductionType || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>月扣除金额</span><div class="rd-value rd-amount">￥{{ formatAmount(viewData.deductionAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>生效日期</span><div class="rd-value">{{ viewData.effectiveDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label"><span class="rd-label-dot"></span>失效日期</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.expiryDate }">{{ viewData.expiryDate || '长期有效' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('c2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.c2">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label"><span class="rd-label-dot"></span>备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无备注' }}</div></div>
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

    <!-- ===== 导入对话框 ===== -->
    <excel-import-dialog
      ref="importRef"
      title="专项附加扣除导入"
      action="/hr/specialDeduction/importData"
      template-action="/hr/specialDeduction/importTemplate"
      template-file-name="hr_special_deduction_template"
      update-support-label="是否更新已存在的扣除记录（同员工+同项目+同生效日期）"
      :tips="importTips"
      :column-labels="importColumnLabels"
      :update-key-options="updateKeyOptions"
      default-update-key="employeeNo"
      @success="getList"
    />
  </div>
</template>

<script setup name="HrSpecialDeduction">
import { listHrSpecialDeduction, getHrSpecialDeduction, addHrSpecialDeduction, updateHrSpecialDeduction, delHrSpecialDeduction } from '@/api/hr/hrSpecialDeduction'
import { Filter, ArrowDown, Search, Edit, Delete, Download } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'
import ExcelImportDialog from '@/components/ExcelImportDialog';
import { Upload } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const hrSpecialDeductionList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const dateRange = ref([])
const expiryDateRange = ref([])
const showAdvanced = ref(false)

/** 扣除项目字典（个人所得税专项附加扣除固定7项） */
const { hr_deduction_type: deductionTypeOptions } = proxy.useDict('hr_deduction_type')

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_specialDeduction_index')

/** 列显隐配置 - 从 localStorage 恢复保存的设置 */
const defaultColumns = {
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  deptName: { label: '部门', visible: true },
  deductionType: { label: '扣除项目', visible: true },
  deductionAmount: { label: '月扣除金额', visible: true },
  effectiveDate: { label: '生效日期', visible: true },
  expiryDate: { label: '失效日期', visible: true },
  delFlag: { label: '删除标志', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_specialDeduction_columns')
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

/** 查看详情 */
const viewData = ref({})
const collapsedCards = reactive({ c0: false, c1: false, c2: false, e0: false, e1: false })
function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function delFlagLabel(v) {
  const map = { '0': '正常', '2': '已删除' }
  return map[String(v)] || '-'
}

/** 金额千分位 + 两位小数（detail-page-style-guide 3.4） */
function formatAmount(val) {
  if (val === null || val === undefined || val === '') return '-'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 扣除项目徽章颜色映射（list-page-style-guide 5.7 字典列统一 badge） */
function deductionBadgeClass(v) {
  const map = {
    '3岁以下婴幼儿照护': 'blue',
    '子女教育': 'green',
    '继续教育': 'violet',
    '大病医疗': 'red',
    '住房贷款利息': 'amber',
    '住房租金': 'amber',
    '赡养老人': 'gray'
  }
  return map[v] || 'gray'
}
function delFlagBadgeClass(v) {
  const map = { '0': 'green', '2': 'red' }
  return map[String(v)] || 'gray'
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, employeeName: undefined, employeeNo: undefined, deductionType: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    deductionType: [{ required: true, message: '扣除项目不能为空', trigger: 'change' }],
    effectiveDate: [{ required: true, message: '生效日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/**
 * 月扣除金额法定标准固化（国家税务总局公告2023年第14号，2026年度维持现行）
 * fixed: 定额锁定；options: 档位下拉；max: 自由录入上限；hint: 填写提示
 */
const deductionRules = {
  '3岁以下婴幼儿照护': { fixed: 2000, hint: '法定标准：每孩每月2000元' },
  '子女教育': { fixed: 2000, hint: '法定标准：每孩每月2000元' },
  '赡养老人': { options: [{ value: 3000, label: '3000 元（独生子女）' }, { value: 1500, label: '1500 元（非独生子女分摊上限）' }], hint: '独生子女每月3000元；非独生子女分摊每月不超过1500元' },
  '住房贷款利息': { fixed: 1000, hint: '法定标准：每月1000元（首套住房贷款，与住房租金不可同时享受）' },
  '住房租金': { options: [{ value: 1500, label: '1500 元（直辖市/省会/计划单列市）' }, { value: 1100, label: '1100 元（市辖区户籍人口超100万）' }, { value: 800, label: '800 元（市辖区户籍人口不超100万）' }], hint: '按主要工作城市分档；与住房贷款利息不可同时享受' },
  '继续教育': { fixed: 400, hint: '学历（学位）继续教育每月400元；职业资格继续教育为取证当年一次性3600元' },
  '大病医疗': { max: 80000, hint: '医保目录内自付累计超1.5万元部分据实录入，年度最高80000元' }
}
const deductionRule = computed(() => deductionRules[form.value.deductionType] || {})
/** 扣除项目变化时按法定标准自动带出/清空金额 */
watch(() => form.value.deductionType, (t, old) => {
  if (t === old) return
  const rule = deductionRules[t]
  if (rule && rule.fixed !== undefined) { form.value.deductionAmount = rule.fixed; return }
  if (rule && rule.options) { form.value.deductionAmount = undefined; return }
  if (old && deductionRules[old] && (deductionRules[old].fixed !== undefined || deductionRules[old].options)) { form.value.deductionAmount = undefined }
})

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.deductionType) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  if (expiryDateRange.value && expiryDateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listHrSpecialDeduction(queryParams.value).then(response => {
    hrSpecialDeductionList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.params = {}
  proxy.addDateRange(queryParams.value, dateRange.value, 'EffectiveDate')
  proxy.addDateRange(queryParams.value, expiryDateRange.value, 'ExpiryDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.deductionType = undefined
  dateRange.value = []
  expiryDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.sdId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    deptId: undefined,
    deptName: undefined,
    deductionType: undefined,
    deductionAmount: undefined,
    effectiveDate: undefined,
    expiryDate: undefined
  }
  proxy.resetForm('hrSpecialDeductionRef')
}

function handleAdd() {
  reset()
  // 年度申报口径默认值：生效=当年1月1日，失效=当年12月31日
  const y = new Date().getFullYear()
  form.value.effectiveDate = `${y}-01-01`
  form.value.expiryDate = `${y}-12-31`
  open.value = true
  title.value = '新增专项附加扣除'
}

function handleUpdate(row) {
  reset()
  const sdId = row.sdId || ids.value[0]
  getHrSpecialDeduction(sdId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改专项附加扣除'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const sdId = row.sdId || ids.value[0]
  getHrSpecialDeduction(sdId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['hrSpecialDeductionRef'].validate(valid => {
    if (valid) {
      if (form.value.sdId != undefined) {
        updateHrSpecialDeduction(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrSpecialDeduction(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const sdIds = row.sdId || ids.value
  proxy.$modal.confirm('是否确认删除选中的专项附加扣除？').then(function() {
    return delHrSpecialDeduction(sdIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/specialDeduction/export', proxy.withExportIds({ ...queryParams.value }, ids.value, 'sdId'), `hrSpecialDeduction_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员弹框选择 ===== */
const employeePickerRef = ref(null)
const importRef = ref(null)

/** 导入对话框配置 */
/** 导入说明（数组，逐条编号渲染；字符串会被 v-for 逐字符遍历导致样式异常） */
const importTips = [
  '请下载模板后按工号填写员工专项附加扣除数据（数据来源于员工在个人所得税App的申报，或员工提交的纸质申报表）。',
  '扣除项目为固定字典值：3岁以下婴幼儿照护、子女教育、继续教育、大病医疗、住房贷款利息、住房租金、赡养老人，须与模板下拉项完全一致。',
  '月扣除金额按法定标准填写：婴幼儿照护/子女教育每孩每月2000元；赡养老人独生子女3000元、非独生分摊不超1500元；住房贷款利息每月1000元；住房租金按城市1500/1100/800元；学历继续教育每月400元；大病医疗为医保自付超1.5万部分，年度最高8万元。',
  '部门无需填写，系统按员工档案自动带出。'
]
const importColumnLabels = {
  employeeNo: '工号（必填，用于匹配员工）',
  employeeName: '员工姓名',
  deductionType: '扣除项目（必填，固定字典值）',
  deductionAmount: '月扣除金额（必填，元，按法定标准）',
  effectiveDate: '生效日期（必填，如 2026-01-01）',
  expiryDate: '失效日期（选填）'
}
const updateKeyOptions = [
  { value: 'employeeNo', label: '工号' }
]
function handleImport() {
  importRef.value.open()
}
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { deptId: emp.orgId, deptName: emp.orgName, idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
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

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.hr-specialDeduction-page {
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
.hr-specialDeduction-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-specialDeduction-page .filter-card { padding:14px 20px 16px; }
.hr-specialDeduction-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-specialDeduction-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-specialDeduction-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-specialDeduction-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-specialDeduction-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-specialDeduction-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-specialDeduction-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-specialDeduction-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-specialDeduction-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-specialDeduction-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-specialDeduction-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-specialDeduction-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-specialDeduction-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-specialDeduction-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-specialDeduction-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-specialDeduction-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-specialDeduction-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-specialDeduction-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-specialDeduction-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-specialDeduction-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-specialDeduction-page .field .control :deep(.el-select) { width:100%; }
.hr-specialDeduction-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-specialDeduction-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-specialDeduction-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-specialDeduction-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-specialDeduction-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-specialDeduction-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-specialDeduction-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-specialDeduction-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-specialDeduction-page .btn-soft .el-icon { font-size:14px; }
.hr-specialDeduction-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-specialDeduction-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-specialDeduction-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-specialDeduction-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-specialDeduction-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-specialDeduction-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }

/* ===== Table ===== */
.hr-specialDeduction-page .table-wrap { overflow-x:auto; }
.hr-specialDeduction-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-specialDeduction-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-specialDeduction-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-specialDeduction-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-specialDeduction-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-specialDeduction-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-specialDeduction-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-specialDeduction-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-specialDeduction-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-specialDeduction-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-specialDeduction-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-specialDeduction-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-specialDeduction-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-specialDeduction-page .badge.amber .dot { background:var(--amber-500); }
.hr-specialDeduction-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-specialDeduction-page .badge.blue .dot { background:var(--blue-500); }
.hr-specialDeduction-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-specialDeduction-page .badge.green .dot { background:var(--green-500); }
.hr-specialDeduction-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-specialDeduction-page .badge.red .dot { background:var(--red-500); }
.hr-specialDeduction-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-specialDeduction-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-specialDeduction-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-specialDeduction-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-specialDeduction-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-specialDeduction-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-specialDeduction-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-specialDeduction-page .pagination-container :deep(.el-pagination .btn-prev), .hr-specialDeduction-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-specialDeduction-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-specialDeduction-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-specialDeduction-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 详情弹窗样式：统一使用全局 detail-page.scss 的 rd- 体系，页面不再重复定义 ===== */

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

.amount-hint { font-size: 12px; color: #909399; line-height: 1.5; margin-top: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-specialDeduction-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-specialDeduction-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-specialDeduction-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
