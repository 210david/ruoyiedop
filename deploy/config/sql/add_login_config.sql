-- 添加系统参数：多终端登录开关 + 会话超时时长
-- 多终端登录开关：默认 true 允许，false 不允许（新登录踢出旧会话）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('系统安全-多终端登录开关', 'sys.login.multiLoginEnabled', 'true', 'Y', 'admin', NOW(), '是否允许同一账号多终端同时登录（true允许，false不允许，不允许时新登录会踢出旧会话）')
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name), remark = VALUES(remark);

-- 会话超时时长：默认 120 分钟 = 2 小时，活跃用户自动续期
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('系统安全-会话超时时长', 'sys.token.expireTime', '120', 'Y', 'admin', NOW(), '令牌有效期（分钟），默认120分钟=2小时，活跃用户自动续期。修改后需刷新缓存生效')
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name), remark = VALUES(remark);
