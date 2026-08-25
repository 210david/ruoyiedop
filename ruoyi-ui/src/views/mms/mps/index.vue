<template>
  <div class="app-container mms-mps-page">
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
          <label>计划编号</label>
          <div class="control">
            <el-input v-model="queryParams.mpsNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品编码</label>
          <div class="control">
            <el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>产品名称</label>
          <div class="control">
            <el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_mps_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>优先级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_priority" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>关联需求号</label>
          <div class="control">
            <el-input v-model="queryParams.demandNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:mps:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:mps:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:mps:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:mps:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_mps_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="计划编号" prop="mpsNo" key="mpsNo" :width="colWidth('mpsNo', 140)" resizable v-if="columns.mpsNo.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 180)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="计划开始" prop="periodStart" key="periodStart" :width="colWidth('periodStart', 120)" resizable align="center" v-if="columns.periodStart.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.periodStart, '{y}-{m}-{d}') }}</span></template>
          </el-table-column>
          <el-table-column label="计划结束" prop="periodEnd" key="periodEnd" :width="colWidth('periodEnd', 120)" resizable align="center" v-if="columns.periodEnd.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.periodEnd, '{y}-{m}-{d}') }}</span></template>
          </el-table-column>
          <el-table-column label="优先级" prop="priority" key="priority" :width="colWidth('priority', 90)" resizable align="center" v-if="columns.priority.visible">
            <template #default="scope"><span v-if="scope.row.priority" class="badge" :class="priorityBadgeClass(scope.row.priority)"><span class="dot"></span>{{ priorityLabel(scope.row.priority) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:mps:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="primary" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['mms:mps:confirm']">提交审批</el-button>
              <el-button v-if="scope.row.status === '1'" link type="primary" icon="Aim" @click="handleAudit(scope.row)" v-hasPermi="['mms:mps:approve']">审批</el-button>
              <el-button v-if="scope.row.status === '2'" link type="success" icon="Document" @click="handleRelease(scope.row)" v-hasPermi="['mms:mps:release']">生成工单</el-button>
              <el-button v-if="scope.row.status !== '3' && scope.row.status !== '4'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:mps:cancel']">取消</el-button>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划编号" prop="mpsNo"><el-input v-model="form.mpsNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="关联需求号" prop="demandNo"><el-input v-model="form.demandNo" readonly placeholder="请选择需求" style="width: 100%" @click="openDemandPicker"><template #append><el-button icon="Search" @click="openDemandPicker" /></template><template #suffix><el-icon v-if="form.demandNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearDemand"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="优先级" prop="priority"><el-select v-model="form.priority" placeholder="请选择" style="width: 100%"><el-option v-for="dict in mms_priority" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.productCode" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称"><el-input v-model="form.productName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="规格型号"><el-input v-model="form.specModel" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col><el-col :span="12"><el-form-item label="单位"><el-input v-model="unitLabel" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>计划参数</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划产量" prop="planQty"><el-input-number v-model="form.planQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="计划期开始" prop="periodStart"><el-date-picker v-model="form.periodStart" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="计划期结束" prop="periodEnd"><el-date-picker v-model="form.periodEnd" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col></el-row>
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
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">主生产计划详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.mpsNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.mpsNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">计划编号</span><div class="rd-value">{{ viewData.mpsNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">关联需求号</span><div class="rd-value">{{ viewData.demandNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="mms_priority" :value="viewData.priority" /></div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_mps_status" :value="viewData.status" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>计划参数</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">计划产量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">计划期开始</span><div class="rd-value">{{ viewData.periodStart ? parseTime(viewData.periodStart, '{y}-{m}-{d}') : '-' }}</div></div><div class="rd-item"><span class="rd-label">计划期结束</span><div class="rd-value">{{ viewData.periodEnd ? parseTime(viewData.periodEnd, '{y}-{m}-{d}') : '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div></div></div>
        </section>
        <!-- 审核记录 -->
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 需求选择器弹窗 ===== -->
    <el-dialog v-model="demandPickerOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">选择生产需求</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="demandPickerQuery.demandNo" placeholder="需求编号" clearable size="small" style="width:180px" @keyup.enter="handleDemandPickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="demandPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width:180px" @keyup.enter="handleDemandPickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleDemandPickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetDemandPickerQuery">重置</el-button>
      </div>
      <el-table ref="demandPickerTableRef" v-loading="demandPickerLoading" :data="demandPickerList" highlight-current-row @row-click="onDemandRowClick" @row-dblclick="onDemandRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="demandPickerSelectedId" :value="row.demandId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="需求编号" prop="demandNo" width="140" show-overflow-tooltip />
        <el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip />
        <el-table-column label="产品名称" prop="productName" min-width="140" show-overflow-tooltip />
        <el-table-column label="需求数量" prop="demandQty" width="90" align="center" />
        <el-table-column label="需求日期" prop="requireDate" width="110" align="center" />
        <el-table-column label="优先级" prop="priority" width="80" align="center"><template #default="scope"><dict-tag :options="mms_priority" :value="scope.row.priority" /></template></el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="demandPickerQuery.pageNum" v-model:page-size="demandPickerQuery.pageSize" :total="demandPickerTotal" layout="total, prev, pager, next" small @current-change="getDemandPickerList" />
      </div>
      <div v-if="demandPickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出需求信息</div>
      <template #footer><el-button @click="demandPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleDemandPickerConfirm" :disabled="!demandPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 审批 Dialog ===== -->
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">主生产计划审批</span>
          <div class="rd-detail-header-sub" v-if="auditData.mpsNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ auditData.mpsNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ac0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ac0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.ac0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">计划编号</span><div class="rd-value">{{ auditData.mpsNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">关联需求号</span><div class="rd-value">{{ auditData.demandNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="mms_priority" :value="auditData.priority" /></div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_mps_status" :value="auditData.status" /></div></div></div></div>
        </section>
        <!-- 产品信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ac3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ac3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.ac3" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ auditData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ auditData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ auditData.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="auditData.unit" /></div></div></div></div>
        </section>
        <!-- 计划参数 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ac4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>计划参数</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ac4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.ac4" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">计划产量</span><div class="rd-value">{{ auditData.planQty != null ? auditData.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">计划期开始</span><div class="rd-value">{{ auditData.periodStart ? parseTime(auditData.periodStart, '{y}-{m}-{d}') : '-' }}</div></div><div class="rd-item"><span class="rd-label">计划期结束</span><div class="rd-value">{{ auditData.periodEnd ? parseTime(auditData.periodEnd, '{y}-{m}-{d}') : '-' }}</div></div></div></div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ac5')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ac5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.ac5" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div></div></div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('ac1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ac1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.ac1" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in auditData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('ac2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ac2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.ac2">
            <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审批意见" prop="auditOpinion">
                <el-input v-model="auditForm.auditOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitAudit(true)">通 过</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 取消 Dialog ===== -->
    <el-dialog title="计划取消" v-model="cancelOpen" width="500px" append-to-body>
      <el-form ref="cancelFormRef" :model="cancelForm" label-width="100px">
        <el-form-item label="计划编号"><span>{{ cancelForm.mpsNo }}</span></el-form-item>
        <el-form-item label="取消原因" prop="cancelReason">
          <el-input v-model="cancelForm.cancelReason" type="textarea" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitCancel">确 定</el-button>
          <el-button @click="cancelOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 物料选择器（仅半成品和成品） ===== -->
    <material-picker ref="materialPickerRef" title="选择产品物料" :material-types="['1','2']" @confirm="onMaterialPickerConfirm" />

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="主生产计划业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、主生产计划释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是主生产计划？</div>
          <div class="highlight-card-body">
            <strong>主生产计划（MPS，Master Production Schedule）</strong>是生产管控中连接销售需求与生产执行的核心计划单据。MPS将生产需求按时间段分解为具体的生产计划，明确各产品的计划生产数量、计划开工和完工时间，审批通过后下达生成生产工单。<br/><br/>
            MPS遵循 <strong>ERP/MES 标准计划体系</strong>，向上对接生产需求和销售订单，向下驱动工单排产，通过审批流程确保计划的准确性和可执行性。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="info">草稿</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">提交审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="primary">待审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">审批通过</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">已审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">生成工单</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已下达</el-tag></div>
        </div>
        <div class="status-flow" style="margin-top:8px">
          <div class="flow-item"><el-tag type="info">草稿/待审批/已审批</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">取消</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="danger">已取消</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">计划新建后的初始状态，可修改计划内容、提交审批或取消</el-descriptions-item>
          <el-descriptions-item label="待审批">计划已提交审批，等待审批通过或驳回</el-descriptions-item>
          <el-descriptions-item label="已审批">计划已审批通过，可下达生成工单</el-descriptions-item>
          <el-descriptions-item label="已下达">计划已下达，系统自动生成对应生产工单</el-descriptions-item>
          <el-descriptions-item label="已取消">计划已取消，不再执行</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>提交审批：</strong>草稿状态的计划提交后进入待审批状态，同时校验产品和计划数量等关键字段</p>
            <p>• <strong>计划审批：</strong>待审批的计划可通过审批进入已审批状态，或驳回回到草稿</p>
            <p>• <strong>计划下达：</strong>已审批的计划下达后自动生成生产工单，进入已下达状态</p>
            <p>• <strong>计划取消：</strong>非已下达、非已取消的计划可取消，取消后不可恢复</p>
            <p>• <strong>优先级：</strong>支持高、中、低三个优先级，影响生产排程顺序</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>创建计划：</strong>点击「新增」创建主生产计划，填写产品、计划数量和计划时间</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>提交审批：</strong>草稿状态下点击「提交审批」，计划进入待审批状态</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>审批下达：</strong>审批通过后点击「生成工单」，系统自动生成生产工单</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>取消计划：</strong>不再执行的计划可取消，取消后不可恢复</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Mps">
import { listMps, getMps, addMps, updateMps, delMps, submitMps, auditMps, releaseMps, cancelMps } from "@/api/mms/mps";
import { listDemand } from "@/api/mms/demand";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_mps_status, mms_priority, wms_unit } = proxy.useDict("mms_mps_status", "mms_priority", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_mps_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3","vc4","ac0","ac1","ac2","ac3","ac4","ac5"])

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const loading = ref(true);
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
const auditData = ref({});
const cancelOpen = ref(false);

// 单位显示文本（字典值 → 中文标签）
const unitLabel = computed(() => {
  if (!form.value.unit) return '';
  const item = wms_unit.value.find(d => d.value == form.value.unit);
  return item ? item.label : form.value.unit;
});

const statusTabList = computed(() => {
  if (!mms_mps_status.value) return [];
  return mms_mps_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  mpsNo: { label: '计划编号', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  planQty: { label: '计划数量', visible: true },
  periodStart: { label: '计划开始', visible: true },
  periodEnd: { label: '计划结束', visible: true },
  priority: { label: '优先级', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_mps_columns')
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
  if (queryParams.value.mpsNo) count++;
  if (queryParams.value.productCode) count++;
  if (queryParams.value.productName) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.priority) count++;
  if (queryParams.value.demandNo) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    mpsNo: undefined,
    productCode: undefined,
    productName: undefined,
    status: undefined,
    priority: undefined,
    demandNo: undefined,
    params: {}
  },
  rules: {
    productCode: [{ required: true, message: "请选择产品编码", trigger: "change" }],
    priority: [{ required: true, message: "请选择优先级", trigger: "change" }],
    planQty: [{ required: true, message: "请输入计划产量", trigger: "blur" }],
    periodStart: [{ required: true, message: "请选择计划期开始", trigger: "change" }],
    periodEnd: [{ required: true, message: "请选择计划期结束", trigger: "change" }]
  },
  auditForm: {},
  auditRules: {
    auditOpinion: [{ required: true, message: "请输入审批意见", trigger: "blur" }]
  },
  cancelForm: {}
});

const { queryParams, form, rules, auditForm, auditRules, cancelForm } = toRefs(data);

function getList() {
  loading.value = true;
  listMps(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    loadStatusCounts();
  });
}

function loadStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.mpsNo) baseQuery.mpsNo = queryParams.value.mpsNo;
  if (queryParams.value.productCode) baseQuery.productCode = queryParams.value.productCode;
  if (queryParams.value.productName) baseQuery.productName = queryParams.value.productName;
  if (queryParams.value.priority) baseQuery.priority = queryParams.value.priority;
  if (queryParams.value.demandNo) baseQuery.demandNo = queryParams.value.demandNo;
  listMps(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_mps_status.value) {
      mms_mps_status.value.forEach(d => { counts[d.value] = 0; });
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
  queryParams.value.mpsNo = undefined;
  queryParams.value.productCode = undefined;
  queryParams.value.productName = undefined;
  queryParams.value.status = undefined;
  queryParams.value.priority = undefined;
  queryParams.value.demandNo = undefined;
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

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.mpsId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    mpsNo: undefined, demandId: undefined, demandNo: undefined, productId: undefined, productCode: undefined, productName: undefined,
    specModel: undefined, unit: undefined, planQty: undefined,
    periodStart: undefined, periodEnd: undefined, priority: undefined, remark: undefined
  };
  proxy.resetForm("formRef");
}

function handleAdd() { reset(); open.value = true; title.value = "新增计划"; }
function handleUpdate(row) {
  reset();
  const id = row.mpsId || ids.value[0];
  getMps(id).then(response => { form.value = response.data; open.value = true; title.value = "修改计划"; });
}
function handleView(row) {
  const id = row.mpsId || ids.value[0];
  getMps(id).then(response => { viewData.value = response.data; viewOpen.value = true; });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.mpsId != null) {
        updateMps(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); });
      } else {
        addMps(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); });
      }
    }
  });
}
function cancel() { open.value = false; reset(); }
function handleDelete(row) {
  const delIds = row.mpsId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的计划？').then(() => delMps(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {});
}
function handleExport() { proxy.download("mms/mps/export", { ...queryParams.value }, `mps_${new Date().getTime()}.xlsx`); }

