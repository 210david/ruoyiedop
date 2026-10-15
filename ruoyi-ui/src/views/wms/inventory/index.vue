<template>
  <div class="app-container wms-inventory-page">
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
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料名称</label>
          <div class="control">
            <el-input v-model="queryParams.materialName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>仓库/库区/库位</label>
          <div class="control">
            <el-input v-model="queryParams.locationKeyword" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>内部批次</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>规格型号</label>
          <div class="control">
            <el-input v-model="queryParams.specModel" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>更新时间</label>
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
      <div class="field" v-show="showAdvanced" style="margin-top: 12px;">
        <label>零库存</label>
        <div class="control">
          <el-switch v-model="showZero" active-text="显示零库存记录" inactive-text="默认隐藏" @change="handleQuery" />
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-radio-group v-model="viewMode" size="default" @change="handleViewChange">
            <el-radio-button value="detail">批次明细</el-radio-button>
            <el-radio-button value="summary">物料汇总</el-radio-button>
          </el-radio-group>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:inventory:remove']" v-if="viewMode === 'detail'">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['wms:inventory:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
          <AiAssistButton button-text="AI库存分析" button-type="success" :button-plain="false" :api="aiStockAnalysis" @result="d => onAiText(d, '库存分析报告')" v-if="viewMode === 'detail'" />
          <AiAssistButton button-text="呆滞识别" button-type="warning" :button-plain="false" :api="aiStaleInventory" @result="onStaleList" v-if="viewMode === 'detail'" />
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="wms_inventory_columns" />
        </div>
      </div>

      <!-- AI 结果面板：独立于工具栏 flex 行，避免挤压按钮 -->
      <AiResultPanel :visible="aiPanelVisible" :content="aiPanelText" :latency-ms="aiLatency" @close="aiPanelVisible = false" />

      <!-- Table -->
      <div class="table-wrap">
        <!-- 明细视图：批次明细 -->
        <el-table ref="tableRef" v-if="viewMode === 'detail'" v-loading="loading" :data="list" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 180)" resizable sortable="custom" v-if="columns.materialCode.visible">
            <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" key="materialName" :min-width="colWidth('materialName', 240)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :min-width="colWidth('specModel', 200)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible">
            <template #default="scope"><span class="badge blue">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="仓库/库区/库位" prop="warehouseName" key="warehouseName" :width="colWidth('warehouseName', 240)" resizable show-overflow-tooltip v-if="columns.warehouseName.visible">
            <template #default="scope">
              {{ scope.row.warehouseName || '' }}<span v-if="scope.row.areaName">/{{ scope.row.areaName }}</span><span v-if="scope.row.locationName">/{{ scope.row.locationName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="内部批次" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 130)" resizable show-overflow-tooltip v-if="columns.batchNo.visible" />
          <el-table-column label="供应商批次" prop="extBatchNo" key="extBatchNo" :width="colWidth('extBatchNo', 130)" resizable show-overflow-tooltip v-if="columns.extBatchNo.visible">
            <template #default="scope">{{ scope.row.extBatchNo || '-' }}</template>
          </el-table-column>
          <el-table-column label="批次状态" prop="batchStatus" key="batchStatus" :width="colWidth('batchStatus', 100)" resizable align="center" v-if="columns.batchStatus.visible">
            <template #default="scope">
              <span v-if="scope.row.batchStatus" class="badge" :class="batchStatusClass(scope.row.batchStatus)"><span class="dot"></span>{{ dictLabel(food_batch_status, scope.row.batchStatus) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="生产日期" prop="productionDate" key="productionDate" :width="colWidth('productionDate', 130)" resizable align="center" v-if="columns.productionDate.visible" />
          <el-table-column label="有效期" prop="expiryDate" key="expiryDate" :width="colWidth('expiryDate', 130)" resizable align="center" v-if="columns.expiryDate.visible" />
          <el-table-column label="可用数量" prop="qty" key="qty" :width="colWidth('qty', 110)" resizable align="center" class-name="col-num" v-if="columns.qty.visible" />
          <el-table-column label="锁定数量" prop="lockQty" key="lockQty" :width="colWidth('lockQty', 110)" resizable align="center" class-name="col-num" v-if="columns.lockQty.visible" />
          <el-table-column label="更新时间" prop="updateTime" key="updateTime" :width="colWidth('updateTime', 180)" resizable align="center" sortable="custom" v-if="columns.updateTime.visible" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:inventory:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 汇总视图：物料+仓库汇总，展开看批次明细 -->
        <el-table v-else ref="tableRefS" v-loading="loading" :data="summaryList" border class="app-table" row-key="summaryKey" @expand-change="handleSummaryExpand" @header-dragend="onHeaderDragEndS">
          <el-table-column type="expand" width="55" align="center">
            <template #default="scope">
              <div style="padding: 8px 16px 12px;">
                <el-table :data="scope.row.detailList || []" size="small" border v-loading="scope.row.detailLoading" @header-dragend="onHeaderDragEndD">
                  <el-table-column label="内部批次" prop="batchNo" :min-width="colWidthD('batchNo', 140)" resizable />
                  <el-table-column label="供应商批次" prop="extBatchNo" :min-width="colWidthD('extBatchNo', 130)" resizable>
                    <template #default="s">{{ s.row.extBatchNo || '-' }}</template>
                  </el-table-column>
                  <el-table-column label="批次状态" prop="batchStatus" :width="colWidthD('batchStatus', 100)" resizable align="center">
                    <template #default="s">
                      <span v-if="s.row.batchStatus" class="badge" :class="batchStatusClass(s.row.batchStatus)"><span class="dot"></span>{{ dictLabel(food_batch_status, s.row.batchStatus) }}</span>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="仓库/库区/库位" prop="warehouseFullName" :min-width="colWidthD('warehouseFullName', 210)" resizable>
                    <template #default="s">{{ s.row.warehouseName || '' }}<span v-if="s.row.areaName">/{{ s.row.areaName }}</span><span v-if="s.row.locationName">/{{ s.row.locationName }}</span></template>
                  </el-table-column>
                  <el-table-column label="可用数量" prop="qty" :width="colWidthD('qty', 100)" resizable align="center" />
                  <el-table-column label="锁定数量" prop="lockQty" :width="colWidthD('lockQty', 100)" resizable align="center" />
                  <el-table-column label="有效期" prop="expiryDate" :width="colWidthD('expiryDate', 120)" resizable align="center" />
                  <el-table-column label="更新时间" prop="updateTime" :min-width="colWidthD('updateTime', 170)" resizable align="center" />
                </el-table>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="序号" type="index" :width="colWidthS('序号', 70)" resizable align="center" />
          <el-table-column label="物料编码" prop="materialCode" :width="colWidthS('materialCode', 200)" resizable>
            <template #default="scope"><span class="col-mono">{{ scope.row.materialCode }}</span></template>
          </el-table-column>
          <el-table-column label="物料名称" prop="materialName" :min-width="colWidthS('materialName', 260)" resizable show-overflow-tooltip />
          <el-table-column label="规格型号" prop="specModel" :min-width="colWidthS('specModel', 220)" resizable show-overflow-tooltip />
          <el-table-column label="单位" :width="colWidthS('unit', 90)" resizable align="center">
            <template #default="scope"><span class="badge blue">{{ unitLabel(scope.row.unit) }}</span></template>
          </el-table-column>
          <el-table-column label="仓库" prop="warehouseName" :min-width="colWidthS('warehouseName', 200)" resizable show-overflow-tooltip />
          <el-table-column label="可用数量合计" prop="totalQty" :width="colWidthS('totalQty', 140)" resizable align="center" class-name="col-num" />
          <el-table-column label="锁定数量合计" prop="totalLockQty" :width="colWidthS('totalLockQty', 140)" resizable align="center" class-name="col-num" />
          <el-table-column label="批次数" prop="batchCount" :width="colWidthS('batchCount', 100)" resizable align="center" class-name="col-num" />
          <el-table-column label="最早有效期" prop="minExpiryDate" :width="colWidthS('minExpiryDate', 130)" resizable align="center">
            <template #default="scope"><span v-if="scope.row.minExpiryDate">{{ scope.row.minExpiryDate }}</span><span v-else>-</span></template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="WmsInventory">
import { listInventory, listInventorySummary, delInventory } from '@/api/wms/inventory'
import { aiStockAnalysis, aiStaleInventory } from '@/api/ai/manage'
import AiAssistButton from '@/components/AiAssist/AiAssistButton.vue'
import AiResultPanel from '@/components/AiAssist/AiResultPanel.vue'
import { fetchAllPages, downloadXlsx } from '@/utils/csvExport'
import { useColumnResize } from '@/composables/useColumnResize'
import { ArrowDown, Delete, Download, RefreshLeft, Search, Filter } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_inventory_index')
const { colWidth: colWidthS, onHeaderDragEnd: onHeaderDragEndS, tableRef: tableRefS, applySavedWidths: applySavedWidthsS } = useColumnResize('wms_inventory_summary')
// 汇总展开的下钻批次明细小表：独立持久化（多行展开共享同一组列宽，不绑 ref 仅存取宽度）
const { colWidth: colWidthD, onHeaderDragEnd: onHeaderDragEndD } = useColumnResize('wms_inventory_summary_detail')
const { wms_unit, food_batch_status } = proxy.useDict('wms_unit', 'food_batch_status')
const list = ref([]); const loading = ref(true); const showSearch = ref(true); const showAdvanced = ref(false); const total = ref(0); const ids = ref([]); const multiple = ref(true)
const dateRange = ref([])
const viewMode = ref('detail')
const showZero = ref(false)
const summaryList = ref([])

const defaultColumns = {
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  warehouseName: { label: '仓库/库区/库位', visible: true },
  batchNo: { label: '内部批次', visible: true },
  extBatchNo: { label: '供应商批次', visible: true },
  batchStatus: { label: '批次状态', visible: true },
  productionDate: { label: '生产日期', visible: true },
  expiryDate: { label: '有效期', visible: true },
  qty: { label: '可用数量', visible: true },
  lockQty: { label: '锁定数量', visible: true },
  updateTime: { label: '更新时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('wms_inventory_columns')
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

const data = reactive({ queryParams: { pageNum: 1, pageSize: 10, materialCode: undefined, materialName: undefined, locationKeyword: undefined, batchNo: undefined, specModel: undefined, params: {} } })
const { queryParams } = toRefs(data)

/** 字典标签 */
function dictLabel(options, v) {
  if (v == null || v === '') return '-'
  const item = (options || []).find(d => d.value == v)
  return item ? item.label : v
}
/** 批次状态徽章样式 */
function batchStatusClass(v) {
  return { '0': 'amber', '1': 'green', '2': 'blue', '3': 'red' }[v] || 'gray'
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.materialCode) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.locationKeyword) count++
  if (queryParams.value.batchNo) count++
  if (queryParams.value.specModel) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function getList() {
  loading.value = true
  const query = proxy.addDateRange(queryParams.value, dateRange.value, 'UpdateTime')
  // 零库存策略：默认隐藏零库存批次行，仅流水与批次台账保留痕迹（食品行业追溯要求：不删数据，只收视图）
  if (!showZero.value) query.params.hideZero = 'true'
  listInventory(query).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
}

/** 汇总视图：按物料+仓库聚合 */
function getSummary() {
  loading.value = true
  const query = proxy.addDateRange(queryParams.value, dateRange.value, 'UpdateTime')
  if (!showZero.value) query.params.hideZero = 'true'
  listInventorySummary(query).then(res => {
    summaryList.value = (res.rows || []).map(r => ({ ...r, summaryKey: r.materialId + '-' + r.warehouseId, detailList: undefined, detailLoading: false }))
    total.value = res.total; loading.value = false; applySavedWidthsS()
  })
}

/** 汇总行展开：加载该物料+仓库下的批次明细 */
function handleSummaryExpand(row, expanded) {
  if (!expanded.includes(row)) return
  if (row.detailList) return
  row.detailLoading = true
  listInventory({ pageNum: 1, pageSize: 200, materialId: row.materialId, warehouseId: row.warehouseId, params: {} }).then(res => {
    row.detailList = res.rows || []
    row.detailLoading = false
  }).catch(() => { row.detailLoading = false })
}

function handleViewChange() {
  if (viewMode.value === 'summary') getSummary(); else getList()
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; viewMode.value === 'summary' ? getSummary() : getList() }
function resetQuery() { dateRange.value = []; queryParams.value.materialCode = undefined; queryParams.value.materialName = undefined; queryParams.value.locationKeyword = undefined; queryParams.value.batchNo = undefined; queryParams.value.specModel = undefined; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) {
  if (column.prop && column.order) {
    queryParams.value.params.orderByColumn = column.prop
    queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.params.orderByColumn = undefined
    queryParams.value.params.isAsc = undefined
  }
  getList()
}
function unitLabel(unit) { const item = wms_unit.value.find(d => d.value == unit); return item ? item.label : '-' }
/** 导出库存（与列表口径一致：含筛选条件，导出全部页数据；仓库/库区/库位为合并列） */
async function handleExport() {
  const query = proxy.addDateRange(queryParams.value, dateRange.value, 'UpdateTime')
  if (!showZero.value) query.params.hideZero = 'true'
  if (viewMode.value === 'summary') {
    const rows = await fetchAllPages(listInventorySummary, query)
    if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
    const headers = ['物料编码', '物料名称', '规格型号', '单位', '仓库', '可用数量合计', '锁定数量合计', '批次数', '最早有效期']
    downloadXlsx(`inventory_summary_${new Date().getTime()}`, headers, rows.map(i => [
      i.materialCode || '', i.materialName || '', i.specModel || '', unitLabel(i.unit), i.warehouseName || '',
      i.totalQty != null ? i.totalQty : '', i.totalLockQty != null ? i.totalLockQty : '', i.batchCount != null ? i.batchCount : '', i.minExpiryDate || ''
    ]))
    return
  }
  let rows = await fetchAllPages(listInventory, query)
  if (ids.value && ids.value.length) { rows = rows.filter(i => ids.value.includes(i.inventoryId)) }
  if (!rows.length) { proxy.$modal.msgWarning('当前筛选下无数据可导出'); return }
  const headers = ['物料编码', '物料名称', '规格型号', '单位', '仓库/库区/库位', '内部批次', '供应商批次', '批次状态', '生产日期', '有效期', '可用数量', '锁定数量', '更新时间']
  downloadXlsx(`inventory_${new Date().getTime()}`, headers, rows.map(i => [
    i.materialCode || '', i.materialName || '', i.specModel || '', unitLabel(i.unit),
    [i.warehouseName, i.areaName, i.locationName].filter(Boolean).join('/'),
    i.batchNo || '', i.extBatchNo || '', dictLabel(food_batch_status, i.batchStatus) || '', i.productionDate || '', i.expiryDate || '',
    i.qty != null ? i.qty : '', i.lockQty != null ? i.lockQty : '', i.updateTime || ''
  ]))
}
function handleSelectionChange(sel) { ids.value = sel.map(i => i.inventoryId); multiple.value = !sel.length }
function handleDelete(row) { const inventoryIds = row.inventoryId || ids.value; proxy.$modal.confirm('确认删除选中的库存记录？').then(() => delInventory(inventoryIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
getList()

// ===== AI辅助（外挂式增量） =====
const aiPanelVisible = ref(false)
const aiPanelText = ref('')
const aiLatency = ref(0)
function onAiText(d, title) {
  aiPanelText.value = (title ? '【' + title + '】\n\n' : '') + (d.content || d.error || 'AI未返回内容')
  aiLatency.value = d.latencyMs || 0
  aiPanelVisible.value = true
}

function onStaleList(d) {
  const list = d.staleList || []
  if (!list.length) { onAiText({ content: '未发现呆滞库存（库龄≥' + (d.staleDays || 90) + '天的库存为0项）', latencyMs: 0 }, '呆滞库存识别'); return }
  const lines = ['共发现 ' + (d.staleCount || list.length) + ' 项呆滞库存（库龄≥' + (d.staleDays || 90) + '天）：', '']
  list.slice(0, 30).forEach((x, i) => {
    lines.push((i + 1) + '. ' + (x.materialName || '-') + ' | ' + (x.warehouseName || '-') + ' | 数量 ' + x.qty + ' | 库龄 ' + x.ageDays + '天 | 生产 ' + (x.productionDate || '-'))
  })
  if (list.length > 30) lines.push('…其余 ' + (list.length - 30) + ' 项略')
  aiPanelText.value = lines.join('\n'); aiLatency.value = 0; aiPanelVisible.value = true
}
</script>

<style scoped>
/* ===== Design Tokens ===== */
.wms-inventory-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.wms-inventory-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.wms-inventory-page .filter-card { padding:14px 20px 16px; }
.wms-inventory-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.wms-inventory-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.wms-inventory-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.wms-inventory-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.wms-inventory-page .filter-card .adv-link:hover { color:var(--brand-600); }
.wms-inventory-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.wms-inventory-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.wms-inventory-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.wms-inventory-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.wms-inventory-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.wms-inventory-page .filter-card .filter-buttons { display:flex; gap:8px; }
.wms-inventory-page .field { display:flex; flex-direction:column; gap:6px; }
.wms-inventory-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.wms-inventory-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.wms-inventory-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.wms-inventory-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.wms-inventory-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.wms-inventory-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.wms-inventory-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.wms-inventory-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.wms-inventory-page .field .control :deep(.el-select) { width:100%; }
.wms-inventory-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.wms-inventory-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.wms-inventory-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.wms-inventory-page .field .control :deep(.el-date-editor) { width:100%; }
.wms-inventory-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.wms-inventory-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.wms-inventory-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.wms-inventory-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.wms-inventory-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.wms-inventory-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.wms-inventory-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.wms-inventory-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.wms-inventory-page .btn-soft .el-icon { font-size:14px; }
.wms-inventory-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.wms-inventory-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.wms-inventory-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.wms-inventory-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.wms-inventory-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.wms-inventory-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.wms-inventory-page .table-wrap { overflow-x:auto; }
.wms-inventory-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.wms-inventory-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.wms-inventory-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.wms-inventory-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.wms-inventory-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.wms-inventory-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.wms-inventory-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.wms-inventory-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.wms-inventory-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.wms-inventory-page .app-table .col-mono { font-family:ui-monospace,"JetBrains Mono","SF Mono",Menlo,monospace; font-size:14px; color:var(--ink-700); letter-spacing:-.01em; }
.wms-inventory-page .app-table :deep(.col-num) { text-align:center; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.wms-inventory-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.wms-inventory-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.wms-inventory-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.wms-inventory-page .badge.amber .dot { background:var(--amber-500); }
.wms-inventory-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.wms-inventory-page .badge.blue .dot { background:var(--blue-500); }
.wms-inventory-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.wms-inventory-page .badge.green .dot { background:var(--green-500); }
.wms-inventory-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.wms-inventory-page .badge.red .dot { background:var(--red-500); }
.wms-inventory-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.wms-inventory-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.wms-inventory-page .badge.gray .dot { background:var(--ink-400); }
.wms-inventory-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.wms-inventory-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.wms-inventory-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.wms-inventory-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; }
.wms-inventory-page .pagination-container :deep(.el-pagination .btn-prev), .wms-inventory-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.wms-inventory-page .pagination-container :deep(.el-pagination .btn-prev:hover), .wms-inventory-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.wms-inventory-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .wms-inventory-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .wms-inventory-page .filter-card .filter-bar { grid-template-columns:1fr; } .wms-inventory-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
