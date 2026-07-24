<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="takeNo">
        <el-input v-model="queryParams.takeNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="盘点类型" prop="takeType">
        <el-select v-model="queryParams.takeType" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px">
          <el-option v-for="d in wms_take_status" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table ref="tableRef" border v-loading="loading" :data="list" @row-click="handleRowClick" highlight-current-row @header-dragend="onHeaderDragEnd">
      <el-table-column label="盘点单号" prop="takeNo" :width="colWidth('takeNo', 180)" resizable />
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="库区" prop="areaName" :width="colWidth('areaName', 120)" resizable />
      <el-table-column label="盘点类型" prop="takeType" :width="colWidth('takeType', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_take_type" :value="scope.row.takeType" /></template>
      </el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center">
        <template #default="scope"><dict-tag :options="wms_take_status" :value="scope.row.status" /></template>
      </el-table-column>
      <el-table-column label="计划日期" prop="planDate" :width="colWidth('planDate', 120)" resizable align="center" />
      <el-table-column label="开始时间" prop="startTime" :width="colWidth('startTime', 160)" resizable align="center" />
      <el-table-column label="结束时间" prop="endTime" :width="colWidth('endTime', 160)" resizable align="center" />
      <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click.stop="handleRowClick(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click.stop="handleRowClick(scope.row)" v-if="scope.row.status === '1'">录入</el-button>
          <el-button link type="primary" icon="Check" @click.stop="handleApprove(scope.row)" v-if="scope.row.status === '2'">审批</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 盘点作业详情面板 -->
    <el-dialog v-model="detailOpen" width="1320px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">{{ '盘点作业 - ' + (currentTake.takeNo || '') }}</span>
        </div>
      </template>
      <div>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">盘点单号</span><div class="rd-value">{{ currentTake.takeNo }}</div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ currentTake.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">库区</span><div class="rd-value">{{ currentTake.areaName || '全部' }}</div></div>
              <div class="rd-item"><span class="rd-label">盘点类型</span><div class="rd-value"><dict-tag :options="wms_take_type" :value="currentTake.takeType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_take_status" :value="currentTake.status" /></div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ currentTake.planDate }}</div></div>
              <div class="rd-item" v-if="currentTake.startTime"><span class="rd-label">开始时间</span><div class="rd-value">{{ currentTake.startTime }}</div></div>
              <div class="rd-item" v-if="currentTake.endTime"><span class="rd-label">结束时间</span><div class="rd-value">{{ currentTake.endTime }}</div></div>
            </div>
          </div>
        </section>
        <!-- 盘点明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dDetail')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>盘点明细</div>
            <div class="rd-card-actions" v-if="currentTake.status === '1'">
              <el-button size="small" type="primary" plain @click.stop="openBatch">批量录入</el-button>
              <el-button size="small" type="success" plain @click.stop="handleImport">批量导入</el-button>
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dDetail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dDetail">
            <el-table :data="detailPageList" border @header-dragend="onHeaderDragEnd">
              <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable />
              <el-table-column label="物料名称" prop="materialName" min-width="200" show-overflow-tooltip />
              <el-table-column label="库区/库位" width="180">
                <template #default="scope">{{ [scope.row.areaName, scope.row.locationName].filter(Boolean).join(' / ') || '-' }}</template>
              </el-table-column>
              <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 100)" resizable />
              <el-table-column label="账面数量" prop="bookQty" :width="colWidth('bookQty', 100)" resizable align="right" />
              <el-table-column label="实盘数量" prop="actualQty" :width="colWidth('actualQty', 100)" resizable align="right">
                <template #default="scope">
                  <span v-if="scope.row.actualQty != null" :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.actualQty }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="差异" prop="diffQty" :width="colWidth('diffQty', 80)" resizable align="right">
                <template #default="scope">
                  <span v-if="scope.row.diffQty != null" :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="差异原因" prop="diffReason" show-overflow-tooltip />
              <el-table-column label="操作" width="120" align="center" v-if="currentTake.status === '1'">
                <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="openSubmit(scope.row)">录入</el-button>
                </template>
              </el-table-column>
            </el-table>
            <pagination v-show="(currentTake.detailList || []).length > 0" :total="(currentTake.detailList || []).length" v-model:page="detailPage.pageNum" v-model:limit="detailPage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
          </div>
        </section>
