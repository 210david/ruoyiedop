-- =============================================
-- 供应商管理系统 - 添加编号规则配置菜单
-- 功能：在供应商管理系统（顶级目录）下添加编号规则入口
-- =============================================

-- 获取采购管理顶级目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- =============================================
-- 1. 删除之前错误创建的菜单（在供应商管理子目录下的）
-- =============================================
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392
);
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392
  )
);
DELETE FROM sys_menu WHERE parent_id IN (
  SELECT t.menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392
  ) t
);
DELETE FROM sys_menu WHERE menu_name='编号规则' AND parent_id=4392;

-- =============================================
-- 2. 创建供应商管理系统目录（如果不存在）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商管理系统', @pmsParentId, 0, 'supplierSystem', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '供应商管理系统目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='供应商管理系统' AND parent_id=@pmsParentId);

-- 获取供应商管理系统目录ID
SET @supplierSystemId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商管理系统' AND parent_id=@pmsParentId LIMIT 1);

-- =============================================
-- 3. 删除已存在的供应商编号规则菜单（防止重复执行）
-- =============================================
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@supplierSystemId
);
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@supplierSystemId
  )
);
DELETE FROM sys_menu WHERE parent_id IN (
  SELECT t.menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@supplierSystemId
  ) t
);
DELETE FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@supplierSystemId;

-- =============================================
-- 4. 在供应商管理系统下添加编号规则菜单
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('编号规则', @supplierSystemId, 99, 'supplierNumberRule', 'mk/numberRule/index', '{"module":"wms","ruleCode":"wms_supplier"}', 'PmsSupplierNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '供应商编号规则配置');
SET @supplierRuleMenuId = LAST_INSERT_ID();

-- 添加按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('规则查询', @supplierRuleMenuId, 1, '', 'F', '0', '0', 'marketing:numberRule:query', 'admin', sysdate()),
('规则新增', @supplierRuleMenuId, 2, '', 'F', '0', '0', 'marketing:numberRule:add', 'admin', sysdate()),
('规则修改', @supplierRuleMenuId, 3, '', 'F', '0', '0', 'marketing:numberRule:edit', 'admin', sysdate()),
('规则导出', @supplierRuleMenuId, 4, '', 'F', '0', '0', 'marketing:numberRule:export', 'admin', sysdate());

-- 给admin角色分配权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (perms LIKE 'marketing:numberRule:%' AND parent_id=@supplierRuleMenuId)
   OR menu_id=@supplierRuleMenuId;

-- 验证结果
SELECT '供应商管理系统编号规则菜单添加完成' AS result;
SELECT menu_id, menu_name, parent_id, menu_type, component, query, perms, icon
FROM sys_menu WHERE menu_id=@supplierRuleMenuId;
SELECT menu_id, menu_name, perms FROM sys_menu WHERE parent_id=@supplierRuleMenuId ORDER BY order_num;
SELECT '供应商管理系统目录结构' AS info;
SELECT menu_id, menu_name, menu_type, path, order_num FROM sys_menu WHERE parent_id=@supplierSystemId ORDER BY order_num;
