<template>
  <div class="app-container mms-downtime-page">
    <!-- ===== 筛选条件卡片 ===== -->
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
          <label>停机单号</label>
          <div class="control">
            <el-input v-model="queryParams.downtimeNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产能单元</label>
          <div class="control"><el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field">
          <label>停机类型</label>
          <div class="control is-select"><el-select v-model="queryParams.dtType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_downtime_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field">
          <label>停机分类</label>
          <div class="control is-select"><el-select v-model="queryParams.dtCategory" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_downtime_category" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>停机级别</label>
          <div class="control is-select"><el-select v-model="queryParams.dtLevel" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_downtime_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>上报人</label>
          <div class="control"><el-input v-model="queryParams.reportBy" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>停机日期</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <!-- ===== 数据列表卡片 ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTypeTab === 'all' }" @click="handleTypeTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ typeCounts.all || 0 }}</span></button>
          <button v-for="t in typeTabList" :key="t.value" class="status-tab" :class="[typeTabClass(t.value), { 'is-active': activeTypeTab === t.value }]" @click="handleTypeTabClick(t.value)"><span class="dot"></span><span>{{ t.label }}</span><span class="count">{{ typeCounts[t.value] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:downtime:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:downtime:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:downtime:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:downtime:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_downtime_columns" /></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="停机单号" prop="downtimeNo" key="downtimeNo" :width="colWidth('downtimeNo', 150)" resizable show-overflow-tooltip v-if="columns.downtimeNo.visible" />
          <el-table-column label="关联异常单号" prop="abnormalNo" key="abnormalNo" :width="colWidth('abnormalNo', 140)" resizable align="center" v-if="columns.abnormalNo.visible">
            <template #default="scope">
              <span v-if="scope.row.abnormalNo" class="badge blue"><span class="dot"></span>{{ scope.row.abnormalNo }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="产能单元" prop="resourceName" key="resourceName" :width="colWidth('resourceName', 150)" resizable show-overflow-tooltip v-if="columns.resourceName.visible" />
          <el-table-column label="停机类型" prop="dtType" key="dtType" :width="colWidth('dtType', 100)" resizable align="center" v-if="columns.dtType.visible">
            <template #default="scope"><span v-if="scope.row.dtType" class="badge" :class="dtTypeBadgeClass(scope.row.dtType)"><span class="dot"></span>{{ dictLabel(mms_downtime_type, scope.row.dtType) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="停机分类" prop="dtCategory" key="dtCategory" :width="colWidth('dtCategory', 100)" resizable align="center" v-if="columns.dtCategory.visible">
            <template #default="scope"><span v-if="scope.row.dtCategory" class="badge" :class="scope.row.dtCategory === '0' ? 'blue' : 'red'"><span class="dot"></span>{{ dictLabel(mms_downtime_category, scope.row.dtCategory) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="停机级别" prop="dtLevel" key="dtLevel" :width="colWidth('dtLevel', 90)" resizable align="center" v-if="columns.dtLevel.visible">
            <template #default="scope"><span v-if="scope.row.dtLevel != null" class="badge" :class="dtLevelBadgeClass(scope.row.dtLevel)"><span class="dot"></span>{{ dictLabel(mms_downtime_level, scope.row.dtLevel) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="开始时间" prop="startTime" key="startTime" :width="colWidth('startTime', 160)" resizable align="center" v-if="columns.startTime.visible"><template #default="scope">{{ parseTime(scope.row.startTime) }}</template></el-table-column>
          <el-table-column label="结束时间" prop="endTime" key="endTime" :width="colWidth('endTime', 160)" resizable align="center" v-if="columns.endTime.visible"><template #default="scope">{{ scope.row.endTime ? parseTime(scope.row.endTime) : '—' }}</template></el-table-column>
          <el-table-column label="停机时长(小时)" prop="hours" key="hours" :width="colWidth('hours', 120)" resizable align="center" v-if="columns.hours.visible"><template #default="scope">{{ scope.row.hours != null ? scope.row.hours + ' 小时' : '—' }}</template></el-table-column>
          <el-table-column label="上报人" prop="reportBy" key="reportBy" :width="colWidth('reportBy', 90)" resizable align="center" v-if="columns.reportBy.visible" />
          <el-table-column label="停机原因" prop="reason" key="reason" :width="colWidth('reason', 200)" resizable show-overflow-tooltip v-if="columns.reason.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:downtime:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:downtime:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="停机单号" prop="downtimeNo"><el-input v-model="form.downtimeNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="产能单元" prop="resourceName" required><el-input v-model="form.resourceName" readonly placeholder="请选择产能单元" style="width: 100%" @click="openResourcePicker"><template #append><el-button icon="Search" @click="openResourcePicker" /></template><template #suffix><el-icon v-if="form.resourceName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearResource"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="停机类型" prop="dtType"><el-select v-model="form.dtType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_downtime_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="停机分类" prop="dtCategory"><el-select v-model="form.dtCategory" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_downtime_category" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="停机级别" prop="dtLevel"><el-select v-model="form.dtLevel" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_downtime_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="上报人" prop="reportBy"><el-input v-model="form.reportBy" readonly placeholder="默认当前用户" style="width: 100%" @click="openReportByPicker"><template #append><el-button icon="Search" @click="openReportByPicker" /></template><template #suffix><el-icon v-if="form.reportBy" class="rd-form-tip" style="cursor:pointer" @click.stop="clearReportBy"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>停机详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" @change="calcDowntimeHours" /></el-form-item></el-col><el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width:100%" @change="calcDowntimeHours" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="停机时长(小时)" label-width="120px"><el-input v-model="form.hours" placeholder="自动计算" disabled /></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-form-item label="停机原因" prop="reason"><el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
              <el-form-item label="处理结果" prop="handleResult"><el-input v-model="form.handleResult" type="textarea" :rows="2" placeholder="恢复时填写处理结果" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 产能单元选择弹窗 ===== -->
    <el-dialog v-model="resourcePickerOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <span class="rd-detail-header-title">选择产能单元</span>
        </div>
      </template>
      <div style="display:flex;gap:8px;margin-bottom:12px">
        <el-input v-model="resourcePickerQuery.resourceName" placeholder="产能单元名称" clearable size="small" style="width:200px" @keyup.enter="handleResourcePickerQuery">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="resourcePickerQuery.lineName" placeholder="产线" clearable size="small" style="width:160px" @keyup.enter="handleResourcePickerQuery" />
        <el-button type="primary" plain icon="Search" size="small" @click="handleResourcePickerQuery">查询</el-button>
        <el-button icon="RefreshLeft" size="small" @click="resetResourcePickerQuery">重置</el-button>
      </div>
      <el-table v-loading="resourcePickerLoading" :data="resourcePickerList" highlight-current-row @row-click="onResourceRowClick" @row-dblclick="onResourceRowDblClick" height="360" size="small">
        <el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="resourcePickerSelectedId" :value="row.resourceId" @click.stop><span /></el-radio></template></el-table-column>
        <el-table-column label="资源编码" prop="resourceCode" width="130" show-overflow-tooltip />
        <el-table-column label="产能单元" prop="resourceName" min-width="150" show-overflow-tooltip />
        <el-table-column label="产线" prop="lineName" width="100" show-overflow-tooltip />
        <el-table-column label="车间" prop="workshopName" width="100" show-overflow-tooltip />
      </el-table>
      <div style="display:flex;justify-content:flex-end;padding-top:8px">
        <el-pagination v-model:current-page="resourcePickerQuery.pageNum" v-model:page-size="resourcePickerQuery.pageSize" :total="resourcePickerTotal" layout="total, prev, pager, next" small @current-change="getResourcePickerList" />
      </div>
      <div v-if="resourcePickerList.length > 0" style="margin-top:6px;font-size:12px;color:#94a3b8;text-align:center">双击行可选择并带出产能单元</div>
      <template #footer><el-button @click="resourcePickerOpen = false">取 消</el-button><el-button type="primary" @click="confirmResourcePicker" :disabled="!resourcePickerSelectedId">确 定</el-button></template>
    </el-dialog>

    <!-- ===== 上报人选择弹窗 ===== -->
    <user-picker ref="userPickerRef" title="选择上报人" @confirm="onReportByPickerConfirm" />

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/></svg></div>
          <span class="rd-detail-header-title">停机记录详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.downtimeNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.downtimeNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">停机单号</span><div class="rd-value">{{ viewData.downtimeNo || '—' }}</div></div><div class="rd-item"><span class="rd-label">关联异常单号</span><div class="rd-value"><span v-if="viewData.abnormalNo" class="badge blue"><span class="dot"></span>{{ viewData.abnormalNo }}</span><span v-else class="text-muted">无关联</span></div></div><div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '—' }}</div></div><div class="rd-item"><span class="rd-label">停机分类</span><div class="rd-value"><dict-tag :options="mms_downtime_category" :value="viewData.dtCategory" /></div></div><div class="rd-item"><span class="rd-label">停机级别</span><div class="rd-value"><dict-tag :options="mms_downtime_level" :value="viewData.dtLevel" /></div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>停机详情</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block"><div class="rd-grid"><div class="rd-item"><span class="rd-label">停机类型</span><div class="rd-value"><dict-tag :options="mms_downtime_type" :value="viewData.dtType" /></div></div><div class="rd-item"><span class="rd-label">停机时长</span><div class="rd-value">{{ viewData.hours != null ? viewData.hours + ' 小时' : '—' }}</div></div><div class="rd-item"><span class="rd-label">开始时间</span><div class="rd-value">{{ viewData.startTime ? parseTime(viewData.startTime) : '—' }}</div></div><div class="rd-item"><span class="rd-label">结束时间</span><div class="rd-value">{{ viewData.endTime ? parseTime(viewData.endTime) : '—' }}</div></div></div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>描述信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">停机原因</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.reason }">{{ viewData.reason || '暂无' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">处理结果</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.handleResult }">{{ viewData.handleResult || '暂无' }}</div></div><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div><div class="rd-item"><span class="rd-label">上报人</span><div class="rd-value">{{ viewData.reportBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">处理人</span><div class="rd-value">{{ viewData.handleBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '—' }}</div></div><div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '—' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="停机管理业务操作说明" width="984px" append-to-body>
      <div class="status-help-content">
        <h4>一、停机管理释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是停机管理？</div>
          <div class="highlight-card-body">
            <strong>停机管理（Downtime Management）</strong>是生产管控中记录生产设备/产线停机事件的单据。停机管理记录停机开始/结束时间、停机类型、停机分类、停机级别和停机原因，支持设备利用率分析和产能损耗统计。<br/><br/>
            停机管理遵循<strong>EMS（设备管理系统）</strong>标准处理机制：通过<strong>停机分类</strong>（计划停机/非计划停机）区分可避免和不可避免的停机事件，通过<strong>停机级别</strong>（一般/重要/紧急）确定处理优先级，通过<strong>停机类型</strong>（故障/换型/物料等）支持根因分析和持续改善，满足精益生产对设备运行状态可追溯、可量化、可管控的要求。
          </div>
        </div>

        <h4>二、停机记录说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="停机记录">记录设备/产线停机事件的完整信息，包括停机开始/结束时间、停机时长、停机类型、分类、级别和原因。填写结束时间后系统自动计算停机时长</el-descriptions-item>
        </el-descriptions>

        <h4>三、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>停机单号：</strong>停机记录的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>产能单元：</strong>发生停机的设备/产线，点击输入框右侧搜索按钮弹框选择<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>停机类型：</strong>停机原因分类（故障停机/换型停机/物料停机/其他停机）</p>
            <p>• <strong>停机分类：</strong>EMS标准分类（计划停机/非计划停机），用于OEE计算时区分可避免停机</p>
            <p>• <strong>停机级别：</strong>事件严重程度（一般/重要/紧急），影响处理优先级</p>
            <p>• <strong>上报人：</strong>默认当前登录用户，可修改</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">停机详情区</div>
          <div class="highlight-card-body">
            <p>• <strong>开始时间：</strong>停机开始的准确时间<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>结束时间：</strong>停机恢复的时间，填写后系统自动计算停机时长</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">描述信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>停机原因：</strong>详细描述停机的原因和经过</p>
            <p>• <strong>处理结果：</strong>恢复生产时填写处理措施和结果</p>
            <p>• <strong>备注：</strong>停机记录的补充说明信息</p>
          </div>
        </div>

        <h4>四、停机管理业务规则</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">EMS标准停机管控机制</div>
          <div class="highlight-card-body">
            <strong>停机管理</strong>遵循EMS标准处理机制，记录设备停机事件的全过程信息。停机时长的自动计算机制确保设备效率数据准确归集，停机分类和类型分类支持根因分析和持续改善。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">业务规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>停机时长自动计算：</strong>填写结束时间后系统自动计算停机时长（小时），无需手动输入</p>
            <p>2. <strong>处理人自动记录：</strong>填写结束时间时系统自动记录处理人为当前操作用户</p>
            <p>3. <strong>停机分类影响OEE：</strong>计划停机（保养/换型）不计入设备损失，非计划停机（故障/物料）计入设备损失</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>停机记录的操作均记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="danger" :hollow="true">
            <strong>记录停机：</strong>设备/产线停机时点击「新增」创建停机记录，选择产能单元、填写开始时间、停机类型、分类和级别
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>记录恢复：</strong>停机结束后填写结束时间和处理结果，系统自动计算停机时长，处理人自动记录
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>点击「查看」查看停机记录完整信息，包括停机时长、停机原因、处理结果等
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>分析统计：</strong>停机数据用于设备利用率分析、OEE计算和产能损耗统计，按分类/类型/级别多维分析
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Downtime">
import { listDowntime, getDowntime, addDowntime, updateDowntime, delDowntime } from "@/api/mms/downtime";
import { listResource } from "@/api/mms/resource";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, CircleClose } from '@element-plus/icons-vue'
import useUserStore from '@/store/modules/user'
import UserPicker from '@/components/UserPicker/index.vue'
const { proxy } = getCurrentInstance();
const { mms_downtime_type, mms_downtime_category, mms_downtime_level } = proxy.useDict("mms_downtime_type", "mms_downtime_category", "mms_downtime_level");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_downtime_index')
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
const activeTypeTab = ref("all");
const typeCounts = ref({});
const showStatusHelp = ref(false);

const typeTabList = computed(() => mms_downtime_type.value ? mms_downtime_type.value.map(d => ({ label: d.label, value: d.value })) : []);

const defaultColumns = {
  downtimeNo: { label: '停机单号', visible: true },
  abnormalNo: { label: '关联异常单号', visible: true },
  resourceName: { label: '产能单元', visible: true },
  dtType: { label: '停机类型', visible: true },
  dtCategory: { label: '停机分类', visible: true },
  dtLevel: { label: '停机级别', visible: true },
  startTime: { label: '开始时间', visible: true },
  endTime: { label: '结束时间', visible: true },
  hours: { label: '停机时长(小时)', visible: true },
  reportBy: { label: '上报人', visible: true },
  reason: { label: '停机原因', visible: true }
};

function loadColumnVisibility() {
  try { const saved = localStorage.getItem('mms_downtime_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

const activeFilterCount = computed(() => {
  let c = 0; if (queryParams.value.downtimeNo) c++; if (queryParams.value.resourceName) c++; if (queryParams.value.dtType) c++; if (queryParams.value.dtCategory) c++; if (queryParams.value.dtLevel) c++; if (queryParams.value.reportBy) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c;
});

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, downtimeNo: undefined, resourceName: undefined, dtType: undefined, dtCategory: undefined, dtLevel: undefined, reportBy: undefined, params: {} },
  rules: { resourceName: [{ required: true, message: "请选择产能单元", trigger: "change" }], startTime: [{ required: true, message: "请选择开始时间", trigger: "change" }], dtType: [{ required: true, message: "请选择停机类型", trigger: "change" }] }
});
const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listDowntime(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => { dataList.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths(); loadTypeCounts(); });
}
function loadTypeCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.downtimeNo) baseQuery.downtimeNo = queryParams.value.downtimeNo; if (queryParams.value.resourceName) baseQuery.resourceName = queryParams.value.resourceName; if (queryParams.value.dtCategory) baseQuery.dtCategory = queryParams.value.dtCategory; if (queryParams.value.dtLevel) baseQuery.dtLevel = queryParams.value.dtLevel; if (queryParams.value.reportBy) baseQuery.reportBy = queryParams.value.reportBy; listDowntime(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_downtime_type.value) { mms_downtime_type.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.dtType] !== undefined) counts[r.dtType]++; }); } typeCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.downtimeNo = undefined; queryParams.value.resourceName = undefined; queryParams.value.dtType = undefined; queryParams.value.dtCategory = undefined; queryParams.value.dtLevel = undefined; queryParams.value.reportBy = undefined; dateRange.value = []; queryParams.value.params = {}; activeTypeTab.value = 'all'; handleQuery(); }
function handleTypeTabClick(type) { activeTypeTab.value = type; queryParams.value.dtType = type === "all" ? undefined : type; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.downtimeId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { downtimeId: undefined, downtimeNo: undefined, resourceId: undefined, resourceName: undefined, startTime: undefined, endTime: undefined, dtType: undefined, dtCategory: '0', dtLevel: '0', reason: undefined, reportBy: useUserStore().nickName, handleBy: undefined, handleResult: undefined, remark: undefined, hours: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); open.value = true; title.value = "新增停机记录"; }
function handleUpdate(row) { reset(); const id = row.downtimeId || ids.value[0]; getDowntime(id).then(res => { form.value = res.data; open.value = true; title.value = "修改停机记录"; }); }
function handleView(row) { const id = row.downtimeId || ids.value[0]; getDowntime(id).then(res => { viewData.value = res.data; viewOpen.value = true; }); }
function submitForm() {
  // 填写了结束时间时自动计算停机时长
  if (form.value.endTime) { calcDowntimeHours(); }
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.downtimeId != null) { updateDowntime(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); }
      else { addDowntime(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); }
    }
  });
}
function cancel() { open.value = false; reset(); }
function handleDelete(row) { const delIds = row.downtimeId || ids.value; proxy.$modal.confirm('确认删除选中的停机记录？').then(() => delDowntime(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/downtime/export", { ...queryParams.value }, `downtime_${new Date().getTime()}.xlsx`); }

// ===== 产能单元弹框选择 =====
const resourcePickerOpen = ref(false)
const resourcePickerLoading = ref(false)
const resourcePickerList = ref([])
const resourcePickerTotal = ref(0)
const resourcePickerSelectedId = ref(null)
const resourcePickerSelectedRow = ref(null)
const resourcePickerQuery = reactive({ pageNum: 1, pageSize: 10, resourceName: undefined, lineName: undefined, status: '0' })

function openResourcePicker() {
  resourcePickerOpen.value = true
  resourcePickerSelectedId.value = form.value.resourceId || null
  resourcePickerSelectedRow.value = null
  resourcePickerQuery.pageNum = 1
  resourcePickerQuery.resourceName = undefined
  resourcePickerQuery.lineName = undefined
  getResourcePickerList()
}
function getResourcePickerList() {
  resourcePickerLoading.value = true
  listResource(resourcePickerQuery).then(res => {
    resourcePickerList.value = res.rows
    resourcePickerTotal.value = res.total
    resourcePickerLoading.value = false
  }).catch(() => { resourcePickerLoading.value = false })
}
function handleResourcePickerQuery() { resourcePickerQuery.pageNum = 1; getResourcePickerList() }
function resetResourcePickerQuery() { resourcePickerQuery.resourceName = undefined; resourcePickerQuery.lineName = undefined; handleResourcePickerQuery() }
function onResourceRowClick(row) { resourcePickerSelectedId.value = row.resourceId; resourcePickerSelectedRow.value = row }
function onResourceRowDblClick(row) { onResourceRowClick(row); confirmResourcePicker() }
function confirmResourcePicker() {
  if (!resourcePickerSelectedRow.value) { proxy.$modal.msgWarning('请先选择产能单元'); return }
  const row = resourcePickerSelectedRow.value
  form.value.resourceId = row.resourceId
  form.value.resourceName = row.resourceName
  resourcePickerOpen.value = false
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('resourceName')
}
function clearResource() {
  form.value.resourceId = undefined
  form.value.resourceName = ''
  proxy.$refs["formRef"] && proxy.$refs["formRef"].validateField('resourceName')
}

function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }

// ===== 上报人弹框选择 =====
function openReportByPicker() {
  proxy.$refs.userPickerRef.open()
}
function onReportByPickerConfirm(user) {
  form.value.reportBy = user.nickName
}
function clearReportBy() {
  form.value.reportBy = undefined
}

// ===== 自动计算停机时长(小时) =====
function calcDowntimeHours() {
  if (form.value.startTime && form.value.endTime) {
    const start = new Date(form.value.startTime).getTime()
    const end = new Date(form.value.endTime).getTime()
    if (end > start) {
      const diffHours = (end - start) / (1000 * 60 * 60)
      form.value.hours = diffHours.toFixed(2)
    } else {
      form.value.hours = undefined
    }
  } else {
    form.value.hours = undefined
  }
}
function dtTypeBadgeClass(type) { const map = { '1': 'red', '2': 'amber', '3': 'amber', '9': 'gray' }; return map[type] || 'gray'; }
function dtLevelBadgeClass(level) { const map = { '0': 'blue', '1': 'amber', '2': 'red' }; return map[level] || 'gray'; }
function typeTabClass(value) { const map = { '1': 'tab-reject', '2': 'tab-draft', '3': 'tab-draft', '9': 'tab-void' }; return map[value] || ''; }

getList();
</script>

<style scoped>
.mms-downtime-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--violet-50:#f5f3ff;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-downtime-page .surface{background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-lg);box-shadow:var(--shadow-card);overflow:hidden;margin-bottom:8px}
.mms-downtime-page .filter-card{padding:14px 20px 16px}
.mms-downtime-page .filter-card .filter-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px}
.mms-downtime-page .filter-card .filter-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-downtime-page .filter-card .filter-title .glyph{width:4px;height:14px;background:var(--brand-600);border-radius:2px}
.mms-downtime-page .filter-card .adv-link{font-size:14px;color:var(--ink-500);text-decoration:none;display:flex;align-items:center;gap:4px;transition:color .15s;cursor:pointer}
.mms-downtime-page .filter-card .adv-link:hover{color:var(--brand-600)}
.mms-downtime-page .filter-card .adv-link .chev{transition:transform .2s var(--ease-out)}
.mms-downtime-page .filter-card .adv-link.is-open .chev{transform:rotate(180deg)}
.mms-downtime-page .filter-card .filter-bar{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px 16px}
.mms-downtime-page .filter-card .filter-actions{display:flex;align-items:center;justify-content:space-between;margin-top:14px;padding-top:14px;border-top:1px dashed var(--ink-200)}
.mms-downtime-page .filter-card .filter-info{font-size:13px;color:var(--ink-500);display:flex;align-items:center;gap:6px}
.mms-downtime-page .filter-card .filter-buttons{display:flex;gap:8px}
.mms-downtime-page .field{display:flex;flex-direction:column;gap:6px}
.mms-downtime-page .field label{font-size:14px;font-weight:500;color:var(--ink-700)}
.mms-downtime-page .field .control{display:flex;align-items:center;height:36px;padding:0 12px;background:#fff;border:1px solid var(--ink-200);border-radius:var(--r-sm)}
.mms-downtime-page .field .control:focus-within{border-color:var(--brand-500);box-shadow:0 0 0 3px rgba(99,102,241,.15)}
.mms-downtime-page .field .control :deep(.el-input__wrapper){box-shadow:none!important;background:transparent!important;padding:0;height:34px}
.mms-downtime-page .field .control :deep(.el-input__inner){border:0;background:transparent;font-size:14px;color:var(--ink-900);height:34px;line-height:34px}
.mms-downtime-page .field .control :deep(.el-input__inner::placeholder){color:var(--ink-400)}
.mms-downtime-page .field .control :deep(.el-input__prefix){color:var(--ink-400);margin-right:4px}
.mms-downtime-page .field .control :deep(.el-select){width:100%}
.mms-downtime-page .field .control :deep(.el-select .el-select__wrapper){box-shadow:none!important;background:transparent!important;padding:0;min-height:34px;height:34px}
.mms-downtime-page .toolbar{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-bottom:1px solid var(--ink-200);background:var(--ink-50)}
.mms-downtime-page .toolbar .left,.mms-downtime-page .toolbar .right{display:flex;gap:8px;align-items:center}
.mms-downtime-page .toolbar-divider{width:1px;height:18px;background:var(--ink-200);margin:0 4px}
.mms-downtime-page .table-wrap{overflow-x:auto}
.mms-downtime-page .app-table{--el-table-bg-color:#fff;--el-table-header-bg-color:var(--ink-50);--el-table-row-hover-bg-color:#fafbff;--el-table-border-color:transparent;--el-table-text-color:var(--ink-700);--el-table-header-text-color:var(--ink-500)}
.mms-downtime-page .app-table :deep(.el-table__body td){border-right-color:transparent!important}
.mms-downtime-page .app-table :deep(.el-table__header th){border-right-color:transparent!important;background:var(--ink-50)!important;color:var(--ink-500);font-weight:600;font-size:14px;padding:12px 16px;border-bottom:1px solid var(--ink-200)}
.mms-downtime-page .app-table :deep(.el-table__body td){padding:14px 16px;border-bottom:1px solid var(--ink-100);color:var(--ink-700)}
.mms-downtime-page .app-table :deep(.el-table__inner-wrapper::before){display:none}
.mms-downtime-page .badge{display:inline-flex;align-items:center;gap:5px;padding:3px 9px;border-radius:999px;font-size:13px;font-weight:600;line-height:1;border:1px solid transparent}
.mms-downtime-page .badge .dot{width:6px;height:6px;border-radius:50%}
.mms-downtime-page .badge.amber{background:var(--amber-50);color:var(--amber-700);border-color:#fde68a}.mms-downtime-page .badge.amber .dot{background:var(--amber-500)}
.mms-downtime-page .badge.blue{background:var(--blue-50);color:var(--blue-700);border-color:#bfdbfe}.mms-downtime-page .badge.blue .dot{background:var(--blue-500)}
.mms-downtime-page .badge.green{background:var(--green-50);color:var(--green-700);border-color:#a7f3d0}.mms-downtime-page .badge.green .dot{background:var(--green-500)}
.mms-downtime-page .badge.red{background:var(--red-50);color:var(--red-700);border-color:#fecaca}.mms-downtime-page .badge.red .dot{background:var(--red-500)}
.mms-downtime-page .badge.gray{background:var(--ink-100);color:var(--ink-500);border-color:var(--ink-200)}.mms-downtime-page .badge.gray .dot{background:var(--ink-400)}
.mms-downtime-page .pagination-container{display:flex;align-items:center;justify-content:flex-end;padding:14px 20px;background:#fff}
.mms-downtime-page .status-tabs{display:flex;align-items:center;gap:12px;padding:6px 10px 6px 12px;border-bottom:1px solid var(--ink-200);background:#fff}
.mms-downtime-page .tabs-track{display:flex;align-items:center;gap:4px;flex:1;min-width:0;overflow-x:auto;scrollbar-width:none}
.mms-downtime-page .tabs-track::-webkit-scrollbar{display:none}
.mms-downtime-page .status-tab{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:var(--r-sm);font-size:14px;color:var(--ink-500);cursor:pointer;user-select:none;transition:all .15s var(--ease-out);white-space:nowrap;border:1px solid transparent;background:transparent}
.mms-downtime-page .status-tab .dot{width:6px;height:6px;border-radius:50%;background:var(--ink-300)}
.mms-downtime-page .status-tab .count{font-size:12px;font-weight:600;padding:1px 6px;border-radius:999px;background:var(--ink-100);color:var(--ink-500);min-width:18px;text-align:center;line-height:1.4}
.mms-downtime-page .status-tab:hover{background:var(--ink-50);color:var(--ink-700)}
.mms-downtime-page .status-tab.is-active{background:var(--brand-50);color:var(--brand-700);font-weight:600;border-color:var(--brand-200)}
.mms-downtime-page .status-tab.is-active .count{background:var(--brand-600);color:#fff}
.mms-downtime-page .status-tab.is-active .dot{background:var(--brand-500)}
.mms-downtime-page .status-tab.tab-draft .dot{background:var(--amber-500)}.mms-downtime-page .status-tab.tab-draft .count{background:var(--amber-50);color:var(--amber-700)}.mms-downtime-page .status-tab.is-active.tab-draft .count{background:var(--amber-500);color:#fff}
.mms-downtime-page .status-tab.tab-audit .dot{background:var(--blue-500)}.mms-downtime-page .status-tab.tab-audit .count{background:var(--blue-50);color:var(--blue-700)}.mms-downtime-page .status-tab.is-active.tab-audit .count{background:var(--blue-500);color:#fff}
.mms-downtime-page .status-tab.tab-done .dot{background:var(--green-500)}.mms-downtime-page .status-tab.tab-done .count{background:var(--green-50);color:var(--green-700)}.mms-downtime-page .status-tab.is-active.tab-done .count{background:var(--green-500);color:#fff}
.mms-downtime-page .status-tab.tab-reject .dot{background:var(--red-500)}.mms-downtime-page .status-tab.tab-reject .count{background:var(--red-50);color:var(--red-700)}.mms-downtime-page .status-tab.is-active.tab-reject .count{background:var(--red-500);color:#fff}
.mms-downtime-page .status-tab.tab-void .dot{background:var(--ink-400)}.mms-downtime-page .status-tab.tab-void .count{background:var(--ink-100);color:var(--ink-500)}.mms-downtime-page .status-tab.is-active.tab-void .count{background:var(--ink-400);color:#fff}
.mms-downtime-page .tip-pill{display:inline-flex;align-items:center;gap:5px;height:30px;padding:0 10px;background:#fffaf0;border:1px solid #fde68a;color:#92400e;border-radius:999px;font-size:13px;font-weight:500;cursor:pointer;transition:all .15s var(--ease-out);flex-shrink:0;white-space:nowrap}
.mms-downtime-page .tip-pill:hover{background:var(--amber-50);border-color:var(--amber-500);color:#7c2d12}
.mms-downtime-page .tip-pill .el-icon{font-size:14px;color:var(--amber-700)}
.mms-downtime-page .text-muted{color:var(--ink-400)}
.mms-downtime-page .rd-page{max-width:760px;margin:0 auto}
.mms-downtime-page .rd-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:12px 24px}
.mms-downtime-page .rd-item{display:flex;align-items:center;gap:12px}
.mms-downtime-page .rd-item--full{grid-column:1 / -1}
.mms-downtime-page .rd-label{flex:0 0 auto;min-width:72px;display:flex;align-items:center;font-size:14px;font-weight:500;color:var(--ink-500);white-space:nowrap}
.mms-downtime-page .rd-value{flex:1 1 auto;font-size:14px;font-weight:500;color:var(--ink-900);line-height:1.5;padding-left:12px;border-left:1px solid var(--ink-200);min-width:0}
.mms-downtime-page .rd-value--muted{color:var(--ink-400);font-style:italic}
.mms-downtime-page .rd-detail-header{display:flex;align-items:center;gap:10px;padding:8px 16px;background:linear-gradient(135deg,#1e3a8a 0%,#2563eb 60%,#3b82f6 100%);border-radius:12px 12px 0 0;position:relative;overflow:hidden}
.mms-downtime-page .rd-detail-header::before{content:'';position:absolute;top:-25px;right:-10px;width:120px;height:120px;border-radius:50%;background:radial-gradient(circle,rgb(255 255 255 / 0.12) 0%,transparent 70%);pointer-events:none}
.mms-downtime-page .rd-detail-header-icon{display:flex;align-items:center;justify-content:center;width:34px;height:34px;border-radius:8px;background:rgb(255 255 255 / 0.2);border:1px solid rgb(255 255 255 / 0.25);color:#fff;flex-shrink:0}
.mms-downtime-page .rd-detail-header-title{font-size:16px;font-weight:700;color:#fff}
.mms-downtime-page .rd-detail-header-sub{display:flex;align-items:center;gap:8px}
.mms-downtime-page .rd-detail-header-divider{width:1px;height:16px;background:rgb(255 255 255 / 0.3)}
.mms-downtime-page .rd-detail-header-no{font-size:12px;font-weight:500;color:rgb(255 255 255 / 0.85)}
.mms-downtime-page .rd-card{background:#fff;border:1px solid var(--ink-200);border-radius:10px;margin-bottom:12px;overflow:hidden}
.mms-downtime-page .rd-card-header{display:flex;align-items:center;justify-content:space-between;padding:12px 16px;cursor:pointer;background:var(--ink-50);border-bottom:1px solid var(--ink-200)}
.mms-downtime-page .rd-card-header:hover{background:var(--ink-100)}
.mms-downtime-page .rd-card-title{display:flex;align-items:center;gap:8px;font-size:14px;font-weight:600;color:var(--ink-700)}
.mms-downtime-page .rd-card-icon{display:flex;align-items:center;color:var(--brand-500)}
.mms-downtime-page .rd-collapse-btn{display:flex;align-items:center;justify-content:center;width:24px;height:24px;border:none;background:transparent;cursor:pointer;color:var(--ink-400);transition:transform .2s}
.mms-downtime-page .rd-collapse-btn.is-collapsed{transform:rotate(180deg)}
.mms-downtime-page .rd-card-body{padding:16px}
.mms-downtime-page .rd-form-tip{color:var(--ink-400)}
.status-help-content{max-height:520px;overflow-y:auto;padding-right:10px}
.status-help-content h4{margin:20px 0 12px 0;color:#303133;font-weight:600;border-left:4px solid #409eff;padding-left:10px}
.status-help-content h4:first-child{margin-top:0}
.status-help-content .status-flow{display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:16px;background-color:#f5f7fa;border-radius:8px;margin-bottom:8px}
.status-help-content .flow-item{display:flex;align-items:center;gap:8px}
.status-help-content .flow-arrow{color:#909399;font-size:16px}
.status-help-content .highlight-card{border-radius:8px;padding:16px;border:1px solid}
.status-help-content .highlight-card-title{font-size:14px;font-weight:600;margin-bottom:8px;display:flex;align-items:center}
.status-help-content .highlight-card-body{font-size:13px;color:#606266;line-height:1.6}
.status-help-content .highlight-card-body p{margin:4px 0}
.status-help-content .highlight-primary{background-color:#ecf5ff;border-color:#a0cfff}
.status-help-content .highlight-primary .highlight-card-title{color:#409eff}
.status-help-content .highlight-success{background-color:#f0f9eb;border-color:#b3e19d}
.status-help-content .highlight-success .highlight-card-title{color:#67c23a}
.status-help-content .highlight-warning{background-color:#fdf6ec;border-color:#f5dab1}
.status-help-content .highlight-warning .highlight-card-title{color:#e6a23c}
.status-help-content .highlight-danger{background-color:#fef0f0;border-color:#fbc4c4}
.status-help-content .highlight-danger .highlight-card-title{color:#f56c6c}
@media(max-width:1100px){.mms-downtime-page .filter-card .filter-bar{grid-template-columns:repeat(2,1fr)}}
@media(max-width:720px){.mms-downtime-page .filter-card .filter-bar{grid-template-columns:1fr}}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>