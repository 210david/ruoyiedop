-- =====================================================
-- 排产状态字典更新：简化流程，去掉"待确认"和"已确认"
-- 新状态：0=已下达, 1=已取消
-- =====================================================

-- 先更新已有排产数据的状态值：2(已下达)->0, 3(已取消)->1, 0(待确认)->0, 1(已确认)->0
UPDATE mms_schedule SET status = '0' WHERE status IN ('0', '1', '2');
UPDATE mms_schedule SET status = '1' WHERE status = '3';

-- 删除旧字典数据
DELETE FROM sys_dict_data WHERE dict_type = 'mms_schedule_status';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_schedule_status';

-- 重新插入字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('排产状态', 'mms_schedule_status', '0', 'admin', sysdate(), '排产任务状态');

-- 插入新字典数据
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已下达', '0', 'mms_schedule_status', '', 'success', 'Y', '0', 'admin', sysdate(), '已下达到生产执行'),
(2, '已取消', '1', 'mms_schedule_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已取消排产');
