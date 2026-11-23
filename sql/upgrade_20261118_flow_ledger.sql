-- ============================================================
-- 流程台账（虚拟台账：按表单配置列 + 授权范围实时查询流程数据）
-- 1. 台账定义 flow_ledger
-- 2. 台账授权 flow_ledger_grant
-- 3. 流程实例扩展 flow_instance_ext（发起时快照，冻结发起部门）
-- 4. flow_data_* 补索引 + 历史实例回填
-- 5. 前台「流程中心 → 流程台账」、后台「流程管理 → 台账配置」菜单与按钮
-- 执行库：ry-vue
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 台账定义
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `flow_ledger` (
  `ledger_id`    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '台账ID',
  `ledger_key`   varchar(64)  NOT NULL                COMMENT '台账标识',
  `ledger_name`  varchar(100) NOT NULL                COMMENT '台账名称',
  `ledger_desc`  varchar(500) DEFAULT NULL            COMMENT '台账说明',
  `category`     varchar(50)  DEFAULT NULL            COMMENT '流程分类（字典 flowable_category）',
  `form_id`      bigint(20)   NOT NULL                COMMENT '绑定表单ID（flow_form.form_id）',
  `form_key`     varchar(64)  DEFAULT NULL            COMMENT '表单Key（flow_form.form_key）',
  `process_key`  varchar(128) DEFAULT NULL            COMMENT '流程定义Key',
  `table_name`   varchar(64)  DEFAULT NULL            COMMENT '表单数据表名（flow_data_xxx）',
  `columns_json` longtext                             COMMENT '列配置JSON [{field,label,source,width,visible,sortable,filterable,align,format}]',
  `query_json`   varchar(1000) DEFAULT NULL           COMMENT '查询配置JSON {defaultSort,defaultSortOrder}',
  `export_flag`  char(1)      DEFAULT '1'             COMMENT '是否允许导出（0否 1是）',
  `status`       char(1)      DEFAULT '0'             COMMENT '状态（0启用 1停用）',
  `order_num`    int(4)       DEFAULT 0               COMMENT '显示顺序',
  `del_flag`     char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  `create_by`    varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`       varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_ledger_key` (`ledger_key`),
  KEY `idx_form` (`form_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程台账定义';

-- ------------------------------------------------------------
-- 2. 台账授权
-- grant_type：授权对象类型 all/company/dept/role/user
-- scope_type：可见数据范围 all/self/dept/deptAndChild/company/companyAndChild/customDept/customUser
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `flow_ledger_grant` (
  `grant_id`    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  `ledger_id`   bigint(20)   NOT NULL                COMMENT '台账ID',
  `grant_type`  varchar(20)  NOT NULL                COMMENT '授权对象类型（all/company/dept/role/user）',
  `target_id`   bigint(20)   DEFAULT NULL            COMMENT '授权对象ID（company/dept/role/user）',
  `scope_type`  varchar(20)  NOT NULL DEFAULT 'self' COMMENT '数据范围类型（all/self/dept/deptAndChild/company/companyAndChild/customDept/customUser）',
  `scope_json`  varchar(2000) DEFAULT NULL           COMMENT '自定义范围JSON {deptIds:[],includeChild:true,userIds:[]}',
  `can_export`  char(1)      DEFAULT '0'             COMMENT '是否允许导出（0否 1是）',
  `create_by`   varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time` datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time` datetime     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`grant_id`),
  KEY `idx_ledger` (`ledger_id`),
  KEY `idx_grant` (`grant_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程台账授权';

-- ------------------------------------------------------------
-- 3. 流程实例扩展（发起时快照）
-- 说明：流程状态/结束时间不落本表，查询时实时取 ACT_HI_PROCINST，
--       避免办结、撤销、强制结束等多条链路漏同步导致状态漂移。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `flow_instance_ext` (
  `instance_id`   varchar(64)  NOT NULL           COMMENT '流程实例ID（ACT_HI_PROCINST.ID_）',
  `form_id`       bigint(20)   DEFAULT NULL       COMMENT '表单ID',
  `form_key`      varchar(64)  DEFAULT NULL       COMMENT '表单Key',
  `process_key`   varchar(128) DEFAULT NULL       COMMENT '流程定义Key',
  `business_key`  varchar(64)  DEFAULT NULL       COMMENT '业务主键（表单数据行ID）',
  `title`         varchar(500) DEFAULT NULL       COMMENT '流程标题（发起时快照）',
  `start_user_id` varchar(64)  DEFAULT NULL       COMMENT '发起人用户ID',
  `start_dept_id` bigint(20)   DEFAULT NULL       COMMENT '发起时部门ID（冻结，供数据权限使用）',
  `start_time`    datetime     DEFAULT NULL       COMMENT '发起时间',
  `create_time`   datetime     DEFAULT NULL       COMMENT '登记时间',
  PRIMARY KEY (`instance_id`),
  KEY `idx_form_time` (`form_key`, `start_time`),
  KEY `idx_dept` (`start_dept_id`),
  KEY `idx_user` (`start_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程实例扩展（发起快照）';

-- ------------------------------------------------------------
-- 4.1 flow_data_* 补索引：台账按流程实例/发起时间检索
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_flow_ledger_data_index`;
DELIMITER $$
CREATE PROCEDURE `sp_flow_ledger_data_index`()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE tname VARCHAR(64);
  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'flow\_data\_%' AND TABLE_TYPE = 'BASE TABLE';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO tname;
    IF done THEN LEAVE read_loop; END IF;
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tname AND COLUMN_NAME = 'process_instance_id')
       AND NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tname AND INDEX_NAME = 'idx_process_instance') THEN
      SET @s = CONCAT('ALTER TABLE `', tname, '` ADD INDEX `idx_process_instance` (`process_instance_id`)');
      PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tname AND COLUMN_NAME = 'create_time')
       AND NOT EXISTS (SELECT 1 FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tname AND INDEX_NAME = 'idx_create_time') THEN
      SET @s = CONCAT('ALTER TABLE `', tname, '` ADD INDEX `idx_create_time` (`create_time`)');
      PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;
CALL `sp_flow_ledger_data_index`();
DROP PROCEDURE IF EXISTS `sp_flow_ledger_data_index`;

-- ------------------------------------------------------------
-- 4.2 历史实例回填 flow_instance_ext（按表单数据表逐表回填，可重复执行）
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `sp_flow_ledger_ext_backfill`;
DELIMITER $$
CREATE PROCEDURE `sp_flow_ledger_ext_backfill`()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE tname VARCHAR(64);
  -- 引擎表流程实例ID列的实际字符集/排序规则（旧库常为 utf8mb3_bin），
  -- 关联时必须按此转换，否则会报 COLLATION is not valid for CHARACTER SET
  DECLARE ext_charset VARCHAR(32) DEFAULT 'utf8mb4';
  DECLARE ext_collation VARCHAR(32) DEFAULT 'utf8mb4_general_ci';
  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 'flow\_data\_%' AND TABLE_TYPE = 'BASE TABLE'
       AND TABLE_NAME NOT LIKE '%\_dt%';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
  SELECT IFNULL(MAX(CHARACTER_SET_NAME), 'utf8mb4'), IFNULL(MAX(COLLATION_NAME), 'utf8mb4_general_ci')
    INTO ext_charset, ext_collation
    FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ACT_HI_PROCINST' AND COLUMN_NAME = 'ID_';
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO tname;
    IF done THEN LEAVE read_loop; END IF;
    BEGIN
      DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
      SET @s = CONCAT(
        'INSERT IGNORE INTO flow_instance_ext ',
        '(instance_id, form_id, form_key, process_key, business_key, title, start_user_id, start_dept_id, start_time, create_time) ',
        'SELECT p.ID_, f.form_id, f.form_key, SUBSTRING_INDEX(p.PROC_DEF_ID_, '':'', 1), p.BUSINESS_KEY_, p.NAME_, ',
        '       p.START_USER_ID_, u.dept_id, p.START_TIME_, NOW() ',
        '  FROM `', tname, '` d ',
        '  JOIN flow_form f ON f.table_name = ''', tname, ''' AND f.del_flag = ''0'' ',
        '  JOIN ACT_HI_PROCINST p ON p.ID_ = CONVERT(d.process_instance_id USING ', ext_charset, ') COLLATE ', ext_collation, ' ',
        '  LEFT JOIN sys_user u ON u.user_id = CAST(p.START_USER_ID_ AS UNSIGNED) ',
        ' WHERE d.process_instance_id IS NOT NULL');
      PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;
CALL `sp_flow_ledger_ext_backfill`();
DROP PROCEDURE IF EXISTS `sp_flow_ledger_ext_backfill`;

-- ------------------------------------------------------------
-- 5. 菜单与按钮权限
--    前台：流程中心（905824） → 流程台账
--    后台：流程管理（905829） → 台账配置
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 9064600 AND 9064699;
DELETE FROM sys_menu WHERE menu_id BETWEEN 9064600 AND 9064699;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(9064600, '流程台账', 905824, 5, 'ledger', 'flowable/ledger/portal', NULL, 'FlowLedgerPortal', 1, 0, 'C', '0', '0', 'flowable:ledger:view', 'table', 'admin', sysdate(), '按流程表单配置的台账视图');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(9064610, '台账配置', 905829, 6, 'ledgerConfig', 'flowable/ledger/index', NULL, 'FlowLedgerConfig', 1, 0, 'C', '0', '0', 'flowable:ledger:list', 'table', 'admin', sysdate(), '流程台账列字段与数据查询权限配置');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES
(9064611, '台账查询', 9064610, 1, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'flowable:ledger:query',  '#', 'admin', sysdate()),
(9064612, '台账新增', 9064610, 2, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'flowable:ledger:add',    '#', 'admin', sysdate()),
(9064613, '台账修改', 9064610, 3, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'flowable:ledger:edit',   '#', 'admin', sysdate()),
(9064614, '台账删除', 9064610, 4, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'flowable:ledger:remove', '#', 'admin', sysdate()),
(9064615, '授权配置', 9064610, 5, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'flowable:ledger:grant',  '#', 'admin', sysdate());

-- admin 角色授权（已存在的跳过）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
 WHERE menu_id BETWEEN 9064600 AND 9064699
   AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);
