-- =====================================================
-- 线索公海菜单按钮权限细化
-- 说明：原线索公海菜单(4050)下无按钮权限，公海页面的
--   "申请领取"复用 marketing:lead:receive（挂在线索列表下）
--   "审批"复用 marketing:lead:assign（与线索分配共用）
--   无法单独控制公海领取和审批权限。
-- 本脚本新增2个独立按钮权限，挂在公海菜单(4050)下：
--   - 公海领取：marketing:lead:pool:receive
--   - 领取审批：marketing:lead:pool:approve
-- =====================================================

-- 1. 新增2个公海按钮权限（父级: 4050 线索公海）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4195, '公海领取', 4050, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:pool:receive', '#', 'admin', sysdate(), '从公海申请领取线索'),
(4196, '领取审批', 4050, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:lead:pool:approve', '#', 'admin', sysdate(), '审批公海线索领取申请（通过/拒绝）');

-- 2. 为超级管理员(role_id=1)授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4195), (1, 4196);

-- 3. 验证
SELECT menu_id, menu_name, parent_id, perms, menu_type FROM sys_menu WHERE menu_id IN (4050, 4195, 4196) ORDER BY menu_id;
