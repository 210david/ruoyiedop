-- 滇味鲜主数据重建（依据分析报告，模板数据供用户核实调整）
-- 1. 清理设备分类测试残留，新增酿造行业分类
DELETE FROM dms_equipment_category WHERE category_id IN (23,24,26,31,32);
INSERT INTO dms_equipment_category (category_code, category_name, parent_id) VALUES
('FJ01', '酿造发酵设备', 0), ('QCL01', '前处理设备', 0), ('GZ01', '灌装包装设备', 0);
SET @cat_fj = (SELECT category_id FROM dms_equipment_category WHERE category_name='酿造发酵设备' LIMIT 1);
SET @cat_qcl = (SELECT category_id FROM dms_equipment_category WHERE category_name='前处理设备' LIMIT 1);
SET @cat_gz = (SELECT category_id FROM dms_equipment_category WHERE category_name='灌装包装设备' LIMIT 1);

-- 2. 仓库（按报告库区划分，5 个）
INSERT INTO wms_warehouse (parent_id, ancestors, node_type, warehouse_code, warehouse_name, dept_id, area_type, location_type, location_status, status, del_flag, create_by, create_time) VALUES
(0, '0', '0', 'YLK001', '原料库', 201, '0', '1', '0', '0', '0', 'admin', NOW()),
(0, '0', '0', 'FLK001', '辅料库', 201, '0', '1', '0', '0', '0', 'admin', NOW()),
(0, '0', '0', 'BCK001', '包材库', 201, '0', '0', '0', '0', '0', 'admin', NOW()),
(0, '0', '0', 'FZK001', '发酵在制品库', 201, '0', '2', '0', '0', '0', 'admin', NOW()),
(0, '0', '0', 'CPK001', '成品库', 201, '0', '0', '0', '0', '0', 'admin', NOW());

-- 3. 物料（酿造调味品典型物料模板，供用户核实调整）
INSERT INTO wms_material (material_code, material_name, material_type, spec_model, unit, is_batch_manage, is_expiry_manage, status, del_flag, create_by, create_time) VALUES
('YL0001', '干辣椒', '0', '', '公斤', 'Y', 'Y', '0', '0', 'admin', NOW()),
('YL0002', '鲜辣椒', '0', '', '公斤', 'Y', 'Y', '0', '0', 'admin', NOW()),
('YL0003', '蚕豆', '0', '', '公斤', 'Y', 'Y', '0', '0', 'admin', NOW()),
('YL0004', '小麦粉', '0', '', '公斤', 'Y', 'Y', '0', '0', 'admin', NOW()),
('YL0005', '食盐', '0', '', '公斤', 'Y', 'N', '0', '0', 'admin', NOW()),
('YL0006', '麸皮', '0', '', '公斤', 'Y', 'Y', '0', '0', 'admin', NOW()),
('FL0001', '玻璃瓶', '3', '', '个', 'N', 'N', '0', '0', 'admin', NOW()),
('FL0002', '瓶盖', '3', '', '个', 'N', 'N', '0', '0', 'admin', NOW()),
('FL0003', '标签', '3', '', '张', 'N', 'N', '0', '0', 'admin', NOW()),
('FL0004', '纸箱', '3', '', '个', 'N', 'N', '0', '0', 'admin', NOW()),
('BCP0001', '发酵酱料（半成品）', '1', '', '公斤', 'Y', 'Y', '0', '0', 'admin', NOW()),
('CP0001', '豆瓣酱', '2', '', '瓶', 'Y', 'Y', '0', '0', 'admin', NOW()),
('CP0002', '香辣酱', '2', '', '瓶', 'Y', 'Y', '0', '0', 'admin', NOW()),
('CP0003', '风味豆豉', '2', '', '瓶', 'Y', 'Y', '0', '0', 'admin', NOW()),
('CP0004', '酱腌菜', '2', '', '袋', 'Y', 'Y', '0', '0', 'admin', NOW());

-- 4. 设备台账（报告中的关键装备，模板数据供用户核实调整）
INSERT INTO dms_equipment (equipment_code, equipment_name, category_id, dept_id, dept_name, install_location, equipment_status, equipment_level, create_by, create_time) VALUES
('SB0001', '辣椒酱发酵池', @cat_fj, 200, '生产部', '发酵车间', '0', '0', 'admin', NOW()),
('SB0002', '辣椒酱发酵槽', @cat_fj, 200, '生产部', '发酵车间', '0', '0', 'admin', NOW()),
('SB0003', '发酵缸', @cat_fj, 200, '生产部', '发酵车间', '0', '2', 'admin', NOW()),
('SB0004', '翻酱机', @cat_fj, 200, '生产部', '发酵车间', '0', '0', 'admin', NOW()),
('SB0005', '辣椒清洗切碎机', @cat_qcl, 200, '生产部', '预处理车间', '0', '1', 'admin', NOW()),
('SB0006', '辣椒烘干装置', @cat_qcl, 200, '生产部', '预处理车间', '0', '1', 'admin', NOW()),
('SB0007', '晒酱顶棚（开合结构）', @cat_fj, 200, '生产部', '晒酱场', '0', '2', 'admin', NOW()),
('SB0008', '大桶酱料运输装车装置', @cat_qcl, 200, '生产部', '成品库月台', '0', '2', 'admin', NOW()),
('SB0009', '定量加料装置', @cat_qcl, 200, '生产部', '调配车间', '0', '1', 'admin', NOW()),
('SB0010', '灌装封口机', @cat_gz, 200, '生产部', '灌装车间', '0', '0', 'admin', NOW());

-- 5. 客户（报告确认的中标客户，模板数据供用户核实调整）
INSERT INTO mk_customer (customer_no, customer_name, industry, city, district, customer_status, create_by, create_time) VALUES
('KH0001', '师宗县第一中学', '教育', '曲靖市', '师宗县', '0', 'admin', NOW());
