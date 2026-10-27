-- 转正/调动/离职：审批日志表 + 审批状态默认值 + 审批按钮权限
-- 1. 审批日志表
CREATE TABLE IF NOT EXISTS biz_hr_regular_audit_log (
  log_id bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  regular_id bigint NOT NULL COMMENT '转正单ID',
  audit_action varchar(2) NOT NULL COMMENT '审核动作（1通过 2驳回）',
  audit_by varchar(64) NOT NULL COMMENT '审核人',
  audit_time datetime NOT NULL COMMENT '审核时间',
  audit_remark varchar(500) DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (log_id),
  KEY idx_regular_id (regular_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='转正审核日志';

CREATE TABLE IF NOT EXISTS biz_hr_transfer_audit_log (
  log_id bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  transfer_id bigint NOT NULL COMMENT '调动单ID',
  audit_action varchar(2) NOT NULL COMMENT '审核动作（1通过 2驳回）',
  audit_by varchar(64) NOT NULL COMMENT '审核人',
  audit_time datetime NOT NULL COMMENT '审核时间',
  audit_remark varchar(500) DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (log_id),
  KEY idx_transfer_id (transfer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调动审核日志';

CREATE TABLE IF NOT EXISTS biz_hr_leave_audit_log (
  log_id bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  leave_id bigint NOT NULL COMMENT '离职单ID',
  audit_action varchar(2) NOT NULL COMMENT '审核动作（1通过 2驳回）',
  audit_by varchar(64) NOT NULL COMMENT '审核人',
  audit_time datetime NOT NULL COMMENT '审核时间',
  audit_remark varchar(500) DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (log_id),
  KEY idx_leave_id (leave_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='离职审核日志';

-- 2. 审批状态默认值与存量回填
ALTER TABLE biz_hr_regular MODIFY COLUMN audit_status char(1) DEFAULT '0' COMMENT '审批状态（0待审批 1已审核 2已驳回）';
UPDATE biz_hr_regular SET audit_status = '0' WHERE audit_status IS NULL;

ALTER TABLE biz_hr_transfer MODIFY COLUMN audit_status char(1) DEFAULT '0' COMMENT '审批状态（0待审批 1已审核 2已驳回）';
UPDATE biz_hr_transfer SET audit_status = '0' WHERE audit_status IS NULL;

ALTER TABLE biz_hr_leave MODIFY COLUMN audit_status char(1) DEFAULT '0' COMMENT '审批状态（0待审批 1已审核 2已驳回）';
UPDATE biz_hr_leave SET audit_status = '0' WHERE audit_status IS NULL;

-- 3. 审批按钮权限（幂等）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '转正审批', 905561, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'hr:regular:audit', '#', 'admin', NOW(), '', NULL, '转正审批按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:regular:audit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '调动审批', 905566, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'hr:transfer:audit', '#', 'admin', NOW(), '', NULL, '调动审批按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:transfer:audit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT '离职审批', 905571, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'hr:leave:audit', '#', 'admin', NOW(), '', NULL, '离职审批按钮'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:leave:audit');
