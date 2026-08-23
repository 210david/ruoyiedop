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
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="质检编号" prop="qcNo" key="qcNo" :width="colWidth('qcNo', 140)" resizable v-if="columns.qcNo.visible" /><el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" /><el-table-column label="工序" prop="processName" key="processName" :width="colWidth('processName', 100)" resizable v-if="columns.processName.visible" /><el-table-column label="类型" prop="qcType" key="qcType" :width="colWidth('qcType', 100)" resizable align="center" v-if="columns.qcType.visible"><template #default="scope"><span v-if="scope.row.qcType" class="badge blue"><span class="dot"></span>{{ dictLabel(mms_qc_type, scope.row.qcType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="检验数量" prop="inspectQty" key="inspectQty" :width="colWidth('inspectQty', 100)" resizable align="center" v-if="columns.inspectQty.visible" /><el-table-column label="不良数" prop="defectQty" key="defectQty" :width="colWidth('defectQty', 80)" resizable align="center" v-if="columns.defectQty.visible"><template #default="scope"><span :style="{ color: scope.row.defectQty > 0 ? '#ef4444' : 'inherit', fontWeight: scope.row.defectQty > 0 ? 600 : 400 }">{{ scope.row.defectQty }}</span></template></el-table-column><el-table-column label="缺陷类型" prop="defectType" key="defectType" :width="colWidth('defectType', 110)" resizable align="center" v-if="columns.defectType.visible"><template #default="scope"><span v-if="scope.row.defectType">{{ dictLabel(mms_defect_type, scope.row.defectType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="检验结果" prop="qcResult" key="qcResult" :width="colWidth('qcResult', 100)" resizable align="center" v-if="columns.qcResult.visible"><template #default="scope"><span v-if="scope.row.qcResult" class="badge" :class="resultBadgeClass(scope.row.qcResult)"><span class="dot"></span>{{ dictLabel(mms_qc_result, scope.row.qcResult) }}</span><span v-else class="badge amber"><span class="dot"></span>待检验</span></template></el-table-column><el-table-column label="来源" key="source" :width="colWidth('source', 90)" resizable align="center" v-if="columns.source.visible"><template #default="scope"><el-tag v-if="isAutoGenerated(scope.row)" type="warning" size="small" effect="plain">自动生成</el-tag><el-tag v-else type="info" size="small" effect="plain">手动新增</el-tag></template></el-table-column><el-table-column label="检验人" prop="qcBy" key="qcBy" :width="colWidth('qcBy', 90)" resizable align="center" v-if="columns.qcBy.visible" /><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="280" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button><el-button link type="success" icon="Check" @click="handleInspect(scope.row)" v-if="!scope.row.qcResult" v-hasPermi="['mms:qc:edit']">质检</el-button><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:qc:edit']">修改</el-button><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:qc:remove']">删除</el-button></template></el-table-column></el-table></div>
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序" prop="processName"><el-select v-model="form.processId" placeholder="请先选择工单" clearable filterable :disabled="!form.workOrderId" style="width: 100%" @change="onProcessChange"><el-option v-for="p in processOptions" :key="p.snapshotId" :label="p.processName + '（工序' + p.stepSeq + '）'" :value="p.processId" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="类型" prop="qcType"><el-select v-model="form.qcType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_qc_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检验数据</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="检验数量" prop="inspectQty"><el-input-number v-model="form.inspectQty" :min="0" :precision="2" :disabled="isAutoGenerated(form)" style="width: 100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="不良数" prop="defectQty"><el-input-number v-model="form.defectQty" :min="0" :precision="2" :disabled="isAutoGenerated(form)" style="width: 100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="报废数量" prop="scrapQty"><el-input-number v-model="form.scrapQty" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="缺陷类型" prop="defectType"><el-select v-model="form.defectType" placeholder="请选择" clearable filterable style="width: 100%"><el-option v-for="d in mms_defect_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="检验结果" prop="qcResult"><el-select v-model="form.qcResult" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_qc_result" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
              <div v-if="isAutoGenerated(form)" class="highlight-card highlight-warning" style="margin-top: 12px; margin-bottom: 0;"><div class="highlight-card-body"><p>该质检单由工单完工自动生成，检验数量和不良数已预填充。质检员请确认实际检验结果，填写报废数量、缺陷类型、检验结论等信息后保存。</p></div></div>
            </div>
          </section>
          <section class="rd-card">
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
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">质检编号</span><div class="rd-value">{{ viewData.qcNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工序</span><div class="rd-value">{{ viewData.processName || '-' }}</div></div><div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><dict-tag :options="mms_qc_type" :value="viewData.qcType" /></div></div></div></div>
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
    <el-dialog v-model="woPickerOpen" title="选择工单" width="936px" append-to-body draggable>
      <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center;">
        <el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable style="width: 200px" @keyup.enter="handleWoPickerQuery" />
        <el-input v-model="woPickerQuery.productName" placeholder="产品名称" clearable style="width: 200px" @keyup.enter="handleWoPickerQuery" />
        <el-button type="primary" icon="Search" @click="handleWoPickerQuery">搜索</el-button>
        <el-button icon="RefreshLeft" @click="resetWoPickerQuery">重置</el-button>
      </div>
      <el-table v-loading="woPickerLoading" :data="woPickerList" highlight-current-row @row-click="onWoRowClick" @row-dblclick="onWoRowDblClick" height="400" size="small" border>
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="woPickerSelectedId" :value="row.workOrderId" @click.stop="onWoRowClick(row)"><span /></el-radio></template></el-table-column>
        <el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip />
        <el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip />
        <el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip />
        <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
        <el-table-column label="计划数量" prop="planQty" width="90" align="center" />
        <el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column>
      </el-table>
      <div style="margin-top: 8px;"><el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" /></div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
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
            <div class="rd-card-body" v-show="!collapsedCards.i0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="质检编号"><el-input v-model="inspectForm.qcNo" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号"><el-input v-model="inspectForm.workOrderNo" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序"><el-input v-model="inspectForm.processName" placeholder="—" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="类型"><el-input :model-value="dictLabel(mms_qc_type, inspectForm.qcType)" disabled /></el-form-item></el-col></el-row>
            </div>
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

    <el-dialog v-model="showStatusHelp" title="质检管理业务操作说明" width="820px" append-to-body><div class="status-help-content"><h4>一、质检管理释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是质检管理？</div><div class="highlight-card-body"><strong>质检管理（Quality Control）</strong>是生产管控中记录生产过程质量检验数据的单据。覆盖首件检验（首检）、过程巡检（巡检）、完工检验（完工检）等多种检验类型，记录检验数量、不良数和检验结果，支持生产过程质量管控和数据追溯。<br/><br/>质检管理遵循 <strong>ISO 9001 质量管理体系</strong>要求，通过首检确认生产条件、巡检监控过程稳定性、完工检保障成品质量，实现生产全过程的质量数据采集和追溯。</div></div><h4>二、自动生成与操作提示</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">自动生成与手动新增</div><div class="highlight-card-body"><p>工单完工后系统会<strong>自动生成完工质检单</strong>，预填充检验数量和不良数。质检员只需在列表中点击「修改」，确认检验结果（合格/让步/拒收）并填写检验人后保存即可。手动新增时，工单编号、工序、检验人均可通过弹框选择。</p></div></div><h4>三、重点业务规则</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">核心规则</div><div class="highlight-card-body"><p>• <strong>检验类型：</strong>支持首检、巡检、完工检等多种检验类型</p><p>• <strong>不良数监控：</strong>不良数大于0时自动标红显示</p><p>• <strong>检验结果：</strong>合格、让步接收、拒收三种结果</p><p>• <strong>工单关联：</strong>质检记录需关联生产工单，确保数据可追溯</p><p>• <strong>工序关联：</strong>选择工单后可联动选择该工单的工序</p><p>• <strong>缺陷类型：</strong>从字典选择标准化缺陷分类</p></div></div><h4>四、新增/修改表单填写指南</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">基本信息区</div><div class="highlight-card-body"><p>• <strong>质检编号：</strong>质检记录的唯一标识编号，保存后由系统自动生成</p><p>• <strong>工单编号：</strong>点击搜索按钮从弹框选择生产工单<span style="color: #f56c6c;">*必填</span></p><p>• <strong>工序：</strong>选择工单后，从该工单的工序快照中选择检验工序</p><p>• <strong>类型：</strong>检验类型，包括首检、巡检、完工检等<span style="color: #f56c6c;">*必填</span></p></div></div><div class="highlight-card highlight-primary" style="margin-top: 12px;"><div class="highlight-card-title">检验数据区</div><div class="highlight-card-body"><p>• <strong>检验数量：</strong>本次检验的抽样数量<span style="color: #f56c6c;">*必填</span></p><p>• <strong>不良数：</strong>检验中发现的不合格品数量，大于0时自动标红<span style="color: #f56c6c;">*必填</span></p><p>• <strong>报废数量：</strong>检验中确定的报废数量</p><p>• <strong>缺陷类型：</strong>从字典选择检验发现的主要缺陷类型</p><p>• <strong>检验结果：</strong>检验的最终判定结果，包括合格、让步接收、拒收<span style="color: #f56c6c;">*必填</span></p></div></div><div class="highlight-card highlight-primary" style="margin-top: 12px;"><div class="highlight-card-title">检验信息区</div><div class="highlight-card-body"><p>• <strong>检验人：</strong>点击搜索按钮从弹框选择执行检验的人员<span style="color: #f56c6c;">*必填</span></p><p>• <strong>检验时间：</strong>执行检验的时间</p></div></div><div class="highlight-card highlight-warning" style="margin-top: 12px;"><div class="highlight-card-title">其他信息区</div><div class="highlight-card-body"><p>• <strong>备注：</strong>检验的补充说明信息</p></div></div><h4>五、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>自动生成（完工检）：</strong>工单完工后，系统自动将工单状态流转为"已完工"，同时<strong>自动生成一条完工检类型的质检单</strong>，预填充检验数量和不良数量。质检作为独立业务流转，不卡住工单状态</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>质检员确认：</strong>质检员在列表中找到待检验的质检单，点击「修改」，确认报废数量、缺陷类型，判定检验结果（合格/让步/拒收），选择检验人后保存</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>手动新增：</strong>对于过程首检、巡检等场景，质检员可点击「新增」手动创建质检记录，选择工单和工序后填写检验数据</el-timeline-item></el-timeline></div><template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template></el-dialog>
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
const { mms_qc_type, mms_qc_result, mms_defect_type, mms_workorder_status } = proxy.useDict("mms_qc_type", "mms_qc_result", "mms_defect_type", "mms_workorder_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_qc_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","vc0","vc1","vc2","vc3","i0","i1","i2","i3"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);
// 质检弹窗
const inspectOpen = ref(false); const inspectForm = ref({}); const inspectRules = { inspectQty: [{ required: true, message: '请输入检验数量', trigger: 'blur' }], defectQty: [{ required: true, message: '请输入不良数', trigger: 'blur' }], qcResult: [{ required: true, message: '请选择检验结果', trigger: 'change' }] };
const inspectTarget = ref('form'); // 区分检验人选择器目标：form 或 inspect

