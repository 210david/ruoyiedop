-- =============================================
-- 供应商资质状态字典清理
-- 去掉"待审核"(0)和"审核不通过"(2)状态
-- =============================================

-- 1. 将现有资质数据中 status='0'(待审核) 或 status='2'(审核不通过) 的记录更新为 status='1'(有效)
UPDATE pms_supplier_qualification
SET status = '1'
WHERE status IN ('0', '2');

-- 2. 删除字典数据中的"待审核"和"审核不通过"
DELETE FROM sys_dict_data
WHERE dict_type = 'pms_qual_status' AND dict_value IN ('0', '2');

-- 3. 验证结果
SELECT dict_label, dict_value, list_class
FROM sys_dict_data
WHERE dict_type = 'pms_qual_status'
ORDER BY dict_sort;

-- 4. 验证资质数据状态分布
SELECT status, COUNT(*) AS cnt
FROM pms_supplier_qualification
GROUP BY status;
