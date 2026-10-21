-- 滇味鲜供应商与客户主数据完善（依据分析报告，模板数据供用户核实调整）
-- 1. 供应商（wms_supplier，覆盖原料农户/经纪人/辅料/包材典型类型）
INSERT INTO wms_supplier (supplier_code, supplier_name, supplier_short_name, supplier_type, supplier_level, unified_credit_code, contact_person, contact_phone, address, legal_person, payment_method, payment_days, settlement_type, currency, delivery_cycle, cooperation_date, qualification_status, status, del_flag, create_by, create_time, remark) VALUES
('GYS0001', '陆良县芳华辣椒种植专业合作社', '芳华辣椒社', '1', 'A', '91530322MA6L12345X', '王建国', '13887123456', '曲靖市陆良县芳华镇', '王建国', '1', 30, '1', 'CNY', 7, '2024-09-01', '1', '0', '0', 'admin', NOW(), '干辣椒主供（农户合作社）'),
('GYS0002', '三宝镇鲜椒收购站', '三宝收购站', '1', 'B', '92530302MA7X87654A', '李有福', '13987234567', '曲靖市麒麟区三宝镇', '李有福', '1', 15, '1', 'CNY', 3, '2024-08-20', '1', '0', '0', 'admin', NOW(), '鲜辣椒季节性收购（经纪人）'),
('GYS0003', '陆良滇陆豆业经营部', '滇陆豆业', '1', 'A', '91530322MA6N23456Y', '赵树明', '13787345678', '曲靖市陆良县中枢街道', '赵树明', '1', 30, '1', 'CNY', 7, '2023-03-15', '1', '0', '0', 'admin', NOW(), '蚕豆/大豆供应商'),
('GYS0004', '云南盐业曲靖配送中心', '云盐曲靖', '2', 'A', '91530300MA6K34567Z', '张伟', '13698765432', '曲靖市麒麟区南宁街道', '张伟', '2', 30, '2', 'CNY', 5, '2023-05-10', '1', '0', '0', 'admin', NOW(), '食盐供应（月结）'),
('GYS0005', '陆良面粉厂', '陆良面粉厂', '2', 'B', '91530322MA6L45678B', '钱进', '13598765431', '曲靖市陆良县板桥镇', '钱进', '1', 30, '1', 'CNY', 7, '2023-06-01', '1', '0', '0', 'admin', NOW(), '小麦粉/麸皮'),
('GYS0006', '曲靖市宏发玻璃制品有限公司', '宏发玻璃', '3', 'A', '91530302MA6M56789C', '孙丽', '13388887777', '曲靖市麒麟区沿江街道', '孙丽', '2', 45, '2', 'CNY', 15, '2023-01-01', '1', '0', '0', 'admin', NOW(), '玻璃瓶（月结）'),
('GYS0007', '曲靖金盖包装有限公司', '金盖包装', '3', 'B', '91530302MA6N67890D', '周斌', '13266665555', '曲靖市经济技术开发区', '周斌', '2', 45, '2', 'CNY', 15, '2023-02-15', '1', '0', '0', 'admin', NOW(), '瓶盖/标签（月结）'),
('GYS0008', '陆良华丰纸箱厂', '华丰纸箱', '3', 'B', '92530322MA6L78901E', '吴刚', '13155554444', '曲靖市陆良县马街镇', '吴刚', '1', 30, '1', 'CNY', 10, '2023-04-01', '1', '0', '0', 'admin', NOW(), '纸箱包装'),
('GYS0009', '曲靖众合调味配料商行', '众合配料', '2', 'B', '92530302MA6M89012F', '冯梅', '13044443333', '曲靖市麒麟区寥廓街道', '冯梅', '1', 30, '1', 'CNY', 7, '2024-01-10', '1', '0', '0', 'admin', NOW(), '味精/香辛料等调味配料');

