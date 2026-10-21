-- =====================================================
-- 异常管理 ↔ 停机记录 联动改造 SQL
-- 1. 停机记录表增加 abnormal_id 关联字段 + abnormal_no 冗余字段
-- 2. 停机记录列表查询增加异常单号关联展示
-- 3. 看板 overview 接口增加今日停机时长统计
-- =====================================================

USE `ry-vue`;

-- 1. 停机记录表增加关联字段
ALTER TABLE `mms_downtime`
    ADD COLUMN `abnormal_id` BIGINT NULL COMMENT '关联异常单ID' AFTER `dispatch_id`,
    ADD COLUMN `abnormal_no` VARCHAR(64) NULL COMMENT '关联异常单号(冗余)' AFTER `abnormal_id`;

-- 2. 添加索引
ALTER TABLE `mms_downtime`
    ADD INDEX `idx_abnormal_id` (`abnormal_id`);

-- 3. 停机记录菜单增加"关联异常单号"列展示（前端已处理，此处仅数据层）

-- 4. 字典补充：停机类型字典（用于联动时自动设置停机类型）
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '停机类型', 'mms_downtime_type', '0', 'admin', sysdate(), '停机类型字典'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mms_downtime_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '计划停机', '0', 'mms_downtime_type', '', 'info', 'N', '0', 'admin', sysdate(), '计划内停机（保养、换型等）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_type' AND `dict_value` = '0');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '故障停机', '1', 'mms_downtime_type', '', 'danger', 'N', '0', 'admin', sysdate(), '设备故障导致的停机'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_type' AND `dict_value` = '1');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '换型停机', '2', 'mms_downtime_type', '', 'warning', 'N', '0', 'admin', sysdate(), '产品换型导致的停机'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_type' AND `dict_value` = '2');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '物料停机', '3', 'mms_downtime_type', '', 'warning', 'N', '0', 'admin', sysdate(), '物料短缺导致的停机'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_type' AND `dict_value` = '3');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '其他停机', '9', 'mms_downtime_type', '', 'info', 'N', '0', 'admin', sysdate(), '其他原因停机'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_type' AND `dict_value` = '9');

-- 5. 异常管理菜单增加"联动停机"按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 904505, '联动停机', 9045, 5, '', '', '', 1, 0, 'F', '0', '0', 'mms:abnormal:linkDowntime', '#', 'admin', sysdate(), '', NULL, '异常联动生成停机记录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 904505);

-- 验证
SELECT 'mms_downtime columns:' AS info;
SHOW COLUMNS FROM `mms_downtime` LIKE 'abnormal%';
