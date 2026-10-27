-- =============================================================
-- 安全生产管理模块 - 菜单及按钮权限修复脚本
-- 修复内容：
--   P0: 检查标准库(StdLibrary)完整菜单+按钮权限
--   P1: 补齐12个模块缺失的export导出按钮权限
--   P1: 统一角色ID分配（role_id=1超管 和 role_id=2 admin）
--   P2: 预置安全模块角色模板（安全员、安全主管）
--   P2: 新增 safety:hazard:rectify 隐患整改权限
-- 本脚本幂等，可重复执行
-- =============================================================

-- =============================================================
-- P0: 检查标准库(StdLibrary) 菜单+按钮权限
-- =============================================================

-- 获取双控机制目录ID（检查标准库属于双控机制的支撑库）
SELECT @dualControlMenuId := menu_id FROM sys_menu WHERE menu_name = '双控机制' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- 检查标准库菜单（如果不存在才插入）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '检查标准库', @dualControlMenuId, 4, 'library', 'safety/dualcontrol/library/index', 1, 0, 'C', '0', '0', 'safety:library:list', 'tree-table', 'admin', NOW(), '检查标准库菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:library:list');
SET @libraryMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:library:list');

-- 检查标准库按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '标准库查询', @libraryMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'safety:library:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:library:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '标准库新增', @libraryMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'safety:library:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:library:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '标准库修改', @libraryMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'safety:library:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:library:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '标准库删除', @libraryMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'safety:library:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:library:remove');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '标准库导出', @libraryMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:library:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:library:export');

-- =============================================================
-- P1: 补齐12个模块缺失的export导出按钮权限
-- =============================================================

-- 获取各菜单ID
SET @areaMenuId        = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:area:list');
SET @certMenuId       = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:cert:list');
SET @workerMenuId     = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:worker:list');
SET @equipmentMenuId  = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:equipment:list');
SET @materialMenuId  = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:material:list');
SET @taskMenuId      = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:task:list');
SET @planMenuId      = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:training:plan:list');
SET @recordMenuId    = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:training:record:list');
SET @ePlanMenuId     = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:emergency:plan:list');
SET @eMaterialMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:emergency:material:list');
SET @drillMenuId     = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:emergency:drill:list');
SET @incidentMenuId  = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:emergency:incident:list');

-- 1. 区域导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '区域导出', @areaMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:area:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:area:export') AND @areaMenuId IS NOT NULL;

-- 2. 证照导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '证照导出', @certMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:cert:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:cert:export') AND @certMenuId IS NOT NULL;

-- 3. 特种人员导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '特种人员导出', @workerMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:worker:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:worker:export') AND @workerMenuId IS NOT NULL;

-- 4. 特种设备导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '特种设备导出', @equipmentMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:equipment:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:equipment:export') AND @equipmentMenuId IS NOT NULL;

-- 5. 危化品导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '危化品导出', @materialMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:material:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:material:export') AND @materialMenuId IS NOT NULL;

-- 6. 排查任务导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '任务导出', @taskMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:task:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:task:export') AND @taskMenuId IS NOT NULL;

-- 7. 培训计划导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '计划导出', @planMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:plan:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:training:plan:export') AND @planMenuId IS NOT NULL;

-- 8. 培训记录导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '记录导出', @recordMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:training:record:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:training:record:export') AND @recordMenuId IS NOT NULL;

-- 9. 应急预案导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '预案导出', @ePlanMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:plan:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:emergency:plan:export') AND @ePlanMenuId IS NOT NULL;

-- 10. 应急物资导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物资导出', @eMaterialMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:material:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:emergency:material:export') AND @eMaterialMenuId IS NOT NULL;

-- 11. 演练记录导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '演练导出', @drillMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:drill:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:emergency:drill:export') AND @drillMenuId IS NOT NULL;

-- 12. 事故管理导出
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '事故导出', @incidentMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'safety:emergency:incident:export', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:emergency:incident:export') AND @incidentMenuId IS NOT NULL;

