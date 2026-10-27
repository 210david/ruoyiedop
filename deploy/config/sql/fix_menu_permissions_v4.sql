-- =====================================================
-- 权限审计修复脚本 v4
-- 全面修复：缺失菜单权限、乱码菜单名、组件路径错误、死权限
-- =====================================================

-- =============================================
-- 1. 修复乱码菜单名称（营销模块 4148-4154）
-- =============================================
UPDATE `sys_menu` SET `menu_name` = '活动状态变更' WHERE `menu_id` = 4148 AND `perms` = 'marketing:activity:status';
UPDATE `sys_menu` SET `menu_name` = '活动复盘' WHERE `menu_id` = 4149 AND `perms` = 'marketing:activity:review';
UPDATE `sys_menu` SET `menu_name` = '活动复制' WHERE `menu_id` = 4150 AND `perms` = 'marketing:activity:copy';
UPDATE `sys_menu` SET `menu_name` = '联系人合并' WHERE `menu_id` = 4151 AND `perms` = 'marketing:contact:merge';
UPDATE `sys_menu` SET `menu_name` = '联系人分配' WHERE `menu_id` = 4152 AND `perms` = 'marketing:contact:assign';
UPDATE `sys_menu` SET `menu_name` = '联系人导入' WHERE `menu_id` = 4153 AND `perms` = 'marketing:contact:import';
UPDATE `sys_menu` SET `menu_name` = '参与人转线索' WHERE `menu_id` = 4154 AND `perms` = 'marketing:participant:convert';

-- =============================================
-- 2. 修复乱码菜单名称（WMS模块 2123-2125）
-- =============================================
UPDATE `sys_menu` SET `menu_name` = '库存删除' WHERE `menu_id` = 2123 AND `perms` = 'wms:inventory:remove';
UPDATE `sys_menu` SET `menu_name` = '流水删除' WHERE `menu_id` = 2124 AND `perms` = 'wms:log:remove';
UPDATE `sys_menu` SET `menu_name` = '预警删除' WHERE `menu_id` = 2125 AND `perms` = 'wms:stockAlert:remove';

-- =============================================
-- 3. 新增缺失的营销模块按钮权限
--    后端Controller已使用但sys_menu中无对应记录
-- =============================================

-- 3.1 客户管理缺失权限（导入、分配、领取）
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '客户导入', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:customer:import', '#', 'admin', NOW(), '客户批量导入'
FROM sys_menu WHERE perms = 'marketing:customer:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:customer:import');

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '客户分配', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:customer:assign', '#', 'admin', NOW(), '客户转移分配'
FROM sys_menu WHERE perms = 'marketing:customer:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:customer:assign');

INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '客户领取', menu_id, 8, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:customer:claim', '#', 'admin', NOW(), '从公海池领取客户'
FROM sys_menu WHERE perms = 'marketing:customer:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:customer:claim');

-- 3.2 互动记录缺失导出权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '互动导出', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:interaction:export', '#', 'admin', NOW(), '互动记录导出'
FROM sys_menu WHERE perms = 'marketing:interaction:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:interaction:export');

-- 3.3 发票管理菜单及按钮（如果upgrade脚本未执行）
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '发票管理', m.menu_id, 13, 'invoice', 'mk/invoice/index', NULL, 1, 0, 'C', '0', '0', 'marketing:invoice:list', 'edit', 'admin', NOW(), ''
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '合同订单' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:invoice:list' AND menu_type = 'C');

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

-- 3.4 订单退货菜单及按钮（修正组件路径 mk/return/index → mk/returnOrder/index）
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单退货', m.menu_id, 14, 'return', 'mk/returnOrder/index', NULL, 1, 0, 'C', '0', '0', 'marketing:return:list', 'error', 'admin', NOW(), ''
FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '合同订单' AND menu_type = 'M' LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:return:list' AND menu_type = 'C');

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

-- 3.5 订单管理新增按钮权限（确认、签收、取消）
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单确认', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:confirm', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:order:confirm');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单签收', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:receive', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:order:receive');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '订单取消', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:order:cancel', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:order:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:order:cancel');

-- 3.6 回款管理新增确认权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '回款确认', menu_id, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:payment:confirm', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:payment:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:payment:confirm');

