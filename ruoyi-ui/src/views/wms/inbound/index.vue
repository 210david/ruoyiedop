<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="入库单号" prop="orderNo"><el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="入库类型" prop="orderType"><el-select v-model="queryParams.orderType" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_inbound_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 200px"><el-option v-for="d in wms_inbound_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:inbound:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:inbound:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:inbound:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="入库单号" prop="orderNo" :width="colWidth('orderNo', 160)" resizable />
      <el-table-column label="入库类型" prop="orderType" :width="colWidth('orderType', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_inbound_type" :value="scope.row.orderType" /></template></el-table-column>
      <el-table-column label="供应商" prop="supplierName" show-overflow-tooltip />
      <el-table-column label="目标仓库" prop="warehouseName" :width="colWidth('warehouseName', 120)" resizable />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="wms_inbound_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="总数量" prop="totalQty" :width="colWidth('totalQty', 100)" resizable align="right" />
      <el-table-column label="入库日期" prop="inboundDate" :width="colWidth('inboundDate', 120)" resizable align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:inbound:edit']" v-if="scope.row.status === '0'">修改</el-button>
          <el-button link type="primary" icon="Promotion" @click="handleSubmit(scope.row)" v-hasPermi="['wms:inbound:edit']" v-if="scope.row.status === '0'">提交</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:inbound:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog v-model="open" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
          </div>
          <span class="rd-detail-header-title">{{ title }}</span>
          <div class="rd-detail-header-sub" v-if="form.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ form.orderNo }}</span>
          </div>
        </div>
      </template>
      <el-form ref="inboundRef" :model="form" :rules="rules" label-width="100px">
          <!-- 单据信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('basic')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
                单据信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.basic">
              <el-row :gutter="16">
                <el-col :span="8"><el-form-item label="入库单号" prop="orderNo"><el-input v-model="form.orderNo" placeholder="自动生成" disabled /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="入库类型" prop="orderType"><el-select v-model="form.orderType" style="width:100%"><el-option v-for="d in wms_inbound_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="状态"><dict-tag :options="wms_inbound_status" :value="form.status" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 入库信息 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('info')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>
                入库信息
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.info }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.info">
              <el-row :gutter="16">
                <el-col :span="8"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable clearable placeholder="请选择供应商" style="width:100%"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="目标仓库" prop="warehouseId"><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库" style="width:100%"><el-option v-for="w in warehouseOptions" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="入库日期" prop="inboundDate"><el-date-picker v-model="form.inboundDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <!-- 入库明细 -->
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('detail')">
              <div class="rd-card-title">
                <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>
                入库明细
              </div>
              <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.detail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
            </div>
            <div class="rd-card-body" v-show="!collapsedCards.detail">
              <el-row :gutter="10" class="mb8">
                <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddDetail">添加明细</el-button></el-col>
              </el-row>
              <el-table :data="form.detailList" border @header-dragend="onHeaderDragEnd">
                <el-table-column label="物料" min-width="200"><template #default="scope"><el-select v-model="scope.row.materialId" filterable clearable placeholder="请选择物料" style="width:100%" @change="handleMaterialChange(scope.$index, scope.row)"><el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" /></el-select></template></el-table-column>
                <el-table-column label="计划数量" width="120"><template #default="scope"><el-input-number v-model="scope.row.planQty" :precision="2" :min="0" controls-position="right" style="width:100%" /></template></el-table-column>
                <el-table-column label="批次号" width="130"><template #default="scope"><el-input v-model="scope.row.batchNo" :placeholder="scope.row.isBatchManage === '1' ? '批次号（必填）' : '批次号'" /></template></el-table-column>
                <el-table-column label="生产日期" width="150"><template #default="scope"><el-date-picker v-model="scope.row.productionDate" type="date" value-format="YYYY-MM-DD" style="width:100%" :placeholder="scope.row.isExpiryManage === '1' ? '生产日期（必填）' : '生产日期'" /></template></el-table-column>
                <el-table-column label="单位" width="80" align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
                <el-table-column label="单价" width="120"><template #default="scope"><el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" controls-position="right" style="width:100%" /></template></el-table-column>
                <el-table-column label="操作" width="80" align="center"><template #default="scope"><el-button link type="danger" icon="Delete" @click="handleDeleteDetail(scope.$index)">删除</el-button></template></el-table-column>
              </el-table>
              <el-form-item label="备注" prop="remark" style="margin-top: 15px"><el-input v-model="form.remark" type="textarea" /></el-form-item>
            </div>
          </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailOpen" width="1080px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M9 12h6"/><path d="M12 9v6"/></svg>
          </div>
          <span class="rd-detail-header-title">入库单详情</span>
          <div class="rd-detail-header-sub" v-if="detailData.orderNo">
            <span class="rd-detail-header-divider"></span>
            <span class="rd-detail-header-no">编号：{{ detailData.orderNo }}</span>
            <dict-tag :options="wms_inbound_status" :value="detailData.status" />
          </div>
        </div>
      </template>
        <!-- 单据信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dBasic')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span>
              单据信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dBasic }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dBasic">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">入库单号</span><div class="rd-value">{{ detailData.orderNo }}</div></div>
              <div class="rd-item"><span class="rd-label">入库类型</span><div class="rd-value"><dict-tag :options="wms_inbound_type" :value="detailData.orderType" /></div></div>
              <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="wms_inbound_status" :value="detailData.status" /></div></div>
              <div class="rd-item"><span class="rd-label">总数量</span><div class="rd-value">{{ detailData.totalQty }}</div></div>
              <div class="rd-item"><span class="rd-label">完成时间</span><div class="rd-value">{{ detailData.completeDate || '暂无' }}</div></div>
            </div>
          </div>
        </section>
        <!-- 入库信息 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dInfo')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg></span>
              入库信息
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dInfo }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dInfo">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ detailData.supplierName }}</div></div>
              <div class="rd-item"><span class="rd-label">目标仓库</span><div class="rd-value">{{ detailData.warehouseName }}</div></div>
              <div class="rd-item"><span class="rd-label">入库日期</span><div class="rd-value">{{ detailData.inboundDate }}</div></div>
            </div>
          </div>
        </section>
        <!-- 入库明细 -->
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('dDetail')">
            <div class="rd-card-title">
              <span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>
              入库明细
            </div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.dDetail }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.dDetail">
            <el-table :data="detailData.detailList" border @header-dragend="onHeaderDragEnd">
              <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 120)" resizable align="center" />
              <el-table-column label="物料名称" prop="materialName" show-overflow-tooltip align="center" />
              <el-table-column label="单位" prop="unit" :width="colWidth('unit', 70)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
              <el-table-column label="计划数量" prop="planQty" :width="colWidth('planQty', 100)" resizable align="center" />
              <el-table-column label="已收数量" prop="receivedQty" :width="colWidth('receivedQty', 100)" resizable align="center" />
              <el-table-column label="已上架数量" prop="putawayQty" :width="colWidth('putawayQty', 100)" resizable align="center" />
              <el-table-column label="批次号" prop="batchNo" :width="colWidth('batchNo', 120)" resizable align="center" />
              <el-table-column label="上架库位" prop="locationName" :width="colWidth('locationName', 100)" resizable show-overflow-tooltip align="center" />
            </el-table>
          </div>
        </section>
    </el-dialog>
  </div>
