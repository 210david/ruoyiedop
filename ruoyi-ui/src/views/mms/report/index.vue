<template>
  <div class="app-container mms-report-page">
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
          <label>报工单号</label>
          <div class="control">
            <el-input v-model="queryParams.reportNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>工序</label>
          <div class="control"><el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>产能单元</label>
          <div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_report_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报工人</label>
          <div class="control"><el-input v-model="queryParams.reportBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>是否首件</label>
          <div class="control is-select"><el-select v-model="queryParams.isFirstPiece" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报工来源</label>
          <div class="control is-select"><el-select v-model="queryParams.source" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_report_source" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报工日期</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:report:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:report:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:report:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:report:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_report_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="报工单号" prop="reportNo" key="reportNo" :width="colWidth('reportNo', 168)" resizable v-if="columns.reportNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 168)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="工序" prop="processName" key="processName" :width="colWidth('processName', 120)" resizable v-if="columns.processName.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 144)" resizable v-if="columns.resourceName.visible" />
          <el-table-column label="合格数" prop="goodQty" key="goodQty" :width="colWidth('goodQty', 96)" resizable align="center" v-if="columns.goodQty.visible" />
          <el-table-column label="不良数" prop="defectQty" key="defectQty" :width="colWidth('defectQty', 96)" resizable align="center" v-if="columns.defectQty.visible">
            <template #default="scope"><span :style="{ color: scope.row.defectQty > 0 ? '#ef4444' : 'inherit', fontWeight: scope.row.defectQty > 0 ? 600 : 400 }">{{ scope.row.defectQty }}</span></template>
          </el-table-column>
          <el-table-column label="是否首件" prop="isFirstPiece" key="isFirstPiece" :width="colWidth('isFirstPiece', 108)" resizable align="center" v-if="columns.isFirstPiece.visible">
            <template #default="scope"><span v-if="scope.row.isFirstPiece" class="badge" :class="scope.row.isFirstPiece === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ isFirstPieceLabel(scope.row.isFirstPiece) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="报工人" prop="reportBy" key="reportBy" :width="colWidth('reportBy', 108)" resizable align="center" v-if="columns.reportBy.visible" />
          <el-table-column label="报工时间" prop="reportTime" key="reportTime" :width="colWidth('reportTime', 192)" resizable align="center" v-if="columns.reportTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.reportTime) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="来源" prop="source" key="source" :width="colWidth('source', 120)" resizable align="center" v-if="columns.source.visible">
            <template #default="scope"><span v-if="scope.row.source" class="badge" :class="sourceBadgeClass(scope.row.source)"><span class="dot"></span>{{ sourceLabel(scope.row.source) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:report:edit']">修改</el-button>
                <el-button v-if="scope.row.status === '0'" link type="success" icon="Check" @click="handleAudit(scope.row, '1')" v-hasPermi="['mms:report:audit']">审核</el-button>
                <el-button v-if="scope.row.status === '0'" link type="danger" icon="Close" @click="handleAudit(scope.row, '2')" v-hasPermi="['mms:report:audit']">驳回</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="报工单号" prop="reportNo"><el-input v-model="form.reportNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请选择工单" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openWoPicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="form.processName" placeholder="请选择工序" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openProcessPicker" :disabled="!form.workOrderId" /></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="form.resourceName" placeholder="请选择产能单元" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openResourcePicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="报工来源" prop="source"><el-select v-model="form.source" placeholder="手动报工" disabled style="width: 100%"><el-option v-for="d in mms_report_source" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>报工数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="合格数" prop="goodQty"><el-input-number v-model="form.goodQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="不良数" prop="defectQty"><el-input-number v-model="form.defectQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>班组信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="班组" prop="teamName"><el-input v-model="form.teamName" placeholder="请选择班组" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openTeamPicker" /></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="班次" prop="shiftName"><el-input v-model="form.shiftName" placeholder="请选择班次" readonly style="width: calc(100% - 32px)"><template #append><el-button icon="Search" @click="openShiftPicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-form-item label="是否首件" prop="isFirstPiece"><el-radio-group v-model="form.isFirstPiece"><el-radio value="1">是</el-radio><el-radio value="0">否</el-radio></el-radio-group></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">报工记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.reportNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.reportNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">报工单号</span><div class="rd-value">{{ viewData.reportNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ viewData.processName || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div><div class="rd-item"><span class="rd-label">来源</span><div class="rd-value"><span v-if="viewData.source" class="badge" :class="sourceBadgeClass(viewData.source)"><span class="dot"></span>{{ sourceLabel(viewData.source) }}</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>报工数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">合格数</span><div class="rd-value">{{ viewData.goodQty != null ? viewData.goodQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">不良数</span><div class="rd-value">{{ viewData.defectQty != null ? viewData.defectQty : '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>班组信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">班组</span><div class="rd-value">{{ viewData.teamName || '-' }}</div></div><div class="rd-item"><span class="rd-label">班次</span><div class="rd-value">{{ viewData.shiftName || '-' }}</div></div><div class="rd-item"><span class="rd-label">是否首件</span><div class="rd-value">{{ viewData.isFirstPiece === '1' ? '是' : '否' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 审核Dialog ===== -->
    <el-dialog :title="auditTitle" v-model="auditOpen" width="500px" append-to-body>
      <el-form ref="auditFormRef" :model="auditForm" label-width="100px">
        <el-form-item label="报工单号"><span>{{ auditForm.reportNo }}</span></el-form-item>
        <el-form-item label="审核备注" prop="auditRemark"><el-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审核备注" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitAudit">确 定</el-button><el-button @click="auditOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="报工管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、报工管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是报工管理？</div>
          <div class="highlight-card-body">
            <strong>报工管理（Work Report）</strong>是生产管控中记录工序生产进度、工时和质量的单据。报工记录工序完成数量、合格/不良数量、工时和操作人员，通过审核流程确保数据准确性，是生产进度追踪、产能分析和绩效考核的核心数据来源。<br/><br/>
            报工管理遵循 <strong>MES 生产进度采集规范</strong>，首件报工标记支持质量追溯，不良数监控支持质量问题预警，工时统计用于产能分析和绩效考核。
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
            <p>• <strong>报工审核：</strong>待审核状态的报工可执行审核通过或驳回操作</p>
            <p>• <strong>首件确认：</strong>首件报工需标记"是否首件"为"是"，以便质量追溯</p>
            <p>• <strong>不良数监控：</strong>不良数大于0时自动标红显示，便于关注质量问题</p>
            <p>• <strong>工时统计：</strong>报工工时将用于产能分析和绩效考核</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建报工：</strong>点击「新增」创建报工记录，填写工单、工序和完成数量</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>记录数据：</strong>填写合格/不良数量和操作人员，标记是否首件</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>审核确认：</strong>审核通过后报工数据生效，用于产能分析和绩效考核</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>

    <!-- ===== 工单选择器弹窗 ===== -->
    <el-dialog v-model="woPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">选择工单</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px" @keyup.enter="handleWoPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-input v-model="woPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleWoPickerQuery" />
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleWoPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetWoPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="woPickerLoading" :data="woPickerList" highlight-current-row @row-click="onWoRowClick" @row-dblclick="onWoRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="woPickerSelectedId" :value="row.workOrderId" @click.stop="onWoRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip />
            <el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip />
            <el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip />
            <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
            <el-table-column label="计划数量" prop="planQty" width="90" align="center" />
            <el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 工序选择器弹窗 ===== -->
    <el-dialog v-model="procPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">选择工序（来源：工单工艺快照）</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-table">
          <el-table v-loading="procPickerLoading" :data="processOptions" highlight-current-row @row-click="onProcRowClick" @row-dblclick="onProcRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="procPickerSelectedId" :value="row.processId" @click.stop="onProcRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="序号" prop="seq" width="60" align="center" />
            <el-table-column label="工序编码" prop="processCode" width="120" show-overflow-tooltip />
            <el-table-column label="工序名称" prop="processName" min-width="160" show-overflow-tooltip />
            <el-table-column label="绑定产能单元" prop="resourceName" min-width="140" show-overflow-tooltip />
            <el-table-column label="标准工时(分)" prop="standardHours" width="100" align="center" />
          </el-table>
        </div>
        <div v-if="processOptions.length === 0 && !procPickerLoading" style="text-align:center;padding:20px;color:#909399;font-size:13px">
          该工单暂无工艺快照数据，请确认工单已下达
        </div>
      </div>
      <template #footer><el-button @click="procPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleProcPickerConfirm" :disabled="!procPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 产能单元选择器弹窗 ===== -->
    <el-dialog v-model="resPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">选择产能单元</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="resPickerQuery.resourceName" placeholder="产能单元名称" clearable size="small" style="width: 200px" @keyup.enter="handleResPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleResPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetResPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="resPickerLoading" :data="resPickerList" highlight-current-row @row-click="onResRowClick" @row-dblclick="onResRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="resPickerSelectedId" :value="row.resourceId" @click.stop="onResRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="单元编码" prop="resourceCode" width="120" show-overflow-tooltip />
            <el-table-column label="单元名称" prop="resourceName" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" prop="resourceType" width="100" align="center"><template #default="scope"><dict-tag :options="mms_resource_type" :value="scope.row.resourceType" /></template></el-table-column>
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="resPickerQuery.pageNum" v-model:page-size="resPickerQuery.pageSize" :total="resPickerTotal" layout="total, prev, pager, next" small @current-change="getResPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="resPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleResPickerConfirm" :disabled="!resPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 班组选择器弹窗 ===== -->
    <el-dialog v-model="teamPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">选择班组</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="teamPickerQuery.teamName" placeholder="班组名称" clearable size="small" style="width: 200px" @keyup.enter="handleTeamPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleTeamPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetTeamPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="teamPickerLoading" :data="teamPickerList" highlight-current-row @row-click="onTeamRowClick" @row-dblclick="onTeamRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="teamPickerSelectedId" :value="row.teamId" @click.stop="onTeamRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="班组编码" prop="teamCode" width="120" show-overflow-tooltip />
            <el-table-column label="班组名称" prop="teamName" min-width="160" show-overflow-tooltip />
            <el-table-column label="负责人" prop="leader" width="100" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="teamPickerQuery.pageNum" v-model:page-size="teamPickerQuery.pageSize" :total="teamPickerTotal" layout="total, prev, pager, next" small @current-change="getTeamPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="teamPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleTeamPickerConfirm" :disabled="!teamPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 班次选择器弹窗 ===== -->
    <el-dialog v-model="shiftPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></div>
          <span class="rd-detail-header-title">选择班次</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="shiftPickerQuery.shiftName" placeholder="班次名称" clearable size="small" style="width: 200px" @keyup.enter="handleShiftPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleShiftPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetShiftPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="shiftPickerLoading" :data="shiftPickerList" highlight-current-row @row-click="onShiftRowClick" @row-dblclick="onShiftRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="shiftPickerSelectedId" :value="row.shiftId" @click.stop="onShiftRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="班次编码" prop="shiftCode" width="120" show-overflow-tooltip />
            <el-table-column label="班次名称" prop="shiftName" min-width="120" show-overflow-tooltip />
            <el-table-column label="开始时间" prop="startTime" width="100" align="center" />
            <el-table-column label="结束时间" prop="endTime" width="100" align="center" />
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="shiftPickerQuery.pageNum" v-model:page-size="shiftPickerQuery.pageSize" :total="shiftPickerTotal" layout="total, prev, pager, next" small @current-change="getShiftPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="shiftPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleShiftPickerConfirm" :disabled="!shiftPickerSelectedId">确 定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkReport">
import { listWorkReport, getWorkReport, addWorkReport, updateWorkReport, delWorkReport, auditWorkReport } from "@/api/mms/report";
import { listWorkOrder, listWorkOrderProcesses } from "@/api/mms/workorder";
import { listResource } from "@/api/mms/resource";
import { listTeam } from "@/api/mms/team";
import { listShift } from "@/api/mms/shift";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_report_status, mms_yes_no, mms_report_source, mms_workorder_status, mms_resource_type } = proxy.useDict("mms_report_status", "mms_yes_no", "mms_report_source", "mms_workorder_status", "mms_resource_type");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_report_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});
const showStatusHelp = ref(false);
const auditOpen = ref(false);
const auditTitle = ref("");

