-- =============================================
-- AI 公共模块（ruoyi-ai）+ AI中心 - 全量SQL脚本
-- 包含：审计表 + 配置键迁移（平移，旧键保留）+ AI中心一级菜单
-- 遵循：业务零侵入红线——只新增，不修改任何现有表/菜单/权限
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- =============================================

-- 1. AI调用审计日志表
DROP TABLE IF EXISTS ai_call_log;
CREATE TABLE ai_call_log (
    log_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    module        VARCHAR(32)  NOT NULL                 COMMENT '业务模块（dms/qms/pms/mms/wms/mk/hr/safety/platform）',
    feature       VARCHAR(64)  NOT NULL                 COMMENT '功能标识',
    user_id       BIGINT                                COMMENT '操作人ID',
    user_name     VARCHAR(64)                           COMMENT '操作人名称',
    status        CHAR(1)      DEFAULT '0'              COMMENT '状态（0成功 1降级 2失败 3限额拦截）',
    duration_ms   INT                                   COMMENT '耗时（毫秒）',
    prompt_digest VARCHAR(500)                          COMMENT '提示词摘要（脱敏后前500字）',
    error_msg     VARCHAR(500)                          COMMENT '错误信息',
    call_time     DATETIME                              COMMENT '调用时间',
    create_by     VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time   DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (log_id),
    KEY idx_module_time (module, call_time),
    KEY idx_user_time (user_id, call_time),
    KEY idx_call_time (call_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用审计日志表';

-- =============================================
-- 二、配置键迁移（dms.ai.* → ai.*，新键INSERT，旧键保留一个版本周期）
-- =============================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI功能总开关', 'ai.enabled', IFNULL(config_value,'true'), 'Y', 'admin', sysdate(), '从dms.ai.enabled平移'
FROM sys_config WHERE config_key='dms.ai.enabled'
AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM sys_config WHERE config_key='ai.enabled') t);

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI大模型API地址', 'ai.api_url', config_value, 'Y', 'admin', sysdate(), '从dms.ai.api_url平移'
FROM sys_config WHERE config_key='dms.ai.api_url'
AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM sys_config WHERE config_key='ai.api_url') t);

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI大模型API密钥', 'ai.api_key', config_value, 'Y', 'admin', sysdate(), '从dms.ai.api_key平移'
FROM sys_config WHERE config_key='dms.ai.api_key'
AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM sys_config WHERE config_key='ai.api_key') t);

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI大模型名称', 'ai.model_name', config_value, 'Y', 'admin', sysdate(), '从dms.ai.model_name平移'
FROM sys_config WHERE config_key='dms.ai.model_name'
AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM sys_config WHERE config_key='ai.model_name') t);

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI月度调用上限', 'ai.monthly_limit', IFNULL(config_value,'1000'), 'Y', 'admin', sysdate(), '从dms.ai.monthly_limit平移'
FROM sys_config WHERE config_key='dms.ai.monthly_limit'
AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM sys_config WHERE config_key='ai.monthly_limit') t);

-- 无对应旧键时补建默认值
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'AI功能总开关', 'ai.enabled', 'true', 'Y', 'admin', sysdate(), 'AI平台级总开关'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key='ai.enabled');

-- =============================================
-- 三、菜单权限（AI中心一级菜单，与业务模块同级；仅INSERT）
-- =============================================

-- 一级菜单（M=目录）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, icon, create_by, create_time)
VALUES ('AI中心', 0, 90, 'ai', NULL, '', 1, 0, 'M', '0', '0', 'skill', 'admin', sysdate());

SET @parentId = LAST_INSERT_ID();

-- 二级菜单：AI配置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('AI配置', @parentId, 1, 'config', 'ai/config/index', 'AiConfig',
    1, 0, 'C', '0', '0', 'ai:center:config', 'edit', 'admin', sysdate());

SET @menuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('AI配置查看', @menuId, 1, '', 'F', '0', '0', 'ai:center:config:view', 'admin', sysdate());

-- 二级菜单：用量统计
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('用量统计', @parentId, 2, 'usage', 'ai/usage/index', 'AiUsage',
    1, 0, 'C', '0', '0', 'ai:center:dashboard', 'chart', 'admin', sysdate());

-- 二级菜单：调用日志
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES ('调用日志', @parentId, 3, 'log', 'ai/log/index', 'AiLog',
    1, 0, 'C', '0', '0', 'ai:center:log:list', 'log', 'admin', sysdate());

SET @menuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('调用日志查询', @menuId, 1, '', 'F', '0', '0', 'ai:center:log:query', 'admin', sysdate());

-- 给admin角色分配权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);
