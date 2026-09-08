-- =====================================================
-- 排班管理升级 SQL
-- 1. 新增排班模板表 biz_hr_schedule_template
-- 2. 排班表 biz_hr_schedule 移除 rest_day 字段（休息日=不排班）
-- 3. 新增菜单：排班模板管理
-- =====================================================

-- 1. 创建排班模板表
DROP TABLE IF EXISTS `biz_hr_schedule_template`;
CREATE TABLE `biz_hr_schedule_template` (
  `template_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name`  VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_code`  VARCHAR(50) DEFAULT NULL COMMENT '模板编码',
  `cycle_days`     INT NOT NULL COMMENT '循环天数',
  `shift_sequence` TEXT NOT NULL COMMENT '班次序列JSON，如 [{"shiftId":1,"shiftName":"早班","day":1},{"shiftId":0,"shiftName":"休息","day":2}]',
  `rotation_type`  CHAR(1) DEFAULT '0' COMMENT '轮转方向(0正向1反向)',
  `description`    VARCHAR(500) DEFAULT NULL COMMENT '模板说明',
  `status`         CHAR(1) DEFAULT '0' COMMENT '状态(0启用1停用)',
  `del_flag`       CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by`      VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time`    DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by`      VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time`    DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR排班模板表';

-- 2. 插入示例模板数据
INSERT INTO `biz_hr_schedule_template` (`template_name`, `template_code`, `cycle_days`, `shift_sequence`, `rotation_type`, `description`, `status`, `create_by`, `create_time`, `remark`) VALUES
('常白班（周一至周五）', 'DAY_SHIFT', 7, '[{"shiftId":1,"shiftName":"白班","day":1},{"shiftId":1,"shiftName":"白班","day":2},{"shiftId":1,"shiftName":"白班","day":3},{"shiftId":1,"shiftName":"白班","day":4},{"shiftId":1,"shiftName":"白班","day":5},{"shiftId":0,"shiftName":"休息","day":6},{"shiftId":0,"shiftName":"休息","day":7}]', '0', '标准白班：周一至周五白班，周末双休', '0', 'admin', NOW(), '常白班模板'),
('两班倒（早夜轮转）', 'TWO_SHIFT', 4, '[{"shiftId":1,"shiftName":"早班","day":1},{"shiftId":1,"shiftName":"早班","day":2},{"shiftId":2,"shiftName":"夜班","day":3},{"shiftId":2,"shiftName":"夜班","day":4}]', '0', '两班倒：早早夜夜循环', '0', 'admin', NOW(), '两班倒模板'),
('三班两倒（8天循环）', 'THREE_SHIFT', 8, '[{"shiftId":1,"shiftName":"早班","day":1},{"shiftId":1,"shiftName":"早班","day":2},{"shiftId":2,"shiftName":"中班","day":3},{"shiftId":2,"shiftName":"中班","day":4},{"shiftId":3,"shiftName":"夜班","day":5},{"shiftId":3,"shiftName":"夜班","day":6},{"shiftId":0,"shiftName":"休息","day":7},{"shiftId":0,"shiftName":"休息","day":8}]', '0', '三班两倒8天循环：早早中中夜夜休休', '0', 'admin', NOW(), '三班两倒模板');

-- 3. 排班表移除 rest_day 字段（休息日 = 不排班 / shift_id 为空）
-- 注意：如果 rest_day=1 的记录已有数据，先将其 shift_id 设为 NULL，shift_name 设为 '休息'
UPDATE `biz_hr_schedule` SET `shift_id` = NULL, `shift_name` = '休息' WHERE `rest_day` = 1 AND (`shift_id` IS NULL OR `shift_id` = 0);
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_hr_schedule' AND COLUMN_NAME = 'rest_day');
SET @sql = IF(@col_exists > 0, 'ALTER TABLE `biz_hr_schedule` DROP COLUMN `rest_day`', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 新增排班模板菜单
-- 先查父菜单ID（排班管理所属的HR菜单目录）
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '考勤排班' AND parent_id != 0 LIMIT 1);
-- 如果找不到则用HR根目录
SET @hrId = (SELECT menu_id FROM sys_menu WHERE menu_name = '人力资源管理' LIMIT 1);
SET @parentId = IFNULL(@parentId, @hrId);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('排班模板', @parentId, 2, 'scheduleTemplate', 'hr/scheduleTemplate/index', NULL, 1, 0, 'C', '0', '0', 'hr:scheduleTemplate:list', 'date', 'admin', NOW(), '排班轮转模板管理');

-- 获取刚插入的菜单ID
SET @templateMenuId = LAST_INSERT_ID();

-- 排班模板按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('模板查询', @templateMenuId, 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:scheduleTemplate:query', '#', 'admin', NOW(), ''),
('模板新增', @templateMenuId, 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:scheduleTemplate:add', '#', 'admin', NOW(), ''),
('模板修改', @templateMenuId, 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:scheduleTemplate:edit', '#', 'admin', NOW(), ''),
('模板删除', @templateMenuId, 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:scheduleTemplate:remove', '#', 'admin', NOW(), ''),
('模板导出', @templateMenuId, 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:scheduleTemplate:export', '#', 'admin', NOW(), '');

-- 5. 给排班表添加批量排班相关权限按钮
SET @scheduleMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'hr:schedule:list' LIMIT 1);
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('批量排班', @scheduleMenuId, 6, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:schedule:batch', '#', 'admin', NOW(), ''),
('清除排班', @scheduleMenuId, 7, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:schedule:clear', '#', 'admin', NOW(), '');
