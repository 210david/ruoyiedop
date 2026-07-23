-- DMS和WMS编号规则初始化
-- 执行前请确保 mk_number_rule 表已存在

-- =============================================
-- 0. 增加 module 字段（按模块分离编号规则）
-- =============================================
ALTER TABLE `mk_number_rule` ADD COLUMN IF NOT EXISTS `module` VARCHAR(20) DEFAULT 'mk' COMMENT '所属模块(mk=营销,dms=设备,wms=仓储)' AFTER `rule_code`;

-- 更新已有规则的模块归属
UPDATE `mk_number_rule` SET module='mk' WHERE rule_code IN ('activity','customer','lead','opportunity','order','contract');
UPDATE `mk_number_rule` SET module='dms' WHERE rule_code LIKE 'dms_%';
UPDATE `mk_number_rule` SET module='wms' WHERE rule_code LIKE 'wms_%';

-- DMS 设备管理模块编号规则
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_equipment', '设备编号', 'SB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '设备编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_equipment');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_part', '备件编号', 'BJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_part');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_in', '备件入库单据号', 'BJIN', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件入库单据号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_in');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_out', '备件出库单据号', 'BJOUT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件出库单据号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_out');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_work_order', '工单号', 'WO', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '维修工单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_work_order');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_inspection_route', '巡检路线编号', 'RT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '巡检路线编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_inspection_route');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_inspection_task', '点检任务编号', 'INS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '点检任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_inspection_task');

-- WMS 仓库管理模块编号规则
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material', '物料编号', 'WL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '物料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_warehouse', '仓库编码', 'WH', '', '0', 3, 1, 1, 0, '', '', '0', 'admin', sysdate(), '仓库编码，不重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_warehouse');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_supplier', '供应商编码', 'GYS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '供应商编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_supplier');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_inbound', '入库单号', 'IN', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '入库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_inbound');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_outbound', '出库单号', 'OUT', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '出库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_outbound');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_move', '移库单号', 'MV', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '移库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_move');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_stocktake', '盘点单号', 'ST', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '盘点单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_stocktake');

-- =============================================
-- 在设备管理、仓储管理菜单下添加编号规则入口
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 3000, 99, 'dmsNumberRule', 'mk/numberRule/index', '', 'DmsNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '设备管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=3000);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 2000, 99, 'wmsNumberRule', 'mk/numberRule/index', '', 'WmsNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '仓储管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=2000);

-- 给admin角色分配编号规则菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id IN (3000, 2000)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- =============================================
-- 更新各模块编号规则菜单的query参数，传递module标识
-- =============================================
UPDATE sys_menu SET query='module=mk' WHERE menu_name='编号规则' AND parent_id=4000;
UPDATE sys_menu SET query='module=dms' WHERE menu_name='编号规则' AND parent_id=3000;
UPDATE sys_menu SET query='module=wms' WHERE menu_name='编号规则' AND parent_id=2000;

-- =============================================
-- 物料按类型分组编码规则（原材料/半成品/成品/辅料）
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_0', 'wms', '物料编号-原材料', 'YL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '原材料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_0');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_1', 'wms', '物料编号-半成品', 'BC', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '半成品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_1');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_2', 'wms', '物料编号-成品', 'CP', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '成品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_2');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_3', 'wms', '物料编号-辅料', 'FL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '辅料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_3');
