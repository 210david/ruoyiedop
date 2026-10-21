<template>
  <!-- 线框样式小图标（参照 Excel/泛微边框下拉的视觉样式） -->
  <svg class="bp-icon" viewBox="0 0 16 14" width="16" height="14" aria-hidden="true">
    <!-- kind=preset：边框类型 -->
    <template v-if="kind === 'preset'">
      <!-- 四边框：外框 + 内部十字线全部实线 -->
      <template v-if="preset === 'all'">
        <line x1="2.5" y1="1.5" x2="13.5" y2="1.5" class="solid" />
        <line x1="2.5" y1="12.5" x2="13.5" y2="12.5" class="solid" />
        <line x1="2.5" y1="1.5" x2="2.5" y2="12.5" class="solid" />
        <line x1="13.5" y1="1.5" x2="13.5" y2="12.5" class="solid" />
        <line x1="2.5" y1="7" x2="13.5" y2="7" class="solid thin" />
        <line x1="8" y1="1.5" x2="8" y2="12.5" class="solid thin" />
      </template>
      <!-- 外框：仅外侧实线 -->
      <template v-else-if="preset === 'outer'">
        <line x1="2.5" y1="1.5" x2="13.5" y2="1.5" class="solid" />
        <line x1="2.5" y1="12.5" x2="13.5" y2="12.5" class="solid" />
        <line x1="2.5" y1="1.5" x2="2.5" y2="12.5" class="solid" />
        <line x1="13.5" y1="1.5" x2="13.5" y2="12.5" class="solid" />
      </template>
      <!-- 无边框：虚线示意 -->
      <template v-else-if="preset === 'none'">
        <rect x="2.5" y="1.5" width="11" height="11" class="dash" />
      </template>
      <!-- 仅上/下/左/右边框：其余边淡色虚线示意 -->
      <template v-else>
        <rect x="2.5" y="1.5" width="11" height="11" class="dash" />
        <line v-if="preset === 'top'" x1="2.5" y1="1.5" x2="13.5" y2="1.5" class="solid" />
        <line v-if="preset === 'bottom'" x1="2.5" y1="12.5" x2="13.5" y2="12.5" class="solid" />
        <line v-if="preset === 'left'" x1="2.5" y1="1.5" x2="2.5" y2="12.5" class="solid" />
        <line v-if="preset === 'right'" x1="13.5" y1="1.5" x2="13.5" y2="12.5" class="solid" />
      </template>
      <!-- 内部横线：外框淡虚线，中间横线实线 -->
      <template v-if="preset === 'hinner'">
        <rect x="2.5" y="1.5" width="11" height="11" class="dash" />
        <line x1="2.5" y1="7" x2="13.5" y2="7" class="solid" />
      </template>
      <!-- 内部竖线：外框淡虚线，中间竖线实线 -->
      <template v-if="preset === 'vinner'">
        <rect x="2.5" y="1.5" width="11" height="11" class="dash" />
        <line x1="8" y1="1.5" x2="8" y2="12.5" class="solid" />
      </template>
    </template>
    <!-- kind=style：线条样式（无/细/粗/虚/点/双线） -->
    <template v-else>
      <template v-if="styleV === '0'"><line x1="1.5" y1="7" x2="14.5" y2="7" class="dash" /></template>
      <template v-else-if="styleV === '1'"><line x1="1.5" y1="7" x2="14.5" y2="7" class="solid" /></template>
      <template v-else-if="styleV === '2'"><line x1="1.5" y1="7" x2="14.5" y2="7" class="solid thick" /></template>
      <template v-else-if="styleV === '3'"><line x1="1.5" y1="7" x2="14.5" y2="7" class="solid dashed-long" /></template>
      <template v-else-if="styleV === '4'"><line x1="1.5" y1="7" x2="14.5" y2="7" class="solid dotted" /></template>
      <template v-else-if="styleV === '5'">
        <line x1="1.5" y1="4.5" x2="14.5" y2="4.5" class="solid" />
        <line x1="1.5" y1="9.5" x2="14.5" y2="9.5" class="solid" />
      </template>
    </template>
  </svg>
</template>

<script setup name="BorderPresetIcon">
defineProps({
  preset: { type: String, default: '' },
  kind: { type: String, default: 'preset' },
  styleV: { type: String, default: '' }
})
</script>

<style>
/* 注意：菜单/下拉可能 teleport 到 body，故样式用全局 */
.bp-icon { flex: none; display: block; color: #3b4557; }
.bp-icon .solid { stroke: currentColor; stroke-width: 1.6; stroke-linecap: square; }
.bp-icon .solid.thin { stroke-width: 1; }
.bp-icon .solid.thick { stroke-width: 2.6; }
.bp-icon .solid.dashed-long { stroke-dasharray: 4 2.5; }
.bp-icon .solid.dotted { stroke-width: 2; stroke-dasharray: 0.5 3; stroke-linecap: round; }
.bp-icon .dash { stroke: currentColor; stroke-width: 1; stroke-dasharray: 2.5 2.5; opacity: .35; fill: none; }
.bd-item { display: inline-flex; align-items: center; gap: 8px; }
.el-dropdown-menu__item .bd-item,
.el-select-dropdown__item .bd-item { width: 100%; }
</style>
