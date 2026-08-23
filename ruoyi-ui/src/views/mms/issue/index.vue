<template>
  <div class="app-container mms-issue-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>领料编号</label><div class="control"><el-input v-model="queryParams.issueNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料编码</label><div class="control"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料名称</label><div class="control"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_issue_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>领料人</label><div class="control"><el-input v-model="queryParams.issueBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="{ 'is-active': activeStatusTab === s.value }" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:issue:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:issue:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:issue:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:issue:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_issue_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="领料编号" prop="issueNo" key="issueNo" :width="colWidth('issueNo', 140)" resizable v-if="columns.issueNo.visible" /><el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" /><el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 130)" resizable v-if="columns.materialCode.visible" /><el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 200)" resizable show-overflow-tooltip v-if="columns.materialName.visible" /><el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" /><el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column><el-table-column label="领料数量" prop="issueQty" key="issueQty" :width="colWidth('issueQty', 100)" resizable align="center" v-if="columns.issueQty.visible" /><el-table-column label="领料人" prop="issueBy" key="issueBy" :width="colWidth('issueBy', 90)" resizable align="center" v-if="columns.issueBy.visible" /><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="280" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:issue:edit']" v-if="scope.row.status === '0'">修改</el-button><el-button link type="success" icon="Check" @click="handleConfirm(scope.row)" v-hasPermi="['mms:issue:confirm']" v-if="scope.row.status === '0'">确认领料</el-button><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:issue:remove']" v-if="scope.row.status === '0'">删除</el-button></template></el-table-column></el-table></div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="领料编号" prop="issueNo"><el-input v-model="form.issueNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单编号" prop="workOrderNo" :rules="[{ required: true, message: '请选择工单', trigger: 'change' }]"><el-input v-model="form.workOrderNo" readonly placeholder="请选择工单" style="width: 100%" @click="openWorkOrderPicker"><template #append><el-button icon="Search" @click="openWorkOrderPicker" /></template><template #suffix><el-icon v-if="form.workOrderNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearWorkOrder"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="产品编码"><el-input v-model="form.productCode" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col><el-col :span="8"><el-form-item label="产品名称"><el-input v-model="form.productName" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col><el-col :span="8"><el-form-item label="工单数量"><el-input v-model="form.planQty" placeholder="选择工单后自动带出" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_issue_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="领料人" prop="issueBy"><el-input v-model="form.issueByName" readonly placeholder="请选择领料人" style="width: 100%" @click="openIssueByPicker"><template #append><el-button icon="Search" @click="openIssueByPicker" /></template><template #suffix><el-icon v-if="form.issueByName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearIssueBy"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>领料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="openMaterialPicker" :disabled="!form.workOrderId">从BOM添加物料</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border size="small" class="detail-table" style="width:100%">
                <el-table-column label="序号" type="index" width="50" align="center" />
                <el-table-column label="物料编码" prop="materialCode" width="120" show-overflow-tooltip />
                <el-table-column label="物料名称" prop="materialName" min-width="120" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="specModel" width="100" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="60" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
                <el-table-column label="单件用量" prop="usageQty" width="80" align="center" />
                <el-table-column label="建议数量" prop="suggestedQty" width="90" align="center" />
                <el-table-column label="领料数量" width="110" align="center"><template #default="scope"><el-input-number v-model="scope.row.issueQty" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" placeholder="数量" /></template></el-table-column>
                <el-table-column label="批次号" width="110"><template #default="scope"><el-input v-model="scope.row.batchNo" size="small" placeholder="批次号" /></template></el-table-column>
                <el-table-column label="操作" width="60" align="center" fixed="right"><template #default="scope"><el-button link type="danger" icon="Delete" @click="removeDetail(scope.$index)" /></template></el-table-column>
              </el-table>
              <div v-if="!form.detailList || form.detailList.length === 0" class="empty-detail-tip">
                <el-icon style="margin-right:4px"><WarningFilled /></el-icon>暂无领料明细，请点击「从BOM添加物料」
              </div>
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
    <el-dialog v-model="viewOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">领料记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.issueNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.issueNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">领料编号</span><div class="rd-value">{{ viewData.issueNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '-' }}</div></div><div class="rd-item"><span class="rd-label">领料人</span><div class="rd-value">{{ viewData.issueBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_issue_status" :value="viewData.status" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>领料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block;padding:12px 20px">
            <el-table :data="viewData.detailList" border size="small" style="width:100%">
              <el-table-column label="序号" prop="seq" width="60" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="120" show-overflow-tooltip />
              <el-table-column label="物料名称" prop="materialName" min-width="120" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" width="100" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="60" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="单件用量" prop="usageQty" width="80" align="center" />
              <el-table-column label="建议数量" prop="suggestedQty" width="90" align="center" />
              <el-table-column label="领料数量" prop="issueQty" width="100" align="center" />
              <el-table-column label="批次号" prop="batchNo" width="110" />
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <el-dialog v-model="showStatusHelp" title="领料管理业务操作说明" width="820px" append-to-body draggable class="rd-dialog status-help-dialog">
      <div class="status-help-content">
        <!-- 一、领料管理释义 -->
        <h4>一、领料管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是领料管理？</div>
          <div class="highlight-card-body">
            <strong>领料管理（Material Issue）</strong>是生产管控中记录生产过程中从仓库领取物料进入生产线的关键单据。领料需关联生产工单和物料，记录领料数量和领料人，确保物料库存准确和生产成本正确归集。<br/><br/>
            领料管理遵循 <strong>MES（制造执行系统）物料管理规范</strong>，领料数据与仓库管理系统（WMS）联动，确保库存数据实时一致。退料业务由独立的<strong>退料管理</strong>模块处理，领料与退料职责分离、单据独立，符合精益生产中物料流转可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、状态流转图 -->
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待领料</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="success">点击「确认领料」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已领料</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">待领料</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="danger">点击「删除」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">已删除</el-tag>
            <el-tag size="small" type="info">软删除，数据保留</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待领料">新建领料单后的初始状态（0）。可修改领料信息、确认领料或删除。确认领料后状态变为「已领料」，不可再修改</el-descriptions-item>
          <el-descriptions-item label="已领料">领料已确认，物料已从仓库领出（1）。记录了领料人、领料时间和领料数量。如需退料，请到「退料管理」模块创建独立的退料单</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>领料编号：</strong>系统自动生成，保存后显示，不可手动修改</p>
            <p>• <strong>工单编号：</strong>点击选择按钮从工单选择器中选择生产工单<span style="color: #f56c6c;">*必填</span></p>
            <p style="padding-left: 16px;">○ 选择工单后自动带出产品编码、产品名称和工单数量</p>
            <p>• <strong>产品编码：</strong>来源于所选工单，不可手动修改</p>
            <p>• <strong>产品名称：</strong>来源于所选工单，不可手动修改</p>
            <p>• <strong>工单数量：</strong>来源于所选工单的计划生产数量，不可手动修改</p>
            <p>• <strong>状态：</strong>新建时默认为「待领料」，确认后自动变为「已领料」</p>
            <p>• <strong>领料人：</strong>点击选择按钮从人员选择器中选择领料人员</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">领料明细区</div>
          <div class="highlight-card-body">
            <p>• 点击「从BOM添加物料」按钮，从工单BOM快照中批量选择物料</p>
            <p>• <strong>物料编码 / 物料名称 / 规格型号 / 单位：</strong>从BOM自动带出，不可修改</p>
            <p>• <strong>单件用量：</strong>来源于BOM中每个产品所需该物料的数量，自动带出</p>
            <p>• <strong>建议数量：</strong>系统根据单件用量 × 工单数量自动计算，仅供参考</p>
            <p>• <strong>领料数量：</strong>可修改，默认为建议数量<span style="color: #f56c6c;">*必填</span>（须大于0）</p>
            <p>• <strong>批次号：</strong>手动输入物料的批次信息，便于物料追溯</p>
          </div>
        </div>

        <!-- 五、核心业务规则 -->
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-danger">
          <div class="highlight-card-title">操作约束</div>
          <div class="highlight-card-body">
            <p>• <strong>工单必选：</strong>必须先选择生产工单，才能添加领料明细</p>
            <p>• <strong>明细必填：</strong>至少添加一条领料明细才能保存</p>
            <p>• <strong>数量校验：</strong>每条明细的领料数量必须大于0</p>
            <p>• <strong>状态限制：</strong>只有「待领料」状态可修改、确认领料或删除；「已领料」状态不可再操作</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">确认领料规则</div>
          <div class="highlight-card-body">
            <p>• 确认领料时需指定<strong>领料人</strong><span style="color: #f56c6c;">*必填</span>和<strong>领料时间</strong><span style="color: #f56c6c;">*必填</span></p>
            <p>• 领料时间默认为当前时间，可手动调整</p>
            <p>• 确认领料时可在弹窗中微调每条明细的领料数量</p>
            <p>• 确认后状态变为「已领料」，领料人和领料时间持久化记录</p>
          </div>
        </div>

        <!-- 六、行业管控说明 -->
        <h4>六、行业管控说明</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">MES 物料管控</div>
          <div class="highlight-card-body">
            <p>• 领料管理遵循 <strong>GB/T 19001 质量管理体系</strong>中物料追溯的要求，每笔领料关联工单和物料明细</p>
            <p>• 支持批次号管理，满足物料批次追溯需求</p>
            <p>• 退料业务独立于领料管理，通过「退料管理」模块处理，职责分离避免状态混淆</p>
            <p>• 领料数据与WMS库存系统联动，确保账实一致</p>
          </div>
        </div>

        <!-- 七、业务操作流程 -->
        <h4>七、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true"><strong>新增领料：</strong>点击「新增」按钮，在弹窗中选择生产工单，系统自动带出产品信息</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>添加物料明细：</strong>点击「从BOM添加物料」按钮，从工单BOM快照中批量选择物料，填写领料数量和批次号</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>保存领料单：</strong>确认明细无误后点击「确定」保存，领料单状态为「待领料」</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>确认领料：</strong>在列表中点击「确认领料」按钮，在弹窗中指定领料人和领料时间（可微调领料数量），确认后状态变为「已领料」</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>退料处理：</strong>如需退料，请到「退料管理」模块创建独立的退料单，不影响领料单状态</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
    <!-- ===== 确认领料弹窗 ===== -->
    <el-dialog v-model="confirmOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon" style="background:#ecfdf5;color:#047857"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <span class="rd-detail-header-title">确认领料</span>
          <div class="rd-detail-header-sub" v-if="confirmData.issueNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ confirmData.issueNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <el-alert type="success" :closable="false" show-icon style="margin-bottom:16px">
          <template #title>请核对以下领料信息，确认无误后点击「确认领料」按钮，状态将变为「已领料」并记录领料时间。</template>
        </el-alert>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc0" style="display:block;padding:16px 20px"><el-form ref="confirmFormRef" :model="confirmData" label-width="100px" size="default"><el-row :gutter="20"><el-col :span="8"><el-form-item label="领料编号"><span class="rd-static-text">{{ confirmData.issueNo || '-' }}</span></el-form-item></el-col><el-col :span="8"><el-form-item label="工单编号"><span class="rd-static-text">{{ confirmData.workOrderNo || '-' }}</span></el-form-item></el-col><el-col :span="8"><el-form-item label="当前状态"><dict-tag :options="mms_issue_status" :value="confirmData.status" /></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="8"><el-form-item label="产品编码"><span class="rd-static-text">{{ confirmData.productCode || '-' }}</span></el-form-item></el-col><el-col :span="8"><el-form-item label="产品名称"><span class="rd-static-text">{{ confirmData.productName || '-' }}</span></el-form-item></el-col><el-col :span="8"><el-form-item label="工单数量"><span class="rd-static-text">{{ confirmData.planQty != null ? confirmData.planQty : '-' }}</span></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="8"><el-form-item label="领料人" prop="issueBy" :rules="[{ required: true, message: '请选择领料人', trigger: 'change' }]"><el-input v-model="confirmData.issueBy" readonly placeholder="请选择领料人" style="width: 100%" @click="openConfirmIssueByPicker"><template #append><el-button icon="Search" @click="openConfirmIssueByPicker" /></template><template #suffix><el-icon v-if="confirmData.issueBy" class="rd-form-tip" style="cursor:pointer" @click.stop="confirmData.issueBy = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="8"><el-form-item label="领料时间" prop="issueTime" :rules="[{ required: true, message: '请选择领料时间', trigger: 'change' }]"><el-date-picker v-model="confirmData.issueTime" type="datetime" placeholder="选择领料时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col></el-row></el-form></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>领料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc1" style="display:block;padding:12px 20px">
            <el-table :data="confirmData.detailList" border size="small" style="width:100%">
              <el-table-column label="序号" prop="seq" width="60" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="120" show-overflow-tooltip />
              <el-table-column label="物料名称" prop="materialName" min-width="140" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" width="100" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="60" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="单件用量" prop="usageQty" width="80" align="center" />
              <el-table-column label="建议数量" prop="suggestedQty" width="90" align="center" />
              <el-table-column label="领料数量" width="130" align="center"><template #header><span style="color:#f56c6c">* 领料数量</span></template><template #default="scope"><el-input-number v-model="scope.row.issueQty" :min="0" :precision="2" :controls="false" size="small" style="width: 100%" placeholder="数量" /></template></el-table-column>
              <el-table-column label="批次号" prop="batchNo" width="110" />
            </el-table>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="confirmOpen = false">取 消</el-button><el-button type="success" :loading="confirmLoading" @click="submitConfirm">确 认 领 料</el-button></template>
    </el-dialog>
    <!-- ===== 工单选择器弹窗 ===== -->
    <el-dialog v-model="woPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">选择工单</span></div></template>
      <div class="material-picker"><div class="material-picker-search"><el-input v-model="woPickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px" @keyup.enter="handleWoPickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-input v-model="woPickerQuery.productName" placeholder="产品名称" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleWoPickerQuery" /><el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleWoPickerQuery">查询</el-button><el-button icon="RefreshLeft" size="small" @click="resetWoPickerQuery">重置</el-button></div><div class="material-picker-table"><el-table v-loading="woPickerLoading" :data="woPickerList" highlight-current-row @row-click="onWoRowClick" @row-dblclick="onWoRowDblClick" height="360" size="small" border><el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="woPickerSelectedId" :value="row.workOrderId" @click.stop="onWoRowClick(row)"><span /></el-radio></template></el-table-column><el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip /><el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip /><el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip /><el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip /><el-table-column label="计划数量" prop="planQty" width="90" align="center" /><el-table-column label="状态" prop="status" width="90" align="center"><template #default="scope"><dict-tag :options="mms_workorder_status" :value="scope.row.status" /></template></el-table-column></el-table></div><div class="material-picker-pager"><el-pagination v-model:current-page="woPickerQuery.pageNum" v-model:page-size="woPickerQuery.pageSize" :total="woPickerTotal" layout="total, prev, pager, next" small @current-change="getWoPickerList" /></div></div>
      <template #footer><el-button @click="woPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWoPickerConfirm" :disabled="!woPickerSelectedId">确 定</el-button></template>
    </el-dialog>
    <!-- ===== 物料选择器弹窗（多选，基于工单BOM快照） ===== -->
    <el-dialog v-model="matPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div><span class="rd-detail-header-title">选择物料（来源：工单BOM）</span></div></template>
      <div class="material-picker"><div class="material-picker-table"><el-table v-loading="matPickerLoading" :data="woBomList" height="360" size="small" border @selection-change="onMatSelectionChange"><el-table-column type="selection" width="45" align="center" /><el-table-column label="序号" prop="seq" width="60" align="center" /><el-table-column label="物料编码" prop="materialCode" width="140" show-overflow-tooltip /><el-table-column label="物料名称" prop="materialName" min-width="160" show-overflow-tooltip /><el-table-column label="规格型号" prop="specModel" width="130" show-overflow-tooltip /><el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column><el-table-column label="单件用量" prop="usageQty" width="90" align="center" /><el-table-column label="关键料" prop="isKeyMaterial" width="70" align="center"><template #default="scope"><el-tag v-if="scope.row.isKeyMaterial === '1'" type="danger" size="small">是</el-tag><span v-else>—</span></template></el-table-column></el-table></div><div v-if="woBomList.length === 0 && !matPickerLoading" style="text-align:center;padding:20px;color:#909399;font-size:13px">该工单暂无BOM快照数据，请确认工单已下达</div></div>
      <template #footer><el-button @click="matPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleMatPickerConfirm" :disabled="matPickerSelectedRows.length === 0">确 定（已选 {{ matPickerSelectedRows.length }} 项）</el-button></template>
    </el-dialog>
    <!-- ===== 人员选择器 ===== -->
    <user-picker ref="userPickerRef" title="选择领料人" @confirm="onUserPickerConfirm" />
    <user-picker ref="confirmUserPickerRef" title="选择领料人" @confirm="onConfirmUserPickerConfirm" />
  </div>
