<template>
  <div class="app-container safety-incident-page">
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
          <label>事故编号</label>
          <div class="control">
            <el-input v-model="queryParams.incidentCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>事故等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.incidentLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_incident_level" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>发生时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field">
          <label>事故地点</label>
          <div class="control">
            <el-input v-model="queryParams.incidentLocation" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报告人</label>
          <div class="control">
            <el-input v-model="queryParams.reportPerson" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:emergency:incident:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:emergency:incident:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:emergency:incident:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_incident_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="incidentList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="事故编号" prop="incidentCode" key="incidentCode" :width="colWidth('incidentCode', 130)" resizable v-if="columns.incidentCode.visible" />
          <el-table-column label="事故等级" prop="incidentLevel" key="incidentLevel" :width="colWidth('incidentLevel', 110)" resizable align="center" v-if="columns.incidentLevel.visible">
            <template #default="scope">
              <span class="badge" :class="incidentLevelBadgeClass(scope.row.incidentLevel)">
                <span class="dot"></span>{{ incidentLevelLabel(scope.row.incidentLevel) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="发生时间" prop="incidentDate" key="incidentDate" :width="colWidth('incidentDate', 160)" resizable align="center" sortable="custom" v-if="columns.incidentDate.visible" />
          <el-table-column label="事故地点" prop="incidentLocation" key="incidentLocation" :width="colWidth('incidentLocation', 150)" resizable show-overflow-tooltip v-if="columns.incidentLocation.visible" />
          <el-table-column label="事故描述" prop="incidentDesc" key="incidentDesc" :width="colWidth('incidentDesc', 200)" resizable show-overflow-tooltip v-if="columns.incidentDesc.visible" />
          <el-table-column label="伤亡人数" prop="casualties" key="casualties" :width="colWidth('casualties', 90)" resizable align="center" v-if="columns.casualties.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.casualties > 0 ? 'red' : 'gray'">
                <span class="dot"></span>{{ scope.row.casualties || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="经济损失" prop="economicLoss" key="economicLoss" :width="colWidth('economicLoss', 120)" resizable align="center" v-if="columns.economicLoss.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.economicLoss) }}</span></template>
          </el-table-column>
          <el-table-column label="报告人" prop="reportPerson" key="reportPerson" :width="colWidth('reportPerson', 100)" resizable v-if="columns.reportPerson.visible" />
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:emergency:incident:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:emergency:incident:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:emergency:incident:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="incidentRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>事故信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="事故编号" prop="incidentCode"><el-input v-model="form.incidentCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="事故等级" prop="incidentLevel"><el-select v-model="form.incidentLevel" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_incident_level" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="发生时间" prop="incidentDate"><el-date-picker v-model="form.incidentDate" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="事故地点" prop="incidentLocation"><el-input v-model="form.incidentLocation" placeholder="请输入事故地点" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="事故描述" prop="incidentDesc"><el-input v-model="form.incidentDesc" type="textarea" :rows="3" placeholder="请输入事故描述" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>伤亡与损失</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="伤亡人数" prop="casualties"><el-input-number v-model="form.casualties" :min="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="经济损失" prop="economicLoss"><el-input-number v-model="form.economicLoss" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>原因与整改</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="原因分析" prop="causeAnalysis"><el-input v-model="form.causeAnalysis" type="textarea" :rows="3" placeholder="请输入原因分析" /></el-form-item>
              <el-form-item prop="fourNotLetGo"><template #label><span>四不放过</span><el-tooltip content="安全事故处理的“四不放过”原则：1.事故原因未查清不放过；2.责任人员未处理不放过；3.整改措施未落实不放过；4.有关人员未受到教育不放过" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.fourNotLetGo" type="textarea" :rows="2" placeholder="请输入四不放过内容" /></el-form-item>
              <el-form-item label="整改措施" prop="rectifyMeasures"><el-input v-model="form.rectifyMeasures" type="textarea" :rows="2" placeholder="请输入整改措施" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="报告人" prop="reportPersonId">
                  <el-input v-model="form.reportPerson" readonly placeholder="请选择报告人" style="width: 100%" @click="openUserPicker">
                    <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                    <template #suffix><el-icon v-if="form.reportPerson" class="clear-icon" @click.stop="clearReportPerson"><CircleClose /></el-icon></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="报告时间" prop="reportTime"><el-date-picker v-model="form.reportTime" type="datetime" placeholder="选择日期时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
              </el-row>
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
          <span class="rd-detail-header-title">事故详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.incidentCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.incidentCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title">事故信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">事故编号</span><div class="rd-value">{{ viewData.incidentCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">事故等级</span><div class="rd-value"><dict-tag :options="safety_incident_level" :value="viewData.incidentLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">发生时间</span><div class="rd-value">{{ viewData.incidentDate || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">事故地点</span><div class="rd-value">{{ viewData.incidentLocation || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">事故描述</span><div class="rd-value">{{ viewData.incidentDesc || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.casualties != null || viewData.economicLoss != null">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title">伤亡与损失</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">伤亡人数</span><div class="rd-value">{{ viewData.casualties != null ? viewData.casualties + ' 人' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">经济损失</span><div class="rd-value">{{ viewData.economicLoss || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.causeAnalysis || viewData.rectifyMeasures">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title">原因与整改</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">原因分析</span><div class="rd-value">{{ viewData.causeAnalysis || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">整改措施</span><div class="rd-value">{{ viewData.rectifyMeasures || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.reportPersonName || viewData.reportTime || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title">其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">报告人</span><div class="rd-value">{{ viewData.reportPersonName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告时间</span><div class="rd-value">{{ viewData.reportTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>


    <!-- 报告人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择报告人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="SafetyIncident">
import { listIncident, getIncident, addIncident, updateIncident, delIncident } from '@/api/safety/incident'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatMoney } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, QuestionFilled, ArrowDown } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_incident_level } = proxy.useDict('safety_incident_level')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_incident_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3"])

const incidentList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})

const default_columns = {
  incidentCode: { label: '事故编号', visible: true },
  incidentLevel: { label: '事故等级', visible: true },
  incidentDate: { label: '发生时间', visible: true },
  incidentLocation: { label: '事故地点', visible: true },
  incidentDesc: { label: '事故描述', visible: true },
  casualties: { label: '伤亡人数', visible: true },
  economicLoss: { label: '经济损失', visible: true },
  reportPerson: { label: '报告人', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_incident_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, incidentCode: undefined, incidentLevel: undefined, incidentLocation: undefined, reportPerson: undefined, params: {} },
  rules: {
    incidentLevel: [{ required: true, message: '事故等级不能为空', trigger: 'change' }],
    incidentDate: [{ required: true, message: '事故发生时间不能为空', trigger: 'change' }],
    incidentLocation: [{ required: true, message: '事故地点不能为空', trigger: 'blur' }],
    incidentDesc: [{ required: true, message: '事故描述不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.incidentCode) count++
  if (queryParams.value.incidentLevel) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  if (queryParams.value.incidentLocation) count++
  if (queryParams.value.reportPerson) count++
  return count
})

function getList() { loading.value = true; listIncident(queryParams.value).then(response => { incidentList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.incidentCode = undefined; queryParams.value.incidentLevel = undefined; queryParams.value.incidentLocation = undefined; queryParams.value.reportPerson = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.incidentId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; collapsedCards.c3 = false; open.value = true; title.value = '添加事故记录' }
function handleView(row) { const id = row.incidentId || ids.value[0]; getIncident(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getIncident(row.incidentId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.casualties && !response.data.economicLoss; collapsedCards.c2 = !response.data.causeAnalysis && !response.data.fourNotLetGo && !response.data.rectifyMeasures; collapsedCards.c3 = !response.data.reportPerson && !response.data.reportTime && !response.data.remark; open.value = true; title.value = '修改事故记录' }) }
function submitForm() {
  proxy.$refs['incidentRef'].validate(valid => {
    if (valid) {
      if (form.value.incidentId != undefined) { updateIncident(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addIncident(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleExport() { proxy.download('safety/emergency/incident/export', { ...queryParams }, `incident_${new Date().getTime()}.xlsx`) }
function handleDelete(row) { const incidentIds = row.incidentId || ids.value; proxy.$modal.confirm('是否确认删除事故记录？').then(function() { return delIncident(incidentIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { incidentId: undefined, incidentCode: undefined, incidentLevel: undefined, incidentDate: undefined, incidentLocation: undefined, incidentDesc: undefined, casualties: 0, economicLoss: undefined, causeAnalysis: undefined, fourNotLetGo: undefined, rectifyMeasures: undefined, reportPersonId: undefined, reportPerson: undefined, reportTime: undefined, remark: undefined }
  proxy.resetForm('incidentRef')
}

/** 打开报告人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.reportPersonId) }
/** 报告人选择确认回调 */
function onUserPickerConfirm(user) { form.value.reportPersonId = user.userId; form.value.reportPerson = user.nickName }
/** 清除报告人 */
function clearReportPerson() { form.value.reportPersonId = undefined; form.value.reportPerson = undefined }

function incidentLevelLabel(level) { const item = safety_incident_level.value.find(d => d.value == level); return item ? item.label : '-' }
function incidentLevelBadgeClass(level) { const map = { '1': 'red', '2': 'red', '3': 'amber', '4': 'gray' }; return map[level] || 'gray' }

getList()
</script>

<style scoped>
.safety-incident-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-incident-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-incident-page .filter-card { padding:14px 20px 16px; }
.safety-incident-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-incident-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-incident-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-incident-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-incident-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-incident-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-incident-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-incident-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-incident-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-incident-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-incident-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-incident-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-incident-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-incident-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-incident-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-incident-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-incident-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-incident-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-incident-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-incident-page .field .control :deep(.el-select) { width:100%; }
.safety-incident-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-incident-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-incident-page .toolbar .left, .safety-incident-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-incident-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-incident-page .table-wrap { overflow-x:auto; }
.safety-incident-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-incident-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-incident-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-incident-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-incident-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-incident-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-incident-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-incident-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-incident-page .badge.amber .dot { background:var(--amber-500); }
.safety-incident-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-incident-page .badge.red .dot { background:var(--red-500); }
.safety-incident-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-incident-page .badge.gray .dot { background:var(--ink-400); }
..safety-incident-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } ..safety-incident-page .badge.blue .dot { background:var(--blue-500); }
..safety-incident-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } ..safety-incident-page .badge.green .dot { background:var(--green-500); }
..safety-incident-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-incident-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.safety-incident-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.safety-incident-page .rd-form-tip:hover { color: #909399; }
.safety-incident-page .form-value-text { display: inline-block; font-size: 14px; font-weight: 600; color: var(--ink-900); line-height: 32px; }
.safety-incident-page .form-value-text--muted { color: var(--ink-400); font-weight: 400; }
.safety-incident-page .rd-amount { font-variant-numeric: tabular-nums; }
@media (max-width:1100px) { .safety-incident-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-incident-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
</style>
