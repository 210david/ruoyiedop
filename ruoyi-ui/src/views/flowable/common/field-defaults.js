/**
 * 流程表单字段默认值
 *
 * 设计器里给字段配「默认值」方式，发起页打开表单时自动带出，用户可再改。
 * 字段定义上的三个键：
 *   defaultMode   none（默认，不配置）| currentUser | currentDept | currentDate
 *                 | currentDatetime | fixedPick（指定人员/部门/物料…，弹框里选一条）
 *                 | fieldValue | fieldUserDept
 *   defaultField  fieldValue / fieldUserDept 时的源字段名（同一张表内的字段）
 *   defaultValue  固定值（选项字段由「配置」弹窗写入；fixedPick 时存选中项的名称）
 *   defaultValueId    fixedPick 时选中项的 ID（人员 userId / 部门 deptId）
 *   defaultValueDept  fixedPick 选人员时带回的部门 {deptId, deptName}，
 *                     供「随人员字段带出部门」在查不到人时也能推出部门
 *
 * 取值口径：
 *   - 人员默认值：字段值存名称，伴生键 `${field}__id` 存用户 ID
 *   - 部门默认值：字段值存部门名，伴生键 `${field}__id` 存部门 ID
 *   - 日期/时间：`YYYY-MM-DD` / `YYYY-MM-DD HH:mm:ss`，与日期控件、后端列格式一致
 *   - 只在字段当前为空时填充，草稿恢复 / 流程回显已有值不会被覆盖
 *   - 「随人员字段带出部门」依赖人员选择器确认时带回的部门信息（`${field}__extra`），
 *     不额外查接口，普通员工发起时也不会因数据权限拿不到部门
 */
import { getUserProfile } from '@/api/system/user';
import { BROWSE_TYPES } from '@/components/FormBrowse/sources.js';

/** 默认值方式 → 中文名（设计器下拉 + 字段列表摘要共用） */
export const DEFAULT_MODE_LABELS = {
  currentUser: '当前用户',
  currentDept: '当前用户所属部门',
  currentDate: '当天日期',
  currentDatetime: '当前时间',
  fixedPick: '指定值',
  fieldValue: '取表单字段',
  fieldUserDept: '随人员字段带出部门'
};

/** 下拉里显示的方式名：「指定值」按浏览数据源细化为「指定人员 / 指定部门 / 指定物料」 */
export function defaultModeLabel(f, mode) {
  if (mode === 'fixedPick') {
    const label = BROWSE_TYPES[(f && f.browseType) || 'user'];
    return label ? '指定' + label : '指定值';
  }
  return DEFAULT_MODE_LABELS[mode] || mode;
}

const pad = (n) => (n < 10 ? '0' + n : '' + n);

/** 当天日期 YYYY-MM-DD */
export function dateText(d) {
  const t = d || new Date();
  return `${t.getFullYear()}-${pad(t.getMonth() + 1)}-${pad(t.getDate())}`;
}

/** 当前时间 YYYY-MM-DD HH:mm:ss */
export function dateTimeText(d) {
  const t = d || new Date();
  return `${dateText(t)} ${pad(t.getHours())}:${pad(t.getMinutes())}:${pad(t.getSeconds())}`;
}

export function isEmptyValue(v) {
  if (v === undefined || v === null || v === '') return true;
  if (Array.isArray(v)) return v.length === 0;
  return false;
}

/** 字段所属表：主表统一记作 main */
const tableOf = (f) => (f && f.table && f.table !== 'main' ? f.table : 'main');

/** 主表字段：默认值应用到主表 values 时用（明细字段同名会串值，必须分开） */
export function mainFieldsOf(fields) {
  return (fields || []).filter((f) => f && tableOf(f) === 'main');
}

/**
 * 该字段可选的默认值方式（按表现形式 / 浏览数据源裁剪，避免列出无意义的组合）
 */
