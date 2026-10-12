<template>
  <div class="wm-mb">
    <div class="wm-tabs">
      <el-dropdown v-for="m in menus" :key="m.key" trigger="click" @command="onCmd" popper-class="wm-menu-popper">
        <span class="wm-tab" :class="{ active: active === m.key }" @click="active = m.key">{{ m.label }}</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="it in m.items" :key="it.cmd" :command="it.cmd" :divided="it.divided" :disabled="it.disabled">
              {{ it.label }}<span v-if="it.tag" class="wm-tag">{{ it.tag }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="wm-actions">
      <el-button size="small" @click="$emit('help')">帮助</el-button>
      <el-button size="small" @click="$emit('toggle-preview')">{{ preview ? '返回设计' : '预览' }}</el-button>
      <el-button size="small" type="primary" @click="$emit('save')">保 存</el-button>
    </div>
  </div>
</template>

<script setup name="WmDesignerMenuBar">
import { ref } from 'vue'

defineProps({ preview: Boolean })
const emit = defineEmits(['cmd', 'help', 'save', 'toggle-preview'])

const active = ref('format')
const menus = [
  { key: 'template', label: '模板', items: [
    { cmd: 'tmp_fee', label: '套用样式模板：费用报销式' },
    { cmd: 'tmp_apply', label: '套用样式模板：申请单式' },
    { cmd: 'tmp_fields', label: '从字段一键初始化' },
    { cmd: 'tmp_clear', label: '清空布局', divided: true }
  ] },
  { key: 'format', label: '格式', items: [
    { cmd: 'fmt_panel', label: '单元格格式…' },
    { cmd: 'fmt_gridlines', label: '显示/隐藏格线' }
  ] },
  { key: 'insert', label: '插入', items: [
    { cmd: 'ins_row_above', label: '上方插入一行' },
    { cmd: 'ins_row_below', label: '下方插入一行' },
    { cmd: 'ins_col_left', label: '左侧插入一列' },
    { cmd: 'ins_col_right', label: '右侧插入一列' },
    { cmd: 'ins_title', label: '标题行', divided: true },
    { cmd: 'ins_note', label: '说明行' },
    { cmd: 'ins_image', label: '图片…' }
  ] },
  { key: 'field', label: '字段属性', items: [
    { cmd: 'fld_bind', label: '绑定字段…' },
    { cmd: 'fld_clear', label: '清除绑定' },
    { cmd: 'fld_info', label: '字段信息', divided: true }
  ] },
  { key: 'detail', label: '明细表', items: [
    { cmd: 'dt_new', label: '新增明细表' }
  ] },
  { key: 'style', label: '样式', items: [
    { cmd: 'sty_painter', label: '格式刷' },
    { cmd: 'sty_clear', label: '清除样式' },
    { cmd: 'sty_manage', label: '样式模板管理', tag: 'P1', divided: true, disabled: true }
  ] }
]

function onCmd(cmd) { emit('cmd', cmd) }
</script>

<style lang="scss" scoped>
.wm-mb {
  display: flex; align-items: center; justify-content: space-between;
  background: #2F3C8E; border-radius: 8px 8px 0 0; padding: 0 10px; height: 40px; flex: none;
}
.wm-tabs { display: flex; align-items: stretch; height: 100%; }
.wm-tab { color: #e5e9ff; font-size: 13px; padding: 0 16px; display: flex; align-items: center; cursor: pointer; outline: none; }
.wm-tab:hover { background: #3D4AA0; }
.wm-tab.active { background: #fff; color: #2F3C8E; font-weight: 600; border-radius: 4px 4px 0 0; }
.wm-actions { display: flex; align-items: center; gap: 8px; }
.wm-actions :deep(.el-button) {
  color: #fff; background: transparent; border-color: rgba(255, 255, 255, .45);
}
.wm-actions :deep(.el-button:hover), .wm-actions :deep(.el-button:focus) {
  background: #3D4AA0; border-color: #fff; color: #fff;
}
.wm-actions :deep(.el-button--primary) { background: #4c5cc4; border-color: #4c5cc4; font-weight: 600; }
.wm-actions :deep(.el-button--primary:hover) { background: #5a6ad4; border-color: #5a6ad4; }
</style>