// 工单选择器
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null);
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined });
// 工序选项（联动工单选择）
const processOptions = ref([]);

const statusTabList = computed(() => mms_qc_result.value ? mms_qc_result.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { qcNo: { label: '质检编号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, processName: { label: '工序', visible: true }, qcType: { label: '类型', visible: true }, inspectQty: { label: '检验数量', visible: true }, defectQty: { label: '不良数', visible: true }, defectType: { label: '缺陷类型', visible: true }, qcResult: { label: '检验结果', visible: true }, source: { label: '来源', visible: true }, qcBy: { label: '检验人', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_qc_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.qcNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.processName) c++; if (queryParams.value.qcType) c++; if (queryParams.value.qcResult) c++; if (queryParams.value.qcBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, qcNo: undefined, workOrderNo: undefined, processName: undefined, qcType: undefined, qcResult: undefined, qcBy: undefined, params: {} }, rules: { qcType: [{ required: true, message: "请选择类型", trigger: "change" }], inspectQty: [{ required: true, message: "请输入检验数量", trigger: "blur" }], defectQty: [{ required: true, message: "请输入不良数", trigger: "blur" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listQc(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.qcNo) baseQuery.qcNo = queryParams.value.qcNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.processName) baseQuery.processName = queryParams.value.processName; if (queryParams.value.qcType) baseQuery.qcType = queryParams.value.qcType; if (queryParams.value.qcBy) baseQuery.qcBy = queryParams.value.qcBy; listQc(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total, pending: 0 }; if (mms_qc_result.value) { mms_qc_result.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (!r.qcResult) { counts.pending++; } else if (counts[r.qcResult] !== undefined) { counts[r.qcResult]++; } }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.qcNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.processName = undefined; queryParams.value.qcType = undefined; queryParams.value.qcResult = undefined; queryParams.value.qcBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; if (status === 'all') { queryParams.value.qcResult = undefined; } else if (status === 'pending') { queryParams.value.qcResult = ''; } else { queryParams.value.qcResult = status; } handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.qcId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { qcNo: undefined, workOrderId: undefined, workOrderNo: undefined, processId: undefined, processName: undefined, qcType: undefined, inspectQty: undefined, defectQty: undefined, scrapQty: undefined, defectType: undefined, qcResult: undefined, qcBy: undefined, qcTime: undefined, remark: undefined }; processOptions.value = []; proxy.resetForm("formRef"); }
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
function handleInspect(row) { const id = row.qcId || ids.value[0]; getQc(id).then(response => { const d = response.data; if (d.qcResult) { proxy.$modal.msgWarning('该质检单已检验，不可重复质检'); return; } inspectForm.value = { qcId: d.qcId, qcNo: d.qcNo, workOrderNo: d.workOrderNo, processName: d.processName, qcType: d.qcType, inspectQty: d.inspectQty, defectQty: d.defectQty, scrapQty: d.scrapQty != null ? d.scrapQty : 0, defectType: d.defectType, qcResult: undefined, qcBy: d.qcBy, qcTime: d.qcTime, remark: d.remark }; inspectOpen.value = true; }); }
function submitInspect() { proxy.$refs["inspectFormRef"].validate(valid => { if (valid) { if (!inspectForm.value.qcBy) { proxy.$modal.msgWarning('请选择检验人'); return; } inspectQc(inspectForm.value).then(() => { proxy.$modal.msgSuccess("质检完成"); inspectOpen.value = false; getList(); }); } }); }

// ===== 工单选择器 =====
function openWorkOrderPicker() { woPickerOpen.value = true; woPickerSelectedId.value = null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; getWoPickerList(); }
function getWoPickerList() { woPickerLoading.value = true; listWorkOrder(woPickerQuery).then(res => { woPickerList.value = res.rows; woPickerTotal.value = res.total; woPickerLoading.value = false; }).catch(() => { woPickerLoading.value = false; }); }
function handleWoPickerQuery() { woPickerQuery.pageNum = 1; getWoPickerList(); }
function resetWoPickerQuery() { woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; handleWoPickerQuery(); }
function onWoRowClick(row) { woPickerSelectedId.value = row.workOrderId; woPickerSelectedRow.value = row; }
function onWoRowDblClick(row) { onWoRowClick(row); handleWoPickerConfirm(); }
function handleWoPickerConfirm() { if (!woPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择工单'); return; } const row = woPickerSelectedRow.value; form.value.workOrderId = row.workOrderId; form.value.workOrderNo = row.workOrderNo; form.value.processId = undefined; form.value.processName = undefined; woPickerOpen.value = false; loadProcessOptions(row.workOrderId); }
function clearWorkOrder() { form.value.workOrderId = undefined; form.value.workOrderNo = undefined; form.value.processId = undefined; form.value.processName = undefined; processOptions.value = []; }

// ===== 工序联动 =====
function loadProcessOptions(workOrderId) { listWorkOrderProcesses(workOrderId).then(res => { processOptions.value = res.data || []; if (form.value.processId) { const matched = processOptions.value.find(p => p.processId === form.value.processId); if (matched) { form.value.processName = matched.processName; } } }).catch(() => { processOptions.value = []; }); }
function onProcessChange(val) { if (val) { const matched = processOptions.value.find(p => p.processId === val); if (matched) { form.value.processName = matched.processName; } } else { form.value.processName = undefined; } }

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
</style>