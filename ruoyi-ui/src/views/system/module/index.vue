<template>
  <div class="app-container module-switch-page">
    <!-- ===== 统计概览 ===== -->
    <div class="surface stat-card">
      <div class="stat-row">
        <div class="stat-item">
          <div class="stat-icon is-green"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-meta"><div class="stat-value">{{ enabledCount }}</div><div class="stat-label">启用模块</div></div>
        </div>
        <div class="stat-item">
          <div class="stat-icon is-gray"><el-icon><RemoveFilled /></el-icon></div>
          <div class="stat-meta"><div class="stat-value">{{ disabledCount }}</div><div class="stat-label">停用模块</div></div>
        </div>
        <div class="stat-item">
          <div class="stat-icon is-brand"><el-icon><User /></el-icon></div>
          <div class="stat-meta"><div class="stat-value">{{ totalRoleCount }}</div><div class="stat-label">关联角色</div></div>
        </div>
        <div class="stat-item">
          <div class="stat-icon is-amber"><el-icon><Timer /></el-icon></div>
          <div class="stat-meta"><div class="stat-value">{{ totalJobCount }}</div><div class="stat-label">关联任务</div></div>
        </div>
      </div>
      <el-alert type="warning" :closable="false" show-icon class="switch-alert">
        <template #title>
          切换模块状态会级联停用/恢复该模块的菜单、角色与定时任务，并强制所有在线用户重新登录以刷新权限，请谨慎操作。
        </template>
      </el-alert>
    </div>

    <!-- ===== 模块卡片列表 ===== -->
    <div class="surface">
      <div class="toolbar">
        <div class="left">
          <span class="toolbar-title"><el-icon><Switch /></el-icon> 模块开关管理</span>
        </div>
        <div class="right">
          <el-input v-model="queryParams.moduleName" placeholder="搜索模块名称" clearable style="width: 200px" @keyup.enter="getList" @clear="getList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px" @change="getList">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
          <el-button icon="Refresh" circle @click="getList" />
          <el-button type="primary" plain icon="RefreshLeft" @click="handleEnableAll" v-hasPermi="['system:module:edit']">一键恢复全部</el-button>
        </div>
      </div>

      <div class="module-grid" v-loading="loading">
        <div v-for="item in moduleList" :key="item.moduleId" class="module-card" :class="item.status === '0' ? 'is-enabled' : 'is-disabled'">
          <div class="module-card-head">
            <div class="module-name">
              <span class="module-dot" :class="item.status === '0' ? 'dot-on' : 'dot-off'"></span>
              {{ item.moduleName }}
              <el-tag v-if="item.isCore === '1'" type="warning" effect="plain" size="small">核心</el-tag>
            </div>
            <el-switch
              v-model="item.status"
              active-value="0"
              inactive-value="1"
              :disabled="item.isCore === '1'"
              @change="handleStatusChange(item)"
              v-hasPermi="['system:module:edit']"
            />
          </div>
          <div class="module-card-body">
            <div class="module-key-row">
              <span class="module-key">{{ item.moduleKey }}</span>
              <span class="module-status" :class="item.status === '0' ? 'st-on' : 'st-off'">{{ item.status === '0' ? '运行中' : '已停用' }}</span>
            </div>
            <div class="module-stats">
              <span class="m-stat"><el-icon><User /></el-icon> {{ item.roleCount || 0 }} 角色</span>
              <span class="m-stat"><el-icon><Timer /></el-icon> {{ item.jobCount || 0 }} 任务</span>
            </div>
            <div class="module-remark" v-if="item.remark" :title="item.remark">{{ item.remark }}</div>
          </div>
        </div>
        <div v-if="!loading && moduleList.length === 0" class="empty-tip">
          <el-empty description="暂无模块数据" />
        </div>
      </div>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>
  </div>
</template>

