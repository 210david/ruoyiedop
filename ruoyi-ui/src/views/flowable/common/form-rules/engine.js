/**
 * 表单规则引擎（运行态）
 *
 * 输入：规则列表（form-rules/constants.js 的结构）+ 字段定义 + 主表值 + 明细行数据
 * 输出：{ values, states, errors, trace }
 *  - values  计算/带出后的主表字段值补丁
 *  - states  字段状态覆盖（required / visible / readonly）
 *  - errors  校验错误 [{ field, message }]
 *  - trace   命中链路（沙箱展示）
 *
 * 明细行（perRow）规则在这里不逐行求值：明细行住在 FormGridRender 内部，
 * 由组件通过 applyDetailRules 按 values.detailData 求值。
 */

import { parseFormula } from './formula';
import { toRmbUpper } from '@/utils/rmb';

const MAX_ITER = 10;

/** 数字化：空值不参与计算，纯数字字符串转数值 */
function numOf(v) {
  if (v === undefined || v === null || v === '') return NaN;
  if (typeof v === 'number') return v;
  const n = Number(String(v).replace(/,/g, ''));
  return isNaN(n) ? NaN : n;
}
function isBlank(v) {
  if (v === undefined || v === null) return true;
  if (typeof v === 'string') return v.trim() === '';
  if (Array.isArray(v)) return v.length === 0;
  return false;
}

/** AST 求值：{ field } / { value } / { fn, args } */
export function evalNode(node, ctx) {
  if (node == null) return undefined;
  if (Object.prototype.hasOwnProperty.call(node, 'value')) return node.value;
  if (Object.prototype.hasOwnProperty.call(node, 'field')) return ctx.get(node.field);
  const fn = FUNCS[node.fn];
  if (!fn) return undefined;
  const args = (node.args || []).map((a) => evalNode(a, ctx));
  try {
    return fn(...args);
  } catch (e) {
    return undefined;
  }
}

