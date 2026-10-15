-- ============================================================
-- P2-批次六：招投标档案 + 价格政策返利 + 信用欠款 + 三单匹配 + 供应商复评
-- 环境：RUOYI_CFSP（业务表统一 utf8mb4_0900_ai_ci）
-- 编号规则：补缺 contract(HT)/pms_payment(FK) + 新增 mk_bid(TB)/mk_rebate_policy(RP)/mk_rebate_settle(FL)
-- 菜单号段：9062200-9062228（营销管理 4000 下）
-- ============================================================

-- 1. 编号规则（幂等插入）
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'mk_bid','mk','投标编号规则','TB','yyyyMMdd','1',4,1,1,0,'','','0','0','admin',NOW(),'P2批次六：招投标档案'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mk_bid');

INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'mk_price_policy','mk','价格政策编号规则','PP','yyyyMMdd','1',4,1,1,0,'','','0','0','admin',NOW(),'P2批次六：价格政策'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mk_price_policy');

INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'mk_rebate_policy','mk','返利政策编号规则','RP','yyyyMMdd','1',4,1,1,0,'','','0','0','admin',NOW(),'P2批次六：返利政策'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mk_rebate_policy');

INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'mk_rebate_settle','mk','返利结算单号规则','FL','yyyyMMdd','1',4,1,1,0,'','','0','0','admin',NOW(),'P2批次六：返利结算'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='mk_rebate_settle');

-- 补缺：合同创建使用 generateNumber("contract")，规则原本缺失会直接报错
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'contract','mk','合同编号规则','HT','yyyyMMdd','1',4,1,1,0,'','','0','0','admin',NOW(),'P2批次六补缺'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='contract');

-- 补缺：采购发票付款使用 generateNumber("pms_payment")，规则原本缺失会直接报错
INSERT INTO mk_number_rule (rule_code, module, rule_name, prefix, date_format, reset_type, seq_length, seq_start, step, current_seq, current_date_str, connector, prefix_field_enabled, status, create_by, create_time, remark)
SELECT 'pms_payment','pms','采购付款单号规则','FK','yyyyMMdd','1',4,1,1,0,'','','0','0','admin',NOW(),'P2批次六补缺'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='pms_payment');

