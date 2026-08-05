<template>
  <div class="app-container pms-qualification-page">
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
          <label>资质名称</label>
          <div class="control">
            <el-input v-model="queryParams.qualName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>资质类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.qualType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_qual_type" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="d in pms_qual_status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>资质编号</label>
          <div class="control">
            <el-input v-model="queryParams.qualCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>颁发日期</label>
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['pms:qualification:add']">新增</el-button>
          <button type="button" class="btn-soft is-outline" :disabled="single" @click="handleUpdate" v-hasPermi="['pms:qualification:edit']">
            <el-icon><Edit /></el-icon> 修改
          </button>
          <button type="button" class="btn-soft is-danger-outline" :disabled="multiple" @click="handleDelete" v-hasPermi="['pms:qualification:remove']">
            <el-icon><Delete /></el-icon> 删除
          </button>
          <div class="toolbar-divider"></div>
          <button type="button" class="btn-soft is-outline" @click="handleExport" v-hasPermi="['pms:qualification:export']">
            <el-icon><Download /></el-icon> 导出
          </button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="pms_qualification_columns" />
        </div>
      </div>

      <!-- Table -->
      <div class="table-wrap">
        <el-table ref="tableRef" border v-loading="loading" :data="list" @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" :row-class-name="tableRowClassName" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="供应商" prop="supplierName" key="supplierName" :width="colWidth('supplierName', 240)" resizable show-overflow-tooltip v-if="columns.supplierName.visible" />
          <el-table-column label="资质名称" prop="qualName" key="qualName" :width="colWidth('qualName', 180)" resizable show-overflow-tooltip v-if="columns.qualName.visible" />
          <el-table-column label="资质类型" prop="qualType" key="qualType" :width="colWidth('qualType', 120)" resizable align="center" v-if="columns.qualType.visible"><template #default="scope"><span class="badge violet">{{ qualTypeLabel(scope.row.qualType) }}</span></template></el-table-column>
          <el-table-column label="资质编号" prop="qualCode" key="qualCode" :width="colWidth('qualCode', 140)" resizable v-if="columns.qualCode.visible" />
          <el-table-column label="颁发日期" prop="issueDate" key="issueDate" :width="colWidth('issueDate', 130)" resizable align="center" v-if="columns.issueDate.visible" />
          <el-table-column label="有效期至" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 150)" resizable align="center" v-if="columns.expireDate.visible"><template #default="scope"><span :class="{ 'rd-expire-warn': isExpiringSoon(scope.row), 'rd-expire-alert': isExpired(scope.row) }">{{ scope.row.expireDate }}</span><el-tag v-if="isExpired(scope.row)" type="danger" size="small" style="margin-left: 4px">已过期</el-tag><el-tag v-else-if="isExpiringSoon(scope.row)" type="warning" size="small" style="margin-left: 4px">即将过期</el-tag></template></el-table-column>
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 120)" resizable align="center" v-if="columns.status.visible"><template #default="scope"><span class="badge" :class="badgeClass(scope.row.status)"><span class="dot"></span>{{ statusLabel(scope.row.status) }}</span></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" resizable align="center" sortable="custom" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['pms:qualification:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['pms:qualification:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Pagination -->
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

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
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('pms_qualification_index')
const { collapsedCards, toggleCard } = useDetailCard(['basic', 'date', 'attach'])

// 列显隐配置 - 从 localStorage 恢复保存的设置
const defaultColumns = {
  supplierName: { label: '供应商', visible: true },
  qualName: { label: '资质名称', visible: true },
  qualType: { label: '资质类型', visible: true },
  qualCode: { label: '资质编号', visible: true },
  issueDate: { label: '颁发日期', visible: true },
  expireDate: { label: '有效期至', visible: true },
  status: { label: '状态', visible: true },
  createTime: { label: '创建时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('pms_qualification_columns')
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
  const item = pms_qual_status.value.find(d => d.value == status)
  return item ? item.label : '-'
}
function qualTypeLabel(type) {
  const item = pms_qual_type.value.find(d => d.value == type)
  return item ? item.label : '-'
}

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.supplierName) count++
  if (queryParams.value.qualName) count++
  if (queryParams.value.qualType) count++
  if (queryParams.value.status) count++
  if (queryParams.value.qualCode) count++
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
const baseUrl = import.meta.env.VITE_APP_BASE_API
const showAdvanced = ref(false)
const dateRange = ref([])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, supplierName: undefined, qualName: undefined, qualType: undefined, status: undefined, qualCode: undefined, params: {} },
  rules: { supplierId: [{ required: true, message: '供应商不能为空', trigger: 'change' }], qualName: [{ required: true, message: '资质名称不能为空', trigger: 'blur' }], qualType: [{ required: true, message: '资质类型不能为空', trigger: 'change' }] }
})
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listQualification(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() }) }
function toggleAdvanced() { showAdvanced.value = !showAdvanced.value }
function handleQuery() { queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, dateRange.value, 'IssueDate'); showAdvanced.value = false; getList() }
function resetQuery() { queryParams.value.supplierName = undefined; queryParams.value.qualName = undefined; queryParams.value.qualType = undefined; queryParams.value.status = undefined; queryParams.value.qualCode = undefined; queryParams.value.params = {}; dateRange.value = []; handleQuery() }
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
function handleExport() { proxy.download('pms/qualification/export', { ...proxy.addDateRange(queryParams.value, dateRange.value, 'IssueDate') }, `qualification_${new Date().getTime()}.xlsx`) }
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
/* ===== Design Tokens ===== */
.pms-qualification-page {
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
.pms-qualification-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.pms-qualification-page .filter-card { padding:14px 20px 16px; }
.pms-qualification-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.pms-qualification-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.pms-qualification-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.pms-qualification-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.pms-qualification-page .filter-card .adv-link:hover { color:var(--brand-600); }
.pms-qualification-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.pms-qualification-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.pms-qualification-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.pms-qualification-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.pms-qualification-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.pms-qualification-page .filter-card .filter-buttons { display:flex; gap:8px; }
.pms-qualification-page .field { display:flex; flex-direction:column; gap:6px; }
.pms-qualification-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); display:flex; align-items:center; gap:6px; }
.pms-qualification-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.pms-qualification-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.pms-qualification-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.pms-qualification-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.pms-qualification-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.pms-qualification-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.pms-qualification-page .field .control :deep(.el-select) { width:100%; }
.pms-qualification-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.pms-qualification-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.pms-qualification-page .field .control :deep(.el-select .el-select__wrapper.is-focused) { box-shadow:none !important; }
.pms-qualification-page .field .control :deep(.el-date-editor) { width:100%; }
.pms-qualification-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.pms-qualification-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.pms-qualification-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.pms-qualification-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.pms-qualification-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.pms-qualification-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.pms-qualification-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.pms-qualification-page .btn-soft { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; font-size:14px; font-weight:500; border-radius:var(--r-sm); border:1px solid transparent; cursor:pointer; user-select:none; transition:all .15s var(--ease-out); }
.pms-qualification-page .btn-soft .el-icon { font-size:14px; }
.pms-qualification-page .btn-soft.is-outline { background:#fff; color:var(--ink-700); border-color:var(--ink-200); }
.pms-qualification-page .btn-soft.is-outline:hover { background:var(--ink-50); border-color:var(--ink-300); color:var(--ink-900); }
.pms-qualification-page .btn-soft.is-danger-outline { background:#fff; color:var(--red-700); border-color:#fecaca; }
.pms-qualification-page .btn-soft.is-danger-outline:hover { background:var(--red-50); border-color:var(--red-500); }
.pms-qualification-page .btn-soft:disabled { opacity:.5; cursor:not-allowed; }
.pms-qualification-page .table-wrap { overflow-x:auto; }
.pms-qualification-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.pms-qualification-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.pms-qualification-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; }
.pms-qualification-page .app-table :deep(.el-table__header th:hover) { border-right-color:var(--ink-200) !important; }
.pms-qualification-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; letter-spacing:.02em; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.pms-qualification-page .app-table :deep(.el-table__header th .cell) { text-transform:uppercase; }
.pms-qualification-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.pms-qualification-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.pms-qualification-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.pms-qualification-page .app-table :deep(.el-table__border-left-patch) { display:none; }
.pms-qualification-page .app-table .rd-amount { font-feature-settings:"tnum" 1; font-variant-numeric:tabular-nums; color:var(--ink-900); font-weight:500; }
/* ===== Badges ===== */
.pms-qualification-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.pms-qualification-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.pms-qualification-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.pms-qualification-page .badge.green .dot { background:var(--green-500); }
.pms-qualification-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.pms-qualification-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.pms-qualification-page .badge.gray .dot { background:var(--ink-400); }
.pms-qualification-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; font-size:14px; color:var(--ink-500); background:#fff; border-top:1px solid transparent; }
.pms-qualification-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.pms-qualification-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; font-size:14px; color:var(--ink-700); margin:0 2px; }
.pms-qualification-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; box-shadow:0 4px 10px -2px rgba(79,70,229,.4); }
.pms-qualification-page .pagination-container :deep(.el-pagination .btn-prev), .pms-qualification-page .pagination-container :deep(.el-pagination .btn-next) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; }
.pms-qualification-page .pagination-container :deep(.el-pagination .btn-prev:hover), .pms-qualification-page .pagination-container :deep(.el-pagination .btn-next:hover) { border-color:var(--brand-200); color:var(--brand-700); }
.pms-qualification-page .pagination-container :deep(.el-pagination .el-pagination__sizes .el-select__wrapper) { border-radius:6px; box-shadow:0 0 0 1px var(--ink-200) inset; }
@media (max-width:1100px) { .pms-qualification-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .pms-qualification-page .filter-card .filter-bar { grid-template-columns:1fr; } .pms-qualification-page .toolbar { flex-wrap:wrap; gap:10px; } }
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
