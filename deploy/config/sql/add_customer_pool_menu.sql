-- ============================================
-- 客户公海池菜单添加脚本
-- 在"客户管理"目录下新增"公海池"子菜单（与"企业客户"同级）
-- ============================================

-- 1. 查找"客户管理"目录的菜单ID（parent_id=4000, path='customer', menu_type='M'）
SET @customerDirId = (SELECT menu_id FROM sys_menu WHERE path = 'customer' AND menu_type = 'M' AND parent_id = 4000 LIMIT 1);

-- 2. 插入"公海池"菜单（放在客户管理目录下，与企业客户同级）
INSERT INTO `sys_menu` (
    `menu_name`, `parent_id`, `order_num`, `path`, `component`,
    `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`,
    `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`
) VALUES (
    '公海池', @customerDirId, 5, 'customerPool', 'mk/customer/pool',
    '', 'MkCustomerPool', 1, 0, 'C',
    '0', '0', 'marketing:customer:list', 'coin', 'admin', NOW(), '客户公海池'
);

-- 3. 将菜单授权给超级管理员角色（role_id=1）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE menu_name = '公海池' AND component = 'mk/customer/pool';

-- 4. 查看插入结果
SELECT menu_id, menu_name, parent_id, path, component, perms FROM sys_menu WHERE menu_name = '公海池' AND component = 'mk/customer/pool';