<script setup name="Module">
import { listModule, changeModuleStatus, enableAllModule } from "@/api/system/module"
import { CircleCheck, RemoveFilled, User, Timer, Search, Switch, RefreshLeft } from "@element-plus/icons-vue"

const { proxy } = getCurrentInstance()

const loading = ref(true)
const moduleList = ref([])
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 50,
    moduleName: undefined,
    status: undefined
  }
})
const { queryParams } = toRefs(data)

const enabledCount = computed(() => moduleList.value.filter(m => m.status === '0').length)
const disabledCount = computed(() => moduleList.value.filter(m => m.status === '1').length)
const totalRoleCount = computed(() => moduleList.value.reduce((s, m) => s + (Number(m.roleCount) || 0), 0))
const totalJobCount = computed(() => moduleList.value.reduce((s, m) => s + (Number(m.jobCount) || 0), 0))

/** 查询模块列表 */
function getList() {
  loading.value = true
  listModule(queryParams.value).then(response => {
    moduleList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 切换模块状态 */
function handleStatusChange(item) {
  const action = item.status === '0' ? '启用' : '停用'
  const tip = item.status === '0'
    ? `确认要启用模块【${item.moduleName}】吗？将恢复该模块的菜单、角色与定时任务，并强制所有在线用户重新登录。`
    : `确认要停用模块【${item.moduleName}】吗？将停用该模块的菜单、角色与定时任务（共 ${item.roleCount || 0} 个角色、${item.jobCount || 0} 个任务），并强制所有在线用户重新登录。`
  proxy.$modal.confirm(tip).then(() => {
    return changeModuleStatus({ moduleId: item.moduleId, status: item.status })
  }).then(() => {
    proxy.$modal.msgSuccess(`${action}成功`)
    getList()
  }).catch(() => {
    item.status = item.status === '0' ? '1' : '0'
  })
}

/** 一键恢复全部已停用模块 */
function handleEnableAll() {
  proxy.$modal.confirm(`确认要恢复全部已停用模块吗？将逐个启用停用中的模块并强制所有在线用户重新登录。`).then(() => {
    return enableAllModule()
  }).then(() => {
    proxy.$modal.msgSuccess('恢复成功')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* ===== Design Tokens ===== */
.module-switch-page {
  padding-top: 10px;
  --brand-50:#eef2ff; --brand-100:#e0e7ff; --brand-200:#c7d2fe; --brand-500:#6366f1; --brand-600:#4f46e5; --brand-700:#4338ca;
  --ink-900:#0f172a; --ink-700:#334155; --ink-500:#64748b; --ink-400:#94a3b8; --ink-300:#cbd5e1; --ink-200:#e2e8f0; --ink-100:#f1f5f9; --ink-50:#f8fafc;
  --amber-50:#fffbeb; --amber-500:#f59e0b; --amber-700:#b45309;
  --green-50:#ecfdf5; --green-500:#10b981; --green-700:#047857;
  --red-50:#fef2f2; --red-500:#ef4444; --red-700:#b91c1c;
  --r-sm:6px; --r-md:10px; --r-lg:14px;
  --shadow-card:0 1px 0 rgba(15,23,42,.04), 0 1px 2px rgba(15,23,42,.04);
  --ease-out:cubic-bezier(.16,.84,.44,1);
  font-feature-settings:"tnum" 1;
  color: var(--ink-900);
}

/* ===== Surface ===== */
.module-switch-page .surface { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-lg); box-shadow:var(--shadow-card); overflow:hidden; margin-bottom:8px; }

/* ===== 统计概览 ===== */
.module-switch-page .stat-card { padding:16px 20px; }
.module-switch-page .stat-row { display:grid; grid-template-columns:repeat(4, minmax(0,1fr)); gap:12px; }
.module-switch-page .stat-item { display:flex; align-items:center; gap:12px; padding:12px 14px; background:var(--ink-50); border:1px solid var(--ink-200); border-radius:var(--r-md); }
.module-switch-page .stat-icon { display:flex; align-items:center; justify-content:center; width:40px; height:40px; border-radius:10px; font-size:20px; }
.module-switch-page .stat-icon.is-green { background:var(--green-50); color:var(--green-500); }
.module-switch-page .stat-icon.is-gray { background:var(--ink-100); color:var(--ink-500); }
.module-switch-page .stat-icon.is-brand { background:var(--brand-50); color:var(--brand-600); }
.module-switch-page .stat-icon.is-amber { background:var(--amber-50); color:var(--amber-500); }
.module-switch-page .stat-value { font-size:22px; font-weight:700; line-height:1.1; color:var(--ink-900); }
.module-switch-page .stat-label { font-size:13px; color:var(--ink-500); margin-top:2px; }
.module-switch-page .switch-alert { margin-top:14px; --el-alert-bg-color:var(--amber-50); }

/* ===== Toolbar ===== */
.module-switch-page .toolbar { display:flex; align-items:center; justify-content:space-between; padding:12px 20px; border-bottom:1px solid var(--ink-200); background:var(--ink-50); }
.module-switch-page .toolbar .left { display:flex; gap:8px; align-items:center; }
.module-switch-page .toolbar .right { display:flex; gap:8px; align-items:center; }
.module-switch-page .toolbar-title { display:flex; align-items:center; gap:6px; font-size:14px; font-weight:600; color:var(--ink-700); }

/* ===== 模块卡片栅格 ===== */
.module-switch-page .module-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(280px, 1fr)); gap:14px; padding:16px 20px; background:var(--ink-50); }
.module-switch-page .module-card { background:#fff; border:1px solid var(--ink-200); border-radius:var(--r-md); padding:14px 16px; transition:all .2s var(--ease-out); }
.module-switch-page .module-card:hover { box-shadow:0 6px 16px -6px rgba(15,23,42,.12); transform:translateY(-2px); }
.module-switch-page .module-card.is-enabled { border-color:#a7f3d0; }
.module-switch-page .module-card.is-disabled { opacity:.82; background:var(--ink-50); }
.module-switch-page .module-card-head { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.module-switch-page .module-name { display:flex; align-items:center; gap:8px; font-size:15px; font-weight:600; color:var(--ink-900); }
.module-switch-page .module-dot { width:8px; height:8px; border-radius:50%; flex-shrink:0; }
.module-switch-page .dot-on { background:var(--green-500); box-shadow:0 0 0 3px var(--green-50); }
.module-switch-page .dot-off { background:var(--ink-400); box-shadow:0 0 0 3px var(--ink-100); }
.module-switch-page .module-card-body { margin-top:10px; display:flex; flex-direction:column; gap:8px; }
.module-switch-page .module-key-row { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.module-switch-page .module-key { font-family:ui-monospace, monospace; font-size:12px; color:var(--brand-700); background:var(--brand-50); border:1px solid var(--brand-200); border-radius:4px; padding:1px 8px; }
.module-switch-page .module-status { font-size:12px; font-weight:600; }
.module-switch-page .st-on { color:var(--green-700); }
.module-switch-page .st-off { color:var(--ink-500); }
.module-switch-page .module-stats { display:flex; gap:14px; }
.module-switch-page .m-stat { display:inline-flex; align-items:center; gap:4px; font-size:13px; color:var(--ink-500); }
.module-switch-page .m-stat .el-icon { font-size:14px; color:var(--ink-400); }
.module-switch-page .module-remark { font-size:12px; color:var(--ink-400); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.module-switch-page .empty-tip { grid-column:1 / -1; }

/* ===== Responsive ===== */
@media (max-width:900px) { .module-switch-page .stat-row { grid-template-columns:repeat(2,1fr); } }
@media (max-width:560px) { .module-switch-page .stat-row { grid-template-columns:1fr; } }
</style>
