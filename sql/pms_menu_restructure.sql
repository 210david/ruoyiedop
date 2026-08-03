-- =============================================
-- 采购管理菜单结构重构脚本
-- 将一级菜单改为二级目录结构
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 删除旧的菜单结构（保留按钮权限）
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu 
    WHERE parent_id = @pmsParentId 
    AND menu_type IN ('C', 'M')
);

-- 删除旧的一级菜单（保留顶级目录）
DELETE FROM sys_menu WHERE parent_id = @pmsParentId AND menu_type IN ('C', 'M');

-- =============================================
-- 创建二级目录结构
-- =============================================

-- 1. 采购业务目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购业务', @pmsParentId, 1, 'business', NULL, '', '', 1, 0, 'M', '0', '0', '', 'document', 'admin', sysdate(), '采购业务目录');
SET @pmsBusinessId = LAST_INSERT_ID();

-- 2. 仓储作业目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('仓储作业', @pmsParentId, 2, 'warehouse', NULL, '', '', 1, 0, 'M', '0', '0', '', 'box', 'admin', sysdate(), '仓储作业目录');
SET @pmsWarehouseId = LAST_INSERT_ID();

-- 3. 财务管理目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('财务管理', @pmsParentId, 3, 'finance', NULL, '', '', 1, 0, 'M', '0', '0', '', 'coin', 'admin', sysdate(), '财务管理目录');
SET @pmsFinanceId = LAST_INSERT_ID();

-- 4. 数据分析目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('数据分析', @pmsParentId, 4, 'analysis', NULL, '', '', 1, 0, 'M', '0', '0', '', 'data-line', 'admin', sysdate(), '数据分析目录');
SET @pmsAnalysisId = LAST_INSERT_ID();

-- 5. 系统配置目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('系统配置', @pmsParentId, 5, 'config', NULL, '', '', 1, 0, 'M', '0', '0', '', 'setting', 'admin', sysdate(), '系统配置目录');
SET @pmsConfigId = LAST_INSERT_ID();

-- =============================================
-- 在采购业务下创建菜单
-- =============================================

-- 采购申请
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购申请', @pmsBusinessId, 1, 'request', 'pms/request/index', '', 'PmsRequest', 1, 0, 'C', '0', '0', 'pms:request:list', 'edit-pen', 'admin', sysdate(), '');
SET @reqMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('申请查询', @reqMenuId, 1, '', 'F', '0', '0', 'pms:request:query', 'admin', sysdate()),
('申请新增', @reqMenuId, 2, '', 'F', '0', '0', 'pms:request:add', 'admin', sysdate()),
('申请修改', @reqMenuId, 3, '', 'F', '0', '0', 'pms:request:edit', 'admin', sysdate()),
('申请删除', @reqMenuId, 4, '', 'F', '0', '0', 'pms:request:remove', 'admin', sysdate()),
('申请导出', @reqMenuId, 5, '', 'F', '0', '0', 'pms:request:export', 'admin', sysdate()),
('申请审批', @reqMenuId, 6, '', 'F', '0', '0', 'pms:request:approve', 'admin', sysdate());

-- 采购订单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购订单', @pmsBusinessId, 2, 'order', 'pms/order/index', '', 'PmsOrder', 1, 0, 'C', '0', '0', 'pms:order:list', 'document-checked', 'admin', sysdate(), '');
SET @ordMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('订单查询', @ordMenuId, 1, '', 'F', '0', '0', 'pms:order:query', 'admin', sysdate()),
('订单新增', @ordMenuId, 2, '', 'F', '0', '0', 'pms:order:add', 'admin', sysdate()),
('订单修改', @ordMenuId, 3, '', 'F', '0', '0', 'pms:order:edit', 'admin', sysdate()),
('订单删除', @ordMenuId, 4, '', 'F', '0', '0', 'pms:order:remove', 'admin', sysdate()),
('订单导出', @ordMenuId, 5, '', 'F', '0', '0', 'pms:order:export', 'admin', sysdate()),
('订单审批', @ordMenuId, 6, '', 'F', '0', '0', 'pms:order:approve', 'admin', sysdate());

-- =============================================
-- 在仓储作业下创建菜单
-- =============================================

