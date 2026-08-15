<template>
  <div class="app-container pms-catalog-page">
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
          <label>供应商</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
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
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_catalog_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>物料编码</label>
          <div class="control">
            <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>规格型号</label>
          <div class="control">
            <el-input v-model="queryParams.specModel" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>单位</label>
          <div class="control is-select">
            <el-select v-model="queryParams.unit" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_unit" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>供货单价≥</label>
          <div class="control">
            <el-input-number v-model="queryParams.minPrice" :precision="2" :min="0" placeholder="最小金额" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>供货单价≤</label>
          <div class="control">
            <el-input-number v-model="queryParams.maxPrice" :precision="2" :min="0" placeholder="最大金额" style="width: 100%" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>价格生效</label>
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
    </div>

    <!-- ===== Table Section ===== -->
    <div class="surface">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:catalog:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:catalog:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:catalog:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleImport" v-hasPermi="['pms:catalog:import']">
            <el-icon><Upload /></el-icon> 导入
          </button>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:catalog:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_catalog_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="物料编码" prop="materialCode" key="materialCode" :width="colWidth('materialCode', 140)" resizable sortable="custom" :sort-orders="['descending','ascending']" v-if="columns.materialCode.visible" />
          <el-table-column label="物料名称" prop="materialName" key="materialName" :width="colWidth('materialName', 180)" resizable show-overflow-tooltip v-if="columns.materialName.visible" />
          <el-table-column label="规格型号" prop="specModel" key="specModel" :width="colWidth('specModel', 150)" resizable show-overflow-tooltip v-if="columns.specModel.visible" />
          <el-table-column label="单位" prop="unit" key="unit" :width="colWidth('unit', 80)" resizable align="center" v-if="columns.unit.visible"><template #default="scope"><span class="badge gray">{{ unitLabel(scope.row.unit) }}</span></template></el-table-column>
          <el-table-column label="供货单价" prop="unitPrice" key="unitPrice" :width="colWidth('unitPrice', 110)" resizable align="right" sortable="custom" :sort-orders="['descending','ascending']" class-name="col-num" v-if="columns.unitPrice.visible"><template #default="scope"><span class="rd-amount">{{ formatMoney(scope.row.unitPrice) }}</span></template></el-table-column>
          <el-table-column label="税率(%)" prop="taxRate" key="taxRate" :width="colWidth('taxRate', 90)" resizable align="center" v-if="columns.taxRate.visible" />
          <el-table-column label="最小起订量" prop="minOrderQty" key="minOrderQty" :width="colWidth('minOrderQty', 110)" resizable align="right" v-if="columns.minOrderQty.visible" />
          <el-table-column label="交货周期" prop="deliveryCycle" key="deliveryCycle" :width="colWidth('deliveryCycle', 90)" resizable align="center" v-if="columns.deliveryCycle.visible"><template #default="scope">{{ scope.row.deliveryCycle != null ? scope.row.deliveryCycle + '天' : '-' }}</template></el-table-column>
          <el-table-column label="价格生效" prop="priceEffectiveDate" key="priceEffectiveDate" :width="colWidth('priceEffectiveDate', 110)" resizable align="center" sortable="custom" :sort-orders="['descending','ascending']" v-if="columns.priceEffectiveDate.visible" />
          <el-table-column label="价格失效" prop="priceExpireDate" key="priceExpireDate" :width="colWidth('priceExpireDate', 110)" resizable align="center" v-if="columns.priceExpireDate.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 90)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" :sort-orders="['descending','ascending']" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:catalog:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:catalog:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

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
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_catalog_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'price', 'date', 'remark'])

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  supplierName: { label: '供应商', visible: true },
  materialCode: { label: '物料编码', visible: true },
  materialName: { label: '物料名称', visible: true },
  specModel: { label: '规格型号', visible: true },
  unit: { label: '单位', visible: true },
  unitPrice: { label: '供货单价', visible: true },
  taxRate: { label: '税率(%)', visible: true },
  minOrderQty: { label: '最小起订量', visible: true },
  deliveryCycle: { label: '交货周期', visible: true },
  priceEffectiveDate: { label: '价格生效', visible: true },
  priceExpireDate: { label: '价格失效', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_catalog_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(defaultColumns).forEach(key => {
        result[key] = {
          label: defaultColumns[key].label,
          visible: parsed[key] !== undefined ? parsed[key] : defaultColumns[key].visible
        }
      })
      return result
    }
  } catch (e) {}
  return { ...defaultColumns }
}
const columns = ref(loadColumnVisibility())

