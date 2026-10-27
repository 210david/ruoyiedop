-- =====================================================================
-- PDM 产品设计模块 第三批脚本（pdm_batch3.sql）
-- 范围：技术变更（变更单+涉及资料明细）+ BOM下发（EBOM→MBOM 下发记录）
--       + mms 侧 receiveMbom 接收服务（D1 决策：mms 纯新增，pdm 依赖 mms）
-- 四段式：一、业务表 DDL；二、字典；三、菜单权限；四、备注
-- 编号规则 pdm_change_order 已随 pdm_all.sql 初始化，此处防重兜底
-- 可重复执行：DROP TABLE IF EXISTS + WHERE NOT EXISTS 防重
-- =====================================================================

-- =====================================================================
-- 一、业务表 DDL
-- =====================================================================

-- 1. 技术变更单表（事务单据类：带 dept_id，BizDeptFillAspect 自动填充，@DataScope 过滤）
-- 状态机：0草稿 → 1审批中 → 2待执行 → 3执行中 → 4已关闭；审批驳回 → 5已驳回（可改后重新提交）
DROP TABLE IF EXISTS `pdm_change_order`;
CREATE TABLE `pdm_change_order` (
  `change_id`      BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '变更单ID',
  `change_no`      VARCHAR(64)   DEFAULT ''              COMMENT '变更单编号（pdm_change_order 规则自动生成）',
  `change_name`    VARCHAR(128)  NOT NULL                COMMENT '变更标题',
  `product_id`     BIGINT(20)    NOT NULL                COMMENT '变更产品档案ID',
  `product_code`   VARCHAR(64)   DEFAULT ''              COMMENT '产品编码（冗余）',
  `product_name`   VARCHAR(128)  DEFAULT ''              COMMENT '产品名称（冗余）',
  `change_level`   VARCHAR(10)   DEFAULT '1'             COMMENT '变更级别（字典 pdm_change_level：1=一般变更,2=重大变更）',
  `urgent_level`   VARCHAR(10)   DEFAULT '1'             COMMENT '紧急程度（字典 pdm_urgent_level：1=普通,2=紧急,3=特急）',
  `change_content` VARCHAR(2000) DEFAULT ''              COMMENT '变更内容说明',
  `change_reason`  VARCHAR(1000) DEFAULT ''              COMMENT '变更原因',
  `effect_analysis` VARCHAR(1000) DEFAULT ''             COMMENT '影响分析',
  `change_status`  VARCHAR(10)   DEFAULT '0'             COMMENT '变更状态（字典 pdm_change_status：0=草稿,1=审批中,2=待执行,3=执行中,4=已关闭,5=已驳回）',
  `submit_time`    DATETIME      DEFAULT NULL            COMMENT '提交审批时间',
  `audit_by`       VARCHAR(64)   DEFAULT ''              COMMENT '审批人',
  `audit_time`     DATETIME      DEFAULT NULL            COMMENT '审批时间',
  `audit_opinion`  VARCHAR(500)  DEFAULT ''              COMMENT '审批意见',
  `close_time`     DATETIME      DEFAULT NULL            COMMENT '关闭时间',
  `close_by`       VARCHAR(64)   DEFAULT ''              COMMENT '关闭人',
  `dept_id`        BIGINT(20)    DEFAULT NULL            COMMENT '部门ID（数据权限，BizDeptFillAspect 自动填充）',
  `del_flag`       CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除；进入流程后记录不可删除）',
  `create_by`      VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    DATETIME                              COMMENT '创建时间',
  `update_by`      VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`    DATETIME                              COMMENT '更新时间',
  `remark`         VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`change_id`),
  UNIQUE KEY `uk_pdm_change_no` (`change_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`change_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术变更单表';

-- 2. 变更涉及资料明细表（勾选自产品当前生效版本清单；执行阶段按变更单升版生效）
DROP TABLE IF EXISTS `pdm_change_order_item`;
CREATE TABLE `pdm_change_order_item` (
  `item_id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `change_id`       BIGINT(20)   NOT NULL                COMMENT '变更单ID',
  `doc_type`        VARCHAR(10)  DEFAULT '1'             COMMENT '资料类型（字典 pdm_doc_type：1=设计方案,2=图纸文件,3=设计BOM）',
  `biz_id`          BIGINT(20)   NOT NULL                COMMENT '资料ID',
  `biz_no`          VARCHAR(64)  DEFAULT ''              COMMENT '资料编号（冗余）',
  `biz_name`        VARCHAR(128) DEFAULT ''              COMMENT '资料名称（冗余）',
  `current_version` VARCHAR(32)  DEFAULT ''              COMMENT '发起变更时生效版本号',
  `new_version`     VARCHAR(32)  DEFAULT ''              COMMENT '升版后生效版本号（执行时回填）',
  `item_status`     VARCHAR(10)  DEFAULT '0'             COMMENT '明细状态（字典 pdm_change_item_status：0=待执行,1=已升版生效）',
  `effect_time`     DATETIME     DEFAULT NULL            COMMENT '升版生效时间',
  `effect_by`       VARCHAR(64)  DEFAULT ''              COMMENT '生效操作人',
  `order_num`       INT(4)       DEFAULT 0               COMMENT '显示顺序',
  `remark`          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_change_id` (`change_id`),
  KEY `idx_biz` (`doc_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技术变更涉及资料明细表';

-- 3. BOM下发记录表（EBOM→MBOM 版本对应关系台账，衔接点一/D1：mms receiveMbom 接收）
DROP TABLE IF EXISTS `pdm_mbom_release`;
CREATE TABLE `pdm_mbom_release` (
  `release_id`    BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '下发记录ID',
  `bom_id`        BIGINT(20)   NOT NULL                COMMENT '设计BOM ID（pdm_design_bom）',
  `bom_no`        VARCHAR(64)  DEFAULT ''              COMMENT '设计BOM编号（冗余）',
  `product_id`    BIGINT(20)   NOT NULL                COMMENT '产品档案ID',
  `product_code`  VARCHAR(64)  DEFAULT ''              COMMENT '产品编码（=物料编码）',
  `product_name`  VARCHAR(128) DEFAULT ''              COMMENT '产品名称（冗余）',
  `ebom_version`  VARCHAR(32)  DEFAULT ''              COMMENT '下发的EBOM生效版本号',
  `mms_bom_id`    BIGINT(20)   DEFAULT NULL            COMMENT '接收方BOM ID（mms_bom）',
  `mms_bom_no`    VARCHAR(64)  DEFAULT ''              COMMENT '接收方BOM编号（mms_bom.bom_no）',
  `mms_version`   VARCHAR(64)  DEFAULT ''              COMMENT '接收方BOM版本号（=EBOM版本）',
  `item_count`    INT(4)       DEFAULT 0               COMMENT '下发明细行数',
  `release_status` VARCHAR(10) DEFAULT '0'             COMMENT '下发状态（字典 pdm_release_status：0=已下发,1=已替代）',
  `release_by`    VARCHAR(64)  DEFAULT ''              COMMENT '下发人',
  `release_time`  DATETIME     DEFAULT NULL            COMMENT '下发时间',
  `create_by`     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME                             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME                             COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`release_id`),
  KEY `idx_bom_id` (`bom_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_mms_bom_id` (`mms_bom_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM下发记录表（EBOM与MBOM版本对应关系）';

-- =====================================================================
-- 二、字典
-- =====================================================================

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '技术变更状态', 'pdm_change_status', '0', 'admin', sysdate(), '产品设计模块：技术变更单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_change_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '变更级别', 'pdm_change_level', '0', 'admin', sysdate(), '产品设计模块：技术变更级别'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_change_level');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '变更紧急程度', 'pdm_urgent_level', '0', 'admin', sysdate(), '产品设计模块：变更紧急程度'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_urgent_level');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '变更资料类型', 'pdm_doc_type', '0', 'admin', sysdate(), '产品设计模块：变更涉及资料类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_doc_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '变更明细状态', 'pdm_change_item_status', '0', 'admin', sysdate(), '产品设计模块：变更资料明细执行状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_change_item_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'BOM下发状态', 'pdm_release_status', '0', 'admin', sysdate(), '产品设计模块：BOM下发记录状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_release_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', '0', 'pdm_change_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '审批中', '1', 'pdm_change_status', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '待执行', '2', 'pdm_change_status', '', 'warning', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_status' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '执行中', '3', 'pdm_change_status', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_status' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '已关闭', '4', 'pdm_change_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_status' AND dict_value='4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '已驳回', '5', 'pdm_change_status', '', 'danger', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_status' AND dict_value='5');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '一般变更', '1', 'pdm_change_level', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_level' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '重大变更', '2', 'pdm_change_level', '', 'danger', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_level' AND dict_value='2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '普通', '1', 'pdm_urgent_level', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_urgent_level' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '紧急', '2', 'pdm_urgent_level', '', 'warning', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_urgent_level' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '特急', '3', 'pdm_urgent_level', '', 'danger', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_urgent_level' AND dict_value='3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '设计方案', '1', 'pdm_doc_type', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_doc_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '图纸文件', '2', 'pdm_doc_type', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_doc_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '设计BOM', '3', 'pdm_doc_type', '', 'warning', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_doc_type' AND dict_value='3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待执行', '0', 'pdm_change_item_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_item_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已升版生效', '1', 'pdm_change_item_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_change_item_status' AND dict_value='1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '已下发', '0', 'pdm_release_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_release_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已替代', '1', 'pdm_release_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_release_status' AND dict_value='1');

-- =====================================================================
-- 三、菜单权限（号段 11400，父目录 11000 产品设计；变更单 11430 号段，BOM下发按钮挂 11420）
-- =====================================================================

-- 3.1 设计BOM下发按钮（挂在 11420 设计BOM 菜单下）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11427, '设计BOM下发', 11420, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:release', '#', 'admin', sysdate(), '生效BOM一键下发转换为制造BOM（mms接收）');

-- 3.2 技术变更单菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11430, '技术变更', 11000, 7, 'change', 'pdm/change/index', '', 'PdmChange', 1, 0, 'C', '0', '0', 'pdm:change:list', 'edit', 'admin', sysdate(), '技术变更管理菜单（变更台账）');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11431, '变更单查询', 11430, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:query', '#', 'admin', sysdate(), ''),
(11432, '变更单新增', 11430, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:add', '#', 'admin', sysdate(), ''),
(11433, '变更单修改', 11430, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:edit', '#', 'admin', sysdate(), ''),
(11434, '变更单删除', 11430, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:remove', '#', 'admin', sysdate(), '仅草稿可删；进入流程后变更记录不可删除'),
(11435, '变更单导出', 11430, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:export', '#', 'admin', sysdate(), ''),
(11436, '变更提交审批', 11430, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:submit', '#', 'admin', sysdate(), ''),
(11437, '变更审批', 11430, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:audit', '#', 'admin', sysdate(), '设计负责人/部门负责人审批（重大变更含会签）'),
(11438, '变更执行生效', 11430, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:execute', '#', 'admin', sysdate(), '按变更单对资料升版生效（依据=技术变更）'),
(11439, '变更关闭', 11430, 9, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:change:close', '#', 'admin', sysdate(), '校验涉及资料全部升版生效后方可关闭');

-- 3.3 超管角色授权（号段防重）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id BETWEEN 11400 AND 11499)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- =====================================================================
-- 四、编号规则（pdm_change_order 已随 pdm_all.sql 初始化，此处幂等兜底）
-- =====================================================================

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_change_order', 'pdm', '变更单编号', 'BG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '技术变更单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_change_order');
