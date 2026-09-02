<template>
  <div class="app-container mms-process-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head"><div class="filter-title"><span class="glyph"></span> 筛选条件</div><a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced"><span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon></a></div>
      <div class="filter-bar">
        <div class="field"><label>工序编码</label><div class="control"><el-input v-model="queryParams.processCode" placeholder="请输入" clearable @keyup.enter="handleQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input></div></div>
        <div class="field"><label>工序名称</label><div class="control"><el-input v-model="queryParams.processName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>类型</label><div class="control is-select"><el-select v-model="queryParams.processType" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_process_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>产线</label><div class="control"><el-input v-model="queryParams.lineName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>状态</label><div class="control is-select"><el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>关键工序</label><div class="control is-select"><el-select v-model="queryParams.isKeyProcess" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>是否外协</label><div class="control is-select"><el-select v-model="queryParams.isOutsource" placeholder="全部" clearable @change="handleQuery"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field" v-show="showAdvanced"><label>创建日期</label><div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div></div>
      </div>
      <div class="filter-actions"><div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div><div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div></div>
    </div>
    <div class="surface">
      <div class="status-tabs"><div class="tabs-track"><button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span></button><button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="{ 'is-active': activeStatusTab === s.value }" @click="handleStatusTabClick(s.value)"><span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span></button></div><button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button></div>
      <div class="toolbar"><div class="left"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:process:add']">新增</el-button><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:process:edit']">修改</el-button><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:process:remove']">删除</el-button><div class="toolbar-divider"></div><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:process:export']">导出</el-button></div><div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_process_columns" /></div></div>
      <div class="table-wrap"><el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table"><el-table-column type="selection" width="55" align="center" /><el-table-column type="index" label="序号" width="85" align="center" /><el-table-column label="工序编码" prop="processCode" key="processCode" :width="colWidth('processCode', 130)" resizable v-if="columns.processCode.visible" /><el-table-column label="工序名称" prop="processName" key="processName" :width="colWidth('processName', 150)" resizable v-if="columns.processName.visible" /><el-table-column label="类型" prop="processType" key="processType" :width="colWidth('processType', 100)" resizable align="center" v-if="columns.processType.visible"><template #default="scope"><span v-if="scope.row.processType" class="badge blue"><span class="dot"></span>{{ dictLabel(mms_process_type, scope.row.processType) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="标准工时" prop="stdTime" key="stdTime" :width="colWidth('stdTime', 90)" resizable align="center" v-if="columns.stdTime.visible"><template #default="scope"><span>{{ scope.row.stdTime != null ? scope.row.stdTime + ' h' : '—' }}</span></template></el-table-column><el-table-column label="关键工序" prop="isKeyProcess" key="isKeyProcess" :width="colWidth('isKeyProcess', 90)" resizable align="center" v-if="columns.isKeyProcess.visible"><template #default="scope"><span v-if="scope.row.isKeyProcess" class="badge" :class="scope.row.isKeyProcess === '1' ? 'green' : 'gray'"><span class="dot"></span>{{ dictLabel(mms_yes_no, scope.row.isKeyProcess) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="是否外协" prop="isOutsource" key="isOutsource" :width="colWidth('isOutsource', 90)" resizable align="center" v-if="columns.isOutsource.visible"><template #default="scope"><span v-if="scope.row.isOutsource" class="badge" :class="scope.row.isOutsource === '1' ? 'amber' : 'gray'"><span class="dot"></span>{{ dictLabel(mms_yes_no, scope.row.isOutsource) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="产线" prop="lineName" key="lineName" :width="colWidth('lineName', 100)" resizable v-if="columns.lineName.visible"><template #default="scope"><span>{{ scope.row.lineName || '—' }}</span></template></el-table-column><el-table-column label="车间" prop="workshopName" key="workshopName" :width="colWidth('workshopName', 100)" resizable v-if="columns.workshopName.visible"><template #default="scope"><span>{{ scope.row.workshopName || '—' }}</span></template></el-table-column><el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span v-if="scope.row.status" class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column>          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:process:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:process:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['mms:process:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column></el-table></div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20M4 20V8l8-5 8 5v12M9 20v-6h6v6"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="工序编码" prop="processCode"><el-input v-model="form.processCode" placeholder="保存后自动生成" readonly><template #suffix><el-tag size="small" type="info" effect="plain">自动生成</el-tag></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="工序名称" prop="processName"><el-input v-model="form.processName" placeholder="请输入" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="类型" prop="processType"><el-select v-model="form.processType" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_process_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="状态" prop="status"><el-select v-model="form.status" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>工时与属性</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="标准工时" prop="stdTime"><el-input-number v-model="form.stdTime" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="准备时间" prop="prepTime"><el-input-number v-model="form.prepTime" :min="0" :precision="2" style="width: 100%" /></el-form-item></el-col></el-row>
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="关键工序" prop="isKeyProcess"><el-select v-model="form.isKeyProcess" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="是否外协" prop="isOutsource"><el-select v-model="form.isOutsource" placeholder="请选择" style="width: 100%"><el-option v-for="d in mms_yes_no" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18M5 21V7l8-4v18M19 21V11l-6-4"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20"><el-col :span="12"><el-form-item label="产线" prop="lineName"><el-input v-model="form.lineName" readonly placeholder="请选择产线" style="width: 100%" @click="openLinePicker"><template #append><el-button icon="Search" @click="openLinePicker" /></template><template #suffix><el-icon v-if="form.lineName" class="clear-icon" @click.stop="clearLine"><CircleClose /></el-icon></template></el-input></el-form-item></el-col><el-col :span="12"><el-form-item label="车间" prop="workshopName"><el-input v-model="form.workshopName" placeholder="选择产线后自动带出" readonly /></el-form-item></el-col></el-row>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 20h20M4 20V8l8-5 8 5v12M9 20v-6h6v6"/></svg></div>
          <span class="rd-detail-header-title">工序详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.processCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编码：{{ viewData.processCode }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工序编码</span><div class="rd-value">{{ viewData.processCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">工序名称</span><div class="rd-value">{{ viewData.processName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">类型</span><div class="rd-value"><dict-tag :options="mms_process_type" :value="viewData.processType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="mms_status" :value="viewData.status" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>工时与属性</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">标准工时</span><div class="rd-value">{{ viewData.stdTime != null ? viewData.stdTime + ' h' : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">准备时间</span><div class="rd-value">{{ viewData.prepTime != null ? viewData.prepTime + ' h' : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">关键工序</span><div class="rd-value"><dict-tag :options="mms_yes_no" :value="viewData.isKeyProcess" /></div></div>
              <div class="rd-item"><span class="rd-label">是否外协</span><div class="rd-value"><dict-tag :options="mms_yes_no" :value="viewData.isOutsource" /></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18M5 21V7l8-4v18M19 21V11l-6-4"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">产线</span><div class="rd-value">{{ viewData.lineName || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">车间</span><div class="rd-value">{{ viewData.workshopName || '—' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '—' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 产线选择弹窗 -->
    <line-picker ref="linePickerRef" title="选择产线" @confirm="onLinePickerConfirm" />

    <el-dialog v-model="showStatusHelp" title="工序管理业务操作说明" width="820px" append-to-body><div class="status-help-content"><h4>一、工序释义</h4><div class="highlight-card highlight-primary"><div class="highlight-card-title">什么是工序？</div><div class="highlight-card-body"><strong>工序（Process）</strong>是生产管控中定义产品加工步骤的基础主数据。每道工序包含标准工时、准备时间、是否关键工序、是否外协等属性，是构建工艺路线和计算产能的基本单元。<br/><br/>工序管理遵循 <strong>MES 标准化生产模型</strong>，通过标准工时（ST）支持产能规划和排产优化，关键工序标识支持质量管控点设置，外协工序标识支持委外加工管理。</div></div><h4>二、重点业务规则</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">核心规则</div><div class="highlight-card-body"><p>• <strong>标准工时：</strong>用于产能计算和排产参考的核心参数</p><p>• <strong>关键工序：</strong>标记关键工序，便于在质量管控中设置管控点</p><p>• <strong>外协管理：</strong>外协工序需关联外协供应商进行委外加工</p><p>• <strong>工序引用：</strong>工序被工艺路线引用后不可删除，需先解除引用</p></div></div><h4>三、新增/修改表单填写指南</h4><div class="highlight-card highlight-warning"><div class="highlight-card-title">基本信息区</div><div class="highlight-card-body"><p>• <strong>工序编码：</strong>工序的唯一标识编码，保存后由系统自动生成</p><p>• <strong>工序名称：</strong>工序的名称<span style="color: #f56c6c;">*必填</span></p><p>• <strong>类型：</strong>包括加工工序、检验工序、搬运工序等<span style="color: #f56c6c;">*必填</span></p></div></div><div class="highlight-card highlight-primary" style="margin-top: 12px;"><div class="highlight-card-title">工时与属性区</div><div class="highlight-card-body"><p>• <strong>标准工时：</strong>该工序的标准加工时间（分钟），用于产能计算<span style="color: #f56c6c;">*必填</span></p><p>• <strong>准备时间：</strong>工序开始前的准备时间（分钟）</p><p>• <strong>关键工序：</strong>标记是否为关键工序，关键工序需重点质量管控<span style="color: #f56c6c;">*必填</span></p><p>• <strong>是否外协：</strong>标记是否为外协工序，外协工序需关联外协供应商<span style="color: #f56c6c;">*必填</span></p></div></div><div class="highlight-card highlight-warning" style="margin-top: 12px;"><div class="highlight-card-title">其他信息区</div><div class="highlight-card-body"><p>• <strong>车间：</strong>工序所属的车间</p><p>• <strong>产线：</strong>工序所属的产线</p><p>• <strong>状态：</strong>工序的启用/停用状态<span style="color: #f56c6c;">*必填</span></p><p>• <strong>备注：</strong>工序的补充说明信息</p></div></div><h4>四、业务操作流程</h4><el-timeline><el-timeline-item type="primary" :hollow="true"><strong>创建工序：</strong>点击「新增」创建工序，填写工序名称、类型、标准工时等基本信息</el-timeline-item><el-timeline-item type="warning" :hollow="true"><strong>设置属性：</strong>标记是否为关键工序和是否外协，设置车间和产线归属</el-timeline-item><el-timeline-item type="success" :hollow="true"><strong>引用工序：</strong>在工艺路线管理中引用已创建的工序，构建产品加工路径</el-timeline-item></el-timeline></div><template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template></el-dialog>
  </div>
</template>

<script setup name="Process">
import { listProcess, getProcess, addProcess, updateProcess, delProcess } from "@/api/mms/process";
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import LinePicker from '@/components/LinePicker/index.vue'
import { Search, Filter, RefreshLeft, ArrowDown, WarningFilled, ArrowRight, QuestionFilled, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_process_type, mms_status, mms_yes_no } = proxy.useDict("mms_process_type", "mms_status", "mms_yes_no");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_process_index')
const { collapsedCards, toggleCard } = useDetailCard(['c0','c1','c2','vc0','vc1','vc2'])

const dataList = ref([]); const open = ref(false); const viewOpen = ref(false); const viewData = ref({}); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false);
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("");
const dateRange = ref([]); const activeStatusTab = ref("all"); const statusCounts = ref({}); const showStatusHelp = ref(false);
const linePickerRef = ref();

const statusTabList = computed(() => mms_status.value ? mms_status.value.map(d => ({ label: d.label, value: d.value })) : []);
const defaultColumns = { processCode: { label: '工序编码', visible: true }, processName: { label: '工序名称', visible: true }, processType: { label: '类型', visible: true }, stdTime: { label: '标准工时', visible: true }, isKeyProcess: { label: '关键工序', visible: true }, isOutsource: { label: '是否外协', visible: true }, lineName: { label: '产线', visible: true }, workshopName: { label: '车间', visible: true }, status: { label: '状态', visible: true }, createTime: { label: '创建时间', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('mms_process_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const activeFilterCount = computed(() => { let c = 0; if (queryParams.value.processCode) c++; if (queryParams.value.processName) c++; if (queryParams.value.processType) c++; if (queryParams.value.lineName) c++; if (queryParams.value.status) c++; if (queryParams.value.isKeyProcess) c++; if (queryParams.value.isOutsource) c++; if (dateRange.value && dateRange.value.length === 2) c++; return c; });

const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, processCode: undefined, processName: undefined, processType: undefined, lineName: undefined, status: undefined, isKeyProcess: undefined, isOutsource: undefined, params: {} }, rules: { processName: [{ required: true, message: "请输入工序名称", trigger: "blur" }] } });
const { queryParams, form, rules } = toRefs(data);

function getList() { loading.value = true; listProcess(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => { dataList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths(); loadStatusCounts(); }); }
function loadStatusCounts() { const baseQuery = { pageNum: 1, pageSize: 999 }; if (queryParams.value.processCode) baseQuery.processCode = queryParams.value.processCode; if (queryParams.value.processName) baseQuery.processName = queryParams.value.processName; if (queryParams.value.processType) baseQuery.processType = queryParams.value.processType; if (queryParams.value.lineName) baseQuery.lineName = queryParams.value.lineName; if (queryParams.value.isKeyProcess) baseQuery.isKeyProcess = queryParams.value.isKeyProcess; if (queryParams.value.isOutsource) baseQuery.isOutsource = queryParams.value.isOutsource; listProcess(proxy.addDateRange(baseQuery, dateRange.value)).then(res => { const counts = { all: res.total }; if (mms_status.value) { mms_status.value.forEach(d => { counts[d.value] = 0; }); (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++; }); } statusCounts.value = counts; }).catch(() => {}); }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList(); }
function resetQuery() { queryParams.value.processCode = undefined; queryParams.value.processName = undefined; queryParams.value.processType = undefined; queryParams.value.lineName = undefined; queryParams.value.status = undefined; queryParams.value.isKeyProcess = undefined; queryParams.value.isOutsource = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery(); }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === "all" ? undefined : status; handleQuery(); }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.processId); single.value = selection.length !== 1; multiple.value = !selection.length; }
function reset() { form.value = { processCode: undefined, processName: undefined, processType: undefined, stdTime: undefined, prepTime: undefined, isKeyProcess: undefined, isOutsource: undefined, workshopName: undefined, lineName: undefined, status: "0", remark: undefined }; proxy.resetForm("formRef"); }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = "新增工序"; }
function handleUpdate(row) { reset(); const id = row.processId || ids.value[0]; getProcess(id).then(response => { form.value = response.data; collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = "修改工序"; }); }
function submitForm() { proxy.$refs["formRef"].validate(valid => { if (valid) { if (form.value.processId != null) { updateProcess(form.value).then(() => { proxy.$modal.msgSuccess("修改成功"); open.value = false; getList(); }); } else { addProcess(form.value).then(() => { proxy.$modal.msgSuccess("新增成功"); open.value = false; getList(); }); } } }); }
function cancel() { open.value = false; reset(); }
function handleView(row) { const id = row.processId || ids.value[0]; getProcess(id).then(response => { viewData.value = response.data; collapsedCards.vc0 = false; collapsedCards.vc1 = false; collapsedCards.vc2 = false; viewOpen.value = true; }); }
function handleDelete(row) { const delIds = row.processId || ids.value; proxy.$modal.confirm('是否确认删除选中的工序？').then(() => delProcess(delIds)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功"); }).catch(() => {}); }
function handleExport() { proxy.download("mms/process/export", { ...queryParams.value }, `process_${new Date().getTime()}.xlsx`); }
function dictLabel(dictRef, value) { if (value === null || value === undefined || value === '') return '—'; const arr = (dictRef && dictRef.value) ? dictRef.value : dictRef; if (!arr || !Array.isArray(arr)) return '—'; const item = arr.find(d => d.value == value); return item ? item.label : '—'; }
function statusLabel(status) { return dictLabel(mms_status, status); }
function badgeClass(status) { const map = { '0': 'green', '1': 'gray' }; return map[status] || 'gray'; }

// 产线选择弹窗
function openLinePicker() {
  linePickerRef.value?.open(form.value.lineName)
}
function onLinePickerConfirm(data) {
  form.value.lineName = data.lineName;
  form.value.workshopName = data.workshopName;
}
function clearLine() {
  form.value.lineName = undefined;
  form.value.workshopName = undefined;
}

getList();
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.mms-process-page{padding-top:10px;--brand-50:#eef2ff;--brand-100:#e0e7ff;--brand-200:#c7d2fe;--brand-500:#6366f1;--brand-600:#4f46e5;--brand-700:#4338ca;--ink-900:#0f172a;--ink-700:#334155;--ink-500:#64748b;--ink-400:#94a3b8;--ink-300:#cbd5e1;--ink-200:#e2e8f0;--ink-100:#f1f5f9;--ink-50:#f8fafc;--amber-50:#fffbeb;--amber-500:#f59e0b;--amber-700:#b45309;--blue-50:#eff6ff;--blue-500:#3b82f6;--blue-700:#1d4ed8;--green-50:#ecfdf5;--green-500:#10b981;--green-700:#047857;--red-50:#fef2f2;--red-500:#ef4444;--red-700:#b91c1c;--r-sm:6px;--r-md:10px;--r-lg:14px;--shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04);--ease-out:cubic-bezier(.16,.84,.44,1);font-feature-settings:"tnum" 1;color:var(--ink-900)}
.mms-process-page .clear-icon{cursor:pointer;color:#c0c4cc;font-size:14px}
.mms-process-page .clear-icon:hover{color:#909399}

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