/** 表达式函数白名单（与后端 FlowFormRuleService 一致） */
const FUNCS = {
  add: (a, b) => numOf(a) + numOf(b),
  sub: (a, b) => numOf(a) - numOf(b),
  mul: (a, b) => numOf(a) * numOf(b),
  div: (a, b) => (numOf(b) === 0 ? undefined : numOf(a) / numOf(b)),
  mod: (a, b) => (numOf(b) === 0 ? undefined : numOf(a) % numOf(b)),
  round: (v, d) => {
    const n = numOf(v);
    const p = d === undefined ? 2 : numOf(d);
    return isNaN(n) ? undefined : Math.round(n * Math.pow(10, p)) / Math.pow(10, p);
  },
  floor: (v) => Math.floor(numOf(v)),
  ceil: (v) => Math.ceil(numOf(v)),
  abs: (v) => Math.abs(numOf(v)),
  min: (...xs) => Math.min(...xs.map(numOf)),
  max: (...xs) => Math.max(...xs.map(numOf)),
  if: (c, a, b) => (c ? a : b),
  eq: (a, b) => String(a ?? '') === String(b ?? ''),
  ne: (a, b) => String(a ?? '') !== String(b ?? ''),
  and: (...xs) => xs.every((x) => !!x),
  or: (...xs) => xs.some((x) => !!x),
  not: (v) => !v,
  concat: (...xs) => xs.map((x) => (x === undefined || x === null ? '' : String(x))).join(''),
  len: (v) => (v === undefined || v === null ? 0 : String(v).length),
  upper: (v) => (v === undefined || v === null ? '' : String(v).toUpperCase()),
  lower: (v) => (v === undefined || v === null ? '' : String(v).toLowerCase()),
  today: () => {
    const d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  },
  now: () => {
    const d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0') + ' ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
  },
  dateDiff: (a, b) => {
    const da = new Date(String(a).replace(' ', 'T'));
    const db = new Date(String(b).replace(' ', 'T'));
    if (isNaN(da) || isNaN(db)) return undefined;
    return Math.round((db - da) / 86400000);
  },
  dateAdd: (d, n) => {
    const dt = new Date(String(d).replace(' ', 'T'));
    if (isNaN(dt) || isNaN(numOf(n))) return undefined;
    dt.setDate(dt.getDate() + numOf(n));
    return dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
  },
  /** 相差小时（加班时长等；支持 yyyy-MM-dd HH:mm，纯日期按 00:00 计）；与后端 hoursDiff 口径一致 */
  hoursDiff: (a, b) => {
    const ta = new Date(String(a).replace(' ', 'T'));
    const tb = new Date(String(b).replace(' ', 'T'));
    if (isNaN(ta) || isNaN(tb)) return undefined;
    return Math.round(((tb - ta) / 3600000) * 100) / 100;
  },
  /** 工作日天数（含首尾，跳过休息日/法定节假日、含调休补班；用于请假天数）。
   *  日历数据由 workday.js 预加载（setWorkdayMap），区间内无日历数据的日期按「剔除周六日」回退；
   *  与后端 FlowFormRuleService.workDays 口径一致 */
  workDays: (a, b) => {
    const da = new Date(String(a).replace(' ', 'T'));
    const db = new Date(String(b).replace(' ', 'T'));
    if (isNaN(da) || isNaN(db)) return 0;
    if (db < da) return 0;
    da.setHours(0, 0, 0, 0);
    db.setHours(0, 0, 0, 0);
    let n = 0;
    for (const d = new Date(da); d <= db; d.setDate(d.getDate() + 1)) {
      const key = d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
      const t = workdayMap[key];
      if (t === '0') n++;
      else if (t !== '1' && t !== '2' && d.getDay() !== 0 && d.getDay() !== 6) n++; // 无日历数据回退：剔除周六日
    }
    return n;
  },
  sum: (...xs) => xs.reduce((s, x) => s + (isNaN(numOf(x)) ? 0 : numOf(x)), 0),
  /** 金额 → 人民币中文大写（合计大写自动生成；后端不执行该函数，derive 结果由前端算出后随表单数据提交） */
  rmbUpper: (v) => (v === undefined || v === null || v === '' ? '' : toRmbUpper(numOf(v))),
  avg: (...xs) => {
    const ns = xs.map(numOf).filter((x) => !isNaN(x));
    return ns.length ? ns.reduce((s, x) => s + x, 0) / ns.length : undefined;
  },
  detailSum: (table, field) => {
    const rows = (ctx.detailData && ctx.detailData[table]) || [];
    return rows.reduce((s, r) => s + (isNaN(numOf(r[field])) ? 0 : numOf(r[field])), 0);
  },
  detailCount: (table) => ((ctx.detailData && ctx.detailData[table]) || []).length,
};
/** evalNode 内部使用的 ctx 引用（detailSum/detailCount 需要明细数据） */
let ctx = { detailData: {} };

/** 企业工作日历 { 'yyyy-MM-dd': '0|1|2' }（0=工作日 1=休息日 2=法定节假日），由 workday.js 预加载 */
let workdayMap = {};
/** 合并注入日历数据（重叠区间累积），供 workDays 使用 */
export function setWorkdayMap(map) {
  if (!map || typeof map !== 'object') return;
  workdayMap = { ...workdayMap, ...map };
}

/** 条件比较（与后端 FlowFormRuleService.compare 一致） */
export function compare(op, left, right) {
  if (op === 'isEmpty') return isBlank(left);
  if (op === 'isNotEmpty') return !isBlank(left);
  if (isBlank(left)) return false;
  if (op === 'in') return (Array.isArray(right) ? right : [right]).some((x) => String(x) === String(left));
  if (op === 'notIn') return !(Array.isArray(right) ? right : [right]).some((x) => String(x) === String(left));
  if (op === 'between') {
    const n = numOf(left);
    const a = numOf(Array.isArray(right) ? right[0] : right);
    const b = numOf(Array.isArray(right) ? right[1] : right);
    return !isNaN(n) && !isNaN(a) && !isNaN(b) && n >= Math.min(a, b) && n <= Math.max(a, b);
  }
  if (op === 'regex') {
    try {
      return new RegExp(right).test(String(left));
    } catch (e) {
      return false;
    }
  }
  const ln = numOf(left);
  const rn = numOf(right);
  const numeric = !isNaN(ln) && !isNaN(rn);
  switch (op) {
    case 'eq': return numeric ? ln === rn : String(left) === String(right);
    case 'ne': return numeric ? ln !== rn : String(left) !== String(right);
    case 'gt': return numeric ? ln > rn : String(left) > String(right);
    case 'gte': return numeric ? ln >= rn : String(left) >= String(right);
    case 'lt': return numeric ? ln < rn : String(left) < String(right);
    case 'lte': return numeric ? ln <= rn : String(left) <= String(right);
    case 'contains': return String(left).includes(String(right));
    case 'notContains': return !String(left).includes(String(right));
    default: return false;
  }
}

