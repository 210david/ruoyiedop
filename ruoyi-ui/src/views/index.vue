<template>
  <div class="app-container home-portal">
    <!-- ========== 1. 欢迎横幅 ========== -->
    <section class="welcome-banner">
      <div class="welcome-left">
        <div class="welcome-avatar">
          <img v-if="userAvatar" :src="userAvatar" alt="avatar" />
          <span v-else>{{ nickNameInitial }}</span>
        </div>
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ nickName || '用户' }}</h1>
          <p>
            <span>{{ today }}</span>
            <span class="dot">·</span>
            <span>{{ weekday }}</span>
            <span v-if="deptName" class="dot">·</span>
            <span v-if="deptName">{{ deptName }}</span>
          </p>
        </div>
      </div>
      <div class="welcome-right">
        <div class="quick-stat" v-for="qs in quickStats" :key="qs.label">
          <span class="quick-stat__value" :style="{ color: qs.color }">{{ qs.value }}</span>
          <span class="quick-stat__label">{{ qs.label }}</span>
        </div>
      </div>
    </section>

    <!-- ========== 2. KPI 指标带 ========== -->
    <section class="kpi-ribbon">
      <div class="kpi-card" v-for="(kpi, idx) in kpiCards" :key="idx" :style="{ '--kpi-accent': kpi.color }">
        <div class="kpi-card__top">
          <div class="kpi-card__info">
            <p class="kpi-card__label">{{ kpi.label }}</p>
            <p class="kpi-card__value">
              {{ kpi.value }}
              <span class="kpi-card__unit" v-if="kpi.unit">{{ kpi.unit }}</span>
            </p>
          </div>
          <div class="kpi-card__icon" :style="{ background: kpi.bgColor }">
            <svg-icon :icon-class="kpi.icon" :style="{ color: kpi.color }" />
          </div>
        </div>
        <div class="kpi-card__bottom" v-if="kpi.sub">
          <span class="kpi-card__trend" :class="kpi.trendClass">
            <svg-icon :icon-class="kpi.trendIcon" v-if="kpi.trendIcon" />
            {{ kpi.sub }}
          </span>
          <span class="kpi-card__sub-text">{{ kpi.subText }}</span>
        </div>
      </div>
    </section>

    <!-- ========== 3. 业务子系统 ========== -->
    <section class="module-section" v-if="systemList.length > 0">
      <div class="section-header">
        <div class="section-header__left">
          <h2 class="section-header__title">业务子系统</h2>
          <span class="section-header__badge">
            {{ systemList.length }} 已启用
          </span>
        </div>
      </div>

      <!-- 已启用模块 -->
      <div class="module-grid module-grid--active">
        <article
          class="module-tile"
          v-for="sys in systemList"
          :key="sys.path"
          :style="{ '--tile-accent': sys.color }"
        >
          <!-- 卡片头部 -->
          <div class="module-tile__header">
            <div class="module-tile__title-wrap">
              <div class="module-tile__icon" :style="{ background: sys.color }">
                <svg-icon :icon-class="sys.icon" />
              </div>
              <div>
                <h3 class="module-tile__name">{{ sys.title }}</h3>
                <span class="module-tile__count">{{ sys.menus.length }} 项功能</span>
              </div>
            </div>
            <span class="module-tile__arrow" @click="goSystemFirst(sys)">
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
            <svg-icon icon-class="list" />
            重要待办事项
          </h3>
          <span class="panel-header__badge" v-if="todoItems.length > 0">{{ todoItems.length }} 项</span>
        </div>
        <div class="panel-body">
          <ul class="todo-list" v-if="todoItems.length > 0">
            <li class="todo-item" v-for="(todo, idx) in todoItems" :key="idx">
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
            <svg-icon icon-class="dashboard" />
            最近动态
          </h3>
        </div>
        <div class="panel-body">
          <ul class="activity-list" v-if="activityItems.length > 0">
            <li class="activity-item" v-for="(act, idx) in activityItems" :key="idx">
              <div class="activity-item__icon" :style="{ background: act.bgColor }">
                <svg-icon :icon-class="act.icon" :style="{ color: act.color }" />
              </div>
              <div class="activity-item__content">
                <p class="activity-item__text">{{ act.text }}</p>
                <p class="activity-item__time">{{ act.time }}</p>
              </div>
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
    />
  </div>
