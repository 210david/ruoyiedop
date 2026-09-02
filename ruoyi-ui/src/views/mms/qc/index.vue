<template>
  <div class="app-container mms-qc-page" style="max-width: 1680px;">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>质检编号</label><div class="control"><el-input v-model="queryParams.qcNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>工序</label><div class="control"><el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>类型</label><div class="control is-select"><el-select v-model="queryParams.qcType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_qc_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>检验结果</label><div class="control is-select"><el-select v-model="queryParams.qcResult" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_qc_result" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>检验人</label><div class="control"><el-input v-model="queryParams.qcBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button class="status-tab tab-draft" :class="{ 'is-active': activeStatusTab === 'pending' }" @click="handleStatusTabClick('pending')"><span class="dot"></span><span>待检验</span><span class="count">{{ statusCounts.pending || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[resultTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:qc:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:qc:edit']">修改</el-button><el-button type="primary" plain icon="Check" :disabled="single" @click="handleInspect" v-hasPermi="['mms:qc:edit']">质检</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:qc:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:qc:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_qc_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column type="index" label="序号" width="85" align="center" /><el-table-column label="质检编号" prop="qcNo" key="qcNo" :width="colWidth('qcNo', 140)" resizable v-if="columns.qcNo.visible" /><el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" /><el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 120)" resizable show-overflow-tooltip v-if="columns.productCode.visible" /><el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 160)" resizable show-overflow-tooltip v-if="columns.productName.visible" /><el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 120)" resizable show-overflow-tooltip v-if="columns.specModel.visible" /><el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 70)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column><el-table-column label="工单数量" prop="planQty" key="planQty" :width="colWidth('planQty', 90)" resizable align="center" v-if="columns.planQty.visible" /><el-table-column label="工序" prop="processName" key="processName" :width="colWidth('processName', 100)" resizable v-if="columns.processName.visible" /><el-table-column label="类型" prop="qcType" key="qcType" :width="colWidth('qcType', 100)" resizable align="center" v-if="columns.qcType.visible"><template #default="scope"><span v-if="scope.row.qcType" class="badge blue"><span class="dot"></span>{{ dictLabel(mms_qc_type, scope.row.qcType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="检验数量" prop="inspectQty" key="inspectQty" :width="colWidth('inspectQty', 100)" resizable align="center" v-if="columns.inspectQty.visible" /><el-table-column label="不良数" prop="defectQty" key="defectQty" :width="colWidth('defectQty', 80)" resizable align="center" v-if="columns.defectQty.visible"><template #default="scope"><span :style="{ color: scope.row.defectQty > 0 ? '#ef4444' : 'inherit', fontWeight: scope.row.defectQty > 0 ? 600 : 400 }">{{ scope.row.defectQty }}</span></template></el-table-column><el-table-column label="缺陷类型" prop="defectType" key="defectType" :width="colWidth('defectType', 110)" resizable align="center" v-if="columns.defectType.visible"><template #default="scope"><span v-if="scope.row.defectType">{{ dictLabel(mms_defect_type, scope.row.defectType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="检验结果" prop="qcResult" key="qcResult" :width="colWidth('qcResult', 100)" resizable align="center" v-if="columns.qcResult.visible"><template #default="scope"><span v-if="scope.row.qcResult" class="badge" :class="resultBadgeClass(scope.row.qcResult)"><span class="dot"></span>{{ dictLabel(mms_qc_result, scope.row.qcResult) }}</span><span v-else class="badge amber"><span class="dot"></span>待检验</span></template></el-table-column><el-table-column label="来源" key="source" :width="colWidth('source', 90)" resizable align="center" v-if="columns.source.visible"><template #default="scope"><el-tag v-if="isAutoGenerated(scope.row)" type="warning" size="small" effect="plain">自动生成</el-tag><el-tag v-else type="info" size="small" effect="plain">手动新增</el-tag></template></el-table-column><el-table-column label="检验人" prop="qcBy" key="qcBy" :width="colWidth('qcBy', 90)" resizable align="center" v-if="columns.qcBy.visible" /><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column>          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="success" icon="Check" @click="handleInspect(scope.row)" v-if="!scope.row.qcResult" v-hasPermi="['mms:qc:edit']">质检</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:qc:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:qc:remove']">删除</el-button>
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="质检编号" prop="qcNo"><el-input v-model="form.qcNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo" :rules="[{ required: true, message: '请选择工单', trigger: 'change' }]"><el-input v-model="form.workOrderNo" readonly placeholder="请选择工单" style="width: 100%" @click="openWorkOrderPicker"><template #append><el-button icon="Search" @click="openWorkOrderPicker" /></template><template #suffix><el-icon v-if="form.workOrderNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearWorkOrder"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序" prop="processName"><el-input v-model="form.processName" readonly placeholder="选填，首检/巡检时可选择" :disabled="!form.workOrderId" style="width: 100%" @click="openProcessPicker"><template #append><el-button icon="Search" :disabled="!form.workOrderId" @click="openProcessPicker" /></template><template #suffix><el-icon v-if="form.processName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearProcess"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="类型" prop="qcType"><el-select v-model="form.qcType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_qc_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-12 4 12 4 8-4z"/><path d="M4 7v6c0 2 4 4 8 4s8-2 8-4V7"/></svg></span>产品信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产品编码"><el-input v-model="form.productCode" disabled placeholder="选择工单后自动带出" /></el-form-item></el-col><el-col :span="12"><el-form-item label="产品名称"><el-input v-model="form.productName" disabled placeholder="选择工单后自动带出" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="规格型号"><el-input v-model="form.specModel" disabled placeholder="—" /></el-form-item></el-col><el-col :span="8"><el-form-item label="单位"><el-select v-model="form.unit" disabled style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="8"><el-form-item label="工单数量"><el-input v-model="form.planQty" disabled placeholder="—" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card" v-if="form.qcId">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检验数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="检验数量" prop="inspectQty"><el-input-number v-model="form.inspectQty" :min="0" :precision="2" :disabled="isAutoGenerated(form)" style="width: 100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="不良数" prop="defectQty"><el-input-number v-model="form.defectQty" :min="0" :precision="2" :disabled="isAutoGenerated(form)" style="width: 100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="报废数量" prop="scrapQty"><el-input-number v-model="form.scrapQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="缺陷类型" prop="defectType"><el-select v-model="form.defectType" placeholder="请选择" clearable filterable style="width: 100%"><el-option v-for="d in mms_defect_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="检验结果" prop="qcResult"><el-select v-model="form.qcResult" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_qc_result" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
              <div v-if="isAutoGenerated(form)" class="highlight-card highlight-warning" style="margin-top: 12px; margin-bottom: 0;"><div class="highlight-card-body"><p>该质检单由工单完工自动生成，检验数量和不良数已预填充。质检员请确认实际检验结果，填写报废数量、缺陷类型、检验结论等信息后保存。</p></div></div>
            </div>
          </section>
          <section class="rd-card" v-if="form.qcId">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>检验信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="检验人" prop="qcBy" :rules="[{ required: true, message: '请选择检验人', trigger: 'change' }]"><el-input v-model="form.qcBy" readonly placeholder="请选择检验人" style="width: 100%" @click="openUserPicker"><template #append><el-button icon="Search" @click="openUserPicker" /></template><template #suffix><el-icon v-if="form.qcBy" class="rd-form-tip" style="cursor:pointer" @click.stop="clearQcBy"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="检验时间" prop="qcTime"><el-date-picker v-model="form.qcTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col></el-row>
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
          <span class="rd-detail-header-title">质检记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.qcNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.qcNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">质检编号</span><div class="rd-value">{{ viewData.qcNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div><div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div><div class="rd-item"><span class="rd-label">工单数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">工序</span><div class="rd-value">{{ viewData.processName || '-' }}</div></div><div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><dict-tag :options="mms_qc_type" :value="viewData.qcType" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检验数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">检验数量</span><div class="rd-value">{{ viewData.inspectQty != null ? viewData.inspectQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">不良数</span><div class="rd-value">{{ viewData.defectQty != null ? viewData.defectQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">报废数量</span><div class="rd-value">{{ viewData.scrapQty != null ? viewData.scrapQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">缺陷类型</span><div class="rd-value"><dict-tag :options="mms_defect_type" :value="viewData.defectType" v-if="viewData.defectType" /><span v-else>-</span></div></div><div class="rd-item"><span class="rd-label">检验结果</span><div class="rd-value"><dict-tag :options="mms_qc_result" :value="viewData.qcResult" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>检验信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">检验人</span><div class="rd-value">{{ viewData.qcBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">检验时间</span><div class="rd-value">{{ viewData.qcTime ? parseTime(viewData.qcTime) : '-' }}</div></div></div></div>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="14" x2="15" y2="14"/><line x1="9" y1="18" x2="13" y2="18"/></svg></div>
          <span class="rd-detail-header-title">选择工单</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width:180px" @keyup.enter="handleWoPickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="woPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width:180px" @keyup.enter="handleWoPickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleWoPickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetWoPickerQuery">重置</el-button>
      </div>
      <el-table ref="woPickerTableRef" v-loading="woPickerLoading" :data="woPickerList" highlight-current-row @row-click="onWoRowClick" @row-dblclick="onWoRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="woPickerSelectedId" :value="row.workOrderId" @click="onWoRowClick(row)"><span /></el-radio></template></el-table-column>
        <el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip />
        <el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip />
        <el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip />
        <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
        <el-table-column label="计划数量" prop="planQty" width="90" align="center" />
        <el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" />
      </div>
      <div v-if="woPickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出工单编号</div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 工序选择弹框 ===== -->
    <el-dialog v-model="procPickerOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="6" r="3"/><path d="M12 9v6"/><path d="M9 15l-2 6"/><path d="M15 15l2 6"/><path d="M6 12h12"/></svg></div>
          <span class="rd-detail-header-title">选择工序</span>
        </div>
      </template>
      <el-table ref="procPickerTableRef" v-loading="procPickerLoading" :data="procPickerList" highlight-current-row @row-click="onProcRowClick" @row-dblclick="onProcRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="procPickerSelectedId" :value="row.snapshotId" @click="onProcRowClick(row)"><span /></el-radio></template></el-table-column>
        <el-table-column label="工序序号" prop="stepSeq" width="80" align="center" />
        <el-table-column label="工序名称" prop="processName" min-width="180" show-overflow-tooltip />
        <el-table-column label="工序编码" prop="processCode" width="140" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="cellName" width="140" show-overflow-tooltip />
      </el-table>
      <div v-if="procPickerList.length === 0 && !procPickerLoading" style="padding:24px;text-align:center;color:#94a3b8;font-size:13px">请先选择工单，工序将自动加载</div>
      <div v-if="procPickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出工序</div>
      <template #footer><el-button @click="procPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleProcPickerConfirm" :disabled="!procPickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 人员选择弹框 ===== -->
    <user-picker ref="userPickerRef" title="选择检验人" @confirm="onUserPickerConfirm" />

    <!-- ===== 质检弹窗 ===== -->
    <el-dialog v-model="inspectOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">执行质检</span>
          <div class="rd-detail-header-sub" v-if="inspectForm.qcNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ inspectForm.qcNo }}</span></div>
        </div>
      </template>
      <el-form ref="inspectFormRef" :model="inspectForm" :rules="inspectRules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('i0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.i0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">质检编号</span><div class="rd-value">{{ inspectForm.qcNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ inspectForm.workOrderNo || '-' }}</div></div><div class="rd-item" v-if="inspectForm.productName"><span class="rd-label">产品编码</span><div class="rd-value">{{ inspectForm.productCode || '-' }}</div></div><div class="rd-item" v-if="inspectForm.productName"><span class="rd-label">产品名称</span><div class="rd-value">{{ inspectForm.productName || '-' }}</div></div><div class="rd-item" v-if="inspectForm.productName"><span class="rd-label">规格型号</span><div class="rd-value">{{ inspectForm.specModel || '-' }}</div></div><div class="rd-item" v-if="inspectForm.productName"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="inspectForm.unit" /></div></div><div class="rd-item" v-if="inspectForm.productName"><span class="rd-label">工单数量</span><div class="rd-value">{{ inspectForm.planQty != null ? inspectForm.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">工序</span><div class="rd-value">{{ inspectForm.processName || '-' }}</div></div><div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><dict-tag :options="mms_qc_type" :value="inspectForm.qcType" /></div></div></div></div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('i1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检验数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.i1">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="检验数量" prop="inspectQty"><el-input-number v-model="inspectForm.inspectQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="不良数" prop="defectQty"><el-input-number v-model="inspectForm.defectQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="报废数量" prop="scrapQty"><el-input-number v-model="inspectForm.scrapQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="缺陷类型" prop="defectType"><el-select v-model="inspectForm.defectType" placeholder="请选择" clearable filterable style="width: 100%"><el-option v-for="d in mms_defect_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="检验结果" prop="qcResult"><el-select v-model="inspectForm.qcResult" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_qc_result" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('i2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>检验信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.i2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="检验人" prop="qcBy" :rules="[{ required: true, message: '请选择检验人', trigger: 'change' }]"><el-input v-model="inspectForm.qcBy" readonly placeholder="请选择检验人" style="width: 100%" @click="openUserPickerForInspect"><template #append><el-button icon="Search" @click="openUserPickerForInspect" /></template><template #suffix><el-icon v-if="inspectForm.qcBy" class="rd-form-tip" style="cursor:pointer" @click.stop="inspectForm.qcBy = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="检验时间" prop="qcTime"><el-date-picker v-model="inspectForm.qcTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('i3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.i3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.i3">
              <el-form-item label="备注" prop="remark"><el-input v-model="inspectForm.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitInspect">提交质检</el-button><el-button @click="inspectOpen = false">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="showStatusHelp" title="质检管理业务操作说明" width="820px" append-to-body><div class="status-help-content"><h4>一、质检管理释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是质检管理？</div><div class="highlight-card-body"><strong>质检管理（Quality Control）</strong>是生产管控中记录生产过程质量检验数据的单据。覆盖首件检验（首检）、过程巡检（巡检）、完工检验（完工检）等多种检验类型，记录检验数量、不良数和检验结果，支持生产过程质量管控和数据追溯。<br/><br/>质检管理遵循 <strong>ISO 9001 质量管理体系</strong>要求，通过首检确认生产条件、巡检监控过程稳定性、完工检保障成品质量，实现生产全过程的质量数据采集和追溯。</div></div><h4>二、质检类型与工序关联说明</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">不同检验类型对工序的关联要求</div><div class="highlight-card-body"><p>质检单上的"工序"字段为<strong>选填</strong>，不同检验类型对工序的关联要求不同：</p><table style="width:100%;margin-top:8px;border-collapse:collapse;font-size:13px"><thead><tr style="background:#f0f5ff"><th style="padding:6px 10px;border:1px solid #e0e0e0;text-align:left">检验类型</th><th style="padding:6px 10px;border:1px solid #e0e0e0;text-align:left">是否需要关联工序</th><th style="padding:6px 10px;border:1px solid #e0e0e0;text-align:left">说明</th></tr></thead><tbody><tr><td style="padding:6px 10px;border:1px solid #e0e0e0">完工检（自动生成）</td><td style="padding:6px 10px;border:1px solid #e0e0e0;color:#67c23a">不需要</td><td style="padding:6px 10px;border:1px solid #e0e0e0">检验最终成品质量，不绑定具体工序</td></tr><tr><td style="padding:6px 10px;border:1px solid #e0e0e0">首检（手动新增）</td><td style="padding:6px 10px;border:1px solid #e0e0e0;color:#e6a23c">可选</td><td style="padding:6px 10px;border:1px solid #e0e0e0">如需记录是哪道工序的首件检验，可选择关联</td></tr><tr><td style="padding:6px 10px;border:1px solid #e0e0e0">巡检（手动新增）</td><td style="padding:6px 10px;border:1px solid #e0e0e0;color:#e6a23c">可选</td><td style="padding:6px 10px;border:1px solid #e0e0e0">如需针对特定工序巡检，可选择关联</td></tr></tbody></table><p style="margin-top:8px;color:#94a3b8;font-size:12px">注：工序字段为空时在列表中显示"—"，不影响质检单的完整性。</p></div></div><h4>三、自动生成与手动新增</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">两种创建方式</div><div class="highlight-card-body"><p><strong>① 自动生成（完工检）：</strong>工单完工后系统自动创建完工质检单，预填充检验数量和不良数，工序字段为空。质检员只需点击「质检」或「修改」，确认检验结果并填写检验人后保存。</p><p style="margin-top:6px"><strong>② 手动新增（首检/巡检）：</strong>质检员点击「新增」手动创建，选择工单后系统自动带出产品信息（编码、名称、规格型号、单位、工单数量），如需关联工序可从工单工艺路线中选择。</p></div></div><h4>四、重点业务规则</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">核心规则</div><div class="highlight-card-body"><p>• <strong>检验类型：</strong>支持首检(0)、巡检(1)、完工检(2)三种检验类型</p><p>• <strong>不良数监控：</strong>不良数大于0时自动标红显示</p><p>• <strong>检验结果：</strong>合格(0)、让步接收(1)、拒收(2)三种结果</p><p>• <strong>工单关联：</strong>质检记录必须关联生产工单，确保数据可追溯</p><p>• <strong>产品信息：</strong>选择工单后自动带出产品编码、名称、规格型号、单位、工单数量，只读不可修改</p><p>• <strong>工序关联：</strong>选填字段，仅首检/巡检场景按需选择，完工检不需要</p><p>• <strong>缺陷类型：</strong>从字典选择标准化缺陷分类</p><p>• <strong>质检独立流转：</strong>质检作为独立业务，不卡住工单状态</p></div></div><h4>五、新增/修改表单填写指南</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">基本信息区</div><div class="highlight-card-body"><p>• <strong>质检编号：</strong>质检记录的唯一标识编号，保存后由系统自动生成</p><p>• <strong>工单编号：</strong>点击搜索按钮从弹框选择生产工单<span style="color: #f56c6c;">*必填</span></p><p>• <strong>工序：</strong>选填，点击搜索按钮从工单工艺路线快照中选择（仅首检/巡检场景需要）</p><p>• <strong>类型：</strong>检验类型，包括首检、巡检、完工检<span style="color: #f56c6c;">*必填</span></p></div></div><div class="highlight-card highlight-primary" style="margin-top: 12px;"><div class="highlight-card-title">产品信息区</div><div class="highlight-card-body"><p>• <strong>产品编码：</strong>选择工单后自动带出，只读不可修改</p><p>• <strong>产品名称：</strong>选择工单后自动带出，只读不可修改</p><p>• <strong>规格型号：</strong>选择工单后自动带出，只读不可修改</p><p>• <strong>单位：</strong>选择工单后自动带出（字典），只读不可修改</p><p>• <strong>工单数量：</strong>选择工单后自动带出，只读不可修改</p></div></div><div class="highlight-card highlight-primary" style="margin-top: 12px;"><div class="highlight-card-title">检验数据区（修改时显示）</div><div class="highlight-card-body"><p>• <strong>检验数量：</strong>本次检验的抽样数量，自动生成的质检单已预填充</p><p>• <strong>不良数：</strong>检验中发现的不合格品数量，自动生成的质检单已预填充，大于0时自动标红</p><p>• <strong>报废数量：</strong>检验中确定的报废数量</p><p>• <strong>缺陷类型：</strong>从字典选择检验发现的主要缺陷类型</p><p>• <strong>检验结果：</strong>检验的最终判定结果，包括合格、让步接收、拒收</p></div></div><div class="highlight-card highlight-primary" style="margin-top: 12px;"><div class="highlight-card-title">检验信息区（修改时显示）</div><div class="highlight-card-body"><p>• <strong>检验人：</strong>点击搜索按钮从弹框选择执行检验的人员</p><p>• <strong>检验时间：</strong>执行检验的时间</p></div></div><div class="highlight-card highlight-warning" style="margin-top: 12px;"><div class="highlight-card-title">其他信息区</div><div class="highlight-card-body"><p>• <strong>备注：</strong>检验的补充说明信息</p></div></div><h4>六、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>自动生成（完工检）：</strong>工单完工后，系统自动将工单状态流转为"已完工"，同时<strong>自动生成一条完工检类型的质检单</strong>，预填充检验数量和不良数量，工序为空。质检作为独立业务流转，不卡住工单状态</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>质检员确认：</strong>质检员在列表中找到待检验的质检单，点击「质检」按钮，确认报废数量、缺陷类型，判定检验结果（合格/让步/拒收），选择检验人后提交</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>手动新增（首检/巡检）：</strong>对于过程首检、巡检等场景，质检员点击「新增」手动创建质检记录，选择工单后自动带出产品信息，按需选择工序，填写类型后保存。检验数据和检验信息在后续修改时补充</el-timeline-item></el-timeline></div><template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template></el-dialog>
  </div>
</template>

<script setup name="Qc">
import { listQc, getQc, addQc, updateQc, delQc, inspectQc } from "@/api/mms/qc";
import { listWorkOrder, listWorkOrderProcesses } from "@/api/mms/workorder";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import UserPicker from '@/components/UserPicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_qc_type, mms_qc_result, mms_defect_type, mms_workorder_status, wms_unit } = proxy.useDict("mms_qc_type", "mms_qc_result", "mms_defect_type", "mms_workorder_status", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_qc_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","c4","vc0","vc1","vc2","vc3","i0","i1","i2","i3"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);
// 质检弹窗
const inspectOpen = ref(false); const inspectForm = ref({}); const inspectRules = { inspectQty: [{ required: true, message: '请输入检验数量', trigger: 'blur' }], defectQty: [{ required: true, message: '请输入不良数', trigger: 'blur' }], qcResult: [{ required: true, message: '请选择检验结果', trigger: 'change' }] };
const inspectTarget = ref('form'); // 区分检验人选择器目标：form 或 inspect

// 工单选择器
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null); const woPickerTableRef = ref();
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined });
// 工序选择器
const procPickerOpen = ref(false); const procPickerLoading = ref(false); const procPickerList = ref([]); const procPickerSelectedId = ref(null); const procPickerSelectedRow = ref(null); const procPickerTableRef = ref();
// 工序选项（联动工单选择）
const processOptions = ref([]);

const statusTabList = computed(() => mms_qc_result.value ? mms_qc_result.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { qcNo: { label: '质检编号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, productCode: { label: '产品编码', visible: true }, productName: { label: '产品名称', visible: true }, specModel: { label: '规格型号', visible: true }, unit: { label: '单位', visible: true }, planQty: { label: '工单数量', visible: true }, processName: { label: '工序', visible: true }, qcType: { label: '类型', visible: true }, inspectQty: { label: '检验数量', visible: true }, defectQty: { label: '不良数', visible: true }, defectType: { label: '缺陷类型', visible: true }, qcResult: { label: '检验结果', visible: true }, source: { label: '来源', visible: true }, qcBy: { label: '检验人', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_qc_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.qcNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.processName) c++; if (queryParams.value.qcType) c++; if (queryParams.value.qcResult) c++; if (queryParams.value.qcBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, qcNo: undefined, workOrderNo: undefined, processName: undefined, qcType: undefined, qcResult: undefined, qcBy: undefined, params: {} }, rules: { qcType: [{ required: true, message: "请选择类型", trigger: "change" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listQc(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.qcNo) baseQuery.qcNo = queryParams.value.qcNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.processName) baseQuery.processName = queryParams.value.processName; if (queryParams.value.qcType) baseQuery.qcType = queryParams.value.qcType; if (queryParams.value.qcBy) baseQuery.qcBy = queryParams.value.qcBy; listQc(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total, pending: 0 }; if (mms_qc_result.value) { mms_qc_result.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (!r.qcResult) { counts.pending++; } else if (counts[r.qcResult] !== undefined) { counts[r.qcResult]++; } }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.qcNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.processName = undefined; queryParams.value.qcType = undefined; queryParams.value.qcResult = undefined; queryParams.value.qcBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; if (status === 'all') { queryParams.value.qcResult = undefined; } else if (status === 'pending') { queryParams.value.qcResult = ''; } else { queryParams.value.qcResult = status; } handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.qcId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { qcNo: undefined, workOrderId: undefined, workOrderNo: undefined, productCode: undefined, productName: undefined, specModel: undefined, unit: undefined, planQty: undefined, processId: undefined, processName: undefined, qcType: undefined, inspectQty: undefined, defectQty: undefined, scrapQty: undefined, defectType: undefined, qcResult: undefined, qcBy: undefined, qcTime: undefined, remark: undefined }; processOptions.value = []; proxy.resetForm("formRef"); }
function isAutoGenerated(row) { return row.remark && row.remark.includes('自动生成'); }
function handleAdd() { reset(); open.value = true; title.value = "新增质检"; }
function handleUpdate(row) { reset(); const id = row.qcId || ids.value[0]; getQc(id).then(response => { form.value = response.data; open.value = true; title.value = "修改质检"; if (form.value.workOrderId) { loadProcessOptions(form.value.workOrderId); } }); }
function handleView(row) { const id = row.qcId || ids.value[0]; getQc(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.qcId != null) { updateQc(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addQc(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.qcId || ids.value; proxy.$modal.confirm('是否确认删除选中的质检记录？').then(() => delQc(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/qc/export", { ...queryParams.value }, `qc_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function resultBadgeClass(result) { const map = { '0': 'green', '1': 'red', '2': 'amber' }; return map[result] || 'gray'; }
function resultTabClass(value) { const map = { '0': 'tab-done', '1': 'tab-reject', '2': 'tab-draft' }; return map[value] || ''; }

// ===== 执行质检 =====
function handleInspect(row) { const id = row.qcId || ids.value[0]; getQc(id).then(response => { const d = response.data; if (d.qcResult) { proxy.$modal.msgWarning('该质检单已检验，不可重复质检'); return; } inspectForm.value = { qcId: d.qcId, qcNo: d.qcNo, workOrderId: d.workOrderId, workOrderNo: d.workOrderNo, productCode: d.productCode, productName: d.productName, specModel: d.specModel, unit: d.unit, planQty: d.planQty, processName: d.processName, qcType: d.qcType, inspectQty: d.inspectQty, defectQty: d.defectQty, scrapQty: d.scrapQty != null ? d.scrapQty : 0, defectType: d.defectType, qcResult: undefined, qcBy: d.qcBy, qcTime: d.qcTime, remark: d.remark }; inspectOpen.value = true; }); }
function submitInspect() { proxy.$refs["inspectFormRef"].validate(valid => { if (valid) { if (!inspectForm.value.qcBy) { proxy.$modal.msgWarning('请选择检验人'); return; } inspectQc(inspectForm.value).then(() => { proxy.$modal.msgSuccess("质检完成"); inspectOpen.value = false; getList(); }); } }); }

// ===== 工单选择器 =====
function openWorkOrderPicker() { woPickerOpen.value = true; woPickerSelectedId.value = null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; getWoPickerList(); }
function getWoPickerList() { woPickerLoading.value = true; listWorkOrder(woPickerQuery).then(res => { woPickerList.value = res.rows; woPickerTotal.value = res.total; woPickerLoading.value = false; }).catch(() => { woPickerLoading.value = false; }); }
function handleWoPickerQuery() { woPickerQuery.pageNum = 1; getWoPickerList(); }
function resetWoPickerQuery() { woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; handleWoPickerQuery(); }
function onWoRowClick(row) { woPickerSelectedId.value = row.workOrderId; woPickerSelectedRow.value = row; }
function onWoRowDblClick(row) { onWoRowClick(row); handleWoPickerConfirm(); }
function handleWoPickerConfirm() { if (!woPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工单'); return; } const row = woPickerSelectedRow.value; form.value.workOrderId = row.workOrderId; form.value.workOrderNo = row.workOrderNo; form.value.productCode = row.productCode; form.value.productName = row.productName; form.value.specModel = row.specModel; form.value.unit = row.unit; form.value.planQty = row.planQty; form.value.processId = undefined; form.value.processName = undefined; woPickerOpen.value = false; loadProcessOptions(row.workOrderId); }
function clearWorkOrder() { form.value.workOrderId = undefined; form.value.workOrderNo = undefined; form.value.productCode = undefined; form.value.productName = undefined; form.value.specModel = undefined; form.value.unit = undefined; form.value.planQty = undefined; form.value.processId = undefined; form.value.processName = undefined; processOptions.value = []; }

// ===== 工序联动 =====
function loadProcessOptions(workOrderId) { listWorkOrderProcesses(workOrderId).then(res => { processOptions.value = res.data || []; if (form.value.processId) { const matched = processOptions.value.find(p => p.processId === form.value.processId); if (matched) { form.value.processName = matched.processName; } } }).catch(() => { processOptions.value = []; }); }

// ===== 工序选择器 =====
function openProcessPicker() {
  if (!form.value.workOrderId) { proxy.$modal.msgWarning('请先选择工单'); return; }
  procPickerOpen.value = true; procPickerSelectedId.value = null; procPickerSelectedRow.value = null;
  procPickerLoading.value = true;
  listWorkOrderProcesses(form.value.workOrderId).then(res => {
    procPickerList.value = res.data || [];
    procPickerLoading.value = false;
    if (form.value.processId) {
      const matched = procPickerList.value.find(p => p.processId === form.value.processId);
      if (matched) { procPickerSelectedId.value = matched.snapshotId; procPickerSelectedRow.value = matched; }
    }
  }).catch(() => { procPickerList.value = []; procPickerLoading.value = false; });
}
function onProcRowClick(row) { procPickerSelectedId.value = row.snapshotId; procPickerSelectedRow.value = row; }
function onProcRowDblClick(row) { onProcRowClick(row); handleProcPickerConfirm(); }
function handleProcPickerConfirm() {
  if (!procPickerSelectedRow.value) return;
  const p = procPickerSelectedRow.value;
  form.value.processId = p.processId; form.value.processName = p.processName;
  procPickerOpen.value = false;
}
function clearProcess() { form.value.processId = undefined; form.value.processName = undefined; }

// ===== 检验人选择器 =====
function openUserPicker() { inspectTarget.value = 'form'; proxy.$refs.userPickerRef.open(); }
function openUserPickerForInspect() { inspectTarget.value = 'inspect'; proxy.$refs.userPickerRef.open(); }
function onUserPickerConfirm(user) { if (inspectTarget.value === 'inspect') { inspectForm.value.qcBy = user.nickName; } else { form.value.qcBy = user.nickName; } }
function clearQcBy() { form.value.qcBy = undefined; }

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-qc-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
