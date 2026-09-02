<template>
  <div class="app-container hr-entry-page">
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
          <label>入职单号</label>
          <div class="control">
            <el-input v-model="queryParams.entryNo" placeholder="请输入入职单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>姓名</label>
          <div class="control">
            <el-input v-model="queryParams.employeeName" placeholder="请输入姓名" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>性别</label>
          <div class="control is-select">
            <el-select v-model="queryParams.gender" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_gender" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
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
          <label>身份证号</label>
          <div class="control">
            <el-input v-model="queryParams.idCard" placeholder="请输入身份证号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>联系电话</label>
          <div class="control">
            <el-input v-model="queryParams.phone" placeholder="请输入联系电话" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>组织名称</label>
          <div class="control">
            <el-input v-model="queryParams.orgName" placeholder="请输入组织名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>岗位名称</label>
          <div class="control">
            <el-input v-model="queryParams.positionName" placeholder="请输入岗位名称" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>合同类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.contractType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in hr_contract_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>入职日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['hr:entry:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['hr:entry:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['hr:entry:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['hr:entry:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="hr_entry_index"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="hrEntryList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" border class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序号" type="index" :width="colWidth('序号', 85)" align="center" />
          <el-table-column label="入职单号" prop="entryNo" key="entryNo" align="center" :width="colWidth('entryNo', 180)" resizable v-if="columns.entryNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.entryNo }}</span></template>
          </el-table-column>
          <el-table-column label="姓名" prop="employeeName" key="employeeName" align="center" :width="colWidth('employeeName', 100)" resizable v-if="columns.employeeName.visible" />
          <el-table-column label="身份证号" prop="idCard" key="idCard" align="center" :width="colWidth('idCard', 180)" resizable show-overflow-tooltip v-if="columns.idCard.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.idCard }}</span></template>
          </el-table-column>
          <el-table-column label="性别" prop="gender" key="gender" align="center" :width="colWidth('gender', 80)" resizable v-if="columns.gender.visible">
            <template #default="scope">
              <span class="badge" :class="genderBadgeClass(scope.row.gender)" v-if="scope.row.gender !== undefined && scope.row.gender !== null && scope.row.gender !== ''">
                <span class="dot"></span>{{ dictLabel(scope.row.gender, hr_gender) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="联系电话" prop="phone" key="phone" align="center" :width="colWidth('phone', 120)" resizable v-if="columns.phone.visible" />
          <el-table-column label="部门" prop="orgName" key="orgName" align="center" :width="colWidth('orgName', 160)" resizable show-overflow-tooltip v-if="columns.orgName.visible" />
          <el-table-column label="岗位名称" prop="positionName" key="positionName" align="center" :width="colWidth('positionName', 140)" resizable show-overflow-tooltip v-if="columns.positionName.visible" />
          <el-table-column label="入职日期" prop="entryDate" key="entryDate" align="center" :width="colWidth('entryDate', 120)" resizable v-if="columns.entryDate.visible" />
          <el-table-column label="合同类型" prop="contractType" key="contractType" align="center" :width="colWidth('contractType', 120)" resizable v-if="columns.contractType.visible">
            <template #default="scope">
              <span class="badge" :class="dictBadgeClass(scope.row.contractType)" v-if="scope.row.contractType !== undefined && scope.row.contractType !== null && scope.row.contractType !== ''">
                <span class="dot"></span>{{ dictLabel(scope.row.contractType, hr_contract_type) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="审批状态" prop="auditStatus" key="auditStatus" align="center" :width="colWidth('auditStatus', 110)" resizable v-if="columns.auditStatus.visible">
            <template #default="scope">
              <span class="badge" :class="auditBadgeClass(scope.row.auditStatus)" v-if="scope.row.auditStatus !== undefined && scope.row.auditStatus !== null && scope.row.auditStatus !== ''">
                <span class="dot"></span>{{ auditLabel(scope.row.auditStatus) }}
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['hr:entry:query']">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['hr:entry:edit']">修改</el-button>
                <el-button link type="warning" icon="Finished" @click="handleAudit(scope.row)" v-if="scope.row.auditStatus === '0'" v-hasPermi="['hr:entry:audit']">审批</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['hr:entry:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="hrEntryRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e0">
              <el-row :gutter="20">
                <el-col :span="12">
          <el-form-item label="入职单号" prop="entryNo">
            <el-input v-model="form.entryNo" placeholder="自动生成" disabled />
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
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in hr_gender" :key="d.value" :label="d.label" :value="d.value" /></el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入联系电话" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>工作信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="部门" prop="orgId">
          <el-input :model-value="form.orgName" readonly placeholder="请选择部门">
            <template #append>
              <el-button v-if="form.orgId" icon="CircleClose" @click="onOrgFieldClick" />
              <el-button v-else icon="Search" @click="onOrgFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12"><el-form-item label="岗位" prop="positionId">
          <el-input :model-value="form.positionName" readonly placeholder="请选择岗位">
            <template #append>
              <el-button v-if="form.positionId" icon="CircleClose" @click="onPositionFieldClick" />
              <el-button v-else icon="Search" @click="onPositionFieldClick" />
            </template>
          </el-input>
        </el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="入职日期" prop="entryDate">
            <el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="试用期月数" prop="probationMonths">
            <el-input-number v-model="form.probationMonths" :precision="0" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="试用工资" prop="probationSalary">
            <el-input-number v-model="form.probationSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="转正工资" prop="regularSalary">
            <el-input-number v-model="form.regularSalary" :precision="2" :min="0" controls-position="right" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同类型" prop="contractType">
            <el-select v-model="form.contractType" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in hr_contract_type" :key="d.value" :label="d.label" :value="d.value" /></el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同期限" prop="contractPeriod">
            <el-input v-model="form.contractPeriod" placeholder="请输入合同期限" />
          </el-form-item>
        </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e2">
              <el-row :gutter="20">
                <el-col :span="24">
          <el-form-item label="材料清单" prop="materialsStatus">
            <file-upload v-model="form.materialsStatus" :limit="5" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="岗前体检" prop="healthCheck">
            <file-upload v-model="form.healthCheck" :limit="5" :file-type="['pdf', 'doc', 'docx', 'jpg', 'png']" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="三级安全教育" prop="safetyEducation">
            <file-upload v-model="form.safetyEducation" :limit="5" :file-type="['pdf', 'doc', 'docx', 'jpg', 'png']" />
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
            <div class="rd-card-header" @click="toggleCard('e3')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e3" style="display:block">
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

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">入职详情</div>
            <div class="rd-detail-header-sub" v-if="viewData.entryNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">入职单号：{{ viewData.entryNo }}</span>
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
          <div class="rd-card-body" v-show="!collapsedCards.vc0">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">入职单号</span><div class="rd-value">{{ viewData.entryNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ viewData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">身份证号</span><div class="rd-value">{{ viewData.idCard || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value"><dict-tag v-if="viewData.gender !== undefined && viewData.gender !== null && viewData.gender !== ''" :options="hr_gender" :value="viewData.gender" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.phone || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>工作信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">组织名称</span><div class="rd-value">{{ viewData.orgName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">岗位名称</span><div class="rd-value">{{ viewData.positionName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">入职日期</span><div class="rd-value">{{ viewData.entryDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">试用期月数</span><div class="rd-value">{{ viewData.probationMonths != null ? viewData.probationMonths + ' 个月' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">试用工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.probationSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">转正工资</span><div class="rd-value rd-amount">{{ formatAmount(viewData.regularSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag v-if="viewData.contractType !== undefined && viewData.contractType !== null && viewData.contractType !== ''" :options="hr_contract_type" :value="viewData.contractType" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">合同期限</span><div class="rd-value">{{ viewData.contractPeriod || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2">
            <div class="rd-grid">
<div class="rd-item rd-item--full"><span class="rd-label">材料清单</span><div class="rd-value">
<template v-if="parseFiles(viewData.materialsStatus).length"><div v-for="(f, i) in parseFiles(viewData.materialsStatus)" :key="'m' + i"><a class="file-link" :href="baseUrl + f" target="_blank">{{ fileName(f) }}</a></div></template>
                <span v-else>-</span>
              </div></div>
<div class="rd-item rd-item--full"><span class="rd-label">岗前体检</span><div class="rd-value">
<template v-if="parseFiles(viewData.healthCheck).length"><div v-for="(f, i) in parseFiles(viewData.healthCheck)" :key="'h' + i"><a class="file-link" :href="baseUrl + f" target="_blank">{{ fileName(f) }}</a></div></template>
                <span v-else>-</span>
              </div></div>
<div class="rd-item rd-item--full"><span class="rd-label">三级安全教育</span><div class="rd-value">
<template v-if="parseFiles(viewData.safetyEducation).length"><div v-for="(f, i) in parseFiles(viewData.safetyEducation)" :key="'s' + i"><a class="file-link" :href="baseUrl + f" target="_blank">{{ fileName(f) }}</a></div></template>
                <span v-else>-</span>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.vc3" style="display:block">
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
            <div class="rd-detail-header-title">入职审批</div>
            <div class="rd-detail-header-sub" v-if="auditData.entryNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">入职单号：{{ auditData.entryNo }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">入职单号</span><div class="rd-value">{{ auditData.entryNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">姓名</span><div class="rd-value">{{ auditData.employeeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">性别</span><div class="rd-value"><dict-tag v-if="auditData.gender !== undefined && auditData.gender !== null && auditData.gender !== ''" :options="hr_gender" :value="auditData.gender" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">身份证号</span><div class="rd-value">{{ auditData.idCard || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ auditData.phone || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">当前审批状态</span><div class="rd-value">
                <span class="badge" :class="auditBadgeClass(auditData.auditStatus)" v-if="auditData.auditStatus !== undefined && auditData.auditStatus !== null && auditData.auditStatus !== ''"><span class="dot"></span>{{ auditLabel(auditData.auditStatus) }}</span>
                <span v-else>-</span>
              </div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>工作信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">部门</span><div class="rd-value">{{ auditData.orgName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">岗位</span><div class="rd-value">{{ auditData.positionName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">入职日期</span><div class="rd-value">{{ auditData.entryDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">试用期月数</span><div class="rd-value">{{ auditData.probationMonths != null ? auditData.probationMonths + ' 个月' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">试用工资</span><div class="rd-value rd-amount">{{ formatAmount(auditData.probationSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">转正工资</span><div class="rd-value rd-amount">{{ formatAmount(auditData.regularSalary) }}</div></div>
              <div class="rd-item"><span class="rd-label">合同类型</span><div class="rd-value"><dict-tag v-if="auditData.contractType !== undefined && auditData.contractType !== null && auditData.contractType !== ''" :options="hr_contract_type" :value="auditData.contractType" /><span v-else>-</span></div></div>
              <div class="rd-item"><span class="rd-label">合同期限</span><div class="rd-value">{{ auditData.contractPeriod || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">材料清单</span><div class="rd-value">
                <template v-if="parseFiles(auditData.materialsStatus).length"><div v-for="(f, i) in parseFiles(auditData.materialsStatus)" :key="'am' + i"><a class="file-link" :href="baseUrl + f" target="_blank">{{ fileName(f) }}</a></div></template>
                <span v-else>-</span>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">岗前体检</span><div class="rd-value">
                <template v-if="parseFiles(auditData.healthCheck).length"><div v-for="(f, i) in parseFiles(auditData.healthCheck)" :key="'ah' + i"><a class="file-link" :href="baseUrl + f" target="_blank">{{ fileName(f) }}</a></div></template>
                <span v-else>-</span>
              </div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">三级安全教育</span><div class="rd-value">
                <template v-if="parseFiles(auditData.safetyEducation).length"><div v-for="(f, i) in parseFiles(auditData.safetyEducation)" :key="'as' + i"><a class="file-link" :href="baseUrl + f" target="_blank">{{ fileName(f) }}</a></div></template>
                <span v-else>-</span>
              </div></div>
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
    <el-dialog v-model="showStatusHelp" title="入职管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是入职管理？</div>
          <div class="highlight-card-body">
            <p><strong>入职管理</strong>是人力资源模块中办理新员工入职手续的核心单据，记录员工从提交入职申请到审批通过正式入岗的全过程信息。</p>
            <p>入职单涵盖人员基本信息、岗位安排、试用期待遇、合同签订及入职材料（材料清单、岗前体检、三级安全教育）等内容，依据《劳动合同法》建立用工关系，入职材料齐备并通过审批后方可正式入岗。</p>
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
          <el-descriptions-item label="待审批">入职单新增后的初始状态。可修改、删除单据；具有审批权限的人员可执行审批操作</el-descriptions-item>
          <el-descriptions-item label="已审核">审批人审核通过，入职手续完成。单据不可再修改、删除，可查看详情及审核记录</el-descriptions-item>
          <el-descriptions-item label="已驳回">审批人驳回并给出审核意见。需点击「修改」查看驳回原因，修改后保存即重新提交回到待审批状态</el-descriptions-item>
        </el-descriptions>
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息（卡片区一）</div>
          <div class="highlight-card-body">
            <p><strong>姓名</strong>：点击输入框从员工档案弹框选择，选择后性别、身份证号、联系电话自动带出 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>性别 / 身份证号 / 联系电话</strong>：由员工档案自动带出，无需手工填写</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px">
          <div class="highlight-card-title">工作信息（卡片区二）</div>
          <div class="highlight-card-body">
            <p><strong>部门</strong>：点击输入框从部门树弹框选择 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>岗位</strong>：点击输入框从岗位弹框选择 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>入职日期</strong>：日期选择器选择 <span style="color: #f56c6c;">*必填</span></p>
            <p><strong>试用期月数</strong>：数字输入，单位为月</p>
            <p><strong>试用工资 / 转正工资</strong>：金额输入，单位为元</p>
            <p><strong>合同类型</strong>：字典下拉选择（固定期限、无固定期限等）</p>
            <p><strong>合同期限</strong>：文本输入，如"3年"</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px">
          <div class="highlight-card-title">其他信息（卡片区三）</div>
          <div class="highlight-card-body">
            <p><strong>材料清单</strong>：上传入职材料扫描件（支持多文件）</p>
            <p><strong>岗前体检</strong>：上传岗前体检报告（pdf/word/图片）</p>
            <p><strong>三级安全教育</strong>：上传公司/部门/班组三级安全教育记录（pdf/word/图片）</p>
            <p><strong>备注</strong>：补充说明信息，选填</p>
          </div>
        </div>
        <h4>五、核心业务规则</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">审批规则</div>
          <div class="highlight-card-body">
            <p>1. 入职单保存后<strong>单号自动生成</strong>，审批状态默认为<strong>待审批</strong></p>
            <p>2. 仅<strong>待审批</strong>状态可执行审批；审批时需填写审批意见（驳回时必填）</p>
            <p>3. <strong>已驳回</strong>的单据不能直接再次审批，必须修改后重新提交</p>
            <p>4. 每次审批（通过/驳回）均写入审核记录，形成完整审批轨迹</p>
<p>5. <strong>审批通过后自动同步员工档案</strong>：员工状态置为试用期，并更新入职日期、部门、岗位</p>
          </div>
        </div>
        <div class="highlight-card highlight-danger" style="margin-top: 12px">
          <div class="highlight-card-title">注意事项</div>
          <div class="highlight-card-body">
            <p style="color: #f56c6c;">已审核的入职单不允许修改和删除，如信息有误请联系管理员处理。</p>
          </div>
        </div>
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建入职单：</strong>点击「新增」，选择员工档案自动带出基本信息，填写岗位与合同信息，上传入职材料，保存后单号自动生成，状态为待审批
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>入职审批：</strong>在待审批状态点击「审批」，核对新人员信息与附件材料后选择通过或驳回并填写审批意见
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>驳回整改：</strong>被驳回的单据修改后保存即重新提交，回到待审批状态重新接受审批
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>入职完成：</strong>审批通过后状态变为已审核，员工正式入岗，入职手续办理完毕
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- ===== 人员/部门/岗位选择弹窗 ===== -->
    <employee-picker ref="employeePickerRef" title="选择人员" @confirm="onEmployeePickerConfirm" />
    <dept-picker ref="orgPickerRef" title="选择部门" @confirm="onOrgPickerConfirm" />
    <position-picker ref="positionPickerRef" title="选择岗位" @confirm="onPositionPickerConfirm" />
  </div>
</template>

<script setup name="HrEntry">
import { listHrEntry, getHrEntry, addHrEntry, updateHrEntry, delHrEntry, auditHrEntry } from '@/api/hr/hrEntry'
import { Filter, ArrowDown, Search, Edit, Delete, Download, QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
import { useColumnResize } from '@/composables/useColumnResize'
import EmployeePicker from '@/components/EmployeePicker'
import DeptPicker from '@/components/DeptPicker'
import PositionPicker from '@/components/PositionPicker'

const { proxy } = getCurrentInstance()

const { hr_audit_status, hr_gender, hr_contract_type } = proxy.useDict('hr_audit_status', 'hr_gender', 'hr_contract_type')

const hrEntryList = ref([])
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusTabList = computed(() => {
  return hr_audit_status.value.map(d => ({ label: d.label, value: d.value }))
})
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listHrEntry({ pageNum: 1, pageSize: 999 }).then(res => {
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
const dateRange = ref([])
const collapsedCards = reactive({ vc0: false, vc1: false, vc2: false, vc3: false, e0: false, e1: false, e2: false, e3: false, a1: false })

/** 列显隐与列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('hr_entry_index')

const defaultColumns = {
  entryNo: { label: '入职单号', visible: true },
  employeeName: { label: '姓名', visible: true },
  idCard: { label: '身份证号', visible: true },
  gender: { label: '性别', visible: true },
  phone: { label: '联系电话', visible: true },
  orgName: { label: '部门', visible: true },
  positionName: { label: '岗位名称', visible: true },
  entryDate: { label: '入职日期', visible: true },
  contractType: { label: '合同类型', visible: true },
  auditStatus: { label: '审批状态', visible: true },
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('hr_entry_index')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, entryNo: undefined, employeeName: undefined, idCard: undefined, gender: undefined, phone: undefined, orgName: undefined, positionName: undefined, contractType: undefined, auditStatus: undefined },
  rules: {
    employeeId: [{ required: true, message: '请选择人员', trigger: 'change' }],
    entryDate: [{ required: true, message: '入职日期不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.entryNo) count++
  if (queryParams.value.employeeName) count++
  if (queryParams.value.gender) count++
  if (queryParams.value.auditStatus) count++
  if (queryParams.value.idCard) count++
  if (queryParams.value.phone) count++
  if (queryParams.value.orgName) count++
  if (queryParams.value.positionName) count++
  if (queryParams.value.contractType) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

const viewData = ref({})
const auditOpen = ref(false)
const auditSubmitting = ref(false)
const auditData = ref({})
const baseUrl = import.meta.env.VITE_APP_BASE_API
const employeePickerRef = ref(null)
const orgPickerRef = ref(null)
const positionPickerRef = ref(null)

function toggleCard(key) {
  collapsedCards[key] = !collapsedCards[key]
}

function auditLabel(v) {
  const d = (hr_audit_status.value || []).find(i => i.value == v)
  return d ? d.label : String(v)
}
function auditBadgeClass(v) {
  const map = { '0': 'amber', '1': 'green', '2': 'red' }
  return map[v] || 'gray'
}

function genderBadgeClass(v) {
  const map = { '0': 'blue', '1': 'red', '2': 'gray' }
  return map[v] || 'gray'
}
function dictBadgeClass(v) {
  const map = { '0': 'blue', '1': 'green', '2': 'violet' }
  return map[v] || 'gray'
}
function dictLabel(v, options) {
  if (v === undefined || v === null || v === '') return '-'
  const d = (options || []).find(i => i.value == v)
  return d ? d.label : String(v)
}

function formatAmount(val) {
  if (val === null || val === undefined || val === '') return '-'
  return '￥' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
loading.value = true
listHrEntry(queryParams.value).then(response => {
hrEntryList.value = response.rows
total.value = response.total
loading.value = false
applySavedWidths()
loadStatusCounts()
})
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value, 'EntryDate')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.entryNo = undefined
  queryParams.value.employeeName = undefined
  queryParams.value.idCard = undefined
  queryParams.value.gender = undefined
  queryParams.value.phone = undefined
  queryParams.value.orgName = undefined
  queryParams.value.positionName = undefined
  queryParams.value.contractType = undefined
  queryParams.value.auditStatus = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.entryId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    entryNo: undefined,
    employeeName: undefined,
    idCard: undefined,
    gender: undefined,
    phone: undefined,
    orgId: undefined,
    orgName: undefined,
    positionId: undefined,
    positionName: undefined,
    entryDate: undefined,
    probationMonths: undefined,
    probationSalary: undefined,
    regularSalary: undefined,
    contractType: undefined,
    contractPeriod: undefined,
    materialsStatus: undefined,
    healthCheck: undefined,
    safetyEducation: undefined,
    remark: undefined,
    auditStatus: undefined,
    auditBy: undefined,
    auditTime: undefined,
    auditRemark: undefined,
    employeeId: undefined
  }
  proxy.resetForm('hrEntryRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增入职'
}

function handleUpdate(row) {
  reset()
  const entryId = row.entryId || ids.value[0]
  getHrEntry(entryId).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改入职'
  })
}

/** 查看 —— 使用独立查看弹窗 */
function handleView(row) {
  const entryId = row.entryId
  getHrEntry(entryId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

/** 解析逗号分隔的附件URL列表 */
function parseFiles(v) {
  if (!v) return []
  return String(v).split(',').map(s => s.trim()).filter(Boolean)
}

/** 从URL中提取文件名 */
function fileName(f) {
  try { return decodeURIComponent(f.split('/').pop() || f) } catch (e) { return f }
}

/** 查看弹窗审核记录：优先使用审核日志，兼容历史数据合成单节点 */
function viewAuditLogs() {
  const d = viewData.value || {}
  if (d.auditLogList && d.auditLogList.length) return d.auditLogList
  if (d.auditStatus === undefined || d.auditStatus === null || d.auditStatus === '') return []
  return [{ auditAction: d.auditStatus, auditBy: d.auditBy, auditTime: d.auditTime, auditRemark: d.auditRemark }]
}

/** 审批 —— 打开审批弹窗 */
function handleAudit(row) {
  getHrEntry(row.entryId).then(response => {
    const d = response.data || {}
    auditData.value = {
      entryId: d.entryId,
      entryNo: d.entryNo,
      employeeName: d.employeeName,
      gender: d.gender,
      idCard: d.idCard,
      phone: d.phone,
      orgName: d.orgName,
      positionName: d.positionName,
      entryDate: d.entryDate,
      probationMonths: d.probationMonths,
      probationSalary: d.probationSalary,
      regularSalary: d.regularSalary,
      contractType: d.contractType,
      contractPeriod: d.contractPeriod,
      materialsStatus: d.materialsStatus,
      healthCheck: d.healthCheck,
      safetyEducation: d.safetyEducation,
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
  auditHrEntry(auditData.value.entryId, action, auditData.value.auditRemark).then(() => {
    proxy.$modal.msgSuccess(action === '1' ? '审批通过' : '已驳回')
    auditOpen.value = false
    getList()
  }).finally(() => {
    auditSubmitting.value = false
  })
}

function submitForm() {
  proxy.$refs['hrEntryRef'].validate(valid => {
    if (valid) {
      if (form.value.entryId != undefined) {
        updateHrEntry(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addHrEntry(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const entryIds = row.entryId || ids.value
  proxy.$modal.confirm('是否确认删除选中的入职？').then(function() {
    return delHrEntry(entryIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('hr/entry/export', { ...queryParams.value }, `hrEntry_${new Date().getTime()}.xlsx`)
}

function cancel() {
  open.value = false
  reset()
}

/** ===== 人员/部门/岗位弹框选择 ===== */
function openEmployeePicker() { employeePickerRef.value.open(form.value.employeeId); }
function onEmployeePickerConfirm(emp) {
  const f = form.value;
  f.employeeId = emp.employeeId;
  f.employeeName = emp.employeeName;
  // 档案信息自动带出：仅填充本页面表单中存在的字段
  const auto = { idCard: emp.idCard, gender: emp.gender, phone: emp.phone, email: emp.email, orgId: emp.orgId, orgName: emp.orgName, positionId: emp.positionId, positionName: emp.positionName };
  Object.keys(auto).forEach(k => { if (k in f && auto[k] !== null && auto[k] !== undefined) f[k] = auto[k]; });
}
function onEmployeeFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.employeeId) { form.value.employeeId = null; form.value.employeeName = ''; }
    else { openEmployeePicker(); }
    return;
  }
  openEmployeePicker();
}
function openOrgPicker() { orgPickerRef.value.open(form.value.orgId); }
function onOrgPickerConfirm(dept) { form.value.orgId = dept.deptId; form.value.orgName = dept.deptName; }
function clearOrg() { form.value.orgId = null; form.value.orgName = ''; }
function onOrgFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.orgId) { clearOrg(); } else { openOrgPicker(); }
    return;
  }
  openOrgPicker();
}
function openPositionPicker() { positionPickerRef.value.open(form.value.positionId); }
function onPositionPickerConfirm(p) { form.value.positionId = p.positionId; form.value.positionName = p.positionName; }
function clearPosition() { form.value.positionId = null; form.value.positionName = ''; }
function onPositionFieldClick(e) {
  if (e && e.target && e.target.closest && e.target.closest('.el-input-group__append')) {
    if (form.value.positionId) { clearPosition(); } else { openPositionPicker(); }
    return;
  }
  openPositionPicker();
}

getList()
</script>

<style scoped>
/* ===== 附件链接 & 驳回提示 ===== */
.file-link { color: var(--brand-600); text-decoration: none; font-size: 13px; }
.file-link:hover { text-decoration: underline; }
.reject-alert { margin-bottom: 16px; }

/* ===== Design Tokens ===== */
.hr-entry-page {
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
.hr-entry-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== Filter Card ===== */
.hr-entry-page .filter-card { padding:14px 20px 16px; }
.hr-entry-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-entry-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-entry-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-entry-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-entry-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-entry-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-entry-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-entry-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-entry-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-entry-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-entry-page .filter-card .filter-buttons { display:flex; gap:8px; }

/* ===== Field / Control ===== */
.hr-entry-page .field { display:flex; flex-direction:column; gap:6px; min-width:0; }
.hr-entry-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.hr-entry-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-entry-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
/* el-input transparent inside .control */
.hr-entry-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-entry-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-entry-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-entry-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.hr-entry-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
/* el-select transparent inside .control */
.hr-entry-page .field .control :deep(.el-select) { width:100%; }
.hr-entry-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.hr-entry-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.hr-entry-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }

/* ===== Toolbar ===== */
.hr-entry-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-entry-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-entry-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-entry-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-entry-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-entry-page .btn-soft .el-icon { font-size:14px; }
.hr-entry-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-entry-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-entry-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-entry-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-entry-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-entry-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.hr-entry-page .btn-soft:focus-visible { outline:2px solid var(--brand-500); outline-offset:2px; }

/* ===== Table ===== */
.hr-entry-page .table-wrap { overflow-x:auto; }
.hr-entry-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-entry-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-entry-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.hr-entry-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.hr-entry-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-entry-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-entry-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-entry-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-entry-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.hr-entry-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }

/* ===== Badge ===== */
.hr-entry-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.hr-entry-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.hr-entry-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.hr-entry-page .badge.amber .dot { background:var(--amber-500); }
.hr-entry-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.hr-entry-page .badge.blue .dot { background:var(--blue-500); }
.hr-entry-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-entry-page .badge.green .dot { background:var(--green-500); }
.hr-entry-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-entry-page .badge.red .dot { background:var(--red-500); }
.hr-entry-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-entry-page .badge.violet .dot { background:var(--brand-500); }
.hr-entry-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-entry-page .badge.gray .dot { background:var(--ink-400); }

/* ===== Pagination ===== */
.hr-entry-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.hr-entry-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.hr-entry-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.hr-entry-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.hr-entry-page .pagination-container :deep(.el-pagination .btn-prev), .hr-entry-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.hr-entry-page .pagination-container :deep(.el-pagination .btn-prev:hover), .hr-entry-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.hr-entry-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }

/* ===== 状态统计页签 ===== */
.hr-entry-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.hr-entry-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.hr-entry-page .tabs-track::-webkit-scrollbar { display:none; }
.hr-entry-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.hr-entry-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); flex-shrink:0; }
.hr-entry-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.hr-entry-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.hr-entry-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.hr-entry-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.hr-entry-page .status-tab.is-active .dot { background:var(--brand-500); }
.hr-entry-page .status-tab.tab-draft .dot { background:var(--amber-500); }
.hr-entry-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); }
.hr-entry-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.hr-entry-page .status-tab.tab-approved .dot { background:var(--green-500); }
.hr-entry-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.hr-entry-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.hr-entry-page .status-tab.tab-reject .dot { background:var(--red-500); }
.hr-entry-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.hr-entry-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.hr-entry-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; transition:all .15s ease; }
.hr-entry-page .tip-pill:hover { background:#fffbeb; border-color:#f59e0b; color:#78350f; }
.hr-entry-page .tip-pill .el-icon { font-size:14px; }

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

/* ===== 详情弹窗（卡片式） ===== */
.hr-entry-page .rd-page { max-height:60vh; overflow-y:auto; }
.hr-entry-page .rd-card { border:1px solid #ebeef5; border-radius:8px; margin-bottom:16px; overflow:hidden; animation:rdFadeIn .4s ease-out forwards; }
.hr-entry-page .rd-card-header { display:flex; align-items:center; justify-content:space-between; padding:12px 16px; background:#f5f7fa; cursor:pointer; user-select:none; }
.hr-entry-page .rd-card-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:#303133; }
.hr-entry-page .rd-card-icon { display:flex; align-items:center; justify-content:center; width:28px; height:28px; border-radius:6px; background:#fff; border:1px solid #e5e7eb; color:#409eff; }
.hr-entry-page .rd-collapse-btn { border:none; background:transparent; cursor:pointer; padding:4px; display:flex; align-items:center; color:#909399; transition:transform .2s; }
.hr-entry-page .rd-collapse-btn.is-collapsed { transform:rotate(-90deg); }
.hr-entry-page .rd-card-body { padding:16px; }
.hr-entry-page .rd-grid { display:grid; grid-template-columns:repeat(2, 1fr); gap:12px 24px; }
.hr-entry-page .rd-item { display:flex; flex-direction:column; gap:4px; }
.hr-entry-page .rd-item--full { grid-column:1 / -1; }
.hr-entry-page .rd-label { font-size:12px; color:#909399; }
.hr-entry-page .rd-value { font-size:14px; color:#303133; font-weight:500; }
.hr-entry-page .rd-value--large { font-size:18px; font-weight:700; }
.hr-entry-page .rd-value--muted { color:#9ca3af; font-style:italic; }
.hr-entry-page .rd-amount { font-variant-numeric:tabular-nums; font-weight:700; color:#dc2626; }
.hr-entry-page .rd-empty { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:32px; color:#c0c4cc; }
.hr-entry-page .rd-empty-icon { margin-bottom:8px; }
.hr-entry-page .rd-empty-text { font-size:13px; margin:0; }
.hr-entry-page .rd-detail-header { display:flex; align-items:center; gap:12px; }
.hr-entry-page .rd-detail-header-icon { width:36px; height:36px; border-radius:8px; background:#ecf5ff; display:flex; align-items:center; justify-content:center; color:#409eff; }
.hr-entry-page .rd-detail-header-main { flex:1; }
.hr-entry-page .rd-detail-header-title { font-size:16px; font-weight:600; color:#303133; }
.hr-entry-page .rd-detail-header-sub { display:flex; align-items:center; gap:12px; margin-top:4px; }
.hr-entry-page .rd-detail-header-divider { width:1px; height:12px; background:#dcdfe6; }
.hr-entry-page .rd-detail-header-no { font-size:13px; color:#909399; }
.hr-entry-page .rd-timeline { position:relative; padding-left:24px; }
.hr-entry-page .rd-timeline::before { content:''; position:absolute; left:10px; top:8px; bottom:8px; width:2px; background:linear-gradient(to bottom, #d1d5db, #e5e7eb); border-radius:1px; }
.hr-entry-page .rd-timeline-item { position:relative; padding-bottom:10px; }
.hr-entry-page .rd-timeline-item:last-child { padding-bottom:0; }
.hr-entry-page .rd-timeline-dot { position:absolute; left:-24px; top:4px; width:20px; height:20px; border-radius:50%; background:#fff; border:3px solid #9ca3af; display:flex; align-items:center; justify-content:center; z-index:1; }
.hr-entry-page .rd-timeline-dot--success { border-color:#10b981; box-shadow:0 0 0 4px #ecfdf5; }
.hr-entry-page .rd-timeline-dot--error { border-color:#ef4444; box-shadow:0 0 0 4px #fef2f2; }
.hr-entry-page .rd-timeline-content { background:#f9fafb; border-radius:10px; padding:10px; border:1px solid #f3f4f6; }
.hr-entry-page .rd-timeline-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; }
.hr-entry-page .rd-timeline-title { font-weight:600; color:#111827; font-size:14px; }
.hr-entry-page .rd-timeline-time { font-size:12px; color:#6b7280; }
.hr-entry-page .rd-timeline-body { display:grid; grid-template-columns:repeat(2, 1fr); gap:10px 20px; }
.hr-entry-page .rd-timeline-comment { margin-top:6px; padding:10px; background:#fff; border-radius:8px; border-left:3px solid #f59e0b; font-size:14px; color:#374151; line-height:1.6; }
@keyframes rdFadeIn { from { opacity:0; transform:translateY(8px); } to { opacity:1; transform:translateY(0); } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }

/* ===== Responsive ===== */
@media (max-width:1100px) { .hr-entry-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-entry-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-entry-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
