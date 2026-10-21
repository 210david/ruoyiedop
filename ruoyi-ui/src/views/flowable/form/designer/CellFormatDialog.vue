<template>
  <el-dialog :model-value="open" @update:model-value="v => $emit('update:open', v)" width="620px" append-to-body draggable :close-on-click-modal="false" class="wm-cf-dialog">
    <template #header>
      <div class="cf-header">
        <span class="cf-title">单元格格式</span>
      </div>
    </template>
    <el-tabs v-model="tab">
      <!-- 数字 -->
      <el-tab-pane label="数字" name="num">
        <div class="cf-body">
          <div class="cf-col">
            <div class="cf-col-title">分类</div>
            <div class="cf-list">
              <div v-for="c in numCats" :key="c.v" class="cf-item" :class="{ on: curCat === c.v }" @click="setCat(c.v)">{{ c.l }}</div>
            </div>
          </div>
          <div class="cf-col cf-col-wide">
            <div class="cf-col-title">类型</div>
            <template v-if="curCat === 'date'">
              <div class="cf-list">
                <div v-for="p in datePatterns" :key="p" class="cf-item" :class="{ on: (cell?.style.fmt?.pattern) === p }" @click="setFmt({ cat: 'date', pattern: p })">{{ p }}</div>
              </div>
            </template>
            <template v-else-if="curCat === 'number' || curCat === 'percent'">
              <div class="cf-form">
                <label>{{ curCat === 'percent' ? '百分比小数位数' : '小数位数' }}</label>
                <el-input-number :model-value="cell?.style.fmt?.decimals ?? 2" :min="0" :max="6" :controls="false" size="small" style="width:100px"
                  @update:model-value="v => setFmt({ cat: curCat, decimals: v })" />
                <div class="cf-tip">{{ curCat === 'percent' ? '详情/预览按 ×100 显示为百分比' : '详情/预览按千分位+小数位显示' }}</div>
              </div>
            </template>
            <div v-else class="cf-tip">该分类无附加设置：{{ curCat === 'general' ? '按原值显示' : curCat === 'time' ? '按时间显示（HH:mm:ss）' : curCat === 'text' ? '按文本原样显示' : '' }}</div>
          </div>
        </div>
      </el-tab-pane>
      <!-- 对齐 -->
      <el-tab-pane label="对齐" name="align">
        <div class="cf-form">
          <label>水平对齐</label>
          <el-radio-group v-model="alignModel">
            <el-radio value="left">左对齐</el-radio><el-radio value="center">居中</el-radio><el-radio value="right">右对齐</el-radio>
          </el-radio-group>
        </div>
        <div class="cf-form">
          <label>垂直对齐</label>
          <el-radio-group v-model="valignModel">
            <el-radio value="top">顶对齐</el-radio><el-radio value="middle">垂直居中</el-radio><el-radio value="bottom">底对齐</el-radio>
          </el-radio-group>
        </div>
        <div class="cf-form">
          <label>缩进（0-8，每格 8px）</label>
          <el-input-number :model-value="cell?.style.indent ?? 0" :min="0" :max="8" :controls="false" size="small" style="width:100px"
            @update:model-value="v => setStyle('indent', v || 0)" />
        </div>
      </el-tab-pane>
      <!-- 字体 -->
      <el-tab-pane label="字体" name="font">
        <div class="cf-form">
          <label>字体 / 字号</label>
          <div class="cf-row">
            <el-select :model-value="cell?.style.font ?? 'Microsoft YaHei'" size="small" style="width:140px" @update:model-value="v => setStyle('font', v)">
              <el-option v-for="f in fonts" :key="f" :label="fontCn(f)" :value="f" />
            </el-select>
            <el-select :model-value="cell?.style.fontSize ?? 12" size="small" style="width:76px" @update:model-value="v => setStyle('fontSize', Number(v))">
              <el-option v-for="s in [9, 10, 11, 12, 14, 16, 18, 20, 24]" :key="s" :label="s" :value="s" />
            </el-select>
          </div>
        </div>
        <div class="cf-form">
          <label>字形</label>
          <div class="cf-row">
            <el-checkbox :model-value="cell?.style.bold" @update:model-value="v => setStyle('bold', v)"><b>加粗</b></el-checkbox>
            <el-checkbox :model-value="cell?.style.italic" @update:model-value="v => setStyle('italic', v)"><i>斜体</i></el-checkbox>
            <el-checkbox :model-value="cell?.style.underline" @update:model-value="v => setStyle('underline', v)"><u>下划线</u></el-checkbox>
            <el-checkbox :model-value="cell?.style.strike" @update:model-value="v => setStyle('strike', v)"><s>删除线</s></el-checkbox>
          </div>
        </div>
        <div class="cf-form">
          <label>字体颜色</label>
          <el-color-picker :model-value="cell?.style.color || '#333333'" @update:model-value="v => setStyle('color', v || '#333333')" />
        </div>
      </el-tab-pane>
      <!-- 边框 -->
      <el-tab-pane label="边框" name="border">
        <div class="cf-form">
          <label>线条样式</label>
          <el-select v-model="bdStyle" size="small" style="width:120px">
            <el-option v-for="b in borderStyles" :key="b.v" :label="b.l" :value="b.v">
              <span class="bd-item"><BorderPresetIcon kind="style" :styleV="b.v" />{{ b.l }}</span>
            </el-option>
          </el-select>
        </div>
        <div class="cf-form">
          <label>线条颜色</label>
          <el-color-picker v-model="bdColor" />
        </div>
        <div class="cf-form">
          <label>边框类型</label>
          <el-select v-model="bdPreset" size="small" style="width:140px">
            <el-option v-for="p in borderPresets" :key="p.v" :label="p.l" :value="p.v">
              <span class="bd-item"><BorderPresetIcon :preset="p.v" />{{ p.l }}</span>
            </el-option>
          </el-select>
          <el-button size="small" type="primary" @click="applyBorder">应用到单元格</el-button>
        </div>
      </el-tab-pane>
      <!-- 填充 -->
      <el-tab-pane label="填充" name="fill">
        <div class="cf-form">
          <label>背景颜色</label>
          <el-color-picker :model-value="cell?.style.bg || ''" @update:model-value="v => setStyle('bg', v || '')" />
        </div>
        <div class="cf-form">
          <label>常用色</label>
          <div class="cf-swatches">
            <span v-for="c in swatches" :key="c" class="cf-swatch" :style="{ background: c }" @click="setStyle('bg', c)"></span>
            <el-button size="small" link @click="setStyle('bg', '')">无填充</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button type="primary" @click="$emit('update:open', false)">确 定</el-button>
      <el-button @click="$emit('update:open', false)">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="WmCellFormatDialog">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BorderPresetIcon from './BorderPresetIcon.vue'

