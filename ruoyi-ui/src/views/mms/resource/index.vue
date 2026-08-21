<template>
  <div class="app-container mms-resource-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>资源编码</label><div class="control"><el-input v-model="queryParams.resourceCode" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>产能单元</label><div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>类型</label><div class="control is-select"><el-select v-model="queryParams.resourceType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_resource_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>产线</label><div class="control"><el-input v-model="queryParams.lineName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>车间</label><div class="control"><el-input v-model="queryParams.workshopName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="{ 'is-active': activeStatusTab === s.value }" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:resource:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:resource:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:resource:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:resource:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_resource_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="资源编码" prop="resourceCode" key="resourceCode" :width="colWidth('resourceCode', 130)" resizable v-if="columns.resourceCode.visible" /><el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 150)" resizable v-if="columns.resourceName.visible" /><el-table-column label="类型" prop="resourceType" key="resourceType" :width="colWidth('resourceType', 100)" resizable align="center" v-if="columns.resourceType.visible"><template #default="scope"><span v-if="scope.row.resourceType" class="badge blue"><span class="dot"></span>{{ dictLabel(mms_resource_type, scope.row.resourceType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="产线" prop="lineName" key="lineName" :width="colWidth('lineName', 100)" resizable v-if="columns.lineName.visible"><template #default="scope"><span>{{ scope.row.lineName || '—' }}</span></template></el-table-column><el-table-column label="车间" prop="workshopName" key="workshopName" :width="colWidth('workshopName', 100)" resizable v-if="columns.workshopName.visible"><template #default="scope"><span>{{ scope.row.workshopName || '—' }}</span></template></el-table-column><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="220" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:resource:edit']">修改</el-button><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:resource:remove']">删除</el-button></template></el-table-column></el-table></div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="资源编码" prop="resourceCode"><el-input v-model="form.resourceCode" placeholder="保存时自动生成" readonly /></el-form-item></el-col><el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="form.resourceName" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="类型" prop="resourceType"><el-select v-model="form.resourceType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_resource_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20"/><path d="M5 20V8l4 4 6-6 4 4v10"/></svg></span>生产归属</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产线" prop="lineName"><el-input v-model="form.lineName" readonly placeholder="请选择产线" style="width: 100%" @click="openLinePicker"><template #append><el-button icon="Search" @click="openLinePicker" /></template><template #suffix><el-icon v-if="form.lineName" class="clear-icon" @click.stop="clearLine"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="车间" prop="workshopName"><el-input v-model="form.workshopName" placeholder="选择产线后自动带出" readonly /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">产能资源详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.resourceCode">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编码：{{ viewData.resourceCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">资源编码</span><div class="rd-value">{{ viewData.resourceCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><dict-tag :options="mms_resource_type" :value="viewData.resourceType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_status" :value="viewData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20"/><path d="M5 20V8l4 4 6-6 4 4v10"/></svg></span>生产归属</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">所属产线</span><div class="rd-value">{{ viewData.lineName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属车间</span><div class="rd-value">{{ viewData.workshopName || '—' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <!-- 产线选择弹窗 -->
    <line-picker ref="linePickerRef" title="选择产线" @confirm="onLinePickerConfirm" />

    <el-dialog v-model="showStatusHelp" title="产能资源业务操作说明" width="820px" append-to-body><div class="status-help-content"><h4>一、产能资源释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是产能资源？</div><div class="highlight-card-body"><strong>产能资源（Resource）</strong>是生产管控中管理生产设备、产线、工作中心等资源的主数据。每个产能资源定义了所属车间和产线，是工单排产和产能规划的基础依据。<br/><br/>产能资源遵循 <strong>MES 资源管理规范</strong>，通过资源类型和所属车间/产线信息支持工单分配和产能规划。</div></div><h4>二、重点业务规则</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">核心规则</div><div class="highlight-card-body"><p>• <strong>资源类型：</strong>包括单设备、产线、设备组等多种类型</p><p>• <strong>所属车间：</strong>资源所属的生产车间</p><p>• <strong>所属产线：</strong>资源所属的生产线</p><p>• <strong>状态控制：</strong>已启用状态的资源方可被工单排产引用</p></div></div><h4>三、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>创建资源：</strong>点击「新增」创建产能资源，填写资源编码、名称和类型</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>配置信息：</strong>设置资源所属车间和产线</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>分配工单：</strong>在工单管理中将资源分配给工单进行生产</el-timeline-item></el-timeline></div><template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template></el-dialog>
  </div>
