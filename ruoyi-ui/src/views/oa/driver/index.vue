<template>
  <div class="app-container oa-list-page oa-driver-page">
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
          <label>司机姓名</label>
          <div class="control">
            <el-input v-model="queryParams.driverName" placeholder="请输入司机姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.driverNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>手机号</label>
          <div class="control">
            <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in driverStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>准驾车型</label>
          <div class="control">
            <el-input v-model="queryParams.licenseType" placeholder="请输入准驾车型" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>驾驶证号</label>
          <div class="control">
            <el-input v-model="queryParams.licenseNo" placeholder="请输入驾驶证号" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>所属部门</label>
          <div class="control">
            <el-input v-model="queryParams.ownerDept" placeholder="请输入所属部门" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>驾驶证到期</label>
          <div class="control">
            <el-date-picker v-model="licenseRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>入职日期</label>
          <div class="control">
            <el-date-picker v-model="entryRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['oa:driver:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['oa:driver:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['oa:driver:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['oa:driver:export']">导出</el-button>
          <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['oa:driver:import']">导入</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="oa_driver_index"></right-toolbar>
        </div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="driverList" @selection-change="handleSelectionChange" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" width="55" align="center" />
          <el-table-column label="司机姓名" prop="driverName" align="center" width="110" v-if="columns.driverName.visible" />
          <el-table-column label="工号" prop="driverNo" align="center" width="110" v-if="columns.driverNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.driverNo || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="手机号" prop="phone" align="center" width="120" v-if="columns.phone.visible" />
          <el-table-column label="准驾车型" prop="licenseType" align="center" width="100" v-if="columns.licenseType.visible" />
          <el-table-column label="驾驶证号" prop="licenseNo" align="center" min-width="160" show-overflow-tooltip v-if="columns.licenseNo.visible" />
          <el-table-column label="驾驶证有效期至" prop="licenseExpire" align="center" width="130" v-if="columns.licenseExpire.visible" />
          <el-table-column label="入职日期" prop="entryDate" align="center" width="110" v-if="columns.entryDate.visible" />
          <el-table-column label="所属部门" prop="ownerDept" align="center" min-width="130" show-overflow-tooltip v-if="columns.ownerDept.visible" />
          <el-table-column label="驾龄(年)" prop="drivingYears" align="center" width="90" v-if="columns.drivingYears.visible" />
          <el-table-column label="状态" prop="status" align="center" width="100" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(driverStatusOptions, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['oa:driver:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['oa:driver:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['oa:driver:remove']">删除</el-button>
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
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub">{{ form.driverName ? '司机：' + form.driverName + (form.driverNo ? ' · ' + form.driverNo : '') : '维护司机基本信息、驾驶资质与联系方式' }}</div>
          </div>
        </div>
      </template>
      <el-form ref="driverRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="司机姓名" prop="driverName">
                    <el-input v-model="form.driverName" placeholder="选择人员或直接输入司机姓名">
                      <template #append><el-button icon="Search" @click="openDriverPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工号" prop="driverNo"><el-input v-model="form.driverNo" placeholder="请输入工号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号" prop="idCard"><el-input v-model="form.idCard" placeholder="请输入身份证号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属部门" prop="ownerDept"><el-input v-model="form.ownerDept" placeholder="请输入所属部门" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                      <el-option v-for="d in driverStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="11" r="2"/><path d="M15 9h3M15 13h3M7 17h10"/></svg></span>驾驶资质</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="驾驶证号" prop="licenseNo"><el-input v-model="form.licenseNo" placeholder="请输入驾驶证号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="准驾车型" prop="licenseType"><el-input v-model="form.licenseType" placeholder="如 A1、B2、C1" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="驾驶证有效期至" prop="licenseExpire">
                    <el-date-picker v-model="form.licenseExpire" type="date" value-format="YYYY-MM-DD" placeholder="请选择有效期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="入职日期" prop="entryDate">
                    <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择入职日期" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="驾龄(年)" prop="drivingYears"><el-input-number v-model="form.drivingYears" :min="0" :max="60" controls-position="right" style="width: 100%" /></el-form-item>
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
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">司机详情</div>
            <div class="rd-detail-header-sub">{{ viewData.driverName || '' }}{{ viewData.driverNo ? ' · ' + viewData.driverNo : '' }}</div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">司机姓名</span><div class="rd-value">{{ viewData.driverName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.driverNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">手机号</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">身份证号</span><div class="rd-value">{{ viewData.idCard || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.ownerDept || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="driverStatusOptions" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">驾驶证号</span><div class="rd-value">{{ viewData.licenseNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">准驾车型</span><div class="rd-value">{{ viewData.licenseType || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">驾驶证有效期至</span><div class="rd-value">{{ viewData.licenseExpire || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">入职日期</span><div class="rd-value">{{ viewData.entryDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">驾龄(年)</span><div class="rd-value">{{ viewData.drivingYears || '-' }}</div></div>
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
    <user-picker ref="driverPickerRef" title="选择司机" @confirm="onDriverPicked" />

    <!-- 导入 -->
    <ExcelImportDialog
      ref="importRef"
      title="司机台账导入"
      action="/oa/driver/importData"
      template-action="/oa/driver/importTemplate"
      template-file-name="司机台账导入模板.xlsx"
      update-support-label="如果已存在相同工号，则更新该司机数据"
      :column-labels="{ driverName: '司机姓名', driverNo: '工号', phone: '手机号', licenseType: '准驾车型', licenseNo: '驾驶证号', licenseExpire: '驾驶证有效期至', entryDate: '入职日期', ownerDept: '所属部门', drivingYears: '驾龄', status: '状态', result: '导入结果', errorReason: '失败原因' }"
      :tips="['司机姓名为必填项，工号建议填写以便重复数据识别', '状态请填写字典值（如：状态 0=在职）', '勾选“更新已存在的数据”后，相同工号的记录将被覆盖更新']"
      @success="getList"
    />
  </div>
</template>

<script setup name="OaDriver">
import { listOaDriver, getOaDriver, addOaDriver, updateOaDriver, delOaDriver } from '@/api/oa/driver'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import UserPicker from '@/components/UserPicker/index.vue'
import { listHrEmployee } from '@/api/hr/hrEmployee'

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(['e0', 'e1', 'e2', 'v0', 'v1'])
const { oa_driver_status: driverStatusOptions } = proxy.useDict('oa_driver_status')

const importRef = ref()
const driverPickerRef = ref()
const driverList = ref([])
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
const licenseRange = ref([])
const entryRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })

const columns = ref({
  driverName: { label: '司机姓名', visible: true },
  driverNo: { label: '工号', visible: true },
  phone: { label: '手机号', visible: true },
  licenseType: { label: '准驾车型', visible: true },
  licenseNo: { label: '驾驶证号', visible: false },
  licenseExpire: { label: '驾驶证有效期至', visible: true },
  entryDate: { label: '入职日期', visible: false },
  ownerDept: { label: '所属部门', visible: true },
  drivingYears: { label: '驾龄', visible: true },
  status: { label: '状态', visible: true }
})

const statusTabList = computed(() => driverStatusOptions.value)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    driverName: undefined, driverNo: undefined, phone: undefined,
    licenseType: undefined, licenseNo: undefined, ownerDept: undefined, status: undefined, params: {}
  },
  rules: {
    driverName: [{ required: true, message: '司机姓名不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.driverName) count++
  if (queryParams.value.driverNo) count++
  if (queryParams.value.phone) count++
  if (queryParams.value.licenseType) count++
  if (queryParams.value.licenseNo) count++
  if (queryParams.value.ownerDept) count++
  if (queryParams.value.status) count++
  if (licenseRange.value && licenseRange.value.length === 2) count++
  if (entryRange.value && entryRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listOaDriver(queryParams.value).then(response => {
    driverList.value = response.rows
    total.value = response.total
    loading.value = false
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, licenseRange.value, 'LicenseExpire')
  proxy.addDateRange(queryParams.value, entryRange.value, 'EntryDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    driverName: undefined, driverNo: undefined, phone: undefined,
    licenseType: undefined, licenseNo: undefined, ownerDept: undefined, status: undefined, params: {}
  }
  licenseRange.value = []
  entryRange.value = []
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
  listOaDriver(query).then(res => {
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
  ids.value = selection.map(item => item.driverId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    driverId: undefined, driverName: undefined, driverNo: undefined, phone: undefined,
    idCard: undefined, licenseNo: undefined, licenseType: undefined, licenseExpire: undefined,
    entryDate: undefined, ownerDept: undefined, drivingYears: undefined,
    status: '0', attachment: undefined, remark: undefined
  }
  proxy.resetForm('driverRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增司机'
}

function handleUpdate(row) {
  reset()
  const driverId = row.driverId || ids.value[0]
  getOaDriver(driverId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改司机'
  })
}

function handleView(row) {
  getOaDriver(row.driverId).then(response => {
    viewData.value = response.data || {}
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['driverRef'].validate(valid => {
    if (!valid) return
    if (form.value.driverId) {
      updateOaDriver(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addOaDriver(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const driverIds = row.driverId ? [row.driverId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的司机记录？').then(() => {
    return delOaDriver(driverIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 打开司机选择弹框 */
function openDriverPicker() {
  driverPickerRef.value.open()
}

/** 选择司机后自动带出部门、电话与身份证号 */
function onDriverPicked(user) {
  form.value.driverName = user.nickName
  form.value.ownerDept = user.deptName
  form.value.phone = user.phonenumber
  listHrEmployee({ userId: user.userId, pageNum: 1, pageSize: 1 }).then(res => {
    const emp = (res.rows || [])[0]
    if (emp) {
      if (emp.idCard) form.value.idCard = emp.idCard
      if (emp.employeeNo) form.value.driverNo = emp.employeeNo
    }
  }).catch(() => {})
}

function handleImport() {
  importRef.value.open()
}

function handleExport() {
  const params = { ...queryParams.value }
  proxy.addDateRange(params, licenseRange.value, 'LicenseExpire')
  proxy.addDateRange(params, entryRange.value, 'EntryDate')
  proxy.download('oa/driver/export', params, `司机台账_${new Date().getTime()}.xlsx`)
}

getList()
</script>
