<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="结算单号" prop="invoiceNo"><el-input v-model="queryParams.invoiceNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="供应商" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px"><el-option v-for="d in pms_invoice_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="发票类型" prop="invoiceType"><el-select v-model="queryParams.invoiceType" placeholder="全部" clearable style="width: 160px"><el-option v-for="d in pms_invoice_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain icon="More" @click="showAdvanced = !showAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
      <template v-if="showAdvanced">
        <el-form-item label="发票号码" prop="invoiceNumber"><el-input v-model="queryParams.invoiceNumber" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
        <el-form-item label="开票日期"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
      </template>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:invoice:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:invoice:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:invoice:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:invoice:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="结算单号" prop="invoiceNo" :width="colWidth('invoiceNo', 160)" resizable sortable="custom" />
      <el-table-column label="合同编号" prop="contractNo" :width="colWidth('contractNo', 140)" resizable />
      <el-table-column label="采购单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom"><template #default="scope"><dict-tag :options="pms_invoice_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="发票类型" prop="invoiceType" :width="colWidth('invoiceType', 130)" resizable align="center"><template #default="scope"><dict-tag :options="pms_invoice_type" :value="scope.row.invoiceType" /></template></el-table-column>
      <el-table-column label="发票号码" prop="invoiceNumber" :width="colWidth('invoiceNumber', 140)" resizable />
      <el-table-column label="开票日期" prop="invoiceDate" :width="colWidth('invoiceDate', 120)" resizable align="center" sortable="custom" />
      <el-table-column label="不含税金额" prop="invoiceAmount" :width="colWidth('invoiceAmount', 130)" resizable align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.invoiceAmount) }}</span></template></el-table-column>
      <el-table-column label="税额" prop="taxAmount" :width="colWidth('taxAmount', 120)" resizable align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.taxAmount) }}</span></template></el-table-column>
      <el-table-column label="开票金额" prop="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
      <el-table-column label="已付金额" prop="paymentAmount" :width="colWidth('paymentAmount', 130)" resizable align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.paymentAmount) }}</span></template></el-table-column>
      <el-table-column label="付款日期" prop="paymentDate" :width="colWidth('paymentDate', 120)" resizable align="center" />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
