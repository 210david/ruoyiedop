-- =============================================
-- 将供应商管理迁移到采购管理模块
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 获取采购业务目录ID
SET @pmsBusinessId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购业务' AND parent_id=@pmsParentId LIMIT 1);

-- =============================================
-- 在采购业务下添加供应商管理菜单
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商管理', @pmsBusinessId, 3, 'supplier', 'wms/supplier/index', '', 'PmsSupplier', 1, 0, 'C', '0', '0', 'wms:supplier:list', 'office-building', 'admin', sysdate(), '采购供应商管理');
SET @pmsSupplierId = LAST_INSERT_ID();

-- 添加供应商管理按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('供应商查询', @pmsSupplierId, 1, '', 'F', '0', '0', 'wms:supplier:query', 'admin', sysdate()),
('供应商新增', @pmsSupplierId, 2, '', 'F', '0', '0', 'wms:supplier:add', 'admin', sysdate()),
('供应商修改', @pmsSupplierId, 3, '', 'F', '0', '0', 'wms:supplier:edit', 'admin', sysdate()),
('供应商删除', @pmsSupplierId, 4, '', 'F', '0', '0', 'wms:supplier:remove', 'admin', sysdate()),
('供应商导出', @pmsSupplierId, 5, '', 'F', '0', '0', 'wms:supplier:export', 'admin', sysdate());

-- 给admin角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu 
WHERE parent_id = @pmsSupplierId 
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- 验证结果
SELECT '供应商管理已添加到采购管理模块' AS result;
SELECT m1.menu_name AS '二级目录', m2.menu_name AS '菜单', m2.icon, m2.perms
FROM sys_menu m1
JOIN sys_menu m2 ON m2.parent_id = m1.menu_id
WHERE m1.menu_name = '采购业务' AND m2.menu_type = 'C'
ORDER BY m2.order_num;
