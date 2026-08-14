<template>
  <div class="app-container qms-task-page">
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
          <label>任务编号</label>
          <div class="control">
            <el-input v-model="queryParams.taskNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>检验类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_insp_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>任务状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_task_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>检验结果</label>
          <div class="control is-select">
            <el-select v-model="queryParams.inspectResult" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in qms_insp_result" :key="d.value" :label="d.label" :value="d.value" />
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
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
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
          <button v-for="s in qms_task_status" :key="s.value"
            class="status-tab"
            :class="[taskStatusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:task:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['qms:task:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['qms:task:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:task:export']">
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
          <el-table-column label="任务编号" prop="taskNo" :width="colWidth('taskNo', 150)" resizable show-overflow-tooltip />
          <el-table-column label="检验类型" prop="taskType" :width="colWidth('taskType', 100)" resizable align="center">
            <template #header><span>检验类型</span><el-tooltip content="检验类型包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope"><span class="badge violet">{{ inspTypeLabel(scope.row.taskType) }}</span></template>
          </el-table-column>
          <el-table-column label="来源单号" prop="sourceNo" :width="colWidth('sourceNo', 150)" resizable show-overflow-tooltip />
          <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 140)" resizable show-overflow-tooltip />
          <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip />
          <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable show-overflow-tooltip />
          <el-table-column label="送检数量" prop="inspectQty" :width="colWidth('inspectQty', 100)" resizable align="center" />
          <el-table-column label="样本量" prop="sampleSize" :width="colWidth('sampleSize', 80)" resizable align="center" />
          <el-table-column label="Ac" prop="acVal" :width="colWidth('acVal', 60)" resizable align="center">
            <template #header><span>Ac</span><el-tooltip content="接收数（Acceptance Number），即抽样检验中允许的最大不合格品数，当样本中的不合格品数≤Ac时判定批合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="Re" prop="reVal" :width="colWidth('reVal', 60)" resizable align="center">
            <template #header><span>Re</span><el-tooltip content="拒收数（Rejection Number），即抽样检验中拒收的最小不合格品数，当样本中的不合格品数≥Re时判定批不合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          </el-table-column>
          <el-table-column label="任务状态" prop="taskStatus" :width="colWidth('taskStatus', 100)" resizable align="center">
            <template #default="scope"><span class="badge" :class="taskBadgeClass(scope.row.taskStatus)"><span class="dot"></span>{{ taskStatusLabel(scope.row.taskStatus) }}</span></template>
          </el-table-column>
          <el-table-column label="检验结果" prop="inspectResult" :width="colWidth('inspectResult', 100)" resizable align="center">
            <template #default="scope">
              <span v-if="scope.row.inspectResult" class="badge" :class="scope.row.inspectResult === '1' ? 'green' : 'red'"><span class="dot"></span>{{ inspResultLabel(scope.row.inspectResult) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="检验员" prop="inspectorName" :width="colWidth('inspectorName', 100)" resizable align="center" />
          <el-table-column label="检验时间" prop="inspectTime" :width="colWidth('inspectTime', 160)" resizable align="center" />
          <el-table-column label="复检" prop="isRecheck" :width="colWidth('isRecheck', 70)" resizable align="center">
            <template #default="scope"><span v-if="scope.row.isRecheck === '1'" class="badge orange"><span class="dot"></span>是</span><span v-else>-</span></template>
          </el-table-column>
          <el-table-column label="操作" width="240" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Printer" @click="handlePrintReport(scope.row)" v-hasPermi="['qms:task:query']" v-if="scope.row.taskStatus === '2'">打印</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:task:edit']" v-if="scope.row.taskStatus === '0'">修改</el-button>
              <el-button link type="success" icon="Check" @click="handleInspect(scope.row)" v-hasPermi="['qms:task:inspect']" v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'">录入</el-button>
              <el-button link type="warning" icon="Refresh" @click="handleRecheck(scope.row)" v-hasPermi="['qms:task:add']" v-if="scope.row.taskStatus === '2' && scope.row.inspectResult === '2' && scope.row.isRecheck !== '1'">复检</el-button>
              <el-button link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-hasPermi="['qms:task:edit']" v-if="scope.row.taskStatus === '0' || scope.row.taskStatus === '1'">作废</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H1l8-8 8 8h-8v8z" transform="rotate(90 12 12)"/><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="任务编号" prop="taskNo"><el-input v-model="form.taskNo" placeholder="保存后自动生成" readonly /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="taskType"><template #label><span>检验类型</span><el-tooltip content="检验类型包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.taskType" style="width: 100%"><el-option v-for="d in qms_insp_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="来源类型" prop="sourceType" required><el-select v-model="form.sourceType" clearable placeholder="请选择" style="width: 100%"><el-option v-for="d in qms_source_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="来源单号" prop="sourceNo"><el-input v-model="form.sourceNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_material')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料与批次</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_material }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_material">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" readonly placeholder="请选择物料" style="width: 100%" @click="openMaterialPicker"><template #append><el-button icon="Search" @click="openMaterialPicker" /></template><template #suffix><el-icon v-if="form.materialCode" class="clear-icon" @click.stop="clearMaterial"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" readonly placeholder="选择物料后自动带出" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-select v-model="form.supplierId" filterable clearable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次号" prop="batchNo" required><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_sample')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>抽样与检验</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_sample }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_sample">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="送检数量" prop="inspectQty"><el-input-number v-model="form.inspectQty" :min="0" :precision="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="6"><el-form-item prop="aqlLevel" required><template #label><span>AQL等级</span><el-tooltip content="AQL（Acceptable Quality Limit）即可接受质量限，表示检验批中允许的最大不合格品率，以百分比表示。常用值为2.5%（一般检验）、1.5%（加严检验）、4.0%（放宽检验）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.aqlLevel" placeholder="如：2.5（%）"><template #append>%</template></el-input></el-form-item></el-col>
                <el-col :span="6"><el-form-item prop="inspectLevel" required><template #label><span>检验严格度</span><el-tooltip content="抽样检验的严格度等级，分为：正常检验、加严检验、放宽检验。根据产品质量历史动态调整检验严格度" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.inspectLevel" style="width: 100%"><el-option v-for="d in qms_inspect_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="6"><el-form-item prop="inspectMethod"><template #label><span>检验水平</span><el-tooltip content="检验水平决定了批量与样本量之间的关系。OQC出货检验默认使用S-4（特殊检验水平，样本量小）；IQC/IPQC/FQC默认使用II（一般检验水平，标准样本量）。检验水平越高样本量越大，检验判别能力越强" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.inspectMethod" style="width: 100%"><el-option label="S-1（特殊-极小）" value="S-1" /><el-option label="S-2（特殊-小）" value="S-2" /><el-option label="S-3（特殊-中）" value="S-3" /><el-option label="S-4（特殊-大）" value="S-4" /><el-option label="I（一般-低）" value="I" /><el-option label="II（一般-标准）" value="II" /><el-option label="III（一般-高）" value="III" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="量检具" prop="gaugeId"><el-select v-model="form.gaugeId" filterable clearable placeholder="请选择" style="width: 100%" @change="onGaugeChange"><el-option v-for="g in gaugeOptions" :key="g.gaugeId" :label="g.gaugeNo ? g.gaugeNo + ' - ' + g.gaugeName : g.gaugeName" :value="g.gaugeId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检验员" prop="inspectorName" required><el-input v-model="form.inspectorName" readonly placeholder="请选择检验员" style="width: 100%" @click="openUserPicker"><template #append><el-button icon="Search" @click="openUserPicker" /></template><template #suffix><el-icon v-if="form.inspectorName" class="clear-icon" @click.stop="clearInspector"><CircleClose /></el-icon></template></el-input></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">检验任务详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_basic')"><div class="rd-card-title">基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_basic" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ viewData.taskNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验类型<el-tooltip content="检验类型包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="qms_insp_type" :value="viewData.taskType" /></div></div>
            <div class="rd-item"><span class="rd-label">来源类型<el-tooltip content="检验任务的来源类型，包括：采购入库、生产工单、出货交付、手动创建等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="qms_source_type" :value="viewData.sourceType" /></div></div>
            <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ viewData.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">送检数量</span><div class="rd-value">{{ viewData.inspectQty != null ? viewData.inspectQty : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">量检具<el-tooltip content="用于检验测量的工具或仪器，如卡尺、千分尺、通止规等，从量检具库中选择" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.gaugeNo ? viewData.gaugeNo + ' - ' + (viewData.gaugeName || '') : (viewData.gaugeName || '-') }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_aql')"><div class="rd-card-title">AQL抽样信息</div><el-tooltip content="AQL（Acceptable Quality Limit）即可接受质量限，是抽样检验标准中的核心参数，用于判定检验批是否合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_aql }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_aql" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">AQL等级<el-tooltip content="AQL（Acceptable Quality Limit）即可接受质量限，表示检验批中允许的最大不合格品率，以百分比表示。常用值为2.5%（一般检验）、1.5%（加严检验）、4.0%（放宽检验）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.aqlLevel != null ? viewData.aqlLevel + '%' : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验严格度<el-tooltip content="抽样检验的严格度等级，分为：正常检验、加严检验、放宽检验。根据产品质量历史动态调整检验严格度" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="qms_inspect_level" :value="viewData.inspectLevel" /></div></div>
            <div class="rd-item"><span class="rd-label">检验水平<el-tooltip content="检验水平决定了批量与样本量之间的关系，分为特殊检验水平（S-1~S-4）和一般检验水平（I~III）。OQC出货检验默认使用S-4" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.inspectMethod || 'II' }}</div></div>
            <div class="rd-item"><span class="rd-label">样本量(n)<el-tooltip content="n为样本量（Sample Size），即从检验批中抽取的样本数量，由批量大小和检验水平查表确定" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.sampleSize != null ? viewData.sampleSize : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">接收数(Ac)<el-tooltip content="Ac为接收数（Acceptance Number），即抽样检验中允许的最大不合格品数，当样本中的不合格品数≤Ac时判定批合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.acVal != null ? viewData.acVal : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">拒收数(Re)<el-tooltip content="Re为拒收数（Rejection Number），即抽样检验中拒收的最小不合格品数，当样本中的不合格品数≥Re时判定批不合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.reVal != null ? viewData.reVal : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('v_result')"><div class="rd-card-title">判定结果</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_result }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_result" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">任务状态</span><div class="rd-value">{{ taskStatusLabel(viewData.taskStatus) }}</div></div>
            <div class="rd-item"><span class="rd-label">检验结果</span><div class="rd-value">{{ viewData.inspectResult ? inspResultLabel(viewData.inspectResult) : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">最高缺陷等级<el-tooltip content="本次检验中所有不合格缺陷中最高（最严重）的缺陷等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value"><dict-tag :options="qms_defect_level" :value="viewData.defectLevel" /></div></div>
            <div class="rd-item"><span class="rd-label">检验员</span><div class="rd-value">{{ viewData.inspectorName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验时间</span><div class="rd-value">{{ viewData.inspectTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">是否复检<el-tooltip content="检验结果为不合格时，可发起复检任务对同批次物料重新进行抽样检验" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></span><div class="rd-value">{{ viewData.isRecheck === '1' ? '是' : '否' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.itemList && viewData.itemList.length > 0">
          <div class="rd-card-header" @click="toggleCard('v_detail')"><div class="rd-card-title">检验明细</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_detail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_detail" style="display:block; padding:0">
            <el-table :data="viewData.itemList" border class="app-table" style="width: 100%">
              <el-table-column label="序号" prop="sampleSeq" width="60" align="center" />
              <el-table-column label="检验项目" prop="stdName" min-width="200" show-overflow-tooltip />
              <el-table-column label="实测值" prop="measuredVal" width="120" align="center" />
              <el-table-column label="单项判定" prop="itemResult" width="100" align="center">
                <template #default="scope"><span class="badge" :class="scope.row.itemResult === '1' ? 'green' : 'red'"><span class="dot"></span>{{ scope.row.itemResult === '1' ? '合格' : '不合格' }}</span></template>
              </el-table-column>
              <el-table-column label="缺陷代码" prop="defectCode" width="100" align="center" />
              <el-table-column label="缺陷名称" prop="defectName" min-width="160" show-overflow-tooltip />
              <el-table-column label="缺陷等级" prop="defectLevel" width="100" align="center">
                <template #header><span>缺陷等级</span><el-tooltip content="不合格品缺陷的严重程度等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                <template #default="scope"><dict-tag :options="qms_defect_level" :value="scope.row.defectLevel" /></template>
              </el-table-column>
              <el-table-column label="缺陷数量" prop="defectQty" width="100" align="center">
                <template #header><span>缺陷数量</span><el-tooltip content="同一检验项目中不合格缺陷的数量" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                <template #default="scope">{{ scope.row.defectQty != null ? scope.row.defectQty : '-' }}</template>
              </el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header" @click="toggleCard('v_remark')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.v_remark }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.v_remark" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="检验任务业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <!-- 一、检验任务释义 -->
        <h4>一、检验任务释义</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">什么是检验任务？</div>
          <div class="highlight-card-body">
            <strong>检验任务（Inspection Task）</strong>是质量管理中用于对物料、半成品或成品进行抽样检验的核心单据。根据检验类型（IQC来料检验、IPQC过程检验、FQC成品检验、OQC出货检验等），系统自动生成检验任务，检验员按照抽样标准（AQL）对样本进行检验，记录实测值和判定结果，最终形成检验报告。<br/><br/>
            检验任务基于<strong>AQL抽样标准</strong>（Acceptable Quality Limit，可接受质量限），通过统计抽样方法判定检验批是否合格。当样本中的不合格品数≤接收数(Ac)时判定批合格，≥拒收数(Re)时判定批不合格，满足ISO 2859、GB/T 2828等抽样检验标准的要求。
          </div>
        </div>

        <!-- 二、检验任务状态流转图 -->
        <h4>二、检验任务状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="info">待检验</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「录入」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="warning">检验中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">提交结果</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已判定</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="success">已判定</el-tag>
            <el-tag size="small" type="warning">不合格时可发起复检</el-tag>
          </div>
        </div>
        <div class="status-flow" style="margin-top: 8px;">
          <div class="flow-item">
            <el-tag type="info">待检验</el-tag>
            <el-tag size="small" type="danger">或</el-tag>
            <el-tag type="warning">检验中</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
            <el-tag size="small" type="primary">点击「作废」</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="danger">已作废</el-tag>
            <el-tag size="small" type="info">异常终止</el-tag>
          </div>
        </div>

        <!-- 三、各状态说明 -->
        <h4>三、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待检验">检验任务创建后的初始状态。可修改任务信息、录入检验结果。点击「录入」开始检验，系统自动将任务状态变为检验中</el-descriptions-item>
          <el-descriptions-item label="检验中">检验员已开始检验，正在录入检验数据。点击「录入」继续录入，完成后提交检验结果，系统自动判定为合格或不合格</el-descriptions-item>
          <el-descriptions-item label="已判定">检验结果已提交并自动判定，检验报告已生成。可查看详情、打印报告。不合格时可发起复检</el-descriptions-item>
          <el-descriptions-item label="已作废">检验任务因异常情况被作废，不再有效。待检验和检验中状态的任务可手动作废</el-descriptions-item>
        </el-descriptions>

        <!-- 四、新增/修改表单填写指南 -->
        <h4>四、新增/修改表单填写指南</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">基本信息区</div>
          <div class="highlight-card-body">
            <p>• <strong>任务编号：</strong>检验任务的唯一标识编号，保存后由系统自动生成</p>
            <p>• <strong>检验类型：</strong>包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>来源类型：</strong>检验任务的来源类型，可选择：采购入库、生产工单、出货交付、手动创建<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>来源单号：</strong>关联的来源单据编号，便于追溯到源头</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">物料与批次区</div>
          <div class="highlight-card-body">
            <p>• <strong>物料编码：</strong>从物料库中选择待检验物料，选择后自动带出物料名称<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>物料名称：</strong>选择物料后自动带出，不可手动编辑</p>
            <p>• <strong>供应商：</strong>从供应商库中选择物料的供应方（IQC检验时选择）</p>
            <p>• <strong>批次号：</strong>待检验物料的批次号，用于质量追溯<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">抽样与检验区</div>
          <div class="highlight-card-body">
            <p>• <strong>送检数量：</strong>本次送检的物料数量<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>AQL等级：</strong>可接受质量限，表示检验批中允许的最大不合格品率，以百分比表示（%）。常用值为2.5%（一般检验）、1.5%（加严检验）、4.0%（放宽检验）<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>检验严格度：</strong>分为正常检验、加严检验、放宽检验。根据产品质量历史动态调整<span style="color: #f56c6c;">*必填</span></p>
            <p>• <strong>检验水平：</strong>决定批量与样本量的映射关系。OQC默认S-4（样本量小），其他默认II（标准）。选填，不填时系统按检验类型自动推断</p>
            <p>• <strong>量检具：</strong>从量检具库中选择，选择时同时显示编号和名称</p>
            <p>• <strong>检验员：</strong>从组织机构中选择执行检验的人员<span style="color: #f56c6c;">*必填</span></p>
          </div>
        </div>

        <!-- 五、AQL抽样标准说明 -->
        <h4>五、AQL抽样标准说明</h4>
        <div class="highlight-card highlight-success">
          <div class="highlight-card-title">什么是AQL抽样检验？</div>
          <div class="highlight-card-body">
            <strong>AQL（Acceptable Quality Limit，可接受质量限）</strong>是统计抽样检验的核心参数，表示检验批中允许的最大不合格品率。本系统抽样方案数据来源于国家标准 <strong>GB/T 2828.1-2012《计数抽样检验程序 第1部分：按接收质量限(AQL)检索的逐批检验抽样计划》</strong>（等同采用国际标准 ISO 2859-1:1999），适用于连续批的计数抽样检验，广泛用于进料检验（IQC）、过程检验（IPQC）、成品检验（FQC）和出货检验（OQC）等质量控制环节。
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">抽样方案核心参数</div>
          <div class="highlight-card-body">
            <p>基于AQL等级、批量大小和检验严格度，通过查表确定以下三个核心参数：</p>
            <p>• <strong>样本量(n)：</strong>从检验批中抽取的样本数量，由字码查表确定。样本量越大，检验结论越可靠。</p>
            <p>• <strong>接收数(Ac)：</strong>样本中允许的最大不合格品数。当样本中的不合格品数 ≤ Ac 时，判定该批合格予以接收。</p>
            <p>• <strong>拒收数(Re)：</strong>判定该批不合格的最小不合格品数。当样本中的不合格品数 ≥ Re 时，判定该批不合格予以拒收。通常 Re = Ac + 1。</p>
            <p style="color: #e6a23c;"><strong>特殊判定：</strong>当 Ac < 不合格品数 < Re 时，处于判定临界区，允许继续抽样或由质量主管根据实际情况判定。</p>
          </div>
        </div>

        <!-- 六、抽样规则详解 -->
        <h4>六、抽样规则详解</h4>
        <div class="highlight-card highlight-warning">
          <div class="highlight-card-title">检验水平选择规则</div>
          <div class="highlight-card-body">
            <p>检验水平决定了批量与样本量之间的关系，分一般检验水平和特殊检验水平两类：</p>
            <p>• <strong>一般检验水平 I（低水平）：</strong>样本量较小，适用于检验费用较高或检验时间较长的场景</p>
            <p>• <strong>一般检验水平 II（标准水平）：</strong>默认检验水平，样本量适中，适用于大部分常规检验场景</p>
            <p>• <strong>一般检验水平 III（高水平）：</strong>样本量较大，适用于对质量要求较高、检验费用较低的场景</p>
            <p>• <strong>特殊检验水平 S-1~S-4：</strong>样本量很小，适用于破坏性检验、检验费用极高或样本来源受限的特殊场景</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>检验水平越高，样本量越大，检验的判别能力越强，但检验成本也越高。系统默认使用一般检验水平 II。</p>
          </div>
        </div>
        <div class="highlight-card highlight-warning" style="margin-top: 12px;">
          <div class="highlight-card-title">字码与批量对应规则</div>
          <div class="highlight-card-body">
            <p>系统根据<strong>批量大小(N)</strong>和<strong>检验水平</strong>查表确定<strong>样本量字码</strong>，字码用字母 A~R 表示，对应不同的样本量：</p>
            <p>• <strong>批量范围：</strong>每条方案适用的批量区间（下限~上限），如 1~8、9~15、16~25 ... 500001 以上等</p>
            <p>• <strong>字码映射：</strong>批量越大，对应的字码越靠后（A→R），样本量也越大</p>
            <p>• <strong>自动匹配：</strong>系统根据实际送检数量自动匹配对应的批量区间和字码，无需人工干预</p>
            <p>• <strong>常见字码示例：</strong>字码 G 对应样本量 32（一般水平 II，批量 151~280）；字码 H 对应样本量 50（一般水平 II，批量 281~500）</p>
          </div>
        </div>
        <div class="highlight-card highlight-primary" style="margin-top: 12px;">
          <div class="highlight-card-title">检验严格度转换规则</div>
          <div class="highlight-card-body">
            <p>检验严格度分为<strong>正常</strong>、<strong>加严</strong>、<strong>放宽</strong>三种，根据检验历史动态调整：</p>
            <p>• <strong>正常检验：</strong>系统默认状态，按标准抽样方案执行检验</p>
            <p>• <strong>正常 → 加严：</strong>正常检验时，连续5批中有2批被拒收，则从下一批起转为加严检验</p>
            <p>• <strong>加严 → 正常：</strong>加严检验时，连续5批均被接收，则恢复正常检验</p>
            <p>• <strong>正常 → 放宽：</strong>正常检验时，连续10批均被接收且生产稳定，经质量主管批准后转为放宽检验</p>
            <p>• <strong>放宽 → 正常：</strong>放宽检验时，有1批被拒收或生产不稳定，立即恢复正常检验</p>
            <p>• <strong>加严 → 暂停：</strong>加严检验时累计5批被拒收，暂停抽样检验，待质量改进后恢复</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>加严检验会增大样本量或降低接收数，放宽检验会减小样本量，系统根据历史检验结果自动跟踪和提示严格度转换。</p>
          </div>
        </div>
        <div class="highlight-card highlight-success" style="margin-top: 12px;">
          <div class="highlight-card-title">批量判定规则</div>
          <div class="highlight-card-body">
            <p>检验结果录入完成后，系统根据以下规则自动判定检验批是否合格：</p>
            <p>1. <strong>合格判定：</strong>样本中不合格品总数(d) ≤ 接收数(Ac) → 判定批合格，予以接收</p>
            <p>2. <strong>不合格判定：</strong>样本中不合格品总数(d) ≥ 拒收数(Re) → 判定批不合格，予以拒收</p>
            <p>3. <strong>致命缺陷规则：</strong>只要样本中存在致命缺陷，不论数量是否超过Ac，直接判定批不合格，禁止放行</p>
            <p>4. <strong>严重缺陷规则：</strong>存在严重缺陷时，需经质量主管评审后决定是否接收</p>
            <p>5. <strong>不合格后处理：</strong>判定不合格后，系统自动阻断入库/流转，可发起不合格品报告(NCR)或复检任务</p>
          </div>
        </div>

        <!-- 七、检验结果录入说明 -->
        <h4>七、检验结果录入说明</h4>
        <div class="highlight-card highlight-primary">
          <div class="highlight-card-title">录入操作指南</div>
          <div class="highlight-card-body">
            <p>点击列表中的「录入」按钮进入检验结果录入界面，按以下步骤操作：</p>
            <p>1. <strong>填写实测值：</strong>对每个检验项目输入实际测量值</p>
            <p>2. <strong>单项判定：</strong>选择「合格」或「不合格」。选择不合格时，需填写缺陷信息</p>
            <p>3. <strong>缺陷信息：</strong>不合格时填写缺陷代码、缺陷名称、缺陷等级和缺陷数量</p>
            <p>4. <strong>提交结果：</strong>所有检验项目判定完成后，点击「提交检验结果」</p>
            <p style="color: #e6a23c;"><strong>提示：</strong>系统根据不合格品数与Ac/Re自动判定批量合格与否</p>
          </div>
        </div>

        <!-- 八、业务操作流程 -->
        <h4>八、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>创建检验任务：</strong>点击「新增」创建检验任务，选择检验类型、从物料库选择物料、从供应商库选择供应商，填写抽样参数，任务编号保存后自动生成
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>录入检验结果：</strong>在待检验状态下点击「录入」开始检验，任务自动切换为检验中状态。逐项填写实测值和判定结果
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>临时保存：</strong>检验过程中可点击「临时保存」保存已录入的数据，不触发判定，任务保持检验中状态，可稍后继续录入
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>提交检验结果：</strong>所有检验项目判定完成后点击「提交检验结果」，系统根据AQL标准自动判定批量合格与否，生成检验报告
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>查看与打印：</strong>已完成的检验任务可点击「查看」查看详情，点击「打印」打印检验报告
          </el-timeline-item>
          <el-timeline-item type="danger" :hollow="true">
            <strong>复检：</strong>检验结果为不合格时，可点击「复检」发起复检任务，重新进行抽样检验
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <!-- Material Picker -->
    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />

    <!-- User Picker -->
    <user-picker ref="userPickerRef" title="选择检验员" @confirm="onUserPickerConfirm" />

    <!-- Inspect Result Dialog -->
    <el-dialog v-model="inspectOpen" width="1360px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">检验结果录入</span>
        </div>
      </template>
      <div style="margin-bottom: 12px; padding: 12px 16px; background: var(--el-fill-color-light); border-radius: 6px; font-size: 14px; color: var(--el-text-color-secondary);">
        任务编号：<b>{{ inspectForm.taskNo }}</b> &nbsp;|&nbsp; 物料：<b>{{ inspectForm.materialName }}</b> &nbsp;|&nbsp; 批次：<b>{{ inspectForm.batchNo }}</b> &nbsp;|&nbsp; 样本量(n)：<b>{{ inspectForm.sampleSize }}</b> &nbsp;|&nbsp; Ac：<b>{{ inspectForm.acVal }}</b> &nbsp;|&nbsp; Re：<b>{{ inspectForm.reVal }}</b>
      </div>
      <div class="inspect-toolbar">
        <el-button type="primary" plain icon="Plus" size="small" @click="handleAddInspectRow">添加检验项</el-button>
        <el-button type="danger" plain icon="Delete" size="small" :disabled="!inspectSelectedRows.length" @click="handleDeleteSelectedRows">删除选中行</el-button>
      </div>
      <el-table :data="inspectPageData" border class="app-table" style="width: 100%" max-height="400" @selection-change="handleInspectSelectionChange">
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column label="序号" prop="sampleSeq" width="70" align="center" />
        <el-table-column label="检验项目" min-width="220" show-overflow-tooltip>
          <template #header><span>检验项目</span><span class="col-required"></span></template>
          <template #default="scope">
            <el-select v-model="scope.row.stdId" placeholder="请选择检验项目" size="small" filterable clearable @change="(val) => onStdChange(scope.row, val)">
              <el-option v-for="s in stdOptions" :key="s.stdId" :label="s.stdName + (s.stdCode ? ' (' + s.stdCode + ')' : '')" :value="s.stdId" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="实测值" min-width="160" align="center">
          <template #default="scope"><el-input v-model="scope.row.measuredVal" placeholder="请输入" size="small" /></template>
        </el-table-column>
        <el-table-column label="单项判定" width="130" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.itemResult" placeholder="选择" size="small" @change="onItemResultChange(scope.row)"><el-option label="合格" value="1" /><el-option label="不合格" value="2" /></el-select>
          </template>
        </el-table-column>
        <el-table-column label="缺陷代码" min-width="160" align="center">
          <template #header><span>缺陷代码</span><span v-if="true" class="col-required"></span></template>
          <template #default="scope">
            <el-select v-model="scope.row.defectCode" placeholder="请选择" size="small" filterable clearable :disabled="scope.row.itemResult !== '2'" @change="(val) => onDefectCodeChange(scope.row, val)">
              <el-option v-for="d in defectOptions" :key="d.defectId" :label="d.defectCode + ' - ' + d.defectName" :value="d.defectCode" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="缺陷名称" min-width="180">
          <template #header><span>缺陷名称</span><span class="col-required"></span></template>
          <template #default="scope"><el-input v-model="scope.row.defectName" placeholder="请输入" size="small" :disabled="scope.row.itemResult !== '2'" /></template>
        </el-table-column>
        <el-table-column label="缺陷等级" width="130" align="center">
          <template #header><span>缺陷等级</span><span class="col-required"></span><el-tooltip content="不合格品缺陷的严重程度等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          <template #default="scope">
            <el-select v-model="scope.row.defectLevel" placeholder="选择" size="small" :disabled="scope.row.itemResult !== '2'">
              <el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="缺陷数量" width="120" align="center">
          <template #header><span>缺陷数量</span><span class="col-required"></span><el-tooltip content="同一检验项目中不合格缺陷的数量，用于统计不合格品总数" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          <template #default="scope"><el-input-number v-model="scope.row.defectQty" :min="0" :max="999" :disabled="scope.row.itemResult !== '2'" size="small" style="width: 100px" /></template>
        </el-table-column>
      </el-table>
      <div class="inspect-pagination">
        <span class="page-info">共 {{ inspectPageTotal }} 条</span>
        <el-pagination
          v-model:current-page="inspectPageNum"
          :page-sizes="[10, 20, 50]"
          :page-size="inspectPageSize"
          :total="inspectPageTotal"
          layout="sizes, prev, pager, next, jumper"
          @current-change="inspectPageChange"
          @size-change="inspectSizeChange"
          small
        />
      </div>
      <template #footer>
        <div class="inspect-footer">
          <el-button type="info" plain icon="Document" @click="handleSaveDraft">临时保存</el-button>
          <el-button type="primary" @click="submitInspectResult">提交检验结果</el-button>
          <el-button @click="inspectOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTask">
import { listTask, getTask, addTask, updateTask, delTask, saveInspectResult, saveInspectDraft, createRecheckTask, voidTask, startInspect, getStatusCounts } from '@/api/qms/task'
import { listGauge } from '@/api/qms/gauge'
import { listDefect } from '@/api/qms/defect'
import { listStd } from '@/api/qms/std'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import UserPicker from '@/components/UserPicker/index.vue'
import { QuestionFilled, ArrowRight, CircleClose } from '@element-plus/icons-vue'

const { collapsedCards, toggleCard } = useDetailCard(['e_basic', 'e_material', 'e_sample', 'e_other', 'v_basic', 'v_aql', 'v_result', 'v_detail', 'v_remark'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_task_index')
const { qms_insp_type, qms_task_status, qms_insp_result, qms_inspect_level, qms_defect_level, qms_source_type } = proxy.useDict('qms_insp_type', 'qms_task_status', 'qms_insp_result', 'qms_inspect_level', 'qms_defect_level', 'qms_source_type')

const showStatusHelp = ref(false)
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0, '0': 0, '1': 0, '2': 0, '3': 0 })

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const inspectOpen = ref(false)
const inspectForm = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const gaugeOptions = ref([])
const defectOptions = ref([])
const stdOptions = ref([])
const supplierOptions = ref([])
const materialPickerRef = ref()
const userPickerRef = ref()

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, taskNo: undefined, taskType: undefined, taskStatus: undefined, inspectResult: undefined, materialCode: undefined, batchNo: undefined, params: {} },
  rules: {
    taskType: [{ required: true, message: '检验类型不能为空', trigger: 'change' }],
    sourceType: [{ required: true, message: '来源类型不能为空', trigger: 'change' }],
    materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'change' }],
    batchNo: [{ required: true, message: '批次号不能为空', trigger: 'blur' }],
    inspectQty: [{ required: true, message: '送检数量不能为空', trigger: 'blur' }],
    aqlLevel: [{ required: true, message: 'AQL等级不能为空', trigger: 'blur' }],
    inspectLevel: [{ required: true, message: '检验严格度不能为空', trigger: 'change' }],
    inspectorName: [{ required: true, message: '检验员不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.taskNo) count++
  if (queryParams.value.taskType) count++
  if (queryParams.value.taskStatus) count++
  if (queryParams.value.inspectResult) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.batchNo) count++
  return count
})

function inspTypeLabel(val) { const item = qms_insp_type.value.find(d => d.value == val); return item ? item.label : '-' }
function taskStatusLabel(val) { const item = qms_task_status.value.find(d => d.value == val); return item ? item.label : '-' }
function taskBadgeClass(val) { if (val == '0') return 'gray'; if (val == '1') return 'orange'; if (val == '2') return 'green'; if (val == '3') return 'red'; return 'gray' }
function inspResultLabel(val) { const item = qms_insp_result.value.find(d => d.value == val); return item ? item.label : '-' }

function loadStatusCounts() {
  getStatusCounts().then(res => {
    const data = res.data || {}
    statusCounts.value = {
      all: data.all || 0,
      '0': data['0'] || 0,
      '1': data['1'] || 0,
      '2': data['2'] || 0,
      '3': data['3'] || 0
    }
  }).catch(() => {})
}

function handleStatusTabClick(status) {
  activeStatusTab.value = status
  queryParams.value.taskStatus = status === 'all' ? undefined : status
  handleQuery()
}

function taskStatusTabClass(value) {
  const map = { '0': 'tab-pending', '1': 'tab-inspecting', '2': 'tab-done', '3': 'tab-void' }
  return map[value] || ''
}

const inspectPageNum = ref(1)
const inspectPageSize = ref(10)
const inspectPageTotal = computed(() => (inspectForm.value.itemList || []).length)
const inspectPageData = computed(() => {
  const list = inspectForm.value.itemList || []
  const start = (inspectPageNum.value - 1) * inspectPageSize.value
  return list.slice(start, start + inspectPageSize.value)
})
const inspectMaxPage = computed(() => Math.max(1, Math.ceil(inspectPageTotal.value / inspectPageSize.value)))
function inspectPageChange(page) { inspectPageNum.value = page }
function inspectSizeChange(size) { inspectPageSize.value = size; inspectPageNum.value = 1 }

const inspectSelectedRows = ref([])
function handleInspectSelectionChange(selection) {
  inspectSelectedRows.value = selection
}
function handleAddInspectRow() {
  if (!inspectForm.value.itemList) inspectForm.value.itemList = []
  const nextSeq = (inspectForm.value.itemList.length || 0) + 1
  inspectForm.value.itemList.push({ sampleSeq: nextSeq, stdId: undefined, stdName: '', measuredVal: '', itemResult: '1', defectCode: '', defectName: '', defectLevel: '', defectQty: 0 })
  const maxPage = Math.max(1, Math.ceil(inspectForm.value.itemList.length / inspectPageSize.value))
  inspectPageNum.value = maxPage
}
function handleDeleteInspectRow(pageIndex) {
  const start = (inspectPageNum.value - 1) * inspectPageSize.value
  const realIndex = start + pageIndex
  if (inspectForm.value.itemList.length <= 1) {
    proxy.$modal.msgWarning('至少保留一条检验项')
    return
  }
  inspectForm.value.itemList.splice(realIndex, 1)
  inspectForm.value.itemList.forEach((item, idx) => { item.sampleSeq = idx + 1 })
  inspectSelectedRows.value = []
  const maxPage = Math.max(1, Math.ceil(inspectForm.value.itemList.length / inspectPageSize.value))
  if (inspectPageNum.value > maxPage) inspectPageNum.value = maxPage
}
function handleDeleteSelectedRows() {
  if (!inspectSelectedRows.value.length) return
  if (inspectForm.value.itemList.length <= inspectSelectedRows.value.length) {
    proxy.$modal.msgWarning('至少保留一条检验项')
    return
  }
  const toDelete = inspectSelectedRows.value
  inspectForm.value.itemList = inspectForm.value.itemList.filter(item => !toDelete.includes(item))
  inspectForm.value.itemList.forEach((item, idx) => { item.sampleSeq = idx + 1 })
  inspectSelectedRows.value = []
  const maxPage = Math.max(1, Math.ceil(inspectForm.value.itemList.length / inspectPageSize.value))
  if (inspectPageNum.value > maxPage) inspectPageNum.value = maxPage
}
function onItemResultChange(row) {
  if (row.itemResult === '2') {
    row.defectQty = 1
  } else {
    row.defectCode = ''
    row.defectName = ''
    row.defectLevel = ''
    row.defectQty = 0
  }
}
function onDefectCodeChange(row, val) {
  if (val) {
    const defect = defectOptions.value.find(d => d.defectCode === val)
    if (defect) {
      row.defectName = defect.defectName
      row.defectLevel = defect.defectLevel
    }
  } else {
    row.defectName = ''
    row.defectLevel = ''
  }
}

function getList() { loading.value = true; listTask(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.taskNo = undefined; queryParams.value.taskType = undefined; queryParams.value.taskStatus = undefined; queryParams.value.inspectResult = undefined; queryParams.value.materialCode = undefined; queryParams.value.batchNo = undefined; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.taskId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { taskId: undefined, taskNo: undefined, taskType: 'IQC', sourceType: undefined, sourceNo: undefined, materialId: undefined, materialCode: undefined, materialName: undefined, supplierId: undefined, supplierName: undefined, batchNo: undefined, inspectQty: undefined, aqlLevel: '2.5', inspectLevel: '1', inspectMethod: undefined, gaugeId: undefined, gaugeName: undefined, gaugeNo: undefined, inspectorId: undefined, inspectorName: undefined, status: '0', remark: undefined }; proxy.resetForm('taskRef') }
function loadGaugeOptions() { listGauge({ pageNum: 1, pageSize: 999, gaugeStatus: '0' }).then(res => { gaugeOptions.value = res.rows || [] }) }
function loadDefectOptions() { listDefect({ pageNum: 1, pageSize: 999, status: '0' }).then(res => { defectOptions.value = res.rows || [] }) }
function loadStdOptions() { listStd({ pageNum: 1, pageSize: 999, status: '0' }).then(res => { stdOptions.value = res.rows || [] }) }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999, status: '0' }).then(res => { supplierOptions.value = res.rows || [] }) }
function onStdChange(row, val) {
  if (val) {
    const std = stdOptions.value.find(s => s.stdId === val)
    if (std) {
      row.stdName = std.stdName
      row.stdCode = std.stdCode
      row.specReq = std.valMin != null && std.valMax != null ? std.valMin + '~' + std.valMax : (std.targetVal != null ? std.targetVal : '')
      row.unit = std.unit
    }
  } else {
    row.stdName = ''
    row.stdCode = ''
    row.specReq = ''
    row.unit = ''
  }
}

