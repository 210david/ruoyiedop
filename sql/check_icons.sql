SELECT menu_name, icon, menu_type 
FROM sys_menu 
WHERE menu_name IN ('采购管理', '采购业务', '仓储作业', '财务管理', '数据分析', '系统配置', '采购申请', '采购订单', '供应商管理', '收货验收', '退货管理', '发票结算', '采购看板', '编号规则') 
ORDER BY menu_type, menu_name;
