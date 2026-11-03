<template>
  <div class="app-container mk-list-page">
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
          <label>经销商</label>
          <div class="control is-select">
            <el-select v-model="queryParams.dealerId" placeholder="全部" clearable filterable @change="handleQuery">
              <el-option v-for="c in dealerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>物料</label>
          <div class="control" @click="materialFilterPickerRef.open(queryParams.materialId)">
            <el-input :model-value="filterMaterialName" placeholder="全部" readonly>
              <template #prefix><el-icon><Search /></el-icon></template>
              <template #suffix><el-icon @click.stop="clearMaterialFilter"><CircleClose /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>批次号</label>
          <div class="control">
            <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>下游类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.downstreamType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in mk_downstream_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>流向日期</label>
          <div class="control">
            <el-date-picker v-model="flowDateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><Filter /></el-icon> 同一发货单同一物料仅登记一次，重复项自动跳过
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab is-active">
            <span class="dot"></span><span>流向记录</span><span class="count">{{ total }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['marketing:flow:add']">手工登记</el-button>
          <button type="button" class="btn-soft is-outline" @click="openDraftDialog" v-hasPermi="['marketing:flow:add']">
            <el-icon><MagicStick /></el-icon> 按发货单生成
          </button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['marketing:flow:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['marketing:flow:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['marketing:flow:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" :width="colWidth('序号', 70)" align="center" />
          <el-table-column label="流向日期" prop="flowDate" :width="colWidth('flowDate', 110)" align="center">
            <template #default="scope">{{ (scope.row.flowDate || '').substring(0, 10) }}</template>
          </el-table-column>
          <el-table-column label="经销商" prop="dealerName" :min-width="colWidth('dealerName', 180)" show-overflow-tooltip />
          <el-table-column label="物料" prop="materialName" :min-width="colWidth('materialName', 180)" show-overflow-tooltip />
          <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" align="center">
            <template #default="scope"><span class="col-mono">{{ scope.row.batchNo || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" :width="colWidth('quantity', 110)" align="right" />
          <el-table-column label="下游类型" prop="downstreamType" :width="colWidth('downstreamType', 110)" align="center">
            <template #default="scope">
              <el-tag :type="downstreamTagType(scope.row.downstreamType)" effect="light">{{ dictLabel(mk_downstream_type, scope.row.downstreamType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下游客户名称" prop="downstreamName" :min-width="colWidth('downstreamName', 170)" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.downstreamName || '—' }}</template>
          </el-table-column>
          <el-table-column label="来源发货单" prop="shipmentNo" :width="colWidth('shipmentNo', 140)" align="center">
            <template #default="scope">
              <span v-if="scope.row.shipmentNo" class="col-mono">{{ scope.row.shipmentNo }}</span>
              <el-tag v-else size="small" type="info" effect="plain">手工</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="colWidth('操作', 130)" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['marketing:flow:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['marketing:flow:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 手工登记/修改对话框 -->
    <el-dialog v-model="open" width="640px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="flowRef" :model="form" :rules="formRules" label-width="100px">
        <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('f_basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>登记信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.f_basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.f_basic">
            <el-row>
              <el-col :span="12"><el-form-item label="经销商" prop="dealerId">
                <el-input v-model="form.dealerName" readonly placeholder="请选择经销商" style="width: 100%" @click="openDealerPicker">
                  <template v-if="form.dealerName" #append><el-button icon="CircleClose" @click.stop="clearDealer" /></template>
                  <template v-else #append><el-button icon="Search" @click="openDealerPicker" /></template>
                </el-input>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="流向日期" prop="flowDate">
                <el-date-picker v-model="form.flowDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item></el-col>
            </el-row>
            <el-form-item label="物料" prop="materialId">
              <el-input :model-value="formMaterialName" readonly placeholder="请选择物料" style="width: 100%" @click="materialPickerRef.open(form.materialId)">
                <template v-if="formMaterialName" #append><el-button icon="CircleClose" @click.stop="clearFormMaterial" /></template>
                <template v-else #append><el-button icon="Search" @click="materialPickerRef.open(form.materialId)" /></template>
              </el-input>
            </el-form-item>
            <el-row>
              <el-col :span="12"><el-form-item label="规格型号"><el-input :model-value="form.specModel" readonly placeholder="选择物料后带出" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="单位">
                <dict-tag :options="wms_unit" :value="form.unit" />
              </el-form-item></el-col>
            </el-row>
            <el-row>
              <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="启用批次管理的物料必填" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :min="0.0001" :precision="4" :controls="false" style="width: 100%" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('f_downstream')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>下游信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.f_downstream }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.f_downstream">
            <el-row>
              <el-col :span="12"><el-form-item label="下游类型" prop="downstreamType">
                <el-select v-model="form.downstreamType" style="width: 100%">
                  <el-option v-for="d in mk_downstream_type" :key="d.value" :label="d.label" :value="d.value" />
                </el-select>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="下游名称" prop="downstreamName"><el-input v-model="form.downstreamName" placeholder="下游客户名称" /></el-form-item></el-col>
            </el-row>
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
          </div>
        </section>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 按发货单生成草稿对话框 -->
    <el-dialog v-model="draftOpen" width="1032px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></div>
          <span class="rd-detail-header-title">按发货单生成流向草稿</span>
        </div>
      </template>
      <el-form inline>
        <el-form-item label="发货单">
          <el-input :model-value="draftShipmentLabel" readonly placeholder="请选择发货单" style="width: 420px" @click="openShipmentPicker">
            <template v-if="draftShipmentId" #append><el-button icon="CircleClose" @click.stop="clearDraftShipment" /></template>
            <template v-else #append><el-button icon="Search" @click="openShipmentPicker" /></template>
          </el-input>
        </el-form-item>
      </el-form>
      <div v-loading="draftLoading">
        <el-table v-if="draftRows.length > 0" :data="draftRows" border size="small">
          <el-table-column label="物料" prop="materialName" min-width="180" show-overflow-tooltip />
          <el-table-column label="发货数量" prop="shipQty" width="100" align="right" />
          <el-table-column label="流向日期" width="160">
            <template #default="scope"><el-date-picker v-model="scope.row.flowDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" size="small" /></template>
          </el-table-column>
          <el-table-column label="批次号" width="140">
            <template #default="scope"><el-input v-model="scope.row.batchNo" size="small" /></template>
          </el-table-column>
          <el-table-column label="数量" width="140">
            <template #default="scope"><el-input-number v-model="scope.row.quantity" :min="0.0001" :precision="4" :controls="false" size="small" style="width: 100%" /></template>
          </el-table-column>
          <el-table-column label="下游类型" width="150">
            <template #default="scope">
              <el-select v-model="scope.row.downstreamType" size="small">
                <el-option v-for="d in mk_downstream_type" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="下游名称" min-width="150">
            <template #default="scope"><el-input v-model="scope.row.downstreamName" size="small" placeholder="终端名称" /></template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="选择发货单后自动生成草稿" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="draftOpen = false">取 消</el-button>
        <el-button type="primary" :disabled="draftRows.length === 0" @click="submitDraft">批量保存</el-button>
      </template>
    </el-dialog>

    <!-- 业务说明 -->
    <el-dialog v-model="showHelp" width="680px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
          <span class="rd-detail-header-title">流向登记业务说明</span>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-body" style="display:block">
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">1</span>功能定位</div>
              <div class="help-item-desc">流向登记记录「经销商把货分销给了谁」，是食品安全追溯链「成品批次 → 发货客户 → 经销商 → 下游去向」的最后一环。发生质量召回时，可按成品批次逆向生成「经销商 → 下游客户」完整清单，第一时间通知到位。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">2</span>登记方式一：手工登记</div>
              <div class="help-item-desc">线下分销未走系统发货时使用。选择经销商（弹框仅可选「经销商」类型客户）、流向日期、物料（自动带出规格型号与单位）、数量及下游信息；启用批次管理的物料批次号为必填（必填星号提示），非批次物料可留空，由登记人按实际情况补录。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">3</span>登记方式二：按发货单生成</div>
              <div class="help-item-desc">系统发货后推荐使用。选择经销商客户名下的发货单，系统按发货明细自动生成草稿（带出物料、批次号、发货数量），核对并补录下游信息后批量保存。发货单选择弹框仅列出经销商客户的发货单；发货单客户非经销商类型时无法生成。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">4</span>关键业务规则</div>
              <div class="help-item-desc">① 经销商名下：流向只能登记在「经销商」类型客户名下，其他类型客户（企业/个人）不产生流向数据；② 防重：同一发货单同一物料仅登记一次（系统唯一键），重复保存自动跳过；③ 下游类型：二级经销商 / 企业客户 / 个人客户 / 终端不详，下游名称用于召回通知与窜货核查定位。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">5</span>数据用途</div>
              <div class="help-item-desc">① 质量召回：按成品批次逆向追溯，出「经销商 → 下游」完整名单；② 窜货核查：流向与经销商授权区域比对，发现区域外分销；③ 市场流向分析与「经销商专项报表」的数据来源。</div>
            </div>
            <div class="help-item">
              <div class="help-item-title"><span class="help-item-index">6</span>数据维护</div>
              <div class="help-item-desc">流向记录支持修改与删除（需相应操作权限）。建议经销商发货或分销后 3 个工作日内完成登记与核对，保证追溯链数据完整；月底核对经销商专项报表中的流向登记率。</div>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
    <material-picker ref="materialFilterPickerRef" title="筛选物料" @confirm="onMaterialFilterConfirm" />
    <customer-picker ref="dealerPickerRef" title="选择经销商" customer-type="2" @confirm="onDealerPickerConfirm" />
    <shipment-picker ref="shipmentPickerRef" title="选择发货单" customer-type="2" @confirm="onShipmentPickerConfirm" />
  </div>
</template>

<script setup name="MkDealerFlow">
import { listFlow, getFlow, flowDraft, addFlow, batchSaveFlow, updateFlow, delFlow } from '@/api/mk/dealerFlow'
import { listCustomer } from '@/api/mk/customer'
import { listShipment } from '@/api/mk/shipment'
import MaterialPicker from '@/components/MaterialPicker/index.vue'
import ShipmentPicker from '@/components/ShipmentPicker/index.vue'
import CustomerPicker from '@/components/CustomerPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('mk_dealer_flow_index')
const { collapsedCards, toggleCard } = useDetailCard(["f_basic", "f_downstream"])
const showAdvanced = ref(false)
const { mk_downstream_type, wms_unit } = proxy.useDict('mk_downstream_type', 'wms_unit')

const list = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')
const flowDateRange = ref([])
const dealerOptions = ref([])
const materialPickerRef = ref()
const materialFilterPickerRef = ref()
const dealerPickerRef = ref()
const filterMaterialName = ref('')
const showHelp = ref(false)

// 草稿弹窗
const draftOpen = ref(false)
const draftShipmentId = ref(null)
const draftRows = ref([])
const draftLoading = ref(false)
const shipmentPickerRef = ref()
const draftShipmentLabel = ref('')

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, dealerId: undefined, materialId: undefined, batchNo: undefined, downstreamType: undefined },
  form: {},
  rules: {
    dealerId: [{ required: true, message: '请选择经销商', trigger: 'change' }],
    flowDate: [{ required: true, message: '请选择流向日期', trigger: 'change' }],
    materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
    quantity: [{ required: true, message: '请填写数量', trigger: 'blur' }],
    downstreamType: [{ required: true, message: '请选择下游类型', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const formRules = computed(() => {
  const r = { ...rules.value }
  // 启用批次管理的物料：批次号必填（红星随物料选择动态显示）
  if (form.value.isBatchManage === '1') {
    r.batchNo = [{ required: true, message: '启用批次管理的物料必须填写批次号', trigger: 'blur' }]
  }
  return r
})

/** 表单内物料名称（通过草稿/详情已知则显示） */
const formMaterialName = computed(() => form.value.materialName || '')

function dictLabel(listRef, val) { const item = listRef.value.find(d => d.value == val); return item ? item.label : '-' }
function downstreamTagType(val) { return { '1': '', '2': 'warning', '3': 'success', '4': 'info' }[val] || 'info' }

function getList() {
  loading.value = true
  listFlow(proxy.addDateRange(queryParams.value, flowDateRange.value, 'FlowDate')).then(res => {
    list.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
    applySavedWidths()
  })
}

function loadDealerOptions() {
  listCustomer({ pageNum: 1, pageSize: 9999, customerType: '2' }, { suppressError: true }).then(res => { dealerOptions.value = res.rows || [] })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 10, dealerId: undefined, materialId: undefined, batchNo: undefined, downstreamType: undefined }
  flowDateRange.value = []
  filterMaterialName.value = ''
  handleQuery()
}
function handleSelectionChange(selection) { ids.value = selection.map(i => i.flowId); single.value = selection.length !== 1; multiple.value = !selection.length }

function reset() {
  form.value = { flowId: undefined, dealerId: undefined, flowDate: proxy.parseTime(new Date(), '{y}-{m}-{d}'), materialId: undefined, materialName: undefined, batchNo: undefined, quantity: undefined, downstreamType: '4', downstreamName: undefined, remark: undefined }
  proxy.resetForm('flowRef')
}

function handleAdd() { reset(); loadDealerOptions(); open.value = true; title.value = '手工登记流向' }
function handleUpdate(row) {
  reset(); loadDealerOptions()
  getFlow(row.flowId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改流向登记' })
}

function onMaterialPickerConfirm(material) {
  form.value.materialId = material.materialId
  form.value.materialName = material.materialName
  form.value.specModel = material.specModel
  form.value.unit = material.unit
  form.value.isBatchManage = material.isBatchManage
}
function openDealerPicker() { dealerPickerRef.value.open() }
function onDealerPickerConfirm(c) { form.value.dealerId = c.customerId; form.value.dealerName = c.customerName }
function clearDealer() { form.value.dealerId = undefined; form.value.dealerName = '' }
function clearFormMaterial() { form.value.materialId = undefined; form.value.materialName = ''; form.value.specModel = ''; form.value.unit = undefined; form.value.isBatchManage = '0' }
function clearMaterialFilter() { queryParams.value.materialId = undefined; filterMaterialName.value = ''; handleQuery() }
function onMaterialFilterConfirm(material) {
  queryParams.value.materialId = material.materialId
  filterMaterialName.value = material.materialName
  handleQuery()
}

function submitForm() {
  proxy.$refs['flowRef'].validate(valid => {
    if (valid) {
      if (form.value.flowId != undefined) { updateFlow(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addFlow(form.value).then(() => { proxy.$modal.msgSuccess('登记成功'); open.value = false; getList() }) }
    }
  })
}

function handleDelete(row) {
  const flowIds = row.flowId || ids.value
  proxy.$modal.confirm('确认删除所选流向记录？').then(() => delFlow(flowIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(e => { if (e !== 'cancel' && e !== 'close') proxy.$modal.msgError('删除失败：' + (e?.message || e)) })
}
function handleExport() { proxy.download('mk/dealerFlow/export', { ...proxy.addDateRange(queryParams.value, flowDateRange.value, 'FlowDate') }, `dealer_flow_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

// ====== 按发货单生成草稿 ======
function openDraftDialog() {
  draftShipmentId.value = null
  draftShipmentLabel.value = ''
  draftRows.value = []
  draftOpen.value = true
}

function openShipmentPicker() { shipmentPickerRef.value.open(draftShipmentId.value) }
function clearDraftShipment() { draftShipmentId.value = null; draftShipmentLabel.value = ''; draftRows.value = [] }
function onShipmentPickerConfirm(s) {
  draftShipmentId.value = s.shipmentId
  draftShipmentLabel.value = s.shipmentNo + (s.customerName ? '（' + s.customerName + '）' : '')
  loadDraft(s.shipmentId)
}

function loadDraft(shipmentId) {
  if (!shipmentId) { draftRows.value = []; return }
  draftLoading.value = true
  flowDraft(shipmentId).then(res => {
    draftRows.value = (res.data || []).map(r => ({ ...r, flowDate: proxy.parseTime(new Date(), '{y}-{m}-{d}'), batchNo: r.batchNo || undefined, downstreamType: r.downstreamType || '4', downstreamName: r.downstreamName || undefined }))
    if (draftRows.value.length === 0) proxy.$modal.msgWarning('该发货单无可生成流向的明细')
  }).finally(() => { draftLoading.value = false })
}

function submitDraft() {
  const invalid = draftRows.value.find(r => !r.quantity || Number(r.quantity) <= 0)
  if (invalid) { proxy.$modal.msgError('存在数量为空或小于等于0的明细，请修正'); return }
  batchSaveFlow(draftRows.value).then(res => {
    proxy.$modal.msgSuccess(res.msg || '保存成功')
    draftOpen.value = false
    getList()
  })
}

getList()
loadDealerOptions()
</script>

<style scoped>
.help-item { padding: 10px 12px; border-radius: 8px; background: var(--bg-50, #f8fafc); border: 1px solid var(--line-200, #e2e8f0); }
.help-item + .help-item { margin-top: 10px; }
.help-item-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: var(--ink-700, #334155); margin-bottom: 4px; }
.help-item-index { display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background: var(--brand-50, #eff6ff); color: var(--brand-500, #3b82f6); font-size: 12px; font-weight: 700; flex: none; }
.help-item-desc { font-size: 13px; line-height: 1.8; color: var(--ink-500, #64748b); padding-left: 28px; }
</style>
