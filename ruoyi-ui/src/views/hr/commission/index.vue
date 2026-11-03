<template>
  <div class="app-container hr-commission-page">
    <el-tabs v-model="activeTab" class="commission-tabs">
      <el-tab-pane label="提成方案" name="plan" />
      <el-tab-pane label="提成核算表" name="bill" />
    </el-tabs>

    <!-- ===== 方案 Pane ===== -->
    <div v-show="activeTab === 'plan'">
      <div class="surface">
        <div class="toolbar">
          <div class="left">
            <el-button type="primary" plain icon="Plus" @click="handleAddPlan" v-hasPermi="['hr:commission:add']">新增方案</el-button>
          </div>
          <div class="right">
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getPlanList" :columns="planColumns" storageKey="hr_commission_plan_columns" />
          </div>
        </div>
        <div class="table-wrap">
          <el-table ref="planTableRef" border v-loading="loading" :data="planList" @header-dragend="onPlanHeaderDragEnd" class="app-table">
            <el-table-column type="index" label="序号" key="序号" :width="colWidthPlan('序号', 70)" align="center" resizable />
            <el-table-column label="方案名称" prop="planName" key="planName" :min-width="colWidthPlan('planName', 190)" resizable show-overflow-tooltip v-if="planColumns.planName.visible" />
            <el-table-column label="计算口径" prop="calcBase" key="calcBase" :width="colWidthPlan('calcBase', 110)" resizable align="center" v-if="planColumns.calcBase.visible"><template #default="scope"><span v-if="dictLabel(baseOptions, scope.row.calcBase)" class="badge blue"><span class="dot"></span>{{ dictLabel(baseOptions, scope.row.calcBase) }}</span></template></el-table-column>
            <el-table-column label="类型" prop="planType" key="planType" :width="colWidthPlan('planType', 110)" resizable align="center" v-if="planColumns.planType.visible"><template #default="scope"><span v-if="dictLabel(typeOptions, scope.row.planType)" class="badge gray"><span class="dot"></span>{{ dictLabel(typeOptions, scope.row.planType) }}</span></template></el-table-column>
            <el-table-column label="状态" prop="planStatus" key="planStatus" :width="colWidthPlan('planStatus', 100)" resizable align="center" v-if="planColumns.planStatus.visible"><template #default="scope"><span class="badge" :class="planStatusClass(scope.row.planStatus)"><span class="dot"></span>{{ planStatusLabel(scope.row.planStatus) }}</span></template></el-table-column>
            <el-table-column label="锁定" prop="locked" key="locked" :width="colWidthPlan('locked', 90)" resizable align="center" v-if="planColumns.locked.visible"><template #default="scope"><span v-if="scope.row.locked === '1'" class="badge red"><span class="dot"></span>已锁定</span><span v-else>-</span></template></el-table-column>
            <el-table-column label="操作" width="240" align="center" fixed="right" class-name="col-action">
              <template #default="scope">
                <div class="action-btn-row">
                  <el-button link type="primary" icon="Edit" @click="handleEditPlan(scope.row)" v-hasPermi="['hr:commission:edit']" v-if="scope.row.locked !== '1'">编辑</el-button>
                  <el-button link :type="scope.row.planStatus === '1' ? 'warning' : 'success'" @click="togglePlan(scope.row)" v-hasPermi="['hr:commission:edit']">{{ scope.row.planStatus === '1' ? '停用' : '启用' }}</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelPlan(scope.row)" v-hasPermi="['hr:commission:remove']" v-if="scope.row.locked !== '1'">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <!-- ===== 核算表 Pane ===== -->
    <div v-show="activeTab === 'bill'">
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
            <label>核算周期</label>
            <div class="control is-select">
              <el-date-picker v-model="billQuery.period" type="month" value-format="YYYY-MM" placeholder="选择月份" clearable style="width: 100%" @change="getBillList" />
            </div>
          </div>
        </div>
        <div class="filter-actions">
          <div class="filter-info">
            <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件
            <button class="tip-pill" style="margin-left:8px" @click="showStatusHelp = true">
              <el-icon><QuestionFilled /></el-icon>
              <span>核算规则说明</span>
            </button>
          </div>
          <div class="filter-buttons">
            <el-button icon="RefreshLeft" @click="resetBillQuery">重置</el-button>
            <el-button type="primary" icon="Search" @click="getBillList">搜索</el-button>
          </div>
        </div>
      </div>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:8px"><template #title>MR-01：核算表生成后方案按生成时快照计算，锁定周期后关联方案自动锁定不可追溯修改；MR-03：已锁定核算表不可删除，保留历史供审计</template></el-alert>
      <div class="surface">
        <div class="toolbar">
          <div class="left">
            <el-button type="primary" plain icon="Cpu" @click="handleGenerate" v-hasPermi="['hr:commission:generate']">生成核算表</el-button>
            <button type="button" class="btn-soft is-outline" @click="handleLock" v-hasPermi="['hr:commission:confirm']">
              <el-icon><Lock /></el-icon> 锁定周期
            </button>
            <button type="button" class="btn-soft is-danger-outline" @click="handleDelBills" v-hasPermi="['hr:commission:remove']">
              <el-icon><Delete /></el-icon> 删除待确认
            </button>
          </div>
          <div class="right">
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getBillList" :columns="billColumns" storageKey="hr_commission_bill_columns" />
          </div>
        </div>
        <div class="table-wrap">
          <el-table ref="billTableRef" border v-loading="loading" :data="billList" @header-dragend="onBillHeaderDragEnd" class="app-table">
            <el-table-column type="index" label="序号" key="序号" :width="colWidthBill('序号', 70)" align="center" resizable />
            <el-table-column label="核算周期" prop="period" key="period" :width="colWidthBill('period', 100)" resizable align="center" v-if="billColumns.period.visible" />
            <el-table-column label="员工姓名" prop="employeeName" key="employeeName" :width="colWidthBill('employeeName', 110)" resizable v-if="billColumns.employeeName.visible" />
            <el-table-column label="提成方案" prop="planName" key="planName" :min-width="colWidthBill('planName', 170)" resizable show-overflow-tooltip v-if="billColumns.planName.visible" />
            <el-table-column label="计算口径" prop="calcBase" key="calcBase" :width="colWidthBill('calcBase', 110)" resizable align="center" v-if="billColumns.calcBase.visible"><template #default="scope"><span v-if="dictLabel(baseOptions, scope.row.calcBase)" class="badge blue"><span class="dot"></span>{{ dictLabel(baseOptions, scope.row.calcBase) }}</span></template></el-table-column>
            <el-table-column label="计算基数" prop="baseAmount" key="baseAmount" :width="colWidthBill('baseAmount', 130)" resizable align="right" v-if="billColumns.baseAmount.visible"><template #default="scope"><span>￥{{ formatAmount(scope.row.baseAmount) }}</span></template></el-table-column>
            <el-table-column label="提成金额" prop="commissionAmount" key="commissionAmount" :width="colWidthBill('commissionAmount', 130)" resizable align="right" v-if="billColumns.commissionAmount.visible"><template #default="scope"><span class="amount-strong">￥{{ formatAmount(scope.row.commissionAmount) }}</span></template></el-table-column>
            <el-table-column label="状态" prop="billStatus" key="billStatus" :width="colWidthBill('billStatus', 100)" resizable align="center" v-if="billColumns.billStatus.visible"><template #default="scope"><span class="badge" :class="billStatusClass(scope.row.billStatus)"><span class="dot"></span>{{ billStatusLabel(scope.row.billStatus) }}</span></template></el-table-column>
            <el-table-column label="确认人" prop="confirmer" key="confirmer" :width="colWidthBill('confirmer', 100)" resizable v-if="billColumns.confirmer.visible" />
            <el-table-column label="操作" width="110" align="center" fixed="right" class-name="col-action">
              <template #default="scope">
                <div class="action-btn-row">
                  <el-button link type="success" icon="CircleCheck" @click="handleConfirm(scope.row)" v-hasPermi="['hr:commission:confirm']" v-if="scope.row.billStatus === '0'">确认</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <pagination v-show="billTotal > 0" :total="billTotal" v-model:page="billQuery.pageNum" v-model:limit="billQuery.pageSize" @pagination="getBillList" />
      </div>
    </div>
    <!-- 方案编辑 Dialog -->
    <el-dialog v-model="planOpen" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ planTitle }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">MR-01：方案锁定后不可追溯修改；核算表生成后方案自动锁定</span></div>
          </div>
        </div>
      </template>
      <el-form ref="planRef" :model="planForm" :rules="planRules" label-width="130px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('pc0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.pc0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.pc0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="方案名称" prop="planName"><el-input v-model="planForm.planName" placeholder="请输入方案名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计算口径" prop="calcBase">
                  <el-select v-model="planForm.calcBase" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in baseOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="方案类型" prop="planType">
                  <el-select v-model="planForm.planType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="固定比例(%)" prop="baseRatio"><el-input-number v-model="planForm.baseRatio" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20" v-if="planForm.planType === '2'">
                <el-col :span="12"><el-form-item label="超额奖励起点"><el-input-number v-model="planForm.bonusThreshold" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="超额奖励比例(%)"><el-input-number v-model="planForm.bonusRatio" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="备注"><el-input v-model="planForm.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
          <section class="rd-card" v-if="planForm.planType === '1'">
            <div class="rd-card-header" @click="toggleCard('pc1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>阶梯明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.pc1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.pc1">
              <div style="margin-bottom:8px;">
                <el-button type="primary" plain icon="Plus" size="small" @click="addTier">添加阶梯</el-button>
              </div>
              <el-table :data="planForm.tiers" size="small" border>
                <el-table-column label="区间下限" width="170"><template #default="scope"><el-input-number v-model="scope.row.minAmount" :min="0" :precision="2" style="width: 100%" /></template></el-table-column>
                <el-table-column label="区间上限（空=不限）" width="190"><template #default="scope"><el-input-number v-model="scope.row.maxAmount" :min="0" :precision="2" style="width: 100%" /></template></el-table-column>
                <el-table-column label="提成比例(%)" width="140"><template #default="scope"><el-input-number v-model="scope.row.ratio" :min="0" :max="100" :precision="2" style="width: 100%" /></template></el-table-column>
                <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" @click="planForm.tiers.splice(scope.$index, 1)">删除</el-button></template></el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitPlan">确 定</el-button><el-button @click="planOpen = false">取 消</el-button></div></template>
    </el-dialog>
    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="提成核算业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、提成方案释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是提成方案？</div>
          <div class="highlight-card-body">
            <strong>提成方案</strong>定义营销人员的提成计算规则：MR-01 计算口径可选「回款口径」或「签单口径」，方案锁定后不可追溯修改；计算类型支持固定比例、阶梯比例、超额奖励三种。方案启用后在营销档案中关联到员工，锁定后自动不可编辑删除。
          </div>
        </div>
        <h4>二、核算表生成规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核算口径与快照</div>
          <div class="highlight-card-body">
            <p>• 依据「业绩归集」页的月度快照 + 员工档案关联的提成方案计算</p>
            <p>• <strong>回款口径：</strong>核算基数取该员工当期回款金额</p>
            <p>• <strong>签单口径：</strong>核算基数取该员工当期合计业绩（成交 + 补录）</p>
            <p>• 核算口径与金额为生成时快照，后续归集变化不影响已生成的核算表</p>
            <p>• 幂等：重新生成前自动清理该周期「待确认」记录（已锁定周期不允许重新生成）</p>
          </div>
        </div>
        <h4>三、确认与锁定（MR-03）</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">确认与锁定流程</div>
          <div class="highlight-card-body">
            <p>• <strong>待确认：</strong>生成后的初始状态，可逐条确认或删除重算</p>
            <p>• <strong>已确认：</strong>财务/人力逐条确认</p>
            <p>• <strong>已锁定：</strong>周期内全部确认后锁定周期；MR-01 锁定后关联提成方案自动锁定、不可追溯修改；已锁定核算表不可删除，保留历史供审计</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="HrCommission">
