-- =============================================
-- 修复采购管理菜单图标 - 使用正确的SVG图标名称
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 更新顶级目录图标
UPDATE sys_menu SET icon = 'shopping' WHERE menu_name = '采购管理' AND parent_id = 0;

-- 更新二级目录图标
UPDATE sys_menu SET icon = 'documentation' WHERE menu_name = '采购业务' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'storage' WHERE menu_name = '仓储作业' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'money' WHERE menu_name = '财务管理' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'chart' WHERE menu_name = '数据分析' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'setting' WHERE menu_name = '系统配置' AND parent_id = @pmsParentId;

-- 获取二级目录ID
SET @pmsBusinessId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购业务' AND parent_id=@pmsParentId);
SET @pmsWarehouseId = (SELECT menu_id FROM sys_menu WHERE menu_name='仓储作业' AND parent_id=@pmsParentId);
SET @pmsFinanceId = (SELECT menu_id FROM sys_menu WHERE menu_name='财务管理' AND parent_id=@pmsParentId);
SET @pmsAnalysisId = (SELECT menu_id FROM sys_menu WHERE menu_name='数据分析' AND parent_id=@pmsParentId);
SET @pmsConfigId = (SELECT menu_id FROM sys_menu WHERE menu_name='系统配置' AND parent_id=@pmsParentId);

-- 更新三级菜单图标
UPDATE sys_menu SET icon = 'edit' WHERE menu_name = '采购申请' AND parent_id = @pmsBusinessId;
UPDATE sys_menu SET icon = 'list' WHERE menu_name = '采购订单' AND parent_id = @pmsBusinessId;
UPDATE sys_menu SET icon = 'peoples' WHERE menu_name = '供应商管理' AND parent_id = @pmsBusinessId;

UPDATE sys_menu SET icon = 'checkbox' WHERE menu_name = '收货验收' AND parent_id = @pmsWarehouseId;
UPDATE sys_menu SET icon = 'swap' WHERE menu_name = '退货管理' AND parent_id = @pmsWarehouseId;

UPDATE sys_menu SET icon = 'money' WHERE menu_name = '发票结算' AND parent_id = @pmsFinanceId;

UPDATE sys_menu SET icon = 'chart' WHERE menu_name = '采购看板' AND parent_id = @pmsAnalysisId;

UPDATE sys_menu SET icon = 'number' WHERE menu_name = '编号规则' AND parent_id = @pmsConfigId;

-- 验证更新结果
SELECT '图标修复完成' AS result;
SELECT menu_name, icon, menu_type 
FROM sys_menu 
WHERE menu_name IN ('采购管理', '采购业务', '仓储作业', '财务管理', '数据分析', '系统配置', '采购申请', '采购订单', '供应商管理', '收货验收', '退货管理', '发票结算', '采购看板', '编号规则') 
ORDER BY menu_type, menu_name;
