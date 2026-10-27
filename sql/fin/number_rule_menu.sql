-- ============================================================
-- 财务模块编号规则（接入 mk_number_rule 统一编码管理）
-- 1. 新增「编码管理」独立菜单（挂财务管理「基础设置」906100 下）
-- 2. 预置财务相关自动编码规则种子数据（module='fin'）
-- 回退：见文末回退语句
-- ============================================================

-- 1. 菜单：编码管理（menu_id 906134，与其他模块 numberRule 页面一致）
-- route_name 必须唯一：未配置时 RuoYi 用 capitalize(path) 生成，与 hr 等模块的 numberRule 路由 name 冲突会被覆盖导致 404
-- 菜单位置：财务管理(905938)末尾一级菜单（order_num=9），与其他模块保持同名「编码规则」
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 906134, '编码规则', 905938, 9, 'numberRule', 'fin/numberRule/index', '', 'FinNumberRule', 1, 0, 'C', '0', '0', 'fin:numberRule:list', 'edit', 'admin', NOW(), '财务编号规则管理页面（复用 mk_number_rule 统一编码服务，module=fin；财务管理末尾一级菜单）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 906134);

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务编码查询', 906134, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'fin:numberRule:query', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:numberRule:query' AND menu_type = 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务编码新增', 906134, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'fin:numberRule:add', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:numberRule:add' AND menu_type = 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务编码修改', 906134, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'fin:numberRule:edit', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:numberRule:edit' AND menu_type = 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务编码删除', 906134, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'fin:numberRule:remove', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:numberRule:remove' AND menu_type = 'F');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务编码导出', 906134, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'fin:numberRule:export', '#', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'fin:numberRule:export' AND menu_type = 'F');

-- 2. 预置财务自动编码规则（单据类 reset_type='1' 按天重置；基础资料类 reset_type='0' 不重置）
-- 注：fin_receipt/fin_payment/fin_expense 已按用户决策移除（财务域无收款单/付款单/报销单实体，报销为 P4 预留），后续业务上线再单独增加
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, connector, status, create_by, create_time, remark)
SELECT * FROM (
  SELECT 'fin_voucher' rule_code, 'fin' module, '记账凭证号' rule_name, 'PV' prefix, 'yyyyMMdd' date_format, '1' reset_type, 5 seq_length, 1 seq_start, 1 step, 0 current_seq, '' connector, '0' status, 'admin' create_by, NOW() create_time, '财务记账凭证编号，按天重置，由 IMkNumberRuleService 生成' remark
  UNION ALL SELECT 'fin_ar_order', 'fin', '应收单号', 'AR', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '财务-应收管理'
  UNION ALL SELECT 'fin_ap_order', 'fin', '应付单号', 'AP', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '财务-应付管理'
  UNION ALL SELECT 'fin_cost_cf', 'fin', '成本结转单号', 'CB', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '财务-存货成本结转'
  UNION ALL SELECT 'fin_partner', 'fin', '往来单位编号', 'WL', 'yyyy', '0', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '财务-往来单位'
  UNION ALL SELECT 'fin_bad_debt', 'fin', '坏账计提单号', 'BD', 'yyyyMMdd', '1', 4, 1, 1, 0, '-', '0', 'admin', NOW(), '财务-坏账计提'
) t
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule r WHERE r.rule_code = t.rule_code);

-- ============================================================
-- 回退语句（如需撤销）
-- DELETE FROM sys_menu WHERE menu_id = 906134 OR (parent_id = 906134 AND menu_type = 'F');
-- DELETE FROM mk_number_rule WHERE module = 'fin' AND current_seq = 0 AND rule_code IN ('fin_voucher','fin_ar_order','fin_ap_order','fin_cost_cf','fin_partner','fin_bad_debt');
-- ============================================================
