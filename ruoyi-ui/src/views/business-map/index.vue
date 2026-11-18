<template>
  <div class="bmap" :data-theme="theme">
    <!-- 背景层：光斑 + 网格 -->
    <div class="bmap__bg" aria-hidden="true">
      <span class="bmap__blob bmap__blob--1"></span>
      <span class="bmap__blob bmap__blob--2"></span>
      <span class="bmap__blob bmap__blob--3"></span>
      <span class="bmap__grid"></span>
    </div>

    <!-- 顶部 -->
    <header class="bmap__head">
      <div class="brand">
        <span class="brand__mark"><svg-icon icon-class="guide" /></span>
        <div class="brand__text">
          <h1>业务全景图 <em>Business Landscape</em></h1>
          <p>一条主线串起「接单 → 采购 → 仓储 → 生产 → 质量 → 财务」，悬停可高亮上下游，点击直达业务页面</p>
        </div>
      </div>

      <div class="tools">
        <label class="tools__search">
          <svg-icon icon-class="search" />
          <input v-model="keyword" type="text" placeholder="搜索环节，如：检验" />
          <button v-if="keyword" class="tools__clear" @click="keyword = ''">×</button>
        </label>
        <button class="tools__btn" :title="theme === 'dark' ? '切换浅色' : '切换深色'" @click="toggleTheme">
          <svg-icon :icon-class="theme === 'dark' ? 'sunny' : 'moon'" />
          <span>{{ theme === 'dark' ? '浅色' : '深色' }}</span>
        </button>
        <button class="tools__btn" title="刷新菜单权限" @click="loadModules">
          <svg-icon icon-class="swap" />
          <span>刷新</span>
        </button>
        <button class="tools__btn" title="打印 / 导出 PDF" @click="handlePrint">
          <svg-icon icon-class="download" />
          <span>导出</span>
        </button>
      </div>
    </header>

    <el-empty
      v-if="!hasAnyMenu"
      description="暂无可访问的业务模块，请联系管理员分配菜单权限"
      :image-size="120"
      style="padding: 80px 0"
    />

    <template v-else>
      <!-- 经营决策带：贯穿全链 -->
      <section class="span-band">
        <div class="span-band__left">
          <span class="span-band__badge"><svg-icon icon-class="dashboard" /></span>
          <div>
            <h3>经营决策层</h3>
            <p>贯穿全链的数据洞察：订单、库存、产能、质量、资金实时汇总</p>
          </div>
        </div>
        <div class="span-band__chips">
          <button
            v-for="chip in biChips"
            :key="chip"
            class="schip"
            :class="{ 'is-dim': keyword && !hit(chip) }"
            @click="enter('bi', chip)"
          >
            {{ chip }}
          </button>
        </div>
      </section>

      <!-- 主业务链 -->
      <section class="flow" :class="{ 'has-active': !!activeStage }">
        <template v-for="(stage, idx) in stages" :key="stage.key">
          <article
            class="stage"
            :class="{ 'is-active': activeStage === stage.key, 'is-dim': activeStage && activeStage !== stage.key }"
            :style="{ '--accent': stage.accent }"
            @mouseenter="activeStage = stage.key"
            @mouseleave="activeStage = ''"
          >
            <span class="stage__no">{{ stage.no }}</span>
            <span class="stage__aura" aria-hidden="true"></span>

            <header class="stage__head" @click="enterModule(stage.key)">
              <span class="stage__icon"><svg-icon :icon-class="stage.icon" /></span>
              <div class="stage__title">
                <h3>{{ stage.title }}</h3>
                <p>{{ stage.tagline }}</p>
              </div>
            </header>

            <ul class="steps">
              <li
                v-for="chip in stage.chips"
                :key="chip"
                class="vstep"
                :class="{ 'is-hit': hit(chip), 'is-fade': keyword && !hit(chip) }"
              >
                <button class="vstep__btn" @click.stop="enter(stage.key, chip)">
                  <span class="vstep__text">{{ chip }}</span>
                  <span class="vstep__go">›</span>
                </button>
              </li>
            </ul>

            <footer class="stage__foot" @click="enterModule(stage.key)">
              进入模块<span v-if="moduleCount(stage.key)"> · {{ moduleCount(stage.key) }} 项功能</span>
            </footer>
          </article>

          <div class="flow__link" v-if="idx < stages.length - 1" :aria-label="stage.linkNext">
            <span class="flow__link-line">
              <span class="flow__link-rail"><i class="flow__spark"></i></span>
              <i class="flow__link-arrow"></i>
            </span>
            <span class="flow__link-text">{{ stage.linkNext }}</span>
          </div>
        </template>
      </section>

      <!-- 价值闭环：财务 -->
      <section class="fin-band" :style="{ '--accent': fin.accent }">
        <div class="fin-band__intro">
          <span class="fin-band__badge"><svg-icon icon-class="money" /></span>
          <div>
            <h3>价值闭环 · 财务结算</h3>
            <p>{{ fin.note }}</p>
          </div>
        </div>
        <div class="fin-band__rail">
          <template v-for="(chip, i) in fin.chips" :key="chip">
            <button class="fin-chip" :class="{ 'is-dim': keyword && !hit(chip) }" @click="enter('fin', chip)">
              {{ chip }}
            </button>
            <span v-if="i < fin.chips.length - 1" class="fin-chip__arrow">›</span>
          </template>
        </div>
        <span class="fin-band__loop">回款反哺市场投入</span>
      </section>

      <!-- 支撑底座 -->
      <section class="base">
        <div class="base__head">
          <h3>支撑底座</h3>
          <p>设备、安全、人力、研发、行政与流程，为整条价值链提供持续供给</p>
        </div>
        <div class="base__grid">
          <article
            v-for="sup in supports"
            :key="sup.key"
            class="scard"
            :style="{ '--accent': sup.accent }"
          >
            <span class="scard__bar"></span>
            <header class="scard__head" @click="enterModule(sup.key)">
              <span class="scard__icon"><svg-icon :icon-class="sup.icon" /></span>
              <h4>{{ sup.title }}</h4>
            </header>
            <p class="scard__note">{{ sup.note }}</p>
            <div class="scard__chips">
              <button
                v-for="chip in sup.chips"
                :key="chip"
                class="schip schip--sm"
                :class="{ 'is-dim': keyword && !hit(chip) }"
                @click.stop="enter(sup.key, chip)"
              >
                {{ chip }}
              </button>
            </div>
          </article>
        </div>
      </section>

      <footer class="bmap__foot">
        全景图仅展示有权限的核心环节，模块内的全部明细功能请点击卡片进入。图由菜单权限实时生成。
      </footer>
    </template>
  </div>