const statusTabList = computed(() => mms_report_status.value ? mms_report_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  reportNo: { label: '报工单号', visible: true }, workOrderNo: { label: '工单号', visible: true },
  processName: { label: '工序', visible: true }, resourceName: { label: '产能单元', visible: true },
  goodQty: { label: '合格数', visible: true }, defectQty: { label: '不良数', visible: true },
  isFirstPiece: { label: '是否首件', visible: true },
  reportBy: { label: '报工人', visible: true }, reportTime: { label: '报工时间', visible: true },
  status: { label: '状态', visible: true }, source: { label: '来源', visible: true }
}

function loadColumnVisibility() {
  try { const saved = localStorage.getItem('mms_report_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let c = 0; if (queryParams.value.reportNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.processName) c++; if (queryParams.value.resourceName) c++; if (queryParams.value.status) c++; if (queryParams.value.reportBy) c++; if (queryParams.value.isFirstPiece) c++; if (queryParams.value.source) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c;
});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, reportNo: undefined, workOrderNo: undefined, processName: undefined, resourceName: undefined, status: undefined, reportBy: undefined, isFirstPiece: undefined, source: undefined, params: {} },
  rules: {
    workOrderNo: [{ required: true, message: "请选择工单", trigger: "change" }],
    processName: [{ required: true, message: "请选择工序", trigger: "change" }],
    resourceName: [{ required: true, message: "请选择产能单元", trigger: "change" }],
    goodQty: [{ required: true, message: "请输入合格数", trigger: "blur" }]
  },
  auditForm: {}
});
const { queryParams, form, rules, auditForm } = toRefs(data);

