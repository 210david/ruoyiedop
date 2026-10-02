<template>
  <el-dialog :model-value="open" @update:model-value="v => $emit('update:open', v)" width="380px" append-to-body draggable :close-on-click-modal="false" class="wm-rz-dialog">
    <template #header>
      <div class="wm-rz-head">
        <el-icon :size="15"><FullScreen /></el-icon>
        <span>{{ kind === 'row' ? '设置行高' : '设置列宽' }}</span>
      </div>
    </template>
    <div class="rw-field">
      <label class="rw-label">{{ kind === 'row' ? '行高' : '列宽' }}</label>
      <el-input v-model="val" class="rw-input" @keyup.enter="confirm">
        <template #append>{{ kind === 'row' || unit === 'px' ? 'px' : '%' }}</template>
      </el-input>
    </div>
    <div class="rw-field" v-if="kind === 'col'">
      <label class="rw-label">单位</label>
      <el-radio-group v-model="unit">
        <el-radio value="px">像素</el-radio>
        <el-radio value="pct">百分比</el-radio>
      </el-radio-group>
    </div>
    <div class="rw-tip" v-if="err">{{ err }}</div>
    <div class="rw-tip ok" v-else-if="kind === 'col' && unit === 'pct'">百分比按画布总宽自动折算为像素</div>
    <template #footer>
      <div class="rw-footer">
        <el-button type="primary" @click="confirm">确 定</el-button>
        <span class="rw-div"></span>
        <el-button @click="$emit('update:open', false)">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="WmResizeDialog">
import { ref, watch } from 'vue'

const props = defineProps({
  open: Boolean,
  kind: { type: String, default: 'row' },   // row | col
  index: { type: Number, default: 0 },
  current: { type: Number, default: 30 },
  totalPx: { type: Number, default: 1000 }  // 百分比折算基准（画布总宽/总高）
})
const emit = defineEmits(['update:open', 'apply'])

const val = ref('30')
const unit = ref('px')
const err = ref('')

watch(() => props.open, v => {
  if (!v) return
  err.value = ''
  val.value = String(props.current)
  unit.value = 'px'
})

function confirm() {
  err.value = ''
  const n = Number(val.value)
  if (!val.value || isNaN(n) || n <= 0) { err.value = '注：' + (props.kind === 'row' ? '行高' : '列宽') + '值不能为零或负数！'; return }
  if (props.kind === 'col' && unit.value === 'pct') {
    if (n > 100) { err.value = '注：百分比不能超过 100！'; return }
    const px = Math.max(40, Math.round((props.totalPx || 1000) * n / 100))
    emit('apply', { kind: props.kind, index: props.index, px })
    emit('update:open', false)
    return
  }
  const min = props.kind === 'row' ? 20 : 40
  if (n < min) { err.value = '注：' + (props.kind === 'row' ? '行高' : '列宽') + '最小 ' + min + 'px！'; return }
  emit('apply', { kind: props.kind, index: props.index, px: Math.round(n) })
  emit('update:open', false)
}
</script>

<style lang="scss" scoped>
/* 以下样式作用于插槽内容（teleport 后仍保留 scoped 属性），弹窗容器样式见 designer.scss */
.rw-field { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.rw-label { width: 40px; font-size: 13px; color: #334155; font-weight: 600; }
.rw-input { width: 150px; }
.rw-tip { font-size: 12px; color: #e0342b; line-height: 1.6; padding-left: 50px; }
.rw-tip.ok { color: #64748b; }
.rw-footer { display: flex; align-items: center; justify-content: center; }
.rw-div { width: 1px; height: 14px; background: #e2e8f0; margin: 0 6px; }
</style>
