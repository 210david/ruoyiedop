<template>
  <div class="app-container home-portal">
    <!-- ========== 1. KPI 指标带（固定 5 张：物料 / 设备 / 销售订单 / 采购订单 / 工单） ========== -->
    <div class="kpi-scope" v-if="kpiRoleName">
      <span class="kpi-scope__dot"></span>
      按「{{ kpiRoleName }}」岗位显示核心指标
    </div>
    <section class="kpi-ribbon" :style="{ '--kpi-cols': Math.max(kpiCards.length, 1) }">
      <div
        class="kpi-card"
        v-for="(kpi, idx) in kpiCards"
        :key="idx"
        :class="{ 'is-clickable': !!kpi.path && !kpi.disabled, 'is-disabled': kpi.disabled }"
        :style="{ '--kpi-accent': kpi.color, '--kpi-bg': kpi.bgColor, 'animation-delay': `${0.15 + idx * 0.08}s` }"
        @click="onKpiClick(kpi)"
      >
        <div class="kpi-card__bars" aria-hidden="true">
          <span v-for="b in 12" :key="b" :style="{ height: barHeight(b, idx) }"></span>
        </div>
        <div class="kpi-card__top">
          <div class="kpi-card__icon">
            <svg-icon :icon-class="kpi.icon" :style="{ color: kpi.color }" />
          </div>
          <p class="kpi-card__label">{{ kpi.label }}</p>
        </div>
        <p class="kpi-card__value">
          {{ kpiDisplay[idx] }}
          <span class="kpi-card__unit" v-if="kpi.unit">{{ kpi.unit }}</span>
        </p>
        <div class="kpi-card__bottom">
          <span class="kpi-card__trend" :class="kpi.trendClass">
            <svg-icon :icon-class="kpi.trendIcon" v-if="kpi.trendIcon" />
            {{ kpi.sub }}
          </span>
          <span class="kpi-card__sub-text">{{ kpi.subText }}</span>
        </div>
      </div>
    </section>

    <!-- ========== 2. 新手上路 + 常用功能 ========== -->
    <OnboardCard
      v-if="!loading"
      :menus="flatMenus"
      :tasks="onboard.tasks"
      :role-name="onboard.roleName"
      :enabled="onboardVisible"
      @close="setOnboardEnabled(false)"
    />
    <ShortcutBar
      v-if="!loading"
      :menus="flatMenus"
      :limit="8"
      :disabled-modules="offModuleList"
      :onboard-enabled="onboard.userEnabled"
      :onboard-global-enabled="onboard.globalEnabled"
      @toggle-onboard="setOnboardEnabled"
    />

    <!-- ========== 3. 待办事项 + 最近动态 ========== -->
    <section class="bottom-section" v-if="allSystemList.length > 0">
      <!-- 待办事项 -->
      <div class="todo-panel">
        <div class="panel-header">
          <h3 class="panel-header__title">
            <span class="panel-header__icon panel-header__icon--todo">
              <svg-icon icon-class="list" />
            </span>
            重要待办事项
          </h3>
          <span class="panel-header__badge" v-if="todoItems.length > 0">{{ todoItems.length }} 项</span>
        </div>
        <div class="panel-body">
          <ul class="todo-list" v-if="todoItems.length > 0">
            <li class="todo-item" v-for="(todo, idx) in todoItems" :key="idx" :class="`todo-item--${todo.priority}`">
              <span class="todo-item__priority" :class="`priority--${todo.priority}`">
                {{ todo.priorityLabel }}
              </span>
              <span class="todo-item__text" @click="goPath(todo.path)">{{ todo.text }}</span>
              <div class="todo-item__right">
                <span class="todo-item__date">
                  <el-icon><Calendar /></el-icon>
                  {{ todo.date }}
                </span>
                <button class="todo-item__btn" @click="goPath(todo.path)" title="前往处理">
                  <el-icon><Right /></el-icon>
                </button>
              </div>
            </li>
          </ul>
          <el-empty v-else description="暂无待办事项" :image-size="60" />
        </div>
      </div>

      <!-- 最近动态 -->
      <div class="activity-panel">
        <div class="panel-header">
          <h3 class="panel-header__title">
            <span class="panel-header__icon panel-header__icon--act">
              <svg-icon icon-class="dashboard" />
            </span>
            最近动态
          </h3>
        </div>
        <div class="panel-body">
          <ul class="activity-list" v-if="activityItems.length > 0">
            <li
              class="activity-item"
              v-for="(act, idx) in activityItems"
              :key="idx"
              :class="{ 'is-clickable': !!act.path }"
              @click="goPath(act.path)"
            >
              <div class="activity-item__icon" :style="{ background: act.bgColor }">
                <svg-icon :icon-class="act.icon" :style="{ color: act.color }" />
              </div>
              <div class="activity-item__content">
                <p class="activity-item__text">{{ act.text }}</p>
                <p class="activity-item__time">{{ act.time }}</p>
              </div>
              <el-icon v-if="act.path" class="activity-item__arrow"><ArrowRight /></el-icon>
            </li>
          </ul>
          <el-empty v-else description="暂无动态" :image-size="60" />
        </div>
      </div>
    </section>

    <!-- ========== 空状态 ========== -->
    <el-empty
      v-if="allSystemList.length === 0 && !loading"
      description="暂无可访问的系统，请联系管理员分配权限"
      :image-size="120"
      style="padding: 80px 0"
    />
  </div>