const props = defineProps({ open: Boolean, cell: { type: Object, default: null } })
defineEmits(['update:open'])

const tab = ref('num')
const fonts = ['Microsoft YaHei', 'SimSun', 'SimHei', 'KaiTi', 'FangSong', 'DengXian']
const numCats = [
  { v: 'general', l: '常规' }, { v: 'number', l: '数值' }, { v: 'date', l: '日期' },
  { v: 'time', l: '时间' }, { v: 'percent', l: '百分比' }, { v: 'text', l: '文本' }
]
const datePatterns = ['2013/3/14', '2013-3-14', '2013年3月14日', '2013年3月', '3月14日', '2013/3/14 10:13 AM', '2013/3/14 10:13']
const borderStyles = [
  { v: '0', l: '无' }, { v: '1', l: '细线' }, { v: '2', l: '粗线' },
  { v: '3', l: '虚线' }, { v: '4', l: '点线' }, { v: '5', l: '双线' }
]
const borderPresets = [
  { v: 'all', l: '四边框' }, { v: 'outer', l: '外框' }, { v: 'none', l: '无边框' },
  { v: 'top', l: '仅上边框' }, { v: 'bottom', l: '仅下边框' }, { v: 'left', l: '仅左边框' }, { v: 'right', l: '仅右边框' },
  { v: 'hinner', l: '内部横线' }, { v: 'vinner', l: '内部竖线' }
]
const swatches = ['#ffffff', '#fef3c7', '#fecaca', '#d1fae5', '#dbeafe', '#e0e7ff', '#f3e8ff', '#f1f5f9', '#fde68a', '#bbf7d0']
const bdStyle = ref('1')
const bdColor = ref('#333333')
const bdPreset = ref('all')

