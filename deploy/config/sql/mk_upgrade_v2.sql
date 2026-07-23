-- =====================================================
-- 营销模块 P0-P3 升级脚本
-- 包含：回款管理增强、订单管理增强、合同管理增强、发票管理
-- =====================================================

-- =============================================
-- 1. 回款计划表升级：新增字段
-- =============================================
ALTER TABLE `mk_payment_plan` 
  ADD COLUMN `order_id` bigint DEFAULT NULL COMMENT '关联订单ID（独立创建时使用）' AFTER `contract_id`,
  ADD COLUMN `payment_method` varchar(10) DEFAULT NULL COMMENT '回款方式（0银行转账 1承兑汇票 2现金 3其他）' AFTER `payment_status`,
  ADD COLUMN `bank_account` varchar(100) DEFAULT NULL COMMENT '收款银行账户' AFTER `payment_method`,
  ADD COLUMN `receipt_attachment` varchar(500) DEFAULT NULL COMMENT '收款凭证附件' AFTER `bank_account`,
  ADD COLUMN `overdue_flag` char(1) DEFAULT '0' COMMENT '逾期标志（0未逾期 1已逾期）' AFTER `receipt_attachment`;

-- 修改 contract_id 允许为空（独立创建回款计划时可以不关联合同）
ALTER TABLE `mk_payment_plan` MODIFY COLUMN `contract_id` bigint DEFAULT NULL COMMENT '合同ID（独立创建时可为空）';

-- 回款状态扩展：0待回款 1已回款 2已逾期 3部分回款
-- (已有数据不需要迁移，状态码兼容)

-- =============================================
-- 2. 新增回款记录表（支持一个计划多次回款）
-- =============================================
DROP TABLE IF EXISTS `mk_payment_record`;
CREATE TABLE `mk_payment_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `plan_id` bigint NOT NULL COMMENT '回款计划ID',
  `contract_id` bigint DEFAULT NULL COMMENT '合同ID',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `this_amount` decimal(14,2) NOT NULL COMMENT '本次回款金额',
  `payment_method` varchar(10) DEFAULT NULL COMMENT '回款方式（0银行转账 1承兑汇票 2现金 3其他）',
  `bank_account` varchar(100) DEFAULT NULL COMMENT '收款银行账户',
  `receipt_attachment` varchar(500) DEFAULT NULL COMMENT '收款凭证附件',
  `payment_date` date NOT NULL COMMENT '回款日期',
  `confirm_status` varchar(10) DEFAULT '0' COMMENT '确认状态（0待确认 1已确认 2已驳回）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='回款记录表（多次回款）';

-- =============================================
-- 3. 订单表升级：新增字段
-- =============================================
ALTER TABLE `mk_order`
  ADD COLUMN `confirm_time` datetime DEFAULT NULL COMMENT '确认时间' AFTER `finish_time`,
  ADD COLUMN `receive_time` datetime DEFAULT NULL COMMENT '签收时间' AFTER `confirm_time`,
  ADD COLUMN `receive_person` varchar(64) DEFAULT NULL COMMENT '签收人' AFTER `receive_time`,
  ADD COLUMN `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因' AFTER `receive_person`;

-- 订单状态扩展：0待确认 1已确认 2已发货 3已签收 4已完成 5已取消 6退货中 7已退货
-- 需迁移已有数据
UPDATE `mk_order` SET `order_status` = '1' WHERE `order_status` = '0'; -- 旧"待发货"->"已确认"
UPDATE `mk_order` SET `order_status` = '2' WHERE `order_status` = '1'; -- 旧"已发货"->"已发货"(新)
UPDATE `mk_order` SET `order_status` = '4' WHERE `order_status` = '2'; -- 旧"已完成"->"已完成"(新)

