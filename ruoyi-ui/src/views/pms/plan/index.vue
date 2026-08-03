<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24" :xs="24">
        <!-- 搜索区域 -->
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
          <el-form-item label="计划单号" prop="planNo">
            <el-input v-model="queryParams.planNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="计划标题" prop="title">
            <el-input v-model="queryParams.title" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="dict in pms_plan_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="计划类型" prop="planType">
            <el-select v-model="queryParams.planType" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="dict in pms_plan_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
<el-form-item>
<el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
<el-button icon="Refresh" @click="resetQuery">重置</el-button>
</el-form-item>
</el-form>

        <!-- 业务说明提示 -->
        <el-alert type="info" :closable="false" show-icon class="mb8">
          <template #title>
            <div style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
              <span style="font-weight: bold;">采购计划业务说明：</span>
              <el-tag size="small" type="primary" effect="dark">草稿 → 待审核 → 已审批</el-tag>
              <el-tag size="small" type="success" effect="dark">审批通过后可生成采购申请</el-tag>
              <el-tag size="small" type="warning" effect="dark">已关闭后不可修改</el-tag>
              <span style="color: #909399; font-size: 12px;">全流程：新建计划 → 提交审核 → 审批通过 → 生成采购申请</span>
              <el-button link type="primary" size="small" @click="showStatusHelp = true">
                <el-icon><QuestionFilled /></el-icon> 查看详情
              </el-button>
            </div>
          </template>
        </el-alert>

        <!-- 操作按钮区域 -->
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:plan:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:plan:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:plan:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:plan:export']">导出</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 表格区域 -->
        <el-table ref="tableRef" border v-loading="loading" :data="planList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="计划单号" prop="planNo" :width="colWidth('planNo', 160)" resizable sortable="custom" />
          <el-table-column label="计划标题" prop="title" :width="colWidth('title', 200)" resizable show-overflow-tooltip />
          <el-table-column label="计划类型" prop="planType" :width="colWidth('planType', 100)" resizable align="center">
            <template #default="scope"><dict-tag :options="pms_plan_type" :value="scope.row.planType" /></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom">
            <template #default="scope"><dict-tag :options="pms_plan_status" :value="scope.row.status" /></template>
          </el-table-column>
          <el-table-column label="预算金额" prop="budgetAmount" :width="colWidth('budgetAmount', 120)" resizable align="right" sortable="custom">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="实际金额" prop="actualAmount" :width="colWidth('actualAmount', 120)" resizable align="right">
            <template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.actualAmount) }}</span></template>
          </el-table-column>
          <el-table-column label="编制人" prop="plannerName" :width="colWidth('plannerName', 100)" resizable />
          <el-table-column label="编制部门" prop="deptName" :width="colWidth('deptName', 120)" resizable show-overflow-tooltip />
          <el-table-column label="开始日期" prop="startDate" :width="colWidth('startDate', 120)" resizable align="center" />
          <el-table-column label="结束日期" prop="endDate" :width="colWidth('endDate', 120)" resizable align="center" />
          <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
          <el-table-column label="操作" width="330" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['pms:plan:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:plan:edit']" v-if="scope.row.status === '0' || scope.row.status === '6'">修改</el-button>
              <el-button link type="primary" icon="Check" @click="handleAudit(scope.row)" v-hasPermi="['pms:plan:audit']" v-if="scope.row.status === '0' || scope.row.status === '1'">审批</el-button>
              <el-button link type="danger" icon="CircleClose" @click="handleClose(scope.row)" v-hasPermi="['pms:plan:edit']" v-if="scope.row.status === '2' || scope.row.status === '3'">关闭</el-button>
              <el-button link type="primary" icon="Promotion" @click="handleGenerateRequest(scope.row)" v-hasPermi="['pms:request:add']" v-if="scope.row.status === '2'">生成申请</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页区域 -->
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-col>
    </el-row>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="1296px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-9h-2v2H7V2H5v2H4a2 2 0 0 0-2 2v1a3 3 0 0 1 0 6v1a2 2 0 0 0 2 2h1v2h2v-2h8v2h2v-2h1a2 2 0 0 0 2-2v-1a3 3 0 0 1 0-6V6a2 2 0 0 0-2-2z"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.planNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ form.planNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="planRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="计划单号" prop="planNo">
                    <el-input v-model="form.planNo" placeholder="系统自动生成" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="计划标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入计划标题" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="计划类型" prop="planType">
                    <el-select v-model="form.planType" placeholder="请选择计划类型" style="width: 100%">
                      <el-option v-for="dict in pms_plan_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-tag :type="getStatusType(form.status)">{{ getStatusLabel(form.status) }}</el-tag>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开始日期" prop="startDate">
                    <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期" prop="endDate">
                    <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="编制人">
                    <el-input v-model="form.plannerName" readonly placeholder="请选择编制人" style="width: 100%" @click="openUserPicker">
                      <template #append>
                        <el-button icon="Search" @click="openUserPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.plannerName" class="clear-icon" @click.stop="clearPlanner"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="编制部门">
                    <el-input v-model="form.deptName" placeholder="选择编制人后自动带出" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>计划明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" size="small" @click="handleAddDetail">添加明细</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border size="small">
                <el-table-column label="序号" type="index" width="55" align="center" />
                <el-table-column label="物料" prop="materialId" min-width="200"><template #default="scope"><el-select v-model="scope.row.materialId" filterable clearable size="small" placeholder="请选择物料" style="width: 100%" @change="(val) => onMaterialChange(val, scope.$index)"><el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" /></el-select></template></el-table-column>
                <el-table-column label="规格型号" prop="specification" min-width="120"><template #default="scope"><span>{{ scope.row.specification }}</span></template></el-table-column>
                <el-table-column label="单位" prop="unit" width="90"><template #default="scope"><el-select v-model="scope.row.unit" size="small" placeholder="单位" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></template></el-table-column>
                <el-table-column label="计划数量" prop="planQuantity" width="110"><template #default="scope"><el-input-number v-model="scope.row.planQuantity" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calculateAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预算单价" prop="budgetPrice" width="110"><template #default="scope"><el-input-number v-model="scope.row.budgetPrice" :precision="2" :min="0" :controls="false" size="small" style="width: 90px" @change="calculateAmount(scope.row)" /></template></el-table-column>
                <el-table-column label="预算金额" prop="budgetAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template></el-table-column>
                <el-table-column label="需求日期" prop="requireDate" width="150"><template #default="scope"><el-date-picker v-model="scope.row.requireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" size="small" style="width: 100%" /></template></el-table-column>
                <el-table-column label="备注" prop="remark" min-width="120"><template #default="scope"><el-input v-model="scope.row.remark" placeholder="备注" size="small" /></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" size="small" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
              <div class="detail-summary" v-if="form.detailList && form.detailList.length > 0">
                <span>合计：计划数量 {{ totalQuantity }} | 预算金额 <span class="rd-amount">{{ formatMoney(totalBudgetAmount) }}</span></span>
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
                  <el-form-item label="计划说明" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入计划说明" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <!-- 审核记录（含驳回提示） -->
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
            <div class="rd-card-body" style="display:block">
              <el-alert v-if="form.status === '6'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该计划已被驳回，请根据审核意见修改后重新提交</template>
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
    <el-dialog v-model="viewOpen" width="1296px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H7v6h2v-6zm4 0h-2v6h2v-6zm4 0h-2v6h2v-6zm2-9h-2v2H7V2H5v2H4a2 2 0 0 0-2 2v1a3 3 0 0 1 0 6v1a2 2 0 0 0 2 2h1v2h2v-2h8v2h2v-2h1a2 2 0 0 0 2-2v-1a3 3 0 0 1 0-6V6a2 2 0 0 0-2-2z"/></svg>
          </div>
          <span class="rd-detail-header-title">采购计划详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.planNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ viewData.planNo }}</span>
          </div>
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
              <div class="rd-item"><span class="rd-label">计划单号</span><div class="rd-value">{{ viewData.planNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划标题</span><div class="rd-value">{{ viewData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划类型</span><div class="rd-value"><dict-tag :options="pms_plan_type" :value="viewData.planType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_plan_status" :value="viewData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ viewData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ viewData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预算金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.budgetAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">实际金额</span><div class="rd-value rd-amount">{{ formatMoney(viewData.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">编制人</span><div class="rd-value">{{ viewData.plannerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">编制部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>计划明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.v2">
            <el-table :data="viewData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specification" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="计划数量" prop="planQuantity" width="110" align="right"><template #default="scope">{{ formatAmount(scope.row.planQuantity) }}</template></el-table-column>
              <el-table-column label="预算单价" prop="budgetPrice" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetPrice) }}</span></template></el-table-column>
              <el-table-column label="预算金额" prop="budgetAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template></el-table-column>
              <el-table-column label="需求日期" prop="requireDate" width="120" align="center" />
              <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
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
              <div class="rd-item rd-item--full"><span class="rd-label">计划说明</span><div class="rd-value">{{ viewData.description || '-' }}</div></div>
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
    <el-dialog v-model="auditOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">采购计划审核</span>
          <div class="rd-detail-header-sub" v-if="auditData.planNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.planNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 信息摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>计划信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">计划单号</span><div class="rd-value">{{ auditData.planNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划标题</span><div class="rd-value">{{ auditData.title || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划类型</span><div class="rd-value"><dict-tag :options="pms_plan_type" :value="auditData.planType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_plan_status" :value="auditData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">开始日期</span><div class="rd-value">{{ auditData.startDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">结束日期</span><div class="rd-value">{{ auditData.endDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">预算金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.budgetAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">实际金额</span><div class="rd-value rd-amount">{{ formatMoney(auditData.actualAmount) }}</div></div>
              <div class="rd-item"><span class="rd-label">编制人</span><div class="rd-value">{{ auditData.plannerName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">编制部门</span><div class="rd-value">{{ auditData.deptName || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">计划说明</span><div class="rd-value">{{ auditData.description || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 计划明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>计划明细</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="auditData.detailList" border size="small">
              <el-table-column label="序号" type="index" width="55" align="center" />
              <el-table-column label="物料编码" prop="materialCode" min-width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip />
              <el-table-column label="规格型号" prop="specification" min-width="120" show-overflow-tooltip />
              <el-table-column label="单位" prop="unit" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="计划数量" prop="planQuantity" width="110" align="right"><template #default="scope">{{ formatAmount(scope.row.planQuantity) }}</template></el-table-column>
              <el-table-column label="预算单价" prop="budgetPrice" width="110" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetPrice) }}</span></template></el-table-column>
              <el-table-column label="预算金额" prop="budgetAmount" width="120" align="right"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.budgetAmount) }}</span></template></el-table-column>
              <el-table-column label="需求日期" prop="requireDate" width="120" align="center" />
              <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
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

    <!-- 编制人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择编制人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="采购计划业务操作说明" width="700px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审核</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已关闭</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">任意状态可作废</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="success">已审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="primary">生成采购申请</el-tag>
            <el-tag size="small" type="info">自动带出计划明细</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">
            新建采购计划后的初始状态，可编辑、删除、提交审核
          </el-descriptions-item>
          <el-descriptions-item label="待审核">
            提交审核后等待审批，可审核通过或驳回（驳回回退为草稿）
          </el-descriptions-item>
          <el-descriptions-item label="已审批">
            审核通过，可生成采购申请。已审批的计划不可修改，可关闭
          </el-descriptions-item>
          <el-descriptions-item label="已关闭">
            已审批的计划可手动关闭，关闭后不再允许生成采购申请
          </el-descriptions-item>
          <el-descriptions-item label="已作废">
            采购计划被作废，不再有效
          </el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">生成采购申请</div>
              <div class="highlight-card-body">
                已审批的计划可点击「生成采购申请」按钮，系统自动跳转到采购申请页面并<strong>自动带出计划明细</strong>，无需手动重新录入
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">预算金额自动计算</div>
              <div class="highlight-card-body">
                预算金额 = <strong>计划数量 × 预算单价</strong>，编辑明细行数量或单价后自动计算，总额自动汇总
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">审核后不可修改</div>
              <div class="highlight-card-body">
                采购计划审核通过后进入「已审批」状态，<strong>不可再修改计划内容</strong>，仅可关闭或生成采购申请
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
            <strong>新建计划：</strong>填写计划标题、计划类型（年度/季度/月度/临时），选择物料并填写计划数量和预算单价后保存
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交审核：</strong>点击「提交审核」按钮，采购计划进入「待审核」状态，等待审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>审核通过：</strong>审核通过后进入「已审批」状态，可点击「生成采购申请」按钮跳转采购申请页面并自动带出明细
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>审核驳回：</strong>审核驳回后回退为「草稿」状态，可修改后重新提交审核
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>关闭计划：</strong>已审批的计划可手动关闭，关闭后不再允许生成采购申请
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsPlan">
import { listPlan, getPlan, delPlan, addPlan, updatePlan, auditPlan, closePlan } from "@/api/pms/plan";
import { getUserProfile } from "@/api/system/user";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import { listMaterial } from '@/api/wms/material'
import UserPicker from '@/components/UserPicker/index.vue'
import { CircleClose, QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const { proxy } = getCurrentInstance();
const { pms_plan_status, pms_plan_type, wms_unit } = proxy.useDict('pms_plan_status', 'pms_plan_type', 'wms_unit');
const { collapsedCards, toggleCard } = useDetailCard(["c1","c2","c0","c3","v1","v2","v3"])
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_plan_index')

const planList = ref([]);
const showStatusHelp = ref(false);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const auditOpen = ref(false);
const auditData = ref({});
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const materialOptions = ref([]);

const data = reactive({
  form: {
    planId: null,
    planNo: null,
    title: null,
    status: '0',
    planType: '2',
    startDate: null,
    endDate: null,
    budgetAmount: 0,
    actualAmount: 0,
    description: null,
    plannerId: null,
    plannerName: null,
    deptId: null,
    deptName: null,
    detailList: []
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    planNo: null,
    title: null,
    status: null,
planType: null,
params: {}
  },
  auditForm: {
    planId: null,
    auditOpinion: null
  },
  rules: {
    title: [{ required: true, message: "计划标题不能为空", trigger: "blur" }],
    planType: [{ required: true, message: "计划类型不能为空", trigger: "change" }],
    startDate: [{ required: true, message: "开始日期不能为空", trigger: "change" }],
    endDate: [{ required: true, message: "结束日期不能为空", trigger: "change" }]
  },
  auditRules: {
    auditOpinion: [{ required: true, message: "请输入审批意见", trigger: "blur" }]
  }
});

const { queryParams, form, auditForm, rules, auditRules } = toRefs(data);

// 计算属性
const totalQuantity = computed(() => {
  if (!form.value.detailList) return 0;
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.planQuantity) || 0), 0).toFixed(2);
});

const totalBudgetAmount = computed(() => {
  if (!form.value.detailList) return 0;
  return form.value.detailList.reduce((sum, item) => sum + (Number(item.budgetAmount) || 0), 0);
});

/** 查询采购计划列表 */
function getList() {
  loading.value = true;
  listPlan(queryParams.value).then(response => {
    planList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    planId: null,
    planNo: null,
    title: null,
    status: '0',
    planType: '2',
    startDate: null,
    endDate: null,
    budgetAmount: 0,
    actualAmount: 0,
    description: null,
    plannerId: null,
    plannerName: null,
    deptId: null,
    deptName: null,
    detailList: []
  };
  proxy.resetForm("planRef");
}

/** 搜索按钮操作 */
function handleQuery() {
queryParams.value.pageNum = 1;
getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  queryParams.value.params = {};
  handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.planId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop;
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.value.params.orderByColumn = undefined;
    queryParams.value.params.isAsc = undefined;
  }
  getList();
}

/** 新增按钮操作 */
async function handleAdd() {
  reset();
  const userRes = await getUserProfile();
  form.value.plannerId = userRes.data.userId;
  form.value.plannerName = userRes.data.userName;
  form.value.deptId = userRes.data.deptId;
  form.value.deptName = userRes.data.deptName;
  open.value = true;
  title.value = "添加采购计划";
}

/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  const planId = row.planId || ids.value[0];
  const response = await getPlan(planId);
  form.value = response.data;
  if (!form.value.detailList) {
    form.value.detailList = [];
  }
  open.value = true;
  title.value = "修改采购计划";
}

/** 查看按钮操作 */
async function handleView(row) {
  const response = await getPlan(row.planId);
  viewData.value = response.data;
  if (!viewData.value.detailList) {
    viewData.value.detailList = [];
  }
  viewOpen.value = true;
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["planRef"].validate(valid => {
    if (valid) {
      form.value.budgetAmount = totalBudgetAmount.value;
      if (form.value.planId != null) {
        updatePlan(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPlan(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const planIds = row.planId ? [row.planId] : ids.value;
  proxy.$modal.confirm('是否确认删除采购计划编号为"' + planIds + '"的数据项？').then(function() {
    return delPlan(planIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 审核按钮操作 */
async function handleAudit(row) {
  const response = await getPlan(row.planId);
  auditData.value = response.data;
  auditForm.value = {
    planId: row.planId,
    auditOpinion: null
  };
  auditOpen.value = true;
}

/** 提交审核 */
function submitAudit(passed) {
  proxy.$refs["auditRef"].validate(valid => {
    if (valid) {
      const status = passed ? '2' : '6';
      const actionText = passed ? '通过' : '驳回';
      proxy.$modal.confirm(`确认${actionText}该采购计划？`).then(() => {
        return auditPlan(auditForm.value.planId, status, auditForm.value.auditOpinion);
      }).then(() => {
        proxy.$modal.msgSuccess("审批成功");
        auditOpen.value = false;
        getList();
      }).catch(() => {});
    }
  });
}

/** 关闭按钮操作 */
function handleClose(row) {
  proxy.$modal.confirm('是否确认关闭该采购计划？').then(function() {
    return closePlan(row.planId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("关闭成功");
  }).catch(() => {});
}

/** 生成采购申请 — 跳转到采购需求页面并传递planId */
function handleGenerateRequest(row) {
  router.push({ path: '/pms/demand/request', query: { planId: row.planId } });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("pms/plan/export", {
    ...queryParams.value
  }, `plan_${new Date().getTime()}.xlsx`);
}

/** 添加明细 */
function handleAddDetail() {
  if (!form.value.detailList) form.value.detailList = [];
  form.value.detailList.push({
    materialId: null,
    materialCode: '',
    materialName: '',
    specification: '',
    unit: '',
    planQuantity: 0,
    budgetPrice: 0,
    budgetAmount: 0,
    requireDate: null,
    remark: null
  });
}

/** 选择物料后自动带出物料信息 */
function onMaterialChange(val, index) {
  const matched = materialOptions.value.find(m => m.materialId === val);
  if (matched) {
    form.value.detailList[index].materialCode = matched.materialCode;
    form.value.detailList[index].materialName = matched.materialName;
    form.value.detailList[index].specification = matched.specModel;
    form.value.detailList[index].unit = matched.unit;
  }
}

/** 加载物料主数据选项 */
function loadMaterialOptions() {
  listMaterial({ pageNum: 1, pageSize: 999, status: '0' }).then(res => {
    materialOptions.value = res.rows || [];
  });
}

/** 删除明细 */
function handleDeleteDetail(index) {
  form.value.detailList.splice(index, 1);
}

function calculateAmount(row) {
  const quantity = Number(row.planQuantity) || 0;
  const price = Number(row.budgetPrice) || 0;
  row.budgetAmount = quantity * price;
}

/** 获取状态类型 */
function getStatusType(status) {
  const typeMap = {
    '0': 'info',
    '1': 'warning',
    '2': 'success',
    '3': 'primary',
    '4': 'success',
    '5': 'info',
    '6': 'danger'
  };
  return typeMap[status] || 'info';
}

/** 获取状态标签 */
function getStatusLabel(status) {
  const labelMap = {
    '0': '草稿',
    '1': '待审批',
    '2': '审批通过',
    '3': '执行中',
    '4': '已完成',
    '5': '已关闭',
    '6': '已驳回'
  };
  return labelMap[status] || '未知';
}

/** 打开编制人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.plannerId)
}
/** 编制人选择确认回调 — 自动带出所在部门 */
function onUserPickerConfirm(user) {
  form.value.plannerId = user.userId
  form.value.plannerName = user.nickName
  if (user.deptId) {
    form.value.deptId = user.deptId
    form.value.deptName = user.deptName
  }
}
/** 清除编制人 */
function clearPlanner() {
  form.value.plannerId = undefined
  form.value.plannerName = undefined
  form.value.deptId = undefined
  form.value.deptName = undefined
}

loadMaterialOptions();
getList();
onActivated(() => { getList(); })
</script>

<style scoped>
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
</style>