</template>

<script setup name="Index">
import { ArrowRight, Calendar, Right } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import usePermissionStore from '@/store/modules/permission'
import useUserStore from '@/store/modules/user'
import { getNormalPath } from '@/utils/ruoyi'
import { isExternal } from '@/utils/validate'
import { getDashboard } from '@/api/home'

const router = useRouter()
const permissionStore = usePermissionStore()
const userStore = useUserStore()

const loading = ref(true)
const dashData = ref({})

/** 用户信息 */
const nickName = computed(() => userStore.nickName || userStore.name || '')
const userAvatar = computed(() => userStore.avatar || '')
const nickNameInitial = computed(() => {
  const name = nickName.value
  return name ? name.charAt(0).toUpperCase() : 'U'
})
const deptName = computed(() => dashData.value?.welcome?.deptName || '')

/** 日期信息 */
const today = computed(() => dashData.value?.welcome?.today || formatDate(new Date()))
const weekday = computed(() => dashData.value?.welcome?.weekday || getWeekday())

/** 问候语 */
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

/** 系统样式映射 — 对应 digital-ops-home 设计的模块色 */
const systemStyles = {
  '/wms': { color: '#4f46e5', icon: 'shopping' },      // 仓库管理 - 靛蓝
  '/dms': { color: '#0891b2', icon: 'server' },         // 设备管理 - 青色
  '/mk':  { color: '#0ea5e9', icon: 'peoples' },        // 营销管理 - 天蓝
  '/pms': { color: '#2563eb', icon: 'shopping' },       // 采购管理 - 蓝色
}
const defaultStyle = { color: '#64748b', icon: 'system' }

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

/** 系统列表 - 从权限路由动态生成 */
const systemList = computed(() => {
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
      const filteredMenus = allMenus
        .filter(menu => !excluded.includes(menu.title))
        .slice(0, 8)
      return {
        path: route.path,
        title: route.meta.title,
        icon: route.meta.icon || style.icon,
        color: style.color,
        menus: filteredMenus
      }
    })
    .filter(sys => sys.menus.length > 0)
})

/** KPI 指标卡片 — 从后端 dashboard 数据构建 */
const kpiCards = computed(() => {
  const modules = dashData.value?.modules || []
  const alerts = dashData.value?.alerts || {}
  const todos = dashData.value?.todos || {}

  // 从 modules 中提取 WMS 和 DMS 数据
  const wms = modules.find(m => m.code === 'wms') || {}
  const dms = modules.find(m => m.code === 'dms') || {}

  const wmsMetrics = wms.metrics || []
  const dmsMetrics = dms.metrics || {}

  // 构建KPI卡片
  return [
    {
      label: '物料总数',
      value: getMetricValue(wmsMetrics, '物料总数'),
      unit: '种',
      icon: 'shopping',
      color: '#4f46e5',
      bgColor: 'rgba(79, 70, 229, 0.1)',
      sub: `${getMetricValue(wmsMetrics, '已入库')} 单`,
      subText: '本月入库',
      trendClass: 'trend--up',
      trendIcon: '',
    },
    {
      label: '设备稼动率',
      value: getMetricValue(dmsMetrics, '在用率'),
      unit: '%',
      icon: 'server',
      color: '#0891b2',
      bgColor: 'rgba(8, 145, 178, 0.1)',
      sub: `${getMetricValue(dmsMetrics, '设备总数')} 台`,
      subText: '设备总数',
      trendClass: 'trend--up',
      trendIcon: '',
    },
    {
      label: '待处理工单',
      value: getMetricValue(dmsMetrics, '待处理工单'),
      unit: '张',
      icon: 'clipboard',
      color: '#e8900f',
      bgColor: 'rgba(232, 144, 15, 0.1)',
      sub: `${todos.workOrders || 0} 张`,
      subText: '待处理',
      trendClass: 'trend--neutral',
      trendIcon: '',
    },
    {
      label: '预警总数',
      value: alerts.total || 0,
      unit: '项',
      icon: 'warning',
      color: '#e9423a',
      bgColor: 'rgba(233, 66, 58, 0.1)',
      sub: `${(alerts.wms && alerts.wms.total) || 0} 库存 · ${(alerts.dms && alerts.dms.total) || 0} 设备`,
      subText: '需关注',
      trendClass: 'trend--down',
      trendIcon: '',
    },
  ]
})