-- 2. 客户信用字段 + 供应商复评周期（幂等）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='mk_customer' AND column_name='credit_limit');
SET @ddl = IF(@col_exists=0, 'ALTER TABLE mk_customer ADD COLUMN credit_limit decimal(14,2) DEFAULT 0.00 COMMENT ''信用额度上限（0=不启用信用控制）'', ADD COLUMN credit_days int DEFAULT 0 COMMENT ''账期天数'', ADD COLUMN credit_status char(1) DEFAULT ''0'' COMMENT ''信用状态（0正常 1冻结）''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='wms_supplier' AND column_name='reeval_cycle_months');
SET @ddl = IF(@col_exists=0, 'ALTER TABLE wms_supplier ADD COLUMN reeval_cycle_months int DEFAULT 12 COMMENT ''复评周期（月）''', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 业务表
CREATE TABLE IF NOT EXISTS mk_bid_archive (
  bid_id bigint NOT NULL AUTO_INCREMENT COMMENT '投标ID',
  bid_no varchar(32) NOT NULL COMMENT '投标编号',
  bid_name varchar(200) NOT NULL COMMENT '投标项目名称',
  tender_no varchar(64) DEFAULT NULL COMMENT '招标编号',
  tender_org varchar(200) DEFAULT NULL COMMENT '招标机构',
  customer_id bigint DEFAULT NULL COMMENT '客户ID',
  customer_name varchar(200) DEFAULT NULL COMMENT '客户名称',
  bid_date date DEFAULT NULL COMMENT '投标日期',
  open_date date DEFAULT NULL COMMENT '开标日期',
  bid_amount decimal(14,2) DEFAULT 0.00 COMMENT '投标金额',
  deposit decimal(14,2) DEFAULT 0.00 COMMENT '保证金',
  bid_status char(1) DEFAULT '0' COMMENT '状态（0编制 1已投标 2中标 3未中标 4废标）',
  win_amount decimal(14,2) DEFAULT NULL COMMENT '中标金额',
  contract_id bigint DEFAULT NULL COMMENT '关联合同ID',
  contract_no varchar(32) DEFAULT NULL COMMENT '关联合同编号',
  result_date date DEFAULT NULL COMMENT '结果日期',
  attachment varchar(500) DEFAULT NULL COMMENT '投标附件',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (bid_id),
  UNIQUE KEY uk_bid_no (bid_no),
  KEY idx_bid_customer (customer_id),
  KEY idx_bid_status (bid_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招投标档案';

CREATE TABLE IF NOT EXISTS mk_bid_item (
  item_id bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  bid_id bigint NOT NULL COMMENT '投标ID',
  line_no int DEFAULT 0 COMMENT '行号',
  material_id bigint DEFAULT NULL COMMENT '物料ID',
  material_code varchar(50) DEFAULT NULL COMMENT '物料编码',
  product_name varchar(200) NOT NULL COMMENT '品名',
  product_spec varchar(200) DEFAULT NULL COMMENT '规格',
  unit varchar(20) DEFAULT NULL COMMENT '单位',
  quantity int DEFAULT 1 COMMENT '数量',
  unit_price decimal(14,2) DEFAULT 0.00 COMMENT '单价',
  subtotal decimal(14,2) DEFAULT 0.00 COMMENT '小计',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (item_id),
  KEY idx_bid_item_bid (bid_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投标明细';

CREATE TABLE IF NOT EXISTS mk_price_policy (
  policy_id bigint NOT NULL AUTO_INCREMENT COMMENT '政策ID',
  policy_no varchar(32) NOT NULL COMMENT '政策编号',
  policy_name varchar(200) NOT NULL COMMENT '政策名称',
  price_type char(1) DEFAULT '1' COMMENT '价格类型（1客户专价 2等级价）',
  customer_id bigint DEFAULT NULL COMMENT '客户ID（客户专价必填）',
  customer_level varchar(10) DEFAULT NULL COMMENT '客户等级（等级价必填）',
  material_id bigint NOT NULL COMMENT '物料ID',
  unit_price decimal(14,4) NOT NULL COMMENT '政策单价',
  start_date date NOT NULL COMMENT '生效日期',
  end_date date NOT NULL COMMENT '失效日期',
  status char(1) DEFAULT '0' COMMENT '状态（0启用 1停用）',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (policy_id),
  UNIQUE KEY uk_pp_no (policy_no),
  KEY idx_pp_customer (customer_id),
  KEY idx_pp_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='价格政策';

CREATE TABLE IF NOT EXISTS mk_rebate_policy (
  policy_id bigint NOT NULL AUTO_INCREMENT COMMENT '阶梯ID',
  policy_no varchar(32) NOT NULL COMMENT '政策编号（同编号多行=阶梯组）',
  policy_name varchar(200) NOT NULL COMMENT '政策名称',
  customer_id bigint DEFAULT NULL COMMENT '客户ID（空=全部客户）',
  basis char(1) DEFAULT '1' COMMENT '考核口径（1金额 2数量）',
  period_type char(1) DEFAULT '1' COMMENT '周期类型（1月 2季 3年）',
  tier_min decimal(14,2) NOT NULL COMMENT '阶梯下限（含）',
  tier_max decimal(14,2) DEFAULT NULL COMMENT '阶梯上限（含，空=不封顶）',
  rebate_rate decimal(5,2) NOT NULL COMMENT '返利比例%',
  start_date date NOT NULL COMMENT '生效日期',
  end_date date NOT NULL COMMENT '失效日期',
  status char(1) DEFAULT '0' COMMENT '状态（0启用 1停用）',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (policy_id),
  KEY idx_rp_no (policy_no),
  KEY idx_rp_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利政策（阶梯组）';

CREATE TABLE IF NOT EXISTS mk_rebate_settle (
  settle_id bigint NOT NULL AUTO_INCREMENT COMMENT '结算ID',
  settle_no varchar(32) NOT NULL COMMENT '结算单号',
  customer_id bigint NOT NULL COMMENT '客户ID',
  customer_name varchar(200) DEFAULT NULL COMMENT '客户名称',
  period varchar(6) NOT NULL COMMENT '核算期间yyyyMM',
  policy_no varchar(32) NOT NULL COMMENT '政策编号',
  policy_name varchar(200) DEFAULT NULL COMMENT '政策名称',
  achieve_amount decimal(14,2) DEFAULT 0.00 COMMENT '达成额',
  rebate_rate decimal(5,2) DEFAULT 0.00 COMMENT '返利比例%',
  rebate_amount decimal(14,2) DEFAULT 0.00 COMMENT '返利金额',
  status char(1) DEFAULT '0' COMMENT '状态（0待兑现 1已兑现）',
  redeem_type varchar(50) DEFAULT NULL COMMENT '兑现方式（抵货款/现金等）',
  redeem_date datetime DEFAULT NULL COMMENT '兑现时间',
  redeem_by varchar(64) DEFAULT NULL COMMENT '兑现人',
  del_flag char(1) DEFAULT '0' COMMENT '删除标志',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (settle_id),
  UNIQUE KEY uk_settle_no (settle_no),
  KEY idx_rs_customer (customer_id),
  KEY idx_rs_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利结算';

-- 4. 字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '投标状态','mk_bid_status','0','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='mk_bid_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 1 AS dict_sort,'编制' AS dict_label,'0' AS dict_value,'mk_bid_status' AS dict_type,'' AS css_class,'info' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 2,'已投标','1','mk_bid_status','','primary','N','0','admin',NOW(),'' UNION ALL
  SELECT 3,'中标','2','mk_bid_status','','success','N','0','admin',NOW(),'' UNION ALL
  SELECT 4,'未中标','3','mk_bid_status','','warning','N','0','admin',NOW(),'' UNION ALL
  SELECT 5,'废标','4','mk_bid_status','','danger','N','0','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mk_bid_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '价格类型','mk_price_type','0','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='mk_price_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 1 AS dict_sort,'客户专价' AS dict_label,'1' AS dict_value,'mk_price_type' AS dict_type,'' AS css_class,'primary' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 2,'等级价','2','mk_price_type','','success','N','0','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mk_price_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '返利考核口径','mk_rebate_basis','0','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='mk_rebate_basis');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 1 AS dict_sort,'按金额' AS dict_label,'1' AS dict_value,'mk_rebate_basis' AS dict_type,'' AS css_class,'primary' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 2,'按数量','2','mk_rebate_basis','','success','N','0','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mk_rebate_basis');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '返利周期类型','mk_rebate_period','0','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='mk_rebate_period');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 1 AS dict_sort,'月' AS dict_label,'1' AS dict_value,'mk_rebate_period' AS dict_type,'' AS css_class,'primary' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 2,'季','2','mk_rebate_period','','success','N','0','admin',NOW(),'' UNION ALL
  SELECT 3,'年','3','mk_rebate_period','','warning','N','0','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mk_rebate_period');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '返利结算状态','mk_rebate_settle_status','0','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='mk_rebate_settle_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 1 AS dict_sort,'待兑现' AS dict_label,'0' AS dict_value,'mk_rebate_settle_status' AS dict_type,'' AS css_class,'warning' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 2,'已兑现','1','mk_rebate_settle_status','','success','N','0','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mk_rebate_settle_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '客户信用状态','mk_credit_status','0','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type='mk_credit_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 1 AS dict_sort,'正常' AS dict_label,'0' AS dict_value,'mk_credit_status' AS dict_type,'' AS css_class,'success' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 2,'冻结','1','mk_credit_status','','danger','N','0','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='mk_credit_status');

-- 5. 菜单（营销管理4000下，号段9062200-9062228）
-- 5.1 招投标档案
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062200,'招投标档案',4000,9,'bid',NULL,1,0,'M','0','0','','form','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062200);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062201,'招投标列表',9062200,1,'list','mk/bid/index',1,0,'C','0','0','marketing:bid:list','#','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062201);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 9062202 AS menu_id,'投标查询' AS menu_name,9062201 AS parent_id,1 AS order_num,'' AS path,'' AS component,1 AS is_frame,0 AS is_cache,'F' AS menu_type,'0' AS visible,'0' AS status,'marketing:bid:query' AS perms,'#' AS icon,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 9062203,'投标新增',9062201,2,'','',1,0,'F','0','0','marketing:bid:add','#','admin',NOW(),'' UNION ALL
  SELECT 9062204,'投标修改',9062201,3,'','',1,0,'F','0','0','marketing:bid:edit','#','admin',NOW(),'' UNION ALL
  SELECT 9062205,'投标删除',9062201,4,'','',1,0,'F','0','0','marketing:bid:remove','#','admin',NOW(),'' UNION ALL
  SELECT 9062206,'投标导出',9062201,5,'','',1,0,'F','0','0','marketing:bid:export','#','admin',NOW(),'' UNION ALL
  SELECT 9062207,'标记中标',9062201,6,'','',1,0,'F','0','0','marketing:bid:win','#','admin',NOW(),'' UNION ALL
  SELECT 9062208,'生成合同',9062201,7,'','',1,0,'F','0','0','marketing:bid:contract','#','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062202);

-- 5.2 价格与返利
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062210,'价格与返利',4000,10,'price',NULL,1,0,'M','0','0','','money','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062210);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062211,'价格政策',9062210,1,'pricePolicy','mk/pricePolicy/index',1,0,'C','0','0','marketing:price:list','#','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062211);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 9062212 AS menu_id,'价格查询' AS menu_name,9062211 AS parent_id,1 AS order_num,'' AS path,'' AS component,1 AS is_frame,0 AS is_cache,'F' AS menu_type,'0' AS visible,'0' AS status,'marketing:price:query' AS perms,'#' AS icon,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 9062213,'价格新增',9062211,2,'','',1,0,'F','0','0','marketing:price:add','#','admin',NOW(),'' UNION ALL
  SELECT 9062214,'价格修改',9062211,3,'','',1,0,'F','0','0','marketing:price:edit','#','admin',NOW(),'' UNION ALL
  SELECT 9062215,'价格删除',9062211,4,'','',1,0,'F','0','0','marketing:price:remove','#','admin',NOW(),'' UNION ALL
  SELECT 9062216,'价格导出',9062211,5,'','',1,0,'F','0','0','marketing:price:export','#','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062212);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9062221,'返利管理',9062210,2,'rebate','mk/rebate/index',1,0,'C','0','0','marketing:rebate:list','#','admin',NOW(),'P2批次六'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062221);

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT t.* FROM (
  SELECT 9062222 AS menu_id,'返利查询' AS menu_name,9062221 AS parent_id,1 AS order_num,'' AS path,'' AS component,1 AS is_frame,0 AS is_cache,'F' AS menu_type,'0' AS visible,'0' AS status,'marketing:rebate:query' AS perms,'#' AS icon,'admin' AS create_by,NOW() AS create_time,'' AS remark UNION ALL
  SELECT 9062223,'返利政策新增',9062221,2,'','',1,0,'F','0','0','marketing:rebate:add','#','admin',NOW(),'' UNION ALL
  SELECT 9062224,'返利政策修改',9062221,3,'','',1,0,'F','0','0','marketing:rebate:edit','#','admin',NOW(),'' UNION ALL
  SELECT 9062225,'返利政策删除',9062221,4,'','',1,0,'F','0','0','marketing:rebate:remove','#','admin',NOW(),'' UNION ALL
  SELECT 9062226,'返利导出',9062221,5,'','',1,0,'F','0','0','marketing:rebate:export','#','admin',NOW(),'' UNION ALL
  SELECT 9062227,'返利核算',9062221,6,'','',1,0,'F','0','0','marketing:rebate:settle','#','admin',NOW(),'' UNION ALL
  SELECT 9062228,'返利兑现',9062221,7,'','',1,0,'F','0','0','marketing:rebate:redeem','#','admin',NOW(),''
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id=9062222);