</template>

<script setup name="BusinessMap">
import { useRouter } from 'vue-router'
import usePermissionStore from '@/store/modules/permission'
import { getNormalPath } from '@/utils/ruoyi'
import { isExternal } from '@/utils/validate'

const router = useRouter()
const permissionStore = usePermissionStore()

/* ============ 业务模型（归纳后的核心环节，不是菜单全量） ============ */
const stages = [
  {
    key: 'mk', no: '01', title: '市场与销售', icon: 'peoples', accent: '#38bdf8',
    tagline: '线索 → 商机 → 合同 → 订单',
    chips: ['线索列表', '商机列表', '投标项目台账', '合同管理', '订单管理', '发货管理'],
    linkNext: '销售订单'
  },
  {
    key: 'pms', no: '02', title: '计划与采购', icon: 'shopping', accent: '#6366f1',
    tagline: '需求驱动 · 比价下单到货',
    chips: ['供应商信息', '采购申请', '询比价管理', '采购订单', '收货验收', '发票结算'],
    linkNext: '到货入库'
  },
  {
    key: 'wms', no: '03', title: '仓储与库存', icon: 'storage', accent: '#14b8a6',
    tagline: '收货上架 · 领料出库 · 盘点',
    chips: ['物料管理', '入库单管理', '库存查询', '出库单管理', '盘点任务', '库存预警'],
    linkNext: '领料投产'
  },
  {
    key: 'mms', no: '04', title: '生产与执行', icon: 'build', accent: '#a855f7',
    tagline: '排产 → 工单 → 报工 → 完工',
    chips: ['BOM管理', '主生产计划', '排产工作台', '工单管理', '报工管理', '完工入库'],
    linkNext: '完工送检'
  },
  {
    key: 'qms', no: '05', title: '质量与交付', icon: 'shield', accent: '#f59e0b',
    tagline: '来料/过程/成品检验与追溯',
    chips: ['检验任务', 'NCR台账', 'CAPA台账', '追溯查询', '客诉台账', '供应商质量评价'],
    linkNext: '合格发货'
  }
]

