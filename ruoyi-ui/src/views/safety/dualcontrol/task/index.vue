<template>
  <div class="app-container safety-task-page">
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
          <label>任务名称</label>
          <div class="control">
            <el-input v-model="queryParams.taskName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>任务编号</label>
          <div class="control">
            <el-input v-model="queryParams.taskCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>任务状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_task_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>任务类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_task_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>执行人</label>
          <div class="control">
            <el-input v-model="queryParams.executorName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>计划日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in safety_task_status" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:task:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:task:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:task:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_task_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="taskList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="任务编号" prop="taskCode" key="taskCode" :width="colWidth('taskCode', 130)" resizable v-if="columns.taskCode.visible" />
          <el-table-column label="任务名称" prop="taskName" key="taskName" :width="colWidth('taskName', 200)" resizable show-overflow-tooltip v-if="columns.taskName.visible" />
          <el-table-column label="任务类型" prop="taskType" key="taskType" :width="colWidth('taskType', 110)" resizable align="center" v-if="columns.taskType.visible">
            <template #default="scope"><span class="badge violet">{{ taskTypeLabel(scope.row.taskType) }}</span></template>
          </el-table-column>
          <el-table-column label="计划日期" prop="planDate" key="planDate" :width="colWidth('planDate', 120)" resizable align="center" sortable="custom" v-if="columns.planDate.visible" />
          <el-table-column label="执行人" prop="executorName" key="executorName" :width="colWidth('executorName', 100)" resizable v-if="columns.executorName.visible" />
          <el-table-column label="任务状态" prop="taskStatus" key="taskStatus" :width="colWidth('taskStatus', 100)" resizable align="center" sortable="custom" v-if="columns.taskStatus.visible">
            <template #default="scope">
              <span class="badge" :class="taskBadgeClass(scope.row.taskStatus)">
                <span class="dot"></span>{{ taskStatusLabel(scope.row.taskStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="检查结果" prop="inspectResult" key="inspectResult" :width="colWidth('inspectResult', 100)" resizable align="center" v-if="columns.inspectResult.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.inspectResult === '1' ? 'green' : scope.row.inspectResult === '2' ? 'red' : 'gray'">
                <span class="dot"></span>{{ scope.row.inspectResult === '1' ? '正常' : scope.row.inspectResult === '2' ? '异常' : '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="隐患数" prop="hazardCount" key="hazardCount" :width="colWidth('hazardCount', 80)" resizable align="center" v-if="columns.hazardCount.visible" />
          <el-table-column label="检查时间" prop="inspectTime" key="inspectTime" :width="colWidth('inspectTime', 160)" resizable align="center" v-if="columns.inspectTime.visible" />
          <el-table-column label="操作" width="360" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:task:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.taskStatus === '0'" v-hasPermi="['safety:task:edit']">修改</el-button>
              <el-button link type="success" icon="Check" @click="handleFeedback(scope.row)" v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'" v-hasPermi="['safety:task:edit']">执行反馈</el-button>
              <el-button link type="warning" icon="CircleClose" @click="handleCancel(scope.row)" v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'" v-hasPermi="['safety:task:edit']">作废</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:task:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>任务信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="任务编号" prop="taskCode"><el-input v-model="form.taskCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" placeholder="请输入任务名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="任务类型" prop="taskType"><el-select v-model="form.taskType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_task_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="执行人" prop="executorId">
                <el-input v-model="form.executorName" readonly placeholder="请选择执行人" style="width: 100%" @click="openUserPicker">
                  <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                  <template #suffix><el-icon v-if="form.executorName" class="clear-icon" @click.stop="clearExecutor"><CircleClose /></el-icon></template>
                </el-input>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">排查任务详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.taskCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.taskCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>任务信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ viewData.taskCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">任务名称</span><div class="rd-value">{{ viewData.taskName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">任务类型</span><div class="rd-value"><dict-tag :options="safety_task_type" :value="viewData.taskType" /></div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ viewData.planDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">执行人</span><div class="rd-value">{{ viewData.executorName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">任务状态</span><div class="rd-value"><span class="badge" :class="taskBadgeClass(viewData.taskStatus)"><span class="dot"></span>{{ taskStatusLabel(viewData.taskStatus) }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.taskStatus === '2' && (viewData.inspectResult || viewData.hazardCount != null || viewData.inspectTime)">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检查执行情况</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">检查结果</span><div class="rd-value"><span class="badge" :class="viewData.inspectResult === '1' ? 'green' : 'red'"><span class="dot"></span>{{ viewData.inspectResult === '1' ? '正常' : '有隐患' }}</span></div></div>
              <div class="rd-item"><span class="rd-label">隐患数量</span><div class="rd-value">{{ viewData.hazardCount != null ? viewData.hazardCount : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">检查时间</span><div class="rd-value">{{ viewData.inspectTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.hazardList && viewData.hazardList.length > 0">
          <div class="rd-card-header" @click="toggleCard('vc3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>关联隐患 ({{ viewData.hazardList.length }})</div></div>
          <div class="rd-card-body" style="display:block">
            <el-table :data="viewData.hazardList" border size="small" style="width: 100%">
              <el-table-column type="index" label="序号" width="85" align="center" />
              <el-table-column label="隐患编号" prop="hazardCode" width="130" />
              <el-table-column label="隐患描述" prop="hazardDesc" show-overflow-tooltip min-width="150" />
              <el-table-column label="类型" width="100" align="center">
                <template #default="scope">{{ hazardTypeLabel(scope.row.hazardType) }}</template>
              </el-table-column>
              <el-table-column label="等级" width="80" align="center">
                <template #default="scope"><span :class="scope.row.hazardLevel === '2' ? 'text-danger' : 'text-warning'">{{ hazardLevelLabel(scope.row.hazardLevel) }}</span></template>
              </el-table-column>
              <el-table-column label="责任部门" prop="deptName" width="100" align="center" />
              <el-table-column label="责任人" prop="personName" width="90" align="center" />
              <el-table-column label="整改要求" prop="rectifyReq" show-overflow-tooltip min-width="120" />
              <el-table-column label="整改期限" prop="rectifyDeadline" width="110" align="center" />
              <el-table-column label="隐患状态" width="100" align="center">
                <template #default="scope">{{ hazardStatusLabel(scope.row.hazardStatus) }}</template>
              </el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.createBy || viewData.createTime || viewData.updateBy || viewData.updateTime">
          <div class="rd-card-header" @click="toggleCard('vc4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>系统信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc4" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">更新人</span><div class="rd-value">{{ viewData.updateBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">更新时间</span><div class="rd-value">{{ viewData.updateTime || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 执行人选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择执行人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="排查任务业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、排查任务释义 -->
        <h4>一、排查任务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是排查任务？</div>
          <div class="highlight-card-body">
            <strong>排查任务（Inspection Task）</strong>是安全生产管理中"双控机制"（风险分级管控与隐患排查治理）的核心执行单据。系统根据风险点的管控措施和排查周期，自动或手动生成排查任务，由安全员或指定执行人按照计划日期对风险点进行现场排查，记录检查结果（正常/有隐患），如发现隐患可同步登记隐患明细并跟踪整改闭环。<br/><br/>
            排查任务基于<strong>风险分级管控（LEC评估法）</strong>，通过风险点（L-发生可能性 × E-暴露频率 × C-后果严重性）计算风险等级，针对不同风险等级制定差异化的排查周期和管控措施。排查任务覆盖日常排查、综合排查、专项排查、季节性排查、节假日排查等多种类型，满足《安全生产法》和《双重预防机制》关于定期隐患排查的合规要求。
          </div>
        </div>

        <!-- 二、排查任务状态流转图 -->
        <h4>二、排查任务状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待执行</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「执行反馈」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">执行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">提交反馈</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完成</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">待执行</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="primary">执行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「作废」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已作废</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待执行">排查任务创建后的初始状态。可修改任务信息、执行反馈或作废。点击「执行反馈」开始现场排查，系统自动将任务状态变为执行中</el-descriptions-item>
          <el-descriptions-item label="执行中">执行人已开始排查，正在填写检查结果和隐患明细。可继续填写或提交反馈，提交后系统自动记录检查结果和隐患数量</el-descriptions-item>
          <el-descriptions-item label="已完成">排查任务已完成执行反馈，系统自动记录检查结果（正常/有隐患）和隐患数量。可查看详情、查看关联隐患列表及整改状态</el-descriptions-item>
          <el-descriptions-item label="已作废">排查任务因异常情况被作废，不再有效。待执行和执行中状态的任务可手动作废</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">任务信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>任务编号：</strong>排查任务的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>任务名称：</strong>排查任务的名称，建议包含排查类型、排查对象和日期信息，如"2026年8月车间日常安全隐患排查"<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>任务类型：</strong>包括日常排查、综合排查、专项排查、季节性排查、节假日排查等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>计划日期：</strong>排查任务的计划执行日期<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>执行人：</strong>从组织机构中选择执行排查任务的人员，选择后自动带出执行人姓名<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">其他信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>备注：</strong>排查任务的补充说明信息，如特殊注意事项、排查重点等</p>
          </div>
        </div>

        <!-- 五、执行反馈填写指南 -->
        <h4>五、执行反馈填写指南</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">检查情况登记</div>
          <div class="highlight-card-body">
            <p>在待执行或执行中状态下，点击列表中的「执行反馈」按钮进入执行反馈界面，按以下步骤操作：</p>
            <p>1. <strong>检查结果：</strong>选择"正常"或"有隐患"。正常表示排查未发现安全隐患；有隐患表示排查过程中发现了需要整改的安全隐患<span style="color: #f56c6c;">*必填</span></p>
            <p>2. <strong>检查说明：</strong>填写排查情况说明，包括检查范围、检查内容、现场情况等信息</p>
          </div>
        </div>
        <div class="highlight-card highlight-danger" style="margin-top: 12px;">
          <div class="highlight-card-title">隐患明细登记</div>
          <div class="highlight-card-body">
            <p>当检查结果选择"有隐患"时，需添加至少一条隐患明细，点击「添加隐患」按钮填写：</p>
            <p>• <strong>隐患描述：</strong>详细描述发现的安全隐患情况<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>隐患类型：</strong>选择隐患分类，如设备设施类、作业行为类、环境条件类、管理类等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>隐患等级：</strong>选择隐患严重程度，一般隐患或重大隐患<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>发现位置：</strong>隐患发生的具体位置描述</p>
            <p>• <strong>隐患图片：</strong>可上传隐患现场照片，最多5张</p>
            <p>• <strong>责任人：</strong>从组织机构中选择负责整改的人员，选择后自动带出责任部门<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>责任部门：</strong>隐患整改的责任部门，选择责任人后自动带出，也可手动选择<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>整改期限：</strong>隐患整改完成的截止日期<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>整改要求：</strong>详细描述整改措施和要求<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>

        <!-- 六、隐患分级管控说明 -->
        <h4>六、隐患分级管控说明</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是隐患分级管控？</div>
          <div class="highlight-card-body">
            <strong>隐患分级管控</strong>是安全生产"双重预防机制"的核心环节，根据隐患的严重程度和可能造成的后果，将隐患分为<strong>一般隐患</strong>和<strong>重大隐患</strong>两个等级。一般隐患由班组或车间内部整改，重大隐患需上报安全主管审批后整改，并纳入隐患台账跟踪闭环。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">隐患整改闭环流程</div>
          <div class="highlight-card-body">
            <p>排查任务中发现隐患后，系统自动进入隐患整改闭环流程：</p>
            <p>1. <strong>隐患登记：</strong>在执行反馈中填写隐患明细，提交后系统自动生成隐患记录</p>
            <p>2. <strong>隐患审批：</strong>重大隐患需安全主管审批，审批通过后进入待整改状态</p>
            <p>3. <strong>隐患整改：</strong>责任人按照整改要求和期限执行整改</p>
            <p>4. <strong>整改验收：</strong>整改完成后由安全员验收，验收通过后隐患闭环</p>
            <p>5. <strong>超期提醒：</strong>整改期限到期未闭环的隐患，系统自动标记为"超期未整改"</p>
          </div>
        </div>

        <!-- 七、业务操作流程 -->
        <h4>七、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建排查任务：</strong>点击「新增」创建排查任务，填写任务名称、选择任务类型、计划日期和执行人，保存后任务编号自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>执行反馈：</strong>在待执行状态下点击「执行反馈」开始排查，任务自动切换为执行中状态。填写检查结果（正常/有隐患），如有隐患则添加隐患明细
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>提交反馈：</strong>填写完检查情况和隐患明细后点击「确定」，系统自动记录检查结果、隐患数量和检查时间，任务变为已完成状态
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>已完成的排查任务可点击「查看」查看详情，包括任务信息、检查执行情况、关联隐患列表及整改状态
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>任务作废：</strong>待执行或执行中状态的任务如因特殊情况无法继续执行，可点击「作废」终止任务，作废后不可恢复
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- 执行反馈弹窗 -->
    <el-dialog v-model="feedbackOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">任务执行反馈</span>
        </div>
      </template>
      <el-form ref="feedbackRef" :model="feedbackForm" :rules="feedbackRules" label-width="100px">
        <div class="rd-page">
          <!-- 任务基本信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fb0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>任务基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fb0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fb0">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="任务编号"><span>{{ feedbackForm.taskCode }}</span></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="任务名称"><span>{{ feedbackForm.taskName }}</span></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="任务类型"><span class="badge violet">{{ taskTypeLabel(feedbackForm.taskType) }}</span></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="计划日期"><span>{{ feedbackForm.planDate }}</span></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="执行人"><span>{{ feedbackForm.executorName }}</span></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="任务状态"><span class="badge" :class="taskBadgeClass(feedbackForm.taskStatus)"><span class="dot"></span>{{ taskStatusLabel(feedbackForm.taskStatus) }}</span></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 检查情况登记 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fb1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>检查情况登记</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fb1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fb1">
              <el-form-item label="检查结果" prop="inspectResult">
                <el-radio-group v-model="feedbackForm.inspectResult">
                  <el-radio value="1">正常</el-radio>
                  <el-radio value="2">有隐患</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="检查说明" prop="inspectDesc">
                <el-input v-model="feedbackForm.inspectDesc" type="textarea" :rows="2" placeholder="请输入检查情况说明，如检查范围、检查内容、现场情况等" />
              </el-form-item>
            </div>
          </section>
          <!-- 隐患明细 -->
          <section class="rd-card" v-if="feedbackForm.inspectResult === '2'">
            <div class="rd-card-header" @click="toggleCard('fb2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>隐患明细 ({{ feedbackForm.hazardList.length }})</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fb2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fb2">
              <div class="hazard-section-title">
                <span>隐患列表</span>
                <el-button type="primary" plain size="small" icon="Plus" @click="openHazardDialog">添加隐患</el-button>
              </div>
              <el-table :data="feedbackForm.hazardList" border size="small" style="margin-bottom: 12px" empty-text="暂无隐患记录，请点击右上角“添加隐患”">
                <el-table-column type="index" label="序号" width="85" align="center" />
                <el-table-column label="隐患描述" prop="hazardDesc" show-overflow-tooltip min-width="150" />
                <el-table-column label="类型" width="100" align="center">
                  <template #default="scope">{{ hazardTypeLabel(scope.row.hazardType) }}</template>
                </el-table-column>
                <el-table-column label="等级" width="80" align="center">
                  <template #default="scope"><span :class="scope.row.hazardLevel === '2' ? 'text-danger' : 'text-warning'">{{ hazardLevelLabel(scope.row.hazardLevel) }}</span></template>
                </el-table-column>
                <el-table-column label="责任部门" prop="deptName" width="100" align="center" />
                <el-table-column label="责任人" prop="personName" width="90" align="center" />
                <el-table-column label="整改期限" prop="rectifyDeadline" width="110" align="center" />
                <el-table-column label="操作" width="60" align="center">
                  <template #default="scope">
                    <el-button link type="danger" icon="Delete" @click="removeHazard(scope.$index)" />
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>
          <!-- 其他信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('fb3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.fb3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.fb3">
              <el-form-item label="备注" prop="remark">
                <el-input v-model="feedbackForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFeedbackForm">确 定</el-button>
          <el-button @click="feedbackOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 隐患责任人选择弹窗 -->
    <user-picker ref="hazardUserPickerRef" title="选择责任人" @confirm="onHazardUserPickerConfirm" />

    <!-- 隐患责任部门选择弹窗 -->
    <dept-picker ref="hazardDeptPickerRef" title="选择责任部门" @confirm="onHazardDeptPickerConfirm" />

    <!-- 添加隐患弹窗 -->
    <el-dialog v-model="hazardDialogOpen" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">添加隐患明细</span>
        </div>
      </template>
      <el-form ref="hazardFormRef" :model="hazardForm" :rules="hazardRules" label-width="90px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('hd0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>隐患信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.hd0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.hd0">
              <el-form-item label="隐患描述" prop="hazardDesc">
                <el-input v-model="hazardForm.hazardDesc" type="textarea" :rows="2" placeholder="请输入隐患描述" />
              </el-form-item>
              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="隐患类型" prop="hazardType">
                    <el-select v-model="hazardForm.hazardType" placeholder="请选择" style="width:100%">
                      <el-option v-for="dict in safety_hazard_type" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="隐患等级" prop="hazardLevel">
                    <el-select v-model="hazardForm.hazardLevel" placeholder="请选择" style="width:100%">
                      <el-option v-for="dict in safety_hazard_level" :key="dict.value" :label="dict.label" :value="dict.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="发现位置" prop="hazardLocation">
                    <el-input v-model="hazardForm.hazardLocation" placeholder="请输入发现位置" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="隐患图片" prop="photos">
                <image-upload v-model="hazardForm.photos" :limit="5" :file-size="50" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('hd1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>整改要求</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.hd1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.hd1">
              <el-row :gutter="16">
                <el-col :span="8">
                  <el-form-item label="责任人" prop="personId">
                    <el-input v-model="hazardForm.personName" readonly placeholder="请选择责任人" @click="openHazardUserPicker">
                      <template #append><el-button icon="Search" @click="openHazardUserPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="责任部门" prop="deptId">
                    <el-input v-model="hazardForm.deptName" readonly placeholder="选择责任人后自动带出">
                      <template #append><el-button icon="Search" @click="openHazardDeptPicker" /></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="整改期限" prop="rectifyDeadline">
                    <el-date-picker v-model="hazardForm.rectifyDeadline" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="整改要求" prop="rectifyReq">
                <el-input v-model="hazardForm.rectifyReq" type="textarea" :rows="2" placeholder="请输入整改要求" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="addHazard">确认添加</el-button>
          <el-button @click="hazardDialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SafetyInspectionTask">
import { listTask, getTask, addTask, updateTask, delTask, submitFeedback, cancelTask } from '@/api/safety/task'
import UserPicker from '@/components/UserPicker/index.vue'
import DeptPicker from '@/components/DeptPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, WarningFilled, ArrowRight, ArrowDown, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { safety_task_type, safety_task_status, safety_hazard_type, safety_hazard_level } = proxy.useDict('safety_task_type', 'safety_task_status', 'safety_hazard_type', 'safety_hazard_level')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_task_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","c3","c4","fb0","fb1","fb2","fb3","hd0","hd1","vc0","vc1","vc2","vc3","vc4"])

const taskList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const defaultColumns = {
  taskCode: { label: '任务编号', visible: true },
  taskName: { label: '任务名称', visible: true },
  taskType: { label: '任务类型', visible: true },
  planDate: { label: '计划日期', visible: true },
  executorName: { label: '执行人', visible: true },
  taskStatus: { label: '任务状态', visible: true },
  inspectResult: { label: '检查结果', visible: true },
  hazardCount: { label: '隐患数', visible: true },
  inspectTime: { label: '检查时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_task_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, taskName: undefined, taskCode: undefined, taskStatus: undefined, taskType: undefined, executorName: undefined, params: {} },
  rules: {
    taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
    taskType: [{ required: true, message: '任务类型不能为空', trigger: 'change' }],
    planDate: [{ required: true, message: '计划日期不能为空', trigger: 'change' }],
    executorId: [{ required: true, message: '执行人不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.taskName) count++
  if (queryParams.value.taskCode) count++
  if (queryParams.value.taskStatus) count++
  if (queryParams.value.taskType) count++
  if (queryParams.value.executorName) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.taskStatus = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() {
  listTask({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    ;(res.rows || []).forEach(r => { counts[r.taskStatus] = (counts[r.taskStatus] || 0) + 1 })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-audit', '2': 'tab-done', '3': 'tab-void' }; return map[value] || '' }

function getList() { loading.value = true; listTask(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { taskList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts() }) }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.taskName = undefined; queryParams.value.taskCode = undefined; queryParams.value.taskStatus = undefined; queryParams.value.taskType = undefined; queryParams.value.executorName = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.taskId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; open.value = true; title.value = '添加排查任务' }
function handleView(row) {
  const taskId = row.taskId || ids.value[0];
  getTask(taskId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}
function handleUpdate(row) { reset(); getTask(row.taskId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.remark; open.value = true; title.value = '修改排查任务' }) }
function submitForm() {
  proxy.$refs['taskRef'].validate(valid => {
    if (valid) {
      if (form.value.taskId != undefined) { updateTask(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addTask(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const taskIds = row.taskId || ids.value; proxy.$modal.confirm('是否确认删除排查任务？').then(function() { return delTask(taskIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/task/export', { ...queryParams.value }, `task_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { taskId: undefined, taskCode: undefined, taskName: undefined, taskType: undefined, planDate: undefined, executorId: undefined, executorName: undefined, remark: undefined }
  proxy.resetForm('taskRef')
}
/** 打开执行人选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.executorId) }
/** 执行人选择确认回调 */
function onUserPickerConfirm(user) { form.value.executorId = user.userId; form.value.executorName = user.nickName }
/** 清除执行人 */
function clearExecutor() { form.value.executorId = undefined; form.value.executorName = undefined }

/** ===== 执行反馈 ===== */
const feedbackOpen = ref(false)
const feedbackForm = ref({})
const feedbackRules = {
  inspectResult: [{ required: true, message: '请选择检查结果', trigger: 'change' }]
}
const hazardDialogOpen = ref(false)
const hazardForm = ref({})
const hazardRules = {
  hazardDesc: [{ required: true, message: '隐患描述不能为空', trigger: 'blur' }],
  hazardType: [{ required: true, message: '请选择隐患类型', trigger: 'change' }],
  hazardLevel: [{ required: true, message: '请选择隐患等级', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择责任部门', trigger: 'change' }],
  personId: [{ required: true, message: '请选择责任人', trigger: 'change' }],
  rectifyReq: [{ required: true, message: '整改要求不能为空', trigger: 'blur' }],
  rectifyDeadline: [{ required: true, message: '请选择整改期限', trigger: 'change' }]
}

function handleFeedback(row) {
  // 从后端获取完整任务数据，确保 executorId 等字段有值
  getTask(row.taskId || ids.value[0]).then(response => {
    const task = response.data
    feedbackForm.value = {
      taskId: task.taskId,
      taskCode: task.taskCode,
      taskName: task.taskName,
      taskType: task.taskType,
      planDate: task.planDate,
      executorId: task.executorId,
      executorName: task.executorName,
      taskStatus: task.taskStatus,
      inspectResult: '1',
      inspectDesc: undefined,
      hazardList: [],
      remark: undefined
    }
    hazardDialogOpen.value = false
    collapsedCards.fb0 = false
    collapsedCards.fb1 = false
    collapsedCards.fb2 = false
    collapsedCards.fb3 = false
    feedbackOpen.value = true
  })
}

function openHazardDialog() {
  hazardForm.value = {}
  hazardDialogOpen.value = true
}

function addHazard() {
  proxy.$refs['hazardFormRef'].validate(valid => {
    if (valid) {
      feedbackForm.value.hazardList.push({
        hazardDesc: hazardForm.value.hazardDesc,
        hazardType: hazardForm.value.hazardType,
        hazardLevel: hazardForm.value.hazardLevel,
        remark: hazardForm.value.hazardLocation,
        deptId: hazardForm.value.deptId,
        deptName: hazardForm.value.deptName,
        personId: hazardForm.value.personId,
        personName: hazardForm.value.personName,
        rectifyReq: hazardForm.value.rectifyReq,
        rectifyDeadline: hazardForm.value.rectifyDeadline,
        photos: hazardForm.value.photos
      })
      hazardForm.value = {}
      hazardDialogOpen.value = false
      proxy.$modal.msgSuccess('已添加到隐患列表')
    }
  })
}

function removeHazard(index) {
  feedbackForm.value.hazardList.splice(index, 1)
}

function openHazardUserPicker() { proxy.$refs.hazardUserPickerRef.open(hazardForm.value.personId) }
function onHazardUserPickerConfirm(user) {
  hazardForm.value.personId = user.userId
  hazardForm.value.personName = user.nickName
  // 选中人后自动带出责任部门
  if (user.deptId) {
    hazardForm.value.deptId = user.deptId
    hazardForm.value.deptName = user.deptName
  }
}
function openHazardDeptPicker() { proxy.$refs.hazardDeptPickerRef.open(hazardForm.value.deptId) }
function onHazardDeptPickerConfirm(dept) { hazardForm.value.deptId = dept.deptId; hazardForm.value.deptName = dept.deptName }

function submitFeedbackForm() {
  proxy.$refs['feedbackRef'].validate(valid => {
    if (valid) {
      if (feedbackForm.value.inspectResult === '2' && (!feedbackForm.value.hazardList || feedbackForm.value.hazardList.length === 0)) {
        proxy.$modal.msgWarning('检查结果为有隐患时，请至少添加一条隐患记录')
        return
      }
      // 合并检查说明和备注到remark字段
      let remarkParts = []
      if (feedbackForm.value.inspectDesc) remarkParts.push('【检查说明】' + feedbackForm.value.inspectDesc)
      if (feedbackForm.value.remark) remarkParts.push('【备注】' + feedbackForm.value.remark)
      // 整体提交，携带任务原始字段（taskType/planDate/executorId等），避免后端校验报错
      const data = {
        ...feedbackForm.value,
        inspectDesc: undefined,
        remark: remarkParts.length > 0 ? remarkParts.join('\n') : undefined
      }
      submitFeedback(data).then(() => {
        proxy.$modal.msgSuccess('执行反馈提交成功')
        feedbackOpen.value = false
        getList()
      })
    }
  })
}

/** ===== 作废任务 ===== */
function handleCancel(row) {
  proxy.$modal.confirm('是否确认作废该排查任务？').then(function() {
    return cancelTask(row.taskId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('作废成功')
  }).catch(() => {})
}

function taskTypeLabel(type) { const item = safety_task_type.value.find(d => d.value == type); return item ? item.label : '-' }
function taskStatusLabel(status) { const item = safety_task_status.value.find(d => d.value == status); return item ? item.label : '-' }
function taskBadgeClass(status) { const map = { '0': 'amber', '1': 'blue', '2': 'green', '3': 'gray' }; return map[status] || 'gray' }
function hazardTypeLabel(type) { const item = safety_hazard_type.value.find(d => d.value == type); return item ? item.label : '-' }
function hazardLevelLabel(level) { const item = safety_hazard_level.value.find(d => d.value == level); return item ? item.label : '-' }
function hazardStatusLabel(status) { const map = { '0': '草稿', '1': '待审批', '2': '审批驳回', '3': '待整改', '4': '整改中', '5': '待验收', '6': '已闭环', '7': '超期未整改' }; return map[status] || '-' }

getList()
</script>

<style scoped>
.safety-task-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-task-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-task-page .filter-card { padding:14px 20px 16px; }
.safety-task-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-task-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-task-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-task-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-task-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-task-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-task-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-task-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-task-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-task-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-task-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-task-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-task-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-task-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-task-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-task-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-task-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-task-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-task-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-task-page .field .control :deep(.el-select) { width:100%; }
.safety-task-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-task-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-task-page .toolbar .left, .safety-task-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-task-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-task-page .table-wrap { overflow-x:auto; }
.safety-task-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-task-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-task-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-task-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-task-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-task-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-task-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-task-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-task-page .badge.amber .dot { background:var(--amber-500); }
.safety-task-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-task-page .badge.blue .dot { background:var(--blue-500); }
.safety-task-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-task-page .badge.green .dot { background:var(--green-500); }
.safety-task-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-task-page .badge.red .dot { background:var(--red-500); }
.safety-task-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-task-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-task-page .badge.gray .dot { background:var(--ink-400); }
.safety-task-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-task-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-task-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
.hazard-section { margin: 0 0 12px; border: 1px solid var(--ink-200); border-radius: var(--r-md); overflow: hidden; }
.hazard-section-title { display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: var(--ink-50); border-bottom: 1px solid var(--ink-200); font-size: 14px; font-weight: 600; color: var(--ink-700); }
.hazard-add-form { padding: 12px; background: var(--ink-50); border-top: 1px solid var(--ink-200); }
.text-danger { color: var(--red-700); font-weight: 600; }
.text-warning { color: var(--amber-700); font-weight: 600; }
.safety-task-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.safety-task-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.safety-task-page .tabs-track::-webkit-scrollbar { display:none; }
.safety-task-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.safety-task-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.safety-task-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.safety-task-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.safety-task-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.safety-task-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.safety-task-page .status-tab.is-active .dot { background:var(--brand-500); }
.safety-task-page .status-tab.tab-draft .dot { background:var(--amber-500); } .safety-task-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .safety-task-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.safety-task-page .status-tab.tab-audit .dot { background:var(--blue-500); } .safety-task-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .safety-task-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.safety-task-page .status-tab.tab-done .dot { background:var(--green-500); } .safety-task-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .safety-task-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.safety-task-page .status-tab.tab-void .dot { background:var(--ink-400); } .safety-task-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .safety-task-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.safety-task-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.safety-task-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.safety-task-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .status-flow { display:flex; align-items:center; flex-wrap:wrap; gap:8px; padding:16px; background-color:#f5f7fa; border-radius:8px; margin-bottom:8px; }
.status-help-content .flow-item { display:flex; align-items:center; gap:8px; }
.status-help-content .flow-arrow { color:#909399; font-size:16px; }
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
</style>