</template>

<script setup name="Index">
import { ArrowRight, Calendar, Right } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import usePermissionStore from '@/store/modules/permission'
import { getNormalPath } from '@/utils/ruoyi'
import { isExternal } from '@/utils/validate'
import { ElMessage } from 'element-plus'
import { getDashboard, getOnboardTasks, saveOnboardSetting } from '@/api/home'
import { getDisabledModules } from '@/api/system/module'
import OnboardCard from './components/OnboardCard.vue'
import ShortcutBar from './components/ShortcutBar.vue'

const router = useRouter()
const permissionStore = usePermissionStore()

const loading = ref(true)
const dashData = ref({})

/**
 * 后台「模块开关」中已停用的模块标识。
 * null 表示未取到（接口异常），此时不做过滤，避免把正常模块误隐藏。
 */
const disabledModuleKeys = ref(null)

/** 命中的岗位名称（后端按 sys_role_kpi 匹配，未配置时为空） */
const kpiRoleName = computed(() => dashData.value?.kpiRoleName || '')

/** 系统样式映射 — 各业务子系统主题色 */
const systemStyles = {
  '/wms':    { color: '#4f46e5', icon: 'shopping' },   // 仓库管理 - 靛蓝
  '/dms':    { color: '#0891b2', icon: 'server' },     // 设备管理 - 青色
  '/pms':    { color: '#2563eb', icon: 'money' },      // 采购管理 - 蓝色
  '/mk':     { color: '#0ea5e9', icon: 'peoples' },    // 营销管理 - 天蓝
  '/mms':    { color: '#722ed1', icon: 'build' },     // 生产管控 - 紫色
  '/qms':    { color: '#e8900f', icon: 'shield' },    // 质量管理 - 琥珀
  '/safety': { color: '#e9423a', icon: 'warning' },   // 安全生产 - 红色
  '/hr':     { color: '#eb2f96', icon: 'education' }, // 人力资源 - 玫红
}
const defaultStyle = { color: '#64748b', icon: 'system' }

/** 各模块 KPI 卡片取数配置：主指标 / 副指标 / 点击跳转的菜单标题（用于精确匹配） / 兜底路径 */
const kpiConfig = {
  wms:    { metric: '物料总数',   subMetric: '已入库',   targetTitles: ['物料管理', '库存查询'],         fallback: '/wms/base/material' },
  dms:    { metric: '设备总数',   subMetric: '在用率',   targetTitles: ['设备台账', '设备管理'],           fallback: '/dms/base/equipment' },
  pms:    { metric: '待审批申请', subMetric: '待审批订单', targetTitles: ['采购申请'],                     fallback: '/pms/demand/request' },
  mk:     { metric: '客户总数',   subMetric: '跟进中线索', targetTitles: ['客户管理', '客户列表'],           fallback: '/mk/customer/list' },
  mms:    { metric: '执行中工单', subMetric: '已完工工单', targetTitles: ['工单管理', '生产工单'],           fallback: '/mms/workorder-group/workorder' },
  qms:    { metric: '未闭环NCR',  subMetric: '待验证NCR', targetTitles: ['NCR台账'],                       fallback: '/qms/ncr/ncrlist' },
  safety: { metric: '未闭环隐患', subMetric: '超期未整改', targetTitles: ['隐患管理', '隐患排查'],          fallback: '/safety/dualcontrol/hazard' },
  hr:     { metric: '在职员工',   subMetric: '试用期员工', targetTitles: ['员工档案', '员工管理'],           fallback: '/hr/org/employee' },
}

/** 路由路径 → 模块编码映射（与后端 modules.code 对应） */
const pathToCode = {
  '/wms': 'wms',
  '/dms': 'dms',
  '/pms': 'pms',
  '/mk': 'mk',
  '/mms': 'mms',
  '/qms': 'qms',
  '/safety': 'safety',
  '/hr': 'hr',
}

/** 每个系统要排除的不常用功能菜单 */
const excludedMenus = {
  '/wms': ['编号规则', '库存流水'],
  '/dms': ['编号规则', '库存流水报表', 'AI配置'],
  '/pms': ['编号规则', '企业查询配置'],
  '/mk': ['编号规则', '销售人员分析'],
}

