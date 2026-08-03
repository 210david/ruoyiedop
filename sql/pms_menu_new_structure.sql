-- =============================================
-- 采购管理菜单结构重构（按需求文档）
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 删除旧的采购管理子菜单（保留顶级目录）
DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = @pmsParentId);
DELETE FROM sys_menu WHERE parent_id = @pmsParentId;

-- =============================================
-- 1、供应商管理
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商管理', @pmsParentId, 1, 'supplier', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '供应商管理目录');
SET @pmsSupplierDirId = LAST_INSERT_ID();

-- 供应商信息管理（复用WMS供应商表）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商信息', @pmsSupplierDirId, 1, 'info', 'wms/supplier/index', '', 'PmsSupplierInfo', 1, 0, 'C', '0', '0', 'wms:supplier:list', 'people', 'admin', sysdate(), '供应商基本信息管理');
SET @pmsSupplierInfoId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('供应商查询', @pmsSupplierInfoId, 1, '', 'F', '0', '0', 'wms:supplier:query', 'admin', sysdate()),
('供应商新增', @pmsSupplierInfoId, 2, '', 'F', '0', '0', 'wms:supplier:add', 'admin', sysdate()),
('供应商修改', @pmsSupplierInfoId, 3, '', 'F', '0', '0', 'wms:supplier:edit', 'admin', sysdate()),
('供应商删除', @pmsSupplierInfoId, 4, '', 'F', '0', '0', 'wms:supplier:remove', 'admin', sysdate()),
('供应商导出', @pmsSupplierInfoId, 5, '', 'F', '0', '0', 'wms:supplier:export', 'admin', sysdate());

-- 供应商资质管理（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商资质', @pmsSupplierDirId, 2, 'qualification', 'pms/qualification/index', '', 'PmsQualification', 1, 0, 'C', '0', '0', 'pms:qualification:list', 'documentation', 'admin', sysdate(), '供应商资质管理');
SET @pmsQualId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('资质查询', @pmsQualId, 1, '', 'F', '0', '0', 'pms:qualification:query', 'admin', sysdate()),
('资质新增', @pmsQualId, 2, '', 'F', '0', '0', 'pms:qualification:add', 'admin', sysdate()),
('资质修改', @pmsQualId, 3, '', 'F', '0', '0', 'pms:qualification:edit', 'admin', sysdate()),
('资质删除', @pmsQualId, 4, '', 'F', '0', '0', 'pms:qualification:remove', 'admin', sysdate()),
('资质导出', @pmsQualId, 5, '', 'F', '0', '0', 'pms:qualification:export', 'admin', sysdate());

-- 供应商供货清单（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供货清单', @pmsSupplierDirId, 3, 'catalog', 'pms/catalog/index', '', 'PmsCatalog', 1, 0, 'C', '0', '0', 'pms:catalog:list', 'list', 'admin', sysdate(), '供应商供货清单管理');
SET @pmsCatalogId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('清单查询', @pmsCatalogId, 1, '', 'F', '0', '0', 'pms:catalog:query', 'admin', sysdate()),
('清单新增', @pmsCatalogId, 2, '', 'F', '0', '0', 'pms:catalog:add', 'admin', sysdate()),
('清单修改', @pmsCatalogId, 3, '', 'F', '0', '0', 'pms:catalog:edit', 'admin', sysdate()),
('清单删除', @pmsCatalogId, 4, '', 'F', '0', '0', 'pms:catalog:remove', 'admin', sysdate()),
('清单导出', @pmsCatalogId, 5, '', 'F', '0', '0', 'pms:catalog:export', 'admin', sysdate()),
('清单导入', @pmsCatalogId, 6, '', 'F', '0', '0', 'pms:catalog:import', 'admin', sysdate());

-- 供应商评价（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('供应商评价', @pmsSupplierDirId, 4, 'eval', 'pms/eval/index', '', 'PmsEval', 1, 0, 'C', '0', '0', 'pms:eval:list', 'rate', 'admin', sysdate(), '供应商评价管理');
SET @pmsEvalId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('评价查询', @pmsEvalId, 1, '', 'F', '0', '0', 'pms:eval:query', 'admin', sysdate()),
('评价新增', @pmsEvalId, 2, '', 'F', '0', '0', 'pms:eval:add', 'admin', sysdate()),
('评价修改', @pmsEvalId, 3, '', 'F', '0', '0', 'pms:eval:edit', 'admin', sysdate()),
('评价删除', @pmsEvalId, 4, '', 'F', '0', '0', 'pms:eval:remove', 'admin', sysdate()),
('评价导出', @pmsEvalId, 5, '', 'F', '0', '0', 'pms:eval:export', 'admin', sysdate());

