<template>
  <div class="rule-panel">
    <div class="rp-head">
      <span class="rp-title">交互规则 <b>{{ rules.length }}</b></span>
      <el-dropdown @command="createRule">
        <el-button size="small" type="primary" icon="Plus">新建规则</el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="t in ruleTypes" :key="t.value" :command="t.value">{{ t.label }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="rp-tip">规则保存在表单扩展（ext_json）中，前端运行时与后端提交校验共用同一份规则。</div>

    <!-- AI 一句话生成 -->
    <div class="rp-ai">
      <el-input v-model="aiText" size="small" placeholder="一句话描述规则，例：金额大于5万时必须上传合同附件" :disabled="aiLoading" @keyup.enter="generateAi" />
      <el-button size="small" type="warning" :loading="aiLoading" @click="generateAi">AI 生成</el-button>
    </div>
    <div class="rp-tip" v-if="!props.formId">AI 生成需要先保存表单（生成时后端会校验字段与依赖环）。</div>

    <div v-if="!rules.length" class="rp-empty">还没有规则。点击「新建规则」为字段配置带出、显隐、必填或校验。</div>

    <div v-for="(r, i) in rules" :key="r.id" class="rp-item" :class="{ 'is-off': r.enabled === false }">
      <div class="rp-item-head">
        <el-tag size="small" :type="typeTag(r.type)">{{ typeLabel(r.type) }}</el-tag>
        <span class="rp-item-name" @click="editRule(r)">{{ r.name || '(未命名)' }}</span>
        <el-switch v-model="r.enabled" size="small" @change="emitChange" />
        <el-button link size="small" icon="Edit" @click="editRule(r)" />
        <el-button link size="small" icon="Delete" @click="removeRule(i)" />
      </div>
      <div class="rp-item-desc">{{ ruleSummary(r) }}</div>
    </div>

    <!-- 高级脚本：规则覆盖不了的复杂场景，用受控 JS 脚本实现 -->
    <div class="rp-scripts-head">
      <span class="rp-scripts-title">高级脚本</span>
      <el-button size="small" link icon="Plus" @click="editScript(null)">新建脚本</el-button>
    </div>
    <div class="rp-tip">脚本在填写人浏览器中执行（发起/办理页），支持 ctx.get(字段) / ctx.set(字段, 值) / ctx.detailData / ctx.fail(原因)。提交前脚本抛错会拦截提交。请勿配置不可重入的随机/时间赋值。</div>
    <div v-if="!scripts.length" class="rp-empty">暂无脚本。规则满足不了的复杂逻辑可用脚本实现。</div>
    <div v-for="(s, i) in scripts" :key="s.id" class="rp-item">
      <div class="rp-item-head">
        <el-tag size="small" type="warning" effect="plain">{{ scriptPhaseLabel(s.phase) }}</el-tag>
        <span class="rp-item-name" @click="editScript(s)">{{ s.name || '(未命名)' }}</span>
        <el-switch v-model="s.enabled" size="small" @change="emitChange" />
        <el-button link size="small" icon="Edit" @click="editScript(s)" />
        <el-button link size="small" icon="Delete" @click="removeScript(i)" />
      </div>
      <div class="rp-item-desc rp-code-desc">{{ (s.code || '').slice(0, 60) }}{{ (s.code || '').length > 60 ? '…' : '' }}</div>
    </div>

    <!-- 规则编辑 -->
    <el-dialog v-model="editOpen" :title="editTitle" width="680px" append-to-body destroy-on-close>
      <el-form label-width="90px" v-if="draft">
        <el-form-item label="规则名称"><el-input v-model="draft.name" maxlength="50" /></el-form-item>
        <el-form-item label="目标字段">
          <el-select v-model="draft.target" filterable placeholder="选择字段" style="width: 100%">
            <el-option v-for="f in allFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
          </el-select>
        </el-form-item>

        <!-- 带出/计算 -->
        <template v-if="draft.type === 'derive'">
          <el-form-item label="计算方式">
            <el-radio-group v-model="deriveMode">
              <el-radio-button value="formula">公式计算</el-radio-button>
              <el-radio-button value="mapping">字段带出</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <template v-if="deriveMode === 'formula'">
            <el-form-item label="公式">
              <el-input v-model="formulaText" placeholder="例：round(quantity * price, 2) 或 add(amount, tax)" />
              <div class="rp-formula-preview" v-if="formulaAst">AST：{{ formulaAstText }}</div>
              <div class="rp-formula-error" v-else-if="formulaError">{{ formulaError }}</div>
            </el-form-item>
            <el-form-item label="触发字段">
              <el-select v-model="draft.trigger" multiple filterable placeholder="留空=任意字段变化都重算" style="width: 100%">
                <el-option v-for="f in mainFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
              </el-select>
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="映射">
              <div class="rp-mapping">
                <div v-for="(m, mi) in draft.mapping" :key="mi" class="rp-mapping-row">
                  <el-select v-model="m.from" filterable placeholder="源字段" style="width: 45%">
                    <el-option v-for="f in mainFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
                  </el-select>
                  <span>→</span>
                  <el-select v-model="m.to" filterable placeholder="目标字段" style="width: 45%">
                    <el-option v-for="f in allFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
                  </el-select>
                  <el-button link icon="Delete" @click="draft.mapping.splice(mi, 1)" />
                </div>
                <el-button size="small" icon="Plus" @click="draft.mapping.push({ from: '', to: '' })">添加映射</el-button>
              </div>
            </el-form-item>
          </template>
        </template>

        <!-- 条件（显隐/只读/必填/校验共用） -->
        <template v-if="draft.type !== 'derive'">
          <el-form-item :label="draft.type === 'validate' ? '生效条件' : '条件'">
            <div class="rp-cond" v-if="draft.when">
              <el-radio-group v-model="draft.when.op" size="small">
                <el-radio-button value="and">且</el-radio-button>
                <el-radio-button value="or">或</el-radio-button>
              </el-radio-group>
              <div v-for="(c, ci) in draft.when.conditions" :key="ci" class="rp-cond-row">
                <el-select v-model="c.field" filterable placeholder="字段" style="width: 32%">
                  <el-option v-for="f in mainFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
                </el-select>
                <el-select v-model="c.cmp" style="width: 26%">
                  <el-option v-for="o in compareOps" :key="o.value" :label="o.label" :value="o.value" />
                </el-select>
                <el-input v-if="!unaryOps.includes(c.cmp)" v-model="c.right" placeholder="比较值（多值用逗号分隔）" style="width: 32%" />
                <el-button link icon="Delete" @click="draft.when.conditions.splice(ci, 1)" />
              </div>
              <el-button size="small" icon="Plus" @click="draft.when.conditions.push({ field: '', cmp: 'eq', right: '' })">添加条件</el-button>
              <div class="rp-cond-tip">不添加条件 = 恒成立</div>
            </div>
          </el-form-item>
        </template>

        <!-- 校验参数 -->
        <template v-if="draft.type === 'validate'">
          <el-form-item label="校验类型">
            <el-select v-model="draft.action.validator" style="width: 100%">
              <el-option v-for="v in validators.filter(x => x.value !== 'unique' && x.value !== 'custom')" :key="v.value" :label="v.label" :value="v.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="draft.action.validator === 'range'" label="范围">
            <el-input v-model="draft.action.min" placeholder="最小值" style="width: 45%" />
            <span style="margin: 0 8px">~</span>
            <el-input v-model="draft.action.max" placeholder="最大值" style="width: 45%" />
          </el-form-item>
          <el-form-item v-if="draft.action.validator === 'regex'" label="正则">
            <el-input v-model="draft.action.pattern" placeholder="例：^1[3-9]\d{9}$" />
          </el-form-item>
          <template v-if="draft.action.validator === 'crossField'">
            <el-form-item label="左字段">
              <el-select v-model="draft.action.left" filterable style="width: 100%">
                <el-option v-for="f in mainFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="比较">
              <el-select v-model="draft.action.cmp" style="width: 100%">
                <el-option v-for="o in compareOps.filter(x => !unaryOps.includes(x.value))" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="右侧">
              <el-select v-model="rightIsField" style="width: 120px">
                <el-option label="固定值" value="value" />
                <el-option label="字段" value="field" />
              </el-select>
              <el-select v-if="rightIsField === 'field'" v-model="draft.action.rightField" filterable style="width: calc(100% - 128px); margin-left: 8px">
                <el-option v-for="f in mainFieldOptions" :key="f.value" :label="f.label" :value="f.value" />
              </el-select>
              <el-input v-else v-model="draft.action.right" style="width: calc(100% - 128px); margin-left: 8px" />
            </el-form-item>
          </template>
          <el-form-item label="提示语"><el-input v-model="draft.message" maxlength="100" /></el-form-item>
        </template>

        <!-- 动态选项：条件满足时替换目标字段选项列表 -->
        <template v-if="draft.type === 'options'">
          <el-form-item label="选项列表">
            <div style="width: 100%">
              <div v-for="(o, oi) in draft.action.options" :key="oi" class="rp-cond-row">
                <el-input v-model="o.label" placeholder="显示文本" style="width: 45%" />
                <el-input v-model="o.value" placeholder="选项值" style="width: 40%" />
                <el-button link type="danger" icon="Delete" @click="draft.action.options.splice(oi, 1)" />
              </div>
              <el-button size="small" icon="Plus" @click="draft.action.options.push({ label: '', value: '' })">添加选项</el-button>
              <div class="rp-cond-tip">条件满足时，目标字段（下拉/单选/复选）的选项整体替换为该列表；条件不满足时还原为字段原始选项</div>
            </div>
          </el-form-item>
        </template>

        <!-- 显隐/只读动作 -->
        <el-form-item v-if="draft.type === 'visible'" label="动作">
          <el-radio-group v-model="draft.action.visible">
            <el-radio :value="true">显示</el-radio>
            <el-radio :value="false">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="draft.type === 'required'" label="提示语"><el-input v-model="draft.message" maxlength="100" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editOpen = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存规则</el-button>
      </template>
    </el-dialog>

    <!-- AI 草稿确认 -->
    <el-dialog v-model="aiOpen" title="AI 生成的规则草稿（请确认后应用）" width="680px" append-to-body>
      <el-checkbox-group v-model="aiChecked">
        <div v-for="(d, i) in aiDrafts" :key="d.id" class="rp-item">
          <div class="rp-item-head">
            <el-checkbox :value="i" />
            <el-tag size="small" :type="typeTag(d.type)">{{ typeLabel(d.type) }}</el-tag>
            <span class="rp-item-name">{{ d.name || '(未命名)' }}</span>
          </div>
          <div class="rp-item-desc">{{ ruleSummary(d) }}</div>
          <div class="rp-item-desc" v-if="d.message">提示：{{ d.message }}</div>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="aiOpen = false">取消</el-button>
        <el-button type="primary" @click="applyAi">应用选中的规则</el-button>
      </template>
    </el-dialog>

    <!-- 高级脚本编辑 -->
    <el-dialog v-model="sOpen" :title="sDraft && sDraft.id ? '编辑脚本' : '新建脚本'" width="720px" append-to-body destroy-on-close>
      <el-form label-width="90px" v-if="sDraft">
        <el-form-item label="脚本名称"><el-input v-model="sDraft.name" maxlength="50" placeholder="例：跨字段联动赋值" /></el-form-item>
        <el-form-item label="执行时机">
          <el-radio-group v-model="sDraft.phase">
            <el-radio value="load">打开表单时</el-radio>
            <el-radio value="change">字段变化时</el-radio>
            <el-radio value="submit">提交前</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="脚本代码">
          <el-input v-model="sDraft.code" type="textarea" :rows="12" class="rp-code-input" spellcheck="false" placeholder="例：&#10;const a = ctx.get('amount');&#10;const b = ctx.get('amount2');&#10;if (a != null && b != null && a + b > 100000) ctx.fail('两项合计不得超过 10 万元');&#10;else ctx.set('remark', '已自动复核');" />
        </el-form-item>
        <div class="rp-tip">
          可用 API：ctx.get(字段) 读取主表字段值；ctx.set(字段, 值) 写回字段（change 时机写回后会再触发一轮交互规则）；ctx.detailData 明细数据（{ 表名: [行] }）；ctx.rows(表名) 取明细行数组；ctx.fail(原因) 拦截提交（仅提交前时机）。
          脚本仅发起/办理（可编辑）节点执行，审批只读节点不会运行。保存时后端校验大小（≤64KB）。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="sOpen = false">取消</el-button>
        <el-button type="primary" @click="saveScript">保存脚本</el-button>
      </template>
    </el-dialog>

    <!-- 沙箱 -->
    <div class="rp-sandbox-bar">
      <el-button size="small" icon="VideoPlay" @click="openSandbox">测试规则</el-button>
    </div>
    <el-dialog v-model="sandboxOpen" title="规则沙箱" width="640px" append-to-body>
      <div class="rp-sb-tip">用于在不发起真实流程的情况下模拟验证已配置的交互规则：给下方字段填入模拟值后点击「运行」，即可预览计算结果、字段显隐/必填/只读状态、校验是否通过，以及每条规则的命中链路。模拟不会保存任何数据。</div>
      <div class="rp-sb-sec" v-if="sandboxFields.length">一、字段模拟值（填入测试数据，留空视为未填写）</div>
      <div class="rp-sb-fields" v-if="sandboxFields.length">
        <div v-for="f in sandboxFields" :key="f" class="rp-sb-row">
          <span class="rp-sb-label">{{ fieldLabelOf(f) }}</span>
          <el-input v-model="sandboxValues[f]" size="small" style="width: 220px" placeholder="输入该字段的模拟值" />
        </div>
      </div>
      <div v-else class="rp-empty">当前规则没有引用任何输入字段，直接点击「运行」查看结果。</div>
      <el-divider />
      <div class="rp-sb-sec" v-if="sandboxResult">二、模拟运行结果</div>
      <template v-if="sandboxResult">
        <div class="rp-sb-line dim">以下为按上述模拟值执行全部规则后的预览：</div>
        <div class="rp-sb-sec-sub">计算结果</div>
        <div v-for="(v, k) in sandboxResult.values" :key="k" class="rp-sb-line">{{ fieldLabelOf(k) }} = <b>{{ v }}</b></div>
        <div class="rp-sb-sec-sub">字段状态</div>
        <div v-for="(s, k) in sandboxResult.states" :key="k" class="rp-sb-line">{{ fieldLabelOf(k) }} → {{ stateText(s) }}</div>
        <div class="rp-sb-sec-sub">校验结果</div>
        <div v-if="!sandboxResult.errors.length" class="rp-sb-line ok">✔ 通过</div>
        <div v-for="(e, i) in sandboxResult.errors" :key="i" class="rp-sb-line bad">✘ {{ fieldLabelOf(e.field) }} {{ e.message }}</div>
        <div class="rp-sb-sec-sub">命中链路</div>
        <div v-for="(t, i) in sandboxResult.trace" :key="i" class="rp-sb-line dim">{{ traceText(t) }}</div>
      </template>
      <template #footer>
        <el-button @click="sandboxOpen = false">关闭</el-button>
        <el-button type="primary" @click="runSandbox">运行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { RULE_TYPES, COMPARE_OPS, UNARY_OPS, VALIDATORS, parseExt, stringifyExt, createRule as newRuleOf, emptyExt } from './constants';
import { parseFormula, formatFormula } from './formula';
import { runRules, condFields } from './engine';
import { aiFormRules } from '@/api/flowable/form';

/** UI 扁平条件 → 存储嵌套格式（与后端 FlowFormRuleService / AI 提示词一致） */
function whenToStore(ui) {
  if (!ui || !ui.conditions || !ui.conditions.length) return null;
  return {
    op: ui.op === 'or' ? 'or' : 'and',
    conditions: ui.conditions
      .filter((c) => c.field)
      .map((c) => ({
        left: { field: c.field },
        cmp: c.cmp,
        right: unaryOps.includes(c.cmp) ? undefined : { value: c.right },
      })),
  };
}
/** 存储嵌套条件 → UI 扁平（编辑用） */
function whenToUi(stored) {
  if (!stored || !Array.isArray(stored.conditions)) return { op: 'and', conditions: [] };
  return {
    op: stored.op === 'or' ? 'or' : 'and',
    conditions: stored.conditions.map((c) => {
      const left = c.left && typeof c.left === 'object' ? c.left.field : c.field;
      const right = c.right && typeof c.right === 'object' && !Array.isArray(c.right) && Object.prototype.hasOwnProperty.call(c.right, 'value') ? c.right.value : c.right;
      return { field: left, cmp: c.cmp || 'eq', right: right === undefined ? '' : right };
    }),
  };
}

const props = defineProps({
  fields: { type: Array, default: () => [] },
  modelValue: { type: String, default: '' },
  formId: { type: [Number, String], default: null },
});
const emit = defineEmits(['update:modelValue']);

const ruleTypes = RULE_TYPES.filter((t) => t.value !== 'script');
const compareOps = COMPARE_OPS;
const unaryOps = UNARY_OPS;
const validators = VALIDATORS;

const ext = ref(emptyExt());
const rules = computed(() => ext.value.rules);

watch(
  () => props.modelValue,
  (v) => {
    ext.value = parseExt(v);
  },
  { immediate: true }
);

function emitChange() {
  emit('update:modelValue', stringifyExt(ext.value));
}

const mainFieldOptions = computed(() =>
  (props.fields || [])
    .filter((f) => !f.sysField && (!f.table || f.table === 'main'))
    .map((f) => ({ value: f.field, label: f.label + '（' + f.field + '）' }))
);
const detailFieldOptions = computed(() =>
  (props.fields || [])
    .filter((f) => !f.sysField && f.table && f.table !== 'main')
    .map((f) => ({ value: f.table + '.' + f.field, label: f.table + '.' + f.label + '（' + f.field + '）' }))
);
const allFieldOptions = computed(() => [...mainFieldOptions.value, ...detailFieldOptions.value]);

function fieldLabelOf(ref_) {
  if (!ref_) return ref_;
  const [table, field] = ref_.includes('.') ? ref_.split('.') : ['main', ref_];
  const f = (props.fields || []).find((x) => x.field === field && (x.table || 'main') === table);
  return f ? f.label : ref_;
}

const typeLabel = (t) => (RULE_TYPES.find((x) => x.value === t) || {}).label || t;
const typeTag = (t) => ({ derive: 'warning', required: 'danger', visible: 'primary', readonly: 'info', validate: 'danger', options: 'success' }[t] || 'info');

function ruleSummary(r) {
  if (r.type === 'derive') {
    if (r.mapping && r.mapping.length) return r.mapping.map((m) => fieldLabelOf(m.from) + ' → ' + fieldLabelOf(m.to)).join('；');
    return formatFormula(r.expr);
  }
  const ui = whenToUi(r.when);
  const cond = ui.conditions && ui.conditions.length
    ? '当 ' + ui.conditions.map((c) => fieldLabelOf(c.field) + ' ' + (COMPARE_OPS.find((o) => o.value === c.cmp) || {}).label + ' ' + c.right).join(ui.op === 'or' ? ' 或 ' : ' 且 ')
    : '恒成立';
  if (r.type === 'required') return cond + ' → 必填';
  if (r.type === 'visible') return cond + ' → ' + (r.action && r.action.visible ? '显示' : '隐藏');
  if (r.type === 'readonly') return cond + ' → ' + (r.action && r.action.readonly ? '只读' : '可编辑');
  if (r.type === 'validate') return cond + ' → ' + ((VALIDATORS.find((v) => v.value === (r.action && r.action.validator)) || {}).label || '校验');
  if (r.type === 'options') {
    const opts = (r.action && r.action.options) || [];
    return cond + ' → 选项替换为：' + (opts.length ? opts.map((o) => o.label).join('/') : '(未配置)');
  }
  return cond;
}

function createRule(type) {
  draft.value = newRuleOf(type);
  draft.value.when = { op: 'and', conditions: [] };
  editOpen.value = true;
}

const editOpen = ref(false);
const draft = ref(null);
const editTitle = computed(() => (draft.value ? '编辑规则：' + typeLabel(draft.value.type) : ''));

const deriveMode = ref('formula');
const formulaText = ref('');
const formulaAst = computed(() => (deriveMode.value === 'formula' && formulaText.value ? parseFormula(formulaText.value).ast : null));
const formulaAstText = computed(() => (formulaAst.value ? formatFormula(formulaAst.value) : ''));
const formulaError = computed(() => (deriveMode.value === 'formula' && formulaText.value ? parseFormula(formulaText.value).error : ''));

watch(editOpen, (open) => {
  if (open && draft.value) {
    deriveMode.value = draft.value.mapping && draft.value.mapping.length ? 'mapping' : 'formula';
    formulaText.value = draft.value.expr ? formatFormula(draft.value.expr) : '';
  }
});

const rightIsField = ref('field');
watch(rightIsField, (v) => {
  if (v === 'field' && draft.value) draft.value.action.rightField = draft.value.action.rightField || '';
});

function editRule(r) {
  draft.value = JSON.parse(JSON.stringify(r));
  if (draft.value.type === 'derive' && !draft.value.mapping) draft.value.mapping = [];
  if ((draft.value.type === 'visible' || draft.value.type === 'readonly') && !draft.value.action) {
    draft.value.action = { [draft.value.type]: draft.value.type === 'visible' ? true : true };
  }
  if (draft.value.type === 'validate' && !draft.value.action) draft.value.action = { validator: 'required' };
  if (draft.value.type === 'options' && !draft.value.action) draft.value.action = { options: [] };
  if (draft.value.type === 'options' && !Array.isArray(draft.value.action.options)) draft.value.action.options = [];
  // 跨字段校验：存储嵌套格式 → 编辑扁平
  if (draft.value.type === 'validate' && draft.value.action.validator === 'crossField') {
    const act = draft.value.action;
    act.left = act.left && typeof act.left === 'object' ? act.left.field : act.left;
    if (act.right && typeof act.right === 'object' && !Array.isArray(act.right)) {
      if (Object.prototype.hasOwnProperty.call(act.right, 'field')) { rightIsField.value = 'field'; act.rightField = act.right.field; act.right = ''; }
      else { rightIsField.value = 'value'; act.right = act.right.value; }
    } else if (!act.rightField) {
      rightIsField.value = 'value';
    }
  }
  draft.value.when = whenToUi(draft.value.when);
  editOpen.value = true;
}

function saveRule() {
  const d = draft.value;
  if (!d.target) return ElMessageWarning('请选择目标字段');
  if (d.type === 'derive') {
    if (deriveMode.value === 'formula') {
      if (!formulaAst.value) return ElMessageWarning(formulaError.value || '请输入有效公式');
      d.expr = formulaAst.value;
      d.mapping = [];
    } else {
      if (!d.mapping.length) return ElMessageWarning('请至少添加一条映射');
      d.expr = null;
    }
  }
  if (d.type === 'validate' && !d.action.validator) return ElMessageWarning('请选择校验类型');
  if (d.type === 'options') {
    const opts = (d.action.options || []).filter((o) => o.label !== '' && o.value !== '');
    if (!opts.length) return ElMessageWarning('请至少配置一条完整的选项（显示文本与选项值都要填写）');
    d.action.options = opts;
  }
  // 条件转存储格式（嵌套，与后端/AI 一致）
  d.when = whenToStore(d.when);
  // 跨字段校验转存储格式
  if (d.type === 'validate' && d.action.validator === 'crossField') {
    d.action = {
      validator: 'crossField',
      cmp: d.action.cmp || 'eq',
      left: { field: d.action.left },
      right: rightIsField.value === 'field' ? { field: d.action.rightField } : { value: d.action.right },
    };
  }
  const idx = ext.value.rules.findIndex((r) => r.id === d.id);
  if (idx >= 0) ext.value.rules.splice(idx, 1, d);
  else ext.value.rules.push(d);
  emitChange();
  editOpen.value = false;
}
function ElMessageWarning(msg) {
  import('element-plus').then(({ ElMessage }) => ElMessage.warning(msg));
}
function ElMessageError(msg) {
  import('element-plus').then(({ ElMessage }) => ElMessage.error(msg));
}

function removeRule(i) {
  ext.value.rules.splice(i, 1);
  emitChange();
}

// ===== 高级脚本管理（ext_json.scripts：[{ id, name, phase, code, enabled }]）=====
const scripts = computed(() => ext.value.scripts || []);
const sOpen = ref(false);
const sDraft = ref(null);

function scriptPhaseLabel(phase) {
  return { load: '打开表单', change: '字段变化', submit: '提交前' }[phase || 'change'] || '字段变化';
}

function editScript(s) {
  sDraft.value = s
    ? JSON.parse(JSON.stringify(s))
    : { id: 's_' + Math.random().toString(36).slice(2, 8) + Date.now().toString(36).slice(-3), name: '', phase: 'change', code: '', enabled: true };
  sOpen.value = true;
}

function saveScript() {
  const d = sDraft.value;
  if (!d.name || !d.name.trim()) return ElMessageWarning('请填写脚本名称');
  if (!d.code || !d.code.trim()) return ElMessageWarning('请填写脚本代码');
  if (d.code.length > 64 * 1024) return ElMessageWarning('脚本代码不能超过 64KB');
  d.name = d.name.trim();
  const idx = ext.value.scripts.findIndex((s) => s.id === d.id);
  if (idx >= 0) ext.value.scripts.splice(idx, 1, d);
  else ext.value.scripts.push(d);
  emitChange();
  sOpen.value = false;
}

function removeScript(i) {
  ext.value.scripts.splice(i, 1);
  emitChange();
}

// ===== AI 一句话生成 =====
const aiText = ref('');
const aiLoading = ref(false);
const aiDrafts = ref([]);
const aiChecked = ref([]);
const aiOpen = ref(false);

async function generateAi() {
  if (!aiText.value.trim()) return ElMessageWarning('请先描述你的规则需求');
  if (!props.formId) return ElMessageWarning('请先保存表单后再使用 AI 生成');
  aiLoading.value = true;
  try {
    const res = await aiFormRules({ formId: props.formId, requirement: aiText.value.trim() });
    const rulesIn = (res.data && res.data.rules) || [];
    if (!rulesIn.length) return ElMessageWarning('AI 没有生成可用规则，请换一种描述（后端已做字段与依赖校验）');
    aiDrafts.value = rulesIn.map((r) => ({ ...r, id: r.id || newRuleId(), enabled: true }));
    aiChecked.value = aiDrafts.value.map((_, i) => i);
    aiOpen.value = true;
  } catch (e) {
    ElMessageError(e?.message || 'AI 生成失败');
  } finally {
    aiLoading.value = false;
  }
}
function newRuleId() {
  return 'r_' + Math.random().toString(36).slice(2, 8) + Date.now().toString(36).slice(-3);
}
function applyAi() {
  const picked = aiDrafts.value.filter((_, i) => aiChecked.value.includes(i));
  if (!picked.length) return;
  // 兜底：应用时若草稿 id 与已有规则冲突，重新生成，避免保存被后端"规则 id 重复"拦截
  const existIds = new Set(ext.value.rules.map(r => r.id));
  picked.forEach((p) => { if (existIds.has(p.id)) p.id = newRuleId(); });
  ext.value.rules.push(...JSON.parse(JSON.stringify(picked)));
  emitChange();
  aiOpen.value = false;
  aiText.value = '';
}

// ===== 沙箱 =====
const sandboxOpen = ref(false);
const sandboxValues = ref({});
const sandboxResult = ref(null);
const sandboxFields = computed(() => {
  const set = new Set();
  for (const r of rules.value) {
    (r.trigger || []).forEach((f) => set.add(f));
    condFields(r.when).forEach((f) => set.add(f));
    if (r.expr) collectExprFields(r.expr, set);
    if (r.mapping && r.mapping.length) set.add(r.mapping[0].from);
  }
  return [...set];
});
function collectExprFields(node, out) {
  if (!node || typeof node !== 'object') return;
  if (Object.prototype.hasOwnProperty.call(node, 'field')) out.add(node.field);
  if (Array.isArray(node.args)) node.args.forEach((a) => collectExprFields(a, out));
}
function openSandbox() {
  sandboxValues.value = {};
  sandboxResult.value = null;
  sandboxOpen.value = true;
}
function runSandbox() {
  const detailData = {};
  for (const f of props.fields || []) {
    if (f.table && f.table !== 'main' && !detailData[f.table]) detailData[f.table] = [];
  }
  sandboxResult.value = runRules(rules.value, props.fields || [], { ...sandboxValues.value }, detailData, {});
}
function stateText(s) {
  const parts = [];
  if (s.required) parts.push('必填');
  if (s.visible === false) parts.push('隐藏');
  if (s.visible === true) parts.push('显示');
  if (s.readonly === true) parts.push('只读');
  if (s.readonly === false) parts.push('可编辑');
  return parts.join('、') || '无变化';
}
function traceText(t) {
  if (t.error) return '规则 ' + t.rule + ' 执行出错：' + t.error;
  if (t.value !== undefined) return fieldLabelOf(t.target) + ' = ' + t.value + '（规则 ' + t.rule + '）';
  if (t.state) return fieldLabelOf(t.target) + ' → ' + t.state + '（规则 ' + t.rule + '）';
  if (t.error !== undefined) return fieldLabelOf(t.target) + (t.error ? ' 校验未通过' : ' 校验通过') + '（规则 ' + t.rule + '）';
  return JSON.stringify(t);
}
</script>

<style lang="scss" scoped>
.rule-panel { padding: 12px; }
.rp-head { display: flex; align-items: center; justify-content: space-between; }
.rp-title { font-weight: 700; b { color: var(--brand-600, #4f46e5); } }
.rp-tip { font-size: 12px; color: #94a3b8; margin: 6px 0 10px; }
.rp-empty { font-size: 13px; color: #94a3b8; padding: 24px 0; text-align: center; }
.rp-item { border: 1px solid #e2e8f0; border-radius: 8px; padding: 8px 10px; margin-bottom: 8px; line-height: 1.5; font-size: 13px; &.is-off { opacity: 0.55; } }
.rp-item-head { display: flex; align-items: center; gap: 8px; }
.rp-item-name { flex: 1; font-size: 13px; cursor: pointer; &:hover { color: var(--brand-600, #4f46e5); } }
.rp-item-desc { font-size: 12px; color: #64748b; margin-top: 4px; }
.rp-cond-row { display: flex; align-items: center; gap: 6px; margin: 6px 0; }
.rp-cond-tip { font-size: 12px; color: #94a3b8; }
.rp-mapping-row { display: flex; align-items: center; gap: 6px; margin: 6px 0; }
.rp-formula-preview { font-size: 12px; color: #64748b; margin-top: 4px; }
.rp-formula-error { font-size: 12px; color: #ef4444; margin-top: 4px; }
.rp-sandbox-bar { margin-top: 12px; }
.rp-scripts-head { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; padding-top: 10px; border-top: 1px dashed #e2e8f0; }
.rp-scripts-title { font-weight: 600; font-size: 13px; color: #475569; }
.rp-code-desc { font-family: Consolas, Menlo, monospace; white-space: pre-wrap; word-break: break-all; }
.rp-code-input :deep(.el-textarea__inner) { font-family: Consolas, Menlo, monospace; font-size: 12px; line-height: 1.5; }
.rp-ai { display: flex; gap: 8px; margin-bottom: 6px; }
.rp-sb-fields { display: flex; flex-wrap: wrap; gap: 8px 16px; }
.rp-sb-tip { font-size: 12px; line-height: 1.6; color: #64748b; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 8px 10px; margin-bottom: 10px; }
.rp-sb-sec-sub { font-weight: 600; font-size: 12px; color: #475569; margin: 8px 0 4px; }
.rp-sb-row { display: flex; align-items: center; gap: 8px; }
.rp-sb-label { font-size: 13px; min-width: 90px; }
.rp-sb-sec { font-weight: 700; font-size: 13px; margin: 8px 0 4px; }
.rp-sb-line { font-size: 13px; color: #334155; &.ok { color: #16a34a; } &.bad { color: #ef4444; } &.dim { color: #94a3b8; } }
</style>
