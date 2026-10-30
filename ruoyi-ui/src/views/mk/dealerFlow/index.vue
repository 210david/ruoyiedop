<template>
  <div class="app-container mk-list-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
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
        <div class="field">
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
        <el-table border v-loading="loading" :data="list" @selection-change="handleSelectionChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="70" align="center" />
          <el-table-column label="流向日期" prop="flowDate" width="110" align="center">
            <template #default="scope">{{ (scope.row.flowDate || '').substring(0, 10) }}</template>
          </el-table-column>
          <el-table-column label="经销商" prop="dealerName" min-width="180" show-overflow-tooltip />
          <el-table-column label="物料" prop="materialName" min-width="180" show-overflow-tooltip />
          <el-table-column label="批次号" prop="batchNo" width="120" align="center">
            <template #default="scope"><span class="col-mono">{{ scope.row.batchNo || '—' }}</span></template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="110" align="right" />
          <el-table-column label="下游类型" prop="downstreamType" width="110" align="center">
            <template #default="scope">
              <el-tag :type="downstreamTagType(scope.row.downstreamType)" effect="light">{{ dictLabel(mk_downstream_type, scope.row.downstreamType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下游客户名称" prop="downstreamName" min-width="170" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.downstreamName || '—' }}</template>
          </el-table-column>
          <el-table-column label="来源发货单" prop="shipmentNo" width="140" align="center">
            <template #default="scope">
              <span v-if="scope.row.shipmentNo" class="col-mono">{{ scope.row.shipmentNo }}</span>
              <el-tag v-else size="small" type="info" effect="plain">手工</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center" fixed="right">
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
        <el-form-item label="经销商" prop="dealerId">
          <el-select v-model="form.dealerId" placeholder="请选择经销商" filterable style="width: 100%">
            <el-option v-for="c in dealerOptions" :key="c.customerId" :label="c.customerName" :value="c.customerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="流向日期" prop="flowDate">
          <el-date-picker v-model="form.flowDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="物料" prop="materialId">
          <div class="control" @click="materialPickerRef.open(form.materialId)">
            <el-input :model-value="formMaterialName" placeholder="点击选择物料" readonly>
              <template #suffix><el-icon v-if="form.materialId" @click.stop="form.materialId = undefined; formMaterialName = ''"><CircleClose /></el-icon></template>
            </el-input>
          </div>
        </el-form-item>
        <el-row>
          <el-col :span="12"><el-form-item label="批次号" prop="batchNo"><el-input v-model="form.batchNo" placeholder="启用批次管理的物料必填" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数量" prop="quantity"><el-input-number v-model="form.quantity" :min="0.0001" :precision="4" :controls="false" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="下游类型" prop="downstreamType">
            <el-select v-model="form.downstreamType" style="width: 100%">
              <el-option v-for="d in mk_downstream_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="下游名称" prop="downstreamName"><el-input v-model="form.downstreamName" placeholder="下游客户名称" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 按发货单生成草稿对话框 -->
    <el-dialog v-model="draftOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg></div>
          <span class="rd-detail-header-title">按发货单生成流向草稿</span>
        </div>
      </template>
      <el-form inline>
        <el-form-item label="发货单">
          <el-select v-model="draftShipmentId" placeholder="搜索并选择发货单" filterable remote :remote-method="searchShipments" :loading="shipmentSearching" style="width: 360px" @change="loadDraft">
            <el-option v-for="s in shipmentOptions" :key="s.shipmentId" :label="s.shipmentNo + '（' + (s.customerName || '') + '）'" :value="s.shipmentId" />
          </el-select>
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
    <el-dialog v-model="showHelp" title="流向登记业务说明" width="640px" append-to-body>
      <div style="line-height: 1.9; font-size: 14px">
        <p><b>1. 用途：</b>登记经销商将货物分销给下游（二级经销商/终端/个人）的流向数据，用于市场流向分析与窜货核查。</p>
        <p><b>2. 系统生成：</b>「按发货单生成」依据发货单明细自动生成草稿（默认下游类型为"终端不详"），核对后批量保存。</p>
        <p><b>3. 防重规则：</b>同一发货单同一物料仅登记一次，重复保存自动跳过。</p>
        <p><b>4. 手工登记：</b>线下分销未走系统发货时，可手工登记流向。</p>
      </div>
      <template #footer>
        <el-button @click="showHelp = false">我知道了</el-button>
      </template>
    </el-dialog>

    <material-picker ref="materialPickerRef" title="选择物料" @confirm="onMaterialPickerConfirm" />
    <material-picker ref="materialFilterPickerRef" title="筛选物料" @confirm="onMaterialFilterConfirm" />
  </div>
</template>

<script setup name="MkDealerFlow">
import { listFlow, getFlow, flowDraft, addFlow, batchSaveFlow, updateFlow, delFlow } from '@/api/mk/dealerFlow'
import { listCustomer } from '@/api/mk/customer'
import { listShipment } from '@/api/mk/shipment'
import MaterialPicker from '@/components/MaterialPicker/index.vue'

const { proxy } = getCurrentInstance()
const { mk_downstream_type } = proxy.useDict('mk_downstream_type')

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
const filterMaterialName = ref('')
const showHelp = ref(false)

// 草稿弹窗
const draftOpen = ref(false)
const draftShipmentId = ref(null)
const draftRows = ref([])
const draftLoading = ref(false)
const shipmentOptions = ref([])
const shipmentSearching = ref(false)

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

const formRules = computed(() => rules.value)

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
}
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
  draftRows.value = []
  searchShipments('')
  draftOpen.value = true
}

function searchShipments(keyword) {
  shipmentSearching.value = true
  listShipment({ pageNum: 1, pageSize: 50, shipmentNo: keyword || undefined }, { suppressError: true }).then(res => {
    shipmentOptions.value = res.rows || []
  }).finally(() => { shipmentSearching.value = false })
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
