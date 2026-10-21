-- 添加企查查 SecretKey 配置项
-- 执行此脚本添加企业查询 API Secret 配置

-- 先检查是否已存在
DELETE FROM sys_config WHERE config_key = 'pms.enterprise.lookup.api.secret';

-- 添加 SecretKey 配置项
INSERT INTO sys_config (
    config_name, 
    config_key, 
    config_value, 
    config_type, 
    create_by, 
    create_time, 
    update_by, 
    update_time, 
    remark
) VALUES (
    '企业查询API Secret', 
    'pms.enterprise.lookup.api.secret', 
    '', 
    'Y', 
    'admin', 
    NOW(), 
    'admin', 
    NOW(), 
    '企查查API SecretKey，用于计算Token鉴权'
);

-- 查看配置是否添加成功
SELECT config_id, config_name, config_key, config_value, config_type, remark 
FROM sys_config 
WHERE config_key LIKE 'pms.enterprise.lookup%'
ORDER BY config_key;