-- 3.7 合同管理新增变更、续签权限
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '合同变更', menu_id, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:contract:change', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:contract:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:contract:change');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '合同续签', menu_id, 7, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:contract:renew', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:contract:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:contract:renew');

-- 3.8 商机阶段配置新增细粒度权限（原来所有操作共用 stage:list，无法单独控制增删改）
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '阶段查询', menu_id, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:stage:query', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:stage:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:stage:query');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '阶段新增', menu_id, 2, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:stage:add', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:stage:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:stage:add');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '阶段修改', menu_id, 3, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:stage:edit', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:stage:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:stage:edit');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '阶段删除', menu_id, 4, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:stage:remove', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:stage:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:stage:remove');
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '阶段导出', menu_id, 5, '', NULL, NULL, 1, 0, 'F', '0', '0', 'marketing:stage:export', '#', 'admin', NOW(), '' FROM sys_menu WHERE perms = 'marketing:stage:list' AND menu_type = 'C' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'marketing:stage:export');

-- =============================================
-- 4. 修复退货菜单组件路径（mk/return/index → mk/returnOrder/index）
-- =============================================
UPDATE `sys_menu` SET `component` = 'mk/returnOrder/index' WHERE `perms` = 'marketing:return:list' AND `menu_type` = 'C' AND `component` = 'mk/return/index';

-- =============================================
-- 5. 修复客户公海池菜单权限（独立权限，不再与客户列表共用）
--    原来公海池使用 marketing:customer:list，导致无法单独控制
--    改为 marketing:customer:pool
--    注意：公海池菜单名可能是"公海池"或"客户公海"，用component匹配
-- =============================================
UPDATE `sys_menu` SET `perms` = 'marketing:customer:pool' WHERE `component` = 'mk/customer/pool' AND `menu_type` = 'C';

-- 5.1 清理因 marketing:customer:list 有两条C菜单导致的重复按钮权限
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (
    SELECT menu_id FROM (
        SELECT m1.menu_id FROM sys_menu m1
        INNER JOIN sys_menu m2 ON m1.perms = m2.perms AND m1.menu_id > m2.menu_id
        WHERE m1.perms IN ('marketing:customer:import','marketing:customer:assign','marketing:customer:claim')
    ) dup
);
DELETE FROM `sys_menu` WHERE `menu_id` IN (
    SELECT menu_id FROM (
        SELECT m1.menu_id FROM sys_menu m1
        INNER JOIN sys_menu m2 ON m1.perms = m2.perms AND m1.menu_id > m2.menu_id
        WHERE m1.perms IN ('marketing:customer:import','marketing:customer:assign','marketing:customer:claim')
    ) dup
);

