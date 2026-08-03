-- ============================================================
-- 采购管理模块 - 菜单按钮权限补丁脚本
-- 补充所有缺失的F型按钮权限（共20个）
-- 
-- 涉及模块：
--   1. 采购计划        - 缺失6个（query/add/edit/remove/export/audit）
--   2. 询比价管理      - 缺失6个（query/add/edit/remove/export/audit）
--   3. 采购合同        - 缺失2个（audit/change）
--   4. 收货验收        - 缺失1个（audit）
--   5. 退货管理        - 缺失1个（audit）
--   6. 发票结算        - 缺失2个（audit/pay）
--   7. 供应商资质      - 缺失1个（audit）
--   8. 供应商评价      - 缺失1个（audit）
-- ============================================================

-- =============================================
-- 1. 采购计划按钮权限（6个）
--    后端Controller: PmsPurchasePlanController
--    前端页面: pms/plan/index.vue
-- =============================================
SET @planMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:plan:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计划查询', @planMenuId, 1, '', 'F', '0', '0', 'pms:plan:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:plan:query' AND parent_id=@planMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计划新增', @planMenuId, 2, '', 'F', '0', '0', 'pms:plan:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:plan:add' AND parent_id=@planMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计划修改', @planMenuId, 3, '', 'F', '0', '0', 'pms:plan:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:plan:edit' AND parent_id=@planMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计划删除', @planMenuId, 4, '', 'F', '0', '0', 'pms:plan:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:plan:remove' AND parent_id=@planMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计划导出', @planMenuId, 5, '', 'F', '0', '0', 'pms:plan:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:plan:export' AND parent_id=@planMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '计划审批', @planMenuId, 6, '', 'F', '0', '0', 'pms:plan:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:plan:audit' AND parent_id=@planMenuId);


-- =============================================
-- 2. 询比价管理按钮权限（6个）
--    后端Controller: PmsInquiryController
--    前端页面: pms/inquiry/index.vue
-- =============================================
SET @inquiryMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:inquiry:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '询价查询', @inquiryMenuId, 1, '', 'F', '0', '0', 'pms:inquiry:query', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:inquiry:query' AND parent_id=@inquiryMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '询价新增', @inquiryMenuId, 2, '', 'F', '0', '0', 'pms:inquiry:add', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:inquiry:add' AND parent_id=@inquiryMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '询价修改', @inquiryMenuId, 3, '', 'F', '0', '0', 'pms:inquiry:edit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:inquiry:edit' AND parent_id=@inquiryMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '询价删除', @inquiryMenuId, 4, '', 'F', '0', '0', 'pms:inquiry:remove', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:inquiry:remove' AND parent_id=@inquiryMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '询价导出', @inquiryMenuId, 5, '', 'F', '0', '0', 'pms:inquiry:export', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:inquiry:export' AND parent_id=@inquiryMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '询价审核', @inquiryMenuId, 6, '', 'F', '0', '0', 'pms:inquiry:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:inquiry:audit' AND parent_id=@inquiryMenuId);


-- =============================================
-- 3. 采购合同按钮权限（2个）
--    后端Controller: PmsContractController
--    前端页面: pms/contract/index.vue
-- =============================================
SET @contractMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:contract:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同审核', @contractMenuId, 6, '', 'F', '0', '0', 'pms:contract:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:audit' AND parent_id=@contractMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '合同变更', @contractMenuId, 7, '', 'F', '0', '0', 'pms:contract:change', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:contract:change' AND parent_id=@contractMenuId);


-- =============================================
-- 4. 收货验收按钮权限（1个）
--    后端Controller: PmsReceiveController
--    前端页面: pms/receive/index.vue
-- =============================================
SET @receiveMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:receive:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '收货审核', @receiveMenuId, 6, '', 'F', '0', '0', 'pms:receive:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:receive:audit' AND parent_id=@receiveMenuId);


-- =============================================
-- 5. 退货管理按钮权限（1个）
--    后端Controller: PmsReturnController
--    前端页面: pms/return/index.vue
-- =============================================
SET @returnMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:return:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '退货审批', @returnMenuId, 6, '', 'F', '0', '0', 'pms:return:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:return:audit' AND parent_id=@returnMenuId);


-- =============================================
-- 6. 发票结算按钮权限（2个）
--    后端Controller: PmsInvoiceController
--    前端页面: pms/invoice/index.vue
-- =============================================
SET @invoiceMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:invoice:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票审核', @invoiceMenuId, 6, '', 'F', '0', '0', 'pms:invoice:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:audit' AND parent_id=@invoiceMenuId);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '发票付款', @invoiceMenuId, 7, '', 'F', '0', '0', 'pms:invoice:pay', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:invoice:pay' AND parent_id=@invoiceMenuId);


