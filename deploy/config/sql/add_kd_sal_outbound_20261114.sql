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
