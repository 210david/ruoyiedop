-- =====================================================
-- 销售人员分析报表 - 菜单添加脚本
-- 在"营销管理 > 数据分析"下新增"销售人员分析"菜单
-- =====================================================

-- 新增销售人员分析菜单（在"数据分析"目录下）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) 
SELECT '销售人员分析', menu_id, 2, 'salesperson', 'mk/salespersonReport/index', NULL, 1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', NOW(), '销售人员多维度分析统计报表'
FROM sys_menu WHERE menu_name = '数据分析' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name = '营销管理') t) LIMIT 1;

-- 检查菜单是否已存在（防止重复执行报错）
-- 如果已存在则忽略