-- =============================================
-- 7. 供应商资质按钮权限（1个）
--    后端Controller: PmsSupplierQualificationController
--    前端页面: pms/qualification/index.vue
-- =============================================
SET @qualMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:qualification:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '资质审核', @qualMenuId, 6, '', 'F', '0', '0', 'pms:qualification:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:qualification:audit' AND parent_id=@qualMenuId);


-- =============================================
-- 8. 供应商评价按钮权限（1个）
--    后端Controller: PmsSupplierEvalController
--    前端页面: pms/eval/index.vue
-- =============================================
SET @evalMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:eval:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估审批', @evalMenuId, 6, '', 'F', '0', '0', 'pms:eval:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:audit' AND parent_id=@evalMenuId);


-- =============================================
-- 9. 给admin角色(role_id=1)分配所有新增按钮权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN (
    'pms:plan:query', 'pms:plan:add', 'pms:plan:edit', 'pms:plan:remove', 'pms:plan:export', 'pms:plan:audit',
    'pms:inquiry:query', 'pms:inquiry:add', 'pms:inquiry:edit', 'pms:inquiry:remove', 'pms:inquiry:export', 'pms:inquiry:audit',
    'pms:contract:audit', 'pms:contract:change',
    'pms:receive:audit',
    'pms:return:audit',
    'pms:invoice:audit', 'pms:invoice:pay',
    'pms:qualification:audit',
    'pms:eval:audit'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- 同时给超管角色(role_id=2)分配（如果存在）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE perms IN (
    'pms:plan:query', 'pms:plan:add', 'pms:plan:edit', 'pms:plan:remove', 'pms:plan:export', 'pms:plan:audit',
    'pms:inquiry:query', 'pms:inquiry:add', 'pms:inquiry:edit', 'pms:inquiry:remove', 'pms:inquiry:export', 'pms:inquiry:audit',
    'pms:contract:audit', 'pms:contract:change',
    'pms:receive:audit',
    'pms:return:audit',
    'pms:invoice:audit', 'pms:invoice:pay',
    'pms:qualification:audit',
    'pms:eval:audit'
)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=2);


-- =============================================
-- 9.5 清理孤立的F型按钮权限
--    pms_menu_new_structure.sql 重构时只删除了C/M型菜单，
--    旧F型按钮的parent_id指向已删除的菜单，成为孤立记录
-- =============================================
-- 查找孤立按钮（可选：先执行SELECT查看再决定是否DELETE）
-- SELECT b.menu_id, b.menu_name, b.perms, b.parent_id 
-- FROM sys_menu b 
-- LEFT JOIN sys_menu p ON p.menu_id = b.parent_id 
-- WHERE b.menu_type = 'F' AND b.perms LIKE 'pms:%' AND p.menu_id IS NULL;

-- 删除孤立的PMS按钮权限记录
DELETE b FROM sys_menu b 
LEFT JOIN sys_menu p ON p.menu_id = b.parent_id 
WHERE b.menu_type = 'F' AND b.perms LIKE 'pms:%' AND p.menu_id IS NULL;

-- 清理已删除菜单的角色关联
DELETE rm FROM sys_role_menu rm
LEFT JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.menu_id IS NULL;


-- =============================================
-- 10. 验证结果
-- =============================================
SELECT '=== PMS菜单按钮权限补丁执行完成 ===' AS result;

-- 验证：列出所有新增的按钮权限
SELECT 
    p.menu_name AS '父菜单',
    b.menu_name AS '按钮名称',
    b.perms    AS '权限标识',
    b.order_num AS '排序'
FROM sys_menu p
JOIN sys_menu b ON b.parent_id = p.menu_id AND b.menu_type = 'F'
WHERE b.perms IN (
    'pms:plan:query', 'pms:plan:add', 'pms:plan:edit', 'pms:plan:remove', 'pms:plan:export', 'pms:plan:audit',
    'pms:inquiry:query', 'pms:inquiry:add', 'pms:inquiry:edit', 'pms:inquiry:remove', 'pms:inquiry:export', 'pms:inquiry:audit',
    'pms:contract:audit', 'pms:contract:change',
    'pms:receive:audit',
    'pms:return:audit',
    'pms:invoice:audit', 'pms:invoice:pay',
    'pms:qualification:audit',
    'pms:eval:audit'
)
ORDER BY p.menu_name, b.order_num;
