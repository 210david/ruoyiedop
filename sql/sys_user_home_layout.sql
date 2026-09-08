-- ----------------------------
-- 用户门户首页布局配置表
-- 存储用户自定义的首页模块显示配置（模块编码逗号分隔）
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_home_layout`;
CREATE TABLE `sys_user_home_layout` (
  `user_id`      BIGINT(20)   NOT NULL                COMMENT '用户ID',
  `module_codes` VARCHAR(500) DEFAULT ''              COMMENT '首页显示模块编码，逗号分隔（空=显示全部）',
  `update_by`    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  DATETIME                             DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户门户首页布局配置表';
