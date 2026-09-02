<template>
  <div class="app-container qms-complaint-page">
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
          <label>客诉编号</label>
          <div class="control">
            <el-input v-model="queryParams.complaintNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客户</label>
          <div class="control">
            <el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.complaintType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.complaintStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs + Tip Pill -->
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
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:complaint:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:complaint:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:complaint:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:complaint:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_complaint_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="客诉编号" prop="complaintNo" key="complaintNo" :width="colWidth('complaintNo', 160)" resizable show-overflow-tooltip v-if="columns.complaintNo.visible" />          <el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip v-if="columns.customerName.visible" />
          <el-table-column label="物料" prop="materialName" key="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="投诉日期" prop="complaintDate" key="complaintDate" :width="colWidth('complaintDate', 120)" resizable align="center" v-if="columns.complaintDate.visible"><template #default="scope"><span>{{ parseTime(scope.row.complaintDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="类型" prop="complaintType" key="complaintType" :width="colWidth('complaintType', 100)" resizable align="center" v-if="columns.complaintType.visible"><template #default="scope"><span v-if="dictLabel(typeOptions, scope.row.complaintType)" class="badge violet">{{ dictLabel(typeOptions, scope.row.complaintType) }}</span></template></el-table-column>
          <el-table-column label="严重程度" prop="severity" key="severity" :width="colWidth('severity', 100)" resizable align="center" v-if="columns.severity.visible"><template #default="scope"><span v-if="dictLabel(severityOptions, scope.row.severity)" class="badge" :class="severityClass(scope.row.severity)"><span class="dot"></span>{{ dictLabel(severityOptions, scope.row.severity) }}</span></template></el-table-column>
          <el-table-column label="退货金额" prop="returnAmt" key="returnAmt" :width="colWidth('returnAmt', 120)" resizable align="right" v-if="columns.returnAmt.visible"><template #default="scope"><span class="rd-amount">{{ scope.row.returnAmt != null ? '￥' + formatAmount(scope.row.returnAmt) : '-' }}</span></template></el-table-column>
          <el-table-column label="索赔金额" prop="claimAmt" key="claimAmt" :width="colWidth('claimAmt', 120)" resizable align="right" v-if="columns.claimAmt.visible"><template #default="scope"><span class="rd-amount">{{ scope.row.claimAmt != null ? '￥' + formatAmount(scope.row.claimAmt) : '-' }}</span></template></el-table-column>
          <el-table-column label="状态" prop="complaintStatus" key="complaintStatus" :width="colWidth('complaintStatus', 100)" resizable align="center" v-if="columns.complaintStatus.visible"><template #default="scope"><span v-if="dictLabel(statusOptions, scope.row.complaintStatus)" class="badge" :class="statusClass(scope.row.complaintStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, scope.row.complaintStatus) }}</span></template></el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:complaint:edit']" v-if="scope.row.complaintStatus === '0'">修改</el-button>
                <el-button link type="warning" icon="Promotion" @click="handleAccept(scope.row)" v-hasPermi="['qms:complaint:edit']" v-if="scope.row.complaintStatus === '0'">受理</el-button>
                <el-button link type="success" icon="Tools" @click="handleProcess(scope.row)" v-hasPermi="['qms:complaint:edit']" v-if="scope.row.complaintStatus === '1'">处理</el-button>
                <el-button link type="info" icon="CircleClose" @click="handleClose(scope.row)" v-hasPermi="['qms:complaint:close']" v-if="scope.row.complaintStatus === '2'">关闭</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:complaint:remove']" v-if="scope.row.complaintStatus === '0'">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">客诉详情</div>
            <div class="rd-detail-header-sub" v-if="viewData?.complaintNo">
              <span class="rd-detail-header-no">编号：{{ viewData.complaintNo }}</span>
              <span v-if="dictLabel(statusOptions, viewData?.complaintStatus)" class="badge badge-on-dark" :class="statusClass(viewData?.complaintStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, viewData?.complaintStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>客诉信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">客诉编号</span><div class="rd-value">{{ viewData.complaintNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">投诉日期</span><div class="rd-value">{{ viewData.complaintDate ? parseTime(viewData.complaintDate, '{y}-{m}-{d}') : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">投诉类型</span><div class="rd-value"><span v-if="dictLabel(typeOptions, viewData.complaintType)" class="badge violet">{{ dictLabel(typeOptions, viewData.complaintType) }}</span><span v-else>-</span></div></div>
            <div class="rd-item"><span class="rd-label">严重程度</span><div class="rd-value"><span v-if="dictLabel(severityOptions, viewData.severity)" class="badge" :class="severityClass(viewData.severity)"><span class="dot"></span>{{ dictLabel(severityOptions, viewData.severity) }}</span><span v-else>-</span></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>客户信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewData.customerName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">客户编号</span><div class="rd-value">{{ viewData.customerCode || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.returnQty != null || viewData.returnAmt != null || viewData.claimAmt != null">
          <div class="rd-card-header" @click="toggleCard('vc3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>退货/索赔信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">退货数量</span><div class="rd-value">{{ viewData.returnQty != null ? viewData.returnQty : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">退货金额</span><div class="rd-value rd-amount">{{ viewData.returnAmt != null ? '￥' + formatAmount(viewData.returnAmt) : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">索赔金额</span><div class="rd-value rd-amount">{{ viewData.claimAmt != null ? '￥' + formatAmount(viewData.claimAmt) : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>处理信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ viewData.defectDesc || '-' }}</div></div>
            <div class="rd-item rd-item--full" v-if="viewData.handleDesc"><span class="rd-label">处理描述</span><div class="rd-value">{{ viewData.handleDesc }}</div></div>
            <div class="rd-item" v-if="viewData.handleResult"><span class="rd-label">处理结果</span><div class="rd-value"><span class="badge amber">{{ dictLabel(handleResultOptions, viewData.handleResult) }}</span></div></div>
            <div class="rd-item" v-if="viewData.closeTime"><span class="rd-label">关闭时间</span><div class="rd-value">{{ parseTime(viewData.closeTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc5')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc5 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc5"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Add/Edit Dialog (只填写客诉信息，不包含处理信息) -->
    <el-dialog v-model="open" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">客诉编号保存后自动生成</span></div>
          </div>
        </div>
      </template>
      <el-form ref="complaintRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>客诉信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="客诉编号"><el-input v-model="form.complaintNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="投诉日期" prop="complaintDate"><el-date-picker v-model="form.complaintDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="投诉类型" prop="complaintType"><el-select v-model="form.complaintType" placeholder="请选择" style="width: 100%"><el-option v-for="d in typeOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="严重程度" prop="severity"><el-select v-model="form.severity" placeholder="请选择" style="width: 100%"><el-option v-for="d in severityOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>客户信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" readonly placeholder="请选择客户" style="width: 100%" @click="openCustomerPicker"><template #append><el-button icon="Search" @click="openCustomerPicker" /></template><template #suffix><el-icon v-if="form.customerName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearCustomer"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="客户编号"><el-input v-model="form.customerCode" readonly placeholder="选择客户后自动带出" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.materialCode" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物料名称"><el-input v-model="form.materialName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入批次号" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>缺陷描述</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec3 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec3">
              <el-form-item label="缺陷描述" prop="defectDesc"><el-input v-model="form.defectDesc" type="textarea" :rows="3" placeholder="请描述客诉缺陷详情" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec4')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec4 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec4">
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- Process Dialog (处理对话框 - 填写处理信息) -->
    <el-dialog v-model="processOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">客诉处理</div>
            <div class="rd-detail-header-sub" v-if="processForm.complaintNo">
              <span class="rd-detail-header-no">编号：{{ processForm.complaintNo }}</span>
              <span class="rd-detail-header-no">{{ processForm.customerName }}</span>
            </div>
          </div>
        </div>
      </template>
      <el-form ref="processRef" :model="processForm" :rules="processRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>客诉概要</div></div>
            <div class="rd-card-body"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">客诉编号</span><div class="rd-value">{{ processForm.complaintNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">投诉日期</span><div class="rd-value">{{ processForm.complaintDate ? parseTime(processForm.complaintDate, '{y}-{m}-{d}') : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ processForm.customerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ processForm.materialName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ processForm.defectDesc || '-' }}</div></div>
            </div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>退货/索赔信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="退货数量" prop="returnQty"><el-input-number v-model="processForm.returnQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="退货金额" prop="returnAmt"><el-input-number v-model="processForm.returnAmt" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="索赔金额" prop="claimAmt"><el-input-number v-model="processForm.claimAmt" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>处理信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="处理描述" prop="handleDesc"><el-input v-model="processForm.handleDesc" type="textarea" :rows="3" placeholder="8D回复内容" /></el-form-item>
              <el-form-item label="处理结果" prop="handleResult"><el-select v-model="processForm.handleResult" placeholder="请选择" style="width: 100%"><el-option v-for="d in handleResultOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
              <el-form-item label="备注"><el-input v-model="processForm.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitProcess">提交处理</el-button><el-button @click="processOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 客户选择器 -->
    <customer-picker ref="customerPickerRef" title="选择客户" @confirm="onCustomerPickerConfirm" />
    <!-- 物料选择器 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="客诉台账业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、客诉释义 -->
        <h4>一、客诉释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是客诉管理？</div>
          <div class="highlight-card-body">
            <strong>客诉管理（Customer Complaint Management）</strong>是质量管理中用于受理、调查和解决客户对产品质量、交期及服务方面投诉的闭环管理流程。当客户提出投诉后，企业需对投诉进行<strong>登记受理 → 调查处理 → 确认关闭</strong>的全链路闭环管理，确保客户诉求得到及时响应和有效解决。<br/><br/>
            客诉管理是衡量企业质量服务水平和客户满意度的重要指标，满足ISO 9001、IATF 16949等质量管理体系对客户反馈管理的要求，同时为CAPA（纠正与预防措施）提供输入来源。
          </div>
        </div>

        <!-- 二、客诉状态流转图 -->
        <h4>二、客诉状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">已登记</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">处理中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待确认</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已关闭</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="已登记">客诉创建后的初始状态。可修改、删除。确认投诉信息无误后点击「受理」进入处理中</el-descriptions-item>
          <el-descriptions-item label="处理中">投诉调查处理中。可填写退货/索赔信息、处理描述及处理结果。处理完成后点击「处理」提交处理结果，进入待确认</el-descriptions-item>
          <el-descriptions-item label="待确认">处理结果已提交，等待客户确认是否满意。客户确认后点击「关闭」完成客诉闭环</el-descriptions-item>
          <el-descriptions-item label="已关闭">客诉流程完成，记录归档用于质量追溯和分析</el-descriptions-item>
        </el-descriptions>

        <!-- 四、业务操作流程 -->
        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>登记客诉：</strong>收到客户投诉后，点击「新增」创建客诉记录，填写投诉日期、投诉类型、严重程度、客户信息、物料信息及缺陷描述
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>受理客诉：</strong>确认客诉信息无误后，在已登记状态下点击「受理」按钮，客诉进入处理中状态，开始调查处理
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>处理客诉：</strong>在处理中状态下点击「处理」按钮，填写退货数量、退货金额、索赔金额、处理描述（8D回复内容）及处理结果，提交后进入待确认
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>关闭客诉：</strong>在待确认状态下，客户确认处理结果满意后，点击「关闭」按钮完成客诉闭环，记录归档
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>删除客诉：</strong>仅在已登记状态下可删除客诉记录，删除后不可恢复
          </el-timeline-item>
        </el-timeline>

        <!-- 五、字段填写指南 -->
        <h4>五、字段填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">客诉信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>客诉编号：</strong>客诉的唯一标识编号，保存后由系统自动生成，无需手动输入</p>
            <p>• <strong>投诉日期：</strong>客户提出投诉的日期。<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>投诉类型：</strong>选择投诉的类型：质量投诉（产品质量问题）、交期投诉（交货延期）、服务投诉（服务态度或响应问题）<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>严重程度：</strong>投诉的严重程度分级：<strong>严重</strong>（影响产品核心功能或安全）、<strong>一般</strong>（影响部分功能或体验）、<strong>轻微</strong>（不影响主要使用）<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">客户与物料信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>客户名称：</strong>投诉客户的企业名称，点击搜索按钮从客户档案中选择<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>客户编号：</strong>客户编号在选择客户后自动带出</p>
            <p>• <strong>物料编码：</strong>投诉涉及的物料编码，点击搜索按钮从物料主数据中选择<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>物料名称：</strong>物料名称在选择物料后自动带出</p>
            <p>• <strong>批次号：</strong>投诉物料的生产批次号<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">处理信息区（处理时填写）</div>
          <div class="highlight-card-body">
            <p>• <strong>退货数量：</strong>因客诉产生的退货数量<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>退货金额：</strong>退货产生的退款金额<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>索赔金额：</strong>客户索赔的金额<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>处理描述：</strong>调查处理过程及8D回复内容，记录根因分析、临时措施、永久措施等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>处理结果：</strong>处理结果分类：退货退款、换货处理、折扣补偿、质量改善、其他处理<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsComplaint">
import { listComplaint, getComplaint, addComplaint, updateComplaint, delComplaint, closeComplaint, acceptComplaint, completeComplaint } from '@/api/qms/complaint'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount } from '@/composables/useDetailCard'
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import { CircleClose } from '@element-plus/icons-vue'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_complaint_index')
const { collapsedCards, toggleCard } = useDetailCard(['vc0','vc1','vc2','vc3','vc4','vc5','ec0','ec1','ec2','ec3','ec4'])
const { proxy } = getCurrentInstance()
const { qms_complaint_type: typeOptions, qms_complaint_status: statusOptions, qms_complaint_severity: severityOptions, qms_complaint_handle_result: handleResultOptions } = proxy.useDict('qms_complaint_type', 'qms_complaint_status', 'qms_complaint_severity', 'qms_complaint_handle_result')

const defaultColumns = {
  complaintNo: { label: '客诉编号', visible: true },
  customerName: { label: '客户名称', visible: true },
  materialName: { label: '物料', visible: true },
  complaintDate: { label: '投诉日期', visible: true },
  complaintType: { label: '类型', visible: true },
  severity: { label: '严重程度', visible: true },
  returnAmt: { label: '退货金额', visible: true },
  claimAmt: { label: '索赔金额', visible: true },
  complaintStatus: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('qms_complaint_columns')
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
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref(null)
const processOpen = ref(false)
const processForm = ref({})
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })
const statusTabList = computed(() => statusOptions.value)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const customerPickerRef = ref()
const materialPickerRef = ref()
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } })
const { queryParams, form } = toRefs(data)
const rules = {
  complaintDate: [{ required: true, message: '请选择投诉日期', trigger: 'change' }],
  complaintType: [{ required: true, message: '请选择投诉类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  customerName: [{ required: true, message: '请选择客户', trigger: 'change' }],
  materialCode: [{ required: true, message: '请选择物料', trigger: 'change' }],
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  defectDesc: [{ required: true, message: '请输入缺陷描述', trigger: 'blur' }]
}
const processRules = {
  handleDesc: [{ required: true, message: '请输入处理描述', trigger: 'blur' }],
  handleResult: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  returnQty: [{ required: true, message: '请输入退货数量', trigger: 'blur' }],
  returnAmt: [{ required: true, message: '请输入退货金额', trigger: 'blur' }],
  claimAmt: [{ required: true, message: '请输入索赔金额', trigger: 'blur' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.complaintNo) count++
  if (queryParams.value.customerName) count++
  if (queryParams.value.complaintType) count++
  if (queryParams.value.complaintStatus) count++
  return count
})

function getList() { loading.value = true; listComplaint(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.complaintNo = undefined; queryParams.value.customerName = undefined; queryParams.value.complaintType = undefined; queryParams.value.complaintStatus = undefined; activeStatusTab.value = 'all'; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增客诉' }
function handleUpdate(row) {
  const id = row?.complaintId || selectedId.value
  getComplaint(id).then(res => { data.form = res.data; open.value = true; title.value = '修改客诉' })
}
function handleView(row) { getComplaint(row.complaintId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleAccept(row) {
  proxy.$modal.confirm('确认受理客诉 "' + row.complaintNo + '"？受理后将进入处理中状态。').then(() => acceptComplaint(row.complaintId)).then(() => { getList(); proxy.$modal.msgSuccess('受理成功') }).catch(() => {})
}
function handleProcess(row) {
  getComplaint(row.complaintId).then(res => { processForm.value = res.data; processOpen.value = true })
}
function submitProcess() {
  proxy.$refs['processRef'].validate(valid => {
    if (valid) {
      completeComplaint(processForm.value).then(() => { proxy.$modal.msgSuccess('处理完成'); processOpen.value = false; getList() })
    }
  })
}
function openCustomerPicker() { customerPickerRef.value.open(form.value.customerId) }
function onCustomerPickerConfirm(customer) {
  form.value.customerId = customer.customerId
  form.value.customerCode = customer.customerNo
  form.value.customerName = customer.customerName
}
function clearCustomer() {
  form.value.customerId = undefined
  form.value.customerCode = undefined
  form.value.customerName = undefined
}
function openMaterialPicker() { materialPickerRef.value.open(form.value.materialId) }
function onMaterialPickerConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialCode = material.materialCode
  form.value.materialName = material.materialName
}
function clearMaterial() {
  form.value.materialId = undefined
  form.value.materialCode = undefined
  form.value.materialName = undefined
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.complaintId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['complaintRef'].validate(valid => {
    if (valid) {
      if (form.value.complaintId) { updateComplaint(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addComplaint(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.complaintId ? [row.complaintId] : selectedIds.value
  proxy.$modal.confirm('确认删除？').then(() => delComplaint(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleClose(row) {
  proxy.$modal.confirm('确认关闭客诉 "' + row.complaintNo + '"？').then(() => closeComplaint(row.complaintId)).then(() => { getList(); proxy.$modal.msgSuccess('关闭成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/complaint/export', { ...queryParams.value }, `complaint_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { complaintId: null, complaintNo: undefined, customerId: undefined, customerName: undefined, customerCode: undefined, materialId: undefined, materialName: undefined, materialCode: undefined, batchNo: undefined, complaintType: undefined, severity: undefined, complaintDate: new Date().toISOString().slice(0,10), defectDesc: undefined }; proxy.resetForm('complaintRef') }
function cancel() { open.value = false; reset() }
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function statusClass(val) {
  const map = { '0': 'blue', '1': 'amber', '2': 'violet', '3': 'green' }
  return map[val] || 'gray'
}
function severityClass(val) {
  const map = { '1': 'red', '2': 'amber', '3': 'gray' }
  return map[val] || 'gray'
}
function loadStatusCounts() {
  listComplaint({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.complaintStatus] !== undefined) counts[r.complaintStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.complaintStatus = status === 'all' ? undefined : status
  handleQuery()
}
function statusTabClass(value) {
  const map = { '0': 'tab-registered', '1': 'tab-processing', '2': 'tab-confirming', '3': 'tab-closed' }
  return map[value] || ''
}
getList()
</script>

<style scoped>
.qms-complaint-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-complaint-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-complaint-page .filter-card { padding:14px 20px 16px; }
.qms-complaint-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-complaint-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-complaint-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-complaint-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-complaint-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-complaint-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-complaint-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-complaint-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-complaint-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-complaint-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-complaint-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-complaint-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-complaint-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-complaint-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-complaint-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-complaint-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-complaint-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-complaint-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-complaint-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-complaint-page .field .control :deep(.el-select) { width:100%; }
.qms-complaint-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-complaint-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-complaint-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-complaint-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-complaint-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-complaint-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-complaint-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-complaint-page .btn-soft .el-icon { font-size:14px; }
.qms-complaint-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-complaint-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-complaint-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-complaint-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-complaint-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-complaint-page .table-wrap { overflow-x:auto; }
.qms-complaint-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-complaint-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-complaint-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-complaint-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-complaint-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-complaint-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-complaint-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-complaint-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-complaint-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-complaint-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考供应商质量评价列表） ===== */
.qms-complaint-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.qms-complaint-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
/* 固定颜色类（用于字典字段：类型=violet, 处理结果=amber） */
.qms-complaint-page .badge.violet { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-complaint-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-complaint-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-complaint-page .badge.blue .dot { background:var(--brand-500); }
.qms-complaint-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-complaint-page .badge.green .dot { background:var(--green-500); }
.qms-complaint-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-complaint-page .badge.red .dot { background:var(--red-500); }
.qms-complaint-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-complaint-page .badge.gray .dot { background:var(--ink-400); }
/* 深色背景上的 badge 变体（标题横幅中使用） */
.qms-complaint-page .badge.badge-on-dark { background:rgba(255,255,255,.15); border-color:rgba(255,255,255,.25); color:#fff; }
.qms-complaint-page .badge.badge-on-dark .dot { background:rgba(255,255,255,.7); }
.qms-complaint-page .badge.badge-on-dark.blue { background:rgba(99,102,241,.25); border-color:rgba(99,102,241,.4); }
.qms-complaint-page .badge.badge-on-dark.blue .dot { background:#818cf8; }
.qms-complaint-page .badge.badge-on-dark.amber { background:rgba(245,158,11,.25); border-color:rgba(245,158,11,.4); }
.qms-complaint-page .badge.badge-on-dark.amber .dot { background:#fbbf24; }
.qms-complaint-page .badge.badge-on-dark.violet { background:rgba(99,102,241,.25); border-color:rgba(99,102,241,.4); }
.qms-complaint-page .badge.badge-on-dark.violet .dot { background:#818cf8; }
.qms-complaint-page .badge.badge-on-dark.green { background:rgba(16,185,129,.25); border-color:rgba(16,185,129,.4); }
.qms-complaint-page .badge.badge-on-dark.green .dot { background:#34d399; }
.qms-complaint-page .badge.badge-on-dark.red { background:rgba(239,68,68,.25); border-color:rgba(239,68,68,.4); }
.qms-complaint-page .badge.badge-on-dark.red .dot { background:#f87171; }
.qms-complaint-page .badge.badge-on-dark.gray { background:rgba(255,255,255,.12); border-color:rgba(255,255,255,.2); }
.qms-complaint-page .badge.badge-on-dark.gray .dot { background:rgba(255,255,255,.5); }
/* ===== Status Tabs ===== */
.qms-complaint-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.qms-complaint-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.qms-complaint-page .tabs-track::-webkit-scrollbar { display:none; }
.qms-complaint-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.qms-complaint-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.qms-complaint-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.qms-complaint-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.qms-complaint-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.qms-complaint-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.qms-complaint-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 已登记 - blue */
.qms-complaint-page .status-tab.tab-registered .dot { background:var(--brand-500); }
.qms-complaint-page .status-tab.tab-registered .count { background:var(--brand-50); color:var(--brand-700); }
.qms-complaint-page .status-tab.is-active.tab-registered .count { background:var(--brand-500); color:#fff; }
/* 处理中 - amber */
.qms-complaint-page .status-tab.tab-processing .dot { background:#f59e0b; }
.qms-complaint-page .status-tab.tab-processing .count { background:#fffbeb; color:#b45309; }
.qms-complaint-page .status-tab.is-active.tab-processing .count { background:#f59e0b; color:#fff; }
/* 待确认 - violet */
.qms-complaint-page .status-tab.tab-confirming .dot { background:#8b5cf6; }
.qms-complaint-page .status-tab.tab-confirming .count { background:var(--brand-50); color:var(--brand-700); }
.qms-complaint-page .status-tab.is-active.tab-confirming .count { background:#8b5cf6; color:#fff; }
/* 已关闭 - green */
.qms-complaint-page .status-tab.tab-closed .dot { background:var(--green-500); }
.qms-complaint-page .status-tab.tab-closed .count { background:var(--green-50); color:var(--green-700); }
.qms-complaint-page .status-tab.is-active.tab-closed .count { background:var(--green-500); color:#fff; }
/* ===== Tip Pill ===== */
.qms-complaint-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-complaint-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
/* ===== 业务操作说明对话框 ===== */
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
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
