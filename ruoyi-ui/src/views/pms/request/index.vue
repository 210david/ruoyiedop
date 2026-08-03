<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="申请单号" prop="requestNo">
        <el-input v-model="queryParams.requestNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="d in pms_request_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="queryParams.priority" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in pms_priority" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
<el-form-item>
<el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
<el-button icon="Refresh" @click="resetQuery">重置</el-button>
<el-button type="info" plain :icon="showAdvanced ? 'ArrowUp' : 'ArrowDown'" @click="showAdvanced = !showAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button>
</el-form-item>
<!-- 高级查询条件（内联展开） -->
<el-form-item label="申请人" prop="requesterName" v-show="showAdvanced"><el-input v-model="queryParams.requesterName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
<el-form-item label="审批人" prop="auditBy" v-show="showAdvanced"><el-input v-model="queryParams.auditBy" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
<el-form-item label="采购用途" prop="purpose" v-show="showAdvanced"><el-input v-model="queryParams.purpose" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
<el-form-item label="创建时间" v-show="showAdvanced"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
</el-form>


    <!-- 业务说明提示 -->
    <el-alert type="info" :closable="false" show-icon class="mb8">
      <template #title>
        <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
          <span style="font-weight: bold;">采购申请业务说明：</span>
          <el-tag size="small" type="primary" effect="dark">草稿 → 待审批 → 已审批 → 已转采购</el-tag>
          <el-tag size="small" type="success" effect="dark">可关联采购计划自动带出明细</el-tag>
          <el-tag size="small" type="warning" effect="dark">审批通过后可转采购订单</el-tag>
          <span style="color: #909399; font-size: 12px;">全流程：新建申请 → 提交审批 → 审批通过 → 转采购</span>
          <el-button link type="primary" size="small" @click="showStatusHelp = true">
            <el-icon><QuestionFilled /></el-icon> 查看详情
          </el-button>
        </div>
      </template>
    </el-alert>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:request:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:request:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:request:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:request:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- ===== 列表表格 ===== -->
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="申请单号" prop="requestNo" :width="colWidth('requestNo', 160)" resizable sortable="custom" />
      <el-table-column label="标题" prop="title" :width="colWidth('title', 200)" resizable show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom">
        <template #default="scope"><dict-tag :options="pms_request_status" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="优先级" prop="priority" :width="colWidth('priority', 90)" resizable align="center" sortable="custom">
        <template #default="scope"><dict-tag :options="pms_priority" :value="scope.row.priority" /></template>
      </el-table-column>
      <el-table-column label="期望交货日期" prop="expectDate" :width="colWidth('expectDate', 130)" resizable align="center" sortable="custom" />
      <el-table-column label="预估总金额" prop="totalAmount" :width="colWidth('totalAmount', 130)" resizable align="right" sortable="custom">
        <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.totalAmount) }}</span></template>
      </el-table-column>
      <el-table-column label="申请人" prop="requesterName" :width="colWidth('requesterName', 100)" resizable />
      <el-table-column label="申请部门" prop="deptName" :width="colWidth('deptName', 140)" resizable show-overflow-tooltip />
      <el-table-column label="审批人" prop="auditBy" :width="colWidth('auditBy', 100)" resizable />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:request:edit']" v-if="scope.row.status === '0' || scope.row.status === '3'">修改</el-button>
              <el-button link type="primary" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:request:approve']" v-if="scope.row.status === '0' || scope.row.status === '1'">审批</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:request:remove']" v-if="scope.row.status === '0'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

