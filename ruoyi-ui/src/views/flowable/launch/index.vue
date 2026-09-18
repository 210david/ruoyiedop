<template>
  <div class="app-container flowable-launch-page">
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

    <!-- ===== 发起弹窗 ===== -->
    <el-dialog v-model="open" :title="'发起：' + (current.modelName || '')" width="680px" append-to-body draggable class="rd-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="rd-detail-header">
          <div class="rd-detail-header-icon"><el-icon :size="20"><Promotion /></el-icon></div>
          <span class="rd-detail-header-title">发起流程</span>
          <div class="rd-detail-header-sub" v-if="current.modelName">
            <div class="rd-detail-header-divider"></div>
            <span class="rd-detail-header-no">{{ current.modelName }}（v{{ current.version }}）</span>
          </div>
        </div>
      </template>

      <div v-if="boundForm && boundForm.fields && boundForm.fields.length">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
          :title="'使用表单：' + boundForm.formName" description="填写后提交，数据将存入业务数据表并进入审批" />
        <el-form ref="dynRef" :model="dynData" label-width="120px">
          <el-form-item v-for="f in boundForm.fields" :key="f.field" :label="f.label" :prop="f.field"
            :rules="f.required ? [{ required: true, message: '请填写' + f.label, trigger: ['blur','change'] }] : []">
            <el-select v-if="f.rtype==='select'" v-model="dynData[f.field]" style="width:100%">
              <el-option v-for="o in (f.optionList||[])" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-radio-group v-else-if="f.rtype==='radio'" v-model="dynData[f.field]">
              <el-radio v-for="o in (f.optionList||[])" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
            </el-radio-group>
            <el-checkbox v-else-if="f.rtype==='check'" v-model="dynData[f.field]" true-value="1" false-value="0">是</el-checkbox>
            <el-switch v-else-if="f.rtype==='switch'" v-model="dynData[f.field]" active-value="1" inactive-value="0" />
            <el-rate v-else-if="f.rtype==='rate'" v-model="dynData[f.field]" />
            <div v-else-if="f.rtype==='browse'" style="display:flex;gap:8px;width:100%">
              <el-input v-model="dynData[f.field]" readonly :placeholder="'点击右侧按钮选择' + (browseTypeLabel(f) || '数据')" />
              <el-button @click="openBrowse(f)">浏览…</el-button>
            </div>
            <file-upload v-else-if="f.rtype==='attach'" v-model="dynData[f.field]" :limit="f.attachLimit || 5" :file-type="attachAccept(f)" :file-size="50" />
            <el-input v-else-if="f.rtype==='text'" v-model="dynData[f.field]" />
            <el-input v-else-if="f.rtype==='textarea'" v-model="dynData[f.field]" type="textarea" :rows="f.rows || 4" />
            <el-input-number v-else-if="f.rtype==='number'" v-model="dynData[f.field]" :controls="false" style="width:100%" />
            <el-date-picker v-else-if="f.rtype==='date'" v-model="dynData[f.field]" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
            <el-date-picker v-else-if="f.rtype==='datetime'" v-model="dynData[f.field]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间" style="width:100%" />
            <el-input v-else v-model="dynData[f.field]" />
          </el-form-item>
        </el-form>
      </div>
      <div v-else>
        <el-alert type="info" :closable="false" show-icon style="margin-bottom:14px"
          title="该流程未绑定表单" description="可直接发起（流程变量在流程设计器中已定义），或先到后台表单管理设计表单并绑定" />
      </div>

      <el-form label-width="120px" style="margin-top:10px">
        <el-form-item label="流程标题">
          <el-input v-model="title" placeholder="默认自动生成：流程名-申请人-日期" />
        </el-form-item>
        <el-form-item label="首节点审批人">
          <el-select v-model="approverUserId" placeholder="选择审批人（将进入其待办）" filterable style="width:100%">
            <el-option v-for="u in userOptions" :key="u.userId" :label="u.nickName + '（' + u.userName + '）'" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" :loading="submitting" @click="doLaunch">提交并发起</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
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

const { proxy } = getCurrentInstance()
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
    .then(res => { userOptions.value = res.rows || [] }).catch(() => {})
}

/** 字段归一化：新版 {form,dtype} 与旧版 {type} 统一为渲染类型 rtype；select 兼容键值对/字符串/数组 */
function normalizeField(f) {
  const g = { ...f }
  let t = f.type
  if (f.form) {
    if (['check', 'browse', 'attach', 'select', 'textarea', 'radio', 'switch', 'rate'].includes(f.form)) t = f.form
    else if (f.form === 'date' || f.form === 'datetime') t = f.form
    else if (f.form === 'number') t = 'number'
    else t = f.dtype === 'datetime' ? 'datetime' : f.dtype === 'date' ? 'date' : (f.dtype === 'float' || f.dtype === 'int') ? 'number' : 'text'
  } else if (!t) t = 'text'
  g.rtype = t
  if (t === 'select' || t === 'radio') {
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
  current.value = d
  approverUserId.value = undefined
  dynData.value = {}
  title.value = (d.modelName || '') + '-' + (userStore.nickName || '') + '-' + new Date().toISOString().slice(0, 10)
  boundForm.value = null
  try {
    const res = await formByProcess(d.modelKey)
    if (res.data && res.data.fieldsJson) {
      let fields = []
      try { fields = (JSON.parse(res.data.fieldsJson) || []).map(normalizeField) } catch (e) {}
      boundForm.value = { ...res.data, fields }
    }
  } catch (e) { /* 无绑定表单 */ }
  loadUsers()
  open.value = true
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
  launch({
    definitionKey: current.value.modelKey,
    formId: boundForm.value ? boundForm.value.formId : null,
    approverUserId: approverUserId.value,
    title: title.value,
    variables: dynData.value
  }).then(() => {
    proxy.$modal.msgSuccess('流程已发起，等待审批人处理')
    open.value = false
  }).finally(() => { submitting.value = false })
}

getList()
</script>

<style lang="scss" scoped>
@use '../model/flow-model.scss';

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
</style>
