<template>
  <div class="app-container pms-contract-page">
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
          <label>合同编号</label>
          <div class="control">
            <el-input v-model="queryParams.contractNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>合同名称</label>
          <div class="control">
            <el-input v-model="queryParams.contractName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_contract_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>合同类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.contractType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_contract_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>签订人</label>
          <div class="control">
            <el-input v-model="queryParams.signBy" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>签订日期</label>
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
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span>
            <span>{{ s.label }}</span>
            <span class="count">{{ statusCounts[s.value] || 0 }}</span>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:contract:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:contract:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:contract:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:contract:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_contract_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="合同编号" prop="contractNo" key="contractNo" :width="colWidth('contractNo', 180)" resizable sortable="custom" v-if="columns.contractNo.visible" />
          <el-table-column label="合同名称" prop="contractName" key="contractName" :width="colWidth('contractName', 240)" resizable show-overflow-tooltip v-if="columns.contractName.visible" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="合同类型" prop="contractType" key="contractType" :width="colWidth('contractType', 120)" resizable align="center" v-if="columns.contractType.visible"><template #default="scope"><span class="badge violet">{{ contractTypeLabel(scope.row.contractType) }}</span></template></el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" sortable="custom" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="签订日期" prop="signDate" key="signDate" :width="colWidth('signDate', 130)" resizable align="center" v-if="columns.signDate.visible" />
          <el-table-column label="到期日期" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 130)" resizable align="center" v-if="columns.expireDate.visible" />
          <el-table-column label="合同金额" prop="contractAmount" key="contractAmount" :width="colWidth('contractAmount', 130)" resizable align="right" sortable="custom" v-if="columns.contractAmount.visible"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.contractAmount) }}</span></template></el-table-column>
          <el-table-column label="已付金额" prop="paidAmount" key="paidAmount" :width="colWidth('paidAmount', 130)" resizable align="right" v-if="columns.paidAmount.visible"><template #default="scope"><span class="rd-amount" :style="{ color: scope.row.paidAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(scope.row.paidAmount) }}</span></template></el-table-column>
          <el-table-column label="未付金额" prop="unpaidAmount" key="unpaidAmount" :width="colWidth('unpaidAmount', 130)" resizable align="right" v-if="columns.unpaidAmount.visible"><template #default="scope"><span class="rd-amount" :style="{ color: (scope.row.contractAmount - scope.row.paidAmount) > 0 ? '#f56c6c' : '#67c23a' }">{{ formatMoney((scope.row.contractAmount || 0) - (scope.row.paidAmount || 0)) }}</span></template></el-table-column>
          <el-table-column label="签订人" prop="signBy" key="signBy" :width="colWidth('signBy', 120)" resizable v-if="columns.signBy.visible" />
          <el-table-column label="变更次数" prop="changeCount" key="changeCount" :width="colWidth('changeCount', 90)" resizable align="center" v-if="columns.changeCount.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button v-if="scope.row.status === '0' || scope.row.status === '6'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:contract:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="warning" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['pms:contract:edit']">提交审批</el-button>
              <el-button v-if="scope.row.status === '5'" link type="warning" icon="DocumentChecked" @click="handleApprove(scope.row)" v-hasPermi="['pms:contract:audit']">审批</el-button>
              <el-button v-if="scope.row.status === '1'" link type="primary" icon="Switch" @click="handleChange(scope.row)" v-hasPermi="['pms:contract:change']">变更</el-button>
              <el-button v-if="scope.row.status === '1'" link type="danger" icon="CircleClose" @click="handleTerminate(scope.row)" v-hasPermi="['pms:contract:edit']">终止</el-button>
              <el-button v-if="scope.row.status === '2'" link type="warning" icon="DocumentChecked" @click="handleView(scope.row, true)" v-hasPermi="['pms:contract:audit']">审核</el-button>
              <el-button v-if="scope.row.status === '0' || scope.row.status === '6'" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:contract:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="1136px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">{{ title }}</span><div class="rd-detail-header-sub" v-if="form.contractNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ form.contractNo }}</span></div></div></template>
      <el-form ref="contractRef" :model="form" :rules="rules" label-width="130px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="合同编号" prop="contractNo"><el-input v-model="form.contractNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="合同名称" prop="contractName"><el-input v-model="form.contractName" placeholder="请输入" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="关联采购申请单号" prop="orderId"><el-select v-model="form.orderId" filterable clearable placeholder="请选择采购申请单号" style="width: 100%" @change="onRequestChange"><el-option v-for="r in requestOptions" :key="r.requestId" :label="r.requestNo" :value="r.requestId" /></el-select></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="合同类型" prop="contractType"><el-select v-model="form.contractType" placeholder="请选择" style="width: 100%"><el-option v-for="d in pms_contract_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('date')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期与金额</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.date }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.date">
            <el-row :gutter="20"><el-col :span="24"><el-form-item label="合同金额" prop="contractAmount"><el-input-number v-model="form.contractAmount" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="签订日期" prop="signDate"><el-date-picker v-model="form.signDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="到期日期" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('party')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>条款信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.party }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.party">
            <el-row :gutter="20"><el-col :span="24"><el-form-item label="付款条款" prop="paymentTerms"><el-input v-model="form.paymentTerms" type="textarea" :rows="3" placeholder="请输入付款条款" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="24"><el-form-item label="交货条款" prop="deliveryTerms"><el-input v-model="form.deliveryTerms" type="textarea" :rows="3" placeholder="请输入交货条款" /></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('attach')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.attach }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.attach">
            <el-form-item label="合同附件" prop="fileUrl"><file-upload v-model="form.fileUrl" :limit="1" :fileSize="50" :fileType="['png','jpg','jpeg','gif','bmp','webp','pdf','doc','docx']" /></el-form-item>
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="1136px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">合同详情</span><div class="rd-detail-header-sub" v-if="viewData.contractNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.contractNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewData.contractNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ viewData.contractName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">关联采购申请单号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag :options="pms_contract_type" :value="viewData.contractType" /></div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_contract_status" :value="viewData.status" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期与金额</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">合同金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.contractAmount) }}</div></div>
            <div class="rd-item"><span class="rd-label">已付金额</span><div class="rd-value rd-amount" :style="{ color: viewData.paidAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(viewData.paidAmount) }}</div></div>
            <div class="rd-item"><span class="rd-label">未付金额</span><div class="rd-value rd-amount" :style="{ color: (viewData.contractAmount - viewData.paidAmount) > 0 ? '#f56c6c' : '#67c23a' }">{{ formatMoney((viewData.contractAmount || 0) - (viewData.paidAmount || 0)) }}</div></div>
            <div class="rd-item"><span class="rd-label">签订日期</span><div class="rd-value">{{ viewData.signDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ viewData.expireDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">变更次数</span><div class="rd-value">{{ viewData.changeCount != null ? viewData.changeCount : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.status === '4' && viewData.terminateReason">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></span>终止信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">终止日期</span><div class="rd-value">{{ viewData.terminateDate || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">终止原因</span><div class="rd-value">{{ viewData.terminateReason || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>条款信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">付款条款</span><div class="rd-value">{{ viewData.paymentTerms || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">交货条款</span><div class="rd-value">{{ viewData.deliveryTerms || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div></div>
          <div class="rd-card-body" style="display:block">
            <template v-if="viewData.fileUrl">
              <div class="rd-grid">
                <div class="rd-item rd-item--full" v-for="(file, idx) in viewData.fileUrl.split(',')" :key="idx">
                  <span class="rd-label">合同附件</span>
                  <div class="rd-value">
                    <el-link :href="baseUrl + file" :underline="false" target="_blank" type="primary"><el-icon><Download /></el-icon> {{ getFileName(file) }}</el-link>
                    <el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(file)">预览</el-button>
                  </div>
                </div>
              </div>
            </template>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
              <p class="rd-empty-text">暂无附件</p>
            </div>
            <div class="rd-grid" style="margin-top: 12px">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.changeList && viewData.changeList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 23 20"/></svg></span>变更记录</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="viewData.changeList" border size="small">
              <el-table-column label="变更编号" prop="changeNo" width="140" />
              <el-table-column label="变更类型" prop="changeType" width="100" align="center"><template #default="scope"><dict-tag :options="pms_contract_change_type" :value="scope.row.changeType" /></template></el-table-column>
              <el-table-column label="原内容" prop="originalContent" show-overflow-tooltip />
              <el-table-column label="变更内容" prop="changeContent" show-overflow-tooltip />
              <el-table-column label="变更原因" prop="changeReason" show-overflow-tooltip />
              <el-table-column label="变更人" prop="changeBy" width="100" />
              <el-table-column label="变更日期" prop="changeDate" width="110" align="center" />
              <el-table-column label="审核状态" prop="auditStatus" width="100" align="center"><template #default="scope"><dict-tag :options="pms_contract_change_status" :value="scope.row.auditStatus" /></template></el-table-column>
              <el-table-column label="操作" width="100" align="center" v-if="!viewApproveMode"><template #default="scope"><el-button v-if="scope.row.auditStatus === '0'" link type="warning" size="small" @click="handleAuditChange(scope.row)">审核</el-button></template></el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('viewLog')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.viewLog }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.viewLog" style="display:block">
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
        <section class="rd-card" v-if="viewApproveMode && pendingChangeList.length > 0">
          <div class="rd-card-header" @click="toggleCard('changeApprove')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>变更审批</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.changeApprove }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.changeApprove" style="display:block">
            <div class="change-approve-list">
              <div class="change-approve-item" v-for="c in pendingChangeList" :key="c.changeId">
                <div class="change-approve-item-header">
                  <el-tag size="small" type="warning" effect="light" round>{{ changeFieldLabels[c.fieldName] || c.fieldName || '-' }}</el-tag>
                  <dict-tag :options="pms_contract_change_type" :value="c.changeType" />
                </div>
                <div class="change-approve-item-values">
                  <span class="change-approve-old">{{ c.originalContent || '-' }}</span>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #9ca3af;"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                  <span class="change-approve-new">{{ c.changeContent || '-' }}</span>
                </div>
              </div>
            </div>
            <div class="change-approve-reason" v-if="pendingChangeList[0]?.changeReason">
              <span class="change-approve-reason-label">变更原因：</span>
              <span class="change-approve-reason-text">{{ pendingChangeList[0].changeReason }}</span>
            </div>
            <el-form label-width="100px" style="margin-top: 16px;">
              <el-form-item label="审批意见" required>
                <el-input v-model="changeApproveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer v-if="viewApproveMode && pendingChangeList.length > 0">
        <el-button type="success" @click="confirmChangeApprove(true)">全部通过</el-button>
        <el-button type="danger" @click="confirmChangeApprove(false)">全部驳回</el-button>
        <el-button @click="viewOpen = false">取 消</el-button>
      </template>
      <template #footer v-else>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <file-preview ref="filePreviewRef" />

    <el-dialog v-model="changeOpen" width="1188px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></div><span class="rd-detail-header-title">合同变更</span><div class="rd-detail-header-sub" v-if="changeForm.contractNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ changeForm.contractNo }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('changeOriginal')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>原合同信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.changeOriginal }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.changeOriginal" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ changeForm.contractNo }}</div></div>
            <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ changeForm.contractName }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ changeForm.supplierName }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">合同金额</span><div class="rd-value rd-amount">{{ formatMoney(changeForm.originalContractAmount) }}</div></div>
            <div class="rd-item"><span class="rd-label">签订日期</span><div class="rd-value">{{ changeForm.originalSignDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ changeForm.originalExpireDate || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">付款条款</span><div class="rd-value">{{ changeForm.originalPaymentTerms || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">交货条款</span><div class="rd-value">{{ changeForm.originalDeliveryTerms || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('changeContent')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></span>变更内容</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.changeContent }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.changeContent">
            <el-form ref="changeRef" :model="changeForm" :rules="changeRules" label-width="130px">
              <el-tabs v-model="changeActiveTab" type="border-card">
                <el-tab-pane label="金额变更" name="amount">
                  <el-row :gutter="20"><el-col :span="12"><el-form-item label="原金额"><el-input-number v-model="changeForm.amountOld" :precision="2" :controls="false" disabled style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="新金额" prop="amountNew"><el-input-number v-model="changeForm.amountNew" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col></el-row>
                  <el-form-item label="差额" v-if="changeForm.amountNew != null && changeForm.amountOld != null"><span :style="{ color: changeForm.amountNew - changeForm.amountOld > 0 ? '#e6a23c' : changeForm.amountNew - changeForm.amountOld < 0 ? '#f56c6c' : '#909399', fontWeight: 'bold' }">{{ changeForm.amountNew - changeForm.amountOld > 0 ? '+' : '' }}{{ formatMoney(Math.abs(changeForm.amountNew - changeForm.amountOld)) }}</span></el-form-item>
                </el-tab-pane>
                <el-tab-pane label="日期变更" name="date">
                  <el-row :gutter="20"><el-col :span="24"><el-form-item label="变更字段" prop="dateField"><el-select v-model="changeForm.dateField" style="width: 100%" @change="onDateFieldChange"><el-option label="签订日期" value="signDate" /><el-option label="到期日期" value="expireDate" /></el-select></el-form-item></el-col></el-row>
                  <el-row :gutter="20"><el-col :span="12"><el-form-item label="原日期"><el-date-picker v-model="changeForm.dateOld" type="date" value-format="YYYY-MM-DD" disabled style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="新日期" prop="dateNew"><el-date-picker v-model="changeForm.dateNew" type="date" value-format="YYYY-MM-DD" placeholder="选择新日期" style="width: 100%" /></el-form-item></el-col></el-row>
                </el-tab-pane>
                <el-tab-pane label="条款变更" name="term">
                  <el-row :gutter="20"><el-col :span="24"><el-form-item label="变更字段" prop="termField"><el-select v-model="changeForm.termField" style="width: 100%" @change="onTermFieldChange"><el-option label="付款条款" value="paymentTerms" /><el-option label="交货条款" value="deliveryTerms" /></el-select></el-form-item></el-col></el-row>
                  <el-row :gutter="20"><el-col :span="12"><el-form-item label="原内容"><el-input v-model="changeForm.termOld" type="textarea" :rows="3" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="新内容" prop="termNew"><el-input v-model="changeForm.termNew" type="textarea" :rows="3" placeholder="请输入新内容" /></el-form-item></el-col></el-row>
                </el-tab-pane>
              </el-tabs>
              <el-row :gutter="20" style="margin-top: 16px"><el-col :span="24"><el-form-item label="变更原因" prop="changeReason"><el-input v-model="changeForm.changeReason" type="textarea" :rows="3" placeholder="请详细说明变更原因" /></el-form-item></el-col></el-row>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><el-button type="primary" @click="submitChange">提交变更申请</el-button><el-button @click="changeOpen = false">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="approveOpen" width="1136px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg>
          </div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">合同审批</div>
            <div class="rd-detail-header-sub" v-if="approveForm.contractNo">
              <span class="rd-detail-header-no">编号：{{ approveForm.contractNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ approveForm.contractNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">合同名称</span><div class="rd-value">{{ approveForm.contractName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ approveForm.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">关联采购申请单号</span><div class="rd-value">{{ approveForm.orderNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag :options="pms_contract_type" :value="approveForm.contractType" /></div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_contract_status" :value="approveForm.status" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期与金额</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">合同金额</span><div class="rd-value rd-amount">{{ formatMoney(approveForm.contractAmount) }}</div></div>
            <div class="rd-item"><span class="rd-label">签订日期</span><div class="rd-value">{{ approveForm.signDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">到期日期</span><div class="rd-value">{{ approveForm.expireDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">变更次数</span><div class="rd-value">{{ approveForm.changeCount != null ? approveForm.changeCount : '0' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>条款信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">付款条款</span><div class="rd-value">{{ approveForm.paymentTerms || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">交货条款</span><div class="rd-value">{{ approveForm.deliveryTerms || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div></div>
          <div class="rd-card-body" style="display:block">
            <template v-if="approveForm.fileUrl">
              <div class="rd-grid">
                <div class="rd-item rd-item--full" v-for="(file, idx) in approveForm.fileUrl.split(',')" :key="idx">
                  <span class="rd-label">合同附件</span>
                  <div class="rd-value">
                    <el-link :href="baseUrl + file" :underline="false" target="_blank" type="primary"><el-icon><Download /></el-icon> {{ getFileName(file) }}</el-link>
                    <el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(file)">预览</el-button>
                  </div>
                </div>
              </div>
            </template>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
              <p class="rd-empty-text">暂无附件</p>
            </div>
            <div class="rd-grid" style="margin-top: 12px">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ approveForm.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="approveForm.auditLogList && approveForm.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('approveLog')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.approveLog }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.approveLog" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in approveForm.auditLogList" :key="log.logId">
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
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审批意见</div></div>
          <div class="rd-card-body" style="display:block">
            <el-form label-width="100px">
              <el-form-item label="审批意见" required>
                <el-input v-model="approveForm.auditOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><el-button type="success" @click="confirmApprove(true)">通过</el-button><el-button type="danger" @click="confirmApprove(false)">驳回</el-button><el-button @click="approveOpen = false">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="changeAuditOpen" width="740px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div><span class="rd-detail-header-title">变更审核</span><div class="rd-detail-header-sub" v-if="changeAuditForm.changeNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ changeAuditForm.changeNo }}</span></div></div></template>
      <el-form label-width="100px">
        <el-form-item label="变更类型"><el-input :value="changeAuditForm.changeType ? pms_contract_change_type.find(d => d.value === changeAuditForm.changeType)?.label : '-'" disabled /></el-form-item>
        <el-form-item label="变更内容"><el-input :value="changeAuditForm.changeContent" type="textarea" :rows="2" disabled /></el-form-item>
        <el-form-item label="变更原因"><el-input :value="changeAuditForm.changeReason" type="textarea" :rows="2" disabled /></el-form-item>
        <el-form-item label="审核意见" required><el-input v-model="changeAuditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" /></el-form-item>
      </el-form>
      <template #footer><el-button type="success" @click="confirmChangeAudit('1')">通过</el-button><el-button type="danger" @click="confirmChangeAudit('2')">驳回</el-button><el-button @click="changeAuditOpen = false">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="terminateOpen" width="660px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div><span class="rd-detail-header-title">终止合同</span><div class="rd-detail-header-sub" v-if="terminateForm.contractNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ terminateForm.contractNo }}</span></div></div></template>
      <el-form label-width="100px">
        <el-form-item label="合同名称"><el-input :value="terminateForm.contractName" disabled /></el-form-item>
        <el-form-item label="终止原因" required><el-input v-model="terminateForm.terminateReason" type="textarea" :rows="3" placeholder="请输入终止原因" /></el-form-item>
      </el-form>
      <template #footer><el-button type="danger" @click="confirmTerminate">确认终止</el-button><el-button @click="terminateOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="采购合同业务状态流转说明" width="924px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已签订</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">变更审批中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已到期</el-tag>
          </div>
        </div>
        <div class="status-flow-branch">
          <span style="color: #909399;">分支：</span>
          <el-tag type="danger">已终止</el-tag>
          <span style="color: #909399; margin-left: 8px;">（从已签订状态终止）</span>
        </div>
        
        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建合同后的初始状态，可编辑、删除、提交审批
          </el-descriptions-item>
          <el-descriptions-item label="待审核">
            已提交审批，等待审批人审核。审批人可【通过】或【驳回】
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批未通过，可根据审批意见修改后重新提交
          </el-descriptions-item>
          <el-descriptions-item label="已签订">
            审批通过，合同正式生效。可进行【变更】或【终止】操作
          </el-descriptions-item>
          <el-descriptions-item label="变更审批中">
            合同变更已提交审批，审批通过后回到已签订状态
          </el-descriptions-item>
          <el-descriptions-item label="已到期">
            合同到达到期日期，自动变为已到期状态
          </el-descriptions-item>
          <el-descriptions-item label="已终止">
            合同被提前终止，终止后不可再进行任何操作
          </el-descriptions-item>
        </el-descriptions>
        
        <h4>三、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>提交审批：</strong>草稿状态点击"提交审批"按钮，变为待审核
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审批通过：</strong>审批人点击"通过"，变为已签订状态
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审批驳回：</strong>审批人点击"驳回"，变为已驳回状态，可修改后重新提交
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>合同变更：</strong>已签订状态点击"变更"按钮，提交变更审批，变为变更审批中
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>合同终止：</strong>已签订状态点击"终止"按钮，确认后变为已终止状态
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>到期处理：</strong>系统根据到期日期自动判断，到期后变为已到期状态
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsContract">
import { listContract, getContract, addContract, updateContract, delContract, addContractChange, submitContract, auditContract, auditContractChange, auditContractChangeByContractId, terminateContract } from '@/api/pms/contract'
import { listRequest } from '@/api/pms/request'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatMoney } from '@/composables/useDetailCard'
import { ArrowRight, ArrowDown, QuestionFilled, Search, Filter, WarningFilled, Edit, Delete, Download } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { pms_contract_status, pms_contract_type, pms_contract_change_type, pms_contract_change_status } = proxy.useDict('pms_contract_status', 'pms_contract_type', 'pms_contract_change_type', 'pms_contract_change_status')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_contract_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'date', 'party', 'attach', 'approveBasic', 'date_approve', 'approveTerms', 'approveParty', 'approveAttach', 'approveChange', 'approveLog', 'changeApprove', 'viewLog'])

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  contractNo: { label: '合同编号', visible: true },
  contractName: { label: '合同名称', visible: true },
  supplierName: { label: '供应商', visible: true },
  contractType: { label: '合同类型', visible: true },
  status: { label: '状态', visible: true },
  signDate: { label: '签订日期', visible: true },
  expireDate: { label: '到期日期', visible: true },
  contractAmount: { label: '合同金额', visible: true },
  paidAmount: { label: '已付金额', visible: true },
  unpaidAmount: { label: '未付金额', visible: true },
  signBy: { label: '签订人', visible: true },
  changeCount: { label: '变更次数', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_contract_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const activeStatusTab = ref('all')
const statusTabList = computed(() => pms_contract_status.value.map(d => ({ label: d.label, value: d.value })))
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listContract({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    pms_contract_status.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '5': 'tab-audit', '1': 'tab-approved', '6': 'tab-reject', '3': 'tab-partial', '2': 'tab-done', '4': 'tab-void' }
  return map[value] || ''
}
function badgeClass(status) {
  const map = { '0': 'amber', '5': 'blue', '1': 'green', '6': 'red', '3': 'violet', '2': 'green', '4': 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = pms_contract_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function contractTypeLabel(type) {
  const item = pms_contract_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  queryParams.value.status = tab === 'all' ? undefined : tab
  handleQuery()
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.contractNo) count++
  if (queryParams.value.contractName) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.contractType) count++
  if (queryParams.value.signBy) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const changeOpen = ref(false)
const approveOpen = ref(false)
const changeAuditOpen = ref(false)
const viewData = ref({})
const viewApproveMode = ref(false)
const changeApproveOpinion = ref('')
const approveForm = ref({})
const changeAuditForm = ref({})
const terminateOpen = ref(false)
const terminateForm = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const supplierOptions = ref([])
const requestOptions = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, contractNo: undefined, contractName: undefined, supplierName: undefined, status: undefined, contractType: undefined, signBy: undefined, params: {} },
  rules: { contractName: [{ required: true, message: '合同名称不能为空', trigger: 'blur' }], supplierId: [{ required: true, message: '供应商不能为空', trigger: 'change' }] }
})
const { queryParams, form, rules } = toRefs(data)

const changeForm = ref({})
const changeActiveTab = ref('amount')
const changeRules = {
  amountNew: [{ required: false, message: '请输入新金额', trigger: 'blur' }],
  dateField: [{ required: false, message: '请选择变更字段', trigger: 'change' }],
  dateNew: [{ required: false, message: '请选择新日期', trigger: 'change' }],
  termField: [{ required: false, message: '请选择变更字段', trigger: 'change' }],
  termNew: [{ required: false, message: '请输入新内容', trigger: 'blur' }],
  changeReason: [{ required: true, message: '请输入变更原因', trigger: 'blur' }]
}
const changeFieldLabels = { contractAmount: '合同金额', signDate: '签订日期', expireDate: '到期日期', paymentTerms: '付款条款', deliveryTerms: '交货条款' }
const pendingChangeList = computed(() => {
  if (!viewData.value.changeList) return []
  return viewData.value.changeList.filter(c => c.auditStatus === '0')
})

function getList() { loading.value = true; listContract(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'SignDate'); getList() }
function resetQuery() { queryParams.value.contractNo = undefined; queryParams.value.contractName = undefined; queryParams.value.supplierName = undefined; queryParams.value.status = undefined; queryParams.value.contractType = undefined; queryParams.value.signBy = undefined; queryParams.value.params = {}; dateRange.value = []; activeStatusTab.value = 'all'; if (tableRef.value) tableRef.value.clearSort(); handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.contractId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { contractId: undefined, contractNo: undefined, contractName: undefined, orderId: undefined, orderNo: undefined, supplierId: undefined, supplierName: undefined, status: '0', contractType: '0', signDate: undefined, effectiveDate: undefined, expireDate: undefined, contractAmount: 0, paymentTerms: undefined, deliveryTerms: undefined, signBy: undefined, signDepartment: undefined, partyA: undefined, partyB: undefined, fileUrl: undefined, fileName: undefined, remark: undefined }; proxy.resetForm('contractRef') }
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function onRequestChange(val) { const matched = requestOptions.value.find(r => r.requestId === val); form.value.orderNo = matched ? matched.requestNo : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function loadRequestOptions() { listRequest({ pageNum: 1, pageSize: 999 }).then(res => { requestOptions.value = res.rows || [] }) }
function getFileName(url) { if (url.lastIndexOf('/') > -1) { return url.slice(url.lastIndexOf('/') + 1) } return url }
function handlePreview(fileUrl) { proxy.$refs.filePreviewRef.open(fileUrl, getFileName(fileUrl)) }
function handleAdd() { reset(); open.value = true; title.value = '添加采购合同' }
function handleUpdate(row) { reset(); getContract(row.contractId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改采购合同' }) }
function handleView(row, approveMode = false) { getContract(row.contractId).then(res => { viewData.value = res.data; viewApproveMode.value = approveMode; changeApproveOpinion.value = ''; viewOpen.value = true }) }
function handleChange(row) { getContract(row.contractId).then(res => { const orig = res.data; changeForm.value = { contractId: orig.contractId, contractNo: orig.contractNo, contractName: orig.contractName, supplierName: orig.supplierName, originalContractAmount: orig.contractAmount, originalSignDate: orig.signDate, originalExpireDate: orig.expireDate, originalPaymentTerms: orig.paymentTerms, originalDeliveryTerms: orig.deliveryTerms, amountOld: orig.contractAmount, amountNew: null, dateField: 'signDate', dateOld: orig.signDate || '', dateNew: '', termField: 'paymentTerms', termOld: orig.paymentTerms || '', termNew: '', changeReason: '' }; changeActiveTab.value = 'amount'; changeOpen.value = true }) }
function onDateFieldChange() { const f = changeForm.value; if (f.dateField === 'signDate') f.dateOld = f.originalSignDate || ''; else if (f.dateField === 'expireDate') f.dateOld = f.originalExpireDate || ''; f.dateNew = '' }
function onTermFieldChange() { const f = changeForm.value; if (f.termField === 'paymentTerms') f.termOld = f.originalPaymentTerms || ''; else if (f.termField === 'deliveryTerms') f.termOld = f.originalDeliveryTerms || ''; f.termNew = '' }
function submitChange() { proxy.$refs['changeRef'].validate(valid => { if (!valid) return; const f = changeForm.value; const submitData = []; if (f.amountNew != null && String(f.amountNew) !== String(f.amountOld)) { submitData.push({ contractId: f.contractId, changeType: '0', fieldName: 'contractAmount', newValue: String(f.amountNew), originalContent: '合同金额: ' + f.amountOld, changeContent: '合同金额: ' + f.amountNew, changeReason: f.changeReason }) } if (f.dateField && f.dateNew && f.dateNew !== f.dateOld) { submitData.push({ contractId: f.contractId, changeType: '1', fieldName: f.dateField, newValue: f.dateNew, originalContent: (changeFieldLabels[f.dateField] || f.dateField) + ': ' + (f.dateOld || ''), changeContent: (changeFieldLabels[f.dateField] || f.dateField) + ': ' + f.dateNew, changeReason: f.changeReason }) } if (f.termField && f.termNew && f.termNew !== f.termOld) { submitData.push({ contractId: f.contractId, changeType: '2', fieldName: f.termField, newValue: f.termNew, originalContent: (changeFieldLabels[f.termField] || f.termField) + ': ' + (f.termOld || ''), changeContent: (changeFieldLabels[f.termField] || f.termField) + ': ' + f.termNew, changeReason: f.changeReason }) } if (submitData.length === 0) { proxy.$modal.msgWarning('请至少填写一项变更内容，且新值不能与原值相同'); return } const promises = submitData.map(d => addContractChange(d)); Promise.all(promises).then(() => { proxy.$modal.msgSuccess('变更申请已提交，等待审核'); changeOpen.value = false; getList() }).catch(() => {}) }) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交合同【' + row.contractNo + '】进行审批？').then(() => submitContract(row.contractId)).then(() => { getList(); proxy.$modal.msgSuccess('已提交审批') }).catch(() => {}) }
function handleApprove(row) { getContract(row.contractId).then(res => { approveForm.value = { ...res.data, auditOpinion: '' }; approveOpen.value = true }) }
function confirmApprove(passed) { if (!approveForm.value.auditOpinion) { proxy.$modal.msgWarning('请输入审批意见'); return } const status = passed ? '1' : '6'; auditContract(approveForm.value.contractId, status, approveForm.value.auditOpinion).then(() => { proxy.$modal.msgSuccess(passed ? '审批通过，合同已签订' : '已驳回'); approveOpen.value = false; getList() }).catch(() => {}) }
function handleAuditChange(row) { changeAuditForm.value = { ...row, auditRemark: '' }; changeAuditOpen.value = true }
function confirmChangeAudit(auditStatus) { if (!changeAuditForm.value.auditRemark) { proxy.$modal.msgWarning('请输入审核意见'); return } auditContractChange(changeAuditForm.value.changeId, auditStatus, changeAuditForm.value.auditRemark).then(() => { proxy.$modal.msgSuccess(auditStatus === '1' ? '审核通过' : '已驳回'); changeAuditOpen.value = false; handleView({ contractId: changeAuditForm.value.contractId }) }).catch(() => {}) }
function confirmChangeApprove(approved) { if (!changeApproveOpinion.value) { proxy.$modal.msgWarning('请输入审批意见'); return } const auditStatus = approved ? '1' : '2'; auditContractChangeByContractId(viewData.value.contractId, auditStatus, changeApproveOpinion.value).then(() => { proxy.$modal.msgSuccess(approved ? '审核通过' : '已驳回'); viewOpen.value = false; getList() }).catch(() => {}) }
function handleTerminate(row) { terminateForm.value = { contractId: row.contractId, contractNo: row.contractNo, contractName: row.contractName, terminateReason: '' }; terminateOpen.value = true }
function confirmTerminate() { if (!terminateForm.value.terminateReason) { proxy.$modal.msgWarning('请输入终止原因'); return } terminateContract(terminateForm.value.contractId, terminateForm.value.terminateReason).then(() => { proxy.$modal.msgSuccess('合同已终止'); terminateOpen.value = false; getList() }).catch(() => {}) }
function submitForm() { proxy.$refs['contractRef'].validate(valid => { if (valid) { if (form.value.contractId != undefined) { updateContract(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addContract(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const contractIds = row.contractId || ids.value; proxy.$modal.confirm('确认删除编号为"' + contractIds + '"的数据？').then(() => delContract(contractIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('pms/contract/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'SignDate') }, `contract_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

loadSupplierOptions()
loadRequestOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-contract-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.pms-contract-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-contract-page .filter-card { padding:14px 20px 16px; }
.pms-contract-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-contract-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-contract-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-contract-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-contract-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-contract-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-contract-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-contract-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-contract-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-contract-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-contract-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-contract-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-contract-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-contract-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-contract-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-contract-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-contract-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-contract-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-contract-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-contract-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-contract-page .field .control :deep(.el-select) { width:100%; }
.pms-contract-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-contract-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-contract-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-contract-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-contract-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-contract-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-contract-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-contract-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-contract-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-contract-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-contract-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-contract-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-contract-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-contract-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-contract-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-contract-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-contract-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-contract-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-contract-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-contract-page .status-tab.tab-approved .dot, .pms-contract-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-contract-page .status-tab.tab-approved .count, .pms-contract-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-contract-page .status-tab.is-active.tab-approved .count, .pms-contract-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-contract-page .status-tab.tab-reject .dot { background:var(--red-500); }
.pms-contract-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.pms-contract-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.pms-contract-page .status-tab.tab-void .dot { background:var(--ink-400); }
.pms-contract-page .status-tab.tab-partial .dot { background:var(--violet-500, #8b5cf6); }
.pms-contract-page .status-tab.tab-partial .count { background:var(--violet-50); color:#7c3aed; }
.pms-contract-page .status-tab.is-active.tab-partial .count { background:var(--violet-500, #8b5cf6); color:#fff; }
.pms-contract-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.pms-contract-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-contract-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-contract-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-contract-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-contract-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-contract-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-contract-page .btn-soft .el-icon { font-size:14px; }
.pms-contract-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-contract-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-contract-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-contract-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-contract-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-contract-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-contract-page .table-wrap { overflow-x:auto; }
.pms-contract-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-contract-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-contract-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-contract-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-contract-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-contract-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-contract-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-contract-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-contract-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-contract-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-contract-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-contract-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-contract-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-contract-page .badge.amber .dot { background:var(--amber-500); }
.pms-contract-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-contract-page .badge.blue .dot { background:var(--blue-500); }
.pms-contract-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-contract-page .badge.green .dot { background:var(--green-500); }
.pms-contract-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pms-contract-page .badge.red .dot { background:var(--red-500); }
.pms-contract-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-contract-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-contract-page .badge.gray .dot { background:var(--ink-400); }
.pms-contract-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-contract-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-contract-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-contract-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-contract-page .pagination-container :deep(.el-pagination .btn-prev), .pms-contract-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-contract-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-contract-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-contract-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-contract-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-contract-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-contract-page .toolbar { flex-wrap:wrap; gap:10px; } }

.change-approve-list { display: flex; flex-direction: column; gap: 10px; }
.change-approve-item { padding: 12px 14px; background: #fef3c7; border-radius: 10px; border: 1px solid #fde68a; border-left: 3px solid #f59e0b; }
.change-approve-item-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.change-approve-item-values { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.change-approve-reason { margin-top: 12px; padding: 10px 12px; background: #f0f9ff; border-radius: 8px; border-left: 3px solid #3b82f6; display: flex; align-items: center; gap: 8px; }
.change-approve-reason-label { font-size: 13px; font-weight: 600; color: #1e40af; white-space: nowrap; }
.change-approve-reason-text { font-size: 13px; color: #1f2937; line-height: 1.5; }
.change-approve-old { color: #9ca3af; text-decoration: line-through; font-variant-numeric: tabular-nums; }
.change-approve-new { color: #111827; font-weight: 700; font-variant-numeric: tabular-nums; }

.status-help-content {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.status-help-content h4 {
  margin: 20px 0 12px 0;
  color: #303133;
  font-weight: 600;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.status-help-content h4:first-child {
  margin-top: 0;
}

.status-flow {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.status-flow-branch {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background-color: #fef0f0;
  border-radius: 8px;
  margin-bottom: 8px;
}

.flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flow-arrow {
  color: #909399;
  font-size: 16px;
}

:deep(.el-timeline-item__node) {
  background-color: transparent;
  border: 2px solid;
}

:deep(.el-timeline-item__node--primary) {
  border-color: #409eff;
}

:deep(.el-timeline-item__node--success) {
  border-color: #67c23a;
}

:deep(.el-timeline-item__node--warning) {
  border-color: #e6a23c;
}

:deep(.el-timeline-item__node--danger) {
  border-color: #f56c6c;
}

:deep(.el-timeline-item__node--info) {
  border-color: #909399;
}
</style>
