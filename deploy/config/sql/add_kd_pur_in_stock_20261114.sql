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
