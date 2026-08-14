<template>
  <div class="app-container qms-sn-page">
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
          <label>序列号</label>
          <div class="control">
            <el-input v-model="queryParams.snCode" placeholder="请输入序列号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入批次号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>工单号</label>
          <div class="control">
            <el-input v-model="queryParams.workOrderNo" placeholder="请输入工单号" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入物料编码" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="在制" value="0" />
              <el-option label="入库" value="1" />
              <el-option label="发货" value="2" />
              <el-option label="退货" value="3" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
          <div class="control"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" /></div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
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
          <button class="tab-btn" :class="{ active: queryParams.status === undefined }" @click="setStatusFilter(undefined)">全部</button>
          <button class="tab-btn" :class="{ active: queryParams.status === '0' }" @click="setStatusFilter('0')">
            <span class="tab-dot dot-warning"></span> 在制
          </button>
          <button class="tab-btn" :class="{ active: queryParams.status === '1' }" @click="setStatusFilter('1')">
            <span class="tab-dot dot-primary"></span> 入库
          </button>
          <button class="tab-btn" :class="{ active: queryParams.status === '2' }" @click="setStatusFilter('2')">
            <span class="tab-dot dot-green"></span> 发货
          </button>
          <button class="tab-btn" :class="{ active: queryParams.status === '3' }" @click="setStatusFilter('3')">
            <span class="tab-dot dot-red"></span> 退货
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['qms:trace:sn:add']">新增SN</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="!selectedId" @click="handleUpdate()" v-hasPermi="['qms:trace:sn:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="!selectedIds.length" @click="handleDelete()" v-hasPermi="['qms:trace:sn:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['qms:trace:sn:query']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="qms_sn_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="snList" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="序列号" prop="snCode" key="snCode" :width="colWidth('snCode', 200)" resizable show-overflow-tooltip v-if="columns.snCode.visible">
            <template #default="scope">
              <el-link type="primary" @click="handleTrace(scope.row)">{{ scope.row.snCode }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="批次号" prop="batchNo" key="batchNo" :width="colWidth('batchNo', 160)" resizable show-overflow-tooltip v-if="columns.batchNo.visible" />
          <el-table-column label="工单号" prop="workOrderNo" key="workOrderNo" :width="colWidth('workOrderNo', 140)" resizable show-overflow-tooltip v-if="columns.workOrderNo.visible" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 160)" resizable show-overflow-tooltip v-if="columns.materialCode.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 100)" resizable align="center" v-if="columns.status.visible">
            <template #header><span>状态</span><el-tooltip content="SN当前状态：在制、入库、发货、退货" placement="top"><el-icon class="col-tip"><QuestionFilled /></el-icon></el-tooltip></template>
            <template #default="scope">
              <span class="badge" :class="snBadgeClass(scope.row.status)"><span class="dot"></span>{{ snStatusText(scope.row.status) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 160)" resizable align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleTrace(scope.row)">追溯</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['qms:trace:sn:edit']">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- ===== Add/Edit Dialog ===== -->
    <el-dialog v-model="open" width="700px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M7 8h.01M12 8h.01M17 8h.01M7 12h.01M12 12h.01M17 12h.01M7 16h.01M12 16h.01M17 16h.01"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="snRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('e_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M7 8h.01M12 8h.01M17 8h.01M7 12h.01M12 12h.01M17 12h.01M7 16h.01M12 16h.01M17 16h.01"/></svg></span>SN基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_basic">
              <el-row :gutter="24">
                <el-col :span="24">
                  <el-form-item prop="snCode"><template #label><span>序列号</span><el-tooltip content="产品的唯一序列号，用于单件追溯" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.snCode" placeholder="请输入序列号" /></el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item prop="batchNo"><template #label><span>批次号</span><el-tooltip content="SN所属的物料批次号" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.batchNo" placeholder="请输入批次号" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="workOrderNo"><template #label><span>工单号</span><el-tooltip content="关联的生产工单编号" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.workOrderNo" placeholder="请输入工单号" /></el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item prop="materialCode"><template #label><span>物料编码</span><el-tooltip content="物料的唯一编码，复用仓库主数据" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template><el-input v-model="form.materialCode" placeholder="请输入物料编码" /></el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item prop="status"><template #label><span>状态</span><el-tooltip content="SN当前生命周期状态：在制、入库、发货、退货" placement="top"><el-icon class="rd-form-tip"><QuestionFilled /></el-icon></el-tooltip></template>
                    <el-select v-model="form.status" style="width:100%">
                      <el-option label="在制" value="0" />
                      <el-option label="入库" value="1" />
                      <el-option label="发货" value="2" />
                      <el-option label="退货" value="3" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card" v-if="form.remark !== undefined || form.id">
            <div class="rd-card-header" @click="toggleCard('e_other')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.e_other }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.e_other">
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== Trace Result Dialog ===== -->
    <el-dialog v-model="traceOpen" width="900px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg></div>
          <span class="rd-detail-header-title">SN追溯结果</span>
        </div>
      </template>
      <div class="rd-page" v-if="traceData">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('t_basic')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M7 8h.01M12 8h.01M17 8h.01M7 12h.01M12 12h.01M17 12h.01M7 16h.01M12 16h.01M17 16h.01"/></svg></span>SN基本信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.t_basic"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">序列号</span><div class="rd-value">{{ traceData.snCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value">
              <span class="badge" :class="snBadgeClass(traceData.status)"><span class="dot"></span>{{ snStatusText(traceData.status) }}</span>
            </div></div>
            <div class="rd-item"><span class="rd-label">批次号</span><div class="rd-value">{{ traceData.batchNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">工单号</span><div class="rd-value">{{ traceData.workOrderNo || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ traceData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">创建时间</span><div class="rd-value">{{ traceData.createTime || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="batchTraceList.length > 0">
          <div class="rd-card-header" @click="toggleCard('t_batch')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg></span>同批次SN列表</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.t_batch }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.t_batch">
            <el-table :data="batchTraceList" border size="small" class="app-table">
              <el-table-column label="序列号" prop="snCode" min-width="200" />
              <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <span class="badge" :class="snBadgeClass(scope.row.status)"><span class="dot"></span>{{ snStatusText(scope.row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="物料编码" prop="materialCode" min-width="160" />
              <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
            </el-table>
          </div>
        </section>
      </div>
      <div v-else style="text-align: center; padding: 40px;">
        <el-empty description="未找到追溯信息" />
      </div>
      <template #footer><el-button @click="traceOpen = false">关 闭</el-button></template>
    </el-dialog>

    <!-- ===== 业务操作说明对话框 ===== -->
    <el-dialog v-model="showStatusHelp" title="SN单件追溯业务操作说明" width="780px" append-to-body>
      <div class="status-help-content">
        <h4>一、SN追溯说明</h4>
        <el-row :gutter="16">
          <el-col :span="12">
            <div class="highlight-card highlight-primary">
              <div class="highlight-card-title">什么是SN追溯</div>
              <div class="highlight-card-body">SN追溯以<strong>单件产品序列号</strong>为索引，记录该产品从生产、入库、发货到退货的全生命周期流转信息</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-success">
              <div class="highlight-card-title">单件追溯</div>
              <div class="highlight-card-body">点击序列号或「追溯」按钮，可查看该SN的详细信息，以及<strong>同批次</strong>下所有SN的列表，实现单件到批次的追溯能力</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :span="12">
            <div class="highlight-card highlight-warning">
              <div class="highlight-card-title">与批次谱系的关系</div>
              <div class="highlight-card-body">SN追溯是批次谱系追溯的<strong>细粒度补充</strong>，谱系管理关注批次级投入-产出关联，SN追溯关注单件级生命周期</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="highlight-card highlight-info">
              <div class="highlight-card-title">适用场景</div>
              <div class="highlight-card-body">适用于高价值产品、有防伪需求的产品、需要精确到单件召回的产品等<strong>单件追溯</strong>场景</div>
            </div>
          </el-col>
        </el-row>

        <h4>二、状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="在制"><strong>生产中</strong>，SN已分配但尚未完成入库</el-descriptions-item>
          <el-descriptions-item label="入库"><strong>已入库</strong>，SN已完工并完成仓库入库登记</el-descriptions-item>
          <el-descriptions-item label="发货"><strong>已发货</strong>，SN已根据出库单发给客户</el-descriptions-item>
          <el-descriptions-item label="退货"><strong>已退货</strong>，SN因质量问题等原因被客户退回</el-descriptions-item>
        </el-descriptions>

        <h4>三、业务操作流程</h4>
        <el-timeline>
          <el-timeline-item type="primary" :hollow="true">
            <strong>新增SN：</strong>手动录入SN记录，填写序列号、批次号、工单号、物料编码等信息
          </el-timeline-item>
          <el-timeline-item type="info" :hollow="true">
            <strong>追溯查询：</strong>点击列表中的序列号链接或「追溯」按钮，查看SN详情和同批次SN列表
          </el-timeline-item>
          <el-timeline-item type="warning" :hollow="true">
            <strong>修改状态：</strong>SN状态随业务流程自动更新，也可通过修改功能手动调整
          </el-timeline-item>
          <el-timeline-item type="success" :hollow="true">
            <strong>导出数据：</strong>支持导出SN列表为Excel，用于审计或备份
          </el-timeline-item>
        </el-timeline>

        <h4>四、专业词汇说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="SN (Serial Number)"><strong>产品序列号</strong>，单件产品的唯一标识，用于单件级追溯</el-descriptions-item>
          <el-descriptions-item label="批次号">SN所属的物料批次号，一个批次可包含多个SN</el-descriptions-item>
          <el-descriptions-item label="工单号">SN关联的生产工单编号</el-descriptions-item>
          <el-descriptions-item label="谱系(Genealogy)">物料批次间的投入-产出关联关系链，是批次级追溯的基础数据结构</el-descriptions-item>
          <el-descriptions-item label="NCR">Non-Conformance Report（不合格品报告），记录质量异常并跟踪处理闭环</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="QmsTraceSn">
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import { listSn, getSn, addSn, updateSn, delSn, getSnByCode, getSnByBatch } from '@/api/qms/traceSn'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('qms_sn_index')
const { collapsedCards, toggleCard } = useDetailCard(['e_basic', 'e_other', 't_basic', 't_batch'])
const { proxy } = getCurrentInstance()

const snList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const showStatusHelp = ref(false)
const dateRange = ref([])
const total = ref(0)
const title = ref('')
const traceOpen = ref(false)
const traceData = ref(null)
const batchTraceList = ref([])
const selectedId = ref(null)
const selectedIds = ref([])

const defaultColumns = {
  snCode: { label: '序列号', visible: true },
  batchNo: { label: '批次号', visible: true },
  workOrderNo: { label: '工单号', visible: true },
  materialCode: { label: '物料编码', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('qms_sn_columns')
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, snCode: undefined, batchNo: undefined, workOrderNo: undefined, materialCode: undefined, status: undefined, params: {} },
  rules: {
    snCode: [{ required: true, message: '序列号不能为空', trigger: 'blur' }],
    batchNo: [{ required: true, message: '批次号不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.snCode) count++
  if (queryParams.value.batchNo) count++
  if (queryParams.value.workOrderNo) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.status !== undefined && queryParams.value.status !== null) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

getList()

function getList() {
  loading.value = true
  listSn(queryParams.value).then(res => {
    snList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  })
}

function snStatusText(status) {
  const map = { '0': '在制', '1': '入库', '2': '发货', '3': '退货' }
  return map[status] || '未知'
}

function snBadgeClass(status) {
  const map = { '0': 'warning', '1': 'primary', '2': 'green', '3': 'red' }
  return map[status] || 'info'
}

function handleQuery() {
  showAdvanced.value = false
  proxy.addDateRange(queryParams.value, dateRange.value, 'CreateTime')
  queryParams.value.pageNum = 1
  getList()
}
function resetQuery() {
  queryParams.value.snCode = undefined
  queryParams.value.batchNo = undefined
  queryParams.value.workOrderNo = undefined
  queryParams.value.materialCode = undefined
  queryParams.value.status = undefined
  dateRange.value = []
  queryParams.value.params = {}
  handleQuery()
}
function setStatusFilter(val) {
  queryParams.value.status = val
  queryParams.value.pageNum = 1
  getList()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
  selectedId.value = selectedIds.value[0] || null
}

function reset() {
  form.value = { snCode: undefined, batchNo: undefined, workOrderNo: undefined, materialCode: undefined, status: '0', remark: undefined }
  proxy.resetForm('snRef')
}

function handleAdd() { reset(); open.value = true; title.value = '新增SN记录' }
function handleUpdate(row) {
  reset()
  const id = row?.id || selectedId.value
  if (!id) return
  getSn(id).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改SN记录'
  })
}

function submitForm() {
  proxy.$refs['snRef'].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateSn(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addSn(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}

function handleDelete(row) {
  const idArr = row?.id ? [row.id] : selectedIds.value
  if (!idArr.length) return
  proxy.$modal.confirm('确认删除选中的SN记录?').then(() => {
    return delSn(idArr)
  }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}

function handleExport() {
  proxy.download('qms/trace/sn/export', { ...queryParams.value }, `SN追溯数据_${new Date().getTime()}.xlsx`)
}

async function handleTrace(row) {
  traceData.value = null
  batchTraceList.value = []
  traceOpen.value = true

  const res = await getSnByCode(row.snCode)
  if (res.data) {
    traceData.value = res.data
    if (res.data.batchNo) {
      const batchRes = await getSnByBatch(res.data.batchNo)
      batchTraceList.value = batchRes.data || []
    }
  }
}

function cancel() { open.value = false; reset() }
</script>

<style scoped>
.qms-sn-page {
  padding-top: 10px;
  --brand-50: #eef2ff; --brand-100: #e0e7ff; --brand-200: #c7d2fe;
  --brand-500: #6366f1; --brand-600: #4f46e5; --brand-700: #4338ca;
  --ink-900: #0f172a; --ink-700: #334155; --ink-500: #64748b;
  --ink-400: #94a3b8; --ink-300: #cbd5e1; --ink-200: #e2e8f0;
  --ink-100: #f1f5f9; --ink-50: #f8fafc;
  --green-50: #ecfdf5; --green-500: #10b981; --green-700: #047857;
  --red-50: #fef2f2; --red-500: #ef4444; --red-700: #b91c1c;
  --warning-50: #fffbeb; --warning-500: #f59e0b; --warning-700: #b45309;
  --primary-50: #eef2ff; --primary-500: #6366f1; --primary-700: #4338ca;
  --r-sm: 6px; --r-md: 10px; --r-lg: 14px;
  --shadow-card: 0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out: cubic-bezier(.16,.84,.44,1);
  color: var(--ink-900);
}
.qms-sn-page .surface {
  background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-lg);
  box-shadow: var(--shadow-card); overflow: hidden; margin-bottom: 8px;
}
/* ===== Filter Card ===== */
.qms-sn-page .filter-card { padding: 14px 20px 16px; }
.qms-sn-page .filter-card .filter-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.qms-sn-page .filter-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink-700); }
.qms-sn-page .filter-title .glyph { width: 4px; height: 14px; background: var(--brand-600); border-radius: 2px; }
.qms-sn-page .filter-card .adv-link { font-size: 14px; color: var(--ink-500); text-decoration: none; display: flex; align-items: center; gap: 4px; transition: color .15s; cursor: pointer; }
.qms-sn-page .filter-card .adv-link:hover { color: var(--brand-600); }
.qms-sn-page .filter-card .adv-link .chev { transition: transform .2s var(--ease-out); }
.qms-sn-page .filter-card .adv-link.is-open .chev { transform: rotate(180deg); }
.qms-sn-page .field .control :deep(.el-date-editor) { width: 100%; }
.qms-sn-page .field .control :deep(.el-date-editor .el-range-input) { background: transparent; border: 0; font-size: 14px; color: var(--ink-900); }
.qms-sn-page .field .control :deep(.el-date-editor .el-range-separator) { color: var(--ink-400); }
.qms-sn-page .field .control :deep(.el-date-editor .el-range__icon) { color: var(--ink-400); }
.qms-sn-page .field .control :deep(.el-date-editor.el-input) { width: 100%; }
.qms-sn-page .field .control :deep(.el-date-editor.el-input .el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.qms-sn-page .field .control :deep(.el-date-editor.el-input .el-input__inner) { border: 0; background: transparent; font-size: 14px; color: var(--ink-900); height: 34px; line-height: 34px; }
.qms-sn-page .filter-card .filter-bar { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px 16px; }
.qms-sn-page .filter-card .filter-actions { display: flex; align-items: center; justify-content: space-between; margin-top: 14px; padding-top: 14px; border-top: 1px dashed var(--ink-200); }
.qms-sn-page .filter-card .filter-info { font-size: 13px; color: var(--ink-500); display: flex; align-items: center; gap: 6px; }
.qms-sn-page .filter-card .filter-buttons { display: flex; gap: 8px; }
.qms-sn-page .field { display: flex; flex-direction: column; gap: 6px; }
.qms-sn-page .field label { font-size: 14px; font-weight: 500; color: var(--ink-700); }
.qms-sn-page .field .control { display: flex; align-items: center; height: 36px; padding: 0 12px; background: #fff; border: 1px solid var(--ink-200); border-radius: var(--r-sm); transition: border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.qms-sn-page .field .control:focus-within { border-color: var(--brand-500); box-shadow: 0 0 0 3px rgba(99,102,241,.15); }
.qms-sn-page .field .control :deep(.el-input__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; height: 34px; }
.qms-sn-page .field .control :deep(.el-input__inner) { border: 0; background: transparent; font-size: 14px; color: var(--ink-900); height: 34px; line-height: 34px; }
.qms-sn-page .field .control :deep(.el-input__inner::placeholder) { color: var(--ink-400); }
.qms-sn-page .field .control :deep(.el-input__prefix) { color: var(--ink-400); margin-right: 4px; }
.qms-sn-page .field .control :deep(.el-input__prefix .el-icon) { font-size: 14px; }
.qms-sn-page .field .control :deep(.el-select) { width: 100%; }
.qms-sn-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow: none !important; background: transparent !important; padding: 0; min-height: 34px; height: 34px; }
.qms-sn-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow: none !important; }
.qms-sn-page .field .control.is-select { padding: 0; }
.qms-sn-page .field .control.is-select :deep(.el-select .el-select__wrapper) { padding: 0 12px; }
/* ===== Status Tabs ===== */
.qms-sn-page .status-tabs { display: flex; align-items: center; justify-content: space-between; padding: 10px 20px; border-bottom: 1px solid var(--ink-200); background: var(--ink-50); }
.qms-sn-page .status-tabs .tabs-track { display: flex; align-items: center; gap: 8px; }
.qms-sn-page .status-tabs .tab-btn { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 14px; font-size: 14px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid var(--ink-200); background: #fff; color: var(--ink-500); cursor: pointer; transition: all .15s var(--ease-out); }
.qms-sn-page .status-tabs .tab-btn:hover { border-color: var(--ink-300); color: var(--ink-700); }
.qms-sn-page .status-tabs .tab-btn.active { background: var(--brand-600); border-color: var(--brand-600); color: #fff; }
.qms-sn-page .status-tabs .tab-dot { width: 8px; height: 8px; border-radius: 50%; }
.qms-sn-page .status-tabs .dot-green { background: var(--green-500); }
.qms-sn-page .status-tabs .dot-red { background: var(--red-500); }
.qms-sn-page .status-tabs .dot-warning { background: var(--warning-500); }
.qms-sn-page .status-tabs .dot-primary { background: var(--brand-500); }
.qms-sn-page .status-tabs .tip-pill { display: inline-flex; align-items: center; gap: 6px; height: 30px; padding: 0 12px; font-size: 14px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid #fde68a; background: #fffbeb; color: #b45309; cursor: pointer; transition: all .15s var(--ease-out); white-space: nowrap; }
.qms-sn-page .status-tabs .tip-pill:hover { background: #fef3c7; border-color: #f59e0b; }
/* ===== Toolbar ===== */
.qms-sn-page .toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid var(--ink-200); background: var(--ink-50); }
.qms-sn-page .toolbar .left { display: flex; gap: 8px; align-items: center; }
.qms-sn-page .toolbar .right { display: flex; gap: 8px; align-items: center; }
.qms-sn-page .toolbar-divider { width: 1px; height: 18px; background: var(--ink-200); margin: 0 4px; }
.qms-sn-page .btn-soft { display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; font-size: 14px; font-weight: 500; border-radius: var(--r-sm); border: 1px solid transparent; cursor: pointer; user-select: none; transition: all .15s var(--ease-out); }
.qms-sn-page .btn-soft .el-icon { font-size: 14px; }
.qms-sn-page .btn-soft.is-outline { background: #fff; color: var(--ink-700); border-color: var(--ink-200); }
.qms-sn-page .btn-soft.is-outline:hover { background: var(--ink-50); border-color: var(--ink-300); color: var(--ink-900); }
.qms-sn-page .btn-soft.is-danger-outline { background: #fff; color: var(--red-700); border-color: #fecaca; }
.qms-sn-page .btn-soft.is-danger-outline:hover { background: var(--red-50); border-color: var(--red-500); }
.qms-sn-page .btn-soft:disabled { opacity: .5; cursor: not-allowed; }
/* ===== Table ===== */
.qms-sn-page .table-wrap { overflow-x: auto; }
.qms-sn-page .app-table { --el-table-bg-color: #fff; --el-table-header-bg-color: var(--ink-50); --el-table-row-hover-bg-color: #fafbff; --el-table-border-color: transparent; --el-table-text-color: var(--ink-700); --el-table-header-text-color: var(--ink-500); }
.qms-sn-page .app-table :deep(.el-table__body td) { border-right-color: transparent !important; }
.qms-sn-page .app-table :deep(.el-table__header th) { border-right-color: transparent !important; background: var(--ink-50) !important; color: var(--ink-500); font-weight: 600; font-size: 14px; padding: 12px 16px; border-bottom: 1px solid var(--ink-200); }
.qms-sn-page .app-table :deep(.el-table__body td) { padding: 14px 16px; border-bottom: 1px solid var(--ink-100); color: var(--ink-700); }
.qms-sn-page .app-table :deep(.el-table__row:hover > td) { background: #fafbff !important; }
.qms-sn-page .app-table :deep(.el-table__inner-wrapper::before) { display: none; }
.qms-sn-page .col-tip { margin-left: 4px; font-size: 14px; color: var(--ink-400); cursor: help; vertical-align: middle; }
.qms-sn-page .col-tip:hover { color: var(--brand-500); }
/* ===== Badge ===== */
.qms-sn-page .badge { display: inline-flex; align-items: center; gap: 5px; padding: 3px 9px; border-radius: 999px; font-size: 13px; font-weight: 600; line-height: 1; border: 1px solid transparent; }
.qms-sn-page .badge .dot { width: 6px; height: 6px; border-radius: 50%; }
.qms-sn-page .badge.green { background: var(--green-50); color: var(--green-700); border-color: #a7f3d0; }
.qms-sn-page .badge.green .dot { background: var(--green-500); }
.qms-sn-page .badge.red { background: var(--red-50); color: var(--red-700); border-color: #fecaca; }
.qms-sn-page .badge.red .dot { background: var(--red-500); }
.qms-sn-page .badge.warning { background: var(--warning-50); color: var(--warning-700); border-color: #fde68a; }
.qms-sn-page .badge.warning .dot { background: var(--warning-500); }
.qms-sn-page .badge.primary { background: var(--primary-50); color: var(--primary-700); border-color: #c7d2fe; }
.qms-sn-page .badge.primary .dot { background: var(--primary-500); }
.qms-sn-page .badge.info { background: var(--ink-100); color: var(--ink-500); border-color: var(--ink-200); }
.qms-sn-page .badge.info .dot { background: var(--ink-400); }
/* ===== Dialog Detail Page Styles ===== */
:deep(.rd-dialog .el-dialog__header) { padding: 0; margin: 0; border: none; }
:deep(.rd-dialog .el-dialog__headerbtn) { top: 14px; right: 14px; z-index: 10; }
:deep(.rd-dialog .el-dialog__headerbtn .el-dialog__close) { color: #fff; font-size: 20px; }
:deep(.rd-dialog .el-dialog__body) { padding: 12px 16px 16px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); }
:deep(.rd-dialog .rd-page) { max-width: 100% !important; }
.rd-detail-header { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 60%, #3b82f6 100%); border-radius: 12px 12px 0 0; position: relative; overflow: hidden; }
.rd-detail-header::before { content: ''; position: absolute; top: -30px; right: -15px; width: 140px; height: 140px; border-radius: 50%; background: radial-gradient(circle, rgb(255 255 255 / 0.15) 0%, transparent 70%); pointer-events: none; }
.rd-detail-header-icon { display: flex; align-items: center; justify-content: center; width: 40px; height: 40px; border-radius: 10px; background: rgb(255 255 255 / 0.2); border: 1px solid rgb(255 255 255 / 0.25); color: #fff; flex-shrink: 0; backdrop-filter: blur(4px); box-shadow: 0 4px 12px rgb(0 0 0 / 0.1); }
.rd-detail-header-title { font-size: 18px; font-weight: 700; color: #fff; letter-spacing: -0.02em; line-height: 1.3; }
.rd-page { max-width: 100%; margin: 0 auto; }
.rd-card { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); overflow: hidden; margin-bottom: 8px; transition: box-shadow 0.25s cubic-bezier(0.4, 0, 0.2, 1); animation: rdFadeIn 0.4s ease-out forwards; }
.rd-card:hover { box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1); }
.rd-card:last-child { margin-bottom: 0; }
.rd-card-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; background: linear-gradient(to right, #f9fafb, #fff); border-bottom: 1px solid #f3f4f6; cursor: pointer; user-select: none; }
.rd-card-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: #111827; }
.rd-card-icon { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; color: #2563eb; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); }
.rd-card-header .rd-collapse-btn { display: flex; align-items: center; justify-content: center; width: 24px; height: 24px; border: none; background: transparent; border-radius: 6px; color: #9ca3af; cursor: pointer; transition: all 0.15s ease; flex-shrink: 0; }
.rd-card-header .rd-collapse-btn:hover { background: #f3f4f6; color: #4b5563; }
.rd-card-header .rd-collapse-btn svg { transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.rd-card-header .rd-collapse-btn.is-collapsed svg { transform: rotate(-90deg); }
.rd-card-body { padding: 14px 16px; }
.rd-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px 24px; }
.rd-item { display: flex; align-items: center; gap: 12px; }
.rd-item--full { grid-column: 1 / -1; }
.rd-label { flex: 0 0 auto; min-width: 80px; display: flex; align-items: center; font-size: 14px; font-weight: 500; color: #6b7280; white-space: nowrap; }
.rd-value { flex: 1 1 auto; font-size: 14px; font-weight: 500; color: #111827; line-height: 1.5; padding-left: 12px; border-left: 1px solid #e5e7eb; min-width: 0; }
.rd-form-tip { margin-left: 4px; color: #c0c4cc; font-size: 14px; cursor: help; }
.rd-form-tip:hover { color: #909399; }
@keyframes rdFadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.rd-card:nth-child(2) { animation-delay: 0.06s; }
.rd-card:nth-child(3) { animation-delay: 0.12s; }
.rd-card:nth-child(4) { animation-delay: 0.18s; }
@media (max-width: 768px) { .rd-grid { grid-template-columns: 1fr; } }
/* ===== Status Help Dialog ===== */
.status-help-content { max-height: 500px; overflow-y: auto; padding-right: 10px; }
.status-help-content h4 { margin: 20px 0 12px 0; color: #303133; font-weight: 600; border-left: 4px solid #409eff; padding-left: 10px; }
.status-help-content h4:first-child { margin-top: 0; }
.status-help-content .highlight-card { border-radius: 8px; padding: 16px; border: 1px solid; }
.status-help-content .highlight-card-title { font-size: 14px; font-weight: 600; margin-bottom: 8px; display: flex; align-items: center; }
.status-help-content .highlight-card-body { font-size: 13px; color: #606266; line-height: 1.6; }
.status-help-content .highlight-primary { background-color: #ecf5ff; border-color: #a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color: #409eff; }
.status-help-content .highlight-success { background-color: #f0f9eb; border-color: #b3e19d; }
.status-help-content .highlight-success .highlight-card-title { color: #67c23a; }
.status-help-content .highlight-warning { background-color: #fdf6ec; border-color: #f5dab1; }
.status-help-content .highlight-warning .highlight-card-title { color: #e6a23c; }
.status-help-content .highlight-info { background-color: #f4f4f5; border-color: #d3d4d6; }
.status-help-content .highlight-info .highlight-card-title { color: #909399; }
/* ===== Responsive ===== */
@media (max-width: 1100px) { .qms-sn-page .filter-card .filter-bar { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 720px) { .qms-sn-page .filter-card .filter-bar { grid-template-columns: 1fr; } .qms-sn-page .toolbar { flex-wrap: wrap; gap: 10px; } }
</style>