-- ============================================
-- 营销管理 多类型客户体系升级脚本（客户类型 / 经销商 / 个人客户）
-- 方案：传丰食品ERP营销管理-多类型客户体系开发方案 V2.1
-- 包含：字典扩展 + DDL（mk_customer/mk_order/mk_rebate_settle 加列 + 4张新表）+ 菜单按钮权限
-- 说明：菜单ID占用 4200~4259（执行前已核实标准库空闲）；可重复执行（字典/菜单带防重，DDL 首次执行）
-- 回滚：DROP TABLE mk_dealer_profile, mk_dealer_sell_flow, mk_rebate_offset, mk_customer_change_log;
--       ALTER TABLE mk_customer DROP INDEX uk_phone, DROP INDEX idx_customer_type, DROP INDEX idx_parent,
--         DROP COLUMN customer_type, DROP COLUMN parent_customer_id, DROP COLUMN contact_phone;
--       ALTER TABLE mk_order DROP COLUMN settle_type;
--       ALTER TABLE mk_rebate_settle DROP COLUMN offset_status, DROP COLUMN offset_amount;
-- ============================================

-- =============================================
-- 一、DDL
-- =============================================

-- 1. 客户主表扩展：客户类型 + 上级经销商 + 个人客户手机号
ALTER TABLE mk_customer
  ADD COLUMN customer_type      char(1)     DEFAULT '1' COMMENT '客户类型（1企业客户 2经销商 3个人客户）',
  ADD COLUMN parent_customer_id bigint      DEFAULT NULL COMMENT '上级经销商ID（仅经销商使用）',
  ADD COLUMN contact_phone      varchar(20) DEFAULT NULL COMMENT '个人客户手机号（本期明文存储，接口层统一脱敏）',
  ADD UNIQUE KEY uk_phone (contact_phone),
  ADD KEY idx_customer_type (customer_type),
  ADD KEY idx_parent (parent_customer_id);

-- 存量客户迁移：全部刷为企业客户
UPDATE mk_customer SET customer_type = '1' WHERE customer_type IS NULL;

-- 2. 订单结算方式（个人客户现结拦截落点：下单+审核双节点强制）
ALTER TABLE mk_order
  ADD COLUMN settle_type char(1) DEFAULT '0' COMMENT '结算方式（0现结 1账期）——个人客户强制0';

-- 3. 返利结算单：冲抵状态扩展
ALTER TABLE mk_rebate_settle
  ADD COLUMN offset_status char(1)       DEFAULT '0' COMMENT '冲抵状态（0未冲抵 1部分冲抵 2全部冲抵）',
  ADD COLUMN offset_amount decimal(14,2) DEFAULT 0   COMMENT '已冲抵累计金额';

