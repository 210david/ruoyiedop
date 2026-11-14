-- ============================================================
-- EDOP YHNY 补丁 SQL 合集（2026-11-14）
-- 内容：金蝶销售出库/采购入库/库存查询建表与菜单、
--       供应商税率列、客户类型字典、供应商类型字典
-- 特性：全部幂等，可整文件重复执行
-- 用法：mysql -uroot -p --default-character-set=utf8mb4 RUOYI_YHNY < patch_20261114_all.sql
-- 注意：巡检类脚本（check_schema_yhny / cleanup_seed_employee_yhny）
--       含只读核对与数据清理，不在本合集内，请单独执行
-- ============================================================

-- ============ add_kd_sal_outbound_20261114.sql ============
-- ============================================================
-- 金蝶云星辰 销售出库单同步数据表 + 菜单
-- 日期：2026-11-14
-- ============================================================

CREATE TABLE IF NOT EXISTS `kd_sal_outbound` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `kd_bill_id`     VARCHAR(64)  NOT NULL COMMENT '金蝶单据ID',
  `kd_entry_id`    VARCHAR(64)  NOT NULL COMMENT '金蝶分录ID',
  `bill_date`      DATE         DEFAULT NULL COMMENT '单据日期',
  `bill_no`        VARCHAR(64)  DEFAULT NULL COMMENT '单据编号',
  `customer_name`  VARCHAR(255) DEFAULT NULL COMMENT '客户',
  `salesman_name`  VARCHAR(64)  DEFAULT NULL COMMENT '业务员',
  `dept_name`      VARCHAR(128) DEFAULT NULL COMMENT '部门',
  `receipt_status` VARCHAR(32)  DEFAULT NULL COMMENT '收款状态',
  `unpaid_amount`  DECIMAL(18,2) DEFAULT '0.00' COMMENT '未收款金额',
  `paid_amount`    DECIMAL(18,2) DEFAULT '0.00' COMMENT '已收款金额',
  `material_name`  VARCHAR(255) DEFAULT NULL COMMENT '商品名称',
  `warehouse_name` VARCHAR(128) DEFAULT NULL COMMENT '仓库',
  `unit_name`      VARCHAR(32)  DEFAULT NULL COMMENT '单位',
  `qty`            DECIMAL(18,4) DEFAULT '0.0000' COMMENT '数量',
  `price`          DECIMAL(18,4) DEFAULT '0.0000' COMMENT '单价',
  `tax_amount`     DECIMAL(18,2) DEFAULT '0.00' COMMENT '税额',
  `total_amount`   DECIMAL(18,2) DEFAULT '0.00' COMMENT '价税合计',
  `return_qty`     DECIMAL(18,4) DEFAULT '0.0000' COMMENT '退货数量',
  `sync_time`      DATETIME     DEFAULT NULL COMMENT '同步时间',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kd_bill_entry` (`kd_bill_id`, `kd_entry_id`),
  KEY `idx_bill_no` (`bill_no`),
  KEY `idx_bill_date` (`bill_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金蝶销售出库单同步数据';

-- 菜单：系统集成 → 金蝶销售出库单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063120, '金蝶销售出库单', 9063000, 4, 'sal-outbound', 'kingdee/salOutbound/index', '', '',
       1, 0, 'C', '0', '0', 'kingdee:saloutbound:list', 'shopping', 'admin', NOW(), '金蝶云星辰销售出库单同步列表'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063120);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063121, '同步金蝶', 9063120, 1, '', '', '', '',
       1, 0, 'F', '0', '0', 'kingdee:saloutbound:sync', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063121);

-- 金蝶集成页「立即同步」中的销售出库单按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063122, '销售出库单同步', 9063109, 5, '', '', '', '',
       1, 0, 'F', '0', '0', 'kingdee:sync:saloutbound', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063122);

-- 已拥有「供应商同步」权限的角色同步授予
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 9063122
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.perms = 'kingdee:sync:supplier';