<!-- 审批信息 -->
<section class="rd-card" v-if="currentTake.approveLogList && currentTake.approveLogList.length">
<div class="rd-card-header" @click="toggleCard('dApprove')">
<div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批信息</div>
<button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dApprove }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
</div>
<div class="rd-card-body" v-show="!collapsedCards.dApprove">
<div class="rd-timeline">
<div class="rd-timeline-item" v-for="item in currentTake.approveLogList" :key="item.logId">
<div class="rd-timeline-dot" :class="{ 'rd-timeline-dot--success': item.approveAction === 'pass', 'rd-timeline-dot--error': item.approveAction === 'reject' }"></div>
<div class="rd-timeline-content">
<div class="rd-timeline-header">
<span class="rd-timeline-title">
<el-tag v-if="item.approveAction === 'pass'" type="success" size="small" effect="light" round>审批通过</el-tag>
<el-tag v-else-if="item.approveAction === 'reject'" type="danger" size="small" effect="light" round>审批驳回</el-tag>
</span>
<span class="rd-timeline-time">{{ item.approveTime }}</span>
</div>
<div class="rd-timeline-body">
<div class="rd-item"><span class="rd-label">审批人</span><div class="rd-value">{{ item.approveBy }}</div></div>
</div>
<div class="rd-timeline-comment" v-if="item.approveOpinion">
<strong>审批意见：</strong>{{ item.approveOpinion }}
</div>
</div>
</div>
</div>
</div>
</section>
      </div>
      <template #footer><el-button type="primary" @click="handleSubmitForApproval" v-if="currentTake.status === '1'">提交审批</el-button><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 盘点审批对话框 -->
    <el-dialog v-model="approveOpen" width="1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></div>
          <span class="rd-detail-header-title">盘点审批</span>
          <div class="rd-detail-header-sub" v-if="approveFormData.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveFormData.takeNo }}</span>
            <dict-tag :options="wms_take_status" :value="approveFormData.status" />
          </div>
        </div>
      </template>
      <div>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">盘点单号</span><div class="rd-value">{{ approveFormData.takeNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_take_status" :value="approveFormData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">盘点类型</span><div class="rd-value"><dict-tag :options="wms_take_type" :value="approveFormData.takeType" /></div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ approveFormData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">库区</span><div class="rd-value">{{ approveFormData.areaName || '全仓库' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ approveFormData.planDate }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ approveFormData.createBy }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ approveFormData.createTime }}</div></div>
            </div>
          </div>
        </section>
        <!-- 盘点明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aDetail')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>盘点明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aDetail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aDetail">
            <el-table :data="approvePageList" border size="small">
              <el-table-column label="物料编码" prop="materialCode" width="120" />
              <el-table-column label="物料名称" prop="materialName" min-width="180" show-overflow-tooltip />
              <el-table-column label="批次号" prop="batchNo" width="100" />
              <el-table-column label="账面数量" prop="bookQty" width="100" align="right" />
              <el-table-column label="实盘数量" prop="actualQty" width="100" align="right" />
              <el-table-column label="差异" prop="diffQty" width="100" align="right">
                <template #default="scope"><span :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span></template>
              </el-table-column>
              <el-table-column label="差异原因" prop="diffReason" min-width="150" show-overflow-tooltip />
            </el-table>
            <pagination v-show="(approveFormData.detailList || []).length > 0" :total="(approveFormData.detailList || []).length" v-model:page="approvePage.pageNum" v-model:limit="approvePage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
          </div>
        </section>
        <!-- 审批意见 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aOpinion')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg></span>审批意见</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aOpinion }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aOpinion">
            <el-form label-width="100px">
              <el-form-item label="审批意见" required>
                <el-input v-model="approveFormData.approveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="success" @click="confirmApprove(true)">通过</el-button>
        <el-button type="danger" @click="confirmApprove(false)">驳回</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 录入实盘数量 -->
    <el-dialog v-model="submitOpen" width="660px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">录入实盘数量</span>
        </div>
      </template>
