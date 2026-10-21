-- ============================================================
-- 工艺路线审批功能升级 - 审核日志表 + 审核意见字段 + 已驳回状态
-- 1. 主表增加 audit_remark 字段（审核意见）
-- 2. 新增工艺路线审核日志表 mms_route_audit_log
-- 3. 更新状态字典：增加已驳回状态(4)
-- 4. 更新变更类型字典：增加审核驳回
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. mms_route 主表增加 audit_remark 字段
-- ============================================================
SET @s = CONCAT('ALTER TABLE `mms_route` ADD COLUMN `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT ''审核意见'' AFTER `audit_time`');
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mms_route' AND COLUMN_NAME = 'audit_remark');
SET @sql = IF(@col_exists = 0, @s, 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. 新增工艺路线审核日志表 mms_route_audit_log
-- ============================================================
CREATE TABLE IF NOT EXISTS `mms_route_audit_log` (
  `log_id`        BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `route_id`      BIGINT(20)    NOT NULL                 COMMENT '工艺路线ID',
  `audit_action`  VARCHAR(2)    DEFAULT ''               COMMENT '审核动作（1通过 2驳回）',
  `audit_by`      VARCHAR(64)   DEFAULT ''               COMMENT '审核人',
  `audit_time`    DATETIME      DEFAULT NULL             COMMENT '审核时间',
  `audit_remark`  VARCHAR(500)  DEFAULT NULL             COMMENT '审核意见',
  PRIMARY KEY (`log_id`),
  KEY `idx_route_id` (`route_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='工艺路线审核日志表';

-- ============================================================
-- 3. 更新字典：工艺路线状态增加已驳回(4)
-- ============================================================
-- 先删除旧字典，重新插入完整状态列表
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_route_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'mms_route_status';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('工艺路线状态', 'mms_route_status', '0', 'admin', sysdate(), '工艺路线状态：草稿/待审核/已审核/已停用/已驳回');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, '草稿',   '0', 'mms_route_status', '', 'info',    'Y', '0', 'admin', sysdate(), '草稿'),
(2, '待审核', '1', 'mms_route_status', '', 'primary', 'N', '0', 'admin', sysdate(), '待审核'),
(3, '已审核', '2', 'mms_route_status', '', 'success', 'N', '0', 'admin', sysdate(), '已审核'),
(4, '已停用', '3', 'mms_route_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已停用'),
(5, '已驳回', '4', 'mms_route_status', '', 'danger',  'N', '0', 'admin', sysdate(), '已驳回');

-- ============================================================
-- 4. 更新变更类型字典：增加审核驳回
-- ============================================================
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES (7, '审核驳回', 'reject', 'mms_route_change_type', '', 'danger', 'N', '0', 'admin', sysdate(), '审核驳回')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label);

-- ============================================================
-- 5. 更新版本变更记录的变更类型说明
-- ============================================================
-- 将 change_type='audit' 的记录中描述为"审核通过"的保持不变，
-- 驳回的记录使用 change_type='reject'（新记录将使用此类型）