-- ============ add_kd_pur_in_stock_20261114.sql ============
-- 金蝶采购入库单同步
-- 表：kd_pur_in_stock（一行 = 一张单据的一条商品分录）
CREATE TABLE IF NOT EXISTS `kd_pur_in_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `kd_bill_id` varchar(64) NOT NULL COMMENT '金蝶单据ID',
  `kd_entry_id` varchar(64) NOT NULL COMMENT '金蝶分录ID',
  `bill_date` date DEFAULT NULL COMMENT '单据日期',
  `bill_no` varchar(64) DEFAULT NULL COMMENT '单据编号',
  `supplier_name` varchar(255) DEFAULT NULL COMMENT '供应商',
  `purchaser_name` varchar(64) DEFAULT NULL COMMENT '采购员',
  `dept_name` varchar(128) DEFAULT NULL COMMENT '部门',
  `pay_status` varchar(32) DEFAULT NULL COMMENT '付款状态',
  `unpaid_amount` decimal(18,6) DEFAULT NULL COMMENT '未付款金额',
  `paid_amount` decimal(18,6) DEFAULT NULL COMMENT '已付款金额',
  `material_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `warehouse_name` varchar(128) DEFAULT NULL COMMENT '仓库',
  `unit_name` varchar(32) DEFAULT NULL COMMENT '单位',
  `qty` decimal(18,6) DEFAULT NULL COMMENT '数量',
  `price` decimal(18,6) DEFAULT NULL COMMENT '单价',
  `tax_amount` decimal(18,6) DEFAULT NULL COMMENT '税额',
  `total_amount` decimal(18,6) DEFAULT NULL COMMENT '价税合计',
  `return_qty` decimal(18,6) DEFAULT NULL COMMENT '退货数量',
  `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_entry` (`kd_bill_id`, `kd_entry_id`),
  KEY `idx_bill_no` (`bill_no`),
  KEY `idx_supplier` (`supplier_name`),
  KEY `idx_bill_date` (`bill_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金蝶采购入库单';

-- 菜单：系统集成 → 金蝶采购入库单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063130, '金蝶采购入库单', 9063000, 3, 'pur-in-stock', 'kingdee/purInStock/index', '', '',
       1, 0, 'C', '0', '0', 'kingdee:purinstock:list', 'shopping', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063130);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063131, '查询', 9063130, 1, '', '', '', '',
       1, 0, 'F', '0', '0', 'kingdee:purinstock:list', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063131);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063132, '同步金蝶', 9063130, 2, '', '', '', '',
       1, 0, 'F', '0', '0', 'kingdee:purinstock:sync', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063132);

-- 金蝶集成页「立即同步」中的采购入库单按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
                      is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063133, '采购入库单同步', 9063109, 6, '', '', '', '',
       1, 0, 'F', '0', '0', 'kingdee:sync:purinstock', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063133);

-- 已拥有「供应商同步」权限的角色同步授予
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id AND m.perms = 'kingdee:sync:supplier'
CROSS JOIN sys_menu m2
WHERE m2.menu_id IN (9063130, 9063131, 9063132, 9063133);

-- ============ add_kd_inventory_20261114.sql ============
-- ============================================================
-- 金蝶库存查询表 + 菜单
-- 日期：2026-11-14
-- 幂等：建表 IF NOT EXISTS、菜单/授权判重，可重复执行
-- ============================================================
CREATE TABLE IF NOT EXISTS `kd_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `material_id` varchar(64) DEFAULT NULL COMMENT '金蝶商品ID',
  `material_number` varchar(64) DEFAULT NULL COMMENT '商品编号',
  `material_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
  `spec_model` varchar(255) DEFAULT NULL COMMENT '规格型号',
  `stock_id` varchar(64) DEFAULT NULL COMMENT '仓库ID',
  `stock_name` varchar(128) DEFAULT NULL COMMENT '仓库名称',
  `unit_name` varchar(64) DEFAULT NULL COMMENT '基本单位',
  `qty` decimal(18,4) DEFAULT NULL COMMENT '即时库存',
  `valid_qty` decimal(18,4) DEFAULT NULL COMMENT '可用库存',
  `available_out_qty` decimal(18,4) DEFAULT NULL COMMENT '可出库数量',
  `category_id` varchar(64) DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(128) DEFAULT NULL COMMENT '分类名称',
  `category_sort` int DEFAULT NULL COMMENT '商品类别排序(金蝶)',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
  `sync_time` datetime DEFAULT NULL COMMENT '同步时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_stock` (`material_id`, `stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金蝶库存查询';

-- 已存在表时补充商品类别排序列（金蝶商品类别展示顺序）
SET @has_cat_sort := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'kd_inventory' AND column_name = 'category_sort'
);
SET @ddl_cat_sort := IF(@has_cat_sort = 0,
    'ALTER TABLE `kd_inventory` ADD COLUMN `category_sort` int DEFAULT NULL COMMENT ''商品类别排序(金蝶)'' AFTER `category_name`',
    'SELECT ''kd_inventory.category_sort 已存在，跳过'' AS msg');
PREPARE stmt_cat_sort FROM @ddl_cat_sort;
EXECUTE stmt_cat_sort;
DEALLOCATE PREPARE stmt_cat_sort;

-- 菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063140, '金蝶库存查询', 9063000, 5, 'inventory', 'kingdee/inventory/index', 1, 0, 'C', '0', '0', 'kingdee:inventory:list', 'shopping', 'admin', sysdate(), '金蝶库存查询'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063140);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063141, '库存查询', 9063140, 1, '', '', 1, 0, 'F', '0', '0', 'kingdee:inventory:list', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063141);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063142, '同步金蝶', 9063140, 2, '', '', 1, 0, 'F', '0', '0', 'kingdee:inventory:sync', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063142);

-- 集成页同步按钮菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063143, '库存同步', 9063000, 6, '', '', 1, 0, 'F', '0', '0', 'kingdee:sync:inventory', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063143);

-- 分配给admin角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, t.menu_id FROM (SELECT 9063140 AS menu_id UNION ALL SELECT 9063141 UNION ALL SELECT 9063142 UNION ALL SELECT 9063143) t
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = t.menu_id);

-- ============ upgrade_wms_supplier_rate_20261114.sql ============
-- ============================================================
-- 供应商表新增「税率」字段（金蝶供应商同步写入 rate）
-- 日期：2026-11-14
-- 幂等：已存在 rate 列时跳过，可重复执行
-- ============================================================
SET NAMES utf8mb4;

SET @has_rate := (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'wms_supplier'
      AND column_name = 'rate'
);

SET @ddl := IF(@has_rate = 0,
    'ALTER TABLE `wms_supplier` ADD COLUMN `rate` varchar(20) DEFAULT NULL COMMENT ''税率'' AFTER `tax_number`',
    'SELECT ''wms_supplier.rate 已存在，跳过'' AS msg');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============ upgrade_mk_customer_type_20261114.sql ============
-- ============================================================
-- 客户类型字典对齐金蝶客户分类（mk_customer_type）
-- 日期：2026-11-14
-- 说明：1 企业客户 → 直营客户；新增 4 代理商、5 其他
-- 幂等：可重复执行
-- ============================================================
SET NAMES utf8mb4;

-- 1. 字典类型（客户类型）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '客户类型', 'mk_customer_type', '0', 'admin', NOW(), '客户分类（对齐金蝶客户分类）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_customer_type');

-- 2. 原「企业客户」更名为「直营客户」（金蝶客户分类）
UPDATE sys_dict_data
SET dict_label = '直营客户', update_by = 'admin', update_time = NOW()
WHERE dict_type = 'mk_customer_type' AND dict_value = '1' AND dict_label <> '直营客户';

-- 3. 补充 代理商 / 其他
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '代理商', '4', 'mk_customer_type', '', '', 'N', '0', 'admin', NOW(), '金蝶客户分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_customer_type' AND dict_value = '4');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '其他', '5', 'mk_customer_type', '', 'info', 'N', '0', 'admin', NOW(), '金蝶客户分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_customer_type' AND dict_value = '5');

-- ============ upgrade_wms_supplier_type_20261114.sql ============
-- ============================================================
-- 供应商类型字典补充金蝶供应商分类（wms_supplier_type）
-- 日期：2026-11-14
-- 说明：金蝶供应商同步会把「供应商分类（group_name）」原样写入
--       wms_supplier.supplier_type，此处补齐对应字典项，
--       保证列表筛选、编辑弹窗下拉、详情回显可正常使用。
-- 幂等：按 dict_value 判重，可重复执行
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '供应商类型', 'wms_supplier_type', '0', 'admin', NOW(), '供应商类型（含金蝶供应商分类）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'wms_supplier_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 10, '原材料', '原材料', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '原材料');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 11, '包装材料', '包装材料', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '包装材料');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 12, '服务类', '服务类', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '服务类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 13, '机器设备', '机器设备', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '机器设备');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 14, '渠道合作', '渠道合作', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '渠道合作');