/** 顶部快捷统计 */
const quickStats = computed(() => {
  const todos = dashData.value?.todos || {}
  const alerts = dashData.value?.alerts || {}
  return [
    { label: '库存预警', value: todos.stockAlerts || 0, color: '#e9423a' },
    { label: '备件预警', value: todos.partAlerts || 0, color: '#e8900f' },
    { label: '待办工单', value: todos.workOrders || 0, color: '#2c8af5' },
  ]
})

/** 待办事项列表 */
const todoItems = computed(() => {
  const items = []
  const todos = dashData.value?.todos || {}
  const alerts = dashData.value?.alerts || {}

  // 待处理工单
  if (todos.workOrders > 0) {
    items.push({
      text: `待处理设备工单 ${todos.workOrders} 张`,
      priority: 'high',
      priorityLabel: '高',
      date: '今天',
      path: '/dms/workorder',
    })
  }

  // 库存预警
  if (todos.stockAlerts > 0) {
    items.push({
      text: `库存预警 ${todos.stockAlerts} 项需处理`,
      priority: 'high',
      priorityLabel: '高',
      date: '今天',
      path: '/wms/stockAlert',
    })
  }

  // 备件预警
  if (todos.partAlerts > 0) {
    items.push({
      text: `备件预警 ${todos.partAlerts} 项低于安全库存`,
      priority: 'medium',
      priorityLabel: '中',
      date: '今天',
      path: '/dms/partalert',
    })
  }

  // DMS 故障设备
  const faultEq = alerts.dms?.faultEquipment || 0
  if (faultEq > 0) {
    items.push({
      text: `故障设备 ${faultEq} 台待维修`,
      priority: 'high',
      priorityLabel: '高',
      date: '今天',
      path: '/dms/equipment',
    })
  }

  return items.slice(0, 6)
})

/** 最近动态列表 */
const activityItems = computed(() => {
  const items = []
  const modules = dashData.value?.modules || []
  const alerts = dashData.value?.alerts || {}
  const todos = dashData.value?.todos || {}

  // 库存预警动态
  if (todos.stockAlerts > 0) {
    items.push({
      text: `库存预警：${todos.stockAlerts} 项物料低于安全库存`,
      time: '刚刚',
      icon: 'warning',
      color: '#e8900f',
      bgColor: 'rgba(232, 144, 15, 0.1)',
    })
  }

  // 备件预警动态
  if (todos.partAlerts > 0) {
    items.push({
      text: `备件预警：${todos.partAlerts} 项备件需要补充`,
      time: '1 小时前',
      icon: 'warning',
      color: '#e9423a',
      bgColor: 'rgba(233, 66, 58, 0.1)',
    })
  }

  // 待处理工单
  if (todos.workOrders > 0) {
    items.push({
      text: `设备工单：${todos.workOrders} 张工单待处理`,
      time: '2 小时前',
      icon: 'clipboard',
      color: '#2c8af5',
      bgColor: 'rgba(44, 138, 245, 0.1)',
    })
  }

  // WMS 入库动态
  const wms = modules.find(m => m.code === 'wms')
  if (wms) {
    const inbound = getMetricValue(wms.metrics, '已入库')
    if (inbound > 0) {
      items.push({
        text: `仓库管理：本月已入库 ${inbound} 单`,
        time: '3 小时前',
        icon: 'shopping',
        color: '#4f46e5',
        bgColor: 'rgba(79, 70, 229, 0.1)',
      })
    }
  }

  return items.slice(0, 5)
})

