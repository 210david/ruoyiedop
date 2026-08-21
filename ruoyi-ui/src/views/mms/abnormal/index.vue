<template>
  <div class="app-container mms-abnormal-page">
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
          <label>异常单号</label>
          <div class="control">
            <el-input v-model="queryParams.abnormalNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control"><el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>异常类型</label>
          <div class="control is-select"><el-select v-model="queryParams.abnormalType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_abnormal_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field">
          <label>严重等级</label>
          <div class="control is-select"><el-select v-model="queryParams.severity" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_abnormal_severity" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_abnormal_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>产能单元</label>
          <div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>上报人</label>
          <div class="control"><el-input v-model="queryParams.reportBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>上报日期</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button>
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:abnormal:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:abnormal:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:abnormal:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:abnormal:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_abnormal_columns" /></div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="异常单号" prop="abnormalNo" key="abnormalNo" :width="colWidth('abnormalNo', 140)" resizable v-if="columns.abnormalNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 120)" resizable v-if="columns.resourceName.visible" />
          <el-table-column label="异常类型" prop="abnormalType" key="abnormalType" :width="colWidth('abnormalType', 100)" resizable align="center" v-if="columns.abnormalType.visible">
            <template #default="scope"><span v-if="scope.row.abnormalType" class="badge" :class="abnormalTypeBadgeClass(scope.row.abnormalType)"><span class="dot"></span>{{ dictLabel(mms_abnormal_type, scope.row.abnormalType) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="严重等级" prop="severity" key="severity" :width="colWidth('severity', 90)" resizable align="center" v-if="columns.severity.visible">
            <template #default="scope"><span v-if="scope.row.severity" class="badge" :class="severityBadgeClass(scope.row.severity)"><span class="dot"></span>{{ dictLabel(mms_abnormal_severity, scope.row.severity) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="描述" prop="description" key="description" :width="colWidth('description', 200)" resizable show-overflow-tooltip v-if="columns.description.visible" />
          <el-table-column label="上报人" prop="reportBy" key="reportBy" :width="colWidth('reportBy', 90)" resizable align="center" v-if="columns.reportBy.visible" />
          <el-table-column label="上报时间" prop="reportTime" key="reportTime" :width="colWidth('reportTime', 160)" resizable align="center" v-if="columns.reportTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.reportTime) }}</span></template>
          </el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:abnormal:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="warning" icon="Bell" @click="handleRespond(scope.row)" v-hasPermi="['mms:abnormal:respond']">响应</el-button>
              <el-button v-if="scope.row.status === '1'" link type="success" icon="CircleCheck" @click="handleResolve(scope.row)" v-hasPermi="['mms:abnormal:resolve']">处理关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="780px" append-to-body draggable class="rd-dialog">
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
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="异常单号" prop="abnormalNo"><el-input v-model="form.abnormalNo" placeholder="自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="工单号" prop="workOrderNo"><el-input v-model="form.workOrderNo" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产能单元" prop="resourceName"><el-input v-model="form.resourceName" placeholder="请输入" /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>异常详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="异常类型" prop="abnormalType"><el-select v-model="form.abnormalType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_abnormal_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="严重等级" prop="severity"><el-select v-model="form.severity" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_abnormal_severity" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="异常描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入异常描述" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="780px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">异常记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.abnormalNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.abnormalNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">异常单号</span><div class="rd-value">{{ viewData.abnormalNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div><div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">{{ viewData.status === '0' ? '待响应' : viewData.status === '1' ? '已响应' : viewData.status === '2' ? '已关闭' : '-' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>异常详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">异常类型</span><div class="rd-value"><dict-tag :options="mms_abnormal_type" :value="viewData.abnormalType" /></div></div><div class="rd-item"><span class="rd-label">严重等级</span><div class="rd-value"><dict-tag :options="mms_abnormal_severity" :value="viewData.severity" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">异常描述</span><div class="rd-value">{{ viewData.description || '-' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 响应Dialog ===== -->
    <el-dialog title="异常响应" v-model="respondOpen" width="500px" append-to-body>
      <el-form ref="respondFormRef" :model="respondForm" label-width="100px">
        <el-form-item label="异常单号"><span>{{ respondForm.abnormalNo }}</span></el-form-item>
        <el-form-item label="响应人" prop="responseBy"><el-input v-model="respondForm.responseBy" placeholder="默认当前用户" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitRespond">确 定</el-button><el-button @click="respondOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 处理关闭Dialog ===== -->
    <el-dialog title="异常处理关闭" v-model="resolveOpen" width="500px" append-to-body>
      <el-form ref="resolveFormRef" :model="resolveForm" label-width="100px">
        <el-form-item label="异常单号"><span>{{ resolveForm.abnormalNo }}</span></el-form-item>
        <el-form-item label="处理结果" prop="handleResult"><el-input v-model="resolveForm.handleResult" type="textarea" placeholder="请输入处理结果" /></el-form-item>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitResolve">确 定</el-button><el-button @click="resolveOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="生产异常管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、生产异常管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是生产异常管理？</div>
          <div class="highlight-card-body">
            <strong>生产异常管理（Abnormal Management）</strong>是生产管控中记录和处理生产过程中设备故障、物料短缺、质量异常等异常情况的单据。异常管理通过响应→处理→关闭的流程，确保异常情况得到及时处理和闭环跟踪，减少生产中断时间。<br/><br/>
            生产异常管理遵循 <strong>MES 异常管控规范</strong>，通过严重等级分类（紧急/重要/一般）确保关键异常优先处理，异常类型分类（设备/物料/质量/人员）支持根因分析和预防措施制定。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="danger">待响应</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">点击「响应」</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">处理中</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon><el-tag size="small" type="primary">点击「关闭」</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已关闭</el-tag></div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待响应">异常新建后的初始状态，等待响应处理。需及时响应以减少生产影响</el-descriptions-item>
          <el-descriptions-item label="处理中">异常已响应，正在处理中。处理完成后可执行关闭操作</el-descriptions-item>
          <el-descriptions-item label="已关闭">异常已处理关闭，不可再操作。关闭时需记录处理结果</el-descriptions-item>
        </el-descriptions>
        <h4>四、重点业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">核心规则</div>
          <div class="highlight-card-body">
            <p>• <strong>异常响应：</strong>待响应状态的异常需及时响应，进入处理中状态</p>
            <p>• <strong>处理关闭：</strong>处理中的异常在处理完成后可执行关闭操作</p>
            <p>• <strong>严重等级：</strong>紧急、重要、一般三个等级，影响处理优先级</p>
            <p>• <strong>异常类型：</strong>设备异常、物料异常、质量异常、人员异常等</p>
          </div>
        </div>
        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="danger" :hollow="true"><strong>上报异常：</strong>点击「新增」创建异常记录，填写异常类型、严重等级和描述</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>响应处理：</strong>点击「响应」记录响应措施，异常进入处理中状态</el-timeline-item>
          <el-timeline-item type="success" :hollow="true"><strong>关闭异常：</strong>处理完成后点击「关闭」关闭异常，记录处理结果</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Abnormal">
import { listAbnormal, getAbnormal, addAbnormal, updateAbnormal, delAbnormal, respondAbnormal, resolveAbnormal } from "@/api/mms/abnormal";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_abnormal_type, mms_abnormal_severity, mms_abnormal_status } = proxy.useDict("mms_abnormal_type", "mms_abnormal_severity", "mms_abnormal_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_abnormal_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true);
const showSearch = ref(true);
const showAdvanced = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const activeStatusTab = ref("all");
const statusCounts = ref({});
const showStatusHelp = ref(false);
const respondOpen = ref(false);
const resolveOpen = ref(false);

const statusTabList = computed(() => mms_abnormal_status.value ? mms_abnormal_status.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  abnormalNo: { label: '异常单号', visible: true }, workOrderNo: { label: '工单号', visible: true },
  resourceName: { label: '产能单元', visible: true }, abnormalType: { label: '异常类型', visible: true },
  severity: { label: '严重等级', visible: true }, description: { label: '描述', visible: true },
  reportBy: { label: '上报人', visible: true }, reportTime: { label: '上报时间', visible: true },
  status: { label: '状态', visible: true }
}

function loadColumnVisibility() {
  try { const saved = localStorage.getItem('mms_abnormal_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let c = 0; if (queryParams.value.abnormalNo) c++; if (queryParams.value.workOrderNo) c++; if (queryParams.value.abnormalType) c++; if (queryParams.value.severity) c++; if (queryParams.value.status) c++; if (queryParams.value.resourceName) c++; if (queryParams.value.reportBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c;
});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, abnormalNo: undefined, workOrderNo: undefined, abnormalType: undefined, severity: undefined, status: undefined, resourceName: undefined, reportBy: undefined, params: {} },
  rules: { abnormalType: [{ required: true, message: "请选择异常类型", trigger: "change" }], severity: [{ required: true, message: "请选择严重等级", trigger: "change" }], description: [{ required: true, message: "请输入异常描述", trigger: "blur" }] },
  respondForm: {}, resolveForm: {}
});
const { queryParams, form, rules, respondForm, resolveForm } = toRefs(data);

function getList() {
  loading.value = true;
  listAbnormal(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); });
}
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.abnormalNo) baseQuery.abnormalNo = queryParams.value.abnormalNo; if (queryParams.value.workOrderNo) baseQuery.workOrderNo = queryParams.value.workOrderNo; if (queryParams.value.abnormalType) baseQuery.abnormalType = queryParams.value.abnormalType; if (queryParams.value.severity) baseQuery.severity = queryParams.value.severity; if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName; if (queryParams.value.reportBy) baseQuery.reportBy = queryParams.value.reportBy; listAbnormal(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_abnormal_status.value) { mms_abnormal_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.abnormalNo = undefined; queryParams.value.workOrderNo = undefined; queryParams.value.abnormalType = undefined; queryParams.value.severity = undefined; queryParams.value.status = undefined; queryParams.value.resourceName = undefined; queryParams.value.reportBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.abnormalId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { abnormalNo: undefined, workOrderNo: undefined, resourceName: undefined, abnormalType: undefined, severity: undefined, description: undefined, remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增异常"; }
function handleUpdate(row) { reset(); const id = row.abnormalId || ids.value[0]; getAbnormal(id).then(response => { form.value = response.data; open.value = true; title.value = "修改异常"; }); }
function handleView(row) { const id = row.abnormalId || ids.value[0]; getAbnormal(id).then(response => { viewData.value = response.data; viewOpen.value = true; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.abnormalId != null) { updateAbnormal(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addAbnormal(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.abnormalId || ids.value; proxy.$modal.confirm('是否确认删除选中的异常记录？').then(() => delAbnormal(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/abnormal/export", { ...queryParams.value }, `abnormal_${new Date().getTime()}.xlsx`); }
function handleRespond(row) { respondForm.value = { abnormalId: row.abnormalId, abnormalNo: row.abnormalNo, responseBy: "" }; respondOpen.value = true; }
function submitRespond() { respondAbnormal(respondForm.value.abnormalId, respondForm.value.responseBy).then(() => { respondOpen.value = false; getList(); proxy.$modal.msgSuccess("响应成功"); }); }
function handleResolve(row) { resolveForm.value = { abnormalId: row.abnormalId, abnormalNo: row.abnormalNo, handleResult: "" }; resolveOpen.value = true; }
function submitResolve() { resolveAbnormal(resolveForm.value.abnormalId, resolveForm.value.handleResult).then(() => { resolveOpen.value = false; getList(); proxy.$modal.msgSuccess("处理关闭成功"); }); }

function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_abnormal_status, status); }
function badgeClass(status) { const map = { '0': 'red', '1': 'amber', '2': 'green' }; return map[status] || 'gray'; }
function statusTabClass(value) { const map = { '0': 'tab-reject', '1': 'tab-draft', '2': 'tab-done' }; return map[value] || ''; }
function abnormalTypeBadgeClass(type) { const map = { '0': 'red', '1': 'amber', '2': 'blue', '3': 'gray' }; return map[type] || 'gray'; }
function severityBadgeClass(severity) { const map = { '0': 'red', '1': 'amber', '2': 'blue' }; return map[severity] || 'gray'; }

getList();
</script>

<style scoped>
.mms-abnormal-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-abnormal-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-abnormal-page .filter-card{padding:14px 20px 16px}
.mms-abnormal-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-abnormal-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-abnormal-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-abnormal-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-abnormal-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-abnormal-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-abnormal-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-abnormal-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-abnormal-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-abnormal-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-abnormal-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-abnormal-page .field{display:flex;flex-direction:column;gap:6px}
.mms-abnormal-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-abnormal-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-abnormal-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-abnormal-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-abnormal-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-abnormal-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-abnormal-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-abnormal-page .field .control :deep(.el-select){width:100%}
.mms-abnormal-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-abnormal-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-abnormal-page .toolbar .left,.mms-abnormal-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-abnormal-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-abnormal-page .table-wrap{overflow-x:auto}
.mms-abnormal-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-abnormal-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-abnormal-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-abnormal-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-abnormal-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-abnormal-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-abnormal-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-abnormal-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-abnormal-page .badge.amber .dot{background:var(--amber-500)}
.mms-abnormal-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-abnormal-page .badge.blue .dot{background:var(--blue-500)}
.mms-abnormal-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-abnormal-page .badge.green .dot{background:var(--green-500)}
.mms-abnormal-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-abnormal-page .badge.red .dot{background:var(--red-500)}
.mms-abnormal-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-abnormal-page .badge.gray .dot{background:var(--ink-400)}
.mms-abnormal-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-abnormal-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-abnormal-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-abnormal-page .tabs-track::-webkit-scrollbar{display:none}
.mms-abnormal-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-abnormal-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-abnormal-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-abnormal-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-abnormal-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-abnormal-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-abnormal-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-abnormal-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-abnormal-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-abnormal-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-abnormal-page .status-tab.tab-audit .dot{background:var(--blue-500)}.mms-abnormal-page .status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)}.mms-abnormal-page .status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
.mms-abnormal-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-abnormal-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-abnormal-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-abnormal-page .status-tab.tab-reject .dot{background:var(--red-500)}.mms-abnormal-page .status-tab.tab-reject .count{background:var(--red-50);color:var(--red-700)}.mms-abnormal-page .status-tab.is-active.tab-reject .count{background:var(--red-500);color:#fff}
.mms-abnormal-page .status-tab.tab-void .dot{background:var(--ink-400)}.mms-abnormal-page .status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)}.mms-abnormal-page .status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
.mms-abnormal-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-abnormal-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-abnormal-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.status-help-content{max-height:500px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{background-color:#ecf5ff;border-radius:8px;padding:16px;border-left:4px solid #409eff}
.status-help-content .highlight-card p{margin:6px 0;line-height:1.6;font-size:13px;color:#606266}
@media(max-width:1100px){.mms-abnormal-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-abnormal-page .filter-card .filter-bar{grid-template-columns:1fr}}
</style>