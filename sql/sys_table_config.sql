-- =============================================
-- 表格列配置表（列宽 + 列显隐）
-- 支持管理员全局配置 + 用户个人配置，优先级：个人 > 全局 > 默认
-- =============================================

DROP TABLE IF EXISTS `sys_table_config`;
CREATE TABLE `sys_table_config` (
    `config_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `table_key`     VARCHAR(100)  NOT NULL                 COMMENT '页面标识（如 pms_eval_index）',
    `col_prop`      VARCHAR(100)  NOT NULL                 COMMENT '列属性名（如 supplierName）',
    `col_width`     INT(11)       DEFAULT NULL             COMMENT '列宽（像素），NULL表示未设置',
    `col_visible`   TINYINT(1)    DEFAULT NULL             COMMENT '列是否可见：1=可见，0=隐藏，NULL=未设置',
    `config_type`   CHAR(1)       DEFAULT 'U'              COMMENT '配置类型：G=全局（管理员设置），U=个人（用户自定义）',
    `user_id`       BIGINT(20)    DEFAULT NULL             COMMENT '用户ID（config_type=U时有效，G时为NULL）',
    `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
    `create_time`   DATETIME      DEFAULT NULL             COMMENT '创建时间',
    `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
    `update_time`   DATETIME      DEFAULT NULL             COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_table_col_type_user` (`table_key`, `col_prop`, `config_type`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表格列配置表（列宽 + 列显隐）';
