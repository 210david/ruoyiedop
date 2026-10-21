-- =====================================================
-- 回款状态字典更新：移除"已逾期"状态
-- 逾期状态由 overdue_flag 字段独立管理
-- =====================================================

-- 1. 删除回款状态中的"已逾期"字典项 (dict_value = '2')
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'marketing_payment_status' AND `dict_value` = '2';

-- 2. 确保字典项正确：待回款(0)、已回款(1)、部分回款(3)
--    如果"部分回款"不存在则插入
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES (3, '部分回款', '3', 'marketing_payment_status', 'warning', 'N', '0', 'admin', NOW(), '');

-- 3. 将已存在的 paymentStatus='2' 的回款计划修正为 paymentStatus='0'（待回款）
--    逾期状态保留在 overdue_flag 字段中
UPDATE `mk_payment_plan` SET `payment_status` = '0' WHERE `payment_status` = '2';
