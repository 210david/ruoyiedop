-- 幂等修复脚本：处理已部分执行的情况

-- 1. 检查并添加缺失的列（通过存储过程）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
  IN table_name_param VARCHAR(100),
  IN column_name_param VARCHAR(100),
  IN column_def VARCHAR(500)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name_param
      AND COLUMN_NAME = column_name_param
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', table_name_param, '` ADD COLUMN ', column_def);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;

-- mk_payment_plan 新增字段
CALL add_column_if_not_exists('mk_payment_plan', 'order_id', '`order_id` bigint DEFAULT NULL COMMENT ''关联订单ID（独立创建时使用）'' AFTER `contract_id`');
CALL add_column_if_not_exists('mk_payment_plan', 'payment_method', '`payment_method` varchar(10) DEFAULT NULL COMMENT ''回款方式'' AFTER `payment_status`');
CALL add_column_if_not_exists('mk_payment_plan', 'bank_account', '`bank_account` varchar(100) DEFAULT NULL COMMENT ''收款银行账户'' AFTER `payment_method`');
CALL add_column_if_not_exists('mk_payment_plan', 'receipt_attachment', '`receipt_attachment` varchar(500) DEFAULT NULL COMMENT ''收款凭证附件'' AFTER `bank_account`');
CALL add_column_if_not_exists('mk_payment_plan', 'overdue_flag', '`overdue_flag` char(1) DEFAULT ''0'' COMMENT ''逾期标志'' AFTER `receipt_attachment`');

-- mk_order 新增字段
CALL add_column_if_not_exists('mk_order', 'confirm_time', '`confirm_time` datetime DEFAULT NULL COMMENT ''确认时间'' AFTER `finish_time`');
CALL add_column_if_not_exists('mk_order', 'receive_time', '`receive_time` datetime DEFAULT NULL COMMENT ''签收时间'' AFTER `confirm_time`');
CALL add_column_if_not_exists('mk_order', 'receive_person', '`receive_person` varchar(64) DEFAULT NULL COMMENT ''签收人'' AFTER `receive_time`');
CALL add_column_if_not_exists('mk_order', 'cancel_reason', '`cancel_reason` varchar(500) DEFAULT NULL COMMENT ''取消原因'' AFTER `receive_person`');

-- 修改 contract_id 允许为空
ALTER TABLE `mk_payment_plan` MODIFY COLUMN `contract_id` bigint DEFAULT NULL COMMENT '合同ID（独立创建时可为空）';

DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 2. 新建表（已存在则重建）
DROP TABLE IF EXISTS `mk_payment_record`;
CREATE TABLE IF NOT EXISTS `mk_payment_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `plan_id` bigint NOT NULL COMMENT '回款计划ID',
  `contract_id` bigint DEFAULT NULL COMMENT '合同ID',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `this_amount` decimal(14,2) NOT NULL COMMENT '本次回款金额',
  `payment_method` varchar(10) DEFAULT NULL COMMENT '回款方式',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '收款银行账户',
  `receipt_attachment` varchar(500) DEFAULT NULL COMMENT '收款凭证附件',
  `payment_date` date NOT NULL COMMENT '回款日期',
  `confirm_status` varchar(10) DEFAULT '0' COMMENT '确认状态',
  `confirm_by` varchar(64) DEFAULT NULL COMMENT '确认人',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`record_id`),
  KEY `idx_record_plan` (`plan_id`),
  KEY `idx_record_contract` (`contract_id`),
  KEY `idx_record_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回款记录表';

CREATE TABLE IF NOT EXISTS `mk_order_return` (
  `return_id` bigint NOT NULL AUTO_INCREMENT COMMENT '退货ID',
  `return_no` varchar(32) NOT NULL COMMENT '退货编号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) DEFAULT NULL COMMENT '订单编号',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(200) DEFAULT NULL COMMENT '客户名称',
  `return_amount` decimal(14,2) NOT NULL COMMENT '退货金额',
  `return_reason` varchar(500) NOT NULL COMMENT '退货原因',
  `return_status` varchar(10) DEFAULT '0' COMMENT '退货状态',
  `refund_amount` decimal(14,2) DEFAULT NULL COMMENT '退款金额',
  `refund_date` date DEFAULT NULL COMMENT '退款日期',
  `approve_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`return_id`),
  UNIQUE KEY `uk_return_no` (`return_no`),
  KEY `idx_return_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单退货表';

CREATE TABLE IF NOT EXISTS `mk_invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `invoice_no` varchar(32) NOT NULL COMMENT '发票编号',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `order_no` varchar(32) DEFAULT NULL COMMENT '订单编号',
  `contract_id` bigint DEFAULT NULL COMMENT '关联合同ID',
  `contract_no` varchar(32) DEFAULT NULL COMMENT '合同编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `customer_name` varchar(200) DEFAULT NULL COMMENT '客户名称',
  `invoice_type` varchar(10) DEFAULT '0' COMMENT '发票类型',
  `invoice_status` varchar(10) DEFAULT '0' COMMENT '发票状态',
  `invoice_amount` decimal(14,2) NOT NULL COMMENT '开票金额',
  `tax_rate` decimal(5,2) DEFAULT '13.00' COMMENT '税率',
  `tax_amount` decimal(14,2) DEFAULT NULL COMMENT '税额',
  `invoice_title` varchar(200) DEFAULT NULL COMMENT '发票抬头',
  `tax_no` varchar(32) DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_date` date DEFAULT NULL COMMENT '开票日期',
  `invoice_attachment` varchar(500) DEFAULT NULL COMMENT '发票附件',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`invoice_id`),
  UNIQUE KEY `uk_invoice_no` (`invoice_no`),
  KEY `idx_invoice_order` (`order_id`),
  KEY `idx_invoice_contract` (`contract_id`),
  KEY `idx_invoice_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票表';