-- =============================================================
-- P2: 新增 safety:hazard:rectify 隐患整改权限
-- =============================================================
SET @hazardMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'safety:hazard:list');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '隐患整改', @hazardMenuId, 9, '#', '', 1, 0, 'F', '0', '0', 'safety:hazard:rectify', '#', 'admin', NOW(), '隐患整改执行权限（与提交权限分离）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'safety:hazard:rectify') AND @hazardMenuId IS NOT NULL;

-- =============================================================
-- P1: 统一角色ID分配
-- 将所有 safety: 开头的权限统一分配给 role_id=1（超级管理员）和 role_id=2（admin）
-- =============================================================

-- 分配给 role_id=1（超级管理员）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'safety:%';

-- 分配给 role_id=2（admin）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:%';

-- 同时分配安全生产管理目录本身
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0;

-- =============================================================
-- P2: 预置安全模块角色模板
-- =============================================================

-- 安全员角色（可查看所有安全数据，可新增/整改/处理，不可删除和审批）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (110, '安全员', 'safety_officer', 7, '3', 1, 1, '0', '0', 'admin', sysdate(), '安全员角色模板：安全数据查看、隐患排查整改、到期提醒处理');

-- 安全员 - 菜单权限（所有安全菜单目录 + 查询/新增/整改权限，不含删除/审批/导出）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 110, menu_id FROM sys_menu WHERE perms LIKE 'safety:%'
  AND perms NOT LIKE 'safety:%:remove'
  AND perms NOT LIKE 'safety:%:approve'
  AND perms NOT LIKE 'safety:%:export'
  AND perms <> 'safety:numberRule:%';

-- 安全员 - 安全管理目录
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 110, menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 110, menu_id FROM sys_menu WHERE menu_name IN ('一企一档','双控机制','教育培训','应急管理','到期提醒','安全驾驶舱')
  AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- 安全主管角色（全部安全模块权限）
INSERT IGNORE INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (111, '安全主管', 'safety_manager', 8, '2', 1, 1, '0', '0', 'admin', sysdate(), '安全主管角色模板：全部安全生产管理权限');

-- 安全主管 - 全部安全模块菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 111, menu_id FROM sys_menu WHERE perms LIKE 'safety:%';

-- 安全主管 - 安全管理目录
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 111, menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 111, menu_id FROM sys_menu WHERE menu_name IN ('一企一档','双控机制','教育培训','应急管理','到期提醒','安全驾驶舱')
  AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- =============================================================
-- 验证
-- =============================================================
SELECT '=== 检查标准库权限 ===' AS info;
SELECT menu_id, menu_name, perms FROM sys_menu WHERE perms LIKE 'safety:library:%' ORDER BY order_num;

SELECT '=== 新增导出权限 ===' AS info;
SELECT menu_id, menu_name, perms FROM sys_menu WHERE perms LIKE 'safety:%:export' AND perms NOT IN (
  'safety:enterprise:export', 'safety:risk:export', 'safety:hazard:export',
  'safety:training:course:export', 'safety:training:attendee:export', 'safety:training:cert:export',
  'safety:numberRule:export', 'safety:library:export'
) ORDER BY perms;

SELECT '=== 隐患整改权限 ===' AS info;
SELECT menu_id, menu_name, perms FROM sys_menu WHERE perms = 'safety:hazard:rectify';

SELECT '=== 安全部色模板 ===' AS info;
SELECT role_id, role_name, role_key, remark FROM sys_role WHERE role_id IN (110, 111);

SELECT '=== 各角色安全权限数量 ===' AS info;
SELECT r.role_id, r.role_name, COUNT(rm.menu_id) AS safety_menu_count
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN sys_menu m ON rm.menu_id = m.menu_id AND (m.perms LIKE 'safety:%' OR (m.menu_name = '安全生产管理' AND m.parent_id = 0))
WHERE r.role_id IN (1, 2, 110, 111)
GROUP BY r.role_id, r.role_name;
