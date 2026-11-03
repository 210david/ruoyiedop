<template>
  <div class="app-container hr-kpi-page">
    <el-tabs v-model="activeTab" class="kpi-tabs">
      <el-tab-pane label="考核方案" name="scheme" />
      <el-tab-pane label="考核记录" name="record" />
    </el-tabs>

    <!-- ===== 方案 Pane ===== -->
    <div v-show="activeTab === 'scheme'">
      <div class="surface">
        <div class="toolbar">
          <div class="left">
            <el-button type="primary" plain icon="Plus" @click="handleAddScheme" v-hasPermi="['hr:kpi:add']">新增方案</el-button>
          </div>
          <div class="right">
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getSchemeList" :columns="schemeColumns" storageKey="hr_kpi_scheme_columns" />
          </div>
        </div>
        <div class="table-wrap">
          <el-table ref="schemeTableRef" border v-loading="loading" :data="schemeList" @header-dragend="onSchemeHeaderDragEnd" class="app-table">
            <el-table-column type="index" label="序号" key="序号" :width="colWidthScheme('序号', 70)" align="center" resizable />
            <el-table-column label="方案名称" prop="schemeName" key="schemeName" :min-width="colWidthScheme('schemeName', 200)" resizable show-overflow-tooltip v-if="schemeColumns.schemeName.visible" />
            <el-table-column label="考核周期" prop="periodType" key="periodType" :width="colWidthScheme('periodType', 110)" resizable align="center" v-if="schemeColumns.periodType.visible"><template #default="scope"><span v-if="dictLabel(periodTypeOptions, scope.row.periodType)" class="badge blue"><span class="dot"></span>{{ dictLabel(periodTypeOptions, scope.row.periodType) }}</span></template></el-table-column>
            <el-table-column label="状态" prop="schemeStatus" key="schemeStatus" :width="colWidthScheme('schemeStatus', 100)" resizable align="center" v-if="schemeColumns.schemeStatus.visible"><template #default="scope"><span class="badge" :class="schemeStatusClass(scope.row.schemeStatus)"><span class="dot"></span>{{ schemeStatusLabel(scope.row.schemeStatus) }}</span></template></el-table-column>
            <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidthScheme('createTime', 160)" resizable align="center" v-if="schemeColumns.createTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template></el-table-column>
            <el-table-column label="操作" :width="colWidthScheme('操作', 300)" align="center" fixed="right" class-name="col-action">
              <template #default="scope">
                <div class="action-btn-row">
                  <el-button link type="primary" icon="Edit" @click="handleEditScheme(scope.row)" v-hasPermi="['hr:kpi:add']">编辑</el-button>
                  <el-button link :type="scope.row.schemeStatus === '1' ? 'warning' : 'success'" @click="toggleScheme(scope.row)" v-hasPermi="['hr:kpi:add']">{{ scope.row.schemeStatus === '1' ? '停用' : '启用' }}</el-button>
                  <el-button link type="success" icon="Position" @click="handleInitiate(scope.row)" v-hasPermi="['hr:kpi:initiate']">发起考核</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelScheme(scope.row)" v-hasPermi="['hr:kpi:add']">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
    <!-- ===== 记录 Pane ===== -->
    <div v-show="activeTab === 'record'">
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
            <label>考核周期</label>
            <div class="control is-select">
              <el-date-picker v-model="recordQuery.period" type="month" value-format="YYYY-MM" placeholder="选择月份" clearable style="width: 100%" @change="getRecordList" />
            </div>
          </div>
          <div class="field">
            <label>员工姓名</label>
            <div class="control">
              <el-input v-model="recordQuery.employeeName" placeholder="请输入" clearable @keyup.enter="getRecordList" />
            </div>
          </div>
        </div>
        <div class="filter-actions">
          <div class="filter-info">
            <el-icon><Filter /></el-icon> 已选 {{ activeRecordFilterCount }} 个条件
            <button class="tip-pill" style="margin-left:8px" @click="showStatusHelp = true">
              <el-icon><QuestionFilled /></el-icon>
              <span>考核流程说明</span>
            </button>
          </div>
          <div class="filter-buttons">
            <el-button icon="RefreshLeft" @click="resetRecordQuery">重置</el-button>
            <el-button type="primary" icon="Search" @click="getRecordList">搜索</el-button>
          </div>
        </div>
      </div>
      <div class="surface">
        <div class="toolbar">
          <div class="left">
            <el-button type="primary" plain icon="Refresh" @click="getRecordList">刷新</el-button>
          </div>
          <div class="right">
            <right-toolbar v-model:showSearch="showSearch" @queryTable="getRecordList" :columns="recordColumns" storageKey="hr_kpi_record_columns" />
          </div>
        </div>
        <div class="table-wrap">
          <el-table ref="recordTableRef" border v-loading="loading" :data="recordList" @header-dragend="onRecordHeaderDragEnd" class="app-table">
            <el-table-column type="index" label="序号" key="序号" :width="colWidthRecord('序号', 70)" align="center" resizable />
            <el-table-column label="考核方案" prop="schemeName" key="schemeName" :min-width="colWidthRecord('schemeName', 170)" resizable show-overflow-tooltip v-if="recordColumns.schemeName.visible" />
            <el-table-column label="周期" prop="period" key="period" :width="colWidthRecord('period', 90)" resizable align="center" v-if="recordColumns.period.visible" />
            <el-table-column label="员工" prop="employeeName" key="employeeName" :width="colWidthRecord('employeeName', 110)" resizable v-if="recordColumns.employeeName.visible" />
            <el-table-column label="自评分" prop="selfScore" key="selfScore" :width="colWidthRecord('selfScore', 90)" resizable align="center" v-if="recordColumns.selfScore.visible" />
            <el-table-column label="上级评分" prop="superiorScore" key="superiorScore" :width="colWidthRecord('superiorScore', 100)" resizable align="center" v-if="recordColumns.superiorScore.visible" />
            <el-table-column label="最终得分" prop="finalScore" key="finalScore" :width="colWidthRecord('finalScore', 100)" resizable align="center" v-if="recordColumns.finalScore.visible" />
            <el-table-column label="状态" prop="kpiStatus" key="kpiStatus" :width="colWidthRecord('kpiStatus', 100)" resizable align="center" v-if="recordColumns.kpiStatus.visible"><template #default="scope"><span class="badge" :class="kpiStatusClass(scope.row.kpiStatus)"><span class="dot"></span>{{ kpiStatusLabel(scope.row.kpiStatus) }}</span></template></el-table-column>
            <el-table-column label="操作" :width="colWidthRecord('操作', 320)" align="center" fixed="right" class-name="col-action">
              <template #default="scope">
                <div class="action-btn-row">
                  <el-button link type="primary" @click="openScore(scope.row, 'self')" v-hasPermi="['hr:kpi:score']" v-if="scope.row.kpiStatus === '0'">自评</el-button>
                  <el-button link type="primary" @click="openScore(scope.row, 'superior')" v-hasPermi="['hr:kpi:score']" v-if="scope.row.kpiStatus === '1'">评分</el-button>
                  <el-button link type="success" @click="handleConfirm(scope.row)" v-hasPermi="['hr:kpi:confirm']" v-if="scope.row.kpiStatus === '2'">确认</el-button>
                  <el-button link type="warning" @click="openAppeal(scope.row)" v-hasPermi="['hr:kpi:appeal']" v-if="scope.row.kpiStatus === '3'">申诉</el-button>
                  <el-button link type="info" @click="openAppealHandle(scope.row)" v-hasPermi="['hr:kpi:confirm']" v-if="scope.row.kpiStatus === '4'">处理申诉</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <pagination v-show="recordTotal > 0" :total="recordTotal" v-model:page="recordQuery.pageNum" v-model:limit="recordQuery.pageSize" @pagination="getRecordList" />
      </div>
    </div>
    <!-- 方案编辑 Dialog -->
    <el-dialog v-model="schemeOpen" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ schemeTitle }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">指标权重合计须等于 100；方案启用后方可发起考核</span></div>
          </div>
        </div>
      </template>
      <el-form ref="schemeRef" :model="schemeForm" :rules="schemeRules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.sc0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="方案名称" prop="schemeName"><el-input v-model="schemeForm.schemeName" placeholder="请输入方案名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="考核周期" prop="periodType">
                  <el-select v-model="schemeForm.periodType" placeholder="请选择" style="width: 100%">
                    <el-option v-for="d in periodTypeOptions" :key="d.value" :label="d.label" :value="d.value" />
                  </el-select>
                </el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('sc1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>考核指标</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sc1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.sc1">
              <div style="margin-bottom:8px; display:flex; align-items:center; gap:12px;">
                <el-button type="primary" plain icon="Plus" size="small" @click="addItem">添加指标</el-button>
                <span style="color:var(--ink-500); font-size:12px;">权重合计须等于 100</span>
              </div>
              <el-table :data="schemeForm.items" size="small" border>
                <el-table-column label="指标名称" min-width="160"><template #default="scope"><el-input v-model="scope.row.indicatorName" placeholder="如：销售额/回款率/新客户数" /></template></el-table-column>
                <el-table-column label="权重(%)" width="120"><template #default="scope"><el-input-number v-model="scope.row.weight" :min="0" :max="100" :precision="2" style="width: 100%" /></template></el-table-column>
                <el-table-column label="目标值" width="130"><template #default="scope"><el-input v-model="scope.row.targetValue" /></template></el-table-column>
                <el-table-column label="评分规则" min-width="160"><template #default="scope"><el-input v-model="scope.row.scoreRule" /></template></el-table-column>
                <el-table-column label="操作" width="70" align="center"><template #default="scope"><el-button link type="danger" @click="schemeForm.items.splice(scope.$index, 1)">删除</el-button></template></el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitScheme">确 定</el-button><el-button @click="schemeOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 发起考核 Dialog -->
    <el-dialog v-model="initiateOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2 11 13"/><path d="M22 2 15 22 11 13 2 9z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">发起考核</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">对指定员工批量生成考核记录，走发起—自评—评分—确认—申诉流程</span></div>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ic0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>考核对象</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ic0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ic0">
              <el-form-item label="考核周期"><el-date-picker v-model="initiateForm.period" type="month" value-format="YYYY-MM" style="width: 100%" /></el-form-item>
              <el-form-item label="考核对象"><el-input v-model="initiateForm.employeeIdsText" placeholder="员工ID列表，逗号分隔（可在营销档案页查询）" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitInitiate">发 起</el-button><el-button @click="initiateOpen = false">取 消</el-button></div></template>
    </el-dialog>
    <!-- 评分 Dialog -->
    <el-dialog v-model="scoreOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ scoreType === 'self' ? '员工自评' : '上级评分' }}</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">{{ scoreType === 'self' ? '自评后进入上级评分环节' : '评分后生成最终得分，等待员工确认' }}</span></div>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分信息</div>
            </div>
            <div class="rd-card-body">
              <el-form-item :label="scoreType === 'self' ? '自评分' : '上级评分'"><el-input-number v-model="scoreForm.score" :min="0" :max="100" :precision="2" style="width: 100%" /></el-form-item>
              <el-form-item :label="scoreType === 'self' ? '自评说明' : '上级评语'"><el-input v-model="scoreForm.comment" type="textarea" :rows="3" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitScore">提 交</el-button><el-button @click="scoreOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 申诉 Dialog -->
    <el-dialog v-model="appealOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">考核申诉</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">已确认的考核结果可发起申诉，提交后进入申诉处理环节</span></div>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>申诉信息</div>
            </div>
            <div class="rd-card-body">
              <el-form-item label="申诉理由"><el-input v-model="appealForm.reason" type="textarea" :rows="3" placeholder="请填写申诉理由" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitAppeal">提 交</el-button><el-button @click="appealOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 处理申诉 Dialog -->
    <el-dialog v-model="appealHandleOpen" width="820px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">申诉处理</div>
            <div class="rd-detail-header-sub"><span class="rd-detail-header-placeholder">处理完成后考核完成，可调整最终得分</span></div>
          </div>
        </div>
      </template>
      <el-form label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>处理结果</div>
            </div>
            <div class="rd-card-body">
              <el-form-item label="处理结果"><el-input v-model="appealHandleForm.result" type="textarea" :rows="2" placeholder="请填写申诉处理结果" /></el-form-item>
              <el-form-item label="调整后得分"><el-input-number v-model="appealHandleForm.finalScore" :min="0" :max="100" :precision="2" style="width: 100%" placeholder="留空则维持原得分" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitAppealHandle">完 成</el-button><el-button @click="appealHandleOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="绩效考核业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、考核释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是绩效考核？</div>
          <div class="highlight-card-body">
            <strong>绩效考核</strong>按 KPI 方案（周期/指标/权重/目标/评分规则）对营销人员开展线上考核，流程为<strong>发起—自评—评分—确认—申诉</strong>五步状态机；考核指标可参考营销业绩归集数据（成交/回款/新客/补录），最终得分默认取上级评分，申诉处理后可调整。考核结果保留历史版本支持审计追溯。
          </div>
        </div>
        <h4>二、流程步骤</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="info">发起</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">已自评</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="primary">已评分</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已确认</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已完成</el-tag></div>
          <div class="flow-item" style="margin-left: 16px;"><el-tag type="danger">申诉中</el-tag></div>
        </div>
        <h4>三、状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="发起">考核记录批量生成后的初始状态，等待员工自评</el-descriptions-item>
          <el-descriptions-item label="已自评">员工完成自评，等待上级评分</el-descriptions-item>
          <el-descriptions-item label="已评分">上级完成评分并生成最终得分，等待员工确认</el-descriptions-item>
          <el-descriptions-item label="已确认">员工确认考核结果，可发起申诉</el-descriptions-item>
          <el-descriptions-item label="申诉中">员工对结果发起申诉，等待处理；处理时可调整最终得分</el-descriptions-item>
          <el-descriptions-item label="已完成">考核全流程结束，结果计入排名与审计</el-descriptions-item>
        </el-descriptions>
        <h4>四、业务规则</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">业务规则说明</div>
          <div class="highlight-card-body">
            <p>• <strong>指标权重：</strong>方案指标权重合计必须等于 100</p>
            <p>• <strong>发起条件：</strong>仅「启用」状态的方案可发起考核</p>
            <p>• <strong>评分顺序：</strong>严格按状态机流转，不可跳步</p>
            <p>• <strong>申诉处理：</strong>可调整最终得分，留空维持原得分</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="HrKpi">
import { listScheme, getScheme, addScheme, updateScheme, delScheme, listKpiRecord, initiateRecords, selfScore, superiorScore, confirmRecord, appealRecord, handleAppeal } from '@/api/hr/kpi'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
const { colWidthScheme, onSchemeHeaderDragEnd, schemeTableRef, applySchemeSavedWidths } = useColumnResize('hr_kpi_scheme')
const { colWidthRecord, onRecordHeaderDragEnd, recordTableRef, applyRecordSavedWidths } = useColumnResize('hr_kpi_record')
const { collapsedCards, toggleCard } = useDetailCard(['sc0','sc1','ic0'])
const { proxy } = getCurrentInstance()
const { hr_kpi_period_type: periodTypeOptions } = proxy.useDict('hr_kpi_period_type')

const activeTab = ref('scheme')
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const loading = ref(false)
const schemeList = ref([])
const recordList = ref([])
const recordTotal = ref(0)
const recordQuery = ref({ pageNum: 1, pageSize: 10, period: undefined, employeeName: undefined })
const schemeOpen = ref(false)
const schemeTitle = ref('')
const schemeForm = ref({ items: [] })
const initiateOpen = ref(false)
const initiateForm = ref({})
const scoreOpen = ref(false)
const scoreType = ref('self')
const scoreForm = ref({})
const appealOpen = ref(false)
const appealForm = ref({})
const appealHandleOpen = ref(false)
const appealHandleForm = ref({})
const schemeRules = {
  schemeName: [{ required: true, message: '请输入方案名称', trigger: 'blur' }],
  periodType: [{ required: true, message: '请选择考核周期', trigger: 'change' }]
}

// 考核状态为本地枚举（发起/已自评/已评分/已确认/申诉中/已完成）
const kpiStatusMap = { '0': { label: '发起', cls: 'gray' }, '1': { label: '已自评', cls: 'amber' }, '2': { label: '已评分', cls: 'blue' }, '3': { label: '已确认', cls: 'green' }, '4': { label: '申诉中', cls: 'red' }, '5': { label: '已完成', cls: 'green' } }
const kpiStatusLabel = (v) => kpiStatusMap[v] ? kpiStatusMap[v].label : (v || '-')
const kpiStatusClass = (v) => kpiStatusMap[v] ? kpiStatusMap[v].cls : 'gray'
const schemeStatusMap = { '0': { label: '草稿', cls: 'gray' }, '1': { label: '启用', cls: 'green' }, '2': { label: '停用', cls: 'amber' } }
const schemeStatusLabel = (v) => schemeStatusMap[v] ? schemeStatusMap[v].label : (v || '-')
const schemeStatusClass = (v) => schemeStatusMap[v] ? schemeStatusMap[v].cls : 'gray'

function loadCols(key, defaults) {
  try {
    const saved = localStorage.getItem(key)
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaults).forEach(k => { result[k] = { label: defaults[k].label, visible: parsed[k] !== undefined ? parsed[k] : defaults[k].visible } })
      return result
    }
  } catch (e) {}
  return JSON.parse(JSON.stringify(defaults))
}
const schemeColumns = ref(loadCols('hr_kpi_scheme_columns', {
  schemeName: { label: '方案名称', visible: true },
  periodType: { label: '考核周期', visible: true },
  schemeStatus: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}))
