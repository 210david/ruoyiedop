-- =============================================
-- 角色分类增加"质量管理"并将QMS角色归入该分类
-- 幂等可重复执行
-- =============================================

-- 1. 添加"质量管理"角色分类字典数据（如果已存在则跳过）
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT * FROM (
  SELECT 7 AS dict_sort, '质量管理' AS dict_label, 'qms' AS dict_value, 'sys_role_category' AS dict_type, '' AS css_class, 'primary' AS list_class, 'N' AS is_default, '0' AS status, 'admin' AS create_by, NOW() AS create_time, '质量管理模块角色' AS remark
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_role_category' AND `dict_value` = 'qms');

-- 2. 将7个QMS角色的role_category设置为qms
UPDATE `sys_role` SET `role_category` = 'qms'
WHERE `del_flag` = '0' AND `role_id` IN (120, 121, 122, 123, 124, 125, 126);

-- 3. 验证结果
SELECT '===== 角色分类字典数据 =====' AS info;
SELECT dict_sort, dict_label, dict_value, list_class, is_default
FROM `sys_dict_data` WHERE `dict_type` = 'sys_role_category' ORDER BY `dict_sort`;

SELECT '===== QMS角色分类状态 =====' AS info;
SELECT role_id, role_name, role_key, role_category
FROM `sys_role` WHERE `role_id` IN (120,121,122,123,124,125,126) ORDER BY role_id;

SELECT '===== 全部角色分类汇总 =====' AS info;
SELECT role_category, GROUP_CONCAT(role_name ORDER BY role_id SEPARATOR ', ') AS roles
FROM `sys_role` WHERE `del_flag` = '0' AND `role_id` >= 100
GROUP BY role_category ORDER BY role_category;
