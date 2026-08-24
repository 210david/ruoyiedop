<template>
  <div class="app-container mms-return-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>退料编号</label><div class="control"><el-input v-model="queryParams.returnNo" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>工单编号</label><div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料编码</label><div class="control"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>物料名称</label><div class="control"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_return_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>退料人</label><div class="control"><el-input v-model="queryParams.returnBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="{ 'is-active': activeStatusTab === s.value }" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:return:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:return:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:return:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:return:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_return_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column label="退料编号" prop="returnNo" key="returnNo" :width="colWidth('returnNo', 140)" resizable v-if="columns.returnNo.visible" /><el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" /><el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 130)" resizable v-if="columns.materialCode.visible" /><el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 200)" resizable show-overflow-tooltip v-if="columns.materialName.visible" /><el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" /><el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column><el-table-column label="退料数量" prop="returnQty" key="returnQty" :width="colWidth('returnQty', 100)" resizable align="center" v-if="columns.returnQty.visible" /><el-table-column label="退料人" prop="returnBy" key="returnBy" :width="colWidth('returnBy', 90)" resizable align="center" v-if="columns.returnBy.visible" /><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column><el-table-column label="操作" width="280" align="center" fixed="right"><template #default="scope"><el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:return:edit']" v-if="scope.row.status === '0'">修改</el-button><el-button link type="success" icon="Check" @click="handleConfirm(scope.row)" v-hasPermi="['mms:return:confirm']" v-if="scope.row.status === '0'">确认退料</el-button><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:return:remove']" v-if="scope.row.status === '0'">删除</el-button></template></el-table-column></el-table></div>
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="退料编号" prop="returnNo"><el-input v-model="form.returnNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="领料单号" prop="issueNo" :rules="[{ required: true, message: '请选择领料单', trigger: 'change' }]"><el-input v-model="form.issueNo" readonly placeholder="请选择领料单" style="width: 100%" @click="openIssuePicker"><template #append><el-button icon="Search" @click="openIssuePicker" /></template><template #suffix><el-icon v-if="form.issueNo" class="rd-form-tip" style="cursor:pointer" @click.stop="clearIssue"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="8"><el-form-item label="工单编号"><el-input v-model="form.workOrderNo" placeholder="选择领料单后自动带出" disabled /></el-form-item></el-col><el-col :span="8"><el-form-item label="产品编码"><el-input v-model="form.productCode" placeholder="选择领料单后自动带出" disabled /></el-form-item></el-col><el-col :span="8"><el-form-item label="产品名称"><el-input v-model="form.productName" placeholder="选择领料单后自动带出" disabled /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_return_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="退料人" prop="returnBy"><el-input v-model="form.returnByName" readonly placeholder="请选择退料人" style="width: 100%" @click="openReturnByPicker"><template #append><el-button icon="Search" @click="openReturnByPicker" /></template><template #suffix><el-icon v-if="form.returnByName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearReturnBy"><CircleClose /></el-icon></template></el-input></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="退料原因" prop="returnReason"><el-input v-model="form.returnReason" type="textarea" :rows="2" placeholder="请输入退料原因" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>退料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-table :data="form.detailList" border size="small" class="detail-table" style="width:100%">
                <el-table-column label="序号" type="index" width="50" align="center" />
                <el-table-column label="物料编码" prop="materialCode" width="130" show-overflow-tooltip />
                <el-table-column label="物料名称" prop="materialName" min-width="140" show-overflow-tooltip />
                <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
                <el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
                <el-table-column label="领料数量" prop="issueQty" width="90" align="center" />
                <el-table-column label="已退数量" prop="returnedQty" width="90" align="center"><template #default="scope"><span style="color:#e6a23c">{{ scope.row.returnedQty || 0 }}</span></template></el-table-column>
                <el-table-column label="可退数量" prop="availableQty" width="90" align="center"><template #default="scope"><span style="color:#67c23a">{{ scope.row.availableQty || 0 }}</span></template></el-table-column>
                <el-table-column label="退料数量" width="120" align="center"><template #header><span style="color:#f56c6c">* 退料数量</span></template><template #default="scope"><el-input-number v-model="scope.row.returnQty" :min="0" :max="scope.row.availableQty" :precision="2" :controls="false" size="small" style="width: 100%" placeholder="数量" /></template></el-table-column>
                <el-table-column label="批次号" prop="batchNo" width="110" show-overflow-tooltip />
                <el-table-column label="操作" width="70" align="center" fixed="right"><template #default="scope"><el-button link type="danger" icon="Delete" @click="removeDetail(scope.$index)" /></template></el-table-column>
              </el-table>
              <div v-if="!form.detailList || form.detailList.length === 0" class="empty-detail-tip">
                <el-icon style="margin-right:4px"><WarningFilled /></el-icon>暂无退料明细，请先选择领料单
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
          <span class="rd-detail-header-title">退料记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.returnNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.returnNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">退料编号</span><div class="rd-value">{{ viewData.returnNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">领料单号</span><div class="rd-value">{{ viewData.issueNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">退料人</span><div class="rd-value">{{ viewData.returnBy || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_return_status" :value="viewData.status" /></div></div><div class="rd-item rd-item--full"><span class="rd-label">退料原因</span><div class="rd-value">{{ viewData.returnReason || '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>退料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block;padding:12px 20px">
            <el-table :data="viewData.detailList" border size="small" style="width:100%">
              <el-table-column label="序号" prop="seq" width="60" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="130" show-overflow-tooltip />
              <el-table-column label="物料名称" prop="materialName" min-width="140" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="70" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="领料数量" prop="issueQty" width="90" align="center" />
              <el-table-column label="退料数量" prop="returnQty" width="90" align="center" />
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
    <el-dialog v-model="showStatusHelp" title="生产退料业务操作说明" width="984px" append-to-body draggable class="rd-dialog status-help-dialog"><div class="status-help-content"><h4>一、生产退料释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是生产退料？</div><div class="highlight-card-body"><strong>生产退料（Return Material）</strong>是生产管控中记录生产过程中多余物料退回仓库的单据。退料需关联领料单和物料，记录退料数量和退料原因，确保物料库存准确和生产成本正确归集。<br/><br/>生产退料遵循 <strong>MES 物料管理规范</strong>，退料原因分析支持物料损耗管控和成本优化。</div></div><h4>二、重点业务规则</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">核心规则</div><div class="highlight-card-body"><p>• <strong>退料编号：</strong>系统自动生成，唯一标识每笔退料记录</p><p>• <strong>领料关联：</strong>退料需关联领料单，选择领料单后自动带出工单号、产品信息和领料明细</p><p>• <strong>退料原因：</strong>需记录退料原因，便于物料损耗分析</p><p>• <strong>退料数量限制：</strong>退料数量不能超过可退数量（领料数量-已退数量）</p><p>• <strong>多次退料：</strong>同一领料单可多次退料，系统自动累计已退数量并计算可退数量</p><p>• <strong>默认退料人：</strong>选择领料单后自动带出领料人作为退料人</p><p>• <strong>退料确认：</strong>确认退料后状态变为已退料，记录退料人和退料时间</p></div></div><h4>三、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>选择领料单：</strong>点击「新增」后选择已领料的领料单，系统自动带出工单号、产品信息和领料明细</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>填写退料信息：</strong>在领料明细基础上填写退料数量和退料原因</el-timeline-item><el-timeline-item type="info" :hollow="true"><strong>选择退料人：</strong>弹框选择退料人员</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>确认退料：</strong>确认信息无误后保存，待退料状态可确认退料，记录退料人和退料时间</el-timeline-item></el-timeline></div><template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template></el-dialog>
    <!-- ===== 确认退料弹窗 ===== -->
    <el-dialog v-model="confirmOpen" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon" style="background:#ecfdf5;color:#047857"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <span class="rd-detail-header-title">确认退料</span>
          <div class="rd-detail-header-sub" v-if="confirmData.returnNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ confirmData.returnNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <el-alert type="success" :closable="false" show-icon style="margin-bottom:16px">
          <template #title>请核对以下退料信息，确认无误后点击「确认退料」按钮，状态将变为「已退料」并记录退料人和退料时间。</template>
        </el-alert>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">退料编号</span><div class="rd-value">{{ confirmData.returnNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">领料单号</span><div class="rd-value">{{ confirmData.issueNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ confirmData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ confirmData.productCode || '-' }}</div></div><div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ confirmData.productName || '-' }}</div></div><div class="rd-item"><span class="rd-label">当前状态</span><div class="rd-value"><dict-tag :options="mms_return_status" :value="confirmData.status" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg></span>确认信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc2" style="display:block;padding:16px 20px"><el-form ref="confirmFormRef" :model="confirmData" label-width="100px" size="default"><el-row :gutter="20"><el-col :span="12"><el-form-item label="退料人" prop="returnBy" :rules="[{ required: true, message: '请选择退料人', trigger: 'change' }]"><el-input v-model="confirmData.returnBy" readonly placeholder="请选择退料人" style="width: 100%" @click="openConfirmReturnByPicker"><template #append><el-button icon="Search" @click="openConfirmReturnByPicker" /></template><template #suffix><el-icon v-if="confirmData.returnBy" class="rd-form-tip" style="cursor:pointer" @click.stop="confirmData.returnBy = undefined"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="退料时间" prop="returnTime" :rules="[{ required: true, message: '请选择退料时间', trigger: 'change' }]"><el-date-picker v-model="confirmData.returnTime" type="datetime" placeholder="选择退料时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col></el-row><el-row :gutter="20"><el-col :span="24"><el-form-item label="退料原因" prop="returnReason"><el-input v-model="confirmData.returnReason" type="textarea" :rows="2" placeholder="请输入退料原因" /></el-form-item></el-col></el-row></el-form></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('cc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7l-8-4-8 4 8 4 8-4z"/><path d="M4 7v10l8 4 8-4V7"/></svg></span>退料明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.cc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.cc1" style="display:block;padding:14px 24px">
            <el-table :data="confirmData.detailList" border size="small" style="width:100%">
              <el-table-column label="序号" prop="seq" width="72" align="center" />
              <el-table-column label="物料编码" prop="materialCode" width="144" show-overflow-tooltip />
              <el-table-column label="物料名称" prop="materialName" min-width="168" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="72" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="领料数量" prop="issueQty" width="96" align="center" />
              <el-table-column label="已退数量" prop="returnedQty" width="96" align="center"><template #default="scope"><span style="color:#e6a23c">{{ scope.row.returnedQty || 0 }}</span></template></el-table-column>
              <el-table-column label="可退数量" prop="availableQty" width="96" align="center"><template #default="scope"><span style="color:#67c23a">{{ scope.row.availableQty || 0 }}</span></template></el-table-column>
              <el-table-column label="退料数量" width="156" align="center"><template #header><span style="color:#f56c6c">* 退料数量</span></template><template #default="scope"><el-input-number v-model="scope.row.returnQty" :min="0" :max="scope.row.availableQty" :precision="2" :controls="false" size="small" style="width: 100%" placeholder="数量" /></template></el-table-column>
              <el-table-column label="批次号" prop="batchNo" width="132" />
            </el-table>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="confirmOpen = false">取 消</el-button><el-button type="success" :loading="confirmLoading" @click="submitConfirm">确 认 退 料</el-button></template>
    </el-dialog>
    <!-- ===== 领料单选择器弹窗 ===== -->
    <el-dialog v-model="issuePickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">选择领料单</span></div></template>
      <div class="material-picker"><div class="material-picker-search"><el-input v-model="issuePickerQuery.issueNo" placeholder="领料编号" clearable size="small" style="width: 180px" @keyup.enter="handleIssuePickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-input v-model="issuePickerQuery.workOrderNo" placeholder="工单编号" clearable size="small" style="width: 180px; margin-left: 8px" @keyup.enter="handleIssuePickerQuery" /><el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleIssuePickerQuery">查询</el-button><el-button icon="RefreshLeft" size="small" @click="resetIssuePickerQuery">重置</el-button></div><div class="material-picker-table"><el-table v-loading="issuePickerLoading" :data="issuePickerList" highlight-current-row @row-click="onIssueRowClick" @row-dblclick="onIssueRowDblClick" height="360" size="small" border><el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="issuePickerSelectedId" :value="row.issueId" @click.stop="onIssueRowClick(row)"><span /></el-radio></template></el-table-column><el-table-column label="领料编号" prop="issueNo" width="150" show-overflow-tooltip /><el-table-column label="工单编号" prop="workOrderNo" width="150" show-overflow-tooltip /><el-table-column label="产品编码" prop="productCode" width="120" show-overflow-tooltip /><el-table-column label="产品名称" prop="productName" min-width="160" show-overflow-tooltip /><el-table-column label="领料人" prop="issueBy" width="90" align="center" /><el-table-column label="领料时间" prop="issueTime" width="160" align="center"><template #default="scope"><span>{{ scope.row.issueTime ? parseTime(scope.row.issueTime) : '-' }}</span></template></el-table-column></el-table></div><div class="material-picker-pager"><el-pagination v-model:current-page="issuePickerQuery.pageNum" v-model:page-size="issuePickerQuery.pageSize" :total="issuePickerTotal" layout="total, prev, pager, next" small @current-change="getIssuePickerList" /></div></div>
      <template #footer><el-button @click="issuePickerOpen = false">取 消</el-button><el-button type="primary" @click="handleIssuePickerConfirm" :disabled="!issuePickerSelectedId">确 定</el-button></template>
    </el-dialog>
    <!-- ===== 人员选择器 ===== -->
    <user-picker ref="userPickerRef" title="选择退料人" @confirm="onUserPickerConfirm" />
    <user-picker ref="confirmUserPickerRef" title="选择退料人" @confirm="onConfirmUserPickerConfirm" />
  </div>
</template>

<script setup name="ReturnMaterial">
import { listReturnMaterial, getReturnMaterial, addReturnMaterial, updateReturnMaterial, delReturnMaterial, listIssueForReturn, getIssueInfo, confirmReturnMaterial } from "@/api/mms/return";
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_return_status, wms_unit } = proxy.useDict("mms_return_status", "wms_unit");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_return_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c3","vc0","vc1","vc3","cc0","cc1","cc2"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);

const statusTabList = computed(() => mms_return_status.value ? mms_return_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { returnNo: { label: '退料编号', visible: true }, workOrderNo: { label: '工单编号', visible: true }, materialCode: { label: '物料编码', visible: true }, materialName: { label: '物料名称', visible: true }, specModel: { label: '规格型号', visible: true }, unit: { label: '单位', visible: true }, returnQty: { label: '退料数量', visible: true }, returnBy: { label: '退料人', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_return_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.returnNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.materialCode) c++; if (queryParams.value.materialName) c++; if (queryParams.value.status) c++; if (queryParams.value.returnBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, returnNo: undefined, workOrderNo: undefined, materialCode: undefined, materialName: undefined, status: undefined, returnBy: undefined, params: {} }, rules: { returnNo: [{ required: false, message: "退料编号自动生成", trigger: "blur" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listReturnMaterial(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.returnNo) baseQuery.returnNo = queryParams.value.returnNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.materialCode) baseQuery.materialCode = queryParams.value.materialCode; if (queryParams.value.materialName) baseQuery.materialName = queryParams.value.materialName; if (queryParams.value.returnBy) baseQuery.returnBy = queryParams.value.returnBy; listReturnMaterial(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_return_status.value) { mms_return_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.returnNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.status = undefined; queryParams.value.returnBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.returnId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { returnNo: undefined, issueId: undefined, issueNo: undefined, workOrderId: undefined, workOrderNo: undefined, productCode: undefined, productName: undefined, returnBy: undefined, returnByName: undefined, returnReason: undefined, status: '0', remark: undefined, detailList: [] }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增退料"; }
function handleUpdate(row) { reset(); const id = row.returnId || ids.value[0]; getReturnMaterial(id).then(response => { form.value = response.data; form.value.returnByName = form.value.returnBy; if (!form.value.detailList) form.value.detailList = []; open.value = true; title.value = "修改退料"; }); }
function handleView(row) { const id = row.returnId || ids.value[0]; getReturnMaterial(id).then(response => { viewData.value = response.data; if (!viewData.value.detailList) viewData.value.detailList = []; viewOpen.value = true; }); }
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (!form.value.issueId) { proxy.$modal.msgError('请选择领料单'); return; }
      if (!form.value.detailList || form.value.detailList.length === 0) { proxy.$modal.msgError('请至少添加一条退料明细'); return; }
      for (let i = 0; i < form.value.detailList.length; i++) {
        const d = form.value.detailList[i];
        if (!d.materialCode) { proxy.$modal.msgError('第' + (i + 1) + '行：物料编码不能为空'); return; }
        if (!d.returnQty || d.returnQty <= 0) { proxy.$modal.msgError('第' + (i + 1) + '行：退料数量必须大于0'); return; }
        if (d.issueQty && d.returnQty > d.issueQty) { proxy.$modal.msgError('第' + (i + 1) + '行：退料数量不能超过领料数量'); return; }
      }
      form.value.returnBy = form.value.returnByName;
      if (form.value.returnId != null) { updateReturnMaterial(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); }
      else { addReturnMaterial(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); }
    }
  });
}
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.returnId || ids.value; proxy.$modal.confirm('是否确认删除选中的退料记录？').then(() => delReturnMaterial(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/return/export", { ...queryParams.value }, `return_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_return_status, status); }
function badgeClass(status) { const map = { '0': 'amber', '1': 'green' }; return map[status] || 'gray'; }

// ===== 确认退料 =====
const confirmOpen = ref(false); const confirmData = ref({}); const confirmLoading = ref(false);
function handleConfirm(row) {
  const id = row.returnId || ids.value[0];
  getReturnMaterial(id).then(response => {
    const data = response.data;
    // 确保明细中的 returnQty 保留
    if (!data.detailList) data.detailList = [];
    data.returnTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}');
    // 如果有领料单ID，获取领料单信息
    if (data.issueId) {
      getIssueInfo(data.issueId).then(res => {
        const issueData = res.data;
        // 默认退料人等于领料人
        if (!data.returnBy && issueData && issueData.issueBy) {
          data.returnBy = issueData.issueBy;
        }
        // 为确认退料弹窗的明细计算已退数量和可退数量
        if (issueData && issueData.detailList) {
          const issueMap = {};
          issueData.detailList.forEach(d => { issueMap[d.materialCode] = d; });
          data.detailList.forEach(d => {
            const issueDetail = issueMap[d.materialCode];
            if (issueDetail) {
              d.returnedQty = issueDetail.returnedQty || 0;
              d.availableQty = issueDetail.availableQty || 0;
              // 确保 returnQty 不超过可退数量
              if (d.returnQty && d.availableQty && d.returnQty > d.availableQty) {
                d.returnQty = d.availableQty;
              }
            }
          });
        }
        confirmData.value = data;
        confirmOpen.value = true;
      }).catch(() => {
        confirmData.value = data;
        confirmOpen.value = true;
      });
    } else {
      confirmData.value = data;
      confirmOpen.value = true;
    }
  });
}
function submitConfirm() {
  if (!confirmData.value.returnBy) { proxy.$modal.msgError('请选择退料人'); return; }
  if (!confirmData.value.detailList || confirmData.value.detailList.length === 0) { proxy.$modal.msgError('退料明细不能为空'); return; }
  for (let i = 0; i < confirmData.value.detailList.length; i++) {
    const d = confirmData.value.detailList[i];
    if (!d.returnQty || d.returnQty <= 0) { proxy.$modal.msgError('第' + (i + 1) + '行退料数量必须大于0'); return; }
  }
  confirmLoading.value = true;
  confirmReturnMaterial(confirmData.value).then(() => {
    proxy.$modal.msgSuccess("确认退料成功，状态已变为已退料");
    confirmOpen.value = false;
    confirmLoading.value = false;
    getList();
  }).catch(() => { confirmLoading.value = false; });
}
function openConfirmReturnByPicker() { proxy.$refs.confirmUserPickerRef.open(); }
function onConfirmUserPickerConfirm(user) {
  confirmData.value.returnBy = user.nickName;
  proxy.$refs["confirmFormRef"] && proxy.$refs["confirmFormRef"].validateField('returnBy');
}

// ===== 领料单选择器 =====
const issuePickerOpen = ref(false); const issuePickerLoading = ref(false); const issuePickerList = ref([]); const issuePickerTotal = ref(0); const issuePickerSelectedId = ref(null); const issuePickerSelectedRow = ref(null);
const issuePickerQuery = reactive({ pageNum: 1, pageSize: 10, issueNo: undefined, workOrderNo: undefined });
function openIssuePicker() { issuePickerOpen.value = true; issuePickerSelectedId.value = null; issuePickerSelectedRow.value = null; issuePickerQuery.pageNum = 1; issuePickerQuery.issueNo = undefined; issuePickerQuery.workOrderNo = undefined; getIssuePickerList(); }
function getIssuePickerList() { issuePickerLoading.value = true; listIssueForReturn(issuePickerQuery).then(res => { issuePickerList.value = res.rows; issuePickerTotal.value = res.total; issuePickerLoading.value = false; }).catch(() => { issuePickerLoading.value = false; }); }
function handleIssuePickerQuery() { issuePickerQuery.pageNum = 1; getIssuePickerList(); }
function resetIssuePickerQuery() { issuePickerQuery.issueNo = undefined; issuePickerQuery.workOrderNo = undefined; handleIssuePickerQuery(); }
function onIssueRowClick(row) { issuePickerSelectedId.value = row.issueId; issuePickerSelectedRow.value = row; }
function onIssueRowDblClick(row) { onIssueRowClick(row); handleIssuePickerConfirm(); }
function handleIssuePickerConfirm() {
  if (!issuePickerSelectedId.value) { proxy.$modal.msgWarning('请先选择领料单'); return; }
  const row = issuePickerSelectedRow.value;
  // 调用后端获取领料单详情（带明细）
  getIssueInfo(row.issueId).then(res => {
    const issueData = res.data;
    form.value.issueId = issueData.issueId;
    form.value.issueNo = issueData.issueNo;
    form.value.workOrderId = issueData.workOrderId;
    form.value.workOrderNo = issueData.workOrderNo;
    form.value.productCode = issueData.productCode;
    form.value.productName = issueData.productName;
    // 默认退料人等于领料人
    if (issueData.issueBy) {
      form.value.returnBy = issueData.issueBy;
      form.value.returnByName = issueData.issueBy;
    }
    // 将领料明细转化为退料明细（包含已退数量和可退数量）
    form.value.detailList = (issueData.detailList || []).map(d => ({
      materialId: d.materialId,
      materialCode: d.materialCode,
      materialName: d.materialName,
      specModel: d.specModel,
      unit: d.unit,
      issueQty: d.issueQty,
      returnedQty: d.returnedQty || 0,
      availableQty: d.availableQty || 0,
      returnQty: undefined,
      batchNo: d.batchNo
    }));
    issuePickerOpen.value = false;
    proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('issueNo');
  });
}
function clearIssue() { form.value.issueId = undefined; form.value.issueNo = undefined; form.value.workOrderId = undefined; form.value.workOrderNo = undefined; form.value.productCode = undefined; form.value.productName = undefined; form.value.detailList = []; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('issueNo'); }

function removeDetail(index) { form.value.detailList.splice(index, 1); }

// ===== 退料人选择器 =====
function openReturnByPicker() { proxy.$refs.userPickerRef.open(); }
function onUserPickerConfirm(user) { form.value.returnBy = user.nickName; form.value.returnByName = user.nickName; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('returnBy'); }
function clearReturnBy() { form.value.returnBy = undefined; form.value.returnByName = undefined; proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('returnBy'); }

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-return-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.empty-detail-tip{display:flex;align-items:center;justify-content:center;padding:24px;color:var(--ink-400);font-size:13px;background:var(--ink-50);border-radius:var(--r-sm);margin-top:8px}
</style>

