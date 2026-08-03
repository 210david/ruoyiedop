-- =============================================
-- MK 营销管理 - 发货管理
-- 功能：独立的发货管理模块，支持同一订单/合同多次发货
-- =============================================

-- =============================================
-- 一、创建发货主表
-- =============================================
DROP TABLE IF EXISTS `mk_shipment`;
CREATE TABLE `mk_shipment` (
  `shipment_id`       BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '发货单ID',
  `shipment_no`       VARCHAR(50)   NOT NULL                 COMMENT '发货单号',
  `order_id`          BIGINT(20)    DEFAULT NULL             COMMENT '订单ID',
  `order_no`          VARCHAR(50)   DEFAULT NULL             COMMENT '订单编号（冗余）',
  `contract_id`       BIGINT(20)    DEFAULT NULL             COMMENT '合ID（可选，支持合同直接发货）',
  `contract_no`       VARCHAR(50)   DEFAULT NULL             COMMENT '合同编号（冗余）',
  `customer_id`       BIGINT(20)    DEFAULT NULL             COMMENT '客户ID',
  `customer_name`     VARCHAR(200)  DEFAULT NULL             COMMENT '客户名称（冗余）',
  `status`            VARCHAR(10)   DEFAULT '0'              COMMENT '状态（0待发货 1已发货 2已签收 3已取消）',
  `shipment_date`     DATE                                   COMMENT '发货日期',
  `logistics_company` VARCHAR(100)  DEFAULT NULL             COMMENT '物流公司',
  `tracking_no`       VARCHAR(100)  DEFAULT NULL             COMMENT '物流单号',
  `total_qty`         DECIMAL(18,2) DEFAULT 0                COMMENT '发货总数量',
  `total_amount`      DECIMAL(18,2) DEFAULT 0                COMMENT '发货总金额',
  `receiver_name`     VARCHAR(50)   DEFAULT NULL             COMMENT '收货人',
  `receiver_phone`    VARCHAR(50)   DEFAULT NULL             COMMENT '收货电话',
  `receiver_address`  VARCHAR(500)  DEFAULT NULL             COMMENT '收货地址',
  `shipper_id`        BIGINT(20)    DEFAULT NULL             COMMENT '发货人ID',
  `shipper_name`      VARCHAR(50)   DEFAULT NULL             COMMENT '发货人姓名',
  `del_flag`          CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
  `create_by`         VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`       DATETIME                               COMMENT '创建时间',
  `update_by`         VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`       DATETIME                               COMMENT '更新时间',
  `remark`            VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`shipment_id`),
  UNIQUE KEY `uk_shipment_no` (`shipment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_customer_id` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货管理主表';

-- =============================================
-- 二、创建发货明细表
-- =============================================
DROP TABLE IF EXISTS `mk_shipment_detail`;
CREATE TABLE `mk_shipment_detail` (
  `detail_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `shipment_id`   BIGINT(20)    NOT NULL                 COMMENT '发货单ID',
  `order_item_id` BIGINT(20)    DEFAULT NULL             COMMENT '订单明细ID',
  `product_name`  VARCHAR(200)  DEFAULT NULL             COMMENT '商品名称',
  `product_spec`  VARCHAR(200)  DEFAULT NULL             COMMENT '规格型号',
  `unit`          VARCHAR(50)   DEFAULT NULL             COMMENT '单位',
  `ship_qty`      DECIMAL(18,2) DEFAULT 0                COMMENT '发货数量',
  `unit_price`    DECIMAL(18,2) DEFAULT NULL             COMMENT '单价',
  `amount`        DECIMAL(18,2) DEFAULT 0                COMMENT '金额',
  `del_flag`      CHAR(1)       DEFAULT '0'              COMMENT '删除标志（0存在 2删除）',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  `remark`        VARCHAR(500)  DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`detail_id`),
  KEY `idx_shipment_id` (`shipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货管理明细表';

-- =============================================
-- 三、给订单明细表增加已发货数量字段（兼容已有delivered_qty列）
-- =============================================
-- 如果已有 delivered_qty 列则重命名为 shipped_qty，否则新增 shipped_qty
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_order_item' AND COLUMN_NAME = 'delivered_qty');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE `mk_order_item` CHANGE COLUMN `delivered_qty` `shipped_qty` DECIMAL(18,2) DEFAULT 0 COMMENT ''已发货数量''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists2 = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mk_order_item' AND COLUMN_NAME = 'shipped_qty');
SET @sql2 = IF(@col_exists2 = 0, 'ALTER TABLE `mk_order_item` ADD COLUMN `shipped_qty` DECIMAL(18,2) DEFAULT 0 COMMENT ''已发货数量'' AFTER `quantity`', 'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- =============================================
-- 四、发货状态字典
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('发货状态', 'marketing_shipment_status', '0', 'admin', sysdate(), '发货管理状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '待发货', '0', 'marketing_shipment_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''),
(2, '已发货', '1', 'marketing_shipment_status', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
(3, '已签收', '2', 'marketing_shipment_status', '', 'success', 'N', '0', 'admin', sysdate(), ''),
(4, '已取消', '3', 'marketing_shipment_status', '', 'danger', 'N', '0', 'admin', sysdate(), '');

-- =============================================
-- 五、编号规则
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`) VALUES
('shipment', '发货编号', 'FH', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '发货单编号，每日重置');

-- =============================================
-- 六、菜单配置
-- =============================================
-- 发货管理菜单（放在合同订单目录下，排序在订单管理之后）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4107, '发货管理', 4005, 3, 'shipment', 'mk/shipment/index', '', 'MkShipment', 1, 0, 'C', '0', '0', 'marketing:shipment:list', 'shopping', 'admin', sysdate(), '发货管理菜单');

-- 发货管理按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4171, '发货查询', 4107, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:query', '#', 'admin', sysdate(), ''),
(4172, '发货新增', 4107, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:add', '#', 'admin', sysdate(), ''),
(4173, '发货修改', 4107, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:edit', '#', 'admin', sysdate(), ''),
(4174, '发货删除', 4107, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:remove', '#', 'admin', sysdate(), ''),
(4175, '发货导出', 4107, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:export', '#', 'admin', sysdate(), ''),
(4176, '发货确认', 4107, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:deliver', '#', 'admin', sysdate(), ''),
(4177, '发货签收', 4107, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:shipment:receive', '#', 'admin', sysdate(), '');

-- 注意：实际环境中菜单ID可能因冲突而不同，请以数据库实际ID为准。如果菜单ID已被占用，请使用其他可用ID。

-- =============================================
-- 七、给admin角色分配发货管理菜单权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 4107), (1, 4171), (1, 4172), (1, 4173), (1, 4174), (1, 4175), (1, 4176), (1, 4177);
