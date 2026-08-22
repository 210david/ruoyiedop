-- =====================================================
-- MMS 测试数据完善脚本
-- 确保5个半成品、5个成品，对应都有BOM、工艺路线和工序
-- =====================================================

-- 先清理旧的测试数据（只清本次脚本生成的数据，保留原有非测试数据）
-- 清理BOM明细中的测试数据
DELETE FROM mms_bom_detail WHERE bom_id IN (SELECT bom_id FROM mms_bom WHERE bom_no IN ('BOM-SC-001','BOM-SC-002','BOM-SC-003','BOM-SC-004','BOM-SC-005','BOM-FP-001','BOM-FP-002','BOM-FP-003','BOM-FP-004','BOM-FP-005'));
-- 清理测试BOM
DELETE FROM mms_bom WHERE bom_no IN ('BOM-SC-001','BOM-SC-002','BOM-SC-003','BOM-SC-004','BOM-SC-005','BOM-FP-001','BOM-FP-002','BOM-FP-003','BOM-FP-004','BOM-FP-005');
-- 清理测试工艺路线工序
DELETE FROM mms_route_process WHERE route_id IN (SELECT route_id FROM mms_route WHERE route_no IN ('RT-SC-001','RT-SC-002','RT-SC-003','RT-SC-004','RT-SC-005','RT-FP-001','RT-FP-002','RT-FP-003','RT-FP-004','RT-FP-005'));
-- 清理测试工艺路线
DELETE FROM mms_route WHERE route_no IN ('RT-SC-001','RT-SC-002','RT-SC-003','RT-SC-004','RT-SC-005','RT-FP-001','RT-FP-002','RT-FP-003','RT-FP-004','RT-FP-005');
-- 清理测试物料（半成品+成品新增的）
DELETE FROM wms_material WHERE material_code IN ('MAT-SC-001','MAT-SC-002','MAT-SC-003','MAT-SC-004','MAT-SC-005','MAT-FP-003','MAT-FP-004','MAT-FP-005');
-- 清理测试工序定义
DELETE FROM mms_process WHERE process_code IN ('PRC-SC-SMT','PRC-SC-INJ','PRC-SC-WLD','PRC-SC-GLU','PRC-SC-TST','PRC-FP-ASM','PRC-FP-FNC','PRC-FP-AGE','PRC-FP-PKG');
-- 清理测试产能单元
DELETE FROM mms_resource WHERE resource_code IN ('RES-SC-SMT','RES-SC-INJ','RES-SC-WLD','RES-SC-GLU','RES-SC-TST','RES-FP-ASM','RES-FP-FNC','RES-FP-AGE','RES-FP-PKG');

-- =====================================================
-- 1. 新增工序定义（补充现有工序不够用的部分）
-- =====================================================
-- process_type: 0=加工,1=装配,2=检验,3=包装
INSERT INTO mms_process (process_code, process_name, process_type, std_time, prep_time, is_key_process, is_outsource, workshop_name, line_name, status, remark, del_flag, create_by, create_time)
VALUES
-- 半成品工序
('PRC-SC-SMT', 'SMT贴片焊接', '0', 0.80, 0.50, '1', '0', '电子车间', 'SMT产线A', '0', 'PCB贴片回流焊，关键工序需AOI检测', '0', 'admin', NOW()),
('PRC-SC-INJ', '注塑成型', '0', 1.20, 0.75, '1', '0', '注塑车间', '注塑线A', '0', 'ABS/PC料注塑成型，需控制成型温度与保压时间', '0', 'admin', NOW()),
('PRC-SC-WLD', '电池焊接组装', '0', 1.50, 0.50, '1', '0', '电子车间', '电池组装线', '0', '锂电池组焊接与BMS安装，关键安全工序', '0', 'admin', NOW()),
('PRC-SC-GLU', '点胶封胶', '0', 0.60, 0.30, '0', '0', '电子车间', '点胶线A', '0', '电子元件点胶固定与防水密封处理', '0', 'admin', NOW()),
('PRC-SC-TST', '半成品功能测试', '2', 0.40, 0.10, '1', '0', '质检部', '测试线A', '0', '半成品电路与功能初测，不合格返修', '0', 'admin', NOW()),
-- 成品工序
('PRC-FP-ASM', '成品组装', '1', 1.80, 0.50, '1', '0', '装配车间', '总装线A', '0', '半成品+原材料组装为成品，关键工序需首件确认', '0', 'admin', NOW()),
('PRC-FP-FNC', '功能与性能测试', '2', 1.00, 0.20, '1', '0', '质检部', '测试线B', '0', '成品全功能测试+蓝牙配对+防水测试', '0', 'admin', NOW()),
('PRC-FP-AGE', '老化测试', '0', 4.00, 0.30, '1', '0', '老化车间', '老化房A', '0', '高温老化4小时，验证产品可靠性', '0', 'admin', NOW()),
('PRC-FP-PKG', '包装与贴标', '3', 0.50, 0.20, '0', '0', '包装车间', '包装线A', '0', '成品装入包装盒，贴标签与序列号', '0', 'admin', NOW());

