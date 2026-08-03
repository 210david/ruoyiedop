-- =============================================
-- 营销订单审核功能升级脚本
-- 版本: v3
-- 日期: 2026-08-02
-- 说明: 为营销订单增加审核流程（提交→待审核→审核通过/驳回→已确认→发货...）
-- =============================================

-- 1. 为 mk_order 表增加审核相关字段
ALTER TABLE `mk_order`
ADD COLUMN `audit_by` varchar(64) DEFAULT NULL COMMENT '审核人' AFTER `cancel_reason`,
ADD COLUMN `audit_time` datetime DEFAULT NULL COMMENT '审核时间' AFTER `audit_by`,
ADD COLUMN `audit_opinion` varchar(500) DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`;

-- 2. 创建营销订单审核日志表
DROP TABLE IF EXISTS `mk_order_audit_log`;
CREATE TABLE `mk_order_audit_log` (
`log_id`        bigint        NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
`order_id`      bigint        NOT NULL                 COMMENT '订单ID',
`audit_action`  varchar(2)    DEFAULT NULL             COMMENT '审核动作（1通过 2驳回）',
`audit_by`      varchar(64)   DEFAULT NULL             COMMENT '审核人',
`audit_time`    datetime                               COMMENT '审核时间',
`audit_remark`  varchar(500)  DEFAULT NULL             COMMENT '审核意见',
PRIMARY KEY (`log_id`),
KEY `idx_audit_log_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销订单审核日志表';

-- 3. 更新字典数据：修改 marketing_order_status
-- 将原 status=1 从"已确认"改为"待审核"
UPDATE `sys_dict_data` SET `dict_label` = '待审核', `list_class` = 'warning' WHERE `dict_type` = 'marketing_order_status' AND `dict_value` = '1';

-- 新增 status=8 "已确认"（审核通过后的状态）
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(9, '已确认', '8', 'marketing_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '审核通过后的状态');

-- 新增 status=9 "已驳回"（审核驳回后的状态）
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`) VALUES
(10, '已驳回', '9', 'marketing_order_status', '', 'danger', 'N', '0', 'admin', sysdate(), '审核驳回后的状态');

-- 4. 迁移已有数据：原 status=1（已确认）的订单需迁移为 status=8（已确认/审核通过）
-- 注意：只有当前处于"已确认"状态的订单才迁移，已流转到后续状态的订单无需迁移
UPDATE `mk_order` SET `order_status` = '8' WHERE `order_status` = '1';

-- 5. 新增审核权限菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('订单审核', 0, 0, '', NULL, '', '', 1, 0, 'F', '0', '0', 'marketing:order:approve', '#', 'admin', sysdate(), '');