import { listPlan, getPlan, addPlan, updatePlan, delPlan, listBill, generateBills, confirmBill, lockBills, delBills } from '@/api/hr/commission'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowDown } from '@element-plus/icons-vue'
const { colWidthPlan, onPlanHeaderDragEnd, planTableRef, applyPlanSavedWidths } = useColumnResize('hr_commission_plan')
const { colWidthBill, onBillHeaderDragEnd, billTableRef, applyBillSavedWidths } = useColumnResize('hr_commission_bill')
const { collapsedCards, toggleCard } = useDetailCard(['pc0','pc1'])
const { proxy } = getCurrentInstance()
const { hr_commission_base: baseOptions } = proxy.useDict('hr_commission_base')
const { hr_commission_type: typeOptions } = proxy.useDict('hr_commission_type')

const activeTab = ref('plan')
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const loading = ref(false)
const planList = ref([])
const billList = ref([])
const billTotal = ref(0)
const billQuery = ref({ pageNum: 1, pageSize: 10, period: undefined })
const planOpen = ref(false)
const planTitle = ref('')
const planForm = ref({ tiers: [] })
const planRules = {
  planName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  calcBase: [{ required: true, message: '请选择计算口径', trigger: 'change' }],
  planType: [{ required: true, message: '请选择方案类型', trigger: 'change' }]
}

