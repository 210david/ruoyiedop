-- =============================================
-- 供应商管理模块 - 添加编号规则配置菜单
-- 功能：在供应商管理目录下添加编号规则入口，
--       复用 MK 编号规则页面，预过滤为 wms_supplier 规则
-- =============================================

-- 获取采购管理顶级目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 获取供应商管理目录ID
-- 优先查找 M 类型（目录），若为 C 类型则取其父级作为挂载点
SET @supplierDirId = (SELECT menu_id FROM sys_menu WHERE menu_name='供应商管理' AND menu_type='M' AND parent_id=@pmsParentId LIMIT 1);
-- 兼容旧结构：若供应商管理是 C 类型菜单，编号规则挂到同级目录下
SET @targetParentId = IFNULL(@supplierDirId,
  IFNULL((SELECT parent_id FROM sys_menu WHERE menu_name='供应商管理' AND parent_id=@pmsParentId LIMIT 1), @pmsParentId));

-- 删除已存在的供应商编号规则菜单（防止重复执行）
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@targetParentId
    AND component='mk/numberRule/index'
);
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@targetParentId
      AND component='mk/numberRule/index'
  )
);
DELETE FROM sys_menu WHERE parent_id IN (
  SELECT t.menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@targetParentId
      AND component='mk/numberRule/index'
  ) t
);
DELETE FROM sys_menu WHERE menu_name='编号规则' AND parent_id=@targetParentId
  AND component='mk/numberRule/index';

-- =============================================
-- 添加编号规则菜单（复用 MK 编号规则页面，预过滤供应商规则）
-- query 字段使用 JSON 格式（前端 sidebar 通过 JSON.parse 解析）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('编号规则', @targetParentId, 99, 'supplierNumberRule', 'mk/numberRule/index', '{"module":"wms","ruleCode":"wms_supplier"}', 'PmsSupplierNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '供应商编号规则配置');
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

-- =============================================
-- 修复已有编号规则菜单的 query 格式为 JSON（前端 sidebar 需 JSON 格式）
-- 旧格式 module=wms 无法被 JSON.parse 解析，会导致模块过滤失效
-- =============================================
UPDATE sys_menu SET query='{"module":"mk"}' WHERE menu_name='编号规则' AND parent_id=4000 AND component='mk/numberRule/index';
UPDATE sys_menu SET query='{"module":"dms"}' WHERE menu_name='编号规则' AND parent_id=3000 AND component='mk/numberRule/index';
UPDATE sys_menu SET query='{"module":"wms"}' WHERE menu_name='编号规则' AND parent_id=2000 AND component='mk/numberRule/index';

-- 验证结果
SELECT '供应商编号规则菜单添加完成' AS result;
SELECT menu_id, menu_name, parent_id, menu_type, component, query, perms, icon
FROM sys_menu WHERE parent_id=@targetParentId AND menu_name='编号规则' AND component='mk/numberRule/index';
SELECT menu_id, menu_name, perms FROM sys_menu WHERE parent_id=@supplierRuleMenuId ORDER BY order_num;
SELECT '所有编号规则菜单 query 格式检查' AS info;
SELECT menu_id, menu_name, parent_id, query FROM sys_menu WHERE menu_name='编号规则' AND component='mk/numberRule/index';
