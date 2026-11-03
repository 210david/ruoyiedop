<template>
  <div class="app-container safety-rulecheck-page">
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
          <label>检查编号</label>
          <div class="control">
            <el-input v-model="queryParams.checkNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>守则名称</label>
          <div class="control">
            <el-input v-model="queryParams.ruleName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>检查结果</label>
          <div class="control is-select">
            <el-select v-model="queryParams.checkResult" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in resultOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.checkStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="s in checkStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
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
          <button v-for="s in checkStatusOptions" :key="s.value"
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:ruleCheck:add']">新增检查</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_ruleCheck_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="检查编号" prop="checkNo" key="checkNo" :width="colWidth('checkNo', 170)" resizable show-overflow-tooltip v-if="columns.checkNo.visible" />
          <el-table-column label="检查日期" prop="checkDate" key="checkDate" :width="colWidth('checkDate', 110)" resizable align="center" v-if="columns.checkDate.visible"><template #default="scope"><span>{{ parseTime(scope.row.checkDate, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="检查人" prop="checker" key="checker" :width="colWidth('checker', 100)" resizable v-if="columns.checker.visible" />
          <el-table-column label="被检查部门/岗位" prop="deptName" key="deptName" :min-width="colWidth('deptName', 140)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="关联守则" prop="ruleName" key="ruleName" :min-width="colWidth('ruleName', 160)" resizable show-overflow-tooltip v-if="columns.ruleName.visible" />
          <el-table-column label="检查结果" prop="checkResult" key="checkResult" :width="colWidth('checkResult', 100)" resizable align="center" v-if="columns.checkResult.visible"><template #default="scope"><span v-if="dictLabel(resultOptions, scope.row.checkResult)" class="badge" :class="resultClass(scope.row.checkResult)"><span class="dot"></span>{{ dictLabel(resultOptions, scope.row.checkResult) }}</span></template></el-table-column>
          <el-table-column label="整改责任人" prop="rectifyUser" key="rectifyUser" :width="colWidth('rectifyUser', 110)" resizable v-if="columns.rectifyUser.visible" />
          <el-table-column label="整改期限" prop="rectifyDeadline" key="rectifyDeadline" :width="colWidth('rectifyDeadline', 110)" resizable align="center" v-if="columns.rectifyDeadline.visible"><template #default="scope"><span>{{ scope.row.rectifyDeadline ? parseTime(scope.row.rectifyDeadline, '{y}-{m}-{d}') : '-' }}</span></template></el-table-column>
          <el-table-column label="状态" prop="checkStatus" key="checkStatus" :width="colWidth('checkStatus', 100)" resizable align="center" v-if="columns.checkStatus.visible"><template #default="scope"><span v-if="dictLabel(checkStatusOptions, scope.row.checkStatus)" class="badge" :class="statusClass(scope.row.checkStatus)"><span class="dot"></span>{{ dictLabel(checkStatusOptions, scope.row.checkStatus) }}</span></template></el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:ruleCheck:edit']" v-if="scope.row.checkStatus !== '3'">修改</el-button>
                <el-button link type="success" icon="CircleCheck" @click="handleReview(scope.row)" v-hasPermi="['safety:ruleCheck:review']" v-if="scope.row.checkResult === '2' && scope.row.checkStatus !== '3'">整改闭环</el-button>
                <el-button link type="info" icon="FolderChecked" @click="handleArchive(scope.row)" v-hasPermi="['safety:ruleCheck:archive']" v-if="scope.row.checkStatus === '2'">归档</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:ruleCheck:remove']" v-if="scope.row.checkStatus !== '3'">删除</el-button>
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
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">检查编号保存后自动生成；SR-04：不符合项必须整改闭环后才能归档</span></div>
          </div>
        </div>
      </template>
      <el-form ref="checkRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>检查信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="检查编号"><el-input v-model="form.checkNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检查日期" prop="checkDate"><el-date-picker v-model="form.checkDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="检查人" prop="checker">
                  <el-input v-model="form.checker" readonly placeholder="请选择检查人" @click="openCheckerPicker">
                    <template #append><el-button icon="Search" @click="openCheckerPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="被检查部门" prop="deptName">
                  <el-input v-model="form.deptName" readonly placeholder="请选择被检查部门" @click="openDeptPicker">
                    <template #append><el-button icon="Search" @click="openDeptPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="关联守则" prop="ruleId">
                  <el-select v-model="form.ruleId" filterable clearable placeholder="请选择守则" style="width: 100%" @change="onRuleChange">
                    <el-option v-for="r in ruleOptions" :key="r.ruleId" :label="r.ruleName + '（v' + r.versionNo + '）'" :value="r.ruleId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检查结果" prop="checkResult">
                  <el-select v-model="form.checkResult" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in resultOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
          <section class="rd-card" v-if="form.checkResult === '2'">
            <div class="rd-card-header" @click="toggleCard('ec1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>不符合与整改（SR-04）</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec1">
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom:12px"><template #title>SR-04：不符合项必须填写整改责任人、期限与整改结果，闭环前不允许归档</template></el-alert>
              <el-form-item label="不符合描述" prop="problemDesc"><el-input v-model="form.problemDesc" type="textarea" :rows="2" placeholder="请描述不符合项" /></el-form-item>
              <el-form-item label="整改要求" prop="rectifyRequirement"><el-input v-model="form.rectifyRequirement" type="textarea" :rows="2" placeholder="请输入整改要求" /></el-form-item>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="整改责任人" prop="rectifyUser">
                  <el-input v-model="form.rectifyUser" readonly placeholder="请选择整改责任人（SR-04 必填）" @click="openRectifyPicker">
                    <template #append><el-button icon="Search" @click="openRectifyPicker" /></template>
                  </el-input>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="整改期限" prop="rectifyDeadline"><el-date-picker v-model="form.rectifyDeadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="整改结果" prop="rectifyResult"><el-input v-model="form.rectifyResult" type="textarea" :rows="2" placeholder="整改完成后填写（闭环复查时必填）" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
    <!-- 整改闭环 Dialog -->
    <el-dialog v-model="reviewOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">整改闭环复查（SR-04）</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">不符合项必须填写整改结果后才能闭环，闭环前不允许归档</span></div>
          </div>
        </div>
      </template>
      <el-form ref="reviewRef" :model="reviewForm" :rules="reviewRules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>整改结果确认</div>
            </div>
            <div class="rd-card-body">
              <el-form-item label="整改结果" prop="rectifyResult"><el-input v-model="reviewForm.rectifyResult" type="textarea" :rows="3" placeholder="SR-04：不符合项必须填写整改结果后才能闭环" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitReview">复查闭环</el-button><el-button @click="reviewOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">检查记录详情</div>
            <div class="rd-detail-header-sub" v-if="viewData?.checkNo">
              <span class="rd-detail-header-no">编号：{{ viewData.checkNo }}</span>
              <span v-if="dictLabel(checkStatusOptions, viewData?.checkStatus)" class="badge badge-on-dark" :class="statusClass(viewData?.checkStatus)"><span class="dot"></span>{{ dictLabel(checkStatusOptions, viewData?.checkStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>检查信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc0"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">检查编号</span><div class="rd-value">{{ viewData.checkNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检查日期</span><div class="rd-value">{{ viewData.checkDate ? parseTime(viewData.checkDate, '{y}-{m}-{d}') : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检查人</span><div class="rd-value">{{ viewData.checker || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">被检查部门/岗位</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">关联守则</span><div class="rd-value">{{ viewData.ruleName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检查结果</span><div class="rd-value"><span v-if="dictLabel(resultOptions, viewData.checkResult)" class="badge" :class="resultClass(viewData.checkResult)"><span class="dot"></span>{{ dictLabel(resultOptions, viewData.checkResult) }}</span><span v-else>-</span></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>整改闭环信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc1"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">不符合描述</span><div class="rd-value">{{ viewData.problemDesc || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">整改要求</span><div class="rd-value">{{ viewData.rectifyRequirement || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">整改责任人</span><div class="rd-value">{{ viewData.rectifyUser || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">整改期限</span><div class="rd-value">{{ viewData.rectifyDeadline ? parseTime(viewData.rectifyDeadline, '{y}-{m}-{d}') : '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">整改结果</span><div class="rd-value">{{ viewData.rectifyResult || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">复查人</span><div class="rd-value">{{ viewData.reviewer || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">复查时间</span><div class="rd-value">{{ viewData.reviewTime ? parseTime(viewData.reviewTime) : '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <!-- 选择器 -->
    <dept-picker ref="deptPickerRef" title="选择被检查部门" @confirm="onDeptPickerConfirm" />
    <user-picker ref="checkerPickerRef" title="选择检查人" @confirm="onCheckerPickerConfirm" />
    <user-picker ref="rectifyPickerRef" title="选择整改责任人" @confirm="onRectifyPickerConfirm" />
    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="守则执行检查业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、执行检查释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是守则执行检查？</div>
          <div class="highlight-card-body">
            <strong>守则执行检查</strong>是安全守则发布后的执行层验证载体：按部门/岗位/守则条目开展检查，判定符合/不符合/不涉及。SR-04：不符合项必须填写整改责任人、期限与整改结果，闭环前不允许归档；检查结果自动纳入守则统计看板（符合率、不符合项分布）。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待整改</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已闭环</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已归档</el-tag>
          </div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待整改">不符合项登记后的初始状态（符合/不涉及记录直接进入已闭环）。填写整改结果并复查后闭环</el-descriptions-item>
          <el-descriptions-item label="待复查">SR-04 配套：整改期限已过仍未闭环的记录，由系统标记进入待复查（逾期提醒定时任务）</el-descriptions-item>
          <el-descriptions-item label="已闭环">不符合项已完成整改并经复查确认，可归档</el-descriptions-item>
          <el-descriptions-item label="已归档">检查记录归档，用于安监检查举证与符合率统计。SR-04：不符合项必须整改闭环后才能归档</el-descriptions-item>
        </el-descriptions>
        <h4>四、业务规则与字段指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">业务规则与字段填写</div>
          <div class="highlight-card-body">
            <p>• <strong>检查日期/检查人/检查结果：</strong>必填项<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>不符合项（SR-04）：</strong>必须填写整改责任人、整改期限，闭环复查时必须填写整改结果</p>
            <p>• <strong>整改闭环：</strong>填写整改结果后复查闭环，复查人自动记录为当前操作人</p>
            <p>• <strong>归档：</strong>SR-04 不符合项必须整改闭环后才能归档，否则后端拦截</p>
            <p>• <strong>统计：</strong>检查符合率 = 符合数/总检查数，纳入守则统计看板</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyRuleCheck">
import { listRuleCheck, getRuleCheck, addRuleCheck, updateRuleCheck, reviewRuleCheck, archiveRuleCheck, delRuleCheck } from '@/api/safety/ruleCheck'
import { listRule } from '@/api/safety/rule'
import DeptPicker from '@/components/DeptPicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_ruleCheck_index')
const { collapsedCards, toggleCard } = useDetailCard(['rc0','rc1','ec0','ec1'])
const { proxy } = getCurrentInstance()
const { safety_rule_check_result: resultOptions } = proxy.useDict('safety_rule_check_result')

// 检查状态为本地枚举（待整改/待复查/已闭环/已归档）；检查结果使用字典 safety_rule_check_result（符合/不符合/不涉及）
const checkStatusOptions = ref([
  { label: '待整改', value: '0' },
  { label: '待复查', value: '1' },
  { label: '已闭环', value: '2' },
  { label: '已归档', value: '3' }
])

const defaultColumns = {
  checkNo: { label: '检查编号', visible: true },
  checkDate: { label: '检查日期', visible: true },
  checker: { label: '检查人', visible: true },
  deptName: { label: '被检查部门', visible: true },
  ruleName: { label: '关联守则', visible: true },
  checkResult: { label: '检查结果', visible: true },
  rectifyUser: { label: '整改责任人', visible: true },
  rectifyDeadline: { label: '整改期限', visible: true },
  checkStatus: { label: '状态', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_ruleCheck_columns')
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
const reviewOpen = ref(false)
const reviewForm = ref({})
const ruleOptions = ref([])
const showStatusHelp = ref(false)
const title = ref('')
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, checkNo: undefined, ruleName: undefined, checkResult: undefined, checkStatus: undefined } })
const { queryParams, form } = toRefs(data)
const rules = {
  checkDate: [{ required: true, message: '请选择检查日期', trigger: 'change' }],
  checker: [{ required: true, message: '请输入检查人', trigger: 'blur' }],
  checkResult: [{ required: true, message: '请选择检查结果', trigger: 'change' }],
  rectifyUser: [{ required: true, message: '不符合项必须填写整改责任人（SR-04）', trigger: 'blur' }],
  rectifyDeadline: [{ required: true, message: '不符合项必须填写整改期限（SR-04）', trigger: 'change' }]
}
const reviewRules = { rectifyResult: [{ required: true, message: 'SR-04：不符合项必须填写整改结果后才能闭环', trigger: 'blur' }] }
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.checkNo) count++
  if (queryParams.value.ruleName) count++
  if (queryParams.value.checkResult) count++
  if (queryParams.value.checkStatus) count++
  return count
})
function getList() {
  loading.value = true
  listRuleCheck(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.checkNo = undefined; queryParams.value.ruleName = undefined; queryParams.value.checkResult = undefined; queryParams.value.checkStatus = undefined; activeStatusTab.value = 'all'; handleQuery() }
function loadStatusCounts() {
  listRuleCheck({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.checkStatus] !== undefined) counts[r.checkStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.checkStatus = status === 'all' ? undefined : status
  handleQuery()
}
function statusTabClass(value) {
  const map = { '0': 'tab-wait', '1': 'tab-review', '2': 'tab-done', '3': 'tab-archived' }
  return map[value] || ''
}
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function statusClass(val) {
  const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'gray' }
  return map[val] || 'gray'
}
function resultClass(val) {
  const map = { '1': 'green', '2': 'red', '3': 'gray' }
  return map[val] || 'gray'
}
function loadRuleOptions() { listRule({ pageNum: 1, pageSize: 200 }).then(res => { ruleOptions.value = res.rows || [] }) }
function onRuleChange(ruleId) { const r = ruleOptions.value.find(x => x.ruleId === ruleId); if (r) form.value.ruleName = r.ruleName }
function handleAdd() { reset(); loadRuleOptions(); open.value = true; title.value = '新增守则执行检查' }
function openDeptPicker() { proxy.$refs.deptPickerRef.open(form.value.deptId) }
function onDeptPickerConfirm(dept) { form.value.deptId = dept.deptId; form.value.deptName = dept.deptName }
function openCheckerPicker() { proxy.$refs.checkerPickerRef.open() }
function onCheckerPickerConfirm(user) { form.value.checker = user.nickName }
function openRectifyPicker() { proxy.$refs.rectifyPickerRef.open() }
function onRectifyPickerConfirm(user) { form.value.rectifyUser = user.nickName }
function handleUpdate(row) {
  reset()
  getRuleCheck(row.checkId).then(res => { data.form = res.data; loadRuleOptions(); open.value = true; title.value = '修改守则执行检查' })
}
function handleView(row) { getRuleCheck(row.checkId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleReview(row) { reviewForm.value = { checkId: row.checkId, rectifyResult: row.rectifyResult }; reviewOpen.value = true }
function submitReview() {
  proxy.$refs['reviewRef'].validate(valid => {
    if (valid) reviewRuleCheck(reviewForm.value).then(() => { proxy.$modal.msgSuccess('复查闭环成功'); reviewOpen.value = false; getList() })
  })
}
function handleArchive(row) {
  proxy.$modal.confirm('确认归档该检查记录？不符合项必须整改闭环后才能归档（SR-04）。').then(() => archiveRuleCheck(row.checkId)).then(() => { getList(); proxy.$modal.msgSuccess('归档成功') }).catch(() => {})
}
function handleDelete(row) {
  proxy.$modal.confirm('确认删除该检查记录？').then(() => delRuleCheck(row.checkId)).then(() => { getList(); loadStatusCounts(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function submitForm() {
  proxy.$refs['checkRef'].validate(valid => {
    if (valid) {
      if (form.value.checkId) { updateRuleCheck(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addRuleCheck(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function reset() { form.value = { checkId: null, checkNo: undefined, checkDate: new Date().toISOString().slice(0, 10), checker: undefined, deptId: undefined, deptName: undefined, ruleId: undefined, ruleName: undefined, checkResult: undefined, problemDesc: undefined, rectifyRequirement: undefined, rectifyUser: undefined, rectifyDeadline: undefined, rectifyResult: undefined, remark: undefined }; proxy.resetForm('checkRef') }
function cancel() { open.value = false; reset() }
getList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.safety-rulecheck-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.safety-rulecheck-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-rulecheck-page .filter-card { padding:14px 20px 16px; }
.safety-rulecheck-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-rulecheck-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-rulecheck-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-rulecheck-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-rulecheck-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-rulecheck-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-rulecheck-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-rulecheck-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-rulecheck-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-rulecheck-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-rulecheck-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-rulecheck-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-rulecheck-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-rulecheck-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.safety-rulecheck-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-rulecheck-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-rulecheck-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-rulecheck-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-rulecheck-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-rulecheck-page .field .control :deep(.el-select) { width:100%; }
.safety-rulecheck-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-rulecheck-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.safety-rulecheck-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-rulecheck-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.safety-rulecheck-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-rulecheck-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-rulecheck-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.safety-rulecheck-page .btn-soft .el-icon { font-size:14px; }
.safety-rulecheck-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.safety-rulecheck-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.safety-rulecheck-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.safety-rulecheck-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.safety-rulecheck-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.safety-rulecheck-page .table-wrap { overflow-x:auto; }
.safety-rulecheck-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-rulecheck-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-rulecheck-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-rulecheck-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-rulecheck-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.safety-rulecheck-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-rulecheck-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .safety-rulecheck-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-rulecheck-page .filter-card .filter-bar { grid-template-columns:1fr; } .safety-rulecheck-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.safety-rulecheck-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.safety-rulecheck-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.safety-rulecheck-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.safety-rulecheck-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-rulecheck-page .badge.blue .dot { background:var(--brand-500); }
.safety-rulecheck-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.safety-rulecheck-page .badge.green .dot { background:var(--green-500); }
.safety-rulecheck-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.safety-rulecheck-page .badge.gray .dot { background:var(--ink-400); }
.safety-rulecheck-page .badge.badge-on-dark { background:rgba(255,255,255,.15); border-color:rgba(255,255,255,.25); color:#fff; }
.safety-rulecheck-page .badge.badge-on-dark .dot { background:rgba(255,255,255,.7); }
/* ===== Status Tabs ===== */
.safety-rulecheck-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.safety-rulecheck-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.safety-rulecheck-page .tabs-track::-webkit-scrollbar { display:none; }
.safety-rulecheck-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.safety-rulecheck-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.safety-rulecheck-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.safety-rulecheck-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.safety-rulecheck-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.safety-rulecheck-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.safety-rulecheck-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 待整改 - amber */
.safety-rulecheck-page .status-tab.tab-wait .dot { background:#f59e0b; }
.safety-rulecheck-page .status-tab.tab-wait .count { background:#fffbeb; color:#b45309; }
.safety-rulecheck-page .status-tab.is-active.tab-wait .count { background:#f59e0b; color:#fff; }
/* 待复查 - blue */
.safety-rulecheck-page .status-tab.tab-review .dot { background:var(--brand-500); }
.safety-rulecheck-page .status-tab.tab-review .count { background:var(--brand-50); color:var(--brand-700); }
.safety-rulecheck-page .status-tab.is-active.tab-review .count { background:var(--brand-500); color:#fff; }
/* 已闭环 - green */
.safety-rulecheck-page .status-tab.tab-done .dot { background:var(--green-500); }
.safety-rulecheck-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.safety-rulecheck-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
/* 已归档 - gray */
.safety-rulecheck-page .status-tab.tab-archived .dot { background:var(--ink-400); }
.safety-rulecheck-page .status-tab.tab-archived .count { background:var(--ink-100); color:var(--ink-500); }
.safety-rulecheck-page .status-tab.is-active.tab-archived .count { background:var(--ink-500); color:#fff; }
/* ===== Tip Pill ===== */
.safety-rulecheck-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.safety-rulecheck-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
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
