-- ================================================================
-- 开放接口文档 - 菜单初始化脚本
-- 功能：在"系统管理"目录下新增"开放接口文档"子菜单，展示对外Open API接口文档
-- 说明：该页面支持免登录独立访问（URL: /openapi-doc），同时也作为系统管理子菜单供登录用户使用
-- 执行：在 MySQL 中执行本脚本即可
-- ================================================================

-- 1. 新增开放接口文档菜单（挂在"系统管理"目录下，parent_id=1）
-- 使用 menu_id=120（118=接口文档、119=开放接口(API Key管理)已占用）
-- path=openapidoc，component=tool/openapidoc/index，登录后访问路径为 /system/openapidoc
INSERT INTO sys_menu VALUES
('120', '开放接口文档', '1', '9', 'openapidoc', 'tool/openapidoc/index', '', '', 1, 0, 'C', '0', '0', 'tool:openapidoc:list', 'guide', 'admin', sysdate(), '', NULL, '开放接口(Open API)文档，供外部系统对接（免登录可访问 /openapi-doc）');

-- 2. 为超管角色(admin, role_id=1)分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 120);

-- ================================================================
-- 如需回滚，执行以下语句：
-- DELETE FROM sys_role_menu WHERE menu_id = 120;
-- DELETE FROM sys_menu WHERE menu_id = 120;
-- ================================================================