function getList() {
  loading.value = true;
  listWorkReport(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts();
  });
}
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.reportNo) baseQuery.reportNo = queryParams.value.reportNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.processName) baseQuery.processName = queryParams.value.processName; if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName; if (queryParams.value.reportBy) baseQuery.reportBy = queryParams.value.reportBy; if (queryParams.value.isFirstPiece) baseQuery.isFirstPiece = queryParams.value.isFirstPiece; if (queryParams.value.source) baseQuery.source = queryParams.value.source; listWorkReport(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_report_status.value) { mms_report_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.reportNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.processName = undefined; queryParams.value.resourceName = undefined; queryParams.value.status = undefined; queryParams.value.reportBy = undefined; queryParams.value.isFirstPiece = undefined; queryParams.value.source = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.reportId); single.value = selection.length !== 1; multiple.value = !selection.length; }

function reset() {
  form.value = { reportNo: undefined, workOrderId: undefined, workOrderNo: undefined, processId: undefined, processName: undefined, resourceId: undefined, resourceName: undefined, goodQty: 0, defectQty: 0, teamId: undefined, teamName: undefined, shiftId: undefined, shiftName: undefined, isFirstPiece: "0", source: "1", remark: undefined };
  processOptions.value = [];
  proxy.resetForm("formRef");
}
function handleAdd() { reset(); open.value = true; title.value = "新增报工"; }
function handleUpdate(row) {
  reset();
  const id = row.reportId || ids.value[0];
  getWorkReport(id).then(response => {
    form.value = response.data;
    // 加载该工单的工序列表供选择
    if (form.value.workOrderId) {
      listWorkOrderProcesses(form.value.workOrderId).then(res => {
        processOptions.value = res.data || [];
        open.value = true; title.value = "修改报工";
      }).catch(() => { open.value = true; title.value = "修改报工"; });
    } else {
      open.value = true; title.value = "修改报工";
    }
  });
}
function handleView(row) {
  const id = row.reportId || ids.value[0];
  getWorkReport(id).then(response => { viewData.value = response.data; viewOpen.value = true; });
}
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.reportId != null) {
        updateWorkReport(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addWorkReport(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}
function cancel() { open.value = false; reset(); }
function handleDelete(row) {
  const delIds = row.reportId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的报工记录？').then(() => delWorkReport(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleExport() { proxy.download("mms/report/export", { ...queryParams.value }, `report_${new Date().getTime()}.xlsx`); }

function handleAudit(row, status) {
  auditTitle.value = status === "1" ? "报工审核-通过" : "报工审核-驳回";
  auditForm.value = { reportId: row.reportId, reportNo: row.reportNo, status: status, auditRemark: "" };
  auditOpen.value = true;
}
function submitAudit() {
  auditWorkReport(auditForm.value.reportId, auditForm.value.status, auditForm.value.auditRemark).then(() => {
    auditOpen.value = false; getList(); proxy.$modal.msgSuccess("操作成功");
  });
}

function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_report_status, status); }
function isFirstPieceLabel(val) { return dictLabel(mms_yes_no, val); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green', '2': 'red' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-reject' }; return map[value] || ''; }
function sourceLabel(value) { return dictLabel(mms_report_source, value); }
function sourceBadgeClass(value) { const map = { '1': 'blue', '2': 'green' }; return map[value] || 'gray'; }