/** 工具函数：从 metrics 数组中获取值 */
function getMetricValue(metrics, label) {
  if (Array.isArray(metrics)) {
    const m = metrics.find(item => item.label === label)
    return m ? m.value : 0
  }
  return 0
}

/** 工具函数：格式化日期 */
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

/** 工具函数：获取星期 */
function getWeekday() {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[new Date().getDay()]
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

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped lang="scss">
/* ========== Design Tokens ========== */
.home-portal {
  --hp-bg: #f7f8fb;
  --hp-foreground: #0d111f;
  --hp-card: #ffffff;
  --hp-card-fg: #0d111f;
  --hp-muted: #eef0f6;
  --hp-muted-fg: #5c6477;
  --hp-border: #e2e5ef;
  --hp-primary: #409eff;
  --hp-radius-sm: 4px;
  --hp-radius-md: 8px;
  --hp-radius-lg: 16px;
  --hp-shadow-1: 0 1px 2px rgb(15 23 42 / 0.04), 0 1px 1px rgb(15 23 42 / 0.02);
  --hp-shadow-2: 0 8px 24px -8px rgb(15 23 42 / 0.14);
  --hp-shadow-3: 0 24px 60px -20px rgb(15 23 42 / 0.22);
  --hp-state-success: #12a65c;
  --hp-state-success-bg: #e6f7ee;
  --hp-state-warning: #e8900f;
  --hp-state-warning-bg: #fdf3e3;
  --hp-state-error: #e9423a;
  --hp-state-error-bg: #fdecec;
  --hp-state-info: #2c8af5;
  --hp-state-info-bg: #e7f1fe;

  background: var(--hp-bg);
  min-height: calc(100vh - 120px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== 1. 欢迎横幅 ========== */
.welcome-banner {
  background: linear-gradient(135deg, #ffffff 0%, #f0f4ff 100%);
  border: 1px solid var(--hp-border);
  border-radius: var(--hp-radius-lg);
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--hp-shadow-1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -20px;
    right: -20px;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(64, 158, 255, 0.06) 0%, transparent 70%);
    pointer-events: none;
  }
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--hp-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.welcome-text {
  h1 {
    font-size: 18px;
    font-weight: 600;
    color: var(--hp-foreground);
    margin: 0 0 4px 0;
  }

  p {
    font-size: 13px;
    color: var(--hp-muted-fg);
    margin: 0;
    display: flex;
    align-items: center;
    gap: 6px;

    .dot {
      color: var(--hp-border);
    }
  }
}

.welcome-right {
  display: flex;
  gap: 28px;
}

.quick-stat {
  display: flex;
  flex-direction: column;
  align-items: center;

  &__value {
    font-size: 22px;
    font-weight: 700;
    line-height: 1.2;
  }

  &__label {
    font-size: 12px;
    color: var(--hp-muted-fg);
    margin-top: 2px;
  }
}

/* ========== 2. KPI 指标带 ========== */
.kpi-ribbon {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.kpi-card {
  --kpi-accent: var(--hp-primary);
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: var(--hp-radius-lg);
  padding: 16px;
  box-shadow: var(--hp-shadow-1);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: var(--kpi-accent);
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 100% 0%, color-mix(in srgb, var(--kpi-accent) 6%, transparent) 0%, transparent 55%);
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--hp-shadow-2);
  }

  &__top {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    position: relative;
  }

  &__info {
    flex: 1;
  }

  &__label {
    font-size: 13px;
    color: var(--hp-muted-fg);
    margin: 0 0 6px 0;
  }

  &__value {
    font-size: 24px;
    font-weight: 700;
    color: var(--hp-foreground);
    margin: 0;
    line-height: 1.1;
  }

  &__unit {
    font-size: 13px;
    font-weight: 500;
    color: var(--hp-muted-fg);
    margin-left: 2px;
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .svg-icon {
      width: 20px;
      height: 20px;
    }
  }

  &__bottom {
    margin-top: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
  }

  &__trend {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-weight: 600;

    .svg-icon {
      width: 14px;
      height: 14px;
    }

    &.trend--up {
      color: var(--hp-state-success);
    }

    &.trend--down {
      color: var(--hp-state-error);
    }

    &.trend--neutral {
      color: var(--hp-state-info);
    }
  }

  &__sub-text {
    color: var(--hp-muted-fg);
  }
}

/* ========== 3. 业务子系统 ========== */
.module-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--hp-foreground);
    margin: 0;
  }

  &__badge {
    display: inline-flex;
    align-items: center;
    padding: 2px 10px;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 500;
    background: rgba(64, 158, 255, 0.1);
    color: var(--hp-primary);
  }
}