// 业务操作
function handleSubmit(row) {
  proxy.$modal.confirm('是否提交计划"' + row.mpsNo + '"进行审批？').then(() => submitMps(row.mpsId)).then(() => { getList(); proxy.$modal.msgSuccess("提交成功"); }).catch(() => {});
}
function handleAudit(row) {
  const id = row.mpsId;
  getMps(id).then(response => {
    auditData.value = response.data;
    auditForm.value = { mpsId: id, auditOpinion: "" };
    auditOpen.value = true;
  });
}
function submitAudit(passed) {
  proxy.$refs["auditFormRef"].validate(valid => {
    if (valid) {
      const status = passed ? '2' : '0';
      const actionText = passed ? '通过' : '驳回';
      proxy.$modal.confirm(`确认${actionText}该主生产计划？`).then(() => {
        return auditMps(auditForm.value.mpsId, status, auditForm.value.auditOpinion);
      }).then(() => {
        proxy.$modal.msgSuccess("审批成功");
        auditOpen.value = false;
        getList();
      }).catch(() => {});
    }
  });
}
function handleRelease(row) {
  proxy.$modal.confirm('是否确认下达计划"' + row.mpsNo + '"？将自动生成生产工单。').then(() => releaseMps(row.mpsId)).then(() => { getList(); proxy.$modal.msgSuccess("下达成功，工单已生成"); }).catch(() => {});
}
function handleCancel(row) {
  cancelForm.value = { mpsId: row.mpsId, mpsNo: row.mpsNo, cancelReason: "" };
  cancelOpen.value = true;
}
function submitCancel() {
  cancelMps(cancelForm.value.mpsId, cancelForm.value.cancelReason).then(() => {
    cancelOpen.value = false; getList(); proxy.$modal.msgSuccess("取消成功");
  });
}

