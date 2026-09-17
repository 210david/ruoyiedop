-- ============================================================
-- 清理系统参数中的冗余AI配置键（2026-09-17）
-- 背景：
--   1. ai.* 新键已由AI中心/AI配置页面接管，dms.ai.* 基础5键（enabled/
--      api_url/api_key/model_name/monthly_limit）为迁移遗留旧键，删除。
--   2. NL2Query 已改为"前缀放行 - 黑名单(ai.nl2query.blockedTables)"模式，
--      旧显式白名单键 ai.nl2query.tables 仅剩兜底逻辑（代码已同步移除），删除。
--   3. 以下键确认仍在使用，不做删除：
--      pms.enterprise.lookup.*（PMS企业信息查询设置页后端存储）
--      ai.embedding_url / ai.embedding_key / ai.embedding_model（AI中心Embedding配置）
--      ai.nl2query.enabled / ai.nl2query.blockedTables（NL2Query开关与黑名单）
--      dms.ai.repair_assistant / dispatch_recommend / knowledge_qa（DMS功能级开关）
-- 注意：执行后需重启服务或清空 sys_config 缓存使Redis缓存失效。
-- ============================================================

-- 1. 兜底迁移：确保 ai.* 新键已有值（空值时从旧键回填，避免删旧键后配置丢失）
UPDATE sys_config new_t
JOIN sys_config old_t ON old_t.config_key = CONCAT('dms.', new_t.config_key)
SET new_t.config_value = old_t.config_value
WHERE new_t.config_key IN ('ai.enabled','ai.api_url','ai.api_key','ai.model_name','ai.monthly_limit')
  AND (new_t.config_value IS NULL OR new_t.config_value = '')
  AND old_t.config_value IS NOT NULL AND old_t.config_value <> '';

-- 2. 删除 dms.ai.* 基础5键（迁移遗留，前台配置页已统一写 ai.* 键）
DELETE FROM sys_config WHERE config_key IN (
    'dms.ai.enabled',
    'dms.ai.api_url',
    'dms.ai.api_key',
    'dms.ai.model_name',
    'dms.ai.monthly_limit'
);

-- 3. 删除 NL2Query 旧白名单键（已改为黑名单模式）
DELETE FROM sys_config WHERE config_key = 'ai.nl2query.tables';

-- 4. 核对剩余相关键（人工确认用）
SELECT config_key, config_name, config_value
FROM sys_config
WHERE config_key LIKE 'ai.%' OR config_key LIKE 'dms.ai.%' OR config_key LIKE 'pms.enterprise.lookup%'
ORDER BY config_key;
