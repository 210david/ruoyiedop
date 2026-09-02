<template>
  <div class="app-container hr-transfer-page">
    <!-- 筛选条件卡片 -->
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
          <label>调动单号</label>
          <div class="control">
            <el-input v-model="queryParams.transferNo" placeholder="请输入调动单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>员工姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入员工姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工号</label>
          <div class="control">
            <el-input v-model="queryParams.employeeNo" placeholder="请输入工号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>审批状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_audit_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>调动类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.transferType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_transfer_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>调出部门</label>
          <div class="control">
            <el-input v-model="queryParams.fromOrgName" placeholder="请输入调出部门" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>申请日期</label>
          <div class="control">
            <el-date-picker v-model="effectiveDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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

    <!-- 数据列表卡片 -->
    <div class="surface">
<!-- 状态统计页签 -->
<div class="status-tabs">
  <div class="tabs-track">
    <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
      <span class="dot"></span>
      <span>全部</span>
      <span class="count">{{ statusCounts.all }}</span>
    </button>
    <button v-for="s in statusTabList" :key="s.value" class="status-tab"
      :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
      @click="handleStatusTabClick(s.value)">
      <span class="dot"></span>
      <span>{{ s.label }}</span>
      <span class="count">{{ statusCounts[s.value] || 0 }}</span>
    </button>
  </div>
  <button class="tip-pill" @click="showStatusHelp = true">
    <el-icon><QuestionFilled /></el-icon>
    <span>业务操作说明</span>
  </button>
