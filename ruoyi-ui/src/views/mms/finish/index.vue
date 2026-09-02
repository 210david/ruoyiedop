<template>
  <div class="app-container mms-finish-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>完工编号</label><div class="control"><el-input v-model="queryParams.finishNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品编码</label><div class="control"><el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>产品名称</label><div class="control"><el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>批次号</label><div class="control"><el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>完工人</label><div class="control"><el-input v-model="queryParams.finishBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:finish:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:finish:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:finish:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:finish:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_finish_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column type="index" label="序号" width="85" align="center" /><el-table-column label="完工编号" prop="finishNo" key="finishNo" :width="colWidth('finishNo', 140)" resizable v-if="columns.finishNo.visible" /><el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" /><el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" /><el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" /><el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" /><el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column><el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" /><el-table-column label="完工数量" prop="finishQty" key="finishQty" :width="colWidth('finishQty', 100)" resizable align="center" v-if="columns.finishQty.visible" /><el-table-column label="合格数量" prop="qualifiedQty" key="qualifiedQty" :width="colWidth('qualifiedQty', 100)" resizable align="center" v-if="columns.qualifiedQty.visible" /><el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 120)" resizable v-if="columns.batchNo.visible" /><el-table-column label="完工人" prop="finishBy" key="finishBy" :width="colWidth('finishBy', 90)" resizable align="center" v-if="columns.finishBy.visible" /><el-table-column label="完工时间" prop="finishTime" key="finishTime" :width="colWidth('finishTime', 160)" resizable align="center" v-if="columns.finishTime.visible"><template #default="scope"><span>{{ scope.row.finishTime ? parseTime(scope.row.finishTime) : '-' }}</span></template></el-table-column><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="入库时间" prop="inboundTime" key="inboundTime" :width="colWidth('inboundTime', 160)" resizable align="center" v-if="columns.inboundTime.visible"><template #default="scope"><span>{{ scope.row.inboundTime ? parseTime(scope.row.inboundTime) : '-' }}</span></template></el-table-column><el-table-column label="入库人" prop="inboundBy" key="inboundBy" :width="colWidth('inboundBy', 90)" resizable align="center" v-if="columns.inboundBy.visible" /><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column>          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="success" icon="Check" @click="handleConfirm(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['mms:finish:confirm']">入库</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['mms:finish:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['mms:finish:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column></el-table></div>
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="完工编号" prop="finishNo"><el-input v-model="form.finishNo" placeholder="自动生成" readonly /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo" :rules="[{ required: true, message: '请选择工单', trigger: 'change' }]"><el-input v-model="form.workOrderNo" readonly placeholder="请选择工单" style="width: 100%" @click="openWorkOrderPicker"><template #append><el-button icon="Search" @click="openWorkOrderPicker" /></template><template #suffix><el-icon v-if="form.workOrderNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearWorkOrder"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="产品编码"><el-input v-model="form.productCode" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col><el-col :span="8"><el-form-item label="产品名称"><el-input v-model="form.productName" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col><el-col :span="8"><el-form-item label="规格型号"><el-input v-model="form.specModel" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="单位"><el-select v-model="form.unit" disabled placeholder="选择工单后自动带出" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="8"><el-form-item label="计划生产数量"><el-input v-model="form.planQty" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>完工详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="完工数量" prop="finishQty"><el-input v-model="form.finishQty" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col><el-col :span="8"><el-form-item label="合格数量" prop="qualifiedQty"><el-input v-model="form.qualifiedQty" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col><el-col :span="8"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="完工人" prop="finishBy"><el-input v-model="form.finishBy" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col><el-col :span="12"><el-form-item label="完工时间" prop="finishTime"><el-input v-model="form.finishTime" readonly placeholder="选择工单后自动带出" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status" disabled style="width: 100%"><el-option v-for="d in mms_finish_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
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
          <span class="rd-detail-header-title">完工入库详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.finishNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.finishNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">完工编号</span><div class="rd-value">{{ viewData.finishNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="viewData.status" class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span><span v-else class="text-muted">—</span></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div><div class="rd-item"><span class="rd-label">计划生产数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>完工详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">完工数量</span><div class="rd-value">{{ viewData.finishQty != null ? viewData.finishQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.qualifiedQty != null ? viewData.qualifiedQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">完工人</span><div class="rd-value">{{ viewData.finishBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">完工时间</span><div class="rd-value">{{ viewData.finishTime ? parseTime(viewData.finishTime) : '-' }}</div></div><div class="rd-item"><span class="rd-label">入库时间</span><div class="rd-value">{{ viewData.inboundTime ? parseTime(viewData.inboundTime) : '-' }}</div></div><div class="rd-item"><span class="rd-label">入库人</span><div class="rd-value">{{ viewData.inboundBy || '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 工单选择弹框 ===== -->
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
            <el-table-column label="完工数量" prop="finishedQty" width="90" align="center" />
            <el-table-column label="合格数量" prop="qualifiedQty" width="90" align="center" />
            <el-table-column label="单位" prop="unit" width="60" align="center" />
            <el-table-column label="实际完工时间" prop="actualFinish" width="160" align="center"><template #default="scope"><span>{{ scope.row.actualFinish ? parseTime(scope.row.actualFinish) : '-' }}</span></template></el-table-column>
          </el-table>
        </div>
        <div class="material-picker-pager"><el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" /></div>
      </div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 入库确认弹窗 ===== -->
    <el-dialog v-model="confirmOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">入库确认</span>
          <div class="rd-detail-header-sub" v-if="confirmForm.finishNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ confirmForm.finishNo }}</span></div>
        </div>
      </template>
      <el-form ref="confirmFormRef" :model="confirmForm" label-width="120px" style="margin-top: 16px;">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">完工编号</span><div class="rd-value">{{ confirmForm.finishNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ confirmForm.workOrderNo || '—' }}</div></div>
            </div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>产品信息</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ confirmForm.productCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ confirmForm.productName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ confirmForm.specModel || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="confirmForm.unit" /></div></div>
            </div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/></svg></span>完工详情</div></div>
            <div class="rd-card-body" style="display:block"><div class="rd-grid">
              <div class="rd-item"><span class="rd-label">完工数量</span><div class="rd-value">{{ confirmForm.finishQty != null ? confirmForm.finishQty : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ confirmForm.qualifiedQty != null ? confirmForm.qualifiedQty : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">完工人</span><div class="rd-value">{{ confirmForm.finishBy || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">完工时间</span><div class="rd-value">{{ confirmForm.finishTime ? parseTime(confirmForm.finishTime) : '—' }}</div></div>
            </div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg></span>入库信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="入库时间" prop="inboundTime"><el-date-picker v-model="confirmForm.inboundTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择入库时间" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="confirmForm.batchNo" placeholder="请输入批次号" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="confirmForm.remark" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button @click="confirmOpen = false">取 消</el-button><el-button type="primary" @click="submitConfirm">确认入库</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="完工入库管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <h4>一、完工入库管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是完工入库管理？</div>
          <div class="highlight-card-body">
            <strong>完工入库管理（Finish Receipt）</strong>是生产管控中记录工单完工入库信息的单据。完工入库单关联生产工单，记录完工数量、合格数量、批次号等数据，是工单生命周期中"完工"环节的核心数据载体，支持生产进度追踪和成本核算。<br/><br/>
            完工入库管理遵循 <strong>MES 完工确认规范</strong>，通过数量校验（合格数量≤完工数量）确保数据准确性，工单完工时系统自动创建完工入库单，实现生产执行环节的自动化联动。
          </div>
        </div>

        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待入库</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="success">已入库</el-tag>
          </div>
        </div>

        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待入库">完工入库单创建后的初始状态。工单完工时系统自动创建完工入库单并设为待入库状态。此状态下可修改批次号、备注等信息</el-descriptions-item>
          <el-descriptions-item label="已入库">完工产品已正式入库，入库流程完成。数据归档，不可再修改</el-descriptions-item>
        </el-descriptions>

        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>自动创建：</strong>工单完工时系统自动创建完工入库单，无需手动新建。完工编号由系统按编号规则自动生成</p>
            <p>• <strong>数据联动：</strong>产品编码、产品名称、规格型号、单位、计划数量、完工数量、合格数量等字段从工单自动带出，不可手动修改</p>
            <p>• <strong>数量校验：</strong>合格数量不能大于完工数量</p>
            <p>• <strong>批次追溯：</strong>批次号用于产品批次追溯，建议填写以便后续质量追踪</p>
            <p>• <strong>状态流转：</strong>状态由系统流转控制，用户不可手动修改。待入库→已入库的流转由入库作业触发</p>
          </div>
        </div>

        <h4>五、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>完工编号：</strong>完工入库单的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>工单编号：</strong>关联的生产工单编号，点击搜索按钮选择已完工工单<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">产品信息区（自动带出）</div>
          <div class="highlight-card-body">
            <p>• <strong>产品编码 / 产品名称 / 规格型号：</strong>选择工单后自动带出，不可修改</p>
            <p>• <strong>单位：</strong>从工单自动带出，字典选择器只读</p>
            <p>• <strong>计划生产数量：</strong>从工单自动带出</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">完工详情区</div>
          <div class="highlight-card-body">
            <p>• <strong>完工数量 / 合格数量：</strong>从工单自动带出，不可修改</p>
            <p>• <strong>批次号：</strong>可手动填写，用于产品批次追溯</p>
            <p>• <strong>完工人 / 完工时间：</strong>从工单自动带出</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">其他信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>状态：</strong>完工入库单的当前状态，由系统控制，不可手动修改</p>
            <p>• <strong>备注：</strong>完工入库单的补充说明信息</p>
          </div>
        </div>

        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>工单完工：</strong>生产工单完工时，系统自动创建完工入库单，关联工单数据自动带出
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>补充信息：</strong>在完工入库单中填写批次号、备注等需要补充的信息
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>入库确认：</strong>完工产品入库后，状态自动从「待入库」流转为「已入库」
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>点击「查看」可查看完工入库单完整信息，包括基本信息、产品信息和完工详情
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FinishReceipt">
import { listFinishReceipt, getFinishReceipt, addFinishReceipt, updateFinishReceipt, delFinishReceipt, confirmFinishReceipt } from "@/api/mms/finish";
import { listWorkOrder } from "@/api/mms/workorder";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { wms_unit, mms_finish_status } = proxy.useDict("wms_unit", "mms_finish_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_finish_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const showStatusHelp = ref(false); const activeStatusTab = ref("all"); const statusCounts = ref({});
const confirmOpen = ref(false); const confirmForm = ref({});
const statusTabList = computed(() => { if (!mms_finish_status.value) return []; return mms_finish_status.value.map(d => ({ label: d.label, value: d.value })); });

const defaultColumns = { finishNo: { label: '完工编号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, productCode: { label: '产品编码', visible: true }, productName: { label: '产品名称', visible: true }, specModel: { label: '规格型号', visible: true }, unit: { label: '单位', visible: true }, planQty: { label: '计划数量', visible: true }, finishQty: { label: '完工数量', visible: true }, qualifiedQty: { label: '合格数量', visible: true }, batchNo: { label: '批次号', visible: true }, finishBy: { label: '完工人', visible: true }, finishTime: { label: '完工时间', visible: true }, status: { label: '状态', visible: true }, inboundTime: { label: '入库时间', visible: true }, inboundBy: { label: '入库人', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_finish_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.finishNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.productCode) c++; if (queryParams.value.productName) c++; if (queryParams.value.batchNo) c++; if (queryParams.value.finishBy) c++; if (queryParams.value.status) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, finishNo: undefined, workOrderNo: undefined, productCode: undefined, productName: undefined, batchNo: undefined, finishBy: undefined, status: undefined, params: {} }, rules: { workOrderNo: [{ required: true, message: "请选择工单", trigger: "change" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listFinishReceipt(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.finishNo) baseQuery.finishNo = queryParams.value.finishNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.productCode) baseQuery.productCode = queryParams.value.productCode; if (queryParams.value.productName) baseQuery.productName = queryParams.value.productName; if (queryParams.value.batchNo) baseQuery.batchNo = queryParams.value.batchNo; if (queryParams.value.finishBy) baseQuery.finishBy = queryParams.value.finishBy; listFinishReceipt(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_finish_status.value) { mms_finish_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.finishNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.productCode = undefined; queryParams.value.productName = undefined; queryParams.value.batchNo = undefined; queryParams.value.finishBy = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.finishId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { finishNo: undefined, workOrderId: undefined, workOrderNo: undefined, productId: undefined, productCode: undefined, productName: undefined, specModel: undefined, unit: undefined, planQty: undefined, finishQty: undefined, qualifiedQty: undefined, batchNo: undefined, finishBy: undefined, finishTime: undefined, status: '0', remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增完工"; }
function handleUpdate(row) { reset(); const id = row.finishId || ids.value[0]; getFinishReceipt(id).then(response => { form.value = response.data; open.value = true; title.value = "修改完工"; }); }
function handleView(row) { const id = row.finishId || ids.value[0]; getFinishReceipt(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.finishId != null) { updateFinishReceipt(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addFinishReceipt(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.finishId || ids.value; proxy.$modal.confirm('是否确认删除选中的完工记录？').then(() => delFinishReceipt(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/finish/export", { ...queryParams.value }, `finish_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_finish_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-warning', '1': 'tab-success' }; return map[value] || ''; }

// ===== 工单选择器 =====
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null);
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined, status: '3' });
function openWorkOrderPicker() { woPickerOpen.value = true; woPickerSelectedId.value = null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; woPickerQuery.status = '3'; getWoPickerList(); }
function getWoPickerList() { woPickerLoading.value = true; listWorkOrder(woPickerQuery).then(res => { woPickerList.value = res.rows; woPickerTotal.value = res.total; woPickerLoading.value = false; }).catch(() => { woPickerLoading.value = false; }); }
function handleWoPickerQuery() { woPickerQuery.pageNum = 1; getWoPickerList(); }
function resetWoPickerQuery() { woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; woPickerQuery.status = '3'; handleWoPickerQuery(); }
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
  form.value.finishQty = row.finishedQty;
  form.value.qualifiedQty = row.qualifiedQty;
  form.value.finishTime = row.actualFinish ? proxy.parseTime(row.actualFinish, '{y}-{m}-{d} {h}:{i}:{s}') : undefined;
  form.value.finishBy = row.updateBy || undefined;
  woPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo');
}
function clearWorkOrder() {
  form.value.workOrderId = undefined;
  form.value.workOrderNo = undefined;
  form.value.productId = undefined;
  form.value.productCode = undefined;
  form.value.productName = undefined;
  form.value.specModel = undefined;
  form.value.unit = undefined;
  form.value.planQty = undefined;
  form.value.finishQty = undefined;
  form.value.qualifiedQty = undefined;
  form.value.finishTime = undefined;
  form.value.finishBy = undefined;
}

// ===== 入库确认 =====
function handleConfirm(row) {
  const id = row.finishId || ids.value[0];
  getFinishReceipt(id).then(response => {
    confirmForm.value = response.data;
    confirmForm.value.inboundTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
    confirmOpen.value = true;
  });
}
function submitConfirm() {
  proxy.$modal.confirm('确认将该完工入库单状态变更为「已入库」？').then(() => {
    return confirmFinishReceipt(confirmForm.value.finishId, confirmForm.value);
  }).then(() => {
    proxy.$modal.msgSuccess("入库确认成功");
    confirmOpen.value = false;
    getList();
  }).catch(() => {});
}

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-finish-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
