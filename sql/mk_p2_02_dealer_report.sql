-- ============================================================
-- P2-02 经销商专项报表：菜单与权限
-- 报表入口：营销管理 → 数据分析 → 经销商专项报表
-- 权限：marketing:dealerReport:list / marketing:dealerReport:export
-- ============================================================

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4215, '经销商专项报表', 4006, 3, 'dealerReport', 'mk/dealerReport/index', '', 'MkDealerReport', 1, 0, 'C', '0', '0', 'marketing:dealerReport:list', 'documentation', 'admin', sysdate(), '分经销商销量/返利/账龄/流向汇总'),
(4216, '报表导出', 4215, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:dealerReport:export', '#', 'admin', sysdate(), '');