/** 条件归一化：兼容两种格式
 *  扁平（面板内部）：{ field, cmp, right }
 *  嵌套（后端/AI 存储）：{ left: { field }, cmp, right: { value } | { field } }
 *  统一成 { field, cmp, rightVal | rightRef } 求值用结构 */
function normCond(c) {
  if (!c) return null;
  const left = c.left && typeof c.left === 'object' ? c.left : null;
  const field = c.field || (left && left.field);
  const rawRight = c.right;
  let rightVal;
  let rightRef;
  if (rawRight && typeof rawRight === 'object' && !Array.isArray(rawRight)) {
    if (Object.prototype.hasOwnProperty.call(rawRight, 'value')) rightVal = rawRight.value;
    else if (Object.prototype.hasOwnProperty.call(rawRight, 'field')) rightRef = rawRight.field;
    else rightVal = rawRight;
  } else {
    rightVal = rawRight;
  }
  return { field, cmp: c.cmp, rightVal, rightRef };
}

/** 条件组求值：{ op: 'and'|'or', conditions: [...] }；values 可为对象或 getter */
export function evalCond(when, values, detailData) {
  if (!when || !when.conditions || !when.conditions.length) return true;
  const get = typeof values === 'function' ? values : (f) => values[f];
  const op = when.op === 'or' ? 'or' : 'and';
  const results = when.conditions.map((c) => {
    const n = normCond(c);
    if (!n || !n.field) return true;
    const left = get(n.field);
    const right = n.rightRef !== undefined ? get(n.rightRef) : n.rightVal;
    return compare(n.cmp, left, right);
  });
  return op === 'or' ? results.some(Boolean) : results.every(Boolean);
}

/** 从条件组收集引用字段（兼容两种格式） */
export function condFields(when) {
  const out = [];
  if (!when || !Array.isArray(when.conditions)) return out;
  for (const c of when.conditions) {
    const n = normCond(c);
    if (n && n.field) out.push(n.field);
    if (n && n.rightRef) out.push(n.rightRef);
  }
  return out;
}

/** 规则读取目标字段的主表值（优先取引擎已算出的补丁值） */
function readField(rule, patched, values) {
  const key = rule.target;
  if (patched[key] !== undefined) return patched[key];
  return values[key];
}

/**
 * 运行规则引擎。
 * @param {Array} rules 规则列表
 * @param {Array} fields 字段定义 [{field, label, rtype, dtype, table}]
 * @param {Object} values 主表值
 * @param {Object} detailData 明细行 { dt1: [rows] }
 * @param {Object} opts { changedField: '触发字段，仅重算关联规则' }
 * @returns {{ values, states, errors, trace }}
 */
