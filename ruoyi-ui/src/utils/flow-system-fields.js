/**
 * 流程表单系统字段（与后端 FlowSystemFields 对齐）
 *
 * 这些字段的值由平台计算或生成，发起页/办理页一律只读回显：
 *  - procStatus  流程状态（1进行中 2已完成 3已撤销）
 *  - printCount  打印次数
 *  - processNo   流程编号（发起时按「流程高级设置 → 流程编号规则」生成）
 */
export const SYS_READONLY_FIELDS = ['procStatus', 'printCount', 'processNo'];

/** 是否系统只读字段（不允许人工填写） */
export function isSysReadonlyField(field) {
  return !!field && SYS_READONLY_FIELDS.includes(field);
}
