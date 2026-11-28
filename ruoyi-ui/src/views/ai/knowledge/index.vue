<template>
  <div class="app-container ai-knowledge-page">
    <!-- ===== Filter Card ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 筛选条件</div>
      </div>
      <div class="filter-bar">
        <div class="field"><div class="control is-select">
            <el-select v-model="queryParams.module" placeholder="所属模块" clearable @change="handleQuery">
              <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
            </el-select>
          </div>
        </div>
        <div class="field"><div class="control">
            <el-input v-model="queryParams.docName" placeholder="文档名称" clearable @keyup.enter="handleQuery">
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
          <el-table-column label="所属模块" prop="module" key="module" :width="colWidth('module', 120)" align="center" resizable v-if="columns.module.visible">
            <template #default="scope">
              <span class="badge violet">{{ moduleNames[scope.row.module] || scope.row.module }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" prop="fileType" key="fileType" :width="colWidth('fileType', 90)" align="center" resizable v-if="columns.fileType.visible">
            <template #default="scope">
              <span class="badge blue">{{ (scope.row.fileType || '').toUpperCase() }}</span>
            </template>
          </el-table-column>
          <el-table-column label="切片数" prop="chunkCount" key="chunkCount" :width="colWidth('chunkCount', 100)" align="center" resizable v-if="columns.chunkCount.visible" />
          <el-table-column label="状态" prop="status" key="status" :width="colWidth('status', 110)" align="center" resizable v-if="columns.status.visible">
            <template #default="scope">
              <span class="badge" :class="badgeClass(scope.row.status)">
                <span class="dot"></span>{{ statusText[scope.row.status] || scope.row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="上传人" prop="createBy" key="createBy" :width="colWidth('createBy', 110)" align="center" resizable v-if="columns.createBy.visible" />
          <el-table-column label="上传时间" prop="createTime" key="createTime" :width="colWidth('createTime', 180)" align="center" resizable v-if="columns.createTime.visible" />
          <!-- 功能入口：问答命中该文档时，AI 直接给出此菜单入口 -->
          <el-table-column label="功能入口" key="navMenuId" :width="colWidth('navMenuId', 140)" align="center" resizable v-if="columns.navMenuId.visible">
            <template #default="scope">
              <el-tag v-if="scope.row.navMenuId" size="small" effect="light" type="primary" class="nav-bound-tag"
                :title="navMenuFullTitle(scope.row.navMenuId)">
                {{ navMenuTitle(scope.row.navMenuId) }}
              </el-tag>
              <button v-else type="button" class="nav-bind-link" v-hasPermi="['ai:knowledge:nav']"
                @click="openNavBind(scope.row)">+ 关联入口</button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center" fixed="right" class-name="col-action">
            <template #default="scope">
              <div class="action-btn-row">
                <el-button link type="primary" icon="Link" @click="openNavBind(scope.row)" v-hasPermi="['ai:knowledge:nav']">入口</el-button>
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
        <el-form-item label="关联功能入口（可选）">
          <el-select v-model="uploadNavMenuId" style="width: 100%" size="large" clearable filterable
            placeholder="选填：问答命中该文档时，直接给出此功能入口">
            <el-option v-for="o in menuOptions" :key="o.menuId" :label="o.title" :value="o.menuId" />
          </el-select>
          <div class="upload-tip" style="margin-top:8px">
            <el-icon :size="13"><InfoFilled /></el-icon>
            例如《入库操作手册》关联「入库单管理」，用户问"入库怎么操作"时会直接给出该页面入口。
          </div>
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

    <!-- 关联功能入口弹窗（文档 → 系统功能页面） -->
    <el-dialog v-model="navBindOpen" width="580px" destroy-on-close append-to-body
      class="ai-nav-bind-dialog" @closed="resetNavBind">
      <template #header>
        <div class="nbd-head">
          <span class="nbd-head-icon"><el-icon :size="16"><Connection /></el-icon></span>
          <div class="nbd-head-text">
            <div class="nbd-head-title">关联功能入口</div>
            <div class="nbd-head-sub">问答命中该文档时，直接给出对应的功能页面</div>
          </div>
        </div>
      </template>

      <!-- 当前文档：卡片化展示 -->
      <div class="nbd-doc">
        <div class="nbd-doc-icon"><el-icon :size="17"><Document /></el-icon></div>
        <div class="nbd-doc-main">
          <div class="nbd-doc-name" :title="navBindDoc.docName">{{ navBindDoc.docName || '—' }}</div>
          <div class="nbd-doc-meta">
            <span class="nbd-module-chip">{{ moduleNames[navBindDoc.module] || navBindDoc.module || '未分类' }}</span>
            <span class="nbd-meta-sep">·</span>
            <span class="nbd-meta-text">{{ (navBindDoc.fileType || '').toUpperCase() || '—' }}</span>
            <span class="nbd-meta-sep">·</span>
            <span class="nbd-meta-text">{{ navBindDoc.chunkCount || 0 }} 个切片</span>
          </div>
        </div>
      </div>

      <!-- 关系示意：让用户一眼看懂"文档 → 页面"这层映射 -->
      <div class="nbd-flow">
        <div class="nbd-flow-node">
          <span class="nbd-flow-label">用户提问</span>
          <span class="nbd-flow-value">命中该文档</span>
        </div>
        <span class="nbd-flow-arrow"></span>
        <div class="nbd-flow-node is-target" :class="{ 'is-empty': !navBindMenuId }">
          <span class="nbd-flow-label">点击直达</span>
          <span class="nbd-flow-value">{{ selectedMenu?.title || '尚未绑定' }}</span>
        </div>
      </div>

      <el-form label-position="top" class="nbd-form">
        <el-form-item label="目标功能页面">
          <el-select v-model="navBindMenuId" style="width: 100%" size="large" clearable filterable
            placeholder="选择该文档对应的系统功能页面">
            <el-option v-for="o in menuOptions" :key="o.menuId" :label="o.title" :value="o.menuId" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 路径预览：管理员确认跳转目标是否正确 -->
      <div v-if="navBindMenuId" class="nbd-preview">
        <div class="nbd-preview-row">
          <span class="nbd-preview-label">跳转路径</span>
          <span class="nbd-preview-path">{{ selectedMenu?.path || '—' }}</span>
        </div>
        <div class="nbd-preview-row">
          <span class="nbd-preview-label">菜单层级</span>
          <span class="nbd-preview-crumb">{{ selectedMenu?.title || '—' }}</span>
        </div>
      </div>

      <div class="nbd-tip">
        <el-icon :size="13"><InfoFilled /></el-icon>
        <div>
          <b>绑定后</b>按确定性映射返回入口，不调用大模型，不受模型波动影响；
          <b>清空</b>则回退为按问题语义自动匹配（依赖意图表与别名配置）。
        </div>
      </div>

      <template #footer>
        <el-button @click="navBindOpen = false">取 消</el-button>
        <el-button v-if="navBindDoc.navMenuId" plain type="danger" :loading="navBinding"
          @click="saveNavBind(true)">解除绑定</el-button>
        <el-button type="primary" :loading="navBinding" @click="saveNavBind(false)">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AiKnowledge">
import { computed } from 'vue'
import { listKnowledge, uploadKnowledge, delKnowledge, updateDocNav } from '@/api/ai/knowledge'
import { navMenuOptions } from '@/api/ai/nav'
import { useColumnResize } from '@/composables/useColumnResize'
import { UploadFilled, Search, InfoFilled, Connection, Document } from '@element-plus/icons-vue'

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
  createTime: { label: '上传时间', visible: true },
  navMenuId: { label: '功能入口', visible: true }
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
const uploadNavMenuId = ref(null)

/* ===== 关联功能入口 ===== */
const menuOptions = ref([])
const navBindOpen = ref(false)
const navBindDoc = ref({})
const navBindMenuId = ref(null)
const navBinding = ref(false)

/** 目标菜单下拉：只取当前登录人有权访问的可跳转页面 */
function loadMenuOptions() {
  if (menuOptions.value.length) return
  navMenuOptions().then(res => {
    menuOptions.value = res.data || []
  }).catch(() => {})
}

/** 列表里只显示菜单名（面包屑最后一段），完整层级放 title 提示 */
function navMenuTitle(menuId) {
  const o = menuOptions.value.find(x => x.menuId === menuId)
  if (!o) return '菜单#' + menuId
  return String(o.title).split(' / ').pop()
}

function navMenuFullTitle(menuId) {
  const o = menuOptions.value.find(x => x.menuId === menuId)
  return o ? o.title : ('菜单#' + menuId)
}

/** 当前弹框选中的菜单对象（用于路径预览） */
const selectedMenu = computed(() => {
  if (!navBindMenuId.value) return null
  return menuOptions.value.find(x => x.menuId === navBindMenuId.value) || null
})

function openNavBind(row) {
  loadMenuOptions()
  navBindDoc.value = row || {}
  navBindMenuId.value = row && row.navMenuId ? row.navMenuId : null
  navBindOpen.value = true
}

function resetNavBind() {
  navBindDoc.value = {}
  navBindMenuId.value = null
}

/**
 * 保存/解除绑定
 * @param {boolean} unbind true=解除绑定（显式传 null），false=按当前选择保存
 */
function saveNavBind(unbind) {
  const docId = navBindDoc.value.docId
  if (!docId) return
  const targetId = unbind ? null : navBindMenuId.value
  if (!unbind && !targetId) {
    proxy.$modal.msgWarning('请先选择目标功能页面')
    return
  }
  navBinding.value = true
  updateDocNav(docId, targetId).then(() => {
    proxy.$modal.msgSuccess(targetId ? '已关联功能入口' : '已解除关联')
    navBindOpen.value = false
    getList()
  }).catch(() => {}).finally(() => { navBinding.value = false })
}

function resetUploadForm() {
  uploadFile.value = null
  uploadModule.value = 'platform'
  uploadNavMenuId.value = null
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
  if (uploadNavMenuId.value) {
    fd.append('navMenuId', uploadNavMenuId.value)
  }
  uploadKnowledge(fd).then(() => {
    uploading.value = false
    uploadOpen.value = false
    uploadFile.value = null
    proxy.$modal.msgSuccess('上传解析成功')
    getList()
  }).catch(() => { uploading.value = false })
}

getList()
// 预载目标菜单清单：列表里"功能入口"列需要按 menuId 显示菜单名
loadMenuOptions()
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

/* ===== 列表内"功能入口"列 ===== */
.nav-bound-tag { max-width:118px; cursor:default; }
.nav-bound-tag :deep(span) { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.nav-bind-link {
  display:inline-flex; align-items:center; gap:3px; padding:2px 8px;
  font-size:12px; color:var(--brand-600); cursor:pointer; white-space:nowrap;
  background:var(--brand-50); border:1px dashed var(--brand-200);
  border-radius:var(--r-sm); transition:all .15s var(--ease-out);
}
.nav-bind-link:hover { background:var(--brand-100); border-color:var(--brand-500); color:var(--brand-700); }

</style>

<style>
/* 弹窗上传区铺满整行（弹层挂载在 body 下，必须用全局样式） */
.ai-upload-dialog .el-upload,
.ai-upload-dialog .el-upload-dragger {
  width: 100% !important;
}

/* ===== 关联功能入口弹窗 ===== */
.ai-nav-bind-dialog .nbd-head { display:flex; align-items:center; gap:10px; }
.ai-nav-bind-dialog .nbd-head-icon {
  width:34px; height:34px; flex:none; border-radius:10px; color:#fff;
  background:linear-gradient(135deg,#8b5cf6,#6366f1);
  display:flex; align-items:center; justify-content:center;
  box-shadow:0 3px 10px rgba(99,102,241,.28);
}
.ai-nav-bind-dialog .nbd-head-text { display:flex; flex-direction:column; gap:2px; }
.ai-nav-bind-dialog .nbd-head-title { font-size:15px; font-weight:600; color:#0f172a; line-height:1.3; }
.ai-nav-bind-dialog .nbd-head-sub { font-size:12px; color:#64748b; line-height:1.3; }

/* 文档卡片 */
.ai-nav-bind-dialog .nbd-doc {
  display:flex; align-items:center; gap:11px;
  padding:12px 14px; margin-bottom:14px;
  background:#f8fafc; border:1px solid #e2e8f0; border-radius:10px;
}
.ai-nav-bind-dialog .nbd-doc-icon {
  width:36px; height:36px; flex:none; border-radius:10px;
  background:#fff; border:1px solid #e0e7ff;
  color:#4f46e5;
  display:flex; align-items:center; justify-content:center;
}
.ai-nav-bind-dialog .nbd-doc-main { min-width:0; flex:1; display:flex; flex-direction:column; gap:5px; }
.ai-nav-bind-dialog .nbd-doc-name {
  font-size:13.5px; font-weight:600; color:#0f172a; line-height:1.4;
  overflow:hidden; text-overflow:ellipsis; white-space:nowrap;
}
.ai-nav-bind-dialog .nbd-doc-meta { display:flex; align-items:center; gap:6px; flex-wrap:wrap; }
.ai-nav-bind-dialog .nbd-module-chip {
  display:inline-flex; align-items:center; padding:2px 8px;
  border-radius:999px; font-size:11.5px; font-weight:600; line-height:1.4;
  background:#f5f3ff; color:#4338ca; border:1px solid #c7d2fe;
}
.ai-nav-bind-dialog .nbd-meta-sep { color:#cbd5e1; font-size:11px; }
.ai-nav-bind-dialog .nbd-meta-text { font-size:11.5px; color:#64748b; }

/* 关系示意 */
.ai-nav-bind-dialog .nbd-flow {
  display:flex; align-items:center; justify-content:center; gap:8px;
  padding:12px 14px; margin-bottom:16px;
  background:#f8faff; border:1px solid #e4e8ff; border-radius:10px;
}
.ai-nav-bind-dialog .nbd-flow-node {
  flex:1; min-width:0; display:flex; flex-direction:column; gap:3px;
  padding:8px 10px; background:#fff;
  border:1px solid #e0e7ff; border-radius:6px;
}
.ai-nav-bind-dialog .nbd-flow-node.is-target { border-color:#c7d2fe; background:#eef2ff; }
.ai-nav-bind-dialog .nbd-flow-node.is-empty { border-style:dashed; border-color:#cbd5e1; background:#fff; }
.ai-nav-bind-dialog .nbd-flow-label { font-size:11px; color:#94a3b8; line-height:1.2; }
.ai-nav-bind-dialog .nbd-flow-value {
  font-size:12.5px; font-weight:600; color:#334155; line-height:1.3;
  overflow:hidden; text-overflow:ellipsis; white-space:nowrap;
}
.ai-nav-bind-dialog .nbd-flow-node.is-target .nbd-flow-value { color:#4338ca; }
.ai-nav-bind-dialog .nbd-flow-node.is-empty .nbd-flow-value { color:#94a3b8; font-weight:500; }
.ai-nav-bind-dialog .nbd-flow-arrow {
  flex:none; width:24px; height:1px; position:relative;
  background:linear-gradient(90deg,#c7d2fe,#e0e7ff);
}
.ai-nav-bind-dialog .nbd-flow-arrow::after {
  content:''; position:absolute; right:0; top:-2.5px;
  border-left:5px solid #c7d2fe; border-top:3px solid transparent; border-bottom:3px solid transparent;
}

.ai-nav-bind-dialog .nbd-form :deep(.el-form-item__label) { font-size:13px; color:#334155; font-weight:500; padding-bottom:6px; }
.ai-nav-bind-dialog .nbd-form :deep(.el-form-item) { margin-bottom:12px; }

/* 路径预览 */
.ai-nav-bind-dialog .nbd-preview {
  padding:10px 14px; margin-bottom:12px;
  background:#fff; border:1px solid #e2e8f0;
  border-left:3px solid #6366f1; border-radius:6px;
}
.ai-nav-bind-dialog .nbd-preview-row { display:flex; align-items:baseline; gap:10px; line-height:1.8; }
.ai-nav-bind-dialog .nbd-preview-row + .nbd-preview-row { margin-top:2px; }
.ai-nav-bind-dialog .nbd-preview-label { flex:none; width:56px; font-size:11.5px; color:#94a3b8; }
.ai-nav-bind-dialog .nbd-preview-path {
  font-family:ui-monospace, Menlo, Consolas, monospace; font-size:12px;
  color:#4338ca; word-break:break-all;
}
.ai-nav-bind-dialog .nbd-preview-crumb { font-size:12px; color:#64748b; word-break:break-all; }

.ai-nav-bind-dialog .nbd-tip {
  display:flex; align-items:flex-start; gap:6px;
  font-size:12px; color:#64748b; line-height:1.65;
  background:#f8fafc; border:1px solid #e2e8f0;
  border-radius:6px; padding:9px 12px;
}
.ai-nav-bind-dialog .nbd-tip .el-icon { margin-top:2px; color:#4f46e5; flex:none; }
.ai-nav-bind-dialog .nbd-tip b { color:#334155; font-weight:600; }
</style>
