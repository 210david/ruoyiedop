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
            <template #default="scope"><dict-tag :options="qms_insp_type" :value="scope.row.taskType" /></template>
          </el-table-column>
          <el-table-column label="来源单号" prop="sourceNo" :width="colWidth('sourceNo', 150)" resizable show-overflow-tooltip />
          <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 140)" resizable show-overflow-tooltip />
          <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip />
          <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable show-overflow-tooltip />
          <el-table-column label="送检数量" prop="inspectQty" :width="colWidth('inspectQty', 100)" resizable align="right" />
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11H1l8-8 8 8h-8v8z" transform="rotate(90 12 12)"/><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="taskRef" :model="form" :rules="rules" label-width="110px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="6" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg></span>基本信息</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="任务编号" prop="taskNo"><el-input v-model="form.taskNo" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item prop="taskType"><template #label><span>检验类型</span><el-tooltip content="检验类型包括IQC（来料检验）、IPQC（过程检验）、FQC（成品检验）、OQC（出货检验）等" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.taskType" style="width: 100%"><el-option v-for="d in qms_insp_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="来源类型" prop="sourceType"><el-input v-model="form.sourceType" placeholder="如：采购入库/生产工单" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="来源单号" prop="sourceNo"><el-input v-model="form.sourceNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料与批次</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="供应商" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>抽样与检验</div></div>
            <div class="rd-card-body">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="送检数量" prop="inspectQty"><el-input-number v-model="form.inspectQty" :min="0" :precision="0" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item prop="aqlLevel"><template #label><span>AQL等级</span><el-tooltip content="AQL（Acceptable Quality Limit）即可接受质量限，表示检验批中允许的最大不合格品率。常用值为2.5（一般检验）、1.5（加严检验）、4.0（放宽检验）" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.aqlLevel" placeholder="如：2.5" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item prop="inspectLevel"><template #label><span>检验严格度</span><el-tooltip content="抽样检验的严格度等级，分为：正常检验、加严检验、放宽检验。根据产品质量历史动态调整检验严格度" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-select v-model="form.inspectLevel" style="width: 100%"><el-option v-for="d in qms_inspect_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="量检具" prop="gaugeId"><el-select v-model="form.gaugeId" filterable clearable placeholder="请选择" style="width: 100%"><el-option v-for="g in gaugeOptions" :key="g.gaugeId" :label="g.gaugeName" :value="g.gaugeId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="检验员" prop="inspectorName"><el-input v-model="form.inspectorName" placeholder="请输入" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>其他信息</div></div>
            <div class="rd-card-body">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- View Dialog -->
    <el-dialog v-model="viewOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg></div>
          <span class="rd-detail-header-title">检验任务详情</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title">基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">任务编号</span><div class="rd-value">{{ viewData.taskNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验类型</span><div class="rd-value"><dict-tag :options="qms_insp_type" :value="viewData.taskType" /></div></div>
            <div class="rd-item"><span class="rd-label">来源类型</span><div class="rd-value">{{ viewData.sourceType || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">来源单号</span><div class="rd-value">{{ viewData.sourceNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ viewData.batchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">送检数量</span><div class="rd-value">{{ viewData.inspectQty != null ? viewData.inspectQty : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">量检具</span><div class="rd-value">{{ viewData.gaugeName || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title">AQL抽样信息</div><el-tooltip content="AQL（Acceptable Quality Limit）即可接受质量限，是抽样检验标准中的核心参数，用于判定检验批是否合格" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">AQL等级</span><div class="rd-value">{{ viewData.aqlLevel || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验严格度</span><div class="rd-value"><dict-tag :options="qms_inspect_level" :value="viewData.inspectLevel" /></div></div>
            <div class="rd-item"><span class="rd-label">样本量(n)</span><div class="rd-value">{{ viewData.sampleSize != null ? viewData.sampleSize : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">接收数(Ac)</span><div class="rd-value">{{ viewData.acVal != null ? viewData.acVal : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">拒收数(Re)</span><div class="rd-value">{{ viewData.reVal != null ? viewData.reVal : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title">判定结果</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">任务状态</span><div class="rd-value">{{ taskStatusLabel(viewData.taskStatus) }}</div></div>
            <div class="rd-item"><span class="rd-label">检验结果</span><div class="rd-value">{{ viewData.inspectResult ? inspResultLabel(viewData.inspectResult) : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">最高缺陷等级</span><div class="rd-value"><dict-tag :options="qms_defect_level" :value="viewData.defectLevel" /></div></div>
            <div class="rd-item"><span class="rd-label">检验员</span><div class="rd-value">{{ viewData.inspectorName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">检验时间</span><div class="rd-value">{{ viewData.inspectTime || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">是否复检</span><div class="rd-value">{{ viewData.isRecheck === '1' ? '是' : '否' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.itemList && viewData.itemList.length > 0">
          <div class="rd-card-header"><div class="rd-card-title">检验明细</div></div>
          <div class="rd-card-body" style="display:block; padding:0">
            <el-table :data="viewData.itemList" border class="app-table" style="width: 100%">
              <el-table-column label="序号" prop="sampleSeq" width="60" align="center" />
              <el-table-column label="检验项目" prop="stdName" width="200" show-overflow-tooltip />
              <el-table-column label="实测值" prop="measuredVal" width="120" align="center" />
              <el-table-column label="单项判定" prop="itemResult" width="100" align="center">
                <template #default="scope"><span class="badge" :class="scope.row.itemResult === '1' ? 'green' : 'red'"><span class="dot"></span>{{ scope.row.itemResult === '1' ? '合格' : '不合格' }}</span></template>
              </el-table-column>
              <el-table-column label="缺陷代码" prop="defectCode" width="100" align="center" />
              <el-table-column label="缺陷名称" prop="defectName" width="160" show-overflow-tooltip />
              <el-table-column label="缺陷等级" prop="defectLevel" width="100" align="center">
                <template #header><span>缺陷等级</span><el-tooltip content="不合格品缺陷的严重程度等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                <template #default="scope"><dict-tag :options="qms_defect_level" :value="scope.row.defectLevel" /></template>
              </el-table-column>
            </el-table>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid"><div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div></div></div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- Inspect Result Dialog -->
    <el-dialog v-model="inspectOpen" width="960px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div>
          <span class="rd-detail-header-title">检验结果录入</span>
        </div>
      </template>
      <div style="margin-bottom: 12px; padding: 12px 16px; background: var(--el-fill-color-light); border-radius: 6px; font-size: 14px; color: var(--el-text-color-secondary);">
        任务编号：<b>{{ inspectForm.taskNo }}</b> &nbsp;|&nbsp; 物料：<b>{{ inspectForm.materialName }}</b> &nbsp;|&nbsp; 批次：<b>{{ inspectForm.batchNo }}</b> &nbsp;|&nbsp; 样本量(n)：<b>{{ inspectForm.sampleSize }}</b> &nbsp;|&nbsp; Ac：<b>{{ inspectForm.acVal }}</b> &nbsp;|&nbsp; Re：<b>{{ inspectForm.reVal }}</b>
      </div>
      <el-table :data="inspectForm.itemList" border class="app-table" style="width: 100%" max-height="400">
        <el-table-column label="序号" prop="sampleSeq" width="60" align="center" />
        <el-table-column label="检验项目" prop="stdName" width="180" show-overflow-tooltip />
        <el-table-column label="实测值" width="140" align="center">
          <template #default="scope"><el-input v-model="scope.row.measuredVal" placeholder="请输入" size="small" /></template>
        </el-table-column>
        <el-table-column label="单项判定" width="120" align="center">
          <template #default="scope">
            <el-select v-model="scope.row.itemResult" placeholder="选择" size="small"><el-option label="合格" value="1" /><el-option label="不合格" value="2" /></el-select>
          </template>
        </el-table-column>
        <el-table-column label="缺陷代码" width="120" align="center">
          <template #default="scope"><el-input v-model="scope.row.defectCode" placeholder="选填" size="small" :disabled="scope.row.itemResult !== '2'" /></template>
        </el-table-column>
        <el-table-column label="缺陷名称" width="160">
          <template #default="scope"><el-input v-model="scope.row.defectName" placeholder="选填" size="small" :disabled="scope.row.itemResult !== '2'" /></template>
        </el-table-column>
        <el-table-column label="缺陷等级" width="120" align="center">
          <template #header><span>缺陷等级</span><el-tooltip content="不合格品缺陷的严重程度等级，通常分为：致命缺陷、严重缺陷、一般缺陷、轻微缺陷" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
          <template #default="scope">
            <el-select v-model="scope.row.defectLevel" placeholder="选择" size="small" :disabled="scope.row.itemResult !== '2'">
              <el-option v-for="d in qms_defect_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <template #footer><el-button type="primary" @click="submitInspectResult">提交检验结果</el-button><el-button @click="inspectOpen = false">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTask">
import { listTask, getTask, addTask, updateTask, delTask, saveInspectResult, createRecheckTask } from '@/api/qms/task'
import { listGauge } from '@/api/qms/gauge'
import { useColumnResize } from '@/composables/useColumnResize'
import { QuestionFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_task_index')
const { qms_insp_type, qms_task_status, qms_insp_result, qms_inspect_level, qms_defect_level } = proxy.useDict('qms_insp_type', 'qms_task_status', 'qms_insp_result', 'qms_inspect_level', 'qms_defect_level')

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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, taskNo: undefined, taskType: undefined, taskStatus: undefined, inspectResult: undefined, materialCode: undefined, batchNo: undefined, params: {} },
  rules: {
    taskNo: [{ required: true, message: '任务编号不能为空', trigger: 'blur' }],
    taskType: [{ required: true, message: '检验类型不能为空', trigger: 'change' }],
    materialCode: [{ required: true, message: '物料编码不能为空', trigger: 'blur' }],
    inspectQty: [{ required: true, message: '送检数量不能为空', trigger: 'blur' }]
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

function taskStatusLabel(val) { const item = qms_task_status.value.find(d => d.value == val); return item ? item.label : '-' }
function taskBadgeClass(val) { if (val == '0') return 'gray'; if (val == '1') return 'orange'; if (val == '2') return 'green'; if (val == '3') return 'red'; return 'gray' }
function inspResultLabel(val) { const item = qms_insp_result.value.find(d => d.value == val); return item ? item.label : '-' }

function getList() { loading.value = true; listTask(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.taskNo = undefined; queryParams.value.taskType = undefined; queryParams.value.taskStatus = undefined; queryParams.value.inspectResult = undefined; queryParams.value.materialCode = undefined; queryParams.value.batchNo = undefined; queryParams.value.params = {}; handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.taskId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { taskId: undefined, taskNo: undefined, taskType: 'IQC', sourceType: undefined, sourceNo: undefined, materialId: undefined, materialCode: undefined, materialName: undefined, supplierId: undefined, supplierName: undefined, batchNo: undefined, inspectQty: undefined, aqlLevel: '2.5', inspectLevel: '1', gaugeId: undefined, inspectorId: undefined, inspectorName: undefined, status: '0', remark: undefined }; proxy.resetForm('taskRef') }
function loadGaugeOptions() { listGauge({ pageNum: 1, pageSize: 999, gaugeStatus: '0' }).then(res => { gaugeOptions.value = res.rows || [] }) }
function handleAdd() { reset(); open.value = true; title.value = '添加检验任务' }
function handleUpdate(row) { reset(); getTask(row.taskId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改检验任务' }) }
function handleView(row) { getTask(row.taskId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handlePrintReport(row) { proxy.$router.push({ path: '/qms/task/report', query: { taskId: row.taskId } }) }
function handleInspect(row) { getTask(row.taskId).then(res => { inspectForm.value = res.data; if (!inspectForm.value.itemList || inspectForm.value.itemList.length === 0) { inspectForm.value.itemList = [{ sampleSeq: 1, stdName: '通用检验', measuredVal: '', itemResult: '1', defectCode: '', defectName: '', defectLevel: '' }] }; inspectOpen.value = true }) }
function handleRecheck(row) { proxy.$modal.confirm('确认对任务"' + row.taskNo + '"发起复检？').then(() => createRecheckTask(row.taskId)).then(() => { proxy.$modal.msgSuccess('复检任务已创建'); getList() }).catch(() => {}) }
function submitForm() { proxy.$refs['taskRef'].validate(valid => { if (valid) { if (form.value.taskId != undefined) { updateTask(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addTask(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function submitInspectResult() { const hasEmpty = inspectForm.value.itemList.some(i => !i.itemResult); if (hasEmpty) { proxy.$modal.msgWarning('请完成所有检验项的判定'); return }; saveInspectResult(inspectForm.value).then(() => { proxy.$modal.msgSuccess('检验结果已提交'); inspectOpen.value = false; getList() }) }
function handleDelete(row) { const taskIds = row.taskId || ids.value; proxy.$modal.confirm('确认删除编号为"' + taskIds + '"的数据？').then(() => delTask(taskIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('qms/task/export', { ...queryParams.value }, `task_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

loadGaugeOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.qms-task-page { padding-top: 10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); color: var(--ink-900); }
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
.qms-task-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; }
@media (max-width:1100px) { .qms-task-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .qms-task-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.qms-task-page .rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.qms-task-page .rd-form-tip:hover { color: #909399; }
</style>
