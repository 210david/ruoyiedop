<template>
  <div class="app-container ai-knowledge-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field">
          <label>所属模块</label>
          <div class="control is-select">
            <el-select v-model="queryParams.module" placeholder="全部" clearable @change="handleQuery">
              <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
            </el-select>
          </div>
        </div>
        <div class="field">
          <label>文档名称</label>
          <div class="control">
            <el-input v-model="queryParams.docName" placeholder="请输入" clearable @keyup.enter="handleQuery">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </div>
      <div class="filter-actions">
        <div class="filter-info">
          <el-icon><InfoFilled /></el-icon> 文档上传解析后自动切片入库，供全平台 AI 问答检索引用
        </div>
        <div class="filter-buttons">
          <el-button icon="RefreshLeft" @click="resetQuery">重置</el-button>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>
      </div>
    </div>

    <!-- ===== List Card ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="uploadOpen = true" v-hasPermi="['ai:knowledge:add']">上传文档</el-button>
        </div>
        <div class="right">
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </div>
      </div>
      <div class="table-wrap">
        <el-table ref="tableRef" v-loading="loading" :data="docList" border
          @header-dragend="onHeaderDragEnd" class="app-table">
          <el-table-column type="index" label="序号" width="85" align="center" />
          <el-table-column label="文档名称" prop="docName" key="docName" :min-width="colWidth('docName', 240)" resizable show-overflow-tooltip v-if="columns.docName.visible" />
          <el-table-column label="所属模块" prop="module" key="module" width="120" align="center" v-if="columns.module.visible">
            <template #default="scope">
              <span class="badge violet">{{ moduleNames[scope.row.module] || scope.row.module }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" prop="fileType" key="fileType" width="90" align="center" v-if="columns.fileType.visible">
            <template #default="scope">
              <span class="badge blue">{{ (scope.row.fileType || '').toUpperCase() }}</span>
            </template>
          </el-table-column>
          <el-table-column label="切片数" prop="chunkCount" key="chunkCount" width="100" align="center" v-if="columns.chunkCount.visible" />
          <el-table-column label="状态" prop="status" key="status" width="110" align="center" v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusText[scope.row.status] || scope.row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="上传人" prop="createBy" key="createBy" width="110" align="center" v-if="columns.createBy.visible" />
          <el-table-column label="上传时间" prop="createTime" key="createTime" width="180" align="center" v-if="columns.createTime.visible" />
          <el-table-column label="操作" width="100" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['ai:knowledge:remove']">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-container">
        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadOpen" title="上传知识文档" width="560px" destroy-on-close @closed="resetUploadForm" class="ai-upload-dialog">
      <div class="upload-steps">
        <div class="upload-step"><span class="step-num">1</span><span>选择所属模块</span></div>
        <span class="step-arrow"></span>
        <div class="upload-step"><span class="step-num">2</span><span>选择文件上传</span></div>
        <span class="step-arrow"></span>
        <div class="upload-step"><span class="step-num">3</span><span>自动解析入库</span></div>
      </div>
      <el-form label-position="top">
        <el-form-item label="所属模块">
          <el-select v-model="uploadModule" style="width: 100%" size="large">
            <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档文件">
          <el-upload ref="uploadRef" drag :auto-upload="false" :limit="1"
            :on-change="f => (uploadFile = f.raw)" :on-remove="() => (uploadFile = null)"
            class="upload-dragger" :on-exceed="() => proxy.$modal.msgWarning('一次只能上传一个文件，请先移除已选文件')">
            <div class="upload-inner">
              <div class="upload-icon"><el-icon :size="26"><UploadFilled /></el-icon></div>
              <div class="upload-main">将文件拖到此处，或<em>点击选择文件</em></div>
              <div class="upload-sub">支持 PDF / DOCX / MD / TXT · 单文件 ≤ 20MB · 扫描版PDF暂不支持</div>
            </div>
          </el-upload>
        </el-form-item>
        <div class="upload-tip">
          <el-icon :size="13"><InfoFilled /></el-icon>
          上传后系统将自动抽取文本、清洗格式并切片入库，供 AI 知识问答检索引用；解析状态可在列表中查看。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="uploadOpen = false">取 消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          <el-icon style="margin-right:4px"><UploadFilled /></el-icon>{{ uploading ? '解析中…' : '上传并解析' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiKnowledge">
import { listKnowledge, uploadKnowledge, delKnowledge } from '@/api/ai/knowledge'
import { useColumnResize } from '@/composables/useColumnResize'
import { UploadFilled, Search, InfoFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const loading = ref(true)
const showSearch = ref(true)
const docList = ref([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, module: undefined, docName: undefined })

const moduleNames = {
  platform: '平台公共', dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产'
}
const statusText = { '0': '已解析', '1': '解析中', '2': '解析失败' }

function badgeClass(status) {
  const map = { '0': 'green', '1': 'amber', '2': 'red' }
  return map[status] || 'gray'
}

/* 列显隐配置 */
const defaultColumns = {
  docName: { label: '文档名称', visible: true },
  module: { label: '所属模块', visible: true },
  fileType: { label: '类型', visible: true },
  chunkCount: { label: '切片数', visible: true },
  status: { label: '状态', visible: true },
  createBy: { label: '上传人', visible: true },
  createTime: { label: '上传时间', visible: true }
}
function loadColumnVisibility() {
  try {
    const saved = localStorage.getItem('ai_knowledge_columns_visibility')
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

/* 列宽拖拽持久化 */
const { colWidth, onHeaderDragEnd, tableRef, applySavedWidths } = useColumnResize('ai_knowledge_index')

// 上传
const uploadOpen = ref(false)
const uploadModule = ref('platform')
const uploadFile = ref(null)
const uploading = ref(false)

function resetUploadForm() {
  uploadFile.value = null
  uploadModule.value = 'platform'
}

function getList() {
  loading.value = true
  listKnowledge(queryParams.value).then(res => {
    docList.value = res.rows
    total.value = res.total
    loading.value = false
    applySavedWidths()
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.module = undefined
  queryParams.value.docName = undefined
  handleQuery()
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除文档《' + row.docName + '》及其全部知识切片？').then(() => {
    return delKnowledge(row.docId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleUpload() {
  if (!uploadFile.value) {
    proxy.$modal.msgWarning('请先选择文件')
    return
  }
  if (uploadFile.value.size > 20 * 1024 * 1024) {
    proxy.$modal.msgWarning('文件超过20MB限制')
    return
  }
  uploading.value = true
  const fd = new FormData()
  fd.append('file', uploadFile.value)
  fd.append('module', uploadModule.value)
  uploadKnowledge(fd).then(() => {
    uploading.value = false
    uploadOpen.value = false
    uploadFile.value = null
    proxy.$modal.msgSuccess('上传解析成功')
    getList()
  }).catch(() => { uploading.value = false })
}

getList()
</script>

<style scoped>
/* ===== Design Tokens ===== */
.ai-knowledge-page {
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
.ai-knowledge-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }
.ai-knowledge-page .filter-card { padding:14px 20px 16px; }
.ai-knowledge-page .filter-card .filter-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.ai-knowledge-page .filter-card .filter-title { display:flex; align-items:center; gap:8px; font-size:14px; font-weight:600; color:var(--ink-700); }
.ai-knowledge-page .filter-card .filter-title .glyph { width:4px; height:14px; background:var(--brand-600); border-radius:2px; }
.ai-knowledge-page .filter-card .filter-bar { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px 16px; }
.ai-knowledge-page .filter-card .filter-actions { display:flex; align-items:center; justify-content:space-between; margin-top:14px; padding-top:14px; border-top:1px dashed var(--ink-200); }
.ai-knowledge-page .filter-card .filter-info { font-size:13px; color:var(--ink-500); display:flex; align-items:center; gap:6px; }
.ai-knowledge-page .filter-card .filter-buttons { display:flex; gap:8px; }
.ai-knowledge-page .field { display:flex; flex-direction:column; gap:6px; }
.ai-knowledge-page .field label { font-size:14px; font-weight:500; color:var(--ink-700); }
.ai-knowledge-page .field .control { display:flex; align-items:center; height:36px; padding:0 12px; background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-sm); transition:border-color .15s var(--ease-out), box-shadow .15s var(--ease-out); }
.ai-knowledge-page .field .control:focus-within { border-color:var(--brand-500); box-shadow:0 0 0 3px rgba(99,102,241,.15); }
.ai-knowledge-page .field .control :deep(.el-input__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; height:34px; }
.ai-knowledge-page .field .control :deep(.el-input__inner) { border:0; background:transparent; font-size:14px; color:var(--ink-900); height:34px; line-height:34px; }
.ai-knowledge-page .field .control :deep(.el-input__inner::placeholder) { color:var(--ink-400); }
.ai-knowledge-page .field .control :deep(.el-input__prefix) { color:var(--ink-400); margin-right:4px; }
.ai-knowledge-page .field .control :deep(.el-select) { width:100%; }
.ai-knowledge-page .field .control :deep(.el-select .el-select__wrapper) { box-shadow:none !important; background:transparent !important; padding:0; min-height:34px; height:34px; }
.ai-knowledge-page .field .control :deep(.el-select .el-select__wrapper .el-select__placeholder) { font-size:14px; color:var(--ink-900); }
.ai-knowledge-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.ai-knowledge-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.ai-knowledge-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.ai-knowledge-page .table-wrap { overflow-x:auto; }
.ai-knowledge-page .app-table { --el-table-bg-color:#fff; --el-table-header-bg-color:var(--ink-50); --el-table-row-hover-bg-color:#fafbff; --el-table-border-color:transparent; --el-table-text-color:var(--ink-700); --el-table-header-text-color:var(--ink-500); }
.ai-knowledge-page .app-table :deep(.el-table__body td) { border-right-color:transparent !important; padding:13px 16px; border-bottom:1px solid var(--ink-100); color:var(--ink-700); }
.ai-log-knowledge-fix {}
.ai-knowledge-page .app-table :deep(.el-table__header th) { background:var(--ink-50) !important; color:var(--ink-500); font-weight:600; font-size:14px; padding:12px 16px; border-bottom:1px solid var(--ink-200); border-right-color:transparent !important; }
.ai-knowledge-page .app-table :deep(.el-table__row:hover > td) { background:#fafbff !important; }
.ai-knowledge-page .badge { display:inline-flex; align-items:center; gap:5px; padding:3px 9px; border-radius:999px; font-size:13px; font-weight:600; line-height:1; border:1px solid transparent; }
.ai-knowledge-page .badge .dot { width:6px; height:6px; border-radius:50%; }
.ai-knowledge-page .badge.blue { background:var(--blue-50); color:var(--blue-700); border-color:#bfdbfe; }
.ai-knowledge-page .badge.green { background:var(--green-50); color:var(--green-700); border-color:#a7f3d0; }
.ai-knowledge-page .badge.green .dot { background:var(--green-500); }
.ai-knowledge-page .badge.amber { background:var(--amber-50); color:var(--amber-700); border-color:#fde68a; }
.ai-knowledge-page .badge.amber .dot { background:var(--amber-500); }
.ai-knowledge-page .badge.red { background:var(--red-50); color:var(--red-700); border-color:#fecaca; }
.ai-knowledge-page .badge.red .dot { background:var(--red-500); }
.ai-knowledge-page .badge.violet { background:var(--violet-50); color:var(--brand-700); border-color:var(--brand-200); }
.ai-knowledge-page .badge.gray { background:var(--ink-100); color:var(--ink-500); border-color:var(--ink-200); }
.ai-knowledge-page .pagination-container { display:flex; align-items:center; justify-content:flex-end; padding:14px 20px; background:#fff; }
.ai-knowledge-page .pagination-container :deep(.el-pagination) { justify-content:flex-end; }
.ai-knowledge-page .pagination-container :deep(.el-pagination .el-pager li) { border-radius:6px; border:1px solid var(--ink-200); background:#fff; min-width:32px; height:32px; line-height:32px; margin:0 2px; }
.ai-knowledge-page .pagination-container :deep(.el-pagination .el-pager li.is-active) { background:var(--brand-600); border-color:var(--brand-600); color:#fff; font-weight:600; }

/* 操作列 */
:deep(.col-action) { padding: 6px 4px !important; }
:deep(.col-action .cell) { display: flex; justify-content: center; padding: 0; }
.action-btn-row { display: inline-flex; flex-wrap: wrap; justify-content: center; gap: 0; }
:deep(.col-action .el-button) { padding: 2px 4px; margin: 0 2px; white-space: nowrap; justify-content: center; }

/* ===== 上传弹窗 ===== */
.upload-steps { display:flex; align-items:center; justify-content:center; gap:8px; background:#f8faff; border:1px solid #e4e8ff; border-radius:10px; padding:12px 16px; margin-bottom:18px; }
.upload-step { display:inline-flex; align-items:center; gap:7px; font-size:13px; color:#4b5563; font-weight:500; }
.upload-step .step-num {
  width:20px; height:20px; border-radius:50%; background:linear-gradient(135deg,#8b5cf6,#6366f1);
  color:#fff; font-size:12px; font-weight:700; display:inline-flex; align-items:center; justify-content:center;
}
.step-arrow { width:22px; height:1px; background:linear-gradient(90deg,#c7d2fe,#e0e7ff); position:relative; }
.step-arrow::after { content:''; position:absolute; right:0; top:-2.5px; border-left:5px solid #c7d2fe; border-top:3px solid transparent; border-bottom:3px solid transparent; }
/* 注意：upload-dragger class 挂在 el-upload 组件根元素上，
   不能写成 ".upload-dragger :deep(.el-upload)"（后代选择器永远匹配不到自身） */
.upload-dragger { width: 100%; }
.upload-dragger :deep(.el-upload-dragger) {
  width: 100%;
  border:1.5px dashed #c7d2fe; border-radius:12px; background:#fbfbff;
  transition:all .2s; padding:20px 0;
}
.upload-dragger :deep(.el-upload-dragger:hover) { border-color:#6366f1; background:#f5f6ff; }
.upload-inner { display:flex; flex-direction:column; align-items:center; gap:6px; padding:6px 0; }
.upload-icon {
  width:48px; height:48px; border-radius:14px; color:#fff;
  background:linear-gradient(135deg,#8b5cf6,#6366f1);
  display:flex; align-items:center; justify-content:center;
  box-shadow:0 4px 12px rgba(99,102,241,.3);
}
.upload-main { font-size:14px; color:#374151; }
.upload-main em { color:#4f46e5; font-style:normal; font-weight:600; }
.upload-sub { font-size:12px; color:#9ca3af; }
.upload-tip { display:flex; align-items:flex-start; gap:6px; font-size:12px; color:#6b7280; background:#f8faff; border:1px solid #e4e8ff; border-radius:8px; padding:9px 12px; line-height:1.6; margin-top:2px; }
.upload-tip .el-icon { margin-top:2px; color:#6366f1; flex:none; }

/* 全局兜底：el-dialog弹层在body下，scoped样式无法命中，用非scoped保证铺满 */
</style>

<style>
/* 弹窗上传区铺满整行（append层在body下，必须用全局样式） */
.ai-upload-dialog .el-upload,
.ai-upload-dialog .el-upload-dragger {
  width: 100% !important;
}
</style>
