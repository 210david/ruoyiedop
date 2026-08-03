-- 企业信息查询配置：默认配置项 + 菜单

-- 1. 插入默认配置到 sys_config
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '是否启用企业信息查询', 'pms.enterprise.lookup.enabled', 'false', 'Y', 'admin', sysdate(), '企业信息查询功能开关'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'pms.enterprise.lookup.enabled');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业查询API提供商', 'pms.enterprise.lookup.provider', 'tianyancha', 'Y', 'admin', sysdate(), 'tianyancha=天眼查 qichacha=企查查 custom=自定义'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'pms.enterprise.lookup.provider');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业查询API地址', 'pms.enterprise.lookup.api.url', '', 'Y', 'admin', sysdate(), '第三方企业信息API地址'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'pms.enterprise.lookup.api.url');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业查询API密钥', 'pms.enterprise.lookup.api.key', '', 'Y', 'admin', sysdate(), '第三方企业信息API密钥'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'pms.enterprise.lookup.api.key');

-- 2. 添加菜单：企业查询配置（放在采购管理下，编号规则旁边）
-- 先查找采购管理父菜单ID
SET @pms_parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '采购管理' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '企业查询配置', @pms_parent_id, 98, 'enterpriseConfig', 'pms/enterpriseConfig/index', 1, 0, 'C', '0', '0', 'pms:enterprise:config', 'tool', 'admin', sysdate(), '企业信息查询API配置'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '企业查询配置' AND parent_id = @pms_parent_id);

-- 获取新菜单ID
SET @config_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '企业查询配置' AND parent_id = @pms_parent_id LIMIT 1);

-- 3. 添加按钮权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, @config_menu_id
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 2 AND menu_id = @config_menu_id);
