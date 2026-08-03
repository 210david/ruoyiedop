<template>
  <div class="app-container home-portal">
    <!-- ========== 系统导航栏目 ========== -->
    <div class="portal-list" v-if="systemList.length > 0">
      <div
        class="system-card"
        v-for="sys in systemList"
        :key="sys.path"
      >
        <!-- 卡片头部 -->
        <div class="system-card__header" :style="{ background: sys.gradient }">
          <span class="system-card__icon">
            <svg-icon :icon-class="sys.icon" />
          </span>
          <div class="system-card__title-wrap">
            <span class="system-card__title">{{ sys.title }}</span>
            <span class="system-card__count">{{ sys.menus.length }} 个常用功能</span>
          </div>
          <span class="system-card__arrow" @click="goSystemFirst(sys)">
            <el-icon><ArrowRight /></el-icon>
          </span>
        </div>

        <!-- 卡片内容 - 快捷菜单（最多两行） -->
        <div class="system-card__body">
          <div
            v-for="menu in sys.menus"
            :key="menu.path"
            class="menu-tile"
            @click="goPath(menu.path)"
          >
            <span class="menu-tile__icon" :style="{ color: sys.color }">
              <svg-icon :icon-class="menu.icon" />
            </span>
            <span class="menu-tile__label">{{ menu.title }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 空状态 ========== -->
    <el-empty
      v-else
      description="暂无可访问的系统，请联系管理员分配权限"
      :image-size="120"
      style="padding: 80px 0"
    />
  </div>
</template>

<script setup name="Index">
import { ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import usePermissionStore from '@/store/modules/permission'
import { getNormalPath } from '@/utils/ruoyi'
import { isExternal } from '@/utils/validate'

const router = useRouter()
const permissionStore = usePermissionStore()

/** 系统样式映射 */
const systemStyles = {
  '/wms':     { gradient: 'linear-gradient(135deg, #409eff, #36cfc9)', color: '#409eff' },
  '/dms':     { gradient: 'linear-gradient(135deg, #67c23a, #95de64)', color: '#67c23a' },
  '/pms':     { gradient: 'linear-gradient(135deg, #e6a23c, #ffa940)', color: '#e6a23c' },
  '/mk':      { gradient: 'linear-gradient(135deg, #722ed1, #9254de)', color: '#722ed1' },
}
const defaultStyle = { gradient: 'linear-gradient(135deg, #4a5568, #718096)', color: '#4a5568' }

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
function collectLeafMenus(parentPath, route, sysPath) {
  const fullPath = resolvePath(parentPath, route.path)
  const visibleChildren = (route.children || []).filter(c => !c.hidden)

  if (visibleChildren.length === 0) {
    return [{
      path: fullPath,
      title: route.meta?.title || '',
      icon: route.meta?.icon || ''
    }]
  }

  return visibleChildren.flatMap(child => collectLeafMenus(fullPath, child, sysPath))
}

/** 系统列表 - 从权限路由动态生成，过滤掉系统管理和不常用菜单 */
const systemList = computed(() => {
  const routes = permissionStore.sidebarRouters || []
  return routes
    .filter(route => {
      // 过滤隐藏路由
      if (route.hidden) return false
      // 过滤首页、个人中心等非业务系统
      if (route.path === '' || route.path === '/index' || route.path === '/user') return false
      // 过滤系统管理
      if (route.path === '/system' || route.path === '/monitor' || route.path === '/tool') return false
      // 必须有标题
      if (!route.meta || !route.meta.title) return false
      // 必须有可见子菜单
      const visibleChildren = (route.children || []).filter(c => !c.hidden)
      return visibleChildren.length > 0
    })
    .map(route => {
      const style = systemStyles[route.path] || defaultStyle
      const visibleChildren = (route.children || []).filter(c => !c.hidden)
      // 递归收集所有叶子菜单
      const allMenus = visibleChildren.flatMap(child => collectLeafMenus(route.path, child, route.path))
      // 过滤掉不常用的菜单，限制最多16个（两行，每行8个）
      const excluded = excludedMenus[route.path] || []
      const filteredMenus = allMenus
        .filter(menu => !excluded.includes(menu.title))
        .slice(0, 16)
      return {
        path: route.path,
        title: route.meta.title,
        icon: route.meta.icon || 'system',
        gradient: style.gradient,
        color: style.color,
        menus: filteredMenus
      }
    })
    .filter(sys => sys.menus.length > 0)
})

/** 跳转到指定路径 */
function goPath(path) {
  if (!path) return
  if (isExternal(path)) {
    window.open(path, '_blank')
  } else {
    router.push(path)
  }
}

/** 点击系统头部箭头 - 跳转到该系统的第一个菜单 */
function goSystemFirst(sys) {
  if (sys.menus.length > 0) {
    goPath(sys.menus[0].path)
  }
}
</script>

<style scoped lang="scss">
.home-portal {
  background: #f0f2f5;
  min-height: calc(100vh - 120px);
  padding: 16px;
}

/* ========== 系统栏目列表（一行一个） ========== */
.portal-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ========== 系统卡片 ========== */
.system-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  }

  &__header {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    color: #fff;
    position: relative;
  }

  &__icon {
    width: 38px;
    height: 38px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 12px;

    .svg-icon {
      width: 20px;
      height: 20px;
      color: #fff;
    }
  }

  &__title-wrap {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
  }

  &__count {
    font-size: 12px;
    opacity: 0.85;
    margin-top: 1px;
  }

  &__arrow {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: background 0.2s;
    flex-shrink: 0;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }

    .el-icon {
      font-size: 16px;
      color: #fff;
    }
  }

  &__body {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 10px;
    padding: 14px 20px 18px;
  }
}

/* ========== 菜单快捷入口 ========== */
.menu-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 4px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.15s;
  text-align: center;
  min-height: 68px;

  &:hover {
    background: #e8ecf1;
    transform: translateY(-1px);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: translateY(0);
  }

  &__icon {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 5px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

    .svg-icon {
      width: 18px;
      height: 18px;
    }
  }

  &__label {
    font-size: 12px;
    color: #303133;
    font-weight: 500;
    line-height: 1.2;
    word-break: break-all;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

/* ========== 响应式 ========== */
@media (max-width: 1400px) {
  .system-card__body {
    grid-template-columns: repeat(8, 1fr);
  }
}

@media (max-width: 1200px) {
  .system-card__body {
    grid-template-columns: repeat(6, 1fr);
  }
}

@media (max-width: 992px) {
  .system-card__body {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (max-width: 768px) {
  .home-portal {
    padding: 12px;
  }
  .system-card__body {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
    padding: 12px 16px 16px;
  }
  .system-card__header {
    padding: 10px 16px;
  }
  .menu-tile {
    min-height: 60px;
    padding: 8px 2px;
  }
}

@media (max-width: 480px) {
  .system-card__body {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* ========== 暗色模式 ========== */
html.dark {
  .home-portal {
    background: var(--el-bg-color);
  }

  .system-card {
    background: var(--el-bg-color-overlay);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  }

  .menu-tile {
    background: var(--el-fill-color-light);

    &:hover {
      background: var(--el-fill-color);
    }

    &__icon {
      background: var(--el-bg-color);
    }

    &__label {
      color: var(--el-text-color-primary);
    }
  }
}
</style>
