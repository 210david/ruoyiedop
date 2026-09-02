<template>
  <div class="app-container qms-seval-page">
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
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>周期</label>
          <div class="control">
            <el-date-picker v-model="queryParams.evalPeriod" type="month" placeholder="选择周期" value-format="YYYY-MM" clearable style="width: 100%" @change="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.grade" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in gradeOptions" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>备注</label>
          <div class="control"><el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
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
        <div class="tabs-track"></div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:supplierEval:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:supplierEval:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:supplierEval:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:supplierEval:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_supplierEval_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
<el-table-column type="selection" width="55" align="center" />
<el-table-column type="index" label="序号" width="85" align="center" />
<el-table-column label="供应商名称" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />          <el-table-column label="评价周期" prop="evalPeriod" key="evalPeriod" :width="colWidth('evalPeriod', 110)" resizable align="center" v-if="columns.evalPeriod.visible" />
          <el-table-column label="来料批次" prop="incomingTotal" key="incomingTotal" :width="colWidth('incomingTotal', 110)" resizable align="center" v-if="columns.incomingTotal.visible">
            <template #header><span>来料批次</span><el-tooltip content="自动统计该供应商在评价周期内的IQC来料检验任务总数（已判定状态）" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="合格批次" prop="incomingPass" key="incomingPass" :width="colWidth('incomingPass', 110)" resizable align="center" v-if="columns.incomingPass.visible">
            <template #header><span>合格批次</span><el-tooltip content="自动统计该供应商在评价周期内IQC检验结果为「合格」的批次数" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="合格率%" prop="passRate" key="passRate" :width="colWidth('passRate', 110)" resizable align="center" v-if="columns.passRate.visible">
            <template #header><span>合格率%</span><el-tooltip content="系统自动计算：合格批次 ÷ 来料批次 × 100%" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="PPM" prop="ppm" key="ppm" :width="colWidth('ppm', 100)" resizable align="center" v-if="columns.ppm.visible">
            <template #header><span>PPM</span><el-tooltip content="系统自动计算：(来料批次 - 合格批次) ÷ 来料批次 × 1,000,000，即百万分之不良数" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="质量分" prop="qualityScore" key="qualityScore" :width="colWidth('qualityScore', 100)" resizable align="center" v-if="columns.qualityScore.visible">
            <template #header><span>质量分</span><el-tooltip content="人工评分(0-100)，依据来料合格率、PPM及缺陷严重程度综合评定" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="交期分" prop="deliveryScore" key="deliveryScore" :width="colWidth('deliveryScore', 100)" resizable align="center" v-if="columns.deliveryScore.visible">
            <template #header><span>交期分</span><el-tooltip content="人工评分(0-100)，依据按时交货率评定" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="服务分" prop="serviceScore" key="serviceScore" :width="colWidth('serviceScore', 100)" resizable align="center" v-if="columns.serviceScore.visible">
            <template #header><span>服务分</span><el-tooltip content="人工评分(0-100)，依据响应速度、配合度及售后服务评定" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="综合分" prop="totalScore" key="totalScore" :width="colWidth('totalScore', 100)" resizable align="center" v-if="columns.totalScore.visible">
            <template #header><span>综合分</span><el-tooltip content="系统自动计算：质量分×50% + 交期分×30% + 服务分×20%" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="等级" prop="grade" key="grade" :width="colWidth('grade', 90)" resizable align="center" v-if="columns.grade.visible">
            <template #header><span>等级</span><el-tooltip content="系统自动评级：A(≥90分) B(80-89分) C(70-79分) D(小于70分)" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge blue"><span class="dot"></span>{{ gradeLabel(scope.row.grade) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:supplierEval:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:supplierEval:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="1030px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">供应商评价详情</span>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供应商名称</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评价周期</span><div class="rd-value">{{ viewData.evalPeriod || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评价类型</span><div class="rd-value">{{ viewData.evalType === 'monthly' ? '月度' : viewData.evalType === 'quarterly' ? '季度' : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_batch')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>来料批次统计</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_batch }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_batch" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">来料批次<el-tooltip content="IQC（来料质量控制）检验的物料批次总数" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.incomingTotal != null ? viewData.incomingTotal : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">合格批次<el-tooltip content="IQC检验结果为「合格」的来料批次数" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.incomingPass != null ? viewData.incomingPass : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">合格率(%)<el-tooltip content="合格率 = 合格批次 ÷ 来料批次 × 100%" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.passRate != null ? viewData.passRate + '%' : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">PPM<el-tooltip content="PPM（Parts Per Million）百万分之缺陷率，PPM值越低表示供应商质量越好" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.ppm != null ? viewData.ppm : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_score')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_score }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_score" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">质量分<el-tooltip content="依据来料合格率、PPM值及缺陷严重程度综合评定的质量维度评分(0-100)" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.qualityScore != null ? viewData.qualityScore : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">交期分<el-tooltip content="依据供应商按时交货率评定的交期维度评分(0-100)" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.deliveryScore != null ? viewData.deliveryScore : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">服务分<el-tooltip content="依据供应商响应速度、配合度及售后服务质量评定的服务维度评分(0-100)" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.serviceScore != null ? viewData.serviceScore : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">综合评分<el-tooltip content="系统自动加权计算：质量分×50% + 交期分×30% + 服务分×20%" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.totalScore != null ? viewData.totalScore : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">等级<el-tooltip content="系统根据综合评分自动评级：A(≥90分)、B(80-89分)、C(70-79分)、D(<70分)" placement="top"><el-icon class="rd-label-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><span class="badge blue"><span class="dot"></span>{{ gradeLabel(viewData.grade) }}</span></div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_other" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="1030px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="evalRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-input v-model="form.supplierName" readonly placeholder="请选择供应商" style="width: 100%" @click="onSupplierFieldClick"><template v-if="form.supplierName" #append><el-button icon="CircleClose" @click.stop="clearSupplier" /></template><template v-else #append><el-button icon="Search" @click="openSupplierPicker" /></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="evalPeriod"><template #label><span>评价周期</span><el-tooltip content="选择需要评价的月份，系统将根据该周期自动统计IQC来料检验数据" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-date-picker v-model="form.evalPeriod" type="month" placeholder="请选择评价周期" value-format="YYYY-MM" style="width: 100%" @change="fetchBatchStats" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12"><el-form-item><template #label><span>评价类型</span><el-tooltip content="评价的时间粒度：月度（按自然月统计）或季度（按自然季度Q1-Q4统计）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.evalType" style="width: 100%"><el-option label="月度" value="monthly" /><el-option label="季度" value="quarterly" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_batch')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>来料批次统计</div>
              <div class="rd-card-header-actions"><el-button type="primary" link icon="Refresh" :loading="batchLoading" @click.stop="fetchBatchStats" :disabled="!form.supplierId || !form.evalPeriod">刷新统计</el-button><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_batch }" aria-label="折叠" @click.stop><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.e_batch">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
                <template #title>来料批次和合格批次由系统自动从IQC（Incoming Quality Control，来料质量控制）检验记录中统计，无需手工输入。选择供应商和评价周期后点击"刷新统计"即可自动获取。</template>
              </el-alert>
              <el-row :gutter="24">
                <el-col :span="8"><el-form-item><template #label><span>来料批次</span><el-tooltip content="IQC（来料质量控制）检验的物料批次总数。系统自动从IQC来料检验记录中统计，无需手工输入" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.incomingTotal" readonly class="readonly-field" placeholder="自动获取"><template #suffix><span class="auto-tag">自动</span></template></el-input></el-form-item></el-col>
                <el-col :span="8"><el-form-item><template #label><span>合格批次</span><el-tooltip content="IQC检验结果为「合格」的来料批次数。系统自动统计，无需手工输入" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.incomingPass" readonly class="readonly-field" placeholder="自动获取"><template #suffix><span class="auto-tag">自动</span></template></el-input></el-form-item></el-col>
                <el-col :span="8"><el-form-item><template #label><span>合格率(%)</span><el-tooltip content="合格率 = 合格批次 ÷ 来料批次 × 100%，反映供应商来料质量水平，保留两位小数" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input :model-value="form.passRate != null ? form.passRate + '%' : '-'" readonly class="readonly-field" placeholder="自动计算"><template #suffix><span class="auto-tag">计算</span></template></el-input></el-form-item></el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="8"><el-form-item><template #label><span>PPM</span><el-tooltip content="PPM（Parts Per Million，百万分之缺陷率）= (来料批次 - 合格批次) ÷ 来料批次 × 1,000,000。PPM值越低表示供应商质量越好" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input :model-value="form.ppm != null ? form.ppm : '-'" readonly class="readonly-field" placeholder="自动计算"><template #suffix><span class="auto-tag">计算</span></template></el-input></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_score')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_score }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_score">
              <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
                <template #title>评分填写说明</template>
                <template #default>
                  <div class="score-guide">
                    <p><b>质量分(0-100)</b>：依据来料合格率、PPM值及缺陷严重程度综合评定。合格率越高、缺陷越少得分越高。</p>
                    <p><b>交期分(0-100)</b>：依据按时交货率评定。按约定日期到货比例越高得分越高。</p>
                    <p><b>服务分(0-100)</b>：依据响应速度、配合度及售后服务质量评定。</p>
                    <p><b>综合评分</b>：系统自动计算 = 质量分×50% + 交期分×30% + 服务分×20%。</p>
                    <p><b>等级</b>：系统自动评级，A(≥90分)、B(80-89分)、C(70-79分)、D(小于70分)。</p>
                  </div>
                </template>
              </el-alert>
              <el-row :gutter="24">
                <el-col :span="8"><el-form-item><template #label><span>质量分</span><el-tooltip content="质量维度评分(0-100)，依据来料合格率、PPM值及缺陷严重程度综合评定。合格率越高、缺陷越少得分越高" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.qualityScore" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item><template #label><span>交期分</span><el-tooltip content="交期维度评分(0-100)，依据供应商按时交货率评定。按约定日期到货比例越高得分越高" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.deliveryScore" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item><template #label><span>服务分</span><el-tooltip content="服务维度评分(0-100)，依据供应商响应速度、配合度及售后服务质量评定" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input-number v-model="form.serviceScore" :min="0" :max="100" :precision="1" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="8"><el-form-item><template #label><span>综合评分</span><el-tooltip content="系统自动加权计算：质量分×50% + 交期分×30% + 服务分×20%，反映供应商综合表现" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input :model-value="form.totalScore != null ? form.totalScore : '-'" readonly class="readonly-field" placeholder="自动计算"><template #suffix><span class="auto-tag">计算</span></template></el-input></el-form-item></el-col>
                <el-col :span="8"><el-form-item><template #label><span>等级</span><el-tooltip content="系统根据综合评分自动评级：A(≥90分)优秀、B(80-89分)合格、C(70-79分)待改进、D(<70分)不合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input :model-value="gradeLabel(form.grade)" readonly class="readonly-field" placeholder="自动评级"><template #suffix><span class="auto-tag">自动</span></template></el-input></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- 供应商选择器 -->
    <supplier-picker ref="supplierPickerRef" title="选择供应商" @confirm="onSupplierPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="供应商质量评价业务操作说明" width="760px" append-to-body>
      <div class="status-help-content">
        <h4>一、来料批次统计规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">自动统计来料数据</div>
              <div class="highlight-card-body">选择供应商和评价周期后，系统自动从<strong>IQC来料检验记录</strong>中统计来料批次和合格批次，无需手工输入</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">合格率自动计算</div>
              <div class="highlight-card-body">合格率 = <strong>合格批次 ÷ 来料批次 × 100%</strong>，保留两位小数</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">PPM自动计算</div>
              <div class="highlight-card-body">PPM = <strong>(来料批次 - 合格批次) ÷ 来料批次 × 1,000,000</strong>，即百万分之不良数</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">刷新统计</div>
              <div class="highlight-card-body">在新增/修改页面点击「刷新统计」按钮可重新获取最新来料数据，切换供应商或周期也会自动刷新</div>
            </div>
          </el-col>
        </el-row>

        <h4>二、评分规则</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="质量分(0-100)"><strong>人工评分</strong>，依据来料合格率、PPM值及缺陷严重程度综合评定。合格率越高、缺陷越少得分越高</el-descriptions-item>
          <el-descriptions-item label="交期分(0-100)"><strong>人工评分</strong>，依据按时交货率评定。按约定日期到货比例越高得分越高</el-descriptions-item>
          <el-descriptions-item label="服务分(0-100)"><strong>人工评分</strong>，依据响应速度、配合度及售后服务质量评定</el-descriptions-item>
          <el-descriptions-item label="综合评分"><strong>系统自动计算</strong> = 质量分 × 50% + 交期分 × 30% + 服务分 × 20%</el-descriptions-item>
        </el-descriptions>

        <h4>三、等级评定规则</h4>
        <div class="grade-rule-table">
          <div class="grade-rule-row grade-a"><span class="grade-badge">A</span><span class="grade-range">综合评分 ≥ 90分</span><span class="grade-desc">优秀供应商，可优先合作</span></div>
          <div class="grade-rule-row grade-b"><span class="grade-badge">B</span><span class="grade-range">80分 ≤ 综合评分 < 90分</span><span class="grade-desc">合格供应商，正常合作</span></div>
          <div class="grade-rule-row grade-c"><span class="grade-badge">C</span><span class="grade-range">70分 ≤ 综合评分 < 80分</span><span class="grade-desc">待改进供应商，需关注并要求改善</span></div>
          <div class="grade-rule-row grade-d"><span class="grade-badge">D</span><span class="grade-range">综合评分 < 70分</span><span class="grade-desc">不合格供应商，需限制或淘汰</span></div>
        </div>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新增评价：</strong>选择供应商、评价周期和评价类型（月度/季度）
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>自动统计来料数据：</strong>系统自动从IQC检验记录中获取来料批次和合格批次，并计算合格率和PPM
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>填写评分：</strong>根据供应商表现填写质量分、交期分、服务分（0-100分）
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>自动计算综合评分和等级：</strong>系统自动计算综合评分（质量分×50% + 交期分×30% + 服务分×20%）并评定A/B/C/D等级
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>保存评价结果：</strong>保存后可在列表中查看评价记录，支持按供应商、周期、等级等条件筛选和导出</el-timeline-item>
        </el-timeline>

        <h4>五、专业词汇说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="IQC"><strong>Incoming Quality Control（来料质量控制）</strong>，指对供应商交付的原材料、零部件进行入库前的质量检验，是供应商质量管控的重要环节</el-descriptions-item>
          <el-descriptions-item label="PPM"><strong>Parts Per Million（百万分之缺陷率）</strong>，表示每百万个产品中的不良品数量。PPM值越低，说明供应商产品质量越好。计算公式：(来料批次 - 合格批次) ÷ 来料批次 × 1,000,000</el-descriptions-item>
          <el-descriptions-item label="合格率">合格批次占来料总批次的百分比，计算公式：合格批次 ÷ 来料批次 × 100%，反映供应商来料质量的整体水平</el-descriptions-item>
          <el-descriptions-item label="来料批次">供应商在评价周期内交付的、经过IQC来料检验的物料批次总数，由系统自动统计</el-descriptions-item>
          <el-descriptions-item label="质量分">质量维度评分(0-100分)，依据来料合格率、PPM值及缺陷严重程度综合评定，在综合评分中权重为50%</el-descriptions-item>
          <el-descriptions-item label="交期分">交期维度评分(0-100分)，依据供应商按时交货率评定，在综合评分中权重为30%</el-descriptions-item>
          <el-descriptions-item label="服务分">服务维度评分(0-100分)，依据供应商响应速度、配合度及售后服务质量评定，在综合评分中权重为20%</el-descriptions-item>
          <el-descriptions-item label="综合评分">质量分、交期分、服务分的加权综合分，计算公式：质量分×50% + 交期分×30% + 服务分×20%</el-descriptions-item>
          <el-descriptions-item label="等级">根据综合评分自动评定的供应商质量等级：A(≥90分)优秀、B(80-89分)合格、C(70-79分)待改进、D(<70分)不合格</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsSupplierEval">