-- =============================================
-- 2、采购需求管理
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购需求管理', @pmsParentId, 2, 'demand', NULL, '', '', 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '采购需求管理目录');
SET @pmsDemandDirId = LAST_INSERT_ID();

-- 采购计划管理（待开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购计划', @pmsDemandDirId, 1, 'plan', 'pms/plan/index', '', 'PmsPlan', 1, 0, 'C', '1', '0', 'pms:plan:list', 'plan', 'admin', sysdate(), '采购计划管理（开发中）');

-- 采购申请管理（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购申请', @pmsDemandDirId, 2, 'request', 'pms/request/index', '', 'PmsRequest', 1, 0, 'C', '0', '0', 'pms:request:list', 'documentation', 'admin', sysdate(), '采购申请管理');
SET @pmsRequestId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('申请查询', @pmsRequestId, 1, '', 'F', '0', '0', 'pms:request:query', 'admin', sysdate()),
('申请新增', @pmsRequestId, 2, '', 'F', '0', '0', 'pms:request:add', 'admin', sysdate()),
('申请修改', @pmsRequestId, 3, '', 'F', '0', '0', 'pms:request:edit', 'admin', sysdate()),
('申请删除', @pmsRequestId, 4, '', 'F', '0', '0', 'pms:request:remove', 'admin', sysdate()),
('申请导出', @pmsRequestId, 5, '', 'F', '0', '0', 'pms:request:export', 'admin', sysdate()),
('申请审批', @pmsRequestId, 6, '', 'F', '0', '0', 'pms:request:approve', 'admin', sysdate());

-- =============================================
-- 3、询比价管理（待开发）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('询比价管理', @pmsParentId, 3, 'inquiry', 'pms/inquiry/index', '', 'PmsInquiry', 1, 0, 'C', '1', '0', 'pms:inquiry:list', 'search', 'admin', sysdate(), '询比价管理（开发中）');

-- =============================================
-- 4、合同和订单管理
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('合同和订单管理', @pmsParentId, 4, 'contract', NULL, '', '', 1, 0, 'M', '0', '0', '', 'list', 'admin', sysdate(), '合同和订单管理目录');
SET @pmsContractDirId = LAST_INSERT_ID();

-- 采购合同管理（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购合同', @pmsContractDirId, 1, 'contract', 'pms/contract/index', '', 'PmsContract', 1, 0, 'C', '0', '0', 'pms:contract:list', 'clipboard', 'admin', sysdate(), '采购合同管理');
SET @pmsContractId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('合同查询', @pmsContractId, 1, '', 'F', '0', '0', 'pms:contract:query', 'admin', sysdate()),
('合同新增', @pmsContractId, 2, '', 'F', '0', '0', 'pms:contract:add', 'admin', sysdate()),
('合同修改', @pmsContractId, 3, '', 'F', '0', '0', 'pms:contract:edit', 'admin', sysdate()),
('合同删除', @pmsContractId, 4, '', 'F', '0', '0', 'pms:contract:remove', 'admin', sysdate()),
('合同导出', @pmsContractId, 5, '', 'F', '0', '0', 'pms:contract:export', 'admin', sysdate());

-- 采购订单管理（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购订单', @pmsContractDirId, 2, 'order', 'pms/order/index', '', 'PmsOrder', 1, 0, 'C', '0', '0', 'pms:order:list', 'shopping', 'admin', sysdate(), '采购订单管理');
SET @pmsOrderId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('订单查询', @pmsOrderId, 1, '', 'F', '0', '0', 'pms:order:query', 'admin', sysdate()),
('订单新增', @pmsOrderId, 2, '', 'F', '0', '0', 'pms:order:add', 'admin', sysdate()),
('订单修改', @pmsOrderId, 3, '', 'F', '0', '0', 'pms:order:edit', 'admin', sysdate()),
('订单删除', @pmsOrderId, 4, '', 'F', '0', '0', 'pms:order:remove', 'admin', sysdate()),
('订单导出', @pmsOrderId, 5, '', 'F', '0', '0', 'pms:order:export', 'admin', sysdate()),
('订单审批', @pmsOrderId, 6, '', 'F', '0', '0', 'pms:order:approve', 'admin', sysdate());

-- =============================================
-- 5、收货和发票管理
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('收货和发票管理', @pmsParentId, 5, 'receive', NULL, '', '', 1, 0, 'M', '0', '0', '', 'checkbox', 'admin', sysdate(), '收货和发票管理目录');
SET @pmsReceiveDirId = LAST_INSERT_ID();

