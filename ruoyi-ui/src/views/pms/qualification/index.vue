<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="供应商" prop="supplierName"><el-input v-model="queryParams.supplierName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="资质名称" prop="qualName"><el-input v-model="queryParams.qualName" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="资质类型" prop="qualType"><el-select v-model="queryParams.qualType" placeholder="全部" clearable style="width: 160px"><el-option v-for="d in pms_qual_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px"><el-option v-for="d in pms_qual_status" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
      <el-form-item label="资质编号" prop="qualCode" v-show="showAdvanced"><el-input v-model="queryParams.qualCode" placeholder="请输入" clearable style="width: 200px" @keyup.enter="handleQuery" /></el-form-item>
      <el-form-item label="颁发日期" v-show="showAdvanced"><el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" /></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button><el-button type="info" plain :icon="showAdvanced ? 'ArrowUp' : 'ArrowDown'" @click="toggleAdvanced">{{ showAdvanced ? '收起' : '更多' }}</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:qualification:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:qualification:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:qualification:remove']">删除</el-button></el-col>
      <el-col :span="1.5"><el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['pms:qualification:export']">导出</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" :row-class-name="tableRowClassName">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="供应商" prop="supplierName" :width="colWidth('supplierName', 200)" resizable show-overflow-tooltip />
      <el-table-column label="资质名称" prop="qualName" :width="colWidth('qualName', 180)" resizable show-overflow-tooltip />
      <el-table-column label="资质类型" prop="qualType" :width="colWidth('qualType', 120)" resizable align="center"><template #default="scope"><dict-tag :options="pms_qual_type" :value="scope.row.qualType" /></template></el-table-column>
      <el-table-column label="资质编号" prop="qualCode" :width="colWidth('qualCode', 140)" resizable />
      <el-table-column label="颁发日期" prop="issueDate" :width="colWidth('issueDate', 110)" resizable align="center" />
      <el-table-column label="有效期至" prop="expireDate" :width="colWidth('expireDate', 130)" resizable align="center"><template #default="scope"><span :class="{ 'rd-expire-warn': isExpiringSoon(scope.row), 'rd-expire-alert': isExpired(scope.row) }">{{ scope.row.expireDate }}</span><el-tag v-if="isExpired(scope.row)" type="danger" size="small" style="margin-left: 4px">已过期</el-tag><el-tag v-else-if="isExpiringSoon(scope.row)" type="warning" size="small" style="margin-left: 4px">即将过期</el-tag></template></el-table-column>
      <el-table-column label="状态" prop="status" :width="colWidth('status', 100)" resizable align="center"><template #default="scope"><dict-tag :options="pms_qual_status" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="创建时间" prop="createTime" :width="colWidth('createTime', 160)" resizable align="center" sortable="custom" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:qualification:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:qualification:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="qualRef" :model="form" :rules="rules" label-width="110px">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('basic')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.basic }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.basic">
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width: 100%" @change="onSupplierChange"><el-option v-for="s in supplierOptions" :key="s.supplierId" :label="s.supplierName" :value="s.supplierId" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="资质名称" prop="qualName"><el-input v-model="form.qualName" placeholder="请输入" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="资质类型" prop="qualType"><el-select v-model="form.qualType" placeholder="请选择" style="width: 100%"><el-option v-for="d in pms_qual_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="资质编号" prop="qualCode"><el-input v-model="form.qualCode" placeholder="请输入" /></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('date')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期与状态</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.date }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.date">
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="颁发日期" prop="issueDate"><el-date-picker v-model="form.issueDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col><el-col :span="12"><el-form-item label="有效期至" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" placeholder="请选择" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col></el-row>
            <el-row :gutter="20"><el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio v-for="d in pms_qual_status" :key="d.value" :value="d.value">{{ d.label }}</el-radio></el-radio-group></el-form-item></el-col></el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('attach')">
            <div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div>
            <button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.attach }"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button>
          </div>
          <div class="rd-card-body" v-show="!collapsedCards.attach">
            <el-form-item label="资质文件" prop="fileUrl"><file-upload v-model="form.fileUrl" :limit="1" :fileSize="10" :fileType="['png','jpg','jpeg','gif','bmp','webp','pdf','doc','docx']" /></el-form-item>
            <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" placeholder="请输入" /></el-form-item>
          </div>
        </section>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>

    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div><span class="rd-detail-header-title">资质详情</span><div class="rd-detail-header-sub" v-if="viewData.qualCode"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.qualCode }}</span></div></div></template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>基本信息</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">供应商</span><div class="rd-value">{{ viewData.supplierName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">资质名称</span><div class="rd-value">{{ viewData.qualName || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">资质类型</span><div class="rd-value"><dict-tag :options="pms_qual_type" :value="viewData.qualType" /></div></div>
            <div class="rd-item"><span class="rd-label">资质编号</span><div class="rd-value">{{ viewData.qualCode || '-' }}</div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期与状态</div></div>
          <div class="rd-card-body" style="display:block"><div class="rd-grid">
            <div class="rd-item"><span class="rd-label">颁发日期</span><div class="rd-value">{{ viewData.issueDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">有效期至</span><div class="rd-value">{{ viewData.expireDate || '-' }}</div></div>
            <div class="rd-item"><span class="rd-label">状态</span><div class="rd-value"><dict-tag :options="pms_qual_status" :value="viewData.status" /></div></div>
          </div></div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></span>附件与备注</div></div>
          <div class="rd-card-body" style="display:block">
            <template v-if="viewData.fileUrl">
              <div class="rd-grid">
                <div class="rd-item rd-item--full" v-for="(file, idx) in viewData.fileUrl.split(',')" :key="idx">
                  <span class="rd-label">资质文件</span>
                  <div class="rd-value">
                    <el-link :href="baseUrl + file" :underline="false" target="_blank" type="primary"><el-icon><Download /></el-icon> {{ getFileName(file) }}</el-link>
                    <el-button link type="success" icon="View" size="small" style="margin-left: 12px" @click="handlePreview(file)">预览</el-button>
                  </div>
                </div>
              </div>
            </template>
            <div class="rd-empty" v-else>
              <svg class="rd-empty-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
              <p class="rd-empty-text">暂无附件</p>
            </div>
            <div class="rd-grid" style="margin-top: 12px">
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>

    <file-preview ref="filePreviewRef" />

  </div>
</template>

<script setup name="PmsQualification">
import { listQualification, getQualification, addQualification, updateQualification, delQualification } from '@/api/pms/qualification'
import { listSupplier } from '@/api/wms/supplier'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import useDictStore from '@/store/modules/dict'

const { proxy } = getCurrentInstance()
useDictStore().removeDict('pms_qual_status')
const { pms_qual_type, pms_qual_status } = proxy.useDict('pms_qual_type', 'pms_qual_status')
const { colWidth, onHeaderDragEnd, tableRef } = useColumnResize('pms_qualification_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'date', 'attach'])

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
const baseUrl = import.meta.env.VITE_APP_BASE_API
const showAdvanced = ref(false)
const dateRange = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, supplierName: undefined, qualName: undefined, qualType: undefined, status: undefined, qualCode: undefined, params: {} },
  rules: { supplierId: [{ required: true, message: '供应商不能为空', trigger: 'change' }], qualName: [{ required: true, message: '资质名称不能为空', trigger: 'blur' }], qualType: [{ required: true, message: '资质类型不能为空', trigger: 'change' }] }
})
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listQualification(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false }) }
function toggleAdvanced() { showAdvanced.value = !showAdvanced.value }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'IssueDate'); showAdvanced.value = false; getList() }
function resetQuery() { proxy.resetForm('queryRef'); queryParams.value.params = {}; dateRange.value = []; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(i => i.qualId); single.value = selection.length !== 1; multiple.value = !selection.length }
function reset() { form.value = { qualId: undefined, supplierId: undefined, supplierName: undefined, qualName: undefined, qualType: undefined, qualCode: undefined, issueDate: undefined, expireDate: undefined, status: '1', fileUrl: undefined, fileName: undefined, remark: undefined }; proxy.resetForm('qualRef') }
function onSupplierChange(val) { const matched = supplierOptions.value.find(s => s.supplierId === val); form.value.supplierName = matched ? matched.supplierName : undefined }
function loadSupplierOptions() { listSupplier({ pageNum: 1, pageSize: 999 }).then(res => { supplierOptions.value = res.rows || [] }) }
function getFileName(url) { if (url.lastIndexOf('/') > -1) { return url.slice(url.lastIndexOf('/') + 1) } return url }
function handlePreview(fileUrl) { proxy.$refs.filePreviewRef.open(fileUrl, getFileName(fileUrl)) }
function handleAdd() { reset(); open.value = true; title.value = '添加资质' }
function handleUpdate(row) { reset(); getQualification(row.qualId).then(res => { form.value = res.data; open.value = true; title.value = '修改资质' }) }
function handleView(row) { getQualification(row.qualId).then(res => { viewData.value = res.data; viewOpen.value = true }) }
function submitForm() { proxy.$refs['qualRef'].validate(valid => { if (valid) { if (form.value.qualId != undefined) { updateQualification(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) } else { addQualification(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) } } }) }
function handleDelete(row) { const qualIds = row.qualId || ids.value; proxy.$modal.confirm('确认删除编号为"' + qualIds + '"的数据？').then(() => delQualification(qualIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('pms/qualification/export', { ...queryParams.value }, `qualification_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }

/** 判断资质是否已过期 */
function isExpired(row) {
  if (!row.expireDate) return false
  return new Date(row.expireDate) < new Date(new Date().toDateString())
}
/** 判断资质是否即将过期（30天内） */
function isExpiringSoon(row) {
  if (!row.expireDate || isExpired(row)) return false
  const diff = new Date(row.expireDate) - new Date(new Date().toDateString())
  return diff > 0 && diff <= 30 * 24 * 60 * 60 * 1000
}
/** 表格行样式：过期资质高亮 */
function tableRowClassName({ row }) {
  if (isExpired(row)) return 'rd-row-expired'
  if (isExpiringSoon(row)) return 'rd-row-expiring'
  return ''
}

loadSupplierOptions()
getList()
</script>

<style scoped>
/* 过期资质行高亮 - 红色背景 */
:deep(.rd-row-expired) {
  background-color: #fef0f0 !important;
}
:deep(.rd-row-expired:hover > td) {
  background-color: #fde2e2 !important;
}
/* 即将过期行高亮 - 黄色背景 */
:deep(.rd-row-expiring) {
  background-color: #fdf6ec !important;
}
:deep(.rd-row-expiring:hover > td) {
  background-color: #faecd8 !important;
}
/* 过期日期文字样式 */
.rd-expire-alert {
  color: #f56c6c;
  font-weight: bold;
}
.rd-expire-warn {
  color: #e6a23c;
  font-weight: bold;
}
</style>
