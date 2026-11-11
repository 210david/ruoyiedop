<template>
  <div class="app-container flow-detail-standalone-page">
    <!--
      详情主体：复用 FlowDetail（全屏弹窗承载）。
      独立页不再额外画一条标题栏 + 关闭按钮：标题、实例号、关闭入口统一由弹窗头部承载，
      页面上只留详情内容那一张卡片（流程图页签铺满整屏，不留下方空白）。
    -->
    <div class="fd-body" v-loading="loading">
      <flow-detail
        v-if="instanceId"
        v-model:open="detailOpen"
        :instance-id="instanceId"
        standalone
      />
      <el-empty v-else description="缺少流程实例标识，无法查看详情" />
    </div>
  </div>
</template>

<script setup name="FlowDetailStandalone">
import { useRouter } from 'vue-router'
import FlowDetail from '@/components/FlowDetail/index.vue'

const route = useRoute()
const router = useRouter()

/** 实例ID 从 URL 参数取：/flow/detail/create?instanceId=xxx&defName=yyy */
const instanceId = ref(route.query.instanceId || '')
const loading = ref(false)
/** 独立页直接以 open=true 挂载 FlowDetail（其内部为全屏弹窗承载，页面形态下保持全屏） */
const detailOpen = ref(true)

/**
 * 关闭：本页由列表页以新标签页打开，点击「关闭」即关闭浏览器标签页。
 * 浏览器限制：非脚本自身 window.open 打开的标签页可能无法直接 close，
 * 此时回退到关闭历史的上一页（等价于离开详情页）。
 */
function closePage() {
  window.close()
  setTimeout(() => {
    if (!window.closed && window.history.length > 1) {
      router.back()
    }
  }, 150)
}

// 弹窗头部「关闭」→ 关掉详情弹窗（此处即整页）→ 关闭标签页 / 返回上一页
watch(detailOpen, open => {
  if (!open) closePage()
})
</script>

<style lang="scss" scoped>
.flow-detail-standalone-page {
  min-height: 100vh;
  padding: 0;
  background: #f8fafc;
}

/* 弹窗为 fullscreen，铺满视口；容器只负责承载，不再撑出额外滚动条 */
.fd-body { height: 100vh; overflow: hidden; }
</style>
