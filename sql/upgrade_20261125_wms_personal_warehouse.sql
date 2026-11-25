-- ============================================================
-- 2026-11-25 仓库管理：个人物品仓库支持
-- 0) 仓库管理中增加个人物品仓库，仓区为个人物品仓区01，库位为个人物品仓位01
-- 1) 入库类型增加「个人物品入库」（字典 wms_inbound_type，值 4）
-- 2) 出库类型增加「个人物品出库」（字典 wms_outbound_type，值 4）
-- ============================================================

-- ---------- 1. 入库类型字典：个人物品入库 ----------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '个人物品入库', '4', 'wms_inbound_type', '', 'danger', 'N', '0', 'admin', NOW(), '个人物品入库'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_inbound_type' AND dict_value = '4');

-- ---------- 2. 出库类型字典：个人物品出库 ----------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '个人物品出库', '4', 'wms_outbound_type', '', 'danger', 'N', '0', 'admin', NOW(), '个人物品出库'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_outbound_type' AND dict_value = '4');

-- ---------- 3. 个人物品仓库（node_type=1） ----------
INSERT INTO wms_warehouse (parent_id, ancestors, warehouse_code, warehouse_name, node_type, status, del_flag, create_by, create_time, remark)
SELECT 0, '0', 'WH06', '个人物品仓库', '1', '0', '0', 'admin', NOW(), '个人物品专用仓库'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM wms_warehouse WHERE warehouse_code = 'WH06' AND del_flag = '0');

-- ---------- 4. 个人物品仓区01（node_type=2，挂 WH06 下） ----------
INSERT INTO wms_warehouse (parent_id, ancestors, warehouse_code, warehouse_name, node_type, status, del_flag, create_by, create_time, remark)
SELECT w.warehouse_id, CONCAT('0,', w.warehouse_id), 'WH06A01', '个人物品仓区01', '2', '0', '0', 'admin', NOW(), '个人物品仓区'
FROM wms_warehouse w
WHERE w.warehouse_code = 'WH06' AND w.node_type = '1' AND w.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM wms_warehouse a WHERE a.warehouse_code = 'WH06A01' AND a.del_flag = '0');

-- ---------- 5. 个人物品仓位01（node_type=3，挂仓区下，库位状态默认空闲） ----------
INSERT INTO wms_warehouse (parent_id, ancestors, warehouse_code, warehouse_name, node_type, area_type, location_type, capacity, location_status, status, del_flag, create_by, create_time, remark)
SELECT a.warehouse_id, CONCAT(a.ancestors, ',', a.warehouse_id), 'WH06A01L01', '个人物品仓位01', '3', NULL, '2', 0, '0', '0', '0', 'admin', NOW(), '个人物品仓位'
FROM wms_warehouse a
WHERE a.warehouse_code = 'WH06A01' AND a.node_type = '2' AND a.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM wms_warehouse l WHERE l.warehouse_code = 'WH06A01L01' AND l.del_flag = '0');
