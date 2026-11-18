/**
 * workDays 规则函数的企业日历预加载
 *
 * workDays(开始, 结束) 需要企业工作日历数据（biz_hr_workday_calendar），
 * 前端引擎是同步求值，这里在调用 runRules 之前扫描规则里的 workDays 引用字段，
 * 按值区间异步拉取日历标记并注入引擎（engine.js setWorkdayMap）。
 * 未加载/区间无数据时引擎按「剔除周六日」回退口径计算。
 */

import request from '@/utils/request';
import { setWorkdayMap } from './engine';

/** 已请求过的区间缓存（key: start~end），避免每次字段变化都请求 */
const rangeCache = new Map();

/**
 * 扫描 derive 规则中 workDays 的参数字段，按当前值区间拉取日历并注入引擎。
 * @param {Array} rules 规则列表
 * @param {Object} values 主表字段当前值
 * @returns {Promise} 日历加载完成（无 workDays 用法时立即 resolve）
 */
export function prepareWorkdayCalendar(rules, values) {
  const dates = [];
  for (const r of rules || []) {
    if (!r || r.enabled === false || r.type !== 'derive') continue;
    collectWorkDaysDates(r.expr, values, dates);
  }
  if (!dates.length) return Promise.resolve();
  const min = dates.reduce((m, d) => (d < m ? d : m));
  const max = dates.reduce((M, d) => (d > M ? d : M));
  if (min > max) return Promise.resolve();
  const key = min + '~' + max;
  if (rangeCache.has(key)) return rangeCache.get(key);
  const p = request({
    url: '/hr/workdayCalendar/map',
    method: 'get',
    params: { startDate: min, endDate: max }
  })
    .then((res) => {
      const map = res && res.data ? res.data : {};
      setWorkdayMap(map);
      return map;
    })
    .catch(() => {
      // 拉取失败静默回退（引擎按剔除周末口径），缓存空结果避免反复请求
      rangeCache.set(key, Promise.resolve({}));
      return {};
    });
  rangeCache.set(key, p);
  return p;
}

/** 递归收集 workDays 参数中的日期（字段值优先，常量兜底） */
function collectWorkDaysDates(node, values, out) {
  if (!node || typeof node !== 'object') return;
  if (node.fn === 'workDays' && Array.isArray(node.args)) {
    for (const a of node.args) {
      if (!a) continue;
      if (a.field) {
        const v = values ? values[a.field] : undefined;
        if (v !== undefined && v !== null && v !== '') out.push(String(v).slice(0, 10));
      } else if (a.value) {
        out.push(String(a.value).slice(0, 10));
      }
    }
    return;
  }
  if (Array.isArray(node.args)) node.args.forEach((a) => collectWorkDaysDates(a, values, out));
}
