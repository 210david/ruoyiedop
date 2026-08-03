-- =============================================
-- 采购管理编号规则独立配置更新脚本
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 删除旧的编号规则菜单（如果存在）
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@pmsParentId);
DELETE FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@pmsParentId;

-- 重新创建编号规则菜单（使用独立的PMS页面）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('编号规则', @pmsParentId, 99, 'numberRule', 'pms/numberRule/index', '', 'PmsNumberRule', 1, 0, 'C', '0', '0', 'pms:numberRule:list', 'number', 'admin', sysdate(), '采购管理编号规则');

SET @pmsRuleMenuId = LAST_INSERT_ID();

-- 添加按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
VALUES 
('规则查询', @pmsRuleMenuId, 1, '', 'F', '0', '0', 'pms:numberRule:query', 'admin', sysdate()),
('规则新增', @pmsRuleMenuId, 2, '', 'F', '0', '0', 'pms:numberRule:add', 'admin', sysdate()),
('规则修改', @pmsRuleMenuId, 3, '', 'F', '0', '0', 'pms:numberRule:edit', 'admin', sysdate()),
('规则导出', @pmsRuleMenuId, 4, '', 'F', '0', '0', 'pms:numberRule:export', 'admin', sysdate());

-- 给admin角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'pms:numberRule:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- 确保PMS编号规则的module字段正确设置
UPDATE mk_number_rule SET module='pms' WHERE rule_code LIKE 'pms_%';

-- 验证配置
SELECT '菜单配置完成' AS result;
SELECT menu_id, menu_name, perms, component FROM sys_menu WHERE parent_id=@pmsParentId AND menu_name='编号规则';
SELECT menu_name, perms FROM sys_menu WHERE perms LIKE 'pms:numberRule:%';
