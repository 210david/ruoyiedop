-- =====================================================================
-- PDM 产品设计管理模块 一体化脚本（pdm_all.sql）
-- 四段式：一、业务表 DDL；二、字典；三、菜单权限；四、编号规则与消息字典
-- 可重复执行：DROP TABLE IF EXISTS + WHERE NOT EXISTS 防重
-- =====================================================================

-- =====================================================================
-- 一、业务表 DDL
-- =====================================================================

-- 1. 产品分类表
DROP TABLE IF EXISTS `pdm_product_category`;
CREATE TABLE `pdm_product_category` (
  `category_id`   BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id`     BIGINT(20)    DEFAULT 0               COMMENT '父分类ID（0=顶级）',
  `ancestors`     VARCHAR(255)  DEFAULT ''              COMMENT '祖级路径（逗号分隔）',
  `category_code` VARCHAR(64)   DEFAULT ''              COMMENT '分类编码（pdm_category 规则自动生成）',
  `category_name` VARCHAR(128)  NOT NULL                COMMENT '分类名称',
  `order_num`     INT(4)        DEFAULT 0               COMMENT '显示顺序',
  `status`        VARCHAR(10)   DEFAULT '0'             COMMENT '状态（0=正常,1=停用）',
  `del_flag`      CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除）',
  `create_by`     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME                              COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME                              COMMENT '更新时间',
  `remark`        VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- 2. 产品档案表（一物一档：产品编码=仓库成品/半成品物料编码，档案为设计资料聚合入口）
DROP TABLE IF EXISTS `pdm_product`;
CREATE TABLE `pdm_product` (
  `product_id`     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_code`   VARCHAR(64)  NOT NULL                COMMENT '产品编码（关联仓库成品/半成品物料编码，启用后不可修改）',
  `material_id`    BIGINT(20)   DEFAULT NULL            COMMENT '物料ID（wms_material，唯一挂接）',
  `material_name`  VARCHAR(128) DEFAULT ''              COMMENT '产品名称（冗余物料名称）',
  `spec_model`     VARCHAR(128) DEFAULT ''              COMMENT '规格型号',
  `unit`           VARCHAR(32)  DEFAULT ''              COMMENT '计量单位',
  `category_id`    BIGINT(20)   DEFAULT NULL            COMMENT '产品分类ID',
  `category_name`  VARCHAR(128) DEFAULT ''              COMMENT '产品分类名称（冗余）',
  `product_status` VARCHAR(10)  DEFAULT '0'             COMMENT '产品状态（字典 pdm_product_status；已定型仅可由设计方案定稿流程触发）',
  `designer_id`    BIGINT(20)   DEFAULT NULL            COMMENT '责任设计员ID',
  `designer_name`  VARCHAR(64)  DEFAULT ''              COMMENT '责任设计员姓名',
  `final_date`     DATE         DEFAULT NULL            COMMENT '定型日期',
  `del_flag`       CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除）',
  `create_by`      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`    DATETIME                             COMMENT '创建时间',
  `update_by`      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`    DATETIME                             COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_pdm_product_code` (`product_code`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品档案表';

-- 3. 物料申请表（EBOM/产品档案新增物料，由仓库/采购岗确认编码后回填）
DROP TABLE IF EXISTS `pdm_material_apply`;
CREATE TABLE `pdm_material_apply` (
  `apply_id`      BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `apply_no`      VARCHAR(64)  DEFAULT ''              COMMENT '申请单号（pdm_material_apply 规则自动生成）',
  `apply_type`    VARCHAR(10)  DEFAULT '0'             COMMENT '申请类型（字典 pdm_material_apply_type）',
  `material_name` VARCHAR(128) NOT NULL                COMMENT '申请物料名称',
  `material_type` VARCHAR(10)  DEFAULT NULL            COMMENT '建议物料类型（字典 wms_material_type）',
  `spec_model`    VARCHAR(128) DEFAULT ''              COMMENT '规格型号',
  `unit`          VARCHAR(32)  DEFAULT ''              COMMENT '计量单位',
  `apply_reason`  VARCHAR(500) DEFAULT ''              COMMENT '申请原因',
  `product_id`    BIGINT(20)   DEFAULT NULL            COMMENT '关联产品ID',
  `product_code`  VARCHAR(64)  DEFAULT ''              COMMENT '关联产品编码',
  `bom_id`        BIGINT(20)   DEFAULT NULL            COMMENT '关联设计BOMID（预留）',
  `status`        VARCHAR(10)  DEFAULT '0'             COMMENT '申请状态（字典 pdm_material_apply_status）',
  `material_id`   BIGINT(20)   DEFAULT NULL            COMMENT '确认回填物料ID（wms_material）',
  `material_code` VARCHAR(64)  DEFAULT ''              COMMENT '确认回填物料编码',
  `confirm_by`    VARCHAR(64)  DEFAULT ''              COMMENT '确认人',
  `confirm_time`  DATETIME     DEFAULT NULL            COMMENT '确认时间',
  `dept_id`       BIGINT(20)   DEFAULT NULL            COMMENT '部门ID（数据权限，BizDeptFillAspect 自动填充）',
  `del_flag`      CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除）',
  `create_by`     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME                             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME                             COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`apply_id`),
  UNIQUE KEY `uk_pdm_apply_no` (`apply_no`),
  KEY `idx_status` (`status`),
  KEY `idx_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料申请表';

-- 4. 设计资料版本表（版本管理公共机制：方案/图纸文件/设计BOM 统一版本记录）
DROP TABLE IF EXISTS `pdm_doc_version`;
CREATE TABLE `pdm_doc_version` (
  `version_id`     BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `biz_type`       VARCHAR(10)  NOT NULL                COMMENT '资料类型（字典 pdm_version_biz_type：1设计方案 2图纸文件 3设计BOM）',
  `biz_id`         BIGINT(20)   NOT NULL                COMMENT '资料业务ID',
  `version_no`     VARCHAR(32)  NOT NULL                COMMENT '版本号（V1.0，系统自动分配）',
  `version_desc`   VARCHAR(500) DEFAULT ''              COMMENT '版本说明',
  `effective_flag` VARCHAR(10)  DEFAULT '0'             COMMENT '生效标志（0=历史版本,1=生效版本）',
  `effective_time` DATETIME     DEFAULT NULL            COMMENT '生效时间',
  `invalid_time`   DATETIME     DEFAULT NULL            COMMENT '失效时间（被新版本替代时记录）',
  `effect_basis`   VARCHAR(10)  DEFAULT NULL            COMMENT '生效依据（字典 pdm_version_effect_basis：1设计定稿 2技术变更）',
  `effect_ref`     VARCHAR(64)  DEFAULT ''              COMMENT '生效依据单号（定稿方案号/变更单号）',
  `is_final_set`   CHAR(1)      DEFAULT '0'             COMMENT '是否定型版本组合成员（0=否,1=是）',
  `file_url`       VARCHAR(500) DEFAULT ''              COMMENT '资料文件相对路径',
  `create_by`      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`    DATETIME                             COMMENT '创建时间',
  `update_by`      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`    DATETIME                             COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`version_id`),
  KEY `idx_biz` (`biz_type`, `biz_id`),
  KEY `idx_effective` (`effective_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计资料版本表';

-- =====================================================================
-- 二、字典
-- =====================================================================

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '产品状态', 'pdm_product_status', '0', 'admin', sysdate(), '产品设计模块：产品状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_product_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '物料申请状态', 'pdm_material_apply_status', '0', 'admin', sysdate(), '产品设计模块：物料申请状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_material_apply_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '物料申请类型', 'pdm_material_apply_type', '0', 'admin', sysdate(), '产品设计模块：物料申请类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_material_apply_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '设计资料版本类型', 'pdm_version_biz_type', '0', 'admin', sysdate(), '产品设计模块：设计资料版本类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_version_biz_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '版本生效依据', 'pdm_version_effect_basis', '0', 'admin', sysdate(), '产品设计模块：版本生效依据'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_version_effect_basis');

-- 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '设计中', '0', 'pdm_product_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_product_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已定型', '1', 'pdm_product_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_product_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已停产', '2', 'pdm_product_status', '', 'danger', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_product_status' AND dict_value='2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待确认', '0', 'pdm_material_apply_status', '', 'warning', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_material_apply_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已确认', '1', 'pdm_material_apply_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_material_apply_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已关闭', '2', 'pdm_material_apply_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_material_apply_status' AND dict_value='2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '产品档案物料', '0', 'pdm_material_apply_type', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_material_apply_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '设计BOM物料', '1', 'pdm_material_apply_type', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_material_apply_type' AND dict_value='1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '设计方案', '1', 'pdm_version_biz_type', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_version_biz_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '图纸文件', '2', 'pdm_version_biz_type', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_version_biz_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '设计BOM', '3', 'pdm_version_biz_type', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_version_biz_type' AND dict_value='3');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '设计定稿', '1', 'pdm_version_effect_basis', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_version_effect_basis' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '技术变更', '2', 'pdm_version_effect_basis', '', 'warning', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_version_effect_basis' AND dict_value='2');

-- =====================================================================
-- 三、菜单权限（pdm 菜单树，号段 11000）
-- =====================================================================

-- 顶级目录：产品设计
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11000, '产品设计', 0, 9, 'pdm', NULL, '', '', 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate(), '产品设计管理目录');

-- 产品分类
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11200, '产品分类', 11000, 1, 'category', 'pdm/category/index', '', 'PdmCategory', 1, 0, 'C', '0', '0', 'pdm:category:list', 'dict', 'admin', sysdate(), '产品分类菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11201, '产品分类查询', 11200, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:category:query', '#', 'admin', sysdate(), ''),
(11202, '产品分类新增', 11200, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:category:add', '#', 'admin', sysdate(), ''),
(11203, '产品分类修改', 11200, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:category:edit', '#', 'admin', sysdate(), ''),
(11204, '产品分类删除', 11200, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:category:remove', '#', 'admin', sysdate(), '');

-- 产品档案
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11100, '产品档案', 11000, 2, 'product', 'pdm/product/index', '', 'PdmProduct', 1, 0, 'C', '0', '0', 'pdm:product:list', 'shopping', 'admin', sysdate(), '产品一物一档菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11101, '产品档案查询', 11100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:product:query', '#', 'admin', sysdate(), ''),
(11102, '产品档案新增', 11100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:product:add', '#', 'admin', sysdate(), ''),
(11103, '产品档案修改', 11100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:product:edit', '#', 'admin', sysdate(), ''),
(11104, '产品档案删除', 11100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:product:remove', '#', 'admin', sysdate(), ''),
(11105, '产品档案导出', 11100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:product:export', '#', 'admin', sysdate(), '');

-- 物料申请（确认岗授权仓库/采购角色，D4 决策：消息推送给 wms 物料管理角色）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11300, '物料申请', 11000, 3, 'materialApply', 'pdm/materialApply/index', '', 'PdmMaterialApply', 1, 0, 'C', '0', '0', 'pdm:materialApply:list', 'list', 'admin', sysdate(), '物料申请与编码确认菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11301, '物料申请查询', 11300, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:materialApply:query', '#', 'admin', sysdate(), ''),
(11302, '物料申请新增', 11300, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:materialApply:add', '#', 'admin', sysdate(), ''),
(11303, '物料申请确认', 11300, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:materialApply:confirm', '#', 'admin', sysdate(), ''),
(11304, '物料申请删除', 11300, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:materialApply:remove', '#', 'admin', sysdate(), ''),
(11305, '物料申请导出', 11300, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:materialApply:export', '#', 'admin', sysdate(), '');

-- 给 admin 角色分配 pdm 菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id BETWEEN 11000 AND 11999)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- =====================================================================
-- 四、编号规则与消息字典
-- =====================================================================

-- 编号规则初始化（module='pdm'，取号经 IMkNumberRuleService.generateNumber）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_category', 'pdm', '产品分类编码', 'FL', '', '0', 3, 1, 1, 0, '', '', '0', 'admin', sysdate(), '产品分类编码，不重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_category');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_material_apply', 'pdm', '物料申请单号', 'SQ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '物料申请单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_material_apply');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_scheme', 'pdm', '设计方案编号', 'FA', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '设计方案编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_scheme');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_design_file', 'pdm', '图纸文件编号', 'TZ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '图纸文件编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_design_file');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_design_bom', 'pdm', '设计BOM编号', 'BOM', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '设计BOM编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_design_bom');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'pdm_change_order', 'pdm', '变更单编号', 'BG', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '技术变更单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pdm_change_order');

-- 消息中心业务来源字典追加 pdm（前端消息分类筛选由字典驱动，零代码改动）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 20, '产品设计', 'pdm', 'sys_message_biz_source', '', 'primary', 'N', '0', 'admin', sysdate(), '产品设计模块'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='sys_message_biz_source' AND dict_value='pdm');
