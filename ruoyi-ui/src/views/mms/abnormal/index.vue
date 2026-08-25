<template>
  <div class="app-container mms-abnormal-page">
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
          <label>异常单号</label>
          <div class="control">
            <el-input v-model="queryParams.abnormalNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>异常类型</label>
          <div class="control is-select"><el-select v-model="queryParams.abnormalType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_abnormal_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field">
          <label>严重等级</label>
          <div class="control is-select"><el-select v-model="queryParams.severity" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_abnormal_severity" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_abnormal_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>产能单元</label>
          <div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>上报人</label>
          <div class="control"><el-input v-model="queryParams.reportBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>上报日期</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:abnormal:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:abnormal:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:abnormal:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:abnormal:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_abnormal_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="异常单号" prop="abnormalNo" key="abnormalNo" :width="colWidth('abnormalNo', 140)" resizable v-if="columns.abnormalNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 120)" resizable v-if="columns.resourceName.visible" />
          <el-table-column label="产品编号" prop="productCode" key="productCode" :width="colWidth('productCode', 120)" resizable show-overflow-tooltip v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 120)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template>
          </el-table-column>
          <el-table-column label="异常类型" prop="abnormalType" key="abnormalType" :width="colWidth('abnormalType', 100)" resizable align="center" v-if="columns.abnormalType.visible">
            <template #default="scope"><span v-if="scope.row.abnormalType" class="badge" :class="abnormalTypeBadgeClass(scope.row.abnormalType)"><span class="dot"></span>{{ dictLabel(mms_abnormal_type, scope.row.abnormalType) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="严重等级" prop="severity" key="severity" :width="colWidth('severity', 90)" resizable align="center" v-if="columns.severity.visible">
            <template #default="scope"><span v-if="scope.row.severity" class="badge" :class="severityBadgeClass(scope.row.severity)"><span class="dot"></span>{{ dictLabel(mms_abnormal_severity, scope.row.severity) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="描述" prop="description" key="description" :width="colWidth('description', 200)" resizable show-overflow-tooltip v-if="columns.description.visible" />
          <el-table-column label="上报人" prop="reportBy" key="reportBy" :width="colWidth('reportBy', 90)" resizable align="center" v-if="columns.reportBy.visible" />
          <el-table-column label="上报时间" prop="reportTime" key="reportTime" :width="colWidth('reportTime', 160)" resizable align="center" v-if="columns.reportTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.reportTime) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="260" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:abnormal:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="warning" icon="Bell" @click="handleRespond(scope.row)" v-hasPermi="['mms:abnormal:respond']">响应</el-button>
              <el-button v-if="scope.row.status === '1'" link type="success" icon="CircleCheck" @click="handleResolve(scope.row)" v-hasPermi="['mms:abnormal:resolve']">处理关闭</el-button>
              <el-button v-if="scope.row.status !== '2'" link type="info" icon="Link" @click="handleLinkDowntime(scope.row)" v-hasPermi="['mms:abnormal:linkDowntime']">联动停机</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="异常单号" prop="abnormalNo"><el-input v-model="form.abnormalNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单号" prop="workOrderNo" :rules="[{ required: true, message: '请选择工单', trigger: 'change' }]"><el-input v-model="form.workOrderNo" readonly placeholder="请选择工单" style="width: 100%" @click="openWorkOrderPicker"><template #append><el-button icon="Search" @click="openWorkOrderPicker" /></template><template #suffix><el-icon v-if="form.workOrderNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearWorkOrder"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产能单元" prop="resourceName" :rules="[{ required: true, message: '请选择产能单元', trigger: 'change' }]"><el-input v-model="form.resourceName" readonly placeholder="请选择产能单元" style="width: 100%" @click="openResourcePicker"><template #append><el-button icon="Search" @click="openResourcePicker" /></template><template #suffix><el-icon v-if="form.resourceName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearResource"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="上报人" prop="reportBy" :rules="[{ required: true, message: '请选择上报人', trigger: 'change' }]"><el-input v-model="form.reportBy" readonly placeholder="请选择上报人" style="width: 100%" @click="openReportByPicker"><template #append><el-button icon="Search" @click="openReportByPicker" /></template><template #suffix><el-icon v-if="form.reportBy" class="rd-form-tip" style="cursor:pointer" @click.stop="clearReportBy"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0a')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0a }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0a">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编号"><el-input v-model="form.productCode" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称"><el-input v-model="form.productName" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="规格型号"><el-input v-model="form.specModel" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="单位"><el-input :model-value="unitLabel(form.unit)" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>异常详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="异常类型" prop="abnormalType"><el-select v-model="form.abnormalType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_abnormal_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="严重等级" prop="severity"><el-select v-model="form.severity" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_abnormal_severity" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="异常描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入异常描述" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">异常记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.abnormalNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.abnormalNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">异常单号</span><div class="rd-value">{{ viewData.abnormalNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status === '0'" class="badge red"><span class="dot"></span>待响应</span><span v-else-if="viewData.status === '1'" class="badge amber"><span class="dot"></span>处理中</span><span v-else-if="viewData.status === '2'" class="badge green"><span class="dot"></span>已关闭</span><span v-else>-</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0a')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0a }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0a" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编号</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>异常详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">异常类型</span><div class="rd-value"><dict-tag :options="mms_abnormal_type" :value="viewData.abnormalType" /></div></div><div class="rd-item"><span class="rd-label">严重等级</span><div class="rd-value"><dict-tag :options="mms_abnormal_severity" :value="viewData.severity" /></div></div><div class="rd-item rd-item--full"><span class="rd-label">异常描述</span><div class="rd-value">{{ viewData.description || '-' }}</div></div><div class="rd-item"><span class="rd-label">上报人</span><div class="rd-value">{{ viewData.reportBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">上报时间</span><div class="rd-value">{{ viewData.reportTime ? parseTime(viewData.reportTime) : '-' }}</div></div></div></div>
        </section>
        <section class="rd-card" v-if="viewData.status !== '0'">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg></span>响应信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">响应人</span><div class="rd-value">{{ viewData.responseBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">响应时间</span><div class="rd-value">{{ viewData.responseTime ? parseTime(viewData.responseTime) : '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">响应措施</span><div class="rd-value">{{ viewData.responseMeasure || '-' }}</div></div><div class="rd-item"><span class="rd-label">影响范围</span><div class="rd-value">{{ viewData.impactScope || '-' }}</div></div><div class="rd-item"><span class="rd-label">预计恢复时间</span><div class="rd-value">{{ viewData.estimatedRestoreTime ? parseTime(viewData.estimatedRestoreTime) : '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">响应备注</span><div class="rd-value">{{ viewData.responseRemark || '-' }}</div></div></div></div>
        </section>
        <section class="rd-card" v-if="viewData.status === '2'">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>处理关闭信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">处理人</span><div class="rd-value">{{ viewData.handleBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">处理时间</span><div class="rd-value">{{ viewData.handleTime ? parseTime(viewData.handleTime) : '-' }}</div></div><div class="rd-item"><span class="rd-label">关闭时间</span><div class="rd-value">{{ viewData.closeTime ? parseTime(viewData.closeTime) : '-' }}</div></div><div class="rd-item"><span class="rd-label">处理方式</span><div class="rd-value"><dict-tag :options="mms_abnormal_handle_method" :value="viewData.handleMethod" /></div></div><div class="rd-item rd-item--full"><span class="rd-label">处理结果</span><div class="rd-value">{{ viewData.handleResult || '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">根本原因</span><div class="rd-value">{{ viewData.rootCause || '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">预防措施</span><div class="rd-value">{{ viewData.preventiveMeasure || '-' }}</div></div><div class="rd-item"><span class="rd-label">停机时长</span><div class="rd-value">{{ viewData.downtimeHours != null ? viewData.downtimeHours + ' 小时' : '-' }}</div></div><div class="rd-item"><span class="rd-label">是否需要追纠</span><div class="rd-value"><span v-if="viewData.needPursuit === '1'" class="badge red"><span class="dot"></span>是</span><span v-else class="badge gray"><span class="dot"></span>否</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div><div class="rd-item"><span class="rd-label">更新人</span><div class="rd-value">{{ viewData.updateBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">更新时间</span><div class="rd-value">{{ viewData.updateTime ? parseTime(viewData.updateTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 响应Dialog ===== -->
    <el-dialog v-model="respondOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg></div>
          <span class="rd-detail-header-title">异常响应</span>
        </div>
      </template>
      <el-form ref="respondFormRef" :model="respondForm" :rules="respondRules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">异常单号</span><div class="rd-value">{{ respondForm.abnormalNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ respondForm.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ respondForm.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ respondForm.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">异常类型</span><div class="rd-value"><dict-tag :options="mms_abnormal_type" :value="respondForm.abnormalType" /></div></div><div class="rd-item"><span class="rd-label">严重等级</span><div class="rd-value"><dict-tag :options="mms_abnormal_severity" :value="respondForm.severity" /></div></div><div class="rd-item"><span class="rd-label">上报人</span><div class="rd-value">{{ respondForm.reportBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">上报时间</span><div class="rd-value">{{ respondForm.reportTime ? parseTime(respondForm.reportTime) : '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">异常描述</span><div class="rd-value" style="white-space: pre-wrap; line-height: 1.6">{{ respondForm.description || '-' }}</div></div></div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>响应信息</div></div>
            <div class="rd-card-body" style="display:block">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="响应人" prop="responseBy">
                    <el-input v-model="respondForm.responseBy" readonly placeholder="请选择响应人" style="width: 100%" @click="openResponseByPicker">
                      <template #append><el-button icon="Search" @click="openResponseByPicker" /></template>
                      <template #suffix><el-icon v-if="respondForm.responseBy" class="rd-form-tip" style="cursor:pointer" @click.stop="respondForm.responseBy = ''"><CircleClose /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="响应时间" prop="responseTime">
                    <el-date-picker v-model="respondForm.responseTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择响应时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="响应措施" prop="responseMeasure">
                <el-input v-model="respondForm.responseMeasure" type="textarea" :rows="3" placeholder="请输入初步应对措施，如：停机检查、切换备用设备、通知维修等" />
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="影响范围" prop="impactScope">
                    <el-input v-model="respondForm.impactScope" placeholder="请输入影响范围，如：产线A停机、批次200件受影响" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="预计恢复时间" prop="estimatedRestoreTime">
                    <el-date-picker v-model="respondForm.estimatedRestoreTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择预计恢复时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="响应备注" prop="responseRemark">
                <el-input v-model="respondForm.responseRemark" type="textarea" :rows="2" placeholder="请输入其他需要说明的事项" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitRespond">确 定</el-button><el-button @click="respondOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 处理关闭Dialog ===== -->
    <el-dialog v-model="resolveOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <span class="rd-detail-header-title">异常处理关闭</span>
        </div>
      </template>
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">异常单号</span><div class="rd-value">{{ resolveForm.abnormalNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ resolveForm.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ resolveForm.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ resolveForm.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">异常类型</span><div class="rd-value"><dict-tag :options="mms_abnormal_type" :value="resolveForm.abnormalType" /></div></div><div class="rd-item"><span class="rd-label">严重等级</span><div class="rd-value"><dict-tag :options="mms_abnormal_severity" :value="resolveForm.severity" /></div></div><div class="rd-item rd-item--full"><span class="rd-label">异常描述</span><div class="rd-value" style="white-space: pre-wrap; line-height: 1.6">{{ resolveForm.description || '-' }}</div></div></div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg></span>响应信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">响应人</span><div class="rd-value">{{ resolveForm.responseBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">响应时间</span><div class="rd-value">{{ resolveForm.responseTime ? parseTime(resolveForm.responseTime) : '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">响应措施</span><div class="rd-value" style="white-space: pre-wrap; line-height: 1.6">{{ resolveForm.responseMeasure || '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">影响范围</span><div class="rd-value">{{ resolveForm.impactScope || '-' }}</div></div></div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>处理信息</div></div>
            <div class="rd-card-body" style="display:block">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="处理人" prop="handleBy">
                    <el-input v-model="resolveForm.handleBy" readonly placeholder="请选择处理人" style="width: 100%" @click="openHandleByPicker">
                      <template #append><el-button icon="Search" @click="openHandleByPicker" /></template>
                      <template #suffix><el-icon v-if="resolveForm.handleBy" class="rd-form-tip" style="cursor:pointer" @click.stop="resolveForm.handleBy = ''"><CircleClose /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="处理时间" prop="handleTime">
                    <el-date-picker v-model="resolveForm.handleTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择处理时间" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="处理结果" prop="handleResult">
                <el-input v-model="resolveForm.handleResult" type="textarea" :rows="3" placeholder="请输入处理结果描述" />
              </el-form-item>
              <el-form-item label="处理方式" prop="handleMethod">
                <el-select v-model="resolveForm.handleMethod" placeholder="请选择处理方式" style="width: 100%">
                  <el-option v-for="d in mms_abnormal_handle_method" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>根因分析与预防</div></div>
            <div class="rd-card-body" style="display:block">
              <el-form-item label="根本原因" prop="rootCause">
                <el-input v-model="resolveForm.rootCause" type="textarea" :rows="2" placeholder="请输入异常根本原因分析" />
              </el-form-item>
              <el-form-item label="预防措施" prop="preventiveMeasure">
                <el-input v-model="resolveForm.preventiveMeasure" type="textarea" :rows="2" placeholder="请输入后续预防措施建议" />
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="停机时长(小时)" prop="downtimeHours">
                    <el-input-number v-model="resolveForm.downtimeHours" :min="0" :max="9999" :precision="2" :step="0.5" placeholder="停机时长" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="是否需要追纠" prop="needPursuit">
                    <el-radio-group v-model="resolveForm.needPursuit">
                      <el-radio value="0">否</el-radio>
                      <el-radio value="1">是</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitResolve">确 定</el-button><el-button @click="resolveOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 工单选择器弹窗 ===== -->
    <el-dialog v-model="woPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">选择工单</span></div></template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px" @keyup.enter="handleWoPickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input>
          <el-input v-model="woPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleWoPickerQuery" />
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleWoPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetWoPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="woPickerLoading" :data="woPickerList" highlight-current-row @row-click="onWoRowClick" @row-dblclick="onWoRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="woPickerSelectedId" :value="row.workOrderId" @click.stop="onWoRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip />
            <el-table-column label="产品编号" prop="productCode" width="120" show-overflow-tooltip />
            <el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip />
            <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
            <el-table-column label="计划数量" prop="planQty" width="90" align="center" />
            <el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager"><el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" /></div>
      </div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 产能单元选择器弹窗 ===== -->
    <el-dialog v-model="resPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div><span class="rd-detail-header-title">选择产能单元</span></div></template>
      <div class="material-picker">
        <div class="material-picker-table">
          <el-table v-loading="resPickerLoading" :data="resPickerList" highlight-current-row @row-click="onResRowClick" @row-dblclick="onResRowDblClick" height="400" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="resPickerSelectedId" :value="row.resourceId" @click.stop="onResRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="序号" prop="stepSeq" width="60" align="center" />
            <el-table-column label="工序编码" prop="processCode" width="120" show-overflow-tooltip />
            <el-table-column label="工序名称" prop="processName" min-width="120" show-overflow-tooltip />
            <el-table-column label="产能单元" prop="resourceName" min-width="140" show-overflow-tooltip />
          </el-table>
        </div>
        <div v-if="!resPickerLoading && resPickerList.length === 0" style="text-align:center;padding:24px;color:var(--ink-500);font-size:14px">该工单的工序未关联产能单元</div>
      </div>
      <template #footer><el-button @click="resPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleResPickerConfirm" :disabled="!resPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 人员选择器 ===== -->
    <user-picker ref="reportByUserPickerRef" title="选择上报人" @confirm="onReportByUserPickerConfirm" />
    <user-picker ref="responseUserPickerRef" title="选择响应人" @confirm="onResponseUserPickerConfirm" />
    <user-picker ref="handleUserPickerRef" title="选择处理人" @confirm="onHandleUserPickerConfirm" />

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="生产异常管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、生产异常管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是生产异常管理？</div>
          <div class="highlight-card-body">
            <strong>生产异常管理（Abnormal Management）</strong>是生产管控中记录和处理生产过程中设备故障、物料短缺、质量异常等异常情况的单据。异常管理通过响应→处理→关闭的流程，确保异常情况得到及时处理和闭环跟踪，减少生产中断时间。<br/><br/>
            生产异常管理遵循 <strong>MES 异常管控规范</strong>，通过严重等级分类（紧急/重要/一般）确保关键异常优先处理，异常类型分类（设备/物料/质量/人员）支持根因分析和预防措施制定。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="danger">待响应</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">点击「响应」</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">处理中</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">点击「关闭」</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已关闭</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待响应">异常新建后的初始状态，等待响应处理。需及时响应以减少生产影响</el-descriptions-item>
          <el-descriptions-item label="处理中">异常已响应，正在处理中。处理完成后可执行关闭操作</el-descriptions-item>
          <el-descriptions-item label="已关闭">异常已处理关闭，不可再操作。关闭时需记录处理结果</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>异常响应：</strong>待响应状态的异常需及时响应，进入处理中状态</p>
            <p>• <strong>处理关闭：</strong>处理中的异常在处理完成后可执行关闭操作，<strong>同时自动关闭关联的停机记录</strong></p>
            <p>• <strong>联动停机：</strong>点击「联动停机」可自动创建一条停机记录，将异常单与停机记录关联，避免重复录入</p>
            <p>• <strong>严重等级：</strong>紧急、重要、一般三个等级，影响处理优先级</p>
            <p>• <strong>异常类型：</strong>设备异常、物料异常、质量异常、人员异常等</p>
            <p>• <strong>类型映射：</strong>设备异常→故障停机，物料异常→物料停机，质量/安全→其他停机</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="danger" :hollow="true"><strong>上报异常：</strong>点击「新增」创建异常记录，填写异常类型、严重等级和描述</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>联动停机：</strong>点击「联动停机」自动生成停机记录，建立异常↔停机关联（可选）</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>响应处理：</strong>点击「响应」记录响应措施，异常进入处理中状态</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>关闭异常：</strong>处理完成后点击「关闭」关闭异常，记录处理结果，<strong>同时自动恢复关联的停机记录</strong></el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Abnormal">
import { listAbnormal, getAbnormal, addAbnormal, updateAbnormal, delAbnormal, respondAbnormal, resolveAbnormal, linkDowntime } from "@/api/mms/abnormal";
import { listWorkOrder, listWorkOrderProcesses } from "@/api/mms/workorder";
import UserPicker from '@/components/UserPicker/index.vue'
import useUserStore from '@/store/modules/user'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_abnormal_type, mms_abnormal_severity, mms_abnormal_status, mms_workorder_status, mms_resource_type, wms_unit, mms_abnormal_handle_method } = proxy.useDict("mms_abnormal_type", "mms_abnormal_severity", "mms_abnormal_status", "mms_workorder_status", "mms_resource_type", "wms_unit", "mms_abnormal_handle_method");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_abnormal_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c0a","c1","c2","vc0","vc0a","vc1","vc2","vc3","vc4"])

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
const respondOpen = ref(false);
const resolveOpen = ref(false);

const statusTabList = computed(() => mms_abnormal_status.value ? mms_abnormal_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  abnormalNo: { label: '异常单号', visible: true }, workOrderNo: { label: '工单号', visible: true },
  resourceName: { label: '产能单元', visible: true },
  productCode: { label: '产品编号', visible: true }, productName: { label: '产品名称', visible: true },
  specModel: { label: '规格型号', visible: true }, unit: { label: '单位', visible: true },
  abnormalType: { label: '异常类型', visible: true },
  severity: { label: '严重等级', visible: true }, description: { label: '描述', visible: true },
  reportBy: { label: '上报人', visible: true }, reportTime: { label: '上报时间', visible: true },
  status: { label: '状态', visible: true }
}

function loadColumnVisibility() {
  try { const saved = localStorage.getItem('mms_abnormal_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let c = 0; if (queryParams.value.abnormalNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.abnormalType) c++; if (queryParams.value.severity) c++; if (queryParams.value.status) c++; if (queryParams.value.resourceName) c++; if (queryParams.value.reportBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c;
});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, abnormalNo: undefined, workOrderNo: undefined, abnormalType: undefined, severity: undefined, status: undefined, resourceName: undefined, reportBy: undefined, params: {} },
  rules: { abnormalType: [{ required: true, message: "请选择异常类型", trigger: "change" }], severity: [{ required: true, message: "请选择严重等级", trigger: "change" }], description: [{ required: true, message: "请输入异常描述", trigger: "blur" }] },
  respondForm: {}, resolveForm: {},
  respondRules: { responseBy: [{ required: true, message: '请选择响应人', trigger: 'change' }], responseTime: [{ required: true, message: '请选择响应时间', trigger: 'change' }], responseMeasure: [{ required: true, message: '请输入响应措施', trigger: 'blur' }] },
  resolveRules: { handleBy: [{ required: true, message: '请选择处理人', trigger: 'change' }], handleTime: [{ required: true, message: '请选择处理时间', trigger: 'change' }], handleResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }] }
});
const { queryParams, form, rules, respondForm, resolveForm, respondRules, resolveRules } = toRefs(data);

function getList() {
  loading.value = true;
  listAbnormal(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); });
}
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.abnormalNo) baseQuery.abnormalNo = queryParams.value.abnormalNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.abnormalType) baseQuery.abnormalType = queryParams.value.abnormalType; if (queryParams.value.severity) baseQuery.severity = queryParams.value.severity; if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName; if (queryParams.value.reportBy) baseQuery.reportBy = queryParams.value.reportBy; listAbnormal(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_abnormal_status.value) { mms_abnormal_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.abnormalNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.abnormalType = undefined; queryParams.value.severity = undefined; queryParams.value.status = undefined; queryParams.value.resourceName = undefined; queryParams.value.reportBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.abnormalId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { abnormalNo: undefined, workOrderId: undefined, workOrderNo: undefined, resourceId: undefined, resourceName: undefined, productCode: undefined, productName: undefined, specModel: undefined, unit: undefined, abnormalType: undefined, severity: undefined, description: undefined, remark: undefined, reportBy: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增异常"; const userStore = useUserStore(); form.value.reportBy = userStore.nickName || userStore.name; }
function handleUpdate(row) { reset(); const id = row.abnormalId || ids.value[0]; getAbnormal(id).then(response => { form.value = response.data; open.value = true; title.value = "修改异常"; }); }
function handleView(row) { const id = row.abnormalId || ids.value[0]; getAbnormal(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.abnormalId != null) { updateAbnormal(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addAbnormal(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.abnormalId || ids.value; proxy.$modal.confirm('是否确认删除选中的异常记录？').then(() => delAbnormal(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/abnormal/export", { ...queryParams.value }, `abnormal_${new Date().getTime()}.xlsx`); }
function handleRespond(row) { respondForm.value = { abnormalId: row.abnormalId, abnormalNo: row.abnormalNo, workOrderNo: row.workOrderNo, resourceName: row.resourceName, productCode: row.productCode, productName: row.productName, specModel: row.specModel, unit: row.unit, abnormalType: row.abnormalType, severity: row.severity, description: row.description, reportBy: row.reportBy, reportTime: row.reportTime, responseBy: '', responseTime: proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'), responseMeasure: '', impactScope: '', estimatedRestoreTime: null, responseRemark: '' }; respondOpen.value = true; }
function submitRespond() { if (!respondForm.value.responseBy) { proxy.$modal.msgError('请选择响应人'); return; } if (!respondForm.value.responseMeasure) { proxy.$modal.msgError('请输入响应措施'); return; } respondAbnormal(respondForm.value.abnormalId, { responseBy: respondForm.value.responseBy, responseTime: respondForm.value.responseTime, responseMeasure: respondForm.value.responseMeasure, impactScope: respondForm.value.impactScope, estimatedRestoreTime: respondForm.value.estimatedRestoreTime, responseRemark: respondForm.value.responseRemark }).then(() => { respondOpen.value = false; getList(); proxy.$modal.msgSuccess("响应成功"); }); }
function handleResolve(row) { resolveForm.value = { abnormalId: row.abnormalId, abnormalNo: row.abnormalNo, workOrderNo: row.workOrderNo, resourceName: row.resourceName, productCode: row.productCode, productName: row.productName, specModel: row.specModel, unit: row.unit, abnormalType: row.abnormalType, severity: row.severity, description: row.description, reportBy: row.reportBy, reportTime: row.reportTime, responseBy: row.responseBy, responseTime: row.responseTime, responseMeasure: row.responseMeasure, impactScope: row.impactScope, handleResult: '', handleBy: row.responseBy || '', handleTime: proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}'), handleMethod: '', rootCause: '', preventiveMeasure: '', downtimeHours: null, needPursuit: '0' }; resolveOpen.value = true; }
function submitResolve() { if (!resolveForm.value.handleBy) { proxy.$modal.msgError('请选择处理人'); return; } if (!resolveForm.value.handleResult) { proxy.$modal.msgError('请输入处理结果'); return; } resolveAbnormal(resolveForm.value.abnormalId, { handleResult: resolveForm.value.handleResult, handleBy: resolveForm.value.handleBy, handleTime: resolveForm.value.handleTime, handleMethod: resolveForm.value.handleMethod, rootCause: resolveForm.value.rootCause, preventiveMeasure: resolveForm.value.preventiveMeasure, downtimeHours: resolveForm.value.downtimeHours, needPursuit: resolveForm.value.needPursuit }).then(() => { resolveOpen.value = false; getList(); proxy.$modal.msgSuccess("处理关闭成功，关联停机记录已自动恢复"); }); }
function handleLinkDowntime(row) { proxy.$modal.confirm('确认为异常[' + row.abnormalNo + ']联动生成停机记录？将自动创建一条停机中状态的停机单。').then(() => linkDowntime(row.abnormalId)).then(res => { proxy.$modal.msgSuccess("停机记录已联动生成，单号：" + (res.data ? res.data : '')); getList(); }).catch(() => {}); }

function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_abnormal_status, status); }
function unitLabel(unit) { if (!unit) return ''; const item = wms_unit.value ? wms_unit.value.find(d => d.value == unit) : null; return item ? item.label : unit; }
function badgeClass(status) { const map = { '0': 'red', '1': 'amber', '2': 'green' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-reject', '1': 'tab-draft', '2': 'tab-done' }; return map[value] || ''; }
function abnormalTypeBadgeClass(type) { const map = { '0': 'red', '1': 'amber', '2': 'blue', '3': 'gray' }; return map[type] || 'gray'; }
function severityBadgeClass(severity) { const map = { '0': 'red', '1': 'amber', '2': 'blue' }; return map[severity] || 'gray'; }

// ===== 工单选择器 =====
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null);
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined });
function openWorkOrderPicker() { woPickerOpen.value = true; woPickerSelectedId.value = null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; getWoPickerList(); }
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
  form.value.productCode = row.productCode;
  form.value.productName = row.productName;
  form.value.specModel = row.specModel;
  form.value.unit = row.unit;
  // 工单可能有多个工序对应不同产能单元，不自动带出，由用户手动选择
  form.value.resourceId = undefined;
  form.value.resourceName = undefined;
  woPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo');
}
function clearWorkOrder() { form.value.workOrderId = undefined; form.value.workOrderNo = undefined; form.value.productCode = undefined; form.value.productName = undefined; form.value.specModel = undefined; form.value.unit = undefined; form.value.resourceId = undefined; form.value.resourceName = undefined; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo'); proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('resourceName'); }

// ===== 产能单元选择器（根据工单工序过滤） =====
const resPickerOpen = ref(false); const resPickerLoading = ref(false); const resPickerList = ref([]); const resPickerSelectedId = ref(null); const resPickerSelectedRow = ref(null);
function openResourcePicker() {
  if (!form.value.workOrderId) { proxy.$modal.msgWarning('请先选择工单'); return; }
  resPickerOpen.value = true;
  resPickerSelectedId.value = form.value.resourceId || null;
  resPickerSelectedRow.value = null;
  getResPickerList();
}
function getResPickerList() {
  resPickerLoading.value = true;
  listWorkOrderProcesses(form.value.workOrderId).then(res => {
    // 从工序快照中提取有产能单元的工序
    resPickerList.value = (res.data || []).filter(p => p.resourceId);
    resPickerLoading.value = false;
  }).catch(() => { resPickerLoading.value = false; });
}
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
function clearResource() { form.value.resourceId = undefined; form.value.resourceName = undefined; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('resourceName'); }

// ===== 上报人选择器 =====
function openReportByPicker() { proxy.$refs.reportByUserPickerRef.open(); }
function onReportByUserPickerConfirm(user) { form.value.reportBy = user.nickName; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('reportBy'); }
function clearReportBy() { form.value.reportBy = undefined; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('reportBy'); }

// ===== 响应人选择器 =====
function openResponseByPicker() { proxy.$refs.responseUserPickerRef.open(); }
function onResponseUserPickerConfirm(user) { respondForm.value.responseBy = user.nickName; }

// ===== 处理人选择器 =====
function openHandleByPicker() { proxy.$refs.handleUserPickerRef.open(); }
function onHandleUserPickerConfirm(user) { resolveForm.value.handleBy = user.nickName; }

getList();
</script>

<style scoped>
.mms-abnormal-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-abnormal-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-abnormal-page .filter-card{padding:14px 20px 16px}
.mms-abnormal-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-abnormal-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-abnormal-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-abnormal-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-abnormal-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-abnormal-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-abnormal-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-abnormal-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-abnormal-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-abnormal-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-abnormal-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-abnormal-page .field{display:flex;flex-direction:column;gap:6px}
.mms-abnormal-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-abnormal-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-abnormal-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-abnormal-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-abnormal-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-abnormal-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-abnormal-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-abnormal-page .field .control :deep(.el-select){width:100%}
.mms-abnormal-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-abnormal-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-abnormal-page .toolbar .left,.mms-abnormal-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-abnormal-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-abnormal-page .table-wrap{overflow-x:auto}
.mms-abnormal-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-abnormal-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-abnormal-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-abnormal-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-abnormal-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-abnormal-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-abnormal-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-abnormal-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-abnormal-page .badge.amber .dot{background:var(--amber-500)}
.mms-abnormal-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-abnormal-page .badge.blue .dot{background:var(--blue-500)}
.mms-abnormal-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-abnormal-page .badge.green .dot{background:var(--green-500)}
.mms-abnormal-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-abnormal-page .badge.red .dot{background:var(--red-500)}
.mms-abnormal-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-abnormal-page .badge.gray .dot{background:var(--ink-400)}
.mms-abnormal-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-abnormal-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-abnormal-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-abnormal-page .tabs-track::-webkit-scrollbar{display:none}
.mms-abnormal-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-abnormal-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-abnormal-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-abnormal-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-abnormal-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-abnormal-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-abnormal-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-abnormal-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-abnormal-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-abnormal-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-abnormal-page .status-tab.tab-audit .dot{background:var(--blue-500)}.mms-abnormal-page .status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)}.mms-abnormal-page .status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
.mms-abnormal-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-abnormal-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-abnormal-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-abnormal-page .status-tab.tab-reject .dot{background:var(--red-500)}.mms-abnormal-page .status-tab.tab-reject .count{background:var(--red-50);color:var(--red-700)}.mms-abnormal-page .status-tab.is-active.tab-reject .count{background:var(--red-500);color:#fff}
.mms-abnormal-page .status-tab.tab-void .dot{background:var(--ink-400)}.mms-abnormal-page .status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)}.mms-abnormal-page .status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
.mms-abnormal-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-abnormal-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-abnormal-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
@media(max-width:1100px){.mms-abnormal-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-abnormal-page .filter-card .filter-bar{grid-template-columns:1fr}}
</style>