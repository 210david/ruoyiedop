-- ---------------------------------------------------------------------------
-- 项目立项字典调整（2026-11-23）
--   1. pjm_project_type(项目类型) 调整为：
--      1=钢结构工程专业承包 2=建筑工程施工总承包 3=防水防腐保温工程专业承包
--      4=建筑机电安装工程专业承包 5=建筑装修装饰工程专业承包 8=研发项目 9=其他
--      原 6=基础设施 7=EPC总承包 停用（status='1'，保留历史数据可读性）
--   2. 工程类别(engineering_category)字段前端改为手动输入，pjm_engineering_category
--      字典保留（安全 risk 页面仍在使用），项目立项不再级联引用。
-- 兼容性：复用原 dict_value，存量 project_type='1' 数据自动映射新标签。
-- 幂等性：可重复执行。
-- ---------------------------------------------------------------------------

-- ---------- 1. 更新项目类型标签 ----------
UPDATE `sys_dict_data` SET `dict_label`='钢结构工程专业承包',   `dict_sort`=1, `list_class`='primary' WHERE `dict_type`='pjm_project_type' AND `dict_value`='1';
UPDATE `sys_dict_data` SET `dict_label`='建筑工程施工总承包',   `dict_sort`=2, `list_class`='success' WHERE `dict_type`='pjm_project_type' AND `dict_value`='2';
UPDATE `sys_dict_data` SET `dict_label`='防水防腐保温工程专业承包', `dict_sort`=3, `list_class`='info' WHERE `dict_type`='pjm_project_type' AND `dict_value`='3';
UPDATE `sys_dict_data` SET `dict_label`='建筑机电安装工程专业承包', `dict_sort`=4, `list_class`='warning' WHERE `dict_type`='pjm_project_type' AND `dict_value`='4';
UPDATE `sys_dict_data` SET `dict_label`='建筑装修装饰工程专业承包', `dict_sort`=5, `list_class`='primary' WHERE `dict_type`='pjm_project_type' AND `dict_value`='5';
UPDATE `sys_dict_data` SET `dict_label`='研发项目',             `dict_sort`=6, `list_class`='default' WHERE `dict_type`='pjm_project_type' AND `dict_value`='8';

-- ---------- 2. 停用不再使用的项目类型 ----------
UPDATE `sys_dict_data` SET `status`='1' WHERE `dict_type`='pjm_project_type' AND `dict_value` IN ('6','7');

-- ---------- 3. 新增「其他」 ----------
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 7,'其他','9','pjm_project_type',NULL,'default','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='9');
