<template>
  <div class="wm-ft">
    <!-- 单行分组（泛微式）：字体 | 对齐 | 缩进 | 线框 | 合并格式 | 行列 | 清除 | 坐标 -->
    <div class="wm-g" title="字体">
      <el-select :model-value="styleObj?.font ?? 'Microsoft YaHei'" size="small" @update:model-value="v => emitSet('font', v)" :disabled="!hasSel" style="width:100px">
        <el-option v-for="f in fonts" :key="f" :label="fontCn(f)" :value="f" />
      </el-select>
      <el-select :model-value="styleObj?.fontSize ?? 12" size="small" @update:model-value="v => emitSet('fontSize', Number(v))" :disabled="!hasSel" style="width:56px">
        <el-option v-for="s in [9, 10, 11, 12, 14, 16, 18, 20, 24]" :key="s" :label="s" :value="s" />
      </el-select>
      <button class="wm-btn b" :class="{ on: styleObj?.bold }" :disabled="!hasSel" @click="$emit('toggle', 'bold')" title="加粗"><b>B</b></button>
      <button class="wm-btn i" :class="{ on: styleObj?.italic }" :disabled="!hasSel" @click="$emit('toggle', 'italic')" title="斜体"><i>I</i></button>
      <button class="wm-btn u" :class="{ on: styleObj?.underline }" :disabled="!hasSel" @click="$emit('toggle', 'underline')" title="下划线"><u>U</u></button>
      <button class="wm-btn s" :class="{ on: styleObj?.strike }" :disabled="!hasSel" @click="$emit('toggle', 'strike')" title="删除线"><s>S</s></button>
      <span class="wm-cp" title="字体颜色">
        <span class="a-glyph" :style="{ color: styleObj?.color || '#333' }">A</span>
        <el-color-picker :model-value="styleObj?.color || '#333333'" size="small" :disabled="!hasSel" @update:model-value="v => emitSet('color', v || '#333333')" />
      </span>
      <span class="wm-cp" title="背景填充颜色">
        <span class="bg-glyph" :style="{ background: styleObj?.bg || '#fff' }"></span>
        <el-color-picker :model-value="styleObj?.bg || ''" size="small" :disabled="!hasSel" @update:model-value="v => emitSet('bg', v || '')" />
      </span>
      <button class="wm-btn" :class="{ on: painter }" :disabled="!hasSel" @click="$emit('painter')" title="格式刷：吸取当前样式，点击目标格应用"><WmIcon name="painter" /></button>
    </div>
    <div class="wm-g" title="对齐">
      <button v-for="a in ['left', 'center', 'right']" :key="a" class="wm-btn" :class="{ on: styleObj?.align === a }" :disabled="!hasSel" @click="$emit('align', a)" :title="alignTitle[a]"><WmIcon :name="'align-' + a" /></button>
      <button v-for="v in ['top', 'middle', 'bottom']" :key="v" class="wm-btn" :class="{ on: styleObj?.valign === v }" :disabled="!hasSel" @click="$emit('valign', v)" :title="valignTitle[v]"><WmIcon :name="'align-' + v" /></button>
    </div>
    <div class="wm-g" title="缩进">
      <button class="wm-btn" :disabled="!hasSel || !(styleObj?.indent)" @click="$emit('indent', -1)" title="后缩进（减少缩进）"><WmIcon name="indent-out" /></button>
      <button class="wm-btn" :disabled="!hasSel || (styleObj?.indent ?? 0) >= 8" @click="$emit('indent', 1)" title="前缩进（增加缩进）"><WmIcon name="indent-in" /></button>
    </div>
    <div class="wm-g" title="线框">
      <el-select :model-value="borderStyle" size="small" @update:model-value="v => (borderStyle = v)" :disabled="!hasSel" style="width:76px">
        <el-option v-for="b in borderStyles" :key="b.v" :label="b.l" :value="b.v" />
      </el-select>
      <el-color-picker v-model="borderColor" size="small" :disabled="!hasSel" title="线框颜色" />
      <el-dropdown trigger="click" @command="p => $emit('border', { preset: p, color: borderColor, style: Number(borderStyle) })" :disabled="!hasSel">
        <button class="wm-btn" :disabled="!hasSel" title="线框类型"><WmIcon name="border-grid" /><i class="caret">▾</i></button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="p in borderPresets" :key="p.v" :command="p.v">{{ p.l }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="wm-g" title="单元格合并与格式">
      <button class="wm-btn" :disabled="!canMerge" @click="$emit('merge')" title="合并单元格（框选相邻区域后可用）"><WmIcon name="merge" />合并</button>
      <button class="wm-btn" :disabled="!canSplit" @click="$emit('split')" title="拆分单元格（选中已合并格后可用）"><WmIcon name="split" />拆分</button>
      <button class="wm-btn" :disabled="!hasSel" @click="$emit('cell-dialog')" title="单元格格式（数字/对齐/字体/边框/填充）"><WmIcon name="pencil" />格式</button>
    </div>
    <div class="wm-g" title="行与列">
      <button class="wm-btn" :disabled="!hasSel" @click="$emit('ins-row')" title="在选中行下方插入一行"><WmIcon name="insert-row" />插行</button>
      <button class="wm-btn" :disabled="!hasSel" @click="$emit('ins-col')" title="在选中列右侧插入一列"><WmIcon name="insert-col" />插列</button>
      <button class="wm-btn danger" :disabled="!hasSel" @click="$emit('del-row')" title="删除选中行"><WmIcon name="delete-row" />删行</button>
      <button class="wm-btn danger" :disabled="!hasSel" @click="$emit('del-col')" title="删除选中列"><WmIcon name="delete-col" />删列</button>
    </div>
    <div class="wm-g no-sep" title="清除">
      <button class="wm-btn" :disabled="!hasSel" @click="$emit('clear-content')" title="清除内容（保留格式）"><WmIcon name="eraser" />内容</button>
      <button class="wm-btn" :disabled="!hasSel" @click="$emit('clear-format')" title="清除格式（保留内容）"><WmIcon name="broom" />格式</button>
      <button class="wm-btn danger" :disabled="!hasSel" @click="$emit('clear-all')" title="全部清除（内容+格式）"><WmIcon name="trash" />全部</button>
    </div>
    <span class="wm-pos">{{ posLabel || '未选中单元格' }}</span>
  </div>
</template>

<script setup name="WmFormatToolbar">
import { ref } from 'vue'
import WmIcon from './WmIcon.vue'

defineProps({
  hasSel: Boolean,
  posLabel: { type: String, default: '' },
  styleObj: { type: Object, default: null },
  canMerge: Boolean,
  canSplit: Boolean,
  painter: Boolean
})
const emit = defineEmits(['set', 'toggle', 'align', 'valign', 'indent', 'border', 'merge', 'split', 'cell-dialog', 'ins-row', 'ins-col', 'del-row', 'del-col', 'clear-content', 'clear-format', 'clear-all', 'painter'])

const fonts = ['Microsoft YaHei', 'SimSun', 'SimHei', 'KaiTi', 'FangSong', 'DengXian']
const borderColor = ref('#333333')
const borderStyle = ref('1')
const borderStyles = [
  { v: '0', l: '无' }, { v: '1', l: '细线' }, { v: '2', l: '粗线' },
  { v: '3', l: '虚线' }, { v: '4', l: '点线' }, { v: '5', l: '双线' }
]
const borderPresets = [
  { v: 'all', l: '四边框' }, { v: 'outer', l: '外框' }, { v: 'none', l: '无边框' },
  { v: 'top', l: '仅上边框' }, { v: 'bottom', l: '仅下边框' }, { v: 'left', l: '仅左边框' }, { v: 'right', l: '仅右边框' }
]
const alignTitle = { left: '左对齐', center: '居中', right: '右对齐' }
const valignTitle = { top: '顶对齐', middle: '垂直居中', bottom: '底对齐' }
const FONT_CN = { 'Microsoft YaHei': '微软雅黑', SimSun: '宋体', SimHei: '黑体', KaiTi: '楷体', FangSong: '仿宋', DengXian: '等线' }
function fontCn(f) { return FONT_CN[f] || f }

function emitSet(k, v) { emit('set', { k, v }) }
</script>

<style lang="scss" scoped>
.wm-ft { display: flex; align-items: center; flex-wrap: wrap; gap: 3px; background: #f5f6fa; border: 1px solid #d9def0; border-radius: 6px; padding: 3px 6px; flex: none; }
/* 泛微式分组：单行排布，组间深色竖线分隔，组内图标紧凑 */
.wm-g { display: flex; align-items: center; gap: 2px; padding: 0 7px; border-right: 1px solid #c9d2e8; }
.wm-g.no-sep { border-right: none; }
.wm-ft :deep(.el-select) { width: 96px; }
.wm-btn {
  min-width: 24px; height: 24px; padding: 0 4px; border: 1px solid transparent; border-radius: 4px;
  background: transparent; cursor: pointer; font-size: 12px; color: #3b4557;
  display: inline-flex; align-items: center; justify-content: center; gap: 2px;
}
.wm-btn:hover:not(:disabled) { background: #e8ebf7; }
.wm-btn.on { background: #2F3C8E; color: #fff; }
.wm-btn.on :deep(.wm-icon), .wm-btn.on .wm-icon { stroke: #fff; }
.wm-btn.danger { color: #d03050; }
.wm-btn.danger:hover:not(:disabled) { background: #fdecef; }
.wm-btn:disabled { color: #7c8aa0; cursor: not-allowed; opacity: .75; }
.wm-pos { margin-left: auto; font-size: 12px; color: #64748b; padding-left: 8px; white-space: nowrap; }
.wm-cp { display: inline-flex; align-items: center; gap: 1px; }
.a-glyph { font-size: 12px; font-weight: 700; line-height: 1.15; }
.bg-glyph { width: 12px; height: 12px; border: 1px solid #c4cbd8; border-radius: 2px; display: inline-block; }
.caret { font-size: 10px; margin-left: 1px; font-style: normal; }
</style>