<el-table-column label="操作" width="240" align="center" fixed="right">
      <template #default="scope">
        <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
        <el-button link type="info" icon="Connection" @click="handleMatch(scope.row)" v-hasPermi="['pms:invoice:query']" v-if="scope.row.orderNo">三方匹配</el-button>
        <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:invoice:edit']" v-if="scope.row.status === '0' || scope.row.status === '5'">修改</el-button>
        <el-button link type="success" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['pms:invoice:edit']" v-if="scope.row.status === '0' || scope.row.status === '5'">提交</el-button>
        <el-button link type="warning" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:invoice:audit']" v-if="scope.row.status === '1'">审批</el-button>
        <el-button link type="success" icon="Wallet" @click="handlePay(scope.row)" v-hasPermi="['pms:invoice:pay']" v-if="scope.row.status === '2' || scope.row.status === '6'">付款</el-button>
        <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:invoice:remove']" v-if="scope.row.status === '0'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="invoiceRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <!-- 发票识别 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('recognize')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 4V2"/><path d="M15 16v-2"/><path d="M8 16v-2"/><path d="M8 4V2"/>
                    <path d="M3 10h18"/><path d="M5 6h14"/><path d="M5 18h14"/>
                    <path d="M3 8V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v2"/>
                    <path d="M3 16v-2a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v2"/>
                  </svg>
                </span>
                发票识别（上传PDF自动识别）
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.recognize }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.recognize">
              <div class="recognize-area">
                <input ref="fileInputRef" type="file" accept=".pdf" style="display: none" @change="handleFileChange" />
                <el-button type="success" icon="MagicStick" :loading="recognizing" @click="fileInputRef?.click()">{{ recognizing ? '识别中...' : '上传PDF发票识别' }}</el-button>
                <span class="recognize-tip">仅支持PDF格式电子发票，上传后自动识别并回填发票信息</span>
              </div>
              <div v-if="recognizedFields.length" class="recognized-info">
                <el-tag v-for="f in recognizedFields" :key="f.label" type="success" class="recognized-tag">{{ f.label }}：{{ f.value }}</el-tag>
              </div>
            </div>
          </section>
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="结算单号" prop="invoiceNo">
                    <el-input v-model="form.invoiceNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="关联合同" prop="contractId">
                    <el-select v-model="form.contractId" filterable clearable placeholder="请选择合同（可选）" style="width: 100%" @change="onContractChange">
                      <el-option v-for="c in contractOptions" :key="c.contractId" :label="c.contractNo" :value="c.contractId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="采购单号" prop="orderId">
                    <el-select v-model="form.orderId" filterable clearable placeholder="请选择采购订单（可选）" style="width: 100%" @change="onOrderChange">
                      <el-option v-for="o in orderOptions" :key="o.orderId" :label="o.orderNo" :value="o.orderId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="供应商" prop="supplierId">
                    <el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange">
                      <el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 发票信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>发票信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="发票类型" prop="invoiceType">
                    <el-select v-model="form.invoiceType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in pms_invoice_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="发票号码" prop="invoiceNumber">
                    <el-input v-model="form.invoiceNumber" placeholder="请输入发票号码" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开票日期" prop="invoiceDate">
                    <el-date-picker v-model="form.invoiceDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="发票抬头" prop="invoiceTitle">
                    <el-input v-model="form.invoiceTitle" placeholder="请输入发票抬头" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="纳税人识别号" prop="taxNumber">
                    <el-input v-model="form.taxNumber" placeholder="请输入纳税人识别号" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="税率(%)" prop="taxRate">
                    <el-input-number v-model="form.taxRate" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTax" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开票金额" prop="totalAmount">
                    <el-input-number v-model="form.totalAmount" :precision="2" :min="0" style="width: 100%" @change="calcTax" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="税额" prop="taxAmount">
                    <el-input-number v-model="form.taxAmount" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 付款信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>付款信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="本次付款金额" prop="payAmount">
                    <el-input-number v-model="form.payAmount" :precision="2" :min="0" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="已付金额" prop="paymentAmount">
                    <el-input-number v-model="form.paymentAmount" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="付款日期" prop="paymentDate">
                    <el-date-picker v-model="form.paymentDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开户银行" prop="bankName">
                    <el-input v-model="form.bankName" placeholder="请输入开户银行" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="银行账号" prop="bankAccount">
                    <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="附件" prop="invoiceAttachment"><file-upload v-model="form.invoiceAttachment" :fileSize="50" :fileType="['pdf']" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <!-- 审核记录 -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
            <div class="rd-card-body" style="display:block">
              <el-alert v-if="form.status === '5'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该发票结算已被驳回，请根据审核意见修改后重新提交</template>
              </el-alert>
              <div class="rd-timeline">
                <div class="rd-timeline-item" v-for="log in form.auditLogList" :key="log.logId">
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></div><span class="rd-detail-header-title">发票结算详情</span><div class="rd-detail-header-sub" v-if="viewData.invoiceNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.invoiceNo }}</span></div></div></template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">结算单号</span><div class="rd-value">{{ viewData.invoiceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联合同</span><div class="rd-value">{{ viewData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_invoice_status" :value="viewData.status" /></div></div>
            </div>
          </div>
        </section>
        <!-- 发票信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>发票信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发票类型</span><div class="rd-value"><dict-tag :options="pms_invoice_type" :value="viewData.invoiceType" /></div></div>
              <div class="rd-item"><span class="rd-label">发票号码</span><div class="rd-value">{{ viewData.invoiceNumber || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开票日期</span><div class="rd-value">{{ viewData.invoiceDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发票抬头</span><div class="rd-value">{{ viewData.invoiceTitle || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">纳税人识别号</span><div class="rd-value">{{ viewData.taxNumber || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">税率(%)</span><div class="rd-value">{{ viewData.taxRate != null ? viewData.taxRate : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不含税金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.invoiceAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">税额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.taxAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">开票金额</span><div class="rd-value rd-amount rd-value--large">{{ formatMoney(viewData.totalAmount) }}</div></div>
            </div>
          </div>
        </section>
        <!-- 付款信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>付款信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">已付金额</span><div class="rd-value rd-amount" :style="{ color: viewData.paymentAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(viewData.paymentAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">未付金额</span><div class="rd-value rd-amount" :style="{ color: (viewData.totalAmount - viewData.paymentAmount) > 0 ? '#f56c6c' : '#67c23a' }">{{ formatMoney((viewData.totalAmount || 0) - (viewData.paymentAmount || 0)) }}</div></div>
              <div class="rd-item"><span class="rd-label">最近付款日期</span><div class="rd-value">{{ viewData.paymentDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">付款方式</span><div class="rd-value"><dict-tag :options="wms_payment_method" :value="viewData.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">开户银行</span><div class="rd-value">{{ viewData.bankName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">银行账号</span><div class="rd-value">{{ viewData.bankAccount || '-' }}</div></div>
            </div>
            <!-- 付款记录列表 -->
            <div v-if="viewData.paymentLogList && viewData.paymentLogList.length" style="margin-top: 16px">
              <el-table :data="viewData.paymentLogList" border size="small">
                <el-table-column label="付款编号" prop="paymentNo" width="150" align="center" />
                <el-table-column label="付款金额" prop="paymentAmount" width="130" align="right"><template #default="scope"><span class="rd-amount" style="color: #67c23a">{{ formatMoney(scope.row.paymentAmount) }}</span></template></el-table-column>
                <el-table-column label="付款日期" prop="paymentDate" width="120" align="center" />
                <el-table-column label="付款方式" prop="paymentMethod" width="100" align="center"><template #default="scope"><dict-tag :options="wms_payment_method" :value="scope.row.paymentMethod" /></template></el-table-column>
                <el-table-column label="付款人" prop="payer" width="100" align="center" />
                <el-table-column label="开户银行" prop="bankName" min-width="120" show-overflow-tooltip />
                <el-table-column label="银行账号" prop="bankAccount" min-width="120" show-overflow-tooltip />
                <el-table-column label="付款凭证" prop="attachment" width="100" align="center"><template #default="scope"><el-link v-if="scope.row.attachment" type="primary" :href="baseUrl + scope.row.attachment" target="_blank" :underline="false">查看</el-link><span v-else class="rd-value--muted">-</span></template></el-table-column>
                <el-table-column label="付款时间" prop="createTime" width="160" align="center" />
                <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
              </el-table>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v5')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v5">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">附件</span><div class="rd-value"><el-link v-if="viewData.invoiceAttachment" type="primary" :href="baseUrl + viewData.invoiceAttachment" target="_blank">{{ getFileName(viewData.invoiceAttachment) }}</el-link><span v-else class="rd-value--muted">暂无附件</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 审核记录 -->
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
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">发票审批</span>
          <div class="rd-detail-header-sub" v-if="auditData.invoiceNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">结算单号</span><div class="rd-value">{{ auditData.invoiceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联合同</span><div class="rd-value">{{ auditData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ auditData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ auditData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_invoice_status" :value="auditData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">发票类型</span><div class="rd-value"><dict-tag :options="pms_invoice_type" :value="auditData.invoiceType" /></div></div>
              <div class="rd-item"><span class="rd-label">发票号码</span><div class="rd-value">{{ auditData.invoiceNumber || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开票日期</span><div class="rd-value">{{ auditData.invoiceDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 发票信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>发票信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发票抬头</span><div class="rd-value">{{ auditData.invoiceTitle || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">纳税人识别号</span><div class="rd-value">{{ auditData.taxNumber || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">税率(%)</span><div class="rd-value">{{ auditData.taxRate != null ? auditData.taxRate : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不含税金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.invoiceAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">税额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.taxAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">开票金额</span><div class="rd-value rd-amount rd-value--large">{{ formatMoney(auditData.totalAmount) }}</div></div>
            </div>
          </div>
        </section>
        <!-- 付款信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>付款信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">本次付款金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.payAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已付金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.paymentAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">付款日期</span><div class="rd-value">{{ auditData.paymentDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">付款方式</span><div class="rd-value"><dict-tag :options="wms_payment_method" :value="auditData.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">开户银行</span><div class="rd-value">{{ auditData.bankName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">银行账号</span><div class="rd-value">{{ auditData.bankAccount || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 发票附件 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>发票附件</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">发票文件</span><div class="rd-value"><el-link v-if="auditData.invoiceAttachment" type="primary" :href="baseUrl + auditData.invoiceAttachment" target="_blank" :underline="false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: -2px; margin-right: 4px"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>{{ getFileName(auditData.invoiceAttachment) }}</el-link><span v-else class="rd-value--muted">暂无附件</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
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
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
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

    <!-- ===== 付款弹窗 ===== -->
    <el-dialog v-model="payOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></div>
          <span class="rd-detail-header-title">发票付款</span>
          <div class="rd-detail-header-sub" v-if="payData.invoiceNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ payData.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 结算信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>结算信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">结算单号</span><div class="rd-value">{{ payData.invoiceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">收款单位</span><div class="rd-value">{{ payData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联合同</span><div class="rd-value">{{ payData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ payData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">开票金额</span><div class="rd-value rd-amount rd-value--large">{{ formatMoney(payData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已付金额</span><div class="rd-value rd-amount" style="color: #67c23a">{{ formatMoney(payData.paymentAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">未付金额</span><div class="rd-value rd-amount" style="color: #f56c6c">{{ formatMoney(payUnpaid) }}</div></div>
              <div class="rd-item"><span class="rd-label">发票号码</span><div class="rd-value">{{ payData.invoiceNumber || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 本次付款 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('p1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>本次付款</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.p1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.p1">
            <el-form ref="payRef" :model="payForm" :rules="payRules" label-width="110px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="本次付款金额" prop="paymentAmount">
                    <el-input-number v-model="payForm.paymentAmount" :precision="2" :min="0.01" :max="payUnpaid" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="付款日期" prop="paymentDate">
                    <el-date-picker v-model="payForm.paymentDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="付款方式" prop="paymentMethod">
                    <el-select v-model="payForm.paymentMethod" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in wms_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="开户银行" prop="bankName">
                    <el-input v-model="payForm.bankName" placeholder="请输入开户银行" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="银行账号" prop="bankAccount">
                    <el-input v-model="payForm.bankAccount" placeholder="请输入银行账号" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="付款凭证" prop="attachment">
                    <file-upload v-model="payForm.attachment" :fileSize="20" :fileType="['pdf','png','jpg','jpeg']" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="payForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </section>
        <!-- 付款记录 -->
        <section class="rd-card" v-if="payData.paymentLogList && payData.paymentLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>付款记录</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="payData.paymentLogList" border size="small">
              <el-table-column label="付款编号" prop="paymentNo" width="150" align="center" />
              <el-table-column label="付款金额" prop="paymentAmount" width="130" align="right"><template #default="scope"><span class="rd-amount" style="color: #67c23a">{{ formatMoney(scope.row.paymentAmount) }}</span></template></el-table-column>
              <el-table-column label="付款日期" prop="paymentDate" width="120" align="center" />
              <el-table-column label="付款方式" prop="paymentMethod" width="100" align="center"><template #default="scope"><dict-tag :options="wms_payment_method" :value="scope.row.paymentMethod" /></template></el-table-column>
              <el-table-column label="付款人" prop="payer" width="100" align="center" />
              <el-table-column label="开户银行" prop="bankName" min-width="120" show-overflow-tooltip />
              <el-table-column label="银行账号" prop="bankAccount" min-width="120" show-overflow-tooltip />
              <el-table-column label="付款凭证" prop="attachment" width="100" align="center"><template #default="scope"><el-link v-if="scope.row.attachment" type="primary" :href="baseUrl + scope.row.attachment" target="_blank" :underline="false">查看</el-link><span v-else class="rd-value--muted">-</span></template></el-table-column>
              <el-table-column label="付款时间" prop="createTime" width="160" align="center" />
              <el-table-column label="备注" prop="remark" min-width="100" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="submitPay" :disabled="payUnpaid <= 0">确认付款</el-button>
        <el-button @click="payOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 三方匹配结果弹窗 -->
    <el-dialog v-model="matchOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 3h5v5"/><path d="M8 3H3v5"/><path d="M21 16v5h-5"/><path d="M3 16v5h5"/>
              <line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">三方匹配结果</span>
          <div class="rd-detail-header-sub" v-if="matchData.invoiceNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">发票：{{ matchData.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="matchData.matchResult">
        <!-- 匹配结果 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('m1')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                </svg>
              </span>
              匹配结果
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.m1 }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.m1">
            <el-alert :type="matchData.matchResult === '匹配成功' ? 'success' : 'warning'" :closable="false" show-icon class="mb8">
              <template #title>{{ matchData.matchResult }}</template>
            </el-alert>
            <div class="rd-grid" style="margin-top: 12px">
              <div class="rd-item">
                <span class="rd-label">订单匹配</span>
                <div class="rd-value">
                  <el-tag v-if="matchData.orderMatch" type="success" size="small" effect="light" round>一致</el-tag>
                  <el-tag v-else type="danger" size="small" effect="light" round>不一致</el-tag>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">收货匹配</span>
                <div class="rd-value">
                  <el-tag v-if="matchData.receiveMatch" type="success" size="small" effect="light" round>一致</el-tag>
                  <el-tag v-else type="danger" size="small" effect="light" round>不一致</el-tag>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 三方金额对比 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('m2')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                </svg>
              </span>
              三方金额对比
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.m2 }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.m2">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">采购订单号</span>
                <div class="rd-value">{{ matchData.orderNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">订单金额</span>
                <div class="rd-value rd-amount">{{ formatMoney(matchData.orderAmount) }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">发票单号</span>
                <div class="rd-value">{{ matchData.invoiceNo || '-' }}</div>
              </div>
              <div class="rd-item">
                <span class="rd-label">发票开票金额</span>
                <div class="rd-value rd-amount">{{ formatMoney(matchData.invoiceAmount) }}</div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">收货合格金额</span>
                <div class="rd-value rd-amount">{{ formatMoney(matchData.receivedAmount) }}</div>
              </div>
            </div>
          </div>
        </section>
        <!-- 差异分析 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('m3')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>
                </svg>
              </span>
              差异分析
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.m3 }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.m3">
            <div class="rd-grid">
              <div class="rd-item">
                <span class="rd-label">订单-发票差异</span>
                <div class="rd-value">
                  <span class="rd-diff" :class="{ 'rd-diff--ok': Number(matchData.orderInvoiceDiff) === 0, 'rd-diff--err': Number(matchData.orderInvoiceDiff) !== 0 }">{{ formatMoney(matchData.orderInvoiceDiff) }}</span>
                </div>
              </div>
              <div class="rd-item">
                <span class="rd-label">收货-发票差异</span>
                <div class="rd-value">
                  <span class="rd-diff" :class="{ 'rd-diff--ok': Number(matchData.receiveInvoiceDiff) === 0, 'rd-diff--err': Number(matchData.receiveInvoiceDiff) !== 0 }">{{ formatMoney(matchData.receiveInvoiceDiff) }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 收货明细 -->
        <section class="rd-card" v-if="matchData.receiveDetails && matchData.receiveDetails.length > 0">
          <div class="rd-card-header" @click="toggleCard('m4')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                  <polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>
                </svg>
              </span>
              收货明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.m4 }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.m4" style="display:block">
            <el-table :data="matchData.receiveDetails" border size="small">
              <el-table-column label="收货单号" prop="receiveNo" width="140" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="120" align="center" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip align="center" />
              <el-table-column label="合格数量" prop="qualifiedQty" width="100" align="center" />
              <el-table-column label="单价" prop="unitPrice" width="100" align="center" />
              <el-table-column label="金额" prop="amount" width="120" align="center"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="matchOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsInvoice">
import { listInvoice, getInvoice, addInvoice, updateInvoice, delInvoice, submitInvoice, auditInvoice, payInvoice, threeWayMatch, recognizeInvoice } from '@/api/pms/invoice'
import { listContract } from '@/api/pms/contract'
import { listOrder, getOrder } from '@/api/pms/order'
import { listSupplier } from '@/api/wms/supplier'
import request from '@/utils/request'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { pms_invoice_status, pms_invoice_type, wms_payment_method } = proxy.useDict('pms_invoice_status', 'pms_invoice_type', 'wms_payment_method')
const baseUrl = import.meta.env.VITE_APP_BASE_API

const { collapsedCards, toggleCard } = useDetailCard(["recognize","c1","c2","c3","c0","v1","v3","v4","v5","a1","p1","m1","m2","m3","m4"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_invoice_index')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const matchOpen = ref(false)
const matchData = ref({})
const auditOpen = ref(false)
const auditData = ref({})
const auditForm = ref({ invoiceId: undefined, auditOpinion: undefined })
const auditRules = { auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }] }
const payOpen = ref(false)
const payData = ref({})
const payForm = ref({ invoiceId: undefined, paymentAmount: undefined, paymentDate: undefined, paymentMethod: '0', bankName: undefined, bankAccount: undefined, payer: undefined, attachment: undefined, remark: undefined })
const payRules = {
  paymentAmount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }],
  paymentDate: [{ required: true, message: '请选择付款日期', trigger: 'change' }],
  paymentMethod: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
}
const payUnpaid = computed(() => {
  const total = Number(payData.value.totalAmount) || 0
  const paid = Number(payData.value.paymentAmount) || 0
  return Math.max(0, parseFloat((total - paid).toFixed(2)))
})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const supplierOptions = ref([])
const contractOptions = ref([])
const orderOptions = ref([])
const ocrLoading = ref(false)
const recognizing = ref(false)
const recognizedFields = ref([])
const fileInputRef = ref(null)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, invoiceNo: undefined, supplierName: undefined, status: undefined, invoiceType: undefined, params: {} },
  rules: {}
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listInvoice(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.invoiceId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { invoiceId: undefined, invoiceNo: undefined, contractId: undefined, contractNo: undefined, orderId: undefined, orderNo: undefined, supplierId: undefined, supplierName: undefined, status: '0', invoiceType: '0', invoiceNumber: undefined, invoiceDate: undefined, invoiceTitle: undefined, taxNumber: undefined, invoiceImageUrl: undefined, invoiceAttachment: undefined, taxRate: 0, taxAmount: 0, totalAmount: 0, paymentAmount: 0, payAmount: 0, paymentDate: undefined, paymentMethod: '0', bankName: undefined, bankAccount: undefined, remark: undefined }; proxy.resetForm('invoiceRef'); recognizedFields.value = [] }
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function loadContractOptions() { listContract({ pageNum: 1, pageSize: 999 }).then(res => { contractOptions.value = (res.rows || []).filter(c => !['0','5','6'].includes(c.status)) }) }
function onContractChange(contractId) {
  if (!contractId) { form.value.contractNo = undefined; return }
  const matched = contractOptions.value.find(c => c.contractId === contractId)
  if (matched) { form.value.contractNo = matched.contractNo; form.value.supplierId = matched.supplierId; form.value.supplierName = matched.supplierName }
}
function loadOrderOptions() { listOrder({ pageNum: 1, pageSize: 999 }).then(res => { orderOptions.value = (res.rows || []).filter(o => ['2','3','4','5'].includes(o.status)) }) }
function onOrderChange(orderId) {
  if (!orderId) { form.value.orderNo = undefined; return }
  getOrder(orderId).then(res => {
    const order = res.data
    form.value.orderNo = order.orderNo
    form.value.supplierId = order.supplierId
    form.value.supplierName = order.supplierName
    if (order.contractId) { form.value.contractId = order.contractId; form.value.contractNo = order.contractNo }
  })
}
function handleAdd() { reset(); open.value = true; title.value = '添加发票结算' }
function handleUpdate(row) { reset(); getInvoice(row.invoiceId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改发票结算' }) }
function handleView(row) { getInvoice(row.invoiceId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function calcTax() { const total = form.value.totalAmount || 0; const rate = form.value.taxRate || 0; form.value.taxAmount = parseFloat((total * rate / 100).toFixed(2)); form.value.invoiceAmount = parseFloat((total - form.value.taxAmount).toFixed(2)); form.value.payAmount = total }
function submitForm() { proxy.$refs['invoiceRef'].validate(valid => { if (valid) { if (form.value.invoiceId != undefined) { updateInvoice(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addInvoice(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const invoiceIds = row.invoiceId || ids.value; proxy.$modal.confirm('确认删除编号为"' + invoiceIds + '"的数据？').then(() => delInvoice(invoiceIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('pms/invoice/export', { ...queryParams.value }, `invoice_${new Date().getTime()}.xlsx`) }
function handleAudit(row) { getInvoice(row.invoiceId).then(res => { auditData.value = res.data; auditForm.value = { invoiceId: row.invoiceId, auditOpinion: '' }; auditOpen.value = true }) }
function submitAudit(passed) { proxy.$refs['auditRef'].validate(valid => { if (valid) { const status = passed ? '2' : '5'; const actionText = passed ? '通过' : '驳回'; proxy.$modal.confirm(`确认${actionText}该发票结算？`).then(() => { return auditInvoice(auditForm.value.invoiceId, status, auditForm.value.auditOpinion) }).then(() => { proxy.$modal.msgSuccess(`${actionText}成功`); auditOpen.value = false; getList() }).catch(() => {}) } }) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交该发票结算审核？').then(() => submitInvoice(row.invoiceId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handlePay(row) {
  getInvoice(row.invoiceId).then(res => {
    payData.value = res.data
    const total = Number(res.data.totalAmount) || 0
    const paid = Number(res.data.paymentAmount) || 0
    const unpaid = parseFloat((total - paid).toFixed(2))
    payForm.value = {
      invoiceId: row.invoiceId,
      paymentAmount: unpaid > 0 ? unpaid : 0,
      paymentDate: new Date().toISOString().slice(0, 10),
      paymentMethod: res.data.paymentMethod || '0',
      bankName: res.data.bankName || undefined,
      bankAccount: res.data.bankAccount || undefined,
      payer: undefined,
      attachment: undefined,
      remark: undefined
    }
    payOpen.value = true
  })
}
function submitPay() {
  proxy.$refs['payRef'].validate(valid => {
    if (!valid) return
    if (payForm.value.paymentAmount > payUnpaid.value) {
      proxy.$modal.msgError('付款金额不能超过未付金额')
      return
    }
    proxy.$modal.confirm(`确认本次付款 ${formatMoney(payForm.value.paymentAmount)} 元？`).then(() => {
      return payInvoice(payForm.value)
    }).then(() => {
      proxy.$modal.msgSuccess('付款成功')
      payOpen.value = false
      getList()
    }).catch(() => {})
  })
}
/** 三方匹配 */
function handleMatch(row) {
  threeWayMatch(row.invoiceId).then(res => {
    matchData.value = res.data
    matchOpen.value = true
  })
}
/** PDF发票识别 */
function handleBeforeRecognize(file) {
  const fileName = file.name.split('.')
  const fileExt = fileName[fileName.length - 1].toLowerCase()
  if (fileExt !== 'pdf') { proxy.$modal.msgError('仅支持PDF格式发票文件!'); return false }
  if (file.size / 1024 / 1024 > 20) { proxy.$modal.msgError('上传文件大小不能超过 20 MB!'); return false }
  return true
}
function handleFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!handleBeforeRecognize(file)) { e.target.value = ''; return }
  handleRecognizeUpload(file)
  e.target.value = ''
}
function handleRecognizeUpload(file) {
  const formData = new FormData()
  formData.append('file', file)
  recognizing.value = true
  recognizedFields.value = []
  recognizeInvoice(formData).then(res => {
    const data = res.data
    if (data) {
      if (data.invoiceNumber) form.value.invoiceNumber = data.invoiceNumber
      if (data.invoiceType) form.value.invoiceType = data.invoiceType
      if (data.invoiceDate) form.value.invoiceDate = data.invoiceDate
      if (data.invoiceAmount != null) form.value.invoiceAmount = Number(data.invoiceAmount)
      if (data.taxAmount != null) form.value.taxAmount = Number(data.taxAmount)
      if (data.totalAmount != null) form.value.totalAmount = Number(data.totalAmount)
      if (data.taxRate != null) form.value.taxRate = Number(data.taxRate)
      if (data.invoiceTitle) form.value.invoiceTitle = data.invoiceTitle
      if (data.taxNumber) form.value.taxNumber = data.taxNumber
      if (data.totalAmount != null && data.taxRate != null) { calcTax() }
      uploadAttachment(file)
      const fieldLabels = { invoiceNumber: '发票号码', invoiceType: '发票类型', invoiceDate: '开票日期', taxAmount: '税额', taxRate: '税率(%)', totalAmount: '开票金额', invoiceTitle: '发票抬头', taxNumber: '纳税人识别号' }
      recognizedFields.value = Object.keys(fieldLabels).filter(k => data[k] != null && data[k] !== '').map(k => ({ label: fieldLabels[k], value: data[k] }))
      if (recognizedFields.value.length > 0) { proxy.$modal.msgSuccess('识别成功，已回填' + recognizedFields.value.length + '项信息') } else { proxy.$modal.msgWarning('未识别到发票信息，请检查PDF文件是否为电子发票') }
    } else { proxy.$modal.msgWarning('未识别到发票信息') }
  }).catch(() => {}).finally(() => { recognizing.value = false })
}
function uploadAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  request({ url: '/common/upload', method: 'post', data: formData, timeout: 60000, headers: { 'repeatSubmit': false, 'Content-Type': false } }).then(res => { if (res.fileName) { form.value.invoiceAttachment = res.fileName } }).catch(() => {})
}
function getFileName(name) { if (name && name.lastIndexOf('/') > -1) { return name.slice(name.lastIndexOf('/') + 1) } return name || '' }
function cancel() { open.value = false; reset() }

getList()
loadSupplierOptions()
loadContractOptions()
loadOrderOptions()
onActivated(() => { getList() })
</script>

<style scoped>
/* 三方匹配差异值样式 */
.rd-diff { font-variant-numeric: tabular-nums; font-weight: 700; }
.rd-diff--ok { color: #10b981; }
.rd-diff--err { color: #dc2626; }

.recognize-area { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #f0f9eb; border-radius: 8px; border: 1px dashed #67c23a; }
.recognize-tip { color: #909399; font-size: 13px; }
.recognized-info { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px; }
.recognized-tag { margin: 0; }
.reject-alert { margin-bottom: 16px; }
</style>
