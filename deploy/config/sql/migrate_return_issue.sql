-- =============================================
-- 退料管理改造：关联领料单
-- 1. mms_return_material 表增加 issue_id 字段
-- 2. mms_return_detail 表增加 issue_qty 字段
-- 3. 退料增加确认退料权限 mms:return:confirm
-- =============================================

USE `ry-vue`;

-- 1. 退料主表增加 issue_id 字段（关联领料单）
ALTER TABLE `mms_return_material` ADD COLUMN `issue_id` bigint(20) DEFAULT NULL COMMENT '领料单ID' AFTER `return_no`;

-- 2. 退料明细表增加 issue_qty 字段（领料数量，只读参考）
ALTER TABLE `mms_return_detail` ADD COLUMN `issue_qty` decimal(18,2) DEFAULT NULL COMMENT '领料数量（来源领料明细）' AFTER `return_qty`;

-- 3. 新增确认退料权限按钮（parent_id=5110 对应退料管理菜单）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '确认退料', 5110, 6, '', NULL, NULL, 1, 0, 'F', '0', '0', 'mms:return:confirm', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'mms:return:confirm');