function loadCols(key, defaults) {
  try {
    const saved = localStorage.getItem(key)
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaults).forEach(k => { result[k] = { label: defaults[k].label, visible: parsed[k] !== undefined ? parsed[k] : defaults[k].visible } })
      return result
    }
  } catch (e) {}
  return JSON.parse(JSON.stringify(defaults))
}
const planColumns = ref(loadCols('hr_commission_plan_columns', {
  planName: { label: '方案名称', visible: true },
  calcBase: { label: '计算口径', visible: true },
  planType: { label: '类型', visible: true },
  planStatus: { label: '状态', visible: true },
  locked: { label: '锁定', visible: true }
}))
const billColumns = ref(loadCols('hr_commission_bill_columns', {
  period: { label: '核算周期', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  planName: { label: '提成方案', visible: true },
  calcBase: { label: '计算口径', visible: true },
  baseAmount: { label: '计算基数', visible: true },
  commissionAmount: { label: '提成金额', visible: true },
  billStatus: { label: '状态', visible: true },
  confirmer: { label: '确认人', visible: true }
}))
const activeFilterCount = computed(() => {
  let count = 0
  if (billQuery.value.period) count++
  return count
})

const planStatusMap = { '0': { label: '草稿', cls: 'gray' }, '1': { label: '启用', cls: 'green' }, '2': { label: '停用', cls: 'amber' } }
const planStatusLabel = (v) => planStatusMap[v] ? planStatusMap[v].label : (v || '-')
const planStatusClass = (v) => planStatusMap[v] ? planStatusMap[v].cls : 'gray'
const billStatusMap = { '0': { label: '待确认', cls: 'amber' }, '1': { label: '已确认', cls: 'green' }, '2': { label: '已锁定', cls: 'gray' } }
const billStatusLabel = (v) => billStatusMap[v] ? billStatusMap[v].label : (v || '-')
const billStatusClass = (v) => billStatusMap[v] ? billStatusMap[v].cls : 'gray'

function formatAmount(v) { return v != null ? Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '0.00' }
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function getPlanList() {
  loading.value = true
  listPlan({ pageNum: 1, pageSize: 100 }).then(res => { planList.value = res.rows; loading.value = false; applyPlanSavedWidths() })
}
function getBillList() {
  loading.value = true
  listBill(billQuery.value).then(res => { billList.value = res.rows; billTotal.value = res.total; loading.value = false; applyBillSavedWidths() })
}
function resetBillQuery() { billQuery.value.period = undefined; getBillList() }
function handleAddPlan() { planForm.value = { planName: undefined, calcBase: undefined, planType: '0', baseRatio: undefined, bonusThreshold: undefined, bonusRatio: undefined, tiers: [], remark: undefined }; planTitle.value = '新增提成方案'; planOpen.value = true }
function handleEditPlan(row) { getPlan(row.planId).then(res => { planForm.value = res.data; planTitle.value = '修改提成方案'; planOpen.value = true }) }
function addTier() { planForm.value.tiers.push({ minAmount: 0, maxAmount: undefined, ratio: undefined }) }
function submitPlan() {
  proxy.$refs['planRef'].validate(valid => {
    if (valid) {
      if (planForm.value.planId) { updatePlan(planForm.value).then(() => { proxy.$modal.msgSuccess('修改成功'); planOpen.value = false; getPlanList() }) }
      else { addPlan(planForm.value).then(() => { proxy.$modal.msgSuccess('新增成功'); planOpen.value = false; getPlanList() }) }
    }
  })
}
function togglePlan(row) {
  const target = row.planStatus === '1' ? '2' : '1'
  updatePlan({ planId: row.planId, planStatus: target }).then(() => { proxy.$modal.msgSuccess(target === '1' ? '已启用' : '已停用'); getPlanList() })
}
function handleDelPlan(row) {
  proxy.$modal.confirm('确认删除方案 "' + row.planName + '"？').then(() => delPlan(row.planId)).then(() => { proxy.$modal.msgSuccess('删除成功'); getPlanList() }).catch(() => {})
}
function handleGenerate() {
  const period = billQuery.value.period || new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 7)
  proxy.$modal.confirm('确认对 ' + period + ' 周期生成提成核算表？将依据月度归集快照与各员工提成方案计算（MR-01 口径快照）。').then(() => generateBills(period)).then(res => {
    proxy.$modal.msgSuccess('已生成 ' + res.data + ' 条核算记录')
    getBillList()
  }).catch(() => {})
}
function handleConfirm(row) {
  proxy.$modal.confirm('确认该核算记录？').then(() => confirmBill(row.billId)).then(() => { proxy.$modal.msgSuccess('已确认'); getBillList() }).catch(() => {})
}
function handleLock() {
  const period = billQuery.value.period
  if (!period) { proxy.$modal.msgError('请先选择核算周期'); return }
  proxy.$modal.confirm('确认锁定 ' + period + ' 周期核算表？锁定后关联提成方案自动锁定不可追溯修改（MR-01/MR-03）。').then(() => lockBills(period)).then(() => { proxy.$modal.msgSuccess('已锁定'); getBillList() }).catch(() => {})
}
function handleDelBills() {
  const period = billQuery.value.period
  if (!period) { proxy.$modal.msgError('请先选择核算周期'); return }
  proxy.$modal.confirm('确认删除 ' + period + ' 周期待确认核算表？').then(() => delBills(period)).then(() => { proxy.$modal.msgSuccess('删除成功'); getBillList() }).catch(() => {})
}
getPlanList()
getBillList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.hr-commission-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.hr-commission-page .commission-tabs { margin-bottom: 8px; }
.hr-commission-page .amount-strong { font-weight: 600; color: var(--brand-700); }
.hr-commission-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.hr-commission-page .filter-card { padding:14px 20px 16px; }
.hr-commission-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-commission-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-commission-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-commission-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-commission-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-commission-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-commission-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-commission-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-commission-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-commission-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-commission-page .filter-card .filter-buttons { display:flex; gap:8px; }
.hr-commission-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-commission-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.hr-commission-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-commission-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-commission-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-commission-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-commission-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-commission-page .field .control :deep(.el-select) { width:100%; }
.hr-commission-page .field .control :deep(.el-date-editor) { width:100%; }
.hr-commission-page .field .control :deep(.el-date-editor .el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-commission-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-commission-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-commission-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-commission-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-commission-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-commission-page .btn-soft .el-icon { font-size:14px; }
.hr-commission-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-commission-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-commission-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-commission-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-commission-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-commission-page .table-wrap { overflow-x:auto; }
.hr-commission-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-commission-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-commission-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-commission-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-commission-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-commission-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-commission-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .hr-commission-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-commission-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-commission-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.hr-commission-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.hr-commission-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-commission-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.hr-commission-page .badge.amber .dot { background:#f59e0b; }
.hr-commission-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-commission-page .badge.blue .dot { background:var(--brand-500); }
.hr-commission-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-commission-page .badge.green .dot { background:var(--green-500); }
.hr-commission-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-commission-page .badge.gray .dot { background:var(--ink-400); }
.hr-commission-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-commission-page .badge.red .dot { background:var(--red-500); }
/* ===== Tip Pill ===== */
.hr-commission-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:28px; padding:0 10px; font-size:13px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.hr-commission-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 520px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.status-help-content .flow-item { display: flex; align-items: center; gap: 8px; }
.status-help-content .flow-arrow { color: #909399; font-size: 16px; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-card-body p { margin: 4px 0; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
