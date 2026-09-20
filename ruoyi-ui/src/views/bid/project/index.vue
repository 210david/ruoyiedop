<template>
  <div class="app-container bid-project-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>投标编号</label><div class="control"><el-input v-model="queryParams.bidNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>项目名称/招标人</label><div class="control"><el-input v-model="queryParams.projectName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>投标状态</label><div class="control"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_project_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>负责人</label><div class="control"><el-input v-model="queryParams.ownerName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>地区</label><div class="control"><el-input v-model="queryParams.regionCode" placeholder="如：云南" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>行业分类</label><div class="control"><el-select v-model="queryParams.industry" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_industry" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>承接方式</label><div class="control"><el-select v-model="queryParams.joinType" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_join_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>递交截止</label><div class="control"><el-date-picker v-model="deadlineRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="handleTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button>
          <button v-for="s in mainTabs" :key="s.value" class="status-tab" :class="[tabClass(s.value), { 'is-active': activeTab === s.value }]" @click="handleTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bid:project:add']">新增投标项目</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['bid:project:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="投标编号" prop="bidNo" key="bidNo" width="180" align="center" sortable="custom" :sort-orders="['descending','ascending']" />
          <el-table-column label="项目名称" prop="projectName" key="projectName" :min-width="colWidth('projectName', 260)" show-overflow-tooltip resizable />
          <el-table-column label="状态" prop="status" key="status" width="120" align="center">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)">{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="我方报价" prop="bidAmount" key="bidAmount" width="130" align="center" sortable="custom" :sort-orders="['descending','ascending']">
            <template #default="scope">{{ scope.row.bidAmount ? '￥' + formatAmount(scope.row.bidAmount) : '—' }}</template>
          </el-table-column>
          <el-table-column label="递交截止" prop="submitDeadline" key="submitDeadline" width="180" align="center" sortable="custom" :sort-orders="['descending','ascending']">
            <template #default="scope"><span v-if="scope.row.submitDeadline" :class="deadlineClass(scope.row.submitDeadline, scope.row.status)">{{ String(scope.row.submitDeadline).slice(0,16) }}</span><span v-else>—</span></template>
          </el-table-column>
          <el-table-column label="负责人" prop="ownerName" key="ownerName" width="110" align="center" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Switch" @click="handleChangeStatus(scope.row)" v-if="nextStatusOptions(scope.row.status).length" v-hasPermi="['bid:project:edit']">流转</el-button>
                <el-button link type="success" icon="Promotion" @click="handleToPms(scope.row)" v-if="scope.row.status === '7'" v-hasPermi="['bid:project:edit']">转立项</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['bid:project:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 状态流转弹窗 -->
    <el-dialog v-model="statusOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg></div><span class="rd-detail-header-title">状态流转</span></div>
      </template>
      <el-form label-width="110px">
        <el-form-item label="目标状态" required>
          <el-select v-model="statusForm.status" placeholder="请选择" style="width:100%">
            <el-option v-for="s in nextStatusOptions(statusRow.status)" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因/备注" :required="['8','9','10'].includes(statusForm.status)">
          <el-input v-model="statusForm.reason" type="textarea" :rows="3" placeholder="未中标/废标/放弃时必填" show-word-limit maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitStatus">确 定</el-button><el-button @click="statusOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.bidNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ form.bidNo }}</span></div>
        </div>
      </template>
      <el-form ref="projectRef" :model="form" :rules="rules" label-width="110px">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="投标编号" prop="bidNo"><el-input v-model="form.bidNo" placeholder="自动生成" disabled /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="关联公告ID" prop="noticeId"><el-input v-model="form.noticeId" placeholder="从公告库「转投标」自动带入，可留空" /></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="项目名称" prop="projectName"><el-input v-model="form.projectName" placeholder="请输入投标项目名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="招标人" prop="tenderee"><el-input v-model="form.tenderee" placeholder="请输入" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="代理机构" prop="agent"><el-input v-model="form.agent" placeholder="请输入" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="承接方式" prop="joinType">
                <el-radio-group v-model="form.joinType"><el-radio v-for="d in bid_join_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio></el-radio-group>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="联营合作方" prop="partner" v-if="form.joinType === '2'"><el-input v-model="form.partner" placeholder="联营时必填" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="负责人" prop="ownerUserId">
                <el-input v-model="form.ownerName" readonly placeholder="请选择负责人" style="width:100%" @click="openUserPicker">
                  <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                  <template #suffix><el-icon v-if="form.ownerName" class="clear-icon" @click.stop="clearOwner"><CircleClose /></el-icon></template>
                </el-input>
              </el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>关键时间与金额</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="报名截止" prop="applyDeadline"><el-date-picker v-model="form.applyDeadline" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="递交截止" prop="submitDeadline"><el-date-picker v-model="form.submitDeadline" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="开标时间" prop="openTime"><el-date-picker v-model="form.openTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="项目预算" prop="budgetAmount"><el-input-number v-model="form.budgetAmount" :precision="2" :min="0" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="我方报价" prop="bidAmount"><el-input-number v-model="form.bidAmount" :precision="2" :min="0" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="行业分类" prop="industry"><el-select v-model="form.industry" placeholder="请选择" clearable style="width:100%"><el-option v-for="d in bid_industry" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div>
          <span class="rd-detail-header-title">投标详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.bidNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ detailData.bidNo }}</span></div>
        </div>
      </template>
      <div class="rd-page" v-loading="detailLoading">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">项目名称</span><div class="rd-value">{{ detailData.projectName }}</div></div>
              <div class="rd-item"><span class="rd-label">投标编号</span><div class="rd-value">{{ detailData.bidNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="badgeClass(detailData.status)">{{ statusLabel(detailData.status) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">招标人</span><div class="rd-value">{{ detailData.tenderee || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">代理机构</span><div class="rd-value">{{ detailData.agent || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">项目预算</span><div class="rd-value rd-amount">{{ detailData.budgetAmount ? '￥' + formatAmount(detailData.budgetAmount) : '￥0.00' }}</div></div>
              <div class="rd-item"><span class="rd-label">我方报价</span><div class="rd-value rd-amount rd-value--large">{{ detailData.bidAmount ? '￥' + formatAmount(detailData.bidAmount) : '￥0.00' }}</div></div>
              <div class="rd-item"><span class="rd-label">报名截止</span><div class="rd-value">{{ detailData.applyDeadline || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">递交截止</span><div class="rd-value">{{ detailData.submitDeadline || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">开标时间</span><div class="rd-value">{{ detailData.openTime || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">负责人</span><div class="rd-value">{{ detailData.ownerName || '—' }}</div></div>
              <div class="rd-item" v-if="detailData.failReason"><span class="rd-label">未中标原因</span><div class="rd-value">{{ failLabel(detailData.failReason) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></span>进度节点</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="detailData.nodeList || []" border align="center">
              <el-table-column label="节点" prop="nodeName" width="130" align="center"><template #default="scope">{{ nodeLabel(scope.row.nodeType) }}</template></el-table-column>
              <el-table-column label="计划时间" prop="planTime" width="170" align="center"><template #default="scope">{{ (scope.row.planTime || '—').slice(0,16) }}</template></el-table-column>
              <el-table-column label="实际完成" prop="actualTime" width="170" align="center"><template #default="scope">{{ (scope.row.actualTime || '—').slice(0,16) }}</template></el-table-column>
              <el-table-column label="状态" width="100" align="center"><template #default="scope"><span class="badge" :class="scope.row.finishFlag === '1' ? 'green' : 'amber'">{{ scope.row.finishFlag === '1' ? '已完成' : '进行中' }}</span></template></el-table-column>
              <el-table-column label="卡点说明" prop="blockDesc" show-overflow-tooltip align="center" />
              <el-table-column label="操作" width="100" align="center">
                <template #default="scope">
                  <el-button link type="primary" @click="handleFinishNode(scope.row)" v-if="scope.row.finishFlag !== '1'" v-hasPermi="['bid:project:edit']">完成</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="(detailData.nodeList || []).length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>节点时间线</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="node in detailData.nodeList" :key="node.nodeId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': node.finishFlag === '1' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ nodeLabel(node.nodeType) }}</span>
                    <span class="rd-timeline-time">{{ node.actualTime || node.planTime || '' }}</span>
                  </div>
                  <div class="rd-timeline-body"><div class="rd-item"><span class="rd-label">说明</span><div class="rd-value">{{ node.blockDesc || (node.finishFlag === '1' ? '已完成' : '进行中') }}</div></div></div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <el-dialog v-model="showStatusHelp" title="投标管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">投标项目台账是什么？</div>
          <div class="highlight-card-body"><p>投标项目台账管理从商机确认到中标/未中标的全过程，包含进度节点跟踪、截止预警与保证金联动，是投标经营的核心单据。</p></div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="info">草稿</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">线索确认</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag type="warning">已报名</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag type="warning">已购标</el-tag></div>
          <div class="flow-item"><el-tag type="warning">标书编制中</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag type="primary">已递交</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag type="primary">已开标</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag type="success">中标</el-tag></div>
          <div class="flow-item"><el-tag type="danger">未中标/废标/已放弃</el-tag></div>
        </div>
        <h4>三、操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>登记商机：</strong>从公告库「转投标」或手动新增</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>过程跟进：</strong>按节点推进并「流转」状态，临近截止自动预警</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>结果闭环：</strong>开标后登记中标/未中标；中标项目可「转立项」</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>

    <user-picker ref="userPickerRef" title="选择投标负责人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="BidProject">
import { listProject, getProject, addProject, updateProject, delProject, changeProjectStatus, finishNode, toPms } from '@/api/bid/project'
import { useColumnResize } from '@/composables/useColumnResize'
import { formatAmount } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const { bid_project_status, bid_industry, bid_join_type, bid_fail_reason, bid_node_type } = proxy.useDict('bid_project_status', 'bid_industry', 'bid_join_type', 'bid_fail_reason', 'bid_node_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_project_index')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const showSearch = ref(true)
const showAdvanced = ref(false)
const deadlineRange = ref([])
const activeTab = ref('all')
const statusCounts = ref({})
const showStatusHelp = ref(false)
const detailOpen = ref(false)
const detailLoading = ref(false)
const detailData = ref({})
const statusOpen = ref(false)
const statusRow = ref({})
const statusForm = ref({ status: undefined, reason: undefined })
const userPickerRef = ref(null)

const mainTabs = [
  { value: '1', label: '进行中' }, { value: '6', label: '已开标' }, { value: '7', label: '中标' }, { value: '8', label: '未中标' }, { value: '10', label: '已放弃' }
]

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, bidNo: undefined, projectName: undefined, status: undefined, ownerName: undefined, regionCode: undefined, industry: undefined, joinType: undefined, params: {} },
  rules: {
    projectName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
    ownerUserId: [{ required: true, message: '投标负责人不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['bidNo','projectName','status','ownerName','regionCode','industry','joinType'].filter(k => q[k]).length + (deadlineRange.value.length ? 1 : 0)
})

function statusLabel(v) { const d = bid_project_status.value.find(x => x.value == v); return d ? d.label : v }
function failLabel(v) { const d = bid_fail_reason.value.find(x => x.value == v); return d ? d.label : v }
function nodeLabel(v) { const d = bid_node_type.value.find(x => x.value == v); return d ? d.label : v }
function badgeClass(v) { return { '0': 'amber', '1': 'amber', '2': 'blue', '3': 'blue', '4': 'blue', '5': 'violet', '6': 'violet', '7': 'green', '8': 'gray', '9': 'red', '10': 'red' }[v] || 'gray' }
function tabClass(v) { return badgeClass(v) === 'gray' ? '' : '' }
function deadlineClass(t, status) {
  if (!t || ['7','8','9','10'].includes(String(status))) return ''
  const days = (new Date(t).getTime() - Date.now()) / 86400000
  if (days <= 3) return 'deadline-red'
  if (days <= 7) return 'deadline-amber'
  return ''
}
const NEXT = { '0': ['1','10'], '1': ['2','10'], '2': ['3','10'], '3': ['4','10'], '4': ['5','10'], '5': ['6','10'], '6': ['7','8','9'] }
function nextStatusOptions(status) {
  return (NEXT[status] || []).map(v => ({ value: v, label: statusLabel(v) }))
}

function getList() {
  loading.value = true
  listProject(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
  loadCounts()
}
function loadCounts() {
  listProject({ pageNum: 1, pageSize: 1 }).then(() => {})
  // 状态计数：轻量按状态查询（仅取 total）
  const statuses = ['all','1','6','7','8','10']
  statuses.forEach(s => {
    const q = { pageNum: 1, pageSize: 1, status: s === 'all' || s === '1' ? undefined : s }
    if (s === '1') { q.params = { ongoing: '1' } }
    listProject(q).then(res => { statusCounts.value[s] = res.total })
  })
}
function handleTabClick(tab) { activeTab.value = tab; queryParams.value.status = tab === 'all' ? undefined : tab; handleQuery() }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, deadlineRange.value, 'SubmitDeadline'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; deadlineRange.value = []; activeTab.value = 'all'; handleQuery() }
function handleSortChange(column) {
  if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' }
  else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }
  getList()
}

function reset() {
  form.value = { projectId: undefined, bidNo: undefined, noticeId: undefined, projectName: undefined, tenderee: undefined, agent: undefined,
    joinType: '1', partner: undefined, ownerUserId: undefined, ownerName: undefined, applyDeadline: undefined, submitDeadline: undefined,
    openTime: undefined, budgetAmount: undefined, bidAmount: undefined, industry: undefined, remark: undefined, nodeList: [] }
  proxy.resetForm('projectRef')
}
function handleAdd() { reset(); open.value = true; title.value = '新增投标项目' }
function handleDetail(row) {
  detailData.value = {}; archiveListReset()
  detailOpen.value = true
  detailLoading.value = true
  getProject(row.projectId).then(res => { detailData.value = res.data }).finally(() => { detailLoading.value = false })
}
function archiveListReset() {}

function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.ownerUserId) }
function onUserPickerConfirm(user) {
  form.value.ownerUserId = user.userId
  form.value.ownerName = user.nickName
  if (user.deptId) { form.value.deptId = user.deptId }
}
function clearOwner() { form.value.ownerUserId = undefined; form.value.ownerName = undefined }

function submitForm() {
  proxy.$refs['projectRef'].validate(v => {
    if (v) {
      if (form.value.projectId != null) {
        updateProject(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addProject(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function cancel() { open.value = false; reset() }
function handleDelete(row) {
  proxy.$modal.confirm('确认删除投标项目「' + row.bidNo + '」？').then(() => delProject(row.projectId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

function handleChangeStatus(row) {
  statusRow.value = row
  statusForm.value = { status: undefined, reason: undefined }
  statusOpen.value = true
}
function submitStatus() {
  if (!statusForm.value.status) { proxy.$modal.msgError('请选择目标状态'); return }
  if (['8','9','10'].includes(statusForm.value.status) && !statusForm.value.reason) { proxy.$modal.msgError('该状态流转必须填写原因'); return }
  changeProjectStatus(statusRow.value.projectId, statusForm.value.status, statusForm.value.reason).then(() => {
    proxy.$modal.msgSuccess('状态流转成功'); statusOpen.value = false; getList()
  })
}
function handleFinishNode(node) {
  proxy.$modal.confirm('确认完成节点「' + nodeLabel(node.nodeType) + '」？可在确认后补充卡点说明。').then(() => finishNode(node.nodeId, '')).then(() => {
    proxy.$modal.msgSuccess('节点已完成')
    getProject(detailData.value.projectId).then(res => { detailData.value = res.data })
  }).catch(() => {})
}
function handleToPms(row) {
  proxy.$modal.confirm('确认为中标项目「' + row.bidNo + '」发起项目立项？').then(() => toPms(row.projectId)).then(res => {
    proxy.$modal.msgSuccess(res.msg)
  }).catch(() => {})
}
function handleExport() {
  proxy.download('bid/project/export', { ...proxy.addDateRange(queryParams.value, deadlineRange.value, 'SubmitDeadline') }, `project_${new Date().getTime()}.xlsx`)
}
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-project-page { padding-top:10px; --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.bid-project-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-project-page .filter-head { display:flex; align-items:center; justify-content:space-between; padding:12px 20px 0; }
.bid-project-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-project-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-project-page .adv-link { display:inline-flex; align-items:center; gap:4px; color:var(--ink-500); cursor:pointer; font-size:13px; }
.bid-project-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-project-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-project-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-project-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-project-page .filter-buttons { display:flex; gap:8px; }
.bid-project-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.bid-project-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.bid-project-page .tabs-track::-webkit-scrollbar { display:none; }
.bid-project-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; white-space:nowrap; border:1px solid transparent; background:transparent; }
.bid-project-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.bid-project-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.bid-project-page .status-tab:hover { background:var(--ink-50); }
.bid-project-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.bid-project-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; }
.tip-pill:hover { background:#fffbeb; border-color:#f59e0b; }
.bid-project-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; }
.bid-project-page .toolbar .left { display:flex; gap:8px; }
.bid-project-page .pagination-container { padding:10px 12px; }
.bid-project-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bid-project-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bid-project-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bid-project-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.bid-project-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.bid-project-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bid-project-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.bid-project-page .deadline-red { color:var(--red-500); font-weight:600; }
.bid-project-page .deadline-amber { color:var(--amber-700); font-weight:600; }
.clear-icon { cursor:pointer; color:#c0c4cc; font-size:14px; }
.clear-icon:hover { color:#909399; }
:deep(.col-action) { padding:6px 4px !important; }
:deep(.col-action .cell) { display:flex; justify-content:center; padding:0; }
.action-btn-row { display:inline-flex; flex-wrap:wrap; justify-content:center; gap:0; }
:deep(.col-action .el-button) { padding:2px 4px; margin:0 2px; white-space:nowrap; justify-content:center; }
:deep(.col-action .el-button + .el-button) { margin-left:2px; }
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
</style>
