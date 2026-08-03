-- 补充供应商类型、等级、企业性质字典数据
-- 先确保字典类型存在
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT '供应商类型' AS dict_name, 'wms_supplier_type' AS dict_type, '0' AS status, 'admin' AS create_by, sysdate() AS create_time, '供应商类型字典' AS remark
    UNION ALL SELECT '供应商等级', 'wms_supplier_level', '0', 'admin', sysdate(), '供应商等级字典'
    UNION ALL SELECT '企业性质', 'wms_enterprise_nature', '0', 'admin', sysdate(), '企业性质字典'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type dt WHERE dt.dict_type = t.dict_type);

-- 供应商类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '原材料供应商', '0', 'wms_supplier_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '设备供应商', '1', 'wms_supplier_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '服务供应商', '2', 'wms_supplier_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '贸易商', '3', 'wms_supplier_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '其他', '9', 'wms_supplier_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='9');

-- 供应商等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '战略合作', '0', 'wms_supplier_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '优选', '1', 'wms_supplier_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '合格', '2', 'wms_supplier_level', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '备选', '3', 'wms_supplier_level', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='3');

-- 企业性质
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '国有企业', '0', 'wms_enterprise_nature', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '民营企业', '1', 'wms_enterprise_nature', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '股份制企业', '2', 'wms_enterprise_nature', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '外资企业', '3', 'wms_enterprise_nature', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '合资企业', '4', 'wms_enterprise_nature', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='4');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '个体工商户', '5', 'wms_enterprise_nature', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='5');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '其他', '9', 'wms_enterprise_nature', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='9');