</template>

<script setup name="WmsInbound">
import { listInbound, getInbound, addInbound, updateInbound, delInbound, submitInbound } from '@/api/wms/inbound'
import { listWarehouse } from '@/api/wms/warehouse'
import { listSupplier } from '@/api/wms/supplier'
import { listMaterial } from '@/api/wms/material'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'info', 'detail', 'dBasic', 'dInfo', 'dDetail'])
const { proxy } = getCurrentInstance()
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('wms_inbound_index')
const { wms_inbound_type, wms_inbound_status, wms_unit } = proxy.useDict('wms_inbound_type', 'wms_inbound_status', 'wms_unit')
const list = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true); const ids = ref([]); const multiple = ref(true); const total = ref(0); const title = ref(''); const detailOpen = ref(false); const detailData = ref({})
const warehouseOptions = ref([]); const supplierOptions = ref([]); const materialOptions = ref([])
function today() { const d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0') }
const data = reactive({ form: {}, queryParams: { pageNum: 1, pageSize: 10, orderNo: undefined, orderType: undefined, status: undefined }, rules: { orderType: [{ required: true, message: '入库类型不能为空', trigger: 'change' }], warehouseId: [{ required: true, message: '目标仓库不能为空', trigger: 'change' }], inboundDate: [{ required: true, message: '预计入库日期不能为空', trigger: 'change' }] } })
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listInbound(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(sel) { ids.value = sel.map(i => i.orderId); multiple.value = !sel.length }
function reset() { form.value = { orderNo: undefined, orderType: '0', supplierId: undefined, warehouseId: undefined, inboundDate: today(), status: '0', remark: undefined, detailList: [] }; proxy.resetForm('inboundRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加入库单' }
function handleUpdate(row) { reset(); getInbound(row.orderId).then(res => { form.value = res.data; open.value = true; title.value = '修改入库单' }) }
function handleDetail(row) { getInbound(row.orderId).then(res => { detailData.value = res.data; detailOpen.value = true }) }
function handleAddDetail() { if (!form.value.detailList) form.value.detailList = []; form.value.detailList.push({ materialId: undefined, planQty: 0, batchNo: undefined, productionDate: undefined, unitPrice: 0, isBatchManage: '0', isExpiryManage: '0' }) }
function handleMaterialChange(index, row) { const material = materialOptions.value.find(m => m.materialId === row.materialId); if (material) { row.isBatchManage = material.isBatchManage; row.isExpiryManage = material.isExpiryManage; row.unit = material.unit } }
function handleDeleteDetail(index) { form.value.detailList.splice(index, 1) }
function submitForm() {
  if (!validateDetailList()) return
  proxy.$refs['inboundRef'].validate(v => {
    if (v) {
      if (form.value.orderId != null) {
        updateInbound(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addInbound(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() })
      }
    }
  })
}
function validateDetailList() {
  if (!form.value.detailList || form.value.detailList.length === 0) { proxy.$modal.msgError('请至少添加一条入库明细'); return false }
  for (let i = 0; i < form.value.detailList.length; i++) {
    const detail = form.value.detailList[i]
    if (!detail.materialId) { proxy.$modal.msgError(`第${i + 1}行明细：请选择物料`); return false }
    if (!detail.planQty || detail.planQty <= 0) { proxy.$modal.msgError(`第${i + 1}行明细：计划数量必须大于0`); return false }
    if (detail.isBatchManage === '1' && (!detail.batchNo || detail.batchNo.trim() === '')) { proxy.$modal.msgError(`第${i + 1}行明细：该物料启用批次管理，批次号必填`); return false }
    if (detail.isExpiryManage === '1' && !detail.productionDate) { proxy.$modal.msgError(`第${i + 1}行明细：该物料启用效期管理，生产日期必填`); return false }
  }
  return true
}
function handleDelete(row) { const orderIds = row.orderId || ids.value; proxy.$modal.confirm('确认删除？').then(() => delInbound(orderIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleSubmit(row) { proxy.$modal.confirm('确认提交入库单「' + row.orderNo + '」？提交后将无法修改。').then(() => submitInbound(row.orderId)).then(() => { getList(); proxy.$modal.msgSuccess('提交成功') }).catch(() => {}) }
function handleExport() { proxy.download('wms/inbound/export', { ...queryParams.value }, `inbound_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function loadOptions() {
  listWarehouse({ status: '0', pageSize: 999 }).then(res => { warehouseOptions.value = res.rows })
  listSupplier({ status: '0', pageSize: 999 }).then(res => { supplierOptions.value = res.rows })
  listMaterial({ status: '0', pageSize: 999 }).then(res => { materialOptions.value = res.rows })
}
loadOptions()
getList()

onActivated(() => {
  getList()
})
</script>