-- 2. 客户（mk_customer，覆盖 B端食堂/流通经销商/商超三类）
INSERT INTO mk_customer (customer_no, customer_name, customer_level, customer_source, industry, company_size, province, city, district, address, customer_status, first_contact_date, cooperation_date, credit_limit, credit_days, credit_status, user_id, dept_id, del_flag, create_by, create_time, remark) VALUES
('KH0001', '师宗县第一中学', '2', '2', '教育', '0', '云南省', '曲靖市', '师宗县', '师宗县丹凤街道', '0', '2026-07-01', '2026-08-01', 100000, 30, '0', 4, 104, '0', 'admin', NOW(), '2026-2027学年食堂大宗食材供货（已中标）'),
('KH0002', '陆良县中枢小学', '3', '2', '教育', '0', '云南省', '曲靖市', '陆良县', '陆良县中枢街道', '0', '2026-06-01', '2026-09-01', 50000, 30, '0', 4, 104, '0', 'admin', NOW(), '食堂调味品供货'),
('KH0003', '曲靖市第二人民医院', '2', '2', '医疗卫生', '0', '云南省', '曲靖市', '麒麟区', '麒麟区文昌街', '0', '2026-05-01', '2026-06-01', 80000, 45, '0', 4, 104, '0', 'admin', NOW(), '单位食堂供货'),
('KH0004', '曲靖市麒麟区粮油批发部', '2', '1', '批发零售', '0', '云南省', '曲靖市', '麒麟区', '麒麟区南片区粮油市场', '0', '2024-03-01', '2024-04-01', 60000, 30, '0', 5, 104, '0', 'admin', NOW(), '经销商批发'),
('KH0005', '陆良县同乐商贸经营部', '2', '1', '批发零售', '0', '云南省', '曲靖市', '陆良县', '陆良县西门外街', '0', '2024-01-01', '2024-02-01', 50000, 30, '0', 5, 104, '0', 'admin', NOW(), '经销商批发'),
('KH0006', '师宗县五龙商贸有限公司', '3', '1', '批发零售', '0', '云南省', '曲靖市', '师宗县', '师宗县梧桐路', '0', '2025-03-01', '2025-04-01', 30000, 15, '0', 5, 104, '0', 'admin', NOW(), '县级经销商'),
('KH0007', '陆良县客都购物中心', '2', '1', '零售商超', '0', '云南省', '曲靖市', '陆良县', '陆良县中兴路', '0', '2024-06-01', '2024-07-01', 40000, 45, '0', 6, 104, '0', 'admin', NOW(), '商超铺货'),
('KH0008', '曲靖沃尔玛百货（麒麟店）', '1', '1', '零售商超', '0', '云南省', '曲靖市', '麒麟区', '麒麟区翠峰路', '0', '2025-06-01', '2025-08-01', 80000, 60, '0', 6, 104, '0', 'admin', NOW(), '连锁商超铺货'),
('KH0009', '陆良县机关事务管理中心', '2', '2', '机关团体', '0', '云南省', '曲靖市', '陆良县', '陆良县政务中心', '0', '2025-09-01', '2025-10-01', 50000, 30, '0', 4, 104, '0', 'admin', NOW(), '单位食堂供货');

-- 3. 供应商供货目录（pms_supplier_catalog，原料/包材关联价格）
INSERT INTO pms_supplier_catalog (supplier_id, supplier_name, material_id, material_code, material_name, unit, unit_price, tax_rate, min_order_qty, delivery_cycle, currency, status, del_flag, create_by, create_time)
SELECT s.supplier_id, s.supplier_name, m.material_id, m.material_code, m.material_name, m.unit, p.price, 9, 100, 7, 'CNY', '0', '0', 'admin', NOW()
FROM (SELECT 'GYS0001' code, 'YL0001' mat, 12.00 price UNION ALL SELECT 'GYS0002','YL0002',8.50 UNION ALL SELECT 'GYS0003','YL0003',6.80 UNION ALL SELECT 'GYS0004','YL0005',5.20 UNION ALL SELECT 'GYS0005','YL0004',4.50 UNION ALL SELECT 'GYS0005','YL0006',2.80 UNION ALL SELECT 'GYS0006','FL0001',0.85 UNION ALL SELECT 'GYS0007','FL0002',0.12 UNION ALL SELECT 'GYS0007','FL0003',0.08 UNION ALL SELECT 'GYS0008','FL0004',3.50 UNION ALL SELECT 'GYS0009','YL0005',5.00) t
JOIN wms_supplier s ON s.supplier_code=t.code
JOIN wms_material m ON m.material_code=t.mat;
