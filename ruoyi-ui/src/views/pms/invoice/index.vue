<template>
  <div class="app-container pms-invoice-page">
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
          <label>结算单号</label>
          <div class="control">
            <el-input v-model="queryParams.invoiceNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_invoice_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>发票类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.invoiceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_invoice_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发票号码</label>
          <div class="control">
            <el-input v-model="queryParams.invoiceNumber" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Document /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>开票日期</label>
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
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:invoice:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:invoice:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:invoice:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:invoice:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_invoice_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="结算单号" prop="invoiceNo" key="invoiceNo" :width="colWidth('invoiceNo', 180)" resizable sortable="custom" v-if="columns.invoiceNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.invoiceNo }}</span></template>
          </el-table-column>
          <el-table-column label="合同编号" prop="contractNo" key="contractNo" :width="colWidth('contractNo', 140)" resizable v-if="columns.contractNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.contractNo }}</span></template>
          </el-table-column>
          <el-table-column label="采购单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 180)" resizable v-if="columns.orderNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.orderNo }}</span></template>
          </el-table-column>
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="发票类型" prop="invoiceType" key="invoiceType" :width="colWidth('invoiceType', 130)" resizable align="center" v-if="columns.invoiceType.visible">
            <template #default="scope">
              <span class="badge violet">{{ invoiceTypeLabel(scope.row.invoiceType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="发票号码" prop="invoiceNumber" key="invoiceNumber" :width="colWidth('invoiceNumber', 140)" resizable v-if="columns.invoiceNumber.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.invoiceNumber }}</span></template>
          </el-table-column>
          <el-table-column label="开票日期" prop="invoiceDate" key="invoiceDate" :width="colWidth('invoiceDate', 130)" resizable align="center" sortable="custom" v-if="columns.invoiceDate.visible" />
          <el-table-column label="不含税金额" prop="invoiceAmount" key="invoiceAmount" :width="colWidth('invoiceAmount', 130)" resizable align="right" class-name="col-num" v-if="columns.invoiceAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.invoiceAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="税额" prop="taxAmount" key="taxAmount" :width="colWidth('taxAmount', 120)" resizable align="right" class-name="col-num" v-if="columns.taxAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.taxAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="开票金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom" class-name="col-num" v-if="columns.totalAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="已付金额" prop="paymentAmount" key="paymentAmount" :width="colWidth('paymentAmount', 130)" resizable align="right" class-name="col-num" v-if="columns.paymentAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.paymentAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="付款日期" prop="paymentDate" key="paymentDate" :width="colWidth('paymentDate', 130)" resizable align="center" v-if="columns.paymentDate.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="290" align="center" fixed="right">
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
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="invoiceRef" :model="form" :rules="rules" label-width="132px">
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
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4" style="display:block">
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

    <el-dialog v-model="viewOpen" width="1032px" append-to-body draggable class="rd-dialog">
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
                <el-table-column label="付款凭证" prop="attachment" width="130" align="center"><template #default="scope"><div v-if="scope.row.attachment" style="display:flex;gap:4px;justify-content:center"><el-link type="primary" :href="baseUrl + scope.row.attachment" target="_blank" :underline="false">下载</el-link><el-link type="success" :underline="false" @click="handlePreview(scope.row.attachment)">预览</el-link></div><span v-else class="rd-value--muted">-</span></template></el-table-column>
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
              <div class="rd-item rd-item--full"><span class="rd-label">附件</span><div class="rd-value"><template v-if="viewData.invoiceAttachment"><el-link type="primary" :href="baseUrl + viewData.invoiceAttachment" target="_blank">{{ getFileName(viewData.invoiceAttachment) }}</el-link><el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(viewData.invoiceAttachment)">预览</el-button></template><span v-else class="rd-value--muted">暂无附件</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 审核记录 -->
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('v6')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v6 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v6" style="display:block">
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
    <el-dialog v-model="auditOpen" width="1152px" append-to-body draggable class="rd-dialog">
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
              <div class="rd-item rd-item--full"><span class="rd-label">发票文件</span><div class="rd-value"><template v-if="auditData.invoiceAttachment"><el-link type="primary" :href="baseUrl + auditData.invoiceAttachment" target="_blank" :underline="false"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: -2px; margin-right: 4px"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>{{ getFileName(auditData.invoiceAttachment) }}</el-link><el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(auditData.invoiceAttachment)">预览</el-button></template><span v-else class="rd-value--muted">暂无附件</span></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a2" style="display:block">
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
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="120px">
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
    <el-dialog v-model="payOpen" width="1080px" append-to-body draggable class="rd-dialog">
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
            <el-form ref="payRef" :model="payForm" :rules="payRules" label-width="132px">
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
              <el-table-column label="付款凭证" prop="attachment" width="130" align="center"><template #default="scope"><div v-if="scope.row.attachment" style="display:flex;gap:4px;justify-content:center"><el-link type="primary" :href="baseUrl + scope.row.attachment" target="_blank" :underline="false">下载</el-link><el-link type="success" :underline="false" @click="handlePreview(scope.row.attachment)">预览</el-link></div><span v-else class="rd-value--muted">-</span></template></el-table-column>
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
    <el-dialog v-model="matchOpen" width="1032px" append-to-body draggable class="rd-dialog">
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

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="发票结算业务状态流转说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已付款</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">审批驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="info">修改后重新提交</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="primary">部分付款</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="success">已付款</el-tag>
            <el-tag size="small" type="info">支持多次付款</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建发票结算单后的初始状态，可编辑、删除、提交审批
          </el-descriptions-item>
          <el-descriptions-item label="待审批">
            提交后等待审批，可审批通过或驳回
          </el-descriptions-item>
          <el-descriptions-item label="已审批">
            审批通过，可进行付款操作
          </el-descriptions-item>
          <el-descriptions-item label="部分付款">
            已审批且已部分付款，可继续付款
          </el-descriptions-item>
          <el-descriptions-item label="已付款">
            付款完成（累计付款金额等于开票金额），流程结束
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批未通过，可修改后重新提交
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><CircleCheck /></el-icon>三方匹配
              </div>
              <div class="highlight-card-body">
                发票结算支持<strong>三方匹配</strong>功能，系统自动比对发票信息、采购订单和收货单数据，确保金额、数量一致。点击"三方匹配"按钮可查看匹配结果，差异项会以红色标注
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><QuestionFilled /></el-icon>发票识别
              </div>
              <div class="highlight-card-body">
                上传发票图片后，系统<strong>自动识别</strong>发票号码、开票日期、金额、税额等关键字段，减少手工录入。识别结果可在此基础上修改确认
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 16px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><CircleClose /></el-icon>付款规则
              </div>
              <div class="highlight-card-body">
                已审批的发票可进行付款，<strong>支持多次付款</strong>。每次付款金额不能超过未付金额（开票金额 - 已付金额）。累计付款金额等于开票金额时，自动变为已付款状态
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">
                <el-icon style="margin-right: 4px;"><CircleClose /></el-icon>审批驳回
              </div>
              <div class="highlight-card-body">
                审批驳回后，发票回到草稿状态，可修改发票信息后重新提交审批。驳回时需填写审批意见
              </div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建发票：</strong>选择合同和采购订单（可选），上传发票图片自动识别或手工填写发票信息，包括发票号码、开票日期、金额、税额等
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>三方匹配：</strong>点击"三方匹配"按钮，系统自动比对发票、采购订单和收货单的数据，展示匹配结果和差异项
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审批：</strong>确认发票信息无误后，点击"提交"按钮，发票进入待审批状态
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审批通过：</strong>审批人审核发票信息，通过后发票进入已审批状态，可进行付款操作
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审批驳回：</strong>审批不通过时，需填写审批意见，发票回到草稿状态，可修改后重新提交
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>付款操作：</strong>已审批的发票可点击"付款"按钮进行付款，填写付款金额、付款日期、付款方式等信息。支持多次付款，累计付款等于开票金额时自动变为已付款状态
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
    <!-- 文件预览 -->
    <file-preview ref="filePreviewRef" />
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
import { ArrowDown, Search, OfficeBuilding, Document, Filter, Edit, Delete, Download, WarningFilled, ArrowRight, CircleCheck, CircleClose, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { pms_invoice_status, pms_invoice_type, wms_payment_method } = proxy.useDict('pms_invoice_status', 'pms_invoice_type', 'wms_payment_method')
const baseUrl = import.meta.env.VITE_APP_BASE_API

const { collapsedCards, toggleCard } = useDetailCard(["recognize","c1","c2","c3","c0","c4","v1","v3","v4","v5","v6","a1","a2","p1","m1","m2","m3","m4"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_invoice_index')

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
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '5': 0, '6': 0 })

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  invoiceNo: { label: '结算单号', visible: true },
  contractNo: { label: '合同编号', visible: true },
  orderNo: { label: '采购单号', visible: true },
  supplierName: { label: '供应商', visible: true },
  status: { label: '状态', visible: true },
  invoiceType: { label: '发票类型', visible: true },
  invoiceNumber: { label: '发票号码', visible: true },
  invoiceDate: { label: '开票日期', visible: true },
  invoiceAmount: { label: '不含税金额', visible: true },
  taxAmount: { label: '税额', visible: true },
  totalAmount: { label: '开票金额', visible: true },
  paymentAmount: { label: '已付金额', visible: true },
  paymentDate: { label: '付款日期', visible: true },
  createTime: { label: '创建时间', visible: true }
}

// 从 localStorage 读取保存的列显隐配置
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_invoice_columns')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, invoiceNo: undefined, supplierName: undefined, status: undefined, invoiceType: undefined, invoiceNumber: undefined, params: {} },
  rules: {}
})
const { queryParams, form, rules } = toRefs(data)

const statusTabList = computed(() => pms_invoice_status.value)
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.invoiceNo) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.invoiceType) count++
  if (queryParams.value.invoiceNumber) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() { loading.value = true; listInvoice(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value.invoiceNo = undefined; queryParams.value.supplierName = undefined; queryParams.value.status = undefined; queryParams.value.invoiceType = undefined; queryParams.value.invoiceNumber = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery()
}
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
function handleExport() { proxy.download('pms/invoice/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `invoice_${new Date().getTime()}.xlsx`) }
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
function handlePreview(fileUrl) { proxy.$refs.filePreviewRef.open(fileUrl, getFileName(fileUrl)) }
function cancel() { open.value = false; reset() }
function loadStatusCounts() {
  listInvoice({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '5': 0, '6': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'green', '5': 'red', '6': 'violet' }; return map[status] || 'gray' }
function statusLabel(status) { const item = pms_invoice_status.value.find(d => d.value == status); return item ? item.label : '-' }
function invoiceTypeLabel(type) { const item = pms_invoice_type.value.find(d => d.value == type); return item ? item.label : '-' }
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-done', '5': 'tab-reject', '6': 'tab-partial' }; return map[value] || '' }

getList()
loadSupplierOptions()
loadContractOptions()
loadOrderOptions()
onActivated(() => { getList() })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-invoice-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff; --violet-500:#8b5cf6;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface Card ===== */
.pms-invoice-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.pms-invoice-page .filter-card { padding:14px 20px 16px; }
.pms-invoice-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-invoice-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-invoice-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-invoice-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-invoice-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-invoice-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-invoice-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-invoice-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-invoice-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-invoice-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-invoice-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Form Field ===== */
.pms-invoice-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-invoice-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-invoice-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-invoice-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }

/* el-input transparent inside .control */
.pms-invoice-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-invoice-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-invoice-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-invoice-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-invoice-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }

/* el-select transparent inside .control */
.pms-invoice-page .field .control :deep(.el-select) { width:100%; }
.pms-invoice-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-invoice-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-invoice-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* el-date-picker transparent inside .control */
.pms-invoice-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-invoice-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-invoice-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-invoice-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }

/* ===== Status Tabs ===== */
.pms-invoice-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-invoice-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-invoice-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-invoice-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-invoice-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-invoice-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-invoice-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-invoice-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-invoice-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-invoice-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-invoice-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-invoice-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-invoice-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-invoice-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-invoice-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-invoice-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-invoice-page .status-tab.tab-approved .dot, .pms-invoice-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-invoice-page .status-tab.tab-approved .count, .pms-invoice-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-invoice-page .status-tab.is-active.tab-approved .count, .pms-invoice-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-invoice-page .status-tab.tab-reject .dot { background:var(--red-500); }
.pms-invoice-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.pms-invoice-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.pms-invoice-page .status-tab.tab-partial .dot { background:var(--violet-500); }
.pms-invoice-page .status-tab.tab-partial .count { background:var(--violet-50); color:#7c3aed; }
.pms-invoice-page .status-tab.is-active.tab-partial .count { background:var(--violet-500); color:#fff; }

/* ===== Tip Pill ===== */
.pms-invoice-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.pms-invoice-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.pms-invoice-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }

/* ===== Toolbar ===== */
.pms-invoice-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-invoice-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-invoice-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-invoice-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }

/* ===== Buttons ===== */
.pms-invoice-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-invoice-page .btn-soft .el-icon { font-size:14px; }
.pms-invoice-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-invoice-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-invoice-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-invoice-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-invoice-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-invoice-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-invoice-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.pms-invoice-page .table-wrap { overflow-x:auto; }
.pms-invoice-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-invoice-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-invoice-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-invoice-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-invoice-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-invoice-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-invoice-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-invoice-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-invoice-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-invoice-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-invoice-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pms-invoice-page .app-table :deep(.col-num) { text-align:right; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.pms-invoice-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }

/* ===== Badges ===== */
.pms-invoice-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-invoice-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-invoice-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-invoice-page .badge.amber .dot { background:var(--amber-500); }
.pms-invoice-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-invoice-page .badge.blue .dot { background:var(--blue-500); }
.pms-invoice-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-invoice-page .badge.green .dot { background:var(--green-500); }
.pms-invoice-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pms-invoice-page .badge.red .dot { background:var(--red-500); }
.pms-invoice-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-invoice-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-invoice-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.pms-invoice-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-invoice-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-invoice-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-invoice-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-invoice-page .pagination-container :deep(.el-pagination .btn-prev), .pms-invoice-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-invoice-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-invoice-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-invoice-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .pms-invoice-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-invoice-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-invoice-page .toolbar { flex-wrap:wrap; gap:10px; } .pms-invoice-page .status-tabs { padding:6px 8px; } }

