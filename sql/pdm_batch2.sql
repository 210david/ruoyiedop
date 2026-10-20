-- =====================================================================
-- PDM 产品设计模块 第二批脚本（pdm_batch2.sql）
-- 范围：设计方案（含配方配比）、图纸技术文件、设计BOM（含明细）三张资料页
--       + 版本机制挂接（pdm_doc_version biz_type 1/2/3）
-- 四段式：一、业务表 DDL；二、字典；三、菜单权限；四、备注
-- 编号规则 pdm_scheme/pdm_design_file/pdm_design_bom 已随 pdm_all.sql 初始化，直接复用
-- 可重复执行：DROP TABLE IF EXISTS + WHERE NOT EXISTS 防重
-- =====================================================================

-- =====================================================================
-- 一、业务表 DDL
-- =====================================================================

-- 1. 设计方案表（事务单据类：带 dept_id，BizDeptFillAspect 自动填充，@DataScope 过滤）
DROP TABLE IF EXISTS `pdm_scheme`;
CREATE TABLE `pdm_scheme` (
  `scheme_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `scheme_no`     VARCHAR(64)   DEFAULT ''              COMMENT '方案编号（pdm_scheme 规则自动生成）',
  `scheme_name`   VARCHAR(128)  NOT NULL                COMMENT '方案名称',
  `product_id`    BIGINT(20)    NOT NULL                COMMENT '产品档案ID',
  `product_code`  VARCHAR(64)   DEFAULT ''              COMMENT '产品编码（冗余）',
  `product_name`  VARCHAR(128)  DEFAULT ''              COMMENT '产品名称（冗余）',
  `design_goal`   VARCHAR(1000) DEFAULT ''              COMMENT '设计目标',
  `main_params`   VARCHAR(2000) DEFAULT ''              COMMENT '主要参数',
  `file_url`      VARCHAR(500)  DEFAULT ''              COMMENT '方案正文附件（Word/PDF，最新版本文件）',
  `scheme_status` VARCHAR(10)   DEFAULT '0'             COMMENT '方案状态（字典 pdm_scheme_status：0=编制中,1=已定稿,2=已停用）',
  `final_time`    DATETIME      DEFAULT NULL            COMMENT '定稿时间',
  `final_by`      VARCHAR(64)   DEFAULT ''              COMMENT '定稿人',
  `dept_id`       BIGINT(20)    DEFAULT NULL            COMMENT '部门ID（数据权限，BizDeptFillAspect 自动填充）',
  `del_flag`      CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除）',
  `create_by`     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME                              COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME                              COMMENT '更新时间',
  `remark`        VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`scheme_id`),
  UNIQUE KEY `uk_pdm_scheme_no` (`scheme_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`scheme_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计方案表';

-- 2. 配方配比明细表（涉密等级最高：查看/导出单独权限点 pdm:scheme:formula）
DROP TABLE IF EXISTS `pdm_scheme_formula`;
CREATE TABLE `pdm_scheme_formula` (
  `formula_id`  BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `scheme_id`   BIGINT(20)   NOT NULL                COMMENT '方案ID',
  `item_name`   VARCHAR(128) NOT NULL                COMMENT '原料名称',
  `ratio`       VARCHAR(64)  DEFAULT ''              COMMENT '配比',
  `unit`        VARCHAR(32)  DEFAULT ''              COMMENT '单位',
  `tolerance`   VARCHAR(64)  DEFAULT ''              COMMENT '允差范围',
  `order_num`   INT(4)       DEFAULT 0               COMMENT '显示顺序',
  `remark`      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`formula_id`),
  KEY `idx_scheme_id` (`scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计方案配方配比明细表';

-- 3. 图纸技术文件表（资料库共享类：豁免归属字段；受控状态由版本生效机制驱动）
DROP TABLE IF EXISTS `pdm_design_file`;
CREATE TABLE `pdm_design_file` (
  `file_id`       BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '资料ID',
  `file_no`       VARCHAR(64)  DEFAULT ''              COMMENT '资料编号（pdm_design_file 规则自动生成）',
  `file_name`     VARCHAR(128) NOT NULL                COMMENT '资料名称',
  `file_category` VARCHAR(10)  DEFAULT '1'             COMMENT '资料类别（字典 pdm_file_category：1=图纸,2=技术文件,3=包装设计稿,4=模型,5=其他）',
  `product_id`    BIGINT(20)   NOT NULL                COMMENT '产品档案ID',
  `product_code`  VARCHAR(64)  DEFAULT ''              COMMENT '产品编码（冗余）',
  `product_name`  VARCHAR(128) DEFAULT ''              COMMENT '产品名称（冗余）',
  `scheme_id`     BIGINT(20)   DEFAULT NULL            COMMENT '关联设计方案ID（可选）',
  `scheme_no`     VARCHAR(64)  DEFAULT ''              COMMENT '关联设计方案编号（冗余）',
  `file_url`      VARCHAR(500) DEFAULT ''              COMMENT '文件相对路径（最新版本）',
  `file_status`   VARCHAR(10)  DEFAULT '0'             COMMENT '受控状态（字典 pdm_file_status：0=未生效,1=受控生效；受控后方可对外分发）',
  `del_flag`      CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除）',
  `create_by`     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   DATETIME                             COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   DATETIME                             COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`file_id`),
  UNIQUE KEY `uk_pdm_file_no` (`file_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_category` (`file_category`),
  KEY `idx_status` (`file_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图纸技术文件表';

-- 4. 设计BOM表（EBOM，主数据/共享类：豁免归属字段；下发转换 MBOM 于第三批）
DROP TABLE IF EXISTS `pdm_design_bom`;
CREATE TABLE `pdm_design_bom` (
  `bom_id`      BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT 'BOM ID',
  `bom_no`      VARCHAR(64)   DEFAULT ''              COMMENT 'BOM编号（pdm_design_bom 规则自动生成）',
  `bom_name`    VARCHAR(128)  DEFAULT ''              COMMENT 'BOM名称',
  `product_id`  BIGINT(20)    NOT NULL                COMMENT '产品档案ID',
  `product_code` VARCHAR(64)  DEFAULT ''              COMMENT '产品编码（冗余）',
  `product_name` VARCHAR(128) DEFAULT ''              COMMENT '产品名称（冗余）',
  `base_qty`    DECIMAL(18,4) DEFAULT 1               COMMENT '产品单台用量',
  `base_unit`   VARCHAR(32)   DEFAULT ''              COMMENT '产品单位',
  `bom_status`  VARCHAR(10)   DEFAULT '0'             COMMENT 'BOM状态（字典 pdm_bom_status：0=编制中,1=已生效,2=已停用）',
  `del_flag`    CHAR(1)       DEFAULT '0'             COMMENT '删除标志（0=存在,2=删除）',
  `create_by`   VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  `create_time` DATETIME                              COMMENT '创建时间',
  `update_by`   VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  `update_time` DATETIME                              COMMENT '更新时间',
  `remark`      VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`bom_id`),
  UNIQUE KEY `uk_pdm_bom_no` (`bom_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`bom_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计BOM表';

-- 5. 设计BOM明细表（多层：parent_id 挂父明细，item_level 记录层级）
DROP TABLE IF EXISTS `pdm_design_bom_item`;
CREATE TABLE `pdm_design_bom_item` (
  `item_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `bom_id`        BIGINT(20)    NOT NULL                COMMENT 'BOM ID',
  `parent_id`     BIGINT(20)    DEFAULT 0               COMMENT '父明细ID（0=顶级）',
  `item_level`    INT(4)        DEFAULT 1               COMMENT '层级（1=顶级）',
  `material_id`   BIGINT(20)    DEFAULT NULL            COMMENT '物料ID（wms_material，物料对照）',
  `material_code` VARCHAR(64)   DEFAULT ''              COMMENT '物料编码',
  `material_name` VARCHAR(128)  NOT NULL                COMMENT '物料名称',
  `spec_model`    VARCHAR(128)  DEFAULT ''              COMMENT '规格型号',
  `unit`          VARCHAR(32)   DEFAULT ''              COMMENT '计量单位',
  `usage_qty`     DECIMAL(18,6) DEFAULT 1               COMMENT '用量',
  `loss_rate`     DECIMAL(10,4) DEFAULT 0               COMMENT '损耗率(%)',
  `order_num`     INT(4)        DEFAULT 0               COMMENT '显示顺序',
  `remark`        VARCHAR(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_bom_id` (`bom_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计BOM明细表';

-- =====================================================================
-- 二、字典
-- =====================================================================

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '设计方案状态', 'pdm_scheme_status', '0', 'admin', sysdate(), '产品设计模块：设计方案状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_scheme_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '图纸文件受控状态', 'pdm_file_status', '0', 'admin', sysdate(), '产品设计模块：图纸文件受控状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_file_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '图纸资料类别', 'pdm_file_category', '0', 'admin', sysdate(), '产品设计模块：图纸资料类别'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_file_category');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '设计BOM状态', 'pdm_bom_status', '0', 'admin', sysdate(), '产品设计模块：设计BOM状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pdm_bom_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '编制中', '0', 'pdm_scheme_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_scheme_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已定稿', '1', 'pdm_scheme_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_scheme_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已停用', '2', 'pdm_scheme_status', '', 'danger', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_scheme_status' AND dict_value='2');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '未生效', '0', 'pdm_file_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '受控生效', '1', 'pdm_file_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_status' AND dict_value='1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '图纸', '1', 'pdm_file_category', '', 'primary', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_category' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '技术文件', '2', 'pdm_file_category', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_category' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '包装设计稿', '3', 'pdm_file_category', '', 'warning', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_category' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '模型', '4', 'pdm_file_category', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_category' AND dict_value='4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '其他', '5', 'pdm_file_category', '', 'default', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_file_category' AND dict_value='5');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '编制中', '0', 'pdm_bom_status', '', 'info', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_bom_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已生效', '1', 'pdm_bom_status', '', 'success', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_bom_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已停用', '2', 'pdm_bom_status', '', 'danger', 'N', '0', 'admin', sysdate(), '' WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pdm_bom_status' AND dict_value='2');

-- =====================================================================
-- 三、菜单权限（号段 11400，父目录 11000 产品设计）
-- =====================================================================

-- 设计方案
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11400, '设计方案', 11000, 4, 'scheme', 'pdm/scheme/index', '', 'PdmScheme', 1, 0, 'C', '0', '0', 'pdm:scheme:list', 'form', 'admin', sysdate(), '设计方案管理菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11401, '设计方案查询', 11400, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:query', '#', 'admin', sysdate(), ''),
(11402, '方案新增', 11400, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:add', '#', 'admin', sysdate(), ''),
(11403, '方案修改', 11400, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:edit', '#', 'admin', sysdate(), ''),
(11404, '方案删除', 11400, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:remove', '#', 'admin', sysdate(), ''),
(11405, '方案导出', 11400, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:export', '#', 'admin', sysdate(), ''),
(11406, '方案定稿生效', 11400, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:effective', '#', 'admin', sysdate(), ''),
(11407, '配方明细查看', 11400, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:scheme:formula', '#', 'admin', sysdate(), '配方配比涉密资料单独授权');

-- 图纸技术文件
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11410, '图纸文件', 11000, 5, 'designFile', 'pdm/designFile/index', '', 'PdmDesignFile', 1, 0, 'C', '0', '0', 'pdm:designFile:list', 'documentation', 'admin', sysdate(), '图纸技术文件管理菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11411, '图纸文件查询', 11410, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designFile:query', '#', 'admin', sysdate(), ''),
(11412, '图纸文件上传', 11410, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designFile:add', '#', 'admin', sysdate(), ''),
(11413, '图纸文件修改', 11410, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designFile:edit', '#', 'admin', sysdate(), ''),
(11414, '图纸文件删除', 11410, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designFile:remove', '#', 'admin', sysdate(), ''),
(11415, '图纸文件导出', 11410, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designFile:export', '#', 'admin', sysdate(), ''),
(11416, '图纸文件生效', 11410, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designFile:effective', '#', 'admin', sysdate(), '');

-- 设计BOM
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11420, '设计BOM', 11000, 6, 'designBom', 'pdm/designBom/index', '', 'PdmDesignBom', 1, 0, 'C', '0', '0', 'pdm:designBom:list', 'tree-table', 'admin', sysdate(), '设计BOM管理菜单');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(11421, '设计BOM查询', 11420, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:query', '#', 'admin', sysdate(), ''),
(11422, '设计BOM新增', 11420, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:add', '#', 'admin', sysdate(), ''),
(11423, '设计BOM修改', 11420, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:edit', '#', 'admin', sysdate(), ''),
(11424, '设计BOM删除', 11420, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:remove', '#', 'admin', sysdate(), ''),
(11425, '设计BOM导出', 11420, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:export', '#', 'admin', sysdate(), ''),
(11426, '设计BOM生效', 11420, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'pdm:designBom:effective', '#', 'admin', sysdate(), '');

-- 给 admin 角色分配新菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (menu_id BETWEEN 11400 AND 11499)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- =====================================================================
-- 四、备注
-- =====================================================================
-- 1. 编号规则 pdm_scheme/pdm_design_file/pdm_design_bom 已随 pdm_all.sql 初始化，直接复用。
-- 2. 版本机制：新增资料自动创建 V1.0 版本记录（pdm_doc_version biz_type 1/2/3），
--    定稿/生效动作调用 PdmDocVersionService.makeEffective，旧版本自动转历史。
-- 3. 已生效资料修订自动小版本升版，原生效版本保持有效（避免变更真空期，见设计方案 7.6 规则）。
