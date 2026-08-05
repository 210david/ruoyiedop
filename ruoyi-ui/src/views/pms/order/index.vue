<template>
  <div class="app-container pms-order-page">
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
          <label>采购单号</label>
          <div class="control">
            <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
              <el-option v-for="d in pms_order_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>关联合同</label>
          <div class="control">
            <el-input v-model="queryParams.contractNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>关联申请</label>
          <div class="control">
            <el-input v-model="queryParams.requestNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>付款方式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.paymentMethod" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_payment_method" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>采购员</label>
          <div class="control">
            <el-input v-model="queryParams.buyerName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>结算方式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.settlementType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_settlement_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>预计到货日期</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:order:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:order:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:order:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:order:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_order_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="采购单号" prop="orderNo" key="orderNo" :width="colWidth('orderNo', 180)" resizable sortable="custom" v-if="columns.orderNo.visible" />
          <el-table-column label="关联合同" prop="contractNo" key="contractNo" :width="colWidth('contractNo', 140)" resizable v-if="columns.contractNo.visible" />
          <el-table-column label="关联申请单号" prop="requestNo" key="requestNo" :width="colWidth('requestNo', 140)" resizable v-if="columns.requestNo.visible" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template>
          </el-table-column>
          <el-table-column label="预计到货日期" prop="orderDate" key="orderDate" :width="colWidth('orderDate', 130)" resizable align="center" sortable="custom" v-if="columns.orderDate.visible" />
          <el-table-column label="总数量" prop="totalQty" key="totalQty" :width="colWidth('totalQty', 110)" resizable align="right" sortable="custom" v-if="columns.totalQty.visible" />
          <el-table-column label="总金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom" v-if="columns.totalAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="已收货金额" prop="receivedAmount" key="receivedAmount" :width="colWidth('receivedAmount', 130)" resizable align="right" v-if="columns.receivedAmount.visible">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.receivedAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="已付金额" prop="paidAmount" key="paidAmount" :width="colWidth('paidAmount', 130)" resizable align="right" v-if="columns.paidAmount.visible">
            <template #default="scope"><span class="rd-amount" :style="{ color: scope.row.paidAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(scope.row.paidAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="未付金额" prop="orderUnpaidAmount" key="orderUnpaidAmount" :width="colWidth('orderUnpaidAmount', 130)" resizable align="right" v-if="columns.orderUnpaidAmount.visible">
            <template #default="scope"><span class="rd-amount" :style="{ color: (scope.row.totalAmount - scope.row.paidAmount) > 0 ? '#f56c6c' : '#67c23a' }">{{ formatMoney((scope.row.totalAmount || 0) - (scope.row.paidAmount || 0)) }}</span></template>
          </el-table-column>
          <el-table-column label="采购员" prop="buyerName" key="buyerName" :width="colWidth('buyerName', 120)" resizable v-if="columns.buyerName.visible" />
          <el-table-column label="付款方式" prop="paymentMethod" key="paymentMethod" :width="colWidth('paymentMethod', 120)" resizable align="center" v-if="columns.paymentMethod.visible">
            <template #default="scope"><span class="badge blue">{{ paymentMethodLabel(scope.row.paymentMethod) }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="330" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:order:edit']" v-if="scope.row.status === '0' || scope.row.status === '7'">修改</el-button>
              <el-button link type="primary" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['pms:order:edit']" v-if="scope.row.status === '0'">提交</el-button>
              <el-button link type="primary" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:order:approve']" v-if="scope.row.status === '1'">审批</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:order:remove']" v-if="scope.row.status === '0'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <!-- 基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="采购单号" prop="orderNo">
                    <el-input v-model="form.orderNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="预计到货日期" prop="orderDate">
                    <el-date-picker v-model="form.orderDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 关联单据 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></span>关联单据</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item prop="contractId">
                    <template #label>
                      <span>关联合同</span>
                      <el-tooltip content="仅可选择审批通过且已生效的合同" placement="top">
                        <el-icon class="rd-form-tip"><question-filled /></el-icon>
                      </el-tooltip>
                    </template>
                    <el-select v-model="form.contractId" filterable clearable placeholder="请选择合同" style="width: 100%" @change="onContractChange">
                      <template #empty>
                        <div class="rd-select-empty">暂无审批通过且已生效的合同</div>
                      </template>
                      <el-option v-for="c in contractOptions" :key="c.contractId" :label="c.contractNo + ' - ' + c.contractName" :value="c.contractId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="requestId" label-width="130px">
                    <template #label>
                      <span>关联申请单号</span>
                      <el-tooltip content="仅可选择审批通过的采购申请；选择后将自动带出申请物料明细" placement="top">
                        <el-icon class="rd-form-tip"><question-filled /></el-icon>
                      </el-tooltip>
                    </template>
                    <el-select v-model="form.requestId" filterable clearable placeholder="可选，选择后自动带出明细" style="width: 100%" @change="onRequestChange">
                      <template #empty>
                        <div class="rd-select-empty">暂无审批通过的采购申请</div>
                      </template>
                      <el-option v-for="r in requestOptions" :key="r.requestId" :label="r.requestNo" :value="r.requestId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 供应商与采购员 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c5')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>供应商与采购员</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c5">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="供应商" prop="supplierName">
                    <el-input v-model="form.supplierName" placeholder="选择合同后自动带出" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="采购员" prop="buyerName">
                    <el-input v-model="form.buyerName" readonly placeholder="请选择采购员" style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.buyerName" class="clear-icon" @click.stop="clearBuyer"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="部门" prop="deptName">
                    <el-input v-model="form.deptName" placeholder="选择采购员后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 付款与结算 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c6')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>付款与结算</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c6 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c6">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="付款方式" prop="paymentMethod">
                    <el-select v-model="form.paymentMethod" placeholder="请选择" style="width: 100%" @change="onPaymentMethodChange">
                      <el-option v-for="d in wms_payment_method" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="账期天数" prop="paymentDays">
                    <el-input-number v-model="form.paymentDays" :min="0" :max="365" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="结算方式" prop="settlementType">
                    <el-select v-model="form.settlementType" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in wms_settlement_type" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="总金额" prop="totalAmount">
                    <el-input-number v-model="form.totalAmount" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>采购明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddDetail">添加明细</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border size="small">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="物料" prop="materialId" min-width="200">
                  <template #default="scope">
                    <el-select v-model="scope.row.materialId" filterable clearable size="small" placeholder="请选择物料" style="width: 100%" @change="(val) => onMaterialChange(val, scope.$index)">
                      <el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" prop="specModel" min-width="120">
                  <template #default="scope"><span>{{ scope.row.specModel }}</span></template>
                </el-table-column>
                <el-table-column label="单位" prop="unit" width="90">
                  <template #default="scope">
                    <el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%">
                      <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" prop="qty" width="100">
                  <template #default="scope"><el-input-number v-model="scope.row.qty" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calcDetailAmount(scope.row)" /></template>
                </el-table-column>
                <el-table-column label="单价" prop="unitPrice" width="110">
                  <template #default="scope"><el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 100px" @change="calcDetailAmount(scope.row)" /></template>
                </el-table-column>
                <el-table-column label="金额" prop="amount" width="110" align="right">
                  <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button></template>
                </el-table-column>
              </el-table>
              <div class="detail-summary" v-if="form.detailList && form.detailList.length > 0">
                <span>合计：总数量 {{ totalQuantity }} | 总金额 <span class="rd-amount">{{ formatMoney(totalAmount) }}</span></span>
              </div>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 审核记录（含驳回提示） -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
            <div class="rd-card-body" style="display:block">
              <el-alert v-if="form.status === '7'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该订单已被驳回，请根据审核意见修改后重新提交</template>
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

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="1166px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
          </div>
          <span class="rd-detail-header-title">采购订单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ viewData.orderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ viewData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_order_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">预计到货日期</span><div class="rd-value">{{ viewData.orderDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 关联单据 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg></span>关联单据</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">关联合同</span><div class="rd-value">{{ viewData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联申请单号</span><div class="rd-value">{{ viewData.requestNo || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 供应商与采购员 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v4')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>供应商与采购员</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v4">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">采购员</span><div class="rd-value">{{ viewData.buyerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 金额与结算 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v5')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>金额与结算</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v5 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v5">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ viewData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已收货金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.receivedAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">已付金额</span><div class="rd-value rd-amount" :style="{ color: viewData.paidAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(viewData.paidAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">未付金额</span><div class="rd-value rd-amount" :style="{ color: (viewData.totalAmount - viewData.paidAmount) > 0 ? '#f56c6c' : '#67c23a' }">{{ formatMoney((viewData.totalAmount || 0) - (viewData.paidAmount || 0)) }}</div></div>
              <div class="rd-item"><span class="rd-label">付款方式</span><div class="rd-value"><dict-tag :options="wms_payment_method" :value="viewData.paymentMethod" /></div></div>
              <div class="rd-item"><span class="rd-label">账期天数</span><div class="rd-value">{{ viewData.paymentDays != null ? viewData.paymentDays + '天' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结算方式</span><div class="rd-value"><dict-tag :options="wms_settlement_type" :value="viewData.settlementType" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>采购明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="采购数量" prop="qty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="110" align="right" />
              <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
              <el-table-column label="已收数量" prop="receivedQty" width="100" align="right" />
            </el-table>
          </div>
        </section>
        <!-- 收货明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v6')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M7 12l3 3 7-7"/></svg></span>收货明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v6 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v6">
            <div v-if="receiveList.length === 0" class="rd-empty-text">暂无收货记录</div>
            <el-table v-else :data="receiveDetailList" border size="small">
              <el-table-column label="收货单号" prop="receiveNo" min-width="140" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="应收数量" prop="receiveQty" width="100" align="right" />
              <el-table-column label="合格数量" prop="qualifiedQty" width="100" align="right"><template #default="scope"><span :style="{ color: scope.row.qualifiedQty > 0 ? '#67c23a' : '' }">{{ scope.row.qualifiedQty }}</span></template></el-table-column>
              <el-table-column label="不合格数量" prop="unqualifiedQty" width="100" align="right"><template #default="scope"><span :style="{ color: scope.row.unqualifiedQty > 0 ? '#f56c6c' : '' }">{{ scope.row.unqualifiedQty }}</span></template></el-table-column>
              <el-table-column label="收货日期" prop="receiveDate" width="110" align="center" />
              <el-table-column label="状态" prop="status" width="100" align="center"><template #default="scope"><dict-tag :options="pms_receive_status" :value="scope.row.status" /></template></el-table-column>
              <el-table-column label="验收人" prop="inspectorName" width="100" align="center" />
            </el-table>
            <!-- 收货汇总 -->
            <div class="receive-summary" v-if="receiveDetailList.length > 0">
              <span>收货次数：{{ receiveList.length }} 次</span>
              <span>合格总量：<strong>{{ receiveDetailList.reduce((s, r) => s + (Number(r.qualifiedQty) || 0), 0) }}</strong></span>
              <span>不合格总量：<strong class="unqualified-total">{{ receiveDetailList.reduce((s, r) => s + (Number(r.unqualifiedQty) || 0), 0) }}</strong></span>
            </div>
          </div>
        </section>
        <!-- 发票结算明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v8')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg></span>发票结算</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v8 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v8">
            <div v-if="invoiceList.length === 0" class="rd-empty-text">暂无发票结算记录</div>
            <el-table v-else :data="invoiceList" border size="small">
              <el-table-column label="结算单号" prop="invoiceNo" min-width="140" />
              <el-table-column label="状态" prop="status" width="100" align="center"><template #default="scope"><dict-tag :options="pms_invoice_status" :value="scope.row.status" /></template></el-table-column>
              <el-table-column label="发票号码" prop="invoiceNumber" width="130" />
              <el-table-column label="开票日期" prop="invoiceDate" width="110" align="center" />
              <el-table-column label="开票金额" prop="totalAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template></el-table-column>
              <el-table-column label="已付金额" prop="paymentAmount" width="120" align="right"><template #default="scope"><span class="rd-amount" :style="{ color: scope.row.paymentAmount > 0 ? '#67c23a' : '' }">{{ formatMoney(scope.row.paymentAmount) }}</span></template></el-table-column>
              <el-table-column label="付款日期" prop="paymentDate" width="110" align="center" />
              <el-table-column label="审核人" prop="auditBy" width="100" align="center" />
            </el-table>
            <!-- 发票汇总 -->
            <div class="receive-summary" v-if="invoiceList.length > 0">
              <span>发票数量：{{ invoiceList.length }} 张</span>
              <span>开票金额：<strong>{{ formatMoney(invoiceList.reduce((s, r) => s + (Number(r.totalAmount) || 0), 0)) }}</strong></span>
              <span>已付总额：<strong class="paid-total">{{ formatMoney(invoiceList.reduce((s, r) => s + (Number(r.paymentAmount) || 0), 0)) }}</strong></span>
            </div>
          </div>
        </section>
        <!-- 其他信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v7')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v7 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v7">
            <div class="rd-grid">
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

    <!-- 审核对话框 -->
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">采购订单审核</span>
          <div class="rd-detail-header-sub" v-if="auditData.orderNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.orderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 订单信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg></span>订单信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">采购单号</span><div class="rd-value">{{ auditData.orderNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联合同</span><div class="rd-value">{{ auditData.contractNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关联申请单号</span><div class="rd-value">{{ auditData.requestNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ auditData.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预计到货日期</span><div class="rd-value">{{ auditData.orderDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_order_status" :value="auditData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">采购员</span><div class="rd-value">{{ auditData.buyerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ auditData.totalQty || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.totalAmount) }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 订单明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>订单明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="采购数量" prop="qty" width="100" align="right" />
              <el-table-column label="单价" prop="unitPrice" width="110" align="right" />
              <el-table-column label="金额" prop="amount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.amount) }}</span></template></el-table-column>
            </el-table>
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
          <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.c3">
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

    <!-- 状态流转帮助对话框 -->
    <el-dialog v-model="showStatusHelp" title="采购订单业务状态流转说明" width="700px" append-to-body>
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
            <el-tag type="primary">已下单</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">部分到货</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="info">修改后重置</el-tag>
          </div>
        </div>
        
        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建采购订单后的初始状态，可编辑、删除、提交审批
          </el-descriptions-item>
          <el-descriptions-item label="待审批">
            已提交审批，等待审批人审核。审批人可【通过】或【驳回】
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批未通过，可根据审批意见修改后重新提交
          </el-descriptions-item>
          <el-descriptions-item label="已下单">
            审批通过，订单已正式下发给供应商，等待收货
          </el-descriptions-item>
          <el-descriptions-item label="部分到货">
            通过收货验收审核后，部分合格物料已到货入库，订单仍在进行中
          </el-descriptions-item>
          <el-descriptions-item label="已完成">
            全部物料已到货验收审核通过并入库完成，订单结束
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            草稿状态的订单被删除（软删除）
          </el-descriptions-item>
        </el-descriptions>
        
        <h4>三、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>提交审批：</strong>草稿状态点击"提交"按钮，变为待审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审批通过：</strong>审批人点击"通过"，变为已下单状态
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审批驳回：</strong>审批人点击"驳回"，变为已驳回状态，可修改后重新提交
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>收货验收：</strong>在【收货和发票管理/收货验收】模块创建收货单，填写合格数量后提交审核。审核通过后系统<strong style="color: #67c23a;">自动生成采购入库单</strong>（合格品和不合格品均入库，不合格品标记待退），并自动更新订单已收数量和状态
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>自动完成：</strong>当累计合格收货数量 ≥ 采购数量时，订单自动变为已完成
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>发起退货：</strong>存在不合格品的收货单，可在收货验收页面点击"发起退货"创建退货单，退货审批通过后自动生成退货出库单，将不合格品退回供应商
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 采购员选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择采购员" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="PmsOrder">
import { listOrder, getOrder, addOrder, updateOrder, delOrder, submitOrder, auditOrder } from '@/api/pms/order'
import { listContract } from '@/api/pms/contract'
import { listRequest, getRequest } from '@/api/pms/request'
import { listReceive, getReceive } from '@/api/pms/receive'
import { listInvoice } from '@/api/pms/invoice'
import { listMaterial } from '@/api/wms/material'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
import { CircleClose, ArrowRight, ArrowDown, QuestionFilled, Search, Filter, WarningFilled, Edit, Delete, Download } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { pms_order_status, pms_receive_status, pms_invoice_status, wms_payment_method, wms_settlement_type, wms_unit } = proxy.useDict('pms_order_status', 'pms_receive_status', 'pms_invoice_status', 'wms_payment_method', 'wms_settlement_type', 'wms_unit')

const { collapsedCards, toggleCard } = useDetailCard(["c1", "c4", "c5", "c6", "c2", "c0", "c3", "v1", "v3", "v4", "v5", "v6", "v8", "v7", "v2"])
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_order_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  orderNo: { label: '采购单号', visible: true },
  contractNo: { label: '关联合同', visible: true },
  requestNo: { label: '关联申请单号', visible: true },
  supplierName: { label: '供应商', visible: true },
  status: { label: '状态', visible: true },
  orderDate: { label: '预计到货日期', visible: true },
  totalQty: { label: '总数量', visible: true },
  totalAmount: { label: '总金额', visible: true },
  receivedAmount: { label: '已收货金额', visible: true },
  paidAmount: { label: '已付金额', visible: true },
  orderUnpaidAmount: { label: '未付金额', visible: true },
  buyerName: { label: '采购员', visible: true },
  paymentMethod: { label: '付款方式', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_order_columns')
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
const statusTabList = computed(() => pms_order_status.value.map(d => ({ label: d.label, value: d.value })))
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listOrder({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    pms_order_status.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-approved', '3': 'tab-partial', '4': 'tab-done', '7': 'tab-reject', '5': 'tab-void' }
  return map[value] || ''
}
function badgeClass(status) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'violet', '4': 'green', '7': 'red', '5': 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = pms_order_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function paymentMethodLabel(method) {
  const item = wms_payment_method.value.find(d => d.value == method)
  return item ? item.label : '-'
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  queryParams.value.status = tab === 'all' ? undefined : tab
  handleQuery()
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.orderNo) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.contractNo) count++
  if (queryParams.value.requestNo) count++
  if (queryParams.value.paymentMethod) count++
  if (queryParams.value.buyerName) count++
  if (queryParams.value.settlementType) count++
  return count
})

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const receiveList = ref([])
const receiveDetailList = ref([])
const invoiceList = ref([])
const auditOpen = ref(false)
const auditData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const dateRange = ref([])
const contractOptions = ref([])
const requestOptions = ref([])
const materialOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    supplierName: undefined,
    status: undefined,
    contractNo: undefined,
    requestNo: undefined,
    paymentMethod: undefined,
    buyerName: undefined,
    settlementType: undefined,
    params: {}
  },
  auditForm: {
    orderId: null,
    auditOpinion: null
  },
  rules: {
    orderDate: [{ required: true, message: '预计到货日期不能为空', trigger: 'change' }],
    buyerName: [{ required: true, message: '采购员不能为空', trigger: 'change' }]
  },
  auditRules: {
    auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
  }
})
const { queryParams, form, rules, auditForm, auditRules } = toRefs(data)

// 计算属性
const totalQuantity = computed(() => {
  if (!form.value.detailList) return 0
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.qty) || 0), 0).toFixed(2)
})

const totalAmount = computed(() => {
  if (!form.value.detailList) return 0
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
})

function getList() {
  loading.value = true
  listOrder(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value, 'Date')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
queryParams.value.orderNo = undefined
queryParams.value.supplierName = undefined
queryParams.value.status = undefined
queryParams.value.contractNo = undefined
queryParams.value.requestNo = undefined
queryParams.value.paymentMethod = undefined
queryParams.value.buyerName = undefined
queryParams.value.settlementType = undefined
dateRange.value = []
queryParams.value.params = {}
activeStatusTab.value = 'all'
if (tableRef.value) tableRef.value.clearSort()
handleQuery()
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(i => i.orderId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    orderId: undefined,
    orderNo: undefined,
    requestId: undefined,
    requestNo: undefined,
    contractId: undefined,
    contractNo: undefined,
    supplierId: undefined,
    supplierName: undefined,
    status: '0',
    orderDate: undefined,
    totalQty: 0,
    totalAmount: 0,
    receivedAmount: 0,
    buyerId: undefined,
    buyerName: undefined,
    deptId: undefined,
    deptName: undefined,
    paymentMethod: '0',
    paymentDays: 0,
    settlementType: '0',
    remark: undefined,
    detailList: []
  }
  proxy.resetForm('orderRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加采购订单'
}

function handleUpdate(row) {
  reset()
  getOrder(row.orderId || ids.value[0]).then(res => {
    form.value = res.data
    if (!form.value.detailList) form.value.detailList = []
    open.value = true
    title.value = '修改采购订单'
  })
}

function handleView(row) {
  getOrder(row.orderId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
    // 根据数据情况自动折叠无数据的卡片
    autoCollapseViewCards(res.data)
    // 加载该订单的收货记录
    loadReceiveList(row.orderId)
    // 加载该订单的发票结算记录
    loadInvoiceList(row.orderId)
  })
}

/** 详情页：自动折叠无数据的卡片 */
function autoCollapseViewCards(data) {
  // 关联单据：无合同且无申请单号
  collapsedCards.v3 = !data.contractNo && !data.requestNo
  // 供应商与采购员：三者都为空
  collapsedCards.v4 = !data.supplierName && !data.buyerName && !data.deptName
  // 采购明细：无明细列表
  collapsedCards.v2 = !data.detailList || data.detailList.length === 0
  // 其他信息：无备注
  collapsedCards.v7 = !data.remark
}

/** 加载订单关联的收货记录，并展开物料明细 */
function loadReceiveList(orderId) {
  receiveList.value = []
  receiveDetailList.value = []
  if (!orderId) return
  listReceive({ orderId: orderId, pageNum: 1, pageSize: 999 }).then(res => {
    const records = res.rows || []
    receiveList.value = records
    // 无收货记录时自动折叠
    collapsedCards.v6 = records.length === 0
    // 获取每条收货记录的明细，展平为物料级列表
    if (records.length === 0) return
    Promise.all(records.map(r => getReceive(r.receiveId))).then(results => {
      const details = []
      results.forEach(item => {
        const rec = item.data
        if (rec.detailList && rec.detailList.length > 0) {
          rec.detailList.forEach(d => {
            details.push({
              receiveNo: rec.receiveNo,
              receiveDate: rec.receiveDate,
              status: rec.status,
              inspectorName: rec.inspectorName,
              materialCode: d.materialCode,
              materialName: d.materialName,
              specModel: d.specModel,
              unit: d.unit,
              receiveQty: d.receiveQty,
              qualifiedQty: d.qualifiedQty,
              unqualifiedQty: d.unqualifiedQty
            })
          })
        }
      })
      receiveDetailList.value = details
    })
  })
}

/** 加载订单关联的发票结算记录 */
function loadInvoiceList(orderId) {
  invoiceList.value = []
  if (!orderId) return
  listInvoice({ orderId: orderId, pageNum: 1, pageSize: 999 }).then(res => {
    invoiceList.value = res.rows || []
    // 无发票记录时自动折叠
    collapsedCards.v8 = invoiceList.value.length === 0
  })
}

function calcDetailAmount(row) {
  if (row.qty != null && row.unitPrice != null) {
    row.amount = parseFloat((row.qty * row.unitPrice).toFixed(2))
  }
  let total = 0, qty = 0
  form.value.detailList.forEach(d => {
    if (d.amount != null) total += d.amount
    if (d.qty != null) qty += d.qty
  })
  form.value.totalAmount = parseFloat(total.toFixed(2))
  form.value.totalQty = parseFloat(qty.toFixed(2))
}

function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = []
  form.value.detailList.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    specModel: '',
    unit: '',
    qty: 0,
    unitPrice: 0,
    amount: 0
  })
}

function handleDeleteDetail(index) {
  form.value.detailList.splice(index, 1)
  calcDetailAmount({})
}

/** 选择物料后自动带出物料信息 */
function onMaterialChange(val, index) {
  const matched = materialOptions.value.find(m => m.materialId === val)
  if (matched) {
    form.value.detailList[index].materialCode = matched.materialCode
    form.value.detailList[index].materialName = matched.materialName
    form.value.detailList[index].specModel = matched.specModel || matched.specification
    form.value.detailList[index].unit = matched.unit
  }
}

/** 加载物料主数据选项 */
function loadMaterialOptions() {
  listMaterial({ pageNum: 1, pageSize: 999, status: '0' }).then(res => {
    materialOptions.value = res.rows || []
  })
}

/** 选择合同后自动带出关联申请单号和供应商 */
function onContractChange(val) {
  if (!val) {
    form.value.contractId = undefined
    form.value.contractNo = undefined
    return
  }
  const matched = contractOptions.value.find(c => c.contractId === val)
  if (matched) {
    form.value.contractNo = matched.contractNo
    // 带出供应商
    if (matched.supplierId) {
      form.value.supplierId = matched.supplierId
      form.value.supplierName = matched.supplierName
    }
    // 带出关联申请单号及物料明细
    if (matched.orderId) {
      form.value.requestId = matched.orderId
      form.value.requestNo = matched.orderNo
      onRequestChange(matched.orderId)
    }
  }
}

/** 选择采购申请后自动带出申请单号和物料明细 */
function onRequestChange(val) {
  if (!val) {
    form.value.requestId = undefined
    form.value.requestNo = undefined
    return
  }
  const matched = requestOptions.value.find(r => r.requestId === val)
  if (matched) {
    form.value.requestNo = matched.requestNo
  }
  // 获取采购申请详情，带出物料明细
  getRequest(val).then(res => {
    const requestData = res.data
    if (requestData.detailList && requestData.detailList.length > 0) {
      // 如果已有明细，提示用户是否覆盖
      if (form.value.detailList && form.value.detailList.length > 0) {
        proxy.$modal.confirm('选择采购申请将覆盖当前已有的采购明细，是否继续？').then(() => {
          fillDetailFromRequest(requestData.detailList)
        }).catch(() => {})
      } else {
        fillDetailFromRequest(requestData.detailList)
      }
    }
  })
}

/** 将采购申请明细填充到采购订单明细 */
function fillDetailFromRequest(detailList) {
  form.value.detailList = detailList.map(d => ({
    materialId: d.materialId,
    materialCode: d.materialCode,
    materialName: d.materialName,
    specModel: d.specModel,
    unit: d.unit,
    qty: d.qty,
    unitPrice: d.estimatedPrice,
    amount: d.estimatedAmount
  }))
  calcDetailAmount({})
}

/** 付款方式变更时自动设置账期天数默认值 */
function onPaymentMethodChange(val) {
  // 根据付款方式设置默认账期天数
  switch (val) {
    case '0': // 现结
      form.value.paymentDays = 0
      break
    case '1': // 月结
      form.value.paymentDays = 30 // 默认月结30天
      break
    case '2': // 预付
      form.value.paymentDays = 0
      break
    default:
      form.value.paymentDays = 0
  }
}

/** 加载合同选项（仅审批通过且已生效的合同） */
function loadContractOptions() {
  listContract({ pageNum: 1, pageSize: 999, status: '1' }).then(res => {
    contractOptions.value = res.rows || []
  })
}

/** 加载采购申请选项（仅已审批通过的申请单） */
function loadRequestOptions() {
  listRequest({ pageNum: 1, pageSize: 999, status: '2' }).then(res => {
    requestOptions.value = res.rows || []
  })
}

function submitForm() {
  proxy.$refs['orderRef'].validate(valid => {
    if (valid) {
      form.value.totalAmount = totalAmount.value
      if (form.value.orderId != undefined) {
        updateOrder(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addOrder(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const orderIds = row.orderId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + orderIds + '"的数据？').then(() => delOrder(orderIds)).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleSubmit(row) {
  proxy.$modal.confirm('确认提交编号为"' + row.orderNo + '"的采购订单？').then(() => submitOrder(row.orderId)).then(() => {
    getList()
    proxy.$modal.msgSuccess('提交成功')
  }).catch(() => {})
}

/** 审核按钮操作 */
async function handleAudit(row) {
  const response = await getOrder(row.orderId)
  auditData.value = response.data
  auditForm.value = {
    orderId: row.orderId,
    auditOpinion: null
  }
  auditOpen.value = true
}

/** 提交审核 */
function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const status = passed ? '2' : '7'
      const actionText = passed ? '通过' : '驳回'
      proxy.$modal.confirm(`确认${actionText}该采购订单？`).then(() => {
        return auditOrder(auditForm.value.orderId, status, auditForm.value.auditOpinion)
      }).then(() => {
        proxy.$modal.msgSuccess('审批成功')
        auditOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
}

function handleExport() {
  proxy.download('pms/order/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'Date') }, `purchase_order_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** 打开采购员选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.buyerId)
}

/** 采购员选择确认回调 — 自动带出所在部门 */
function onUserPickerConfirm(user) {
  form.value.buyerId = user.userId
  form.value.buyerName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}

/** 清除采购员 */
function clearBuyer() {
  form.value.buyerId = undefined
  form.value.buyerName = undefined
  form.value.deptId = undefined
  form.value.deptName = undefined
}

loadContractOptions()
loadRequestOptions()
loadMaterialOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-order-page {
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
.pms-order-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-order-page .filter-card { padding:14px 20px 16px; }
.pms-order-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-order-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-order-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-order-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-order-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-order-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-order-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-order-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-order-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-order-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-order-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-order-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-order-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-order-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-order-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-order-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-order-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-order-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-order-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-order-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-order-page .field .control :deep(.el-select) { width:100%; }
.pms-order-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-order-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-order-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-order-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-order-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-order-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-order-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-order-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-order-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-order-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-order-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-order-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-order-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-order-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.pms-order-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.pms-order-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.pms-order-page .status-tab.tab-audit .dot { background:var(--blue-500); }
.pms-order-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); }
.pms-order-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.pms-order-page .status-tab.tab-approved .dot, .pms-order-page .status-tab.tab-done .dot { background:var(--green-500); }
.pms-order-page .status-tab.tab-approved .count, .pms-order-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.pms-order-page .status-tab.is-active.tab-approved .count, .pms-order-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.pms-order-page .status-tab.tab-reject .dot { background:var(--red-500); }
.pms-order-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.pms-order-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.pms-order-page .status-tab.tab-void .dot { background:var(--ink-400); }
.pms-order-page .status-tab.tab-partial .dot { background:var(--violet-500, #8b5cf6); }
.pms-order-page .status-tab.tab-partial .count { background:var(--violet-50); color:#7c3aed; }
.pms-order-page .status-tab.is-active.tab-partial .count { background:var(--violet-500, #8b5cf6); color:#fff; }
.pms-order-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.pms-order-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-order-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-order-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-order-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-order-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-order-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-order-page .btn-soft .el-icon { font-size:14px; }
.pms-order-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-order-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-order-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-order-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-order-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-order-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-order-page .table-wrap { overflow-x:auto; }
.pms-order-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-order-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-order-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-order-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-order-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-order-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-order-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-order-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-order-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-order-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-order-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-order-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-order-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-order-page .badge.amber .dot { background:var(--amber-500); }
.pms-order-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-order-page .badge.blue .dot { background:var(--blue-500); }
.pms-order-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-order-page .badge.green .dot { background:var(--green-500); }
.pms-order-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.pms-order-page .badge.red .dot { background:var(--red-500); }
.pms-order-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-order-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-order-page .badge.gray .dot { background:var(--ink-400); }
.pms-order-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-order-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-order-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-order-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-order-page .pagination-container :deep(.el-pagination .btn-prev), .pms-order-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-order-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-order-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-order-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-order-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-order-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-order-page .toolbar { flex-wrap:wrap; gap:10px; } }

.detail-summary {
  text-align: right;
  padding: 10px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-top: none;
  font-weight: bold;
}
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
.rd-form-tip {
  margin-left: 4px;
  color: #c0c4cc;
  font-size: 14px;
  vertical-align: middle;
  cursor: help;
}
.rd-form-tip:hover {
  color: #909399;
}
.rd-select-empty {
  padding: 10px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
.reject-alert {
  margin-bottom: 16px;
}

.rd-empty-text {
  text-align: center;
  color: #909399;
  padding: 24px 0;
  font-size: 13px;
}

.receive-summary {
  display: flex;
  gap: 24px;
  padding: 10px 16px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-top: none;
  font-size: 13px;
}

.receive-summary .unqualified-total {
  color: #f56c6c;
}

.receive-summary .paid-total {
  color: #67c23a;
}

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
</style>
