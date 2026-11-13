-- ============================================================
-- 升级脚本：PJM 项目管理 - 开票管理功能
-- 日期：2026-11-07
-- 背景：客户提出在项目回款基础上增加开票管理，日常经常查开票信息。
--       开票与回款为弱关联（payment_id 可空），无审批流，
--       状态流转：待开票(0) -> 已开票(1) -> 已作废(2)。
--       发票附件必须上传（统一 FileUpload 组件，多个附件逗号分隔 url 存储）。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，幂等可重复执行）
-- ============================================================
SET NAMES utf8mb4;

-- ============================================================
-- §1 建表 pjm_invoice（幂等：已存在则跳过）
-- ============================================================
CREATE TABLE IF NOT EXISTS `pjm_invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '开票ID',
  `invoice_no` varchar(50) NOT NULL COMMENT '开票单号(KP前缀)',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `payment_id` bigint DEFAULT NULL COMMENT '关联回款ID(弱关联,可空)',
  `invoice_type` char(1) DEFAULT '0' COMMENT '发票类型(字典 pjm_invoice_type)',
  `invoice_status` char(1) DEFAULT '0' COMMENT '发票状态(字典 pjm_invoice_status: 0待开票 1已开票 2已作废)',
  `invoice_amount` decimal(18,2) NOT NULL COMMENT '开票金额',
  `tax_rate` decimal(5,2) DEFAULT NULL COMMENT '税率(%)',
  `tax_amount` decimal(18,2) DEFAULT NULL COMMENT '税额',
  `invoice_title` varchar(200) DEFAULT NULL COMMENT '发票抬头',
  `tax_no` varchar(50) DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_date` date DEFAULT NULL COMMENT '开票日期',
  `invoice_attachment` varchar(1000) DEFAULT NULL COMMENT '发票附件(多个url逗号分隔,必须上传)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`invoice_id`),
  KEY `idx_pjm_invoice_project` (`project_id`),
  KEY `idx_pjm_invoice_payment` (`payment_id`),
  KEY `idx_pjm_invoice_status` (`invoice_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目开票记录表';

-- ============================================================
-- §2 编号规则种子（幂等：已存在则跳过）
-- 单号规则与生成器调用约定一致：
--   PjmInvoiceServiceImpl -> PJM_INVOICE
-- ============================================================

-- 回滚语句（如需移除本规则时执行）：
-- DELETE FROM mk_number_rule WHERE rule_code = 'PJM_INVOICE';

-- 开票单编号（每日重置，如 KP202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_INVOICE', 'pjm', '开票单编号', 'KP', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '开票单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_INVOICE');

-- ============================================================
-- §3 字典类型与字典数据（幂等：已存在则跳过）
-- ============================================================

-- 回滚语句（如需移除字典时执行）：
-- DELETE FROM sys_dict_data WHERE dict_type IN ('pjm_invoice_type','pjm_invoice_status');
-- DELETE FROM sys_dict_type WHERE dict_type IN ('pjm_invoice_type','pjm_invoice_status');

-- 字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '项目发票类型', 'pjm_invoice_type', '0', '项目管理-开票管理-发票类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_invoice_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '项目发票状态', 'pjm_invoice_status', '0', '项目管理-开票管理-发票状态' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_invoice_status');

-- 字典数据：发票类型
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'增值税专用发票','0','pjm_invoice_type','','primary','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_type' AND `dict_value`='0');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'增值税普通发票','1','pjm_invoice_type','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'电子发票','2','pjm_invoice_type','','success','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_type' AND `dict_value`='2');

-- 字典数据：发票状态
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'待开票','0','pjm_invoice_status','','warning','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_status' AND `dict_value`='0');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'已开票','1','pjm_invoice_status','','success','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_status' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'已作废','2','pjm_invoice_status','','danger','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_status' AND `dict_value`='2');

-- ============================================================
-- §4 菜单（幂等：固定 menu_id + INSERT IGNORE）
-- 开票管理挂在项目管理(906202)下，与回款管理(项目付款 906602)平级
-- menu_id 避开 906604（已用），启用 906605
-- ============================================================

-- 回滚语句（如需移除菜单时执行）：
-- DELETE FROM sys_role_menu WHERE menu_id = 906605 OR parent_id = 906605;
-- DELETE FROM sys_menu WHERE menu_id = 906605 OR parent_id = 906605;

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name) VALUES
(906605, '开票管理', 906202, 10, 'invoice', 'pjm/invoice/index', 'C', '0', '0', '', 'ticket', 'admin', NOW(), '项目开票', 'PjmInvoice');

SET @invoiceMenuId = 906605;

-- 按钮权限（F 类型，menu_id 自增，逐条判重防止重复）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票查询', @invoiceMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:query' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票登记', @invoiceMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:add' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票修改', @invoiceMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:edit' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票删除', @invoiceMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:remove' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票确认', @invoiceMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:issue', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:issue' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票作废', @invoiceMenuId, 6, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:void', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:void' AND parent_id = @invoiceMenuId);

-- 给 admin(role_id=1) 授权整个开票管理子树
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id = 906605 OR parent_id = 906605;

-- ============================================================
-- §5 验证
-- ============================================================
SELECT CONCAT('VERIFY: pjm_invoice exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pjm_invoice';
SELECT CONCAT('VERIFY: PJM_INVOICE rule = ', COUNT(*)) FROM mk_number_rule WHERE rule_code='PJM_INVOICE';
SELECT CONCAT('VERIFY: pjm invoice dicts = ', COUNT(*)) FROM sys_dict_type WHERE dict_type IN ('pjm_invoice_type','pjm_invoice_status');
SELECT CONCAT('VERIFY: invoice menu = ', COUNT(*)) FROM sys_menu WHERE menu_id = 906605;
SELECT CONCAT('VERIFY: invoice buttons = ', COUNT(*)) FROM sys_menu WHERE parent_id = 906605;