-- =============================================
-- 4. 新增订单退货表
-- =============================================
DROP TABLE IF EXISTS `mk_order_return`;
CREATE TABLE `mk_order_return` (
  `return_id` bigint NOT NULL AUTO_INCREMENT COMMENT '退货ID',
  `return_no` varchar(32) NOT NULL COMMENT '退货编号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) DEFAULT NULL COMMENT '订单编号',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID',
  `customer_name` varchar(200) DEFAULT NULL COMMENT '客户名称',
  `return_amount` decimal(14,2) NOT NULL COMMENT '退货金额',
  `return_reason` varchar(500) NOT NULL COMMENT '退货原因',
  `return_status` varchar(10) DEFAULT '0' COMMENT '退货状态（0待审批 1已批准 2已驳回 3已退款）',
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

-- =============================================
-- 5. 新增发票表
-- =============================================
DROP TABLE IF EXISTS `mk_invoice`;
CREATE TABLE `mk_invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `invoice_no` varchar(32) NOT NULL COMMENT '发票编号',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `order_no` varchar(32) DEFAULT NULL COMMENT '订单编号',
  `contract_id` bigint DEFAULT NULL COMMENT '关联合同ID',
  `contract_no` varchar(32) DEFAULT NULL COMMENT '合同编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `customer_name` varchar(200) DEFAULT NULL COMMENT '客户名称',
  `invoice_type` varchar(10) DEFAULT '0' COMMENT '发票类型（0增值税专用发票 1增值税普通发票 2电子发票）',
  `invoice_status` varchar(10) DEFAULT '0' COMMENT '发票状态（0待开票 1已开票 2已作废）',
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

-- =============================================
-- 6. 合同变更记录表
-- =============================================
DROP TABLE IF EXISTS `mk_contract_change_log`;
CREATE TABLE `mk_contract_change_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `contract_id` bigint NOT NULL COMMENT '合同ID',
  `contract_no` varchar(32) DEFAULT NULL COMMENT '合同编号',
  `change_type` varchar(10) NOT NULL COMMENT '变更类型（1金额变更 2日期变更 3条款变更 4其他）',
  `field_name` varchar(100) DEFAULT NULL COMMENT '变更字段',
  `old_value` varchar(500) DEFAULT NULL COMMENT '原值',
  `new_value` varchar(500) DEFAULT NULL COMMENT '新值',
  `change_reason` varchar(500) NOT NULL COMMENT '变更原因',
  `change_status` varchar(10) DEFAULT '0' COMMENT '变更状态（0待审批 1已批准 2已驳回 3已执行）',
  `approve_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approve_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_change_contract` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同变更记录表';

-- =============================================
-- 7. 新增字典类型
-- =============================================
INSERT IGNORE INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('回款方式', 'marketing_payment_method', '0', 'admin', NOW(), '回款方式字典'),
('发票类型', 'marketing_invoice_type', '0', 'admin', NOW(), '发票类型字典'),
('发票状态', 'marketing_invoice_status', '0', 'admin', NOW(), '发票状态字典'),
('退货状态', 'marketing_return_status', '0', 'admin', NOW(), '退货状态字典'),
('合同变更类型', 'marketing_contract_change_type', '0', 'admin', NOW(), '合同变更类型字典'),
('合同变更状态', 'marketing_contract_change_status', '0', 'admin', NOW(), '合同变更状态字典'),
('回款确认状态', 'marketing_payment_confirm', '0', 'admin', NOW(), '回款确认状态字典');

-- =============================================
-- 8. 新增字典数据
-- =============================================
-- 回款方式
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '银行转账', '0', 'marketing_payment_method', 'primary', 'Y', '0', 'admin', NOW(), ''),
(2, '承兑汇票', '1', 'marketing_payment_method', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '现金', '2', 'marketing_payment_method', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '其他', '3', 'marketing_payment_method', 'info', 'N', '0', 'admin', NOW(), '');

-- 发票类型
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '增值税专用发票', '0', 'marketing_invoice_type', 'primary', 'Y', '0', 'admin', NOW(), ''),
(2, '增值税普通发票', '1', 'marketing_invoice_type', 'info', 'N', '0', 'admin', NOW(), ''),
(3, '电子发票', '2', 'marketing_invoice_type', 'success', 'N', '0', 'admin', NOW(), '');

-- 发票状态
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待开票', '0', 'marketing_invoice_status', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已开票', '1', 'marketing_invoice_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已作废', '2', 'marketing_invoice_status', 'danger', 'N', '0', 'admin', NOW(), '');

-- 退货状态
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待审批', '0', 'marketing_return_status', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已批准', '1', 'marketing_return_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已驳回', '2', 'marketing_return_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(4, '已退款', '3', 'marketing_return_status', 'info', 'N', '0', 'admin', NOW(), '');

-- 合同变更类型
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '金额变更', '1', 'marketing_contract_change_type', 'primary', 'Y', '0', 'admin', NOW(), ''),
(2, '日期变更', '2', 'marketing_contract_change_type', 'info', 'N', '0', 'admin', NOW(), ''),
(3, '条款变更', '3', 'marketing_contract_change_type', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '其他', '4', 'marketing_contract_change_type', 'success', 'N', '0', 'admin', NOW(), '');

-- 合同变更状态
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待审批', '0', 'marketing_contract_change_status', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已批准', '1', 'marketing_contract_change_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已驳回', '2', 'marketing_contract_change_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(4, '已执行', '3', 'marketing_contract_change_status', 'info', 'N', '0', 'admin', NOW(), '');