export function defaultModesOf(f) {
  if (!f || !f.field) return [];
  if (f.form === 'browse') {
    const type = f.browseType || 'user';
    // 浏览按钮的默认值应该“直接在组织/基础数据里指定一条”，而不是抄另一个字段的值
    if (type === 'user') return ['currentUser', 'fixedPick'];
    if (type === 'dept') return ['currentDept', 'fixedPick', 'fieldUserDept'];
    return ['fixedPick'];
  }
  if (f.form === 'date') return ['currentDate', 'fieldValue'];
  if (f.form === 'datetime') return ['currentDatetime', 'fieldValue'];
  // 文本 / 数字 / 选项类：用来引用别的字段（选项类的固定默认值走「配置」弹窗）
  return ['fieldValue'];
}

/**
 * 源字段候选：只在同一张表内取值（主表取主表、明细取同明细表），
 * 「随人员字段带出部门」只能选人员浏览字段，「取表单字段」不列附件与浏览按钮。
 * 注：不同表允许有同名字段（如主表与明细都有 bm），所以表名优先用 selfTable 传入，
 * 只给 selfField 时再回退按名字猜（同名时会猜不准）。
 */
export function sourceFieldOptions(fields, mode, selfField, selfTable) {
  let table = tableOf({ table: selfTable });
  if (!selfTable) {
    const self = (fields || []).find((x) => x && x.field === selfField);
    table = tableOf(self);
  }
  const list = (fields || []).filter((x) => x.field && x.field !== selfField && tableOf(x) === table);
  if (mode === 'fieldUserDept') {
    return list.filter((x) => x.form === 'browse' && (x.browseType || 'user') === 'user');
  }
  return list.filter((x) => x.form !== 'attach' && x.form !== 'browse');
}

/** 字段列表里的默认值摘要（空串表示未配置） */
export function defaultSummary(f) {
  if (!f) return '';
  const mode = f.defaultMode && f.defaultMode !== 'none' ? f.defaultMode : '';
  if (!mode) {
    return isEmptyValue(f.defaultValue) ? '' : `固定值：${Array.isArray(f.defaultValue) ? f.defaultValue.join('、') : f.defaultValue}`;
  }
  const label = defaultModeLabel(f, mode);
  if (mode === 'fixedPick') {
    return isEmptyValue(f.defaultValue) ? label : `${label}：${f.defaultValue}`;
  }
  if (mode === 'fieldValue' || mode === 'fieldUserDept') {
    return `${label}（${f.defaultField || '未选字段'}）`;
  }
  return label;
}

/* ===================== 运行态取值 ===================== */

let profileCache = null;

/** 当前用户上下文：ID / 姓名 / 所属部门（部门取 /system/user/profile，普通员工也有权访问） */
export async function currentUserCtx() {
  if (profileCache) return profileCache;
  const ctx = { userId: '', userName: '', deptId: '', deptName: '' };
  try {
    const res = await getUserProfile();
    const u = (res && res.data) || {};
    ctx.userId = u.userId || '';
    ctx.userName = u.nickName || u.userName || '';
    if (u.dept) {
      ctx.deptId = u.dept.deptId || '';
      ctx.deptName = u.dept.deptName || '';
    }
  } catch (e) {
    /* 取不到就留空，默认值回退为不填 */
  }
  profileCache = ctx;
  return ctx;
}

/** 从人员字段推出部门：优先用选择器带回的部门信息，其次比对「是否当前用户」 */
export function deptOfPerson(values, srcField, ctx) {
  if (!values || !srcField) return null;
  const extra = values[srcField + '__extra'];
  if (extra && extra.deptName) return { deptId: extra.deptId, deptName: extra.deptName };
  const pid = values[srcField + '__id'];
  if (ctx && !isEmptyValue(pid) && String(pid) === String(ctx.userId)) {
    return { deptId: ctx.deptId, deptName: ctx.deptName };
  }
  return null;
}

/** 是否配置了需要在打开表单时求值的默认值 */
export function hasRuntimeDefaults(fields) {
  return (fields || []).some((f) => f && f.defaultMode && f.defaultMode !== 'none');
}

/**
 * 打开表单时填充默认值（只填空字段）
 * @param {Array} fields 字段定义
 * @param {Object} values 表单值对象（会被就地修改）
 * @param {Object} ctx currentUserCtx() 的结果
 */