const fin = {
  key: 'fin', accent: '#10b981',
  chips: ['应收管理', '应付管理', '成本台账', '资金流水', '往来对账', '期末结账'],
  note: '发货开票 → 回款核销；采购入库 → 应付付款；生产成本自动结转'
}

const biChips = ['决策驾驶舱', '销售看板', '库存看板', '指标字典']

const supports = [
  { key: 'dms', title: '设备管理', icon: 'setting', accent: '#22d3ee', note: '设备可用率与维修保障', chips: ['设备台账', '点检任务', 'PM计划', '工单列表'] },
  { key: 'safety', title: '安全生产', icon: 'alert', accent: '#ef4444', note: '风险前置管控与培训', chips: ['隐患管理', '风险点管理', '培训计划', '应急预案'] },
  { key: 'hr', title: '人力资源', icon: 'peoples', accent: '#ec4899', note: '人岗匹配与工时供给', chips: ['员工档案', '入职管理', '考勤记录', '定薪调薪'] },
  { key: 'pdm', title: '产品设计', icon: 'tree-table', accent: '#8b5cf6', note: 'BOM 与工艺数据源头', chips: ['产品档案', '设计方案', '设计BOM', '技术变更'] },
  { key: 'oa', title: '综合办公', icon: 'office-building', accent: '#64748b', note: '用印、车辆等行政事务', chips: ['印章管理', '车辆管理', '司机管理'] },
  { key: 'flow', title: '流程中心', icon: 'list', accent: '#0ea5e9', note: '跨模块审批与流转总线', chips: ['待办流程', '流程发起', '用印流程台账'] }
]

/* ============ 状态 ============ */
const keyword = ref('')
const activeStage = ref('')
const theme = ref(localStorage.getItem('edop-bmap-theme') || 'dark')

/** 模块路径 → { 标题, 菜单树 } */
const moduleMap = ref({})
/** 模块 key（去掉前导 /）→ Map<菜单标题, 完整路径> */
const menuIndex = ref({})
const hasAnyMenu = computed(() => Object.keys(moduleMap.value).length > 0)

function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('edop-bmap-theme', theme.value)
}

/* ============ 菜单数据 ============ */
function resolvePath(parentPath, childPath) {
  if (isExternal(childPath)) return childPath
  if (isExternal(parentPath)) return parentPath
  if (childPath.startsWith('/')) return childPath
  return getNormalPath(parentPath + '/' + childPath)
}

/** 递归收集叶子菜单：all=含隐藏页（用于路径匹配），visible=侧边栏可见（用于入口与计数） */
function collectLeafMenus(parentPath, route, all, visible) {
  const fullPath = resolvePath(parentPath, route.path)
  const children = route.children || []

  if (children.length === 0) {
    const title = route.meta?.title
    if (!title) return
    all.push({ path: fullPath, title })
    if (!route.hidden) visible.push({ path: fullPath, title })
    return
  }
  children.forEach(child => collectLeafMenus(fullPath, child, all, visible))
}