CREATE TABLE IF NOT EXISTS `mk_contract_change_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `contract_no` varchar(32) DEFAULT NULL COMMENT '合同编号',
  `change_type` varchar(10) NOT NULL COMMENT '变更类型',
  `field_name` varchar(100) DEFAULT NULL COMMENT '变更字段',
  `old_value` varchar(500) DEFAULT NULL COMMENT '原值',
  `new_value` varchar(500) DEFAULT NULL COMMENT '新值',
  `change_reason` varchar(500) NOT NULL COMMENT '变更原因',
  `change_status` varchar(10) DEFAULT '0' COMMENT '变更状态',
  `approve_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_change_contract` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同变更记录表';

-- 3. 字典类型（幂等）
INSERT IGNORE INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('回款方式', 'marketing_payment_method', '0', 'admin', NOW(), '回款方式字典'),
('发票类型', 'marketing_invoice_type', '0', 'admin', NOW(), '发票类型字典'),
('发票状态', 'marketing_invoice_status', '0', 'admin', NOW(), '发票状态字典'),
('退货状态', 'marketing_return_status', '0', 'admin', NOW(), '退货状态字典'),
('合同变更类型', 'marketing_contract_change_type', '0', 'admin', NOW(), '合同变更类型字典'),
('合同变更状态', 'marketing_contract_change_status', '0', 'admin', NOW(), '合同变更状态字典'),
('回款确认状态', 'marketing_payment_confirm', '0', 'admin', NOW(), '回款确认状态字典');

-- 4. 字典数据（幂等）
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '银行转账', '0', 'marketing_payment_method', 'primary', 'Y', '0', 'admin', NOW(), ''),
(2, '承兑汇票', '1', 'marketing_payment_method', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '现金', '2', 'marketing_payment_method', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '其他', '3', 'marketing_payment_method', 'info', 'N', '0', 'admin', NOW(), ''),
(1, '增值税专用发票', '0', 'marketing_invoice_type', 'primary', 'Y', '0', 'admin', NOW(), ''),
(2, '增值税普通发票', '1', 'marketing_invoice_type', 'info', 'N', '0', 'admin', NOW(), ''),
(3, '电子发票', '2', 'marketing_invoice_type', 'success', 'N', '0', 'admin', NOW(), ''),
(1, '待开票', '0', 'marketing_invoice_status', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已开票', '1', 'marketing_invoice_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已作废', '2', 'marketing_invoice_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(1, '待审批', '0', 'marketing_return_status', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已批准', '1', 'marketing_return_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已驳回', '2', 'marketing_return_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(4, '已退款', '3', 'marketing_return_status', 'info', 'N', '0', 'admin', NOW(), ''),
(1, '金额变更', '1', 'marketing_contract_change_type', 'primary', 'Y', '0', 'admin', NOW(), ''),
(2, '日期变更', '2', 'marketing_contract_change_type', 'info', 'N', '0', 'admin', NOW(), ''),
(3, '条款变更', '3', 'marketing_contract_change_type', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '其他', '4', 'marketing_contract_change_type', 'success', 'N', '0', 'admin', NOW(), ''),
(1, '待审批', '0', 'marketing_contract_change_status', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已批准', '1', 'marketing_contract_change_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已驳回', '2', 'marketing_contract_change_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(4, '已执行', '3', 'marketing_contract_change_status', 'info', 'N', '0', 'admin', NOW(), ''),
(1, '待确认', '0', 'marketing_payment_confirm', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已确认', '1', 'marketing_payment_confirm', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已驳回', '2', 'marketing_payment_confirm', 'danger', 'N', '0', 'admin', NOW(), '');

-- 更新订单状态字典
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'marketing_order_status';
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待确认', '0', 'marketing_order_status', 'info', 'Y', '0', 'admin', NOW(), ''),
(2, '已确认', '1', 'marketing_order_status', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '已发货', '2', 'marketing_order_status', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '已签收', '3', 'marketing_order_status', 'success', 'N', '0', 'admin', NOW(), ''),
(5, '已完成', '4', 'marketing_order_status', 'success', 'N', '0', 'admin', NOW(), ''),
(6, '已取消', '5', 'marketing_order_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(7, '退货中', '6', 'marketing_order_status', 'warning', 'N', '0', 'admin', NOW(), ''),
(8, '已退货', '7', 'marketing_order_status', 'danger', 'N', '0', 'admin', NOW(), '');

-- 更新回款状态字典
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'marketing_payment_status';
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待回款', '0', 'marketing_payment_status', 'info', 'Y', '0', 'admin', NOW(), ''),
(2, '已回款', '1', 'marketing_payment_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '部分回款', '3', 'marketing_payment_status', 'warning', 'N', '0', 'admin', NOW(), '');

-- 5. 菜单（幂等 - 通过 perms 唯一性检查）
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票管理', m.menu_id, 13, 'invoice', 'mk/invoice/index', NULL, 1, 0, 'C', '0', '0', 'marketing:invoice:list', 'edit', 'admin', NOW(), '' 
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '营销管理' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:list');

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单退货', m.menu_id, 14, 'return', 'mk/return/index', NULL, 1, 0, 'C', '0', '0', 'marketing:return:list', 'error', 'admin', NOW(), '' 
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '营销管理' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:list');

-- 发票管理按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票查询', menu_id, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:query', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:query');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票新增', menu_id, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:add', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:add');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票修改', menu_id, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:edit', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:edit');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票删除', menu_id, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:remove', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:remove');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票导出', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:export', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:export');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票开票', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:issue', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:issue');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票作废', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:void', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:void');

-- 退货按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '退货查询', menu_id, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:query', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:query');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '退货新增', menu_id, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:add', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:add');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '退货删除', menu_id, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:remove', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:remove');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '退货导出', menu_id, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:export', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:export');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '退货审批', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:approve', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:approve');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '退货退款', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:refund', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:refund');

-- 回款管理新增按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款新增', menu_id, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:add', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:payment:add');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款删除', menu_id, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:remove', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:payment:remove');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款确认', menu_id, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:confirm', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:payment:confirm');

-- 订单管理新增按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单确认', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:confirm', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:order:confirm');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单签收', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:receive', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:order:receive');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单取消', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:cancel', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:order:cancel');

-- 合同变更按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '合同变更', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:contract:change', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:contract:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:contract:change');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '合同续签', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:contract:renew', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:contract:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:contract:renew');