// ===== 需求选择器 =====
const demandPickerOpen = ref(false);
const demandPickerLoading = ref(false);
const demandPickerList = ref([]);
const demandPickerTotal = ref(0);
const demandPickerSelectedId = ref(null);
const demandPickerSelectedRow = ref(null);
const demandPickerTableRef = ref();
const demandPickerQuery = reactive({ pageNum: 1, pageSize: 10, demandNo: undefined, productName: undefined, status: '1' });

function openDemandPicker() {
  demandPickerOpen.value = true;
  demandPickerSelectedId.value = null;
  demandPickerSelectedRow.value = null;
  demandPickerQuery.pageNum = 1;
  demandPickerQuery.demandNo = undefined;
  demandPickerQuery.productName = undefined;
  getDemandPickerList();
}
function getDemandPickerList() {
  demandPickerLoading.value = true;
  listDemand(demandPickerQuery).then(res => {
    demandPickerList.value = res.rows;
    demandPickerTotal.value = res.total;
    demandPickerLoading.value = false;
  }).catch(() => { demandPickerLoading.value = false; });
}
function handleDemandPickerQuery() { demandPickerQuery.pageNum = 1; getDemandPickerList(); }
function resetDemandPickerQuery() { demandPickerQuery.demandNo = undefined; demandPickerQuery.productName = undefined; handleDemandPickerQuery(); }
function onDemandRowClick(row) { demandPickerSelectedId.value = row.demandId; demandPickerSelectedRow.value = row; }
function onDemandRowDblClick(row) { onDemandRowClick(row); handleDemandPickerConfirm(); }
function handleDemandPickerConfirm() {
  if (!demandPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择需求'); return; }
  const row = demandPickerSelectedRow.value;
  form.value.demandId = row.demandId;
  form.value.demandNo = row.demandNo;
  // 自动带出产品信息
  form.value.productId = row.productId;
  form.value.productCode = row.productCode;
  form.value.productName = row.productName;
  form.value.specModel = row.specModel;
  form.value.unit = row.unit;
  // 自动带出计划数量
  if (row.demandQty != null) form.value.planQty = Number(row.demandQty);
  // 自动带出优先级
  if (row.priority) form.value.priority = row.priority;
  // 自动带出计划期（以需求日期为完工日期）
  if (row.requireDate) {
    form.value.periodEnd = row.requireDate;
    const d = new Date(row.requireDate);
    d.setDate(d.getDate() - 7);
    form.value.periodStart = d.toISOString().split('T')[0];
  }
  demandPickerOpen.value = false;
}
function clearDemand() {
  form.value.demandId = undefined;
  form.value.demandNo = undefined;
}

// ===== 物料选择器 =====
const materialPickerRef = ref()
function openMaterialPicker() { materialPickerRef.value.open(form.value.productId) }
function onMaterialPickerConfirm(material) {
  form.value.productId = material.materialId
  form.value.productCode = material.materialCode
  form.value.productName = material.materialName
  form.value.specModel = material.specModel
  if (material.unit) form.value.unit = material.unit
}
function clearMaterial() {
  form.value.productId = undefined
  form.value.productCode = undefined
  form.value.productName = undefined
  form.value.specModel = undefined
  form.value.unit = undefined
}

// ===== 字典辅助函数 =====
function statusLabel(status) {
  const item = mms_mps_status.value.find(d => d.value == status);
  return item ? item.label : '—';
}

function badgeClass(status) {
  const map = {
    '0': 'gray',     // 草稿
    '1': 'amber',    // 待审批
    '2': 'blue',     // 已审批
    '3': 'green',    // 已下达
    '4': 'gray'      // 已取消
  };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = {
    '0': 'tab-void',
    '1': 'tab-draft',
    '2': 'tab-audit',
    '3': 'tab-done',
    '4': 'tab-void'
  };
  return map[value] || '';
}

function priorityLabel(priority) {
  const item = mms_priority.value.find(d => d.value == priority);
  return item ? item.label : '—';
}

function priorityBadgeClass(priority) {
  const map = { '0': 'red', '1': 'amber', '2': 'blue' };
  return map[priority] || 'gray';
}

getList();
</script>

<style scoped>
.mms-mps-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-mps-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-mps-page .filter-card { padding:14px 20px 16px; }
.mms-mps-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-mps-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-mps-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-mps-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-mps-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-mps-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-mps-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-mps-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-mps-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-mps-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-mps-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-mps-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-mps-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-mps-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-mps-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-mps-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-mps-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-mps-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-mps-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-mps-page .field .control :deep(.el-select) { width:100%; }
.mms-mps-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-mps-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-mps-page .toolbar .left, .mms-mps-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-mps-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-mps-page .table-wrap { overflow-x:auto; }
.mms-mps-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-mps-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-mps-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-mps-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-mps-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-mps-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-mps-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-mps-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-mps-page .badge.amber .dot { background:var(--amber-500); }
.mms-mps-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-mps-page .badge.blue .dot { background:var(--blue-500); }
.mms-mps-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-mps-page .badge.green .dot { background:var(--green-500); }
.mms-mps-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-mps-page .badge.red .dot { background:var(--red-500); }
.mms-mps-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-mps-page .badge.gray .dot { background:var(--ink-400); }
.mms-mps-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-mps-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-mps-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-mps-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-mps-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-mps-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-mps-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-mps-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-mps-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-mps-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-mps-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-mps-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-mps-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-mps-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-mps-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-mps-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-mps-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-mps-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-mps-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-mps-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-mps-page .status-tab.tab-reject .dot { background:var(--red-500); } .mms-mps-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); } .mms-mps-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.mms-mps-page .status-tab.tab-void .dot { background:var(--ink-400); } .mms-mps-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .mms-mps-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.mms-mps-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-mps-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-mps-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
.status-help-content .highlight-card { background-color:#ecf5ff; border-radius:8px; padding:16px; border-left:4px solid #409eff; }
.status-help-content .highlight-card p { margin:6px 0; line-height:1.6; font-size:13px; color:#606266; }
@media (max-width:1100px) { .mms-mps-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-mps-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>