</div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:transfer:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:transfer:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:transfer:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:transfer:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_transfer_columns"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrTransferList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="调动单号" prop="transferNo" key="transferNo" align="center" :width="colWidth('transferNo', 180)" resizable v-if="columns.transferNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.transferNo }}</span></template>
          </el-table-column>
          <el-table-column label="员工姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 110)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="工号" prop="employeeNo" key="employeeNo" align="center" :width="colWidth('employeeNo', 120)" resizable v-if="columns.employeeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.employeeNo }}</span></template>
          </el-table-column>
          <el-table-column label="调动类型" prop="transferType" key="transferType" align="center" :width="colWidth('transferType', 120)" resizable v-if="columns.transferType.visible">
            <template #default="scope">
              <span class="badge" :class="transferTypeBadgeClass(scope.row.transferType)" v-if="scope.row.transferType !== undefined && scope.row.transferType !== null && scope.row.transferType !== ''">
                <span class="dot"></span>{{ transferTypeLabel(scope.row.transferType) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="调出部门" prop="fromOrgName" key="fromOrgName" align="center" :width="colWidth('fromOrgName', 140)" resizable v-if="columns.fromOrgName.visible" />
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditBadgeClass(scope.row.auditStatus)"><span class="dot"></span>{{ auditLabel(scope.row.auditStatus) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:transfer:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:transfer:edit']">修改</el-button>
                <el-button link type="warning" icon="Finished" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus === '0'" v-hasPermi="['hr:transfer:audit']">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:transfer:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrTransferRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
          <el-form-item label="调动单号" prop="transferNo">
            <el-input v-model="form.transferNo" placeholder="自动生成" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12"><el-form-item label="姓名" prop="employeeId">
          <el-input :model-value="form.employeeName" readonly placeholder="请选择人员">
            <template #append>
              <el-button v-if="form.employeeId" icon="CircleClose" @click="onEmployeeFieldClick" />
              <el-button v-else icon="Search" @click="onEmployeeFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="工号" prop="employeeNo">
            <el-input v-model="form.employeeNo" placeholder="选择人员后自动带出" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
<el-form-item label="调动类型" prop="transferType">
<el-select v-model="form.transferType" placeholder="请选择" clearable style="width: 100%" @change="onTransferTypeChange"><el-option v-for="d in hr_transfer_type" :key="d.value" :label="d.label" :value="d.value" /></el-select>
</el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="申请日期" prop="effectiveDate">
            <el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><line x1="9" y1="9" x2="9" y2="9"/><line x1="9" y1="13" x2="9" y2="13"/><line x1="9" y1="17" x2="9" y2="17"/></svg></span>调动信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="原部门" prop="fromOrgId">
          <el-input :model-value="form.fromOrgName" readonly placeholder="请选择原部门">
            <template #append>
              <el-button v-if="form.fromOrgId" icon="CircleClose" @click="onFromOrgFieldClick" />
              <el-button v-else icon="Search" @click="onFromOrgFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12"><el-form-item label="目标部门" prop="toOrgId">
          <el-input :model-value="form.toOrgName" readonly placeholder="请选择目标部门">
            <template #append>
              <el-button v-if="form.toOrgId" icon="CircleClose" @click="onToOrgFieldClick" />
              <el-button v-else icon="Search" @click="onToOrgFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12"><el-form-item label="原岗位" prop="fromPosition">
          <el-input :model-value="form.fromPosition" readonly placeholder="请选择原岗位">
            <template #append>
              <el-button v-if="form.fromPosition" icon="CircleClose" @click="onFromPositionFieldClick" />
              <el-button v-else icon="Search" @click="onFromPositionFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12"><el-form-item label="目标岗位" prop="toPosition">
          <el-input :model-value="form.toPosition" readonly placeholder="请选择目标岗位">
            <template #append>
              <el-button v-if="form.toPosition" icon="CircleClose" @click="onToPositionFieldClick" />
              <el-button v-else icon="Search" @click="onToPositionFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="原薪资" prop="fromSalary">
            <el-input-number v-model="form.fromSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标薪资" prop="toSalary">
            <el-input-number v-model="form.toSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
                <el-col :span="24">
          <el-form-item label="调动原因" prop="reason">
            <el-input v-model="form.reason" placeholder="请输入调动原因" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="请输入备注" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2" style="display:block">
              <el-alert v-if="form.auditStatus === '2'" type="warning" :closable="false" show-icon class="reject-alert">
                <template #title>该记录已被驳回，请根据审核意见修改后重新提交</template>
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
                    <div class="rd-timeline-comment" v-if="log.auditRemark"><strong>审核意见：</strong>{{ log.auditRemark }}</div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewOpen" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">调动详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.transferNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">单号：{{ viewData.transferNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">调动单号</span><div class="rd-value">{{ viewData.transferNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">员工姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ viewData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调动类型</span><div class="rd-value"><dict-tag v-if="viewData.transferType !== undefined && viewData.transferType !== null && viewData.transferType !== ''" :options="hr_transfer_type" :value="viewData.transferType" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">申请日期</span><div class="rd-value">{{ viewData.effectiveDate || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><line x1="9" y1="9" x2="9" y2="9"/><line x1="9" y1="13" x2="9" y2="13"/><line x1="9" y1="17" x2="9" y2="17"/></svg></span>调动信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">调出部门</span><div class="rd-value">{{ viewData.fromOrgName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调入部门</span><div class="rd-value">{{ viewData.toOrgName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">原岗位</span><div class="rd-value">{{ viewData.fromPosition || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">目标岗位</span><div class="rd-value">{{ viewData.toPosition || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">原薪资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.fromSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">目标薪资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.toSalary) }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">调动原因</span><div class="rd-value">{{ viewData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-timeline" v-if="viewAuditLogs().length">
              <div class="rd-timeline-item" v-for="(log, idx) in viewAuditLogs()" :key="idx">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime || '-' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark"><strong>审核意见：</strong>{{ log.auditRemark }}</div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
              <p class="rd-empty-text">暂无审核记录</p>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="912px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">调动审批</div>
            <div class="rd-detail-header-sub" v-if="auditData.transferNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">调动单号：{{ auditData.transferNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">调动单号</span><div class="rd-value">{{ auditData.transferNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ auditData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">工号</span><div class="rd-value">{{ auditData.employeeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">当前审批状态</span><div class="rd-value">
                <span class="badge" :class="auditBadgeClass(auditData.auditStatus)" v-if="auditData.auditStatus !== undefined && auditData.auditStatus !== null && auditData.auditStatus !== ''"><span class="dot"></span>{{ auditLabel(auditData.auditStatus) }}</span>
                <span v-else>-</span>
              </div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><line x1="9" y1="9" x2="9" y2="9"/><line x1="9" y1="13" x2="9" y2="13"/><line x1="9" y1="17" x2="9" y2="17"/></svg></span>工作信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">调动类型</span><div class="rd-value"><dict-tag v-if="auditData.transferType !== undefined && auditData.transferType !== null && auditData.transferType !== ''" :options="hr_transfer_type" :value="auditData.transferType" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">申请日期</span><div class="rd-value">{{ auditData.effectiveDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调出部门</span><div class="rd-value">{{ auditData.fromOrgName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调入部门</span><div class="rd-value">{{ auditData.toOrgName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调出岗位</span><div class="rd-value">{{ auditData.fromPosition || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调入岗位</span><div class="rd-value">{{ auditData.toPosition || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">调出工资</span><div class="rd-value rd-amount">{{ formatAmount(auditData.fromSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">调入工资</span><div class="rd-value rd-amount">{{ formatAmount(auditData.toSalary) }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">调动原因</span><div class="rd-value">{{ auditData.reason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ auditData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1" style="display:block">
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
                  <div class="rd-timeline-comment" v-if="log.auditRemark"><strong>审核意见：</strong>{{ log.auditRemark }}</div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>审批意见</div></div>
          <div class="rd-card-body" style="display:block">
            <el-input v-model="auditData.auditRemark" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请输入审批意见，驳回时建议填写驳回原因" />
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" :loading="auditSubmitting" @click="submitAudit('1')">通 过</el-button>
        <el-button type="danger" :loading="auditSubmitting" @click="submitAudit('2')">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 业务操作说明弹窗 ===== -->
    <el-dialog v-model="showStatusHelp" title="调动管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是调动管理？</div>
          <div class="highlight-card-body">
            <p><strong>调动管理</strong>是人力资源模块中办理员工内部调动的人事调整单据，记录员工从提交调动申请到审批通过正式生效的全过程信息。</p>
            <p>调动单涵盖员工基本信息、调动类型、调出/调入部门与岗位、薪资变化、申请日期及调动原因等内容，用于规范企业内部的部门调动、岗位调动等人事变动流程，审批通过后正式执行。</p>
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「审批」通过</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="success">已审核</el-tag>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待审批</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「审批」驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="danger">已驳回</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">修改后重新提交</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag type="warning">待审批</el-tag>
          </div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待审批">调动单新增后的初始状态。可修改、删除单据；具有审批权限的人员可执行审批操作</el-descriptions-item>
          <el-descriptions-item label="已审核">审批人审核通过，调动生效。单据不可再修改、删除，可查看详情及审核记录</el-descriptions-item>
          <el-descriptions-item label="已驳回">审批人驳回并给出审核意见。需点击「修改」查看驳回原因，修改后保存即重新提交回到待审批状态</el-descriptions-item>
        </el-descriptions>
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息（卡片区一）</div>
          <div class="highlight-card-body">
            <p><strong>调动单号</strong>：系统自动生成，无需填写</p>
            <p><strong>姓名</strong>：点击输入框从员工档案弹框选择，选择后工号自动带出 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>工号</strong>：选择人员后自动带出</p>
            <p><strong>调动类型</strong>：字典下拉选择（部门调动 / 岗位调动）</p>
            <p><strong>申请日期</strong>：日期选择器选择，默认为当天 <span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px">
          <div class="highlight-card-title">调动信息（卡片区二）</div>
          <div class="highlight-card-body">
<p><strong>原部门 / 目标部门</strong>：点击输入框从部门树弹框选择；选择人员后原部门自动带出 <span style="color: #f56c6c;">部门调动时必填</span></p>
<p><strong>原岗位 / 目标岗位</strong>：点击输入框从岗位弹框选择；选择人员后原岗位自动带出 <span style="color: #f56c6c;">岗位调动时必填</span></p>
<p><strong>原薪资 / 目标薪资</strong>：金额输入，单位为元；选择人员后原薪资自动带出（取最近入职单的转正/试用期薪资）</p>
<p><strong>调动原因</strong>：填写本次调动的事由说明 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>备注</strong>：补充说明信息，选填</p>
          </div>
        </div>
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">审批规则</div>
          <div class="highlight-card-body">
            <p>1. 调动单保存后<strong>单号自动生成</strong>，审批状态默认为<strong>待审批</strong></p>
            <p>2. 仅<strong>待审批</strong>状态可执行审批；审批时需填写审批意见（驳回时必填）</p>
            <p>3. <strong>已驳回</strong>的单据不能直接再次审批，必须修改后重新提交</p>
            <p>4. 每次审批（通过/驳回）均写入审核记录，形成完整审批轨迹</p>
<p>5. <strong>审批通过后自动同步员工档案</strong>：更新员工的部门、岗位为目标值</p>
          </div>
        </div>
        <div class="highlight-card highlight-danger" style="margin-top: 12px">
          <div class="highlight-card-title">注意事项</div>
          <div class="highlight-card-body">
            <p style="color: #f56c6c;">已审核的调动单不允许修改和删除，如信息有误请联系管理员处理。</p>
          </div>
        </div>
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建调动单：</strong>点击「新增」，选择员工档案带出基本信息，填写调出/调入部门岗位与薪资变化及调动原因，保存后单号自动生成，状态为待审批
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>调动审批：</strong>在待审批状态点击「审批」，核对调动前后信息后选择通过或驳回并填写审批意见
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>驳回整改：</strong>被驳回的单据修改后保存即重新提交，回到待审批状态重新接受审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>调动生效：</strong>审批通过后状态变为已审核，员工正式调往新部门/岗位
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- ===== 人员/部门选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
    <dept-picker ref="fromOrgPickerRef" title="选择原部门" @confirm="onFromOrgPickerConfirm" />
    <dept-picker ref="toOrgPickerRef" title="选择目标部门" @confirm="onToOrgPickerConfirm" />
  <position-picker ref="fromPositionPickerRef" title="选择原岗位" @confirm="onFromPositionPickerConfirm" />
<position-picker ref="toPositionPickerRef" title="选择目标岗位" @confirm="onToPositionPickerConfirm" />
</div>
</template>

<script setup name="HrTransfer">
import { listHrTransfer, getHrTransfer, addHrTransfer, updateHrTransfer, delHrTransfer, auditHrTransfer } from '@/api/hr/hrTransfer'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'
import DeptPicker from '@/components/DeptPicker'
import PositionPicker from '@/components/PositionPicker'
import { listHrEntry } from '@/api/hr/hrEntry'

const { proxy } = getCurrentInstance()

const { hr_audit_status, hr_transfer_type } = proxy.useDict('hr_audit_status', 'hr_transfer_type')

/** 条件必填校验助手：cond 返回 true 时 value 必填 */
function requireWhen(cond, msg) {
return (rule, value, cb) => {
if (cond() && (value === undefined || value === null || value === '')) return cb(new Error(msg))
cb()
}
}

const hrTransferList = ref([])
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusTabList = computed(() => {
  return hr_audit_status.value.map(d => ({ label: d.label, value: d.value }))
})
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listHrTransfer({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    hr_audit_status.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.auditStatus] !== undefined) counts[r.auditStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-approved', '2': 'tab-reject' }
  return map[value] || ''
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  if (tab === 'all') {
    queryParams.value.auditStatus = undefined
  } else {
    queryParams.value.auditStatus = tab
  }
  queryParams.value.pageNum = 1
  getList()
}
const open = ref(false)
const viewOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showAdvanced = ref(false)
const effectiveDateRange = ref([])
const collapsedCards = reactive({ vc0: false, vc1: false, vc2: false, e0: false, e1: false, e2: false, a1: false })
const employeePickerRef = ref(null)
const fromOrgPickerRef = ref(null)
const toOrgPickerRef = ref(null)
const fromPositionPickerRef = ref(null)
const toPositionPickerRef = ref(null)

/** 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_transfer_index')

/** 列显隐配置 */
const defaultColumns = {
  transferNo: { label: '调动单号', visible: true },
  employeeName: { label: '员工姓名', visible: true },
  employeeNo: { label: '工号', visible: true },
  transferType: { label: '调动类型', visible: true },
  fromOrgName: { label: '调出部门', visible: true },
  auditStatus: { label: '审批状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_transfer_columns')
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

/** 审批状态徽章映射（与字典 hr_audit_status 对应：0待审批 1已审核 2已驳回） */
function auditLabel(v) {
  const d = (hr_audit_status.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function auditBadgeClass(v) {
  const map = { '0': 'amber', '1': 'green', '2': 'red' }
  return map[v] || 'gray'
}

function transferTypeLabel(v) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (hr_transfer_type.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function transferTypeBadgeClass(v) {
const map = { '1': 'blue', '2': 'green' }
return map[v] || 'gray'
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.transferNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.employeeNo) count++
  if (queryParams.value.auditStatus) count++
  if (queryParams.value.transferType) count++
  if (queryParams.value.fromOrgName) count++
  if (effectiveDateRange.value && effectiveDateRange.value.length === 2) count++
  return count
})

const viewData = ref({})
const auditOpen = ref(false)
const auditSubmitting = ref(false)
const auditData = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, transferNo: undefined, employeeId: undefined, employeeName: undefined, employeeNo: undefined, transferType: undefined, fromOrgName: undefined, auditStatus: undefined },
rules: {
employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
effectiveDate: [{ required: true, message: '申请日期不能为空', trigger: 'blur' }],
reason: [{ required: true, message: '调动原因不能为空', trigger: 'blur' }],
fromOrgId: [{ validator: requireWhen(() => form.value.transferType === '1', '部门调动时原部门不能为空'), trigger: 'change' }],
toOrgId: [{ validator: requireWhen(() => form.value.transferType === '1', '部门调动时目标部门不能为空'), trigger: 'change' }],
fromPosition: [{ validator: requireWhen(() => form.value.transferType === '2', '岗位调动时原岗位不能为空'), trigger: 'change' }],
toPosition: [{ validator: requireWhen(() => form.value.transferType === '2', '岗位调动时目标岗位不能为空'), trigger: 'change' }]
}
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listHrTransfer(queryParams.value).then(response => {
    hrTransferList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
    loadStatusCounts()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  queryParams.value.params = {}
  proxy.addDateRange(queryParams.value, effectiveDateRange.value, 'EffectiveDate')
  getList()
}

function resetQuery() {
  queryParams.value.transferNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.employeeNo = undefined
  queryParams.value.auditStatus = undefined
  queryParams.value.transferType = undefined
  queryParams.value.fromOrgName = undefined
  effectiveDateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.transferId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function reset() {
  form.value = {
    transferNo: undefined,
    employeeId: undefined,
    employeeName: undefined,
    employeeNo: undefined,
    transferType: undefined,
    fromOrgId: undefined,
    fromOrgName: undefined,
    toOrgId: undefined,
    toOrgName: undefined,
    fromPosition: undefined,
    toPosition: undefined,
    fromSalary: undefined,
    toSalary: undefined,
    effectiveDate: undefined,
    reason: undefined,
    remark: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined
  }
  proxy.resetForm('hrTransferRef')
}

function handleAdd() {
reset()
form.value.effectiveDate = new Date().toISOString().slice(0, 10)
open.value = true
title.value = '新增调动'
}

function handleUpdate(row) {
  reset()
  const transferId = row.transferId || ids.value[0]
  getHrTransfer(transferId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改调动'
  })
}

function handleView(row) {
  const transferId = row.transferId
  getHrTransfer(transferId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

/** 查看弹窗审核记录：优先使用审核日志，兼容历史数据合成单节点 */
function viewAuditLogs() {
  const d = viewData.value || {}
  if (d.auditLogList && d.auditLogList.length) return d.auditLogList
  if (d.auditStatus === undefined || d.auditStatus === null || d.auditStatus === '') return []
  return [{ auditAction: d.auditStatus, auditBy: d.auditBy, auditTime: d.auditTime, auditRemark: d.auditRemark }]
}

function formatAmount(val) {
  if (val === null || val === undefined || val === '') return '-'
  return '￥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 审批 —— 打开审批弹窗 */
function handleAudit(row) {
  getHrTransfer(row.transferId).then(response => {
    const d = response.data || {}
    auditData.value = {
      transferId: d.transferId,
      transferNo: d.transferNo,
      employeeName: d.employeeName,
      employeeNo: d.employeeNo,
      transferType: d.transferType,
      fromOrgName: d.fromOrgName,
      toOrgName: d.toOrgName,
      fromPosition: d.fromPosition,
      toPosition: d.toPosition,
      fromSalary: d.fromSalary,
      toSalary: d.toSalary,
      effectiveDate: d.effectiveDate,
      reason: d.reason,
      remark: d.remark,
      auditStatus: d.auditStatus,
      auditLogList: d.auditLogList || [],
      auditRemark: ''
    }
    auditOpen.value = true
  })
}

/** 提交审批（action：1通过 2驳回） */
function submitAudit(action) {
  if (action === '2' && !auditData.value.auditRemark) {
    proxy.$modal.msgWarning('驳回时请填写审批意见')
    return
  }
  auditSubmitting.value = true
  auditHrTransfer(auditData.value.transferId, action, auditData.value.auditRemark).then(() => {
    proxy.$modal.msgSuccess(action === '1' ? '审批通过' : '已驳回')
    auditOpen.value = false
    getList()
  }).finally(() => {
    auditSubmitting.value = false
  })
}

function submitForm() {
  proxy.$refs['hrTransferRef'].validate(valid => {
    if (valid) {
      if (form.value.transferId != undefined) {
        updateHrTransfer(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrTransfer(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const transferIds = row.transferId || ids.value
  proxy.$modal.confirm('是否确认删除选中的调动？').then(function() {
    return delHrTransfer(transferIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/transfer/export', { ...queryParams.value }, `hrTransfer_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员/部门弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
const f = form.value;
f.employeeId = emp.employeeId;
f.employeeName = emp.employeeName;
// 档案信息自动带出：仅填充本页面表单中存在的字段
const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, employeeNo: emp.employeeNo };
Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
// 原部门/原岗位自动带出
if (emp.orgId) { f.fromOrgId = emp.orgId; f.fromOrgName = emp.orgName; }
if (emp.positionName) { f.fromPosition = emp.positionName; }
// 原薪资自动带出：取该员工最近入职单的转正薪资，无则取试用期薪资
listHrEntry({ employeeId: emp.employeeId, pageNum: 1, pageSize: 20 }).then(res => {
const rows = (res.rows || []).filter(r => String(r.employeeId) === String(emp.employeeId));
let sal;
for (const r of rows) { if (r.regularSalary !== null && r.regularSalary !== undefined && r.regularSalary !== '') { sal = r.regularSalary; break; } }
if (sal === undefined) { for (const r of rows) { if (r.probationSalary !== null && r.probationSalary !== undefined && r.probationSalary !== '') { sal = r.probationSalary; break; } } }
if (sal !== undefined && f.employeeId === emp.employeeId) f.fromSalary = sal;
}).catch(() => {});
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}
function openFromOrgPicker() { fromOrgPickerRef.value.open(form.value.fromOrgId); }
function onFromOrgPickerConfirm(dept) { form.value.fromOrgId = dept.deptId; form.value.fromOrgName = dept.deptName; }
function clearFromOrg() { form.value.fromOrgId = null; form.value.fromOrgName = ''; }
function onFromOrgFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.fromOrgId) { clearFromOrg(); } else { openFromOrgPicker(); }
    return;
  }
  openFromOrgPicker();
}
function openToOrgPicker() { toOrgPickerRef.value.open(form.value.toOrgId); }
function onToOrgPickerConfirm(dept) { form.value.toOrgId = dept.deptId; form.value.toOrgName = dept.deptName; }
function clearToOrg() { form.value.toOrgId = null; form.value.toOrgName = ''; }
function onToOrgFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.toOrgId) { clearToOrg(); } else { openToOrgPicker(); }
    return;
  }
  openToOrgPicker();
}
/** 原岗位/目标岗位弹框选择（与转正页一致） */
function onFromPositionPickerConfirm(p) { form.value.fromPosition = p.positionName; }
function onToPositionPickerConfirm(p) { form.value.toPosition = p.positionName; }
function onFromPositionFieldClick(e) {
if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
if (form.value.fromPosition) { form.value.fromPosition = ''; }
else { fromPositionPickerRef.value.open(); }
return;
}
fromPositionPickerRef.value.open();
}
function onToPositionFieldClick(e) {
if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
if (form.value.toPosition) { form.value.toPosition = ''; }
else { toPositionPickerRef.value.open(); }
return;
}
toPositionPickerRef.value.open();
}
/** 调动类型切换后重置条件必填字段的校验状态 */
function onTransferTypeChange() {
proxy.$refs.hrTransferRef && proxy.$refs.hrTransferRef.clearValidate(['fromOrgId', 'toOrgId', 'fromPosition', 'toPosition'])
}

getList()
</script>

<style scoped>
/* ===== 附件链接 & 驳回提示 ===== */
.reject-alert { margin-bottom: 16px; }

/* ===== Design Tokens ===== */
.hr-transfer-page {
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

/* ===== Surface Card ===== */
.hr-transfer-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-transfer-page .filter-card { padding:14px 20px 16px; }
.hr-transfer-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-transfer-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-transfer-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-transfer-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-transfer-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-transfer-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-transfer-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-transfer-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-transfer-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-transfer-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-transfer-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-transfer-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-transfer-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-transfer-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-transfer-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-transfer-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-transfer-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-transfer-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-transfer-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-transfer-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.hr-transfer-page .field .control :deep(.el-select) { width:100%; }
.hr-transfer-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-transfer-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-transfer-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-transfer-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-transfer-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-transfer-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-transfer-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-transfer-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-transfer-page .btn-soft .el-icon { font-size:14px; }
.hr-transfer-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-transfer-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-transfer-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-transfer-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-transfer-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-transfer-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-transfer-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-transfer-page .table-wrap { overflow-x:auto; }
.hr-transfer-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-transfer-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-transfer-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-transfer-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-transfer-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-transfer-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-transfer-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-transfer-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-transfer-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-transfer-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-transfer-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-transfer-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-transfer-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-transfer-page .badge.amber .dot { background:var(--amber-500); }
.hr-transfer-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-transfer-page .badge.blue .dot { background:var(--blue-500); }
.hr-transfer-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-transfer-page .badge.green .dot { background:var(--green-500); }
.hr-transfer-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-transfer-page .badge.red .dot { background:var(--red-500); }
.hr-transfer-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-transfer-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-transfer-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-transfer-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-transfer-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-transfer-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-transfer-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-transfer-page .pagination-container :deep(.el-pagination .btn-prev), .hr-transfer-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-transfer-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-transfer-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-transfer-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 状态统计页签 ===== */
.hr-transfer-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.hr-transfer-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.hr-transfer-page .tabs-track::-webkit-scrollbar { display:none; }
.hr-transfer-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.hr-transfer-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); flex-shrink:0; }
.hr-transfer-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.hr-transfer-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.hr-transfer-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.hr-transfer-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.hr-transfer-page .status-tab.is-active .dot { background:var(--brand-500); }
.hr-transfer-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.hr-transfer-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.hr-transfer-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.hr-transfer-page .status-tab.tab-approved .dot { background:var(--green-500); }
.hr-transfer-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.hr-transfer-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.hr-transfer-page .status-tab.tab-reject .dot { background:var(--red-500); }
.hr-transfer-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.hr-transfer-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.hr-transfer-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; transition:all .15s ease; }
.hr-transfer-page .tip-pill:hover { background:#fffbeb; border-color:#f59e0b; color:#78350f; }
.hr-transfer-page .tip-pill .el-icon { font-size:14px; }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; display:flex; align-items:center; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-card-body p { margin:4px 0; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
.status-help-content .highlight-success { background-color:#f0f9eb; border-color:#b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color:#67c23a; }
.status-help-content .highlight-warning { background-color:#fdf6ec; border-color:#f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color:#e6a23c; }
.status-help-content .highlight-danger { background-color:#fef0f0; border-color:#fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color:#f56c6c; }

/* ===== 详情弹窗样式（遵循 detail-page-style-guide 规范） ===== */
.hr-transfer-page .rd-page { max-height: 62vh; overflow-y: auto; }
.hr-transfer-page .rd-card { border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; overflow: hidden; }
.hr-transfer-page .rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; cursor: pointer; user-select: none; }
.hr-transfer-page .rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #303133; }
.hr-transfer-page .rd-card-icon { display: flex; align-items: center; justify-content: center; width: 28px; height: 28px; border-radius: 6px; background: #fff; border: 1px solid #e5e7eb; color: #409eff; }
.hr-transfer-page .rd-collapse-btn { border: none; background: transparent; cursor: pointer; padding: 4px; display: flex; align-items: center; color: #909399; transition: transform 0.2s; }
.hr-transfer-page .rd-collapse-btn.is-collapsed { transform: rotate(-90deg); }
.hr-transfer-page .rd-card-body { padding: 16px; }
.hr-transfer-page .rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.hr-transfer-page .rd-item { display: flex; flex-direction: column; gap: 4px; }
.hr-transfer-page .rd-item--full { grid-column: 1 / -1; }
.hr-transfer-page .rd-label { font-size: 12px; color: #909399; }
.hr-transfer-page .rd-value { font-size: 14px; color: #303133; font-weight: 500; }
.hr-transfer-page .rd-amount { font-variant-numeric:tabular-nums; font-weight:700; color:#dc2626; }
.hr-transfer-page .rd-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; color: #c0c4cc; }
.hr-transfer-page .rd-empty-icon { margin-bottom: 8px; }
.hr-transfer-page .rd-empty-text { font-size: 13px; margin: 0; }
.hr-transfer-page .rd-detail-header { display: flex; align-items: center; gap: 12px; }
.hr-transfer-page .rd-detail-header-icon { width: 36px; height: 36px; border-radius: 8px; background: #ecf5ff; display: flex; align-items: center; justify-content: center; color: #409eff; }
.hr-transfer-page .rd-detail-header-main { flex: 1; }
.hr-transfer-page .rd-detail-header-title { font-size: 16px; font-weight: 600; color: #303133; }
.hr-transfer-page .rd-detail-header-sub { display: flex; align-items: center; gap: 12px; margin-top: 4px; }
.hr-transfer-page .rd-detail-header-divider { width: 1px; height: 12px; background: #dcdfe6; }
.hr-transfer-page .rd-detail-header-no { font-size: 13px; color: #909399; }
.hr-transfer-page .rd-timeline { position:relative; padding-left:24px; }
.hr-transfer-page .rd-timeline::before { content:''; position:absolute; left:10px; top:8px; bottom:8px; width:2px; background:linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius:1px; }
.hr-transfer-page .rd-timeline-item { position:relative; padding-bottom:10px; }
.hr-transfer-page .rd-timeline-item:last-child { padding-bottom:0; }
.hr-transfer-page .rd-timeline-dot { position:absolute; left:-24px; top:4px; width:20px; height:20px; border-radius:50%; background:#fff; border:3px solid #9ca3af; display:flex; align-items:center; justify-content:center; z-index:1; }
.hr-transfer-page .rd-timeline-dot--success { border-color:#10b981; box-shadow:0 0 0 4px #ecfdf5; }
.hr-transfer-page .rd-timeline-dot--error { border-color:#ef4444; box-shadow:0 0 0 4px #fef2f2; }
.hr-transfer-page .rd-timeline-content { background:#f9fafb; border-radius:10px; padding:10px; border:1px solid #f3f4f6; }
.hr-transfer-page .rd-timeline-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; }
.hr-transfer-page .rd-timeline-title { font-weight:600; color:#111827; font-size:14px; }
.hr-transfer-page .rd-timeline-time { font-size:12px; color:#6b7280; }
.hr-transfer-page .rd-timeline-body { display:grid; grid-template-columns:repeat(2, 1fr); gap:10px 20px; }
.hr-transfer-page .rd-timeline-comment { margin-top:6px; padding:10px; background:#fff; border-radius:8px; border-left:3px solid #f59e0b; font-size:14px; color:#374151; line-height:1.6; }
@keyframes rdFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.hr-transfer-page .rd-card { animation: rdFadeIn 0.4s ease-out forwards; }
.hr-transfer-page .rd-card:nth-child(2) { animation-delay: 0.06s; }
.hr-transfer-page .rd-card:nth-child(3) { animation-delay: 0.12s; }
.hr-transfer-page .rd-card:nth-child(4) { animation-delay: 0.18s; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-transfer-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-transfer-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-transfer-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
