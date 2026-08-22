<template>
  <div class="app-container mms-dispatch-page">
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
          <label>派工单号</label>
          <div class="control">
            <el-input v-model="queryParams.dispatchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单编号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>工序名称</label>
          <div class="control">
            <el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>产能单元</label>
          <div class="control">
            <el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_dispatch_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>班组</label>
          <div class="control is-select">
            <el-select v-model="queryParams.teamId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="t in teamOptions" :key="t.teamId" :label="t.teamName" :value="t.teamId" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建日期</label>
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
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:dispatch:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_dispatch_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column label="派工单号" prop="dispatchNo" key="dispatchNo" :width="colWidth('dispatchNo', 150)" resizable v-if="columns.dispatchNo.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 120)" resizable show-overflow-tooltip v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 120)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
          <el-table-column label="工序序号" prop="opSeq" key="opSeq" :width="colWidth('opSeq', 90)" resizable align="center" v-if="columns.opSeq.visible" />
          <el-table-column label="工序名称" prop="processName" key="processName" :width="colWidth('processName', 140)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 140)" resizable show-overflow-tooltip v-if="columns.resourceName.visible" />
          <el-table-column label="班组" prop="teamName" key="teamName" :width="colWidth('teamName', 120)" resizable v-if="columns.teamName.visible" />
          <el-table-column label="派工人员" prop="userIds" key="userIds" :width="colWidth('userIds', 150)" resizable show-overflow-tooltip v-if="columns.userIds.visible" />
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="合格数量" prop="goodQty" key="goodQty" :width="colWidth('goodQty', 100)" resizable align="center" v-if="columns.goodQty.visible" />
          <el-table-column label="不良数量" prop="defectQty" key="defectQty" :width="colWidth('defectQty', 100)" resizable align="center" v-if="columns.defectQty.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="计划开始" prop="planStart" key="planStart" :width="colWidth('planStart', 160)" resizable align="center" v-if="columns.planStart.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planStart) }}</span></template>
          </el-table-column>
          <el-table-column label="计划结束" prop="planEnd" key="planEnd" :width="colWidth('planEnd', 160)" resizable align="center" v-if="columns.planEnd.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planEnd) }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:dispatch:query']">详情</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="VideoPlay" @click="handleStart(scope.row)" v-hasPermi="['mms:dispatch:start']">开工</el-button>
              <el-button v-if="scope.row.status === '1'" link type="primary" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['mms:dispatch:finish']">完工</el-button>
              <el-button v-if="scope.row.status === '0' || scope.row.status === '1'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:dispatch:cancel']">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/><path d="M9 3V1h4v2"/><path d="M9 3v6h4V3"/></svg>
          </div>
          <span class="rd-detail-header-title">派工单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.dispatchNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewData.dispatchNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">派工单号</span><div class="rd-value">{{ viewData.dispatchNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
            <div class="rd-item"><span class="rd-label">工序序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ viewData.processName || '—' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>派工信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">班组</span><div class="rd-value">{{ viewData.teamName || '—' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">派工人员</span><div class="rd-value">{{ viewData.userIds || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.goodQty != null ? viewData.goodQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">不良数量</span><div class="rd-value">{{ viewData.defectQty != null ? viewData.defectQty : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>时间信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">计划开始</span><div class="rd-value">{{ viewData.planStart ? parseTime(viewData.planStart) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划结束</span><div class="rd-value">{{ viewData.planEnd ? parseTime(viewData.planEnd) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">实际开始</span><div class="rd-value">{{ viewData.actualStart ? parseTime(viewData.actualStart) : '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">实际结束</span><div class="rd-value">{{ viewData.actualEnd ? parseTime(viewData.actualEnd) : '—' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <!-- ===== 开工 Dialog ===== -->
    <el-dialog v-model="startOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg></div>
          <span class="rd-detail-header-title">派工开工</span>
          <div class="rd-detail-header-sub" v-if="startForm.dispatchNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ startForm.dispatchNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="startFormRef" :model="startForm" :rules="startRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>派工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="派工单号"><el-input :model-value="startForm.dispatchNo" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号"><el-input :model-value="startForm.workOrderNo" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编码"><el-input :model-value="startForm.productCode" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称"><el-input :model-value="startForm.productName" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="规格型号"><el-input :model-value="startForm.specModel" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="单位"><el-input :model-value="unitLabel(startForm.unit)" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序序号"><el-input :model-value="startForm.opSeq" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工序名称"><el-input :model-value="startForm.processName" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产能单元"><el-input :model-value="startForm.resourceName" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="班组" prop="teamId" required><el-select v-model="startForm.teamId" placeholder="请选择班组" style="width: 100%" @change="onStartTeamChange"><el-option v-for="t in teamOptions" :key="t.teamId" :label="t.teamName" :value="t.teamId" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划数量"><el-input-number :model-value="startForm.planQty" :min="0" :precision="2" disabled style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="计划开始"><el-input :model-value="startForm.planStart ? parseTime(startForm.planStart) : '—'" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划结束"><el-input :model-value="startForm.planEnd ? parseTime(startForm.planEnd) : '—'" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>开工确认</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="操作人员" prop="operatorName"><el-input v-model="startForm.operatorName" readonly placeholder="当前操作人" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
        </div>
      </el-form>
      <div style="margin: 8px 0 0; padding: 10px 16px; background: #f0f9ff; border: 1px solid #d0e8ff; border-radius: 6px; font-size: 13px; color: #1d4ed8; display: flex; align-items: center; gap: 6px;">
        <el-icon><InfoFilled /></el-icon>
        <span>开工后派工单状态变为进行中，同时联动将关联工单状态从已下达变为执行中</span>
      </div>
      <template #footer>
        <el-button @click="startOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitStart">确 认 开 工</el-button>
      </template>
    </el-dialog>

    <!-- ===== 完工 Dialog ===== -->
    <el-dialog v-model="finishOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <span class="rd-detail-header-title">派工完工</span>
          <div class="rd-detail-header-sub" v-if="finishForm.dispatchNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ finishForm.dispatchNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="finishFormRef" :model="finishForm" :rules="finishRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>派工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="派工单号"><el-input :model-value="finishForm.dispatchNo" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号"><el-input :model-value="finishForm.workOrderNo" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编码"><el-input :model-value="finishForm.productCode" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称"><el-input :model-value="finishForm.productName" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="规格型号"><el-input :model-value="finishForm.specModel" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="单位"><el-input :model-value="unitLabel(finishForm.unit)" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序序号"><el-input :model-value="finishForm.opSeq" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工序名称"><el-input :model-value="finishForm.processName" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产能单元"><el-input :model-value="finishForm.resourceName" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="班组"><el-input :model-value="finishForm.teamName" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>完工数据填报</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划数量"><el-input-number :model-value="finishForm.planQty" :min="0" :precision="2" disabled style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="开工时间"><el-input :model-value="finishForm.actualStart ? parseTime(finishForm.actualStart) : '—'" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="实际开始" prop="actualStartInput"><el-date-picker v-model="finishForm.actualStartInput" type="datetime" placeholder="选择实际开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="实际结束" prop="actualEndInput"><el-date-picker v-model="finishForm.actualEndInput" type="datetime" placeholder="选择实际结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="合格数量" prop="goodQty"><el-input-number v-model="finishForm.goodQty" :min="0" :precision="2" :max="finishForm.planQty" placeholder="请输入" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="不良数量" prop="defectQty"><el-input-number v-model="finishForm.defectQty" :min="0" :precision="2" :max="finishForm.planQty" placeholder="请输入" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-form-item label="完工备注"><el-input v-model="finishForm.remark" type="textarea" :rows="2" placeholder="不良原因、其他说明" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <div style="margin: 8px 0 0; padding: 10px 16px; background: #f0f9ff; border: 1px solid #d0e8ff; border-radius: 6px; font-size: 13px; color: #1d4ed8; display: flex; align-items: center; gap: 6px;">
        <el-icon><InfoFilled /></el-icon>
        <span>完工后系统将自动生成已审核的报工记录并联动更新工单进度。若存在后续工序，且当前工序所在并行组已全部完工（或取消），系统将自动创建下一组工序的派工单（计划数量取并行组最小合格数）。如需多次中间报工、首件确认或记录工时/班次，请前往报工管理手动新增</span>
      </div>
      <template #footer>
        <el-button @click="finishOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitFinish">确 认 完 工</el-button>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="派工管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <!-- 一、派工单释义 -->
        <h4>一、派工单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是派工单？</div>
          <div class="highlight-card-body">
            <strong>派工单（Dispatch Order）</strong>是生产管控中将工单工序任务分配到具体产能单元和人员的执行单据。派工单关联工单编号、工序序号、产能单元、班组和派工人员，记录计划数量与实际完成数量，通过开工→完工→取消的状态流转，实现工序级任务的精细化管控和过程可追溯。<br/><br/>
            派工单是<strong>MES（制造执行系统）</strong>中工序级任务管理的核心载体，向上对接工单的工序拆分与排产计划，向下驱动产能单元的任务执行、人员调配和进度回报，满足精益生产对工序级任务可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、派工单状态流转图 -->
        <h4>二、派工单状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待开工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「开工」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「完工」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">待开工</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="primary">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「取消」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已取消</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待开工">派工单由工单下达自动生成后的初始状态。可开工或取消。点击「开工」将弹出开工窗口，需填写操作人员姓名，确认后派工单下发至产能单元，状态变为进行中，同时联动将关联工单状态从已下达变为执行中</el-descriptions-item>
          <el-descriptions-item label="进行中">派工单已开工，正在执行生产任务。可完工或取消。点击「完工」将弹出完工窗口，需填写合格数量、不良数量和完工备注，确认后系统自动回写派工单数量、生成报工记录（已审核状态）、联动更新工单完工进度和状态</el-descriptions-item>
          <el-descriptions-item label="已完成">派工单已完工，实际生产数量已确认回写，报工记录已自动生成。不可取消，数据归档</el-descriptions-item>
          <el-descriptions-item label="已取消">派工单因异常情况被取消作废，不再执行。除已完成和已取消外的派工单均可取消</el-descriptions-item>
        </el-descriptions>

        <!-- 四、派工单字段说明 -->
        <h4>四、派工单字段说明</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息</div>
          <div class="highlight-card-body">
            <p>• <strong>派工单号：</strong>派工单的唯一标识编号，由系统自动生成</p>
            <p>• <strong>工单编号：</strong>关联的生产工单编号，工单下达时自动带出</p>
            <p>• <strong>产品编码：</strong>生产的产品编码，工单下达时自动带出</p>
            <p>• <strong>产品名称：</strong>生产的产品名称，工单下达时自动带出</p>
            <p>• <strong>规格型号：</strong>产品的规格型号，工单下达时自动带出</p>
            <p>• <strong>单位：</strong>产品的计量单位（字典 wms_unit），工单下达时自动带出</p>
            <p>• <strong>工序序号：</strong>对应工单工艺路线中的工序顺序号，工单下达时自动带出</p>
            <p>• <strong>工序名称：</strong>对应工单工艺路线中的工序名称，工单下达时自动带出</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">派工信息</div>
          <div class="highlight-card-body">
            <p>• <strong>产能单元：</strong>执行该工序任务的产能单元（设备/产线），工单下达时自动带出</p>
            <p>• <strong>班组：</strong>指定执行任务的班组</p>
            <p>• <strong>派工人员：</strong>执行任务的操作人员，开工时填写</p>
            <p>• <strong>计划数量：</strong>该工序的计划生产数量，工单下达时自动带出</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">时间信息</div>
          <div class="highlight-card-body">
            <p>• <strong>计划开始：</strong>计划开始执行任务的时间</p>
            <p>• <strong>计划结束：</strong>计划完成任务的时间</p>
            <p>• <strong>实际开始：</strong>完工时填写，记录实际开工时间，默认带出开工时系统记录的时间</p>
            <p>• <strong>实际结束：</strong>完工时填写，记录实际完工时间，默认为当前系统时间</p>
          </div>
        </div>

        <!-- 五、派工单生命周期管控 -->
        <h4>五、派工单生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是派工单生命周期管控？</div>
          <div class="highlight-card-body">
            <strong>派工单生命周期管控</strong>是生产管控的核心机制，通过状态流转实现派工单从创建到完工的全过程管理。每个状态对应特定的可执行操作，确保工序任务有序可控、可追溯。派工单的开工/完工机制确保生产进度数据实时回写，取消机制支持应对订单变更、设备故障等异常情况。<br/><br/>
            完工操作是核心联动节点：<strong>完工确认 → 回写派工单数量 → 自动生成报工记录 → 联动更新工单完工进度和状态</strong>，实现工序级生产数据的闭环管理。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">异常处理规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>已开工无法取消编辑：</strong>派工单开工后进入「进行中」状态，仅可完工或取消，确保执行数据的一致性</p>
            <p>2. <strong>已完工无法取消：</strong>完工状态为终态，实际数量已回写归档，报工记录已生成，不可再取消</p>
            <p>3. <strong>取消后不可恢复：</strong>派工单取消后作废终止，如需重新执行需新建派工单</p>
            <p>4. <strong>工单暂停/作废联动：</strong>若关联工单被暂停或作废，派工单将同步受到影响，暂停状态下不可开工</p>
            <p>5. <strong>完工数量校验：</strong>合格数量+不良数量不能超过计划数量，各数量不能为负数</p>
            <p>6. <strong>时间校验：</strong>实际结束时间不能早于实际开始时间</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>取消操作需确认，系统自动记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <!-- 六、开工与完工操作详解 -->
        <h4>六、开工与完工操作详解</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">开工操作</div>
          <div class="highlight-card-body">
            <p>在待开工状态下点击「开工」按钮，系统弹出开工确认窗口，需填写以下内容：</p>
            <p>• <strong>操作人员</strong><span style="color: #f56c6c;">*必填</span>：从人员选择弹窗中选择实际执行该工序任务的操作人员，将记录到派工单的派工人员字段</p>
            <p style="color: #409eff;"><strong>联动逻辑：</strong>开工后派工单状态变为进行中，记录实际开始时间；同时联动将关联工单状态从已下达(1)变为执行中(2)</p>
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">完工操作</div>
          <div class="highlight-card-body">
            <p>在进行中状态下点击「完工」按钮，系统弹出完工确认窗口，需填写以下内容：</p>
            <p>• <strong>合格数量</strong><span style="color: #f56c6c;">*必填</span>：本次完工的合格品数量，不能超过计划数量</p>
            <p>• <strong>不良数量</strong>：本次完工的不良品数量，合格+不良不能超过计划数量</p>
            <p>• <strong>实际开始时间</strong><span style="color: #f56c6c;">*必填</span>：实际开工的日期时间，默认带出开工时系统记录的时间，可修改</p>
            <p>• <strong>实际结束时间</strong><span style="color: #f56c6c;">*必填</span>：实际完工的日期时间，默认为当前系统时间，可修改</p>
            <p>• <strong>完工备注</strong>：不良原因、其他说明等信息（选填）</p>
            <p style="color: #67c23a;"><strong>联动逻辑（三步联动）：</strong></p>
            <p style="padding-left: 16px;">① <strong>回写派工单</strong>：更新合格数量、不良数量、实际开始时间、实际结束时间，状态变为已完成</p>
            <p style="padding-left: 16px;">② <strong>自动生成报工记录</strong>：系统自动创建一条已审核状态的报工记录，包含合格数、不良数、工序、产能单元等信息</p>
            <p style="padding-left: 16px;">③ <strong>联动更新工单进度</strong>：工单完工数量、合格数量、不良数量自动累计；工单状态从已下达(1)→执行中(2)→报工中(3)</p>
          </div>
        </div>

        <!-- 七、业务操作流程 -->
        <h4>七、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>自动生成派工单：</strong>生产工单下达时，系统自动生成首工序（step_seq最小的一组）派工单。若首工序含多道并行工序，则同时生成多条派工单，带出工单编号、工序信息、产能单元、计划数量等，无需手动创建
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>开工：</strong>待开工状态下点击「开工」弹出开工窗口，填写操作人员姓名后确认，派工单状态变为进行中，同时联动将关联工单状态从已下达变为执行中
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>完工确认：</strong>生产完成后点击「完工」弹出完工窗口，填写合格数量、不良数量、实际开始时间、实际结束时间和完工备注后确认，系统自动回写派工单数量、生成报工记录、联动更新工单完工进度和状态，派工单状态变为已完成。若存在后续工序，且当前工序所在并行组已全部完工（或取消），系统将自动创建下一组工序的派工单（计划数量取并行组最小合格数），实现工序级流转
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>点击「详情」可查看派工单完整信息，包括基本信息、派工信息、实际执行数据、时间信息等
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>取消：</strong>异常派工单可在待开工或进行中状态点击「取消」终止，取消后不可恢复
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Dispatch">
import { listDispatch, getDispatch,
         startDispatch, finishDispatch, cancelDispatch } from "@/api/mms/dispatch";
import { listTeam } from "@/api/mms/team";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled, InfoFilled, CircleClose } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance();
const { mms_dispatch_status, wms_unit } = proxy.useDict("mms_dispatch_status", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_dispatch_index')
const { collapsedCards, toggleCard } = useDetailCard(["vc0","vc1","vc2","vc3","sc0","sc1","sc2","fc0","fc1"])

const dataList = ref([]);
const viewOpen = ref(false);
const viewData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const showStatusHelp = ref(false);
const total = ref(0);
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});
const teamOptions = ref([]);

// 加载班组下拉
function loadTeamOptions() {
  listTeam({ pageNum: 1, pageSize: 999, status: '0' }).then(response => {
    teamOptions.value = response.rows || [];
  });
}

// 状态标签列表
const statusTabList = computed(() => {
  if (!mms_dispatch_status.value) return [];
  return mms_dispatch_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  dispatchNo: { label: '派工单号', visible: true },
  workOrderNo: { label: '工单编号', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  opSeq: { label: '工序序号', visible: true },
  processName: { label: '工序名称', visible: true },
  resourceName: { label: '产能单元', visible: true },
  teamName: { label: '班组', visible: true },
  userIds: { label: '派工人员', visible: true },
  planQty: { label: '计划数量', visible: true },
  goodQty: { label: '合格数量', visible: true },
  defectQty: { label: '不良数量', visible: true },
  status: { label: '状态', visible: true },
  planStart: { label: '计划开始', visible: true },
  planEnd: { label: '计划结束', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_dispatch_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}

const columns = ref(loadColumnVisibility())

// 当前已选筛选条件数
const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.dispatchNo) count++;
  if (queryParams.value.workOrderNo) count++;
  if (queryParams.value.processName) count++;
  if (queryParams.value.resourceName) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.teamId) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

// 开工弹窗
const startOpen = ref(false);
const startForm = reactive({
  dispatchId: undefined,
  dispatchNo: '',
  workOrderNo: '',
  productCode: '',
  productName: '',
  specModel: '',
  unit: '',
  opSeq: undefined,
  processName: '',
  resourceName: '',
  teamId: undefined,
  teamName: '',
  planQty: 0,
  planStart: undefined,
  planEnd: undefined,
  operatorName: ''
});
const startRules = {
  teamId: [{ required: true, message: "请选择班组", trigger: "change" }],
  operatorName: [{ required: true, message: "操作人员不能为空", trigger: "blur" }]
};

// 完工弹窗
const finishOpen = ref(false);
const finishForm = reactive({
  dispatchId: undefined,
  dispatchNo: '',
  workOrderNo: '',
  productCode: '',
  productName: '',
  specModel: '',
  unit: '',
  opSeq: undefined,
  processName: '',
  resourceName: '',
  teamName: '',
  planQty: 0,
  actualStart: undefined,       // 开工时记录的时间（只读展示）
  actualStartInput: undefined,   // 完工时可编辑的实际开始时间
  actualEndInput: undefined,      // 完工时可编辑的实际结束时间
  goodQty: 0,
  defectQty: 0,
  remark: ''
});
const finishRules = {
  goodQty: [{ required: true, message: "请输入合格数量", trigger: "blur" }],
  actualStartInput: [{ required: true, message: "请选择实际开始时间", trigger: "change" }],
  actualEndInput: [{ required: true, message: "请选择实际结束时间", trigger: "change" }]
};

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dispatchNo: undefined,
    workOrderNo: undefined,
    processName: undefined,
    resourceName: undefined,
    teamId: undefined,
    status: undefined,
    params: {}
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listDispatch(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    loadStatusCounts();
  });
}

function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.dispatchNo) baseQuery.dispatchNo = queryParams.value.dispatchNo;
  if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo;
  if (queryParams.value.processName) baseQuery.processName = queryParams.value.processName;
  if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName;
  if (queryParams.value.teamId) baseQuery.teamId = queryParams.value.teamId;
  listDispatch(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_dispatch_status.value) {
      mms_dispatch_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    statusCounts.value = counts;
  }).catch(() => {});
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.dispatchNo = undefined;
  queryParams.value.workOrderNo = undefined;
  queryParams.value.processName = undefined;
  queryParams.value.resourceName = undefined;
  queryParams.value.teamId = undefined;
  queryParams.value.status = undefined;
  dateRange.value = [];
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  handleQuery();
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.value.status = status === "all" ? undefined : status;
  handleQuery();
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop;
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.value.params.orderByColumn = undefined;
    queryParams.value.params.isAsc = undefined;
  }
  getList();
}

function handleView(row) {
  getDispatch(row.dispatchId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function handleExport() {
  proxy.download("mms/dispatch/export", { ...queryParams.value }, `dispatch_${new Date().getTime()}.xlsx`);
}

// ===== 业务操作 =====

function handleStart(row) {
  startForm.dispatchId = row.dispatchId;
  startForm.dispatchNo = row.dispatchNo;
  startForm.workOrderNo = row.workOrderNo;
  startForm.productCode = row.productCode;
  startForm.productName = row.productName;
  startForm.specModel = row.specModel;
  startForm.unit = row.unit;
  startForm.opSeq = row.opSeq;
  startForm.processName = row.processName;
  startForm.resourceName = row.resourceName;
  startForm.teamId = row.teamId;
  startForm.teamName = row.teamName;
  startForm.planQty = row.planQty;
  startForm.planStart = row.planStart;
  startForm.planEnd = row.planEnd;
  // 默认取当前登录用户昵称
  startForm.operatorName = useUserStore().nickName;
  startOpen.value = true;
}

// ===== 开工-班组选择 =====
function onStartTeamChange(val) {
  const team = teamOptions.value.find(t => t.teamId === val);
  startForm.teamName = team ? team.teamName : '';
}

function submitStart() {
  proxy.$refs["startFormRef"].validate(valid => {
    if (valid) {
      startDispatch(startForm.dispatchId, {
        operatorName: startForm.operatorName,
        teamId: startForm.teamId,
        teamName: startForm.teamName
      }).then(() => {
        startOpen.value = false;
        getList();
        loadStatusCounts();
        proxy.$modal.msgSuccess("开工成功");
      });
    }
  });
}

function handleFinish(row) {
  finishForm.dispatchId = row.dispatchId;
  finishForm.dispatchNo = row.dispatchNo;
  finishForm.workOrderNo = row.workOrderNo;
  finishForm.productCode = row.productCode;
  finishForm.productName = row.productName;
  finishForm.specModel = row.specModel;
  finishForm.unit = row.unit;
  finishForm.opSeq = row.opSeq;
  finishForm.processName = row.processName;
  finishForm.resourceName = row.resourceName;
  finishForm.teamName = row.teamName;
  finishForm.planQty = row.planQty;
  finishForm.actualStart = row.actualStart;
  // 默认实际开始时间 = 开工时记录时间
  finishForm.actualStartInput = row.actualStart ? parseTime(row.actualStart) : parseTime(new Date());
  // 默认实际结束时间 = 当前时间
  finishForm.actualEndInput = parseTime(new Date());
  finishForm.goodQty = row.planQty;
  finishForm.defectQty = 0;
  finishForm.remark = '';
  finishOpen.value = true;
}

function submitFinish() {
  proxy.$refs["finishFormRef"].validate(valid => {
    if (valid) {
      // 前端校验实际结束不早于实际开始
      if (finishForm.actualStartInput && finishForm.actualEndInput) {
        const start = new Date(finishForm.actualStartInput);
        const end = new Date(finishForm.actualEndInput);
        if (end < start) {
          proxy.$modal.msgError("实际结束时间不能早于实际开始时间");
          return;
        }
      }
      finishDispatch(finishForm.dispatchId, {
        goodQty: finishForm.goodQty,
        defectQty: finishForm.defectQty,
        remark: finishForm.remark,
        actualStart: finishForm.actualStartInput,
        actualEnd: finishForm.actualEndInput
      }).then(() => {
        finishOpen.value = false;
        getList();
        loadStatusCounts();
        proxy.$modal.msgSuccess("完工成功，已自动生成报工记录");
      });
    }
  });
}

function handleCancel(row) {
  proxy.$modal.confirm('是否确认取消派工单"' + row.dispatchNo + '"？').then(function() {
    return cancelDispatch(row.dispatchId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("取消成功");
  }).catch(() => {});
}

// ===== 字典辅助函数 =====
function statusLabel(status) {
  const item = mms_dispatch_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function unitLabel(val) {
  if (!val && val !== 0) return '—';
  const item = wms_unit.value ? wms_unit.value.find(d => d.value == val) : null;
  return item ? item.label : val;
}

function badgeClass(status) {
  const map = {
    '0': 'amber',    // 待开工
    '1': 'blue',     // 进行中
    '2': 'green',    // 已完成
    '3': 'gray'      // 已取消
  };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = {
    '0': 'tab-draft',
    '1': 'tab-audit',
    '2': 'tab-done',
    '3': 'tab-void'
  };
  return map[value] || '';
}

loadTeamOptions();
getList();
</script>

<style scoped>
.mms-dispatch-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-dispatch-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-dispatch-page .filter-card { padding:14px 20px 16px; }
.mms-dispatch-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-dispatch-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-dispatch-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-dispatch-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-dispatch-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-dispatch-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-dispatch-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-dispatch-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-dispatch-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-dispatch-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-dispatch-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-dispatch-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-dispatch-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-dispatch-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-dispatch-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-dispatch-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-dispatch-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-dispatch-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-dispatch-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-dispatch-page .field .control :deep(.el-select) { width:100%; }
.mms-dispatch-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-dispatch-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-dispatch-page .toolbar .left, .mms-dispatch-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-dispatch-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-dispatch-page .table-wrap { overflow-x:auto; }
.mms-dispatch-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-dispatch-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-dispatch-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-dispatch-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-dispatch-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-dispatch-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-dispatch-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-dispatch-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-dispatch-page .badge.amber .dot { background:var(--amber-500); }
.mms-dispatch-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-dispatch-page .badge.blue .dot { background:var(--blue-500); }
.mms-dispatch-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-dispatch-page .badge.green .dot { background:var(--green-500); }
.mms-dispatch-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-dispatch-page .badge.red .dot { background:var(--red-500); }
.mms-dispatch-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-dispatch-page .badge.gray .dot { background:var(--ink-400); }
.mms-dispatch-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-dispatch-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-dispatch-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-dispatch-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-dispatch-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-dispatch-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-dispatch-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-dispatch-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-dispatch-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-dispatch-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-dispatch-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-dispatch-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-dispatch-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-dispatch-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-dispatch-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-dispatch-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-dispatch-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-dispatch-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-dispatch-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-dispatch-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-dispatch-page .status-tab.tab-void .dot { background:var(--ink-400); } .mms-dispatch-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .mms-dispatch-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.mms-dispatch-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-dispatch-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-dispatch-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }
.rd-page { max-width: 760px; margin: 0 auto; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.mms-dispatch-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-dispatch-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-dispatch-page .rd-card-header:hover { background: #f1f5f9; }
.mms-dispatch-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-dispatch-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-dispatch-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-dispatch-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-dispatch-page .rd-card-body { padding: 16px; }
.mms-dispatch-page .text-muted { color: #94a3b8; }
@media (max-width:1100px) { .mms-dispatch-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-dispatch-page .filter-card .filter-bar { grid-template-columns:1fr; } }
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
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
</style>
