-- =====================================================
-- DMS 工单状态流转按钮权限细化
-- 说明：原工单的派工/改派/接单/处理/完工/验收/驳回/撤销 8个操作
--       全部复用 dms:workorder:edit，无法实现细粒度权限控制。
--       本脚本新增8个独立按钮权限，实现：
--       - 维修工只能接单/处理/完工
--       - 调度员只能派工/改派
--       - 验收员只能验收/驳回
--       - 报修人只能撤销
-- 注意：menu_id 3127-3131 已被其他菜单占用，故使用 3132-3139
-- 执行方式：直接在数据库中执行
-- =====================================================

-- 1. 新增8个工单状态流转按钮权限（父级菜单: 3040 工单列表）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3135, '工单派工', 3040, 6,  '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:dispatch', '#', 'admin', sysdate(), '工单状态流转：派工'),
(3136, '工单改派', 3040, 7,  '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:reassign', '#', 'admin', sysdate(), '工单状态流转：改派'),
(3137, '工单接单', 3040, 8,  '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:accept', '#', 'admin', sysdate(), '工单状态流转：接单'),
(3138, '工单处理', 3040, 9,  '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:process', '#', 'admin', sysdate(), '工单状态流转：开始处理'),
(3139, '工单完工', 3040, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:complete', '#', 'admin', sysdate(), '工单状态流转：完工'),
(3132, '工单验收', 3040, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:verify', '#', 'admin', sysdate(), '工单状态流转：验收通过'),
(3133, '工单驳回', 3040, 12, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:reject', '#', 'admin', sysdate(), '工单状态流转：驳回重做'),
(3134, '工单撤销', 3040, 13, '', '', '', '', 1, 0, 'F', '0', '0', 'dms:workorder:cancel', '#', 'admin', sysdate(), '工单状态流转：撤销');

-- 2. 为超级管理员(role_id=1)授权新增的8个按钮权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 3132), (1, 3133), (1, 3134), (1, 3135),
(1, 3136), (1, 3137), (1, 3138), (1, 3139);

-- 3. 验证
SELECT menu_id, menu_name, parent_id, perms, menu_type
FROM sys_menu
WHERE menu_id BETWEEN 3132 AND 3139
ORDER BY menu_id;

SELECT rm.role_id, r.role_name, rm.menu_id, m.menu_name, m.perms
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.role_id
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.menu_id BETWEEN 3132 AND 3139
ORDER BY rm.role_id, rm.menu_id;
