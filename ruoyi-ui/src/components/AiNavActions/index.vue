<template>
  <div v-if="list.length" class="ai-nav-actions">
    <div class="ai-nav-title">
      <el-icon :size="12"><Pointer /></el-icon>
      <span>可直接打开以下功能</span>
    </div>
    <button
      v-for="(a, i) in list"
      :key="a.path + i"
      type="button"
      class="ai-nav-card"
      @click="go(a)"
    >
      <span class="ai-nav-icon">
        <svg-icon :icon-class="a.icon || 'component'" />
      </span>
      <span class="ai-nav-main">
        <span class="ai-nav-name">{{ a.title }}</span>
        <span class="ai-nav-crumb" :title="a.breadcrumb">{{ a.breadcrumb }}</span>
      </span>
      <el-icon class="ai-nav-arrow" :size="13"><ArrowRight /></el-icon>
    </button>
  </div>
</template>

<script setup name="AiNavActions">
/**
 * AI 菜单直达入口卡片
 *
 * 安全设计：点击前用当前用户路由树做二次校验。
 * 服务端已按登录人权限过滤过一遍，但角色/权限可能在会话期间被修改，
 * 这里再查一次，避免跳进已无权访问的页面导致 404 白屏。
 */
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { isExternal } from '@/utils/validate'
import { getNormalPath } from '@/utils/ruoyi'
import usePermissionStore from '@/store/modules/permission'

const props = defineProps({
  actions: { type: Array, default: () => [] }
})
const emit = defineEmits(['navigate'])

const router = useRouter()
const permissionStore = usePermissionStore()

/** 兜底去重：同路径只留一个（后端已去重，这里防历史脏数据） */
const list = computed(() => {
  const seen = new Set()
  return (props.actions || []).filter(a => {
    if (!a || !a.path || seen.has(a.path)) return false
    seen.add(a.path)
    return true
  })
})

/** 递归收集当前用户全部叶子路由路径（口径与侧边栏 resolvePath 一致） */
function collectPaths(routes, parentPath, out) {
  if (!routes || !routes.length) return
  routes.forEach(r => {
    const full = resolvePath(parentPath, r.path)
    const children = r.children || []
    if (children.length === 0) {
      if (full) out.add(full)
    } else {
      collectPaths(children, full, out)
    }
  })
}

function resolvePath(parentPath, childPath) {
  if (!childPath) return parentPath || ''
  if (isExternal(childPath)) return childPath
  if (isExternal(parentPath)) return parentPath
  if (childPath.startsWith('/')) return childPath
  if (!parentPath) return getNormalPath('/' + childPath)
  return getNormalPath(parentPath + '/' + childPath)
}

/** 校验路径是否在当前用户可访问路由内 */
function pathAllowed(path) {
  const set = new Set()
  const roots = permissionStore.sidebarRouters || []
  roots.forEach(r => {
    const full = r.path || ''
    if ((r.children || []).length === 0) {
      if (full) set.add(full)
    } else {
      collectPaths(r.children, full, set)
    }
  })
  return set.has(path)
}

function go(a) {
  if (!pathAllowed(a.path)) {
    emit('navigate', { ...a, denied: true })
    return
  }
  emit('navigate', a)
  router.push({ path: a.path, query: a.query || {} })
}
</script>

<style scoped>
.ai-nav-actions {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #E5E7EB;
}

.ai-nav-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 8px;
}

.ai-nav-card {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  text-align: left;
  padding: 9px 10px;
  margin-bottom: 6px;
  cursor: pointer;
  background: #F8FAFC;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  transition: all .15s;
}

.ai-nav-card:last-child {
  margin-bottom: 0;
}

.ai-nav-card:hover {
  background: #EFF6FF;
  border-color: #BFDBFE;
}

.ai-nav-icon {
  display: flex;
  width: 22px;
  height: 22px;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #2563EB;
}

.ai-nav-main {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
  flex: 1;
}

.ai-nav-name {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
}

.ai-nav-crumb {
  font-size: 11px;
  color: #9CA3AF;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-nav-arrow {
  color: #9CA3AF;
  flex-shrink: 0;
}
</style>