-- =====================================================
-- 2. 新增产能单元（补充不够用的部分）
-- =====================================================
INSERT INTO mms_resource (resource_code, resource_name, resource_type, rate, rate_unit, batch_min, batch_max, efficiency, workshop_name, line_name, status, remark, del_flag, create_by, create_time)
VALUES
('RES-SC-SMT', 'SMT贴片产线', '1', 0.000, 'pcs/h', 0.000, 0.000, 95.00, '电子车间', 'SMT产线A', '0', '全自动SMT贴片线，含锡膏印刷+贴片+回流焊+AOI', '0', 'admin', NOW()),
('RES-SC-INJ', '注塑机C', '0', 0.000, 'pcs/h', 0.000, 0.000, 90.00, '注塑车间', '注塑线A', '0', '200吨注塑机，适用于外壳件注塑成型', '0', 'admin', NOW()),
('RES-SC-WLD', '电池焊接工作站', '0', 0.000, 'pcs/h', 0.000, 0.000, 85.00, '电子车间', '电池组装线', '0', '激光点焊机+BMS安装工位', '0', 'admin', NOW()),
('RES-SC-GLU', '点胶工作站', '0', 0.000, 'pcs/h', 0.000, 0.000, 92.00, '电子车间', '点胶线A', '0', '自动点胶机，支持AB双组份胶水', '0', 'admin', NOW()),
('RES-SC-TST', '半成品测试台A', '0', 0.000, 'pcs/h', 0.000, 0.000, 88.00, '质检部', '测试线A', '0', '半成品电路通断与功能初测工位', '0', 'admin', NOW()),
('RES-FP-ASM', '成品总装线B', '1', 0.000, 'pcs/h', 0.000, 0.000, 90.00, '装配车间', '总装线A', '0', '成品组装线，含8个工位，节拍60s/件', '0', 'admin', NOW()),
('RES-FP-FNC', '成品测试台B', '0', 0.000, 'pcs/h', 0.000, 0.000, 95.00, '质检部', '测试线B', '0', '成品全功能测试台，含蓝牙测试仪与防水测试仪', '0', 'admin', NOW()),
('RES-FP-AGE', '老化房B', '0', 0.000, 'pcs/h', 0.000, 0.000, 100.00, '老化车间', '老化房A', '0', '高温老化房，40℃恒温循环测试', '0', 'admin', NOW()),
('RES-FP-PKG', '包装线B', '1', 0.000, 'pcs/h', 0.000, 0.000, 95.00, '包装车间', '包装线A', '0', '成品包装产线，含自动贴标机', '0', 'admin', NOW());

-- =====================================================
-- 3. 新增半成品物料（3个新的，已有2个: PCBA主板组件=3, 锂电池组=4）
-- =====================================================
INSERT INTO wms_material (material_code, material_name, material_type, spec_model, unit, status, del_flag, create_by, create_time, remark)
VALUES
('MAT-SC-001', '手环外壳组件', '1', 'TPU+ABS 黑色 42mm表盘', '0', '0', '0', 'admin', NOW(), '智能手环外壳半成品，含表壳+底壳+按键'),
('MAT-SC-002', '耳机外壳组件', '1', 'PC+ABS 白色 入耳式', '0', '0', '0', 'admin', NOW(), '蓝牙耳机外壳半成品，含左/右腔体+耳塞套'),
('MAT-SC-003', '耳机充电盒组件', '1', 'ABS 白色 Type-C充电口', '0', '0', '0', 'admin', NOW(), '蓝牙耳机充电盒半成品，含盒体+电池+PCBA');

-- 半成品列表（5个）:
-- ID=3:  PCBA主板组件 (MAT20250003) - 半成品
-- ID=4:  锂电池组 48V20Ah (MAT20250004) - 半成品
-- ID=新:  手环外壳组件 (MAT-SC-001) - 半成品
-- ID=新:  耳机外壳组件 (MAT-SC-002) - 半成品
-- ID=新:  耳机充电盒组件 (MAT-SC-003) - 半成品

-- =====================================================
-- 4. 新增成品物料（3个新的，已有2个: 智能手环Pro=5, 无线蓝牙耳机=6）
-- =====================================================
INSERT INTO wms_material (material_code, material_name, material_type, spec_model, unit, status, del_flag, create_by, create_time, remark)
VALUES
('MAT-FP-003', '手环表带替换装', '2', '硅胶 多色 S/M/L码', '1', '0', '0', 'admin', NOW(), '手环表带替换装成品，含3种尺寸表带+工具'),
('MAT-FP-004', '蓝牙耳机套装', '2', '白色 含充电盒+耳塞 降噪版', '1', '0', '0', 'admin', NOW(), '蓝牙耳机完整套装产品'),
('MAT-FP-005', '智能手环 Lite版', '2', '蓝色 硅胶表带 基础版', '0', '0', '0', 'admin', NOW(), '智能手环精简版，无心率/NFC，主打性价比');

-- 成品列表（5个）:
-- ID=5:  智能手环 Pro (MAT20250005) - 成品
-- ID=6:  无线蓝牙耳机 (MAT20250006) - 成品
-- ID=新:  手环表带替换装 (MAT-FP-003) - 成品
-- ID=新:  蓝牙耳机套装 (MAT-FP-004) - 成品
-- ID=新:  智能手环 Lite版 (MAT-FP-005) - 成品

-- =====================================================
-- 5. 插入BOM（10个: 5半成品 + 5成品）
-- =====================================================
-- 获取物料ID变量
SET @sc_pcba_id    = (SELECT material_id FROM wms_material WHERE material_code='MAT20250003'); -- PCBA主板组件
SET @sc_battery_id = (SELECT material_id FROM wms_material WHERE material_code='MAT20250004'); -- 锂电池组
SET @sc_case_wrist = (SELECT material_id FROM wms_material WHERE material_code='MAT-SC-001');   -- 手环外壳组件
SET @sc_case_ear   = (SELECT material_id FROM wms_material WHERE material_code='MAT-SC-002');   -- 耳机外壳组件
SET @sc_box_ear    = (SELECT material_id FROM wms_material WHERE material_code='MAT-SC-003');   -- 耳机充电盒组件

