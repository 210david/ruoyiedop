-- sys_message 表增加接收角色权限标识字段
ALTER TABLE `sys_message` ADD COLUMN `recipient_role_key` varchar(100) DEFAULT NULL COMMENT '接收角色权限标识（为空则全员可见，否则只有拥有该权限的角色对应用户可见）';
