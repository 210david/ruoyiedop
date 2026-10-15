<template>
  <div class="app-container wms-list-page">
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
          <label>召回单号</label>
          <div class="control">
            <el-input v-model="queryParams.recallCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>触发批次号</label>
          <div class="control">
            <el-input v-model="queryParams.triggerBatchCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>单据状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_recall_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>召回范围</label>
          <div class="control is-select">
            <el-select v-model="queryParams.recallScope" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_recall_scope" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>召回等级</label>
          <div class="control is-select">
            <el-select v-model="queryParams.recallLevel" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_recall_level" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>触发物料</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>执行人</label>
          <div class="control">
            <el-input v-model="queryParams.executeBy" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建人</label>
          <div class="control">
            <el-input v-model="queryParams.createBy" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>执行时间</label>
          <div class="control">
            <el-date-picker v-model="executeDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced" style="grid-column: span 2">
          <label>创建时间</label>
          <div class="control">
            <el-date-picker v-model="createDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <button v-for="s in statusTabList" :key="s.value" class="status-tab" :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]" @click="handleStatusTabClick(s.value)">
            <span class="dot"></span><span>{{ s.label }}</span><span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon><span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:recall:add']">新建召回单</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['food:recall:remove']">删除</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['food:recall:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_recall_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="recallList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="召回单号" prop="recallCode" key="recallCode" :min-width="colWidth('recallCode', 160)" resizable show-overflow-tooltip v-if="columns.recallCode.visible" />
          <el-table-column label="触发批次号" prop="triggerBatchCode" key="triggerBatchCode" :min-width="colWidth('triggerBatchCode', 170)" resizable show-overflow-tooltip v-if="columns.triggerBatchCode.visible" />
          <el-table-column label="触发物料" prop="materialName" key="materialName" :min-width="colWidth('materialName', 140)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="召回范围" prop="recallScope" key="recallScope" :width="colWidth('recallScope', 110)" resizable align="center" v-if="columns.recallScope.visible">
            <template #default="scope">
              <span class="badge blue">{{ dictLabel(food_recall_scope, scope.row.recallScope) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="召回等级" prop="recallLevel" key="recallLevel" :width="colWidth('recallLevel', 100)" resizable align="center" v-if="columns.recallLevel.visible">
            <template #default="scope">
              <span class="badge" :class="levelClass(scope.row.recallLevel)"><span class="dot"></span>{{ dictLabel(food_recall_level, scope.row.recallLevel) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="圈批数量" prop="affectedCount" key="affectedCount" :width="colWidth('affectedCount', 90)" resizable align="right" v-if="columns.affectedCount.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="statusClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(food_recall_status, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="召回原因" prop="recallReason" key="recallReason" :min-width="colWidth('recallReason', 150)" resizable show-overflow-tooltip v-if="columns.recallReason.visible" />
          <el-table-column label="执行人" prop="executeBy" key="executeBy" :width="colWidth('executeBy', 100)" resizable v-if="columns.executeBy.visible" />
          <el-table-column label="执行时间" prop="executeTime" key="executeTime" :width="colWidth('executeTime', 160)" resizable v-if="columns.executeTime.visible" />
          <el-table-column label="创建人" prop="createBy" key="createBy" :width="colWidth('createBy', 100)" resizable v-if="columns.createBy.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable v-if="columns.createTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 230)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">明细</el-button>
                <el-button v-if="scope.row.status === '0'" link type="success" icon="CaretRight" @click="handleExecute(scope.row)" v-hasPermi="['food:recall:execute']">执行召回</el-button>
                <el-button v-if="scope.row.status === '0'" link type="warning" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['food:recall:cancel']">取消</el-button>
                <el-button v-if="scope.row.status !== '1'" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['food:recall:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新建召回单 -->
    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog" @closed="onDialogClosed">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="recallRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 单据信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('a1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.a1">
              <el-form-item label="触发批次" prop="triggerBatchId">
                <el-input :model-value="form.triggerBatchCode || ''" readonly placeholder="点击选择触发批次（问题批次）" @click="openBatchPicker">
                  <template v-if="form.triggerBatchId" #append><el-button icon="CircleClose" @click.stop="clearTriggerBatch" /></template>
                  <template v-else #append><el-button icon="Search" @click="openBatchPicker" /></template>
                </el-input>
              </el-form-item>
              <el-form-item label="召回范围" prop="recallScope">
                <el-radio-group v-model="form.recallScope" @change="loadCircles">
                  <el-radio-button v-for="d in food_recall_scope" :key="d.value" :value="d.value">{{ d.label }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="召回等级" prop="recallLevel">
                <el-radio-group v-model="form.recallLevel">
                  <el-radio-button v-for="d in food_recall_level" :key="d.value" :value="d.value">{{ d.label }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="召回原因" prop="recallReason">
                <el-input v-model="form.recallReason" type="textarea" :rows="2" placeholder="请输入召回原因（如：原料抽检不合格、客户投诉异物等）" />
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" />
              </el-form-item>
            </div>
          </section>
          <!-- 圈批预览 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('a2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>圈批预览（按谱系自动圈出关联批次）</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.a2">
              <el-alert v-if="!form.triggerBatchId" title="请先选择触发批次，系统将按谱系自动圈出关联批次并预览" type="info" :closable="false" style="margin-bottom: 8px" />
              <el-alert v-else type="warning" :closable="false" style="margin-bottom: 8px">
                <template #title>按谱系圈出 <b>{{ circleList.length }}</b> 批（含触发批次），执行召回后圈内批次将被<strong>强制冻结隔离</strong>，请仔细核对</template>
              </el-alert>
              <el-table :data="circleList" size="small" border max-height="320" v-loading="circleLoading">
                <el-table-column type="index" label="序号" width="55" align="center" />
                <el-table-column label="批次号" prop="batchCode" min-width="150" show-overflow-tooltip />
                <el-table-column label="物料" min-width="150" show-overflow-tooltip>
                  <template #default="scope">{{ scope.row.materialName || '-' }}</template>
                </el-table-column>
                <el-table-column label="层级" prop="depth" width="70" align="center">
                  <template #default="scope">{{ scope.row.depth === 0 ? '触发' : scope.row.depth }}</template>
                </el-table-column>
                <el-table-column label="关联路径" min-width="200" show-overflow-tooltip>
                  <template #default="scope">{{ scope.row.relationPath }}</template>
                </el-table-column>
                <el-table-column label="批次数量" prop="qty" width="100" align="right">
                  <template #default="scope">{{ formatQty(scope.row.qty) }}</template>
                </el-table-column>
                <el-table-column label="到期日期" prop="expireDate" width="105" align="center" />
                <el-table-column label="批次状态" width="90" align="center">
                  <template #default="scope">
                    <span class="badge" :class="batchStatusClass(scope.row.batchStatus)"><span class="dot"></span>{{ dictLabel(food_batch_status, scope.row.batchStatus) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">保存待执行</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 明细查看 -->
    <el-dialog v-model="detailOpen" width="880px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></div>
          <span class="rd-detail-header-title">召回单明细</span>
          <div class="rd-detail-header-sub" v-if="detailRow">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ detailRow.recallCode }}</span>
          </div>
        </div>
      </template>
      <div class="rd-page" v-if="detailRow">
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dt1')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dt1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dt1">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">触发批次号</span><div class="rd-value">{{ detailRow.triggerBatchCode || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">召回范围</span><div class="rd-value"><dict-tag :options="food_recall_scope" :value="detailRow.recallScope" /></div></div>
              <div class="rd-item"><span class="rd-label">召回等级</span><div class="rd-value"><dict-tag :options="food_recall_level" :value="detailRow.recallLevel" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="food_recall_status" :value="detailRow.status" /></div></div>
              <div class="rd-item"><span class="rd-label">圈批数量</span><div class="rd-value">{{ detailRow.affectedCount != null ? detailRow.affectedCount + ' 批' : '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">执行人</span><div class="rd-value">{{ detailRow.executeBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">执行时间</span><div class="rd-value">{{ detailRow.executeTime || '-' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">召回原因</span><div class="rd-value" :class="{ 'rd-value--muted': !detailRow.recallReason }">{{ detailRow.recallReason || '暂无' }}</div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value" :class="{ 'rd-value--muted': !detailRow.remark }">{{ detailRow.remark || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 圈批明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dt2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>圈批明细</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dt2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dt2">
            <div class="toolbar" style="padding: 0 0 8px 0">
              <div class="left" v-if="detailRow.status === '1'">
                <el-button type="warning" plain icon="Download" size="small" @click="handleBatchExport" v-hasPermi="['food:recall:export']">导出圈批明细</el-button>
              </div>
            </div>
            <el-table :data="detailBatches" size="small" border>
              <el-table-column type="index" label="序号" width="55" align="center" />
              <el-table-column label="批次号" prop="batchCode" min-width="150" show-overflow-tooltip />
              <el-table-column label="物料" prop="materialName" min-width="140" show-overflow-tooltip />
              <el-table-column label="层级" prop="depth" width="70" align="center">
                <template #default="scope">{{ scope.row.depth === 0 ? '触发' : scope.row.depth }}</template>
              </el-table-column>
              <el-table-column label="关联路径" min-width="180" show-overflow-tooltip>
                <template #default="scope">{{ scope.row.relationPath || '-' }}</template>
              </el-table-column>
              <el-table-column label="召回前状态" width="100" align="center">
                <template #default="scope">
                  <span class="badge" :class="batchStatusClass(scope.row.prevStatus)"><span class="dot"></span>{{ dictLabel(food_batch_status, scope.row.prevStatus) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="是否冻结" width="90" align="center">
                <template #default="scope">
                  <span class="badge" :class="scope.row.frozenFlag === '1' ? 'red' : 'gray'"><span class="dot"></span>{{ scope.row.frozenFlag === '1' ? '已冻结' : '未冻结' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 批次选择器 -->
    <batch-picker ref="batchPickerRef" title="选择触发批次" @confirm="onBatchPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="召回隔离业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、召回单状态流转图</h4>
        <div class="status-flow">
          <div class="flow-item">
            <el-tag type="warning">待执行</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="success">已执行</el-tag>
          </div>
          <div class="flow-item" style="margin-left: 16px;">
            <el-tag type="warning">待执行</el-tag>
            <el-icon class="flow-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="flow-item">
            <el-tag type="info">已取消</el-tag>
          </div>
        </div>

        <h4>二、各状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="待执行">召回单已创建（选定触发批次、召回范围与等级），尚未执行圈批；批次暂未冻结，此状态下可执行或取消</el-descriptions-item>
          <el-descriptions-item label="已执行">系统按批次谱系<strong>自动圈批</strong>（触发批次+上游原料批次+下游成品批次），圈批数量计入召回单；<strong>全部关联批次强制冻结</strong>，冻结批次立即从出库可发列表排除；终态，不可取消、不可删除</el-descriptions-item>
          <el-descriptions-item label="已取消">召回单作废，批次无任何变更；仅待执行状态可取消</el-descriptions-item>
        </el-descriptions>

        <h4>三、召回等级与范围</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">召回等级（一级/二级/三级）</div>
              <div class="highlight-card-body">按危害程度分级：<strong>一级</strong>——食用后可能危及健康或生命（如致病菌、异物伤害），需立即停止销售并公告；<strong>二级</strong>——存在潜在健康风险；<strong>三级</strong>——不符合质量标准但危害较小（如标签瑕疵）。等级决定响应速度与通知范围</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">召回范围</div>
              <div class="highlight-card-body">定义圈批边界：仅触发批次本身，或按谱系<strong>向上追至原料批次、向下追至成品批次</strong>（正反向追溯），圈批结果在详情中可查、可导出</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、执行联动与合规要求</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">自动联动</div>
              <div class="highlight-card-body">执行时自动完成：谱系圈批 → 批次冻结（写状态日志）→ 圈批数量汇总；已建未发的出库单中冻结批次发不了，销售发货时同样被拦截</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">法规依据</div>
              <div class="highlight-card-body">对应《食品安全法》召回制度与 GB 14881 追溯要求：发现问题时<strong>先冻结止损、再评估处置</strong>；召回全程记录（圈批清单、冻结日志）保存备查</div>
            </div>
          </el-col>
        </el-row>

        <h4>五、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建召回单：</strong>选择触发批次（如质检异常批）、填写召回原因、等级与范围
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>执行召回：</strong>系统按谱系圈批并强制冻结全部关联批次，圈批明细可在详情查看/导出
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>处置与复盘：</strong>冻结批次按评估结果处置（报废/解冻），处置记录留痕后归档召回单
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FoodRecall">
import { listRecall, getRecall, circlesBatch, recallBatchList, addRecall, executeRecall, cancelRecall, delRecall } from "@/api/food/recall"
import BatchPicker from '@/components/BatchPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['a1', 'a2', 'dt1', 'dt2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_recall_index')
const { food_recall_scope, food_recall_level, food_recall_status, food_batch_status } = proxy.useDict("food_recall_scope", "food_recall_level", "food_recall_status", "food_batch_status")

// 列显隐配置
const defaultColumns = {
  recallCode: { label: '召回单号', visible: true },
  triggerBatchCode: { label: '触发批次号', visible: true },
  materialName: { label: '触发物料', visible: true },
  recallScope: { label: '召回范围', visible: true },
  recallLevel: { label: '召回等级', visible: true },
  affectedCount: { label: '圈批数量', visible: true },
  status: { label: '状态', visible: true },
  recallReason: { label: '召回原因', visible: true },
  executeBy: { label: '执行人', visible: true },
  executeTime: { label: '执行时间', visible: true },
  createBy: { label: '创建人', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_recall_columns')
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

const recallList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const batchPickerRef = ref(null)
const executeDateRange = ref([])
const createDateRange = ref([])

const circleList = ref([])
const circleLoading = ref(false)

const detailOpen = ref(false)
const detailRow = ref(null)
const detailBatches = ref([])

// ---------------- 状态卡片统计 ----------------
const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const showStatusHelp = ref(false)
const statusTabList = computed(() => food_recall_status.value)
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-partial' }; return map[value] || '' }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() { listRecall({ pageNum: 1, pageSize: 999 }).then(res => { const counts = { all: res.total, '0': 0, '1': 0, '2': 0 }; (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ }); statusCounts.value = counts }).catch(() => {}) }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, recallCode: undefined, triggerBatchCode: undefined, recallScope: undefined, recallLevel: undefined, status: undefined, materialName: undefined, executeBy: undefined, createBy: undefined, params: {} },
  rules: {
    triggerBatchId: [{ required: true, message: "触发批次不能为空", trigger: "change" }],
    recallReason: [{ required: true, message: "召回原因不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.recallCode) count++
  if (queryParams.value.triggerBatchCode) count++
  if (queryParams.value.recallScope) count++
  if (queryParams.value.recallLevel) count++
  if (queryParams.value.status) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.executeBy) count++
  if (queryParams.value.createBy) count++
  if (executeDateRange.value && executeDateRange.value.length > 0) count++
  if (createDateRange.value && createDateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  listRecall(queryParams.value).then(response => {
    recallList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

/** 字典值转标签 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}

function statusClass(v) {
  return { "0": "amber", "1": "green", "2": "gray" }[v] || "gray"
}

function levelClass(v) {
  return { "1": "blue", "2": "amber", "3": "red" }[v] || "gray"
}

function batchStatusClass(v) {
  return { "0": "amber", "1": "green", "2": "red", "3": "gray" }[v] || "gray"
}

function formatQty(v) {
  return v == null ? "0" : Number(v).toLocaleString(undefined, { maximumFractionDigits: 2 })
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, executeDateRange.value, 'ExecuteTime')
  proxy.addDateRange(queryParams.value, createDateRange.value, 'CreateTime')
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize,
    recallCode: undefined, triggerBatchCode: undefined, recallScope: undefined, recallLevel: undefined,
    status: undefined, materialName: undefined, executeBy: undefined, createBy: undefined, params: {} }
  executeDateRange.value = []
  createDateRange.value = []
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.recallId)
  multiple.value = !selection.length
}

function reset() {
  form.value = { recallId: undefined, triggerBatchId: undefined, triggerBatchCode: undefined, recallScope: "3", recallLevel: "2", recallReason: undefined, remark: undefined }
  proxy.resetForm("recallRef")
  circleList.value = []
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新建召回单"
}

/** 圈批预览：触发批次+范围任一变化即刷新 */
function loadCircles() {
  if (!form.value.triggerBatchId) {
    circleList.value = []
    return
  }
  circleLoading.value = true
  circlesBatch(form.value.triggerBatchId, form.value.recallScope).then(res => {
    circleList.value = res.data || []
    circleLoading.value = false
  }).catch(() => {
    circleLoading.value = false
  })
}

function openBatchPicker() {
  batchPickerRef.value.open(form.value.triggerBatchId)
}

function clearTriggerBatch() {
  form.value.triggerBatchId = undefined
  form.value.triggerBatchCode = undefined
  circleList.value = []
}

function onBatchPickerConfirm(batch) {
  form.value.triggerBatchId = batch.batchId
  form.value.triggerBatchCode = batch.batchCode
  loadCircles()
}

function onDialogClosed() {
  reset()
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["recallRef"].validate(valid => {
    if (!valid) return
    addRecall(form.value).then(() => {
      proxy.$modal.msgSuccess("召回单创建成功，可在列表中执行")
      open.value = false
      getList()
      loadStatusCounts()
    })
  })
}

function handleExecute(row) {
  proxy.$modal.confirm("确认执行召回【" + row.recallCode + "】吗？将按谱系圈批并强制冻结全部关联批次，冻结批次不可出库使用。").then(() => {
    return executeRecall(row.recallId)
  }).then(res => {
    proxy.$modal.msgSuccess(res.msg || "召回执行成功")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

function handleCancel(row) {
  proxy.$modal.confirm("确认取消召回单【" + row.recallCode + "】吗？").then(() => cancelRecall(row.recallId)).then(() => {
    proxy.$modal.msgSuccess("已取消")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

function handleDelete(row) {
  const deleteIds = row.recallId || ids.value
  proxy.$modal.confirm("是否确认删除所选召回单？已执行的召回单不可删除。").then(() => delRecall(deleteIds)).then(() => {
    getList()
    loadStatusCounts()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleDetail(row) {
  getRecall(row.recallId).then(res => {
    detailRow.value = res.data
    detailOpen.value = true
    // 已执行召回单加载圈批明细（含冻结结果）
    if (res.data && res.data.status === '1') {
      recallBatchList(row.recallId).then(r => {
        detailBatches.value = r.data || []
      })
    } else {
      detailBatches.value = (res.data && res.data.batchList) || []
    }
  })
}

function handleBatchExport() {
  proxy.download("/food/recall/batchExport/" + detailRow.value.recallId, {}, `recall_batch_${new Date().getTime()}.xlsx`)
}

/** 导出按钮 */
function handleExport() {
  proxy.download("food/recall/export", { ...queryParams.value }, `recall_${new Date().getTime()}.xlsx`)
}

onMounted(() => {
  getList()
  loadStatusCounts()
})
</script>

<style scoped>
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-flow { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; padding: 16px; background-color: #f5f7fa; border-radius: 8px; margin-bottom: 8px; }
.flow-item { display: flex; align-items: center; gap: 8px; }
.flow-arrow { color: #909399; font-size: 16px; }
.highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.highlight-success { background-color: #f0f9ff; border-color: #b3e19d; }
.highlight-danger { background-color: #fef0f0; border-color: #fbc4c4; }
.highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.highlight-success .highlight-card-title { color: #67c23a; }
.highlight-danger .highlight-card-title { color: #f56c6c; }
.highlight-primary .highlight-card-title { color: #409eff; }
.highlight-warning .highlight-card-title { color: #e6a23c; }
.highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
</style>