function loadModules() {
  const routes = permissionStore.sidebarRouters || []
  const mods = {}
  const index = {}

  routes
    .filter(route => {
      if (route.hidden) return false
      if (['', '/', '/index', '/user', '/system', '/monitor', '/tool', '/business-map'].includes(route.path)) return false
      if (!route.meta || !route.meta.title) return false
      return (route.children || []).some(c => !c.hidden)
    })
    .forEach(route => {
      const all = []
      const visible = []
      ;(route.children || []).forEach(child => collectLeafMenus(route.path, child, all, visible))
      if (visible.length === 0) return

      const key = route.path.replace(/^\//, '')
      mods[key] = { path: route.path, title: route.meta.title, leaves: visible }
      index[key] = {}
      // 隐藏页（如「报工管理」这类从其他页面进入的页面）也要能跳，但不作为模块入口
      all.forEach(m => {
        if (!index[key][m.title]) index[key][m.title] = m.path
      })
    })

  moduleMap.value = mods
  menuIndex.value = index
}

/** 按模块 + 菜单标题取真实路径；标题对不上时退回模块首页 */
function pathOf(moduleKey, title) {
  const local = menuIndex.value[moduleKey]
  if (local && local[title]) return local[title]
  for (const key of Object.keys(menuIndex.value)) {
    if (menuIndex.value[key][title]) return menuIndex.value[key][title]
  }
  return moduleEntry(moduleKey)
}

/** 模块第一个叶子菜单作为入口 */
function moduleEntry(moduleKey) {
  const mod = moduleMap.value[moduleKey]
  return mod && mod.leaves.length ? mod.leaves[0].path : ''
}

function moduleCount(moduleKey) {
  const mod = moduleMap.value[moduleKey]
  return mod ? mod.leaves.length : 0
}

/** 搜索命中（空关键词时全部命中） */
function hit(text) {
  const kw = keyword.value.trim()
  return !kw || String(text).includes(kw)
}

/* ============ 跳转 ============ */
function goPath(path) {
  if (!path) return
  if (isExternal(path)) {
    window.open(path, '_blank')
  } else {
    router.push(path).catch(() => {})
  }
}

function enter(moduleKey, chipTitle) {
  goPath(pathOf(moduleKey, chipTitle))
}

/** 每个模块的首个核心环节（作为「进入模块」的落点，比第一个菜单更贴合业务） */
const entryChip = computed(() => {
  const map = {}
  stages.forEach(s => { map[s.key] = s.chips[0] })
  supports.forEach(s => { map[s.key] = s.chips[0] })
  map[fin.key] = fin.chips[0]
  map.bi = biChips[0]
  return map
})

function enterModule(moduleKey) {
  const chip = entryChip.value[moduleKey]
  goPath(chip ? pathOf(moduleKey, chip) : moduleEntry(moduleKey))
}

function handlePrint() {
  const prev = theme.value
  theme.value = 'light'
  setTimeout(() => {
    window.print()
    theme.value = prev
  }, 60)
}

loadModules()
</script>

<style scoped lang="scss">
/* ================= 主题变量 ================= */
.bmap {
  position: relative;
  min-height: calc(100vh - 50px);
  padding: 22px 26px 40px;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', system-ui, sans-serif;
  transition: background 0.4s ease, color 0.4s ease;
}

.bmap[data-theme='dark'] {
  --bm-bg: #060a18;
  --bm-text: #edf1ff;
  --bm-text-2: #97a3c2;
  --bm-text-3: #67739a;
  --bm-panel: rgba(255, 255, 255, 0.042);
  --bm-panel-2: rgba(255, 255, 255, 0.025);
  --bm-chip: rgba(255, 255, 255, 0.065);
  --bm-card: rgba(14, 22, 44, 0.86);
  --bm-border: rgba(255, 255, 255, 0.09);
  --bm-border-soft: rgba(255, 255, 255, 0.06);
  --bm-grid: rgba(120, 155, 255, 0.055);
  --bm-shadow: 0 18px 50px rgba(0, 0, 0, 0.45);
  --bm-inset: inset 0 1px 0 rgba(255, 255, 255, 0.07);
  background: var(--bm-bg);
  color: var(--bm-text);
}

.bmap[data-theme='light'] {
  --bm-bg: #f4f7fd;
  --bm-text: #17203a;
  --bm-text-2: #5b6784;
  --bm-text-3: #8b96ae;
  --bm-panel: #ffffff;
  --bm-panel-2: #f8faff;
  --bm-chip: #f3f6fd;
  --bm-card: #ffffff;
  --bm-border: #e2e8f5;
  --bm-border-soft: #eef2fa;
  --bm-grid: rgba(80, 110, 200, 0.06);
  --bm-shadow: 0 14px 38px rgba(30, 50, 110, 0.1);
  --bm-inset: inset 0 1px 0 #ffffff;
  background: var(--bm-bg);
  color: var(--bm-text);
}

/* ================= 背景装饰 ================= */
.bmap__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;

  .bmap__grid {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(var(--bm-grid) 1px, transparent 1px),
      linear-gradient(90deg, var(--bm-grid) 1px, transparent 1px);
    background-size: 44px 44px;
    mask-image: radial-gradient(ellipse 90% 70% at 50% 0%, #000 20%, transparent 78%);
    -webkit-mask-image: radial-gradient(ellipse 90% 70% at 50% 0%, #000 20%, transparent 78%);
  }

  .bmap__blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(90px);
    opacity: 0.5;
    animation: drift 18s ease-in-out infinite alternate;
  }

  .bmap__blob--1 {
    width: 520px;
    height: 520px;
    top: -220px;
    left: -80px;
    background: radial-gradient(circle, rgba(56, 189, 248, 0.55), transparent 70%);
  }

  .bmap__blob--2 {
    width: 560px;
    height: 560px;
    top: -180px;
    right: -120px;
    background: radial-gradient(circle, rgba(168, 85, 247, 0.45), transparent 70%);
    animation-delay: -6s;
  }

  .bmap__blob--3 {
    width: 620px;
    height: 620px;
    bottom: -320px;
    left: 40%;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.35), transparent 70%);
    animation-delay: -11s;
  }
}

