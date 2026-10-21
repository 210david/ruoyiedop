<template>
  <div class="app-container flowable-launch-page">
    <!-- 列表模式：仅菜单「流程发起」时渲染（独立页 /flow/launch/create 不显示列表，避免跳转闪烁） -->
    <template v-if="!isStandaloneLaunch">
    <!-- ===== 搜索 ===== -->
    <div class="surface filter-card" v-show="showSearch">
      <div class="filter-head">
        <div class="filter-title"><span class="glyph"></span> 可发起的流程</div>
        <div class="control" style="width:280px">
          <el-input v-model="keyword" placeholder="搜索流程名称" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- ===== 分类区块 + 流程名称列表（参照e9新建流程） ===== -->
    <div class="surface" v-loading="loading">
      <div class="status-tabs">
        <div class="tabs-track">
          <button class="status-tab" :class="{ 'is-active': activeCategory === 'all' }" @click="activeCategory = 'all'">
            <span class="dot"></span><span>全部</span><span class="count">{{ defs.length }}</span>
          </button>
          <button v-for="c in categoryGroups" :key="c.value" class="status-tab"
            :class="{ 'is-active': activeCategory === c.value }" @click="activeCategory = c.value">
            <span class="dot"></span><span>{{ c.label }}</span><span class="count">{{ c.items.length }}</span>
          </button>
        </div>
      </div>

      <div class="cat-groups">
        <section class="cat-card" v-for="g in visibleGroups" :key="g.value">
          <div class="cat-top" :class="cardColor(g.value)"></div>
          <div class="cat-head">
            <div class="cat-icon"><el-icon :size="20"><Files /></el-icon></div>
            <div class="cat-name">{{ g.label }}</div>
            <div class="cat-count">{{ g.items.length }}</div>
          </div>
          <div class="cat-list">
            <div class="flow-item" v-for="d in g.items" :key="d.definitionId" @click="handleLaunch(d)">
              <el-icon class="flow-glyph" :size="16"><Promotion /></el-icon>
              <span class="flow-name">{{ d.modelName || d.modelKey }}</span>
              <span class="flow-ver">v{{ d.version }}</span>
              <el-icon class="flow-go"><ArrowRight /></el-icon>
            </div>
          </div>
        </section>
        <el-empty v-if="!loading && !visibleGroups.length" description="暂无可发起的流程，请先在后台部署"
          style="grid-column:1/-1" />
      </div>
    </div>
    </template>

    <!-- ===== 发起流程：泛微式全屏页面（顶部：标题 + 页签 + 操作按钮；下方：内容区） ===== -->
    <el-dialog v-model="open" fullscreen append-to-body class="launch-dialog" :close-on-click-modal="false" destroy-on-close :show-close="false">
      <template #header>
        <div class="launch-header">
          <!-- 第一行：标题 + 操作按钮 -->
          <div class="launch-titlebar">
            <div class="lt-title">
              <span class="lt-glyph"></span>
              <span class="lt-name">{{ current.modelName || '发起流程' }}</span>
              <span class="lt-ver" v-if="current.version">V{{ current.version }}</span>
              <span class="lt-form" v-if="boundForm">
                <el-icon><Document /></el-icon>{{ boundForm.formName }}
              </span>
            </div>
            <div class="lt-actions">
              <el-button type="primary" icon="Check" :loading="submitting" @click="doLaunch">提交</el-button>
              <el-button icon="Finished" @click="saveDraft">保存</el-button>
            </div>
          </div>
          <!-- 第二行：页签 -->
          <div class="launch-tabs">
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'form' }" @click="launchTab = 'form'">流程表单</button>
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'diagram' }" @click="launchTab = 'diagram'">流程图</button>
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'status' }" @click="launchTab = 'status'">流程状态</button>
            <button class="launch-tab" :class="{ 'is-active': launchTab === 'resource' }" @click="launchTab = 'resource'">相关资源</button>
          </div>
        </div>
      </template>

      <div class="launch-body">
        <!-- 页签：流程表单 -->
        <div class="launch-pane" v-show="launchTab === 'form'">
          <div v-if="boundForm && boundForm.fields && boundForm.fields.length">
            <el-form ref="dynRef" :model="dynData" label-width="120px">
              <FormGridRender v-if="layoutMode === 'grid' && layoutGrid" ref="gridRenderRef" :grid="layoutGrid"
                :fields="boundForm.fields" :values="dynData" editable @browse="openBrowse" />
              <template v-else>
                <template v-for="(grp, gi) in renderGroups" :key="gi">
            <div class="dyn-section-title" v-if="grp.title">{{ grp.title }}</div>
            <el-row :gutter="8">
              <el-col v-for="c in grp.cells" :key="c.key" :span="c.span || 24">
                <div v-if="c.detailTable" class="dyn-detail-block">
                  <div class="dyn-detail-bar">
                    <span class="dyn-detail-name"><el-icon><Grid /></el-icon> {{ detailTableLabel(c.detailTable) }}</span>
                    <span class="dyn-detail-ops">
                      <el-button size="small" link type="primary" icon="Plus" @click="addDetailRow(c.detailTable)">添加明细</el-button>
                    </span>
                  </div>
                  <table class="dyn-detail-table" v-if="detailTableFields(c.detailTable).length">
                    <thead>
                      <tr>
                        <th style="width:48px">序号</th>
                        <th v-for="f in detailTableFields(c.detailTable)" :key="f.field">
                          {{ f.label }}<span v-if="f.required" style="color:#ef4444"> *</span>
                        </th>
                        <th style="width:48px">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, ri) in (detailRows[c.detailTable] || [])" :key="row._key">
                        <td style="text-align:center;color:#94a3b8">{{ ri + 1 }}</td>
                        <td v-for="f in detailTableFields(c.detailTable)" :key="f.field">
                          <dyn-field :f="f" v-model="row[f.field]" @browse="openBrowse(f)" />
                        </td>
                        <td style="text-align:center">
                          <el-button size="small" link type="danger" icon="Delete" @click="removeDetailRow(c.detailTable, ri)" />
                        </td>
                      </tr>
                      <tr v-if="(detailRows[c.detailTable] || []).length" class="dyn-detail-sum-row">
                        <td style="text-align:center">合计</td>
                        <td v-for="f in detailTableFields(c.detailTable)" :key="f.field" style="text-align:right;color:#7c3aed;font-weight:600">
                          <template v-if="(c.detailSum || []).includes(f.field)">Σ {{ detailSumValue(c, f.field) }}</template>
                        </td>
                        <td></td>
                      </tr>
                    </tbody>
                  </table>
                  <div v-else class="dyn-detail-empty">明细表{{ c.detailTable }}暂无字段，请先到表单管理"编辑字段"中添加</div>
                </div>
                <div v-else-if="c.note" class="dyn-note" :style="dynCellStyle(c)">{{ c.note }}</div>
                <div v-else-if="c.style && (c.style.bgColor || c.style.border)" class="dyn-cell-wrap" :style="dynCellStyle(c)">
                  <el-form-item :label="c.f.label" :prop="c.f.field"
                    :rules="c.f.required ? [{ required: true, message: '请填写' + c.f.label, trigger: ['blur','change'] }] : []">
                    <dyn-field :f="c.f" v-model="dynData[c.f.field]" @browse="openBrowse(c.f)" />
                  </el-form-item>
                </div>
                <el-form-item v-else :label="c.f.label" :prop="c.f.field" :style="dynCellStyle(c)"
                  :rules="c.f.required ? [{ required: true, message: '请填写' + c.f.label, trigger: ['blur','change'] }] : []">
                  <dyn-field :f="c.f" v-model="dynData[c.f.field]" @browse="openBrowse(c.f)" />
                </el-form-item>
              </el-col>
            </el-row>
                </template>
              </template>
            </el-form>
          </div>
          <div v-else>
            <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
              title="该流程未绑定表单" description="仍可直接发起（填写下方「发起信息」即可）；如需填写业务表单，请先到后台表单管理设计表单并在模型基本信息中关联" />
          </div>

          <!-- 发起信息（流程标题 / 首节点审批人） -->
          <section class="launch-card">
            <div class="launch-card-head"><span class="lc-icon">◈</span>发起信息</div>
            <div class="launch-card-body">
              <el-form label-width="120px">
                <el-form-item label="流程标题">
                  <el-input v-model="title" placeholder="默认自动生成：流程名-申请人-日期" />
                </el-form-item>
                <el-form-item label="首节点审批人">
                  <el-select v-model="approverUserId" placeholder="选择审批人（将进入其待办）" filterable style="width:100%">
                    <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="u.userId" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </section>
        </div>

        <!-- 页签：流程图（发起前预览，未产生实例，展示设计态流程） -->
        <div class="launch-pane" v-show="launchTab === 'diagram'">
          <section class="launch-card">
            <div class="launch-card-head"><span class="lc-icon">◈</span>流程图（发起前预览）</div>
            <div class="launch-card-body">
              <div class="diagram-wrap">
                <img v-if="previewDiagramUrl" :src="previewDiagramUrl" class="diagram-img" alt="流程图" />
                <div v-else-if="previewDiagramLoading" class="diagram-empty">流程图加载中…</div>
                <div v-else class="diagram-empty">暂无流程图，请先在设计器中绘制并部署</div>
              </div>
            </div>
          </section>
        </div>

        <!-- 页签：流程状态（发起前无实例，展示待发起状态） -->
        <div class="launch-pane" v-show="launchTab === 'status'">
          <div class="status-cards">
            <div class="status-card"><div class="sc-num">1</div><div class="sc-label">总人次</div></div>
            <div class="status-card"><div class="sc-num sc-amber">0</div><div class="sc-label">已提交</div></div>
            <div class="status-card"><div class="sc-num">1</div><div class="sc-label">未提交</div></div>
            <div class="status-card"><div class="sc-num">{{ launchNodeCount }}</div><div class="sc-label">节点数</div></div>
          </div>
          <section class="launch-card">
            <div class="launch-card-head"><span class="lc-icon">≡</span>流转明细</div>
            <div class="launch-card-body">
              <el-table :data="launchStatusRows" border size="small">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column label="节点" prop="taskName" min-width="140" show-overflow-tooltip />
                <el-table-column label="操作人" prop="assigneeName" width="140">
                  <template #default="s">{{ s.row.assigneeName || '-' }}</template>
                </el-table-column>
                <el-table-column label="状态" width="110" align="center">
                  <template #default="s">
                    <span class="badge" :class="s.row.done ? 'green' : 'amber'"><span class="dot"></span>{{ s.row.done ? '已提交' : '待提交' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="接收时间" prop="createTime" width="170" align="center">
                  <template #default="s">{{ s.row.createTime || '-' }}</template>
                </el-table-column>
              </el-table>
            </div>
          </section>
        </div>

        <!-- 页签：相关资源 -->
        <div class="launch-pane" v-show="launchTab === 'resource'">
          <section class="launch-card">
            <div class="launch-card-head"><span class="lc-icon">⊞</span>相关资源</div>
            <div class="launch-card-body">
              <el-table :data="launchResources" border size="small">
                <el-table-column label="资源名称" prop="name" min-width="220" show-overflow-tooltip />
                <el-table-column label="类型" prop="type" width="140" align="center" />
                <el-table-column label="说明" prop="desc" min-width="220" show-overflow-tooltip />
              </el-table>
              <div class="resource-empty">发起后，流程中上传的附件与关联单据将在此汇总展示</div>
            </div>
          </section>
        </div>
      </div>
    </el-dialog>

    <!-- 浏览按钮：主数据选择弹窗（人员/部门/岗位/角色/物料/客户/供应商/合同） -->
    <el-dialog v-model="browseOpen" :title="'选择' + (browseTypeLabel(currentBrowse) || '数据')" width="640px" append-to-body :close-on-click-modal="false">
      <el-input v-model="browseKeyword" placeholder="输入关键字搜索，回车确认" clearable style="margin-bottom:10px" @keyup.enter="loadBrowseData" />
      <el-table :data="browseRows" border size="small" max-height="380" v-loading="browseLoading"
        @selection-change="s => browseSelection = s" @row-dblclick="row => { browseSelection = [row]; confirmBrowse() }">
        <el-table-column type="selection" width="45" align="center" />
        <el-table-column v-for="col in browseColumns" :key="col.prop" :label="col.label" :prop="col.prop" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="confirmBrowse">确 定</el-button>
        <el-button @click="browseOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FlowLaunch">
import { listDefinition } from '@/api/flowable/flow'
import { formByProcess, launch } from '@/api/flowable/form'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
import { Search, Promotion, ArrowRight, Files } from '@element-plus/icons-vue'
import FormGridRender from '@/components/FormGridRender.vue'
import { parseLayout } from '@/views/flowable/form/designer/converters'
import { useRoute, useRouter } from 'vue-router'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const showSearch = ref(true)
const defs = ref([])
const keyword = ref('')
const activeCategory = ref('all')
const open = ref(false)
const current = ref({})
const boundForm = ref(null)
const dynData = ref({})
const approverUserId = ref(undefined)
const title = ref('')
const userOptions = ref([])
const submitting = ref(false)
/** 全屏发起页页签：流程表单 / 流程图 / 流程状态 / 相关资源 */
const launchTab = ref('form')
/** 发起前流程图预览（部署态的流程定义图，尚未产生实例） */
const previewDiagramUrl = ref('')
/** 流程图加载中标志 */
const previewDiagramLoading = ref(false)
/** 发起前流程状态：节点数 + 待提交行 */
const launchNodeCount = ref(0)
const launchStatusRows = ref([])
/** 相关资源：发起阶段主要为关联表单与流程定义信息 */
const launchResources = ref([])
/** 表单布局解析：mode grid → FormGridRender 网格渲染；section → 分组渲染 */
const layoutParsed = ref({ mode: 'section', sections: [], grid: null })
const layoutMode = computed(() => layoutParsed.value.mode)
const layoutGrid = computed(() => layoutParsed.value.grid)
const gridRenderRef = ref(null)

const filteredDefs = computed(() => {
  if (!keyword.value) return defs.value
  return defs.value.filter(d => (d.modelName || '').includes(keyword.value) || d.modelKey.includes(keyword.value))
})

// 按分类分组
const categoryGroups = computed(() => {
  const map = {}
  filteredDefs.value.forEach(d => {
    const key = d.category || 'biz'
    ;(map[key] = map[key] || { value: key, label: catLabel(key), items: [] }).items.push(d)
  })
  return Object.values(map)
})
const visibleGroups = computed(() =>
  activeCategory.value === 'all' ? categoryGroups.value : categoryGroups.value.filter(g => g.value === activeCategory.value)
)
function catLabel(v) {
  const { flowable_category } = proxy.useDict('flowable_category')
  const item = flowable_category.value.find(d => d.value == v)
  return item ? item.label : (v || '未分类')
}
function cardColor(v) {
  return { oa: 'c-blue', hr: 'c-green', fin: 'c-amber', pms: 'c-red', biz: 'c-violet' }[v] || 'c-blue'
}

function getList() {
  loading.value = true
  listDefinition({ pageNum: 1, pageSize: 100 }).then(res => {
    defs.value = res.rows || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

function loadUsers() {
  request({ url: '/system/user/list', method: 'get', params: { pageNum: 1, pageSize: 200 } })
    .then(res => { userOptions.value = res.rows || [] })
    .catch(e => { proxy.$modal.msgWarning('审批人列表加载失败，将无法选择首节点审批人：' + (e?.message || e)) })
}

/** 加载发起前流程图预览（按流程 key 取已部署定义的 BPMN 图）
 *  注意：<img src> 不会携带 Authorization 头，直接指向接口会 401，
 *  因此用 request(blob) 取回后再转成 objectURL
 */
function loadPreviewDiagram() {
  previewDiagramUrl.value = ''
  previewDiagramLoading.value = true
  if (!current.value.modelKey) { previewDiagramLoading.value = false; return }
  request({
    url: '/flowable/definition/diagram/' + current.value.modelKey,
    method: 'get',
    responseType: 'blob'
  }).then(res => {
    previewDiagramUrl.value = URL.createObjectURL(new Blob([res], { type: 'image/png' }))
  }).catch(() => {
    previewDiagramUrl.value = ''
  }).finally(() => {
    previewDiagramLoading.value = false
  })
}

/** 发起前流程状态：节点数取自流程图预览，流转明细为待提交一行 */
function buildLaunchStatus() {
  launchStatusRows.value = [{
    taskName: '发起节点（申请人提交）',
    assigneeName: userStore.nickName || userStore.name || '',
    done: false,
    createTime: ''
  }]
}

/** 相关资源：发起阶段展示关联表单与流程定义基础信息 */
function buildLaunchResources() {
  const rows = []
  if (boundForm.value) {
    rows.push({
      name: boundForm.value.formName || '流程表单',
      type: '关联表单',
      desc: boundForm.value.tableName ? '数据表：' + boundForm.value.tableName : '发起时渲染填写'
    })
  }
  if (current.value.modelName) {
    rows.push({
      name: current.value.modelName,
      type: '流程定义',
      desc: '流程Key：' + (current.value.modelKey || '-') + '，版本 V' + (current.value.version || '-')
    })
  }
  launchResources.value = rows
}

/** 保存草稿：本地暂存表单填写内容（不发起流程） */
function saveDraft() {
  try {
    const draftKey = 'flow_launch_draft_' + (current.value.modelKey || 'unknown')
    localStorage.setItem(draftKey, JSON.stringify({
      title: title.value,
      approverUserId: approverUserId.value,
      dynData: dynData.value,
      savedAt: Date.now()
    }))
    proxy.$modal.msgSuccess('已保存草稿（本地暂存，未发起流程）')
  } catch (e) {
    proxy.$modal.msgError('草稿保存失败')
  }
}

/** 关闭全屏发起页 */
function closeLaunch() {
  open.value = false
}

/** 字段归一化：新版 {form,dtype} 与旧版 {type} 统一为渲染类型 rtype；select 兼容键值对/字符串/数组 */
function normalizeField(f) {
  const g = { ...f }
  let t = f.type
  if (f.form) {
    if (['check', 'browse', 'attach', 'select', 'textarea', 'radio', 'checkbox', 'switch', 'rate'].includes(f.form)) t = f.form
    else if (f.form === 'date' || f.form === 'datetime') t = f.form
    else if (f.form === 'number') t = 'number'
    else t = f.dtype === 'datetime' ? 'datetime' : f.dtype === 'date' ? 'date' : (f.dtype === 'float' || f.dtype === 'int') ? 'number' : 'text'
  } else if (!t) t = 'text'
  g.rtype = t
  if (t === 'select' || t === 'radio' || t === 'checkbox') {
    // 新版键值对 selectOptions: [{label,value}]；旧版 options 字符串/数组兼容（label=value）
    if (Array.isArray(f.selectOptions) && f.selectOptions.length) {
      g.optionList = f.selectOptions.map(o => typeof o === 'object' ? { label: o.label, value: o.value !== undefined ? o.value : o.label } : { label: o, value: o })
    } else {
      const raw = f.options
      const arr = Array.isArray(raw) ? raw : String(raw || '').split(/[,，、;；]/).map(s => s.trim()).filter(Boolean)
      g.optionList = arr.map(o => (typeof o === 'object') ? o : parseOptionPair(o))
    }
  }
  return g
}

/** 单个选项文本 "显示值:存储值" → {label,value} */
function parseOptionPair(s) {
  const idx = String(s).indexOf(':')
  if (idx > 0) return { label: String(s).substring(0, idx).trim(), value: String(s).substring(idx + 1).trim() }
  return { label: s, value: s }
}

async function handleLaunch(d) {
  // 泛微式：点击流程在浏览器新标签页打开独立发起页（不在当前页面弹窗）
  const routeUrl = router.resolve({
    path: '/flow/launch/create',
    query: { key: d.modelKey, name: d.modelName || '', version: d.version || '' }
  }).href
  window.open(routeUrl, '_blank')
}

/** 独立发起页初始化：按 URL 参数加载流程定义与绑定表单 */
async function initLaunchFromRoute() {
  const key = route.query.key
  if (!key) {
    proxy.$modal.msgWarning('缺少流程标识，无法发起')
    return
  }
  loading.value = false
  const hit = defs.value.find(x => x.modelKey === key)
  const target = hit || {
    modelKey: key,
    modelName: route.query.name || '',
    version: route.query.version ? Number(route.query.version) : undefined
  }
  await prepareLaunch(target)
}

async function prepareLaunch(d) {
  current.value = d
  approverUserId.value = undefined
  dynData.value = {}
  title.value = (d.modelName || '') + '-' + (userStore.nickName || '') + '-' + new Date().toISOString().slice(0, 10)
  boundForm.value = null
  launchTab.value = 'form'
  try {
    const res = await formByProcess(d.modelKey)
    if (res.data && res.data.fieldsJson) {
      let fields = []
      try { fields = (JSON.parse(res.data.fieldsJson) || []).map(normalizeField) } catch (e) {}
      layoutParsed.value = parseLayout(res.data.layoutJson)
      boundForm.value = { ...res.data, fields, layoutJson: res.data.layoutJson }
      initDetailRows()
    }
  } catch (e) { /* 无绑定表单 */ }
  // 恢复本地草稿（若有）
  try {
    const draft = JSON.parse(localStorage.getItem('flow_launch_draft_' + d.modelKey) || 'null')
    if (draft && draft.dynData) {
      dynData.value = draft.dynData
      if (draft.title) title.value = draft.title
      if (draft.approverUserId) approverUserId.value = draft.approverUserId
    }
  } catch (e) {}
  loadUsers()
  loadPreviewDiagram()
  buildLaunchStatus()
  buildLaunchResources()
  open.value = true
}

// ===== 页面布局渲染（按表单 layoutJson 分组分栏；无布局回退单栏） =====
const renderGroups = computed(() => {
  const all = boundForm.value?.fields || []
  if (!all.length) return []
  let layout = { sections: [] }
  try { const o = JSON.parse(boundForm.value?.layoutJson || '{}'); if (Array.isArray(o.sections)) layout = o } catch (e) {}
  const byField = {}
  all.forEach(f => { byField[f.field] = f })
  const used = new Set()
  const detailUsed = new Set()
  const groups = []
  let noteSeq = 0
  let dtSeq = 0
  const spanOf = s => { const n = Number(s); return ([6, 8, 12, 24].includes(n) || (n > 6 && n < 24)) ? n : 24 }
  for (const sec of layout.sections) {
    const cells = (sec.rows || []).flat()
      .filter(c => c && ((c.field && byField[c.field]) || c.note || c.detailTable))
      .map(c => {
        if (c.detailTable) { detailUsed.add(c.detailTable); return { key: 'dtb' + (++dtSeq), detailTable: c.detailTable, detailSum: c.detailSum || [], span: 24, style: {} } }
        if (c.note) return { key: 'note' + (++noteSeq), note: c.note, span: spanOf(c.span), style: c }
        used.add(c.field)
        return { key: c.field, f: { ...byField[c.field], label: c.label || byField[c.field].label }, span: spanOf(c.span), style: c }
      })
    if (cells.length) groups.push({ title: sec.title || '', cells })
  }
  // 未布局字段回退区：仅主表字段（明细字段由明细表块承载）
  const rest = all.filter(f => !used.has(f.field) && (!f.table || f.table === 'main'))
  if (rest.length) groups.push({ title: layout.sections.length ? '其他信息' : '', cells: rest.map(f => ({ key: f.field, f, span: 24, style: {} })) })
  // 布局中未出现但字段里存在的明细表：追加默认明细表块，避免明细字段丢失
  for (const dt of new Set(all.filter(f => f.table && f.table !== 'main').map(f => f.table))) {
    if (!detailUsed.has(dt)) groups.push({ title: '', cells: [{ key: 'dtb_auto_' + dt, detailTable: dt, detailSum: [], span: 24, style: {} }] })
  }
  return groups
})

// ===== 明细表运行时（参照泛微：可编辑表格 + 添加/删除行 + 表尾合计） =====
const detailRows = ref({})
/** 按布局初始化各明细表行（默认 1 行，泛微式"新增默认明细行数"） */
function initDetailRows() {
  const rows = {}
  for (const grp of renderGroups.value) {
    for (const c of grp.cells) {
      if (c.detailTable && !rows[c.detailTable]) {
        const fs = detailTableFields(c.detailTable)
        rows[c.detailTable] = fs.length ? [blankDetailRow(fs)] : []
      }
    }
  }
  detailRows.value = rows
}
function detailTableFields(dt) {
  return (boundForm.value?.fields || []).filter(f => f.table === dt)
}
/** 明细表显示名：dt1 → 明细表1 */
function detailTableLabel(dt) {
  const set = [...new Set((boundForm.value?.fields || []).filter(f => f.table && f.table !== 'main').map(f => f.table))]
  const idx = set.indexOf(dt)
  return '明细表' + (idx >= 0 ? String(idx + 1) : dt.slice(2))
}
function blankDetailRow(fs) {
  const row = { _key: 'r' + Math.random().toString(36).slice(2, 8) }
  fs.forEach(f => { row[f.field] = undefined })
  return row
}
function addDetailRow(dt) {
  if (!detailRows.value[dt]) detailRows.value[dt] = []
  detailRows.value[dt].push(blankDetailRow(detailTableFields(dt)))
}
function removeDetailRow(dt, idx) {
  detailRows.value[dt].splice(idx, 1)
  if (!detailRows.value[dt].length) addDetailRow(dt)
}
/** 表尾合计：对 detailSum 字段逐列求和（仅数值） */
function detailSumValue(cell, field) {
  const rows = detailRows.value[cell.detailTable] || []
  return rows.reduce((s, r) => s + (Number(r[field]) || 0), 0)
}

/** 布局单元格样式 → 发起页内联样式（字体/字号/粗斜体/文字色/背景/边框） */
function dynCellStyle(c) {
  const s = c && c.style ? c.style : {}
  const o = {}
  if (s.fontFamily) o.fontFamily = s.fontFamily
  if (s.fontSize) o.fontSize = s.fontSize + 'px'
  if (s.bold) o.fontWeight = '700'
  if (s.italic) o.fontStyle = 'italic'
  if (s.color) o.color = s.color
  if (s.bgColor) o.backgroundColor = s.bgColor
  if (s.borderColor) o.borderColor = s.borderColor
  if (s.border === 'all') { o.border = '1px solid ' + (s.borderColor || '#94a3b8'); o.borderRadius = '4px'; o.padding = '4px 8px'; o.marginBottom = '4px' }
  if (s.border === 'bottom') { o.borderBottom = '2px solid ' + (s.borderColor || '#94a3b8'); o.paddingBottom = '4px' }
  return o
}

// 浏览按钮（browse）：多数据源主数据选择（人员/部门/岗位/角色/物料/客户/供应商/合同，支持自定义扩展）
const browseOpen = ref(false)
const browseField = ref(null)
const browseSelection = ref([])
const browseRows = ref([])
const browseColumns = ref([])
const browseKeyword = ref('')
const browseLoading = ref(false)

/** 数据源定义：API + 列配置 + 选中取值字段（与显示字段分离） */
const BROWSE_SOURCES = {
  user:     { label: '人员',     api: '/system/user/list',    columns: [{ prop: 'nickName', label: '姓名' }, { prop: 'userName', label: '账号' }, { prop: 'email', label: '邮箱' }], valueKey: 'userName', labelKey: 'nickName' },
  dept:     { label: '部门',     api: '/system/dept/list',    columns: [{ prop: 'deptName', label: '部门名称' }, { prop: 'leader', label: '负责人' }], valueKey: 'deptId', labelKey: 'deptName' },
  post:     { label: '岗位',     api: '/system/post/list',    columns: [{ prop: 'postName', label: '岗位名称' }, { prop: 'postCode', label: '岗位编码' }], valueKey: 'postCode', labelKey: 'postName' },
  role:     { label: '角色',     api: '/system/role/list',    columns: [{ prop: 'roleName', label: '角色名称' }, { prop: 'roleKey', label: '权限字符' }], valueKey: 'roleId', labelKey: 'roleName' },
  material: { label: '物料',     api: '/wms/material/list',   columns: [{ prop: 'materialName', label: '物料名称' }, { prop: 'materialCode', label: '物料编码' }, { prop: 'specModel', label: '规格型号' }], valueKey: 'materialCode', labelKey: 'materialName' },
  customer: { label: '客户',     api: '/mk/customer/list',    columns: [{ prop: 'customerName', label: '客户名称' }, { prop: 'customerNo', label: '客户编码' }], valueKey: 'customerNo', labelKey: 'customerName' },
  supplier: { label: '供应商',   api: '/wms/supplier/list',   columns: [{ prop: 'supplierName', label: '供应商名称' }, { prop: 'supplierCode', label: '供应商编码' }], valueKey: 'supplierCode', labelKey: 'supplierName' },
  contract: { label: '合同',     api: '/mk/contract/list',    columns: [{ prop: 'contractName', label: '合同名称' }, { prop: 'contractNo', label: '合同编号' }], valueKey: 'contractNo', labelKey: 'contractName' },
  custom:   { label: '自定义',   api: null, columns: [{ prop: 'name', label: '名称' }], valueKey: 'id', labelKey: 'name' }
}

function browseTypeLabel(f) {
  const bt = (f && (f.browseType || 'user')) || 'user'
  return (BROWSE_SOURCES[bt] || {}).label
}

/** 附件类型约束 → file-upload 组件 accept */
function attachAccept(f) {
  const t = (f && f.attachType) || 'all'
  if (t === 'image') return ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp']
  if (t === 'doc') return ['doc', 'docx', 'pdf', 'xls', 'xlsx', 'ppt', 'pptx', 'txt']
  return undefined
}

function openBrowse(f) {
  browseField.value = f
  browseSelection.value = []
  browseKeyword.value = ''
  const src = BROWSE_SOURCES[f.browseType || 'user'] || BROWSE_SOURCES.user
  browseColumns.value = src.columns
  browseOpen.value = true
  loadBrowseData()
}

/** 按数据源加载主数据列表 */
function loadBrowseData() {
  const f = browseField.value
  const src = BROWSE_SOURCES[(f && f.browseType) || 'user'] || BROWSE_SOURCES.user
  if (!src.api) { browseRows.value = []; return }
  browseLoading.value = true
  request({ url: src.api, method: 'get', params: { pageNum: 1, pageSize: 200, keyword: browseKeyword.value || undefined } })
    .then(res => { browseRows.value = res.rows || res.data || [] })
    .catch(() => { browseRows.value = [] })
    .finally(() => { browseLoading.value = false })
}

function confirmBrowse() {
  const f = browseField.value
  if (!f) return
  const src = BROWSE_SOURCES[f.browseType || 'user'] || BROWSE_SOURCES.user
  // 存储实际值（编码/ID），非显示名称（泛微式：显示与存储分离）
  const values = (browseSelection.value || []).map(r => String(r[src.valueKey] !== undefined ? r[src.valueKey] : (r[src.labelKey] || '')))
  dynData.value[f.field] = values.join(',')
  browseOpen.value = false
}

function doLaunch() {
  if (boundForm.value && boundForm.value.fields && boundForm.value.fields.length) {
    proxy.$refs['dynRef'].validate(valid => { if (valid) submitLaunch() })
    return
  }
  submitLaunch()
}

function submitLaunch() {
  if (!approverUserId.value) { proxy.$modal.msgWarning('请选择首节点审批人'); return }
  submitting.value = true
  // 简易分组模式：明细行数据从 detailRows 提取；网格模式：从 FormGridRender 实例取
  let detailData = {}
  const rows = {}
  for (const [dt, list] of Object.entries(detailRows.value || {})) {
    if (list.length) rows[dt] = list.map(({ _key, ...rest }) => rest)
  }
  if (Object.keys(rows).length) detailData = rows
  else if (gridRenderRef.value && gridRenderRef.value.getDetailData) detailData = gridRenderRef.value.getDetailData()
  // 明细属性校验：必须填写明细（mustAdd）→ 至少一行且必填列已填（网格模式经实例校验）
  if (gridRenderRef.value && gridRenderRef.value.validateDetails) {
    const msgs = gridRenderRef.value.validateDetails()
    if (msgs.length) { proxy.$modal.msgWarning(msgs[0]); submitting.value = false; return }
  }
  const variables = { ...dynData.value }
  if (Object.keys(detailData).length) variables.detailData = detailData
  launch({
    definitionKey: current.value.modelKey,
    formId: boundForm.value ? boundForm.value.formId : null,
    approverUserId: approverUserId.value,
    title: title.value,
    variables
  }).then(() => {
    proxy.$modal.msgSuccess('流程已发起，等待审批人处理')
    open.value = false
  }).finally(() => { submitting.value = false })
}

/** 是否独立发起页（新标签页打开：/flow/launch/create?key=xxx） */
const isStandaloneLaunch = computed(() => route.path === '/flow/launch/create')

if (isStandaloneLaunch.value) {
  // 独立发起页：直接加载表单详情，不展示流程列表
  initLaunchFromRoute()
} else {
  // 列表页：加载可发起流程
  getList()
}
</script>

<script>
import { defineComponent, h, resolveComponent } from 'vue'
import { ElSelect, ElOption, ElRadioGroup, ElRadio, ElCheckbox, ElCheckboxGroup, ElSwitch, ElRate, ElInput, ElInputNumber, ElDatePicker, ElButton } from 'element-plus'

/** 动态字段控件：按 rtype 渲染对应 Element 控件（布局样式渲染共用） */
export const DynField = defineComponent({
  name: 'DynField',
  props: { f: { type: Object, required: true }, modelValue: { type: [String, Number, Boolean], default: undefined } },
  emits: ['update:modelValue', 'browse'],
  setup(props, { emit }) {
    const set = v => emit('update:modelValue', v)
    return () => {
      const f = props.f
      const v = props.modelValue
      switch (f.rtype) {
        case 'select': return h(ElSelect, { modelValue: v, 'onUpdate:modelValue': set, style: 'width:100%', placeholder: f.placeholder || '请选择' },
          () => (f.optionList || []).map(o => h(ElOption, { key: o.value, label: o.label, value: o.value })))
        case 'radio': return h(ElRadioGroup, { modelValue: v, 'onUpdate:modelValue': set },
          () => (f.optionList || []).map(o => h(ElRadio, { key: o.value, value: o.value }, () => o.label)))
        case 'checkbox': return h(ElCheckboxGroup, {
          modelValue: v ? String(v).split(',').filter(Boolean) : [],
          'onUpdate:modelValue': arr => set((arr || []).join(','))
        }, () => (f.optionList || []).map(o => h(ElCheckbox, { key: o.value, value: o.value }, () => o.label)))
        case 'check': return h(ElCheckbox, { modelValue: v, 'onUpdate:modelValue': set, trueValue: '1', falseValue: '0' }, () => '是')
        case 'switch': return h(ElSwitch, { modelValue: v, 'onUpdate:modelValue': set, activeValue: '1', inactiveValue: '0' })
        case 'rate': return h(ElRate, { modelValue: v, 'onUpdate:modelValue': set })
        case 'browse': return h('div', { style: 'display:flex;gap:8px;width:100%' }, [
          h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, readonly: true, placeholder: '点击右侧按钮选择数据' }),
          h(ElButton, { onClick: () => emit('browse') }, () => '浏览…')
        ])
        case 'attach': return h(resolveComponent('FileUpload'), { modelValue: v, 'onUpdate:modelValue': set, limit: f.attachLimit || 5, fileSize: 50 })
        case 'textarea': return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, type: 'textarea', rows: f.rows || 4, placeholder: f.placeholder })
        case 'number': return h(ElInputNumber, { modelValue: v, 'onUpdate:modelValue': set, controls: false, style: 'width:100%', placeholder: f.placeholder || '请输入' })
        case 'date': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'date', valueFormat: 'YYYY-MM-DD', placeholder: f.placeholder || '选择日期', style: 'width:100%' })
        case 'datetime': return h(ElDatePicker, { modelValue: v, 'onUpdate:modelValue': set, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss', placeholder: f.placeholder || '选择日期时间', style: 'width:100%' })
        default: return h(ElInput, { modelValue: v, 'onUpdate:modelValue': set, placeholder: f.placeholder || '请输入' })
      }
    }
  }
})
export default { name: 'FlowLaunch', components: { 'dyn-field': DynField } }
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

.dyn-section-title { font-size: 14px; font-weight: 600; color: #0f172a; border-left: 3px solid var(--brand-600, #4f46e5); padding-left: 8px; margin: 6px 0 12px; }
.dyn-note { background: #fffbeb; border: 1px dashed #fcd34d; border-radius: 6px; color: #b45309; font-size: 12px; padding: 6px 10px; margin-bottom: 4px; line-height: 1.5; }
.dyn-cell-wrap { border-radius: 4px; padding: 4px 8px; margin-bottom: 4px; }

/* ===== 运行时明细表块（参照泛微 e-cology） ===== */
.dyn-detail-block { border: 1px solid #e2e8f0; border-radius: 6px; overflow: hidden; margin: 4px 0 10px; }
.dyn-detail-bar { display: flex; align-items: center; justify-content: space-between; background: #f8fafc; border-bottom: 1px solid #e2e8f0; padding: 5px 10px; }
.dyn-detail-name { display: inline-flex; align-items: center; gap: 5px; font-size: 13px; font-weight: 600; color: #0f172a; }
.dyn-detail-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.dyn-detail-table th { background: #fef3c7; color: #92400e; padding: 6px 8px; border: 1px solid #fde68a; font-weight: 600; text-align: center; white-space: nowrap; }
.dyn-detail-table td { border: 1px solid #eef2f7; padding: 4px 6px; }
.dyn-detail-sum-row td { background: #f8fafc; }
.dyn-detail-empty { padding: 16px; text-align: center; color: #94a3b8; font-size: 13px; }

.flowable-launch-page .cat-groups {
  display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 14px; padding: 16px 20px 20px;
}
.flowable-launch-page .cat-card {
  border: 1px solid var(--ink-200); border-radius: var(--r-lg); overflow: hidden; background: #fff;
  box-shadow: var(--shadow-card);
}
.flowable-launch-page .cat-top { height: 4px; }
.flowable-launch-page .c-blue { background: linear-gradient(90deg,#3b82f6,#60a5fa); }
.flowable-launch-page .c-green { background: linear-gradient(90deg,#10b981,#34d399); }
.flowable-launch-page .c-amber { background: linear-gradient(90deg,#f59e0b,#fbbf24); }
.flowable-launch-page .c-red { background: linear-gradient(90deg,#ef4444,#f87171); }
.flowable-launch-page .c-violet { background: linear-gradient(90deg,#8b5cf6,#a78bfa); }
.flowable-launch-page .cat-head { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-bottom: 1px solid var(--ink-100); }
.flowable-launch-page .cat-icon {
  width: 34px; height: 34px; border-radius: 8px; background: var(--brand-50); color: var(--brand-600);
  display: grid; place-items: center;
}
.flowable-launch-page .cat-name { font-weight: 600; color: var(--ink-900); flex: 1; }
.flowable-launch-page .cat-count {
  font-size: 12px; font-weight: 600; padding: 1px 8px; border-radius: 999px;
  background: var(--ink-100); color: var(--ink-500);
}
.flowable-launch-page .cat-list { padding: 6px 8px; }
.flowable-launch-page .flow-item {
  display: flex; align-items: center; gap: 8px; padding: 9px 10px; border-radius: var(--r-sm);
  cursor: pointer; transition: background .15s var(--ease-out);
}
.flowable-launch-page .flow-item:hover { background: var(--brand-50); }
.flowable-launch-page .flow-glyph { color: var(--brand-500); flex-shrink: 0; }
.flowable-launch-page .flow-name { flex: 1; font-size: 14px; color: var(--ink-700); }
.flowable-launch-page .flow-ver { font-size: 12px; color: var(--ink-400); }
.flowable-launch-page .flow-go { color: var(--ink-300); }
.flowable-launch-page .flow-item:hover .flow-go { color: var(--brand-600); }
@media (max-width:1200px) { .flowable-launch-page .cat-groups { grid-template-columns: repeat(2,1fr); } }

/* ===== 泛微式全屏发起页：顶部（标题 + 页签 + 操作按钮） + 内容区 ===== */
.launch-header { padding:0 4px; }

/* 第一行：标题左、操作按钮右 */
.launch-titlebar { display:flex; align-items:center; justify-content:space-between; gap:16px; padding:2px 0 10px; }
.launch-titlebar .lt-title { display:flex; align-items:center; gap:10px; min-width:0; }
.launch-titlebar .lt-glyph { width:4px; height:18px; background:var(--brand-600, #4f46e5); border-radius:2px; flex:none; }
.launch-titlebar .lt-name { font-size:16px; font-weight:600; color:var(--ink-900, #0f172a); white-space:nowrap; }
.launch-titlebar .lt-ver {
  font-size:12px; font-weight:600; padding:1px 8px; border-radius:999px;
  background:var(--brand-50, #eef2ff); color:var(--brand-700, #4338ca); flex:none;
}
.launch-titlebar .lt-form {
  display:inline-flex; align-items:center; gap:5px; font-size:13px; color:var(--ink-500, #64748b);
  padding-left:10px; border-left:1px solid var(--ink-200, #e2e8f0); white-space:nowrap;
}
.launch-titlebar .lt-actions { display:flex; align-items:center; gap:8px; flex:none; }

/* 第二行：页签行（下划线风格，与流程详情组件口径一致） */
.launch-tabs { display:flex; align-items:center; gap:4px; padding:0; border-bottom:1px solid var(--ink-200, #e2e8f0); }
.launch-tab {
  position:relative; border:none; background:transparent; cursor:pointer;
  padding:9px 16px; font-size:14px; color:var(--ink-500, #64748b);
  transition:color .15s var(--ease-out, cubic-bezier(.16,.84,.44,1));
}
.launch-tab:hover { color:var(--brand-600, #4f46e5); }
.launch-tab.is-active { color:var(--brand-600, #4f46e5); font-weight:600; }
.launch-tab.is-active::after {
  content:''; position:absolute; left:12px; right:12px; bottom:-1px; height:2px;
  background:var(--brand-600, #4f46e5); border-radius:2px;
}

/* 内容区：卡片式，铺满剩余高度 */
.launch-body { padding:16px 20px 28px; background:#f8fafc; min-height:100%; }
.launch-pane { width:100%; }
.launch-card {
  background:#fff; border:1px solid var(--ink-200, #e2e8f0); border-radius:var(--r-lg, 14px);
  box-shadow:var(--shadow-card, 0 1px 2px rgba(15,23,42,.04)); overflow:hidden; margin-bottom:14px;
}
.launch-card-head {
  display:flex; align-items:center; gap:8px; padding:11px 16px;
  border-bottom:1px solid var(--ink-100, #f1f5f9); font-size:14px; font-weight:600; color:var(--ink-900, #0f172a);
}
.launch-card-head .lc-icon { color:var(--brand-600, #4f46e5); }
.launch-card-body { padding:16px; }

/* 流程图预览 */
.diagram-wrap { display:flex; align-items:center; justify-content:center; min-height:360px; background:#fbfcfe; border:1px dashed var(--ink-200, #e2e8f0); border-radius:8px; padding:12px; overflow:auto; }
.diagram-img { max-width:100%; height:auto; }
.diagram-empty { color:var(--ink-400, #94a3b8); font-size:13px; }

/* 流程状态统计卡 */
.status-cards { display:grid; grid-template-columns:repeat(4,minmax(0,1fr)); gap:12px; margin-bottom:14px; }
.status-card { background:#fff; border:1px solid var(--ink-200, #e2e8f0); border-radius:var(--r-lg, 14px); padding:16px; text-align:center; box-shadow:var(--shadow-card, 0 1px 2px rgba(15,23,42,.04)); }
.status-card .sc-num { font-size:24px; font-weight:700; color:var(--ink-900, #0f172a); line-height:1.2; }
.status-card .sc-num.sc-green { color:#059669; }
.status-card .sc-num.sc-amber { color:#d97706; }
.status-card .sc-label { margin-top:4px; font-size:13px; color:var(--ink-500, #64748b); }

.resource-empty { margin-top:10px; text-align:center; color:var(--ink-400, #94a3b8); font-size:13px; }

@media (max-width:900px) {
  .status-cards { grid-template-columns:repeat(2,minmax(0,1fr)); }
  .launch-titlebar { flex-wrap:wrap; }
}
</style>
