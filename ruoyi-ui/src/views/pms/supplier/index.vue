<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="供应商编码" prop="supplierCode"><el-input v-model="queryParams.supplierCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="供应商名称" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="供应商类型" prop="supplierType"><el-select v-model="queryParams.supplierType" placeholder="全部" clearable style="width: 160px"><el-option v-for="d in wms_supplier_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px"><el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
<el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain :icon="showAdvanced ? 'ArrowUp' : 'ArrowDown'" @click="showAdvanced = !showAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
<!-- 高级查询条件（内联展开） -->
<el-form-item label="联系人" prop="contactPerson" v-show="showAdvanced"><el-input v-model="queryParams.contactPerson" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
<el-form-item label="联系电话" prop="contactPhone" v-show="showAdvanced"><el-input v-model="queryParams.contactPhone" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
<el-form-item label="统一信用码" prop="unifiedCreditCode" v-show="showAdvanced"><el-input v-model="queryParams.unifiedCreditCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
<el-form-item label="创建时间" v-show="showAdvanced"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
</el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['wms:supplier:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['wms:supplier:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['wms:supplier:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['wms:supplier:export']">导出</el-button></el-col>
      
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商编码" prop="supplierCode" :width="colWidth('supplierCode', 150)" resizable sortable="custom" />
      <el-table-column label="供应商名称" prop="supplierName" :width="colWidth('supplierName', 220)" resizable show-overflow-tooltip />
      <el-table-column label="简称" prop="supplierShortName" :width="colWidth('supplierShortName', 120)" resizable show-overflow-tooltip />
      <el-table-column label="类型" prop="supplierType" :width="colWidth('supplierType', 90)" resizable align="center"><template #default="scope"><dict-tag :options="wms_supplier_type" :value="scope.row.supplierType" /></template></el-table-column>
      <el-table-column label="等级" prop="supplierLevel" :width="colWidth('supplierLevel', 90)" resizable align="center" sortable="custom"><template #default="scope"><dict-tag :options="wms_supplier_level" :value="scope.row.supplierLevel" /></template></el-table-column>
      <el-table-column label="联系人" prop="contactPerson" :width="colWidth('contactPerson', 100)" resizable />
      <el-table-column label="联系电话" prop="contactPhone" :width="colWidth('contactPhone', 130)" resizable />
      <el-table-column label="统一信用代码" prop="unifiedCreditCode" :width="colWidth('unifiedCreditCode', 180)" resizable show-overflow-tooltip />
      <el-table-column label="状态" prop="status" :width="colWidth('status', 80)" resizable align="center" sortable="custom"><template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['wms:supplier:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['wms:supplier:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

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
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_supplier_index')

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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, supplierCode: undefined, supplierName: undefined, supplierType: undefined, status: undefined, contactPerson: undefined, contactPhone: undefined, unifiedCreditCode: undefined, params: {} },
  rules: { supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }] }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listSupplier(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.supplierId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { supplierId: undefined, supplierCode: undefined, supplierName: undefined, supplierShortName: undefined, supplierType: '0', supplierLevel: undefined, unifiedCreditCode: undefined, contactPerson: undefined, contactPhone: undefined, address: undefined, legalPerson: undefined, enterpriseNature: undefined, status: '0', remark: undefined }; proxy.resetForm('supplierRef') }
function handleAdd() { reset(); open.value = true; title.value = '添加供应商' }
function handleUpdate(row) { reset(); getSupplier(row.supplierId || ids.value[0]).then(res => { form.value = res.data; open.value = true; title.value = '修改供应商' }) }
function handleView(row) { getSupplier(row.supplierId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['supplierRef'].validate(valid => { if (valid) { if (form.value.supplierId != undefined) { updateSupplier(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addSupplier(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const supplierIds = row.supplierId || ids.value; proxy.$modal.confirm('确认删除编号为"' + supplierIds + '"的数据？').then(() => delSupplier(supplierIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('wms/supplier/export', { ...queryParams.value }, `supplier_${new Date().getTime()}.xlsx`) }
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
