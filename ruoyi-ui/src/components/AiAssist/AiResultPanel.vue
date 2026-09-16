<template>
  <div v-if="visible" class="ai-result-panel" :class="{ 'is-degraded': degraded }">
    <div class="ai-result-head">
      <el-icon :size="16" :color="degraded ? '#e6a23c' : '#67c23a'">
        <MagicStick />
      </el-icon>
      <span class="ai-result-title">{{ degraded ? 'AI降级提示' : 'AI建议' }}</span>
      <span class="ai-result-meta" v-if="latencyMs">耗时 {{ formatLatency }}</span>
      <el-button link type="primary" size="small" class="ai-result-close" @click="handleClose">关闭</el-button>
    </div>
    <div class="ai-result-body">
      <slot>{{ content }}</slot>
    </div>
    <div class="ai-result-foot">AI 结果仅供参考，请确认后使用</div>
  </div>
</template>

<script setup name="AiResultPanel">
/**
 * 统一AI结果展示面板
 * 含"AI 结果仅供参考，请确认后使用"合规声明；降级时切换为警示样式
 */
import { computed } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  content: { type: String, default: '' },
  degraded: { type: Boolean, default: false },
  latencyMs: { type: Number, default: 0 }
})

const emit = defineEmits(['close'])

const formatLatency = computed(() => {
  const v = props.latencyMs || 0
  return v < 1000 ? v + 'ms' : (v / 1000).toFixed(1) + 's'
})

function handleClose() {
  emit('close')
}
</script>

<style scoped>
.ai-result-panel {
  border: 1px solid #d9ecff;
  background: #f0f9ff;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 8px 0;
}
.ai-result-panel.is-degraded {
  border-color: #faecd8;
  background: #fdf6ec;
}
.ai-result-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.ai-result-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}
.ai-result-meta {
  font-size: 12px;
  color: #909399;
}
.ai-result-close {
  margin-left: auto;
}
.ai-result-body {
  font-size: 13px;
  color: #303133;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}
.ai-result-foot {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  border-top: 1px dashed #e4e7ed;
  padding-top: 6px;
}
</style>