export function runRules(rules, fields, values, detailData, opts) {
  const optsIn = opts || {};
  const detail = {};
  for (const f of fields || []) if (f.table && f.table !== 'main') detail[f.table] = detail[f.table] || [];
  ctx = { detailData: detailData || detail };

  const rulesList = (rules || []).filter((r) => r && r.enabled !== false);
  const patched = {};
  const states = {};
  const errors = [];
  const trace = [];

  // 拓扑序：derive 依赖其他 derive 时按依赖顺序求值（成环的丢弃并告警）
  const ordered = topologicalSort(rulesList);

  for (const rule of ordered) {
    const ctxFor = (field) => ({
      get: (f) => {
        if (patched[f] !== undefined) return patched[f];
        if (f === '__detail') return detailData;
        return values[f];
      },
      detailData: detailData || {},
    });
    const ctxIn = ctxFor(null);
    try {
      if (rule.type === 'derive') {
        const target = rule.target;
        if (!target) continue;
        // 字段带出（mapping）：逐条把源字段值映射到目标字段
        if (rule.mapping && rule.mapping.length) {
          for (const m of rule.mapping) {
            if (!m || !m.to) continue;
            const v = values[m.from];
            if (v !== undefined && v !== null && v !== '') patched[m.to] = v;
          }
          trace.push({ rule: rule.id, target, mapping: true });
          continue;
        }
        if (!rule.expr) continue;
        normalizeDetailAgg(rule, fields);
        // 触发字段限定：配置了 trigger 时仅触发字段变化才重算。
        // changedField 支持数组：明细变化场景传 [表名, 字段名]，trigger 任一命中即重算
        const cf = optsIn.changedField;
        const cfList = Array.isArray(cf) ? cf : cf ? [cf] : [];
        if (rule.trigger && rule.trigger.length && cfList.length && !rule.trigger.some((t) => cfList.includes(t))) continue;
        const v = evalNode(rule.expr, { get: (f) => (patched[f] !== undefined ? patched[f] : values[f]), detailData: detailData || {} });
        if (v !== undefined) patched[target] = v;
        trace.push({ rule: rule.id, target, value: v });
      } else if (rule.type === 'visible' || rule.type === 'readonly') {
        if (!rule.target) continue;
        if (rule.when && !evalCond(rule.when, buildGetter(values, patched), detailData)) continue;
        const act = rule.action || {};
        if (rule.type === 'visible') states[rule.target] = { ...(states[rule.target] || {}), visible: act.visible !== false };
        else states[rule.target] = { ...(states[rule.target] || {}), readonly: act.readonly !== false };
        trace.push({ rule: rule.id, target: rule.target, state: rule.type });
      } else if (rule.type === 'options') {
        // 动态选项：条件满足时替换目标下拉/单选/复选字段的选项列表；
        // 条件不满足时清除本规则已有覆盖，让选项还原为字段定义的原始列表
        if (!rule.target) continue;
        const clearOpts = () => {
          const cur = states[rule.target];
          if (cur && cur.options) {
            const rest = { ...cur };
            delete rest.options;
            states[rule.target] = rest;
          }
        };
        if (rule.when && !evalCond(rule.when, buildGetter(values, patched), detailData)) {
          clearOpts();
          continue;
        }
        const act = rule.action || {};
        const opts = Array.isArray(act.options)
          ? act.options.filter((o) => o && o.value !== undefined).map((o) => ({ label: o.label !== undefined ? o.label : String(o.value), value: o.value }))
          : [];
        if (opts.length) {
          states[rule.target] = { ...(states[rule.target] || {}), options: opts };
        } else {
          clearOpts();
        }
        trace.push({ rule: rule.id, target: rule.target, state: 'options', count: opts.length });
      } else if (rule.type === 'required') {
        if (!rule.target) continue;
        const getOv = buildGetter(values, patched);
        if (rule.when && !evalCond(rule.when, getOv, detailData)) continue;
        states[rule.target] = { ...(states[rule.target] || {}), required: true };
        trace.push({ rule: rule.id, target: rule.target, state: 'required' });
        // 提交校验模式：必填规则同样拦截空值（防止绕过前端直接调接口）
        if (optsIn.validate && isBlank(getOv(rule.target))) {
          errors.push({ field: rule.target, message: rule.message || '不能为空', rule: rule.id });
        }
      } else if (rule.type === 'validate') {
        if (!rule.target) continue;
        // 明细字段校验：按行求值（行值优先、主表字段兜底），错误带表名/行号供组件行内标红
        const dfd = (fields || []).find((f) => f && f.field === rule.target && f.table && f.table !== 'main');
        if (dfd) {
          const rows = (detailData && detailData[dfd.table]) || [];
          let bad = 0;
          rows.forEach((row, ri) => {
            const err = validateOne(rule, { ...values, ...row }, {}, detailData);
            if (err) {
              bad++;
              errors.push({ ...err, table: dfd.table, rowIndex: ri });
            }
          });
          trace.push({ rule: rule.id, target: rule.target, detail: dfd.table, error: bad > 0 });
          continue;
        }
        if (rule.when && !evalCond(rule.when, buildGetter(values, patched), detailData)) continue;
        const err = validateOne(rule, values, patched, detailData);
        if (err) errors.push(err);
        trace.push({ rule: rule.id, target: rule.target, error: !!err });
      }
    } catch (e) {
      trace.push({ rule: rule.id, error: String(e && e.message || e) });
    }
  }
  return { values: patched, states, errors, trace };
}

