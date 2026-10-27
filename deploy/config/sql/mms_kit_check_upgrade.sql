-- =====================================================
-- 齐套检查功能升级脚本
-- 1. 补充 mms_kit_check_detail 表的 update_by/update_time/remark 字段
-- 2. 补充 is_complete 筛选条件索引
-- 3. 补充 mms_kit_check 表的 is_complete 筛选索引
-- =====================================================

-- 1. mms_kit_check_detail 表增加 update_by/update_time/remark 字段
ALTER TABLE `mms_kit_check_detail` ADD COLUMN IF NOT EXISTS `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者' AFTER `create_time`;
ALTER TABLE `mms_kit_check_detail` ADD COLUMN IF NOT EXISTS `update_time` DATETIME DEFAULT NULL COMMENT '更新时间' AFTER `update_by`;
ALTER TABLE `mms_kit_check_detail` ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER `update_time`;

-- 2. mms_kit_check_detail 表增加索引（按 kit_id 查询已在建表时创建）
ALTER TABLE `mms_kit_check_detail` ADD INDEX IF NOT EXISTS `idx_material_id` (`material_id`);

-- 3. mms_kit_check 表增加索引（按 is_complete 和 status 筛选）
ALTER TABLE `mms_kit_check` ADD INDEX IF NOT EXISTS `idx_is_complete` (`is_complete`);
ALTER TABLE `mms_kit_check` ADD INDEX IF NOT EXISTS `idx_status` (`status`);
ALTER TABLE `mms_kit_check` ADD INDEX IF NOT EXISTS `idx_work_order_no` (`work_order_no`);
ALTER TABLE `mms_kit_check` ADD INDEX IF NOT EXISTS `idx_mps_no` (`mps_no`);

-- 4. 如果 mms_kit_status 字典没有 "缺料" 状态，补充
-- 状态: 0=待检查, 1=已通过, 2=缺料
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`)
SELECT 3, '缺料', '2', 'mms_kit_status', '', 'danger', 'N', '0', '齐套检查-缺料状态', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'mms_kit_status' AND `dict_value` = '2'
);
