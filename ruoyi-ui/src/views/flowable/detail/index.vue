<template>
  <div class="app-container flow-detail-standalone-page">
    <!-- 顶部操作条：与「流程发起」页保持同一视觉口径（标题行 + 页签行） -->
    <div class="fd-header">
      <div class="fd-titlebar">
        <div class="fd-title">
          <span class="fd-glyph"></span>
          <span class="fd-name">{{ defName || '流程详情' }}</span>
          <span class="fd-ver" v-if="title">{{ title }}</span>
          <span class="fd-def" v-if="defName">流程详情</span>
          <span class="fd-no" v-if="instanceId">{{ instanceId }}</span>
        </div>
        <div class="fd-actions">
          <el-button icon="Close" @click="closePage">关 闭</el-button>
        </div>
      </div>
      <!-- 页签行：与办理页/发起页统一口径（视图切换由 FlowDetail 内部页签承载，此处仅对齐顶部视觉） -->
    </div>

    <!-- 详情主体：复用 FlowDetail（弹窗式全屏，X 已隐藏，由顶部「关闭」统一出口） -->
    <div class="fd-body" v-loading="loading">
      <flow-detail
        v-if="instanceId"
        v-model:open="detailOpen"
        :instance-id="instanceId"
      />
      <el-empty v-else description="缺少流程实例标识，无法查看详情" />
    </div>
  </div>
</template>

<script setup name="FlowDetailStandalone">
import { useRoute, useRouter } from 'vue-router'
import FlowDetail from '@/components/FlowDetail/index.vue'

const route = useRoute()
const router = useRouter()

/** 实例ID / 流程名 从 URL 参数取：/flow/detail/create?instanceId=xxx&defName=yyy */
const instanceId = ref(route.query.instanceId || '')
const defName = ref(route.query.defName || route.query.formName || '')
const title = ref(route.query.title || '')
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
</script>

<style lang="scss" scoped>
.flow-detail-standalone-page {
  min-height: 100vh;
  background: #f8fafc;
  padding: 0;
}

/* 顶部区：与 launch 页 launch-header / launch-titlebar 口径一致 */
.fd-header { padding: 12px 20px 0; background: #fff; }

.fd-titlebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 2px 0 10px;
}

.fd-title { display: flex; align-items: center; gap: 10px; min-width: 0; }

.fd-glyph {
  width: 4px;
  height: 18px;
  background: var(--brand-600, #4f46e5);
  border-radius: 2px;
  flex: none;
}

.fd-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--ink-900, #0f172a);
  white-space: nowrap;
}

.fd-ver {
  font-size: 12px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 999px;
  background: var(--brand-50, #eef2ff);
  color: var(--brand-700, #4338ca);
  flex: none;
}

.fd-def {
  font-size: 13px;
  color: var(--ink-500, #64748b);
  padding-left: 10px;
  border-left: 1px solid var(--ink-200, #e2e8f0);
  white-space: nowrap;
}

.fd-no {
  font-size: 12px;
  color: var(--ink-400, #94a3b8);
  font-family: ui-monospace, Menlo, monospace;
  white-space: nowrap;
}

.fd-actions { display: flex; align-items: center; gap: 8px; flex: none; }

.fd-body { min-height: calc(100vh - 57px); }

@media (max-width: 900px) {
  .fd-titlebar { flex-wrap: wrap; }
}
</style>
