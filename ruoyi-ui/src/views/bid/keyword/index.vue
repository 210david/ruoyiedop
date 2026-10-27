<template>
  <div class="app-container bid-keyword-page">
    <!-- 筛选条件卡片 -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>关键词</label><div class="control"><el-input v-model="queryParams.keyword" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>匹配方式</label><div class="control"><el-select v-model="queryParams.matchType" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_match_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>地区</label><div class="control"><el-input v-model="queryParams.regionCode" placeholder="如：云南" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>行业</label><div class="control"><el-select v-model="queryParams.industry" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_industry" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>启用状态</label><div class="control"><el-select v-model="queryParams.enabled" placeholder="全部" clearable style="width:100%"><el-option label="启用" value="0" /><el-option label="停用" value="1" /></el-select></div></div>
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
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ tabCounts.all || 0 }}</span>
          </button>
          <button class="status-tab" :class="[statusTabClass('0'), { 'is-active': activeTab === '0' }]" @click="handleStatusTabClick('0')">
            <span class="dot"></span><span>启用</span><span class="count">{{ tabCounts['0'] || 0 }}</span>
          </button>
          <button class="status-tab" :class="[statusTabClass('1'), { 'is-active': activeTab === '1' }]" @click="handleStatusTabClick('1')">
            <span class="dot"></span><span>停用</span><span class="count">{{ tabCounts['1'] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bid:keyword:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['bid:keyword:remove']">批量删除</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="bid_keyword_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table"
          @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="关键词" prop="keyword" key="keyword" :min-width="colWidth('keyword', 200)" show-overflow-tooltip resizable v-if="columns.keyword.visible" />
          <el-table-column label="同义词" prop="synonymWords" key="synonymWords" :min-width="colWidth('synonymWords', 180)" show-overflow-tooltip resizable v-if="columns.synonymWords.visible" />
          <el-table-column label="排除词" prop="excludeWords" key="excludeWords" :min-width="colWidth('excludeWords', 180)" show-overflow-tooltip resizable v-if="columns.excludeWords.visible" />
          <el-table-column label="匹配方式" prop="matchType" key="matchType" :width="colWidth('matchType', 120)" resizable align="center" v-if="columns.matchType.visible">
            <template #default="scope"><span class="badge blue">{{ matchLabel(scope.row.matchType) }}</span></template>
          </el-table-column>
          <el-table-column label="地区" prop="regionCode" key="regionCode" :width="colWidth('regionCode', 100)" resizable align="center" v-if="columns.regionCode.visible" />
          <el-table-column label="行业" prop="industry" key="industry" :width="colWidth('industry', 110)" resizable align="center" v-if="columns.industry.visible">
            <template #default="scope"><span class="badge violet">{{ industryLabel(scope.row.industry) }}</span></template>
          </el-table-column>
          <el-table-column label="金额下限" prop="minAmount" key="minAmount" :width="colWidth('minAmount', 150)" resizable align="center" v-if="columns.minAmount.visible">
            <template #default="scope">{{ scope.row.minAmount != null ? formatWan(scope.row.minAmount) + ' 万元' : '—' }}</template>
          </el-table-column>
          <el-table-column label="启用" prop="enabled" key="enabled" :width="colWidth('enabled', 100)" resizable align="center" v-if="columns.enabled.visible">
            <template #default="scope"><span class="badge" :class="scope.row.enabled === '0' ? 'green' : 'gray'">{{ scope.row.enabled === '0' ? '启用' : '停用' }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bid:keyword:edit']">编辑</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bid:keyword:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" v-model="open" width="860px" class="rd-dialog" draggable append-to-body>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="keywordRef" :model="form" :rules="rules" label-width="130px">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>匹配规则</div></div>
          <div class="rd-card-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="关键词" prop="keyword"><el-input v-model="form.keyword" placeholder="请输入主匹配词" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="匹配方式" prop="matchType">
                  <el-radio-group v-model="form.matchType">
                    <el-radio v-for="d in bid_match_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="同义词">
                  <el-select v-model="synonymList" multiple filterable allow-create default-first-option placeholder="输入后回车创建" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排除词">
                  <el-select v-model="excludeList" multiple filterable allow-create default-first-option placeholder="输入后回车创建" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></span>过滤条件</div></div>
          <div class="rd-card-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="地区"><el-input v-model="form.regionCode" placeholder="如：云南（空=不限）" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="行业">
                  <el-select v-model="form.industry" placeholder="不限" clearable style="width:100%"><el-option v-for="d in bid_industry" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="金额下限（万元）"><el-input-number v-model="minAmountWan" :precision="4" :min="0" controls-position="right" style="width:100%" placeholder="空=不限" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="限定信息类型">
                  <el-select v-model="noticeTypeList" multiple collapse-tags collapse-tags-tooltip placeholder="空=不限" style="width:100%">
                    <el-option v-for="d in bid_notice_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>订阅范围</div></div>
          <div class="rd-card-body">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="按部门添加">
                  <div class="scope-chips">
                    <el-tag v-for="s in deptScopes" :key="'d' + s.deptId" closable @close="removeScope(s)" style="margin:0 6px 6px 0">{{ s.deptName || ('部门' + s.deptId) }}</el-tag>
                    <el-button size="small" icon="Plus" @click="openDeptPicker">选择部门</el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="按用户添加">
                  <div class="scope-chips">
                    <el-tag v-for="s in userScopes" :key="'u' + s.userId" closable type="info" @close="removeScope(s)" style="margin:0 6px 6px 0">{{ s.userName || ('用户' + s.userId) }}</el-tag>
                    <el-button size="small" icon="Plus" @click="openUserPicker">选择用户</el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="启用状态">
                  <el-radio-group v-model="form.enabled">
                    <el-radio value="0">启用</el-radio>
                    <el-radio value="1">停用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="订阅详情" v-model="detailOpen" width="860px" class="rd-dialog" draggable append-to-body>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></div>
          <span class="rd-detail-header-title">订阅详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.keyword"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ detailData.keyword }}</span></div>
        </div>
      </template>
      <div class="rd-page" v-loading="detailLoading">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>匹配规则</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">关键词</span><div class="rd-value">{{ detailData.keyword || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">匹配方式</span><div class="rd-value"><span class="badge blue">{{ matchLabel(detailData.matchType) }}</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">同义词</span><div class="rd-value">{{ detailData.synonymWords || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">排除词</span><div class="rd-value">{{ detailData.excludeWords || '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg></span>过滤条件</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">地区</span><div class="rd-value">{{ detailData.regionCode || '不限' }}</div></div>
              <div class="rd-item"><span class="rd-label">行业</span><div class="rd-value"><span v-if="detailData.industry" class="badge violet">{{ industryLabel(detailData.industry) }}</span><span v-else>不限</span></div></div>
              <div class="rd-item"><span class="rd-label">金额下限</span><div class="rd-value">{{ detailData.minAmount != null ? formatWan(detailData.minAmount) + ' 万元' : '不限' }}</div></div>
              <div class="rd-item"><span class="rd-label">限定信息类型</span><div class="rd-value"><template v-if="detailData.noticeTypes"><el-tag v-for="t in detailData.noticeTypes.split(',')" :key="t" size="small" style="margin:0 6px 6px 0">{{ noticeTypeLabel(t) }}</el-tag></template><span v-else>不限</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title">订阅范围与状态</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">推送范围</span><div class="rd-value">
                <template v-if="(detailData.scopeList || []).length">
                  <el-tag v-for="s in detailData.scopeList" :key="s.scopeId || (s.scopeType + (s.deptId || s.userId))" size="small" :type="s.scopeType === '1' ? 'primary' : 'info'" style="margin:0 6px 6px 0">{{ s.scopeType === '1' ? ('部门：' + (s.deptName || s.deptId)) : ('用户：' + (s.userName || s.userId)) }}</el-tag>
                </template>
                <span v-else>—</span>
              </div></div>
              <div class="rd-item"><span class="rd-label">启用状态</span><div class="rd-value"><span class="badge" :class="detailData.enabled === '0' ? 'green' : 'gray'">{{ detailData.enabled === '0' ? '启用' : '停用' }}</span></div></div>
              <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ detailData.remark || '—' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 业务操作说明弹窗 -->
    <el-dialog title="关键词订阅业务操作说明" v-model="showStatusHelp" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">关键词订阅是什么？</div>
          <div class="highlight-card-body"><p>关键词订阅用于定义「哪些公告值得推送」。系统按订阅规则对采集到的公告做匹配，命中的公告按订阅范围推送给对应部门/用户。</p></div>
        </div>
        <h4>二、命中逻辑</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>① 命中：</strong>关键词与同义词任一命中即视为匹配</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>② 剔除：</strong>排除词命中则剔除该公告</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>③ 过滤：</strong>再依次过滤地区、行业、金额下限、限定信息类型（留空均表示不限）</el-timeline-item>
        </el-timeline>
        <h4>三、订阅范围</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">按部门 / 按用户添加</div>
          <div class="highlight-card-body"><p>按部门添加时该部门下所有用户均接收推送；按用户添加时仅指定用户接收。两者可叠加（如"经营部全员 + 分管副总个人"）。</p></div>
        </div>
        <h4>四、操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>新增订阅：</strong>设置匹配规则与过滤条件，选择订阅范围</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>启用：</strong>启用后自动参与公告匹配，可随时停用或调整</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>

    <!-- 部门选择 -->
    <DeptPicker ref="deptPickerRef" :multiple="true" @confirm="handleDeptConfirm" />
    <!-- 用户选择 -->
    <UserPicker ref="userPickerRef" :multiple="true" @confirm="handleUserConfirm" />
  </div>
</template>

<script setup name="BidKeyword">
import { listKeyword, getKeyword, addKeyword, updateKeyword, delKeyword } from '@/api/bid/keyword'
import { useColumnResize } from '@/composables/useColumnResize'
import { formatWan, yuanToWan, wanToYuan } from '@/composables/useDetailCard'
import DeptPicker from '@/components/DeptPicker'
import UserPicker from '@/components/UserPicker'

const { proxy } = getCurrentInstance()
const { bid_match_type, bid_industry, bid_notice_type } = proxy.useDict('bid_match_type', 'bid_industry', 'bid_notice_type')
function noticeTypeLabel(v) {
  if (v == null || v === '') return '不限'
  const d = bid_notice_type.value.find(x => x.value == v)
  return d ? d.label : v
}
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_keyword_index')

const showAdvanced = ref(false)
const showSearch = ref(true)
const loading = ref(true)
const list = ref([])
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const open = ref(false)
const title = ref('')
const activeTab = ref('all')
const tabCounts = ref({})
const showStatusHelp = ref(false)
const deptPickerRef = ref(null)
const userPickerRef = ref(null)
const synonymList = ref([])
const excludeList = ref([])
const noticeTypeList = ref([])

const defaultColumns = {
  keyword: { label: '关键词', visible: true },
  synonymWords: { label: '同义词', visible: true },
  excludeWords: { label: '排除词', visible: true },
  matchType: { label: '匹配方式', visible: true },
  regionCode: { label: '地区', visible: true },
  industry: { label: '行业', visible: true },
  minAmount: { label: '金额下限', visible: true },
  enabled: { label: '启用', visible: true }
}
function loadColumnVisibility() { try { const saved = localStorage.getItem('bid_keyword_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, keyword: undefined, matchType: undefined, regionCode: undefined, industry: undefined, enabled: undefined, params: {} },
  rules: {
    keyword: [{ required: true, message: '关键词不能为空', trigger: 'blur' }],
    matchType: [{ required: true, message: '匹配方式不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 金额下限：存储单位为元，与公告预算保持一致以万元录入/展示
const minAmountWan = computed({
  get: () => yuanToWan(form.value.minAmount),
  set: v => { form.value.minAmount = wanToYuan(v) }
})

const deptScopes = computed(() => (form.value.scopeList || []).filter(s => s.scopeType === '1'))
const userScopes = computed(() => (form.value.scopeList || []).filter(s => s.scopeType === '2'))

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['keyword', 'matchType', 'regionCode', 'industry', 'enabled'].filter(k => q[k]).length
})

function matchLabel(v) { const d = bid_match_type.value.find(x => x.value == v); return d ? d.label : '—' }
function industryLabel(v) { const d = bid_industry.value.find(x => x.value == v); return d ? d.label : '—' }
function statusTabClass(v) { return v === '0' ? 'tab-approved' : 'tab-void' }

function getList() {
  loading.value = true
  listKeyword(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
    computeTabCounts()
  })
}
function computeTabCounts() {
  // 全量计数（不受当前筛选影响）：轻量拉取一次
  listKeyword({ pageNum: 1, pageSize: 1 }).then(res => {
    const all = res.total || 0
    listKeyword({ pageNum: 1, pageSize: 1, enabled: '0' }).then(r0 => {
      listKeyword({ pageNum: 1, pageSize: 1, enabled: '1' }).then(r1 => {
        tabCounts.value = { all, '0': r0.total || 0, '1': r1.total || 0 }
      })
    })
  })
}
function handleStatusTabClick(v) {
  activeTab.value = v
  queryParams.value.enabled = v === 'all' ? undefined : v
  queryParams.value.pageNum = 1
  getList()
}
function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, keyword: undefined, matchType: undefined,
    regionCode: undefined, industry: undefined, enabled: undefined, params: {} }
  activeTab.value = 'all'
  getList()
}
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.keywordId)
  multiple.value = !selection.length
}

function reset() {
  form.value = { keywordId: undefined, keyword: undefined, synonymWords: undefined, excludeWords: undefined,
    matchType: '1', regionCode: undefined, industry: undefined, minAmount: undefined,
    noticeTypes: undefined, notifyEnabled: '1', enabled: '0', scopeList: [] }
  synonymList.value = []
  excludeList.value = []
  noticeTypeList.value = []
  proxy.resetForm('keywordRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增关键词订阅' }
const detailOpen = ref(false)
const detailLoading = ref(false)
const detailData = ref({})
function handleDetail(row) {
  detailOpen.value = true
  detailLoading.value = true
  detailData.value = {}
  getKeyword(row.keywordId).then(res => { detailData.value = res.data }).finally(() => { detailLoading.value = false })
}
function handleUpdate(row) {
  reset()
  getKeyword(row.keywordId).then(res => {
    form.value = res.data
    synonymList.value = res.data.synonymWords ? res.data.synonymWords.split(',') : []
    excludeList.value = res.data.excludeWords ? res.data.excludeWords.split(',') : []
    noticeTypeList.value = res.data.noticeTypes ? res.data.noticeTypes.split(',') : []
    open.value = true
    title.value = '编辑关键词订阅'
  })
}
function submitForm() {
  proxy.$refs['keywordRef'].validate(v => {
    if (v) {
      form.value.synonymWords = synonymList.value.join(',')
      form.value.excludeWords = excludeList.value.join(',')
      form.value.noticeTypes = noticeTypeList.value.join(',')
      const api = form.value.keywordId != null ? updateKeyword : addKeyword
      api(form.value).then(() => {
        proxy.$modal.msgSuccess(form.value.keywordId != null ? '修改成功' : '新增成功')
        open.value = false
        getList()
      })
    }
  })
}
function cancel() { open.value = false; reset() }

function handleDelete(row) {
  const keywordIds = row && row.keywordId ? [row.keywordId] : ids.value
  if (!keywordIds.length) return
  const label = row && row.keywordId ? '关键词「' + row.keyword + '」' : '选中的 ' + keywordIds.length + ' 条订阅'
  proxy.$modal.confirm('确认删除' + label + '？').then(() => delKeyword(keywordIds)).then(() => {
    getList(); proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function openDeptPicker() { deptPickerRef.value.open() }
function handleDeptConfirm(selection) {
  const arr = Array.isArray(selection) ? selection : [selection]
  arr.forEach(d => {
    if (d.deptId && !(form.value.scopeList || []).some(s => s.scopeType === '1' && s.deptId == d.deptId)) {
      form.value.scopeList = [...(form.value.scopeList || []), { scopeType: '1', deptId: Number(d.deptId), deptName: d.deptName }]
    }
  })
}
function openUserPicker() { userPickerRef.value.open() }
function handleUserConfirm(selection) {
  const arr = Array.isArray(selection) ? selection : [selection]
  arr.forEach(u => {
    if (u.userId && !(form.value.scopeList || []).some(s => s.scopeType === '2' && s.userId == u.userId)) {
      form.value.scopeList = [...(form.value.scopeList || []), { scopeType: '2', userId: Number(u.userId), userName: u.nickName || u.userName }]
    }
  })
}
function removeScope(s) {
  form.value.scopeList = (form.value.scopeList || []).filter(x => x !== s)
}

getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-keyword-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --violet-50:#f5f3ff; --violet-500:#8b5cf6;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.bid-keyword-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-keyword-page .filter-head { display:flex; align-items:center; justify-content:space-between; padding:12px 20px 0; }
.bid-keyword-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-keyword-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-keyword-page .adv-link { display:inline-flex; align-items:center; gap:4px; color:var(--ink-500); cursor:pointer; font-size:13px; }
.bid-keyword-page .adv-link:hover { color:var(--brand-600); }
.bid-keyword-page .adv-link.is-open .chev { transform:rotate(180deg); }
.bid-keyword-page .adv-link .chev { transition:transform .2s; }
.bid-keyword-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-keyword-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-keyword-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-keyword-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-keyword-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-100); }
.bid-keyword-page .toolbar .left { display:flex; gap:10px; }
.bid-keyword-page .table-wrap { padding:0; }
.bid-keyword-page .pagination-container { display:flex; justify-content:flex-end; padding:12px 20px; }

/* 状态标签栏 */
.bid-keyword-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.bid-keyword-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.bid-keyword-page .tabs-track::-webkit-scrollbar { display:none; }
.bid-keyword-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s; white-space:nowrap; border:1px solid transparent; background:transparent; }
.bid-keyword-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.bid-keyword-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.bid-keyword-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.bid-keyword-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.bid-keyword-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.bid-keyword-page .status-tab.is-active .dot { background:var(--brand-500); }
.bid-keyword-page .status-tab.tab-approved .dot { background:var(--green-500); }
.bid-keyword-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.bid-keyword-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.bid-keyword-page .status-tab.tab-void .dot { background:var(--ink-400); }
.bid-keyword-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:28px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; color:var(--ink-500); font-size:12px; cursor:pointer; white-space:nowrap; }
.bid-keyword-page .tip-pill:hover { color:var(--brand-600); border-color:var(--brand-200); }

/* 徽章 */
.bid-keyword-page .badge { display:inline-flex; align-items:center; gap:5px; padding:2px 10px; border-radius:999px; font-size:12px; line-height:1.6; white-space:nowrap; }
.bid-keyword-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.bid-keyword-page .badge.green { background:var(--green-50); color:var(--green-700); }
.bid-keyword-page .badge.green .dot { background:var(--green-500); }
.bid-keyword-page .badge.gray { background:var(--ink-100); color:var(--ink-500); }
.bid-keyword-page .badge.gray .dot { background:var(--ink-400); }
.bid-keyword-page .badge.blue { background:var(--blue-50); color:var(--blue-700); }
.bid-keyword-page .badge.violet { background:var(--violet-50); color:#7c3aed; }

/* 操作列 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* 弹窗卡片 */
.bid-keyword-page .rd-card { border:1px solid var(--ink-200); border-radius:10px; margin-bottom:14px; overflow:hidden; }
.bid-keyword-page .rd-card-header { padding:10px 16px; background:var(--ink-50); border-bottom:1px solid var(--ink-200); }
.bid-keyword-page .rd-card-title { font-size:14px; font-weight:600; }
.bid-keyword-page .rd-card-body { padding:16px 16px 0; }
.bid-keyword-page .rd-card-body .el-form-item:last-child { margin-bottom:16px; }
.scope-chips { display:flex; flex-wrap:wrap; align-items:center; }

/* 帮助弹窗 */
.help-body h4 { margin:12px 0 6px; font-size:14px; color:var(--ink-900); }
.help-body p { margin:0 0 8px; font-size:13px; color:var(--ink-500); line-height:1.7; }

@media (max-width:1100px) { .bid-keyword-page .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .bid-keyword-page .filter-bar { grid-template-columns:1fr; } }
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
.bid-keyword-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; }
.bid-keyword-page .tip-pill:hover { background:#fffbeb; border-color:#f59e0b; }
</style>