</template>

<script setup name="Issue">
import { listIssue, getIssue, addIssue, updateIssue, delIssue, confirmIssue } from "@/api/mms/issue";
import { listWorkOrder, getWorkOrderBomSnapshot } from "@/api/mms/workorder";
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_issue_status, mms_workorder_status, wms_unit } = proxy.useDict("mms_issue_status", "mms_workorder_status", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_issue_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c3","vc0","vc1","vc3","cc0","cc1"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);

const statusTabList = computed(() => mms_issue_status.value ? mms_issue_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { issueNo: { label: '领料编号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, materialCode: { label: '物料编码', visible: true }, materialName: { label: '物料名称', visible: true }, specModel: { label: '规格型号', visible: true }, unit: { label: '单位', visible: true }, issueQty: { label: '领料数量', visible: true }, issueBy: { label: '领料人', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_issue_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.issueNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.materialCode) c++; if (queryParams.value.materialName) c++; if (queryParams.value.status) c++; if (queryParams.value.issueBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, issueNo: undefined, workOrderNo: undefined, materialCode: undefined, materialName: undefined, status: undefined, issueBy: undefined, params: {} }, rules: { issueNo: [{ required: false, message: "领料编号自动生成", trigger: "blur" }], workOrderNo: [{ required: true, message: '请选择工单', trigger: 'change' }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listIssue(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.issueNo) baseQuery.issueNo = queryParams.value.issueNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.materialCode) baseQuery.materialCode = queryParams.value.materialCode; if (queryParams.value.materialName) baseQuery.materialName = queryParams.value.materialName; if (queryParams.value.issueBy) baseQuery.issueBy = queryParams.value.issueBy; listIssue(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_issue_status.value) { mms_issue_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.issueNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.status = undefined; queryParams.value.issueBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.issueId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { issueNo: undefined, workOrderId: undefined, workOrderNo: undefined, productCode: undefined, productName: undefined, planQty: undefined, issueBy: undefined, issueByName: undefined, status: '0', remark: undefined, detailList: [] }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增领料"; }
function handleUpdate(row) { reset(); const id = row.issueId || ids.value[0]; getIssue(id).then(response => { form.value = response.data; form.value.issueByName = form.value.issueBy; if (!form.value.detailList) form.value.detailList = []; open.value = true; title.value = "修改领料"; }); }
function handleView(row) { const id = row.issueId || ids.value[0]; getIssue(id).then(response => { viewData.value = response.data; if (!viewData.value.detailList) viewData.value.detailList = []; viewOpen.value = true; }); }
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 校验明细
      if (!form.value.detailList || form.value.detailList.length === 0) { proxy.$modal.msgError('请至少添加一条领料明细'); return; }
      for (let i = 0; i < form.value.detailList.length; i++) {
        const d = form.value.detailList[i];
        if (!d.materialCode) { proxy.$modal.msgError('第' + (i + 1) + '行：物料编码不能为空'); return; }
        if (!d.issueQty || d.issueQty <= 0) { proxy.$modal.msgError('第' + (i + 1) + '行：领料数量必须大于0'); return; }
      }
      form.value.issueBy = form.value.issueByName;
      if (form.value.issueId != null) { updateIssue(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); }
      else { addIssue(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); }
    }
  });
}
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.issueId || ids.value; proxy.$modal.confirm('是否确认删除选中的领料记录？').then(() => delIssue(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/issue/export", { ...queryParams.value }, `issue_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_issue_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green' }; return map[status] || 'gray'; }

// ===== 确认领料 =====
const confirmOpen = ref(false); const confirmData = ref({}); const confirmLoading = ref(false);
function handleConfirm(row) {
  const id = row.issueId || ids.value[0];
  getIssue(id).then(response => {
    confirmData.value = response.data;
    if (!confirmData.value.detailList) confirmData.value.detailList = [];
    // 默认领料时间为当前时间
    confirmData.value.issueTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
    confirmOpen.value = true;
  });
}
function submitConfirm() {
  // 校验领料人必填
  if (!confirmData.value.issueBy) {
    proxy.$modal.msgError('请选择领料人');
    return;
  }
  // 校验明细领料数量
  if (!confirmData.value.detailList || confirmData.value.detailList.length === 0) {
    proxy.$modal.msgError('领料明细不能为空');
    return;
  }
  for (let i = 0; i < confirmData.value.detailList.length; i++) {
    const d = confirmData.value.detailList[i];
    if (!d.issueQty || d.issueQty <= 0) {
      proxy.$modal.msgError('第' + (i + 1) + '行领料数量必须大于0');
      return;
    }
  }
  confirmLoading.value = true;
  confirmIssue(confirmData.value).then(() => {
    proxy.$modal.msgSuccess("确认领料成功，状态已变为已领料");
    confirmOpen.value = false;
    confirmLoading.value = false;
    getList();
  }).catch(() => { confirmLoading.value = false; });
}

// 确认弹窗的领料人选择器
function openConfirmIssueByPicker() { proxy.$refs.confirmUserPickerRef.open(); }
function onConfirmUserPickerConfirm(user) {
  confirmData.value.issueBy = user.nickName;
  proxy.$refs["confirmFormRef"] && proxy.$refs["confirmFormRef"].validateField('issueBy');
}

// ===== 工单选择器 =====
const woPickerOpen = ref(false); const woPickerLoading = ref(false); const woPickerList = ref([]); const woPickerTotal = ref(0); const woPickerSelectedId = ref(null); const woPickerSelectedRow = ref(null);
const woPickerQuery = reactive({ pageNum: 1, pageSize: 10, workOrderNo: undefined, productName: undefined });
function openWorkOrderPicker() { woPickerOpen.value = true; woPickerSelectedId.value = null; woPickerSelectedRow.value = null; woPickerQuery.pageNum = 1; woPickerQuery.workOrderNo = undefined; woPickerQuery.productName = undefined; getWoPickerList(); }
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
  form.value.productCode = row.productCode;
  form.value.productName = row.productName;
  form.value.planQty = row.planQty;
  woPickerOpen.value = false;
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo');
  // 清空已选物料明细（因为换了工单）
  form.value.detailList = [];
}
function clearWorkOrder() { form.value.workOrderId = undefined; form.value.workOrderNo = undefined; form.value.productCode = undefined; form.value.productName = undefined; form.value.planQty = undefined; form.value.detailList = []; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('workOrderNo'); }

// ===== 物料选择器（多选，基于工单BOM快照） =====
const matPickerOpen = ref(false); const matPickerLoading = ref(false); const woBomList = ref([]);
const matPickerSelectedRows = ref([]);
function openMaterialPicker() {
  if (!form.value.workOrderId) { proxy.$modal.msgWarning('请先选择工单'); return; }
  // 自动收缩领料明细卡片，避免被弹窗遮挡
  collapsedCards.c1 = true;
  matPickerOpen.value = true;
  matPickerSelectedRows.value = [];
  matPickerLoading.value = true;
  getWorkOrderBomSnapshot(form.value.workOrderId).then(res => {
    woBomList.value = res.data || [];
    matPickerLoading.value = false;
    // 默认选中已添加的物料
    nextTick(() => {
      if (form.value.detailList && form.value.detailList.length > 0) {
        const existingCodes = form.value.detailList.map(d => d.materialCode);
        woBomList.value.forEach(row => {
          if (existingCodes.includes(row.materialCode)) {
            // 需要通过ref来toggle，这里简化处理
          }
        });
      }
    });
  }).catch(() => { matPickerLoading.value = false; });
}
function onMatSelectionChange(selection) { matPickerSelectedRows.value = selection; }
function handleMatPickerConfirm() {
  if (matPickerSelectedRows.value.length === 0) { proxy.$modal.msgWarning('请至少选择一种物料'); return; }
  // 合并已选物料和新选物料（去重）
  const existingCodes = (form.value.detailList || []).map(d => d.materialCode);
  const newRows = matPickerSelectedRows.value.filter(r => !existingCodes.includes(r.materialCode));
  const planQty = Number(form.value.planQty) || 0;
  newRows.forEach(row => {
    const usageQty = Number(row.usageQty) || 0;
    const suggestedQty = Number((usageQty * planQty).toFixed(2));
    form.value.detailList.push({
      materialId: row.materialId,
      materialCode: row.materialCode,
      materialName: row.materialName,
      specModel: row.specModel,
      unit: row.unit,
      usageQty: usageQty,
      suggestedQty: suggestedQty,
      issueQty: suggestedQty,
      batchNo: undefined
    });
  });
  matPickerOpen.value = false;
  // 展开明细卡片，让用户看到刚添加的物料
  collapsedCards.c1 = false;
  if (newRows.length === 0) { proxy.$modal.msgSuccess('所选物料已在列表中'); }
}
function removeDetail(index) { form.value.detailList.splice(index, 1); }

// ===== 领料人选择器 =====
function openIssueByPicker() { proxy.$refs.userPickerRef.open(); }
function onUserPickerConfirm(user) { form.value.issueBy = user.nickName; form.value.issueByName = user.nickName; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('issueBy'); }
function clearIssueBy() { form.value.issueBy = undefined; form.value.issueByName = undefined; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('issueBy'); }

getList();
</script>

<style scoped>
.mms-issue-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-issue-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-issue-page .filter-card{padding:14px 20px 16px}
.mms-issue-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-issue-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-issue-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-issue-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-issue-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-issue-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-issue-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-issue-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-issue-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-issue-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-issue-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-issue-page .field{display:flex;flex-direction:column;gap:6px}
.mms-issue-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-issue-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-issue-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-issue-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-issue-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-issue-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-issue-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-issue-page .field .control :deep(.el-select){width:100%}
.mms-issue-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-issue-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-issue-page .toolbar .left,.mms-issue-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-issue-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-issue-page .table-wrap{overflow-x:auto}
.mms-issue-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-issue-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-issue-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-issue-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-issue-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-issue-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-issue-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-issue-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-issue-page .badge.amber .dot{background:var(--amber-500)}
.mms-issue-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-issue-page .badge.blue .dot{background:var(--blue-500)}
.mms-issue-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-issue-page .badge.green .dot{background:var(--green-500)}
.mms-issue-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-issue-page .badge.red .dot{background:var(--red-500)}
.mms-issue-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-issue-page .badge.gray .dot{background:var(--ink-400)}
.mms-issue-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-issue-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-issue-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-issue-page .tabs-track::-webkit-scrollbar{display:none}
.mms-issue-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-issue-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-issue-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-issue-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-issue-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-issue-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-issue-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-issue-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-issue-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-issue-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
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
.empty-detail-tip{display:flex;align-items:center;justify-content:center;padding:24px;color:var(--ink-400);font-size:13px;background:var(--ink-50);border-radius:var(--r-sm);margin-top:8px}
@media(max-width:1100px){.mms-issue-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-issue-page .filter-card .filter-bar{grid-template-columns:1fr}}
</style>

<style>
/* 全局样式（弹窗 append-to-body 会在 body 下渲染，scoped 样式无法覆盖） */
.rd-static-text{display:inline-block;font-size:14px;color:#303133;line-height:32px}
</style>
