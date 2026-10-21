-- ============================================================
-- 上传路径配置项（system.upload.path）
-- 用途：支持在管理端页面修改文件上传根目录，改完即时生效
-- 说明：优先级 环境变量 EDOP_UPLOAD_PATH > 本配置项 > application.yml 默认值
-- 幂等：重复执行不会产生重复记录
-- ============================================================

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '文件上传根目录', 'system.upload.path', 'D:/EDOP/uploadPath', 'Y', 'admin', NOW(),
       '文件上传根目录（优先级：环境变量 EDOP_UPLOAD_PATH > 本项 > application.yml）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system.upload.path');
