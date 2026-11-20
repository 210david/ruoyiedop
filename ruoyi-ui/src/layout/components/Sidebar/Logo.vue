<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <span class="sidebar-logo-inner">
          <span class="sidebar-logo-mark">
            <img :src="logoMark" alt="郎鹰金属" />
          </span>
        </span>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <span class="sidebar-logo-inner">
          <span class="sidebar-logo-mark">
            <img :src="logoMark" alt="郎鹰金属" />
          </span>
          <span class="sidebar-logo-text">
            <span class="sidebar-title">{{ title }}</span>
            <span class="sidebar-title-en">QUJING LANG EAGLE</span>
          </span>
        </span>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'
import logoToneMark from '@/assets/logo/ly-mark.png'
import logoToneLight from '@/assets/logo/ly-mark-light.png'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const title = import.meta.env.VITE_APP_TITLE
const settingsStore = useSettingsStore()
const sideTheme = computed(() => settingsStore.sideTheme)

// 当前 Logo 背景是否为深色（决定用反白版还是原色版）
const isDarkBackground = computed(() => {
  if (settingsStore.isDark) {
    return true
  }
  if (settingsStore.navType == 3) {
    return false
  }
  return sideTheme.value === 'theme-dark'
})

// 获取Logo背景色
const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightBg
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 获取Logo文字颜色
const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-logo-text)'
  }
  if (settingsStore.navType == 3) {
    return variables.menuLightText
  }
  return sideTheme.value === 'theme-dark' ? '#fff' : variables.menuLightText
})

// 深色底用反白版、浅色底用原色版，保证徽标始终清晰
const logoMark = computed(() => (isDarkBackground.value ? logoToneLight : logoToneMark))
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 50px;
  background: v-bind(getLogoBackground);
  overflow: hidden;
  /* 容器自身 flex 居中：避免 sidebar.scss 里 a{display:inline-block} 的
     基线对齐把图标顶偏（旧版图标整体偏上的根因） */
  display: flex;
  align-items: center;
  justify-content: center;

  & .sidebar-logo-link {
    display: block;
    height: 100%;
    max-width: 100%;
  }

  /* flex 布局放在内层 span 上，不依赖 a 元素，
     规避全局 a 规则对 display 的覆盖 */
  & .sidebar-logo-inner {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
    height: 100%;
    padding: 0 8px;
    box-sizing: border-box;
  }

  & .sidebar-logo-mark {
    width: 28px;
    height: 28px;
    flex: 0 0 28px;
    display: inline-flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      display: block;
    }
  }

  & .sidebar-logo-text {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
  }

  & .sidebar-title {
    display: block;
    color: v-bind(getLogoTextColor);
    font-weight: 600;
    font-size: 13px;
    line-height: 1.2;
    font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* 中文标题下方的英文副行（公司英文名） */
  & .sidebar-title-en {
    display: block;
    margin-top: 3px;
    font-size: 8px;
    line-height: 1;
    letter-spacing: 1.2px;
    font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
    color: v-bind(getLogoTextColor);
    opacity: 0.55;
    white-space: nowrap;
    overflow: hidden;
  }

  &.collapse {
    .sidebar-logo-inner {
      padding: 0;
      gap: 0;
    }

    .sidebar-logo-mark {
      width: 32px;
      height: 32px;
      flex: 0 0 32px;
    }
  }
}
</style>
