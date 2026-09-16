<template>
  <div class="ai-assist">
    <el-button :type="buttonType" :plain="buttonPlain" :size="size"
      :icon="MagicStick" :loading="loading" :disabled="disabled"
      @click="handleClick">{{ buttonText }}</el-button>
  </div>
</template>

<script setup name="AiAssistButton">
/**
 * 统一AI建议按钮
 * 业务页面一行接入：传 api（返回Promise）与参数，通过 @result / @error 回调接收
 * 组件内部处理 loading / 降级提示，业务代码只传参数和回调
 */
import { ref } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  buttonText: { type: String, default: 'AI建议' },
  buttonType: { type: String, default: 'success' },
  buttonPlain: { type: Boolean, default: true },
  size: { type: String, default: 'default' },
  disabled: { type: Boolean, default: false },
  /** 调用函数：(...args) => Promise */
  api: { type: Function, required: true },
  /** 传给api的参数 */
  params: { type: Array, default: () => [] },
  /** 降级时是否自动提示（默认true） */
  degradeTip: { type: Boolean, default: true }
})

const emit = defineEmits(['result', 'error'])
const loading = ref(false)

async function handleClick() {
  if (!props.api) return
  loading.value = true
  try {
    const res = await props.api(...props.params)
    const d = res.data || res
    if (d && (d.success === false)) {
      if (props.degradeTip) {
        ElMessage.warning(d.error || 'AI暂不可用，已降级处理')
      }
      emit('error', d)
    } else {
      emit('result', d)
    }
  } catch (e) {
    if (props.degradeTip) {
      ElMessage.warning('AI调用失败，请稍后重试或检查AI配置')
    }
    emit('error', e)
  } finally {
    loading.value = false
  }
}

defineExpose({ loading })
</script>