</template>

<script setup name="Resource">
import { listResource, getResource, addResource, updateResource, delResource } from "@/api/mms/resource";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import LinePicker from '@/components/LinePicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_resource_type, mms_status } = proxy.useDict("mms_resource_type", "mms_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_resource_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);
const linePickerRef = ref();

const statusTabList = computed(() => mms_status.value ? mms_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { resourceCode: { label: '资源编码', visible: true }, resourceName: { label: '产能单元', visible: true }, resourceType: { label: '类型', visible: true }, lineName: { label: '产线', visible: true }, workshopName: { label: '车间', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_resource_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.resourceCode) c++; if (queryParams.value.resourceName) c++; if (queryParams.value.resourceType) c++; if (queryParams.value.workshopName) c++; if (queryParams.value.status) c++; if (queryParams.value.lineName) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, resourceCode: undefined, resourceName: undefined, resourceType: undefined, workshopName: undefined, lineName: undefined, status: undefined, params: {} }, rules: { resourceName: [{ required: true, message: "请输入产能单元名称", trigger: "blur" }], resourceType: [{ required: true, message: "请选择类型", trigger: "change" }], status: [{ required: true, message: "请选择状态", trigger: "change" }], lineName: [{ required: true, message: "请选择产线", trigger: "change" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listResource(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.resourceCode) baseQuery.resourceCode = queryParams.value.resourceCode; if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName; if (queryParams.value.resourceType) baseQuery.resourceType = queryParams.value.resourceType; if (queryParams.value.workshopName) baseQuery.workshopName = queryParams.value.workshopName; if (queryParams.value.lineName) baseQuery.lineName = queryParams.value.lineName; listResource(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_status.value) { mms_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.resourceCode = undefined; queryParams.value.resourceName = undefined; queryParams.value.resourceType = undefined; queryParams.value.workshopName = undefined; queryParams.value.lineName = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.resourceId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { resourceCode: undefined, resourceName: undefined, resourceType: undefined, workshopName: undefined, lineName: undefined, status: '0', remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = "新增产能资源"; }
function handleUpdate(row) { reset(); const id = row.resourceId || ids.value[0]; getResource(id).then(response => { form.value = response.data; collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = "修改产能资源"; }); }
function handleView(row) { const id = row.resourceId || ids.value[0]; getResource(id).then(response => { viewData.value = response.data; collapsedCards.vc0 = false; collapsedCards.vc1 = false; collapsedCards.vc2 = false; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.resourceId != null) { updateResource(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addResource(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.resourceId || ids.value; proxy.$modal.confirm('是否确认删除选中的产能资源？').then(() => delResource(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/resource/export", { ...queryParams.value }, `resource_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_status, status); }
function badgeClass(status) { const map = { '0': 'green', '1': 'gray' }; return map[status] || 'gray'; }

// 产线选择弹窗
function openLinePicker() {
  linePickerRef.value?.open(form.value.lineName)
}
function onLinePickerConfirm(data) {
  form.value.lineName = data.lineName;
  form.value.workshopName = data.workshopName;
}
function clearLine() {
  form.value.lineName = undefined;
  form.value.workshopName = undefined;
}

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-resource-page .clear-icon{cursor:pointer;color:#c0c4cc;font-size:14px}
.mms-resource-page .clear-icon:hover{color:#909399}
.mms-resource-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
</style>