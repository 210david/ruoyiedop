<template>
  <div class="app-container wms-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>转移单号</label>
          <div class="control">
            <el-input v-model="queryParams.transferNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>垛号</label>
          <div class="control">
            <el-input v-model="queryParams.wipNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>损耗类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.lossType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in food_loss_type" :key="d.value" :label="d.label" :value="d.value" />
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['food:wipTransfer:add']">工序转移</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['food:wipTransfer:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="food_wip_transfer_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="transferList" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 60)" resizable align="center" />
          <el-table-column label="转移单号" prop="transferNo" key="transferNo" :width="colWidth('transferNo', 140)" resizable v-if="columns.transferNo.visible">
            <template #default="scope"><span class="mono-text">{{ scope.row.transferNo }}</span></template>
          </el-table-column>
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable show-overflow-tooltip v-if="columns.workOrderNo.visible" />
          <el-table-column label="垛号" prop="wipNo" key="wipNo" :width="colWidth('wipNo', 130)" resizable v-if="columns.wipNo.visible">
            <template #default="scope"><span class="mono-text">{{ scope.row.wipNo }}</span></template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchCode" key="batchCode" :width="colWidth('batchCode', 140)" resizable show-overflow-tooltip v-if="columns.batchCode.visible">
            <template #default="scope"><span v-if="scope.row.batchCode">{{ scope.row.batchCode }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 110)" resizable show-overflow-tooltip v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 140)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="转出工序" key="fromProcess" :width="colWidth('fromProcess', 130)" resizable show-overflow-tooltip v-if="columns.fromProcess.visible">
            <template #default="scope"><span v-if="scope.row.fromStepSeq" class="text-muted">{{ scope.row.fromStepSeq }} {{ scope.row.fromProcessName }}</span><span v-else class="text-muted">建垛起点</span></template>
          </el-table-column>
          <el-table-column label="转入工序" key="toProcess" :width="colWidth('toProcess', 130)" resizable show-overflow-tooltip v-if="columns.toProcess.visible">
            <template #default="scope"><span class="badge blue"><span class="dot"></span>{{ scope.row.toStepSeq }} {{ scope.row.toProcessName }}</span></template>
          </el-table-column>
          <el-table-column label="转移数量" prop="transferQty" key="transferQty" :width="colWidth('transferQty', 100)" resizable align="right" v-if="columns.transferQty.visible">
            <template #default="scope"><span class="num-text">{{ scope.row.transferQty }}</span></template>
          </el-table-column>
          <el-table-column label="损耗数量" prop="lossQty" key="lossQty" :width="colWidth('lossQty', 100)" resizable align="right" v-if="columns.lossQty.visible">
            <template #default="scope"><span v-if="scope.row.lossQty > 0" class="badge red"><span class="dot"></span>{{ scope.row.lossQty }}</span><span v-else class="text-muted">0</span></template>
          </el-table-column>
          <el-table-column label="损耗率" prop="lossRate" key="lossRate" :width="colWidth('lossRate', 90)" resizable align="center" v-if="columns.lossRate.visible">
            <template #default="scope"><span :class="scope.row.lossRate > 0 ? 'text-danger' : 'text-muted'">{{ scope.row.lossRate }}%</span></template>
          </el-table-column>
          <el-table-column label="损耗类型" prop="lossType" key="lossType" :width="colWidth('lossType', 110)" resizable align="center" v-if="columns.lossType.visible">
            <template #default="scope"><span v-if="scope.row.lossType" class="badge violet"><span class="dot"></span>{{ dictLabel(food_loss_type, scope.row.lossType) }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="预计出柜" prop="planStepEnd" key="planStepEnd" :width="colWidth('planStepEnd', 160)" resizable align="center" v-if="columns.planStepEnd.visible">
            <template #default="scope"><span v-if="scope.row.planStepEnd">{{ parseTime(scope.row.planStepEnd, '{y}-{m}-{d} {h}:{i}') }}</span><span v-else class="text-muted">—</span></template>
          </el-table-column>
          <el-table-column label="转移时间" prop="transferTime" key="transferTime" :width="colWidth('transferTime', 160)" resizable align="center" v-if="columns.transferTime.visible">
            <template #default="scope">{{ parseTime(scope.row.transferTime, '{y}-{m}-{d} {h}:{i}') }}</template>
          </el-table-column>
          <el-table-column label="转移人" prop="transferBy" key="transferBy" :width="colWidth('transferBy', 90)" resizable align="center" v-if="columns.transferBy.visible" />
          <el-table-column label="操作" :width="colWidth('操作', 90)" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 工序转移对话框 -->
    <el-dialog v-model="open" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></div>
          <span class="rd-detail-header-title">工序转移</span>
        </div>
      </template>
      <el-form ref="transferRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('t1')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>在制垛</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.t1">
              <el-form-item label="在制垛" prop="wipId">
                <el-input :model-value="form.wipNo" readonly placeholder="点击选择在制垛" @click="openWipPicker">
                  <template v-if="form.wipId" #append><el-button icon="CircleClose" @click.stop="clearWip" /></template>
                  <template v-else #append><el-button icon="Search" @click="openWipPicker" /></template>
                </el-input>
                <div class="form-tip">仅可选择「在制」状态的垛；工序不可回退，只能转移到后续工序</div>
              </el-form-item>
              <el-form-item label="垛信息" v-if="form.wipId">
                <div class="wip-brief">
                  <div class="wip-brief-item"><span class="rd-label">工单</span><span>{{ form.workOrderNo || '—' }}</span></div>
                  <div class="wip-brief-item"><span class="rd-label">批次</span><span>{{ form.batchCode || '—' }}</span></div>
                  <div class="wip-brief-item"><span class="rd-label">物料</span><span>{{ [form.materialCode, form.materialName].filter(Boolean).join(' - ') || '—' }}</span></div>
                  <div class="wip-brief-item"><span class="rd-label">当前工序</span><span class="badge blue"><span class="dot"></span>{{ form.fromStepSeq }} {{ form.fromProcessName }}</span></div>
                  <div class="wip-brief-item"><span class="rd-label">现存数量</span><span class="num-text">{{ wipRow ? wipRow.qty : '—' }} {{ wipRow && wipRow.qtyUnit ? unitLabel(wipRow.qtyUnit) : '' }}</span></div>
                </div>
              </el-form-item>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('t2')">
              <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg></span>转移信息</div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.t2">
              <el-form-item label="转入工序" prop="toStepSeq">
                <el-select v-model="form.toStepSeq" placeholder="请选择目标工序" style="width: 100%" :disabled="targetSteps.length === 0">
                  <el-option v-for="s in targetSteps" :key="s.stepSeq" :label="s.stepSeq + ' - ' + s.processName + (s.stdDays ? `（标准${s.stdDays}天）` : '')" :value="s.stepSeq" />
                </el-select>
                <div class="form-tip" v-if="form.wipId && targetSteps.length === 0">该垛已在最后一道工序，或未挂工艺路线，无法转移</div>
              </el-form-item>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="转移数量" prop="transferQty">
                    <el-input-number v-model="form.transferQty" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 100%" @change="onQtyChange" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="损耗数量" prop="lossQty">
                    <el-input-number v-model="form.lossQty" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" @change="onQtyChange" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="损耗类型" prop="lossType">
                <el-select v-model="form.lossType" placeholder="有损耗时必选" clearable style="width: 100%">
                  <el-option v-for="d in food_loss_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
                <div class="form-tip">损耗率超工序阈值时将推送消息告警（不阻断转移）；损耗率 = 损耗 ÷（转移+损耗）× 100</div>
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入内容" />
              </el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确认转移</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailOpen" width="760px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="6" cy="19" r="3"/><path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"/><circle cx="18" cy="5" r="3"/></svg></div>
          <span class="rd-detail-header-title">转移单详情</span>
          <div class="rd-detail-header-sub" v-if="detail.transferNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ detail.transferNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/></svg></span>流转信息</div></div>
          <div class="rd-card-body" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ detail.workOrderNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">垛号</span><div class="rd-value mono-text">{{ detail.wipNo || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ detail.batchCode || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">物料</span><div class="rd-value">{{ [detail.materialCode, detail.materialName].filter(Boolean).join(' - ') || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">转出工序</span><div class="rd-value">{{ detail.fromStepSeq ? detail.fromStepSeq + ' ' + detail.fromProcessName : '建垛起点' }}</div></div>
              <div class="rd-item"><span class="rd-label">转入工序</span><div class="rd-value">{{ detail.toStepSeq ? detail.toStepSeq + ' ' + detail.toProcessName : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">转移数量</span><div class="rd-value">{{ detail.transferQty != null ? detail.transferQty : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">损耗数量</span><div class="rd-value">{{ detail.lossQty != null ? detail.lossQty : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">损耗率</span><div class="rd-value">{{ detail.lossRate != null ? detail.lossRate + '%' : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">损耗类型</span><div class="rd-value">{{ detail.lossType ? dictLabel(food_loss_type, detail.lossType) : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">标准时长</span><div class="rd-value">{{ detail.stdDays != null ? detail.stdDays + ' 天' : '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">预计出柜</span><div class="rd-value">{{ parseTime(detail.planStepEnd, '{y}-{m}-{d} {h}:{i}') || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">转移时间</span><div class="rd-value">{{ parseTime(detail.transferTime, '{y}-{m}-{d} {h}:{i}') || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">转移人</span><div class="rd-value">{{ detail.transferBy || '—' }}</div></div>
              <div class="rd-item"><span class="rd-label">备注</span><div class="rd-value">{{ detail.remark || '—' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- 在制垛选择器 -->
    <el-dialog v-model="wipPickerOpen" width="936px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">选择在制垛（仅在制状态）</span></div></template>
      <div class="material-picker"><div class="material-picker-search"><el-input v-model="wipPickerQuery.wipNo" placeholder="垛号" clearable size="small" style="width: 170px" @keyup.enter="handleWipPickerQuery"><template #prefix><el-icon><Search /></el-icon></template></el-input><el-input v-model="wipPickerQuery.workOrderNo" placeholder="工单号" clearable size="small" style="width: 170px; margin-left: 8px" @keyup.enter="handleWipPickerQuery" /><el-input v-model="wipPickerQuery.materialName" placeholder="物料名称" clearable size="small" style="width: 170px; margin-left: 8px" @keyup.enter="handleWipPickerQuery" /><el-button type="primary" plain icon="Search" size="small" style="margin-left: 8px" @click="handleWipPickerQuery">查询</el-button><el-button icon="RefreshLeft" size="small" @click="resetWipPickerQuery">重置</el-button></div><div class="material-picker-table"><el-table v-loading="wipPickerLoading" :data="wipPickerList" highlight-current-row @row-click="onWipRowClick" @row-dblclick="onWipRowDblClick" height="360" size="small" border><el-table-column width="45" align="center"><template #default="{ row }"><el-radio :model-value="wipPickerSelectedId" :value="row.wipId" @click.stop="onWipRowClick(row)"><span /></el-radio></template></el-table-column><el-table-column label="垛号" prop="wipNo" width="140" show-overflow-tooltip /><el-table-column label="工单号" prop="workOrderNo" width="140" show-overflow-tooltip /><el-table-column label="物料名称" prop="materialName" min-width="150" show-overflow-tooltip /><el-table-column label="当前工序" min-width="130" show-overflow-tooltip><template #default="scope"><span v-if="scope.row.currentStepSeq">{{ scope.row.currentStepSeq }} {{ scope.row.currentProcessName }}</span><span v-else class="text-muted">—</span></template></el-table-column><el-table-column label="现存数量" prop="qty" width="100" align="right" /><el-table-column label="入柜时间" width="150" align="center"><template #default="scope">{{ parseTime(scope.row.stepStartTime, '{y}-{m}-{d} {h}:{i}') }}</template></el-table-column></el-table></div><div class="material-picker-pager"><el-pagination v-model:current-page="wipPickerQuery.pageNum" v-model:page-size="wipPickerQuery.pageSize" :total="wipPickerTotal" layout="total, prev, pager, next" small @current-change="getWipPickerList" /></div></div>
      <template #footer><el-button @click="wipPickerOpen = false">取 消</el-button><el-button type="primary" @click="handleWipPickerConfirm" :disabled="!wipPickerSelectedId">确 定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="FoodWipTransfer">
import { listTransfer, getTransfer, addTransfer } from "@/api/food/wipTransfer"
import { listWip } from "@/api/food/wip"
import { getRoute } from "@/api/mms/route"
import { useDetailCard } from '@/composables/useDetailCard'
import { useColumnResize } from '@/composables/useColumnResize'
const { collapsedCards, toggleCard } = useDetailCard(['t1', 't2'])

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('food_wip_transfer_index')
const { food_loss_type, wms_unit } = proxy.useDict("food_loss_type", "wms_unit")

// 列显隐配置
const defaultColumns = {
  transferNo: { label: '转移单号', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  wipNo: { label: '垛号', visible: true },
  batchCode: { label: '批次号', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  fromProcess: { label: '转出工序', visible: true },
  toProcess: { label: '转入工序', visible: true },
  transferQty: { label: '转移数量', visible: true },
  lossQty: { label: '损耗数量', visible: true },
  lossRate: { label: '损耗率', visible: true },
  lossType: { label: '损耗类型', visible: true },
  planStepEnd: { label: '预计出柜', visible: true },
  transferTime: { label: '转移时间', visible: true },
  transferBy: { label: '转移人', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('food_wip_transfer_columns')
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

const transferList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const detail = ref({})
const wipRow = ref(null)
const routeSteps = ref([])
const targetSteps = computed(() => routeSteps.value.filter(s => form.value.fromStepSeq == null || s.stepSeq > form.value.fromStepSeq))

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, transferNo: undefined, wipNo: undefined, workOrderNo: undefined, lossType: undefined },
  rules: {
    wipId: [{ required: true, message: "在制垛不能为空", trigger: "change" }],
    toStepSeq: [{ required: true, message: "转入工序不能为空", trigger: "change" }],
    transferQty: [{ required: true, message: "转移数量不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 在制垛选择器
const wipPickerOpen = ref(false); const wipPickerLoading = ref(false); const wipPickerList = ref([]); const wipPickerTotal = ref(0); const wipPickerSelectedId = ref(null); const wipPickerSelectedRow = ref(null);
const wipPickerQuery = reactive({ pageNum: 1, pageSize: 10, wipNo: undefined, workOrderNo: undefined, materialName: undefined, wipStatus: '0' });

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.transferNo) count++
  if (queryParams.value.wipNo) count++
  if (queryParams.value.workOrderNo) count++
  if (queryParams.value.lossType) count++
  return count
})

function getList() {
  loading.value = true
  listTransfer(queryParams.value).then(response => {
    transferList.value = response.rows
    total.value = response.total
    loading.value = false
    applySavedWidths()
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.transferNo = undefined
  queryParams.value.wipNo = undefined
  queryParams.value.workOrderNo = undefined
  queryParams.value.lossType = undefined
  handleQuery()
}

function reset() {
  form.value = { wipId: null, wipNo: undefined, workOrderNo: undefined, batchCode: undefined, materialCode: undefined, materialName: undefined, fromStepSeq: null, fromProcessName: undefined, toStepSeq: undefined, transferQty: undefined, lossQty: 0, lossType: undefined, remark: undefined }
  wipRow.value = null
  routeSteps.value = []
  proxy.resetForm("transferRef")
}

function handleAdd() {
  reset()
  open.value = true
}

function handleDetail(row) {
  getTransfer(row.transferId).then(response => {
    detail.value = response.data
    detailOpen.value = true
  })
}

function cancel() {
  open.value = false
  reset()
}

function onQtyChange() {
  if (wipRow.value && form.value.transferQty != null && form.value.lossQty != null) {
    const totalOut = Number(form.value.transferQty) + Number(form.value.lossQty)
    const remain = Number(wipRow.value.qty)
    if (totalOut > remain) {
      form.value.lossQty = Math.max(0, remain - Number(form.value.transferQty))
      proxy.$modal.msgWarning("转移数量+损耗数量不能超过垛现存数量 " + remain)
    }
  }
}

function submitForm() {
  proxy.$refs["transferRef"].validate(valid => {
    if (valid) {
      if (form.value.lossQty > 0 && !form.value.lossType) {
        proxy.$modal.msgWarning("有损耗时必须选择损耗类型")
        return
      }
      addTransfer(form.value).then(() => {
        proxy.$modal.msgSuccess("转移成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleExport() {
  proxy.download("food/wip/transfer/export", { ...queryParams.value }, `wip_transfer_${new Date().getTime()}.xlsx`)
}

/** 在制垛选择 */
function openWipPicker() {
  wipPickerOpen.value = true; wipPickerSelectedId.value = null; wipPickerSelectedRow.value = null; wipPickerQuery.pageNum = 1; wipPickerQuery.wipNo = undefined; wipPickerQuery.workOrderNo = undefined; wipPickerQuery.materialName = undefined; getWipPickerList();
}
function getWipPickerList() { wipPickerLoading.value = true; listWip(wipPickerQuery).then(res => { wipPickerList.value = res.rows; wipPickerTotal.value = res.total; wipPickerLoading.value = false; }).catch(() => { wipPickerLoading.value = false; }); }
function handleWipPickerQuery() { wipPickerQuery.pageNum = 1; getWipPickerList(); }
function resetWipPickerQuery() { wipPickerQuery.wipNo = undefined; wipPickerQuery.workOrderNo = undefined; wipPickerQuery.materialName = undefined; handleWipPickerQuery(); }
function onWipRowClick(row) { wipPickerSelectedId.value = row.wipId; wipPickerSelectedRow.value = row; }
function onWipRowDblClick(row) { onWipRowClick(row); handleWipPickerConfirm(); }
function handleWipPickerConfirm() {
  if (!wipPickerSelectedId.value) { proxy.$modal.msgWarning('请先选择在制垛'); return; }
  const row = wipPickerSelectedRow.value;
  wipRow.value = row;
  form.value.wipId = row.wipId;
  form.value.wipNo = row.wipNo;
  form.value.workOrderNo = row.workOrderNo;
  form.value.batchCode = row.batchCode;
  form.value.materialCode = row.materialCode;
  form.value.materialName = row.materialName;
  form.value.fromStepSeq = row.currentStepSeq;
  form.value.fromProcessName = row.currentProcessName;
  form.value.toStepSeq = undefined;
  form.value.transferQty = Number(row.qty);
  form.value.lossQty = 0;
  loadRouteSteps(row.routeId);
  wipPickerOpen.value = false;
}
function clearWip() {
  form.value.wipId = null; form.value.wipNo = undefined; form.value.workOrderNo = undefined; form.value.batchCode = undefined;
  form.value.materialCode = undefined; form.value.materialName = undefined; form.value.fromStepSeq = null; form.value.fromProcessName = undefined;
  form.value.toStepSeq = undefined; form.value.transferQty = undefined;
  wipRow.value = null;
  routeSteps.value = [];
}

/** 加载路线工序列表并过滤出可转移的后续工序 */
function loadRouteSteps(routeId) {
  routeSteps.value = []
  if (!routeId) return
  getRoute(routeId).then(res => {
    const list = (res.data && res.data.processList) || []
    routeSteps.value = list.map(p => ({ stepSeq: p.stepSeq, processName: p.processName, stdDays: p.stdDays }))
  })
}

/** 工具函数 */
function dictLabel(options, v) {
  if (v == null || v === "") return "-"
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
function unitLabel(v) {
  if (v == null || v === '') return ''
  const item = (wms_unit.value || []).find(d => d.value == v)
  return item ? item.label : v
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.form-tip { font-size: 12px; color: #909399; line-height: 1.6; }
.wip-brief { display: flex; flex-wrap: wrap; gap: 8px 24px; background: #f8f9fb; border-radius: 6px; padding: 10px 14px; width: 100%; }
.wip-brief-item { display: flex; align-items: center; gap: 6px; font-size: 13px; }
.wip-brief-item .rd-label { color: #909399; }
</style>
