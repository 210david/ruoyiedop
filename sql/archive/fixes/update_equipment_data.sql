-- 设备台账数据补全脚本
-- 补全所有空字段，报废日期仅报废状态设备才有

UPDATE dms_equipment SET asset_code='ZC20240001', model='C6132', serial_number='SN20230001', manufacturer='沈阳机床', supplier='若依科技', original_value=45000.00, dept_id=105, dept_name='综合管理部', install_location='D车间-01工位', equipment_status='5', equipment_level='2', warranty_date='2024-07-01', retire_date='2026-07-15', responsible_id=145, responsible_name='罗强', remark='设备老旧，已报废处置', update_by='admin', update_time=NOW() WHERE equipment_id=1;

UPDATE dms_equipment SET asset_code='ZC20240002', equipment_level='0', warranty_date='2025-01-15', dept_name='研发部', responsible_name='张涛', remark='主力加工设备', update_by='admin', update_time=NOW() WHERE equipment_id=2;

UPDATE dms_equipment SET asset_code='ZC20240003', equipment_level='1', warranty_date='2025-02-20', dept_name='市场部', responsible_name='张敏', remark='常规铣削加工', update_by='admin', update_time=NOW() WHERE equipment_id=3;

UPDATE dms_equipment SET asset_code='ZC20240004', equipment_level='2', warranty_date='2025-03-10', dept_name='市场部', responsible_name='李华', remark='钻孔专用设备', update_by='admin', update_time=NOW() WHERE equipment_id=4;

UPDATE dms_equipment SET asset_code='ZC20240005', equipment_level='1', warranty_date='2025-04-05', dept_name='综合管理部', responsible_name='李华', remark='精密磨削加工', update_by='admin', update_time=NOW() WHERE equipment_id=5;

UPDATE dms_equipment SET asset_code='ZC20240006', equipment_level='0', warranty_date='2025-05-15', dept_name='综合管理部', responsible_name='吴霞', remark='高精度加工中心', update_by='admin', update_time=NOW() WHERE equipment_id=6;
