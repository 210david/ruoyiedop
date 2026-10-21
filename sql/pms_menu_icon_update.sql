-- =============================================
-- 采购管理菜单图标优化更新脚本
-- =============================================

-- 获取采购管理目录ID
SET @pmsParentId = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=0 LIMIT 1);

-- 更新顶级目录图标
UPDATE sys_menu SET icon = 'shopping-cart' WHERE menu_name = '采购管理' AND parent_id = 0;

-- 更新各功能菜单图标
UPDATE sys_menu SET icon = 'edit-pen' WHERE menu_name = '采购申请' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'document' WHERE menu_name = '采购订单' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'box' WHERE menu_name = '收货验收' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'refresh-left' WHERE menu_name = '退货管理' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'coin' WHERE menu_name = '发票结算' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'data-line' WHERE menu_name = '采购看板' AND parent_id = @pmsParentId;
UPDATE sys_menu SET icon = 'tickets' WHERE menu_name = '编号规则' AND parent_id = @pmsParentId;

-- 验证更新结果
SELECT menu_name, icon, component 
FROM sys_menu 
WHERE parent_id = @pmsParentId OR (menu_name = '采购管理' AND parent_id = 0)
ORDER BY order_num;
