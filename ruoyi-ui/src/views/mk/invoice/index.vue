<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="发票编号" prop="invoiceNo">
        <el-input v-model="queryParams.invoiceNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="发票类型" prop="invoiceType">
        <el-select v-model="queryParams.invoiceType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_invoice_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="发票状态" prop="invoiceStatus">
        <el-select v-model="queryParams.invoiceStatus" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in marketing_invoice_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:invoice:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:invoice:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['marketing:invoice:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="发票编号" prop="invoiceNo" :width="colWidth('invoiceNo', 150)" resizable />
      <el-table-column label="客户名称" prop="customerName" :width="colWidth('customerName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="发票类型" prop="invoiceType" :width="colWidth('invoiceType', 130)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_invoice_type" :value="scope.row.invoiceType" /></template>
      </el-table-column>
      <el-table-column label="开票金额" prop="invoiceAmount" :width="colWidth('invoiceAmount', 120)" resizable align="right" />
      <el-table-column label="税额" prop="taxAmount" :width="colWidth('taxAmount', 100)" resizable align="right" />
      <el-table-column label="发票抬头" prop="invoiceTitle" :width="colWidth('invoiceTitle', 200)" resizable show-overflow-tooltip />
      <el-table-column label="开票日期" prop="invoiceDate" :width="colWidth('invoiceDate', 120)" resizable />
      <el-table-column label="状态" prop="invoiceStatus" :width="colWidth('invoiceStatus', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="marketing_invoice_status" :value="scope.row.invoiceStatus" /></template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button v-if="scope.row.invoiceStatus === '1'" link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-hasPermi="['marketing:invoice:void']">作废</el-button>
          <el-button v-if="scope.row.invoiceStatus === '1'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:invoice:edit']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1000px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.invoiceNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ form.invoiceNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="invoiceRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <!-- 发票识别 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('recognize')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 4V2"/>
                    <path d="M15 16v-2"/>
                    <path d="M8 16v-2"/>
                    <path d="M8 4V2"/>
                    <path d="M3 10h18"/>
                    <path d="M5 6h14"/>
                    <path d="M5 18h14"/>
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
                <el-upload ref="recognizeUploadRef" :auto-upload="true" :show-file-list="false" accept=".pdf" :before-upload="handleBeforeRecognize" :http-request="handleRecognizeUpload" :disabled="recognizing">
                  <el-button type="success" icon="MagicStick" :loading="recognizing">{{ recognizing ? '识别中...' : '上传PDF发票识别' }}</el-button>
                </el-upload>
                <span class="recognize-tip">仅支持PDF格式电子发票，上传后自动识别并回填发票信息</span>
              </div>
              <div v-if="recognizedFields.length" class="recognized-info">
                <el-tag v-for="f in recognizedFields" :key="f.label" type="success" class="recognized-tag">{{ f.label }}：{{ f.value }}</el-tag>
              </div>
            </div>
          </section>

          <!-- 发票基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                  </svg>
                </span>
                发票基本信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row>
                <el-col :span="12"><el-form-item label="发票编号" prop="invoiceNo"><el-input v-model="form.invoiceNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发票类型" prop="invoiceType">
                  <el-select v-model="form.invoiceType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in marketing_invoice_type" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开票金额" prop="invoiceAmount"><el-input-number v-model="form.invoiceAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="税率(%)" prop="taxRate"><el-input-number v-model="form.taxRate" :min="0" :precision="2" controls-position="right" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="税额" prop="taxAmount"><el-input-number v-model="form.taxAmount" :min="0" :precision="2" controls-position="right" style="width: 100%" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="开票日期" prop="invoiceDate"><el-date-picker v-model="form.invoiceDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 关联信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('relation')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                    <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                  </svg>
                </span>
                关联信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.relation }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.relation">
              <el-row>
                <el-col :span="12"><el-form-item label="关联客户" prop="customerId">
                  <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width: 100%" @change="onCustomerChange">
                    <el-option v-for="c in customerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联订单" prop="orderId">
                  <el-select v-model="form.orderId" filterable clearable placeholder="可选" style="width: 100%" @change="onOrderChange">
                    <el-option v-for="o in orderOptions" :key="o.orderId" :label="o.orderNo + ' - ' + o.customerName" :value="o.orderId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联合同" prop="contractId">
                  <el-select v-model="form.contractId" filterable clearable placeholder="可选" style="width: 100%">
                    <el-option v-for="c in contractOptions" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 抬头与税号信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('title')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                  </svg>
                </span>
                抬头与税号信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.title }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.title">
              <el-row>
                <el-col :span="12"><el-form-item label="发票抬头" prop="invoiceTitle"><el-input v-model="form.invoiceTitle" placeholder="请输入发票抬头" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="纳税人识别号" prop="taxNo"><el-input v-model="form.taxNo" placeholder="请输入纳税人识别号" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>

          <!-- 附件与备注 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('attachment')">
              <div class="rd-card-title">
                <span class="rd-card-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                  </svg>
                </span>
                附件与备注
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.attachment }" aria-label="折叠">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
              </button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.attachment">
              <el-form-item label="发票附件" prop="invoiceAttachment">
                <file-upload v-model="form.invoiceAttachment" :fileSize="50" :fileType="['pdf']" />
              </el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情 -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
          </div>
          <span class="rd-detail-header-title">发票详情</span>
          <div class="rd-detail-header-sub" v-if="viewForm.invoiceNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewForm.invoiceNo }}</span>
            <dict-tag :options="marketing_invoice_status" :value="viewForm.invoiceStatus" />
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 发票基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
              </span>
              发票基本信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发票编号</span><div class="rd-value">{{ viewForm.invoiceNo }}</div></div>
              <div class="rd-item"><span class="rd-label">发票类型</span><div class="rd-value"><dict-tag :options="marketing_invoice_type" :value="viewForm.invoiceType" /></div></div>
              <div class="rd-item"><span class="rd-label">开票金额</span><div class="rd-value rd-value--large rd-amount">￥{{ formatAmount(viewForm.invoiceAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">税率(%)</span><div class="rd-value">{{ viewForm.taxRate }}</div></div>
              <div class="rd-item"><span class="rd-label">税额</span><div class="rd-value rd-amount">￥{{ formatAmount(viewForm.taxAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">开票日期</span><div class="rd-value">{{ viewForm.invoiceDate }}</div></div>
              <div class="rd-item"><span class="rd-label">发票状态</span><div class="rd-value"><dict-tag :options="marketing_invoice_status" :value="viewForm.invoiceStatus" /></div></div>
            </div>
          </div>
        </section>

        <!-- 关联信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('relation')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
              </span>
              关联信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.relation }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.relation">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewForm.customerName }}</div></div>
              <div class="rd-item"><span class="rd-label">订单编号</span><div class="rd-value">{{ viewForm.orderNo }}</div></div>
              <div class="rd-item"><span class="rd-label">合同编号</span><div class="rd-value">{{ viewForm.contractNo }}</div></div>
            </div>
          </div>
        </section>

        <!-- 抬头与税号信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('title')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                </svg>
              </span>
              抬头与税号信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.title }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.title">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发票抬头</span><div class="rd-value">{{ viewForm.invoiceTitle }}</div></div>
              <div class="rd-item"><span class="rd-label">纳税人识别号</span><div class="rd-value">{{ viewForm.taxNo }}</div></div>
            </div>
          </div>
        </section>

        <!-- 附件与备注 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('attachment')">
            <div class="rd-card-title">
              <span class="rd-card-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                </svg>
              </span>
              附件与备注
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.attachment }" aria-label="折叠">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg>
            </button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.attachment">
            <div class="rd-grid">
              <div class="rd-item rd-item--full">
                <span class="rd-label">发票附件</span>
                <div class="rd-value">
                  <template v-if="viewForm.invoiceAttachment">
                    <div v-for="(file, idx) in viewForm.invoiceAttachment.split(',')" :key="idx">
                      <el-link :href="baseUrl + file" :underline="false" target="_blank" type="primary">
                        <el-icon><Download /></el-icon> {{ getFileName(file) }}
                      </el-link>
                    </div>
                  </template>
                  <span v-else class="rd-value--muted">暂无附件</span>
                </div>
              </div>
              <div class="rd-item rd-item--full">
                <span class="rd-label">备注</span>
                <div class="rd-value" :class="{ 'rd-value--muted': !viewForm.remark }">{{ viewForm.remark || '暂无备注' }}</div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="MkInvoice">
import { listInvoice, getInvoice, addInvoice, updateInvoice, delInvoice, voidInvoice, recognizeInvoice } from '@/api/mk/invoice'
import { listCustomer } from '@/api/mk/customer'
import { listContract } from '@/api/mk/contract'
import { listOrder } from '@/api/mk/order'
import { useColumnResize } from '@/composables/useColumnResize'
import request from '@/utils/request'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_invoice_index')
const { marketing_invoice_type, marketing_invoice_status } = proxy.useDict('marketing_invoice_type', 'marketing_invoice_status')

const baseUrl = import.meta.env.VITE_APP_BASE_API
const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewForm = ref({})
const customerOptions = ref([])
const contractOptions = ref([])
const orderOptions = ref([])
const recognizing = ref(false)
const recognizedFields = ref([])
const collapsedCards = reactive({ recognize: false, basic: false, relation: false, title: false, attachment: false })
function toggleCard(name) { collapsedCards[name] = !collapsedCards[name] }
function formatAmount(val) { if (val == null || val === '') return '-'; return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, invoiceNo: undefined, customerName: undefined, invoiceType: undefined, invoiceStatus: undefined },
  rules: {
    customerId: [{ required: true, message: '请选择关联客户', trigger: 'change' }],
    invoiceAmount: [{ required: true, message: '开票金额不能为空', trigger: 'blur' }],
    invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
    invoiceTitle: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listInvoice(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function getCustomerOptions() { listCustomer({ pageNum: 1, pageSize: 9999 }).then(res => { customerOptions.value = res.rows }) }
function getContractOptions() { listContract({ pageNum: 1, pageSize: 9999 }).then(res => { contractOptions.value = res.rows }) }
function getOrderOptions() { listOrder({ pageNum: 1, pageSize: 9999 }).then(res => { orderOptions.value = res.rows }) }
function onCustomerChange(customerId) { if (customerId) { const customer = customerOptions.value.find(c => c.customerId === customerId); if (customer) { form.value.customerName = customer.customerName; form.value.invoiceTitle = customer.customerName } } }
function onOrderChange(orderId) { if (orderId) { const order = orderOptions.value.find(o => o.orderId === orderId); if (order) { form.value.orderNo = order.orderNo; form.value.customerId = order.customerId; form.value.customerName = order.customerName; form.value.invoiceAmount = order.orderAmount; form.value.invoiceTitle = order.customerName } } }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.invoiceId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { invoiceNo: undefined, orderId: undefined, contractId: undefined, customerId: undefined, customerName: undefined, invoiceType: '0', invoiceStatus: '1', invoiceAmount: 0, taxRate: 13.00, taxAmount: 0, invoiceTitle: undefined, taxNo: undefined, invoiceDate: undefined, invoiceAttachment: undefined, remark: undefined }; proxy.resetForm('invoiceRef'); recognizedFields.value = []; collapsedCards.recognize = false; collapsedCards.basic = false; collapsedCards.relation = false; collapsedCards.title = false; collapsedCards.attachment = false }
function handleAdd() { reset(); open.value = true; title.value = '新增发票' }
function handleUpdate(row) { reset(); collapsedCards.recognize = true; getInvoice(row.invoiceId).then(res => { form.value = res.data; open.value = true; title.value = '修改发票' }) }
function handleView(row) { getInvoice(row.invoiceId).then(res => { viewForm.value = res.data; collapsedCards.basic = false; collapsedCards.relation = false; collapsedCards.title = false; collapsedCards.attachment = false; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['invoiceRef'].validate(valid => {
    if (valid) {
      if (form.value.invoiceAmount != null && form.value.taxRate != null) {
        form.value.taxAmount = Number((form.value.invoiceAmount * form.value.taxRate / 100).toFixed(2))
      }
      if (form.value.invoiceId != undefined) {
        updateInvoice(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addInvoice(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function handleDelete(row) { const invoiceIds = row.invoiceId || ids.value; proxy.$modal.confirm('确认删除选中的发票？').then(() => delInvoice(invoiceIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleVoid(row) { proxy.$modal.confirm('确认作废发票【' + row.invoiceNo + '】？').then(() => voidInvoice(row.invoiceId)).then(() => { getList(); proxy.$modal.msgSuccess('发票已作废') }).catch(() => {}) }
function handleExport() { proxy.download('mk/invoice/export', { ...queryParams.value }, `invoice_${new Date().getTime()}.xlsx`) }

function handleBeforeRecognize(file) {
  const fileName = file.name.split('.')
  const fileExt = fileName[fileName.length - 1].toLowerCase()
  if (fileExt !== 'pdf') { proxy.$modal.msgError('仅支持PDF格式发票文件!'); return false }
  if (file.size / 1024 / 1024 > 20) { proxy.$modal.msgError('上传文件大小不能超过 20 MB!'); return false }
  return true
}

function handleRecognizeUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  recognizing.value = true
  recognizedFields.value = []
  recognizeInvoice(formData).then(res => {
    const data = res.data
    if (data) {
      if (data.invoiceNo) form.value.invoiceNo = data.invoiceNo
      if (data.invoiceType) form.value.invoiceType = data.invoiceType
      if (data.invoiceDate) form.value.invoiceDate = data.invoiceDate
      if (data.invoiceAmount != null) form.value.invoiceAmount = Number(data.invoiceAmount)
      if (data.taxAmount != null) form.value.taxAmount = Number(data.taxAmount)
      if (data.taxRate != null) form.value.taxRate = Number(data.taxRate)
      if (data.invoiceTitle) form.value.invoiceTitle = data.invoiceTitle
      if (data.taxNo) form.value.taxNo = data.taxNo
      const fieldLabels = { invoiceNo: '发票号码', invoiceType: '发票类型', invoiceDate: '开票日期', invoiceAmount: '金额', taxAmount: '税额', taxRate: '税率(%)', invoiceTitle: '发票抬头', taxNo: '纳税人识别号' }
      recognizedFields.value = Object.keys(fieldLabels).filter(k => data[k] != null && data[k] !== '').map(k => ({ label: fieldLabels[k], value: data[k] }))
      if (recognizedFields.value.length > 0) { proxy.$modal.msgSuccess('识别成功，已回填' + recognizedFields.value.length + '项信息') } else { proxy.$modal.msgWarning('未识别到发票信息，请检查PDF文件是否为电子发票') }
      uploadAttachment(options.file)
    } else { proxy.$modal.msgWarning('未识别到发票信息') }
  }).catch(() => {}).finally(() => { recognizing.value = false })
}

function uploadAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  request({ url: '/common/upload', method: 'post', data: formData, timeout: 60000, headers: { 'repeatSubmit': false, 'Content-Type': false } }).then(res => { if (res.fileName) { form.value.invoiceAttachment = res.fileName } }).catch(() => {})
}

function getFileName(name) { if (name.lastIndexOf('/') > -1) { return name.slice(name.lastIndexOf('/') + 1) } return name }

getCustomerOptions(); getContractOptions(); getOrderOptions(); getList()
</script>

<style scoped>
/* 卡片式样式使用全局 detail-page.scss，此处仅保留页面特有样式 */

/* 发票识别区域 */
.recognize-area { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #f0f9eb; border-radius: 8px; border: 1px dashed #67c23a; }
.recognize-tip { color: #909399; font-size: 13px; }
.recognized-info { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px; }
.recognized-tag { margin: 0; }
</style>
