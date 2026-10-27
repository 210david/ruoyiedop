-- 价格比价菜单
-- 在采购管理目录下新增"价格比价"菜单

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 获取当前最大排序号
SET @maxSort = (SELECT IFNULL(MAX(order_num), 0) FROM sys_menu WHERE parent_id = @pmsParentId);

-- 插入价格比价菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('价格比价', @pmsParentId, @maxSort + 1, 'priceCompare', 'pms/priceCompare/index', '', 1, 0, 'C', '0', '0', 'pms:priceCompare:list', 'chart', 'admin', sysdate(), '价格比价');

-- 获取刚插入的菜单ID
SET @menuId = LAST_INSERT_ID();

-- 插入按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
('价格比价查询', @menuId, 1, '', '', '', 1, 0, 'F', '0', '0', 'pms:priceCompare:query', '#', 'admin', sysdate(), ''),
('价格比价导出', @menuId, 2, '', '', '', 1, 0, 'F', '0', '0', 'pms:priceCompare:export', '#', 'admin', sysdate(), '');

-- 为admin角色(role_id=1)授权
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @menuId);
SET @btnId1 = @menuId + 1;
SET @btnId2 = @menuId + 2;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @btnId1);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, @btnId2);

SELECT '价格比价菜单创建完成' AS result;
