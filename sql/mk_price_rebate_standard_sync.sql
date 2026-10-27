-- =============================================================
-- 价格与返利功能同步标准库（ry-vue）
-- 来源：RUOYI_CFSP 同名菜单/字典；目标库：ry-vue
-- 幂等：均带 NOT EXISTS 防重复插入
-- =============================================================
USE ry-vue;

-- 1. 营销管理(4000)子菜单顺延，为“价格与返利”(order 4)腾位
UPDATE sys_menu SET order_num = 9 WHERE menu_id = 4140 AND order_num < 9;
UPDATE sys_menu SET order_num = 8 WHERE menu_id = 4006 AND order_num < 8;
UPDATE sys_menu SET order_num = 7 WHERE menu_id = 4487 AND order_num < 7;
UPDATE sys_menu SET order_num = 6 WHERE menu_id = 4005 AND order_num < 6;
UPDATE sys_menu SET order_num = 5 WHERE menu_id = 4003 AND order_num < 5;

-- 2. 目录：价格与返利（营销管理下、商机管理前）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062210, '价格与返利', 4000, 4, 'price', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', NOW(), '价格政策与返利管理'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062210);

-- 3. 价格政策页 + 按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062211, '价格政策', 9062210, 1, 'pricePolicy', 'mk/pricePolicy/index', NULL, '', 1, 0, 'C', '0', '0', 'marketing:price:list', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062211);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062212, '价格查询', 9062211, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:price:query', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062212);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062213, '价格新增', 9062211, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:price:add', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062213);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062214, '价格修改', 9062211, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:price:edit', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062214);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062215, '价格删除', 9062211, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:price:remove', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062215);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062216, '价格导出', 9062211, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:price:export', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062216);

-- 4. 返利管理页 + 按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062221, '返利管理', 9062210, 2, 'rebate', 'mk/rebate/index', NULL, '', 1, 0, 'C', '0', '0', 'marketing:rebate:list', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062221);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062222, '返利查询', 9062221, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:query', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062222);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062223, '返利政策新增', 9062221, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:add', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062223);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062224, '返利政策修改', 9062221, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:edit', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062224);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062225, '返利政策删除', 9062221, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:remove', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062225);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062226, '返利导出', 9062221, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:export', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062226);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062227, '返利核算', 9062221, 6, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:settle', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062227);
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062228, '返利兑现', 9062221, 7, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'marketing:rebate:redeem', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9062228);

-- 5. 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '价格类型', 'mk_price_type', '0', 'admin', NOW(), '价格政策-价格类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_price_type');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '返利考核口径', 'mk_rebate_basis', '0', 'admin', NOW(), '返利政策-考核口径'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_rebate_basis');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '返利周期类型', 'mk_rebate_period', '0', 'admin', NOW(), '返利政策-周期类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_rebate_period');
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '返利结算状态', 'mk_rebate_settle_status', '0', 'admin', NOW(), '返利结算-状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_rebate_settle_status');

-- 6. 字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '客户专价', '1', 'mk_price_type', '', 'primary', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_price_type' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '等级价', '2', 'mk_price_type', '', 'success', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_price_type' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '按金额', '1', 'mk_rebate_basis', '', 'primary', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_basis' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '按数量', '2', 'mk_rebate_basis', '', 'success', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_basis' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '月', '1', 'mk_rebate_period', '', 'primary', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_period' AND dict_value = '1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '季', '2', 'mk_rebate_period', '', 'success', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_period' AND dict_value = '2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '年', '3', 'mk_rebate_period', '', 'warning', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_period' AND dict_value = '3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待兑现', '0', 'mk_rebate_settle_status', '', 'warning', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_settle_status' AND dict_value = '0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已兑现', '1', 'mk_rebate_settle_status', '', 'success', 'N', '0', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_rebate_settle_status' AND dict_value = '1');

-- 7. 建表（标准库缺表时补齐，DDL 与 RUOYI_CFSP 完全一致）
CREATE TABLE IF NOT EXISTS mk_price_policy (
  `policy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '政策ID',
  `policy_no` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '政策编号',
  `policy_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '政策名称',
  `price_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '价格类型（1客户专价 2等级价）',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID（客户专价必填）',
  `customer_level` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户等级（等级价必填）',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `unit_price` decimal(14,4) NOT NULL COMMENT '政策单价',
  `start_date` date NOT NULL COMMENT '生效日期',
  `end_date` date NOT NULL COMMENT '失效日期',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0启用 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`policy_id`),
  UNIQUE KEY `uk_pp_no` (`policy_no`),
  KEY `idx_pp_customer` (`customer_id`),
  KEY `idx_pp_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='价格政策';

CREATE TABLE IF NOT EXISTS mk_rebate_policy (
  `policy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
  `policy_no` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '政策编号（同编号多行=阶梯组）',
  `policy_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '政策名称',
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID（空=全部客户）',
  `basis` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '考核口径（1金额 2数量）',
  `period_type` char(1) COLLATE utf8mb4_general_ci DEFAULT '1' COMMENT '周期类型（1月 2季 3年）',
  `tier_min` decimal(14,2) NOT NULL COMMENT '阶梯下限（含）',
  `tier_max` decimal(14,2) DEFAULT NULL COMMENT '阶梯上限（含，空=不封顶）',
  `rebate_rate` decimal(5,2) NOT NULL COMMENT '返利比例%',
  `start_date` date NOT NULL COMMENT '生效日期',
  `end_date` date NOT NULL COMMENT '失效日期',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0启用 1停用）',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`policy_id`),
  KEY `idx_rp_no` (`policy_no`),
  KEY `idx_rp_customer` (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='返利政策（阶梯组）';

CREATE TABLE IF NOT EXISTS mk_rebate_settle (
  `settle_id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  `settle_no` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算单号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `customer_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '客户名称',
  `period` varchar(6) COLLATE utf8mb4_general_ci NOT NULL COMMENT '核算期间yyyyMM',
  `policy_no` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '政策编号',
  `policy_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '政策名称',
  `achieve_amount` decimal(14,2) DEFAULT '0.00' COMMENT '达成额',
  `rebate_rate` decimal(5,2) DEFAULT '0.00' COMMENT '返利比例%',
  `rebate_amount` decimal(14,2) DEFAULT '0.00' COMMENT '返利金额',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0待兑现 1已兑现）',
  `redeem_type` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '兑现方式（抵货款/现金等）',
  `redeem_date` datetime DEFAULT NULL COMMENT '兑现时间',
  `redeem_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '兑现人',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`settle_id`),
  UNIQUE KEY `uk_settle_no` (`settle_no`),
  KEY `idx_rs_customer` (`customer_id`),
  KEY `idx_rs_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='返利结算';
