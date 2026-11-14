-- ============================================================
-- 供应商类型字典补充金蝶供应商分类（wms_supplier_type）
-- 日期：2026-11-14
-- 说明：金蝶供应商同步会把「供应商分类（group_name）」原样写入
--       wms_supplier.supplier_type，此处补齐对应字典项，
--       保证列表筛选、编辑弹窗下拉、详情回显可正常使用。
-- 幂等：按 dict_value 判重，可重复执行
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '供应商类型', 'wms_supplier_type', '0', 'admin', NOW(), '供应商类型（含金蝶供应商分类）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'wms_supplier_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 10, '原材料', '原材料', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '原材料');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 11, '包装材料', '包装材料', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '包装材料');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 12, '服务类', '服务类', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '服务类');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 13, '机器设备', '机器设备', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '机器设备');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 14, '渠道合作', '渠道合作', 'wms_supplier_type', '', '', 'N', '0', 'admin', NOW(), '金蝶供应商分类'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'wms_supplier_type' AND dict_value = '渠道合作');
