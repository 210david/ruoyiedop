<template>
  <div class="app-container mms-dispatch-page">
    <!-- ===== Top-Level Tab Switch: 派工管理 / 报工记录 ===== -->
    <div class="page-tabs-bar">
      <button class="page-tab" :class="{ 'is-active': pageTab === 'dispatch' }" @click="switchPageTab('dispatch')">
        <el-icon><Grid /></el-icon><span>派工管理</span>
      </button>
      <button class="page-tab" :class="{ 'is-active': pageTab === 'report' }" @click="switchPageTab('report')">
        <el-icon><Tickets /></el-icon><span>报工记录</span>
      </button>
    </div>

    <!-- ===== 派工管理 Tab ===== -->
    <template v-if="pageTab === 'dispatch'">
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

      <!-- View Mode Switch -->
      <div class="view-mode-bar">
        <div class="view-mode-tabs">
          <button class="view-mode-tab" :class="{ 'is-active': viewMode === 'grouped' }" @click="switchViewMode('grouped')">
            <el-icon><Grid /></el-icon><span>工单分组视图</span>
          </button>
          <button class="view-mode-tab" :class="{ 'is-active': viewMode === 'flat' }" @click="switchViewMode('flat')">
            <el-icon><List /></el-icon><span>平铺列表视图</span>
          </button>
        </div>
      </div>

      <!-- ===== Grouped View (工单分组折叠列表) ===== -->
      <div v-if="viewMode === 'grouped'" class="grouped-view" v-loading="loading">
        <div v-if="groupedList.length === 0 && !loading" class="empty-state">
          <el-icon class="empty-icon"><Box /></el-icon>
          <p>暂无派工数据</p>
        </div>
        <div v-for="group in groupedList" :key="group.workOrderNo" class="wo-group">
          <!-- 工单分组头 -->
          <div class="wo-group-header" @click="toggleGroup(group.workOrderNo)">
            <div class="wo-group-toggle">
              <el-icon class="toggle-icon" :class="{ 'is-expanded': expandedGroups.has(group.workOrderNo) }"><ArrowRight /></el-icon>
            </div>
            <div class="wo-group-main">
              <div class="wo-group-line1">
                <span class="wo-no">{{ group.workOrderNo }}</span>
                <span class="wo-product">{{ group.productName }}</span>
                <span class="wo-code text-muted">{{ group.productCode }}</span>
                <span class="wo-spec text-muted" v-if="group.specModel">{{ group.specModel }}</span>
              </div>
              <div class="wo-group-line2">
                <!-- 进度 -->
                <div class="wo-progress">
                  <span class="wo-progress-label">进度</span>
                  <span class="wo-progress-value">{{ group.doneCount }}/{{ group.totalCount }}</span>
                  <div class="wo-progress-bar">
                    <div class="wo-progress-fill" :style="{ width: group.progressPercent + '%' }"></div>
                  </div>
                </div>
                <!-- 状态统计 -->
                <div class="wo-status-stats">
                  <span class="stat-chip stat-pending" v-if="group.statusCounts['0']">待开工 {{ group.statusCounts['0'] }}</span>
                  <span class="stat-chip stat-running" v-if="group.statusCounts['1']">进行中 {{ group.statusCounts['1'] }}</span>
                  <span class="stat-chip stat-done" v-if="group.statusCounts['2']">已完成 {{ group.statusCounts['2'] }}</span>
                  <span class="stat-chip stat-cancel" v-if="group.statusCounts['3']">已取消 {{ group.statusCounts['3'] }}</span>
                </div>
                <!-- 数量汇总 -->
                <div class="wo-qty-stats">
                  <span class="qty-item"><span class="qty-label">计划</span><span class="qty-val">{{ group.summaryPlanQty }}</span></span>
                  <span class="qty-item"><span class="qty-label">{{ isGroupFinished(group) ? '合格' : '当前合格' }}</span><span class="qty-val" :class="isGroupFinished(group) ? 'text-success' : 'text-in-progress'">{{ group.summaryGoodQty }}</span></span>
                  <span class="qty-item"><span class="qty-label">不良</span><span class="qty-val text-danger" v-if="group.totalDefectQty">{{ group.totalDefectQty }}</span></span>
                </div>
                <!-- 时间范围 -->
                <div class="wo-time-range" v-if="group.minPlanStart">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ group.minPlanStart }} ~ {{ group.maxPlanEnd }}</span>
                </div>
              </div>
            </div>
            <div class="wo-group-actions">
              <el-button link type="primary" size="small" @click.stop="toggleGroup(group.workOrderNo)">
                {{ expandedGroups.has(group.workOrderNo) ? '收起工序' : '展开工序' }}
              </el-button>
            </div>
          </div>
          <!-- 工序列表（展开时显示） -->
          <div class="wo-group-body" v-show="expandedGroups.has(group.workOrderNo)">
            <el-table :data="group.items" border class="app-table inner-table" size="small" :ref="el => setInnerTableRef(el, group.workOrderNo)" @header-dragend="(n,o,w) => onInnerHeaderDragEnd(group.workOrderNo, n, o, w)">
              <el-table-column label="序号" prop="opSeq" :width="innerColWidth('opSeq', 70)" resizable align="center" />
              <el-table-column label="工序名称" prop="processName" :width="innerColWidth('processName', 150)" resizable show-overflow-tooltip />
              <el-table-column label="派工单号" prop="dispatchNo" :width="innerColWidth('dispatchNo', 160)" resizable show-overflow-tooltip />
              <el-table-column label="产能单元" prop="resourceName" :width="innerColWidth('resourceName', 150)" resizable show-overflow-tooltip />
              <el-table-column label="班组" prop="teamName" :width="innerColWidth('teamName', 120)" resizable show-overflow-tooltip />
              <el-table-column label="派工人员" prop="userIds" :width="innerColWidth('userIds', 130)" resizable show-overflow-tooltip />
              <el-table-column label="计划数量" prop="planQty" :width="innerColWidth('planQty', 100)" resizable align="center" />
              <el-table-column label="合格" prop="goodQty" :width="innerColWidth('goodQty', 90)" resizable align="center" />
              <el-table-column label="不良" prop="defectQty" :width="innerColWidth('defectQty', 90)" resizable align="center" />
              <el-table-column label="状态" prop="status" :width="innerColWidth('status', 100)" resizable align="center">
                <template #default="scope">
                  <span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)">
                    <span class="dot"></span>{{ statusLabel(scope.row.status) }}
                  </span>
                  <span v-else class="text-muted">—</span>
                </template>
              </el-table-column>
              <el-table-column label="计划开始" prop="planStart" :width="innerColWidth('planStart', 160)" resizable align="center">
                <template #default="scope"><span>{{ parseTime(scope.row.planStart) }}</span></template>
              </el-table-column>
              <el-table-column label="计划结束" prop="planEnd" :width="innerColWidth('planEnd', 160)" resizable align="center">
                <template #default="scope"><span>{{ parseTime(scope.row.planEnd) }}</span></template>
              </el-table-column>
              <el-table-column label="操作" width="190" align="center" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:dispatch:query']">详情</el-button>
                  <el-button v-if="scope.row.status === '0'" link type="success" icon="VideoPlay" @click="handleStart(scope.row)" v-hasPermi="['mms:dispatch:start']">开工</el-button>
                  <el-button v-if="scope.row.status === '1'" link type="primary" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['mms:dispatch:finish']">完工</el-button>
                  <el-button v-if="scope.row.status === '0' || scope.row.status === '1'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:dispatch:cancel']">取消</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>

      <!-- ===== Flat View (原始平铺列表) ===== -->
      <div v-show="viewMode === 'flat'" class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column label="派工单号" prop="dispatchNo" key="dispatchNo" :width="colWidth('dispatchNo', 150)" resizable v-if="columns.dispatchNo.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 120)" resizable show-overflow-tooltip v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 120)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
          <el-table-column label="工序顺序号" prop="opSeq" key="opSeq" :width="colWidth('opSeq', 90)" resizable align="center" v-if="columns.opSeq.visible" />
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
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
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
            <div class="rd-item"><span class="rd-label">工序顺序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '—' }}</div></div>
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
    <el-dialog v-model="startOpen" width="1037px" append-to-body draggable class="rd-dialog">
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
          <!-- 派工单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>派工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">派工单号</span><div class="rd-value">{{ startForm.dispatchNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ startForm.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工序顺序号</span><div class="rd-value">{{ startForm.opSeq != null ? startForm.opSeq : '-' }}</div></div><div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ startForm.processName || '-' }}</div></div></div></div>
          </section>
          <!-- 产品信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc0a')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc0a }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc0a" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ startForm.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ startForm.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ startForm.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="startForm.unit" /></div></div></div></div>
          </section>
          <!-- 计划信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc1" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">计划数量</span><div class="rd-value">{{ startForm.planQty != null ? startForm.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">计划开始</span><div class="rd-value">{{ startForm.planStart ? parseTime(startForm.planStart) : '-' }}</div></div><div class="rd-item"><span class="rd-label">计划结束</span><div class="rd-value">{{ startForm.planEnd ? parseTime(startForm.planEnd) : '-' }}</div></div></div></div>
          </section>
          <!-- 执行资源 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc0c')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>执行资源</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc0c }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc0c">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产能单元" prop="resourceName" required><el-input v-model="startForm.resourceName" readonly placeholder="请选择产能单元" style="width: 100%" @click="openStartResourcePicker"><template #append><el-button icon="Search" @click="openStartResourcePicker" /></template><template #suffix><el-icon v-if="startForm.resourceName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearStartResource"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="班组" prop="teamName" required><el-input v-model="startForm.teamName" readonly placeholder="请选择班组" style="width: 100%" @click="openStartTeamPicker"><template #append><el-button icon="Search" @click="openStartTeamPicker" /></template><template #suffix><el-icon v-if="startForm.teamName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearStartTeam"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
            </div>
          </section>
          <!-- 开工确认 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg></span>开工确认</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.sc2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="操作人员" prop="operatorName"><el-input v-model="startForm.operatorName" readonly placeholder="请选择操作人员" style="width: 100%" @click="openOperatorPicker"><template #append><el-button icon="Search" @click="openOperatorPicker" /></template><template #suffix><el-icon v-if="startForm.operatorName" class="rd-form-tip" style="cursor:pointer" @click.stop="startForm.operatorName = ''"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="操作时间" prop="operateTime"><el-date-picker v-model="startForm.operateTime" type="datetime" placeholder="请选择操作时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col></el-row>
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

    <!-- ===== 开工-产能单元选择弹窗 ===== -->
    <el-dialog v-model="startResourcePickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">选择产能单元</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="startResourcePickerQuery.resourceName" placeholder="产能单元名称" clearable size="small" style="width:200px" @keyup.enter="handleStartResourcePickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="startResourcePickerQuery.lineName" placeholder="产线" clearable size="small" style="width:160px" @keyup.enter="handleStartResourcePickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleStartResourcePickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetStartResourcePickerQuery">重置</el-button>
      </div>
      <el-table v-loading="startResourcePickerLoading" :data="startResourcePickerList" highlight-current-row @row-click="onStartResourceRowClick" @row-dblclick="onStartResourceRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="startResourcePickerSelectedId" :value="row.resourceId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="资源编码" prop="resourceCode" width="130" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="resourceName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产线" prop="lineName" width="100" show-overflow-tooltip />
        <el-table-column label="车间" prop="workshopName" width="100" show-overflow-tooltip />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="startResourcePickerQuery.pageNum" v-model:page-size="startResourcePickerQuery.pageSize" :total="startResourcePickerTotal" layout="total, prev, pager, next" small @current-change="getStartResourcePickerList" />
      </div>
      <div v-if="startResourcePickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出产能单元</div>
      <template #footer><el-button @click="startResourcePickerOpen = false">取 消</el-button><el-button type="primary" @click="confirmStartResourcePicker" :disabled="!startResourcePickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 开工-班组选择弹窗 ===== -->
    <el-dialog v-model="startTeamPickerOpen" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">选择班组</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="startTeamPickerQuery.teamName" placeholder="班组名称" clearable size="small" style="width:200px" @keyup.enter="handleStartTeamPickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain icon="Search" size="small" @click="handleStartTeamPickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetStartTeamPickerQuery">重置</el-button>
      </div>
      <el-table v-loading="startTeamPickerLoading" :data="startTeamPickerList" highlight-current-row @row-click="onStartTeamRowClick" @row-dblclick="onStartTeamRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="startTeamPickerSelectedId" :value="row.teamId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="班组编号" prop="teamCode" width="130" show-overflow-tooltip />
        <el-table-column label="班组名称" prop="teamName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="resourceName" width="150" show-overflow-tooltip />
        <el-table-column label="班组长" prop="leader" width="100" show-overflow-tooltip />
        <el-table-column label="班组人数" prop="memberCount" width="90" align="center" />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="startTeamPickerQuery.pageNum" v-model:page-size="startTeamPickerQuery.pageSize" :total="startTeamPickerTotal" layout="total, prev, pager, next" small @current-change="getStartTeamPickerList" />
      </div>
      <div v-if="startTeamPickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出班组</div>
      <template #footer><el-button @click="startTeamPickerOpen = false">取 消</el-button><el-button type="primary" @click="confirmStartTeamPicker" :disabled="!startTeamPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 开工-操作人员选择弹窗 ===== -->
    <user-picker ref="operatorPickerRef" title="选择操作人员" @confirm="onOperatorPickerConfirm" />

    <!-- ===== 完工 Dialog ===== -->
    <el-dialog v-model="finishOpen" width="864px" append-to-body draggable class="rd-dialog">
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
          <!-- 派工单信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>派工单信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">派工单号</span><div class="rd-value">{{ finishForm.dispatchNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ finishForm.workOrderNo || '-' }}</div></div></div></div>
          </section>
          <!-- 产品信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc0a')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc0a }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc0a" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ finishForm.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ finishForm.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ finishForm.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="finishForm.unit" /></div></div></div></div>
          </section>
          <!-- 工序与资源 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc0b')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H5a2 2 0 0 0-2 2v3a2 2 0 0 0 2 2h4"/><path d="M15 11h4a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-4"/><line x1="9" y1="13" x2="15" y2="13"/></svg></span>工序与资源</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc0b }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc0b" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">工序顺序号</span><div class="rd-value">{{ finishForm.opSeq != null ? finishForm.opSeq : '-' }}</div></div><div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ finishForm.processName || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ finishForm.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">班组</span><div class="rd-value">{{ finishForm.teamName || '-' }}</div></div></div></div>
          </section>
          <!-- 计划与进度 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc0c')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>计划与进度</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc0c }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc0c" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ finishForm.planQty != null ? finishForm.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">开工时间</span><div class="rd-value">{{ finishForm.actualStart ? parseTime(finishForm.actualStart) : '-' }}</div></div><div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><span class="badge blue"><span class="dot"></span>进行中</span></div></div></div></div>
          </section>
          <!-- 完工数据填报 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>完工数据填报</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fc1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="实际开始" prop="actualStartInput"><el-date-picker v-model="finishForm.actualStartInput" type="datetime" placeholder="选择实际开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="实际结束" prop="actualEndInput"><el-date-picker v-model="finishForm.actualEndInput" type="datetime" placeholder="选择实际结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="合格数量" prop="goodQty"><el-input-number v-model="finishForm.goodQty" :min="0" :precision="2" placeholder="请输入" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="不良数量" prop="defectQty"><el-input-number v-model="finishForm.defectQty" :min="0" :precision="2" placeholder="请输入" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-form-item label="完工备注"><el-input v-model="finishForm.remark" type="textarea" :rows="2" placeholder="不良原因、其他说明" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <div style="margin: 8px 0 0; padding: 10px 16px; background: #f0f9ff; border: 1px solid #d0e8ff; border-radius: 6px; font-size: 13px; color: #1d4ed8; display: flex; align-items: center; gap: 6px;">
        <el-icon><InfoFilled /></el-icon>
        <span>完工后系统将自动生成已审核的报工记录并联动更新工单进度。若存在后续工序，且当前工序所在并行组已全部完工（或取消），系统将自动创建下一组工序的派工单（计划数量取并行组最小合格数）。若为末道工序，且所有工序均已完工，系统将自动完工工单，无需手动操作。如需多次中间报工、首件确认或记录工时/班次，请切换到「报工记录」Tab手动新增</span>
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
            <strong>派工单（Dispatch Order）</strong>是生产管控中将工单工序任务分配到具体产能单元和人员的执行单据。派工单关联工单编号、工序顺序号、产能单元、班组和派工人员，记录计划数量与实际完成数量，通过开工→完工→取消的状态流转，实现工序级任务的精细化管控和过程可追溯。<br/><br/>
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
            <p>• <strong>工序顺序号：</strong>该工序在工单工艺路线中的先后顺序（如10、20、30…），工单下达时自动带出</p>
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
            <p style="padding-left: 16px;">③ <strong>联动更新工单进度</strong>：工单完工数量、合格数量、不良数量自动累计；工单状态从已下达(1)→执行中(2)</p>
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
    </template>
    <!-- ===== /派工管理 Tab ===== -->

    <!-- ===== 报工记录 Tab ===== -->
    <template v-if="pageTab === 'report'">
    <!-- ===== Report Filter Card ===== -->
    <div class="surface filter-card" v-show="reportShowSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': reportShowAdvanced }" @click.prevent="reportShowAdvanced = !reportShowAdvanced">
          <span>{{ reportShowAdvanced ? '收起' : '高级筛选' }}</span>
          <el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>报工单号</label>
          <div class="control">
            <el-input v-model="reportQuery.reportNo" placeholder="请输入" clearable @keyup.enter="handleReportQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control"><el-input v-model="reportQuery.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleReportQuery" /></div>
        </div>
        <div class="field">
          <label>工序</label>
          <div class="control"><el-input v-model="reportQuery.processName" placeholder="请输入" clearable @keyup.enter="handleReportQuery" /></div>
        </div>
        <div class="field">
          <label>产能单元</label>
          <div class="control"><el-input v-model="reportQuery.resourceName" placeholder="请输入" clearable @keyup.enter="handleReportQuery" /></div>
        </div>
        <div class="field" v-show="reportShowAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="reportQuery.status" placeholder="全部" clearable @change="handleReportQuery"><el-option v-for="d in mms_report_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="reportShowAdvanced">
          <label>报工人</label>
          <div class="control"><el-input v-model="reportQuery.reportBy" placeholder="请输入" clearable @keyup.enter="handleReportQuery" /></div>
        </div>
        <div class="field" v-show="reportShowAdvanced">
          <label>是否首件</label>
          <div class="control is-select"><el-select v-model="reportQuery.isFirstPiece" placeholder="全部" clearable @change="handleReportQuery"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="reportShowAdvanced">
          <label>报工来源</label>
          <div class="control is-select"><el-select v-model="reportQuery.source" placeholder="全部" clearable @change="handleReportQuery"><el-option v-for="d in mms_report_source" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="reportShowAdvanced">
          <label>报工日期</label>
          <div class="control"><el-date-picker v-model="reportDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ reportActiveFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetReportQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleReportQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== Report Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': reportActiveStatusTab === 'all' }" @click="handleReportStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ reportStatusCounts.all || 0 }}</span></button>
          <button v-for="s in reportStatusTabList" :key="s.value" class="status-tab" :class="[reportStatusTabClass(s.value), { 'is-active': reportActiveStatusTab === s.value }]" @click="handleReportStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ reportStatusCounts[s.value] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="reportShowHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>

      <!-- ===== 报工记录提示 ===== -->
      <div class="report-info-banner">
        <el-icon class="report-info-icon"><InfoFilled /></el-icon>
        <div class="report-info-text">
          <span class="report-info-title">报工记录说明</span>
          <span class="report-info-desc">派工完工时系统会<span class="emph">自动生成已审核的报工记录</span>，无需在此手动操作。此页面用于补充报工场景：<span class="emph">多次中间报工</span>（分批记录进度）、<span class="emph">首件确认</span>（质量追溯）、<span class="emph">记录工时/班次</span>（绩效统计）等。手动新增的报工需经审核通过后才生效。</span>
        </div>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleReportAdd" v-hasPermi="['mms:report:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="reportSingle" @click="handleReportUpdate" v-hasPermi="['mms:report:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="reportMultiple" @click="handleReportDelete" v-hasPermi="['mms:report:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleReportExport" v-hasPermi="['mms:report:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="reportShowSearch" @queryTable="getReportList" :columns="reportColumns" storageKey="mms_report_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="reportTableRef" v-loading="reportLoading" :data="reportDataList" border @selection-change="handleReportSelectionChange" @header-dragend="onReportHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="报工单号" prop="reportNo" key="reportNo" :width="reportColWidth('reportNo', 168)" resizable show-overflow-tooltip v-if="reportColumns.reportNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="reportColWidth('workOrderNo', 168)" resizable show-overflow-tooltip v-if="reportColumns.workOrderNo.visible" />
          <el-table-column label="工序" prop="processName" key="processName" :width="reportColWidth('processName', 120)" resizable show-overflow-tooltip v-if="reportColumns.processName.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="reportColWidth('resourceName', 144)" resizable show-overflow-tooltip v-if="reportColumns.resourceName.visible" />
          <el-table-column label="合格数" prop="goodQty" key="goodQty" :width="reportColWidth('goodQty', 96)" resizable align="center" v-if="reportColumns.goodQty.visible" />
          <el-table-column label="不良数" prop="defectQty" key="defectQty" :width="reportColWidth('defectQty', 96)" resizable align="center" v-if="reportColumns.defectQty.visible">
            <template #default="scope"><span :style="{ color: scope.row.defectQty > 0 ? '#ef4444' : 'inherit', fontWeight: scope.row.defectQty > 0 ? 600 : 400 }">{{ scope.row.defectQty }}</span></template>
          </el-table-column>
          <el-table-column label="是否首件" prop="isFirstPiece" key="isFirstPiece" :width="reportColWidth('isFirstPiece', 108)" resizable align="center" v-if="reportColumns.isFirstPiece.visible">
            <template #default="scope"><span v-if="scope.row.isFirstPiece" class="badge" :class="scope.row.isFirstPiece === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ reportIsFirstPieceLabel(scope.row.isFirstPiece) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="报工人" prop="reportBy" key="reportBy" :width="reportColWidth('reportBy', 108)" resizable align="center" v-if="reportColumns.reportBy.visible" />
          <el-table-column label="报工时间" prop="reportTime" key="reportTime" :width="reportColWidth('reportTime', 192)" resizable align="center" v-if="reportColumns.reportTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.reportTime) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="reportColWidth('status', 120)" resizable align="center" v-if="reportColumns.status.visible">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="reportBadgeClass(scope.row.status)"><span class="dot"></span>{{ reportStatusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="来源" prop="source" key="source" :width="reportColWidth('source', 120)" resizable align="center" v-if="reportColumns.source.visible">
            <template #default="scope"><span v-if="scope.row.source" class="badge" :class="reportSourceBadgeClass(scope.row.source)"><span class="dot"></span>{{ reportSourceLabel(scope.row.source) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleReportView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleReportUpdate(scope.row)" v-hasPermi="['mms:report:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="Check" @click="handleReportAudit(scope.row, '1')" v-hasPermi="['mms:report:audit']">审核</el-button>
              <el-button v-if="scope.row.status === '0'" link type="danger" icon="Close" @click="handleReportAudit(scope.row, '2')" v-hasPermi="['mms:report:audit']">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="reportTotal > 0" :total="reportTotal" v-model:page="reportQuery.pageNum" v-model:limit="reportQuery.pageSize" @pagination="getReportList" />
    </div>

    <!-- ===== Report 查看详情弹窗 ===== -->
    <el-dialog v-model="reportViewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">报工记录详情</span>
          <div class="rd-detail-header-sub" v-if="reportViewData.reportNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ reportViewData.reportNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rvc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rvc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.rvc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">报工单号</span><div class="rd-value">{{ reportViewData.reportNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ reportViewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ reportViewData.processName || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ reportViewData.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="reportViewData.status" class="badge" :class="reportBadgeClass(reportViewData.status)"><span class="dot"></span>{{ reportStatusLabel(reportViewData.status) }}</span><span v-else class="text-muted">—</span></div></div><div class="rd-item"><span class="rd-label">来源</span><div class="rd-value"><span v-if="reportViewData.source" class="badge" :class="reportSourceBadgeClass(reportViewData.source)"><span class="dot"></span>{{ reportSourceLabel(reportViewData.source) }}</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rvc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>报工数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rvc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.rvc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">合格数</span><div class="rd-value">{{ reportViewData.goodQty != null ? reportViewData.goodQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">不良数</span><div class="rd-value">{{ reportViewData.defectQty != null ? reportViewData.defectQty : '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rvc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>班组信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rvc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.rvc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">班组</span><div class="rd-value">{{ reportViewData.teamName || '-' }}</div></div><div class="rd-item"><span class="rd-label">班次</span><div class="rd-value">{{ reportViewData.shiftName || '-' }}</div></div><div class="rd-item"><span class="rd-label">是否首件</span><div class="rd-value">{{ reportViewData.isFirstPiece === '1' ? '是' : '否' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rvc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rvc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.rvc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ reportViewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ reportViewData.createTime ? parseTime(reportViewData.createTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="reportViewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== Report 编辑弹窗 ===== -->
    <el-dialog v-model="reportEditOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ reportEditTitle }}</span>
        </div>
      </template>
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.rc0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="报工单号" prop="reportNo"><el-input v-model="reportForm.reportNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单号" prop="workOrderNo"><el-input v-model="reportForm.workOrderNo" placeholder="请选择工单" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openReportWoPicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="reportForm.processName" placeholder="请选择工序" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openReportProcessPicker" :disabled="!reportForm.workOrderId" /></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="reportForm.resourceName" placeholder="请选择产能单元" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openReportResPicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="报工来源" prop="source"><el-select v-model="reportForm.source" placeholder="手动报工" disabled style="width: 100%"><el-option v-for="d in mms_report_source" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>报工数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.rc1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="合格数" prop="goodQty"><el-input-number v-model="reportForm.goodQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="不良数" prop="defectQty"><el-input-number v-model="reportForm.defectQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>班组信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.rc2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="班组" prop="teamName"><el-input v-model="reportForm.teamName" placeholder="请选择班组" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openReportTeamPicker" /></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="班次" prop="shiftName"><el-input v-model="reportForm.shiftName" placeholder="请选择班次" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openReportShiftPicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-form-item label="是否首件" prop="isFirstPiece"><el-radio-group v-model="reportForm.isFirstPiece"><el-radio value="1">是</el-radio><el-radio value="0">否</el-radio></el-radio-group></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('rc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.rc3">
              <el-form-item label="备注" prop="remark"><el-input v-model="reportForm.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitReportForm">确 定</el-button><el-button @click="reportEditOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- ===== Report 审核Dialog ===== -->
    <el-dialog :title="reportAuditTitle" v-model="reportAuditOpen" width="500px" append-to-body>
      <el-form ref="reportAuditFormRef" :model="reportAuditForm" label-width="100px">
        <el-form-item label="报工单号"><span>{{ reportAuditForm.reportNo }}</span></el-form-item>
        <el-form-item label="审核备注" prop="auditRemark"><el-input v-model="reportAuditForm.auditRemark" type="textarea" placeholder="请输入审核备注" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitReportAudit">确 定</el-button><el-button @click="reportAuditOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== Report 业务操作说明对话框 ===== -->
    <el-dialog v-model="reportShowHelp" title="报工记录业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、报工记录释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是报工记录？</div>
          <div class="highlight-card-body">
            <strong>报工记录（Work Report）</strong>是生产管控中记录工序生产进度、工时和质量的单据。报工记录工序完成数量、合格/不良数量、工时和操作人员，通过审核流程确保数据准确性，是生产进度追踪、产能分析和绩效考核的核心数据来源。<br/><br/>
            <strong>报工来源：</strong>派工管理中点击「完工」后，系统将自动生成已审核的报工记录。本页面也可手动新增报工记录，用于多次中间报工、首件确认或记录工时/班次等场景。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="warning">待审核</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">审核通过</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已通过</el-tag></div>
        </div>
        <div class="status-flow" style="margin-top:8px">
          <div class="flow-item"><el-tag type="warning">待审核</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">驳回</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="danger">已驳回</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待审核">报工新建后的初始状态，等待审核确认</el-descriptions-item>
          <el-descriptions-item label="已通过">报工审核通过，数据已生效</el-descriptions-item>
          <el-descriptions-item label="已驳回">报工审核未通过，需修改后重新提交</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>派工完工自动生成：</strong>在派工管理中完工时，系统自动生成已审核状态的报工记录，无需在此手动操作</p>
            <p>• <strong>手动新增：</strong>如需多次中间报工、首件确认或记录工时/班次，可点击「新增」手动创建</p>
            <p>• <strong>报工审核：</strong>待审核状态的报工可执行审核通过或驳回操作</p>
            <p>• <strong>首件确认：</strong>首件报工需标记"是否首件"为"是"，以便质量追溯</p>
            <p>• <strong>不良数监控：</strong>不良数大于0时自动标红显示，便于关注质量问题</p>
          </div>
        </div>
      </div>
      <template #footer><el-button type="primary" @click="reportShowHelp = false">我知道了</el-button></template>
    </el-dialog>

    <!-- ===== Report 工单选择器弹窗 ===== -->
    <el-dialog v-model="reportWoPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">选择工单</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="reportWoPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px" @keyup.enter="handleReportWoPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input v-model="reportWoPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleReportWoPickerQuery" />
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleReportWoPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetReportWoPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="reportWoPickerLoading" :data="reportWoPickerList" highlight-current-row @row-click="onReportWoRowClick" @row-dblclick="onReportWoRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="reportWoPickerSelectedId" :value="row.workOrderId" @click.stop="onReportWoRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip />
            <el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip />
            <el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip />
            <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
            <el-table-column label="计划数量" prop="planQty" width="90" align="center" />
            <el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="reportWoPickerQuery.pageNum" v-model:page-size="reportWoPickerQuery.pageSize" :total="reportWoPickerTotal" layout="total, prev, pager, next" small @current-change="getReportWoPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="reportWoPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleReportWoPickerConfirm" :disabled="!reportWoPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== Report 工序选择器弹窗 ===== -->
    <el-dialog v-model="reportProcPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">选择工序（来源：工单工艺快照）</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-table">
          <el-table v-loading="reportProcPickerLoading" :data="reportProcessOptions" highlight-current-row @row-click="onReportProcRowClick" @row-dblclick="onReportProcRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="reportProcPickerSelectedId" :value="row.processId" @click.stop="onReportProcRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="序号" prop="seq" width="60" align="center" />
            <el-table-column label="工序编码" prop="processCode" width="120" show-overflow-tooltip />
            <el-table-column label="工序名称" prop="processName" min-width="160" show-overflow-tooltip />
            <el-table-column label="绑定产能单元" prop="resourceName" min-width="140" show-overflow-tooltip />
            <el-table-column label="标准工时(分)" prop="standardHours" width="100" align="center" />
          </el-table>
        </div>
        <div v-if="reportProcessOptions.length === 0 && !reportProcPickerLoading" style="text-align:center;padding:20px;color:#909399;font-size:13px">
          该工单暂无工艺快照数据，请确认工单已下达
        </div>
      </div>
      <template #footer><el-button @click="reportProcPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleReportProcPickerConfirm" :disabled="!reportProcPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== Report 产能单元选择器弹窗 ===== -->
    <el-dialog v-model="reportResPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">选择产能单元</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="reportResPickerQuery.resourceName" placeholder="产能单元名称" clearable size="small" style="width: 200px" @keyup.enter="handleReportResPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleReportResPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetReportResPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="reportResPickerLoading" :data="reportResPickerList" highlight-current-row @row-click="onReportResRowClick" @row-dblclick="onReportResRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="reportResPickerSelectedId" :value="row.resourceId" @click.stop="onReportResRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="单元编码" prop="resourceCode" width="120" show-overflow-tooltip />
            <el-table-column label="单元名称" prop="resourceName" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" prop="resourceType" width="100" align="center"><template #default="scope"><dict-tag :options="mms_resource_type" :value="scope.row.resourceType" /></template></el-table-column>
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="reportResPickerQuery.pageNum" v-model:page-size="reportResPickerQuery.pageSize" :total="reportResPickerTotal" layout="total, prev, pager, next" small @current-change="getReportResPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="reportResPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleReportResPickerConfirm" :disabled="!reportResPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== Report 班组选择器弹窗 ===== -->
    <el-dialog v-model="reportTeamPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">选择班组</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="reportTeamPickerQuery.teamName" placeholder="班组名称" clearable size="small" style="width: 200px" @keyup.enter="handleReportTeamPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleReportTeamPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetReportTeamPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="reportTeamPickerLoading" :data="reportTeamPickerList" highlight-current-row @row-click="onReportTeamRowClick" @row-dblclick="onReportTeamRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="reportTeamPickerSelectedId" :value="row.teamId" @click.stop="onReportTeamRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="班组编码" prop="teamCode" width="120" show-overflow-tooltip />
            <el-table-column label="班组名称" prop="teamName" min-width="160" show-overflow-tooltip />
            <el-table-column label="负责人" prop="leader" width="100" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="reportTeamPickerQuery.pageNum" v-model:page-size="reportTeamPickerQuery.pageSize" :total="reportTeamPickerTotal" layout="total, prev, pager, next" small @current-change="getReportTeamPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="reportTeamPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleReportTeamPickerConfirm" :disabled="!reportTeamPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== Report 班次选择器弹窗 ===== -->
    <el-dialog v-model="reportShiftPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <span class="rd-detail-header-title">选择班次</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="reportShiftPickerQuery.shiftName" placeholder="班次名称" clearable size="small" style="width: 200px" @keyup.enter="handleReportShiftPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleReportShiftPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetReportShiftPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="reportShiftPickerLoading" :data="reportShiftPickerList" highlight-current-row @row-click="onReportShiftRowClick" @row-dblclick="onReportShiftRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="reportShiftPickerSelectedId" :value="row.shiftId" @click.stop="onReportShiftRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="班次编码" prop="shiftCode" width="120" show-overflow-tooltip />
            <el-table-column label="班次名称" prop="shiftName" min-width="120" show-overflow-tooltip />
            <el-table-column label="开始时间" prop="startTime" width="100" align="center" />
            <el-table-column label="结束时间" prop="endTime" width="100" align="center" />
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="reportShiftPickerQuery.pageNum" v-model:page-size="reportShiftPickerQuery.pageSize" :total="reportShiftPickerTotal" layout="total, prev, pager, next" small @current-change="getReportShiftPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="reportShiftPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleReportShiftPickerConfirm" :disabled="!reportShiftPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    </template>
    <!-- ===== /报工记录 Tab ===== -->
  </div>
</template>

<script setup name="Dispatch">
import { listDispatch, getDispatch,
         startDispatch, finishDispatch, cancelDispatch } from "@/api/mms/dispatch";
import { listWorkReport, getWorkReport, addWorkReport, updateWorkReport, delWorkReport, auditWorkReport } from "@/api/mms/report";
import { listWorkOrder, listWorkOrderProcesses } from "@/api/mms/workorder";
import { listTeam } from "@/api/mms/team";
import { listResource } from "@/api/mms/resource";
import { listShift } from "@/api/mms/shift";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled, InfoFilled, CircleClose, Grid, List, Box, Calendar, Tickets } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance();
const { mms_dispatch_status, wms_unit, mms_report_status, mms_yes_no, mms_report_source, mms_workorder_status, mms_resource_type } = proxy.useDict("mms_dispatch_status", "wms_unit", "mms_report_status", "mms_yes_no", "mms_report_source", "mms_workorder_status", "mms_resource_type");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_dispatch_index')
const { colWidth: reportColWidth, onHeaderDragEnd: onReportHeaderDragEnd, tableRef: reportTableRef, applySavedWidths: applyReportSavedWidths } = useColumnResize('mms_report_index')
const { collapsedCards, toggleCard } = useDetailCard(["vc0","vc1","vc2","vc3","sc0","sc0a","sc0b","sc0c","sc1","sc2","fc0","fc0a","fc0b","fc0c","fc1","rvc0","rvc1","rvc2","rvc3","rc0","rc1","rc2","rc3"])

// ===== 页面Tab切换 =====
const pageTab = ref('dispatch');
function switchPageTab(tab) {
  pageTab.value = tab;
  if (tab === 'report' && reportDataList.value.length === 0) {
    getReportList();
  }
}

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
const resourceOptions = ref([]);
const viewMode = ref('grouped'); // 'grouped' | 'flat'
const groupedList = ref([]);
const expandedGroups = ref(new Set());

// 切换视图模式
function switchViewMode(mode) {
  viewMode.value = mode;
  if (mode === 'grouped') {
    buildGroupedList();
  }
}

// 切换单个工单分组展开/折叠
function toggleGroup(workOrderNo) {
  if (expandedGroups.value.has(workOrderNo)) {
    expandedGroups.value.delete(workOrderNo);
  } else {
    expandedGroups.value.add(workOrderNo);
  }
  // 触发响应式更新
  expandedGroups.value = new Set(expandedGroups.value);
}

// 展开某个工单分组（用于点击"展开工序"按钮）
function expandAllGroup(workOrderNo) {
  expandedGroups.value.add(workOrderNo);
  expandedGroups.value = new Set(expandedGroups.value);
}

// ===== 内表格（分组视图）列宽管理 =====
const innerTableRefs = {};  // 存储 el-table 实例引用
const innerColWidths = ref({});  // 全局共享列宽配置

// 内表格列宽获取函数
function innerColWidth(prop, defaultWidth) {
  const w = innerColWidths.value[prop];
  return w ? w : defaultWidth;
}

// 设置内表格 ref
function setInnerTableRef(el, workOrderNo) {
  if (el) {
    innerTableRefs[workOrderNo] = el;
  } else {
    delete innerTableRefs[workOrderNo];
  }
}

// 内表格 header-dragend 事件处理：拖一个卡片的列，所有卡片同步生效
function onInnerHeaderDragEnd(workOrderNo, newWidth, oldWidth, column) {
  if (column && column.property) {
    // 保存到全局共享列宽
    innerColWidths.value[column.property] = Math.round(newWidth);
    // 触发响应式更新
    innerColWidths.value = { ...innerColWidths.value };
    // 同步更新所有展开的表格列宽
    nextTick(() => {
      Object.keys(innerTableRefs).forEach(key => {
        if (key !== workOrderNo && innerTableRefs[key]) {
          const table = innerTableRefs[key];
          const col = table.columns.find(c => c.property === column.property);
          if (col) {
            col.width = Math.round(newWidth);
            col.resizeWidth = Math.round(newWidth);
            table.headerChecked = false;
            table.updateLayout();
          }
        }
      });
    });
  }
}

// 构建工单分组数据
function buildGroupedList() {
  const groupMap = new Map();
  dataList.value.forEach(row => {
    const key = row.workOrderNo || row.dispatchNo;
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        workOrderNo: row.workOrderNo,
        productCode: row.productCode,
        productName: row.productName,
        specModel: row.specModel,
        unit: row.unit,
        items: [],
        totalCount: 0,
        doneCount: 0,
        statusCounts: { '0': 0, '1': 0, '2': 0, '3': 0 },
        totalPlanQty: 0,
        totalGoodQty: 0,
        totalDefectQty: 0,
        rawDefectQty: 0,
        firstOpSeq: undefined,
        firstPlanQty: 0,
        lastDoneOpSeq: undefined,
        lastDoneGoodQty: undefined,
        summaryPlanQty: 0,
        summaryGoodQty: 0,
        minPlanStart: null,
        maxPlanEnd: null,
        progressPercent: 0
      });
    }
    const g = groupMap.get(key);
    g.items.push(row);
    g.totalCount++;
    if (row.status === '2') g.doneCount++;
    if (row.status !== undefined && g.statusCounts[row.status] !== undefined) {
      g.statusCounts[row.status]++;
    }
    // 数量汇总（修正逻辑：不简单求和）
    // 计划数量：取首工序（op_seq最小的一组）的计划数量，并行取最小值
    if (row.planQty != null) g.totalPlanQty += Number(row.planQty) || 0;
    if (row.goodQty != null) g.totalGoodQty += Number(row.goodQty) || 0;
    if (row.defectQty != null) g.rawDefectQty += Number(row.defectQty) || 0;
    // 记录首工序的计划数量（op_seq最小）
    if (row.opSeq != null) {
      if (g.firstOpSeq === undefined || row.opSeq < g.firstOpSeq) {
        g.firstOpSeq = row.opSeq;
        g.firstPlanQty = Number(row.planQty) || 0;
      } else if (row.opSeq === g.firstOpSeq) {
 // 并行工序：取最小值
        g.firstPlanQty = Math.min(g.firstPlanQty, Number(row.planQty) || 0);
      }
      // 记录最大 op_seq 的已完成工序的合格数量（最终产出）
      if (row.status === '2') {
        if (g.lastDoneOpSeq === undefined || row.opSeq >= g.lastDoneOpSeq) {
          if (row.opSeq > g.lastDoneOpSeq || g.lastDoneGoodQty === undefined) {
            g.lastDoneOpSeq = row.opSeq;
            g.lastDoneGoodQty = Number(row.goodQty) || 0;
          } else {
            // 同一 op_seq 的并行工序，取最小合格数
            g.lastDoneGoodQty = Math.min(g.lastDoneGoodQty, Number(row.goodQty) || 0);
          }
        }
      }
    }
    // 时间范围
    if (row.planStart) {
      if (!g.minPlanStart || row.planStart < g.minPlanStart) g.minPlanStart = row.planStart;
    }
    if (row.planEnd) {
      if (!g.maxPlanEnd || row.planEnd > g.maxPlanEnd) g.maxPlanEnd = row.planEnd;
    }
  });
  // 计算进度百分比并格式化时间
  const result = Array.from(groupMap.values());
  result.forEach(g => {
    g.progressPercent = g.totalCount > 0 ? Math.round((g.doneCount / g.totalCount) * 100) : 0;
    // 计划数量：取首工序计划数量（代表工单总投入量）
    g.summaryPlanQty = g.firstPlanQty || 0;
    // 合格数量：取最后一道已完成工序的合格数量（代表当前最终产出）
    g.summaryGoodQty = g.lastDoneGoodQty !== undefined ? g.lastDoneGoodQty : 0;
    // 不良数量：工单总投入量 - 最终合格数量（串行工序不良是递进淘汰的，不能简单累加）
    // 注意：如果没有已完工工序，不良数量应为0
    if (g.lastDoneGoodQty !== undefined && g.lastDoneOpSeq !== undefined) {
      g.totalDefectQty = Math.max(0, (g.firstPlanQty || 0) - g.lastDoneGoodQty);
    } else {
      g.totalDefectQty = 0;
    }
    g.totalPlanQty = Number(g.totalPlanQty.toFixed(2));
    g.totalGoodQty = Number(g.totalGoodQty.toFixed(2));
    g.totalDefectQty = Number(g.totalDefectQty.toFixed(2));
    g.summaryPlanQty = Number(g.summaryPlanQty.toFixed(2));
    g.summaryGoodQty = Number(g.summaryGoodQty.toFixed(2));
    if (g.minPlanStart) g.minPlanStart = proxy.parseTime(g.minPlanStart, '{y}-{m}-{d}');
    if (g.maxPlanEnd) g.maxPlanEnd = proxy.parseTime(g.maxPlanEnd, '{y}-{m}-{d}');
    // 按工序顺序号排序
    g.items.sort((a, b) => (a.opSeq || 0) - (b.opSeq || 0));
  });
  groupedList.value = result;
  // 默认展开第一个分组
  if (result.length > 0 && expandedGroups.value.size === 0) {
    expandedGroups.value.add(result[0].workOrderNo);
    expandedGroups.value = new Set(expandedGroups.value);
  }
}

// 加载班组下拉
function loadTeamOptions() {
  listTeam({ pageNum: 1, pageSize: 999, status: '0' }).then(response => {
    teamOptions.value = response.rows || [];
  });
}

// 加载产能单元下拉
function loadResourceOptions() {
  listResource({ pageNum: 1, pageSize: 999, status: '0' }).then(response => {
    resourceOptions.value = response.rows || [];
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
  opSeq: { label: '工序顺序号', visible: true },
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
  resourceId: undefined,
  resourceName: '',
  teamId: undefined,
  teamName: '',
  planQty: 0,
  planStart: undefined,
  planEnd: undefined,
  operatorName: '',
  operateTime: ''
});
const startRules = {
  resourceName: [{ required: true, message: "请选择产能单元", trigger: "change" }],
  teamName: [{ required: true, message: "请选择班组", trigger: "change" }],
  operatorName: [{ required: true, message: "请选择操作人员", trigger: "change" }],
  operateTime: [{ required: true, message: "操作时间不能为空", trigger: "change" }]
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
  planStart: undefined,         // 计划开始时间
  planEnd: undefined,           // 计划结束时间
  actualStart: undefined,       // 开工时记录的时间（只读展示）
  actualStartInput: undefined,   // 完工时可编辑的实际开始时间
  actualEndInput: undefined,    // 完工时可编辑的实际结束时间
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
    if (viewMode.value === 'grouped') {
      buildGroupedList();
    }
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
  // 校验关联工单状态：暂停(7)或作废(8)时不允许开工
  if (row.workOrderStatus === '7') {
    proxy.$modal.msgWarning("关联工单[" + (row.workOrderNo || '') + "]已暂停，请先恢复工单后再开工");
    return;
  }
  if (row.workOrderStatus === '8') {
    proxy.$modal.msgWarning("关联工单[" + (row.workOrderNo || '') + "]已作废，派工单无法开工");
    return;
  }
  startForm.dispatchId = row.dispatchId;
  startForm.dispatchNo = row.dispatchNo;
  startForm.workOrderNo = row.workOrderNo;
  startForm.productCode = row.productCode;
  startForm.productName = row.productName;
  startForm.specModel = row.specModel;
  startForm.unit = row.unit;
  startForm.opSeq = row.opSeq;
  startForm.processName = row.processName;
  startForm.resourceId = row.resourceId;
  startForm.resourceName = row.resourceName;
  startForm.teamId = row.teamId;
  startForm.teamName = row.teamName;
  startForm.planQty = row.planQty;
  startForm.planStart = row.planStart;
  startForm.planEnd = row.planEnd;
  // 默认取当前登录用户昵称
  startForm.operatorName = useUserStore().nickName;
  // 默认取当前时间
  startForm.operateTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
  startOpen.value = true;
}

// ===== 开工-操作人员选择 =====
function openOperatorPicker() {
  proxy.$refs.operatorPickerRef.open();
}
function onOperatorPickerConfirm(user) {
  startForm.operatorName = user.nickName;
  proxy.$refs["startFormRef"] && proxy.$refs["startFormRef"].validateField('operatorName');
}

// ===== 开工-产能单元选择弹窗 =====
const startResourcePickerOpen = ref(false)
const startResourcePickerLoading = ref(false)
const startResourcePickerList = ref([])
const startResourcePickerTotal = ref(0)
const startResourcePickerSelectedId = ref(null)
const startResourcePickerSelectedRow = ref(null)
const startResourcePickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, lineName: undefined, status: '0' })

function openStartResourcePicker() {
  startResourcePickerOpen.value = true
  startResourcePickerSelectedId.value = startForm.resourceId || null
  startResourcePickerSelectedRow.value = null
  startResourcePickerQuery.pageNum = 1
  startResourcePickerQuery.resourceName = undefined
  startResourcePickerQuery.lineName = undefined
  getStartResourcePickerList()
}
function getStartResourcePickerList() {
  startResourcePickerLoading.value = true
  listResource(startResourcePickerQuery).then(res => {
    startResourcePickerList.value = res.rows
    startResourcePickerTotal.value = res.total
    startResourcePickerLoading.value = false
  }).catch(() => { startResourcePickerLoading.value = false })
}
function handleStartResourcePickerQuery() { startResourcePickerQuery.pageNum = 1; getStartResourcePickerList() }
function resetStartResourcePickerQuery() { startResourcePickerQuery.resourceName = undefined; startResourcePickerQuery.lineName = undefined; handleStartResourcePickerQuery() }
function onStartResourceRowClick(row) { startResourcePickerSelectedId.value = row.resourceId; startResourcePickerSelectedRow.value = row }
function onStartResourceRowDblClick(row) { onStartResourceRowClick(row); confirmStartResourcePicker() }
function confirmStartResourcePicker() {
  if (!startResourcePickerSelectedRow.value) { proxy.$modal.msgWarning('请先选择产能单元'); return }
  const row = startResourcePickerSelectedRow.value
  startForm.resourceId = row.resourceId
  startForm.resourceName = row.resourceName
  startResourcePickerOpen.value = false
  // 触发表单校验
  proxy.$refs["startFormRef"] && proxy.$refs["startFormRef"].validateField('resourceName')
}
function clearStartResource() {
  startForm.resourceId = undefined
  startForm.resourceName = ''
  proxy.$refs["startFormRef"] && proxy.$refs["startFormRef"].validateField('resourceName')
}

// ===== 开工-班组选择弹窗 =====
const startTeamPickerOpen = ref(false)
const startTeamPickerLoading = ref(false)
const startTeamPickerList = ref([])
const startTeamPickerTotal = ref(0)
const startTeamPickerSelectedId = ref(null)
const startTeamPickerSelectedRow = ref(null)
const startTeamPickerQuery = reactive({ pageNum: 1, pageSize: 10, teamName: undefined, status: '0' })

function openStartTeamPicker() {
  startTeamPickerOpen.value = true
  startTeamPickerSelectedId.value = startForm.teamId || null
  startTeamPickerSelectedRow.value = null
  startTeamPickerQuery.pageNum = 1
  startTeamPickerQuery.teamName = undefined
  getStartTeamPickerList()
}
function getStartTeamPickerList() {
  startTeamPickerLoading.value = true
  listTeam(startTeamPickerQuery).then(res => {
    startTeamPickerList.value = res.rows
    startTeamPickerTotal.value = res.total
    startTeamPickerLoading.value = false
  }).catch(() => { startTeamPickerLoading.value = false })
}
function handleStartTeamPickerQuery() { startTeamPickerQuery.pageNum = 1; getStartTeamPickerList() }
function resetStartTeamPickerQuery() { startTeamPickerQuery.teamName = undefined; handleStartTeamPickerQuery() }
function onStartTeamRowClick(row) { startTeamPickerSelectedId.value = row.teamId; startTeamPickerSelectedRow.value = row }
function onStartTeamRowDblClick(row) { onStartTeamRowClick(row); confirmStartTeamPicker() }
function confirmStartTeamPicker() {
  if (!startTeamPickerSelectedRow.value) { proxy.$modal.msgWarning('请先选择班组'); return }
  const row = startTeamPickerSelectedRow.value
  startForm.teamId = row.teamId
  startForm.teamName = row.teamName
  startTeamPickerOpen.value = false
  // 触发表单校验
  proxy.$refs["startFormRef"] && proxy.$refs["startFormRef"].validateField('teamName')
}
function clearStartTeam() {
  startForm.teamId = undefined
  startForm.teamName = ''
  proxy.$refs["startFormRef"] && proxy.$refs["startFormRef"].validateField('teamName')
}

function submitStart() {
  proxy.$refs["startFormRef"].validate(valid => {
    if (valid) {
      startDispatch(startForm.dispatchId, {
        operatorName: startForm.operatorName,
        operateTime: startForm.operateTime,
        teamId: startForm.teamId,
        teamName: startForm.teamName,
        resourceId: startForm.resourceId,
        resourceName: startForm.resourceName
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
  finishForm.planStart = row.planStart;
  finishForm.planEnd = row.planEnd;
  finishForm.actualStart = row.actualStart;
  // 默认实际开始时间 = 计划开始时间，若没有则取开工记录时间，再没有取当前时间
  finishForm.actualStartInput = row.planStart ? proxy.parseTime(row.planStart)
    : (row.actualStart ? proxy.parseTime(row.actualStart) : proxy.parseTime(new Date()));
  // 默认实际结束时间 = 当前时间
  finishForm.actualEndInput = proxy.parseTime(new Date());
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
      // 前端校验实际结束时间不能早于计划开始时间
      if (finishForm.planStart && finishForm.actualEndInput) {
        const planStart = new Date(finishForm.planStart);
        const end = new Date(finishForm.actualEndInput);
        if (end < planStart) {
          proxy.$modal.msgError("实际结束时间不能早于计划开始时间");
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
        proxy.$modal.msgSuccess("完工成功，已自动生成报工记录，若为末道工序将自动完工工单");
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
/** 判断工单分组是否已完工（所有派工单都已完成或取消） */
function isGroupFinished(group) {
  if (!group || !group.items || group.items.length === 0) return false;
  // 如果所有工序都已完成(2)或取消(3)，则视为已完工
  return group.items.every(item => item.status === '2' || item.status === '3');
}

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
loadResourceOptions();
getList();

// ============================================================
// ===== 报工记录 Tab 逻辑 =====
// ============================================================

// ===== 报工记录列显隐配置 =====
const reportDefaultColumns = {
  reportNo: { label: '报工单号', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  processName: { label: '工序', visible: true },
  resourceName: { label: '产能单元', visible: true },
  goodQty: { label: '合格数', visible: true },
  defectQty: { label: '不良数', visible: true },
  isFirstPiece: { label: '是否首件', visible: true },
  reportBy: { label: '报工人', visible: true },
  reportTime: { label: '报工时间', visible: true },
  status: { label: '状态', visible: true },
  source: { label: '来源', visible: true }
}
function loadReportColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_report_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(reportDefaultColumns).forEach(key => {
        result[key] = {
          label: reportDefaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : reportDefaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...reportDefaultColumns }
}
const reportColumns = ref(loadReportColumnVisibility())

const reportDataList = ref([]);
const reportLoading = ref(true);
const reportShowSearch = ref(true);
const reportShowAdvanced = ref(false);
const reportTotal = ref(0);
const reportDateRange = ref([]);
const reportActiveStatusTab = ref("all");
const reportStatusCounts = ref({});
const reportShowHelp = ref(false);
const reportViewOpen = ref(false);
const reportViewData = ref({});
const reportEditOpen = ref(false);
const reportEditTitle = ref("");
const reportIds = ref([]);
const reportSingle = ref(true);
const reportMultiple = ref(true);
const reportAuditOpen = ref(false);
const reportAuditTitle = ref("");

const reportStatusTabList = computed(() => mms_report_status.value ? mms_report_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const reportActiveFilterCount = computed(() => {
  let c = 0;
  if (reportQuery.value.reportNo) c++;
  if (reportQuery.value.workOrderNo) c++;
  if (reportQuery.value.processName) c++;
  if (reportQuery.value.resourceName) c++;
  if (reportQuery.value.status) c++;
  if (reportQuery.value.reportBy) c++;
  if (reportQuery.value.isFirstPiece) c++;
  if (reportQuery.value.source) c++;
  if (reportDateRange.value && reportDateRange.value.length === 2) c++;
  return c;
});

const reportData = reactive({
  reportForm: {},
  reportQuery: { pageNum: 1, pageSize: 10, reportNo: undefined, workOrderNo: undefined, processName: undefined, resourceName: undefined, status: undefined, reportBy: undefined, isFirstPiece: undefined, source: undefined, params: {} },
  reportRules: {
    workOrderNo: [{ required: true, message: "请选择工单", trigger: "change" }],
    processName: [{ required: true, message: "请选择工序", trigger: "change" }],
    resourceName: [{ required: true, message: "请选择产能单元", trigger: "change" }],
    goodQty: [{ required: true, message: "请输入合格数", trigger: "blur" }]
  },
  reportAuditForm: {}
});
const { reportQuery, reportForm, reportRules, reportAuditForm } = toRefs(reportData);

function getReportList() {
  reportLoading.value = true;
  listWorkReport(proxy.addDateRange(reportQuery.value, reportDateRange.value)).then(response => {
    reportDataList.value = response.rows;
    reportTotal.value = response.total;
    reportLoading.value = false;
    applyReportSavedWidths();
    loadReportStatusCounts();
  });
}

function loadReportStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (reportQuery.value.reportNo) baseQuery.reportNo = reportQuery.value.reportNo;
  if (reportQuery.value.workOrderNo) baseQuery.workOrderNo = reportQuery.value.workOrderNo;
  if (reportQuery.value.processName) baseQuery.processName = reportQuery.value.processName;
  if (reportQuery.value.resourceName) baseQuery.resourceName = reportQuery.value.resourceName;
  if (reportQuery.value.reportBy) baseQuery.reportBy = reportQuery.value.reportBy;
  if (reportQuery.value.isFirstPiece) baseQuery.isFirstPiece = reportQuery.value.isFirstPiece;
  if (reportQuery.value.source) baseQuery.source = reportQuery.value.source;
  listWorkReport(proxy.addDateRange(baseQuery, reportDateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_report_status.value) {
      mms_report_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    reportStatusCounts.value = counts;
  }).catch(() => {});
}

function handleReportQuery() {
  reportShowAdvanced.value = false;
  reportQuery.value.pageNum = 1;
  getReportList();
}

function resetReportQuery() {
  reportQuery.value.reportNo = undefined;
  reportQuery.value.workOrderNo = undefined;
  reportQuery.value.processName = undefined;
  reportQuery.value.resourceName = undefined;
  reportQuery.value.status = undefined;
  reportQuery.value.reportBy = undefined;
  reportQuery.value.isFirstPiece = undefined;
  reportQuery.value.source = undefined;
  reportDateRange.value = [];
  reportQuery.value.params = {};
  reportActiveStatusTab.value = 'all';
  handleReportQuery();
}

function handleReportStatusTabClick(status) {
  reportActiveStatusTab.value = status;
  reportQuery.value.status = status === "all" ? undefined : status;
  handleReportQuery();
}

function handleReportSelectionChange(selection) {
  reportIds.value = selection.map(item => item.reportId);
  reportSingle.value = selection.length !== 1;
  reportMultiple.value = !selection.length;
}

function resetReportForm() {
  reportForm.value = { reportNo: undefined, workOrderId: undefined, workOrderNo: undefined, processId: undefined, processName: undefined, resourceId: undefined, resourceName: undefined, goodQty: 0, defectQty: 0, teamId: undefined, teamName: undefined, shiftId: undefined, shiftName: undefined, isFirstPiece: "0", source: "1", remark: undefined };
  reportProcessOptions.value = [];
  proxy.resetForm("reportFormRef");
}

function handleReportAdd() {
  resetReportForm();
  reportEditOpen.value = true;
  reportEditTitle.value = "新增报工";
}

function handleReportUpdate(row) {
  resetReportForm();
  const id = row.reportId || reportIds.value[0];
  getWorkReport(id).then(response => {
    reportForm.value = response.data;
    if (reportForm.value.workOrderId) {
      listWorkOrderProcesses(reportForm.value.workOrderId).then(res => {
        reportProcessOptions.value = res.data || [];
        reportEditOpen.value = true;
        reportEditTitle.value = "修改报工";
      }).catch(() => { reportEditOpen.value = true; reportEditTitle.value = "修改报工"; });
    } else {
      reportEditOpen.value = true;
      reportEditTitle.value = "修改报工";
    }
  });
}

function handleReportView(row) {
  const id = row.reportId || reportIds.value[0];
  getWorkReport(id).then(response => { reportViewData.value = response.data; reportViewOpen.value = true; });
}

function submitReportForm() {
  proxy.$refs["reportFormRef"].validate(valid => {
    if (valid) {
      if (reportForm.value.reportId != null) {
        updateWorkReport(reportForm.value).then(() => { proxy.$modal.msgSuccess("修改成功"); reportEditOpen.value = false; getReportList(); });
      } else {
        addWorkReport(reportForm.value).then(() => { proxy.$modal.msgSuccess("新增成功"); reportEditOpen.value = false; getReportList(); });
      }
    }
  });
}

function handleReportDelete(row) {
  const delIds = row.reportId || reportIds.value;
  proxy.$modal.confirm('是否确认删除选中的报工记录？').then(() => delWorkReport(delIds)).then(() => { getReportList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}

function handleReportExport() {
  proxy.download("mms/report/export", { ...reportQuery.value }, `report_${new Date().getTime()}.xlsx`);
}

function handleReportAudit(row, status) {
  reportAuditTitle.value = status === "1" ? "报工审核-通过" : "报工审核-驳回";
  reportAuditForm.value = { reportId: row.reportId, reportNo: row.reportNo, status: status, auditRemark: "" };
  reportAuditOpen.value = true;
}

function submitReportAudit() {
  auditWorkReport(reportAuditForm.value.reportId, reportAuditForm.value.status, reportAuditForm.value.auditRemark).then(() => {
    reportAuditOpen.value = false;
    getReportList();
    proxy.$modal.msgSuccess("操作成功");
  });
}

// ===== 报工字典辅助 =====
function reportDictLabel(dictRef, value) {
  if (value === null || value === undefined || value === '') return '—';
  const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef;
  if (!arr || !Array.isArray(arr)) return '—';
  const item = arr.find(d => d.value == value);
  return item ? item.label : '—';
}
function reportStatusLabel(status) { return reportDictLabel(mms_report_status, status); }
function reportIsFirstPieceLabel(val) { return reportDictLabel(mms_yes_no, val); }
function reportBadgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'red' }; return map[status] || 'gray'; }
function reportStatusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-reject' }; return map[value] || ''; }
function reportSourceLabel(value) { return reportDictLabel(mms_report_source, value); }
function reportSourceBadgeClass(value) { const map = { '1': 'blue', '2': 'green' }; return map[value] || 'gray'; }

// ===== 报工-工单选择器 =====
const reportWoPickerOpen = ref(false);
const reportWoPickerLoading = ref(false);
const reportWoPickerList = ref([]);
const reportWoPickerTotal = ref(0);
const reportWoPickerSelectedId = ref(null);
const reportWoPickerSelectedRow = ref(null);
const reportWoPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined });

function openReportWoPicker() {
  reportWoPickerOpen.value = true;
  reportWoPickerSelectedId.value = reportForm.value.workOrderId || null;
  reportWoPickerSelectedRow.value = null;
  reportWoPickerQuery.pageNum = 1;
  reportWoPickerQuery.workOrderNo = undefined;
  reportWoPickerQuery.productName = undefined;
  getReportWoPickerList();
}
function getReportWoPickerList() {
  reportWoPickerLoading.value = true;
  listWorkOrder(reportWoPickerQuery).then(res => {
    reportWoPickerList.value = res.rows;
    reportWoPickerTotal.value = res.total;
    reportWoPickerLoading.value = false;
  }).catch(() => { reportWoPickerLoading.value = false; });
}
function handleReportWoPickerQuery() { reportWoPickerQuery.pageNum = 1; getReportWoPickerList(); }
function resetReportWoPickerQuery() { reportWoPickerQuery.workOrderNo = undefined; reportWoPickerQuery.productName = undefined; handleReportWoPickerQuery(); }
function onReportWoRowClick(row) { reportWoPickerSelectedId.value = row.workOrderId; reportWoPickerSelectedRow.value = row; }
function onReportWoRowDblClick(row) { onReportWoRowClick(row); handleReportWoPickerConfirm(); }
function handleReportWoPickerConfirm() {
  if (!reportWoPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工单'); return; }
  const row = reportWoPickerSelectedRow.value;
  reportForm.value.workOrderId = row.workOrderId;
  reportForm.value.workOrderNo = row.workOrderNo;
  reportForm.value.processId = undefined;
  reportForm.value.processName = undefined;
  reportProcessOptions.value = [];
  reportWoPickerOpen.value = false;
  proxy.$refs["reportFormRef"] && proxy.$refs["reportFormRef"].validateField('workOrderNo');
  listWorkOrderProcesses(row.workOrderId).then(res => {
    reportProcessOptions.value = res.data || [];
  }).catch(() => {});
}

// ===== 报工-工序选择器 =====
const reportProcPickerOpen = ref(false);
const reportProcPickerLoading = ref(false);
const reportProcPickerSelectedId = ref(null);
const reportProcPickerSelectedRow = ref(null);
const reportProcessOptions = ref([]);

function openReportProcessPicker() {
  if (!reportForm.value.workOrderId) { proxy.$modal.msgWarning('请先选择工单'); return; }
  reportProcPickerOpen.value = true;
  reportProcPickerSelectedId.value = reportForm.value.processId || null;
  reportProcPickerSelectedRow.value = null;
  if (reportProcessOptions.value.length === 0) {
    reportProcPickerLoading.value = true;
    listWorkOrderProcesses(reportForm.value.workOrderId).then(res => {
      reportProcessOptions.value = res.data || [];
      reportProcPickerLoading.value = false;
    }).catch(() => { reportProcPickerLoading.value = false; });
  }
}
function onReportProcRowClick(row) { reportProcPickerSelectedId.value = row.processId; reportProcPickerSelectedRow.value = row; }
function onReportProcRowDblClick(row) { onReportProcRowClick(row); handleReportProcPickerConfirm(); }
function handleReportProcPickerConfirm() {
  if (!reportProcPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工序'); return; }
  const row = reportProcPickerSelectedRow.value;
  reportForm.value.processId = row.processId;
  reportForm.value.processName = row.processName;
  if (row.resourceId) {
    reportForm.value.resourceId = row.resourceId;
    reportForm.value.resourceName = row.resourceName;
  }
  reportProcPickerOpen.value = false;
  proxy.$refs["reportFormRef"] && proxy.$refs["reportFormRef"].validateField('processName');
}

// ===== 报工-产能单元选择器 =====
const reportResPickerOpen = ref(false);
const reportResPickerLoading = ref(false);
const reportResPickerList = ref([]);
const reportResPickerTotal = ref(0);
const reportResPickerSelectedId = ref(null);
const reportResPickerSelectedRow = ref(null);
const reportResPickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, status: '0' });

function openReportResPicker() {
  reportResPickerOpen.value = true;
  reportResPickerSelectedId.value = reportForm.value.resourceId || null;
  reportResPickerSelectedRow.value = null;
  reportResPickerQuery.pageNum = 1;
  reportResPickerQuery.resourceName = undefined;
  getReportResPickerList();
}
function getReportResPickerList() {
  reportResPickerLoading.value = true;
  listResource(reportResPickerQuery).then(res => {
    reportResPickerList.value = res.rows;
    reportResPickerTotal.value = res.total;
    reportResPickerLoading.value = false;
  }).catch(() => { reportResPickerLoading.value = false; });
}
function handleReportResPickerQuery() { reportResPickerQuery.pageNum = 1; getReportResPickerList(); }
function resetReportResPickerQuery() { reportResPickerQuery.resourceName = undefined; handleReportResPickerQuery(); }
function onReportResRowClick(row) { reportResPickerSelectedId.value = row.resourceId; reportResPickerSelectedRow.value = row; }
function onReportResRowDblClick(row) { onReportResRowClick(row); handleReportResPickerConfirm(); }
function handleReportResPickerConfirm() {
  if (!reportResPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择产能单元'); return; }
  const row = reportResPickerSelectedRow.value;
  reportForm.value.resourceId = row.resourceId;
  reportForm.value.resourceName = row.resourceName;
  reportResPickerOpen.value = false;
  proxy.$refs["reportFormRef"] && proxy.$refs["reportFormRef"].validateField('resourceName');
}

// ===== 报工-班组选择器 =====
const reportTeamPickerOpen = ref(false);
const reportTeamPickerLoading = ref(false);
const reportTeamPickerList = ref([]);
const reportTeamPickerTotal = ref(0);
const reportTeamPickerSelectedId = ref(null);
const reportTeamPickerSelectedRow = ref(null);
const reportTeamPickerQuery = reactive({ pageNum: 1, pageSize: 10, teamName: undefined, status: '0' });

function openReportTeamPicker() {
  reportTeamPickerOpen.value = true;
  reportTeamPickerSelectedId.value = reportForm.value.teamId || null;
  reportTeamPickerSelectedRow.value = null;
  reportTeamPickerQuery.pageNum = 1;
  reportTeamPickerQuery.teamName = undefined;
  getReportTeamPickerList();
}
function getReportTeamPickerList() {
  reportTeamPickerLoading.value = true;
  listTeam(reportTeamPickerQuery).then(res => {
    reportTeamPickerList.value = res.rows;
    reportTeamPickerTotal.value = res.total;
    reportTeamPickerLoading.value = false;
  }).catch(() => { reportTeamPickerLoading.value = false; });
}
function handleReportTeamPickerQuery() { reportTeamPickerQuery.pageNum = 1; getReportTeamPickerList(); }
function resetReportTeamPickerQuery() { reportTeamPickerQuery.teamName = undefined; handleReportTeamPickerQuery(); }
function onReportTeamRowClick(row) { reportTeamPickerSelectedId.value = row.teamId; reportTeamPickerSelectedRow.value = row; }
function onReportTeamRowDblClick(row) { onReportTeamRowClick(row); handleReportTeamPickerConfirm(); }
function handleReportTeamPickerConfirm() {
  if (!reportTeamPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择班组'); return; }
  const row = reportTeamPickerSelectedRow.value;
  reportForm.value.teamId = row.teamId;
  reportForm.value.teamName = row.teamName;
  reportTeamPickerOpen.value = false;
}

// ===== 报工-班次选择器 =====
const reportShiftPickerOpen = ref(false);
const reportShiftPickerLoading = ref(false);
const reportShiftPickerList = ref([]);
const reportShiftPickerTotal = ref(0);
const reportShiftPickerSelectedId = ref(null);
const reportShiftPickerSelectedRow = ref(null);
const reportShiftPickerQuery = reactive({ pageNum: 1, pageSize: 10, shiftName: undefined, status: '0' });

function openReportShiftPicker() {
  reportShiftPickerOpen.value = true;
  reportShiftPickerSelectedId.value = reportForm.value.shiftId || null;
  reportShiftPickerSelectedRow.value = null;
  reportShiftPickerQuery.pageNum = 1;
  reportShiftPickerQuery.shiftName = undefined;
  getReportShiftPickerList();
}
function getReportShiftPickerList() {
  reportShiftPickerLoading.value = true;
  listShift(reportShiftPickerQuery).then(res => {
    reportShiftPickerList.value = res.rows;
    reportShiftPickerTotal.value = res.total;
    reportShiftPickerLoading.value = false;
  }).catch(() => { reportShiftPickerLoading.value = false; });
}
function handleReportShiftPickerQuery() { reportShiftPickerQuery.pageNum = 1; getReportShiftPickerList(); }
function resetReportShiftPickerQuery() { reportShiftPickerQuery.shiftName = undefined; handleReportShiftPickerQuery(); }
function onReportShiftRowClick(row) { reportShiftPickerSelectedId.value = row.shiftId; reportShiftPickerSelectedRow.value = row; }
function onReportShiftRowDblClick(row) { onReportShiftRowClick(row); handleReportShiftPickerConfirm(); }
function handleReportShiftPickerConfirm() {
  if (!reportShiftPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择班次'); return; }
  const row = reportShiftPickerSelectedRow.value;
  reportForm.value.shiftId = row.shiftId;
  reportForm.value.shiftName = row.shiftName;
  reportShiftPickerOpen.value = false;
}
// ===== /报工记录 Tab 逻辑 =====
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
.rd-page { max-width: 100%; margin: 0 auto; }
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
/* ===== View Mode Switch Bar ===== */
.mms-dispatch-page .view-mode-bar { display: flex; align-items: center; justify-content: flex-end; padding: 8px 20px; border-bottom: 1px solid var(--ink-200); background: var(--ink-50); }
.mms-dispatch-page .view-mode-tabs { display: inline-flex; gap: 4px; padding: 3px; background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-sm); }
.mms-dispatch-page .view-mode-tab { display: inline-flex; align-items: center; gap: 5px; height: 28px; padding: 0 12px; border: none; background: transparent; border-radius: 4px; font-size: 13px; color: var(--ink-500); cursor: pointer; transition: all .15s var(--ease-out); white-space: nowrap; }
.mms-dispatch-page .view-mode-tab:hover { background: var(--ink-50); color: var(--ink-700); }
.mms-dispatch-page .view-mode-tab.is-active { background: var(--brand-600); color: #fff; font-weight: 600; }
.mms-dispatch-page .view-mode-tab.is-active .el-icon { color: #fff; }
.mms-dispatch-page .view-mode-tab .el-icon { font-size: 14px; }
/* ===== Grouped View ===== */
.mms-dispatch-page .grouped-view { padding: 12px 20px; }
.mms-dispatch-page .empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; color: var(--ink-400); }
.mms-dispatch-page .empty-state .empty-icon { font-size: 48px; margin-bottom: 12px; }
.mms-dispatch-page .empty-state p { font-size: 14px; margin: 0; }
.mms-dispatch-page .wo-group { background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-md); margin-bottom: 10px; overflow: hidden; box-shadow: var(--shadow-card); transition: border-color .15s; }
.mms-dispatch-page .wo-group:hover { border-color: var(--brand-200); }
.mms-dispatch-page .wo-group-header { display: flex; align-items: center; gap: 12px; padding: 14px 16px; cursor: pointer; background: #fff; transition: background .15s; user-select: none; }
.mms-dispatch-page .wo-group-header:hover { background: var(--ink-50); }
.mms-dispatch-page .wo-group-toggle { flex: 0 0 auto; display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: var(--r-sm); background: var(--ink-100); color: var(--ink-500); transition: all .2s var(--ease-out); }
.mms-dispatch-page .wo-group-header:hover .wo-group-toggle { background: var(--brand-100); color: var(--brand-600); }
.mms-dispatch-page .wo-group-toggle .toggle-icon { font-size: 14px; transition: transform .2s var(--ease-out); }
.mms-dispatch-page .wo-group-toggle .toggle-icon.is-expanded { transform: rotate(90deg); }
.mms-dispatch-page .wo-group-main { flex: 1 1 auto; min-width: 0; }
.mms-dispatch-page .wo-group-line1 { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; flex-wrap: wrap; }
.mms-dispatch-page .wo-group-line1 .wo-no { font-size: 15px; font-weight: 700; color: var(--ink-900); font-variant-numeric: tabular-nums; }
.mms-dispatch-page .wo-group-line1 .wo-product { font-size: 14px; font-weight: 600; color: var(--ink-700); }
.mms-dispatch-page .wo-group-line1 .wo-code { font-size: 13px; font-variant-numeric: tabular-nums; }
.mms-dispatch-page .wo-group-line1 .wo-spec { font-size: 13px; }
.mms-dispatch-page .wo-group-line2 { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
/* Progress */
.mms-dispatch-page .wo-progress { display: flex; align-items: center; gap: 8px; }
.mms-dispatch-page .wo-progress-label { font-size: 12px; color: var(--ink-400); font-weight: 500; }
.mms-dispatch-page .wo-progress-value { font-size: 13px; font-weight: 700; color: var(--ink-700); font-variant-numeric: tabular-nums; }
.mms-dispatch-page .wo-progress-bar { width: 80px; height: 6px; background: var(--ink-100); border-radius: 999px; overflow: hidden; }
.mms-dispatch-page .wo-progress-fill { height: 100%; background: linear-gradient(90deg, var(--green-500), #34d399); border-radius: 999px; transition: width .3s var(--ease-out); }
/* Status chips */
.mms-dispatch-page .wo-status-stats { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.mms-dispatch-page .stat-chip { display: inline-flex; align-items: center; gap: 3px; height: 22px; padding: 0 8px; border-radius: 999px; font-size: 12px; font-weight: 600; border: 1px solid transparent; white-space: nowrap; }
.mms-dispatch-page .stat-chip.stat-pending { background: var(--amber-50); color: var(--amber-700); border-color: #fde68a; }
.mms-dispatch-page .stat-chip.stat-running { background: var(--blue-50); color: var(--blue-700); border-color: #bfdbfe; }
.mms-dispatch-page .stat-chip.stat-done { background: var(--green-50); color: var(--green-700); border-color: #a7f3d0; }
.mms-dispatch-page .stat-chip.stat-cancel { background: var(--ink-100); color: var(--ink-500); border-color: var(--ink-200); }
/* Qty stats */
.mms-dispatch-page .wo-qty-stats { display: flex; align-items: center; gap: 12px; }
.mms-dispatch-page .qty-item { display: inline-flex; align-items: center; gap: 4px; }
.mms-dispatch-page .qty-label { font-size: 12px; color: var(--ink-400); font-weight: 500; }
.mms-dispatch-page .qty-val { font-size: 13px; font-weight: 700; color: var(--ink-700); font-variant-numeric: tabular-nums; }
.mms-dispatch-page .text-success { color: var(--green-700) !important; }
.mms-dispatch-page .text-danger { color: var(--red-700) !important; }
.mms-dispatch-page .text-in-progress { color: #6366f1 !important; }
/* Time range */
.mms-dispatch-page .wo-time-range { display: flex; align-items: center; gap: 5px; font-size: 12px; color: var(--ink-400); font-variant-numeric: tabular-nums; }
.mms-dispatch-page .wo-time-range .el-icon { font-size: 13px; }
/* Group actions */
.mms-dispatch-page .wo-group-actions { flex: 0 0 auto; }
/* Group body (inner table) */
.mms-dispatch-page .wo-group-body { border-top: 1px solid var(--ink-200); background: var(--ink-50); padding: 8px 12px 12px; }
.mms-dispatch-page .wo-group-body .inner-table { --el-table-bg-color: #fff; --el-table-header-bg-color: var(--ink-50); --el-table-row-hover-bg-color: #fafbff; --el-table-border-color: transparent; --el-table-text-color: var(--ink-700); --el-table-header-text-color: var(--ink-500); }
.mms-dispatch-page .wo-group-body .inner-table :deep(.el-table__header th) { border-right-color: transparent !important; background: var(--ink-50) !important; color: var(--ink-500); font-weight: 600; font-size: 13px; padding: 8px 12px; border-bottom: 1px solid var(--ink-200); }
.mms-dispatch-page .wo-group-body .inner-table :deep(.el-table__body td) { border-right-color: transparent !important; padding: 8px 12px; border-bottom: 1px solid var(--ink-100); color: var(--ink-700); font-size: 13px; }
.mms-dispatch-page .wo-group-body .inner-table :deep(.el-table__body tr:last-child td) { border-bottom: none; }
.mms-dispatch-page .wo-group-body .inner-table :deep(.el-table__inner-wrapper::before) { display: none; }
.mms-dispatch-page .wo-group-body .inner-table .badge { font-size: 12px; padding: 2px 7px; }
/* Pagination in grouped view */
.mms-dispatch-page .grouped-view + .pagination-container,
.mms-dispatch-page .grouped-view .pagination-container { display: flex; align-items: center; justify-content: flex-end; padding: 14px 20px; background: #fff; }
/* ===== Page Tabs Bar ===== */
.mms-dispatch-page .page-tabs-bar { display: flex; align-items: center; gap: 4px; padding: 0 0 10px 0; border-bottom: 2px solid var(--ink-200); margin-bottom: 10px; }
.mms-dispatch-page .page-tab { display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 16px; border: none; background: transparent; border-radius: var(--r-sm) var(--r-sm) 0 0; font-size: 15px; font-weight: 500; color: var(--ink-500); cursor: pointer; transition: all .15s var(--ease-out); white-space: nowrap; position: relative; }
.mms-dispatch-page .page-tab:hover { background: var(--ink-50); color: var(--ink-700); }
.mms-dispatch-page .page-tab.is-active { color: var(--brand-600); font-weight: 600; }
.mms-dispatch-page .page-tab.is-active::after { content: ''; position: absolute; bottom: -2px; left: 0; right: 0; height: 3px; background: var(--brand-600); border-radius: 3px 3px 0 0; }
.mms-dispatch-page .page-tab .el-icon { font-size: 16px; }
/* ===== Report Status Tab Reject ===== */
.mms-dispatch-page .status-tab.tab-reject .dot { background: var(--red-500); } .mms-dispatch-page .status-tab.tab-reject .count { background: var(--red-50); color: var(--red-700); } .mms-dispatch-page .status-tab.is-active.tab-reject .count { background: var(--red-500); color: #fff; }
/* ===== Material Picker (shared) ===== */
.mms-dispatch-page .material-picker { display: flex; flex-direction: column; gap: 8px; }
.mms-dispatch-page .material-picker-search { display: flex; align-items: center; gap: 4px; flex-wrap: wrap; padding-bottom: 4px; }
.mms-dispatch-page .material-picker-table { flex: 1; overflow: hidden; border: 1px solid var(--ink-200); border-radius: var(--r-sm); }
.mms-dispatch-page .material-picker-pager { display: flex; justify-content: flex-end; padding-top: 4px; }
/* ===== Report Info Banner ===== */
.mms-dispatch-page .report-info-banner { display: flex; align-items: flex-start; gap: 10px; margin: 0 20px; padding: 12px 16px; background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%); border: 1px solid #bfdbfe; border-left: 4px solid #3b82f6; border-radius: 8px; }
.mms-dispatch-page .report-info-banner .report-info-icon { font-size: 20px; color: #3b82f6; flex-shrink: 0; margin-top: 1px; }
.mms-dispatch-page .report-info-banner .report-info-text { flex: 1; min-width: 0; }
.mms-dispatch-page .report-info-banner .report-info-title { display: block; font-size: 14px; font-weight: 700; color: #1d4ed8; margin-bottom: 4px; }
.mms-dispatch-page .report-info-banner .report-info-desc { display: block; font-size: 13px; color: #475569; line-height: 1.6; }
.mms-dispatch-page .report-info-banner .report-info-desc .emph { font-weight: 600; color: #2563eb; }
</style>
