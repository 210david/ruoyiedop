<template>
  <div class="app-container qms-ncr-page">
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
          <label>NCR编号</label>
          <div class="control">
            <el-input v-model="queryParams.ncrNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>NCR状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.ncrStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_ncr_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>来源类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.sourceType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_ncr_source" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>缺陷等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.defectLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>处置方式</label>
          <div class="control is-select">
            <el-select v-model="queryParams.disposition" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_disposition" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>来源单号</label>
          <div class="control">
            <el-input v-model="queryParams.sourceNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>报告人</label>
          <div class="control">
            <el-input v-model="queryParams.discovererName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
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
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><QuestionFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:ncr:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate()" v-hasPermi="['qms:ncr:edit']"><el-icon><Edit /></el-icon> 修改</button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete()" v-hasPermi="['qms:ncr:remove']"><el-icon><Delete /></el-icon> 删除</button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:ncr:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="NCR编号" prop="ncrNo" :width="colWidth('ncrNo', 150)" resizable show-overflow-tooltip>
            <template #header><span>NCR编号</span><el-tooltip content="NCR（Non-Conformance Report）即不合格品报告编号，用于标识和追溯不合格品的全流程处理记录" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="来源类型" prop="sourceType" :width="colWidth('sourceType', 100)" resizable align="center">
            <template #default="scope"><dict-tag :options="qms_ncr_source" :value="scope.row.sourceType" /></template>
          </el-table-column>
          <el-table-column label="来源单号" prop="sourceNo" :width="colWidth('sourceNo', 150)" resizable show-overflow-tooltip />
          <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 140)" resizable show-overflow-tooltip />
          <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip />
          <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable show-overflow-tooltip />
          <el-table-column label="不合格数量" prop="defectQty" :width="colWidth('defectQty', 100)" resizable align="right" />
          <el-table-column label="缺陷等级" prop="defectLevel" :width="colWidth('defectLevel', 100)" resizable align="center">
            <template #header><span>缺陷等级</span><el-tooltip content="不合格品缺陷的严重程度等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge" :class="defectBadgeClass(scope.row.defectLevel)"><span class="dot"></span>{{ defectLevelLabel(scope.row.defectLevel) }}</span></template>
          </el-table-column>
          <el-table-column label="处置方式" prop="disposition" :width="colWidth('disposition', 110)" resizable align="center">
            <template #header><span>处置方式</span><el-tooltip content="对不合格品的处理决定，通常包括：退货、挑选、返工、报废、让步接收等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><dict-tag :options="qms_disposition" :value="scope.row.disposition" /></template>
          </el-table-column>
          <el-table-column label="NCR状态" prop="ncrStatus" :width="colWidth('ncrStatus', 100)" resizable align="center">
            <template #header><span>NCR状态</span><el-tooltip content="不合格品报告的流程状态，包括：已登记、评审中、处置中、待验证、已关闭、已作废" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge" :class="ncrBadgeClass(scope.row.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(scope.row.ncrStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="报告人" prop="discovererName" :width="colWidth('discovererName', 100)" resizable align="center" />
          <el-table-column label="报告时间" prop="discoverTime" :width="colWidth('discoverTime', 160)" resizable align="center" />
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:ncr:edit']" v-if="scope.row.ncrStatus === '0'">修改</el-button>
              <el-button link type="warning" icon="Promotion" @click="handleSubmitReview(scope.row)" v-hasPermi="['qms:ncr:edit']" v-if="scope.row.ncrStatus === '0'">提交评审</el-button>
              <el-button link type="success" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['qms:ncr:approve']" v-if="scope.row.ncrStatus === '1'">审批</el-button>
              <el-button link type="warning" icon="Tools" @click="handleDisposition(scope.row)" v-hasPermi="['qms:ncr:edit']" v-if="scope.row.ncrStatus === '2'">处置</el-button>
              <el-button link type="success" icon="Check" @click="handleVerify(scope.row)" v-hasPermi="['qms:ncr:approve']" v-if="scope.row.ncrStatus === '3'">验证</el-button>
              <el-button link type="info" icon="CircleClose" @click="handleClose(scope.row)" v-hasPermi="['qms:ncr:approve']" v-if="scope.row.ncrStatus === '3'">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="ncrRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="ncrNo"><template #label><span>NCR编号</span><el-tooltip content="NCR（Non-Conformance Report）即不合格品报告编号，用于标识和追溯不合格品的全流程处理记录，保存后由系统自动生成" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.ncrNo" placeholder="保存后自动生成" disabled /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="来源类型" prop="sourceType"><el-select v-model="form.sourceType" placeholder="请选择" style="width: 100%"><el-option v-for="d in qms_ncr_source" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="来源单号" prop="sourceNo"><el-input v-model="form.sourceNo" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.materialCode" class="rd-form-tip" style="cursor:pointer" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable clearable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="不合格数量" prop="defectQty"><el-input-number v-model="form.defectQty" :min="0" :precision="0" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>缺陷与处置</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="defectLevel"><template #label><span>缺陷等级</span><el-tooltip content="不合格品缺陷的严重程度等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.defectLevel" style="width: 100%"><el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="disposition"><template #label><span>处置方式</span><el-tooltip content="对不合格品的处理决定，通常包括：退货、挑选、返工、报废、让步接收等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.disposition" style="width: 100%"><el-option v-for="d in qms_disposition" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="是否隔离" prop="isolateFlag"><el-radio-group v-model="form.isolateFlag"><el-radio value="0">否</el-radio><el-radio value="1">是</el-radio></el-radio-group></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="隔离区域" prop="isolateArea" v-if="form.isolateFlag === '1'"><el-input v-model="form.isolateArea" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="缺陷描述" prop="defectDesc"><el-input v-model="form.defectDesc" type="textarea" :rows="3" placeholder="请输入" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>报告与责任</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c3">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="报告人" prop="discovererName"><el-input v-model="form.discovererName" readonly placeholder="请选择报告人" style="width: 100%" @click="openUserPicker"><template #append><el-button icon="Search" @click="openUserPicker" /></template><template #suffix><el-icon v-if="form.discovererName" class="rd-form-tip" style="cursor:pointer" @click.stop="clearDiscoverer"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="报告时间" prop="discoverTime"><el-date-picker v-model="form.discoverTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="责任部门" prop="respDeptName"><el-input v-model="form.respDeptName" readonly placeholder="选择报告人后自动带出" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c4">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">不合格品报告详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v0" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">NCR编号</span><div class="rd-value">{{ viewData.ncrNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value"><dict-tag :options="qms_ncr_source" :value="viewData.sourceType" /></div></div>
            <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ viewData.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ viewData.defectQty != null ? viewData.defectQty : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">报告人</span><div class="rd-value">{{ viewData.discovererName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">报告时间</span><div class="rd-value">{{ viewData.discoverTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value">{{ viewData.respDeptName || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>缺陷与处置</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v1" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">缺陷等级</span><div class="rd-value"><dict-tag :options="qms_defect_level" :value="viewData.defectLevel" /></div></div>
            <div class="rd-item"><span class="rd-label">处置方式</span><div class="rd-value"><dict-tag :options="qms_disposition" :value="viewData.disposition" /></div></div>
            <div class="rd-item"><span class="rd-label">是否隔离</span><div class="rd-value">{{ viewData.isolateFlag === '1' ? '是' : '否' }}</div></div>
            <div class="rd-item" v-if="viewData.isolateFlag === '1'"><span class="rd-label">隔离区域</span><div class="rd-value">{{ viewData.isolateArea || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ viewData.defectDesc || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>状态与验证</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v2" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">NCR状态</span><div class="rd-value">{{ ncrStatusLabel(viewData.ncrStatus) }}</div></div>
            <div class="rd-item" v-if="viewData.maxNode > 0"><span class="rd-label">审批进度</span><div class="rd-value">{{ viewData.currentNode }} / {{ viewData.maxNode }}</div></div>
            <div class="rd-item"><span class="rd-label">验证结果</span><div class="rd-value">{{ viewData.verifyResult === '1' ? '通过' : viewData.verifyResult === '2' ? '不通过' : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">验证人</span><div class="rd-value">{{ viewData.verifierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">验证时间</span><div class="rd-value">{{ viewData.verifyTime || '-' }}</div></div>
            <div class="rd-item rd-item--full" v-if="viewData.verifyRemark"><span class="rd-label">验证说明</span><div class="rd-value">{{ viewData.verifyRemark || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.approveLogList && viewData.approveLogList.length > 0">
          <div class="rd-card-header" @click="toggleCard('v3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v3" style="display:block; padding:0">
            <el-timeline style="padding: 20px 20px 20px 30px;">
              <el-timeline-item v-for="log in viewData.approveLogList" :key="log.logId" :timestamp="log.approveTime" placement="top" :type="log.actionType === '2' || log.actionType === '5' ? 'success' : log.actionType === '3' || log.actionType === '6' ? 'danger' : 'primary'">
                <div style="font-size: 14px; font-weight: 600; color: var(--ink-900);">{{ log.nodeName || '节点' + log.nodeSeq }}</div>
                <div style="font-size: 13px; color: var(--ink-500); margin-top: 4px;">{{ approveActionLabel(log.actionType) }} · {{ log.approverName }}</div>
                <div v-if="log.approveOpinion" style="font-size: 13px; color: var(--ink-700); margin-top: 4px;">{{ log.approveOpinion }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v4" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Approve Dialog (MRB审批) -->
    <el-dialog v-model="approveOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">MRB审批<el-tooltip content="MRB（Material Review Board）即物料评审委员会，由质量、生产、技术等部门共同组成，对不合格品的处置方案进行评审和决策" placement="top"><el-icon class="rd-form-tip" style="margin-left:6px"><QuestionFilled /></el-icon></el-tooltip></div>
            <div class="rd-detail-header-sub" v-if="approveForm.ncrNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">编号：{{ approveForm.ncrNo }}</span>
              <span class="badge" :class="ncrBadgeClass(approveForm.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(approveForm.ncrStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">NCR编号</span><div class="rd-value">{{ approveForm.ncrNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value"><dict-tag :options="qms_ncr_source" :value="approveForm.sourceType" /></div></div>
              <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ approveForm.sourceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ approveForm.batchNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告人</span><div class="rd-value">{{ approveForm.discovererName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告时间</span><div class="rd-value">{{ approveForm.discoverTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value">{{ approveForm.respDeptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">审批进度</span><div class="rd-value">{{ approveForm.currentNode || 0 }} / {{ approveForm.maxNode || 0 }}</div></div>
            </div>
          </div>
        </section>
        <!-- 物料与缺陷信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>物料与缺陷信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ approveForm.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ approveForm.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ approveForm.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ approveForm.defectQty != null ? approveForm.defectQty : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">缺陷等级</span><div class="rd-value"><span class="badge" :class="defectBadgeClass(approveForm.defectLevel)"><span class="dot"></span>{{ defectLevelLabel(approveForm.defectLevel) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">是否隔离</span><div class="rd-value">{{ approveForm.isolateFlag === '1' ? '是' : '否' }}</div></div>
              <div class="rd-item" v-if="approveForm.isolateFlag === '1'"><span class="rd-label">隔离区域</span><div class="rd-value">{{ approveForm.isolateArea || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ approveForm.defectDesc || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- MRB审批历史记录 -->
        <section class="rd-card" v-if="approveForm.approveLogList && approveForm.approveLogList.length > 0">
          <div class="rd-card-header" @click="toggleCard('a2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a2" style="display:block; padding:0">
            <div class="rd-timeline" style="padding: 20px 20px 20px 30px;">
              <div class="rd-timeline-item" v-for="log in approveForm.approveLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2' || log.actionType === '5', 'rd-timeline-dot--error': log.actionType === '3' || log.actionType === '6' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ log.nodeName || '节点' + log.nodeSeq }}</span>
                    <span class="rd-timeline-time">{{ log.approveTime || '-' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">操作类型</span><div class="rd-value">{{ approveActionLabel(log.actionType) }}</div></div>
                    <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.approverName || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.approveOpinion">
                    <strong>审批意见：</strong>{{ log.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 审批意见输入区 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('a3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.a3">
            <el-form ref="approveRef" :model="approveForm" :rules="approveRules" label-width="100px">
              <el-form-item label="审批意见" prop="approveOpinion">
                <el-input v-model="approveForm.approveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="danger" plain @click="confirmApprove(false)" v-hasPermi="['qms:ncr:approve']">驳 回</el-button>
        <el-button type="success" @click="confirmApprove(true)" v-hasPermi="['qms:ncr:approve']">通 过</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- Disposition Dialog (处置提交) -->
    <el-dialog v-model="dispositionOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">提交处置结论</div>
            <div class="rd-detail-header-sub" v-if="dispositionForm.ncrNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">编号：{{ dispositionForm.ncrNo }}</span>
              <span class="badge" :class="ncrBadgeClass(dispositionForm.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(dispositionForm.ncrStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">NCR编号</span><div class="rd-value">{{ dispositionForm.ncrNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value"><dict-tag :options="qms_ncr_source" :value="dispositionForm.sourceType" /></div></div>
              <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ dispositionForm.sourceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ dispositionForm.batchNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告人</span><div class="rd-value">{{ dispositionForm.discovererName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告时间</span><div class="rd-value">{{ dispositionForm.discoverTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value">{{ dispositionForm.respDeptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">NCR状态</span><div class="rd-value"><span class="badge" :class="ncrBadgeClass(dispositionForm.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(dispositionForm.ncrStatus) }}</span></div></div>
            </div>
          </div>
        </section>
        <!-- 物料与缺陷信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>物料与缺陷信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ dispositionForm.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ dispositionForm.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ dispositionForm.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ dispositionForm.defectQty != null ? dispositionForm.defectQty : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">缺陷等级</span><div class="rd-value"><span class="badge" :class="defectBadgeClass(dispositionForm.defectLevel)"><span class="dot"></span>{{ defectLevelLabel(dispositionForm.defectLevel) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">是否隔离</span><div class="rd-value">{{ dispositionForm.isolateFlag === '1' ? '是' : '否' }}</div></div>
              <div class="rd-item" v-if="dispositionForm.isolateFlag === '1'"><span class="rd-label">隔离区域</span><div class="rd-value">{{ dispositionForm.isolateArea || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ dispositionForm.defectDesc || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- MRB审批历史记录 -->
        <section class="rd-card" v-if="dispositionForm.approveLogList && dispositionForm.approveLogList.length > 0">
          <div class="rd-card-header" @click="toggleCard('d2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d2" style="display:block; padding:0">
            <div class="rd-timeline" style="padding: 20px 20px 20px 30px;">
              <div class="rd-timeline-item" v-for="log in dispositionForm.approveLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2' || log.actionType === '5', 'rd-timeline-dot--error': log.actionType === '3' || log.actionType === '6' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ log.nodeName || '节点' + log.nodeSeq }}</span>
                    <span class="rd-timeline-time">{{ log.approveTime || '-' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">操作类型</span><div class="rd-value">{{ approveActionLabel(log.actionType) }}</div></div>
                    <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.approverName || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.approveOpinion">
                    <strong>审批意见：</strong>{{ log.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 处置结论输入区 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('d3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>处置结论</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.d3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.d3">
            <el-form ref="dispositionRef" :model="dispositionForm" :rules="dispositionRules" label-width="100px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="处置方式" prop="disposition">
                    <el-select v-model="dispositionForm.disposition" style="width: 100%" placeholder="请选择处置方式"><el-option v-for="d in qms_disposition" :key="d.value" :label="d.label" :value="d.value" /></el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="处置数量" prop="disposeQty">
                    <el-input-number v-model="dispositionForm.disposeQty" :min="0" :precision="0" style="width: 100%" placeholder="处置数量" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="处置说明" prop="remark">
                <el-input v-model="dispositionForm.remark" type="textarea" :rows="4" placeholder="请输入处置说明，包括处置过程、结果、责任部门等信息" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><el-button type="primary" @click="submitDispositionForm">确 定</el-button><el-button @click="dispositionOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- Verify Dialog -->
    <el-dialog v-model="verifyOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">NCR验证</div>
            <div class="rd-detail-header-sub" v-if="verifyForm.ncrNo">
              <div class="rd-detail-header-divider"></div>
              <span class="rd-detail-header-no">编号：{{ verifyForm.ncrNo }}</span>
              <span class="badge" :class="ncrBadgeClass(verifyForm.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(verifyForm.ncrStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <!-- 基本信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vf0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">NCR编号</span><div class="rd-value">{{ verifyForm.ncrNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value"><dict-tag :options="qms_ncr_source" :value="verifyForm.sourceType" /></div></div>
              <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ verifyForm.sourceNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ verifyForm.batchNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告人</span><div class="rd-value">{{ verifyForm.discovererName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">报告时间</span><div class="rd-value">{{ verifyForm.discoverTime || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value">{{ verifyForm.respDeptName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">NCR状态</span><div class="rd-value"><span class="badge" :class="ncrBadgeClass(verifyForm.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(verifyForm.ncrStatus) }}</span></div></div>
            </div>
          </div>
        </section>
        <!-- 物料与缺陷信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>物料与缺陷信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vf1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ verifyForm.materialCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ verifyForm.materialName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ verifyForm.supplierName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ verifyForm.defectQty != null ? verifyForm.defectQty : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">缺陷等级</span><div class="rd-value"><span class="badge" :class="defectBadgeClass(verifyForm.defectLevel)"><span class="dot"></span>{{ defectLevelLabel(verifyForm.defectLevel) }}</span></div></div>
              <div class="rd-item"><span class="rd-label">是否隔离</span><div class="rd-value">{{ verifyForm.isolateFlag === '1' ? '是' : '否' }}</div></div>
              <div class="rd-item" v-if="verifyForm.isolateFlag === '1'"><span class="rd-label">隔离区域</span><div class="rd-value">{{ verifyForm.isolateArea || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ verifyForm.defectDesc || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 处置信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg></span>处置信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vf2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">处置方式</span><div class="rd-value"><dict-tag :options="qms_disposition" :value="verifyForm.disposition" /></div></div>
              <div class="rd-item"><span class="rd-label">处置数量</span><div class="rd-value">{{ verifyForm.disposeQty != null ? verifyForm.disposeQty : '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">处置说明</span><div class="rd-value">{{ verifyForm.disposeRemark || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- MRB审批历史记录 -->
        <section class="rd-card" v-if="verifyForm.approveLogList && verifyForm.approveLogList.length > 0">
          <div class="rd-card-header" @click="toggleCard('vf3')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf3 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vf3" style="display:block; padding:0">
            <div class="rd-timeline" style="padding: 20px 20px 20px 30px;">
              <div class="rd-timeline-item" v-for="log in verifyForm.approveLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.actionType === '2' || log.actionType === '5', 'rd-timeline-dot--error': log.actionType === '3' || log.actionType === '6' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">{{ log.nodeName || '节点' + log.nodeSeq }}</span>
                    <span class="rd-timeline-time">{{ log.approveTime || '-' }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">操作类型</span><div class="rd-value">{{ approveActionLabel(log.actionType) }}</div></div>
                    <div class="rd-item"><span class="rd-label">操作人</span><div class="rd-value">{{ log.approverName || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.approveOpinion">
                    <strong>审批意见：</strong>{{ log.approveOpinion }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 验证结论输入区 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vf4')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg></span>验证结论</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vf4 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vf4">
            <el-form ref="verifyRef" :model="verifyForm" :rules="verifyRules" label-width="100px">
              <el-form-item label="验证结果" prop="verifyResult">
                <el-radio-group v-model="verifyForm.verifyResult"><el-radio value="1">通过</el-radio><el-radio value="2">不通过</el-radio></el-radio-group>
              </el-form-item>
              <el-form-item label="验证说明" prop="verifyRemark">
                <el-input v-model="verifyForm.verifyRemark" type="textarea" :rows="4" placeholder="请输入验证说明，包括处置效果评估、是否满足要求、后续建议等" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer><el-button type="primary" @click="submitVerify">确 定</el-button><el-button @click="verifyOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- 物料选择器 -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- 人员选择器 -->
    <user-picker ref="userPickerRef" title="选择报告人" @confirm="onUserPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="NCR台账业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、NCR释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是NCR？</div>
          <div class="highlight-card-body">
            <strong>NCR（Non-Conformance Report，不合格品报告）</strong>是质量管理中用于记录和处置不合格品的工具。当来料检验（IQC）、过程检验（IPQC）、成品检验（FQC）或出货检验（OQC）判定为不合格，或客诉、巡检发现不合格品时，发起NCR进行<strong>登记 → MRB评审 → 处置 → 验证 → 关闭</strong>的全流程闭环管理。<br/><br/>
            NCR通过<strong>MRB（Material Review Board，物料评审委员会）</strong>对不合格品进行评审和处置决策，确保不合格品得到妥善处理，防止不合格品流入下一环节，满足ISO 9001、IATF 16949、GMP等质量管理体系要求。
          </div>
        </div>

        <h4>二、NCR状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item"><el-tag type="info">已登记</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">评审中</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="warning">处置中</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="primary">待验证</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="success">已关闭</el-tag></div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item"><el-tag type="warning">评审中</el-tag><el-icon class="flow-arrow"><ArrowRight /></el-icon></div>
          <div class="flow-item"><el-tag type="danger">已作废</el-tag></div>
        </div>

        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="已登记">NCR创建后的初始状态。可修改、删除，填写不合格品基本信息和缺陷描述。填写完成后点击「提交评审」进入评审中</el-descriptions-item>
          <el-descriptions-item label="评审中">MRB评审流程进行中。根据缺陷等级路由审批人：致命/严重→质量经理+责任部门主管审批；一般/轻微→QE审批。审批通过后进入处置中，驳回则退回已登记</el-descriptions-item>
          <el-descriptions-item label="处置中">评审通过，等待执行处置方案。可选择退货、挑选使用、返工/返修、报废、让步接收等处置方式，提交处置结论后进入待验证</el-descriptions-item>
          <el-descriptions-item label="待验证">处置执行完毕，等待QE验证处置效果。验证通过后关闭NCR，不通过则退回处置中</el-descriptions-item>
          <el-descriptions-item label="已关闭">NCR流程完成，隔离解除。关闭后不可修改，记录归档用于追溯</el-descriptions-item>
          <el-descriptions-item label="已作废">评审驳回后退回，可修改后重新提交评审</el-descriptions-item>
        </el-descriptions>

        <h4>四、MRB评审流程</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">MRB评审规则</div>
          <div class="highlight-card-body">
            <p>• <strong>致命/严重缺陷（等级1/2）：</strong>2个审批节点 — 质量经理审批 → 责任部门主管审批 → 处置执行</p>
            <p>• <strong>一般/轻微缺陷（等级3/4）：</strong>1个审批节点 — QE审批 → 处置执行</p>
            <p>• 每个审批节点记录审批人、审批意见、时间戳</p>
          </div>
        </div>

        <h4>五、处置方式说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="退货">将不合格品退回供应商，适用于来料检验不合格</el-descriptions-item>
          <el-descriptions-item label="挑选使用">对不合格品进行全数挑选，合格品继续使用，不合格品另行处理</el-descriptions-item>
          <el-descriptions-item label="返工/返修">对不合格品进行返工或返修，使其符合要求后继续使用</el-descriptions-item>
          <el-descriptions-item label="报废">不合格品无法修复或修复成本过高，直接报废处理</el-descriptions-item>
          <el-descriptions-item label="让步接收">在不影响最终产品质量的前提下，经质量经理特批后让步接收使用</el-descriptions-item>
        </el-descriptions>

        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>登记NCR：</strong>检验不合格或巡检/客诉发现不合格品后，点击「新增」创建NCR，填写来源类型、物料信息、缺陷描述等
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交评审：</strong>已登记状态下，点击「提交评审」启动MRB评审流程，系统根据缺陷等级自动路由审批人
          </el-timeline-item>
          <el-timeline-item type="primary" :hollow="true">
            <strong>MRB审批：</strong>评审中状态下，审批人点击「审批」进行审批操作，可选择通过或驳回
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>提交处置：</strong>审批通过后进入处置中，责任部门执行处置方案，点击「处置」提交处置结论
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>验证关闭：</strong>处置完成后进入待验证，QE点击「验证」验证处置效果。验证通过后可点击「关闭」完成NCR闭环
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsNcr">
import { listNcr, getNcr, addNcr, updateNcr, delNcr, verifyNcr, closeNcr, submitReview, approveNcr, rejectNcr, submitDisposition } from '@/api/qms/ncr'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight, CircleClose } from '@element-plus/icons-vue'
const { collapsedCards, toggleCard } = useDetailCard(['c0','c1','c2','c3','c4','v0','v1','v2','v3','v4','a0','a1','a2','a3','d0','d1','d2','d3','vf0','vf1','vf2','vf3','vf4'])
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_ncr_index')
const { qms_ncr_status, qms_disposition, qms_defect_level, qms_approve_action, qms_ncr_source } = proxy.useDict('qms_ncr_status', 'qms_disposition', 'qms_defect_level', 'qms_approve_action', 'qms_ncr_source')

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const verifyOpen = ref(false)
const verifyForm = ref({})
const approveOpen = ref(false)
const approveForm = ref({})
const dispositionOpen = ref(false)
const dispositionForm = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 })
const statusTabList = computed(() => qms_ncr_status.value)
const supplierOptions = ref([])
const materialPickerRef = ref()
const userPickerRef = ref()

const approveRules = {
  approveOpinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

const dispositionRules = {
  disposition: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  disposeQty: [{ required: true, message: '请输入处置数量', trigger: 'blur' }],
  remark: [{ required: true, message: '请输入处置说明', trigger: 'blur' }]
}

const verifyRules = {
  verifyResult: [{ required: true, message: '请选择验证结果', trigger: 'change' }],
  verifyRemark: [{ required: true, message: '请输入验证说明', trigger: 'blur' }]
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ncrNo: undefined, ncrStatus: undefined, sourceType: undefined, defectLevel: undefined, disposition: undefined, materialCode: undefined, sourceNo: undefined, discovererName: undefined, params: {} }
})
const { queryParams, form } = toRefs(data)

const rules = {
  sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }],
  materialCode: [{ required: true, message: '请选择物料', trigger: 'change' }],
  defectQty: [{ required: true, message: '请输入不合格数量', trigger: 'blur' }],
  defectLevel: [{ required: true, message: '请选择缺陷等级', trigger: 'change' }],
  disposition: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  discovererName: [{ required: true, message: '请选择报告人', trigger: 'change' }],
  discoverTime: [{ required: true, message: '请选择报告时间', trigger: 'change' }]
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ncrNo) count++
  if (queryParams.value.ncrStatus) count++
  if (queryParams.value.sourceType) count++
  if (queryParams.value.defectLevel) count++
  if (queryParams.value.disposition) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.sourceNo) count++
  if (queryParams.value.discovererName) count++
  return count
})

function getList() {
  loading.value = true
  listNcr(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
    applySavedWidths()
  })
}

function handleQuery() {
  showAdvanced.value = false
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.ncrNo = undefined
  queryParams.value.ncrStatus = undefined
  queryParams.value.sourceType = undefined
  queryParams.value.defectLevel = undefined
  queryParams.value.disposition = undefined
  queryParams.value.materialCode = undefined
  queryParams.value.sourceNo = undefined
  queryParams.value.discovererName = undefined
  queryParams.value.params = {}
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增NCR'
}

function handleUpdate(row) {
  const ncrId = row?.ncrId || ids.value[0]
  getNcr(ncrId).then(res => {
    data.form = res.data
    open.value = true
    title.value = '修改NCR'
  })
}

function handleView(row) {
  getNcr(row.ncrId).then(res => {
    viewData.value = res.data
    viewOpen.value = true
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.ncrId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function submitForm() {
  proxy.$refs['ncrRef'].validate(valid => {
    if (valid) {
      if (form.value.ncrId) {
        updateNcr(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addNcr(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const delIds = row?.ncrId ? [row.ncrId] : ids.value
  proxy.$modal.confirm('确认删除编号为"' + delIds + '"的数据项？').then(() => delNcr(delIds.join(','))).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('qms/ncr/export', { ...queryParams.value }, `ncr_${new Date().getTime()}.xlsx`)
}

function handleSubmitReview(row) {
  proxy.$modal.confirm('确认提交NCR "' + row.ncrNo + '"进入MRB评审？').then(() => submitReview(row.ncrId)).then(() => {
    getList()
    proxy.$modal.msgSuccess('提交评审成功')
  }).catch(() => {})
}

function handleApprove(row) {
  getNcr(row.ncrId).then(res => {
    approveForm.value = { ...res.data, approveOpinion: undefined }
    approveOpen.value = true
  })
}

function confirmApprove(passed) {
  proxy.$refs['approveRef'].validate(valid => {
    if (valid) {
      if (passed) {
        approveNcr(approveForm.value.ncrId, approveForm.value.approveOpinion).then(() => {
          proxy.$modal.msgSuccess('审批通过')
          approveOpen.value = false
          getList()
        })
      } else {
        rejectNcr(approveForm.value.ncrId, approveForm.value.approveOpinion).then(() => {
          proxy.$modal.msgSuccess('已驳回')
          approveOpen.value = false
          getList()
        })
      }
    }
  })
}

function handleDisposition(row) {
  getNcr(row.ncrId).then(res => {
    dispositionForm.value = { ...res.data, remark: undefined, disposeQty: res.data.defectQty }
    dispositionOpen.value = true
  })
}

function submitDispositionForm() {
  proxy.$refs['dispositionRef'].validate(valid => {
    if (valid) {
      submitDisposition({
        ncrId: dispositionForm.value.ncrId,
        disposition: dispositionForm.value.disposition,
        disposeQty: dispositionForm.value.disposeQty,
        remark: dispositionForm.value.remark
      }).then(() => {
        proxy.$modal.msgSuccess('处置结论已提交')
        dispositionOpen.value = false
        getList()
      })
    }
  })
}

function handleVerify(row) {
  getNcr(row.ncrId).then(res => {
    verifyForm.value = { ...res.data, verifyResult: undefined, verifyRemark: undefined }
    verifyOpen.value = true
  })
}

function submitVerify() {
  proxy.$refs['verifyRef'].validate(valid => {
    if (valid) {
      verifyNcr({
        ncrId: verifyForm.value.ncrId,
        verifyResult: verifyForm.value.verifyResult,
        verifyRemark: verifyForm.value.verifyRemark
      }).then(() => {
        proxy.$modal.msgSuccess('验证已提交')
        verifyOpen.value = false
        getList()
      })
    }
  })
}

function handleClose(row) {
  proxy.$modal.confirm('确认关闭NCR "' + row.ncrNo + '"？关闭后不可修改。').then(() => closeNcr(row.ncrId)).then(() => {
    getList()
    proxy.$modal.msgSuccess('关闭成功')
  }).catch(() => {})
}

function reset() {
  form.value = {
    ncrId: null,
    ncrNo: undefined,
    sourceType: undefined,
    sourceNo: undefined,
    materialCode: undefined,
    materialName: undefined,
    supplierId: undefined,
    supplierName: undefined,
    batchNo: undefined,
    defectQty: undefined,
    defectLevel: undefined,
    disposition: undefined,
    isolateFlag: '0',
    isolateArea: undefined,
    defectDesc: undefined,
    discovererName: undefined,
    discoverTime: undefined,
    respDeptId: undefined,
    respDeptName: undefined,
    ncrStatus: '0',
    remark: undefined
  }
  proxy.resetForm('ncrRef')
}

function cancel() {
  open.value = false
  reset()
}

/* ===== 字典标签辅助函数 ===== */
function ncrStatusLabel(val) {
  const item = qms_ncr_status.value.find(d => d.value == val)
  return item ? item.label : '-'
}

function ncrBadgeClass(val) {
  if (val == '0') return 'orange'
  if (val == '1') return 'blue'
  if (val == '2') return 'blue'
  if (val == '3') return 'green'
  if (val == '4') return 'gray'
  if (val == '5') return 'red'
  return 'gray'
}

function defectLevelLabel(val) {
  const item = qms_defect_level.value.find(d => d.value == val)
  return item ? item.label : '-'
}

function defectBadgeClass(val) {
  if (val == '1') return 'red'
  if (val == '2') return 'orange'
  if (val == '3') return 'blue'
  if (val == '4') return 'gray'
  return 'gray'
}

function approveActionLabel(val) {
  const item = qms_approve_action.value.find(d => d.value == val)
  return item ? item.label : '-'
}

/* ===== 状态标签栏 ===== */
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-progress', '2': 'tab-disposition', '3': 'tab-verifying', '4': 'tab-closed', '5': 'tab-reject' }
  return map[value] || ''
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.ncrStatus = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  listNcr({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0, '4': 0, '5': 0 }
    ;(res.rows || []).forEach(r => {
      if (counts[r.ncrStatus] !== undefined) counts[r.ncrStatus]++
    })
    statusCounts.value = counts
  }).catch(() => {})
}

/* ===== 物料选择器 ===== */
function openMaterialPicker() {
  materialPickerRef.value.open(form.value.materialCode)
}

function onMaterialPickerConfirm(material) {
  form.value.materialCode = material.materialCode
  form.value.materialName = material.materialName
}

function clearMaterial() {
  form.value.materialCode = undefined
  form.value.materialName = undefined
}

/* ===== 供应商选择 ===== */
function loadSupplierOptions() {
  listSupplier({ pageNum: 1, pageSize: 999, status: '0' }).then(res => {
    supplierOptions.value = res.rows || []
  }).catch(() => {})
}

function onSupplierChange(val) {
  const supplier = supplierOptions.value.find(s => s.supplierId === val)
  form.value.supplierName = supplier ? supplier.supplierName : undefined
}

/* ===== 报告人选择器 ===== */
function openUserPicker() {
  userPickerRef.value.open()
}

function onUserPickerConfirm(user) {
  form.value.discovererName = user.nickName
  form.value.respDeptId = user.deptId
  form.value.respDeptName = user.deptName
}

function clearDiscoverer() {
  form.value.discovererName = undefined
  form.value.respDeptId = undefined
  form.value.respDeptName = undefined
}

/* ===== 初始化 ===== */
loadSupplierOptions()
getList()
</script>

<style scoped>
.qms-ncr-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-ncr-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-ncr-page .filter-card { padding:14px 20px 16px; }
.qms-ncr-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-ncr-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-ncr-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-ncr-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-ncr-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-ncr-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-ncr-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-ncr-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-ncr-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-ncr-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-ncr-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-ncr-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-ncr-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-ncr-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-ncr-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-ncr-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-ncr-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-ncr-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-ncr-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-ncr-page .field .control :deep(.el-select) { width:100%; }
.qms-ncr-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-ncr-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-ncr-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-ncr-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-ncr-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-ncr-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-ncr-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-ncr-page .btn-soft .el-icon { font-size:14px; }
.qms-ncr-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-ncr-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-ncr-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-ncr-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-ncr-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-ncr-page .table-wrap { overflow-x:auto; }
.qms-ncr-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-ncr-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-ncr-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-ncr-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-ncr-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-ncr-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-ncr-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-ncr-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-ncr-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-ncr-page .badge.green .dot { background:var(--green-500); }
.qms-ncr-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-ncr-page .badge.gray .dot { background:var(--ink-400); }
.qms-ncr-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-ncr-page .badge.red .dot { background:var(--red-500); }
.qms-ncr-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-ncr-page .badge.orange .dot { background:#f59e0b; }
.qms-ncr-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-ncr-page .badge.blue .dot { background:var(--brand-500); }
.qms-ncr-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-ncr-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-ncr-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.qms-ncr-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.qms-ncr-page .rd-form-tip:hover { color: #909399; }
/* ===== Status Tabs ===== */
.qms-ncr-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.qms-ncr-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.qms-ncr-page .tabs-track::-webkit-scrollbar { display:none; }
.qms-ncr-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.qms-ncr-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.qms-ncr-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.qms-ncr-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.qms-ncr-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.qms-ncr-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.qms-ncr-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 已登记 - orange */
.qms-ncr-page .status-tab.tab-draft .dot { background:#f59e0b; }
.qms-ncr-page .status-tab.tab-draft .count { background:#fffbeb; color:#b45309; }
.qms-ncr-page .status-tab.is-active.tab-draft .count { background:#f59e0b; color:#fff; }
/* 评审中 - brand/blue */
.qms-ncr-page .status-tab.tab-progress .dot { background:var(--brand-500); }
.qms-ncr-page .status-tab.tab-progress .count { background:var(--brand-50); color:var(--brand-700); }
.qms-ncr-page .status-tab.is-active.tab-progress .count { background:var(--brand-500); color:#fff; }
/* 处置中 - amber */
.qms-ncr-page .status-tab.tab-disposition .dot { background:#f59e0b; }
.qms-ncr-page .status-tab.tab-disposition .count { background:#fffbeb; color:#b45309; }
.qms-ncr-page .status-tab.is-active.tab-disposition .count { background:#f59e0b; color:#fff; }
/* 待验证 - green */
.qms-ncr-page .status-tab.tab-verifying .dot { background:var(--green-500); }
.qms-ncr-page .status-tab.tab-verifying .count { background:var(--green-50); color:var(--green-700); }
.qms-ncr-page .status-tab.is-active.tab-verifying .count { background:var(--green-500); color:#fff; }
/* 已关闭 - gray */
.qms-ncr-page .status-tab.tab-closed .dot { background:var(--ink-400); }
.qms-ncr-page .status-tab.tab-closed .count { background:var(--ink-100); color:var(--ink-500); }
.qms-ncr-page .status-tab.is-active.tab-closed .count { background:var(--ink-400); color:#fff; }
/* 已作废 - red */
.qms-ncr-page .status-tab.tab-reject .dot { background:var(--red-500); }
.qms-ncr-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); }
.qms-ncr-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
/* ===== Tip Pill ===== */
.qms-ncr-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-ncr-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
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
@media (max-width:720px) { .qms-ncr-page .toolbar { flex-wrap:wrap; gap:10px; } .qms-ncr-page .status-tabs { padding:6px 8px; } }
</style>
