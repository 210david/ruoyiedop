<template>
  <div class="navbar" :class="'nav' + settingsStore.navType">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb v-if="settingsStore.navType == 1" id="breadcrumb-container" class="breadcrumb-container" />
    <top-nav v-if="settingsStore.navType == 2" id="topmenu-container" class="topmenu-container" />
    <template v-if="settingsStore.navType == 3">
      <logo v-show="settingsStore.sidebarLogo" :collapse="false"></logo>
      <top-bar id="topbar-container" class="topbar-container" />
    </template>

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <header-notice id="header-notice" />
      </template>

      <div class="right-divider"></div>

      <el-dropdown @command="handleCommand" class="avatar-container" trigger="hover">
        <div class="avatar-wrapper">
          <div class="avatar-ring">
            <img :src="userStore.avatar" class="user-avatar" />
          </div>
          <span class="user-nickname">{{ userStore.nickName }}</span>
          <el-icon class="arrow-icon"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <router-link to="/user/profile">
              <el-dropdown-item>个人中心</el-dropdown-item>
            </router-link>
            <el-dropdown-item command="setLayout" v-if="settingsStore.showSettings">
                <span>布局设置</span>
            </el-dropdown-item>
            <el-dropdown-item command="lockScreen">
                <span>锁定屏幕</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from './TopNav'
import TopBar from './TopBar'
import Logo from './Sidebar/Logo'
import Hamburger from '@/components/Hamburger'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useLockStore from '@/store/modules/lock'
import useSettingsStore from '@/store/modules/settings'
import useTagsViewStore from '@/store/modules/tagsView'
import HeaderNotice from './HeaderNotice'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const lockStore = useLockStore()
const settingsStore = useSettingsStore()

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case "setLayout":
      setLayout()
      break
    case "lockScreen":
      lockScreen()
      break
    case "logout":
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      // 手动登出时清除超时恢复用的最后路由记录和标签页
      useTagsViewStore().clearLastRoute()
      useTagsViewStore().delAllViews()
      // 手动登出使用 Vue Router 跳转，不携带 redirect 参数
      router.push({ path: '/login' })
    })
  }).catch(() => { })
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

function lockScreen() {
  const currentPath = route.fullPath
  lockStore.lockScreen(currentPath)
  router.push('/lock')
}

</script>

<style lang='scss' scoped>
.navbar.nav3 {
  .hamburger-container {
    display: none !important;
  }
}

.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: var(--navbar-bg);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  // padding: 0 8px;
  box-sizing: border-box;

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    margin-right: 8px;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    flex-shrink: 0;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .topbar-container {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow: hidden;
    margin-left: 8px;
  }

  .right-menu {
    height: 100%;
    display: flex;
    align-items: center;
    margin-left: auto;
    padding-right: 12px;
    gap: 2px;

    &:focus {
      outline: none;
    }

    .right-divider {
      width: 1px;
      height: 20px;
      background: var(--el-border-color-light, #e4e7ed);
      margin: 0 6px;
      flex-shrink: 0;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0 8px;
      height: 36px;
      border-radius: 8px;
      font-size: 18px;
      color: var(--navbar-text, #5a5e66);
      cursor: pointer;
      transition: background 0.25s, color 0.25s;

      &.hover-effect {
        &:hover {
          background: var(--el-fill-color-light, rgba(0, 0, 0, 0.04));
          color: var(--el-color-primary, #409eff);
        }
      }
    }

    .avatar-container {
      cursor: pointer;
      padding: 0 10px;
      height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      transition: background 0.25s;

      &:hover {
        background: var(--el-fill-color-light, rgba(0, 0, 0, 0.04));
      }

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;

        .avatar-ring {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          padding: 2px;
          background: linear-gradient(135deg, var(--el-color-primary, #409eff), color-mix(in srgb, var(--el-color-primary, #409eff) 60%, #fff));
          flex-shrink: 0;

          .user-avatar {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            display: block;
            object-fit: cover;
          }
        }

        .user-nickname {
          font-size: 13px;
          font-weight: 600;
          color: var(--navbar-text, #303133);
          white-space: nowrap;
          max-width: 120px;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .arrow-icon {
          font-size: 12px;
          color: var(--navbar-text, #909399);
          opacity: 0.6;
          transition: transform 0.25s, opacity 0.25s;
        }
      }

      &:hover .arrow-icon {
        transform: rotate(180deg);
        opacity: 1;
      }
    }
  }

  // 暗色模式适配
  html.dark & {
    .right-menu {
      .right-menu-item {
        &.hover-effect:hover {
          background: var(--el-fill-color, rgba(255, 255, 255, 0.08));
        }
      }

      .avatar-container {
        &:hover {
          background: var(--el-fill-color, rgba(255, 255, 255, 0.08));
        }
      }
    }
  }
}
</style>