const FONT_CN = { 'Microsoft YaHei': '微软雅黑', SimSun: '宋体', SimHei: '黑体', KaiTi: '楷体', FangSong: '仿宋', DengXian: '等线' }
function fontCn(f) { return FONT_CN[f] || f }

const curCat = computed(() => (props.cell && props.cell.style.fmt && props.cell.style.fmt.cat) || 'general')
const alignModel = computed({ get: () => (props.cell ? (props.cell.style.align || 'left') : 'left'), set: v => setStyle('align', v) })
const valignModel = computed({ get: () => (props.cell ? (props.cell.style.valign || 'middle') : 'middle'), set: v => setStyle('valign', v) })

watch(() => props.open, v => { if (v) { tab.value = 'num'; bdStyle.value = '1'; bdPreset.value = 'all' } })

function setCat(v) {
  const c = props.cell
  if (!c) return
  if (v === 'general' || v === 'text') c.style.fmt = null
  else c.style.fmt = { ...(c.style.fmt || {}), cat: v, pattern: v === 'date' ? (c.style.fmt?.pattern || '2013-3-14') : undefined }
}
function setFmt(p) {
  const c = props.cell
  if (!c) return
  c.style.fmt = { ...(c.style.fmt || {}), ...p }
}
function setStyle(k, v) {
  const c = props.cell
  if (!c) return
  c.style[k] = v
}
function applyBorder() {
  const c = props.cell
  if (!c) return
  // 内部横线/竖线仅对多格选区有意义（在工具条下拉中使用）；单格对话框中直接提示
  if (bdPreset.value === 'hinner' || bdPreset.value === 'vinner') {
    ElMessage.warning('内部横线/竖线需框选多格后从工具条"线框"下拉设置')
    return
  }
  const e = bdPreset.value === 'none' ? { s: 0 } : { s: Number(bdStyle.value) || 1, c: bdColor.value || '#333333' }
  // 'none'（无边框）必须显式覆盖四边为 s:0；否则 sides=['none'] 写入无效键，边框不会被清除
  const all = ['top', 'right', 'bottom', 'left']
  const sides = (bdPreset.value === 'all' || bdPreset.value === 'outer' || bdPreset.value === 'none') ? all : [bdPreset.value]
  for (const s of sides) c.style.borders[s] = { ...e }
  // 单边预设（如仅上边框）：其余边显式清除，避免残留旧边框导致"设置不生效"的观感（Excel 同款行为）
  if (bdPreset.value !== 'all' && bdPreset.value !== 'outer') for (const s of all) if (!sides.includes(s)) c.style.borders[s] = { s: 0 }
}
</script>

<style lang="scss" scoped>
.cf-header { display: flex; align-items: center; }
.cf-title { font-weight: 600; }
.cf-body { display: flex; gap: 16px; min-height: 220px; }
.cf-col { width: 120px; flex: none; }
.cf-col-wide { flex: 1; width: auto; }
.cf-col-title { font-size: 12px; color: #64748b; margin-bottom: 6px; }
.cf-list { border: 1px solid #e2e8f0; border-radius: 6px; max-height: 220px; overflow: auto; }
.cf-item { font-size: 13px; color: #334155; padding: 6px 10px; cursor: pointer; }
.cf-item:hover { background: #f1f5f9; }
.cf-item.on { background: #e0e7ff; color: #2F3C8E; }
.cf-form { margin-bottom: 14px; }
.cf-form label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
.cf-row { display: flex; align-items: center; gap: 8px; }
.cf-tip { font-size: 12px; color: #94a3b8; line-height: 1.6; }
.cf-swatches { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.cf-swatch { width: 18px; height: 18px; border: 1px solid #c4cbd8; border-radius: 3px; cursor: pointer; }
.cf-swatch:hover { outline: 2px solid #2F3C8E; outline-offset: 1px; }
</style>