SET @fp_wrist_pro  = (SELECT material_id FROM wms_material WHERE material_code='MAT20250005'); -- 智能手环 Pro
SET @fp_earphone   = (SELECT material_id FROM wms_material WHERE material_code='MAT20250006'); -- 无线蓝牙耳机
SET @fp_strap      = (SELECT material_id FROM wms_material WHERE material_code='MAT-FP-003');  -- 手环表带替换装
SET @fp_ear_set    = (SELECT material_id FROM wms_material WHERE material_code='MAT-FP-004');  -- 蓝牙耳机套装
SET @fp_wrist_lite = (SELECT material_id FROM wms_material WHERE material_code='MAT-FP-005');  -- 智能手环 Lite版

-- 原材料ID
SET @rm_abs_id     = (SELECT material_id FROM wms_material WHERE material_code='MAT20250001'); -- ABS塑料颗粒
SET @rm_steel_id   = (SELECT material_id FROM wms_material WHERE material_code='MAT20250002'); -- 304不锈钢板
SET @rm_alu_id     = (SELECT material_id FROM wms_material WHERE material_code='MAT20250010'); -- 铝合金型材
SET @rm_grease_id  = (SELECT material_id FROM wms_material WHERE material_code='MAT20250009'); -- 导热硅脂

-- 辅料ID
SET @au_box_id     = (SELECT material_id FROM wms_material WHERE material_code='MAT20250007'); -- 包装纸箱
SET @au_bag_id     = (SELECT material_id FROM wms_material WHERE material_code='MAT20250008'); -- 防静电包装袋