.bmap[data-theme='light'] .bmap__blob {
  opacity: 0.32;
}

@keyframes drift {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to { transform: translate3d(40px, 30px, 0) scale(1.12); }
}

/* ================= 顶部 ================= */
.bmap__head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;

  &__mark {
    width: 46px;
    height: 46px;
    border-radius: 14px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #fff;
    background: linear-gradient(135deg, #4f7cff, #8b5cf6 62%, #22d3ee);
    box-shadow: 0 10px 26px rgba(79, 124, 255, 0.38);
  }

  &__text {
    h1 {
      margin: 0;
      font-size: 22px;
      font-weight: 700;
      letter-spacing: 0.5px;

      em {
        margin-left: 8px;
        font-size: 11px;
        font-weight: 500;
        font-style: normal;
        letter-spacing: 1.6px;
        text-transform: uppercase;
        color: var(--bm-text-3);
      }
    }

    p {
      margin: 5px 0 0;
      font-size: 12.5px;
      color: var(--bm-text-2);
    }
  }
}

.tools {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;

  &__search {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    height: 34px;
    padding: 0 10px;
    border-radius: 10px;
    border: 1px solid var(--bm-border);
    background: var(--bm-panel);
    color: var(--bm-text-3);
    backdrop-filter: blur(8px);

    input {
      width: 168px;
      border: 0;
      outline: 0;
      background: transparent;
      color: var(--bm-text);
      font-size: 12.5px;

      &::placeholder {
        color: var(--bm-text-3);
      }
    }
  }

  &__clear {
    border: 0;
    background: transparent;
    color: var(--bm-text-3);
    cursor: pointer;
    font-size: 14px;
    line-height: 1;

    &:hover {
      color: var(--bm-text);
    }
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    height: 34px;
    padding: 0 12px;
    border-radius: 10px;
    border: 1px solid var(--bm-border);
    background: var(--bm-panel);
    color: var(--bm-text-2);
    font-size: 12.5px;
    cursor: pointer;
    backdrop-filter: blur(8px);
    transition: all 0.18s ease;

    &:hover {
      color: var(--bm-text);
      border-color: rgba(120, 150, 255, 0.55);
      transform: translateY(-1px);
    }
  }
}

