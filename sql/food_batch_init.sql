-- =============================================================================
-- 食品行业批次效期能力 P0-批次一 初始化脚本（传丰环境 RUOYI_CFSP）
-- 依据《食品行业批次效期能力设计方案》3.3/3.4
-- 内容：food_* 9张核心表 + 字典 + 菜单注册（仓储管理>批次管理）+ 种子数据
-- 边界：表前缀 food_ / 后端包 com.ruoyi.food / 接口前缀 /food/ / 前端 views/food/
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、核心表（9张）
-- -----------------------------------------------------------------------------

-- 1. 批次主表
CREATE TABLE IF NOT EXISTS `food_batch` (
  `batch_id`        bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `batch_code`      varchar(64)   NOT NULL                COMMENT '内部批次号（规则引擎生成，禁手工修改）',
  `ext_batch_code`  varchar(64)   DEFAULT NULL            COMMENT '外部批次号（供应商批次等）',
  `material_id`     bigint(20)    NOT NULL                COMMENT '物料ID',
  `material_code`   varchar(64)   DEFAULT NULL            COMMENT '物料编码（冗余）',
  `source_type`     char(1)       NOT NULL DEFAULT '6'    COMMENT '批次来源（1采购入库 2生产完工 3拆批 4合批 5退货 6初始化）',
  `source_bill_type` varchar(30)  DEFAULT NULL            COMMENT '来源单据类型',
  `source_bill_id`  bigint(20)    DEFAULT NULL            COMMENT '来源单据ID',
  `produce_date`    date          DEFAULT NULL            COMMENT '生产日期',
  `receive_date`    date          DEFAULT NULL            COMMENT '收货/入库日期',
  `shelf_life_days` int(11)       DEFAULT NULL            COMMENT '保质期天数（固化）',
  `expire_date`     date          DEFAULT NULL            COMMENT '到期日期（推算后固化）',
  `qty`             decimal(18,2) NOT NULL DEFAULT 0     COMMENT '批次数量（冗余总量，按谱系/调整联动）',
  `qty_unit`        varchar(20)   DEFAULT NULL            COMMENT '数量单位',
  `batch_status`    char(1)       NOT NULL DEFAULT '0'    COMMENT '批次状态（0待检 1合格 2冻结 3报废）',
  `expire_status`   char(1)       NOT NULL DEFAULT '0'    COMMENT '效期状态（0正常 1临期 2过期）',
  `freeze_reason`   varchar(255)  DEFAULT NULL            COMMENT '冻结原因',
  `freeze_time`     datetime      DEFAULT NULL            COMMENT '冻结时间',
  `del_flag`        char(1)       NOT NULL DEFAULT '0'    COMMENT '删除标志（0存在 2删除）',
  `create_by`       varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL            COMMENT '创建时间',
  `update_by`       varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`     datetime      DEFAULT NULL            COMMENT '更新时间',
  `remark`          varchar(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`batch_id`),
  UNIQUE KEY `uk_batch_code` (`batch_code`),
  KEY `idx_batch_material` (`material_id`),
  KEY `idx_batch_status` (`batch_status`),
  KEY `idx_batch_expire` (`expire_date`),
  KEY `idx_batch_ext_code` (`ext_batch_code`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次主表';

-- 2. 批次谱系表
CREATE TABLE IF NOT EXISTS `food_batch_genealogy` (
  `genealogy_id`    bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '谱系ID',
  `parent_batch_id` bigint(20)    NOT NULL                COMMENT '父批次ID',
  `child_batch_id`  bigint(20)    NOT NULL                COMMENT '子批次ID',
  `relation_type`   char(1)       NOT NULL DEFAULT '2'    COMMENT '关系类型（1投入 2派生 3拆分 4合并 5退回）',
  `qty`             decimal(18,2) DEFAULT 0               COMMENT '流转数量',
  `qty_unit`        varchar(20)   DEFAULT NULL            COMMENT '数量单位',
  `ref_bill_type`   varchar(30)   DEFAULT NULL            COMMENT '关联单据类型',
  `ref_bill_id`     bigint(20)    DEFAULT NULL            COMMENT '关联单据ID',
  `ref_bill_code`   varchar(64)   DEFAULT NULL            COMMENT '关联单据号',
  `create_by`       varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime      DEFAULT NULL            COMMENT '创建时间',
  `remark`          varchar(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`genealogy_id`),
  KEY `idx_genealogy_parent` (`parent_batch_id`),
  KEY `idx_genealogy_child` (`child_batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次谱系表';

-- 3. 批次库存表（批次维度库存，与wms_inventory按batch_no软关联）
CREATE TABLE IF NOT EXISTS `food_batch_stock` (
  `bs_id`        bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id`  bigint(20)    NOT NULL                COMMENT '物料ID',
  `warehouse_id` bigint(20)    NOT NULL                COMMENT '仓库ID',
  `location_id`  bigint(20)    NOT NULL                COMMENT '库位ID',
  `batch_id`     bigint(20)    NOT NULL                COMMENT '批次ID',
  `qty`          decimal(18,2) NOT NULL DEFAULT 0     COMMENT '批次库存量',
  `qty_locked`   decimal(18,2) NOT NULL DEFAULT 0     COMMENT '锁定/预留量',
  `version`      int(11)       NOT NULL DEFAULT 0     COMMENT '乐观锁版本',
  `create_by`    varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime      DEFAULT NULL            COMMENT '创建时间',
  `update_by`    varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime      DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`bs_id`),
  UNIQUE KEY `uk_batch_stock` (`material_id`,`warehouse_id`,`location_id`,`batch_id`),
  KEY `idx_batch_stock_batch` (`batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次库存表';

-- 4. 批次号规则表（占位符：{yyyyMMdd} {yyMM} {yyyy} {materialCode} {seq:n}）
CREATE TABLE IF NOT EXISTS `food_batch_rule` (
  `rule_id`      bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name`    varchar(100) NOT NULL                COMMENT '规则名称',
  `material_id`  bigint(20)   NOT NULL DEFAULT 0      COMMENT '适用物料ID（0=默认规则）',
  `rule_pattern` varchar(100) NOT NULL                COMMENT '批次号模板（占位符组合）',
  `current_seq`  bigint(20)   NOT NULL DEFAULT 0      COMMENT '当前流水号',
  `seq_date`     varchar(10)  DEFAULT NULL            COMMENT '流水日期（跨日自动重置）',
  `status`       char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0启用 1停用）',
  `del_flag`     char(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志（0存在 2删除）',
  `create_by`    varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`       varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_batch_rule_material` (`material_id`),
  KEY `idx_batch_rule_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次号规则表';

-- 5. 效期策略表（三级匹配：物料 → 物料类别 → 默认）
CREATE TABLE IF NOT EXISTS `food_expiry_policy` (
  `policy_id`       bigint(20) NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `policy_name`     varchar(100) NOT NULL              COMMENT '策略名称',
  `material_id`     bigint(20) NOT NULL DEFAULT 0      COMMENT '适用物料ID（0=不限）',
  `material_type`   varchar(20) NOT NULL DEFAULT ''    COMMENT '适用物料类别（空=不限，对应wms_material.material_type）',
  `warn_days`       int(11) NOT NULL DEFAULT 30        COMMENT '临期预警天数',
  `freeze_days`     int(11) DEFAULT 0                  COMMENT '过期冻结天数（0=过期即冻结）',
  `min_ship_days`   int(11) DEFAULT 0                  COMMENT '最小可发运剩余天数',
  `fefo_enabled`    char(1) NOT NULL DEFAULT '1'       COMMENT '是否启用FEFO先进先出（0否 1是）',
  `status`          char(1) NOT NULL DEFAULT '0'       COMMENT '状态（0启用 1停用）',
  `del_flag`        char(1) NOT NULL DEFAULT '0'       COMMENT '删除标志（0存在 2删除）',
  `create_by`       varchar(64) DEFAULT ''             COMMENT '创建者',
  `create_time`     datetime    DEFAULT NULL           COMMENT '创建时间',
  `update_by`       varchar(64) DEFAULT ''             COMMENT '更新者',
  `update_time`     datetime    DEFAULT NULL           COMMENT '更新时间',
  `remark`          varchar(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (`policy_id`),
  KEY `idx_expiry_policy_material` (`material_id`),
  KEY `idx_expiry_policy_type` (`material_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品效期策略表';

-- 6. 批次调整主单
CREATE TABLE IF NOT EXISTS `food_batch_adjust` (
  `adjust_id`       bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '调整单ID',
  `adjust_code`     varchar(64)  NOT NULL                COMMENT '调整单号',
  `adjust_type`     char(1)      NOT NULL                COMMENT '调整类型（1冻结 2解冻 3报废 4拆批 5合批 6召回 7效期调整）',
  `source_batch_id`  bigint(20)  DEFAULT NULL            COMMENT '主批次ID',
  `source_batch_code` varchar(64) DEFAULT NULL           COMMENT '主批次号',
  `material_id`     bigint(20)   DEFAULT NULL            COMMENT '物料ID',
  `status`          char(1)      NOT NULL DEFAULT '0'    COMMENT '单据状态（0待执行 1已执行 2已取消）',
  `reason`          varchar(255) DEFAULT NULL            COMMENT '调整原因',
  `execute_time`    datetime     DEFAULT NULL            COMMENT '执行时间',
  `execute_by`      varchar(64)  DEFAULT NULL            COMMENT '执行人',
  `del_flag`        char(1)      NOT NULL DEFAULT '0'    COMMENT '删除标志（0存在 2删除）',
  `create_by`       varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     datetime     DEFAULT NULL            COMMENT '更新时间',
  `remark`          varchar(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`adjust_id`),
  UNIQUE KEY `uk_adjust_code` (`adjust_code`),
  KEY `idx_adjust_batch` (`source_batch_id`),
  KEY `idx_adjust_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次调整主单';

-- 7. 批次调整明细
CREATE TABLE IF NOT EXISTS `food_batch_adjust_item` (
  `item_id`          bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `adjust_id`        bigint(20)    NOT NULL                COMMENT '调整单ID',
  `source_batch_id`  bigint(20)    DEFAULT NULL            COMMENT '源批次ID',
  `source_batch_code` varchar(64)  DEFAULT NULL            COMMENT '源批次号',
  `target_batch_id`  bigint(20)    DEFAULT NULL            COMMENT '目标批次ID（拆批/合批生成后回填）',
  `target_batch_code` varchar(64)  DEFAULT NULL            COMMENT '目标批次号',
  `qty`              decimal(18,2) DEFAULT 0               COMMENT '调整数量',
  `qty_unit`         varchar(20)   DEFAULT NULL            COMMENT '数量单位',
  `expire_action`    char(1)       DEFAULT '1'             COMMENT '效期处理（1沿用源批次 2重新计算 3顺延）',
  `new_expire_date`  date          DEFAULT NULL            COMMENT '新到期日期（效期调整时填写）',
  `remark`           varchar(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_adjust_item_adjust` (`adjust_id`),
  KEY `idx_adjust_item_source` (`source_batch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次调整明细';

-- 8. 批次状态日志表（只增不删，禁删改）
CREATE TABLE IF NOT EXISTS `food_batch_status_log` (
  `log_id`        bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `batch_id`      bigint(20)   NOT NULL                COMMENT '批次ID',
  `batch_code`    varchar(64)  DEFAULT NULL            COMMENT '批次号',
  `from_status`   char(1)      DEFAULT NULL            COMMENT '迁移前状态',
  `to_status`     char(1)      DEFAULT NULL            COMMENT '迁移后状态',
  `operate_type`  varchar(30)  NOT NULL                COMMENT '操作类型（QUALIFY/FREEZE/UNFREEZE/SCRAP/SPLIT/MERGE/RECALL/EXPIRE/CREATE）',
  `reason`        varchar(255) DEFAULT NULL            COMMENT '原因',
  `ref_bill_type` varchar(30)  DEFAULT NULL            COMMENT '关联单据类型',
  `ref_bill_id`   bigint(20)   DEFAULT NULL            COMMENT '关联单据ID',
  `operate_by`    varchar(64)  DEFAULT NULL            COMMENT '操作人',
  `operate_time`  datetime     DEFAULT NULL            COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_status_log_batch` (`batch_id`),
  KEY `idx_status_log_time` (`operate_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次状态日志表';

-- 9. 批次预留表（销售预留，qty_locked核销）
CREATE TABLE IF NOT EXISTS `food_batch_reserve` (
  `reserve_id`     bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '预留ID',
  `material_id`    bigint(20)    NOT NULL                COMMENT '物料ID',
  `batch_id`       bigint(20)    NOT NULL                COMMENT '批次ID',
  `customer_name`  varchar(128)  DEFAULT NULL            COMMENT '客户名称',
  `order_bill_type` varchar(30)  DEFAULT NULL            COMMENT '单据类型（销售订单等）',
  `order_bill_id`  bigint(20)    DEFAULT NULL            COMMENT '单据ID',
  `order_bill_code` varchar(64)  DEFAULT NULL            COMMENT '单据号',
  `qty`            decimal(18,2) NOT NULL DEFAULT 0     COMMENT '预留数量',
  `status`         char(1)       NOT NULL DEFAULT '0'    COMMENT '预留状态（0预留中 1已核销 2已释放）',
  `release_time`   datetime      DEFAULT NULL            COMMENT '释放时间',
  `del_flag`       char(1)       NOT NULL DEFAULT '0'    COMMENT '删除标志（0存在 2删除）',
  `create_by`      varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime      DEFAULT NULL            COMMENT '创建时间',
  `update_by`      varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`    datetime      DEFAULT NULL            COMMENT '更新时间',
  `remark`         varchar(500)  DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`reserve_id`),
  KEY `idx_reserve_batch` (`batch_id`),
  KEY `idx_reserve_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食品批次预留表';

-- -----------------------------------------------------------------------------
-- 二、字典（幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_dict_data` WHERE dict_type IN ('food_batch_status','food_expire_status','food_source_type','food_relation_type','food_adjust_type','food_adjust_status','food_reserve_status');
DELETE FROM `sys_dict_type` WHERE dict_type IN ('food_batch_status','food_expire_status','food_source_type','food_relation_type','food_adjust_type','food_adjust_status','food_reserve_status');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('批次状态',   'food_batch_status',  '0', 'admin', sysdate(), '食品批次状态'),
('效期状态',   'food_expire_status', '0', 'admin', sysdate(), '食品批次效期状态'),
('批次来源',   'food_source_type',   '0', 'admin', sysdate(), '食品批次来源类型'),
('批次谱系关系', 'food_relation_type', '0', 'admin', sysdate(), '批次谱系关系类型'),
('批次调整类型', 'food_adjust_type',   '0', 'admin', sysdate(), '批次调整单类型'),
('调整单状态', 'food_adjust_status', '0', 'admin', sysdate(), '批次调整单状态'),
('批次预留状态', 'food_reserve_status','0', 'admin', sysdate(), '批次预留状态');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, '待检',   '0', 'food_batch_status',  '', 'warning', 'N', '0', 'admin', sysdate()),
(2, '合格',   '1', 'food_batch_status',  '', 'success', 'N', '0', 'admin', sysdate()),
(3, '冻结',   '2', 'food_batch_status',  '', 'danger',  'N', '0', 'admin', sysdate()),
(4, '报废',   '3', 'food_batch_status',  '', 'info',    'N', '0', 'admin', sysdate()),
(1, '正常',   '0', 'food_expire_status', '', 'success', 'N', '0', 'admin', sysdate()),
(2, '临期',   '1', 'food_expire_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '过期',   '2', 'food_expire_status', '', 'danger',  'N', '0', 'admin', sysdate()),
(1, '采购入库', '1', 'food_source_type', '', '',       'N', '0', 'admin', sysdate()),
(2, '生产完工', '2', 'food_source_type', '', '',       'N', '0', 'admin', sysdate()),
(3, '拆批',   '3', 'food_source_type',   '', '',       'N', '0', 'admin', sysdate()),
(4, '合批',   '4', 'food_source_type',   '', '',       'N', '0', 'admin', sysdate()),
(5, '退货',   '5', 'food_source_type',   '', '',       'N', '0', 'admin', sysdate()),
(6, '初始化', '6', 'food_source_type',   '', '',       'Y', '0', 'admin', sysdate()),
(1, '投入',   '1', 'food_relation_type', '', '',       'N', '0', 'admin', sysdate()),
(2, '派生',   '2', 'food_relation_type', '', '',       'N', '0', 'admin', sysdate()),
(3, '拆分',   '3', 'food_relation_type', '', '',       'N', '0', 'admin', sysdate()),
(4, '合并',   '4', 'food_relation_type', '', '',       'N', '0', 'admin', sysdate()),
(5, '退回',   '5', 'food_relation_type', '', '',       'N', '0', 'admin', sysdate()),
(1, '冻结',   '1', 'food_adjust_type',   '', 'warning', 'N', '0', 'admin', sysdate()),
(2, '解冻',   '2', 'food_adjust_type',   '', 'success', 'N', '0', 'admin', sysdate()),
(3, '报废',   '3', 'food_adjust_type',   '', 'danger',  'N', '0', 'admin', sysdate()),
(4, '拆批',   '4', 'food_adjust_type',   '', '',       'N', '0', 'admin', sysdate()),
(5, '合批',   '5', 'food_adjust_type',   '', '',       'N', '0', 'admin', sysdate()),
(6, '召回',   '6', 'food_adjust_type',   '', 'danger',  'N', '0', 'admin', sysdate()),
(7, '效期调整', '7', 'food_adjust_type', '', '',       'N', '0', 'admin', sysdate()),
(1, '待执行', '0', 'food_adjust_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(2, '已执行', '1', 'food_adjust_status', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '已取消', '2', 'food_adjust_status', '', 'info',    'N', '0', 'admin', sysdate()),
(1, '预留中', '0', 'food_reserve_status','', 'warning', 'N', '0', 'admin', sysdate()),
(2, '已核销', '1', 'food_reserve_status','', 'success', 'N', '0', 'admin', sysdate()),
(3, '已释放', '2', 'food_reserve_status','', 'info',    'N', '0', 'admin', sysdate());

-- -----------------------------------------------------------------------------
-- 三、菜单注册（仓储管理2000 > 批次管理，幂等：先删后插）
-- -----------------------------------------------------------------------------
DELETE FROM `sys_role_menu` WHERE menu_id BETWEEN 906210 AND 9062149;
DELETE FROM `sys_menu` WHERE menu_id BETWEEN 906210 AND 9062149;

-- 目录：批次管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906210, '批次管理', 2000, 8, 'batch', '', '', '', 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate(), '食品行业批次效期管理');

-- 页面1：批次台账
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906211, '批次台账', 906210, 1, 'ledger', 'food/batch/index', '', '', 1, 0, 'C', '0', '0', 'food:batch:list', 'list', 'admin', sysdate(), '批次台账与效期状态');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062111, '批次查询',   906211, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:query',   '#', 'admin', sysdate(), ''),
(9062112, '批次新增',   906211, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:add',     '#', 'admin', sysdate(), ''),
(9062113, '批次修改',   906211, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:edit',    '#', 'admin', sysdate(), ''),
(9062114, '批次删除',   906211, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:remove',  '#', 'admin', sysdate(), ''),
(9062115, '冻结解冻',   906211, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:freeze',  '#', 'admin', sysdate(), ''),
(9062116, '批次报废',   906211, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:scrap',   '#', 'admin', sysdate(), ''),
(9062117, '合格判定',   906211, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:qualify', '#', 'admin', sysdate(), ''),
(9062118, '批次导出',   906211, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:batch:export',  '#', 'admin', sysdate(), '');

-- 页面2：批次操作（调整单）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906212, '批次操作', 906210, 2, 'adjust', 'food/adjust/index', '', '', 1, 0, 'C', '0', '0', 'food:adjust:list', 'edit', 'admin', sysdate(), '批次调整单：冻结/解冻/报废/拆批/合批/召回/效期调整');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062121, '调整查询',   906212, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:adjust:query',   '#', 'admin', sysdate(), ''),
(9062122, '调整新增',   906212, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:adjust:add',     '#', 'admin', sysdate(), ''),
(9062123, '调整执行',   906212, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:adjust:execute', '#', 'admin', sysdate(), ''),
(9062124, '调整取消',   906212, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:adjust:cancel',  '#', 'admin', sysdate(), ''),
(9062125, '调整删除',   906212, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:adjust:remove',  '#', 'admin', sysdate(), '');

-- 页面3：批次规则
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906213, '批次规则', 906210, 3, 'rule', 'food/rule/index', '', '', 1, 0, 'C', '0', '0', 'food:rule:list', 'number', 'admin', sysdate(), '批次号生成规则配置');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062131, '规则查询',   906213, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:rule:query',   '#', 'admin', sysdate(), ''),
(9062132, '规则新增',   906213, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:rule:add',     '#', 'admin', sysdate(), ''),
(9062133, '规则修改',   906213, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:rule:edit',    '#', 'admin', sysdate(), ''),
(9062134, '规则删除',   906213, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:rule:remove',  '#', 'admin', sysdate(), '');

-- 页面4：效期策略
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES (906214, '效期策略', 906210, 4, 'expiryPolicy', 'food/expiryPolicy/index', '', '', 1, 0, 'C', '0', '0', 'food:policy:list', 'time', 'admin', sysdate(), '效期预警/冻结/FEFO策略配置');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(9062141, '策略查询',   906214, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:policy:query',   '#', 'admin', sysdate(), ''),
(9062142, '策略新增',   906214, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:policy:add',     '#', 'admin', sysdate(), ''),
(9062143, '策略修改',   906214, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:policy:edit',    '#', 'admin', sysdate(), ''),
(9062144, '策略删除',   906214, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'food:policy:remove',  '#', 'admin', sysdate(), '');

-- 授权超级管理员角色（admin 实际全量放行，此处按惯例同步登记）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.menu_id FROM `sys_menu` m WHERE m.menu_id BETWEEN 906210 AND 9062149;

-- -----------------------------------------------------------------------------
-- 四、种子数据
-- -----------------------------------------------------------------------------
-- 默认批次号规则（物料0=默认；含跨日流水重置）
DELETE FROM `food_batch_rule` WHERE material_id = 0;
INSERT INTO `food_batch_rule` (`rule_name`, `material_id`, `rule_pattern`, `current_seq`, `seq_date`, `status`, `create_by`, `remark`)
VALUES ('默认批次号规则', 0, 'B{yyyyMMdd}{seq:4}', 0, NULL, '0', 'admin', '全局默认规则：B+日期+4位流水');

-- 默认效期策略（三级匹配兜底）
DELETE FROM `food_expiry_policy` WHERE material_id = 0 AND material_type = '';
INSERT INTO `food_expiry_policy` (`policy_name`, `material_id`, `material_type`, `warn_days`, `freeze_days`, `min_ship_days`, `fefo_enabled`, `status`, `create_by`, `remark`)
VALUES ('默认效期策略', 0, '', 30, 0, 0, '1', '0', 'admin', '全局默认：临期预警30天，过期即冻结，启用FEFO');

-- -----------------------------------------------------------------------------
-- 回滚脚本（仅注释备用）
-- =============================================================================
-- DROP TABLE IF EXISTS `food_batch_reserve`;
-- DROP TABLE IF EXISTS `food_batch_status_log`;
-- DROP TABLE IF EXISTS `food_batch_adjust_item`;
-- DROP TABLE IF EXISTS `food_batch_adjust`;
-- DROP TABLE IF EXISTS `food_expiry_policy`;
-- DROP TABLE IF EXISTS `food_batch_rule`;
-- DROP TABLE IF EXISTS `food_batch_stock`;
-- DROP TABLE IF EXISTS `food_batch_genealogy`;
-- DROP TABLE IF EXISTS `food_batch`;
-- DELETE FROM `sys_dict_data` WHERE dict_type IN ('food_batch_status','food_expire_status','food_source_type','food_relation_type','food_adjust_type','food_adjust_status','food_reserve_status');
-- DELETE FROM `sys_dict_type` WHERE dict_type IN ('food_batch_status','food_expire_status','food_source_type','food_relation_type','food_adjust_type','food_adjust_status','food_reserve_status');
-- DELETE FROM `sys_role_menu` WHERE menu_id BETWEEN 906210 AND 9062149;
-- DELETE FROM `sys_menu` WHERE menu_id BETWEEN 906210 AND 9062149;