/** 打开物料选择弹窗 */
function openMaterialPicker() { materialPickerRef.value.open(form.value.materialId) }
/** 物料选择确认回调 */
function onMaterialPickerConfirm(material) { form.value.materialId = material.materialId; form.value.materialCode = material.materialCode; form.value.materialName = material.materialName }
/** 清空物料 */
function clearMaterial() { form.value.materialId = undefined; form.value.materialCode = undefined; form.value.materialName = undefined }

/** 供应商选择变更 */
function onSupplierChange(val) {
  if (val) {
    const supplier = supplierOptions.value.find(s => s.supplierId === val)
    if (supplier) { form.value.supplierName = supplier.supplierName }
  } else {
    form.value.supplierName = undefined
  }
}

/** 量检具选择变更 — 同时带出名称和编号 */
function onGaugeChange(val) {
  if (val) {
    const gauge = gaugeOptions.value.find(g => g.gaugeId == val)
    if (gauge) { form.value.gaugeName = gauge.gaugeName; form.value.gaugeNo = gauge.gaugeNo }
  } else {
    form.value.gaugeName = undefined; form.value.gaugeNo = undefined
  }
}

/** 打开检验员选择弹窗 */
function openUserPicker() { userPickerRef.value.open(form.value.inspectorId) }
/** 检验员选择确认回调 */
function onUserPickerConfirm(user) { form.value.inspectorId = user.userId; form.value.inspectorName = user.nickName }
/** 清空检验员 */
function clearInspector() { form.value.inspectorId = undefined; form.value.inspectorName = undefined }

