<template>
  <div class="app-container">
    <!-- ===== 搜索表单（常用 + 高级条件统一管理） ===== -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <!-- ===== 常用条件（始终显示） ===== -->
      <el-form-item label="供应商" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="评估周期" prop="evalPeriod">
        <el-input v-model="queryParams.evalPeriod" placeholder="如2026-Q3" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="d in pms_eval_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <!-- ===== 高级条件（点击「更多」展开，v-show 控制） ===== -->
      <el-form-item label="评估等级" prop="evalLevel" v-show="showAdvanced">
        <el-select v-model="queryParams.evalLevel" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="d in pms_eval_level" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="评估人" prop="evaluatorName" v-show="showAdvanced">
        <el-input v-model="queryParams.evaluatorName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="评估日期" v-show="showAdvanced">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item label="创建时间" v-show="showAdvanced">
        <el-date-picker v-model="createTimeRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <!-- ===== 按钮区 ===== -->
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="info" plain :icon="showAdvanced ? 'ArrowUp' : 'ArrowDown'" @click="toggleAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:eval:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:eval:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:eval:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:eval:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="评估周期" prop="evalPeriod" :width="colWidth('evalPeriod', 120)" resizable align="center" />
      <el-table-column label="评估日期" prop="evalDate" :width="colWidth('evalDate', 110)" resizable align="center" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="交货评分" prop="deliveryScore" :width="colWidth('deliveryScore', 90)" resizable align="center" />
      <el-table-column label="质量评分" prop="qualityScore" :width="colWidth('qualityScore', 90)" resizable align="center" />
      <el-table-column label="价格评分" prop="priceScore" :width="colWidth('priceScore', 90)" resizable align="center" />
      <el-table-column label="服务评分" prop="serviceScore" :width="colWidth('serviceScore', 90)" resizable align="center" />
      <el-table-column label="综合评分" prop="totalScore" :width="colWidth('totalScore', 100)" resizable align="center" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="评估等级" prop="evalLevel" :width="colWidth('evalLevel', 90)" resizable align="center"><template #default="scope"><dict-tag :options="pms_eval_level" :value="scope.row.evalLevel" /></template></el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 90)" resizable align="center" sortable="custom" :sort-orders="['descending', 'ascending']">
        <template #default="scope">
          <dict-tag :options="pms_eval_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="评估人" prop="evaluatorName" :width="colWidth('evaluatorName', 100)" resizable />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" :sort-orders="['descending', 'ascending']" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '2'" v-hasPermi="['pms:eval:edit']">修改</el-button>
          <el-button link type="primary" icon="Check" @click="handleAudit(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['pms:eval:audit']">审批</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '2'" v-hasPermi="['pms:eval:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- ===== 新增/修改弹窗 ===== -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="evalRef" :model="form" :rules="rules" label-width="110px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="评估周期" prop="evalPeriod"><el-input v-model="form.evalPeriod" placeholder="如2026-Q3" /></el-form-item></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="评估日期" prop="evalDate"><el-date-picker v-model="form.evalDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="评估人" prop="evaluatorName">
                <el-input v-model="form.evaluatorName" readonly placeholder="请选择评估人" style="width: 100%" @click="openUserPicker">
                  <template #append><el-button icon="Search" @click="openUserPicker"></el-button></template>
                  <template #suffix><el-icon v-if="form.evaluatorName" class="clear-icon" @click.stop="clearEvaluator"><CircleClose /></el-icon></template>
                </el-input>
              </el-form-item></el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="状态"><dict-tag :options="pms_eval_status" :value="form.status" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('score')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.score }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.score">
            <el-alert type="info" :closable="false" show-icon class="score-rules-alert">
              <template #title>
                <span class="score-rules-title">打分规则说明</span>
              </template>
              <div class="score-rules-content">
                <p>1. 每项评分范围为 <b>0~100</b> 分，权重为百分比（%），四项权重之和建议为 <b>100%</b>。</p>
                <p>2. 单项得分 = 评分 × 权重 ÷ 100，系统自动计算。</p>
                <p>3. 综合评分 = 各单项得分之和，系统自动计算。</p>
                <p>4. 评估等级自动评定：<b>≥90</b> 为优秀(A)，<b>≥80</b> 为良好(B)，<b>≥60</b> 为合格(C)，<b>&lt;60</b> 为不合格(D)。</p>
              </div>
            </el-alert>
            <!-- 交货 -->
            <el-row :gutter="20" class="score-row">
              <el-col :span="8"><el-form-item label="交货评分" prop="deliveryScore"><el-input-number v-model="form.deliveryScore" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="交货权重(%)"><el-input-number v-model="form.deliveryWeight" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="交货得分"><el-input v-model="form.deliveryItemScore" disabled><template #append>分</template></el-input></el-form-item></el-col>
            </el-row>
            <!-- 质量 -->
            <el-row :gutter="20" class="score-row">
              <el-col :span="8"><el-form-item label="质量评分" prop="qualityScore"><el-input-number v-model="form.qualityScore" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="质量权重(%)"><el-input-number v-model="form.qualityWeight" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="质量得分"><el-input v-model="form.qualityItemScore" disabled><template #append>分</template></el-input></el-form-item></el-col>
            </el-row>
            <!-- 价格 -->
            <el-row :gutter="20" class="score-row">
              <el-col :span="8"><el-form-item label="价格评分" prop="priceScore"><el-input-number v-model="form.priceScore" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="价格权重(%)"><el-input-number v-model="form.priceWeight" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="价格得分"><el-input v-model="form.priceItemScore" disabled><template #append>分</template></el-input></el-form-item></el-col>
            </el-row>
            <!-- 服务 -->
            <el-row :gutter="20" class="score-row">
              <el-col :span="8"><el-form-item label="服务评分" prop="serviceScore"><el-input-number v-model="form.serviceScore" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="服务权重(%)"><el-input-number v-model="form.serviceWeight" :precision="2" :min="0" :max="100" style="width: 100%" @change="calcTotal" /></el-form-item></el-col>
              <el-col :span="8"><el-form-item label="服务得分"><el-input v-model="form.serviceItemScore" disabled><template #append>分</template></el-input></el-form-item></el-col>
            </el-row>
            <el-divider />
            <el-row :gutter="20" class="score-summary-row">
              <el-col :span="12">
                <div class="score-summary-item">
                  <span class="score-summary-label">综合评分</span>
                  <span class="score-summary-value score-summary-total">{{ form.totalScore ?? '-' }}<small>分</small></span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="score-summary-item">
                  <span class="score-summary-label">评估等级</span>
                  <span class="score-summary-value score-summary-level" :class="levelClass(form.evalLevel)">{{ levelText }}<small>({{ form.evalLevel || '-' }})</small></span>
                </div>
              </el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('remark')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>备注</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.remark }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.remark"><el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item></div>
        </section>
        <section class="rd-card" v-if="form.auditLogList && form.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
            <el-alert v-if="form.status === '2'" type="warning" :closable="false" show-icon class="reject-alert">
              <template #title>该评估已被驳回，请根据审核意见修改后重新提交</template>
            </el-alert>
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in form.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></div><span class="rd-detail-header-title">评估详情</span><div class="rd-detail-header-sub" v-if="viewData.supplierName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ viewData.supplierName }} - {{ viewData.evalPeriod }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估周期</span><div class="rd-value">{{ viewData.evalPeriod || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估日期</span><div class="rd-value">{{ viewData.evalDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估人</span><div class="rd-value">{{ viewData.evaluatorName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_eval_status" :value="viewData.status" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">交货评分</span><div class="rd-value">{{ viewData.deliveryScore ?? '-' }}（权重 {{ viewData.deliveryWeight ?? 0 }}%）</div></div>
            <div class="rd-item"><span class="rd-label">交货得分</span><div class="rd-value">{{ viewData.deliveryItemScore ?? '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">质量评分</span><div class="rd-value">{{ viewData.qualityScore ?? '-' }}（权重 {{ viewData.qualityWeight ?? 0 }}%）</div></div>
            <div class="rd-item"><span class="rd-label">质量得分</span><div class="rd-value">{{ viewData.qualityItemScore ?? '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">价格评分</span><div class="rd-value">{{ viewData.priceScore ?? '-' }}（权重 {{ viewData.priceWeight ?? 0 }}%）</div></div>
            <div class="rd-item"><span class="rd-label">价格得分</span><div class="rd-value">{{ viewData.priceItemScore ?? '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">服务评分</span><div class="rd-value">{{ viewData.serviceScore ?? '-' }}（权重 {{ viewData.serviceWeight ?? 0 }}%）</div></div>
            <div class="rd-item"><span class="rd-label">服务得分</span><div class="rd-value">{{ viewData.serviceItemScore ?? '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">综合评分</span><div class="rd-value rd-amount">{{ viewData.totalScore ?? '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估等级</span><div class="rd-value"><dict-tag :options="pms_eval_level" :value="viewData.evalLevel" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.auditLogList && viewData.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in viewData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
            <div class="rd-empty" v-if="!viewData.auditLogList || viewData.auditLogList.length === 0">
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
              <p class="rd-empty-text">暂无审核记录</p>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <!-- ===== 审批弹窗 ===== -->
    <el-dialog v-model="auditOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></div><span class="rd-detail-header-title">供应商评估审批</span><div class="rd-detail-header-sub" v-if="auditData.supplierName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ auditData.supplierName }} - {{ auditData.evalPeriod }}</span></div></div></template>
      <div class="rd-page" v-loading="auditLoading">
        <!-- 评估摘要 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>评估摘要</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ auditData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估周期</span><div class="rd-value">{{ auditData.evalPeriod || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估日期</span><div class="rd-value">{{ auditData.evalDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">评估人</span><div class="rd-value">{{ auditData.evaluatorName || '-' }}</div></div>
          </div></div>
        </section>
        <!-- 评分明细 -->
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg></span>评分明细</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="audit-score-table">
              <div class="audit-score-row audit-score-head">
                <div class="audit-score-cell">评分项</div>
                <div class="audit-score-cell">评分</div>
                <div class="audit-score-cell">权重</div>
                <div class="audit-score-cell">得分</div>
              </div>
              <div class="audit-score-row">
                <div class="audit-score-cell">交货</div>
                <div class="audit-score-cell">{{ auditData.deliveryScore ?? '-' }}</div>
                <div class="audit-score-cell">{{ auditData.deliveryWeight ?? 0 }}%</div>
                <div class="audit-score-cell">{{ auditData.deliveryItemScore ?? '-' }}</div>
              </div>
              <div class="audit-score-row">
                <div class="audit-score-cell">质量</div>
                <div class="audit-score-cell">{{ auditData.qualityScore ?? '-' }}</div>
                <div class="audit-score-cell">{{ auditData.qualityWeight ?? 0 }}%</div>
                <div class="audit-score-cell">{{ auditData.qualityItemScore ?? '-' }}</div>
              </div>
              <div class="audit-score-row">
                <div class="audit-score-cell">价格</div>
                <div class="audit-score-cell">{{ auditData.priceScore ?? '-' }}</div>
                <div class="audit-score-cell">{{ auditData.priceWeight ?? 0 }}%</div>
                <div class="audit-score-cell">{{ auditData.priceItemScore ?? '-' }}</div>
              </div>
              <div class="audit-score-row">
                <div class="audit-score-cell">服务</div>
                <div class="audit-score-cell">{{ auditData.serviceScore ?? '-' }}</div>
                <div class="audit-score-cell">{{ auditData.serviceWeight ?? 0 }}%</div>
                <div class="audit-score-cell">{{ auditData.serviceItemScore ?? '-' }}</div>
              </div>
            </div>
            <el-row :gutter="20" class="score-summary-row">
              <el-col :span="12">
                <div class="score-summary-item">
                  <span class="score-summary-label">综合评分</span>
                  <span class="score-summary-value score-summary-total">{{ auditData.totalScore ?? '-' }}<small>分</small></span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="score-summary-item">
                  <span class="score-summary-label">评估等级</span>
                  <span class="score-summary-value score-summary-level" :class="levelClass(auditData.evalLevel)">{{ levelTextFor(auditData.evalLevel) }}<small>({{ auditData.evalLevel || '-' }})</small></span>
                </div>
              </el-col>
            </el-row>
          </div>
        </section>
        <!-- 历史审核记录 -->
        <section class="rd-card" v-if="auditData.auditLogList && auditData.auditLogList.length">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg></span>审核记录</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="log in auditData.auditLogList" :key="log.logId">
                <div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': log.auditAction === '1', 'rd-timeline-dot--error': log.auditAction === '2' }"></div>
                <div class="rd-timeline-content">
                  <div class="rd-timeline-header">
                    <span class="rd-timeline-title">
                      <el-tag v-if="log.auditAction === '1'" type="success" size="small" effect="light" round>审核通过</el-tag>
                      <el-tag v-else-if="log.auditAction === '2'" type="danger" size="small" effect="light" round>审核驳回</el-tag>
                    </span>
                    <span class="rd-timeline-time">{{ log.auditTime }}</span>
                  </div>
                  <div class="rd-timeline-body">
                    <div class="rd-item"><span class="rd-label">审核人</span><div class="rd-value">{{ log.auditBy || '-' }}</div></div>
                  </div>
                  <div class="rd-timeline-comment" v-if="log.auditRemark">
                    <strong>审核意见：</strong>{{ log.auditRemark }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('audit')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批意见</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.audit }" type="button"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.audit">
            <el-form ref="auditRef" :model="auditForm" :rules="auditRules" label-width="100px">
              <el-form-item label="审批意见" prop="auditRemark">
                <el-input v-model="auditForm.auditRemark" type="textarea" :rows="4" placeholder="请输入审批意见" show-word-limit maxlength="500" />
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

    <!-- ===== 人员选择弹框 ===== -->
    <user-picker ref="userPickerRef" title="选择评估人" @confirm="onUserPickerConfirm" />

  </div>
</template>

<script setup name="PmsEval">
import { listEval, getEval, addEval, updateEval, delEval, auditEval } from '@/api/pms/eval'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import useUserStore from '@/store/modules/user'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const { pms_eval_level, pms_eval_status } = proxy.useDict('pms_eval_level', 'pms_eval_status')
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_eval_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'score', 'remark', 'audit'])

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const auditOpen = ref(false)
const auditLoading = ref(false)
const viewData = ref({})
const auditData = ref({})
const auditForm = reactive({
  evalId: undefined, auditRemark: undefined
})
const auditRules = {
  auditRemark: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const createTimeRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const supplierOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    supplierName: undefined,
    evalPeriod: undefined,
    status: undefined,
    evalLevel: undefined,
    evaluatorName: undefined,
    params: {}
  },
  rules: {
    supplierId: [{ required: true, message: '供应商不能为空', trigger: 'change' }],
    evalDate: [{ required: true, message: '评估日期不能为空', trigger: 'change' }],
    evaluatorName: [{ required: true, message: '评估人不能为空', trigger: 'change' }],
    deliveryScore: [{ required: true, message: '交货评分不能为空', trigger: 'blur' }],
    qualityScore: [{ required: true, message: '质量评分不能为空', trigger: 'blur' }],
    priceScore: [{ required: true, message: '价格评分不能为空', trigger: 'blur' }],
    serviceScore: [{ required: true, message: '服务评分不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 等级文本 */
const levelText = computed(() => {
  const levelMap = { A: '优秀', B: '良好', C: '合格', D: '不合格' }
  return levelMap[form.value.evalLevel] || ''
})

/** 等级样式class */
function levelClass(level) {
  return level ? `level-${level}` : ''
}
/** 等级文本（支持任意数据源） */
function levelTextFor(level) {
  const levelMap = { A: '优秀', B: '良好', C: '合格', D: '不合格' }
  return levelMap[level] || ''
}

/** 计算单项得分、综合评分、评估等级 */
function calcTotal() {
  const d = form.value.deliveryScore || 0
  const dw = form.value.deliveryWeight || 0
  const q = form.value.qualityScore || 0
  const qw = form.value.qualityWeight || 0
  const p = form.value.priceScore || 0
  const pw = form.value.priceWeight || 0
  const s = form.value.serviceScore || 0
  const sw = form.value.serviceWeight || 0

  // 单项得分 = 评分 × 权重 / 100
  form.value.deliveryItemScore = parseFloat((d * dw / 100).toFixed(2))
  form.value.qualityItemScore = parseFloat((q * qw / 100).toFixed(2))
  form.value.priceItemScore = parseFloat((p * pw / 100).toFixed(2))
  form.value.serviceItemScore = parseFloat((s * sw / 100).toFixed(2))

  // 综合评分 = 各单项得分之和
  const total = form.value.deliveryItemScore + form.value.qualityItemScore +
    form.value.priceItemScore + form.value.serviceItemScore
  form.value.totalScore = parseFloat(total.toFixed(2))

  // 自动评定等级
  if (total >= 90) form.value.evalLevel = 'A'
  else if (total >= 80) form.value.evalLevel = 'B'
  else if (total >= 60) form.value.evalLevel = 'C'
  else form.value.evalLevel = 'D'
}

/** 切换展开/收起高级查询 */
function toggleAdvanced() {
  showAdvanced.value = !showAdvanced.value
}

function getList() { loading.value = true; listEval(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() {
  queryParams.value.pageNum = 1
  proxy.addDateRange(queryParams.value, dateRange.value, 'EvalDate')
  proxy.addDateRange(queryParams.value, createTimeRange.value, 'CreateTime')
  showAdvanced.value = false
  getList()
}
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.params = {}
  dateRange.value = []
  createTimeRange.value = []
  if (tableRef.value) tableRef.value.clearSort()
  handleQuery()
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
function handleSelectionChange(selection) { ids.value = selection.map(i => i.evalId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() {
  form.value = {
    evalId: undefined,
    supplierId: undefined,
    supplierName: undefined,
    evalPeriod: undefined,
    evalDate: proxy.parseTime(new Date(), '{y}-{m}-{d}'),
    deliveryScore: 0,
    deliveryWeight: 30,
    deliveryItemScore: 0,
    qualityScore: 0,
    qualityWeight: 30,
    qualityItemScore: 0,
    priceScore: 0,
    priceWeight: 20,
    priceItemScore: 0,
    serviceScore: 0,
    serviceWeight: 20,
    serviceItemScore: 0,
    totalScore: 0,
    evalLevel: undefined,
    status: '0',
    evaluatorId: userStore.id,
    evaluatorName: userStore.nickName,
    remark: undefined
  }
  proxy.resetForm('evalRef')
}
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function handleAdd() { reset(); open.value = true; title.value = '添加供应商评估' }
function handleUpdate(row) {
  reset()
  getEval(row.evalId || ids.value[0]).then(res => {
    form.value = res.data
    // 编辑时不再覆盖评估人
    open.value = true
    title.value = '修改供应商评估'
  })
}
function handleView(row) { getEval(row.evalId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function handleAudit(row) {
  auditForm.evalId = row.evalId
  auditForm.auditRemark = undefined
  auditData.value = {}
  auditOpen.value = true
  auditLoading.value = true
  getEval(row.evalId).then(res => {
    auditData.value = res.data
  }).finally(() => {
    auditLoading.value = false
  })
}
function submitAudit(passed) {
  proxy.$refs['auditRef'].validate(valid => {
    if (valid) {
      const status = passed ? '1' : '2'
      const actionText = passed ? '通过' : '驳回'
      proxy.$modal.confirm(`确认${actionText}该供应商评估记录？`).then(() => {
        return auditEval(auditForm.evalId, status, auditForm.auditRemark)
      }).then(() => {
        proxy.$modal.msgSuccess('审批成功')
        auditOpen.value = false
        getList()
      }).catch(() => {})
    }
  })
}
function submitForm() {
  proxy.$refs['evalRef'].validate(valid => {
    if (valid) {
      // 校验权重合计必须等于100%
      const weightSum = parseFloat(((form.value.deliveryWeight || 0) + (form.value.qualityWeight || 0) +
        (form.value.priceWeight || 0) + (form.value.serviceWeight || 0)).toFixed(2))
      if (weightSum !== 100) {
        proxy.$modal.msgError(`权重合计必须等于100%，当前合计为 ${weightSum}%`)
        return
      }
      // 提交前重新计算确保数据一致
      calcTotal()
      if (form.value.evalId != undefined) {
        updateEval(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addEval(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function handleDelete(row) {
  const evalIds = row.evalId || ids.value
  proxy.$modal.confirm('确认删除编号为"' + evalIds + '"的数据？').then(() => delEval(evalIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() { proxy.download('pms/eval/export', { ...queryParams.value }, `eval_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

/** 打开评估人选择弹窗 */
function openUserPicker() {
  proxy.$refs.userPickerRef.open(form.value.evaluatorId)
}
/** 评估人选择确认回调 */
function onUserPickerConfirm(user) {
  form.value.evaluatorId = user.userId
  form.value.evaluatorName = user.nickName
}
/** 清除评估人 */
function clearEvaluator() {
  form.value.evaluatorId = undefined
  form.value.evaluatorName = undefined
}

loadSupplierOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.clear-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
}
.clear-icon:hover {
  color: #909399;
}
.reject-history-card {
  margin-bottom: 16px;
  border: 1px solid #fde2e2;
  background: #fef0f0;
}
.reject-history-card .rd-card-header {
  border-bottom-color: #fde2e2;
}
.reject-alert {
  margin-bottom: 16px;
}
.reject-alert-content {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
}
.score-rules-alert {
  margin-bottom: 16px;
}
.score-rules-title {
  font-weight: 600;
}
.score-rules-content p {
  margin: 4px 0;
  font-size: 13px;
  line-height: 1.6;
}
.score-row {
  margin-bottom: 4px;
}
.score-summary-row {
  margin-top: 8px;
}
.score-summary-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  border: 1px solid #d6e4ff;
}
.score-summary-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}
.score-summary-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}
.score-summary-value small {
  font-size: 14px;
  font-weight: 400;
  margin-left: 4px;
  color: #909399;
}
.score-summary-total {
  color: #409eff;
}
.score-summary-level.level-A {
  color: #67c23a;
}
.score-summary-level.level-B {
  color: #409eff;
}
.score-summary-level.level-C {
  color: #e6a23c;
}
.score-summary-level.level-D {
  color: #f56c6c;
}
/* ===== 审批弹窗样式 ===== */
.audit-score-table {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}
.audit-score-row {
  display: flex;
}
.audit-score-row + .audit-score-row {
  border-top: 1px solid #ebeef5;
}
.audit-score-head {
  background: #f5f7fa;
}
.audit-score-cell {
  flex: 1;
  padding: 10px 12px;
  font-size: 13px;
  text-align: center;
  color: #606266;
}
.audit-score-head .audit-score-cell {
  font-weight: 600;
  color: #303133;
}
</style>