/* ===== 三方匹配差异值样式 ===== */
.rd-diff { font-variant-numeric: tabular-nums; font-weight: 700; }
.rd-diff--ok { color: #10b981; }
.rd-diff--err { color: #dc2626; }

.recognize-area { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #f0f9eb; border-radius: 8px; border: 1px dashed #67c23a; }
.recognize-tip { color: #909399; font-size: 13px; }
.recognized-info { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px; }
.recognized-tag { margin: 0; }
.reject-alert { margin-bottom: 16px; }

/* ===== Status Help Dialog ===== */
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
.flow-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.flow-arrow {
  color: #909399;
  font-size: 16px;
}
.highlight-card {
  border-radius: 8px;
  padding: 16px;
  border: 1px solid;
}
.highlight-success {
  background-color: #f0f9ff;
  border-color: #b3e19d;
}
.highlight-danger {
  background-color: #fef0f0;
  border-color: #fbc4c4;
}
.highlight-info {
  background-color: #f4f4f5;
  border-color: #d3d4d6;
}
.highlight-warning {
  background-color: #fdf6ec;
  border-color: #f5dab1;
}
.highlight-card-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}
.highlight-success .highlight-card-title {
  color: #67c23a;
}
.highlight-danger .highlight-card-title {
  color: #f56c6c;
}
.highlight-info .highlight-card-title {
  color: #909399;
}
.highlight-warning .highlight-card-title {
  color: #e6a23c;
}
.highlight-card-body {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