-- 收货验收（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('收货验收', @pmsReceiveDirId, 1, 'receive', 'pms/receive/index', '', 'PmsReceive', 1, 0, 'C', '0', '0', 'pms:receive:list', 'checkbox', 'admin', sysdate(), '收货验收管理');
SET @pmsReceiveId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('收货查询', @pmsReceiveId, 1, '', 'F', '0', '0', 'pms:receive:query', 'admin', sysdate()),
('收货新增', @pmsReceiveId, 2, '', 'F', '0', '0', 'pms:receive:add', 'admin', sysdate()),
('收货修改', @pmsReceiveId, 3, '', 'F', '0', '0', 'pms:receive:edit', 'admin', sysdate()),
('收货删除', @pmsReceiveId, 4, '', 'F', '0', '0', 'pms:receive:remove', 'admin', sysdate()),
('收货导出', @pmsReceiveId, 5, '', 'F', '0', '0', 'pms:receive:export', 'admin', sysdate());

-- 退货管理（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('退货管理', @pmsReceiveDirId, 2, 'return', 'pms/return/index', '', 'PmsReturn', 1, 0, 'C', '0', '0', 'pms:return:list', 'swap', 'admin', sysdate(), '退货管理');
SET @pmsReturnId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('退货查询', @pmsReturnId, 1, '', 'F', '0', '0', 'pms:return:query', 'admin', sysdate()),
('退货新增', @pmsReturnId, 2, '', 'F', '0', '0', 'pms:return:add', 'admin', sysdate()),
('退货修改', @pmsReturnId, 3, '', 'F', '0', '0', 'pms:return:edit', 'admin', sysdate()),
('退货删除', @pmsReturnId, 4, '', 'F', '0', '0', 'pms:return:remove', 'admin', sysdate()),
('退货导出', @pmsReturnId, 5, '', 'F', '0', '0', 'pms:return:export', 'admin', sysdate());

-- 发票结算（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('发票结算', @pmsReceiveDirId, 3, 'invoice', 'pms/invoice/index', '', 'PmsInvoice', 1, 0, 'C', '0', '0', 'pms:invoice:list', 'money', 'admin', sysdate(), '发票结算管理');
SET @pmsInvoiceId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('发票查询', @pmsInvoiceId, 1, '', 'F', '0', '0', 'pms:invoice:query', 'admin', sysdate()),
('发票新增', @pmsInvoiceId, 2, '', 'F', '0', '0', 'pms:invoice:add', 'admin', sysdate()),
('发票修改', @pmsInvoiceId, 3, '', 'F', '0', '0', 'pms:invoice:edit', 'admin', sysdate()),
('发票删除', @pmsInvoiceId, 4, '', 'F', '0', '0', 'pms:invoice:remove', 'admin', sysdate()),
('发票导出', @pmsInvoiceId, 5, '', 'F', '0', '0', 'pms:invoice:export', 'admin', sysdate());

-- =============================================
-- 6、统计分析
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('统计分析', @pmsParentId, 6, 'analysis', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '统计分析目录');
SET @pmsAnalysisDirId = LAST_INSERT_ID();

-- 采购看板（已开发）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购看板', @pmsAnalysisDirId, 1, 'dashboard', 'pms/dashboard/index', '', 'PmsDashboard', 1, 0, 'C', '0', '0', 'pms:dashboard:view', 'chart', 'admin', sysdate(), '采购数据看板');

-- =============================================
-- 7、编号规则
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('编号规则', @pmsParentId, 7, 'numberRule', 'pms/numberRule/index', '', 'PmsNumberRule', 1, 0, 'C', '0', '0', 'pms:numberRule:list', 'number', 'admin', sysdate(), '采购编号规则');
SET @pmsNumberRuleId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('规则查询', @pmsNumberRuleId, 1, '', 'F', '0', '0', 'pms:numberRule:query', 'admin', sysdate()),
('规则新增', @pmsNumberRuleId, 2, '', 'F', '0', '0', 'pms:numberRule:add', 'admin', sysdate()),
('规则修改', @pmsNumberRuleId, 3, '', 'F', '0', '0', 'pms:numberRule:edit', 'admin', sysdate()),
('规则导出', @pmsNumberRuleId, 4, '', 'F', '0', '0', 'pms:numberRule:export', 'admin', sysdate());

-- 给admin角色分配所有新菜单权限（跳过已存在的）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu 
WHERE perms LIKE 'pms:%' OR perms LIKE 'wms:supplier:%';

-- 验证结果
SELECT '菜单重构完成' AS result;
SELECT m1.menu_name AS '一级目录', m2.menu_name AS '二级目录', m3.menu_name AS '三级菜单', m3.icon, m3.perms, m3.visible
FROM sys_menu m1
LEFT JOIN sys_menu m2 ON m2.parent_id = m1.menu_id AND m2.menu_type = 'M'
LEFT JOIN sys_menu m3 ON m3.parent_id = m2.menu_id AND m3.menu_type = 'C'
WHERE m1.menu_name = '采购管理'
ORDER BY m1.order_num, m2.order_num, m3.order_num;
