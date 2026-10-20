<template>
  <div class="app-container pdm-change-page">
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
          <label>变更单编号</label>
          <div class="control">
            <el-input v-model="queryParams.changeNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>变更标题</label>
          <div class="control">
            <el-input v-model="queryParams.changeName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品名称</label>
          <div class="control">
            <el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品编码</label>
          <div class="control">
            <el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>变更级别</label>
          <div class="control">
            <el-select v-model="queryParams.changeLevel" placeholder="全部" clearable style="width: 100%">
              <el-option v-for="d in pdm_change_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="d in pdm_change_status" :key="d.value" class="status-tab" :class="[statusTabClass(d.value), { 'is-active': activeStatusTab === d.value }]" @click="handleStatusTabClick(d.value)">
            <span class="dot"></span><span>{{ d.label }}</span><span class="count">{{ statusCounts[d.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><WarningFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pdm:change:add']">发起变更</el-button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pdm:change:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pdm:change:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pdm_change_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="changeList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 70)" resizable align="center" />
          <el-table-column label="变更单编号" prop="changeNo" key="changeNo" :width="colWidth('changeNo', 170)" resizable sortable="custom" v-if="columns.changeNo.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.changeNo }}</span></template>
          </el-table-column>
          <el-table-column label="变更标题" prop="changeName" key="changeName" :min-width="colWidth('changeName', 180)" resizable show-overflow-tooltip sortable="custom" v-if="columns.changeName.visible" />
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 140)" resizable v-if="columns.productCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.productCode }}</span></template>
          </el-table-column>
          <el-table-column label="产品名称" prop="productName" key="productName" :min-width="colWidth('productName', 150)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="变更级别" prop="changeLevel" key="changeLevel" :width="colWidth('changeLevel', 100)" resizable align="center" v-if="columns.changeLevel.visible">
            <template #default="scope"><dict-tag :options="pdm_change_level" :value="scope.row.changeLevel" /></template>
          </el-table-column>
          <el-table-column label="紧急程度" prop="urgentLevel" key="urgentLevel" :width="colWidth('urgentLevel', 100)" resizable align="center" v-if="columns.urgentLevel.visible">
            <template #default="scope"><dict-tag :options="pdm_urgent_level" :value="scope.row.urgentLevel" /></template>
          </el-table-column>
          <el-table-column label="变更状态" prop="changeStatus" key="changeStatus" :width="colWidth('changeStatus', 100)" resizable align="center" sortable="custom" v-if="columns.changeStatus.visible">
            <template #default="scope">
              <span class="badge" :class="statusBadgeClass(scope.row.changeStatus)">
                <span class="dot"></span>{{ statusLabel(scope.row.changeStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" prop="submitTime" key="submitTime" :width="colWidth('submitTime', 160)" resizable align="center" v-if="columns.submitTime.visible" />
          <el-table-column label="审批人" prop="auditBy" key="auditBy" :width="colWidth('auditBy', 90)" resizable align="center" v-if="columns.auditBy.visible" />
          <el-table-column label="审批时间" prop="auditTime" key="auditTime" :width="colWidth('auditTime', 160)" resizable align="center" v-if="columns.auditTime.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 230)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.changeStatus === '0' || scope.row.changeStatus === '5'" v-hasPermi="['pdm:change:edit']">修改</el-button>
                <el-button link type="primary" icon="Position" @click="handleSubmit(scope.row)" v-if="scope.row.changeStatus === '0' || scope.row.changeStatus === '5'" v-hasPermi="['pdm:change:submit']">提交</el-button>
                <el-button link type="warning" icon="Checked" @click="handleAudit(scope.row)" v-if="scope.row.changeStatus === '1'" v-hasPermi="['pdm:change:audit']">审批</el-button>
                <el-button link type="success" icon="CircleCheck" @click="handleClose(scope.row)" v-if="scope.row.changeStatus === '2' || scope.row.changeStatus === '3'" v-hasPermi="['pdm:change:close']">关闭</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.changeStatus === '0'" v-hasPermi="['pdm:change:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 添加/修改对话框 -->
    <el-dialog v-model="open" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="changeRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>基本信息</div>
            </div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="变更标题" prop="changeName">
                    <el-input v-model="form.changeName" placeholder="请输入变更标题" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="变更产品" prop="productId">
                    <el-input :model-value="form.productName" placeholder="请选择产品档案" readonly style="width: 100%" @click="openProductPicker">
                      <template #append>
                        <el-button icon="Search" @click="openProductPicker" />
                      </template>
                      <template #suffix>
                        <el-icon v-if="form.productName" class="clear-icon" @click.stop="clearProduct"><CircleClose /></el-icon>
                      </template>
                    </el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="变更级别" prop="changeLevel">
                    <el-select v-model="form.changeLevel" placeholder="请选择变更级别" style="width: 100%">
                      <el-option v-for="d in pdm_change_level" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="紧急程度" prop="urgentLevel">
                    <el-select v-model="form.urgentLevel" placeholder="请选择紧急程度" style="width: 100%">
                      <el-option v-for="d in pdm_urgent_level" :key="d.value" :label="d.label" :value="d.value" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="变更内容" prop="changeContent">
                <el-input v-model="form.changeContent" type="textarea" :rows="2" placeholder="请输入变更内容说明" />
              </el-form-item>
              <el-form-item label="变更原因" prop="changeReason">
                <el-input v-model="form.changeReason" type="textarea" :rows="2" placeholder="请输入变更原因" />
              </el-form-item>
              <el-form-item label="影响分析" prop="effectAnalysis">
                <el-input v-model="form.effectAnalysis" type="textarea" :rows="2" placeholder="请输入影响分析（对成本/工艺/物料/下游的影响）" />
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>涉及资料清单</div>
            </div>
            <div class="rd-card-body">
              <el-alert type="info" :closable="false" show-icon style="margin-bottom: 12px" title="勾选本次变更涉及的资料（仅列出产品当前生效版本）；执行阶段按变更单对资料升版生效，依据=变更单号" />
              <el-table ref="docsTableRef" :data="docList" border size="small" row-key="docRowKey" @selection-change="onDocSelectionChange" empty-text="请先选择变更产品">
                <el-table-column type="selection" width="45" align="center" reserve-selection />
                <el-table-column label="资料类型" width="100" align="center">
                  <template #default="scope"><dict-tag :options="pdm_doc_type" :value="scope.row.docType" /></template>
                </el-table-column>
                <el-table-column label="资料编号" prop="bizNo" width="170">
                  <template #default="scope"><span class="col-mono">{{ scope.row.bizNo }}</span></template>
                </el-table-column>
                <el-table-column label="资料名称" prop="bizName" min-width="180" show-overflow-tooltip />
                <el-table-column label="当前生效版本" prop="currentVersion" width="120" align="center">
                  <template #default="scope"><span class="badge blue">{{ scope.row.currentVersion || '-' }}</span></template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看对话框 -->
    <el-dialog v-model="viewOpen" width="1152px" style="--el-dialog-width: 1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></div>
          <span class="rd-detail-header-title">变更单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.changeNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ viewData.changeNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>基本信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">变更单编号</span><div class="rd-value col-mono">{{ viewData.changeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">变更标题</span><div class="rd-value">{{ viewData.changeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value col-mono">{{ viewData.productCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">变更级别</span><div class="rd-value"><dict-tag :options="pdm_change_level" :value="viewData.changeLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">紧急程度</span><div class="rd-value"><dict-tag :options="pdm_urgent_level" :value="viewData.urgentLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">变更状态</span><div class="rd-value"><dict-tag :options="pdm_change_status" :value="viewData.changeStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">提交时间</span><div class="rd-value">{{ viewData.submitTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">变更内容</span><div class="rd-value">{{ viewData.changeContent || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">变更原因</span><div class="rd-value">{{ viewData.changeReason || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">影响分析</span><div class="rd-value">{{ viewData.effectAnalysis || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>涉及资料明细（{{ (viewData.itemList || []).length }} 项）</div>
            <el-button v-if="canClose" type="success" plain icon="CircleCheck" size="small" @click="handleClose(viewData)" v-hasPermi="['pdm:change:close']">关闭变更单</el-button>
            <!-- 关闭入口双通道：列表行操作列 + 详情明细卡（同一 handleClose），方便在查看执行进度时直接关闭 -->
          </div>
          <div class="rd-card-body">
            <el-table :data="viewData.itemList || []" border size="small" empty-text="未勾选涉及资料">
              <el-table-column label="资料类型" width="100" align="center">
                <template #default="scope"><dict-tag :options="pdm_doc_type" :value="scope.row.docType" /></template>
              </el-table-column>
              <el-table-column label="资料编号" prop="bizNo" width="170">
                <template #default="scope"><span class="col-mono">{{ scope.row.bizNo }}</span></template>
              </el-table-column>
              <el-table-column label="资料名称" prop="bizName" min-width="160" show-overflow-tooltip />
              <el-table-column label="当前版本" prop="currentVersion" width="95" align="center">
                <template #default="scope"><span class="badge gray">{{ scope.row.currentVersion || '-' }}</span></template>
              </el-table-column>
              <el-table-column label="升版后版本" prop="newVersion" width="100" align="center">
                <template #default="scope">
                  <span class="badge" :class="scope.row.newVersion ? 'green' : 'gray'">{{ scope.row.newVersion || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="执行状态" width="110" align="center">
                <template #default="scope"><dict-tag :options="pdm_change_item_status" :value="scope.row.itemStatus" /></template>
              </el-table-column>
              <el-table-column label="生效时间" prop="effectTime" width="160" align="center" />
              <el-table-column label="操作" width="110" align="center" v-if="viewData.changeStatus === '2' || viewData.changeStatus === '3'">
                <template #default="scope">
                  <el-button link type="primary" icon="Stamp" @click="handleExecute(viewData, scope.row)" v-if="scope.row.itemStatus === '0'" v-hasPermi="['pdm:change:execute']">升版生效</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg></span>审批与关闭信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ viewData.auditBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">审批时间</span><div class="rd-value">{{ viewData.auditTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">审批意见</span><div class="rd-value">{{ viewData.auditOpinion || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关闭人</span><div class="rd-value">{{ viewData.closeBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">关闭时间</span><div class="rd-value">{{ viewData.closeTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审批记录</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审批意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框（参照采购申请审批：单据信息 + 涉及资料明细 + 审批记录时间轴 + 审批意见） -->
    <el-dialog v-model="auditOpen" width="1060px" style="--el-dialog-width: 1060px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/></svg></div>
          <span class="rd-detail-header-title">变更审批</span>
          <div class="rd-detail-header-sub" v-if="auditData.changeNo">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">编号：{{ auditData.changeNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>变更单信息</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">变更单编号</span><div class="rd-value col-mono">{{ auditData.changeNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">变更标题</span><div class="rd-value">{{ auditData.changeName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value col-mono">{{ auditData.productCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ auditData.productName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">变更级别</span><div class="rd-value"><dict-tag :options="pdm_change_level" :value="auditData.changeLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">紧急程度</span><div class="rd-value"><dict-tag :options="pdm_urgent_level" :value="auditData.urgentLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">变更状态</span><div class="rd-value"><dict-tag :options="pdm_change_status" :value="auditData.changeStatus" /></div></div>
              <div class="rd-item"><span class="rd-label">提交时间</span><div class="rd-value">{{ auditData.submitTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">变更内容</span><div class="rd-value">{{ auditData.changeContent || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">变更原因</span><div class="rd-value">{{ auditData.changeReason || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="auditData.effectAnalysis"><span class="rd-label">影响分析</span><div class="rd-value">{{ auditData.effectAnalysis }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>涉及资料明细（{{ (auditData.itemList || []).length }} 项）</div>
          </div>
          <div class="rd-card-body">
            <el-table :data="auditData.itemList || []" border size="small" empty-text="未勾选涉及资料">
              <el-table-column label="资料类型" width="100" align="center">
                <template #default="scope"><dict-tag :options="pdm_doc_type" :value="scope.row.docType" /></template>
              </el-table-column>
              <el-table-column label="资料编号" prop="bizNo" width="170">
                <template #default="scope"><span class="col-mono">{{ scope.row.bizNo }}</span></template>
              </el-table-column>
              <el-table-column label="资料名称" prop="bizName" min-width="160" show-overflow-tooltip />
              <el-table-column label="当前版本" prop="currentVersion" width="95" align="center">
                <template #default="scope"><span class="badge gray">{{ scope.row.currentVersion || '-' }}</span></template>
              </el-table-column>
              <el-table-column label="升版后版本" prop="newVersion" width="100" align="center">
                <template #default="scope"><span class="badge" :class="scope.row.newVersion ? 'green' : 'gray'">{{ scope.row.newVersion || '-' }}</span></template>
              </el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>历史审批记录</div>
          </div>
          <div class="rd-card-body">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in auditData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审批通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审批意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></span>审批意见</div>
          </div>
          <div class="rd-card-body">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="110px">
              <el-form-item label="审批意见" prop="auditOpinion">
                <el-input v-model="auditForm.auditOpinion" type="textarea" :rows="4" placeholder="请输入审批意见（通过/驳回均需填写，作为审批依据留痕）" show-word-limit maxlength="500" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="submitAudit(true)">通 过</el-button>
        <el-button type="danger" @click="submitAudit(false)">驳 回</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 产品档案选择器 -->
    <PdmProductPicker ref="productPickerRef" title="选择变更产品" @confirm="onProductConfirm" />
    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="技术变更业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">审批中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">待执行</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">执行中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已关闭</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">变更单创建后的初始状态，可编辑变更产品、变更内容与升版资料清单；提交后进入审批流程</el-descriptions-item>
          <el-descriptions-item label="审批中">变更单已提交等待审批；审批不通过转为「已驳回」，可修改后重新提交</el-descriptions-item>
          <el-descriptions-item label="待执行">审批通过，变更内容确认生效，可逐项执行资料升版</el-descriptions-item>
          <el-descriptions-item label="执行中">正在逐项执行变更（方案/图纸/BOM 升版），系统自动生成新版本并记录生效依据，相关岗位收到通知</el-descriptions-item>
          <el-descriptions-item label="已关闭">全部变更项执行完毕后关闭变更单，流程结束</el-descriptions-item>
        </el-descriptions>

        <h4>三、重点业务规则</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">受控修订优先走变更</div>
              <div class="highlight-card-body">已定稿/受控资料的<strong>正式修改建议通过变更单</strong>审批留痕；系统同时保留直接修订的快速通道（见下方「两条修订升版路径」）</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">变更级别管理</div>
              <div class="highlight-card-body">变更单按<strong>一般/重要/重大</strong>三级管理，重大变更需从严审批；编号由系统按规则自动生成</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">多资料批量升版</div>
              <div class="highlight-card-body">一张变更单可同时携带<strong>多项资料与 BOM</strong>，逐项执行、逐项留痕，全部完成后关闭</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">岗位协同通知</div>
              <div class="highlight-card-body">变更执行时系统自动向<strong>相关岗位推送通知</strong>；BOM 升版后需在设计BOM页面重新下发仓库</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、两条修订升版路径</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">路径一：直接修订（快速通道）</div>
              <div class="highlight-card-body">
                资料页面直接「编辑」修改内容保存（已定稿资料自动生成新版本）→ 再「定稿/生效」。<br />
                版本依据记为<strong>「设计定稿」</strong>，仅记录操作人与时间。<br />
                <strong>适用：</strong>编制中资料的正常迭代；已定稿资料的轻微修正（无下游影响）。
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">路径二：技术变更（受控通道）</div>
              <div class="highlight-card-body">
                发起变更单（说明变更原因/内容/影响）→ 审批通过 → 资料页面按变更要求修订 → 回变更单「升版生效」→ 关闭。<br />
                版本依据记为<strong>「技术变更 + 变更单号」</strong>，原因、审批意见、执行记录全程可追溯。<br />
                <strong>适用：</strong>影响下游使用的正式修改、需审批留痕的受控变更、多资料联动变更。
              </div>
            </div>
          </el-col>
        </el-row>
        <el-alert type="error" :closable="false" show-icon style="margin-top: 12px" title="注意：两种路径不要混用同一批资料" description="已被变更单挂用的资料，请勿再走「直接修订+定稿」——变更执行时要求资料存在未生效的新版本，若新版本已被直接定稿生效，变更单将无法闭环（需重新修订一次）。受控资料建议统一走变更单。" />

        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>发起变更：</strong>点击「发起变更」，选择变更产品与变更级别，填写变更原因与内容，添加需要升版的资料与 BOM 清单
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>审批确认：</strong>提交后由审批岗位审核，审批通过进入待执行状态
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>执行升版：</strong>逐项执行资料变更，系统自动升版并通知相关岗位；全部执行完毕后关闭变更单
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PdmChange">
import { listChange, getChange, addChange, updateChange, delChange, submitChange, auditChange, executeItem, closeChange, listEffectiveDocs } from '@/api/pdm/change'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import PdmProductPicker from '@/components/PdmProductPicker/index.vue'
import { ArrowDown, ArrowRight, CircleClose, Delete, Download, Promotion, RefreshLeft, Search, Filter, WarningFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pdm_change_index')
const { pdm_change_status, pdm_change_level, pdm_urgent_level, pdm_doc_type, pdm_change_item_status } = proxy.useDict('pdm_change_status', 'pdm_change_level', 'pdm_urgent_level', 'pdm_doc_type', 'pdm_change_item_status')

const changeList = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const auditOpen = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showStatusHelp = ref(false)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const productPickerRef = ref()
const docsTableRef = ref()
const docList = ref([])
const selectedDocs = ref([])
const auditData = ref({})
const auditForm = ref({ changeId: undefined, auditOpinion: undefined })
const auditRules = { auditOpinion: [{ required: true, message: '请填写审批意见', trigger: 'blur' }] }

const defaultColumns = {
  changeNo: { label: '变更单编号', visible: true },
  changeName: { label: '变更标题', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  changeLevel: { label: '变更级别', visible: true },
  urgentLevel: { label: '紧急程度', visible: true },
  changeStatus: { label: '变更状态', visible: true },
  submitTime: { label: '提交时间', visible: true },
  auditBy: { label: '审批人', visible: true },
  auditTime: { label: '审批时间', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pdm_change_columns')
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
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    changeNo: undefined,
    changeName: undefined,
    productName: undefined,
    productCode: undefined,
    changeLevel: undefined,
    changeStatus: undefined,
    params: {}
  },
  rules: {
    changeName: [{ required: true, message: '请输入变更标题', trigger: 'blur' }],
    productId: [{ required: true, message: '请选择变更产品', trigger: 'change' }],
    changeLevel: [{ required: true, message: '请选择变更级别', trigger: 'change' }],
    urgentLevel: [{ required: true, message: '请选择紧急程度', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 是否允许关闭（待执行/执行中） */
const canClose = computed(() => viewData.value.changeStatus === '2' || viewData.value.changeStatus === '3')

function getList() {
  loading.value = true
  listChange(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    changeList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params = queryParams.value.params || {}
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params = queryParams.value.params || {}
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}

function statusLabel(status) { const item = pdm_change_status.value.find(d => d.value == status); return item ? item.label : '-' }
function statusBadgeClass(status) { const item = pdm_change_status.value.find(d => d.value == status); const map = { 'info': 'gray', 'success': 'green', 'danger': 'red', 'warning': 'amber', 'primary': 'blue' }; return map[item ? item.listClass : ''] || 'gray' }
function statusTabClass(value) {
  const map = { '0': 'tab-draft', '1': 'tab-auditing', '2': 'tab-pending', '3': 'tab-executing', '4': 'tab-closed', '5': 'tab-rejected' }
  return map[value] || ''
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadStatusCounts()
}

function resetQuery() {
  queryParams.value.changeNo = undefined
  queryParams.value.changeName = undefined
  queryParams.value.productName = undefined
  queryParams.value.productCode = undefined
  queryParams.value.changeLevel = undefined
  queryParams.value.changeStatus = undefined
  queryParams.value.params = {}
  dateRange.value = []
  activeStatusTab.value = 'all'
  handleQuery()
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.changeStatus = status === 'all' ? undefined : status
  handleQuery()
}

function loadStatusCounts() {
  listChange({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total };
    (pdm_change_status.value || []).forEach(d => { counts[d.value] = 0 });
    (res.rows || []).forEach(r => { if (counts[r.changeStatus] !== undefined) counts[r.changeStatus]++ })
    statusCounts.value = counts
  }).catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.changeId)
  multiple.value = !selection.length
}

function reset() {
  form.value = {
    changeId: undefined,
    changeName: undefined,
    productId: undefined,
    productName: undefined,
    productCode: undefined,
    changeLevel: '1',
    urgentLevel: '1',
    changeContent: undefined,
    changeReason: undefined,
    effectAnalysis: undefined,
    itemList: []
  }
  docList.value = []
  selectedDocs.value = []
  proxy.resetForm('changeRef')
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '发起技术变更'
}

function handleUpdate(row) {
  reset()
  const changeId = row.changeId || ids.value[0]
  getChange(changeId).then(response => {
    form.value = response.data
    form.value.itemList = response.data.itemList || []
    open.value = true
    title.value = '修改变更单'
    loadEffectiveDocs(form.value.productId, form.value.itemList)
  })
}

/** 加载产品当前生效资料清单（编辑时回显已勾选项） */
function loadEffectiveDocs(productId, savedItems) {
  if (!productId) { docList.value = []; return }
  listEffectiveDocs(productId).then(res => {
    const items = res.data || []
    docList.value = items.map(d => ({ ...d, docRowKey: d.docType + '-' + d.bizId }))
    selectedDocs.value = []
    nextTick(() => {
      (savedItems || []).forEach(it => {
        const row = docList.value.find(d => d.docType === it.docType && d.bizId === it.bizId)
        if (row) docsTableRef.value.toggleRowSelection(row, true)
      })
    })
  })
}

function onProductConfirm(product) {
  form.value.productId = product.productId
  form.value.productName = product.productName
  form.value.productCode = product.productCode
  proxy.$refs['changeRef'].clearValidate(['productId'])
  loadEffectiveDocs(product.productId, form.value.changeId ? form.value.itemList : [])
}

/** 清除变更产品（联动清空涉及资料清单） */
function clearProduct() {
  form.value.productId = undefined
  form.value.productName = undefined
  form.value.productCode = undefined
  docList.value = []
  selectedDocs.value = []
  proxy.$refs['changeRef'].clearValidate(['productId'])
}

function onDocSelectionChange(selection) {
  selectedDocs.value = selection
}

function openProductPicker() {
  productPickerRef.value.open(form.value.productId)
}

function submitForm() {
  proxy.$refs['changeRef'].validate(valid => {
    if (!valid) return
    const items = selectedDocs.value.map(d => ({
      docType: d.docType, bizId: d.bizId, bizNo: d.bizNo, bizName: d.bizName, currentVersion: d.currentVersion
    }))
    if (!items.length) {
      proxy.$modal.msgWarning('请勾选本次变更涉及的资料')
      return
    }
    form.value.itemList = items
    if (form.value.changeId != undefined) {
      updateChange(form.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        open.value = false
        getList(); loadStatusCounts()
      })
    } else {
      addChange(form.value).then(() => {
        proxy.$modal.msgSuccess('变更单已创建（草稿），提交审批后进入变更流程')
        open.value = false
        getList(); loadStatusCounts()
      })
    }
  })
}

function handleSubmit(row) {
  const changeId = row.changeId || ids.value[0]
  const target = row || changeList.value.find(i => i.changeId === changeId)
  proxy.$modal.confirm('是否确认提交变更单 "' + (target ? target.changeNo : '') + '" 进入审批流程？提交后不可修改。').then(() => {
    return submitChange(changeId)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('提交成功，已推送审批待办')
  }).catch(() => {})
}

function handleAudit(row) {
  // 拉取变更单详情（含涉及资料明细与历史审批记录），供审批人参考后决策
  getChange(row.changeId).then(res => {
    auditData.value = res.data || {}
    auditForm.value = { changeId: row.changeId, auditOpinion: undefined }
    auditOpen.value = true
  })
}

function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      auditChange({ changeId: auditForm.value.changeId, approved: passed, auditOpinion: auditForm.value.auditOpinion }).then(() => {
        proxy.$modal.msgSuccess(passed ? '审批通过，变更单进入待执行' : '已驳回，可修改后重新提交')
        auditOpen.value = false
        getList(); loadStatusCounts()
      })
    }
  })
}

function handleView(row) {
  getChange(row.changeId).then(response => {
    viewData.value = response.data
    viewOpen.value = true
  })
}

function handleExecute(change, item) {
  proxy.$modal.confirm('按变更单对资料 "' + (item.bizNo || '') + '" 升版生效？需先在资料模块按变更单完成修订（自动升版）。').then(() => {
    return executeItem({ changeId: change.changeId, itemId: item.itemId })
  }).then(() => {
    return getChange(change.changeId)
  }).then(response => {
    viewData.value = response.data
    proxy.$modal.msgSuccess('升版生效成功，新版本生效依据=技术变更单')
    getList(); loadStatusCounts()
  }).catch(() => {})
}

function handleClose(change) {
  proxy.$modal.confirm('关闭变更单将校验所有涉及资料已升版生效，并通知仓库/采购/质量/生产岗位。是否确认关闭？').then(() => {
    return closeChange(change.changeId)
  }).then(() => {
    return getChange(change.changeId)
  }).then(response => {
    viewData.value = response.data
    proxy.$modal.msgSuccess('变更单已关闭，变更通知已推送相关岗位')
    getList(); loadStatusCounts()
  }).catch(() => {})
}

function handleDelete(row) {
  const changeIds = row.changeId || ids.value
  proxy.$modal.confirm('是否确认删除选中的变更单？仅草稿状态允许删除，进入流程后变更记录不可删除。').then(function () {
    return delChange(changeIds)
  }).then(() => {
    getList(); loadStatusCounts()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

async function handleExport() {
  let rows = await fetchAllPages(listChange, queryParams.value)
  if (ids.value && ids.value.length) { rows = rows.filter(i => ids.value.includes(i.changeId)) }
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['变更单编号', '变更标题', '产品编码', '产品名称', '变更级别', '紧急程度', '变更状态', '提交时间', '审批人', '审批时间', '创建时间']
  downloadXlsx(`pdm_change_order_${new Date().getTime()}`, headers, rows.map(i => [
    i.changeNo || '', i.changeName || '', i.productCode || '', i.productName || '',
    dictLabel(pdm_change_level.value, i.changeLevel), dictLabel(pdm_urgent_level.value, i.urgentLevel),
    statusLabel(i.changeStatus), i.submitTime || '', i.auditBy || '', i.auditTime || '', i.createTime || ''
  ]))
}

function dictLabel(options, value) { const item = (options || []).find(d => d.value == value); return item ? item.label : (value || '') }

function cancel() {
  open.value = false
  reset()
}

getList()
loadStatusCounts()
</script>

<style src="@/composables/mms-list-styles.scss" lang="scss" scoped></style>
<style scoped>
.pdm-change-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.pdm-change-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pdm-change-page .filter-card { padding:14px 20px 16px; }
.pdm-change-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pdm-change-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pdm-change-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pdm-change-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pdm-change-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pdm-change-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pdm-change-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pdm-change-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pdm-change-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pdm-change-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pdm-change-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pdm-change-page .field { display:flex; flex-direction:column; gap:6px; }
.pdm-change-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.pdm-change-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pdm-change-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pdm-change-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pdm-change-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; }
.pdm-change-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pdm-change-page .status-tabs { display:flex; align-items:center; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pdm-change-page .tabs-track { display:flex; align-items:center; gap:4px; flex-wrap:wrap; flex:1; min-width:0; }
.pdm-change-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pdm-change-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pdm-change-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.pdm-change-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pdm-change-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pdm-change-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pdm-change-page .status-tab.is-active .dot { background:var(--brand-500); }
.pdm-change-page .status-tab.tab-draft .dot { background:var(--blue-500); }
.pdm-change-page .status-tab.is-active.tab-draft .count { background:var(--blue-500); color:#fff; }
.pdm-change-page .status-tab.tab-auditing .dot { background:var(--brand-500); }
.pdm-change-page .status-tab.is-active.tab-auditing .count { background:var(--brand-500); color:#fff; }
.pdm-change-page .status-tab.tab-pending .dot { background:var(--amber-500); }
.pdm-change-page .status-tab.is-active.tab-pending .count { background:var(--amber-500); color:#fff; }
.pdm-change-page .status-tab.tab-executing .dot { background:var(--brand-600); }
.pdm-change-page .status-tab.is-active.tab-executing .count { background:var(--brand-600); color:#fff; }
.pdm-change-page .status-tab.tab-closed .dot { background:var(--green-500); }
.pdm-change-page .status-tab.is-active.tab-closed .count { background:var(--green-500); color:#fff; }
.pdm-change-page .status-tab.tab-rejected .dot { background:var(--red-500); }
.pdm-change-page .status-tab.is-active.tab-rejected .count { background:var(--red-500); color:#fff; }
.pdm-change-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pdm-change-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pdm-change-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pdm-change-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pdm-change-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pdm-change-page .btn-soft .el-icon { font-size:14px; }
.pdm-change-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pdm-change-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pdm-change-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pdm-change-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pdm-change-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pdm-change-page .table-wrap { overflow-x:auto; }
.pdm-change-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pdm-change-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pdm-change-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pdm-change-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pdm-change-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pdm-change-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pdm-change-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pdm-change-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pdm-change-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.pdm-change-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pdm-change-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pdm-change-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pdm-change-page .badge.blue .dot { background:var(--blue-500); }
.pdm-change-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pdm-change-page .badge.green .dot { background:var(--green-500); }
.pdm-change-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pdm-change-page .badge.gray .dot { background:var(--ink-400); }
.pdm-change-page .clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.pdm-change-page .clear-icon:hover { color: #909399; }
.pdm-change-page :deep(.col-action) { padding: 6px 4px !important; }
.pdm-change-page :deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.pdm-change-page .action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
.pdm-change-page :deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
.pdm-change-page :deep(.col-action .el-button + .el-button) { margin-left: 2px; }
@media (max-width:1100px) { .pdm-change-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pdm-change-page .filter-card .filter-bar { grid-template-columns:1fr; } .pdm-change-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