-- 收货验收
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('收货验收', @pmsWarehouseId, 1, 'receive', 'pms/receive/index', '', 'PmsReceive', 1, 0, 'C', '0', '0', 'pms:receive:list', 'circle-check', 'admin', sysdate(), '');
SET @recvMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('收货查询', @recvMenuId, 1, '', 'F', '0', '0', 'pms:receive:query', 'admin', sysdate()),
('收货新增', @recvMenuId, 2, '', 'F', '0', '0', 'pms:receive:add', 'admin', sysdate()),
('收货修改', @recvMenuId, 3, '', 'F', '0', '0', 'pms:receive:edit', 'admin', sysdate()),
('收货删除', @recvMenuId, 4, '', 'F', '0', '0', 'pms:receive:remove', 'admin', sysdate()),
('收货导出', @recvMenuId, 5, '', 'F', '0', '0', 'pms:receive:export', 'admin', sysdate());

-- 退货管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('退货管理', @pmsWarehouseId, 2, 'return', 'pms/return/index', '', 'PmsReturn', 1, 0, 'C', '0', '0', 'pms:return:list', 'refresh-left', 'admin', sysdate(), '');
SET @retMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('退货查询', @retMenuId, 1, '', 'F', '0', '0', 'pms:return:query', 'admin', sysdate()),
('退货新增', @retMenuId, 2, '', 'F', '0', '0', 'pms:return:add', 'admin', sysdate()),
('退货修改', @retMenuId, 3, '', 'F', '0', '0', 'pms:return:edit', 'admin', sysdate()),
('退货删除', @retMenuId, 4, '', 'F', '0', '0', 'pms:return:remove', 'admin', sysdate()),
('退货导出', @retMenuId, 5, '', 'F', '0', '0', 'pms:return:export', 'admin', sysdate());

-- =============================================
-- 在财务管理下创建菜单
-- =============================================

-- 发票结算
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('发票结算', @pmsFinanceId, 1, 'invoice', 'pms/invoice/index', '', 'PmsInvoice', 1, 0, 'C', '0', '0', 'pms:invoice:list', 'money', 'admin', sysdate(), '');
SET @invMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('发票查询', @invMenuId, 1, '', 'F', '0', '0', 'pms:invoice:query', 'admin', sysdate()),
('发票新增', @invMenuId, 2, '', 'F', '0', '0', 'pms:invoice:add', 'admin', sysdate()),
('发票修改', @invMenuId, 3, '', 'F', '0', '0', 'pms:invoice:edit', 'admin', sysdate()),
('发票删除', @invMenuId, 4, '', 'F', '0', '0', 'pms:invoice:remove', 'admin', sysdate()),
('发票导出', @invMenuId, 5, '', 'F', '0', '0', 'pms:invoice:export', 'admin', sysdate());

-- =============================================
-- 在数据分析下创建菜单
-- =============================================

-- 采购看板
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('采购看板', @pmsAnalysisId, 1, 'dashboard', 'pms/dashboard/index', '', 'PmsDashboard', 1, 0, 'C', '0', '0', 'pms:dashboard:view', 'trend-charts', 'admin', sysdate(), '');

-- =============================================
-- 在系统配置下创建菜单
-- =============================================

-- 编号规则
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('编号规则', @pmsConfigId, 1, 'numberRule', 'pms/numberRule/index', '', 'PmsNumberRule', 1, 0, 'C', '0', '0', 'pms:numberRule:list', 'tickets', 'admin', sysdate(), '采购管理编号规则');
SET @pmsRuleMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time) VALUES
('规则查询', @pmsRuleMenuId, 1, '', 'F', '0', '0', 'pms:numberRule:query', 'admin', sysdate()),
('规则新增', @pmsRuleMenuId, 2, '', 'F', '0', '0', 'pms:numberRule:add', 'admin', sysdate()),
('规则修改', @pmsRuleMenuId, 3, '', 'F', '0', '0', 'pms:numberRule:edit', 'admin', sysdate()),
('规则导出', @pmsRuleMenuId, 4, '', 'F', '0', '0', 'pms:numberRule:export', 'admin', sysdate());

-- =============================================
-- 给admin角色分配所有新菜单权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu 
WHERE perms LIKE 'pms:%' 
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- 验证结果
SELECT '菜单重构完成' AS result;
SELECT m1.menu_name AS '一级目录', m2.menu_name AS '二级目录', m3.menu_name AS '菜单/按钮', m3.icon, m3.perms
FROM sys_menu m1
LEFT JOIN sys_menu m2 ON m2.parent_id = m1.menu_id
LEFT JOIN sys_menu m3 ON m3.parent_id = m2.menu_id
WHERE m1.menu_name = '采购管理'
ORDER BY m1.order_num, m2.order_num, m3.order_num;
