-- =============================================================================
-- P0-批次三：召回隔离字典（幂等：先删后插）
-- =============================================================================

DELETE FROM `sys_dict_data` WHERE dict_type IN ('food_recall_scope','food_recall_level','food_recall_status');
DELETE FROM `sys_dict_type` WHERE dict_type IN ('food_recall_scope','food_recall_level','food_recall_status');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('召回范围', 'food_recall_scope',  '0', 'admin', sysdate(), '食品批次召回范围'),
('召回等级', 'food_recall_level',  '0', 'admin', sysdate(), '食品批次召回等级'),
('召回单状态', 'food_recall_status', '0', 'admin', sysdate(), '食品批次召回单状态');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, '正向（下游去向）', '1', 'food_recall_scope', '', '', 'N', '0', 'admin', sysdate()),
(2, '反向（上游原料）', '2', 'food_recall_scope', '', '', 'N', '0', 'admin', sysdate()),
(3, '双向',             '3', 'food_recall_scope', '', '', 'Y', '0', 'admin', sysdate()),
(1, '一般', '1', 'food_recall_level', '', 'info',    'N', '0', 'admin', sysdate()),
(2, '重要', '2', 'food_recall_level', '', 'warning', 'N', '0', 'admin', sysdate()),
(3, '紧急', '3', 'food_recall_level', '', 'danger',  'N', '0', 'admin', sysdate()),
(1, '待执行', '0', 'food_recall_status', '', 'warning', 'N', '0', 'admin', sysdate()),
(2, '已执行', '1', 'food_recall_status', '', 'success', 'N', '0', 'admin', sysdate()),
(3, '已取消', '2', 'food_recall_status', '', 'info',    'N', '0', 'admin', sysdate());