/** 路径拼接 */
function resolvePath(parentPath, childPath) {
  if (isExternal(childPath)) return childPath
  if (isExternal(parentPath)) return parentPath
  if (childPath.startsWith('/')) return childPath
  return getNormalPath(parentPath + '/' + childPath)
}

/** 递归收集叶子菜单 */
function collectLeafMenus(parentPath, route) {
  const fullPath = resolvePath(parentPath, route.path)
  const visibleChildren = (route.children || []).filter(c => !c.hidden)

  if (visibleChildren.length === 0) {
    return [{
      path: fullPath,
      title: route.meta?.title || '',
      icon: route.meta?.icon || ''
    }]
  }

  return visibleChildren.flatMap(child => collectLeafMenus(fullPath, child))
}

/** 全部系统列表 - 从权限路由动态生成 */
const allSystemList = computed(() => {
  const routes = permissionStore.sidebarRouters || []
  return routes
    .filter(route => {
      if (route.hidden) return false
      if (route.path === '' || route.path === '/index' || route.path === '/user') return false
      if (route.path === '/system' || route.path === '/monitor' || route.path === '/tool') return false
      // 业务全景图本身是导航页，不参与"业务子系统"统计，避免出现在自己的模块列表里
      if (route.path === '/business-map') return false
      // 后台「模块开关」已关闭的模块，其功能不进入工作台与「自定义常用功能」弹窗
      const moduleCode = route.path.replace(/^\//, '')
      if (disabledModuleKeys.value && disabledModuleKeys.value.has(moduleCode)) return false
      if (!route.meta || !route.meta.title) return false
      const visibleChildren = (route.children || []).filter(c => !c.hidden)
      return visibleChildren.length > 0
    })
    .map(route => {
      const style = systemStyles[route.path] || defaultStyle
      const visibleChildren = (route.children || []).filter(c => !c.hidden)
      const allMenus = visibleChildren.flatMap(child => collectLeafMenus(route.path, child))
      const excluded = excludedMenus[route.path] || []
      const fullMenus = allMenus.filter(menu => !excluded.includes(menu.title))
      return {
        path: route.path,
        title: route.meta.title,
        icon: route.meta.icon || style.icon,
        color: style.color,
        menus: fullMenus.slice(0, 8),
        fullMenus
      }
    })
    .filter(sys => sys.menus.length > 0)
})

/** 全量叶子菜单扁平化 — 供「常用功能」与「新手上路」使用 */
const flatMenus = computed(() => {
  const arr = []
  allSystemList.value.forEach(sys => {
    const moduleCode = sys.path.replace('/', '')
    sys.fullMenus.forEach(menu => {
      if (!menu.path || !menu.title) return
      arr.push({
        path: menu.path,
        title: menu.title,
        icon: menu.icon || '',
        module: moduleCode,
        moduleTitle: sys.title
      })
    })
  })
  return arr
})

/** 菜单标题 → 路径 映射（用于待办事项跳转，基于完整菜单列表） */
const menuPathByTitle = computed(() => {
  const map = {}
  allSystemList.value.forEach(sys => {
    sys.fullMenus.forEach(menu => {
      if (menu.title && !map[menu.title]) map[menu.title] = menu.path
    })
  })
  return map
})

/** 按菜单标题查找跳转路径，找不到则使用兜底路径 */
function findMenuPath(titles, fallback) {
  for (const t of titles) {
    if (menuPathByTitle.value[t]) return menuPathByTitle.value[t]
  }
  return fallback
}

/** KPI 指标卡片 — 顶部固定 5 张（一屏一行），超出截断 */
const KPI_LIMIT = 5

/**
 * 菜单路径首段 → 模块标识。
 * 绝大多数模块两者一致（/wms/xxx → wms），个别例外单独登记：
 * 消息中心菜单路径是 /message，但 sys_module.module_key 为 msg。
 */
const MODULE_PATH_ALIAS = { message: 'msg' }

function moduleCodeOfPath(path) {
  const seg = String(path || '').split('?')[0].split('/').filter(Boolean)[0]
  if (!seg) return ''
  return MODULE_PATH_ALIAS[seg] || seg
}

/** 菜单路径所属模块是否被后台「模块开关」关闭 */
function pathModuleOff(path) {
  const set = disabledModuleKeys.value
  if (!set || set.size === 0 || !path) return false
  const seg = moduleCodeOfPath(path)
  return !!seg && set.has(seg)
}

/** 已停用模块标识列表（传给常用功能栏，过滤掉指向已关模块的固定项） */
const offModuleList = computed(() => (disabledModuleKeys.value ? Array.from(disabledModuleKeys.value) : []))

/**
 * 给卡片补 disabled 标记。
 * 顶部 5 张卡不随模块开关消失（少一张会留白很难看），但模块关闭后目标页面不可达，
 * 因此保留卡片、仅取消跳转，点击时提示去模块开关开启。
 */
function withModuleState(cards) {
  return cards.map(c => ({ ...c, disabled: pathModuleOff(c.path) }))
}

const kpiCards = computed(() => {
  const modules = dashData.value?.modules || []
  const alerts = dashData.value?.alerts || {}

  const cards = []

  // 后端下发的核心指标卡（当前统一为 sys_role_kpi 的 __default__ 五张固定卡；
  // 岗位差异化配置已下线，后续改由「门户卡片自定义维护」在后台统一配置）
  // 接口返回空数组时走下方原「按模块」逻辑兜底，保证界面不空白
  const roleKpis = dashData.value?.coreKpis || []
  if (roleKpis.length > 0) {
    roleKpis.forEach(k => {
      const titles = String(k.targetTitles || '').split(',').map(s => s.trim()).filter(Boolean)
      const color = k.color || '#4f46e5'
      cards.push({
        label: k.label,
        value: k.value,
        unit: k.unit || '',
        icon: k.icon || 'chart',
        color,
        bgColor: hexToRgba(color, 0.1),
        sub: k.sub || '',
        subText: k.subText || '',
        trendClass: 'trend--neutral',
        trendIcon: '',
        path: findMenuPath(titles, k.targetPath || ''),
      })
    })
    return withModuleState(cards).slice(0, KPI_LIMIT)
  }

  modules.forEach(mod => {
    if (mod.status !== 'active') return
    const conf = kpiConfig[mod.code]
    if (!conf || !Array.isArray(mod.metrics) || mod.metrics.length === 0) return
    const mainMetric = mod.metrics.find(m => m.label === conf.metric) || mod.metrics[0]
    const subMetric = mod.metrics.find(m => m.label === conf.subMetric)
    const sysStyle = Object.entries(systemStyles).find(([p]) => pathToCode[p] === mod.code)
    // 点击卡片跳转到与该 KPI 指标最相关的业务页面（通过菜单标题精确匹配）
    const kpiPath = conf.targetTitles ? findMenuPath(conf.targetTitles, conf.fallback || '') : ''
    cards.push({
      label: mainMetric.label,
      value: mainMetric.value,
      unit: mainMetric.unit || '',
      icon: sysStyle ? sysStyle[1].icon : 'chart',
      color: mod.color,
      bgColor: hexToRgba(mod.color, 0.1),
      sub: subMetric ? `${subMetric.value}${subMetric.unit || ''}` : `${mod.name}`,
      subText: subMetric ? subMetric.label : '业务模块',
      trendClass: 'trend--neutral',
      trendIcon: '',
      path: kpiPath,
    })
  })

  // 预警总数卡（始终显示，跳转库存预警页）
  cards.push({
    label: '预警总数',
    value: alerts.total || 0,
    unit: '项',
    icon: 'bug',
    color: '#e9423a',
    bgColor: 'rgba(233, 66, 58, 0.1)',
    sub: `${(alerts.wms && alerts.wms.total) || 0} 库存 · ${(alerts.dms && alerts.dms.total) || 0} 设备`,
    subText: `${(alerts.qms && alerts.qms.total) || 0} 质量 · ${(alerts.safety && alerts.safety.total) || 0} 安全`,
    trendClass: 'trend--down',
    trendIcon: '',
    path: findMenuPath(['库存预警'], '/wms/inv/alert'),
  })

  return withModuleState(cards).slice(0, KPI_LIMIT)
})

/** KPI 卡片点击：模块已关闭时只提示不跳转（避免跳到 404 页面） */
function onKpiClick(kpi) {
  if (kpi.disabled) {
    ElMessage.info(`「${kpi.label}」所属模块已关闭，可在「系统管理 → 模块开关」中重新开启`)
    return
  }
  goPath(kpi.path)
}

/** 十六进制色值转 rgba */
function hexToRgba(hex, alpha) {
  if (!hex || hex[0] !== '#') return `rgba(100, 116, 139, ${alpha})`
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

/** KPI 数字滚动动画 */
const kpiDisplay = ref([])

function tweenKpi(index, target, decimals) {
  const startVal = parseFloat(kpiDisplay.value[index]) || 0
  if (startVal === target) {
    kpiDisplay.value[index] = target.toFixed(decimals)
    return
  }
  const dur = 1100
  const t0 = performance.now()
  function frame(t) {
    const p = Math.min((t - t0) / dur, 1)
    const eased = 1 - Math.pow(1 - p, 3) // easeOutCubic
    const v = startVal + (target - startVal) * eased
    kpiDisplay.value[index] = v.toFixed(decimals)
    if (p < 1) requestAnimationFrame(frame)
  }
  requestAnimationFrame(frame)
}

watch(kpiCards, (cards) => {
  // 卡片数量变化时重置显示数组
  if (kpiDisplay.value.length !== cards.length) {
    kpiDisplay.value = cards.map(() => '0')
  }
  cards.forEach((c, i) => {
    const target = parseFloat(c.value) || 0
    const decimals = String(c.value).includes('.') ? 1 : 0
    tweenKpi(i, target, decimals)
  })
}, { immediate: true })

/** 模块是否启用：后台「模块开关」关闭后，其待办与动态一律不展示（接口异常时按启用处理） */
function isModuleOn(code) {
  if (!code) return true
  return !(disabledModuleKeys.value && disabledModuleKeys.value.has(code))
}

/** 待办事项列表 — 覆盖全模块 */
const todoItems = computed(() => {
  const todos = dashData.value?.todos || {}
  const alerts = dashData.value?.alerts || {}

  const items = []

  /** 待办定义：模块路径 / 取数字段 / 文案 / 优先级 / 菜单标题匹配 / 兜底路径 */
  const definitions = [
    // 流程中心待办置顶：有则排第一，无则不占位（流转到我这里的审批最需要先处理）
    { module: '/flow',  count: todos.flowTodo,           text: c => `流程待办 ${c} 条待我审批`,         priority: 'high',   titles: ['待办流程'], fallback: '/flow/todo' },
    { module: '/safety', count: todos.safetyHazards,    text: c => `安全隐患 ${c} 项待整改闭环`,       priority: 'high',   titles: ['隐患管理', '隐患排查', '隐患排查治理'], fallback: '/safety/dualcontrol/hazard' },
    { module: '/qms',    count: todos.qmsNcrs,           text: c => `质量NCR ${c} 项未闭环`,            priority: 'high',   titles: ['NCR台账'], fallback: '/qms/ncr/ncrlist' },
    { module: '/dms',    count: todos.workOrders,         text: c => `待处理设备工单 ${c} 张`,           priority: 'high',   titles: ['工单列表'], fallback: '/dms/workorder/list' },
    { module: '/wms',   count: todos.stockAlerts,        text: c => `库存预警 ${c} 项需处理`,           priority: 'high',   titles: ['库存预警'], fallback: '/wms/inv/alert' },
    { module: '/pms',   count: todos.purchaseRequests,   text: c => `采购申请 ${c} 单待审批`,            priority: 'medium', titles: ['采购申请'], fallback: '/pms/demand/request' },
    { module: '/pms',   count: todos.purchaseOrders,     text: c => `采购订单 ${c} 单待审批`,            priority: 'medium', titles: ['采购订单'], fallback: '/pms/contract/order' },
    { module: '/dms',   count: todos.partAlerts,         text: c => `备件预警 ${c} 项低于安全库存`,       priority: 'medium', titles: ['备件预警'], fallback: '/dms/sparepart/partalert' },
    { module: '/dms',   count: alerts.dms?.faultEquipment, text: c => `故障设备 ${c} 台待维修`,         priority: 'high',   titles: ['设备台账', '设备管理'], fallback: '/dms/base/equipment' },
    { module: '/mms',   count: todos.mmsWorkOrders,      text: c => `生产工单 ${c} 张执行中`,            priority: 'low',    titles: ['工单管理', '生产工单'], fallback: '/mms/workorder-group/workorder' },
    { module: '/mk',    count: todos.mkLeads,            text: c => `营销线索 ${c} 条待跟进`,            priority: 'low',    titles: ['线索列表'], fallback: '/mk/lead/list' },
  ]

  definitions.forEach(def => {
    if (!def.count || def.count <= 0) return
    // 模块已关闭：不展示该模块的待办
    if (!isModuleOn(def.module.replace(/^\//, ''))) return
    items.push({
      text: def.text(def.count),
      priority: def.priority,
      priorityLabel: def.priority === 'high' ? '高' : def.priority === 'medium' ? '中' : '低',
      date: '今天',
      path: findMenuPath(def.titles, def.fallback),
    })
  })

  return items.slice(0, 8)
})

/** 最近动态列表 — 各启用模块的关键业务概览（点击可跳转对应模块） */
const activityItems = computed(() => {
  const items = []
  const modules = dashData.value?.modules || []
  const todos = dashData.value?.todos || {}

  // 置顶预警类动态（模块关闭后不再推送该类提醒）
  if (todos.safetyHazards > 0 && isModuleOn('safety')) {
    items.push({ text: `安全生产：${todos.safetyHazards} 项隐患治理进行中`, time: '实时', icon: 'warning', color: '#e9423a', bgColor: 'rgba(233, 66, 58, 0.1)', path: findMenuPath(['隐患管理', '隐患排查'], '/safety/dualcontrol/hazard') })
  }
  if (todos.qmsNcrs > 0 && isModuleOn('qms')) {
    items.push({ text: `质量管理：${todos.qmsNcrs} 项NCR处置流程中`, time: '实时', icon: 'shield', color: '#e8900f', bgColor: 'rgba(232, 144, 15, 0.1)', path: findMenuPath(['NCR台账'], '/qms/ncr/ncrlist') })
  }
  if (todos.stockAlerts > 0 && isModuleOn('wms')) {
    items.push({ text: `仓库管理：${todos.stockAlerts} 项物料库存预警`, time: '实时', icon: 'shopping', color: '#4f46e5', bgColor: 'rgba(79, 70, 229, 0.1)', path: findMenuPath(['库存预警'], '/wms/inv/alert') })
  }
  if (todos.workOrders > 0 && isModuleOn('dms')) {
    items.push({ text: `设备管理：${todos.workOrders} 张工单待处理`, time: '实时', icon: 'clipboard', color: '#2c8af5', bgColor: 'rgba(44, 138, 245, 0.1)', path: findMenuPath(['工单列表'], '/dms/workorder/list') })
  }

  // 各模块业务概览动态（仅展示有数据的模块，全 0 模块不推送动态）
  modules.forEach(mod => {
    if (items.length >= 8) return
    if (!isModuleOn(mod.code)) return
    if (mod.status !== 'active' || !Array.isArray(mod.metrics) || mod.metrics.length === 0) return
    const hasData = mod.metrics.some(m => parseFloat(m.value) > 0)
    if (!hasData) return
    const summary = mod.metrics.slice(0, 2)
      .map(m => `${m.label} ${m.value}${m.unit || ''}`)
      .join('，')
    // 跳转到与该模块指标最相关的业务页面
    const conf = kpiConfig[mod.code]
    const modPath = conf ? findMenuPath(conf.targetTitles, conf.fallback || '') : ''
    items.push({
      text: `${mod.name}：${summary}`,
      time: '实时',
      icon: mod.icon,
      color: mod.color,
      bgColor: hexToRgba(mod.color, 0.1),
      path: modPath,
    })
  })

  return items.slice(0, 8)
})

/** KPI 卡片装饰条高度（伪迷你柱状图） */
const barSeeds = [
  [38, 55, 42, 66, 50, 78, 60, 88, 70, 95, 82, 100],
  [60, 45, 70, 52, 80, 58, 90, 66, 76, 88, 72, 96],
  [50, 68, 42, 76, 55, 84, 62, 92, 58, 80, 90, 70],
  [70, 52, 82, 60, 74, 48, 88, 64, 78, 56, 92, 68],
]
function barHeight(barIndex, cardIndex) {
  const seeds = barSeeds[cardIndex % barSeeds.length]
  return `${seeds[(barIndex - 1) % seeds.length]}%`
}

/** 跳转到指定路径 */
function goPath(path) {
  if (!path) return
  if (isExternal(path)) {
    window.open(path, '_blank')
  } else {
    router.push(path)
  }
}

/** 加载首页数据 */
function loadDashboard() {
  loading.value = true
  getDashboard().then(res => {
    dashData.value = res.data || {}
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 新手上路：任务列表 + 双层开关状态 */
const onboard = ref({
  tasks: [],
  roleName: '',
  globalEnabled: true,
  userEnabled: true
})

/** 系统级与用户级开关同时开启时才展示 */
const onboardVisible = computed(() => onboard.value.globalEnabled && onboard.value.userEnabled)

function loadOnboard() {
  getOnboardTasks().then(res => {
    const data = res.data || {}
    onboard.value = {
      tasks: Array.isArray(data.tasks) ? data.tasks : [],
      roleName: data.roleName || '',
      globalEnabled: data.globalEnabled !== false,
      userEnabled: data.userEnabled !== false
    }
  }).catch(() => {
    // 接口异常时静默降级，不打扰用户
  })
}

/** 保存用户级开关（关闭后可在「常用功能 → 自定义」里重新开启） */
function setOnboardEnabled(enabled) {
  const next = !!enabled
  saveOnboardSetting(next).then(() => {
    onboard.value = { ...onboard.value, userEnabled: next }
    if (next) {
      ElMessage.success('已开启新手上路')
    } else {
      ElMessage.success('已关闭新手上路，可在「常用功能 → 自定义」里重新开启')
    }
  }).catch(() => {
    ElMessage.error('操作失败，请稍后重试')
  })
}

/** 加载后台已停用模块（工作台按模块开关过滤功能入口） */
function loadDisabledModules() {
  getDisabledModules().then(res => {
    disabledModuleKeys.value = Array.isArray(res.data) ? new Set(res.data) : null
  }).catch(() => {
    // 接口异常时不阻塞页面，按不过滤处理
    disabledModuleKeys.value = null
  })
}

onMounted(() => {
  loadDashboard()
  loadOnboard()
  loadDisabledModules()
})

// 从菜单页返回时（keep-alive 缓存），刷新任务完成状态：刚走完的步骤应立刻打勾
onActivated(() => {
  loadOnboard()
})
</script>

<style scoped lang="scss">
/* ========== Design Tokens ========== */
.home-portal {
  --hp-bg: #f6f7fb;
  --hp-card: #ffffff;
  --hp-foreground: #0d111f;
  --hp-muted-fg: #5c6477;
  --hp-border: #e4e7f1;
  --hp-primary: #4f46e5;
  --hp-radius-lg: 20px;
  --hp-shadow-1: 0 1px 2px rgb(15 23 42 / 0.04), 0 1px 1px rgb(15 23 42 / 0.02);
  --hp-shadow-2: 0 10px 30px -10px rgb(15 23 42 / 0.14);
  --hp-shadow-3: 0 24px 60px -20px rgb(15 23 42 / 0.22);

  background: var(--hp-bg);
  min-height: calc(100vh - 120px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== 入场动画 ========== */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes barGrow {
  from { transform: scaleY(0); }
  to { transform: scaleY(1); }
}

/* ========== 1. KPI 指标带 ========== */
.kpi-scope {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 12px;
  color: var(--hp-muted-fg);

  &__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--hp-primary);
    flex-shrink: 0;
  }
}

/* 固定 5 列：一屏一行铺满，卡片数超出的截断（见 kpiCards 里的 KPI_LIMIT） */
.kpi-ribbon {
  display: grid;
  /* 列数随卡片数变化：岗位只配了 3-4 张卡时铺满一行，不留空位（默认为 5 张固定卡） */
  grid-template-columns: repeat(var(--kpi-cols, 5), minmax(0, 1fr));
  gap: 14px;
}

.kpi-card {
  position: relative;
  overflow: hidden;
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: 16px;
  padding: 18px;
  box-shadow: var(--hp-shadow-1);
  animation: fadeUp 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s, border-color 0.3s;

  &.is-clickable {
    cursor: pointer;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--hp-shadow-2);
    border-color: color-mix(in srgb, var(--kpi-accent) 38%, var(--hp-border));
  }

  /* 所属模块已关闭：卡片保留占位，但不做可点击反馈 */
  &.is-disabled {
    cursor: default;

    &:hover {
      transform: none;
      box-shadow: var(--hp-shadow-1);
      border-color: var(--hp-border);
    }
  }

  /* 顶部渐变描边 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, var(--kpi-accent), transparent 75%);
    opacity: 0.85;
  }
}

/* 迷你柱状图装饰 */
.kpi-card__bars {
  position: absolute;
  right: 12px;
  bottom: 38px;
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 46px;
  opacity: 0.45;
  pointer-events: none;

  span {
    width: 5px;
    border-radius: 3px 3px 0 0;
    background: linear-gradient(to top, color-mix(in srgb, var(--kpi-accent) 18%, transparent), color-mix(in srgb, var(--kpi-accent) 62%, transparent));
    transform-origin: bottom;
    animation: barGrow 0.8s cubic-bezier(0.22, 1, 0.36, 1) both;
  }

  @for $i from 1 through 12 {
    span:nth-child(#{$i}) { animation-delay: #{0.3 + $i * 0.04}s; }
  }
}

@keyframes barGrow {
  from { transform: scaleY(0); }
  to { transform: scaleY(1); }
}

.kpi-card__top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.kpi-card__icon {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: var(--kpi-bg);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--kpi-accent) 14%, transparent);
}

.kpi-card__label {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--hp-muted-fg);
  letter-spacing: 0.02em;
}

.kpi-card__value {
  margin: 0 0 12px;
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
  color: var(--hp-foreground);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;

  .kpi-card__unit {
    font-size: 12px;
    font-weight: 600;
    color: var(--hp-muted-fg);
    margin-left: 3px;
  }
}

.kpi-card__bottom {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  position: relative;
  z-index: 1;
}

.kpi-card__trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 999px;
  white-space: nowrap;

  &.trend--up { color: #12a65c; background: #e6f7ee; }
  &.trend--down { color: #e9423a; background: #fdecec; }
  &.trend--neutral { color: #2c8af5; background: #e7f1fe; }
}

.kpi-card__sub-text {
  font-size: 12px;
  color: var(--hp-muted-fg);
}

/* ========== 4. 待办 + 动态 ========== */
.bottom-section {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
  animation: fadeUp 0.6s 0.35s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.todo-panel,
.activity-panel {
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: 16px;
  box-shadow: var(--hp-shadow-1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--hp-border);

  &__title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14.5px;
    font-weight: 700;
    color: var(--hp-foreground);
    margin: 0;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border-radius: 9px;
    font-size: 15px;

    &--todo { color: #e8900f; background: #fdf3e3; }
    &--act { color: #0891b2; background: #e0f5f9; }
  }

  &__badge {
    font-size: 12px;
    font-weight: 700;
    color: #e9423a;
    background: #fdecec;
    padding: 2px 10px;
    border-radius: 999px;
  }
}

.panel-body {
  padding: 14px 20px 18px;
}

/* 待办列表 */
.todo-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px 11px 16px;
  border-radius: 11px;
  background: #f8f9fc;
  border: 1px solid transparent;
  transition: all 0.25s;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 18%;
    bottom: 18%;
    width: 3px;
    border-radius: 2px;
    background: #d3d8e6;
    transition: background 0.25s;
  }

  &--high::before { background: #e9423a; }
  &--medium::before { background: #e8900f; }

  &:hover {
    background: #fff;
    border-color: var(--hp-border);
    box-shadow: var(--hp-shadow-1);
    transform: translateX(3px);
  }

  .todo-item__priority {
    flex-shrink: 0;
    font-size: 11px;
    font-weight: 700;
    padding: 2px 8px;
    border-radius: 6px;

    &.priority--high { color: #e9423a; background: #fdecec; }
    &.priority--medium { color: #e8900f; background: #fdf3e3; }
    &.priority--low { color: #2c8af5; background: #e7f1fe; }
  }

  .todo-item__text {
    flex: 1;
    min-width: 0;
    font-size: 13.5px;
    color: #2b3245;
    cursor: pointer;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &:hover { color: var(--hp-primary); }
  }

  .todo-item__right {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
  }

  .todo-item__date {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: var(--hp-muted-fg);
  }

  .todo-item__btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border-radius: 50%;
    font-size: 12px;
    color: var(--hp-muted-fg);
    background: #eef0f6;
    border: none;
    cursor: pointer;
    transition: all 0.25s;

    &:hover {
      background: var(--hp-primary);
      color: #fff;
      transform: translateX(2px);
    }
  }
}

/* 动态列表 */
.activity-list {
  list-style: none;
  margin: 0;
  padding: 2px 0 0 6px;
  display: flex;
  flex-direction: column;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 21px;
    top: 22px;
    bottom: 22px;
    width: 1px;
    background: linear-gradient(180deg, var(--hp-border), transparent);
  }
}

.activity-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 6px;
  border-radius: 10px;
  transition: background 0.22s;

  &.is-clickable {
    cursor: pointer;

    &:hover {
      background: #f4f6fb;

      .activity-item__text { color: var(--hp-primary); }
      .activity-item__arrow { opacity: 1; transform: translateX(0); }
    }
  }

  &__icon {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border-radius: 10px;
    font-size: 15px;
    flex-shrink: 0;
  }

  &__content { min-width: 0; flex: 1; }

  &__text {
    margin: 0 0 3px;
    font-size: 13px;
    color: #2b3245;
    line-height: 1.45;
  }

  &__time {
    margin: 0;
    font-size: 12px;
    color: var(--hp-muted-fg);
  }

  &__arrow {
    align-self: center;
    font-size: 13px;
    color: var(--hp-primary);
    opacity: 0;
    transform: translateX(-6px);
    transition: all 0.25s;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 1366px) {
  .kpi-ribbon { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 1200px) {
  .bottom-section { grid-template-columns: 1fr; }
}

@media (max-width: 900px) {
  .kpi-ribbon { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 640px) {
  .kpi-ribbon { grid-template-columns: 1fr; }
}
</style>

<style lang="scss">
/* 自定义模块弹窗（append-to-body，需全局样式） */
.home-layout-dialog {
  .layout-tip {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 10px 14px;
    margin-bottom: 16px;
    border-radius: 10px;
    font-size: 13px;
    line-height: 1.6;
    color: #4f46e5;
    background: rgba(79, 70, 229, 0.07);
    border: 1px solid rgba(79, 70, 229, 0.18);

    .el-icon {
      flex-shrink: 0;
      margin-top: 3px;
      font-size: 15px;
    }
  }

  .layout-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .layout-item {
    display: flex;
    align-items: center;
    height: auto !important;
    margin-right: 0 !important;
    padding: 12px 14px;
    border: 1px solid #e4e7f1;
    border-radius: 12px;
    background: #f8f9fc;
    transition: all 0.22s;

    &:hover {
      border-color: #c7cdf5;
      background: #f3f4fe;
    }

    .el-checkbox__label {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-size: 13.5px;
      color: #2b3245;
    }

    .layout-item__dot {
      width: 9px;
      height: 9px;
      border-radius: 3px;
      flex-shrink: 0;
    }

    .layout-item__count {
      font-size: 11.5px;
      color: #8a92a6;
    }
  }

  .layout-actions {
    display: flex;
    justify-content: flex-end;
    gap: 4px;
    margin-top: 12px;
  }
}
</style>
