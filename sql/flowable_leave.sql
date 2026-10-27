-- =============================================
-- 请假流程示例 - 业务表 + 菜单（幂等）
-- =============================================
SET NAMES utf8mb4;

-- ========== 业务表 ==========
CREATE TABLE IF NOT EXISTS `flow_leave` (
  `leave_id`        bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '请假单ID',
  `title`           varchar(100) NOT NULL             COMMENT '请假标题',
  `leave_type`      char(1)      DEFAULT '1'          COMMENT '请假类型（1事假 2病假 3年假 4调休）',
  `start_date`      date         DEFAULT NULL         COMMENT '开始日期',
  `end_date`        date         DEFAULT NULL         COMMENT '结束日期',
  `days`            decimal(5,1) DEFAULT 0            COMMENT '请假天数',
  `reason`          varchar(500) DEFAULT NULL         COMMENT '请假事由',
  `status`          char(1)      DEFAULT '0'          COMMENT '状态（0草稿 1审批中 2已通过 3已驳回 4已撤销）',
  `instance_id`     varchar(64)  DEFAULT NULL         COMMENT '流程实例ID',
  `start_user_id`   bigint(20)   DEFAULT NULL         COMMENT '申请人ID',
  `start_user_name` varchar(50)  DEFAULT NULL         COMMENT '申请人姓名',
  `audit_opinion`   varchar(500) DEFAULT NULL         COMMENT '最终审批意见',
  `del_flag`        char(1)      DEFAULT '0'          COMMENT '删除标志（0存在 2删除）',
  `create_by`       varchar(64)  DEFAULT ''           COMMENT '创建者',
  `create_time`     datetime     DEFAULT NULL         COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT ''           COMMENT '更新者',
  `update_time`     datetime     DEFAULT NULL         COMMENT '更新时间',
  `remark`          varchar(500) DEFAULT NULL         COMMENT '备注',
  PRIMARY KEY (`leave_id`),
  KEY `idx_start_user` (`start_user_id`),
  KEY `idx_instance` (`instance_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='请假单（工作流示例业务表）';

-- ========== 菜单 ==========
DELETE FROM sys_menu WHERE perms LIKE 'flowable:leave:%';
SET @flowMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '工作流管理' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('请假示例', @flowMenuId, 5, 'leave', 'flowable/leave/index', 1, 0, 'C', '0', '0', 'flowable:leave:list', 'form', 'admin', sysdate(), '请假审批流程示例（含业务表联动）');
SET @p = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('请假查询', @p, 1, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:query',   '#', 'admin', sysdate(), ''),
('请假新增', @p, 2, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:add',     '#', 'admin', sysdate(), ''),
('请假提交', @p, 3, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:submit',  '#', 'admin', sysdate(), ''),
('请假删除', @p, 4, '#', '', 1, 0, 'F', '0', '0', 'flowable:leave:remove',  '#', 'admin', sysdate(), '');
