<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="takeNo"><el-input v-model="queryParams.takeNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="盘点类型" prop="takeType"><el-select v-model="queryParams.takeType" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_take_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:stocktake:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:stocktake:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="盘点单号" prop="takeNo" :width="colWidth('takeNo', 160)" resizable />
      <el-table-column label="仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="库区" prop="areaName" :width="colWidth('areaName', 120)" resizable />
      <el-table-column label="盘点类型" prop="takeType" :width="colWidth('takeType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_take_type" :value="scope.row.takeType" /></template></el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_take_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="计划日期" prop="planDate" :width="colWidth('planDate', 120)" resizable align="center" />
      <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" />
      <el-table-column label="操作" width="300" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="VideoPlay" @click="handleStart(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['wms:stocktake:edit']">开始</el-button>
          <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="scope.row.status === '2'" v-hasPermi="['wms:stocktake:edit']">审批</el-button>
          <el-button link type="danger" icon="CircleClose" @click="handleVoid(scope.row)" v-if="scope.row.status === '0' || scope.row.status === '1'" v-hasPermi="['wms:stocktake:edit']">作废</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:stocktake:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
        <el-form ref="takeRef" :model="form" :rules="rules" label-width="100px">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>盘点范围</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%" @change="onWarehouseChange"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="库区" prop="areaId"><el-select v-model="form.areaId" filterable clearable placeholder="请选择库区（可选）" style="width:100%"><el-option v-for="a in areaOptions" :key="a.warehouseId" :label="a.warehouseName" :value="a.warehouseId" /></el-select></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('setting')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>盘点设置</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.setting }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.setting">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="盘点类型" prop="takeType"><el-select v-model="form.takeType" style="width:100%" @change="onTakeTypeChange"><el-option v-for="d in wms_take_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="12" v-if="form.takeType === '1'"><el-form-item label="抽样比例" prop="sampleRatio"><el-slider v-model="form.sampleRatio" :min="10" :max="100" :step="10" show-input style="width:100%" /></el-form-item></el-col>
                <el-col :span="12" v-if="form.takeType === '2'"><el-form-item label="循环批次" prop="cycleNo"><el-select v-model="form.cycleNo" style="width:100%"><el-option :value="1" label="第1轮（共4轮）" /><el-option :value="2" label="第2轮（共4轮）" /><el-option :value="3" label="第3轮（共4轮）" /><el-option :value="4" label="第4轮（共4轮）" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('other')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>其他信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
            </div>
          </section>
        </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailOpen" width="1296px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg></div>
          <span class="rd-detail-header-title">盘点详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.takeNo }}</span>
          </div>
        </div>
      </template>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">盘点单号</span><div class="rd-value">{{ detailData.takeNo }}</div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">库区</span><div class="rd-value">{{ detailData.areaName || '全仓库' }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_take_status" :value="detailData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">盘点类型</span><div class="rd-value"><dict-tag :options="wms_take_type" :value="detailData.takeType" /></div></div>
              <div class="rd-item" v-if="detailData.takeType === '1'"><span class="rd-label">抽样比例</span><div class="rd-value">{{ detailData.sampleRatio }}%</div></div>
              <div class="rd-item" v-if="detailData.takeType === '2'"><span class="rd-label">循环批次</span><div class="rd-value">第{{ detailData.cycleNo }}轮（共4轮）</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ detailData.planDate }}</div></div>
              <div class="rd-item" v-if="detailData.startTime"><span class="rd-label">开始时间</span><div class="rd-value">{{ detailData.startTime }}</div></div>
              <div class="rd-item" v-if="detailData.endTime"><span class="rd-label">结束时间</span><div class="rd-value">{{ detailData.endTime }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detailData.remark }">{{ detailData.remark || '暂无备注' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dDetail')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>盘点明细</div>
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
              <el-table-column label="实盘数量" prop="actualQty" :width="colWidth('actualQty', 100)" resizable align="right" />
              <el-table-column label="差异" prop="diffQty" :width="colWidth('diffQty', 100)" resizable align="right">
                <template #default="scope"><span :style="{color: scope.row.diffQty < 0 ? 'red' : scope.row.diffQty > 0 ? 'green' : ''}">{{ scope.row.diffQty }}</span></template>
              </el-table-column>
              <el-table-column label="差异原因" prop="diffReason" show-overflow-tooltip />
            </el-table>
            <pagination v-show="(detailData.detailList || []).length > 0" :total="(detailData.detailList || []).length" v-model:page="detailPage.pageNum" v-model:limit="detailPage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
          </div>
        </section>
        <!-- 审批信息 -->
        <section class="rd-card" v-if="detailData.approveLogList && detailData.approveLogList.length">
          <div class="rd-card-header" @click="toggleCard('dApprove')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></span>审批信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dApprove }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dApprove">
            <div class="rd-timeline">
              <div class="rd-timeline-item" v-for="item in detailData.approveLogList" :key="item.logId">
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
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog v-model="approveOpen" width="1152px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg></div>
          <span class="rd-detail-header-title">盘点审批</span>
          <div class="rd-detail-header-sub" v-if="approveForm.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ approveForm.takeNo }}</span>
            <dict-tag :options="wms_take_status" :value="approveForm.status" />
          </div>
        </div>
      </template>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('aBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.aBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.aBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">盘点单号</span><div class="rd-value">{{ approveForm.takeNo }}</div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_take_status" :value="approveForm.status" /></div></div>
              <div class="rd-item"><span class="rd-label">盘点类型</span><div class="rd-value"><dict-tag :options="wms_take_type" :value="approveForm.takeType" /></div></div>
              <div class="rd-item"><span class="rd-label">仓库</span><div class="rd-value">{{ approveForm.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">库区</span><div class="rd-value">{{ approveForm.areaName || '全仓库' }}</div></div>
              <div class="rd-item"><span class="rd-label">计划日期</span><div class="rd-value">{{ approveForm.planDate }}</div></div>
              <div class="rd-item"><span class="rd-label">创建人</span><div class="rd-value">{{ approveForm.createBy }}</div></div>
              <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ approveForm.createTime }}</div></div>
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
            <pagination v-show="(approveForm.detailList || []).length > 0" :total="(approveForm.detailList || []).length" v-model:page="approvePage.pageNum" v-model:limit="approvePage.pageSize" :page-sizes="[10, 20, 50, 100]" layout="total, prev, pager, next, jumper" />
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
                <el-input v-model="approveForm.approveOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
              </el-form-item>
            </el-form>
          </div>
        </section>
      <template #footer>
        <el-button type="success" @click="confirmApprove(true)">通过</el-button>
        <el-button type="danger" @click="confirmApprove(false)">驳回</el-button>
        <el-button @click="approveOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 录入实盘数量 -->
    <el-dialog v-model="submitOpen" width="600px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></div>
          <span class="rd-detail-header-title">录入实盘数量</span>
          <div class="rd-detail-header-sub" v-if="detailData.takeNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.takeNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="submitRef" :model="inputForm" label-width="100px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('sMaterial')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg></span>物料信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sMaterial }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.sMaterial">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ inputForm.materialCode || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">物料名称</span><div class="rd-value">{{ inputForm.materialName }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ inputForm.batchNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">库区/库位</span><div class="rd-value">{{ [inputForm.areaName, inputForm.locationName].filter(Boolean).join(' / ') || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('sBasic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></span>盘点信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.sBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.sBasic">
            <div class="rd-grid" style="margin-bottom: 16px">
              <div class="rd-item"><span class="rd-label">账面数量</span><div class="rd-value">{{ inputForm.bookQty }}</div></div>
              <div class="rd-item"><span class="rd-label">当前差异</span><div class="rd-value" :style="{ color: (inputForm.actualQty - inputForm.bookQty) < 0 ? '#f56c6c' : (inputForm.actualQty - inputForm.bookQty) > 0 ? '#67c23a' : '' }">{{ (inputForm.actualQty - inputForm.bookQty).toFixed(2) }}</div></div>
            </div>
            <el-form-item label="实盘数量" prop="actualQty"><el-input-number v-model="inputForm.actualQty" :precision="2" :min="0" style="width: 100%" /></el-form-item>
            <el-form-item label="差异原因"><el-input v-model="inputForm.diffReason" type="textarea" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitDetailForm">确 定</el-button><el-button @click="submitOpen = false">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="WmsStockTake">
import { listStockTake, getStockTake, addStockTake, delStockTake, startStockTake, submitStockTakeDetail, voidStockTake, approveStockTake, rejectStockTake, submitForApproval } from '@/api/wms/stocktake'
import { listWarehouse, listArea } from '@/api/wms/warehouse'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'setting', 'other', 'dBasic', 'dDetail', 'dApprove', 'aBasic', 'aDetail', 'aOpinion', 'sMaterial', 'sBasic'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_stocktake_index')
const { wms_take_type, wms_take_status } = proxy.useDict('wms_take_type', 'wms_take_status')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const detailOpen = ref(false); const detailData = ref({}); const submitOpen = ref(false); const approveOpen = ref(false)
const inputForm = ref({})
const approveForm = ref({})
// 盘点明细前端分页
const detailPage = reactive({ pageNum: 1, pageSize: 10 })
const detailPageList = computed(() => {
  const list = detailData.value.detailList || []
  const start = (detailPage.pageNum - 1) * detailPage.pageSize
  return list.slice(start, start + detailPage.pageSize)
})
const approvePage = reactive({ pageNum: 1, pageSize: 10 })
const approvePageList = computed(() => {
  const list = approveForm.value.detailList || []
  const start = (approvePage.pageNum - 1) * approvePage.pageSize
  return list.slice(start, start + approvePage.pageSize)
})
const warehouseOptions = ref([]); const areaOptions = ref([])
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, takeNo: undefined, takeType: undefined, status: undefined }, rules: { warehouseId: [{ required: true, message: '仓库不能为空', trigger: 'change' }], takeType: [{ required: true, message: '盘点类型不能为空', trigger: 'change' }], planDate: [{ required: true, message: '计划日期不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listStockTake(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.takeId); multiple.value = !sel.length }
function reset() { form.value = { warehouseId: undefined, areaId: undefined, takeType: '0', sampleRatio: 30, cycleNo: 1, planDate: undefined, remark: undefined }; areaOptions.value = []; proxy.resetForm('takeRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加盘点单' }
function submitForm() { proxy.$refs['takeRef'].validate(v => { if (v) { addStockTake(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } }) }
function handleDetail(row) { getStockTake(row.takeId).then(res => { detailData.value = res.data; detailPage.pageNum = 1; detailOpen.value = true }) }
function handleApprove(row) { getStockTake(row.takeId).then(res => { approveForm.value = { ...res.data, approveOpinion: '' }; approvePage.pageNum = 1; approveOpen.value = true }) }
function confirmApprove(passed) {
  if (!approveForm.value.approveOpinion) { proxy.$modal.msgWarning('请输入审批意见'); return }
  if (passed) {
    approveStockTake(approveForm.value.takeId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('审批通过'); approveOpen.value = false; getList()
    })
  } else {
    rejectStockTake(approveForm.value.takeId, approveForm.value.approveOpinion).then(() => {
      proxy.$modal.msgSuccess('已驳回，盘点单已退回盘点人员重新核实'); approveOpen.value = false; getList()
    })
  }
}
function handleStart(row) { proxy.$modal.confirm('确认开始盘点？').then(() => startStockTake(row.takeId)).then(() => { getList(); proxy.$modal.msgSuccess('已开始') }).catch(() => {}) }
function handleVoid(row) { proxy.$modal.confirm('确认作废该盘点单？作废后将无法继续盘点。').then(() => voidStockTake(row.takeId)).then(() => { getList(); proxy.$modal.msgSuccess('已作废') }).catch(() => {}) }
function handleSubmitDetail(row) { inputForm.value = { takeId: detailData.value.takeId, detailId: row.detailId, materialCode: row.materialCode, materialName: row.materialName, batchNo: row.batchNo, areaName: row.areaName, locationName: row.locationName, bookQty: row.bookQty, actualQty: row.actualQty || row.bookQty, diffReason: undefined }; submitOpen.value = true }
function submitDetailForm() { submitStockTakeDetail(inputForm.value.takeId, inputForm.value.detailId, inputForm.value.actualQty, inputForm.value.diffReason).then(() => { proxy.$modal.msgSuccess('录入成功'); submitOpen.value = false; getStockTake(detailData.value.takeId).then(res => { detailData.value = res.data }) }) }
function handleSubmitForApproval() { proxy.$modal.confirm('确认提交审批？提交后将等待审批人审批。').then(() => submitForApproval(detailData.value.takeId)).then(() => { proxy.$modal.msgSuccess('已提交审批'); getStockTake(detailData.value.takeId).then(res => { detailData.value = res.data }); getList() }).catch(() => {}) }
function handleDelete(row) { const takeIds = row.takeId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delStockTake(takeIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function cancel() { open.value = false; reset() }
function onTakeTypeChange() {
  if (form.value.takeType === '1' && !form.value.sampleRatio) { form.value.sampleRatio = 30 }
  if (form.value.takeType === '2' && !form.value.cycleNo) { form.value.cycleNo = 1 }
}
function onWarehouseChange(warehouseId) {
  form.value.areaId = undefined
  if (warehouseId) {
    listArea({ warehouseId: warehouseId, status: '0', pageSize: 999 }).then(res => { areaOptions.value = res.rows })
  } else {
    areaOptions.value = []
  }
}
listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
getList()
</script>