-- =============================================
-- 6. 将所有新增菜单授权给超级管理员角色（role_id=1）
-- =============================================
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` 
WHERE `perms` IN (
    'marketing:customer:import',
    'marketing:customer:assign',
    'marketing:customer:claim',
    'marketing:customer:pool',
    'marketing:interaction:export',
    'marketing:invoice:list',
    'marketing:invoice:query',
    'marketing:invoice:add',
    'marketing:invoice:edit',
    'marketing:invoice:remove',
    'marketing:invoice:export',
    'marketing:invoice:issue',
    'marketing:invoice:void',
    'marketing:return:list',
    'marketing:return:query',
    'marketing:return:add',
    'marketing:return:remove',
    'marketing:return:export',
    'marketing:return:approve',
    'marketing:return:refund',
    'marketing:order:confirm',
    'marketing:order:receive',
    'marketing:order:cancel',
    'marketing:payment:confirm',
    'marketing:contract:change',
    'marketing:contract:renew',
    'marketing:stage:query',
    'marketing:stage:add',
    'marketing:stage:edit',
    'marketing:stage:remove',
    'marketing:stage:export'
) AND `menu_id` NOT IN (SELECT `menu_id` FROM `sys_role_menu` WHERE `role_id` = 1);

-- =============================================
-- 7. 验证：检查是否仍有后端使用但SQL未定义的权限
-- =============================================
-- 以下权限在后端Controller中使用但在SQL中无对应sys_menu记录（执行后应为空）：
-- SELECT perms FROM sys_menu WHERE perms IN (
--     'marketing:customer:import',
--     'marketing:customer:assign',
--     'marketing:customer:claim',
--     'marketing:interaction:export'
-- );
-- 如果查询结果不为空，说明权限已成功添加。

-- =============================================
-- 8. 第二轮深度审计修复
-- 8.1 新增缺失的 DMS 备件查询权限
--     后端 DmsSparePartController 使用 @PreAuthorize("hasAuthority('dms:sparepart:query')")
--     但 sys_menu 中从未定义此按钮权限，导致非超管角色无法查询备件。
-- =============================================
INSERT IGNORE INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '备件查询', menu_id, 1, '', NULL, NULL, 1, 0, 'F', '0', '0', 'dms:sparepart:query', '#', 'admin', NOW(), '备件主数据查询'
FROM sys_menu WHERE perms = 'dms:sparepart:list' AND menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'dms:sparepart:query');

-- 8.2 补全超管对 DMS 备件子菜单的授权（3105-3116, 3121-3123）
--     这些菜单存在于 sys_menu 但未在 sys_role_menu 中授权给超管
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.menu_id FROM `sys_menu` m
WHERE m.menu_id IN (3105,3106,3107,3108,3109,3110,3111,3112,3113,3114,3115,3116,3121,3122,3123)
AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 8.3 补全超管对新增 dms:sparepart:query 菜单的授权
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE perms = 'dms:sparepart:query'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 8.4 补全超管对 marketing:order:deliver（4106）的授权
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE perms = 'marketing:order:deliver'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- =============================================
-- 9. 修复菜单排序冲突（order_num）
-- 9.1 系统管理目录下：接口文档(118)与通知公告(107)的 order_num 均为8，开放接口文档(120)与开放接口(119)均为9
--     调整为：通知公告=8, 接口文档=13, 开放接口=9, 开放接口文档=14
-- =============================================
UPDATE `sys_menu` SET `order_num` = 13 WHERE `menu_id` = 118 AND `order_num` = 8;
UPDATE `sys_menu` SET `order_num` = 14 WHERE `menu_id` = 120 AND `order_num` = 9;

-- =============================================
-- 10. 最终验证查询（执行后应全部为空结果）
-- =============================================
-- 10.1 超管未授权菜单（排除系统原生未授权项）
-- SELECT m.menu_id, m.menu_name, m.perms FROM sys_menu m
-- LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id AND rm.role_id = 1
-- WHERE rm.menu_id IS NULL AND m.status = '0'
-- AND m.menu_id NOT IN (1,2,3,4,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,
--   500,501,1000-1060系列);
--
-- 10.2 order_num冲突
-- SELECT parent_id, order_num, COUNT(*) as cnt FROM sys_menu
-- WHERE menu_type IN ('C','F') AND status = '0'
-- GROUP BY parent_id, order_num HAVING cnt > 1;
--
-- 10.3 乱码检查
-- SELECT COUNT(*) FROM sys_menu WHERE menu_name LIKE '%?%';

-- =============================================
-- 11. 修复说明（完整版）：
-- 11.1 乱码菜单名：原SQL中菜单ID 4148-4154和2123-2125的menu_name字段因编码问题显示为乱码，
--      已修复为正确的中文名称。
-- 11.2 缺失权限：customer:import/assign/claim、interaction:export 在后端Controller和前端
--      v-hasPermi中使用，但从未在sys_menu中定义，导致非超级管理员角色无法使用这些功能。
-- 11.3 组件路径：退货菜单的component路径 'mk/return/index' 有误，实际文件在
--      'mk/returnOrder/index.vue'，已修正。
-- 11.4 公海池权限：公海池菜单原来使用 marketing:customer:list（与企业客户共用），
--      导致无法单独授权。已改为独立的 marketing:customer:pool 权限。
-- 11.5 升级脚本补全：invoice/return/order:confirm等权限仅在mk_upgrade_v2脚本中定义，
--      未包含在EDOP_ALL.sql中。本脚本以幂等方式确保所有权限都已创建。
-- 11.6 DMS备件查询权限：dms:sparepart:query 在后端Controller中使用但sys_menu未定义，已补充。
-- 11.7 超管授权补全：DMS备件子菜单(3105-3123)、订单发货(4106)等未授权给超管，已补充。
-- 11.8 排序冲突修复：接口文档(118)与通知公告(107)order_num均为8，开放接口文档(120)与开放接口(119)
--      均为9，已调整为118=13、120=14。
-- =============================================