-- 插入半成品BOM
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, expire_date, remark, del_flag, create_by, create_time) VALUES
('BOM-SC-001', 'BOM-PCBA主板组件', @sc_pcba_id,    'MAT20250003', 'PCBA主板组件',   '0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', 'PCBA主板组件标准BOM', '0', 'admin', NOW()),
('BOM-SC-002', 'BOM-锂电池组',     @sc_battery_id, 'MAT20250004', '锂电池组 48V20Ah','0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '锂电池组标准BOM', '0', 'admin', NOW()),
('BOM-SC-003', 'BOM-手环外壳组件', @sc_case_wrist, 'MAT-SC-001',  '手环外壳组件',   '0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '手环外壳组件标准BOM', '0', 'admin', NOW()),
('BOM-SC-004', 'BOM-耳机外壳组件', @sc_case_ear,   'MAT-SC-002',  '耳机外壳组件',   '0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '耳机外壳组件标准BOM', '0', 'admin', NOW()),
('BOM-SC-005', 'BOM-耳机充电盒组件',@sc_box_ear,   'MAT-SC-003',  '耳机充电盒组件', '0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '耳机充电盒组件标准BOM', '0', 'admin', NOW());

-- 插入成品BOM
INSERT INTO mms_bom (bom_no, bom_name, product_id, product_code, product_name, bom_type, version, base_qty, base_unit, status, effective_date, expire_date, remark, del_flag, create_by, create_time) VALUES
('BOM-FP-001', 'BOM-智能手环Pro',  @fp_wrist_pro,  'MAT20250005', '智能手环 Pro',   '0', '2.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '智能手环Pro标准BOM V2.0', '0', 'admin', NOW()),
('BOM-FP-002', 'BOM-无线蓝牙耳机', @fp_earphone,   'MAT20250006', '无线蓝牙耳机',   '0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '无线蓝牙耳机标准BOM', '0', 'admin', NOW()),
('BOM-FP-003', 'BOM-手环表带替换装',@fp_strap,     'MAT-FP-003',  '手环表带替换装', '0', '1.0', 1.000000, '1', '1', '2026-01-01', '2027-12-31', '手环表带替换装标准BOM', '0', 'admin', NOW()),
('BOM-FP-004', 'BOM-蓝牙耳机套装', @fp_ear_set,    'MAT-FP-004',  '蓝牙耳机套装',   '0', '1.0', 1.000000, '1', '1', '2026-01-01', '2027-12-31', '蓝牙耳机套装标准BOM', '0', 'admin', NOW()),
('BOM-FP-005', 'BOM-智能手环Lite', @fp_wrist_lite, 'MAT-FP-005',  '智能手环 Lite版','0', '1.0', 1.000000, '0', '1', '2026-01-01', '2027-12-31', '智能手环Lite版标准BOM', '0', 'admin', NOW());

-- =====================================================
-- 6. 插入BOM明细
-- =====================================================
-- 获取BOM ID
SET @bom_sc1 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-SC-001');
SET @bom_sc2 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-SC-002');
SET @bom_sc3 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-SC-003');
SET @bom_sc4 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-SC-004');
SET @bom_sc5 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-SC-005');
SET @bom_fp1 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-FP-001');
SET @bom_fp2 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-FP-002');
SET @bom_fp3 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-FP-003');
SET @bom_fp4 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-FP-004');
SET @bom_fp5 = (SELECT bom_id FROM mms_bom WHERE bom_no='BOM-FP-005');

-- --- BOM-SC-001: PCBA主板组件 ---
-- 原材料: 导热硅脂、ABS塑料颗粒（外壳）、铝合金型材（散热片）
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_sc1, 10, @rm_grease_id, 'MAT20250009', '导热硅脂',   'HY510 灰色 1g/支',         '0', 0.500000, 2.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '散热用导热材料'),
(@bom_sc1, 20, @rm_abs_id,    'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋',      '3', 0.200000, 3.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '外壳注塑原料'),
(@bom_sc1, 30, @rm_alu_id,    'MAT20250010', '铝合金型材', '6063-T5 20mm*20mm 6米/根','1', 0.100000, 1.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '散热片材料');

-- --- BOM-SC-002: 锂电池组 ---
-- 原材料: 铝合金型材（外壳）、导热硅脂
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_sc2, 10, @rm_alu_id,    'MAT20250010', '铝合金型材', '6063-T5 20mm*20mm 6米/根','1', 0.500000, 1.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '电池外壳材料'),
(@bom_sc2, 20, @rm_grease_id, 'MAT20250009', '导热硅脂',   'HY510 灰色 1g/支',         '0', 1.000000, 0.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '电芯间导热填充'),
(@bom_sc2, 30, @rm_steel_id,  'MAT20250002', '304不锈钢板', '1.2mm*1220mm*2440mm',     '3', 0.300000, 2.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '连接片材料');

-- --- BOM-SC-003: 手环外壳组件 ---
-- 原材料: ABS塑料颗粒
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_sc3, 10, @rm_abs_id, 'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', '3', 0.300000, 5.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '表壳+底壳注塑原料'),
(@bom_sc3, 20, @rm_alu_id, 'MAT20250010', '铝合金型材', '6063-T5 20mm*20mm 6米/根', '1', 0.050000, 2.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '按键材料');

-- --- BOM-SC-004: 耳机外壳组件 ---
-- 原材料: ABS塑料颗粒
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_sc4, 10, @rm_abs_id, 'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', '3', 0.150000, 5.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '左/右耳腔体注塑原料'),
(@bom_sc4, 20, @rm_grease_id, 'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '0', 0.200000, 0.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '接合面密封用');

-- --- BOM-SC-005: 耳机充电盒组件 ---
-- 半成品引用: PCBA主板组件 (bom_ref_id)
-- 原材料: ABS塑料颗粒、锂电池组(半成品，有BOM引用)
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_sc5, 10, @rm_abs_id,     'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', '3', 0.200000, 5.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '盒体注塑原料'),
(@bom_sc5, 20, @sc_pcba_id,    'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc1, '0', 'admin', NOW(), '充电盒控制板（引用子BOM）'),
(@bom_sc5, 30, @sc_battery_id, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc2, '0', 'admin', NOW(), '充电盒内置电池（引用子BOM）');

-- --- BOM-FP-001: 智能手环Pro ---
-- 半成品引用: PCBA主板组件、锂电池组、手环外壳组件
-- 辅料: 防静电包装袋
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_fp1, 10, @sc_pcba_id,    'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc1, '0', 'admin', NOW(), '手环主板（引用子BOM）'),
(@bom_fp1, 20, @sc_battery_id, 'MAT20250004', '锂电池组 48V20Ah', '18650-13S4P 带BMS', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc2, '0', 'admin', NOW(), '手环电池（引用子BOM）'),
(@bom_fp1, 30, @sc_case_wrist, 'MAT-SC-001',  '手环外壳组件', 'TPU+ABS 黑色 42mm表盘', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc3, '0', 'admin', NOW(), '手环外壳（引用子BOM）'),
(@bom_fp1, 40, @rm_grease_id,  'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '0', 0.500000, 0.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '主板与底壳间导热'),
(@bom_fp1, 50, @au_bag_id,     'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '0', 1.000000, 0.00, '0', '2', '0', NULL, '0', 'admin', NOW(), '成品包装用');

-- --- BOM-FP-002: 无线蓝牙耳机 ---
-- 半成品引用: PCBA主板组件、耳机外壳组件
-- 辅料: 防静电包装袋
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_fp2, 10, @sc_pcba_id,  'MAT20250003', 'PCBA主板组件', 'V2.3 带WiFi模组', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc1, '0', 'admin', NOW(), '耳机主板（引用子BOM）'),
(@bom_fp2, 20, @sc_case_ear,'MAT-SC-002',  '耳机外壳组件', 'PC+ABS 白色 入耳式', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc4, '0', 'admin', NOW(), '耳机外壳（引用子BOM）'),
(@bom_fp2, 30, @rm_grease_id,'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '0', 0.300000, 0.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '喇叭与腔体间密封'),
(@bom_fp2, 40, @au_bag_id,  'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '0', 1.000000, 0.00, '0', '2', '0', NULL, '0', 'admin', NOW(), '成品包装用');

-- --- BOM-FP-003: 手环表带替换装 ---
-- 原材料: ABS塑料颗粒、铝合金型材
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_fp3, 10, @rm_abs_id, 'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', '3', 0.100000, 5.00, '1', '1', '0', NULL, '0', 'admin', NOW(), '表带注塑原料(S/M/L三尺寸)'),
(@bom_fp3, 20, @rm_alu_id, 'MAT20250010', '铝合金型材', '6063-T5 20mm*20mm 6米/根', '1', 0.020000, 2.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '表带拆卸工具'),
(@bom_fp3, 30, @au_box_id, 'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '1', 1.000000, 0.00, '0', '2', '0', NULL, '0', 'admin', NOW(), '外箱包装');

-- --- BOM-FP-004: 蓝牙耳机套装 ---
-- 半成品引用: 耳机外壳组件、耳机充电盒组件
-- 辅料: 包装纸箱、防静电包装袋
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_fp4, 10, @sc_case_ear, 'MAT-SC-002',  '耳机外壳组件', 'PC+ABS 白色 入耳式', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc4, '0', 'admin', NOW(), '耳机本体（引用子BOM）'),
(@bom_fp4, 20, @sc_box_ear,  'MAT-SC-003',  '耳机充电盒组件', 'ABS 白色 Type-C充电口', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc5, '0', 'admin', NOW(), '充电盒（引用子BOM，含二级子BOM）'),
(@bom_fp4, 30, @au_box_id,   'MAT20250007', '包装纸箱 400*300*250', '五层瓦楞 K=K', '1', 1.000000, 0.00, '0', '2', '0', NULL, '0', 'admin', NOW(), '套装外箱'),
(@bom_fp4, 40, @au_bag_id,  'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '0', 2.000000, 0.00, '0', '2', '0', NULL, '0', 'admin', NOW(), '内包装用');

-- --- BOM-FP-005: 智能手环Lite版 ---
-- 半成品引用: 手环外壳组件
-- 原材料: ABS塑料颗粒、导热硅脂
-- 辅料: 防静电包装袋
INSERT INTO mms_bom_detail (bom_id, seq, material_id, material_code, material_name, spec_model, unit, usage_qty, loss_rate, is_key_material, supply_type, is_phantom, bom_ref_id, del_flag, create_by, create_time, remark) VALUES
(@bom_fp5, 10, @sc_case_wrist, 'MAT-SC-001',  '手环外壳组件', 'TPU+ABS 黑色 42mm表盘', '0', 1.000000, 0.00, '1', '1', '0', @bom_sc3, '0', 'admin', NOW(), '手环外壳（引用子BOM）'),
(@bom_fp5, 20, @rm_abs_id,     'MAT20250001', 'ABS塑料颗粒', 'PA-757 白色 25kg/袋', '3', 0.150000, 5.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '表壳补充原料'),
(@bom_fp5, 30, @rm_grease_id,  'MAT20250009', '导热硅脂', 'HY510 灰色 1g/支', '0', 0.300000, 0.00, '0', '1', '0', NULL, '0', 'admin', NOW(), '密封导热用'),
(@bom_fp5, 40, @au_bag_id,     'MAT20250008', '防静电包装袋', '200mm*300mm 自封袋', '0', 1.000000, 0.00, '0', '2', '0', NULL, '0', 'admin', NOW(), '成品包装用');

-- =====================================================
-- 7. 插入工艺路线（10条: 5半成品 + 5成品）
-- =====================================================
-- 获取工序ID
SET @p_smt = (SELECT process_id FROM mms_process WHERE process_code='PRC-SC-SMT');
SET @p_inj = (SELECT process_id FROM mms_process WHERE process_code='PRC-SC-INJ');
SET @p_wld = (SELECT process_id FROM mms_process WHERE process_code='PRC-SC-WLD');
SET @p_glu = (SELECT process_id FROM mms_process WHERE process_code='PRC-SC-GLU');
SET @p_sct = (SELECT process_id FROM mms_process WHERE process_code='PRC-SC-TST');
SET @p_asm = (SELECT process_id FROM mms_process WHERE process_code='PRC-FP-ASM');
SET @p_fnc = (SELECT process_id FROM mms_process WHERE process_code='PRC-FP-FNC');
SET @p_age = (SELECT process_id FROM mms_process WHERE process_code='PRC-FP-AGE');
SET @p_pkg = (SELECT process_id FROM mms_process WHERE process_code='PRC-FP-PKG');
SET @p_cut = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819001'); -- 下料切割
SET @p_cnc = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819002'); -- 数控车削
SET @p_mil = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819003'); -- 铣削加工
SET @p_grd = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819005'); -- 表面研磨
SET @p_ass = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819006'); -- 部件装配
SET @p_fin = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819009'); -- 成品检验
SET @p_pak = (SELECT process_id FROM mms_process WHERE process_code='PRC20260819010'); -- 包装入库

-- 获取产能单元ID
SET @r_smt   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-SC-SMT');
SET @r_inj   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-SC-INJ');
SET @r_wld   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-SC-WLD');
SET @r_glu   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-SC-GLU');
SET @r_sct   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-SC-TST');
SET @r_asm   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-FP-ASM');
SET @r_fnc   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-FP-FNC');
SET @r_age   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-FP-AGE');
SET @r_pkg   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES-FP-PKG');
SET @r_cncA  = (SELECT resource_id FROM mms_resource WHERE resource_code='RES20260819001'); -- CNC加工中心A
SET @r_aslA  = (SELECT resource_id FROM mms_resource WHERE resource_code='RES20260819002'); -- 总装线A
SET @r_qcF   = (SELECT resource_id FROM mms_resource WHERE resource_code='RES20260819005'); -- 质检工作站F

-- --- 半成品工艺路线 ---
INSERT INTO mms_route (route_no, route_name, product_id, product_code, product_name, version, effective_date, expire_date, status, audit_by, audit_time, audit_remark, default_workshop, default_line, total_std_time, process_count, remark, del_flag, create_by, create_time) VALUES
('RT-SC-001', 'PCBA主板组件工艺路线', @sc_pcba_id,    'MAT20250003', 'PCBA主板组件',   '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '电子车间', 'SMT产线A', 1.80, 3, 'PCBA贴片→点胶→测试', '0', 'admin', NOW()),
('RT-SC-002', '锂电池组工艺路线',     @sc_battery_id, 'MAT20250004', '锂电池组 48V20Ah','1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '电子车间', '电池组装线', 2.50, 3, '焊接→点胶→测试', '0', 'admin', NOW()),
('RT-SC-003', '手环外壳组件工艺路线', @sc_case_wrist, 'MAT-SC-001',  '手环外壳组件',   '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '注塑车间', '注塑线A', 2.00, 3, '注塑→研磨→检验', '0', 'admin', NOW()),
('RT-SC-004', '耳机外壳组件工艺路线', @sc_case_ear,   'MAT-SC-002',  '耳机外壳组件',   '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '注塑车间', '注塑线A', 2.30, 4, '注塑→研磨→点胶→检验', '0', 'admin', NOW()),
('RT-SC-005', '耳机充电盒组件工艺路线',@sc_box_ear,    'MAT-SC-003',  '耳机充电盒组件', '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '电子车间', '总装线A', 3.30, 4, '注塑→SMT→组装→测试', '0', 'admin', NOW());

-- --- 成品工艺路线 ---
INSERT INTO mms_route (route_no, route_name, product_id, product_code, product_name, version, effective_date, expire_date, status, audit_by, audit_time, audit_remark, default_workshop, default_line, total_std_time, process_count, remark, del_flag, create_by, create_time) VALUES
('RT-FP-001', '智能手环Pro工艺路线',  @fp_wrist_pro,  'MAT20250005', '智能手环 Pro',   '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '装配车间', '总装线A', 7.80, 5, '组装→功能测试→老化→包装', '0', 'admin', NOW()),
('RT-FP-002', '无线蓝牙耳机工艺路线', @fp_earphone,   'MAT20250006', '无线蓝牙耳机',   '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '装配车间', '总装线A', 7.30, 5, '组装→功能测试→老化→包装', '0', 'admin', NOW()),
('RT-FP-003', '手环表带替换装工艺路线',@fp_strap,     'MAT-FP-003',  '手环表带替换装', '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '注塑车间', '注塑线A', 2.70, 3, '注塑→检验→包装', '0', 'admin', NOW()),
('RT-FP-004', '蓝牙耳机套装工艺路线', @fp_ear_set,    'MAT-FP-004',  '蓝牙耳机套装',   '1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '装配车间', '总装线A', 6.30, 4, '组装→功能测试→老化→包装', '0', 'admin', NOW()),
('RT-FP-005', '智能手环Lite工艺路线', @fp_wrist_lite, 'MAT-FP-005',  '智能手环 Lite版','1.0', '2026-01-01', '2027-12-31', '2', 'admin', NOW(), '审核通过', '装配车间', '总装线A', 5.80, 4, '组装→功能测试→包装', '0', 'admin', NOW());

-- =====================================================
-- 8. 插入工艺路线工序明细
-- =====================================================
-- 获取路线ID
SET @rt_sc1 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-001');
SET @rt_sc2 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-002');
SET @rt_sc3 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-003');
SET @rt_sc4 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-004');
SET @rt_sc5 = (SELECT route_id FROM mms_route WHERE route_no='RT-SC-005');
SET @rt_fp1 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-001');
SET @rt_fp2 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-002');
SET @rt_fp3 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-003');
SET @rt_fp4 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-004');
SET @rt_fp5 = (SELECT route_id FROM mms_route WHERE route_no='RT-FP-005');

-- --- RT-SC-001: PCBA主板组件 (SMT贴片→点胶封胶→半成品测试) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_sc1, @p_smt, 'PRC-SC-SMT', 'SMT贴片焊接', '0', '电子车间', 'SMT产线A', 10, 0.80, 0.50, '1', '0', @r_smt, 'SMT贴片产线', '0', 'admin', NOW(), 'PCB贴片+回流焊+AOI检测'),
(@rt_sc1, @p_glu, 'PRC-SC-GLU', '点胶封胶', '0', '电子车间', '点胶线A', 20, 0.60, 0.30, '0', '0', @r_glu, '点胶工作站', '0', 'admin', NOW(), '元件固定与防水处理'),
(@rt_sc1, @p_sct, 'PRC-SC-TST', '半成品功能测试', '2', '质检部', '测试线A', 30, 0.40, 0.10, '1', '0', @r_sct, '半成品测试台A', '0', 'admin', NOW(), '电路通断与功能初测');

-- --- RT-SC-002: 锂电池组 (电池焊接→点胶封胶→半成品测试) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_sc2, @p_wld, 'PRC-SC-WLD', '电池焊接组装', '0', '电子车间', '电池组装线', 10, 1.50, 0.50, '1', '0', @r_wld, '电池焊接工作站', '0', 'admin', NOW(), '电芯激光点焊+BMS安装'),
(@rt_sc2, @p_glu, 'PRC-SC-GLU', '点胶封胶', '0', '电子车间', '点胶线A', 20, 0.60, 0.30, '0', '0', @r_glu, '点胶工作站', '0', 'admin', NOW(), '电池组防水密封'),
(@rt_sc2, @p_sct, 'PRC-SC-TST', '半成品功能测试', '2', '质检部', '测试线A', 30, 0.40, 0.10, '1', '0', @r_sct, '半成品测试台A', '0', 'admin', NOW(), '电池组电压/容量/BMS通信测试');

-- --- RT-SC-003: 手环外壳组件 (注塑成型→表面研磨→成品检验) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_sc3, @p_inj, 'PRC-SC-INJ', '注塑成型', '0', '注塑车间', '注塑线A', 10, 1.20, 0.75, '1', '0', @r_inj, '注塑机C', '0', 'admin', NOW(), '表壳+底壳注塑成型'),
(@rt_sc3, @p_grd, 'PRC20260819005', '表面研磨', '0', '一车间', 'B线', 20, 0.80, 0.30, '0', '0', NULL, NULL, '0', 'admin', NOW(), '表面修整去毛刺'),
(@rt_sc3, @p_fin, 'PRC20260819009', '成品检验', '2', '质检部', '检验线', 30, 0.50, 0.15, '1', '0', @r_qcF, '质检工作站F', '0', 'admin', NOW(), '外观与尺寸检验');

-- --- RT-SC-004: 耳机外壳组件 (注塑成型→表面研磨→点胶封胶→成品检验) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_sc4, @p_inj, 'PRC-SC-INJ', '注塑成型', '0', '注塑车间', '注塑线A', 10, 1.20, 0.75, '1', '0', @r_inj, '注塑机C', '0', 'admin', NOW(), '左/右耳腔体注塑'),
(@rt_sc4, @p_grd, 'PRC20260819005', '表面研磨', '0', '一车间', 'B线', 20, 0.80, 0.30, '0', '0', NULL, NULL, '0', 'admin', NOW(), '表面修整'),
(@rt_sc4, @p_glu, 'PRC-SC-GLU', '点胶封胶', '0', '电子车间', '点胶线A', 30, 0.30, 0.15, '0', '0', @r_glu, '点胶工作站', '0', 'admin', NOW(), '接合面密封胶'),
(@rt_sc4, @p_fin, 'PRC20260819009', '成品检验', '2', '质检部', '检验线', 40, 0.50, 0.15, '1', '0', @r_qcF, '质检工作站F', '0', 'admin', NOW(), '外观检验');

-- --- RT-SC-005: 耳机充电盒组件 (注塑成型→SMT贴片→部件装配→半成品测试) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_sc5, @p_inj, 'PRC-SC-INJ', '注塑成型', '0', '注塑车间', '注塑线A', 10, 1.20, 0.75, '1', '0', @r_inj, '注塑机C', '0', 'admin', NOW(), '盒体注塑'),
(@rt_sc5, @p_smt, 'PRC-SC-SMT', 'SMT贴片焊接', '0', '电子车间', 'SMT产线A', 20, 0.80, 0.50, '1', '0', @r_smt, 'SMT贴片产线', '0', 'admin', NOW(), '充电PCB贴片'),
(@rt_sc5, @p_ass, 'PRC20260819006', '部件装配', '1', '二车间', '装配线', 30, 1.00, 0.50, '1', '0', @r_aslA, '总装线A', '0', 'admin', NOW(), 'PCB+电池+盒体组装'),
(@rt_sc5, @p_sct, 'PRC-SC-TST', '半成品功能测试', '2', '质检部', '测试线A', 40, 0.40, 0.10, '1', '0', @r_sct, '半成品测试台A', '0', 'admin', NOW(), '充电功能与接口测试');

-- --- RT-FP-001: 智能手环Pro (成品组装→功能测试→老化测试→成品检验→包装) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_fp1, @p_asm, 'PRC-FP-ASM', '成品组装', '1', '装配车间', '总装线A', 10, 1.80, 0.50, '1', '0', @r_asm, '成品总装线B', '0', 'admin', NOW(), '主板+电池+外壳组装'),
(@rt_fp1, @p_fnc, 'PRC-FP-FNC', '功能与性能测试', '2', '质检部', '测试线B', 20, 1.00, 0.20, '1', '0', @r_fnc, '成品测试台B', '0', 'admin', NOW(), '心率+蓝牙+防水+触屏测试'),
(@rt_fp1, @p_age, 'PRC-FP-AGE', '老化测试', '0', '老化车间', '老化房A', 30, 4.00, 0.30, '1', '0', @r_age, '老化房B', '0', 'admin', NOW(), '高温40℃老化4小时'),
(@rt_fp1, @p_fin, 'PRC20260819009', '成品检验', '2', '质检部', '检验线', 40, 0.50, 0.15, '1', '0', @r_qcF, '质检工作站F', '0', 'admin', NOW(), 'FQC终检'),
(@rt_fp1, @p_pkg, 'PRC-FP-PKG', '包装与贴标', '3', '包装车间', '包装线A', 50, 0.50, 0.20, '0', '0', @r_pkg, '包装线B', '0', 'admin', NOW(), '装盒+贴标+入库');

-- --- RT-FP-002: 无线蓝牙耳机 (成品组装→功能测试→老化测试→成品检验→包装) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_fp2, @p_asm, 'PRC-FP-ASM', '成品组装', '1', '装配车间', '总装线A', 10, 1.80, 0.50, '1', '0', @r_asm, '成品总装线B', '0', 'admin', NOW(), '主板+外壳+喇叭组装'),
(@rt_fp2, @p_fnc, 'PRC-FP-FNC', '功能与性能测试', '2', '质检部', '测试线B', 20, 1.00, 0.20, '1', '0', @r_fnc, '成品测试台B', '0', 'admin', NOW(), '蓝牙配对+降噪+防水测试'),
(@rt_fp2, @p_age, 'PRC-FP-AGE', '老化测试', '0', '老化车间', '老化房A', 30, 4.00, 0.30, '1', '0', @r_age, '老化房B', '0', 'admin', NOW(), '高温老化4小时'),
(@rt_fp2, @p_fin, 'PRC20260819009', '成品检验', '2', '质检部', '检验线', 40, 0.50, 0.15, '1', '0', @r_qcF, '质检工作站F', '0', 'admin', NOW(), 'FQC终检'),
(@rt_fp2, @p_pkg, 'PRC-FP-PKG', '包装与贴标', '3', '包装车间', '包装线A', 50, 0.50, 0.20, '0', '0', @r_pkg, '包装线B', '0', 'admin', NOW(), '装盒+贴标+入库');

-- --- RT-FP-003: 手环表带替换装 (注塑成型→成品检验→包装) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_fp3, @p_inj, 'PRC-SC-INJ', '注塑成型', '0', '注塑车间', '注塑线A', 10, 1.20, 0.75, '1', '0', @r_inj, '注塑机C', '0', 'admin', NOW(), 'S/M/L三尺寸表带注塑'),
(@rt_fp3, @p_fin, 'PRC20260819009', '成品检验', '2', '质检部', '检验线', 20, 0.50, 0.15, '1', '0', @r_qcF, '质检工作站F', '0', 'admin', NOW(), '外观与尺寸检验'),
(@rt_fp3, @p_pak, 'PRC20260819010', '包装入库', '3', '三车间', '包装线', 30, 1.00, 0.20, '0', '0', @r_pkg, '包装线B', '0', 'admin', NOW(), '装箱+贴标+入库');

-- --- RT-FP-004: 蓝牙耳机套装 (成品组装→功能测试→老化测试→包装) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_fp4, @p_asm, 'PRC-FP-ASM', '成品组装', '1', '装配车间', '总装线A', 10, 1.80, 0.50, '1', '0', @r_asm, '成品总装线B', '0', 'admin', NOW(), '耳机+充电盒+配件组装'),
(@rt_fp4, @p_fnc, 'PRC-FP-FNC', '功能与性能测试', '2', '质检部', '测试线B', 20, 1.00, 0.20, '1', '0', @r_fnc, '成品测试台B', '0', 'admin', NOW(), '蓝牙配对+充电+防水测试'),
(@rt_fp4, @p_age, 'PRC-FP-AGE', '老化测试', '0', '老化车间', '老化房A', 30, 4.00, 0.30, '1', '0', @r_age, '老化房B', '0', 'admin', NOW(), '高温老化4小时'),
(@rt_fp4, @p_pkg, 'PRC-FP-PKG', '包装与贴标', '3', '包装车间', '包装线A', 40, 0.50, 0.20, '0', '0', @r_pkg, '包装线B', '0', 'admin', NOW(), '套装外箱包装+贴标');

-- --- RT-FP-005: 智能手环Lite (成品组装→功能测试→成品检验→包装) ---
INSERT INTO mms_route_process (route_id, process_id, process_code, process_name, process_type, workshop, line, step_seq, std_time, prep_time, is_key_process, is_outsource, resource_id, resource_name, del_flag, create_by, create_time, remark) VALUES
(@rt_fp5, @p_asm, 'PRC-FP-ASM', '成品组装', '1', '装配车间', '总装线A', 10, 1.80, 0.50, '1', '0', @r_asm, '成品总装线B', '0', 'admin', NOW(), '外壳+主板+电池组装'),
(@rt_fp5, @p_fnc, 'PRC-FP-FNC', '功能与性能测试', '2', '质检部', '测试线B', 20, 1.00, 0.20, '1', '0', @r_fnc, '成品测试台B', '0', 'admin', NOW(), '蓝牙+触屏+基础功能测试'),
(@rt_fp5, @p_fin, 'PRC20260819009', '成品检验', '2', '质检部', '检验线', 30, 0.50, 0.15, '1', '0', @r_qcF, '质检工作站F', '0', 'admin', NOW(), 'FQC终检'),
(@rt_fp5, @p_pkg, 'PRC-FP-PKG', '包装与贴标', '3', '包装车间', '包装线A', 40, 0.50, 0.20, '0', '0', @r_pkg, '包装线B', '0', 'admin', NOW(), '装盒+贴标+入库');

-- =====================================================
-- 9. 更新已有BOM和工艺路线状态（使旧数据也一致）
-- =====================================================
-- 将旧的测试BOM（BOM-001, BOM-002, BOM-003, BOM-001-COPY）状态设为停用，避免干扰
UPDATE mms_bom SET status='2' WHERE bom_no IN ('BOM-001','BOM-002','BOM-003','BOM-001-COPY') AND del_flag='0';

-- 将旧的测试工艺路线（RT0001~RT0007）状态设为停用
UPDATE mms_route SET status='3' WHERE route_no IN ('RT0001','RT0002','RT0003','RT0004','RT0005','RT0006','RT0007') AND del_flag='0';

-- =====================================================
-- 10. 更新工艺路线的 total_std_time 和 process_count（修正为实际汇总值）
-- =====================================================
UPDATE mms_route SET total_std_time = (
    SELECT COALESCE(SUM(std_time), 0) FROM mms_route_process WHERE route_id = mms_route.route_id AND del_flag='0'
), process_count = (
    SELECT COUNT(*) FROM mms_route_process WHERE route_id = mms_route.route_id AND del_flag='0'
) WHERE route_no LIKE 'RT-SC-%' OR route_no LIKE 'RT-FP-%';
