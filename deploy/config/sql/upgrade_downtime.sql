-- ============================================================
-- 停机记录完善脚本
-- 1. 修正停机状态字典（0=停机中, 1=已恢复）
-- 2. 新增停机分类字典（计划/非计划）
-- 3. 新增停机级别字典（一般/重要/紧急）
-- 4. 增加表字段 dt_category, dt_level, report_by, handle_by, handle_result
-- 5. 增加5条测试数据
-- ============================================================

USE `ry-vue`;

-- ============================================================
-- 一、修正停机状态字典
-- 原: 0=计划停机, 1=故障停机, 2=已恢复（混淆了类型和状态）
-- 新: 0=停机中, 1=已恢复（纯状态）
-- ============================================================

-- 删除旧的停机状态字典数据
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_downtime_status';

-- 重新插入
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '停机中', '0', 'mms_downtime_status', '', 'danger',  'Y', '0', 'admin', sysdate(), '设备/产线停机中，尚未恢复'),
(2, '已恢复', '1', 'mms_downtime_status', '', 'success', 'N', '0', 'admin', sysdate(), '设备/产线已恢复生产');

-- 更新字典类型名称
UPDATE `sys_dict_type` SET `dict_name` = '停机状态', `remark` = '停机记录状态（停机中/已恢复）' WHERE `dict_type` = 'mms_downtime_status';

-- ============================================================
-- 二、新增停机分类字典（EMS标准：计划停机/非计划停机）
-- ============================================================

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '停机分类', 'mms_downtime_category', '0', 'admin', sysdate(), 'EMS标准停机分类（计划/非计划）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mms_downtime_category');
UPDATE `sys_dict_type` SET `dict_name` = '停机分类', `remark` = 'EMS标准停机分类（计划/非计划）' WHERE `dict_type` = 'mms_downtime_category';

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '计划停机',   '0', 'mms_downtime_category', '', 'info',    'Y', '0', 'admin', sysdate(), '计划内停机（保养、换型、培训等）'),
(2, '非计划停机', '1', 'mms_downtime_category', '', 'danger',  'N', '0', 'admin', sysdate(), '非计划停机（故障、物料短缺等）')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`), `list_class` = VALUES(`list_class`), `remark` = VALUES(`remark`);

-- ============================================================
-- 三、新增停机级别字典
-- ============================================================

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '停机级别', 'mms_downtime_level', '0', 'admin', sysdate(), '停机事件级别（一般/重要/紧急）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mms_downtime_level');
UPDATE `sys_dict_type` SET `dict_name` = '停机级别', `remark` = '停机事件级别（一般/重要/紧急）' WHERE `dict_type` = 'mms_downtime_level';

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '一般', '0', 'mms_downtime_level', '', 'info',    'Y', '0', 'admin', sysdate(), '一般停机，影响较小'),
(2, '重要', '1', 'mms_downtime_level', '', 'warning', 'N', '0', 'admin', sysdate(), '重要停机，影响生产进度'),
(3, '紧急', '2', 'mms_downtime_level', '', 'danger',  'N', '0', 'admin', sysdate(), '紧急停机，需立即处理')
ON DUPLICATE KEY UPDATE `dict_label` = VALUES(`dict_label`), `list_class` = VALUES(`list_class`), `remark` = VALUES(`remark`);

-- ============================================================
-- 四、增加表字段
-- ============================================================

-- 停机分类
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME = 'mms_downtime' AND COLUMN_NAME = 'dt_category'),
  'SELECT 1', 'ALTER TABLE `mms_downtime` ADD COLUMN `dt_category` varchar(1) DEFAULT ''0'' COMMENT ''停机分类(0=计划停机,1=非计划停机)'' AFTER `dt_type`');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 停机级别
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME = 'mms_downtime' AND COLUMN_NAME = 'dt_level'),
  'SELECT 1', 'ALTER TABLE `mms_downtime` ADD COLUMN `dt_level` varchar(1) DEFAULT ''0'' COMMENT ''停机级别(0=一般,1=重要,2=紧急)'' AFTER `dt_category`');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 上报人
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME = 'mms_downtime' AND COLUMN_NAME = 'report_by'),
  'SELECT 1', 'ALTER TABLE `mms_downtime` ADD COLUMN `report_by` varchar(64) DEFAULT NULL COMMENT ''上报人'' AFTER `status`');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 处理人
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME = 'mms_downtime' AND COLUMN_NAME = 'handle_by'),
  'SELECT 1', 'ALTER TABLE `mms_downtime` ADD COLUMN `handle_by` varchar(64) DEFAULT NULL COMMENT ''处理人'' AFTER `report_by`');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 处理结果
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry-vue' AND TABLE_NAME = 'mms_downtime' AND COLUMN_NAME = 'handle_result'),
  'SELECT 1', 'ALTER TABLE `mms_downtime` ADD COLUMN `handle_result` varchar(500) DEFAULT NULL COMMENT ''处理结果'' AFTER `handle_by`');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 五、修正旧数据状态（将 status=2 的旧"已恢复"改为 status=1）
-- ============================================================
UPDATE `mms_downtime` SET `status` = '1' WHERE `status` = '2';

-- ============================================================
-- 六、增加5条测试数据
-- ============================================================

INSERT INTO `mms_downtime` (`downtime_no`, `resource_id`, `resource_name`, `start_time`, `end_time`, `dt_type`, `dt_category`, `dt_level`, `reason`, `minutes`, `status`, `report_by`, `handle_by`, `handle_result`, `del_flag`, `create_by`, `create_time`, `remark`) VALUES
('DT20260823001', 1, 'CNC加工中心-A01', '2026-08-20 08:30:00', '2026-08-20 10:15:00', '1', '1', '2', '主轴轴承过热报警，停机检查并更换润滑脂', 105, '1', '张操作', '李维修', '更换主轴润滑脂，测试运行正常，已恢复生产', '0', 'admin', '2026-08-20 08:30:00', 'CNC-A01故障停机'),
('DT20260823002', 2, '注塑机-B03', '2026-08-21 14:00:00', '2026-08-21 14:45:00', '2', '0', '0', '产品换型，由A产品切换至B产品，需清洗模具和调试参数', 45, '1', '王操作', '赵技术员', '完成换型调试，首件检验合格，恢复生产', '0', 'admin', '2026-08-21 14:00:00', '注塑机换型停机'),
('DT20260823003', 3, '装配线-C02', '2026-08-22 09:15:00', NULL, '3', '1', '1', '关键物料（螺栓M8x30）供应延迟，等待来料', NULL, '0', '陈班长', NULL, NULL, '0', 'admin', '2026-08-22 09:15:00', '物料短缺导致停机，待恢复'),
('DT20260823004', 4, '包装线-D01', '2026-08-22 16:00:00', '2026-08-22 17:30:00', '0', '0', '0', '设备日常保养和周保计划', 90, '1', '刘班长', '孙维修', '完成周保计划，润滑、紧固、清洁完毕', '0', 'admin', '2026-08-22 16:00:00', '计划保养停机'),
('DT20260823005', 5, '焊接机器人-E02', '2026-08-23 07:45:00', '2026-08-23 09:20:00', '1', '1', '2', '焊枪气缸故障，无法正常送丝', 95, '1', '周操作', '吴维修', '更换焊枪气缸密封圈和送丝轮，测试焊接正常', '0', 'admin', '2026-08-23 07:45:00', '焊接机器人故障停机');