import { listSupplierEval, getSupplierEval, addSupplierEval, updateSupplierEval, delSupplierEval, getBatchStats } from '@/api/qms/supplierEval'
import SupplierPicker from '@/components/SupplierPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_supplierEval_index')
const { collapsedCards, toggleCard } = useDetailCard(['v_basic', 'v_batch', 'v_score', 'v_other', 'e_basic', 'e_batch', 'e_score', 'e_other'])
const { proxy } = getCurrentInstance()
const { qms_supplier_grade: gradeOptions } = proxy.useDict('qms_supplier_grade')

const supplierPickerRef = ref()
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const total = ref(0)
const open = ref(false)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const viewOpen = ref(false)
const viewData = ref(null)
const batchLoading = ref(false)
const showStatusHelp = ref(false)
const defaultColumns = { supplierName: { label: '供应商名称', visible: true }, evalPeriod: { label: '评价周期', visible: true }, incomingTotal: { label: '来料批次', visible: true }, incomingPass: { label: '合格批次', visible: true }, passRate: { label: '合格率%', visible: true }, ppm: { label: 'PPM', visible: true }, qualityScore: { label: '质量分', visible: true }, deliveryScore: { label: '交期分', visible: true }, serviceScore: { label: '服务分', visible: true }, totalScore: { label: '综合分', visible: true }, grade: { label: '等级', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_supplierEval_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, supplierName: undefined, evalPeriod: undefined, grade: undefined, remark: undefined, params: {} } })
const { queryParams, form } = toRefs(data)
const rules = { supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }], evalPeriod: [{ required: true, message: '请选择评价周期', trigger: 'change' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierName) count++
  if (queryParams.value.evalPeriod) count++
  if (queryParams.value.grade) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function onSupplierFieldClick(e) {
  // 点击的是右侧清空按钮时不打开弹窗（el-button 的 .stop 无法阻止原生冒泡）
  if (e.target && e.target.closest && e.target.closest('.el-input-group__append')) return
  openSupplierPicker()
}
function openSupplierPicker() { supplierPickerRef.value.open(form.value.supplierId) }
function onSupplierPickerConfirm(supplier) { form.value.supplierId = supplier.supplierId; form.value.supplierName = supplier.supplierName; fetchBatchStats() }
function clearSupplier() { form.value.supplierId = undefined; form.value.supplierName = undefined; fetchBatchStats() }
function fetchBatchStats() {
  if (!form.value.supplierId || !form.value.evalPeriod) return
  batchLoading.value = true
  getBatchStats(form.value.supplierId, form.value.evalPeriod).then(res => {
    const stats = res.data || {}
    form.value.incomingTotal = stats.incomingTotal || 0
    form.value.incomingPass = stats.incomingPass || 0
    // 前端预览计算结果（最终以后台 calcScore 为准）
    if (form.value.incomingTotal > 0) {
      const pass = Number(form.value.incomingPass)
      const total = Number(form.value.incomingTotal)
      form.value.passRate = Math.round(pass * 100 / total * 100) / 100
      form.value.ppm = Math.round((total - pass) * 1000000 / total * 100) / 100
    } else {
      form.value.passRate = null
      form.value.ppm = null
    }
  }).finally(() => { batchLoading.value = false })
}
function getList() { loading.value = true; listSupplierEval(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.supplierName = undefined; queryParams.value.evalPeriod = undefined; queryParams.value.grade = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增供应商评价' }
function handleUpdate(row) {
  const id = row?.evalId || selectedId.value
  getSupplierEval(id).then(res => { data.form = res.data; open.value = true; title.value = '修改供应商评价' })
}
function handleView(row) { getSupplierEval(row.evalId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.evalId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['evalRef'].validate(valid => {
    if (valid) {
      if (form.value.evalId) { updateSupplierEval(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addSupplierEval(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.evalId ? [row.evalId] : selectedIds.value
  proxy.$modal.confirm('确认删除选中的数据？').then(() => delSupplierEval(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/supplierEval/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `supplierEval_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { evalId: null, supplierId: undefined, supplierName: undefined, evalPeriod: undefined, evalType: 'monthly', incomingTotal: 0, incomingPass: 0, passRate: null, ppm: null, qualityScore: undefined, deliveryScore: undefined, serviceScore: undefined, totalScore: null, grade: null }; proxy.resetForm('evalRef') }
function cancel() { open.value = false; reset() }
// 监听三项评分变化，前端预览综合评分和等级（最终以后台 calcScore 为准）
watch(() => [form.value.qualityScore, form.value.deliveryScore, form.value.serviceScore], ([q, d, s]) => {
  if (q != null && d != null && s != null) {
    const total = Math.round((q * 0.5 + d * 0.3 + s * 0.2) * 100) / 100
    form.value.totalScore = total
    form.value.grade = total >= 90 ? 'A' : total >= 80 ? 'B' : total >= 70 ? 'C' : 'D'
  } else {
    form.value.totalScore = null
    form.value.grade = null
  }
}, { deep: true })
function gradeLabel(val) { const item = gradeOptions.value.find(d => d.value == val); return item ? item.label : '-' }
getList()
</script>

<style scoped>
.qms-seval-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-seval-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-seval-page .filter-card { padding:14px 20px 16px; }
.qms-seval-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-seval-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-seval-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-seval-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-seval-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-seval-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-seval-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-seval-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-seval-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-seval-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-seval-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-seval-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-seval-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-seval-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-seval-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-seval-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-seval-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-seval-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-seval-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-seval-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-seval-page .field .control :deep(.el-select) { width:100%; }
.qms-seval-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-seval-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-seval-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-seval-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-seval-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-seval-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-seval-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-seval-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-seval-page .btn-soft .el-icon { font-size:14px; }
.qms-seval-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-seval-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-seval-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-seval-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-seval-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-seval-page .table-wrap { overflow-x:auto; }
.qms-seval-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-seval-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-seval-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-seval-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-seval-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-seval-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-seval-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-seval-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-seval-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-seval-page .badge.blue .dot { background:var(--brand-500); }
.qms-seval-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-seval-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-seval-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-seval-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-seval-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.qms-seval-page .field .control :deep(.el-date-editor.el-input) { width:100%; }
.qms-seval-page .field .control :deep(.el-date-editor.el-input .el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-seval-page .field .control :deep(.el-date-editor.el-input .el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-seval-page .field .control :deep(.el-date-editor.el-input .el-input__prefix) { color:var(--ink-400); }
@media (max-width:1100px) { .qms-seval-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-seval-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-seval-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* table header tooltip icon */
.qms-seval-page .col-tip { margin-left:4px; font-size:14px; color:var(--ink-400); cursor:help; vertical-align:middle; }
.qms-seval-page .col-tip:hover { color:var(--brand-500); }
/* form label tooltip icon */
.qms-seval-page .rd-form-tip { margin-left:4px; color:var(--ink-400); font-size:14px; cursor:help; vertical-align:middle; }
.qms-seval-page .rd-form-tip:hover { color:var(--brand-500); }
/* view dialog label tooltip icon */
.qms-seval-page .rd-label-tip { margin-left:4px; font-size:13px; color:var(--ink-400); cursor:help; vertical-align:middle; }
.qms-seval-page .rd-label-tip:hover { color:var(--brand-500); }
/* dialog card header with action button */
.qms-seval-page .rd-dialog .rd-card-header { display:flex; align-items:center; justify-content:space-between; }
/* read-only field */
.qms-seval-page .readonly-field :deep(.el-input__wrapper) { background:var(--ink-50); box-shadow:0 0 0 1px var(--ink-200) inset !important; cursor:default; }
.qms-seval-page .readonly-field :deep(.el-input__inner) { color:var(--ink-700); -webkit-text-fill-color:var(--ink-700); cursor:default; }
/* auto/calculated tag suffix */
.qms-seval-page .auto-tag { display:inline-flex; align-items:center; height:18px; padding:0 6px; font-size:11px; font-weight:600; color:var(--brand-700); background:var(--brand-50); border:1px solid var(--brand-200); border-radius:4px; white-space:nowrap; }
/* score guide text */
.qms-seval-page .score-guide p { margin:4px 0; font-size:13px; line-height:1.6; color:var(--ink-700); }
.qms-seval-page .score-guide b { color:var(--ink-900); }
/* ===== Status Tabs + Tip Pill ===== */
.qms-seval-page .status-tabs { display:flex; align-items:center; justify-content:space-between; padding:10px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-seval-page .status-tabs .tabs-track { display:flex; align-items:center; gap:8px; }
.qms-seval-page .status-tabs .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-seval-page .status-tabs .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }

/* ===== Dialog Detail Page Styles ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: 100% !important; }

.rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -30px; right: -15px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 10px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }

.rd-page { max-width: 100%; margin: 0 auto; }
.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-header-actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.rd-card-header .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; border-radius: 6px; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-card-header .rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-card-header .rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-card-header .rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }
.rd-card-body { padding: 14px 16px; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.rd-form-tip:hover { color: #909399; }
.rd-label-tip { margin-left: 4px; font-size: 13px; color: #c0c4cc; cursor: help; vertical-align: middle; }

@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }
.rd-card:nth-child(5) { animation-delay: 0.24s; }
.rd-card:nth-child(6) { animation-delay: 0.30s; }

@media (max-width:768px) { .rd-grid { grid-template-columns: 1fr; } }

/* ===== 业务操作说明对话框 ===== */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-info { background-color: #f4f4f5; border-color: #d3d4d6; }
.status-help-content .highlight-info .highlight-card-title { color: #909399; }
/* 等级评定规则表 */
.status-help-content .grade-rule-table { border: 1px solid #e4e7ed; border-radius: 8px; overflow: hidden; }
.status-help-content .grade-rule-row { display: flex; align-items: center; gap: 16px; padding: 12px 16px; border-bottom: 1px solid #e4e7ed; }
.status-help-content .grade-rule-row:last-child { border-bottom: none; }
.status-help-content .grade-badge { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 50%; font-size: 16px; font-weight: 700; flex-shrink: 0; }
.status-help-content .grade-range { font-size: 14px; font-weight: 600; color: #303133; min-width: 200px; }
.status-help-content .grade-desc { font-size: 13px; color: #606266; }
.status-help-content .grade-a { background: #f0f9eb; }
.status-help-content .grade-a .grade-badge { background: #67c23a; color: #fff; }
.status-help-content .grade-b { background: #ecf5ff; }
.status-help-content .grade-b .grade-badge { background: #409eff; color: #fff; }
.status-help-content .grade-c { background: #fdf6ec; }
.status-help-content .grade-c .grade-badge { background: #e6a23c; color: #fff; }
.status-help-content .grade-d { background: #fef0f0; }
.status-help-content .grade-d .grade-badge { background: #f56c6c; color: #fff; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
