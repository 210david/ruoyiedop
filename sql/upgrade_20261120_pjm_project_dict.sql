-- ---------------------------------------------------------------------------
-- 补齐本地开发库缺失的项目立项字典（生产 2026-11-13 已上线，本地缺失）
--   1. sys_dict_type  : pjm_project_type(项目类型) / pjm_engineering_category(工程类别)
--   2. sys_dict_data  : 项目类型 8 条（顶级，parent_code=NULL）
--                       工程类别 27 条（parent_code 按项目类型 dict_value 前缀级联）
-- 数据来源：生产库导出 dict_data_1795160286809.xlsx + 生产 API /system/dict/data/type/*
-- 幂等性：逐条 WHERE NOT EXISTS 判重，可重复执行。
-- 前置条件：sys_dict_data 需已有 parent_code 列（见 sql/upgrade_20261120_dict_parent_code.sql）。
-- ---------------------------------------------------------------------------

-- ---------- 1. 字典类型 ----------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `dict_category`, `remark`)
SELECT '项目类型', 'pjm_project_type', '0', 'project', '项目立项-项目类型（工程类别父级）' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_project_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `dict_category`, `remark`)
SELECT '工程类别', 'pjm_engineering_category', '0', 'project', '项目立项-工程类别级联字典（按项目类型前缀）' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_engineering_category');

-- ---------- 2. 字典数据：项目类型（8 条，顶级） ----------
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'房屋建筑','1','pjm_project_type',NULL,'primary','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'市政工程','2','pjm_project_type',NULL,'success','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'机电安装','3','pjm_project_type',NULL,'info','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'装饰装修','4','pjm_project_type',NULL,'warning','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='4');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 5,'园林绿化','5','pjm_project_type',NULL,'success','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='5');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 6,'基础设施','6','pjm_project_type',NULL,'primary','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='6');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 7,'EPC总承包','7','pjm_project_type',NULL,'violet','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='7');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 8,'研发项目','8','pjm_project_type',NULL,'default','N','0',NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_type' AND `dict_value`='8');

-- ---------- 3. 字典数据：工程类别（27 条，parent_code 级联到项目类型 dict_code） ----------
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'住宅工程','house_residential','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='1' LIMIT 1),
       'default','N','0','高层/多层住宅、公寓' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='house_residential');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'城市道路','municipal_road','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='2' LIMIT 1),
       'default','N','0','主干道/次干道/支路' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='municipal_road');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'工业机电','mep_industrial','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='3' LIMIT 1),
       'default','N','0','化工/电力/冶金/石化装置' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='mep_industrial');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'室内精装','deco_indoor','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='4' LIMIT 1),
       'default','N','0','建筑室内装饰装修' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='deco_indoor');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'公园景区','landscape_park','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='5' LIMIT 1),
       'default','N','0','综合性公园/风景区' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='landscape_park');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'交通工程','infra_transport','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='6' LIMIT 1),
       'default','N','0','公路/铁路/机场/港口' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='infra_transport');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'通用EPC','epc_general','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='7' LIMIT 1),
       'default','N','0','设计-采购-施工总承包' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='epc_general');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'公共建筑','house_public','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='1' LIMIT 1),
       'default','N','0','办公/商业/酒店/会展' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='house_public');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'桥梁工程','municipal_bridge','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='2' LIMIT 1),
       'default','N','0','城市桥梁/立交桥' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='municipal_bridge');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'建筑机电','mep_building','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='3' LIMIT 1),
       'default','N','0','水电暖通消防智能' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='mep_building');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'幕墙工程','deco_curtain','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='4' LIMIT 1),
       'default','N','0','玻璃/石材/金属幕墙' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='deco_curtain');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'道路绿化','landscape_road','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='5' LIMIT 1),
       'default','N','0','城市道路绿化景观' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='landscape_road');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'水利工程','infra_water','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='6' LIMIT 1),
       'default','N','0','水库/大坝/河道/灌溉' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='infra_water');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'工业EPC','epc_industrial','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='7' LIMIT 1),
       'default','N','0','化工/电力/冶金装置EPC' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='epc_industrial');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'工业建筑','house_industrial','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='1' LIMIT 1),
       'default','N','0','厂房/仓库/物流建筑' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='house_industrial');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'给排水工程','municipal_water','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='2' LIMIT 1),
       'default','N','0','给水/排水/污水处理' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='municipal_water');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'高压输变电','mep_high_voltage','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='3' LIMIT 1),
       'default','N','0','高压输电线路/变电站' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='mep_high_voltage');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'外立面改造','deco_facade','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='4' LIMIT 1),
       'default','N','0','既有建筑外立面翻新' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='deco_facade');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'居住区绿化','landscape_residential','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='5' LIMIT 1),
       'default','N','0','住宅小区景观绿化' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='landscape_residential');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'能源工程','infra_energy','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='6' LIMIT 1),
       'default','N','0','火电/水电/新能源电站' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='infra_energy');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'城镇综合开发EPC','epc_urban','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='7' LIMIT 1),
       'default','N','0','新城/园区/城市更新' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='epc_urban');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'城市综合体','house_mixed','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='1' LIMIT 1),
       'default','N','0','商业+办公+住宅复合' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='house_mixed');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'燃气热力','municipal_gas_heat','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='2' LIMIT 1),
       'default','N','0','城市燃气/集中供热' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='municipal_gas_heat');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'轨道交通机电','mep_transport','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='3' LIMIT 1),
       'default','N','0','地铁/轻轨/机场机电' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='mep_transport');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'生态修复','landscape_ecology','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='5' LIMIT 1),
       'default','N','0','边坡/矿山/湿地生态修复' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='landscape_ecology');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'通信工程','infra_communication','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='6' LIMIT 1),
       'default','N','0','基站/光纤/数据中心' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='infra_communication');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`parent_code`,`list_class`,`is_default`,`status`,`remark`)
SELECT 5,'地下综合管廊','municipal_utiliduct','pjm_engineering_category',
       (SELECT dict_code FROM sys_dict_data WHERE dict_type='pjm_project_type' AND dict_value='2' LIMIT 1),
       'default','N','0','城市地下综合管廊' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_engineering_category' AND `dict_value`='municipal_utiliduct');
