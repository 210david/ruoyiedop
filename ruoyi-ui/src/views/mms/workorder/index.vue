<template>
  <div class="app-container mms-workorder-page">
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
          <label>工单编号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>产品编码</label>
          <div class="control">
            <el-input v-model="queryParams.productCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>产品名称</label>
          <div class="control">
            <el-input v-model="queryParams.productName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>工单类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.orderType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_order_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工单状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_workorder_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>优先级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.priority" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mms_priority" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>BOM编号</label>
          <div class="control">
            <el-input v-model="queryParams.bomNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>工艺路线</label>
          <div class="control">
            <el-input v-model="queryParams.routeNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>产能单元</label>
          <div class="control">
            <el-input v-model="queryParams.resourceName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建日期</label>
          <div class="control">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Status Tabs -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span><span>全部</span><span class="count">{{ statusCounts.all || 0 }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['mms:workorder:add']">新增</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['mms:workorder:edit']">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['mms:workorder:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['mms:workorder:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="mms_workorder_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="dataList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="工单编号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 150)" resizable v-if="columns.workOrderNo.visible" />
          <el-table-column label="工单类型" prop="orderType" key="orderType" :width="colWidth('orderType', 100)" resizable align="center" v-if="columns.orderType.visible">
            <template #default="scope"><span class="badge violet">{{ orderTypeLabel(scope.row.orderType) }}</span></template>
          </el-table-column>
          <el-table-column label="产品编码" prop="productCode" key="productCode" :width="colWidth('productCode', 130)" resizable v-if="columns.productCode.visible" />
          <el-table-column label="产品名称" prop="productName" key="productName" :width="colWidth('productName', 200)" resizable show-overflow-tooltip v-if="columns.productName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 140)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible" />
          <el-table-column label="计划数量" prop="planQty" key="planQty" :width="colWidth('planQty', 100)" resizable align="center" v-if="columns.planQty.visible" />
          <el-table-column label="完工数量" prop="finishedQty" key="finishedQty" :width="colWidth('finishedQty', 100)" resizable align="center" v-if="columns.finishedQty.visible" />
          <el-table-column label="合格数量" prop="qualifiedQty" key="qualifiedQty" :width="colWidth('qualifiedQty', 100)" resizable align="center" v-if="columns.qualifiedQty.visible" />
          <el-table-column label="优先级" prop="priority" key="priority" :width="colWidth('priority', 90)" resizable align="center" v-if="columns.priority.visible">
            <template #default="scope"><span class="badge" :class="priorityBadgeClass(scope.row.priority)"><span class="dot"></span>{{ priorityLabel(scope.row.priority) }}</span></template>
          </el-table-column>
          <el-table-column label="工单状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" sortable="custom" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusLabel(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="计划开工" prop="planStart" key="planStart" :width="colWidth('planStart', 160)" resizable align="center" v-if="columns.planStart.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planStart) }}</span></template>
          </el-table-column>
          <el-table-column label="计划完工" prop="planFinish" key="planFinish" :width="colWidth('planFinish', 160)" resizable align="center" v-if="columns.planFinish.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.planFinish) }}</span></template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible">
            <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="320" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['mms:workorder:query']">详情</el-button>
              <el-button v-if="scope.row.status === '0'" link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['mms:workorder:edit']">修改</el-button>
              <el-button v-if="scope.row.status === '0'" link type="success" icon="Promotion" @click="handleRelease(scope.row)" v-hasPermi="['mms:workorder:release']">下达</el-button>
              <el-button v-if="scope.row.status === '1' || scope.row.status === '2'" link type="warning" icon="VideoPause" @click="handlePause(scope.row)" v-hasPermi="['mms:workorder:pause']">暂停</el-button>
              <el-button v-if="scope.row.status === '7'" link type="success" icon="VideoPlay" @click="handleResume(scope.row)" v-hasPermi="['mms:workorder:resume']">恢复</el-button>
              <el-button v-if="scope.row.status === '2' || scope.row.status === '3'" link type="primary" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['mms:workorder:finish']">完工</el-button>
              <el-button v-if="scope.row.status === '4' || scope.row.status === '5'" link type="success" icon="Lock" @click="handleClose(scope.row)" v-hasPermi="['mms:workorder:close']">关闭</el-button>
              <el-button v-if="scope.row.status !== '6' && scope.row.status !== '8'" link type="danger" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['mms:workorder:cancel']">作废</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== 新增/修改 Dialog ===== -->
    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工单编号" prop="workOrderNo">
              <el-input v-model="form.workOrderNo" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单类型" prop="orderType">
              <el-select v-model="form.orderType" placeholder="请选择">
                <el-option v-for="dict in mms_order_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品编码" prop="productCode">
              <el-input v-model="form.productCode" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格型号" prop="specModel">
              <el-input v-model="form.specModel" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划数量" prop="planQty">
              <el-input-number v-model="form.planQty" :min="0" :precision="2" placeholder="请输入" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="BOM编号" prop="bomNo">
              <el-input v-model="form.bomNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工艺路线" prop="routeNo">
              <el-input v-model="form.routeNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产能单元" prop="resourceName">
              <el-input v-model="form.resourceName" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开工" prop="planStart">
              <el-date-picker v-model="form.planStart" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划完工" prop="planFinish">
              <el-date-picker v-model="form.planFinish" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择">
                <el-option v-for="dict in mms_priority" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联计划号" prop="mpsNo">
              <el-input v-model="form.mpsNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 暂停原因 Dialog ===== -->
    <el-dialog title="工单暂停" v-model="pauseOpen" width="500px" append-to-body>
      <el-form ref="pauseFormRef" :model="pauseForm" label-width="100px">
        <el-form-item label="工单编号">
          <span>{{ pauseForm.workOrderNo }}</span>
        </el-form-item>
        <el-form-item label="暂停原因" prop="pauseReason">
          <el-input v-model="pauseForm.pauseReason" type="textarea" placeholder="请输入暂停原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitPause">确 定</el-button>
          <el-button @click="pauseOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 关闭/作废 Dialog ===== -->
    <el-dialog :title="closeTitle" v-model="closeOpen" width="500px" append-to-body>
      <el-form ref="closeFormRef" :model="closeForm" label-width="100px">
        <el-form-item label="工单编号">
          <span>{{ closeForm.workOrderNo }}</span>
        </el-form-item>
        <el-form-item :label="closeLabel" prop="remark">
          <el-input v-model="closeForm.remark" type="textarea" :placeholder="'请输入' + closeLabel" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitClose">确 定</el-button>
          <el-button @click="closeOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ===== 工单详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="800px" append-to-body draggable>
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1h-7"/><path d="M9 3V1h4v2"/><path d="M9 3v6h4V3"/></svg>
          </div>
          <span class="rd-detail-header-title">工单详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.workOrderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ viewData.workOrderNo }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page">
        <div class="rd-grid">
          <div class="rd-item"><span class="rd-label">工单编号</span><div class="rd-value">{{ viewData.workOrderNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">工单类型</span><div class="rd-value"><dict-tag :options="mms_order_type" :value="viewData.orderType" /></div></div>
          <div class="rd-item"><span class="rd-label">产品编码</span><div class="rd-value">{{ viewData.productCode || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">产品名称</span><div class="rd-value">{{ viewData.productName || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value">{{ viewData.unit || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">计划数量</span><div class="rd-value">{{ viewData.planQty != null ? viewData.planQty : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">完工数量</span><div class="rd-value">{{ viewData.finishedQty != null ? viewData.finishedQty : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">合格数量</span><div class="rd-value">{{ viewData.qualifiedQty != null ? viewData.qualifiedQty : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">优先级</span><div class="rd-value"><dict-tag :options="mms_priority" :value="viewData.priority" /></div></div>
          <div class="rd-item"><span class="rd-label">工单状态</span><div class="rd-value"><span class="badge" :class="badgeClass(viewData.status)"><span class="dot"></span>{{ statusLabel(viewData.status) }}</span></div></div>
          <div class="rd-item"><span class="rd-label">BOM编号</span><div class="rd-value">{{ viewData.bomNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">工艺路线</span><div class="rd-value">{{ viewData.routeNo || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">产能单元</span><div class="rd-value">{{ viewData.resourceName || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">计划开工</span><div class="rd-value">{{ viewData.planStart ? parseTime(viewData.planStart) : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">计划完工</span><div class="rd-value">{{ viewData.planFinish ? parseTime(viewData.planFinish) : '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">关联计划号</span><div class="rd-value">{{ viewData.mpsNo || '-' }}</div></div>
          <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !viewData.remark }">{{ viewData.remark || '暂无' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ viewData.createBy || '-' }}</div></div>
          <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ viewData.createTime ? parseTime(viewData.createTime) : '-' }}</div></div>
        </div>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 审核日志 Dialog ===== -->
    <el-dialog title="工单审核日志" v-model="logOpen" width="700px" append-to-body>
      <el-table :data="auditLogList" border>
        <el-table-column label="操作" align="center" prop="auditAction" width="100" />
        <el-table-column label="操作人" align="center" prop="auditBy" width="100" />
        <el-table-column label="操作时间" align="center" prop="auditTime" width="160">
          <template #default="scope"><span>{{ parseTime(scope.row.auditTime) }}</span></template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="auditRemark" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="工单管理业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、工单释义 -->
        <h4>一、工单释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是工单？</div>
          <div class="highlight-card-body">
            <strong>工单（Work Order）</strong>是生产管控中用于安排、执行和跟踪生产任务的核心单据。工单关联产品、BOM、工艺路线和产能单元，定义计划数量、计划开工/完工时间，通过下达→生产→完工的全生命周期管理，实现生产过程的数字化管控和进度追踪。<br/><br/>
            工单是<strong>MES（制造执行系统）</strong>的核心数据载体，向上对接主生产计划（MPS）和销售需求，向下驱动物料领料、工序流转、质量检验和完工入库，满足精益生产和离散制造对生产过程可追溯、可量化、可管控的要求。
          </div>
        </div>

        <!-- 二、工单状态流转图 -->
        <h4>二、工单状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「下达」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">已下达</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">开始生产</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">生产中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「完工」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">待完工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">确认完工</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已完工</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">草稿</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="primary">已下达/生产中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「作废」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="primary">已下达/生产中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「暂停」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">已暂停</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「恢复」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="primary">生产中</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="草稿">工单新建后的初始状态。可修改工单信息、下达至生产线或作废。点击「下达」将工单下发至车间，状态变为已下达</el-descriptions-item>
          <el-descriptions-item label="已下达">工单已下达至生产线，等待开始生产。可暂停或开始生产。进入生产后状态自动变为生产中</el-descriptions-item>
          <el-descriptions-item label="生产中">工单正在生产执行中。可暂停（需填写暂停原因）或完工。暂停后可恢复继续生产</el-descriptions-item>
          <el-descriptions-item label="待完工">生产基本完成，等待最终完工确认。可执行完工操作，系统自动记录完工数量和合格数量</el-descriptions-item>
          <el-descriptions-item label="已完工">工单已完工，完工数量已确认。可关闭工单或查看详情。已完工工单可关闭</el-descriptions-item>
          <el-descriptions-item label="已关闭">工单已关闭，不可再操作。关闭表示工单生命周期结束</el-descriptions-item>
          <el-descriptions-item label="已作废">工单因异常情况被作废，不再执行。除已作废和已关闭外的工单均可作废</el-descriptions-item>
          <el-descriptions-item label="已暂停">工单已暂停生产，可恢复继续生产。暂停时需填写暂停原因</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>工单编号：</strong>工单的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>工单类型：</strong>包括生产工单、返工工单、试产工单等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品编码：</strong>待生产产品的编码<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>产品名称：</strong>待生产产品的名称<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>规格型号：</strong>产品的规格型号信息</p>
            <p>• <strong>单位：</strong>产品的计量单位，如个、kg、m等</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">生产信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>计划数量：</strong>本工单计划生产的数量<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>BOM编号：</strong>关联的物料清单编号，用于确定物料需求</p>
            <p>• <strong>工艺路线：</strong>关联的工艺路线编号，用于确定加工工序路径</p>
            <p>• <strong>产能单元：</strong>指派生产的产能单元（设备/产线）</p>
            <p>• <strong>计划开工：</strong>计划开始生产的时间</p>
            <p>• <strong>计划完工：</strong>计划完成生产的时间</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">其他信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>优先级：</strong>工单的生产优先级，分为高、中、低，影响排产顺序</p>
            <p>• <strong>关联计划号：</strong>关联的主生产计划（MPS）编号</p>
            <p>• <strong>备注：</strong>工单的补充说明信息</p>
          </div>
        </div>

        <!-- 五、工单生命周期管控 -->
        <h4>五、工单生命周期管控</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是工单生命周期管控？</div>
          <div class="highlight-card-body">
            <strong>工单生命周期管控</strong>是生产管控的核心机制，通过状态流转实现工单从创建到关闭的全过程管理。每个状态对应特定的可执行操作，确保生产过程有序可控、可追溯。工单的暂停/恢复机制支持应对设备故障、物料短缺等异常情况，完工确认机制确保生产数量和质量数据的准确归集。
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">异常处理规则</div>
          <div class="highlight-card-body">
            <p>1. <strong>暂停处理：</strong>生产中如遇设备故障、物料短缺等异常，可暂停工单并填写暂停原因，排除故障后恢复生产</p>
            <p>2. <strong>作废处理：</strong>因订单取消、工艺变更等原因无法继续生产的工单，可作废终止，作废后不可恢复</p>
            <p>3. <strong>关闭处理：</strong>已完工的工单确认无误后可关闭，关闭表示工单生命周期结束，数据归档</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>暂停/作废/关闭操作均需填写原因，系统自动记录操作日志，确保全流程可追溯</p>
          </div>
        </div>

        <!-- 六、业务操作流程 -->
        <h4>六、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建工单：</strong>点击「新增」创建工单，填写工单类型、产品信息、计划数量、关联BOM和工艺路线，保存后工单编号自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>下达工单：</strong>草稿状态下点击「下达」将工单下发至生产线，状态变为已下达，车间可开始排产
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>暂停/恢复：</strong>生产中如需暂停，点击「暂停」并填写原因。暂停后点击「恢复」继续生产
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>完工确认：</strong>生产完成后点击「完工」，系统自动记录完工数量和合格数量，状态变为已完工
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看详情：</strong>已完工的工单可点击「详情」查看完整信息，包括生产数据、审核日志等
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>作废/关闭：</strong>异常工单可「作废」终止；已完工工单确认无误后可「关闭」归档
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WorkOrder">
import { listWorkOrder, getWorkOrder, addWorkOrder, updateWorkOrder, delWorkOrder,
         releaseWorkOrder, pauseWorkOrder, resumeWorkOrder, finishWorkOrder,
         closeWorkOrder, cancelWorkOrder, getWorkOrderAuditLog } from "@/api/mms/workorder";
import { useColumnResize } from '@/composables/useColumnResize'
import { Search, Filter, RefreshLeft, ArrowRight, ArrowDown, WarningFilled, QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance();
const { mms_order_type, mms_priority, mms_workorder_status } = proxy.useDict("mms_order_type", "mms_priority", "mms_workorder_status");
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mms_workorder_index')

const dataList = ref([]);
const open = ref(false);
const viewOpen = ref(false);
const viewData = ref({});
const loading = ref(true);
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
const pauseOpen = ref(false);
const closeOpen = ref(false);
const closeTitle = ref("");
const closeLabel = ref("");
const logOpen = ref(false);
const auditLogList = ref([]);

// 状态标签列表
const statusTabList = computed(() => {
  if (!mms_workorder_status.value) return [];
  return mms_workorder_status.value.map(d => ({ label: d.label, value: d.value }));
});

// 列显隐配置
const defaultColumns = {
  workOrderNo: { label: '工单编号', visible: true },
  orderType: { label: '工单类型', visible: true },
  productCode: { label: '产品编码', visible: true },
  productName: { label: '产品名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  planQty: { label: '计划数量', visible: true },
  finishedQty: { label: '完工数量', visible: true },
  qualifiedQty: { label: '合格数量', visible: true },
  priority: { label: '优先级', visible: true },
  status: { label: '工单状态', visible: true },
  planStart: { label: '计划开工', visible: true },
  planFinish: { label: '计划完工', visible: true },
  createTime: { label: '创建时间', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('mms_workorder_columns')
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

// 当前已选筛选条件数
const activeFilterCount = computed(() => {
  let count = 0;
  if (queryParams.value.workOrderNo) count++;
  if (queryParams.value.productCode) count++;
  if (queryParams.value.productName) count++;
  if (queryParams.value.orderType) count++;
  if (queryParams.value.status) count++;
  if (queryParams.value.priority) count++;
  if (queryParams.value.bomNo) count++;
  if (queryParams.value.routeNo) count++;
  if (queryParams.value.resourceName) count++;
  if (dateRange.value && dateRange.value.length === 2) count++;
  return count;
});

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    workOrderNo: undefined,
    productCode: undefined,
    productName: undefined,
    orderType: undefined,
    priority: undefined,
    status: undefined,
    bomNo: undefined,
    routeNo: undefined,
    resourceName: undefined,
    params: {}
  },
  rules: {
    orderType: [{ required: true, message: "请选择工单类型", trigger: "change" }],
    productCode: [{ required: true, message: "请输入产品编码", trigger: "blur" }],
    productName: [{ required: true, message: "请输入产品名称", trigger: "blur" }],
    planQty: [{ required: true, message: "请输入计划数量", trigger: "blur" }]
  },
  pauseForm: {},
  closeForm: {}
});

const { queryParams, form, rules, pauseForm, closeForm } = toRefs(data);

function getList() {
  loading.value = true;
  listWorkOrder(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    dataList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    applySavedWidths();
    updateStatusCounts(response.rows);
  });
}

function updateStatusCounts(rows) {
  const counts = { all: total.value };
  if (mms_workorder_status.value) {
    mms_workorder_status.value.forEach(d => {
      counts[d.value] = rows.filter(r => r.status === d.value).length;
    });
  }
  statusCounts.value = counts;
}

function handleQuery() {
  showAdvanced.value = false;
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value.workOrderNo = undefined;
  queryParams.value.productCode = undefined;
  queryParams.value.productName = undefined;
  queryParams.value.orderType = undefined;
  queryParams.value.priority = undefined;
  queryParams.value.status = undefined;
  queryParams.value.bomNo = undefined;
  queryParams.value.routeNo = undefined;
  queryParams.value.resourceName = undefined;
  dateRange.value = [];
  queryParams.value.params = {};
  activeStatusTab.value = 'all';
  handleQuery();
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status;
  queryParams.value.status = status === "all" ? undefined : status;
  handleQuery();
}

function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop;
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc';
  } else {
    queryParams.value.params.orderByColumn = undefined;
    queryParams.value.params.isAsc = undefined;
  }
  getList();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.workOrderId);
  single.value = selection.length !== 1;
  multiple.value = !selection.length;
}

function reset() {
  form.value = {
    workOrderNo: undefined,
    orderType: undefined,
    mpsNo: undefined,
    demandNo: undefined,
    productCode: undefined,
    productName: undefined,
    specModel: undefined,
    unit: undefined,
    planQty: undefined,
    bomNo: undefined,
    routeNo: undefined,
    resourceName: undefined,
    planStart: undefined,
    planFinish: undefined,
    priority: undefined,
    status: undefined,
    remark: undefined,
  };
  proxy.resetForm("formRef");
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = "新增工单";
}

function handleUpdate(row) {
  reset();
  const id = row.workOrderId || ids.value[0];
  getWorkOrder(id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改工单";
  });
}

function handleView(row) {
  getWorkOrder(row.workOrderId).then(response => {
    viewData.value = response.data;
    viewOpen.value = true;
  });
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.workOrderId != null) {
        updateWorkOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addWorkOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function cancel() {
  open.value = false;
  reset();
}

function handleDelete(row) {
  const delIds = row.workOrderId || ids.value;
  proxy.$modal.confirm('是否确认删除选中的工单？').then(function() {
    return delWorkOrder(delIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

function handleExport() {
  proxy.download("mms/workorder/export", { ...queryParams.value }, `workorder_${new Date().getTime()}.xlsx`);
}

// ===== 业务操作 =====

function handleRelease(row) {
  proxy.$modal.confirm('是否确认下达工单"' + row.workOrderNo + '"？').then(function() {
    return releaseWorkOrder(row.workOrderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("下达成功");
  }).catch(() => {});
}

function handlePause(row) {
  pauseForm.value = { workOrderId: row.workOrderId, workOrderNo: row.workOrderNo, pauseReason: "" };
  pauseOpen.value = true;
}

function submitPause() {
  pauseWorkOrder(pauseForm.value.workOrderId, pauseForm.value.pauseReason).then(() => {
    pauseOpen.value = false;
    getList();
    proxy.$modal.msgSuccess("暂停成功");
  });
}

function handleResume(row) {
  proxy.$modal.confirm('是否确认恢复工单"' + row.workOrderNo + '"？').then(function() {
    return resumeWorkOrder(row.workOrderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("恢复成功");
  }).catch(() => {});
}

function handleFinish(row) {
  proxy.$modal.confirm('是否确认完工工单"' + row.workOrderNo + '"？').then(function() {
    return finishWorkOrder(row.workOrderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("完工成功");
  }).catch(() => {});
}

function handleClose(row) {
  closeTitle.value = "工单关闭";
  closeLabel.value = "关闭备注";
  closeForm.value = { workOrderId: row.workOrderId, workOrderNo: row.workOrderNo, remark: "", type: "close" };
  closeOpen.value = true;
}

function handleCancel(row) {
  closeTitle.value = "工单作废";
  closeLabel.value = "作废原因";
  closeForm.value = { workOrderId: row.workOrderId, workOrderNo: row.workOrderNo, remark: "", type: "cancel" };
  closeOpen.value = true;
}

function submitClose() {
  if (closeForm.value.type === "close") {
    closeWorkOrder(closeForm.value.workOrderId, closeForm.value.remark).then(() => {
      closeOpen.value = false;
      getList();
      proxy.$modal.msgSuccess("关闭成功");
    });
  } else {
    cancelWorkOrder(closeForm.value.workOrderId, closeForm.value.remark).then(() => {
      closeOpen.value = false;
      getList();
      proxy.$modal.msgSuccess("作废成功");
    });
  }
}

function handleAuditLog(row) {
  getWorkOrderAuditLog(row.workOrderId).then(response => {
    auditLogList.value = response.data;
    logOpen.value = true;
  });
}

// ===== 字典辅助函数 =====
function orderTypeLabel(type) {
  const item = mms_order_type.value.find(d => d.value == type);
  return item ? item.label : '-';
}

function statusLabel(status) {
  const item = mms_workorder_status.value.find(d => d.value == status);
  return item ? item.label : '-';
}

function badgeClass(status) {
  const map = {
    '0': 'amber',    // 草稿
    '1': 'blue',     // 已下达
    '2': 'blue',     // 生产中
    '3': 'amber',    // 待完工
    '4': 'green',    // 已完工
    '5': 'gray',     // 已关闭
    '6': 'gray',     // 已作废
    '7': 'amber',    // 已暂停
    '8': 'red'       // 异常
  };
  return map[status] || 'gray';
}

function statusTabClass(value) {
  const map = {
    '0': 'tab-draft',
    '1': 'tab-audit',
    '2': 'tab-audit',
    '3': 'tab-draft',
    '4': 'tab-done',
    '5': 'tab-void',
    '6': 'tab-void',
    '7': 'tab-draft',
    '8': 'tab-reject'
  };
  return map[value] || '';
}

function priorityLabel(priority) {
  const item = mms_priority.value.find(d => d.value == priority);
  return item ? item.label : '-';
}

function priorityBadgeClass(priority) {
  const map = { '0': 'red', '1': 'amber', '2': 'blue' };
  return map[priority] || 'gray';
}

getList();
</script>

<style scoped>
.mms-workorder-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.mms-workorder-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.mms-workorder-page .filter-card { padding:14px 20px 16px; }
.mms-workorder-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.mms-workorder-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.mms-workorder-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.mms-workorder-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.mms-workorder-page .filter-card .adv-link:hover { color:var(--brand-600); }
.mms-workorder-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.mms-workorder-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.mms-workorder-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.mms-workorder-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.mms-workorder-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.mms-workorder-page .filter-card .filter-buttons { display:flex; gap:8px; }
.mms-workorder-page .field { display:flex; flex-direction:column; gap:6px; }
.mms-workorder-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.mms-workorder-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.mms-workorder-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.mms-workorder-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.mms-workorder-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.mms-workorder-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.mms-workorder-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.mms-workorder-page .field .control :deep(.el-select) { width:100%; }
.mms-workorder-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.mms-workorder-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.mms-workorder-page .toolbar .left, .mms-workorder-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.mms-workorder-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.mms-workorder-page .table-wrap { overflow-x:auto; }
.mms-workorder-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.mms-workorder-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.mms-workorder-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.mms-workorder-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.mms-workorder-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.mms-workorder-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.mms-workorder-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.mms-workorder-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .mms-workorder-page .badge.amber .dot { background:var(--amber-500); }
.mms-workorder-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .mms-workorder-page .badge.blue .dot { background:var(--blue-500); }
.mms-workorder-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .mms-workorder-page .badge.green .dot { background:var(--green-500); }
.mms-workorder-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .mms-workorder-page .badge.red .dot { background:var(--red-500); }
.mms-workorder-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.mms-workorder-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .mms-workorder-page .badge.gray .dot { background:var(--ink-400); }
.mms-workorder-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.mms-workorder-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.mms-workorder-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.mms-workorder-page .tabs-track::-webkit-scrollbar { display:none; }
.mms-workorder-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.mms-workorder-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.mms-workorder-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; }
.mms-workorder-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.mms-workorder-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.mms-workorder-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.mms-workorder-page .status-tab.is-active .dot { background:var(--brand-500); }
.mms-workorder-page .status-tab.tab-draft .dot { background:var(--amber-500); } .mms-workorder-page .status-tab.tab-draft .count { background:var(--amber-50); color:var(--amber-700); } .mms-workorder-page .status-tab.is-active.tab-draft .count { background:var(--amber-500); color:#fff; }
.mms-workorder-page .status-tab.tab-audit .dot { background:var(--blue-500); } .mms-workorder-page .status-tab.tab-audit .count { background:var(--blue-50); color:var(--blue-700); } .mms-workorder-page .status-tab.is-active.tab-audit .count { background:var(--blue-500); color:#fff; }
.mms-workorder-page .status-tab.tab-done .dot { background:var(--green-500); } .mms-workorder-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); } .mms-workorder-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
.mms-workorder-page .status-tab.tab-reject .dot { background:var(--red-500); } .mms-workorder-page .status-tab.tab-reject .count { background:var(--red-50); color:var(--red-700); } .mms-workorder-page .status-tab.is-active.tab-reject .count { background:var(--red-500); color:#fff; }
.mms-workorder-page .status-tab.tab-void .dot { background:var(--ink-400); } .mms-workorder-page .status-tab.tab-void .count { background:var(--ink-100); color:var(--ink-500); } .mms-workorder-page .status-tab.is-active.tab-void .count { background:var(--ink-400); color:#fff; }
.mms-workorder-page .tip-pill { display:inline-flex; align-items:center; gap:5px; height:30px; padding:0 10px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; transition:all .15s var(--ease-out); flex-shrink:0; white-space:nowrap; }
.mms-workorder-page .tip-pill:hover { background:var(--amber-50); border-color:var(--amber-500); color:#7c2d12; }
.mms-workorder-page .tip-pill .el-icon { font-size:14px; color:var(--amber-700); }
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
.rd-detail-header { display: flex; align-items: center; gap: 10px; padding: 8px 16px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -25px; right: -10px; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.12) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 34px; height: 34px; border-radius: 8px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 2px 8px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 16px; font-weight: 700; color: #fff; letter-spacing: -0.02em; white-space: nowrap; }
.rd-detail-header-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.rd-detail-header-divider { width: 1px; height: 16px; background: rgb(255 255 255 / 0.3); flex-shrink: 0; }
.rd-detail-header-no { font-size: 12px; font-weight: 500; color: rgb(255 255 255 / 0.85); font-variant-numeric: tabular-nums; white-space: nowrap; }
.rd-page { max-width: 760px; margin: 0 auto; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 72px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-value--muted { color: #9ca3af; font-style: italic; }
@media (max-width:1100px) { .mms-workorder-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .mms-workorder-page .filter-card .filter-bar { grid-template-columns:1fr; } }
</style>