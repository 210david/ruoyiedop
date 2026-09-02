<template>
  <div class="app-container safety-enterprise-page">
    <!-- ===== Filter Card ===== -->
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
          <label>企业名称</label>
          <div class="control">
            <el-input v-model="queryParams.enterpriseName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>信用代码</label>
          <div class="control">
            <el-input v-model="queryParams.creditCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>行业类别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.industryType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_industry_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>是否高危</label>
          <div class="control is-select">
            <el-select v-model="queryParams.isHighRisk" placeholder="全部" clearable @change="handleQuery">
              <el-option label="是" value="1" />
              <el-option label="否" value="0" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>法定代表人</label>
          <div class="control">
            <el-input v-model="queryParams.legalPerson" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>安全负责人</label>
          <div class="control">
            <el-input v-model="queryParams.safetyManager" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>安全管理机构</label>
          <div class="control">
            <el-input v-model="queryParams.safetyOrg" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:enterprise:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:enterprise:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Upload" @click="handleExport" v-hasPermi="['safety:enterprise:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_enterprise_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="enterpriseList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="企业名称" prop="enterpriseName" key="enterpriseName" :width="colWidth('enterpriseName', 200)" resizable sortable="custom" show-overflow-tooltip v-if="columns.enterpriseName.visible" />
          <el-table-column label="统一社会信用代码" prop="creditCode" key="creditCode" :width="colWidth('creditCode', 200)" resizable show-overflow-tooltip v-if="columns.creditCode.visible" />
          <el-table-column label="行业类别" prop="industryType" key="industryType" :width="colWidth('industryType', 120)" resizable align="center" v-if="columns.industryType.visible">
            <template #default="scope">
              <span class="badge violet">{{ industryTypeLabel(scope.row.industryType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="是否高危" prop="isHighRisk" key="isHighRisk" :width="colWidth('isHighRisk', 90)" resizable align="center" v-if="columns.isHighRisk.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.isHighRisk === '1' ? 'red' : 'gray'">
                <span class="dot"></span>{{ scope.row.isHighRisk === '1' ? '是' : '否' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="法定代表人" prop="legalPerson" key="legalPerson" :width="colWidth('legalPerson', 120)" resizable show-overflow-tooltip v-if="columns.legalPerson.visible" />
          <el-table-column label="安全负责人" prop="safetyManager" key="safetyManager" :width="colWidth('safetyManager', 120)" resizable show-overflow-tooltip v-if="columns.safetyManager.visible" />
          <el-table-column label="安全管理机构" prop="safetyOrg" key="safetyOrg" :width="colWidth('safetyOrg', 150)" resizable show-overflow-tooltip v-if="columns.safetyOrg.visible" />
          <el-table-column label="属地监管单位" prop="superviseDept" key="superviseDept" :width="colWidth('superviseDept', 150)" resizable show-overflow-tooltip v-if="columns.superviseDept.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:enterprise:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:enterprise:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:enterprise:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="enterpriseRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="企业名称" prop="enterpriseName"><el-input v-model="form.enterpriseName" placeholder="请输入企业名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="信用代码" prop="creditCode"><el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="行业类别" prop="industryType"><el-select v-model="form.industryType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_industry_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="是否高危行业" prop="isHighRisk"><el-radio-group v-model="form.isHighRisk"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>管理信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="法定代表人" prop="legalPerson"><el-input v-model="form.legalPerson" placeholder="请输入法定代表人" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="安全负责人" prop="safetyManagerId">
                  <el-input v-model="form.safetyManager" readonly placeholder="请选择安全负责人" style="width: 100%" @click="openUserPicker">
                    <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                    <template #suffix><el-icon v-if="form.safetyManager" class="clear-icon" @click.stop="clearSafetyManager"><CircleClose /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="安全管理机构" prop="safetyOrg"><el-input v-model="form.safetyOrg" placeholder="请输入安全管理机构" /></el-form-item>
              <el-form-item label="属地监管单位" prop="superviseDept"><el-input v-model="form.superviseDept" placeholder="请输入属地监管单位" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="安全生产责任制" prop="responsibility"><el-input v-model="form.responsibility" type="textarea" :rows="3" placeholder="请输入安全生产责任制" /></el-form-item>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/></svg></div>
          <span class="rd-detail-header-title">企业详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.creditCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">信用代码：{{ viewData.creditCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">企业名称</span><div class="rd-value">{{ viewData.enterpriseName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">信用代码</span><div class="rd-value">{{ viewData.creditCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">行业类别</span><div class="rd-value"><dict-tag :options="safety_industry_type" :value="viewData.industryType" /></div></div>
              <div class="rd-item"><span class="rd-label">是否高危</span><div class="rd-value"><span class="badge" :class="viewData.isHighRisk === '1' ? 'red' : 'gray'"><span class="dot"></span>{{ viewData.isHighRisk === '1' ? '是' : '否' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.legalPerson || viewData.safetyManager || viewData.safetyOrg || viewData.superviseDept">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>管理信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">法定代表人</span><div class="rd-value">{{ viewData.legalPerson || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">安全负责人</span><div class="rd-value">{{ viewData.safetyManager || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">安全管理机构</span><div class="rd-value">{{ viewData.safetyOrg || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">属地监管单位</span><div class="rd-value">{{ viewData.superviseDept || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.responsibility || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">安全生产责任制</span><div class="rd-value">{{ viewData.responsibility || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 安全负责人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择安全负责人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="SafetyEnterprise">
import { listEnterprise, getEnterprise, addEnterprise, updateEnterprise, delEnterprise } from '@/api/safety/enterprise'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_industry_type } = proxy.useDict('safety_industry_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_enterprise_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const enterpriseList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  enterpriseName: { label: '企业名称', visible: true },
  creditCode: { label: '统一社会信用代码', visible: true },
  industryType: { label: '行业类别', visible: true },
  isHighRisk: { label: '是否高危', visible: true },
  legalPerson: { label: '法定代表人', visible: true },
  safetyManager: { label: '安全负责人', visible: true },
  safetyOrg: { label: '安全管理机构', visible: true },
  superviseDept: { label: '属地监管单位', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_enterprise_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, enterpriseName: undefined, creditCode: undefined, industryType: undefined, isHighRisk: undefined, legalPerson: undefined, safetyManager: undefined, safetyOrg: undefined, params: {} },
  rules: {
    enterpriseName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
    creditCode: [{ required: true, message: '统一社会信用代码不能为空', trigger: 'blur' }],
    industryType: [{ required: true, message: '行业类别不能为空', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.enterpriseName) count++
  if (queryParams.value.creditCode) count++
  if (queryParams.value.industryType) count++
  if (queryParams.value.isHighRisk) count++
  if (queryParams.value.legalPerson) count++
  if (queryParams.value.safetyManager) count++
  if (queryParams.value.safetyOrg) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listEnterprise(queryParams.value).then(response => {
    enterpriseList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  }).catch(error => { console.error(error) }).finally(() => { loading.value = false })
}

function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.enterpriseName = undefined; queryParams.value.creditCode = undefined; queryParams.value.industryType = undefined; queryParams.value.isHighRisk = undefined; queryParams.value.legalPerson = undefined; queryParams.value.safetyManager = undefined; queryParams.value.safetyOrg = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.enterpriseId); single.value = selection.length !== 1; multiple.value = !selection.length }

function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = '添加企业档案' }
function handleView(row) {
  const enterpriseId = row.enterpriseId || ids.value[0]
  getEnterprise(enterpriseId).then(response => { viewData.value = response.data; viewOpen.value = true })
}
function handleUpdate(row) {
  reset()
  const enterpriseId = row.enterpriseId || ids.value[0]
  getEnterprise(enterpriseId).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.legalPerson && !response.data.safetyOrg && !response.data.superviseDept; collapsedCards.c2 = !response.data.responsibility && !response.data.remark; open.value = true; title.value = '修改企业档案' })
}
function submitForm() {
  proxy.$refs['enterpriseRef'].validate(valid => {
    if (valid) {
      if (form.value.enterpriseId != undefined) {
        updateEnterprise(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addEnterprise(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function handleDelete(row) {
  const enterpriseIds = row.enterpriseId || ids.value
  proxy.$modal.confirm('是否确认删除企业档案？').then(function() { return delEnterprise(enterpriseIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('safety/enterprise/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `enterprise_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { enterpriseId: undefined, enterpriseName: undefined, creditCode: undefined, industryType: undefined, isHighRisk: '0', legalPerson: undefined, safetyManagerId: undefined, safetyManager: undefined, safetyOrg: undefined, superviseDept: undefined, responsibility: undefined, remark: undefined }
  proxy.resetForm('enterpriseRef')
}
/** 打开安全负责人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.safetyManagerId) }
/** 安全负责人选择确认回调 */
function onUserPickerConfirm(user) { form.value.safetyManagerId = user.userId; form.value.safetyManager = user.nickName }
/** 清除安全负责人 */
function clearSafetyManager() { form.value.safetyManagerId = undefined; form.value.safetyManager = undefined }

function industryTypeLabel(type) { const item = safety_industry_type.value.find(d => d.value == type); return item ? item.label : '-' }

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.safety-enterprise-page {
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

.safety-enterprise-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

.safety-enterprise-page .filter-card { padding:14px 20px 16px; }
.safety-enterprise-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-enterprise-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-enterprise-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-enterprise-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-enterprise-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-enterprise-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-enterprise-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-enterprise-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-enterprise-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-enterprise-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-enterprise-page .filter-card .filter-buttons { display:flex; gap:8px; }

.safety-enterprise-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-enterprise-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.safety-enterprise-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.safety-enterprise-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-enterprise-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-enterprise-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-enterprise-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-enterprise-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-enterprise-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.safety-enterprise-page .field .control :deep(.el-select) { width:100%; }
.safety-enterprise-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-enterprise-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.safety-enterprise-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.safety-enterprise-page .field .control :deep(.el-date-editor) { width:100%; }
.safety-enterprise-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.safety-enterprise-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.safety-enterprise-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

.safety-enterprise-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-enterprise-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.safety-enterprise-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-enterprise-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

.safety-enterprise-page .table-wrap { overflow-x:auto; }
.safety-enterprise-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-enterprise-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-enterprise-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.safety-enterprise-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.safety-enterprise-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-enterprise-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-enterprise-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.safety-enterprise-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }

.safety-enterprise-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-enterprise-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-enterprise-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.safety-enterprise-page .badge.amber .dot { background:var(--amber-500); }
.safety-enterprise-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.safety-enterprise-page .badge.blue .dot { background:var(--blue-500); }
.safety-enterprise-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.safety-enterprise-page .badge.green .dot { background:var(--green-500); }
.safety-enterprise-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.safety-enterprise-page .badge.red .dot { background:var(--red-500); }
.safety-enterprise-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-enterprise-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.safety-enterprise-page .badge.gray .dot { background:var(--ink-400); }

.safety-enterprise-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.safety-enterprise-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.safety-enterprise-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.safety-enterprise-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; }

@media (max-width:1100px) { .safety-enterprise-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-enterprise-page .filter-card .filter-bar { grid-template-columns:1fr; } .safety-enterprise-page .toolbar { flex-wrap:wrap; gap:10px; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