export function applyFieldDefaults(fields, values, ctx) {
  if (!values) return;
  const user = ctx || {};
  (fields || []).forEach((f) => {
    if (!f || !f.field) return;
    // 固定值（选项 / 文本）：哪怕没配 defaultMode 也生效；「指定人员/部门等」同时带上 ID 与附加信息
    if (isEmptyValue(values[f.field]) && !isEmptyValue(f.defaultValue)) {
      values[f.field] = Array.isArray(f.defaultValue) ? f.defaultValue.join(',') : f.defaultValue;
      if (!isEmptyValue(f.defaultValueId)) values[f.field + '__id'] = f.defaultValueId;
      if (f.defaultValueDept && f.defaultValueDept.deptName && isEmptyValue(values[f.field + '__extra'])) {
        values[f.field + '__extra'] = { deptId: f.defaultValueDept.deptId, deptName: f.defaultValueDept.deptName };
      }
    }
    if (isEmptyValue(values[f.field])) {
      const mode = f.defaultMode;
      if (mode === 'currentUser') {
        if (user.userName) {
          values[f.field] = user.userName;
          values[f.field + '__id'] = user.userId;
        }
      } else if (mode === 'currentDept') {
        if (user.deptName) {
          values[f.field] = user.deptName;
          values[f.field + '__id'] = user.deptId;
        }
      } else if (mode === 'currentDate') {
        values[f.field] = dateText();
      } else if (mode === 'currentDatetime') {
        values[f.field] = dateTimeText();
      } else if (mode === 'fieldValue') {
        const src = f.defaultField;
        if (src && !isEmptyValue(values[src])) values[f.field] = values[src];
      }
    }
  });
  // 第二遍：「随人员字段带出部门」要等人员字段的值（含指定人员带回的部门）都就位后再算，
  // 否则部门字段排在人员字段前面时会取不到部门
  (fields || []).forEach((f) => {
    if (!f || !f.field) return;
    if (f.form === 'browse' && f.browseType === 'dept') {
      if (f.defaultMode === 'fieldUserDept' && isEmptyValue(values[f.field])) {
        const dept = deptOfPerson(values, f.defaultField, user);
        if (dept && dept.deptName) {
          values[f.field] = dept.deptName;
          values[f.field + '__id'] = dept.deptId;
          return;
        }
      }
      // 部门字段已有名称但缺 ID（历史数据 / 手工填写）：按人员再补一次，便于打开选择器时高亮
      if (!isEmptyValue(values[f.field]) && isEmptyValue(values[f.field + '__id'])) {
        const dept = deptOfPerson(values, f.defaultField, user);
        if (dept && dept.deptName === values[f.field]) values[f.field + '__id'] = dept.deptId;
      }
    }
  });
}

/**
 * 人员字段变化 → 同步部门字段（「随人员字段带出部门」）
 * 人员有值且能推出部门时覆盖；人员被清空时同步清空部门（避免留下对不上的旧部门）
 */
export function syncPersonDept(fields, values, ctx) {
  if (!values) return false;
  let changed = false;
  (fields || []).forEach((f) => {
    if (!f || f.form !== 'browse' || f.defaultMode !== 'fieldUserDept') return;
    const src = f.defaultField;
    if (!src) return;
    const personEmpty = isEmptyValue(values[src]) && isEmptyValue(values[src + '__id']);
    if (personEmpty) {
      if (!isEmptyValue(values[f.field]) || !isEmptyValue(values[f.field + '__id'])) {
        values[f.field] = '';
        values[f.field + '__id'] = '';
        changed = true;
      }
      return;
    }
    const dept = deptOfPerson(values, src, ctx);
    if (!dept || !dept.deptName) return;
    if (values[f.field] !== dept.deptName || String(values[f.field + '__id'] || '') !== String(dept.deptId || '')) {
      values[f.field] = dept.deptName;
      values[f.field + '__id'] = dept.deptId;
      changed = true;
    }
  });
  return changed;
}