.module-grid {
  display: grid;
  gap: 16px;

  &--active {
    grid-template-columns: repeat(4, 1fr);
  }
}

.module-tile {
  --tile-accent: var(--hp-primary);
  position: relative;
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: var(--hp-radius-md);
  padding: 16px;
  overflow: hidden;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.2s ease, border-color 0.2s ease;
  isolation: isolate;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: var(--tile-accent);
  }

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 100% 0%, color-mix(in srgb, var(--tile-accent) 7%, transparent) 0%, transparent 55%);
    pointer-events: none;
    z-index: -1;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 14px 30px -10px color-mix(in srgb, var(--tile-accent) 22%, rgb(15 23 42 / 0.12));
    border-color: color-mix(in srgb, var(--tile-accent) 32%, var(--hp-border));
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  &__title-wrap {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);

    .svg-icon {
      width: 20px;
      height: 20px;
      color: #fff;
    }
  }

  &__name {
    font-size: 14px;
    font-weight: 600;
    color: var(--hp-foreground);
    margin: 0;
  }

  &__count {
    font-size: 10px;
    color: var(--hp-muted-fg);
    display: block;
    margin-top: 1px;
  }

  &__arrow {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: var(--hp-muted-fg);
    transition: all 0.15s ease;

    &:hover {
      background: color-mix(in srgb, var(--tile-accent) 10%, transparent);
      color: var(--tile-accent);
    }

    .el-icon {
      font-size: 16px;
    }
  }

  &__menu-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 6px;
  }
}

/* ========== 菜单芯片 ========== */
.menu-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 8px;
  border-radius: 6px;
  background: var(--hp-muted);
  color: var(--hp-muted-fg);
  font-size: 11px;
  line-height: 1.35;
  transition: all 0.15s ease;
  border: 1px solid transparent;
  white-space: nowrap;
  cursor: pointer;
  text-decoration: none;

  .svg-icon {
    width: 12px;
    height: 12px;
    flex-shrink: 0;
  }

  &:hover {
    background: color-mix(in srgb, var(--tile-accent) 9%, var(--hp-muted));
    color: var(--tile-accent);
    border-color: color-mix(in srgb, var(--tile-accent) 18%, transparent);
  }
}

/* ========== 4. 待办 + 动态 ========== */
.bottom-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.todo-panel,
.activity-panel {
  background: var(--hp-card);
  border: 1px solid var(--hp-border);
  border-radius: var(--hp-radius-lg);
  box-shadow: var(--hp-shadow-1);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--hp-border);

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--hp-foreground);
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    .svg-icon {
      width: 16px;
      height: 16px;
      color: var(--hp-muted-fg);
    }
  }

  &__badge {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 999px;
    background: var(--hp-state-error-bg);
    color: var(--hp-state-error);
    font-weight: 500;
  }
}

