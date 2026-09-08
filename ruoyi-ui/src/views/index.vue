<template>
  <div class="app-container home-portal">
    <!-- ========== 1. KPI 指标带 ========== -->
    <section class="kpi-ribbon">
      <div
        class="kpi-card"
        v-for="(kpi, idx) in kpiCards"
        :key="idx"
        :class="{ 'is-clickable': !!kpi.path }"
        :style="{ '--kpi-accent': kpi.color, '--kpi-bg': kpi.bgColor, 'animation-delay': `${0.15 + idx * 0.08}s` }"
        @click="goPath(kpi.path)"
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

    <!-- ========== 3. 业务子系统 ========== -->
    <section class="module-section" v-if="allSystemList.length > 0">
      <div class="section-header">
        <div class="section-header__left">
          <span class="section-header__bar"></span>
          <h2 class="section-header__title">业务子系统</h2>
          <span class="section-header__badge">{{ systemList.length }} / {{ allSystemList.length }} 已启用</span>
        </div>
        <div class="section-header__right">
          <el-button size="small" text bg icon="Setting" @click="openLayoutDialog">自定义模块</el-button>
        </div>
      </div>

      <!-- 已启用模块 -->
      <div class="module-grid module-grid--active">
        <article
          class="module-tile"
          v-for="(sys, sIdx) in systemList"
          :key="sys.path"
          :style="{ '--tile-accent': sys.color, 'animation-delay': `${0.1 + sIdx * 0.08}s` }"
        >
          <span class="module-tile__sheen"></span>

          <!-- 卡片头部 -->
          <div class="module-tile__header">
            <div class="module-tile__title-wrap">
              <div class="module-tile__icon">
                <svg-icon :icon-class="sys.icon" />
              </div>
              <div>
                <h3 class="module-tile__name">{{ sys.title }}</h3>
                <span class="module-tile__count">{{ sys.menus.length }} 项功能</span>
              </div>
            </div>
            <span class="module-tile__arrow" @click="goSystemFirst(sys)" title="进入系统">
              <el-icon><ArrowRight /></el-icon>
            </span>
          </div>

          <!-- 功能入口网格 -->
          <div class="module-tile__menu-grid">
            <a
              v-for="menu in sys.menus"
              :key="menu.path"
              class="menu-chip"
              @click="goPath(menu.path)"
            >
              <svg-icon :icon-class="menu.icon" v-if="menu.icon" />
              <span>{{ menu.title }}</span>
            </a>
          </div>
        </article>
      </div>
    </section>

    <!-- ========== 4. 待办事项 + 最近动态 ========== -->
    <section class="bottom-section" v-if="systemList.length > 0">
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
      v-if="systemList.length === 0 && !loading"
      description="暂无可访问的系统，请联系管理员分配权限"
      :image-size="120"
      style="padding: 80px 0"
    >
      <el-button v-if="allSystemList.length > 0" type="primary" icon="Setting" @click="openLayoutDialog">自定义模块</el-button>
    </el-empty>

    <!-- ========== 自定义模块弹窗 ========== -->
    <el-dialog v-model="layoutDialog.visible" title="自定义首页模块" width="580px" append-to-body class="home-layout-dialog">
      <div class="layout-tip">
        <el-icon><InfoFilled /></el-icon>
        勾选需要在首页展示的业务模块，保存后立即生效。不同岗位/企业可按实际使用的模块组合自己的工作台。
      </div>
      <el-checkbox-group v-model="layoutDialog.checked">
        <div class="layout-grid">
          <el-checkbox v-for="sys in allSystemList" :key="sys.path" :label="sys.path" class="layout-item">
            <span class="layout-item__dot" :style="{ background: sys.color }"></span>
            <span class="layout-item__name">{{ sys.title }}</span>
            <span class="layout-item__count">{{ sys.menus.length }} 项功能</span>
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <div class="layout-actions">
        <el-button link type="primary" @click="checkAllModules">全选</el-button>
        <el-button link @click="layoutDialog.checked = []">清空</el-button>
      </div>
      <template #footer>
        <el-button @click="layoutDialog.visible = false">取 消</el-button>
        <el-button type="primary" :loading="layoutDialog.saving" @click="saveLayoutConfig">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import { ArrowRight, Calendar, Right, Setting, InfoFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import usePermissionStore from '@/store/modules/permission'
import { getNormalPath } from '@/utils/ruoyi'
import { isExternal } from '@/utils/validate'
import { getDashboard, getHomeLayout, saveHomeLayout } from '@/api/home'

const { proxy } = getCurrentInstance()
const router = useRouter()
const permissionStore = usePermissionStore()

const loading = ref(true)
const dashData = ref({})

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

/** 用户自定义模块布局（逗号分隔的模块路径；空 = 显示全部） */
const layoutCodes = ref('')

/** 当前生效的模块集合（null = 不限制） */
const enabledKeys = computed(() => {
  if (!layoutCodes.value) return null
  const set = new Set(layoutCodes.value.split(',').map(s => s.trim()).filter(Boolean))
  return set.size > 0 ? set : null
})

/** 按用户配置过滤后的系统列表 */
const systemList = computed(() => {
  const all = allSystemList.value
  const set = enabledKeys.value
  if (!set) return all
  return all.filter(sys => set.has(sys.path))
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

/** KPI 指标卡片 — 按启用模块动态生成（每模块主指标一张卡 + 预警总数） */
const kpiCards = computed(() => {
  const modules = dashData.value?.modules || []
  const alerts = dashData.value?.alerts || {}
  const set = enabledKeys.value

  const cards = []
  // 后端模块顺序即为卡片顺序
  modules.forEach(mod => {
    if (set && !set.has('/' + mod.code)) return
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

  return cards
})

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

/** 待办事项列表 — 覆盖全模块，并按用户启用的模块过滤 */
const todoItems = computed(() => {
  const todos = dashData.value?.todos || {}
  const alerts = dashData.value?.alerts || {}
  const set = enabledKeys.value

  const items = []

  /** 待办定义：模块路径 / 取数字段 / 文案 / 优先级 / 菜单标题匹配 / 兜底路径 */
  const definitions = [
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
    if (set && !set.has(def.module)) return
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
  const set = enabledKeys.value
  const timeLabels = ['刚刚', '1 小时前', '2 小时前', '3 小时前', '今天上午', '昨天']

  // 置顶预警类动态
  if (todos.safetyHazards > 0 && (!set || set.has('/safety'))) {
    items.push({ text: `安全生产：${todos.safetyHazards} 项隐患治理进行中`, time: '刚刚', icon: 'warning', color: '#e9423a', bgColor: 'rgba(233, 66, 58, 0.1)', path: findMenuPath(['隐患管理', '隐患排查'], '/safety/dualcontrol/hazard') })
  }
  if (todos.qmsNcrs > 0 && (!set || set.has('/qms'))) {
    items.push({ text: `质量管理：${todos.qmsNcrs} 项NCR处置流程中`, time: '刚刚', icon: 'shield', color: '#e8900f', bgColor: 'rgba(232, 144, 15, 0.1)', path: findMenuPath(['NCR台账'], '/qms/ncr/ncrlist') })
  }
  if (todos.stockAlerts > 0 && (!set || set.has('/wms'))) {
    items.push({ text: `仓库管理：${todos.stockAlerts} 项物料库存预警`, time: '1 小时前', icon: 'shopping', color: '#4f46e5', bgColor: 'rgba(79, 70, 229, 0.1)', path: findMenuPath(['库存预警'], '/wms/inv/alert') })
  }
  if (todos.workOrders > 0 && (!set || set.has('/dms'))) {
    items.push({ text: `设备管理：${todos.workOrders} 张工单待处理`, time: '2 小时前', icon: 'clipboard', color: '#2c8af5', bgColor: 'rgba(44, 138, 245, 0.1)', path: findMenuPath(['工单列表'], '/dms/workorder/list') })
  }

  // 各模块业务概览动态
  let idx = 0
  modules.forEach(mod => {
    if (items.length >= 8) return
    if (set && !set.has('/' + mod.code)) return
    if (mod.status !== 'active' || !Array.isArray(mod.metrics) || mod.metrics.length === 0) return
    const summary = mod.metrics.slice(0, 2)
      .map(m => `${m.label} ${m.value}${m.unit || ''}`)
      .join('，')
    // 跳转到与该模块指标最相关的业务页面
    const conf = kpiConfig[mod.code]
    const modPath = conf ? findMenuPath(conf.targetTitles, conf.fallback || '') : ''
    items.push({
      text: `${mod.name}：${summary}`,
      time: timeLabels[idx % timeLabels.length],
      icon: mod.icon,
      color: mod.color,
      bgColor: hexToRgba(mod.color, 0.1),
      path: modPath,
    })
    idx++
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

/** 点击系统箭头 - 跳转到该系统的第一个菜单 */
function goSystemFirst(sys) {
  if (sys.menus.length > 0) {
    goPath(sys.menus[0].path)
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

/** 加载用户模块布局配置 */
function loadLayout() {
  getHomeLayout().then(res => {
    layoutCodes.value = (res.data || res.msg || '') + ''
  }).catch(() => {})
}

/** 自定义模块弹窗 */
const layoutDialog = reactive({
  visible: false,
  checked: [],
  saving: false,
})

function openLayoutDialog() {
  // 以当前生效模块为初始勾选
  layoutDialog.checked = systemList.value.map(sys => sys.path)
  layoutDialog.visible = true
}

function checkAllModules() {
  layoutDialog.checked = allSystemList.value.map(sys => sys.path)
}

function saveLayoutConfig() {
  if (layoutDialog.checked.length === 0) {
    proxy.$modal.msgWarning('请至少选择一个模块，或选择后保存')
    return
  }
  layoutDialog.saving = true
  const codes = layoutDialog.checked.join(',')
  saveHomeLayout(codes).then(() => {
    layoutCodes.value = codes
    layoutDialog.visible = false
    proxy.$modal.msgSuccess('首页模块配置已保存')
  }).finally(() => {
    layoutDialog.saving = false
  })
}

onMounted(() => {
  loadDashboard()
  loadLayout()
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
.kpi-ribbon {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(225px, 1fr));
  gap: 16px;
}

.kpi-card {
  position: relative;
  overflow: hidden;
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: 16px;
  padding: 20px 22px;
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
  right: 16px;
  bottom: 40px;
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 52px;
  opacity: 0.55;
  pointer-events: none;

  span {
    width: 6px;
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
  width: 42px;
  height: 42px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
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
  margin: 0 0 14px;
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
  color: var(--hp-foreground);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;

  .kpi-card__unit {
    font-size: 13px;
    font-weight: 600;
    color: var(--hp-muted-fg);
    margin-left: 4px;
  }
}

.kpi-card__bottom {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.kpi-card__trend {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 999px;

  &.trend--up { color: #12a65c; background: #e6f7ee; }
  &.trend--down { color: #e9423a; background: #fdecec; }
  &.trend--neutral { color: #2c8af5; background: #e7f1fe; }
}

.kpi-card__sub-text {
  font-size: 12px;
  color: var(--hp-muted-fg);
}

/* ========== 3. 业务子系统 ========== */
.module-section {
  animation: fadeUp 0.6s 0.25s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;

  &__left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__bar {
    width: 4px;
    height: 18px;
    border-radius: 2px;
    background: linear-gradient(180deg, var(--hp-primary), #38bdf8);
  }

  &__title {
    font-size: 17px;
    font-weight: 700;
    color: var(--hp-foreground);
    margin: 0;
  }

  &__badge {
    font-size: 12px;
    font-weight: 600;
    color: var(--hp-primary);
    background: color-mix(in srgb, var(--hp-primary) 10%, #fff);
    border: 1px solid color-mix(in srgb, var(--hp-primary) 22%, transparent);
    padding: 2px 10px;
    border-radius: 999px;
  }
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.module-tile {
  position: relative;
  overflow: hidden;
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--hp-shadow-1);
  animation: fadeUp 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s, border-color 0.3s;

  &:hover {
    transform: translateY(-5px);
    border-color: color-mix(in srgb, var(--tile-accent) 40%, var(--hp-border));
    box-shadow:
      var(--hp-shadow-2),
      0 14px 34px -14px color-mix(in srgb, var(--tile-accent) 45%, transparent);

    .module-tile__sheen { transform: translateX(220%) skewX(-18deg); }
    .module-tile__arrow { background: var(--tile-accent); color: #fff; border-color: var(--tile-accent); }
  }
}

/* 悬停扫光 */
.module-tile__sheen {
  position: absolute;
  top: 0;
  bottom: 0;
  left: -60%;
  width: 45%;
  background: linear-gradient(100deg, transparent, rgb(255 255 255 / 0.5), transparent);
  transform: translateX(-120%) skewX(-18deg);
  transition: transform 0.7s ease;
  pointer-events: none;
}

.module-tile__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.module-tile__title-wrap {
  display: flex;
  align-items: center;
  gap: 13px;
}

.module-tile__icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  background: linear-gradient(135deg, color-mix(in srgb, var(--tile-accent) 82%, #fff 0%), color-mix(in srgb, var(--tile-accent) 58%, #38bdf8));
  box-shadow: 0 8px 18px -6px color-mix(in srgb, var(--tile-accent) 55%, transparent);
}

.module-tile__name {
  font-size: 15.5px;
  font-weight: 700;
  color: var(--hp-foreground);
  margin: 0 0 3px;
}

.module-tile__count {
  font-size: 12px;
  color: var(--hp-muted-fg);
}

.module-tile__arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-size: 14px;
  color: var(--hp-muted-fg);
  background: var(--hp-bg);
  border: 1px solid var(--hp-border);
  cursor: pointer;
  transition: all 0.3s;

  &:hover { transform: translateX(2px); }
}

.module-tile__menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.menu-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 6px;
  border-radius: 9px;
  font-size: 12.5px;
  color: #3f4658;
  background: #f6f7fb;
  border: 1px solid transparent;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: all 0.22s;

  .svg-icon { font-size: 14px; color: var(--hp-muted-fg); flex-shrink: 0; }

  &:hover {
    color: var(--tile-accent);
    background: color-mix(in srgb, var(--tile-accent) 8%, #fff);
    border-color: color-mix(in srgb, var(--tile-accent) 25%, transparent);
    transform: translateY(-1px);

    .svg-icon { color: var(--tile-accent); }
  }
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
@media (max-width: 1400px) {
  .kpi-ribbon { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 1200px) {
  .bottom-section { grid-template-columns: 1fr; }
}

@media (max-width: 900px) {
  .module-grid { grid-template-columns: 1fr; }
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
