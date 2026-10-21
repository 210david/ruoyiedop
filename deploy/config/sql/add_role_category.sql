-- ----------------------------
-- 角色分类管理 - 添加 role_category 字段和字典数据
-- ----------------------------

-- 1. 给 sys_role 表添加 role_category 字段
ALTER TABLE `sys_role` ADD COLUMN `role_category` varchar(50) DEFAULT 'system' COMMENT '角色分类（业务模块）' AFTER `role_sort`;

-- 2. 自动根据 role_key 对现有角色进行分类
-- 营销管理模块（role_key 包含 mk 或 marketing）
UPDATE `sys_role` SET `role_category` = 'mk' WHERE `del_flag` = '0' AND (`role_key` LIKE '%mk%' OR `role_key` LIKE '%marketing%');

-- 设备管理模块（role_key 包含 dms）
UPDATE `sys_role` SET `role_category` = 'dms' WHERE `del_flag` = '0' AND `role_key` LIKE '%dms%';

-- 采购管理模块（role_key 包含 pms）
UPDATE `sys_role` SET `role_category` = 'pms' WHERE `del_flag` = '0' AND `role_key` LIKE '%pms%';

-- 仓储管理模块（role_key 包含 wms）
UPDATE `sys_role` SET `role_category` = 'wms' WHERE `del_flag` = '0' AND `role_key` LIKE '%wms%';

-- 安全生产管理模块（role_key 包含 safety）
UPDATE `sys_role` SET `role_category` = 'safety' WHERE `del_flag` = '0' AND `role_key` LIKE '%safety%';

-- 质量管理模块（role_key 包含 qms）
UPDATE `sys_role` SET `role_category` = 'qms' WHERE `del_flag` = '0' AND `role_key` LIKE '%qms%';

-- 其余角色（admin、common 等）默认归为系统管理
UPDATE `sys_role` SET `role_category` = 'system' WHERE `del_flag` = '0' AND (`role_category` IS NULL OR `role_category` = '');

-- 3. 添加角色分类字典类型（如果已存在则跳过）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '角色分类', 'sys_role_category', '0', 'admin', sysdate(), '角色所属业务模块分类'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'sys_role_category');

-- 4. 添加角色分类字典数据（如果已存在则跳过）
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT * FROM (
  SELECT 1 AS dict_sort, '系统管理' AS dict_label, 'system' AS dict_value, 'sys_role_category' AS dict_type, '' AS css_class, 'primary' AS list_class, 'Y' AS is_default, '0' AS status, 'admin' AS create_by, sysdate() AS create_time, '系统管理模块角色' AS remark
  UNION ALL
  SELECT 2, '设备管理', 'dms', 'sys_role_category', '', 'success', 'N', '0', 'admin', sysdate(), '设备管理模块角色'
  UNION ALL
  SELECT 3, '采购管理', 'pms', 'sys_role_category', '', 'warning', 'N', '0', 'admin', sysdate(), '采购管理模块角色'
  UNION ALL
  SELECT 4, '营销管理', 'mk', 'sys_role_category', '', 'danger', 'N', '0', 'admin', sysdate(), '营销管理模块角色'
  UNION ALL
  SELECT 5, '仓储管理', 'wms', 'sys_role_category', '', 'info', 'N', '0', 'admin', sysdate(), '仓储管理模块角色'
  UNION ALL
  SELECT 6, '安全生产管理', 'safety', 'sys_role_category', '', 'warning', 'N', '0', 'admin', sysdate(), '安全生产管理模块角色'
  UNION ALL
  SELECT 7, '质量管理', 'qms', 'sys_role_category', '', 'success', 'N', '0', 'admin', sysdate(), '质量管理模块角色'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_role_category' AND `dict_value` = tmp.dict_value);
