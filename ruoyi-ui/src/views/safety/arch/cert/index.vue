<template>
  <div class="app-container safety-cert-page">
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
          <label>证照名称</label>
          <div class="control">
            <el-input v-model="queryParams.certName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>证照编号</label>
          <div class="control">
            <el-input v-model="queryParams.certNo" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>证照类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.certType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_license_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>发证机关</label>
          <div class="control">
            <el-input v-model="queryParams.issueOrg" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>状态</label>
          <div class="control is-select">
            <el-select v-model="queryParams.status" placeholder="全部" clearable @change="handleQuery">
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
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
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:cert:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:cert:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:cert:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_cert_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="certList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="证照名称" prop="certName" key="certName" :width="colWidth('certName', 180)" resizable show-overflow-tooltip v-if="columns.certName.visible" />
          <el-table-column label="证照编号" prop="certNo" key="certNo" :width="colWidth('certNo', 160)" resizable v-if="columns.certNo.visible" />
          <el-table-column label="证照类型" prop="certType" key="certType" :width="colWidth('certType', 120)" resizable align="center" v-if="columns.certType.visible">
            <template #default="scope">
              <span class="badge violet">{{ certTypeLabel(scope.row.certType) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="发证机关" prop="issueOrg" key="issueOrg" :width="colWidth('issueOrg', 160)" resizable show-overflow-tooltip v-if="columns.issueOrg.visible" />
          <el-table-column label="发证日期" prop="issueDate" key="issueDate" :width="colWidth('issueDate', 130)" resizable align="center" sortable="custom" v-if="columns.issueDate.visible" />
          <el-table-column label="有效期至" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 130)" resizable align="center" sortable="custom" v-if="columns.expireDate.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.remindStatus === '1' ? 'red' : 'green'">
                <span class="dot"></span>{{ scope.row.expireDate }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:cert:query']">查看</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:cert:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:cert:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="certRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>证照信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="证照名称" prop="certName"><el-input v-model="form.certName" placeholder="请输入证照名称" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="证照编号" prop="certNo"><el-input v-model="form.certNo" placeholder="请输入证照编号" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="证照类型" prop="certType"><el-select v-model="form.certType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_license_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发证机关" prop="issueOrg"><el-input v-model="form.issueOrg" placeholder="请输入发证机关" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="发证日期" prop="issueDate"><el-date-picker v-model="form.issueDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="有效期至" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-form-item label="附件" prop="attachment"><file-upload v-model="form.attachment" :fileSize="50" /></el-form-item>
              <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" /></el-form-item>
            </div>
          </section>
        </div>
      </el-form>
      <template #footer><div class="dialog-footer"><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></div></template>
    </el-dialog>

    <!-- ===== 查看详情弹窗 ===== -->
    <el-dialog v-model="viewOpen" width="860px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/></svg></div>
          <span class="rd-detail-header-title">证照详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.certNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.certNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg></span>证照信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">证照名称</span><div class="rd-value">{{ viewData.certName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">证照编号</span><div class="rd-value">{{ viewData.certNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">证照类型</span><div class="rd-value"><dict-tag :options="safety_license_type" :value="viewData.certType" /></div></div>
              <div class="rd-item"><span class="rd-label">发证机关</span><div class="rd-value">{{ viewData.issueOrg || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">发证日期</span><div class="rd-value">{{ viewData.issueDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">有效期至</span><div class="rd-value"><span class="badge" :class="viewData.remindStatus === '1' ? 'red' : 'green'"><span class="dot"></span>{{ viewData.expireDate || '-' }}</span></div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.attachment || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>其他信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item rd-item--full"><span class="rd-label">附件</span><div class="rd-value"><div class="rd-file-links" v-if="viewData.attachment">
<div class="rd-file-item" v-for="(url, idx) in String(viewData.attachment).split(',')" :key="idx">
<div class="rd-file-link" @click="handleFilePreview(url)">
<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
<span class="rd-file-name">{{ url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url }}</span>
</div>
<span class="rd-file-dl" @click="handleFileDownload(url)">
<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
下载
</span>
</div>
            </div></div></div>
              <div class="rd-item rd-item--full"><span class="rd-label">备注</span><div class="rd-value">{{ viewData.remark || '-' }}</div></div>
            </div>
          </div>
        </section>
      </div>
      <template #footer><el-button @click="viewOpen = false">关 闭</el-button></template>
    </el-dialog>
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="SafetyCert">
import { listCert, getCert, addCert, updateCert, delCert } from '@/api/safety/cert'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, ArrowDown } from '@element-plus/icons-vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { downloadFile } from '@/utils/downloadFile'

const { proxy } = getCurrentInstance()
const { safety_license_type } = proxy.useDict('safety_license_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_cert_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","vc0","vc1"])

const certList = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
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

const defaultColumns = {
  certName: { label: '证照名称', visible: true },
  certNo: { label: '证照编号', visible: true },
  certType: { label: '证照类型', visible: true },
  issueOrg: { label: '发证机关', visible: true },
  issueDate: { label: '发证日期', visible: true },
  expireDate: { label: '有效期至', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_cert_columns')
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
  queryParams: { pageNum: 1, pageSize: 10, certName: undefined, certNo: undefined, certType: undefined, issueOrg: undefined, status: undefined, params: {} },
  rules: {
    certName: [{ required: true, message: '证照名称不能为空', trigger: 'blur' }],
    certNo: [{ required: true, message: '证照编号不能为空', trigger: 'blur' }],
    expireDate: [{ required: true, message: '有效期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.certName) count++
  if (queryParams.value.certNo) count++
  if (queryParams.value.certType) count++
  if (queryParams.value.issueOrg) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length > 0) count++
  return count
})

function handleFilePreview(url) {
const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
proxy.$refs.filePreviewRef.open(url, name)
}
function handleFileDownload(url) {
downloadFile(url)
}

function getList() { loading.value = true; listCert(queryParams.value).then(response => { certList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() }).catch(error => { console.error(error) }).finally(() => { loading.value = false }) }
function handleQuery() { showAdvanced.value = false; proxy.addDateRange(queryParams.value, dateRange.value); queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.certName = undefined; queryParams.value.certNo = undefined; queryParams.value.certType = undefined; queryParams.value.issueOrg = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.certId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; open.value = true; title.value = '添加证照' }
function handleView(row) { const certId = row.certId || ids.value[0]; getCert(certId).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function handleUpdate(row) { reset(); getCert(row.certId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.attachment && !response.data.remark; open.value = true; title.value = '修改证照' }) }
function submitForm() {
  proxy.$refs['certRef'].validate(valid => {
    if (valid) {
      if (form.value.certId != undefined) { updateCert(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addCert(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const certIds = row.certId || ids.value; proxy.$modal.confirm('是否确认删除证照？').then(function() { return delCert(certIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/cert/export', { ...queryParams.value }, `cert_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { certId: undefined, certName: undefined, certNo: undefined, certType: undefined, issueOrg: undefined, issueDate: undefined, expireDate: undefined, attachment: undefined, remark: undefined }
  proxy.resetForm('certRef')
}

function certTypeLabel(type) { const item = safety_license_type.value.find(d => d.value == type); return item ? item.label : '-' }

getList()
</script>

<style scoped>
.safety-cert-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-cert-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-cert-page .filter-card { padding:14px 20px 16px; }
.safety-cert-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-cert-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-cert-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-cert-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-cert-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-cert-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-cert-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-cert-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-cert-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-cert-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-cert-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-cert-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-cert-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-cert-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-cert-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-cert-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-cert-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-cert-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-cert-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-cert-page .field .control :deep(.el-select) { width:100%; }
.safety-cert-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-cert-page .field .control :deep(.el-date-editor) { width:100%; }
.safety-cert-page .field .control :deep(.el-date-editor .el-range-input) { background:transparent; border:0; font-size:14px; color:var(--ink-900); }
.safety-cert-page .field .control :deep(.el-date-editor .el-range-separator) { color:var(--ink-400); }
.safety-cert-page .field .control :deep(.el-date-editor .el-range__icon) { color:var(--ink-400); }
.safety-cert-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-cert-page .toolbar .left, .safety-cert-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-cert-page .toolbar-divider { width:1px; height:18px; background:var(--ink-200); margin:0 4px; }
.safety-cert-page .table-wrap { overflow-x:auto; }
.safety-cert-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-cert-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-cert-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); }
.safety-cert-page .app-table :deep(.el-table__body td) { padding:14px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-cert-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-cert-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-cert-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-cert-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; } .safety-cert-page .badge.amber .dot { background:var(--amber-500); }
.safety-cert-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; } .safety-cert-page .badge.blue .dot { background:var(--blue-500); }
.safety-cert-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-cert-page .badge.green .dot { background:var(--green-500); }
.safety-cert-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-cert-page .badge.red .dot { background:var(--red-500); }
.safety-cert-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-cert-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); } .safety-cert-page .badge.gray .dot { background:var(--ink-400); }
.safety-cert-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-cert-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-cert-page .filter-card .filter-bar { grid-template-columns:1fr; } }

/* 操作列按钮对齐：每行2个按钮，flex-wrap 自动换行，按钮自适应内容宽度 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }
:deep(.col-action .el-button + .el-button) { margin-left: 2px; }
</style>