-- 回款确认状态
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待确认', '0', 'marketing_payment_confirm', 'warning', 'Y', '0', 'admin', NOW(), ''),
(2, '已确认', '1', 'marketing_payment_confirm', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '已驳回', '2', 'marketing_payment_confirm', 'danger', 'N', '0', 'admin', NOW(), '');

-- 更新订单状态字典（扩展为7个状态）
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'marketing_order_status';
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待确认', '0', 'marketing_order_status', 'info', 'Y', '0', 'admin', NOW(), ''),
(2, '已确认', '1', 'marketing_order_status', 'primary', 'N', '0', 'admin', NOW(), ''),
(3, '已发货', '2', 'marketing_order_status', 'warning', 'N', '0', 'admin', NOW(), ''),
(4, '已签收', '3', 'marketing_order_status', 'success', 'N', '0', 'admin', NOW(), ''),
(5, '已完成', '4', 'marketing_order_status', 'success', 'N', '0', 'admin', NOW(), ''),
(6, '已取消', '5', 'marketing_order_status', 'danger', 'N', '0', 'admin', NOW(), ''),
(7, '退货中', '6', 'marketing_order_status', 'warning', 'N', '0', 'admin', NOW(), ''),
(8, '已退货', '7', 'marketing_order_status', 'danger', 'N', '0', 'admin', NOW(), '');

-- 更新回款状态字典（扩展为4个状态）
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'marketing_payment_status';
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '待回款', '0', 'marketing_payment_status', 'info', 'Y', '0', 'admin', NOW(), ''),
(2, '已回款', '1', 'marketing_payment_status', 'success', 'N', '0', 'admin', NOW(), ''),
(3, '部分回款', '3', 'marketing_payment_status', 'warning', 'N', '0', 'admin', NOW(), '');

-- =============================================
-- 9. 新增菜单
-- =============================================
-- 发票管理菜单
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
VALUES ('发票管理', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '营销管理' LIMIT 1) t), 13, 'invoice', 'mk/invoice/index', NULL, 1, 0, 'C', '0', '0', 'marketing:invoice:list', 'edit', 'admin', NOW(), '');

-- 订单退货菜单
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
VALUES ('订单退货', (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '营销管理' LIMIT 1) t), 14, 'return', 'mk/return/index', NULL, 1, 0, 'C', '0', '0', 'marketing:return:list', 'error', 'admin', NOW(), '');

-- 回款统计菜单（隐藏菜单，作为回款管理子页面）
-- (回款统计在回款管理页面内通过Tab实现，不需要独立菜单)

-- 发票管理按钮权限
SET @invoiceMenuId = LAST_INSERT_ID();
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('发票查询', @invoiceMenuId, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:query', '#', 'admin', NOW(), ''),
('发票新增', @invoiceMenuId, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:add', '#', 'admin', NOW(), ''),
('发票修改', @invoiceMenuId, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:edit', '#', 'admin', NOW(), ''),
('发票删除', @invoiceMenuId, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:remove', '#', 'admin', NOW(), ''),
('发票导出', @invoiceMenuId, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:export', '#', 'admin', NOW(), ''),
('发票开票', @invoiceMenuId, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:issue', '#', 'admin', NOW(), ''),
('发票作废', @invoiceMenuId, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:invoice:void', '#', 'admin', NOW(), '');

-- 退货按钮权限
SET @returnMenuId = @invoiceMenuId + 8;
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('退货查询', @returnMenuId, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:query', '#', 'admin', NOW(), ''),
('退货新增', @returnMenuId, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:add', '#', 'admin', NOW(), ''),
('退货删除', @returnMenuId, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:remove', '#', 'admin', NOW(), ''),
('退货导出', @returnMenuId, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:export', '#', 'admin', NOW(), ''),
('退货审批', @returnMenuId, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:approve', '#', 'admin', NOW(), ''),
('退货退款', @returnMenuId, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:return:refund', '#', 'admin', NOW(), '');

-- 回款管理新增按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款新增', menu_id, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:add', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' LIMIT 1;

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款删除', menu_id, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:remove', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' LIMIT 1;

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款确认', menu_id, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:confirm', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' LIMIT 1;

-- 订单管理新增按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单确认', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:confirm', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' LIMIT 1;

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单签收', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:receive', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' LIMIT 1;

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单取消', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:cancel', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' LIMIT 1;

-- 合同变更按钮权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '合同变更', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:contract:change', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:contract:list' AND menu_type = 'C' LIMIT 1;

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '合同续签', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:contract:renew', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:contract:list' AND menu_type = 'C' LIMIT 1;