/* ================= 经营决策带 ================= */
.span-band {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  padding: 14px 20px;
  margin-bottom: 22px;
  border-radius: 16px;
  border: 1px solid transparent;
  background:
    linear-gradient(var(--bm-card), var(--bm-card)) padding-box,
    linear-gradient(100deg, rgba(99, 102, 241, 0.7), rgba(34, 211, 238, 0.55), rgba(168, 85, 247, 0.7)) border-box;
  box-shadow: var(--bm-shadow), var(--bm-inset);
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -30%;
    width: 30%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(140, 170, 255, 0.16), transparent);
    animation: sheen 6s ease-in-out infinite;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0.3px;
    }

    p {
      margin: 3px 0 0;
      font-size: 11.5px;
      color: var(--bm-text-2);
    }
  }

  &__badge {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #c7d2fe;
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.32), rgba(34, 211, 238, 0.22));
    border: 1px solid rgba(129, 140, 248, 0.35);
  }

  &__chips {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }
}

@keyframes sheen {
  0% { left: -30%; }
  55%, 100% { left: 110%; }
}

/* ================= 通用小胶囊 ================= */
.schip {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--bm-border-soft);
  background: var(--bm-chip);
  color: var(--bm-text-2);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover {
    color: var(--bm-text);
    border-color: rgba(129, 160, 255, 0.5);
    background: rgba(99, 102, 241, 0.12);
    transform: translateY(-1px);
  }

  &--sm {
    height: 25px;
    padding: 0 9px;
    font-size: 11.5px;
  }

  &.is-dim {
    opacity: 0.3;
  }
}

/* ================= 主链 ================= */
.flow {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: stretch;
  gap: 0;
  margin-bottom: 22px;
  overflow-x: auto;
  padding-bottom: 4px;

  &::-webkit-scrollbar {
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(140, 160, 210, 0.3);
    border-radius: 3px;
  }
}

