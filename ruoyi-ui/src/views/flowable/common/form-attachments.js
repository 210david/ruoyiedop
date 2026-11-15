import { rtypeOf } from '@/components/FormGridControl.js';

/** 表单附件来源标识（相关资源「上传环节」列） */
const FORM_SOURCE = '表单附件';

function fileNameOf(url) {
  const s = String(url || '').trim();
  const name = s.slice(s.lastIndexOf('/') + 1) || s;
  try {
    return decodeURIComponent(name);
  } catch (e) {
    return name;
  }
}

function splitUrls(value) {
  if (value === undefined || value === null) return [];
  const arr = Array.isArray(value) ? value : String(value).split(',');
  return arr
    .map((v) => {
      if (v === null || v === undefined) return '';
      if (typeof v === 'object') return v.url || '';
      return String(v).trim();
    })
    .filter(Boolean);
}

/**
 * 汇总流程表单里已上传的附件（主表 + 明细表）。
 * 表单附件字段只存文件地址（逗号分隔），这里转成与流程附件同形状的行，
 * 供「相关资源」页签与表单附件字段统一展示。
 *
 * @param {Array} fields 表单字段定义
 * @param {Object} values 表单值（流程变量，明细在 values.detailData）
 * @param {Object} [meta] 上传人/上传时间（表单附件没有独立的登记记录，取流程发起人 + 发起时间）
 * @returns {Array<{attachId:null,fileName:string,fileUrl:string,fileSize:null,taskName:string,uploaderName:string,createTime:string}>}
 */
export function collectFormAttachments(fields, values, meta = {}) {
  const rows = [];
  if (!Array.isArray(fields) || !values) return rows;
  const seen = new Set();
  const push = (url, label) => {
    if (!url || seen.has(url)) return;
    seen.add(url);
    rows.push({
      attachId: null,
      fileName: fileNameOf(url),
      fileUrl: url,
      fileSize: null,
      taskName: label || FORM_SOURCE,
      uploaderName: meta.uploaderName || '',
      createTime: meta.createTime || '',
    });
  };
  for (const f of fields) {
    if (!f || rtypeOf(f) !== 'attach') continue;
    // 主表字段（老表单可能没有 table，默认按主表处理）
    if (!f.table || f.table === 'main') {
      splitUrls(values[f.field]).forEach((url) => push(url, f.label ? FORM_SOURCE + '·' + f.label : FORM_SOURCE));
      continue;
    }
    // 明细表附件列
    const detail = values.detailData;
    const detailRows = detail && typeof detail === 'object' ? detail[f.table] || [] : [];
    for (const row of detailRows) {
      splitUrls(row && row[f.field]).forEach((url) => push(url, f.label ? FORM_SOURCE + '·' + f.label : FORM_SOURCE));
    }
  }
  return rows;
}