<div>
<el-form ref="submitRef" :model="submitFormData" :rules="submitRules" label-width="100px">
<section class="rd-card">
<div class="rd-card-header" @click="toggleCard('sMaterial')">
<div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
<button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sMaterial }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
</div>
<div class="rd-card-body" v-show="!collapsedCards.sMaterial">
<div class="rd-grid">
<div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ submitFormData.materialCode || '-' }}</div></div>
<div class="rd-item rd-item--full"><span class="rd-label">物料名称</span><div class="rd-value">{{ submitFormData.materialName }}</div></div>
<div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ submitFormData.batchNo || '-' }}</div></div>
<div class="rd-item"><span class="rd-label">库区/库位</span><div class="rd-value">{{ [submitFormData.areaName, submitFormData.locationName].filter(Boolean).join(' / ') || '-' }}</div></div>
</div>
</div>
</section>
<section class="rd-card">
<div class="rd-card-header" @click="toggleCard('sInput')">
<div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>盘点信息</div>
<button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sInput }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
</div>
<div class="rd-card-body" v-show="!collapsedCards.sInput">
<div class="rd-grid" style="margin-bottom: 16px">
<div class="rd-item"><span class="rd-label">账面数量</span><div class="rd-value">{{ submitFormData.bookQty }}</div></div>
<div class="rd-item"><span class="rd-label">当前差异</span><div class="rd-value" :style="{ color: (submitFormData.actualQty - submitFormData.bookQty) < 0 ? '#f56c6c' : (submitFormData.actualQty - submitFormData.bookQty) > 0 ? '#67c23a' : '' }">{{ (submitFormData.actualQty - submitFormData.bookQty).toFixed(2) }}</div></div>
</div>
<el-form-item label="实盘数量" prop="actualQty">
<el-input-number v-model="submitFormData.actualQty" :precision="2" :min="0" style="width: 100%" />
</el-form-item>
<el-form-item label="差异原因" :rules="[{ required: (submitFormData.actualQty - submitFormData.bookQty) !== 0, message: '存在差异时差异原因必填', trigger: 'blur' }]" prop="diffReason"><el-input v-model="submitFormData.diffReason" type="textarea" placeholder="存在差异时必填" /></el-form-item>
</div>
</section>
</el-form>
</div>
      <template #footer>
        <el-button type="primary" @click="submitDetailForm">确 定</el-button>
        <el-button @click="submitOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量录入 -->
    <el-dialog v-model="batchOpen" width="1100px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">批量录入实盘数量</span>
          <div class="rd-detail-header-sub" v-if="currentTake.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ currentTake.takeNo }}</span>
          </div>
        </div>
      </template>
      <el-table :data="batchPageList" border size="small" max-height="500">
        <el-table-column label="物料编码" prop="materialCode" width="120" />
        <el-table-column label="物料名称" prop="materialName" min-width="160" show-overflow-tooltip />
        <el-table-column label="库区/库位" width="140">
          <template #default="scope">{{ [scope.row.areaName, scope.row.locationName].filter(Boolean).join(' / ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="批次号" prop="batchNo" width="100" />
        <el-table-column label="账面数量" prop="bookQty" width="100" align="right" />
        <el-table-column label="实盘数量" width="140" align="center">
          <template #default="scope">
            <el-input-number v-model="scope.row.actualQty" :precision="2" :min="0" :controls="false" size="small" style="width: 110px" />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="80" align="right">
          <template #default="scope">
            <span :style="{ color: getRowDiff(scope.row) < 0 ? '#f56c6c' : getRowDiff(scope.row) > 0 ? '#67c23a' : '' }">{{ getRowDiff(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" min-width="150">
          <template #default="scope">
            <el-input v-model="scope.row.diffReason" size="small" placeholder="差异原因" />
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="(batchForm.detailList || []).length > 0" :total="(batchForm.detailList || []).length" v-model:page="batchPage.pageNum" v-model:limit="batchPage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
      <template #footer>
        <el-button type="primary" @click="submitBatchForm">提交录入</el-button>
        <el-button @click="batchOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入 -->
    <ExcelImportDialog ref="importRef" title="批量导入盘点明细" width="460px" :action="importAction" :template-action="templateAction" template-file-name="盘点明细模板" update-support-label="" @success="handleImportSuccess" />
  </div>
</template>

<script setup name="WmsStockTakeDetail">
import { listStockTake, getStockTake, submitStockTakeDetail, approveStockTake, rejectStockTake, submitForApproval, batchSubmitDetail } from '@/api/wms/stocktake'
import { importDetailUrl, exportTemplateUrl } from '@/api/wms/stocktake'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'
const { collapsedCards, toggleCard } = useDetailCard(['dBasic', 'dDetail', 'dApprove', 'aBasic', 'aDetail', 'aOpinion', 'sMaterial', 'sInput'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_stocktake_detail')
const { wms_take_type, wms_take_status } = proxy.useDict('wms_take_type', 'wms_take_status')
const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const currentTake = ref({})
const submitOpen = ref(false)
const submitFormData = ref({})
const approveOpen = ref(false)
const approveFormData = ref({})
const batchOpen = ref(false)
const batchForm = ref({ takeId: null, detailList: [] })
// 批量录入前端分页
const batchPage = reactive({ pageNum: 1, pageSize: 10 })
const batchPageList = computed(() => {
  const list = batchForm.value.detailList || []
  const start = (batchPage.pageNum - 1) * batchPage.pageSize
  return list.slice(start, start + batchPage.pageSize)
})
const importRef = ref(null)
// 盘点明细前端分页
const detailPage = reactive({ pageNum: 1, pageSize: 10 })
const detailPageList = computed(() => {
  const list = currentTake.value.detailList || []
  const start = (detailPage.pageNum - 1) * detailPage.pageSize
  return list.slice(start, start + detailPage.pageSize)
})
const approvePage = reactive({ pageNum: 1, pageSize: 10 })
const approvePageList = computed(() => {
  const list = approveFormData.value.detailList || []
  const start = (approvePage.pageNum - 1) * approvePage.pageSize
  return list.slice(start, start + approvePage.pageSize)
})
const submitRules = {
  actualQty: [{ required: true, message: '请输入实盘数量', trigger: 'blur' }],
  diffReason: [{ validator: (rule, value, callback) => { if ((submitFormData.value.actualQty - submitFormData.value.bookQty) !== 0 && !value) { callback(new Error('存在差异时差异原因必填')) } else { callback() } }, trigger: 'blur' }]
}

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, takeNo: undefined, takeType: undefined, status: undefined }
})
const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listStockTake(queryParams.value).then(res => {
    list.value = res.rows
    total.value = res.total
    loading.value = false
  })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleRowClick(row) {
  getStockTake(row.takeId).then(res => {
    currentTake.value = res.data
    detailPage.pageNum = 1
    detailOpen.value = true
  })
}
function openSubmit(row) {
  submitFormData.value = {
    takeId: currentTake.value.takeId,
    detailId: row.detailId,
    materialCode: row.materialCode,
    materialName: row.materialName,
    locationCode: row.locationCode,
    locationName: row.locationName,
    areaName: row.areaName,
    batchNo: row.batchNo,
    bookQty: row.bookQty,
    actualQty: row.actualQty != null ? row.actualQty : row.bookQty,
    diffReason: row.diffReason
  }
  submitOpen.value = true
}
function submitDetailForm() {
  proxy.$refs['submitRef'].validate(valid => {
    if (valid) {
      submitStockTakeDetail(submitFormData.value.takeId, submitFormData.value.detailId, submitFormData.value.actualQty, submitFormData.value.diffReason).then(() => {
        proxy.$modal.msgSuccess('录入成功')
        submitOpen.value = false
        getStockTake(currentTake.value.takeId).then(res => {
          currentTake.value = res.data
        })
      })
    }
  })
}
// 批量录入
function openBatch() {
  const details = (currentTake.value.detailList || []).map(d => ({
    detailId: d.detailId,
    materialCode: d.materialCode,
    materialName: d.materialName,
    areaName: d.areaName,
    locationName: d.locationName,
    batchNo: d.batchNo,
    bookQty: d.bookQty,
    actualQty: d.actualQty != null ? d.actualQty : d.bookQty,
    diffReason: d.diffReason || '',
    status: d.status
  }))
  batchForm.value = { takeId: currentTake.value.takeId, detailList: details }
  batchPage.pageNum = 1
  batchOpen.value = true
}
function getRowDiff(row) {
  if (row.actualQty == null || row.bookQty == null) return 0
  return Number((row.actualQty - row.bookQty).toFixed(2))
}
function submitBatchForm() {
  const payload = batchForm.value.detailList.map(d => ({
    detailId: d.detailId,
    actualQty: d.actualQty,
    diffReason: d.diffReason
  }))
  batchSubmitDetail(batchForm.value.takeId, payload).then(res => {
    proxy.$modal.msgSuccess(res.msg || '批量录入成功')
    batchOpen.value = false
    getStockTake(currentTake.value.takeId).then(r => {
      currentTake.value = r.data
    })
  })
}
// 批量导入
const importAction = computed(() => importDetailUrl(currentTake.value.takeId))
const templateAction = computed(() => exportTemplateUrl(currentTake.value.takeId))
function handleImport() {
  importRef.value.open()
}
function handleImportSuccess() {
  getStockTake(currentTake.value.takeId).then(res => {
    currentTake.value = res.data
  })
  getList()
}
function handleSubmitForApproval() {
  proxy.$modal.confirm('确认提交审批？提交后将等待审批人审批。').then(() => {
    return submitForApproval(currentTake.value.takeId)
  }).then(() => {
    proxy.$modal.msgSuccess('已提交审批')
    getStockTake(currentTake.value.takeId).then(res => {
      currentTake.value = res.data
    })
    getList()
  }).catch(() => {})
}
function handleApprove(row) {
  getStockTake(row.takeId).then(res => {
    approveFormData.value = { ...res.data, approveOpinion: '' }
    approvePage.pageNum = 1
    approveOpen.value = true
  })
}
function confirmApprove(passed) {
  if (!approveFormData.value.approveOpinion) {
    proxy.$modal.msgWarning('请输入审批意见')
    return
  }
  if (passed) {
    approveStockTake(approveFormData.value.takeId, approveFormData.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('审批通过，库存已自动调整')
      approveOpen.value = false
      getList()
    })
  } else {
    rejectStockTake(approveFormData.value.takeId, approveFormData.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('已驳回，盘点单已退回盘点人员重新核实')
      approveOpen.value = false
      getList()
    })
  }
}
// 初始化
getList()
</script>