function badgeClass(status) {
  const map = { '0': 'green', '1': 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = pms_catalog_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function unitLabel(unit) {
  const item = wms_unit.value.find(d => d.value == unit)
  return item ? item.label : '-'
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierName) count++
  if (queryParams.value.materialName) count++
  if (queryParams.value.status) count++
  if (queryParams.value.materialCode) count++
  if (queryParams.value.specModel) count++
  if (queryParams.value.unit) count++
  if (queryParams.value.minPrice != null) count++
  if (queryParams.value.maxPrice != null) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

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

function getList() { loading.value = true; listCatalog(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function toggleAdvanced() { showAdvanced.value = !showAdvanced.value }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'PriceEffectiveDate'); showAdvanced.value = false; getList() }
function resetQuery() { queryParams.value.supplierName = undefined; queryParams.value.materialName = undefined; queryParams.value.status = undefined; queryParams.value.materialCode = undefined; queryParams.value.specModel = undefined; queryParams.value.unit = undefined; queryParams.value.minPrice = undefined; queryParams.value.maxPrice = undefined; queryParams.value.params = {}; dateRange.value = []; handleQuery() }
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
function handleExport() { proxy.download('pms/catalog/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'PriceEffectiveDate') }, `catalog_${new Date().getTime()}.xlsx`) }
function handleImport() { proxy.$refs['importRef'].open() }
function cancel() { open.value = false; reset() }

loadSupplierOptions()
loadMaterialOptions()
getList()
onActivated(() => { getList() })
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-catalog-page {
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
.pms-catalog-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-catalog-page .filter-card { padding:14px 20px 16px; }
.pms-catalog-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-catalog-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-catalog-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-catalog-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-catalog-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-catalog-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-catalog-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-catalog-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-catalog-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-catalog-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-catalog-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-catalog-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-catalog-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-catalog-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-catalog-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-catalog-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-catalog-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-catalog-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-catalog-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-catalog-page .field .control :deep(.el-select) { width:100%; }
.pms-catalog-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-catalog-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-catalog-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-catalog-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-catalog-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-catalog-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-catalog-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.pms-catalog-page .field .control :deep(.el-input-number) { width:100%; }
.pms-catalog-page .field .control :deep(.el-input-number .el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-catalog-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-catalog-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-catalog-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-catalog-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-catalog-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-catalog-page .btn-soft .el-icon { font-size:14px; }
.pms-catalog-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-catalog-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-catalog-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-catalog-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-catalog-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-catalog-page .table-wrap { overflow-x:auto; }
.pms-catalog-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-catalog-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-catalog-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-catalog-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-catalog-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-catalog-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-catalog-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-catalog-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-catalog-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-catalog-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-catalog-page .app-table :deep(.col-num) { text-align:right; font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; }
.pms-catalog-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-catalog-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-catalog-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-catalog-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-catalog-page .badge.green .dot { background:var(--green-500); }
.pms-catalog-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-catalog-page .badge.gray .dot { background:var(--ink-400); }
.pms-catalog-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-catalog-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-catalog-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-catalog-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-catalog-page .pagination-container :deep(.el-pagination .btn-prev), .pms-catalog-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-catalog-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-catalog-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-catalog-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-catalog-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-catalog-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-catalog-page .toolbar { flex-wrap:wrap; gap:10px; } }
</style>