const recordColumns = ref(loadCols('hr_kpi_record_columns', {
  schemeName: { label: '考核方案', visible: true },
  period: { label: '周期', visible: true },
  employeeName: { label: '员工', visible: true },
  selfScore: { label: '自评分', visible: true },
  superiorScore: { label: '上级评分', visible: true },
  finalScore: { label: '最终得分', visible: true },
  kpiStatus: { label: '状态', visible: true }
}))
const activeRecordFilterCount = computed(() => {
  let count = 0
  if (recordQuery.value.period) count++
  if (recordQuery.value.employeeName) count++
  return count
})
function getSchemeList() {
  loading.value = true
  listScheme({ pageNum: 1, pageSize: 100 }).then(res => { schemeList.value = res.rows; loading.value = false; applySchemeSavedWidths() })
}
function getRecordList() {
  loading.value = true
  listKpiRecord(recordQuery.value).then(res => { recordList.value = res.rows; recordTotal.value = res.total; loading.value = false; applyRecordSavedWidths() })
}
function resetRecordQuery() { recordQuery.value.period = undefined; recordQuery.value.employeeName = undefined; getRecordList() }
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function handleAddScheme() { schemeForm.value = { schemeName: undefined, periodType: undefined, items: [{}] }; schemeTitle.value = '新增考核方案'; schemeOpen.value = true }
function handleEditScheme(row) { getScheme(row.schemeId).then(res => { schemeForm.value = res.data; schemeTitle.value = '修改考核方案'; schemeOpen.value = true }) }
function addItem() { schemeForm.value.items.push({ indicatorName: undefined, weight: undefined, targetValue: undefined, scoreRule: undefined }) }
function submitScheme() {
  proxy.$refs['schemeRef'].validate(valid => {
    if (valid) {
      if (schemeForm.value.schemeId) { updateScheme(schemeForm.value).then(() => { proxy.$modal.msgSuccess('修改成功'); schemeOpen.value = false; getSchemeList() }) }
      else { addScheme(schemeForm.value).then(() => { proxy.$modal.msgSuccess('新增成功'); schemeOpen.value = false; getSchemeList() }) }
    }
  })
}
function toggleScheme(row) {
  const target = row.schemeStatus === '1' ? '2' : '1'
  updateScheme({ schemeId: row.schemeId, schemeStatus: target }).then(() => { proxy.$modal.msgSuccess(target === '1' ? '已启用' : '已停用'); getSchemeList() })
}
function handleDelScheme(row) {
  proxy.$modal.confirm('确认删除方案 "' + row.schemeName + '"？').then(() => delScheme(row.schemeId)).then(() => { proxy.$modal.msgSuccess('删除成功'); getSchemeList() }).catch(() => {})
}
function handleInitiate(row) { initiateForm.value = { schemeId: row.schemeId, period: undefined, employeeIdsText: '' }; initiateOpen.value = true }
function submitInitiate() {
  const ids = (initiateForm.value.employeeIdsText || '').split(/[,，]/).map(s => s.trim()).filter(s => s).map(Number)
  if (!initiateForm.value.period || ids.length === 0) { proxy.$modal.msgError('请填写考核周期与考核对象'); return }
  initiateRecords({ schemeId: initiateForm.value.schemeId, period: initiateForm.value.period, employeeIds: ids }).then(res => {
    proxy.$modal.msgSuccess('已发起 ' + res.data + ' 条考核记录')
    initiateOpen.value = false
    activeTab.value = 'record'
    getRecordList()
  })
}
function openScore(row, type) { scoreType.value = type; scoreForm.value = { recordId: row.recordId, score: type === 'self' ? row.selfScore : row.superiorScore, comment: type === 'self' ? row.selfComment : row.superiorComment }; scoreOpen.value = true }
function submitScore() {
  const payload = scoreType.value === 'self'
    ? { recordId: scoreForm.value.recordId, selfScore: scoreForm.value.score, selfComment: scoreForm.value.comment }
    : { recordId: scoreForm.value.recordId, superiorScore: scoreForm.value.score, superiorComment: scoreForm.value.comment }
  const fn = scoreType.value === 'self' ? selfScore : superiorScore
  fn(payload).then(() => { proxy.$modal.msgSuccess('提交成功'); scoreOpen.value = false; getRecordList() })
}
function handleConfirm(row) {
  proxy.$modal.confirm('确认该考核结果？').then(() => confirmRecord(row.recordId)).then(() => { proxy.$modal.msgSuccess('已确认'); getRecordList() }).catch(() => {})
}
function openAppeal(row) { appealForm.value = { recordId: row.recordId, reason: '' }; appealOpen.value = true }
function submitAppeal() {
  if (!appealForm.value.reason) { proxy.$modal.msgError('请填写申诉理由'); return }
  appealRecord(appealForm.value).then(() => { proxy.$modal.msgSuccess('申诉已提交'); appealOpen.value = false; getRecordList() })
}
function openAppealHandle(row) { appealHandleForm.value = { recordId: row.recordId, result: '', finalScore: row.finalScore }; appealHandleOpen.value = true }
function submitAppealHandle() {
  if (!appealHandleForm.value.result) { proxy.$modal.msgError('请填写处理结果'); return }
  handleAppeal(appealHandleForm.value).then(() => { proxy.$modal.msgSuccess('申诉处理完成'); appealHandleOpen.value = false; getRecordList() })
}
getSchemeList()
getRecordList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.hr-kpi-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.hr-kpi-page .kpi-tabs { margin-bottom: 8px; }
.hr-kpi-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.hr-kpi-page .filter-card { padding:14px 20px 16px; }
.hr-kpi-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.hr-kpi-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.hr-kpi-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.hr-kpi-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.hr-kpi-page .filter-card .adv-link:hover { color:var(--brand-600); }
.hr-kpi-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.hr-kpi-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.hr-kpi-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.hr-kpi-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.hr-kpi-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.hr-kpi-page .filter-card .filter-buttons { display:flex; gap:8px; }
.hr-kpi-page .field { display:flex; flex-direction:column; gap:6px; }
.hr-kpi-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.hr-kpi-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.hr-kpi-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.hr-kpi-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-kpi-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.hr-kpi-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.hr-kpi-page .field .control :deep(.el-select) { width:100%; }
.hr-kpi-page .field .control :deep(.el-date-editor) { width:100%; }
.hr-kpi-page .field .control :deep(.el-date-editor .el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.hr-kpi-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.hr-kpi-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.hr-kpi-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.hr-kpi-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.hr-kpi-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.hr-kpi-page .btn-soft .el-icon { font-size:14px; }
.hr-kpi-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.hr-kpi-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.hr-kpi-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.hr-kpi-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.hr-kpi-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.hr-kpi-page .table-wrap { overflow-x:auto; }
.hr-kpi-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.hr-kpi-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.hr-kpi-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.hr-kpi-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.hr-kpi-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.hr-kpi-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.hr-kpi-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .hr-kpi-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .hr-kpi-page .filter-card .filter-bar { grid-template-columns:1fr; } .hr-kpi-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考客诉台账） ===== */
.hr-kpi-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.hr-kpi-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.hr-kpi-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.hr-kpi-page .badge.amber .dot { background:#f59e0b; }
.hr-kpi-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.hr-kpi-page .badge.blue .dot { background:var(--brand-500); }
.hr-kpi-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.hr-kpi-page .badge.green .dot { background:var(--green-500); }
.hr-kpi-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.hr-kpi-page .badge.red .dot { background:var(--red-500); }
.hr-kpi-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.hr-kpi-page .badge.gray .dot { background:var(--ink-400); }
.hr-kpi-page .badge.badge-on-dark { background:rgba(255,255,255,.15); border-color:rgba(255,255,255,.25); color:#fff; }
.hr-kpi-page .badge.badge-on-dark .dot { background:rgba(255,255,255,.7); }
/* ===== Tip Pill ===== */
.hr-kpi-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:28px; padding:0 10px; font-size:13px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.hr-kpi-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
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