.stage {
  position: relative;
  display: flex;
  flex-direction: column;
  flex: 1 1 0;
  min-width: 178px;
  padding: 16px 14px 12px;
  border-radius: 18px;
  border: 1px solid var(--bm-border);
  background-color: var(--bm-card);
  background-image: linear-gradient(180deg, color-mix(in srgb, var(--accent) 20%, transparent), transparent 58%);
  box-shadow: var(--bm-shadow), var(--bm-inset);
  backdrop-filter: blur(10px);
  overflow: hidden;
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.28s, opacity 0.28s, border-color 0.28s;

  /* 顶部光条 */
  &::before {
    content: '';
    position: absolute;
    inset: 0 12px auto;
    height: 2px;
    border-radius: 2px;
    background: linear-gradient(90deg, transparent, var(--accent), transparent);
    opacity: 0.9;
  }

  &__no {
    position: absolute;
    top: 2px;
    right: 12px;
    font-size: 52px;
    font-weight: 800;
    line-height: 1;
    letter-spacing: -2px;
    color: var(--accent);
    opacity: 0.13;
    pointer-events: none;
  }

  &__aura {
    position: absolute;
    top: -60px;
    left: -30px;
    width: 180px;
    height: 180px;
    border-radius: 50%;
    background: radial-gradient(circle, color-mix(in srgb, var(--accent) 42%, transparent), transparent 68%);
    opacity: 0.35;
    pointer-events: none;
    transition: opacity 0.3s;
  }

  &:hover,
  &.is-active {
    transform: translateY(-5px);
    border-color: color-mix(in srgb, var(--accent) 55%, var(--bm-border));
    box-shadow: 0 22px 54px color-mix(in srgb, var(--accent) 26%, rgba(0, 0, 0, 0.4)), var(--bm-inset);

    .stage__aura {
      opacity: 0.75;
    }
  }

  &.is-dim {
    opacity: 0.55;
    filter: saturate(0.7);
  }

  &__head {
    position: relative;
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 12px;
    margin-bottom: 6px;
    border-bottom: 1px solid var(--bm-border-soft);
    cursor: pointer;
  }

  &__icon {
    width: 34px;
    height: 34px;
    flex: 0 0 auto;
    border-radius: 11px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: var(--accent);
    background: color-mix(in srgb, var(--accent) 18%, transparent);
    border: 1px solid color-mix(in srgb, var(--accent) 34%, transparent);
  }

  &__title {
    min-width: 0;

    h3 {
      margin: 0;
      font-size: 14.5px;
      font-weight: 600;
      letter-spacing: 0.3px;
    }

    p {
      margin: 3px 0 0;
      font-size: 11px;
      color: var(--bm-text-3);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  &__foot {
    margin-top: 8px;
    padding-top: 9px;
    border-top: 1px dashed var(--bm-border-soft);
    font-size: 11px;
    color: var(--bm-text-3);
    cursor: pointer;
    transition: color 0.16s ease;

    &:hover {
      color: var(--accent);
    }
  }
}

/* 环节竖向串联 */
.steps {
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1 1 auto;
}

.vstep {
  position: relative;
  padding-left: 15px;

  /* 竖线 */
  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 3px;
    top: 18px;
    bottom: 2px;
    width: 1px;
    background: linear-gradient(180deg, color-mix(in srgb, var(--accent) 70%, transparent), transparent);
    opacity: 0.55;
  }

  /* 节点圆点 */
  &::after {
    content: '';
    position: absolute;
    left: 0;
    top: 12px;
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 8px color-mix(in srgb, var(--accent) 80%, transparent);
  }

  &.is-fade {
    opacity: 0.32;
  }

  &.is-hit::after {
    box-shadow: 0 0 0 4px color-mix(in srgb, var(--accent) 30%, transparent), 0 0 10px var(--accent);
  }

  &__btn {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 6px;
    padding: 7px 8px;
    margin-bottom: 2px;
    border: 1px solid transparent;
    border-radius: 9px;
    background: transparent;
    color: var(--bm-text-2);
    font-size: 12.5px;
    text-align: left;
    cursor: pointer;
    transition: all 0.16s ease;

    &:hover {
      color: var(--bm-text);
      background: color-mix(in srgb, var(--accent) 14%, transparent);
      border-color: color-mix(in srgb, var(--accent) 34%, transparent);
      transform: translateX(2px);

      .vstep__go {
        opacity: 1;
        transform: translateX(0);
      }
    }
  }

  &__text {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  &__go {
    flex: 0 0 auto;
    font-size: 14px;
    line-height: 1;
    color: var(--accent);
    opacity: 0;
    transform: translateX(-3px);
    transition: all 0.16s ease;
  }
}

/* 阶段之间的流转线 */
.flow__link {
  position: relative;
  flex: 0 0 62px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 7px;

  &-line {
    position: relative;
    display: block;
    width: calc(100% - 10px);
    height: 2px;
  }

  &-rail {
    position: absolute;
    inset: 0;
    border-radius: 2px;
    background: linear-gradient(90deg, rgba(140, 160, 220, 0.1), rgba(140, 160, 220, 0.45), rgba(140, 160, 220, 0.1));
    overflow: hidden;
  }

  &-arrow {
    position: absolute;
    right: -7px;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-left: 6px solid rgba(160, 180, 235, 0.8);
    border-top: 4px solid transparent;
    border-bottom: 4px solid transparent;
  }

  &-text {
    font-size: 10px;
    letter-spacing: 0.4px;
    color: var(--bm-text-3);
    white-space: nowrap;
  }
}

.flow__spark {
  position: absolute;
  top: -1px;
  left: -40%;
  width: 40%;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(90deg, transparent, #8ab4ff, transparent);
  animation: spark 2.6s linear infinite;
}

@keyframes spark {
  from { left: -40%; }
  to { left: 105%; }
}

/* ================= 财务闭环 ================= */
.fin-band {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  padding: 15px 20px;
  margin-bottom: 22px;
  border-radius: 16px;
  border: 1px solid var(--bm-border);
  background-color: var(--bm-card);
  background-image: linear-gradient(120deg, color-mix(in srgb, var(--accent) 16%, transparent), transparent 62%);
  box-shadow: var(--bm-shadow), var(--bm-inset);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 14%;
    bottom: 14%;
    width: 3px;
    border-radius: 0 3px 3px 0;
    background: var(--accent);
    box-shadow: 0 0 14px var(--accent);
  }

  &__intro {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 250px;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
    }

    p {
      margin: 3px 0 0;
      font-size: 11.5px;
      color: var(--bm-text-2);
      max-width: 340px;
    }
  }

  &__badge {
    width: 34px;
    height: 34px;
    flex: 0 0 auto;
    border-radius: 11px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: var(--accent);
    background: color-mix(in srgb, var(--accent) 18%, transparent);
    border: 1px solid color-mix(in srgb, var(--accent) 34%, transparent);
  }

  &__rail {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
    flex: 1;
  }

  &__loop {
    font-size: 11px;
    padding: 4px 10px;
    border-radius: 999px;
    color: var(--accent);
    background: color-mix(in srgb, var(--accent) 12%, transparent);
    border: 1px dashed color-mix(in srgb, var(--accent) 45%, transparent);
  }
}

.fin-chip {
  height: 28px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid var(--bm-border-soft);
  background: var(--bm-chip);
  color: var(--bm-text-2);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.16s ease;

  &:hover {
    color: var(--bm-text);
    border-color: color-mix(in srgb, var(--accent) 55%, transparent);
    background: color-mix(in srgb, var(--accent) 14%, transparent);
  }

  &.is-dim {
    opacity: 0.3;
  }
}

.fin-chip__arrow {
  color: var(--bm-text-3);
  font-size: 13px;
  line-height: 1;
}

/* ================= 支撑底座 ================= */
.base {
  position: relative;
  z-index: 1;

  &__head {
    display: flex;
    align-items: baseline;
    gap: 10px;
    margin-bottom: 12px;

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      letter-spacing: 0.3px;
    }

    p {
      margin: 0;
      font-size: 11.5px;
      color: var(--bm-text-3);
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: 12px;
  }
}

.scard {
  position: relative;
  padding: 13px 13px 11px;
  border-radius: 14px;
  border: 1px solid var(--bm-border);
  background: var(--bm-card);
  box-shadow: var(--bm-shadow), var(--bm-inset);
  overflow: hidden;
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease;

  &__bar {
    position: absolute;
    inset: 0 0 auto;
    height: 2px;
    background: linear-gradient(90deg, var(--accent), transparent 85%);
    opacity: 0.85;
  }

  &:hover {
    transform: translateY(-4px);
    border-color: color-mix(in srgb, var(--accent) 48%, var(--bm-border));
    box-shadow: 0 16px 36px color-mix(in srgb, var(--accent) 20%, rgba(0, 0, 0, 0.3));
  }

  &__head {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    margin-bottom: 6px;

    h4 {
      margin: 0;
      font-size: 13px;
      font-weight: 600;
    }
  }

  &__icon {
    width: 26px;
    height: 26px;
    flex: 0 0 auto;
    border-radius: 8px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    color: var(--accent);
    background: color-mix(in srgb, var(--accent) 16%, transparent);
  }

  &__note {
    margin: 0 0 9px;
    font-size: 11px;
    color: var(--bm-text-3);
  }

  &__chips {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }
}

/* ================= 底部说明 ================= */
.bmap__foot {
  position: relative;
  z-index: 1;
  margin-top: 18px;
  font-size: 11.5px;
  color: var(--bm-text-3);
  text-align: center;
}

/* ================= 响应式 ================= */
@media (max-width: 1500px) {
  .base__grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .stage {
    min-width: 168px;
  }

  .base__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .bmap {
    padding: 16px 14px 28px;
  }

  .base__grid {
    grid-template-columns: 1fr;
  }

  .flow {
    flex-direction: column;
  }

  .flow__link {
    flex: 0 0 34px;
  }
}

/* ================= 打印 ================= */
@media print {
  .tools {
    display: none;
  }

  .bmap {
    padding: 0;
    background: #fff;
  }

  .bmap__bg {
    display: none;
  }

  .stage,
  .scard,
  .fin-band,
  .span-band {
    break-inside: avoid;
    box-shadow: none !important;
    transform: none !important;
  }

  .flow {
    overflow: visible;
  }
}
</style>