function buildGetter(values, patched) {
  return (f) => (patched[f] !== undefined ? patched[f] : values[f]);
}

/** 拓扑排序：derive 依赖其他 derive 时按依赖顺序求值，成环的规则丢弃并告警 */
function topologicalSort(rules) {
  const derives = rules.filter((r) => r.type === 'derive' && r.target);
  const others = rules.filter((r) => !(r.type === 'derive' && r.target));
  const deps = new Map();
  for (const r of derives) {
    const read = new Set();
    collectFields(r.expr, read);
    if (r.trigger && r.trigger.length) r.trigger.forEach((t) => read.add(t));
    read.delete(r.target);
    deps.set(r.target, [...read].filter((f) => derives.some((d) => d.target === f)));
  }
  const ordered = [];
  const done = new Set();
  let guard = 0;
  while (derives.length && guard++ < 500) {
    const ready = derives.filter((r) => (deps.get(r.target) || []).every((d) => done.has(d)));
    if (!ready.length) break; // 成环，剩余丢弃
    for (const r of ready) {
      ordered.push(r);
      done.add(r.target);
    }
    ready.forEach((r) => {
      const i = derives.indexOf(r);
      if (i >= 0) derives.splice(i, 1);
    });
  }
  return [...ordered, ...derives, ...others];
}

function collectFields(node, out) {
  if (!node || typeof node !== 'object') return;
  if (Object.prototype.hasOwnProperty.call(node, 'field')) out.add(node.field);
  if (Array.isArray(node.args)) node.args.forEach((a) => collectFields(a, out));
}

/** 明细字段聚合归一：AI/用户配置 sum(明细字段) 时自动转为 detailSum(表, 字段)，
 *  因为明细字段值不在主表 values 里，直接 sum 永远得 0 */
function normalizeDetailAgg(rule, fields) {
  if (!rule.expr || rule.expr.fn !== 'sum' || !Array.isArray(rule.expr.args) || rule.expr.args.length !== 1) return;
  const a = rule.expr.args[0];
  if (!a || !a.field || a.value !== undefined) return;
  const fd = (fields || []).find((f) => f && f.field === a.field && f.table && f.table !== 'main');
  if (fd) rule.expr = { fn: 'detailSum', args: [{ value: fd.table }, { value: a.field }] };
}