// ===== 工单选择器 =====
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null);
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined });

function openWoPicker() { woPickerOpen.value = true; woPickerSelectedId.value = form.value.workOrderId || null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; getWoPickerList(); }
function getWoPickerList() { woPickerLoading.value = true; listWorkOrder(woPickerQuery).then(res => { woPickerList.value = res.rows; woPickerTotal.value = res.total; woPickerLoading.value = false; }).catch(() => { woPickerLoading.value = false; }); }
function handleWoPickerQuery() { woPickerQuery.pageNum = 1; getWoPickerList(); }
function resetWoPickerQuery() { woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; handleWoPickerQuery(); }
function onWoRowClick(row) { woPickerSelectedId.value = row.workOrderId; woPickerSelectedRow.value = row; }
function onWoRowDblClick(row) { onWoRowClick(row); handleWoPickerConfirm(); }
function handleWoPickerConfirm() {
  if (!woPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工单'); return; }
  const row = woPickerSelectedRow.value;
  form.value.workOrderId = row.workOrderId;
  form.value.workOrderNo = row.workOrderNo;
  // 清空工序（因为换了工单）
  form.value.processId = undefined;
  form.value.processName = undefined;
  processOptions.value = [];
  woPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo');
  // 加载该工单的工艺快照工序列表
  listWorkOrderProcesses(row.workOrderId).then(res => {
    processOptions.value = res.data || [];
  }).catch(() => {});
}

// ===== 工序选择器 =====
const procPickerOpen = ref(false); const procPickerLoading = ref(false); const procPickerSelectedId = ref(null); const procPickerSelectedRow = ref(null);
const processOptions = ref([]);

function openProcessPicker() {
  if (!form.value.workOrderId) { proxy.$modal.msgWarning('请先选择工单'); return; }
  procPickerOpen.value = true;
  procPickerSelectedId.value = form.value.processId || null;
  procPickerSelectedRow.value = null;
  if (processOptions.value.length === 0) {
    procPickerLoading.value = true;
    listWorkOrderProcesses(form.value.workOrderId).then(res => {
      processOptions.value = res.data || [];
      procPickerLoading.value = false;
    }).catch(() => { procPickerLoading.value = false; });
  }
}
function onProcRowClick(row) { procPickerSelectedId.value = row.processId; procPickerSelectedRow.value = row; }
function onProcRowDblClick(row) { onProcRowClick(row); handleProcPickerConfirm(); }
function handleProcPickerConfirm() {
  if (!procPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工序'); return; }
  const row = procPickerSelectedRow.value;
  form.value.processId = row.processId;
  form.value.processName = row.processName;
  // 如果工序有绑定的产能单元，自动带出
  if (row.resourceId) {
    form.value.resourceId = row.resourceId;
    form.value.resourceName = row.resourceName;
  }
  procPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('processName');
}

// ===== 产能单元选择器 =====
const resPickerOpen = ref(false); const resPickerLoading = ref(false); const resPickerList = ref([]); const resPickerTotal = ref(0); const resPickerSelectedId = ref(null); const resPickerSelectedRow = ref(null);
const resPickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, status: '0' });

