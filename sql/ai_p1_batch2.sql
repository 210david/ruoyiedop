-- =============================================
-- AI P1第二批：提示词模板管理 + NL2Query - SQL脚本
-- 遵循：业务零侵入——只新增
-- =============================================

-- =============================================
-- 一、配置键
-- =============================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'NL2Query功能开关', 'ai.nl2query.enabled', 'false', 'Y', 'admin', sysdate(), '自然语言查数开关，默认关闭，开启前需配置白名单表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.nl2query.enabled');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'NL2Query白名单表', 'ai.nl2query.tables', '', 'Y', 'admin', sysdate(), '允许自然语言查询的表，逗号分隔，如 dms_work_order,dms_equipment'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.nl2query.tables');

-- =============================================
-- 二、菜单权限（AI中心下新增"提示词模板"与"智能查询"）
-- =============================================

-- 提示词模板
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '提示词模板', m.menu_id, 5, 'prompt', 'ai/prompt/index', 'AiPrompt',
    1, 0, 'C', '0', '0', 'ai:prompt:list', 'form', 'admin', sysdate()
FROM sys_menu m WHERE m.menu_name='AI中心' AND m.parent_id=0
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='ai:prompt:list');

SET @menuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('提示词查询', @menuId, 1, '', 'F', '0', '0', 'ai:prompt:query', 'admin', sysdate()),
('提示词修改', @menuId, 2, '', 'F', '0', '0', 'ai:prompt:edit',  'admin', sysdate());

-- 智能查询（NL2Query）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '智能查询', m.menu_id, 6, 'nlquery', 'ai/nlquery/index', 'AiNlQuery',
    1, 0, 'C', '0', '0', 'ai:nlquery:query', 'search', 'admin', sysdate()
FROM sys_menu m WHERE m.menu_name='AI中心' AND m.parent_id=0
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='ai:nlquery:query');

-- 给admin角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE (perms LIKE 'ai:prompt%' OR perms LIKE 'ai:nlquery%')
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);
