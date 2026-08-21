-- =====================================================
-- 排产状态数据修复：统一为新状态体系
-- 新状态：0=已下达, 1=已取消
-- 旧状态：0=待确认, 1=已确认, 2=已下达, 3=已取消
-- =====================================================

USE `ry-vue`;

-- 1. 修复排产数据状态值
-- 旧 0(待确认)、1(已确认)、2(已下达) → 新 0(已下达)
UPDATE mms_schedule_task SET status = '0' WHERE status IN ('0', '1', '2');
-- 旧 3(已取消) → 新 1(已取消)
UPDATE mms_schedule_task SET status = '1' WHERE status = '3';

-- 2. 更新字典数据（先删后插）
DELETE FROM sys_dict_data WHERE dict_type = 'mms_schedule_status';
DELETE FROM sys_dict_type WHERE dict_type = 'mms_schedule_status';

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('排产状态', 'mms_schedule_status', '0', 'admin', sysdate(), '排产任务状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
(1, '已下达', '0', 'mms_schedule_status', '', 'success', 'Y', '0', 'admin', sysdate(), '已下达到生产执行'),
(2, '已取消', '1', 'mms_schedule_status', '', 'danger', 'N', '0', 'admin', sysdate(), '已取消排产');

-- 3. 验证修复结果
SELECT '排产数据状态分布' AS check_item;
SELECT status, COUNT(*) AS cnt FROM mms_schedule_task WHERE del_flag = '0' GROUP BY status;

SELECT '字典数据' AS check_item;
SELECT dict_value, dict_label, list_class FROM sys_dict_data WHERE dict_type = 'mms_schedule_status' ORDER BY dict_sort;