-- 4. 经销商扩展档案
CREATE TABLE IF NOT EXISTS mk_dealer_profile (
  profile_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  customer_id     BIGINT        NOT NULL                COMMENT '客户ID',
  dealer_level    CHAR(1)                               COMMENT '经销层级（1一级 2二级）',
  auth_regions    VARCHAR(500)                          COMMENT '授权区域（省市区JSON）',
  auth_categories VARCHAR(500)                          COMMENT '授权品类（物料分类ID串）',
  deposit_amount  DECIMAL(14,2) DEFAULT 0               COMMENT '保证金（仅档案金额，收退台账独立立项）',
  coop_start_date DATE                                  COMMENT '合作开始日期',
  coop_end_date   DATE                                  COMMENT '合作结束日期',
  status          CHAR(1)       DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  create_by       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  remark          VARCHAR(500)                          COMMENT '备注',
  PRIMARY KEY (profile_id),
  UNIQUE KEY uk_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经销商扩展档案';

-- 5. 经销商分销流向登记（追溯链关键表）
CREATE TABLE IF NOT EXISTS mk_dealer_sell_flow (
  flow_id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '流向ID',
  dealer_id       BIGINT        NOT NULL                COMMENT '经销商客户ID',
  flow_date       DATE          NOT NULL                COMMENT '流向日期',
  material_id     BIGINT        NOT NULL                COMMENT '物料ID',
  batch_no        VARCHAR(64)   DEFAULT NULL            COMMENT '批次号（启用批次管理的行业/物料必填，其他可空）',
  quantity        DECIMAL(18,4) NOT NULL                COMMENT '数量',
  downstream_type CHAR(1)                               COMMENT '下游客户类型（1企业 2经销商 3个人 4终端不详）',
  downstream_name VARCHAR(200)                          COMMENT '下游客户名称',
  shipment_id     BIGINT                                COMMENT '来源发货单ID',
  create_by       VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME                              COMMENT '创建时间',
  update_by       VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME                              COMMENT '更新时间',
  remark          VARCHAR(500)                          COMMENT '备注',
  PRIMARY KEY (flow_id),
  KEY idx_flow_batch (batch_no),
  KEY idx_dealer_date (dealer_id, flow_date),
  UNIQUE KEY uk_shipment_material (shipment_id, material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经销商分销流向登记';

-- 6. 返利冲抵台账（财务性质按销售折扣处理，红票由财务据待办开具，ERP 仅记台账）
CREATE TABLE IF NOT EXISTS mk_rebate_offset (
  offset_id     BIGINT        NOT NULL AUTO_INCREMENT COMMENT '冲抵ID',
  settle_id     BIGINT        NOT NULL                COMMENT '返利结算单ID',
  offset_amount DECIMAL(14,2) NOT NULL                COMMENT '本次冲抵金额',
  offset_date   DATE          NOT NULL                COMMENT '冲抵日期',
  audit_status  CHAR(1)       DEFAULT '0'             COMMENT '审批状态（0待财务审核 1审核通过 2驳回）——一级审批，财务审核通过即生效',
  audit_by      VARCHAR(64)                           COMMENT '审核人',
  audit_time    DATETIME                              COMMENT '审核时间',
  audit_opinion VARCHAR(500)                          COMMENT '审核意见',
  msg_id        BIGINT                                COMMENT '财务待办消息ID（消息中心推送，红票开票依据）',
  create_by     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
  create_time   DATETIME                              COMMENT '创建时间',
  update_by     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
  update_time   DATETIME                              COMMENT '更新时间',
  remark        VARCHAR(500)                          COMMENT '备注',
  PRIMARY KEY (offset_id),
  KEY idx_offset_settle (settle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返利冲抵台账';

-- 7. 客户变更日志（上级经销商变更 / 客户类型变更）
CREATE TABLE IF NOT EXISTS mk_customer_change_log (
  log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  customer_id   BIGINT       NOT NULL                COMMENT '客户ID',
  change_type   VARCHAR(20)  NOT NULL                COMMENT '变更类型（customer_type客户类型 parent上级经销商）',
  old_value     VARCHAR(200)                         COMMENT '变更前值',
  new_value     VARCHAR(200)                         COMMENT '变更后值',
  change_reason VARCHAR(500)                         COMMENT '变更原因',
  create_by     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time   DATETIME                             COMMENT '创建时间',
  update_by     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time   DATETIME                             COMMENT '更新时间',
  remark        VARCHAR(500)                         COMMENT '备注',
  PRIMARY KEY (log_id),
  KEY idx_change_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户变更日志';

-- =============================================
-- 二、字典数据（防重插入）
-- =============================================

-- 1. 客户类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '客户类型', 'mk_customer_type', '0', 'admin', sysdate(), '企业客户/经销商/个人客户'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_customer_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 1 AS dict_sort, '企业客户' AS dict_label, '1' AS dict_value, 'mk_customer_type' AS dict_type, '' AS css_class, 'primary' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 2, '经销商',   '2', 'mk_customer_type', '', 'warning', 'N', '0', 'admin', sysdate() UNION ALL
  SELECT 3, '个人客户', '3', 'mk_customer_type', '', 'info',    'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_customer_type' AND dict_value = t.dict_value);

-- 2. 经销层级
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '经销层级', 'mk_dealer_level', '0', 'admin', sysdate(), '经销商层级字典'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_dealer_level');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 1 AS dict_sort, '一级经销商' AS dict_label, '1' AS dict_value, 'mk_dealer_level' AS dict_type, '' AS css_class, 'primary' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 2, '二级经销商', '2', 'mk_dealer_level', '', 'success', 'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_dealer_level' AND dict_value = t.dict_value);

-- 3. 订单结算方式
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '结算方式', 'mk_settle_type', '0', 'admin', sysdate(), '订单结算方式（现结/账期）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_settle_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 1 AS dict_sort, '现结' AS dict_label, '0' AS dict_value, 'mk_settle_type' AS dict_type, '' AS css_class, 'primary' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 2, '账期', '1', 'mk_settle_type', '', 'warning', 'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_settle_type' AND dict_value = t.dict_value);

-- 4. 返利冲抵状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '冲抵状态', 'mk_offset_status', '0', 'admin', sysdate(), '返利结算单冲抵状态'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_offset_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 1 AS dict_sort, '未冲抵' AS dict_label, '0' AS dict_value, 'mk_offset_status' AS dict_type, '' AS css_class, 'info' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 2, '部分冲抵', '1', 'mk_offset_status', '', 'warning', 'N', '0', 'admin', sysdate() UNION ALL
  SELECT 3, '全部冲抵', '2', 'mk_offset_status', '', 'success', 'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_offset_status' AND dict_value = t.dict_value);

-- 5. 冲抵审批状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '冲抵审批状态', 'mk_offset_audit_status', '0', 'admin', sysdate(), '返利冲抵审批状态（一级审批，财务审核通过即生效）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_offset_audit_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 1 AS dict_sort, '待财务审核' AS dict_label, '0' AS dict_value, 'mk_offset_audit_status' AS dict_type, '' AS css_class, 'warning' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 2, '审核通过', '1', 'mk_offset_audit_status', '', 'success', 'N', '0', 'admin', sysdate() UNION ALL
  SELECT 3, '驳回', '2', 'mk_offset_audit_status', '', 'danger', 'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_offset_audit_status' AND dict_value = t.dict_value);

-- 6. 流向下游客户类型（复用客户类型 + 终端不详）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '下游客户类型', 'mk_downstream_type', '0', 'admin', sysdate(), '经销商流向下游客户类型'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'mk_downstream_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 1 AS dict_sort, '企业客户' AS dict_label, '1' AS dict_value, 'mk_downstream_type' AS dict_type, '' AS css_class, 'primary' AS list_class, 'N' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 2, '经销商', '2', 'mk_downstream_type', '', 'warning', 'N', '0', 'admin', sysdate() UNION ALL
  SELECT 3, '个人客户', '3', 'mk_downstream_type', '', 'info', 'N', '0', 'admin', sysdate() UNION ALL
  SELECT 4, '终端不详', '4', 'mk_downstream_type', '', 'info', 'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_downstream_type' AND dict_value = t.dict_value);

-- 7. 客户等级字典扩展：一级经销商/二级经销商档位（P1-03，供等级价取价兜底）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT t.* FROM (
  SELECT 5 AS dict_sort, '一级经销商' AS dict_label, '5' AS dict_value, 'marketing_customer_level' AS dict_type, '' AS css_class, 'warning' AS list_class, 'N' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time UNION ALL
  SELECT 6, '二级经销商', '6', 'marketing_customer_level', '', 'warning', 'N', '0', 'admin', sysdate()
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'marketing_customer_level' AND dict_value = t.dict_value);

-- 8. 价格类型扩展：3=经销商供货价（P1-04）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
SELECT 3, '经销商供货价', '3', 'mk_price_type', '', 'warning', 'N', '0', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'mk_price_type' AND dict_value = '3');

-- =============================================
-- 三、菜单与按钮权限（ID 4200~4259 已核实空闲）
-- =============================================

-- 防重：先清理旧的同权限菜单
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE perms IN (
      'marketing:dealer:list','marketing:dealer:query','marketing:dealer:add','marketing:dealer:edit','marketing:dealer:remove',
      'marketing:flow:list','marketing:flow:query','marketing:flow:add','marketing:flow:edit','marketing:flow:remove',
      'marketing:customer:exportPersonal','marketing:customer:changeType',
      'marketing:rebate:offset:add','marketing:rebate:offset:audit','marketing:rebate:offset:list'
    ) AND menu_id BETWEEN 4200 AND 4259
  ) t
);
DELETE FROM sys_menu WHERE menu_id BETWEEN 4200 AND 4259;

