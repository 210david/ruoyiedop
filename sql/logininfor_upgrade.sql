-- ----------------------------
-- 登录日志表增加登录设备类型字段
-- ----------------------------
ALTER TABLE `sys_logininfor` ADD COLUMN `login_device` varchar(20) DEFAULT '' COMMENT '登录设备类型（PC/手机/平板）' AFTER `os`;
