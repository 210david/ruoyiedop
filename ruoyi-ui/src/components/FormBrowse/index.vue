<template>
  <div class="form-browse" :class="{ 'is-disabled': disabled }">
    <!-- 数据源缺失（历史表单的「自定义」浏览框等）：降级为普通输入框，保证旧数据可编辑不丢 -->
    <el-input v-if="!pickerComp" :model-value="modelValue" :disabled="disabled" :placeholder="props.placeholder || '请填写'" @update:model-value="(v) => emit('update:modelValue', v)" />
    <el-input v-else :model-value="modelValue" readonly :disabled="disabled" :placeholder="placeholderText" class="fb-input" @click="openPicker">
      <template #suffix>
        <el-icon v-if="hasValue && !disabled" class="fb-clear" title="清除" @click.stop="clearValue">
          <CircleClose />
        </el-icon>
      </template>
      <template #append>
        <el-button :icon="Search" :disabled="disabled" title="浏览选择" @click.stop="openPicker" />
      </template>
    </el-input>
    <component :is="pickerComp" v-if="pickerComp" ref="pickerRef" v-bind="pickerProps" @confirm="onConfirm" />
  </div>
</template>

<script setup name="FormBrowse">
/**
 * 流程表单浏览按钮（统一的浏览按钮）
 *
 * 一个字段 = 只读输入框（显示已选名称）+ 浏览按钮，点击后打开该数据源对应的
 * 真实弹框选择器（人员/部门/岗位/角色/客户/供应商/物料/设备/合同/仓库/备件/产品/项目）。
 * 数据源在表单设计器的字段属性里配置：browseType（数据源）+ browseMultiple（是否多选）。
 *
 * 取值口径：
 * v-model → 名称文本（多选用「、」连接），用于详情/打印/只读展示
 * v-model:modelIds → ID 文本（逗号分隔），仅用于再次打开弹框时回显高亮
 */
import { computed, ref } from 'vue';
import { Search, CircleClose } from '@element-plus/icons-vue';
import { browseSourceOf, browsePlaceholder } from './sources.js';

const props = defineProps({
  /** 字段定义（读 browseType / browseMultiple / placeholder） */
  f: { type: Object, default: () => ({}) },
  /** 显示值：已选名称 */
  modelValue: { type: [String, Number], default: '' },
  /** 存储值：已选 ID（逗号分隔），用于弹框回显 */
  modelIds: { type: [String, Array], default: '' },
  /** 附加信息（人员字段带回部门等），由父级存到 `${field}__extra` */
  modelExtra: { type: Object, default: null },
  disabled: { type: Boolean, default: false },
  placeholder: { type: String, default: '' },
});
const emit = defineEmits(['update:modelValue', 'update:modelIds', 'update:modelExtra']);

const pickerRef = ref(null);

const source = computed(() => browseSourceOf(props.f));
const pickerComp = computed(() => (source.value ? source.value.component : null));
const pickerProps = computed(() => (source.value && source.value.buildProps ? source.value.buildProps(props.f) : {}));
const placeholderText = computed(() => props.placeholder || browsePlaceholder(props.f));
const hasValue = computed(() => props.modelValue !== undefined && props.modelValue !== null && String(props.modelValue) !== '');
/** 已选 ID 数组：兼容逗号串与数组两种入参 */
const selectedIds = computed(() => {
  const raw = props.modelIds;
  const list = Array.isArray(raw) ? raw : String(raw === undefined || raw === null ? '' : raw).split(',');
  return list.map((s) => String(s).trim()).filter(Boolean);
});
/** 已选名称数组：与 ID 按下标一一对应，供设备多选等需要「行数据」回显的选择器 */
const selectedNames = computed(() =>
  String(props.modelValue === undefined || props.modelValue === null ? '' : props.modelValue)
    .split('、')
    .filter(Boolean),
);

/** 打开弹框选择器 */
function openPicker() {
  if (props.disabled) return;
  const p = pickerRef.value;
  if (!p || typeof p.open !== 'function') return;
  const args = source.value && source.value.openArgs ? source.value.openArgs(selectedIds.value, props.f, selectedNames.value) : [];
  p.open(...args);
}

/** 确认选择：名称写入字段值，ID 写入伴生值，部门等附加信息写入 extra */
function onConfirm(payload) {
  if (!source.value) return;
  const list = (Array.isArray(payload) ? payload : [payload]).filter(Boolean);
  if (!list.length) return;
  const rows = list.map((item) => source.value.pick(item)).filter((r) => r && r.name);
  if (!rows.length) return;
  emit('update:modelValue', rows.map((r) => r.name).join('、'));
  emit('update:modelIds', rows.map((r) => (r.id === undefined || r.id === null ? '' : r.id)).join(','));
  // 单选人员时带回部门：多选下一个人对应多个部门，不替用户猜
  const hit = rows.length === 1 ? rows[0] : null;
  emit('update:modelExtra', hit && hit.deptName ? { deptId: hit.deptId, deptName: hit.deptName } : null);
}

/** 清除已选 */
function clearValue() {
  emit('update:modelValue', '');
  emit('update:modelIds', '');
  emit('update:modelExtra', null);
}
</script>

<style lang="scss" scoped>
.form-browse {
  width: 100%;
  min-width: 0;
}

/* 只读输入框整体可点：鼠标提示「可浏览」 */
.fb-input :deep(.el-input__wrapper) {
  cursor: pointer;
}

.fb-input :deep(.el-input__inner) {
  cursor: pointer;
  text-overflow: ellipsis;
}

/* 清除按钮：悬浮变深，与项目其他表单字段一致 */
.fb-clear {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
  transition: color 0.15s;

  &:hover {
    color: #909399;
  }
}
</style>
