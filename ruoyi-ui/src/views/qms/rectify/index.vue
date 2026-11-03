<template>
  <div class="app-container qms-rectify-page">
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
          <label>整改单编号</label>
          <div class="control">
            <el-input v-model="queryParams.rectifyNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>客诉编号</label>
          <div class="control">
            <el-input v-model="queryParams.complaintNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.rectifyStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in statusOptions" :key="d.value" :label="d.label" :value="d.value" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:rectify:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:rectify:edit']">
            <el-icon><Edit /></el-icon> 回复
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:rectify:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:rectify:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_rectify_columns" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" align="center" resizable />
          <el-table-column label="整改单编号" prop="rectifyNo" key="rectifyNo" :width="colWidth('rectifyNo', 170)" resizable show-overflow-tooltip v-if="columns.rectifyNo.visible" />
          <el-table-column label="客诉编号" prop="complaintNo" key="complaintNo" :width="colWidth('complaintNo', 160)" resizable show-overflow-tooltip v-if="columns.complaintNo.visible" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :min-width="colWidth('supplierName', 180)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="物料" prop="materialName" key="materialName" :width="colWidth('materialName', 150)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 130)" resizable show-overflow-tooltip v-if="columns.batchNo.visible" />
          <el-table-column label="回复时限" prop="replyDeadline" key="replyDeadline" :width="colWidth('replyDeadline', 110)" resizable align="center" v-if="columns.replyDeadline.visible"><template #default="scope"><span>{{ parseTime(scope.row.replyDeadline, '{y}-{m}-{d}') }}</span></template></el-table-column>
          <el-table-column label="状态" prop="rectifyStatus" key="rectifyStatus" :width="colWidth('rectifyStatus', 110)" resizable align="center" v-if="columns.rectifyStatus.visible"><template #default="scope"><span v-if="dictLabel(statusOptions, scope.row.rectifyStatus)" class="badge" :class="statusClass(scope.row.rectifyStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, scope.row.rectifyStatus) }}</span></template></el-table-column>
          <el-table-column label="回复时间" prop="replyTime" key="replyTime" :width="colWidth('replyTime', 150)" resizable align="center" v-if="columns.replyTime.visible"><template #default="scope"><span>{{ scope.row.replyTime ? parseTime(scope.row.replyTime) : '-' }}</span></template></el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 200)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:rectify:edit']" v-if="scope.row.rectifyStatus === '0'">回复</el-button>
                <el-button link type="info" icon="CircleClose" @click="handleClose(scope.row)" v-hasPermi="['qms:rectify:edit']" v-if="scope.row.rectifyStatus === '1' || scope.row.rectifyStatus === '2'">关闭</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['qms:rectify:remove']" v-if="scope.row.rectifyStatus === '0'">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">整改单详情</div>
            <div class="rd-detail-header-sub" v-if="viewData?.rectifyNo">
              <span class="rd-detail-header-no">编号：{{ viewData.rectifyNo }}</span>
              <span v-if="dictLabel(statusOptions, viewData?.rectifyStatus)" class="badge badge-on-dark" :class="statusClass(viewData?.rectifyStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, viewData?.rectifyStatus) }}</span>
            </div>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="viewData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc0')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>整改单信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc0"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">整改单编号</span><div class="rd-value">{{ viewData.rectifyNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">客诉编号</span><div class="rd-value">{{ viewData.complaintNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><span v-if="dictLabel(statusOptions, viewData.rectifyStatus)" class="badge" :class="statusClass(viewData.rectifyStatus)"><span class="dot"></span>{{ dictLabel(statusOptions, viewData.rectifyStatus) }}</span><span v-else>-</span></div></div>
            <div class="rd-item"><span class="rd-label">回复时限</span><div class="rd-value">{{ viewData.replyDeadline ? parseTime(viewData.replyDeadline, '{y}-{m}-{d}') : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">回复时间</span><div class="rd-value">{{ viewData.replyTime ? parseTime(viewData.replyTime) : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>来源客诉（供应商与产品）</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc1"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('rc2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>整改与回复</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.rc2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.rc2"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">整改要求</span><div class="rd-value">{{ viewData.rectifyRequirement || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">整改结果</span><div class="rd-value">{{ viewData.rectifyResult || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">供应商回复</span><div class="rd-value">{{ viewData.supplierReply || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">整改证据附件</span><div class="rd-value">{{ viewData.evidenceAttachment || '-' }}</div></div>
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="943px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-1 1z"/></svg></div>
          <div class="rd-detail-header-main">
            <div class="rd-detail-header-title">{{ title }}</div>
            <div class="rd-detail-header-sub">
              <span class="rd-detail-header-no" v-if="form.complaintNo">客诉：{{ form.complaintNo }} · {{ form.supplierName }}</span>
              <span class="rd-detail-header-placeholder" v-else>选择客诉后自动带出供应商信息</span>
            </div>
          </div>
        </div>
      </template>
      <el-form ref="rectifyRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec0')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>来源客诉</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec0 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec0">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom:12px"><template #title>整改单面向供应商责任：客诉/供应商/物料/批次从客诉单带出（KR-01）；逾期未回复由系统自动升级提醒采购负责人（KR-03）</template></el-alert>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="客诉" prop="complaintId">
                  <el-select v-model="form.complaintId" filterable clearable placeholder="请选择客诉（处理中/待确认且已关联供应商）" style="width: 100%" :disabled="!!form.rectifyId" @change="onComplaintChange">
                    <el-option v-for="c in complaintOptions" :key="c.complaintId" :label="c.complaintNo + ' · ' + (c.supplierName || '')" :value="c.complaintId" />
                  </el-select>
                </el-form-item></el-col>
                <el-col :span="12"><el-form-item label="供应商"><el-input v-model="form.supplierName" readonly placeholder="选择客诉后自动带出" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料"><el-input v-model="form.materialName" readonly placeholder="选择客诉后自动带出" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次号"><el-input v-model="form.batchNo" readonly placeholder="选择客诉后自动带出" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('ec1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>整改要求</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec1 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec1">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="回复时限" prop="replyDeadline"><el-date-picker v-model="form.replyDeadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态"><el-input :model-value="statusLabelOf(form.rectifyStatus)" disabled /></el-form-item></el-col>
              </el-row>
              <el-form-item label="整改要求" prop="rectifyRequirement"><el-input v-model="form.rectifyRequirement" type="textarea" :rows="3" placeholder="请填写对供应商的整改要求" /></el-form-item>
            </div>
          </section>
          <section class="rd-card" v-if="form.rectifyId">
            <div class="rd-card-header" @click="toggleCard('ec2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>供应商回复与整改</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.ec2 }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.ec2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="整改结果"><el-input v-model="form.rectifyResult" type="textarea" :rows="2" placeholder="请输入整改结果" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="供应商回复"><el-input v-model="form.supplierReply" type="textarea" :rows="2" placeholder="供应商首次回复后状态自动置为已回复" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="整改证据附件"><el-input v-model="form.evidenceAttachment" placeholder="请输入整改证据附件地址" /></el-form-item>
              <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>
    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="供应商整改单业务操作说明" width="943px" append-to-body>
      <div class="status-help-content">
        <h4>一、整改单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是供应商整改单？</div>
          <div class="highlight-card-body">
            <strong>供应商整改单（Supplier Rectification）</strong>是客诉判定为「供应商责任」后，向供应商发起的整改跟踪单据：明确整改要求与回复时限，供应商在时限内回复整改结果与证据，逾期未回复由系统自动升级提醒采购负责人（KR-03），实现"客户—产品—批次—供应商"全链路责任追溯。整改单面向供应商责任处理，客诉/供应商/物料/批次信息从客诉单带出（KR-01 数据源约束）。
          </div>
        </div>
        <h4>二、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待回复</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已回复</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">逾期升级</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已关闭</el-tag>
          </div>
        </div>
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待回复">整改单发起后的初始状态。可修改、删除。供应商在时限内回复后进入已回复</el-descriptions-item>
          <el-descriptions-item label="已回复">供应商已回复整改结果与证据，可填写整改结果并关闭</el-descriptions-item>
          <el-descriptions-item label="逾期升级">回复时限已过，系统自动升级提醒采购负责人（KR-03），仍可回复</el-descriptions-item>
          <el-descriptions-item label="已关闭">整改流程完成，记录归档用于供应商考核（投诉率 = 投诉次数/供货批次）</el-descriptions-item>
        </el-descriptions>
        <h4>四、字段填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">整改单信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>客诉：</strong>发起整改单的来源客诉，需为处理中/待确认且已关联供应商的客诉<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>供应商/物料/批次号：</strong>选择客诉后自动带出，不可修改</p>
            <p>• <strong>回复时限：</strong>要求供应商回复整改结果的最后期限，逾期系统自动升级提醒<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>整改要求：</strong>对供应商的整改内容与要求<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">供应商回复区（回复时填写）</div>
          <div class="highlight-card-body">
            <p>• <strong>整改结果：</strong>供应商整改完成情况说明</p>
            <p>• <strong>供应商回复：</strong>供应商首次回复后状态自动置为已回复</p>
            <p>• <strong>整改证据附件：</strong>整改完成的证据材料地址</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsRectify">
import { listRectify, getRectify, addRectify, updateRectify, delRectify, closeRectify } from '@/api/qms/rectify'
import { listComplaint } from '@/api/qms/complaint'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { QuestionFilled, ArrowRight } from '@element-plus/icons-vue'
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_rectify_index')
const { collapsedCards, toggleCard } = useDetailCard(['rc0','rc1','rc2','ec0','ec1','ec2'])
const { proxy } = getCurrentInstance()
const { qms_rectify_status: statusOptions } = proxy.useDict('qms_rectify_status')

const defaultColumns = {
  rectifyNo: { label: '整改单编号', visible: true },
  complaintNo: { label: '客诉编号', visible: true },
  supplierName: { label: '供应商', visible: true },
  materialName: { label: '物料', visible: true },
  batchNo: { label: '批次号', visible: true },
  replyDeadline: { label: '回复时限', visible: true },
  rectifyStatus: { label: '状态', visible: true },
  replyTime: { label: '回复时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('qms_rectify_columns')
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
const showStatusHelp = ref(false)
const title = ref('')
const selectedId = ref(null)
const selectedIds = ref([])
const complaintOptions = ref([])
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })
const statusTabList = computed(() => statusOptions.value)
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10 } })
const { queryParams, form } = toRefs(data)
const rules = {
  complaintId: [{ required: true, message: '请选择客诉', trigger: 'change' }],
  rectifyRequirement: [{ required: true, message: '请输入整改要求', trigger: 'blur' }],
  replyDeadline: [{ required: true, message: '请选择供应商回复时限', trigger: 'change' }]
}
const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.rectifyNo) count++
  if (queryParams.value.complaintNo) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.rectifyStatus) count++
  return count
})