-- 经销商管理（客户管理目录下，与企业客户同级）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4200, '经销商管理', 4001, 6, 'dealer', 'mk/dealer/index', '', 'MkDealer', 1, 0, 'C', '0', '0', 'marketing:dealer:list', 'shopping', 'admin', sysdate(), '经销商档案/层级/授权管理'),
(4201, '经销商查询', 4200, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:dealer:query', '#', 'admin', sysdate(), ''),
(4202, '经销商新增', 4200, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:dealer:add', '#', 'admin', sysdate(), ''),
(4203, '经销商修改', 4200, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:dealer:edit', '#', 'admin', sysdate(), ''),
(4204, '经销商删除', 4200, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:dealer:remove', '#', 'admin', sysdate(), '');

-- 流向登记（销售订单执行目录下，与发货管理同级；4487=销售订单执行）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4205, '流向登记', 4487, 6, 'dealerFlow', 'mk/dealerFlow/index', '', 'MkDealerFlow', 1, 0, 'C', '0', '0', 'marketing:flow:list', 'log', 'admin', sysdate(), '经销商分销流向登记'),
(4206, '流向查询', 4205, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:flow:query', '#', 'admin', sysdate(), ''),
(4207, '流向新增', 4205, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:flow:add', '#', 'admin', sysdate(), ''),
(4208, '流向修改', 4205, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:flow:edit', '#', 'admin', sysdate(), ''),
(4209, '流向删除', 4205, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:flow:remove', '#', 'admin', sysdate(), '');

-- 客户管理按钮扩展
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4210, '个人客户导出', 4010, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:exportPersonal', '#', 'admin', sysdate(), '个人客户手机号明文导出（留痕审计）'),
(4211, '客户类型变更', 4010, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:customer:changeType', '#', 'admin', sysdate(), '企业客户/经销商互转');

-- 返利冲抵按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(4212, '冲抵登记', 9062221, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:rebate:offset:add', '#', 'admin', sysdate(), '返利冲抵台账登记（支持拆分）'),
(4213, '冲抵审核', 9062221, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:rebate:offset:audit', '#', 'admin', sysdate(), '财务一级审批'),
(4214, '冲抵明细', 9062221, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'marketing:rebate:offset:list', '#', 'admin', sysdate(), '查看冲抵台账明细');

-- 授权给超级管理员角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 4200 AND 4259;

-- 授权给营销相关业务角色（营销总监/销售经理/销售代表/财务专员）与财务人员
-- 敏感权限单独控制：4210 个人客户导出、4211 客户类型变更 → 仅营销总监；4213 冲抵审核 → 仅财务
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r CROSS JOIN sys_menu m
WHERE m.menu_id BETWEEN 4200 AND 4259 AND m.menu_id NOT IN (4210, 4211, 4213)
  AND r.role_key IN ('mk_director','mk_sales_manager','mk_sales','mk_finance','finance');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r CROSS JOIN sys_menu m
WHERE m.menu_id IN (4210, 4211) AND r.role_key = 'mk_director';

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r CROSS JOIN sys_menu m
WHERE m.menu_id = 4213 AND r.role_key IN ('mk_finance','finance');

-- 付款方式字典（个人客户现结合同专用：一次性）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('付款方式', 'marketing_payment_method', '0', 'admin', sysdate(), '合同付款方式（含个人客户现结专用：一次性）');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time) VALUES
(1, '银行转账', '0', 'marketing_payment_method', 'primary', 'Y', '0', 'admin', sysdate()),
(2, '承兑汇票', '1', 'marketing_payment_method', 'info', 'N', '0', 'admin', sysdate()),
(3, '现金', '2', 'marketing_payment_method', 'success', 'N', '0', 'admin', sysdate()),
(4, '其他', '3', 'marketing_payment_method', '', 'N', '0', 'admin', sysdate()),
(5, '一次性', '4', 'marketing_payment_method', 'warning', 'N', '0', 'admin', sysdate());
