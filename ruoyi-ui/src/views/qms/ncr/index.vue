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
          <label>缺陷等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.defectLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
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
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>来源单号</label>
          <div class="control">
            <el-input v-model="queryParams.sourceNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:ncr:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:ncr:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:ncr:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
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
          <el-table-column label="来源类型" prop="sourceType" :width="colWidth('sourceType', 100)" resizable align="center" />
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
            <template #header><span>NCR状态</span><el-tooltip content="不合格品报告的流程状态，包括：待提交、待评审、审批中、待处置、待验证、已关闭" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge" :class="ncrBadgeClass(scope.row.ncrStatus)"><span class="dot"></span>{{ ncrStatusLabel(scope.row.ncrStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="发现人" prop="discovererName" :width="colWidth('discovererName', 100)" resizable align="center" />
          <el-table-column label="发现时间" prop="discoverTime" :width="colWidth('discoverTime', 160)" resizable align="center" />
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
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item prop="ncrNo"><template #label><span>NCR编号</span><el-tooltip content="NCR（Non-Conformance Report）即不合格品报告编号，用于标识和追溯不合格品的全流程处理记录" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.ncrNo" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="来源类型" prop="sourceType"><el-input v-model="form.sourceType" placeholder="如：检验/投诉/巡检" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="来源单号" prop="sourceNo"><el-input v-model="form.sourceNo" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="不合格数量" prop="defectQty"><el-input-number v-model="form.defectQty" :min="0" :precision="0" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>缺陷与处置</div></div>
            <div class="rd-card-body">
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
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>发现与责任</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="发现人" prop="discovererName"><el-input v-model="form.discovererName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发现时间" prop="discoverTime"><el-date-picker v-model="form.discoverTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="责任部门" prop="respDeptName"><el-input v-model="form.respDeptName" placeholder="请输入" /></el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
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
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">NCR编号</span><div class="rd-value">{{ viewData.ncrNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value">{{ viewData.sourceType || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ viewData.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">不合格数量</span><div class="rd-value">{{ viewData.defectQty != null ? viewData.defectQty : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">发现人</span><div class="rd-value">{{ viewData.discovererName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">发现时间</span><div class="rd-value">{{ viewData.discoverTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">责任部门</span><div class="rd-value">{{ viewData.respDeptName || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></span>缺陷与处置</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">缺陷等级</span><div class="rd-value"><dict-tag :options="qms_defect_level" :value="viewData.defectLevel" /></div></div>
            <div class="rd-item"><span class="rd-label">处置方式</span><div class="rd-value"><dict-tag :options="qms_disposition" :value="viewData.disposition" /></div></div>
            <div class="rd-item"><span class="rd-label">是否隔离</span><div class="rd-value">{{ viewData.isolateFlag === '1' ? '是' : '否' }}</div></div>
            <div class="rd-item" v-if="viewData.isolateFlag === '1'"><span class="rd-label">隔离区域</span><div class="rd-value">{{ viewData.isolateArea || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">缺陷描述</span><div class="rd-value">{{ viewData.defectDesc || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>状态与验证</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">NCR状态</span><div class="rd-value">{{ ncrStatusLabel(viewData.ncrStatus) }}</div></div>
            <div class="rd-item" v-if="viewData.maxNode > 0"><span class="rd-label">审批进度</span><div class="rd-value">{{ viewData.currentNode }} / {{ viewData.maxNode }}</div></div>
            <div class="rd-item"><span class="rd-label">验证结果</span><div class="rd-value">{{ viewData.verifyResult === '1' ? '通过' : viewData.verifyResult === '2' ? '不通过' : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">验证人</span><div class="rd-value">{{ viewData.verifierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">验证时间</span><div class="rd-value">{{ viewData.verifyTime || '-' }}</div></div>
            <div class="rd-item rd-item--full" v-if="viewData.verifyRemark"><span class="rd-label">验证说明</span><div class="rd-value">{{ viewData.verifyRemark || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.approveLogList && viewData.approveLogList.length > 0">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>MRB审批流程</div></div>
          <div class="rd-card-body" style="display:block; padding:0">
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
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Approve Dialog (MRB审批) -->
    <el-dialog v-model="approveOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><span class="rd-detail-header-title">MRB审批<el-tooltip content="MRB（Material Review Board）即物料评审委员会，由质量、生产、技术等部门共同组成，对不合格品的处置方案进行评审和决策" placement="top"><el-icon class="rd-form-tip" style="margin-left:6px"><QuestionFilled /></el-icon></el-tooltip></span></div></template>
      <div style="margin-bottom: 12px; padding: 12px 16px; background: var(--el-fill-color-light); border-radius: 6px; font-size: 14px; color: var(--el-text-color-secondary);">
        NCR编号：<b>{{ approveForm.ncrNo }}</b> &nbsp;|&nbsp; 物料：<b>{{ approveForm.materialName }}</b> &nbsp;|&nbsp; 批次：<b>{{ approveForm.batchNo }}</b><br>
        缺陷等级：<b>{{ defectLevelLabel(approveForm.defectLevel) }}</b> &nbsp;|&nbsp; 审批进度：<b>{{ approveForm.currentNode }} / {{ approveForm.maxNode }}</b>
      </div>
      <el-form ref="approveRef" :model="approveForm" label-width="100px">
        <el-form-item label="审批意见" prop="approveOpinion">
          <el-input v-model="approveForm.approveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="danger" plain @click="confirmApprove(false)" v-hasPermi="['qms:ncr:approve']">驳 回</el-button>
        <el-button type="success" @click="confirmApprove(true)" v-hasPermi="['qms:ncr:approve']">通过</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- Disposition Dialog (处置提交) -->
    <el-dialog v-model="dispositionOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><span class="rd-detail-header-title">提交处置结论</span></div></template>
      <div style="margin-bottom: 12px; padding: 12px 16px; background: var(--el-fill-color-light); border-radius: 6px; font-size: 14px; color: var(--el-text-color-secondary);">
        NCR编号：<b>{{ dispositionForm.ncrNo }}</b> &nbsp;|&nbsp; 物料：<b>{{ dispositionForm.materialName }}</b> &nbsp;|&nbsp; 批次：<b>{{ dispositionForm.batchNo }}</b>
      </div>
      <el-form ref="dispositionRef" :model="dispositionForm" :rules="dispositionRules" label-width="100px">
        <el-form-item label="处置方式" prop="disposition">
          <el-select v-model="dispositionForm.disposition" style="width: 100%"><el-option v-for="d in qms_disposition" :key="d.value" :label="d.label" :value="d.value" /></el-select>
        </el-form-item>
        <el-form-item label="处置说明" prop="remark">
          <el-input v-model="dispositionForm.remark" type="textarea" :rows="4" placeholder="请输入处置说明" />
        </el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitDispositionForm">确 定</el-button><el-button @click="dispositionOpen = false">取 消</el-button></template>
    </el-dialog>

    <!-- Verify Dialog -->
    <el-dialog v-model="verifyOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><span class="rd-detail-header-title">NCR验证</span></div></template>
      <div style="margin-bottom: 12px; padding: 12px 16px; background: var(--el-fill-color-light); border-radius: 6px; font-size: 14px; color: var(--el-text-color-secondary);">
        NCR编号：<b>{{ verifyForm.ncrNo }}</b> &nbsp;|&nbsp; 物料：<b>{{ verifyForm.materialName }}</b> &nbsp;|&nbsp; 批次：<b>{{ verifyForm.batchNo }}</b>
      </div>
      <el-form ref="verifyRef" :model="verifyForm" :rules="verifyRules" label-width="100px">
        <el-form-item label="验证结果" prop="verifyResult">
          <el-radio-group v-model="verifyForm.verifyResult"><el-radio value="1">通过</el-radio><el-radio value="2">不通过</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="验证说明" prop="verifyRemark">
          <el-input v-model="verifyForm.verifyRemark" type="textarea" :rows="4" placeholder="请输入验证说明" />
        </el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitVerify">确 定</el-button><el-button @click="verifyOpen = false">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsNcr">
import { listNcr, getNcr, addNcr, updateNcr, delNcr, verifyNcr, closeNcr, submitReview, approveNcr, rejectNcr, submitDisposition } from '@/api/qms/ncr'
import { useColumnResize } from '@/composables/useColumnResize'
import { QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_ncr_index')
const { qms_ncr_status, qms_disposition, qms_defect_level, qms_approve_action } = proxy.useDict('qms_ncr_status', 'qms_disposition', 'qms_defect_level', 'qms_approve_action')

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

const dispositionRules = {
  disposition: [{ required: true, message: '请选择处置方式', trigger: 'change' }]
}

const verifyRules = {
  verifyResult: [{ required: true, message: '请选择验证结果', trigger: 'change' }],
  verifyRemark: [{ required: true, message: '验证说明不能为空', trigger: 'blur' }]
}

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, ncrNo: undefined, sourceType: undefined, sourceNo: undefined, materialCode: undefined, ncrStatus: undefined, defectLevel: undefined, disposition: undefined, params: {} },
  rules: {
    ncrNo: [{ required: true, message: 'NCR编号不能为空', trigger: 'blur' }],
    materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
    defectLevel: [{ required: true, message: '缺陷等级不能为空', trigger: 'change' }],
    disposition: [{ required: true, message: '处置方式不能为空', trigger: 'change' }],
    defectQty: [{ required: true, message: '不合格数量不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.ncrNo) count++
  if (queryParams.value.ncrStatus) count++
  if (queryParams.value.defectLevel) count++
  if (queryParams.value.disposition) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.sourceNo) count++
  return count
})

function ncrStatusLabel(val) { const item = qms_ncr_status.value.find(d => d.value == val); return item ? item.label : '-' }
function ncrBadgeClass(val) { if (val == '0') return 'gray'; if (val == '1' || val == '2') return 'orange'; if (val == '3') return 'blue'; if (val == '4') return 'green'; if (val == '5') return 'red'; return 'gray' }
function defectLevelLabel(val) { const item = qms_defect_level.value.find(d => d.value == val); return item ? item.label : '-' }
function defectBadgeClass(val) { if (val == '1' || val == '2') return 'red'; if (val == '3') return 'orange'; return 'gray' }

function getList() { loading.value = true; listNcr(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.ncrNo = undefined; queryParams.value.sourceType = undefined; queryParams.value.sourceNo = undefined; queryParams.value.materialCode = undefined; queryParams.value.ncrStatus = undefined; queryParams.value.defectLevel = undefined; queryParams.value.disposition = undefined; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.ncrId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { ncrId: undefined, ncrNo: undefined, sourceType: undefined, sourceId: undefined, sourceNo: undefined, taskId: undefined, materialId: undefined, materialCode: undefined, materialName: undefined, supplierId: undefined, supplierName: undefined, batchNo: undefined, defectQty: undefined, defectLevel: undefined, defectDesc: undefined, disposition: undefined, isolateFlag: '0', isolateArea: undefined, ncrStatus: '0', discovererId: undefined, discovererName: undefined, discoverTime: undefined, respDeptId: undefined, respDeptName: undefined, status: '0', remark: undefined }; proxy.resetForm('ncrRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加不合格品报告' }
function handleUpdate(row) { reset(); getNcr(row.ncrId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改不合格品报告' }) }
function handleView(row) { getNcr(row.ncrId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function approveActionLabel(val) { const item = qms_approve_action.value.find(d => d.value == val); return item ? item.label : '-' }
function handleSubmitReview(row) { proxy.$modal.confirm('确认提交NCR"' + row.ncrNo + '"进行MRB评审？').then(() => submitReview(row.ncrId)).then(() => { proxy.$modal.msgSuccess('已提交评审'); getList() }).catch(() => {}) }
function handleApprove(row) { getNcr(row.ncrId).then(res => { approveForm.value = { ...res.data, approveOpinion: '' }; approveOpen.value = true }) }
function confirmApprove(passed) { if (!approveForm.value.approveOpinion) { proxy.$modal.msgWarning('请输入审批意见'); return }; if (passed) { approveNcr(approveForm.value.ncrId, approveForm.value.approveOpinion).then(() => { proxy.$modal.msgSuccess('审批通过'); approveOpen.value = false; getList() }) } else { rejectNcr(approveForm.value.ncrId, approveForm.value.approveOpinion).then(() => { proxy.$modal.msgSuccess('已驳回'); approveOpen.value = false; getList() }) } }
function handleDisposition(row) { getNcr(row.ncrId).then(res => { dispositionForm.value = { ncrId: res.data.ncrId, ncrNo: res.data.ncrNo, materialName: res.data.materialName, batchNo: res.data.batchNo, disposition: res.data.disposition || '', remark: '' }; dispositionOpen.value = true }) }
function submitDispositionForm() { proxy.$refs['dispositionRef'].validate(valid => { if (valid) { submitDisposition(dispositionForm.value).then(() => { proxy.$modal.msgSuccess('处置结论已提交'); dispositionOpen.value = false; getList() }) } }) }
function handleVerify(row) { verifyForm.value = { ncrId: row.ncrId, ncrNo: row.ncrNo, materialName: row.materialName, batchNo: row.batchNo, verifyResult: '1', verifyRemark: '' }; verifyOpen.value = true }
function submitVerify() { proxy.$refs['verifyRef'].validate(valid => { if (valid) { verifyNcr(verifyForm.value).then(() => { proxy.$modal.msgSuccess('验证成功'); verifyOpen.value = false; getList() }) } }) }
function handleClose(row) { proxy.$modal.confirm('确认关闭NCR"' + row.ncrNo + '"？').then(() => closeNcr(row.ncrId)).then(() => { proxy.$modal.msgSuccess('关闭成功'); getList() }).catch(() => {}) }
function submitForm() { proxy.$refs['ncrRef'].validate(valid => { if (valid) { if (form.value.ncrId != undefined) { updateNcr(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addNcr(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const ncrIds = row.ncrId || ids.value; proxy.$modal.confirm('确认删除编号为"' + ncrIds + '"的数据？').then(() => delNcr(ncrIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/ncr/export', { ...queryParams.value }, `ncr_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

getList()
onActivated(() => { getList() })
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
</style>