function getList() {
  loading.value = true
  listRectify(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
    loadStatusCounts()
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.rectifyNo = undefined; queryParams.value.complaintNo = undefined; queryParams.value.supplierName = undefined; queryParams.value.rectifyStatus = undefined; handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = '新增供应商整改单' }
function handleUpdate(row) {
  const id = row?.rectifyId || selectedId.value
  getRectify(id).then(res => { data.form = res.data; open.value = true; title.value = '修改供应商整改单' })
}
function handleView(row) { getRectify(row.rectifyId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleDelete(row) {
  const ids = row?.rectifyId ? [row.rectifyId] : (selectedIds.value || [])
  proxy.$modal.confirm('确认删除？').then(() => delRectify(ids.join(','))).then(() => { getList(); loadStatusCounts(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('qms/rectify/export', { ...queryParams.value }, `rectify_${new Date().getTime()}.xlsx`) }
function reset() { form.value = { rectifyId: null, rectifyNo: undefined, complaintId: undefined, complaintNo: undefined, supplierName: undefined, materialName: undefined, batchNo: undefined, rectifyRequirement: undefined, rectifyResult: undefined, supplierReply: undefined, replyTime: undefined }; proxy.resetForm('rectifyRef') }
function cancel() { open.value = false; reset() }
function dictLabel(options, val) { const arr = Array.isArray(options) ? options : options.value; const item = arr?.find(d => d.value == val); return item ? item.label : '' }
function statusClass(val) {
  const map = { '0': 'amber', '1': 'blue', '2': 'red', '3': 'gray' }
  return map[val] || 'gray'
}
function statusTabClass(value) {
  const map = { '0': 'tab-wait', '1': 'tab-replied', '2': 'tab-overdue', '3': 'tab-closed' }
  return map[value] || ''
}
function statusLabelOf(val) { return dictLabel(statusOptions, val) || (val === '0' ? '待供应商回复' : '-') }
function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.rectifyStatus = status === 'all' ? undefined : status
  handleQuery()
}
function loadStatusCounts() {
  listRectify({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total, '0': 0, '1': 0, '2': 0, '3': 0 }
    ;(res.rows || []).forEach(r => { if (counts[r.rectifyStatus] !== undefined) counts[r.rectifyStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
getList()
</script>
<style scoped>
/* 与客诉台账页样式口径一致 */
.qms-rectify-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-rectify-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-rectify-page .filter-card { padding:14px 20px 16px; }
.qms-rectify-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-rectify-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-rectify-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-rectify-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-rectify-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-rectify-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-rectify-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-rectify-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-rectify-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-rectify-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-rectify-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-rectify-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-rectify-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-rectify-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-rectify-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-rectify-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-rectify-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-rectify-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-rectify-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-rectify-page .field .control :deep(.el-select) { width:100%; }
.qms-rectify-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-rectify-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-rectify-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-rectify-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-rectify-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-rectify-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-rectify-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-rectify-page .btn-soft .el-icon { font-size:14px; }
.qms-rectify-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-rectify-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-rectify-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-rectify-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-rectify-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-rectify-page .table-wrap { overflow-x:auto; }
.qms-rectify-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-rectify-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-rectify-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-rectify-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-rectify-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-rectify-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-rectify-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-rectify-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-rectify-page .filter-card .filter-bar { grid-template-columns:1fr; } .qms-rectify-page .toolbar { flex-wrap:wrap; gap:10px; } }
/* ===== Badge 字典样式（参考供应商质量评价列表） ===== */
.qms-rectify-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; white-space:nowrap; }
.qms-rectify-page .badge .dot { width:6px; height:6px; border-radius:50%; flex-shrink:0; }
.qms-rectify-page .badge.amber { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-rectify-page .badge.blue { background:var(--brand-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-rectify-page .badge.blue .dot { background:var(--brand-500); }
.qms-rectify-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-rectify-page .badge.red .dot { background:var(--red-500); }
.qms-rectify-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-rectify-page .badge.gray .dot { background:var(--ink-400); }
/* ===== Status Tabs ===== */
.qms-rectify-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.qms-rectify-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.qms-rectify-page .tabs-track::-webkit-scrollbar { display:none; }
.qms-rectify-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.qms-rectify-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.qms-rectify-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.qms-rectify-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.qms-rectify-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.qms-rectify-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.qms-rectify-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 待回复 - amber */
.qms-rectify-page .status-tab.tab-wait .dot { background:#f59e0b; }
.qms-rectify-page .status-tab.tab-wait .count { background:#fffbeb; color:#b45309; }
.qms-rectify-page .status-tab.is-active.tab-wait .count { background:#f59e0b; color:#fff; }
/* 已回复 - blue */
.qms-rectify-page .status-tab.tab-replied .dot { background:var(--brand-500); }
.qms-rectify-page .status-tab.tab-replied .count { background:var(--brand-50); color:var(--brand-700); }
.qms-rectify-page .status-tab.is-active.tab-replied .count { background:var(--brand-500); color:#fff; }
/* 逾期升级 - red */
.qms-rectify-page .status-tab.tab-overdue .dot { background:var(--red-500); }
.qms-rectify-page .status-tab.tab-overdue .count { background:var(--red-50); color:var(--red-700); }
.qms-rectify-page .status-tab.is-active.tab-overdue .count { background:var(--red-500); color:#fff; }
/* 已关闭 - green */
.qms-rectify-page .status-tab.tab-closed .dot { background:var(--green-500); }
.qms-rectify-page .status-tab.tab-closed .count { background:var(--green-50); color:var(--green-700); }
.qms-rectify-page .status-tab.is-active.tab-closed .count { background:var(--green-500); color:#fff; }
/* ===== Tip Pill ===== */
.qms-rectify-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-rectify-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }
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
