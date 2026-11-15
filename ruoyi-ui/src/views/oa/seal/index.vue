<template>
  <div class="app-container oa-list-page oa-seal-page">
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
          <label>印章名称</label>
          <div class="control">
            <el-input v-model="queryParams.sealName" placeholder="请输入印章名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>印章编号</label>
          <div class="control">
            <el-input v-model="queryParams.sealCode" placeholder="请输入印章编号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>印章类型</label>
          <div class="control">
            <el-select v-model="queryParams.sealType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sealTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>保管人</label>
          <div class="control">
            <el-input v-model="queryParams.keeperName" placeholder="请输入保管人" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sealStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>保管部门</label>
          <div class="control">
            <el-input v-model="queryParams.keeperDept" placeholder="请输入保管部门" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>存放地点</label>
          <div class="control">
            <el-input v-model="queryParams.keepPlace" placeholder="请输入存放地点" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>启用日期</label>
          <div class="control">
            <el-date-picker v-model="enableDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['oa:seal:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['oa:seal:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['oa:seal:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['oa:seal:export']">导出</el-button>
          <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['oa:seal:import']">导入</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="oa_seal_index"></right-toolbar>
        </div>
      </div>
      <div class="table-wrap">
        <el-table v-loading="loading" :data="sealList" @selection-change="handleSelectionChange" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" width="55" align="center" />
          <el-table-column label="印章名称" prop="sealName" align="center" min-width="140" show-overflow-tooltip v-if="columns.sealName.visible" />
          <el-table-column label="印章编号" prop="sealCode" align="center" width="130" v-if="columns.sealCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.sealCode || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="印章类型" prop="sealType" align="center" width="120" v-if="columns.sealType.visible">
            <template #default="scope">
              <span v-if="scope.row.sealType" class="badge blue"><span class="dot"></span>{{ dictLabel(sealTypeOptions, scope.row.sealType) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="保管人" prop="keeperName" align="center" width="100" v-if="columns.keeperName.visible" />
          <el-table-column label="保管部门" prop="keeperDept" align="center" min-width="130" show-overflow-tooltip v-if="columns.keeperDept.visible" />
          <el-table-column label="联系电话" prop="keeperPhone" align="center" width="120" v-if="columns.keeperPhone.visible" />
          <el-table-column label="存放地点" prop="keepPlace" align="center" min-width="130" show-overflow-tooltip v-if="columns.keepPlace.visible" />
          <el-table-column label="启用日期" prop="enableDate" align="center" width="110" v-if="columns.enableDate.visible" />
          <el-table-column label="状态" prop="status" align="center" width="100" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(sealStatusOptions, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['oa:seal:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['oa:seal:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['oa:seal:remove']">删除</el-button>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub">{{ form.sealCode ? '印章编号：' + form.sealCode : '维护印章基本信息、保管信息与使用状态' }}</div>
          </div>
        </div>
      </template>
      <el-form ref="sealRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="印章名称" prop="sealName"><el-input v-model="form.sealName" placeholder="请输入印章名称" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="印章编号" prop="sealCode"><el-input v-model="form.sealCode" placeholder="请输入印章编号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="印章类型" prop="sealType">
                    <el-select v-model="form.sealType" placeholder="请选择印章类型" style="width: 100%">
                      <el-option v-for="d in sealTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态" prop="status">
                    <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                      <el-option v-for="d in sealStatusOptions" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>保管信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="保管人" prop="keeperName">
                    <el-input v-model="form.keeperName" placeholder="选择人员或直接输入保管人">
                      <template #append><el-button icon="Search" @click="openKeeperPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="保管部门" prop="keeperDept"><el-input v-model="form.keeperDept" placeholder="请输入保管部门" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="keeperPhone"><el-input v-model="form.keeperPhone" placeholder="请输入联系电话" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="存放地点" prop="keepPlace"><el-input v-model="form.keepPlace" placeholder="请输入存放地点" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="启用日期" prop="enableDate">
                    <el-date-picker v-model="form.enableDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择启用日期" style="width: 100%" />
                  </el-form-item>
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
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">印章详情</div>
            <div class="rd-detail-header-sub">{{ viewData.sealName || '' }}{{ viewData.sealCode ? ' · ' + viewData.sealCode : '' }}</div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" style="display:block" v-show="!collapsedCards.v0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">印章名称</span><div class="rd-value">{{ viewData.sealName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">印章编号</span><div class="rd-value">{{ viewData.sealCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">印章类型</span><div class="rd-value"><dict-tag :options="sealTypeOptions" :value="viewData.sealType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="sealStatusOptions" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">保管人</span><div class="rd-value">{{ viewData.keeperName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">保管部门</span><div class="rd-value">{{ viewData.keeperDept || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.keeperPhone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">存放地点</span><div class="rd-value">{{ viewData.keepPlace || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">启用日期</span><div class="rd-value">{{ viewData.enableDate || '-' }}</div></div>
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
    <user-picker ref="keeperPickerRef" title="选择保管人" @confirm="onKeeperPicked" />

    <!-- 导入 -->
    <ExcelImportDialog
      ref="importRef"
      title="印章台账导入"
      action="/oa/seal/importData"
      template-action="/oa/seal/importTemplate"
      template-file-name="印章台账导入模板.xlsx"
      update-support-label="如果已存在相同印章编号，则更新该印章数据"
      :column-labels="{ sealName: '印章名称', sealCode: '印章编号', sealType: '印章类型', keeperName: '保管人', keeperDept: '保管部门', keeperPhone: '联系电话', keepPlace: '存放地点', enableDate: '启用日期', status: '状态', result: '导入结果', errorReason: '失败原因' }"
      :tips="['印章名称为必填项，印章编号建议填写以便重复数据识别', '印章类型、状态请填写字典值（如：印章类型 1=公章，状态 0=正常）', '勾选“更新已存在的数据”后，相同印章编号的记录将被覆盖更新']"
      @success="getList"
    />
  </div>
</template>

<script setup name="OaSeal">
import { listOaSeal, getOaSeal, addOaSeal, updateOaSeal, delOaSeal } from '@/api/oa/seal'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const { collapsedCards, toggleCard } = useDetailCard(['e0', 'e1', 'e2', 'v0', 'v1'])
const { oa_seal_type: sealTypeOptions } = proxy.useDict('oa_seal_type')
const { oa_seal_status: sealStatusOptions } = proxy.useDict('oa_seal_status')

const importRef = ref()
const keeperPickerRef = ref()
const sealList = ref([])
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
const enableDateRange = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })

const columns = ref({
  sealName: { label: '印章名称', visible: true },
  sealCode: { label: '印章编号', visible: true },
  sealType: { label: '印章类型', visible: true },
  keeperName: { label: '保管人', visible: true },
  keeperDept: { label: '保管部门', visible: true },
  keeperPhone: { label: '联系电话', visible: false },
  keepPlace: { label: '存放地点', visible: true },
  enableDate: { label: '启用日期', visible: true },
  status: { label: '状态', visible: true }
})

const statusTabList = computed(() => sealStatusOptions.value)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1, pageSize: 10,
    sealName: undefined, sealCode: undefined, sealType: undefined,
    keeperName: undefined, keeperDept: undefined, keepPlace: undefined,
    status: undefined, params: {}
  },
  rules: {
    sealName: [{ required: true, message: '印章名称不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.sealName) count++
  if (queryParams.value.sealCode) count++
  if (queryParams.value.sealType) count++
  if (queryParams.value.keeperName) count++
  if (queryParams.value.keeperDept) count++
  if (queryParams.value.keepPlace) count++
  if (queryParams.value.status) count++
  if (enableDateRange.value && enableDateRange.value.length === 2) count++
  return count
})

function getList() {
  loading.value = true
  listOaSeal(queryParams.value).then(response => {
    sealList.value = response.rows
    total.value = response.total
    loading.value = false
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, enableDateRange.value, 'EnableDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1, pageSize: queryParams.value.pageSize,
    sealName: undefined, sealCode: undefined, sealType: undefined,
    keeperName: undefined, keeperDept: undefined, keepPlace: undefined,
    status: undefined, params: {}
  }
  enableDateRange.value = []
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
  listOaSeal(query).then(res => {
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
  ids.value = selection.map(item => item.sealId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    sealId: undefined, sealName: undefined, sealCode: undefined, sealType: undefined,
    keeperName: undefined, keeperDept: undefined, keeperPhone: undefined,
    keepPlace: undefined, enableDate: undefined, status: '0', attachment: undefined, remark: undefined
  }
  proxy.resetForm('sealRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增印章'
}

function handleUpdate(row) {
  reset()
  const sealId = row.sealId || ids.value[0]
  getOaSeal(sealId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改印章'
  })
}

function handleView(row) {
  getOaSeal(row.sealId).then(response => {
    viewData.value = response.data || {}
    viewOpen.value = true
  })
}

function submitForm() {
  proxy.$refs['sealRef'].validate(valid => {
    if (!valid) return
    if (form.value.sealId) {
      updateOaSeal(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList()
      })
    } else {
      addOaSeal(form.value).then(() => {
        proxy.$modal.msgSuccess('新增成功')
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const sealIds = row.sealId ? [row.sealId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的印章记录？').then(() => {
    return delOaSeal(sealIds.join(','))
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 打开保管人选择弹框 */
function openKeeperPicker() {
  keeperPickerRef.value.open()
}

/** 选择保管人后自动带出部门与联系电话 */
function onKeeperPicked(user) {
  form.value.keeperName = user.nickName
  form.value.keeperDept = user.deptName
  form.value.keeperPhone = user.phonenumber
}

function handleImport() {
  importRef.value.open()
}

function handleExport() {
  proxy.download('oa/seal/export', proxy.addDateRange({ ...queryParams.value }, enableDateRange.value, 'EnableDate'), `印章台账_${new Date().getTime()}.xlsx`)
}

getList()
</script>