/** 单条校验规则求值 */
function validateOne(rule, values, patched, detailData) {
  const get = (f) => (patched[f] !== undefined ? patched[f] : values[f]);
  const target = rule.target;
  const act = rule.action || {};
  const blank = (v) => isBlank(v);
  if (act.validator === 'required') {
    return blank(get(target)) ? err(rule, '不能为空') : null;
  }
  if (act.validator === 'range') {
    const n = numOf(get(target));
    if (blank(get(target))) return null;
    const min = act.min === undefined || act.min === '' ? -Infinity : numOf(act.min);
    const max = act.max === undefined || act.max === '' ? Infinity : numOf(act.max);
    return isNaN(n) || n < min || n > max ? err(rule, '需在 ' + (act.min ?? '') + ' ~ ' + (act.max ?? '') + ' 范围内') : null;
  }
  if (act.validator === 'regex') {
    if (blank(get(target))) return null;
    try {
      return new RegExp(act.pattern).test(String(get(target))) ? null : err(rule, '格式不正确');
    } catch (e) {
      return err(rule, '正则表达式无效');
    }
  }
  if (act.validator === 'crossField') {
    // 目标为空时跳过比较（空值约束由必填规则负责），避免空字段误报「不能大于/必须等于」
    if (blank(get(target))) return null;
    // 兼容两种格式：面板 {left: 字段名, rightField/right} 与 后端/AI {left:{field}, right:{value|field}}
    const leftN = act.left && typeof act.left === 'object' ? act.left.field : act.left;
    let rightVal;
    if (act.right && typeof act.right === 'object' && !Array.isArray(act.right)) {
      rightVal = Object.prototype.hasOwnProperty.call(act.right, 'value') ? act.right.value : get(act.right.field);
    } else if (act.rightField) {
      rightVal = get(act.rightField);
    } else {
      rightVal = act.right;
    }
    const ok = compare(act.cmp || 'eq', leftN !== undefined ? get(leftN) : undefined, rightVal);
    return ok ? null : err(rule, '校验不通过');
  }
  if (act.validator === 'detailSum') {
    const rows = (detailData && detailData[act.table]) || [];
    const sum = rows.reduce((s, r) => s + (isNaN(numOf(r[act.field])) ? 0 : numOf(r[act.field])), 0);
    const max = numOf(act.max);
    return isNaN(max) || sum <= max ? null : err(rule, '明细合计 ' + sum + ' 超过上限 ' + act.max);
  }
  return null;
}

function err(rule, msg) {
  return { field: rule.target, message: rule.message || ('字段校验不通过：' + msg), rule: rule.id };
}

/** 明细行规则（在 FormGridRender 内按行求值）：数量×单价 等行内计算 */
export function applyDetailRules(rules, rowsByTable, fieldMap) {
  const rulesList = (rules || []).filter((r) => r && r.enabled !== false && r.type === 'derive' && r.scope && r.scope.detail && r.target);
  if (!rulesList.length) return;
  ctx = { detailData: rowsByTable || {} };
  for (const [table, rows] of Object.entries(rowsByTable || {})) {
    const tableRules = rulesList.filter((r) => (r.scope && r.scope.detail) === table);
    for (const row of rows) {
      for (const rule of tableRules) {
        const target = rule.target;
        if (!target) continue;
        let v;
        if (rule.mapping && rule.mapping.length) {
          v = row[rule.mapping[0].from];
        } else if (rule.expr) {
          const ctx = { get: (f) => row[f], detailData: rowsByTable || {} };
          v = evalNode(rule.expr, ctx);
        }
        if (v !== undefined && fieldMap && fieldMap[target]) row[target] = v;
      }
    }
  }
}

/**
 * 高级脚本执行器（前端运行态，仅管理员在设计器中配置）。
 *
 * @param {Array} scripts ext_json.scripts：[{ id, name, phase: 'load'|'change'|'submit', code }]
 * @param {String} phase 本次执行时机，只跑匹配 phase 的脚本
 * @param {Object} api 脚本可用能力：
 *   - get(field)              读主表字段值
 *   - set(field, value)       写主表字段值（页面直接落 formData，随后触发规则重算）
 *   - detailData              明细数据 { 表名: [行] }
 *   - rows(table)             取明细行数组
 *   - fail(message)           submit 时机抛出拦截提交；其他时机提示
 * @returns {void} 脚本通过 api.set 直接修改值
 */
export function runFormScripts(scripts, phase, api) {
  const list = (scripts || []).filter((s) => s && s.code && s.enabled !== false && (s.phase || 'change') === phase);
  if (!list.length || !api) return;
  for (const s of list) {
    try {
      // 管理员配置的受控脚本：以 Function 沙箱执行，ctx 注入受限 API，不暴露页面对象
      const fn = new Function('ctx', '"use strict";\n' + s.code);
      fn(api);
    } catch (e) {
      const msg = (e && e.message) || '脚本执行出错';
      if (phase === 'submit' && api.fail) {
        api.fail((s.name || '高级脚本') + '：' + msg);
      } else {
        console.warn('[表单脚本] ' + (s.name || s.id) + ' 执行失败：', msg);
      }
    }
  }
}
