-- BOM明细测试数据
USE `ry-vue`;
SET NAMES utf8mb4;

-- BOM-001 标准BOM-产品A (bom_id=2) 产品A 由 ABS塑料、不锈钢板、PCBA主板、锂电池 组成
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark)
VALUES
(2, 10, 1, 'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', 'kg', 0.500000, 2.00, '0', '1', '0', '0', 'admin', NOW(), '外壳原料'),
(2, 20, 2, 'MAT20250002', '304不锈钢板', '1.2mm*1220mm*2440mm', 'kg', 1.200000, 1.00, '1', '1', '0', '0', 'admin', NOW(), '结构件材料'),
(2, 30, 3, 'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '核心电路板'),
(2, 40, 4, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '动力电源'),
(2, 50, 7, 'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '包装用');

-- BOM-002 标准BOM-智能手表V2 (bom_id=3) 由 PCBA、锂电池、铝合金型材、导热硅脂 组成
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark)
VALUES
(3, 10, 3, 'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '主板'),
(3, 20, 4, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '电池'),
(3, 30, 10, 'MAT20250010', '铝合金型材', '6063-T5 20mm*20mm 6米/根', '根', 0.500000, 1.00, '0', '1', '0', '0', 'admin', NOW(), '表壳材料'),
(3, 40, 9, 'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '支', 2.000000, 0.00, '0', '1', '0', '0', 'admin', NOW(), '散热用'),
(3, 50, 8, 'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '包装用');

-- BOM-003 替代BOM-智能手表V2（替代料）(bom_id=4) 替代料版本
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark)
VALUES
(4, 10, 3, 'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '主板-替代版'),
(4, 20, 4, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '电池-替代版'),
(4, 30, 1, 'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', 'kg', 0.300000, 2.00, '0', '1', '0', '0', 'admin', NOW(), '替代不锈钢板的塑料件'),
(4, 40, 7, 'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '包装用');

-- BOM-004 配方-无线耳机Pro (bom_id=5) 已发布状态
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark)
VALUES
(5, 10, 3, 'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '蓝牙主板'),
(5, 20, 4, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '耳机电池'),
(5, 30, 9, 'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '支', 1.000000, 0.00, '0', '1', '0', '0', 'admin', NOW(), '散热用'),
(5, 40, 8, 'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '防静电包装'),
(5, 50, 7, 'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '外包装箱');

-- BOM-005 临时BOM-蓝牙音箱Mini (bom_id=6)
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark)
VALUES
(6, 10, 3, 'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '音响主板'),
(6, 20, 4, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '电源'),
(6, 30, 1, 'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', 'kg', 0.800000, 2.00, '0', '1', '0', '0', 'admin', NOW(), '外壳材料'),
(6, 40, 7, 'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '包装');

-- BOM-006 标准BOM-智能手环S3 (bom_id=7)
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, del_flag, create_by, create_time, remark)
VALUES
(7, 10, 3, 'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '手环主板'),
(7, 20, 4, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '个', 1.000000, 0.00, '1', '1', '0', '0', 'admin', NOW(), '手环电池'),
(7, 30, 10, 'MAT20250010', '铝合金型材', '6063-T5 20mm*20mm 6米/根', '根', 0.200000, 1.00, '0', '1', '0', '0', 'admin', NOW(), '表壳材料'),
(7, 40, 9, 'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '支', 1.000000, 0.00, '0', '1', '0', '0', 'admin', NOW(), '散热'),
(7, 50, 8, 'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '包装'),
(7, 60, 7, 'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '个', 1.000000, 0.00, '0', '2', '0', '0', 'admin', NOW(), '外包装');

-- 验证
SELECT bom_id, COUNT(1) as detail_cnt FROM mms_bom_detail WHERE del_flag='0' GROUP BY bom_id ORDER BY bom_id;
