INSERT INTO pms_supplier_catalog (supplier_id, supplier_name, material_id, material_code, material_name, unit, unit_price, tax_rate, min_order_qty, delivery_cycle, currency, status, del_flag, create_by, create_time)
SELECT s.supplier_id, s.supplier_name, m.material_id, m.material_code, m.material_name, m.unit, p.price, 9, 100, 7, 'CNY', '0', '0', 'admin', NOW()
FROM (SELECT 'GYS0001' code, 'YL0001' mat, 12.00 price UNION ALL SELECT 'GYS0002','YL0002',8.50 UNION ALL SELECT 'GYS0003','YL0003',6.80 UNION ALL SELECT 'GYS0004','YL0005',5.20 UNION ALL SELECT 'GYS0005','YL0004',4.50 UNION ALL SELECT 'GYS0005','YL0006',2.80 UNION ALL SELECT 'GYS0006','FL0001',0.85 UNION ALL SELECT 'GYS0007','FL0002',0.12 UNION ALL SELECT 'GYS0007','FL0003',0.08 UNION ALL SELECT 'GYS0008','FL0004',3.50 UNION ALL SELECT 'GYS0009','YL0005',5.00) t
JOIN wms_supplier s ON s.supplier_code=t.code
JOIN wms_material m ON m.material_code=t.mat;
