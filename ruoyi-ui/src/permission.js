import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import useTagsViewStore from '@/store/modules/tagsView'
import { hitShortcut } from '@/api/home'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/apidoc', '/openapi-doc', '/h5/register/**', '/h5/signin']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach(async (to, from) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title)
    const isLock = useLockStore().isLock
    if (to.path === '/login') {
      NProgress.done()
      return { path: '/' }
    }
    if (isWhiteList(to.path)) {
      return true
    }
    if (isLock && to.path !== '/lock') {
      NProgress.done()
      return { path: '/lock' }
    }
    if (!isLock && to.path === '/lock') {
      NProgress.done()
      return { path: '/' }
    }
    if (useUserStore().roles.length === 0) {
      isRelogin.show = true
      try {
        // 拉取user_info信息
        await useUserStore().getInfo()
        isRelogin.show = false
        // 根据roles权限生成可访问的路由
        const accessRoutes = await usePermissionStore().generateRoutes()
        accessRoutes.forEach(route => {
          if (!isHttp(route.path)) {
            router.addRoute(route)
          }
        })
        // 重新导航到目标路由，确保动态路由已注册
        return { ...to, replace: true }
      } catch (err) {
        await useUserStore().logOut()
        ElMessage.error(err)
        return { path: '/' }
      }
    }
    return true
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      return true
    }
    NProgress.done()
    return `/login?redirect=${to.fullPath}` // 否则全部重定向到登录页
  }
})

router.afterEach((to) => {
  NProgress.done()
  // 保存当前访问的路由到 sessionStorage，超时重新登录时用于恢复
  if (to.path && !isWhiteList(to.path) && to.path !== '/login') {
    useTagsViewStore().saveLastRoute(to)
    recordMenuVisit(to)
  }
})

/**
 * 记录菜单访问频次（供首页「常用功能」自动沉淀，同时作为新人任务完成判定依据）
 *
 * 只上报真实业务页面：跳过首页、个人中心、白名单；
 * 同一路径 5 分钟内不重复上报，后端另有 60 秒去重保护。
 * 上报失败静默处理，绝不影响正常导航。
 */
let lastVisit = { path: '', time: 0 }
const VISIT_SKIP_PREFIX = ['/user', '/system', '/monitor', '/tool', '/redirect']
function recordMenuVisit(to) {
  const path = to.path
  if (!path || path === '/' || path === '/index') {
    return
  }
  if (VISIT_SKIP_PREFIX.some(prefix => path.startsWith(prefix))) {
    return
  }
  const now = Date.now()
  if (lastVisit.path === path && now - lastVisit.time < 300000) {
    return
  }
  lastVisit = { path, time: now }
  const seg = path.split('/').filter(Boolean)[0]
  hitShortcut({
    path,
    title: to.meta?.title || '',
    icon: to.meta?.icon || '',
    module: seg ? '/' + seg : ''
  }).catch(() => {})
}
