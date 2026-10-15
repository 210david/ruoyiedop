-- ============================================================
-- P0-批次三：谱系追溯报告 + 召回隔离 + mms领退料批次化 + 存量迁移
-- 环境：仅传丰CFSP（RUOYI_CFSP），永不同步标准版
-- 执行前请先执行备份语句（见文件末尾）
-- ============================================================

-- 1. 召回单主表
CREATE TABLE IF NOT EXISTS `food_recall` (
  `recall_id`         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '召回ID',
  `recall_code`       varchar(64)  NOT NULL                COMMENT '召回单号',
  `trigger_batch_id`  bigint(20)   NOT NULL                COMMENT '触发批次ID',
  `trigger_batch_code` varchar(64) DEFAULT NULL            COMMENT '触发批次号',
  `recall_scope`      char(1)      NOT NULL DEFAULT '3'    COMMENT '召回范围（1正向 2反向 3双向）',
  `recall_reason`     varchar(500) NOT NULL                COMMENT '召回原因',
  `recall_level`      char(1)      DEFAULT '2'             COMMENT '召回等级（1一般 2重要 3紧急）',
  `affected_count`    int(11)      DEFAULT 0               COMMENT '圈批数量',
  `status`            char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0待执行 1已执行 2已取消）',
  `execute_time`      datetime     DEFAULT NULL            COMMENT '执行时间',
  `execute_by`        varchar(64)  DEFAULT ''              COMMENT '执行人',
  `del_flag`          char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  `create_by`         varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`       datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`         varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`       datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`            varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`recall_id`),
  KEY `idx_recall_trigger` (`trigger_batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次召回单';

-- 2. 召回圈批明细表
CREATE TABLE IF NOT EXISTS `food_recall_batch` (
  `rb_id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `recall_id`      bigint(20)  NOT NULL                COMMENT '召回ID',
  `batch_id`       bigint(20)  NOT NULL                COMMENT '批次ID',
  `batch_code`     varchar(64) DEFAULT NULL            COMMENT '批次号',
  `material_id`    bigint(20)  DEFAULT NULL            COMMENT '物料ID',
  `depth`          int(11)     DEFAULT 0               COMMENT '谱系深度（0=触发批次本身）',
  `relation_path`  varchar(500) DEFAULT NULL           COMMENT '关系路径说明',
  `prev_status`    char(1)     DEFAULT NULL            COMMENT '冻结前批次状态',
  `frozen_flag`    char(1)     DEFAULT '1'             COMMENT '是否本次冻结（1是 0执行前已是冻结）',
  `create_time`    datetime    DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (`rb_id`),
  KEY `idx_rb_recall` (`recall_id`),
  KEY `idx_rb_batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品召回圈批明细';

-- 3. 退料明细加批次号（退料批次回归，效期不重算=沿用原批次）
ALTER TABLE `mms_return_detail`
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号（须与领料批次一致）' AFTER `return_qty`;

-- 4. 菜单：召回隔离（挂批次管理 906210 下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('召回隔离', 906210, 5, 'recall', 'food/recall/index', 1, 0, 'C', '0', '0', 'food:recall:list', 'warning', 'admin', NOW(), 'P0批次三：一键按谱系圈批冻结');

SET @recall_menu_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('召回查询', @recall_menu_id, 1, 'F', '0', '0', 'food:recall:query',  '#', 'admin', NOW(), '召回隔离按钮'),
('召回新增', @recall_menu_id, 2, 'F', '0', '0', 'food:recall:add',    '#', 'admin', NOW(), '召回隔离按钮'),
('召回执行', @recall_menu_id, 3, 'F', '0', '0', 'food:recall:execute','#', 'admin', NOW(), '召回隔离按钮'),
('召回取消', @recall_menu_id, 4, 'F', '0', '0', 'food:recall:cancel', '#', 'admin', NOW(), '召回隔离按钮'),
('召回导出', @recall_menu_id, 5, 'F', '0', '0', 'food:recall:export', '#', 'admin', NOW(), '召回隔离按钮');

-- 5. 超管角色授权（角色2=超级管理员按需自行分配；此处仅建菜单）

-- ============================================================
-- 迁移前备份（上线迁移要求：迁移前全量备份）
-- ============================================================
-- DROP TABLE IF EXISTS food_migrate_backup_inventory;
-- CREATE TABLE food_migrate_backup_inventory AS SELECT * FROM wms_inventory;
-- DROP TABLE IF EXISTS food_migrate_backup_batch;
-- CREATE TABLE food_migrate_backup_batch AS SELECT * FROM food_batch;
-- DROP TABLE IF EXISTS food_migrate_backup_batch_stock;
-- CREATE TABLE food_migrate_backup_batch_stock AS SELECT * FROM food_batch_stock;

-- 验证
SELECT COUNT(*) recall_tbl FROM information_schema.tables WHERE table_schema='RUOYI_CFSP' AND table_name='food_recall';
SELECT COUNT(*) rb_tbl FROM information_schema.tables WHERE table_schema='RUOYI_CFSP' AND table_name='food_recall_batch';
SHOW COLUMNS FROM mms_return_detail LIKE 'batch_no';
