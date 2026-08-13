<template>
  <div class="app-container qms-capa-page">
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
              <label>编号</label>
              <div class="control">
                <el-input v-model="queryParams.capaNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field">
              <label>来源</label>
              <div class="control is-select">
                <el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="d in sourceOptions" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>状态</label>
              <div class="control is-select">
                <el-select v-model="queryParams.capaStatus" placeholder="全部" clearable @change="handleQuery">
                  <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </div>
            </div>
            <div class="field">
              <label>标题</label>
              <div class="control">
                <el-input v-model="queryParams.title" placeholder="请输入" clearable @keyup.enter="handleQuery">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>备注</label>
              <div class="control">
                <el-input v-model="queryParams.remark" placeholder="请输入" clearable @keyup.enter="handleQuery" />
              </div>
            </div>
            <div class="field" v-show="showAdvanced">
              <label>创建时间</label>
              <div class="control">
                <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </div>
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
              <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:capa:add']">新增</el-button>
              <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:capa:edit']">
                <el-icon><Edit /></el-icon> 修改
              </button>
              <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:capa:remove']">
                <el-icon><Delete /></el-icon> 删除
              </button>
              <div class="toolbar-divider"></div>
              <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:capa:export']">
                <el-icon><Download /></el-icon> 导出
              </button>
            </div>
            <div class="right">
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_capa_columns" />
            </div>
          </div>

          <div class="table-wrap">
            <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="CAPA编号" prop="capaNo" key="capaNo" :width="colWidth('capaNo', 160)" resizable show-overflow-tooltip v-if="columns.capaNo.visible">
                <template #header><span>CAPA编号</span><el-tooltip content="CAPA（Corrective and Preventive Action）即纠正与预防措施编号，用于标识和追溯质量问题的闭环处理过程" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
              </el-table-column>
              <el-table-column label="标题" prop="title" key="title" :width="colWidth('title', 220)" resizable show-overflow-tooltip v-if="columns.title.visible" />
              <el-table-column label="来源" prop="sourceType" key="sourceType" :width="colWidth('sourceType', 110)" resizable align="center" v-if="columns.sourceType.visible"><template #default="scope"><span class="badge violet"><span class="dot"></span>{{ capaSourceTypeLabel(scope.row.sourceType) }}</span></template></el-table-column>
              <el-table-column label="来源编号" prop="sourceNo" key="sourceNo" :width="colWidth('sourceNo', 140)" resizable show-overflow-tooltip v-if="columns.sourceNo.visible" />
              <el-table-column label="状态" prop="capaStatus" key="capaStatus" :width="colWidth('capaStatus', 100)" resizable align="center" v-if="columns.capaStatus.visible"><template #default="scope"><span class="badge" :class="capaStatusBadgeClass(scope.row.capaStatus)"><span class="dot"></span>{{ capaStatusLabel(scope.row.capaStatus) }}</span></template></el-table-column>
              <el-table-column label="责任人" prop="responsiblePerson" key="responsiblePerson" :width="colWidth('responsiblePerson', 100)" resizable show-overflow-tooltip v-if="columns.responsiblePerson.visible" />
              <el-table-column label="计划关闭" prop="planCloseTime" key="planCloseTime" :width="colWidth('planCloseTime', 120)" resizable align="center" v-if="columns.planCloseTime.visible"><template #default="scope"><span>{{ parseTime(scope.row.planCloseTime, '{y}-{m}-{d}') }}</span></template></el-table-column>
              <el-table-column label="操作" width="300" align="center" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                  <el-button v-if="scope.row.capaStatus === '0' || scope.row.capaStatus === '4'" link type="primary" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['qms:capa:edit']">提交</el-button>
                  <el-button v-if="scope.row.capaStatus === '1'" link type="primary" icon="Promotion" @click="handleSubmitVerify(scope.row)" v-hasPermi="['qms:capa:edit']">提交验证</el-button>
                  <el-button v-if="scope.row.capaStatus === '2'" link type="success" icon="CircleCheck" @click="handleClose(scope.row)" v-hasPermi="['qms:capa:close']">关闭</el-button>
                  <el-button v-if="scope.row.capaStatus === '2'" link type="warning" icon="CircleClose" @click="handleReject(scope.row)" v-hasPermi="['qms:capa:close']">驳回</el-button>
                  <el-button v-if="scope.row.capaStatus !== '3'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:capa:edit']">修改</el-button>
                  <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:capa:remove']">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
        </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="984px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="capaRef" :model="form" :rules="rules" label-width="120px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="capaNo"><template #label><span>CAPA编号</span><el-tooltip content="CAPA（Corrective and Preventive Action）即纠正与预防措施编号，用于标识和追溯质量问题的闭环处理过程，保存后由系统自动生成" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.capaNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="标题" prop="title"><el-input v-model="form.title" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="来源类型"><el-select v-model="form.sourceType" style="width: 100%"><el-option v-for="d in sourceOptions" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="来源编号"><el-input v-model="form.sourceNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>责任与计划</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="责任人"><el-input v-model="form.responsiblePerson" readonly placeholder="请选择责任人" style="width: 100%" @click="openUserPicker"><template #append><el-button icon="Search" @click="openUserPicker" /></template><template #suffix><el-icon v-if="form.responsiblePerson" class="rd-form-tip" style="cursor:pointer" @click.stop="clearResponsiblePerson"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划关闭"><el-date-picker v-model="form.planCloseTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注信息" maxlength="500" show-word-limit /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告内容（D1~D5）
                <el-tooltip content="D1~D5在新建/修改时填写；D6验证结果在「提交验证」时填写；D7预防措施、D8关闭备注在「关闭」时填写" placement="top"><el-icon class="rd-form-tip" style="margin-left:6px"><QuestionFilled /></el-icon></el-tooltip>
              </div>
            </div>
            <div class="rd-card-body">
              <el-form-item><template #label><span>D1 团队成员</span><el-tooltip content="D1：组建跨职能团队来解决问题，团队成员应包含具有相关知识和权限的人员" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.teamMembers" type="textarea" :rows="1" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D2 问题描述</span><el-tooltip content="D2：用可测量的术语清晰描述问题，明确谁、什么、何时、何地、为何" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.problemDesc" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D3 临时措施</span><el-tooltip content="D3：制定并实施临时遏制措施以隔离问题、防止问题扩大或产生不良影响" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.interimAction" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D4 根因分析</span><el-tooltip content="D4：识别并验证导致问题的根本原因，可使用鱼骨图、5Why等分析工具" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.rootCause" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
              <el-form-item><template #label><span>D5 永久措施</span><el-tooltip content="D5：基于根因分析制定并实施永久性纠正措施，消除问题根因" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.permanentAction" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="984px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">CAPA详情</span>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">CAPA编号</span><div class="rd-value">{{ viewData.capaNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="capaStatusBadgeClass(viewData.capaStatus)"><span class="dot"></span>{{ capaStatusLabel(viewData.capaStatus) }}</span></div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">标题</span><div class="rd-value">{{ viewData.title || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源</span><div class="rd-value">{{ sourceOptions.find(d => d.value === viewData.sourceType)?.label || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源编号</span><div class="rd-value">{{ viewData.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ viewData.responsiblePerson || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划关闭</span><div class="rd-value">{{ parseTime(viewData.planCloseTime, '{y}-{m}-{d}') || '-' }}</div></div>
            <div class="rd-item" v-if="viewData.closeTime"><span class="rd-label">实际关闭</span><div class="rd-value">{{ parseTime(viewData.closeTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</div></div>
            <div class="rd-item rd-item--full" v-if="viewData.remark && viewData.capaStatus !== '4'"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">D1 团队成员</span><div class="rd-value">{{ viewData.teamMembers || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D2 问题描述</span><div class="rd-value">{{ viewData.problemDesc || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D3 临时措施</span><div class="rd-value">{{ viewData.interimAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D4 根因分析</span><div class="rd-value">{{ viewData.rootCause || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D5 永久措施</span><div class="rd-value">{{ viewData.permanentAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D6 验证结果</span><div class="rd-value">{{ viewData.verifyResult || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D7 预防措施</span><div class="rd-value">{{ viewData.preventAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D8 关闭备注</span><div class="rd-value">{{ viewData.closeRemark || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.capaStatus === '4' && viewData.remark">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></span>驳回信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">驳回原因</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 提交验证对话框（D6） -->
    <el-dialog v-model="verifyOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <span class="rd-detail-header-title">提交验证</span>
          <div class="rd-detail-header-sub" v-if="verifyForm.capaNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ verifyForm.capaNo }}</span></div>
        </div>
      </template>
      <div class="rd-page" v-if="verifyForm.capaId">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">CAPA编号</span><div class="rd-value">{{ verifyForm.capaNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="capaStatusBadgeClass(verifyForm.capaStatus)"><span class="dot"></span>{{ capaStatusLabel(verifyForm.capaStatus) }}</span></div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">标题</span><div class="rd-value">{{ verifyForm.title || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源</span><div class="rd-value">{{ sourceOptions.find(d => d.value === verifyForm.sourceType)?.label || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源编号</span><div class="rd-value">{{ verifyForm.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ verifyForm.responsiblePerson || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划关闭</span><div class="rd-value">{{ parseTime(verifyForm.planCloseTime, '{y}-{m}-{d}') || '-' }}</div></div>
          </div></div>
        </section>
        <!-- 8D报告摘要（D1~D5） -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告摘要</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">D1 团队成员</span><div class="rd-value">{{ verifyForm.teamMembers || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D2 问题描述</span><div class="rd-value">{{ verifyForm.problemDesc || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D3 临时措施</span><div class="rd-value">{{ verifyForm.interimAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D4 根因分析</span><div class="rd-value">{{ verifyForm.rootCause || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D5 永久措施</span><div class="rd-value">{{ verifyForm.permanentAction || '-' }}</div></div>
          </div></div>
        </section>
        <!-- D6 验证结果 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>D6 验证结果</div></div>
          <div class="rd-card-body">
            <el-form ref="verifyRef" :model="verifyForm" :rules="verifyRules" label-width="100px">
              <el-form-item label="验证结果" prop="verifyResult">
                <el-input v-model="verifyForm.verifyResult" type="textarea" :rows="4" placeholder="请输入验证结果，如：已确认永久措施有效，问题不再复现，数据指标恢复正常等" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitVerify">确认提交</el-button><el-button @click="verifyOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 关闭对话框（D7/D8） -->
    <el-dialog v-model="closeOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <span class="rd-detail-header-title">关闭CAPA</span>
          <div class="rd-detail-header-sub" v-if="closeForm.capaNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ closeForm.capaNo }}</span></div>
        </div>
      </template>
      <div class="rd-page" v-if="closeForm.capaId">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">CAPA编号</span><div class="rd-value">{{ closeForm.capaNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="capaStatusBadgeClass(closeForm.capaStatus)"><span class="dot"></span>{{ capaStatusLabel(closeForm.capaStatus) }}</span></div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">标题</span><div class="rd-value">{{ closeForm.title || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源</span><div class="rd-value">{{ sourceOptions.find(d => d.value === closeForm.sourceType)?.label || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源编号</span><div class="rd-value">{{ closeForm.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ closeForm.responsiblePerson || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划关闭</span><div class="rd-value">{{ parseTime(closeForm.planCloseTime, '{y}-{m}-{d}') || '-' }}</div></div>
          </div></div>
        </section>
        <!-- 8D报告摘要（D1~D6） -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告摘要</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">D1 团队成员</span><div class="rd-value">{{ closeForm.teamMembers || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D2 问题描述</span><div class="rd-value">{{ closeForm.problemDesc || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D3 临时措施</span><div class="rd-value">{{ closeForm.interimAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D4 根因分析</span><div class="rd-value">{{ closeForm.rootCause || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D5 永久措施</span><div class="rd-value">{{ closeForm.permanentAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D6 验证结果</span><div class="rd-value">{{ closeForm.verifyResult || '-' }}</div></div>
          </div></div>
        </section>
        <!-- D7/D8 关闭信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>D7/D8 关闭信息</div></div>
          <div class="rd-card-body">
            <el-form ref="closeRef" :model="closeForm" :rules="closeRules" label-width="120px">
              <el-form-item prop="preventAction">
                <template #label><span>D7 预防措施</span><el-tooltip content="D7：制定预防措施，防止类似问题在其他产品或过程中再次发生" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input v-model="closeForm.preventAction" type="textarea" :rows="3" placeholder="请输入预防措施，如：更新SOP、修改检验标准、加强培训等" show-word-limit maxlength="500" />
              </el-form-item>
              <el-form-item prop="closeRemark">
                <template #label><span>D8 关闭备注</span><el-tooltip content="D8：确认团队任务完成、认可团队贡献、记录经验教训" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                <el-input v-model="closeForm.closeRemark" type="textarea" :rows="3" placeholder="请输入关闭备注，如：团队任务已完成，措施验证有效，经验教训已记录" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitClose">确认关闭</el-button><el-button @click="closeOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">驳回CAPA</span>
          <div class="rd-detail-header-sub" v-if="rejectForm.capaNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ rejectForm.capaNo }}</span></div>
        </div>
      </template>
      <div class="rd-page" v-if="rejectForm.capaId">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">CAPA编号</span><div class="rd-value">{{ rejectForm.capaNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span class="badge" :class="capaStatusBadgeClass(rejectForm.capaStatus)"><span class="dot"></span>{{ capaStatusLabel(rejectForm.capaStatus) }}</span></div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">标题</span><div class="rd-value">{{ rejectForm.title || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源</span><div class="rd-value">{{ sourceOptions.find(d => d.value === rejectForm.sourceType)?.label || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源编号</span><div class="rd-value">{{ rejectForm.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">责任人</span><div class="rd-value">{{ rejectForm.responsiblePerson || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">计划关闭</span><div class="rd-value">{{ parseTime(rejectForm.planCloseTime, '{y}-{m}-{d}') || '-' }}</div></div>
          </div></div>
        </section>
        <!-- 8D报告摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>8D报告摘要</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">D2 问题描述</span><div class="rd-value">{{ rejectForm.problemDesc || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D5 永久措施</span><div class="rd-value">{{ rejectForm.permanentAction || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">D6 验证结果</span><div class="rd-value">{{ rejectForm.verifyResult || '-' }}</div></div>
          </div></div>
        </section>
        <!-- 驳回意见 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>驳回意见</div></div>
          <div class="rd-card-body">
            <el-form ref="rejectRef" :model="rejectForm" :rules="rejectRules" label-width="100px">
              <el-form-item label="驳回原因" prop="rejectReason">
                <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因，如验证不通过、措施无效、根因分析不充分等" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><div class="dialog-footer"><el-button type="danger" @click="submitReject">确认驳回</el-button><el-button @click="rejectOpen = false">取 消</el-button></div></template>
    </el-dialog>

    <!-- 人员选择器 -->
    <user-picker ref="userPickerRef" title="选择责任人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="CAPA台账业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、CAPA释义 -->
        <h4>一、CAPA释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是CAPA？</div>
          <div class="highlight-card-body">
            <strong>CAPA（Corrective and Preventive Action，纠正与预防措施）</strong>是质量管理中用于系统性解决质量问题、防止问题再发的核心闭环工具。当不合格品报告（NCR）关闭、客户投诉成立或内审发现不符合项时，发起CAPA对问题进行<strong>根因分析 → 制定纠正措施 → 实施预防措施 → 验证有效性 → 关闭</strong>的全链路闭环管理。<br/><br/>
            CAPA采用<strong>8D（Eight Disciplines）方法论</strong>，通过8个结构化步骤（D1~D8）确保问题得到彻底解决，避免同类问题重复发生，满足ISO 9001、IATF 16949、GMP等质量管理体系对持续改进的要求。
          </div>
        </div>

        <!-- 二、CAPA状态流转图 -->
        <h4>二、CAPA状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">进行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">验证中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已关闭</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="warning">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已驳回</el-tag>
            <el-tag size="small" type="info">修改后重新提交</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">CAPA创建后的初始状态。可修改、删除、填写8D报告内容。填写完成后点击「提交」进入进行中</el-descriptions-item>
          <el-descriptions-item label="进行中">8D报告填写中或措施执行中。可继续修改8D各节点内容，推进根因分析和措施实施。完成后点击「提交验证」进入验证中</el-descriptions-item>
          <el-descriptions-item label="验证中">措施已实施，等待验证措施有效性。可点击「关闭」完成CAPA闭环，或点击「驳回」退回修改</el-descriptions-item>
          <el-descriptions-item label="已关闭">CAPA流程完成，8D报告锁定。关闭后不可修改，记录归档用于有效性追踪</el-descriptions-item>
          <el-descriptions-item label="已驳回">验证不通过，措施无效。可修改后点击「提交」重新进入进行中</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>CAPA编号：</strong>纠正与预防措施的唯一标识编号。保存后由系统自动生成，无需手动输入</p>
            <p>• <strong>标题：</strong>简要概括质量问题内容，便于检索和识别。<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>来源类型：</strong>选择CAPA的来源，关联到触发质量问题闭环的源头单据：</p>
            <p style="padding-left: 16px;">— <strong>不合格品NCR：</strong>来源为NCR台账中关闭的不合格品报告</p>
            <p style="padding-left: 16px;">— <strong>客诉：</strong>来源为客诉管理中成立的客户投诉</p>
            <p style="padding-left: 16px;">— <strong>内审不符合项：</strong>来源为内部审核中发现的不符合项</p>
            <p>• <strong>来源编号：</strong>关联的来源单据编号（如NCR编号、客诉编号、内审编号），便于追溯到源头</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">责任与计划区</div>
          <div class="highlight-card-body">
            <p>• <strong>责任人：</strong>负责推进CAPA闭环的责任人，从组织机构人员中选择</p>
            <p>• <strong>计划关闭：</strong>CAPA计划完成的日期，用于超期预警和进度跟踪</p>
            <p>• <strong>备注：</strong>补充说明信息，如关联文件编号、特殊注意事项等，非必填</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">8D报告内容区</div>
          <div class="highlight-card-body">
            <p>8D报告是CAPA的核心内容，采用八步法结构化解决问题。各步骤填写说明如下：</p>
            <p>• <strong>D1 团队成员：</strong>组建跨职能团队，列出参与问题解决的核心成员（含姓名/岗位）。团队应包含具有相关知识和权限的人员</p>
            <p>• <strong>D2 问题描述：</strong>用可测量的术语清晰描述问题，明确<strong>谁、什么、何时、何地、为何、如何</strong>（5W2H）。描述应具体、量化、无歧义</p>
            <p>• <strong>D3 临时措施：</strong>制定并实施临时遏制措施以隔离问题、防止问题扩大或产生不良影响。如：隔离库存、暂停生产、通知客户等</p>
            <p>• <strong>D4 根因分析：</strong>识别并验证导致问题的根本原因。推荐使用<strong>5Why分析法</strong>（连续追问5个为什么）或<strong>鱼骨图</strong>（人/机/料/法/环/测）等工具，避免仅停留在表面原因</p>
            <p>• <strong>D5 永久措施：</strong>基于根因分析制定并实施永久性纠正措施，从根本上消除问题根因。措施应明确、可执行、可验证</p>
            <p>• <strong>D6 验证结果：</strong>验证永久纠正措施是否有效解决了问题，确认措施不会产生新的不良影响。可通过数据对比、试产验证、客户反馈等方式验证</p>
            <p>• <strong>D7 预防措施：</strong>制定预防措施，防止类似问题在其他产品、产线或过程中再次发生。如：更新SOP、修改检验标准、加强培训等</p>
            <p>• <strong>D8 关闭备注：</strong>确认团队任务完成、认可团队贡献、记录经验教训，填写关闭说明后关闭CAPA</p>
          </div>
        </div>

        <!-- 五、8D方法论说明 -->
        <h4>五、8D方法论说明</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是8D？</div>
          <div class="highlight-card-body">
            <strong>8D（Eight Disciplines）</strong>是质量管理中用于系统性解决问题的结构化方法，起源于福特汽车公司，现广泛应用于制造业。8D方法通过8个步骤确保问题得到彻底解决：<br/><br/>
            <strong>D1 组建团队 → D2 问题描述 → D3 临时措施 → D4 根因分析 → D5 永久措施 → D6 验证 → D7 预防 → D8 关闭</strong><br/><br/>
            8D方法的核心价值在于：不止步于表面原因，而是通过根因分析找到问题的本质，再通过永久措施和预防措施确保问题不再发生。
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>发起CAPA：</strong>NCR关闭或客诉成立后，点击「新增」创建CAPA，填写标题、来源类型、来源编号、责任人等基本信息
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>填写8D报告：</strong>在新增/修改对话框中按D1~D8顺序填写8D报告内容。可分多次保存，逐步完善各节点内容
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交CAPA：</strong>草稿状态下填写基本信息后，点击「提交」将CAPA状态推进到进行中，开始8D流程
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>根因分析：</strong>重点完成D4根因分析，使用5Why或鱼骨图等工具找到问题根本原因，制定D5永久措施
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>提交验证：</strong>进行中状态完成D1~D6后，点击「提交验证」将CAPA状态推进到验证中，等待验证措施有效性
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>关闭CAPA：</strong>验证中状态验证通过后，点击「关闭」按钮，填写D7预防措施和D8关闭备注，完成CAPA闭环
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>驳回CAPA：</strong>验证中状态验证不通过时，点击「驳回」按钮，填写驳回原因，CAPA退回到已驳回状态，可修改后重新提交
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsCapa">
import { listCapa, getCapa, addCapa, updateCapa, delCapa, submitCapa, submitVerifyCapa, closeCapa, rejectCapa } from '@/api/qms/capa'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown, ArrowRight, QuestionFilled, CircleClose } from '@element-plus/icons-vue'
import UserPicker from '@/components/UserPicker/index.vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_capa_index')
const { proxy } = getCurrentInstance()
const userPickerRef = ref()
const sourceOptions = [{ value: 'ncr', label: '不合格品NCR' }, { value: 'complaint', label: '客诉' }, { value: 'audit', label: '内审不符合项' }]
const { qms_capa_status: statusOptions } = proxy.useDict('qms_capa_status')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const total = ref(0)
const open = ref(false)
const closeOpen = ref(false)
const rejectOpen = ref(false)
const verifyOpen = ref(false)
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 })
const statusTabList = computed(() => statusOptions.value)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref(null)
const selectedId = ref(null)
const selectedIds = ref([])
const defaultColumns = { capaNo: { label: 'CAPA编号', visible: true }, title: { label: '标题', visible: true }, sourceType: { label: '来源', visible: true }, sourceNo: { label: '来源编号', visible: true }, capaStatus: { label: '状态', visible: true }, responsiblePerson: { label: '责任人', visible: true }, planCloseTime: { label: '计划关闭', visible: true } }
function loadColumnVisibility() { try { const saved = localStorage.getItem('qms_capa_columns'); if (saved) { const parsed = JSON.parse(saved); const result = {}; Object.keys(defaultColumns).forEach(key => { result[key] = { label: defaultColumns[key].label, visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible } }); return result } } catch (e) {} return { ...defaultColumns } }
const columns = ref(loadColumnVisibility())
const closeForm = ref({ capaId: null, capaNo: undefined, title: undefined, sourceType: undefined, sourceNo: undefined, capaStatus: undefined, responsiblePerson: undefined, planCloseTime: undefined, teamMembers: undefined, problemDesc: undefined, interimAction: undefined, rootCause: undefined, permanentAction: undefined, verifyResult: undefined, preventAction: undefined, closeRemark: undefined })
const rejectForm = ref({ capaId: null, capaNo: undefined, rejectReason: undefined })
const verifyForm = ref({ capaId: null, capaNo: undefined, verifyResult: undefined })
const closeRules = { preventAction: [{ required: true, message: '请输入预防措施', trigger: 'blur' }], closeRemark: [{ required: true, message: '请输入关闭备注', trigger: 'blur' }] }
const rejectRules = { rejectReason: [{ required: true, message: '请输入驳回原因', trigger: 'blur' }] }
const verifyRules = { verifyResult: [{ required: true, message: '请输入验证结果', trigger: 'blur' }] }
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, capaNo: undefined, sourceType: undefined, capaStatus: undefined, title: undefined, remark: undefined, params: {} } })
const { queryParams, form } = toRefs(data)
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.capaNo) count++
  if (queryParams.value.sourceType) count++
  if (queryParams.value.capaStatus) count++
  if (queryParams.value.title) count++
  if (queryParams.value.remark) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listCapa(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.capaNo = undefined; queryParams.value.sourceType = undefined; queryParams.value.capaStatus = undefined; queryParams.value.title = undefined; queryParams.value.remark = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增CAPA' }
function openUserPicker() { userPickerRef.value.open(form.value.responsiblePersonId) }
function onUserPickerConfirm(user) { form.value.responsiblePersonId = user.userId; form.value.responsiblePerson = user.nickName }
function clearResponsiblePerson() { form.value.responsiblePersonId = undefined; form.value.responsiblePerson = undefined }
function handleUpdate(row) {
  const id = row?.capaId || selectedId.value
  getCapa(id).then(res => {
    data.form = res.data
    open.value = true
    title.value = '修改CAPA'
  })
}
function handleView(row) { getCapa(row.capaId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleSelectionChange(selection) { selectedIds.value = selection.map(item => item.capaId); selectedId.value = selectedIds.value[0] }
function submitForm() {
  proxy.$refs['capaRef'].validate(valid => {
    if (valid) {
      if (form.value.capaId) { updateCapa(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCapa(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) {
  const ids = row?.capaId ? [row.capaId] : selectedIds.value
  proxy.$modal.confirm('确认删除编号为"' + ids + '"的数据项？').then(() => delCapa(ids.join(','))).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleSubmit(row) {
  proxy.$modal.confirm('确认提交CAPA "' + row.capaNo + '"进入进行中？').then(() => submitCapa(row.capaId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {})
}
function handleSubmitVerify(row) {
  getCapa(row.capaId).then(res => {
    verifyForm.value = { ...res.data, verifyResult: res.data.verifyResult || undefined }
    verifyOpen.value = true
  })
}
function submitVerify() {
  proxy.$refs['verifyRef'].validate(valid => {
    if (valid) { submitVerifyCapa({ capaId: verifyForm.value.capaId, verifyResult: verifyForm.value.verifyResult }).then(() => { proxy.$modal.msgSuccess('已提交验证'); verifyOpen.value = false; getList() }) }
  })
}
function handleClose(row) {
  getCapa(row.capaId).then(res => {
    closeForm.value = { ...res.data, preventAction: res.data.preventAction || undefined, closeRemark: res.data.closeRemark || undefined }
    closeOpen.value = true
  })
}
function submitClose() {
  proxy.$refs['closeRef'].validate(valid => {
    if (valid) { closeCapa(closeForm.value).then(() => { proxy.$modal.msgSuccess('关闭成功'); closeOpen.value = false; getList() }) }
  })
}
function handleReject(row) {
  getCapa(row.capaId).then(res => {
    rejectForm.value = { ...res.data, rejectReason: undefined }
    rejectOpen.value = true
  })
}
function submitReject() {
  proxy.$refs['rejectRef'].validate(valid => {
    if (valid) { rejectCapa(rejectForm.value.capaId, rejectForm.value.rejectReason).then(() => { proxy.$modal.msgSuccess('已驳回'); rejectOpen.value = false; getList() }) }
  })
}
function handleExport() { proxy.download('qms/capa/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `capa_${new Date().getTime()}.xlsx`) }
function reset() {
  form.value = { capaId: null, capaNo: undefined, sourceType: 'ncr', sourceNo: undefined, title: undefined, teamMembers: undefined, problemDesc: undefined, interimAction: undefined, rootCause: undefined, permanentAction: undefined, verifyResult: undefined, preventAction: undefined, closeRemark: undefined, capaStatus: '0', responsiblePersonId: undefined, responsiblePerson: undefined, planCloseTime: undefined, remark: undefined }
  proxy.resetForm('capaRef')
}
function cancel() { open.value = false; reset() }
function capaSourceTypeLabel(val) { const item = sourceOptions.find(d => d.value == val); return item ? item.label : '-' }
function capaStatusLabel(val) { const item = statusOptions.value.find(d => d.value == val); return item ? item.label : '-' }
function capaStatusBadgeClass(val) { if (val == '0') return 'orange'; if (val == '1') return 'blue'; if (val == '2') return 'green'; if (val == '3') return 'gray'; if (val == '4') return 'red'; return 'gray' }

function loadStatusCounts() {
  listCapa({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.capaStatus] !== undefined) counts[r.capaStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.capaStatus = status === 'all' ? undefined : status
  handleQuery()
}

function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-progress', '2': 'tab-verifying', '3': 'tab-closed', '4': 'tab-reject' }
  return map[value] || ''
}
getList()
</script>

<style scoped>
.qms-capa-page { padding-top: 10px; --violet-50:#f5f3ff; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-capa-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-capa-page .filter-card { padding:14px 20px 16px; }
.qms-capa-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-capa-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-capa-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-capa-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-capa-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-capa-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-capa-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-capa-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-capa-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-capa-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-capa-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-capa-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-capa-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.qms-capa-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-capa-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-capa-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-capa-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-capa-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-capa-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-capa-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.qms-capa-page .field .control :deep(.el-select) { width:100%; }
.qms-capa-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-capa-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.qms-capa-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-capa-page .field .control :deep(.el-date-editor) { width:100%; }
.qms-capa-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.qms-capa-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.qms-capa-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.qms-capa-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-capa-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-capa-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-capa-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-capa-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-capa-page .btn-soft .el-icon { font-size:14px; }
.qms-capa-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-capa-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-capa-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-capa-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-capa-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-capa-page .table-wrap { overflow-x:auto; }
.qms-capa-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-capa-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-capa-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-capa-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-capa-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-capa-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-capa-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-capa-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-capa-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-capa-page .badge.green .dot { background:var(--green-500); }
.qms-capa-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-capa-page .badge.gray .dot { background:var(--ink-400); }
.qms-capa-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-capa-page .badge.orange .dot { background:#f59e0b; }
.qms-capa-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-capa-page .badge.blue .dot { background:var(--brand-500); }
.qms-capa-page .badge.violet { background:var(--violet-50); color:#7c3aed; border-color:#ddd6fe; }
.qms-capa-page .badge.violet .dot { background:#8b5cf6; }
.qms-capa-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-capa-page .badge.red .dot { background:var(--red-500); }
.qms-capa-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
.qms-capa-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.qms-capa-page .rd-form-tip:hover { color: #909399; }
/* ===== Status Tabs ===== */
.qms-capa-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.qms-capa-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.qms-capa-page .tabs-track::-webkit-scrollbar { display:none; }
.qms-capa-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.qms-capa-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.qms-capa-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.qms-capa-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.qms-capa-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.qms-capa-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.qms-capa-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 草稿 - orange */
.qms-capa-page .status-tab.tab-draft .dot { background:#f59e0b; }
.qms-capa-page .status-tab.tab-draft .count { background:#fffbeb; color:#b45309; }
.qms-capa-page .status-tab.is-active.tab-draft .count { background:#f59e0b; color:#fff; }
/* 进行中 - brand/blue */
.qms-capa-page .status-tab.tab-progress .dot { background:var(--brand-500); }
.qms-capa-page .status-tab.tab-progress .count { background:var(--brand-50); color:var(--brand-700); }
.qms-capa-page .status-tab.is-active.tab-progress .count { background:var(--brand-500); color:#fff; }
/* 验证中 - green */
.qms-capa-page .status-tab.tab-verifying .dot { background:var(--green-500); }
.qms-capa-page .status-tab.tab-verifying .count { background:var(--green-50); color:var(--green-700); }
.qms-capa-page .status-tab.is-active.tab-verifying .count { background:var(--green-500); color:#fff; }
/* 已关闭 - gray */
.qms-capa-page .status-tab.tab-closed .dot { background:var(--ink-400); }
.qms-capa-page .status-tab.tab-closed .count { background:var(--ink-100); color:var(--ink-500); }
.qms-capa-page .status-tab.is-active.tab-closed .count { background:var(--ink-400); color:#fff; }
/* 已驳回 - red */
.qms-capa-page .status-tab.tab-reject .dot { background:var(--red-500); }
.qms-capa-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.qms-capa-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
/* ===== Tip Pill ===== */
.qms-capa-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-capa-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
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
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.status-help-content .highlight-danger .highlight-card-title { color: #f56c6c; }
@media (max-width:1100px) { .qms-capa-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-capa-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-capa-page .toolbar { flex-wrap:wrap; gap:10px; } .qms-capa-page .status-tabs { padding:6px 8px; } }
</style>
