-- =====================================================
-- 工作日期维护管理 SQL
-- 1. 新建工作日历表 biz_hr_workday_calendar
-- 2. 新增菜单：工作日期维护
-- =====================================================

-- 1. 创建工作日历表
CREATE TABLE IF NOT EXISTS `biz_hr_workday_calendar` (
  `calendar_id`    BIGINT NOT NULL AUTO_INCREMENT COMMENT '日历ID',
  `calendar_date`  DATE NOT NULL COMMENT '日期',
  `year`           INT NOT NULL COMMENT '年份',
  `month`          INT NOT NULL COMMENT '月份(1-12)',
  `week_day`       INT DEFAULT NULL COMMENT '星期(1=周一,7=周日)',
  `date_type`      CHAR(1) NOT NULL DEFAULT '0' COMMENT '日期类型(0=工作日,1=休息日,2=法定节假日)',
  `shift_id`       BIGINT DEFAULT NULL COMMENT '关联班次ID(工作日)',
  `shift_name`     VARCHAR(100) DEFAULT NULL COMMENT '班次名称',
  `holiday_name`   VARCHAR(100) DEFAULT NULL COMMENT '节假日名称(如：春节、国庆节)',
  `status`         CHAR(1) DEFAULT '0' COMMENT '状态(0=启用 1=停用)',
  `del_flag`       CHAR(1) DEFAULT '0' COMMENT '删除标志(0=正常 2=删除)',
  `create_by`      VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time`    DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by`      VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time`    DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`calendar_id`),
  UNIQUE KEY `uk_calendar_date` (`calendar_date`),
  KEY `idx_year_month` (`year`, `month`),
  KEY `idx_date_type` (`date_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR工作日期维护表';

-- 2. 新增菜单：工作日期维护
-- 找到考勤排班的父菜单
SET @parentId = (SELECT menu_id FROM sys_menu WHERE menu_name = '考勤排班' AND parent_id != 0 LIMIT 1);
SET @hrId = (SELECT menu_id FROM sys_menu WHERE menu_name = '人力资源管理' LIMIT 1);
SET @parentId = IFNULL(@parentId, @hrId);

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('工作日期维护', @parentId, 5, 'workdayCalendar', 'hr/workdayCalendar/index', NULL, 1, 0, 'C', '0', '0', 'hr:workdayCalendar:list', 'date', 'admin', NOW(), '工作日期维护管理');

SET @menuId = LAST_INSERT_ID();

-- 按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('日历查询', @menuId, 1, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:workdayCalendar:query', '#', 'admin', NOW(), ''),
('日历新增', @menuId, 2, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:workdayCalendar:add', '#', 'admin', NOW(), ''),
('日历修改', @menuId, 3, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:workdayCalendar:edit', '#', 'admin', NOW(), ''),
('日历删除', @menuId, 4, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:workdayCalendar:remove', '#', 'admin', NOW(), ''),
('日历导出', @menuId, 5, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:workdayCalendar:export', '#', 'admin', NOW(), ''),
('日历生成', @menuId, 6, '#', '', NULL, 1, 0, 'F', '0', '0', 'hr:workdayCalendar:generate', '#', 'admin', NOW(), '');