.panel-body {
  padding: 4px 0;
}

/* ========== 待办列表 ========== */
.todo-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--hp-border);
  transition: background 0.15s ease;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--hp-muted);
  }

  &__priority {
    flex-shrink: 0;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 600;
    min-width: 28px;
    text-align: center;

    &.priority--high {
      background: var(--hp-state-error-bg);
      color: var(--hp-state-error);
    }

    &.priority--medium {
      background: var(--hp-state-warning-bg);
      color: var(--hp-state-warning);
    }

    &.priority--low {
      background: var(--hp-state-info-bg);
      color: var(--hp-state-info);
    }
  }

  &__text {
    flex: 1;
    font-size: 13px;
    color: var(--hp-foreground);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;

    &:hover {
      color: var(--hp-primary);
    }
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
  }

  &__date {
    font-size: 12px;
    color: var(--hp-muted-fg);
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      font-size: 14px;
    }
  }

  &__btn {
    width: 26px;
    height: 26px;
    border-radius: 6px;
    border: none;
    background: transparent;
    color: var(--hp-muted-fg);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.15s ease;

    &:hover {
      background: var(--hp-muted);
      color: var(--hp-primary);
    }

    .el-icon {
      font-size: 14px;
    }
  }
}

/* ========== 动态列表 ========== */
.activity-list {
  list-style: none;
  margin: 0;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.activity-item {
  display: flex;
  gap: 12px;

  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .svg-icon {
      width: 16px;
      height: 16px;
    }
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__text {
    font-size: 13px;
    color: var(--hp-foreground);
    margin: 0 0 2px 0;
    line-height: 1.4;
  }

  &__time {
    font-size: 11px;
    color: var(--hp-muted-fg);
    margin: 0;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 1400px) {
  .module-grid--active {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1200px) {
  .kpi-ribbon {
    grid-template-columns: repeat(2, 1fr);
  }

  .module-grid--active {
    grid-template-columns: repeat(2, 1fr);
  }

  .bottom-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-portal {
    padding: 12px;
    gap: 12px;
  }

  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .welcome-right {
    width: 100%;
    justify-content: space-around;
    gap: 12px;
  }

  .kpi-ribbon {
    grid-template-columns: 1fr;
  }

  .module-grid--active {
    grid-template-columns: 1fr;
  }

  .module-tile__menu-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

/* ========== 暗色模式 ========== */
html.dark {
  .home-portal {
    --hp-bg: var(--el-bg-color);
    --hp-foreground: var(--el-text-color-primary);
    --hp-card: var(--el-bg-color-overlay);
    --hp-card-fg: var(--el-text-color-primary);
    --hp-muted: var(--el-fill-color-light);
    --hp-muted-fg: var(--el-text-color-secondary);
    --hp-border: var(--el-border-color);
  }

  .welcome-banner {
    background: linear-gradient(135deg, var(--el-bg-color-overlay) 0%, color-mix(in srgb, var(--el-color-primary) 8%, var(--el-bg-color-overlay)) 100%);
  }

  .welcome-avatar {
    background: var(--el-color-primary);
  }

  .kpi-card,
  .module-tile,
  .todo-panel,
  .activity-panel {
    background: var(--el-bg-color-overlay);
  }

  .menu-chip {
    background: var(--el-fill-color-light);

    &:hover {
      background: color-mix(in srgb, var(--tile-accent) 12%, var(--el-fill-color-light));
    }
  }

  .todo-item {
    &:hover {
      background: var(--el-fill-color-light);
    }
  }

  .todo-item__btn {
    &:hover {
      background: var(--el-fill-color-light);
    }
  }
}
</style>
