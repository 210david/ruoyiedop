-- =====================================================
-- 仓库管理 - 盘点业务按钮权限细化
-- 创建时间：2026-07-26
-- 说明：
--   1. 修复菜单2091(盘点作业)权限标识：wms:stocktake:detail → wms:stocktake:query
--   2. 盘点任务(2090)下新增5个状态流转按钮权限
--   3. 盘点作业(2091)下新增3个操作按钮权限
--   4. 原先8个状态流转接口全部复用 wms:stocktake:edit，无法单独控制
-- =====================================================

-- 1. 修复盘点作业菜单(2091)权限标识
UPDATE sys_menu SET perms = 'wms:stocktake:query' WHERE menu_id = 2091 AND perms = 'wms:stocktake:detail';

-- 2. 盘点任务(2090)下新增5个按钮权限
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2130, '开始盘点', 2090, 6,  '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:start',      '#', 'admin', sysdate(), '盘点状态流转：草稿→进行中'),
(2131, '提交审批', 2090, 7,  '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:submit',     '#', 'admin', sysdate(), '盘点状态流转：进行中→待审批'),
(2132, '盘点审批', 2090, 8,  '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:approve',    '#', 'admin', sysdate(), '盘点审批：通过/驳回'),
(2133, '盘点作废', 2090, 9,  '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:void',       '#', 'admin', sysdate(), '作废盘点单'),
(2134, '盘点导出', 2090, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:export',     '#', 'admin', sysdate(), '导出盘点明细模板');

-- 3. 盘点作业(2091)下新增3个按钮权限
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2135, '明细录入', 2091, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:input',      '#', 'admin', sysdate(), '单条录入实盘数量'),
(2136, '批量录入', 2091, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:batchInput', '#', 'admin', sysdate(), '批量录入实盘数量'),
(2137, '批量导入', 2091, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:stocktake:import',     '#', 'admin', sysdate(), 'Excel导入盘点明细');

-- 4. 为超级管理员(role_id=1)和总经理(role_id=104)授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 2130), (1, 2131), (1, 2132), (1, 2133), (1, 2134), (1, 2135), (1, 2136), (1, 2137),
(104, 2130), (104, 2131), (104, 2132), (104, 2133), (104, 2134), (104, 2135), (104, 2136), (104, 2137);

-- 5. 验证
SELECT menu_id, menu_name, parent_id, order_num, menu_type, perms FROM sys_menu WHERE menu_id IN (2090, 2091, 2092, 2093, 2094, 2095, 2130, 2131, 2132, 2133, 2134, 2135, 2136, 2137) ORDER BY parent_id, order_num, menu_id;
