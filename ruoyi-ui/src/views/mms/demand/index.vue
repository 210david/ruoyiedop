<template>
  <div class="app-container mms-demand-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>需求编号</label><div class="control"><el-input v-model="queryParams.demandNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>产品编码</label><div class="control"><el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品名称</label><div class="control"><el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>类型</label><div class="control is-select"><el-select v-model="queryParams.demandType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_demand_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_demand_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>优先级</label><div class="control is-select"><el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_priority" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>客户名称</label><div class="control"><el-input v-model="queryParams.customerName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="{ 'is-active': activeStatusTab === s.value }" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:demand:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:demand:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:demand:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="primary" plain icon="Check" :disabled="multiple" @click="handleBatchConfirm" v-hasPermi="['mms:demand:confirm']">批量确认</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:demand:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_demand_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="需求编号" prop="demandNo" key="demandNo" :width="colWidth('demandNo', 140)" resizable v-if="columns.demandNo.visible" /><el-table-column label="类型" prop="demandType" key="demandType" :width="colWidth('demandType', 100)" resizable align="center" v-if="columns.demandType.visible"><template #default="scope"><span v-if="scope.row.demandType" class="badge blue"><span class="dot"></span>{{ dictLabel(mms_demand_type, scope.row.demandType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" /><el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" /><el-table-column label="需求数量" prop="demandQty" key="demandQty" :width="colWidth('demandQty', 100)" resizable align="center" v-if="columns.demandQty.visible" /><el-table-column label="需求日期" prop="requireDate" key="requireDate" :width="colWidth('requireDate', 120)" resizable align="center" v-if="columns.requireDate.visible" /><el-table-column label="优先级" prop="priority" key="priority" :width="colWidth('priority', 90)" resizable align="center" v-if="columns.priority.visible"><template #default="scope"><span v-if="scope.row.priority" class="badge" :class="priorityBadgeClass(scope.row.priority)"><span class="dot"></span>{{ dictLabel(mms_priority, scope.row.priority) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="客户名称" prop="customerName" key="customerName" :width="colWidth('customerName', 150)" resizable show-overflow-tooltip v-if="columns.customerName.visible" /><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="340" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button><el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:demand:edit']">修改</el-button><el-button v-if="scope.row.status === '0'" link type="success" icon="Check" @click="handleConfirm(scope.row)" v-hasPermi="['mms:demand:confirm']">确认</el-button><el-button v-if="scope.row.status === '1'" link type="warning" icon="RefreshRight" @click="handleUnconfirm(scope.row)" v-hasPermi="['mms:demand:confirm']">取消确认</el-button><el-button v-if="scope.row.status === '1'" link type="primary" icon="Promotion" @click="handleToMps(scope.row)" v-hasPermi="['mms:demand:toplan']">转计划</el-button><el-button v-if="scope.row.status === '0'" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:demand:remove']">删除</el-button></template></el-table-column></el-table></div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="936px" append-to-body draggable class="rd-dialog">
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="需求编号" prop="demandNo"><el-input v-model="form.demandNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="需求类型" prop="demandType"><el-select v-model="form.demandType" placeholder="请选择" style="width: 100%" @change="onDemandTypeChange"><el-option v-for="d in mms_demand_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="来源单号" prop="sourceOrderNo"><el-input v-model="form.sourceOrderNo" readonly placeholder="选择订单后自动带出" @click="openOrderPicker"><template #append><el-button icon="Search" @click="openOrderPicker" /></template><template #suffix><el-icon v-if="form.sourceOrderNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearOrder"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-input :model-value="statusLabel(form.status)" disabled placeholder="系统自动维护" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编码" prop="productCode"><el-input v-model="form.productCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.productCode" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称"><el-input v-model="form.productName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="规格型号"><el-input v-model="form.specModel" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col><el-col :span="12"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></span>需求计划</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="需求数量" prop="demandQty"><el-input-number v-model="form.demandQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="需求日期" prop="requireDate"><el-date-picker v-model="form.requireDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="优先级" prop="priority"><el-select v-model="form.priority" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_priority" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="客户名称" prop="customerName"><el-input v-model="form.customerName" readonly placeholder="请选择客户" style="width: 100%" @click="openCustomerPicker"><template #append><el-button icon="Search" @click="openCustomerPicker" /></template><template #suffix><el-icon v-if="form.customerName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearCustomer"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
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
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">生产需求详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.demandNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.demandNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">需求编号</span><div class="rd-value">{{ viewData.demandNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><dict-tag :options="mms_demand_type" :value="viewData.demandType" /></div></div><div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ viewData.sourceOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_demand_status" :value="viewData.status" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg></span>需求计划</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">需求数量</span><div class="rd-value">{{ viewData.demandQty != null ? viewData.demandQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">需求日期</span><div class="rd-value">{{ viewData.requireDate || '-' }}</div></div><div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="mms_priority" :value="viewData.priority" /></div></div><div class="rd-item"><span class="rd-label">客户名称</span><div class="rd-value">{{ viewData.customerName || '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 销售订单选择弹窗 ===== -->
    <el-dialog v-model="orderPickerOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/></svg></div>
          <span class="rd-detail-header-title">选择销售订单</span>
        </div>
      </template>
      <div class="material-picker">
        <div class="material-picker-search">
          <el-input v-model="orderPickerQuery.orderNo" placeholder="订单编号" clearable size="small" style="width: 180px" @keyup.enter="handleOrderPickerQuery">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleOrderPickerQuery">查询</el-button>
          <el-button icon="RefreshLeft" size="small" @click="resetOrderPickerQuery">重置</el-button>
        </div>
        <div class="material-picker-table order-picker-table">
          <el-table ref="orderTableRef" v-loading="orderPickerLoading" :data="orderPickerFlatList" highlight-current-row @row-click="onOrderRowClick" @row-dblclick="onOrderRowDblClick" height="380" size="small" :span-method="onOrderSpanMethod" :row-class-name="orderRowClassName" empty-text="暂无已确认的订单明细">
            <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="orderPickerSelectedId" :value="row._rowKey" @click.stop="onOrderRadioClick(row)"><span /></el-radio></template></el-table-column>
            <el-table-column label="订单编号" prop="orderNo" width="140" show-overflow-tooltip />
            <el-table-column label="客户名称" prop="customerName" min-width="120" show-overflow-tooltip />
            <el-table-column label="订单金额" prop="orderAmount" width="100" align="center" />
            <el-table-column label="行号" prop="lineNo" width="60" align="center" />
            <el-table-column label="产品名称" prop="productName" min-width="140" show-overflow-tooltip />
            <el-table-column label="规格型号" prop="productSpec" width="120" show-overflow-tooltip />
            <el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
            <el-table-column label="订单数量" prop="quantity" width="90" align="center" />
          </el-table>
        </div>
        <div class="material-picker-pager">
          <el-pagination v-model:current-page="orderPickerQuery.pageNum" v-model:page-size="orderPickerQuery.pageSize" :total="orderPickerTotal" layout="total, prev, pager, next" small @current-change="getOrderPickerList" />
        </div>
      </div>
      <template #footer><el-button @click="orderPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleOrderPickerConfirm" :disabled="!orderPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 转计划确认弹窗 ===== -->
    <el-dialog v-model="toMpsOpen" width="560px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2L11 13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></div>
          <span class="rd-detail-header-title">需求转计划</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="需求编号">{{ toMpsRow.demandNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ toMpsRow.productName }}</el-descriptions-item>
        <el-descriptions-item label="产品编码">{{ toMpsRow.productCode }}</el-descriptions-item>
        <el-descriptions-item label="需求数量">{{ toMpsRow.demandQty }} {{ dictLabel(wms_unit, toMpsRow.unit) }}</el-descriptions-item>
        <el-descriptions-item label="需求日期">{{ toMpsRow.requireDate }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ dictLabel(mms_priority, toMpsRow.priority) }}</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 16px; padding: 12px 16px; background: #f0f9ff; border-radius: 8px; border-left: 4px solid #409eff; font-size: 13px; color: #606266; line-height: 1.8;">
        系统将根据此需求自动创建一条主生产计划（MPS草稿状态）：<br/>
        • 计划产量 = 需求数量<br/>
        • 计划完工日期 = 需求日期<br/>
        • 计划开工日期 = 需求日期前推7天（可后续在MPS中调整）<br/>
        • 需求状态将变更为"已排产"
      </div>
      <template #footer>
        <el-button @click="toMpsOpen = false">取 消</el-button>
        <el-button type="primary" :loading="toMpsLoading" @click="submitToMps">确认转计划</el-button>
      </template>
    </el-dialog>

    <!-- ===== 物料选择器（仅半成品和成品） ===== -->
    <material-picker ref="materialPickerRef" title="选择产品物料" :material-types="['1','2']" @confirm="onMaterialPickerConfirm" />
    <!-- ===== 客户选择器 ===== -->
    <customer-picker ref="customerPickerRef" title="选择客户" @confirm="onCustomerPickerConfirm" />

    <!-- ===== 业务操作说明 ===== -->
    <el-dialog v-model="showStatusHelp" title="生产需求业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、生产需求释义 -->
        <h4>一、生产需求释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是生产需求？</div>
          <div class="highlight-card-body">
            <strong>生产需求（Manufacturing Demand）</strong>是生产管控模块中用于汇集各类生产需求来源的统一单据，作为主生产计划（MPS）的输入源。通过将销售订单、销售预测、安全库存补库等多来源需求统一归集，形成排产需求池，确保生产计划的准确性和完整性。<br/><br/>
            生产需求管理遵循<strong>MRP（物料需求计划）</strong>理论体系，以独立需求（销售订单、预测）为源头，结合安全库存策略，驱动后续的排产计划和物料需求计算。符合 GB/T 31807-2015《制造业物料需求计划（MRP）系统应用规范》中对需求归集和计划生成的要求。
          </div>
        </div>

        <!-- 二、状态流转图 -->
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「确认」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已确认</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="success">点击「转计划」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已排产</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="primary">已确认</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「取消」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已取消</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="success">已排产</el-tag>
            <el-tag size="small" type="warning">需先解除排产关联</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">已取消</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">新增需求后的默认状态。可编辑、删除。点击「确认」后转入已确认状态，可参与排产</el-descriptions-item>
          <el-descriptions-item label="已确认">需求信息已确认，可参与排产转计划。可取消确认回到草稿状态，也可点击「转计划」生成MPS</el-descriptions-item>
          <el-descriptions-item label="已排产">需求已被排产计划（MPS）引用，由系统自动置为此状态。取消前需先解除与MPS的排产关联</el-descriptions-item>
          <el-descriptions-item label="已取消">需求不再有效时手动取消。草稿和已确认状态可直接取消；已排产的需求需先解除排产关联后才能取消</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>需求编号：</strong>保存后由系统自动生成，不可手动编辑</p>
            <p>• <strong>需求类型：</strong>包括销售订单、预测、补库、插单等多种来源类型<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>来源单号：</strong>需求类型为"订单"时，点击选择框从销售管理模块的已确认订单中选择，自动带出订单信息</p>
            <p>• <strong>状态：</strong>由系统自动维护，新增时默认为"草稿"</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">产品信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>产品编码：</strong>从物料主数据中选择，点击选择框打开物料选择器<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品名称：</strong>选择物料后自动带出，不可手动编辑</p>
            <p>• <strong>规格型号：</strong>选择物料后自动带出，不可手动编辑</p>
            <p>• <strong>单位：</strong>从字典中选择计量单位<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">需求计划区</div>
          <div class="highlight-card-body">
            <p>• <strong>需求数量：</strong>填写客户要求的需求数量，支持两位小数<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>需求日期：</strong>选择客户要求的交货日期，用于排产参考<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>优先级：</strong>分为高、中、低三个等级，影响排产顺序<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>客户名称：</strong>从客户库中选择，自动带出客户编号<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">其他信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>备注：</strong>选填，记录需求的补充说明信息</p>
          </div>
        </div>

        <!-- 五、核心业务规则 -->
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">需求来源规则</div>
          <div class="highlight-card-body">
            <p>• <strong>销售订单需求：</strong>需求类型为"订单"时，须从销售管理模块的已确认订单中选择来源单号，系统自动带出产品、客户等信息</p>
            <p>• <strong>预测需求：</strong>基于销售预测创建，无需关联来源订单</p>
            <p>• <strong>补库需求：</strong>基于安全库存策略自动触发，记录补库目标数量</p>
            <p>• <strong>插单需求：</strong>临时紧急需求，优先级通常设为"高"</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">排产与转计划规则</div>
          <div class="highlight-card-body">
            <p>• <strong>转计划条件：</strong>只有"已确认"状态的需求才能转为主生产计划（MPS）</p>
            <p>• <strong>自动创建MPS：</strong>转计划时系统自动创建一条MPS草稿，计划产量=需求数量，计划完工日期=需求日期</p>
            <p>• <strong>计划开工日期：</strong>系统按需求日期前推7天生成计划开工日期，可在MPS中调整</p>
            <p>• <strong>状态自动变更：</strong>转计划成功后，需求状态自动变更为"已排产"</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>已排产的需求如需修改或取消，须先在MPS模块中解除排产关联。</p>
          </div>
        </div>
        <div class="highlight-card highlight-danger" style="margin-top: 12px;">
          <div class="highlight-card-title">取消与删除规则</div>
          <div class="highlight-card-body">
            <p>• <strong>可取消状态：</strong>草稿、已确认状态的需求可直接取消</p>
            <p>• <strong>已排产取消：</strong>须先在MPS中解除排产关联后，方可取消</p>
            <p>• <strong>可删除状态：</strong>仅草稿状态的需求可删除</p>
            <p>• <strong>批量确认：</strong>仅草稿状态的需求可批量确认，非草稿状态会被自动过滤</p>
          </div>
        </div>

        <!-- 六、MRP理论说明 -->
        <h4>六、MRP需求管理说明</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是MRP需求归集？</div>
          <div class="highlight-card-body">
            <strong>MRP（Material Requirements Planning，物料需求计划）</strong>是制造业生产管控的核心方法论，通过将独立需求（销售订单、预测）和相关需求（BOM展开后的子件需求）统一归集，驱动生产计划和采购计划的形成。本系统的生产需求管理对应MRP理论中的<strong>独立需求归集</strong>环节，是整个计划链条的起点。
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">优先级与排产策略</div>
          <div class="highlight-card-body">
            <p>系统根据需求优先级自动安排排产顺序：</p>
            <p>• <strong>高优先级：</strong>紧急订单、插单需求，优先安排产能</p>
            <p>• <strong>中优先级：</strong>常规销售订单，按需求日期顺序排产</p>
            <p>• <strong>低优先级：</strong>补库需求、预测需求，在产能充裕时安排</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>需求日期是排产的重要参考，系统会根据优先级和需求日期综合计算排产优先级。</p>
          </div>
        </div>

        <!-- 七、业务操作流程 -->
        <h4>七、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建需求：</strong>点击「新增」创建生产需求，选择需求类型，填写产品信息（从物料库选择）、需求数量、需求日期、优先级（从客户库选择客户），保存后编号自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>确认需求：</strong>草稿状态下点击「确认」，需求进入已确认状态，可参与排产。支持批量确认多条草稿需求
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>转计划：</strong>已确认的需求点击「转计划」，系统自动创建主生产计划（MPS草稿），需求状态变为已排产。计划产量=需求数量，计划完工日期=需求日期
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>取消确认：</strong>已确认但尚未转计划的需求，可点击「取消确认」回到草稿状态，重新编辑
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>取消/删除需求：</strong>不再有效的需求可取消；仅草稿状态的需求可删除。已排产的需求须先解除MPS关联后才能取消
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Demand">
import { listDemand, getDemand, addDemand, updateDemand, delDemand, confirmDemand, unconfirmDemand, demandToMps } from "@/api/mms/demand";
import { listOrder, getOrder } from "@/api/mk/order";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowDown, ArrowRight, QuestionFilled, CircleClose, Check, Promotion, RefreshRight } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_demand_type, mms_priority, mms_demand_status, wms_unit } = proxy.useDict("mms_demand_type", "mms_priority", "mms_demand_status", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_demand_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);

/* ===== 选择器相关 ===== */
const materialPickerRef = ref()
const customerPickerRef = ref()
const orderPickerOpen = ref(false)
const orderPickerLoading = ref(false)
const orderPickerTotal = ref(0)
const orderPickerSelectedId = ref(null)
const orderPickerSelectedRow = ref(null)
const orderTableRef = ref()
const orderPickerQuery = reactive({ pageNum: 1, pageSize: 10, orderNo: undefined, orderStatus: '2' })
const orderPickerFlatList = ref([])

const statusTabList = computed(() => mms_demand_status.value ? mms_demand_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { demandNo: { label: '需求编号', visible: true }, demandType: { label: '类型', visible: true }, productCode: { label: '产品编码', visible: true }, productName: { label: '产品名称', visible: true }, demandQty: { label: '需求数量', visible: true }, requireDate: { label: '需求日期', visible: true }, priority: { label: '优先级', visible: true }, customerName: { label: '客户名称', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_demand_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.demandNo) c++; if (queryParams.value.productCode) c++; if (queryParams.value.productName) c++; if (queryParams.value.demandType) c++; if (queryParams.value.status) c++; if (queryParams.value.priority) c++; if (queryParams.value.customerName) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, demandNo: undefined, demandType: undefined, productCode: undefined, productName: undefined, status: undefined, priority: undefined, customerName: undefined, params: {} }, rules: {
  demandType: [{ required: true, message: '请选择需求类型', trigger: 'change' }],
  productCode: [{ required: true, message: '请选择产品编码', trigger: 'change' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  demandQty: [{ required: true, message: '请输入需求数量', trigger: 'blur' }],
  requireDate: [{ required: true, message: '请选择需求日期', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  customerName: [{ required: true, message: '请选择客户', trigger: 'change' }]
} });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listDemand(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.demandNo) baseQuery.demandNo = queryParams.value.demandNo; if (queryParams.value.demandType) baseQuery.demandType = queryParams.value.demandType; if (queryParams.value.productCode) baseQuery.productCode = queryParams.value.productCode; if (queryParams.value.productName) baseQuery.productName = queryParams.value.productName; if (queryParams.value.priority) baseQuery.priority = queryParams.value.priority; if (queryParams.value.customerName) baseQuery.customerName = queryParams.value.customerName; listDemand(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_demand_status.value) { mms_demand_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.demandNo = undefined; queryParams.value.demandType = undefined; queryParams.value.productCode = undefined; queryParams.value.productName = undefined; queryParams.value.status = undefined; queryParams.value.priority = undefined; queryParams.value.customerName = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.demandId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { demandNo: undefined, demandType: undefined, sourceOrderNo: undefined, productId: undefined, productCode: undefined, productName: undefined, specModel: undefined, unit: undefined, demandQty: undefined, requireDate: undefined, priority: undefined, customerId: undefined, customerNo: undefined, customerName: undefined, status: undefined, remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); form.value.status = '0'; open.value = true; title.value = "新增生产需求"; }
function handleUpdate(row) { reset(); const id = row.demandId || ids.value[0]; getDemand(id).then(response => { form.value = response.data; open.value = true; title.value = "修改生产需求"; }); }
function handleView(row) { const id = row.demandId || ids.value[0]; getDemand(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.demandId != null) { updateDemand(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addDemand(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.demandId || ids.value; proxy.$modal.confirm('是否确认删除选中的生产需求？').then(() => delDemand(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/demand/export", { ...queryParams.value }, `demand_${new Date().getTime()}.xlsx`); }

/* ===== 需求确认 ===== */
function handleConfirm(row) {
  proxy.$modal.confirm('是否确认需求"' + row.demandNo + '"？确认后可参与排产转计划。').then(() => confirmDemand(row.demandId)).then(() => { getList(); proxy.$modal.msgSuccess("确认成功"); }).catch(() => {});
}

/* ===== 批量确认 ===== */
function handleBatchConfirm() {
  const selectedRows = dataList.value.filter(r => ids.value.includes(r.demandId) && r.status === '0');
  if (selectedRows.length === 0) {
    proxy.$modal.msgWarning('请选择草稿状态的需求');
    return;
  }
  proxy.$modal.confirm('是否批量确认选中的 ' + selectedRows.length + ' 条草稿需求？').then(() => {
    const promises = selectedRows.map(r => confirmDemand(r.demandId));
    return Promise.all(promises);
  }).then(() => { getList(); proxy.$modal.msgSuccess("批量确认成功"); }).catch(() => {});
}

/* ===== 取消确认 ===== */
function handleUnconfirm(row) {
  proxy.$modal.confirm('是否取消确认需求"' + row.demandNo + '"？取消后将回到草稿状态。').then(() => unconfirmDemand(row.demandId)).then(() => { getList(); proxy.$modal.msgSuccess("取消确认成功"); }).catch(() => {});
}

/* ===== 转计划 ===== */
const toMpsOpen = ref(false);
const toMpsRow = ref({});
const toMpsLoading = ref(false);
function handleToMps(row) {
  toMpsRow.value = row;
  toMpsOpen.value = true;
}
function submitToMps() {
  proxy.$modal.confirm('是否将需求"' + toMpsRow.value.demandNo + '"转为主生产计划？系统将自动创建MPS草稿。').then(() => {
    toMpsLoading.value = true;
    return demandToMps(toMpsRow.value.demandId);
  }).then(res => {
    toMpsLoading.value = false;
    toMpsOpen.value = false;
    getList();
    proxy.$modal.msgSuccess("转计划成功，已创建MPS：" + (res.data ? res.data.mpsNo : ''));
  }).catch(() => { toMpsLoading.value = false; });
}
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_demand_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'gray' }; return map[status] || 'gray'; }
function priorityBadgeClass(priority) { const map = { '0': 'red', '1': 'amber', '2': 'blue' }; return map[priority] || 'gray'; }

/* ===== 需求类型变更：订单类型时提示选择来源订单 ===== */
function onDemandTypeChange(val) {
  if (val === '0') {
    // 订单类型，可选择来源订单
  } else {
    // 非订单类型，清空来源单号
    form.value.sourceOrderNo = undefined
  }
}

/* ===== 销售订单选择器（订单 + 明细行展平到单表） ===== */
function openOrderPicker() {
  orderPickerOpen.value = true
  orderPickerSelectedId.value = null
  orderPickerSelectedRow.value = null
  orderPickerFlatList.value = []
  _orderGroupIndexMap.value = {}
  orderPickerQuery.pageNum = 1
  orderPickerQuery.orderNo = undefined
  getOrderPickerList()
}

/** 查询订单列表 → 逐个加载明细 → 展平到一张表 */
function getOrderPickerList() {
  orderPickerLoading.value = true
  listOrder(orderPickerQuery).then(async res => {
    const orders = res.rows || []
    orderPickerTotal.value = res.total
    // 逐个获取订单明细，展平为行
    const flatRows = []
    for (const order of orders) {
      try {
        const detail = await getOrder(order.orderId)
        const items = (detail.data && detail.data.itemList) ? detail.data.itemList : []
        if (items.length === 0) {
          // 无明细的订单也展示一行（明细字段为空）
          flatRows.push({
            _rowKey: order.orderId + '_0',
            orderId: order.orderId,
            orderNo: order.orderNo,
            customerName: order.customerName,
            orderAmount: order.orderAmount,
            customerId: order.customerId,
            lineNo: null, productName: null, productSpec: null, unit: null, quantity: null,
            _orderRowspan: 1
          })
        } else {
          items.forEach((item, idx) => {
            flatRows.push({
              _rowKey: order.orderId + '_' + item.itemId,
              orderId: order.orderId,
              orderNo: order.orderNo,
              customerName: order.customerName,
              orderAmount: order.orderAmount,
              customerId: order.customerId,
              itemId: item.itemId,
              lineNo: item.lineNo,
              materialId: item.materialId,
              materialCode: item.materialCode,
              productName: item.productName,
              productSpec: item.productSpec,
              unit: item.unit,
              quantity: item.quantity,
              _orderRowspan: idx === 0 ? items.length : 0
            })
          })
        }
      } catch (e) {
        // 单个订单加载失败，跳过
      }
    }
    orderPickerFlatList.value = flatRows
    orderPickerLoading.value = false
  }).catch(() => {
    orderPickerLoading.value = false
    orderPickerFlatList.value = []
  })
}

function handleOrderPickerQuery() { orderPickerQuery.pageNum = 1; getOrderPickerList() }
function resetOrderPickerQuery() { orderPickerQuery.orderNo = undefined; handleOrderPickerQuery() }

/** radio 点击 → 选中行（与行点击逻辑一致） */
function onOrderRadioClick(row) {
  onOrderRowClick(row)
}

/** 行样式：不同订单交替背景色，便于区分 */
const _orderGroupIndexMap = ref({})
function orderRowClassName({ row }) {
  if (!row.orderId) return ''
  if (_orderGroupIndexMap.value[row.orderId] === undefined) {
    const usedIds = Object.keys(_orderGroupIndexMap.value)
    _orderGroupIndexMap.value[row.orderId] = usedIds.length % 2
  }
  return _orderGroupIndexMap.value[row.orderId] === 0 ? 'order-group-a' : 'order-group-b'
}

/** 行点击 → 选中（必须有明细行才算有效选择） */
function onOrderRowClick(row) {
  if (row.itemId == null) return
  orderPickerSelectedId.value = row._rowKey
  orderPickerSelectedRow.value = row
}
function onOrderRowDblClick(row) {
  if (row.itemId == null) return
  onOrderRowClick(row)
  handleOrderPickerConfirm()
}

/** 合并行：同一订单的 orderNo/customerName/orderAmount 合并显示 */
function onOrderSpanMethod({ row, columnIndex }) {
  if (columnIndex >= 1 && columnIndex <= 3) {
    // 订单编号(1)、客户名称(2)、订单金额(3) 列合并
    if (row._orderRowspan > 0) return { rowspan: row._orderRowspan, colspan: 1 }
    return { rowspan: 0, colspan: 0 }
  }
}

/** 确认选择：带出订单 + 产品 + 客户信息 */
function handleOrderPickerConfirm() {
  if (!orderPickerSelectedId.value) {
    proxy.$modal.msgWarning('请先选择订单明细行')
    return
  }
  const row = orderPickerSelectedRow.value
  // 来源单号
  form.value.sourceOrderNo = row.orderNo
  // 产品信息（从订单明细行带出）
  form.value.productId = row.materialId || undefined
  form.value.productCode = row.materialCode || undefined
  form.value.productName = row.productName || undefined
  form.value.specModel = row.productSpec || undefined
  form.value.unit = row.unit || undefined
  form.value.demandQty = row.quantity != null ? Number(row.quantity) : undefined
  // 客户信息（从订单带出）
  if (row.customerId) form.value.customerId = row.customerId
  if (row.customerName) form.value.customerName = row.customerName
  orderPickerOpen.value = false
}
function clearOrder() {
  form.value.sourceOrderNo = undefined
}

/* ===== 物料选择器 ===== */
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
}

/* ===== 客户选择器 ===== */
function openCustomerPicker() { customerPickerRef.value.open(form.value.customerId) }
function onCustomerPickerConfirm(customer) {
  form.value.customerId = customer.customerId
  form.value.customerNo = customer.customerNo
  form.value.customerName = customer.customerName
}
function clearCustomer() {
  form.value.customerId = undefined
  form.value.customerNo = undefined
  form.value.customerName = undefined
}

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-demand-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
/* ===== 订单选择弹窗：不同订单交替背景色 ===== */
.mms-demand-page :deep(.order-group-a > td) { background-color: #fafbff !important; }
.mms-demand-page :deep(.order-group-b > td) { background-color: #fff8f0 !important; }
.mms-demand-page :deep(.el-table__body tr.order-group-a:hover > td) { background-color: #eef2ff !important; }
.mms-demand-page :deep(.el-table__body tr.order-group-b:hover > td) { background-color: #fdf2e9 !important; }
.mms-demand-page :deep(.el-table__body tr.current-row > td) { background-color: #e0e7ff !important; }

/* ===== Tip Pill ===== */
.mms-demand-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:13px; font-weight:500; border-radius:999px; border:1px solid #fde68a; background:#fffaf0; color:#92400e; cursor:pointer; transition:all .15s ease; white-space:nowrap; flex-shrink:0; }
.mms-demand-page .tip-pill:hover { background:#fffbeb; border-color:#f59e0b; color:#78350f; }
.mms-demand-page .tip-pill .el-icon { font-size:14px; }

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
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
</style>