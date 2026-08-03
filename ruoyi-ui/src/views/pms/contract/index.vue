<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同编号" prop="contractNo"><el-input v-model="queryParams.contractNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="合同名称" prop="contractName"><el-input v-model="queryParams.contractName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="供应商" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px"><el-option v-for="d in pms_contract_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain icon="More" @click="showAdvanced = !showAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
      <template v-if="showAdvanced">
        <el-form-item label="合同类型" prop="contractType"><el-select v-model="queryParams.contractType" placeholder="全部" clearable style="width: 140px"><el-option v-for="d in pms_contract_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
        <el-form-item label="签订人" prop="signBy"><el-input v-model="queryParams.signBy" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="签订日期"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
      </template>
    </el-form>

    <!-- 业务状态流转说明 -->
    <el-alert type="info" :closable="false" show-icon class="mb8">
      <template #title>
        <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
          <span>业务状态流转：</span>
          <el-tag size="small" type="info">草稿</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="warning">待审核</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="success">已签订</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="primary">变更审批中</el-tag>
          <el-icon><ArrowRight /></el-icon>
          <el-tag size="small" type="info">已到期</el-tag>
          <span style="color: #909399; margin: 0 4px;">/</span>
          <el-tag size="small" type="danger">已终止</el-tag>
          <el-button link type="primary" size="small" @click="showStatusHelp = true" style="margin-left: 8px;">
            <el-icon><QuestionFilled /></el-icon> 查看详情
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:contract:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:contract:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:contract:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:contract:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="合同编号" prop="contractNo" :width="colWidth('contractNo', 160)" resizable sortable="custom" />
      <el-table-column label="合同名称" prop="contractName" :width="colWidth('contractName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="合同类型" prop="contractType" :width="colWidth('contractType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="pms_contract_type" :value="scope.row.contractType" /></template></el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom"><template #default="scope"><dict-tag :options="pms_contract_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="签订日期" prop="signDate" :width="colWidth('signDate', 110)" resizable align="center" />
      <el-table-column label="到期日期" prop="expireDate" :width="colWidth('expireDate', 110)" resizable align="center" />
      <el-table-column label="合同金额" prop="contractAmount" :width="colWidth('contractAmount', 120)" resizable align="right" sortable="custom"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.contractAmount) }}</span></template></el-table-column>
      <el-table-column label="已付金额" prop="paidAmount" :width="colWidth('paidAmount', 120)" resizable align="right"><template #default="scope"><span class="rd-amount" :style="{ color: scope.row.paidAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(scope.row.paidAmount) }}</span></template></el-table-column>
      <el-table-column label="未付金额" :width="colWidth('unpaidAmount', 120)" resizable align="right"><template #default="scope"><span class="rd-amount" :style="{ color: (scope.row.contractAmount - scope.row.paidAmount) > 0 ? '#f56c6c' : '#67c23a' }">{{ formatMoney((scope.row.contractAmount || 0) - (scope.row.paidAmount || 0)) }}</span></template></el-table-column>
      <el-table-column label="签订人" prop="signBy" :width="colWidth('signBy', 100)" resizable />
      <el-table-column label="变更次数" prop="changeCount" :width="colWidth('changeCount', 90)" resizable align="center" />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
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
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

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
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
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
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
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
import { ArrowRight, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { pms_contract_status, pms_contract_type, pms_contract_change_type, pms_contract_change_status } = proxy.useDict('pms_contract_status', 'pms_contract_type', 'pms_contract_change_type', 'pms_contract_change_status')
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_contract_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'date', 'party', 'attach', 'approveBasic', 'date_approve', 'approveTerms', 'approveParty', 'approveAttach', 'approveChange', 'approveLog', 'changeApprove'])

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
  queryParams: { pageNum: 1, pageSize: 10, contractNo: undefined, contractName: undefined, supplierName: undefined, status: undefined, params: {} },
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

function getList() { loading.value = true; listContract(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; handleQuery() }
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
function handleExport() { proxy.download('pms/contract/export', { ...queryParams.value }, `contract_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

loadSupplierOptions()
loadRequestOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
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
