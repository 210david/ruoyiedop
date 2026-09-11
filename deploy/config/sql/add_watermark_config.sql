-- 添加系统参数：页面水印开关
-- 默认关闭(false)，开启后所有页面显示当前登录用户名水印
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('系统安全-页面水印开关', 'sys.index.watermarkEnabled', 'false', 'Y', 'admin', NOW(), '是否启用页面水印功能（true开启，false关闭），开启后所有页面显示当前用户名水印')
ON DUPLICATE KEY UPDATE config_name = VALUES(config_name), remark = VALUES(remark);
