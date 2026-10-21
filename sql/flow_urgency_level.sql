-- ----------------------------
-- 紧急程度配置表（P0 改造：从硬编码改为可配置）
-- 背景：参考泛微 E9 workflow_customlevel（默认名/自定义名/排序/封存）
--       系统内置字段 urgency 的选项由本表驱动，企业可自定义档位名称与顺序
-- ----------------------------
DROP TABLE IF EXISTS flow_urgency_level;
CREATE TABLE flow_urgency_level (
  level_id      bigint       NOT NULL AUTO_INCREMENT           COMMENT '主键',
  level_value   varchar(20)  NOT NULL                          COMMENT '档位值（存业务表 urgency 列）',
  default_name  varchar(20)  NOT NULL                          COMMENT '系统默认名（普通/紧急/特急）',
  custom_name   varchar(100) DEFAULT NULL                      COMMENT '用户自定义名（为空则用 default_name）',
  show_order    int          DEFAULT 0                         COMMENT '排序（越小越靠前）',
  is_sealed     char(1)      DEFAULT '0'                       COMMENT '是否封存：0否 1是（封存后新发起不可选）',
  is_remind     char(1)      DEFAULT '0'                       COMMENT '是否触发提醒分级：0否 1是',
  status        char(1)      DEFAULT '0'                       COMMENT '状态：0正常 1停用',
  create_by     varchar(64)  DEFAULT ''                        COMMENT '创建者',
  create_time   datetime     DEFAULT NULL                      COMMENT '创建时间',
  update_by     varchar(64)  DEFAULT ''                        COMMENT '更新者',
  update_time   datetime     DEFAULT NULL                      COMMENT '更新时间',
  PRIMARY KEY (level_id),
  UNIQUE KEY uk_level_value (level_value),
  KEY idx_show_order (show_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='紧急程度配置（驱动系统字段 urgency 选项）';

-- 初始化数据：与改造前的硬编码档位保持一致，保证平滑迁移
INSERT INTO flow_urgency_level (level_value, default_name, custom_name, show_order, is_sealed, is_remind, status, create_by, create_time)
VALUES ('1', '普通', NULL, 1, '0', '0', '0', 'admin', NOW()),
       ('2', '紧急', NULL, 2, '0', '1', '0', 'admin', NOW()),
       ('3', '特急', NULL, 3, '0', '1', '0', 'admin', NOW());

-- ----------------------------
-- 回退语句（如需还原）
-- ----------------------------
-- DROP TABLE IF EXISTS flow_urgency_level;
