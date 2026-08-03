<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="供应商" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="物料名称" prop="materialName"><el-input v-model="queryParams.materialName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px"><el-option v-for="d in pms_catalog_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="物料编码" prop="materialCode" v-show="showAdvanced"><el-input v-model="queryParams.materialCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="规格型号" prop="specModel" v-show="showAdvanced"><el-input v-model="queryParams.specModel" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="单位" prop="unit" v-show="showAdvanced"><el-select v-model="queryParams.unit" placeholder="全部" clearable style="width: 160px"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="供货单价≥" prop="minPrice" v-show="showAdvanced"><el-input-number v-model="queryParams.minPrice" :precision="2" :min="0" placeholder="最小金额" style="width: 200px" /></el-form-item>
      <el-form-item label="供货单价≤" prop="maxPrice" v-show="showAdvanced"><el-input-number v-model="queryParams.maxPrice" :precision="2" :min="0" placeholder="最大金额" style="width: 200px" /></el-form-item>
      <el-form-item label="价格生效" v-show="showAdvanced"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain :icon="showAdvanced ? 'ArrowUp' : 'ArrowDown'" @click="toggleAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:catalog:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:catalog:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:catalog:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['pms:catalog:import']">导入</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:catalog:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="物料编码" prop="materialCode" :width="colWidth('materialCode', 140)" resizable sortable="custom" :sort-orders="['descending','ascending']" />
      <el-table-column label="物料名称" prop="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="规格型号" prop="specModel" :width="colWidth('specModel', 150)" resizable show-overflow-tooltip />
      <el-table-column label="单位" prop="unit" :width="colWidth('unit', 80)" resizable align="center"><template #default="scope"><dict-tag :options="wms_unit" :value="scope.row.unit" /></template></el-table-column>
      <el-table-column label="供货单价" prop="unitPrice" :width="colWidth('unitPrice', 110)" resizable align="right" sortable="custom" :sort-orders="['descending','ascending']"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.unitPrice) }}</span></template></el-table-column>
      <el-table-column label="税率(%)" prop="taxRate" :width="colWidth('taxRate', 90)" resizable align="center" />
      <el-table-column label="最小起订量" prop="minOrderQty" :width="colWidth('minOrderQty', 110)" resizable align="right" />
      <el-table-column label="交货周期" prop="deliveryCycle" :width="colWidth('deliveryCycle', 90)" resizable align="center"><template #default="scope">{{ scope.row.deliveryCycle != null ? scope.row.deliveryCycle + '天' : '-' }}</template></el-table-column>
      <el-table-column label="价格生效" prop="priceEffectiveDate" :width="colWidth('priceEffectiveDate', 110)" resizable align="center" sortable="custom" :sort-orders="['descending','ascending']" />
      <el-table-column label="价格失效" prop="priceExpireDate" :width="colWidth('priceExpireDate', 110)" resizable align="center" />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 90)" resizable align="center"><template #default="scope"><dict-tag :options="pms_catalog_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" :sort-orders="['descending','ascending']" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:catalog:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:catalog:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="catalogRef" :model="form" :rules="rules" label-width="110px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="物料" prop="materialId"><el-select v-model="form.materialId" filterable placeholder="请选择物料" style="width: 100%" @change="onMaterialChange"><el-option v-for="m in materialOptions" :key="m.materialId" :label="m.materialCode + ' - ' + m.materialName" :value="m.materialId" /></el-select></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="物料编码"><el-input v-model="form.materialCode" placeholder="选择物料后自动带出" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="物料名称"><el-input v-model="form.materialName" placeholder="选择物料后自动带出" disabled /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="规格型号"><el-input v-model="form.specModel" placeholder="选择物料后自动带出" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="单位" prop="unit"><el-select v-model="form.unit" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('price')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>价格与订购</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.price }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.price">
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="供货单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="税率(%)" prop="taxRate"><el-input-number v-model="form.taxRate" :precision="2" :min="0" :max="100" style="width: 100%" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="最小起订量" prop="minOrderQty"><el-input-number v-model="form.minOrderQty" :precision="2" :min="0" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="交货周期(天)" prop="deliveryCycle"><el-input-number v-model="form.deliveryCycle" :min="0" style="width: 100%" /></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('date')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>价格有效期与状态</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.date }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.date">
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="价格生效日期" prop="priceEffectiveDate"><el-date-picker v-model="form.priceEffectiveDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="价格失效日期" prop="priceExpireDate"><el-date-picker v-model="form.priceExpireDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="24"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio v-for="d in pms_catalog_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio></el-radio-group></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('remark')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.remark }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.remark">
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <excel-import-dialog ref="importRef" title="供货清单导入" action="/pms/catalog/importData" template-action="/pms/catalog/importTemplate" template-file-name="catalog_template" update-support-label="是否更新已经存在的供货清单数据" @success="getList" />

    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></div><span class="rd-detail-header-title">供货清单详情</span><div class="rd-detail-header-sub" v-if="viewData.supplierName"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">{{ viewData.supplierName }} - {{ viewData.materialName }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料编码</span><div class="rd-value">{{ viewData.materialCode || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">物料名称</span><div class="rd-value">{{ viewData.materialName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">规格型号</span><div class="rd-value">{{ viewData.specModel || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">单位</span><div class="rd-value"><dict-tag :options="wms_unit" :value="viewData.unit" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg></span>价格与订购</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供货单价</span><div class="rd-value rd-amount">{{ formatMoney(viewData.unitPrice) }}</div></div>
            <div class="rd-item"><span class="rd-label">税率(%)</span><div class="rd-value">{{ viewData.taxRate != null ? viewData.taxRate : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">最小起订量</span><div class="rd-value">{{ viewData.minOrderQty != null ? viewData.minOrderQty : '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">交货周期</span><div class="rd-value">{{ viewData.deliveryCycle != null ? viewData.deliveryCycle + '天' : '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>价格有效期与状态</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">价格生效日期</span><div class="rd-value">{{ viewData.priceEffectiveDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">价格失效日期</span><div class="rd-value">{{ viewData.priceExpireDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_catalog_status" :value="viewData.status" /></div></div>
          </div></div>
        </section>
        <section class="rd-card" v-if="viewData.remark">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>备注</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
          </div></div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="PmsCatalog">
import { listCatalog, getCatalog, addCatalog, updateCatalog, delCatalog } from '@/api/pms/catalog'
import { listSupplier } from '@/api/wms/supplier'
import { listMaterial } from '@/api/wms/material'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard, formatAmount, formatMoney } from '@/composables/useDetailCard'
import ExcelImportDialog from '@/components/ExcelImportDialog'

const { proxy } = getCurrentInstance()
const { pms_catalog_status, wms_unit } = proxy.useDict('pms_catalog_status', 'wms_unit')
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_catalog_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'price', 'date', 'remark'])

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const supplierOptions = ref([])
const materialOptions = ref([])
const showAdvanced = ref(false)
const dateRange = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, supplierName: undefined, materialName: undefined, status: undefined, materialCode: undefined, specModel: undefined, unit: undefined, minPrice: undefined, maxPrice: undefined, params: {} },
  rules: { supplierId: [{ required: true, message: '供应商不能为空', trigger: 'change' }], materialId: [{ required: true, message: '物料不能为空', trigger: 'change' }], unitPrice: [{ required: true, message: '供货单价不能为空', trigger: 'blur' }] }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listCatalog(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function toggleAdvanced() { showAdvanced.value = !showAdvanced.value }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'PriceEffectiveDate'); showAdvanced.value = false; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; dateRange.value = []; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.catalogId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { catalogId: undefined, supplierId: undefined, supplierName: undefined, materialId: undefined, materialCode: undefined, materialName: undefined, specModel: undefined, unit: undefined, unitPrice: undefined, taxRate: 13, minOrderQty: 1, deliveryCycle: undefined, currency: 'CNY', priceEffectiveDate: undefined, priceExpireDate: undefined, status: '1', remark: undefined }; proxy.resetForm('catalogRef') }
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function onMaterialChange(val) { const matched = materialOptions.value.find(m => m.materialId === val); if (matched) { form.value.materialCode = matched.materialCode; form.value.materialName = matched.materialName; form.value.specModel = matched.specModel; form.value.unit = matched.unit } }
function loadMaterialOptions() { listMaterial({ pageNum: 1, pageSize: 999, status: '0' }).then(res => { materialOptions.value = res.rows || [] }) }
function handleAdd() { reset(); open.value = true; title.value = '添加供货清单' }
function handleUpdate(row) { reset(); getCatalog(row.catalogId).then(res => { form.value = res.data; open.value = true; title.value = '修改供货清单' }) }
function handleView(row) { getCatalog(row.catalogId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['catalogRef'].validate(valid => { if (valid) { if (form.value.catalogId != undefined) { updateCatalog(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addCatalog(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const catalogIds = row.catalogId || ids.value; proxy.$modal.confirm('确认删除编号为"' + catalogIds + '"的数据？').then(() => delCatalog(catalogIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('pms/catalog/export', { ...queryParams.value }, `catalog_${new Date().getTime()}.xlsx`) }
function handleImport() { proxy.$refs['importRef'].open() }
function cancel() { open.value = false; reset() }

loadSupplierOptions()
loadMaterialOptions()
getList()
onActivated(() => { getList() })
</script>
