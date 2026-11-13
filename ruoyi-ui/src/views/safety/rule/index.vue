<template>
  <div class="app-container safety-rule-page">
    <!-- ===== Filter Card ===== -->
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
          <label>守则名称</label>
          <div class="control">
            <el-input v-model="queryParams.ruleName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>适用部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>适用岗位</label>
          <div class="control">
            <el-input v-model="queryParams.postName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.ruleStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件</div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs + Tip Pill -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:rule:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['safety:rule:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['safety:rule:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_rule_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="守则编号" prop="ruleNo" key="ruleNo" :width="colWidth('ruleNo', 170)" resizable show-overflow-tooltip v-if="columns.ruleNo.visible" />
          <el-table-column label="守则名称" prop="ruleName" key="ruleName" :min-width="colWidth('ruleName', 180)" resizable show-overflow-tooltip v-if="columns.ruleName.visible" />
          <el-table-column label="适用部门" prop="deptName" key="deptName" :width="colWidth('deptName', 130)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="适用岗位" prop="postName" key="postName" :width="colWidth('postName', 120)" resizable show-overflow-tooltip v-if="columns.postName.visible" />
          <el-table-column label="作业场景" prop="workScene" key="workScene" :width="colWidth('workScene', 140)" resizable show-overflow-tooltip v-if="columns.workScene.visible" />
          <el-table-column label="版本" key="versionNo" :width="colWidth('versionNo', 70)" resizable align="center" v-if="columns.versionNo.visible"><template #default="scope"><span>V{{ scope.row.versionNo || 1 }}</span></template></el-table-column>
          <el-table-column label="状态" prop="ruleStatus" key="ruleStatus" :width="colWidth('ruleStatus', 90)" resizable align="center" v-if="columns.ruleStatus.visible"><template #default="scope"><span v-if="dictLabel(statusOptions, scope.row.ruleStatus)" class="badge" :class="statusClass(scope.row.ruleStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, scope.row.ruleStatus) }}</span></template></el-table-column>
          <el-table-column label="生效日期" prop="effectiveDate" key="effectiveDate" :width="colWidth('effectiveDate', 110)" resizable align="center" v-if="columns.effectiveDate.visible"><template #default="scope"><span>{{ parseTime(scope.row.effectiveDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="关联制度" prop="regulationName" key="regulationName" :min-width="colWidth('regulationName', 150)" resizable show-overflow-tooltip v-if="columns.regulationName.visible"><template #default="scope"><span>{{ scope.row.regulationName || '-' }}</span></template></el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 280)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:rule:edit']" v-if="scope.row.ruleStatus !== '2'">修改</el-button>
                <el-button link type="success" icon="Position" @click="handlePublish(scope.row)" v-hasPermi="['safety:rule:publish']" v-if="scope.row.ruleStatus === '0'">发布</el-button>
                <el-button link type="warning" icon="TurnOff" @click="handleAbolish(scope.row)" v-hasPermi="['safety:rule:abolish']" v-if="scope.row.ruleStatus === '1'">废止</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:rule:remove']" v-if="scope.row.ruleStatus === '0'">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">守则编号保存后自动生成；内容变化自动生成新版本留痕</span></div>
          </div>
        </div>
      </template>
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="守则编号"><el-input v-model="form.ruleNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="守则名称" prop="ruleName"><el-input v-model="form.ruleName" placeholder="请输入守则名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="适用部门" prop="deptName">
                  <el-input v-model="form.deptName" readonly placeholder="请选择适用部门" @click="openDeptPicker">
                    <template #append><el-button icon="Search" @click="openDeptPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="适用岗位" prop="postName">
                  <el-input v-model="form.postName" readonly placeholder="请选择适用岗位" @click="openPostPicker">
                    <template #append><el-button icon="Search" @click="openPostPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="适用作业场景" prop="workScene"><el-input v-model="form.workScene" placeholder="请输入适用作业场景" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>制度关联与期限</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec1">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"><template #title>SR-01：关联安全生产制度为选填弱关联（名称+附件）；SR-02：生效守则不可删除，仅可废止并留痕</template></el-alert>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="关联制度名称" prop="regulationName"><el-input v-model="form.regulationName" placeholder="选填，如《安全生产管理制度》" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="关联制度附件" prop="regulationAttachment"><file-upload v-model="form.regulationAttachment" :file-size="50" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="生效日期" prop="effectiveDate"><el-date-picker v-model="form.effectiveDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="失效日期" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="编制人" prop="author">
                  <el-input v-model="form.author" readonly placeholder="请选择编制人" @click="openAuthorPicker">
                    <template #append><el-button icon="Search" @click="openAuthorPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="审批人" prop="approver">
                  <el-input v-model="form.approver" readonly placeholder="请选择审批人" @click="openApproverPicker">
                    <template #append><el-button icon="Search" @click="openApproverPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>守则内容</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec2">
              <el-form-item label="守则内容" prop="ruleContent"><el-input v-model="form.ruleContent" type="textarea" :rows="6" placeholder="请输入守则内容" /></el-form-item>
              <el-form-item label="附件" prop="attachment"><file-upload v-model="form.attachment" :file-size="50" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">守则详情</div>
            <div class="rd-detail-header-sub" v-if="viewData?.ruleNo">
              <span class="rd-detail-header-no">编号：{{ viewData.ruleNo }}</span>
              <span v-if="dictLabel(statusOptions, viewData?.ruleStatus)" class="badge badge-on-dark" :class="statusClass(viewData?.ruleStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, viewData?.ruleStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc0"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">守则编号</span><div class="rd-value">{{ viewData.ruleNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">守则名称</span><div class="rd-value">{{ viewData.ruleName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">适用部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">适用岗位</span><div class="rd-value">{{ viewData.postName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">适用作业场景</span><div class="rd-value">{{ viewData.workScene || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">版本</span><div class="rd-value">v{{ viewData.versionNo || 1 }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="dictLabel(statusOptions, viewData.ruleStatus)" class="badge" :class="statusClass(viewData.ruleStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, viewData.ruleStatus) }}</span><span v-else>-</span></div></div>
            <div class="rd-item"><span class="rd-label">生效日期</span><div class="rd-value">{{ viewData.effectiveDate ? parseTime(viewData.effectiveDate, '{y}-{m}-{d}') : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>制度关联与期限</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc1"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">关联制度名称</span><div class="rd-value">{{ viewData.regulationName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">失效日期</span><div class="rd-value">{{ viewData.expireDate ? parseTime(viewData.expireDate, '{y}-{m}-{d}') : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">编制人</span><div class="rd-value">{{ viewData.author || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ viewData.approver || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">覆盖率（应签/已签）</span><div class="rd-value">{{ learnStat.total }} / {{ learnStat.signed }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>守则内容</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc2"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">守则内容</span><div class="rd-value" style="white-space: pre-wrap">{{ viewData.ruleContent || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">附件</span><div class="rd-value">{{ viewData.attachment || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc3')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>历史版本（变更留痕，可回溯）</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc3 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc3">
            <el-table :data="versions" size="small" border>
              <el-table-column label="版本" prop="versionNo" width="70" align="center" />
              <el-table-column label="守则名称" prop="ruleName" show-overflow-tooltip />
              <el-table-column label="当时状态" prop="ruleStatus" width="90" align="center"><template #default="scope"><span v-if="dictLabel(statusOptions, scope.row.ruleStatus)" class="badge" :class="statusClass(scope.row.ruleStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, scope.row.ruleStatus) }}</span></template></el-table-column>
              <el-table-column label="变更说明" prop="changeNote" show-overflow-tooltip />
              <el-table-column label="变更人" prop="changeBy" width="100" />
              <el-table-column label="变更时间" width="160"><template #default="scope"><span>{{ parseTime(scope.row.changeTime) }}</span></template></el-table-column>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 选择器 -->
    <dept-picker ref="deptPickerRef" title="选择适用部门" @confirm="onDeptPickerConfirm" />
    <position-picker ref="postPickerRef" @confirm="onPostPickerConfirm" />
    <user-picker ref="authorPickerRef" title="选择编制人" @confirm="onAuthorPickerConfirm" />
    <user-picker ref="approverPickerRef" title="选择审批人" @confirm="onApproverPickerConfirm" />
    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="安全守则业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、守则释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是安全守则？</div>
          <div class="highlight-card-body">
            <strong>安全守则</strong>是安全生产制度与一线作业之间的执行层记录载体，面向岗位与作业场景明确操作要求。守则支持编制、发布、废止全生命周期管理与版本留痕（内容变更/发布自动生成新版本，历史版本可查看、回溯）。SR-01：关联安全生产制度为选填弱关联（名称+附件）；SR-02：生效守则不可删除，只可废止并留痕。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">生效</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">废止</el-tag>
          </div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">守则创建后的初始状态。可修改、删除，内容完善后点击「发布」进入生效</el-descriptions-item>
          <el-descriptions-item label="生效">守则已发布生效，可发起学习任务（守则学习页）。不可修改删除，内容变更自动生成新版本留痕</el-descriptions-item>
          <el-descriptions-item label="废止">SR-02：生效守则不允许删除，只可废止并留痕。废止后守则归档，不可恢复为生效</el-descriptions-item>
        </el-descriptions>
        <h4>四、业务规则与字段指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">业务规则与字段填写</div>
          <div class="highlight-card-body">
            <p>• <strong>守则名称/守则内容：</strong>必填项<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>适用部门/岗位/作业场景：</strong>明确守则的适用范围，供学习任务圈定对象参考</p>
            <p>• <strong>关联制度名称/附件：</strong>SR-01 选填弱关联，如《安全生产管理制度》</p>
            <p>• <strong>发布：</strong>发布时自动生成新版本留痕，仅生效守则可在「守则学习」页发起学习任务</p>
            <p>• <strong>覆盖率：</strong>详情中展示应签阅/已签阅人数，来源于守则学习任务的签阅名单</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="SafetyRule">
import { listRule, getRule, addRule, updateRule, delRule, publishRule, abolishRule, listRuleVersions, ruleLearnStats } from '@/api/safety/rule'
import DeptPicker from '@/components/DeptPicker/index.vue'
import PositionPicker from '@/components/PositionPicker'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_rule_index')
const { collapsedCards, toggleCard } = useDetailCard(['rc0','rc1','rc2','rc3','ec0','ec1','ec2'])
const { proxy } = getCurrentInstance()
const { safety_rule_status: statusOptions } = proxy.useDict('safety_rule_status')

const defaultColumns = {
  ruleNo: { label: '守则编号', visible: true },
  ruleName: { label: '守则名称', visible: true },
  deptName: { label: '适用部门', visible: true },
  postName: { label: '适用岗位', visible: true },
  workScene: { label: '作业场景', visible: true },
  versionNo: { label: '版本', visible: true },
  ruleStatus: { label: '状态', visible: true },
  effectiveDate: { label: '生效日期', visible: true },
  regulationName: { label: '关联制度', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_rule_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const total = ref(0)
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref(null)
const versions = ref([])
const learnStat = ref({ total: 0, signed: 0 })
const showStatusHelp = ref(false)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0 })
const statusTabList = computed(() => statusOptions.value)
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, ruleName: undefined, deptName: undefined, postName: undefined, ruleStatus: undefined } })
const { queryParams, form } = toRefs(data)
const rules = {
  ruleName: [{ required: true, message: '请输入守则名称', trigger: 'blur' }],
  ruleContent: [{ required: true, message: '请输入守则内容', trigger: 'blur' }]
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ruleName) count++
  if (queryParams.value.deptName) count++
  if (queryParams.value.postName) count++
  if (queryParams.value.ruleStatus) count++
  return count
})

function getList() {
  loading.value = true
  listRule(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.ruleName = undefined; queryParams.value.deptName = undefined; queryParams.value.postName = undefined; queryParams.value.ruleStatus = undefined; activeStatusTab.value = 'all'; handleQuery() }
function loadStatusCounts() {
  listRule({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.ruleStatus] !== undefined) counts[r.ruleStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.ruleStatus = status === 'all' ? undefined : status
  handleQuery()
}
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-active', '2': 'tab-abolish' }
  return map[value] || ''
}
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function statusClass(val) {
  const map = { '0': 'gray', '1': 'green', '2': 'red' }
  return map[val] || 'gray'
}
function handleAdd() { reset(); open.value = true; title.value = '新增安全守则' }
function openDeptPicker() { proxy.$refs.deptPickerRef.open(form.value.deptId) }
function onDeptPickerConfirm(dept) { form.value.deptId = dept.deptId; form.value.deptName = dept.deptName }
function openPostPicker() { proxy.$refs.postPickerRef.open() }
function onPostPickerConfirm(p) { if (p) form.value.postName = p.positionName || p.postName }
function openAuthorPicker() { proxy.$refs.authorPickerRef.open() }
function onAuthorPickerConfirm(user) { form.value.author = user.nickName }
function openApproverPicker() { proxy.$refs.approverPickerRef.open() }
function onApproverPickerConfirm(user) { form.value.approver = user.nickName }
function handleUpdate(row) {
  const id = row?.ruleId || selectedId.value
  if (!id) { proxy.$modal.msgError('请先选择守则'); return }
  getRule(id).then(res => { data.form = res.data; open.value = true; title.value = '修改安全守则' })
}
function handleView(row) {
  getRule(row.ruleId).then(res => { viewData.value = res.data; viewOpen.value = true })
  listRuleVersions(row.ruleId).then(res => { versions.value = res.data || [] })
  ruleLearnStats(row.ruleId).then(res => { learnStat.value = res.data || { total: 0, signed: 0 } })
}
function handlePublish(row) {
  proxy.$modal.confirm('确认发布守则 "' + row.ruleName + '"？发布后将生成新版本留痕，且可发起学习任务。').then(() => publishRule(row.ruleId)).then(() => { getList(); proxy.$modal.msgSuccess('发布成功') }).catch(() => {})
}
function handleAbolish(row) {
  proxy.$modal.confirm('确认废止守则 "' + row.ruleName + '"？废止后守则保留留痕，不可删除。').then(() => abolishRule(row.ruleId)).then(() => { getList(); proxy.$modal.msgSuccess('已废止') }).catch(() => {})
}
function handleDelete(row) {
  const ids = row?.ruleId ? [row.ruleId] : selectedIds.value
  proxy.$modal.confirm('确认删除草稿守则？生效守则不允许删除（SR-02）。').then(() => delRule(ids.join(','))).then(() => { getList(); loadStatusCounts(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.ruleId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['ruleRef'].validate(valid => {
    if (valid) {
      if (form.value.ruleId) { updateRule(form.value).then(() => { proxy.$modal.msgSuccess('修改成功，内容变化自动生成新版本'); open.value = false; getList() }) }
      else { addRule(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function reset() { form.value = { ruleId: null, ruleNo: undefined, ruleName: undefined, deptId: undefined, deptName: undefined, postName: undefined, workScene: undefined, ruleContent: undefined, attachment: undefined, regulationName: undefined, regulationAttachment: undefined, effectiveDate: undefined, expireDate: undefined, author: undefined, approver: undefined, remark: undefined }; proxy.resetForm('ruleRef') }
function cancel() { open.value = false; reset() }
getList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.safety-rule-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.safety-rule-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-rule-page .filter-card { padding:14px 20px 16px; }
.safety-rule-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-rule-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-rule-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-rule-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-rule-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-rule-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-rule-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-rule-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-rule-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-rule-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-rule-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-rule-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-rule-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-rule-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.safety-rule-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-rule-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-rule-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-rule-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-rule-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-rule-page .field .control :deep(.el-select) { width:100%; }
.safety-rule-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-rule-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.safety-rule-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-rule-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.safety-rule-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-rule-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-rule-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.safety-rule-page .btn-soft .el-icon { font-size:14px; }
.safety-rule-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.safety-rule-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.safety-rule-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.safety-rule-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.safety-rule-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.safety-rule-page .table-wrap { overflow-x:auto; }
.safety-rule-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-rule-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-rule-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-rule-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-rule-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.safety-rule-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-rule-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .safety-rule-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-rule-page .filter-card .filter-bar { grid-template-columns:1fr; } .safety-rule-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.safety-rule-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.safety-rule-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.safety-rule-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.safety-rule-page .badge.green .dot { background:var(--green-500); }
.safety-rule-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.safety-rule-page .badge.gray .dot { background:var(--ink-400); }
.safety-rule-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.safety-rule-page .badge.red .dot { background:var(--red-500); }
/* 深色背景上的 badge 变体（标题横幅中使用） */
.safety-rule-page .badge.badge-on-dark { background:rgba(255,255,255,.15); border-color:rgba(255,255,255,.25); color:#fff; }
.safety-rule-page .badge.badge-on-dark .dot { background:rgba(255,255,255,.7); }
/* ===== Status Tabs ===== */
.safety-rule-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.safety-rule-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.safety-rule-page .tabs-track::-webkit-scrollbar { display:none; }
.safety-rule-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.safety-rule-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.safety-rule-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.safety-rule-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.safety-rule-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.safety-rule-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.safety-rule-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 草稿 - gray */
.safety-rule-page .status-tab.tab-draft .dot { background:var(--ink-400); }
.safety-rule-page .status-tab.tab-draft .count { background:var(--ink-100); color:var(--ink-500); }
.safety-rule-page .status-tab.is-active.tab-draft .count { background:var(--ink-500); color:#fff; }
/* 生效 - green */
.safety-rule-page .status-tab.tab-active .dot { background:var(--green-500); }
.safety-rule-page .status-tab.tab-active .count { background:var(--green-50); color:var(--green-700); }
.safety-rule-page .status-tab.is-active.tab-active .count { background:var(--green-500); color:#fff; }
/* 废止 - red */
.safety-rule-page .status-tab.tab-abolish .dot { background:var(--red-500); }
.safety-rule-page .status-tab.tab-abolish .count { background:var(--red-50); color:var(--red-700); }
.safety-rule-page .status-tab.is-active.tab-abolish .count { background:var(--red-500); color:#fff; }
/* ===== Tip Pill ===== */
.safety-rule-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.safety-rule-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
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
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
