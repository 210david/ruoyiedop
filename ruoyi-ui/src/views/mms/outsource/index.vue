<template>
  <div class="app-container mms-outsource-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>外协单号</label><div class="control"><el-input v-model="queryParams.outsourceNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>供应商</label><div class="control"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_outsource_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>外协日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track">
        <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button>
        <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button>
      </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:outsource:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:outsource:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:outsource:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:outsource:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_outsource_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="外协单号" prop="outsourceNo" key="outsourceNo" :width="colWidth('outsourceNo', 150)" resizable v-if="columns.outsourceNo.visible" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="工序" prop="processName" key="processName" :width="colWidth('processName', 120)" resizable show-overflow-tooltip v-if="columns.processName.visible" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 140)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="外协数量" prop="outQty" key="outQty" :width="colWidth('outQty', 100)" resizable align="center" v-if="columns.outQty.visible" />
          <el-table-column label="单价" prop="unitPrice" key="unitPrice" :width="colWidth('unitPrice', 110)" resizable align="center" v-if="columns.unitPrice.visible"><template #default="scope"><span class="rd-amount">{{ scope.row.unitPrice != null ? '￥' + formatAmount(scope.row.unitPrice) : '—' }}</span></template></el-table-column>
          <el-table-column label="总金额" prop="totalAmount" key="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="center" v-if="columns.totalAmount.visible"><template #default="scope"><span class="rd-amount rd-value--large">{{ scope.row.totalAmount != null ? '￥' + formatAmount(scope.row.totalAmount) : '—' }}</span></template></el-table-column>
          <el-table-column label="外协日期" prop="outDate" key="outDate" :width="colWidth('outDate', 120)" resizable align="center" v-if="columns.outDate.visible"><template #default="scope">{{ scope.row.outDate ? parseTime(scope.row.outDate, '{y}-{m}-{d}') : '—' }}</template></el-table-column>
          <el-table-column label="计划回厂" prop="planBackDate" key="planBackDate" :width="colWidth('planBackDate', 120)" resizable align="center" v-if="columns.planBackDate.visible"><template #default="scope">{{ scope.row.planBackDate ? parseTime(scope.row.planBackDate, '{y}-{m}-{d}') : '—' }}</template></el-table-column>
          <el-table-column label="实际回厂" prop="actualBackDate" key="actualBackDate" :width="colWidth('actualBackDate', 120)" resizable align="center" v-if="columns.actualBackDate.visible"><template #default="scope">{{ scope.row.actualBackDate ? parseTime(scope.row.actualBackDate, '{y}-{m}-{d}') : '—' }}</template></el-table-column>
          <el-table-column label="回厂数量" prop="backQty" key="backQty" :width="colWidth('backQty', 100)" resizable align="center" v-if="columns.backQty.visible" />
          <el-table-column label="损耗数量" prop="lossQty" key="lossQty" :width="colWidth('lossQty', 100)" resizable align="center" v-if="columns.lossQty.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:outsource:edit']">修改</el-button>
                <el-button v-if="scope.row.status === '0'" link type="success" icon="Promotion" @click="handleSendOut(scope.row)" v-hasPermi="['mms:outsource:edit']">发料外协</el-button>
                <el-button v-if="scope.row.status === '1'" link type="warning" icon="CircleCheck" @click="handleBackAccept(scope.row)" v-hasPermi="['mms:outsource:edit']">回厂验收</el-button>
                <el-button v-if="scope.row.status === '0'" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:outsource:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="984px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="外协单号" prop="outsourceNo"><el-input v-model="form.outsourceNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请选择" readonly><template #append><el-button icon="Search" @click="openWoPicker" /></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序顺序号" prop="opSeq"><el-input v-model="form.opSeq" placeholder="请选择" readonly><template #append><el-button icon="Search" @click="openProcPicker" /></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="form.processName" placeholder="选择工序后自动带出" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称" prop="productName"><el-input v-model="form.productName" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="规格型号" prop="specModel"><el-input v-model="form.specModel" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col><el-col :span="8"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" placeholder="选择工单后自动带出" disabled><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="8"><el-form-item label="计划数量" prop="planQty"><el-input v-model="form.planQty" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>外协信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请选择" readonly><template #append><el-button icon="Search" @click="openSupplierPicker" /></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="外协数量" prop="outQty"><el-input-number v-model="form.outQty" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="计划回厂" prop="planBackDate"><el-date-picker v-model="form.planBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="984px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">外协单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.outsourceNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.outsourceNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">外协单号</span><div class="rd-value">{{ viewData.outsourceNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">工序顺序号</span><div class="rd-value">{{ viewData.opSeq != null ? viewData.opSeq : '—' }}</div></div><div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ viewData.processName || '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '—' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '—' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '—' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div><div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>外协信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '—' }}</div></div><div class="rd-item"><span class="rd-label">外协数量</span><div class="rd-value">{{ viewData.outQty != null ? viewData.outQty : '—' }}</div></div><div class="rd-item"><span class="rd-label">单价</span><div class="rd-value rd-amount">{{ viewData.unitPrice != null ? '￥' + formatAmount(viewData.unitPrice) : '—' }}</div></div><div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value rd-amount rd-value--large">{{ viewData.totalAmount != null ? '￥' + formatAmount(viewData.totalAmount) : '—' }}</div></div><div class="rd-item"><span class="rd-label">回厂数量</span><div class="rd-value">{{ viewData.backQty != null ? viewData.backQty : '—' }}</div></div><div class="rd-item"><span class="rd-label">损耗数量</span><div class="rd-value">{{ viewData.lossQty != null ? viewData.lossQty : '—' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>日期信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">外协日期</span><div class="rd-value">{{ viewData.outDate ? parseTime(viewData.outDate, '{y}-{m}-{d}') : '—' }}</div></div><div class="rd-item"><span class="rd-label">计划回厂</span><div class="rd-value">{{ viewData.planBackDate ? parseTime(viewData.planBackDate, '{y}-{m}-{d}') : '—' }}</div></div><div class="rd-item"><span class="rd-label">实际回厂</span><div class="rd-value">{{ viewData.actualBackDate ? parseTime(viewData.actualBackDate, '{y}-{m}-{d}') : '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div><div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 发料外协弹窗 ===== -->
    <el-dialog v-model="sendOutOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></div>
          <span class="rd-detail-header-title">发料外协</span>
          <div class="rd-detail-header-sub" v-if="sendOutForm.outsourceNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ sendOutForm.outsourceNo }}</span></div>
        </div>
      </template>
      <el-form ref="sendOutFormRef" :model="sendOutForm" :rules="sendOutRules" label-width="120px" style="margin-top: 16px;">
        <div class="rd-page">
          <!-- 基本信息（只读） -->
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外协单号</span><div class="rd-value">{{ sendOutForm.outsourceNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ sendOutForm.workOrderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ sendOutForm.supplierName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ sendOutForm.productCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ sendOutForm.productName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ sendOutForm.specModel || '—' }}</div></div>
            </div></div>
          </section>
          <!-- 外协信息（只读） -->
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>外协信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外协数量</span><div class="rd-value">{{ sendOutForm.outQty != null ? sendOutForm.outQty : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value">{{ sendOutForm.unitLabel || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">单价</span><div class="rd-value rd-amount">{{ sendOutForm.unitPrice != null ? '￥' + formatAmount(sendOutForm.unitPrice) : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value rd-amount rd-value--large">{{ sendOutForm.totalAmount != null ? '￥' + formatAmount(sendOutForm.totalAmount) : '—' }}</div></div>
            </div></div>
          </section>
          <!-- 发料信息（可编辑） -->
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></span>发料信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="外协日期" prop="outDate"><el-date-picker v-model="sendOutForm.outDate" type="date" value-format="YYYY-MM-DD" placeholder="选择发料日期" style="width:100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划回厂日期" prop="planBackDate"><el-date-picker v-model="sendOutForm.planBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择计划回厂日期" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button @click="sendOutOpen = false">取 消</el-button><el-button type="primary" @click="submitSendOut">确认发料</el-button></template>
    </el-dialog>

    <!-- ===== 回厂验收弹窗 ===== -->
    <el-dialog v-model="backAcceptOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">回厂验收</span>
          <div class="rd-detail-header-sub" v-if="backAcceptForm.outsourceNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ backAcceptForm.outsourceNo }}</span></div>
        </div>
      </template>
      <el-form ref="backAcceptFormRef" :model="backAcceptForm" :rules="backAcceptRules" label-width="120px" style="margin-top: 16px;">
        <div class="rd-page">
          <!-- 基本信息（只读） -->
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外协单号</span><div class="rd-value">{{ backAcceptForm.outsourceNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ backAcceptForm.workOrderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ backAcceptForm.supplierName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ backAcceptForm.productCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ backAcceptForm.productName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ backAcceptForm.specModel || '—' }}</div></div>
            </div></div>
          </section>
          <!-- 外协信息（只读） -->
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>外协信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">外协数量</span><div class="rd-value">{{ backAcceptForm.outQty != null ? backAcceptForm.outQty : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value">{{ backAcceptForm.unitLabel || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">单价</span><div class="rd-value rd-amount">{{ backAcceptForm.unitPrice != null ? '￥' + formatAmount(backAcceptForm.unitPrice) : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">总金额</span><div class="rd-value rd-amount rd-value--large">{{ backAcceptForm.totalAmount != null ? '￥' + formatAmount(backAcceptForm.totalAmount) : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">外协日期</span><div class="rd-value">{{ backAcceptForm.outDate || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划回厂</span><div class="rd-value">{{ backAcceptForm.planBackDate || '—' }}</div></div>
            </div></div>
          </section>
          <!-- 回厂验收信息（可编辑） -->
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>回厂验收信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="实际回厂日期" prop="actualBackDate"><el-date-picker v-model="backAcceptForm.actualBackDate" type="date" value-format="YYYY-MM-DD" placeholder="选择回厂日期" style="width:100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="回厂数量" prop="backQty"><el-input-number v-model="backAcceptForm.backQty" :min="0" :precision="2" :step="1" :max="Number(backAcceptForm.outQty) || undefined" placeholder="请输入" style="width:100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="损耗数量"><el-input :model-value="computedLossQty" disabled><template #append>自动计算（外协-回厂）</template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="损耗率"><el-input :model-value="computedLossRate" disabled><template #append>%</template></el-input></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button @click="backAcceptOpen = false">取 消</el-button><el-button type="primary" @click="submitBackAccept">确认验收</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="外协管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <!-- 一、外协单释义 -->
        <h4>一、外协单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是外协单？</div>
          <div class="highlight-card-body">
            <strong>外协单（Outsource Order）</strong>是生产管控中将工序任务委托外部供应商加工的业务单据。外协单关联工单编号、工序信息和供应商，记录外协数量、单价、总金额及回厂数量，通过外协发料→回厂验收的状态流转，实现外协业务的精细化管控和过程可追溯。<br/><br/>
            外协单是<strong>MES（制造执行系统）</strong>中外协管理的核心载体，向上对接工单的工序拆分与产能规划，向下驱动供应商协同、回厂验收和损耗统计，满足精益生产对外协业务可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、外协单状态流转图 -->
        <h4>二、外协单状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待外协</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">发料外协</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">外协中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">回厂验收</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已回厂</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待外协">外协单新建后的初始状态。可修改外协信息，点击「发料外协」将物料发至供应商，状态变为外协中</el-descriptions-item>
          <el-descriptions-item label="外协中">外协物料已发至供应商，正在外协加工中。点击「回厂验收」录入实际回厂日期和回厂数量，系统自动计算损耗数量，状态变为已回厂</el-descriptions-item>
          <el-descriptions-item label="已回厂">外协物料已回厂验收，回厂数量和损耗数量已确认。不可再修改，数据归档</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>外协单号：</strong>外协单的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>工单编号：</strong>关联的生产工单编号<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>工序顺序号：</strong>对应工单中的工序顺序号</p>
            <p>• <strong>工序名称：</strong>外协加工的工序名称</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">外协信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>供应商：</strong>承接外协任务的供应商名称<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>外协数量：</strong>委托外协加工的数量<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>单价：</strong>外协加工的单价</p>
            <p>• <strong>状态：</strong>外协单的当前流转状态</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">日期信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>外协日期：</strong>物料发至供应商的日期</p>
            <p>• <strong>计划回厂：</strong>计划外协物料回厂的日期</p>
            <p>• <strong>实际回厂：</strong>外协物料实际回厂的日期</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">备注信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>备注：</strong>外协单的补充说明信息</p>
          </div>
        </div>

        <!-- 五、外协单生命周期管控 -->
        <h4>五、外协单生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是外协单生命周期管控？</div>
          <div class="highlight-card-body">
            <strong>外协单生命周期管控</strong>是生产管控的核心机制，通过状态流转实现外协单从创建到回厂的全过程管理。每个状态对应特定的可执行操作，确保外协业务有序可控、可追溯。外协发料机制确保物料流向可查，回厂验收机制确保回厂数量和损耗数量的准确归集。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">异常处理规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>已回厂的外协单无法修改：</strong>外协单回厂验收后进入「已回厂」状态，不允许再编辑基本信息，确保外协数据的一致性</p>
            <p>2. <strong>损耗数量记录：</strong>外协物料回厂时需核对数量，损耗数量 = 外协数量 - 回厂数量，系统自动计算</p>
            <p>3. <strong>超期预警：</strong>超过计划回厂日期未回厂的外协单需及时跟进供应商，确保生产计划不受影响</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>外协单的创建和回厂操作均记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建外协单：</strong>点击「新增」创建外协单，填写工单编号、工序信息、供应商和外协数量，保存后外协单号自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>发料外协：</strong>将物料发至供应商，填写外协日期，状态变为外协中
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>点击「查看」查看外协单完整信息，包括外协信息、日期信息和回厂数据
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>回厂验收：</strong>外协物料回厂后填写实际回厂日期和回厂数量，系统自动计算损耗数量，状态变为已回厂
          </el-timeline-item>
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
          <el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px" @keyup.enter="handleWoPickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input>
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
        <div class="material-picker-pager"><el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" /></div>
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
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="procPickerSelectedId" :value="row.snapshotId" @click.stop="onProcRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="顺序号" prop="stepSeq" width="80" align="center" />
            <el-table-column label="工序编码" prop="processCode" width="120" show-overflow-tooltip />
            <el-table-column label="工序名称" prop="processName" min-width="160" show-overflow-tooltip />
            <el-table-column label="绑定产能单元" prop="resourceName" min-width="140" show-overflow-tooltip />
            <el-table-column label="是否外协" prop="isOutsource" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.isOutsource === '1' ? 'warning' : 'info'" size="small">{{ scope.row.isOutsource === '1' ? '是' : '否' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div v-if="processOptions.length === 0 && !procPickerLoading" style="text-align:center;padding:20px;color:#909399;font-size:13px">
          该工单暂无工艺快照数据，请确认工单已下达
        </div>
      </div>
      <template #footer><el-button @click="procPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleProcPickerConfirm" :disabled="!procPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 供应商选择器弹窗 ===== -->
    <el-dialog v-model="supplierPickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></div>
          <span class="rd-detail-header-title">选择供应商</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="supplierPickerQuery.supplierCode" placeholder="供应商编码" clearable size="small" style="width: 160px" @keyup.enter="handleSupplierPickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input>
          <el-input v-model="supplierPickerQuery.supplierName" placeholder="供应商名称" clearable size="small" style="width: 200px; margin-left: 8px" @keyup.enter="handleSupplierPickerQuery" />
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleSupplierPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetSupplierPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table">
          <el-table v-loading="supplierPickerLoading" :data="supplierPickerList" highlight-current-row @row-click="onSupplierRowClick" @row-dblclick="onSupplierRowDblClick" height="360" size="small" border>
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="supplierPickerSelectedId" :value="row.supplierId" @click.stop="onSupplierRowClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="供应商编码" prop="supplierCode" width="130" show-overflow-tooltip />
            <el-table-column label="供应商名称" prop="supplierName" min-width="180" show-overflow-tooltip />
            <el-table-column label="联系人" prop="contactPerson" width="100" show-overflow-tooltip />
            <el-table-column label="联系电话" prop="contactPhone" width="120" show-overflow-tooltip />
            <el-table-column label="状态" prop="status" width="80" align="center"><template #default="scope"><el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager"><el-pagination v-model:current-page="supplierPickerQuery.pageNum" v-model:page-size="supplierPickerQuery.pageSize" :total="supplierPickerTotal" layout="total, prev, pager, next" small @current-change="getSupplierPickerList" /></div>
      </div>
      <template #footer><el-button @click="supplierPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleSupplierPickerConfirm" :disabled="!supplierPickerSelectedId">确 定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Outsource">
import { listOutsource, getOutsource, addOutsource, updateOutsource, delOutsource, sendOutOutsource, backAcceptOutsource } from "@/api/mms/outsource";
import { listWorkOrder, listWorkOrderProcesses } from "@/api/mms/workorder";
import { listSupplier } from "@/api/wms/supplier";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount } from '@/composables/useDetailCard'
import { Filter, RefreshLeft, ArrowDown, ArrowRight, WarningFilled, Search, Promotion, CircleCheck } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();
const { mms_outsource_status, mms_workorder_status, wms_unit } = proxy.useDict("mms_outsource_status", "mms_workorder_status", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_outsource_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c4","vc0","vc1","vc2","vc3","vc4"])
const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref(""); const dateRange = ref([]); const showStatusHelp = ref(false);
const activeStatusTab = ref("all");
const statusTabList = computed(() => mms_outsource_status.value ? mms_outsource_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { outsourceNo: { label: '外协单号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, productName: { label: '产品名称', visible: true }, planQty: { label: '计划数量', visible: true }, processName: { label: '工序', visible: true }, supplierName: { label: '供应商', visible: true }, outQty: { label: '外协数量', visible: true }, unitPrice: { label: '单价', visible: true }, totalAmount: { label: '总金额', visible: true }, outDate: { label: '外协日期', visible: true }, planBackDate: { label: '计划回厂', visible: true }, actualBackDate: { label: '实际回厂', visible: true }, backQty: { label: '回厂数量', visible: true }, lossQty: { label: '损耗数量', visible: true }, status: { label: '状态', visible: true } };
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_outsource_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result; } } catch (e) {} return { ...defaultColumns }; }
const columns = ref(loadColumnVisibility());
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, outsourceNo: undefined, workOrderNo: undefined, supplierName: undefined, status: undefined, params: {} },
  rules: { workOrderNo: [{ required: true, message: "请选择工单", trigger: "change" }], supplierName: [{ required: true, message: "请选择供应商", trigger: "change" }], outQty: [{ required: true, message: "请输入外协数量", trigger: "blur" }] }
});
const { queryParams, form, rules } = toRefs(data);
function getList() { loading.value = true; listOutsource(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths(); }); fetchStatusCounts(); }
function handleQuery() { queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.outsourceNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.supplierName = undefined; queryParams.value.status = undefined; dateRange.value = []; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.outsourceId); single.value = sel.length !== 1; multiple.value = !sel.length; }
function reset() { form.value = { outsourceId: undefined, outsourceNo: undefined, workOrderId: undefined, workOrderNo: undefined, opSeq: undefined, processName: undefined, productId: undefined, productCode: undefined, productName: undefined, specModel: undefined, unit: undefined, planQty: undefined, supplierId: undefined, supplierName: undefined, outQty: undefined, unitPrice: undefined, outDate: undefined, planBackDate: undefined, actualBackDate: undefined, status: "0", remark: undefined }; processOptions.value = []; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增外协单"; }
function handleUpdate(row) { reset(); getOutsource(row.outsourceId || ids.value[0]).then(res => { form.value = res.data; if (form.value.workOrderId) { listWorkOrderProcesses(form.value.workOrderId).then(pres => { processOptions.value = pres.data || []; }).catch(() => {}); } open.value = true; title.value = "修改外协单"; }); }
function handleView(row) { const id = row.outsourceId || ids.value[0]; getOutsource(id).then(res => { viewData.value = res.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(v => { if (v) { if (form.value.outsourceId != null) { updateOutsource(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addOutsource(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.outsourceId || ids.value; proxy.$modal.confirm('确认删除选中的外协记录？').then(() => delOutsource(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/outsource/export", { ...queryParams.value }, `outsource_${new Date().getTime()}.xlsx`); }
function statusLabel(s) { if (s === null || s === undefined || s === '') return '—'; const i = mms_outsource_status.value?.find(d => d.value == s); return i ? i.label : '—'; }
function badgeClass(s) { return { '0': 'amber', '1': 'blue', '2': 'green' }[s] || 'gray'; }
function statusTabClass(v) { return { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done' }[v] || ''; }
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.outsourceNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.supplierName) c++; if (queryParams.value.status) c++; if (dateRange.value && dateRange.value.length) c++; return c; });
const statusCounts = ref({});
function fetchStatusCounts() {
  const baseQuery = { pageNum: 1, pageSize: 999 };
  if (queryParams.value.outsourceNo) baseQuery.outsourceNo = queryParams.value.outsourceNo;
  if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo;
  if (queryParams.value.supplierName) baseQuery.supplierName = queryParams.value.supplierName;
  listOutsource(proxy.addDateRange(baseQuery, dateRange.value)).then(res => {
    const counts = { all: res.total };
    if (mms_outsource_status.value) {
      mms_outsource_status.value.forEach(d => { counts[d.value] = 0; });
      (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; });
    }
    statusCounts.value = counts;
  }).catch(() => {});
}

// ===== 发料外协 =====
const sendOutOpen = ref(false);
const sendOutForm = ref({});
const sendOutRules = { outDate: [{ required: true, message: '请选择外协日期', trigger: 'change' }] };
function handleSendOut(row) {
  getOutsource(row.outsourceId).then(res => {
    const d = res.data;
    let unitLabel = d.unit;
    if (wms_unit.value && d.unit) {
      const u = wms_unit.value.find(x => x.value === d.unit);
      if (u) unitLabel = u.label;
    }
    sendOutForm.value = {
      outsourceId: d.outsourceId,
      outsourceNo: d.outsourceNo,
      workOrderNo: d.workOrderNo,
      supplierName: d.supplierName,
      productCode: d.productCode,
      productName: d.productName,
      specModel: d.specModel,
      outQty: d.outQty,
      unitLabel: unitLabel,
      unitPrice: d.unitPrice,
      totalAmount: d.totalAmount,
      outDate: d.outDate,
      planBackDate: d.planBackDate
    };
    sendOutOpen.value = true;
  });
}
function submitSendOut() {
  proxy.$refs["sendOutFormRef"].validate(v => {
    if (v) {
      sendOutOutsource(sendOutForm.value.outsourceId, { outDate: sendOutForm.value.outDate, planBackDate: sendOutForm.value.planBackDate }).then(() => {
        proxy.$modal.msgSuccess('发料成功'); sendOutOpen.value = false; getList();
      });
    }
  });
}

// ===== 回厂验收 =====
const backAcceptOpen = ref(false);
const backAcceptForm = ref({});
const backAcceptRules = { actualBackDate: [{ required: true, message: '请选择实际回厂日期', trigger: 'change' }], backQty: [{ required: true, message: '请输入回厂数量', trigger: 'blur' }] };
const computedLossQty = computed(() => {
  if (backAcceptForm.value.outQty != null && backAcceptForm.value.backQty != null) {
    const out = parseFloat(backAcceptForm.value.outQty);
    const back = parseFloat(backAcceptForm.value.backQty);
    if (isNaN(out) || isNaN(back)) return 0;
    const loss = out - back;
    return loss >= 0 ? Math.round(loss * 100) / 100 : 0;
  }
  return 0;
});
const computedLossRate = computed(() => {
  if (backAcceptForm.value.outQty != null && backAcceptForm.value.outQty > 0 && backAcceptForm.value.backQty != null) {
    const out = parseFloat(backAcceptForm.value.outQty);
    const back = parseFloat(backAcceptForm.value.backQty);
    if (isNaN(out) || isNaN(back) || out === 0) return '0.00';
    const rate = ((out - back) / out * 100).toFixed(2);
    return parseFloat(rate) >= 0 ? rate : '0.00';
  }
  return '0.00';
});
function handleBackAccept(row) {
  // 获取完整数据，加载完整外协信息到弹窗
  getOutsource(row.outsourceId).then(res => {
    const d = res.data;
    // 转换单位编码为可读文本
    let unitLabel = d.unit;
    if (wms_unit.value && d.unit) {
      const u = wms_unit.value.find(x => x.value === d.unit);
      if (u) unitLabel = u.label;
    }
    backAcceptForm.value = {
      outsourceId: d.outsourceId,
      outsourceNo: d.outsourceNo,
      workOrderNo: d.workOrderNo,
      supplierName: d.supplierName,
      productCode: d.productCode,
      productName: d.productName,
      specModel: d.specModel,
      outQty: d.outQty,
      unitLabel: unitLabel,
      unitPrice: d.unitPrice,
      totalAmount: d.totalAmount,
      outDate: d.outDate,
      planBackDate: d.planBackDate,
      actualBackDate: d.actualBackDate,
      backQty: d.backQty
    };
    backAcceptOpen.value = true;
  });
}
function submitBackAccept() {
  proxy.$refs["backAcceptFormRef"].validate(v => {
    if (v) {
      backAcceptOutsource(backAcceptForm.value.outsourceId, { actualBackDate: backAcceptForm.value.actualBackDate, backQty: backAcceptForm.value.backQty }).then(() => {
        proxy.$modal.msgSuccess('回厂验收成功'); backAcceptOpen.value = false; getList();
      });
    }
  });
}

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
  form.value.productId = row.productId;
  form.value.productCode = row.productCode;
  form.value.productName = row.productName;
  form.value.specModel = row.specModel;
  form.value.unit = row.unit;
  form.value.planQty = row.planQty;
  form.value.opSeq = undefined;
  form.value.processName = undefined;
  processOptions.value = [];
  woPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo');
  listWorkOrderProcesses(row.workOrderId).then(res => { processOptions.value = res.data || []; }).catch(() => {});
}

// ===== 工序选择器 =====
const procPickerOpen = ref(false); const procPickerLoading = ref(false); const procPickerSelectedId = ref(null); const procPickerSelectedRow = ref(null);
const processOptions = ref([]);
function openProcPicker() {
  if (!form.value.workOrderId) { proxy.$modal.msgWarning('请先选择工单'); return; }
  procPickerOpen.value = true;
  procPickerSelectedId.value = form.value.opSeq ? form.value.opSeq : null;
  procPickerSelectedRow.value = null;
  if (processOptions.value.length === 0) {
    procPickerLoading.value = true;
    listWorkOrderProcesses(form.value.workOrderId).then(res => {
      processOptions.value = res.data || [];
      procPickerLoading.value = false;
    }).catch(() => { procPickerLoading.value = false; });
  }
}
function onProcRowClick(row) { procPickerSelectedId.value = row.snapshotId; procPickerSelectedRow.value = row; }
function onProcRowDblClick(row) { onProcRowClick(row); handleProcPickerConfirm(); }
function handleProcPickerConfirm() {
  if (!procPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工序'); return; }
  const row = procPickerSelectedRow.value;
  form.value.opSeq = row.stepSeq;
  form.value.processName = row.processName;
  procPickerOpen.value = false;
}

// ===== 供应商选择器 =====
const supplierPickerOpen = ref(false); const supplierPickerLoading = ref(false); const supplierPickerList = ref([]); const supplierPickerTotal = ref(0); const supplierPickerSelectedId = ref(null); const supplierPickerSelectedRow = ref(null);
const supplierPickerQuery = reactive({ pageNum: 1, pageSize: 10, supplierCode: undefined, supplierName: undefined, status: '0' });
function openSupplierPicker() { supplierPickerOpen.value = true; supplierPickerSelectedId.value = form.value.supplierId || null; supplierPickerSelectedRow.value = null; supplierPickerQuery.pageNum = 1; supplierPickerQuery.supplierCode = undefined; supplierPickerQuery.supplierName = undefined; getSupplierPickerList(); }
function getSupplierPickerList() { supplierPickerLoading.value = true; listSupplier(supplierPickerQuery).then(res => { supplierPickerList.value = res.rows; supplierPickerTotal.value = res.total; supplierPickerLoading.value = false; }).catch(() => { supplierPickerLoading.value = false; }); }
function handleSupplierPickerQuery() { supplierPickerQuery.pageNum = 1; getSupplierPickerList(); }
function resetSupplierPickerQuery() { supplierPickerQuery.supplierCode = undefined; supplierPickerQuery.supplierName = undefined; handleSupplierPickerQuery(); }
function onSupplierRowClick(row) { supplierPickerSelectedId.value = row.supplierId; supplierPickerSelectedRow.value = row; }
function onSupplierRowDblClick(row) { onSupplierRowClick(row); handleSupplierPickerConfirm(); }
function handleSupplierPickerConfirm() {
  if (!supplierPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择供应商'); return; }
  const row = supplierPickerSelectedRow.value;
  form.value.supplierId = row.supplierId;
  form.value.supplierName = row.supplierName;
  supplierPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('supplierName');
}

getList();
</script>

<style scoped>
.mms-outsource-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-outsource-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-outsource-page .filter-card { padding:14px 20px 16px; }
.mms-outsource-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-outsource-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-outsource-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-outsource-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-outsource-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-outsource-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-outsource-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-outsource-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-outsource-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-outsource-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-outsource-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-outsource-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-outsource-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-outsource-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-outsource-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-outsource-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-outsource-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-outsource-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-outsource-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-outsource-page .field .control :deep(.el-select) { width:100%; }
.mms-outsource-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-outsource-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-outsource-page .toolbar .left, .mms-outsource-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-outsource-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-outsource-page .table-wrap { overflow-x:auto; }
.mms-outsource-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-outsource-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-outsource-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-outsource-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-outsource-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-outsource-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-outsource-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-outsource-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-outsource-page .badge.amber .dot { background:var(--amber-500); }
.mms-outsource-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-outsource-page .badge.blue .dot { background:var(--blue-500); }
.mms-outsource-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-outsource-page .badge.green .dot { background:var(--green-500); }
.mms-outsource-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-outsource-page .badge.gray .dot { background:var(--ink-400); }
.mms-outsource-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-outsource-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-outsource-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-outsource-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-outsource-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-outsource-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-outsource-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-outsource-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-outsource-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-outsource-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-outsource-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-outsource-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-outsource-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-outsource-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-outsource-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-outsource-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-outsource-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-outsource-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-outsource-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-outsource-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-outsource-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-outsource-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-outsource-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.mms-outsource-page .text-muted { color:var(--ink-400); }
.mms-outsource-page .rd-page { max-width: 912px; margin: 0 auto; }
.mms-outsource-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.mms-outsource-page .rd-item { display: flex; align-items: center; gap: 12px; }
.mms-outsource-page .rd-item--full { grid-column: 1 / -1; }
.mms-outsource-page .rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.mms-outsource-page .rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.mms-outsource-page .rd-value--muted { color: #9ca3af; font-style: italic; }
.mms-outsource-page .rd-value--large { font-size: 18px; font-weight: 700; }
.mms-outsource-page .rd-amount { font-variant-numeric: tabular-nums; font-weight: 700; color: #dc2626; }
.mms-outsource-page .app-table .rd-amount { font-size: 13px; }
.mms-outsource-page .app-table .rd-amount.rd-value--large { font-size: 15px; }
.mms-outsource-page .rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.mms-outsource-page .rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.mms-outsource-page .rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; }
.mms-outsource-page .rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; }
.mms-outsource-page .rd-detail-header-sub { display: flex; align-items: center; gap: 8px; }
.mms-outsource-page .rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); }
.mms-outsource-page .rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); }
.mms-outsource-page .rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.mms-outsource-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.mms-outsource-page .rd-card-header:hover { background: #f1f5f9; }
.mms-outsource-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.mms-outsource-page .rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.mms-outsource-page .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.mms-outsource-page .rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.mms-outsource-page .rd-card-body { padding: 16px; }
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
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }
.rd-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; margin-bottom: 12px; overflow: hidden; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; cursor: pointer; background: #f8fafc; border-bottom: 1px solid #e5e7eb; }
.rd-card-header:hover { background: #f1f5f9; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #334155; }
.rd-card-icon { display: flex; align-items: center; color: #6366f1; }
.rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; cursor: pointer; color: #94a3b8; transition: transform .2s; }
.rd-collapse-btn.is-collapsed { transform: rotate(180deg); }
.rd-card-body { padding: 16px; }
.rd-page { max-width: 912px; margin: 0 auto; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
.rd-value--large { font-size: 18px; font-weight: 700; }
.rd-amount { font-variant-numeric: tabular-nums; font-weight: 700; color: #dc2626; }
@media (max-width: 1100px) { .mms-outsource-page .filter-card .filter-bar { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 720px) { .mms-outsource-page .filter-card .filter-bar { grid-template-columns: 1fr; } }
.material-picker { display: flex; flex-direction: column; }
.material-picker-search { display: flex; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 4px; }
.material-picker-table { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.material-picker-table :deep(.el-table__row) { cursor: pointer; }
.material-picker-table :deep(.el-table__row:hover > td) { background: #f0f7ff; }
.material-picker-table :deep(.el-table__row.is-current > td) { background: #e6f0fd; }
.material-picker-pager { margin-top: 8px; display: flex; justify-content: flex-end; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
