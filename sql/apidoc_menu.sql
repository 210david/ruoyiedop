-- ================================================================
-- 业务模块接口文档 - 菜单初始化脚本
-- 功能：在"系统管理"目录下新增"接口文档"子菜单，集中展示WMS和DMS全接口
-- 说明：该页面支持免登录独立访问（URL: /apidoc），同时也作为系统管理子菜单供登录用户使用
-- 执行：在 MySQL 中执行本脚本即可
-- ================================================================

-- 1. 新增接口文档菜单（挂在"系统管理"目录下，parent_id=1）
-- 使用 menu_id=118（116=代码生成、117=系统接口已占用）
-- path=apidoc，component=tool/apidoc/index，登录后访问路径为 /system/apidoc
INSERT INTO sys_menu VALUES
('118', '接口文档', '1', '8', 'apidoc', 'tool/apidoc/index', '', '', 1, 0, 'C', '0', '0', 'tool:apidoc:list', 'documentation', 'admin', sysdate(), '', NULL, '业务模块接口文档集中管理（免登录可访问 /apidoc）');

-- 2. 为超管角色(admin, role_id=1)分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 118);

-- ================================================================
-- 如需回滚，执行以下语句：
-- DELETE FROM sys_role_menu WHERE menu_id = 118;
-- DELETE FROM sys_menu WHERE menu_id = 118;
-- ================================================================
