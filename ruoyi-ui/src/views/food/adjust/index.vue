<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>调整单号</label>
          <div class="control">
            <el-input v-model="queryParams.adjustCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.sourceBatchCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>调整类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.adjustType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_adjust_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>单据状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_adjust_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:adjust:add']">新建调整单</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['food:adjust:remove']">删除</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_adjust_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="adjustList" border class="app-table" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="调整单号" prop="adjustCode" key="adjustCode" :min-width="colWidth('adjustCode', 180)" resizable show-overflow-tooltip v-if="columns.adjustCode.visible" />
          <el-table-column label="调整类型" prop="adjustType" key="adjustType" :width="colWidth('adjustType', 100)" resizable align="center" v-if="columns.adjustType.visible">
            <template #default="scope">
              <span class="badge violet">{{ dictLabel(food_adjust_type, scope.row.adjustType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="主批次" prop="sourceBatchCode" key="sourceBatchCode" :min-width="colWidth('sourceBatchCode', 160)" resizable show-overflow-tooltip v-if="columns.sourceBatchCode.visible" />
          <el-table-column label="原因" prop="reason" key="reason" :min-width="colWidth('reason', 160)" resizable show-overflow-tooltip v-if="columns.reason.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="adjustStatusClass(scope.row.status)"><span class="dot"></span>{{ dictLabel(food_adjust_status, scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建人" prop="createBy" key="createBy" :width="colWidth('createBy', 100)" resizable v-if="columns.createBy.visible" />
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable v-if="columns.createTime.visible" />
          <el-table-column label="执行时间" prop="executeTime" key="executeTime" :width="colWidth('executeTime', 160)" resizable v-if="columns.executeTime.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 230)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">明细</el-button>
                <el-button v-if="scope.row.status === '0'" link type="success" icon="CaretRight" @click="handleExecute(scope.row)" v-hasPermi="['food:adjust:execute']">执行</el-button>
                <el-button v-if="scope.row.status === '0'" link type="warning" icon="Close" @click="handleCancel(scope.row)" v-hasPermi="['food:adjust:cancel']">取消</el-button>
                <el-button v-if="scope.row.status !== '1'" link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['food:adjust:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 新建调整单 -->
    <el-dialog v-model="open" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="adjustRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <!-- 单据信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('a1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>单据信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.a1">
              <el-form-item label="调整类型" prop="adjustType">
                <el-radio-group v-model="form.adjustType" @change="onTypeChange">
                  <el-radio-button v-for="d in food_adjust_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item :label="needDetail ? '明细批次' : '目标批次'" prop="sourceBatchId" :required="!needDetail">
                <template v-if="!needDetail">
                  <el-input :model-value="form.sourceBatchCode || ''" readonly placeholder="点击选择批次" @click="openBatchPicker('target')">
                    <template v-if="form.sourceBatchId" #append><el-button icon="CircleClose" @click.stop="clearTargetBatch" /></template>
                    <template v-else #append><el-button icon="Search" @click="openBatchPicker('target')" /></template>
                  </el-input>
                </template>
                <template v-else>
                  <el-alert :title="detailTip" type="info" :closable="false" style="width: 100%; margin-bottom: 6px" />
                </template>
              </el-form-item>
              <el-form-item label="调整原因" prop="reason">
                <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="请输入调整原因" />
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" />
              </el-form-item>
            </div>
          </section>
          <!-- 调整明细 -->
          <section class="rd-card" v-show="needDetail">
            <div class="rd-card-header" @click="toggleCard('a2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>调整明细</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.a2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.a2">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="addItem">添加明细行</el-button></el-col>
              </el-row>
              <el-table :data="form.itemList" size="small" border>
                <el-table-column label="源批次" min-width="260">
                  <template #default="scope">
                    <el-input :model-value="scope.row.sourceBatchCode || ''" readonly size="small" placeholder="点击选择批次" @click="openBatchPicker('item', scope.$index)">
                      <template v-if="scope.row.sourceBatchId" #append><el-button size="small" icon="CircleClose" @click.stop="clearItemBatch(scope.$index)" /></template>
                      <template v-else #append><el-button size="small" icon="Search" @click="openBatchPicker('item', scope.$index)" /></template>
                    </el-input>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="140">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.qty" :min="0.01" :precision="2" size="small" controls-position="right" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column v-if="form.adjustType === '7'" label="新到期日期" width="160">
                  <template #default="scope">
                    <el-date-picker v-model="scope.row.newExpireDate" type="date" value-format="YYYY-MM-DD" size="small" style="width: 100%" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="70" align="center">
                  <template #default="scope">
                    <el-button link type="danger" size="small" @click="removeItem(scope.$index)">删除</el-button>
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
    <el-dialog v-model="detailOpen" width="720px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="17 1 21 5 17 9"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><polyline points="7 23 3 19 7 15"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg></div>
          <span class="rd-detail-header-title">调整单明细</span>
          <div class="rd-detail-header-sub" v-if="detailRow">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">{{ detailRow.adjustCode }}</span>
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
              <div class="rd-item"><span class="rd-label">调整类型</span><div class="rd-value"><dict-tag :options="food_adjust_type" :value="detailRow.adjustType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="food_adjust_status" :value="detailRow.status" /></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">原因</span><div class="rd-value" :class="{ 'rd-value--muted': !detailRow.reason }">{{ detailRow.reason || '暂无' }}</div></div>
              <div class="rd-item"><span class="rd-label">执行人</span><div class="rd-value">{{ detailRow.executeBy || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">执行时间</span><div class="rd-value">{{ detailRow.executeTime || '-' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 明细数据 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dt2')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>明细数据</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dt2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dt2">
            <el-table :data="detailItems" size="small" border>
              <el-table-column label="源批次" prop="sourceBatchCode" min-width="150" />
              <el-table-column label="目标批次" min-width="150">
                <template #default="scope">{{ scope.row.targetBatchCode || '-' }}</template>
              </el-table-column>
              <el-table-column label="数量" prop="qty" width="100" align="right" />
              <el-table-column label="新到期日期" width="110" align="center">
                <template #default="scope">{{ scope.row.newExpireDate || '-' }}</template>
              </el-table-column>
              <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
            </el-table>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 批次选择器 -->
    <batch-picker ref="batchPickerRef" title="选择批次" @confirm="onBatchPickerConfirm" />

    <!-- 业务操作说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="批次操作（调整单）业务操作说明" width="720px" append-to-body>
      <div class="status-help-content">
        <h4>一、调整单状态流转图</h4>
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
          <el-descriptions-item label="待执行">调整单已创建但尚未生效，此状态下可执行或取消；批次状态与数量暂未变更</el-descriptions-item>
          <el-descriptions-item label="已执行">调整已生效，批次状态与批次数量<strong>已联动变更</strong>并全程留痕；终态，不可取消、不可删除</el-descriptions-item>
          <el-descriptions-item label="已取消">调整单作废，批次无任何变更；仅待执行状态可取消</el-descriptions-item>
        </el-descriptions>

        <h4>三、调整类型说明（七种操作）</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">质量类操作</div>
              <div class="highlight-card-body"><strong>冻结/解冻/报废</strong>：直接变更批次质量状态，与批次台账联动——冻结后批次立即从出库可发列表排除，报废为不可逆终态</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">批次重组操作</div>
              <div class="highlight-card-body"><strong>拆批</strong>：一个批次拆成多个批次（如大袋分装小袋）；<strong>合批</strong>：多个同物料批次合并为一个批次，数量随之转移</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">召回与效期</div>
              <div class="highlight-card-body"><strong>召回</strong>：将批次标记为召回状态并锁定库存；<strong>效期调整</strong>：修正批次生产日期/到期日期（如复检后重新计算保质期），变更留痕</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-danger">
              <div class="highlight-card-title">执行规则</div>
              <div class="highlight-card-body">已执行的调整单<strong>不可取消、不可删除</strong>；执行时系统自动校验批次状态合法性（如报废批次不能再冻结），全部变更写入批次状态日志</div>
            </div>
          </el-col>
        </el-row>

        <h4>四、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新建调整单：</strong>选择调整类型（冻结/解冻/报废/拆批/合批/召回/效期调整），选择目标批次并填写原因
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>确认执行：</strong>核对明细无误后点「执行」，系统联动变更批次状态与数量（拆批/合批会生成新批次或合并数量）
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>生效留痕：</strong>执行后到「批次台账」查看批次最新状态，状态变更日志记录操作人与原因
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>误操作处理：</strong>待执行的调整单可「取消」作废；已执行的操作不可撤销，需新建反向调整单处理
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FoodAdjust">
import { listAdjust, getAdjust, addAdjust, executeAdjust, cancelAdjust, delAdjust } from "@/api/food/adjust"
import BatchPicker from '@/components/BatchPicker/index.vue'
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['a1', 'a2', 'dt1', 'dt2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_adjust_index')
const { food_adjust_type, food_adjust_status } = proxy.useDict("food_adjust_type", "food_adjust_status")

// 列显隐配置
const defaultColumns = {
  adjustCode: { label: '调整单号', visible: true },
  adjustType: { label: '调整类型', visible: true },
  sourceBatchCode: { label: '主批次', visible: true },
  reason: { label: '原因', visible: true },
  status: { label: '状态', visible: true },
  createBy: { label: '创建人', visible: true },
  createTime: { label: '创建时间', visible: true },
  executeTime: { label: '执行时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_adjust_columns')
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

const adjustList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const batchPickerRef = ref(null)
const pickerMode = ref("target")
const pickerItemIndex = ref(-1)

const detailOpen = ref(false)
const detailRow = ref(null)
const detailItems = ref([])

/** 需要明细行的类型：拆批4/合批5/效期调整7/召回6/解冻2（可批量） */
const needDetail = computed(() => ["4", "5", "7", "6", "2"].includes(form.value.adjustType))

const detailTip = computed(() => {
  const t = form.value.adjustType
  if (t === "4") return "拆批：源批次按明细行数量拆出多个子批次，子批次号自动生成、效期沿用源批次"
  if (t === "5") return "合批：多个源批次按明细行数量合并生成一个新批次，到期日期取最早"
  if (t === "7") return "效期调整：按明细行修正各批次到期日期"
  if (t === "6") return "召回：明细行中的批次将被强制冻结"
  return ""
})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, adjustCode: undefined, sourceBatchCode: undefined, adjustType: undefined, status: undefined },
  rules: {
    adjustType: [{ required: true, message: "调整类型不能为空", trigger: "change" }],
    reason: [{ required: true, message: "调整原因不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.adjustCode) count++
  if (queryParams.value.sourceBatchCode) count++
  if (queryParams.value.adjustType) count++
  if (queryParams.value.status) count++
  return count
})

function getList() {
  loading.value = true
  listAdjust(queryParams.value).then(response => {
    adjustList.value = response.rows
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

function adjustStatusClass(v) {
  return { "0": "amber", "1": "green", "2": "gray" }[v] || "gray"
}

// ---------------- 状态卡片统计 ----------------

const activeStatusTab = ref('all')
const statusCounts = ref({ all: 0 })
const showStatusHelp = ref(false)
const statusTabList = computed(() => food_adjust_status.value)
function statusTabClass(value) { const map = { '0': 'tab-draft', '1': 'tab-done', '2': 'tab-partial' }; return map[value] || '' }
function handleStatusTabClick(status) { activeStatusTab.value = status; queryParams.value.status = status === 'all' ? undefined : status; handleQuery() }
function loadStatusCounts() { listAdjust({ pageNum: 1, pageSize: 999 }).then(res => { const counts = { all: res.total, '0': 0, '1': 0, '2': 0 }; (res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ }); statusCounts.value = counts }).catch(() => {}) }

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.adjustCode = undefined
  queryParams.value.sourceBatchCode = undefined
  queryParams.value.adjustType = undefined
  queryParams.value.status = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.adjustId)
  multiple.value = !selection.length
}

function reset() {
  form.value = { adjustId: undefined, adjustType: "1", sourceBatchId: undefined, sourceBatchCode: undefined, materialId: undefined, reason: undefined, remark: undefined, itemList: [] }
  proxy.resetForm("adjustRef")
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新建批次调整单"
}

function onTypeChange() {
  form.value.itemList = []
}

function addItem() {
  form.value.itemList.push({ sourceBatchId: undefined, sourceBatchCode: undefined, qty: undefined, newExpireDate: undefined })
}

function removeItem(index) {
  form.value.itemList.splice(index, 1)
}

function openBatchPicker(mode, itemIndex = -1) {
  pickerMode.value = mode
  pickerItemIndex.value = itemIndex
  const current = mode === "target" ? form.value.sourceBatchId : (form.value.itemList[itemIndex] || {}).sourceBatchId
  batchPickerRef.value.open(current)
}

function clearTargetBatch() {
  form.value.sourceBatchId = undefined
  form.value.sourceBatchCode = undefined
}

function clearItemBatch(index) {
  const row = form.value.itemList[index]
  if (row) {
    row.sourceBatchId = undefined
    row.sourceBatchCode = undefined
  }
}

function onBatchPickerConfirm(batch) {
  if (pickerMode.value === "target") {
    form.value.sourceBatchId = batch.batchId
    form.value.sourceBatchCode = batch.batchCode
  } else {
    const row = form.value.itemList[pickerItemIndex.value]
    if (row) {
      row.sourceBatchId = batch.batchId
      row.sourceBatchCode = batch.batchCode
    }
  }
}

/** 取消按钮 */
function cancel()
{
  open.value = false
  reset()
}

function submitForm() {
  proxy.$refs["adjustRef"].validate(valid => {
    if (!valid) return
    const payload = { ...form.value }
    if (needDetail.value) {
      const items = (payload.itemList || []).filter(i => i.sourceBatchId && i.qty)
      if (!items.length) {
        proxy.$modal.msgError("请至少填写一条有效明细（批次+数量）")
        return
      }
      if (payload.adjustType === "7" && items.some(i => !i.newExpireDate)) {
        proxy.$modal.msgError("效期调整明细必须填写新到期日期")
        return
      }
      payload.itemList = items
    } else {
      if (!payload.sourceBatchId) {
        proxy.$modal.msgError("请选择目标批次")
        return
      }
      payload.itemList = []
    }
    addAdjust(payload).then(() => {
      proxy.$modal.msgSuccess("调整单创建成功，可在列表中执行")
      open.value = false
      getList()
      loadStatusCounts()
    })
  })
}

function doExecute(row, silent) {
  return executeAdjust(row.adjustId).then(() => {
    if (!silent) proxy.$modal.msgSuccess("执行成功")
    getList()
    loadStatusCounts()
  })
}

function handleExecute(row) {
  proxy.$modal.confirm("确认执行调整单【" + row.adjustCode + "】吗？执行后批次状态与数量将联动变更。").then(() => doExecute(row)).catch(() => {})
}

function handleCancel(row) {
  proxy.$modal.confirm("确认取消调整单【" + row.adjustCode + "】吗？").then(() => cancelAdjust(row.adjustId)).then(() => {
    proxy.$modal.msgSuccess("已取消")
    getList()
    loadStatusCounts()
  }).catch(() => {})
}

function handleDelete(row) {
  const deleteIds = row.adjustId || ids.value
  proxy.$modal.confirm("是否确认删除所选调整单？已执行的调整单不可删除。").then(() => delAdjust(deleteIds)).then(() => {
    getList()
    loadStatusCounts()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleDetail(row) {
  getAdjust(row.adjustId).then(res => {
    detailRow.value = res.data
    detailItems.value = (res.data && res.data.itemList) || []
    detailOpen.value = true
  })
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
