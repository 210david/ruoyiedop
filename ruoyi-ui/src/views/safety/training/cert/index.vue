<template>
  <div class="app-container safety-training-cert-page">
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
          <label>证书编号</label>
          <div class="control">
            <el-input v-model="queryParams.certNo" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
        <div class="field">
          <label>证书名称</label>
          <div class="control">
            <el-input v-model="queryParams.certName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field">
          <label>证书类型</label>
          <div class="control is-select">
            <el-select v-model="queryParams.certType" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="dict in safety_cert_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>获证人员</label>
          <div class="control">
            <el-input v-model="queryParams.userName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>所属部门</label>
          <div class="control">
            <el-input v-model="queryParams.deptName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
          </div>
        </div>
        <div class="field" v-show="showAdvanced">
          <label>发证机构</label>
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
          <label>发证日期</label>
          <div class="control is-select">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" @change="handleQuery" />
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
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['safety:training:cert:add']">新增</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['safety:training:cert:remove']">删除</el-button>
          <div class="toolbar-divider"></div>
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['safety:training:cert:export']">导出</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" storageKey="safety_training_cert_columns" />
        </div>
      </div>

      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="certList" border @selection-change="handleSelectionChange" @header-dragend="onHeaderDragEnd" @sort-change="handleSortChange" class="app-table">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="证书编号" prop="certNo" key="certNo" :width="colWidth('certNo', 156)" resizable v-if="columns.certNo.visible" />
          <el-table-column label="证书名称" prop="certName" key="certName" :width="colWidth('certName', 240)" resizable show-overflow-tooltip v-if="columns.certName.visible" />
          <el-table-column label="证书类型" prop="certType" key="certType" :width="colWidth('certType', 180)" resizable align="center" v-if="columns.certType.visible">
            <template #default="scope"><span class="badge violet">{{ certTypeLabel(scope.row.certType) }}</span></template>
          </el-table-column>
          <el-table-column label="获证人员" prop="userName" key="userName" :width="colWidth('userName', 144)" resizable v-if="columns.userName.visible" />
          <el-table-column label="所属部门" prop="deptName" key="deptName" :width="colWidth('deptName', 168)" resizable show-overflow-tooltip v-if="columns.deptName.visible" />
          <el-table-column label="发证日期" prop="issueDate" key="issueDate" :width="colWidth('issueDate', 132)" resizable align="center" sortable="custom" v-if="columns.issueDate.visible" />
          <el-table-column label="有效期至" prop="expireDate" key="expireDate" :width="colWidth('expireDate', 132)" resizable align="center" v-if="columns.expireDate.visible">
            <template #default="scope">
              <span :class="isExpiringSoon(scope.row.expireDate) ? 'expiring-soon' : ''">{{ scope.row.expireDate || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="发证机构" prop="issueOrg" key="issueOrg" :width="colWidth('issueOrg', 180)" resizable show-overflow-tooltip v-if="columns.issueOrg.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 96)" resizable align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="scope.row.status === '0' ? 'green' : 'red'">
                <span class="dot"></span>{{ scope.row.status === '0' ? '正常' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="216" align="center" fixed="right">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['safety:training:cert:query']">查看</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['safety:training:cert:edit']">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['safety:training:cert:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <el-dialog v-model="open" width="840px" append-to-body draggable class="rd-dialog">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></div>
          <span class="rd-detail-header-title">{{ title }}</span>
        </div>
      </template>
      <el-form ref="certRef" :model="form" :rules="rules" label-width="100px">
        <div class="rd-page">
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>证书信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c0">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="证书编号" prop="certNo"><el-input v-model="form.certNo" placeholder="请输入证书编号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="证书名称" prop="certName"><el-input v-model="form.certName" placeholder="请输入证书名称" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="证书类型" prop="certType"><el-select v-model="form.certType" placeholder="请选择" style="width: 100%"><el-option v-for="dict in safety_cert_type" :key="dict.value" :label="dict.label" :value="dict.value" /></el-select></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="发证机构" prop="issueOrg"><el-input v-model="form.issueOrg" placeholder="请输入发证机构" /></el-form-item></el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>获证人员</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c1">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="获证人员" prop="userId">
                    <el-input v-model="form.userName" readonly placeholder="请选择获证人员" style="width: 100%" @click="openUserPicker">
                      <template #append><el-button icon="Search" @click="openUserPicker" /></template>
                      <template #suffix><el-icon v-if="form.userName" class="clear-icon" @click.stop="clearUser"><CircleClose /></el-icon></template>
                    </el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所属部门" prop="deptName"><el-input v-model="form.deptName" placeholder="选择人员后自动回填" disabled /></el-form-item>
                </el-col>
              </el-row>
            </div>
          </section>
          <section class="rd-card">
            <div class="rd-card-header" @click="toggleCard('c2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期与附件</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.c2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
            <div class="rd-card-body" v-show="!collapsedCards.c2">
              <el-row :gutter="20">
                <el-col :span="12"><el-form-item label="发证日期" prop="issueDate"><el-date-picker v-model="form.issueDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="有效期至" prop="expireDate"><el-date-picker v-model="form.expireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
              </el-row>
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
          <div class="rd-detail-header-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg></div>
          <span class="rd-detail-header-title">证书详情</span>
          <div class="rd-detail-header-sub" v-if="viewData.certNo"><div class="rd-detail-header-divider"></div><span class="rd-detail-header-no">编号：{{ viewData.certNo }}</span></div>
        </div>
      </template>
      <div class="rd-page">
        <section class="rd-card">
          <div class="rd-card-header" @click="toggleCard('vc0')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg></span>证书信息</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc0 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc0" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">证书编号</span><div class="rd-value">{{ viewData.certNo || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">证书名称</span><div class="rd-value">{{ viewData.certName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">证书类型</span><div class="rd-value"><dict-tag :options="safety_cert_type" :value="viewData.certType" /></div></div>
              <div class="rd-item"><span class="rd-label">发证机构</span><div class="rd-value">{{ viewData.issueOrg || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.userName || viewData.deptName">
          <div class="rd-card-header" @click="toggleCard('vc1')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg></span>获证人员</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc1 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc1" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">获证人员</span><div class="rd-value">{{ viewData.userName || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">所属部门</span><div class="rd-value">{{ viewData.deptName || '-' }}</div></div>
            </div>
          </div>
        </section>
        <section class="rd-card" v-if="viewData.issueDate || viewData.expireDate || viewData.attachment || viewData.remark">
          <div class="rd-card-header" @click="toggleCard('vc2')"><div class="rd-card-title"><span class="rd-card-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg></span>有效期与附件</div><button class="rd-collapse-btn" :class="{ 'is-collapsed': collapsedCards.vc2 }" aria-label="折叠"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"/></svg></button></div>
          <div class="rd-card-body" v-show="!collapsedCards.vc2" style="display:block">
            <div class="rd-grid">
              <div class="rd-item"><span class="rd-label">发证日期</span><div class="rd-value">{{ viewData.issueDate || '-' }}</div></div>
              <div class="rd-item"><span class="rd-label">有效期至</span><div class="rd-value">{{ viewData.expireDate || '-' }}</div></div>
              <div class="rd-item rd-item--full" v-if="viewData.attachment"><span class="rd-label">附件</span><div class="rd-value"><div class="rd-file-links" v-if="viewData.attachment">
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


    <!-- 获证人员选择弹窗 -->
    <user-picker ref="userPickerRef" title="选择获证人员" @confirm="onUserPickerConfirm" />
    <file-preview ref="filePreviewRef" />
  </div>
</template>

<script setup name="SafetyTrainingCert">
import { listTrainingCert, getTrainingCert, addTrainingCert, updateTrainingCert, delTrainingCert } from '@/api/safety/trainingCert'
import UserPicker from '@/components/UserPicker/index.vue'
import { useColumnResize } from '@/composables/useColumnResize'
import { useDetailCard } from '@/composables/useDetailCard'
import { Search, Filter, RefreshLeft, CircleClose, ArrowDown } from '@element-plus/icons-vue'
import FilePreview from '@/components/FilePreview/index.vue'
import { downloadFile } from '@/utils/downloadFile'

const { proxy } = getCurrentInstance()
const { safety_cert_type } = proxy.useDict('safety_cert_type')
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('safety_training_cert_index')
const { collapsedCards, toggleCard } = useDetailCard(["c0","c1","c2","vc0","vc1","vc2"])

const certList = ref([])
const baseUrl = import.meta.env.VITE_APP_BASE_API
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const showAdvanced = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const viewOpen = ref(false)
const viewData = ref({})
const dateRange = ref([])

const default_columns = {
  certNo: { label: '证书编号', visible: true },
  certName: { label: '证书名称', visible: true },
  certType: { label: '证书类型', visible: true },
  userName: { label: '获证人员', visible: true },
  deptName: { label: '所属部门', visible: true },
  issueDate: { label: '发证日期', visible: true },
  expireDate: { label: '有效期至', visible: true },
  issueOrg: { label: '发证机构', visible: true },
  status: { label: '状态', visible: true }
}

function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('safety_training_cert_columns')
    if (saved) {
      const parsed = JSON.parse(saved)
      const result = {}
      Object.keys(default_columns).forEach(key => {
        result[key] = { label: default_columns[key].label, visible: parsed[key] !== undefined ? parsed[key] : default_columns[key].visible }
      })
      return result
    }
  } catch (e) {}
  return { ...default_columns }
}

const columns = ref(loadColumnVisibility())

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, certNo: undefined, certName: undefined, certType: undefined, userName: undefined, deptName: undefined, issueOrg: undefined, status: undefined, params: {} },
  rules: {
    certNo: [{ required: true, message: '证书编号不能为空', trigger: 'blur' }],
    certName: [{ required: true, message: '证书名称不能为空', trigger: 'blur' }],
    certType: [{ required: true, message: '证书类型不能为空', trigger: 'change' }],
    userId: [{ required: true, message: '获证人员不能为空', trigger: 'change' }],
    issueDate: [{ required: true, message: '发证日期不能为空', trigger: 'change' }],
    expireDate: [{ required: true, message: '有效期至不能为空', trigger: 'change' }],
    issueOrg: [{ required: true, message: '发证机构不能为空', trigger: 'blur' }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const activeFilterCount = computed(() => {
  let count = 0
  if (queryParams.value.certNo) count++
  if (queryParams.value.certName) count++
  if (queryParams.value.certType) count++
  if (queryParams.value.userName) count++
  if (queryParams.value.deptName) count++
  if (queryParams.value.issueOrg) count++
  if (queryParams.value.status) count++
  if (dateRange.value && dateRange.value.length === 2) count++
  return count
})

function handleFilePreview(url) {
const name = url.includes('/') ? url.substring(url.lastIndexOf('/') + 1) : url
proxy.$refs.filePreviewRef.open(url, name)
}
function handleFileDownload(url) {
downloadFile(url)
}

function getList() {
  loading.value = true
  proxy.addDateRange(queryParams.value, dateRange.value)
  listTrainingCert(queryParams.value).then(response => { certList.value = response.rows; total.value = response.total; loading.value = false; applySavedWidths() })
}
function handleQuery() { showAdvanced.value = false; queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.certNo = undefined; queryParams.value.certName = undefined; queryParams.value.certType = undefined; queryParams.value.userName = undefined; queryParams.value.deptName = undefined; queryParams.value.issueOrg = undefined; queryParams.value.status = undefined; dateRange.value = []; queryParams.value.params = {}; handleQuery() }
function handleSortChange(column) { if (column.prop && column.order) { queryParams.value.params.orderByColumn = column.prop; queryParams.value.params.isAsc = column.order === 'ascending' ? 'asc' : 'desc' } else { queryParams.value.params.orderByColumn = undefined; queryParams.value.params.isAsc = undefined }; getList() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.certId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleAdd() { reset(); collapsedCards.c0 = false; collapsedCards.c1 = false; collapsedCards.c2 = false; open.value = true; title.value = '添加证书' }
function handleUpdate(row) { reset(); getTrainingCert(row.certId || ids.value[0]).then(response => { form.value = response.data; collapsedCards.c1 = !response.data.userName; collapsedCards.c2 = !response.data.attachment && !response.data.remark; open.value = true; title.value = '修改证书' }) }
function handleView(row) { const id = row.certId || ids.value[0]; getTrainingCert(id).then(response => { viewData.value = response.data; viewOpen.value = true }) }
function submitForm() {
  proxy.$refs['certRef'].validate(valid => {
    if (valid) {
      if (form.value.certId != undefined) { updateTrainingCert(form.value).then(() => { proxy.$modal.msgSuccess('修改成功'); open.value = false; getList() }) }
      else { addTrainingCert(form.value).then(() => { proxy.$modal.msgSuccess('新增成功'); open.value = false; getList() }) }
    }
  })
}
function handleDelete(row) { const certIds = row.certId || ids.value; proxy.$modal.confirm('是否确认删除证书？').then(function() { return delTrainingCert(certIds) }).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {}) }
function handleExport() { proxy.download('safety/training/cert/export', { ...queryParams.value }, `training_cert_${new Date().getTime()}.xlsx`) }
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { certId: undefined, certNo: undefined, certName: undefined, certType: undefined, userId: undefined, userName: undefined, deptId: undefined, deptName: undefined, issueDate: undefined, expireDate: undefined, issueOrg: undefined, attachment: undefined, status: '0', remark: undefined }
  proxy.resetForm('certRef')
}

/** 打开获证人员选择弹窗 */
function openUserPicker() { proxy.$refs.userPickerRef.open(form.value.userId) }
/** 获证人员选择确认回调 */
function onUserPickerConfirm(user) { form.value.userId = user.userId; form.value.userName = user.nickName; form.value.deptId = user.deptId; form.value.deptName = user.deptName || '' }
/** 清除获证人员 */
function clearUser() { form.value.userId = undefined; form.value.userName = undefined; form.value.deptId = undefined; form.value.deptName = undefined }

function certTypeLabel(type) { const item = safety_cert_type.value.find(d => d.value == type); return item ? item.label : '-' }

/** 判断是否即将过期（30天内） */
function isExpiringSoon(expireDate) {
  if (!expireDate) return false
  const diff = new Date(expireDate) - new Date()
  return diff > 0 && diff < 30 * 24 * 60 * 60 * 1000
}

getList()
</script>

<style scoped>
.safety-training-cert-page { padding-top:10px; --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca; --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc; --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309; --blue-50:#eff6ff; --blue-500:#3b82f6; --blue-700:#1d4ed8; --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857; --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c; --violet-50:#f5f3ff; --r-sm:6px; --r-md:10px; --r-lg:14px; --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04); --ease-out:cubic-bezier(.16,.84,.44,1); font-feature-settings:"tnum" 1; color:var(--ink-900); }
.safety-training-cert-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.safety-training-cert-page .filter-card { padding:14px 20px 16px; }
.safety-training-cert-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.safety-training-cert-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.safety-training-cert-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.safety-training-cert-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.safety-training-cert-page .filter-card .adv-link { font-size:14px; color:var(--ink-500); text-decoration:none; display:flex; align-items:center; gap:4px; transition:color .15s; cursor:pointer; }
.safety-training-cert-page .filter-card .adv-link:hover { color:var(--brand-600); }
.safety-training-cert-page .filter-card .adv-link .chev { transition:transform .2s var(--ease-out); }
.safety-training-cert-page .filter-card .adv-link.is-open .chev { transform:rotate(180deg); }
.safety-training-cert-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.safety-training-cert-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.safety-training-cert-page .filter-card .filter-buttons { display:flex; gap:8px; }
.safety-training-cert-page .field { display:flex; flex-direction:column; gap:6px; }
.safety-training-cert-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.safety-training-cert-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); }
.safety-training-cert-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.safety-training-cert-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.safety-training-cert-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.safety-training-cert-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.safety-training-cert-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.safety-training-cert-page .field .control :deep(.el-select) { width:100%; }
.safety-training-cert-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.safety-training-cert-page .field .control.is-select { padding:0; }
.safety-training-cert-page .field .control.is-select :deep(.el-select) { width:100%; }
.safety-training-cert-page .field .control.is-select :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0 12px; min-height:34px; height:34px; }
.safety-training-cert-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.safety-training-cert-page .toolbar .left, .safety-training-cert-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.safety-training-cert-page .table-wrap { overflow-x:auto; }
.safety-training-cert-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.safety-training-cert-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; }
.safety-training-cert-page .app-table :deep(.el-table__header th) { border-right-color:transparent !important; background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 19px; border-bottom:1px solid var(--ink-200); }
.safety-training-cert-page .app-table :deep(.el-table__body td) { padding:14px 19px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.safety-training-cert-page .app-table :deep(.el-table__inner-wrapper::before) { display:none; }
.safety-training-cert-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.safety-training-cert-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.safety-training-cert-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; } .safety-training-cert-page .badge.green .dot { background:var(--green-500); }
.safety-training-cert-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; } .safety-training-cert-page .badge.red .dot { background:var(--red-500); }
.safety-training-cert-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.safety-training-cert-page .expiring-soon { color: var(--amber-700); font-weight: 600; }
.safety-training-cert-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
@media (max-width:1100px) { .safety-training-cert-page .filter-card .filter-bar { grid-template-columns:repeat(2,1fr); } }
@media (max-width:720px) { .safety-training-cert-page .filter-card .filter-bar { grid-template-columns:1fr; } }
.clear-icon { cursor: pointer; color: #c0c4cc; font-size: 14px; }
.clear-icon:hover { color: #909399; }
</style>
