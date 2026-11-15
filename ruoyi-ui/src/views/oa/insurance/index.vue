<template>
  <div class="app-container oa-list-page oa-insurance-page">
    <!-- 筛选条件 -->
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
          <label>保单号</label>
          <div class="control">
            <el-input v-model="queryParams.policyNo" placeholder="请输入保单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>车牌号</label>
          <div class="control">
            <el-input v-model="queryParams.plateNo" placeholder="请输入车牌号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>保险类型</label>
          <div class="control">
            <el-select v-model="queryParams.insuranceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in insuranceTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>保险公司</label>
          <div class="control">
            <el-input v-model="queryParams.insurer" placeholder="请输入保险公司" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>联系人</label>
          <div class="control">
            <el-input v-model="queryParams.contactName" placeholder="请输入联系人" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in insuranceStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>起保日期</label>
          <div class="control">
            <el-date-picker v-model="startDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>到期日期</label>
          <div class="control">
            <el-date-picker v-model="endDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 列表 -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['oa:insurance:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['oa:insurance:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['oa:insurance:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['oa:insurance:export']">导出</el-button>
          <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['oa:insurance:import']">导入</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="oa_insurance_index"></right-toolbar>
        </div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="insuranceList" @selection-change="handleSelectionChange" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" width="55" align="center" />
          <el-table-column label="保单号" prop="policyNo" align="center" min-width="160" show-overflow-tooltip v-if="columns.policyNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.policyNo }}</span></template>
          </el-table-column>
          <el-table-column label="车牌号" prop="plateNo" align="center" width="120" v-if="columns.plateNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.plateNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="保险类型" prop="insuranceType" align="center" width="130" v-if="columns.insuranceType.visible">
            <template #default="scope">
              <span v-if="scope.row.insuranceType" class="badge blue"><span class="dot"></span>{{ dictLabel(insuranceTypeOptions, scope.row.insuranceType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="保险公司" prop="insurer" align="center" min-width="150" show-overflow-tooltip v-if="columns.insurer.visible" />
          <el-table-column label="起保日期" prop="startDate" align="center" width="110" v-if="columns.startDate.visible" />
          <el-table-column label="到期日期" prop="endDate" align="center" width="110" v-if="columns.endDate.visible" />
          <el-table-column label="保费(元)" prop="premium" align="center" width="110" v-if="columns.premium.visible" />
          <el-table-column label="保额(元)" prop="insuredAmount" align="center" width="120" v-if="columns.insuredAmount.visible" />
          <el-table-column label="联系人" prop="contactName" align="center" width="100" v-if="columns.contactName.visible" />
          <el-table-column label="联系电话" prop="contactPhone" align="center" width="120" v-if="columns.contactPhone.visible" />
          <el-table-column label="状态" prop="status" align="center" width="100" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(insuranceStatusOptions, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['oa:insurance:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['oa:insurance:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['oa:insurance:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改 -->
    <el-dialog v-model="open" width="880px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub">{{ form.policyNo ? '保单号：' + form.policyNo : '维护保单信息、保期费用与联系人信息' }}</div>
          </div>
        </div>
      </template>
      <el-form ref="insuranceRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>保单信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="保单号" prop="policyNo"><el-input v-model="form.policyNo" placeholder="请输入保单号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="投保单号" prop="insuranceNo"><el-input v-model="form.insuranceNo" placeholder="请输入投保单号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="车牌号" prop="plateNo"><el-input v-model="form.plateNo" placeholder="请输入车牌号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保险类型" prop="insuranceType">
                    <el-select v-model="form.insuranceType" placeholder="请选择保险类型" style="width: 100%">
                      <el-option v-for="d in insuranceTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保险公司" prop="insurer"><el-input v-model="form.insurer" placeholder="请输入保险公司" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                      <el-option v-for="d in insuranceStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>保期与费用</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="起保日期" prop="startDate">
                    <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择起保日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="到期日期" prop="endDate">
                    <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择到期日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保费(元)" prop="premium"><el-input-number v-model="form.premium" :min="0" :precision="2" :controls="false" placeholder="请输入保费" style="width: 100%" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保额(元)" prop="insuredAmount"><el-input-number v-model="form.insuredAmount" :min="0" :precision="2" :controls="false" placeholder="请输入保额" style="width: 100%" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系人" prop="contactName">
                    <el-input v-model="form.contactName" placeholder="选择人员或直接输入联系人">
                      <template #append><el-button icon="Search" @click="openContactPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入联系电话" /></el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>附件与备注</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-form-item label="附件" prop="attachment">
                <file-upload v-model="form.attachment" :limit="3" :fileSize="50" />
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="viewOpen" width="880px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">车辆保险详情</div>
            <div class="rd-detail-header-sub">{{ viewData.policyNo || '' }}{{ viewData.plateNo ? ' · ' + viewData.plateNo : '' }}</div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>保单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">保单号</span><div class="rd-value">{{ viewData.policyNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">投保单号</span><div class="rd-value">{{ viewData.insuranceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">车牌号</span><div class="rd-value">{{ viewData.plateNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">保险类型</span><div class="rd-value"><dict-tag :options="insuranceTypeOptions" :value="viewData.insuranceType" /></div></div>
              <div class="rd-item"><span class="rd-label">保险公司</span><div class="rd-value">{{ viewData.insurer || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="insuranceStatusOptions" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">起保日期</span><div class="rd-value">{{ viewData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ viewData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">保费(元)</span><div class="rd-value">{{ viewData.premium ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">保额(元)</span><div class="rd-value">{{ viewData.insuredAmount ?? '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewData.contactName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.contactPhone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联流程</span><div class="rd-value">{{ viewData.flowInstanceId || '未关联' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.attachment">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>附件</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v1">
            <div class="rd-file-links">
              <a class="rd-file-link" v-for="(url, idx) in String(viewData.attachment).split(',')" :key="idx" :href="url" target="_blank" rel="noopener">
                <span class="rd-file-name">{{ url }}</span>
              </a>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 人员选择 -->
    <user-picker ref="contactPickerRef" title="选择联系人" @confirm="onContactPicked" />

    <!-- 导入 -->
    <ExcelImportDialog
      ref="importRef"
      title="车辆保险台账导入"
      action="/oa/insurance/importData"
      template-action="/oa/insurance/importTemplate"
      template-file-name="车辆保险台账导入模板.xlsx"
      update-support-label="如果已存在相同保单号，则更新该保险数据"
      :column-labels="{ policyNo: '保单号', insuranceNo: '投保单号', plateNo: '车牌号', insuranceType: '保险类型', insurer: '保险公司', startDate: '起保日期', endDate: '到期日期', premium: '保费(元)', insuredAmount: '保额(元)', contactName: '联系人', contactPhone: '联系电话', status: '状态', result: '导入结果', errorReason: '失败原因' }"
      :tips="['保单号为必填项，且用于识别重复数据', '保险类型、状态请填写字典值（如：保险类型 1=交强险，状态 0=有效）', '勾选“更新已存在的数据”后，相同保单号的记录将被覆盖更新']"
      @success="getList"
    />
  </div>
</template>

<script setup name="OaInsurance">
import { listOaInsurance, getOaInsurance, addOaInsurance, updateOaInsurance, delOaInsurance } from '@/api/oa/insurance'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(['e0', 'e1', 'e2', 'v0', 'v1'])
const { oa_insurance_type: insuranceTypeOptions } = proxy.useDict('oa_insurance_type')
const { oa_insurance_status: insuranceStatusOptions } = proxy.useDict('oa_insurance_status')

const importRef = ref()
const contactPickerRef = ref()
const insuranceList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const title = ref('')
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const startDateRange = ref([])
const endDateRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })

const columns = ref({
  policyNo: { label: '保单号', visible: true },
  plateNo: { label: '车牌号', visible: true },
  insuranceType: { label: '保险类型', visible: true },
  insurer: { label: '保险公司', visible: true },
  startDate: { label: '起保日期', visible: true },
  endDate: { label: '到期日期', visible: true },
  premium: { label: '保费', visible: true },
  insuredAmount: { label: '保额', visible: false },
  contactName: { label: '联系人', visible: false },
  contactPhone: { label: '联系电话', visible: true },
  status: { label: '状态', visible: true }
})

const statusTabList = computed(() => insuranceStatusOptions.value)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    policyNo: undefined, plateNo: undefined, insuranceType: undefined,
    insurer: undefined, contactName: undefined, status: undefined, params: {}
  },
  rules: {
    policyNo: [{ required: true, message: '保单号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.policyNo) count++
  if (queryParams.value.plateNo) count++
  if (queryParams.value.insuranceType) count++
  if (queryParams.value.insurer) count++
  if (queryParams.value.contactName) count++
  if (queryParams.value.status) count++
  if (startDateRange.value && startDateRange.value.length === 2) count++
  if (endDateRange.value && endDateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listOaInsurance(queryParams.value).then(response => {
    insuranceList.value = response.rows
    total.value = response.total
    loading.value = false
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, startDateRange.value, 'StartDate')
  proxy.addDateRange(queryParams.value, endDateRange.value, 'EndDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    policyNo: undefined, plateNo: undefined, insuranceType: undefined,
    insurer: undefined, contactName: undefined, status: undefined, params: {}
  }
  startDateRange.value = []
  endDateRange.value = []
  activeStatusTab.value = 'all'
  getList()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.status = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  const query = { ...queryParams.value, pageNum: 1, pageSize: 9999, status: undefined, params: { ...queryParams.value.params } }
  listOaInsurance(query).then(res => {
    const counts = { all: 0, '0': 0, '1': 0, '2': 0 }
    const rows = res.rows || []
    rows.forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    counts.all = rows.length
    statusCounts.value = counts
  }).catch(() => {})
}

function statusTabClass(value) { return { '0': 'tab-green', '1': 'tab-amber', '2': 'tab-red' }[value] || '' }
function statusBadgeClass(value) { return { '0': 'green', '1': 'amber', '2': 'red' }[value] || 'gray' }
function dictLabel(options, v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (options || []).find(o => String(o.value) === String(v))
  return d ? d.label : String(v)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.insuranceId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    insuranceId: undefined, policyNo: undefined, insuranceNo: undefined, vehicleId: undefined,
    plateNo: undefined, insuranceType: undefined, insurer: undefined, startDate: undefined,
    endDate: undefined, premium: undefined, insuredAmount: undefined, contactName: undefined,
    contactPhone: undefined, status: '0', attachment: undefined, remark: undefined
  }
  proxy.resetForm('insuranceRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增车辆保险'
}

function handleUpdate(row) {
  reset()
  const insuranceId = row.insuranceId || ids.value[0]
  getOaInsurance(insuranceId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改车辆保险'
  })
}

function handleView(row) {
  getOaInsurance(row.insuranceId).then(response => {
    viewData.value = response.data || {}
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['insuranceRef'].validate(valid => {
    if (!valid) return
    if (form.value.insuranceId) {
      updateOaInsurance(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addOaInsurance(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const insuranceIds = row.insuranceId ? [row.insuranceId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的车辆保险记录？').then(() => {
    return delOaInsurance(insuranceIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 打开联系人选择弹框 */
function openContactPicker() {
  contactPickerRef.value.open()
}

/** 选择联系人后自动带出联系电话 */
function onContactPicked(user) {
  form.value.contactName = user.nickName
  form.value.contactPhone = user.phonenumber
}

function handleImport() {
  importRef.value.open()
}

function handleExport() {
  const params = { ...queryParams.value }
  proxy.addDateRange(params, startDateRange.value, 'StartDate')
  proxy.addDateRange(params, endDateRange.value, 'EndDate')
  proxy.download('oa/insurance/export', params, `车辆保险台账_${new Date().getTime()}.xlsx`)
}

getList()
</script>
