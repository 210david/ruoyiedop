<template>
  <div class="app-container pms-supplier-page">
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
          <label>供应商编码</label>
          <div class="control">
            <el-input v-model="queryParams.supplierCode" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商名称</label>
          <div class="control">
            <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><OfficeBuilding /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>供应商类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.supplierType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>联系人</label>
          <div class="control">
            <el-input v-model="queryParams.contactPerson" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>联系电话</label>
          <div class="control">
            <el-input v-model="queryParams.contactPhone" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>统一信用码</label>
          <div class="control">
            <el-input v-model="queryParams.unifiedCreditCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>创建时间</label>
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
      <!-- 状态标签栏 -->
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeStatusTab === 'all' }" @click="handleStatusTabClick('all')">
            <span class="dot"></span>
            <span>全部</span>
            <span class="count">{{ statusCounts.all }}</span>
          </button>
          <button v-for="s in statusTabList" :key="s.value"
            class="status-tab"
            :class="[statusTabClass(s.value), { 'is-active': activeStatusTab === s.value }]"
            @click="handleStatusTabClick(s.value)">
            <span class="dot"></span>
            <span>{{ s.label }}</span>
            <span class="count">{{ statusCounts[s.value] || 0 }}</span>
          </button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true">
          <el-icon><WarningFilled /></el-icon>
          <span>业务操作说明</span>
        </button>
      </div>

      <!-- Toolbar -->
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:supplier:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:supplier:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:supplier:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['wms:supplier:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_supplier_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" key="序号" :width="colWidth('序号', 85)" resizable align="center" />
          <el-table-column label="供应商编码" prop="supplierCode" key="supplierCode" :width="colWidth('supplierCode', 180)" resizable sortable="custom" v-if="columns.supplierCode.visible" />
          <el-table-column label="供应商名称" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="简称" prop="supplierShortName" key="supplierShortName" :width="colWidth('supplierShortName', 120)" resizable show-overflow-tooltip v-if="columns.supplierShortName.visible" />
          <el-table-column label="类型" prop="supplierType" key="supplierType" :width="colWidth('supplierType', 120)" resizable align="center" v-if="columns.supplierType.visible"><template #default="scope"><span class="badge violet">{{ supplierTypeLabel(scope.row.supplierType) }}</span></template></el-table-column>
          <el-table-column label="等级" prop="supplierLevel" key="supplierLevel" :width="colWidth('supplierLevel', 120)" resizable align="center" sortable="custom" v-if="columns.supplierLevel.visible"><template #default="scope"><span class="badge blue">{{ supplierLevelLabel(scope.row.supplierLevel) }}</span></template></el-table-column>
          <el-table-column label="联系人" prop="contactPerson" key="contactPerson" :width="colWidth('contactPerson', 120)" resizable v-if="columns.contactPerson.visible" />
          <el-table-column label="联系电话" prop="contactPhone" key="contactPhone" :width="colWidth('contactPhone', 130)" resizable v-if="columns.contactPhone.visible" />
          <el-table-column label="统一信用代码" prop="unifiedCreditCode" key="unifiedCreditCode" :width="colWidth('unifiedCreditCode', 180)" resizable show-overflow-tooltip v-if="columns.unifiedCreditCode.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" sortable="custom" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:supplier:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:supplier:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="supplierRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商编码" prop="supplierCode"><el-input v-model="form.supplierCode" placeholder="保存后自动生成" disabled /></el-form-item></el-col><el-col :span="12"><el-form-item label="供应商名称" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入企业全称" /></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商简称" prop="supplierShortName"><el-input v-model="form.supplierShortName" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="供应商类型" prop="supplierType"><el-select v-model="form.supplierType" placeholder="请选择" style="width: 100%"><el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商等级" prop="supplierLevel"><el-select v-model="form.supplierLevel" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in wms_supplier_level" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="统一信用代码" prop="unifiedCreditCode"><div style="display:flex;gap:8px;width:100%"><el-input v-model="form.unifiedCreditCode" placeholder="请输入18位统一社会信用代码" @keyup.enter="handleEnterpriseLookup" style="flex:1" /><el-tooltip content="输入统一社会信用代码后点击查询，自动获取企业名称、法人等信息" placement="top"><el-button type="primary" plain icon="Search" :loading="enterpriseLoading" @click="handleEnterpriseLookup">查询企业</el-button></el-tooltip></div><div style="color: #999; font-size: 12px; margin-top: 4px;">提示：输入18位统一社会信用代码，点击查询按钮自动获取企业信息</div></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="联系人" prop="contactPerson"><el-input v-model="form.contactPerson" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="请输入" /></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="24"><el-form-item label="地址" prop="address"><el-input v-model="form.address" type="textarea" placeholder="请输入" /></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="法人代表" prop="legalPerson"><el-input v-model="form.legalPerson" placeholder="请输入" /></el-form-item></el-col><el-col :span="12"><el-form-item label="企业性质" prop="enterpriseNature"><el-select v-model="form.enterpriseNature" placeholder="请选择" clearable style="width: 100%"><el-option v-for="d in wms_enterprise_nature" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col></el-row>
        <el-row :gutter="20"><el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="0">正常</el-radio><el-radio value="1">停用</el-radio></el-radio-group></el-form-item></el-col></el-row>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></div><span class="rd-detail-header-title">供应商详情</span><div class="rd-detail-header-sub" v-if="viewData.supplierCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编码：{{ viewData.supplierCode }}</span></div></div></template>
      <div class="rd-page"><section class="rd-card"><div class="rd-card-body" style="display:block"><div class="rd-grid">
        <div class="rd-item"><span class="rd-label">供应商编码</span><div class="rd-value">{{ viewData.supplierCode || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">供应商名称</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">供应商简称</span><div class="rd-value">{{ viewData.supplierShortName || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">供应商类型</span><div class="rd-value"><dict-tag :options="wms_supplier_type" :value="viewData.supplierType" /></div></div>
        <div class="rd-item"><span class="rd-label">供应商等级</span><div class="rd-value"><dict-tag :options="wms_supplier_level" :value="viewData.supplierLevel" /></div></div>
        <div class="rd-item"><span class="rd-label">统一信用代码</span><div class="rd-value">{{ viewData.unifiedCreditCode || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">联系人</span><div class="rd-value">{{ viewData.contactPerson || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">联系电话</span><div class="rd-value">{{ viewData.contactPhone || '-' }}</div></div>
        <div class="rd-item rd-item--full"><span class="rd-label">地址</span><div class="rd-value">{{ viewData.address || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">法人代表</span><div class="rd-value">{{ viewData.legalPerson || '-' }}</div></div>
        <div class="rd-item"><span class="rd-label">企业性质</span><div class="rd-value"><dict-tag :options="wms_enterprise_nature" :value="viewData.enterpriseNature" /></div></div>
        <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="sys_normal_disable" :value="viewData.status" /></div></div>
        <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
      </div></div></section></div>
      <template #footer>
        <el-button @click="viewOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 状态说明对话框 -->
    <el-dialog v-model="showStatusHelp" title="供应商管理状态说明" width="600px" append-to-body>
      <div class="status-help-content">
        <h4>一、状态说明</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="正常">供应商处于正常可用状态，可在采购业务中被选择和使用</el-descriptions-item>
          <el-descriptions-item label="停用">供应商已被停用，不可在新的采购业务中被选择，但已有的业务记录仍可查看</el-descriptions-item>
        </el-descriptions>
        <h4>二、操作说明</h4>
        <el-timeline>
          <el-timeline-item type="success" :hollow="true"><strong>新增供应商：</strong>填写供应商基本信息（名称、类型、联系人等），保存后状态默认为"正常"</el-timeline-item>
          <el-timeline-item type="warning" :hollow="true"><strong>修改供应商：</strong>可修改供应商的基本信息，包括名称、联系人、统一信用代码等</el-timeline-item>
          <el-timeline-item type="danger" :hollow="true"><strong>停用供应商：</strong>将供应商状态改为"停用"，停用后不可在新的采购业务中被选择</el-timeline-item>
          <el-timeline-item type="info" :hollow="true"><strong>企业信息查询：</strong>输入18位统一社会信用代码，可自动获取企业名称、法人代表等信息</el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button type="primary" @click="showStatusHelp = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PmsSupplier">
import { listSupplier, getSupplier, addSupplier, updateSupplier, delSupplier } from '@/api/wms/supplier'
import { lookupEnterprise } from '@/api/pms/enterprise'
import { useColumnResize } from '@/composables/useColumnResize'
import { formatAmount } from '@/composables/useDetailCard'

const { proxy } = getCurrentInstance()
const { wms_supplier_type, wms_supplier_level, wms_enterprise_nature, sys_normal_disable } = proxy.useDict(
  'wms_supplier_type', 'wms_supplier_level', 'wms_enterprise_nature', 'sys_normal_disable'
)
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_supplier_index')

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  supplierCode: { label: '供应商编码', visible: true },
  supplierName: { label: '供应商名称', visible: true },
  supplierShortName: { label: '简称', visible: true },
  supplierType: { label: '类型', visible: true },
  supplierLevel: { label: '等级', visible: true },
  contactPerson: { label: '联系人', visible: true },
  contactPhone: { label: '联系电话', visible: true },
  unifiedCreditCode: { label: '统一信用代码', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_supplier_columns')
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

const activeStatusTab = ref('all')
const statusTabList = computed(() => sys_normal_disable.value.map(d => ({ label: d.label, value: d.value })))
const statusCounts = ref({ all: 0 })
function loadStatusCounts() {
  listSupplier({ pageNum: 1, pageSize: 999 }).then(res => {
    const counts = { all: res.total }
    sys_normal_disable.value.forEach(d => { counts[d.value] = 0 })
    ;(res.rows || []).forEach(r => { if (counts[r.status] !== undefined) counts[r.status]++ })
    statusCounts.value = counts
  }).catch(() => {})
}
function statusTabClass(value) {
  const map = { '0': 'tab-approved', '1': 'tab-void' }
  return map[value] || ''
}
function handleStatusTabClick(tab) {
  activeStatusTab.value = tab
  queryParams.value.status = tab === 'all' ? undefined : tab
  handleQuery()
}
function badgeClass(status) {
  const map = { '0': 'green', '1': 'gray' }
  return map[status] || 'gray'
}
function statusLabel(status) {
  const item = sys_normal_disable.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function supplierTypeLabel(type) {
  const item = wms_supplier_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}
function supplierLevelLabel(level) {
  const item = wms_supplier_level.value.find(d => d.value == level)
  return item ? item.label : '-'
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierCode) count++
  if (queryParams.value.supplierName) count++
  if (queryParams.value.supplierType) count++
  if (queryParams.value.status) count++
  if (queryParams.value.contactPerson) count++
  if (queryParams.value.contactPhone) count++
  if (queryParams.value.unifiedCreditCode) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

const list = ref([])
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const dateRange = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const enterpriseLoading = ref(false)
const showStatusHelp = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, supplierCode: undefined, supplierName: undefined, supplierType: undefined, status: undefined, contactPerson: undefined, contactPhone: undefined, unifiedCreditCode: undefined, params: {} },
  rules: { supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }] }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listSupplier(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; loadStatusCounts(); applySavedWidths() }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.supplierCode = undefined; queryParams.value.supplierName = undefined; queryParams.value.supplierType = undefined; queryParams.value.status = undefined; queryParams.value.contactPerson = undefined; queryParams.value.contactPhone = undefined; queryParams.value.unifiedCreditCode = undefined; dateRange.value = []; queryParams.value.params = {}; activeStatusTab.value = 'all'; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.supplierId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { supplierId: undefined, supplierCode: undefined, supplierName: undefined, supplierShortName: undefined, supplierType: '0', supplierLevel: undefined, unifiedCreditCode: undefined, contactPerson: undefined, contactPhone: undefined, address: undefined, legalPerson: undefined, enterpriseNature: undefined, status: '0', remark: undefined }; proxy.resetForm('supplierRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加供应商' }
function handleUpdate(row) { reset(); getSupplier(row.supplierId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改供应商' }) }
function handleView(row) { getSupplier(row.supplierId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['supplierRef'].validate(valid => { if (valid) { if (form.value.supplierId != undefined) { updateSupplier(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addSupplier(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const supplierIds = row.supplierId || ids.value; proxy.$modal.confirm('确认删除编号为"' + supplierIds + '"的数据？').then(() => delSupplier(supplierIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('wms/supplier/export', { ...proxy.addDateRange(queryParams.value, dateRange.value) }, `supplier_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

function handleEnterpriseLookup() {
  const keyword = form.value.unifiedCreditCode?.trim()
  if (!keyword) { 
    proxy.$modal.msgWarning('请先输入统一社会信用代码'); 
    return 
  }
  // 校验统一社会信用代码格式（18位）
  if (keyword.length !== 18) {
    proxy.$modal.msgError('统一社会信用代码应为18位，请检查输入');
    return
  }
  enterpriseLoading.value = true
  lookupEnterprise(keyword).then(res => {
    const info = res.data
    if (!info || !info.enterpriseName || info.enterpriseName.startsWith('【未找到】')) { 
      const errorMsg = info?.enterpriseStatus || '未查询到企业信息，请确认统一社会信用代码正确'
      proxy.$modal.msgError(errorMsg); 
      return 
    }
    // 回填企业信息
    const filledFields = []
    if (!form.value.supplierName && info.enterpriseName) {
      form.value.supplierName = info.enterpriseName
      filledFields.push('企业名称')
    }
    if (!form.value.legalPerson && info.legalPerson) {
      form.value.legalPerson = info.legalPerson
      filledFields.push('法人代表')
    }
    if (!form.value.enterpriseNature && info.enterpriseNature) {
      form.value.enterpriseNature = info.enterpriseNature
      filledFields.push('企业性质')
    }
    if (!form.value.address && info.address) {
      form.value.address = info.address
      filledFields.push('地址')
    }
    const fillMsg = filledFields.length > 0 ? `已自动回填：${filledFields.join('、')}` : '企业信息查询成功'
    proxy.$modal.msgSuccess(`${fillMsg}（${info.dataSource || '查询成功'}）`)
  }).catch(err => { 
    const errorMsg = err?.message || err?.msg || '企业信息查询失败，请检查网络或API配置'
    proxy.$modal.msgError(errorMsg) 
  }).finally(() => { enterpriseLoading.value = false })
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.pms-supplier-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --violet-50:#f5f3ff; --violet-500:#8b5cf6;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}
.pms-supplier-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-supplier-page .filter-card { padding:14px 20px 16px; }
.pms-supplier-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-supplier-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-supplier-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-supplier-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-supplier-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-supplier-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-supplier-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-supplier-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-supplier-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-supplier-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-supplier-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-supplier-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-supplier-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-supplier-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-supplier-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-supplier-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-supplier-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-supplier-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-supplier-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-supplier-page .field .control :deep(.el-input__prefix .el-icon) { font-size:14px; }
.pms-supplier-page .field .control :deep(.el-select) { width:100%; }
.pms-supplier-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-supplier-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-supplier-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-supplier-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-supplier-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-supplier-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-supplier-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
/* ===== Status Tabs ===== */
.pms-supplier-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.pms-supplier-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.pms-supplier-page .tabs-track::-webkit-scrollbar { display:none; }
.pms-supplier-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; transition:all .15s var(--ease-out); white-space:nowrap; border:1px solid transparent; background:transparent; }
.pms-supplier-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.pms-supplier-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; line-height:1.4; font-feature-settings:"tnum" 1; }
.pms-supplier-page .status-tab:hover { background:var(--ink-50); color:var(--ink-700); }
.pms-supplier-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.pms-supplier-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.pms-supplier-page .status-tab.is-active .dot { background:var(--brand-500); }
.pms-supplier-page .status-tab.tab-approved .dot { background:var(--green-500); }
.pms-supplier-page .status-tab.tab-approved .count { background:var(--green-50); color:var(--green-700); }
.pms-supplier-page .status-tab.is-active.tab-approved .count { background:var(--green-500); color:#fff; }
.pms-supplier-page .status-tab.tab-void .dot { background:var(--ink-400); }
/* ===== Tip Pill ===== */
.pms-supplier-page .tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; border-radius:999px; border:1px solid var(--ink-200); background:#fff; font-size:13px; color:var(--ink-500); cursor:pointer; transition:all .15s var(--ease-out); white-space:nowrap; flex-shrink:0; }
.pms-supplier-page .tip-pill:hover { border-color:var(--brand-200); color:var(--brand-700); background:var(--brand-50); }
.pms-supplier-page .tip-pill .el-icon { font-size:14px; }
/* ===== Toolbar ===== */
.pms-supplier-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-supplier-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-supplier-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-supplier-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-supplier-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-supplier-page .btn-soft .el-icon { font-size:14px; }
.pms-supplier-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-supplier-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-supplier-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-supplier-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-supplier-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-supplier-page .btn-soft:disabled:hover { transform:none; box-shadow:none; }
.pms-supplier-page .table-wrap { overflow-x:auto; }
.pms-supplier-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-supplier-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-supplier-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-supplier-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-supplier-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-supplier-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-supplier-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-supplier-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-supplier-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-supplier-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-supplier-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-supplier-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-supplier-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-supplier-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.pms-supplier-page .badge.amber .dot { background:var(--amber-500); }
.pms-supplier-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.pms-supplier-page .badge.blue .dot { background:var(--blue-500); }
.pms-supplier-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-supplier-page .badge.green .dot { background:var(--green-500); }
.pms-supplier-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-supplier-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-supplier-page .badge.gray .dot { background:var(--ink-400); }
/* ===== Pagination ===== */
.pms-supplier-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-supplier-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-supplier-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-supplier-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-supplier-page .pagination-container :deep(.el-pagination .btn-prev), .pms-supplier-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-supplier-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-supplier-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-supplier-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-supplier-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-supplier-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-supplier-page .toolbar { flex-wrap:wrap; gap:10px; } .pms-supplier-page .status-tabs { padding:6px 8px; } }
/* ===== Status Help Dialog ===== */
.status-help-content { max-height:500px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
