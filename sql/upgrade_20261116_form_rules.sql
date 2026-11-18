-- ============================================================
-- 流程表单规则引擎升级脚本
-- 日期：2026-11-16
-- 内容：
--   1. flow_form 增加 ext_json（表单规则/脚本）
--   2. 新增权限 flowable:form:rule（规则配置 / AI 生成）
--   3. 新增 AI 功能开关 ai.feature.flowable.form_rules
-- 说明：可重复执行
-- ============================================================

-- 1. ext_json 字段
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'flow_form' AND COLUMN_NAME = 'ext_json'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE flow_form ADD COLUMN ext_json MEDIUMTEXT NULL COMMENT ''表单扩展JSON（规则/脚本）'' AFTER layout_json',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 规则配置权限（挂在「表单管理」菜单下）
SET @form_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:form:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '规则配置', @form_menu, 6, '#', '', 1, 0, 'F', '0', '0',
       'flowable:form:rule', '#', 'admin', sysdate(), '表单规则配置与 AI 规则生成'
FROM DUAL
WHERE @form_menu IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'flowable:form:rule');

-- 授权给拥有表单修改权限的角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id
FROM sys_role_menu rm
JOIN sys_menu m ON m.perms = 'flowable:form:rule'
WHERE rm.menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'flowable:form:edit' LIMIT 1)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = m.menu_id
  );

-- 3. AI 规则生成功能开关（复用统一 AiClient；全局 ai.enabled 关闭时不生效）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '表单规则AI生成开关', 'ai.feature.flowable.form_rules', 'true', 'Y', 'admin', sysdate(),
       '自然语言生成表单规则（设计态，结果需人工确认）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'ai.feature.flowable.form_rules');
