<template>
  <div class="app-container ai-nav-page">
    <!-- ===== 说明卡 ===== -->
    <div class="surface nav-hint">
      <div class="nav-hint-main">
        <span class="nav-hint-icon"><el-icon :size="15"><Guide /></el-icon></span>
        <div>
          <div class="nav-hint-title">问题 → 系统菜单 直达配置</div>
          <div class="nav-hint-desc">
            用户在 AI 问答后可直接点击打开对应功能页面。此处维护「别名」与「示例问句」——它们是命中率的关键，
            建议按客户实际口语补充（如「收料」「机台」「提审批」这类现场说法）。
          </div>
        </div>
      </div>
      <el-button plain icon="Search" @click="showMyMenus">自查我的可推荐菜单</el-button>
    </div>

    <!-- ===== 列表 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" plain icon="Plus" @click="openForm(null)" v-hasPermi="['ai:nav:add']">新增意图</el-button>
        </div>
        <div class="right">
          <el-input v-model="keyword" placeholder="搜索 标识/名称/别名" clearable style="width: 240px"
            @keyup.enter="getList" @clear="getList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button icon="Refresh" @click="getList" style="margin-left:8px">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border class="app-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column label="意图标识" prop="intentKey" min-width="150" show-overflow-tooltip />
        <el-table-column label="展示名称" prop="title" min-width="130" show-overflow-tooltip />
        <el-table-column label="模块" prop="moduleCode" width="90" align="center">
          <template #default="{ row }">
            <span class="badge violet">{{ moduleNames[row.moduleCode] || row.moduleCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="目标菜单" prop="menuId" min-width="110" align="center">
          <template #default="{ row }">
            <span class="mono">#{{ row.menuId }}</span>
            <el-tooltip v-if="resolvedPath(row.menuId)" :content="resolvedPath(row.menuId)" placement="top">
              <el-icon class="link-ico" :size="13"><Link /></el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="别名（命中关键词）" prop="aliases" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="alias-text">{{ row.aliases || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" prop="sortNo" width="80" align="center" />
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="{ row }">
            <span class="badge" :class="row.status === '0' ? 'green' : 'gray'">
              <span class="dot"></span>{{ row.status === '0' ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="openForm(row)" v-hasPermi="['ai:nav:edit']">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['ai:nav:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && !list.length" class="empty-tip">
        暂无意图配置，点击「新增意图」开始；或先执行 sql/ai_nav_intent.sql 导入种子数据。
      </div>
    </div>

    <!-- ===== 编辑弹窗 ===== -->
    <el-dialog v-model="formOpen" :title="form.intentId ? '编辑意图' : '新增意图'" width="640px"
      destroy-on-close append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="意图标识" prop="intentKey">
          <el-input v-model="form.intentKey" placeholder="英文唯一标识，如 wms_inbound" :disabled="!!form.intentId" />
          <div class="form-tip">创建后不可修改；模型只在候选里选它，不会输出路径。</div>
        </el-form-item>
        <el-form-item label="目标菜单" prop="menuId">
          <el-select v-model="form.menuId" style="width:100%" filterable placeholder="选择可跳转的菜单页面" @change="onMenuChange">
            <el-option v-for="o in menuOptions" :key="o.menuId" :label="o.title" :value="o.menuId" />
          </el-select>
          <div v-if="currentResolved" class="form-tip ok">
            <el-icon :size="12"><CircleCheck /></el-icon>
            实际跳转路径：{{ currentResolved }}
          </div>
        </el-form-item>
        <el-form-item label="展示名称" prop="title">
          <el-input v-model="form.title" placeholder="用户在卡片上看到的名称，如 入库单管理" />
        </el-form-item>
        <el-form-item label="模块编码" prop="moduleCode">
          <el-select v-model="form.moduleCode" style="width:100%" placeholder="用于两级路由归类">
            <el-option v-for="(label, key) in moduleNames" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="别名">
          <el-input v-model="form.aliases" type="textarea" :rows="3"
            placeholder="英文逗号分隔，如：入库,收料,收货单,到货登记" />
          <div class="form-tip">客户口语的非标准说法优先填这里，命中率提升主要靠它。</div>
        </el-form-item>
        <el-form-item label="示例问句">
          <el-input v-model="form.examples" type="textarea" :rows="2"
            placeholder="英文逗号分隔，如：入库单在哪里录入,怎么收货入库" />
        </el-form-item>
        <el-form-item label="关联知识文档">
          <el-select v-model="form.docId" style="width:100%" clearable filterable
            placeholder="选填：该文档被问答命中时，确定性返回此入口">
            <el-option v-for="d in docOptions" :key="d.docId" :label="d.docName" :value="d.docId" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.sortNo" :min="0" :max="999" />
          <span class="form-tip" style="margin-left:8px">数值越大越优先展示</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formOpen = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保 存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 自查弹窗：当前用户可被推荐的菜单 ===== -->
    <el-dialog v-model="myMenusOpen" title="我的可推荐菜单" width="720px" append-to-body>
      <div class="form-tip" style="margin-bottom:10px">
        以下为按您的权限实时裁剪出的可跳转菜单（与侧边栏口径一致）。若某意图未出现在此，说明您无权访问其目标菜单。
      </div>
      <el-table :data="myMenuList" border max-height="420" size="small">
        <el-table-column label="菜单ID" prop="menuId" width="100" align="center" />
        <el-table-column label="面包屑" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="实际路径" prop="path" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup name="AiNavConfig">
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import { listNavIntent, addNavIntent, updateNavIntent, delNavIntent, navMenuOptions, myNavMenus } from '@/api/ai/nav'
import { listKnowledge } from '@/api/ai/knowledge'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const keyword = ref('')

const moduleNames = {
  platform: '平台公共', dms: '设备管理', qms: '质量管理', pms: '采购管理', mms: '生产管控',
  wms: '仓储管理', mk: '营销管理', hr: '人力资源', safety: '安全生产',
  bid: '招投标', fin: '财务管理', flow: '流程中心', ai: 'AI中心', system: '系统管理'
}

/* ===== 可选目标菜单（按当前用户权限裁剪）===== */
const menuOptions = ref([])
const resolvedMap = ref({})

function loadMenuOptions() {
  return navMenuOptions().then(res => {
    menuOptions.value = res.data || []
    const m = {}
    menuOptions.value.forEach(o => { m[o.menuId] = o.path })
    resolvedMap.value = m
  }).catch(() => {})
}

function resolvedPath(menuId) {
  return resolvedMap.value[menuId] || ''
}

/* ===== 知识文档下拉（用于文档直连绑定）===== */
const docOptions = ref([])
function loadDocs() {
  listKnowledge({ pageNum: 1, pageSize: 500 }).then(res => {
    docOptions.value = res.rows || []
  }).catch(() => {})
}

/* ===== 列表 ===== */
function getList() {
  loading.value = true
  listNavIntent(keyword.value || undefined).then(res => {
    list.value = res.data || []
    loading.value = false
  }).catch(() => { loading.value = false })
}

/* ===== 表单 ===== */
const formOpen = ref(false)
const formRef = ref()
const form = reactive({
  intentId: null, intentKey: '', menuId: null, moduleCode: '', title: '',
  aliases: '', examples: '', navQuery: '', docId: null, sortNo: 10, status: '0'
})

const rules = {
  intentKey: [
    { required: true, message: '请输入意图标识', trigger: 'blur' },
    { pattern: /^[a-z0-9_]{2,64}$/i, message: '仅支持字母/数字/下划线，2-64 位', trigger: 'blur' }
  ],
  menuId: [{ required: true, message: '请选择目标菜单', trigger: 'change' }],
  title: [{ required: true, message: '请输入展示名称', trigger: 'blur' }],
  moduleCode: [{ required: true, message: '请选择模块编码', trigger: 'change' }]
}

const currentResolved = computed(() => resolvedPath(form.menuId))

function openForm(row) {
  if (!menuOptions.value.length) loadMenuOptions()
  if (!docOptions.value.length) loadDocs()
  Object.assign(form, {
    intentId: row ? row.intentId : null,
    intentKey: row ? row.intentKey : '',
    menuId: row ? row.menuId : null,
    moduleCode: row ? row.moduleCode : '',
    title: row ? row.title : '',
    aliases: row ? (row.aliases || '') : '',
    examples: row ? (row.examples || '') : '',
    navQuery: row ? (row.navQuery || '') : '',
    docId: row ? row.docId || null : null,
    sortNo: row && row.sortNo != null ? row.sortNo : 10,
    status: row ? row.status || '0' : '0'
  })
  formOpen.value = true
  proxy.$nextTick && proxy.$nextTick(() => { formRef.value && formRef.value.clearValidate() })
}

/** 选菜单时自动填充名称与模块，减少手工填错 */
function onMenuChange(menuId) {
  const o = menuOptions.value.find(x => x.menuId === menuId)
  if (!o) return
  if (!form.title) form.title = String(o.title).split(' / ').pop()
  const first = String(o.path || '').replace(/^\//, '').split('/')[0]
  if (first && moduleNames[first] && !form.moduleCode) form.moduleCode = first
}

function submit() {
  formRef.value.validate(valid => {
    if (!valid) return
    saving.value = true
    const payload = { ...form }
    // 空字符串统一转 null，避免后端把 '' 当成有效值
    if (!payload.docId) payload.docId = null
    if (!payload.navQuery) payload.navQuery = ''
    const req = payload.intentId ? updateNavIntent(payload) : addNavIntent(payload)
    req.then(() => {
      proxy.$modal.msgSuccess('保存成功')
      formOpen.value = false
      getList()
    }).catch(e => {
      proxy.$modal.msgError(e && e.message ? e.message : '保存失败')
    }).finally(() => { saving.value = false })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除意图「' + row.title + '」？删除后该问题将不再推荐入口。').then(() => {
    return delNavIntent(row.intentId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

/* ===== 自查 ===== */
const myMenusOpen = ref(false)
const myMenuList = ref([])

function showMyMenus() {
  myNavMenus().then(res => {
    const data = res.data || {}
    myMenuList.value = Object.keys(data).map(k => ({
      menuId: Number(k),
      title: data[k].breadcrumb,
      path: data[k].path
    }))
    myMenusOpen.value = true
  }).catch(() => { proxy.$modal.msgError('查询失败') })
}

loadMenuOptions()
getList()
loadDocs()
</script>

<style scoped>
.ai-nav-page { padding-top: 10px; }

.nav-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
  margin-bottom: 12px;
}

.nav-hint-main { display: flex; align-items: flex-start; gap: 10px; }
.nav-hint-icon { color: #2563EB; margin-top: 2px; }
.nav-hint-title { font-size: 14px; font-weight: 600; color: #111827; margin-bottom: 3px; }
.nav-hint-desc { font-size: 12px; color: #6B7280; line-height: 1.6; }

.toolbar { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; }
.toolbar .right { display: flex; align-items: center; }

.mono { font-family: ui-monospace, Menlo, Consolas, monospace; font-size: 12px; color: #374151; }
.link-ico { color: #2563EB; margin-left: 4px; vertical-align: middle; cursor: help; }
.alias-text { font-size: 12px; color: #4B5563; }
.empty-tip { padding: 32px 0; text-align: center; color: #9CA3AF; font-size: 13px; }

.form-tip { font-size: 12px; color: #9CA3AF; line-height: 1.6; margin-top: 4px; }
.form-tip.ok { color: #059669; display: flex; align-items: center; gap: 4px; }
</style>
