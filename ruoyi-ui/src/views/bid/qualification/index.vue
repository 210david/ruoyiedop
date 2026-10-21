<template>
  <div class="app-container bid-qualification-page">
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
        <a class="adv-link" :class="{ 'is-open': showAdvanced }" @click.prevent="showAdvanced = !showAdvanced">
          <span>{{ showAdvanced ? '收起' : '高级筛选' }}</span><el-icon class="chev"><ArrowDown /></el-icon>
        </a>
      </div>
      <div class="filter-bar">
        <div class="field"><label>名称/证书编号</label><div class="control"><el-input v-model="queryParams.name" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field"><label>资料类型</label><div class="control"><el-select v-model="queryParams.qualType" placeholder="全部" clearable style="width:100%"><el-option v-for="d in bid_qual_type" :key="d.value" :label="d.label" :value="d.value" /></el-select></div></div>
        <div class="field"><label>到期状态</label><div class="control"><el-select v-model="queryParams.expireStatus" placeholder="全部" clearable style="width:100%"><el-option label="30天内到期" value="soon30" /><el-option label="31-60天到期" value="soon60" /><el-option label="长期有效" value="long" /></el-select></div></div>
        <div class="field"><label>发证日期</label><div class="control"><el-date-picker v-model="issueRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:100%" /></div></div>
        <div class="field" v-show="showAdvanced"><label>持有人</label><div class="control"><el-input v-model="queryParams.ownerName" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
        <div class="field" v-show="showAdvanced"><label>资质等级</label><div class="control"><el-input v-model="queryParams.qualLevel" placeholder="请输入" clearable @keyup.enter="handleQuery" /></div></div>
      </div>
      <div class="filter-actions">
        <div class="filter-info"><el-icon><Filter /></el-icon> 已选 {{ activeFilterCount }} 个条件，支持回车快速搜索</div>
        <div class="filter-buttons"><el-button icon="RefreshLeft" @click="resetQuery">重置</el-button><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button></div>
      </div>
    </div>

    <div class="surface">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeTab === 'all' }" @click="handleTabClick('all')"><span class="dot"></span><span>全部</span><span class="count">{{ tabCounts.all || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '1' }" @click="handleTabClick('1')"><span class="dot" style="background:var(--brand-500)"></span><span>企业资质</span><span class="count">{{ tabCounts['1'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '2' }" @click="handleTabClick('2')"><span class="dot" style="background:var(--blue-500)"></span><span>人员证书</span><span class="count">{{ tabCounts['2'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '3' }" @click="handleTabClick('3')"><span class="dot" style="background:var(--green-500)"></span><span>工程业绩</span><span class="count">{{ tabCounts['3'] || 0 }}</span></button>
          <button class="status-tab" :class="{ 'is-active': activeTab === '4' }" @click="handleTabClick('4')"><span class="dot" style="background:var(--amber-500)"></span><span>获奖荣誉</span><span class="count">{{ tabCounts['4'] || 0 }}</span></button>
        </div>
        <button class="tip-pill" @click="showStatusHelp = true"><el-icon><QuestionFilled /></el-icon><span>业务操作说明</span></button>
      </div>
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['bid:qualification:add']">登记资质</el-button>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['bid:qualification:export']">导出</el-button>
        </div>
        <div class="right"><right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar></div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="list" border class="app-table" @header-dragend="onHeaderDragEnd">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="名称" prop="name" key="name" :min-width="colWidth('name', 240)" show-overflow-tooltip resizable />
          <el-table-column label="类型" prop="qualType" key="qualType" :width="colWidth('qualType', 110)" resizable align="center">
            <template #default="scope"><span class="badge" :class="typeBadge(scope.row.qualType)">{{ typeLabel(scope.row.qualType) }}</span></template>
          </el-table-column>
          <el-table-column label="证书编号" prop="certNo" key="certNo" :width="colWidth('certNo', 180)" resizable align="center" />
          <el-table-column label="资质等级" prop="qualLevel" key="qualLevel" :min-width="colWidth('qualLevel', 180)" show-overflow-tooltip resizable />
          <el-table-column label="持有人" prop="ownerName" key="ownerName" :width="colWidth('ownerName', 110)" resizable align="center">
            <template #default="scope">{{ scope.row.ownerType === '1' ? '企业' : (scope.row.ownerName || '—') }}</template>
          </el-table-column>
          <el-table-column label="发证日期" prop="issueDate" key="issueDate" :width="colWidth('issueDate', 120)" resizable align="center" />
          <el-table-column label="有效期至" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 130)" resizable align="center">
            <template #default="scope">{{ scope.row.expireDate || '长期' }}</template>
          </el-table-column>
          <el-table-column label="到期预警" prop="remain" key="remain" :width="colWidth('remain', 120)" resizable align="center">
            <template #default="scope"><span class="badge" :class="remainBadge(scope.row)">{{ remainText(scope.row) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
                <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['bid:qualification:edit']">修改</el-button>
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['bid:qualification:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <el-dialog v-model="open" width="860px" append-to-body draggable class="rd-dialog">
      <template #header><div class="rd-detail-header"><div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c3 3 9 3 12 0v-5"/></svg></div><span class="rd-detail-header-title">{{ title }}</span></div></template>
      <el-form ref="qualRef" :model="form" :rules="rules" label-width="110px" :disabled="isView">
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg></span>资质信息</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="24"><el-form-item label="资料类型" prop="qualType">
                <el-radio-group v-model="form.qualType"><el-radio v-for="d in bid_qual_type" :key="d.value" :value="d.value">{{ d.label }}</el-radio></el-radio-group>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="资质/证书/业绩名称" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="证书编号" prop="certNo"><el-input v-model="form.certNo" placeholder="请输入" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="资质等级" prop="qualLevel"><el-input v-model="form.qualLevel" placeholder="如：建筑工程施工总承包贰级" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21V8l-8-5-8 5v13"/><path d="M2 21h20"/><path d="M9 21v-6h6v6"/></svg></span>归属与附件</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="归属" prop="ownerType">
                <el-radio-group v-model="form.ownerType" @change="form.ownerUserId = undefined; form.ownerName = undefined">
                  <el-radio value="1">企业</el-radio><el-radio value="2">人员</el-radio>
                </el-radio-group>
              </el-form-item></el-col>
              <el-col :span="12"><el-form-item label="持有人" prop="ownerName" v-if="form.ownerType === '2'">
                <el-input v-model="form.ownerName" readonly placeholder="请选择持有人" @click="openUserPicker">
                  <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                </el-input>
              </el-form-item></el-col>
              <el-col :span="24"><el-form-item label="扫描件" prop="fileUrl"><file-upload v-model="form.fileUrl" :disabled="isView" :fileSize="10" :fileType="['png','jpg','jpeg','gif','bmp','webp','pdf','doc','docx']" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
        <section class="rd-card">
          <div class="rd-card-header"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期与预警</div></div>
          <div class="rd-card-body" style="display:block">
            <el-row :gutter="20">
              <el-col :span="12"><el-form-item label="发证日期" prop="issueDate"><el-date-picker v-model="form.issueDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="有效期至" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" placeholder="留空=长期有效" style="width:100%" /></el-form-item></el-col>
              <el-col :span="12"><el-form-item label="预警天数" prop="warnDays"><el-input-number v-model="form.warnDays" :min="7" :step="30" style="width:100%" /></el-form-item></el-col>
            </el-row>
          </div>
        </section>
      </el-form>
      <template #footer>
        <template v-if="isView"><el-button type="primary" @click="cancel">关 闭</el-button></template>
        <template v-else><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
      </template>
    </el-dialog>

    <el-dialog v-model="showStatusHelp" title="资质资料库业务操作说明" width="820px" append-to-body>
      <div class="status-help-content">
        <h4>一、业务释义</h4>
        <div class="highlight-card highlight-primary"><div class="highlight-card-title">为什么要集中管理资质？</div>
          <div class="highlight-card-body"><p>企业资质、人员证书、工程业绩是投标的"弹药库"。集中归档+有效期监控，从源头避免用过期证书投标导致废标。</p></div></div>
        <h4>二、预警机制</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="红色（<30天）">紧急临期，立即安排续期</el-descriptions-item>
          <el-descriptions-item label="橙色（30~60天）">提前预警，开始准备年审材料</el-descriptions-item>
          <el-descriptions-item label="每日扫描">系统每日自动检查并推送提醒到负责人</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer><el-button type="primary" @click="showStatusHelp = false">我知道了</el-button></template>
    </el-dialog>

    <user-picker ref="userPickerRef" title="选择持有人" @confirm="onUserPickerConfirm" />
  </div>
</template>

<script setup name="BidQualification">
import { listQualification, getQualification, addQualification, updateQualification, delQualification } from '@/api/bid/qualification'
import { useColumnResize } from '@/composables/useColumnResize'
import UserPicker from '@/components/UserPicker/index.vue'

const { proxy } = getCurrentInstance()
const { bid_qual_type } = proxy.useDict('bid_qual_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('bid_qualification_index')

const loading = ref(true)
const list = ref([])
const total = ref(0)
const open = ref(false)
const isView = ref(false)
const title = ref('')
const showSearch = ref(true)
const issueRange = ref([])
const showAdvanced = ref(false)
const activeTab = ref('all')
const tabCounts = ref({})
const showStatusHelp = ref(false)
const userPickerRef = ref(null)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, name: undefined, qualType: undefined, expireStatus: undefined, ownerName: undefined, params: {} },
  rules: {
    qualType: [{ required: true, message: '资料类型不能为空', trigger: 'change' }],
    name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    certNo: [{ required: true, message: '证书编号不能为空', trigger: 'blur' }],
    qualLevel: [{ required: true, message: '资质等级不能为空', trigger: 'blur' }],
    ownerType: [{ required: true, message: '归属不能为空', trigger: 'change' }],
    issueDate: [{ required: true, message: '发证日期不能为空', trigger: 'change' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  const q = queryParams.value
  return ['name','qualType','expireStatus','ownerName','qualLevel'].filter(k => q[k]).length + (issueRange.value.length ? 1 : 0)
})

function typeLabel(v) { const d = bid_qual_type.value.find(x => x.value == v); return d ? d.label : '—' }
function typeBadge(v) { return { '1': 'violet', '2': 'blue', '3': 'green', '4': 'amber' }[v] || 'gray' }
function remainDays(row) { return row.expireDate ? Math.ceil((new Date(row.expireDate) - new Date()) / 86400000) : null }
function remainBadge(row) {
  if (!row.expireDate) return 'gray'
  const d = remainDays(row)
  if (d < 30) return 'red'
  if (d < 60) return 'amber'
  return 'green'
}
function remainText(row) {
  if (!row.expireDate) return '长期有效'
  const d = remainDays(row)
  return d < 0 ? '已过期' : `剩${d}天`
}

function getList() {
  loading.value = true
  listQualification(queryParams.value).then(res => { list.value = res.rows; total.value = res.total; loading.value = false; applySavedWidths() })
  loadCounts()
}
/** 分类页签计数：逐类型取 total，与列表口径一致 */
function loadCounts() {
  const types = ['1', '2', '3', '4']
  listQualification({ pageNum: 1, pageSize: 1 }).then(res => { tabCounts.value.all = res.total })
  types.forEach(t => {
    listQualification({ pageNum: 1, pageSize: 1, qualType: t }).then(res => { tabCounts.value[t] = res.total })
  })
}
function handleTabClick(tab) { activeTab.value = tab; queryParams.value.qualType = tab === 'all' ? undefined : tab; handleQuery() }
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; proxy.addDateRange(queryParams.value, issueRange.value, 'IssueDate'); getList() }
function resetQuery() { queryParams.value = { pageNum: 1, pageSize: queryParams.value.pageSize, name: undefined, qualType: undefined, expireStatus: undefined, ownerName: undefined, params: {} }; issueRange.value = []; activeTab.value = 'all'; getList() }

function reset() {
  form.value = { qualificationId: undefined, qualType: '1', name: undefined, certNo: undefined, qualLevel: undefined, ownerType: '1', ownerUserId: undefined, ownerName: undefined, issueDate: undefined, expireDate: undefined, warnDays: 60, fileUrl: undefined }
  proxy.resetForm('qualRef')
}
function handleAdd() { reset(); isView.value = false; open.value = true; title.value = '登记资质资料' }
function handleUpdate(row) { reset(); isView.value = false; getQualification(row.qualificationId).then(res => { form.value = res.data; open.value = true; title.value = '修改资质资料' }) }
function handleDetail(row) { reset(); isView.value = true; getQualification(row.qualificationId).then(res => { form.value = res.data; open.value = true; title.value = '资质详情' }) }
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.ownerUserId) }
function onUserPickerConfirm(user) { form.value.ownerUserId = user.userId; form.value.ownerName = user.nickName; if (user.deptId) { form.value.deptId = user.deptId } }
function submitForm() {
  proxy.$refs['qualRef'].validate(v => {
    if (v) {
      if (form.value.qualificationId != null) {
        updateQualification(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() })
      } else {
        addQualification(form.value).then(() => { proxy.$modal.msgSuccess('登记成功'); open.value = false; getList() })
      }
    }
  })
}
function cancel() { open.value = false; reset() }
function handleDelete(row) {
  proxy.$modal.confirm('确认删除「' + row.name + '」？').then(() => delQualification(row.qualificationId)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleExport() {
  proxy.download('bid/qualification/export', { ...proxy.addDateRange(queryParams.value, issueRange.value, 'IssueDate') }, `qualification_${new Date().getTime()}.xlsx`)
}
getList()
onActivated(() => { getList() })
</script>

<style scoped>
.bid-qualification-page { padding-top:10px; --brand-50:#eef2ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04),0 1px 2px rgba(15,23,42,.04); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.bid-qualification-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.bid-qualification-page .filter-head { display:flex; align-items:center; justify-content:space-between; padding:12px 20px 0; }
.bid-qualification-page .adv-link { display:inline-flex; align-items:center; gap:4px; color:var(--ink-500); cursor:pointer; font-size:13px; }
.bid-qualification-page .adv-link:hover { color:var(--brand-600); }
.bid-qualification-page .adv-link.is-open .chev { transform:rotate(180deg); }
.bid-qualification-page .adv-link .chev { transition:transform .2s; }
.bid-qualification-page .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; }
.bid-qualification-page .filter-title .glyph { width:4px; height:14px; border-radius:2px; background:var(--brand-500); }
.bid-qualification-page .filter-bar { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px 16px; padding:12px 20px; }
.bid-qualification-page .field label { display:block; font-size:12px; color:var(--ink-500); margin-bottom:4px; }
.bid-qualification-page .filter-actions { display:flex; align-items:center; justify-content:space-between; padding:0 20px 14px; }
.bid-qualification-page .filter-info { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--ink-400); }
.bid-qualification-page .filter-buttons { display:flex; gap:8px; }
.bid-qualification-page .status-tabs { display:flex; align-items:center; gap:12px; padding:6px 10px 6px 12px; border-bottom:1px solid var(--ink-200); background:#fff; }
.bid-qualification-page .tabs-track { display:flex; align-items:center; gap:4px; flex:1; min-width:0; overflow-x:auto; scrollbar-width:none; }
.bid-qualification-page .tabs-track::-webkit-scrollbar { display:none; }
.bid-qualification-page .status-tab { display:inline-flex; align-items:center; gap:6px; height:32px; padding:0 12px; border-radius:var(--r-sm); font-size:14px; color:var(--ink-500); cursor:pointer; user-select:none; white-space:nowrap; border:1px solid transparent; background:transparent; }
.bid-qualification-page .status-tab .dot { width:6px; height:6px; border-radius:50%; background:var(--ink-300); }
.bid-qualification-page .status-tab .count { font-size:12px; font-weight:600; padding:1px 6px; border-radius:999px; background:var(--ink-100); color:var(--ink-500); min-width:18px; text-align:center; }
.bid-qualification-page .status-tab:hover { background:var(--ink-50); }
.bid-qualification-page .status-tab.is-active { background:var(--brand-50); color:var(--brand-700); font-weight:600; border-color:var(--brand-200); }
.bid-qualification-page .status-tab.is-active .count { background:var(--brand-600); color:#fff; }
.tip-pill { display:inline-flex; align-items:center; gap:6px; height:30px; padding:0 12px; background:#fffaf0; border:1px solid #fde68a; color:#92400e; border-radius:999px; font-size:13px; font-weight:500; cursor:pointer; white-space:nowrap; flex-shrink:0; }
.tip-pill:hover { background:#fffbeb; border-color:#f59e0b; }
.bid-qualification-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:10px 12px; }
.bid-qualification-page .toolbar .left { display:flex; gap:8px; }
.bid-qualification-page .pagination-container { padding:10px 12px; }
.bid-qualification-page .badge { display:inline-flex; align-items:center; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.bid-qualification-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.bid-qualification-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.bid-qualification-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.bid-qualification-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.bid-qualification-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.bid-qualification-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
:deep(.col-action) { padding:6px 4px !important; }
:deep(.col-action .cell) { display:flex; justify-content:center; padding:0; }
.action-btn-row { display:inline-flex; flex-wrap:wrap; justify-content:center; gap:0; }
:deep(.col-action .el-button) { padding:2px 4px; margin:0 2px; white-space:nowrap; justify-content:center; }
:deep(.col-action .el-button + .el-button) { margin-left:2px; }
.status-help-content { max-height:520px; overflow-y:auto; padding-right:10px; }
.status-help-content h4 { margin:20px 0 12px 0; color:#303133; font-weight:600; border-left:4px solid #409eff; padding-left:10px; }
.status-help-content h4:first-child { margin-top:0; }
.status-help-content .highlight-card { border-radius:8px; padding:16px; border:1px solid; }
.status-help-content .highlight-card-title { font-size:14px; font-weight:600; margin-bottom:8px; }
.status-help-content .highlight-card-body { font-size:13px; color:#606266; line-height:1.6; }
.status-help-content .highlight-primary { background-color:#ecf5ff; border-color:#a0cfff; }
.status-help-content .highlight-primary .highlight-card-title { color:#409eff; }
</style>