function openResourcePicker() {
  resPickerOpen.value = true;
  resPickerSelectedId.value = form.value.resourceId || null;
  resPickerSelectedRow.value = null;
  resPickerQuery.pageNum = 1;
  resPickerQuery.resourceName = undefined;
  getResPickerList();
}
function getResPickerList() {
  resPickerLoading.value = true;
  listResource(resPickerQuery).then(res => {
    resPickerList.value = res.rows;
    resPickerTotal.value = res.total;
    resPickerLoading.value = false;
  }).catch(() => { resPickerLoading.value = false; });
}
function handleResPickerQuery() { resPickerQuery.pageNum = 1; getResPickerList(); }
function resetResPickerQuery() { resPickerQuery.resourceName = undefined; handleResPickerQuery(); }
function onResRowClick(row) { resPickerSelectedId.value = row.resourceId; resPickerSelectedRow.value = row; }
function onResRowDblClick(row) { onResRowClick(row); handleResPickerConfirm(); }
function handleResPickerConfirm() {
  if (!resPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择产能单元'); return; }
  const row = resPickerSelectedRow.value;
  form.value.resourceId = row.resourceId;
  form.value.resourceName = row.resourceName;
  resPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('resourceName');
}

// ===== 班组选择器 =====
const teamPickerOpen = ref(false); const teamPickerLoading = ref(false); const teamPickerList = ref([]); const teamPickerTotal = ref(0); const teamPickerSelectedId = ref(null); const teamPickerSelectedRow = ref(null);
const teamPickerQuery = reactive({ pageNum: 1, pageSize: 10, teamName: undefined, status: '0' });

function openTeamPicker() {
  teamPickerOpen.value = true;
  teamPickerSelectedId.value = form.value.teamId || null;
  teamPickerSelectedRow.value = null;
  teamPickerQuery.pageNum = 1;
  teamPickerQuery.teamName = undefined;
  getTeamPickerList();
}
function getTeamPickerList() {
  teamPickerLoading.value = true;
  listTeam(teamPickerQuery).then(res => {
    teamPickerList.value = res.rows;
    teamPickerTotal.value = res.total;
    teamPickerLoading.value = false;
  }).catch(() => { teamPickerLoading.value = false; });
}
function handleTeamPickerQuery() { teamPickerQuery.pageNum = 1; getTeamPickerList(); }
function resetTeamPickerQuery() { teamPickerQuery.teamName = undefined; handleTeamPickerQuery(); }
function onTeamRowClick(row) { teamPickerSelectedId.value = row.teamId; teamPickerSelectedRow.value = row; }
function onTeamRowDblClick(row) { onTeamRowClick(row); handleTeamPickerConfirm(); }
function handleTeamPickerConfirm() {
  if (!teamPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择班组'); return; }
  const row = teamPickerSelectedRow.value;
  form.value.teamId = row.teamId;
  form.value.teamName = row.teamName;
  teamPickerOpen.value = false;
}

// ===== 班次选择器 =====
const shiftPickerOpen = ref(false); const shiftPickerLoading = ref(false); const shiftPickerList = ref([]); const shiftPickerTotal = ref(0); const shiftPickerSelectedId = ref(null); const shiftPickerSelectedRow = ref(null);
const shiftPickerQuery = reactive({ pageNum: 1, pageSize: 10, shiftName: undefined, status: '0' });

function openShiftPicker() {
  shiftPickerOpen.value = true;
  shiftPickerSelectedId.value = form.value.shiftId || null;
  shiftPickerSelectedRow.value = null;
  shiftPickerQuery.pageNum = 1;
  shiftPickerQuery.shiftName = undefined;
  getShiftPickerList();
}
function getShiftPickerList() {
  shiftPickerLoading.value = true;
  listShift(shiftPickerQuery).then(res => {
    shiftPickerList.value = res.rows;
    shiftPickerTotal.value = res.total;
    shiftPickerLoading.value = false;
  }).catch(() => { shiftPickerLoading.value = false; });
}
function handleShiftPickerQuery() { shiftPickerQuery.pageNum = 1; getShiftPickerList(); }
function resetShiftPickerQuery() { shiftPickerQuery.shiftName = undefined; handleShiftPickerQuery(); }
function onShiftRowClick(row) { shiftPickerSelectedId.value = row.shiftId; shiftPickerSelectedRow.value = row; }
function onShiftRowDblClick(row) { onShiftRowClick(row); handleShiftPickerConfirm(); }
function handleShiftPickerConfirm() {
  if (!shiftPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择班次'); return; }
  const row = shiftPickerSelectedRow.value;
  form.value.shiftId = row.shiftId;
  form.value.shiftName = row.shiftName;
  shiftPickerOpen.value = false;
}

getList();
</script>

<style scoped>
.mms-report-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-report-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-report-page .filter-card{padding:14px 20px 16px}
.mms-report-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-report-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-report-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-report-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-report-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-report-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-report-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-report-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-report-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-report-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-report-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-report-page .field{display:flex;flex-direction:column;gap:6px}
.mms-report-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-report-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-report-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-report-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-report-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-report-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-report-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-report-page .field .control :deep(.el-select){width:100%}
.mms-report-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-report-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-report-page .toolbar .left,.mms-report-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-report-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-report-page .table-wrap{overflow-x:auto}
.mms-report-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-report-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-report-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-report-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-report-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-report-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-report-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-report-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-report-page .badge.amber .dot{background:var(--amber-500)}
.mms-report-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-report-page .badge.blue .dot{background:var(--blue-500)}
.mms-report-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-report-page .badge.green .dot{background:var(--green-500)}
.mms-report-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-report-page .badge.red .dot{background:var(--red-500)}
.mms-report-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-report-page .badge.gray .dot{background:var(--ink-400)}
.mms-report-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-report-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-report-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-report-page .tabs-track::-webkit-scrollbar{display:none}
.mms-report-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-report-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-report-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-report-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-report-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-report-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-report-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-report-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-report-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-report-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-report-page .status-tab.tab-audit .dot{background:var(--blue-500)}.mms-report-page .status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)}.mms-report-page .status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
.mms-report-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-report-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-report-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-report-page .status-tab.tab-reject .dot{background:var(--red-500)}.mms-report-page .status-tab.tab-reject .count{background:var(--red-50);color:var(--red-700)}.mms-report-page .status-tab.is-active.tab-reject .count{background:var(--red-500);color:#fff}
.mms-report-page .status-tab.tab-void .dot{background:var(--ink-400)}.mms-report-page .status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)}.mms-report-page .status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
.mms-report-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-report-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-report-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
.material-picker{display:flex;flex-direction:column;gap:12px}
.material-picker-search{display:flex;align-items:center;gap:4px}
.material-picker-table{border:1px solid var(--ink-200);border-radius:var(--r-sm);overflow:hidden}
.material-picker-pager{display:flex;justify-content:flex-end}
@media(max-width:1100px){.mms-report-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-report-page .filter-card .filter-bar{grid-template-columns:1fr}}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>