<!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="1190px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="requestRef" :model="form" :rules="rules" label-width="130px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="申请单号" prop="requestNo">
                    <el-input v-model="form.requestNo" placeholder="保存后自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="申请标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入申请标题" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="优先级" prop="priority">
                    <el-select v-model="form.priority" placeholder="请选择" style="width: 100%">
                      <el-option v-for="d in pms_priority" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="期望交货日期" prop="expectDate">
                    <el-date-picker v-model="form.expectDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="申请人" prop="requesterName">
                    <el-input v-model="form.requesterName" readonly placeholder="请选择申请人" style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.requesterName" class="clear-icon" @click.stop="clearRequester"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="申请部门" prop="deptName">
                    <el-input v-model="form.deptName" placeholder="选择申请人后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="预估总金额" prop="totalAmount">
                    <el-input-number v-model="form.totalAmount" :precision="2" :min="0" disabled style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="planId">
                    <template #label>
                      <span>关联采购计划</span>
                      <el-tooltip content="仅可选择审批通过的采购计划；选择后将自动带出计划明细" placement="top">
                        <el-icon class="rd-form-tip"><question-filled /></el-icon>
                      </el-tooltip>
                    </template>
                    <el-select v-model="form.planId" clearable filterable placeholder="可选，选择后自动带出明细" style="width: 100%" @change="onPlanChange">
                      <template #empty>
                        <div class="rd-select-empty">暂无审批通过的采购计划</div>
                      </template>
                      <el-option v-for="p in planOptions" :key="p.planId" :label="p.planNo + ' - ' + p.title" :value="p.planId" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="采购用途" prop="purpose">
                    <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="请输入采购用途" />
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
                <el-table-column label="物料" prop="materialId" min-width="200"><template #default="scope"><el-select v-model="scope.row.materialId" filterable clearable size="small" placeholder="请选择物料" style="width: 100%" @change="(val) => onMaterialChange(val, scope.$index)"><el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" /></el-select></template></el-table-column>
                <el-table-column label="规格型号" prop="specModel" min-width="120"><template #default="scope"><span>{{ scope.row.specModel }}</span></template></el-table-column>
                <el-table-column label="单位" prop="unit" width="90"><template #default="scope"><el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="数量" prop="qty" width="100"><template #default="scope"><el-input-number v-model="scope.row.qty" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calcDetailAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预估单价" prop="estimatedPrice" width="110"><template #default="scope"><el-input-number v-model="scope.row.estimatedPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 100px" @change="calcDetailAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预估金额" prop="estimatedAmount" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedAmount) }}</span></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="24"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <!-- 审核记录（含驳回提示） -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
            <div class="rd-card-body" style="display:block">
              <el-alert v-if="form.status === '3'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该申请已被驳回，请根据审核意见修改后重新提交</template>
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
    <el-dialog v-model="viewOpen" width="1190px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">采购申请详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.requestNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.requestNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">申请单号</span><div class="rd-value">{{ viewData.requestNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">标题</span><div class="rd-value">{{ viewData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_request_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="pms_priority" :value="viewData.priority" /></div></div>
              <div class="rd-item"><span class="rd-label">期望交货日期</span><div class="rd-value">{{ viewData.expectDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预估总金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ viewData.requesterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
              <div class="rd-item" v-if="viewData.planNo"><span class="rd-label">关联计划</span><div class="rd-value">{{ viewData.planNo }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">采购用途</span><div class="rd-value">{{ viewData.purpose || '-' }}</div></div>
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
              <el-table-column label="数量" prop="qty" width="100" align="right" />
              <el-table-column label="预估单价" prop="estimatedPrice" width="110" align="right" />
              <el-table-column label="预估金额" prop="estimatedAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedAmount) }}</span></template></el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v3">
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
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditOpen" width="1060px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">采购申请审核</span>
          <div class="rd-detail-header-sub" v-if="auditData.requestNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.requestNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>申请信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">申请单号</span><div class="rd-value">{{ auditData.requestNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请标题</span><div class="rd-value">{{ auditData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_request_status" :value="auditData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="pms_priority" :value="auditData.priority" /></div></div>
              <div class="rd-item"><span class="rd-label">期望交货日期</span><div class="rd-value">{{ auditData.expectDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预估总额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.totalAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">申请人</span><div class="rd-value">{{ auditData.requesterName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">申请部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
              <div class="rd-item" v-if="auditData.planNo"><span class="rd-label">关联计划</span><div class="rd-value">{{ auditData.planNo }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">采购用途</span><div class="rd-value">{{ auditData.purpose || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 采购明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>采购明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specModel" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="数量" prop="qty" width="100" align="right"><template #default="scope">{{ formatAmount(scope.row.qty) }}</template></el-table-column>
              <el-table-column label="预估单价" prop="estimatedPrice" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedPrice) }}</span></template></el-table-column>
              <el-table-column label="预估金额" prop="estimatedAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.estimatedAmount) }}</span></template></el-table-column>
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
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="110px">
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

    <!-- 申请人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择申请人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="采购申请业务操作说明" width="700px" append-to-body>
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
            <el-tag type="primary">已转采购</el-tag>
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
          <div class="flow-item">
            <el-tag type="info">已作废</el-tag>
            <el-tag size="small" type="info">任意状态可作废</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建采购申请后的初始状态，可编辑、删除、提交审批
          </el-descriptions-item>
          <el-descriptions-item label="待审批">
            提交审批后等待审核，可审核通过或驳回
          </el-descriptions-item>
          <el-descriptions-item label="已审批">
            审核通过，可转为采购订单。已审批的申请不可修改
          </el-descriptions-item>
          <el-descriptions-item label="已驳回">
            审批未通过，回退为草稿状态可修改后重新提交
          </el-descriptions-item>
          <el-descriptions-item label="已转采购">
            已转为采购订单，采购申请流程结束
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            采购申请被作废，不再有效
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">关联采购计划</div>
              <div class="highlight-card-body">
                新建申请时可选择已审批通过的采购计划，系统<strong>自动带出计划明细</strong>（物料、数量、预算单价），无需手动重新录入
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">预估金额自动计算</div>
              <div class="highlight-card-body">
                预估金额 = <strong>数量 × 预估单价</strong>，编辑明细行数量或单价后自动计算，预估总金额自动汇总
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">审批后不可修改</div>
              <div class="highlight-card-body">
                采购申请审批通过后进入「已审批」状态，<strong>不可再修改申请内容</strong>，仅可转为采购订单
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">物料选择自动带出</div>
              <div class="highlight-card-body">
                选择物料后自动带出<strong>物料编码、名称、规格型号、单位</strong>等信息，无需手动填写
              </div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务触发说明</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建申请：</strong>填写申请标题、优先级（普通/紧急/特急）、期望交货日期，选择物料并填写数量和预估单价后保存
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>关联计划（可选）：</strong>可选择已审批通过的采购计划，自动带出计划明细，也可手动添加物料明细
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审批：</strong>点击「提交审批」按钮，采购申请进入「待审批」状态，等待审核
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审核通过：</strong>审核通过后进入「已审批」状态，可转为采购订单进行后续采购流程
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审核驳回：</strong>审核驳回后回退为「草稿」状态，可修改后重新提交审批
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsRequest">
import { listRequest, getRequest, addRequest, updateRequest, delRequest, auditRequest } from '@/api/pms/request'
import { listPlan, getPlan } from '@/api/pms/plan'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import { listMaterial } from '@/api/wms/material'
import UserPicker from '@/components/UserPicker/index.vue'
import { CircleClose, QuestionFilled } from '@element-plus/icons-vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const { proxy } = getCurrentInstance()
const { pms_request_status, pms_priority, wms_unit } = proxy.useDict('pms_request_status', 'pms_priority', 'wms_unit')

const { collapsedCards, toggleCard } = useDetailCard(["c1","c2","c0","v1","v2","v3","a1"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_request_index')

const list = ref([])
const showStatusHelp = ref(false)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const materialOptions = ref([])
const planOptions = ref([])
const auditOpen = ref(false)
const auditData = ref({})
const auditForm = ref({ requestId: undefined, auditOpinion: null })
const auditRules = { auditOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }] }

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    requestNo: undefined,
    title: undefined,
    status: undefined,
    priority: undefined,
    requesterName: undefined,
    auditBy: undefined,
    purpose: undefined,
    params: {}
  },
  rules: {
    title: [{ required: true, message: '申请标题不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const showAdvanced = ref(false)
const dateRange = ref([])

function getList() {
  loading.value = true
  listRequest(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime'); queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; handleQuery() }
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
function handleSelectionChange(selection) { ids.value = selection.map(i => i.requestId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = { requestId: undefined, requestNo: undefined, title: undefined, status: '0', priority: '0', expectDate: undefined, purpose: undefined, totalAmount: 0, requesterId: undefined, requesterName: undefined, deptId: undefined, deptName: undefined, planId: undefined, planNo: undefined, remark: undefined, detailList: [] }
  proxy.resetForm('requestRef')
}

function handleAdd() { reset(); open.value = true; title.value = '添加采购申请' }
function handleUpdate(row) { reset(); getRequest(row.requestId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改采购申请' }) }
function handleView(row) { getRequest(row.requestId).then(res => { viewData.value = res.data; viewOpen.value = true }) }

function calcDetailAmount(row) {
  if (row.qty != null && row.estimatedPrice != null) {
    row.estimatedAmount = parseFloat((row.qty * row.estimatedPrice).toFixed(2))
  }
  let total = 0
  form.value.detailList.forEach(d => { if (d.estimatedAmount != null) total += d.estimatedAmount })
  form.value.totalAmount = parseFloat(total.toFixed(2))
}

function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = []
  form.value.detailList.push({ materialId: null, materialCode: '', materialName: '', specModel: '', unit: '', qty: 0, estimatedPrice: 0, estimatedAmount: 0 })
}

/** 选择物料后自动带出物料信息 */
function onMaterialChange(val, index) {
  const matched = materialOptions.value.find(m => m.materialId === val)
  if (matched) {
    form.value.detailList[index].materialCode = matched.materialCode
    form.value.detailList[index].materialName = matched.materialName
    form.value.detailList[index].specModel = matched.specModel
    form.value.detailList[index].unit = matched.unit
  }
}

/** 加载物料主数据选项 */
function loadMaterialOptions() {
  listMaterial({ pageNum: 1, pageSize: 999, status: '0' }).then(res => {
    materialOptions.value = res.rows || []
  })
}

/** 加载已审批通过的采购计划选项 */
function loadPlanOptions() {
  listPlan({ pageNum: 1, pageSize: 999, status: '2' }).then(res => {
    planOptions.value = res.rows || []
  })
}

/** 选择采购计划后自动带出明细 */
function onPlanChange(val) {
  if (!val) {
    form.value.planId = undefined
    form.value.planNo = undefined
    return
  }
  getPlan(val).then(res => {
    const plan = res.data
    form.value.planId = plan.planId
    form.value.planNo = plan.planNo
    // 自动带出计划明细为采购申请明细
    if (plan.detailList && plan.detailList.length > 0) {
      form.value.detailList = plan.detailList.map(d => ({
        materialId: d.materialId,
        materialCode: d.materialCode,
        materialName: d.materialName,
        specModel: d.specification,
        unit: d.unit,
        qty: d.planQuantity,
        estimatedPrice: d.budgetPrice,
        estimatedAmount: d.budgetAmount
      }))
      calcDetailAmount({})
    }
  })
}

function handleDeleteDetail(index) { form.value.detailList.splice(index, 1); calcDetailAmount({}) }

function submitForm() {
  proxy.$refs['requestRef'].validate(valid => {
    if (valid) {
      if (form.value.requestId != undefined) {
        updateRequest(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addRequest(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

function handleDelete(row) {
  const requestIds = row.requestId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + requestIds + '"的数据？').then(() => delRequest(requestIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

/** 审核按钮操作 */
async function handleAudit(row) {
  const res = await getRequest(row.requestId)
  auditData.value = res.data
  auditForm.value = { requestId: row.requestId, auditOpinion: null }
  auditOpen.value = true
}

/** 提交审核 */
function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const status = passed ? '2' : '3'
      const actionText = passed ? '通过' : '驳回'
      proxy.$modal.confirm(`确认${actionText}该采购申请？`).then(() => {
        return auditRequest(auditForm.value.requestId, status, auditForm.value.auditOpinion)
      }).then(() => {
        proxy.$modal.msgSuccess('审批成功')
        auditOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
}

function handleExport() { proxy.download('pms/request/export', { ...queryParams.value }, `purchase_request_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

/** 打开申请人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.requesterId)
}
/** 申请人选择确认回调 — 自动带出所在部门 */
function onUserPickerConfirm(user) {
  form.value.requesterId = user.userId
  form.value.requesterName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除申请人 */
function clearRequester() {
  form.value.requesterId = undefined
  form.value.requesterName = undefined
  form.value.deptId = undefined
  form.value.deptName = undefined
}

loadMaterialOptions()
loadPlanOptions()
getList()

/** 从采购计划页面跳转过来时，自动打开新增弹窗并预选计划 */
const route = useRoute()
onActivated(() => {
  getList()
  if (route.query.planId) {
    handleAdd()
    nextTick(() => {
      form.value.planId = Number(route.query.planId)
      onPlanChange(form.value.planId)
    })
  }
})
</script>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
.reject-alert {
  margin-bottom: 16px;
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
.highlight-primary {
  background-color: #ecf5ff;
  border-color: #a0cfff;
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
.highlight-primary .highlight-card-title {
  color: #409eff;
}
.highlight-warning .highlight-card-title {
  color: #e6a23c;
}
.highlight-card-body {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
:deep(.el-timeline-item__node) {
  background-color: transparent;
  border: 2px solid;
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
</style>