function handleAdd() { reset(); open.value = true; title.value = '添加检验任务' }
function handleUpdate(row) { reset(); getTask(row.taskId || ids.value[0]).then(res => { form.value = res.data; syncGaugeInfo(); open.value = true; title.value = '修改检验任务' }) }
function handleView(row) { getTask(row.taskId).then(res => { viewData.value = res.data; syncViewGaugeInfo(); viewOpen.value = true }) }
function handlePrintReport(row) { proxy.$router.push({ path: '/qms/task-report/index', query: { taskId: row.taskId } }) }
function handleInspect(row) {
  // 如果是待检状态，先调用 startInspect 将状态变为检验中
  const shouldStart = row.taskStatus === '0'
  const startPromise = shouldStart ? startInspect(row.taskId) : Promise.resolve()
  startPromise.then(() => {
    if (shouldStart) { getList() }
    return getTask(row.taskId)
  }).then(res => {
    inspectForm.value = res.data
    if (!inspectForm.value.itemList || inspectForm.value.itemList.length === 0) {
      inspectForm.value.itemList = [{ sampleSeq: 1, stdId: undefined, stdName: '', measuredVal: '', itemResult: '1', defectCode: '', defectName: '', defectLevel: '', defectQty: 0 }]
    }
    inspectForm.value.itemList.forEach(i => {
      if (i.defectQty == null) i.defectQty = i.itemResult === '2' ? 1 : 0
      if (i.itemResult !== '2' && !i.defectCode) { i.defectCode = ''; i.defectName = ''; i.defectLevel = ''; i.defectQty = 0 }
    })
    inspectPageNum.value = 1
    inspectOpen.value = true
  })
}
function handleRecheck(row) { proxy.$modal.confirm('确认对任务"' + row.taskNo + '"发起复检？').then(() => createRecheckTask(row.taskId)).then(() => { proxy.$modal.msgSuccess('复检任务已创建'); getList() }).catch(() => {}) }
function handleVoid(row) {
  proxy.$modal.confirm('确认将检验任务"' + row.taskNo + '"作废？作废后任务将不可继续检验。').then(() => {
    return voidTask(row.taskId, { reason: '手动作废', voidType: 'manual' })
  }).then(() => {
    proxy.$modal.msgSuccess('作废成功')
    getList()
  }).catch(() => {})
}
function submitForm() {
  proxy.$refs['taskRef'].validate(valid => {
    if (valid) {
      // 提交前确保量检具名称和编号与ID同步
      syncGaugeInfo()
      if (form.value.taskId != undefined) {
        updateTask(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addTask(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function submitInspectResult() {
  const list = inspectForm.value.itemList || []
  for (let i = 0; i < list.length; i++) {
    const item = list[i]
    if (!item.stdId) { proxy.$modal.msgWarning('第' + (i + 1) + '行：请选择检验项目'); return }
    if (!item.itemResult) { proxy.$modal.msgWarning('第' + (i + 1) + '行：请完成单项判定'); return }
    if (item.itemResult === '2') {
      if (!item.defectCode) { proxy.$modal.msgWarning('第' + (i + 1) + '行：不合格时缺陷代码必填'); return }
      if (!item.defectName) { proxy.$modal.msgWarning('第' + (i + 1) + '行：不合格时缺陷名称必填'); return }
      if (!item.defectLevel) { proxy.$modal.msgWarning('第' + (i + 1) + '行：不合格时缺陷等级必填'); return }
      if (!item.defectQty || item.defectQty <= 0) { proxy.$modal.msgWarning('第' + (i + 1) + '行：不合格时缺陷数量必须大于0'); return }
    } else {
      if (item.defectQty == null || item.defectQty === undefined) item.defectQty = 0
    }
  }
  saveInspectResult(inspectForm.value).then(() => { proxy.$modal.msgSuccess('检验结果已提交'); inspectOpen.value = false; getList() })
}
function handleSaveDraft() {
  saveInspectDraft(inspectForm.value).then(() => {
    proxy.$modal.msgSuccess('已临时保存，可稍后继续录入')
    getList()
  }).catch(() => {})
}
function handleDelete(row) { const taskIds = row.taskId || ids.value; proxy.$modal.confirm('确认删除编号为"' + taskIds + '"的数据？').then(() => delTask(taskIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
/** 同步量检具信息：根据gaugeId从选项列表中补全gaugeName和gaugeNo */
function syncGaugeInfo() {
  if (form.value.gaugeId) {
    const gauge = gaugeOptions.value.find(g => g.gaugeId == form.value.gaugeId)
    if (gauge) { form.value.gaugeName = gauge.gaugeName; form.value.gaugeNo = gauge.gaugeNo }
  }
}

/** 详情页同步量检具信息：根据gaugeId从选项列表中补全gaugeName和gaugeNo */
function syncViewGaugeInfo() {
  if (viewData.value.gaugeId && (!viewData.value.gaugeName || !viewData.value.gaugeNo)) {
    const gauge = gaugeOptions.value.find(g => g.gaugeId == viewData.value.gaugeId)
    if (gauge) { viewData.value.gaugeName = gauge.gaugeName; viewData.value.gaugeNo = gauge.gaugeNo }
  }
}

function handleExport() { proxy.download('qms/task/export', { ...queryParams.value }, `task_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

loadGaugeOptions()
loadDefectOptions()
loadStdOptions()
loadSupplierOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-task-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --violet-500:#8b5cf6; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
.qms-task-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.qms-task-page .filter-card { padding:14px 20px 16px; }
.qms-task-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.qms-task-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.qms-task-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.qms-task-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.qms-task-page .filter-card .adv-link:hover { color:var(--brand-600); }
.qms-task-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.qms-task-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.qms-task-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.qms-task-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.qms-task-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.qms-task-page .filter-card .filter-buttons { display:flex; gap:8px; }
.qms-task-page .field { display:flex; flex-direction:column; gap:6px; }
.qms-task-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.qms-task-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-task-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.qms-task-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.qms-task-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.qms-task-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.qms-task-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.qms-task-page .field .control :deep(.el-select) { width:100%; }
.qms-task-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.qms-task-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.qms-task-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.qms-task-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.qms-task-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.qms-task-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.qms-task-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.qms-task-page .btn-soft .el-icon { font-size:14px; }
.qms-task-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.qms-task-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.qms-task-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.qms-task-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.qms-task-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.qms-task-page .table-wrap { overflow-x:auto; }
.qms-task-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.qms-task-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.qms-task-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.qms-task-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.qms-task-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.qms-task-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.qms-task-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.qms-task-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.qms-task-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.qms-task-page .badge.green .dot { background:var(--green-500); }
.qms-task-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.qms-task-page .badge.gray .dot { background:var(--ink-400); }
.qms-task-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.qms-task-page .badge.red .dot { background:var(--red-500); }
.qms-task-page .badge.orange { background:#fffbeb; color:#b45309; border-color:#fde68a; }
.qms-task-page .badge.orange .dot { background:#f59e0b; }
.qms-task-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.qms-task-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-task-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-task-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.qms-task-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.qms-task-page .rd-form-tip:hover { color: #909399; }
.qms-task-page .clear-icon { color: #c0c4cc; cursor: pointer; font-size: 14px; }
.qms-task-page .clear-icon:hover { color: #909399; }

/* ===== Status Tabs ===== */
.qms-task-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.qms-task-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.qms-task-page .tabs-track::-webkit-scrollbar { display:none; }
.qms-task-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.qms-task-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.qms-task-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.qms-task-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.qms-task-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.qms-task-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.qms-task-page .status-tab.is-active .dot { background:var(--brand-500); }
/* 待检验 - gray */
.qms-task-page .status-tab.tab-pending .dot { background:var(--ink-400); }
.qms-task-page .status-tab.tab-pending .count { background:var(--ink-100); color:var(--ink-500); }
.qms-task-page .status-tab.is-active.tab-pending .count { background:var(--ink-400); color:#fff; }
/* 检验中 - orange */
.qms-task-page .status-tab.tab-inspecting .dot { background:#f59e0b; }
.qms-task-page .status-tab.tab-inspecting .count { background:#fffbeb; color:#b45309; }
.qms-task-page .status-tab.is-active.tab-inspecting .count { background:#f59e0b; color:#fff; }
/* 已完成 - green */
.qms-task-page .status-tab.tab-done .dot { background:var(--green-500); }
.qms-task-page .status-tab.tab-done .count { background:var(--green-50); color:var(--green-700); }
.qms-task-page .status-tab.is-active.tab-done .count { background:var(--green-500); color:#fff; }
/* 已作废 - red */
.qms-task-page .status-tab.tab-void .dot { background:var(--red-500); }
.qms-task-page .status-tab.tab-void .count { background:var(--red-50); color:var(--red-700); }
.qms-task-page .status-tab.is-active.tab-void .count { background:var(--red-500); color:#fff; }

/* ===== Tip Pill ===== */
.qms-task-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid #fde68a; background:#fffbeb; color:#b45309; cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; }
.qms-task-page .tip-pill:hover { background:#fef3c7; border-color:#f59e0b; }

/* ===== 录入页面工具栏 ===== */
.qms-task-page .inspect-toolbar { display:flex; align-items:center; gap:8px; margin-bottom:10px; }

/* ===== 录入页面必填标记 ===== */
.qms-task-page .col-required { color:#f56c6c; margin-left:2px; font-size:14px; }
.qms-task-page .col-required::after { content:'*'; }

/* ===== 录入页面分页 ===== */
.qms-task-page .inspect-pagination { display:flex; align-items:center; justify-content:space-between; margin-top:12px; padding:8px 0; }
.qms-task-page .inspect-pagination .page-info { font-size:13px; color:var(--ink-500); }

/* ===== 录入弹窗底部布局 ===== */
.qms-task-page .inspect-footer { display:flex; align-items:center; justify-content:center; gap:12px; }

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

@media (max-width:720px) { .qms-task-page .status-tabs { padding:6px 8px; } }
</style>
